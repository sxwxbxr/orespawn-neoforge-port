# Portierungsentscheidungen

Bindend für jeden, der an diesem Port baut. Wo ein Kapitel in `docs/catalog/` oder
eine Portierungsnotiz eines Agenten etwas anderes sagt, gilt diese Datei. Eine
Entscheidung wird geändert, indem man sie **hier** ändert und begründet, nicht
indem man im Code davon abweicht.

Stand: 2026-09-10. Grundlage: `docs/research/` (fünf Berichte), `docs/catalog/`
(aus dem Jar erzeugt) und der dekompilierte Quelltext unter `reference/`.

## R0 — Zweck und Grenzen

Privater 1:1-Port von OreSpawn 1.7.10 Build 20.3 (TheyCallMeDanger) auf NeoForge
1.21.1, nur für eigene Packs und Freunde. Originaltexturen, -sounds und
-modellgeometrie werden unverändert übernommen.

Der Autor hat zuletzt ausdrücklich geschrieben, dass er keinen Port will, und
ein Teil der Kreaturmodelle gehört Dritten (`docs/research/04-history-license-ports.md`).

**Geändert am 20.09.2026 (Entscheidung des Nutzers).** Die ursprüngliche Fassung
lautete: „Dieser Mod wird nicht veröffentlicht — weder auf CurseForge noch auf
Modrinth noch als öffentliches Repo." Der Quelltext und die Dokumentation liegen
seitdem in einem öffentlichen Repo. Die Grenze verläuft jetzt hier:

| | |
|---|---|
| **öffentlich** | Quelltext, Werkzeuge, Recherche, Katalog, diese Entscheidungen |
| **nicht im Repo** | jede Textur, jeder Sound, `sounds.json`, `lang/en_us.json` — von `.gitignore` gesperrt, erzeugt aus der eigenen Jar-Kopie über `tools/assets.py` |
| **nie** | ein gebautes Jar, eine Veröffentlichung auf CurseForge oder Modrinth, eine Lizenz, die Nutzungsrechte einräumt |

Der Grund für die Trennung ist die Rechtelage, nicht Vorsicht: die Lizenz von
10/2020 untersagt die Weiterverbreitung „in whole or in part", und die Modelle
Dritter könnte auch der Autor nicht freigeben. Der Quelltext bleibt trotzdem eine
Ableitung eines geschützten Werks und steht unter *All Rights Reserved* — er ist
einsehbar, nicht nutzungsfrei. Eine Veröffentlichung **als Mod** bliebe, was sie
war: eine neue Entscheidung mit neuen Assets und anderem Namen.

## R1 — Plattform

Minecraft 1.21.1, NeoForge 21.1.248, Java 21, ModDevGradle 2.0.144, Parchment
2024.11.17. **Keine Mixins.** **Keine Bibliotheksabhängigkeit** — GeckoLib ist
nach der Modellanalyse unnötig (R8) und wurde aus dem Build entfernt.
Dedizierter Server muss laufen: alles aus `net.minecraft.client` liegt unter
`com.swbr.orespawn.client`.

## R2 — Namen und IDs

| Art | Registry-Id | Herkunft |
|---|---|---|
| Items, Blöcke | `unlocalizedName` des Originals, kleingeschrieben | `manifest.json` → `id` |
| Entities | snake_case des `registerModEntity`-Namens (`The King` → `the_king`) | `manifest.json` → `id` |
| Sounds | Original-Event kleingeschrieben (`MothraWings` → `mothrawings`) | `manifest.json` → `sounds` |
| Texturen | Originaldateiname kleingeschrieben, Entity-Texturen unter `textures/entity/` | `manifest.json` → `texture_map` |

Die Ids im Manifest sind endgültig. Drei Sonderfälle:

- `CrystalFurnaceBlock` und `CrystalFurnaceOnBlock` teilen sich `crystalfurnace`
  → **ein** Block `crystalfurnace` mit `BlockStateProperties.LIT`.
- `pizza_item` und `ducttape_item`: das Item hieß im Original wie der Block.
- Numerische IDs (`BaseBlockID`, `BaseItemID`, `DimensionID`, Biome-IDs) gibt es
  in 1.21.1 nicht mehr; diese Config-Schlüssel entfallen ersatzlos.

Anzeigenamen kommen aus dem Jar (`lang/en_us.json`, erzeugt von `tools/assets.py`).
Eine deutsche Übersetzung ist nicht vorgesehen.

## R3 — Werte und Config

Jede Zahl stammt aus dem Jar: Mob-Werte aus `mob_stats`, Materialien aus
`tool_materials`/`armor_materials`, alles andere aus dem Quelltext mit Zeilenangabe
in `docs/catalog/verhalten/`. Wo Code und Config-Default sich widersprechen
(The King: Config 350, Klasse setzt 250), gilt **der Code** — er ist, was im
Spiel passierte.

Die 628 Config-Schlüssel werden als **eine** `ModConfigSpec` vom Typ `COMMON`
nachgebaut (`config/orespawn-common.toml`), Abschnitte nach Originalkategorie
(`OreSpawnTWEAKS`, `OreSpawnWEAPONS`, `OreSpawnMOBS`, `OreSpawnARMOR`,
`OreSpawnORES`), Schlüsselnamen unverändert. Das Original hatte ebenfalls eine
einzige globale Datei; eine SERVER-Config pro Welt wäre eine Abweichung ohne
Gewinn. Der Abschnitt `OreSpawnIDS` entfällt (numerische IDs, siehe R2).

**Ladezeitpunkt, belegt:** NeoForge lädt COMMON-Configs erst *nach* den
Registry-Events (`net/neoforged/neoforge/internal/CommonModLoader.java:51-65`:
erst „Registry initialization", dann „Config loading"). Werte, die beim
Registrieren feststehen müssen — Werkzeug- und Rüstungsmaterialien,
Haltbarkeit, Verzauberbarkeit —, liest deshalb eine kleine `EarlyConfig` beim
Mod-Konstruktor direkt aus derselben TOML-Datei und fällt auf die Jar-Defaults
zurück, wenn die Datei noch nicht existiert. Die `ModConfigSpec` bleibt
Eigentümerin der Datei. Änderungen an diesen Werten brauchen einen Neustart —
wie im Original. Alles andere liest der Code zur Laufzeit aus der Spec.

## R4 — Lebenspunkte über 1024

`Attributes.MAX_HEALTH` ist in 1.21.1 auf 1024 geklemmt. Betroffen: The King
(7000), The Queen (6000), Mobzilla (4000), die Köpfe, Young Adult Prince (3000),
Young Prince und Robot Spider (1500). **Virtuelle Lebenspunkte:** das Attribut
bekommt `min(original, 1024)`, die Entity-Klasse kennt den Originalwert, und
eingehender Schaden wird *nach* allen Original-Deckeln mit
`1024 / original` skaliert. Bossleisten und Kampfdauer bleiben damit exakt wie
im Original. Umsetzung zentral in einer Hilfsklasse, nicht pro Boss.

## R5 — Rüstung

1.7.10 rechnete `schaden × (25 − Rüstung) / 25` ohne Deckel: ab 25 Punkten war
blockbarer Schaden null. Genau davon leben die Bosse (Rüstungswerte 21–26) und
die Royal-Guardian- und Queen-Scale-Sets (42 und 48 Punkte). 1.21.1 deckelt auf
80 % Reduktion und klemmt `ARMOR` auf 30 — ein direkter Übertrag würde jeden
Boss und jede Endgame-Rüstung entwerten.

**Entscheidung:** die Original-Formel wird nachgebaut, ohne Mixin, über den
`DamageContainer` von NeoForge (`LivingIncomingDamageEvent`, Reduktion
`ARMOR` ersetzen). Sie greift, wenn das Ziel eine OreSpawn-Entity ist oder ein
Spieler mindestens ein OreSpawn-Rüstungsteil trägt; die Rüstungssumme wird dann
aus den Item-Werten gerechnet, nicht aus dem geklemmten Attribut. Schalter
`legacyArmorFormula` (COMMON, Default `true`). Die genaue API vor dem Schreiben
in den NeoForge-Quellen nachsehen — nicht aus dem Gedächtnis.

## R6 — Werkzeuge und Waffen

`Tier` je `tool_materials`-Eintrag. 1.7.10-Schwerter machten `4 + material.damage`;
in 1.21.1 ist es `1 (Spieler) + Modifikator + tier.getAttackDamageBonus()`. Also
Tier-Bonus = Materialschaden, Schwert-Modifikator = 3 — die Ultimate Sword kommt
damit auf die dokumentierten 40. Angriffsgeschwindigkeit: Vanilla-Werte
(Schwert −2,4), weil 1.7.10 keinen Cooldown kannte und es keine sinnvolle
1:1-Entsprechung gibt. Harvest Level → `incorrect_for_*`-Tags: 0 Holz, 1 Stein,
2 Eisen, 3 Diamant, ≥4 Netherite.

## R7 — Vorverzauberte Items

Verzauberungen sind in 1.21.1 datengetriebene Registries und stehen beim
Item-Registrieren noch nicht fest. Items, die im Original ab Werk verzaubert
sind (Ultimate-Werkzeuge, die `e_*`-Stufen der Rüstungen), bekommen ihre
Verzauberungen in `onCraftedBy` und, als Rückfall für Kreativ und Loot, beim
ersten `inventoryTick` ohne Verzauberung.

## R8 — Modelle und Rendering

**Alle 109 Modelle werden 1:1 übernommen, auf Vanilla-`ModelPart`.** Die
Geometrie erzeugt `tools/gen_models.py` nach
`client/model/geom/<Name>Geometry.java` — nie von Hand ändern. Die Animation
wird von Hand aus `render()`/`setRotationAngles()` nach
`client/model/<Name>Model.java` portiert:

- Schreibzugriffe auf `rotateAngle*`/`rotationPoint*` → `setupAnim`, mit
  `resetPose()` am Anfang, weil das Original Felder frameübergreifend stehen ließ.
- `glBlendFunc`/`glColor4f`-Blöcke → zweiter Durchgang mit
  `RenderType.entityTranslucent` und ARGB-Farbe.
- `glRotatef`/`glTranslatef` um Teilgruppen → `PoseStack` in `renderToBuffer`.
- `preRenderCallback`-Skalierung → `LivingEntityRenderer.scale()`.
- Texturvarianten → `getTextureLocation(entity)` über `SynchedEntityData`.
- Die acht 3D-Waffen → `BlockEntityWithoutLevelRenderer` über
  `IClientItemExtensions`.

Belegte Umrechnung: `docs/research/06-models-design.md`, Abschnitt „Verified conventions".

## R9 — Entities

Ein `EntityType` je Manifest-Eintrag. `MobCategory` aus dem `addSpawn`-Typ, sonst
aus der Basisklasse. Hitbox = erstes `size`-Paar; weitere `setSize`-Aufrufe sind
Laufzeit-Umskalierungen → `getDefaultDimensions` überschreiben und
`refreshDimensions()`. Tracking-Reichweite in Chunks = Original / 16.
`DataWatcher` → `SynchedEntityData`, `EntityAI*` → `Goal`, eigenes
`updateAITasks` → `customServerAiStep`. Die Köpfe (`king_head`, `queen_head`,
`mobzilla_head`) bleiben eigene Entities wie im Original.

**Spawn-Eier (korrigiert nach Katalog 6.2):** die 114 Eier sind **kein**
`DeferredSpawnEggItem`. Das Original hat je Ei eine eigene Icon-Textur, darunter
Piktogramm-Eier ohne einfärbbare Fläche (`eggcrystalcow`, `eggrotator`,
`eggvortex`, `eggirukandji`); ein getöntes Vanilla-Ei würde R0 brechen. Ein
eigenes `Item` je Ei mit `item/generated`-Modell auf die Originaltextur, das
Spawnverhalten aus `ItemSpawnEgg` portiert (Rechtsklick, Dispenser über
`DispenserBehaviorOreSpawnEgg`).

Zählung laut Manifest: 114 Spawn-Eier, 119 Käfig-Items (114 `CritterCage` +
5 `ZooCage`), 121 „Ancient Dried Spawn Egg"-Blöcke (`OreGenericEgg`). Alle
bleiben eigene Registrierungen.

## R10 — Drops

Entity-Drops bleiben **Java-Code** in `dropCustomDeathLoot`, weil das Original
sie mit Zufall, Bedingungen und Schleifen im Code würfelt; ein Loot-Table-Nachbau
wäre eine zweite, abweichende Wahrheit. Die Loot-Tables der Entities bleiben leer.
`ChestGenHooks` → Global Loot Modifier auf die Vanilla-Truhen-Tables.

## R11 — Natürliches Spawnen

Die 348 `addSpawn`-Regeln bekommt ein eigener `BiomeModifier`-Typ
`orespawn:config_spawns`, der die Tabelle aus dem Manifest liest und die
Enable-Flags aus der COMMON-Config prüft. 1.7.10-Biomnamen werden über eine
Zuordnungstabelle (`docs/catalog/`, zu erzeugen) auf 1.21.1-Biome gemappt.
`getCanSpawnHere` → `RegisterSpawnPlacementsEvent` mit portierter Bedingung.

## R12 — Weltgenerierung

- Erze → `ConfiguredFeature`/`PlacedFeature` mit Werten aus der Config; Raten
  über einen eigenen Placement-Modifier aus der COMMON-Config.
- Dekorationen aus `OreSpawnWorld` (Bäume, Nester, kleine Strukturen) →
  `Feature`, **wenn** sie in den erlaubten 3×3-Chunk-Bereich passen.
- Alles Größere, insbesondere `GenericDungeon`, → `Structure` mit
  `StructurePiece`, deren `postProcess` nur innerhalb der übergebenen
  `BoundingBox` schreibt. Die Blockplatzierung wird Zeile für Zeile aus dem
  Original portiert, nicht als NBT-Vorlage nachgebaut.

## R13 — Dimensionen

Sechs Dimensionen als Datapack-`dimension_type` und `dimension` unter
`data/orespawn/`, jede mit eigenem `ChunkGenerator`-Codec als Port des
`ChunkProviderOreSpawnN`:

| Original | Id | Zugang |
|---|---|---|
| Dimension-Utopia | `orespawn:utopia` | Brown Ant |
| Dimension-Extreme | `orespawn:mining` | Red Ant |
| Dimension-VillageMania | `orespawn:village` | Rainbow Ant |
| Islands | `orespawn:danger` | Unstable Ant |
| Crystal | `orespawn:crystal` | Termite (leeres Inventar, keine Rüstung) |
| Chaos | `orespawn:chaos` | Butterfly |

Reisen über `ServerPlayer.changeDimension(DimensionTransition)`; die
Zielsuche und Plattformlogik aus `OreSpawnTeleporter` wird portiert.

## R14 — Rezepte

Aus `docs/catalog/recipes.json` erzeugt, als Datagen oder Generator, nie von
Hand. OreDictionary-Namen → `c:`-Tags.

## R15 — Netzwerk, Eingabe, GUI

`RiderControl` → `CustomPacketPayload` (Client → Server), `KeyHandler` →
`KeyMapping` über `RegisterKeyMappingsEvent`.

**Bewusste Abweichung:** das Original hält den Flug-/Reit-Tastenzustand in
einem einzigen statischen Feld `flyup_keystate` für *alle* Spieler (gelesen von
Cephadrome, Dragon, Elevator, Leon, Ostrich, ThePrinceAdult, ThePrinceTeen).
Im Mehrspieler steuert damit ein Spieler das Reittier eines anderen. Der Port
hält den Zustand **pro Spieler** in einem Attachment, gesetzt von der Payload.
Einspieler verhält sich identisch. `Elevator` und `SpiderRobot` greifen im
Original auf `EntityClientPlayerMP` zu; das wird über dieselbe Payload
ersetzt, sonst lädt der dedizierte Server die Klasse nicht. Crystal Furnace und Crystal
Workbench → `MenuType` plus `Screen` mit den Original-GUI-Texturen.
`GirlfriendOverlayGui` → `LayeredDraw`-Layer.

## R16 — Umfang

Alles aus 20.3 kommt hinein, auch Girlfriend/Boyfriend mit allen 62 bzw. 48
Texturvarianten und das Valentins-Verhalten. Was nur in DangerZone oder im
abgebrochenen 1.12-Port existierte, kommt **nicht** hinein.

## R17 — Was „fertig" hier heißt

Auf dieser Maschine gibt es keinen Client. Belegbar sind:

1. `./gradlew build` grün,
2. `./gradlew runGameTests` — jede Entity spawnt und tickt 100 Ticks ohne
   Ausnahme, jeder Block lässt sich setzen, jede Dimension lädt,
3. `./gradlew runServer` bis `Done` ohne `Failed to create mod instance`,
4. `tools/mod.py verify orespawn`.

Gerendert oder gespielt ist damit nichts. Das gehört in jede Fertigmeldung.

## R18 — Originalfehler und Ersatzmechaniken (Katalog 6.7)

**Grundregel: 1:1, auch die Fehler.** Ein Port, der still repariert, ist
nicht mehr OreSpawn — und jede Reparatur ist eine Behauptung darüber, was der
Autor gemeint hat. Abgewichen wird nur in vier Fällen, jeweils mit
`// PORT:`-Kommentar:

1. der Originalcode würde in 1.21.1 abstürzen (NPE, Klassenladen auf dem Server),
2. globaler Zustand greift im Mehrspieler auf fremde Spieler über (R15),
3. die Mechanik ist in 1.21.1 technisch nicht nachbaubar,
4. Client und Server sähen Verschiedenes, weil 1.7.10 ein Feld nie synchronisierte.

Daraus folgen die Einzelfragen des Katalogs:

| Frage | Entscheidung |
|---|---|
| Entity-Fehler (Fairy-Typ, besitzerloser Nachwuchs, Gazelle findet sich selbst, Besitzübernahme per Diamantblock, Mantis greift sich an, Rat folgt nie, Robot4-Schild, Rotator-Pfeilimmunität, RubyBird-Flug, PitchBlack-XP, Giftdauer, geerbter Mothra-Teleport, `attackEntityFrom` → `false`, Kraken ohne `LongEnough`) | 1:1 |
| Hydrolisc `heal(-1)` als Lebensentzug | 1:1 in der Wirkung: direkt über `setHealth`, falls `heal` negative Beträge in 1.21.1 verwirft |
| EntityLunaMoth `moth_type` nicht synchronisiert/gespeichert | synchronisieren und speichern (Fall 4) |
| Item-/Block-Fehler (gelbe Kristallpflanze droppt blau, Ofen-Front `_on` nie, Zielhöhe neu gewürfelt, tote Kettensägen-Methoden, `leaves_peach` droppt nie, Sifter-Weizen, ItemRock-Versatz, Step* bei negativem Gierwinkel, ExperienceCatcher im Kreativmodus, unreife Ernte, CreeperRepellent-`return`) | 1:1 |
| Titan-/Uran-Glühen global je Blocktyp | 1:1 als Zustand des Block-Singletons, gelesen von `animateTick` (Partikel). **Korrigiert in W02:** das Original setzt keinen Lichtwert (`OreTitanium.java:48-52, 70-99` erzeugen nur Redstone-Partikel); die ursprüngliche Zeile hier nannte eine `lightLevel`-Funktion und war falsch |
| Sounds ohne Namespace (`little_splt`, `little_splat`, `splash`, `vortexlive`) | stumm, wie im Original |
| Weltgen-Fehler (`addMosquitos` mit Chunk-Index, `(int)`-Cast Richtung 0, `z < 300`, Robot-Lab-Fläche) | 1:1 |
| `piston_extension`, `lit_redstone_lamp` in Strukturen | 1:1 mit `moving_piston` und `redstone_lamp[lit=true]` |
| `recently_placed`-Abstandszähler | Ersatz über `spacing`/`separation` der Structure Sets je Dimension (Fall 3); Dichte an der Originalrate ausrichten, Abweichung dokumentieren |
| `Math.random` in Labyrinthen und Strukturen | Seed plus `ChunkPos` (Fall 3: Chunk-Generierung muss deterministisch sein) |
| `EntityAIMoveIndoors` | als eigenes Goal nachbauen |
| VelocityRaptor-Spielertempo (Client-Hack) | serverseitiger `AttributeModifier` (Fall 1) |
| CrystalFurnace-Brennstoffe | nur die Originalliste, keine Data-Map-Rückfallebene |
| Kristallplanken in `#minecraft:planks` | nein, wie im Original |
| Fackel-Ids | Standvariante `<id>`, Wandvariante `<id>_wall` |
| Wither Skeleton und Pferdevarianten in Käfigen/Eiern | auf die getrennten 1.21.1-Typen abbilden |
| Y-Bänder von Erzen und Features | absolute Originalwerte |
| Erze und Wurm-Blockliste im Tiefenschiefer | ersetzen `stone_ore_replaceables` **und** `deepslate_ore_replaceables` mit demselben Block (keine Tiefenschiefer-Textur im Original) |
| Oberflächensuche ab Y 100/128 | Heightmap `WORLD_SURFACE_WG` (Fall 3, Welt reicht bis 320) |
| Mineshafts/Strongholds in Mining und Village | genau dann, wenn der Original-ChunkProvider ihre Generatoren aufruft |
| Chaos-Respawn über dem Void | 1:1 |
| zwei 1.7.10-Biome auf ein 1.21.1-Biom (z. B. `forest` + `forestHills`) | nicht addieren: je Entity und Zielbiom der größere Eintrag |
| MobCategory feindlicher Mobs aus `ambient` | 1:1 die Kategorie des `addSpawn`-Aufrufs |
| Datumsprüfungen (Valentinstag, 20. April, Halloween) | beim Serverstart wie im Original, Ostern fest am 20. April |
| Opazität der Kristallblöcke | wie im Original (`isOpaqueCube`), Cutout-Rendering |
| schnelle Grafik (`generic_solid`, `scary_solid`) | entfällt, 1.21.1 hat keinen Texturtausch dafür |
| ModelRotator/ModelChainsaw an Systemzeit gebunden | `ageInTicks` plus Teiltick |
| BandP-Diebesgut verliert NBT | 1:1, Komponenten gehen verloren; Offhand wird nicht bestohlen |
| Zahl gleichzeitiger PurplePower-Kugeln | 1:1 ohne Deckel |
| Leonopteryx-Config, NightmareDungeon | tot lassen, als Lücke dokumentiert |

## R19 — Schwertblocken (offene Punkte aus W03)

- **Alle Schwerter, die in 1.7.10 blocken konnten, blocken auch im Port, mit
  ihrer eigenen Dauer.** Belegt im Bytecode des 20.3-Jars: jede dieser Klassen
  überschreibt `func_77626_a` (`getMaxItemUseDuration`) —
  UltimateSword und die drei Großwaffen 9000, NightmareSword 5000,
  ExperienceSword/PoisonSword/MantisClaw 3000, **AmethystSword 3500,
  CrystalSword 300, RubySword 4000, EmeraldSword 3000**. Die Übergabe
  `docs/port/W03.md` nahm für die letzten vier Vanilla-72000 an; das war falsch,
  und die Zeile „Schwertblocken entfällt" im Katalog-README ist überholt.
  Grund: R18, 1:1.
- **Halbierung im Unverwundbarkeitsfenster: bewusste Abweichung.** Im Original
  halbierte `damageEntity` den Schaden *nach* dem Abzug von `lastDamage`;
  NeoForges `LivingIncomingDamageEvent` feuert davor. Ein stärkerer zweiter
  Treffer im Fenster wird deshalb als Ganzes halbiert statt nur die Differenz —
  höchstens ein halber Punkt Unterschied je solchem Treffer. Den Vanilla-Ablauf
  dafür nachzubauen hieße, `LivingEntity.hurt` zu ersetzen; das steht in keinem
  Verhältnis. Fall 3 aus R18.

## R20 — Querschnittsfragen aus W04

- **`canTriggerWalking`.** Entities, deren Original `canTriggerWalking()` mit
  `false` überschreibt (zuerst `Elevator`, das Hoverboard, Elevator.java:136),
  implementieren eine Marker-Schnittstelle `NoStepTrigger`; `Legacy.wasWalking`
  prüft sie. So lösen sie wie im Original keine `stepOn`-Wirkung aus
  (Titan-/Uranerz-Glühen, Rubin-Stärke). Kein Access Transformer: die geschützte
  `getMovementEmission()` wäre der allgemeinere Weg, aber R1 hält die Plattform
  schlank, und die Schnittstelle folgt dem Muster der MyUtils-Marker. Jede
  spätere Welle prüft ihre Entities auf dieses Override.
- **Verzauberbarkeit folgt der 1.7.10-Basisklasse.** 1.7.10 entschied über
  `EnumEnchantmentType`, 1.21.1 über die Tags `#minecraft:enchantable/*`
  (Pfade belegt in `neoforge-21.1.248-client-extra-aka-minecraft-resources.jar`).
  Zuordnung: `ItemSword`-Unterklassen → `sword`, `sharp_weapon`, `weapon`,
  `fire_aspect`, `durability`, `vanishing`; `ItemTool`-Unterklassen →
  `mining`, `mining_loot`, `durability`, `vanishing`; `ItemArmor` → der
  Rüstungs-Tag des Slots plus `armor`, `durability`, `equippable`, `vanishing`;
  `ItemBow` → `bow`, `durability`, `vanishing`; `ItemFishingRod` → `fishing`,
  `durability`, `vanishing`; ein `Item` mit Haltbarkeit, aber anderer Basis
  (UltimateBow, SkateBow, UltimateFishingRod, ItemSifter erben `Item`) →
  nur `durability` und `vanishing`, weil 1.7.10 ihnen am Tisch nichts anbot.
  Die genaue Tag-Liste vor dem Schreiben im Client-Extra-Jar nachsehen.
- **Ganzzahl-Casts auf Koordinaten werden `Mth.floor`.** 1.7.10 kannte keine
  negativen Y-Werte, also waren `(int) posY` und `floor(posY)` dort gleich. In
  1.21.1 liest `(int)` unterhalb von Y 0 die falsche Blockzeile. Wo das Original
  eine **Koordinate** in eine Blockposition castet, schreibt der Port
  `Mth.floor`; X und Z ebenso, weil auch dort negative Werte die Zeile
  verschieben. Ausnahme bleibt, was R18 ausdrücklich 1:1 hält (die `(int)`-Casts
  im Teleporter und in den Kreisen von GenericDungeon — dort ist der Versatz
  Teil des Originalverhaltens, auch im Negativen). Fall 3 aus R18.

## R22 — Blocklisten aus 1.7.10 in einer 1.21.1-Welt (Spieltest des Nutzers, 2026-09-13)

**Anlass:** Der Miner's Dream ließ im Spiel Andesit, Diorit und Granit stehen.
Die Blockliste des Originals (`Blocks.stone`, Erde, Kies …) war 1.7.10 vollständig,
weil es diese Steine erst ab 1.8 gibt. In einer 1.21.1-Welt ist sie es nicht mehr.
Das ist keine Originaltreue, sondern ein Loch: das Original wollte „Gestein weg",
nicht „nur der Steinblock von 2014 weg".

- **Regel:** Wo das Original eine feste Liste von Vanilla-Blöcken für eine
  *Kategorie* benutzt (Gestein, Erde, Sand, Holz, Blätter, Erze, Pflanzen …),
  deckt der Port die ganze 1.21.1-Kategorie ab, möglichst über Tags
  (`#minecraft:base_stone_overworld`, `#minecraft:dirt`, `#minecraft:logs`,
  `#c:ores` …). Einzelne, benannte Blöcke (z. B. „nur Obsidian") bleiben einzeln.
  Diese Regel ersetzt die engere Zeile „Erze und Wurm-Blockliste im
  Tiefenschiefer" aus R18.
- **Miner's Dream konkret (Nutzerwunsch):** Der Tunnel entfernt **jeden** Block
  außer `#c:ores`. Drei Ausnahmen, damit nichts Unwiederbringliches verschwindet:
  unzerstörbare Blöcke (`destroySpeed < 0`, z. B. Grundgestein), Blöcke mit
  Block-Entity (Truhen, Spawner — sonst ist der Inhalt still weg) und
  Flüssigkeiten behandelt er wie das Original (Dach/Wand gegen Nachfließen).
  Die Fackeln und das Dach aus dem Original bleiben. Fackelboden: jede Oberseite,
  die `isFaceSturdy(UP)` erfüllt.
- **Tags so, wie 1.21.1 sie liefert (Nachtrag nach der Bug-Suche, 2026-09-14).**
  `#minecraft:dirt` enthält Grasblock, Mycelium, Moos, Mud und Rooted Dirt. Wo das
  Original „Erde" als Kategorie meinte, gilt der ganze Tag, Mycelium eingeschlossen.
  Ausnahme nur, wo der Originalcode für Gras oder einen der Blöcke einen **eigenen
  Zweig** hat (z. B. die Grastabelle des Sifters) — dann bleibt dieser Block im
  eigenen Zweig und wird aus der Erdmenge herausgenommen.
- **Fackeln sind eine Kategorie.** `SOUL_TORCH`/`SOUL_WALL_TORCH` gehören zu
  „Fackel" (gleiche Klassen); Redstone-Fackeln nicht (andere Klasse, im Original
  bewusst ausgelassen). Betrifft `Alien.isTorch`.
- **Kleine Blumen sind eine Kategorie** (`#minecraft:small_flowers`); wo das
  Original Löwenzahn (`yellow_flower`) getrennt behandelt, bleibt er getrennt.
- **Besitzer-Todesnachricht zahmer OreSpawn-Tiere: 1:1 aus.** 1.7.10 schickte sie
  nicht; `TamableAnimal.die` in 1.21.1 schon. Jede zahme OreSpawn-Entity
  überschreibt `die` ohne die Nachricht.
- **Brennwert:** Jeder Block mit `Material.wood` im Original bekommt 300 Ticks in
  der `furnace_fuels`-Data-Map (nicht über `#minecraft:planks`, siehe R18).
- **Ausnahme zu R20 (`Mth.floor`):** Island/IslandToo kürzen X/Z mit `(int)` samt
  `xoff`/`zoff`. Die Kürzung ist Teil der Inselgeometrie (Ausrichtung am
  Blockraster je Bewegungsschritt) und bleibt 1:1, wie die Casts im Teleporter.
- **Rückkehr in eine Vanilla-Dimension:** Die Zielsuche beginnt bei
  `WORLD_SURFACE` + 1 der Spalte, gedeckelt auf die Bauhöhe; die wörtliche 180
  gilt nur beim Ziel in einer der sechs OreSpawn-Dimensionen (R21).

## R23 — Aus W12 (2026-09-14)

- **`getCanSpawnHere` bei der Chunk-Generierung: 1:1 wie 1.7.10.**
  `SpawnerAnimals.performWorldGenSpawning` rief laut Bytecode des Vanilla-Jars
  weder `getCanSpawnHere` noch Kollisionsprüfungen; es prüfte nur den Blocktyp am
  Boden. Für OreSpawn-Typen gilt bei `MobSpawnType.CHUNK_GENERATION` deshalb
  keine Placement-Prädikate und kein `checkSpawnRules`/`checkSpawnObstruction`
  (`world/spawn/ChunkGenerationSpawns`). Ein Forge-1.7.10-Patch an dieser Stelle
  ist nicht belegt; findet sich einer, wird die Regel angepasst.
- **Strukturschreiber ohne Nähte vor W13.** `StructureWriter` beantwortet
  Lesezugriffe und Truhenfüllung abhängig vom Clip-Fenster des aktuellen Chunks.
  Baut ein Generator in jedem Chunk-Lauf neu und verzweigt über gelesene Blöcke
  oder würfelt Truhen, entstehen an Chunkgrenzen Nähte. Vor GenericDungeon wird der
  Schreiber so umgestellt, dass jeder Lauf dieselbe Folge von Lese- und
  Zufallsergebnissen sieht (ein vollständiger Durchlauf je Struktur mit
  gespeichertem Schreibprotokoll, das die Chunks nur noch clippen).

## R24 — Reihenfolge: OreSpawn-Strukturen nach der Dekoration (aus W13, 2026-09-14)

In 1.7.10 lief `OreSpawnWorld.generate` (samt GenericDungeon, King/Queen-Altar,
Bäumen) als FML-`IWorldGenerator` **nach** dem Populate des Chunk-Providers. Deshalb
überschreiben die Strukturen die Dekoration, nicht umgekehrt. Im Port sollen
Dungeons nicht von Bäumen, Erzen, Blumen und Dorfteilen durchlöchert werden.

- Alle `orespawn:legacy`-Strukturen und die OreSpawnWorld-Features werden **zuletzt**
  platziert: in den sechs eigenen Dimensionen läuft das Legacy-Populate des
  Generators **vor** `super.applyBiomeDecoration`; ihre Structure Sets stehen auf
  dem spätesten Schritt (`top_layer_modification`), damit sie auch nach den
  Vanilla-Features der Oberwelt kommen.
- Innerhalb der OreSpawnWorld-Aufrufe gilt die Originalreihenfolge von
  `OreSpawnWorld.generate` (nicht die alphabetische der Set-Namen).
- Der GameTest, der erzeugte Welt und Aufzeichnung vergleicht, ist mit dieser
  Reihenfolge streng: jede fehlende aufgezeichnete Blockschreibung ist ein Fehler.
- Umsetzung: `LegacyStructurePass` schreibt am Ende jeder Chunk-Dekoration die
  Legacy-Stücke im 3×3-Feld erneut. **Überlappen zwei Strukturen** aus
  verschiedenen Start-Chunks (im End häufig, weil `end_dungeon` wie 1.7.10 ohne
  Sperre startet), gewinnt fest: Reihenfolge von `OreSpawnWorld.generate`, dann
  Start-Chunk. 1.7.10 hing an der Populate-Reihenfolge, die nicht deterministisch
  war; eine feste Regel ist die ehrlichere Nachbildung. Angenommen.

## R26 — Abschluss-Bugsuche (BUGHUNT2, 2026-09-14)

- **Biome nach R22.** `hell` = `#minecraft:is_nether`; `ocean` = ocean, lukewarm, warm,
  cold; `deepOcean` = deep_ocean, deep_lukewarm, deep_cold; `swampland` = swamp und
  mangrove_swamp; `jungle`/`jungleHills` = jungle und bamboo_jungle. Frozen-Ozeane
  bleiben draußen, die gab es schon in 1.7.10 ohne OreSpawn-Spawns. Das gilt für die
  Spawn-Tabelle **und** die `legacy_name`-Tags, und zwar im Generator.
- **Nether Tracker bleibt bei Netherrack** (R22-Ausnahme „einzelner, benannter
  Block"). 1:1, keine Erweiterung.
- **Babys:** natürliche Spawns von OreSpawn-Tieren sind nie Babys, wie in 1.7.10.
- **Velocity-Raptor-Tempo** ist flüchtig wie in 1.7.10: der Modifier fällt beim
  nächsten Wechsel von `isSprinting` weg.
- **King/Queen-Y-Deckel:** 230 in den OreSpawn-Dimensionen, sonst
  `getMaxBuildHeight() - 26`.
- **Chunk-Cache in `getBaseColumn`** ist erlaubt: reine Beschleunigung, gleiche Blöcke.
- **Sky Tree:** der abgebaute Block mit `UPDATE_ALL`, die rekursiven Stammblöcke still
  (`UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`). Die Krone schwebt wie im Original.

## R25 — Bosse (aus W10, 2026-09-14)

- **Keine Vanilla-Bossleiste.** 1.7.10 zeigte King, Queen und Mobzilla nicht über
  `IBossDisplayData`, sondern über das eigene Lebensbalken-Overlay
  (`GirlfriendOverlayGui`, W11). Der Port hält das 1:1: kein `ServerBossEvent`,
  der Balken kommt mit W11 und liest das Lebensverhältnis (unter R4 unverändert).
- **Köpfe einheitlich.** Was für `KingHead` gilt (Position per `setPos`,
  Blitzschlag wirkungslos wie in 1.7.10 wegen Feuerimmunität, R22-Kategorien in
  `MyCanSee` für Laub und Pferdearten), gilt genauso für `QueenHead` und
  `GodzillaHead`.

## R21 — Dimensionen: eine Linie für alle sechs (offene Punkte aus W05)

- **Ein Weltseed-Pfad.** Alle sechs Generatoren nehmen den Weltseed aus
  `createState` — dem Wert, den 1.7.10 als `worldObj.getSeed()` sah. Crystal und
  Chaos leiten ihn bisher über `RandomState` ab (`LegacyChunk.legacyWorldSeed`);
  deterministisch, aber ein anderer Wert als bei den übrigen vier, also nicht
  derselbe Seed wie im Original. Wird angeglichen.
- **Dekoration als portierter 1.7.10-Java-Code in allen eigenen Dimensionen.**
  Utopia und VillageMania portieren `BiomeDecorator` und die `WorldGen*`-Klassen
  zeilengetreu; Mining nutzt bisher Vanilla-Placed-Features. Vanilla-Features
  haben andere Zufallsfolgen, Dichten und Formen — das ist nicht OreSpawn.
  Mining wird auf den Java-Decorator umgestellt.
- **Keine doppelten 1.7.10-Hilfsklassen.** `BiomeDecorator`, `WorldGenMinable`,
  `WorldGenLakes`, `LegacyChunk` und das Rauschen liegen genau einmal, in
  `world/gen/`; die Kopien unter `world/dimension/utopia/legacy` und
  `world/dimension/crystal` werden zusammengeführt. Zwei Kopien laufen sonst beim
  nächsten Fix auseinander.
- **Wörtliche Y-Werte bleiben in den eigenen Dimensionen.** Die
  Heightmap-Regel aus R18 („Oberflächensuche per `WORLD_SURFACE_WG`") gilt für die
  Oberwelt und andere Vanilla-Welten, deren Gelände bis Y 320 reicht. In den
  sechs OreSpawn-Dimensionen (Y 0–256, Gelände vom portierten Generator) sind die
  Original-Scans ab Y 128/120 exakt richtig und bleiben.
- **Äxte gehören in `#minecraft:enchantable/sharp_weapon`.** Das ist in 1.21.1
  die Liste für den Amboss, nicht für den Tisch (der Tisch nimmt `sword`). 1.7.10
  erlaubte Schärfe auf Äxten am Amboss (`EnchantmentDamage.canApply`:
  `ItemAxe` → `true`) — also 1:1. Ergänzt R20.
- **Weit reichende Dekoration wird geprüft, nicht vermutet.** Die Sorge, dass
  `ChunkProviderOreSpawn2.applyBiomeDecoration` in Chunk x+2/z+2 schreibt, wird
  durch einen GameTest entschieden, der dekorierte Chunks in einem 5×5-Feld
  erzeugt und `Detected setBlock in a far chunk` zählt; bei einem Treffer wird
  das Populate-Fenster wie in Utopia auf Chunk C−(1,1) verschoben.
