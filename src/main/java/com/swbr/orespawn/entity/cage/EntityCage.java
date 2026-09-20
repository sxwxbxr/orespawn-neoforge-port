package com.swbr.orespawn.entity.cage;

import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.arthropod.CaveFisher;
import com.swbr.orespawn.entity.arthropod.EmperorScorpion;
import com.swbr.orespawn.entity.arthropod.HerculesBeetle;
import com.swbr.orespawn.entity.arthropod.Scorpion;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Frog;
import com.swbr.orespawn.entity.aquatic.GoldFish;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import com.swbr.orespawn.entity.cannonfodder.Gazelle;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.cow.EnchantedCow;
import com.swbr.orespawn.entity.cow.GoldCow;
import com.swbr.orespawn.entity.cow.RedCow;
import com.swbr.orespawn.entity.critter.CliffRacer;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.critter.Cricket;
import com.swbr.orespawn.entity.critter.Dragonfly;
import com.swbr.orespawn.entity.crystal.DungeonBeast;
import com.swbr.orespawn.entity.crystal.Rotator;
import com.swbr.orespawn.entity.crystal.Vortex;
import com.swbr.orespawn.entity.dino.Alosaurus;
import com.swbr.orespawn.entity.dino.Basilisk;
import com.swbr.orespawn.entity.dino.Cryolophosaurus;
import com.swbr.orespawn.entity.dino.Nastysaurus;
import com.swbr.orespawn.entity.dino.Pointysaurus;
import com.swbr.orespawn.entity.dino.TRex;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.easter.EasterBunny;
import com.swbr.orespawn.entity.ender.EnderKnight;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.fairy.Fairy;
import com.swbr.orespawn.entity.herbivore.Baryonyx;
import com.swbr.orespawn.entity.herbivore.Beaver;
import com.swbr.orespawn.entity.herbivore.Camarasaurus;
import com.swbr.orespawn.entity.herbivore.Cassowary;
import com.swbr.orespawn.entity.herbivore.Hydrolisc;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.entity.herbivore.StinkBug;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.monster.Alien;
import com.swbr.orespawn.entity.monster.BandP;
import com.swbr.orespawn.entity.monster.Hammerhead;
import com.swbr.orespawn.entity.monster.Kyuubi;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.monster.Molenoid;
import com.swbr.orespawn.entity.moth.Brutalfly;
import com.swbr.orespawn.entity.moth.CaterKiller;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.pet.RubberDucky;
import com.swbr.orespawn.entity.pet.Stinky;
import com.swbr.orespawn.entity.projectile.LegacyProjectiles;
import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.entity.rider.Ostrich;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.entity.sea.CloudShark;
import com.swbr.orespawn.entity.sea.Irukandji;
import com.swbr.orespawn.entity.sea.SeaMonster;
import com.swbr.orespawn.entity.sea.SeaViper;
import com.swbr.orespawn.entity.sea.Skate;
import com.swbr.orespawn.entity.sea.Urchin;
import com.swbr.orespawn.entity.spiderrobot.SpiderDriver;
import com.swbr.orespawn.entity.terror.Crab;
import com.swbr.orespawn.entity.terror.CreepingHorror;
import com.swbr.orespawn.entity.terror.LurkingTerror;
import com.swbr.orespawn.entity.terror.Mantis;
import com.swbr.orespawn.entity.terror.Rat;
import com.swbr.orespawn.entity.terror.TerribleTerror;
import com.swbr.orespawn.entity.triffid.Triffid;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.entity.worm.WormLarge;
import com.swbr.orespawn.entity.worm.WormMedium;
import com.swbr.orespawn.entity.worm.WormSmall;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.EntityCage} (verhalten/entity-07.md, "EntityCage"; entity
 * {@code entity_cage}): the thrown empty Critter Cage. Flight is the 1.7.10 {@code EntityThrowable} of
 * {@link LegacyThrowable} (velocity 1.5, gravity 0.03, thrower ignored for five ticks, entity hits on the
 * server only).
 *
 * <p>{@link #onImpact} is a chain of independent {@code instanceof} tests in the original order
 * (EntityCage.java:75-839). A creature that matches no line consumes the cage without a drop - also
 * the original. Hierarchies the order depends on, checked against the port: {@code SpiderDriver}
 * extends {@code Spider} (split by {@code else if}), {@code GoldCow}/{@code EnchantedCow}/
 * {@code CrystalCow} extend {@code RedCow} extends {@code Cow} (split by {@code return}),
 * {@code RubyBird} extends {@code Cockateil} (caught as four birds, like 1.7.10), {@code Mothra} extends
 * {@code EntityButterfly} (butterflies themselves match nothing).
 *
 * <p>Vanilla classes on their 1.21.1 types (R18): {@code EntityHorse} is horse, donkey, mule, zombie and
 * skeleton horse - the five 1.7.10 horse types, not llamas or camels; {@code EntitySkeleton} is
 * {@code Skeleton} and {@code WitherSkeleton} (type 0 and 1), not stray or bogged; {@code EntityOcelot}
 * includes {@code Cat}, which was a tamed ocelot in 1.7.10. Subclasses 1.21.1 added under a caught class
 * stay caught as that class, because the test is {@code instanceof} (glow squid as squid; husk, drowned
 * and zombie villager as zombie - the 1.7.10 zombie villager was an {@code EntityZombie} too).
 *
 * <p>Spinner index (catalogue 6.5): {@code my_index} is set only by the constructors and read only by
 * {@code RenderCage}; nothing in {@code onImpact} writes it, so a catch never changes the tile before
 * the drop. It is neither synchronised nor saved. The client builds its copy through the registry
 * factory and draws the default 160 - which is also the only index a player can throw (the empty cage).
 */
public class EntityCage extends LegacyThrowable {

    private float my_rotation;
    public int my_index;
    @Nullable
    private Level throwerWorld;
    /** Only set by the throwing constructor and not saved: a reloaded cage plays no catch sound (:64-66). */
    @Nullable
    private Player thrower;

    /** {@code EntityCage(World)} (:19-26); also the entity type factory. */
    public EntityCage(final EntityType<? extends EntityCage> type, final Level par1World) {
        super(type, par1World);
        this.my_rotation = 0.0f;
        this.my_index = 160;
        this.throwerWorld = null;
        this.thrower = null;
        this.throwerWorld = par1World;
    }

    /** {@code EntityCage(World, int)} (:28-36). */
    public EntityCage(final Level par1World, final int i) {
        super(ModEntities.ENTITY_CAGE.get(), par1World);
        this.my_rotation = 0.0f;
        this.my_index = 160;
        this.throwerWorld = null;
        this.thrower = null;
        this.throwerWorld = par1World;
        this.my_index = i;
    }

    /** {@code EntityCage(World, EntityPlayer, int)} (:38-50): the throw of {@code CritterCage}. */
    public EntityCage(final Level par1World, final Player par2EntityLiving, final int i) {
        super(ModEntities.ENTITY_CAGE.get(), par1World, par2EntityLiving);
        this.my_rotation = 0.0f;
        this.my_index = 160;
        this.throwerWorld = null;
        this.thrower = null;
        this.throwerWorld = par1World;
        this.thrower = par2EntityLiving;
        this.my_index = i;
        if (this.thrower.level() != null) {
            this.throwerWorld = this.thrower.level();
        }
    }

    /** {@code getCageIndex} (:52-54): the {@code spinners.png} tile. */
    public int getCageIndex() {
        return this.my_index;
    }

    /** {@code dropItem(Item, int)}: {@code entityDropItem(new ItemStack(item, n), 0.0f)} at the cage. */
    private void dropItem(final Item item, final int count) {
        this.spawnAtLocation(new ItemStack(item, count), 0.0f);
    }

    /**
     * Escape roll shared by most lines: {@code rand.nextInt(bound) < limit} returns the empty cage and
     * ends the impact (the {@code setDead} there was server-guarded; entity hits only reach the server).
     */
    private boolean escapes(final int bound, final int limit) {
        if (this.random.nextInt(bound) < limit) {
            if (!this.level().isClientSide) {
                this.dropItem(ModItems.CAGE_EMPTY.get(), 1);
                this.discard();
            }
            return true;
        }
        return false;
    }

    /** {@code onImpact} (:56-847). {@code setDead} on the creature is {@code discard()}: no death, no drops. */
    @Override
    protected void onImpact(final HitResult par1MovingObjectPosition) {
        final Entity entityHit = LegacyProjectiles.entityHit(par1MovingObjectPosition);
        if (entityHit != null && this.random.nextInt(10) >= 2) {
            if (this.throwerWorld != null) {
                // :59-63 - on the thrower's (server) world; 1.7.10's server world drew no particles, and
                // Level.addParticle on a ServerLevel draws none either. Kept for the line order.
                for (int var3 = 0; var3 < 4; ++var3) {
                    this.throwerWorld.addParticle(ParticleTypes.SMOKE, entityHit.getX(), entityHit.getY() + 0.25, entityHit.getZ(), 0.0, 0.0, 0.0);
                    this.throwerWorld.addParticle(ParticleTypes.POOF, entityHit.getX(), entityHit.getY() + 0.25, entityHit.getZ(), 0.0, 0.0, 0.0);
                    this.throwerWorld.addParticle(DustParticleOptions.REDSTONE, entityHit.getX(), entityHit.getY() + 0.25, entityHit.getZ(), 0.0, 0.0, 0.0);
                }
                if (this.thrower != null) {
                    // :65 playSoundAtEntity(thrower, "random.explode", 1.0, 1.5): heard by everyone near the thrower.
                    this.throwerWorld.playSound(null, this.thrower.getX(), this.thrower.getY(), this.thrower.getZ(),
                            SoundEvents.GENERIC_EXPLODE, this.thrower.getSoundSource(), 1.0f, 1.5f);
                }
            }
            if (entityHit instanceof Player) {
                if (!this.level().isClientSide) {
                    this.dropItem(ModItems.CAGE_EMPTY.get(), 1);
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof SpiderDriver) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SPIDERDRIVER.get(), 1);
            } else if (entityHit instanceof Spider) {
                if (entityHit instanceof CaveSpider) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_CAVESPIDER.get(), 1);
                } else {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_SPIDER.get(), 1);
                }
            }
            if (entityHit instanceof Crab) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CRAB.get(), 1);
            }
            if (entityHit instanceof Bat) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BAT.get(), 2);
            }
            if (entityHit instanceof Pig) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_PIG.get(), 1);
            }
            if (entityHit instanceof Squid) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SQUID.get(), 1);
            }
            if (entityHit instanceof Chicken) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CHICKEN.get(), 1);
            }
            if (entityHit instanceof Creeper) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CREEPER.get(), 1);
            }
            // PORT: EntityHorse (:113) covered all five 1.7.10 horse types; they are separate classes now (R18).
            if (entityHit instanceof Horse || entityHit instanceof Donkey || entityHit instanceof Mule
                    || entityHit instanceof ZombieHorse || entityHit instanceof SkeletonHorse) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_HORSE.get(), 1);
            }
            // PORT: EntitySkeleton (:117) with getSkeletonType() != 0 is the separate WitherSkeleton (R18).
            if (entityHit instanceof Skeleton || entityHit instanceof WitherSkeleton) {
                if (entityHit instanceof WitherSkeleton) {
                    this.dropItem(ModItems.CAGE_WITHERSKELETON.get(), 1);
                } else {
                    this.dropItem(ModItems.CAGE_SKELETON.get(), 1);
                }
                entityHit.discard();
            }
            if (entityHit instanceof Zombie) {
                if (entityHit instanceof ZombifiedPiglin) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_ZOMBIEPIGMAN.get(), 1);
                } else {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_ZOMBIE.get(), 1);
                }
            }
            if (entityHit instanceof Slime) {
                if (entityHit instanceof MagmaCube) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_MAGMACUBE.get(), 1);
                } else {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_SLIME.get(), 1);
                }
            }
            if (entityHit instanceof Ghast) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_GHAST.get(), 1);
            }
            if (entityHit instanceof EnderMan) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ENDERMAN.get(), 1);
            }
            if (entityHit instanceof Silverfish) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SILVERFISH.get(), 2);
            }
            if (entityHit instanceof Witch) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_WITCH.get(), 1);
            }
            if (entityHit instanceof Sheep) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SHEEP.get(), 1);
            }
            if (entityHit instanceof Wolf) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_WOLF.get(), 1);
            }
            // PORT: a tamed 1.7.10 ocelot was the cat; Cat is its own class now (R18).
            if (entityHit instanceof Ocelot || entityHit instanceof Cat) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_OCELOT.get(), 1);
            }
            if (entityHit instanceof Blaze) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BLAZE.get(), 1);
            }
            if (entityHit instanceof Girlfriend) {
                final Girlfriend gf = (Girlfriend) entityHit;
                if (!gf.isTame()) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_GIRLFRIEND.get(), 1);
                }
            }
            if (entityHit instanceof Boyfriend) {
                final Boyfriend gf2 = (Boyfriend) entityHit;
                if (!gf2.isTame()) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_BOYFRIEND.get(), 1);
                }
            }
            if (entityHit instanceof EnderDragon) {
                if (this.escapes(10, 5)) {
                    return;
                }
                final EnderDragon dr = (EnderDragon) entityHit;
                dr.discard();
                this.dropItem(ModItems.CAGE_ENDERDRAGON.get(), 1);
            }
            if (entityHit instanceof EnderDragonPart) {
                if (this.escapes(10, 5)) {
                    return;
                }
                final EnderDragonPart dp = (EnderDragonPart) entityHit;
                final EnderDragon dr2 = dp.parentMob;
                dr2.discard();
                this.dropItem(ModItems.CAGE_ENDERDRAGON.get(), 1);
            }
            if (entityHit instanceof SnowGolem) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SNOWGOLEM.get(), 1);
            }
            if (entityHit instanceof IronGolem) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_IRONGOLEM.get(), 1);
            }
            if (entityHit instanceof WitherBoss) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_WITHERBOSS.get(), 1);
            }
            if (entityHit instanceof CrystalCow) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CRYSTALCOW.get(), 1);
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof EnchantedCow) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ENCHANTEDCOW.get(), 1);
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof GoldCow) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_GOLDCOW.get(), 1);
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof RedCow) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_REDCOW.get(), 1);
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof Cow) {
                if (entityHit instanceof MushroomCow) {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_MOOSHROOM.get(), 1);
                } else {
                    entityHit.discard();
                    this.dropItem(ModItems.CAGE_COW.get(), 1);
                }
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof Villager) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_VILLAGER.get(), 1);
                if (!this.level().isClientSide) {
                    this.discard();
                }
                return;
            }
            if (entityHit instanceof Mothra) {
                if (this.escapes(10, 4)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_MOTHRA.get(), 1);
            }
            if (entityHit instanceof Alosaurus) {
                if (this.escapes(10, 4)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ALOSAURUS.get(), 1);
            }
            if (entityHit instanceof Cryolophosaurus) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CRYOLOPHOSAURUS.get(), 1);
            }
            if (entityHit instanceof Camarasaurus) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CAMARASAURUS.get(), 1);
            }
            if (entityHit instanceof VelocityRaptor) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_VELOCITYRAPTOR.get(), 1);
            }
            if (entityHit instanceof Hydrolisc) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_HYDROLISC.get(), 1);
            }
            if (entityHit instanceof Basilisk) {
                if (this.escapes(10, 6)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BASILISC.get(), 1);
            }
            if (entityHit instanceof Dragonfly) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_DRAGONFLY.get(), 2);
            }
            if (entityHit instanceof EmperorScorpion) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_EMPERORSCORPION.get(), 1);
            }
            if (entityHit instanceof Cephadrome) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CEPHADROME.get(), 1);
            }
            if (entityHit instanceof Dragon) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_DRAGON.get(), 1);
            }
            if (entityHit instanceof Scorpion) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SCORPION.get(), 1);
            }
            if (entityHit instanceof CaveFisher) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CAVEFISHER.get(), 1);
            }
            if (entityHit instanceof Spyro) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SPYRO.get(), 1);
            }
            if (entityHit instanceof Baryonyx) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BARYONYX.get(), 1);
            }
            if (entityHit instanceof GammaMetroid) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_GAMMAMETROID.get(), 1);
            }
            if (entityHit instanceof Cockateil) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_COCKATEIL.get(), 4);
            }
            if (entityHit instanceof AttackSquid) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ATTACKSQUID.get(), 6);
            }
            if (entityHit instanceof Kyuubi) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_KYUUBI.get(), 1);
            }
            if (entityHit instanceof WaterDragon) {
                if (this.escapes(10, 6)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_WATERDRAGON.get(), 1);
            }
            if (entityHit instanceof Kraken) {
                if (this.escapes(100, 95)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_KRAKEN.get(), 1);
            }
            if (entityHit instanceof Lizard) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_LIZARD.get(), 1);
            }
            if (entityHit instanceof Alien) {
                if (this.escapes(10, 5)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ALIEN.get(), 1);
            }
            if (entityHit instanceof Bee) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BEE.get(), 1);
            }
            if (entityHit instanceof Firefly) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_FIREFLY.get(), 1);
            }
            if (entityHit instanceof Chipmunk) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CHIPMUNK.get(), 1);
            }
            if (entityHit instanceof Gazelle) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_GAZELLE.get(), 1);
            }
            if (entityHit instanceof Ostrich) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_OSTRICH.get(), 1);
            }
            if (entityHit instanceof TrooperBug) {
                if (this.escapes(10, 6)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_TROOPER.get(), 1);
            }
            if (entityHit instanceof SpitBug) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SPIT.get(), 1);
            }
            if (entityHit instanceof StinkBug) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_STINK.get(), 1);
            }
            if (entityHit instanceof CreepingHorror) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CREEPINGHORROR.get(), 1);
            }
            if (entityHit instanceof TerribleTerror) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_TERRIBLETERROR.get(), 1);
            }
            if (entityHit instanceof CliffRacer) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CLIFFRACER.get(), 1);
            }
            if (entityHit instanceof Triffid) {
                if (this.escapes(10, 6)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_TRIFFID.get(), 1);
            }
            if (entityHit instanceof PitchBlack) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_NIGHTMARE.get(), 1);
            }
            if (entityHit instanceof LurkingTerror) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_LURKINGTERROR.get(), 1);
            }
            if (entityHit instanceof WormSmall) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SMALLWORM.get(), 1);
            }
            if (entityHit instanceof WormMedium) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_MEDIUMWORM.get(), 1);
            }
            if (entityHit instanceof Cassowary) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CASSOWARY.get(), 1);
            }
            if (entityHit instanceof CloudShark) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CLOUDSHARK.get(), 1);
            }
            if (entityHit instanceof GoldFish) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_GOLDFISH.get(), 1);
            }
            if (entityHit instanceof LeafMonster) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_LEAFMONSTER.get(), 1);
            }
            if (entityHit instanceof WormLarge) {
                if (this.escapes(10, 5)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_LARGEWORM.get(), 1);
            }
            if (entityHit instanceof EnderKnight) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ENDERKNIGHT.get(), 1);
            }
            if (entityHit instanceof EnderReaper) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ENDERREAPER.get(), 1);
            }
            if (entityHit instanceof Beaver) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BEAVER.get(), 1);
            }
            if (entityHit instanceof Urchin) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_URCHIN.get(), 1);
            }
            if (entityHit instanceof Flounder) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_FLOUNDER.get(), 1);
            }
            if (entityHit instanceof Skate) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SKATE.get(), 1);
            }
            if (entityHit instanceof Rotator) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_ROTATOR.get(), 1);
            }
            if (entityHit instanceof Peacock) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_PEACOCK.get(), 1);
            }
            if (entityHit instanceof Fairy) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_FAIRY.get(), 1);
            }
            if (entityHit instanceof DungeonBeast) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_DUNGEONBEAST.get(), 1);
            }
            if (entityHit instanceof Vortex) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_VORTEX.get(), 1);
            }
            if (entityHit instanceof Rat) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_RAT.get(), 1);
            }
            if (entityHit instanceof Whale) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_WHALE.get(), 1);
            }
            if (entityHit instanceof Irukandji) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_IRUKANDJI.get(), 1);
            }
            if (entityHit instanceof Stinky) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_STINKY.get(), 1);
            }
            if (entityHit instanceof Mantis) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_MANTIS.get(), 1);
            }
            if (entityHit instanceof TRex) {
                if (this.escapes(10, 4)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_TREX.get(), 1);
            }
            if (entityHit instanceof HerculesBeetle) {
                if (this.escapes(10, 5)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_HERCULES.get(), 1);
            }
            if (entityHit instanceof EasterBunny) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_EASTERBUNNY.get(), 1);
            }
            if (entityHit instanceof CaterKiller) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CATERKILLER.get(), 1);
            }
            if (entityHit instanceof Molenoid) {
                if (this.escapes(10, 5)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_MOLENOID.get(), 1);
            }
            if (entityHit instanceof SeaMonster) {
                if (this.escapes(10, 3)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SEAMONSTER.get(), 1);
            }
            if (entityHit instanceof SeaViper) {
                if (this.escapes(10, 4)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_SEAVIPER.get(), 1);
            }
            if (entityHit instanceof RubberDucky) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_RUBBERDUCKY.get(), 1);
            }
            if (entityHit instanceof Leon) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_LEON.get(), 1);
            }
            if (entityHit instanceof Hammerhead) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_HAMMERHEAD.get(), 1);
            }
            if (entityHit instanceof BandP) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CRIMINAL.get(), 1);
            }
            if (entityHit instanceof Cricket) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_CRICKET.get(), 1);
            }
            if (entityHit instanceof Frog) {
                entityHit.discard();
                this.dropItem(ModItems.CAGE_FROG.get(), 1);
            }
            if (entityHit instanceof Brutalfly) {
                if (this.escapes(10, 5)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_BRUTALFLY.get(), 1);
            }
            if (entityHit instanceof Nastysaurus) {
                if (this.escapes(10, 7)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_NASTYSAURUS.get(), 1);
            }
            if (entityHit instanceof Pointysaurus) {
                if (this.escapes(10, 2)) {
                    return;
                }
                entityHit.discard();
                this.dropItem(ModItems.CAGE_POINTYSAURUS.get(), 1);
            }
        } else if (!this.level().isClientSide) {
            this.dropItem(ModItems.CAGE_EMPTY.get(), 1); // :841-843
        }
        if (!this.level().isClientSide) {
            this.discard(); // :844-846
        }
    }

    /** {@code onUpdate} (:849-858): the cage spins 20 degrees a tick around its pitch. */
    @Override
    public void tick() {
        super.tick();
        this.my_rotation += 20.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
    }
}
