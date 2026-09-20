package com.swbr.orespawn.item.cage;

import com.swbr.orespawn.entity.cage.EntityCage;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.CritterCage} (verhalten/itemblock-01.md, "CritterCage"): 114 items of
 * one class, stack 16. The empty cage ({@code cage_id} 160) is thrown as an {@link EntityCage}; every
 * other cage releases its creature on the clicked block and drops an empty cage from it.
 *
 * <p><b>The second constructor argument</b> (catalogue README 6.5, open until this wave):
 * {@code OreSpawnMain.java:5069-5182} builds every cage as {@code new CritterCage(BaseItemID + n, j)}.
 * The first argument is the 1.7.10 numeric item id and is ignored by the constructor (R2: numeric ids
 * are gone). The second, {@code j}, is stored as {@code cage_id} (CritterCage.java:16-18) and has two
 * readers: the release {@code switch} in {@link #useOn} (:54-506), and - for the empty cage only - the
 * throw, which hands it to {@code EntityCage.my_index}, the {@code spinners.png} tile of the flying cage
 * (CritterCage.java:31, RenderCage.java:12). Since only the empty cage is ever thrown, the tile is always
 * 160. The numbers are not otherwise meaningful: 150-154 and 357-384 were added later and are not in
 * item-id order. They are kept verbatim so the switch below reads like the original.
 *
 * <p>No data component: the original stored nothing on the stack. The caught creature is the item
 * itself, one registration per creature (R9).
 */
public class CritterCage extends Item {

    public int cage_id;

    /**
     * {@code CritterCage(int i, int j)} (:16-21).
     *
     * @param j          the original's second argument, {@code cage_id}
     * @param properties registry properties; stack size 16 is set here (:19)
     */
    public CritterCage(final int j, final Item.Properties properties) {
        // PORT: CreativeTabs.tabMisc (:20) is gone; the registration files the cage under
        // OriginalTab.MISC (the spawn-egg tab), like the eggs.
        super(properties.stacksTo(16));
        this.cage_id = 0;
        this.cage_id = j;
    }

    /**
     * {@code onItemRightClick} (:23-35): only the empty cage does anything. PORT: 1.7.10 had no off
     * hand; the throw works from either hand, like every 1.21.1 throwable (ItemShoes does the same).
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level par2World, final Player par3EntityPlayer, final InteractionHand hand) {
        final ItemStack par1ItemStack = par3EntityPlayer.getItemInHand(hand);
        final CritterCage cc = (CritterCage) ModItems.CAGE_EMPTY.get();
        if (this.cage_id == cc.cage_id) {
            if (!par3EntityPlayer.getAbilities().instabuild) {
                par1ItemStack.shrink(1);
            }
            // "random.bow" is entity.arrow.shoot; itemRand is the level random here.
            par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(), SoundEvents.ARROW_SHOOT,
                    SoundSource.NEUTRAL, 0.5f, 0.4f / (par2World.getRandom().nextFloat() * 0.4f + 0.8f));
            if (!par2World.isClientSide) {
                par2World.addFreshEntity(new EntityCage(par2World, par3EntityPlayer, this.cage_id));
            }
            return InteractionResultHolder.sidedSuccess(par1ItemStack, par2World.isClientSide());
        }
        // PORT: 1.7.10 returned the unchanged stack without a result code; PASS is that in 1.21.1.
        return InteractionResultHolder.pass(par1ItemStack);
    }

    /**
     * {@code onItemUse} (:37-526). Returning {@link InteractionResult#PASS} for the empty cage is the
     * original's {@code return false}: 1.21.1 then calls {@link #use}, as 1.7.10 then called
     * {@code onItemRightClick}.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final CritterCage cc = (CritterCage) ModItems.CAGE_EMPTY.get();
        if (this.cage_id == cc.cage_id) {
            return InteractionResult.PASS; // :39-41
        }
        final Level par3World = context.getLevel();
        final Player par2EntityPlayer = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        final BlockPos pos = context.getClickedPos();
        final int par4 = pos.getX();
        final int par5 = pos.getY();
        final int par6 = pos.getZ();
        // :42-46 - on both sides; the server's copies went nowhere in 1.7.10 and do here as well.
        for (int var3 = 0; var3 < 6; ++var3) {
            par3World.addParticle(ParticleTypes.SMOKE, (double) (par4 + 0.5f), (double) (par5 + 1.25f), (double) (par6 + 0.5f), 0.0, 0.0, 0.0);
            // "explode" is poof; "reddust" with zero arguments is plain redstone red.
            par3World.addParticle(ParticleTypes.POOF, (double) (par4 + 0.5f), (double) (par5 + 1.25f), (double) (par6 + 0.5f), 0.0, 0.0, 0.0);
            par3World.addParticle(DustParticleOptions.REDSTONE, (double) (par4 + 0.5f), (double) (par5 + 1.25f), (double) (par6 + 0.5f), 0.0, 0.0, 0.0);
        }
        // :47 playSoundAtEntity(player, "random.explode", 1.0, 1.5): broadcast by the server; a null
        // player makes the client copy silent, so it is heard once.
        // PORT: UseOnContext.getPlayer() is nullable (deployers); without a player the sound plays at
        // the clicked block.
        if (par2EntityPlayer != null) {
            par3World.playSound(null, par2EntityPlayer.getX(), par2EntityPlayer.getY(), par2EntityPlayer.getZ(),
                    SoundEvents.GENERIC_EXPLODE, par2EntityPlayer.getSoundSource(), 1.0f, 1.5f);
        } else {
            par3World.playSound(null, par4 + 0.5, par5 + 0.5, par6 + 0.5, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.5f);
        }
        if (par3World.isClientSide) {
            return InteractionResult.SUCCESS; // :48-50
        }
        int entityID = 0;
        int skelly_type = 0;
        Supplier<? extends EntityType<?>> name = null;
        // :54-506. Vanilla creatures keep their 1.7.10 numeric id (resolved in vanillaType), OreSpawn
        // creatures replace the global entity name by their registry holder (manifest entities[].name).
        switch (this.cage_id) {
            case 161: {
                entityID = 52;
                break;
            }
            case 162: {
                entityID = 65;
                break;
            }
            case 163: {
                entityID = 92;
                break;
            }
            case 164: {
                entityID = 90;
                break;
            }
            case 165: {
                entityID = 94;
                break;
            }
            case 166: {
                entityID = 93;
                break;
            }
            case 167: {
                entityID = 50;
                break;
            }
            case 188: {
                skelly_type = 1;
            }
            // fall through, as in the original (:83-89)
            case 168: {
                entityID = 51;
                break;
            }
            case 169: {
                entityID = 54;
                break;
            }
            case 170: {
                entityID = 55;
                break;
            }
            case 171: {
                entityID = 56;
                break;
            }
            case 172: {
                entityID = 57;
                break;
            }
            case 173: {
                entityID = 58;
                break;
            }
            case 174: {
                entityID = 59;
                break;
            }
            case 175: {
                entityID = 60;
                break;
            }
            case 176: {
                entityID = 62;
                break;
            }
            case 177: {
                entityID = 66;
                break;
            }
            case 178: {
                entityID = 91;
                break;
            }
            case 179: {
                entityID = 95;
                break;
            }
            case 180: {
                entityID = 96;
                break;
            }
            case 181: {
                entityID = 98;
                break;
            }
            case 182: {
                entityID = 61;
                break;
            }
            case 184: {
                entityID = 63;
                break;
            }
            case 185: {
                entityID = 97;
                break;
            }
            case 186: {
                entityID = 99;
                break;
            }
            case 187: {
                entityID = 64;
                break;
            }
            case 253: {
                entityID = 100;
                break;
            }
            case 217: {
                entityID = 120;
                break;
            }
            case 183: {
                name = ModEntities.GIRLFRIEND; // "Girlfriend"
                break;
            }
            case 215: {
                name = ModEntities.BOYFRIEND; // "Boyfriend"
                break;
            }
            case 189: {
                name = ModEntities.APPLE_COW; // "Apple Cow"
                break;
            }
            case 190: {
                name = ModEntities.GOLDEN_APPLE_COW; // "Golden Apple Cow"
                break;
            }
            case 191: {
                name = ModEntities.ENCHANTED_GOLDEN_APPLE_COW; // "Enchanted Golden Apple Cow"
                break;
            }
            case 208: {
                name = ModEntities.MOTHRA; // "Mothra"
                break;
            }
            case 209: {
                name = ModEntities.ALOSAURUS; // "Alosaurus"
                break;
            }
            case 210: {
                name = ModEntities.CRYOLOPHOSAURUS; // "Cryolophosaurus"
                break;
            }
            case 211: {
                name = ModEntities.CAMARASAURUS; // "Camarasaurus"
                break;
            }
            case 212: {
                name = ModEntities.VELOCITY_RAPTOR; // "Velocity Raptor"
                break;
            }
            case 213: {
                name = ModEntities.HYDROLISC; // "Hydrolisc"
                break;
            }
            case 214: {
                name = ModEntities.BASILISK; // "Basilisk"
                break;
            }
            case 220: {
                name = ModEntities.DRAGONFLY; // "Dragonfly"
                break;
            }
            case 222: {
                name = ModEntities.EMPEROR_SCORPION; // "Emperor Scorpion"
                break;
            }
            case 224: {
                name = ModEntities.SCORPION; // "Scorpion"
                break;
            }
            case 226: {
                name = ModEntities.CAVE_FISHER; // "CaveFisher"
                break;
            }
            case 228: {
                name = ModEntities.BABY_DRAGON; // "Baby Dragon"
                break;
            }
            case 230: {
                name = ModEntities.BARYONYX; // "Baryonyx"
                break;
            }
            case 232: {
                name = ModEntities.WTF; // "WTF?"
                break;
            }
            case 234: {
                name = ModEntities.BIRD; // "Bird"
                break;
            }
            case 236: {
                name = ModEntities.KYUUBI; // "Kyuubi"
                break;
            }
            case 238: {
                name = ModEntities.ALIEN; // "Alien"
                break;
            }
            case 240: {
                name = ModEntities.ATTACK_SQUID; // "Attack Squid"
                break;
            }
            case 242: {
                name = ModEntities.WATER_DRAGON; // "Water Dragon"
                break;
            }
            case 244: {
                name = ModEntities.THE_KRAKEN; // "The Kraken"
                break;
            }
            case 246: {
                name = ModEntities.LIZARD; // "Lizard"
                break;
            }
            case 248: {
                name = ModEntities.CEPHADROME; // "Cephadrome"
                break;
            }
            case 250: {
                name = ModEntities.DRAGON; // "Dragon"
                break;
            }
            case 252: {
                name = ModEntities.BEE; // "Bee"
                break;
            }
            case 255: {
                name = ModEntities.FIREFLY; // "Firefly"
                break;
            }
            case 256: {
                name = ModEntities.CHIPMUNK; // "Chipmunk"
                break;
            }
            case 257: {
                name = ModEntities.GAZELLE; // "Gazelle"
                break;
            }
            case 258: {
                name = ModEntities.OSTRICH; // "Ostrich"
                break;
            }
            case 259: {
                name = ModEntities.JUMPY_BUG; // "Jumpy Bug"
                break;
            }
            case 260: {
                name = ModEntities.SPIT_BUG; // "Spit Bug"
                break;
            }
            case 261: {
                name = ModEntities.STINK_BUG; // "Stink Bug"
                break;
            }
            case 268: {
                name = ModEntities.CREEPING_HORROR; // "Creeping Horror"
                break;
            }
            case 269: {
                name = ModEntities.TERRIBLE_TERROR; // "Terrible Terror"
                break;
            }
            case 270: {
                name = ModEntities.CLIFF_RACER; // "Cliff Racer"
                break;
            }
            case 271: {
                name = ModEntities.TRIFFID; // "Triffid"
                break;
            }
            case 272: {
                name = ModEntities.NIGHTMARE; // "Nightmare"
                break;
            }
            case 273: {
                name = ModEntities.LURKING_TERROR; // "Lurking Terror"
                break;
            }
            case 281: {
                name = ModEntities.SMALL_WORM; // "Small Worm"
                break;
            }
            case 283: {
                name = ModEntities.LARGE_WORM; // "Large Worm"
                break;
            }
            case 282: {
                name = ModEntities.MEDIUM_WORM; // "Medium Worm"
                break;
            }
            case 284: {
                name = ModEntities.CASSOWARY; // "Cassowary"
                break;
            }
            case 285: {
                name = ModEntities.CLOUD_SHARK; // "Cloud Shark"
                break;
            }
            case 286: {
                name = ModEntities.GOLD_FISH; // "Gold Fish"
                break;
            }
            case 287: {
                name = ModEntities.LEAF_MONSTER; // "Leaf Monster"
                break;
            }
            case 296: {
                name = ModEntities.ENDER_KNIGHT; // "Ender Knight"
                break;
            }
            case 297: {
                name = ModEntities.ENDER_REAPER; // "Ender Reaper"
                break;
            }
            case 300: {
                name = ModEntities.BEAVER; // "Beaver"
                break;
            }
            case 323: {
                name = ModEntities.CRYSTAL_URCHIN; // "Crystal Urchin"
                break;
            }
            case 319: {
                name = ModEntities.FLOUNDER; // "Flounder"
                break;
            }
            case 322: {
                name = ModEntities.SKATE; // "Skate"
                break;
            }
            case 313: {
                name = ModEntities.ROTATOR; // "Rotator"
                break;
            }
            case 315: {
                name = ModEntities.PEACOCK; // "Peacock"
                break;
            }
            case 316: {
                name = ModEntities.FAIRY; // "Fairy"
                break;
            }
            case 317: {
                name = ModEntities.DUNGEON_BEAST; // "Dungeon Beast"
                break;
            }
            case 314: {
                name = ModEntities.VORTEX; // "Vortex"
                break;
            }
            case 318: {
                name = ModEntities.RAT; // "Rat"
                break;
            }
            case 320: {
                name = ModEntities.WHALE; // "Whale"
                break;
            }
            case 321: {
                name = ModEntities.IRUKANDJI; // "Irukandji"
                break;
            }
            case 345: {
                name = ModEntities.T_REX; // "T. Rex"
                break;
            }
            case 346: {
                name = ModEntities.HERCULES_BEETLE; // "Hercules Beetle"
                break;
            }
            case 347: {
                name = ModEntities.MANTIS; // "Mantis"
                break;
            }
            case 348: {
                name = ModEntities.STINKY; // "Stinky"
                break;
            }
            case 150: {
                name = ModEntities.EASTER_BUNNY; // "Easter Bunny"
                break;
            }
            case 151: {
                name = ModEntities.CATER_KILLER; // "CaterKiller"
                break;
            }
            case 152: {
                name = ModEntities.MOLENOID; // "Molenoid"
                break;
            }
            case 153: {
                name = ModEntities.SEA_MONSTER; // "Sea Monster"
                break;
            }
            case 154: {
                name = ModEntities.SEA_VIPER; // "Sea Viper"
                break;
            }
            case 357: {
                name = ModEntities.LEONOPTERYX; // "Leonopteryx"
                break;
            }
            case 359: {
                name = ModEntities.HAMMERHEAD; // "Hammerhead"
                break;
            }
            case 361: {
                name = ModEntities.RUBBER_DUCKY; // "Rubber Ducky"
                break;
            }
            case 216: {
                name = ModEntities.CRYSTAL_APPLE_COW; // "Crystal Apple Cow"
                break;
            }
            case 218: {
                name = ModEntities.CRIMINAL; // "Criminal"
                break;
            }
            case 373: {
                name = ModEntities.BRUTALFLY; // "Brutalfly"
                break;
            }
            case 374: {
                name = ModEntities.NASTYSAURUS; // "Nastysaurus"
                break;
            }
            case 375: {
                name = ModEntities.POINTYSAURUS; // "Pointysaurus"
                break;
            }
            case 376: {
                name = ModEntities.CRICKET; // "Cricket"
                break;
            }
            case 377: {
                name = ModEntities.FROG; // "Frog"
                break;
            }
            case 382: {
                name = ModEntities.SPIDER_DRIVER; // "Spider Driver"
                break;
            }
            case 384: {
                name = ModEntities.CRAB; // "Crab"
                break;
            }
        }
        if (entityID != 0 || name != null) {
            Entity ent = null;
            ent = spawnCreature(par3World, entityID, skelly_type, name, par4 + 0.5, par5 + 1.1, par6 + 0.5); // :509
            if (ent != null) {
                ent.spawnAtLocation(new ItemStack(ModItems.CAGE_EMPTY.get(), 1), 0.0f); // :511
                // :512-515 setSkeletonType(skelly_type) happened here; the type was already chosen in
                // spawnCreature (see vanillaType).
                if (ent instanceof Mob && par1ItemStack.has(DataComponents.CUSTOM_NAME)) { // :516-518
                    ent.setCustomName(par1ItemStack.getHoverName());
                }
            }
            // PORT: getPlayer() is nullable in 1.21.1; no player counts as "not creative".
            if (par2EntityPlayer == null || !par2EntityPlayer.getAbilities().instabuild) { // :520-522
                par1ItemStack.shrink(1);
            }
            // PORT: 1.7.10 returned true; CONSUME is the server-side "handled" result.
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS; // :525
    }

    /**
     * {@code spawnCreature} (:528-546): creates the creature, places it with a random yaw and pitch 0,
     * runs {@code onSpawnWithEgg} for the horse (100) and the villager (120) only, adds it to the level
     * and plays its ambient sound.
     *
     * @param par1        the original's vanilla entity id, 0 for an OreSpawn creature
     * @param skelly_type the fall-through flag of case 188 (1 = wither skeleton)
     * @param name        the OreSpawn creature, or null for a vanilla id
     * @return the spawned entity, or null if none could be created
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final int par1, final int skelly_type,
            @Nullable final Supplier<? extends EntityType<?>> name, final double par2, final double par4, final double par6) {
        Entity var8 = null;
        if (name == null) {
            final EntityType<?> type = vanillaType(par0World, par1, skelly_type);
            var8 = type == null ? null : type.create(par0World); // createEntityByID
        } else {
            var8 = name.get().create(par0World); // createEntityByName
        }
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.getRandom().nextFloat() * 360.0f, 0.0f); // :537
            if (var8 instanceof Slime slime) {
                // PORT: the 1.7.10 EntitySlime constructor rolled its size, 1 << rand.nextInt(3)
                // (javap ym.<init>); 1.21.1 rolls it in finalizeSpawn, which this path never calls. The
                // roll moves here so a caged slime or magma cube keeps its random size.
                slime.setSize(1 << slime.getRandom().nextInt(3), true);
            }
            if ((par1 == 100 || par1 == 120) && var8 instanceof Mob sk && par0World instanceof ServerLevel serverLevel) { // :538-541
                // onSpawnWithEgg(null) -> finalizeSpawn with SPAWN_EGG, through NeoForge's event hook.
                // Horse: random coat and markings, random health/speed/jump (Horse/AbstractHorse.finalizeSpawn).
                // Villager: PORT - 1.7.10 rolled one of five professions (javap yv.a(sy): rand.nextInt(5));
                // 1.21.1 binds professions to job sites and ResetProfession strips an unemployed level-1
                // profession on the next brain tick, so no profession is set. finalizeSpawn picks the biome
                // villager type instead (R18 case 3).
                EventHooks.finalizeMobSpawn(sk, serverLevel, serverLevel.getCurrentDifficultyAt(sk.blockPosition()), MobSpawnType.SPAWN_EGG, null);
                if (par1 == 100 && sk instanceof AbstractHorse horse && sk.getRandom().nextInt(5) == 0) {
                    // PORT: EntityHorse.onSpawnWithEgg made one horse in five a foal (javap wi.a(sy):
                    // rand.nextInt(5) == 0 -> setGrowingAge(-24000)). 1.21.1's AgeableMob.finalizeSpawn
                    // never makes the first mob of a group a baby, so the roll is repeated here.
                    horse.setAge(-24000);
                }
            }
            par0World.addFreshEntity(var8); // :542
            // PORT: the original cast to EntityLiving unchecked (:543); every cage creature is a Mob.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /**
     * {@code EntityList.createEntityByID} for the 28 vanilla ids of the switch, with the two 1.7.10
     * subtypes on their own 1.21.1 types (R18):
     * <ul>
     *   <li>51 with {@code skelly_type} 1 is {@link EntityType#WITHER_SKELETON} (1.7.10 created a
     *       skeleton and called {@code setSkeletonType(1)} after spawning it, :512-515);</li>
     *   <li>100 is a horse, or a donkey one time in ten: {@code EntityHorse.onSpawnWithEgg} set horse
     *       type 1 (donkey) on {@code rand.nextInt(10) == 0}, otherwise type 0 with a random coat
     *       (javap {@code wi.a(sy)}). Mule, zombie and skeleton horse never came out of it.</li>
     * </ul>
     */
    @Nullable
    static EntityType<?> vanillaType(final Level level, final int entityID, final int skelly_type) {
        switch (entityID) {
            case 50: return EntityType.CREEPER;
            case 51: return skelly_type != 0 ? EntityType.WITHER_SKELETON : EntityType.SKELETON;
            case 52: return EntityType.SPIDER;
            case 54: return EntityType.ZOMBIE;
            case 55: return EntityType.SLIME;
            case 56: return EntityType.GHAST;
            case 57: return EntityType.ZOMBIFIED_PIGLIN; // PigZombie
            case 58: return EntityType.ENDERMAN;
            case 59: return EntityType.CAVE_SPIDER;
            case 60: return EntityType.SILVERFISH;
            case 61: return EntityType.BLAZE;
            case 62: return EntityType.MAGMA_CUBE; // LavaSlime
            case 63: return EntityType.ENDER_DRAGON;
            case 64: return EntityType.WITHER;
            case 65: return EntityType.BAT;
            case 66: return EntityType.WITCH;
            case 90: return EntityType.PIG;
            case 91: return EntityType.SHEEP;
            case 92: return EntityType.COW;
            case 93: return EntityType.CHICKEN;
            case 94: return EntityType.SQUID;
            case 95: return EntityType.WOLF;
            case 96: return EntityType.MOOSHROOM; // MushroomCow
            case 97: return EntityType.SNOW_GOLEM; // SnowMan
            case 98: return EntityType.OCELOT; // Ozelot
            case 99: return EntityType.IRON_GOLEM; // VillagerGolem
            // PORT: the donkey roll moves from onSpawnWithEgg (entity random, after creation) to here
            // (level random, before creation), because the horse type is the EntityType in 1.21.1 (R18).
            case 100: return level.getRandom().nextInt(10) == 0 ? EntityType.DONKEY : EntityType.HORSE; // EntityHorse
            case 120: return EntityType.VILLAGER;
            default: return null;
        }
    }
}
