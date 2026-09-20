package com.swbr.orespawn.entity.boss.prince;

import com.swbr.orespawn.OreSpawn;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/**
 * Helpers {@link ThePrince}, {@link ThePrinceTeen} and {@link ThePrincess} share. No original class: each method stands
 * for a 1.7.10 idiom the three originals wrote inline ({@code spawnCreature}, {@code playSoundAtEntity}) or for the
 * one piece of 1.7.10 control flow 1.21.1 no longer allows a subclass to express (skipping
 * {@code super.updateAITasks()} in flight).
 */
final class PrinceSupport {

    /** Registry id of W10's {@code ThePrinceAdult} ("The Young Adult Prince"), written in parallel by w10-prince-adult. */
    static final ResourceLocation THE_YOUNG_ADULT_PRINCE = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_young_adult_prince");
    /** Registry id of W10's {@code PurplePower}, written in parallel by w10-king. */
    static final ResourceLocation PURPLE_POWER = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "purple_power");

    private PrinceSupport() {
    }

    /**
     * {@code spawnCreature(World, String, x, y, z)} (ThePrince.java:839-848, ThePrinceTeen.java:1342-1351,
     * ThePrincess.java:943-952): {@code EntityList.createEntityByName} (no {@code onSpawnWithEgg}),
     * {@code setLocationAndAngles} with a random yaw from the world random, {@code spawnEntityInWorld},
     * {@code playLivingSound}.
     */
    @Nullable
    static <T extends Mob> T spawnCreature(final Level par0World, final EntityType<T> type, final double par2,
                                           final double par4, final double par6) {
        final T var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            var8.playAmbientSound();
        }
        return var8;
    }

    /**
     * {@link #spawnCreature(Level, EntityType, double, double, double)} for a type of another porter of this wave, looked
     * up by registry id so that this package compiles on its own. A name {@code EntityList} did not know gave
     * {@code null}; so does an id that is not registered. The original cast to {@code EntityLiving}; a non-mob type
     * gives {@code null} as well.
     */
    @Nullable
    static Mob spawnCreature(final Level par0World, final ResourceLocation id, final double par2, final double par4,
                             final double par6) {
        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            return null;
        }
        final Entity created = type.create(par0World);
        if (!(created instanceof Mob var8)) {
            return null;
        }
        var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
        par0World.addFreshEntity(var8);
        var8.playAmbientSound();
        return var8;
    }

    /** {@code worldObj.playSoundAtEntity(entity, name, volume, pitch)}: heard by every player, the entity's own included. */
    static void playSoundAtEntity(final Entity entity, final SoundEvent sound, final float volume, final float pitch) {
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(), volume, pitch);
    }

    /**
     * {@code if (this.activity != 2) super.updateAITasks();} (ThePrince.java:501-503, ThePrincess.java:503-505).
     *
     * <p>1.7.10 {@code EntityLiving.updateAITasks} was one block: senses, target tasks, tasks, navigator, move helper,
     * look helper, jump helper. 1.21.1 runs the same steps in the final {@code Mob.serverAiStep}, with
     * {@code customServerAiStep} between the navigation and the controls, so a subclass can no longer skip the block.
     * PORT (R18 case 3): the decision is taken where 1.7.10 took it, at the start of the AI step, and applied as
     * <ul>
     *   <li>every control flag of both goal selectors disabled - {@code GoalSelector.tick} then stops running goals and
     *       starts none (all goals of the princes carry MOVE, LOOK, JUMP or TARGET);</li>
     *   <li>the move, look and jump controls idle ({@link GatedMoveControl} and siblings).</li>
     * </ul>
     * What still runs in flight: the sensing cache and {@code navigation.tick()} on a path that the stopped goals have
     * normally cleared.
     */
    static void gateGoals(final Mob mob, final boolean off) {
        for (final Goal.Flag flag : Goal.Flag.values()) {
            mob.goalSelector.setControlFlag(flag, !off);
            mob.targetSelector.setControlFlag(flag, !off);
        }
    }

    /**
     * The move helper the princes had: skipped while {@code super.updateAITasks()} is (see {@link #gateGoals}). This also
     * keeps the {@code moveForward = 0.75 * speed_factor} of {@code do_movement}, which 1.7.10 set after the helper
     * and 1.21.1's idle branch ({@code zza = 0}) would erase.
     */
    static final class GatedMoveControl extends MoveControl {
        private final BooleanSupplier off;

        GatedMoveControl(final Mob mob, final BooleanSupplier off) {
            super(mob);
            this.off = off;
        }

        @Override
        public void tick() {
            if (this.off.getAsBoolean()) {
                return;
            }
            super.tick();
        }
    }

    /** The look helper, skipped with {@code super.updateAITasks()}. */
    static final class GatedLookControl extends LookControl {
        private final BooleanSupplier off;

        GatedLookControl(final Mob mob, final BooleanSupplier off) {
            super(mob);
            this.off = off;
        }

        @Override
        public void tick() {
            if (this.off.getAsBoolean()) {
                return;
            }
            super.tick();
        }
    }

    /** The jump helper, skipped with {@code super.updateAITasks()}. */
    static final class GatedJumpControl extends JumpControl {
        private final BooleanSupplier off;

        GatedJumpControl(final Mob mob, final BooleanSupplier off) {
            super(mob);
            this.off = off;
        }

        @Override
        public void tick() {
            if (this.off.getAsBoolean()) {
                return;
            }
            super.tick();
        }
    }
}
