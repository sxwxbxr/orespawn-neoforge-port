package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.gui.GirlfriendOverlayTarget;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.cage.EntityCage;
import com.swbr.orespawn.entity.easter.EasterBunny;
import com.swbr.orespawn.item.cage.CritterCage;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * W11 collectors and overviews on a real server (DECISIONS R17, R18, R25): every Critter Cage releases a creature
 * and the thrown cage catches exactly that creature back into the same cage; the Easter Bunny spawns, ticks and lays
 * one of 109 eggs; the health bar overlay reads full health as a full bar and half health of the three bosses as half.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W11GameTests {

    private static final String ARENA = "arena";
    private static final float EPS = 0.05F;
    /** Catch attempts per creature: the Kraken's 0.8 x 0.05 success chance fails 3000 times with p below 1e-50. */
    private static final int MAX_THROWS = 3000;

    private W11GameTests() {}

    /** The thrown cage with its protected impact reachable from the test, never added to the level. */
    private static final class TestCage extends EntityCage {
        TestCage(final Level level) {
            super(level, 160);
        }

        void impact(final HitResult hit) {
            this.onImpact(hit);
        }
    }

    private static void discardAround(final GameTestHelper helper, final AABB box) {
        helper.getLevel().getEntitiesOfClass(Entity.class, box, e -> !(e instanceof Player)).forEach(Entity::discard);
    }

    /** All 114 cage items by registry path. */
    private static Map<String, CritterCage> cages() {
        final Map<String, CritterCage> result = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            final ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (item instanceof CritterCage cage && OreSpawn.MOD_ID.equals(id.getNamespace())) {
                result.put(id.getPath(), cage);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ Critter Cage

    /**
     * Releases every filled cage with {@code useOn} on a block, as a player does, and throws cages at the creature
     * until it is caught. The caught creature must leave the cage it came from - and no other cage - so the release
     * switch of {@code CritterCage} and the catch chain of {@code EntityCage} agree for all 113 creatures. Covers the
     * R18 splits: a released horse may be a donkey and still fills {@code cagehorse}, the wither skeleton cage releases
     * a wither skeleton, a released slime or magma cube keeps a 1.7.10 size (1, 2 or 4).
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "w11_cages")
    public static void everyCageReleasesACreatureThatIsCaughtIntoTheSameCage(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final AABB box = helper.getBounds().inflate(8.0);
        final BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        discardAround(helper, box);
        final Map<String, CritterCage> cages = cages();
        helper.assertTrue(cages.size() == 114, "expected 114 CritterCage items, found " + cages.size());
        helper.assertTrue(cages.get("cageempty") == ModItems.CAGE_EMPTY.get(), "cageempty is not ModItems.CAGE_EMPTY");

        final List<String> failures = new ArrayList<>();
        for (Map.Entry<String, CritterCage> entry : cages.entrySet()) {
            final String id = entry.getKey();
            final CritterCage cage = entry.getValue();
            if (cage == ModItems.CAGE_EMPTY.get()) {
                continue;
            }
            final Set<Entity> before = new HashSet<>(level.getEntitiesOfClass(Entity.class, box));
            final ItemStack stack = new ItemStack(cage, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            final BlockHitResult click = new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false);
            cage.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, click));
            final List<Mob> released = level.getEntitiesOfClass(Mob.class, box, e -> !before.contains(e));
            if (released.size() != 1) {
                failures.add(id + ": released " + released.size() + " mobs");
                discardAround(helper, box);
                continue;
            }
            final Mob creature = released.get(0);
            if (stack.getCount() != 1) {
                failures.add(id + ": stack not shrunk (" + stack.getCount() + ")");
            }
            if (id.equals("cagewitherskeleton") && creature.getType() != EntityType.WITHER_SKELETON) {
                failures.add(id + ": released " + creature.getType().toShortString());
            }
            if (id.equals("cagehorse") && creature.getType() != EntityType.HORSE && creature.getType() != EntityType.DONKEY) {
                failures.add(id + ": released " + creature.getType().toShortString());
            }
            if (creature instanceof Slime slime && slime.getSize() != 1 && slime.getSize() != 2 && slime.getSize() != 4) {
                failures.add(id + ": slime size " + slime.getSize());
            }
            final String typeName = creature.getType().toShortString();
            int throwsUsed = 0;
            while (!creature.isRemoved() && throwsUsed < MAX_THROWS) {
                final TestCage thrown = new TestCage(level);
                thrown.setPos(creature.getX(), creature.getY(), creature.getZ());
                thrown.impact(new EntityHitResult(creature));
                ++throwsUsed;
            }
            if (!creature.isRemoved()) {
                failures.add(id + ": " + typeName + " not caught in " + MAX_THROWS + " throws");
            }
            final Set<Item> filled = new HashSet<>();
            int count = 0;
            for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, box, e -> !before.contains(e))) {
                final Item item = drop.getItem().getItem();
                if (item instanceof CritterCage && item != ModItems.CAGE_EMPTY.get()) {
                    filled.add(item);
                    count += drop.getItem().getCount();
                }
            }
            if (!filled.equals(Set.of(cage))) {
                final List<String> names = new ArrayList<>();
                filled.forEach(i -> names.add(BuiltInRegistries.ITEM.getKey(i).getPath()));
                failures.add(id + ": " + typeName + " was caught into " + names);
            } else if (count < 1) {
                failures.add(id + ": no filled cage dropped");
            }
            discardAround(helper, box);
        }
        discardAround(helper, box);
        helper.assertTrue(failures.isEmpty(), failures.size() + " cage failures: " + String.join("; ", failures));
        helper.succeed();
    }

    /** A thrown empty cage that hits no creature breaks on the floor and gives back one empty cage (:841-846). */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "w11_cage_flight")
    public static void aThrownCageThatHitsTheFloorDropsAnEmptyCage(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final AABB box = helper.getBounds().inflate(4.0);
        discardAround(helper, box);
        final EntityCage cage = new EntityCage(level, 160);
        final Vec3 start = Vec3.atCenterOf(helper.absolutePos(new BlockPos(6, 5, 6)));
        cage.setPos(start.x, start.y, start.z);
        level.addFreshEntity(cage);
        helper.succeedWhen(() -> {
            helper.assertTrue(cage.isRemoved(), "cage still flying");
            final List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, box,
                    e -> e.getItem().is(ModItems.CAGE_EMPTY.get()));
            helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 1,
                    "expected one empty cage, found " + drops.size() + " drops");
            drops.forEach(Entity::discard);
        });
    }

    // ------------------------------------------------------------------ Easter Bunny

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w11_easter_bunny")
    public static void easterBunnySpawnsAndTicks(final GameTestHelper helper) {
        final EasterBunny bunny = helper.spawn(ModEntities.EASTER_BUNNY.get(), new BlockPos(6, 2, 6));
        bunny.setPersistenceRequired();
        helper.assertTrue(bunny.getMaxHealth() == 10.0F, "max health " + bunny.getMaxHealth());
        helper.runAfterDelay(100, () -> {
            final boolean alive = bunny.isAlive();
            final int ticks = bunny.tickCount;
            discardAround(helper, helper.getBounds().inflate(32.0));
            helper.assertTrue(alive, "Easter Bunny did not survive 100 ticks (removed: " + bunny.getRemovalReason() + ")");
            helper.assertTrue(ticks >= 100, "Easter Bunny ticked only " + ticks + " times");
            helper.succeed();
        });
    }

    /**
     * {@code LayAnEgg} (EasterBunny.java:115-573): rolls 0..4 and 114 lay nothing, 5..113 lay 109 different spawn eggs.
     * The Easter Bunny egg itself is among them (case 92), and every egg is a registered spawn egg.
     */
    @GameTest(template = ARENA, batch = "w11_instant")
    public static void easterBunnyLaysOneOf109DifferentEggs(final GameTestHelper helper) {
        final Set<Item> eggs = new HashSet<>();
        for (int i = 0; i < 115; ++i) {
            final Item egg = EasterBunny.eggFor(i);
            if (i < 5 || i == 114) {
                helper.assertTrue(egg == null, "roll " + i + " lays " + egg);
                continue;
            }
            helper.assertTrue(egg instanceof ItemSpawnEgg, "roll " + i + " lays no spawn egg: " + egg);
            helper.assertTrue(eggs.add(egg), "roll " + i + " repeats " + egg);
        }
        helper.assertTrue(eggs.size() == 109, "expected 109 eggs, found " + eggs.size());
        helper.assertTrue(eggs.contains(ModItems.EGG_EASTER_BUNNY.get()), "the Easter Bunny egg is not in the table");

        final EasterBunny bunny = helper.spawn(ModEntities.EASTER_BUNNY.get(), new BlockPos(6, 2, 6));
        final AABB box = helper.getBounds().inflate(4.0);
        int laid = 0;
        for (int attempt = 0; attempt < 200 && laid == 0; ++attempt) {
            final int count = 1 + attempt % 3;
            final ItemStack stack = bunny.LayAnEgg(count);
            if (stack != null) {
                laid = count;
                helper.assertTrue(stack.getCount() == count && eggs.contains(stack.getItem()), "laid " + stack);
            }
        }
        final int laidCount = laid;
        final boolean dropped = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, box,
                e -> e.getItem().getItem() instanceof ItemSpawnEgg && e.getItem().getCount() == laidCount).isEmpty();
        discardAround(helper, box);
        helper.assertTrue(laidCount > 0, "no egg in 200 lays");
        helper.assertTrue(dropped, "the laid egg is not in the level");
        helper.succeed();
    }

    // ------------------------------------------------------------------ health bar overlay

    /**
     * The overlay chain ({@code GirlfriendOverlayTarget.select}) over every OreSpawn type at full health: whatever it
     * shows has a name and a full bar - the getters in original units and the attribute ratio agree (R4). An unowned
     * Girlfriend shows nothing. The King, The Queen and Mobzilla at half health show a half bar under their names (R25).
     */
    @GameTest(template = ARENA, batch = "w11_instant")
    public static void overlayShowsFullBarsAndHalfBossBars(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        final List<String> failures = new ArrayList<>();
        int shown = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!OreSpawn.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            final Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(living, player);
            if (target == null) {
                continue;
            }
            ++shown;
            if (target.name().isEmpty()) {
                failures.add(id + ": empty name");
            }
            if (target.ratio() < 1.0F - EPS || target.ratio() > 1.0F + EPS) {
                failures.add(id + ": full health shows " + target.ratio());
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.assertTrue(shown >= 30, "only " + shown + " types show a bar");

        helper.assertTrue(GirlfriendOverlayTarget.select(ModEntities.GIRLFRIEND.get().create(level), player) == null,
                "an unowned Girlfriend shows a bar");

        halfBar(helper, ModEntities.THE_KING.get().create(level), player, "The King");
        halfBar(helper, ModEntities.THE_QUEEN.get().create(level), player, "The Queen");
        halfBar(helper, ModEntities.MOBZILLA.get().create(level), player, "Mobzilla");
        helper.succeed();
    }

    /**
     * The ratio function itself for bosses with virtual health (R4, R25): The King, The Queen and Mobzilla store a
     * clamped attribute (at most 1024) but the overlay must show the 1.7.10 ratio {@code health / originalMax}. Every
     * OreSpawn type that shows a bar is set to 25 % and 60 % of its original maximum in original units
     * ({@code VirtualHealth.setOriginalHealth}); the bar must equal the original ratio, up to the integer truncation of
     * the 1.7.10 {@code get<Name>Health()} getters (one original health point).
     */
    @GameTest(template = ARENA, batch = "w11_instant")
    public static void overlayRatioIsTheOriginalRatioUnderVirtualHealth(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        final List<String> failures = new ArrayList<>();
        int checked = 0;
        int virtual = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!OreSpawn.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            final Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity living) || GirlfriendOverlayTarget.select(living, player) == null) {
                continue;
            }
            final double originalMax = VirtualHealth.originalMaxHealth(living);
            if (originalMax > living.getMaxHealth() + 0.5) {
                ++virtual;
            }
            for (float fraction : new float[] {0.25F, 0.6F}) {
                VirtualHealth.setOriginalHealth(living, (float) (originalMax * fraction));
                final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(living, player);
                if (target == null) {
                    continue; // activity conditions (e.g. a buried worm) may hide the bar; not a ratio question
                }
                ++checked;
                final float expected = (float) (VirtualHealth.originalHealth(living) / originalMax);
                final float tolerance = (float) (1.0 / originalMax) + 0.002F;
                if (Math.abs(target.ratio() - expected) > tolerance) {
                    failures.add(id + " at " + fraction + ": bar " + target.ratio() + ", original ratio " + expected);
                }
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.assertTrue(checked >= 60, "only " + checked + " ratio checks");
        helper.assertTrue(virtual >= 3, "only " + virtual + " overlay types have virtual health");

        for (EntityType<? extends LivingEntity> type : List.of(ModEntities.THE_KING.get(), ModEntities.THE_QUEEN.get(),
                ModEntities.MOBZILLA.get())) {
            final LivingEntity boss = type.create(level);
            helper.assertTrue(boss instanceof VirtualHealth, type.toShortString() + " has no virtual health");
            final double originalMax = VirtualHealth.originalMaxHealth(boss);
            helper.assertTrue(originalMax > 1024.0 && boss.getMaxHealth() <= 1024.0F,
                    type.toShortString() + ": original " + originalMax + ", attribute " + boss.getMaxHealth());
            // 1.7.10: a King at 1750 of 7000 showed a quarter bar, although the stored health is 256 of 1024.
            VirtualHealth.setOriginalHealth(boss, (float) (originalMax / 4.0));
            final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(boss, player);
            helper.assertTrue(target != null && Math.abs(target.ratio() - 0.25F) < 0.001F,
                    type.toShortString() + " quarter health shows " + (target == null ? "nothing" : target.ratio()));
            helper.assertTrue(Math.abs(boss.getHealth() - boss.getMaxHealth() / 4.0F) < 0.01F,
                    type.toShortString() + " stores " + boss.getHealth() + " of " + boss.getMaxHealth());
        }
        helper.succeed();
    }

    /** An owned Girlfriend (not riding) shows her getter ratio under her name (:65-80). */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "w11_girlfriend_overlay")
    public static void ownedGirlfriendShowsHerHealth(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final Girlfriend gf = ModEntities.GIRLFRIEND.get().create(helper.getLevel());
        try {
            gf.tame(player);
            gf.setHealth(gf.getMaxHealth() * 0.5F);
            final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(gf, player);
            helper.assertTrue(target != null, "an owned Girlfriend shows no bar");
            helper.assertTrue("Girlfriend".equals(target.name()), "shown as " + target.name());
            final float expected = (int) gf.getHealth() / gf.getMaxHealth();
            helper.assertTrue(Math.abs(target.ratio() - expected) < 0.001F, "ratio " + target.ratio() + ", expected " + expected);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ catch table, reverse direction

    /**
     * Every registered living type - vanilla and OreSpawn - is hit by thrown cages. A type that is caught must land in
     * exactly one filled cage, and that cage must release the same type again. Allowed differences, each logged:
     * a subclass caught by its parent's {@code instanceof} line (glow squid, husk, drowned, zombie villager, Ruby Bird -
     * the 1.7.10 chain tests classes), and the R18 splits: all five horse types fill {@code cagehorse}, which releases
     * a horse or a donkey; a cat fills {@code cageocelot}.
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "w11_cage_reverse")
    public static void everyCatchableTypeIsReleasedAsTheSameType(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final AABB box = helper.getBounds().inflate(8.0);
        final BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        final Vec3 spot = Vec3.atCenterOf(floor).add(0.0, 1.0, 0.0);
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        discardAround(helper, box);
        final Set<EntityType<?>> horses = Set.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE,
                EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE);
        final List<String> failures = new ArrayList<>();
        final List<String> aliases = new ArrayList<>();
        final Set<Item> cagesUsed = new HashSet<>();
        int caught = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            final String typeName = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            final Entity created;
            try {
                created = type.create(level);
            } catch (RuntimeException e) {
                failures.add(typeName + ": create threw " + e);
                continue;
            }
            if (!(created instanceof LivingEntity creature)) {
                continue;
            }
            creature.setPos(spot.x, spot.y, spot.z);
            final Set<Entity> before = new HashSet<>(level.getEntitiesOfClass(Entity.class, box));
            final int maxThrows = creature instanceof com.swbr.orespawn.entity.boss.kraken.Kraken ? MAX_THROWS : 200;
            for (int i = 0; i < maxThrows && !creature.isRemoved(); ++i) {
                final TestCage thrown = new TestCage(level);
                thrown.setPos(spot.x, spot.y, spot.z);
                thrown.impact(new EntityHitResult(creature));
            }
            final Set<Item> filled = new HashSet<>();
            for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, box, e -> !before.contains(e))) {
                final Item item = drop.getItem().getItem();
                if (item instanceof CritterCage && item != ModItems.CAGE_EMPTY.get()) {
                    filled.add(item);
                }
            }
            discardAround(helper, box);
            if (!creature.isRemoved()) {
                if (!filled.isEmpty()) {
                    failures.add(typeName + ": filled cage without catching");
                }
                continue;
            }
            ++caught;
            if (filled.size() != 1) {
                failures.add(typeName + ": caught into " + filled.size() + " different cages");
                continue;
            }
            final CritterCage cage = (CritterCage) filled.iterator().next();
            cagesUsed.add(cage);
            final String cageName = BuiltInRegistries.ITEM.getKey(cage).getPath();
            final Set<Entity> beforeRelease = new HashSet<>(level.getEntitiesOfClass(Entity.class, box));
            final ItemStack stack = new ItemStack(cage, 1);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            cage.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
            final List<Mob> released = level.getEntitiesOfClass(Mob.class, box, e -> !beforeRelease.contains(e));
            discardAround(helper, box);
            if (released.size() != 1) {
                failures.add(typeName + " -> " + cageName + ": released " + released.size() + " mobs");
                continue;
            }
            final Mob back = released.get(0);
            if (back.getType() == type) {
                continue;
            }
            if (horses.contains(type) && (back.getType() == EntityType.HORSE || back.getType() == EntityType.DONKEY)) {
                aliases.add(typeName + " -> " + cageName + " -> " + back.getType().toShortString() + " (R18 horse)");
            } else if (type == EntityType.CAT && back.getType() == EntityType.OCELOT) {
                aliases.add(typeName + " -> " + cageName + " -> ocelot (R18 cat)");
            } else if (back.getClass().isInstance(creature)) {
                aliases.add(typeName + " -> " + cageName + " -> " + back.getType().toShortString() + " (subclass)");
            } else {
                failures.add(typeName + " -> " + cageName + " releases " + back.getType().toShortString());
            }
        }
        OreSpawn.LOG.info("W11 reverse cage table: {} types caught into {} cages; aliases: {}", caught, cagesUsed.size(), aliases);
        helper.assertTrue(failures.isEmpty(), failures.size() + " failures: " + String.join("; ", failures));
        helper.assertTrue(cagesUsed.size() == 113, "caught types fill " + cagesUsed.size() + " of 113 filled cages");
        helper.succeed();
    }

    // ------------------------------------------------------------------ back-edges

    /** Catalogue 4.11: every back-edge of W11 is wired - no source or resource file still carries a W11 TODO. */
    @GameTest(template = ARENA, batch = "w11_instant")
    public static void noW11TodoRemainsInTheSources(final GameTestHelper helper) {
        final Pattern marker = Pattern.compile("TODO\\W{0,3}W" + "11\\b");
        final Path project = Paths.get("").toAbsolutePath().getParent().getParent();
        final List<String> failures = new ArrayList<>();
        int files = 0;
        for (String root : List.of("src/main/java", "src/main/resources")) {
            final Path dir = project.resolve(root);
            if (!Files.isDirectory(dir)) {
                failures.add("precondition: " + dir + " not found");
                continue;
            }
            try (Stream<Path> walk = Files.walk(dir)) {
                for (Path file : walk.filter(Files::isRegularFile).filter(f -> {
                    final String n = f.getFileName().toString();
                    return n.endsWith(".java") || n.endsWith(".json") || n.endsWith(".md") || n.endsWith(".toml")
                            || n.endsWith(".mcmeta") || n.endsWith(".snbt");
                }).toList()) {
                    ++files;
                    final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for (int i = 0; i < lines.size(); ++i) {
                        if (marker.matcher(lines.get(i)).find()) {
                            failures.add(project.relativize(file) + ":" + (i + 1));
                        }
                    }
                }
            } catch (IOException e) {
                failures.add(root + ": " + e);
            }
        }
        OreSpawn.LOG.info("W11 TODO scan: {} files, {} findings", files, failures.size());
        helper.assertTrue(files >= 500, "only " + files + " files scanned");
        helper.assertTrue(failures.isEmpty(), "W11 TODO left: " + String.join(", ", failures));
        helper.succeed();
    }

    private static void halfBar(final GameTestHelper helper, final LivingEntity boss, final Player player, final String name) {
        boss.setHealth(boss.getMaxHealth() * 0.5F);
        final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(boss, player);
        helper.assertTrue(target != null, name + " shows no bar");
        helper.assertTrue(name.equals(target.name()), name + " is shown as " + target.name());
        helper.assertTrue(Math.abs(target.ratio() - 0.5F) < EPS, name + " at half health shows " + target.ratio());
    }
}
