package com.swbr.orespawn.entity.moth;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.registry.ModBlocks;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.7.10 idioms that {@link Mothra}, {@link Brutalfly} and {@link CaterKiller} repeat inline. No original class: each
 * method stands for a block of statements the three originals copied between each other, or for a piece of 1.7.10
 * {@code World} they called.
 */
final class MothSupport {

    /** {@code orespawn:vortex} (manifest), see {@link #isType}. */
    static final ResourceLocation VORTEX = id("vortex");
    /** {@code orespawn:terrible_terror}. */
    static final ResourceLocation TERRIBLE_TERROR = id("terrible_terror");
    /** {@code orespawn:lurking_terror}. */
    static final ResourceLocation LURKING_TERROR = id("lurking_terror");
    /** {@code orespawn:cloud_shark}. */
    static final ResourceLocation CLOUD_SHARK = id("cloud_shark");
    /** {@code orespawn:rotator}. */
    static final ResourceLocation ROTATOR = id("rotator");
    /** {@code orespawn:mantis}. */
    static final ResourceLocation MANTIS = id("mantis");

    private MothSupport() {}

    private static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    /**
     * {@code instanceof Vortex}, {@code TerribleTerror}, {@code LurkingTerror}, {@code CloudShark}, {@code Rotator},
     * {@code Mantis}. PORT: those classes are written by parallel porters of the same wave (W08 crystal, terrors, water);
     * the test compares the registry id so this package compiles on its own (W07 precedent
     * {@code ArthropodSupport.isEnderKnight}). None of the six has a subclass in 20.2 (grep {@code extends}), so the id
     * names the same set of entities. The integrator may switch to {@code instanceof}.
     */
    static boolean isType(final Entity e, final ResourceLocation id) {
        return EntityType.getKey(e.getType()).equals(id);
    }

    /**
     * {@code World.findNearestEntityWithinAABB(EntityPlayer.class, boundingBox.expand(x, y, z), self)} ({@code ahb.a(Class,
     * azt, sa)}): every player in the box except {@code self}, the nearest by squared distance; {@code <=} keeps the
     * last of equally near ones (EntityAIWatchClosest, W06).
     */
    @Nullable
    static Player findNearestPlayer(final Entity self, final double x, final double y, final double z) {
        final List<Player> var4 = self.level().getEntitiesOfClass(Player.class, self.getBoundingBox().inflate(x, y, z));
        Player var5 = null;
        double var6 = Double.MAX_VALUE;
        for (final Player var9 : var4) {
            if (var9 == self) {
                continue;
            }
            final double var10 = self.distanceToSqr(var9);
            if (var10 > var6) {
                continue;
            }
            var5 = var9;
            var6 = var10;
        }
        return var5;
    }

    /** {@code getBlock(x, y, z) == Blocks.air}. PORT: {@code isAir()} also accepts cave and void air, which 1.7.10 generated as plain air. */
    static boolean isAir(final LevelAccessor level, final int x, final int y, final int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    /**
     * The shared firing block of {@code Mothra.attackWithSomething} (Mothra.java:393-426) and
     * {@code Brutalfly.attackWithSomething} (Brutalfly.java:364-397): muzzle 2.25 blocks ahead of the body centre at
     * the feet height; EASY a small fireball with {@code random.bow} 0.75, NORMAL a coin flip on the world random between
     * that and a {@link BetterFireball} with {@code setNotMe()} and {@code random.fuse} 1.0, every other difficulty (HARD
     * and, if ever reached, PEACEFUL) the BetterFireball.
     *
     * <p>PORT: {@code "random.bow"} is {@link SoundEvents#ARROW_SHOOT}, {@code "random.fuse"} {@link SoundEvents#TNT_PRIMED};
     * {@code playSoundAtEntity} is {@code Level.playSound(null, ...)} at the feet with the mob's sound source.
     */
    static void fireAt(final Mob self, final LivingEntity par1) {
        final double xzoff = 2.25;
        final double yoff = 0.0;
        final double cx = self.getX() - xzoff * Math.sin(Math.toRadians(self.getYRot()));
        final double cz = self.getZ() + xzoff * Math.cos(Math.toRadians(self.getYRot()));
        final Level world = self.level();
        if (world.getDifficulty() == Difficulty.EASY) {
            smallFireball(self, par1, cx, yoff, cz);
        } else if (world.getDifficulty() == Difficulty.NORMAL) {
            if (world.random.nextInt(2) == 0) {
                smallFireball(self, par1, cx, yoff, cz);
            } else {
                betterFireball(self, par1, cx, yoff, cz);
            }
        } else {
            betterFireball(self, par1, cx, yoff, cz);
        }
    }

    /**
     * {@code new EntitySmallFireball(world, self, dx, dy, dz)}, {@code setLocationAndAngles(cx, posY + yoff, cz, yaw, 0)},
     * {@code setPosition}, {@code random.bow}, spawn.
     *
     * <p>PORT: the 1.7.10 {@code EntityFireball} constructor added {@code nextGaussian() * 0.4} to each direction
     * component on the fireball's own random before normalising; 1.21.1's {@link SmallFireball} takes the direction as
     * is, so the spread is drawn here from the shooter's random (Kyuubi, W07). The flight follows 1.21.1 hurting-projectile
     * physics; damage and entity fire are the vanilla small fireball in both versions.
     */
    private static void smallFireball(final Mob self, final LivingEntity par1, final double cx, final double yoff, final double cz) {
        final double dx = par1.getX() - cx + self.getRandom().nextGaussian() * 0.4;
        final double dy = par1.getY() + 0.55 - (self.getY() + yoff) + self.getRandom().nextGaussian() * 0.4;
        final double dz = par1.getZ() - cz + self.getRandom().nextGaussian() * 0.4;
        final SmallFireball sf = legacySmallFireball(self.level(), self, new Vec3(dx, dy, dz));
        sf.moveTo(cx, self.getY() + yoff, cz, self.getYRot(), 0.0f);
        sf.setPos(cx, self.getY() + yoff, cz);
        self.level().playSound(null, self.getX(), self.getY(), self.getZ(), SoundEvents.ARROW_SHOOT, self.getSoundSource(),
                0.75f, 1.0f / (self.getRandom().nextFloat() * 0.4f + 0.8f));
        self.level().addFreshEntity(sf);
    }

    /** {@code new BetterFireball(world, self, dx, dy, dz)}, placed like the small one, {@code setNotMe()}, {@code random.fuse}, spawn. */
    private static void betterFireball(final Mob self, final LivingEntity par1, final double cx, final double yoff, final double cz) {
        final BetterFireball bf = new BetterFireball(self.level(), self, par1.getX() - cx, par1.getY() + 0.55 - (self.getY() + yoff),
                par1.getZ() - cz);
        bf.moveTo(cx, self.getY() + yoff, cz, self.getYRot(), 0.0f);
        bf.setPos(cx, self.getY() + yoff, cz);
        bf.setNotMe();
        self.level().playSound(null, self.getX(), self.getY(), self.getZ(), SoundEvents.TNT_PRIMED, self.getSoundSource(),
                1.0f, 1.0f / (self.getRandom().nextFloat() * 0.4f + 0.8f));
        self.level().addFreshEntity(bf);
    }

    /**
     * A vanilla small fireball whose block hit lights air without the 1.21.1 mobGriefing gate, as 1.7.10
     * {@code EntitySmallFireball.onImpact} did.
     *
     * <p>PORT: the same anonymous subclass as {@code Kyuubi.shootFireball} (W07, private there); a fireball reloaded from
     * disk is a plain vanilla one again and follows the 1.21.1 rule. R21: whoever moves the shared projectile helpers to
     * {@code entity.projectile} merges both copies.
     */
    private static SmallFireball legacySmallFireball(final Level level, final LivingEntity owner, final Vec3 movement) {
        return new SmallFireball(level, owner, movement) {
            @Override
            protected void onHitBlock(final BlockHitResult result) {
                super.onHitBlock(result);
                if (!this.level().isClientSide) {
                    final BlockPos blockpos = result.getBlockPos().relative(result.getDirection());
                    if (this.level().isEmptyBlock(blockpos)) {
                        this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                    }
                }
            }
        };
    }

    /**
     * {@code Blocks.leaves}: oak, spruce, birch and jungle as metadata 0-3 (plus the decay bits); acacia and dark oak were
     * the separate {@code Blocks.leaves2}.
     *
     * <p>PORT (R22): "leaves" is a category, so every block of {@code #minecraft:leaves} counts - acacia, dark oak, azalea,
     * mangrove and cherry included. Where a caller also names {@code leaves2} ({@link #isLegacyLeaves2}) that term is now
     * redundant; {@code CaterKiller.MyCanSee} (original :704), which named only {@code Blocks.leaves}, lets acacia and dark
     * oak leaves through as well. OreSpawn's own leaves are excluded: the port adds them to that tag
     * ({@code data/minecraft/tags/block/leaves.json}), but they were separate blocks with their own terms
     * ({@link #isOreSpawnLeaves}), and {@code MyCanSee} and the spawn check never let them through.
     */
    static boolean isLegacyLeaves(final BlockState state) {
        return state.is(BlockTags.LEAVES)
                && !OreSpawn.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    /** {@code Blocks.leaves2}: acacia and dark oak leaves. */
    static boolean isLegacyLeaves2(final BlockState state) {
        return state.is(Blocks.ACACIA_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES);
    }

    /**
     * {@code Blocks.log}: oak, spruce, birch and jungle logs (metadata 0-3 with the axis bits) and their six-sided wood
     * blocks (metadata 12-15), as in {@code Beaver.isWood} (W06). Stripped logs did not exist.
     */
    static boolean isLegacyLog(final BlockState state) {
        return state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG)
                || state.is(Blocks.OAK_WOOD) || state.is(Blocks.SPRUCE_WOOD) || state.is(Blocks.BIRCH_WOOD) || state.is(Blocks.JUNGLE_WOOD);
    }

    /** {@code Blocks.log2}: acacia and dark oak log and wood. */
    static boolean isLegacyLog2(final BlockState state) {
        return state.is(Blocks.ACACIA_LOG) || state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.ACACIA_WOOD) || state.is(Blocks.DARK_OAK_WOOD);
    }

    /** OreSpawn's five {@code BlockLeaves} subclasses: {@code MyAppleLeaves}, {@code MyExperienceLeaves}, {@code MyScaryLeaves}, {@code MyPeachLeaves}, {@code MyCherryLeaves}. */
    static boolean isOreSpawnLeaves(final BlockState state) {
        return state.is(ModBlocks.LEAVES_APPLE.get()) || state.is(ModBlocks.LEAVES_EXPERIENCE.get()) || state.is(ModBlocks.LEAVES_SCARY.get())
                || state.is(ModBlocks.LEAVES_PEACH.get()) || state.is(ModBlocks.LEAVES_CHERRY.get());
    }

    /**
     * {@code Blocks.tallgrass}: short grass and fern. Meta 0, the "shrub", became the dead bush in the flattening and is
     * left out (CannonFodderSupport.isLegacyTallGrass, W06).
     */
    static boolean isLegacyTallGrass(final BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }
}
