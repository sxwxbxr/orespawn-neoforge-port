package com.swbr.orespawn.entity.robot;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Code the six robot classes carried as identical private copies: {@code isSuitableTarget},
 * {@code findSomethingToAttack}, {@code dropItemRand}, the redstone loot switch, the heading test and the halves of
 * {@code getCanSpawnHere}. One copy here (R21, no duplicated helpers); every method names the original lines of
 * each class it replaces.
 */
public final class RobotSupport {

    private RobotSupport() {
    }

    /**
     * {@code isSuitableTarget} (Robot1.java:163-190, Robot2.java:352-379, Robot3.java:275-302, Robot4.java:341-368,
     * Robot5.java:249-276, GiantRobot.java:299-326), identical in all six: alive, not ignorable, visible, no
     * {@code EntityMob} (= {@link Monster}, see {@link MyUtils}) and no player in creative mode.
     *
     * <p>{@code capabilities.isCreativeMode} is {@code Abilities.instabuild} (HerbivoreSupport precedent).
     */
    static boolean isSuitableTarget(final Mob self, @Nullable final LivingEntity par1EntityLiving) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == self) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!self.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@code findSomethingToAttack} (Robot1.java:192-205 box 8/3/8, Robot2.java:381-394 box 14/3/14,
     * Robot3.java:304-317 box 16/3/16, Robot4.java:370-383 box 16/4/16, Robot5.java:278-291 box 30/6/30,
     * GiantRobot.java:328-341 box 16/12/16): {@code PlayNicely} disables it, then the nearest suitable living
     * entity by {@link GenericTargetSorter}. {@code boundingBox.expand} grew both sides, as {@code inflate} does.
     */
    @Nullable
    static LivingEntity findSomethingToAttack(final Mob self, final GenericTargetSorter sorter, final double x,
                                              final double y, final double z) {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = self.level().getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(x, y, z));
        var5.sort(sorter);
        for (final LivingEntity var8 : var5) {
            if (isSuitableTarget(self, var8)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * The heading test of {@code updateAITasks} (Robot2.java:288-295 with {@code rotationYaw}; Robot3.java:236-243,
     * Robot4.java:280-287, Robot5.java:208-215, GiantRobot.java:236-243 with {@code rotationYawHead}): the absolute
     * angle between the direction to the target and {@code yaw + 90} degrees, folded into 0..pi with the original's
     * own pi {@code 3.1415926545}.
     */
    static double headingDifference(final Entity self, final Entity e, final float yaw) {
        final double rr = Math.atan2(e.getZ() - self.getZ(), e.getX() - self.getX());
        final double rhdir = Math.toRadians((yaw + 90.0f) % 360.0f);
        final double pi = 3.1415926545;
        double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
        if (rdd > pi) {
            rdd -= pi * 2.0;
        }
        rdd = Math.abs(rdd);
        return rdd;
    }

    /**
     * {@code World.playSoundAtEntity(entity, name, volume, pitch)}: at the entity's position for every nearby
     * player; on the client it does nothing, as the 1.7.10 client world did. Same reading as
     * {@code CompanionSupport.playSoundAtEntity} (package-private there).
     */
    static void playSoundAtEntity(final Entity entity, final SoundEvent sound, final float volume, final float pitch) {
        entity.level().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(), volume, pitch);
    }

    /**
     * {@code dropItemRand} (Robot2.java:139-147, Robot3.java:138-146, Robot4.java:168-176, Robot5.java:110-118,
     * GiantRobot.java:130-138): an item entity one block up, x and z shifted by {@code OreSpawnRand} -1..+1, spawned
     * directly - not through {@code entityDropItem}, so no pickup delay and no drop capture, as in 1.7.10.
     */
    static ItemStack dropItemRand(final Mob self, final ItemLike index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(self.level(), self.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                self.getY() + 1.0, self.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), is);
        self.level().addFreshEntity(var3);
        return is;
    }

    /**
     * One roll of the redstone loot of Robot2 to Robot5 (Robot2.java:158-200, Robot3.java:154-196,
     * Robot4.java:186-228, Robot5.java:126-168): {@code rand(15)} 0..9 give one item, redstone block twice
     * (cases 3 and 8), 10..14 nothing.
     */
    static void dropRedstoneRoll(final Mob self, final int var7) {
        switch (var7) {
            case 0 -> dropItemRand(self, Items.REDSTONE, 1);
            case 1 -> dropItemRand(self, Items.REPEATER, 1);
            case 2 -> dropItemRand(self, Items.COMPARATOR, 1);
            case 3 -> dropItemRand(self, Blocks.REDSTONE_BLOCK, 1);
            case 4 -> dropItemRand(self, Blocks.DISPENSER, 1);
            case 5 -> dropItemRand(self, Blocks.STICKY_PISTON, 1);
            case 6 -> dropItemRand(self, Blocks.PISTON, 1);
            case 7 -> dropItemRand(self, Blocks.LEVER, 1);
            case 8 -> dropItemRand(self, Blocks.REDSTONE_BLOCK, 1);
            case 9 -> dropItemRand(self, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1);
            default -> {
            }
        }
    }

    /**
     * The air column of {@code getCanSpawnHere} (Robot2.java:426-435, Robot3.java:334-343, Robot4.java:423-432,
     * GiantRobot.java:358-367 with {@code i < 6}; Robot5.java:323-332 with {@code i < 3}): x -1..+1, z -1..0,
     * y +1 .. {@code maxI - 1}, each block air or {@code tallgrass}.
     *
     * <p>PORT: a placement predicate receives the block position; the mob stands at its centre, so
     * {@code (int) posX} is {@code pos.getX()} (R20, floor instead of truncation). {@code Blocks.air} is
     * {@code isAir()} (1.7.10 had one air block); {@code Blocks.tallgrass} is
     * {@link CannonFodderSupport#isLegacyTallGrass} (short grass and fern, W06 decision on meta 0).
     */
    static boolean columnIsClear(final LevelReader level, final BlockPos pos, final int maxI) {
        for (int k = -1; k < 1; ++k) {
            for (int j = -1; j <= 1; ++j) {
                for (int i = 1; i < maxI; ++i) {
                    final BlockState bid = level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k));
                    if (!bid.isAir() && !CannonFodderSupport.isLegacyTallGrass(bid)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
