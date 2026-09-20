package com.swbr.orespawn.entity.portal;

import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.EntityUnstableAnt} (EntityUnstableAnt.java:9-53): {@code unstable_ant},
 * passive portal to the Islands dimension ({@code DimensionID4}, id {@code orespawn:danger} per R13;
 * the research calls it "Danger Dimension") - "Unstable Ant", OreSpawnMain.java:3339-3343, tracking
 * 16/1/false. Values from the base class; Panic and Wander(9) added a second time (:17-18), kept (R18).
 */
public class EntityUnstableAnt extends EntityAnt {

    /** {@code EntityUnstableAnt(World)} (:11-19). */
    public EntityUnstableAnt(final EntityType<? extends EntityUnstableAnt> type, final Level par1World) {
        super(type, par1World);
        // setSize(0.1f, 0.1f) is the EntityType size; experienceValue = 0 is dead.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, LegacyPanic.legacyPanic(this, 1.399999976158142));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 9, 1.0));
    }

    /** {@code applyEntityAttributes} (:21-27): health 1, speed 0.15, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15000000596046448)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code interact} (:29-52): into {@code DimensionID4}, or home from there. */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        final InteractionResult pass = checkPortalUse(par1EntityPlayer, hand);
        if (pass != null) {
            return pass;
        }
        if (par1EntityPlayer.level().dimension() != OreSpawnTeleporter.DANGER) {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, OreSpawnTeleporter.DANGER, this.level());
        } else {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, Level.OVERWORLD, this.level());
        }
        return portalUsed();
    }
}
