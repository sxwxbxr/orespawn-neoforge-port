package com.swbr.orespawn.client.gui;

import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import com.swbr.orespawn.entity.arthropod.EmperorScorpion;
import com.swbr.orespawn.entity.arthropod.HerculesBeetle;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.boss.mobzilla.Godzilla;
import com.swbr.orespawn.entity.boss.prince.ThePrince;
import com.swbr.orespawn.entity.boss.prince.ThePrinceTeen;
import com.swbr.orespawn.entity.boss.prince.ThePrincess;
import com.swbr.orespawn.entity.boss.princeadult.ThePrinceAdult;
import com.swbr.orespawn.entity.boss.queen.TheQueen;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.crystal.Vortex;
import com.swbr.orespawn.entity.dino.Alosaurus;
import com.swbr.orespawn.entity.dino.Basilisk;
import com.swbr.orespawn.entity.dino.Nastysaurus;
import com.swbr.orespawn.entity.dino.TRex;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.monster.Alien;
import com.swbr.orespawn.entity.monster.BandP;
import com.swbr.orespawn.entity.monster.Hammerhead;
import com.swbr.orespawn.entity.monster.Kyuubi;
import com.swbr.orespawn.entity.monster.Molenoid;
import com.swbr.orespawn.entity.moth.CaterKiller;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.robot.GiantRobot;
import com.swbr.orespawn.entity.robot.Robot2;
import com.swbr.orespawn.entity.robot.Robot4;
import com.swbr.orespawn.entity.sea.Irukandji;
import com.swbr.orespawn.entity.sea.SeaMonster;
import com.swbr.orespawn.entity.sea.SeaViper;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import com.swbr.orespawn.entity.terror.Crab;
import com.swbr.orespawn.entity.terror.Mantis;
import com.swbr.orespawn.entity.triffid.Triffid;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.entity.worm.WormLarge;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The part of {@code GirlfriendOverlayGui.onRenderOverlay} (GirlfriendOverlayGui.java:24-390) that decides <em>what</em>
 * the bar shows: the fallback ray of {@code OreSpawnMain.getPointedAtEntity} and the 45-class chain that picks a name
 * and a health ratio. {@link GirlfriendOverlayGui} draws the result.
 *
 * <p>PORT: split off from the layer so a GameTest on the dedicated server can check the ratio; this class imports
 * nothing from {@code net.minecraft.client} and loads on both sides (STYLE, server checklist). The package stays
 * {@code client.gui} because only the overlay uses it.
 *
 * <p>Health (R4, R25): 1.7.10 divided by {@code getMaxHealth()}, which for The King, The Queen, Mobzilla, the young
 * princes, the robot spider and a doubled Kraken or Jeffery was the original maximum. In 1.21.1 their attribute is
 * clamped to 1024 and the stored health scaled by the same factor, so {@code getHealth() / getMaxHealth()} - both
 * values synchronized to the client by vanilla - is still the original proportion ({@link #healthRatio}). The
 * {@code get<Name>Health()} getters return original units (Kraken's goes through {@link VirtualHealth}), so they are
 * divided by {@link VirtualHealth#originalMaxHealth} ({@link #getterRatio}), which is {@code getMaxHealth()} for every
 * class without virtual health.
 *
 * @param name  the text above the bar ({@code outstring})
 * @param ratio {@code gfHealth}, current over maximum health
 */
public record GirlfriendOverlayTarget(String name, float ratio) {

    /** 1.7.10 {@code getHealth() / getMaxHealth()}: the synchronized ratio, identical in original and attribute scale. */
    public static float healthRatio(final LivingEntity e) {
        return e.getHealth() / e.getMaxHealth();
    }

    /** 1.7.10 {@code get<Name>Health() / getMaxHealth()} for a getter that returns original units. */
    public static float getterRatio(final int originalHealth, final LivingEntity e) {
        return originalHealth / (float) VirtualHealth.originalMaxHealth(e);
    }

    /** 1.7.10 {@code getCustomNameTag()}: the plain name, empty without one. */
    private static String customName(final Entity e) {
        return e.getCustomName() == null ? "" : e.getCustomName().getString();
    }

    /**
     * The class chain of {@code onRenderOverlay} (:65-390) in the original order: every later match overwrites an
     * earlier one, and a failed owner or activity condition aborts the whole overlay.
     *
     * @return the name and ratio, or {@code null} where the original returned without drawing
     */
    @Nullable
    public static GirlfriendOverlayTarget select(final Entity entity, final Player player) {
        String outstring = null;
        float gfHealth = 0.0f;
        if (entity instanceof Girlfriend) {
            Girlfriend gf = null;
            gf = (Girlfriend) entity;
            // func_152114_e(player) is "is owner" (verhalten/itemblock-03.md).
            if (!gf.isOwnedBy(player)) {
                return null;
            }
            if (gf.passenger != 0) {
                return null;
            }
            if (gf.hasCustomName()) {
                outstring = customName(gf);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Girlfriend";
            }
            gfHealth = getterRatio(gf.getGirlfriendHealth(), gf);
        }
        if (entity instanceof Boyfriend) {
            Boyfriend gf2 = null;
            gf2 = (Boyfriend) entity;
            if (!gf2.isOwnedBy(player)) {
                return null;
            }
            if (gf2.passenger != 0) {
                return null;
            }
            if (gf2.hasCustomName()) {
                outstring = customName(gf2);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Boyfriend";
            }
            gfHealth = getterRatio(gf2.getBoyfriendHealth(), gf2);
        }
        if (entity instanceof ThePrince) {
            ThePrince gf3 = null;
            gf3 = (ThePrince) entity;
            if (!gf3.isOwnedBy(player)) {
                return null;
            }
            if (gf3.hasCustomName()) {
                outstring = customName(gf3);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Toddler Prince";
            }
            gfHealth = healthRatio(gf3);
        }
        if (entity instanceof ThePrincess) {
            ThePrincess gf4 = null;
            gf4 = (ThePrincess) entity;
            if (!gf4.isOwnedBy(player)) {
                return null;
            }
            if (gf4.hasCustomName()) {
                outstring = customName(gf4);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Toddler Princess";
            }
            gfHealth = healthRatio(gf4);
        }
        if (entity instanceof ThePrinceTeen) {
            ThePrinceTeen gf5 = null;
            gf5 = (ThePrinceTeen) entity;
            if (!gf5.isOwnedBy(player)) {
                return null;
            }
            if (gf5.hasCustomName()) {
                outstring = customName(gf5);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Young Prince";
            }
            if (gf5.getActivity() != 0) {
                return null;
            }
            gfHealth = healthRatio(gf5);
        }
        if (entity instanceof ThePrinceAdult) {
            ThePrinceAdult gf6 = null;
            gf6 = (ThePrinceAdult) entity;
            if (!gf6.isOwnedBy(player)) {
                return null;
            }
            if (gf6.hasCustomName()) {
                outstring = customName(gf6);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Young Adult Prince";
            }
            if (gf6.getActivity() != 0) {
                return null;
            }
            gfHealth = healthRatio(gf6);
        }
        if (entity instanceof Dragon) {
            Dragon df = null;
            df = (Dragon) entity;
            if (df.hasCustomName()) {
                outstring = customName(df);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Dragon";
            }
            if (df.getActivity() != 0) {
                return null;
            }
            gfHealth = getterRatio(df.getDragonHealth(), df);
        }
        if (entity instanceof EmperorScorpion) {
            final EmperorScorpion e = (EmperorScorpion) entity;
            outstring = "Emperor Scorpion";
            gfHealth = getterRatio(e.getEmperorScorpionHealth(), e);
        }
        if (entity instanceof Basilisk) {
            final Basilisk e2 = (Basilisk) entity;
            outstring = "Basilisk";
            gfHealth = getterRatio(e2.getBasiliskHealth(), e2);
        }
        if (entity instanceof Mothra) {
            final Mothra e3 = (Mothra) entity;
            outstring = "Mothra!";
            gfHealth = getterRatio(e3.getMothraHealth(), e3);
        }
        if (entity instanceof Spyro) {
            final Spyro e4 = (Spyro) entity;
            if (e4.hasCustomName()) {
                outstring = customName(e4);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Baby Dragon";
            }
            gfHealth = getterRatio(e4.getSpyroHealth(), e4);
        }
        if (entity instanceof WormLarge) {
            final WormLarge e5 = (WormLarge) entity;
            // noClip is noPhysics.
            if (!e5.noPhysics) {
                outstring = "Worm";
                gfHealth = healthRatio(e5);
            }
        }
        if (entity instanceof Alien) {
            final Alien e6 = (Alien) entity;
            outstring = "Alien!";
            gfHealth = getterRatio(e6.getAlienHealth(), e6);
        }
        if (entity instanceof WaterDragon) {
            final WaterDragon e7 = (WaterDragon) entity;
            if (e7.hasCustomName()) {
                outstring = customName(e7);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "WaterDragon";
            }
            gfHealth = getterRatio(e7.getWaterDragonHealth(), e7);
        }
        if (entity instanceof Kraken) {
            final Kraken e8 = (Kraken) entity;
            outstring = "Kraken";
            gfHealth = getterRatio(e8.getKrakenHealth(), e8);
        }
        if (entity instanceof Cephadrome) {
            final Cephadrome e9 = (Cephadrome) entity;
            outstring = "Cephadrome";
            gfHealth = getterRatio(e9.getCephadromeHealth(), e9);
            if (e9.getActivity() != 0) {
                return null;
            }
        }
        if (entity instanceof TrooperBug) {
            final TrooperBug e10 = (TrooperBug) entity;
            outstring = "Jumpy Bug";
            gfHealth = getterRatio(e10.getTrooperBugHealth(), e10);
        }
        if (entity instanceof SpitBug) {
            final SpitBug e11 = (SpitBug) entity;
            outstring = "Spit Bug";
            gfHealth = healthRatio(e11);
        }
        if (entity instanceof PitchBlack) {
            final PitchBlack e12 = (PitchBlack) entity;
            outstring = "Nightmare";
            gfHealth = healthRatio(e12);
        }
        if (entity instanceof Alosaurus) {
            final Alosaurus e13 = (Alosaurus) entity;
            outstring = "Alosaurus";
            gfHealth = healthRatio(e13);
        }
        if (entity instanceof Nastysaurus) {
            final Nastysaurus e14 = (Nastysaurus) entity;
            outstring = "Nastysaurus";
            gfHealth = healthRatio(e14);
        }
        if (entity instanceof TRex) {
            final TRex e15 = (TRex) entity;
            outstring = "T. Rex";
            gfHealth = healthRatio(e15);
        }
        if (entity instanceof Kyuubi) {
            final Kyuubi e16 = (Kyuubi) entity;
            outstring = "Kyuubi";
            gfHealth = healthRatio(e16);
        }
        if (entity instanceof Robot2) {
            final Robot2 e17 = (Robot2) entity;
            outstring = "Robo-Pounder";
            gfHealth = healthRatio(e17);
        }
        if (entity instanceof Robot4) {
            final Robot4 e18 = (Robot4) entity;
            outstring = "Robo-Warrior";
            gfHealth = getterRatio(e18.getRobot4Health(), e18);
        }
        if (entity instanceof Triffid) {
            final Triffid e19 = (Triffid) entity;
            outstring = "Triffid";
            gfHealth = healthRatio(e19);
        }
        if (entity instanceof Godzilla) {
            final Godzilla e20 = (Godzilla) entity;
            outstring = "Mobzilla";
            gfHealth = healthRatio(e20);
        }
        if (entity instanceof Vortex) {
            final Vortex e21 = (Vortex) entity;
            outstring = "Vortex";
            gfHealth = healthRatio(e21);
        }
        if (entity instanceof Irukandji) {
            final Irukandji e22 = (Irukandji) entity;
            outstring = "Irukandji";
            gfHealth = healthRatio(e22);
        }
        if (entity instanceof Mantis) {
            final Mantis e23 = (Mantis) entity;
            outstring = "Mantis";
            gfHealth = healthRatio(e23);
        }
        if (entity instanceof HerculesBeetle) {
            final HerculesBeetle e24 = (HerculesBeetle) entity;
            outstring = "Hercules Beetle";
            gfHealth = healthRatio(e24);
        }
        if (entity instanceof TheKing) {
            final TheKing e25 = (TheKing) entity;
            outstring = "The King";
            gfHealth = healthRatio(e25);
        }
        if (entity instanceof TheQueen) {
            final TheQueen e26 = (TheQueen) entity;
            outstring = "The Queen";
            gfHealth = healthRatio(e26);
        }
        if (entity instanceof SeaViper) {
            final SeaViper e27 = (SeaViper) entity;
            outstring = "Sea Viper";
            gfHealth = healthRatio(e27);
        }
        if (entity instanceof SeaMonster) {
            final SeaMonster e28 = (SeaMonster) entity;
            outstring = "Sea Monster";
            gfHealth = healthRatio(e28);
        }
        if (entity instanceof Molenoid) {
            final Molenoid e29 = (Molenoid) entity;
            outstring = "Molenoid";
            gfHealth = healthRatio(e29);
        }
        if (entity instanceof CaterKiller) {
            final CaterKiller e30 = (CaterKiller) entity;
            outstring = "CaterKiller";
            gfHealth = healthRatio(e30);
        }
        if (entity instanceof Leon) {
            final Leon e31 = (Leon) entity;
            if (e31.hasCustomName()) {
                outstring = customName(e31);
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Leonopteryx";
            }
            gfHealth = healthRatio(e31);
        }
        if (entity instanceof Hammerhead) {
            final Hammerhead e32 = (Hammerhead) entity;
            outstring = "Hammerhead";
            gfHealth = healthRatio(e32);
        }
        if (entity instanceof BandP) {
            final BandP e33 = (BandP) entity;
            if (e33.getWhat() == 0) {
                outstring = "Banker";
            } else {
                outstring = "Politician";
            }
            gfHealth = healthRatio(e33);
        }
        if (entity instanceof SpiderRobot) {
            final SpiderRobot e34 = (SpiderRobot) entity;
            outstring = "Giant Robot Spider";
            gfHealth = healthRatio(e34);
        }
        if (entity instanceof GiantRobot) {
            final GiantRobot e35 = (GiantRobot) entity;
            outstring = "Jeffery";
            gfHealth = healthRatio(e35);
        }
        if (entity instanceof AntRobot) {
            final AntRobot e36 = (AntRobot) entity;
            outstring = "Giant Robot Red Ant";
            gfHealth = healthRatio(e36);
        }
        if (entity instanceof Crab) {
            final Crab e37 = (Crab) entity;
            final float myf = e37.getCrabScale();
            if (myf > 0.75f) {
                outstring = "Very Large Crab";
                gfHealth = healthRatio(e37);
            }
        }
        if (outstring == null) {
            return null;
        }
        return new GirlfriendOverlayTarget(outstring, gfHealth);
    }

    /**
     * {@code OreSpawnMain.getPointedAtEntity} (OreSpawnMain.java:5455-5497): the entity the look ray hits first within
     * {@code dist}, searched in the player's box stretched along the look and grown by 1.
     *
     * <p>PORT: {@code player.getPosition(1.0f)} returned {@code posX/posY/posZ}, and a 1.7.10 client player's
     * {@code posY} sat at eye height ({@code yOffset} 1.62), so the ray starts at {@link Entity#getEyePosition(float)}.
     * {@code canBeCollidedWith} is {@code isPickable}, {@code getCollisionBorderSize} {@code getPickRadius},
     * {@code calculateIntercept} {@link AABB#clip}.
     */
    @Nullable
    public static Entity getPointedAtEntity(final Level world, final Player player, final double dist) {
        Entity pointedAt = null;
        if (player != null && world != null) {
            final double d0 = dist;
            final double d2 = dist;
            final Vec3 vec3 = player.getEyePosition(1.0f);
            final Vec3 vec4 = player.getViewVector(1.0f);
            final Vec3 vec5 = vec3.add(vec4.x * d0, vec4.y * d0, vec4.z * d0);
            pointedAt = null;
            final float f1 = 1.0f;
            final List<Entity> list = world.getEntities(player,
                    player.getBoundingBox().expandTowards(vec4.x * d0, vec4.y * d0, vec4.z * d0).inflate(f1, f1, f1));
            double d3 = d2;
            for (int i = 0; i < list.size(); ++i) {
                final Entity entity = list.get(i);
                if (entity.isPickable()) {
                    final float f2 = entity.getPickRadius();
                    final AABB axisalignedbb = entity.getBoundingBox().inflate(f2, f2, f2);
                    final Optional<Vec3> movingobjectposition = axisalignedbb.clip(vec3, vec5);
                    if (axisalignedbb.contains(vec3)) {
                        if (0.0 < d3 || d3 == 0.0) {
                            pointedAt = entity;
                            d3 = 0.0;
                        }
                    } else if (movingobjectposition.isPresent()) {
                        final double d4 = vec3.distanceTo(movingobjectposition.get());
                        if (d4 < d3 || d3 == 0.0) {
                            if (entity == player.getVehicle() && !entity.canRiderInteract()) {
                                if (d3 == 0.0) {
                                    pointedAt = entity;
                                }
                            } else {
                                pointedAt = entity;
                                d3 = d4;
                            }
                        }
                    }
                }
            }
        }
        return pointedAt;
    }
}
