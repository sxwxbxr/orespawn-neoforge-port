package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code BlockRuby} (BlockRuby.java:12-46), three storage blocks:
 * <ul>
 *   <li>{@code blockmobzillascale} Mobzilla Scale Block (OreSpawnMain.java:1280) - gives Strength</li>
 *   <li>{@code blockruby} Ruby Block (:1282)</li>
 *   <li>{@code blockamethyst} Amethyst Block (:1283)</li>
 * </ul>
 * The original picked the Strength branch with {@code this == OreSpawnMain.MyBlockMobzillaScaleBlock}
 * (:31, :37); here that is the {@code mobzillaScale} flag.
 *
 * <p>{@code isOpaqueCube} false but {@code renderAsNormalBlock} true (:22-28): a full cube that neither
 * culls its neighbours' faces nor blocks light ({@link NonOpaqueBlock}, {@code noOcclusion()}), but is
 * otherwise a normal block - mobs spawn on it, redstone runs through it. Drops itself.
 */
public class BlockRuby extends NonOpaqueBlock {

    private final boolean mobzillaScale;

    public BlockRuby(BlockBehaviour.Properties properties, boolean mobzillaScale) {
        super(properties);
        this.mobzillaScale = mobzillaScale;
    }

    /** {@code Material.rock}, hardness 4, resistance 4, light 0.4 (:15-19). */
    public static BlockBehaviour.Properties originalProperties() {
        int light = Legacy.light(0.4f);
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(4.0f, Legacy.resistance(4.0f))
                .lightLevel(state -> light)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    /** {@code onEntityCollidedWithBlock} (:30-34): Strength I for 200 ticks, both sides like the original. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.mobzillaScale && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        }
    }

    /**
     * {@code onEntityWalking} (:36-40); step-vs-tick note in {@code OreTitanium.stepOn}. Only behind the
     * 1.7.10 {@code moveEntity} gate - {@code canTriggerWalking}, not sneaking, not riding - which
     * {@link Legacy#wasWalking} reproduces (R20: the hoverboard gets no Strength here).
     */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (this.mobzillaScale && entity instanceof LivingEntity living && Legacy.wasWalking(entity)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        }
    }
}
