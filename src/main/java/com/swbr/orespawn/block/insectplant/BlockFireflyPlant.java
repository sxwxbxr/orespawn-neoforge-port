package com.swbr.orespawn.block.insectplant;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.crop.CropBlocks;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Port of {@code danger.orespawn.BlockFireflyPlant} (BlockFireflyPlant.java:13-86), id
 * {@code firefly_plant}, planted by {@code firefly_seed}. {@link BlockButterflyPlant} with three
 * differences: the gate is {@code 6 - age} (:32, always from age 5), it hatches at <em>night</em> under
 * {@code FireflyEnable} (:37), and it releases {@code 2 + world.rand.nextInt(5)} = 2..6 fireflies
 * (:38-41) - the count from the world's random, not {@code OreSpawnRand}. Icons {@code firefly_0..3},
 * drops {@code 1 + rand(5)} seeds unripe, the vanilla seed rolls ripe.
 */
public class BlockFireflyPlant extends CropBlock {

    public static final MapCodec<BlockFireflyPlant> CODEC = simpleCodec(BlockFireflyPlant::new);

    /** {@code BlockCrops}: {@code Material.plants}, no hardness, grass step sound, random ticks. */
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY);

    public BlockFireflyPlant(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockFireflyPlant> codec() {
        return CODEC;
    }

    /** {@code setTickRandomly(true)} (:19). PORT: keeps ticking at the maximum age, see {@link BlockButterflyPlant}. */
    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    /** {@code updateTick} (:22-43). */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel par1World, final BlockPos pos, final RandomSource par5Random) {
        super.randomTick(state, par1World, pos, par5Random);
        if (par1World.isRaining()) {
            return;
        }
        int rate = legacyMetadata(par1World, pos);
        rate &= 0x7;
        rate = 6 - rate;
        if (rate > 1 && OreSpawn.OreSpawnRand.nextInt(rate) != 0) {
            return;
        }
        final BlockState bid = par1World.getBlockState(pos.above());
        if (bid.isAir() && !InsectSupport.isDaytime(par1World) && MobSwitches.enabled(OreSpawnConfig.MOBS.FireflyEnable)) {
            rate = 2 + par1World.random.nextInt(5);
            for (int i = 0; i < rate; ++i) {
                spawnCreature(par1World, ModEntities.FIREFLY.get(), pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            }
        }
    }

    /** {@code getBlockMetadata} after the growth step. PORT: another block reads as 0, the metadata of air. */
    private int legacyMetadata(final ServerLevel level, final BlockPos pos) {
        final BlockState now = level.getBlockState(pos);
        return now.is(this) ? this.getAge(now) : 0;
    }

    /** {@code spawnCreature} (:45-54). */
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4, final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            ((Mob) var8).playAmbientSound();
        }
        return var8;
    }

    /** {@code func_149866_i} (:71-73). */
    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.FIREFLY_SEED;
    }

    /** Farmland or Crystal Grass (CrystalGrass.java:58-60). */
    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(CropBlocks.CRYSTAL_GRASS);
    }
}
