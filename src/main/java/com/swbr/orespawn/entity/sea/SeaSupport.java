package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

/**
 * The 1.7.10 idioms the seven W08 water mobs repeat verbatim: the water shell scan of the dry-out logic (AttackSquid,
 * SeaMonster, SeaViper, Skate, Irukandji - the same 80 lines five times), {@code heal(-1)}, the knockback block of
 * the two sea bosses, their iron drop table and the gold table of the Attack Squid. The numbers stay at the call
 * sites; these helpers only hold the shape.
 */
final class SeaSupport {

    private SeaSupport() {}

    /**
     * {@code bid == Blocks.water || bid == Blocks.flowing_water}: one block in 1.21.1. A waterlogged block is not a
     * water block, as 1.7.10 had none (same reading as {@code Whale.isWater}, W06).
     */
    static boolean isWater(final LevelAccessor level, final int x, final int y, final int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /**
     * The per-entity state {@code closest}, {@code tx}, {@code ty}, {@code tz} and the method {@code scan_it} of the
     * five water mobs (e.g. AttackSquid.java:393-474): the six faces of a box with half sizes {@code dx, dy, dz}
     * around {@code (x, y, z)}; every water block closer (squared face distance) than the best so far becomes the
     * target.
     */
    static final class WaterScan {

        int closest = 99999;
        int tx = 0;
        int ty = 0;
        int tz = 0;

        boolean scan_it(final Level level, final int x, final int y, final int z, final int dx, final int dy, final int dz) {
            int found = 0;
            for (int i = -dy; i <= dy; ++i) {
                for (int j = -dz; j <= dz; ++j) {
                    if (isWater(level, x + dx, y + i, z + j)) {
                        final int d = dx * dx + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x + dx;
                            this.ty = y + i;
                            this.tz = z + j;
                            ++found;
                        }
                    }
                    if (isWater(level, x - dx, y + i, z + j)) {
                        final int d = dx * dx + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x - dx;
                            this.ty = y + i;
                            this.tz = z + j;
                            ++found;
                        }
                    }
                }
            }
            for (int i = -dx; i <= dx; ++i) {
                for (int j = -dz; j <= dz; ++j) {
                    if (isWater(level, x + i, y + dy, z + j)) {
                        final int d = dy * dy + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x + i;
                            this.ty = y + dy;
                            this.tz = z + j;
                            ++found;
                        }
                    }
                    if (isWater(level, x + i, y - dy, z + j)) {
                        final int d = dy * dy + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x + i;
                            this.ty = y - dy;
                            this.tz = z + j;
                            ++found;
                        }
                    }
                }
            }
            for (int i = -dx; i <= dx; ++i) {
                for (int j = -dy; j <= dy; ++j) {
                    if (isWater(level, x + i, y + j, z + dz)) {
                        final int d = dz * dz + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x + i;
                            this.ty = y + j;
                            this.tz = z + dz;
                            ++found;
                        }
                    }
                    if (isWater(level, x + i, y + j, z - dz)) {
                        final int d = dz * dz + j * j + i * i;
                        if (d < this.closest) {
                            this.closest = d;
                            this.tx = x + i;
                            this.ty = y + j;
                            this.tz = z - dz;
                            ++found;
                        }
                    }
                }
            }
            return found != 0;
        }

        /**
         * The search loop of {@code updateAITasks} (e.g. AttackSquid.java:489-505): reset, then shells 1, 2, 3, 4, 5, 7,
         * 9, 11 around one block below the feet, the vertical half size capped at {@code dyCap}, stopping at the first
         * shell with a hit. {@code final boolean tx = false; tz = ty = tx = 0} is the reset to 0.
         *
         * <p>R20: the {@code (int)} casts of the origin are {@code Mth.floor}.
         */
        void search(final Mob mob, final int dyCap) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 12; ++i) {
                int j = i;
                if (j > dyCap) {
                    j = dyCap;
                }
                if (this.scan_it(mob.level(), Mth.floor(mob.getX()), Mth.floor(mob.getY()) - 1,
                        Mth.floor(mob.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
        }
    }

    /**
     * {@code heal(f)} with a negative amount, as 1.7.10 applied it: {@code setHealth(health + f)} while alive.
     * NeoForge's {@code LivingEntity.heal} returns for amounts {@code <= 0} after {@code EventHooks.onLivingHeal}, so
     * this goes straight to {@code setHealth} (R18, catalogue 6.3; same as Whale and Flounder, W06).
     */
    static void legacyHeal(final LivingEntity entity, final float f) {
        final float f1 = entity.getHealth();
        if (f1 > 0.0f) {
            entity.setHealth(f1 + f);
        }
    }

    /**
     * The knockback block of SeaMonster/SeaViper {@code attackEntityAsMob}: {@code f3 = atan2(dz, dx)} from attacker to
     * target, vertical {@code inair} doubled for a dead ({@code isDead}, i.e. removed) entity or a player, then
     * {@code addVelocity(cos(f3) * ks, inair, sin(f3) * ks)}.
     *
     * <p>PORT: {@code addVelocity} is {@link Entity#push(double, double, double)}; a server player only receives the
     * impulse through {@code hurtMarked}, which the preceding successful hit already set - set again here so it never
     * depends on the damage path (same as {@code MonsterSupport.legacyKnockback}, W07).
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
     * {@code dropItemRand(index, 1)}: {@code new ItemStack(index, 1, 0)} one block up, {@code OreSpawnRand.nextInt(spread)
     * - OreSpawnRand.nextInt(spread)} off on x and z; see {@link ArthropodSupport#dropItemRand} for the enchant-before-drop
     * order, which keeps both random streams in their original order.
     */
    static ItemStack stack(final Item item) {
        return new ItemStack(item, 1);
    }

    /**
     * The {@code rand(20)} iron table shared verbatim by SeaMonster (:171-331) and SeaViper (:175-335): 1 ingot, 3 sword,
     * 4 shovel, 5 pickaxe, 6 axe, 7 hoe, 8 helmet, 9 chestplate, 10 leggings, 11 boots, 13 iron block; the enchantment
     * rolls are {@link ArthropodSupport}'s identical blocks. The draw is on the world random.
     */
    static void ironTable(final Mob mob) {
        final Level level = mob.level();
        final int var6 = level.random.nextInt(20);
        ItemStack is;
        switch (var6) {
            case 1 -> ArthropodSupport.dropItemRand(mob, stack(Items.IRON_INGOT), 2);
            case 3 -> {
                is = stack(Items.IRON_SWORD);
                ArthropodSupport.enchantSword(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 4 -> {
                is = stack(Items.IRON_SHOVEL);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 5 -> {
                is = stack(Items.IRON_PICKAXE);
                ArthropodSupport.enchantPickaxe(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 6 -> {
                is = stack(Items.IRON_AXE);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 7 -> {
                is = stack(Items.IRON_HOE);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 8 -> {
                is = stack(Items.IRON_HELMET);
                ArthropodSupport.enchantHelmet(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 9 -> {
                is = stack(Items.IRON_CHESTPLATE);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 10 -> {
                is = stack(Items.IRON_LEGGINGS);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 11 -> {
                is = stack(Items.IRON_BOOTS);
                ArthropodSupport.enchantBoots(is, level);
                ArthropodSupport.dropItemRand(mob, is, 2);
            }
            case 13 -> ArthropodSupport.dropItemRand(mob, stack(Items.IRON_BLOCK), 2);
            default -> {
            }
        }
    }

    /**
     * An OreSpawn entity type of a later wave, by registry id, or {@code null} while it is not registered.
     * {@code BuiltInRegistries.ENTITY_TYPE} is a defaulted registry (pig), so the key is tested first.
     */
    @Nullable
    static EntityType<?> laterWaveType(final String path) {
        final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.get(id);
    }

    /** {@code e instanceof <class>} for a class of a later or parallel wave, by registry id (W07 precedent). */
    static boolean isType(final Entity e, final String path) {
        return e != null && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType())
                .equals(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path));
    }
}
