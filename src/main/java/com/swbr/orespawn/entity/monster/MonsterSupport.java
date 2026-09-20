package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared 1.7.10 idioms of the six W07 monsters in this package (Alien, Molenoid, Kyuubi, BandP, LeafMonster,
 * Hammerhead). Each helper is a line pattern the originals repeat verbatim; the numbers stay at the call sites.
 */
final class MonsterSupport {

    private MonsterSupport() {}

    /**
     * {@code dropItemRand(index, par1)}: a fresh {@code EntityItem} at {@code posX + rand(bound) - rand(bound)},
     * {@code posY + yOffset}, {@code posZ + rand(bound) - rand(bound)} on the shared {@link OreSpawn#OreSpawnRand}, with
     * a stack without metadata ({@code new ItemStack(index, par1, 0)}). The raw {@code EntityItem} constructor gave no
     * pickup delay and the random 0.2-wide spread of {@code ItemEntity(Level, x, y, z, stack)}; the entity is added
     * directly, so NeoForge's drop capture does not see it - neither did Forge's {@code captureDrops}, which only
     * caught {@code entityDropItem}.
     *
     * @return the stack inside the new item entity, or {@link ItemStack#EMPTY} for a {@code null} item
     */
    static ItemStack dropItemRand(final Mob mob, final Item index, final int par1, final int bound, final double yOffset) {
        if (index == null) {
            return ItemStack.EMPTY;
        }
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(mob.level(),
                mob.getX() + OreSpawn.OreSpawnRand.nextInt(bound) - OreSpawn.OreSpawnRand.nextInt(bound),
                mob.getY() + yOffset,
                mob.getZ() + OreSpawn.OreSpawnRand.nextInt(bound) - OreSpawn.OreSpawnRand.nextInt(bound),
                is);
        mob.level().addFreshEntity(var3);
        return is;
    }

    /**
     * The knockback block of the {@code attackEntityAsMob} overrides: {@code f3 = atan2(dz, dx)} from the attacker to the
     * target, vertical {@code inair} doubled for a dead ({@code isDead}, i.e. removed) entity or a player, then
     * {@code addVelocity(cos(f3) * ks, inair, sin(f3) * ks)}.
     *
     * <p>PORT: {@code addVelocity} is {@link Entity#push(double, double, double)}. A server player only receives an
     * impulse through {@code hurtMarked} (catalogue README, "Serverimpuls an Spieler braucht hurtMarked"); the preceding
     * successful hit already sets it, it is set again here so the impulse never depends on the damage path.
     */
    static void legacyKnockback(final Mob attacker, final Entity par1Entity, final double ks, final double inairBase) {
        double inair = inairBase;
        final float f3 = (float) Math.atan2(par1Entity.getZ() - attacker.getZ(), par1Entity.getX() - attacker.getX());
        if (par1Entity.isRemoved() || par1Entity instanceof Player) {
            inair *= 2.0;
        }
        par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        par1Entity.hurtMarked = true;
    }

    /**
     * {@code new EntityAIMoveThroughVillage(creature, speed, false)}.
     *
     * <p>PORT (R18 case 3): 1.7.10 walked to the nearest door of the {@code VillageCollection}; 1.21.1 has no village
     * records, and vanilla's {@link MoveThroughVillageGoal} is the POI-based successor. {@code isNocturnal} is
     * {@code onlyAtNight}; the distance to a visited POI is 4, as vanilla's zombie uses. The original set
     * {@code setBreakDoors(false)} around its path search ({@code EntityAIMoveThroughVillage.shouldExecute}), so doors
     * are never part of that path: {@code canDealWithDoors} is {@code false}. Unlike {@code EntityAIMoveIndoors} there
     * is no line-by-line port of this task yet; whoever adds one to {@code entity.ai} replaces this factory.
     */
    static MoveThroughVillageGoal legacyMoveThroughVillage(final PathfinderMob creature, final double speed) {
        return new MoveThroughVillageGoal(creature, speed, false, 4, () -> false);
    }

    /** {@code worldObj.provider.dimensionId == OreSpawnMain.DimensionID4}: the Islands ("danger") dimension. */
    static boolean isDangerDimension(final Level level) {
        return level.dimension() == WorldProviderOreSpawn4.DIMENSION;
    }

    /** {@code bid != Blocks.air}: 1.7.10 knew one air block; cave and void air are air as well. */
    static boolean isAir(final LevelAccessor level, final int x, final int y, final int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    /**
     * {@code Blocks.leaves}: one block with oak, spruce, birch and jungle as metadata 0-3 (plus the decay bits);
     * acacia and dark oak were the separate {@code Blocks.leaves2}.
     *
     * <p>PORT (R22): "leaves" is a category, so every block of {@code #minecraft:leaves} counts - acacia, dark oak,
     * azalea, mangrove and cherry included. OreSpawn's own leaves are excluded: the port adds them to that tag
     * ({@code data/minecraft/tags/block/leaves.json}), but they were separate blocks the original never compared with
     * {@code Blocks.leaves}.
     */
    static boolean isLegacyLeaves(final BlockState state) {
        return state.is(BlockTags.LEAVES)
                && !OreSpawn.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    /**
     * {@code Blocks.sand}: sand (0) and red sand (1).
     *
     * <p>PORT (R22): the category {@code #minecraft:sand}, which adds suspicious sand.
     */
    static boolean isLegacySand(final BlockState state) {
        return state.is(BlockTags.SAND);
    }
}
