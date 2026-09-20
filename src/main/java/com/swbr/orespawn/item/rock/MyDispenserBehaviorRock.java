package com.swbr.orespawn.item.rock;

import com.swbr.orespawn.entity.rock.EntityThrownRock;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyDispenserBehaviorRock} (MyDispenserBehaviorRock.java:10-64):
 * the dispenser behaviour of all twelve rocks. The original overrode
 * {@code BehaviorProjectileDispense.dispenseStack} completely so it could set the rock type from
 * the item between creating and spawning the projectile (:12-58); the launch itself copied
 * vanilla 1.7.10 line by line.
 *
 * <p>PORT: not {@code ProjectileItem} + {@code DispenserBlock.registerProjectileBehavior}, as the
 * catalogue suggested (verhalten/core-02.md). Vanilla 1.21.1 lifts the spawn position by 0.1 and
 * shoots straight along the facing; 1.7.10 spawned at {@code centre + 0.7 * facing} and added the
 * 0.1 to the <em>direction's</em> Y (:17). This class keeps the 1.7.10 numbers; the Gaussian spread
 * comes from {@link EntityThrownRock#shoot}. The class was package-private in the original; it is
 * public so {@code platform.CommonSetup} can call {@link #registerAll()}.
 */
public final class MyDispenserBehaviorRock extends DefaultDispenseItemBehavior {

    /**
     * {@code dispenseStack} (:12-58): position {@code func_149939_a} = block centre + 0.7 in the
     * facing direction (:14), the facing (:15), a rock at that position (:16, :60-63), heading
     * {@code (fx, fy + 0.1f, fz)} at velocity {@code func_82500_b()} = 1.1 with spread
     * {@code func_82498_a()} = 6.0 (:17), the type from the item (:18-54), spawn (:55), one item
     * less (:56). Sound and smoke follow in {@code dispense}, as in {@code BehaviorDefaultDispenseItem}.
     */
    @Override
    protected ItemStack execute(final BlockSource par1IBlockSource, final ItemStack par2ItemStack) {
        final Level world = par1IBlockSource.level(); // :13
        final Position iposition = DispenserBlock.getDispensePosition(par1IBlockSource, 0.7, Vec3.ZERO); // :14
        final Direction enumfacing = par1IBlockSource.state().getValue(DispenserBlock.FACING); // :15
        final EntityThrownRock iprojectile = getProjectileEntity(world, iposition); // :16
        iprojectile.shoot(enumfacing.getStepX(), enumfacing.getStepY() + 0.1f, enumfacing.getStepZ(), 1.1f, 6.0f); // :17
        final EntityThrownRock r = iprojectile; // :18 - an unchecked cast in the original; the factory returns the type
        final Item item = par2ItemStack.getItem();
        if (item instanceof ItemRock rock) { // :19-54, one arm per item; an unknown item keeps type 0
            r.setRockType(rock.getRockType());
        }
        world.addFreshEntity(iprojectile); // :55
        par2ItemStack.split(1); // :56
        return par2ItemStack; // :57
    }

    /** {@code getProjectileEntity} (:60-63): {@code new EntityThrownRock(world, x, y, z)}. */
    private static EntityThrownRock getProjectileEntity(final Level par1World, final Position par2IPosition) {
        return new EntityThrownRock(par1World, par2IPosition.x(), par2IPosition.y(), par2IPosition.z());
    }

    /** The inherited {@code BehaviorProjectileDispense.playDispenseSound}: level event 1002 (shoot), not 1000. */
    @Override
    protected void playSound(final BlockSource blockSource) {
        blockSource.level().levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, blockSource.pos(), 0);
    }

    /**
     * {@code BlockDispenser.dispenseBehaviorRegistry.putObject(OreSpawnMain.My*Rock, new
     * MyDispenserBehaviorRock())} x12 (OreSpawnMain.java:5422-5433). Call from
     * {@code FMLCommonSetupEvent.enqueueWork}, after the items are registered.
     */
    public static void registerAll() {
        for (final ItemRock rock : ItemRock.all()) {
            DispenserBlock.registerBehavior(rock, new MyDispenserBehaviorRock());
        }
    }
}
