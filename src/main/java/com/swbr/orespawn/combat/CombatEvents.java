package com.swbr.orespawn.combat;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.PortConfig;
import com.swbr.orespawn.item.tool.BlockingSword;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The combat hooks of the port (docs/DECISIONS.md, R4 and R5, plus the 1.7.10 sword block), on
 * the game bus.
 *
 * <p>All hang on {@link LivingIncomingDamageEvent}, which NeoForge fires inside
 * {@code LivingEntity.hurt} right after the invulnerability checks
 * ({@code LivingEntity.java:1152-1153}). That is the one point in the sequence that is early enough
 * for a reduction modifier to take effect ({@code DamageContainer.addModifier}) and late enough
 * that every original {@code hurt} override - the per-hit caps, the {@code hurt_timer} checks - has
 * already run, because those call {@code super.hurt} with their capped amount.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class CombatEvents {

    private CombatEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        applySwordBlock(event, entity);
        applyVirtualHealth(event, entity);
        applyLegacyArmor(event, entity);
    }

    /**
     * The 1.7.10 sword block, {@code EntityPlayer.damageEntity}: {@code if (!source.isUnblockable()
     * && isBlocking() && damage > 0) damage = (1 + damage) * 0.5}, before armor and potions. Only
     * players blocked ({@code damageEntity} was overridden in {@code EntityPlayer}), and only the
     * five OreSpawn swords still block ({@link BlockingSword}); {@code isUnblockable()} was set by
     * {@code setDamageBypassesArmor()}, hence {@code #minecraft:bypasses_armor} as in
     * {@link #applyLegacyArmor}.
     *
     * <p>PORT: {@code damageEntity} ran after the invulnerability window had subtracted
     * {@code lastDamage}; this event fires before it, so a stronger hit inside the window is
     * halved as a whole ({@code (1 + a) / 2 - (1 + l) / 2}) instead of the difference
     * ({@code (1 + (a - l)) / 2}) - half a point less per such hit. {@code lastHurt} is not
     * reachable from here and the alternative, {@code LivingDamageEvent.Pre}, fires after the armor
     * reduction, which would move the halving behind armor.
     */
    private static void applySwordBlock(LivingIncomingDamageEvent event, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        if (BlockingSword.isBlockingWith(entity) && event.getAmount() > 0.0F) {
            event.setAmount((1.0F + event.getAmount()) * 0.5F);
        }
    }

    /**
     * R4: the amount that reached {@code super.hurt} is in original units; the entity stores
     * health at {@code 1024 / original} of that. Scaling here, before shield, armor and effects,
     * is exact because every later reduction is a multiple of the current amount, and absorption
     * is stored in the same scale as health.
     */
    private static void applyVirtualHealth(LivingIncomingDamageEvent event, LivingEntity entity) {
        if (entity instanceof VirtualHealth virtual && LegacyCombatMath.needsScaling(virtual.getOriginalMaxHealth())) {
            event.setAmount(LegacyCombatMath.toAttributeScale(event.getAmount(), virtual.getOriginalMaxHealth()));
        }
    }

    /**
     * R5: replace vanilla's {@code ARMOR} reduction with {@code damage * (25 - armor) / 25}.
     *
     * <p>{@code LivingEntity.actuallyHurt} ({@code :1787}) always records the {@code ARMOR}
     * reduction, so the modifier always runs; it sees the container's current damage - after
     * shield, freeze and helmet adjustments, before magic and absorption - which is what the
     * 1.7.10 {@code applyArmorCalculations} saw. Vanilla still calls {@code hurtArmor} on the way,
     * so armor durability is spent like in 1.7.10's {@code damageArmor}.
     *
     * <p>{@code #minecraft:bypasses_armor} is the 1.21.1 form of {@code isUnblockable()}: those
     * sources keep vanilla's reduction, which is zero for them.
     */
    private static void applyLegacyArmor(LivingIncomingDamageEvent event, LivingEntity entity) {
        if (!PortConfig.legacyArmorFormula()) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        if (!LegacyArmorFormula.applies(entity)) {
            return;
        }
        event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, vanillaReduction) -> {
            // The armor value is read at reduction time, not at event time: The King's override
            // depends on the health he has when the hit lands, like the original.
            int armor = LegacyArmorFormula.armorValue(entity);
            return LegacyCombatMath.armorReduction(container.getNewDamage(), armor);
        });
    }

    /**
     * R4, the other direction: {@code heal(x)} in a ported entity is written in original units
     * (The Queen's regeneration, Regeneration potions). Without scaling, one point of healing on a
     * 7000-health boss would be worth seven original points and the fight would take longer than
     * it did.
     */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        // PORT: R4 names only incoming damage. Healing is scaled by the same factor so that health,
        // damage and healing stay in one unit; otherwise the ratio the boss bar shows would drift
        // from the original fight.
        LivingEntity entity = event.getEntity();
        if (entity instanceof VirtualHealth virtual && LegacyCombatMath.needsScaling(virtual.getOriginalMaxHealth())) {
            event.setAmount(LegacyCombatMath.toAttributeScale(event.getAmount(), virtual.getOriginalMaxHealth()));
        }
    }
}
