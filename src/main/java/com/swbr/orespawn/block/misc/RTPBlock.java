package com.swbr.orespawn.block.misc;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code RTPBlock} (RTPBlock.java:13-72): Random Teleport Block, id {@code blockteleport}
 * (OreSpawnMain.java:1541, stone sound). A player who walks on it is moved 9-23 blocks away on each
 * horizontal axis, to the first free two-block-high spot with a solid floor within four blocks of height.
 *
 * <p>No hardness was ever set (:15-18): instantly breakable, no blast resistance. Drops itself.
 *
 * <p>PORT (R18 case 4): the original ran {@code onEntityWalking} on both sides. The client rolled its own
 * target for the local player, moved him there and showed the particles at <em>that</em> spot, while the
 * server rolled a different one and overrode the position a tick later; the sound played twice. Here the
 * server does the roll, teleports through the connection (the {@code EntityPlayerMP} branch, :52-54),
 * and sends particles and sound from the real destination. The {@code setLocationAndAngles} branch
 * (:55-57) was the client player's and has no server-side counterpart.
 *
 * <p>PORT (BUGHUNT2 2.4): the teleport itself runs one tick after the step, see {@link #stepOn}.
 */
public class RTPBlock extends Block {

    /**
     * Players whose teleport is queued but has not run yet. Server thread only. Weak keys: a player object
     * that leaves (logout, world closed before the task ran) must not keep the entry alive, and a re-joined
     * player is a new object anyway.
     */
    private static final Set<ServerPlayer> PENDING = Collections.newSetFromMap(new WeakHashMap<>());

    public RTPBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock} without any setter (:15-18): hardness 0, resistance 0; stone sound from :1541. */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(0.0f, 0.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    /**
     * {@code onEntityWalking} (:20-66), behind the guard 1.7.10 {@code Entity.moveEntity} put in front of
     * it ({@link Legacy#wasWalking}): a sneaking player crossed the block untouched, a rider too.
     *
     * <p>PORT: the step-vs-tick note of {@code OreTitanium.stepOn} matters more here than there.
     * {@code onEntityWalking} fired when the walked distance crossed the next step boundary, so a player
     * standing on the block, or landing on it, was never moved, and one crossing it at a run was moved
     * only if a step boundary fell inside that one block. {@code stepOn} runs on every {@code move} call
     * while on the ground, so here the first contact teleports. The counter behind the boundary
     * ({@code nextStep}) is private in 1.21.1 and would need per-player state to rebuild; not done.
     */
    @Override
    @SuppressWarnings("deprecation") // BlockState.isSolid() is the 1.7.10 Material.isSolid()
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof ServerPlayer p && level instanceof ServerLevel server && Legacy.wasWalking(p)) {
            RandomSource rand = level.random;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            int found = 0;
            for (int tries = 0; tries < 1000 && found == 0; ++tries) {
                if (rand.nextInt(2) == 0) {
                    x = pos.getX() + 16 + rand.nextInt(8) - rand.nextInt(8);
                } else {
                    x = pos.getX() - 16 + rand.nextInt(8) - rand.nextInt(8);
                }
                if (rand.nextInt(2) == 0) {
                    z = pos.getZ() + 16 + rand.nextInt(8) - rand.nextInt(8);
                } else {
                    z = pos.getZ() - 16 + rand.nextInt(8) - rand.nextInt(8);
                }
                for (y = pos.getY() - 4; y <= pos.getY() + 4; ++y) {
                    if (blockAt(level, x, y - 1, z).isSolid() && blockAt(level, x, y, z).isAir() && blockAt(level, x, y + 1, z).isAir()) {
                        found = 1;
                        break;
                    }
                }
            }
            if (found != 0 && PENDING.add(p)) {
                final int tx = x;
                final int ty = y;
                final int tz = z;
                // PORT: BUGHUNT2 2.4 - stepOn runs inside ServerGamePacketListenerImpl.handleMovePlayer ->
                // player.move. A teleport there is undone by the handler right after ("moved wrongly!"),
                // because 1.21.1 compares the packet position with the one before the move; 1.7.10's
                // setPlayerLocation also overwrote lastPosX/Y/Z, so the correction landed at the target.
                // The roll stays here, in the step, with the original order of random calls; teleport,
                // particles and sound run as a server task one tick later, after the move handler has
                // returned (MinecraftServer.tell always queues; execute would run at once on the server
                // thread). PENDING keeps further move packets of the same player, queued before the task,
                // from rolling a second target while the first is still outstanding. Those extra steps still
                // run the search and advance level.random before being discarded; only the random sequence
                // differs from 1.7.10, never the target, sound or particles.
                MinecraftServer mc = server.getServer();
                mc.tell(new TickTask(mc.getTickCount() + 1, () -> {
                    PENDING.remove(p);
                    if (p.isRemoved() || p.level() != server) {
                        return;
                    }
                    // playerNetServerHandler.setPlayerLocation(x + 0.5, y, z + 0.5, yaw, 0) (:53): pitch reset.
                    p.connection.teleport(tx + 0.5, ty, tz + 0.5, p.getYRot(), 0.0f);
                    // Six each of smoke, explode (poof) and reddust at the destination (:58-62), one packet per type.
                    server.sendParticles(ParticleTypes.SMOKE, tx + 0.5, ty + 2.25, tz + 0.5, 6, 0.0, 0.0, 0.0, 0.0);
                    server.sendParticles(ParticleTypes.POOF, tx + 0.5, ty + 2.25, tz + 0.5, 6, 0.0, 0.0, 0.0, 0.0);
                    server.sendParticles(DustParticleOptions.REDSTONE, tx + 0.5, ty + 2.25, tz + 0.5, 6, 0.0, 0.0, 0.0, 0.0);
                    // playSoundAtEntity(p, "random.explode", 1.0, 1.5) (:63); the player already stands at the target.
                    server.playSound(null, p, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
                }));
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    /**
     * 1.7.10 {@code World.getBlock} returned air for an unloaded chunk instead of loading it. The search
     * reaches 24 blocks out, which is always loaded around a player, but the fallback keeps the exact
     * behaviour and never forces a chunk load from a step.
     */
    private static BlockState blockAt(Level level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return level.hasChunkAt(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }
}
