package com.swbr.orespawn.item.armor;

import com.swbr.orespawn.combat.LegacyArmorItem;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.ArmorStats;
import com.swbr.orespawn.config.stats.StatSource;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.ItemOreSpawnArmor}: the one class behind all 56 armor pieces
 * (14 sets x helmet/chest/leggings/boots, OreSpawnMain.java:1446-1501; manifest kind {@code armor}).
 *
 * <p>What the original did, and where it went:
 * <ul>
 *   <li>Constructor (:18-66): {@code ItemArmor(material, renderIndex, armorType)} on tabCombat,
 *       {@code armor_material} derived from the material constant, {@code armor_type} = ctor arg 3.
 *       Here: {@link ArmorSet} plus {@link ArmorItem.Type}; the tab is filed by the registry
 *       holder; the render index ({@code proxy.setArmorPrefix}) has no 1.21.1 counterpart.
 *       {@code original_d = damageReduceAmount} (:65) was stored and never read - dropped.</li>
 *   <li>{@code onCreated} (:76-148) -> {@link #onCraftedBy}: the set's {@code e_*} enchantments.</li>
 *   <li>{@code onUpdate} (:150-252) -> {@link #inventoryTick}: re-applies them once the stack
 *       carries none of the eight (DECISIONS R7: creative and loot copies get them on their first
 *       tick this way).</li>
 *   <li>{@code getArmorTexture} (:254-339): the material's single {@code ArmorMaterial.Layer}
 *       ({@code OreSpawnArmorMaterials.create}) - no override needed.</li>
 *   <li>{@code onArmorTick} (:341-363) -> the armor-slot branch of {@link #inventoryTick}:
 *       gliding with Royal, Peacock and Queen boots.</li>
 *   <li>{@code registerIcons} (:365-368): {@code models/item/<id>.json} on the original icon.</li>
 * </ul>
 *
 * <p>Implements {@link LegacyArmorItem} so a wearer gets the 1.7.10 armor formula (R5); the item
 * tag {@code orespawn:legacy_armor} lists the same 56 ids for data-driven consumers.
 */
public class ItemOreSpawnArmor extends ArmorItem implements LegacyArmorItem {

    /** {@code armor_material} (:14), as the set it indexed. */
    private final ArmorSet armorSet;

    /**
     * {@code armor_type} (:15): 0 helmet, 1 chest, 2 leggings, 3 boots - the 1.7.10
     * {@code ItemArmor} numbering, kept because {@code ExperienceSword} switches on it.
     */
    private final int armorType;

    /**
     * @param set   the material the original passed as {@code par2EnumArmorMaterial}
     * @param type  the piece; its 1.21.1 durability factor (11/16/15/13) equals 1.7.10's
     *              {@code ItemArmor.maxDamageArray}, so {@code type.getDurability(durability)} is
     *              the original {@code setMaxDamage} value (design-items-gear.md, class {@code abb})
     * @param props from the registry holder; durability and stack size 1 are set here, as
     *              {@code ItemArmor}'s constructor did
     */
    public ItemOreSpawnArmor(ArmorSet set, ArmorItem.Type type, Item.Properties props) {
        super(OreSpawnArmorMaterials.holder(set), type,
                props.durability(type.getDurability(set.stats(StatSource.EARLY).durability())));
        this.armorSet = set;
        this.armorType = legacyArmorType(type);
    }

    /** 1.21.1 {@code Type} -> 1.7.10 {@code armorType} (helmet 0 ... boots 3). */
    private static int legacyArmorType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case LEGGINGS -> 2;
            case BOOTS -> 3;
            // The original had no animal armor; BODY cannot be constructed through the registry.
            default -> throw new IllegalArgumentException("OreSpawn armor has no piece of type " + type);
        };
    }

    /** {@code get_armor_material()} (:68-70): 0 Ultimate ... 13 Queen. Read by {@code ExperienceSword}. */
    public int get_armor_material() {
        return armorSet.index();
    }

    /** {@code get_armor_type()} (:72-74): 0 helmet, 1 chest, 2 leggings, 3 boots. Read by {@code ExperienceSword}. */
    public int get_armor_type() {
        return armorType;
    }

    /** The set this piece belongs to. */
    public ArmorSet armorSet() {
        return armorSet;
    }

    /**
     * {@code onCreated} (:76-148): the set's fixed enchantments, applied when the piece leaves a
     * crafting result slot. Runs on both sides, as the original did.
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        ArmorStats a = armorSet.stats(StatSource.RUNTIME);
        if (a != null) {
            applyEnchantments(stack, level, a);
        }
    }

    /**
     * {@code onUpdate} (:150-252) and {@code onArmorTick} (:341-363). 1.21.1 has no separate armor
     * tick: {@code Inventory.tick} calls {@code inventoryTick} for every compartment including
     * armor, on both sides ({@code Player.aiStep}), which is exactly where Forge 1.7.10 called
     * {@code onArmorTick} from ({@code InventoryPlayer.decrementAnimations}). The armor-slot branch
     * therefore runs when this stack is the one in the piece's slot.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        onUpdate(stack, level);
        if (entity instanceof Player player && player.getItemBySlot(type.getSlot()) == stack) {
            onArmorTick(player);
        }
    }

    /**
     * {@code onUpdate} (:150-252): if the set has any {@code e_*} level and the stack carries none
     * of the eight enchantments, add the whole set again. The consequence is the original's: the
     * enchantments cannot be removed (a grindstone gives them back next tick).
     */
    private void onUpdate(ItemStack stack, Level level) {
        ArmorStats a = armorSet.stats(StatSource.RUNTIME);
        int lvl = 0;
        int enchanted = 0;
        if (a != null) {
            enchanted = a.e_aquaaffinity() + a.e_blastprotection() + a.e_featherfalling() + a.e_fireprotection();
            enchanted += a.e_projectileprotection() + a.e_protection() + a.e_respiration() + a.e_unbreaking();
            if (enchanted > 0) {
                HolderLookup.RegistryLookup<Enchantment> enchantments = enchantmentRegistry(level);
                lvl = level(enchantments, Enchantments.PROTECTION, stack);
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.FIRE_PROTECTION, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.BLAST_PROTECTION, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.PROJECTILE_PROTECTION, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.RESPIRATION, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.AQUA_AFFINITY, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.UNBREAKING, stack);
                }
                if (lvl <= 0) {
                    lvl = level(enchantments, Enchantments.FEATHER_FALLING, stack);
                }
                if (lvl == 0) {
                    applyEnchantments(stack, enchantments, a);
                }
            }
        }
    }

    /**
     * {@code onArmorTick} (:341-363), for every worn piece of the three gliding sets. The boots
     * themselves are such a piece, so boots alone are enough. The original compared the boots'
     * item against {@code OreSpawnMain.RoyalBoots} etc.; there is exactly one item per
     * (set, boots), so set and type identify it here without a holder reference.
     */
    private void onArmorTick(Player player) {
        ItemStack boots;
        if ((armorSet == ArmorSet.ROYAL || armorSet == ArmorSet.PEACOCK) && player != null) {
            boots = player.getItemBySlot(EquipmentSlot.FEET); // getEquipmentInSlot(1) = armorInventory[0] = boots
            if (!boots.isEmpty() && ((isPiece(boots, ArmorSet.ROYAL, 3) && royalGlideEnable() != 0)
                    || isPiece(boots, ArmorSet.PEACOCK, 3))) {
                Vec3 motion = player.getDeltaMovement();
                if (motion.y < -0.10000000149011612) {
                    player.setDeltaMovement(motion.x, -0.10000000149011612, motion.z);
                }
                player.fallDistance = 0.0f;
            }
        }
        if (armorSet == ArmorSet.QUEEN && player != null) {
            boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty() && isPiece(boots, ArmorSet.QUEEN, 3) && royalGlideEnable() != 0) {
                Vec3 motion = player.getDeltaMovement();
                if (motion.y < -0.25) {
                    player.setDeltaMovement(motion.x, -0.25, motion.z);
                }
                player.fallDistance = 0.0f;
            }
        }
    }

    /** {@code stack.getItem() == OreSpawnMain.<Set><Piece>}: the one item of that set and 1.7.10 armor type. */
    private static boolean isPiece(ItemStack stack, ArmorSet set, int legacyType) {
        return stack.getItem() instanceof ItemOreSpawnArmor armor
                && armor.armorSet == set && armor.armorType == legacyType;
    }

    /** {@code OreSpawnMain.RoyalGlideEnable} (OreSpawnMain.java:56, :1154), category OreSpawnTWEAKS, default 1, unclamped. */
    private static int royalGlideEnable() {
        return OreSpawnConfig.TWEAKS.RoyalGlideEnable.get();
    }

    /**
     * The enchantment block shared by {@code onCreated} (:121-146) and {@code onUpdate}
     * (:223-248), in the original order: protection, fire, blast, projectile, unbreaking on every
     * piece; feather falling only on boots (type 3); respiration and aqua affinity only on helmets
     * (type 0). A level of 0 adds nothing. Levels above the vanilla maximum (Protection 10, Feather
     * Falling 10, Aqua Affinity 3) are stored as-is: {@code ItemEnchantments.Mutable.upgrade} caps
     * at 255, not at the enchantment's max level (ItemEnchantments.java:162-166).
     */
    private void applyEnchantments(ItemStack stack, Level level, ArmorStats a) {
        applyEnchantments(stack, enchantmentRegistry(level), a);
    }

    private void applyEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> enchantments, ArmorStats a) {
        if (a.e_protection() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.PROTECTION), a.e_protection());
        }
        if (a.e_fireprotection() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.FIRE_PROTECTION), a.e_fireprotection());
        }
        if (a.e_blastprotection() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.BLAST_PROTECTION), a.e_blastprotection());
        }
        if (a.e_projectileprotection() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION), a.e_projectileprotection());
        }
        if (a.e_unbreaking() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), a.e_unbreaking());
        }
        if (armorType == 3 && a.e_featherfalling() != 0) {
            stack.enchant(enchantments.getOrThrow(Enchantments.FEATHER_FALLING), a.e_featherfalling());
        }
        if (armorType == 0) {
            if (a.e_respiration() != 0) {
                stack.enchant(enchantments.getOrThrow(Enchantments.RESPIRATION), a.e_respiration());
            }
            if (a.e_aquaaffinity() != 0) {
                stack.enchant(enchantments.getOrThrow(Enchantments.AQUA_AFFINITY), a.e_aquaaffinity());
            }
        }
    }

    /**
     * Enchantments are a data-driven registry in 1.21.1 (R7); the holders come from the level's
     * registry access, which exists on both sides.
     */
    private static HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }

    /** {@code EnchantmentHelper.getEnchantmentLevel(effectId, stack)} (:200-220). */
    private static int level(HolderLookup.RegistryLookup<Enchantment> enchantments, ResourceKey<Enchantment> key, ItemStack stack) {
        Holder<Enchantment> holder = enchantments.getOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }
}
