# Bug-Hunt W01–W07

Stand 2026-09-13. Geprüft wurde der gebaute Stand der Wellen W01 bis W07 gegen
das Original (`reference/src-20.2`) und gegen `docs/DECISIONS.md` (R0–R22). Jeder
Befund unten wurde von zwei Prüfern unabhängig nachgeprüft. **Bestätigt** heißt:
beide Prüfer haben ihn bestätigt. **Strittig** heißt: nur einer von beiden.
Punkte, die erst in späteren Wellen gebaut werden (W08+), zählen hier nicht.

Die Pfade im Port beziehen sich auf `src/main/java/com/swbr/orespawn/`. Die
Pfade im Original beziehen sich auf `reference/src-20.2/src/main/java/danger/orespawn/`.

## 1. Kurzfassung

23 bestätigte Fehler (ein Doppelbefund zusammengelegt: Big Hammer) und 1 strittiger Punkt.

| Schwere | Anzahl |
|---|---|
| high | 1 |
| medium | 13 |
| low | 9 |

| Kategorie | Anzahl | Bedeutung |
|---|---|---|
| `r22-list` | 18 | feste 1.7.10-Liste deckt die 1.21.1-Kategorie nicht ab (R22) |
| `divergence` | 4 | Verhalten weicht vom Original ab, ohne `// PORT:`-Kommentar und ohne Regel |
| `silent` | 1 | registriert nichts, fällt nur im Spiel auf |

Der einzige Befund mit `high` ist der Miner's Dream. Den hat der Nutzer im
Spiel gefunden, und R22 wurde seinetwegen geschrieben. Die Klasse ist seitdem
trotzdem unverändert.

Drei `// PORT:`-Kommentare stützen sich auf R18 und sind seit R22 überholt, und
zwar in `ItemMinersDream`, `UltimateSword` und `WormSupport`. `Beaver` und
`CannonFodderSupport.isLegacyDye` begründen die schmale Liste ebenfalls noch
ausdrücklich. Diese Kommentare sind keine Abnahme. Sie zeigen, wo die Korrektur
ansetzen muss.

## 2. Bestätigte Fehler

### core-combat

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Spawn-Eier für Girlfriend, Boyfriend und Basilisk fehlen | `registry/ModItems.java:294-616` (Spawn-Ei-Block ohne `egggirlfriend`, `eggboyfriend`, `eggbasilisc`) | `OreSpawnMain.java:5188` (197), `:5199` (207), `:5273` (349); Registrierung `:2009`, `:2010`, `:2024`; `ItemSpawnEgg.java:63`, `:103`, `:403` | `/give @s orespawn:egggirlfriend` (ebenso `eggboyfriend`, `eggbasilisc`) meldet ein unbekanntes Item. Keins der drei Eier steht im Creative-Tab. Die Entities (`ModEntities` GIRLFRIEND :158, BASILISK :254, BOYFRIEND :448), die Texturen und die lang-Schlüssel (`en_us.json:532`, `:535`, `:572`) sind vorhanden. `W07.md:99` behauptet, der Basilisk habe kein Ei. `manifest.json:26421` widerlegt das. Keine Wellen-JSON übernimmt die drei Eier. W11 (EasterBunny verschenkt diese Eier) läuft deshalb in dieselbe Lücke. | Drei `ItemSpawnEgg`-Einträge in `ModItems` und `models/item/<id>.json` (item/generated) anlegen. `SpawnEggSetup` verdrahtet danach Dispenser und Tab. `W07.md:99` korrigieren. | medium |
| Big Hammer kann nicht blocken | `item/tool/BigHammer.java:20-23` (`extends SwordItem`); das Javadoc `:17-19` nennt die Blockdauer fälschlich tot | `BigHammer.java:9` (`extends ItemSword`), `:36-38` (`getMaxItemUseDuration` = 3000); `itemblock-01.md:153` | Big Hammer halten, Rechtsklick gedrückt halten, einen Zombie zuschlagen lassen: keine Blockhaltung, voller Schaden. In 1.7.10 halbierte der geerbte ItemSword-Block den Treffer. `CombatEvents.applySwordBlock` (`:54-61`) greift nur für `instanceof BlockingSword` (`BlockingSword.java:50`). Alle anderen Schwerter mit Override erben davon, der Hammer als einziges nicht. Das verstößt gegen R19 (`DECISIONS.md:292-302`). | `extends BlockingSword` mit `super(material.withUses(9000), props.attributes(OreSpawnTiers.sword(material)), 3000)`, `hurtEnemy` behalten, Javadoc korrigieren, Hammer in den Block-GameTest aufnehmen (`W03GameTests.java:110` prüft nur Haltbarkeit und Angriff). | medium |

### blocks-plants

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Boden für Samen, Feldfrüchte, Setzlinge und Blumen kennt nur 1.7.10-Erde | `block/crop/CropBlocks.java:57-61`; `block/plant/ReedLikePlant.java:48-60`; `block/plant/MyBlockFlower.java:93`. Aufrufer: `ItemAppleSeed:83,123`, `ItemExperienceTreeSeed:49`, `BlockCorn:76`, `BlockTomato:85`, `BlockLettuce:98`, `BlockQuinoa:79`, `BlockCrystalPlant:59`, `BlockExperiencePlant:35` | `ItemAppleSeed.java:23,44`; `ItemExperienceTreeSeed.java:22`; `BlockCorn.java:24`; `BlockTomato.java:24`; `BlockLettuce.java:21`; `BlockCrystalPlant.java:23`; `BlockExperiencePlant.java:21`; `MyBlockFlower.java:34/73` | Einen Apple Tree Seed oder den Experience Tree Seed per Rechtsklick auf Moosblock, Rooted Dirt, Mud oder Muddy Mangrove Roots setzen: nichts wächst. Corn, Tomato, Lettuce, Crystal-Setzlinge und OreSpawn-Blumen lassen sich dort nicht setzen oder brechen beim nächsten Update weg. Vanilla-Setzlinge wachsen auf allen diesen Blöcken. `MyBlockFlower` fragt zuerst `canSustainPlant`, bekommt dort aber `DEFAULT` und fällt auf die Liste zurück. | `state.is(BlockTags.DIRT) \|\| state.is(Blocks.FARMLAND)` einsetzen. Mycelium bewusst ausschließen (war in 1.7.10 nicht erlaubt) oder die Aufnahme im `// PORT: R22`-Kommentar begründen. | medium |
| OreSpawn-Holz brennt im Vanilla-Ofen nicht | `registry/ModItems.java:204-205, 254, 257`; es gibt keine `furnace_fuels`-Data-Map und keinen Item-Tag `#minecraft:logs/planks` (nur den Block-Tag `data/minecraft/tags/block/logs.json`) | `CrystalWood.java:13`, `BlockCrystalTreeLog.java:21`, `BlockSkyTreeLog.java:16`, `BlockDuplicatorLog.java:15` (`Material.wood`); `TileEntityFurnace.getItemBurnTime` gibt 300 Ticks | Crystal Planks, Crystal Tree Wood, Sky Tree Wood oder Duplicator Tree Wood in den Brennstoffslot eines Vanilla-Ofens legen: der Slot nimmt sie nicht an. In 1.7.10 brannte jeder dieser Blöcke 300 Ticks. `IItemExtension.getBurnTime` (`:619-622`) liest nur die Data-Map. R18 (`DECISIONS.md:273`) betrifft nur den Crystal Furnace. | Data-Map `furnace_fuels` mit `burn_time` 300 für die vier Items. **Nicht** über `#minecraft:planks` lösen, das schließt R18 (`:274`) aus. Prüfen, ob die Crystal Workbench ebenfalls `Material.wood` ist. | low |

### gear-workshop

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Miner's Dream lässt Granit, Diorit, Andesit, Tuff, Deepslate stehen und setzt dort keine Fackeln | `item/utility/ItemMinersDream.java:48-55` (`isDiggable`), `:64-67` (`carriesExtremeTorch`); der `// PORT:`-Kommentar `:49-50` widerspricht R22 | `ItemMinersDream.java:72`, `:99` | Den Miner's Dream bei Y 40 in einem Granit- oder Dioritfleck benutzen: Andesit, Diorit, Granit, Tuff, Calcit, Rooted Dirt, Mud usw. bleiben im 11x5x64-Tunnel stehen. Unter Y 0 wird Deepslate nie gegraben, und auf Deepslate-Boden setzt die Fackelschleife (`:163-173`) keine einzige Extreme Torch. `catalog/README.md:503` führt die alte Lesart „nur minecraft:stone“ weiter. | Gemäß R22 (`DECISIONS.md:342-362`): `isDiggable = !state.is(Tags.Blocks.ORES) && destroySpeed >= 0 && !state.hasBlockEntity()`, Flüssigkeiten wie im Original. Fackelboden: jede tragfähige Oberseite (`isFaceSturdy(UP)`) oder mindestens `#minecraft:base_stone_overworld`, `#minecraft:dirt`, dazu Bedrock, End Stone und Netherrack. Den PORT-Kommentar ersetzen. | high |
| Chainsaw-Kahlschlag kennt nur vier Holz-/Laubarten | `item/tool/UltimateSword.java:306-332` (`canCrush`), `:335-349` (`isLeaves`), `:361-382` (`mineBlock`); PORT-Javadoc `:299-305` aus der Zeit vor R22 | `UltimateSword.java:254-259` (`canCrush`), `:261-263` (`isLeaves`) | Mit der Chainsaw einen Kirsch- oder Mangrovenstamm abbauen: nur dieser Block bricht, das 11x16x11-Feld bleibt stehen. Bei Eiche wird das ganze Feld geräumt. Genauso bleiben Mangrove-, Kirsch-, Bambus-, Crimson- und Warped-Planken, entrindete Stämme und Azaleenlaub stehen. Akazie und Schwarzeiche fehlten schon im Original (`log2`), fallen aber unter R22. `isLeaves` hat dieselbe Lücke, ist aber nur mit `leaf=true` erreichbar. | `BlockTags.LOGS`, `LEAVES`, `PLANKS`, `SAPLINGS` sowie COBWEB, SHORT_GRASS/FERN, CACTUS und die OreSpawn-Blöcke. In `isLeaves` `LEAVES` und `SAPLINGS`. R22-Kommentar ergänzen. | medium |
| Ultimate Pickaxe würfelt den Edelsteinbonus nur auf Stein und Deepslate | `item/tool/UltimatePickaxe.java:86` (Eisen), `:89` (Gold), `:92` (Stein); PORT-Javadoc `:75-76` | `UltimatePickaxe.java:92`, `:95`, `:98-118` | Einige hundert Andesit-, Diorit-, Granit- oder Tuffblöcke abbauen: der 1:100-Bonus (Diamant, Smaragd, Amethyst, Rubin, Uran- oder Titannugget) kommt nie. Auf Stein und Deepslate kommt er. Nether Gold Ore gibt keinen Barrenbonus (das ist schwächer belegt, weil das Original einen einzelnen Block nannte). | Edelsteinwurf mit `state.is(BlockTags.BASE_STONE_OVERWORLD)`, Barrenbonus mit `BlockTags.IRON_ORES` bzw. `BlockTags.GOLD_ORES`. | medium |
| Instant Garden: Wasser läuft in den Garten, Sand und Kies fallen hinein | `item/utility/InstantGarden.java:86` (`flags = Block.UPDATE_CLIENTS`), benutzt in `:95-158` über `FastBlocks.setBlockFast`; der Kommentar `:84-85` ist falsch | `InstantGarden.java:69-131` (`setBlock(..., 0, 2)`) | Den Instant Survival Garden an einem Fluss oder Teich benutzen, sodass das 15x18x10-Volumen an eine Wasserquelle grenzt: Wasser läuft hinein und spült die Feldfrüchte weg. Unter einem Sand- oder Kiesüberhang (Oberfläche genau 10 über den Füßen) fällt der Überhang auf die Beete. Ursache: Ohne Bit 16 ruft `Level.java:293-296` `updateNeighbourShapes` auf, `LiquidBlock.updateShape` (`:154-157`) und `FallingBlock.updateShape` (`:38-39`) planen dann Ticks ein. W03 hat genau diesen Fehler im Miner's Dream schon behoben (`ItemMinersDream.java:127-131`, `W03.md:102`). | `Block.UPDATE_CLIENTS \| Block.UPDATE_KNOWN_SHAPE` wie `ItemMinersDream:131` und den Kommentar korrigieren. | medium |
| Ultimate Hoe verweigert Podsol und Grobe Erde | `item/tool/UltimateHoe.java:77` (Klickprüfung), `:95` (3x3x3-Schleife); Javadoc `:58-61` falsch | `UltimateHoe.java:44`, `:57` | In einer Riesentaiga Podsol oder Coarse Dirt mit Luft darüber per Rechtsklick bearbeiten: nichts passiert. Weil `useOn` nie `super` aufruft, fällt auch das Vanilla-Pflügen aus. In 1.7.10 waren beide `Blocks.dirt` (Meta 1/2), und das 3x3x3 wurde nasses Farmland. Wird ein Grasblock angeklickt, überspringt die Schleife Podsol und Grobe Erde im Würfel ebenfalls. | `OneUse.isDirt` (DIRT, COARSE_DIRT, PODZOL) plus GRASS_BLOCK an beiden Stellen. Nach R22 zusätzlich Rooted Dirt und Mud erwägen. | low |
| Crystal- und Tiger's-Eye-Schaufeln graben neuen Boden nur mit Handgeschwindigkeit | `item/tool/CrystalShovel.java:50-63` (`Tool.Rule.overrideSpeed`, Default 1.0f bei `:64`) | `CrystalShovel.java:32` (`blocksEffectiveAgainst`), `:22-24` (`canHarvestBlock` nur Schnee) | Mit einer Crystalwood-, Crystalpink-, Crystalstone- oder Tigerseye-Schaufel Mud oder Rooted Dirt neben normaler Erde graben: Erde geht mit Materialtempo, Mud und Rooted Dirt mit Faustgeschwindigkeit. Dasselbe gilt für Moosblock, Muddy Mangrove Roots, Dirt Path, Suspicious Sand und Suspicious Gravel. **Seelensand gehört nicht dazu**, der fehlte schon im Original. | Speed-Regel aus `#minecraft:dirt`, `#minecraft:sand` (ohne Seelensand), Gravel und Suspicious Gravel, Dirt Path, Schnee, Ton, Farmland, Mycelium und CRYSTAL_GRASS aufbauen. `minesAndDrops` nur für den Schneeblock behalten. | low |

### projectiles-companions

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Girlfriend: Zähmen und Anlocken ignoriert die neuen kleinen Blumen | `entity/companion/CompanionSupport.java:101-105` (`isRedFlower`); benutzt in `Girlfriend.java:176` (Tempt, Prio 2), `:554` (Zähmen), `:753` (`isBreedingItem`) | `Girlfriend.java:129`, `:609`, `:852` (`Blocks.red_flower`, jede Meta) | Eine wilde Girlfriend mit einer Kornblume oder einem Maiglöckchen in der Hand anlaufen: sie folgt nicht. Rechtsklick innerhalb von 4 Blöcken: kein Zähmen. Mit Mohn oder Tulpe klappt beides. Die Liste stammt aus `core-01b.md:62`, einer Katalognotiz, keiner Regel, und ist älter als R22. | `#minecraft:small_flowers` ohne Löwenzahn (der hat den eigenen `yellow_flower`-Zweig, `Girlfriend.java:614`). Wither Rose und Torchflower sind Ermessenssache, Kornblume und Maiglöckchen gehören sicher dazu. Der Mohn-Drop bleibt. | medium |
| Sifter ignoriert die neuen Sand-, Kies- und Erdvarianten | `item/rock/ItemSifter.java:280` (Sand), `:341` (Kies), `:394` (Erde), `:92` (Wasserprüfung), `:520-528` (Haltbarkeit) | `ItemSifter.java:33-37`, `:224`, `:285`, `:338` | Suspicious Sand, Suspicious Gravel, Rooted Dirt oder Mud mit dem Sifter anklicken: die Haltbarkeit sinkt um 1, es dropt nie etwas. **Korrektur zum Ursprungsbefund:** Seegras am Meeresboden ist fast nie das Problem. Ein Klick trifft das Seegras selbst, und darüber liegt Wasser, also läuft die Wassertabelle. Wirklich tot sind nur mittlere `kelp_plant`-Blöcke und Nicht-Tabellen-Böden am Rand der Pflanzen-Hitbox. | Kategorien explizit: Sand = SAND, RED_SAND, SUSPICIOUS_SAND; Kies = GRAVEL, SUSPICIOUS_GRAVEL; Erde = DIRT, COARSE_DIRT, PODZOL, ROOTED_DIRT, MUD. **Nicht** blind `#minecraft:dirt` nehmen: das enthält den Grasblock, der eine eigene Tabelle hat (`:455`), und Mycelium. Optional die Wasserprüfung auf `getFluidState(pos.above()).is(FluidTags.WATER)` umstellen. | low |
| Besitzer bekommt eine Todesnachricht für jede gezähmte Girlfriend und jeden Boyfriend | `entity/companion/Girlfriend.java:89`, `Boyfriend.java:79` (kein `die`-Override) | kein Override; `EntityTameable` (`tg` laut `joined.srg:1667`) überschreibt `onDeath` nicht | Eine Girlfriend zähmen, ohne sie zu benennen, und von einem Zombie töten lassen. Der Besitzer sieht „Girlfriend was slain by Zombie“ im Chat (`TamableAnimal.java:237-247`, prüft nur Gamerule und Besitzer). In 1.7.10 gab es keine Nachricht, auch nicht bei benannten Tieren. `W04.md:81-82` führt den Punkt als offen, und seine Aussage „1.7.10 meldete benannte Tiere“ ist laut srg falsch. | `die` in beiden Klassen überschreiben und die Besitzernachricht auslassen, oder das 1.21.1-Verhalten per DECISIONS-Regel annehmen. | low |

### dimensions-portals

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Rückkehr in die Overworld sucht nur Y 180..2 und landet in Berghöhlen | `world/dimension/OreSpawnTeleporter.java:167`, Fallback `:218-227`; das Javadoc `:154` zitiert die falsche R18-Zeile. Aufrufer: `EntityAnt.java:137`, `EntityRedAnt.java:88`, `EntityRainbowAnt.java:54`, `EntityUnstableAnt.java:51`, `Termite.java:145`, `EntityButterfly.java:404` | `OreSpawnTeleporter.java:53`, `:106` | In Utopia, Mining, VillageMania, Islands, Crystal oder Chaos an einer X/Z stehen, die in der Overworld ein Gipfel über Y 181 ist (Jagged, Frozen oder Stony Peaks). Die Ameise, Termite oder den Schmetterling mit leerer Hand anklicken: der Spieler landet in einer dunklen Höhle im Berg statt auf dem Gipfel. Der Fallback auf Y 1 im Stein ist möglich, aber selten (1000 Fehlversuche nötig). R18 `:277` gilt für Erz- und Feature-Bänder. Einschlägig sind R18 `:279` (Heightmap) und R21 `:381-385` (wörtliche Y-Werte nur in den sechs OreSpawn-Dimensionen). | Ist das Ziel keine OreSpawn-Dimension, die Suche bei `WORLD_SURFACE`-Höhe + 1 der Spalte beginnen, begrenzt auf die maximale Bauhöhe. In den sechs Dimensionen bleibt 180. | medium |
| Termiten fressen nur das 1.7.10-Holzsortiment | `entity/portal/Termite.java:182-192` (`isWood`), `scan_it` `:204-267`, Fressen `:323`, `:331`; PORT-Kommentar `:173-181` aus der Zeit vor R22 | `Termite.java:125-127` | Bei `mobGriefing` Termiten neben eine Hütte aus Kirsch-, Mangroven-, Bambus-, Crimson- oder Warped-Planken, -Stufen oder -Treppen setzen, dazu Fichten- oder Birkenzäune, -Tore, -Türen, -Schilder oder -Druckplatten: die Termiten gehen vorbei. Dieselben Formen aus Eiche werden gefressen. Akazien- und Schwarzeichentreppen fehlten schon im Original (das ist schwächer belegt). | `#minecraft:planks`, `wooden_slabs`, `wooden_stairs`, `wooden_fences`, `fence_gates`, `wooden_doors`, `standing_signs` (und `wall_signs`), `wooden_pressure_plates`, `beds`. Crafting Table, Bookshelf und CRYSTAL_PLANKS bleiben einzeln. | medium |

### animals-landmobs

| Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere |
|---|---|---|---|---|---|
| Würmer sterben in Mud, Rooted Dirt, Moos, Muddy Mangrove Roots und Mycelium | `entity/worm/WormSupport.java:64-67`; benutzt in `WormSmall.java:193/210/225`, `WormMedium.java:180/197/212`, `WormLarge.java:257`; das Javadoc `:61-62` zitiert die R18-Zeile, die R22 ersetzt hat | `WormSmall.java:103,122,138`; `WormMedium.java:107,126,142`; `WormLarge.java:135` | Ein Small-Worm-Ei auf einem Moosblock in einer Lush Cave oder auf Mud im Mangrovensumpf benutzen: der Wurm verschwindet beim ersten Grabtick ohne Drops. Ein Large Worm verliert dort seine ganze Brut. In Stein, Erde und Gras gräbt er normal. Die Steinhälfte (zwei `ore_replaceables`-Tags) ist korrekt. Mycelium tötete Würmer auch im Original, fällt aber unter die R22-Kategorie. | Die vier Erdblöcke durch `BlockTags.DIRT` ersetzen, den Steinteil behalten und das Javadoc auf R22 umstellen. | medium |
| Lizard ignoriert schwarzen, blauen, braunen und weißen Farbstoff | `entity/cannonfodder/CannonFodderSupport.java:66-73` (`isLegacyDye`); `Lizard.java:102` (Tempt, Prio 3), `:219` (Anfreunden), `:228-233` (Zurücksetzen) | `Lizard.java:48` (`EntityAITempt ... Items.dye`), `:140` | Mit weißem oder schwarzem Farbstoff in der Hand in der Nähe eines normalen Lizard stehen: er folgt nicht. Rechtsklick: Rauchpartikel, der Farbstoff bleibt, der Buddy wird gelöscht. Mit rotem oder gelbem Farbstoff: Anlocken, Herzen, Folgen für 3000–4999 Ticks. Das Javadoc (`:58-65`) nennt die Lücke selbst. Auf Kampf-Lizards (`is_activated==2`) ist der Zweig ohnehin unerreichbar (`entity-10.md:158`). | `isLegacyDye(stack) \|\| stack.is(Tags.Items.DYES)`. | medium |
| Beaver fällt nur Eiche, Fichte, Birke, Tropenholz und Eichenzaun/-tor/-schild | `entity/herbivore/Beaver.java:126-131` (`isWood`), `:158-161` (`isBlockLog`), `:477-481` (Spawnboden Laub), `:285` (`breakRecursor`); PORT-Javadoc `:120-124` („not #minecraft:logs“) | `Beaver.java:66`, `:294` | Einen Beaver neben einen Kirsch- oder Mangrovenbaum, einen Fichtenzaun oder ein Birkenschild setzen: er geht nicht hin und nagt nichts an. Einen Eichenbaum daneben fällt er. Auf Akazien- oder Schwarzeichenlaub schlägt die Spawnprüfung fehl (Spawn-Eier und Regeln sind schon gebaut). | `isWood`: `BlockTags.LOGS_THAT_BURN`, `WOODEN_FENCES`, `FENCE_GATES`, `STANDING_SIGNS` plus die zwei OreSpawn-Stämme. `isBlockLog`: `LOGS_THAT_BURN`. Spawnboden: `BlockTags.LEAVES`. `legacyItemFromBlock` (`:142-151`) mitziehen, sonst droppt ein Fichtenzaun einen Eichenstamm. | medium |
| Molenoid gräbt und sieht nicht durch Mud, Rooted Dirt, Moos, Suspicious Sand/Gravel und neues Laub | `entity/monster/Molenoid.java:321-322` (Graben), `:494-500` (`MyCanSee`); `MonsterSupport.java:98-106`; `CannonFodderSupport.java:79-81` | `Molenoid.java:223`, `:376-382` | Einen Molenoid (Spawn-Ei) in einen Mangrovensumpf oder eine Lush Cave setzen, der Spieler steht hinter Mud oder Moos: `MyCanSee` bricht am ersten solchen Block ab, er nimmt den Spieler nicht als Ziel. Sein Tunnel lässt Mud, Moos, Schwarzeichen- und Kirschlaub stehen. In normaler Erde und Sand sieht und gräbt er. | Graben: `BlockTags.DIRT`, `BlockTags.SAND`, GRAVEL + SUSPICIOUS_GRAVEL, `BlockTags.LEAVES`. `MyCanSee`: dieselben Erd-, Sand- und Kiesmengen plus Gras. | medium |
| Camarasaurus frisst kein Akazien-, Schwarzeichen-, Mangroven-, Kirsch- oder Azaleenlaub | `entity/herbivore/Camarasaurus.java:228-236` (`isPlantAt`), `:285-287` (Laubzweig); Javadoc `:222-227` | `Camarasaurus.java:90,101,116,127,142,153`, `:198` | **Korrigierte Reproduktion:** In Savanne oder Kirschhain frisst er weiter Gras und heilt, der Befund ist dort unsichtbar. Sichtbar wird er auf einer freigeräumten oder beplankten Fläche ohne Gras und Ranken neben einem Akazien-, Schwarzeichen-, Kirsch- oder Mangrovenbaum. Das Laub rührt er dort nicht an. Neben einer Eiche frisst er das Laub und rülpst. | `BlockTags.LEAVES` in `isPlantAt` und im Laubzweig. | low |
| Velocity Raptor frisst keine Kornblume, Maiglöckchen, Torchflower, Wither Rose, Pitcher Plant | `entity/rider/RiderSupport.java:54-62` (`isRaptorFood`); `CannonFodderSupport.java:89`, `:94-97`; `scan_it` in `VelocityRaptor.java:195-258`, Fressen `:315-326` | `VelocityRaptor.java:92,103,118,129,144,155` | Einen verletzten Velocity Raptor in ein Gehege setzen, in dem nur Kornblumen oder Maiglöckchen wachsen: er geht nicht hin, frisst nicht, heilt nicht. Mit einem Mohn im Gehege frisst er. | `BlockTags.SMALL_FLOWERS` und `BlockTags.TALL_FLOWERS` zu den Gras-, Dead-Bush- und Doppelpflanzen-Prüfungen hinzufügen. | low |
| Chipmunk gräbt kein Rooted Dirt, Mud, Moos, Muddy Mangrove Roots | `entity/cannonfodder/Chipmunk.java:157` (`CannonFodderSupport.isLegacyDirt`, `:79-81`) | `Chipmunk.java:91` (`Blocks.dirt \|\| Blocks.farmland`) | Einen Chipmunk bei `mobGriefing` auf einen Moos- oder Rooted-Dirt-Boden setzen: er entfernt den Block unter sich nie (1/600 pro Tick). Auf Erde, Grober Erde oder Podsol gräbt er. | Den gemeinsamen Helfer auf Tag-Basis umstellen (siehe Abschnitt 4). Grasblock und Mycelium waren in 1.7.10 eigene Blöcke und hier nicht erfasst, also bewusst entscheiden. | low |
| Gezähmter Ostrich setzt sich nicht auf Mud, Rooted Dirt, Moos oder Suspicious Sand/Gravel | `entity/rider/RiderSupport.java:40-46` (`isSittingGround`), aufgerufen in `Ostrich.java:284-288` | `Ostrich.java:220` | Als Besitzer mit irgendeinem Item in der Hand (nicht leer, kein Dead Bush) innerhalb von 4 Blöcken einen gezähmten Ostrich auf Mud oder Rooted Dirt anklicken: er setzt sich nicht. Auf Sand oder Erde setzt er sich. | `BlockTags.SAND`, GRAVEL + SUSPICIOUS_GRAVEL, `BlockTags.DIRT`, FARMLAND. Mycelium ist auch hier eine Ermessensfrage. | low |

## 3. Strittige Punkte

- **Alien ignoriert Soul Torches** (`entity/monster/Alien.java:327-331`, Original `Alien.java:232-295`, `:348`; low). Prüfer 1 sagt **widerlegt**: Das Original hat den Redstone-Fackelblock bewusst ausgelassen, die Liste nennt also einzelne Blöcke, und nach R22 bleiben einzeln benannte Blöcke einzeln. Prüfer 2 sagt **echt**: `SOUL_TORCH` und `SOUL_WALL_TORCH` sind dieselben Klassen (`TorchBlock`/`WallTorchBlock`, `Blocks.java:2320`, `:2327`), Redstone-Fackeln eine andere, gemeint war also die Kategorie „Fackel“. Entscheidung offen. Wird es übernommen, genügen zwei Zeilen in `isTorch`.

## 4. Fehlerklassen, die mehrfach vorkommen

### 4.1 R22: 1.7.10-Kategorie als feste Blockliste (18 Befunde)

Das Muster ist fast immer dasselbe: ein Helfer bildet die 1.7.10-Metadaten sauber
auf 1.21.1-Blöcke ab und hört dort auf. Die meisten Stellen sind älter als R22
(2026-09-13). Mehrere tragen noch einen `// PORT:`-Kommentar, der die schmale
Liste mit R18 begründet.

Gemeinsame Helfer: eine Korrektur dort behebt mehrere Befunde auf einmal.

| Helfer | Fundort | bekannte Aufrufer | weitere Verdachtsfälle |
|---|---|---|---|
| `CannonFodderSupport.isLegacyDirt` | `:79-81` | Chipmunk `:157`, Molenoid `:321`, `:495`, RiderSupport `:43` | Gazelle `:523` (Spawnboden) |
| `CannonFodderSupport.isLegacyDoublePlant` / `isLegacyTallGrass` | `:94-97`, `:89` | RiderSupport (Raptor) | alle weiteren Aufrufer auf Pitcher Plant prüfen |
| `CannonFodderSupport.isLegacyDye` | `:66-73` | Lizard | weitere Farbstoff-Nutzer |
| `MonsterSupport.isLegacySand` / `isLegacyLeaves` | `:98-106` | Molenoid | alle weiteren Aufrufer |
| `OneUse.isDirt` | `:60-67` | (Referenz für UltimateHoe) | selbst nur 1.7.10-Erde |
| `WorldGenAbstractTree.isDirt` | `:37-39` | Baumgenerierung der Setzlinge | gleiche Fünferliste wie CropBlocks |
| `CropBlocks.isGrassDirtOrFarmland`, `ReedLikePlant.isGrass/isDirt` | s. o. | alle Pflanzen | – |
| `MothSupport` | `:182-188` | – | gleiches Laubmuster, ungeprüft |
| `CompanionSupport.isRedFlower` | `:101-105` | Girlfriend | Rose-Sword-Rezept (W12) |

Greps, die die Klasse finden (in `src/main/java`):

```
Blocks\.(OAK|SPRUCE|BIRCH|JUNGLE)_(LOG|WOOD|LEAVES|PLANKS|SLAB|STAIRS|FENCE|SAPLING)
Blocks\.(DIRT|COARSE_DIRT|PODZOL|GRASS_BLOCK)\b
Blocks\.(STONE|DEEPSLATE)\b
Blocks\.(SAND|RED_SAND|GRAVEL)\b
Blocks\.(POPPY|DANDELION|SHORT_GRASS|FERN|TORCH)\b
isLegacy[A-Z]\w*
Blocks\.(IRON_ORE|GOLD_ORE|COAL_ORE|DIAMOND_ORE)\b
```

Und im Original die Gegenstücke `Blocks\.(stone|dirt|grass|sand|gravel|log|leaves|planks|sapling|tallgrass|red_flower|yellow_flower|double_plant|wool|fence)`
sowie `Items\.dye`. Jeder Treffer, der als Kategorie gemeint war, braucht einen
Tag. Bei jeder Umstellung auf `#minecraft:dirt` entscheiden, ob Mycelium und der
Grasblock mit hinein sollen: beide waren in 1.7.10 eigene Blöcke, und mehrere
Aufrufer prüfen Gras schon separat oder haben eine eigene Tabelle dafür (Sifter).

### 4.2 Feste Y-Werte außerhalb der OreSpawn-Dimensionen

Der Teleporter setzt `180` wörtlich ein, obwohl R21 das nur in den sechs
eigenen Dimensionen erlaubt. Suchen: `posY = 1\d\d`, `for \(\w+ = (100|128|180|256)`,
`getHeight\(\)`-freie Oberflächensuchen und jede Stelle, an der ein Mob oder
Item einen Zielpunkt in einer Vanilla-Welt bestimmt.

### 4.3 1.7.10-Flag 2 falsch übersetzt

Flag 2 in 1.7.10 heißt in 1.21.1 `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`, sobald
ein Volumen geräumt wird. Der Miner's Dream hat das bereits, der Instant Garden
nicht. Suchen: `UPDATE_CLIENTS\b` ohne `UPDATE_KNOWN_SHAPE` in derselben Zeile
sowie alle Aufrufer von `FastBlocks.setBlockFast` mit `flags` = 2. Das Javadoc
von `FastBlocks` behält Form-Updates für Strukturen bewusst bei. Für Items und
Mobs, die Blöcke räumen, gilt das nicht.

### 4.4 Geerbtes 1.7.10-Verhalten ohne Gegenstück in 1.21.1

- **Schwertblock:** Im Original jede `extends ItemSword`-Klasse mit
  `getMaxItemUseDuration` gegen `extends BlockingSword` im Port abgleichen
  (`grep -l "extends ItemSword"` im Original, `grep "extends SwordItem"` im Port).
  Der Big Hammer war der einzige Treffer im Befund. Ein Lauf über alle
  ItemSword-Unterklassen ohne Override lohnt sich trotzdem, weil auch die mit
  72000 Ticks geblockt haben.
- **Brennwert:** Jeder Block mit `Material.wood` im Original war
  Vanilla-Brennstoff. `grep "Material.wood"` im Original gegen die
  `furnace_fuels`-Data-Map.
- **Todesnachricht:** Jede `extends TamableAnimal`-Klasse im Port schickt dem
  Besitzer eine Chatnachricht. `grep "extends TamableAnimal"` und entscheiden,
  ob das eine Regel wird oder überall `die` überschrieben wird.

### 4.5 Registrierungslücken, die Hand-off-Notizen als „offen“ führen

Die zwei Companion-Eier standen seit W04 als Lücke in `W06.md:242` und
`W07.md:259`, keine Welle hat sie übernommen. Der Gegentest ist billig: alle
`egg*`-Einträge aus `docs/catalog/manifest.json` gegen die Registrierungen in
`ModItems` und gegen `models/item/` abgleichen. Genauso lohnt ein Durchgang über
alle „offen“-Punkte der Hand-off-Notizen W01–W07, ob eine Wellen-JSON sie
tatsächlich übernimmt.
