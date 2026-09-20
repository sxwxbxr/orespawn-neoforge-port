package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.armor.ItemOreSpawnArmor;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ExperienceSword} (ExperienceSword.java:13-148): an
 * {@code ItemSword} on {@code toolEMERALD}, stack 1, {@code setMaxDamage(1400)} (:27; the material
 * says 1300), tab Combat (:28); registered as {@code experiencesword} (OreSpawnMain.java:1325).
 * Attack {@code 4 + 6} in 1.7.10 (R6).
 *
 * <ul>
 *   <li>{@code onCreated}: Sharpness II, Unbreaking III (:31-34); {@code onUsingTick} re-adds both
 *       when Sharpness is missing (:36-42), called every tick of a block ({@link #onUseTick}) and
 *       from {@code onUpdate} every tick (:48) - R7.</li>
 *   <li>{@code onUpdate} (:44-102): experience trickle from worn Experience armor, see
 *       {@link #inventoryTick}.</li>
 *   <li>{@code hitEntity} (:112-138, live): +10 XP for hitting a mob, a second hit of
 *       {@code experienceLevel / 2}, portal particles, see {@link #hurtEnemy}.</li>
 *   <li>Right-click blocks for 3000 ticks ({@code getMaxItemUseDuration} :140-142, live -
 *       {@link BlockingSword}).</li>
 *   <li>Dead: {@code weaponDamage = 15} / {@code getDamageVsEntity} (:25, :104-106),
 *       {@code getMaterialName} (:108-110).</li>
 * </ul>
 *
 * <p>PORT: the singleton fields {@code worldObj}/{@code worldObjr} (:17-18, :49-54) cached the
 * server and the client world. {@code hitEntity} ran on the server only ({@code attackEntityFrom}
 * returned false on the client) and put its particles into the cached <em>client</em> world, so
 * they showed in singleplayer and for a LAN host and never on a dedicated server (R18 case 4:
 * the two sides saw different things). 1.21.1 calls {@code hurtEnemy} only from a
 * {@code ServerLevel} ({@code Player.attack}), and there is no client world to reach from it;
 * the port sends the particles to every client in range with {@code ServerLevel.sendParticles}
 * (catalogue itemblock-01), so a dedicated server shows them too.
 */
public class ExperienceSword extends BlockingSword {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.SHARPNESS, 2),
            PreEnchant.entry(Enchantments.UNBREAKING, 3));

    /** {@code getEquipmentInSlot(1..4)}: boots, leggings, chestplate, helmet - in that order. */
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    /** {@code get_armor_material() == 4}: {@code armorEXPERIENCE} (ItemOreSpawnArmor.java:34-36). */
    private static final int EXPERIENCE_ARMOR_MATERIAL = 4;

    public ExperienceSword(Material material, Item.Properties props) {
        super(material.withUses(1400), props.attributes(OreSpawnTiers.sword(material)), 3000);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUsingTick} (:36-42): every tick of a block. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        PreEnchant.restore(stack, level, Enchantments.SHARPNESS, ENCHANTMENTS);
    }

    /** {@code onUpdate} (:44-102). */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.SHARPNESS, ENCHANTMENTS);
        if (level.random.nextInt(60) == 1 && entity != null && entity instanceof LivingEntity e) {
            Player p = e instanceof Player player ? player : null;
            if (p == null) {
                // PORT: the original dereferenced p without a check (:61); only player inventories
                // tick items, so this never happened - guarded instead of crashing (R18 case 1).
                return;
            }
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack is = p.getItemBySlot(slot);
                if (!is.isEmpty()) {
                    Item it = is.getItem();
                    // PORT: w03-armor contract - ItemOreSpawnArmor keeps the original accessors
                    // get_armor_material() (4 = Experience) and get_armor_type() (0 helmet .. 3 boots).
                    if (it instanceof ItemOreSpawnArmor ia && ia.get_armor_material() == EXPERIENCE_ARMOR_MATERIAL) {
                        switch (ia.get_armor_type()) {
                            case 0 -> trickle(level, p, 10, 1.5);
                            case 1 -> trickle(level, p, 20, 1.25);
                            case 2 -> trickle(level, p, 30, 0.75);
                            case 3 -> trickle(level, p, 40, 0.25);
                            default -> {
                            }
                        }
                    }
                }
            }
        }
    }

    /** One {@code case} of :67-96: server-side {@code 1/chance} for +1 XP, a portal particle at {@code y + dy} on the client. */
    private static void trickle(Level level, Player p, int chance, double dy) {
        if (!level.isClientSide && level.random.nextInt(chance) == 1) {
            p.giveExperiencePoints(1);
        }
        level.addParticle(ParticleTypes.PORTAL, p.getX(), p.getY() + dy, p.getZ(),
                level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextGaussian());
    }

    /** {@code hitEntity} (:112-138). Server only, as in 1.7.10 (class Javadoc). */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float i = 0.0f;
        Player p = null;
        if (attacker instanceof Player player) {
            p = player;
        }
        // EntityLiving = every mob, not players (:119-121).
        if (target != null && target instanceof Mob) {
            i = 10.0f;
        }
        if (i > 0.0f && p != null) {
            p.giveExperiencePoints((int) i);
        }
        if (p != null) {
            i = (float) (p.experienceLevel / 2);
            if (i > 0.0f && target != null) {
                // Second hit in the same tick; whether it lands past the invulnerability window is
                // the original's behaviour too (catalogue README 5.x, "Treffer im selben Tick").
                target.hurt(p.damageSources().playerAttack(p), i);
            }
        }
        if (target != null && target.level() instanceof ServerLevel level) {
            // :131-135, "for (j = 0; j <= i / 2; ++j) spawnParticle(portal, x, y + 1, z, gauss, gauss, gauss)":
            // floor(i / 2) + 1 particles at the exact position with one Gaussian velocity per axis.
            // A count > 0 makes the client roll gaussian * offset for the position (offset 0 here)
            // and gaussian * speed for each velocity axis (ClientPacketListener.handleParticleEvent),
            // which is the same distribution the original drew from the client world's random.
            level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0, target.getZ(),
                    (int) (i / 2.0f) + 1, 0.0, 0.0, 0.0, 1.0);
        }
        return true;
    }
}
