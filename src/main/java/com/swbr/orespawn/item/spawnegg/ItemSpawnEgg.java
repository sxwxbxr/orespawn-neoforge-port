package com.swbr.orespawn.item.spawnegg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Port of {@code danger.orespawn.ItemSpawnEgg} and {@code DispenserBehaviorOreSpawnEgg}.
 *
 * <p>The original resolved a numeric {@code my_id} through a 114-arm {@code switch} to either a
 * vanilla entity id or a global entity name. Here every egg is constructed with the
 * {@link EntityType} it spawns (DECISIONS R9, catalogue README 4.1 point 2); the switch is gone, the
 * spawn path is the same. Each egg keeps its own original icon through an {@code item/generated}
 * model - no {@code DeferredSpawnEggItem}, no tinting (R9, corrected).
 *
 * <p>What the original does <em>not</em> do is also kept: the class reads no {@code *Enable}
 * config key, and {@code OreSpawnMain} registers every egg unconditionally
 * ({@code OreSpawnMain.java:2004-2008, 5183-5187}). A disabled mob can still be spawned from its
 * egg, exactly as in 1.7.10.
 */
public class ItemSpawnEgg extends Item {

    /** Every egg constructed so far, in registration order; see {@link #all()}. */
    private static final List<ItemSpawnEgg> ALL = Collections.synchronizedList(new ArrayList<>());

    /** Replaces {@code my_id}: the type this egg spawns, resolved lazily because entity types
     *  register after items in this mod's holder order. */
    private final Supplier<? extends EntityType<?>> type;

    /**
     * @param type  the entity type to spawn (the original's {@code my_id} switch arm)
     * @param props item properties from the registry holder; stack size 64 is applied here to
     *              mirror {@code ItemSpawnEgg.java:19}
     */
    public ItemSpawnEgg(Supplier<? extends EntityType<?>> type, Item.Properties props) {
        // PORT: CreativeTabs.tabMisc (ItemSpawnEgg.java:20) has no 1.21.1 counterpart; the eggs go
        // into CreativeModeTabs.SPAWN_EGGS via SpawnEggSetup.
        super(props.stacksTo(64));
        this.type = type;
        ALL.add(this);
    }

    /** The entity type this egg spawns. */
    public EntityType<?> getType() {
        return this.type.get();
    }

    /**
     * All spawn eggs of every wave, in registration order. Used by {@link SpawnEggSetup} for the
     * dispenser behaviour and the creative tab so later waves only have to construct their eggs.
     */
    public static List<ItemSpawnEgg> all() {
        return Collections.unmodifiableList(ALL);
    }

    /**
     * {@code onItemUse} (ItemSpawnEgg.java:23-35). Spawns at the centre of the clicked block, one
     * block and a hair above it - the clicked face is ignored, as in the original. One egg is
     * consumed outside creative mode whether or not anything spawned.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS; // :24-26, original returns true on the client
        }
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Entity ent = spawnSomething(this.getType(), level,
                pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5); // :27
        if (ent != null && ent instanceof Mob && stack.has(DataComponents.CUSTOM_NAME)) { // :28-30
            ent.setCustomName(stack.getHoverName());
        }
        Player player = context.getPlayer();
        // PORT: UseOnContext.getPlayer() is nullable in 1.21.1 (the original always had a player);
        // no player counts as "not creative", so the egg is consumed.
        if (player == null || !player.getAbilities().instabuild) { // :31-33
            stack.shrink(1);
        }
        // PORT: 1.7.10 returned true on both sides; CONSUME is the server-side "handled" result.
        return InteractionResult.CONSUME;
    }

    /**
     * {@code spawn_something} + {@code spawnCreature} (ItemSpawnEgg.java:37-525) with the id switch
     * folded into {@code type}. Creates the entity, places it with a random yaw and pitch 0, adds
     * it to the level and plays its ambient sound. No {@code finalizeSpawn}: the original went
     * through {@code EntityList.createEntityByName} and never called {@code onSpawnWithEgg}, so
     * eggs gave no random equipment or spawn-time setup either.
     *
     * @return the spawned entity, or {@code null} if the type could not be created (the original
     *         returned null for an unknown id or name)
     */
    public static Entity spawnSomething(EntityType<?> type, Level level, double x, double y, double z) {
        // PORT: case 192 set skelly_type = 1 on a vanilla skeleton (:43-44, :503-506); the wither
        // skeleton is its own EntityType in 1.21.1, so the egg simply carries WITHER_SKELETON (R18).
        Entity ent = type.create(level); // :514-518, createEntityByID / createEntityByName
        if (ent != null) {
            ent.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0f, 0.0f); // :520
            level.addFreshEntity(ent); // :521
            // PORT: the original cast to EntityLiving unchecked (:522); every egg type is a Mob,
            // the instanceof only turns an impossible ClassCastException into a no-op (R18 case 1).
            if (ent instanceof Mob mob) {
                mob.playAmbientSound(); // playLivingSound
            }
        }
        return ent;
    }

    /**
     * Port of the package-private {@code DispenserBehaviorOreSpawnEgg} (lines 11-26). Registered
     * for every egg by {@link SpawnEggSetup}; stateless, so one instance serves all eggs.
     */
    public static final class DispenserBehaviorOreSpawnEgg extends DefaultDispenseItemBehavior {

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.state().getValue(DispenserBlock.FACING); // :12
            BlockPos pos = source.pos();
            // :13-15 - 1.7.10 IBlockSource.getX()/getZ() are block + 0.5, getYInt() is the block
            // Y. The facing's Y component is ignored: an up- or down-facing dispenser spawns on
            // its own level. The +0.2f is a float, as in the original.
            double d0 = (pos.getX() + 0.5) + facing.getStepX() * 2.0;
            double d2 = pos.getY() + 0.2f;
            double d3 = (pos.getZ() + 0.5) + facing.getStepZ() * 2.0;
            Item it = stack.getItem();
            if (it instanceof ItemSpawnEgg ise) { // :17-18
                // :19 - the (int) casts put the mob on a block corner. PORT: Mth.floor (DECISIONS
                // R20): truncation moved it one block towards zero at negative x/z and one block up
                // at negative y, which the 1.7.10 world (y >= 0) never reached.
                Entity entity = spawnSomething(ise.getType(), source.level(), Mth.floor(d0), Mth.floor(d2), Mth.floor(d3));
                if (entity instanceof LivingEntity && stack.has(DataComponents.CUSTOM_NAME)) { // :20-22
                    entity.setCustomName(stack.getHoverName());
                }
            }
            stack.split(1); // :24 - always, even when nothing spawned or the item is no egg
            return stack;
        }
    }
}
