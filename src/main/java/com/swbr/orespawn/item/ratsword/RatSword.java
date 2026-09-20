package com.swbr.orespawn.item.ratsword;

import com.swbr.orespawn.entity.terror.Rat;
import com.swbr.orespawn.item.tool.BlockingSword;
import com.swbr.orespawn.item.tool.OreSpawnTiers;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.RatSword} (RatSword.java:10-67): an {@code ItemSword} on {@code toolEMERALD}, stack 1,
 * {@code setMaxDamage(1300)} (:20), tab Combat (:21); registered as {@code ratsword} (legacy id 9256). Attack
 * {@code 4 + 6} in 1.7.10 (R6).
 *
 * <ul>
 *   <li>{@code hitEntity} (:28-41): on the server, 1..6 rats appear at the struck entity and take the attacker as owner
 *       (a player only, {@code Rat.setOwner}); then {@code damageItem(1)}, which is vanilla
 *       {@code SwordItem.postHurtEnemy}.</li>
 *   <li>Right-click blocks for 3000 ticks ({@code getMaxItemUseDuration} :59-61) - {@link BlockingSword}, the same
 *       reading of R19 as {@code FairySword} (W07).</li>
 *   <li>Dead: {@code weaponDamage = 15} (:18), {@code toolMaterial} (:13, :17), {@code getMaterialName} (:24-26).</li>
 *   <li>{@code registerIcons} (:63-66) is the item model {@code models/item/ratsword.json}.</li>
 * </ul>
 */
public class RatSword extends BlockingSword {

    private int weaponDamage;
    private final Material toolMaterial;

    /** {@code RatSword(int, ToolMaterial)} (:15-22). */
    public RatSword(final Material par2EnumToolMaterial, final Item.Properties props) {
        super(par2EnumToolMaterial.withUses(1300), props.attributes(OreSpawnTiers.sword(par2EnumToolMaterial)), 3000);
        this.toolMaterial = par2EnumToolMaterial;
        this.weaponDamage = 15;
    }

    /** {@code getMaterialName} (:24-26), unused. */
    public String getMaterialName() {
        return "Rat";
    }

    /**
     * {@code hitEntity} (:28-41). {@code par2EntityLiving} is the struck entity, {@code par3EntityLiving} the attacker.
     * The durability point is taken by {@code postHurtEnemy} after this returns {@code true}.
     */
    @Override
    public boolean hurtEnemy(final ItemStack par1ItemStack, final LivingEntity par2EntityLiving, final LivingEntity par3EntityLiving) {
        final int var2 = 5;
        if (par2EntityLiving != null && !par2EntityLiving.level().isClientSide) {
            final Level world = par2EntityLiving.level();
            for (int num = 1 + world.random.nextInt(6), i = 0; i < num; ++i) {
                Rat r = null;
                final Entity spawned = spawnCreature(world, ModEntities.RAT.get(),
                        par2EntityLiving.getX() + (world.random.nextFloat() - world.random.nextFloat()) * 0.5,
                        par2EntityLiving.getY() + world.random.nextFloat() + 0.01,
                        par2EntityLiving.getZ() + (world.random.nextFloat() - world.random.nextFloat()) * 0.5);
                if (spawned instanceof Rat rat) {
                    r = rat;
                }
                if (r != null) {
                    r.setOwner(par3EntityLiving);
                }
            }
        }
        return true;
    }

    /**
     * {@code spawnCreature} (:43-57) with the name lookup ({@code EntityList.createEntityByName("Rat")}) folded into the
     * type: create, random yaw, add, living sound. No {@code finalizeSpawn}, as the original called no
     * {@code onSpawnWithEgg}. PORT: the unchecked {@code (EntityLiving)} cast is an {@code instanceof} (R18 case 1).
     * The owner is set after the rat joined the level, as in the original.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> type, final double par2, final double par4,
                                       final double par6) {
        final Entity var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }
}
