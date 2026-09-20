package com.swbr.orespawn.item.rock;

import com.swbr.orespawn.entity.rock.EntityThrownRock;
import com.swbr.orespawn.entity.rock.RockBase;
import com.swbr.orespawn.registry.ModEntities;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemRock} (ItemRock.java:11-146): the twelve throwable rocks,
 * one class, stack 64, creative tab {@code tabCombat} (:13-16). Right-click into the air throws an
 * {@link EntityThrownRock} (:18-62); right-click on a block places a {@link RockBase} entity on top
 * of it (:64-117). Icon {@code OreSpawn:<id>} (:142-145) is the {@code item/generated} model of the id.
 *
 * <p>The original told the twelve items apart by identity against the {@code OreSpawnMain}
 * fields (:24-59, :75-110). The port gives each item its type number in the constructor - the same
 * table, read from one place (ItemRock.java:24-59, OreSpawnMain.java:1396-1407):
 *
 * <table>
 * <tr><th>type</th><th>id</th><th>name</th><th>legacy id</th></tr>
 * <tr><td>1</td><td>{@code rocksmall}</td><td>Small Rock</td><td>9436</td></tr>
 * <tr><td>2</td><td>{@code rock}</td><td>Big Rock</td><td>9435</td></tr>
 * <tr><td>3</td><td>{@code rockred}</td><td>Flame Rock</td><td>9437</td></tr>
 * <tr><td>4</td><td>{@code rockgreen}</td><td>Poison Rock</td><td>9438</td></tr>
 * <tr><td>5</td><td>{@code rockblue}</td><td>Slowness Rock</td><td>9439</td></tr>
 * <tr><td>6</td><td>{@code rockpurple}</td><td>Weakness Rock</td><td>9440</td></tr>
 * <tr><td>7</td><td>{@code rockspikey}</td><td>Painful Rock</td><td>9441</td></tr>
 * <tr><td>8</td><td>{@code rocktnt}</td><td>Explosive Rock</td><td>9442</td></tr>
 * <tr><td>9</td><td>{@code rockcrystalred}</td><td>Flame Crystal</td><td>9443</td></tr>
 * <tr><td>10</td><td>{@code rockcrystalgreen}</td><td>Poison Crystal</td><td>9444</td></tr>
 * <tr><td>11</td><td>{@code rockcrystalblue}</td><td>Slowness Crystal</td><td>9445</td></tr>
 * <tr><td>12</td><td>{@code rockcrystaltnt}</td><td>Explosive Crystal</td><td>9446</td></tr>
 * </table>
 *
 * {@link #byType(int)} is the reverse table for the two entities, which drop "their" item
 * (EntityThrownRock.java:256-293, RockBase.java:209-247), and for the dispenser
 * ({@link MyDispenserBehaviorRock}); it is filled by the constructor, so the registry holders can
 * carry any name.
 */
public class ItemRock extends Item {

    /** Type number to item, filled as the twelve holders construct their item. */
    private static final Map<Integer, ItemRock> BY_TYPE = new ConcurrentHashMap<>();

    /** The type this item throws and places (1-12). */
    private final int rockType;

    /**
     * {@code ItemRock(id)} (:13-16): {@code maxStackSize = 64}, {@code tabCombat}.
     *
     * @param rockType the type number of the table above
     */
    public ItemRock(final int rockType, final Item.Properties props) {
        super(props.stacksTo(64));
        this.rockType = rockType;
        BY_TYPE.put(rockType, this);
    }

    /** The type number this item throws and places. */
    public int getRockType() {
        return this.rockType;
    }

    /** The item of a type number, or {@code null} for an unregistered type (the original's type 0). */
    @Nullable
    public static ItemRock byType(final int type) {
        return BY_TYPE.get(type);
    }

    /** Every rock item constructed so far; used to register the dispenser behaviour. */
    public static Collection<ItemRock> all() {
        return Collections.unmodifiableCollection(BY_TYPE.values());
    }

    /**
     * {@code onItemRightClick} (:18-62): one less outside creative on both sides (:19-21), the
     * {@code random.bow} throw sound at 0.5 / {@code 0.4 / (rand * 0.4 + 0.8)} (:22), and on the
     * server a thrown rock of this type (:23-60). The result is {@code CONSUME} on both sides: in
     * 1.7.10 a used item re-equipped without an arm swing, which is what {@code CONSUME} does in
     * 1.21.1 ({@code shouldSwing()} false, {@code itemUsed} re-equips).
     *
     * <p>PORT: {@code playSoundAtEntity} on both sides played the sound twice for the thrower in
     * 1.7.10 (local and broadcast); {@code level.playSound(null, ...)} plays it once for everyone,
     * the same split {@code SnowballItem} uses. The throw velocity (1.5, spread 1.0) sat in the
     * 1.7.10 {@code EntityThrowable} constructor and is set in the port's entity constructor.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level par2World, final Player par3EntityPlayer, final InteractionHand hand) {
        final ItemStack par1ItemStack = par3EntityPlayer.getItemInHand(hand);
        if (!par3EntityPlayer.getAbilities().instabuild) { // :19-21
            par1ItemStack.shrink(1);
        }
        par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5f,
                0.4f / (par2World.getRandom().nextFloat() * 0.4f + 0.8f)); // :22 "random.bow"
        if (!par2World.isClientSide) { // :23-60
            par2World.addFreshEntity(new EntityThrownRock(par2World, par3EntityPlayer, this.rockType));
        }
        return InteractionResultHolder.consume(par1ItemStack); // :61
    }

    /**
     * {@code onItemUse} (:64-117). Server side: negative block coordinates move one towards zero
     * (:66-71), then {@code spawnCreature("Rock", x, y + 1.01, z)} (:72) and {@code placeRock}
     * with this item's type (:73-111). One rock less outside creative on both sides, whether or
     * not the entity was created (:113-115); {@code true} on both sides (:116), which swung the arm.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final Player par2EntityPlayer = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        final BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        final int y = pos.getY();
        int z = pos.getZ();
        if (!world.isClientSide) { // :65
            if (x < 0) { // :66-68
                ++x;
            }
            if (z < 0) { // :69-71
                ++z;
            }
            final Entity e = spawnCreature(world, x, y + 1.01, z); // :72
            if (e != null) { // :73-111
                final RockBase r = (RockBase) e;
                r.placeRock(this.rockType);
            }
        }
        // PORT: the player of a UseOnContext is nullable in 1.21.1; none counts as "not creative",
        // as in ItemSpawnEgg and the W03 builders.
        if (par2EntityPlayer == null || !par2EntityPlayer.getAbilities().instabuild) { // :113-115
            par1ItemStack.shrink(1);
        }
        return InteractionResult.sidedSuccess(world.isClientSide); // :116
    }

    /**
     * {@code spawnCreature} (:119-140) with {@code EntityList.createEntityByName("Rock")} resolved
     * to the {@code rock} entity type. The half-block centring is sign-based: {@code +0.5} above
     * zero, {@code -0.5} below zero and <strong>nothing at exactly zero</strong> (:123-134), so a
     * rock placed on the x = 0 or z = 0 column stands on the block edge, and after the
     * {@code ++x} of {@code useOn} the same happens on the x = -1 column. Kept as it is
     * (DECISIONS R18, "ItemRock-Versatz"). Random yaw, pitch 0, one more hundredth up (:135),
     * added to the level (:136) and the ambient sound played (:137, a null sound for a rock).
     */
    @Nullable
    private static Entity spawnCreature(final Level par0World, double par2, final double par4, double par6) {
        Entity var8 = null;
        var8 = ModEntities.ROCK.get().create(par0World); // :121
        if (var8 != null) {
            if (par2 > 0.0) { // :123-125
                par2 += 0.5;
            }
            if (par2 < 0.0) { // :126-128
                par2 -= 0.5;
            }
            if (par6 > 0.0) { // :129-131
                par6 += 0.5;
            }
            if (par6 < 0.0) { // :132-134
                par6 -= 0.5;
            }
            var8.moveTo(par2, par4 + 0.01, par6, par0World.random.nextFloat() * 360.0f, 0.0f); // :135
            par0World.addFreshEntity(var8); // :136
            ((Mob) var8).playAmbientSound(); // :137 playLivingSound
        }
        return var8;
    }
}
