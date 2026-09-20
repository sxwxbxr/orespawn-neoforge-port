package com.swbr.orespawn.entity.cow;

import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.EnchantedCow} (EnchantedCow.java:10-40), id {@code enchanted_golden_apple_cow}.
 * A {@link RedCow} whose drops include two golden apples and an enchanted golden apple. The renderer gives it
 * {@code gold_cow.png} plus the enchantment glint ({@code RenderEnchantedCow}).
 */
public class EnchantedCow extends RedCow {

    public EnchantedCow(final EntityType<? extends EnchantedCow> type, final Level level) {
        super(type, level);
    }

    /**
     * {@code dropEnchantedGoldenApple} (:16-19): {@code golden_apple} meta 1 - its own item in 1.21.1 - one block
     * above the feet. Added straight to the level like the original's {@code spawnEntityInWorld}, so it bypasses
     * the captured drop list and has no pickup delay.
     */
    private void dropEnchantedGoldenApple() {
        final ItemEntity var3 = new ItemEntity(this.level(), this.getX(), this.getY() + 1.0, this.getZ(),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        this.level().addFreshEntity(var3);
    }

    /**
     * {@code dropFewItems} (:21-29), in this order: {@code rand(4) + rand(1 + looting)} apples, one stack of two
     * golden apples, the enchanted golden apple, then {@code RedCow.dropFewItems}.
     */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var3 = this.random.nextInt(4) + this.random.nextInt(1 + par2), var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.APPLE, 1));
        }
        this.spawnAtLocation(new ItemStack(Items.GOLDEN_APPLE, 2));
        this.dropEnchantedGoldenApple();
        super.dropFewItems(par1, par2);
    }

    /** {@code createChild} (:31-34). */
    @Nullable
    @Override
    public EnchantedCow getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:36-39). */
    @Nullable
    @Override
    public EnchantedCow spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get().create(this.level());
    }
}
