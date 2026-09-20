package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.dimension.chaos.WorldProviderOreSpawn6;
import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 idioms the seven W08 terror classes ({@link TerribleTerror}, {@link LurkingTerror}, {@link CreepingHorror},
 * {@link Mantis}, {@link Rat}, {@link Crab}) repeat inline. No original class. Pieces that earlier waves already made
 * public are called there instead of copied (R21): {@code ArthropodSupport.findSomethingToAttack},
 * {@code ArthropodSupport.dropItemRand}, {@code ArthropodSupport.playSoundAtEntity}, {@code InsectSupport.isDaytime},
 * {@code LegacyPanic.legacyPanic}, {@code HerbivoreSupport.legacyDropFewItems},
 * {@code LegacyLightLevel.isValidLightLevel}.
 */
public final class TerrorSupport {

    private TerrorSupport() {}

    /**
     * {@code instanceof <OreSpawn class>} for classes that are written by a parallel porter of this wave or by a later
     * wave (CloudShark, Rotator, Mothra, Triffid, PitchBlack, Island, IslandToo, Irukandji, Skate, AttackSquid,
     * DungeonBeast; Dragon, WaterDragon, RubberDucky).
     *
     * <p>PORT: the test compares the registry id from {@code manifest.json} so this package compiles on its own
     * (ArthropodSupport precedent, W07). None of these classes has a subclass in 20.2 except through
     * {@code EntityButterfly} (Mothra), which the callers test with {@code instanceof EntityButterfly} as well, so the
     * id names the same set of entities. The integrator may switch to {@code instanceof}.
     */
    public static boolean isType(final Entity e, final String id) {
        return EntityType.getKey(e.getType()).equals(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id));
    }

    /** {@code worldObj.provider.dimensionId == OreSpawnMain.DimensionID6}: the Chaos dimension. */
    public static boolean isChaos(final Level level) {
        return level.dimension() == WorldProviderOreSpawn6.DIMENSION;
    }

    /** {@code worldObj.provider.dimensionId == OreSpawnMain.DimensionID5}: the Crystal dimension. */
    public static boolean isCrystal(final Level level) {
        return level.dimension() == WorldProviderOreSpawn5.DIMENSION;
    }

    /** The creative test of {@code isSuitableTarget}: {@code p.capabilities.isCreativeMode}. */
    public static boolean isCreative(final LivingEntity e) {
        return e instanceof Player p && p.getAbilities().instabuild;
    }

    /**
     * 1.7.10 {@code World.getPlayerEntityByName(name)}: the first player of this world whose command sender name equals
     * {@code name}. {@code null} for a {@code null} name.
     */
    @Nullable
    public static Player getPlayerEntityByName(final Level level, @Nullable final String name) {
        if (name == null) {
            return null;
        }
        for (final Player p : level.players()) {
            if (name.equals(p.getName().getString())) {
                return p;
            }
        }
        return null;
    }

    /**
     * 1.7.10 {@code EntityLivingBase.heal(amount)} for any sign of {@code amount}: {@code setHealth(health + amount)}
     * while the health is above 0.
     *
     * <p>PORT (R18, Hydrolisc ruling): NeoForge's {@code LivingEntity.heal} returns for amounts {@code <= 0}
     * (LivingEntity.java:1119), so the negative "heal" of the originals is written out through {@code setHealth}.
     */
    public static void legacyHeal(final LivingEntity entity, final float amount) {
        final float f1 = entity.getHealth();
        if (f1 > 0.0f) {
            entity.setHealth(f1 + amount);
        }
    }
}
