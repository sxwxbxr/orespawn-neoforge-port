package com.swbr.orespawn.block.workshop;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.menu.ContainerCrystalWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.CrystalWorkbench} (CrystalWorkbench.java:11-51): the Crystal
 * Workbench, {@code crystalworkbench} (OreSpawnMain.java:1532, legacy id +211). A
 * {@code BlockWorkbench} ({@code Material.wood}) in tab Decorations with hardness 1 and
 * resistance 5 (:18-22) that opens GUI 1 on the server (:24-30) - a plain 3×3 crafting grid over
 * the vanilla recipes ({@link ContainerCrystalWorkbench}).
 *
 * <p>Not opaque, not a normal cube (:32-38): {@link Legacy#notANormalCube} plus the overrides
 * below. Textures (:41-50): top {@code _top}, bottom and the south/east faces {@code _side},
 * north and west {@code _bottom} - no orientation, the model JSON carries the odd mapping.
 * The base {@code CraftingTableBlock} is not extended because its menu provider is fixed to the
 * vanilla {@code CraftingMenu}/{@code MenuType.CRAFTING}.
 */
public class CrystalWorkbench extends Block {

    public static final MapCodec<CrystalWorkbench> CODEC = simpleCodec(CrystalWorkbench::new);
    /** {@code container.crafting}, what {@code CrystalWorkbenchGUI} drew as the title (CrystalWorkbenchGUI.java:22). */
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public CrystalWorkbench(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * {@code Material.wood}, hardness 1, resistance 5 (:18-22; OreSpawnMain.java:1532
     * {@code (…, 1.0f, 5.0f)}). No tool requirement (wood). Step sound: {@code BlockWorkbench}'s
     * constructor set none, only vanilla's own {@code crafting_table} registration line added
     * {@code soundTypeWood} - so this one keeps {@code Block}'s default, stone (the same finding
     * as for the torches and saplings in W02).
     */
    public static BlockBehaviour.Properties originalProperties() {
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, Legacy.resistance(5.0f))
                .sound(SoundType.STONE));
    }

    @Override
    protected MapCodec<CrystalWorkbench> codec() {
        return CODEC;
    }

    // --- isOpaqueCube / renderAsNormalBlock false (:32-38), see block.tree.CrystalCube for the table ---

    /** {@code isNormalCube() == false}: nothing attaches to its faces. */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    /** {@code lightOpacity} was 0 because {@code isOpaqueCube()} was false at construction. */
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /**
     * {@code onBlockActivated} (:24-30): the server opens GUI 1, both sides report "handled".
     * No {@code INTERACT_WITH_CRAFTING_TABLE} stat - the original awarded none.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(state.getMenuProvider(level, pos), pos);
        return InteractionResult.CONSUME;
    }

    /** {@code OreSpawnGUIHandler.getServerGuiElement} case 1 (OreSpawnGUIHandler.java:19-21). */
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new ContainerCrystalWorkbench(containerId, inventory,
                        ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE);
    }
}
