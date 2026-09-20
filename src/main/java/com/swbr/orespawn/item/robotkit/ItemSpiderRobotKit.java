package com.swbr.orespawn.item.robotkit;

import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemSpiderRobotKit} (ItemSpiderRobotKit.java:11-72, verhalten/itemblock-02.md): one
 * class for {@code spiderrobotkit} "Spider Robot Kit" (BaseItemID + 471, OreSpawnMain.java:1385) and
 * {@code antrobotkit} "Red Ant Robot Kit" (+473, :1386), creative tab tools, stack 1. Right-clicking a block unpacks the
 * robot with the health the kit carries: the item damage is the missing health, so a fresh kit gives a fully repaired
 * robot. {@code ItemWrench} packs it back.
 *
 * <p>The original told the two kits apart by the numeric constructor argument ({@code i == BaseItemID + 471}, :16) and
 * by identity with {@code OreSpawnMain.AntRobotKit} (:30); numeric ids do not exist in 1.21.1 (R2), so the port takes
 * the {@link Kit} as a constructor argument (verhalten/core-01a.md §3.1).
 *
 * <p>R3: the durability is the robot's maximum health from the config, consumed at registration and therefore read
 * through {@code EarlyConfig} ({@link StatSource#EARLY}); a changed value needs a restart, as in the original.
 *
 * <p>R4: the Spider Robot's 1500 lies above the attribute clamp. The kit keeps the original units (durability 1500) and
 * writes the health through {@link VirtualHealth#setOriginalHealth}; the Red Ant Robot's scale is 1, so the same call is
 * exact there too.
 */
public class ItemSpiderRobotKit extends Item {

    /** Which robot a kit holds - the original's {@code BaseItemID + 471} / {@code + 473} and its entity name. */
    public enum Kit {
        /** {@code SpiderRobotKit}: "Robot Spider" ({@code robot_spider}), durability {@code SpiderRobot_stats.health}. */
        SPIDER_ROBOT("orespawn:robot_spider"),
        /** {@code AntRobotKit}: "Robot Red Ant" ({@code robot_red_ant}), durability {@code AntRobot_stats.health}. */
        ANT_ROBOT("orespawn:robot_red_ant");

        private final String entityId;

        Kit(final String entityId) {
            this.entityId = entityId;
        }

        /** {@code setMaxDamage} (:16-21) at registration time (R3). */
        int maxDamage() {
            if (this == SPIDER_ROBOT) {
                return MobStats.SpiderRobot_stats(StatSource.EARLY).health();
            }
            return MobStats.AntRobot_stats(StatSource.EARLY).health();
        }
    }

    private final Kit kit;

    /** {@code ItemSpiderRobotKit(int)} (:13-22): stack 1, durability the robot's maximum health. */
    public ItemSpiderRobotKit(final Kit kit, final Item.Properties props) {
        // PORT: CreativeTabs.tabTools (:15) is filed by the registry holder (ModCreativeTabs.OriginalTab.TOOLS).
        super(props.durability(kit.maxDamage()));
        this.kit = kit;
    }

    /** The robot this kit holds. */
    public Kit getKit() {
        return this.kit;
    }

    /**
     * {@code onItemUse} (:24-50). Client: handled. Server: spawn the robot on top of the clicked block, set its health
     * to {@code maxDamage - damage}, give it the kit's custom name, play {@code random.explode}, mark an ant robot as
     * owned, and use up the kit outside creative - whether or not anything spawned.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level par3World = context.getLevel();
        if (par3World.isClientSide) {
            return InteractionResult.SUCCESS; // :25-27
        }
        final ItemStack par1ItemStack = context.getItemInHand();
        final Player par2EntityPlayer = context.getPlayer();
        final BlockPos pos = context.getClickedPos();
        String name = null;
        name = Kit.SPIDER_ROBOT.entityId; // "Robot Spider"
        if (this.kit == Kit.ANT_ROBOT) {
            name = Kit.ANT_ROBOT.entityId; // "Robot Red Ant"
        }
        final Entity ent = spawnCreature(par3World, 0, name, pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
        if (ent != null) {
            // PORT: the unchecked (EntityLiving) cast (:35) becomes a type test; both kit types are living entities.
            if (ent instanceof LivingEntity e) {
                // setHealth(getMaxDamage() - getDamage(stack)) (:36), in original units (R4).
                VirtualHealth.setOriginalHealth(e, (float) (par1ItemStack.getMaxDamage() - par1ItemStack.getDamageValue()));
            }
            if (ent instanceof Mob && par1ItemStack.has(DataComponents.CUSTOM_NAME)) { // :37-39
                ent.setCustomName(par1ItemStack.getHoverName());
            }
            if (par2EntityPlayer != null) {
                // playSoundAtEntity(player, "random.explode", 1.0, rand * 0.2 + 0.9) (:40): server-only here, so every
                // player in range hears it once, the clicking player included.
                par3World.playSound(null, par2EntityPlayer, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f,
                        par3World.random.nextFloat() * 0.2f + 0.9f);
            }
            if (ent instanceof AntRobot a) { // :41-44
                a.setOwned();
            }
        }
        // PORT: UseOnContext.getPlayer() is nullable in 1.21.1; no player counts as "not creative" (ItemSpawnEgg).
        if (par2EntityPlayer == null || !par2EntityPlayer.getAbilities().instabuild) { // :46-48
            par1ItemStack.shrink(1);
        }
        // PORT: 1.7.10 returned true on both sides; CONSUME is the server-side "handled" result (ItemSpawnEgg).
        return InteractionResult.CONSUME;
    }

    /**
     * {@code spawnCreature} (:52-66): {@code EntityList.createEntityByName} is {@link EntityType#byString} on the
     * registry id (R2 maps "Robot Spider" to {@code robot_spider}), then {@code setLocationAndAngles} with a random yaw,
     * spawn, and the ambient sound. Numeric ids ({@code name == null}) do not exist in 1.21.1; that branch spawns
     * nothing. No {@code finalizeSpawn}, as in the original.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final int par1, @Nullable final String name,
                                       final double par2, final double par4, final double par6) {
        Entity var8 = null;
        if (name != null) {
            final EntityType<?> type = EntityType.byString(name).orElse(null);
            var8 = type == null ? null : type.create(par0World);
        }
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            // PORT: the unchecked (EntityLiving) cast (:63) becomes a type test.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }
}
