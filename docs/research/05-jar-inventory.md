# OreSpawn 1.7.10-20.3 - jar inventory (ground truth)

Stand: 10.09.2026. Alles in diesem Dokument ist aus dem Jar gelesen (Bytecode, Konstantenpool, Assets), nicht aus Wikis. Jeder Wert nennt seinen Extraktionsweg in "Method notes". Code-Bezeichner und Tabellen bleiben englisch.

Raw dumps (alle in `jar/`): `classes.txt`, `calls_OreSpawnMain.txt`, `calls_ClientProxyOreSpawn.txt`, `config_dump.txt`, `lang_dump.txt`, `items_blocks.txt`, `entities_dump.txt`, `renderers_dump.txt`, `sounds_dump.txt`, `textures_dump.txt`, `inventory.json`, `models/*.json`, `anim/*.json`, `models_summary.txt`, `anim_summary.txt`, `model_validation_vs_src.txt`, `diff_20.2_vs_20.3.txt`. Extrahierte Assets: `jar/extracted/assets/orespawn/`. Modell-Details und Portierungsnotizen: `../06-models-design.md`.

## 1. Provenance

| | |
|---|---|
| File | `orespawn-1.7.10-20.3.jar` (local copy in `jar/`) |
| Source URL | `https://web.archive.org/web/20201105194809id_/http://www.orespawn.com/uploads/2/5/3/5/25358181/orespawn-1.7.10-20.3.jar` (Wayback capture of the author's own site orespawn.com, raw `id_` mode) |
| Size | 17,541,969 bytes |
| SHA-1 | `d43dbe9a400dc8df06418da3e04d36422b2176d7` |
| Same capture as `.zip` | `.../20191215074855id_/http://www.orespawn.com/uploads/.../orespawn-1.7.10-20.3.zip` - byte-identical (same SHA-1), deleted as duplicate |
| Independent copies with identical SHA-1 | archive.org items `orespawn-1.7.10-20.3_202109`, `ore-spawn-1.7.10-20.3`, `orespawn_202111` (file metadata sha1 via `archive.org/metadata/<id>`) |
| Content check | ZIP/JAR magic `PK\x03\x04`; 1974 entries: 594 `.class` (package `danger/orespawn`), 1058 `.png`, 310 `.ogg`, `sounds.json`, `mcmod.info`, `META-INF/MANIFEST.MF` (only `Manifest-Version: 1.0`, no signature, no coremod). No executables or foreign files. |
| Version inside jar | `@Mod(modid="OreSpawn", name="OreSpawn", version="1.7.10.20.3")` on `OreSpawnMain`; `OreSpawnMain.getVersion()` returns `"1.7.10.20.3"`; `mcmod.info` still says `1.7.10-20.2` (stale file) |
| Not used | CurseForge: `cfcheck.py orespawn` -> HTTP 404 (not listed); Modrinth search: only remakes/forks, no original. Mirrors not needed. |

Also captured on orespawn.com (not downloaded): `orespawn164v20a.zip` (1.6.4), `orespawnmc_1.12-development_0.1..0.7.jar`, DangerZone builds.

## 2. mcmod.info

```json
[
{
  "modid": "orespawn",
  "name": "The OreSpawn Mod",
  "description": "Just plain fun!",
  "version": "1.7.10-20.2",
  "mcversion": "1.7.10",
  "url": "OreSpawn.com",
  "updateUrl": "",
  "authors": ["TheyCallMeDanger"],
  "credits": "TheyCallMeDanger",
  "logoFile": "",
  "screenshots": [],
  "dependencies": []
}
]
```

Note: the Forge mod id at runtime is the `@Mod` value `OreSpawn`; the resource domain used by all textures/sounds is lower-case `orespawn`.

## 3. Package / class overview

- One package `danger.orespawn`, **594 classes** (586 top-level + 8 anonymous `Render*$1` in the item renderers), class file version 50 (Java 6).
- Main mod class: `danger.orespawn.OreSpawnMain`; proxies via `@SidedProxy(clientSide="danger.orespawn.ClientProxyOreSpawn", serverSide="danger.orespawn.CommonProxyOreSpawn")`.
- Superclass histogram (top): ModelBase 109, RenderLiving 106, EntityMob 54, Object 50, Item 42, Block 25, EntityAnimal 19, EntityTameable 17, ItemSword 14, BlockReed 10, EntityThrowable 9, EntityLiving 8, BehaviorProjectileDispense 8, BlockCrops 7, ItemFood 7, EntityAmbientCreature 6

### Where the code lives (for the behaviour port)

| Concern | Class / method (all in `danger.orespawn`) | Notes |
|---|---|---|
| Config (`Configuration.get`) | `OreSpawnMain.preInit` (IDs, tweaks, weapons, ores), `OreSpawnMain.getMobs` (per-mob enable flags + stats), helpers `get_armorstats`, `get_weaponstats`, `get_mobstats`, `get_orestats` | stats land in `MobStats`, `WeaponStats`, `ArmorStats`, `OreStats` objects held in `OreSpawnMain.<X>_stats` statics |
| Item/block construction and registration | `OreSpawnMain.preInit` (245 items, 86 blocks), `OreSpawnMain.initializeCagesAndEggs` (228 items: spawn eggs, critter cages), `OreSpawnMain.laySomeEggs` (125 "egg" blocks) | `GameRegistry.registerItem/registerBlock`, names via `LanguageRegistry.addNameForObject` |
| Entity registration | `OreSpawnMain.make_some_more_things` - 131 x `EntityRegistry.registerModEntity`, 134 x `registerGlobalEntityID` | all IDs from `EntityRegistry.findGlobalUniqueEntityId()` at runtime (no fixed numbers) |
| Natural spawns | `OreSpawnMain.make_some_more_things` - 348 x `EntityRegistry.addSpawn`, each guarded by `OreSpawnMain.<X>Enable` | per-entity extra conditions in `getCanSpawnHere` overrides (103 classes) |
| Recipes, smelting, chest loot, dimensions, tile entity, GUI handler, world generator | `OreSpawnMain.make_some_more_things` (tail) | `GameRegistry.addRecipe` 189, `addShapelessRecipe` 176, `addSmelting` 16, `ChestGenHooks` 9, `DimensionManager.registerProviderType/registerDimension` 6 |
| Dispenser behaviours | `OreSpawnMain.DoDispenserRegistrations` | `DispenserBehaviorOreSpawnEgg`, `MyDispenserBehaviorRock` |
| AI | vanilla `EntityAI*` tasks added in each entity constructor; custom tasks `MyEntityAIAvoidEntity`, `MyEntityAIDance`, `MyEntityAIFollowOwner`, `MyEntityAIJealousy`, `MyEntityAINearestAttackableTarget`(+`Sorter`), `MyEntityAITarget`, `MyEntityAIWander`, `MyEntityAIWanderALot`, `MyValentineTarget`, sorters `GenericTargetSorter`, `MyValentineTargetSorter` | most bosses implement their own targeting in `updateAITasks` (`func_70619_bc`, overridden in 86 classes) |
| World generation | `OreSpawnWorld` (IWorldGenerator, registered with weight 10), `ChunkOreGenerator`, `GenericDungeon`, `NightmareDungeon`, `RubyBirdDungeon`, `BasiliskMaze`, `CrystalMaze`, `Trees`, `MapGenMoreVillages` | |
| Dimensions | `WorldProviderOreSpawn`..`6`, `ChunkProviderOreSpawn`..`6`, `OreSpawnTeleporter`, biome `BiomeGenUtopianPlains` | see section 9 |
| Client | `ClientProxyOreSpawn.registerRenderThings` (133 entity renderers, 8 item renderers), `OreSpawnSounds` (event handler), `KeyHandler`, `GirlfriendOverlayGui` | |
| Network | `RiderControl`, `RiderControlMessage`, `RiderControlMessageHandler` (mount controls) | |
| GUI / containers | `CrystalFurnace`(+`GUI`, `Container`), `CrystalWorkbench`(+`GUI`, `Container`), `OreSpawnGUIHandler`, `TileEntityCrystalFurnace` | |

## 4. Entities

134 entity classes are registered (131 `registerModEntity` + 3 global-only: `UltimateFishHook`, `UltimateArrow`, `IrukandjiArrow`). Columns: registry name = string passed to `registerModEntity`; lang = `addStringLocalization("entity.<name>.name", "en_US", ...)`; HP/dmg/speed = values passed to `setBaseValue` in `applyEntityAttributes` (`func_110147_ax`), resolved through `mygetMaxHealth()`, constructor fields and `OreSpawnMain.<X>_stats` config defaults; several values mean the class has branches (e.g. "PlayNicely" or tamed variants). "inherited" = the class does not override `applyEntityAttributes`, vanilla parent values apply. "?" = not resolvable statically. armor = return of `getTotalArmorValue` (`func_70658_aO`). size = `setSize(w,h)` calls in the constructor. tracking = range/updateFrequency/sendVelocity.

| registry name | lang (en_US) | class | vanilla base | HP | attack | speed | armor | size w x h | tracking | spawns |
|---|---|---|---|---|---|---|---|---|---|---|
| - | - | UltimateFishHook | EntityFishHook | inherited (EntityFishHook) | inherited (EntityFishHook) | inherited (EntityFishHook) | - | 0.25x0.25 | global only | - |
| SunspotUrchin | - | SunspotUrchin | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| WaterBall | - | WaterBall | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| InkSack | - | InkSack | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| LaserBall | - | LaserBall | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| IceBall | - | IceBall | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| Acid | - | Acid | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| DeadIrukandji | - | DeadIrukandji | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| BerthaHit | - | BerthaHit | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | 0.33x0.33 | 64/1/1 | - |
| PurplePower | - | PurplePower | EntityLiving | 1000 | 500 | 0.25 | 25 | 0.75x0.75 | 64/1/1 | - |
| EntityThrownRock | - | EntityThrownRock | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| Girlfriend | Girlfriend | Girlfriend | EntityTameable | 800 / 80 | 8 | 0.3 | 23 | 0.5x1.6, 2.5x8 | 64/1/0 | 12 x addSpawn; creature; weight/min-max 10/3-6, 2/1-3, 30/8-15, 5/2-3 ...; biomes: beach, forest, forestHills, plains, river, stoneBeach, birchForest, birchForestHills +4; guarded by GirlfriendEnable |
| Apple Cow | Apple Cow | RedCow | EntityCow | inherited (EntityCow) | inherited (EntityCow) | inherited (EntityCow) | - | - | 64/1/0 | 6 x addSpawn; creature; weight/min-max 2/1-3, 5/2-5, 8/1-3, 8/4-8; biomes: plains, forest, megaTaiga, taiga, savanna, savannaPlateau; guarded by CowEnable |
| Golden Apple Cow | Golden Apple Cow | GoldCow | EntityCow | inherited (EntityCow) | inherited (EntityCow) | inherited (EntityCow) | - | - | 64/1/0 | 4 x addSpawn; creature; weight/min-max 5/2-5, 5/2-6; biomes: plains, forest, megaTaiga, taiga; guarded by CowEnable |
| Enchanted Golden Apple Cow | Enchanted Golden Apple Cow | EnchantedCow | EntityCow | inherited (EntityCow) | inherited (EntityCow) | inherited (EntityCow) | - | - | 64/1/0 | 4 x addSpawn; creature; weight/min-max 15/3-6, 3/2-4, 5/2-5; biomes: forest, plains, megaTaiga, mushroomIsland; guarded by CowEnable |
| Butterfly | Butterfly | EntityButterfly | EntityAmbientCreature | 2 | 0 | 0.1 | - | 0.4x0.4 | 32/1/0 | 16 x addSpawn; ambient; weight/min-max 10/1-5, 10/2-5, 15/2-4, 15/2-5 ...; biomes: beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains +8; guarded by ButterflyEnable |
| Moth | Moth | EntityLunaMoth | EntityAmbientCreature | 2 | 0 | 0.1 | - | 0.5x0.5 | 32/1/0 | 15 x addSpawn; ambient; weight/min-max 10/1-5, 10/2-5, 15/2-4, 15/2-5 ...; biomes: extremeHills, extremeHillsEdge, forestHills, forest, jungle, jungleHills, swampland, plains +7; guarded by MothEnable |
| Mosquito | Mosquito | EntityMosquito | EntityAmbientCreature | 2 | 0 | 0.1 | - | 0.2x0.2 | 16/1/0 | 4 x addSpawn; ambient; weight/min-max 15/2-5, 20/5-10, 30/5-10; biomes: swampland, jungle, jungleHills, roofedForest; guarded by MosquitoEnable |
| Firefly | Firefly | Firefly | EntityAmbientCreature | 1 | 0 | 0.1 | - | 0.4x0.8 | 64/1/0 | 13 x addSpawn; ambient; weight/min-max 10/2-8, 10/3-6, 10/4-8, 15/2-10 ...; biomes: forest, forestHills, swampland, jungle, jungleHills, stoneBeach, birchForest, birchForestHills +5; guarded by FireflyEnable |
| Bee | Bee | Bee | EntityMob | 80 | 12 | 0.32 | 5 | 1.5x2.5 | 64/1/0 | 10 x addSpawn; ambient; weight/min-max 2/1-1, 2/1-2, 3/1-1, 3/2-4 ...; biomes: forest, forestHills, jungle, jungleHills, birchForest, birchForestHills, megaTaiga, taiga +2; guarded by BeeEnable |
| Mothra | Mothra | Mothra | EntityAmbientCreature | 150 | 12 | 0.35 | 8 | 5x2 | 128/1/0 | 2 x addSpawn; ambient; weight/min-max 2/1-1; biomes: extremeHills, extremeHillsPlus; guarded by MothraEnable |
| Ant | Ant | EntityAnt | EntityAnimal | 1 | 0 | 0.15 | - | 0.1x0.1 | 16/1/0 | - |
| Red Ant | Red Ant | EntityRedAnt | EntityAnimal | 2 | 1 | 0.2 | - | 0.2x0.2 | 16/1/0 | - |
| Rainbow Ant | Rainbow Ant | EntityRainbowAnt | EntityAnimal | 1 | 0 | 0.15 | - | 0.1x0.1 | 16/1/0 | - |
| Unstable Ant | Unstable Ant | EntityUnstableAnt | EntityAnimal | 1 | 0 | 0.15 | - | 0.1x0.1 | 16/1/0 | - |
| Bomb-Omb | Bomb-Omb | Robot1 | EntityMob | 5 | 4 | 0.2 | 2 | 0.5x0.5 | 32/1/0 | - |
| Robo-Pounder | Robo-Pounder | Robot2 | EntityMob | 200 | 22 | 0.3 | 18 | 3x6.2 | 64/1/0 | - |
| Robo-Gunner | Robo-Gunner | Robot3 | EntityMob | 80 | 16 | 0.35 | 14 | 2.5x5 | 64/1/0 | - |
| Robo-Warrior | Robo-Warrior | Robot4 | EntityMob | 170 | 12 | 0.34 | 18 | 2.5x4 | 64/1/0 | - |
| Robo-Sniper | Robo-Sniper | Robot5 | EntityMob | 20 | 5 | 0.3 | 6 | 1x2.25 | 64/1/0 | - |
| Alosaurus | Alosaurus | Alosaurus | EntityMob | 110 | 18 | 0.35 | 8 | 1.9x3.6 | 64/1/0 | - |
| Cryolophosaurus | Cryolophosaurus | Cryolophosaurus | EntityMob | 10 | 3 | 0.25 | 1 | 0.75x0.75 | 64/1/0 | - |
| Basilisk | Basilisk | Basilisk | EntityMob | 200 | 24 | 0.4 | 15 | 1.6x3.5 | 64/1/0 | 4 x addSpawn; ambient; weight/min-max 15/1-2, 2/1-1, 3/1-1, 4/1-2; biomes: jungle, jungleHills, birchForestHills, roofedForest; guarded by BasiliskEnable |
| Camarasaurus | Camarasaurus | Camarasaurus | EntityTameable | 20 | 1 | 0.2 | - | 0.5x1.2 | 64/1/0 | - |
| Hydrolisc | Hydrolisc | Hydrolisc | EntityTameable | 100 | 1 | 0.25 | 10 | 0.5x0.5 | 64/1/0 | 4 x addSpawn; creature; weight/min-max 10/1-3, 15/2-5, 25/3-6, 5/3-6; biomes: swampland, jungle, jungleHills, stoneBeach; guarded by HydroliscEnable |
| Velocity Raptor | Velocity Raptor | VelocityRaptor | EntityTameable | 10 | 2 | 0.55 | 3 / 0 | 0.5x0.6 | 64/1/0 | - |
| Dragonfly | Dragonfly | Dragonfly | EntityAnimal | 10 | 2 | 0.33 | - | 1.5x0.5 | 64/1/0 | 2 x addSpawn; ambient; weight/min-max 4/1-2, 5/3-5; biomes: swampland, river; guarded by DragonflyEnable |
| Emperor Scorpion | Emperor Scorpion | EmperorScorpion | EntityMob | 350 | 35 | 0.35 | 20 | 3.5x3 | 64/1/0 | 2 x addSpawn; ambient; weight/min-max 1/1-1, 2/1-1; biomes: desert, savanna; guarded by EmperorScorpionEnable |
| Scorpion | Scorpion | Scorpion | EntityMob | 15 | 4 | 0.2 | 10 | 0.85x0.55 | 32/1/0 | 7 x addSpawn; ambient; weight/min-max 15/2-4, 15/3-5, 15/3-6, 28/2-4 ...; biomes: desert, roofedForest, savanna, savannaPlateau, mesa, mesaPlateau, mesaPlateau_F; guarded by ScorpionEnable |
| CaveFisher | CaveFisher | CaveFisher | EntityMob | 10 | 4 | 0.2 | 4 | 1.35x0.75 | 32/1/0 | - |
| Baby Dragon | Baby Dragon | Spyro | EntityTameable | 200 | 5 | 0.3 | 5 | 0.5x0.5 | 64/1/0 | - |
| Baryonyx | Baryonyx | Baryonyx | EntityAnimal | 40 | 8 | 0.25 | - | 1.5x2.8 | 64/1/0 | - |
| WTF? | WTF? | GammaMetroid | EntityTameable | 100 | 10 | 0.15 | 12 | 1.5x1.5 | 64/1/0 | - |
| Bird | Bird | Cockateil | EntityAnimal | 2 | 1 | 0.33 | - | 0.5x0.5 | 32/1/0 | 16 x addSpawn; ambient; weight/min-max 10/1-2, 10/2-4, 10/2-5, 11/1-5 ...; biomes: beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains +8; guarded by CockateilEnable |
| Ruby Bird | Ruby Bird | RubyBird | EntityAnimal | 2 | 1 | 0.33 | - | 0.5x0.5 | 32/1/0 | - |
| Kyuubi | Kyuubi | Kyuubi | EntityMob | 125 | 10 | 0.25 | 10 | 0.5x1.25 | 64/1/0 | 1 x addSpawn; monster; weight/min-max 10/1-1; biomes: hell; guarded by KyuubiEnable |
| Water Dragon | Water Dragon | WaterDragon | EntityTameable | 150 | 20 | 0.25 | 8 | 1.25x1.9 | 64/1/0 | 4 x addSpawn; waterCreature; weight/min-max 2/1-1, 3/1-1, 5/1-1; biomes: river, swampland, ocean, stoneBeach; guarded by WaterDragonEnable |
| Attack Squid | - | AttackSquid | EntityMob | 10 | 8 | 0.25 | 0 | 1x1.25 | 32/1/0 | 3 x addSpawn; waterCreature; weight/min-max 10/5-9, 12/6-10, 7/4-8; biomes: river, swampland, ocean; guarded by AttackSquidEnable |
| Alien | Alien | Alien | EntityMob | 100 | 12 | 0.65 | 8 | 1.1x3.25 | 64/1/0 | - |
| Hoverboard | Hoverboard | Elevator | EntityLiving | 60 | 0 | 1.33 | - | 1.25x1 | 128/1/1 | - |
| The Kraken | The Kraken | Kraken | EntityMob | 1000 | 40 | 0.37 | 10 | 4x15, 1.3333x5 | 128/1/0 | - |
| Lizard | Lizard | Lizard | EntityTameable | 30 | 6 | 0.3 | 5 | 1.5x1.25 | 64/1/0 | 3 x addSpawn; waterCreature; weight/min-max 2/2-4, 4/2-4, 5/2-4; biomes: river, swampland, ocean; guarded by LizardEnable |
| Cephadrome | Cephadrome | Cephadrome | EntityCreature | 300 | 70 | 0.25 | 16 | 2.5x2.25 | 128/1/1 | 2 x addSpawn; ambient; weight/min-max 1/1-1; biomes: icePlains, coldTaiga; guarded by CephadromeEnable |
| Dragon | Dragon | Dragon | EntityTameable | 200 | 35 | 0.32 | 14 | 1.5x1.25 | 128/1/1 | - |
| Chipmunk | Chipmunk | Chipmunk | EntityTameable | 5 | 1 | 0.38 | 3 / 0 | 0.35x0.35 | 32/1/0 | 9 x addSpawn; ambient; weight/min-max 10/2-5, 2/1-2, 2/2-5, 4/3-6 ...; biomes: forest, forestHills, jungle, plains, birchForest, birchForestHills, roofedForest, megaTaiga +1; guarded by ChipmunkEnable |
| Gazelle | Gazelle | Gazelle | EntityTameable | 15 | 0 | 0.2 / 0.3 | - | 0.6x1.8 | 64/1/0 | - |
| Ostrich | Ostrich | Ostrich | EntityTameable | 25 | 6 | 0.2 / 0.38 | 3 / 0 | 0.85x2.1 | 64/1/1 | 4 x addSpawn; ambient; weight/min-max 1/1-1; biomes: desert, stoneBeach, savanna, savannaPlateau; guarded by OstrichEnable |
| Jumpy Bug | Jumpy Bug | TrooperBug | EntityMob | 200 | 20 | 0.4 | 15 | 3x3.5 | 64/1/0 | 2 x addSpawn; ambient; weight/min-max 1/1-1, 3/1-2; biomes: swampland, mesa; guarded by TrooperBugEnable |
| Spit Bug | Spit Bug | SpitBug | EntityMob | 100 | 10 | 0.33 | 12 | 2x2 | 64/1/0 | 1 x addSpawn; ambient; weight/min-max 6/1-2; biomes: swampland; guarded by SpitBugEnable |
| Stink Bug | Stink Bug | StinkBug | EntityAnimal | 5 | 0 | 0.15 | - | 0.55x0.55 | 32/1/0 | 5 x addSpawn; ambient; weight/min-max 10/2-4, 4/2-4, 6/2-4, 8/2-4 ...; biomes: forest, jungle, forestHills, jungleHills, savanna; guarded by StinkBugEnable |
| T-Shirt | T-Shirt! | Tshirt | EntityAnimal | 1 | 0 | 0 | 0 | 4x4 | 32/1/0 | - |
| Island | Light Floating Island | Island | EntityAnimal | inherited (EntityAnimal) | inherited (EntityAnimal) | inherited (EntityAnimal) | - | 0.5x0.5 | 64/1/0 | - |
| IslandToo | Dark Floating Island | IslandToo | EntityAnimal | inherited (EntityAnimal) | inherited (EntityAnimal) | inherited (EntityAnimal) | - | 0.5x0.5 | 64/1/0 | - |
| Creeping Horror | Creeping Horror | CreepingHorror | EntityMob | 10 | 3 | 0.25 | 2 | 0.75x0.5 | 64/1/0 | - |
| Terrible Terror | Terrible Terror | TerribleTerror | EntityMob | 10 | 5 | 0.1 | 3 | 1x0.75 | 64/1/0 | - |
| Cliff Racer | Cliff Racer | CliffRacer | EntityAnimal | 5 | 1 | 0.33 | - | 0.75x0.5 | 32/1/0 | - |
| Triffid | Triffid | Triffid | EntityMob | 100 | 20 | 0.13 | 12 | 2x4 | 64/1/0 | - |
| Nightmare | Nightmare | PitchBlack | EntityMob | formula `(PitchBlack_stats.health * this->PitchBlack.getPitchBlackScale())` | formula `(this->PitchBlack.getPitchBlackScale() * PitchBlack_stats.attack)` | formula `(this.MyMoveSpeed + (0.10000000149011612 * this->PitchBlack.getPitchBlackScale()))` | formula `(PitchBlack_stats.defense + (2.0 * this->PitchBlack.getPitchBlackScale()))` | 2x3 | 64/1/0 | - |
| Lurking Terror | Lurking Terror | LurkingTerror | EntityMob | 30 | 6 | 0.25 | 5 | 1.75x1.25 | 64/1/0 | - |
| Mobzilla | Mobzilla | Godzilla | EntityMob | 4000 | 175 | 0.75 | 25 / 21 | 9.9x25, 2.475x6.25 | 128/1/0 | - |
| Ghost | Ghost | Ghost | EntityAmbientCreature | 2 | 0 | 0.1 | - | 0.5x1.5 | 32/1/0 | 27 x addSpawn; ambient; weight/min-max 10/5-10, 15/2-5, 15/3-6, 15/5-10 ...; biomes: beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains +16; guarded by GhostEnable |
| Ghost Pumpkin Skelly | Ghost Pumpkin Skelly | GhostSkelly | EntityAmbientCreature | 5 | 0 | 0.1 | - | 1.5x2 | 64/1/0 | 27 x addSpawn; ambient; weight/min-max 10/5-10, 15/2-5, 15/3-6, 15/5-10 ...; biomes: beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains +16; guarded by GhostSkellyEnable |
| Small Worm | Small Worm | WormSmall | EntityMob | 10 | 3 | 0.1 | 0 | 0.25x1 | 32/1/0 | - |
| Medium Worm | Medium Worm | WormMedium | EntityMob | 30 | 10 | 0.1 | 8 | 0.5x2 | 64/1/0 | - |
| Large Worm | Large Worm | WormLarge | EntityMob | 90 | 18 | 0.2 | 14 | 1.55x2.5 | 64/1/0 | 3 x addSpawn; creature; weight/min-max 10/1-1, 15/1-1, 25/1-1; biomes: plains, savanna, savannaPlateau; guarded by WormEnable |
| Cassowary | Cassowary | Cassowary | EntityAnimal | 10 | 8 | 0.25 | - | 0.5x1.2 | 64/1/0 | 9 x addSpawn; ambient; weight/min-max 10/1-5, 15/2-5, 3/1-5, 5/1-2 ...; biomes: extremeHills, extremeHillsEdge, extremeHillsPlus, birchForest, birchForestHills, megaTaiga, megaTaigaHills, savanna +1; guarded by CassowaryEnable |
| Cloud Shark | Cloud Shark | CloudShark | EntityMob | 15 | 6 | 0.3 | 5 | 1x0.75 | 64/1/0 | - |
| Gold Fish | Goldfish | GoldFish | EntityAnimal | 6 | 1 | 0.22 | - | 0.75x0.5 | 32/1/0 | - |
| Leaf Monster | LeafMonster | LeafMonster | EntityMob | 6 | 2 | 0.25 | 1 | 1x2.5 | 64/1/0 | 8 x addSpawn; ambient; weight/min-max 2/2-5, 2/3-6, 3/1-2, 3/2-4 ...; biomes: jungle, forest, jungleHills, forestHills, birchForest, birchForestHills, megaTaiga, taiga; guarded by LeafMonsterEnable |
| MobzillaHead | - | GodzillaHead | EntityLiving | 4000 | 0 | 1.33 | - | 9.9x10 | 128/10/1 | - |
| Ender Knight | Ender Knight | EnderKnight | EntityMob | 60 | 12 | 0.32 | 6 | 0.6x2.9 | 64/1/0 | 9 x addSpawn; ambient; weight/min-max 2/2-4, 20/2-4, 4/2-4; biomes: extremeHills, extremeHillsEdge, forest, forestHills, jungleHills, plains, river, desert +1; guarded by EnderKnightEnable |
| Ender Reaper | Ender Reaper | EnderReaper | EntityMob | 90 | 18 | 0.37 | 8 | 0.7x2.9 | 64/1/0 | 9 x addSpawn; ambient; weight/min-max 1/1-2, 2/1-2, 38/2-4; biomes: extremeHills, extremeHillsEdge, forest, forestHills, jungleHills, plains, river, desert +1; guarded by EnderReaperEnable |
| Beaver | Beaver | Beaver | EntityAnimal | 15 | 1 | 0.15 / 0.2 | - | 0.6x0.8 | 64/1/0 | 6 x addSpawn; creature; weight/min-max 10/2-4, 2/2-4, 2/2-5, 3/2-4 ...; biomes: river, forest, birchForest, birchForestHills, megaTaiga, taiga; guarded by BeaverEnable |
| Termite | Termite | Termite | EntityAnimal | 5 | 2 | 0.2 | - | 0.2x0.2 | 32/1/0 | - |
| Fairy | Fairy | Fairy | EntityAmbientCreature | 40 | 3 | 0.1 | 4 | 0.4x0.8 | 32/1/0 | 1 x addSpawn; ambient; weight/min-max 25/2-4; biomes: roofedForest; guarded by FairyEnable |
| Peacock | Peacock | Peacock | EntityAnimal | 15 | 4 | 0.38 | - | 0.65x1.2 | 64/1/0 | 2 x addSpawn; ambient; weight/min-max 1/1-3; biomes: mesa, mesaPlateau; guarded by PeacockEnable |
| Rotator | Rotator | Rotator | EntityMob | 35 | 10 | 0.25 | 8 | 1x2 | 64/1/0 | - |
| Vortex | Vortex | Vortex | EntityMob | 150 | 26 | 0.35 | 10 | 2x4 | 64/1/0 | - |
| Dungeon Beast | Dungeon Beast | DungeonBeast | EntityMob | 65 | 12 | 0.29 | 6 | 1.15x1.1 | 64/1/0 | 1 x addSpawn; ambient; weight/min-max 20/2-4; biomes: roofedForest; guarded by DungeonBeastEnable |
| Rat | Rat | Rat | EntityMob | 5 | 3 | 0.25 | 1 | 0.25x0.5 | 32/1/0 | 2 x addSpawn; ambient; weight/min-max 25/2-8, 35/10-20; biomes: roofedForest, taiga; guarded by RatEnable |
| Flounder | Flounder | Flounder | EntityAnimal | 5 | 0 | 0.25 | - | 0.55x0.25 | 32/1/0 | - |
| Whale | Whale | Whale | EntityAnimal | 100 | 0 | 0.35 | - | 1.5x2.5 | 64/1/0 | 1 x addSpawn; waterCreature; weight/min-max 1/1-2; biomes: deepOcean; guarded by WhaleEnable |
| Irukandji | Irukandji | Irukandji | EntityMob | 1 | 20 | 0.15 | 0 | 0.25x0.25 | 32/1/0 | - |
| Skate | Skate | Skate | EntityMob | 8 | 8 | 0.25 | 4 | 0.75x0.25 | 32/1/0 | - |
| Crystal Urchin | Crystal Urchin | Urchin | EntityMob | 25 | 10 | 0.3 | 4 | 1.35x2.1 | 64/1/0 | - |
| Mantis | Mantis | Mantis | EntityMob | 120 | 16 | 0.32 | 10 | 2.5x3.25 | 64/1/0 | 9 x addSpawn; ambient; weight/min-max 1/1-1, 1/1-2, 1/2-4, 5/1-2; biomes: forest, forestHills, jungle, plains, swampland, birchForest, megaTaiga, savanna +1; guarded by MantisEnable |
| Hercules Beetle | Hercules Beetle | HerculesBeetle | EntityMob | 250 | 30 | 0.25 | 19 | 3.25x2.75 | 64/1/0 | 7 x addSpawn; ambient; weight/min-max 2/1-1, 2/1-2, 5/1-1, 5/1-2; biomes: forestHills, jungleHills, extremeHillsEdge, taigaHills, birchForestHills, coldTaigaHills, megaTaigaHills; guarded by HerculesBeetleEnable |
| T. Rex | T. Rex | TRex | EntityMob | 160 | 22 | 0.38 | 14 | 2x4.2 | 64/1/0 | - |
| Stinky | Stinky | Stinky | EntityTameable | 100 | 10 | 0.3 | 6 | 0.75x0.75 | 64/1/0 | 4 x addSpawn; ambient,monster; weight/min-max 1/1-1, 2/1-1; biomes: hell, mesa, mesaPlateau, mesaPlateau_F; guarded by StinkyEnable |
| Coin | Coin! | Coin | EntityAnimal | 1 | 0 | 0 | 0 | 1.5x1.5 | 64/1/0 | 6 x addSpawn; ambient; weight/min-max 2/1-1; biomes: taiga, forest, jungle, birchForest, coldTaiga, megaTaiga; guarded by CoinEnable |
| The King | The King | TheKing | EntityMob | 7000 | 250 | 0.62 | 25 / 22 / 23 / 24 / 21 | 22x24, 5.5x6 | 128/1/0 | - |
| KingHead | - | KingHead | EntityLiving | 7000 | 0 | 1.33 | - | 19.9x10 | 128/10/1 | - |
| The Queen | The Queen | TheQueen | EntityMob | 6000 | 250 | 0.62 | 23 / 24 / 26 / 21 | 22x24, 5.5x6 | 128/1/0 | - |
| QueenHead | - | QueenHead | EntityLiving | 6000 | 0 | 1.33 | - | 19.9x10 | 128/10/1 | - |
| Boyfriend | Boyfriend | Boyfriend | EntityTameable | 80 | 8 | 0.3 | 23 | 0.5x1.6 | 64/1/0 | 12 x addSpawn; creature; weight/min-max 10/3-6, 2/1-3, 30/8-15, 5/2-3 ...; biomes: beach, forest, forestHills, plains, river, stoneBeach, birchForest, birchForestHills +4; guarded by BoyfriendEnable |
| The Prince | The Prince | ThePrince | EntityTameable | 500 | 10 | 0.3 / 0.32 | 16 | 0.75x1.25 | 64/1/0 | - |
| Molenoid | Molenoid | Molenoid | EntityMob | 200 | 18 | 0.35 | 12 | 3.9x2.6 | 64/1/0 | 3 x addSpawn; ambient; weight/min-max 2/1-1, 2/1-2; biomes: plains, savanna, savannaPlateau; guarded by MolenoidEnable |
| Sea Monster | Sea Monster | SeaMonster | EntityMob | 110 | 14 | 0.25 | 8 | 1.25x2.5 | 64/1/0 | 2 x addSpawn; waterCreature; weight/min-max 2/1-1, 4/1-1; biomes: ocean, swampland; guarded by SeaMonsterEnable |
| Sea Viper | Sea Viper | SeaViper | EntityMob | 160 | 22 | 0.25 / 0.35 | 12 | 1.5x2.5 | 64/1/0 | 2 x addSpawn; waterCreature; weight/min-max 2/1-1, 3/1-1; biomes: ocean, stoneBeach; guarded by SeaViperEnable |
| EasterBunny | - | EasterBunny | EntityAnimal | 10 | 8 | 0.45 | - | 0.5x0.75 | 64/1/0 | 7 x addSpawn; ambient; weight/min-max 10/1-2, 5/1-2, 8/1-2; biomes: plains, forest, forestHills, birchForest, birchForestHills, megaTaiga, taiga; guarded by EasterBunnyEnable,easter_day |
| CaterKiller | CaterKiller | CaterKiller | EntityMob | 450 | 32 | 0.35 | 19 | 2.9x4.6, 1.45x2.3 | 64/1/0 | 9 x addSpawn; ambient; weight/min-max 10/1-2, 2/1-1, 2/1-2, 4/1-2 ...; biomes: forest, jungle, forestHills, jungleHills, birchForest, birchForestHills, megaTaiga, taiga +1; guarded by CaterKillerEnable |
| Crystal Apple Cow | Crystal Apple Cow | CrystalCow | EntityCow | inherited (EntityCow) | inherited (EntityCow) | inherited (EntityCow) | - | - | 64/1/0 | - |
| Leonopteryx | Leonopteryx | Leon | EntityTameable | 250 | 55 | 0.25 | 16 | 3.5x8.25 | 64/1/0 | - |
| Hammerhead | Hammerhead | Hammerhead | EntityMob | 240 | 75 | 0.35 | 20 | 3x5 | 64/1/0 | - |
| Rubber Ducky | Rubber Ducky | RubberDucky | EntityTameable | 5 | 6 | 0.22 | 1 | 0.33x0.5 | 64/1/0 | 2 x addSpawn; waterCreature; weight/min-max 10/10-20, 4/4-6; biomes: river, stoneBeach; guarded by RubberDuckyEnable |
| The Young Prince | The Young Prince | ThePrinceTeen | EntityTameable | 1500 | 50 | 0.32 | 18 | 3.25x4.25 | 64/1/0 | - |
| Criminal | Criminal | BandP | EntityMob | 100 | 1 | 0.32 | 18 | 0.75x1.75 | 64/1/0 | 3 x addSpawn; ambient; weight/min-max 20/1-2; biomes: plains, desert, savanna; guarded by CriminalEnable |
| Rock | Rock | RockBase | EntityLiving | ? | ? | ? | 0 | 0.25x0.15 | 32/1/0 | - |
| Brutalfly | Brutalfly | Brutalfly | EntityMob | 110 | 10 | 0.35 | 6 | 5x2 | 128/1/0 | 3 x addSpawn; ambient; weight/min-max 2/1-1; biomes: megaTaigaHills, extremeHillsPlus, mesaPlateau; guarded by BrutalflyEnable |
| Nastysaurus | Nastysaurus | Nastysaurus | EntityMob | 200 | 32 | 0.35 | 17 | 2.2x4.6 | 128/1/0 | - |
| Pointysaurus | Pointysaurus | Pointysaurus | EntityMob | 80 | 10 | 0.35 | 16 | 2.9x2.9 | 64/1/0 | - |
| Cricket | Cricket | Cricket | EntityAnimal | 3 | 0 | 0.15 | - | 0.1x0.1 | 32/1/0 | 11 x addSpawn; ambient; weight/min-max 1/1-4, 2/1-6, 2/2-4, 2/2-6 ...; biomes: forest, forestHills, jungle, jungleHills, plains, birchForest, birchForestHills, roofedForest +3; guarded by CricketEnable |
| The Princess | The Princess | ThePrincess | EntityTameable | 400 | 10 | 0.3 / 0.32 | 14 | 0.75x1.25 | 64/1/0 | - |
| Frog | Frog | Frog | EntityAnimal | 8 | 0 | 0.1 | - | 0.75x0.75 | 32/1/0 | 5 x addSpawn; ambient,waterCreature; weight/min-max 2/2-6, 20/2-6, 20/3-6, 3/3-6; biomes: river, jungle, swampland; guarded by FrogEnable |
| The Young Adult Prince | The Young Adult Prince | ThePrinceAdult | EntityTameable | 3000 | 100 | 0.36 | 20 | 6.25x10.25 | 128/1/0 | - |
| Robot Spider | Robot Spider | SpiderRobot | EntityLiving | 1500 | 100 | 0.35 | 16 | 3.25x2.25 | 128/1/0 | - |
| Spider Driver | Spider Driver | SpiderDriver | EntitySpider | inherited (EntitySpider) | inherited (EntitySpider) | inherited (EntitySpider) | 8 / 20 | - | 64/1/0 | - |
| Jeffery | Jeffery | GiantRobot | EntityMob | 550 | 40 | 0.55 | 18 | 3x9.75 | 128/1/0 | - |
| Robot Red Ant | Robot Red Ant | AntRobot | EntityLiving | 300 | 30 | 0.3 | 16 | 2.75x1.25 | 128/1/0 | - |
| Crab | Crab | Crab | EntityMob | formula `(PitchBlack_stats.health * this->Crab.getCrabScale())` | formula `(Crab_stats.attack * this->Crab.getCrabScale())` | formula `(this.moveSpeed * this->Crab.getCrabScale())` | formula `(Crab_stats.defense + (2.0 * this->Crab.getCrabScale()))` | 1.25x2.5 | 64/1/0 | 3 x addSpawn; waterCreature; weight/min-max 1/2-4, 1/3-6, 2/3-6; biomes: ocean, swampland, stoneBeach; guarded by CrabEnable |
| Shoes | - | Shoes | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| EntityCage | - | EntityCage | EntityThrowable | inherited (EntityThrowable) | inherited (EntityThrowable) | inherited (EntityThrowable) | - | - | 64/1/1 | - |
| - | - | UltimateArrow | EntityArrow | inherited (EntityArrow) | inherited (EntityArrow) | inherited (EntityArrow) | - | - | global only | - |
| - | - | IrukandjiArrow | EntityArrow | inherited (EntityArrow) | inherited (EntityArrow) | inherited (EntityArrow) | - | - | global only | - |

Entity IDs: every `<X>ID` static in `OreSpawnMain` is assigned `EntityRegistry.findGlobalUniqueEntityId()` (119 statics, `<clinit>` value 0). There are no fixed numeric entity IDs in the jar.

Observed in bytecode, worth checking against wikis: `Crab.applyEntityAttributes` reads `OreSpawnMain.PitchBlack_stats.health` (not `Crab_stats.health`) multiplied by `getCrabScale()`; attack uses `Crab_stats.attack`. The decompiled source shows the same line, so it is in the original code.

### Mob stat config defaults (`OreSpawnMain.getMobs` -> `get_mobstats`)

Category `OreSpawnMOBS`, keys `<name>_health`, `<name>_attack`, `<name>_defense`. These are the config defaults; the entity table above shows how each class uses them.

| config name | static | health | attack | defense |
|---|---|---|---|---|
| Bee | Bee_stats | 80 | 12 | 5 |
| Mantis | Mantis_stats | 120 | 16 | 10 |
| HerculesBeetle | HerculesBeetle_stats | 250 | 30 | 19 |
| Mothra | Mothra_stats | 150 | 12 | 8 |
| Brutalfly | Brutalfly_stats | 110 | 10 | 6 |
| Nastysaurus | Nastysaurus_stats | 200 | 32 | 17 |
| Pointysaurus | Pointysaurus_stats | 80 | 10 | 16 |
| Alosaurus | Alosaurus_stats | 110 | 18 | 8 |
| SpiderRobot | SpiderRobot_stats | 1500 | 100 | 16 |
| AntRobot | AntRobot_stats | 300 | 30 | 16 |
| Jeffery | Jeffery_stats | 550 | 40 | 18 |
| Hammerhead | Hammerhead_stats | 240 | 75 | 20 |
| Molenoid | Molenoid_stats | 200 | 18 | 12 |
| TRex | TRex_stats | 160 | 22 | 14 |
| BandP | BandP_stats | 100 | 1 | 18 |
| CaterKiller | CaterKiller_stats | 450 | 32 | 19 |
| Cryolophosaurus | Cryolophosaurus_stats | 10 | 3 | 1 |
| Rat | Rat_stats | 5 | 3 | 1 |
| Urchin | Urchin_stats | 25 | 10 | 4 |
| Kyuubi | Kyuubi_stats | 125 | 10 | 10 |
| GammaMetroid | GammaMetroid_stats | 100 | 10 | 12 |
| Basilisk | Basilisk_stats | 200 | 24 | 15 |
| EmperorScorpion | EmperorScorpion_stats | 350 | 35 | 20 |
| TrooperBug | TrooperBug_stats | 200 | 20 | 15 |
| SpitBug | SpitBug_stats | 100 | 10 | 12 |
| Alien | Alien_stats | 100 | 12 | 8 |
| WaterDragon | WaterDragon_stats | 150 | 20 | 8 |
| SeaMonster | SeaMonster_stats | 110 | 14 | 8 |
| SeaViper | SeaViper_stats | 160 | 22 | 12 |
| Robot2 | Robot2_stats | 200 | 22 | 18 |
| Robot3 | Robot3_stats | 80 | 16 | 14 |
| Robot4 | Robot4_stats | 170 | 12 | 18 |
| Robot5 | Robot5_stats | 20 | 5 | 6 |
| Rotator | Rotator_stats | 35 | 10 | 8 |
| Vortex | Vortex_stats | 150 | 26 | 10 |
| DungeonBeast | DungeonBeast_stats | 65 | 12 | 6 |
| Triffid | Triffid_stats | 100 | 20 | 12 |
| LurkingTerror | LurkingTerror_stats | 30 | 6 | 5 |
| WormSmall | WormSmall_stats | 10 | 3 | 0 |
| WormMedium | WormMedium_stats | 30 | 10 | 8 |
| WormLarge | WormLarge_stats | 90 | 18 | 14 |
| EnderKnight | EnderKnight_stats | 60 | 12 | 6 |
| EnderReaper | EnderReaper_stats | 90 | 18 | 8 |
| Irukandji | Irukandji_stats | 1 | 20 | 0 |
| AttackSquid | AttackSquid_stats | 10 | 8 | 0 |
| CaveFisher | CaveFisher_stats | 10 | 4 | 4 |
| CloudShark | CloudShark_stats | 15 | 6 | 5 |
| CreepingHorror | CreepingHorror_stats | 10 | 3 | 2 |
| Mobzilla | Godzilla_stats | 4000 | 175 | 21 |
| Kraken | Kraken_stats | 1000 | 40 | 10 |
| LeafMonster | LeafMonster_stats | 6 | 2 | 1 |
| Nightmare | PitchBlack_stats | 250 | 30 | 10 |
| Scorpion | Scorpion_stats | 15 | 4 | 10 |
| Skate | Skate_stats | 8 | 8 | 4 |
| TerribleTerror | TerribleTerror_stats | 10 | 5 | 3 |
| TheKing | TheKing_stats | 7000 | 350 | 21 |
| TheQueen | TheQueen_stats | 6000 | 225 | 21 |
| Leonopteryx | Leon_stats | 150 | 20 | 8 |
| Crab | Crab_stats | 180 | 24 | 16 |

## 5. Items (473)

Construction in `OreSpawnMain` (`new <Class>(BaseItemID + n)` then `setUnlocalizedName`), names from `LanguageRegistry.addNameForObject(item, "en_US", name)`. Item IDs are `BaseItemID (config default 9000) + n`. Icon = `registerIcons` string, 473 of 473 resolved to an existing PNG in `assets/orespawn/textures/items/`. Full table with registry names, IDs, icon paths and PNG sizes: `items_blocks.txt`. Format below: *English name* (`unlocalized`).

**Armor (ItemOreSpawnArmor)** (56): The Ultimate Helmet (`ultimate_helmet`), The Ultimate Chestplate (`ultimate_chest`), The Ultimate Leggings (`ultimate_leggings`), The Ultimate Boots (`ultimate_boots`), Lava Eel Helmet (`lavaeel_helmet`), Lava Eel Chestplate (`lavaeel_chest`), Lava Eel Leggings (`lavaeel_leggings`), Lava Eel Boots (`lavaeel_boots`), Moth Scale Helmet (`mothscale_helmet`), Moth Scale Chestplate (`mothscale_chest`), Moth Scale Leggings (`mothscale_leggings`), Moth Scale Boots (`mothscale_boots`), Emerald Helmet (`emerald_helmet`), Emerald Chestplate (`emerald_chest`), Emerald Leggings (`emerald_leggings`), Emerald Boots (`emerald_boots`), Experience Helmet (`experience_helmet`), Experience Chestplate (`experience_chest`), Experience Leggings (`experience_leggings`), Experience Boots (`experience_boots`), Ruby Helmet (`ruby_helmet`), Ruby Chestplate (`ruby_chest`), Ruby Leggings (`ruby_leggings`), Ruby Boots (`ruby_boots`), Amethyst Helmet (`amethyst_helmet`), Amethyst Chestplate (`amethyst_chest`), Amethyst Leggings (`amethyst_leggings`), Amethyst Boots (`amethyst_boots`), Pink Tourmailine Helmet (`pink_helmet`), Pink Tourmailine Chestplate (`pink_chest`), Pink Tourmailine Leggings (`pink_leggings`), Pink Tourmailine Boots (`pink_boots`), Tiger's Eye Helmet (`tigerseye_helmet`), Tiger's Eye Chestplate (`tigerseye_chest`), Tiger's Eye Leggings (`tigerseye_leggings`), Tiger's Eye Boots (`tigerseye_boots`), Peacock Feather Boots (`peacock_boots`), Peacock Feather Helmet (`peacock_helmet`), Peacock Feather Chestplate (`peacock_chest`), Peacock Feather Leggings (`peacock_leggings`), Mobzilla Scale Helmet (`mobzilla_helmet`), Mobzilla Scale Chestplate (`mobzilla_chest`), Mobzilla Scale Leggings (`mobzilla_leggings`), Mobzilla Scale Boots (`mobzilla_boots`), Royal Guardian Helmet (`royal_helmet`), Royal Guardian Chestplate (`royal_chest`), Royal Guardian Leggings (`royal_leggings`), Royal Guardian Boots (`royal_boots`), Lapis Lazuli Helmet (`lapis_helmet`), Lapis Lazuli Chestplate (`lapis_chest`), Lapis Lazuli Leggings (`lapis_leggings`), Lapis Lazuli Boots (`lapis_boots`), Queen Scale Helmet (`queen_helmet`), Queen Scale Chestplate (`queen_chest`), Queen Scale Leggings (`queen_leggings`), Queen Scale Boots (`queen_boots`)

**Critter cages (CritterCage)** (114): Empty Critter Cage (`cageempty`), Caged Spider (`cagespider`), Caged Bat (`cagebat`), Caged Cow (`cagecow`), Caged Pig (`cagepig`), Caged Squid (`cagesquid`), Caged Chicken (`cagechicken`), Caged Creeper (`cagecreeper`), Caged Skeleton (`cageskeleton`), Caged Zombie (`cagezombie`), Caged Slime (`cageslime`), Caged Ghast (`cageghast`), Caged ZombiePigman (`cagezombiepigman`), Caged Enderman (`cageenderman`), Caged Cave Spider (`cagecavespider`), Caged Silverfish (`cagesilverfish`), Caged Magma Cube (`cagemagmacube`), Caged Witch (`cagewitch`), Caged Sheep (`cagesheep`), Caged Wolf (`cagewolf`), Caged Mooshroom (`cagemooshroom`), Caged Ocelot (`cageocelot`), Caged Blaze (`cageblaze`), Caged Girlfriend (`cagegirlfriend`), Caged Boyfriend (`cageboyfriend`), Caged Wither Skeleton (`cagewitherskeleton`), Caged Ender Dragon (`cageenderdragon`), Caged Snow Golem (`cagesnowgolem`), Caged Iron Golem (`cageirongolem`), Caged Wither Boss (`cagewitherboss`), Caged Apple Cow (`cageredcow`), Caged Golden Apple Cow (`cagegoldcow`), Caged Enchanted Golden Apple Cow (`cageenchantedcow`), Caged MOTHRA (`cageMOTHRA`), Caged Alosaurus (`cagealosaurus`), Caged Cryosaurus (`cagecryolophosaurus`), Caged Camarasaurus (`cagecamarasaurus`), Caged Velocity Raptor (`cagevelocityraptor`), Caged Hydrolisc (`cagehydrolisc`), Caged Basilisk (`cagebasilisc`), Caged Dragonfly (`cagedragonfly`), Caged Emperor Scorpion (`cageemperorscorpion`), Caged Scorpion (`cagescorpion`), Caged Cave Fisher (`cagecavefisher`), Caged Baby Dragon (`cagespyro`), Caged Baryonyx (`cagebaryonyx`), Caged WTF? (`cagegammametroid`), Caged Bird (`cagecockateil`), Caged Kyuubi (`cagekyuubi`), Caged Alien (`cagealien`), Caged Attack Squid (`cageattacksquid`), Caged Water Dragon (`cagewaterdragon`), Caged Cephadrome (`cagecephadrome`), Caged Kraken (`cagekraken`), Caged Lizard (`cagelizard`), Caged Dragon (`cagedragon`), Caged Bee (`cagebee`), Caged Horse (`cagehorse`), Caged Firefly (`cagefirefly`), Caged Chipmunk (`cagechipmunk`), Caged Gazelle (`cagegazelle`), Caged Ostrich (`cageostrich`), Caged Jumpy Bug (`cagetrooper`), Caged Spit Bug (`cagespit`), Caged Stink Bug (`cagestink`), Caged Creeping Horror (`cagecreepinghorror`), Caged Terrible Terror (`cageterribleterror`), Caged Cliff Racer (`cagecliffracer`), Caged Triffid (`cagetriffid`), Caged Nightmare (`cagenightmare`), Caged Lurking Terror (`cagelurkingterror`), Caged Small Worm (`cagesmallworm`), Caged Medium Worm (`cagemediumworm`), Caged Large Worm (`cagelargeworm`), Caged Cassowary (`cagecassowary`), Caged Cloud Shark (`cagecloudshark`), Caged Gold Fish (`cagegoldfish`), Caged Leaf Monster (`cageleafmonster`), Caged Ender Knight (`cageenderknight`), Caged Ender Reaper (`cageenderreaper`), Caged Beaver (`cagebeaver`), Caged Crystal Urchin (`cageurchin`), Caged Flounder (`cageflounder`), Caged Skate (`cageskate`), Caged Rotator (`cagerotator`), Caged Peacock (`cagepeacock`), Caged Fairy (`cagefairy`), Caged Dungeon Beast (`cagedungeonbeast`), Caged Vortex (`cagevortex`), Caged Rat (`cagerat`), Caged Whale (`cagewhale`), Caged Irukandji (`cageirukandji`), Caged T. Rex (`cagetrex`), Caged Hercules Beetle (`cagehercules`), Caged Mantis (`cagemantis`), Caged Stinky (`cagestinky`), Caged Easter Bunny (`cageeasterbunny`), Caged CaterKiller (`cagecaterkiller`), Caged Molenoid (`cagemolenoid`), Caged Sea Monster (`cageseamonster`), Caged Sea Viper (`cageseaviper`), Caged Leonopteryx (`cageleon`), Caged Hammerhead (`cagehammerhead`), Caged Rubber Ducky (`cagerubberducky`), Caged Crystal Cow (`cagecrystalcow`), Caged Villager (`cagevillager`), Caged Criminal (`cagecriminal`), Caged Brutalfly (`cagebrutalfly`), Caged Nastysaurus (`cagenastysaurus`), Caged Pointysaurus (`cagepointysaurus`), Caged Cricket (`cagecricket`), Caged Frog (`cagefrog`), Caged Spider Driver (`cagespiderdriver`), Caged Crab (`cagecrab`)

**Item-derived** (90): Uranium Ingot (`ingoturanium`), Titanium Ingot (`ingottitanium`), Pink Tourmaline Ingot (`crystalpink_ingot`), Tiger's Eye Ingot (`tigerseye_ingot`), Pizza! (`pizza`), Duct Tape! (`ducttape`), Red Heels (`redheels`), Black Heels (`blackheels`), Slippers (`slippers`), Boots (`boots`), Game Controller (`gamecontroller`), The Ultimate Bow (`ultimatebow`), Skate String Bow (`skatebow`), The Ultimate Fishing Rod (`ultimatefishingrod`), Moth Scale (`mothscale`), The Queen Scale (`queenscale`), Nightmare Scale (`nightmarescale`), Emperor Scorpion Scale (`emperorscorpionscale`), Basilisk Scale (`basiliskscale`), Water Dragon Scale (`waterdragonscale`), Peacock Feather (`peacockfeather`), Jumpy Bug Scale (`jumpybugscale`), Kraken Tooth (`krakentooth`), Mobzilla Scale (`godzillascale`), Green Goo (`greengoo`), Spider Robot Kit (`spiderrobotkit`), Red Ant Robot Kit (`antrobotkit`), ZooKeeper Shard (`zookeeper`), Creeper Launcher (`creeperlauncher`), Nether Tracker (`netherlost`), Crystal Shards (`crystalsticks`), Sunspot Urchin (`sunspoturchin`), WaterDragon Charge (`waterball`), Robot Laser Charge (`laserball`), Ice Ball (`iceball`), Small Rock (`rocksmall`), Big Rock (`rock`), Flame Rock (`rockred`), Flame Crystal (`rockcrystalred`), Poison Crystal (`rockcrystalgreen`), Slowness Crystal (`rockcrystalblue`), Explosive Crystal (`rockcrystaltnt`), Poison Rock (`rockgreen`), Slowness Rock (`rockblue`), Weakness Rock (`rockpurple`), Painful Rock (`rockspikey`), Explosive Rock (`rocktnt`), A Freakin' Ray Gun! (`RayGun`), Thunder Staff (`thunderstaff`), Wrench (`wrench`), Acid (`acid`), Dead Irukandji (`deadirukandji`), Irukandji Arrow (`irukandjiarrow`), Sifter (`sifter`), SquidZooka! (`squidzookasmall`), Big Bertha Handle (`bbhandle`), Big Bertha Guard (`bbguard`), Big Bertha Blade (`bbblade`), Molenoid Nose (`molenoidnose`), Sea Monster Scale (`seamonsterscale`), Worm Tooth (`wormtooth`), TRex Tooth (`trextooth`), CaterKiller Jaws (`caterkillerjaw`), Sea Viper Tongue (`seavipertongue`), Vortex Eye (`vortexeye`), Salt (`salt`), Ruby (`ruby`), Amethyst (`amethyst`), Uranium Nugget (`uranium_nugget`), Titanium Nugget (`titanium_nugget`), Stairs going Up (`step_up`), Stairs going Down (`step_down`), Insta-Bridge (`step_accross`), Hoverboard (`elevator`), OMG! No! Don't do it!!! (`magicapple`), Miner's Dream; Miner's Dream (`minersdream`), Extra Small Zoo Cage (`zoo2`), Small Zoo Cage (`zoo4`), Medium Zoo Cage (`zoo6`), Large Zoo Cage (`zoo8`), Extra Large Zoo Cage (`zoo10`), Instant Survival Shelter (`instantshelter`), Instant Survival Garden (`instantgarden`), Random Dungeon (`randomdungeon`), Apple Tree Seed (`appletree_seed`), Experience Orb Catcher (`experiencecatcher`), Experience Tree Seed (`experiencetree_seed`), Dead Stink Bug (`deadstinkbug`), Cherry Pit (`cherrytree_seed`), Peach Pit (`peachtree_seed`)

**ItemAxe-derived** (8): The Ultimate Axe (`ultimateaxe`), Emerald Axe (`emeraldaxe`), Ruby Axe (`rubyaxe`), Amethyst Axe (`amethystaxe`), Crystal Wood Axe (`crystalwoodaxe`), Pink Tourmaline Axe (`crystalpinkaxe`), Kyanite Axe (`crystalstoneaxe`), Tiger's Eye Axe (`tigerseye_axe`)

**ItemFood-derived** (33): Fire Fish (`firefish`), Sun Fish (`sunfish`), Lava Eel (`lavaeel`), Spark Fish (`sparkfish`), Green Fish (`greenfish`), Blue Fish (`bluefish`), Pink Fish (`pinkfish`), Rock Fish (`rockfish`), Wood Fish (`woodfish`), Grey Fish (`greyfish`), Popcorn (`popcorn`), Buttered Popcorn (`popcorn_buttered`), Buttered and Salted Popcorn (`popcorn_buttered_salted`), Bag of Popcorn (`popcorn_bag`), Butter (`butter`), Corn Dog (`corndog_cooked`), Raw Corn Dog (`corndog_raw`), Butter Candy! (`buttercandy`), Bacon! (`cookedbacon`), Raw Bacon (`bacon`), Crab Meat! (`cookedcrabmeat`), Raw Crab Meat (`crabmeat`), Cheese (`cheese`), Garden Salad (`salad`), BLT Sandwich! (`blt_sandwich`), A Crabby Patty! (`crabbypatty`), Cooked Peacock (`cookedpeacock`), Raw Peacock (`rawpeacock`), Strawberry (`strawberry`), Cherries (`cherries`), Peach (`peach`), Crystal Apple (`crystalapple`), Love (`heart`)

**ItemHoe-derived** (8): The Ultimate Hoe (`ultimatehoe`), Emerald Hoe (`emeraldhoe`), Ruby Hoe (`rubyhoe`), Amethyst Hoe (`amethysthoe`), Crystal Wood Hoe (`crystalwoodhoe`), Pink Tourmaline Hoe (`crystalpinkhoe`), Kyanite Hoe (`crystalstonehoe`), Tiger's Eye Hoe (`tigerseye_hoe`)

**ItemPickaxe-derived** (8): The Ultimate Pickaxe (`ultimatepickaxe`), Emerald Pickaxe (`emeraldpickaxe`), Ruby Pickaxe (`rubypickaxe`), Amethyst Pickaxe (`amethystpickaxe`), Crystal Wood Pickaxe (`crystalwoodpickaxe`), Pink Tourmaline Pickaxe (`crystalpinkpickaxe`), Kyanite Pickaxe (`crystalstonepickaxe`), Tiger's Eye Pickaxe (`tigerseye_pickaxe`)

**ItemSeedFood-derived** (6): Radish (`radish`), Rice (`rice`), Corn (`corn_seed`), Quinoa (`quinoa`), Tomato (`tomato_seed`), Lettuce (`lettuce_seed`)

**ItemSeeds-derived** (5): Strawberry Plant (`strawberry_seed`), Butterfly Plant (`butterfly_seed`), Moth Plant (`moth_seed`), Mosquito Plant (`mosquito_seed`), Firefly Plant (`firefly_seed`)

**ItemSpade-derived** (4): The Ultimate Shovel (`ultimateshovel`), Emerald Shovel (`emeraldshovel`), Ruby Shovel (`rubyshovel`), Amethyst Shovel (`amethystshovel`)

**ItemSword-derived** (23): The Ultimate Sword (`ultimatesword`), Nightmare Sword (`nightmaresword`), Big Bertha (`berthasmall`), Slice (`slicesmall`), Royal Guardian Sword (`royalsmall`), Attitude Adjuster (`hammysmall`), Battle Axe (`battleaxesmall`), Chainsaw (`chainsawsmall`), Queen Scale Battle Axe (`queenbattleaxesmall`), Emerald Sword (`emeraldsword`), Experience Sword (`experiencesword`), Poison Sword (`poisonsword`), Rat Sword (`ratsword`), Fairy Sword (`fairysword`), Mantis Claw (`mantisclaw`), Big Hammer (`bighammer`), Ruby Sword (`rubysword`), Amethyst Sword (`amethystsword`), Crystal Wood Sword (`crystalwoodsword`), Pink Tourmaline Sword (`crystalpinksword`), Kyanite Sword (`crystalstonesword`), Tiger's Eye Sword (`tigerseye_sword`), Rose Sword (`rosesword`)

**ItemTool-derived** (4): Crystal Wood Shovel (`crystalwoodshovel`), Pink Tourmaline Shovel (`crystalpinkshovel`), Kyanite Shovel (`crystalstoneshovel`), Tiger's Eye Shovel (`tigerseye_shovel`)

**Spawn eggs (ItemSpawnEgg)** (114): Spawn Wither Skeleton (`eggwitherskeleton`), Spawn Ender Dragon (`eggenderdragon`), Spawn Snow Golem (`eggsnowgolem`), Spawn Iron Golem (`eggirongolem`), Spawn Wither Boss (`eggwitherboss`), Spawn Girlfriend (`egggirlfriend`), Spawn Apple Cow (`eggredcow`), Spawn Crystal Cow (`eggcrystalcow`), Spawn Golden Apple Cow (`egggoldcow`), Spawn Enchanted Golden Apple Cow (`eggenchantedcow`), Spawn MOTHRA! (`eggMOTHRA`), Spawn Alosaurus (`eggalosaurus`), Spawn Cryolophosaurus (`eggcryolophosaurus`), Spawn Camarasaurus (`eggcamarasaurus`), Spawn Velocity Raptor (`eggvelocityraptor`), Spawn Hydrolisc (`egghydrolisc`), Spawn Basilisk (`eggbasilisc`), Spawn Dragonfly (`eggdragonfly`), Spawn Emperor Scorpion! (`eggemperorscorpion`), Spawn Scorpion (`eggscorpion`), Spawn Cave Fisher (`eggcavefisher`), Spawn Baby Dragon (`eggspyro`), Spawn Baryonyx (`eggbaryonyx`), Spawn WTF? (`egggammametroid`), Spawn Bird (`eggcockateil`), Spawn Kyuubi (`eggkyuubi`), Spawn Alien (`eggalien`), Spawn Attack Squid (`eggattacksquid`), Spawn Water Dragon (`eggwaterdragon`), Spawn Cephadrome (`eggcephadrome`), Uh, no. Don't. (`eggkraken`), Spawn Lizard (`egglizard`), Spawn Dragon (`eggdragon`), Spawn Bee (`eggbee`), Spawn Jumpy Bug (`eggtrooper`), Spawn Spit Bug (`eggspit`), Spawn Stink Bug (`eggstink`), Spawn Ostrich (`eggostrich`), Spawn Gazelle (`egggazelle`), Spawn Chipmunk (`eggchipmunk`), Spawn Creeping Horror (`eggcreepinghorror`), Spawn Terrible Terror (`eggterribleterror`), Spawn Cliff Racer (`eggcliffracer`), Spawn Triffid (`eggtriffid`), Spawn Nightmare!!! (`eggnightmare`), Spawn Lurking Terror (`egglurkingterror`), Spawn Mobzilla (`egggodzilla`), Spawn Small Worm (`eggsmallworm`), Spawn Medium Worm (`eggmediumworm`), Spawn Large Worm (`egglargeworm`), Spawn Cassowary (`eggcassowary`), Spawn Cloud Shark (`eggcloudshark`), Spawn Gold Fish (`egggoldfish`), Spawn Leaf Monster (`eggleafmonster`), Spawn T-Shirt! (`eggtshirt`), Spawn Ender Knight (`eggenderknight`), Spawn Ender Reaper (`eggenderreaper`), Spawn Beaver (`eggbeaver`), Spawn Rotator (`eggrotator`), Spawn Vortex (`eggvortex`), Spawn Peacock (`eggpeacock`), Spawn Fairy (`eggfairy`), Spawn Dungeon Beast (`eggdungeonbeast`), Spawn Rat (`eggrat`), Spawn Flounder (`eggflounder`), Spawn Whale (`eggwhale`), Spawn Irukandji (`eggirukandji`), Spawn Skate (`eggskate`), Spawn Crystal Urchin (`eggurchin`), Spawn Bomb-Omb (`eggrobot1`), Spawn Robo-Pounder (`eggrobot2`), Spawn Robo-Gunner (`eggrobot3`), Spawn Robo-Warrior (`eggrobot4`), Spawn Ghost (`eggghost`), Spawn Ghost Pumpkin Skelly (`eggghostskelly`), Spawn Brown Ant (`eggbrownant`), Spawn Red Ant (`eggredant`), Spawn Rainbow Ant (`eggrainbowant`), Spawn Unstable Ant (`eggunstableant`), Spawn Termite (`eggtermite`), Spawn Butterfly (`eggbutterfly`), Spawn Moth (`eggmoth`), Spawn Mosquito (`eggmosquito`), Spawn Firefly (`eggfirefly`), Spawn T. Rex (`eggtrex`), Spawn Hercules Beetle (`egghercules`), Spawn Mantis (`eggmantis`), Spawn Stinky (`eggstinky`), Spawn Robo-Sniper (`eggrobot5`), Spawn Coin (`eggcoin`), Spawn Boyfriend (`eggboyfriend`), Spawn The King (`eggtheking`), Spawn The Queen (`eggthequeen`), Spawn The Prince (`eggtheprince`), Spawn Easter Bunny (`eggeasterbunny`), Spawn Molenoid (`eggmolenoid`), Spawn Sea Monster (`eggseamonster`), Spawn Sea Viper (`eggseaviper`), Spawn CaterKiller (`eggcaterkiller`), Spawn Rubber Ducky (`eggrubberducky`), Spawn Hammerhead (`egghammerhead`), Spawn Leonopteryx (`eggleon`), Spawn Criminal (`eggcriminal`), Spawn Brutalfly (`eggbrutalfly`), Spawn Nastysaurus (`eggnastysaurus`), Spawn Pointysaurus (`eggpointysaurus`), Spawn Cricket (`eggcricket`), Spawn The Princess (`eggtheprincess`), Spawn Frog (`eggfrog`), Spawn Jeffery (`eggrobot6`), Spawn Red Ant Robot (`eggantrobot`), Spawn Giant Spider Robot (`eggspiderrobot`), Spawn Spider Robot Driver (`eggspiderdriver`), Spawn Crab (`eggcrab`)

## 6. Blocks (211)

Block IDs are `BaseBlockID (config default 2700) + n`; all 211 block textures resolved in `assets/orespawn/textures/blocks/`. Full table: `items_blocks.txt`.

**Block-derived** (38): Uranium Ore (`oreuranium`), Titanium Ore (`oretitanium`), Uranium Block (`blockuranium`), Titanium Block (`blocktitanium`), Mobzilla Scale Block (`blockmobzillascale`), Lava Foam (`lavafoam`), Ruby Block (`blockruby`), Amethyst Block (`blockamethyst`), Pink Tourmaline Block (`crystalpink_block`), Tiger's Eye Block (`tigerseye_block`), Pizza! (`pizza`), Duct Tape! (`ducttape`), Salt Ore (`oresalt`), Ruby Ore (`oreruby`), Amethyst Ore (`oreamethyst`), Kyanite (`crystalstone`), Crystal Energy (`crystalcoal`), Crystal Grass (`crystalgrass`), Pink Tourmaline (`crystalcrystal`), Tiger's Eye (`tigerseye`), Crystal Planks (`crystalplanks`), Crystalized Rats (`crystalrat`), Crystalized Fairies (`crystalfairy`), Red Ant Troll Block (`redanttroll`), Termite Troll Block (`termitetroll`), Random Teleport Block (`blockteleport`), Molenoid Dirt (`moledirt`), Sky Tree Wood (`skytreelog`), Duplicator Tree Wood (`duplicatortreelog`), Pink Flower (`flower_pink`), Blue Flower (`flower_blue`), Black Flower (`flower_black`), Dead Flower (`flower_scary`), Red Crystal Flower (`crystalflower_red`), Green Crystal Flower (`crystalflower_green`), Blue Crystal Flower (`crystalflower_blue`), Yellow Crystal Flower (`crystalflower_yellow`), Crystal Termite Nest (`CrystalTermiteBlock`)

**Mob "egg" ore blocks (OreGenericEgg)** (121): Ender-Pearl Block (`blockenderpearl`), Eye-of-Ender Block (`blockeyeofender`), Ancient Dried Spider Spawn Egg (`orespider`), Ancient Dried Bat Spawn Egg (`orebat`), Ancient Dried Cow Spawn Egg (`orecow`), Ancient Dried Pig Spawn Egg (`orepig`), Ancient Dried Squid Spawn Egg (`oresquid`), Ancient Dried Chicken Spawn Egg (`orechicken`), Ancient Dried Creeper Spawn Egg (`orecreeper`), Ancient Dried Skeleton Spawn Egg (`oreskeleton`), Ancient Dried Zombie Spawn Egg (`orezombie`), Ancient Dried Slime Spawn Egg (`oreslime`), Ancient Dried Ghast Spawn Egg (`oreghast`), Ancient Dried Zombie Pigman Spawn Egg (`orezombiepigman`), Ancient Dried Enderman Spawn Egg (`oreenderman`), Ancient Dried Cave Spider Spawn Egg (`orecavespider`), Ancient Dried Silverfish Spawn Egg (`oresilverfish`), Ancient Dried Magma Cube Spawn Egg (`oremagmacube`), Ancient Dried Witch Spawn Egg (`orewitch`), Ancient Dried Sheep Spawn Egg (`oresheep`), Ancient Dried Wolf Spawn Egg (`orewolf`), Ancient Dried Mooshroom Spawn Egg (`oremooshroom`), Ancient Dried Ocelot Spawn Egg (`oreocelot`), Ancient Dried Blaze Spawn Egg (`oreblaze`), Ancient Dried Wither Skeleton Spawn Egg (`orewitherskeleton`), Ancient Dried Ender Dragon Spawn Egg (`oreenderdragon`), Ancient Dried Snow Golem Spawn Egg (`oresnowgolem`), Ancient Dried Iron Golem Spawn Egg (`oreirongolem`), Ancient Dried Wither Boss Spawn Egg (`orewitherboss`), Ancient Dried Girlfriend Spawn Egg (`oregirlfriend`), Ancient Dried Boyfriend Spawn Egg (`oreboyfriend`), Ancient Dried Apple Cow Spawn Egg (`oreredcow`), Ancient Dried Crystal Cow Spawn Egg (`orecrystalcow`), Ancient Dried Villager Spawn Egg (`orevillager`), Ancient Dried Golden Apple Cow Spawn Egg (`oregoldcow`), Ancient Dried Enchanted Golden Apple Cow Spawn Egg (`oreenchantedcow`), Ancient Dried MOTHRA Spawn Egg (`oreMOTHRA`), Ancient Dried Alosaurus Spawn Egg (`orealosaurus`), Ancient Dried Cryolophosaurus Spawn Egg (`orecryolophosaurus`), Ancient Dried Camarasaurus Spawn Egg (`orecamarasaurus`), Ancient Dried Velocity Raptor Spawn Egg (`orevelocityraptor`), Ancient Dried Hydrolisc Spawn Egg (`orehydrolisc`), Ancient Dried Basilisk Spawn Egg (`orebasilisc`), Ancient Dried Dragonfly Spawn Egg (`oredragonfly`), Ancient Dried Emperor Scorpion Spawn Egg (`oreemperorscorpion`), Ancient Dried Scorpion Spawn Egg (`orescorpion`), Ancient Dried Cave Fisher Spawn Egg (`orecavefisher`), Ancient Dried Baby Dragon Spawn Egg (`orespyro`), Ancient Dried Baryonyx Spawn Egg (`orebaryonyx`), Ancient Dried WTF? Spawn Egg (`oregammametroid`), Ancient Dried Bird Spawn Egg (`orecockateil`), Ancient Dried Kyuubi Spawn Egg (`orekyuubi`), Ancient Dried Alien Spawn Egg (`orealien`), Ancient Dried Attack Squid Spawn Egg (`oreattacksquid`), Ancient Dried WaterDragon Spawn Egg (`orewaterdragon`), Ancient Dried Cephadrome Spawn Egg (`orecephadrome`), Ancient Dried Dragon Spawn Egg (`oredragon`), Ancient Dried Kraken Spawn Egg (`orekraken`), Ancient Dried Lizard Spawn Egg (`orelizard`), Ancient Dried Bee Spawn Egg (`orebee`), Ancient Dried Horse Spawn Egg (`orehorse`), Ancient Dried Jumpy Bug Spawn Egg (`oretrooper`), Ancient Dried Spit Bug Spawn Egg (`orespit`), Ancient Dried Stink Bug Spawn Egg (`orestink`), Ancient Dried Ostrich Spawn Egg (`oreostrich`), Ancient Dried Gazelle Spawn Egg (`oregazelle`), Ancient Dried Chipmunk Spawn Egg (`orechipmunk`), Ancient Dried Creeping Horror Spawn Egg (`orecreepinghorror`), Ancient Dried Terrible Terror Spawn Egg (`oreterribleterror`), Ancient Dried Cliff Racer Spawn Egg (`orecliffracer`), Ancient Dried Triffid Spawn Egg (`oretriffid`), Ancient Dried Nightmare Spawn Egg (`orenightmare`), Ancient Dried Lurking Terror Spawn Egg (`orelurkingterror`), Ancient Dried Mobzilla Spawn Egg Part (`oregodzillapart`), Ancient Dried Mobzilla Spawn Egg (`oregodzilla`), Ancient Dried Small Worm Spawn Egg (`oresmallworm`), Ancient Dried Medium Worm Spawn Egg (`oremediumworm`), Ancient Dried Large Worm Spawn Egg (`orelargeworm`), Ancient Dried Cassowary Spawn Egg (`orecassowary`), Ancient Dried Cloud Shark Spawn Egg (`orecloudshark`), Ancient Dried Gold Fish Spawn Egg (`oregoldfish`), Ancient Dried Leaf Monster Spawn Egg (`oreleafmonster`), Ancient Dried T-Shirt Spawn Egg (`oretshirt`), Ancient Dried Ender Knight Spawn Egg (`oreenderknight`), Ancient Dried Ender Reaper Spawn Egg (`oreenderreaper`), Ancient Dried Beaver Spawn Egg (`orebeaver`), Ancient Dried Urchin Spawn Egg (`oreurchin`), Ancient Dried Flounder Spawn Egg (`oreflounder`), Ancient Dried Skate Spawn Egg (`oreskate`), Ancient Dried Rotator Spawn Egg (`orerotator`), Ancient Dried Peacock Spawn Egg (`orepeacock`), Ancient Dried Fairy Spawn Egg (`orefairy`), Ancient Dried Dungeon Beast Spawn Egg (`oredungeonbeast`), Ancient Dried Vortex Spawn Egg (`orevortex`), Ancient Dried Rat Spawn Egg (`orerat`), Ancient Dried Whale Spawn Egg (`orewhale`), Ancient Dried Irukandji Spawn Egg (`oreirukandji`), Ancient Dried T. Rex Spawn Egg (`oretrex`), Ancient Dried Hercules Beetle Spawn Egg (`orehercules`), Ancient Dried Mantis Spawn Egg (`oremantis`), Ancient Dried Stinky Spawn Egg (`orestinky`), Ancient Dried The King Spawn Egg Part (`orethekingpart`), Ancient Dried The King Spawn Egg (`oretheking`), Ancient Dried The Queen Spawn Egg Part (`orethequeenpart`), Ancient Dried The Queen Spawn Egg (`orethequeen`), Ancient Dried Easter Bunny Spawn Egg (`oreeasterbunny`), Ancient Dried CaterKiller Spawn Egg (`orecaterkiller`), Ancient Dried Molenoid Spawn Egg (`oremolenoid`), Ancient Dried Sea Monster Spawn Egg (`oreseamonster`), Ancient Dried Sea Viper Spawn Egg (`oreseaviper`), Ancient Dried Leonopteryx Spawn Egg (`oreleon`), Ancient Dried Hammerhead Spawn Egg (`orehammerhead`), Ancient Dried Rubber Ducky Spawn Egg (`orerubberducky`), Ancient Dried Criminal Spawn Egg (`orecriminal`), Ancient Dried Brutalfly Spawn Egg (`orebrutalfly`), Ancient Dried Nastysaurus Spawn Egg (`orenastysaurus`), Ancient Dried Pointysaurus Spawn Egg (`orepointysaurus`), Ancient Dried Cricket Spawn Egg (`orecricket`), Ancient Dried Frog Spawn Egg (`orefrog`), Ancient Dried Spider Driver Spawn Egg (`orespiderdriver`), Ancient Dried Crab Spawn Egg (`orecrab`)

**BlockContainer-derived** (2): Crystal Furnace (`crystalfurnace`), Crystal Furnace (`crystalfurnace`)

**BlockCrops-derived** (7): (no addNameForObject) (`None`), (no addNameForObject) (`None`), (no addNameForObject) (`None`), (no addNameForObject) (`None`), (no addNameForObject) (`None`), (no addNameForObject) (`None`), (no addNameForObject) (`None`)

**BlockGrass-derived** (5): Ant Nest (`AntBlock`), Red Ant Nest (`RedAntBlock`), Termite Nest (`TermiteBlock`), Rainbow Ant Nest (`RainbowAntBlock`), Unstable Ant Nest (`UnstableAntBlock`)

**BlockLeaves-derived** (8): Apple Tree Leaves (`leaves_apple`), Experience Tree Leaves (`leaves_experience`), Scary Tree Leaves (`leaves_scary`), Cherry Tree Leaves (`leaves_cherry`), Peach Tree Leaves (`leaves_peach`), Crystal Tree Leaves (`crystaltreeleaves`), Crystal Tree Leaves (`crystaltreeleaves2`), Crystal Tree Leaves (`crystaltreeleaves3`)

**BlockReed-derived** (24): Corn Plant (`corn_0`), Corn Plant (`corn_1`), Corn Plant (`corn_2`), Corn Plant (`corn_3`), Quinoa Plant (`quinoa_0`), Quinoa Plant (`quinoa_1`), Quinoa Plant (`quinoa_2`), Quinoa Plant (`quinoa_3`), Tomato Plant (`tomato_0`), Tomato Plant (`tomato_1`), Tomato Plant (`tomato_2`), Tomato Plant (`tomato_3`), Lettuce Plant (`lettuce_0`), Lettuce Plant (`lettuce_1`), Lettuce Plant (`lettuce_2`), Lettuce Plant (`lettuce_3`), Island Block (`island`), The King Spawner Block (`kingspawner`), The Queen Spawner Block (`queenspawner`), Random Dungeon Spawner (`dungeonspawner`), Experience Tree Sapling (`experiencesapling`), Red Crystal Tree Sapling (`crystalsapling`), Yellow Crystal Tree Sapling (`crystalsapling2`), Blue Crystal Tree Sapling (`crystalsapling3`)

**BlockRotatedPillar-derived** (1): Crystal Tree Wood (`crystaltreelog`)

**BlockTorch-derived** (4): Extreme Torch (`extremetorch`), Kraken Repellent (`krakenrepellent`), Creeper Repellent (`creeperrepellent`), Crystal Torch (`crystaltorch`)

**BlockWorkbench-derived** (1): Crystal Workbench (`crystalworkbench`)

## 7. Tool materials (`EnumHelper.addToolMaterial`)

Arguments come from `OreSpawnMain.<x>_stats` (`WeaponStats`), which `get_weaponstats(cfg, "OreSpawnWEAPONS", <prefix>, harvest, maxUses, efficiency, damage, enchantability)` fills from config; values below are the literal defaults at the call site.

| material | config prefix | harvestLevel | maxUses | efficiency | damage | enchantability |
|---|---|---|---|---|---|---|
| ULTIMATE | Ultimate | 10 | 3000 | 15 | 36 | 100 |
| NIGHTMARE | Nightmare | 3 | 1800 | 12 | 26 | 60 |
| REALEMERALD | Emerald | 3 | 1300 | 10 | 6 | 75 |
| RUBY | Ruby | 5 | 1500 | 11 | 16 | 85 |
| AMETHYST | Amethyst | 4 | 2000 | 11 | 11 | 70 |
| BERTHA | Bertha | 3 | 9000 | 15 | 496 | 100 |
| CRYSTALWOOD | CrystalWood | 2 | 300 | 3 | 2 | 15 |
| CRYSTALSTONE | CrystalStone | 3 | 800 | 6 | 5 | 45 |
| CRYSTALPINK | Pink | 4 | 1100 | 10 | 7 | 65 |
| TIGERSEYE | TigersEye | 4 | 1600 | 12 | 8 | 75 |
| ROYAL | Royal | 3 | 10000 | 15 | 746 | 150 |
| HAMMY | Attitude | 5 | 2000 | 15 | 82 | 100 |
| BATTLE | BattleAxe | 3 | 1500 | 15 | 46 | 75 |
| CHAINSAW | Chainsaw | 3 | 1500 | 10 | 56 | 75 |
| QUEENBATTLE | QueenBattleAxe | 3 | 2200 | 15 | 662 | 100 |

## 8. Armor materials (`EnumHelper.addArmorMaterial`)

From `get_armorstats(cfg, <prefix>, durability, head, chest, legs, boots, enchantability, respiration, aquaaffinity, protection, fireprotection, blastprotection, projectileprotection, unbreaking, featherfalling)`, category `OreSpawnARMOR`. Layer textures from `ItemOreSpawnArmor.getArmorTexture`: `orespawn:<prefix>_1.png` / `_2.png` (asset root, not `textures/models/armor`).

| material | durability | reductions (head, chest, legs, boots) | enchantability | auto-enchant levels (resp, aqua, prot, fire, blast, proj, unbr, feather) | layer PNGs |
|---|---|---|---|---|---|
| ULTIMATE | 200 | 6, 12, 10, 6 | 100 | 2, 3, 5, 5, 5, 5, 0, 3 | `ultimate_1.png` [64, 32], `ultimate_2.png` [64, 32] |
| MOBZILLA | 1000 | 7, 13, 11, 7 | 150 | 0, 0, 10, 10, 10, 10, 5, 10 | `mobzilla_1.png` [64, 32], `mobzilla_2.png` [64, 32] |
| LAVAEEL | 40 | 2, 7, 5, 2 | 35 | 1, 2, 3, 2, 10, 0, 0, 2 | `lavaeel_1.png` [64, 32], `lavaeel_2.png` [64, 32] |
| MOTHSCALE | 50 | 2, 7, 5, 2 | 50 | 0, 0, 3, 3, 3, 0, 0, 5 | `mothscale_1.png` [64, 32], `mothscale_2.png` [64, 32] |
| EMERALD | 60 | 3, 8, 6, 3 | 40 | 0, 0, 0, 0, 0, 0, 0, 0 | `emerald_1.png` [64, 32], `emerald_2.png` [64, 32] |
| EXPERIENCE | 70 | 5, 9, 7, 4 | 50 | 0, 0, 2, 0, 1, 0, 0, 1 | `experience_1.png` [64, 32], `experience_2.png` [64, 32] |
| RUBY | 90 | 4, 9, 8, 4 | 40 | 0, 0, 0, 0, 0, 0, 0, 0 | `ruby_1.png` [64, 32], `ruby_2.png` [64, 32] |
| AMETHYST | 100 | 4, 8, 7, 3 | 40 | 0, 0, 0, 0, 0, 0, 0, 0 | `amethyst_1.png` [64, 32], `amethyst_2.png` [64, 32] |
| PINK | 50 | 3, 7, 5, 2 | 40 | 0, 0, 0, 0, 0, 0, 0, 0 | `pink_1.png` [64, 32], `pink_2.png` [64, 32] |
| TIGERSEYE | 80 | 4, 8, 7, 4 | 55 | 0, 0, 0, 0, 0, 0, 0, 0 | `tigerseye_1.png` [64, 32], `tigerseye_2.png` [64, 32] |
| PEACOCK | 40 | 2, 5, 4, 2 | 30 | 0, 0, 0, 0, 0, 0, 0, 10 | `peacock_1.png` [64, 32], `peacock_2.png` [64, 32] |
| ROYAL | 2000 | 8, 14, 12, 8 | 200 | 1, 2, 10, 10, 10, 10, 5, 10 | `royal_1.png` [64, 32], `royal_2.png` [64, 32] |
| LAPIS | 60 | 2, 7, 5, 2 | 60 | 1, 1, 1, 0, 0, 1, 0, 0 | `lapis_1.png` [64, 32], `lapis_2.png` [64, 32] |
| QUEEN | 1500 | 9, 16, 14, 9 | 150 | 0, 0, 0, 0, 0, 0, 0, 0 | `queen_1.png` [64, 32], `queen_2.png` [64, 32] |

56 armor items (`ItemOreSpawnArmor(id, material, renderIndex = proxy.setArmorPrefix(prefix), slot)`), 4 per material.

## 9. Dimensions, WorldProviders, biomes

`DimensionID = BaseDimensionID` (config default 80), `DimensionID2..6 = Base + 1..5`; biome IDs `BiomeUtopiaID = BaseBiomeID` (default 120), `BiomeIslandsID +1`, `BiomeCrystalID +2`, `BiomeVillageID +3`, `BiomeChaosID +4`. Every provider is registered with `registerProviderType(id, class, true)` and `registerDimension(id, id)`.

| provider | getDimensionName | dimension id (default) | chunk provider | world chunk manager | biome |
|---|---|---|---|---|---|
| WorldProviderOreSpawn | Dimension-Utopia | 80 | ChunkProviderOreSpawn | WorldChunkManagerHell(this.MyPlains, 0.5) | BiomeGenUtopianPlains(BiomeUtopiaID), colour 353825 |
| WorldProviderOreSpawn2 | Dimension-Extreme | 81 | ChunkProviderOreSpawn2 | WorldChunkManagerHell(BiomeGenBase.extremeHills, 0.009999999776482582) | vanilla (see chunk manager) |
| WorldProviderOreSpawn3 | Dimension-VillageMania | 82 | ChunkProviderOreSpawn3 | WorldChunkManagerHell(this.MyPlains, 0.5) | BiomeGenUtopianPlains(BiomeVillageID), colour 353825 |
| WorldProviderOreSpawn4 | Dimension-Islands | 83 | ChunkProviderOreSpawn4 | WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) | BiomeGenUtopianPlains(BiomeIslandsID), colour 353825 |
| WorldProviderOreSpawn5 | Dimension-Crystal | 84 | ChunkProviderOreSpawn5 | BiomeGenBase$Height(0.10000000149011612, 0.5), WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) | BiomeGenUtopianPlains(BiomeCrystalID), colour 353825 |
| WorldProviderOreSpawn6 | Dimension-Chaos | 85 | ChunkProviderOreSpawn6 | WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) | BiomeGenUtopianPlains(BiomeChaosID), colour 353825 |

`WorldProviderOreSpawn6` assigns `dimensionId` twice in `registerWorldChunkManager` (first `DimensionID4`, then `DimensionID6`); only one custom biome class exists (`BiomeGenUtopianPlains`), instantiated per provider with a different biome ID.

## 10. Config keys (628)

All `Configuration.get(category, key, default)` calls reachable from `OreSpawnMain.preInit`, helper calls expanded with their call-site literals. No call passes a comment string, so the comment column is always empty. Helper-backed values are clamped after reading (see notes below the table). Raw: `config_dump.txt`.

| category | key | default | type | comment | stored in |
|---|---|---|---|---|---|
| OreSpawnIDS | BaseBlockID | 2700 | I | - | OreSpawnMain.BaseBlockID |
| OreSpawnIDS | BaseItemID | 9000 | I | - | OreSpawnMain.BaseItemID |
| OreSpawnIDS | BaseBiomeID | 120 | I | - | OreSpawnMain.BaseBiomeID |
| OreSpawnIDS | BaseDimensionID | 80 | I | - | OreSpawnMain.BaseDimensionID |
| OreSpawnTWEAKS | AllMobsDisable | 0 | I | - | OreSpawnMain.AllMobsDisable |
| OreSpawnTWEAKS | LessOre | 0 | I | - | OreSpawnMain.LessOre |
| OreSpawnTWEAKS | LessLag | 0 | I | - | OreSpawnMain.LessLag |
| OreSpawnTWEAKS | RatPlayerFriendly | 1 | I | - | OreSpawnMain.RatPlayerFriendly |
| OreSpawnTWEAKS | RatPetFriendly | 1 | I | - | OreSpawnMain.RatPetFriendly |
| OreSpawnTWEAKS | NightmareSize | 0 | I | - | OreSpawnMain.NightmareSize |
| OreSpawnTWEAKS | IslandSpeedFactor | 2 | I | - | OreSpawnMain.IslandSpeedFactor |
| OreSpawnTWEAKS | IslandSizeFactor | 2 | I | - | OreSpawnMain.IslandSizeFactor |
| OreSpawnTWEAKS | GinormousEmeraldTreeEnable | 1 | I | - | OreSpawnMain.GinormousEmeraldTreeEnable |
| OreSpawnTWEAKS | GuiOverlayEnable | 1 | I | - | OreSpawnMain.GuiOverlayEnable |
| OreSpawnTWEAKS | UltimateSwordPvp | 0 | I | - | OreSpawnMain.ultimate_sword_pvp |
| OreSpawnTWEAKS | BigBerthaPvp | 0 | I | - | OreSpawnMain.big_bertha_pvp |
| OreSpawnTWEAKS | BoyfriendBroMode | 0 | I | - | OreSpawnMain.bro_mode |
| OreSpawnTWEAKS | DuplicatorTreeEnable | 1 | I | - | OreSpawnMain.enableduplicatortree |
| OreSpawnTWEAKS | RoyalGlideEnable | 1 | I | - | OreSpawnMain.RoyalGlideEnable |
| OreSpawnTWEAKS | DragonflyHorseFriendly | 0 | I | - | OreSpawnMain.DragonflyHorseFriendly |
| OreSpawnTWEAKS | PlayNicely | 0 | I | - | OreSpawnMain.PlayNicely |
| OreSpawnTWEAKS | MinersDreamExpensive | 0 | I | - | OreSpawnMain.MinersDreamExpensive |
| OreSpawnTWEAKS | DisableOverworldDungeons | 0 | I | - | OreSpawnMain.DisableOverworldDungeons |
| OreSpawnTWEAKS | FullPowerKingEnable | 0 | I | - | OreSpawnMain.FullPowerKingEnable |
| OreSpawnWEAPONS | UltimateSwordEnchantmentLevel | 5 | I | - | OreSpawnMain.UltimateSwordMagic |
| OreSpawnWEAPONS | UltimateBowDamage | 10 | I | - | OreSpawnMain.UltimateBowDamage |
| OreSpawnMOBS | MosquitoEnable | 1 | I | - | OreSpawnMain.MosquitoEnable |
| OreSpawnMOBS | RockEnable | 1 | I | - | OreSpawnMain.RockEnable |
| OreSpawnMOBS | GhostEnable | 1 | I | - | OreSpawnMain.GhostEnable |
| OreSpawnMOBS | GhostSkellyEnable | 1 | I | - | OreSpawnMain.GhostSkellyEnable |
| OreSpawnMOBS | SpiderDriverEnable | 1 | I | - | OreSpawnMain.SpiderDriverEnable |
| OreSpawnMOBS | JefferyEnable | 1 | I | - | OreSpawnMain.JefferyEnable |
| OreSpawnMOBS | MothraEnable | 1 | I | - | OreSpawnMain.MothraEnable |
| OreSpawnMOBS | BrutalflyEnable | 1 | I | - | OreSpawnMain.BrutalflyEnable |
| OreSpawnMOBS | NastysaurusEnable | 1 | I | - | OreSpawnMain.NastysaurusEnable |
| OreSpawnMOBS | PointysaurusEnable | 1 | I | - | OreSpawnMain.PointysaurusEnable |
| OreSpawnMOBS | CricketEnable | 1 | I | - | OreSpawnMain.CricketEnable |
| OreSpawnMOBS | FrogEnable | 1 | I | - | OreSpawnMain.FrogEnable |
| OreSpawnMOBS | MothraPeaceful | 0 | I | - | OreSpawnMain.MothraPeaceful |
| OreSpawnMOBS | BlackAntEnable | 1 | I | - | OreSpawnMain.BlackAntEnable |
| OreSpawnMOBS | RedAntEnable | 1 | I | - | OreSpawnMain.RedAntEnable |
| OreSpawnMOBS | TermiteEnable | 1 | I | - | OreSpawnMain.TermiteEnable |
| OreSpawnMOBS | UnstableAntEnable | 1 | I | - | OreSpawnMain.UnstableAntEnable |
| OreSpawnMOBS | RainbowedAntEnable | 1 | I | - | OreSpawnMain.RainbowAntEnable |
| OreSpawnMOBS | AlosaurusEnable | 1 | I | - | OreSpawnMain.AlosaurusEnable |
| OreSpawnMOBS | HammerheadEnable | 1 | I | - | OreSpawnMain.HammerheadEnable |
| OreSpawnMOBS | LeonEnable | 1 | I | - | OreSpawnMain.LeonEnable |
| OreSpawnMOBS | CaterKillerEnable | 1 | I | - | OreSpawnMain.CaterKillerEnable |
| OreSpawnMOBS | MolenoidEnable | 1 | I | - | OreSpawnMain.MolenoidEnable |
| OreSpawnMOBS | TRexEnable | 1 | I | - | OreSpawnMain.TRexEnable |
| OreSpawnMOBS | CriminalEnable | 1 | I | - | OreSpawnMain.CriminalEnable |
| OreSpawnMOBS | CryolophosaurusEnable | 1 | I | - | OreSpawnMain.CryolophosaurusEnable |
| OreSpawnMOBS | RatEnable | 1 | I | - | OreSpawnMain.RatEnable |
| OreSpawnMOBS | UrchinEnable | 1 | I | - | OreSpawnMain.UrchinEnable |
| OreSpawnMOBS | CamarasaurusEnable | 1 | I | - | OreSpawnMain.CamarasaurusEnable |
| OreSpawnMOBS | VelocityRaptorEnable | 1 | I | - | OreSpawnMain.VelocityRaptorEnable |
| OreSpawnMOBS | HydroliscEnable | 1 | I | - | OreSpawnMain.HydroliscEnable |
| OreSpawnMOBS | SpyroEnable | 1 | I | - | OreSpawnMain.SpyroEnable |
| OreSpawnMOBS | BaryonyxEnable | 1 | I | - | OreSpawnMain.BaryonyxEnable |
| OreSpawnMOBS | BirdEnable | 1 | I | - | OreSpawnMain.CockateilEnable |
| OreSpawnMOBS | CassowaryEnable | 1 | I | - | OreSpawnMain.CassowaryEnable |
| OreSpawnMOBS | EasterBunnyEnable | 1 | I | - | OreSpawnMain.EasterBunnyEnable |
| OreSpawnMOBS | PeacockEnable | 1 | I | - | OreSpawnMain.PeacockEnable |
| OreSpawnMOBS | KyuubiEnable | 1 | I | - | OreSpawnMain.KyuubiEnable |
| OreSpawnMOBS | CephadromeEnable | 1 | I | - | OreSpawnMain.CephadromeEnable |
| OreSpawnMOBS | DragonEnable | 1 | I | - | OreSpawnMain.DragonEnable |
| OreSpawnMOBS | GammaMetroidEnable | 1 | I | - | OreSpawnMain.GammaMetroidEnable |
| OreSpawnMOBS | BasiliskEnable | 1 | I | - | OreSpawnMain.BasiliskEnable |
| OreSpawnMOBS | DragonflyEnable | 1 | I | - | OreSpawnMain.DragonflyEnable |
| OreSpawnMOBS | EmperorScorpionEnable | 1 | I | - | OreSpawnMain.EmperorScorpionEnable |
| OreSpawnMOBS | TrooperBugEnable | 1 | I | - | OreSpawnMain.TrooperBugEnable |
| OreSpawnMOBS | SpitBugEnable | 1 | I | - | OreSpawnMain.SpitBugEnable |
| OreSpawnMOBS | StinkBugEnable | 1 | I | - | OreSpawnMain.StinkBugEnable |
| OreSpawnMOBS | ScorpionEnable | 1 | I | - | OreSpawnMain.ScorpionEnable |
| OreSpawnMOBS | CaveFisherEnable | 1 | I | - | OreSpawnMain.CaveFisherEnable |
| OreSpawnMOBS | AlienEnable | 1 | I | - | OreSpawnMain.AlienEnable |
| OreSpawnMOBS | WaterDragonEnable | 1 | I | - | OreSpawnMain.WaterDragonEnable |
| OreSpawnMOBS | SeaMonsterEnable | 1 | I | - | OreSpawnMain.SeaMonsterEnable |
| OreSpawnMOBS | SeaViperEnable | 1 | I | - | OreSpawnMain.SeaViperEnable |
| OreSpawnMOBS | AttackSquidEnable | 1 | I | - | OreSpawnMain.AttackSquidEnable |
| OreSpawnMOBS | Robot1Enable | 1 | I | - | OreSpawnMain.Robot1Enable |
| OreSpawnMOBS | Robot2Enable | 1 | I | - | OreSpawnMain.Robot2Enable |
| OreSpawnMOBS | Robot3Enable | 1 | I | - | OreSpawnMain.Robot3Enable |
| OreSpawnMOBS | Robot4Enable | 1 | I | - | OreSpawnMain.Robot4Enable |
| OreSpawnMOBS | Robot5Enable | 1 | I | - | OreSpawnMain.Robot5Enable |
| OreSpawnMOBS | RotatorEnable | 1 | I | - | OreSpawnMain.RotatorEnable |
| OreSpawnMOBS | VortexEnable | 1 | I | - | OreSpawnMain.VortexEnable |
| OreSpawnMOBS | DungeonBeastEnable | 1 | I | - | OreSpawnMain.DungeonBeastEnable |
| OreSpawnMOBS | KrakenEnable | 1 | I | - | OreSpawnMain.KrakenEnable |
| OreSpawnMOBS | LizardEnable | 1 | I | - | OreSpawnMain.LizardEnable |
| OreSpawnMOBS | RubberDuckyEnable | 1 | I | - | OreSpawnMain.RubberDuckyEnable |
| OreSpawnMOBS | GirlfriendEnable | 1 | I | - | OreSpawnMain.GirlfriendEnable |
| OreSpawnMOBS | BoyfriendEnable | 0 | I | - | OreSpawnMain.BoyfriendEnable |
| OreSpawnMOBS | FireflyEnable | 1 | I | - | OreSpawnMain.FireflyEnable |
| OreSpawnMOBS | FairyEnable | 1 | I | - | OreSpawnMain.FairyEnable |
| OreSpawnMOBS | BeeEnable | 1 | I | - | OreSpawnMain.BeeEnable |
| OreSpawnMOBS | TheKingEnable | 1 | I | - | OreSpawnMain.TheKingEnable |
| OreSpawnMOBS | TheQueenEnable | 1 | I | - | OreSpawnMain.TheQueenEnable |
| OreSpawnMOBS | MantisEnable | 1 | I | - | OreSpawnMain.MantisEnable |
| OreSpawnMOBS | StinkyEnable | 1 | I | - | OreSpawnMain.StinkyEnable |
| OreSpawnMOBS | HerculesBeetleEnable | 1 | I | - | OreSpawnMain.HerculesBeetleEnable |
| OreSpawnMOBS | ChipmunkEnable | 1 | I | - | OreSpawnMain.ChipmunkEnable |
| OreSpawnMOBS | OstrichEnable | 1 | I | - | OreSpawnMain.OstrichEnable |
| OreSpawnMOBS | GazelleEnable | 1 | I | - | OreSpawnMain.GazelleEnable |
| OreSpawnMOBS | CowEnable | 1 | I | - | OreSpawnMain.CowEnable |
| OreSpawnMOBS | ButterflyEnable | 1 | I | - | OreSpawnMain.ButterflyEnable |
| OreSpawnMOBS | MothEnable | 1 | I | - | OreSpawnMain.MothEnable |
| OreSpawnMOBS | TshirtEnable | 1 | I | - | OreSpawnMain.TshirtEnable |
| OreSpawnMOBS | CoinEnable | 1 | I | - | OreSpawnMain.CoinEnable |
| OreSpawnMOBS | CreepingHorrorEnable | 1 | I | - | OreSpawnMain.CreepingHorrorEnable |
| OreSpawnMOBS | TerribleTerrorEnable | 1 | I | - | OreSpawnMain.TerribleTerrorEnable |
| OreSpawnMOBS | CliffRacerEnable | 1 | I | - | OreSpawnMain.CliffRacerEnable |
| OreSpawnMOBS | TriffidEnable | 1 | I | - | OreSpawnMain.TriffidEnable |
| OreSpawnMOBS | WormEnable | 1 | I | - | OreSpawnMain.WormEnable |
| OreSpawnMOBS | CloudSharkEnable | 1 | I | - | OreSpawnMain.CloudSharkEnable |
| OreSpawnMOBS | GoldFishEnable | 1 | I | - | OreSpawnMain.GoldFishEnable |
| OreSpawnMOBS | LeafMonsterEnable | 1 | I | - | OreSpawnMain.LeafMonsterEnable |
| OreSpawnMOBS | EnderKnightEnable | 1 | I | - | OreSpawnMain.EnderKnightEnable |
| OreSpawnMOBS | EnderReaperEnable | 1 | I | - | OreSpawnMain.EnderReaperEnable |
| OreSpawnMOBS | BeaverEnable | 1 | I | - | OreSpawnMain.BeaverEnable |
| OreSpawnMOBS | IrukandjiEnable | 1 | I | - | OreSpawnMain.IrukandjiEnable |
| OreSpawnMOBS | SkateEnable | 1 | I | - | OreSpawnMain.SkateEnable |
| OreSpawnMOBS | WhaleEnable | 1 | I | - | OreSpawnMain.WhaleEnable |
| OreSpawnMOBS | FlounderEnable | 1 | I | - | OreSpawnMain.FlounderEnable |
| OreSpawnMOBS | NightmareEnable | 1 | I | - | OreSpawnMain.PitchBlackEnable |
| OreSpawnMOBS | LurkingTerrorEnable | 1 | I | - | OreSpawnMain.LurkingTerrorEnable |
| OreSpawnMOBS | GodzillaEnable | 1 | I | - | OreSpawnMain.GodzillaEnable |
| OreSpawnMOBS | CrabEnable | 1 | I | - | OreSpawnMain.CrabEnable |
| OreSpawnMOBS | Bee_health | 80 | I | - | MobStats.health |
| OreSpawnMOBS | Bee_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | Bee_defense | 5 | I | - | MobStats.defense |
| OreSpawnMOBS | Mantis_health | 120 | I | - | MobStats.health |
| OreSpawnMOBS | Mantis_attack | 16 | I | - | MobStats.attack |
| OreSpawnMOBS | Mantis_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | HerculesBeetle_health | 250 | I | - | MobStats.health |
| OreSpawnMOBS | HerculesBeetle_attack | 30 | I | - | MobStats.attack |
| OreSpawnMOBS | HerculesBeetle_defense | 19 | I | - | MobStats.defense |
| OreSpawnMOBS | Mothra_health | 150 | I | - | MobStats.health |
| OreSpawnMOBS | Mothra_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | Mothra_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | Brutalfly_health | 110 | I | - | MobStats.health |
| OreSpawnMOBS | Brutalfly_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | Brutalfly_defense | 6 | I | - | MobStats.defense |
| OreSpawnMOBS | Nastysaurus_health | 200 | I | - | MobStats.health |
| OreSpawnMOBS | Nastysaurus_attack | 32 | I | - | MobStats.attack |
| OreSpawnMOBS | Nastysaurus_defense | 17 | I | - | MobStats.defense |
| OreSpawnMOBS | Pointysaurus_health | 80 | I | - | MobStats.health |
| OreSpawnMOBS | Pointysaurus_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | Pointysaurus_defense | 16 | I | - | MobStats.defense |
| OreSpawnMOBS | Alosaurus_health | 110 | I | - | MobStats.health |
| OreSpawnMOBS | Alosaurus_attack | 18 | I | - | MobStats.attack |
| OreSpawnMOBS | Alosaurus_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | SpiderRobot_health | 1500 | I | - | MobStats.health |
| OreSpawnMOBS | SpiderRobot_attack | 100 | I | - | MobStats.attack |
| OreSpawnMOBS | SpiderRobot_defense | 16 | I | - | MobStats.defense |
| OreSpawnMOBS | AntRobot_health | 300 | I | - | MobStats.health |
| OreSpawnMOBS | AntRobot_attack | 30 | I | - | MobStats.attack |
| OreSpawnMOBS | AntRobot_defense | 16 | I | - | MobStats.defense |
| OreSpawnMOBS | Jeffery_health | 550 | I | - | MobStats.health |
| OreSpawnMOBS | Jeffery_attack | 40 | I | - | MobStats.attack |
| OreSpawnMOBS | Jeffery_defense | 18 | I | - | MobStats.defense |
| OreSpawnMOBS | Hammerhead_health | 240 | I | - | MobStats.health |
| OreSpawnMOBS | Hammerhead_attack | 75 | I | - | MobStats.attack |
| OreSpawnMOBS | Hammerhead_defense | 20 | I | - | MobStats.defense |
| OreSpawnMOBS | Molenoid_health | 200 | I | - | MobStats.health |
| OreSpawnMOBS | Molenoid_attack | 18 | I | - | MobStats.attack |
| OreSpawnMOBS | Molenoid_defense | 12 | I | - | MobStats.defense |
| OreSpawnMOBS | TRex_health | 160 | I | - | MobStats.health |
| OreSpawnMOBS | TRex_attack | 22 | I | - | MobStats.attack |
| OreSpawnMOBS | TRex_defense | 14 | I | - | MobStats.defense |
| OreSpawnMOBS | BandP_health | 100 | I | - | MobStats.health |
| OreSpawnMOBS | BandP_attack | 1 | I | - | MobStats.attack |
| OreSpawnMOBS | BandP_defense | 18 | I | - | MobStats.defense |
| OreSpawnMOBS | CaterKiller_health | 450 | I | - | MobStats.health |
| OreSpawnMOBS | CaterKiller_attack | 32 | I | - | MobStats.attack |
| OreSpawnMOBS | CaterKiller_defense | 19 | I | - | MobStats.defense |
| OreSpawnMOBS | Cryolophosaurus_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | Cryolophosaurus_attack | 3 | I | - | MobStats.attack |
| OreSpawnMOBS | Cryolophosaurus_defense | 1 | I | - | MobStats.defense |
| OreSpawnMOBS | Rat_health | 5 | I | - | MobStats.health |
| OreSpawnMOBS | Rat_attack | 3 | I | - | MobStats.attack |
| OreSpawnMOBS | Rat_defense | 1 | I | - | MobStats.defense |
| OreSpawnMOBS | Urchin_health | 25 | I | - | MobStats.health |
| OreSpawnMOBS | Urchin_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | Urchin_defense | 4 | I | - | MobStats.defense |
| OreSpawnMOBS | Kyuubi_health | 125 | I | - | MobStats.health |
| OreSpawnMOBS | Kyuubi_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | Kyuubi_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | GammaMetroid_health | 100 | I | - | MobStats.health |
| OreSpawnMOBS | GammaMetroid_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | GammaMetroid_defense | 12 | I | - | MobStats.defense |
| OreSpawnMOBS | Basilisk_health | 200 | I | - | MobStats.health |
| OreSpawnMOBS | Basilisk_attack | 24 | I | - | MobStats.attack |
| OreSpawnMOBS | Basilisk_defense | 15 | I | - | MobStats.defense |
| OreSpawnMOBS | EmperorScorpion_health | 350 | I | - | MobStats.health |
| OreSpawnMOBS | EmperorScorpion_attack | 35 | I | - | MobStats.attack |
| OreSpawnMOBS | EmperorScorpion_defense | 20 | I | - | MobStats.defense |
| OreSpawnMOBS | TrooperBug_health | 200 | I | - | MobStats.health |
| OreSpawnMOBS | TrooperBug_attack | 20 | I | - | MobStats.attack |
| OreSpawnMOBS | TrooperBug_defense | 15 | I | - | MobStats.defense |
| OreSpawnMOBS | SpitBug_health | 100 | I | - | MobStats.health |
| OreSpawnMOBS | SpitBug_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | SpitBug_defense | 12 | I | - | MobStats.defense |
| OreSpawnMOBS | Alien_health | 100 | I | - | MobStats.health |
| OreSpawnMOBS | Alien_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | Alien_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | WaterDragon_health | 150 | I | - | MobStats.health |
| OreSpawnMOBS | WaterDragon_attack | 20 | I | - | MobStats.attack |
| OreSpawnMOBS | WaterDragon_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | SeaMonster_health | 110 | I | - | MobStats.health |
| OreSpawnMOBS | SeaMonster_attack | 14 | I | - | MobStats.attack |
| OreSpawnMOBS | SeaMonster_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | SeaViper_health | 160 | I | - | MobStats.health |
| OreSpawnMOBS | SeaViper_attack | 22 | I | - | MobStats.attack |
| OreSpawnMOBS | SeaViper_defense | 12 | I | - | MobStats.defense |
| OreSpawnMOBS | Robot2_health | 200 | I | - | MobStats.health |
| OreSpawnMOBS | Robot2_attack | 22 | I | - | MobStats.attack |
| OreSpawnMOBS | Robot2_defense | 18 | I | - | MobStats.defense |
| OreSpawnMOBS | Robot3_health | 80 | I | - | MobStats.health |
| OreSpawnMOBS | Robot3_attack | 16 | I | - | MobStats.attack |
| OreSpawnMOBS | Robot3_defense | 14 | I | - | MobStats.defense |
| OreSpawnMOBS | Robot4_health | 170 | I | - | MobStats.health |
| OreSpawnMOBS | Robot4_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | Robot4_defense | 18 | I | - | MobStats.defense |
| OreSpawnMOBS | Robot5_health | 20 | I | - | MobStats.health |
| OreSpawnMOBS | Robot5_attack | 5 | I | - | MobStats.attack |
| OreSpawnMOBS | Robot5_defense | 6 | I | - | MobStats.defense |
| OreSpawnMOBS | Rotator_health | 35 | I | - | MobStats.health |
| OreSpawnMOBS | Rotator_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | Rotator_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | Vortex_health | 150 | I | - | MobStats.health |
| OreSpawnMOBS | Vortex_attack | 26 | I | - | MobStats.attack |
| OreSpawnMOBS | Vortex_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | DungeonBeast_health | 65 | I | - | MobStats.health |
| OreSpawnMOBS | DungeonBeast_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | DungeonBeast_defense | 6 | I | - | MobStats.defense |
| OreSpawnMOBS | Triffid_health | 100 | I | - | MobStats.health |
| OreSpawnMOBS | Triffid_attack | 20 | I | - | MobStats.attack |
| OreSpawnMOBS | Triffid_defense | 12 | I | - | MobStats.defense |
| OreSpawnMOBS | LurkingTerror_health | 30 | I | - | MobStats.health |
| OreSpawnMOBS | LurkingTerror_attack | 6 | I | - | MobStats.attack |
| OreSpawnMOBS | LurkingTerror_defense | 5 | I | - | MobStats.defense |
| OreSpawnMOBS | WormSmall_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | WormSmall_attack | 3 | I | - | MobStats.attack |
| OreSpawnMOBS | WormSmall_defense | 0 | I | - | MobStats.defense |
| OreSpawnMOBS | WormMedium_health | 30 | I | - | MobStats.health |
| OreSpawnMOBS | WormMedium_attack | 10 | I | - | MobStats.attack |
| OreSpawnMOBS | WormMedium_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | WormLarge_health | 90 | I | - | MobStats.health |
| OreSpawnMOBS | WormLarge_attack | 18 | I | - | MobStats.attack |
| OreSpawnMOBS | WormLarge_defense | 14 | I | - | MobStats.defense |
| OreSpawnMOBS | EnderKnight_health | 60 | I | - | MobStats.health |
| OreSpawnMOBS | EnderKnight_attack | 12 | I | - | MobStats.attack |
| OreSpawnMOBS | EnderKnight_defense | 6 | I | - | MobStats.defense |
| OreSpawnMOBS | EnderReaper_health | 90 | I | - | MobStats.health |
| OreSpawnMOBS | EnderReaper_attack | 18 | I | - | MobStats.attack |
| OreSpawnMOBS | EnderReaper_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | Irukandji_health | 1 | I | - | MobStats.health |
| OreSpawnMOBS | Irukandji_attack | 20 | I | - | MobStats.attack |
| OreSpawnMOBS | Irukandji_defense | 0 | I | - | MobStats.defense |
| OreSpawnMOBS | AttackSquid_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | AttackSquid_attack | 8 | I | - | MobStats.attack |
| OreSpawnMOBS | AttackSquid_defense | 0 | I | - | MobStats.defense |
| OreSpawnMOBS | CaveFisher_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | CaveFisher_attack | 4 | I | - | MobStats.attack |
| OreSpawnMOBS | CaveFisher_defense | 4 | I | - | MobStats.defense |
| OreSpawnMOBS | CloudShark_health | 15 | I | - | MobStats.health |
| OreSpawnMOBS | CloudShark_attack | 6 | I | - | MobStats.attack |
| OreSpawnMOBS | CloudShark_defense | 5 | I | - | MobStats.defense |
| OreSpawnMOBS | CreepingHorror_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | CreepingHorror_attack | 3 | I | - | MobStats.attack |
| OreSpawnMOBS | CreepingHorror_defense | 2 | I | - | MobStats.defense |
| OreSpawnMOBS | Mobzilla_health | 4000 | I | - | MobStats.health |
| OreSpawnMOBS | Mobzilla_attack | 175 | I | - | MobStats.attack |
| OreSpawnMOBS | Mobzilla_defense | 21 | I | - | MobStats.defense |
| OreSpawnMOBS | Kraken_health | 1000 | I | - | MobStats.health |
| OreSpawnMOBS | Kraken_attack | 40 | I | - | MobStats.attack |
| OreSpawnMOBS | Kraken_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | LeafMonster_health | 6 | I | - | MobStats.health |
| OreSpawnMOBS | LeafMonster_attack | 2 | I | - | MobStats.attack |
| OreSpawnMOBS | LeafMonster_defense | 1 | I | - | MobStats.defense |
| OreSpawnMOBS | Nightmare_health | 250 | I | - | MobStats.health |
| OreSpawnMOBS | Nightmare_attack | 30 | I | - | MobStats.attack |
| OreSpawnMOBS | Nightmare_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | Scorpion_health | 15 | I | - | MobStats.health |
| OreSpawnMOBS | Scorpion_attack | 4 | I | - | MobStats.attack |
| OreSpawnMOBS | Scorpion_defense | 10 | I | - | MobStats.defense |
| OreSpawnMOBS | Skate_health | 8 | I | - | MobStats.health |
| OreSpawnMOBS | Skate_attack | 8 | I | - | MobStats.attack |
| OreSpawnMOBS | Skate_defense | 4 | I | - | MobStats.defense |
| OreSpawnMOBS | TerribleTerror_health | 10 | I | - | MobStats.health |
| OreSpawnMOBS | TerribleTerror_attack | 5 | I | - | MobStats.attack |
| OreSpawnMOBS | TerribleTerror_defense | 3 | I | - | MobStats.defense |
| OreSpawnMOBS | TheKing_health | 7000 | I | - | MobStats.health |
| OreSpawnMOBS | TheKing_attack | 350 | I | - | MobStats.attack |
| OreSpawnMOBS | TheKing_defense | 21 | I | - | MobStats.defense |
| OreSpawnMOBS | TheQueen_health | 6000 | I | - | MobStats.health |
| OreSpawnMOBS | TheQueen_attack | 225 | I | - | MobStats.attack |
| OreSpawnMOBS | TheQueen_defense | 21 | I | - | MobStats.defense |
| OreSpawnMOBS | Leonopteryx_health | 150 | I | - | MobStats.health |
| OreSpawnMOBS | Leonopteryx_attack | 20 | I | - | MobStats.attack |
| OreSpawnMOBS | Leonopteryx_defense | 8 | I | - | MobStats.defense |
| OreSpawnMOBS | Crab_health | 180 | I | - | MobStats.health |
| OreSpawnMOBS | Crab_attack | 24 | I | - | MobStats.attack |
| OreSpawnMOBS | Crab_defense | 16 | I | - | MobStats.defense |
| OreSpawnARMOR | Amethyst_durability | 100 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Amethyst_head_damage_reduce | 4 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Amethyst_chest_damage_reduce | 8 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Amethyst_leggings_damage_reduce | 7 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Amethyst_boots_damage_reduce | 3 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Amethyst_enchantability | 40 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Amethyst_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Amethyst_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Amethyst_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Amethyst_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Amethyst_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Amethyst_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Amethyst_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Amethyst_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Emerald_durability | 60 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Emerald_head_damage_reduce | 3 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Emerald_chest_damage_reduce | 8 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Emerald_leggings_damage_reduce | 6 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Emerald_boots_damage_reduce | 3 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Emerald_enchantability | 40 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Emerald_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Emerald_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Emerald_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Emerald_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Emerald_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Emerald_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Emerald_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Emerald_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Experience_durability | 70 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Experience_head_damage_reduce | 5 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Experience_chest_damage_reduce | 9 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Experience_leggings_damage_reduce | 7 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Experience_boots_damage_reduce | 4 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Experience_enchantability | 50 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Experience_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Experience_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Experience_enchant_protection | 2 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Experience_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Experience_enchant_blastprotection | 1 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Experience_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Experience_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Experience_enchant_featherfalling | 1 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | MothScale_durability | 50 | I | - | ArmorStats.durability |
| OreSpawnARMOR | MothScale_head_damage_reduce | 2 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | MothScale_chest_damage_reduce | 7 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | MothScale_leggings_damage_reduce | 5 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | MothScale_boots_damage_reduce | 2 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | MothScale_enchantability | 50 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | MothScale_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | MothScale_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | MothScale_enchant_protection | 3 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | MothScale_enchant_fireprotection | 3 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | MothScale_enchant_blastprotection | 3 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | MothScale_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | MothScale_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | MothScale_enchant_featherfalling | 5 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | LavaEel_durability | 40 | I | - | ArmorStats.durability |
| OreSpawnARMOR | LavaEel_head_damage_reduce | 2 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | LavaEel_chest_damage_reduce | 7 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | LavaEel_leggings_damage_reduce | 5 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | LavaEel_boots_damage_reduce | 2 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | LavaEel_enchantability | 35 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | LavaEel_enchant_respiration | 1 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | LavaEel_enchant_aquaaffinity | 2 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | LavaEel_enchant_protection | 3 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | LavaEel_enchant_fireprotection | 2 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | LavaEel_enchant_blastprotection | 10 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | LavaEel_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | LavaEel_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | LavaEel_enchant_featherfalling | 2 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Ultimate_durability | 200 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Ultimate_head_damage_reduce | 6 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Ultimate_chest_damage_reduce | 12 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Ultimate_leggings_damage_reduce | 10 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Ultimate_boots_damage_reduce | 6 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Ultimate_enchantability | 100 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Ultimate_enchant_respiration | 2 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Ultimate_enchant_aquaaffinity | 3 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Ultimate_enchant_protection | 5 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Ultimate_enchant_fireprotection | 5 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Ultimate_enchant_blastprotection | 5 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Ultimate_enchant_projectileprotection | 5 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Ultimate_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Ultimate_enchant_featherfalling | 3 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Pink_durability | 50 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Pink_head_damage_reduce | 3 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Pink_chest_damage_reduce | 7 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Pink_leggings_damage_reduce | 5 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Pink_boots_damage_reduce | 2 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Pink_enchantability | 40 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Pink_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Pink_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Pink_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Pink_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Pink_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Pink_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Pink_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Pink_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | TigersEye_durability | 80 | I | - | ArmorStats.durability |
| OreSpawnARMOR | TigersEye_head_damage_reduce | 4 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | TigersEye_chest_damage_reduce | 8 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | TigersEye_leggings_damage_reduce | 7 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | TigersEye_boots_damage_reduce | 4 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | TigersEye_enchantability | 55 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | TigersEye_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | TigersEye_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | TigersEye_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | TigersEye_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | TigersEye_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | TigersEye_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | TigersEye_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | TigersEye_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Peacock_durability | 40 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Peacock_head_damage_reduce | 2 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Peacock_chest_damage_reduce | 5 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Peacock_leggings_damage_reduce | 4 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Peacock_boots_damage_reduce | 2 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Peacock_enchantability | 30 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Peacock_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Peacock_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Peacock_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Peacock_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Peacock_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Peacock_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Peacock_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Peacock_enchant_featherfalling | 10 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Mobzilla_durability | 1000 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Mobzilla_head_damage_reduce | 7 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Mobzilla_chest_damage_reduce | 13 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Mobzilla_leggings_damage_reduce | 11 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Mobzilla_boots_damage_reduce | 7 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Mobzilla_enchantability | 150 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Mobzilla_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Mobzilla_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Mobzilla_enchant_protection | 10 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Mobzilla_enchant_fireprotection | 10 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Mobzilla_enchant_blastprotection | 10 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Mobzilla_enchant_projectileprotection | 10 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Mobzilla_enchant_unbreaking | 5 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Mobzilla_enchant_featherfalling | 10 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Ruby_durability | 90 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Ruby_head_damage_reduce | 4 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Ruby_chest_damage_reduce | 9 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Ruby_leggings_damage_reduce | 8 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Ruby_boots_damage_reduce | 4 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Ruby_enchantability | 40 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Ruby_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Ruby_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Ruby_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Ruby_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Ruby_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Ruby_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Ruby_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Ruby_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Royal_durability | 2000 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Royal_head_damage_reduce | 8 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Royal_chest_damage_reduce | 14 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Royal_leggings_damage_reduce | 12 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Royal_boots_damage_reduce | 8 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Royal_enchantability | 200 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Royal_enchant_respiration | 1 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Royal_enchant_aquaaffinity | 2 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Royal_enchant_protection | 10 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Royal_enchant_fireprotection | 10 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Royal_enchant_blastprotection | 10 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Royal_enchant_projectileprotection | 10 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Royal_enchant_unbreaking | 5 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Royal_enchant_featherfalling | 10 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Lapis_durability | 60 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Lapis_head_damage_reduce | 2 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Lapis_chest_damage_reduce | 7 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Lapis_leggings_damage_reduce | 5 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Lapis_boots_damage_reduce | 2 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Lapis_enchantability | 60 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Lapis_enchant_respiration | 1 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Lapis_enchant_aquaaffinity | 1 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Lapis_enchant_protection | 1 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Lapis_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Lapis_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Lapis_enchant_projectileprotection | 1 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Lapis_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Lapis_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnARMOR | Queen_durability | 1500 | I | - | ArmorStats.durability |
| OreSpawnARMOR | Queen_head_damage_reduce | 9 | I | - | ArmorStats.head_protection |
| OreSpawnARMOR | Queen_chest_damage_reduce | 16 | I | - | ArmorStats.chest_protection |
| OreSpawnARMOR | Queen_leggings_damage_reduce | 14 | I | - | ArmorStats.leg_protection |
| OreSpawnARMOR | Queen_boots_damage_reduce | 9 | I | - | ArmorStats.boot_protection |
| OreSpawnARMOR | Queen_enchantability | 150 | I | - | ArmorStats.enchantability |
| OreSpawnARMOR | Queen_enchant_respiration | 0 | I | - | ArmorStats.e_respiration |
| OreSpawnARMOR | Queen_enchant_aquaaffinity | 0 | I | - | ArmorStats.e_aquaaffinity |
| OreSpawnARMOR | Queen_enchant_protection | 0 | I | - | ArmorStats.e_protection |
| OreSpawnARMOR | Queen_enchant_fireprotection | 0 | I | - | ArmorStats.e_fireprotection |
| OreSpawnARMOR | Queen_enchant_blastprotection | 0 | I | - | ArmorStats.e_blastprotection |
| OreSpawnARMOR | Queen_enchant_projectileprotection | 0 | I | - | ArmorStats.e_projectileprotection |
| OreSpawnARMOR | Queen_enchant_unbreaking | 0 | I | - | ArmorStats.e_unbreaking |
| OreSpawnARMOR | Queen_enchant_featherfalling | 0 | I | - | ArmorStats.e_featherfalling |
| OreSpawnWEAPONS | Ultimate_harvestlevel | 10 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Ultimate_maxuses | 3000 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Ultimate_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Ultimate_damage | 36 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Ultimate_enchantability | 100 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Nightmare_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Nightmare_maxuses | 1800 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Nightmare_efficiency | 12 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Nightmare_damage | 26 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Nightmare_enchantability | 60 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Bertha_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Bertha_maxuses | 9000 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Bertha_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Bertha_damage | 496 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Bertha_enchantability | 100 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | CrystalWood_harvestlevel | 2 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | CrystalWood_maxuses | 300 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | CrystalWood_efficiency | 3 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | CrystalWood_damage | 2 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | CrystalWood_enchantability | 15 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | CrystalStone_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | CrystalStone_maxuses | 800 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | CrystalStone_efficiency | 6 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | CrystalStone_damage | 5 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | CrystalStone_enchantability | 45 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Pink_harvestlevel | 4 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Pink_maxuses | 1100 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Pink_efficiency | 10 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Pink_damage | 7 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Pink_enchantability | 65 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | TigersEye_harvestlevel | 4 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | TigersEye_maxuses | 1600 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | TigersEye_efficiency | 12 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | TigersEye_damage | 8 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | TigersEye_enchantability | 75 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Ruby_harvestlevel | 5 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Ruby_maxuses | 1500 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Ruby_efficiency | 11 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Ruby_damage | 16 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Ruby_enchantability | 85 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Amethyst_harvestlevel | 4 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Amethyst_maxuses | 2000 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Amethyst_efficiency | 11 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Amethyst_damage | 11 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Amethyst_enchantability | 70 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Emerald_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Emerald_maxuses | 1300 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Emerald_efficiency | 10 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Emerald_damage | 6 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Emerald_enchantability | 75 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Royal_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Royal_maxuses | 10000 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Royal_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Royal_damage | 746 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Royal_enchantability | 150 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Attitude_harvestlevel | 5 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Attitude_maxuses | 2000 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Attitude_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Attitude_damage | 82 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Attitude_enchantability | 100 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | BattleAxe_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | BattleAxe_maxuses | 1500 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | BattleAxe_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | BattleAxe_damage | 46 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | BattleAxe_enchantability | 75 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | Chainsaw_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | Chainsaw_maxuses | 1500 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | Chainsaw_efficiency | 10 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | Chainsaw_damage | 56 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | Chainsaw_enchantability | 75 | I | - | WeaponStats.enchantability |
| OreSpawnWEAPONS | QueenBattleAxe_harvestlevel | 3 | I | - | WeaponStats.harvestlevel |
| OreSpawnWEAPONS | QueenBattleAxe_maxuses | 2200 | I | - | WeaponStats.maxuses |
| OreSpawnWEAPONS | QueenBattleAxe_efficiency | 15 | I | - | WeaponStats.efficiency |
| OreSpawnWEAPONS | QueenBattleAxe_damage | 662 | I | - | WeaponStats.damage |
| OreSpawnWEAPONS | QueenBattleAxe_enchantability | 100 | I | - | WeaponStats.enchantability |
| OreSpawnORES | Ruby_rate | 10 | I | - | OreStats.rate |
| OreSpawnORES | Ruby_clumpsize | 1 | I | - | OreStats.clumpsize |
| OreSpawnORES | Ruby_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Ruby_maxdepth | 50 | I | - | OreStats.maxdepth |
| OreSpawnORES | BlockRuby_rate | 1 | I | - | OreStats.rate |
| OreSpawnORES | BlockRuby_clumpsize | 2 | I | - | OreStats.clumpsize |
| OreSpawnORES | BlockRuby_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | BlockRuby_maxdepth | 15 | I | - | OreStats.maxdepth |
| OreSpawnORES | Uranium_rate | 3 | I | - | OreStats.rate |
| OreSpawnORES | Uranium_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | Uranium_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Uranium_maxdepth | 30 | I | - | OreStats.maxdepth |
| OreSpawnORES | Titanium_rate | 3 | I | - | OreStats.rate |
| OreSpawnORES | Titanium_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | Titanium_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Titanium_maxdepth | 20 | I | - | OreStats.maxdepth |
| OreSpawnORES | Amethyst_rate | 2 | I | - | OreStats.rate |
| OreSpawnORES | Amethyst_clumpsize | 6 | I | - | OreStats.clumpsize |
| OreSpawnORES | Amethyst_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Amethyst_maxdepth | 25 | I | - | OreStats.maxdepth |
| OreSpawnORES | Salt_rate | 5 | I | - | OreStats.rate |
| OreSpawnORES | Salt_clumpsize | 12 | I | - | OreStats.clumpsize |
| OreSpawnORES | Salt_mindepth | 50 | I | - | OreStats.mindepth |
| OreSpawnORES | Salt_maxdepth | 128 | I | - | OreStats.maxdepth |
| OreSpawnORES | SpawnOres_rate | 28 | I | - | OreStats.rate |
| OreSpawnORES | SpawnOres_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | SpawnOres_mindepth | 50 | I | - | OreStats.mindepth |
| OreSpawnORES | SpawnOres_maxdepth | 128 | I | - | OreStats.maxdepth |
| OreSpawnORES | Diamond_rate | 4 | I | - | OreStats.rate |
| OreSpawnORES | Diamond_clumpsize | 6 | I | - | OreStats.clumpsize |
| OreSpawnORES | Diamond_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Diamond_maxdepth | 30 | I | - | OreStats.maxdepth |
| OreSpawnORES | BlockDiamond_rate | 2 | I | - | OreStats.rate |
| OreSpawnORES | BlockDiamond_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | BlockDiamond_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | BlockDiamond_maxdepth | 20 | I | - | OreStats.maxdepth |
| OreSpawnORES | Emerald_rate | 4 | I | - | OreStats.rate |
| OreSpawnORES | Emerald_clumpsize | 6 | I | - | OreStats.clumpsize |
| OreSpawnORES | Emerald_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Emerald_maxdepth | 40 | I | - | OreStats.maxdepth |
| OreSpawnORES | BlockEmerald_rate | 2 | I | - | OreStats.rate |
| OreSpawnORES | BlockEmerald_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | BlockEmerald_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | BlockEmerald_maxdepth | 20 | I | - | OreStats.maxdepth |
| OreSpawnORES | Gold_rate | 4 | I | - | OreStats.rate |
| OreSpawnORES | Gold_clumpsize | 8 | I | - | OreStats.clumpsize |
| OreSpawnORES | Gold_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | Gold_maxdepth | 40 | I | - | OreStats.maxdepth |
| OreSpawnORES | BlockGold_rate | 2 | I | - | OreStats.rate |
| OreSpawnORES | BlockGold_clumpsize | 4 | I | - | OreStats.clumpsize |
| OreSpawnORES | BlockGold_mindepth | 0 | I | - | OreStats.mindepth |
| OreSpawnORES | BlockGold_maxdepth | 25 | I | - | OreStats.maxdepth |

**Clamps after reading** (bytecode branches in the helpers, same code in the decompiled source):

- `get_mobstats`: health and attack clamped to [default/2, default*2]; defense to [default-4, default+4], then to [0, 22].
- `get_weaponstats`: harvestlevel below default-1 is reset to the default; maxuses, efficiency, damage, enchantability clamped to [default/2, default*2].
- `get_armorstats`: durability and enchantability [default/2, default*2]; head/chest/leggings/boots reduction only lower bound default-2; the eight auto-enchant levels only lower bound default/2.
- `get_orestats`: rate and clumpsize [default/2, default*2], clumpsize at least 1; min/max depth at least 0; if maxdepth - mindepth < 10 both are reset to the defaults.
- `preInit` itself: UltimateSwordEnchantmentLevel 1..10, UltimateBowDamage 2..20, IslandSpeedFactor and IslandSizeFactor 1..5, NightmareSize 0..5, LessLag 0..2; LessLag == 1 caps both island factors at 2; LessLag == 2 caps them at 1 and forces LessOre = 1.
- `AllMobsDisable != 0` calls `disableAllMobs()` (right after `config.save()`), which sets 99 statics to 0: 98 `<X>Enable` flags plus `MothraPeaceful` (`CrabEnable` is written twice).

## 11. Sounds and textures

- `assets/orespawn/sounds.json`: **126 sound events**, 307 file references, all categories `master`; **310 `.ogg`** files in `assets/orespawn/sounds/`.
- Event `wtf_hurt` is defined but never referenced by a class string. Class literals not present in `sounds.json`: `orespawn:frog`, `orespawn:little_splt`, `orespawn:o_rain`, `orespawn:rat_hit`. `.ogg` files not referenced by `sounds.json`: `frog1.ogg`, `frog2.ogg`, `o_rain.ogg`. Mapping event -> files -> referencing classes: `sounds_dump.txt`.
- **1058 PNGs**: 336 in `assets/orespawn/` root (entity skins, armor layers, item-renderer skins, GUI), 474 in `textures/items/`, 248 in `textures/blocks/`. Every PNG with its pixel size: `textures_dump.txt`.
- No model files (`.json`, `.obj`, `.tcn`): all entity and item-renderer geometry is Java code (109 `ModelBase` subclasses).

| sound event | files | referenced by |
|---|---|---|
| alien_hurt | alien_hurt | Alien |
| alien_death | alien_death | Alien |
| alien_living | alien_living | Alien |
| alo_hurt | alo_hurt | Alosaurus, Basilisk, Cephadrome, Dragon, EmperorScorpion, Godzilla, Hammerhead, HerculesBeetle, Kyuubi, Lizard, Nastysaurus, Pointysaurus, TRex, ThePrinceTeen |
| alo_death | alo_death | Alosaurus, Bee, Cephadrome, Dragon, GammaMetroid, Kraken, Kyuubi, Lizard, Mantis, Nastysaurus, Pointysaurus, ThePrinceTeen, WormLarge |
| alo_living | alo_living | Alosaurus, Nastysaurus, Pointysaurus |
| b_dark | b_dark | Boyfriend |
| b_death_boyfriend | b_death_boyfriend | Boyfriend |
| b_death_single | b_death_single1, b_death_single2 | Boyfriend |
| b_fight | b_fight1, b_fight2, b_fight3, b_fight4, b_fight5, b_fight6, b_fight7 | Boyfriend |
| b_happy | b_happy1, b_happy2, b_happy3, b_happy4, b_happy5, b_happy6, b_happy7, b_happy8 | Boyfriend |
| b_hurt | b_hurt1, b_hurt2, b_hurt3, b_hurt4, b_hurt5, b_hurt6, b_hurt7, b_hurt8, b_hurt9, b_hurt10 | Boyfriend |
| b_ow | b_ow1, b_ow2, b_ow3, b_ow4, b_ow5, b_ow6, b_ow7, b_ow8, b_ow9 | Boyfriend |
| b_rain | b_rain1, b_rain2 | Boyfriend |
| b_taunt | b_taunt1, b_taunt2, b_taunt3, b_taunt4, b_taunt5, b_taunt6 | Boyfriend |
| b_thunder | b_thunder | Boyfriend |
| b_water | b_water1, b_water2 | Boyfriend |
| b_woohoo | b_woohoo1, b_woohoo2, b_woohoo3, b_woohoo4 | Boyfriend |
| basilisk_living | basilisk_living | Basilisk |
| bb_happy | bb_happy1, bb_happy2, bb_happy3, bb_happy4, bb_happy5, bb_happy6 | Boyfriend |
| Beebuzz | Beebuzz | Bee, Mantis |
| big_splat | big_splat | CloudShark, Fairy, Frog, Whale, WormLarge, WormMedium |
| birds | birds1, birds2, birds3, birds4, birds5, birds6, birds7, birds8, birds9, birds10, birds11, birds12, birds13, birds14, birds15, birds16, birds17, birds18, birds19, birds20, birds21, birds22, birds23 | Cockateil |
| caterkiller_death | caterkiller_death | CaterKiller |
| caterkiller_hit | caterkiller_hit1, caterkiller_hit2, caterkiller_hit3, caterkiller_hit4 | CaterKiller |
| caterkiller_living | caterkiller_living1, caterkiller_living2, caterkiller_living3, caterkiller_living4 | CaterKiller |
| chain_rattles | chain_rattles | GhostSkelly |
| chainsaw | chainsaw | Beaver |
| chainsawshort | chainsawshort | UltimateSword |
| clatter | clatter1, clatter2 | SpitBug, TrooperBug |
| cliffracer | cliffracer | CliffRacer |
| creepinghorror_dead | creepinghorror_dead | CreepingHorror |
| creepinghorror_hit | creepinghorror_hit | CreepingHorror |
| creepinghorror_living | creepinghorror_living | CreepingHorror |
| cricket | cricket | Cricket |
| crunch | crunch | SpitBug, TrooperBug |
| cryo_death | cryo_death | Beaver, Camarasaurus, CaveFisher, Chipmunk, Cryolophosaurus, Gazelle, Hydrolisc, Ostrich, Scorpion, Spyro, Stinky, ThePrince, ThePrincess, VelocityRaptor |
| cryo_hurt | cryo_hurt | Camarasaurus, CaveFisher, Cryolophosaurus, Hydrolisc, Ostrich, VelocityRaptor |
| cryo_living | cryo_living | Cryolophosaurus |
| dbdead | dbdead | DungeonBeast |
| dbhit | dbhit1, dbhit2, dbhit3 | DungeonBeast |
| dragonfly_death | dragonfly_death | Dragonfly |
| dragonfly_hurt | dragonfly_hurt | Bee, Dragonfly, Mantis |
| dragonfly_living | dragonfly_living | Dragonfly |
| duck_hurt | duck_hurt | Baryonyx, Cassowary, Cockateil, EasterBunny, GammaMetroid, RubberDucky, Spyro, Stinky, ThePrince, ThePrincess |
| emperorscorpion_death | emperorscorpion_death | Basilisk, EmperorScorpion, SpitBug, TrooperBug |
| fart | fart1, fart2, fart3, fart4, fart5, fart6, fart7, fart8, fart9 | StinkBug, Stinky |
| ghost_sound | ghost_sound | Ghost |
| glassdead | glassdead1, glassdead2 | EntityThrownRock, Rotator, Urchin |
| glasshit | glasshit1, glasshit2, glasshit3, glasshit4, glasshit5 | Rotator, Urchin |
| godzilla_death | godzilla_death | Godzilla |
| godzilla_living | godzilla_living | Godzilla |
| hammerhead_death | hammerhead_death | Hammerhead |
| hammerhead_living | hammerhead_living1, hammerhead_living2 | Hammerhead |
| hercules_death | hercules_death | HerculesBeetle |
| hover | hover1, hover2, hover3, hover4, hover5, hover6 | Elevator |
| king_hit | king_hit | TheKing, ThePrinceAdult, TheQueen |
| king_living | king_living | TheKing, ThePrinceAdult, TheQueen |
| kraken_living | kraken_living | Kraken |
| kyuubi_living | kyuubi_living | Kyuubi, Robot1, Urchin |
| leaves_death | leaves_death | LeafMonster |
| leaves_hit | leaves_hit | Crab, LeafMonster |
| leon_death | leon_death | Leon |
| leon_hit | leon_hit1, leon_hit2, leon_hit3 | Leon |
| leon_living | leon_living | Leon |
| little_splat | little_splat | CloudShark, GoldFish, Whale, WormMedium, WormSmall |
| lurkinghorror_dead | lurkinghorror_dead | LurkingTerror |
| lurkinghorror_hit | lurkinghorror_hit | LurkingTerror |
| lurkinghorror_living | lurkinghorror_living | LurkingTerror |
| molenoid_death | molenoid_death | Molenoid |
| molenoid_hit | molenoid_hit1, molenoid_hit2, molenoid_hit3, molenoid_hit4, molenoid_hit5, molenoid_hit6 | Molenoid |
| molenoid_living | molenoid_living1, molenoid_living2, molenoid_living3 | Molenoid |
| mosquito | mosquito | EntityMosquito |
| MothraWings | MothraWings1, MothraWings2, MothraWings3 | Brutalfly, Cephadrome, Dragon, Leon, Mothra, PitchBlack, TheKing, ThePrinceAdult, ThePrinceTeen, TheQueen |
| o_dark | o_dark | Girlfriend |
| o_death_girlfriend | o_death_girlfriend | Girlfriend |
| o_death_single | o_death_single | Girlfriend |
| o_fight | o_fight1, o_fight2, o_fight3, o_fight4, o_fight5, o_fight6, o_fight7 | Girlfriend |
| o_happy | o_happy1, o_happy2, o_happy3, o_happy4, o_happy5, o_happy6, o_happy7 | Girlfriend |
| o_hurt | o_hurt1, o_hurt2, o_hurt3, o_hurt4, o_hurt5, o_hurt6, o_hurt7, o_hurt8, o_hurt9 | Girlfriend |
| o_ow | o_ow1, o_ow2, o_ow3, o_ow4, o_ow5, o_ow6, o_ow7, o_ow8 | Girlfriend |
| o_taunt | o_taunt1, o_taunt2, o_taunt3, o_taunt4 | Girlfriend |
| o_thunder | o_thunder | Girlfriend |
| o_water | o_water1, o_water2 | Girlfriend |
| o_woohoo | o_woohoo1, o_woohoo2, o_woohoo3, o_woohoo4 | Girlfriend |
| peacockdead | peacockdead | Peacock |
| peacockhit | peacockhit | Peacock |
| peacocklive | peacocklive | Peacock |
| pitchblack_dead | pitchblack_dead | PitchBlack |
| pitchblack_hit | pitchblack_hit | PitchBlack |
| pitchblack_living | pitchblack_living | PitchBlack |
| ratdead | ratdead1, ratdead2, ratdead3 | Flounder, Irukandji, Rat, Skate |
| rathit | rathit | Rat |
| ratlive | ratlive | Rat |
| roar | roar1, roar2, roar3, roar4, roar5, roar6 | Dragon, Spyro, ThePrince, ThePrinceTeen, ThePrincess |
| robot_death | robot_death1, robot_death2, robot_death3, robot_death4, robot_death5 | GiantRobot, Robot2, Robot3, Robot4, Robot5 |
| robot_hurt | robot_hurt1, robot_hurt2, robot_hurt3, robot_hurt4, robot_hurt5, robot_hurt6, robot_hurt7, robot_hurt8 | GiantRobot, Robot2, Robot3, Robot4, Robot5 |
| robot_living | robot_living1, robot_living2, robot_living3, robot_living4 | GiantRobot, Robot2, Robot3, Robot4, Robot5 |
| robot1_death | robot1_death | Robot1 |
| robotspider | robotspider1, robotspider2, robotspider3, robotspider4, robotspider5, robotspider6, robotspider7, robotspider8, robotspider9, robotspider10, robotspider11 | AntRobot, SpiderRobot |
| robotspidermount | robotspidermount | AntRobot, SpiderRobot |
| rubybird | rubybird | RubyBird |
| scorpion_attack | scorpion_attack | Crab, EmperorScorpion, HerculesBeetle, Scorpion, TrooperBug |
| scorpion_hit | scorpion_hit | Beaver, Chipmunk, Frog, Gazelle, Robot1, Scorpion |
| scorpion_living | scorpion_living | Crab, EmperorScorpion, HerculesBeetle |
| seamonster_death | seamonster_death | SeaMonster |
| seamonster_hit | seamonster_hit | SeaMonster |
| seamonster_living | seamonster_living1, seamonster_living2 | SeaMonster |
| seaviper_death | seaviper_death | SeaViper |
| seaviper_hit | seaviper_hit1, seaviper_hit2, seaviper_hit3 | SeaViper |
| seaviper_living | seaviper_living | SeaViper |
| squid_death | squid_death1, squid_death2 | AttackSquid |
| squid_hurt | squid_hurt1, squid_hurt2, squid_hurt3, squid_hurt4 | AttackSquid |
| terribleterror_dead | terribleterror_dead | TerribleTerror |
| terribleterror_hit | terribleterror_hit | TerribleTerror |
| terribleterror_living | terribleterror_living | TerribleTerror |
| trex_death | trex_death | TRex, TheKing, ThePrinceAdult, TheQueen |
| trex_living | trex_living | TRex |
| triffid_dead | triffid_dead | Triffid |
| triffid_hit | triffid_hit | Triffid |
| triffid_living | triffid_living | Triffid |
| vortexlive | vortexlive | Vortex |
| waterdragon_death | waterdragon_death | WaterDragon |
| waterdragon_hurt | waterdragon_hurt1, waterdragon_hurt2, waterdragon_hurt3 | WaterDragon |
| wtf_hurt | wtf_hurt | - |
| wtf_living | wtf_living | GammaMetroid |

## 12. Achievements

None. No class references `net.minecraft.stats.Achievement` or `StatBase` (constant-pool scan of all 594 classes).

## 13. Models & design

109 of 109 `ModelBase` subclasses reconstructed from constructor bytecode, 0 failures; all 3584 parts match the decompiled source (texture offset, box, pivot, rotation). Per-model detail and porting notes: `../06-models-design.md`; geometry JSON: `models/<Model>.json`.

| entity | model class | parts | boxes | texture(s) | PNG size | model decl. size | animated parts | GL in model render | renderer specials |
|---|---|---|---|---|---|---|---|---|---|
| Girlfriend | vanilla ModelBiped | - | - | FrogPrincess.png, FrogPrincess2.png, bikini0.png, bikini1.png +58 | 64x32 | - | - | - | glScalef(5.0); ctor args 0.5 |
| Boyfriend | vanilla ModelBiped | - | - | FrogPrince.png, FrogPrince2.png, boyfriend0.png, boyfriend1.png +44 | 64x32 | - | - | - | ctor args 0.55 |
| RedCow | vanilla ModelCow | - | - | crystal_cow.png, gold_cow.png, red_cow.png | 64x32 | - | - | - | shouldRenderPass (2nd pass); ctor args 0.7 |
| GoldCow | vanilla ModelCow | - | - | crystal_cow.png, gold_cow.png, red_cow.png | 64x32 | - | - | - | shouldRenderPass (2nd pass); ctor args 0.7 |
| EnchantedCow | vanilla ModelCow | - | - | crystal_cow.png, gold_cow.png, red_cow.png | 64x32 | - | - | - | shouldRenderPass (2nd pass); ctor args 0.7 |
| CrystalCow | vanilla ModelCow | - | - | crystal_cow.png, gold_cow.png, red_cow.png | 64x32 | - | - | - | shouldRenderPass (2nd pass); ctor args 0.7 |
| Shoes | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| SunspotUrchin | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| WaterBall | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| InkSack | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| LaserBall | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| IceBall | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| Acid | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| DeadIrukandji | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| BerthaHit | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| EntityCage | none | - | - | ? | ? | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| UltimateFishHook | none | - | - | ? | ? | - | - | - | - |
| UltimateArrow | none | - | - | ? | ? | - | - | - | - |
| EntityThrownRock | none | - | - | textures/items/rock.png, textures/items/rockblue.png, textures/items/rockcrystalblue.png, textures/items/rockcrystalgreen.png +8 | 16x16 | - | - | - | custom doRender scale: doRender: 0.5, 0.5, 0.5 |
| EntityButterfly | ModelButterfly | 10 | 10 | butterfly.png, butterfly2.png, butterfly3.png, butterfly4.png +6 | 64x32 | 64x32 | 8 | - | glScalef(this.scale); shouldRenderPass (2nd pass); ctor args 0.3, 1 |
| Firefly | ModelFirefly | 12 | 12 | Fireflytexture.png | 64x128 | 64x128 | 2 | tint | glScalef(this.scale); ctor args 0.2, 0.75 |
| EntityLunaMoth | ModelButterfly | 10 | 10 | textures/entity/creeper/creeper_armor.png | ? | 64x32 | 8 | - | glScalef(this.scale); shouldRenderPass (2nd pass); ctor args 0.4, 1.5 |
| EntityMosquito | ModelMosquito | 5 | 5 | mosquito.png | 32x32 | 32x32 | 4 | - | glScalef(this.scale); ctor args 0.3, 0.5 |
| Ghost | ModelGhost | 3 | 3 | Ghosttexture.png | 64x64 | 64x64 | 2 | blend, tint | glScalef(this.scale); ctor args 0, 0.65 |
| GhostSkelly | ModelGhostSkelly | 10 | 10 | GhostSkellytexture.png | 128x64 | 128x64 | 7 | blend, tint | glScalef(this.scale); ctor args 0, 1.05 |
| Mothra | ModelButterfly | 10 | 10 | textures/entity/creeper/creeper_armor.png | ? | 64x32 | 8 | - | glScalef(this.scale); shouldRenderPass (2nd pass); ctor args 0.75, 10 |
| EntityAnt | ModelAnt | 20 | 20 | ant.png, rainbow_ant.png, red_ant.png, termite.png +1 | 64x32 | 64x32 | 14 | - | glScalef(this.scale); ctor args 0.1, 0.25 |
| EntityRedAnt | ModelAnt | 20 | 20 | ? | ? | 64x32 | 14 | - | glScalef(this.scale); ctor args 0.15, 0.35 |
| EntityRainbowAnt | ModelAnt | 20 | 20 | ? | ? | 64x32 | 14 | - | glScalef(this.scale); ctor args 0.1, 0.25 |
| EntityUnstableAnt | ModelAnt | 20 | 20 | ? | ? | 64x32 | 14 | - | glScalef(this.scale); ctor args 0.1, 0.25 |
| Alosaurus | ModelAlosaurus | 21 | 21 | alosaurus.png | 128x128 | 128x128 | 11 | - | glScalef(this.scale); ctor args 1, 1 |
| TRex | ModelTRex | 27 | 27 | TRextexture.png | 128x128 | 128x128 | 11 | - | glScalef(this.scale); ctor args 1, 1.2 |
| Tshirt | ModelTshirt | 2 | 2 | Tshirttexture.png | 320x160 | 512x256 | 2 | - | glScalef(this.scale); ctor args 1, 0.33 |
| Cryolophosaurus | ModelCryolophosaurus | 20 | 20 | cryolophosaurus.png | 128x128 | 128x128 | 9 | - | glScalef(this.scale); ctor args 0.75, 0.5 |
| Basilisk | ModelBasilisk | 21 | 21 | basilisk.png | 256x64 | 256x64 | 11 | - | glScalef(this.scale); ctor args 0.5, 1.25 |
| Camarasaurus | ModelCamarasaurus | 21 | 21 | camarasaurus.png | 256x256 | 256x256 | 17 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.65, 0.65 |
| Hydrolisc | ModelHydrolisc | 40 | 40 | hydrolisc.png | 64x128 | 64x128 | 30 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.65, 0.65 |
| VelocityRaptor | ModelVelocityRaptor | 34 | 34 | velocityraptor.png, velocityraptor2.png, velocityraptor3.png | 128x128 | 128x128 | 26 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.55, 0.75 |
| Dragonfly | ModelDragonfly | 26 | 26 | dragonfly.png | 64x64 | 64x64 | 6 | - | glScalef(this.scale); ctor args 0.3, 1.5 |
| Bee | ModelBee | 23 | 23 | Beetexture.png | 256x256 | 256x256 | 20 | - | glScalef(this.scale); ctor args 0.9, 1.1 |
| EmperorScorpion | ModelEmperorScorpion | 78 | 78 | emperorscorpion.png | 256x128 | 256x128 | 23 + helper | - | glScalef(this.scale); ctor args 0.95, 1.5 |
| Spyro | ModelSpyro | 37 | 37 | spyrotexture.png | 64x64 | 64x64 | 31 | - | glScalef(this.scale); ctor args 0.65, 0.75 |
| Baryonyx | ModelBaryonyx | 52 | 52 | Baryonyx.png | 128x128 | 128x128 | 8 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 1, 1 |
| GammaMetroid | ModelGammaMetroid | 21 | 21 | GammaMetroid.png | 256x64 | 256x64 | 8 + helper | blend | glScalef((this.scale / 2.0) or this.scale); ctor args 0.75, 0.9 |
| Cockateil | ModelCockateil | 16 | 16 | Bird1.png, Bird2.png, Bird3.png, Bird4.png +2 | 64x32 | 64x32 | 10 | - | glScalef(this.scale); ctor args 0.3, 0.75 |
| RubyBird | ModelCockateil | 16 | 16 | ? | ? | 64x32 | 10 | - | glScalef(this.scale); ctor args 0.3, 0.75 |
| Kyuubi | ModelKyuubi | 42 | 42 | Kyuubi.png | 512x256 | 512x256 | 37 | blend, rotate, translate | glScalef(this.scale); ctor args 0.1, 1 |
| Scorpion | ModelScorpion | 22 | 22 | Scorpion.png | 88x24 | 88x24 | 20 | - | glScalef(this.scale); ctor args 0.35, 0.75 |
| CaveFisher | ModelCaveFisher | 75 | 75 | CaveFisher.png | 64x32 | 64x32 | 52 | - | glScalef(this.scale); ctor args 0.35, 0.75 |
| Alien | ModelAlien | 55 | 55 | MyAlien.png | 256x128 | 256x128 | 53 | - | glScalef(this.scale); ctor args 0.35, 1.1 |
| WaterDragon | ModelWaterDragon | 24 | 24 | WaterDragon.png | 128x128 | 128x128 | 18 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.85, 1.1 |
| AttackSquid | ModelAttackSquid | 9 | 9 | AttackSquid.png | 64x32 | 64x32 | 9 | - | glScalef(this.scale); ctor args 0.25, 0.9 |
| Elevator | ModelElevator | 5 | 5 | Elevator1.png, Elevator10.png, Elevator2.png, Elevator3.png +6 | 64x64 | 64x64 | 0 | - | custom doRender scale: renderElevator: 0.75, 0.75, 0.75; renderElevator: 1.3333333333333333, 1.3333333333333333, 1.3333333333333333; renderElevator: -1.0, -1.0, 1.0 |
| Robot1 | ModelRobot1 | 27 | 27 | Robot1.png | 64x32 | 64x32 | 7 | - | glScalef(this.scale); ctor args 0.3, 1 |
| Robot2 | ModelRobot2 | 15 | 15 | Robot2.png | 256x512 | 256x512 | 11 | - | glScalef(this.scale); ctor args 1, 1 |
| Robot3 | ModelRobot3 | 19 | 19 | Robot3.png | 512x512 | 512x512 | 11 | - | glScalef(this.scale); ctor args 1, 0.5 |
| Robot4 | ModelRobot4 | 56 | 56 | Robot4.png | 512x512 | 512x512 | 39 | - | glScalef(this.scale); ctor args 1, 1 |
| Robot5 | ModelRobot5 | 11 | 11 | Robot5texture.png | 128x128 | 128x128 | 7 | - | glScalef(this.scale); ctor args 0.5, 1 |
| Kraken | ModelKraken | 111 | 111 | Kraken.png | 512x512 | 512x512 | 53 + helper | rotate, translate | glScalef((this.scale / 3.0) or this.scale); ctor args 1, 1 |
| Lizard | ModelLizard | 71 | 71 | Lizard.png, Lizard2.png, Lizard3.png | 128x128 | 128x128 | 53 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.75, 1 |
| Cephadrome | ModelCephadrome | 50 | 50 | Cephadrome.png | 512x256 | 512x256 | 43 | - | glScalef(this.scale); ctor args 1.25, 1 |
| Dragon | ModelDragon | 55 | 55 | Dragon.png, WhiteDragon.png | 256x128 | 256x128 | 49 | - | glScalef(this.scale); ctor args 1.25, 1 |
| Chipmunk | ModelChipmunk | 18 | 18 | chipmunktexture.png, chipmunktexture2.png, chipmunktexture3.png | 64x32 | 64x32 | 15 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.15, 0.9 |
| Gazelle | ModelGazelle | 34 | 34 | Gazelletexture.png | 64x64 | 64x64 | 30 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.45, 1 |
| Ostrich | ModelOstrich | 38 | 38 | Ostrichtexture.png, Ostrichtexture2.png, Ostrichtexture3.png | 256x128 | 256x128 | 33 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.55, 1 |
| TrooperBug | ModelTrooperBug | 134 | 134 | TrooperBug.png | 512x256 | 512x256 | 68 | - | glScalef(this.scale); ctor args 0.95, 1.1 |
| SpitBug | ModelSpitBug | 93 | 93 | BlisterBug.png | 512x256 | 512x256 | 38 | - | glScalef(this.scale); ctor args 0.55, 0.75 |
| StinkBug | ModelStinkBug | 50 | 50 | StinkBug.png | 64x32 | 64x32 | 33 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.35, 0.85 |
| Island | ModelIsland | 3 | 3 | Island.png | 64x32 | 64x32 | 3 | - | glScalef(this.scale); ctor args 0.25, 1 |
| IslandToo | ModelIsland | 3 | 3 | IslandToo.png | 64x32 | 64x32 | 3 | - | glScalef(this.scale); ctor args 0.25, 1 |
| CreepingHorror | ModelCreepingHorror | 26 | 26 | CreepingHorror.png | 128x128 | 128x128 | 20 | - | glScalef(this.scale); ctor args 0.45, 0.75 |
| TerribleTerror | ModelTerribleTerror | 21 | 21 | TerribleTerror.png | 119x72 | 119x72 | 15 | - | glScalef(this.scale); ctor args 0.45, 0.75 |
| CliffRacer | ModelCliffRacer | 8 | 8 | Cliffracertexture.png | 64x64 | 64x64 | 2 | - | glScalef(this.scale); ctor args 0.3, 1 |
| Triffid | ModelTriffid | 178 | 178 | Triffidtexture.png | 532x715 | 532x715 | 19 + helper | glEnable, rotate, translate | glScalef(this.scale); ctor args 0.3, 1 |
| PitchBlack | ModelPitchBlack | 101 | 101 | PitchBlacktexture.png | 512x256 | 512x256 | 95 | - | glScalef(L1->PitchBlack.getPitchBlackScale()); ctor args 1.25, 1 |
| LurkingTerror | ModelLurkingTerror | 59 | 59 | LurkingTerror.png | 256x64 | 256x64 | 57 | - | glScalef(this.scale); ctor args 0.45, 0.85 |
| Godzilla | ModelGodzilla | 71 | 71 | Godzillatexture.png | 1024x1024 | 1024x1024 | 57 | - | glScalef((this.scale / 4.0) or this.scale); ctor args 1, 2 |
| GodzillaHead | none | - | - | ? | ? | - | - | - | ctor args None, 0, 0 |
| KingHead | none | - | - | ? | ? | - | - | - | ctor args None, 0, 0 |
| QueenHead | none | - | - | ? | ? | - | - | - | ctor args None, 0, 0 |
| WormSmall | ModelWormSmall | 3 | 3 | WormSmalltexture.png | 64x32 | 64x32 | 3 | - | glScalef(this.scale); ctor args 0.1, 1 |
| WormMedium | ModelWormMedium | 8 | 8 | WormMediumtexture.png | 64x32 | 64x32 | 8 | - | glScalef(this.scale); ctor args 0.25, 1 |
| WormLarge | ModelWormLarge | 23 | 23 | WormLargetexture.png | 256x256 | 256x256 | 19 | - | glScalef(this.scale); ctor args 0.9, 1 |
| Cassowary | ModelCassowary | 12 | 12 | Cassowary.png | 64x32 | 64x32 | 9 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.5, 1 |
| GoldFish | ModelGoldFish | 16 | 16 | GoldFish.png | 64x64 | 64x64 | 7 | - | glScalef(this.scale); ctor args 0.2, 1 |
| CloudShark | ModelCloudShark | 8 | 8 | CloudShark.png | 64x64 | 64x64 | 4 | - | glScalef(this.scale); ctor args 0.5, 1 |
| LeafMonster | ModelLeafMonster | 5 | 5 | LeafMonstertexture.png | 128x128 | 128x128 | 5 | - | glScalef(this.scale); ctor args 0.65, 1 |
| EnderKnight | ModelEnderKnight | 40 | 40 | EnderKnighttexture.png | 512x512 | 512x512 | 24 | - | glScalef(this.scale); ctor args 0.3, 1 |
| EnderReaper | ModelEnderReaper | 66 | 66 | EnderReapertexture.png | 512x512 | 512x512 | 11 | - | glScalef(this.scale); ctor args 0.2, 1 |
| Beaver | ModelBeaver | 9 | 9 | Beavertexture.png | 64x32 | 64x32 | 6 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.15, 0.75 |
| Termite | ModelAnt | 20 | 20 | ? | ? | 64x32 | 14 | - | glScalef(this.scale); ctor args 0.15, 0.35 |
| Fairy | ModelFairy | 15 | 15 | fairytexture.png, fairytexture2.png, fairytexture3.png, fairytexture4.png +5 | 64x64 | 64x64 | 7 | tint | glScalef(this.scale); ctor args 0.1, 0.35 |
| Peacock | ModelPeacock | 16 | 16 | peacocktexture.png | 128x128 | 128x128 | 12 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.25, 1 |
| Rotator | ModelRotator | 3 | 3 | Rotatortexture.png | 64x32 | 64x32 | 3 | rotate | glScalef(this.scale); ctor args 0.1, 1 |
| Vortex | ModelVortex | 1 | 1 | Vortextexture.png | 256x128 | 256x128 | 0 | - | glScalef(this.scale); ctor args 0.1, 1 |
| DungeonBeast | ModelDungeonBeast | 64 | 64 | Botwtexture.png | 128x64 | 128x64 | 39 | rotate | glScalef(this.scale); ctor args 0.25, 1 |
| Rat | ModelRat | 12 | 12 | Rattexture.png | 64x64 | 64x64 | 6 | - | glScalef(this.scale); ctor args 0.1, 0.75 |
| Flounder | ModelFlounder | 6 | 6 | Floundertexture.png | 64x32 | 64x32 | 4 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.1, 1 |
| Whale | ModelWhale | 14 | 14 | Whaletexture.png | 256x256 | 256x256 | 9 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.1, 1 |
| Irukandji | ModelIrukandji | 9 | 9 | Irukandjitexture.png | 64x32 | 64x32 | 8 | - | glScalef(this.scale); ctor args 0.1, 0.25 |
| Skate | ModelSkate | 3 | 3 | Skatetexture.png | 64x32 | 64x32 | 1 | - | glScalef(this.scale); ctor args 0.1, 0.75 |
| Urchin | ModelUrchin | 17 | 17 | Urchintexture.png | 128x128 | 128x128 | 17 | - | glScalef(this.scale); ctor args 0.35, 1.25 |
| Mantis | ModelMantis | 36 | 36 | Mantistexture.png | 256x256 | 256x256 | 10 | - | glScalef(this.scale); ctor args 0.9, 1.1 |
| HerculesBeetle | ModelHerculesBeetle | 37 | 37 | Beetletexture.png | 256x256 | 256x256 | 27 | - | glScalef(this.scale); ctor args 0.99, 1.1 |
| Stinky | ModelStinky | 20 | 20 | Stinkytexture1.png, Stinkytexture10.png, Stinkytexture11.png, Stinkytexture12.png +15 | 128x64 | 128x64 | 16 | - | glScalef(this.scale); ctor args 0.75, 1 |
| Coin | ModelCoin | 1 | 1 | Cointexture.png | 512x512 | 512x512 | 1 | - | glScalef(this.scale); ctor args 0.75, 0.125 |
| TheKing | ModelTheKing | 119 | 119 | TheKingtexture.png | 2048x2048 | 2048x2048 | 112 | blend, tint | glScalef((this.scale / 4.0) or this.scale); ctor args 1.9, 2.1 |
| TheQueen | ModelTheQueen | 130 | 130 | TheQueentexture.png, TheQueentexture2.png | 2048x2048 | 2048x2048 | 115 | blend, tint | glScalef((this.scale / 4.0) or this.scale); ctor args 1.9, 2 |
| ThePrince | ModelThePrince | 35 | 35 | ThePrincetexture.png | 128x128 | 128x128 | 28 | blend, tint | glScalef(this.scale); ctor args 0.75, 0.75 |
| Molenoid | ModelMolenoid | 37 | 37 | Molenoidtexture.png | 256x256 | 256x256 | 30 | - | glScalef(this.scale); ctor args 1, 1 |
| SeaMonster | ModelSeaMonster | 23 | 23 | SeaMonstertexture.png | 256x128 | 256x128 | 21 | - | glScalef(this.scale); ctor args 1, 1 |
| SeaViper | ModelSeaViper | 34 | 34 | SeaVipertexture.png | 128x128 | 128x128 | 12 + helper | - | glScalef(this.scale); ctor args 1, 1 |
| EasterBunny | ModelEasterBunny | 13 | 13 | EasterBunnytexture.png | 64x128 | 64x128 | 6 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.5, 1 |
| CaterKiller | ModelCaterKiller | 31 | 31 | CaterKillertexture.png | 256x512 | 256x512 | 31 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 1, 1.25 |
| Leon | ModelLeon | 98 | 98 | Leon.png | 256x256 | 256x256 | 92 | - | glScalef(this.scale); ctor args 1, 1.75 |
| Hammerhead | ModelHammerhead | 37 | 37 | Hammerheadtexture.png | 222x256 | 222x256 | 30 | - | glScalef(this.scale); ctor args 1, 2.5 |
| RubberDucky | ModelRubberDucky | 8 | 8 | EvilRubberDuckytexture.png, RubberDuckytexture.png | 64x64 | 64x64 | 4 | - | glScalef((this.scale / 2.0) or this.scale); ctor args 0.15, 0.75 |
| ThePrinceTeen | ModelThePrinceTeen | 71 | 71 | PrinceTeentexture.png | 512x256 | 512x256 | 64 | blend, tint | glScalef(this.scale); ctor args 1, 1.25 |
| BandP | ModelBandP | 7 | 7 | BandPtexture.png | 64x128 | 64x128 | 6 | - | glScalef(this.scale); ctor args 1, 1 |
| RockBase | ModelRockBase | 22 | 22 | RockBluetexture.png, RockGreentexture.png, RockPurpletexture.png, RockRedtexture.png +6 | 64x64 | 64x64 | 0 | blend, tint | glScalef(this.scale); ctor args 0, 1 |
| PurplePower | ModelPurplePower | 3 | 3 | PurplePowertexture.png, PurplePowertexture10.png, PurplePowertexture2.png, PurplePowertexture3.png +1 | 64x32 | 64x32 | 3 | blend, tint, rotate | glScalef(0.550000011920929); ctor args 0.3, 2.75 |
| Brutalfly | ModelBrutalfly | 14 | 14 | Brutalfly_overlay2.png, Brutalflytexture.png | 64x32 | 64x32 | 12 | - | glScalef(this.scale); shouldRenderPass (2nd pass); ctor args 0.75, 9 |
| Nastysaurus | ModelNastysaurus | 59 | 59 | Nastysaurustexture.png | 512x256 | 512x256 | 51 | - | glScalef(this.scale); ctor args 1, 1.5 |
| Pointysaurus | ModelPointysaurus | 30 | 30 | Pointysaurustexture.png | 128x128 | 128x128 | 27 | - | glScalef(this.scale); ctor args 1, 1 |
| Cricket | ModelCricket | 11 | 11 | Crickettexture.png | 64x64 | 64x64 | 8 | - | glScalef(this.scale); ctor args 0.15, 0.5 |
| ThePrincess | ModelThePrincess | 37 | 37 | ThePrincesstexture.png, ThePrincesstexture2.png | 128x128 | 128x128 | 31 | blend, tint | glScalef(this.scale); ctor args 0.7, 0.7 |
| Frog | ModelFrog | 10 | 10 | Frogtexture.png | 64x64 | 64x64 | 7 | - | glScalef(this.scale); ctor args 0.35, 1 |
| ThePrinceAdult | ModelThePrinceAdult | 119 | 119 | TheKingtexture.png | 2048x2048 | 2048x2048 | 112 | blend, tint | glScalef(this.scale); ctor args 1.2, 1 |
| SpiderRobot | ModelSpiderRobot | 39 | 39 | SpiderRobottexture.png | 256x512 | 256x512 | 25 | - | glScalef(this.scale); custom doRender scale: renderSpiderRobot: -1.0, -1.0, 1.0; ctor args 0.99, 1 |
| SpiderDriver | vanilla ModelSpider | - | - | spiderdriver.png | 64x32 | - | - | - | ctor args 0.5 |
| GiantRobot | ModelGiantRobot | 18 | 18 | GiantRobottexture.png | 256x512 | 256x512 | 18 | - | glScalef(this.scale); ctor args 0.99, 1 |
| AntRobot | ModelAntRobot | 27 | 27 | AntRobottexture.png | 128x256 | 128x256 | 16 | - | glScalef(this.scale); custom doRender scale: renderAntRobot: -1.0, -1.0, 1.0; ctor args 0.99, 1 |
| Crab | ModelCrab | 25 | 25 | RobotCrabtexture.png | 256x512 | 256x512 | 15 | - | glScalef(L1->Crab.getCrabScale()); ctor args 0.99, 1 |
| item MyBertha (IItemRenderer RenderBertha) | ModelBertha | 12 | 12 | Berthatexture.png | 64x128 | 64x128 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MySlice (IItemRenderer RenderSlice) | ModelSlice | 14 | 14 | Slicetexture.png | 64x128 | 64x128 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MyRoyal (IItemRenderer RenderRoyal) | ModelSlice | 14 | 14 | Royaltexture.png | 64x128 | 64x128 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MySquidZooka (IItemRenderer RenderSquidZooka) | ModelSquidZooka | 12 | 12 | SquidZookatexture.png | 128x128 | 128x128 | 0 | rotate | glScalef in renderSword/renderSwordF5 (per render type) |
| item MyHammy (IItemRenderer RenderHammy) | ModelHammy | 33 | 33 | AttitudeAdjustertexture.png | 128x256 | 128x256 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MyBattleAxe (IItemRenderer RenderBattleAxe) | ModelBattleAxe | 15 | 15 | BattleAxetexture.png | 128x64 | 128x64 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MyChainsaw (IItemRenderer RenderChainsaw) | ModelChainsaw | 8 | 8 | Chainsawtexture.png | 64x64 | 64x64 | 2 | - | glScalef in renderSword/renderSwordF5 (per render type) |
| item MyQueenBattleAxe (IItemRenderer RenderQueenBattleAxe) | ModelQueenBattleAxe | 9 | 9 | QueenBattleAxetexture.png | 128x64 | 128x64 | 0 | - | glScalef in renderSword/renderSwordF5 (per render type) |

Item icons: all 473 items and 211 blocks use flat 16px-style icons from `textures/items|blocks` (sizes in `items_blocks.txt`); the 8 items above additionally have a 3D `IItemRenderer`. Armor layers: section 8.

## 14. 20.2 decompiled source vs 20.3 jar

Reference tree: `github.com/history-craft/OreSpawn`, commit `4f800e6` (2019-03-26), cloned to `scratchpad/orespawn/src-20.2/` (586 `.java`, 1370 resource files). Handed over as "20.2"; the checks below say it is a decompile of the **same 20.3 build**. Details: `jar/diff_20.2_vs_20.3.txt`, `jar/model_validation_vs_src.txt`.

| check | result |
|---|---|
| class list | identical: 586 top-level classes on both sides; the jar's 8 extra entries are the anonymous `Render*$1` classes of the 8 item renderers |
| version | source `@Mod(version = "1.7.10.20.3")` and `getVersion()` = `"1.7.10.20.3"`, same as the jar; `mcmod.info` says `1.7.10-20.2` in both (jar 322 bytes LF, source 338 bytes CRLF, otherwise identical) |
| resources | 1369 of 1370 files byte-identical (SHA-1); only `mcmod.info` differs (line endings); `META-INF/MANIFEST.MF` only in jar |
| string literals per class | identical for all 586 classes; the only reported difference (`OreSpawn`, proxy class names in `OreSpawnMain`) are annotation values, which the jar stores in annotation attributes, not in the constant pool |
| declared methods | 179 classes report name differences; the inspected cases are artefacts: `@SideOnly`-annotated methods (the regex captured `SideOnly`), and SRG names the source leaves unmapped (`func_149865_P`, `func_149866_i`, `func_147416_a`) while the jar side was mapped to MCP (`getCrop`, `getSeed`, `findClosestStructure`). No method was found that exists on one side only once these are accounted for, but this was not proven for every one of the 179 classes |
| declared fields | 2 classes (`MyEntityAITarget`, `UltimateFishHook`): the source keeps SRG field names (`field_75298_g`...), the jar side maps them to MCP names - naming only |
| model geometry | 109 of 109 models, 3584 of 3584 parts identical: texture offset, box origin/size, pivot (incl. constructor `rotationPointY += 30` style adjustments), initial rotation, declared texture size |
| config | 128 direct `config.get` keys with identical defaults; all helper call defaults identical (59 mob, 15 weapon, 13 ore, 14 armor stats) |
| client proxy | 133 entity renderer registrations identical (renderer class, model class, all numeric args; Robot1..5 first flagged by a regex that read the digit in the class name, confirmed identical by eye) |
| GL11 in models | identical call sets in all 19 model classes that use GL11 |

Conclusion: no behavioural difference found. The source tree can be read as documentation of 20.3; the jar stays the reference for any value.

## 15. Method notes

| value | how it was extracted | confidence |
|---|---|---|
| provenance | download from Wayback raw capture of orespawn.com; SHA-1 compared with three independent archive.org uploads (`archive.org/metadata/<id>` file sha1) | high |
| class structure, annotations | own stdlib Python class-file parser (`jar/cf.py`): constant pool, fields, methods, `RuntimeVisibleAnnotations`; nothing from the jar is executed | high |
| registrations, config, materials, names, spawns | linear symbolic evaluation of `OreSpawnMain` bytecode (`cf.symbolic_calls`): every invoke with its argument expressions; helper methods expanded by substituting call-site literals; spawn guards = `getstatic <X>Enable` + `ifeq` ranges | high for literals; guards only capture the `Enable` flag |
| config defaults | literal arguments of `Configuration.get`; cross-checked 1:1 against decompiled source | high |
| entity HP / attack / speed | `setBaseValue` arguments inside `applyEntityAttributes` resolved through constructor constants, no-arg method returns (`mygetMaxHealth`) and `MobStats` config defaults; several values = code branches; formulas printed verbatim when runtime-dependent | high for constants |
| SRG -> MCP names | MCP `mcp_stable-12-1.7.10.zip` (`maven.minecraftforge.net`): `fields.csv`, `methods.csv`, `params.csv`. Checked names include `field_111267_a` maxHealth, `field_111264_e` attackDamage, `field_111263_d` movementSpeed, `func_110148_a` getEntityAttribute, `func_111128_a` setBaseValue, `func_110147_ax` applyEntityAttributes, `func_70105_a` setSize, `func_70658_aO` getTotalArmorValue, `func_70601_bi` getCanSpawnHere, `func_70619_bc` updateAITasks, `func_75776_a` addTask, `func_78789_a`/`func_78790_a`/`func_78786_a` addBox, `func_78793_a` setRotationPoint, `func_78787_b` setTextureSize, `func_78784_a` setTextureOffset, `func_78792_a` addChild, `field_78795_f`/`_g`/`_h` rotateAngleX/Y/Z, `field_78809_i` mirror, `field_78090_t`/`field_78089_u` textureWidth/Height, `func_77041_b` preRenderCallback, `func_77032_a` shouldRenderPass, `func_80007_l` getDimensionName, `func_94581_a` registerIcons | high |
| vanilla 1.7.10 semantics | Mojang `1.7.10` client.jar (SHA-1 `e80d9b3b...`, matches launcher manifest) disassembled with MCP `joined.srg`: `ModelBase.<init>`, `ModelRenderer.<init>/addBox/setTextureSize/render/compileDisplayList`, `ModelBox.<init>/render`, `TexturedQuad.<init>/draw`, `RendererLivingEntity.doRender` | high |
| model geometry | concrete bytecode interpreter `jar/modelvm.py` running each model constructor (and its helper methods) against a Python model of the vanilla state above; constructor args taken from the `new Model*(...)` sites in the jar. Validated with `javap -c` spot checks (ModelGodzilla `LToe1`, ModelKraken `Lefteye`/`Backbody`, ModelAnt `thorax`/`thorax1`) and a full comparison against the decompiled constructors (3584 of 3584 parts) | high |
| animation overview | symbolic pass over every non-constructor model method: writes to part fields, MathHelper/Math calls, GL11 calls, parameter reads, bytecode length; GL11 sets cross-checked with the source (19 of 19) | medium: shows *what* is animated, not the exact curves - read the source for those |
| renderer textures and scaling | `ResourceLocation` literals in renderer classes plus all `.png` string literals in renderer and entity classes (variants are picked at runtime); `glScalef` arguments from renderer methods; PNG sizes from the IHDR chunk | high for names; which variant is used when is runtime logic |
| item/block icons | `registerIcons` string (`"OreSpawn:" + getUnlocalizedName().substring(5)`), resolved against the jar file list | high |
| 1.21.1 conventions | `neoforge-21.1.248-sources.jar` from the repo toolchain cache (`mods/armature/build/moddev/artifacts/`): `ModelPart`, `CubeListBuilder`, `CubeDefinition`, `CubeDeformation`, `LayerDefinition`, `PartPose`, `EntityModel`, `HierarchicalModel`, `LivingEntityRenderer` | high |
| GeckoLib availability | `tools/check.py --mc 1.21.1 --loader neoforge geckolib` -> `4.9.2` | high (Modrinth metadata) |

Nothing was launched or rendered; all statements about runtime look are derived from code.

## 16. Open questions

- **Spawn conditions beyond the enable flag**: 103 entity classes override `getCanSpawnHere`; their rules (dimension, light, height, rarity rolls) are not tabulated - read per class in `danger.orespawn.<Entity>`.
- **Entity IDs** are assigned at runtime (`findGlobalUniqueEntityId`); any numeric ID in a wiki is install-specific.
- **Runtime-scaled stats**: Nightmare (`PitchBlack`) and `Crab` compute HP/attack/speed from `getPitchBlackScale()` / `getCrabScale()`; the scale logic lives in those entity classes. `Crab` uses `PitchBlack_stats.health` for its HP (bytecode and source agree).
- **"inherited" attributes** (cows, `SpiderDriver`, islands, `RockBase`) come from the vanilla parent and were not read from the vanilla jar.
- **Branching values** (e.g. Girlfriend HP 800 / 80, Mobzilla size 9.9x25 / 2.475x6.25) depend on conditions such as PlayNicely or tamed state; the condition per branch is in the entity class.
- **Recipes** (189 shaped, 176 shapeless, 16 smelting) and chest loot are in `calls_OreSpawnMain.txt` but not tabulated here.
- **5 boxes whose UV rectangle leaves the declared texture** (see 06) - harmless in 1.7.10 (wraps/clamps at runtime), check visually after porting.
- **Entity culling for giants** in 1.21.1 (22x24 King/Queen hitboxes, rendered geometry far larger) was not verified.
- Other captured builds (`orespawn164v20a.zip` for 1.6.4, `orespawnmc_1.12-development_0.1..0.7.jar`) were not analysed.
