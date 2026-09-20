# BUGHUNT2 – letzte Fehlersuche vor dem ersten Spieltest

Stand 2026-09-14. Alle Wellen W00–W13 sind gebaut. Geprüft wurde nur lesend: kein
Gradle-Lauf, kein Start. Jeder Befund unten wurde von zwei Gegenprüfern
unabhängig nachgesehen. Unter „Bestätigt" steht nur, was **beide** bestätigt
haben. Was schon in `BUGHUNT.md`, `FIX1.md` oder einer Entscheidung R0–R25 steht,
ist nicht noch einmal aufgeführt.

Pfade im Port sind relativ zu `mods/orespawn/`. Originalstellen beziehen sich auf
`reference/src-20.2/src/main/java/danger/orespawn/`.

---

## 1. Kurzfassung

**15 bestätigte Fehler** (aus 17 Meldungen: der Biom-Befund kam dreimal, aus
worldgen, land-mobs und config-network-save, und ist hier zusammengelegt),
dazu **2 strittige Punkte**.

| Schwere | Anzahl |
|---|---|
| hoch | 4 |
| mittel | 6 |
| niedrig | 5 |

| Kategorie | Anzahl | Befunde |
|---|---|---|
| divergence (weicht vom Original ab, ohne `PORT:` oder Entscheidung) | 7 | Grasfärbung, Miner's Dream, Sky Tree, Babys, Raptor-Tempo, Königssalven, Alt-Taste |
| r22-list (1.7.10-Kategorieliste ohne ihre 1.21.1-Mitglieder) | 5 | Biome, Rose Sword, Elevator, Kristallofen, Y-230-Deckel |
| silent (Funktion tut still nichts) | 2 | RTP-Block, Ackerboden |
| crash (Absturz oder Spieler steckt im Fels) | 1 | Rückkehr-Teleporter |

**Vor dem Spieltest unbedingt beheben:** Der Rückkehr-Teleporter setzt
Spieler auf Y -63 (2.2). Der Miner's Dream löscht OreSpawns eigene Erze (2.3).
Der RTP-Block zieht Spieler im Überlebensmodus zurück (2.4). King und Queen
schießen in ihre eigene Kopf-Hitbox (2.6).

---

## 2. Bestätigte Fehler

### 2.1 client-start

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Die BlockReed-Grasfärbung gilt nur für die Kristallsetzlinge. Mais, Tomate, Salat, Quinoa, Experience Sapling, Island und die drei Spawner-Blöcke bleiben ungefärbt. | `src/main/java/com/swbr/orespawn/client/BlockRenderTypes.java:59-66` färbt nur `CRYSTAL_SAPLING`..`3`. Die Modelle `models/block/corn_0.json:2` (ebenso `corn_1-3`, `tomato_0-3`, `lettuce_0-3`, `quinoa_0-3`, `experiencesapling`, `island`, `kingspawner`, `queenspawner`, `dungeonspawner`) haben `parent minecraft:block/cross` ohne `tintindex`. | `BlockCorn.java:11`, `BlockTomato.java:11`, `BlockLettuce.java:11`, `BlockQuinoa.java:11`, `BlockExperiencePlant.java:11`, `IslandBlock.java:13`, `KingSpawnerBlock.java:13`, `QueenSpawnerBlock.java:13`, `DungeonSpawnerBlock.java:11`: alle `extends BlockReed` und ohne Override von `colorMultiplier`/`getRenderType`. Sie erben also die Biom-Grasfarbe wie Zuckerrohr. W02.md:146 wendet dieselbe Regel nur auf die Kristallsetzlinge an. | Mais, Tomate, Salat oder Quinoa neben Zuckerrohr in einem Sumpf oder Dschungel pflanzen. Zuckerrohr und die Kristallsetzlinge nehmen die Biomfarbe an, die OreSpawn-Pflanzen und die Spawner-Blöcke bleiben in jedem Biom grell in ihrer Rohtextur. | Die genannten Modelle auf `minecraft:block/tinted_cross` umstellen (`render_type cutout` bleibt). Die Blöcke in `onBlockColors` mit demselben `BiomeColors.getAverageGrassColor`/`GrassColor.getDefaultColor`-Lambda eintragen. Keine Item-Färbung. | niedrig |

### 2.2 worldgen-dimensions

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Wer in die Oberwelt zurückkehrt, landet auf Y -63 in Bedrock und Deepslate, wenn die Ziel-Chunks nicht geladen sind. | `src/main/java/com/swbr/orespawn/world/dimension/OreSpawnTeleporter.java:298` (`searchTop`), benutzt in `:172` und `:227`. `Level.getHeight` gibt bei `!hasChunk` den Wert `getMinBuildHeight()` = -64 zurück (Level.java:383-397). Die Schleife `for (posY = -63; posY > 1; …)` läuft deshalb nie, lädt nichts, und auch der Fallback bleibt bei -63. | `OreSpawnTeleporter.java:53` und `:106` zählen von 180 abwärts, und `world.getBlock` lädt dabei den Chunk. | Mit einer Brown Ant nach Utopia gehen, ein paar hundert Blöcke weg vom Spawn laufen oder einfach warten, dann per Brown Ant (oder Red/Rainbow/Unstable Ant, Termite, Butterfly) zurückkehren. Spieler und alle mitreisenden Begleiter erscheinen auf Y -63 und ersticken. Der Test `Fix1GameTests.teleporterReturnLandsOnAPeakAboveY181` sieht das nicht, weil er einen schon geladenen Arena-Chunk nutzt. | Vor dem Lesen den Chunk laden: `world.getChunk(x >> 4, z >> 4).getHeight(WORLD_SURFACE, x & 15, z & 15) + 1`, begrenzt auf die Bauhöhe (die Chunk-Variante zählt das +1 nicht selbst dazu). Dazu ein GameTest mit einer Zielspalte in einem nie geladenen Chunk. Der Klassen-Javadoc (`:50-51`) behauptet das Laden fälschlich und muss mit korrigiert werden. | hoch |
| Die Spawn-Tabelle und die `legacy_name`-Biom-Tags übergehen die Biome, in die 1.21.1 die alten Biome aufgeteilt hat: Nether-Biome, warme, lauwarme und kalte Ozeane, Mangrovensumpf, Bambusdschungel. *(Dreifach gemeldet: worldgen, land-mobs, config-network-save.)* | `docs/catalog/biome_map.json:9,17,19-20,27,34` (`hell` → nur `nether_wastes`, `ocean` → nur `ocean`, `deepOcean` → nur `deep_ocean`, `swampland` → nur `swamp`, `jungle`/`jungleHills` → nur `jungle`). Daraus erzeugt `tools/gen_spawns.py` die Datei `src/main/resources/data/orespawn/spawn_table/spawns.json` mit 31 Biom-Schlüsseln. `world/spawn/ConfigSpawnsBiomeModifier.java:57-78` sucht nur den exakten Schlüssel und kennt keine Tags. Dieselbe Enge steckt in `data/orespawn/tags/worldgen/biome/legacy_name/ocean.json`, `swampland.json` und `jungle.json`, gelesen von `world/structure/LegacyBiomeNames.java:25,31,33` und benutzt in `OreSpawnWorld.java:578-618, :915, :1217-1234`. | `OreSpawnMain.java:4462, :4465` (Kyuubi, Stinky in `BiomeGenBase.hell`, damals der ganze Nether), `:4366` (Whale, `deepOcean`), `:4489, :4506-4531` (Hydrolisc, WaterDragon, SeaMonster, SeaViper, Crab, AttackSquid, Lizard in `ocean`/`swampland`). `OreSpawnWorld.java:1164, :1206, :1397, :1418, :1439` (Strukturen mit `biomeName` „Ocean"). | (1) In Soul Sand Valley, Crimson Forest, Warped Forest und Basalt Deltas spawnen nie Kyuubi oder Stinky, nur in den Nether Wastes. (2) In lauwarmen, warmen und kalten Ozeanen gibt es weder Water Dragon, Sea Monster, Sea Viper, Crab, Attack Squid noch Lizard. In den tiefen Varianten fehlt der Whale. Play Pool, Water Dragon Lair, Gold Fish Bowl, Girlfriend Island und Monster Island entstehen dort nie. (3) Mangrovensümpfe und Bambusdschungel bekommen keine Sumpf- bzw. Dschungel-Spawns, kein Gemüse, keine Schmetterlingspflanzen, keine Nester und keine Spit-Bug-Lairs. Die Nether-Ores des Ports benutzen dagegen schon `#minecraft:is_nether`. | Nach R22 `biome_map.json` erweitern und `spawns.json` neu erzeugen: `hell` → `#minecraft:is_nether`; `ocean` → `ocean`, `lukewarm_ocean`, `warm_ocean`, `cold_ocean`; `deepOcean` → `deep_ocean`, `deep_lukewarm_ocean`, `deep_cold_ocean`; `swampland` → `swamp` + `mangrove_swamp`; `jungle` → `jungle` + `bamboo_jungle`. Die `legacy_name`-Tags genauso erweitern. Die Merge-Regel „größerer Eintrag gewinnt" (R18) bleibt, und `ConfigSpawnsBiomeModifier` muss Tag-Einträge annehmen. Den Beschluss unter R22 festhalten. **Offen gebliebene Abwägungen der Gegenprüfer:** `frozen_ocean`/`deep_frozen_ocean` gab es in 1.7.10 schon als eigene Biome ohne OreSpawn-Spawns, eher weglassen. Mangrove und Bambusdschungel sind neue Biome und keine reinen Abspaltungen, also Ermessenssache. Unstrittig ist die Lücke im Nether. | mittel |

### 2.3 items-tools-armor

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Miner's Dream gräbt OreSpawns eigene Erze weg: Ruby, Amethyst, Titanium, Uranium, Salt, die Kristall-Erze und alle Spawn-Egg-Erze. | `src/main/java/com/swbr/orespawn/item/utility/ItemMinersDream.java:59-62` (`isDiggable`) gräbt alles außer `#c:ores`. Kein OreSpawn-Block steht in diesem Tag: Es gibt kein `data/c/tags/`, und NeoForges `c:ores` listet nur Vanilla-Untertags. | `ItemMinersDream.java:72` gräbt nur Stein, Erde, Kies, Flüssigkeiten, Netherrack, End Stone und `CrystalStone`. Alle OreSpawn-Erze blieben stehen. R22 sagt: „Gestein weg, Erze bleiben". | Ruby Ore (oder `orespider` usw.) auf Fußhöhe in Stein setzen und den Miner's Dream darauf richten. Das Erz verschwindet ohne Drop, Eisen- und Diamanterz daneben bleiben. In der Crystal-Dimension verschwinden auch Crystal Coal, Crystal Crystal und Tiger's Eye. | `data/c/tags/block/ores.json` anlegen (am besten mit Untertags wie `c:ores/ruby`) mit `oresalt`, `oreuranium`, `oretitanium`, `oreamethyst`, `oreruby`, `crystalcoal`, `crystalcrystal`, `tigerseye` und allen `OreGenericEgg`-Erzblöcken. Alternativ eine eigene OreSpawn-Erz-Prüfung in `isDiggable`. Ein OreSpawn-Erz in den Fix1-GameTest aufnehmen. | hoch |
| Das Rose-Sword-Rezept friert die 1.7.10-Liste für `red_flower` ein, Kornblume und Maiglöckchen fehlen. | `src/main/resources/data/orespawn/recipe/rosesword.json:9-37`: Zutat `E` sind genau neun Items (Mohn, Blue Orchid, Allium, Azure Bluet, vier Tulpen, Oxeye Daisy), kein Tag. | `OreSpawnMain.java:2837-2839`: `Blocks.red_flower` mit Wildcard-Meta, also jede kleine Blume außer dem Löwenzahn. R22 (DECISIONS.md:373) nennt kleine Blumen eine Kategorie. `CompanionSupport.isRedFlower` ist bereits umgestellt, das Rezept nicht (in BUGHUNT.md:118 nur als Verdacht vermerkt). | Zwei Kornblumen oder zwei Maiglöckchen über einen Stock legen: kein Rose Sword. Mit Mohn geht es. Für die Girlfriend zählt die Kornblume dagegen schon als rote Blume. | `E` um `minecraft:cornflower` und `minecraft:lily_of_the_valley` ergänzen (Torchflower und Wither Rose sind Ermessenssache, wie bei der Girlfriend) oder einen eigenen Tag `orespawn:red_flowers` = `small_flowers` ohne Löwenzahn verwenden. Im Rezeptgenerator beheben, sonst macht ein neuer Lauf den Fix rückgängig. | mittel |
| Das Elevator-Rezept nimmt nur die sechs Plankensorten aus 1.7.10. | `src/main/resources/data/orespawn/recipe/elevator.json:15-34`: Schlüssel `W` ist eine feste Liste mit Eiche, Fichte, Birke, Tropenholz, Akazie und Schwarzeiche. Die Liste stammt aus `tools/gen_recipes.py:17`. | `OreSpawnMain.java:5034`: `'W', Blocks.planks` mit Wildcard-Meta, also jede Planke. | Den Elevator (3 Planken über Diamant-Redstone-Diamant) aus Kirsch-, Mangroven-, Bambus-, Karmesin- oder Wirrholzplanken bauen: kein Ergebnis. | `W` auf `{"tag": "minecraft:planks"}` setzen. Kristallplanken sind nicht in diesem Tag, das passt zu R18. Die Wildcard-Abbildung im Generator anpassen. | niedrig |

### 2.4 blocks-crops

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Der Random Teleport Block zieht Spieler im Überlebensmodus zurück, statt sie zu teleportieren. | `src/main/java/com/swbr/orespawn/block/misc/RTPBlock.java:89`: `p.connection.teleport(...)` läuft in `stepOn`, also mitten in `ServerGamePacketListenerImpl.handleMovePlayer` → `player.move` (Zeile ~936). Danach liegt die Paketposition weit weg, der Handler meldet „moved wrongly!", setzt `flag2` und ruft `teleport(d3, d4, d5)` zurück an die alte Position (Zeile ~993). | `RTPBlock.java:52-54` bzw. `:318-362`: `setPlayerLocation` hat in 1.7.10 auch `lastPosX/Y/Z` überschrieben, die Anti-Cheat-Korrektur landete deshalb am Ziel. | Im Überlebensmodus (auch im Einzelspieler) auf einen Random Teleport Block laufen. Explosionsgeräusch und Partikel erscheinen am Ziel, der Spieler steht aber wieder neben dem Block und der Log meldet „moved wrongly!". Im Kreativmodus geht es. Der GameTest `rtpBlockRelocatesAServerPlayer` ruft `stepOn` direkt auf und sieht das nicht. | Nicht innerhalb von `move()` teleportieren. Das Ziel auf den nächsten Tick verschieben, z. B. mit `server.tell(new TickTask(server.getTickCount() + 1, …))` oder einem Attachment, das `PlayerTickEvent.Post` abarbeitet. Geräusch und Partikel dann ausführen. `server.execute` hilft nicht, es läuft sofort. | hoch |
| OreSpawn-CropBlocks halten den Ackerboden nicht feucht: Rettich, Erdbeere, Reis und die Insektenpflanzen fallen von trockenem Acker. | `block/crop/BlockRadish.java:63`, `BlockStrawberry.java:40`, `BlockRice.java:43`, `insectplant/BlockButterflyPlant.java:121`, `BlockMothPlant.java:106`, `BlockMosquitoPlant.java:108`, `BlockFireflyPlant.java:111`. Ein `data/minecraft/tags/block/maintains_farmland.json` fehlt. `FarmBlock.randomTick` (FarmBlock.java:95-126) erhält Ackerboden nur unter Blöcken aus `#minecraft:maintains_farmland`. | `BlockRadish.java:228`, `BlockStrawberry.java:182`, `BlockButterflyPlant.java:13`: `extends BlockCrops`. Im Forge-1.7.10-`BlockFarmland` blieb der Acker unter jedem `IPlantable` vom Typ Crop erhalten (die Forge-Klasse liegt nicht im Repo, die Aussage stützt sich auf Kenntnis von Forge 1.7.10). | Acker mehr als 4 Blöcke vom Wasser entfernt anlegen, trockenes Wetter, Rettich, Erdbeere oder Insektensamen pflanzen. Nach wenigen Random Ticks wird der Acker zu Erde und die Pflanze fällt als Item ab. Weizen daneben bleibt stehen. | `data/minecraft/tags/block/maintains_farmland.json` (`replace: false`) mit `orespawn:radish_plant`, `strawberry_plant`, `rice_plant`, `butterfly_plant`, `moth_plant`, `mosquito_plant` und `firefly_plant` anlegen. Die Registry-IDs vor dem Eintragen gegen `ModBlocks` prüfen, das hat kein Gegenprüfer getan. Die BlockReed-Pflanzen (Mais, Tomate, Salat, Quinoa) nicht aufnehmen. | mittel |
| Beim Fällen eines Sky Tree wird Flag 2 aus 1.7.10 wörtlich genommen: Formupdates laufen, aber der abgebaute Block meldet keinen Nachbarwechsel. | `src/main/java/com/swbr/orespawn/block/tree/BlockSkyTreeLog.java:56` (rekursiv) und `:76` (`onDestroyedByPlayer`) setzen Luft nur mit `Block.UPDATE_CLIENTS`. Ohne `UPDATE_KNOWN_SHAPE` ruft `Level.markAndNotifyBlock:293-297` trotzdem `updateNeighbourShapes` auf. | `BlockSkyTreeLog.java:53`, `:65-69`. Flag 2 hieß in 1.7.10: keine Nachbarn und keine Formupdates. Den ersten Block hatte `removedByPlayer` schon vorher mit Flag 3 entfernt. | (a) In Utopia einen Sky-Tree-Stamm abbauen: die ganze Krone aus Eichenlaub zerfällt mit Drops, Sand und Kies neben dem Stamm fallen, Wasser läuft nach. Im Original blieb die Krone schweben. (b) Eine Schiene auf den Stamm setzen und diesen Block abbauen: die Schiene schwebt weiter. `FIX1.md:80-83` führt das als offen. | Den vom Spieler abgebauten Block mit `Block.UPDATE_ALL` entfernen, die rekursiven Stammblöcke mit `UPDATE_CLIENTS \| UPDATE_KNOWN_SHAPE` (wie `InstantGarden` und `ItemMinersDream`). Alternativ den Laubzerfall als bewusste Abweichung in DECISIONS festhalten. | niedrig |
| Der Kristallofen nimmt die 1.21.1-Mitglieder der Kategorie „Holzblock" nicht als Brennstoff an. | `src/main/java/com/swbr/orespawn/block/entity/TileEntityCrystalFurnace.java:306-343` (Sets `MATERIAL_WOOD_TAGS`/`MATERIAL_WOOD_BLOCKS`), `getItemBurnTime` ab `:354`. | `TileEntityCrystalFurnace.java:231-233`: jeder Block mit `Material.wood` brennt 300 Ticks, eine Kategorie und keine Liste. R18 schließt nur die Data-Map-Rückfallebene aus, R22 verlangt die ganze Kategorie. | Eine Schmelzzutat einlegen und als Brennstoff Fass, Lesepult, Komposter, Webstuhl, Kartografie-, Bogner- oder Schmiedetisch, Chiseled Bookshelf oder Bambusblock nehmen: der Ofen zündet nicht. Mit einer Truhe zündet er. *Korrektur eines Gegenprüfers:* Der Slot ist ein normaler `Slot` (`ContainerCrystalFurnace.java:57`) und nimmt das Item an, es brennt nur nie. Per Shift-Klick landet es nicht im Brennstoff-Slot (`:116`, `isItemFuel`). | `MATERIAL_WOOD_BLOCKS` um `BARREL`, `LECTERN`, `COMPOSTER`, `LOOM`, `CARTOGRAPHY_TABLE`, `FLETCHING_TABLE`, `SMITHING_TABLE`, `CHISELED_BOOKSHELF`, `BAMBOO_MOSAIC` (plus Treppe und Stufe, falls nicht schon im Tag) und `MANGROVE_ROOTS` ergänzen, dazu `ItemTags.BAMBOO_BLOCKS` in `MATERIAL_WOOD_TAGS`. `NON_FLAMMABLE_WOOD` bleibt ausgeschlossen. Beehive und Bee Nest sind strittig: laut einem Gegenprüfer kein Vanilla-Brennstoff in 1.21.1, deshalb vor dem Eintragen prüfen. | niedrig |

### 2.5 land-mobs

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Etwa 5 % der natürlich gespawnten OreSpawn-Tiere sind Babys, und Babys von Dinos, Vögeln und Reittieren despawnen danach nie. | `src/main/java/com/swbr/orespawn/entity/critter/CritterSupport.java:75-85` (`noBabies`, nur von den Crittern benutzt). Kein `finalizeSpawn`-Override in `Girlfriend.java:90`, Boyfriend, `RedCow.java:47` samt Gold/Enchanted/Crystal Cow, Camarasaurus, Hydrolisc, Baryonyx, Beaver, Cassowary, Peacock, StinkBug, Chipmunk, Gazelle, Lizard, Ostrich, VelocityRaptor, Frog, Flounder, Whale, GoldFish und `EntityCannonFodder`. Persistenz z. B. in `herbivore/Baryonyx.java:335-340`, `herbivore/Camarasaurus.java:453-459`, `rider/Ostrich.java:765-771`. `AgeableMob.finalizeSpawn` (AgeableMob.java:28-42) macht jedes weitere Rudelmitglied mit 5 % zum Baby, auch bei der Chunk-Generierung (`NaturalSpawner.java:196`, `:402`). | `Baryonyx.java:227-233` (`canDespawn` hält ein Kind dauerhaft). 1.7.10 `EntityAgeable`/`EntityAnimal` hatten kein `onSpawnWithEgg`, natürliche Spawns waren also nie Kinder. | Neue Chunks in Wald, Ebene oder Strand (Girlfriend/Boyfriend-Rudel, Apple Cows) oder in Mining (Baryonyx, Camarasaurus, Velocity Raptor) erzeugen. Es tauchen halb große Girlfriends und Baby-Kühe ohne Drops auf. Baby-Dinos, -Vögel und -Reittiere werden persistent und sammeln sich dauerhaft an. | `noBabies` in einen gemeinsamen Helfer verschieben und in jeder natürlich spawnenden `AgeableMob`-Unterklasse `finalizeSpawn` überschreiben: `return super.finalizeSpawn(level, difficulty, type, noBabies(data));`. Dazu ein GameTest mit Gruppengröße 100 ohne ein einziges Baby. | mittel |
| Der Velocity Raptor gibt seinem Besitzer für die ganze Sitzung dreifaches (oder sechsfaches) Lauftempo. | `src/main/java/com/swbr/orespawn/entity/rider/RiderSupport.java:83-90`: transienter `ADD_MULTIPLIED_BASE`-Modifier `orespawn:velocity_raptor_speed`, der nirgends entfernt wird. Aufrufe in `entity/rider/VelocityRaptor.java:377` (+5.0 beim Apfelfüttern), `:395` (+2.0 beim Dead Bush), `:414` (+2.0 bei jedem Sitz-Umschalten). Der Javadoc `:75-81` nennt das selbst eine offene DECISIONS-Frage (ebenso W06 „Offene Punkte" 2, FIX1.md:95). | `VelocityRaptor.java:259, :283, :313`: `setBaseValue` nur clientseitig, das nächste `S20PacketEntityProperties` (spätestens beim nächsten Sprint-Wechsel) setzt den Wert zurück. | Einen Raptor zähmen und mit leerer Hand rechtsklicken. Der Spieler läuft ab jetzt mit 0.3 statt 0.1, nach einem weiteren Apfel mit 0.6, und Sprinten kommt noch obendrauf. Das hält über Dimensionswechsel hinweg, bis zum Tod oder zum Neueinloggen. | Den Modifier so ablaufen lassen wie in 1.7.10: beim Wechsel von `isSprinting` entfernen (per `PlayerTickEvent` gegen den vorigen Tick vergleichen) oder spätestens nach kurzer fester Zeit. Die gewählte Dauer in DECISIONS festhalten, dazu ein GameTest „nach einem Sprint-Wechsel wieder 0.1". | mittel |

*(Der Biom-Befund aus land-mobs steht unter 2.2.)*

### 2.6 bosses-cages

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Die Blitz- und Eissalven von The King und The Queen treffen die eigene Kopf-Hitbox und verschwinden. | `entity/boss/king/TheKing.java:949` (ThunderBolt), `:979` (IceBall); `entity/boss/queen/TheQueen.java:1023`. Die Ursache ist die Kombination aus `KingHead.java:221` / `QueenHead.java:240` (`setPos`, R25; Box 19.9 × 10, 30 Blöcke vor und 12 Blöcke über dem Boss, `ModEntities.java:731-741`), `entity/projectile/LegacyThrowable.java:171-187` (nimmt jede pickable Entity als Kandidat), `LegacyProjectiles.calculateIntercept` (liefert auch die Austrittsfläche) und dem Royalty-Discard in `ThunderBolt.java:54` / `IceBall.java:58`. | `TheKing.java:745, :779`; `TheQueen.java:761`; `KingHead.java:145-147` / `QueenHead.java:145-147`. Die Kopfposition wird roh in `posX/Y/Z` geschrieben, die Server-Box bleibt am Spawnpunkt stehen, und die Salven fliegen frei. | King oder Queen im Überlebensmodus bekämpfen und horizontal mehr als 30 Blöcke Abstand halten. Feuerbälle kommen an (BetterFireball hat eigene Filter), die Dreier-Salven aus ThunderBolts dagegen fast nie: keine Explosion, kein Blitz. Die Fünfer-Eissalve des Kings erzeugt nie Eis. Das passiert, sobald Kopf- und Körperausrichtung ungefähr gleich sind, also im Normalfall. | R25 beibehalten, aber `KingHead`/`QueenHead` für ThunderBolt und IceBall als Kollisionskandidaten ausschließen, z. B. über einen überschreibbaren Kandidatenfilter in `LegacyThrowable`. Dazu ein GameTest: King mit Kopf, Ziel in 40 Blöcken, `firecanonl` muss am Ziel Explosion oder Blitz auslösen. | hoch |
| Flug- und Angriffsziele von King und Queen sind in einer Oberwelt von -64 bis 320 weiter auf Y 230 gedeckelt. | `entity/boss/king/TheKing.java:627-628, :663-664, :703-704, :1054-1055`; `entity/boss/queen/TheQueen.java:676-677, :747-748, :765-766, :1106-1107`. Kein `PORT:`-Kommentar. R21 erlaubt wörtliche Y-Werte nur in den sechs OreSpawn-Dimensionen. | `TheKing.java:429-430, :466-467, :508-509, :848-849`; `TheQueen.java:522-523, :595-596, :637-638, :836-837`. Bei einer Bauhöhe von 256 blieben höchstens 26 Blöcke Luft, also innerhalb der 30 Blöcke Nahkampfreichweite. | Den King per King Spawner in der Oberwelt rufen und auf über etwa Y 260 hochbauen. Der Boss schwebt auf Y 230 darunter. Nahkampf (< 30 Blöcke), Stampf-Boxen und Fernangriffe (erst ab 30 Blöcken horizontal) erreichen den Spieler nicht, der Spieler kann aber ungestört nach unten schießen. | Außerhalb der OreSpawn-Dimensionen die Grenze aus dem Level ableiten, z. B. `getMaxBuildHeight() - 26` (294 in der Oberwelt). Innerhalb bleibt 230. Ein gemeinsamer Helfer für alle acht Stellen. | niedrig |

### 2.7 config-network-save

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Die Fly-up-Taste bleibt nach jedem Dimensionswechsel auf dem Server „gedrückt". | `src/main/java/com/swbr/orespawn/client/input/RiderControl.java:59-62`: `onClone` setzt den gemerkten `keystate` auf 0. Beim Dimensionswechsel ruft `Minecraft.setScreen` (Minecraft.java:1062) zwar `KeyMapping.releaseAll()` auf, das nachfolgende `runTick(false)` sendet aber nichts. `Clone` feuert noch im selben Handler (`ClientPacketListener.handleRespawn`). Die Server-Seite `ModAttachments.FLY_UP_KEYSTATE` (`ModAttachments.java:29-30`) bleibt bei 1, weil `changeDimension` denselben `ServerPlayer` behält. Der `PORT`-Kommentar `:46-48` gilt nur für Login und Tod. | `RiderControl.java:19-31`: sendet nur bei einer Änderung und setzt nie zurück. | Mit gedrückter linker Alt-Taste auf Dragon, Cephadrome oder Leon durch ein Netherportal fliegen (oder während einer OreSpawn-Teleportation) und die Taste im Ladebildschirm loslassen. Nach der Ankunft steigt oder beschleunigt jedes gerittene Tier (Dragon, Cephadrome, Leon, Ostrich, Hoverboard, junger Prince, Elevator) weiter, bis Alt einmal gedrückt und wieder losgelassen wird. | In `onClone` den Wert auf `-1` setzen, damit der nächste Tick den echten `isDown()`-Zustand sicher sendet. Alternativ nur zurücksetzen, wenn `event.getOldPlayer().isDeadOrDying()` gilt. | mittel |

*(Der Biom-Befund aus config-network-save steht unter 2.2.)*

---

## 3. Strittige Punkte

Hier hat nur einer von zwei Gegenprüfern den Befund bestätigt. Vor einem Fix
braucht es eine Entscheidung.

| Titel | Port / Original | Urteil A | Urteil B |
|---|---|---|---|
| Große Strukturen halten die Chunk-Generierung 9–30 s auf, weil `getBaseColumn` für jede Spalte den Terrain eines ganzen Chunks neu erzeugt (worldgen, gemeldet als hoch). | `world/dimension/utopia/ChunkProviderOreSpawn.java:551-563` (ebenso `ChunkProviderOreSpawn2:541-553`, `5:986-995`, `6:585-594`), `world/structure/StructureRecording.java:483-491, :259-277, :79`, `world/dungeon/b/GenericDungeonB3.java:357-369` / `GenericDungeon.java:4391-4441` | **Widerlegt:** Es ist ein bekannter, nicht gemessener Performance-Punkt (W12.md:229, W13.md:56-58, :147). Die 9.5–29.7 s aus W13.md:351 enthalten die komplette Chunk-Generierung. Die eigene Schätzung liegt bei etwa 1 ms pro Chunk, also wenige Sekunden auf einem Worker, einmal pro Altar. Kein Watchdog-Risiko, keine falschen Blöcke. | **Bestätigt:** Der Codepfad ist belegt (etwa 2601 volle Terrain-Läufe unter einem `synchronized`-Slot), W13 hat eine Zeit festgehalten, und keine Entscheidung nimmt das hin. Beim ersten Kontakt mit einem King- oder Queen-Altar lädt der Terrain spürbar nicht nach. Selten, ein Watchdog-Auslöser ist unwahrscheinlich. Fix: Chunk-Cache in `getBaseColumn`. |
| Der Nether Tracker legt seine Quarzspur nur auf reinem Netherrack, nicht auf Nylium, Basalt oder Blackstone (items, niedrig). | `item/utility/ItemNetherLost.java:83` / `ItemNetherLost.java:49` | **Widerlegt:** Der Port entspricht 1:1 dem Original. Die Ausnahme in R22 („einzelne, benannte Blöcke bleiben einzeln") greift hier. Auch 1.7.10 hatte Lücken durch Seelensand, Kies und Glowstone. Eine Erweiterung wäre eine Designfrage. | **Bestätigt:** Netherrack war in 1.7.10 das einzige Nether-Gestein, gemeint war also die Kategorie „Nether-Gestein" (`#minecraft:base_stone_nether` + `#minecraft:nylium`). In 3 von 5 Nether-Biomen tut das Item nichts. Seelensand und Seelenerde bleiben ausgeschlossen. |

---

## 4. Fehlerklassen, die mehrfach vorkommen

### 4.1 R22: Kategorieliste ohne ihre 1.21.1-Mitglieder (6 Befunde inkl. Biome)

Betroffen sind Biome, kleine Blumen, Planken, Holzmaterial und die Y-Grenze. Das
Muster steckt oft in **Daten statt in Java**: von Generatoren erzeugte Rezepte
und Tabellen (`gen_recipes.py`, `gen_spawns.py`, `biome_map.json`). Ein Fix nur
im JSON geht beim nächsten Generatorlauf wieder verloren.

Weitere Suchhinweise:
- Rezepte mit expliziten Vanilla-Listen statt Tags:
  `rg -l '"minecraft:(oak|spruce)_(planks|log)"|"minecraft:poppy"|"minecraft:white_wool"' src/main/resources/data/orespawn/recipe`
  und in `tools/gen_recipes.py` jede Wildcard-Abbildung (`Blocks.planks`, `log`,
  `wool`, `red_flower`, `sapling`, `leaves`, `stained_*`).
- Einzelblock-Vergleiche, die eine Kategorie meinen:
  `rg 'is\(Blocks\.(NETHERRACK|STONE|DIRT|GRASS_BLOCK|SAND|GRAVEL|OAK_LOG|OAK_LEAVES|WHITE_WOOL)\)' src/main/java`
- Y-Grenzen aus der Zeit mit 256 Blöcken außerhalb der OreSpawn-Dimensionen:
  `rg '\b(230|240|250|255|256)\b' src/main/java/com/swbr/orespawn/entity` und
  `rg 'getY\(\) *[<>]=? *[0-9]' src/main/java`. Bei jedem Treffer fragen: gilt die
  Stelle auch in der Oberwelt oder im Nether?
- Weitere Biom-Namenslisten: `rg 'legacy_name|nether_wastes|"minecraft:ocean"' src docs/catalog tools`.

### 4.2 Fehlende Vanilla- oder `c:`-Tags für eigene Blöcke (2 Befunde)

`#c:ores` fehlt (Miner's Dream), `#minecraft:maintains_farmland` fehlt
(Ackerboden). 1.21.1 hängt Verhalten an Tags, das 1.7.10 über
Klassenvererbung oder `Material` hatte. Ein Mod-Block, der im Tag fehlt,
verliert dieses Verhalten still.

Suchhinweise: `ls src/main/resources/data/minecraft/tags/block` enthält bisher nur
`leaves`, `logs` und `mineable/*`. Kandidaten, die man gegen die Originalklassen
prüfen sollte: `crops` und `bee_growables` (Bienen, Dorfbewohner), `saplings`,
`flowers`/`small_flowers`, `climbable`, `replaceable`, `dirt` (für `mayPlaceOn`
fremder Pflanzen auf OreSpawn-Erde), `planks`/`wooden_*`, `c:ores/*`, `c:ingots/*`,
`c:gems/*` sowie die Item-Tags `c:foods`, `c:seeds` und `c:crops`.
`rg 'BlockTags\.|ItemTags\.' <architrave/.cache/sources>` zeigt, welche Vanilla-Stellen
Tags auswerten.

### 4.3 Aktionen mitten in einem Bewegungs- oder Paket-Handler (1 Befund, wahrscheinlich mehr)

Der RTP-Block teleportiert in `stepOn`, und der Movement-Handler setzt danach
zurück. Dasselbe droht überall, wo ein Spieler in `stepOn`, `entityInside`,
`fallOn` oder `onProjectileHit` bzw. `onImpact` versetzt wird.
Suchhinweis: `rg -n 'connection\.teleport|teleportTo\(|changeDimension' src/main/java`
und jeden Treffer darauf prüfen, ob er innerhalb von `Entity.move` aufgerufen wird.
Zu prüfen sind dabei vor allem Portalblöcke und Teleport-Items, deren Wirkung per
Block-Kontakt ausgelöst wird.

### 4.4 Effekte, die 1.7.10 nur clientseitig und damit flüchtig setzte, sind im Port dauerhaft (1 Befund)

Das Raptor-Tempo war ein clientseitiges `setBaseValue`, im Port ist es ein
serverseitiger Modifier, der nie entfernt wird.
Suchhinweis: `rg -n 'addOrUpdateTransientModifier|addTransientModifier|addPermanentModifier' src/main/java`
und zu jeder ID prüfen, ob irgendwo ein `removeModifier` dazu existiert. Ebenso
`rg -n 'isRemote' reference/src-20.2/src/main/java/danger/orespawn | rg 'setBaseValue|motion|capabilities'`
im Original.

### 4.5 Geänderte Hitboxen und Positionen (R25) wirken auf andere Systeme (1 Befund)

Die mit `setPos` versetzten Köpfe von King und Queen fangen jetzt Projektile ab.
Suchhinweis: `rg -n 'implements .*Royalty|isPickable' src/main/java/com/swbr/orespawn/entity`
und jede weitere Multipart- oder Kopf-Entity (z. B. `GodzillaHead`) gegen die
Projektilklassen ohne eigenen Filter prüfen (`rg -n 'extends LegacyThrowable' src/main/java`).

### 4.6 1.21.1-Vanilla-Pfade, die 1.7.10 nicht kannte (3 Befunde)

- `getHeight` bei nicht geladenem Chunk (Teleporter):
  `rg -n 'getHeight\(Heightmap|getHeightmapPos' src/main/java` und bei jedem Treffer
  auf Ziele außerhalb geladener Chunks achten (Spawn-Items, Rückkehr, Strukturplatzierung).
- `AgeableMob.finalizeSpawn` erzeugt Babys (Rudel-Spawns):
  `rg -l 'extends (Animal|TamableAnimal|AgeableMob|Cow|EntityCannonFodder)' src/main/java`
  mit `rg -l 'finalizeSpawn' src/main/java` abgleichen.
- `ClientPlayerNetworkEvent.Clone` feuert auch beim Dimensionswechsel (Alt-Taste):
  `rg -n 'ClientPlayerNetworkEvent\.(Clone|LoggingIn|LoggingOut)' src/main/java` und
  jeden clientseitigen Zustand prüfen, der dort unter der Annahme „Server ist frisch"
  zurückgesetzt wird.

### 4.7 1.7.10-Flags und -Vererbung wörtlich übernommen (2 Befunde)

- `setBlock(..., 2)` ist in 1.21.1 nicht mehr „still" (Sky Tree):
  `rg -n 'Block\.UPDATE_CLIENTS\)' src/main/java` findet Stellen ohne
  `UPDATE_KNOWN_SHAPE`. Bei jeder prüfen, ob Laub, Sand oder Wasser in der Nähe
  sein kann.
- Render-Eigenschaften der Elternklasse (BlockReed-Färbung): im Original
  `rg -l 'extends (BlockReed|BlockBush|BlockFlower|BlockLeaves|BlockGrass)' reference/src-20.2`
  und die zugehörigen Port-Modelle auf `tintindex` bzw. `tinted_cross` prüfen, z. B.
  `rg -L 'tint' src/main/resources/assets/orespawn/models/block/*leaves*.json`.
