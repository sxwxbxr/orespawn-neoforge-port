package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.config.stats.WeaponStats;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.LegacyAiTick;
import com.swbr.orespawn.entity.arrow.BerthaHit;
import com.swbr.orespawn.entity.arrow.UltimateArrow;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.companion.Shoes;
import com.swbr.orespawn.entity.projectile.Acid;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.LaserBall;
import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.entity.rock.EntityThrownRock;
import com.swbr.orespawn.entity.vehicle.Elevator;
import com.swbr.orespawn.item.rock.ItemRock;
import com.swbr.orespawn.network.RiderControlMessage;
import com.swbr.orespawn.network.RiderControlMessageHandler;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Wave 4: every W04 entity type is constructible from its registry factory and survives 100 ticks
 * (R17, point 2), every dispenser behaviour fires its own projectile, and - the behaviour half - the
 * sampled projectiles fly and hit with their original damage and side effect, Girlfriend is tamed,
 * follows and throws Shoes, the hoverboard carries a rider whose fly-up payload raises the top
 * speed, a Bertha swing sends a BerthaHit, and {@code EntityAIMoveIndoors} leads to a home at night.
 *
 * <p>Damage is read from {@code LivingDamageEvent.Post}, which NeoForge fires once for every point
 * that actually lands: a second hit inside the invulnerability window only shows up with the part
 * that exceeds the first. The target dummy is an iron golem without goals - 100 health, no armor,
 * knockback resistance 1, and not an OreSpawn entity, so neither R4 nor R5 touch the numbers.
 *
 * <p>Flight is real flight: every projectile starts four to five blocks west of the dummy and has
 * to cross that gap with its own tick before it can hit.
 *
 * <p>An exception inside an entity tick is not a test failure here but a server crash
 * ({@code Level.guardEntityTick}); a green run therefore also means no tick threw.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public class W04GameTests {

    private static final String ARENA = "arena";
    private static final String ARENA_LARGE = "arena_large";
    /** Batch whose level time is set to midnight; batches run one after another, so no other test sees it. */
    private static final String NIGHT = "w04_night";
    /** Batch pinned to noon, so the negative case does not depend on how long earlier batches ran. */
    private static final String DAY = "w04_day";

    // ------------------------------------------------------------------ spawn and tick

    /**
     * The four living types - Girlfriend, Boyfriend, hoverboard, placed rock - spawn on the floor
     * (relative y = 1, so feet at y = 2) and are still alive after 100 ticks. They need their
     * attribute sets from {@code CommonSetup.onEntityAttributes}; without one the constructor's
     * {@code getAttribute(...)} is null.
     */
    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void livingW04EntitiesSpawnAndTickOneHundredTicks(GameTestHelper helper) {
        List<Entity> living = List.of(
                helper.spawn(ModEntities.GIRLFRIEND.get(), new BlockPos(2, 2, 2)),
                helper.spawn(ModEntities.BOYFRIEND.get(), new BlockPos(10, 2, 2)),
                helper.spawn(ModEntities.HOVERBOARD.get(), new BlockPos(2, 2, 10)),
                helper.spawn(ModEntities.ROCK.get(), new BlockPos(10, 2, 10)));
        helper.runAfterDelay(100, () -> {
            for (Entity e : living) {
                helper.assertTrue(e.isAlive(), e.getType().toShortString() + " did not survive 100 ticks");
            }
            helper.succeed();
        });
    }

    /**
     * The fifteen non-living types spawn five blocks above the floor with no motion, fall, hit the
     * floor or each other and run their impact code without an owner - the path a dispenser, a
     * reloaded chunk or {@code /summon} takes.
     */
    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void projectileW04EntitiesSpawnAndTickOneHundredTicks(GameTestHelper helper) {
        List<EntityType<?>> types = List.of(
                ModEntities.ULTIMATE_FISH_HOOK.get(), ModEntities.SUNSPOT_URCHIN.get(), ModEntities.WATER_BALL.get(),
                ModEntities.INK_SACK.get(), ModEntities.LASER_BALL.get(), ModEntities.ICE_BALL.get(),
                ModEntities.ACID.get(), ModEntities.DEAD_IRUKANDJI.get(), ModEntities.BERTHA_HIT.get(),
                ModEntities.ENTITY_THROWN_ROCK.get(), ModEntities.SHOES.get(), ModEntities.ULTIMATE_ARROW.get(),
                ModEntities.IRUKANDJI_ARROW.get(), ModEntities.BETTER_FIREBALL.get(), ModEntities.THUNDER_BOLT.get());
        for (int i = 0; i < types.size(); i++) {
            Entity spawned = helper.spawn(types.get(i), new BlockPos(2 + (i % 5) * 2, 7, 2 + (i / 5) * 4));
            helper.assertTrue(spawned != null, "spawn returned null for " + types.get(i).toShortString());
        }
        helper.runAfterDelay(100, helper::succeed);
    }

    // ------------------------------------------------------------------ dispensers

    /**
     * An upward dispenser holding 16 of {@code item} is powered by a redstone block; the scheduled
     * dispense (4 ticks) must put an entity of {@code type} into the arena.
     */
    private static void dispenses(GameTestHelper helper, Item item, EntityType<?> type) {
        BlockPos pos = new BlockPos(6, 2, 6);
        helper.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        DispenserBlockEntity dispenser = helper.getBlockEntity(pos);
        dispenser.setItem(0, new ItemStack(item, 16));
        helper.assertEntityNotPresent(type);
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> helper.assertEntityPresent(type));
    }

    /** OreSpawnMain.java:5415, {@code MyDispenserBehaviorArrow}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresIrukandjiArrow(GameTestHelper helper) {
        dispenses(helper, ModItems.IRUKANDJI_ARROW.get(), ModEntities.IRUKANDJI_ARROW.get());
    }

    /** :5416, {@code MyDispenserBehaviorWDCharge}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresWaterBall(GameTestHelper helper) {
        dispenses(helper, ModItems.WATER_BALL.get(), ModEntities.WATER_BALL.get());
    }

    /** :5417, {@code MyDispenserBehaviorSunspotUrchin}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresSunspotUrchin(GameTestHelper helper) {
        dispenses(helper, ModItems.SUNSPOT_URCHIN.get(), ModEntities.SUNSPOT_URCHIN.get());
    }

    /** :5418, {@code MyDispenserBehaviorAcid}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresAcid(GameTestHelper helper) {
        dispenses(helper, ModItems.ACID.get(), ModEntities.ACID.get());
    }

    /** :5419, {@code MyDispenserBehaviorIceball}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresIceBall(GameTestHelper helper) {
        dispenses(helper, ModItems.ICE_BALL.get(), ModEntities.ICE_BALL.get());
    }

    /** :5420, {@code MyDispenserBehaviorDeadIrukandji}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresDeadIrukandji(GameTestHelper helper) {
        dispenses(helper, ModItems.DEAD_IRUKANDJI.get(), ModEntities.DEAD_IRUKANDJI.get());
    }

    /** :5421, {@code MyDispenserBehaviorLaserball}. */
    @GameTest(template = ARENA)
    public static void dispenserFiresLaserBall(GameTestHelper helper) {
        dispenses(helper, ModItems.LASER_BALL.get(), ModEntities.LASER_BALL.get());
    }

    /** :5422-5433, {@code MyDispenserBehaviorRock} - one class for all twelve rocks, sampled with the small rock. */
    @GameTest(template = ARENA)
    public static void dispenserFiresThrownRock(GameTestHelper helper) {
        dispenses(helper, ModItems.ROCK_SMALL.get(), ModEntities.ENTITY_THROWN_ROCK.get());
    }

    /**
     * OreSpawnMain.java:5422-5433 registers the rock behaviour once per rock item, and
     * MyDispenserBehaviorRock.java:19-54 copies the item's type onto the rock before it spawns. Twelve
     * upward dispensers, one rock each, each powered by its own redstone block: every one of the
     * twelve types must fly, none as type 0.
     */
    @GameTest(template = ARENA, timeoutTicks = 60)
    public static void dispenserFiresEveryRockItemWithItsOwnType(GameTestHelper helper) {
        List<ItemRock> rocks = List.copyOf(ItemRock.all());
        if (rocks.size() != 12) {
            helper.fail("expected 12 rock items, found " + rocks.size());
        }
        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < rocks.size(); i++) {
            BlockPos pos = new BlockPos(i, 2, 6);
            helper.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
            DispenserBlockEntity dispenser = helper.getBlockEntity(pos);
            dispenser.setItem(0, new ItemStack(rocks.get(i), 1));
            expected.add(rocks.get(i).getRockType());
        }
        if (expected.size() != 12 || expected.contains(0)) {
            helper.fail("the rock items do not carry twelve distinct non-zero types: " + expected);
        }
        for (int i = 0; i < rocks.size(); i++) {
            helper.setBlock(new BlockPos(i, 2, 7), Blocks.REDSTONE_BLOCK);
        }
        Set<Integer> seen = ConcurrentHashMap.newKeySet();
        AABB air = helper.getBounds().inflate(4.0, 40.0, 4.0);
        helper.succeedWhen(() -> {
            for (EntityThrownRock rock : helper.getLevel().getEntitiesOfClass(EntityThrownRock.class, air)) {
                seen.add(rock.getRockType());
            }
            helper.assertTrue(!seen.contains(0), "a dispensed rock flew as type 0");
            helper.assertTrue(seen.containsAll(expected), "rock types seen in flight " + seen + ", expected " + expected);
        });
    }

    // ------------------------------------------------------------------ damage recording

    /** One {@code LivingDamageEvent.Post}: the source and the damage that actually landed. */
    private record Hit(DamageSource source, float amount) {
        @Override
        public String toString() {
            return this.source.getMsgId() + " " + this.amount + " from " + this.source.getDirectEntity();
        }
    }

    private static final Map<UUID, List<Hit>> HITS = new ConcurrentHashMap<>();
    private static final AtomicBoolean LISTENING = new AtomicBoolean();

    /** Starts recording the damage {@code target} takes; the listener is registered on first use. */
    private static List<Hit> record(LivingEntity target) {
        if (LISTENING.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDamageEvent.Post.class, W04GameTests::onDamagePost);
        }
        List<Hit> hits = new CopyOnWriteArrayList<>();
        HITS.put(target.getUUID(), hits);
        return hits;
    }

    private static void onDamagePost(LivingDamageEvent.Post event) {
        List<Hit> hits = HITS.get(event.getEntity().getUUID());
        if (hits != null) {
            hits.add(new Hit(event.getSource(), event.getNewDamage()));
        }
    }

    private static void forget(LivingEntity target) {
        HITS.remove(target.getUUID());
    }

    /** Waiting condition: at least one hit landed. */
    private static void assertHit(GameTestHelper helper, List<Hit> hits, String what) {
        if (hits.isEmpty()) {
            throw new GameTestAssertException(what + " has not hit its target yet");
        }
    }

    /** The first landed hit has this damage type, this amount and - where given - this direct entity. */
    private static void expectFirstHit(GameTestHelper helper, List<Hit> hits, ResourceKey<DamageType> type, float amount,
            @Nullable Entity direct, String what) {
        Hit first = hits.get(0);
        boolean ok = first.source().is(type) && Math.abs(first.amount() - amount) < 1.0e-3F
                && (direct == null || first.source().getDirectEntity() == direct);
        if (!ok) {
            helper.fail(what + ": first hit was " + first + ", expected " + type.location() + " " + amount + " from " + direct
                    + " (all hits: " + hits + ")");
        }
    }

    private static boolean anyHit(List<Hit> hits, java.util.function.Predicate<Hit> test) {
        return hits.stream().anyMatch(test);
    }

    private static boolean isExplosion(Hit hit) {
        return hit.source().is(DamageTypeTags.IS_EXPLOSION) && hit.amount() > 0.0F;
    }

    /** The projectile is gone and crossed at least {@code blocks} of the gap along +x under its own tick. */
    private static void expectFlew(GameTestHelper helper, Entity projectile, Vec3 start, double blocks, String what) {
        if (!projectile.isRemoved()) {
            helper.fail(what + " is still alive after its hit");
        }
        if (projectile.getX() - start.x < blocks) {
            helper.fail(what + " moved only " + (projectile.getX() - start.x) + " blocks before hitting, expected at least " + blocks);
        }
    }

    // ------------------------------------------------------------------ projectiles

    /** A still iron golem at relative (9, 2, 6): box x 8.8..10.2, y 2..4.7. */
    private static IronGolem dummy(GameTestHelper helper) {
        return helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(9, 2, 6));
    }

    /** Launch point five blocks west of the dummy's centre, at its middle height. */
    private static Vec3 launchPoint(GameTestHelper helper) {
        return helper.absoluteVec(new Vec3(4.5, 3.3, 6.5));
    }

    /** Straight east at speed 1 without spread ({@code setThrowableHeading}, inaccuracy 0). */
    private static <T extends LegacyThrowable> T launchEast(GameTestHelper helper, T projectile) {
        projectile.setThrowableHeading(1.0, 0.0, 0.0, 1.0F, 0.0F);
        helper.getLevel().addFreshEntity(projectile);
        return projectile;
    }

    /**
     * LaserBall.java:95-177 without flags: 16 thrown damage, one second of fire, no explosion (only
     * {@code is_special} or {@code is_iceball} explode).
     */
    @GameTest(template = ARENA)
    public static void laserBallFliesAndHitsForSixteenWithFire(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Vec3 start = launchPoint(helper);
        LaserBall ball = launchEast(helper, new LaserBall(helper.getLevel(), start.x, start.y, start.z));
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "laser_ball"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.THROWN, 16.0F, ball, "laser_ball");
                    expectFlew(helper, ball, start, 3.0, "laser_ball");
                    helper.assertTrue(golem.getRemainingFireTicks() > 0, "laser_ball did not set its target on fire");
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertFalse(anyHit(hits, W04GameTests::isExplosion), "laser_ball exploded: " + hits);
                    forget(golem);
                })
                .thenSucceed();
    }

    /** Acid.java:13 sets the acid flag: 16 thrown damage and one second of fire, no explosion. */
    @GameTest(template = ARENA)
    public static void acidFliesAndHitsForSixteenWithFireAndNoExplosion(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Vec3 start = launchPoint(helper);
        Acid acid = launchEast(helper, new Acid(helper.getLevel(), start.x, start.y, start.z));
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "acid"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.THROWN, 16.0F, acid, "acid");
                    expectFlew(helper, acid, start, 3.0, "acid");
                    helper.assertTrue(golem.getRemainingFireTicks() > 0, "acid did not set its target on fire");
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertFalse(anyHit(hits, W04GameTests::isExplosion), "acid exploded: " + hits);
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * IceBall = LaserBall with the ice flag (LaserBall.java:121-123, :142-145): 16 thrown damage, no
     * fire, then the strength-3 explosion, whose excess over 16 lands inside the invulnerability window.
     */
    @GameTest(template = ARENA)
    public static void iceBallFliesAndHitsForSixteenWithoutFireThenExplodes(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Vec3 start = launchPoint(helper);
        IceBall ball = launchEast(helper, new IceBall(helper.getLevel(), start.x, start.y, start.z));
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "ice_ball"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.THROWN, 16.0F, ball, "ice_ball");
                    expectFlew(helper, ball, start, 3.0, "ice_ball");
                    helper.assertTrue(golem.getRemainingFireTicks() <= 0, "ice_ball set its target on fire");
                    helper.assertTrue(anyHit(hits, W04GameTests::isExplosion), "ice_ball did not explode: " + hits);
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * ThunderBolt.java:27-49: 20 thrown damage, then 20 mob damage in the same tick - inside the
     * invulnerability window, equal to the first, so it adds nothing (catalogue 6.4) - one second of
     * fire, the strength-3 explosion and a real lightning bolt.
     */
    @GameTest(template = ARENA)
    public static void thunderBoltHitsForTwentyExplodesAndCallsLightning(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Vec3 start = launchPoint(helper);
        ThunderBolt bolt = launchEast(helper, new ThunderBolt(helper.getLevel(), start.x, start.y, start.z));
        AABB area = helper.getBounds().inflate(2.0);
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "thunder_bolt"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.THROWN, 20.0F, bolt, "thunder_bolt");
                    expectFlew(helper, bolt, start, 3.0, "thunder_bolt");
                    helper.assertFalse(anyHit(hits, h -> h.source().is(DamageTypes.MOB_ATTACK) && h.amount() > 0.0F),
                            "the second, equal mob-damage hit landed on top of the first: " + hits);
                    helper.assertTrue(golem.getRemainingFireTicks() > 0, "thunder_bolt did not set its target on fire");
                    helper.assertTrue(anyHit(hits, W04GameTests::isExplosion), "thunder_bolt did not explode: " + hits);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        !helper.getLevel().getEntitiesOfClass(LightningBolt.class, area).isEmpty(), "no lightning bolt"))
                .thenExecute(() -> forget(golem))
                .thenSucceed();
    }

    /**
     * LaserBall.java:173 and ThunderBolt.java:45 explode through {@code createExplosion(this, ...)}. For an
     * exploder that is neither living nor primed TNT, 1.7.10 {@code Explosion.getExplosivePlacedBy} is null,
     * so the blast is a plain {@code "explosion"} even when a player threw the ball. The projectile gets a
     * player owner here: its direct hit is credited to him, the blast must carry no entity at all.
     */
    private static void explodesWithoutCreditingTheThrower(GameTestHelper helper, LegacyThrowable projectile, String what) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Player thrower = helper.makeMockPlayer(GameType.SURVIVAL);
        projectile.setOwner(thrower);
        launchEast(helper, projectile);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(anyHit(hits, W04GameTests::isExplosion), what + " has not exploded yet: " + hits))
                .thenExecute(() -> {
                    helper.assertTrue(hits.get(0).source().getEntity() == thrower,
                            what + ": the direct hit is not credited to the thrower: " + hits);
                    for (Hit hit : hits) {
                        if (hit.source().is(DamageTypeTags.IS_EXPLOSION)) {
                            helper.assertTrue(hit.source().is(DamageTypes.EXPLOSION) && hit.source().getEntity() == null
                                            && hit.source().getDirectEntity() == null,
                                    what + ": the blast was " + hit + " credited to " + hit.source().getEntity() + ", expected a plain explosion");
                        }
                    }
                    forget(golem);
                })
                .thenSucceed();
    }

    /** IceBall (LaserBall with the ice flag, LaserBall.java:142-145) thrown by a player. */
    @GameTest(template = ARENA)
    public static void ownedIceBallExplodesWithoutCreditingTheThrower(GameTestHelper helper) {
        Vec3 start = launchPoint(helper);
        explodesWithoutCreditingTheThrower(helper, new IceBall(helper.getLevel(), start.x, start.y, start.z), "ice_ball");
    }

    /** ThunderBolt (ThunderBolt.java:45) fired by a player. */
    @GameTest(template = ARENA)
    public static void ownedThunderBoltExplodesWithoutCreditingTheThrower(GameTestHelper helper) {
        Vec3 start = launchPoint(helper);
        explodesWithoutCreditingTheThrower(helper, new ThunderBolt(helper.getLevel(), start.x, start.y, start.z), "thunder_bolt");
    }

    /**
     * BetterFireball.java:216-284 without a shooter and not small: 10 fireball damage
     * ({@code causeFireballDamage(this, null)} = unattributed) and five seconds of fire. The ball starts
     * at rest and has to accelerate itself (0.1 per pass, twice a tick) across the gap.
     */
    @GameTest(template = ARENA)
    public static void betterFireballAcceleratesAndHitsForTenWithFiveSecondsOfFire(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Vec3 start = launchPoint(helper);
        BetterFireball ball = new BetterFireball(ModEntities.BETTER_FIREBALL.get(), helper.getLevel());
        ball.setPos(start.x, start.y, start.z);
        ball.setAcceleration(0.1, 0.0, 0.0);
        helper.getLevel().addFreshEntity(ball);
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "better_fireball"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.UNATTRIBUTED_FIREBALL, 10.0F, ball, "better_fireball");
                    expectFlew(helper, ball, start, 3.0, "better_fireball");
                    helper.assertTrue(golem.getRemainingFireTicks() > 60,
                            "better_fireball set " + golem.getRemainingFireTicks() + " fire ticks, expected about 100");
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * EntityThrownRock.java:127-139, type 5 thrown by a player: 10 player-attack damage and Slowness
     * for 100 ticks. The rock leaves the thrower's eyes along his view (yaw -90 = east).
     */
    @GameTest(template = ARENA)
    public static void thrownRockHitsWithItsTypeDamageAndEffect(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Player thrower = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 feet = helper.absoluteVec(new Vec3(4.5, 2.0, 6.5));
        thrower.moveTo(feet.x, feet.y, feet.z, -90.0F, 0.0F);
        EntityThrownRock rock = new EntityThrownRock(helper.getLevel(), thrower, 5);
        Vec3 start = rock.position();
        helper.getLevel().addFreshEntity(rock);
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "entity_thrown_rock"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.PLAYER_ATTACK, 10.0F, thrower, "entity_thrown_rock type 5");
                    expectFlew(helper, rock, start, 2.5, "entity_thrown_rock");
                    MobEffectInstance slow = golem.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    helper.assertTrue(slow != null && slow.getAmplifier() == 0 && slow.getDuration() > 90 && slow.getDuration() <= 100,
                            "type 5 rock gave " + slow + ", expected Slowness I for 100 ticks");
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * EntityThrownRock.java:302 runs the 1.7.10 {@code EntityThrowable.onUpdate}, which traced blocks with the
     * selection box: short grass stops a rock and runs the block branch (:235-294), so the small rock's item
     * drops. A rock falls straight down from relative y 4.5 onto grass on the floor (floor top 2.0, grass
     * outline up to 2.8125): the second tick's trace (3.5 to 2.48) reaches the outline. A collision-shape
     * trace passes the grass and reaches the floor only in the third tick. The removal tick is the test,
     * not the final position - {@code zk.h} still moves a projectile after {@code onImpact} killed it.
     */
    @GameTest(template = ARENA)
    public static void thrownRockStopsOnShortGrassAndDropsItsItem(GameTestHelper helper) {
        BlockPos grass = new BlockPos(6, 2, 6);
        helper.setBlock(grass.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(grass, Blocks.SHORT_GRASS);
        Vec3 start = helper.absoluteVec(new Vec3(6.5, 4.5, 6.5));
        EntityThrownRock rock = new EntityThrownRock(helper.getLevel(), start.x, start.y, start.z);
        rock.setRockType(1);
        rock.setThrowableHeading(0.0, -1.0, 0.0, 1.0F, 0.0F);
        helper.getLevel().addFreshEntity(rock);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(rock.isRemoved(), "entity_thrown_rock still flying"))
                .thenExecute(() -> {
                    helper.assertTrue(rock.tickCount == 2, "the rock was removed in tick " + rock.tickCount
                            + ", expected 2 (grass outline reached); 3 means it flew through the grass to the floor");
                    helper.assertBlockPresent(Blocks.SHORT_GRASS, grass);
                    helper.assertItemEntityPresent(ModItems.ROCK_SMALL.get(), grass, 2.0);
                })
                .thenSucceed();
    }

    /**
     * Girlfriend.java:983-1001 with the Ultimate Bow: an UltimateArrow at speed 2.0 whose hit deals
     * {@code ceil(speed * UltimateBowDamage)} (UltimateArrow.java:187-188). The flight speed at the
     * hit is just under 2 after the drag of the crossing, so the damage is {@code ceil(1.9 * d)} to
     * {@code ceil(2 * d)}. The 1-in-4 crit roll of the shot adds a random bonus and is switched off.
     */
    @GameTest(template = ARENA)
    public static void girlfriendShootsUltimateArrowForBowDamage(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Girlfriend girlfriend = helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(3, 2, 6));
        ItemStack bow = new ItemStack(ModItems.ULTIMATE_BOW.get());
        girlfriend.setItemSlot(EquipmentSlot.MAINHAND, bow);
        girlfriend.performRangedAttack(golem, 1.0F);
        List<UltimateArrow> arrows = helper.getLevel().getEntitiesOfClass(UltimateArrow.class, helper.getBounds().inflate(2.0),
                a -> a.getOwner() == girlfriend);
        if (arrows.size() != 1) {
            helper.fail("Girlfriend with the Ultimate Bow shot " + arrows.size() + " UltimateArrows, expected 1");
        }
        UltimateArrow arrow = arrows.get(0);
        arrow.setCritArrow(false);
        Vec3 start = arrow.position();
        if (bow.getDamageValue() != 1) {
            helper.fail("the shot cost the bow " + bow.getDamageValue() + " durability, expected 1");
        }
        int bowDamage = TweakStats.UltimateBowDamage();
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "ultimate_arrow"))
                .thenExecute(() -> {
                    Hit first = hits.get(0);
                    helper.assertTrue(first.source().is(DamageTypes.ARROW) && first.source().getDirectEntity() == arrow
                            && first.source().getEntity() == girlfriend, "ultimate_arrow first hit was " + first);
                    helper.assertTrue(first.amount() >= Mth.ceil(1.9 * bowDamage) && first.amount() <= Mth.ceil(2.0 * bowDamage),
                            "ultimate_arrow dealt " + first.amount() + ", expected ceil(speed * " + bowDamage + ") for speed 1.9..2.0");
                    expectFlew(helper, arrow, start, 3.0, "ultimate_arrow");
                    helper.assertTrue(golem.getArrowCount() == 1, "the hit mob counts " + golem.getArrowCount() + " arrows, expected 1");
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * UltimateArrow.java:189-205 with {@code UltimateSwordPvp = 0} (the default): a tamed animal is
     * spared, the arrow heals it by 1 and disappears.
     */
    @GameTest(template = ARENA)
    public static void ultimateArrowSparesATamedAnimalAndHealsIt(GameTestHelper helper) {
        if (com.swbr.orespawn.config.OreSpawnConfig.TWEAKS.UltimateSwordPvp.get() != 0) {
            helper.fail("precondition: UltimateSwordPvp must be 0 (the default)");
        }
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(9, 2, 6));
        wolf.setTame(true, false);
        wolf.setHealth(4.0F);
        List<Hit> hits = record(wolf);
        Vec3 start = helper.absoluteVec(new Vec3(4.5, 2.4, 6.5));
        UltimateArrow arrow = new UltimateArrow(helper.getLevel(), start.x, start.y, start.z);
        arrow.shoot(1.0, 0.0, 0.0, 2.0F, 0.0F);
        helper.getLevel().addFreshEntity(arrow);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(arrow.isRemoved(), "ultimate_arrow still flying"))
                .thenExecute(() -> {
                    helper.assertTrue(hits.isEmpty(), "the tamed wolf took damage: " + hits);
                    helper.assertTrue(Math.abs(wolf.getHealth() - 5.0F) < 1.0e-3F,
                            "the tamed wolf has " + wolf.getHealth() + " health, expected 4 + 1");
                    forget(wolf);
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ Bertha

    /**
     * Bertha.java:69-92: swinging Big Bertha puts a BerthaHit two blocks ahead of the player and costs
     * one durability; BerthaHit.java:55-111 type 0 then hits within 9 blocks for
     * {@code bertha_stats.damage} as player damage and sets 10 seconds of fire. The swing is the
     * NeoForge {@code onEntitySwing} hook inside {@code LivingEntity.swing}.
     */
    @GameTest(template = ARENA)
    public static void berthaSwingSendsABerthaHitThatStrikesForBerthaDamage(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 feet = helper.absoluteVec(new Vec3(3.5, 2.0, 6.5));
        player.moveTo(feet.x, feet.y, feet.z, -90.0F, 0.0F);
        player.setYHeadRot(-90.0F);
        ItemStack bertha = new ItemStack(ModItems.BERTHA.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bertha);
        AABB area = helper.getBounds().inflate(2.0);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(BerthaHit.class, area).isEmpty(), "precondition: no BerthaHit");
        player.swing(InteractionHand.MAIN_HAND);
        List<BerthaHit> sent = helper.getLevel().getEntitiesOfClass(BerthaHit.class, area, b -> b.getOwner() == player);
        if (sent.size() != 1) {
            helper.fail("the swing sent " + sent.size() + " BerthaHits, expected 1");
        }
        BerthaHit hit = sent.get(0);
        Vec3 ahead = hit.position().subtract(player.position());
        if (Math.abs(ahead.x - 2.0) > 1.0e-3 || Math.abs(ahead.y - 1.55) > 1.0e-3 || Math.abs(ahead.z) > 1.0e-3) {
            helper.fail("BerthaHit spawned at " + ahead + " from the player, expected (2, 1.55, 0)");
        }
        if (bertha.getDamageValue() != 1) {
            helper.fail("the swing cost Big Bertha " + bertha.getDamageValue() + " durability, expected 1");
        }
        int damage = WeaponStats.bertha_stats().damage();
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "bertha_hit"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.PLAYER_ATTACK, (float) damage, player, "bertha_hit type 0");
                    helper.assertTrue(hit.isRemoved(), "bertha_hit still alive after its hit");
                    helper.assertTrue(golem.getRemainingFireTicks() > 0, "bertha_hit did not set its target on fire");
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * BerthaHit.java:10 runs the 1.7.10 {@code EntityThrowable.onUpdate}, whose {@code calculateIntercept}
     * ({@code azt.a}) also hits a target whose grown box already holds the start point. The player stands
     * 2.5 blocks west of the dummy's centre, so the swing puts the hit at x 9.0, inside the dummy (box x
     * 8.8..10.2): a mob in melee range must still take the Big Bertha hit.
     */
    @GameTest(template = ARENA)
    public static void berthaHitStrikesAMobStandingInMeleeRange(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 feet = helper.absoluteVec(new Vec3(7.0, 2.0, 6.5));
        player.moveTo(feet.x, feet.y, feet.z, -90.0F, 0.0F);
        player.setYHeadRot(-90.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BERTHA.get()));
        player.swing(InteractionHand.MAIN_HAND);
        int damage = WeaponStats.bertha_stats().damage();
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "bertha_hit in melee range"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.PLAYER_ATTACK, (float) damage, player, "bertha_hit in melee range");
                    forget(golem);
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ Girlfriend

    /**
     * Girlfriend.java:603-627: a red flower tames her with a one-in-three chance per click, makes the
     * clicking player her owner and heals her to full; MyEntityAIFollowOwner (priority 1) then brings
     * her to that owner once he is more than 6 blocks away (half of 12 below y = 60, which is where the
     * test world lies). The owner has to be a player in the level: {@code getOwner()} looks the UUID up
     * in the level's player list.
     */
    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void girlfriendIsTamedWithRedFlowersAndFollowsHerOwner(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawn(ModEntities.GIRLFRIEND.get(), new BlockPos(1, 2, 1));
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        Vec3 near = helper.absoluteVec(new Vec3(2.5, 2.0, 1.5));
        owner.moveTo(near.x, near.y, near.z, 0.0F, 0.0F);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY, 64));
        girlfriend.setHealth(40.0F);
        helper.assertFalse(girlfriend.isTame(), "precondition: a fresh Girlfriend is wild");
        int clicks = 0;
        while (!girlfriend.isTame() && clicks < 64) {
            owner.interactOn(girlfriend, InteractionHand.MAIN_HAND);
            clicks++;
        }
        if (!girlfriend.isTame() || girlfriend.getOwner() != owner) {
            leave(helper, owner);
            helper.fail("64 red flowers did not tame her to the clicking player (tame=" + girlfriend.isTame()
                    + ", owner=" + girlfriend.getOwner() + ")");
        }
        if (Math.abs(girlfriend.getHealth() - girlfriend.getMaxHealth()) > 1.0e-3F) {
            leave(helper, owner);
            helper.fail("taming healed her to " + girlfriend.getHealth() + " of " + girlfriend.getMaxHealth());
        }
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Vec3 far = helper.absoluteVec(new Vec3(11.5, 2.0, 11.5));
        owner.moveTo(far.x, far.y, far.z, 0.0F, 0.0F);
        double startDistance = girlfriend.distanceTo(owner);
        if (startDistance < 12.0) {
            leave(helper, owner);
            helper.fail("precondition: owner only " + startDistance + " blocks away");
        }
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(girlfriend.distanceTo(owner) < 4.0,
                        "Girlfriend is still " + girlfriend.distanceTo(owner) + " blocks from her owner"))
                .thenExecute(() -> leave(helper, owner))
                .thenSucceed();
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /**
     * Girlfriend.java:1002-1016 with no bow: the ranged attack throws a Shoes projectile with a random
     * shoe id 2..5 at velocity 1.8, which hits for 2 thrown damage (10 on Valentine's Day,
     * Shoes.java:59-89). {@code performRangedAttack} is what her RangedAttackGoal calls.
     */
    @GameTest(template = ARENA)
    public static void girlfriendThrowsShoesThatHitForTwo(GameTestHelper helper) {
        IronGolem golem = dummy(helper);
        List<Hit> hits = record(golem);
        Girlfriend girlfriend = helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(3, 2, 6));
        girlfriend.performRangedAttack(golem, 1.0F);
        List<Shoes> shoes = helper.getLevel().getEntitiesOfClass(Shoes.class, helper.getBounds().inflate(2.0),
                s -> s.getOwner() == girlfriend);
        if (shoes.size() != 1) {
            helper.fail("the ranged attack threw " + shoes.size() + " shoes, expected 1");
        }
        Shoes shoe = shoes.get(0);
        if (shoe.getShoeId() < 2 || shoe.getShoeId() > 5) {
            helper.fail("thrown shoe has id " + shoe.getShoeId() + ", expected 2..5");
        }
        Vec3 start = shoe.position();
        float expected = OreSpawn.valentines_day != 0 ? 10.0F : 2.0F;
        helper.startSequence()
                .thenWaitUntil(() -> assertHit(helper, hits, "shoes"))
                .thenExecute(() -> {
                    expectFirstHit(helper, hits, DamageTypes.THROWN, expected, shoe, "shoes");
                    helper.assertTrue(hits.get(0).source().getEntity() == girlfriend, "the shoe's damage is not credited to her");
                    expectFlew(helper, shoe, start, 2.5, "shoes");
                    forget(golem);
                })
                .thenSucceed();
    }

    /**
     * Girlfriend.java:154 and Boyfriend.java:128 set {@code experienceValue = 0}, but 1.7.10
     * {@code EntityAnimal.getExperiencePoints} ({@code wf.e}) ignores that field and returns
     * {@code 1 + rand.nextInt(3)}; neither class overrides it. 200 draws each: all in 1..3, all three seen
     * (missing one by chance has a probability of about 1e-35).
     */
    @GameTest(template = ARENA)
    public static void girlfriendAndBoyfriendGiveOneToThreeExperienceLikeAnyAnimal(GameTestHelper helper) {
        List<Mob> companions = List.of(
                helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(2, 2, 2)),
                helper.spawnWithNoFreeWill(ModEntities.BOYFRIEND.get(), new BlockPos(10, 2, 2)));
        for (Mob mob : companions) {
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < 200; i++) {
                int xp = mob.getExperienceReward(helper.getLevel(), null);
                if (xp < 1 || xp > 3) {
                    helper.fail(mob.getType().toShortString() + " gives " + xp + " experience, expected 1..3");
                }
                seen.add(xp);
            }
            helper.assertTrue(seen.size() == 3, mob.getType().toShortString() + " gave only " + seen + " in 200 draws");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ EntityAIMoveIndoors

    private static long savedDayTime;

    @BeforeBatch(batch = NIGHT)
    public static void startNight(ServerLevel level) {
        savedDayTime = level.getDayTime();
        level.setDayTime(18000L);
    }

    @AfterBatch(batch = NIGHT)
    public static void endNight(ServerLevel level) {
        level.setDayTime(savedDayTime);
    }

    @BeforeBatch(batch = DAY)
    public static void startDay(ServerLevel level) {
        savedDayTime = level.getDayTime();
        level.setDayTime(6000L);
    }

    @AfterBatch(batch = DAY)
    public static void endDay(ServerLevel level) {
        level.setDayTime(savedDayTime);
    }

    /**
     * Asks the goal up to {@code evaluations} times, one 1.7.10 {@code shouldExecute} each: the mob's
     * tick counter is moved to the next multiple of three before every call, so {@link LegacyAiTick}
     * grants exactly one roll per call. 2000 evaluations of a 1-in-50 chance all failing has a
     * probability of about 1e-18.
     */
    private static boolean evaluateUntilStarted(Mob mob, Goal goal, int evaluations) {
        mob.tickCount = (mob.tickCount / LegacyAiTick.TICK_RATE + 1) * LegacyAiTick.TICK_RATE;
        for (int n = 0; n < evaluations; ++n) {
            mob.tickCount += LegacyAiTick.TICK_RATE;
            if (goal.canUse()) {
                return true;
            }
        }
        return false;
    }

    private static void placeBed(GameTestHelper helper, BlockPos head) {
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        helper.setBlock(head.south(), bed.setValue(BedBlock.PART, BedPart.FOOT));
        helper.setBlock(head, bed.setValue(BedBlock.PART, BedPart.HEAD));
    }

    /** Mob.goalSelector is protected; reflection reads the goals a constructor added. */
    private static GoalSelector goalSelector(Mob mob) {
        try {
            Field field = Mob.class.getDeclaredField("goalSelector");
            field.setAccessible(true);
            return (GoalSelector) field.get(mob);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new GameTestAssertException("cannot read Mob.goalSelector: " + e);
        }
    }

    /** Girlfriend.java:139 and Boyfriend.java:115: {@code tasks.addTask(11, new EntityAIMoveIndoors(this))}, mutex MOVE. */
    @GameTest(template = ARENA)
    public static void girlfriendAndBoyfriendCarryMoveIndoorsAtPriorityEleven(GameTestHelper helper) {
        List<Mob> companions = List.of(
                helper.spawn(ModEntities.GIRLFRIEND.get(), new BlockPos(2, 2, 2)),
                helper.spawn(ModEntities.BOYFRIEND.get(), new BlockPos(10, 2, 2)));
        for (Mob mob : companions) {
            List<WrappedGoal> indoors = goalSelector(mob).getAvailableGoals().stream()
                    .filter(w -> w.getGoal() instanceof EntityAIMoveIndoors).toList();
            if (indoors.size() != 1 || indoors.get(0).getPriority() != 11
                    || !indoors.get(0).getGoal().getFlags().equals(java.util.EnumSet.of(Goal.Flag.MOVE))) {
                helper.fail(mob.getType().toShortString() + " carries " + indoors.size()
                        + " EntityAIMoveIndoors goals" + (indoors.isEmpty() ? "" : " at priority " + indoors.get(0).getPriority()
                        + " with flags " + indoors.get(0).getGoal().getFlags()) + ", expected one at 11 with MOVE");
            }
        }
        helper.succeed();
    }

    /**
     * EntityAIMoveIndoors at night ({@code ur.a/c/d}): without a home in range it never starts; with a
     * bed eight blocks away it starts and walks straight to the bed (within 16 blocks, no random
     * detour); once it has reached that inside position it does not start again while the mob stands
     * within 2 blocks of it, and does again from farther away.
     */
    @GameTest(template = ARENA, batch = NIGHT, timeoutTicks = 60)
    public static void moveIndoorsLeadsToTheNearestBedAtNight(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(2, 2, 6));
        EntityAIMoveIndoors goal = new EntityAIMoveIndoors(girlfriend);
        BlockPos head = new BlockPos(10, 2, 6);
        BlockPos bed = helper.absolutePos(head);
        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(helper.getLevel().getSkyDarken() >= 4,
                            "precondition: the night batch left sky darken at " + helper.getLevel().getSkyDarken());
                    helper.assertFalse(evaluateUntilStarted(girlfriend, goal, 2000), "the goal started with no home in range");
                    placeBed(helper, head);
                })
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(helper.getLevel().getPoiManager()
                            .findClosest(h -> h.is(PoiTypes.HOME), bed, 1, PoiManager.Occupancy.ANY).isPresent(),
                            "precondition: the bed head is not a minecraft:home POI");
                    helper.assertTrue(evaluateUntilStarted(girlfriend, goal, 2000), "the goal never started with a bed 8 blocks away");
                    goal.start();
                    BlockPos target = girlfriend.getNavigation().getTargetPos();
                    helper.assertTrue(target != null && target.distManhattan(bed) <= 1,
                            "the goal walks to " + target + ", expected the bed at " + bed);
                    helper.assertTrue(goal.canContinueToUse(), "the goal stops although it has a path");
                    goal.stop();
                    girlfriend.moveTo(bed.getX() - 0.5, girlfriend.getY(), bed.getZ() + 0.5, 0.0F, 0.0F);
                    helper.assertFalse(evaluateUntilStarted(girlfriend, goal, 2000),
                            "the goal started again within 2 blocks of the inside position it reached");
                    Vec3 away = helper.absoluteVec(new Vec3(2.5, 2.0, 6.5));
                    girlfriend.moveTo(away.x, away.y, away.z, 0.0F, 0.0F);
                    helper.assertTrue(evaluateUntilStarted(girlfriend, goal, 2000),
                            "the goal did not start again 8 blocks from the inside position");
                })
                .thenSucceed();
    }

    /** By day, under a clear sky, in a biome that rains, the goal never starts - bed or not. */
    @GameTest(template = ARENA, batch = DAY, timeoutTicks = 60)
    public static void moveIndoorsStaysOutsideByDay(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(2, 2, 6));
        EntityAIMoveIndoors goal = new EntityAIMoveIndoors(girlfriend);
        placeBed(helper, new BlockPos(10, 2, 6));
        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(helper.getLevel().getSkyDarken() < 4 && !helper.getLevel().isRaining(),
                            "precondition: the day batch is not a clear day (sky darken " + helper.getLevel().getSkyDarken() + ")");
                    helper.assertFalse(evaluateUntilStarted(girlfriend, goal, 2000), "the goal started by day under a clear sky");
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ hoverboard

    /** A server-bound payload context for {@link RiderControlMessageHandler}: work runs at once. */
    private static IPayloadContext serverbound(Player sender) {
        return new IPayloadContext() {
            @Override
            public ICommonPacketListener listener() {
                throw new UnsupportedOperationException("no connection in a GameTest");
            }

            @Override
            public Player player() {
                return sender;
            }

            @Override
            public CompletableFuture<Void> enqueueWork(Runnable task) {
                task.run();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
                return CompletableFuture.completedFuture(task.get());
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.SERVERBOUND;
            }

            @Override
            public void handle(CustomPacketPayload payload) {
                throw new UnsupportedOperationException("no connection in a GameTest");
            }

            @Override
            public void finishCurrentTask(ConfigurationTask.Type type) {
                throw new UnsupportedOperationException("no configuration phase in a GameTest");
            }
        };
    }

    private static Player mount(GameTestHelper helper, Elevator board) {
        Player rider = helper.makeMockPlayer(GameType.SURVIVAL);
        rider.moveTo(board.getX(), board.getY(), board.getZ(), 0.0F, 0.0F);
        if (!rider.startRiding(board) || board.getFirstPassenger() != rider) {
            helper.fail("the mock player could not mount the hoverboard");
        }
        rider.zza = 1.0F; // moveForward: the forward key held
        return rider;
    }

    /**
     * Elevator.java:232-527 on the server. An empty board hovers above the floor and stays where it
     * is. A ridden board with the forward key held moves along the rider's yaw (0 = +z) and never goes
     * faster than 0.85 a tick; after the {@code RiderControlMessage(1)} payload - encoded and decoded
     * through its stream codec, then handled by {@link RiderControlMessageHandler} - its rider is in
     * fly-up state, the top speed is 1.85 and the acceleration 0.175 a tick (:444-470). Speed means
     * lift here too: the obstruction scan reaches {@code 3 + 8 * speed} blocks down (:388-402), so the
     * boosted board climbs higher. The rider sits on the board's own Y (:529-533).
     *
     * <p>Speed under the 0.98 drag of :482 grows as {@code (deltav / 0.02) * (1 - 0.98^n)}: the plain
     * board (0.025 a tick) passes 0.8 after 51 ticks and reaches its 0.85 cap after about 57, the
     * boosted one (0.175) passes 1.5 after 10 and reaches 1.85 after about 12.
     *
     * <p>W08: the windows count <b>board</b> ticks ({@code Entity.tickCount}), not test ticks - 20 for
     * the boosted board, 80 for the plain one. Two logged runs (W07 open point 1, W08.md "W07-Nachzug")
     * failed with exactly the step after 48 and 49 accelerating ticks; a window in test ticks cannot
     * tell a board that did not run from a board that stopped accelerating. The failure messages name
     * the ticks run, the accelerating ticks (rider on the board, no engine explosion), the tick of the
     * last new maximum and both states. The engine explosion is 1:1 (:303-326,
     * {@code worldObj.rand.nextInt(20000) == 1} above speed 0.65, then 0.05 less per tick for 45 ticks)
     * and legitimately caps the top speed; if it fired, the board must have reached the speed of its
     * accelerating ticks before it instead of 0.8.
     */
    @GameTest(template = ARENA_LARGE, timeoutTicks = 240)
    public static void hoverboardCarriesItsRiderAndTheFlyUpPayloadRaisesTheTopSpeed(GameTestHelper helper) {
        Elevator parked = helper.spawn(ModEntities.HOVERBOARD.get(), new BlockPos(24, 2, 60));
        Elevator plain = helper.spawn(ModEntities.HOVERBOARD.get(), new BlockPos(12, 2, 4));
        Elevator boosted = helper.spawn(ModEntities.HOVERBOARD.get(), new BlockPos(36, 2, 4));
        Player plainRider = mount(helper, plain);
        Player boostedRider = mount(helper, boosted);

        ByteBuf wire = Unpooled.buffer();
        RiderControlMessage.STREAM_CODEC.encode(wire, new RiderControlMessage(1));
        RiderControlMessage received = RiderControlMessage.STREAM_CODEC.decode(wire);
        new RiderControlMessageHandler().handle(received, serverbound(boostedRider));
        if (!RiderKeys.isFlyUp(boostedRider) || RiderKeys.isFlyUp(plainRider)) {
            helper.fail("the payload set fly-up on the wrong rider: sender " + RiderKeys.isFlyUp(boostedRider)
                    + ", other " + RiderKeys.isFlyUp(plainRider));
        }

        Vec3 parkedStart = parked.position();
        Vec3 plainStart = plain.position();
        Vec3 boostedStart = boosted.position();
        double[] last = {plain.getX(), plain.getZ(), boosted.getX(), boosted.getZ()};
        double[] maxStep = {0.0, 0.0};
        double[] maxRelY = {0.0, 0.0, 0.0};
        // plain: last seen tickCount, ticks run, accelerating ticks, ticks run at the last new maximum
        int[] plainTicks = {plain.tickCount, 0, 0, 0};
        // boosted: last seen tickCount, ticks run
        int[] boostedTicks = {boosted.tickCount, 0};
        // plain: engine explosion seen, rider no longer on the board
        boolean[] plainState = {false, false};
        Runnable sample = () -> {
            if (!boosted.isRemoved()) {
                maxStep[1] = Math.max(maxStep[1], Math.hypot(boosted.getX() - last[2], boosted.getZ() - last[3]));
                last[2] = boosted.getX();
                last[3] = boosted.getZ();
                maxRelY[1] = Math.max(maxRelY[1], helper.relativeVec(boosted.position()).y);
                boostedTicks[1] += boosted.tickCount - boostedTicks[0];
                boostedTicks[0] = boosted.tickCount;
            }
            int ran = plain.tickCount - plainTicks[0];
            plainTicks[0] = plain.tickCount;
            plainTicks[1] += ran;
            if (plain.getExploding() > 0) {
                plainState[0] = true;
            }
            if (plain.getFirstPassenger() != plainRider) {
                plainState[1] = true;
            }
            if (!plainState[0] && !plainState[1]) {
                plainTicks[2] += ran;
            }
            double step = Math.hypot(plain.getX() - last[0], plain.getZ() - last[1]);
            if (step > maxStep[0]) {
                maxStep[0] = step;
                plainTicks[3] = plainTicks[1];
            }
            last[0] = plain.getX();
            last[1] = plain.getZ();
            maxRelY[0] = Math.max(maxRelY[0], helper.relativeVec(plain.position()).y);
            maxRelY[2] = Math.max(maxRelY[2], helper.relativeVec(parked.position()).y);
        };
        helper.startSequence()
                .thenWaitUntil(() -> {
                    sample.run();
                    if (boostedTicks[1] < 20 && !boosted.isRemoved()) {
                        throw new GameTestAssertException("the boosted board has run " + boostedTicks[1] + " of 20 ticks");
                    }
                })
                .thenExecute(() -> {
                    boosted.positionRider(boostedRider);
                    helper.assertTrue(boostedRider.position().distanceTo(boosted.position()) < 1.0e-6,
                            "the rider sits at " + boostedRider.position() + ", the board is at " + boosted.position());
                    helper.assertTrue(boosted.isAlive(), "the boosted board broke");
                    helper.assertTrue(maxStep[1] > 1.5,
                            "with fly-up the board reached only " + maxStep[1] + " blocks a tick, expected close to 1.85");
                    helper.assertTrue(maxStep[1] <= 1.85 + 1.0e-6, "with fly-up the board exceeded 1.85: " + maxStep[1]);
                    helper.assertTrue(boosted.getZ() - boostedStart.z > 15.0 && Math.abs(boosted.getX() - boostedStart.x) < 0.5,
                            "the boosted board moved " + boosted.position().subtract(boostedStart) + ", expected far along +z");
                    helper.assertTrue(plain.getZ() - plainStart.z < boosted.getZ() - boostedStart.z,
                            "the plain board kept up with the boosted one");
                    boosted.discard();
                })
                .thenWaitUntil(() -> {
                    sample.run();
                    if (plainTicks[1] < 80 && !plain.isRemoved()) {
                        throw new GameTestAssertException("the plain board has run " + plainTicks[1] + " of 80 ticks");
                    }
                })
                .thenExecute(() -> {
                    String state = " (board ticks run " + plainTicks[1] + ", accelerating " + plainTicks[2]
                            + ", last new maximum after tick " + plainTicks[3] + ", engine explosion " + plainState[0]
                            + ", rider lost " + plainState[1] + ")";
                    helper.assertTrue(plain.isAlive(), "the plain board broke" + state);
                    if (plainState[0]) {
                        double expected = Math.min(0.85, 1.25 * (1.0 - Math.pow(0.98, plainTicks[2])));
                        helper.assertTrue(maxStep[0] >= expected - 0.01, "before its engine exploded the board reached only "
                                + maxStep[0] + " blocks a tick, expected " + expected + state);
                    } else {
                        helper.assertTrue(maxStep[0] > 0.8,
                                "without fly-up the board reached only " + maxStep[0] + " blocks a tick" + state);
                    }
                    helper.assertTrue(maxStep[0] <= 0.85 + 1.0e-6, "without fly-up the board exceeded 0.85: " + maxStep[0]);
                    helper.assertTrue(plain.getZ() - plainStart.z > 10.0 && Math.abs(plain.getX() - plainStart.x) < 0.5,
                            "the plain board moved " + plain.position().subtract(plainStart) + ", expected along +z" + state);
                    helper.assertTrue(maxRelY[1] > maxRelY[0],
                            "the boosted board climbed to " + maxRelY[1] + ", the plain one to " + maxRelY[0]);
                    helper.assertTrue(parked.getX() == parkedStart.x && parked.getZ() == parkedStart.z,
                            "the empty board drifted from " + parkedStart + " to " + parked.position());
                    helper.assertTrue(maxRelY[2] > 2.5,
                            "the empty board never rose above relative y " + maxRelY[2] + " (floor top 2.0, hover band up to 2.75)");
                    plain.discard();
                    parked.discard();
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ back-edges

    /**
     * The wave is closed only when no {@code PORT: TODO} addressed to W04 is left anywhere under
     * {@code src/}. The GameTest server runs in {@code run/gametest}; the source tree is found by
     * walking up from there.
     */
    @GameTest(template = ARENA)
    public static void noTodoForWaveFourRemainsInTheSourceTree(GameTestHelper helper) {
        Path src = null;
        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            if (Files.isDirectory(dir.resolve("src/main/java/com/swbr/orespawn"))) {
                src = dir.resolve("src");
                break;
            }
        }
        if (src == null) {
            helper.fail("source tree not found above " + Paths.get("").toAbsolutePath());
            return;
        }
        String marker = "TODO " + "W04"; // split so this file does not match itself
        List<String> found = new ArrayList<>();
        Path root = src;
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(f -> f.toString().matches(".*\\.(java|json|toml|mcmeta|md|txt|snbt)$")).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains(marker)) {
                        found.add(root.relativize(file) + ":" + (i + 1));
                    }
                }
            }
        } catch (IOException e) {
            helper.fail("reading the source tree failed: " + e);
        }
        if (!found.isEmpty()) {
            helper.fail(found.size() + " '" + marker + "' left: " + found);
        }
        helper.succeed();
    }
}
