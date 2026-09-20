package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.entity.boss.king.KingHead;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.boss.mobzilla.Godzilla;
import com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead;
import com.swbr.orespawn.entity.boss.prince.ThePrince;
import com.swbr.orespawn.entity.boss.prince.ThePrinceTeen;
import com.swbr.orespawn.entity.boss.queen.QueenHead;
import com.swbr.orespawn.entity.boss.queen.TheQueen;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * W10 bosses and royals on a real server (DECISIONS R4, R5, R9, R17).
 *
 * <p>Each boss spawns through its registry factory, carries {@code min(original, 1024)} as {@code MAX_HEALTH} and is
 * alive after 100 ticks (one batch each: they fly, roam and pick targets across the grid). Damage goes through the
 * real {@code hurt} override and {@code CombatEvents}: an armor-bypassing hit is capped by the original class and then
 * scaled by {@code 1024 / original}; a blockable hit additionally passes the 1.7.10 armor formula, which is zero at 25
 * (King and Mobzilla after a large attacker). The heads follow their body and forward a hit; the King and Queen
 * spawner blocks summon their boss in guard mode; the Kraken Repellent pushes a Kraken out of its box; a diamond block
 * makes The Prince grow into The Young Prince with the same owner.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W10GameTests {

    private static final String ARENA = "arena";
    /** Tests that spawn, hit and discard in the same tick - nothing ticks, so they share a batch. */
    private static final String INSTANT = "w10_instant";
    private static final float EPS = 0.05F;

    private W10GameTests() {}

    private static void clearMobsAround(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(64.0), entity -> !(entity instanceof Player))
                .forEach(Entity::discard);
    }

    /** Chunk radius kept entity-ticking around a roaming boss: The Queen covers 20 blocks in 17 ticks. */
    private static final int ROAM_CHUNKS = 12;

    /**
     * Force-loads the chunks around the test so that a flying boss keeps ticking once it leaves the structure grid (the
     * framework forces only the structures' chunks, and an entity outside entity-ticking chunks stops counting ticks).
     *
     * @return a release action that unforces exactly the chunks this call forced - the framework's own stay
     */
    private static Runnable forceRoamingArea(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos centre = new ChunkPos(helper.absolutePos(new BlockPos(6, 2, 6)));
        LongSet before = new LongOpenHashSet(level.getForcedChunks());
        List<ChunkPos> ours = new ArrayList<>();
        for (int x = -ROAM_CHUNKS; x <= ROAM_CHUNKS; ++x) {
            for (int z = -ROAM_CHUNKS; z <= ROAM_CHUNKS; ++z) {
                ChunkPos c = new ChunkPos(centre.x + x, centre.z + z);
                if (!before.contains(c.toLong())) {
                    level.setChunkForced(c.x, c.z, true);
                    ours.add(c);
                }
            }
        }
        return () -> {
            level.getEntitiesOfClass(Entity.class, helper.getBounds().inflate(ROAM_CHUNKS * 16.0 + 32.0, 256.0, ROAM_CHUNKS * 16.0 + 32.0),
                    entity -> !(entity instanceof Player)).forEach(Entity::discard);
            for (ChunkPos c : ours) {
                level.setChunkForced(c.x, c.z, false);
            }
        };
    }

    /** The Kraken turns on rain and thunder ten ticks after it exists (Kraken.tick, weather_set); later batches need a clear sky. */
    private static void clearWeather(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.setWeatherParameters(6000, 0, false, false);
        level.setRainLevel(0.0F);
        level.setThunderLevel(0.0F);
    }

    private static DamageSource generic(GameTestHelper helper) {
        DamageSource source = helper.getLevel().damageSources().generic();
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_ARMOR), "precondition: minecraft:generic must bypass armor");
        return source;
    }

    /** An arrow hit without an attacker: blockable, and no attacker branch of any boss {@code hurt} runs. */
    private static DamageSource blockable(GameTestHelper helper) {
        DamageSource source = helper.getLevel().damageSources().source(DamageTypes.ARROW);
        helper.assertTrue(!source.is(DamageTypeTags.BYPASSES_ARMOR), "precondition: minecraft:arrow must not bypass armor");
        return source;
    }

    // ------------------------------------------------------------------ spawn and tick

    private static <T extends LivingEntity> void spawnAndTick(GameTestHelper helper, EntityType<T> type, double original) {
        Runnable release = forceRoamingArea(helper);
        T e = helper.spawn(type, new BlockPos(6, 2, 6));
        if (e instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        String name = type.toShortString();
        float expected = (float) LegacyCombatMath.attributeMaxHealth(original);
        helper.assertTrue(e.getMaxHealth() == expected, name + " max health " + e.getMaxHealth() + ", expected " + expected);
        helper.assertTrue(e.getHealth() == expected, name + " spawned with " + e.getHealth() + " health, expected " + expected);
        if (e instanceof VirtualHealth virtual) {
            helper.assertTrue(virtual.getOriginalMaxHealth() == original,
                    name + " declares original max health " + virtual.getOriginalMaxHealth() + ", expected " + original);
        } else {
            helper.assertTrue(original <= LegacyCombatMath.MAX_ATTRIBUTE_HEALTH, name + " has " + original + " health but no VirtualHealth");
        }
        helper.runAfterDelay(100, () -> {
            boolean alive = e.isAlive();
            int ticks = e.tickCount;
            e.discard();
            release.run();
            clearWeather(helper);
            helper.assertTrue(alive, name + " did not survive 100 ticks (removed: " + e.getRemovalReason() + ")");
            helper.assertTrue(ticks >= 100, name + " ticked only " + ticks + " times");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_kraken")
    public static void krakenSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_KRAKEN.get(), 1000.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_mobzilla")
    public static void mobzillaSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.MOBZILLA.get(), 4000.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_king")
    public static void theKingSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_KING.get(), 7000.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_queen")
    public static void theQueenSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_QUEEN.get(), 6000.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_young_adult_prince")
    public static void youngAdultPrinceSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_YOUNG_ADULT_PRINCE.get(), 3000.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_young_prince")
    public static void youngPrinceSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_YOUNG_PRINCE.get(), 1500.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_prince")
    public static void thePrinceSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.THE_PRINCE.get(), 500.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w10_princess")
    public static void thePrincessSpawnsAndTicks(GameTestHelper helper) {
        LivingEntity probe = ModEntities.THE_PRINCESS.get().create(helper.getLevel());
        double original = probe.getMaxHealth();
        probe.discard();
        spawnAndTick(helper, ModEntities.THE_PRINCESS.get(), original);
    }

    // ------------------------------------------------------------------ R4: cap, then 1024 / original

    /**
     * One armor-bypassing hit of {@code amount} on a fresh boss: the class caps it at {@code cap} (no cap: pass the
     * amount), {@code CombatEvents} scales the capped amount by {@code 1024 / original}.
     */
    private static <T extends LivingEntity> void scaledHit(GameTestHelper helper, EntityType<T> type, double original, float amount, float cap) {
        T e = helper.spawn(type, new BlockPos(6, 2, 6));
        String name = type.toShortString();
        try {
            float before = e.getHealth();
            e.hurt(generic(helper), amount);
            float lost = before - e.getHealth();
            float expected = Math.min(amount, cap) * LegacyCombatMath.scale(original);
            helper.assertTrue(Math.abs(lost - expected) < EPS, name + ": a hit of " + amount + " removed " + lost
                    + " attribute health, expected min(" + amount + ", " + cap + ") * 1024 / " + original + " = " + expected);
            float originalHealth = VirtualHealth.originalHealth(e);
            float expectedOriginal = (float) (original - Math.min(amount, cap));
            helper.assertTrue(Math.abs(originalHealth - expectedOriginal) < 0.5F,
                    name + ": original-unit health reads " + originalHealth + ", expected " + expectedOriginal);
        } finally {
            e.discard();
        }
        helper.succeed();
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void krakenTakesUnscaledDamage(GameTestHelper helper) {
        scaledHit(helper, ModEntities.THE_KRAKEN.get(), 1000.0, 100.0F, Float.MAX_VALUE);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void mobzillaCapsAt750ThenScales(GameTestHelper helper) {
        scaledHit(helper, ModEntities.MOBZILLA.get(), 4000.0, 1000.0F, 750.0F);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void theKingCapsAt750ThenScales(GameTestHelper helper) {
        scaledHit(helper, ModEntities.THE_KING.get(), 7000.0, 1000.0F, 750.0F);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void theQueenCapsAt750ThenScales(GameTestHelper helper) {
        scaledHit(helper, ModEntities.THE_QUEEN.get(), 6000.0, 1000.0F, 750.0F);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void youngAdultPrinceScales(GameTestHelper helper) {
        scaledHit(helper, ModEntities.THE_YOUNG_ADULT_PRINCE.get(), 3000.0, 1000.0F, Float.MAX_VALUE);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void youngPrinceScales(GameTestHelper helper) {
        scaledHit(helper, ModEntities.THE_YOUNG_PRINCE.get(), 1500.0, 1000.0F, Float.MAX_VALUE);
    }

    // ------------------------------------------------------------------ R5: the 1.7.10 armor formula on the bosses

    /** A blockable hit: cap, scale, then {@code (25 - armor) / 25} with the boss's own {@code getTotalArmorValue}. */
    private static <T extends LivingEntity> void armoredHit(GameTestHelper helper, EntityType<T> type, double original, float amount, float cap,
            int expectedArmor) {
        T e = helper.spawn(type, new BlockPos(6, 2, 6));
        String name = type.toShortString();
        try {
            int armor = ((LegacyArmor) e).getLegacyArmorValue();
            helper.assertTrue(armor == expectedArmor, name + " armor at full health is " + armor + ", expected " + expectedArmor);
            float before = e.getHealth();
            e.hurt(blockable(helper), amount);
            float lost = before - e.getHealth();
            float expected = LegacyCombatMath.damageAfterArmor(Math.min(amount, cap) * LegacyCombatMath.scale(original), armor);
            helper.assertTrue(Math.abs(lost - expected) < EPS, name + ": a blockable hit of " + amount + " removed " + lost
                    + ", expected " + expected + " (cap " + cap + ", armor " + armor + ")");
        } finally {
            e.discard();
        }
        helper.succeed();
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void krakenArmorFormula(GameTestHelper helper) {
        armoredHit(helper, ModEntities.THE_KRAKEN.get(), 1000.0, 100.0F, Float.MAX_VALUE, 10);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void theKingArmorFormula(GameTestHelper helper) {
        armoredHit(helper, ModEntities.THE_KING.get(), 7000.0, 1000.0F, 750.0F, 21);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void theQueenArmorFormula(GameTestHelper helper) {
        armoredHit(helper, ModEntities.THE_QUEEN.get(), 6000.0, 1000.0F, 750.0F, 21);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void youngAdultPrinceArmorFormula(GameTestHelper helper) {
        armoredHit(helper, ModEntities.THE_YOUNG_ADULT_PRINCE.get(), 3000.0, 1000.0F, Float.MAX_VALUE, 20);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void youngPrinceArmorFormula(GameTestHelper helper) {
        armoredHit(helper, ModEntities.THE_YOUNG_PRINCE.get(), 1500.0, 1000.0F, Float.MAX_VALUE, 18);
    }

    /**
     * A living attacker with a footprint above 30 (a Giant, 3.6 x 12) trips {@code large_unknown_detected}: the hit is
     * cut to a tenth and {@code getTotalArmorValue} returns 25 - read at reduction time, so this very hit is blocked
     * completely (R5: {@code damage * (25 - 25) / 25}).
     */
    private static <T extends LivingEntity> void largeAttackerIsBlocked(GameTestHelper helper, EntityType<T> type) {
        T boss = helper.spawn(type, new BlockPos(6, 2, 6));
        Giant giant = helper.spawn(EntityType.GIANT, new BlockPos(2, 2, 2));
        String name = type.toShortString();
        try {
            helper.assertTrue(((LegacyArmor) boss).getLegacyArmorValue() < 25, name + " starts with armor 25 already");
            DamageSource source = helper.getLevel().damageSources().mobAttack(giant);
            helper.assertTrue(!source.is(DamageTypeTags.BYPASSES_ARMOR), "precondition: mob attack must be blockable");
            float before = boss.getHealth();
            boss.hurt(source, 1000.0F);
            int armor = ((LegacyArmor) boss).getLegacyArmorValue();
            helper.assertTrue(armor == 25, name + " armor after a Giant's hit is " + armor + ", expected 25");
            helper.assertTrue(boss.getHealth() == before, name + " lost " + (before - boss.getHealth())
                    + " health to a blockable hit at armor 25, expected 0");
        } finally {
            giant.discard();
            boss.discard();
        }
        helper.succeed();
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void theKingBlocksEverythingAtArmor25(GameTestHelper helper) {
        largeAttackerIsBlocked(helper, ModEntities.THE_KING.get());
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void mobzillaBlocksEverythingAtArmor25(GameTestHelper helper) {
        largeAttackerIsBlocked(helper, ModEntities.MOBZILLA.get());
    }

    // ------------------------------------------------------------------ heads (R9)

    /**
     * The head forwards a hit unchanged to its body (same tick, before any AI step could start the body's
     * {@code hurt_timer}) and loses nothing itself; after 20 ticks it stands {@code ahead} blocks in front of the body
     * and {@code up} above it, and mirrors the body's health in original units.
     */
    private static <B extends LivingEntity, H extends LivingEntity> void headFollowsAndForwards(GameTestHelper helper, EntityType<B> bodyType,
            EntityType<H> headType, double original, double ahead, double up) {
        Runnable release = forceRoamingArea(helper);
        B body = helper.spawn(bodyType, new BlockPos(6, 2, 6));
        ((Mob) body).setPersistenceRequired();
        H head = helper.spawn(headType, new BlockPos(6, 3, 6));
        String name = headType.toShortString();
        float bodyBefore = body.getHealth();
        float headBefore = head.getHealth();
        boolean answered = head.hurt(generic(helper), 1000.0F);
        float lost = bodyBefore - body.getHealth();
        float expected = 750.0F * LegacyCombatMath.scale(original);
        helper.assertTrue(answered, name + ".hurt answered false for a forwarded hit");
        helper.assertTrue(Math.abs(lost - expected) < EPS, name + " forwarded a hit of 1000 and the body lost " + lost + ", expected " + expected);
        helper.assertTrue(head.getHealth() == headBefore, name + " lost health itself: " + (headBefore - head.getHealth()));
        helper.runAfterDelay(20, () -> {
            try {
                helper.assertTrue(head.isAlive(), name + " was removed although its body is alive: " + head.getRemovalReason());
                helper.assertTrue(body.isAlive(), bodyType.toShortString() + " died");
                double dy = head.getY() - body.getY();
                double horizontal = Math.hypot(head.getX() - body.getX(), head.getZ() - body.getZ());
                // The head reads the body's position in its own tick; the body may move once more in the same tick.
                helper.assertTrue(Math.abs(dy - up) < 6.0, name + " is " + dy + " above the body, expected " + up);
                helper.assertTrue(Math.abs(horizontal - ahead) < 6.0, name + " is " + horizontal + " away horizontally, expected " + ahead);
                float headOriginal = VirtualHealth.originalHealth(head);
                float bodyOriginal = VirtualHealth.originalHealth(body);
                helper.assertTrue(Math.abs(headOriginal - bodyOriginal) <= 0.01F * (float) original + 1.0F,
                        name + " mirrors " + headOriginal + " original health, the body has " + bodyOriginal);
            } finally {
                release.run();
            }
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w10_king_head")
    public static void kingHeadFollowsAndForwards(GameTestHelper helper) {
        headFollowsAndForwards(helper, ModEntities.THE_KING.get(), ModEntities.KING_HEAD.get(), 7000.0, 30.0, 12.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w10_queen_head")
    public static void queenHeadFollowsAndForwards(GameTestHelper helper) {
        headFollowsAndForwards(helper, ModEntities.THE_QUEEN.get(), ModEntities.QUEEN_HEAD.get(), 6000.0, 30.0, 12.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w10_mobzilla_head")
    public static void mobzillaHeadFollowsAndForwards(GameTestHelper helper) {
        headFollowsAndForwards(helper, ModEntities.MOBZILLA.get(), ModEntities.MOBZILLA_HEAD.get(), 4000.0, 17.0, 16.0);
    }

    @GameTest(template = ARENA, batch = INSTANT)
    public static void headsWithoutBodyAreRemoved(GameTestHelper helper) {
        KingHead king = helper.spawn(ModEntities.KING_HEAD.get(), new BlockPos(6, 3, 6));
        QueenHead queen = helper.spawn(ModEntities.QUEEN_HEAD.get(), new BlockPos(6, 3, 6));
        GodzillaHead mobzilla = helper.spawn(ModEntities.MOBZILLA_HEAD.get(), new BlockPos(6, 3, 6));
        king.tick();
        queen.tick();
        mobzilla.tick();
        helper.assertTrue(king.isRemoved() && queen.isRemoved() && mobzilla.isRemoved(), "a head without a body in reach stayed: king "
                + king.isRemoved() + ", queen " + queen.isRemoved() + ", mobzilla " + mobzilla.isRemoved());
        helper.succeed();
    }

    // ------------------------------------------------------------------ spawner blocks

    private static <T extends Mob> void spawnerSummons(GameTestHelper helper, net.minecraft.world.level.block.Block block, Class<T> bossClass) {
        BlockPos pos = new BlockPos(6, 2, 6);
        helper.setBlock(pos, block);
        BlockPos abs = helper.absolutePos(pos);
        helper.succeedWhen(() -> {
            List<T> bosses = helper.getLevel().getEntitiesOfClass(bossClass, helper.getBounds().inflate(40.0));
            helper.assertTrue(!bosses.isEmpty(), "no " + bossClass.getSimpleName() + " summoned yet");
            helper.assertTrue(bosses.size() == 1, bosses.size() + " " + bossClass.getSimpleName() + "s summoned, expected one");
            T boss = bosses.get(0);
            helper.assertTrue(helper.getBlockState(pos).isAir(), "the spawner block is still there after the summoning");
            CompoundTag tag = boss.saveWithoutId(new CompoundTag());
            helper.assertTrue(tag.getInt("GuardMode") == 1, bossClass.getSimpleName() + " guard mode " + tag.getInt("GuardMode") + ", expected 1");
            // Summoned at the block corner, eight above; it may have started to fly in the tick it was found.
            helper.assertTrue(boss.tickCount > 5 || Math.abs(boss.getY() - (abs.getY() + 8)) < 3.0,
                    bossClass.getSimpleName() + " summoned at y " + boss.getY() + ", expected " + (abs.getY() + 8));
            clearMobsAround(helper);
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200, batch = "w10_king_spawner")
    public static void kingSpawnerSummonsTheKing(GameTestHelper helper) {
        spawnerSummons(helper, ModBlocks.KINGSPAWNER.get(), TheKing.class);
    }

    @GameTest(template = ARENA, timeoutTicks = 200, batch = "w10_queen_spawner")
    public static void queenSpawnerSummonsTheQueen(GameTestHelper helper) {
        spawnerSummons(helper, ModBlocks.QUEENSPAWNER.get(), TheQueen.class);
    }

    // ------------------------------------------------------------------ Kraken Repellent

    /**
     * KrakenRepellent.findSomethingToRepell: every 10 ticks a Kraken in x +-20, y -10..+40, z +-20 gets
     * {@code clamp(20 - d, 0, 20) * 0.4} of horizontal motion away from the block, {@code d} measured from 15 below its
     * feet. Spawned 5 blocks east with its feet 15 above the block, the first push lands on tick 10.
     *
     * <p>What the original does with that is not "out of the box": the push fades to zero at {@code d = 20}, and the
     * Kraken's own flight (0.45 blocks per tick at most, Kraken.customServerAiStep) may carry it back in, so it ends up
     * held at about 20 blocks (seen in the first runs: pushed to 20.2, then drifting back to 18.4 with no second push).
     * The test therefore checks the push itself - a jump of the eastward motion far above what the Kraken's flight
     * can add in one tick (0.15 x 0.9) - and that the Kraken reaches the 20-block ring, (18 is asserted, 20 minus the height offset of the push), which its own flight cannot do
     * from 5.5 blocks within the first 20 ticks (at most 9 blocks).
     */
    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w10_kraken_repellent")
    public static void krakenRepellentPushesAKrakenAway(GameTestHelper helper) {
        BlockPos torch = new BlockPos(6, 2, 6);
        helper.setBlock(torch, ModBlocks.KRAKEN_REPELLENT.get());
        BlockPos abs = helper.absolutePos(torch);
        Runnable release = forceRoamingArea(helper);
        Kraken kraken = helper.spawn(ModEntities.THE_KRAKEN.get(), new BlockPos(11, 17, 6));
        kraken.setPersistenceRequired();
        double[] lastVx = {kraken.getDeltaMovement().x};
        double[] maxJump = {0.0};
        double[] maxDistance = {0.0};
        StringBuilder trace = new StringBuilder();
        helper.onEachTick(() -> {
            if (kraken.tickCount > 20) {
                return;
            }
            double vx = kraken.getDeltaMovement().x;
            maxJump[0] = Math.max(maxJump[0], vx - lastVx[0]);
            lastVx[0] = vx;
            double dx = kraken.getX() - abs.getX();
            double dz = kraken.getZ() - abs.getZ();
            maxDistance[0] = Math.max(maxDistance[0], Math.hypot(dx, dz));
            trace.append(String.format(" [%d x%.1f vx%.2f]", kraken.tickCount, dx, vx));
        });
        helper.runAfterDelay(25, () -> {
            release.run();
            clearWeather(helper);
            helper.setBlock(torch, Blocks.AIR);
            helper.assertTrue(maxJump[0] > 1.0, "no push: the largest one-tick rise of the Kraken's eastward motion was " + maxJump[0] + trace);
            helper.assertTrue(maxDistance[0] >= 18.0, "the Kraken got only " + maxDistance[0] + " blocks away in 20 ticks" + trace);
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ The Prince grows

    /**
     * ThePrince.legacyInteract: a diamond block tames the prince to the clicking player and sets kill, feed and day
     * counters to 1000; the next {@code customServerAiStep} spawns The Young Prince with the same owner and removes the
     * prince (ThePrince.java :730-740).
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w10_prince_grows")
    public static void thePrinceGrowsIntoTheYoungPrince(GameTestHelper helper) {
        ThePrince prince = helper.spawn(ModEntities.THE_PRINCE.get(), new BlockPos(6, 2, 6));
        prince.setPersistenceRequired();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 2, 6)));
        player.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_BLOCK));
        player.interactOn(prince, InteractionHand.MAIN_HAND);
        helper.succeedWhen(() -> {
            List<ThePrinceTeen> teens = helper.getLevel().getEntitiesOfClass(ThePrinceTeen.class, helper.getBounds().inflate(16.0));
            helper.assertTrue(!teens.isEmpty(), "no Young Prince yet (prince tame " + prince.isTame() + ", removed " + prince.isRemoved() + ")");
            helper.assertTrue(teens.size() == 1, teens.size() + " Young Princes, expected one");
            helper.assertTrue(prince.isRemoved(), "The Prince stayed after growing");
            ThePrinceTeen teen = teens.get(0);
            helper.assertTrue(teen.isTame(), "the Young Prince is not tame");
            helper.assertTrue(player.getUUID().equals(teen.getOwnerUUID()), "the Young Prince's owner is " + teen.getOwnerUUID()
                    + ", expected the player " + player.getUUID());
            clearMobsAround(helper);
            helper.getLevel().getServer().getPlayerList().remove(player);
        });
    }
}
