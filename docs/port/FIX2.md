# FIX2 – Fixes aus BUGHUNT2

Stand 2026-09-14. Grundlage: `docs/port/BUGHUNT2.md` Abschnitt 2, Entscheidungen unter R26. Drei Porter-Abschnitte, danach der Integrationsabschnitt.

## fix2-items-blocks (BUGHUNT2 2.1, 2.3, 2.4, Suchhinweise 4.2 und 4.3)

Kein Gradle-Lauf (Porter-Regel). Alle Signaturen in den 1.21.1-Quellen nachgesehen.

### Geändert

| Befund | Dateien | Was |
|---|---|---|
| 2.1 BlockReed-Grasfärbung | `client/BlockRenderTypes.java`, `models/block/{corn,tomato,lettuce,quinoa}_{0..3}.json`, `experiencesapling.json`, `island.json`, `kingspawner.json`, `queenspawner.json`, `dungeonspawner.json` | 21 Modelle auf `minecraft:block/tinted_cross` (`render_type cutout` bleibt), dieselben 21 Blöcke im Gras-Lambda der Kristallsetzlinge. Keine Item-Farbe (`item/generated`, wie bei den Setzlingen). Alle neun Originalklassen `extends BlockReed` ohne Farb-Override, nachgesehen. |
| 2.3 Miner's Dream / `#c:ores` | neu `data/c/tags/block/ores.json`, `data/c/tags/block/ores/{ruby,amethyst,titanium,uranium,salt}.json`; Kommentar in `item/utility/ItemMinersDream.java` | `#c:ores` = die fünf Untertags + `crystalcoal`, `crystalcrystal`, `tigerseye` + die vier Mob-Steine `crystalrat`, `crystalfairy`, `redanttroll`, `termitetroll` + alle 119 `DRIED_EGG_IDS`. Ids per Skript gegen `ModBlocks`/`OreGenericEgg` geprüft (131 Einträge). `blockenderpearl`/`blockeyeofender` sind Speicherblöcke und fehlen bewusst (wie `blockruby`). |
| 2.3 Rose Sword | `tools/gen_recipes.py`, neu erzeugt `recipe/rosesword.json` | `WILDCARD_CATEGORIES`: die `red_flower`-Liste wird `{"type":"neoforge:difference","base":{"tag":"minecraft:small_flowers"},"subtracted":{"item":"minecraft:dandelion"}}`, dieselbe Regel wie `CompanionSupport.isRedFlower` (also auch Kornblume, Maiglöckchen, Wither Rose, Torchflower). |
| 2.3 Elevator | `tools/gen_recipes.py`, neu erzeugt `recipe/elevator.json` | Plankenliste wird `{"tag":"minecraft:planks"}`. Eine unbekannte Mehrfach-Liste bricht den Generator jetzt ab. Neulauf: 328 Dateien, Diff gegen vorher genau diese zwei Dateien. |
| 2.4 RTP-Block | `block/misc/RTPBlock.java` | Zielwurf bleibt in `stepOn` (gleiche Zufallsfolge), Teleport, Partikel und Sound laufen als `MinecraftServer.tell(new TickTask(tick + 1, ...))` nach dem Move-Handler. Ein schwaches `PENDING`-Set je `ServerPlayer` verhindert, dass weitere schon eingereihte Move-Pakete ein zweites Ziel würfeln. Der Task bricht ab, wenn der Spieler entfernt ist oder die Welt gewechselt hat. |
| 2.4 Ackerboden | neu `data/minecraft/tags/block/maintains_farmland.json` | `radish_plant`, `strawberry_plant`, `rice_plant`, `butterfly_plant`, `moth_plant`, `mosquito_plant`, `firefly_plant` (Ids gegen `ModBlocks` geprüft; alle sieben Originale `extends BlockCrops`, also `EnumPlantType.Crop`). BlockReed-Pflanzen nicht (Forge-1.7.10-`BlockReed` ist `Beach`), `MyBlockFlower` nicht (`Plains`, MyBlockFlower.java:97-99). |
| 2.4 Sky Tree | `block/tree/BlockSkyTreeLog.java` | R26: abgebauter Block `UPDATE_ALL`, rekursive Stammblöcke `UPDATE_CLIENTS \| UPDATE_KNOWN_SHAPE`. |
| 2.4 Kristallofen | `block/entity/TileEntityCrystalFurnace.java` | Kategorie „Material.wood" über den 1.21.1-Ersatz belegt: in `Blocks.java` jeder Block mit `instrument(BASS)` **und** `ignitedByLava()` (skriptgestützt, alle 197 BASS-Blöcke durchgesehen, dazu die `log(...)`- und `legacyStair(...)`-Helfer). Neu 300 Ticks: `#minecraft:bamboo_blocks`, `#minecraft:banners`, Bamboo Mosaic und Treppe, Mangrove Roots, Barrel, Lectern, Composter, Loom, Cartography/Fletching/Smithing Table, Chiseled Bookshelf, Bee Nest, Beehive, Campfire, Soul Campfire. Bamboo Mosaic Slab: 150 (Holzstufe, Zweig :228). Weiter ausgeschlossen wie bisher: Schilder, Hängeschilder, Türen (keine `ItemBlock`s in 1.7.10), Knöpfe (`Material.circuits`, auch in 1.21.1 kein BASS), Nether-Holz. |

### 4.2 und 4.3

- **4.2:** Außer `#c:ores` und `#minecraft:maintains_farmland` nichts ergänzt. Geprüft: `sword_efficient`
  (1.7.10 `ItemSword` 1.5 auf `Material.plants/leaves`): Laub steckt schon über `#minecraft:leaves`
  darin, alle übrigen Pflanzen haben Härte 0, also keine sichtbare Wirkung. `crops`/`bee_growables`,
  `saplings`, `flowers`, `climbable`, `replaceable`, `dirt`: kein 1.7.10-Verhalten, das daran hing
  (Bienen und Feldarbeit der Dorfbewohner gab es nicht; `CrystalGrass.canSustainPlant` ist schon als
  `TriState.TRUE` portiert; `BlockBush` prüfte Erde per Identität). `c:ingots`/`c:gems`: das Original
  registriert nichts im OreDictionary (grep ohne Treffer).
- **4.3:** Suche nach `connection.teleport`, `teleportTo(` und `changeDimension` in `block/` und `item/`:
  nur der RTP-Block. Die übrigen `stepOn`/`entityInside` (BlockRuby, OreSalt, OreTitanium, Lavafoam,
  MoleDirtBlock) setzen Effekte oder Geschwindigkeit, keine Position. Die Dimensionsreisen hängen an
  `mobInteract` bzw. Entity-Code (fix2-mobs), nicht am Move-Handler.

### Offen / für den Integrator

- **`W02GameTests.rtpBlockRelocatesAServerPlayer` schlägt nach dem Fix fehl**, weil der Test direkt nach
  `stepOn` misst. Umbauen: nach `stepOn` mit `helper.runAfterDelay(2, ...)` messen; den Schleich-Teil
  mit einem zweiten Mock-Spieler prüfen, `player.remove` erst am Ende.
- Mob-Steine in `#c:ores`: abgeleitet aus dem Originalverhalten (Miner's Dream :72 ließ sie stehen).
  Andere Mods sehen sie damit als Erz. Falls unerwünscht, in DECISIONS festhalten.

### Gewünschte GameTests (`Fix2GameTests`)

1. **`blockReedPlantsAreGrassTinted`** (Client-Farben sind im GameTest nicht prüfbar): die 21 Modelle
   `assets/orespawn/models/block/<id>.json` vom Classpath lesen und
   `parent == "minecraft:block/tinted_cross"` verlangen. Vorher: `block/cross`.
2. **`minersDreamKeepsOreSpawnOres`**: in einer Steinwand auf Fußhöhe `oreruby`, `crystalcoal`,
   `tigerseye`, `crystalrat`, `orespider`, daneben Diamanterz als Kontrolle und normaler Stein; Miner's
   Dream wie im Fix1-Test benutzen; alle fünf OreSpawn-Blöcke stehen noch, der Stein ist weg. Zusätzlich
   `state.is(Tags.Blocks.ORES)` für alle 131 Einträge. Vorher: die OreSpawn-Erze verschwinden.
3. **`roseSwordAcceptsCornflowerAndLilyOfTheValley`**: `RecipeManager.getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, ...))`
   mit dem Rose-Sword-Muster aus Kornblumen, dann aus Maiglöckchen: `orespawn:rosesword`; mit Löwenzahn
   kein Treffer. Vorher: Kornblume ohne Treffer.
4. **`elevatorAcceptsCherryAndCrimsonPlanks`**: Elevator-Muster (`WWW`/`DRD`) mit `cherry_planks`, dann
   `crimson_planks`: `orespawn:elevator`; mit `crystalplanks` kein Treffer (R18).
5. **`rtpBlockTeleportSurvivesTheMoveHandler`**: Mock-Spieler neben dem RTP-Block, Boden in den
   Zielquadraten wie im W02-Test. Das ausstehende Login-Teleport quittieren
   (`ServerboundAcceptTeleportationPacket` mit der ID aus `awaitingTeleport`, notfalls per Reflection), dann
   `player.connection.handleMovePlayer(new ServerboundMovePlayerPacket.Pos(x + 0.5, y + 1, z + 0.5, true))`
   auf den Block; nach 2 Ticks steht der Spieler 9..23 Blöcke entfernt. Vorher: der Handler setzt ihn
   zurück („moved wrongly!"). Ein Test, der nur `stepOn` aufruft, sieht den Fehler nicht.
6. **`oreSpawnCropsKeepFarmlandWet`**: für die sieben Pflanzen `state.is(BlockTags.MAINTAINS_FARMLAND)`;
   funktional: trockener Acker (`MOISTURE 0`, kein Wasser in 4 Blöcken), Rettich darauf,
   `FarmBlock.randomTick` 20-mal direkt aufrufen: der Acker bleibt Acker. Kontrolle: `corn_0` nicht im Tag.
7. **`skyTreeCrownFloatsButRailDrops`**: Stamm aus drei `skytreelog` übereinander, eine Schiene auf dem
   obersten, Eichenlaub (`persistent=false`, `distance=1`) seitlich am untersten, Sand über Luft seitlich am
   mittleren (mit `setBlock(..., Block.UPDATE_CLIENTS)` gesetzt, damit er nicht sofort fällt). Den **obersten** Block per
   `player.gameMode.destroyBlock` abbauen, 5 Ticks warten: die Schiene ist gedroppt (Nachbar des abgebauten
   Blocks), Laub und Sand an den rekursiv entfernten Blöcken stehen noch. Vorher: Laub zerfällt, Sand fällt.
8. **`crystalFurnaceBurnsModernWoodBlocks`**: `TileEntityCrystalFurnace.getItemBurnTime` = 300 für
   Barrel, Lectern, Composter, Loom, Cartography/Fletching/Smithing Table, Chiseled Bookshelf, Bamboo Mosaic,
   Bamboo Mosaic Stairs, Bamboo Block, Mangrove Roots, Beehive, Bee Nest, Campfire, White Banner; 150 für
   Bamboo Mosaic Slab; 0 für Crimson Planks, Oak Sign, Oak Door, Oak Button. Vorher: 0 für die neuen.

## fix2-world (BUGHUNT2 2.2 und strittiger Punkt getBaseColumn)

### (a) Rückkehr-Teleporter landet auf Y -63
`OreSpawnTeleporter.searchTop` liest die Höhe jetzt über `world.getChunk(cx, cz).getHeight(WORLD_SURFACE, x & 15, z & 15) + 1`, begrenzt auf `getMaxBuildHeight()`. `getChunk` lädt bzw. erzeugt den Chunk (FULL) wie `world.getBlock` in 1.7.10; `Level.getHeight` lieferte bei nicht geladenem Chunk `getMinBuildHeight()`. Der Klassen-Javadoc ist korrigiert. In den sechs OreSpawn-Dimensionen bleibt der wörtliche Start bei 180.

### (b) Biome nach R26
- `docs/catalog/biome_map.json`: `hell` → `#minecraft:is_nether`; `ocean` → ocean, lukewarm_ocean, warm_ocean, cold_ocean; `deepOcean` → deep_ocean, deep_lukewarm_ocean, deep_cold_ocean; `swampland` → swamp, mangrove_swamp; `jungle`/`jungleHills` → jungle, bamboo_jungle. Frozen-Ozeane bleiben draußen. Neuer Abschnitt `legacy_names` (Tag → 1.7.10-Biomfelder, deren `biomeName` OreSpawnWorld verglich).
- `tools/gen_spawns.py` erlaubt Tag-Ziele (`#ns:path`, geprüft), schreibt zusätzlich die zwölf `legacy_name`-Tags aus derselben Karte und löscht veraltete Tag-Dateien. Neu erzeugt: `spawns.json` mit 37 Biomen + 1 Tag, 510 Einträge (vorher 31 Biome, 451), 79 wegfusioniert (vorher 65). `nether_wastes` ist als Schlüssel entfallen, die zwei Einträge (Kyuubi, Stinky) stehen unter `#minecraft:is_nether`. Die neuen Biome tragen exakt die Listen ihres Stamm-Bioms (geprüft: warm_ocean == alter ocean, bamboo_jungle == alter jungle). `legacy_name/ocean.json`, `swampland.json`, `jungle.json` erweitert, die übrigen neun unverändert. `config_spawns.json` byte-gleich.
- `SpawnTable`: Schlüssel werden als String gelesen; `#`-Schlüssel werden zu `TagKey<Biome>`. Neu `entries(Holder<Biome>)` (Id-Schlüssel plus jeder passende Tag; greifen mehrere, gilt die R18-Regel zur Laufzeit: je Entity/Kategorie/Flags/Datum gewinnt der Schlüssel mit dem größten Eintrag nach (weight, max, min), bei Gleichstand der frühere), `entries(TagKey)`, `tags()`. `entries(ResourceLocation)` und `biomes()` liefern weiter nur Id-Schlüssel, damit `W13WriterGameTests.configSpawnsAddsEveryEntryOnlyWithAllItsEnableFlags` unverändert gilt.
- `ConfigSpawnsBiomeModifier.modify` nutzt `entries(biome)`.
- `LegacyBiomeNames`: nur Javadoc (Konstanten und Tag-Ids unverändert).

### getBaseColumn-Cache (R26)
Neu `world/dimension/ChunkTerrainCache` (LRU, 32 Chunks, synchronisiert, Laden außerhalb der Sperre; Wettläufe ergeben identische Arrays). Je Seed ein Cache im `Terrain`/`Noises`-Objekt von `ChunkProviderOreSpawn` (erbt VillageMania), `2`, `5`, `6`. Benutzt von `getBaseColumn`, in 2/5/6 auch von `getBaseHeight` (gleicher Pfad `columnChunk` bzw. dasselbe Array). `fillFromNoise` bleibt ungecacht. Gleiche Blöcke, weil `generateTerrain` eine reine Funktion der Chunk-Koordinaten ist.

### Gewünschte GameTests (für Fix2GameTests)
1. `teleporterReturnFromNeverLoadedChunkLandsOnSurface`: Spieler in einer Spalte außerhalb geladener Chunks (20000, 20000; vorher `hasChunk` == false prüfen), Rücksuche; danach Y > `getMinBuildHeight() + 2`, fester Boden, Füße und Kopf Luft. Vor dem Fix Y = -63.
2. `netherTagBiomesGetKyuubiAndStinky`: `ConfigSpawnsBiomeModifier.modify` für die fünf Nether-Biome bei gesetzten Schaltern: Kyuubi und Stinky vorhanden (OreSpawnMain.java:4462/:4465). Vor dem Fix fehlten sie in vier der fünf.
3. `splitOceanSwampJungleBiomesGetTheirParentSpawns`: dasselbe für warm/lukewarm/cold ocean (:4506-4531), deep_cold_ocean (Whale :4366), mangrove_swamp (Hydrolisc :4489), bamboo_jungle; negativ `frozen_ocean` ohne OreSpawn-Einträge.
4. `legacyNameTagsCoverSplitBiomes`: WARM_OCEAN in OCEAN, MANGROVE_SWAMP in SWAMPLAND, BAMBOO_JUNGLE in JUNGLE; DEEP_OCEAN und FROZEN_OCEAN nicht in OCEAN.
5. `spawnTableMergesIdAndTagKeysByR18`: nur mit Test-Datapack sinnvoll, sonst weglassen.
6. `baseColumnCacheReturnsSameBlocks`: für Utopia, Mining, Crystal, Chaos wiederholte `getBaseColumn`-Aufrufe blockweise gleich.

## fix2-mobs (BUGHUNT2 2.5, 2.6, 2.7, Suchhinweise 4.4, 4.5)

### Behoben

| Befund | Fix | Dateien |
|---|---|---|
| 2.5 Babys bei natürlichen Spawns | Neuer gemeinsamer Helfer `entity/LegacyAgeable.noBabies` (aus `CritterSupport` verschoben). `finalizeSpawn`-Override in **jeder** OreSpawn-`AgeableMob`-Basisklasse, nicht nur in den gelisteten: Flounder, Frog, GoldFish, Whale, ThePrince, ThePrincess, ThePrinceTeen, ThePrinceAdult, EntityCannonFodder (deckt Chipmunk, Lizard, Ostrich, VelocityRaptor), Gazelle, Boyfriend, Girlfriend, RedCow (deckt Gold/Enchanted/Crystal Cow), Dragon, Spyro, EasterBunny, Baryonyx, Beaver, Camarasaurus, Cassowary, Hydrolisc, Peacock, StinkBug, Island, IslandToo, Leon, RubberDucky, Stinky, EntityAnt (deckt Red/Rainbow/Unstable Ant, Termite), GammaMetroid, WaterDragon; die sechs W06-Critter (RubyBird über Cockateil) rufen jetzt denselben Helfer. Liste per Vererbungsscan über `entity/` erstellt (alle Klassen mit Kette nach `Animal`/`TamableAnimal`/`Cow`). Auch nicht natürlich spawnende Typen haben den Override: ein Einzelspawn wird ohnehin nie Baby, der Override ändert dort nichts, und Spawner-Blöcke/`/summon` bleiben so wie in 1.7.10. Züchten läuft nicht über `finalizeSpawn` und bleibt unberührt. | `entity/LegacyAgeable.java`, 37 Entity-Dateien |
| 2.5 Velocity-Raptor-Tempo | Neue Klasse `entity/rider/VelocityRaptorSpeed` (`@EventBusSubscriber`, keine Registrierung nötig): `set` merkt sich beim Setzen des Modifiers den `isSprinting`-Zustand, `PlayerTickEvent.Post` (Server) entfernt den Modifier beim ersten abweichenden Zustand (R26). `RiderSupport.setVelocityRaptorSpeed` delegiert. Modifier-ID unverändert `orespawn:velocity_raptor_speed`, jetzt `VelocityRaptorSpeed.ID` (public). | `VelocityRaptorSpeed.java`, `RiderSupport.java` |
| 2.6 Königssalven treffen den eigenen Kopf | `LegacyThrowable.canHitCandidate(Entity)` (Default `true`) im Kandidatenfilter; `ThunderBolt` und `IceBall` schließen `KingHead`/`QueenHead` aus (`LegacyProjectiles.isRoyalHead`). GodzillaHead nicht: Mobzilla schießt weder ThunderBolt noch IceBall. | `LegacyThrowable.java`, `LegacyProjectiles.java`, `ThunderBolt.java`, `IceBall.java` |
| 2.6 Y-230-Deckel | Ein Helfer `entity/boss/RoyalCeiling.of(level)`: 230 in den sechs OreSpawn-Dimensionen, sonst `getMaxBuildHeight() - 26`. Alle acht Stellen in TheKing/TheQueen umgestellt, je mit `PORT:`. | `RoyalCeiling.java`, `TheKing.java`, `TheQueen.java` |
| 2.7 Alt-Taste nach Dimensionswechsel | `RiderControl.onClone` setzt `keystate = -1`, der nächste Tick sendet den echten Zustand. | `client/input/RiderControl.java` |

### Suchhinweis 4.4 (Attribut-Modifier ohne Entfernen)

`addTransientModifier`/`addOrUpdateTransientModifier`/`addPermanentModifier` in `src/main/java`: nur EnderKnight und EnderReaper (beide mit `removeModifier` davor, wie Vanilla-Enderman) und der Raptor. Kein weiterer Treffer. Im Original setzt nur `VelocityRaptor` (:259, :313; :283 im else-Zweig) clientseitig `setBaseValue` auf den Spieler.

### Suchhinweis 4.5 (Köpfe fangen Projektile ab) - ein echter Treffer

`BetterFireball.fireballTick` (der absichtlich nachgebaute, **ungefilterte** Vanilla-Pass `ze.h()`) nahm jeden pickable Kandidaten. Die Mündung von `firecanon` liegt beim King/bei der Queen (32 vor, 14 hoch) in der Kopfbox (30 vor, 12 hoch, 19,9 x 10) und bei Mobzilla (22 vor, 19 hoch) in der um 0,3 vergrößerten Kopfbox (17 vor, 16 hoch, 9,9 x 10). Jeder Feuerball traf also den Kopf (Austrittsfläche via `calculateIntercept`), `hurt` wird vom Kopf ignoriert, dann Explosion an der Mündung und `discard`. Die Aussage in BUGHUNT2 2.6 „Feuerbälle kommen an" stimmt nicht: BetterFireballs eigener Filter läuft erst im zweiten Pass. Fix: `fireballTick` überspringt `LegacyProjectiles.isRelocatedHead` (King-, Queen-, GodzillaHead). Andere Projektile (LegacyArrow, Steine, Laser der Spieler) werden nicht aus Köpfen abgefeuert, dort ist der Kopftreffer die gewollte R25-Hitbox.

### Gewünschte GameTests (Fix2GameTests)

1. `naturalGroupSpawnsHaveNoBabies`: für mehrere Typen je 100 Entities, `finalizeSpawn(..., NATURAL, groupData)` mit der jeweils zurückgegebenen Gruppendaten-Instanz verketten; kein `isBaby()`.
2. `velocityRaptorSpeedDropsOnSprintToggle`: gezähmter Raptor, Sitz-Umschalten mit leerer Hand → 0.3; Sprint-Wechsel und `VelocityRaptorSpeed.expireIfSprintChanged` → Modifier weg, 0.1.
3. `kingThunderSalvoReachesTarget`: ThunderBolt/IceBall in der Kopfbox, ein Tick, `isAlive()` bleibt true.
4. `royalFireballSkipsOwnHead`: dasselbe für BetterFireball, auch Mobzilla/GodzillaHead.
5. `royalCeilingFollowsBuildHeight`: `RoyalCeiling.of(overworld) == 294`, `RoyalCeiling.of(utopia) == 230`.
6. RiderControl ist clientseitig, kein GameTest möglich; Beleg nur im Code (`-1` passt auf keinen gesendeten Wert).

### Offen

- 1.7.10 verlor das Raptor-Tempo auch beim Dimensionswechsel (neuer Client-Spieler). R26 nennt nur den Sprint-Wechsel, deshalb nur der umgesetzt.
- Importreihenfolge in den 37 Dateien ist nicht sortiert (die neuen Importe stehen oben), rein kosmetisch.

## Integration (fix2)

### Zusammengeführt

- Die drei Porter-Abschnitte stehen oben; fix2-world und fix2-mobs kamen als Registry-Einträge und wurden hier eingefügt, der Kopf der Datei ist neu.
- Keine Holder-, Registry- oder Mod-Konstruktor-Änderungen nötig: `VelocityRaptorSpeed` ist ein `@EventBusSubscriber` (Game-Bus), `ChunkTerrainCache`, `LegacyAgeable`, `RoyalCeiling` sind reine Helfer.
- Der erste Gradle-Lauf über alle Porter-Dateien war sofort grün, es gab keine Compile-Fehler zu beheben.
- `W02GameTests.rtpBlockRelocatesAServerPlayer` umgebaut: gemessen wird nach `runAfterDelay(2, ...)`, die Schleich-Prüfung läuft mit einem zweiten Mock-Spieler (der erste steht schon in `RTPBlock.PENDING`).
- Neu `gametest/Fix2GameTests.java` mit 16 Tests, eine je Fix:

| Befund | Test | Batch |
|---|---|---|
| 2.1 Grasfärbung | `blockReedPlantsAreGrassTinted` (21 Modelle, Parent `tinted_cross`) | `fix2_static` |
| 2.2 Teleporter Y -63 | `teleporterReturnFromNeverLoadedChunkLandsOnSurface` (Spalte 20000/20000, `hasChunk` vorher false) | `fix2_teleporter` |
| 2.2 Biome | `netherTagBiomesGetKyuubiAndStinky`, `splitOceanSwampJungleBiomesGetTheirParentSpawns` (inkl. frozen_ocean leer), `legacyNameTagsCoverSplitBiomes` | `fix2_static` |
| R26 getBaseColumn-Cache | `baseColumnCacheReturnsSameBlocks` (Cache-Vertrag; Utopia/Mining/Crystal/Chaos: kalt = gecacht = nach Verdrängung neu berechnet) | `fix2_base_column` |
| 2.3 Miner's Dream | `minersDreamKeepsOreSpawnOres` (131 Einträge in `#c:ores`, sechs OreSpawn-Erze bleiben im Tunnel stehen) | `fix2_miners_dream` |
| 2.3 Rezepte | `roseSwordAcceptsCornflowerAndLilyOfTheValley`, `elevatorAcceptsCherryAndCrimsonPlanks` | `fix2_static` |
| 2.4 RTP-Block | `rtpBlockTeleportSurvivesTheMoveHandler`: echter `handleMovePlayer` im Überlebensmodus (Login-Teleport per Reflection auf `awaitingTeleport` quittiert, Paket 0,01 in den Block, damit `verticalCollisionBelow` und damit `stepOn` greift) | `fix2_rtp` |
| 2.4 Ackerboden | `oreSpawnCropsKeepFarmlandWet` (Tag + 20 Random Ticks auf trockenem Acker) | `fix2_farmland` |
| 2.4 Sky Tree | `skyTreeCrownFloatsButRailDrops` | `fix2_sky_tree` |
| 2.4 Kristallofen | `crystalFurnaceBurnsModernWoodBlocks` | `fix2_static` |
| 2.5 Babys | `naturalGroupSpawnsHaveNoBabies` (Girlfriend, Apple Cow, Baryonyx, Velocity Raptor, Ant, Whale; je 100 verkettet) | `fix2_babies` |
| 2.5 Raptor-Tempo | `velocityRaptorSpeedDropsOnSprintToggle` | `fix2_raptor` |
| 2.6 Salven | `royalSalvoLeavesTheHeadBox` (ThunderBolt, IceBall) | `fix2_royal_salvo` |
| 4.5 Feuerbälle | `royalFireballSkipsOwnHead` (King-, Queen-, Mobzilla-Kopf) | `fix2_royal_fireball` |
| 2.6 Salven, Ende zu Ende | `kingThunderSalvoReachesTargetFortyBlocksAway` (nachgetragen vom Testlauf, siehe unten) | `fix2_king_salvo` |
| 2.6 Y-Deckel | `royalCeilingFollowsBuildHeight`, `kingWaypointAboveY230InTheOverworld` (nachgetragen) | `fix2_static`, `fix2_king_ceiling` |
| 2.7 Alt-Taste | kein Test (clientseitig), Beleg im Code | - |

### Abweichungen von den Testwünschen der Porter

- Sky Tree: Statt Sand über Luft prüft der Test eine **Wandfackel** am mittleren Stamm. Sand plant in `FallingBlock.onPlace` unabhängig vom Update-Flag einen Tick ein und fällt immer, der Test wäre also auch mit dem Fix rot gewesen. Die Fackel fällt nur bei einem Formupdate.
- Laub: geprüft wird `DISTANCE == 1` statt Zerfall, weil Zerfall erst im Random Tick kommt; das Formupdate setzt die Distanz sofort auf 7.
- Salven und Feuerbälle: Projektil 1 Block vor der (um 0,3 vergrößerten) Kopfbox-Außenfläche, Bewegung 3 Blöcke nach außen, ein `tick()` - so schneidet die Bahn die Austrittsfläche wie bei `firecanonl`. Ein Projektil ohne Bewegung hätte keinen Schnittpunkt und wäre kein Beleg.
- `spawnTableMergesIdAndTagKeysByR18` weggelassen (bräuchte ein Test-Datapack; das ausgelieferte Pack hat keinen Fall mit zwei Schlüsseln).

### Ergebnis

`./gradlew build` grün, `./gradlew runGameTests`: **All 361 required tests passed**.

### Testlauf (fix2-gametests)

Zwei Tests ergänzt, drei Tests gegen den ungefixten Code gegengeprüft. Kein Fehler im Port gefunden.

- **`kingThunderSalvoReachesTargetFortyBlocksAway`**: echter King (KI aus) mit Kopf an seinem R25-Platz ruft
  `firecanonl` (per Reflection) auf eine Kuh in 40 Blöcken Abstand, dahinter eine Steinwand; innerhalb von 20 Ticks
  muss am Ziel ein Blitz stehen. Das Ziel steht **seitlich** (24 Ost, 32 voraus): geradeaus in 40 Blöcken liegt die
  Kuhbox in der aufgeblähten Kopfbox, die Kuh wurde dann auch ohne Fix zuerst getroffen (Gegenprobe war grün, also
  kein Beleg). Die Chunks der Flugbahn werden für den Test geforct, sonst stehen die Bolzen außerhalb der
  Strukturchunks still.
- **`kingWaypointAboveY230InTheOverworld`**: Treffer von einem Angreifer auf Y 280 setzt `currentFlightTarget` auf
  Y 280 (TheKing.hurt, `rt`-Zweig). Belegt, dass die acht Stellen den Helfer wirklich benutzen, nicht nur dass es
  ihn gibt.
- **Gegenprobe** (Fix jeweils temporär zurückgedreht, danach byte-gleich wiederhergestellt): `RoyalCeiling` auf 230
  → beide Deckeltests rot; `canHitCandidate` in ThunderBolt/IceBall und `isRelocatedHead` in BetterFireball
  zurückgedreht → `kingThunderSalvo…`, `royalSalvoLeavesTheHeadBox`, `royalFireballSkipsOwnHead` rot.
- **Falle, gefunden an der Gegenprobe:** `royalSalvoLeavesTheHeadBox` war bei einem Lauf ohne Fix rot, beim nächsten
  grün. `getEntities` durchsucht nur die Entity-Sektionen (16er-Raster) bis 2 Blöcke um die Suchbox; eine 19,9 breite
  Kopfbox, deren Mittelpunkt in der Nachbarsektion liegt, wird gar nicht gefunden. Je nach Gitterplatz des Tests war
  der Test also kein Beleg. Die drei Kopf-Tests legen Kopf bzw. King jetzt über `sectionAligned` an den Anfang einer
  Sektion. Dasselbe Verhalten hatte 1.7.10 (`MAX_ENTITY_RADIUS` 2.0), es ist also keine Abweichung des Ports.
- **Falle im GameTest-Framework:** `helper.onEachTick`/`runAfterDelay` **innerhalb** einer `runAfterDelay`-Aufgabe
  registriert, lässt `GameTestInfo.tickInternal` dieselbe Aufgabe zweimal laufen (Map wird während der Iteration
  verändert; `firecanonl` feuerte 9 statt 3 Bolzen). Aufgaben immer auf oberster Ebene des Tests registrieren.
- **Wand zuerst zu niedrig:** einer von drei Läufen war rot, zwei Bolzen flogen über die 11 Blöcke hohe Wand
  (Vorhalt 0,2 × 24 = 4,8 nach oben plus Streuung 4). Wand jetzt 23 hoch und 17 breit, Blitzsuche ±8/±18/±8; die
  Fehlermeldung nennt dx/dy/dz jedes Bolzens.
- **Ergebnis:** danach dreimal hintereinander `./gradlew build runGameTests`: **All 363 required tests passed**,
  im Log null `Failed to create mod instance`, `Cowardly refusing`, `Failed to parse` und keine ERROR/FATAL-Zeile.

### Für die nächsten Wellen

- Spawn-Tabelle: `SpawnTable.entries(Holder<Biome>)` ist der Einstieg für alles, was Biome kennt; `entries(ResourceLocation)`/`biomes()` sehen Tag-Schlüssel (`#minecraft:is_nether`) nicht. Biom-Änderungen nur in `docs/catalog/biome_map.json` + `tools/gen_spawns.py`, die auch die `legacy_name`-Tags schreibt.
- Neue OreSpawn-`AgeableMob`-Basisklassen brauchen den `finalizeSpawn`-Override mit `LegacyAgeable.noBabies`.
- Spielertempo-Effekte aus 1.7.10-Clientcode: Vorbild `VelocityRaptorSpeed` (Modifier + Ablauf beim Sprint-Wechsel).
- Neue Projektile, die aus einem Boss-Kopf starten: `LegacyThrowable.canHitCandidate` überschreiben bzw. `LegacyProjectiles.isRelocatedHead`.
- Y-Grenzen von Bossen außerhalb der OreSpawn-Dimensionen: `RoyalCeiling.of(level)`.
- Aktionen, die einen Spieler aus `stepOn`/`entityInside` versetzen: wie `RTPBlock` per `MinecraftServer.tell(new TickTask(tick + 1, ...))`; ein Test muss den echten `handleMovePlayer` treiben (Vorlage `rtpBlockTeleportSurvivesTheMoveHandler`).
- Offen (nicht in R26): Raptor-Tempo auch beim Dimensionswechsel verlieren; ob Mob-Steine in `#c:ores` gehören, ggf. als DECISIONS-Zeile festhalten.
