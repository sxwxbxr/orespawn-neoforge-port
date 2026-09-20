package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.config.stats.WeaponStats;
import java.util.function.Function;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * The fifteen {@code Item.ToolMaterial}s of {@code OreSpawnMain.preInit}
 * (OreSpawnMain.java:1292-1306, manifest {@code tool_materials}) as 1.21.1 {@link Tier}s, plus the
 * attack-damage conversion of DECISIONS R6.
 *
 * <p>Every number comes from {@link WeaponStats}, read through {@link StatSource#EARLY} because the
 * materials are built while items register, before NeoForge loads the COMMON config (DECISIONS R3).
 * The clamps of {@code get_weaponstats} (harvest level below default-1 resets to the default, the
 * other four to [default/2, default*2]) live in {@link WeaponStats#read}.
 *
 * <p>1.7.10 {@code ToolMaterial} field to 1.21.1 {@link Tier} method:
 * <ul>
 *   <li>{@code maxUses} - {@link Tier#getUses()} (a class that called {@code setMaxDamage} keeps its
 *       own number through {@link Material#withUses(int)}, catalogue itemblock-01 section 0.2)</li>
 *   <li>{@code efficiencyOnProperMaterial} - {@link Tier#getSpeed()}</li>
 *   <li>{@code damageVsEntity} - {@link Tier#getAttackDamageBonus()} (R6)</li>
 *   <li>{@code harvestLevel} - {@link #incorrectFor(int)}: 0 wood, 1 stone, 2 iron, 3 diamond,
 *       4 and above netherite (R6). Ruby and Hammy (5) and Ultimate (10) therefore mine everything,
 *       as the Forge harvest levels above diamond did in 1.7.10.</li>
 *   <li>{@code enchantability} - {@link Tier#getEnchantmentValue()}</li>
 *   <li>{@code customCraftingMaterial} - never set by the original, so no repair ingredient</li>
 * </ul>
 */
public final class OreSpawnTiers {

    /**
     * Sword attack modifier (R6): the tooltip value of 1.7.10 ({@code 4 + material.damage},
     * ItemSword {@code ldc 4.0f}) becomes the 1.21.1 total {@code 1 + 3 + bonus}. The Ultimate Sword
     * lands on the documented 40.
     */
    public static final float SWORD_DAMAGE = 3.0f;
    /** {@code ItemAxe}: {@code ldc 3.0f} to {@code ItemTool.<init>} (catalogue itemblock-01 section 0.1). */
    public static final float AXE_DAMAGE = 3.0f;
    /** {@code ItemPickaxe}: {@code fconst_2}. */
    public static final float PICKAXE_DAMAGE = 2.0f;
    /** {@code ItemSpade} and {@code CrystalShovel}: {@code fconst_1} / {@code 1.0f}. */
    public static final float SHOVEL_DAMAGE = 1.0f;

    // Attack speed did not exist in 1.7.10 (no cooldown). R6: vanilla values, taken from the
    // diamond and netherite tools because every OreSpawn material is at or above that level.
    private static final float SWORD_SPEED = -2.4f;
    private static final float AXE_SPEED = -3.0f;
    private static final float PICKAXE_SPEED = -2.8f;
    private static final float SHOVEL_SPEED = -3.0f;
    private static final float HOE_SPEED = 0.0f;

    private OreSpawnTiers() {}

    /** One entry per {@code EnumHelper.addToolMaterial} call, in source order (OreSpawnMain.java:1292-1306). */
    public enum Material implements Tier {
        /** {@code toolULTIMATE} - "ULTIMATE", {@code ultimate_stats} (:1292; default 10/3000/15/36/100). */
        ULTIMATE(WeaponStats::ultimate_stats),
        /** {@code toolNIGHTMARE} - "NIGHTMARE", {@code nightmare_stats} (:1293; 3/1800/12/26/60). */
        NIGHTMARE(WeaponStats::nightmare_stats),
        /** {@code toolEMERALD} - "REALEMERALD", {@code emerald_stats} (:1294; 3/1300/10/6/75). */
        REALEMERALD(WeaponStats::emerald_stats),
        /** {@code toolRUBY} - "RUBY", {@code ruby_stats} (:1295; 5/1500/11/16/85). */
        RUBY(WeaponStats::ruby_stats),
        /** {@code toolAMETHYST} - "AMETHYST", {@code amethyst_stats} (:1296; 4/2000/11/11/70). */
        AMETHYST(WeaponStats::amethyst_stats),
        /** {@code toolBERTHA} - "BERTHA", {@code bertha_stats} (:1297; 3/9000/15/496/100). W04 Bertha, Slice. */
        BERTHA(WeaponStats::bertha_stats),
        /** {@code toolCRYSTALWOOD} - "CRYSTALWOOD", {@code crystalwood_stats} (:1298; 2/300/3/2/15). */
        CRYSTALWOOD(WeaponStats::crystalwood_stats),
        /** {@code toolCRYSTALSTONE} - "CRYSTALSTONE", {@code crystalstone_stats} (:1299; 3/800/6/5/45). */
        CRYSTALSTONE(WeaponStats::crystalstone_stats),
        /** {@code toolCRYSTALPINK} - "CRYSTALPINK", {@code crystalpink_stats} (:1300; 4/1100/10/7/65). */
        CRYSTALPINK(WeaponStats::crystalpink_stats),
        /** {@code toolTIGERSEYE} - "TIGERSEYE", {@code tigerseye_stats} (:1301; 4/1600/12/8/75). */
        TIGERSEYE(WeaponStats::tigerseye_stats),
        /** {@code toolROYAL} - "ROYAL", {@code royal_stats} (:1302; 3/10000/15/746/150). W04 Royal Guardian Sword. */
        ROYAL(WeaponStats::royal_stats),
        /** {@code toolHAMMY} - "HAMMY", {@code hammy_stats} (:1303; 5/2000/15/82/100). W04 Attitude Adjuster. */
        HAMMY(WeaponStats::hammy_stats),
        /** {@code toolBATTLE} - "BATTLE", {@code battleaxe_stats} (:1304; 3/1500/15/46/75). */
        BATTLE(WeaponStats::battleaxe_stats),
        /** {@code toolCHAINSAW} - "CHAINSAW", {@code chainsaw_stats} (:1305; 3/1500/10/56/75). */
        CHAINSAW(WeaponStats::chainsaw_stats),
        /** {@code toolQUEENBATTLE} - "QUEENBATTLE", {@code queenbattleaxe_stats} (:1306; 3/2200/15/662/100). */
        QUEENBATTLE(WeaponStats::queenbattleaxe_stats);

        private final WeaponStats stats;
        private final TagKey<Block> incorrectBlocksForDrops;

        Material(Function<StatSource, WeaponStats> reader) {
            // Enum constants initialise on first use, which is the item registry event: the COMMON
            // spec is not loaded yet, the file on disk (or the defaults) is (R3).
            this.stats = reader.apply(StatSource.EARLY);
            this.incorrectBlocksForDrops = incorrectFor(stats.harvestlevel());
        }

        /** The clamped 1.7.10 stats this tier was built from. */
        public WeaponStats stats() {
            return stats;
        }

        /** The 1.7.10 harvest level, for callers that need the number rather than the tag. */
        public int harvestLevel() {
            return stats.harvestlevel();
        }

        @Override
        public int getUses() {
            return stats.maxuses();
        }

        @Override
        public float getSpeed() {
            return stats.efficiency();
        }

        @Override
        public float getAttackDamageBonus() {
            return stats.damage();
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return incorrectBlocksForDrops;
        }

        @Override
        public int getEnchantmentValue() {
            return stats.enchantability();
        }

        @Override
        public Ingredient getRepairIngredient() {
            // EnumHelper.addToolMaterial leaves customCraftingMaterial null: nothing repairs these.
            return Ingredient.EMPTY;
        }

        /**
         * This material with a fixed durability, for the classes that called
         * {@code setMaxDamage(n)} in their constructor. In 1.7.10 that call overrode the material's
         * {@code maxUses} - a config change to {@code *_maxuses} never reached those items
         * (catalogue itemblock-01 section 0.2, "Falle"). {@code TieredItem} takes its durability from
         * {@link Tier#getUses()}, so the override is expressed as a tier view; everything else is
         * delegated unchanged.
         */
        public Tier withUses(int uses) {
            return new FixedUses(this, uses);
        }
    }

    /** {@link Material#withUses(int)}. */
    public record FixedUses(Material base, int uses) implements Tier {
        @Override
        public int getUses() {
            return uses;
        }

        @Override
        public float getSpeed() {
            return base.getSpeed();
        }

        @Override
        public float getAttackDamageBonus() {
            return base.getAttackDamageBonus();
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return base.getIncorrectBlocksForDrops();
        }

        @Override
        public int getEnchantmentValue() {
            return base.getEnchantmentValue();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return base.getRepairIngredient();
        }
    }

    /** Harvest level to {@code incorrect_for_*} tag (DECISIONS R6). */
    public static TagKey<Block> incorrectFor(int harvestLevel) {
        return switch (harvestLevel) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> harvestLevel < 0 ? BlockTags.INCORRECT_FOR_WOODEN_TOOL : BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    /** {@code ItemSword}: {@code 4 + damage} in 1.7.10, modifier {@link #SWORD_DAMAGE} in 1.21.1 (R6). */
    public static ItemAttributeModifiers sword(Tier tier) {
        return SwordItem.createAttributes(tier, SWORD_DAMAGE, SWORD_SPEED);
    }

    /** {@code ItemAxe}: {@code 3 + damage}. */
    public static ItemAttributeModifiers axe(Tier tier) {
        return DiggerItem.createAttributes(tier, AXE_DAMAGE, AXE_SPEED);
    }

    /** {@code ItemPickaxe}: {@code 2 + damage}. */
    public static ItemAttributeModifiers pickaxe(Tier tier) {
        return DiggerItem.createAttributes(tier, PICKAXE_DAMAGE, PICKAXE_SPEED);
    }

    /** {@code ItemSpade} and {@code CrystalShovel}: {@code 1 + damage}. */
    public static ItemAttributeModifiers shovel(Tier tier) {
        return DiggerItem.createAttributes(tier, SHOVEL_DAMAGE, SHOVEL_SPEED);
    }

    /**
     * {@code ItemHoe} had no attack modifier at all (catalogue itemblock-01 section 0.1): the hoe hit
     * for the player's base 1. {@code DiggerItem.createAttributes} always adds the tier bonus, so the
     * modifier cancels it - the same trick vanilla uses for its own hoes.
     */
    public static ItemAttributeModifiers hoe(Tier tier) {
        return DiggerItem.createAttributes(tier, -tier.getAttackDamageBonus(), HOE_SPEED);
    }
}
