package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.registry.ModItems;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * The projectile lines of {@code OreSpawnMain.DoDispenserRegistrations} (OreSpawnMain.java:5416-5421),
 * in the original order. The rock lines (:5422-5424) belong to {@code item.rock}, the Irukandji arrow
 * (:5415) to the bows. Call from {@code FMLCommonSetupEvent.enqueueWork}: the dispenser registry is
 * a plain map.
 */
public final class ProjectileDispenserSetup {

    private ProjectileDispenserSetup() {
    }

    public static void register() {
        DispenserBlock.registerBehavior(ModItems.WATER_BALL.get(), new MyDispenserBehaviorWDCharge());          // :5416
        DispenserBlock.registerBehavior(ModItems.SUNSPOT_URCHIN.get(), new MyDispenserBehaviorSunspotUrchin()); // :5417
        DispenserBlock.registerBehavior(ModItems.ACID.get(), new MyDispenserBehaviorAcid());                    // :5418
        DispenserBlock.registerBehavior(ModItems.ICE_BALL.get(), new MyDispenserBehaviorIceball());             // :5419
        DispenserBlock.registerBehavior(ModItems.DEAD_IRUKANDJI.get(), new MyDispenserBehaviorDeadIrukandji()); // :5420
        DispenserBlock.registerBehavior(ModItems.LASER_BALL.get(), new MyDispenserBehaviorLaserball());         // :5421
    }
}
