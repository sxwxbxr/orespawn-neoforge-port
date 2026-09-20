package com.swbr.orespawn.entity.herbivore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 vanilla behaviour shared by the W06 herbivores ({@link Baryonyx}, {@link Cassowary},
 * {@link Camarasaurus}, {@link Beaver}, {@link Peacock}, {@link StinkBug}, {@link Hydrolisc}). No
 * original OreSpawn class: every method here is a piece of {@code EntityAnimal}, {@code EntityLiving} or
 * {@code EntityTameable} that the seven classes inherited and 1.21.1 changed. {@code EntityAIPanic} lies once, in
 * {@code entity.ai.LegacyPanic} (W12).
 *
 * <p>The companion and insect packages carry package-private copies of some of these helpers
 * ({@code spawnerNearby}); they are not visible from here. An integrator who merges them moves this class to
 * {@code entity.ai} (R21, no duplicate helpers).
 */
public final class HerbivoreSupport {

    /**
     * 1.7.10 {@code setBlock(x, y, z, block, meta, 2)}: send to clients, no neighbour notification.
     * In 1.21.1 {@code UPDATE_CLIENTS} alone still runs the neighbours' shape updates; the
     * {@code UPDATE_KNOWN_SHAPE} bit suppresses them (W03 Miner's Dream lesson).
     *
     * <p>Not the whole of flag 2: 1.7.10 {@code Chunk.func_150807_a} called {@code breakBlock} on the old block on the
     * server whatever the flags were. Where that {@code breakBlock} did something, the caller runs its port right after
     * the {@code setBlock}: {@link #legacyLogBreakBlock} for a vanilla log, {@link #legacyLeavesBreakBlock} for vanilla
     * leaves. Without them a felled tree keeps its leaves for ever, because 1.21.1 starts leaf decay only from the shape
     * update this flag skips.
     */
    public static final int LEGACY_FLAG_2 = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private HerbivoreSupport() {}

    /**
     * 1.7.10 {@code BlockLog.breakBlock} ({@code alx.a(ahb, III, aji, I)}, disassembled), run for a removed
     * {@code Blocks.log} (oak, spruce, birch, jungle log and wood): when all chunks within 5 exist, every block of leaves
     * material within 4 without the decay bit gets it. OreSpawn's own logs ({@code BlockSkyTreeLog},
     * {@code BlockDuplicatorLog}) extend {@code Block} and did nothing.
     */
    public static void legacyLogBreakBlock(final Level world, final BlockPos pos) {
        beginLeavesDecay(world, pos, 4);
    }

    /**
     * 1.7.10 {@code BlockLeaves.breakBlock} ({@code alt.a(ahb, III, aji, I)}, disassembled), run for removed vanilla
     * leaves: when all chunks within 2 exist, every block of leaves material within 1 gets the decay bit.
     */
    public static void legacyLeavesBreakBlock(final Level world, final BlockPos pos) {
        beginLeavesDecay(world, pos, 1);
    }

    /**
     * The shared loop of both {@code breakBlock}s: {@code checkChunksExist} over {@code b0 + 1}, then the cube of
     * radius {@code b0}.
     *
     * <p>PORT: 1.21.1 leaves have no decay bit. Its counterpart is a leaves tick: {@link LeavesBlock#tick} recomputes
     * the distance, a changed distance updates the neighbours, and leaves cut off from every log reach 7 and decay on
     * a random tick - where 1.7.10 checked the marked leaf for a log within 4 on its random tick. A tick already
     * scheduled is not scheduled twice, which covers {@code BlockLog}'s "only without the bit". OreSpawn's own leaves
     * ({@code OreSpawnLeaves}) are left out: their {@code updateTick} ignored the bit and searched for support on every
     * random tick anyway. Leaves 1.7.10 did not have (cherry, mangrove, azalea) are {@code LeavesBlock}s too and get
     * the same tick.
     */
    private static void beginLeavesDecay(final Level world, final BlockPos pos, final int b0) {
        final int i1 = b0 + 1;
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        if (!world.hasChunksAt(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
            return;
        }
        for (int j1 = -b0; j1 <= b0; ++j1) {
            for (int k1 = -b0; k1 <= b0; ++k1) {
                for (int l1 = -b0; l1 <= b0; ++l1) {
                    final BlockPos at = new BlockPos(x + j1, y + k1, z + l1);
                    final BlockState block = world.getBlockState(at);
                    if (block.getBlock() instanceof LeavesBlock) {
                        world.scheduleTick(at, block.getBlock(), 1);
                    }
                }
            }
        }
    }

    /**
     * {@code World.isDaytime()}: {@code skylightSubtracted < 4} and nothing else (bytecode {@code ahb.w()},
     * see {@code MyEntityAIFollowOwner}), which is {@code getSkyDarken() < 4}.
     */
    public static boolean isDaytime(final Level level) {
        return level.getSkyDarken() < 4;
    }

    /**
     * 1.7.10 {@code EntityAnimal.interact} ({@code wf.a(yz)}, disassembled): with a breeding item, age 0
     * and {@code inLove <= 0}, take one item unless creative, fall in love and return {@code true};
     * otherwise {@code EntityAgeable.interact}, whose only branch (vanilla spawn egg on an adult)
     * 1.21.1 runs in {@code Mob.interact} before this. Babies are <em>not</em> fed: 1.21.1's
     * {@code Animal.mobInteract} ages them up, 1.7.10 did not.
     *
     * <p>PORT: 1.7.10 had one hand, so the off hand passes. On the client 1.21.1 reports the age of
     * every adult as 1 ({@code AgeableMob.getAge}), so an adult counts as age 0 there; an adult on
     * breeding cooldown therefore swings the arm on the client where 1.7.10 went on to eat the apple.
     * The server decides alone either way.
     *
     * @return {@code sidedSuccess} when the animal fell in love, else {@code PASS}
     */
    public static InteractionResult legacyAnimalInteract(final Animal animal, final Player player, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
        final boolean isRemote = animal.level().isClientSide;
        final boolean ageZero = isRemote ? !animal.isBaby() : animal.getAge() == 0;
        if (!itemstack.isEmpty() && animal.isFood(itemstack) && ageZero && animal.canFallInLove()) {
            consumeOne(player, itemstack);
            animal.setInLove(player);
            return InteractionResult.sidedSuccess(isRemote);
        }
        return InteractionResult.PASS;
    }

    /** {@code --stackSize} unless {@code capabilities.isCreativeMode}; the emptied stack replaces the explicit {@code null}. */
    public static void consumeOne(final Player player, final ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * {@code Items.fish} with any metadata: raw cod, salmon, clownfish, pufferfish. PORT: 1.21.1's
     * {@code #minecraft:fishes} also holds the cooked ones, which were {@code Items.cooked_fished}.
     */
    public static boolean isRawFish(final ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH);
    }

    /**
     * 1.7.10 {@code EntityTameable.setSitting}: one synced flag. 1.21.1 splits it into the order (saved
     * as {@code Sitting}) and the pose (synced), so both are set (same as {@code Girlfriend}).
     */
    public static void setSitting(final TamableAnimal animal, final boolean sitting) {
        animal.setOrderedToSit(sitting);
        animal.setInSittingPose(sitting);
    }

    /**
     * The {@code fall(float)} override of {@link Camarasaurus} (:69-83) and {@link Hydrolisc} (:147-161):
     * damage {@code ceil(distance - 3)} capped at 2, big-fall sound above 3, no rider propagation, no block
     * fall sound, no {@code LivingFallEvent}. PORT: the 1.21.1 multiplier (hay bale, bed) and the safe-fall
     * attribute did not exist and are ignored, as in {@code Girlfriend}.
     */
    public static boolean legacyFall(final LivingEntity entity, final float par1) {
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 3.0f) {
                entity.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f);
            } else {
                entity.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f);
            }
            if (i > 2.0f) {
                i = 2.0f;
            }
            entity.hurt(entity.damageSources().fall(), i);
            return true;
        }
        return false;
    }

    /**
     * The looting level 1.7.10 {@code EntityLivingBase.onDeath} handed to {@code dropFewItems}:
     * {@code EnchantmentHelper.getLootingModifier} of the killer when the damage source's entity is a
     * player, else 0. Enchantments are a data registry in 1.21.1.
     */
    public static int lootingLevel(final ServerLevel level, final DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player player) {
            return EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), player);
        }
        return 0;
    }

    /**
     * 1.7.10 {@code EntityLiving.dropFewItems} ({@code sw.b(ZI)}, disassembled), for classes that only
     * override {@code getDropItem}: {@code rand.nextInt(3)} items, plus {@code rand.nextInt(looting + 1)}
     * when looting is above 0, each through {@code dropItem(item, 1)} = {@code spawnAtLocation}.
     */
    public static void legacyDropFewItems(final Mob mob, final Item item, final int looting) {
        if (item != null) {
            int j = mob.getRandom().nextInt(3);
            if (looting > 0) {
                j += mob.getRandom().nextInt(looting + 1);
            }
            for (int k = 0; k < j; ++k) {
                mob.spawnAtLocation(new ItemStack(item, 1));
            }
        }
    }

    /**
     * The spawner test of {@code StinkBug.getCanSpawnHere} (:118-132): a {@code mob_spawner} with the
     * given entity in x/z {@code -3..2}, y {@code 0..4} around the floored position. The spawner's entity
     * is read the way W04's {@code CompanionSupport.spawnerNearby} reads it (the {@code SpawnData} of
     * {@code BaseSpawner.save}); 1.7.10 compared the registered name ({@code "Stink Bug"}), which is the
     * registry id here (R2).
     *
     * <p>PORT (R18 case 1): the original cast the tile entity without a null check; a spawner block
     * without its block entity would have crashed, so it just does not match.
     */
    public static boolean spawnerNearby(final LevelAccessor level, final int posX, final int posY, final int posZ,
                                        final String entityId) {
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    final BlockPos at = new BlockPos(posX + j, posY + i, posZ + k);
                    if (level.getBlockState(at).is(Blocks.SPAWNER)) {
                        final BlockEntity tileentitymobspawner = level.getBlockEntity(at);
                        if (tileentitymobspawner instanceof SpawnerBlockEntity spawner) {
                            final String s = spawner.getSpawner().save(new CompoundTag())
                                    .getCompound("SpawnData").getCompound("entity").getString("id");
                            if (s.equals(entityId)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
