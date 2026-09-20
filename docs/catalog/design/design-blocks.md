# Design: design-blocks

Alle 211 Block-Einträge aus `manifest.json` (210 eindeutige Ids; `crystalfurnace` steht zweimal, einmal je Brennzustand — manifest `problems`). Die Bildsprache ist durchgehend 16×16-Pixelkunst mit vier klar getrennten Welten: **Overworld-Erze** sind Vanilla-Stein mit farbigen Einsprengseln (gelb = Uran, blau = Titan, rot = Rubin, violett = Amethyst, weiß = Salz). Die **Kristalldimension** hat eine eigene Signatur: ein blauer Rahmen mit cyanfarbenem Diagonalgitter auf transparentem Grund (`crystalstone`, `crystalgrass_*`, `crystaltermite_*`, `crystalfurnace_*`), dazu freistehende Kreuz-Kristalle (`crystalcoal`, `crystalcrystal`, `tigerseye`) und ein cremeweißes Holz (`crystaltreelog`, `crystalplanks`, `crystalworkbench_*`). Die **Ancient Dried Spawn Eggs** sind 119 Varianten eines einzigen Musters — Steinwürfel mit einem farbigen Mob-Gekritzel; zwölf davon tragen statt Stein den blauen Kristallrahmen. **Pflanzen** sind Kreuz-Sprites (Sugar-Cane-Renderer) oder Vanilla-Crops mit vier Wachstumstexturen. Fast alles, was leuchtet oder lebt, streut außerdem Vanilla-Partikel (`flame`, `smoke`, `reddust`, `fireworksSpark`, `happyVillager`); die Partikel sind Teil des Looks und stehen bei jeder Familie.

Konventionen in diesem Dokument: Zeilenangaben `(Klasse.java:N)` zeigen auf `reference/src-20.2/src/main/java/danger/orespawn/`; `(manifest)` auf `docs/catalog/manifest.json`; „gemessen" = aus dem PNG unter `reference/jar/extracted/assets/orespawn/textures/blocks/` gezählt. Lichtwerte: 1.7.10 speichert `setLightLevel(f)` als `(int)(15.0F * f)` (`Block.func_149715_a`), die Tabelle nennt beides. Neue Texturpfade sind `assets/orespawn/textures/block/<name>.png` in Kleinschreibung (manifest `texture_map`); nur `oreMOTHRA.png` → `oremothra.png` ändert tatsächlich den Namen.

Zwei Client-Flags steuern das Aussehen mehrerer Familien und kommen **nicht** aus der Config: `OreSpawnMain.FastGraphicsLeaves` wird jedes Frame aus `mc.gameSettings.fancyGraphics` gesetzt (GirlfriendOverlayGui.java:46-51), `OreSpawnMain.current_dimension` aus der Spielerdimension (GirlfriendOverlayGui.java:45). Beide sind statische Felder, also auf einem dedizierten Server immer 0 — die Server-Seite sieht damit alle „Fast-Graphics"-Blöcke als nicht-opak und Kyanite/Crystal Grass nie als opak.

## Render-Typen der Oberklassen (1.7.10)

| 1.7.10 | `getRenderType()` | Aussehen | 1.21.1-Modell |
|---|---|---|---|
| `Block` (ohne Override) | 0 | Würfel | `cube_all` / `cube_bottom_top` / `orientable` |
| `BlockReed`, `MyBlockFlower`, `OreCrystal`, `OreCrystalCrystal`, `BlockCrystalLeaves` (fancy) | 1 | zwei gekreuzte Flächen | `minecraft:block/cross` |
| `BlockTorch` | 2 | Fackel | `template_torch` + `template_torch_wall` |
| `BlockCrops` | 6 | vier versetzte Flächen (`#`) | `minecraft:block/crop` |
| `BlockLeaves` | 0 | Würfel, Foliage-Tint | `leaves` (cube_all + BlockColors) |
| `BlockRotatedPillar` | 0 | Säule mit Achse | `cube_column` + `axis` |

Die Override-Stellen: MyBlockFlower.java:88-90, OreCrystal.java:49-51, OreCrystalCrystal.java:43-45, BlockCrystalLeaves.java:90-95. Alles andere erbt.

## Blockstates und Modell-JSON, die der Port braucht

| Form | Blöcke | Property |
|---|---|---|
| `cube_all` | Erze, Speicherblöcke, 121 Eier, `lavafoam`, `blockteleport`, `moledirt`, `skytreelog`, `duplicatortreelog`, `crystalplanks`, `crystalpink_block`, `tigerseye_block`, `crystalstone`, `crystalrat`, `crystalfairy`, `redanttroll`, `termitetroll`, 5 Laubarten + 3 Kristall-Laub (fast) | — |
| `cube_bottom_top` (top/bottom/side) | `crystalgrass`, 5 Ameisennester (`antnest_*`), `crystaltermiteblock` (`crystaltermite_*`) | — |
| `cube_column` | `crystaltreelog` | `axis` |
| `orientable` + lit | `crystalfurnace` | `facing`, `lit` |
| eigenes Cube mit 6 Face-Texturen | `crystalworkbench` | — (keine Ausrichtung im Original) |
| `cross` | 16 Stufenpflanzen (corn/quinoa/tomato/lettuce ×4), 4 Sämlinge, 8 Blumen, 4 Spawner, `crystalcoal`, `crystalcrystal`, `tigerseye`, 3 Kristall-Laub (fancy) | Stufen sind eigene Blöcke, keine Property |
| `crop` (4 Stufenmodelle) | 7 `BlockCrops` | `age` 0–7 → Textur `_0`/`_1`/`_2`/`_3` |
| `template_torch` / `_wall` | 4 Fackeln | `facing` (Wand) |
| Kuchen-Modelle (6 Bissstufen) | `pizza`, `ducttape` | `bites` 0–5 |

Kein Block braucht einen `BlockEntityRenderer`. Der einzige `BlockEntity` ist `TileEntityCrystalFurnace` (Inventar + Brennzeit), und er zeichnet nichts.

---

### Erze (Overworld)

| id | name | texture | look |
|---|---|---|---|
| `oreuranium` | Uranium Ore | `oreuranium` | Vanilla-Stein mit sattgelben Klumpen (wie Golderz, aber kälteres Gelb) |
| `oretitanium` | Titanium Ore | `oretitanium` | Stein mit leuchtend blauen, kantigen Einsprengseln (Lapis-Anmutung, größer) |
| `oresalt` | Salt Ore | `oresalt` | Stein mit großen, fast reinweißen Flecken — deutlich heller als jedes Vanilla-Erz |
| `oreruby` | Ruby Ore | `oreruby` | Stein mit roten Splittern, denen weiße Glanzpunkte aufsitzen |
| `oreamethyst` | Amethyst Ore | `oreamethyst` | Stein mit violetten Splittern gleicher Form wie Ruby Ore |

Render: opake `cube_all`, keine Alpha (gemessen: 0 transparente Pixel). Alle `Material.rock`, Creative-Tab `tabBlock`.

| id | Härte / Resistenz | Besonderheit |
|---|---|---|
| `oreuranium` | 10 / 1 (OreUranium.java:22-23) | Klick, Betreten oder Rechtsklick lösen `glow()` aus: 10 Display-Ticks lang `reddust`-Partikel an jeder freien Seite (OreUranium.java:33-52, 58-68, 70-99). XP 5+r(5)+r(10) nur unter Y 40 (OreUranium.java:101-107) |
| `oretitanium` | 15 / 5 (OreTitanium.java:22-23) | wie Uran, aber 5 Ticks Glühen (OreTitanium.java:49-50) |
| `oresalt` | 5 / 2 (OreSalt.java:16-17) | jede `EntityAnt` erhält beim Berühren 5 Kaktus-Schaden (OreSalt.java:21-31) — Salz als Ameisenzaun |
| `oreruby` | 10 / 4 (OreRuby.java:16-17) | droppt `MyRuby` ×1, mit Fortune 1–2, XP 5+r(5)+r(5) (OreRuby.java:21-37) |
| `oreamethyst` | 10 / 4 (OreAmethyst.java:16-17) | identisch zu Ruby mit `MyAmethyst` (OreAmethyst.java:21-37) |

Quirk, den der Port nicht 1:1 übernehmen sollte: `glowing`/`glowcount` sind Felder des Block-Singletons (OreUranium.java:15-16), ein Treffer lässt also *alle* Uranerze im Sichtfeld funkeln. In 1.21.1 gehört das in einen kurzen `BlockState`-Timer oder einen clientseitigen Partikel-Burst beim Event, ohne globalen Zustand.

Port: `cube_all`, `DropExperienceBlock`-artiges Verhalten mit der Y<40-Regel im Loot-Handler; Partikel im `animateTick` (client) — der Auslöser (Interaktion) kommt vom Server, also Partikel per `level.addParticle` aus einem Server-Event nur mit Netzwerkpaket; einfachere Wahl: beim Betreten/Klicken ein `ParticleOptions`-Paket senden. `oresalt`-Schaden über `stepOn`/`entityInside` gegen `EntityAnt`-Nachfolger.

### Kristalldimension

| id | name | texture | look |
|---|---|---|---|
| `crystalstone` | Kyanite | `crystalstone` | blauer Rand, cyanfarbenes Diagonalkreuz, Rest transparent (174 px, gemessen) — das Grundgestein der Dimension |
| `crystalcoal` | Crystal Energy | `crystalcoal` | freistehendes orange-gelbes Kreuz mit rotem Kern, 236 px transparent |
| `crystalcrystal` | Pink Tourmaline | `crystalcrystal` | magentafarbenes Balkenkreuz auf transparentem Grund |
| `tigerseye` | Tiger's Eye | `tigerseye` | gold-braunes Balkenkreuz, gleiche Form wie Pink Tourmaline |
| `crystalrat` | Crystalized Rats | `crystalrat` | Kyanite-Gitter mit heller Zickzack-Spur in der Mitte |
| `crystalfairy` | Crystalized Fairies | `crystalfairy` | Kyanite-Gitter mit orange-gelbem Funken im Zentrum |
| `redanttroll` | Red Ant Troll Block | `redanttroll` | Vanilla-Stein mit cyanen Splittern — sieht absichtlich wie Diamanterz aus |
| `termitetroll` | Termite Troll Block | `termitetroll` | Stein mit grünen Splittern — sieht wie Smaragderz aus |
| `crystalgrass` | Crystal Grass | `crystalgrass_top` / `_side` / `_bottom` | oben grünes Gitter, seitlich und unten blaues Gitter, jeweils transparent |
| `crystalplanks` | Crystal Planks | `crystalplanks` | cremefarbene, schräg gesetzte Bretter auf transparentem Grund |
| `crystalpink_block` | Pink Tourmaline Block | `crystalpink_block` | weißer Rahmen, magentafarbenes Sternkreuz (8 Strahlen), transparente Felder |
| `tigerseye_block` | Tiger's Eye Block | `tigerseye_block` | derselbe Sternrahmen in Braun-Gold |

Werte und Verhalten:

| id | Klasse | Härte / Res. | Licht | Render |
|---|---|---|---|---|
| `crystalstone` | `OreBasicStone` | 2.0 / 10.0 (manifest ctor) | 0 | Würfel; opak **nur** wenn `current_dimension == DimensionID5` (OreBasicStone.java:50-56) |
| `crystalrat`, `crystalfairy`, `redanttroll`, `termitetroll` | `OreBasicStone` | 2.5 / 14.0 (manifest ctor) | 0 | wie oben; beim Abbau spawnen 1+r(10) `Rat`, 1+r(6) `Fairy`, 15+r(6) `Red Ant`, 15+r(6) `Termite` (OreBasicStone.java:22-44) |
| `crystalcoal` | `OreCrystal` | 6.0 / 20.0 (manifest ctor) | 0.6 → 9 | **Kreuz** (OreCrystal.java:49-51); 1/5 Display-Tick 5 Partikel `flame`/`smoke`/`reddust` aus der Mitte (24-47); beim Abbau 1/3 Explosion Stärke 1.5 mit Feuer (61-66); XP 5+r(5)+r(10) unter Y 40 (68-74) |
| `crystalcrystal` | `OreCrystalCrystal` | 12.0 / 40.0 (manifest ctor) | 0.4 → 6 | Kreuz (43-45); 1/20 Tick ein `fireworksSpark` (24-40); 1/10 Explosion Stärke 1.0 (55-60) |
| `tigerseye` | `OreCrystalCrystal` | 15.0 / 60.0 (manifest ctor) | 0.5 → 7 | Kreuz; 1/20 Tick ein `flame`; droppt r(2), also 0 oder 1 (70-75) |
| `crystalgrass` | `CrystalGrass` | 0.6 / 2.0 (manifest ctor) | 0 | `cube_bottom_top`; opak nur in Dim 5 (62-68); trägt jede Pflanze (`canSustainPlant` true, 58-60) |
| `crystalplanks` | `CrystalWood` | 1.5 / 4.0 (manifest ctor) | 0 | Würfel, nie opak (25-31) |
| `crystalpink_block`, `tigerseye_block` | `BlockCrystal` | 4.0 / 4.0 (BlockCrystal.java:13-14) | 0.4 → 6 | Würfel, nie opak, `renderAsNormalBlock` false (19-25) |

Der Dimensionsschalter ist der wichtigste Design-Fakt dieser Familie: außerhalb der Kristalldimension sind Kyanite und Crystal Grass durchsichtige Gitterwürfel, durch die man die Nachbarflächen sieht; drinnen sind sie opak, die Nachbarflächen werden weggeschnitten, und durch die Löcher blickt man auf Himmel und Nebel — das Ganze wirkt wie ein ätherisches Gerüst. `OreSpawnMain.current_dimension` ist ein statisches Client-Feld (GirlfriendOverlayGui.java:45), auf dem Server immer 0.

Port: 1.21.1 kennt keine dimensionsabhängige Opazität — `noOcclusion()` ist eine Blockeigenschaft. Zwei saubere Wege: (a) immer `noOcclusion()` + `RenderType.cutout` (entspricht dem Look außerhalb der Dimension, verliert den Himmel-durch-die-Löcher-Effekt), (b) opak lassen und `RenderType.cutout` — dann sieht man wie im Original in Dim 5 durch die Löcher, überall sonst aber auch. Entscheidung offen; Empfehlung (a), weil (b) außerhalb der Dimension X-Ray-Löcher zeigt. Kreuz-Erze: `cross`-Modell, `noOcclusion()`, Explosion in `playerDestroy`/`onRemove`, Partikel im `animateTick`. `crystalgrass`: `cube_bottom_top`, `mayPlaceOn`-Regeln der Pflanzen erweitern (Quinoa, Kristallsämlinge, Blumen prüfen explizit auf `CrystalGrass`).

### Speicherblöcke

| id | name | texture | look |
|---|---|---|---|
| `blockuranium` | Uranium Block | `blockuranium` | sattgelber, fast glatter Würfel mit hellerem Zentrum |
| `blocktitanium` | Titanium Block | `blocktitanium` | hellcyaner Würfel mit weißem Kern, wirkt wie Eis mit Licht dahinter |
| `blockruby` | Ruby Block | `blockruby` | einfarbig sattrot mit einem einzelnen weißen Glanzpixel oben links |
| `blockamethyst` | Amethyst Block | `blockamethyst` | violett mit hellem Diagonalglanz, Schliffkanten |
| `blockmobzillascale` | Mobzilla Scale Block | `blockmobzillascale` | fast schwarz mit violetten Linien, wie Obsidian mit Schuppenmuster |

| id | Klasse | Härte / Res. | Licht | Verhalten |
|---|---|---|---|---|
| `blockuranium` | `BlockUranium` | 5 / 5 (BlockUranium.java:15-16) | 0.2 → 3 (18) | 1/20 Display-Tick sechs Partikel (`flame`/`smoke`/`reddust`, je 1/3) an freien Seiten (26-70) |
| `blocktitanium` | `BlockTitanium` | 5 / 5 (BlockTitanium.java:15-16) | 0.5 → 7 (18) | wie Uran (26-70) |
| `blockruby`, `blockamethyst` | `BlockRuby` | 4 / 4 (BlockRuby.java:16-17) | 0.4 → 6 (19) | `isOpaqueCube` false, `renderAsNormalBlock` true (22-28) — Nachbarflächen werden nicht gecullt, sichtbar ist das nicht (0 transparente Pixel) |
| `blockmobzillascale` | `BlockRuby` | wie oben | 0.4 → 6 | Berühren oder Betreten gibt `damageBoost` (Stärke I) 200 Ticks (30-40) |

Port: `cube_all`, `lightLevel(state -> n)`, Partikel im `animateTick`; `blockmobzillascale` über `entityInside`/`stepOn`. Alle drei `BlockRuby`-Blöcke sollten opak bleiben (die Nicht-Opazität des Originals ist ein wirkungsloser Rest).

### Pflanzen und Crops mit Wachstumsstufen

**Crops (`BlockCrops`, 7 Blöcke, Renderer 6):** Vier Texturen `_0`…`_3` je Pflanze; die Metadaten 0–7 werden so abgebildet (BlockStrawberry.java:17-25, identisch in allen sieben): meta 0–1 → `_0`, 2–3 → `_1`, 4–6 → `_2`, 7 → `_3`. Vanilla-Bounds von `BlockCrops` (1 × 0.25 × 1).

| id | name | texture | look |
|---|---|---|---|
| `strawberry_plant` | — (Item „Strawberry Plant") | `strawberry_0..3` | zwei grüne Halme → buschig → weiße Blüten → rote Beeren mit gelben Punkten |
| `radish_plant` | — (Item „Radish") | `radish_0..3` | dünne Sprossen, zuletzt kräftige Blätter über rot-weißem Wurzelansatz |
| `rice_plant` | — (Item „Rice") | `rice_0..3` | schmale Grashalme, Stufe 3 mit weißen Rispen |
| `butterfly_plant` | — (Item „Butterfly Plant") | `butterfly_0..3` | blaugrüne Stängel mit gelb-blauen Schmetterlingen, die mit jeder Stufe mehr werden |
| `moth_plant` | — (Item „Moth Plant") | `moth_0..3` | dunkelgraue Stängel mit schwarz-grauen Faltern |
| `mosquito_plant` | — (Item „Mosquito Plant") | `mosquito_0..3` | dunkelrote, dünne Stängel mit schwarzen Mücken |
| `firefly_plant` | — (Item „Firefly Plant") | `firefly_0..3` | grüne Pflanze mit gelben Leuchtpunkten, Stufe 3 dicht besetzt |

Die Blöcke haben keinen `addNameForObject` (manifest `name: null`); der sichtbare Name ist der des Saatguts (`lang_dump.txt`: `MyStrawberrySeed` „Strawberry Plant" usw.).

| id | Saat / Ernte | Menge | Spawn-Verhalten |
|---|---|---|---|
| `strawberry_plant` | `MyStrawberrySeed` / `MyStrawberry` (BlockStrawberry.java:31-37) | 1+r(5) (28) | — |
| `radish_plant` | `MyRadish` / `MyRadish` (BlockRadish.java:31-37) | 2+r(4) (28) | — |
| `rice_plant` | `MyRice` / `MyRice` (BlockRice.java:31-37) | 2+r(4) | — |
| `butterfly_plant` | `MyButterflySeed` / — (BlockButterflyPlant.java:68-74) | 1+r(5) | am Tag, ohne Regen, Luft darüber, Chance 1/(7−meta): ein `Butterfly` (22-40) |
| `moth_plant` | `MyMothSeed` / — | 1+r(5) | nachts: ein `Moth` (BlockMothPlant.java:22-40) |
| `mosquito_plant` | `MyMosquitoSeed` / — | 1+r(5) | Tag und Nacht, auch im Regen: 2+r(5) `Mosquito` (BlockMosquitoPlant.java:22-42) |
| `firefly_plant` | `MyFireflySeed` / — | 1+r(5) | nachts, Chance 1/(6−meta): 2+r(5) `Firefly` (BlockFireflyPlant.java:22-43) |

Alle vier Spawner-Crops respektieren `ButterflyEnable`/`MothEnable`/`MosquitoEnable`/`FireflyEnable` (config_dump, Default 1).

**Stufenpflanzen (`BlockReed`, 16 Blöcke, Renderer 1):** jede Stufe ist ein eigener Block, Bounds 0.125–0.875 × 0–1 × 0.125–0.875 (BlockCorn.java:17-18, BlockQuinoa.java:17-18, BlockTomato.java:17-18, BlockLettuce.java:14-15), Kreuz-Sprite.

| id | name | texture | look |
|---|---|---|---|
| `corn_0` … `corn_3` | Corn Plant | `corn_0..3` | Keimling → breitblättrig → grün mit gelben Kolben → reife gelbe Kolben |
| `quinoa_0` … `quinoa_3` | Quinoa Plant | `quinoa_0..3` | dünner Spross → Rispe → bunte (rot-gelb-violett) Samenstände → vertrocknet-braune Rispe |
| `tomato_0` … `tomato_3` | Tomato Plant | `tomato_0..3` | Spross → grüner Busch → Busch mit gelben Blüten → rote Tomaten |
| `lettuce_0` … `lettuce_3` | Lettuce Plant | `lettuce_0..3` | winziger Spross → kleiner Kopf → mittlerer Kopf → großer hellgrüner Salatkopf |

Wachstum: Corn wächst als Stapel bis `myMaxHeight` = 4+r(4) (BlockCorn.java:39-41), Tomato 3+r(3) (BlockTomato.java:39-41), Quinoa 2+r(3) (BlockQuinoa.java:39-41); nur Stufe 0/1 (Corn, Tomato) bzw. 0/2 (Quinoa) ticken (BlockCorn.java:33-35, BlockQuinoa.java:33-35). Lettuce ist ein einzelner Block und rückt bei meta ≥ 4 eine Stufe vor (BlockLettuce.java:29-46). Ernte nur aus Stufe 3: `MyCornCob` 1+r(2) (BlockCorn.java:91-96), `MyTomato` 2+r(4) (BlockTomato.java:87-92), `MyQuinoa` 3+r(3) (BlockQuinoa.java:85-90), `MyLettuce` 2+r(3) (BlockLettuce.java:53-58). Quinoa ist zusätzlich auf `CrystalGrass` pflanzbar (BlockQuinoa.java:24).

Quirk: die Zielhöhe wird als `myMaxHeight << 8` in die Metadaten geschrieben (BlockCorn.java:59-60, 78). 1.7.10 speichert Block-Metadaten als 4-Bit-Nibble; Zeile 37 liest deshalb immer 0 und würfelt die Höhe bei jedem Tick neu. Beim Port als `IntegerProperty` sauber modellieren, nicht nachbauen.

**Sämlinge (`BlockReed`, 4 Blöcke, Renderer 1):**

| id | name | texture | look |
|---|---|---|---|
| `experiencesapling` | Experience Tree Sapling | `experiencesapling` | grüner Sämling mit gelben Punkten (XP-Farbe) |
| `crystalsapling` | Red Crystal Tree Sapling | `crystalsapling` | weißer Stiel mit roten Kristallblättern |
| `crystalsapling2` | Yellow Crystal Tree Sapling | `crystalsapling2` | grün-gelbe Kristallblätter an hellem Stiel |
| `crystalsapling3` | Blue Crystal Tree Sapling | `crystalsapling3` | blau-weiße Kristallblätter |

`experiencesapling`: 20 `happyVillager` bei 1/20 Display-Tick (BlockExperiencePlant.java:24-31), 1/10 pro Random-Tick → `OreSpawnTrees.ExperienceTree` (33-42). Kristallsämlinge (`BlockCrystalPlant`): auf Gras/Erde/Ackerland/`CrystalGrass` (21-24), 10 `happyVillager` bei 1/30 (26-33), 1/5 pro Tick wächst der Baum (35-52): rot → `TallCrystalTree` (Stamm 10+r(12), Laub 1; 80-139), gelb → `ScragglyCrystalTreeWithBranches` (Laub 2; 185-228), blau → `TallCrystalTreeBlue` (Stamm 5+r(6), Laub 3; 230-289). Drop-Bug: `crystalsapling2` droppt `crystalsapling3`, weil das Ergebnis in Zeile 59 verworfen wird (BlockCrystalPlant.java:54-62).

**Blumen (`MyBlockFlower`, 8 Blöcke, Renderer 1):** Bounds 0.3–0.7 × 0–0.6 × 0.3–0.7 (MyBlockFlower.java:20-21), keine Kollision (76-78), Härte 0, Grass-Sound (manifest), auf Gras/Erde/Ackerland/`CrystalGrass` (33-35).

| id | name | texture | look |
|---|---|---|---|
| `flower_pink` | Pink Flower | `flower_pink` | einzelne magentafarbene Tulpenblüte an grünem Stiel |
| `flower_blue` | Blue Flower | `flower_blue` | cyanblaue Blüte, gleiche Form |
| `flower_black` | Black Flower | `flower_black` | schwarz-graue Blüte, gleiche Form |
| `flower_scary` | Dead Flower | `flower_scary` | kahles, braun-schwarzes Zweiggestrüpp ohne Blüte |
| `crystalflower_red` | Red Crystal Flower | `crystalflower_red` | roter Kristallzweig mit kantigen Blättern |
| `crystalflower_green` | Green Crystal Flower | `crystalflower_green` | grüner Stiel mit weißer Kristallblüte |
| `crystalflower_blue` | Blue Crystal Flower | `crystalflower_blue` | violette Blüte mit blauen Kristallblättern |
| `crystalflower_yellow` | Yellow Crystal Flower | `crystalflower_yellow` | gelber Stiel mit cyanfarbener Blüte |

Tag/Nacht-Wechsel: bei Weltzeit > 12000 wird `flower_pink` → `flower_black` und `flower_blue` → `flower_scary`, tagsüber zurück (MyBlockFlower.java:52-69) — die Blumen „sterben" jede Nacht und erwachen morgens.

Port: `cross`-Modelle, `noOcclusion()`, `RenderType.cutout`. Crops als `CropBlock` mit `age` 0–7 und vier Modellen (`crop`-Parent), Stufenpflanzen als je vier `BushBlock`-Varianten mit Kreuz-Modell oder ein Block mit `stage`-Property (letzteres ist sauberer, ändert aber Registry-Ids — für 1:1 die vier Ids behalten). Blumen als `BushBlock` mit Tag/Nacht-Tick (`randomTick` + `isDaytime`).

### Laub und Stämme

| id | name | texture | look |
|---|---|---|---|
| `leaves_apple` | Apple Tree Leaves | `leaves_apple` (+ `generic_solid`) | graustufiges Vanilla-Laubmuster, wird vom Biom-Foliage-Tint eingefärbt |
| `leaves_cherry` | Cherry Tree Leaves | `leaves_cherry` (+ `generic_solid`, `scary_solid`) | wie Apple, mit einzelnen dunkleren Pixeln |
| `leaves_peach` | Peach Tree Leaves | `leaves_peach` (+ `generic_solid`, `scary_solid`) | wie Apple, etwas grober, mehr Alpha (138 px) |
| `leaves_scary` | Scary Tree Leaves | `leaves_scary` (+ `generic_solid`, `scary_solid`) | schwarzbraunes Zweiggestrüpp auf transparentem Grund (164 px), kaum Blattmasse |
| `leaves_experience` | Experience Tree Leaves | `leaves_experience` (+ `generic_solid`) | Laubmuster mit grünlichem Schleier |
| `crystaltreeleaves` | Crystal Tree Leaves | `crystaltreeleaves` | horizontale rot-orange Kristallstreifen mit weißen Enden, transparent dazwischen |
| `crystaltreeleaves2` | Crystal Tree Leaves | `crystaltreeleaves2` | gelb-grün-blaue Streifen |
| `crystaltreeleaves3` | Crystal Tree Leaves | `crystaltreeleaves3` | grün-blaue Streifen |
| `skytreelog` | Sky Tree Wood | `skytreelog` | dunkle Eichenrinde mit fast schwarzen Furchen |
| `duplicatortreelog` | Duplicator Tree Wood | `duplicatortreelog` | hellere, graubraune Rinde mit dunklen Streifen |
| `crystaltreelog` | Crystal Tree Wood | `crystaltreelog` / `crystaltreelog_top` | senkrechte cremeweiße Stäbe auf transparentem Grund; die Top-Textur ist dieselbe |

Fast Graphics: bei `FastGraphicsLeaves != 0` (Client-Grafik „Schnell", GirlfriendOverlayGui.java:46-51) werden Apple-, Cherry-, Peach- und Experience-Laub opak und zeigen `generic_solid` (grau-grünes, geschlossenes Laubmuster), Scary-Laub zeigt `scary_solid` (dichtes Zweigmuster auf braunem Grund) (BlockAppleLeaves.java:86-108, BlockScaryLeaves.java:86-115, BlockExperienceLeaves.java:112-134). Kristall-Laub wird bei Fast zum opaken Würfel, bei Fancy zum **Kreuz-Sprite** (BlockCrystalLeaves.java:82-95) — Kristallbäume sehen mit Fancy also aus wie aus schwebenden Streifen-Kreuzen gebaut. Farbe: die fünf Vanilla-artigen Laubblöcke erben den Foliage-Tint von `BlockLeaves`; Kristall-Laub setzt fest `14540253` = `0xDDDDDD` (BlockCrystalLeaves.java:104-116), also 87 % Helligkeit.

| id | Härte | Drops / Verhalten |
|---|---|---|
| `leaves_apple` | 0.2 (manifest) | beim Zerfall: Apfel 1/25, Goldapfel 1/500, verzauberter Goldapfel 1/1000, `MagicApple` 1/10000 (BlockAppleLeaves.java:25-40); lässt bei Luft darunter mit 1/20 (Dim 4: 1/100) Früchte fallen (46-65); wird in `DimensionID4` nachts zu Scary Leaves (66-70) |
| `leaves_cherry` | 0.15 (manifest) | `MyCherry` 1/25, Menge r(4) (BlockScaryLeaves.java:27-46) |
| `leaves_peach` | 0.15 (manifest) | `MyPeach` 1/25, Menge r(1) — also nie mehr als 0 aus `quantityDropped` |
| `leaves_scary` | 0.2 (manifest) | keine Drops; wird tagsüber (t < 12000) wieder zu Apple Leaves (61-63) |
| `leaves_experience` | 0.2 (manifest) | kein Item-Drop (27-33); nachts (14000–22000) 1/65 Bottle o' Enchanting über dem Block, 1/75 geworfene `EntityExpBottle` darunter (46-65); `fireworksSpark`-Regen 13000–23000 (76-105) |
| `crystaltreeleaves`(1-3) | 0.2 / 0.25 / 0.25 (manifest) | `MyCrystalApple` 1/100, passender Sämling 1/50 (BlockCrystalLeaves.java:24-41) |
| `skytreelog` | 0.2 (manifest), Wood-Sound | beim Abbau durch Spieler wird der ganze zusammenhängende Stamm rekursiv abgebaut, bis 1000 Rekursionen (BlockSkyTreeLog.java:40-69) |
| `duplicatortreelog` | 0.2 (manifest), tickRandomly, `tickRate` 1 | jeder Random-Tick ruft `OreSpawnTrees.DuplicatorTree`, wenn `DuplicatorTreeEnable` (BlockDuplicatorLog.java:20-31) |
| `crystaltreelog` | 0.2 (manifest), Wood-Sound | `BlockRotatedPillar`, nie opak (BlockCrystalTreeLog.java:37-43), Seiten-/Top-Icon (60-62) |

Alle Laubblöcke: `setLightOpacity(1)`, Grass-Sound (manifest); Zerfallsprüfung sucht Holz im Taxicab-Radius 3 innerhalb ±2 (Dim 4: ±1) (BlockAppleLeaves.java:46-78).

Port: Laub als `LeavesBlock`-Ableitung mit `cutout_mipped`, `BlockColors` → Foliage für die fünf, konstantes `0xDDDDDD` für Kristall-Laub. Die Fast/Fancy-Umschaltung macht 1.21.1 selbst (`ItemBlockRenderTypes` / `LeavesBlock` rendert bei „Schnell" opak); die Texturen `generic_solid`/`scary_solid` würden nur über einen eigenen `BakedModel`-Wrapper erreichbar — **offen**, ob das den Aufwand lohnt; ohne sie zeigt „Schnell" dieselbe Textur opak. Kristall-Laub als `cross`-Modell mit `noOcclusion()` (Fancy-Look) — eine Fast-Variante bräuchte denselben Wrapper. Logs: `cube_all` bzw. `RotatedPillarBlock` + `cube_column`.

### Dried Egg Ores (121 Blöcke, `OreGenericEgg`)

Klasse für alle: `Material.ground`, Härte 0.5, Resistenz 1, Gravel-Sound, `tabBlock` (OreGenericEgg.java:12-18); beim Abbau 50 % Chance auf XP 5+r(3)+r(3) (20-26); opaker Normalwürfel (28-34). Kein Loot-Override — droppt sich selbst. Rehydrierung per Wassereimer zum Spawn-Egg-Item (docs/research/03-items.md, Z. 260/387).

Look-Muster: ein grauer Steinwürfel (Basis `ore.png` — im Jar vorhanden, in keiner Klasse referenziert, `texture_map` führt sie trotzdem) mit einem Mob-Gekritzel in ein bis zwei Farben. Die Farben unten sind aus dem PNG gemessen (gesättigte Pixel, Spreizung > 45; „braun" ist der Orange-Hue der Steinabschattung und steht bei fast jedem Ei). Zwölf Eier (Block-Ids 2950–2961) tragen statt Stein den Kristall-Rahmen mit blau-cyanem Gitter und transparenten Feldern (gemessen 146–170 px Alpha): `oreurchin`, `oreflounder`, `oreskate`, `orerotator`, `orepeacock`, `orefairy`, `oredungeonbeast`, `orevortex`, `orerat`, `orewhale`, `oreirukandji`, `orecrystalcow`. Da die Klasse opak ist, sind die transparenten Felder in 1.7.10 Löcher, durch die man in den Nachbarblock sieht — ein X-Ray-Artefakt, kein Design.

| id | name | texture | look |
|---|---|---|---|
| `blockenderpearl` | Ender-Pearl Block | `blockenderpearl` | türkis-schwarze Enderperlen-Oberfläche, wirbelig, geschlossen |
| `blockeyeofender` | Eye-of-Ender Block | `blockeyeofender` | schwarzer Würfel mit einem großen orange-gelben Enderauge; mit `extremetorch` darauf ruft er ein Cephadrome (BlockExtremeTorch.java:59-107) |
| `orespider` … `orecrab` (119) | Ancient Dried … Spawn Egg | `ore<mob>` | Steinwürfel mit farbigem Mob-Gekritzel; Farbe je Ei in `entries` (gemessen) |

Auffällig starke Motive: `orebasilisc`/`orelizard` (schwarz-grüne Echsensilhouette, 92/100 farbige Px), `orecaterkiller` (162 orange Px, fast ganzflächig), `oreeasterbunny` (grün-orange, 121 Px), `orelargeworm` (117 Px), `orewaterdragon` (blau-rot, 90 Px), `oregoldfish`/`oreblaze` (gelb). Die Vanilla-Mobs (`orebat`, `orezombie`, `oreghast`, `oreenderman`, `oresilverfish`, `orewitherskeleton`) haben nur graue oder schwarze Kritzel (0 gesättigte Px).

Port: 121 × `cube_all`, ein gemeinsames `Block`-Objekt mit `DropExperience`-Logik (50 % × 5+r(3)+r(3)). Für die zwölf Kristall-Eier `noOcclusion()` + `cutout` setzen, damit die Gitter über den Nachbarn statt über dem Void liegen — bewusste Abweichung vom Original-Artefakt, im Kommentar begründen. `oreMOTHRA` → Id `oremothra`, Textur umbenannt (manifest `texture_map`).

### Fackeln (`BlockTorch`, 4 Blöcke)

| id | name | texture | look |
|---|---|---|---|
| `extremetorch` | Extreme Torch | `extremetorch` | Vanilla-Fackelstiel mit rot-gelber Flamme und rotem Funkenkranz |
| `krakenrepellent` | Kraken Repellent | `krakenrepellent` | Stiel mit orangefarbener Kürbis-Kugel, schwarze Augen, rote Flamme obendrauf, zwei weiße Stummel seitlich |
| `creeperrepellent` | Creeper Repellent | `creeperrepellent` | Stiel mit grünem Blattkranz und weiß-roter Flamme |
| `crystaltorch` | Crystal Torch | `crystaltorch` | dünner weißer Kristallstiel, kleine rot-gelbe Flamme; wirkt zerbrechlich |

| id | Licht | Tab | Partikel (Display-Tick) | Verhalten |
|---|---|---|---|---|
| `extremetorch` | 1.0 → 15 (manifest) | `tabRedstone` (BlockExtremeTorch.java:16) | **jeden** Tick `smoke` + `flame` + `reddust` an der Flammenposition (20-51), kein Zufallsfilter | ruft in `randomDisplayTick` erneut `onBlockPlacedBy` (52): steht darunter `MyEyeOfEnderBlock`, wird 100× ein freier Platz ±4 gesucht und dort ein `Cephadrome` gespawnt, die Fackel verschwindet mit Explosionsgeräusch und 16× `smoke`/`explode`/`reddust` (59-107) |
| `krakenrepellent` | 0.8 → 12 | `tabRedstone` | wie oben, Flamme 0.413 höher (24-25) | alle 10 Ticks (53-55, 57-70) schiebt er `Kraken` (Suchbox −20..+20 / −10..+40 / −20..+20, Kraken-Y um 15 versetzt) und `EntityAnt` radial weg, Kraft (20 − Abstand)·0.4 (76-123) |
| `creeperrepellent` | 0.8 → 12 | `tabRedstone` | wie Kraken (20-52) | alle 10 Ticks: `EntityCreeper`, `EntityAnt`, `PurplePower` (außer Typ 10) in ±20/±10/±20 (77-148) |
| `crystaltorch` | 0.99 → 14 | `tabDecorations` (BlockCrystalTorch.java:14) | 1/4 Tick `fireworksSpark` + `flame` (18-47) | hängt auch an Kristallblöcken (`CrystalStone`, `CrystalGrass`, `MyCrystalTreeLog`, `CrystalPlanksBlock`), die keine Vanilla-Solid-Seite melden (50-86) |

Port: pro Fackel zwei Blöcke (`TorchBlock` + `WallTorchBlock`) mit einem `Item` — Vanilla-Muster; `template_torch`/`template_torch_wall`, `cutout`. Die Partikel gehören in `animateTick`. Repellent-Logik als `scheduleTick(10)`-Kette auf dem Server; der Extreme-Torch-Cephadrome-Trigger im Original läuft **clientseitig** aus dem Display-Tick heraus (Zeile 52) und spawnt dennoch serverseitig (`!world.isRemote`, 86) — auf einem dedizierten Server feuert er nie. Port: in `onPlace`/`neighborChanged` serverseitig auslösen, das ist die einzige Variante, die auf einem Server funktioniert.

### Utility-Blöcke

| id | name | texture | look |
|---|---|---|---|
| `lavafoam` | Lava Foam | `lavafoam` | grau-weißer, marmorierter Würfel, wie erstarrter Schaum |
| `blockteleport` | Random Teleport Block | `blockteleport` | fast schwarz mit spärlichen violetten Pixeln — Obsidian-Anmutung |
| `moledirt` | Molenoid Dirt | `moledirt` | Vanilla-Erde, minimal röter |
| `pizza` | Pizza! | `pizza_top` / `_side` / `_bottom` / `_inner` | runde Pizza mit Käse und roten Tomatenscheiben, Rand braun; Unterseite blass; `_inner` zeigt den angebissenen Schnitt mit Belag |
| `ducttape` | Duct Tape! | `ducttape_top` / `_side` / `_bottom` / `_inner` | dunkelgrüne Rolle mit schwarzen Linien, Seiten nur ein flacher grüner Streifen (200 px Alpha) |
| `antblock` | Ant Nest | `antnest_top` / `_side` / `_bottom` | oben grauer Fels mit schwarzem Loch, Seiten und Unterseite Erde; Biom-Grastint |
| `redantblock` | Red Ant Nest | dieselben drei | identisch |
| `termiteblock` | Termite Nest | dieselben drei | identisch |
| `rainbowantblock` | Rainbow Ant Nest | dieselben drei | identisch |
| `unstableantblock` | Unstable Ant Nest | dieselben drei | identisch |
| `crystaltermiteblock` | Crystal Termite Nest | `crystaltermite_top` / `_side` / `_bottom` | Kristallgitter, oben mit orangefarbenem Nestkranz |

| id | Klasse / Material | Härte / Res. | Form | Verhalten |
|---|---|---|---|---|
| `lavafoam` | `Lavafoam`, rock | 5 / 5 (Lavafoam.java:17-18) | Kollision 0.0125 eingerückt (117-120) | `slipperiness` 1.1 (21) — glatter als Eis; Lebewesen werden achsenparallel mit 0.45 vom Zentrum weggeschleudert, Querkomponente ×1.35, ab Geschwindigkeit > 1 Fallschaden in dieser Höhe (72-107); 1/20 Tick Partikel, davon 1/10 `smoke`, 1/10 `reddust` (29-70); XP 5+r(5)+r(5) nur im Nether (109-115) |
| `blockteleport` | `RTPBlock`, rock, Stone-Sound (manifest) | Vanilla-Default | Vollblock | beim Betreten wird ein Spieler ±16 (±r(8)) versetzt, freie Stelle Y±4, bis 1000 Versuche (RTPBlock.java:20-50); 6× `smoke`/`explode`/`reddust` 2.25 über dem Ziel, `random.explode` Pitch 1.5 (58-63) |
| `moledirt` | `MoleDirtBlock`, ground, Gravel-Sound | 0.6 (manifest) | Kollision 1 × 0.875 × 1 (29-32) | bremst Entities auf ×0.3 (34-39); verschwindet beim nächsten Random-Tick (22-27) — Spur des Molenoid |
| `pizza` | `BlockPizza`, `Material.cake` | Vanilla-Default | (1+2·bites)/16 … 15/16 × 0 … 0.25 × 1/16 … 15/16 (27-33) | Links- oder Rechtsklick isst einen Bissen: 4 Hunger, 0.2 Sättigung (79-98); nach 6 Bissen Luft (92-93); `_inner` auf Seite 4 sobald angebissen (63-64); braucht Normalwürfel darunter (111-113); droppt `MyPizzaItem`, Menge aber 0 (115-121) |
| `ducttape` | `BlockDuctTape`, **`Material.anvil`** (23) | Vanilla-Default | wie Pizza (27-33) | Klick mit einem einzelnen beschädigten Item repariert `maxDamage/6` (mind. 1) und verbraucht eine „Scheibe", 6 Scheiben (88-119); kein Drop, Pick-Block gibt `MyDuctTapeItem` (135-146) |
| `antblock`…`unstableantblock` | `AntBlock` extends `BlockGrass`, tickRandomly, `tabBlock` (19-22) | Vanilla-Grass-Default | Vollblock | Random-Tick ohne Regen und mit Luft darüber: 2+r(6) Ameisen der passenden Art (`Ant`, `Red Ant`, `Unstable Ant`, `Termite`, sonst `Rainbow Ant`), gated durch `BlackAntEnable`/`RedAntEnable`/`UnstableAntEnable`/`TermiteEnable`/`RainbowAntEnable` (46-82); Farbe = Mittel der 3×3-Biom-Grasfarbe (112-125) |
| `crystaltermiteblock` | `CrystalAntBlock`, `Material.grass`, nie opak (106-112) | Vanilla-Default | Vollblock | wie Ameisennest, spawnt `Termite` (78-82) |

Anmerkung zum Grastint der Nester: `colorMultiplier` gilt in 1.7.10 für den ganzen Block, das Vanilla-Sonderverhalten (Seitenflächen ungetönt) ist in `RenderBlocks` auf `Blocks.grass` verdrahtet — ob ein `BlockGrass`-Erbe davon profitiert, ist **offen** und im Spiel gegenzuprüfen. Fünf Nester teilen exakt drei Texturen; sie unterscheiden sich nur im Namen (manifest).

Port: `lavafoam` → `friction(1.1f)` (Vanilla-Eis ist 0.98) + `entityInside`; `blockteleport` → `stepOn` mit `ServerPlayer.teleportTo`; `moledirt` → `randomTick` + `Shapes.box(0,0,0,1,0.875,1)` als Kollisionsform; Pizza/Ducttape → `CakeBlock`-Muster mit `bites` 0–5, sechs Modelle mit `_inner` auf der West-Seite (Seite 4 = West in 1.7.10); Ducttape-Reparatur in `useItemOn`. Nester: `GrassBlock`-ähnlich mit `BlockColors` (Grastint) und `cube_bottom_top`; `crystaltermiteblock` mit `noOcclusion()` + `cutout`.

### Spawner-Blöcke (`BlockReed`, Kreuz-Sprite, Licht 0.9 → 13)

| id | name | texture | look |
|---|---|---|---|
| `island` | Island Block | `island` | eine winzige schwebende Insel: grüne Grasplatte, brauner Erdkegel, ein Baum, gelbe Blüten |
| `kingspawner` | The King Spawner Block | `kingspawner` | weißer Sack (Ei) mit goldener, blau besetzter Krone — Silhouette eines Königs-Eis |
| `queenspawner` | The Queen Spawner Block | `queenspawner` | schwarzes Ei mit violetten Pixeln und derselben goldenen Krone |
| `dungeonspawner` | Random Dungeon Spawner | `dungeonspawner` | konzentrische schwarz-graue Quadrate mit roten Eckpunkten und orangefarbenem Kern — ein Zielscheiben-Portal |

Alle vier: Bounds 0.125–0.875 × 0–1 (IslandBlock.java:16-17 usw.), `tabDecorations` (Island, King, Queen), Platzierung nur auf solidem Block (IslandBlock.java:22-24, KingSpawnerBlock.java:22-24, QueenSpawnerBlock.java:22-24, DungeonSpawnerBlock.java:19-21).

| id | Partikel | Auslöser | Wirkung |
|---|---|---|---|
| `island` | 20 `happyVillager` bei 1/20 Tick (26-33) | Random-Tick | 1+r(3) Inseln in Höhe 12+r(m) mit m = 64 / 55 (`IslandSizeFactor` 2, Default) / 45 (Faktor 1) (40-47), nur wenn 21×21 Luft dort ist (51-59); 1/25 `Island`, sonst `IslandToo` (61-66); Block und Block darüber werden Luft (69-70) |
| `kingspawner` | 20 `fireworksSpark` bei 1/20 (31-36) | 100 Ticks nach Setzen (39-44), Zerstören, `canBlockStay` (46-48, 81-84) | `The King` 8 Blöcke höher im Guard-Modus, wenn `TheKingEnable` (50-59, 69-79) |
| `queenspawner` | wie King | wie King | `The Queen` (QueenSpawnerBlock.java:50-79) |
| `dungeonspawner` | 5 `fireworksSpark` **jeden** Display-Tick, mit Aufwärtsdrift (23-27) | 400 Ticks nach Setzen (29-34) | einer von 50 Dungeon-/Struktur-Typen `r(50)` (40-197), Block + Block darüber Luft (44-45); droppt das Item `RandomDungeon` (200-206) |

Kurios: King/Queen rufen `updateTick` **auch aus `randomDisplayTick`**, sobald `!isRemote` (KingSpawnerBlock.java:27-30) — auf dem integrierten Server also faktisch sofort. Port: `scheduleTick(100)` im `onPlace`, Rest serverseitig; `cross`-Modell, `noOcclusion()`, `lightLevel(13)`.

### Ofen und Werkbank

| id | name | texture | look |
|---|---|---|---|
| `crystalfurnace` (unlit, Id 2912) | Crystal Furnace | `crystalfurnace_side` / `_top` / `_front_off` | Kristallrahmen (blau, Ecken ausgefüllt) mit weißem Gitter; die Front zeigt eine dunkle, schmale Ofenöffnung |
| `crystalfurnace` (lit, Id 2913, Feld `CrystalFurnaceOnBlock`) | Crystal Furnace | dieselben — **`_front_on` wird nie geladen** | identisch zum unlit-Block, nur Licht und Partikel |
| `crystalworkbench` | Crystal Workbench | `crystalworkbench_top` / `_side` / `_bottom` | weißer Rahmen mit einem tanfarbenen Plus-Kreuz und vier Eckkeilen; die drei Texturen sind fast identisch |

`CrystalFurnace` (extends `BlockContainer`, `Material.rock`, Härte 2, Resistenz 10 — manifest ctor): `registerBlockIcons` bildet `(isActive ? "_front_off" : "_front_off")` (CrystalFurnace.java:67) — der Bug sorgt dafür, dass `crystalfurnace_front_on.png` (rote Flamme in der Öffnung, im Jar vorhanden) im Original nie sichtbar ist. Der brennende Block hat Licht 0.6 → 9 (36) und streut `smoke`+`flame` aus der Front (129-154). Seiten-Icons: oben und unten `_top`, Front `_front_off`, Rest `_side` (60-62). Ausrichtung aus dem Spieler-Yaw als Meta 2/5/3/4 (156-169). `TileEntityCrystalFurnace`: Garzeit **150** Ticks statt 200 (TileEntityCrystalFurnace.java:126, 165), Brennwerte `CrystalCoal` 20000, `MyCrystalTreeLog` 800, `CrystalPlanksBlock` 400 (262-270), sonst Vanilla-Tabelle (221-271). GUI benutzt Vanillas `textures/gui/container/furnace.png` (CrystalFurnaceGUI.java:43).

`CrystalWorkbench` (extends `BlockWorkbench`, Härte 1, Resistenz 5 — manifest ctor, `tabDecorations`, nie opak): Icon-Zuordnung ist ungewöhnlich — oben `_top`, **unten `_side`**, Seiten 2 und 4 (Nord/West) `_bottom`, Seiten 3 und 5 `_side` (CrystalWorkbench.java:42, 47-49); keine Ausrichtung. GUI = Vanilla `crafting_table.png` (CrystalWorkbenchGUI.java:35), öffnet `OreSpawnGUIHandler` Id 1 (28).

Port: ein `crystalfurnace`-Block mit `facing` + `lit` (Vanilla-`AbstractFurnaceBlock`-Muster), `orientable` + `orientable`-lit-Modell, `BlockEntity` mit `cookingTotalTime = 150`, eigener `RecipeType`? Nein — das Original nutzt `FurnaceRecipes.smelting()`, also Vanilla-Smelting-Rezepte; `RecipeType.SMELTING` wiederverwenden. **Entscheidung offen:** `_front_on` in der lit-Variante zeigen (Absicht des Autors, Bug behoben) oder `_front_off` behalten (1:1 zum Jar). Empfehlung: `_front_on` — die Textur liegt im Jar, das Ternary in Zeile 67 ist offensichtlich ein Tippfehler. Zwei Registry-Ids → eine Id `crystalfurnace`; `OreSpawn_CrystalFurnaceOnBlock` nur als Legacy-Alias für Weltimporte. Werkbank: `CraftingTableBlock`-Ableitung mit `MenuProvider` auf `CraftingMenu`, Modell mit den sechs Face-Zuordnungen oben (`up=_top`, `down=_side`, `north=_bottom`, `west=_bottom`, `south=_side`, `east=_side`), `noOcclusion()` + `cutout` (136–168 px Alpha, gemessen). Beide brauchen keinen `BlockEntityRenderer`.

---

## Offene Punkte

- Dimensionsabhängige Opazität (`crystalstone`, `crystalgrass`, vier `OreBasicStone`-Varianten): nicht 1:1 abbildbar, Entscheidung (a)/(b) siehe Kristalldimension.
- `FastGraphicsLeaves`: die Ersatztexturen `generic_solid`/`scary_solid` sind ohne eigenen Model-Wrapper nicht erreichbar; ob das gebraucht wird, ist offen.
- `_front_on` des Ofens: Bug beheben oder Jar-Verhalten nachbauen.
- Grastint der Ameisennester auf allen sechs Seiten: im Spiel prüfen.
- `crystalsapling2` droppt `crystalsapling3` (Quellbug) — beheben oder nachbauen.
- Höhen-Metadaten der Stapelpflanzen (`<< 8` in ein Nibble) — Port mit sauberer Property.
- `ore.png` (Stein-Basistextur) wird von keiner Klasse referenziert; im Port nicht registrieren, aber im Asset-Ordner lassen.
