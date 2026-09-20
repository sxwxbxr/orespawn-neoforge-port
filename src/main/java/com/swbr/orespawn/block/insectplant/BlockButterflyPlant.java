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
 * Port of {@code danger.orespawn.BlockButterflyPlant} (BlockButterflyPlant.java:13-83), id
 * {@code butterfly_plant}, planted by {@code butterfly_seed}. A vanilla {@code BlockCrops} that hatches
 * butterflies: after the normal growth step of each random tick, on a dry day with air above, one
 * butterfly with chance {@code 1/(7 - age)} (always from age 6).
 *
 * <p>Drops (loot table {@code blocks/butterfly_plant}): {@code quantityDropped} {@code 1 + rand(5)} seeds
 * (:64-66) while unripe; the ripe plant's crop item is {@code null} (:72-74) so it gives only the vanilla
 * {@code 3 + fortune} rolls of {@code rand(15) <= 7} on the seed. The four icons {@code butterfly_0..3}
 * (:53-62: age 0-1, 2-3, 4-6, 7) are the blockstate file.
 */
public class BlockButterflyPlant extends CropBlock {

    public static final MapCodec<BlockButterflyPlant> CODEC = simpleCodec(BlockButterflyPlant::new);

    /** {@code BlockCrops}: {@code Material.plants}, no hardness, grass step sound, random ticks (as the W02 crops). */
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY);

    public BlockButterflyPlant(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockButterflyPlant> codec() {
        return CODEC;
    }

    /**
     * {@code setTickRandomly(true)} (:19): 1.7.10 ticked the plant at every age. PORT: 1.21.1
     * {@code CropBlock} stops ticking at the maximum age, which would end the hatching exactly where the
     * original hatched most reliably.
     */
    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    /** {@code updateTick} (:22-40); random ticks run on the server only, which is the {@code isRemote} return of :24-26. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel par1World, final BlockPos pos, final RandomSource par5Random) {
        super.randomTick(state, par1World, pos, par5Random);
        if (par1World.isRaining()) {
            return;
        }
        int rate = legacyMetadata(par1World, pos);
        rate &= 0x7;
        rate = 7 - rate;
        if (rate > 1 && OreSpawn.OreSpawnRand.nextInt(rate) != 0) {
            return;
        }
        final BlockState bid = par1World.getBlockState(pos.above());
        if (bid.isAir() && InsectSupport.isDaytime(par1World) && MobSwitches.enabled(OreSpawnConfig.MOBS.ButterflyEnable)) {
            spawnCreature(par1World, ModEntities.BUTTERFLY.get(), pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
        }
    }

    /**
     * {@code getBlockMetadata} after the growth step: the age the super call may just have raised.
     * PORT: a block other than this plant has no age; it reads as 0, the metadata of air.
     */
    private int legacyMetadata(final ServerLevel level, final BlockPos pos) {
        final BlockState now = level.getBlockState(pos);
        return now.is(this) ? this.getAge(now) : 0;
    }

    /**
     * {@code spawnCreature} (:42-51): create by type (was: by name), place with a random yaw from the
     * world's random, add, and play its living sound.
     */
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

    /** {@code func_149866_i} (getSeedItem, :68-70) - pick block. */
    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BUTTERFLY_SEED;
    }

    /** Farmland, or Crystal Grass, which sustained every plant (CrystalGrass.java:58-60) - as the W02 crops. */
    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(CropBlocks.CRYSTAL_GRASS);
    }
}
