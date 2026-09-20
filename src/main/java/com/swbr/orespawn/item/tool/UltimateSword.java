package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.config.stats.WeaponStats;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModSounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.UltimateSword} (UltimateSword.java:19-310): one {@code ItemSword}
 * class for four weapons, told apart by identity against {@code OreSpawnMain.MyChainsaw} and
 * {@code MyBattleAxe} - here by {@link Variant}. All four: stack 1, {@code setMaxDamage(3000)}
 * (:29, overriding every material's {@code maxUses}), tab Combat (:30).
 *
 * <table>
 *   <tr><th>id</th><th>material</th><th>constructor</th></tr>
 *   <tr><td>{@code ultimatesword}</td><td>ULTIMATE</td><td>OreSpawnMain.java:1307</td></tr>
 *   <tr><td>{@code battleaxesmall}</td><td>BATTLE</td><td>:1317</td></tr>
 *   <tr><td>{@code chainsawsmall}</td><td>CHAINSAW</td><td>:1318</td></tr>
 *   <tr><td>{@code queenbattleaxesmall}</td><td>QUEENBATTLE</td><td>:1319</td></tr>
 * </table>
 * Attack {@code 4 + material} in 1.7.10: 40 / 50 / 60 / 666 (R6 conversion in
 * {@link OreSpawnTiers#sword}). The three big weapons render through a
 * {@code BlockEntityWithoutLevelRenderer} of w03-bigweapons (R8); the model JSONs of those ids have
 * the {@code builtin/entity} parent.
 *
 * <p><b>Blocking</b>: right-click blocks for 9000 ticks ({@code getMaxItemUseDuration}
 * :148-150, live in the jar - {@link BlockingSword}); {@code onUsingTick} ran every tick of it.
 *
 * <p><b>Enchantments</b> ({@code onCreated} :33-50, re-added when Looting is missing by
 * {@code onUsingTick} :60-80 while blocking and {@code onUpdate} :103-118 - R7), with
 * {@code M = UltimateSwordEnchantmentLevel} (default 5, clamped to 1..10, {@link TweakStats}):
 * Chainsaw none; Battle Axe Looting and Unbreaking {@code 1 + M/2}; Ultimate Sword and Queen
 * Battle Axe Sharpness, Smite and Bane of Arthropods {@code M}, Knockback, Looting and Unbreaking
 * {@code 1 + M/2}, Fire Aspect {@code 1 + M/3} (integer division).
 *
 * <p><b>Chainsaw</b>: the swing sound with a 50-tick timer (:52-58), spark, smoke and flame
 * particles while the timer runs (:83-101), the area attack on every left click (:142-144,
 * :157-248) and the 11x16x11 clear-cut on every mined block (:265-296).
 *
 * <p><b>Dead in the jar</b> (R18, "tote Kettensaegen-Methoden"): {@code getStrVsBlock} (:298-309,
 * wrong signature - would have set {@code leaf} and returned the chainsaw efficiency on wood and
 * plants) and {@code canHarvestBlock(Block)} (:250-252). Consequences kept: {@code leaf} never
 * becomes true, the chainsaw digs at vanilla sword speed and only cobweb counts as harvestable
 * (the {@code SwordItem} tool component). Also dead: the {@code EntityLiving}-typed
 * {@code hitEntity} (:125-128), {@code getMaterialName} (:121-123).
 *
 * <p><b>PvP guard</b> (:130-141, :181-191): {@link UltimatePvp}, Girlfriend/Boyfriend are W04.
 */
public class UltimateSword extends BlockingSword {

    /** Which {@code OreSpawnMain} field the original compared {@code this} against. */
    public enum Variant {
        /** {@code MyUltimateSword}. */
        ULTIMATE_SWORD,
        /** {@code MyBattleAxe}. */
        BATTLE_AXE,
        /** {@code MyChainsaw}. */
        CHAINSAW,
        /** {@code MyQueenBattleAxe}. */
        QUEEN_BATTLE_AXE
    }

    private final Variant variant;

    /**
     * {@code swingtimer} (:21). PORT: the original kept one counter on the item singleton, so on a
     * server one player's swing silenced every other chainsaw for 50 ticks (R18 case 2). Held per
     * swinging entity instead; client and server entities are different objects, so one map
     * serves both sides of an integrated server. Weak keys let unloaded entities go.
     */
    private final Map<Entity, Integer> swingtimer = Collections.synchronizedMap(new WeakHashMap<>());

    /** {@code leaf} (:22). Never set: the only writer, {@code getStrVsBlock}, was dead in the jar. */
    private boolean leaf = false;

    public UltimateSword(Variant variant, Material material, Item.Properties props) {
        super(material.withUses(3000), props.attributes(OreSpawnTiers.sword(material)), 9000);
        this.variant = variant;
    }

    /** {@code onUsingTick} (:60-80): nothing for the chainsaw, else the Looting-sentinel restore every tick of a block. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (variant == Variant.CHAINSAW) {
            return;
        }
        PreEnchant.restore(stack, level, Enchantments.LOOTING, enchantments());
    }

    public Variant variant() {
        return variant;
    }

    /** The {@code addEnchantment} block shared by :38-49, :67-78 and :106-117. */
    private List<PreEnchant.Entry> enchantments() {
        int magic = TweakStats.UltimateSwordMagic();
        List<PreEnchant.Entry> list = new ArrayList<>();
        if (variant != Variant.BATTLE_AXE) {
            list.add(PreEnchant.entry(Enchantments.SHARPNESS, magic));
            list.add(PreEnchant.entry(Enchantments.SMITE, magic));
            list.add(PreEnchant.entry(Enchantments.BANE_OF_ARTHROPODS, magic));
            list.add(PreEnchant.entry(Enchantments.KNOCKBACK, 1 + magic / 2));
            list.add(PreEnchant.entry(Enchantments.LOOTING, 1 + magic / 2));
            list.add(PreEnchant.entry(Enchantments.UNBREAKING, 1 + magic / 2));
            list.add(PreEnchant.entry(Enchantments.FIRE_ASPECT, 1 + magic / 3));
        } else {
            list.add(PreEnchant.entry(Enchantments.LOOTING, 1 + magic / 2));
            list.add(PreEnchant.entry(Enchantments.UNBREAKING, 1 + magic / 2));
        }
        return list;
    }

    /** {@code onCreated} (:33-50). */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (variant == Variant.CHAINSAW) {
            return;
        }
        PreEnchant.addAll(stack, level, enchantments());
    }

    /** {@code onEntitySwing} (:52-58): the chainsaw sound once per 50 ticks. */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving, InteractionHand hand) {
        if (variant == Variant.CHAINSAW && entityLiving != null && swingtimer.getOrDefault(entityLiving, 0) == 0) {
            entityLiving.playSound(ModSounds.CHAINSAWSHORT.get(), 1.0f, entityLiving.level().random.nextFloat() * 0.2f + 0.9f);
            swingtimer.put(entityLiving, 50);
        }
        return false;
    }

    /** {@code onUpdate} (:82-119). */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (variant == Variant.CHAINSAW) {
            int timer = swingtimer.getOrDefault(entity, 0);
            if (timer > 0) {
                --timer;
                swingtimer.put(entity, timer);
            }
            if (level.isClientSide && timer > 0) {
                float f = 1.0f;
                float dx = (float) (f * Math.cos(Math.toRadians(entity.getYRot() + 90.0f + 45.0f)));
                float dz = (float) (f * Math.sin(Math.toRadians(entity.getYRot() + 90.0f + 45.0f)));
                if (level.random.nextInt(8) == 0) {
                    level.addParticle(ParticleTypes.FLAME, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                            level.random.nextFloat() / 10.0f,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
                }
                if (level.random.nextInt(2) == 0) {
                    level.addParticle(ParticleTypes.SMOKE, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                            level.random.nextFloat() / 10.0f,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
                }
                if (level.random.nextInt(10) == 0) {
                    // "fireworksSpark"
                    level.addParticle(ParticleTypes.FIREWORK, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                            level.random.nextFloat() / 5.0f,
                            (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
                }
            }
            return;
        }
        PreEnchant.restore(stack, level, Enchantments.LOOTING, enchantments());
    }

    /** {@code onLeftClickEntity} (:130-146): PvP guard first, then the chainsaw's area attack. */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (UltimatePvp.protects(entity)) {
            return true;
        }
        if (variant == Variant.CHAINSAW && player != null) {
            findSomethingToHit(player);
        }
        return false;
    }

    /**
     * {@code findSomethingToHit} (:157-169): every living entity within the player's bounding box
     * grown by 5, the clicked one included, takes {@code chainsaw_stats.damage} as player damage.
     */
    private void findSomethingToHit(Player player) {
        List<LivingEntity> var5 = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(5.0, 5.0, 5.0));
        for (LivingEntity var8 : var5) {
            if (isSuitableTarget(var8, false, player)) {
                var8.hurt(player.damageSources().playerAttack(player), (float) WeaponStats.chainsaw_stats().damage());
            }
        }
    }

    /** {@code isSuitableTarget} (:171-193). */
    private boolean isSuitableTarget(LivingEntity par1EntityLiving, boolean par2, Player player) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == player) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (UltimatePvp.protects(par1EntityLiving)) {
            return false;
        }
        return MyCanSee(par1EntityLiving, player);
    }

    /**
     * {@code MyCanSee} (:195-248): steps from the player's eye line ({@code y + 1.4}) to the
     * target's centre in at most ten normalised steps; any non-air block in between blocks the
     * hit. The step-count {@code (int)} casts stay; the block-position casts (:242) are
     * {@code Mth.floor} (PORT, DECISIONS R20).
     */
    public boolean MyCanSee(LivingEntity e, Player player) {
        int nblks = 10;
        double cx = player.getX();
        double cz = player.getZ();
        float startx = (float) cx;
        float starty = (float) (player.getY() + (double) 1.4f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 10.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 10.0);
        float dz = (float) ((e.getZ() - startz) / 10.0);
        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks *= (int) Math.abs(dx);
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks *= (int) Math.abs(dy);
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks *= (int) Math.abs(dz);
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            // getBlock(...) != Blocks.air: 1.7.10 had a single air block, isAir() is the 1.21.1 equivalent.
            BlockState bid = player.level().getBlockState(new BlockPos(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz)));
            if (!bid.isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@code canCrush} (:254-259). PORT: R22 - {@code Blocks.log}, {@code leaves}, {@code planks} and
     * {@code sapling} were the whole wood, leaf, plank and sapling categories of 1.7.10; the port
     * covers the 1.21.1 categories via {@code #minecraft:logs}, {@code #minecraft:leaves},
     * {@code #minecraft:planks} and {@code #minecraft:saplings} (cherry, mangrove, bamboo, crimson,
     * warped, stripped logs, azalea leaves - and acacia and dark oak, which sat in {@code log2} in
     * 1.7.10). {@code tallgrass} = short grass and fern (its meta-0 "shrub" has no successor). Web,
     * cactus and the OreSpawn blocks stay single, as named in the original.
     */
    private boolean canCrush(BlockState state) {
        final Block blockID = state.getBlock();
        if (variant == Variant.CHAINSAW) {
            return blockID == Blocks.COBWEB
                    || state.is(BlockTags.LOGS)
                    || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.PLANKS)
                    || state.is(BlockTags.SAPLINGS)
                    || blockID == Blocks.SHORT_GRASS || blockID == Blocks.FERN
                    || blockID == Blocks.CACTUS
                    || blockID == ModBlocks.CRYSTAL_PLANKS.get()
                    || blockID == ModBlocks.LEAVES_APPLE.get()
                    || blockID == ModBlocks.SKY_TREE_LOG.get()
                    || blockID == ModBlocks.DUPLICATOR_TREE_LOG.get()
                    || blockID == ModBlocks.LEAVES_EXPERIENCE.get()
                    || blockID == ModBlocks.LEAVES_SCARY.get()
                    || blockID == ModBlocks.LEAVES_CHERRY.get()
                    || blockID == ModBlocks.LEAVES_PEACH.get()
                    || blockID == ModBlocks.CRYSTAL_TREE_LEAVES.get()
                    || blockID == ModBlocks.CRYSTAL_TREE_LEAVES2.get()
                    || blockID == ModBlocks.CRYSTAL_TREE_LEAVES3.get()
                    || blockID == ModBlocks.CRYSTAL_TREE_LOG.get();
        }
        return blockID == Blocks.COBWEB;
    }

    /**
     * {@code isLeaves} (:261-263). PORT: R22 - leaves and saplings as {@code #minecraft:leaves} and
     * {@code #minecraft:saplings}, as in {@link #canCrush}. Only reachable with {@code leaf}, which never is.
     */
    private boolean isLeaves(BlockState state) {
        final Block blockID = state.getBlock();
        return blockID == Blocks.COBWEB
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS)
                || blockID == Blocks.SHORT_GRASS || blockID == Blocks.FERN
                || blockID == ModBlocks.LEAVES_APPLE.get()
                || blockID == ModBlocks.LEAVES_EXPERIENCE.get()
                || blockID == ModBlocks.LEAVES_SCARY.get()
                || blockID == ModBlocks.LEAVES_CHERRY.get()
                || blockID == ModBlocks.LEAVES_PEACH.get()
                || blockID == ModBlocks.CRYSTAL_TREE_LEAVES.get()
                || blockID == ModBlocks.CRYSTAL_TREE_LEAVES2.get()
                || blockID == ModBlocks.CRYSTAL_TREE_LEAVES3.get();
    }

    /**
     * {@code onBlockDestroyed} (:265-286): the chainsaw clears every crushable block in the
     * 11x16x11 box {@code x-5..x+5, y-5..y+10, z-5..z+5} around the mined block, dropping one item
     * per block, then the vanilla sword durability (two per block, {@code super}).
     *
     * <p>PORT: {@code new ItemStack(Item.getItemFromBlock(bid), 1, 0)} dropped metadata 0 - every
     * log variant fell as an oak log, every leaf as oak leaves, every sapling as an oak sapling.
     * Those variants are separate blocks now and drop themselves.
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (variant == Variant.CHAINSAW && !level.isClientSide) {
            for (int i = -5; i <= 5; ++i) {
                for (int j = -5; j <= 10; ++j) {
                    for (int k = -5; k <= 5; ++k) {
                        BlockPos p = pos.offset(i, j, k);
                        BlockState bstate = level.getBlockState(p);
                        Block bid = bstate.getBlock();
                        if (this.leaf) {
                            if (isLeaves(bstate)) {
                                dropItemRand(level, bid.asItem(), 1, p);
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                            }
                        } else if (canCrush(bstate)) {
                            dropItemRand(level, bid.asItem(), 1, p);
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    /**
     * {@code dropItemRand} (:288-296): the item entity lands at {@code x +- rand(5)}, {@code y + 1 +
     * rand(5)}, {@code z +- rand(5)} - x and z from {@code OreSpawnRand}, y from the level's random.
     */
    private static void dropItemRand(Level world, Item index, int par1, BlockPos at) {
        ItemStack is = new ItemStack(index, par1);
        if (is.isEmpty()) {
            // PORT: a block without an item would have been an EntityItem with a null stack in 1.7.10.
            return;
        }
        ItemEntity var3 = new ItemEntity(world,
                at.getX() + OreSpawn.OreSpawnRand.nextInt(5) - OreSpawn.OreSpawnRand.nextInt(5),
                at.getY() + 1.0 + world.random.nextInt(5),
                at.getZ() + OreSpawn.OreSpawnRand.nextInt(5) - OreSpawn.OreSpawnRand.nextInt(5),
                is);
        world.addFreshEntity(var3);
    }
}
