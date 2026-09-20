package com.swbr.orespawn.item.utility;

import javax.annotation.Nullable;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared pieces of the one-use building items (no original class). Every 1.7.10 builder in this
 * package repeats the same four things - the {@code random.explode} sound, the
 * {@code (int)(pos + 0.99 * dir)} floor approximation, the "one less outside creative" line and a
 * handful of block-identity tests that need a 1.21.1 translation. They live here once, so the
 * item classes read like their originals.
 */
public final class OneUse {

    private OneUse() {
    }

    /**
     * {@code Player.worldObj.playSoundAtEntity(Player, "random.explode", volume, pitch)}. The
     * original called it on both sides and the server broadcast it to everyone in range, so the
     * clicking player heard it twice in 1.7.10 (once local, once from the server).
     * PORT: the standard 1.21.1 split - the server excludes the acting player, the client plays it
     * for him - gives every player exactly one playback; there is no way to ask for the duplicate.
     */
    static void playExplode(final Level world, final Player player, final float volume, final float pitch) {
        world.playSound(player, player, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, volume, pitch);
    }

    /**
     * {@code if (!Player.capabilities.isCreativeMode) --par1ItemStack.stackSize;}
     * PORT: the player of a {@code UseOnContext} is nullable in 1.21.1; none counts as
     * "not creative", as in {@code ItemSpawnEgg} and {@code ItemAppleSeed}.
     */
    static void consumeUnlessCreative(final ItemStack stack, @Nullable final Player player) {
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * {@code (int)(pos + 0.99 * dir)} with {@code dir = -1} when the clicked coordinate is negative
     * (InstantShelter.java:31-39, InstantGarden.java:29-37, ItemMinersDream.java:30-38,
     * ZooCage.java:30-38). It is the original's floor for negative coordinates - a hair off at
     * {@code pos = n + 0.005}, and decided by the <em>clicked</em> block's sign, not the player's.
     * Kept as it is (DECISIONS R18).
     */
    static int legacyFloor(final double pos, final int clicked) {
        final int dir = clicked < 0 ? -1 : 0;
        return (int) (pos + 0.99 * dir);
    }

    /**
     * {@code bid == Blocks.dirt}. 1.7.10 {@code dirt} carried coarse dirt (meta 1) and podzol (meta 2) as metadata
     * of the same block.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): "dirt" is a category, so the whole {@code #minecraft:dirt} tag as
     * 1.21.1 ships it counts, grass block and mycelium included; a caller with its own {@code Blocks.grass} branch
     * has to test that branch first. Same rule as {@code CropBlocks.isGrassDirtOrFarmland}.
     */
    public static boolean isDirt(final BlockState bid) {
        return bid.is(BlockTags.DIRT);
    }

    /** {@code bid == Blocks.sand}. PORT: red sand was meta 1 of the same block in 1.7.10. */
    static boolean isSand(final BlockState bid) {
        return bid.is(Blocks.SAND) || bid.is(Blocks.RED_SAND);
    }

    /** {@code bid == Blocks.flowing_water || bid == Blocks.water}: one block with a level in 1.21.1. */
    static boolean isWater(final BlockState bid) {
        return bid.is(Blocks.WATER);
    }

    /** {@code bid == Blocks.flowing_lava || bid == Blocks.lava}: one block with a level in 1.21.1. */
    static boolean isLava(final BlockState bid) {
        return bid.is(Blocks.LAVA);
    }

    /**
     * {@code bid == Blocks.air} and {@code world.isAirBlock(...)}. PORT: 1.7.10 had one air block;
     * 1.21.1 fills caves with {@code cave_air} and the void with {@code void_air}. A strict
     * {@code is(Blocks.AIR)} would count every cave ceiling as solid and never roof a tunnel that
     * crosses one, so the port tests {@link BlockState#isAir()} - what "air" meant in 1.7.10.
     */
    static boolean isAir(final BlockState bid) {
        return bid.isAir();
    }

    /**
     * The client-side burst of the three Step items (StepUp.java:78-82, StepDown.java:78-82,
     * StepAccross.java:78-82): six times {@code largesmoke}, {@code largeexplode} and
     * {@code reddust} at {@code x + f - f}, {@code y + f + yLift}, {@code z + f - f}, no motion.
     * {@code yLift} is {@code 1.0f} for StepUp and {@code 0} for the other two.
     */
    static void stepParticles(final Level world, final int x, final int y, final int z, final float yLift) {
        for (int var3 = 0; var3 < 6; ++var3) {
            world.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + world.random.nextFloat() - world.random.nextFloat(),
                    y + world.random.nextFloat() + yLift,
                    z + world.random.nextFloat() - world.random.nextFloat(), 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.EXPLOSION,
                    x + world.random.nextFloat() - world.random.nextFloat(),
                    y + world.random.nextFloat() + yLift,
                    z + world.random.nextFloat() - world.random.nextFloat(), 0.0, 0.0, 0.0);
            world.addParticle(DustParticleOptions.REDSTONE,
                    x + world.random.nextFloat() - world.random.nextFloat(),
                    y + world.random.nextFloat() + yLift,
                    z + world.random.nextFloat() - world.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    /**
     * The eight-sector switch of the three Step items (StepUp.java:27-72 and twins):
     * {@code f = ((rotationYawHead + 22.5) % 360) / 45}, truncated to {@code int}, mapped to a
     * horizontal step. Sectors outside 0..7 leave both deltas at 0 and the item does nothing -
     * <strong>including every negative yaw below -67.5 degrees</strong>, because Java's {@code %}
     * keeps the sign and the cast truncates toward zero (yaw in (-67.5, 0) lands in sector 0,
     * south). The server player's {@code yRot} is {@code yaw % 360} of what the client sent
     * ({@code Entity.absRotateTo}) and {@code yHeadRot} follows it ({@code Player.tick}), so the
     * negative half of the circle is reached as in 1.7.10. Kept on purpose: DECISIONS R18
     * ("Step* bei negativem Gierwinkel: 1:1").
     *
     * @return {@code {deltax, deltaz}}
     */
    static int[] stepDirection(final float rotationYawHead) {
        int deltax = 0;
        int deltaz = 0;
        float f = rotationYawHead;
        f += 22.5f;
        f %= 360.0f;
        f /= 45.0f;
        switch ((int) f) {
            case 0 -> {
                deltax = 0;
                deltaz = 1;
            }
            case 1 -> {
                deltax = -1;
                deltaz = 1;
            }
            case 2 -> {
                deltax = -1;
                deltaz = 0;
            }
            case 3 -> {
                deltax = -1;
                deltaz = -1;
            }
            case 4 -> {
                deltax = 0;
                deltaz = -1;
            }
            case 5 -> {
                deltax = 1;
                deltaz = -1;
            }
            case 6 -> {
                deltax = 1;
                deltaz = 0;
            }
            case 7 -> {
                deltax = 1;
                deltaz = 1;
            }
            default -> {
                // no sector: both deltas stay 0, the caller returns false
            }
        }
        return new int[] {deltax, deltaz};
    }
}
