package com.swbr.orespawn.client;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.tree.BlockCrystalLeaves;
import com.swbr.orespawn.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-side look of the W02 tree, plant and torch blocks: render layers and tints.
 *
 * <p><b>Render layers.</b> Every block whose 1.7.10 class answered {@code isOpaqueCube() == false}
 * or rendered as a cross (crystal grass, crystal planks, crystal tree wood, all leaves, the
 * saplings, flowers and torches) draws in the cutout layer (DECISIONS R18). That is declared where
 * NeoForge 21.1 wants it, in the block model JSON ({@code "render_type": "minecraft:cutout"} and
 * {@code "minecraft:cutout_mipped"} for the cube leaves), not through the deprecated
 * {@code ItemBlockRenderTypes.setRenderLayer}. Nothing to register here for that.
 *
 * <p><b>Tints.</b> Two groups, from the 1.7.10 colour methods:
 * <ul>
 *   <li>Apple, Scary, Cherry, Peach and Experience leaves inherited {@code BlockLeaves}'
 *       {@code colorMultiplier}, the biome's average foliage colour, and {@code getRenderColor(0)},
 *       the default foliage colour in the inventory - the same lambda vanilla registers for oak.</li>
 *   <li>The three crystal leaves return {@code 14540253} from all three colour methods
 *       (BlockCrystalLeaves.java:103-116): {@link BlockCrystalLeaves#COLOR} for block and item.</li>
 *   <li>The three crystal saplings - and every other {@code BlockReed} subclass: the corn, tomato,
 *       lettuce and quinoa stages, the experience sapling, the island and the king, queen and dungeon
 *       spawners (BUGHUNT2 2.1) - inherited {@code BlockReed.colorMultiplier}, the biome grass colour
 *       at the block (client-1.7.10.jar, {@code ane.d(IBlockAccess,III)}), and the cross renderer
 *       multiplied by it - the same lambda vanilla registers for sugar cane. {@code BlockReed} overrides
 *       neither {@code getBlockColor} nor {@code getRenderColor}, so the inventory icon stayed white:
 *       no item colour, and the item models ({@code item/generated}) get none registered. The block models
 *       use {@code minecraft:block/tinted_cross}.</li>
 * </ul>
 * The textures under {@code block/leaves_*} are greyscale foliage like vanilla's (design/design-blocks.md).
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BlockRenderTypes {

    private BlockRenderTypes() {}

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                ModBlocks.LEAVES_APPLE.get(),
                ModBlocks.LEAVES_SCARY.get(),
                ModBlocks.LEAVES_CHERRY.get(),
                ModBlocks.LEAVES_PEACH.get(),
                ModBlocks.LEAVES_EXPERIENCE.get());
        event.register(
                (state, level, pos, tintIndex) -> BlockCrystalLeaves.COLOR,
                ModBlocks.CRYSTAL_TREE_LEAVES.get(),
                ModBlocks.CRYSTAL_TREE_LEAVES2.get(),
                ModBlocks.CRYSTAL_TREE_LEAVES3.get());
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor(),
                ModBlocks.CRYSTAL_SAPLING.get(),
                ModBlocks.CRYSTAL_SAPLING2.get(),
                ModBlocks.CRYSTAL_SAPLING3.get(),
                // BUGHUNT2 2.1 - every other BlockReed subclass inherited the same colour: BlockCorn,
                // BlockTomato, BlockLettuce, BlockQuinoa, BlockExperiencePlant, IslandBlock,
                // KingSpawnerBlock, QueenSpawnerBlock, DungeonSpawnerBlock (each extends BlockReed and
                // overrides none of colorMultiplier/getBlockColor/getRenderColor/getRenderType).
                ModBlocks.CORN_0.get(),
                ModBlocks.CORN_1.get(),
                ModBlocks.CORN_2.get(),
                ModBlocks.CORN_3.get(),
                ModBlocks.TOMATO_0.get(),
                ModBlocks.TOMATO_1.get(),
                ModBlocks.TOMATO_2.get(),
                ModBlocks.TOMATO_3.get(),
                ModBlocks.LETTUCE_0.get(),
                ModBlocks.LETTUCE_1.get(),
                ModBlocks.LETTUCE_2.get(),
                ModBlocks.LETTUCE_3.get(),
                ModBlocks.QUINOA_0.get(),
                ModBlocks.QUINOA_1.get(),
                ModBlocks.QUINOA_2.get(),
                ModBlocks.QUINOA_3.get(),
                ModBlocks.EXPERIENCE_SAPLING.get(),
                ModBlocks.ISLAND.get(),
                ModBlocks.KINGSPAWNER.get(),
                ModBlocks.QUEENSPAWNER.get(),
                ModBlocks.DUNGEONSPAWNER.get());
        // W05 - AntBlock.colorMultiplier (AntBlock.java:111-125): biome grass average; all faces carry tintindex 0.
        // crystaltermiteblock has no colour override in CrystalAntBlock and stays untinted.
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.get(0.5, 1.0),
                ModBlocks.ANTBLOCK.get(),
                ModBlocks.REDANTBLOCK.get(),
                ModBlocks.RAINBOWANTBLOCK.get(),
                ModBlocks.UNSTABLEANTBLOCK.get(),
                ModBlocks.TERMITEBLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                ModBlocks.LEAVES_APPLE,
                ModBlocks.LEAVES_SCARY,
                ModBlocks.LEAVES_CHERRY,
                ModBlocks.LEAVES_PEACH,
                ModBlocks.LEAVES_EXPERIENCE);
        event.register(
                (stack, tintIndex) -> BlockCrystalLeaves.COLOR,
                ModBlocks.CRYSTAL_TREE_LEAVES,
                ModBlocks.CRYSTAL_TREE_LEAVES2,
                ModBlocks.CRYSTAL_TREE_LEAVES3);
        // W05 - AntBlock.getRenderColor -> getBlockColor = ColorizerGrass.getGrassColor(0.5, 1.0) (:99-109).
        event.register(
                (stack, tintIndex) -> GrassColor.get(0.5, 1.0),
                ModBlocks.ANTBLOCK,
                ModBlocks.REDANTBLOCK,
                ModBlocks.RAINBOWANTBLOCK,
                ModBlocks.UNSTABLEANTBLOCK,
                ModBlocks.TERMITEBLOCK);
    }
}
