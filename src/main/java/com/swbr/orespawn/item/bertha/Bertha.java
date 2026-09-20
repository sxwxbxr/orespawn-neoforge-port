package com.swbr.orespawn.item.bertha;

import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.arrow.BerthaHit;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.BlockingSword;
import com.swbr.orespawn.item.tool.OreSpawnTiers;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Bertha} (Bertha.java:13-111): one {@code ItemSword} class for four
 * great swords, told apart by identity against {@code OreSpawnMain.MyRoyal} and {@code MyHammy} -
 * here by {@link Variant}. All four: stack 1, {@code setMaxDamage(9000)} (:18, overriding the
 * material's 9000 / 10000 / 2000), tab Combat (:19).
 *
 * <table>
 *   <tr><th>id</th><th>material</th><th>attack (R6)</th><th>constructor</th></tr>
 *   <tr><td>{@code berthasmall}</td><td>BERTHA</td><td>4 + 496 = 500</td><td>OreSpawnMain.java:1313</td></tr>
 *   <tr><td>{@code slicesmall}</td><td>BERTHA</td><td>500</td><td>:1314</td></tr>
 *   <tr><td>{@code royalsmall}</td><td>ROYAL</td><td>750</td><td>:1315</td></tr>
 *   <tr><td>{@code hammysmall}</td><td>HAMMY</td><td>86</td><td>:1316</td></tr>
 * </table>
 * The unused class {@code Slice} (Slice.java) is not ported: {@code slicesmall} is a {@code Bertha}
 * (docs/port/W01.md, "Nur abgehakt").
 *
 * <p><b>Enchantments</b> ({@code onCreated} :22-31; re-added by {@code onUsingTick} :33-48, which
 * {@code onUpdate} calls every tick :50-52): Royal Unbreaking 5, Hammy none, Bertha and Slice
 * Knockback 5, Bane of Arthropods 1, Fire Aspect 1 - whenever Knockback is 0 <em>and</em> Unbreaking
 * is at most 0.
 *
 * <p><b>Swing</b> ({@code onEntitySwing} :69-92): a {@link BerthaHit} two blocks ahead of a
 * swinging player, motion doubled, type 2 for Royal and 3 for Hammy, one durability. No mixin and
 * no payload is needed (catalogue 6.3, checked in the 1.21.1 sources): a swing - including a swing
 * into empty air - is a {@code ServerboundSwingPacket} sent by {@code LocalPlayer.swing}
 * (LocalPlayer.java:342); the server handles it in {@code ServerGamePacketListenerImpl.handleAnimate}
 * (:1467-1471) with {@code player.swing(hand)}, and {@code LivingEntity.swing(hand, updateSelf)}
 * calls {@code stack.onEntitySwing(this, hand)} first (LivingEntity.java:1861-1863) - the NeoForge
 * successor of the Forge 1.7.10 hook in {@code EntityLivingBase.swingItem}, reached the same way.
 *
 * <p><b>Dead in the jar</b>: {@code hitEntity(ItemStack, EntityLiving, EntityLiving)} (:98-101,
 * wrong signature - vanilla {@code ItemSword.hitEntity} applied, one durability per hit, which
 * {@code SwordItem} does too) and {@code getMaterialName} (:94-96).
 *
 * <p>Right-click blocks for 9000 ticks ({@code getMaxItemUseDuration} :103-105, DECISIONS R19 -
 * {@link BlockingSword}).
 */
public class Bertha extends BlockingSword {

    /** Which {@code OreSpawnMain} field the original compared {@code this} against. */
    public enum Variant {
        /** {@code MyBertha}. */
        BERTHA,
        /** {@code MySlice}. */
        SLICE,
        /** {@code MyRoyal}. */
        ROYAL,
        /** {@code MyHammy}. */
        HAMMY
    }

    private final Variant variant;

    public Bertha(Variant variant, Material material, Item.Properties props) {
        super(material.withUses(9000), props.attributes(OreSpawnTiers.sword(material)), 9000);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    /** The {@code addEnchantment} block shared by :23-30 and :39-46. */
    private List<PreEnchant.Entry> enchantments() {
        if (variant == Variant.ROYAL) {
            return List.of(PreEnchant.entry(Enchantments.UNBREAKING, 5));
        } else if (variant != Variant.HAMMY) {
            return List.of(
                    PreEnchant.entry(Enchantments.KNOCKBACK, 5),
                    PreEnchant.entry(Enchantments.BANE_OF_ARTHROPODS, 1),
                    PreEnchant.entry(Enchantments.FIRE_ASPECT, 1));
        }
        return List.of();
    }

    /** {@code onCreated} (:22-31). */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, enchantments());
    }

    /**
     * {@code onUsingTick} (:33-48): the sentinel is Knockback, falling back to Unbreaking.
     *
     * <p>PORT: server only, as {@code PreEnchant.restore} - the client copy is replaced by the next
     * inventory sync anyway.
     */
    private void onUsingTick(ItemStack stack, Level level) {
        if (level.isClientSide) {
            return;
        }
        int lvl = PreEnchant.level(stack, level, Enchantments.KNOCKBACK);
        if (lvl == 0) {
            lvl = PreEnchant.level(stack, level, Enchantments.UNBREAKING);
        }
        if (lvl <= 0) {
            PreEnchant.addAll(stack, level, enchantments());
        }
    }

    /** {@code onUsingTick} while blocking. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        onUsingTick(stack, level);
    }

    /** {@code onUpdate} (:50-52): {@code onUsingTick(stack, null, 0)} every inventory tick. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        onUsingTick(stack, level);
    }

    /**
     * {@code onLeftClickEntity} (:54-67): with {@code BigBerthaPvp == 0}, attacks on players,
     * Girlfriend, Boyfriend and tamed animals are cancelled.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity != null && OreSpawnConfig.TWEAKS.BigBerthaPvp.get() == 0) {
            if (entity instanceof Player || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof TamableAnimal t) {
                if (t.isTame()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@code onEntitySwing} (:69-92), server side, players only.
     *
     * <p>PORT: 1.7.10 had one held item; the durability is taken from the hand that swung.
     */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving, InteractionHand hand) {
        if (entityLiving instanceof Player p && !entityLiving.level().isClientSide) {
            final double xzoff = 2.0;
            final double yoff = 1.55;
            final BerthaHit lb = new BerthaHit(p.level(), p);
            lb.moveTo(p.getX() - xzoff * Math.sin(Math.toRadians(p.getYHeadRot())), p.getY() + yoff,
                    p.getZ() + xzoff * Math.cos(Math.toRadians(p.getYHeadRot())), p.getYHeadRot(), p.getXRot());
            Vec3 motion = lb.getDeltaMovement();
            lb.setDeltaMovement(motion.x * 2.0, motion.y * 2.0, motion.z * 2.0);
            if (variant == Variant.ROYAL) {
                lb.setHitType(2);
            }
            if (variant == Variant.HAMMY) {
                lb.setHitType(3);
            }
            p.level().addFreshEntity(lb);
            stack.hurtAndBreak(1, p, LivingEntity.getSlotForHand(hand));
        }
        return false;
    }
}
