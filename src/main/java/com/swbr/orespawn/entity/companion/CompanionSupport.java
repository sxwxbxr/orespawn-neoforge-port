package com.swbr.orespawn.entity.companion;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/**
 * Translation helpers that {@link Girlfriend} and {@link Boyfriend} share. Neither original had a
 * helper class - the two entity classes repeated every line - so this holds only 1.7.10 to 1.21.1
 * mappings, never behaviour. Everything the companions decide stays in their own classes, in the
 * original order.
 */
final class CompanionSupport {

    /**
     * 1.7.10 {@code getEquipmentInSlot}/{@code setCurrentItemOrArmor} index to slot: 0 held item,
     * 1 boots, 2 leggings, 3 chestplate, 4 helmet ({@code EntityLiving.equipment}, the order
     * {@code getLastActiveItems()} returned).
     */
    private static final EquipmentSlot[] LEGACY_SLOTS = {
        EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private CompanionSupport() {}

    /** {@code new ResourceLocation("orespawn", name + ".png")}, moved to {@code textures/entity/} and lower-cased by tools/assets.py. */
    static ResourceLocation texture(final String name) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + name + ".png");
    }

    /** The numbered texture literals of a static initialiser, {@code prefix0.png .. prefix(count-1).png}. */
    static ResourceLocation[] textures(final String prefix, final int count) {
        final ResourceLocation[] out = new ResourceLocation[count];
        for (int i = 0; i < count; ++i) {
            out[i] = texture(prefix + i);
        }
        return out;
    }

    /** {@code getEquipmentInSlot(i)}; 1.7.10 {@code null} is {@link ItemStack#EMPTY}. */
    static ItemStack getEquipmentInSlot(final Mob mob, final int index) {
        return mob.getItemBySlot(LEGACY_SLOTS[index]);
    }

    /** {@code setCurrentItemOrArmor(i, stack)}; a 1.7.10 {@code null} stack is {@link ItemStack#EMPTY}. */
    static void setCurrentItemOrArmor(final Mob mob, final int index, final ItemStack stack) {
        mob.setItemSlot(LEGACY_SLOTS[index], stack == null ? ItemStack.EMPTY : stack);
    }

    /**
     * {@code getTotalArmorValue()} of both companions (Girlfriend.java:185-200, Boyfriend.java:158-173):
     * the {@code damageReduceAmount} of every {@code ItemArmor} among the five 1.7.10 equipment slots -
     * the held item included, which is why this does not use
     * {@code LegacyArmorFormula.armorFromEquipment} (armor slots only) - clamped to 8..23.
     * {@link ArmorItem#getDefense()} is the 1.21.1 name of {@code damageReduceAmount}.
     */
    static int totalArmorValue(final Mob mob) {
        int i = 0;
        for (final EquipmentSlot slot : LEGACY_SLOTS) {
            final ItemStack itemstack = mob.getItemBySlot(slot);
            if (!itemstack.isEmpty() && itemstack.getItem() instanceof ArmorItem armor) {
                final int l = armor.getDefense();
                i += l;
            }
        }
        if (i < 8) {
            i = 8;
        }
        if (i > 23) {
            i = 23;
        }
        return i;
    }

    /**
     * {@code Item.getItemFromBlock(Blocks.red_flower)}. The 1.7.10 red flower was one block with nine
     * metadata variants and the check compared the item only, so every variant matched. Drops of
     * {@code red_flower} use meta 0, the poppy.
     *
     * <p>PORT (R22): small flowers are a category, so this is {@code #minecraft:small_flowers} - with
     * cornflower, lily of the valley, wither rose and torchflower - minus the dandelion, which was the
     * separate {@code Blocks.yellow_flower} and keeps its own branch in {@code Girlfriend} (original :614).
     */
    static boolean isRedFlower(final ItemStack stack) {
        return stack.is(ItemTags.SMALL_FLOWERS) && !stack.is(Items.DANDELION);
    }

    /**
     * Identity test against an OreSpawn item by its manifest id. Used for {@code MyBertha}
     * ({@code berthasmall}) and {@code MyUltimateBow} ({@code ultimatebow}), whose holders are written
     * by another W04 porter; the registry id is fixed by the manifest (R2), the holder field name is not.
     */
    static boolean isItem(final ItemStack stack, final String id) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id)));
    }

    /** {@code EnchantmentHelper.getEnchantmentLevel(Enchantment.x.effectId, stack)}: enchantments are a data registry in 1.21.1. */
    static int enchantmentLevel(final Level level, final ItemStack stack, final ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key));
    }

    /**
     * {@code World.playSoundAtEntity(entity, name, volume, pitch)}: plays at the entity's position for
     * every nearby player and, unlike {@code Entity.playSound}, ignores the silent flag. On the client
     * it does nothing, as the 1.7.10 client world did.
     */
    static void playSoundAtEntity(final Entity entity, final SoundEvent sound, final float volume, final float pitch) {
        entity.level().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(), volume, pitch);
    }

    /**
     * The spawner half of {@code getCanSpawnHere} (Girlfriend.java:1117-1134, Boyfriend.java:990-1007):
     * a mob spawner in x/z -3..2, y 0..4 around the spawn position whose entity is {@code entityId}.
     *
     * <p>PORT: a spawn predicate receives a {@link BlockPos}; the entity the spawner places sits at the
     * block centre on x/z, so the original's {@code (int) posX} is {@code Mth.floor(x + 0.5)} (DECISIONS
     * R20: floor, not truncation, which named the neighbouring column at negative x/z). 1.7.10
     * {@code getEntityNameToSpawn()} ("Girlfriend") is the spawner's
     * {@code SpawnData.entity.id} ("orespawn:girlfriend"). The original dereferenced the tile entity
     * without a null check; a spawner block without its block entity would have crashed (R18 case 1),
     * so a missing or foreign block entity just does not match.
     */
    static boolean spawnerNearby(final ServerLevelAccessor level, final BlockPos pos, final String entityId) {
        final int posX = Mth.floor(pos.getX() + 0.5);
        final int posY = pos.getY();
        final int posZ = Mth.floor(pos.getZ() + 0.5);
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

    /** {@code --stackSize} unless creative; an emptied stack reads as empty, which replaces the explicit {@code null}. */
    static void consumeOne(final Player player, final ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /** {@code player.inventory.setInventorySlotContents(player.inventory.currentItem, stack)}. */
    static void setHeldSlot(final Player player, final ItemStack stack) {
        player.getInventory().setItem(player.getInventory().selected, stack == null ? ItemStack.EMPTY : stack);
    }
}
