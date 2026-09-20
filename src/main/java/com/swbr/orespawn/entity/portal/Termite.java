package com.swbr.orespawn.entity.portal;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Termite} (Termite.java:14-279): {@code termite}, the wood-eating ant and
 * portal to the Crystal dimension ("Termite", OreSpawnMain.java:3809-3813, tracking 32/1/false).
 *
 * <p>Health 5 (:49-52), speed 0.2 (:30), hitbox 0.2 (:29), attack attribute 2.0 (:46) which the bite
 * never reads - it deals a fixed 1.0 with a one-in-fifteen chance, not on Peaceful (:54-63). The 20-tick
 * bite clock (:103-123) has no {@code PlayNicely} test, unlike the Red Ant's. Travel to the Crystal
 * dimension needs an empty hand, an empty inventory and no armour; the way home needs only the hand
 * (:65-101).
 *
 * <p>Wood eating (:212-263): with 1 in 200 per AI tick and {@code PlayNicely == 0}, the nearest wooden
 * block in growing shells around the head is walked to; within squared distance 6 it becomes dirt (2 in
 * 3) or air (1 in 3) when mob griefing allows, a new termite appears while fewer than 10 are within 3
 * blocks - with or without griefing - and the eater heals 1.
 */
public class Termite extends EntityAnt {

    int attack_delay;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Termite(World)} (:22-39); the base constructor has already added Panic and Wander(9). */
    public Termite(final EntityType<? extends Termite> type, final Level par1World) {
        super(type, par1World);
        this.attack_delay = 20;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(0.2f, 0.2f) is the EntityType size; experienceValue = 1 is dead.
        this.moveSpeed = 0.20000000298023224;
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, LegacyPanic.legacyPanic(this, 1.399999976158142));
        this.goalSelector.addGoal(1, new EntityAIAttackOnCollide(this, Player.class, 1.0, false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 8, 1.0));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(1, new EntityAINearestAttackableTarget<>(this, Player.class, 6, true));
        }
    }

    /** {@code applyEntityAttributes} (:41-47): health 5, speed, attack 2.0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /** {@code mygetMaxHealth} (:49-52). */
    @Override
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code attackEntityAsMob} (:54-63). */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (OreSpawn.OreSpawnRand.nextInt(15) != 0) {
            return false;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 1.0f);
        return var4;
    }

    /**
     * {@code interact} (:65-101). The messages go to the server-side player only, as
     * {@code addChatComponentMessage} on an {@code EntityPlayerMP} did.
     *
     * <p>PORT: the "Empty your inventory!" test also covers the off-hand slot, which 1.7.10 did not have;
     * without it an item would ride into the Crystal dimension past the rule the check exists for.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (par1EntityPlayer == null) {
            return InteractionResult.PASS;
        }
        if (!(par1EntityPlayer instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        if (!par1EntityPlayer.getMainHandItem().isEmpty()) {
            par1EntityPlayer.sendSystemMessage(Component.literal("Empty your hand!"));
            return InteractionResult.PASS;
        }
        if (par1EntityPlayer.level().dimension() != OreSpawnTeleporter.CRYSTAL) {
            for (int i = 0; i < par1EntityPlayer.getInventory().items.size(); ++i) {
                if (!par1EntityPlayer.getInventory().items.get(i).isEmpty()) {
                    par1EntityPlayer.sendSystemMessage(Component.literal("Empty your inventory!"));
                    return InteractionResult.PASS;
                }
            }
            for (int i = 0; i < par1EntityPlayer.getInventory().offhand.size(); ++i) {
                if (!par1EntityPlayer.getInventory().offhand.get(i).isEmpty()) {
                    par1EntityPlayer.sendSystemMessage(Component.literal("Empty your inventory!"));
                    return InteractionResult.PASS;
                }
            }
            for (int i = 0; i < par1EntityPlayer.getInventory().armor.size(); ++i) {
                if (!par1EntityPlayer.getInventory().armor.get(i).isEmpty()) {
                    par1EntityPlayer.sendSystemMessage(Component.literal("Take off your armor!"));
                    return InteractionResult.PASS;
                }
            }
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, OreSpawnTeleporter.CRYSTAL, this.level());
        } else {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, Level.OVERWORLD, this.level());
        }
        return portalUsed();
    }

    /** {@code onUpdate} (:103-123), both sides; no {@code PlayNicely} test here. */
    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.attack_delay > 0) {
            --this.attack_delay;
        }
        if (this.attack_delay > 0) {
            return;
        }
        this.attack_delay = 20;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        final LivingEntity e = getClosestVulnerablePlayerToEntity(this, 1.5);
        if (e != null) {
            this.doHurtTarget(e);
        }
    }

    /**
     * {@code isWood} (:125-127). PORT: R22 - the original's wooden blocks were categories in 1.7.10 (fence,
     * fence gate, planks, wooden slab and its double, bed, standing sign, wooden door, wooden pressure plate,
     * the wooden stairs), so the port reads the 1.21.1 wooden tags ({@code #minecraft:wooden_fences},
     * {@code fence_gates}, {@code planks}, {@code wooden_slabs} - the slab block holds the double state -,
     * {@code beds}, {@code standing_signs}, {@code wooden_doors}, {@code wooden_pressure_plates},
     * {@code wooden_stairs}, which also brings in the acacia and dark oak stairs the original missed).
     * Crafting table, bookshelf and Crystal Planks are single named blocks and stay single. Wall signs stay
     * out: {@code wall_sign} was its own 1.7.10 block and not in the list, like the redstone torch in R22.
     */
    public boolean isWood(final BlockState bid) {
        return bid.is(BlockTags.WOODEN_FENCES) || bid.is(BlockTags.FENCE_GATES) || bid.is(BlockTags.PLANKS)
                || bid.is(BlockTags.WOODEN_SLABS)
                || (bid.is(BlockTags.BEDS) || bid.is(Blocks.CRAFTING_TABLE))
                || (bid.is(BlockTags.STANDING_SIGNS) || bid.is(Blocks.BOOKSHELF) || bid.is(BlockTags.WOODEN_DOORS)
                        || bid.is(BlockTags.WOODEN_PRESSURE_PLATES))
                || bid.is(BlockTags.WOODEN_STAIRS)
                || bid.is(ModBlocks.CRYSTAL_PLANKS.get());
    }

    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /** {@code scan_it} (:129-210): the six faces of the shell {@code (dx, dy, dz)}; nearer wood wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.blockAt(x + dx, y + i, z + j);
                if (this.isWood(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.blockAt(x - dx, y + i, z + j);
                if (this.isWood(bid)) {
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
                BlockState bid = this.blockAt(x + i, y + dy, z + j);
                if (this.isWood(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.blockAt(x + i, y - dy, z + j);
                if (this.isWood(bid)) {
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
                BlockState bid = this.blockAt(x + i, y + j, z + dz);
                if (this.isWood(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                bid = this.blockAt(x + i, y + j, z - dz);
                if (this.isWood(bid)) {
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
     * {@code updateAITick} (:212-263). The revenge-target roll runs here and again in
     * {@link EntityAnt#customServerAiStep}, which this calls at the end - twice per tick, as in 1.7.10.
     *
     * <p>PORT (R20): {@code (int) posX}, {@code (int) posY + 1}, {@code (int) posZ} are {@code Mth.floor}.
     * PORT: {@code mobGriefing} is asked through {@code EventHooks.canEntityGrief}, which reads the same
     * game rule unless a mod answers the NeoForge event. Flag 2 of {@code setBlock} is
     * {@code UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE}: 1.7.10 had no shape updates, and flag 2 alone would
     * let them run in 1.21.1 (W03 finding).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (this.level().random.nextInt(200) == 1 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 8; ++i) {
                int j = i;
                if (j > 4) {
                    j = 4;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 6) {
                    final BlockPos eaten = new BlockPos(this.tx, this.ty, this.tz);
                    if (this.level().random.nextInt(3) != 0) {
                        if (EventHooks.canEntityGrief(this.level(), this)) {
                            this.level().setBlock(eaten, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                        if (this.findBuddies() < 10) {
                            spawnCreature(this.level(), ModEntities.TERMITE.get(), this.getX() + 0.10000000149011612,
                                    this.getY() + 0.10000000149011612, this.getZ() + 0.10000000149011612);
                        }
                    } else {
                        if (EventHooks.canEntityGrief(this.level(), this)) {
                            this.level().setBlock(eaten, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                        if (this.findBuddies() < 10) {
                            // float arithmetic as written: tx + 0.1f is a float before it widens.
                            spawnCreature(this.level(), ModEntities.TERMITE.get(), this.tx + 0.1f, this.ty + 0.1f, this.tz + 0.1f);
                        }
                    }
                    this.heal(1.0f);
                }
            }
        }
        super.customServerAiStep();
    }

    /** {@code findBuddies} (:265-268): termites only, {@code expand(3, 3, 3)}, this one included. */
    private int findBuddies() {
        final List<Termite> var5 = this.level().getEntitiesOfClass(Termite.class, this.getBoundingBox().inflate(3.0, 3.0, 3.0));
        return var5.size();
    }

    /** {@code spawnCreature} (:270-278): create, random yaw, add - no ambient sound, unlike the nests. */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> type, final double par2,
                                       final double par4, final double par6) {
        Entity var8 = null;
        var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
        }
        return var8;
    }
}
