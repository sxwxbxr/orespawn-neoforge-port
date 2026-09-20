package com.swbr.orespawn.item.armor;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.stats.ArmorStats;
import com.swbr.orespawn.config.stats.StatSource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The fourteen {@code EnumHelper.addArmorMaterial} calls of {@code OreSpawnMain.load}
 * (OreSpawnMain.java:1432-1445) as entries of the 1.21.1 {@code armor_material} registry, one per
 * {@link ArmorSet}, registered as {@code orespawn:<prefix>}.
 *
 * <p>The original passed {@code (name, durability, {head, chest, leg, boot}, enchantability)} from
 * the {@code <Set>_armorstats} holders it had read in preInit. 1.21.1 registers materials before
 * the COMMON config is loaded, so the values come from {@link StatSource#EARLY} (DECISIONS R3);
 * a changed value needs a restart, as it did in 1.7.10.
 *
 * <p>What 1.7.10's {@code ArmorMaterial} had no notion of and is therefore fixed here: toughness
 * 0, knockback resistance 0 (both 1.9+ concepts), no repair ingredient
 * ({@code EnumHelper.addArmorMaterial} left {@code customCraftingMaterial} null, so
 * {@code ItemArmor.getIsRepairable} never matched), and the equip sound - see the
 * {@code // PORT:} note in {@link #create}.
 */
public final class OreSpawnArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, OreSpawn.MOD_ID);

    /** Declared before the holders below so their registration can fill it. */
    private static final Map<ArmorSet, DeferredHolder<ArmorMaterial, ArmorMaterial>> BY_SET =
            new EnumMap<>(ArmorSet.class);

    // Registration order = OreSpawnMain.java:1432-1445.
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ULTIMATE = register(ArmorSet.ULTIMATE); // :1432
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOBZILLA = register(ArmorSet.MOBZILLA); // :1433
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LAVAEEL = register(ArmorSet.LAVAEEL); // :1434
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOTHSCALE = register(ArmorSet.MOTHSCALE); // :1435
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EMERALD = register(ArmorSet.EMERALD); // :1436
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EXPERIENCE = register(ArmorSet.EXPERIENCE); // :1437
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> RUBY = register(ArmorSet.RUBY); // :1438
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> AMETHYST = register(ArmorSet.AMETHYST); // :1439
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PINK = register(ArmorSet.PINK); // :1440
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TIGERSEYE = register(ArmorSet.TIGERSEYE); // :1441
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PEACOCK = register(ArmorSet.PEACOCK); // :1442
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ROYAL = register(ArmorSet.ROYAL); // :1443
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LAPIS = register(ArmorSet.LAPIS); // :1444
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> QUEEN = register(ArmorSet.QUEEN); // :1445

    private OreSpawnArmorMaterials() {}

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(ArmorSet set) {
        DeferredHolder<ArmorMaterial, ArmorMaterial> holder =
                ARMOR_MATERIALS.register(set.prefix(), () -> create(set));
        BY_SET.put(set, holder);
        return holder;
    }

    /**
     * {@code EnumHelper.addArmorMaterial(name, durability, reductions, enchantability)} for one
     * set. The durability factor is not part of the 1.21.1 material; {@link ItemOreSpawnArmor}
     * applies it per piece through {@code ArmorItem.Type.getDurability}, whose factors 11/16/15/13
     * are the same {@code ItemArmor.maxDamageArray} 1.7.10 used (design-items-gear.md, bytecode of
     * class {@code abb}; ArmorItem.java:150-153).
     */
    private static ArmorMaterial create(ArmorSet set) {
        ArmorStats a = set.stats(StatSource.EARLY);
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, a.head_protection());
        defense.put(ArmorItem.Type.CHESTPLATE, a.chest_protection());
        defense.put(ArmorItem.Type.LEGGINGS, a.leg_protection());
        defense.put(ArmorItem.Type.BOOTS, a.boot_protection());
        // No BODY entry: the original had no animal armor; getDefense() falls back to 0.
        return new ArmorMaterial(
                defense,
                a.enchantability(),
                // PORT: 1.7.10 had no armor equip sounds at all (they came with 1.9); the record
                // needs a holder, and the generic sound is the one 1.21.1 plays for armor without
                // a material of its own.
                SoundEvents.ARMOR_EQUIP_GENERIC,
                // customCraftingMaterial was null in every EnumHelper call: not anvil-repairable.
                () -> Ingredient.of(),
                // getArmorTexture (ItemOreSpawnArmor.java:254-339): <prefix>_1.png for helmet,
                // chest and boots, <prefix>_2.png for leggings - exactly the outer/inner split of
                // one 1.21.1 Layer, which resolves to textures/models/armor/<prefix>_layer_{1,2}.png
                // (ArmorMaterial.java:47-50).
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, set.prefix()))),
                0.0F,
                0.0F);
    }

    /** The registered material of {@code set}; lazily resolved, safe to hand to an {@code ArmorItem} constructor. */
    public static Holder<ArmorMaterial> holder(ArmorSet set) {
        return BY_SET.get(set);
    }

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
