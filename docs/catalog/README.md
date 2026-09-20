# OreSpawn-Portierungskatalog

Einstieg in den Katalog für den privaten 1:1-Port von OreSpawn (TheyCallMeDanger, 1.7.10 Build 20.3) nach NeoForge 1.21.1. Modid `orespawn`, Paket `com.swbr.orespawn`.

Bindend vor allem, was hier steht: [`../DECISIONS.md`](../DECISIONS.md) (Entscheidungen R0 bis R17) und [`../STYLE.md`](../STYLE.md) (Bauregeln). Wo dieser Katalog eine Portierungsnotiz enthält, die einer Entscheidung widerspricht, gilt die Entscheidung; die bekannten Widersprüche stehen in Abschnitt 6.2.

Der Wellenplan in Abschnitt 4 ist die Eingabe für [`../../tools/workflows/wave.js`](../../tools/workflows/wave.js).

## 1. Was dieser Katalog ist

Ein vollständiges Nachschlagewerk über das Original: jede Entity, jedes Item, jeder Block, jedes Modell, jede Dimension, jeder Config-Schlüssel und jede der 586 Klassen des Jars (classes.json). Er beschreibt, was das Original tut, mit welchen Zahlen und an welcher Zeile, und was davon in 1.21.1 anders gebaut werden muss. Er ist kein Code und keine Spezifikation neuer Funktionen.

### 1.1 Entstehung

1. **Jar-Inventar.** `tools/catalog.py` liest `reference/jar/inventory.json` (aus `orespawn-1.7.10-20.3.jar`, SHA-1 `d43dbe9a400dc8df06418da3e04d36422b2176d7`, manifest) und erzeugt [`manifest.json`](manifest.json), [`classes.json`](classes.json) und die Tabellen 10 bis 60. Nicht von Hand ändern, sondern den Generator anpassen.
2. **Verhaltenskapitel.** Die 351 Klassen mit eigener Logik wurden in 23 Stapel geschnitten ([`batches.json`](batches.json)) und je Klasse aus dem dekompilierten Quelltext gelesen: Rolle, Werte, KI, Interaktion, Drops, Spawnen, Zustand, Sounds, Config, Portierungshinweise. Abdeckung 351 von 351 ohne Nachlauf.
3. **Designkapitel.** Neun Stapel beschreiben Aussehen und Größe aus Modell-JSON, Animationszusammenfassung, Texturen und Renderern: 134 von 134 Entities, dazu alle Items und Blöcke.
4. **Recherche.** Sechs Berichte unter [`../research/`](../research/): 01 bis 04 aus dem Web, 05 aus dem Jar gelesen, 06 mit den Modellkonventionen. Sie liefern Kontext und Namen.
5. **Wellenplan.** Abschnitt 4 ist per Skript aus classes.json, manifest.json und einem Referenzscan des Originalquelltexts berechnet und von Hand in Wellen geschnitten.

### 1.2 Rangfolge der Quellen

| Rang | Quelle | Ort | Wofür |
|---|---|---|---|
| 1 | Jar-Inventar | [`manifest.json`](manifest.json) (1,1 MB: mit Python auslesen, nie ganz lesen) | Ids, Anzeigenamen, Klassen, Attribute, Hitboxen, Spawnregeln, Renderer, Modelle, Texturpfade alt und neu, Materialien, Sounds, Config-Schlüssel |
| 2 | Dekompilierter Quelltext 20.2 | `reference/src-20.2/src/main/java/danger/orespawn/<Klasse>.java` | Verhalten mit Zeilenangabe. Abgleich gegen das 20.3-Jar in `reference/jar/diff_20.2_vs_20.3.txt`: 586 Klassen in Jar und Quelltext, keine nur auf einer Seite. SRG-Namen über `reference/jar/mcp/methods.csv` und `fields.csv` |
| 3 | Jar-Extrakte | `reference/jar/models/*.json` (Geometrie), `reference/jar/anim/*.json` (Animationszusammenfassung), `config_dump.txt`, `sounds_dump.txt`, `renderers_dump.txt`, `calls_OreSpawnMain.txt`, `extracted/` | Rohdaten hinter manifest und Kapiteln |
| 4 | Recherche | [`../research/01-mobs.md`](../research/01-mobs.md) bis [`06-models-design.md`](../research/06-models-design.md) (01 bis 04 aus dem Web) | Kontext, geringeres Vertrauen |

Widersprechen sich Quelltext und Recherche, gilt der Quelltext; jedes Kapitel nennt den Widerspruch in einer Zeile (gesammelt in Abschnitt 6.8). Für Zahlen, bei denen Config-Default und Code auseinanderlaufen, gilt nach R3 der Code.

### 1.3 So liest man ihn

- Jede Zahl trägt ihre Herkunft: `(Klasse.java:ZEILE)` oder `(manifest)`. `offen: <Grund>` heißt nicht auflösbar, nie geraten.
- In den Tabellen 10 bis 60 bedeutet `?` statisch nicht auflösbar; mehrere Werte in einer Zelle sind Verzweigungen im Originalcode (zahm/wild, PlayNicely, Größenstufen).
- Aufwand S, M, L, XL ist die Einstufung des jeweiligen Verhaltensstapels je Klasse; eine Zeilengrenze dahinter gibt es nicht.
- Bezeichner, Klassennamen, Registry-Ids und Methodennamen stehen englisch wie im Code, der Fließtext ist Deutsch.
- Reihenfolge beim Portieren einer Klasse: DECISIONS.md, dann das Verhaltenskapitel, dann der Originalquelltext vollständig, dann das Designkapitel, dann manifest.json für Ids und Texturen.

### 1.4 Dateien

**Erzeugte Tabellen und Daten** (`tools/catalog.py`, Rezepte und Biomzuordnung aus eigenen Läufen)

| Datei | Inhalt |
|---|---|
| [`10-entities.md`](10-entities.md) | 134 Entities: Id, Name, Klasse, Basis, Leben, Angriff, Tempo, Rüstung, Hitbox, Modell, Spawnregeln |
| [`20-items.md`](20-items.md) | 473 Items nach Art (armor, cage, food, item, seed, spawn_egg, tool, weapon) mit Konstruktor, Textur, 3D-Modell |
| [`30-blocks.md`](30-blocks.md) | 211 Blöcke nach Art (block, container, dried_egg_ore, leaves, ore, plant, torch) |
| [`40-models.md`](40-models.md) | 109 Modelle: Nutzer, Teile, Boxen, Textur, animierte Teile, Animations-Bytecode, GL-Aufrufe |
| [`50-materials.md`](50-materials.md) | Werkzeugmaterialien, Rüstungsmaterialien, Mob-Werte aus der Config |
| [`60-world-config-sounds.md`](60-world-config-sounds.md) | Dimensionen, Sounds, Config-Schlüssel, offene Punkte des Generators |
| [`manifest.json`](manifest.json) | Maschinenlesbare Grundwahrheit aller obigen Tabellen plus `texture_map` und `problems` |
| [`classes.json`](classes.json) | Domäne, Oberklasse und Zeilenzahl jeder der 586 Klassen |
| [`recipes.json`](recipes.json) | 381 Rezepte mit Quellzeile in `OreSpawnMain.java` |
| [`batches.json`](batches.json) | Zuschnitt der Verhaltens- und Designstapel |
| [`biome_map.json`](biome_map.json) | 1.7.10-`BiomeGenBase`-Felder der `addSpawn`-Aufrufe auf 1.21.1-Biome |

**Verhaltenskapitel** (`verhalten/`)

| Datei | Klassen | Inhalt |
|---|---|---|
| [`entity-01.md`](verhalten/entity-01.md) | 5 | TheKing, KingHead, TheQueen, QueenHead, PurplePower |
| [`entity-02.md`](verhalten/entity-02.md) | 4 | ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess |
| [`entity-03.md`](verhalten/entity-03.md) | 3 | Godzilla (Mobzilla), GodzillaHead, Kraken |
| [`entity-04.md`](verhalten/entity-04.md) | 11 | Acid bis BerthaHit |
| [`entity-05.md`](verhalten/entity-05.md) | 8 | BetterFireball bis Cephadrome |
| [`entity-06.md`](verhalten/entity-06.md) | 14 | Chipmunk bis DungeonBeast |
| [`entity-07.md`](verhalten/entity-07.md) | 13 | EasterBunny bis EntityMosquito |
| [`entity-08.md`](verhalten/entity-08.md) | 15 | EntityRainbowAnt bis GoldCow |
| [`entity-09.md`](verhalten/entity-09.md) | 13 | GoldFish bis LeafMonster |
| [`entity-10.md`](verhalten/entity-10.md) | 9 | Leon bis Peacock |
| [`entity-11.md`](verhalten/entity-11.md) | 14 | PitchBlack (Nightmare) bis Scorpion |
| [`entity-12.md`](verhalten/entity-12.md) | 9 | SeaMonster bis StinkBug |
| [`entity-13.md`](verhalten/entity-13.md) | 13 | Stinky bis VelocityRaptor |
| [`entity-14.md`](verhalten/entity-14.md) | 7 | Vortex bis WormSmall |
| [`itemblock-01.md`](verhalten/itemblock-01.md) | 59 | AmethystAxe bis ItemSparkFish |
| [`itemblock-02.md`](verhalten/itemblock-02.md) | 55 | ItemSpawnEgg bis ZooCage, AntBlock bis BlockRadish |
| [`itemblock-03.md`](verhalten/itemblock-03.md) | 41 | BlockRice bis RockBlock, Kristallofen und -werkbank samt Container, Screens und Block-Entity, GirlfriendOverlayGui |
| [`world-01.md`](verhalten/world-01.md) | 1 | GenericDungeon: 47 `make…`-Methoden, 38 Loot-Listen |
| [`world-02.md`](verhalten/world-02.md) | 5 | OreSpawnWorld, ChunkOreGenerator, MapGenMoreVillages, OreSpawnTeleporter, BiomeGenUtopianPlains |
| [`world-03.md`](verhalten/world-03.md) | 12 | ChunkProviderOreSpawn 1 bis 6, WorldProviderOreSpawn 1 bis 6 |
| [`world-04.md`](verhalten/world-04.md) | 5 | Trees, BasiliskMaze, CrystalMaze, NightmareDungeon, RubyBirdDungeon |
| [`core-01a.md`](verhalten/core-01a.md) | 1 | OreSpawnMain: Config, Registrierung, Spawns, globale Statics |
| [`core-01b.md`](verhalten/core-01b.md) | 1 | OreSpawnMain: Rezepte, Truhen-Loot, Dispenser-Verhalten |
| [`core-02.md`](verhalten/core-02.md) | 34 | Stat-Halter, Proxys, Dispenser, KI-Bausteine, Netzwerk und Eingabe |

**Designkapitel** (`design/`)

| Datei | Inhalt |
|---|---|
| [`design-entities-01.md`](design/design-entities-01.md) | 23 Entities: the_king bis bertha_hit |
| [`design-entities-02.md`](design/design-entities-02.md) | 23 Entities: boyfriend bis hoverboard |
| [`design-entities-03.md`](design/design-entities-03.md) | 23 Entities: emperor_scorpion bis girlfriend |
| [`design-entities-04.md`](design/design-entities-04.md) | 23 Entities: golden_apple_cow bis peacock |
| [`design-entities-05.md`](design/design-entities-05.md) | 23 Entities: nightmare bis stink_bug |
| [`design-entities-06.md`](design/design-entities-06.md) | 19 Entities: stinky bis small_worm |
| [`design-items-gear.md`](design/design-items-gear.md) | Items der Arten weapon, tool, armor, inklusive der 3D-Waffenmodelle |
| [`design-items-other.md`](design/design-items-other.md) | Items der Arten item, food, seed, cage, spawn_egg |
| [`design-blocks.md`](design/design-blocks.md) | alle Blockarten |

**Recherche** (`../research/`, Rang 4): [`01-mobs.md`](../research/01-mobs.md), [`02-dimensions-worldgen.md`](../research/02-dimensions-worldgen.md), [`03-items.md`](../research/03-items.md), [`04-history-license-ports.md`](../research/04-history-license-ports.md), [`05-jar-inventory.md`](../research/05-jar-inventory.md) (aus dem Jar gelesen), [`06-models-design.md`](../research/06-models-design.md) (belegte Modellkonventionen, auf die R8 verweist).

## 2. Inhalt in Zahlen

| Gegenstand | Zahl | Aufschlüsselung | Herkunft |
|---|---|---|---|
| Entities (registriert) | 134 | Basis EntityMob 54, EntityAnimal 24, EntityTameable 20, EntityThrowable 11, EntityLiving 8, EntityAmbientCreature 8, EntityCow 4, EntityArrow 2, EntityFishHook 1, EntityCreature 1, EntitySpider 1 | manifest |
| Entity-Klassen im Quelltext | 138 | vier ohne Registrierung: EntityCannonFodder (abstrakte Basis), EntityLavaLovingItem (toter Code), BetterFireball, ThunderBolt | classes.json, verhalten/entity-05.md, entity-07.md, entity-13.md |
| Items | 473 | cage 119 (114 CritterCage, 5 ZooCage), spawn_egg 114, item 95, armor 56, food 33, tool 28, weapon 23, seed 5 | manifest |
| Blöcke | 211 | dried_egg_ore 121 (119 Ancient Dried Spawn Eggs plus Ender-Pearl- und Eye-of-Ender-Block), block 32, plant 31, ore 13, leaves 8, torch 4, container 2 (Kristallofen an und aus, eine Id) | manifest |
| Modelle | 109 | 3584 Teile, jedes Teil genau eine Box; alle 109 mit Status ok; 19 mit GL-Aufrufen | manifest |
| Texturen | 1058 | Einträge der `texture_map` alt auf neu | manifest |
| Sounds | 126 Events | 307 Dateien; zwei Events umbenannt (`Beebuzz`, `MothraWings`) | manifest |
| Config-Schlüssel | 628 | OreSpawnMOBS 279, OreSpawnARMOR 196, OreSpawnWEAPONS 77, OreSpawnORES 52, OreSpawnTWEAKS 20, OreSpawnIDS 4 | manifest |
| Materialien und Mob-Werte | 15 / 14 / 59 | Werkzeugmaterialien, Rüstungsmaterialien, Mob-Stat-Einträge | manifest |
| Dimensionen | 6 | Utopia, Extreme (Mining), VillageMania, Islands (Danger), Crystal, Chaos | manifest |
| Natürliche Spawns | 348 Einträge | für 55 Entities; 304 mit Config-Guard, 44 ohne; Typ ambient 273, creature 51, waterCreature 22, monster 2 | manifest |
| Rezepte | 381 | shaped 189, shapeless 176, smelting 16 | recipes.json |
| Truhen-Loot und Dispenser | 9 / 134 | ChestGenHooks-Einträge; Dispenser-Verhalten für 133 Items | verhalten/core-01b.md |
| Strukturbaukasten | 47 / 38 | `make…`-Methoden und Loot-Listen in GenericDungeon; rund 60 Strukturaufrufe in OreSpawnWorld | verhalten/world-01.md, world-02.md |
| Klassen | 586 | entity 138, item 93, block 55, core_other 19, world 23, gui_tile 7, ai 12, network_input 4, model 109, render 126 | classes.json |
| Quelltextzeilen | 142342 | davon Modelle 40339, Renderer 6663, Entities 56262, Welt 16122 | classes.json |
| Verhaltenskapitel | 351 Klassen | 23 Stapel; OreSpawnMain in zwei Kapiteln, daher 352 Einstufungen: S 193, M 109, L 38, XL 12 | batches.json, Stapelergebnisse |
| Designkapitel | 134 Entities | 9 Stapel, dazu alle Items und Blöcke | batches.json |
| Generator-Probleme | 6 | `crystalfurnace` doppelt; `pizza_item`, `ducttape_item` umbenannt; ModelElevator als unbenutzt gemeldet (falsch, siehe 6.6); zwei Sound-Umbenennungen | manifest `problems` |

## 3. Portierungsaufwand je Bereich

Bereiche nach den Stapel-Schlüsseln; Items, Blöcke und GUI teilen sich die `itemblock`-Stapel und sind nach classes.json getrennt. Zeilen aus classes.json.

| Bereich | Stapel | Klassen | S/M/L/XL | Zeilen | Größte Risiken |
|---|---|---|---|---|---|
| Bosse und Royals | entity-01 bis 03 | 12 | 1/5/3/3 | 10827 | Leben 7000, 6000, 4000, 3000 und 1500 über dem Deckel 1024 (manifest, R4); 1.7.10-Rüstungsformel mit Immunität ab 25 (R5); Nahkampfwerte als Feld statt Attribut (King bis 56000, Queen bis 225000, verhalten/entity-01.md); Hitbox-Wechsel über PlayNicely; Zweit-Hitboxen ohne eigenen Living-Tick; Mobzilla mit bis zu 2178 Blockzugriffen je Tick (verhalten/entity-03.md); Spieler tragen, einfrieren, zurückstoßen braucht serverseitigen Impuls |
| Mobs, Begleiter, Projektile | entity-04 bis 14 | 126 | 42/57/21/6 | 45435 | handgeschriebene Flug- und Reitphysik mit einem globalen Tastenzustand; Client-Klassen im gemeinsamen Code (Elevator, SpiderRobot); Verzauberungen in Drops über dem Maximum; Vanilla-Umbauten (Items.fish, EntityAIMoveIndoors, Metadaten-Blöcke, Pferdetypen); doppelter Tick und beidseitige Einschläge bei Projektilen; Spawner-Erkennung per Blockscan; Laufzeit-Inselbau (Island, IslandToo) |
| Items | itemblock-01, 02 | 93 | 75/14/4/0 | 7638 | Werkzeug- und Rüstungsmaterialien zur Registrierzeit aus der Config (R3); Rüstungssummen 34, 38, 42, 48 über ARMOR 30 (R5); Schwung- und Linksklick-Hooks ohne Mixin (Bertha, ItemCreeperLauncher, ItemWrench); 114 Käfige und 114 Eier; Einweg-Bauitems schreiben über Chunkgrenzen; Zustand in Item-Singletons (ItemThunderStaff, UltimateSword) |
| Blöcke | itemblock-02, 03 | 55 | 35/19/1/0 | 5079 | Stand- und Wandfackel brauchen zwei Ids; Stapelpflanzen verlieren ihre Zielhöhe im 4-Bit-Metadatum (BlockCorn, BlockQuinoa, BlockTomato); Opazität hängt an einem Client-Static; DungeonSpawnerBlock baut 50 Strukturen zur Laufzeit; Mob-Spawns ohne Obergrenze (AntBlock, CrystalAntBlock, OreBasicStone); eigener Blattzerfall |
| GUI und Block-Entity | itemblock-03 | 7 | 4/1/2/0 | 1075 | Kristallofen mit fester Kochzeit 150 Ticks, eigener Brennwerttabelle und XP über `recipesUsed`; GirlfriendOverlayGui zeigt 45 Klassen und braucht bei virtuellem Leben ein synchronisiertes Lebensverhältnis |
| Weltgenerierung | world-01 bis 04 | 23 | 11/4/6/2 | 16122 | etwa 13 Bauten größer als das 3x3-Chunk-Fenster (Structure, R12); globaler Sperrzähler `recently_placed` nicht nachbildbar; `Math.random` und `world.rand` statt Seed; 1.7.10-Rauschterrain je Dimension; der Chunk-Clip der Erzadern ist Spielverhalten (Crystal-Adern behalten etwa 29 % ihrer Blöcke, verhalten/world-03.md); 1.7.10-Höhen gegen `min_y` −64 |
| Hauptklasse OreSpawnMain | core-01a, 01b | 1 | XL und M | 6575 | 628 Config-Schlüssel mit relativen Klemmungen; 348 Spawn-Einträge mit Guards und Systemdatum; globale Statics; Direktschreiber `setBlockFast` ohne Licht-Update; 381 Rezepte mit Wildcard-Metas und schadensempfindlichen Zutaten |
| Kern-Hilfen, Dispenser, Proxys | core-02 | 18 | 13/4/1/0 | 1410 | Stat-Halter zur Registrierzeit; 133 Entity-Renderer und 8 Item-Renderer in ClientProxyOreSpawn; Harvest-Level 4, 5 und 10 ohne Vanilla-Tag; MyUtils prüft `instanceof` auf 31 spätere Klassen |
| KI | core-02 | 12 | 8/4/0/0 | 1059 | Goal-Takt in 1.21.1 verschiebt die 1/30- und 1/90-Chancen und das 60-Tick-Sichtverlust-Timeout; Tanzdrehungen gegen BodyRotationControl; Valentinstagsziele ohne Kreativprüfung |
| Netzwerk und Eingabe | core-02 | 4 | 4/0/0/0 | 120 | ein Tastenzustand für alle Reiter; Schreiben auf dem Netty-Thread; Tastencode 56 als linke Alt-Taste nicht belegt |
| Modelle und Renderer | design-* | 235 | ohne Einstufung | 47002 | 109 Modelle mit 3584 Teilen von Hand animieren (R8); 12 Modelle mit `glBlendFunc`-Durchgang; Client-IK (RenderSpiderRobotInfo, RenderGiantRobotInfo); acht BEWLR-Items; framegebundene Animationen (ModelRotator, ModelChainsaw) |

## 4. Abhängigkeitsreihenfolge

Zwölf Wellen W01 bis W12. Welle W00 ist erledigt und liegt im Baum: `OreSpawn.java`, `config/EarlyConfig.java`, `config/OreSpawnConfig.java` (generiert), `registry/ModSounds.java` (generiert), `gametest/W00GameTests.java`, 109 `client/model/geom/*Geometry.java` (generiert) und die Assets.

**Jede der 586 Klassen aus classes.json steht in genau einer Welle. Nicht einordenbar: keine.** Klassen, die nicht portiert werden (toter Code oder in 1.21.1 ersatzlos), stehen in W01 zum Abhaken.

### 4.1 Wie die Reihenfolge berechnet ist

- **Referenzen** kommen nicht nur aus den DEPS der Kapitel, sondern aus einem Scan des Originalquelltexts: Klassennamen, `OreSpawnMain.<Feld>` (Items und Blöcke über das Feld `field` im manifest, Singletons und Stat-Halter über den Feldtyp) und Entity-Namensliterale (`registerModEntity`/`registerGlobalEntityID` in `reference/jar/calls_OreSpawnMain.txt`). OreSpawnMain, OreSpawnConstants und die Proxys zählen nicht, weil sie alles referenzieren.
- **Regel:** Eine Klasse steht in der frühesten Welle, in der ihre tragenden Abhängigkeiten existieren. Modelle und Renderer stehen in der Welle ihrer Entity (manifest `renderers`, `used_by`); von Hand zugeordnet sind die zwölf Renderer ohne Entity-Bezug (Item-Renderer, RenderInfo, RenderSpinner, die beiden IK-Hilfen), die sieben Item-Modelle und ModelElevator, das manifest fälschlich als unbenutzt führt.
- **Zyklen** gibt es viele (Zielauswahl-Listen nennen sich gegenseitig). Sie werden an drei Stellen gebrochen; insgesamt zeigen 220 Referenzen auf eine spätere Welle:
  1. **MyUtils** (W01) nennt 31 spätere Klassen in `isRoyalty`, `isAttackableNonMob` und `isIgnoreable`. Je Prädikat eine Java-Schnittstelle in W01; jede spätere Entity implementiert die passende. Unterklassen erben sie wie im Original (Mothra erbt über EntityButterfly zugleich ignorierbar und angreifbar, verhalten/core-02.md). Keine Datapack-Tags, weil sie die Unterklassen-Semantik verlieren.
  2. **ItemSpawnEgg und DispenserBehaviorOreSpawnEgg** (W01) werden eine generische Klasse mit EntityType-Supplier; jede Welle registriert die Eier ihrer Entities. Das deckt 109 Verweise und gibt jeder Welle Eier für ihre GameTests. Texturfrage siehe 6.2.
  3. **Rückwärtskanten:** Die übrigen 80 Referenzen in 41 Klassen (Tabelle 4.14). Die frühere Welle portiert die Klasse ohne den betroffenen Zweig und markiert ihn mit `// PORT: TODO Wxx`; der Worker der späteren Welle trägt ihn nach und bekommt die Datei dafür in `owns`.
- **Querschnitt je Welle:** Registry-Holder, Renderer- und Layer-Registrierung, Attribut- und Spawn-Placement-Events wachsen jede Welle über die `registry_entries` von `wave.js`; ihr Eigentümer ist der Integrator.

### 4.2 W01 – Fundament

Ziel: alle Querschnittsdienste stehen, bevor Inhalt existiert, und der dedizierte Server startet.

Neu ohne Originalklasse: Registry-Holder nach STYLE.md; `combat` mit virtuellen Lebenspunkten (R4) und der 1.7.10-Rüstungsformel über `LivingIncomingDamageEvent` (R5); die drei MyUtils-Schnittstellen; Spieler-Attachment für den Steigen-Tastenzustand, Payload und KeyMapping (R15); generische Spawn-Ei-Klasse mit den fünf Vanilla-Eiern `eggwitherskeleton`, `eggenderdragon`, `eggsnowgolem`, `eggirongolem`, `eggwitherboss` (manifest); Client-Setup als Nachfolger von ClientProxyOreSpawn; Schreibhilfen auf `WorldGenLevel` als Ersatz für `setBlockFast` und `setBlockIDWithMetadataInChunk`; die Stat-Halter lesen über EarlyConfig und OreSpawnConfig.

Nur abhaken, nicht portieren: EntityLavaLovingItem, PortalBlock, RockBlock, Slice, NightmareDungeon (nie instanziiert, laut Verhaltenskapiteln toter Code), OreSpawnConstants (numerische Ids, R2), OreSpawnSounds (ersetzt durch das generierte ModSounds), CommonProxyOreSpawn (Proxy-Muster entfällt).

GameTests: Schaden an einem Test-Entity mit Originalleben 7000 wird mit 1024/7000 skaliert; bei Rüstung 25 kommt nach Originalformel kein blockbarer Schaden an; die Vanilla-Eier spawnen ihr Entity; die Payload ändert nur den Zustand des sendenden Spielers.

Umfang: 24 Klassen mit Verhaltenskapitel (S 17 / M 5 / L 1 / XL 1), 2 Modell- und Renderer-Klassen, 8954 Zeilen Originalquelltext (classes.json).

- **Kern:** `OreSpawnMain` XL, `OreSpawnConstants` S, `OreSpawnSounds` S, `ArmorStats` M, `WeaponStats` M, `MobStats` M, `OreStats` M, `CommonProxyOreSpawn` S, `ClientProxyOreSpawn` L, `MyUtils` S, `DispenserBehaviorOreSpawnEgg` S
- **Netzwerk/Eingabe:** `KeyHandler` S, `RiderControl` S, `RiderControlMessage` S, `RiderControlMessageHandler` S
- **KI:** `GenericTargetSorter` S, `MyEntityAIWander` S, `MyEntityAIWanderALot` S
- **Items:** `ItemSpawnEgg` M, `Slice` S
- **Entities:** `EntityLavaLovingItem` S
- **Blöcke:** `PortalBlock` S, `RockBlock` S
- **Welt:** `NightmareDungeon` S
- **Modelle und Renderer (nur `client`):** `RenderInfo`, `RenderSpinner`

### 4.3 W02 – Rohstoffe, Blöcke, Pflanzen, Nahrung

Ziel: alle Blöcke, Rohstoffe, Speisen und Feldfrüchte ohne Entity-Bezug, einschließlich der Kristallblöcke der Crystal-Dimension und der 121 Dried-Egg-Blöcke. Noch keine Weltgenerierung.

Hinweise: Fackeln brauchen Stand- und Wandvariante; die Zielhöhe der Stapelpflanzen ist eine offene Entscheidung (6.7); Blätter bekommen eigenen Zerfall statt Vanilla-Distanz; die Umwandlung der Dried Eggs in Eier läuft über Rezepte (W12); die Opazität der Kristallblöcke ist Client-Rendering (6.7).

Schnitt: Erze und Speicherblöcke, Kristallblöcke, Pflanzen mit Saat-Items, Blätter und Stämme, Nahrung und Rohstoffe.

GameTests: jeder Block setzbar und abbaubar mit Originaldrop; Feldfrüchte wachsen und droppen; Blätter ohne Stamm zerfallen; Lavafoam prallt ab; RTPBlock versetzt einen Spieler.

Umfang: 59 Klassen mit Verhaltenskapitel (S 43 / M 16 / L 0 / XL 0), 0 Modell- und Renderer-Klassen, 4052 Zeilen Originalquelltext (classes.json).

- **Items:** `IngotTitanium` S, `IngotUranium` S, `ItemSalt` S, `ItemCrystalSticks` S, `ItemPopcorn` S, `ItemStrawberry` S, `ItemSunFish` S, `ItemFireFish` S, `ItemLavaEel` S, `ItemSparkFish` S, `ItemGenericFish` S, `ItemRadish` S, `ItemCornCob` S, `ItemTomato` S, `ItemLettuce` S, `ItemStrawberrySeed` S, `ItemPizza` S, `ItemDuctTape` S, `ItemAppleSeed` M, `ItemExperienceTreeSeed` S
- **Blöcke:** `OreUranium` S, `OreTitanium` S, `OreRuby` S, `OreAmethyst` S, `OreCrystal` S, `OreCrystalCrystal` S, `OreSalt` S, `OreBasicStone` S, `OreGenericEgg` M, `BlockUranium` S, `BlockTitanium` S, `BlockRuby` S, `BlockCrystal` S, `CrystalGrass` S, `CrystalWood` S, `BlockCrystalTreeLog` S, `BlockSkyTreeLog` M, `BlockDuplicatorLog` S, `BlockCrystalLeaves` M, `BlockCrystalPlant` M, `BlockCrystalTorch` M, `BlockExtremeTorch` M, `BlockAppleLeaves` M, `BlockScaryLeaves` M, `BlockExperienceLeaves` M, `BlockExperiencePlant` S, `MyBlockFlower` S, `MoleDirtBlock` S, `Lavafoam` M, `RTPBlock` S, `BlockPizza` M, `BlockDuctTape` M, `BlockRadish` S, `BlockRice` S, `BlockStrawberry` S, `BlockLettuce` S, `BlockTomato` M, `BlockCorn` M, `BlockQuinoa` M

### 4.4 W03 – Ausrüstung und Werkstatt

Ziel: 15 Werkzeug- und 14 Rüstungsmaterialien (manifest) mit allen Werkzeugen, den Schwertern ohne Beschwörung und allen 56 Rüstungsteilen; Kristallofen und Kristallwerkbank; Einweg-Bauitems.

Hinweise: Tier und ArmorMaterial aus EarlyConfig (R3), Schadensumrechnung nach R6, Verzauberungen nach R7; `crystalfurnace` ist ein Block mit `LIT` (R2); OreSpawnGUIHandler wird MenuType- und Screen-Registrierung (R15); die drei Großwaffen der UltimateSword-Varianten als BEWLR (R8). PvP-Schutz der Ultimate-Werkzeuge gegen Girlfriend und Boyfriend ist Rückwärtskante nach W04.

Schnitt: Tiers und Werkzeuge, Rüstung, Kristallofen und Werkbank, Bauitems, Großwaffen-Renderer.

GameTests: Haltbarkeit und Schaden je Material gegen manifest; Rüstungsreduktion eines Queen-Sets nach R5; Ofen schmilzt in 150 Ticks; InstantShelter, InstantGarden, ItemMinersDream und ZooCage bauen vollständig.

Umfang: 50 Klassen mit Verhaltenskapitel (S 40 / M 7 / L 3 / XL 0), 6 Modell- und Renderer-Klassen, 4707 Zeilen Originalquelltext (classes.json).

- **Items:** `AmethystAxe` S, `AmethystHoe` S, `AmethystPickaxe` S, `AmethystShovel` S, `AmethystSword` S, `EmeraldAxe` S, `EmeraldHoe` S, `EmeraldPickaxe` S, `EmeraldShovel` S, `EmeraldSword` S, `RubyAxe` S, `RubyHoe` S, `RubyPickaxe` S, `RubyShovel` S, `RubySword` S, `CrystalAxe` S, `CrystalHoe` S, `CrystalPickaxe` S, `CrystalShovel` S, `CrystalSword` S, `UltimateAxe` S, `UltimateHoe` S, `UltimatePickaxe` M, `UltimateShovel` S, `UltimateSword` L, `NightmareSword` S, `PoisonSword` S, `MantisClaw` S, `BigHammer` S, `ExperienceSword` M, `ItemOreSpawnArmor` L, `InstantShelter` M, `InstantGarden` M, `ItemMinersDream` M, `StepUp` S, `StepDown` S, `StepAccross` S, `ZooCage` S, `ExperienceCatcher` S, `ItemCreeperLauncher` S, `ItemZooKeeper` S, `ItemNetherLost` S
- **Blöcke:** `CrystalFurnace` M, `CrystalWorkbench` S
- **GUI/Block-Entity:** `TileEntityCrystalFurnace` L, `ContainerCrystalFurnace` M, `CrystalFurnaceGUI` S, `ContainerCrystalWorkbench` S, `CrystalWorkbenchGUI` S, `OreSpawnGUIHandler` S
- **Modelle und Renderer (nur `client`):** `RenderBattleAxe`, `RenderChainsaw`, `RenderQueenBattleAxe`, `ModelBattleAxe`, `ModelChainsaw`, `ModelQueenBattleAxe`

### 4.5 W04 – Projektile, Fernkampf, Girlfriend und Boyfriend

Ziel: alle Wurf- und Schuss-Entities mit Items und Dispenser-Verhalten, die Bertha-Schwerter, Ultimate Bow, Skate Bow, Ultimate Fishing Rod, dazu Girlfriend, Boyfriend und das Hoverboard mit ihren KI-Bausteinen. Die Begleiter kommen hierher, weil Schuhe, Ultimate-Pfeile, Hoverboard und Begleiter sich gegenseitig referenzieren und viele spätere Klassen sie in Ziellisten nennen.

Hinweise: LaserBall zuerst als Basis mit Modusflags; BetterFireball und ThunderBolt brauchen neue Registry-Ids (6.6); der doppelte Tick von BetterFireball wird bewusst nachgebaut; BerthaHit braucht einen Schwung-Hook ohne Mixin (6.3); UltimateFishHook muss FishingHook erben; Girlfriend 62 und Boyfriend 48 Texturvarianten (R16).

Schnitt: LaserBall-Familie und kleine Wurfgeschosse, BetterFireball und ThunderBolt, Pfeile und Angel, Bertha-Familie, Dispenser, Girlfriend mit KI, Boyfriend und Hoverboard.

GameTests: jedes Projektil trifft mit Originalschaden und Nebenwirkung; Dispenser feuern; Girlfriend lässt sich zähmen, folgt und wirft Schuhe; Hoverboard fährt mit Reiter.

Umfang: 52 Klassen mit Verhaltenskapitel (S 31 / M 17 / L 3 / XL 1), 16 Modell- und Renderer-Klassen, 9640 Zeilen Originalquelltext (classes.json).

- **Entities:** `LaserBall` M, `Acid` S, `IceBall` S, `DeadIrukandji` S, `BetterFireball` M, `ThunderBolt` S, `WaterBall` S, `InkSack` S, `SunspotUrchin` S, `Shoes` S, `EntityThrownRock` M, `RockBase` M, `BerthaHit` M, `UltimateArrow` M, `IrukandjiArrow` M, `UltimateFishHook` L, `Girlfriend` XL, `Boyfriend` L, `Elevator` L
- **Items:** `Bertha` M, `UltimateBow` M, `SkateBow` M, `UltimateFishingRod` M, `ItemAcid` S, `ItemIceBall` S, `ItemIrukandji` S, `ItemIrukandjiArrow` S, `ItemLaserBall` S, `ItemRayGun` M, `ItemRock` S, `ItemShoes` S, `ItemSunspotUrchin` S, `ItemThunderStaff` S, `ItemWaterBall` S, `ItemSifter` M, `ItemElevator` S
- **Kern:** `MyDispenserBehaviorAcid` S, `MyDispenserBehaviorArrow` S, `MyDispenserBehaviorDeadIrukandji` S, `MyDispenserBehaviorIceball` S, `MyDispenserBehaviorLaserball` S, `MyDispenserBehaviorRock` S, `MyDispenserBehaviorSunspotUrchin` S, `MyDispenserBehaviorWDCharge` S
- **KI:** `MyEntityAITarget` M, `MyEntityAINearestAttackableTarget` M, `MyEntityAINearestAttackableTargetSorter` S, `MyEntityAIFollowOwner` M, `MyEntityAIJealousy` S, `MyEntityAIDance` M, `MyValentineTarget` S, `MyValentineTargetSorter` S
- **Modelle und Renderer (nur `client`):** `ModelElevator`, `RenderItemUrchin`, `RenderShoe`, `RenderThrownRock`, `RenderBertha`, `RenderSlice`, `RenderRoyal`, `RenderHammy`, `ModelBertha`, `ModelSlice`, `ModelHammy`, `RenderGirlfriend`, `RenderElevator`, `RenderBoyfriend`, `RenderRockBase`, `ModelRockBase`

### 4.6 W05 – Dimensionen, Terrain, Portaltiere

Ziel: sechs Dimensionen nach R13 mit Java-ChunkGenerator je `ChunkProviderOreSpawnN`, Erze im Generator, Crystal-Labyrinth, verdichtete Dörfer, Teleporter, und die Tiere, die hineinführen, samt Nestern und Spawn-Pflanzen.

Hinweise: Biome der Dimensionen hier als Datapack-JSON; ihre Spawnlisten kommen erst mit BiomeGenUtopianPlains (W12); die Dino-Liste von ChunkProviderOreSpawn2 ist Rückwärtskante; Crystal braucht `min_y` 0, damit das Labyrinth auf Y 24 bis 28 bleibt (verhalten/world-04.md); der Chunk-Clip der Adern bleibt.

Schnitt: Utopia und Village, Mining, Islands, Crystal mit Labyrinth, Chaos, Teleporter mit Ameisen, Schmetterling und Motte.

GameTests: jede Dimension lädt und erzeugt Chunks; jede Portal-Ameise, die Termite und der Schmetterling bringen einen Spieler hin und zurück; das Labyrinth liegt auf Y 24 bis 28.

Umfang: 35 Klassen mit Verhaltenskapitel (S 24 / M 7 / L 4 / XL 0), 8 Modell- und Renderer-Klassen, 7074 Zeilen Originalquelltext (classes.json).

- **Welt:** `WorldProviderOreSpawn` S, `WorldProviderOreSpawn2` S, `WorldProviderOreSpawn3` S, `WorldProviderOreSpawn4` S, `WorldProviderOreSpawn5` S, `WorldProviderOreSpawn6` S, `ChunkProviderOreSpawn` L, `ChunkProviderOreSpawn2` L, `ChunkProviderOreSpawn3` L, `ChunkProviderOreSpawn4` S, `ChunkProviderOreSpawn5` L, `ChunkProviderOreSpawn6` M, `ChunkOreGenerator` M, `CrystalMaze` S, `MapGenMoreVillages` S, `OreSpawnTeleporter` M
- **Entities:** `EntityAnt` M, `EntityRedAnt` S, `EntityRainbowAnt` S, `EntityUnstableAnt` S, `Termite` M, `EntityButterfly` M, `EntityLunaMoth` S, `EntityMosquito` S, `Firefly` S
- **Blöcke:** `AntBlock` M, `CrystalAntBlock` S, `BlockButterflyPlant` S, `BlockMothPlant` S, `BlockMosquitoPlant` S, `BlockFireflyPlant` S
- **Items:** `ItemButterflySeed` S, `ItemMothSeed` S, `ItemMosquitoSeed` S, `ItemFireflySeed` S
- **Modelle und Renderer (nur `client`):** `RenderButterfly`, `ModelButterfly`, `RenderMosquito`, `ModelMosquito`, `RenderFirefly`, `ModelFirefly`, `RenderAnt`, `ModelAnt`

### 4.7 W06 – Friedliche Tiere und Battle Mobs

Ziel: Vögel, Fische, Kühe, Saurier-Pflanzenfresser, Geister und die EntityCannonFodder-Familie.

Hinweise: EntityCannonFodder vor Chipmunk, Lizard, Ostrich, VelocityRaptor; RedCow vor GoldCow, EnchantedCow, CrystalCow; Ostrich nutzt den Tastenzustand aus W01; der Halloween-Spawn der Geister kommt mit dem BiomeModifier in W12; Drops als Java nach R10.

GameTests: jede Entity spawnt und tickt 100 Ticks (R17); Zähmen und Züchten mit Crystal Apple; Battle-Mob-Hüte teilen Teams; Drops gegen Kapitel.

Umfang: 31 Klassen mit Verhaltenskapitel (S 19 / M 9 / L 3 / XL 0), 49 Modell- und Renderer-Klassen, 13704 Zeilen Originalquelltext (classes.json).

- **Entities:** `Cockateil` S, `RubyBird` S, `CliffRacer` S, `GoldFish` M, `Cricket` S, `Dragonfly` S, `Frog` M, `Coin` S, `Tshirt` S, `RedCow` S, `GoldCow` S, `EnchantedCow` S, `CrystalCow` S, `Baryonyx` S, `Cassowary` S, `Camarasaurus` S, `Beaver` M, `EntityCannonFodder` M, `Chipmunk` M, `Lizard` M, `Ostrich` L, `VelocityRaptor` L, `Gazelle` M, `Peacock` S, `Whale` M, `Flounder` M, `StinkBug` S, `Hydrolisc` L, `Ghost` S, `GhostSkelly` S
- **KI:** `MyEntityAIAvoidEntity` S
- **Modelle und Renderer (nur `client`):** `RenderEnchantedCow`, `RenderCamarasaurus`, `ModelCamarasaurus`, `RenderHydrolisc`, `ModelHydrolisc`, `RenderVelocityRaptor`, `ModelVelocityRaptor`, `RenderDragonfly`, `ModelDragonfly`, `RenderBaryonyx`, `ModelBaryonyx`, `RenderCockateil`, `ModelCockateil`, `RenderLizard`, `ModelLizard`, `RenderChipmunk`, `ModelChipmunk`, `RenderGazelle`, `ModelGazelle`, `RenderOstrich`, `ModelOstrich`, `RenderStinkBug`, `ModelStinkBug`, `RenderTshirt`, `ModelTshirt`, `RenderCliffRacer`, `ModelCliffRacer`, `RenderGhost`, `ModelGhost`, `RenderGhostSkelly`, `ModelGhostSkelly`, `RenderCassowary`, `ModelCassowary`, `RenderGoldFish`, `ModelGoldFish`, `RenderBeaver`, `ModelBeaver`, `RenderPeacock`, `ModelPeacock`, `RenderFlounder`, `ModelFlounder`, `RenderWhale`, `ModelWhale`, `RenderCoin`, `ModelCoin`, `RenderCricket`, `ModelCricket`, `RenderFrog`, `ModelFrog`

### 4.8 W07 – Feindliche Landmobs, Roboter, Würmer

Ziel: Saurier, Skorpione, Insekten, Ender-Mobs, Fairy, die Robo-Reihe mit Jeffery und die drei Würmer.

Hinweise: Mob-Werte zur Laufzeit aus MobStats; Spawner-Erkennung über `MobSpawnType.SPAWNER`; Drops mit Verzauberungen über R7; Würmer graben mit `noPhysics`; der Fairy-Spawner im Feenbaum kommt mit Trees (W12).

GameTests: spawnen und 100 Ticks; Angriffe und Rückstoß auf ein Ziel; Robo-Gunner und Robo-Sniper feuern LaserBalls; WormLarge erzeugt beim ersten Tick seine Brut.

Umfang: 32 Klassen mit Verhaltenskapitel (S 7 / M 21 / L 4 / XL 0), 63 Modell- und Renderer-Klassen, 24035 Zeilen Originalquelltext (classes.json).

- **Entities:** `Alien` M, `Alosaurus` S, `Cryolophosaurus` S, `TRex` M, `Nastysaurus` S, `Pointysaurus` M, `Basilisk` M, `Scorpion` S, `EmperorScorpion` L, `CaveFisher` S, `Bee` M, `HerculesBeetle` M, `Molenoid` M, `Kyuubi` M, `BandP` M, `EnderKnight` M, `EnderReaper` M, `LeafMonster` M, `Fairy` M, `SpitBug` M, `TrooperBug` L, `Hammerhead` M, `Robot1` S, `Robot2` M, `Robot3` M, `Robot4` M, `Robot5` M, `GiantRobot` L, `WormSmall` M, `WormMedium` M, `WormLarge` L
- **Items:** `FairySword` S
- **Modelle und Renderer (nur `client`):** `RenderGiantRobotInfo`, `RenderBee`, `ModelBee`, `RenderRobot1`, `ModelRobot1`, `RenderRobot2`, `ModelRobot2`, `RenderRobot3`, `ModelRobot3`, `RenderRobot4`, `ModelRobot4`, `RenderRobot5`, `ModelRobot5`, `RenderAlosaurus`, `ModelAlosaurus`, `RenderCryolophosaurus`, `ModelCryolophosaurus`, `RenderBasilisk`, `ModelBasilisk`, `RenderEmperorScorpion`, `ModelEmperorScorpion`, `RenderScorpion`, `ModelScorpion`, `RenderCaveFisher`, `ModelCaveFisher`, `RenderKyuubi`, `ModelKyuubi`, `RenderAlien`, `ModelAlien`, `RenderTrooperBug`, `ModelTrooperBug`, `RenderSpitBug`, `ModelSpitBug`, `RenderWormSmall`, `ModelWormSmall`, `RenderWormMedium`, `ModelWormMedium`, `RenderWormLarge`, `ModelWormLarge`, `RenderLeafMonster`, `ModelLeafMonster`, `RenderEnderKnight`, `ModelEnderKnight`, `RenderEnderReaper`, `ModelEnderReaper`, `RenderFairy`, `ModelFairy`, `RenderHerculesBeetle`, `ModelHerculesBeetle`, `RenderTRex`, `ModelTRex`, `RenderMolenoid`, `ModelMolenoid`, `RenderHammerhead`, `ModelHammerhead`, `RenderBandP`, `ModelBandP`, `RenderNastysaurus`, `ModelNastysaurus`, `RenderPointysaurus`, `ModelPointysaurus`, `RenderGiantRobot`, `ModelGiantRobot`

### 4.9 W08 – Wasser-, Kristall- und Flugmobs, Nightmare, Mothra, Inseln

Ziel: Wasser- und Crystal-Mobs, Terrors, Nightmare in fünf Größen, Mothra, CaterKiller mit Brutalfly, die schwebenden Inseln mit Triffid.

Hinweise: Island und IslandToo bleiben tickende Entities mit Laufzeit-Blockschreibern, keine Structure; PitchBlack über `getDefaultDimensions` und `refreshDimensions`; Mothra erbt EntityButterfly aus W05; AttackSquid ruft Kraken (Rückwärtskante W10); `heal(-x)` beim Austrocknen über `setHealth` (6.3).

GameTests: spawnen und 100 Ticks; Wassermobs trocknen an Land aus; Island baut eine Insel und verschiebt sie; CaterKiller wird verletzt zur Brutalfly.

Umfang: 26 Klassen mit Verhaltenskapitel (S 6 / M 14 / L 6 / XL 0), 45 Modell- und Renderer-Klassen, 18761 Zeilen Originalquelltext (classes.json).

- **Entities:** `CloudShark` M, `TerribleTerror` M, `LurkingTerror` S, `CreepingHorror` S, `Mantis` M, `Rat` M, `Crab` M, `SeaMonster` M, `SeaViper` M, `Skate` S, `Irukandji` M, `Urchin` M, `Rotator` M, `Vortex` M, `DungeonBeast` M, `AttackSquid` M, `PitchBlack` L, `Triffid` L, `Island` L, `IslandToo` L, `Mothra` L, `Brutalfly` M, `CaterKiller` L
- **Items:** `RatSword` S, `ItemSquidZooka` S
- **Blöcke:** `IslandBlock` S
- **Modelle und Renderer (nur `client`):** `RenderSquidZooka`, `ModelSquidZooka`, `RenderAttackSquid`, `ModelAttackSquid`, `RenderIsland`, `ModelIsland`, `RenderIslandToo`, `RenderCreepingHorror`, `ModelCreepingHorror`, `RenderTerribleTerror`, `ModelTerribleTerror`, `RenderTriffid`, `ModelTriffid`, `RenderPitchBlack`, `ModelPitchBlack`, `RenderLurkingTerror`, `ModelLurkingTerror`, `RenderCloudShark`, `ModelCloudShark`, `RenderRotator`, `ModelRotator`, `RenderVortex`, `ModelVortex`, `RenderDungeonBeast`, `ModelDungeonBeast`, `RenderRat`, `ModelRat`, `RenderIrukandji`, `ModelIrukandji`, `RenderSkate`, `ModelSkate`, `RenderUrchin`, `ModelUrchin`, `RenderMantis`, `ModelMantis`, `RenderSeaMonster`, `ModelSeaMonster`, `RenderSeaViper`, `ModelSeaViper`, `RenderCaterKiller`, `ModelCaterKiller`, `RenderBrutalfly`, `ModelBrutalfly`, `RenderCrab`, `ModelCrab`

### 4.10 W09 – Reittiere, zähmbare Drachen, Roboter-Fahrzeuge

Ziel: Dragon, Spyro, Cephadrome, Leon, WaterDragon, Stinky, GammaMetroid, RubberDucky und die beiden Reit-Roboter mit Kit und Wrench.

Hinweise: Reiten über `tickRidden` und `getRiddenInput` mit dem Tastenzustand pro Spieler; die Flugphase ruft im Original `super.onLivingUpdate` nicht auf; SpiderRobot hat 1500 Leben (R4) und Client-Klassen im gemeinsamen Code, die ersetzt werden müssen; Spyro und Dragon wandeln sich ineinander mit Besitzerübergabe.

GameTests: zähmen, aufsteigen, steigen per Payload; Kit spawnt Roboter mit gespeichertem Leben, Wrench packt ihn wieder ein.

Umfang: 13 Klassen mit Verhaltenskapitel (S 1 / M 3 / L 4 / XL 5), 22 Modell- und Renderer-Klassen, 14553 Zeilen Originalquelltext (classes.json).

- **Entities:** `Dragon` XL, `Spyro` L, `Cephadrome` XL, `Leon` XL, `WaterDragon` L, `Stinky` L, `GammaMetroid` M, `RubberDucky` L, `AntRobot` XL, `SpiderRobot` XL, `SpiderDriver` M
- **Items:** `ItemSpiderRobotKit` M, `ItemWrench` S
- **Modelle und Renderer (nur `client`):** `RenderSpiderRobotInfo`, `RenderSpyro`, `ModelSpyro`, `RenderGammaMetroid`, `ModelGammaMetroid`, `RenderWaterDragon`, `ModelWaterDragon`, `RenderCephadrome`, `ModelCephadrome`, `RenderDragon`, `ModelDragon`, `RenderStinky`, `ModelStinky`, `RenderLeon`, `ModelLeon`, `RenderRubberDucky`, `ModelRubberDucky`, `RenderSpiderRobot`, `ModelSpiderRobot`, `RenderSpiderDriver`, `RenderAntRobot`, `ModelAntRobot`

### 4.11 W10 – Bosse und Royals

Ziel: Kraken, Mobzilla, The King, The Queen, die Prince-Linie, Princess, PurplePower, die Köpfe und die Spawner- und Abwehrblöcke.

Hinweise: virtuelle Lebenspunkte und Rüstungsformel aus W01 im Ernstfall; Köpfe als eigene Entities (R9); ThePrinceAdult wird unter FullPowerKingEnable zu TheKing; der Ultimate-Zustand des Kings spawnt explodierende PurplePower (Entity-Spam, verhalten/entity-01.md); Repellents als Stand- und Wandfackel.

GameTests: jeder Boss spawnt, tickt 100 Ticks, nimmt skalierten Schaden, Kopf leitet Treffer weiter; KingSpawnerBlock beschwört den King; Kraken-Repellent stößt Kraken ab.

Umfang: 16 Klassen mit Verhaltenskapitel (S 3 / M 7 / L 3 / XL 3), 21 Modell- und Renderer-Klassen, 21270 Zeilen Originalquelltext (classes.json).

- **Entities:** `Kraken` L, `Godzilla` XL, `GodzillaHead` M, `TheKing` XL, `KingHead` M, `TheQueen` XL, `QueenHead` S, `PurplePower` M, `ThePrince` M, `ThePrinceTeen` L, `ThePrinceAdult` L, `ThePrincess` M
- **Blöcke:** `KrakenRepellent` M, `CreeperRepellent` M, `KingSpawnerBlock` S, `QueenSpawnerBlock` S
- **Modelle und Renderer (nur `client`):** `RenderPurplePower`, `ModelPurplePower`, `RenderKraken`, `ModelKraken`, `RenderGodzilla`, `ModelGodzilla`, `RenderGodzillaHead`, `RenderTheKing`, `ModelTheKing`, `RenderKingHead`, `RenderTheQueen`, `ModelTheQueen`, `RenderQueenHead`, `RenderThePrince`, `ModelThePrince`, `RenderThePrinceTeen`, `ModelThePrinceTeen`, `RenderThePrincess`, `ModelThePrincess`, `RenderThePrinceAdult`, `ModelThePrinceAdult`

### 4.12 W11 – Sammler und Übersichten

Ziel: die Klassen, die fast alle Entities kennen: Critter Cage (114 Items), Wurfkäfig mit Fangtabelle, Easter Bunny (legt Eier aus 109 Typen, verhalten/entity-07.md) und das Lebensbalken-Overlay über 45 Klassen.

Hinweise: EntityCage prüft `instanceof` in Originalreihenfolge und braucht deshalb alle Klassen auf einmal; CritterCage gibt je `cage_id` das Entity frei; das Overlay wird GUI-Layer (R15) und liest bei virtuellem Leben das synchronisierte Verhältnis.

GameTests: jede Entity mit Käfigregel lässt sich fangen und freilassen; ein Boss zeigt den Balken mit Originalverhältnis.

Umfang: 4 Klassen mit Verhaltenskapitel (S 0 / M 1 / L 3 / XL 0), 3 Modell- und Renderer-Klassen, 2635 Zeilen Originalquelltext (classes.json).

- **Items:** `CritterCage` L
- **Entities:** `EntityCage` L, `EasterBunny` M
- **GUI/Block-Entity:** `GirlfriendOverlayGui` L
- **Modelle und Renderer (nur `client`):** `RenderCage`, `RenderEasterBunny`, `ModelEasterBunny`

### 4.13 W12 – Bäume, Strukturen, Oberwelt-Generierung, natürliches Spawnen, Rezepte

Ziel: Trees, GenericDungeon, BasiliskMaze, RubyBirdDungeon, DungeonSpawnerBlock, ItemRandomDungeon, ItemMagicApple, OreSpawnWorld und die Spawnlisten aus BiomeGenUtopianPlains; dazu die Aufgaben ohne eigene Klasse aus verhalten/core-01b.md: 381 Rezepte (R14), 9 Truhen-Loot-Einträge als Global Loot Modifier (R10), der BiomeModifier `orespawn:config_spawns` für 348 Spawn-Einträge (R11) und die Prüfung, dass alle 134 Dispenser-Verhalten registriert sind (Eier je Welle ab W01, Wurfgeschosse in W04).

Hinweise: je Bau Feature oder Structure nach R12 anhand der Maße in world-01, world-02 und world-04; alle offenen Rückwärtskanten nach W12 schließen; die Datumsspawns (Halloween, Ostern) nach der Entscheidung in 6.7.

GameTests: jede Struktur wird in einer Testwelt vollständig platziert und schreibt nur in ihre BoundingBox; BiomeModifier fügt Spawns nur bei gesetztem Enable-Flag ein; Stichproben der Rezepte gegen recipes.json.

Umfang: 9 Klassen mit Verhaltenskapitel (S 2 / M 1 / L 4 / XL 2), 0 Modell- und Renderer-Klassen, 12957 Zeilen Originalquelltext (classes.json).

- **Welt:** `Trees` L, `GenericDungeon` XL, `RubyBirdDungeon` S, `BasiliskMaze` L, `OreSpawnWorld` XL, `BiomeGenUtopianPlains` M
- **Blöcke:** `DungeonSpawnerBlock` L
- **Items:** `ItemRandomDungeon` S, `ItemMagicApple` L

### 4.14 Rückwärtskanten

Referenz einer früheren Welle auf eine Klasse einer späteren; ohne MyUtils und ItemSpawnEgg (siehe 4.1).

| Welle | Klasse | verweist auf |
|---|---|---|
| W02 | `OreSalt` | `EntityAnt` (W05) |
| W02 | `OreBasicStone` | `EntityRedAnt` (W05), `Fairy` (W07), `Rat` (W08), `Termite` (W05) |
| W02 | `BlockDuplicatorLog` | `Trees` (W12) |
| W02 | `BlockExtremeTorch` | `Cephadrome` (W09) |
| W02 | `BlockAppleLeaves` | `ItemMagicApple` (W12) |
| W02 | `BlockExperiencePlant` | `Trees` (W12) |
| W03 | `UltimateAxe` | `Boyfriend` (W04), `Girlfriend` (W04) |
| W03 | `UltimatePickaxe` | `Boyfriend` (W04), `Girlfriend` (W04) |
| W03 | `UltimateShovel` | `Boyfriend` (W04), `Girlfriend` (W04) |
| W03 | `UltimateSword` | `Boyfriend` (W04), `Girlfriend` (W04) |
| W04 | `LaserBall` | `Dragon` (W09), `GiantRobot` (W07), `Robot2` (W07), `Robot3` (W07), `Robot4` (W07), `Robot5` (W07), `SpitBug` (W07), `TrooperBug` (W07) |
| W04 | `BetterFireball` | `Dragon` (W09), `Godzilla` (W10), `GodzillaHead` (W10), `Kraken` (W10), `Mothra` (W08), `PitchBlack` (W08) |
| W04 | `WaterBall` | `AttackSquid` (W08), `Dragon` (W09), `WaterDragon` (W09) |
| W04 | `InkSack` | `AttackSquid` (W08), `WaterDragon` (W09) |
| W04 | `UltimateArrow` | `Cephadrome` (W09), `Dragon` (W09) |
| W04 | `IrukandjiArrow` | `Cephadrome` (W09), `Dragon` (W09) |
| W04 | `MyEntityAITarget` | `Mothra` (W08) |
| W05 | `WorldProviderOreSpawn` | `BiomeGenUtopianPlains` (W12) |
| W05 | `WorldProviderOreSpawn3` | `BiomeGenUtopianPlains` (W12) |
| W05 | `WorldProviderOreSpawn4` | `BiomeGenUtopianPlains` (W12) |
| W05 | `WorldProviderOreSpawn5` | `BiomeGenUtopianPlains` (W12) |
| W05 | `WorldProviderOreSpawn6` | `BiomeGenUtopianPlains` (W12) |
| W05 | `ChunkProviderOreSpawn2` | `Alien` (W07), `Alosaurus` (W07), `Baryonyx` (W06), `Camarasaurus` (W06), `CaveFisher` (W07), `Cryolophosaurus` (W07), `Dragonfly` (W06), `GammaMetroid` (W09), `Nastysaurus` (W07), `Pointysaurus` (W07), `Spyro` (W09), `TRex` (W07), `VelocityRaptor` (W06) |
| W05 | `EntityButterfly` | `Mothra` (W08) |
| W06 | `Frog` | `WormSmall` (W07) |
| W06 | `Lizard` | `AttackSquid` (W08) |
| W07 | `Cryolophosaurus` | `GammaMetroid` (W09) |
| W07 | `Hammerhead` | `CreeperRepellent` (W10) |
| W07 | `GiantRobot` | `ItemSpiderRobotKit` (W09) |
| W08 | `TerribleTerror` | `Dragon` (W09) |
| W08 | `LurkingTerror` | `Dragon` (W09) |
| W08 | `CreepingHorror` | `Dragon` (W09) |
| W08 | `Mantis` | `WaterDragon` (W09) |
| W08 | `Crab` | `RubberDucky` (W09) |
| W08 | `AttackSquid` | `Kraken` (W10), `WaterDragon` (W09) |
| W08 | `PitchBlack` | `Godzilla` (W10), `GodzillaHead` (W10) |
| W08 | `Triffid` | `Dragon` (W09) |
| W08 | `IslandToo` | `Trees` (W12) |
| W09 | `Dragon` | `Kraken` (W10) |
| W09 | `Cephadrome` | `Kraken` (W10) |
| W09 | `Leon` | `Kraken` (W10), `KrakenRepellent` (W10) |

## 5. Querschnittsrisiken

Klassenlisten mit „grep" sind Treffer im Originalquelltext; sie zeigen, wo das Muster vorkommt, nicht dass jede Stelle gleich schwer ist.

### 5.1 Lebenspunkte über 1024 (R4)

- **Über dem Deckel** (manifest): Godzilla 4000, GodzillaHead 4000, TheKing 7000, KingHead 7000, TheQueen 6000, QueenHead 6000, ThePrinceAdult 3000, ThePrinceTeen 1500, SpiderRobot 1500.
- **Per Config verdoppelbar über den Deckel:** Kraken und GiantRobot (Jeffery), weil MobStats das Leben auf `[h/2, h*2]` klemmt (verhalten/core-01a.md, core-02.md).
- **Lesen oder schreiben Leben direkt:** KingHead, QueenHead, GodzillaHead (Spiegel), ItemSpiderRobotKit und ItemWrench (Rest-Leben als Item-Schaden), GirlfriendOverlayGui (Balken), BetterFireball (`setHealth(health/2)` bei großen Zielen), PurplePower (`setHealth(hp/4-1)`), TheQueen (Heilsperre per `setHealth`), PitchBlack (Leben skaliert mit der Größe; offen, ob über 1024).
- **Umgang:** eine Hilfsklasse nach R4; alle Spiegel, Kits und Balken rechnen im Originalmaßstab.

### 5.2 Rüstung, Schadensdeckel, eigene Unverwundbarkeit (R5)

- **Entities mit Rüstung ab 20** (manifest): PurplePower 25, Godzilla 25/21, TheKing 21 bis 25, TheQueen 21 bis 26, Girlfriend 23, Boyfriend 23, EmperorScorpion 20, Hammerhead 20, ThePrinceAdult 20, SpiderDriver 8/20.
- **Rüstungssets über ARMOR 30:** Ultimate 34, Mobzilla 38, Royal 42, Queen 48 (ItemOreSpawnArmor, ArmorStats).
- **Eigene Unverwundbarkeitszähler neben `invulnerableTime`** (grep `hurt_timer`, 20 Klassen): `Alien`, `Basilisk`, `Cephadrome`, `Crab`, `Dragon`, `EmperorScorpion`, `Godzilla`, `HerculesBeetle`, `Kraken`, `Leon`, `SeaMonster`, `SeaViper`, `SpitBug`, `TheKing`, `ThePrinceAdult`, `ThePrinceTeen`, `TheQueen`, `Triffid`, `TrooperBug`, `WaterDragon`.
- **Schadensdeckel je Treffer:** Godzilla 750, PurplePower 10, Hydrolisc 10, Boyfriend 10 (verhalten/entity-03.md, 01, 09, 05).
- **Schaden über dem Attribut:** Nahkampf des Kings bis 56000, der Queen bis 225000 als Feld führen (verhalten/entity-01.md).
- **Zweiter Treffer im selben Tick** fällt vermutlich ins Unverwundbarkeitsfenster: ThunderBolt, ExperienceSword, TheKing (Stampfer); jeweils offen (6.4).

### 5.3 Flug- und Reitphysik, globaler Tastenzustand (R15)

- **Lesen `flyup_keystate`** (grep, 8 Klassen): `Cephadrome`, `Dragon`, `Elevator`, `Leon`, `Ostrich`, `RiderControlMessageHandler`, `ThePrinceAdult`, `ThePrinceTeen`. Im Original ein Wert für alle Spieler.
- **Client-Klassen im gemeinsamen Code** (grep `EntityClientPlayerMP`): `Elevator`, `SpiderRobot`. Klassenladefehler auf dem dedizierten Server.
- **Bewegung ohne Navigation** (Motion direkt, laut Kapiteln): AntRobot, SpiderRobot (doppeltes `moveEntity`), Elevator, Dragon, Cephadrome, Leon, ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, Spyro, Stinky, Kraken, TheKing, TheQueen, Mothra, Brutalfly, Bee, CliffRacer, Cockateil, GoldFish, Dragonfly, EntityButterfly, EntityMosquito, Firefly, Fairy, LurkingTerror, TerribleTerror, Rotator, Mantis.
- **Serverimpuls an Spieler braucht `hurtMarked`:** Kraken (Tragen), TheKing (Einfrieren, Drehen), Vortex (Sog), Hammerhead, BigHammer, ItemCreeperLauncher, ItemRayGun, Lavafoam, CreeperRepellent, KrakenRepellent.
- **Umgang:** Payload und Attachment pro Spieler (Abweichung, 6.7); Physik in `customServerAiStep` oder `tickRidden` in Originalreihenfolge.

### 5.4 Hitbox und Größe zur Laufzeit (R9)

- **PlayNicely** wird in 90 Klassen gelesen (grep): `Alien`, `Alosaurus`, `AntRobot`, `AttackSquid`, `BandP`, `Baryonyx`, `Basilisk`, `Beaver`, `Bee`, `Boyfriend`, `Brutalfly`, `Camarasaurus`, `CaterKiller`, `CaveFisher`, `Cephadrome`, `CloudShark`, `Crab`, `CreepingHorror`, `Cryolophosaurus`, `Dragon`, `Dragonfly`, `DungeonBeast`, `EmperorScorpion`, `EnderKnight`, `EnderReaper`, `EntityRedAnt`, `Fairy`, `Frog`, `GammaMetroid`, `Gazelle`, `GiantRobot`, `Girlfriend`, `Godzilla`, `Hammerhead`, `HerculesBeetle`, `Irukandji`, `Kraken`, `Kyuubi`, `LeafMonster`, `Leon`, `Lizard`, `LurkingTerror`, `Mantis`, `Molenoid`, `Mothra`, `Nastysaurus`, `Peacock`, `PitchBlack`, `Pointysaurus`, `PurplePower`, `Rat`, `RenderCaterKiller`, `RenderGodzilla`, `RenderKraken`, `RenderTheKing`, `RenderTheQueen`, `Robot1`, `Robot2`, `Robot3`, `Robot4`, `Robot5`, `Rotator`, `RubberDucky`, `Scorpion`, `SeaMonster`, `SeaViper`, `Skate`, `SpiderDriver`, `SpiderRobot`, `SpitBug`, `Spyro`, `Stinky`, `TRex`, `Termite`, `TerribleTerror`, `TheKing`, `ThePrince`, `ThePrinceAdult`, `ThePrinceTeen`, `ThePrincess`, `TheQueen`, `Triffid`, `TrooperBug`, `Urchin`, `VelocityRaptor`, `Vortex`, `WaterDragon`, `WormLarge`, `WormMedium`, `WormSmall`.
- **Hitbox wechselt mit PlayNicely:** TheKing und TheQueen (22x24 oder 5,5x6), Godzilla (9,9x25 oder 2,475x6,25), Kraken (4x15 oder 1,333x5), CaterKiller (2,9x4,6 oder 1,45x2,3) (design/design-entities-01.md, 02).
- **Größenstufen:** Crab (Skala 0,25, 0,5, 1,0, aus Spawnern 0,35), PitchBlack (fünf Größen), Girlfriend (Valentinstag, fünffach) (design/design-entities-02.md, 03, 05).
- **Umgang:** `getDefaultDimensions` aus synchronisierten Daten plus `refreshDimensions`; Pfadsuche für breite Mobs prüfen.

### 5.5 Chunkgrenzen und Direktschreiber (R12)

- **Direkt in Chunk-Sections schreiben** (grep `setBlockFast`, `setBlockSuperFast`, `setBlockIDWithMetadataInChunk`, 18 Klassen): `BasiliskMaze`, `BlockAppleLeaves`, `BlockCrystalPlant`, `BlockScaryLeaves`, `ChunkOreGenerator`, `ChunkProviderOreSpawn4`, `ChunkProviderOreSpawn5`, `ChunkProviderOreSpawn6`, `CrystalMaze`, `GenericDungeon`, `Island`, `IslandToo`, `ItemAppleSeed`, `ItemMagicApple`, `NightmareDungeon`, `OreSpawnWorld`, `RubyBirdDungeon`, `Trees`.
- **Bauten größer als 3x3 Chunks:** GenericDungeon mit EnormousCastle (93x82x84), King- und QueenAltar (61x68x61), IncaPyramid, RobotLab, AlienWTF, Kyuubi, EnderCastle, NightmareRookery (verhalten/world-01.md); Trees mit Sky Tree (±34), Wind Tree (+37), Fairy Castle Tree (85x85) (world-04); BasiliskMaze bis 73x42x34 (world-04); ItemMagicApple mit Riesenbäumen (itemblock-01); Prüfflächen in OreSpawnWorld bis 50x320 (WhiteHouse, world-02).
- **Laufzeit-Bauer außerhalb der Weltgenerierung:** DungeonSpawnerBlock (50 Strukturen), Island, IslandToo, InstantShelter, InstantGarden (18x15x10), ItemMinersDream (64x11x5), ZooCage (bis 19x19), StepUp, StepDown, StepAccross (bis 32), UltimateHoe (3x3x3), UltimateSword als Kettensäge (11x16x11), Godzilla (bis 2178 Blockzugriffe je Tick), Beaver (Rekursion bis Tiefe 200), BlockSkyTreeLog (Rekursion bis 1000), Robot2 (Radius 6,5).
- **Chunk-Clip als Spielverhalten:** ChunkOreGenerator, ChunkProviderOreSpawn4 (Bäume), ChunkProviderOreSpawn5 (Erze und Kristalle), CrystalMaze.

### 5.6 Zufall, globaler Zustand und Singleton-Felder

- **`recently_placed`**, statisch, dimensionsübergreifend, nicht gespeichert: OreSpawnWorld und die von ihr gerufenen GenericDungeon, Trees, BasiliskMaze, RubyBirdDungeon (verhalten/world-02.md).
- **`Math.random`** (grep): `BasiliskMaze`, `CrystalMaze`; `world.rand` statt Seed in ChunkProviderOreSpawn4, ChunkProviderOreSpawn5, ChunkProviderOreSpawn6 und dem Chaos-Lauf von ChunkOreGenerator.
- **Zustand in Item- und Block-Singletons:** ItemThunderStaff (`ticker`), UltimateSword (`swingtimer`, `leaf`), OreTitanium und OreUranium (Glühen), BlockCorn, BlockQuinoa, BlockTomato (`myMaxHeight`), ItemMagicApple (`no_critters`).
- **Geteilte Statics:** `OreSpawnRand` (`Random(151L)`) in 74 Klassen (verhalten/core-01a.md); `current_dimension` und `FastGraphicsLeaves` clientseitig geschrieben, gemeinsam gelesen (grep): `BlockAppleLeaves`, `BlockCrystalLeaves`, `BlockExperienceLeaves`, `BlockScaryLeaves`, `CrystalGrass`, `GirlfriendOverlayGui`, `OreBasicStone`.

### 5.7 Config zur Registrierzeit und relative Klemmungen (R3)

- **Beim Registrieren verbraucht:** ArmorStats, WeaponStats, ItemOreSpawnArmor, alle Werkzeuge und Schwerter über ihre Tiers, Bertha (`setMaxDamage(9000)` überschreibt Royal und Hammy), ItemSpiderRobotKit (Haltbarkeit aus `SpiderRobot_health` und `AntRobot_health`).
- **Stat-Halter aus der Config** (`OreSpawnMain.*_stats`, also MobStats, OreStats, WeaponStats) werden in 66 Klassen gelesen (grep): `Alien`, `Alosaurus`, `AntRobot`, `AttackSquid`, `BandP`, `Basilisk`, `Bee`, `BerthaHit`, `Brutalfly`, `CaterKiller`, `CaveFisher`, `ChunkOreGenerator`, `CloudShark`, `Crab`, `CreepingHorror`, `Cryolophosaurus`, `DungeonBeast`, `EmperorScorpion`, `EnderKnight`, `EnderReaper`, `GammaMetroid`, `GiantRobot`, `Godzilla`, `GodzillaHead`, `Hammerhead`, `HerculesBeetle`, `Irukandji`, `ItemSpiderRobotKit`, `KingHead`, `Kraken`, `Kyuubi`, `LeafMonster`, `LurkingTerror`, `Mantis`, `Molenoid`, `Mothra`, `Nastysaurus`, `OreSpawnWorld`, `PitchBlack`, `Pointysaurus`, `QueenHead`, `Rat`, `Robot2`, `Robot3`, `Robot4`, `Robot5`, `Rotator`, `Scorpion`, `SeaMonster`, `SeaViper`, `Skate`, `SpiderRobot`, `SpitBug`, `TRex`, `TerribleTerror`, `TheKing`, `TheQueen`, `Triffid`, `TrooperBug`, `UltimateSword`, `Urchin`, `Vortex`, `WaterDragon`, `WormLarge`, `WormMedium`, `WormSmall`. MobStats klemmt `[h/2, h*2]`, `[a/2, a*2]`, `[d-4, d+4]` bis 22 (verhalten/core-02.md).
- **Code weicht von der Config ab** (R3: der Code gilt): TheKing Nahkampfbasis 250 statt 350, TheQueen 250 statt 225 (entity-01); Leon liest Leonopteryx-Werte nie und hat fest 250/55/16 (entity-10); LurkingTerror und TerribleTerror machen fest 5 Schaden (entity-10, 13).
- **Sonderregeln:** OreStats setzt bei `maxdepth - mindepth < 10` beide Defaults zurück; WeaponStats klemmt das Harvest-Level unter `d-1` auf `d`; LessLag koppelt weitere Werte, darunter Astlängen der Bäume und `IslandSizeFactor` (core-01a, world-04, itemblock-03).
- **Spawn-Guards:** 304 von 348 Einträgen hängen an Enable-Flags (manifest); `AllMobsDisable` schaltet sie über `disableAllMobs()` ab (OreSpawnMain.java:1258-1260, 5804).

### 5.8 Datengetriebene Verzauberungen (R7)

- **Verzaubern zur Laufzeit** (grep, 29 Klassen): `AttackSquid`, `Basilisk`, `Bertha`, `CaterKiller`, `Cephadrome`, `EmeraldPickaxe`, `EmperorScorpion`, `ExperienceSword`, `Godzilla`, `HerculesBeetle`, `ItemMagicApple`, `ItemNetherLost`, `ItemOreSpawnArmor`, `ItemRandomDungeon`, `Kraken`, `NightmareSword`, `PoisonSword`, `SeaMonster`, `SeaViper`, `Slice`, `TrooperBug`, `UltimateAxe`, `UltimateBow`, `UltimateFishingRod`, `UltimateHoe`, `UltimatePickaxe`, `UltimateShovel`, `UltimateSword`, `WaterDragon`.
- **Stufen über dem Maximum:** ItemOreSpawnArmor (Protection 10), CaterKiller (Feather Falling 9), SeaMonster (Feather Falling 5 bis 9) (itemblock-01, entity-05, entity-12).
- **Doppelter Sharpness-Eintrag:** Godzilla, CaterKiller, TrooperBug, HerculesBeetle, WaterDragon; 1.21.1 nimmt beim Hochstufen einen Wert statt beide (entity-03, 05, 09, 13, 14).
- **Jeden Tick nachgetragen:** Bertha, EmeraldPickaxe, ExperienceSword, NightmareSword, ItemNetherLost, die Ultimate-Werkzeuge, ItemOreSpawnArmor.

### 5.9 Spawnregeln, Kategorien, Datum (R11)

- **Feindliche Mobs in der Spawnliste `ambient`** (manifest): BandP, Basilisk, Bee, Brutalfly, CaterKiller, DungeonBeast, EmperorScorpion, EnderKnight, EnderReaper, HerculesBeetle, LeafMonster, Mantis, Molenoid, Rat, Scorpion, SpitBug, TrooperBug. Folgen für Peaceful und Cap.
- **Spawner-Erkennung per Blockscan** (grep `func_145881_a`, 54 Klassen): `Alien`, `Alosaurus`, `BandP`, `Basilisk`, `Bee`, `Boyfriend`, `Brutalfly`, `CaterKiller`, `CaveFisher`, `Cephadrome`, `Crab`, `DungeonBeast`, `EmperorScorpion`, `EnderKnight`, `EnderReaper`, `EntityButterfly`, `GammaMetroid`, `GenericDungeon`, `Ghost`, `GhostSkelly`, `Girlfriend`, `Hammerhead`, `HerculesBeetle`, `LeafMonster`, `Leon`, `LurkingTerror`, `Mantis`, `Molenoid`, `Mothra`, `Nastysaurus`, `NightmareDungeon`, `OreSpawnWorld`, `PitchBlack`, `Pointysaurus`, `Rat`, `Robot2`, `Robot4`, `Robot5`, `Rotator`, `RubberDucky`, `RubyBirdDungeon`, `Scorpion`, `SeaMonster`, `SeaViper`, `SpitBug`, `StinkBug`, `TRex`, `TerribleTerror`, `Trees`, `TrooperBug`, `Urchin`, `Vortex`, `WaterDragon`, `WormLarge`. Ersatz über `MobSpawnType.SPAWNER`; Merker wie `was_spawnered` werden im Original nicht gespeichert.
- **Datum beim Modstart:** Valentinstag (grep `valentines_day`): `Girlfriend`, `MyEntityAIFollowOwner`, `MyEntityAITarget`, `MyValentineTarget`, `RenderGirlfriend`, `Shoes`; Ostern fest am 20. April (EasterBunny, entity-07); Halloween am 31. Oktober für Ghost und GhostSkelly (OreSpawnMain.java:4181-4226, entity-08).
- **1.7.10-Höhen** in `getCanSpawnHere`: Alien unter 50, Alosaurus ab 50, BandP ab 100, Beaver 50 bis 100, Scorpion bis 50, CreepingHorror bis 15, DungeonBeast 25 bis 28 (entity-04 bis 12). Die Spawnprüfung von WormLarge scannt 13x13x14 Blöcke (entity-14).
- **Spawnlisten außerhalb der Biomklassen** fehlen im manifest: ChunkProviderOreSpawn2 (Dino-Liste), BiomeGenUtopianPlains (5 Dimensionen) (6.6).

### 5.10 SRG-Namen ohne MCP-Mapping

- **Besitzer-Methoden** `func_152115_b`, `func_152114_e`, `func_152113_b` fehlen in `methods.csv`; Bedeutung nur aus der Nutzung (grep, 23 Klassen): `Boyfriend`, `Camarasaurus`, `Chipmunk`, `Dragon`, `EntityCannonFodder`, `GammaMetroid`, `Gazelle`, `Girlfriend`, `GirlfriendOverlayGui`, `Hydrolisc`, `Leon`, `OreSpawnTeleporter`, `Ostrich`, `Rat`, `RubberDucky`, `Spyro`, `Stinky`, `ThePrince`, `ThePrinceAdult`, `ThePrinceTeen`, `ThePrincess`, `VelocityRaptor`, `WaterDragon`.
- **Weitere ungemappte Namen:** `func_145881_a` (Spawner-Logik, 5.9), `func_147487_a` (Partikel über WorldServer), `EnchantmentHelper.func_151386_g` und `func_151387_h` (Luck of the Sea, Lure), `StatList.field_151183_A` und `field_151184_B` (Junk, Treasure), `func_70182_d` (Wurfgeschwindigkeit von EntityThrowable), `UltimateFishHook.field_146042_b` (entity-13, entity-04).
- **Umgang:** nach STYLE.md aus `methods.csv`; wo es fehlt, im Port als `// PORT:` mit der erschlossenen Bedeutung markieren.

### 5.11 Modelle, GL-Durchgänge, Client-Zustand (R8)

- **GL-Aufrufe in 19 Modellen** (manifest `gl`): ModelDungeonBeast, ModelFairy, ModelFirefly, ModelGammaMetroid, ModelGhost, ModelGhostSkelly, ModelKraken, ModelKyuubi, ModelPurplePower, ModelRockBase, ModelRotator, ModelSquidZooka, ModelTheKing, ModelThePrince, ModelThePrinceAdult, ModelThePrincess, ModelThePrinceTeen, ModelTheQueen, ModelTriffid. Davon mit `glBlendFunc`: GammaMetroid, Ghost, GhostSkelly, Kyuubi, PurplePower, RockBase, TheKing, ThePrince, ThePrinceAdult, ThePrincess, ThePrinceTeen, TheQueen.
- **Renderer-Durchgänge:** RenderBrutalfly (additiver UV-Scroll), RenderButterfly (Energie-Overlay für Mothra und EntityLunaMoth), RenderEnchantedCow (Glint, Deutung offen) (entity-05, entity-07, design-entities-03).
- **Große Modelle** (manifest Teile): Triffid 178, TrooperBug 134, TheQueen 130, TheKing 119, ThePrinceAdult 119, Kraken 111, PitchBlack 101, Leon 98.
- **Animationszustand im Entity** (`RenderInfo`, grep, 30 Entities): `Alien`, `CaveFisher`, `Cephadrome`, `Dragon`, `DungeonBeast`, `EmperorScorpion`, `GhostSkelly`, `Godzilla`, `Kraken`, `Leon`, `LurkingTerror`, `Nastysaurus`, `Ostrich`, `PitchBlack`, `Robot1`, `Robot2`, `Robot3`, `Robot4`, `Rotator`, `RubberDucky`, `Scorpion`, `SeaMonster`, `SeaViper`, `SpitBug`, `ThePrinceAdult`, `ThePrinceTeen`, `Triffid`, `TrooperBug`, `Urchin`, `WaterDragon`. Darf auf dem Server keine Client-Klassen laden.
- **Client-IK und Modell schreibt Entity-Daten:** RenderSpiderRobotInfo (SpiderRobot, AntRobot), RenderGiantRobotInfo (GiantRobot), ModelRobot4 (Schild in DataWatcher 21).
- **Framegebunden oder nicht synchronisiert:** ModelRotator, ModelChainsaw; EntityLunaMoth `moth_type`, Peacock `blinker`, Whale `spray`.

### 5.12 Projektile, beidseitige Ausführung, fehlende Registrierungen

- **Nicht registriert:** BetterFireball (Client sah Vanilla-Feuerball), ThunderBolt (kein Renderer).
- **Nur `registerGlobalEntityID`, keine Tracking-Werte:** UltimateArrow, UltimateFishHook, IrukandjiArrow.
- **Doppelter Tick durch Feld-Shadowing:** BetterFireball.
- **Beidseitig ausgeführt:** LaserBall, IceBall, InkSack (Einschlag), ItemThunderStaff (Spawn), ItemWrench (`setDead`), EntityCage (Fang), RTPBlock (Würfeln), WormSmall (Graben), Robot1 (Zielsuche), BlockExtremeTorch (Trigger aus `randomDisplayTick`, auf dem dedizierten Server nie).
- **Durchfliegen immuner Ziele per frühem `return`:** WaterBall, InkSack; im Port über `canHitEntity`.

### 5.13 Wasserbindung, negative Heilung, Entfernen ohne Drops

- **`heal` mit negativem Betrag** (grep, 11 Klassen): `AttackSquid`, `Crab`, `Flounder`, `Hydrolisc`, `Irukandji`, `MantisClaw`, `SeaMonster`, `SeaViper`, `Skate`, `WaterDragon`, `Whale`. In NeoForge vermutlich wirkungslos (6.3); `setHealth` verwenden.
- **Entfernen per `setDead` ohne Drops:** Crab, Flounder, Irukandji, SeaMonster, SeaViper, WaterDragon, Whale (Austrocknen), CreepingHorror und Firefly (Tageslicht), Robot1 (Explosion), Urchin, Rotator, Vortex (Tages-Despawn); im Port `discard()`.

### 5.14 1.7.10-Vanilla, das es so nicht mehr gibt

- **`Items.fish` mit allen Metas** (grep): `AttackSquid`, `Crab`, `Flounder`, `GenericDungeon`, `Hydrolisc`, `ItemSifter`, `RubberDucky`, `SeaMonster`, `SeaViper`, `StinkBug`, `UltimateFishHook`, `WaterDragon`, `Whale`. `#minecraft:fishes` enthält Gebratenes.
- **`EntityAIMoveIndoors`** ohne Gegenstück (grep): `BandP`, `Boyfriend`, `Camarasaurus`, `Chipmunk`, `Dragon`, `Gazelle`, `Girlfriend`, `Hydrolisc`, `Ostrich`, `Spyro`, `StinkBug`, `Stinky`, `ThePrince`, `ThePrinceTeen`, `ThePrincess`, `VelocityRaptor`.
- **Metadaten-Blöcke und Fackeln:** Alien, EntityLunaMoth (Fackelsuche), BlockCrystalTorch, BlockExtremeTorch, CreeperRepellent, KrakenRepellent (Stand und Wand), Lizard (alle Farbstoff-Metas), Camarasaurus (Blätter Meta 0 bis 3), Termite, VelocityRaptor (Holz- und Blumenlisten).
- **Nur `minecraft:stone`:** WormSmall, WormMedium, WormLarge (Todesregel grass/dirt/stone), ChunkOreGenerator, Godzilla (Ausschlussliste ohne Deepslate und Tuff). ItemMinersDream ist durch R22 entschieden: der Tunnel räumt jeden Block außer `#c:ores`, unzerstörbaren Blöcken und Blöcken mit Block-Entity; Fackelboden ist jede tragfähige Oberseite.
- **Umgebaute Vanilla-Typen:** Pferdetypen und Wither Skeleton (Dragonfly, ItemSpawnEgg, CritterCage, EntityCage); Schwertblocken entfällt (AmethystSword, CrystalSword, RubySword, PoisonSword); ShovelItem legt Pfade an, das Original nicht (AmethystShovel, itemblock-01); Hills-Biome zusammengelegt (biome_map.json); Jigsaw-Dörfer statt 1.7.10-Dörfer (MapGenMoreVillages, ChunkProviderOreSpawn3); Höhenlimit 256 fest codiert (Kraken).

### 5.15 Griefing und Explosionen

- **Prüfen `mobGriefing`** (grep, 26 Klassen): `Alien`, `Baryonyx`, `Beaver`, `BerthaHit`, `BetterFireball`, `Camarasaurus`, `CaterKiller`, `Chipmunk`, `Elevator`, `EntityThrownRock`, `GammaMetroid`, `Gazelle`, `Godzilla`, `LaserBall`, `Molenoid`, `OreCrystal`, `OreCrystalCrystal`, `PurplePower`, `Robot1`, `Robot2`, `SpiderRobot`, `Termite`, `ThePrincess`, `TheQueen`, `ThunderBolt`, `VelocityRaptor`.
- **Explosionen** (grep): `BerthaHit`, `BetterFireball`, `EntityThrownRock`, `Godzilla`, `Island`, `IslandToo`, `LaserBall`, `OreCrystal`, `OreCrystalCrystal`, `PurplePower`, `Robot1`, `ThunderBolt`.
- **Verändern die Welt ohne `mobGriefing`-Prüfung** (laut Kapiteln): IceBall (Eis über jeden Block), CaterKiller (Netze), Stinky (frisst Kohleerz), SunspotUrchin (Feuer), Termite (Vermehrung), Island und IslandToo (Explosion Stärke 5), Robot2 (Blockzerstörung ohne Härteprüfung), ZooCage und GenericDungeon (überschreiben auch Bedrock), Trees (Duplicator kopiert beliebige Blöcke).

### 5.16 Numerische Dimensionsabfragen

`OreSpawnMain.DimensionID…` wird in 37 Klassen verglichen (grep): `Alien`, `AttackSquid`, `Bee`, `BlockAppleLeaves`, `BlockCrystalLeaves`, `Cockateil`, `Crab`, `CreepingHorror`, `CrystalGrass`, `Dragon`, `DungeonBeast`, `EntityAnt`, `EntityButterfly`, `EntityLunaMoth`, `EntityRainbowAnt`, `EntityRedAnt`, `EntityUnstableAnt`, `Firefly`, `Frog`, `GammaMetroid`, `ItemMinersDream`, `LeafMonster`, `LurkingTerror`, `Mantis`, `OreBasicStone`, `OreSpawnWorld`, `PitchBlack`, `Rat`, `RockBase`, `Termite`, `TerribleTerror`, `WorldProviderOreSpawn`, `WorldProviderOreSpawn2`, `WorldProviderOreSpawn3`, `WorldProviderOreSpawn4`, `WorldProviderOreSpawn5`, `WorldProviderOreSpawn6`. Eine zentrale Abfrage über `ResourceKey` aus R13, früh in W01 oder W05.

## 6. Offene Fragen

Aus allen OFFEN-Listen der Verhaltens- und Designkapitel, entdoppelt. Wo DECISIONS.md oder eine Nachprüfung beim Schreiben dieser Seite die Frage schon beantwortet, steht es dabei.

### 6.1 Durch DECISIONS.md oder Nachprüfung erledigt

- **Config-Typ und Ladezeitpunkt** (OreSpawnMain, ArmorStats, WeaponStats, ItemSpiderRobotKit): R3, eine COMMON-Config; Registrierwerte über EarlyConfig, weil NeoForge COMMON erst nach den Registry-Events lädt.
- **Code oder Config bei King 250/350 und Queen 250/225:** R3, der Code.
- **Köpfe als PartEntity oder eigene Entity** (KingHead, QueenHead, GodzillaHead): R9, eigene Entities.
- **Einheit der Tracking-Reichweite:** R9, Chunks = Original / 16.
- **Entity-Drops als Loot-Table:** R10, Java in `dropCustomDeathLoot`.
- **LootTableLoadEvent oder Global Loot Modifier:** R10, Global Loot Modifier.
- **GeckoLib:** R1 und R8, nicht verwendet.
- **Zuordnungstabelle der 1.7.10-Biome** (R11 „zu erzeugen"): liegt als [`biome_map.json`](biome_map.json) vor.
- **Easter Bunny fehlt im manifest** (itemblock-01): falsch, `easter_bunny` steht in manifest `entities`.
- **Default von `GinormousEmeraldTreeEnable`** (entity-01): 1, Kategorie OreSpawnTWEAKS (manifest).
- **Schaltet `AllMobsDisable` auch Wal und Würmer ab** (entity-14): ja, `disableAllMobs()` (OreSpawnMain.java:5804) setzt `TriffidEnable` (:5889), `WormEnable` (:5890) und `WhaleEnable` (:5899) auf 0; Aufruf OreSpawnMain.java:1258-1260.
- **Wo `TriffidEnable` ausgewertet wird** (entity-13): außerhalb von OreSpawnMain liest es keine Klasse (grep), und Triffid hat keine Spawn-Einträge (manifest). Der Schalter hat im Original keine Wirkung außer über OreSpawnMain selbst.
- **Wo `RockEnable` gelesen wird** (entity-08, entity-11): OreSpawnWorld.java:2015 und 2033; EntityThrownRock liest es nicht.

### 6.2 Widersprüche zwischen Katalog und DECISIONS.md

- **Spawn-Eier:** R9 sieht `DeferredSpawnEggItem` mit Farben vor. Das Jar hat eigene Ei-Texturen, darunter Piktogramm-Eier ohne einfärbbare Fläche (eggcrystalcow, eggrotator, eggvortex, eggirukandji) und Roboter-Eier mit identischer Textur (design/design-items-other.md, verhalten/itemblock-02.md). Mit R0 (Originaltexturen unverändert) nur vereinbar, wenn die Eier ihre eigene Textur behalten. Entscheidung in DECISIONS.md nachtragen.
- **Zählung Critter Cages:** R9 nennt 119 Critter Cages; die Art `cage` im manifest umfasst 114 CritterCage und 5 ZooCage.
- **Tastenzustand pro Spieler:** R15 nennt nur die Payload. Das Original hat einen globalen Wert; pro Spieler ist eine Abweichung, die in DECISIONS.md stehen sollte (core-02).

### 6.3 NeoForge-1.21.1-API vor dem Schreiben prüfen

Quellen: `F:/Repositories/Modpacks/mods/architrave/.cache/sources/`.

- Teilschaden am Ender-Drachen über `EnderDragon.hurt(part, …)` mit Explosionsquelle (TheKing, Godzilla, Cephadrome, Leon, PitchBlack).
- `LivingEntity.getDamageAfterArmorAbsorb`, `EventHooks.canEntityGrief`, `PathType.WATER`, `ServerLevel.setWeatherParameters` (Godzilla, Kraken, Molenoid, alle Wassermobs).
- `LivingEntity.heal` mit negativem Betrag, vermutlich wirkungslos nach `onLivingHeal` (Klassen aus 5.13).
- Ob `Player.zza` und `xxa` serverseitig gesetzt werden (ThePrinceTeen, ThePrinceAdult und alle Reittiere).
- `DimensionTransition`, `isOrderedToSit`, `isInSittingPose`, `isOwnedBy`, `BlockState.isSolid`, `SpawnerBlockEntity.setEntityId` (OreSpawnTeleporter, Ameisen, Termite, Schmetterling).
- Wirkung von `setDayTime` auf abgeleiteten Leveln und Respawnposition ohne Bett (WorldProviderOreSpawn 1 bis 6, Chaos-Void).
- Codec-Schlüssel von `dimension_type`, `structure_set` und Structure, nicht die Record-Feldnamen (world-03).
- Passende NaturalSpawner-Methode für Anfangs-Spawns, Existenz eines Wassersee-Features, Abstandswerte des Vanilla-Sets `minecraft:villages` (world-02, world-03).
- Ob `neoforge:add_spawns` die MobCategory aus dem EntityType nimmt (feindliche Mobs in `ambient`, 5.9).
- Event-Namen `RegisterClientExtensionsEvent` und `RegisterGuiLayersEvent` (ClientProxyOreSpawn, GirlfriendOverlayGui).
- Goal-Takt mit `reducedTickDelay` in `Mob.serverAiStep` (MyEntityAITarget, MyEntityAIWander, MyEntityAIWanderALot).
- Hooks für Leerschwung, Linksklick auf Entity, Rüstungs-Tick und Rüstungstextur (Bertha, BerthaHit, ItemCreeperLauncher, ItemWrench, ItemOreSpawnArmor), und ob der Leerschwung serverseitig ohne eigenes Paket ankommt.
- Ob `Item.Properties.durability` die Tier-Haltbarkeit überschreibt (MantisClaw, BigHammer, UltimateSword).
- Ob eine Zutat mit `neoforge:components` und `minecraft:damage=0` einen frischen Stapel trifft, und ob ModConfigSpec-Werte bei der Auswertung von Rezeptbedingungen lesbar sind (Rezepte, `MinersDreamExpensive`).
- Modell-Loader `neoforge:separate_transforms`, Pfadauflösung von `ArmorMaterial.Layer` (Texturen liegen unter `textures/entity/`), 1.21.1-Rüstungsformel und ihr Deckel (design-items-gear).
- `ItemAbilities.FISHING_ROD_CAST` für die Schnurdarstellung (UltimateFishingRod).
- Zieltaste für Tastencode 56 (`KEY_LALT` = 342 unbestätigt; KeyHandler).

### 6.4 1.7.10-Verhalten ohne Beleg im Repo

Nachlesbar im Bytecode von `reference/jar/mcp/client-1.7.10.jar`, bisher nicht getan.

- **Vanilla-Zahlen:** Wurfgeschwindigkeit und Schwerkraft von EntityThrowable (Acid, BerthaHit, alle Wurfgeschosse); Stückzahl von `dropFewItems` (Beaver, CaveFisher, Fairy, Firefly, Gazelle, GoldFish, Irukandji, LeafMonster, Island, IslandToo, LurkingTerror, Ostrich, TerribleTerror, Urchin, Tshirt, Kuh-Drops); Schaden und Brenndauer von EntitySmallFireball (Kyuubi, Mothra, Brutalfly); XP über `EntityAnimal.getExperiencePoints` (GoldFish, Hydrolisc, Fairy, GoldCow, EntityButterfly, EntityLunaMoth, EntityMosquito); Standard-Maxleben eines EntityLiving (Island, IslandToo, RockBase); Spinnenwerte für SpiderDriver.
- **Vanilla-Abläufe:** `WeightedRandomChestContent.generateChestContents` (Zugzahl, Slotwahl, Überschreiben; GenericDungeon, Trees); Gewichtssummen von ChestGenHooks (9 Truhen-Einträge); `genBiomeTerrain` und Decorator-Defaults (Utopia, Mining, Village, Chaos); `has_precipitation` und Baumart bei `treesPerChunk 1` (Chaos); obfuskierte Grundlisten-Klassen `xz` und `ws`; Ziel `ajn.b` von WorldGenMinable; Meta-Konvention von Fackel, Hebel, Knopf, Treppe und Kolben (GenericDungeon); ob `performWorldGenSpawning` `getCanSpawnHere` ruft (WormLarge, Whale).
- **Engine-Semantik:** negativer Sound-Pitch (PitchBlack); Bedeutung von `fireResistance`; ob `getTrackingRange`/`getUpdateFrequency` oder die `registerModEntity`-Werte gelten (ThePrinceTeen, ThePrinceAdult, Leon, Ostrich, SpiderRobot); DataWatcher-Indizes von EntityTameable, EntitySpider, EntityAgeable; ob `attackEntity` mit `isAIEnabled` tot ist (SpiderDriver); `isDaytime` im Nether (Stinky); Client-Sync von `addVelocity` ohne Treffer (Vortex); `getEntity()` bei Pfeil- und Feuerballschaden (PurplePower, ThePrinceTeen, ThePrinceAdult); Rückfall-Renderer ohne Registrierung (IrukandjiArrow); ob `colorMultiplier` einer BlockGrass-Unterklasse auf alle Seiten wirkt (AntBlock); Rückgabe 31 in `shouldRenderPass` als Glint (EnchantedCow); Sound-Events ohne Namespace `splash` und `vortexlive` (CloudShark, WaterDragon, Whale, Rotator); ItemSword `onBlockDestroyed` und `getStrVsBlock`-Materialien `awt.k`, `awt.l`, `awt.v`, `awt.C`; ItemTool-Schadensaddition; Forge-Werkzeugklasse der Crystal-Werkzeuge; Verrechnung von Rüstungssummen über 25 in Forge; Haltbarkeitsfaktor 11/16/15/13 in `EnumHelper.addArmorMaterial`; Zweig `RenderPlayer` 0,375 für IItemRenderer (Blockgrößen der 3D-Waffen); Knochenmehl-Verhalten von AntBlock.
- **Treffer im selben Tick:** zweiter Schaden von ThunderBolt, ExperienceSword und TheKing (`doJumpDamage`) sowie überlappende Landestoß-Ringe von Godzilla gegen `hurtResistantTime`.
- **Abläufe, die nur ein Spiel belegt:** doppelter Drop von BlockSkyTreeLog; 4-Bit-Überlauf der Zielhöhe (BlockTomato, BlockCorn, BlockQuinoa); Drop-Reihenfolge beim Abbau von KingSpawnerBlock und QueenSpawnerBlock; Lava-Einfluss bei RubyBirdDungeon in Utopia; ob Mineshafts und Strongholds in Mining und Village je entstehen; Seed-Unabhängigkeit der Mining-Erze; Höhenverteilung der Chaos-Steinlagen; veraltete Server-BoundingBox von GodzillaHead nach direktem `posX`; mögliches NPE bei ThunderBolt und IceBall ohne Werfer; Tanzdrehung und Duckpose (MyEntityAIDance); doppeltes Feuern von RiderControl und `sendToServer` ohne Verbindung.

### 6.5 Nicht gelesene Stellen im Originalquelltext

- Frühe Abbrüche in `OreSpawnWorld.addHugeTree` vor Zeile 1905, also die Häufigkeit der King- und Queen-Bäume; Vorbedingungen `D4BigSpaceCheck`, `addAppleTrees`; Chance von `addVeggies` für den Duplicator-Stamm (OreSpawnWorld.java:1997).
- Weltgen-Platzierer für `kingspawner` und `queenspawner` (grep fand keinen).
- Wirkradius des Creeper Repellent auf PurplePower (Box vor CreeperRepellent.java:85).
- Weitere Zweige in Cephadrome.java gegen Kraken; Sonderfaktor ThePrinceAdult.java:295; die Klasse, die beim Tod der Cloud Shark Kraken ruft (Recherche).
- Nastysaurus als `critter` in `GenericDungeon.addLevelDecorationsQ` (Zeilen 6789 bis 6928); Dimensionen der Aufrufer von `addLevelDecorations(Q)`, `makeEnderDragonHospital`, `makeShadowDungeon`, `makeDungeon`, `makeHauntedHouse`, `makeIgloo`.
- Rückverwandlung Apple Leaves zu Scary Leaves (vermutlich BlockAppleLeaves).
- Stützprüfung `canBlockStay` von BlockCrystalTorch und BlockExtremeTorch; Blattzerfall der OreSpawn-Blattklassen im Detail.
- `PitchBlack.getPitchBlackScale()` für die Frage, ob Nightmare-Leben 1024 übersteigt (NightmareDungeon, W08).
- `ModelRobot4.wingspeed`, `ModelCoin` und die Flugmodelle (ModelCliffRacer, ModelCloudShark, ModelDragonfly, ModelCockateil).
- Rückgabewert von `CommonProxyOreSpawn.setArmorPrefix` (Render-Index), `getMaterialName()` in mehreren Item-Klassen, Klemmung von `e_unbreaking` und `e_featherfalling` in `get_armorstats`.
- Bedeutung des zweiten Konstruktorarguments von CritterCage und ItemSpawnEgg; Anwendung von `MobStats.defense` in den Entities; ob abgeschaltete Enable-Flags das Spawnen per Ei verhindern.
- Wirkung von `EntityCannonFodder.get_is_activated` auf MyEntityAIAvoidEntity.
- Bild von Icon-Index 6 in ItemShoes; ob ein gefangener Mob den Spinner-Index von EntityCage vor dem Drop ändert.
- Verbraucher der Projektil-Indizes aus OreSpawnConstants (49, 50, 65, 81, 84, 85, 86), weil javac sie inline eingesetzt hat.

### 6.6 Lücken im manifest und in den Tabellen (Generator `tools/catalog.py`)

- **Fehlende Spawn-Einträge:** Mobzilla in VillageMania (BiomeGenUtopianPlains.java:214-216), GoldFish (Utopia, Danger, Chaos), Irukandji (Crystal-Wasser), LeafMonster und HerculesBeetle (Danger, Chaos), die Dino-Liste von ChunkProviderOreSpawn2; Halloween-Spawns von Ghost und GhostSkelly ohne Datumsbedingung.
- **Texturen und Namen:** `eyemoth.png` fehlt im Renderer-Eintrag von Mothra und in `texture_map`; `crystalfurnace_front_off` als unaufgelöster StringBuilder-Ausdruck; `rice_plant` und `strawberry_plant` ohne Anzeigenamen; Renderer und Textur von IrukandjiArrow fehlen; ModelTshirt deklariert 512x256, Textur 320x160; Anzeigename `T-Shirt` gegen `T-Shirt!`; `ore.png` von keiner Klasse benutzt.
- **Falsche oder fehlende Einträge:** ModelElevator ist nicht unbenutzt, RenderElevator zeichnet es (entity-07); MyEnderPearlBlock und MyEyeOfEnderBlock laufen als `dried_egg_ore`, sind aber 3x3-Speicherblöcke; keine Registry-Id für BetterFireball, ThunderBolt und die Wandvarianten der vier Fackeln; Gazelle mit zwei Tempowerten 0,2 und 0,3 (wirksam 0,3, Gazelle.java:26/33); Hitbox `null` bei Wurfgeschossen, Kühen und SpiderDriver (aus Vanilla geerbt).
- **Nicht katalogisiert:** Zuordnung der Wassereimer-Rezepte (OreSpawnMain.java ab 2326) von Vanilla-`spawn_egg`-Metas auf 1.21.1-Ei-Items; Tint-Rückfall für die Piktogramm-Eier; die Spinner-Kacheln 6, 50, 84 und 86 von `spinners.png` als eigene Flugtexturen; Grundmaße aller Baumgeneratoren.
- **Zählweise:** `03-items.md` zählt 40 Speisen, das manifest 33 `food`-Einträge (Pizza und ItemSeedFood anders gezählt).

### 6.7 Portierungsentscheidungen: 1:1 oder korrigieren

Jede dieser Fragen braucht einen Eintrag in DECISIONS.md, bevor die betroffene Welle startet.

- **Originalfehler an Entities:** Fairy schreibt `FairyType`, liest `fairyType`; Nachwuchs zahmer GammaMetroid und WaterDragon ohne Besitzer; Gazelle `findBuddy` findet sich selbst; Girlfriend und Leon lassen Fremde per Diamantblock den Besitz übernehmen, Namensschild-Zweig unerreichbar (Girlfriend, Ostrich); Mantis greift sich nach Wassertreffer selbst an; Rat folgt ihrem Besitzer nie; Robot4-Schild blockiert nie; Rotator-Pfeilimmunität greift nie; RubyBird `setFlyUp` wirkungslos; PitchBlack gibt frisch gespawnt 200 statt `100·scale` XP; EntityLunaMoth `moth_type` weder synchronisiert noch gespeichert; Giftdauer-Fehler nach Schwierigkeit (Alien, EmperorScorpion, SeaViper); Mothra-Butterfly-Teleport geerbt; Hydrolisc `heal(-1)` als Lebensübertragung; attackEntityFrom liefert `false` trotz Schaden (Ostrich, Leon, Dragon); Kraken ohne `LongEnough` im NBT sofort auf Flucht.
- **Originalfehler an Items und Blöcken:** BlockCrystalPlant gelb droppt blau; Kristallofen lädt `_front_on` nie; Zielhöhe der Stapelpflanzen wird jeden Tick neu gewürfelt; Kettensäge ohne Effizienz und Blattmodus, weil die Methoden tot sind (Abweichung zu `03-items.md`); `leaves_peach` droppt nie; ItemSifter droppt beim Graswurf 5 zusätzlich Weizen; Steinwurf ohne +0,5 bei Koordinate 0 (ItemRock); StepUp, StepDown, StepAccross wirkungslos bei negativem Gierwinkel; ExperienceCatcher verbraucht sich auch im Kreativmodus; BlockRadish und BlockRice ernten unreif volle Menge; Titan- und Uran-Glühen global je Blocktyp; CreeperRepellent bricht bei PurplePower Typ 10 die ganze Abstoßung per `return` ab.
- **Sounds:** Irukandji `little_splt` und Flounder `little_splat` ohne Namespace, im Original stumm; `splash` und `vortexlive` ohne Namespace.
- **Fehler in der Weltgenerierung:** `addMosquitos` in VillageMania mit Chunk-Index statt Blockkoordinate (OreSpawnWorld.java:103); `(int)`-Cast Richtung 0 im Teleporter und in Kreisen von GenericDungeon; `addD4WhiteHouse` prüft `z < 300` (OreSpawnWorld.java:2441), `addD4RobotLab` eine unpassende Fläche; `piston_extension` in den White-House-Bänken und `lit_redstone_lamp` ohne Strom (IncaPyramid).
- **Ersatzmechaniken:** Ersatz für `recently_placed` (spacing, separation, exclusion zone je Dimension; Originaldichte nicht exakt erreichbar); Seed plus ChunkPos statt `Math.random` für Labyrinthe; `EntityAIMoveIndoors` nachbauen oder weglassen; Spieler-Tempo von VelocityRaptor war ein Client-Hack (serverseitiger AttributeModifier); CrystalFurnace mit oder ohne Rückfall auf die NeoForge-Brennstoff-Data-Map; Kristallplanken nicht in `#minecraft:planks`; Stand- und Wandfackel-Ids; Wither Skeleton und Pferdevarianten in Käfigen und Eiern.
- **Welt und Höhen:** Y-Bänder der Erze und Features absolut oder auf `min_y` −64 angepasst; Oberflächensuche per Heightmap statt Start bei Y 100 oder 128; Deepslate in Wurm-Blockliste, ItemMinersDream und ChunkOreGenerator; Wurm-Blockliste 1:1 oder als Tag; Mineshafts und Strongholds in Mining und Village; Chaos-Respawn über dem Void 1:1 oder Umleitung; doppelte 1.21.1-Biome aus zwei 1.7.10-Biomen addieren oder zusammenfassen.
- **Kategorien und Datum:** MobCategory für feindliche Mobs aus `ambient` (5.9) und WormLarge als CREATURE oder MONSTER; Datumsprüfung (Valentinstag, Ostern am 20. April, Halloween) beim Serverstart wie im Original oder zur Laufzeit; Ostern fest oder beweglich.
- **Darstellung:** Opazität der Kristallblöcke immer `noOcclusion` oder opak mit Cutout; Ersatztexturen `generic_solid` und `scary_solid` für schnelle Grafik; Tick- statt Framebindung bei ModelRotator und ModelChainsaw; EntityLunaMoth-Textur synchronisieren.
- **Sonstiges:** BandP-Diebesgut verliert NBT und Komponenten; Offhand beim Diebstahl; Anzahl gleichzeitiger PurplePower-Kugeln begrenzen oder nicht; Leonopteryx-Config verdrahten oder tot lassen; NightmareDungeon als bewusste Lücke dokumentieren.

### 6.8 Recherche widerspricht dem Code (Code gilt, nur dokumentieren)

- Cephadrome gegen Kraken: Code 1,5 × 70 = 105, Recherche 140 (anderer Zweig ungeprüft, 6.5).
- Red Ant Angriff 2 statt 1,0; Fairy Angriff 3 statt 2,0; Gazelle spawnt laut Recherche in Plains, laut Code nur in Utopia; Hydrolisc laut Recherche 50 Leben, Angriff 0, Rüstung 0, laut Code 100/1,0/10; Crab laut Recherche 180/90/45 Leben, laut Code aus PitchBlack-Werten; Robot4 1,7 s statt 65 Ticks Unverwundbarkeit.
- Magic Apple: Recherche 50 % Ginormous Tree, Code 1 %; Duct Tape repariert 1/6 der Maximal-Haltbarkeit, nicht des Schadens; Lava Foam macht geschwindigkeitsabhängigen Fallschaden statt 0,5 Kontaktschaden; Crystalized Rats spawnen immer 1 bis 10 statt mit Chance 1/10; Kraken Repellent wirkt 40x50x40 statt etwa 10x10; Random Dungeon braucht festes Material statt Stein oder Erde.
- BandP ist Politiker, nicht Polizei; Red Ant Robots rächen keine Ameisen; Cephadrome-Altäre gibt es nicht als eigene Struktur; Rat-Config wirkt nur bei Ratten mit Besitzer; Lizard frisst jeden Farbstoff, nicht nur Tintenbeutel.
- Dimensionen: Unstable Ant führt nach Islands (intern DimensionID4), die Recherche nennt Danger; Spyro spawnt in Dimension-Extreme, die Recherche nennt Mining; Skate spawnt in Utopia, VillageMania, Islands und Crystal, die Recherche nennt nur Crystal; die Recherche nennt eine Danger-Dimension für Nightmares ohne eindeutige Zuordnung.
- Strukturen: Island-Inseln enthalten kein Bedrock als Baustoff, CrystalMaze baut nur aus Bedrock, nicht aus Obsidian; Easter Bunny spawnt am 20. April, nicht zu Ostern.
