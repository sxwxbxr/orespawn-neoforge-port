package com.swbr.orespawn.block.torch;

import com.swbr.orespawn.OreSpawn;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockExtremeTorch}, standing half: Extreme Torch
 * ({@code extremetorch}, OreSpawnMain.java:1589, light 1.0 -> 15), tab Redstone
 * (BlockExtremeTorch.java:15-17). Wall half: {@link BlockExtremeWallTorch} ({@code extremetorch_wall}).
 * Support rules are vanilla's ({@code canPlaceBlockAt} -> {@code super}, :55-57).
 *
 * <p>{@code onBlockPlacedBy} (:59-107): placed on an Eye of Ender Block ({@code blockeyeofender},
 * W02 ores porter), it looks up to 100 times for a spot about four blocks away with a solid floor
 * and two air blocks, spawns a Cephadrome there (server), shows 16 smoke/poof/redstone particles
 * (client), plays the explosion sound and removes itself.
 *
 * <p>{@code randomDisplayTick} (:20-53): every display tick smoke, flame and redstone dust at the
 * flame position - and then, in the original, a client-side call of {@code onBlockPlacedBy} with a
 * null placer (:52). PORT: that call is dropped (DECISIONS R18, case 4): it made the client remove
 * its copy of the torch and play the sound while the server kept the block and spawned nothing.
 * The server-side summoning at placement is unchanged.
 */
public class BlockExtremeTorch extends TorchBlock {

    /** {@code OreSpawnMain.MyEyeOfEnderBlock} ({@code blockeyeofender}), referenced by id (other porter's holder). */
    public static final ResourceKey<Block> EYE_OF_ENDER_BLOCK = ResourceKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "blockeyeofender"));

    /**
     * {@code EntityList.createEntityByName("Cephadrome")}: the Cephadrome's entity type, resolved
     * through the registry at spawn time (the id is {@code ModEntities.CEPHADROME}, registered in W09).
     */
    public static final ResourceLocation CEPHADROME = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cephadrome");

    public BlockExtremeTorch(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /** {@code randomDisplayTick} (BlockExtremeTorch.java:20-53), standing branch, without the placement call. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        spawnParticles(level, pos.getX() + 0.5f, pos.getY() + 0.7f, pos.getZ() + 0.5f);
    }

    /** {@code smoke}, {@code flame}, {@code reddust} without velocity - every branch of {@code randomDisplayTick}. */
    static void spawnParticles(Level world, double x, double y, double z) {
        world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        onBlockPlacedBy(level, pos, placer);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    /**
     * {@code onBlockPlacedBy} (BlockExtremeTorch.java:59-105), shared with the wall half. Runs on both
     * sides like the original: the server spawns, the client shows particles, both play the sound
     * and remove the torch.
     */
    static void onBlockPlacedBy(final Level world, final BlockPos placed, @Nullable final LivingEntity par5EntityLiving) {
        final int par2 = placed.getX();
        final int par3 = placed.getY();
        final int par4 = placed.getZ();
        int x = par2;
        int y = par3;
        int z = par4;
        int found = 0;
        final RandomSource rand = world.random;
        if (world.getBlockState(new BlockPos(x, y - 1, z)).is(EYE_OF_ENDER_BLOCK)) {
            for (int tries = 0; tries < 100 && found == 0; ++tries) {
                if (rand.nextInt(2) == 0) {
                    x = par2 + 4 + rand.nextInt(3) - rand.nextInt(3);
                } else {
                    x = par2 - 4 + rand.nextInt(3) - rand.nextInt(3);
                }
                if (rand.nextInt(2) == 0) {
                    z = par4 + 4 + rand.nextInt(3) - rand.nextInt(3);
                } else {
                    z = par4 - 4 + rand.nextInt(3) - rand.nextInt(3);
                }
                for (y = par3 - 2; y <= par3 + 2; ++y) {
                    // getBlock(x, y - 1, z).getMaterial().isSolid() && air above it, twice
                    if (world.getBlockState(new BlockPos(x, y - 1, z)).isSolid()
                            && world.getBlockState(new BlockPos(x, y, z)).isAir()
                            && world.getBlockState(new BlockPos(x, y + 1, z)).isAir()) {
                        found = 1;
                        break;
                    }
                }
            }
            if (found != 0) {
                if (!world.isClientSide) {
                    spawnCreature(world, CEPHADROME, x + 0.5, y + 0.01, z + 0.5);
                } else {
                    for (int var3 = 0; var3 < 16; ++var3) {
                        world.addParticle(ParticleTypes.SMOKE,
                                par2 + rand.nextFloat() - rand.nextFloat(), par3 + rand.nextFloat(),
                                par4 + rand.nextFloat() - rand.nextFloat(), 0.0, 0.0, 0.0);
                        world.addParticle(ParticleTypes.POOF,
                                par2 + rand.nextFloat() - rand.nextFloat(), par3 + rand.nextFloat(),
                                par4 + rand.nextFloat() - rand.nextFloat(), 0.0, 0.0, 0.0);
                        world.addParticle(DustParticleOptions.REDSTONE,
                                par2 + rand.nextFloat() - rand.nextFloat(), par3 + rand.nextFloat(),
                                par4 + rand.nextFloat() - rand.nextFloat(), 0.0, 0.0, 0.0);
                    }
                }
                // playSoundAtEntity(placer, "random.explode", 1.0f, rand * 0.2f + 0.9f), else world.playSound at
                // the torch. PORT: the 1.21.1 idiom - the server plays to everyone but the placer, the
                // placer's own client plays it locally - so the placer hears it once, not twice.
                final Player except = par5EntityLiving instanceof Player p ? p : null;
                world.playSound(except, placed, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS,
                        1.0f, rand.nextFloat() * 0.2f + 0.9f);
                world.setBlock(placed, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * {@code spawnCreature} (BlockExtremeTorch.java:109-118): create by name, place with a random yaw,
     * add to the world, play the living sound. No {@code finalizeSpawn} - the original had none.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final ResourceLocation par1,
                                       final double par2, final double par4, final double par6) {
        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(par1).orElse(null);
        if (type == null) {
            return null;
        }
        final Entity var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }
}
