package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Port of {@code danger.orespawn.RubyBird} (RubyBird.java:5-30), id {@code ruby_bird} ("Ruby Bird",
 * OreSpawnMain.java:3499-3503, tracking 32/1/false). A {@link Cockateil} fixed on colour 5 with its own call,
 * spawned only by the spawner of {@code RubyBirdDungeon} (W12). Everything else is inherited.
 */
public class RubyBird extends Cockateil {

    /**
     * Constructor (:9-11) plus the colour of {@code entityInit} (:14): {@code setBirdType(birdtype = 5)}
     * after the {@code Cockateil} roll, on both sides. 5 differs from the accessor default 0, so the server
     * sends it with the spawn packet.
     */
    public RubyBird(final EntityType<? extends RubyBird> type, final Level par1World) {
        super(type, par1World);
        this.setBirdType(this.birdtype = 5);
    }

    /**
     * The {@code setFlyUp()} of {@code entityInit} (:15), called where 1.7.10 called it: inside the entity
     * constructor, before {@code Cockateil}'s field initializers. {@code flyup = 0} then overwrites the 2,
     * so the call has no effect - the original finding, kept 1:1 (R18, verhalten/entity-11.md). The colour is
     * set in the constructor instead, because {@code entityData} does not exist yet at this point.
     */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        this.setFlyUp();
    }

    /** {@code getLivingSound} (:18-24): only by day and without rain. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (InsectSupport.isDaytime(this.level()) && !this.level().isRaining()) {
            return ModSounds.RUBYBIRD.get();
        }
        return null;
    }

    /** {@code getCanSpawnHere} (:26-29): always. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** {@code getCanSpawnHere} (:26-29) as the placement predicate: always. */
    public static boolean checkRubyBirdSpawnRules(final EntityType<RubyBird> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }
}
