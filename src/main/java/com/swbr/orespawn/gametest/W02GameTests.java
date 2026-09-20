package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.crop.BlockDuctTape;
import com.swbr.orespawn.block.crop.BlockLettuce;
import com.swbr.orespawn.block.crop.BlockPizza;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Wave 2: every ore, storage and egg block places and drops its original item; Lavafoam throws;
 * RTPBlock relocates; crops grow and drop; pizza and duct tape count their uses; foods restore the
 * original hunger and effects (catalogue README 4.3, R17).
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public class W02GameTests {

    private static final String ARENA = "arena";
    private static final float EPS = 0.05F;

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id));
    }

    // ------------------------------------------------------------------ w02-ores

    /** Every w02-ores block can be set and its loot table yields the drop of getItemDropped/quantityDropped. */
    @GameTest(template = ARENA)
    public static void w02OresBlocksPlaceAndDropTheOriginalItem(GameTestHelper helper) {
        List<DeferredBlock<? extends Block>> all = new ArrayList<>(List.of(
                ModBlocks.ORESALT, ModBlocks.OREURANIUM, ModBlocks.ORETITANIUM, ModBlocks.OREAMETHYST, ModBlocks.ORERUBY,
                ModBlocks.BLOCKTELEPORT, ModBlocks.LAVAFOAM, ModBlocks.BLOCKURANIUM, ModBlocks.BLOCKTITANIUM,
                ModBlocks.BLOCKRUBY, ModBlocks.BLOCKAMETHYST, ModBlocks.BLOCKENDERPEARL, ModBlocks.BLOCKEYEOFENDER,
                ModBlocks.MOLEDIRT, ModBlocks.BLOCKMOBZILLASCALE, ModBlocks.CRYSTALSTONE, ModBlocks.CRYSTALCOAL,
                ModBlocks.CRYSTALCRYSTAL, ModBlocks.CRYSTALPINK_BLOCK, ModBlocks.TIGERSEYE, ModBlocks.TIGERSEYE_BLOCK,
                ModBlocks.CRYSTALRAT, ModBlocks.CRYSTALFAIRY, ModBlocks.REDANTTROLL, ModBlocks.TERMITETROLL));
        all.addAll(ModBlocks.DRIED_EGGS.values());
        if (all.size() != 144) {
            helper.fail("expected 144 w02-ores blocks, found " + all.size());
        }
        if (ModItems.DRIED_EGG_ITEMS.size() != 119) {
            helper.fail("expected 119 dried-egg block items, found " + ModItems.DRIED_EGG_ITEMS.size());
        }
        BlockPos rel = new BlockPos(2, 2, 2);
        for (DeferredBlock<? extends Block> holder : all) {
            Block block = holder.get();
            helper.setBlock(rel, block);
            BlockState state = helper.getBlockState(rel);
            if (!state.is(block)) {
                helper.fail(holder.getId() + " could not be placed");
            }
            if (block.asItem() == Items.AIR) {
                helper.fail(holder.getId() + " has no block item");
            }
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), helper.absolutePos(rel), null);
            String id = holder.getId().getPath();
            Item expected = id.equals("oreruby") ? item("ruby") : id.equals("oreamethyst") ? item("amethyst") : block.asItem();
            int count = drops.stream().filter(s -> s.is(expected)).mapToInt(ItemStack::getCount).sum();
            int min = id.equals("tigerseye") ? 0 : 1;
            int max = (id.equals("oreruby") || id.equals("oreamethyst")) ? 2 : 1;
            if (count < min || count > max || drops.stream().anyMatch(s -> !s.is(expected) && !s.isEmpty())) {
                helper.fail(holder.getId() + " dropped " + drops + ", expected " + min + ".." + max + " x " + expected);
            }
            helper.setBlock(rel, Blocks.AIR);
        }
        helper.succeed();
    }

    /**
     * Lavafoam.java:72-107: a living entity touching the block gets 0.45 on one horizontal axis, 1.35 x its
     * motion on the other, and fall damage when the result exceeds 1.
     *
     * <p>Which axis is which depends on {@code atan2(x - (bx + 0.5f), z - (bz + 0.5f))} - the original's
     * {@code float} arithmetic, kept 1:1. The game-test world sits at |x| ~ 1.3e7, beyond 2^23, where a float
     * cannot hold the {@code .5} and the direction becomes a rounding artefact (the first run landed in the
     * "else" branch from the +x side). So the test starts with symmetric motion (2, 0, 2): every branch then
     * yields {+-0.45, 2.7} in some order, and the damage sqrt(2.7^2 + 0.45^2) is the same for all four.
     */
    @GameTest(template = ARENA)
    public static void lavafoamThrowsAndHurtsALivingEntity(GameTestHelper helper) {
        BlockPos rel = new BlockPos(3, 1, 3);
        helper.setBlock(rel, ModBlocks.LAVAFOAM.get());
        BlockPos abs = helper.absolutePos(rel);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(3.5 + 0.8, 1.0, 3.5));
        try {
            zombie.setNoAi(true);
            zombie.setDeltaMovement(2.0, 0.0, 2.0);
            float before = zombie.getHealth();
            helper.getBlockState(rel).entityInside(helper.getLevel(), abs, zombie);
            Vec3 motion = zombie.getDeltaMovement();
            boolean pushedAlongX = Math.abs(Math.abs(motion.x) - 0.45) < 1.0E-6 && Math.abs(motion.z - 2.7) < 1.0E-6;
            boolean pushedAlongZ = Math.abs(Math.abs(motion.z) - 0.45) < 1.0E-6 && Math.abs(motion.x - 2.7) < 1.0E-6;
            if (!pushedAlongX && !pushedAlongZ) {
                helper.fail("motion after contact is " + motion + ", expected +-0.45 on one axis and 2.7 on the other");
            }
            // minecraft:fall is in #bypasses_armor, so the zombie's two armor points do not reduce it.
            float expectedDamage = (float) Math.sqrt(2.7 * 2.7 + 0.45 * 0.45);
            if (Math.abs((before - zombie.getHealth()) - expectedDamage) > EPS) {
                helper.fail("fall damage was " + (before - zombie.getHealth()) + ", expected " + expectedDamage);
            }
        } finally {
            zombie.discard();
        }
        helper.succeed();
    }

    /**
     * RTPBlock.java:20-66: a server player who steps on it lands 9-23 blocks away on each axis, on a solid floor.
     * {@code makeMockServerPlayerInLevel} is marked for removal but is the only mock with a {@code connection},
     * which the teleport goes through; {@code makeMockPlayer} is not a {@code ServerPlayer}.
     *
     * <p>Own batch (W07 integration): the landing floor below rewrites a 47x47 ring (x and z each +-9..23) at floor and
     * head height, far outside the 13x13 arena. The GameTest grid puts the next arena 18 blocks east and the next row
     * 19 blocks south, so the diagonal neighbour's (3, 1, 3) lies inside that ring. Once W07's tests changed the batch
     * layout, {@code W03GameTests.crystalFurnaceSmeltsInOneHundredFiftyTicks} landed there and its furnace became air
     * at tick 0 - proven by a stack trace from {@code CrystalFurnace.onRemove} pointing at the {@code setBlockAndUpdate}
     * below. In a batch of its own the ring can only hit arenas whose tests are already finished.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "w02_rtp")
    public static void rtpBlockRelocatesAServerPlayer(GameTestHelper helper) {
        BlockPos rel = new BlockPos(3, 1, 3);
        helper.setBlock(rel, ModBlocks.BLOCKTELEPORT.get());
        BlockPos abs = helper.absolutePos(rel);
        // A floor in the four target squares (x, z each +-9..23), one below the block, so the test does not depend
        // on where the flat test world's surface sits relative to the arena.
        for (int dx = -23; dx <= 23; dx++) {
            for (int dz = -23; dz <= 23; dz++) {
                if (Math.abs(dx) >= 9 && Math.abs(dz) >= 9) {
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        // PORT (BUGHUNT2 2.4): the teleport runs as a server task one tick after stepOn, so both players are measured
        // two ticks later; the sneaking check uses a second player, the first one is already queued.
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ServerPlayer sneaker = helper.makeMockServerPlayerInLevel();
        player.moveTo(abs.getX() + 0.5, abs.getY() + 1.0, abs.getZ() + 0.5, 0.0F, 0.0F);
        ModBlocks.BLOCKTELEPORT.get().stepOn(helper.getLevel(), abs, helper.getBlockState(rel), player);
        // 1.7.10 Entity.moveEntity skipped onEntityWalking for a sneaking player on the ground (Legacy.wasWalking).
        sneaker.moveTo(abs.getX() + 0.5, abs.getY() + 1.0, abs.getZ() + 0.5, 0.0F, 0.0F);
        sneaker.setShiftKeyDown(true);
        ModBlocks.BLOCKTELEPORT.get().stepOn(helper.getLevel(), abs, helper.getBlockState(rel), sneaker);
        helper.runAfterDelay(2, () -> {
            try {
                int dx = Math.abs(player.getBlockX() - abs.getX());
                int dz = Math.abs(player.getBlockZ() - abs.getZ());
                if (dx < 9 || dx > 23 || dz < 9 || dz > 23) {
                    helper.fail("player moved by (" + dx + ", " + dz + "), expected 9..23 on each axis");
                }
                if (!helper.getLevel().getBlockState(player.blockPosition().below()).isSolid()) {
                    helper.fail("no solid floor under the player after the teleport");
                }
                if (player.getXRot() != 0.0F) {
                    helper.fail("pitch was not reset to 0");
                }
                if (sneaker.getBlockX() != abs.getX() || sneaker.getBlockZ() != abs.getZ()) {
                    helper.fail("a sneaking player was teleported to " + sneaker.blockPosition());
                }
            } finally {
                player.remove(Entity.RemovalReason.DISCARDED);
                sneaker.remove(Entity.RemovalReason.DISCARDED);
            }
            helper.succeed();
        });
    }

    /**
     * OreGenericEgg.java:20-26: {@code dropBlockAsItemWithChance} rolled {@code 5 + rand(3) + rand(3)}
     * experience with a 1/2 chance on every drop path - a harvest, {@code destroyBlock}, an explosion of
     * any cause - and NeoForge passes {@code dropExperience = false} on all of them but a player's explosion
     * (Legacy.dropXpOnBlockBreak). Lavafoam.java:109-115 gates the same roll on dimension -1; the game-test
     * level is the overworld, so its Nether branch is only pinned as "nothing here".
     */
    @GameTest(template = ARENA)
    public static void eggBlocksDropExperienceOnEveryDropPath(GameTestHelper helper) {
        BlockPos rel = new BlockPos(3, 3, 3);
        BlockPos abs = helper.absolutePos(rel);
        Block egg = ModBlocks.DRIED_EGGS.get("orespider").get();
        int none = 0;
        int some = 0;
        for (int i = 0; i < 60; i++) {
            helper.setBlock(rel, egg);
            helper.getLevel().destroyBlock(abs, true);
            int xp = collectExperience(helper, rel);
            if (xp == 0) {
                none++;
            } else if (xp >= 5 && xp <= 9) {
                some++;
            } else {
                helper.fail("destroyBlock on orespider gave " + xp + " experience, expected 0 or 5..9");
            }
        }
        if (none == 0 || some == 0) {
            helper.fail("destroyBlock on orespider gave experience " + some + " times in 60, expected about 30 (1 in 2)");
        }
        // An explosion nobody set off: BlockBehaviour.onExplosionHit passes false, the original still rolled.
        none = 0;
        some = 0;
        for (int i = 0; i < 60; i++) {
            helper.setBlock(rel, egg);
            helper.getLevel().explode(null, abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.BLOCK);
            if (!helper.getBlockState(rel).isAir()) {
                helper.fail("the explosion left the egg block standing: " + helper.getBlockState(rel));
            }
            int xp = collectExperience(helper, rel);
            if (xp == 0) {
                none++;
            } else if (xp >= 5 && xp <= 9) {
                some++;
            } else {
                helper.fail("an explosion on orespider gave " + xp + " experience, expected 0 or 5..9");
            }
        }
        if (none == 0 || some == 0) {
            helper.fail("an explosion on orespider gave experience " + some + " times in 60, expected about 30 (1 in 2)");
        }
        for (int i = 0; i < 20; i++) {
            helper.setBlock(rel, ModBlocks.LAVAFOAM.get());
            helper.getLevel().destroyBlock(abs, true);
            int xp = collectExperience(helper, rel);
            if (xp != 0) {
                helper.fail("lavafoam gave " + xp + " experience outside the Nether");
            }
        }
        helper.succeed();
    }

    /**
     * Sum of the experience orbs within two blocks of {@code rel}; the orbs and any dropped items are removed.
     * {@code ExperienceOrb.award} merges same-value orbs spawned at one spot into one orb with a
     * {@code count} (5 = 3 + 1 + 1 becomes a 3 and a 1 x2), and {@code count} has no getter - only the
     * {@code Count} NBT key.
     */
    private static int collectExperience(GameTestHelper helper, BlockPos rel) {
        int xp = 0;
        for (ExperienceOrb orb : helper.getEntities(EntityType.EXPERIENCE_ORB, rel, 2.0)) {
            xp += orb.getValue() * Math.max(1, orb.saveWithoutId(new CompoundTag()).getInt("Count"));
            orb.discard();
        }
        helper.getEntities(EntityType.ITEM, rel, 2.0).forEach(Entity::discard);
        return xp;
    }

    // ------------------------------------------------------------------ w02-crops

    /** BlockLettuce.java:24-47: at age 4 the random tick replaces lettuce_0 by lettuce_1 with age 0. */
    @GameTest(template = ARENA)
    public static void lettuceAdvancesAStageAtAgeFour(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.FARMLAND);
        helper.setBlock(rel, ModBlocks.LETTUCE_0.get().defaultBlockState().setValue(BlockLettuce.AGE, 3));
        BlockPos abs = helper.absolutePos(rel);
        BlockState state = helper.getBlockState(rel);
        state.randomTick(helper.getLevel(), abs, helper.getLevel().random);
        state = helper.getBlockState(rel);
        if (!state.is(ModBlocks.LETTUCE_0.get()) || state.getValue(BlockLettuce.AGE) != 4) {
            helper.fail("age 3 + one tick gave " + state + ", expected lettuce_0 age 4");
        }
        state.randomTick(helper.getLevel(), abs, helper.getLevel().random);
        state = helper.getBlockState(rel);
        if (!state.is(ModBlocks.LETTUCE_1.get()) || state.getValue(BlockLettuce.AGE) != 0) {
            helper.fail("age 4 + one tick gave " + state + ", expected lettuce_1 age 0");
        }
        helper.succeed();
    }

    /** Unripe harvest keeps the full yield (R18): radish_plant at age 0 drops 2-5 radish, strawberry_plant only seeds. */
    @GameTest(template = ARENA)
    public static void unripeCropsDropLikeTheOriginal(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.FARMLAND);
        helper.setBlock(rel, ModBlocks.RADISH_PLANT.get());
        BlockPos abs = helper.absolutePos(rel);
        for (int i = 0; i < 20; i++) {
            List<ItemStack> drops = Block.getDrops(helper.getBlockState(rel), helper.getLevel(), abs, null);
            int radish = drops.stream().filter(s -> s.is(item("radish"))).mapToInt(ItemStack::getCount).sum();
            if (radish < 2 || radish > 5 || drops.stream().anyMatch(s -> !s.is(item("radish")))) {
                helper.fail("radish_plant age 0 dropped " + drops + ", expected 2..5 radish");
            }
        }
        helper.setBlock(rel, ModBlocks.STRAWBERRY_PLANT.get());
        for (int i = 0; i < 20; i++) {
            List<ItemStack> drops = Block.getDrops(helper.getBlockState(rel), helper.getLevel(), abs, null);
            int seeds = drops.stream().filter(s -> s.is(item("strawberry_seed"))).mapToInt(ItemStack::getCount).sum();
            if (seeds < 1 || seeds > 5 || drops.stream().anyMatch(s -> !s.is(item("strawberry_seed")))) {
                helper.fail("strawberry_plant age 0 dropped " + drops + ", expected 1..5 strawberry_seed");
            }
        }
        helper.succeed();
    }

    private static BlockHitResult hitTop(BlockPos abs) {
        return new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
    }

    /** BlockPizza.java:88-99: a bite is 4 hunger, 0.2 saturation and one slice; the sixth removes the block. */
    @GameTest(template = ARENA)
    public static void pizzaFeedsFourPerSliceAndVanishesAfterSix(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.STONE);
        helper.setBlock(rel, ModBlocks.PIZZA.get());
        BlockPos abs = helper.absolutePos(rel);
        // 1.7.10 canEat(false) was needFood() && !capabilities.disableDamage: a creative player never ate a slice.
        Player creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().invulnerable = true;
        creative.getFoodData().setFoodLevel(10);
        helper.getBlockState(rel).useItemOn(ItemStack.EMPTY, helper.getLevel(), creative, InteractionHand.MAIN_HAND, hitTop(abs));
        if (helper.getBlockState(rel).getValue(BlockPizza.BITES) != 0 || creative.getFoodData().getFoodLevel() != 10) {
            helper.fail("a creative player ate a slice: " + helper.getBlockState(rel) + ", food " + creative.getFoodData().getFoodLevel());
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        helper.getBlockState(rel).useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        BlockState state = helper.getBlockState(rel);
        if (!state.is(ModBlocks.PIZZA.get()) || state.getValue(BlockPizza.BITES) != 1) {
            helper.fail("one bite gave " + state + ", expected pizza bites=1");
        }
        if (player.getFoodData().getFoodLevel() != 14) {
            helper.fail("food level after one slice is " + player.getFoodData().getFoodLevel() + ", expected 14");
        }
        for (int i = 0; i < 5; i++) {
            player.getFoodData().setFoodLevel(10);
            helper.getBlockState(rel).useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        }
        if (!helper.getBlockState(rel).isAir()) {
            helper.fail("six bites left " + helper.getBlockState(rel) + ", expected air");
        }
        helper.succeed();
    }

    /** BlockDuctTape.java:88-119: a damaged main-hand item loses maxDamage/6 damage per use; iron pickaxe 250 -> 41 per use. */
    @GameTest(template = ARENA)
    public static void ductTapeRepairsASixthPerUse(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.STONE);
        helper.setBlock(rel, ModBlocks.DUCTTAPE.get());
        BlockPos abs = helper.absolutePos(rel);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(100);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        helper.getBlockState(rel).useItemOn(pickaxe, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        if (pickaxe.getDamageValue() != 59) {
            helper.fail("damage after one use is " + pickaxe.getDamageValue() + ", expected 100 - 250/6 = 59");
        }
        BlockState state = helper.getBlockState(rel);
        if (!state.is(ModBlocks.DUCTTAPE.get()) || state.getValue(BlockDuctTape.USES) != 1) {
            helper.fail("one use gave " + state + ", expected ducttape uses=1");
        }
        // An undamaged item consumes nothing (:99-100).
        helper.getBlockState(rel).useItemOn(pickaxe, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        helper.getBlockState(rel).useItemOn(pickaxe, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        if (pickaxe.getDamageValue() != 0 || helper.getBlockState(rel).getValue(BlockDuctTape.USES) != 3) {
            helper.fail("three uses gave damage " + pickaxe.getDamageValue() + " and " + helper.getBlockState(rel));
        }
        helper.getBlockState(rel).useItemOn(pickaxe, helper.getLevel(), player, InteractionHand.MAIN_HAND, hitTop(abs));
        if (helper.getBlockState(rel).getValue(BlockDuctTape.USES) != 3) {
            helper.fail("an undamaged item consumed a use");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ w02-materials-food

    /** ItemPopcorn (1, 0.5): +1 hunger, +1.0 saturation (1.7.10 saturation = heal * modifier * 2). */
    @GameTest(template = ARENA)
    public static void popcornRestoresTheOriginalHunger(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        player.getFoodData().setSaturation(0.0F);
        player.eat(helper.getLevel(), new ItemStack(ModItems.POPCORN.get()));
        if (player.getFoodData().getFoodLevel() != 11) {
            helper.fail("popcorn gave food level " + player.getFoodData().getFoodLevel() + ", expected 11");
        }
        if (Math.abs(player.getFoodData().getSaturationLevel() - 1.0F) > EPS) {
            helper.fail("popcorn gave saturation " + player.getFoodData().getSaturationLevel() + ", expected 1.0");
        }
        helper.succeed();
    }

    /** ItemSunFish.java:34-41 (MyLove): edible at full hunger and six effects with the original amplifiers. */
    @GameTest(template = ARENA)
    public static void heartIsAlwaysEdibleAndAppliesSixEffects(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        ItemStack heart = new ItemStack(ModItems.HEART.get());
        if (!player.canEat(heart.getFoodProperties(player).canAlwaysEat())) {
            helper.fail("heart must be edible at food level 20 (setAlwaysEdible)");
        }
        player.eat(helper.getLevel(), heart);
        expectEffect(helper, player, MobEffects.REGENERATION, 6000, 3);
        expectEffect(helper, player, MobEffects.DAMAGE_BOOST, 6000, 2);
        expectEffect(helper, player, MobEffects.FIRE_RESISTANCE, 6000, 2);
        expectEffect(helper, player, MobEffects.DAMAGE_RESISTANCE, 6000, 1);
        expectEffect(helper, player, MobEffects.MOVEMENT_SPEED, 5000, 0);
        expectEffect(helper, player, MobEffects.JUMP, 5000, 0);
        if (player.getActiveEffects().size() != 6) {
            helper.fail("heart applied " + player.getActiveEffects().size() + " effects, expected 6");
        }
        helper.succeed();
    }

    private static void expectEffect(GameTestHelper helper, Player player,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
        MobEffectInstance instance = player.getEffect(effect);
        if (instance == null) {
            helper.fail("missing effect " + effect.getRegisteredName());
            return;
        }
        if (instance.getAmplifier() != amplifier || instance.getDuration() > duration || instance.getDuration() < duration - 5) {
            helper.fail(effect.getRegisteredName() + " is " + instance + ", expected duration " + duration + " amplifier " + amplifier);
        }
    }

    // ------------------------------------------------------------------ shared helpers for the block tests

    private static void tick(GameTestHelper helper, BlockPos rel) {
        helper.getBlockState(rel).randomTick(helper.getLevel(), helper.absolutePos(rel), helper.getLevel().random);
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static boolean onlyOf(List<ItemStack> drops, Set<Item> allowed) {
        return drops.stream().allMatch(s -> s.isEmpty() || allowed.contains(s.getItem()));
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    /** {@code rounds} rolls of the loot table: {@code item} within [min, max] and nothing else. */
    private static void expectDropRange(GameTestHelper helper, BlockState state, BlockPos abs, Item item,
            int min, int max, int rounds, String what) {
        for (int i = 0; i < rounds; i++) {
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), abs, null);
            int n = count(drops, item);
            if (n < min || n > max || !onlyOf(drops, Set.of(item))) {
                helper.fail(what + " dropped " + drops + ", expected " + min + ".." + max + " x " + name(item));
            }
        }
    }

    private static void expectNoDrops(GameTestHelper helper, BlockState state, BlockPos abs, String what) {
        List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), abs, null);
        if (drops.stream().anyMatch(s -> !s.isEmpty())) {
            helper.fail(what + " dropped " + drops + ", expected nothing");
        }
    }

    /**
     * A leaf block: without a tool only the class's fruit rolls (each pool at most once), with shears
     * exactly the leaf block - what 1.7.10 Forge's {@code IShearable} gave every {@code BlockLeaves}.
     * Silk Touch gives the leaf block where 1.7.10 {@code canSilkHarvest} ({@code renderAsNormalBlock()})
     * was true, and falls through to the fruit rolls for the crystal leaves, whose answer was a client
     * static that stayed 0 on the server (OreSpawnLeaves class comment).
     */
    private static void expectLeafDrops(GameTestHelper helper, BlockState state, BlockPos abs, Block leaf, Set<Item> fruit,
            boolean silkTouchGivesLeaf) {
        for (int i = 0; i < 20; i++) {
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), abs, null);
            if (!onlyOf(drops, fruit) || drops.stream().anyMatch(s -> s.getCount() > 1) || count(drops, leaf.asItem()) != 0) {
                helper.fail(name(leaf.asItem()) + " dropped " + drops + " by hand, expected only single " + fruit);
            }
        }
        List<ItemStack> sheared = Block.getDrops(state, helper.getLevel(), abs, null, null, new ItemStack(Items.SHEARS));
        if (count(sheared, leaf.asItem()) != 1 || !onlyOf(sheared, Set.of(leaf.asItem()))) {
            helper.fail(name(leaf.asItem()) + " dropped " + sheared + " to shears, expected exactly one leaf block");
        }
        ItemStack silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
        for (int i = 0; i < 20; i++) {
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), abs, null, null, silk);
            if (silkTouchGivesLeaf) {
                if (count(drops, leaf.asItem()) != 1 || !onlyOf(drops, Set.of(leaf.asItem()))) {
                    helper.fail(name(leaf.asItem()) + " dropped " + drops + " to Silk Touch, expected exactly one leaf block");
                }
            } else if (!onlyOf(drops, fruit) || count(drops, leaf.asItem()) != 0) {
                helper.fail(name(leaf.asItem()) + " dropped " + drops + " to Silk Touch, expected only the fruit rolls (canSilkHarvest was false)");
            }
        }
    }

    private static BlockHitResult hitFace(BlockPos abs, Direction face) {
        Vec3 centre = Vec3.atCenterOf(abs);
        return new BlockHitResult(centre.add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5), face, abs, false);
    }

    private static InteractionResult useOn(GameTestHelper helper, Player player, ItemStack stack, BlockHitResult hit) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }

    /** The 144 blocks the w02-ores test above covers; everything else under {@code orespawn:} is trees-crystal or crops. */
    private static Set<ResourceLocation> oresPorterIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (DeferredBlock<? extends Block> holder : List.of(
                ModBlocks.ORESALT, ModBlocks.OREURANIUM, ModBlocks.ORETITANIUM, ModBlocks.OREAMETHYST, ModBlocks.ORERUBY,
                ModBlocks.BLOCKTELEPORT, ModBlocks.LAVAFOAM, ModBlocks.BLOCKURANIUM, ModBlocks.BLOCKTITANIUM,
                ModBlocks.BLOCKRUBY, ModBlocks.BLOCKAMETHYST, ModBlocks.BLOCKENDERPEARL, ModBlocks.BLOCKEYEOFENDER,
                ModBlocks.MOLEDIRT, ModBlocks.BLOCKMOBZILLASCALE, ModBlocks.CRYSTALSTONE, ModBlocks.CRYSTALCOAL,
                ModBlocks.CRYSTALCRYSTAL, ModBlocks.CRYSTALPINK_BLOCK, ModBlocks.TIGERSEYE, ModBlocks.TIGERSEYE_BLOCK,
                ModBlocks.CRYSTALRAT, ModBlocks.CRYSTALFAIRY, ModBlocks.REDANTTROLL, ModBlocks.TERMITETROLL)) {
            ids.add(holder.getId());
        }
        ModBlocks.DRIED_EGGS.values().forEach(holder -> ids.add(holder.getId()));
        return ids;
    }

    /** Blocks whose loot table is the block itself, one stack of one. */
    private static final Set<String> SELF_DROPPING = Set.of(
            "crystalgrass", "crystalplanks", "crystaltreelog", "skytreelog", "duplicatortreelog", "experiencesapling",
            "flower_pink", "flower_blue", "flower_black", "flower_scary",
            "crystalflower_red", "crystalflower_green", "crystalflower_blue", "crystalflower_yellow",
            "crystalsapling", "crystalsapling3", "crystaltorch", "extremetorch");

    /**
     * Blocks the port registers without a block item. The seed, pizza and duct tape items are
     * {@code BlockItem}s of the stage-0 / crop / pizza / duct tape blocks ({@code ItemSeedFood},
     * {@code ItemPizza}, {@code ItemDuctTape}) and the {@code StandingAndWallBlockItem} of a torch
     * answers for both twins, so {@code asItem()} is set for all of those. Only the higher reed
     * stages have none. 1.7.10's {@code GameRegistry.registerBlock(block, name)} gave every block an
     * {@code ItemBlock} that no creative tab listed (OreSpawnMain.java:1798-1813); the port leaves
     * these twelve out - pinned here so the choice is visible, not accidental.
     */
    private static final Set<String> NO_BLOCK_ITEM = Set.of(
            "corn_1", "corn_2", "corn_3", "tomato_1", "tomato_2", "tomato_3",
            "quinoa_1", "quinoa_2", "quinoa_3", "lettuce_1", "lettuce_2", "lettuce_3");

    /** Reed stages below the ripe one, pizza and duct tape: {@code quantityDropped} 0. */
    private static final Set<String> DROP_NOTHING = Set.of(
            "corn_0", "corn_1", "corn_2", "tomato_0", "tomato_1", "tomato_2", "quinoa_0", "quinoa_1", "quinoa_2",
            "lettuce_0", "lettuce_1", "lettuce_2", "pizza", "ducttape");

    /**
     * Every w02-trees-crystal and w02-crops block can be set and its loot table yields what
     * {@code getItemDropped}/{@code quantityDropped}/{@code dropBlockAsItemWithChance} gave.
     * Ripe {@code BlockCrops} at age 7 add the {@code 3 + fortune} bonus rolls of {@code rand(15) <= 7}.
     */
    @GameTest(template = ARENA)
    public static void w02TreeCrystalAndCropBlocksPlaceAndDropTheOriginalItem(GameTestHelper helper) {
        Set<ResourceLocation> ores = oresPorterIds();
        // Blocks of later waves are not W02's; each wave that adds blocks lists them here so the
        // census below keeps pinning exactly the 50 trees-crystal and crop blocks.
        ores.add(ModBlocks.CRYSTALWORKBENCH.getId()); // W03
        ores.add(ModBlocks.CRYSTALFURNACE.getId());   // W03
        ores.add(ModBlocks.ANTBLOCK.getId());            // W05
        ores.add(ModBlocks.REDANTBLOCK.getId());         // W05
        ores.add(ModBlocks.RAINBOWANTBLOCK.getId());     // W05
        ores.add(ModBlocks.UNSTABLEANTBLOCK.getId());    // W05
        ores.add(ModBlocks.TERMITEBLOCK.getId());        // W05
        ores.add(ModBlocks.CRYSTALTERMITEBLOCK.getId()); // W05
        ores.add(ModBlocks.FIREFLY_PLANT.getId());       // W05
        ores.add(ModBlocks.BUTTERFLY_PLANT.getId());     // W05
        ores.add(ModBlocks.MOTH_PLANT.getId());          // W05
        ores.add(ModBlocks.MOSQUITO_PLANT.getId());      // W05
        ores.add(ModBlocks.ISLAND.getId());              // W08
        ores.add(ModBlocks.DUNGEONSPAWNER.getId());      // W12
        ores.add(ModBlocks.KRAKEN_REPELLENT.getId());      // W10
        ores.add(ModBlocks.KRAKEN_REPELLENT_WALL.getId()); // W10
        ores.add(ModBlocks.CREEPER_REPELLENT.getId());     // W10
        ores.add(ModBlocks.CREEPER_REPELLENT_WALL.getId());// W10
        ores.add(ModBlocks.KINGSPAWNER.getId());           // W10
        ores.add(ModBlocks.QUEENSPAWNER.getId());          // W10
        List<Block> rest = BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(OreSpawn.MOD_ID) && !ores.contains(e.getKey().location()))
                .map(java.util.Map.Entry::getValue)
                .toList();
        if (rest.size() != 50) {
            helper.fail("expected 50 trees-crystal + crop blocks besides the 144 ore-porter blocks, found " + rest.size());
        }
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.DIRT);
        BlockPos abs = helper.absolutePos(rel);
        Set<String> seen = new HashSet<>();
        for (Block block : rest) {
            String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
            BlockState state = block.defaultBlockState();
            if (block instanceof CropBlock crop) {
                state = crop.getStateForAge(crop.getMaxAge());
            }
            helper.setBlock(rel, state);
            state = helper.getBlockState(rel);
            if (!state.is(block)) {
                helper.fail(id + " could not be placed");
            }
            if ((block.asItem() == Items.AIR) != NO_BLOCK_ITEM.contains(id)) {
                helper.fail(id + (block.asItem() == Items.AIR ? " has no block item" : " has a block item the registry was not meant to give it"));
            }
            seen.add(id);
            switch (id) {
                case "corn_3" -> expectDropRange(helper, state, abs, item("corn_seed"), 1, 2, 20, id); // BlockCorn.java:92-96
                case "tomato_3" -> expectDropRange(helper, state, abs, item("tomato_seed"), 2, 5, 20, id); // BlockTomato.java:87-91
                case "quinoa_3" -> expectDropRange(helper, state, abs, item("quinoa"), 3, 5, 20, id); // BlockQuinoa.java:85-89
                case "lettuce_3" -> expectDropRange(helper, state, abs, item("lettuce_seed"), 2, 4, 20, id); // BlockLettuce.java:53-57
                // 2 + rand(4) plus three rolls of rand(15) <= 7 - 200 samples so a 9 (a fourth bonus seed) cannot hide.
                case "radish_plant" -> expectDropRange(helper, state, abs, item("radish"), 2, 8, 200, id + " age 7");
                case "rice_plant" -> expectDropRange(helper, state, abs, item("rice"), 2, 8, 200, id + " age 7");
                case "strawberry_plant" -> {
                    for (int i = 0; i < 200; i++) {
                        List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), abs, null);
                        int fruit = count(drops, item("strawberry"));
                        int seeds = count(drops, item("strawberry_seed"));
                        if (fruit < 1 || fruit > 5 || seeds > 3 || !onlyOf(drops, Set.of(item("strawberry"), item("strawberry_seed")))) {
                            helper.fail("strawberry_plant age 7 dropped " + drops + ", expected 1..5 strawberry and 0..3 strawberry_seed");
                        }
                    }
                }
                case "crystalsapling2" -> // the missing return in getItemDropped (BlockCrystalPlant.java:54-62), R18
                        expectDropRange(helper, state, abs, item("crystalsapling3"), 1, 1, 1, id);
                case "crystaltorch_wall" -> expectDropRange(helper, state, abs, item("crystaltorch"), 1, 1, 1, id);
                case "extremetorch_wall" -> expectDropRange(helper, state, abs, item("extremetorch"), 1, 1, 1, id);
                case "leaves_apple" -> expectLeafDrops(helper, state, abs, block,
                        Set.of(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, item("magicapple")), true);
                case "leaves_scary", "leaves_experience" -> expectLeafDrops(helper, state, abs, block, Set.of(), true);
                case "leaves_cherry" -> expectLeafDrops(helper, state, abs, block, Set.of(item("cherries")), true);
                case "leaves_peach" -> expectLeafDrops(helper, state, abs, block, Set.of(item("peach")), true);
                case "crystaltreeleaves" -> expectLeafDrops(helper, state, abs, block, Set.of(item("crystalapple"), item("crystalsapling")), false);
                case "crystaltreeleaves2" -> expectLeafDrops(helper, state, abs, block, Set.of(item("crystalapple"), item("crystalsapling2")), false);
                case "crystaltreeleaves3" -> expectLeafDrops(helper, state, abs, block, Set.of(item("crystalapple"), item("crystalsapling3")), false);
                default -> {
                    if (SELF_DROPPING.contains(id)) {
                        expectDropRange(helper, state, abs, block.asItem(), 1, 1, 1, id);
                    } else if (DROP_NOTHING.contains(id)) {
                        expectNoDrops(helper, state, abs, id);
                    } else {
                        helper.fail("no drop expectation for W02 block " + id + " - is it new?");
                    }
                }
            }
            helper.setBlock(rel, Blocks.AIR);
        }
        if (seen.size() != 50) {
            helper.fail("visited " + seen.size() + " distinct blocks, expected 50");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ growth

    /** BlockLettuce.java:24-47: five effective ticks per stage, lettuce_3 stays lettuce_3, then 2-4 seeds. */
    @GameTest(template = ARENA)
    public static void lettuceReachesStageFourInFifteenTicksAndDropsSeeds(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        helper.setBlock(rel.below(), Blocks.FARMLAND);
        helper.setBlock(rel, ModBlocks.LETTUCE_0.get());
        for (int i = 0; i < 15; i++) {
            tick(helper, rel);
        }
        BlockState state = helper.getBlockState(rel);
        if (!state.is(ModBlocks.LETTUCE_3.get()) || state.getValue(BlockLettuce.AGE) != 0) {
            helper.fail("15 ticks from lettuce_0 gave " + state + ", expected lettuce_3 age 0");
        }
        for (int i = 0; i < 10; i++) {
            tick(helper, rel);
        }
        if (!helper.getBlockState(rel).is(ModBlocks.LETTUCE_3.get())) {
            helper.fail("lettuce_3 changed into " + helper.getBlockState(rel) + ", the last stage has no successor");
        }
        expectDropRange(helper, helper.getBlockState(rel), helper.absolutePos(rel), item("lettuce_seed"), 2, 4, 20, "lettuce_3");
        helper.succeed();
    }

    private static Predicate<BlockState> anyOf(DeferredBlock<?>... holders) {
        return state -> {
            for (DeferredBlock<?> holder : holders) {
                if (state.is(holder.get())) {
                    return true;
                }
            }
            return false;
        };
    }

    /** The stalk from {@code base} upwards, as long as {@code isStalk} holds. */
    private static List<BlockState> stalk(GameTestHelper helper, BlockPos base, Predicate<BlockState> isStalk) {
        List<BlockState> out = new ArrayList<>();
        for (BlockPos p = base; isStalk.test(helper.getBlockState(p)); p = p.above()) {
            out.add(helper.getBlockState(p));
        }
        return out;
    }

    /**
     * Ticks the tip of a reed-like stalk (only the block with air above does anything,
     * BlockTomato.java:42-43) until a block of {@code ripe} appears or {@code maxTicks} pass.
     */
    private static List<BlockState> growStalk(GameTestHelper helper, BlockPos base, Predicate<BlockState> isStalk,
            Block ripe, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            BlockPos top = base;
            while (isStalk.test(helper.getBlockState(top.above()))) {
                top = top.above();
            }
            tick(helper, top);
            List<BlockState> stalk = stalk(helper, base, isStalk);
            if (stalk.stream().anyMatch(s -> s.is(ripe))) {
                return stalk;
            }
        }
        return stalk(helper, base, isStalk);
    }

    /**
     * BlockCorn.java:27-81: the stalk grows to the re-rolled target of 4..7 (R18), then ripens from
     * corn_1 over corn_2 to corn_3 - never the tip, never the base (:63). corn_3 drops 1-2 cobs.
     */
    @GameTest(template = ARENA)
    public static void cornGrowsAStalkAndRipensBelowTheTip(GameTestHelper helper) {
        BlockPos base = new BlockPos(2, 2, 2);
        helper.setBlock(base.below(), Blocks.FARMLAND);
        helper.setBlock(base, ModBlocks.CORN_0.get());
        Predicate<BlockState> isCorn = anyOf(ModBlocks.CORN_0, ModBlocks.CORN_1, ModBlocks.CORN_2, ModBlocks.CORN_3);
        List<BlockState> stalk = growStalk(helper, base, isCorn, ModBlocks.CORN_3.get(), 400);
        if (stalk.stream().noneMatch(s -> s.is(ModBlocks.CORN_3.get()))) {
            helper.fail("no corn_3 after 400 tip ticks, stalk is " + stalk);
        }
        if (stalk.size() < 4 || stalk.size() > 7) {
            helper.fail("corn stalk is " + stalk.size() + " high, expected 4..7 (myMaxHeight = 4 + rand(4))");
        }
        if (!stalk.get(stalk.size() - 1).is(ModBlocks.CORN_0.get())) {
            helper.fail("the tip is " + stalk.get(stalk.size() - 1) + ", expected corn_0 (the ripening loop skips it)");
        }
        BlockPos ripe = null;
        for (BlockPos p = base; isCorn.test(helper.getBlockState(p)); p = p.above()) {
            if (helper.getBlockState(p).is(ModBlocks.CORN_3.get())) {
                ripe = p;
            }
        }
        expectDropRange(helper, helper.getBlockState(ripe), helper.absolutePos(ripe), item("corn_seed"), 1, 2, 20, "corn_3");
        BlockPos tip = base.above(stalk.size() - 1);
        expectNoDrops(helper, helper.getBlockState(tip), helper.absolutePos(tip), "corn_0 tip");
        helper.succeed();
    }

    /** BlockTomato.java:27-81: target 3..5, ripening from this block down (:63) leaves the tomato_0 tip. */
    @GameTest(template = ARENA)
    public static void tomatoGrowsAStalkAndRipensToStageFour(GameTestHelper helper) {
        BlockPos base = new BlockPos(2, 2, 2);
        helper.setBlock(base.below(), Blocks.FARMLAND);
        helper.setBlock(base, ModBlocks.TOMATO_0.get());
        Predicate<BlockState> isTomato = anyOf(ModBlocks.TOMATO_0, ModBlocks.TOMATO_1, ModBlocks.TOMATO_2, ModBlocks.TOMATO_3);
        List<BlockState> stalk = growStalk(helper, base, isTomato, ModBlocks.TOMATO_3.get(), 400);
        if (stalk.stream().noneMatch(s -> s.is(ModBlocks.TOMATO_3.get()))) {
            helper.fail("no tomato_3 after 400 tip ticks, stalk is " + stalk);
        }
        if (stalk.size() < 3 || stalk.size() > 5) {
            helper.fail("tomato stalk is " + stalk.size() + " high, expected 3..5 (myMaxHeight = 3 + rand(3))");
        }
        if (!stalk.get(stalk.size() - 1).is(ModBlocks.TOMATO_0.get())) {
            helper.fail("the tip is " + stalk.get(stalk.size() - 1) + ", expected tomato_0");
        }
        BlockPos ripe = null;
        for (BlockPos p = base; isTomato.test(helper.getBlockState(p)); p = p.above()) {
            if (helper.getBlockState(p).is(ModBlocks.TOMATO_3.get())) {
                ripe = p;
            }
        }
        expectDropRange(helper, helper.getBlockState(ripe), helper.absolutePos(ripe), item("tomato_seed"), 2, 5, 20, "tomato_3");
        helper.succeed();
    }

    /** BlockQuinoa.java:27-79: target 2..4, only the ticking tip ripens, quinoa_0 -> quinoa_2 -> quinoa_3 (:63-69). */
    @GameTest(template = ARENA)
    public static void quinoaGrowsOnCrystalGrassAndRipensAtTheTip(GameTestHelper helper) {
        BlockPos base = new BlockPos(2, 2, 2);
        helper.setBlock(base.below(), ModBlocks.CRYSTAL_GRASS.get());
        helper.setBlock(base, ModBlocks.QUINOA_0.get());
        Predicate<BlockState> isQuinoa = anyOf(ModBlocks.QUINOA_0, ModBlocks.QUINOA_1, ModBlocks.QUINOA_2, ModBlocks.QUINOA_3);
        List<BlockState> stalk = growStalk(helper, base, isQuinoa, ModBlocks.QUINOA_3.get(), 400);
        if (stalk.isEmpty() || !stalk.get(stalk.size() - 1).is(ModBlocks.QUINOA_3.get())) {
            helper.fail("the tip is not quinoa_3 after 400 tip ticks, stalk is " + stalk);
        }
        if (stalk.size() > 4) {
            helper.fail("quinoa stalk is " + stalk.size() + " high, expected at most 4 (myMaxHeight = 2 + rand(3))");
        }
        for (int i = 0; i < stalk.size() - 1; i++) {
            if (!stalk.get(i).is(ModBlocks.QUINOA_1.get())) {
                helper.fail("stalk block " + i + " is " + stalk.get(i) + ", expected quinoa_1 - the lower blocks never ripen");
            }
        }
        BlockPos tip = base.above(stalk.size() - 1);
        expectDropRange(helper, helper.getBlockState(tip), helper.absolutePos(tip), item("quinoa"), 3, 5, 20, "quinoa_3");
        // quinoa_3 does not tick (:33-35): nothing changes any more.
        tick(helper, tip);
        if (!helper.getBlockState(tip).is(ModBlocks.QUINOA_3.get())) {
            helper.fail("quinoa_3 ticked into " + helper.getBlockState(tip));
        }
        helper.succeed();
    }

    /**
     * The three {@code BlockCrops} grow like vanilla: bonemeal adds 2..5 age per use up to 7. Rice on
     * Crystal Grass (its planting soil), radish and strawberry on farmland. At age 7 the loot table
     * adds the {@code 3 + fortune} bonus rolls of {@code rand(15) <= 7} (vanilla BlockCrops 1.7.10).
     */
    @GameTest(template = ARENA)
    public static void cropBlocksGrowWithBonemealToAgeSevenAndDropTheirYield(GameTestHelper helper) {
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = helper.absolutePos(rel);
        record Crop(DeferredBlock<? extends Block> block, Block soil) {}
        for (Crop crop : List.of(
                new Crop(ModBlocks.RADISH_PLANT, Blocks.FARMLAND),
                new Crop(ModBlocks.RICE_PLANT, ModBlocks.CRYSTAL_GRASS.get()),
                new Crop(ModBlocks.STRAWBERRY_PLANT, Blocks.FARMLAND))) {
            helper.setBlock(rel.below(), crop.soil());
            helper.setBlock(rel, crop.block().get());
            CropBlock block = (CropBlock) crop.block().get();
            BlockState state = helper.getBlockState(rel);
            if (block.getAge(state) != 0) {
                helper.fail(crop.block().getId() + " placed as " + state + ", expected age 0");
            }
            int uses = 0;
            while (!block.isMaxAge(state) && uses < 10) {
                if (!block.isValidBonemealTarget(helper.getLevel(), abs, state)) {
                    helper.fail(crop.block().getId() + " at age " + block.getAge(state) + " refuses bonemeal");
                }
                block.performBonemeal(helper.getLevel(), helper.getLevel().random, abs, state);
                state = helper.getBlockState(rel);
                uses++;
            }
            if (block.getAge(state) != 7 || uses < 2 || uses > 4) {
                helper.fail(crop.block().getId() + " reached age " + block.getAge(state) + " after " + uses + " bonemeal uses, expected 7 in 2..4");
            }
            if (block.isValidBonemealTarget(helper.getLevel(), abs, state)) {
                helper.fail(crop.block().getId() + " at age 7 still accepts bonemeal");
            }
        }
        // The last crop in the loop is the strawberry at age 7.
        BlockState ripe = helper.getBlockState(rel);
        for (int i = 0; i < 200; i++) {
            List<ItemStack> drops = Block.getDrops(ripe, helper.getLevel(), abs, null);
            int fruit = count(drops, item("strawberry"));
            int seeds = count(drops, item("strawberry_seed"));
            if (fruit < 1 || fruit > 5 || seeds > 3 || !onlyOf(drops, Set.of(item("strawberry"), item("strawberry_seed")))) {
                helper.fail("ripe strawberry_plant dropped " + drops + ", expected 1..5 strawberry and 0..3 strawberry_seed");
            }
        }
        helper.setBlock(rel, ((CropBlock) ModBlocks.RADISH_PLANT.get()).getStateForAge(7));
        expectDropRange(helper, helper.getBlockState(rel), abs, item("radish"), 2, 8, 200, "ripe radish_plant");
        helper.succeed();
    }

    /**
     * ItemSeedFood.onItemUse (top face, farmland or Crystal Grass, air above): the crop appears and
     * one seed is consumed. Crystal Grass sustains everything (CrystalGrass.java:58-60); plain stone
     * nothing.
     */
    @GameTest(template = ARENA)
    public static void seedItemsPlantTheirCropOnTheirSoil(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos soilRel = new BlockPos(2, 1, 2);
        BlockPos soilAbs = helper.absolutePos(soilRel);
        record Planting(String seed, Block soil, DeferredBlock<? extends Block> crop) {}
        for (Planting p : List.of(
                new Planting("radish", Blocks.FARMLAND, ModBlocks.RADISH_PLANT),
                new Planting("rice", ModBlocks.CRYSTAL_GRASS.get(), ModBlocks.RICE_PLANT),
                new Planting("strawberry_seed", Blocks.FARMLAND, ModBlocks.STRAWBERRY_PLANT),
                new Planting("corn_seed", Blocks.FARMLAND, ModBlocks.CORN_0),
                new Planting("quinoa", ModBlocks.CRYSTAL_GRASS.get(), ModBlocks.QUINOA_0),
                new Planting("tomato_seed", Blocks.FARMLAND, ModBlocks.TOMATO_0),
                new Planting("lettuce_seed", Blocks.FARMLAND, ModBlocks.LETTUCE_0),
                new Planting("radish", ModBlocks.CRYSTAL_GRASS.get(), ModBlocks.RADISH_PLANT))) {
            helper.setBlock(soilRel, p.soil());
            helper.setBlock(soilRel.above(), Blocks.AIR);
            ItemStack stack = new ItemStack(item(p.seed()), 2);
            InteractionResult result = useOn(helper, player, stack, hitFace(soilAbs, Direction.UP));
            BlockState planted = helper.getBlockState(soilRel.above());
            if (!planted.is(p.crop().get())) {
                helper.fail(p.seed() + " on " + p.soil() + " gave " + planted + " (" + result + "), expected " + p.crop().getId());
            }
            if (stack.getCount() != 1) {
                helper.fail(p.seed() + " left " + stack.getCount() + " in the stack, expected one seed consumed");
            }
        }
        helper.setBlock(soilRel, Blocks.STONE);
        helper.setBlock(soilRel.above(), Blocks.AIR);
        ItemStack seed = new ItemStack(item("strawberry_seed"), 2);
        InteractionResult onStone = useOn(helper, player, seed, hitFace(soilAbs, Direction.UP));
        if (onStone.consumesAction() || !helper.getBlockState(soilRel.above()).isAir() || seed.getCount() != 2) {
            helper.fail("strawberry_seed on stone gave " + onStone + " and " + helper.getBlockState(soilRel.above()));
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ leaves

    /**
     * The four leaf classes share one search (BlockAppleLeaves.java:46-79 etc.): a log at Manhattan
     * distance 3 or less, at or below the leaf, sustains it; otherwise the leaf drops and vanishes on
     * its random tick. No distance property, no persistence: a leaf placed without a log decays.
     * {@code leaves_scary} turns into apple leaves by day when sustained (:59-63), so the scary class
     * is exercised through {@code leaves_cherry}.
     */
    @GameTest(template = ARENA)
    public static void leavesWithoutALogWithinReachDecay(GameTestHelper helper) {
        BlockPos rel = new BlockPos(6, 5, 6);
        for (DeferredBlock<? extends Block> leaves : List.of(ModBlocks.LEAVES_APPLE, ModBlocks.LEAVES_CHERRY,
                ModBlocks.LEAVES_EXPERIENCE, ModBlocks.CRYSTAL_TREE_LEAVES)) {
            String id = leaves.getId().getPath();
            helper.setBlock(rel, leaves.get());
            tick(helper, rel);
            if (!helper.getBlockState(rel).isAir()) {
                helper.fail(id + " survived a random tick with no log within reach");
            }
            // A crystal tree log 2 down and 1 aside (Manhattan 3) sustains it.
            BlockPos log = rel.below(2).east();
            helper.setBlock(rel, leaves.get());
            helper.setBlock(log, ModBlocks.CRYSTAL_TREE_LOG.get());
            tick(helper, rel);
            if (!helper.getBlockState(rel).is(leaves.get())) {
                helper.fail(id + " decayed next to a crystal tree log at distance 3");
            }
            helper.setBlock(log, Blocks.AIR);
            // Any vanilla log counts (1.7.10 BlockLog.canSustainLeaves).
            helper.setBlock(rel.west(2), Blocks.OAK_LOG);
            tick(helper, rel);
            if (!helper.getBlockState(rel).is(leaves.get())) {
                helper.fail(id + " decayed next to an oak log two blocks west");
            }
            helper.setBlock(rel.west(2), Blocks.AIR);
            // A log above does not count: the search covers only the layers at and below the leaf.
            helper.setBlock(rel.above(), Blocks.OAK_LOG);
            tick(helper, rel);
            if (!helper.getBlockState(rel).isAir()) {
                helper.fail(id + " was sustained by a log above it, which the original never looked at");
            }
            helper.setBlock(rel.above(), Blocks.AIR);
            // Manhattan distance 4 (2 down, 2 aside) is out of reach.
            helper.setBlock(rel, leaves.get());
            helper.setBlock(rel.below(2).east(2), Blocks.OAK_LOG);
            tick(helper, rel);
            if (!helper.getBlockState(rel).isAir()) {
                helper.fail(id + " was sustained by a log at Manhattan distance 4");
            }
            helper.setBlock(rel.below(2).east(2), Blocks.AIR);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ torches

    /**
     * One {@code StandingAndWallBlockItem} per torch: the top face gives the standing block, a side
     * face the {@code _wall} twin facing away from its wall; both fall when the support goes. The
     * crystal torch also hangs on the crystal blocks its class names (BlockCrystalTorch.java:50-53),
     * and an extreme torch placed on an Eye of Ender block removes itself after the summoning
     * (BlockExtremeTorch.java:59-107; the Cephadrome itself is W09).
     */
    @GameTest(template = ARENA)
    public static void torchesStandOnTopAndHangOnWalls(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // The template is loaded one block above the structure block: the arena's stone floor is
        // relative y = 1. The fixtures sit at y = 2 so their side faces meet air, not floor.
        BlockPos stoneRel = new BlockPos(3, 2, 3);
        BlockPos stoneAbs = helper.absolutePos(stoneRel);
        record Torch(Item item, DeferredBlock<? extends Block> standing, DeferredBlock<? extends Block> wall) {}
        for (Torch torch : List.of(
                new Torch(ModItems.CRYSTAL_TORCH.get(), ModBlocks.CRYSTAL_TORCH, ModBlocks.CRYSTAL_TORCH_WALL),
                new Torch(ModItems.EXTREME_TORCH.get(), ModBlocks.EXTREME_TORCH, ModBlocks.EXTREME_TORCH_WALL))) {
            String id = name(torch.item());
            helper.setBlock(stoneRel, Blocks.STONE);
            ItemStack stack = new ItemStack(torch.item(), 2);
            useOn(helper, player, stack, hitFace(stoneAbs, Direction.UP));
            if (!helper.getBlockState(stoneRel.above()).is(torch.standing().get())) {
                helper.fail(id + " on the top face gave " + helper.getBlockState(stoneRel.above()) + ", expected " + torch.standing().getId());
            }
            if (stack.getCount() != 1) {
                helper.fail(id + " left " + stack.getCount() + " in the stack after placing");
            }
            InteractionResult hung = useOn(helper, player, stack, hitFace(stoneAbs, Direction.NORTH));
            BlockState wall = helper.getBlockState(stoneRel.north());
            if (!wall.is(torch.wall().get()) || wall.getValue(WallTorchBlock.FACING) != Direction.NORTH) {
                helper.fail(id + " on the north face gave " + wall + " (" + hung + "), expected " + torch.wall().getId()
                        + " facing north; around the stone: N=" + helper.getBlockState(stoneRel.north())
                        + " S=" + helper.getBlockState(stoneRel.south()) + " E=" + helper.getBlockState(stoneRel.east())
                        + " W=" + helper.getBlockState(stoneRel.west()) + " up=" + helper.getBlockState(stoneRel.above())
                        + " at=" + helper.getBlockState(stoneRel) + ", stack " + stack.getCount());
            }
            helper.setBlock(stoneRel, Blocks.AIR);
            if (!helper.getBlockState(stoneRel.above()).isAir() || !helper.getBlockState(stoneRel.north()).isAir()) {
                helper.fail(id + " kept standing after its support was removed: " + helper.getBlockState(stoneRel.above())
                        + " / " + helper.getBlockState(stoneRel.north()));
            }
        }
        // On stone a neighbour update leaves the crystal torch where it is (BlockTorch.onNeighborBlockChange, vanilla check passes).
        helper.setBlock(stoneRel, Blocks.STONE);
        useOn(helper, player, new ItemStack(ModItems.CRYSTAL_TORCH.get()), hitFace(stoneAbs, Direction.UP));
        helper.setBlock(stoneRel.above().east(), Blocks.STONE);
        if (!helper.getBlockState(stoneRel.above()).is(ModBlocks.CRYSTAL_TORCH.get())) {
            helper.fail("crystal torch on stone fell off on a neighbour update: " + helper.getBlockState(stoneRel.above()));
        }
        helper.setBlock(stoneRel.above().east(), Blocks.AIR);
        // Crystal Planks is a crystal block: the crystal torch stands on it and hangs on it ...
        BlockPos planksRel = new BlockPos(8, 2, 3);
        BlockPos planksAbs = helper.absolutePos(planksRel);
        helper.setBlock(planksRel, ModBlocks.CRYSTAL_PLANKS.get());
        useOn(helper, player, new ItemStack(ModItems.CRYSTAL_TORCH.get()), hitFace(planksAbs, Direction.UP));
        useOn(helper, player, new ItemStack(ModItems.CRYSTAL_TORCH.get()), hitFace(planksAbs, Direction.WEST));
        if (!helper.getBlockState(planksRel.above()).is(ModBlocks.CRYSTAL_TORCH.get())) {
            helper.fail("crystal torch does not stand on crystal planks: " + helper.getBlockState(planksRel.above()));
        }
        BlockState onPlanks = helper.getBlockState(planksRel.west());
        if (!onPlanks.is(ModBlocks.CRYSTAL_TORCH_WALL.get()) || onPlanks.getValue(WallTorchBlock.FACING) != Direction.WEST) {
            helper.fail("crystal torch does not hang on crystal planks: " + onPlanks);
        }
        // ... until the next neighbour update, when the inherited vanilla side check drops both as items
        // (BlockCrystalTorch class comment, R18). One stone above the wall torch neighbours both torches.
        helper.setBlock(planksRel.west().above(), Blocks.STONE);
        if (!helper.getBlockState(planksRel.above()).isAir() || !helper.getBlockState(planksRel.west()).isAir()) {
            helper.fail("crystal torches on crystal planks survived a neighbour update: "
                    + helper.getBlockState(planksRel.above()) + " / " + helper.getBlockState(planksRel.west()));
        }
        helper.assertItemEntityCountIs(ModItems.CRYSTAL_TORCH.get(), planksRel.above(), 2.0, 2);
        helper.setBlock(planksRel.west().above(), Blocks.AIR);
        // Extreme torch on an Eye of Ender block: the search finds a spot on the arena floor 2..6 blocks
        // away (solid below, two air above) and the torch goes.
        BlockPos eyeRel = new BlockPos(6, 2, 6);
        helper.setBlock(eyeRel, ModBlocks.BLOCKEYEOFENDER.get());
        ItemStack extreme = new ItemStack(ModItems.EXTREME_TORCH.get(), 2);
        InteractionResult summoned = useOn(helper, player, extreme, hitFace(helper.absolutePos(eyeRel), Direction.UP));
        if (!summoned.consumesAction() || extreme.getCount() != 1) {
            helper.fail("extreme torch on the eye block was not placed: " + summoned + ", " + extreme.getCount() + " left");
        }
        if (!helper.getBlockState(eyeRel.above()).isAir()) {
            helper.fail("extreme torch on an Eye of Ender block stayed: " + helper.getBlockState(eyeRel.above()));
        }
        if (!helper.getBlockState(eyeRel).is(ModBlocks.BLOCKEYEOFENDER.get())) {
            helper.fail("the Eye of Ender block itself was changed: " + helper.getBlockState(eyeRel));
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ sky tree log

    /**
     * BlockSkyTreeLog.java:40-69: breaking one log walks the 26-neighbourhood recursively, turning
     * every connected sky tree log into air with one log dropped each, plus one for the broken block.
     * A log not touching the tree stays.
     */
    @GameTest(template = ARENA)
    public static void skyTreeLogFellsTheConnectedTree(GameTestHelper helper) {
        Block log = ModBlocks.SKY_TREE_LOG.get();
        List<BlockPos> tree = List.of(new BlockPos(4, 1, 4), new BlockPos(4, 2, 4), new BlockPos(4, 3, 4),
                new BlockPos(4, 4, 4), new BlockPos(5, 5, 4), new BlockPos(6, 6, 5), new BlockPos(6, 7, 6));
        for (BlockPos p : tree) {
            helper.setBlock(p, log);
        }
        BlockPos loner = new BlockPos(9, 1, 9);
        helper.setBlock(loner, log);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos base = tree.get(0);
        BlockPos baseAbs = helper.absolutePos(base);
        log.onDestroyedByPlayer(helper.getBlockState(base), helper.getLevel(), baseAbs, player, true,
                helper.getLevel().getFluidState(baseAbs));
        for (BlockPos p : tree) {
            if (!helper.getBlockState(p).isAir()) {
                helper.fail("sky tree log at " + p + " survived the felling: " + helper.getBlockState(p));
            }
        }
        if (!helper.getBlockState(loner).is(log)) {
            helper.fail("the unconnected sky tree log was felled too");
        }
        helper.assertItemEntityCountIs(log.asItem(), new BlockPos(6, 4, 6), 8.0, tree.size());
        helper.succeed();
    }

    // ------------------------------------------------------------------ foods

    /**
     * Every {@code ItemFood} of the wave with its {@code (healAmount, saturationModifier)} from
     * OreSpawnMain.java (:1371-1373, :1392, :1414-1419, :1504-1519, :1535-1536, :1547-1586).
     * 1.7.10 saturation = heal * modifier * 2, the same product 1.21.1 stores (see FoodValues).
     */
    private record Food(String id, int heal, float modifier, boolean alwaysEdible) {}

    private static final List<Food> FOODS = List.of(
            new Food("lavaeel", 2, 0.6f, true), new Food("firefish", 4, 0.6f, true), new Food("sunfish", 6, 0.6f, true),
            new Food("sparkfish", 1, 0.2f, true),
            new Food("greenfish", 3, 0.5f, false), new Food("bluefish", 4, 0.4f, false), new Food("pinkfish", 4, 0.6f, false),
            new Food("rockfish", 3, 0.7f, false), new Food("woodfish", 5, 0.7f, false), new Food("greyfish", 5, 0.5f, false),
            new Food("popcorn", 1, 0.5f, false), new Food("popcorn_buttered", 2, 0.6f, false),
            new Food("popcorn_buttered_salted", 3, 0.75f, false), new Food("popcorn_bag", 10, 1.25f, false),
            new Food("butter", 1, 0.5f, false), new Food("corndog_cooked", 16, 2.5f, false), new Food("corndog_raw", 4, 0.6f, false),
            new Food("buttercandy", 4, 0.5f, true), new Food("cookedbacon", 14, 1.5f, true), new Food("bacon", 8, 1.0f, false),
            new Food("cookedcrabmeat", 6, 0.75f, true), new Food("crabmeat", 4, 0.25f, false), new Food("cheese", 4, 0.5f, false),
            new Food("salad", 10, 0.95f, false), new Food("blt_sandwich", 12, 0.95f, false), new Food("crabbypatty", 16, 2.35f, false),
            new Food("cookedpeacock", 12, 1.4f, false), new Food("rawpeacock", 6, 0.7f, false),
            new Food("strawberry", 2, 0.65f, false), new Food("radish", 2, 0.45f, false), new Food("cherries", 3, 0.45f, false),
            new Food("peach", 4, 0.55f, false), new Food("crystalapple", 5, 0.85f, true), new Food("heart", 8, 0.95f, true),
            new Food("rice", 5, 0.65f, false), new Food("corn_seed", 6, 0.75f, false), new Food("quinoa", 7, 0.85f, false),
            new Food("tomato_seed", 4, 0.55f, false), new Food("lettuce_seed", 3, 0.45f, false));

    @GameTest(template = ARENA)
    public static void everyFoodCarriesTheOriginalHungerAndSaturation(GameTestHelper helper) {
        for (Food food : FOODS) {
            Item item = item(food.id());
            if (item == Items.AIR) {
                helper.fail("orespawn:" + food.id() + " is not registered");
                continue;
            }
            FoodProperties props = new ItemStack(item).get(DataComponents.FOOD);
            if (props == null) {
                helper.fail(food.id() + " is not edible");
                continue;
            }
            float saturation = food.heal() * food.modifier() * 2.0f;
            if (props.nutrition() != food.heal() || Math.abs(props.saturation() - saturation) > 1.0E-3f
                    || props.canAlwaysEat() != food.alwaysEdible()) {
                helper.fail(food.id() + " is " + props + ", expected nutrition " + food.heal() + ", saturation " + saturation
                        + ", alwaysEdible " + food.alwaysEdible());
            }
        }
        for (String notFood : List.of("strawberry_seed", "pizza_item", "ducttape_item", "appletree_seed", "salt")) {
            if (new ItemStack(item(notFood)).has(DataComponents.FOOD)) {
                helper.fail(notFood + " is edible, the original was not an ItemFood");
            }
        }
        // Eating goes through FoodData like 1.7.10 FoodStats.addStats: corn cob 6 hunger, 6 * 0.75 * 2 = 9 saturation.
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        player.getFoodData().setSaturation(0.0F);
        player.eat(helper.getLevel(), new ItemStack(item("corn_seed")));
        if (player.getFoodData().getFoodLevel() != 16 || Math.abs(player.getFoodData().getSaturationLevel() - 9.0F) > EPS) {
            helper.fail("corn cob gave food " + player.getFoodData().getFoodLevel() + " saturation "
                    + player.getFoodData().getSaturationLevel() + ", expected 16 and 9.0");
        }
        helper.succeed();
    }

    /** The fish and sweets add their effects on eating: durations and amplifiers from the ItemXxx.onFoodEaten sources. */
    @GameTest(template = ARENA)
    public static void fishAndSweetsApplyTheirOriginalEffects(GameTestHelper helper) {
        record Expect(String id, java.util.Map<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>, int[]> effects) {}
        List<Expect> table = List.of(
                new Expect("lavaeel", java.util.Map.of(MobEffects.FIRE_RESISTANCE, new int[] {600, 0})), // ItemLavaEel.java:17-22
                new Expect("firefish", java.util.Map.of(MobEffects.FIRE_RESISTANCE, new int[] {1200, 0})), // ItemFireFish.java:17-22
                new Expect("sparkfish", java.util.Map.of(MobEffects.FIRE_RESISTANCE, new int[] {100, 0})), // ItemSparkFish.java:17-22
                new Expect("sunfish", java.util.Map.of(MobEffects.FIRE_RESISTANCE, new int[] {6000, 0})), // ItemSunFish.java:19-21
                new Expect("buttercandy", java.util.Map.of(MobEffects.MOVEMENT_SPEED, new int[] {2000, 0}, MobEffects.JUMP, new int[] {2000, 0})),
                new Expect("cookedbacon", java.util.Map.of(MobEffects.REGENERATION, new int[] {2000, 0}, MobEffects.DAMAGE_BOOST, new int[] {2000, 0})),
                new Expect("crystalapple", java.util.Map.of(MobEffects.REGENERATION, new int[] {3000, 0}, MobEffects.DAMAGE_BOOST, new int[] {3000, 0})),
                new Expect("cookedcrabmeat", java.util.Map.of()));
        for (Expect expect : table) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getFoodData().setFoodLevel(20);
            ItemStack stack = new ItemStack(item(expect.id()));
            if (!player.canEat(stack.getFoodProperties(player).canAlwaysEat())) {
                helper.fail(expect.id() + " must be edible at food level 20 (setAlwaysEdible)");
            }
            player.eat(helper.getLevel(), stack);
            expect.effects().forEach((effect, spec) -> expectEffect(helper, player, effect, spec[0], spec[1]));
            if (player.getActiveEffects().size() != expect.effects().size()) {
                helper.fail(expect.id() + " applied " + player.getActiveEffects() + ", expected " + expect.effects().size() + " effects");
            }
        }
        // ItemGenericFish: not always edible, Hunger 20 ticks one time in four (ItemGenericFish.java:16-21).
        int hungry = 0;
        for (int i = 0; i < 200; i++) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getFoodData().setFoodLevel(10);
            player.eat(helper.getLevel(), new ItemStack(item("greenfish")));
            MobEffectInstance hunger = player.getEffect(MobEffects.HUNGER);
            if (hunger != null) {
                hungry++;
                if (hunger.getAmplifier() != 0 || hunger.getDuration() > 20 || hunger.getDuration() < 15) {
                    helper.fail("greenfish gave " + hunger + ", expected Hunger 20 ticks amplifier 0");
                }
            }
        }
        if (hungry < 25 || hungry > 80) {
            helper.fail("greenfish gave Hunger " + hungry + " times in 200, expected about 50 (1 in 4)");
        }
        Player full = helper.makeMockPlayer(GameType.SURVIVAL);
        full.getFoodData().setFoodLevel(20);
        if (full.canEat(new ItemStack(item("greenfish")).getFoodProperties(full).canAlwaysEat())) {
            helper.fail("greenfish is edible at full hunger; the original had no setAlwaysEdible");
        }
        helper.succeed();
    }
}
