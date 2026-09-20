package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers the seven W06 critters ({@link Cockateil}, {@link RubyBird}, {@link CliffRacer}, {@link Cricket},
 * {@link Dragonfly}, {@link Coin}, {@link Tshirt}) share. No original class: each method stands for a
 * 1.7.10 idiom the originals repeated inline or inherited from vanilla.
 */
public final class CritterSupport {

    /** {@code OreSpawnMain.DimensionID4}, the Islands dimension ({@code orespawn:danger}, R13). */
    public static final ResourceKey<Level> DIMENSION_ISLANDS = OreSpawnTeleporter.DANGER;

    private CritterSupport() {
    }

    /**
     * {@code canSeeTarget(pX, pY, pZ)} of the three flyers:
     * {@code worldObj.rayTraceBlocks(Vec3(posX, posY + eye, posZ), Vec3(pX, pY, pZ), false) == null}.
     *
     * <p>1.7.10 {@code World.func_147447_a(from, to, false, false, false)} tested every block whose
     * {@code canCollideCheck(meta, false)} held (air and liquids do not) against its selection bounds
     * ({@code collisionRayTrace} after {@code setBlockBoundsBasedOnState}) - that is the outline shape with
     * fluids ignored, the same mapping {@code UltimateFishHook} and {@code LegacyArrow} use.
     */
    public static boolean canSeeTarget(final Entity entity, final double eye, final double pX, final double pY, final double pZ) {
        return entity.level().clip(new ClipContext(new Vec3(entity.getX(), entity.getY() + eye, entity.getZ()),
                new Vec3(pX, pY, pZ), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    /**
     * 1.7.10 {@code EntityLiving.dropFewItems(recentlyHit, looting)} for a class that only overrides
     * {@code getDropItem}: nothing for {@code null}, otherwise {@code rand(3)} single items, plus
     * {@code rand(looting + 1)} with Looting. {@code EntityLivingBase.onDeath} took the Looting level only
     * from a player killer. The caller has already asked {@code getDropItem}, which the original did first
     * inside this method, so the random draws keep their order (R10: code, not a loot table).
     */
    public static void dropFewItems(final Mob mob, final ServerLevel level, final DamageSource damageSource, @Nullable final Item item) {
        int looting = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        if (item == null) {
            return;
        }
        int j = mob.getRandom().nextInt(3);
        if (looting > 0) {
            j += mob.getRandom().nextInt(looting + 1);
        }
        for (int k = 0; k < j; ++k) {
            mob.spawnAtLocation(new ItemStack(item, 1));
        }
    }
}
