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
 * Port of {@code danger.orespawn.EntityRainbowAnt} (EntityRainbowAnt.java:9-53): {@code rainbow_ant},
 * passive portal to VillageMania ("Rainbow Ant", OreSpawnMain.java:3331-3335, tracking 16/1/false).
 * Health 1, speed 0.15, attack 0, hitbox 0.1, no XP field effect - all from the base class.
 *
 * <p>The constructor adds Panic(1.4) and Wander(9) a second time (:17-18). The catalogue suggested
 * dropping the duplicates; they stay (R18): two wander goals roll their one-in-thirty chance
 * independently, which is how this ant wandered in 1.7.10.
 */
public class EntityRainbowAnt extends EntityAnt {

    /** {@code EntityRainbowAnt(World)} (:11-19). */
    public EntityRainbowAnt(final EntityType<? extends EntityRainbowAnt> type, final Level par1World) {
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

    /** {@code interact} (:29-52): into {@code DimensionID3}, or home from there. */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        final InteractionResult pass = checkPortalUse(par1EntityPlayer, hand);
        if (pass != null) {
            return pass;
        }
        if (par1EntityPlayer.level().dimension() != OreSpawnTeleporter.VILLAGE) {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, OreSpawnTeleporter.VILLAGE, this.level());
        } else {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, Level.OVERWORLD, this.level());
        }
        return portalUsed();
    }
}
