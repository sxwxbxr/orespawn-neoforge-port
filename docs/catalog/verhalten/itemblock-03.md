# Verhalten: itemblock-03

Dieser Stapel umfasst 41 Klassen, fast alle Blöcke, dazu das komplette Kristallofen-/Kristallwerkbank-Paket (Block, TileEntity, Container, GUI, GUI-Handler) und das clientseitige Lebensbalken-Overlay `GirlfriendOverlayGui`. Die Klassen stehen hinter 172 Registry-Einträgen des Manifests. 121 davon entfallen auf `OreGenericEgg` (Ancient Dried Spawn Eggs plus Ender-Pearl- und Eye-of-Ender-Block). Den Rest bilden Erze (Salz, Titan, Uran, Rubin, Amethyst, fünf Kristall-Erze), Speicherblöcke, Pflanzen (Reis, Erdbeere, Tomate, Blumen mit Tag/Nacht-Wechsel, Blätter) und Effektblöcke: Teleport, Lava Foam, Molenoid-Erde, Repellents, Island-, King-, Queen- und Dungeon-Spawner. `PortalBlock` und `RockBlock` werden nirgends instanziiert und sind toter Code. Mehrere Methoden tragen 1.6er-Namen (`tickRate()`, `itemPicked`, `getBlockTexture`, `isBlockSolidOnSide`) und überschreiben in 1.7.10 nichts. Das ist unten je Klasse vermerkt, weil ein 1:1-Port sonst Verhalten erfindet, das das Original nie hatte.

## Gemeinsame Umrechnungen (gelten für alle Abschnitte)

Belegt per `javap -c` auf `reference/jar/mcp/client-1.7.10.jar`, Klasse `aji` (= `net.minecraft.block.Block`):

| 1.7.10-Aufruf | Bytecode | Folge für 1.21.1 |
|---|---|---|
| `setLightLevel(f)` (`func_149715_a`) | `lightValue = (int)(15.0f * f)` | `lightLevel(s -> (int)(15*f))` |
| `setResistance(f)` (`func_149752_b`) | `blockResistance = f * 3.0f` | – |
| `setHardness(h)` (`func_149711_c`) | `blockHardness = h`; hebt `blockResistance` auf `h*5` an, falls kleiner | – |
| `getExplosionResistance(Entity)` (`func_149638_a`) | `blockResistance / 5.0f` | `explosionResistance` in 1.21 = **R·3/5** (bzw. `h` allein, wenn nur `setHardness` gerufen wurde) |

Alle Klassen hier rufen `setHardness` vor `setResistance`. Deshalb gilt überall *Explosion 1.21 = R_code × 0.6*. Die einzigen Ausnahmen haben nur eine Härte: Molenoid-Erde, Sky Tree Log, Blätter, Blumen.

**Werkzeug:** In keinem Quelltext steht `setHarvestLevel` für einen Block. Ein grep über alle `*.java` findet es nur bei Werkzeug-Items. Deutung aus dem Forge-1.7.10-Ablauf (`ForgeHooks.canHarvestBlock` → `ItemPickaxe.func_150897_b`): Blöcke aus `Material.rock` droppen mit **jeder** Spitzhacke und ohne Werkzeug nichts. `Material.ground`, `grass`, `wood` und `plants` droppen auch mit der Hand. Für 1.21: `rock` → `requiresCorrectToolForDrops()` + Tag `mineable/pickaxe`, **ohne** `needs_*_tool`. `ground`/`grass` → `mineable/shovel`, `wood` → `mineable/axe`.

**Client-Statics im Serverpfad:** `OreSpawnMain.current_dimension` und `OreSpawnMain.FastGraphicsLeaves` werden **nur** in `GirlfriendOverlayGui.onRenderOverlay` gesetzt (GirlfriendOverlayGui.java:45-51). Auf einem dedizierten Server bleiben sie auf dem Initialwert 0 (OreSpawnMain.java:6222, 6353). `isOpaqueCube()` von `CrystalGrass`, `OreBasicStone` und `BlockScaryLeaves` hängt daran. Serverseitig sind diese Blöcke damit immer „nicht opak“. Im Port gehört das in Render-Layer und Modell, nicht in Blockeigenschaften.

**Entity-Namen → Registry-IDs (manifest):** `"Termite"` → `termite`, `"Rat"` → `rat`, `"Fairy"` → `fairy`, `"Red Ant"` → `red_ant`, `"Island"` → `island`, `"IslandToo"` → `island_too`, `"The King"` → `the_king` (max_health 7000), `"The Queen"` → `the_queen` (max_health 6000). Repellent-Ziele: `the_kraken` (Kraken, 1000), `purple_power` (PurplePower, 1000), `ant` (EntityAnt). `Termite`, `EntityRedAnt`, `EntityRainbowAnt` und `EntityUnstableAnt` erben von `EntityAnt` (Termite.java:14, EntityRedAnt.java:11, EntityRainbowAnt.java:10, EntityUnstableAnt.java:10).

---

### BlockRice

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `rice_plant` | – (manifest: `name: null`) | `MyRicePlant` | OreSpawnMain.java:1564, registriert 1793 als `OreSpawn_RicePlant` |

- **Rolle:** Reis-Feldfrucht. Vanilla-Basis `BlockCrops`.
- **Werte:**
  - Härte 0, kein Aufruf.
  - Meta/Alter 0–7 (Vanilla `BlockCrops`).
  - Texturen `rice_0..3` (BlockRice.java:40-45): Alter 0–1 → `rice_0`, 2–3 → `rice_1`, 4–6 → `rice_2` (Alter 6 wird auf 4 gesetzt, :19-21), 7 → `rice_3` (:24).
- **Drops:**
  - `quantityDropped` = `2 + nextInt(4)`, also 2–5 (BlockRice.java:27-29).
  - Samen-Item (`func_149866_i`) **und** Frucht-Item (`func_149865_P`) sind beide `MyRice` = Item `rice` (BlockRice.java:31-37; manifest).
  - Vanilla `BlockCrops.getItemDropped` (javap `akf`): Alter 7 → Frucht, sonst Samen. Beides ist Reis, also droppt **auch unreifer Reis 2–5 Reis**.
  - Zusätzlich ab Alter 7 (javap `akf.a(ahb,IIIIFI)`): `3 + fortune` Versuche mit je `nextInt(15) <= meta` → +1 Samen (Reis).
- **Wachstum** (Vanilla, javap `akf.a(ahb,III,Random)`): Licht über der Pflanze ≥ 9, Alter < 7, Chance `1 / ((int)(25/f) + 1)`. `f` kommt aus `func_149864_n` (Nachbarpflanzen, Farmland-Feuchte).
- **Pflanzen:** über Item `rice` (`ItemRadish`, ctor_args: `MyRicePlant`, `OreSpawnMain.CrystalGrass`, manifest). Auf Crystal Grass hält die Pflanze, weil `CrystalGrass.canSustainPlant` immer `true` liefert (CrystalGrass.java:58-60). Deutung: Crystal Grass ist kein Farmland, daher der langsamste Wachstumsfaktor.
- **Rezeptbezug:** keiner in der Klasse.
- **Portierung 1.21.1:**
  - `CropBlock`-Unterklasse, `getBaseSeedId()` → `rice`.
  - `mayPlaceOn` um `crystalgrass` erweitern bzw. `canSustainPlant` (TriState) am Crystal-Grass-Block.
  - Blockstate-Modell mit 4 Texturen nach obiger Zuordnung.
  - Loot-Table: immer `rice` × uniform(2,5). Bei `age=7` zusätzlich `apply_bonus binomial_with_bonus_count extra=3, probability=8/15` (8/15 = (7+1)/15 aus `nextInt(15) <= 7`). **Nicht** die Vanilla-Weizenwahrscheinlichkeit übernehmen.

### BlockRuby

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `blockmobzillascale` | Mobzilla Scale Block | `MyBlockMobzillaScaleBlock` | OreSpawnMain.java:1280 |
| `blockruby` | Ruby Block | `MyBlockRubyBlock` | OreSpawnMain.java:1282 |
| `blockamethyst` | Amethyst Block | `MyBlockAmethystBlock` | OreSpawnMain.java:1283 |

- **Rolle:** Speicherblock. Vanilla-Basis `Block`, `Material.rock`, Kreativtab Blocks (BlockRuby.java:15-18).
- **Werte:**
  - Härte 4, R 4 → Explosion 1.21 = 2.4 (BlockRuby.java:16-17).
  - Licht 0.4 → 6 (BlockRuby.java:19).
  - `isOpaqueCube` false (:22-24), `renderAsNormalBlock` true (:26-28). Folge: volle Würfelform, aber lichtdurchlässig und ohne Nachbar-Culling.
- **Verhalten:** nur für `blockmobzillascale`. Bei `onEntityCollidedWithBlock` und `onEntityWalking` bekommt jede `EntityLivingBase` Strength (`Potion.damageBoost`), Amplifier 0, 200 Ticks (BlockRuby.java:30-40). Ruby- und Amethyst-Block haben kein Verhalten.
- **Drops:** sich selbst (Standard).
- **Rezeptbezug** (nicht in der Klasse): 9 `godzillascale`-Items ↔ Block (OreSpawnMain.java:2960-2961), 9 Ruby ↔ Block (2962-2963), 9 Amethyst ↔ Block (2964, 2967).
- **Portierung:**
  - `Block` mit `noOcclusion()` (Original lichtdurchlässig), `lightLevel 6`, `strength(4, 2.4)`.
  - Effekt über `stepOn` plus `entityInside`. 1.7.10 `onEntityWalking` feuert schrittweise, 1.21 `stepOn` bei jeder Bewegung auf dem Block. Das weicht leicht in der Frequenz ab, ist bei 200 Ticks Dauer aber unerheblich.
  - Separate Klasse oder Flag für den Mobzilla-Block statt `this ==`-Vergleich.

### BlockScaryLeaves

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `leaves_scary` | Scary Tree Leaves | `MyScaryLeaves` | OreSpawnMain.java:1618 (Härte 0.2) |
| `leaves_cherry` | Cherry Tree Leaves | `MyCherryLeaves` | OreSpawnMain.java:1619 (Härte 0.15) |
| `leaves_peach` | Peach Tree Leaves | `MyPeachLeaves` | OreSpawnMain.java:1620 (Härte 0.15) |

- **Rolle:** Blätter mit eigenem Zerfall und Fruchtdrop. Vanilla-Basis `BlockLeaves`. Aus der Aufrufkette: `setLightOpacity(1)`, Grass-Sound (OreSpawnMain.java:1618-1620).
- **Werte:**
  - Härte 0.2 bzw. 0.15, nur Härte gesetzt → Explosion 1.21 = 0.2 bzw. 0.15.
  - Sub-Blocks nur Meta 0 (BlockScaryLeaves.java:23-25).
  - `func_150125_e` (BlockLeaves, Namensliste der Holzarten) → `null` (:117-119).
- **Zufallstick** `updateTick` (BlockScaryLeaves.java:48-76), nur Server:
  1. Radius `var7 = 2`. Nur wenn die Chunks im Würfel ±2 existieren (:49-51).
  2. Suche über dx −2..2, **dy −2..0**, dz −2..2 mit Manhattan-Abstand ≤ 3 nach einem Block mit `canSustainLeaves` (Stämme, u. a. `BlockSkyTreeLog`) (:52-58).
  3. Gefunden:
     - Scary Leaves bei Tageszeit `worldTime % 24000 < 12000` → per `OreSpawnMain.setBlockFast(..., MyAppleLeaves, 0, 3)` zu `leaves_apple` (:59-63).
     - Ist der Block darunter Luft und `nextInt(20) == 3`: `dropBlockAsItemWithChance` an y−1 (:64-67). Dann `return`.
  4. Nicht gefunden → `removeLeaves`: Drop-Aufruf, dann Luft mit Flag 2 (:74, 81-84).
  5. Der Vanilla-Zerfalls-Bit (Meta 8) wird ignoriert, deshalb zerfallen auch von Spielern gesetzte Blätter ohne Stamm.
- **Drops:** `dropBlockAsItemWithChance` ist ohne `super` überschrieben (:27-36). Einziger Drop ist mit `nextInt(25) == 1` eine Kirsche (`cherries`) bzw. ein Pfirsich (`peach`). Scary Leaves: nichts. Keine Setzlinge. `quantityDropped` (Kirsche `nextInt(4)`, Pfirsich `nextInt(1)` = 0, sonst 0; :38-46) läuft nur über `getDrops`-Pfade, die hier nie aufgerufen werden. Deutung: praktisch wirkungslos. Schere: Vanilla-Forge-`IShearable` → Blattblock.
- **Rendering** (Client):
  - Fancy (`FastGraphicsLeaves == 0`): `blockIcon`, alle Seiten.
  - Fast: Kirsche/Pfirsich `generic_solid`, Scary `scary_solid`, Seite nur gegen fremde Nachbarn (:86-115).
  - `FastGraphicsLeaves` setzt `GirlfriendOverlayGui` aus `gameSettings.fancyGraphics`.
- **Rezeptbezug:** Kirsche → Kirschsamen, Pfirsich → Pfirsichsamen (OreSpawnMain.java:3050, 3052), nicht in der Klasse.
- **Abhängigkeit:** Die Rückverwandlung Apple → Scary zur Nacht steht nicht hier. Vermutlich in `BlockAppleLeaves` (anderer Stapel), offen.
- **Portierung:**
  - Keine Vanilla-`LeavesBlock`-Distanzlogik: die hat Reichweite 7 und respektiert `PERSISTENT`, das Original prüft Manhattan ≤ 3 nur nach unten.
  - Eigener Block mit `randomTicks()` und `randomTick` 1:1.
  - Render-Type `cutout_mipped`. Fast/Fancy übernimmt 1.21 automatisch über das Leaves-Modell, `generic_solid`/`scary_solid` nur als Option.
  - `setBlockFast` → `level.setBlock(pos, state, 3)`.

### BlockSkyTreeLog

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `skytreelog` | Sky Tree Wood | `MySkyTreeLog` | OreSpawnMain.java:1607, ctor `(BaseBlockID+113, 20)`, `setHardness(0.2)`, Wood-Sound |

- **Rolle:** Stamm der Sky Trees mit Baumfäll-Rekursion. Vanilla-Basis `Block`, `Material.wood`, Tab Blocks (BlockSkyTreeLog.java:15-18). Das zweite ctor-Argument `20` bleibt ungenutzt.
- **Werte:** Härte 0.2 (Kette), Explosion 1.21 = 0.2. `canSustainLeaves` und `isWood` → true (:28-34). Drop `skytreelog` (:36-38). Silk-Touch-Stack Meta 0 (:24-26).
- **Verhalten** `onBlockDestroyedByPlayer` (:65-69):
  1. Block auf Luft (Flag 2).
  2. `breakRecursor(x,y,z, x,y,z, 0)`.
  3. `dropBlockAsItem(..., 0, 0)` → ein Log.
- **`breakRecursor`** (:40-63):
  - Abbruch bei Rekursionstiefe > 1000 (:42).
  - Durchläuft die 26 Nachbarn im 3×3×3-Würfel, ausgenommen die Aufrufer-Position (:48-49).
  - Ab Tiefe 1 werden Nachbarn übersprungen, die auch im 3×3×3-Würfel um die Aufrufer-Position liegen (:50). Deutung: Optimierung, die diagonale Äste verfehlen kann.
  - Jeder gefundene `skytreelog`-Block: Luft (Flag 2), 1 Log droppen, rekursiv weiter (:52-56).
- **Deutung zum Drop** (Vanilla-1.7.10-Ablauf `ItemInWorldManager.tryHarvestBlock`): Nach `onBlockDestroyedByPlayer` folgt noch `harvestBlock`. Der abgebaute Block droppt damit wahrscheinlich **zwei** Logs. `onBlockDestroyedByPlayer` läuft auch im Kreativmodus, dort fällt der Baum mit Drops.
- **Rezeptbezug:** 1 `skytreelog` → 4 Vanilla-Planken (OreSpawnMain.java:5023).
- **Portierung:**
  - Rekursion als iterative BFS mit Grenze 1000 Schritte, gleiche Nachbarschaftsregel für 1:1.
  - Einstieg über `playerDestroy` bzw. `onDestroyedByPlayer`. Die Doppel-Drop-Frage vorher im Spiel belegen oder bewusst entscheiden.
  - Tags `minecraft:logs` (für Blattzerfall der eigenen Blätterklassen) und `mineable/axe`.

### BlockStrawberry

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `strawberry_plant` | – (manifest `name: null`) | `MyStrawberryPlant` | OreSpawnMain.java:1548, registriert 1791 |

- **Rolle:** Erdbeer-Feldfrucht. Vanilla-Basis `BlockCrops`.
- **Werte:** Alter 0–7. Texturen `strawberry_0..3`, gleiche Zuordnung wie Reis (BlockStrawberry.java:17-25, 40-45).
- **Drops:**
  - `quantityDropped` = `1 + nextInt(5)` = 1–5 (:27-29).
  - Samen `MyStrawberrySeed` = `strawberry_seed`, Frucht `MyStrawberry` = `strawberry` (:31-37; manifest).
  - Unreif: 1–5 `strawberry_seed`. Reif (Alter 7): 1–5 `strawberry` + `3 + fortune` Versuche `nextInt(15) <= 7` → +1 `strawberry_seed` (Vanilla, javap `akf`).
- **Wachstum:** Vanilla, siehe BlockRice. Gepflanzt über `strawberry_seed` auf Farmland (manifest ctor_args `Blocks.field_150458_ak`).
- **Portierung:** `CropBlock`. Loot-Table: `strawberry_seed` uniform(1,5) bei age<7, `strawberry` uniform(1,5) bei age=7, plus `strawberry_seed` binomial extra 3 p=8/15 bei age=7.

### BlockTitanium

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `blocktitanium` | Titanium Block | `MyBlockTitaniumBlock` | OreSpawnMain.java:1279 |

- **Rolle:** Speicherblock mit Funkenpartikeln. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks.
- **Werte:** Härte 5, R 5 → Explosion 3.0 (BlockTitanium.java:15-16). Licht 0.5 → 7 (:18). `tickRate()` ohne `World`-Parameter (:21-23) überschreibt `func_149738_a(World)` nicht (MCP joined.srg) → toter Code.
- **Verhalten** (Client, `randomDisplayTick`): mit `nextInt(20) == 0` (:27) `sparkle`, 6 Partikel (:35).
  - Position zufällig im Block. Für Seite i (0 oben, 1 unten, 2 +z, 3 −z, 4 +x, 5 −x) wird die Koordinate um 0.0625 nach außen gesetzt, wenn der Nachbar nicht opak ist (:34-56).
  - Gespawnt wird nur, wenn der Punkt außerhalb des Blocks liegt (:57). Die y-Untergrenze prüft `var9 < 0.0` statt `< par3`, ein Tippfehler im Original, praktisch wirkungslos.
  - Typ `nextInt(3)`: `flame` / `smoke` / `reddust` (:58-67).
- **Rezeptbezug:** 9 `MyIngotTitanium` ↔ Block (OreSpawnMain.java:2958-2959).
- **Portierung:** `animateTick` mit `ParticleTypes.FLAME`, `SMOKE`, `DustParticleOptions` (rot). Nur Client-Aufruf, also serverneutral.

### BlockTomato

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `tomato_0` | Tomato Plant | `MyTomatoPlant1` | OreSpawnMain.java:1577 |
| `tomato_1` | Tomato Plant | `MyTomatoPlant2` | OreSpawnMain.java:1578 |
| `tomato_2` | Tomato Plant | `MyTomatoPlant3` | OreSpawnMain.java:1579 |
| `tomato_3` | Tomato Plant | `MyTomatoPlant4` | OreSpawnMain.java:1580 |

- **Rolle:** mehrblockige Tomatenstaude. Stufen: Plant1 = Spitze/wachsend, Plant2 = Stamm unreif, Plant3 = halbreif, Plant4 = reif.
- **Vanilla-Basis `BlockReed`:** kein Kollisionskasten, Kreuz-Rendering, Bruch bei fehlendem Halt über `onNeighborBlockChange` → `canBlockStay` → `canPlaceBlockAt`.
- **Werte:** Hitbox 0.125–0.875 in x und z, Höhe 1.0 (`var3 = 0.375`, BlockTomato.java:17-18). Zufallsticks an (:19). Härte 0.
- **Platzierung** (:22-25): Darunter muss eine der vier Tomatenstufen, Gras, Erde oder Farmland liegen.
- **Zufallstick** (:27-81), nur Server und nur für Plant1/Plant2 (:30-35):
  1. `var7 = meta`, `myMaxHeight = meta >> 8`, Alter = `meta & 0xFF` (:36-38).
     **Deutung (Engine-Fakt 1.7.10):** Blockmetadaten sind 4 Bit, alle Werte `x << 8` werden beim Speichern zu 0. `myMaxHeight` ist deshalb beim Lesen immer 0 und wird **jeden Tick neu** ausgewürfelt: `3 + OreSpawnRand.nextInt(3)` = 3–5 (:39-41).
  2. Nur wenn über dem Block Luft ist (:42-43). Zählt dann nach unten bis 9 Blöcke durchgehenden Tomatenstamm, `Height` beginnt bei 1 (:44-53). Liegt darunter eine Plant3/Plant4, gilt `myMaxHeight = Height` (:50-56).
  3. Alter `< 5 − myMaxHeight/3` (bei 3–5 also < 4): Alter +1 (:76-79).
  4. Sonst, falls `Height < myMaxHeight`: über dem Block Plant1, der Block selbst wird Plant2 (Meta effektiv 0) (:58-61).
  5. Sonst Reifung: für i = 0..myMaxHeight−1 nach unten Plant2 → Plant3 und Plant3 → Plant4 (Plant1 bleibt), dann Meta des Blocks auf 0 (:62-74).
- **Drops:** Item `tomato_seed` (Lang „Tomato“, manifest), Anzahl nur bei Plant4 `2 + nextInt(4)` = 2–5, sonst 0 (:83-92).
- **Toter Code:** `itemPicked`, `getSeedItem`, `getCropItem` (:94-104) haben keine MCP-Entsprechung (methods.csv: 0 Treffer). Deutung: Pick-Block liefert daher Vanilla-`BlockReed.getItem` = Zuckerrohr.
- **Portierung:**
  - 4 Blöcke. `IntegerProperty AGE 0..15` bildet die 4-Bit-Meta ab, `randomTicks`, `noCollission`, `canSurvive` wie oben.
  - `updateShape`/`neighborChanged` → zerstören wie `SugarCaneBlock`.
  - Die Überlauf-Semantik 1:1 nachbilden: maxHeight jeden Tick neu würfeln, Schwelle 4.
  - Loot-Table nur für `tomato_3`.

### BlockUranium

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `blockuranium` | Uranium Block | `MyBlockUraniumBlock` | OreSpawnMain.java:1278 |

- **Rolle:** wie `BlockTitanium`, Code zeilengleich bis auf das Licht.
- **Werte:** Härte 5, R 5 → Explosion 3.0 (BlockUranium.java:15-16). Licht 0.2 → 3 (:18). `tickRate()` toter Code (:21-23).
- **Verhalten:** Partikel exakt wie `BlockTitanium` (:26-70).
- **Rezeptbezug:** 9 `MyIngotUranium` ↔ Block (OreSpawnMain.java:2956-2957).
- **Portierung:** gemeinsame Klasse mit Titanium-Block, Licht als Parameter.

### CreeperRepellent

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `creeperrepellent` | Creeper Repellent | `CreeperRepellent` | OreSpawnMain.java:1592 (Licht 0.8) |

- **Rolle:** Fackelartiger Block, der Creeper, Ameisen und Purple Power wegschleudert. Vanilla-Basis `BlockTorch`, Tab Redstone (CreeperRepellent.java:16).
- **Werte:** Licht 0.8 → 12 (Kette). Härte 0. Tickrate 10 Ticks (:54-56, echter Override `func_149738_a(World)`).
- **Tick-Kette:** `onBlockAdded` und `onNeighborBlockChange` planen einen Tick in 10 Ticks (:65-71). `updateTick` (Server) ruft `findSomethingToRepell` und plant neu (:58-63). Alle drei Overrides **ohne `super`**, daraus folgt (Deutung aus BlockTorch-1.7.10-Ablauf):
  - Die Fackel fällt bei entferntem Halt **nicht** ab.
  - `BlockTorch.onBlockAdded` setzt keine Ausrichtung. Die Ausrichtung kommt allein aus `onBlockPlaced`.
- **`findSomethingToRepell`** (:77-149):
  - Suchbox x ±20, y ±10, z ±20 um die Blockecke (:78), alle `EntityLivingBase`.
  - Für `EntityCreeper`, `EntityAnt` und `PurplePower` gilt: `f = clamp(20 − Abstand, 0, 20) × 0.4`, maximal 8.0 (:85-97). `d4 = atan2(dx, dz)`. `motionX += f·sin(d4)`, `motionZ += f·cos(d4)`, also horizontal vom Block weg (:98-102).
  - `PurplePower` mit `getPurpleType() == 10` → **`return`**. Bricht die ganze Suche ab, danach stehende Entities werden in diesem Tick nicht mehr geschoben (:124-128).
- **Partikel** (Client, :20-52): Basis (x+0.5, y+0.7, z+0.5). Meta 1–4 (Wand) um ±0.271 horizontal und +0.413 vertikal versetzt, sonst +0.21. Je ein `smoke`, `flame`, `reddust`.
- **Rezeptbezug:** `"D D","STS","D D"` mit D = `GreenGoo`, T = `ExtremeTorch`, S = String (OreSpawnMain.java:3042).
- **Portierung:**
  - 1.21 trennt Stand- und Wandfackel (`TorchBlock` + `WallTorchBlock` + `StandingAndWallBlockItem`). Dafür ist eine **zweite Registry-ID** nötig, z. B. `wall_creeperrepellent`. Sie steht nicht im Manifest.
  - Alternative ist ein Block mit eigener `FACING`-Property inkl. UP.
  - Schieben per `entity.push`/`setDeltaMovement` plus `hurtMarked = true`, sonst sieht der Client für Spieler nichts (hier sind nur Mobs Ziel).
  - `scheduleTick` beibehalten. Das Nicht-Abfallen ist 1:1 nur ohne `canSurvive`-Prüfung zu haben.

### CrystalAntBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystaltermiteblock` | Crystal Termite Nest | `CrystalTermiteBlock` | OreSpawnMain.java:5946, registriert 1850 |

- **Rolle:** Termitennest der Crystal Dimension. Vanilla-Basis `Block`, `Material.grass`, Zufallsticks, Tab Blocks (CrystalAntBlock.java:21-23).
- **Werte:** keine Härte gesetzt → 0, sofort abbaubar. Nicht opak, kein Normal-Rendering (:106-112). Texturen `crystaltermite_top/_bottom/_side` (:115-119).
- **Zufallstick** (:48-89), nur Server:
  1. Bei Regen `return` (:51-53).
  2. Nur wenn über dem Block Luft ist (:54-55).
  3. `howmany = OreSpawnRand.nextInt(6) + 2`, also 2–7 (:56).
  4. Je Durchlauf spawnt bei `TermiteEnable != 0` eine `"Termite"` an (x+0.5, y+1.01, z+0.5) (:78-82).
  - Die Zweige für `MyAntBlock`, `MyRedAntBlock`, `MyUnstableAntBlock`, `TermiteBlock` und den Rainbow-else sind hier tot: diese Felder sind `AntBlock`-Instanzen (OreSpawnMain.java:5943-5948).
  - Für Crystal Termite erreichbar wäre der else-Zweig „Rainbow Ant“ nie, weil `this == CrystalTermiteBlock` vorher greift.
  - Keine Obergrenze der Population.
- **`spawnCreature`** (:95-104): `EntityList.createEntityByName`, zufälliger Yaw 0–360, `playLivingSound`.
- **Drops:** sich selbst (:91-93).
- **Config:** `OreSpawnMOBS.TermiteEnable` (Default 1, manifest).
- **Portierung:** `randomTick`, `EntityType.TERMITE.spawn(...)`, danach `playAmbientSound()`. Config-Schalter über ModConfigSpec. Die toten Zweige nicht übernehmen, die gehören zu `AntBlock`.

### CrystalFurnace

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalfurnace` | Crystal Furnace | `CrystalFurnaceBlock` (aus) | OreSpawnMain.java:1533, `(…, false, 2.0f, 10.0f)`, Legacy `OreSpawn_CrystalFurnaceBlock` |
| `crystalfurnace` | Crystal Furnace | `CrystalFurnaceOnBlock` (an) | OreSpawnMain.java:1534, `(…, true, 2.0f, 10.0f)`, Legacy `OreSpawn_CrystalFurnaceOnBlock` |

- **Rolle:** Ofen der Crystal Dimension. Vanilla-Basis `BlockContainer`, `Material.rock`. TileEntity `TileEntityCrystalFurnace`.
- **Manifest-Kollision:** beide Instanzen haben die ID `crystalfurnace` (`setBlockName`), siehe unresolved.
- **Werte:**
  - Härte 2, R 10 → Explosion 6.0 (CrystalFurnace.java:38-39; OreSpawnMain.java:1533-1534).
  - Aus-Variante im Tab Decorations, An-Variante mit Licht 0.6 → 9 (:32-37).
  - Nicht opak, kein Normal-Rendering (:42-48).
- **Texturen** (:60-69): Oben **und** unten `crystalfurnace_top`, Front (Seite == Meta) `crystalfurnace_front_off` in **beiden** Zuständen, sonst `_side`. `crystalfurnace_front_on.png` liegt im Jar (texture_map), wird aber nie geladen.
- **Ausrichtung:**
  - `onBlockPlacedBy` (:156-173): `l = floor(yaw·4/360 + 0.5) & 3`, dann 0 → Meta 2, 1 → 5, 2 → 3, 3 → 4. Die Front zeigt zum Spieler.
  - Individueller Name aus dem ItemStack → `TileEntityCrystalFurnace.func_145951_a` (MCP: `setCustomInventoryName`).
  - `onBlockAdded` → `setDefaultDirection` (:89-115): Meta 3, 2, 5, 4 je nach `func_149730_j` (MCP: `isFullBlock`) der Nachbarn. `onBlockPlacedBy` überschreibt das danach.
- **Zustandswechsel** `updateFurnaceBlockState` (:71-87):
  1. Meta und TileEntity merken.
  2. `keepFurnaceInventory = true`, Block tauschen (Flag 3), wieder false.
  3. Meta zurücksetzen, TileEntity `validate` und neu setzen.
- **Rechtsklick** (:117-126): Server öffnet GUI-ID 0 über `OreSpawnMain.instance`, sofern TileEntity vorhanden. Gibt immer true zurück.
- **Partikel** (Client, nur An, :129-154): y = y + rand·6/16, seitlicher Versatz ±0.3, vor der Front (Meta 4/5/2/3). Je `smoke` + `flame`.
- **Abbau** `breakBlock` (:175-208): ohne Keep-Flag alle Slots auswerfen.
  - Stapel in Teilen von `nextInt(21) + 10` = 10–30 (:186).
  - Position +0.1..0.9 im Block (:182-184), Bewegung Gauß × 0.05, y +0.2 (:196-199). NBT bleibt erhalten.
  - Danach `func_147453_f` (MCP: `updateNeighborsAboutBlockChange`).
- **Drops:** immer die Aus-Variante (:50-52). `idPicked` (:55-57) ist kein 1.7.10-Name (methods.csv 0) → toter Code.
- **Komparator:** `Container.calcRedstoneFromInventory` (:210-216).
- **Rezeptbezug:** Ring aus 8 `crystalstone` (OreSpawnMain.java:2741).
- **Portierung:**
  - **Ein** Block `crystalfurnace` mit `HORIZONTAL_FACING` + `LIT`, `lightLevel(s -> s.getValue(LIT) ? 9 : 0)`. `keepFurnaceInventory`-Hack entfällt (in `onRemove` `newState.is(this)` prüfen).
  - Die Legacy-ID `OreSpawn_CrystalFurnaceOnBlock` ist nur für Weltkonvertierung relevant.
  - Front-Textur in beiden Zuständen `front_off` (1:1).
  - `BaseEntityBlock` + `getTicker` (Server).
  - Menü über `player.openMenu(MenuProvider, pos)`.

### CrystalGrass

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalgrass` | Crystal Grass | `CrystalGrass` | OreSpawnMain.java:1528, `(…, 0.6f, 2.0f)` |

- **Rolle:** Oberflächenblock der Crystal Dimension. Vanilla-Basis `Block`, `Material.grass`.
- **Werte:** Härte 0.6, R 2 → Explosion 1.2 (CrystalGrass.java:22-23). Keine Zufallsticks, breitet sich also nicht aus (:24). Tab Blocks.
- **Verhalten:**
  - `canSustainPlant(...)` → immer true, für **jede** Pflanzenart (Crop, Plains, Desert, Cave …) (:58-60).
  - Drop: sich selbst (:54-56).
  - `isOpaqueCube`/`renderAsNormalBlock` → true nur bei `current_dimension == DimensionID5` (:62-68). `DimensionID5 = BaseDimensionID + 4` (OreSpawnMain.java:1270), Default `BaseDimensionID` 80 (OreSpawnMain.java:1138), also 84. Serverseitig immer false (siehe oben).
  - `isBlockSolidOnSide(World,…)` (:28-30) ist ein 1.6er-Name. Deutung: Forge 1.7.10 heißt die Methode `isSideSolid(IBlockAccess,…)`, deshalb toter Code.
  - `getBlockTexture` (:41-52): 1.6er-Name, tot. Wirkungsgleich mit `getIcon(side, meta)`.
- **Texturen:** `crystalgrass_top/_bottom/_side` (:72-74).
- **Portierung:**
  - Voller, opaker Würfel (das Original ist in seiner Dimension opak, anderswo wirkt es nur auf das Client-Rendering).
  - `canSustainPlant` → `TriState.TRUE`.
  - Tag `mineable/shovel`. Kein Graswachstum, kein Tinting.

### CrystalWood

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalplanks` | Crystal Planks | `CrystalPlanksBlock` | OreSpawnMain.java:1531, `(…, 1.5f, 4.0f)` |

- **Rolle:** Planken der Crystal Dimension. Vanilla-Basis `Block`, `Material.wood`, Tab Blocks, keine Zufallsticks (CrystalWood.java:13-17).
- **Werte:** Härte 1.5, R 4 → Explosion 2.4. Nicht opak, kein Normal-Rendering (:25-31), Standard-Würfel-Rendering.
- **Brennwert:**
  - Im Kristallofen **300**, weil der `Material.wood`-Zweig (TileEntityCrystalFurnace.java:231-233) vor dem `CrystalPlanksBlock → 400`-Zweig (:268-270) greift. Die 400 sind nie erreichbar.
  - Deutung aus demselben Materialzweig in Vanilla: auch im normalen Ofen 300.
- **Rezeptbezug:**
  - 1 `crystaltreelog` → 4 Planken (OreSpawnMain.java:2739), 4 Planken → Crystal Workbench (2740).
  - Truhe (2742, 2868), Holztür (2743-2744), Crystal Sticks (3035), Holzwerkzeuge (2859-2867).
- **Portierung:** `Block` mit `noOcclusion()` (1:1) oder vollem Würfel. Tags `planks` + `mineable/axe`. Brennwert 300 über Data-Map `neoforge:furnace_fuels`, gilt dann auch für den Vanilla-Ofen.

### CrystalWorkbench

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalworkbench` | Crystal Workbench | `CrystalWorkbenchBlock` | OreSpawnMain.java:1532, `(…, 1.0f, 5.0f)` |

- **Rolle:** Werkbank. Vanilla-Basis `BlockWorkbench` (`Material.wood`), Tab Decorations (CrystalWorkbench.java:19-21).
- **Werte:** Härte 1, R 5 → Explosion 3.0. Nicht opak (:32-38).
- **Texturen** (:41-50): oben `_top`; unten und Seiten 3/5 `_side`; Seiten 2 und 4 `_bottom` als Front.
- **Verhalten:** Rechtsklick öffnet auf dem Server GUI-ID 1, gibt immer true zurück (:24-30). Container `ContainerCrystalWorkbench`.
- **Rezeptbezug:** Rezept 4 Planken shapeless (OreSpawnMain.java:2740). Die Werkbank selbst nutzt den Vanilla-`CraftingManager`, siehe Container.
- **Portierung:** `CraftingTableBlock`-Unterklasse. `getMenuProvider` → eigener `CraftingMenu`-Subtyp mit `stillValid` gegen `crystalworkbench`. Screen = Vanilla `CraftingScreen` (Client).

### DungeonSpawnerBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `dungeonspawner` | Random Dungeon Spawner | `MyDungeonSpawnerBlock` | OreSpawnMain.java:1604 (Licht 0.9), registriert 1844 |

- **Rolle:** Einweg-Block, der eine von 50 Strukturen baut. Vanilla-Basis `BlockReed`, kein Kreativtab.
- **Werte:**
  - Licht 0.9 → 13. Härte 0.
  - Hitbox 0.125–0.875 × Höhe 1 (DungeonSpawnerBlock.java:14-15).
  - Zufallsticks an (:16). `canBlockStay` → true (:212-214), der Block bricht nie durch Nachbarn.
- **Platzierung:** Material des Blocks darunter `isSolid()` (:19-21).
- **Ablauf:**
  - `onBlockAdded` (Server) plant einen Tick in 400 Ticks (:29-34). Da auch Zufallsticks laufen, kann der Bau früher auslösen.
  - `updateTick` (:40-197):
    1. Block und Block darüber auf Luft (Flag 2) (:44-45).
    2. `type = nextInt(50)` (:46), dann Bau an (x,y,z), bei 43–45 an (x,y+1,z):

| type | Aufruf | type | Aufruf |
|---|---|---|---|
| 0 | `OreSpawnTrees.FairyTree` | 25 | `MyDungeon.makeCrystalHauntedHouse` |
| 1 | `OreSpawnTrees.FairyCastleTree` | 26 | `makeBouncyCastle` |
| 2 | `MyDungeon.makeEnormousCastle` | 27 | `makeEnderCastle` |
| 3 | `makeRotatorStation` | 28 | `makeDamselInDistress` |
| 4 | `makeBeeHive` | 29 | `makeIncaPyramid` |
| 5 | `makeHauntedHouse` | 30 | `makeRobotLab` |
| 6 | `makeMantisHive` | 31 | `makeKingAltar` |
| 7 | `makeKyuubiDungeon` | 32 | `makeLeonNest` |
| 8 | `makeSmallBeeHive` | 33 | `makeCrystalBattleTower` |
| 9 | `makeShadowDungeon` | 34 | `makeCephadromeAltar` |
| 10 | `makeAlienWTFDungeon` | 35 | `makeGirlfriendIsland` |
| 11 | `makeEnderKnightDungeon` | 36 | `makeGreenhouseDungeon` |
| 12 | `makePlayPool` | 37 | `makeMonsterIsland` |
| 13 | `makeWaterDragonLair` | 38 | `makeNightmareRookery` |
| 14 | `makeCloudSharkDungeon` | 39 | `makeStinkyHouse` |
| 15 | `makeLeafMonsterDungeon` | 40 | `makeRubberDuckyPond` |
| 16 | `makeMiniDungeon` | 41 | `makeWhiteHouse` |
| 17 | `makeGoldFishBowl` | 42 | `makeQueenAltar` |
| 18 | `makeEnderReaperGraveyard` | 43 | `makeFrogPond` (y+1) |
| 19 | `makeSpitBugLair` | 44 | `makePumpkin` (y+1) |
| 20 | `makeIgloo` | 45 | `makeRoundRotator` (y+1) |
| 21 | `MyDungeon.makeDungeon` | 46 | `makeRainbow` |
| 22 | `RubyDungeon.makeDungeon` (`RubyBirdDungeon`) | 47 | `makeEnormousCastleQ` |
| 23 | `BMaze.buildBasiliskMaze` (`BasiliskMaze`) | 48 | `makeSpiderHangout` |
| 24 | `MyDungeon.makeEnderDragonHospital` | 49 | `makeRedAntHangout` |

  Quelle der Tabelle: DungeonSpawnerBlock.java:47-196. Feldtypen: `OreSpawnTrees: Trees`, `MyDungeon: GenericDungeon`, `RubyDungeon: RubyBirdDungeon`, `BMaze: BasiliskMaze` (OreSpawnMain.java:195-198). Die Research nennt nur die Fälle 0–18 als gelesen; hier sind alle 50 belegt.

- **Partikel** (`randomDisplayTick`): 5 × `fireworksSpark` zufällig im Block, Geschwindigkeit x/z ±0.25, y 0..0.5 (:23-27).
- **Drops/Pick:** Item `randomdungeon` ×1 (:199-210). Abbau durch den Spieler baut nichts (:36-38).
- **Rezeptbezug:** Item `randomdungeon` = 8 Redstone-Blöcke um 1 Kohle (OreSpawnMain.java:3017), nicht in der Klasse.
- **Research-Widerspruch:** Website „auf Stein/Bruchstein“, NamuWiki „nur auf Erde“. Der Code verlangt nur festes Material darunter.
- **Portierung:**
  - Block ohne Kollision, `scheduleTick(400)` in `onPlace`, `randomTicks`.
  - Die 50 Bauaufrufe setzen eine portierte `GenericDungeon`-/`Trees`-API voraus. Laut Guardrail sind das Structure-Pieces, hier aber *zur Laufzeit* am Blockort gebaut, also als `StructureTemplate`/Piece-Platzierung mit `ServerLevel` außerhalb der Weltgenerierung.
  - Chunk-Ladung großer Strukturen beachten.

### IslandBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `island` | Island Block | `MyIslandBlock` | OreSpawnMain.java:1591 (Licht 0.9), registriert 1841 |

- **Rolle:** pflanzbarer Block, der schwebende Inseln (Entities) erzeugt. Vanilla-Basis `BlockReed`, Tab Decorations.
- **Werte:** Licht 0.9 → 13. Härte 0. Hitbox wie DungeonSpawner (IslandBlock.java:16-17). Zufallsticks (:18).
- **Platzierung:** festes Material darunter (:22-24). Bruch bei Nachbaränderung über `BlockReed.canBlockStay` = `canPlaceBlockAt`.
- **Zufallstick** (:35-71), nur Server, kein geplanter Tick:
  1. `n = 1 + nextInt(3)` Versuche (:40).
  2. `m = 64`, bei `IslandSizeFactor == 2` 55, bei `== 1` 45 (:41-47). Der Config-Default ist 2, also m = 55. Bei 3–5 gilt 64.
  3. Pro Versuch `height = 12 + nextInt(m)` (:49). Prüft 21×21 (±10) auf Höhe y+height auf reine Luft (:51-59).
  4. Frei: mit `nextInt(25) == 1` spawnt `"Island"`, sonst `"IslandToo"`, an (x, y+height, z) ohne +0.5 (:60-67).
  5. Block und Block darüber → Luft (Flag 2) (:69-70).
- **Partikel:** mit `nextInt(20) == 1` 20 × `happyVillager` (:26-33).
- **Drops:** `island` ×1 (:73-79). Pick-Block Deutung: Vanilla-Reed-Item.
- **Config:** `OreSpawnTWEAKS.IslandSizeFactor` (Default 2, manifest; OreSpawnMain.java:1147):
  - geklemmt auf 1..5 (OreSpawnMain.java:1209-1214)
  - bei `LessLag == 1` ≤ 2, bei `LessLag == 2` = 1 (OreSpawnMain.java:1226-1238)
- **Entities:** `island` (Klasse `Island`), `island_too` (`IslandToo`), beide `EntityAnimal` (Island.java:13, IslandToo.java:12).
- **Portierung:** `randomTick` 1:1. Spawn über EntityType, danach `playAmbientSound`. Config-Klemmung inkl. LessLag-Abhängigkeit in ModConfigSpec nachbilden.

### KingSpawnerBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `kingspawner` | The King Spawner Block | `MyKingSpawnerBlock` | OreSpawnMain.java:1601 (Licht 0.9), registriert 1842 |

- **Rolle:** Einweg-Beschwörung von The King. Vanilla-Basis `BlockReed`, Tab Decorations.
- **Werte:** Licht 0.9 → 13. Härte 0. Hitbox 0.125–0.875 (KingSpawnerBlock.java:16-17). Zufallsticks.
- **Auslöser**, alle → `updateTick`:
  - Platzierung: `onBlockAdded` plant einen Tick in 100 Ticks (:39-44).
  - Jeder Zufallstick.
  - `onBlockDestroyedByPlayer` (:46-48).
  - `canBlockStay` (:81-84), wird von `BlockReed.onNeighborBlockChange` bei jeder Nachbaränderung gerufen.
  - `randomDisplayTick` mit `!isRemote` (:27-30). In 1.7.10 nur clientseitig aufgerufen, Deutung: toter Zweig.
- **`updateTick`** (:50-59): Bei `TheKingEnable != 0` spawnt `"The King"` an (x, y+8, z) (:55), Yaw zufällig, `playLivingSound`, `setGuardMode(1)` (:69-79). Danach Block und Block darüber → Luft.
- **Partikel:** mit `nextInt(20) == 1` 20 × `fireworksSpark` (:31-36).
- **Drops:** `kingspawner` ×1 (:61-67). Wird aber beim Abbau selbst gelöscht, bevor gedroppt werden kann. Deutung: Reihenfolge `onBlockDestroyedByPlayer` vor `harvestBlock` → Drop möglich, im Spiel belegen.
- **Config:** `OreSpawnMOBS.TheKingEnable` (Default 1).
- **Portierung:**
  - `onPlace` → `scheduleTick(100)`, `randomTick`, `neighborChanged`, `playerWillDestroy` → Spawn.
  - TheKing hat laut Manifest `max_health` 7000 > 1024. Virtual Health liegt beim Entity-Port, nicht hier.

### KrakenRepellent

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `krakenrepellent` | Kraken Repellent | `KrakenRepellent` | OreSpawnMain.java:1590 (Licht 0.8), registriert 1839 |

- **Rolle:** wie `CreeperRepellent`, aber gegen Kraken und Ameisen. Vanilla-Basis `BlockTorch`, Tab Redstone.
- **Werte:** Licht 0.8 → 12. Tickrate 10 (KrakenRepellent.java:53-55). Gleiche Tick-Kette ohne `super` (:57-70).
- **Suche** (:76-124): Box x ±20, **y −10..+40**, z ±20 (:77).
  - `Kraken`: Abstand wird mit `posY − 15` berechnet (:85). Also wie 20−d, geklemmt, × 0.4.
  - `EntityAnt`: normaler Abstand (:103-122).
  - Kein PurplePower-Zweig.
- **Partikel:** identisch zu CreeperRepellent (:19-51).
- **Rezeptbezug:** `"D D","STS","D D"` mit D = `MyDeadStinkBug`, T = `ExtremeTorch`, S = String (OreSpawnMain.java:3040).
- **Research-Widerspruch:** 01-mobs.md „protects ~10×10 blocks“. Der Code wirkt in 40×50×40.
- **Portierung:** wie CreeperRepellent. Zweite ID für die Wandvariante nötig. Kraken hat `max_health` 1000 (manifest), ohne Bezug hierher.

### Lavafoam

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `lavafoam` | Lava Foam | `MyLavafoamBlock` | OreSpawnMain.java:1281, registriert 1767 |

- **Rolle:** rutschiger, abprallender Netherblock. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks.
- **Werte:**
  - Härte 5, R 5 → Explosion 3.0 (Lavafoam.java:17-18).
  - `slipperiness = 1.1` (:21). Vanilla-Eis liegt unter 1.0, hier beschleunigt der Block also. Deutung, Eiswert nicht aus der Mod.
  - Zufallsticks an, aber kein `updateTick` (:20). `tickRate()` toter Code (:24-26).
  - Kollisionsbox seitlich um 0.0125 eingezogen, oben voll (:117-120). Deshalb löst das Berühren der Seite `onEntityCollidedWithBlock` aus.
- **Kontakt** `onEntityCollidedWithBlock` (:72-107), nur `EntityLivingBase`:
  1. Winkel `d = atan2(ex − (x+0.5), ez − (z+0.5))`, auf 0..2π normalisiert (:83-86).
  2. Quadranten mit π = 3.14159 (:73-75):
     - um π/2 → `motionX = 0.45`, `motionZ ×= 1.35`
     - um π → `motionZ = −0.45`, `motionX ×= 1.35`
     - um 3π/2 → `motionX = −0.45`, `motionZ ×= 1.35`
     - sonst `motionZ = 0.45`, `motionX ×= 1.35`

     Werte als float-Literale 0.44999998807907104 und 1.350000023841858 (:87-102).
  3. Horizontale Geschwindigkeit > 1.0 → `attackEntityFrom(DamageSource.fall, v)` (:103-106).
- **Partikel:** mit `nextInt(20) == 0` sparkle, 6 Positionen, `nextInt(10)`: 1 → `smoke`, 2 → `reddust` (:29-70).
- **Drops:** sich selbst. XP nur in Dimension −1 (Nether): `5 + nextInt(5) + nextInt(5)` = 5–13 (:109-115).
- **Research-Widerspruch:** 02-dimensions-worldgen.md „0.5 Schaden bei Kontakt“. Der Code macht Fallschaden in Höhe der horizontalen Geschwindigkeit, nur über 1.0.
- **Portierung:**
  - `friction(1.1f)` in den Properties.
  - `getCollisionShape` = `box(0.2,0,0.2,15.8,16,15.8)` (0.0125 × 16 = 0.2).
  - `entityInside` für Stoß und Schaden (`damageSources().fall()`), `setDeltaMovement` + `hurtMarked`.
  - XP über `spawnAfterBreak` nur bei `level.dimension() == Level.NETHER`.

### MoleDirtBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `moledirt` | Molenoid Dirt | `MyMoleDirtBlock` | OreSpawnMain.java:1545 (`setHardness(0.6)`, Gravel-Sound 1.0/1.0) |

- **Rolle:** selbstlöschende Erde, die der Molenoid wirft. Vanilla-Basis `Block`, `Material.ground`, Tab Blocks, Zufallsticks (MoleDirtBlock.java:17-19).
- **Werte:** Härte 0.6, nur Härte → Explosion 0.6. Kollisionsbox oben bei 0.875 (`f = 0.125`, :29-32).
- **Verhalten:**
  - Zufallstick (Server): Block → Luft (Flag 2), ohne Drop (:22-27).
  - Kontakt: `motionX` und `motionZ` × 0.3 für **jede** Entity (:34-39).
- **Drops:** sich selbst beim Abbau (Standard).
- **Portierung:** `randomTick` → `removeBlock`. `getCollisionShape` box(0,0,0,16,14,16). `entityInside` → `setDeltaMovement(v.multiply(0.3,1,0.3))`. Die Entity steht wegen der abgesenkten Box „im“ Block, das entspricht dem Original.

### MyBlockFlower

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `flower_pink` | Pink Flower | `MyFlowerPinkBlock` | OreSpawnMain.java:1614 |
| `flower_blue` | Blue Flower | `MyFlowerBlueBlock` | OreSpawnMain.java:1615 |
| `flower_black` | Black Flower | `MyFlowerBlackBlock` | OreSpawnMain.java:1616 |
| `flower_scary` | Dead Flower | `MyFlowerScaryBlock` | OreSpawnMain.java:1617 |
| `crystalflower_red` | Red Crystal Flower | `CrystalFlowerRedBlock` | OreSpawnMain.java:1623 |
| `crystalflower_green` | Green Crystal Flower | `CrystalFlowerGreenBlock` | OreSpawnMain.java:1624 |
| `crystalflower_blue` | Blue Crystal Flower | `CrystalFlowerBlueBlock` | OreSpawnMain.java:1625 |
| `crystalflower_yellow` | Yellow Crystal Flower | `CrystalFlowerYellowBlock` | OreSpawnMain.java:1626 |

- **Rolle:** Blumen mit Tag/Nacht-Wechsel. Vanilla-Basis `Block implements IPlantable`, `Material.plants`, Tab Decorations. Alle mit Härte 0 und Grass-Sound (Kette).
- **Werte:**
  - Hitbox 0.3–0.7 in x/z, Höhe 0.6 (`f = 0.2`, MyBlockFlower.java:20-21). Keine Kollision (:76-78).
  - Render-Typ 1 (Kreuz) (:88-90). Zufallsticks (:19). Pflanzentyp `Plains` (:97-99).
- **Halt:** `canBlockStay` = Block darunter `canSustainPlant(UP, this)` (:72-74). Deutung: Vanilla-Forge `Plains` → Gras, Erde, Farmland. Crystal Grass immer.
  - `canPlaceBlockAt` = Standard ∧ `canBlockStay` (:29-31).
  - `canPlaceBlockOn(Block)` (:33-35) ist in `Block` keine Methode (nur in `BlockBush`) → toter Code.
- **`checkFlowerChange`**, bei Zufallstick und Nachbaränderung (:37-70):
  1. Kein Halt → Drop mit Meta, Luft (Flag 2) (:47-51).
  2. `t = worldTime % 24000`. Bei `t > 12000` (Nacht): Pink → Black, Blue → Scary. Sonst: Black → Pink, Scary → Blue (:52-69). `setBlock` ohne Flag = 3.
  3. Crystal-Blumen wechseln nie.
- **Drops:** sich selbst.
- **Portierung:**
  - `BushBlock`-Unterklasse (Guardrail-konform) mit Shape box(4.8,0,4.8,11.2,9.6,11.2), `randomTicks`, cutout-Kreuzmodell.
  - `mayPlaceOn`: Gras/Erde/Farmland plus `crystalgrass`.
  - Tageszeit über `level.getDayTime() % 24000`. Wechsel mit `setBlock(..., 3)`.

### OreAmethyst

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `oreamethyst` | Amethyst Ore | `MyOreAmethystBlock` | OreSpawnMain.java:1522, registriert 1770 |

- **Rolle:** Erz. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks.
- **Werte:** Härte 10, R 4 → Explosion 2.4 (OreAmethyst.java:16-17).
- **Drops:**
  - Item `amethyst` (:27-29).
  - Menge `quantityDroppedWithBonus` = `1 + nextInt(2)` = 1–2, **unabhängig von Fortune** (:31-33). Das 1.7.10-`Block.quantityDropped(meta,fortune,rand)` ruft diese Methode immer, `quantityDropped(Random) = 1` (:35-37) wird nicht benutzt.
  - XP immer `5 + nextInt(5) + nextInt(5)` = 5–13 (:21-25), auch bei Explosionen, weil `dropBlockAsItemWithChance` dort ebenfalls läuft.
- **Portierung:** Loot-Table `amethyst` uniform(1,2) ohne Fortune-Funktion, Silk Touch → Block (Vanilla-Konvention, im Original nicht belegt). XP über `DropExperienceBlock(UniformInt.of(5,13))`, dessen Verteilung von der Summe zweier Würfel abweicht. 1:1 → eigene `spawnAfterBreak`.

### OreBasicStone

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalstone` | Kyanite | `CrystalStone` | OreSpawnMain.java:1526, `(…, 2.0f, 10.0f)` |
| `crystalrat` | Crystalized Rats | `CrystalRat` | OreSpawnMain.java:1537, `(…, 2.5f, 14.0f)` |
| `crystalfairy` | Crystalized Fairies | `CrystalFairy` | OreSpawnMain.java:1538, `(…, 2.5f, 14.0f)` |
| `redanttroll` | Red Ant Troll Block | `RedAntTroll` | OreSpawnMain.java:1539, `(…, 2.5f, 14.0f)` |
| `termitetroll` | Termite Troll Block | `TermiteTroll` | OreSpawnMain.java:1540, `(…, 2.5f, 14.0f)` |

- **Rolle:** Grundgestein der Crystal Dimension plus „Troll“-Erze, die beim Abbau Mobs freisetzen. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks, keine Zufallsticks (OreBasicStone.java:15-19).
- **Werte:** Kyanite Härte 2, R 10 → Explosion 6.0. Die vier anderen Härte 2.5, R 14 → Explosion 8.4.
- **Verhalten** `onBlockDestroyedByPlayer` (:22-44), nur Server, **ohne Zufallsprüfung und ohne Config-Abfrage**:
  - `crystalrat`: `1 + nextInt(10)` = 1–10 `"Rat"` (:23-27)
  - `crystalfairy`: `1 + nextInt(6)` = 1–6 `"Fairy"` (:28-32)
  - `redanttroll`: `15 + nextInt(6)` = 15–20 `"Red Ant"` (:33-37)
  - `termitetroll`: `15 + nextInt(6)` = 15–20 `"Termite"` (:38-42)
  - Position je (x+0.5 ± 0.2·(r−r), y+0.01, z+0.5 ± 0.2·(r−r)), Yaw zufällig, `playLivingSound` (:58-72).
  - Läuft auch im Kreativmodus.
- **Opazität:** nur in `DimensionID5` (Client-Static) (:50-56). `isBlockSolidOnSide` toter Code (:46-48).
- **Drops:** sich selbst.
- **Rezeptbezug:**
  - `crystalfurnace` aus 8 `crystalstone` (OreSpawnMain.java:2741), Kyanite-Werkzeuge (2903-2911).
  - Rat Sword aus `crystalrat` (2848-2850), Fairy Sword aus `crystalfairy` (2851-2853).
- **Research-Widerspruch:** 02-dimensions-worldgen.md „Zufall 1/10 im Code“ für Rats. Der Code spawnt immer 1–10.
- **Portierung:** voller opaker Würfel. Spawn in `playerDestroy` bzw. `onDestroyedByPlayer` (auch Kreativ, 1:1) oder `playerWillDestroy`. Kyanite-Tag `mineable/pickaxe`.

### OreCrystal

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalcoal` | Crystal Energy | `CrystalCoal` | OreSpawnMain.java:1527, `(…, 0.6f, 6.0f, 20.0f)` |

- **Rolle:** Kristall-Kohle, leuchtend und explosiv. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks, Zufallsticks ohne `updateTick` (OreCrystal.java:14-21).
- **Werte:**
  - Licht 0.6 → 9. Härte 6, R 20 → Explosion 12.0.
  - **Render-Typ 1 (Kreuz)** (:49-51), nicht opak (:53-59). Der Block rendert als X-Sprite, kollidiert aber als voller Würfel.
- **Abbau** `onBlockDestroyedByPlayer` (:61-66): mit `nextInt(3) == 1` (1/3) `newExplosion(null, Mitte, 1.5f, flaming = true, smoking = mobGriefing)`.
- **Drops:** sich selbst. XP nur bei y < 40: `5 + nextInt(5) + nextInt(10)` = 5–18 (:68-74).
- **Partikel:** mit `nextInt(5) == 0` 5 Partikel aus der Blockmitte, zufällig `flame`, `smoke` oder `reddust`, Geschwindigkeit je Achse (r−r)/4 (:24-47).
- **Brennwert:** im Kristallofen 20000 (TileEntityCrystalFurnace.java:262-264). Die Mod registriert keinen `IFuelHandler` (grep leer), im Vanilla-Ofen also 0.
- **Rezeptbezug:** 1 Crystal Energy + Crystal Sticks → 6 Crystal Torches (OreSpawnMain.java:3036).
- **Research:** 02-dimensions-worldgen.md „Wahrscheinlichkeit nicht ausgewertet“ → hier 1/3.
- **Portierung:**
  - Modell `minecraft:block/cross` mit cutout, `getCollisionShape` voll.
  - `level.explode(null, x,y,z, 1.5f, true, Level.ExplosionInteraction.MOB)`. MOB bindet an mobGriefing, Deutung gleichwertig.
  - Kein Vanilla-Brennwert (keine furnace_fuels-Data-Map). Der Kristallofen braucht eine eigene Tabelle.

### OreCrystalCrystal

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `crystalcrystal` | Pink Tourmaline | `CrystalCrystal` | OreSpawnMain.java:1529, `(…, 0.4f, 12.0f, 40.0f)` |
| `tigerseye` | Tiger's Eye | `TigersEye` | OreSpawnMain.java:1530, `(…, 0.5f, 15.0f, 60.0f)` |

- **Rolle:** Edelstein-Erze der Crystal Dimension. Vanilla-Basis `Block`, `Material.rock`, Render-Typ 1 (Kreuz), nicht opak.
- **Werte:**
  - Pink Tourmaline: Licht 6, Härte 12, R 40 → Explosion 24.0.
  - Tiger's Eye: Licht 7, Härte 15, R 60 → Explosion 36.0.
  - Quelle: OreCrystalCrystal.java:14-21, 43-53.
- **Abbau:** nur `crystalcrystal`, mit `nextInt(10) == 1` (1/10) Explosion 1.0, flaming true, smoking = mobGriefing (:55-60).
- **Drops:** sich selbst. `crystalcrystal` 1, `tigerseye` `nextInt(2)` = 0–1 (:70-75). XP bei y < 40: 5–18 (:62-68).
- **Partikel:** mit `nextInt(20) == 0` genau 1 Partikel. Tiger's Eye `flame`, sonst `fireworksSpark`, aus der Mitte mit (r−r)/4 (:24-41).
- **Rezeptbezug:** Schmelzen `crystalcrystal` → Pink-Tourmaline-Barren, 0.3 XP (OreSpawnMain.java:2763). `tigerseye` → Tiger's-Eye-Barren, 0.3 XP (2765).
- **Portierung:** wie OreCrystal. Loot-Table für `tigerseye` mit `set_count uniform(0,1)`. `fireworksSpark` → `ParticleTypes.FIREWORK`.

### OreGenericEgg

121 Registry-IDs (manifest `blocks`, `class == OreGenericEgg`, `kind` `dried_egg_ore`). Die Tabelle steht am Ende dieses Abschnitts.

- **Rolle:**
  - Ancient Dried Spawn Eggs: erzartige Blöcke, die über Rezepte zu Spawn-Eiern werden.
  - Dieselbe Klasse dient als Speicherblock für Ender-Pearl- und Eye-of-Ender-Block.
  - Vanilla-Basis `Block`, `Material.ground`, Gravel-Sound, Tab Blocks (OreGenericEgg.java:13-17).
- **Werte:** Härte 0.5, R 1 → Explosion 0.6 (:14-15). Opak, Normal-Rendering (:28-34).
- **Drops:** sich selbst (keine Overrides). XP mit `nextInt(2) == 1` (50 %): `5 + nextInt(3) + nextInt(3)` = 5–9 (:20-26).
- **Kein Spawn-Verhalten im Block.** Die Mechanik steckt in Rezepten in `OreSpawnMain`:
  - Wassereimer + Dried Egg → Spawn-Ei (ab OreSpawnMain.java:2326, z. B. Spider → `spawn_egg` Meta 52).
  - 9 Teile → volles Ei: Mobzilla (2545), King (2551), Queen (2557).
  - 9 Ender Pearls ↔ `blockenderpearl` (2970-2971), 9 Eyes of Ender ↔ `blockeyeofender` (2972-2973). `BlockExtremeTorch` prüft `blockeyeofender` darunter (BlockExtremeTorch.java:64).
- **Weltgenerierung:** `ChunkOreGenerator`, `OreSpawnWorld` (anderer Stapel).
- **Portierung:**
  - Eine Klasse, 121 Registrierungen per Schleife aus einer ID-Liste.
  - `DropExperienceBlock` reicht nicht (50-%-Wurf) → eigenes `spawnAfterBreak`. Tag `mineable/shovel`.
  - Die Spawn-Ei-Rezepte werden Datapack-Rezepte. Vanilla-Spawn-Eier sind in 1.21 eigene Items (`minecraft:spider_spawn_egg`), die Meta-Zuordnung braucht eine Tabelle, offen.

Registry-IDs der Klasse OreGenericEgg (manifest):

| # | Registry-ID | Lang-Name | Feld (OreSpawnMain) | ctor-Arg (manifest) | Legacy-Name |
|---|---|---|---|---|---|
| 1 | `blockenderpearl` | Ender-Pearl Block | `MyEnderPearlBlock` | 2811 | `OreSpawn_EnderPearlBlock` |
| 2 | `blockeyeofender` | Eye-of-Ender Block | `MyEyeOfEnderBlock` | 2812 | `OreSpawn_EyeOfEnderBlock` |
| 3 | `orespider` | Ancient Dried Spider Spawn Egg | `MySpiderSpawnBlock` | (OreSpawnMain.BaseBlockID + 0) | `OreSpawn_SpiderSpawnBlock` |
| 4 | `orebat` | Ancient Dried Bat Spawn Egg | `MyBatSpawnBlock` | 2701 | `OreSpawn_BatSpawnBlock` |
| 5 | `orecow` | Ancient Dried Cow Spawn Egg | `MyCowSpawnBlock` | 2702 | `OreSpawn_CowSpawnBlock` |
| 6 | `orepig` | Ancient Dried Pig Spawn Egg | `MyPigSpawnBlock` | 2703 | `OreSpawn_PigSpawnBlock` |
| 7 | `oresquid` | Ancient Dried Squid Spawn Egg | `MySquidSpawnBlock` | 2704 | `OreSpawn_SquidSpawnBlock` |
| 8 | `orechicken` | Ancient Dried Chicken Spawn Egg | `MyChickenSpawnBlock` | 2705 | `OreSpawn_ChickenSpawnBlock` |
| 9 | `orecreeper` | Ancient Dried Creeper Spawn Egg | `MyCreeperSpawnBlock` | 2706 | `OreSpawn_CreeperSpawnBlock` |
| 10 | `oreskeleton` | Ancient Dried Skeleton Spawn Egg | `MySkeletonSpawnBlock` | 2707 | `OreSpawn_SkeletonSpawnBlock` |
| 11 | `orezombie` | Ancient Dried Zombie Spawn Egg | `MyZombieSpawnBlock` | 2708 | `OreSpawn_ZombieSpawnBlock` |
| 12 | `oreslime` | Ancient Dried Slime Spawn Egg | `MySlimeSpawnBlock` | 2709 | `OreSpawn_SlimeSpawnBlock` |
| 13 | `oreghast` | Ancient Dried Ghast Spawn Egg | `MyGhastSpawnBlock` | 2710 | `OreSpawn_GhastSpawnBlock` |
| 14 | `orezombiepigman` | Ancient Dried Zombie Pigman Spawn Egg | `MyZombiePigmanSpawnBlock` | 2711 | `OreSpawn_ZombiePigmanSpawnBlock` |
| 15 | `oreenderman` | Ancient Dried Enderman Spawn Egg | `MyEndermanSpawnBlock` | 2712 | `OreSpawn_EndermanSpawnBlock` |
| 16 | `orecavespider` | Ancient Dried Cave Spider Spawn Egg | `MyCaveSpiderSpawnBlock` | 2713 | `OreSpawn_CaveSpiderSpawnBlock` |
| 17 | `oresilverfish` | Ancient Dried Silverfish Spawn Egg | `MySilverfishSpawnBlock` | 2714 | `OreSpawn_SilverfishSpawnBlock` |
| 18 | `oremagmacube` | Ancient Dried Magma Cube Spawn Egg | `MyMagmaCubeSpawnBlock` | 2715 | `OreSpawn_MagmaCubeSpawnBlock` |
| 19 | `orewitch` | Ancient Dried Witch Spawn Egg | `MyWitchSpawnBlock` | 2716 | `OreSpawn_WitchSpawnBlock` |
| 20 | `oresheep` | Ancient Dried Sheep Spawn Egg | `MySheepSpawnBlock` | 2717 | `OreSpawn_SheepSpawnBlock` |
| 21 | `orewolf` | Ancient Dried Wolf Spawn Egg | `MyWolfSpawnBlock` | 2718 | `OreSpawn_WolfSpawnBlock` |
| 22 | `oremooshroom` | Ancient Dried Mooshroom Spawn Egg | `MyMooshroomSpawnBlock` | 2719 | `OreSpawn_MooshroomSpawnBlock` |
| 23 | `oreocelot` | Ancient Dried Ocelot Spawn Egg | `MyOcelotSpawnBlock` | 2720 | `OreSpawn_OcelotSpawnBlock` |
| 24 | `oreblaze` | Ancient Dried Blaze Spawn Egg | `MyBlazeSpawnBlock` | 2721 | `OreSpawn_BlazeSpawnBlock` |
| 25 | `orewitherskeleton` | Ancient Dried Wither Skeleton Spawn Egg | `MyWitherSkeletonSpawnBlock` | 2722 | `OreSpawn_WitherSkeletonSpawnBlock` |
| 26 | `oreenderdragon` | Ancient Dried Ender Dragon Spawn Egg | `MyEnderDragonSpawnBlock` | 2723 | `OreSpawn_EnderDragonSpawnBlock` |
| 27 | `oresnowgolem` | Ancient Dried Snow Golem Spawn Egg | `MySnowGolemSpawnBlock` | 2724 | `OreSpawn_SnowGolemSpawnBlock` |
| 28 | `oreirongolem` | Ancient Dried Iron Golem Spawn Egg | `MyIronGolemSpawnBlock` | 2725 | `OreSpawn_IronGolemSpawnBlock` |
| 29 | `orewitherboss` | Ancient Dried Wither Boss Spawn Egg | `MyWitherBossSpawnBlock` | 2726 | `OreSpawn_WitherBossSpawnBlock` |
| 30 | `oregirlfriend` | Ancient Dried Girlfriend Spawn Egg | `MyGirlfriendSpawnBlock` | 2727 | `OreSpawn_GirlfriendSpawnBlock` |
| 31 | `oreboyfriend` | Ancient Dried Boyfriend Spawn Egg | `MyBoyfriendSpawnBlock` | 2784 | `OreSpawn_BoyfriendSpawnBlock` |
| 32 | `oreredcow` | Ancient Dried Apple Cow Spawn Egg | `MyRedCowSpawnBlock` | 2728 | `OreSpawn_RedCowSpawnBlock` |
| 33 | `orecrystalcow` | Ancient Dried Crystal Cow Spawn Egg | `MyCrystalCowSpawnBlock` | 2961 | `OreSpawn_CrystalCowSpawnBlock` |
| 34 | `orevillager` | Ancient Dried Villager Spawn Egg | `MyVillagerSpawnBlock` | 2794 | `OreSpawn_VillagerSpawnBlock` |
| 35 | `oregoldcow` | Ancient Dried Golden Apple Cow Spawn Egg | `MyGoldCowSpawnBlock` | 2729 | `OreSpawn_GoldCowSpawnBlock` |
| 36 | `oreenchantedcow` | Ancient Dried Enchanted Golden Apple Cow Spawn Egg | `MyEnchantedCowSpawnBlock` | 2730 | `OreSpawn_EnchantedCowSpawnBlock` |
| 37 | `oremothra` | Ancient Dried MOTHRA Spawn Egg | `MyMOTHRASpawnBlock` | 2731 | `OreSpawn_MOTHRASpawnBlock` |
| 38 | `orealosaurus` | Ancient Dried Alosaurus Spawn Egg | `MyAloSpawnBlock` | 2732 | `OreSpawn_AloSpawnBlock` |
| 39 | `orecryolophosaurus` | Ancient Dried Cryolophosaurus Spawn Egg | `MyCryoSpawnBlock` | 2733 | `OreSpawn_CryoSpawnBlock` |
| 40 | `orecamarasaurus` | Ancient Dried Camarasaurus Spawn Egg | `MyCamaSpawnBlock` | 2734 | `OreSpawn_CamaSpawnBlock` |
| 41 | `orevelocityraptor` | Ancient Dried Velocity Raptor Spawn Egg | `MyVeloSpawnBlock` | 2735 | `OreSpawn_VeloSpawnBlock` |
| 42 | `orehydrolisc` | Ancient Dried Hydrolisc Spawn Egg | `MyHydroSpawnBlock` | 2736 | `OreSpawn_HydroSpawnBlock` |
| 43 | `orebasilisc` | Ancient Dried Basilisk Spawn Egg | `MyBasilSpawnBlock` | 2737 | `OreSpawn_BasilSpawnBlock` |
| 44 | `oredragonfly` | Ancient Dried Dragonfly Spawn Egg | `MyDragonflySpawnBlock` | 2738 | `OreSpawn_DragonflySpawnBlock` |
| 45 | `oreemperorscorpion` | Ancient Dried Emperor Scorpion Spawn Egg | `MyEmperorScorpionSpawnBlock` | 2739 | `OreSpawn_EmperorScorpionSpawnBlock` |
| 46 | `orescorpion` | Ancient Dried Scorpion Spawn Egg | `MyScorpionSpawnBlock` | 2740 | `OreSpawn_ScorpionSpawnBlock` |
| 47 | `orecavefisher` | Ancient Dried Cave Fisher Spawn Egg | `MyCaveFisherSpawnBlock` | 2741 | `OreSpawn_CaveFisherSpawnBlock` |
| 48 | `orespyro` | Ancient Dried Baby Dragon Spawn Egg | `MySpyroSpawnBlock` | 2742 | `OreSpawn_SpyroSpawnBlock` |
| 49 | `orebaryonyx` | Ancient Dried Baryonyx Spawn Egg | `MyBaryonyxSpawnBlock` | 2743 | `OreSpawn_BaryonyxSpawnBlock` |
| 50 | `oregammametroid` | Ancient Dried WTF? Spawn Egg | `MyGammaMetroidSpawnBlock` | 2744 | `OreSpawn_GammaMetroidSpawnBlock` |
| 51 | `orecockateil` | Ancient Dried Bird Spawn Egg | `MyCockateilSpawnBlock` | 2745 | `OreSpawn_CockateilSpawnBlock` |
| 52 | `orekyuubi` | Ancient Dried Kyuubi Spawn Egg | `MyKyuubiSpawnBlock` | 2746 | `OreSpawn_KyuubiSpawnBlock` |
| 53 | `orealien` | Ancient Dried Alien Spawn Egg | `MyAlienSpawnBlock` | 2747 | `OreSpawn_AlienSpawnBlock` |
| 54 | `oreattacksquid` | Ancient Dried Attack Squid Spawn Egg | `MyAttackSquidSpawnBlock` | 2748 | `OreSpawn_AttackSquidSpawnBlock` |
| 55 | `orewaterdragon` | Ancient Dried WaterDragon Spawn Egg | `MyWaterDragonSpawnBlock` | 2749 | `OreSpawn_WaterDragonSpawnBlock` |
| 56 | `orecephadrome` | Ancient Dried Cephadrome Spawn Egg | `MyCephadromeSpawnBlock` | 2752 | `OreSpawn_CephadromeSpawnBlock` |
| 57 | `oredragon` | Ancient Dried Dragon Spawn Egg | `MyDragonSpawnBlock` | 2753 | `OreSpawn_DragonSpawnBlock` |
| 58 | `orekraken` | Ancient Dried Kraken Spawn Egg | `MyKrakenSpawnBlock` | 2750 | `OreSpawn_KrakenSpawnBlock` |
| 59 | `orelizard` | Ancient Dried Lizard Spawn Egg | `MyLizardSpawnBlock` | 2751 | `OreSpawn_LizardSpawnBlock` |
| 60 | `orebee` | Ancient Dried Bee Spawn Egg | `MyBeeSpawnBlock` | 2754 | `OreSpawn_BeeSpawnBlock` |
| 61 | `orehorse` | Ancient Dried Horse Spawn Egg | `MyHorseSpawnBlock` | 2755 | `OreSpawn_HorseSpawnBlock` |
| 62 | `oretrooper` | Ancient Dried Jumpy Bug Spawn Egg | `MyTrooperBugSpawnBlock` | 2756 | `OreSpawn_TrooperBugSpawnBlock` |
| 63 | `orespit` | Ancient Dried Spit Bug Spawn Egg | `MySpitBugSpawnBlock` | 2757 | `OreSpawn_SpitBugSpawnBlock` |
| 64 | `orestink` | Ancient Dried Stink Bug Spawn Egg | `MyStinkBugSpawnBlock` | 2758 | `OreSpawn_StinkBugSpawnBlock` |
| 65 | `oreostrich` | Ancient Dried Ostrich Spawn Egg | `MyOstrichSpawnBlock` | 2759 | `OreSpawn_OstrichSpawnBlock` |
| 66 | `oregazelle` | Ancient Dried Gazelle Spawn Egg | `MyGazelleSpawnBlock` | 2760 | `OreSpawn_GazelleSpawnBlock` |
| 67 | `orechipmunk` | Ancient Dried Chipmunk Spawn Egg | `MyChipmunkSpawnBlock` | 2761 | `OreSpawn_ChipmunkSpawnBlock` |
| 68 | `orecreepinghorror` | Ancient Dried Creeping Horror Spawn Egg | `MyCreepingHorrorSpawnBlock` | 2762 | `OreSpawn_CreepingHorrorSpawnBlock` |
| 69 | `oreterribleterror` | Ancient Dried Terrible Terror Spawn Egg | `MyTerribleTerrorSpawnBlock` | 2763 | `OreSpawn_TerribleTerrorSpawnBlock` |
| 70 | `orecliffracer` | Ancient Dried Cliff Racer Spawn Egg | `MyCliffRacerSpawnBlock` | 2764 | `OreSpawn_CliffRacerSpawnBlock` |
| 71 | `oretriffid` | Ancient Dried Triffid Spawn Egg | `MyTriffidSpawnBlock` | 2765 | `OreSpawn_TriffidSpawnBlock` |
| 72 | `orenightmare` | Ancient Dried Nightmare Spawn Egg | `MyPitchBlackSpawnBlock` | 2766 | `OreSpawn_PitchBlackSpawnBlock` |
| 73 | `orelurkingterror` | Ancient Dried Lurking Terror Spawn Egg | `MyLurkingTerrorSpawnBlock` | 2767 | `OreSpawn_LurkingTerrorSpawnBlock` |
| 74 | `oregodzillapart` | Ancient Dried Mobzilla Spawn Egg Part | `MyGodzillaPartSpawnBlock` | 2768 | `OreSpawn_GodzillaPartSpawnBlock` |
| 75 | `oregodzilla` | Ancient Dried Mobzilla Spawn Egg | `MyGodzillaSpawnBlock` | 2819 | `OreSpawn_GodzillaSpawnBlock` |
| 76 | `oresmallworm` | Ancient Dried Small Worm Spawn Egg | `MySmallWormSpawnBlock` | 2769 | `OreSpawn_SmallWormSpawnBlock` |
| 77 | `oremediumworm` | Ancient Dried Medium Worm Spawn Egg | `MyMediumWormSpawnBlock` | 2770 | `OreSpawn_MediumWormSpawnBlock` |
| 78 | `orelargeworm` | Ancient Dried Large Worm Spawn Egg | `MyLargeWormSpawnBlock` | 2771 | `OreSpawn_LargeWormSpawnBlock` |
| 79 | `orecassowary` | Ancient Dried Cassowary Spawn Egg | `MyCassowarySpawnBlock` | 2772 | `OreSpawn_CassowarySpawnBlock` |
| 80 | `orecloudshark` | Ancient Dried Cloud Shark Spawn Egg | `MyCloudSharkSpawnBlock` | 2773 | `OreSpawn_CloudSharkSpawnBlock` |
| 81 | `oregoldfish` | Ancient Dried Gold Fish Spawn Egg | `MyGoldFishSpawnBlock` | 2774 | `OreSpawn_GoldFishSpawnBlock` |
| 82 | `oreleafmonster` | Ancient Dried Leaf Monster Spawn Egg | `MyLeafMonsterSpawnBlock` | 2775 | `OreSpawn_LeafMonsterSpawnBlock` |
| 83 | `oretshirt` | Ancient Dried T-Shirt Spawn Egg | `MyTshirtSpawnBlock` | 2776 | `OreSpawn_TshirtSpawnBlock` |
| 84 | `oreenderknight` | Ancient Dried Ender Knight Spawn Egg | `MyEnderKnightSpawnBlock` | 2777 | `OreSpawn_EnderKnightSpawnBlock` |
| 85 | `oreenderreaper` | Ancient Dried Ender Reaper Spawn Egg | `MyEnderReaperSpawnBlock` | 2778 | `OreSpawn_EnderReaperSpawnBlock` |
| 86 | `orebeaver` | Ancient Dried Beaver Spawn Egg | `MyBeaverSpawnBlock` | 2779 | `OreSpawn_BeaverSpawnBlock` |
| 87 | `oreurchin` | Ancient Dried Urchin Spawn Egg | `MyUrchinSpawnBlock` | 2950 | `OreSpawn_UrchinSpawnBlock` |
| 88 | `oreflounder` | Ancient Dried Flounder Spawn Egg | `MyFlounderSpawnBlock` | 2951 | `OreSpawn_FlounderSpawnBlock` |
| 89 | `oreskate` | Ancient Dried Skate Spawn Egg | `MySkateSpawnBlock` | 2952 | `OreSpawn_SkateSpawnBlock` |
| 90 | `orerotator` | Ancient Dried Rotator Spawn Egg | `MyRotatorSpawnBlock` | 2953 | `OreSpawn_RotatorSpawnBlock` |
| 91 | `orepeacock` | Ancient Dried Peacock Spawn Egg | `MyPeacockSpawnBlock` | 2954 | `OreSpawn_PeacockSpawnBlock` |
| 92 | `orefairy` | Ancient Dried Fairy Spawn Egg | `MyFairySpawnBlock` | 2955 | `OreSpawn_FairySpawnBlock` |
| 93 | `oredungeonbeast` | Ancient Dried Dungeon Beast Spawn Egg | `MyDungeonBeastSpawnBlock` | 2956 | `OreSpawn_DungeonBeastSpawnBlock` |
| 94 | `orevortex` | Ancient Dried Vortex Spawn Egg | `MyVortexSpawnBlock` | 2957 | `OreSpawn_VortexSpawnBlock` |
| 95 | `orerat` | Ancient Dried Rat Spawn Egg | `MyRatSpawnBlock` | 2958 | `OreSpawn_RatSpawnBlock` |
| 96 | `orewhale` | Ancient Dried Whale Spawn Egg | `MyWhaleSpawnBlock` | 2959 | `OreSpawn_WhaleSpawnBlock` |
| 97 | `oreirukandji` | Ancient Dried Irukandji Spawn Egg | `MyIrukandjiSpawnBlock` | 2960 | `OreSpawn_IrukandjiSpawnBlock` |
| 98 | `oretrex` | Ancient Dried T. Rex Spawn Egg | `MyTRexSpawnBlock` | 2780 | `OreSpawn_TRexSpawnBlock` |
| 99 | `orehercules` | Ancient Dried Hercules Beetle Spawn Egg | `MyHerculesSpawnBlock` | 2781 | `OreSpawn_HerculesSpawnBlock` |
| 100 | `oremantis` | Ancient Dried Mantis Spawn Egg | `MyMantisSpawnBlock` | 2782 | `OreSpawn_MantisSpawnBlock` |
| 101 | `orestinky` | Ancient Dried Stinky Spawn Egg | `MyStinkySpawnBlock` | 2783 | `OreSpawn_StinkySpawnBlock` |
| 102 | `orethekingpart` | Ancient Dried The King Spawn Egg Part | `MyTheKingPartSpawnBlock` | 2785 | `OreSpawn_TheKingPartSpawnBlock` |
| 103 | `oretheking` | Ancient Dried The King Spawn Egg | `MyTheKingSpawnBlock` | 2822 | `OreSpawn_TheKingSpawnBlock` |
| 104 | `orethequeenpart` | Ancient Dried The Queen Spawn Egg Part | `MyTheQueenPartSpawnBlock` | 2796 | `OreSpawn_TheQueenPartSpawnBlock` |
| 105 | `orethequeen` | Ancient Dried The Queen Spawn Egg | `MyTheQueenSpawnBlock` | 2825 | `OreSpawn_TheQueenSpawnBlock` |
| 106 | `oreeasterbunny` | Ancient Dried Easter Bunny Spawn Egg | `MyEasterBunnySpawnBlock` | 2786 | `OreSpawn_EasterBunnySpawnBlock` |
| 107 | `orecaterkiller` | Ancient Dried CaterKiller Spawn Egg | `MyCaterKillerSpawnBlock` | 2787 | `OreSpawn_CaterKillerSpawnBlock` |
| 108 | `oremolenoid` | Ancient Dried Molenoid Spawn Egg | `MyMolenoidSpawnBlock` | 2788 | `OreSpawn_MolenoidSpawnBlock` |
| 109 | `oreseamonster` | Ancient Dried Sea Monster Spawn Egg | `MySeaMonsterSpawnBlock` | 2789 | `OreSpawn_SeaMonsterSpawnBlock` |
| 110 | `oreseaviper` | Ancient Dried Sea Viper Spawn Egg | `MySeaViperSpawnBlock` | 2790 | `OreSpawn_SeaViperSpawnBlock` |
| 111 | `oreleon` | Ancient Dried Leonopteryx Spawn Egg | `MyLeonSpawnBlock` | 2791 | `OreSpawn_LeonSpawnBlock` |
| 112 | `orehammerhead` | Ancient Dried Hammerhead Spawn Egg | `MyHammerheadSpawnBlock` | 2792 | `OreSpawn_HammerheadSpawnBlock` |
| 113 | `orerubberducky` | Ancient Dried Rubber Ducky Spawn Egg | `MyRubberDuckySpawnBlock` | 2793 | `OreSpawn_RubberDuckySpawnBlock` |
| 114 | `orecriminal` | Ancient Dried Criminal Spawn Egg | `MyCriminalSpawnBlock` | 2795 | `OreSpawn_CriminalSpawnBlock` |
| 115 | `orebrutalfly` | Ancient Dried Brutalfly Spawn Egg | `MyBrutalflySpawnBlock` | 3000 | `OreSpawn_BrutalflySpawnBlock` |
| 116 | `orenastysaurus` | Ancient Dried Nastysaurus Spawn Egg | `MyNastysaurusSpawnBlock` | 3001 | `OreSpawn_NastysaurusSpawnBlock` |
| 117 | `orepointysaurus` | Ancient Dried Pointysaurus Spawn Egg | `MyPointysaurusSpawnBlock` | 3002 | `OreSpawn_PointysaurusSpawnBlock` |
| 118 | `orecricket` | Ancient Dried Cricket Spawn Egg | `MyCricketSpawnBlock` | 3003 | `OreSpawn_CricketSpawnBlock` |
| 119 | `orefrog` | Ancient Dried Frog Spawn Egg | `MyFrogSpawnBlock` | 3004 | `OreSpawn_FrogSpawnBlock` |
| 120 | `orespiderdriver` | Ancient Dried Spider Driver Spawn Egg | `MySpiderDriverSpawnBlock` | 3005 | `OreSpawn_SpiderDriverSpawnBlock` |
| 121 | `orecrab` | Ancient Dried Crab Spawn Egg | `MyCrabSpawnBlock` | 3006 | `OreSpawn_CrabSpawnBlock` |

### OreRuby

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `oreruby` | Ruby Ore | `MyOreRubyBlock` | OreSpawnMain.java:1520, registriert 1768 |

- **Rolle/Werte/Drops:** zeilengleich mit `OreAmethyst`, Drop-Item `ruby` (OreRuby.java:27-29).
  - Härte 10, R 4 → Explosion 2.4 (:16-17).
  - Menge `1 + nextInt(2)` ohne Fortune (:31-33).
  - XP immer 5–13 (:21-25).
- **Rezeptbezug:** 9 Ruby ↔ `blockruby` (OreSpawnMain.java:2962-2963).
- **Portierung:** wie OreAmethyst, gemeinsame Klasse mit Drop-Item-Parameter.

### OreSalt

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `oresalt` | Salt Ore | `MyOreSaltBlock` | OreSpawnMain.java:1502, registriert 1759 |

- **Rolle:** Salz-Erz, das Ameisen verletzt. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks.
- **Werte:** Härte 5, R 2 → Explosion 1.2 (OreSalt.java:16-17).
- **Verhalten:** `onEntityCollidedWithBlock` und `onEntityWalking`: jede `EntityAnt` (inkl. Termite, Red, Rainbow, Unstable) nimmt `DamageSource.cactus` 5.0 (:21-31).
- **Drops:** sich selbst.
- **Rezeptbezug:** Schmelzen → 8 `MySalt`, 0.1 XP (OreSpawnMain.java:2755).
- **Portierung:** `stepOn` + `entityInside`. `damageSources().cactus()`. Ziel-Prüfung `instanceof` der portierten Ameisen-Basisklasse oder Entity-Tag.

### OreTitanium

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `oretitanium` | Titanium Ore | `MyOreTitaniumBlock` | OreSpawnMain.java:1275, registriert 1762 |

- **Rolle:** Titan-Erz mit „Aufglühen“ bei Berührung. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks.
- **Werte:** Härte 15, R 5 → Explosion 3.0 (OreTitanium.java:22-23). Zufallsticks an, `updateTick` leer (:25, 54-55). `tickRate()` = 30 toter Code (:29-31).
- **Glühen:**
  - `glowing` und `glowcount` sind **Instanzfelder des Block-Singletons** (:15-16). Sie gelten also für alle Titan-Erze gleichzeitig und getrennt je Seite (Client/Server).
  - `glow()` setzt `glowing = true`, `glowcount = 5` und spawnt sofort sparkle (:48-52). Auslöser: `onBlockClicked` (:33-36), `onEntityWalking` (:38-41), `onBlockActivated` (:43-46, gibt `super` = false zurück, Rechtsklick geht also durch).
  - `randomDisplayTick` (Client): solange `glowing`, sparkle und `glowcount` −1, bei 0 `glowing = false` (:58-68).
  - Sparkle: 6 Positionen wie BlockTitanium, nur `reddust` (:70-99).
  - Serverseitige `spawnParticle` sind in 1.7.10 wirkungslos. Deutung: `WorldServer` leitet Partikel nicht weiter.
- **Drops:** sich selbst. XP bei y < 40: 5–18 (:101-107).
- **Rezeptbezug:** Schmelzen → `TitaniumNugget`, 0.3 XP (OreSpawnMain.java:2753).
- **Research-Widerspruch:** keiner zu den Werten.
- **Portierung:**
  - Client-only Effekt: `attack` bzw. `stepOn` rufen auf dem Client einen statischen Zähler je Blocktyp. `animateTick` liest ihn.
  - Der Guardrail „client-only code unter `client`“ gilt: Zähler in eine Client-Klasse, Aufruf per `level.isClientSide`-Weiche.

### OreUranium

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `oreuranium` | Uranium Ore | `MyOreUraniumBlock` | OreSpawnMain.java:1274, registriert 1763 |

- **Rolle:** wie OreTitanium.
- **Werte:** Härte 10, R 1 → Explosion 0.6 (OreUranium.java:22-23). `glowcount = 10` (:50). Sonst zeilengleich (:29-107), XP bei y < 40: 5–18.
- **Rezeptbezug:** Schmelzen → `UraniumNugget`, 0.3 XP (OreSpawnMain.java:2751).
- **Portierung:** gemeinsame Klasse mit OreTitanium, Parameter glowcount.

### PortalBlock

- **Registry-IDs:** keine. Weder im Manifest noch per grep instanziiert (Suche nach `PortalBlock` über alle `*.java` außerhalb der Klasse: 0 Treffer).
- **Rolle:** unbenutzter `BlockPortal`-Ableger (PortalBlock.java:8).
- **Verhalten (nur formal):** `updateTick` leer (:13-14), kein Pigman-Spawn. `tryToCreatePortal` → false (:16-18, `func_150000_e`). `onEntityCollidedWithBlock` leer (:23-24), kein Teleport. `onNeighborBlockChange(World,int,int,int,int)` (:20-21) hat die 1.6-Signatur und überschreibt `func_149695_a(…, Block)` nicht.
- **Portierung:** nicht portieren. Dimensionsportale kommen aus anderen Klassen.

### QueenSpawnerBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `queenspawner` | The Queen Spawner Block | `MyQueenSpawnerBlock` | OreSpawnMain.java:1602 (Licht 0.9), registriert 1843 |

- **Rolle/Verhalten:** zeilengleich mit `KingSpawnerBlock`, spawnt `"The Queen"` an (x, y+8, z) mit `setGuardMode(1)` (QueenSpawnerBlock.java:50-59, 69-79).
- **Auslöser:** `onBlockAdded` 100 Ticks (:39-44), Zufallstick, Spielerabbau (:46-48), `canBlockStay` (:81-84).
- **Werte:** Licht 13, Härte 0, Hitbox 0.125–0.875.
- **Config:** `OreSpawnMOBS.TheQueenEnable` (Default 1).
- **Drops:** `queenspawner` (:61-67).
- **Portierung:** gemeinsame Klasse mit KingSpawnerBlock, Parameter EntityType und Config-Schalter. TheQueen `max_health` 6000 (manifest), Virtual Health beim Entity.

### RTPBlock

| Registry-ID | Lang-Name | Feld | Quelle |
|---|---|---|---|
| `blockteleport` | Random Teleport Block | `MyRTPBlock` | OreSpawnMain.java:1541 (Stone-Sound), registriert 1760 |

- **Rolle:** Teleportfalle für Spieler. Vanilla-Basis `Block`, `Material.rock`, Tab Blocks (RTPBlock.java:16-17).
- **Werte:** **keine Härte gesetzt** → 0, sofort abbaubar, Explosion 0.
- **Verhalten** `onEntityWalking`, nur `EntityPlayer` (:20-66):
  1. Bis zu 1000 Versuche (:31).
  2. `x = px ± 16 + nextInt(8) − nextInt(8)`, Vorzeichen per `nextInt(2)`, gleich für z (:32-43). Also 9–23 Blöcke je Achse.
  3. y von py−4 bis py+4: Block darunter `isSolid`, y und y+1 Luft → Treffer (:44-49).
  4. Treffer: `EntityPlayerMP` → `playerNetServerHandler.setPlayerLocation(x+0.5, y, z+0.5, yaw, 0)`, sonst `setLocationAndAngles` (:51-57). Pitch wird auf 0 gesetzt.
  5. 6 × (`smoke`, `explode`, `reddust`) an (x+0.5, y+2.25, z+0.5) (:58-62).
  6. `playSoundAtEntity(p, "random.explode", 1.0, 1.5)` (:63).
  7. Kein Treffer → nichts.
- **Deutung zur Seite:** `onEntityWalking` läuft auch auf dem Client für den eigenen Spieler (`EntityClientPlayerMP` ist kein `EntityPlayerMP`). Der Client würfelt eigenständig und wird vom Server korrigiert. Nur dort erscheinen die Partikel.
- **Drops:** sich selbst.
- **Portierung:**
  - Nur serverseitig `stepOn` → `ServerPlayer.teleportTo`. Partikel per `ServerLevel.sendParticles` (POOF, SMOKE, Dust rot).
  - `level.playSound(null, player, SoundEvents.GENERIC_EXPLODE, …, 1.0f, 1.5f)`.
  - `strength(0)` beibehalten.

### RockBlock

- **Registry-IDs:** keine. Nicht instanziiert (grep 0 Treffer). `MyRock` in `OreSpawnMain` ist ein Item.
- **Rolle:** unbenutzter Steinblock. `Material.rock`, Härte 2, R 1 → Explosion 0.6, Tab Blocks (RockBlock.java:12-15).
- **Portierung:** nicht portieren.

### ContainerCrystalFurnace

- **Registry-IDs:** keine (Menü zu `crystalfurnace`).
- **Rolle:** Server-Container des Kristallofens. Vanilla-Basis `Container`.
- **Slots** (ContainerCrystalFurnace.java:18-28):

| Index | Inventar | Slot | Position |
|---|---|---|---|
| 0 | TileEntity | 0 Eingang | (56, 17) |
| 1 | TileEntity | 1 Brennstoff | (56, 53) |
| 2 | TileEntity | 2 Ausgang, `SlotFurnace` (vergibt Schmelz-XP beim Entnehmen) | (116, 35) |
| 3–29 | Spieler 9..35 | Hauptinventar | x = 8 + j·18, y = 84 + i·18 |
| 30–38 | Spieler 0..8 | Hotbar | x = 8 + i·18, y = 142 |

- **Sync:** 3 Progress-Bars. 0 = `furnaceCookTime`, 1 = `furnaceBurnTime`, 2 = `currentItemBurnTime`. Beim Öffnen gesendet, danach bei Änderung (:31-55). Client setzt die Felder direkt in der TileEntity (:58-68). Die Werte gehen als short über die Leitung, max. Brennwert 20000 passt.
- **`canInteractWith`:** `TileEntityCrystalFurnace.isUseableByPlayer`, Abstand² ≤ 64 (:70-72).
- **Shift-Klick** (:74-121):
  - Ausgang (2) → Spielerslots 3–38 von hinten.
  - Eingang/Brennstoff → 3–38.
  - Spielerslot: schmelzbar (`FurnaceRecipes.smelting()`) → 0. Sonst Brennstoff (`TileEntityCrystalFurnace.isItemFuel`) → 1. Sonst Haupt ↔ Hotbar.
- **Portierung:**
  - `AbstractContainerMenu` mit `ContainerData` (3 Werte, `DataSlot`). `FurnaceResultSlot` braucht in 1.21 `AbstractFurnaceBlockEntity`-XP-Tracking, das bei eigener BE nachgebaut werden muss.
  - Alternative ist `AbstractFurnaceMenu` mit überschriebenem `isFuel`. Das verlangt 4 Datenwerte (litTime, litDuration, cookingProgress, cookingTotalTime = 150).
  - `MenuType` über `IMenuTypeExtension.create`, BlockPos im Buffer.

### ContainerCrystalWorkbench

- **Registry-IDs:** keine (Menü zu `crystalworkbench`).
- **Rolle:** 3×3-Werkbank. Vanilla-Basis `Container`, Vanilla-`CraftingManager`.
- **Slots** (ContainerCrystalWorkbench.java:25-38):

| Index | Inhalt | Position |
|---|---|---|
| 0 | Ergebnis (`SlotCrafting`) | (124, 35) |
| 1–9 | Matrix | x = 30 + i·18, y = 17 + l·18 |
| 10–36 | Hauptinventar | x = 8 + i·18, y = 84 + l·18 |
| 37–45 | Hotbar | x = 8 + l·18, y = 142 |

- **Verhalten:**
  - Matrixänderung → `findMatchingRecipe` (:42-44).
  - Schließen (Server) → alle 9 Matrixslots auswerfen (:46-56).
  - `canInteractWith`: Block an Position == `CrystalWorkbenchBlock` ∧ Abstand² ≤ 64 (:58-60).
  - Shift-Klick: Ergebnis → 10–45 von hinten, Haupt ↔ Hotbar, Matrix → 10–45 (:62-99).
  - `func_94530_a` (MCP: `canMergeSlot` in ContainerWorkbench) false für den Ergebnisslot (:101-103).
- **Portierung:** `CraftingMenu` erweitern, nur `stillValid` mit eigener Block-Prüfung überschreiben. Vanilla-Slotlayout ist identisch.

### CrystalFurnaceGUI

- **Registry-IDs:** keine. `@SideOnly(CLIENT)`.
- **Rolle:** GuiContainer des Kristallofens (CrystalFurnaceGUI.java:12-20).
- **Rendering:**
  - Hintergrund Vanilla `textures/gui/container/furnace.png`, volle `xSize × ySize` (:28-33, 43).
  - Titel: eigener Name oder `I18n("container.furnace")`, zentriert y = 6, Farbe 4210752 (:23-24).
  - „container.inventory“ an (8, ySize − 96 + 2) (:25).
  - Flamme wenn `isBurning`: `i1 = getBurnTimeRemainingScaled(12)`, blit an (k+56, l+36+12−i1) aus (176, 12−i1), 14 × (i1+2) (:34-37).
  - Pfeil: `i1 = getCookProgressScaled(24)`, blit an (k+79, l+34) aus (176, 14), (i1+1) × 16 (:38-39).
- **Portierung:**
  - Unter `com.swbr.orespawn.client`. Registrierung über `RegisterMenuScreensEvent`.
  - Die 1.21-`furnace.png` zeichnet Flamme und Pfeil über Sprites (`container/furnace/lit_progress`, `burn_progress`), die alten UV-Koordinaten stimmen nicht mehr.
  - Einfachster 1:1-Weg: `AbstractFurnaceScreen` bzw. dieselben Sprites mit den Skalen 12/24.

### CrystalWorkbenchGUI

- **Registry-IDs:** keine. `@SideOnly(CLIENT)`.
- **Rolle:** GuiContainer der Kristallwerkbank.
- **Rendering:** Vanilla `textures/gui/container/crafting_table.png` (CrystalWorkbenchGUI.java:35). Titel „container.crafting“ an (28, 6), „container.inventory“ an (8, ySize−96+2), Farbe 4210752 (:21-24).
- **Portierung:** Vanilla `CraftingScreen` wiederverwenden (Client-Paket), Menü-Typ eigener.

### GirlfriendOverlayGui

- **Registry-IDs:** keine. Registriert im Client-Proxy (ClientProxyOreSpawn.java:15).
- **Rolle:** HUD-Lebensbalken über der Hotbar für anvisierte Begleiter und Bosse. Nebenbei setzt die Klasse die Client-Statics `current_dimension` und `FastGraphicsLeaves`.
- **Ablauf** `onRenderOverlay(RenderGameOverlayEvent)` (GirlfriendOverlayGui.java:23-406):
  1. Nur nicht-abbrechbare Events vom Typ `HOTBAR` (:25-27). Deutung: in Forge 1.7.10 das `Post`-Event.
  2. Abbruch bei `hideGUI` oder offenem Screen oder ohne Spieler (:38-44).
  3. `current_dimension = player.worldObj.provider.dimensionId`. `FastGraphicsLeaves = fancyGraphics ? 0 : 1` (:45-51).
  4. `GuiOverlayEnable == 0` → Ende (:52-54).
  5. Ziel ist `mc.pointedEntity`. Sonst `OreSpawnMain.getPointedAtEntity(world, player, 16.0)`: Blickstrahl 16 Blöcke, Box +1.0 (OreSpawnMain.java:5455-5497). Das Ergebnis muss `EntityLivingBase` sein. Beim `pointedEntity`-Pfad fehlt diese Prüfung (:55-64).
  6. Name und Verhältnis nach Klasse. Spätere Treffer überschreiben frühere:

| Klasse | Anzeige | Bedingung | Lebensquelle |
|---|---|---|---|
| `Girlfriend` | Nametag oder „Girlfriend“ | `func_152114_e(player)` (SRG, laut Nutzung Besitzerprüfung), `passenger == 0` | `getGirlfriendHealth()/getMaxHealth()` (:65-81) |
| `Boyfriend` | Nametag oder „Boyfriend“ | Besitzer, `passenger == 0` | `getBoyfriendHealth()` (:82-98) |
| `ThePrince` | Nametag oder „The Toddler Prince“ | Besitzer | `getHealth()` (:99-112) |
| `ThePrincess` | Nametag oder „The Toddler Princess“ | Besitzer | `getHealth()` (:113-126) |
| `ThePrinceTeen` | Nametag oder „The Young Prince“ | Besitzer, `getActivity() == 0` | `getHealth()` (:127-143) |
| `ThePrinceAdult` | Nametag oder „The Young Adult Prince“ | Besitzer, `getActivity() == 0` | `getHealth()` (:144-160) |
| `Dragon` | Nametag oder „Dragon“ | `getActivity() == 0` | `getDragonHealth()` (:161-174) |
| `EmperorScorpion` | „Emperor Scorpion“ | – | `getEmperorScorpionHealth()` (:175-179) |
| `Basilisk` | „Basilisk“ | – | `getBasiliskHealth()` (:180-184) |
| `Mothra` | „Mothra!“ | – | `getMothraHealth()` (:185-189) |
| `Spyro` | Nametag oder „Baby Dragon“ | – | `getSpyroHealth()` (:190-199) |
| `WormLarge` | „Worm“ | `!noClip` | `getHealth()` (:200-206) |
| `Alien` | „Alien!“ | – | `getAlienHealth()` (:207-211) |
| `WaterDragon` | Nametag oder „WaterDragon“ | – | `getWaterDragonHealth()` (:212-221) |
| `Kraken` | „Kraken“ | – | `getKrakenHealth()` (:222-226) |
| `Cephadrome` | „Cephadrome“ | `getActivity() == 0`, sonst Abbruch | `getCephadromeHealth()` (:227-234) |
| `TrooperBug` | „Jumpy Bug“ | – | `getTrooperBugHealth()` (:235-239) |
| `SpitBug` | „Spit Bug“ | – | `getHealth()` (:240-244) |
| `PitchBlack` | „Nightmare“ | – | `getHealth()` (:245-249) |
| `Alosaurus` | „Alosaurus“ | – | `getHealth()` (:250-254) |
| `Nastysaurus` | „Nastysaurus“ | – | `getHealth()` (:255-259) |
| `TRex` | „T. Rex“ | – | `getHealth()` (:260-264) |
| `Kyuubi` | „Kyuubi“ | – | `getHealth()` (:265-269) |
| `Robot2` | „Robo-Pounder“ | – | `getHealth()` (:270-274) |
| `Robot4` | „Robo-Warrior“ | – | `getRobot4Health()` (:275-279) |
| `Triffid` | „Triffid“ | – | `getHealth()` (:280-284) |
| `Godzilla` | „Mobzilla“ | – | `getHealth()` (:285-289) |
| `Vortex` | „Vortex“ | – | `getHealth()` (:290-294) |
| `Irukandji` | „Irukandji“ | – | `getHealth()` (:295-299) |
| `Mantis` | „Mantis“ | – | `getHealth()` (:300-304) |
| `HerculesBeetle` | „Hercules Beetle“ | – | `getHealth()` (:305-309) |
| `TheKing` | „The King“ | – | `getHealth()` (:310-314) |
| `TheQueen` | „The Queen“ | – | `getHealth()` (:315-319) |
| `SeaViper` | „Sea Viper“ | – | `getHealth()` (:320-324) |
| `SeaMonster` | „Sea Monster“ | – | `getHealth()` (:325-329) |
| `Molenoid` | „Molenoid“ | – | `getHealth()` (:330-334) |
| `CaterKiller` | „CaterKiller“ | – | `getHealth()` (:335-339) |
| `Leon` | Nametag oder „Leonopteryx“ | – | `getHealth()` (:340-349) |
| `Hammerhead` | „Hammerhead“ | – | `getHealth()` (:350-354) |
| `BandP` | „Banker“ (`getWhat() == 0`) oder „Politician“ | – | `getHealth()` (:355-364) |
| `SpiderRobot` | „Giant Robot Spider“ | – | `getHealth()` (:365-369) |
| `GiantRobot` | „Jeffery“ | – | `getHealth()` (:370-374) |
| `AntRobot` | „Giant Robot Red Ant“ | – | `getHealth()` (:375-379) |
| `Crab` | „Very Large Crab“ | `getCrabScale() > 0.75` | `getHealth()` (:380-387) |

  7. Kein Name → Ende (:388-390).
  8. Zeichnen:
     - `y = 25`, bei Spieler im Wasser oder Rüstungswert > 0 `y = 15` (:395-398).
     - Name zentriert mit Schatten an y−10, Farbe 16725044 (0xFF3434) (:31, 399).
     - Textur `orespawn:girlfriendgui.png`. Manifest texture_map: `assets/orespawn/girlfriendgui.png` → `assets/orespawn/textures/entity/girlfriendgui.png` (:409).
     - Hintergrund 182 × 5 aus (0,0), Füllung `(int)(ratio × 183)` breit aus (0,5), bei x = Breite/2 − 91 (:33-34, 391-405).
- **Research:** 01-mobs.md nennt „Banker/Police“ (Claim). Der Code sagt „Banker“/„Politician“.
- **Portierung:**
  - Client-Paket. `RegisterGuiLayersEvent.registerAbove(VanillaGuiLayers.HOTBAR, …)` bzw. über dem Bossbalken.
  - Ziel: `Minecraft.crosshairPickEntity`, sonst eigener Raycast 16 (`ProjectileUtil.getEntityHitResult`).
  - Das Setzen von `current_dimension`/`FastGraphicsLeaves` **nicht** übernehmen, die abhängigen Blöcke lösen das per Render-Layer.
  - Entities mit Virtual Health (TheKing 7000, TheQueen 6000 u. a. über 1024) brauchen ein synchronisiertes Verhältnis. `getHealth()/getMaxHealth()` wäre dort falsch.
  - Die custom Health-Getter (`getGirlfriendHealth` usw.) sind SynchedEntityData-Werte der jeweiligen Entities.

### OreSpawnGUIHandler

- **Registry-IDs:** keine. Registriert über `NetworkRegistry.INSTANCE.registerGuiHandler` (OreSpawnMain.java:5064).
- **Rolle:** `IGuiHandler`, ordnet GUI-IDs zu.
  - Server: ID 0 → `ContainerCrystalFurnace`, falls TileEntity `TileEntityCrystalFurnace`. ID 1 → `ContainerCrystalWorkbench` (OreSpawnGUIHandler.java:10-24).
  - Client: ID 0 → `CrystalFurnaceGUI`, ID 1 → `CrystalWorkbenchGUI` (:26-40).
- **Portierung:** entfällt als Klasse. Ersetzt durch zwei `DeferredRegister<MenuType<?>>`-Einträge, `MenuProvider` in den Blöcken und Screen-Registrierung im Client-Paket.

### TileEntityCrystalFurnace

- **Registry-IDs:** keine im Manifest. TileEntity-Name `"TileEntityCrystalFurnace"` (OreSpawnMain.java:5063). Gehört zu `crystalfurnace`.
- **Rolle:** Ofenlogik. Vanilla-Basis `TileEntity implements ISidedInventory`.
- **Inventar:** 3 Slots (0 Eingang, 1 Brennstoff, 2 Ausgang), Stapelgrenze 64 (TileEntityCrystalFurnace.java:26-28, 120-122).
- **NBT:**
  - Schreiben (:101-118): `Items` (Liste mit `Slot`-Byte), `BurnTime` (short), `CookTime` (short), `CustomName`.
  - Lesen (:82-99): wie oben. `currentItemBurnTime` wird **nicht** gespeichert, sondern aus dem aktuellen Brennstoffslot neu berechnet (:95).
- **`updateEntity`** (:141-183):
  1. `furnaceBurnTime > 0` → −1 (:144-146).
  2. Server: Brennzeit 0 und `canSmelt` → neuer Brennstoff. `furnaceBurnTime = currentItemBurnTime = getItemBurnTime(slot1)`. Bei > 0 ein Item verbrauchen, leerer Stack → Container-Item, z. B. Eimer (:148-162).
  3. Brennt und `canSmelt` → `furnaceCookTime + 1`. Bei **150** → 0 und `smeltItem` (:163-170). Sonst `furnaceCookTime = 0` (:171-173).
  4. Wechsel des Brennzustands → `CrystalFurnace.updateFurnaceBlockState` (:174-178). Bei Änderung `markDirty`.
- **`canSmelt`/`smeltItem`** (:185-219): Vanilla-`FurnaceRecipes.smelting()`. Ausgang leer oder gleiches Item und Summe ≤ 64 ∧ ≤ maxStackSize. Keine eigenen Rezepte.
- **Brennwerte** `getItemBurnTime` (:221-272), Reihenfolge entscheidend:

| Prüfung | Ticks |
|---|---|
| ItemBlock `wooden_slab` | 150 |
| ItemBlock mit `Material.wood` (u. a. `crystalplanks`, `crystaltreelog` (BlockCrystalTreeLog.java:21), `skytreelog`) | 300 |
| ItemBlock `coal_block` | 16000 |
| `ItemTool`/`ItemSword`/`ItemHoe` aus Material „WOOD“ | 200 |
| `stick` | 100 |
| `coal` | 1600 |
| `lava_bucket` | 20000 |
| Vanilla-Setzling | 100 |
| `blaze_rod` | 2400 |
| `crystalcoal` | 20000 |
| `crystaltreelog` | 800 (unerreichbar, Holz-Zweig greift vorher) |
| `crystalplanks` | 400 (unerreichbar) |
| sonst `GameRegistry.getFuelValue` | Mod hat keinen `IFuelHandler` (grep), also nur Fremdmods |

- **GUI-Skalen** (Client): `getCookProgressScaled(n) = cookTime·n/150` (:125-127). `getBurnTimeRemainingScaled(n) = burnTime·n/currentItemBurnTime`, bei 0 wird `currentItemBurnTime` auf 150 gesetzt (:130-135).
- **Seiten:**
  - Oben [0], unten [2, 1], seitlich [1] (:310-314).
  - Einfügen = `isItemValidForSlot`: nie Slot 2, Slot 1 nur Brennstoff (:288-298).
  - Entnehmen: von unten aus Slot 1 nur `bucket`, sonst frei (:300-302).
- **`isUseableByPlayer`:** TileEntity noch vorhanden ∧ Abstand² ≤ 64 (:278-280).
- **Portierung:**
  - Eigene `BlockEntity` mit `WorldlyContainer` (Seitenlogik 1:1) + `MenuProvider`.
  - Ticker nur Server. Kochzeit fest 150, **nicht** die `cookingTime` des Rezepts.
  - Rezepte über `RecipeType.SMELTING` (`RecipeManager.getRecipeFor`).
  - XP: 1.7.10 vergibt sie über `SlotFurnace` aus `FurnaceRecipes`, 1.21 über `recipesUsed` in der BE. Das muss nachgebaut werden, sonst gibt der Ofen keine XP.
  - Brennwerte: eigene Tabelle wie oben, Fallback offen, siehe unresolved.
  - Items-Capability über `RegisterCapabilitiesEvent` mit `SidedInvWrapper`.
  - NBT-Schlüssel beibehalten.
  - Blockzustand über `LIT` statt Blocktausch.
