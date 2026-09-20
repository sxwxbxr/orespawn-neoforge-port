package com.swbr.orespawn.block.crop;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry lookups for the crop classes. The original compared block identity against the static
 * fields of {@code OreSpawnMain} ({@code bid == OreSpawnMain.MyTomatoPlant2}); the port compares
 * against the manifest ids (DECISIONS R2), so these classes depend on nothing but the id strings and
 * the registry holders of any wave can name their fields as they like.
 *
 * <p>Lookups by id resolve through {@link BuiltInRegistries} at call time - every caller runs after
 * registration (random ticks, item use), never during it.
 */
public final class CropBlocks {

    /** {@code OreSpawnMain.CrystalGrass} - soil of Quinoa (BlockQuinoa.java:24) and of every seed
     *  item, because {@code CrystalGrass.canSustainPlant} always returned true (CrystalGrass.java:58-60). */
    public static final ResourceKey<Block> CRYSTAL_GRASS = blockKey("crystalgrass");

    private CropBlocks() {
    }

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    public static ResourceKey<Block> blockKey(final String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    /** The registered block behind a manifest id; air until registration has run. */
    public static Block block(final ResourceKey<Block> key) {
        return BuiltInRegistries.BLOCK.get(key.location());
    }

    /** The registered item behind a manifest id; air until registration has run. */
    public static Item item(final String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    /**
     * {@code bid == Blocks.grass || bid == Blocks.dirt || bid == Blocks.farmland}, the soil test the
     * BlockReed crops and the tree seeds share.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): 1.7.10 {@code Blocks.dirt} was one block with dirt,
     * coarse dirt (1) and podzol (2); "dirt" is a category, so the whole {@code #minecraft:dirt} tag as
     * 1.21.1 ships it counts - rooted dirt, moss, mud, muddy mangrove roots and mycelium included. The
     * grass block is in that tag and was accepted by the same test, so it needs no term of its own.
     */
    public static boolean isGrassDirtOrFarmland(final BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.FARMLAND);
    }
}
