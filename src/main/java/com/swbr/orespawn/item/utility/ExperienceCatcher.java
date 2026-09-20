package com.swbr.orespawn.item.utility;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.ExperienceCatcher} (ExperienceCatcher.java:15-81):
 * {@code experiencecatcher} "Experience Orb Catcher" (OreSpawnMain.java:1610), stack 16, creative
 * tab {@code tabTools} (:17-20). Clicked onto the ground next to an experience orb worth at least
 * 3, it turns the orb - four times out of five - into a Bottle o' Enchanting plus the string and
 * stick the catcher was made of. Clicked with no orb to catch it drops itself on the ground.
 */
public class ExperienceCatcher extends Item {

    /** {@code ExperienceCatcher(id)} (:17-20): {@code maxStackSize = 16}. */
    public ExperienceCatcher(final Item.Properties props) {
        super(props.stacksTo(16));
    }

    /**
     * {@code onItemUse} (:22-70). Swing (:23); the debug {@code System.out.printf} of :24 is
     * not ported. Server (:25-68): the search box is one block wide around the click point and two
     * high (:26); the first orb worth 3 or more that survives the 1-in-5 roll disappears - its
     * experience is lost - and drops the three items at {@code (x + hitX, y + 1, z + hitZ)}
     * (:28-59), one catcher less outside creative. Without a catch the catcher itself drops and
     * the stack shrinks by one <em>even in creative mode</em> (:61-67, DECISIONS R18:
     * "ExperienceCatcher im Kreativmodus 1:1"). Both sides answer {@code true} (:69).
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final Player par2EntityPlayer = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        if (par2EntityPlayer == null) {
            // PORT: no player in 1.21.1 (dispenser-like callers); the original always had one.
            return InteractionResult.PASS;
        }
        final BlockPos pos = context.getClickedPos();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        // par8 / par10 were the hit position inside the clicked block, 0..1 on each axis.
        final Vec3 hit = context.getClickLocation();
        final float par8 = (float) (hit.x - x);
        final float par10 = (float) (hit.z - z);
        par2EntityPlayer.swing(context.getHand()); // :23 swingItem
        if (!world.isClientSide) { // :25
            final AABB bb = new AABB(x - 0.5 + par8, y, z - 0.5 + par10, x + 0.5 + par8, y + 2.0, z + 0.5 + par10); // :26
            final List<ExperienceOrb> var5 = world.getEntitiesOfClass(ExperienceOrb.class, bb); // :27
            for (final ExperienceOrb ex : var5) { // :28-31
                if (ex.getValue() < 3) { // :32-34 getXpValue
                    continue;
                }
                if (world.random.nextInt(5) == 1) { // :35-37
                    continue;
                }
                ex.discard(); // :38 setDead
                ItemStack is = new ItemStack(Items.EXPERIENCE_BOTTLE, 1); // :40
                world.addFreshEntity(new ItemEntity(world, par8 + x, y + 1.0, par10 + z, is)); // :41-44
                is = new ItemStack(Items.STRING, 1); // :45
                world.addFreshEntity(new ItemEntity(world, par8 + x, y + 1.0, par10 + z, is)); // :46-49
                is = new ItemStack(Items.STICK, 1); // :50
                world.addFreshEntity(new ItemEntity(world, par8 + x, y + 1.0, par10 + z, is)); // :51-54
                OneUse.consumeUnlessCreative(par1ItemStack, par2EntityPlayer); // :55-57
                return InteractionResult.sidedSuccess(false); // :58
            }
            final ItemStack is2 = new ItemStack(this, 1); // :62 MyExperienceCatcher
            world.addFreshEntity(new ItemEntity(world, par8 + x, y + 1.0, par10 + z, is2)); // :63-66
            par1ItemStack.shrink(1); // :67 - no creative check, as in the original
        }
        return InteractionResult.sidedSuccess(world.isClientSide); // :69
    }

    /** {@code onItemRightClick} (:72-75): swings the arm, nothing else. */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player player, final InteractionHand hand) {
        player.swing(hand); // :73
        return InteractionResultHolder.pass(player.getItemInHand(hand)); // :74 the stack unchanged
    }
}
