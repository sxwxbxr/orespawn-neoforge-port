package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmorFormula;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.PortConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.config.stats.WeaponStats;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModAttachments;
import com.swbr.orespawn.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Wave 1: the cross-cutting services are live on a dedicated server.
 *
 * <p>Covers what the wave plan (catalogue README 4.2) lists and what a compile cannot prove: the
 * clamping stat readers return the original numbers, the {@code AllMobsDisable} bug survives
 * (R18), the per-player rider attachment is registered and isolated (R15), the port config is
 * loaded, and the five vanilla eggs spawn their entity through the ported spawn path.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public class W01GameTests {

    // Bare path: @GameTestHolder already supplies the namespace (see mods/armature).
    private static final String ARENA = "arena";

    /** Float tolerance for health arithmetic that goes through {@code hurt}. */
    private static final float EPS = 0.05F;

    /** {@code MobStats} reads the loaded spec and clamps like {@code get_mobstats} (:5717-5780). */
    @GameTest(template = ARENA)
    public static void mobStatsReturnTheOriginalDefaults(GameTestHelper helper) {
        int health = MobStats.TheKing_stats().health();
        if (health != 7000) {
            helper.fail("TheKing_stats().health() is " + health + ", the original default is 7000");
        }
        helper.succeed();
    }

    /** {@code WeaponStats} through {@code get_weaponstats} (:5701-5736): the jar defaults come back. */
    @GameTest(template = ARENA)
    public static void weaponStatsReturnTheOriginalDefaults(GameTestHelper helper) {
        WeaponStats ultimate = WeaponStats.ultimate_stats();
        if (ultimate.damage() != 36) {
            helper.fail("ultimate_stats().damage() is " + ultimate.damage() + ", the original default is 36");
        }
        if (ultimate.maxuses() != 3000 || ultimate.enchantability() != 100 || ultimate.harvestlevel() != 10) {
            helper.fail("ultimate_stats() is " + ultimate + ", expected (10, 3000, 15, 36, 100)");
        }
        int royal = WeaponStats.royal_stats().damage();
        if (royal != 746) {
            helper.fail("royal_stats().damage() is " + royal + ", the original default is 746");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ R4: virtual health

    /**
     * A vanilla zombie that declares The King's original maximum. No OreSpawn entity exists in
     * W01, and {@code CombatEvents} keys on the {@link VirtualHealth} interface, not on the
     * entity type - so this is the event path itself, through {@code LivingEntity.hurt}.
     */
    private static final class VirtualZombie extends Zombie implements VirtualHealth {

        private static final double ORIGINAL_MAX_HEALTH = 7000.0;

        VirtualZombie(Level level) {
            super(EntityType.ZOMBIE, level);
            // What every VirtualHealth entity's createAttributes() must do (R4).
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(ORIGINAL_MAX_HEALTH));
            setHealth(getMaxHealth());
        }

        @Override
        public double getOriginalMaxHealth() {
            return ORIGINAL_MAX_HEALTH;
        }
    }

    private static VirtualZombie spawnVirtualZombie(GameTestHelper helper) {
        VirtualZombie zombie = new VirtualZombie(helper.getLevel());
        Vec3 pos = helper.absoluteVec(new Vec3(6.5, 1.0, 6.5));
        zombie.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    /** Wave plan (2): damage to an entity with original max health 7000 arrives scaled by 1024/7000. */
    @GameTest(template = ARENA)
    public static void virtualHealthScalesDamageBy1024OverOriginal(GameTestHelper helper) {
        VirtualZombie zombie = spawnVirtualZombie(helper);
        try {
            if (zombie.getMaxHealth() != 1024.0F) {
                helper.fail("MAX_HEALTH is " + zombie.getMaxHealth() + ", the attribute clamp is 1024");
            }
            DamageSource generic = helper.getLevel().damageSources().generic();
            if (!generic.is(DamageTypeTags.BYPASSES_ARMOR)) {
                helper.fail("precondition: minecraft:generic must bypass armor so that no vanilla reduction interferes");
            }
            float before = zombie.getHealth();
            if (!zombie.hurt(generic, 700.0F)) {
                helper.fail("hurt() was refused");
            }
            float lost = before - zombie.getHealth();
            float expected = 700.0F * 1024.0F / 7000.0F; // 102.4
            if (Math.abs(lost - expected) > EPS) {
                helper.fail("700 original damage removed " + lost + " attribute health, expected " + expected);
            }
            float original = VirtualHealth.originalHealth(zombie);
            if (Math.abs(original - 6300.0F) > 0.5F) {
                helper.fail("originalHealth() reads " + original + " after 700 damage on 7000, expected 6300");
            }
        } finally {
            zombie.discard();
        }
        helper.succeed();
    }

    /** {@code heal()} is written in original units by every port and scaled by the same factor. */
    @GameTest(template = ARENA)
    public static void virtualHealthScalesHealingTheSameWay(GameTestHelper helper) {
        VirtualZombie zombie = spawnVirtualZombie(helper);
        try {
            zombie.setHealth(512.0F);
            zombie.heal(350.0F);
            float expected = 512.0F + 350.0F * 1024.0F / 7000.0F; // 563.2
            if (Math.abs(zombie.getHealth() - expected) > EPS) {
                helper.fail("heal(350) on a 7000-health entity gave " + zombie.getHealth() + ", expected " + expected);
            }
        } finally {
            zombie.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ R5: 1.7.10 armor formula

    /** Full diamond: 3 + 8 + 6 + 3 = 20 armor points, vanilla toughness 8. */
    private static Player diamondPlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(3.5, 1.0, 3.5));
        player.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        applyEquipmentAttributes(player);
        return player;
    }

    /**
     * What {@code LivingEntity.tick} -> {@code collectEquipmentChanges} -> {@code handleEquipmentChanges}
     * does for a real player: puts the worn items' modifiers on the attributes. A mock player never
     * ticks, so without this the vanilla {@code ARMOR} attribute stays 0 and vanilla reduces nothing -
     * which would make the guard test pass for the wrong reason. The port itself never reads the
     * attribute for players (R5), so the R5 tests are unaffected either way.
     */
    private static void applyEquipmentAttributes(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            player.getItemBySlot(slot).forEachModifier(slot, (attribute, modifier) -> {
                var instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modifier.id());
                    instance.addTransientModifier(modifier);
                }
            });
        }
    }

    /**
     * A helmet worth {@code points} armor instead of 3, the way an OreSpawn armor piece will carry
     * its value in 1.21.1: an {@code ADD_VALUE} modifier on {@code ARMOR} in the item's
     * {@code ATTRIBUTE_MODIFIERS} component. Replaces the diamond default.
     */
    private static void setHelmetArmor(Player player, int points) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        helmet.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "gametest_helmet_armor"),
                        points, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                .build());
    }

    /**
     * Runs {@code body} while {@code minecraft:diamond_helmet} is in {@code orespawn:legacy_armor}.
     *
     * <p>W01 ships no armor item, and the tag is filled by W03. The condition of R5 is "wears at
     * least one OreSpawn armor piece", checked through that tag, so the test binds a vanilla item
     * into it for its duration and restores the previous binding afterwards. {@code bindTags}
     * replaces every item's tag set, hence the full map.
     */
    private static void withDiamondHelmetAsLegacyArmor(Runnable body) {
        Map<TagKey<Item>, List<Holder<Item>>> original = new HashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
        Map<TagKey<Item>, List<Holder<Item>>> patched = new HashMap<>(original);
        List<Holder<Item>> legacy = new ArrayList<>(patched.getOrDefault(LegacyArmorFormula.LEGACY_ARMOR, List.of()));
        legacy.add(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_HELMET));
        patched.put(LegacyArmorFormula.LEGACY_ARMOR, legacy);
        BuiltInRegistries.ITEM.bindTags(patched);
        try {
            body.run();
        } finally {
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }

    private static void expectHealthAfterHit(GameTestHelper helper, Player player, DamageSource source,
            float damage, float expectedRemaining, String what) {
        float before = player.getHealth();
        player.hurt(source, damage);
        float remaining = player.getHealth();
        if (Math.abs(remaining - expectedRemaining) > EPS) {
            helper.fail(what + ": health went from " + before + " to " + remaining
                    + " on " + damage + " damage, expected " + expectedRemaining);
        }
    }

    /** Wave plan (3): with 25 armor points no blockable damage arrives. */
    @GameTest(template = ARENA)
    public static void legacyArmorAt25BlocksEveryBlockableHit(GameTestHelper helper) {
        withDiamondHelmetAsLegacyArmor(() -> {
            Player player = diamondPlayer(helper);
            setHelmetArmor(player, 8); // 8 + 8 + 6 + 3
            int armor = LegacyArmorFormula.armorValue(player);
            if (armor != 25) {
                helper.fail("armorValue() summed " + armor + " from the worn items, expected 25");
            }
            if (!LegacyArmorFormula.applies(player)) {
                helper.fail("a player wearing a legacy_armor item must get the 1.7.10 formula");
            }
            expectHealthAfterHit(helper, player, helper.getLevel().damageSources().cactus(), 10.0F, 20.0F,
                    "25 armor points against a blockable hit");
        });
        helper.succeed();
    }

    /** Wave plan (3): with 20 points, {@code 10 * (25 - 20) / 25 = 2} arrives - vanilla would let 3 through. */
    @GameTest(template = ARENA)
    public static void legacyArmorAt20LetsAFifthThrough(GameTestHelper helper) {
        withDiamondHelmetAsLegacyArmor(() -> {
            Player player = diamondPlayer(helper);
            int armor = LegacyArmorFormula.armorValue(player);
            if (armor != 20) {
                helper.fail("armorValue() summed " + armor + " from full diamond, expected 20");
            }
            expectHealthAfterHit(helper, player, helper.getLevel().damageSources().cactus(), 10.0F, 18.0F,
                    "20 armor points against a blockable hit");
        });
        helper.succeed();
    }

    /** Wave plan (3): {@code #minecraft:bypasses_armor} is 1.7.10's {@code isUnblockable()} - no reduction at all. */
    @GameTest(template = ARENA)
    public static void legacyArmorDoesNotTouchUnblockableDamage(GameTestHelper helper) {
        withDiamondHelmetAsLegacyArmor(() -> {
            Player player = diamondPlayer(helper);
            setHelmetArmor(player, 8); // 25 points: would block everything blockable
            DamageSource generic = helper.getLevel().damageSources().generic();
            if (!generic.is(DamageTypeTags.BYPASSES_ARMOR)) {
                helper.fail("precondition: minecraft:generic is expected in #minecraft:bypasses_armor");
            }
            expectHealthAfterHit(helper, player, generic, 10.0F, 10.0F, "25 armor points against unblockable damage");
        });
        helper.succeed();
    }

    /**
     * The guard of R5: a player in plain vanilla armor keeps the vanilla formula. Full diamond
     * against 10 cactus damage is 70 % reduction in 1.21.1 ({@code CombatRules.getDamageAfterAbsorb}:
     * armor 20, toughness 8), so 3 arrive - not the 2 of the 1.7.10 formula.
     */
    @GameTest(template = ARENA)
    public static void vanillaArmorKeepsTheVanillaFormula(GameTestHelper helper) {
        Player player = diamondPlayer(helper);
        if (LegacyArmorFormula.applies(player)) {
            helper.fail("the 1.7.10 formula applied to a player without any OreSpawn armor");
        }
        expectHealthAfterHit(helper, player, helper.getLevel().damageSources().cactus(), 10.0F, 17.0F,
                "vanilla diamond against a blockable hit");
        helper.succeed();
    }

    /**
     * {@code disableAllMobs} (OreSpawnMain.java:5804-5905) forgot RockEnable, CricketEnable and
     * FrogEnable; R18 keeps the bug. Read through the switch with AllMobsDisable forced to 1 and
     * restored afterwards, so the other tests see the file's value.
     */
    @GameTest(template = ARENA)
    public static void allMobsDisableSparesTheCricket(GameTestHelper helper) {
        int saved = OreSpawnConfig.TWEAKS.AllMobsDisable.get();
        OreSpawnConfig.TWEAKS.AllMobsDisable.set(1);
        try {
            int cricket = MobSwitches.get(OreSpawnConfig.MOBS.CricketEnable);
            int king = MobSwitches.get(OreSpawnConfig.MOBS.TheKingEnable);
            if (cricket == 0) {
                helper.fail("CricketEnable was zeroed by AllMobsDisable; the original skipped it");
            }
            if (king != 0) {
                helper.fail("TheKingEnable survived AllMobsDisable = 1");
            }
        } finally {
            OreSpawnConfig.TWEAKS.AllMobsDisable.set(saved);
        }
        helper.succeed();
    }

    /** The second config file (R5 switch) is registered from the constructor and loaded. */
    @GameTest(template = ARENA)
    public static void portConfigIsLoaded(GameTestHelper helper) {
        if (!PortConfig.SPEC.isLoaded()) {
            helper.fail(PortConfig.FILE_NAME + " was never loaded");
        }
        if (!PortConfig.legacyArmorFormula()) {
            helper.fail("legacyArmorFormula default must be true (R5)");
        }
        helper.succeed();
    }

    /** Wave plan (5): the rider payload changes only the sending player's key state. */
    @GameTest(template = ARENA)
    public static void riderPayloadChangesOnlyTheSendingPlayer(GameTestHelper helper) {
        if (!ModAttachments.FLY_UP_KEYSTATE.get().equals(NeoForgeRegistries.ATTACHMENT_TYPES.get(
                ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "fly_up_keystate")))) {
            helper.fail("orespawn:fly_up_keystate is not the registered attachment type");
        }
        Player sender = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        // Exactly what RiderControlMessageHandler does on the server thread for keystate 1.
        RiderKeys.set(sender, 1);
        if (!RiderKeys.isFlyUp(sender)) {
            helper.fail("sender's fly-up state was not set");
        }
        if (RiderKeys.isFlyUp(other)) {
            helper.fail("another player's fly-up state changed");
        }
        if (RiderKeys.isFlyUp(null)) {
            helper.fail("a missing rider must read as released");
        }
        RiderKeys.set(sender, 0);
        if (RiderKeys.isFlyUp(sender)) {
            helper.fail("sender's fly-up state was not cleared");
        }
        helper.succeed();
    }

    /**
     * Wave plan: the five vanilla eggs spawn their entity. Uses the same entry point as
     * {@code useOn} (block centre, one block up); the dragon is removed right away so its own
     * fight logic does not run in the arena.
     */
    @GameTest(template = ARENA)
    public static void vanillaEggsSpawnTheirEntity(GameTestHelper helper) {
        List<DeferredItem<ItemSpawnEgg>> eggs = List.of(
                ModItems.EGG_WITHER_SKELETON, ModItems.EGG_ENDER_DRAGON, ModItems.EGG_SNOW_GOLEM,
                ModItems.EGG_IRON_GOLEM, ModItems.EGG_WITHER_BOSS);
        BlockPos floor = helper.absolutePos(new BlockPos(1, 1, 1));
        for (DeferredItem<ItemSpawnEgg> holder : eggs) {
            ItemSpawnEgg egg = holder.get();
            EntityType<?> type = egg.getType();
            Entity spawned = ItemSpawnEgg.spawnSomething(type, helper.getLevel(),
                    floor.getX() + 0.5, floor.getY() + 1.01, floor.getZ() + 0.5);
            if (spawned == null || spawned.getType() != type) {
                helper.fail(holder.getId() + " did not spawn " + EntityType.getKey(type));
            }
            if (!spawned.isAlive() || spawned.level() != helper.getLevel()) {
                helper.fail(holder.getId() + " spawned an entity that is not in the level");
            }
            spawned.discard();
        }
        if (ItemSpawnEgg.all().size() < eggs.size()) {
            helper.fail("ItemSpawnEgg.all() lists " + ItemSpawnEgg.all().size() + " eggs, expected at least " + eggs.size());
        }
        helper.succeed();
    }
}
