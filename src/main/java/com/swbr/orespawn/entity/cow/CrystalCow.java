package com.swbr.orespawn.entity.cow;

import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.CrystalCow} (CrystalCow.java:8-32), id {@code crystal_apple_cow}. The Crystal
 * dimension's {@link RedCow}: crystal apples instead of the first apple roll. No spawn rule of its own, so the
 * animal rule (grass below, light above 8) applies, as in the original.
 */
public class CrystalCow extends RedCow {

    public CrystalCow(final EntityType<? extends CrystalCow> type, final Level level) {
        super(type, level);
    }

    /**
     * {@code dropFewItems} (:14-21): {@code rand(3) + rand(1 + looting)} crystal apples, one apple, then
     * {@code RedCow.dropFewItems}.
     */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var3 = this.random.nextInt(3) + this.random.nextInt(1 + par2), var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(ModItems.CRYSTAL_APPLE.get(), 1));
        }
        this.spawnAtLocation(new ItemStack(Items.APPLE, 1));
        super.dropFewItems(par1, par2);
    }

    /** {@code createChild} (:23-26). */
    @Nullable
    @Override
    public CrystalCow getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:28-31). */
    @Nullable
    @Override
    public CrystalCow spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.CRYSTAL_APPLE_COW.get().create(this.level());
    }
}
