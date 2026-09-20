package com.swbr.orespawn.block.misc;

import com.swbr.orespawn.entity.NoStepTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Conversions from the numbers the 1.7.10 setters took to what 1.21.1 {@link BlockBehaviour.Properties}
 * stores. Every OreSpawn block class calls these with the literal from its original constructor, so the
 * original value stays visible in the code and the arithmetic lives in one place.
 *
 * <p>No original class: the 1.7.10 engine facts below are the reason this file exists.
 */
public final class Legacy {

    private Legacy() {}

    /**
     * {@code Block.setResistance(r)} stored {@code r * 3}, {@code Explosion} read it back through
     * {@code getExplosionResistance()} as {@code blockResistance / 5} and then applied
     * {@code (x + 0.3) * 0.3}. 1.21.1 applies the same {@code (x + 0.3) * 0.3} to the stored
     * {@code explosionResistance} directly, so the equivalent stored value is {@code r * 3 / 5}.
     */
    public static float resistance(float setResistance) {
        return setResistance * 3.0f / 5.0f;
    }

    /**
     * A class that called only {@code setHardness(h)}: the setter raised {@code blockResistance} to
     * {@code h * 5} when it was lower, and {@code / 5} gives {@code h} back.
     */
    public static float resistanceFromHardnessOnly(float hardness) {
        return hardness;
    }

    /** {@code Block.setLightLevel(f)}: {@code lightValue = (int) (15.0F * f)}. */
    public static int light(float setLightLevel) {
        return (int) (15.0f * setLightLevel);
    }

    /**
     * What {@code renderAsNormalBlock() == false} meant beyond rendering in 1.7.10:
     * {@code Block.isNormalCube()} was {@code material.isOpaque() && renderAsNormalBlock() && !canProvidePower()},
     * and that single flag gated mob spawning on top ({@code canCreatureSpawn -> isSideSolid}),
     * redstone conduction ({@code isBlockNormalCube}) and suffocation ({@code isNormalCube} in
     * {@code EntityLivingBase.isEntityInsideOpaqueBlock}). Together with {@code isOpaqueCube() == false}
     * the block did not cull neighbouring faces either.
     */
    public static BlockBehaviour.Properties notANormalCube(BlockBehaviour.Properties properties) {
        return properties
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    /**
     * The experience half of a 1.7.10 {@code dropBlockAsItemWithChance} override, for
     * {@code Block.spawnAfterBreak}. In 1.7.10 the method ran on every drop path - harvest,
     * {@code destroyBlock}, piston, an explosion of any cause - and was skipped only by a Silk Touch
     * harvest of a block whose {@code canSilkHarvest} was true, i.e. a normal cube.
     *
     * <p>PORT: NeoForge 21.1 calls {@code spawnAfterBreak} on all of those paths as well, but its
     * {@code dropExperience} flag is {@code false} on every one of them ({@code CommonHooks.handleBlockDrops}:
     * "Always pass false ... since we handle XP") except an explosion whose indirect source is a player
     * ({@code BlockBehaviour.onExplosionHit}); vanilla ores get their experience through {@code getExpDrop}
     * instead, which only the drop paths consult. A port that honours the flag never drops experience for a
     * harvest, and one that uses {@code getExpDrop} drops none for a creeper's or TNT's explosion. So the
     * overrides ignore the flag and roll on every call, which is what the original did, once per path.
     *
     * @param stack the harvesting tool as {@code spawnAfterBreak} received it (empty for an explosion);
     *              {@link EnchantmentHelper#processBlockExperience} zeroes the amount under Silk Touch,
     *              which is where 1.7.10 skipped the method. A block that was <em>not</em> a normal cube
     *              ({@code canSilkHarvest} false: the cross-model crystals) passes {@link ItemStack#EMPTY},
     *              because for it the method ran under Silk Touch too.
     */
    public static void dropXpOnBlockBreak(Block block, ServerLevel level, BlockPos pos, ItemStack stack, int amount) {
        int i = EnchantmentHelper.processBlockExperience(level, stack, amount);
        if (i > 0) {
            block.popExperience(level, pos, i);
        }
    }

    /**
     * The guard 1.7.10 {@code Entity.moveEntity} put in front of {@code Block.onEntityWalking}:
     * {@code canTriggerWalking() && !(onGround && isSneaking() && instanceof EntityPlayer) && ridingEntity == null}.
     * {@code stepOn} runs only while {@code onGround}, so that part is given. {@code canTriggerWalking}
     * is 1.21.1's protected {@code getMovementEmission()} and cannot be read from a block: the OreSpawn
     * entities whose original returned {@code false} implement {@link NoStepTrigger} instead
     * (DECISIONS R20, first {@code Elevator}, Elevator.java:136). The vanilla entities it excluded
     * (items, orbs, arrows, boats, minecarts) never reach the callers' checks anyway.
     *
     * <p>What this does not restore: {@code onEntityWalking} fired once per step distance
     * ({@code distanceWalkedOnStepModified > nextStepDistance}), {@code stepOn} on every {@code move}
     * call while on the ground - see {@code OreTitanium.stepOn}.
     */
    public static boolean wasWalking(Entity entity) {
        if (entity instanceof NoStepTrigger) {
            return false;
        }
        return !(entity instanceof Player && entity.isShiftKeyDown()) && !entity.isPassenger();
    }
}
