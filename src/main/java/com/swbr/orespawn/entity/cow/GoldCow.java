package com.swbr.orespawn.entity.cow;

import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.GoldCow} (GoldCow.java:8-32), id {@code golden_apple_cow}. A {@link RedCow} with
 * a guaranteed golden apple on top of the apple drops; rendered with {@code gold_cow.png} by
 * {@code RenderEnchantedCow}.
 */
public class GoldCow extends RedCow {

    public GoldCow(final EntityType<? extends GoldCow> type, final Level level) {
        super(type, level);
    }

    /**
     * {@code dropFewItems} (:14-21): {@code rand(3) + rand(1 + looting)} apples, one golden apple, then
     * {@code RedCow.dropFewItems} - which rolls its own apples and the {@code EntityCow} drops.
     */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var3 = this.random.nextInt(3) + this.random.nextInt(1 + par2), var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.APPLE, 1));
        }
        this.spawnAtLocation(new ItemStack(Items.GOLDEN_APPLE, 1));
        super.dropFewItems(par1, par2);
    }

    /** {@code createChild} (:23-26). */
    @Nullable
    @Override
    public GoldCow getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:28-31). */
    @Nullable
    @Override
    public GoldCow spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.GOLDEN_APPLE_COW.get().create(this.level());
    }
}
