# Design: design-entities-04

Stand 10.09.2026. Batch: `golden_apple_cow`, `gold_fish`, `hammerhead`, `hercules_beetle`, `hydrolisc`, `ice_ball`, `ink_sack`, `irukandji`, `irukandji_arrow`, `island`, `island_too`, `kyuubi`, `laser_ball`, `leaf_monster`, `leonopteryx`, `lizard`, `lurking_terror`, `mantis`, `molenoid`, `mothra`, `nastysaurus`, `ostrich`, `peacock`.

Die Bildsprache dieses Batches ist die typische OreSpawn-Mischung: alles sind reine Box-Modelle (ein `addBox` je Teil, keine Hierarchie, kein Mirror, kein Inflate — `docs/research/06-models-design.md`), aber mit sehr unterschiedlicher Textur-Handschrift. Es gibt drei Familien: (1) **Kreaturen mit fotorealistisch anmutender Fell-/Hauttextur** (Molenoid, Nastysaurus, Hammerhead, Leon, Ostrich), bei denen große Boxen mit gescannt wirkenden Oberflächen belegt sind; (2) **flach kolorierte Cartoon-Tiere** (Gold Fish, Peacock, Irukandji, Hydrolisc, Lizard, Mantis, Hercules Beetle), deren Texturen aus wenigen Flächenfarben bestehen; (3) **abstrakte Marker und Sprites** (Island/IslandToo als drei taumelnde Farbwürfel, die Projektile als Billboards aus `spinners.png`). Zwei Sonderfälle sind Kyuubi, dessen Schweife und „Fire“-Hüllen halbtransparent in der Textur liegen und im Blend-Modus gezeichnet werden, und Mothra, die ein 10-fach skalierter Schmetterling mit Creeper-Energie-Overlay ist. Alle Größenangaben unten sind Modell-Ausdehnungen (unrotiert, aus `reference/jar/models/<Model>.json`) mal Renderer-Skalierung durch 16; das Bodenniveau liegt bei Pivot-Y 24 (06-models-design.md, Zeile 20). Die Zahl in Klammern ist immer die Quelle.

Attribut-Klemmen 1.21.1: kein Tier dieser Batch braucht „virtuelle Gesundheit“ — die höchsten Werte sind 250 Leben (`Hammerhead` 240, `HerculesBeetle`/`Leon` 250; manifest `max_health`) und 20 Rüstung (`Hammerhead`; manifest `armor_value`), beides unter den Grenzen 1024 bzw. 30.

Konventionen: „(manifest)“ = `docs/catalog/manifest.json`; „Extent“ = `reference/jar/models/<Model>.json` (Summe aus Pivot + Box-Ursprung + Größe, ohne Rotation); Animation aus `reference/jar/anim/<Model>.json` und dem dekompilierten `render()`; Renderer-Argumente sind `(model, shadow, scale)` aus `ClientProxyOreSpawn.registerRenderThings` (manifest `renderers[].renderer_args`), der Schatten ist `shadow * scale` (`Render<X>.java:16` in jedem Renderer dieser Batch: `super(model, par2 * par3)`).

---

### Golden Apple Cow (`golden_apple_cow`)

- **Aussehen:** Vanilla-Kuh-Silhouette (`ModelCow`), Textur `gold_cow.png` 64x32 im Vanilla-Kuh-Layout: orange-goldenes Fell mit hellgelben Adern, eingestreute graue Erz-Brocken, dunkelgrüne Flecken, rosa Euter und rosa Nase; Gesicht mit schwarzen Augen und hellem Maul (Textur gesichtet).
- **Größe:** Hitbox aus Vanilla `EntityCow`: 0.9 x 1.3 (`client-1.7.10.jar`, Klasse `wh` = `EntityCow`, Konstruktor `ldc 0.9f`, `ldc 1.3f`, `func_70105_a`); manifest `size: null`, weil `GoldCow -> RedCow -> EntityCow` kein `setSize` überschreibt. Keine Renderer-Skalierung (`gl_scale: []`, manifest), Schatten 0.7 (`renderer_args`). Höhe damit wie Vanilla ~1.4 Blöcke (Kuhmodell).
- **Modell:** vanilla `ModelCow` (`RenderEnchantedCow.java:11`) — in 1.21.1 `CowModel`. Kein eigenes Geometrie-JSON.
- **Animationen:** Vanilla-Vierbeiner (`ModelQuadruped`), keine Zusätze.
- **Varianten:** `RenderEnchantedCow.getEntityTexture` (`RenderEnchantedCow.java:42-53`): `EnchantedCow` -> `gold_cow.png`, `GoldCow` -> `gold_cow.png`, `CrystalCow` -> `crystal_cow.png`, sonst (`RedCow`) `red_cow.png`. Die Golden Apple Cow teilt sich also die Textur mit der Enchanted Cow; nur letztere bekommt den Glanz-Pass (`shouldRenderPass`, Zeile 33-40, `par2 == 3`, Rückgabe 31 — nur `instanceof EnchantedCow`). Baby über `isChild()` wie Vanilla.
- **Klang:** keine eigenen Events (manifest: kein `referenced_by` für `GoldCow`/`RedCow`); Vanilla-Kuhlaute geerbt.
- **Portierungshinweise:** `CowRenderer`-Klon mit eigener `getTextureLocation`, die nach Klasse verzweigt; kein `scale()`-Override. Der `shouldRenderPass`-Zweig betrifft nur `enchanted_apple_cow` (anderer Batch) und muss hier nicht nachgebaut werden.

### Gold Fish (`gold_fish`)

- **Aussehen:** knallgelber Fisch, Textur `GoldFish.png` 64x64 fast einfarbig (Hauptfarben 192/192/0 und 224/224/0, Textur gesichtet), rot-weißes Auge, zwei grüne Pixel am Rumpf; kastiger Rumpf 4x4x10 mit Kopf 3x4x3, Rückenflosse als 0-dicke Fläche (`Dorsalfin` size 0x4x10), Maul + Unterkiefer, 4 Brustflossen, Bauchflossen, zweiteiliger Schwanz mit zwei Schwanzflossen (Teilnamen `ModelGoldFish.json`).
- **Größe:** Hitbox 0.75 x 0.5 (`GoldFish.java:18`); Renderer `RenderGoldFish(model, 0.2, 1.0)` -> scale 1.0, Schatten 0.2 (manifest). Extent 4 x 15.5 x 25.4 Einheiten (y 8..23.5) -> ~0.25 breit, ~1.0 hoch (24-8=16 -> 16/16), ~1.6 lang. Das Modell ragt sichtbar über die 0.75er-Hitbox hinaus.
- **Modell:** `ModelGoldFish`, 16 Teile / 16 Boxen, Textur 64x64 (manifest models), Konstruktor-Arg 0.7 = `wingspeed` (`ModelGoldFish.java:27-29`). Keine GL-Aufrufe.
- **Animationen (`ModelGoldFish.java:114-147`):** alles läuft auf `f2` (ageInTicks) — kein Laufzyklus: `Pectoralfin1..4.rotateAngleY = ±0.4 + cos(f2 * 1.3/1.2/1.1/1.0 * wingspeed) * π * 0.15` (Zeile 118-125), `Bottomfin1/2.rotateAngleY = ±cos(f2 * 1.7 * ws) * π * 0.25` (126-128), `Jaw.rotateAngleX = -0.25 + cos(f2 * 0.7 * ws) * π * 0.1` (129-130). Dauerndes Flossenwedeln und Maulöffnen, auch im Stand.
- **Varianten:** keine (eine Textur, `RenderGoldFish.java:47`).
- **Klang:** living/hurt = vanilla `splash` (`GoldFish.java:44-50`), death `orespawn:little_splat` (Zeile 53; manifest `little_splat`, referenced_by GoldFish).
- **Portierungshinweise:** gerades `LayerDefinition`; `scale()`-Override entfällt (1.0). Für 1.21.1 die Fischtextur 1:1 nach `assets/orespawn/textures/entity/goldfish.png` (manifest texture_map).

### Hammerhead (`hammerhead`)

- **Aussehen:** massiger sechsbeiniger Panzer-Koloss (Avatar-„Hammerhead Titanothere“, 01-mobs.md #12): Textur `Hammerheadtexture.png` 222x256 mit **opak weißem Hintergrund**, darauf grau-blaue, steinig schattierte Flächen (Hauptfarben 224/224/224 = Weißfläche, 64/96/96, 96/96/128) und ein Gesichtsfeld mit zwei orangen Augen (Textur gesichtet). Teile: `chest`, `abdomen`, `neck`, `head`, `snout`, `neck_armour`, Hornbasis + `horn_1/2/L/R` (Hammerkopf), sechs Rückenpanzer-Platten `back_armour1..4R`, `tail`, 6 Beine je 2 Segmente (`leg_1..3(R)(b)`), Nackenfächer `fan1`, `Lfan2/3`, `Rfan2/3`, Ohren `Lear/Rear` (`ModelHammerhead.json`).
- **Größe:** Hitbox 3.0 x 5.0 (`Hammerhead.java:27`); Renderer `(model, 1.0, 2.5)` -> scale 2.5, Schatten 2.5 (manifest). Extent 53 x 32.5 x 72 Einheiten (y -8..24.5) -> ~8.3 breit, ~5.0 hoch ((24+8)*2.5/16), ~11.25 lang.
- **Modell:** `ModelHammerhead`, 37 Teile, Textur 222x256 (nicht-2er-Potenz, laut 06-models-design.md Zeile 183 unproblematisch), Konstruktor-Arg 0.33 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelHammerhead.java:240-343`):** Laufzyklus nur wenn `f1 > 0.1`: `newangle = cos(f2 * 1.3 * ws) * π * 0.1 * f1`, `newangle2` um π/4 versetzt (246-249); Beine 1 und 3 gegenphasig auf `newangle`, Beinpaar 2 auf `newangle2`, jeweils Grundneigung -0.087/-0.052/-0.349 (253-264). Kopfgruppe (neck, armour, Hörner, head, snout, fans, ears) folgt `toRadians(f3) * 0.25` mit festen Y-Versätzen der Fächer (±0.122/±0.226) und Ohren (±0.227) (265-280). Rückenplatten `back_armour_4/4R` atmen langsam (`cos(f2 * 0.3 * ws) * π * 0.03`, 281-283). **Angriff** (`getAttacking() != 0`, `Hammerhead.java:255`): ganze Kopfgruppe nickt mit `cos(f2 * 1.3 * ws) * π * 0.13` auf `rotateAngleX` (284-305) — das „Rammen“.
- **Varianten:** keine.
- **Klang:** living `orespawn:hammerhead_living` mit 1/3 Chance je Aufruf (`Hammerhead.java:76-81`; manifest `hammerhead_living1/2`), hurt `orespawn:alo_hurt` (85), death `orespawn:hammerhead_death` (89).
- **Portierungshinweise:** `scale()` -> 2.5; `setupAnim` braucht `getAttacking()` als synced data. Textur-Hintergrund ist Weiß, nicht Alpha — ungenutzte UV-Bereiche sind harmlos, aber man darf die Textur nicht „freistellen“.

### Hercules Beetle (`hercules_beetle`)

- **Aussehen:** riesiger schwarzer Käfer: Textur `Beetletexture.png` 256x256 fast rein schwarz (5262 von 7612 opaken Pixeln in 0/0/0) mit dunkelrot-melierten Flächen (Rumpf) und einem kleinen Kopffeld mit zwei roten Augenpunkten und weißen Mandibeln (Textur gesichtet). Teile: `body1/2`, acht Kopfteile `head1..8`, neun Kieferteile `jaw1..9` (das lange Hercules-Horn), sechs Beine à drei Segmente (`lfleg1..3`, `lmleg`, `lrleg`, `rfleg`, `rmleg`, `rrleg`).
- **Größe:** Hitbox 3.25 x 2.75 (`HerculesBeetle.java:28`); Renderer `(model, 0.99, 1.1)` -> scale 1.1, Schatten 1.089. Extent 74 x 22 x 100 (y -4..18) -> ~5.1 breit, ~1.9 hoch ((24+4)*1.1/16, Beine unrotiert), ~6.9 lang.
- **Modell:** `ModelHerculesBeetle`, 37 Teile, 256x256, Konstruktor-Arg 1.0 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelHerculesBeetle.java:240-334`):** Beine drehen auf **Y** (spreizen statt schwingen): `newangle = cos(f2 * ws * 0.45) * π * 0.12 * f1`; Vorder-/Hinterbeine mit ±0.349 Grundwinkel, Mittelbeine `-newangle`; Segment 2 und 3 kopieren Segment 1 (246-281). Kiefer/Horn `jaw1..9.rotateAngleX = Grundwinkel (0.122/0/0.314) + newangle` mit ruhig `cos(f2 * 0.051 * ws) * π * 0.01`, bei `getAttacking() != 0` (`HerculesBeetle.java:430`) `cos(f2 * 0.51 * ws) * π * 0.07` (282-296) — das Horn hebt und senkt sich beim Angriff.
- **Varianten:** keine.
- **Klang:** living null, hurt `alo_hurt`, death `hercules_death` (`HerculesBeetle.java:90-98`); beim Zubeißen spielt er am Opfer `scorpion_attack` (1.4) bzw. `scorpion_living` (Zeile 358/361).
- **Portierungshinweise:** `scale()` -> 1.1; `getAttacking()` synced. Sonst gerades `LayerDefinition`.

### Hydrolisc (`hydrolisc`)

- **Aussehen:** nachtblaues, echsenhaftes Tier mit vielen Beinen: Textur `hydrolisc.png` 64x128 in Marineblau/Schwarz (0/0/32, 0/0/64, 0/0/0) mit hellblauen Rautenmustern, dazu kleine Rot-Grün-Blau-Punktfelder für die `spine1..4`-Rückenstacheln und das Kopffeld mit grünen Augen (Textur gesichtet). Teile: `body0/1/2`, `head1..3`, drei Kopffedern `feather1..3` (1x2x9/1x2x10, `ModelHydrolisc.java:176-187`), vier Beinketten à sechs Segmente `lf1..6`, `rf1..6`, `lb1..6`, `rb1..6`, Schwanz `tail1..3`, `spine1..4`.
- **Größe:** Hitbox 0.5 x 0.5 (`Hydrolisc.java:29`); Renderer `(model, 0.65, 0.65)` -> scale 0.65, Schatten 0.4225; Baby: scale / 2 (`RenderHydrolisc.java:35-38`, `isChild()`). Extent 14 x 24.5 x 37 (y 3.5..28) -> ~0.57 breit, ~0.83 hoch ((24-3.5)*0.65/16), ~1.5 lang.
- **Modell:** `ModelHydrolisc`, 40 Teile, 64x128, Konstruktor-Arg 0.65 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelHydrolisc.java:258-351`):** Beine: `newangle = cos(f2 * 1.3 * ws) * π * 0.25 * f1` (nur `f1 > 0.1`); alle 24 Beinsegmente auf `rotateAngleX` mit festen Knickwinkeln (0, -0.488, -2.347, -0.628, -0.628, +0.174), links/rechts und vorn/hinten gegenphasig (270-293). Schwanz: `cos(f2 * 1.0 * ws) * π * 0.15`, **0 wenn `isSitting()`** (294-297); Kettenglieder werden per `rotationPointX/Z = vorheriges + cos/sin * 5 bzw. 8` nachgeführt (298-304). Kopffedern: `hf = getHydroHealth() / getMaxHealth()` (305); `feather2.rotateAngleY = cos(f2 * 1.25 * ws * hf) * π * 0.2 * hf`, `feather1/3 = ±(cos(f2 * 0.75 * ws * hf) * π * 0.2 * hf) ∓ 0.9` (306-310) — bei voller Gesundheit flattern die Federn, bei niedriger stehen sie still. Das deckt sich mit dem Wiki („head ruffles show health, tail wagging shows sitting“, 01-mobs.md #79).
- **Varianten:** keine Textur-Varianten; Baby halb so groß.
- **Klang:** living null, hurt `cryo_hurt`, death `cryo_death`, Lautstärke 0.4 (`Hydrolisc.java:319-333`); Vanilla `splash` beim Wassereintritt (197) und `damage.fallbig/fallsmall` (151/154). Pitch bei Kindern 1.5 (351).
- **Portierungshinweise:** `scale()` mit `isBaby()`-Zweig (0.325 statt 0.65). `setupAnim` liest `isSitting()`, `getHydroHealth()` und `getMaxHealth()` — `getHydroHealth` muss synced sein. Abweichung zum Wiki: 01-mobs.md nennt Health 50, Quelle/Manifest 100 (`max_health: [100]`) — Quelle gilt.

### IceBall (`ice_ball`)

- **Aussehen:** kein Modell. `RenderItemUrchin`/`RenderSpinner` zeichnet ein Billboard-Quad mit Sprite-Index `my_index = 84` (`IceBall.java:15`) aus `spinners.png` 256x256 (16x16-Raster, Spalte 84 % 16 = 4, Zeile 84 / 16 = 5; `RenderSpinner.java:33-36`): eine weiß-hellcyane Schneekugel (Sprite gesichtet, Hauptfarben 224/224/224 und 96/224/224).
- **Größe:** `glScalef(0.5, 0.5, 0.5)` (`RenderSpinner.java:25`) auf ein 1x1-Quad -> 0.5 Blöcke Kantenlänge; Hitbox aus `EntityThrowable` (manifest `size: null`).
- **Modell:** keins.
- **Animationen:** Billboard zum Spieler (`glRotatef(180 - playerViewY)`, `-playerViewX`, Zeile 40-41), dann `glRotatef(rotationPitch, 0, 0, 1)` (42) — der Sprite **rollt** um die Blickachse: `LaserBall.onUpdate` (geerbt, `LaserBall.java:185-192`) addiert je Tick 50° auf `my_rotation` und schreibt sie in `rotationPitch`. Partikel: `fireworksSpark` 2x je Tick (`is_iceball != 0 && is_special == 0` -> `mx = 2`, Zeile 197-204), kein `reddust` (Zeile 205-206). Beim Einschlag Explosion 3.0 (`LaserBall.java:172-174`, `is_iceball`) und Eisblöcke (`IceBall.java:74`).
- **Varianten:** keine. Geworfen von `Dragon`, `TheKing`, `ThePrince*`, `ThePrincess`, `ItemIceBall`, Dispenser (grep `new IceBall(`).
- **Klang:** `random.explode` beim Einschlag (`LaserBall.java:171`), sonst keine.
- **Portierungshinweise:** ein `EntityRenderer`, der ein gedrehtes, spielerzugewandtes Quad aus dem Atlas zeichnet (`spinners.png` als eigene Textur, UV = Index-Raster); Rotation aus `getXRot()`; Skalierung 0.5. Alternativ `ThrownItemRenderer` mit einem 16x16-Item aus `spinners.png` — dann fehlt aber das Rollen.

### InkSack (`ink_sack`)

- **Aussehen:** kein Modell; Sprite-Index `my_index = 65` (`InkSack.java:18`) aus `spinners.png` (Spalte 1, Zeile 4): ein schwarz-graues Tintenbeutel-Item mit brauner Schnürung (Sprite gesichtet).
- **Größe:** 0.5-Block-Billboard (`RenderSpinner.java:25`).
- **Modell:** keins.
- **Animationen:** rollt über `rotationPitch` (`InkSack.java:75-83`, eigener `onUpdate`); `smoke`-Partikel beim Aufprall (67); Treffer geben Blindness 100+50·rand(8) Ticks (63).
- **Varianten:** keine. Geworfen nur von `AttackSquid` (grep `new InkSack(`).
- **Klang:** keine eigenen.
- **Portierungshinweise:** wie IceBall, gleicher Renderer, anderer Index.

### Irukandji (`irukandji`)

- **Aussehen:** winzige weißgraue Qualle: Textur `Irukandjitexture.png` 64x32 fast nur Hellgrau (192/192/192) mit weißen Punkten, ein Feld für den Glockenkörper mit zwei dunklen Augenlöchern (Textur gesichtet). `body` 4x4x4, acht Tentakelsegmente `t11/t12`, `t21/t22`, `t31/t32`, `t41/t42` je 1x7x1 an den vier Ecken (`ModelIrukandji.json`).
- **Größe:** Hitbox 0.25 x 0.25 (`Irukandji.java:33`); Renderer `(model, 0.1, 0.25)` -> scale 0.25, Schatten 0.025. Extent 4 x 18 x 4 (y 6..24) -> ~0.06 breit, ~0.28 hoch (18*0.25/16) — im Wiki „one of the smallest mobs“ (01-mobs.md #58).
- **Modell:** `ModelIrukandji`, 9 Teile, 64x32, Konstruktor-Arg 1.0 (`wingspeed`, im `render()` aber nicht benutzt — dort steht rohes `f2`).
- **Animationen (`ModelIrukandji.java:72-146`):** jede Tentakel als Zweiglied-Kette: `t?1.rotateAngleX = cos(f2 * 0.55/0.65/0.5/0.57) * π * 0.15`, `t?1.rotateAngleZ = cos(f2 * 0.35/0.45/0.3/0.37) * π * 0.1`; das zweite Glied wird mit `sin/cos(newangle) * 7` an das Ende des ersten gesetzt (`rotationPointX/Y/Z`, Zeile 79-87 usw.) und schwingt mit eigener Frequenz. Vier verschiedene Frequenzen pro Tentakel -> unregelmäßiges Treiben, unabhängig von der Bewegung.
- **Varianten:** keine.
- **Klang:** living null; hurt `orespawn:little_splt` (`Irukandji.java:97`) — **Tippfehler im Original**, dieses Event existiert nicht (manifest kennt nur `little_splat`), also stumm; death `orespawn:ratdead` (101, manifest `ratdead1..3`).
- **Portierungshinweise:** `scale()` -> 0.25. Tentakel-Ketten über `ModelPart.x/y/z` in `setupAnim` nachführen (06-models-design.md Zeile 27). Den Sound-Tippfehler beim Port entweder 1:1 (stumm) lassen oder auf `little_splat` korrigieren — bewusst entscheiden.

### Irukandji Arrow (`irukandji_arrow`)

- **Aussehen:** kein eigener Renderer (manifest `renderers: []`, `name: null`). `IrukandjiArrow extends EntityArrow` (`IrukandjiArrow.java:15`), und 1.7.10 sucht den Renderer über die Superklassenkette -> vanilla Pfeil-Renderer (`RenderArrow`, Vanilla-Pfeiltextur). Es gibt nur das **Item**-Bild `irukandjiarrow.png` (manifest texture_map -> `textures/item/irukandjiarrow.png`).
- **Größe:** Vanilla-Pfeil.
- **Modell:** keins.
- **Animationen:** Vanilla-Pfeil (Flug, Stecken, Wackeln nach Einschlag).
- **Varianten:** keine. Verschossen von `SkateBow` und dem Dispenser-Verhalten `MyDispenserBehaviorArrow` (grep `new IrukandjiArrow(`).
- **Klang:** keine eigenen.
- **Portierungshinweise:** `AbstractArrow`-Subklasse mit `ArrowRenderer`/`TippableArrowRenderer` und Vanilla-Pfeiltextur; nichts Eigenes zu zeichnen.

### Island (`island`)

- **Aussehen:** kein Lebewesen, sondern der Steuer-Entity der schwebenden Inseln in der Danger Dimension (01-mobs.md Zeile 155; 02-dimensions-worldgen.md Zeile 218-228). Gezeichnet werden **drei ineinander liegende 8x8x8-Würfel** (`ModelIsland.json`: `Shape1` uv 0,0; `Shape2` uv 32,0 mit Rotation 45/45/45°; `Shape3` uv 32,16 mit 45/45/45°), Textur `Island.png` 64x32 aus drei Vollfarben: Shape1 **weiß** (255/255/255), Shape2 **schwarz** (0/0/0), Shape3 **gelb** (255/246/1) (Pixelanalyse der drei UV-Felder).
- **Größe:** Hitbox 0.5 x 0.5 (`Island.java:38`); Renderer `(model, 0.25, 1.0)` -> scale 1.0, Schatten 0.25. Würfel 8 Einheiten = 0.5 Blöcke, durch die 45°-Drehung Diagonale bis ~0.87 Blöcke; Pivot y 16 -> Würfelmitte 0.5 Blöcke über dem Boden.
- **Modell:** `ModelIsland`, 3 Teile, 64x32, Konstruktor-Arg wird ignoriert (`wingspeed = 1.0`, `ModelIsland.java:14-15`). Geteilt mit `island_too`.
- **Animationen (`ModelIsland.java:35-60`):** alle drei Würfel taumeln auf allen drei Achsen mit leicht verschiedenen Frequenzen: `Shape1: cos(f2 * 0.050/0.051/0.052) * π`, `Shape2: 0.053/0.054/0.055`, `Shape3: 0.056/0.057/0.058` — ein langsam rotierendes Dreifarb-Gebilde, unabhängig von Bewegung. Die Entity selbst bewegt sich nicht (`motionX/Y/Z = 0`, `Island.java:45-48`), sie versetzt periodisch die Inselblöcke (`update_island`, Zeile 78-81).
- **Varianten:** keine.
- **Klang:** keine.
- **Portierungshinweise:** gerade `LayerDefinition` mit drei Kindern; `setupAnim` nur auf `ageInTicks`. Praktisch ein sichtbarer Marker — beim Port prüfen, ob das Gebilde in der Insel steckt (Pivot 0.5 Blöcke über Fuß) oder frei sichtbar ist; im Original ist nichts unsichtbar geschaltet (kein `setInvisible` in `Island.java`).

### IslandToo (`island_too`)

- **Aussehen:** wie `island`, gleiche `ModelIsland`-Geometrie, Textur `IslandToo.png` 64x32: weiß / schwarz / **rot** (208/0/0) statt gelb (Pixelanalyse). Steuert die Gras-Inseln mit Wasser, Blumen und Bäumen (02-dimensions-worldgen.md Zeile 222-225).
- **Größe:** Hitbox 0.5 x 0.5 (`IslandToo.java:42`); Renderer `RenderIslandToo(model, 0.25, 1.0)` -> scale 1.0, Schatten 0.25; Würfel 0.5 Blöcke.
- **Modell:** `ModelIsland` (geteilt).
- **Animationen:** identisch (gleiche Modellklasse).
- **Varianten:** keine.
- **Klang:** keine.
- **Portierungshinweise:** ein Renderer für beide mit anderer `getTextureLocation`; sonst wie `island`.

### Kyuubi (`kyuubi`)

- **Aussehen:** aufrecht gehender Fuchsdämon mit **neun Schweifen**: Textur `Kyuubi.png` 512x256, dominant Rot (224/32/0 — 4464 Pixel) und Schwarz; Kopffeld mit orangem Fuchsgesicht, schwarzen Ohren und gelb-schwarzem Stirnband (Textur gesichtet, UV 168,0). Die Körperteile (`head`, `chest`, `body`, Arme/Beine je Ober-/Unterteil) sind orange-schwarz und **opak**; die Schweife `tail0..9`, die Hörner `lfHorn1..5`/`rtHorn1..5` und die elf „Fire“-Hüllen (`headFire`, `chestFire`, `bodyFire`, `*ArmUpper/LowerFire`, `*LegUpper/LowerFire`) sind in der Textur **halbtransparent** (Alpha-Analyse: z. B. `tail4` 0 opak / 219 semi / 69 transparent; `headFire` 1 / 599 / 200; `chest` 292 opak / 0 semi). Der Semi-Alpha-Wert ist praktisch überall **154/255 (≈ 60 %)** (Alpha-Histogramm der ganzen Textur: 123677 Pixel 0, 3624 Pixel 154, 2111 Pixel 255; jedes Fire-Feld besteht zu >75 % aus Alpha 154). Die Fire-Teile sind je 2 Einheiten größere Boxen um die Körperteile (z. B. `head` 8³ bei UV 168,0, `headFire` 10³ bei 168,84) — ein rot-glühender Chakra-Mantel um einen schwarzen Fuchs, dazu neun rot glühende Schweife, die hinten in einem Fächer aufsteigen (`tail1..9` mit Grundwinkeln -0.26 … 2.0 rad auf X, `ModelKyuubi.java:362-386`).
- **Größe:** Hitbox 0.5 x 1.25 (`Kyuubi.java:23`); Renderer `(model, 0.1, 1.0)` -> scale 1.0, Schatten 0.1. Extent 20.3 x 36.5 x 38.8 (y -12.5..24) -> ~1.3 breit, ~2.3 hoch (36.5/16), ~2.4 lang mit Schweifen.
- **Modell:** `ModelKyuubi`, 42 Teile, 512x256, Konstruktor-Arg 0.5 = `wingspeed`. **GL im Modell** (06-models-design.md Zeile 196): `glEnable(2977)`, `glEnable(3042)`, `glBlendFunc(770, 771)`, `glRotatef(180, 0, 1, 0)`, `glTranslatef(0,0,0)` — der Blend-Block umschließt **alle** `render(f5)`-Aufrufe (`ModelKyuubi.java:387-437`), es gibt kein `glColor4f`; die Transparenz kommt ausschließlich aus dem Textur-Alpha. Das ganze Modell wird um 180° um Y gedreht (Zeile 392); der Kopf bekommt dafür `toRadians(f3) + 4 * π/4` (Zeile 309), die Fire-/Horn-Kette rechnet mit dem ungedrehten `headFire.rotateAngleY = toRadians(f3)` (310).
- **Animationen (`ModelKyuubi.java:271-438`):** Beine `0.59/-0.15` (rechts) bzw. `0.26/-0.44` (links) ± `cos(f2 * 1.1 * ws) * π * 0.2 * f1` (nur `f1 > 0.1`), Unterschenkel mit `rotationPointZ = sin(oberer Winkel) * 8` nachgeführt (276-293). Arme: Laufanteil `cos(f2*1.1*ws)*π*0.08*f1` + Idle `cos(f2*0.5*ws)*π*0.01`, Unterarm 0.48 versetzt (294-307). Hörner: zwei fünfgliedrige Ketten links/rechts vom Kopf, jedes Glied `±0.244 ± cos(f2 * 1.3 * ws - n * π/4) * π * 0.1` auf Y, Position aus dem Vorgänger mit Längen 3.6/2/4/3/2 (313-344) — wellenförmig wehende Ohren/Hörner. Schweife: Y-Schwingung `cos(f2 * 0.9 * ws - n * π/4) * π * 0.2` und X-Hebung `Grundwinkel + cos(f2 * 0.5 * ws - n * π/4) * π * 0.1`, Positionen aus Vorgänger mit Längen 3/4/3.5/5/4/3/2/1 (345-386) — ein ständig wogender Fächer. Alles unabhängig vom Angriff.
- **Varianten:** keine Textur-Varianten. Dazu Partikel aus der Entity: alle ~10 Ticks `reddust` + `lava` bei y+2 und `setFire(5)` (`Kyuubi.java:72-75`) — der Kyuubi brennt sichtbar dauerhaft (Vanilla-Feuer-Overlay).
- **Klang:** living `kyuubi_living`, hurt `alo_hurt`, death `alo_death` (`Kyuubi.java:90-100`).
- **Portierungshinweise:** die **ganze Modellzeichnung** läuft mit `RenderType.entityTranslucent(texture)` (Blend über alles); da die opaken Teile Alpha 255 haben, reicht ein einziger translucenter Pass — Sortierung innerhalb des Modells wie im Original (Render-Reihenfolge Zeile 394-435: Körper zuerst, Schweife, Hörner, dann Fire-Hüllen zuletzt). Die 180°-Y-Drehung entweder als `poseStack.mulPose(Axis.YP.rotationDegrees(180))` in `renderToBuffer` oder in die Pivots einbacken; dann die `+π`-Korrektur am Kopf (Zeile 309) nicht vergessen. Kein `scale()`-Override (1.0). Dauerfeuer: `setRemainingFireTicks` im Tick + Partikel wie Original.

### LaserBall (`laser_ball`)

- **Aussehen:** kein Modell; Sprite-Index `my_index = 81` (`LaserBall.java:22`, Spalte 1, Zeile 5 in `spinners.png`): eine dunkelrot glühende Kugel mit hellrotem Kern (Sprite gesichtet, Farben 64/0/0, 128/0/0).
- **Größe:** 0.5-Block-Billboard.
- **Modell:** keins.
- **Animationen:** rollt 50°/Tick über `rotationPitch` (`LaserBall.java:185-192`); je Tick 4 `fireworksSpark` + `reddust` (Zeile 197-207; `is_special` -> 10); Lebensdauer 200 Ticks (180-183). Einschlag: 10 (special 20) Runden `smoke` + `largesmoke` + `fireworksSpark` (161-170), Explosion 3.0 nur bei `is_special` (172-174).
- **Varianten:** `is_special` (mehr Partikel, Explosion; `setSpecial()`, Zeile 78), `is_acid`/`is_iceball`-Flags für Subklassen. Geworfen von `GiantRobot`, `Robot3/4/5`, `ItemLaserBall`, `ItemRayGun`, Dispenser (grep `new LaserBall(`).
- **Klang:** `random.explode` (0.5, Pitch 1 ± 0.5) beim Einschlag (`LaserBall.java:171`).
- **Portierungshinweise:** wie IceBall (Spinner-Renderer); Partikel über `level.addParticle` mit `ParticleTypes.FIREWORK` / `DUST`.

### Leaf Monster (`leaf_monster`)

- **Aussehen:** fünf riesige Laubwürfel: `body`, `larm`, `rarm`, `lleg`, `rleg`, jeder 16x16x16 (`ModelLeafMonster.json`), Textur `LeafMonstertexture.png` 128x128 = Vanilla-Laubtextur-Kacheln (Grün 0/96/0, 32/128/0) mit einem braunen Holzstamm-Feld in der Mitte (Textur gesichtet). Im Ruhezustand ein kompakter Laubhaufen, im Angriff eine aufgerichtete Blockfigur mit rudernden Armen (Wiki: „blends in, jumps out when you get close“, 01-mobs.md #67).
- **Größe:** Hitbox 1.0 x 2.5 (`LeafMonster.java:24`); Renderer `(model, 0.65, 1.0)` -> scale 1.0, Schatten 0.65. Extent 48 x 48 x 16 -> aufgerichtet ~3.0 breit, ~3.0 hoch, 1.0 tief; versteckt (`body.rotationPointY = 16`, Arme bei 8): Spanne y -8..24 -> ~2.0 hoch.
- **Modell:** `ModelLeafMonster`, 5 Teile, 128x128, kein Konstruktor-Arg. Keine GL-Aufrufe.
- **Animationen (`ModelLeafMonster.java:45-84`):** `getAttacking() == 0` (`LeafMonster.java:46`): `body.rotationPointY = 16`, Arme `rotationPointY = 8`, alle Winkel 0 — Haufen. Sonst: `body.rotationPointY = 0`, Arme `-8`; Beine `±cos(f2 * 0.95) * π * 0.25 * f1` (nur `f1 > 0.1`); Arme `rotateAngleY = ∓|cos(f2 * 0.7) * π * 0.55|`, `rotateAngleX = -|...|` (60-78) — ein hektisches Armrudern.
- **Varianten:** keine.
- **Klang:** living null, hurt `leaves_hit`, death `leaves_death` (`LeafMonster.java:112-120`); Vanilla `damage.fallbig/fallsmall` (70/74).
- **Portierungshinweise:** trivial als `LayerDefinition`; `getAttacking()` synced; kein `scale()`.

### Leonopteryx (`leonopteryx`)

- **Aussehen:** riesiger Flugdrache (Toruk aus „Avatar“, 01-mobs.md #44): Textur `Leon.png` 256x256 mit **weißem Grund** (14721 Pixel 224/224/224) und rot-orange-gelben Flammenstreifen (160/0/0, 192/96/32, 224/192/64), dazu dunkelviolette Flächen für Hals-/Kopfsegel und Fußkrallen und ein hellblauer Streifen (Textur gesichtet). Anatomie: `chest`, `abdomen`, drei Halssegmente `neck_1..3`, `head`, `upper_jaw`, `bottom_jaw`, `chest_ridge`, sechs Segel (`upper_sail_1..3`, `lower_sail1..3`), Augenwülste, vier Antennen, Arme `arm_1/2_L/R` mit Flügelflächen `wing_1..7_L/R`, Beine `leg_1/2_L/R`, Füße, vier Krallen. **Das ganze Skelett existiert doppelt**: 49 Bodenteile und 49 Flugteile mit Präfix `f` (`fchest`, `fwing_5_L`, …), insgesamt 98 Teile (`ModelLeon.json`).
- **Werte-Abweichung:** `mygetMaxHealth()` liefert hart 250 (`Leon.java:150-152`) und `attackDamage` 55.0 (`Leon.java:99`); die Config-Tabelle `Leon_stats` wird dagegen mit 150/20/8 angelegt (`OreSpawnMain.java:6195`, manifest `mob_stats.Leon_stats`) und vom Modell/Renderer nicht gelesen. Wiki (01-mobs.md #44) nennt 250/55/16 — Quelle und Wiki stimmen überein, nur die Config-Defaults nicht.
- **Größe:** Hitbox 3.5 x 8.25 (`Leon.java:63`); Renderer `(model, 1.0, 1.75)` -> scale 1.75, Schatten 1.75. Extent 62 x 76 x 100 (y -49..27) -> ~6.8 breit (Flügel angelegt), ~8.3 hoch, ~10.9 lang. In der Flugpose stehen die Flügel seitlich ab (`farm_1_L.rotateAngleZ = -π/2 - newangle`, `ModelLeon.java:871`), die Spannweite ist dann größer als die Extent-Rechnung — offen: nicht ohne Posenrechnung bestimmbar.
- **Modell:** `ModelLeon`, 98 Teile, 256x256, Konstruktor-Arg 0.22 = `wingspeed`. Keine GL-Aufrufe. In 06-models-design.md Rang 8 der schwersten Ports (215 Punkte, 5831 Anim-Bytes).
- **Animationen (`ModelLeon.java:606-1080`):** Weiche über `getActivity()` (DataWatcher 21, `Leon.java:1108`): **Boden (0)**: Beine `-0.611/0.611 ± cos(f2 * 1.8 * ws) * π * 0.25 * f1`, Unterschenkel/Füße kinematisch nachgeführt (Längen 9, 13/11; Zeile 628-639); Flügel angelegt (`wing_3` ±0.523 auf Y, Arme `-0.07`, `-0.471`, Flügelspitzen 0.68/0.453/0.119 + langsames Atmen `newangle2 / 2..8`, 640-696); Brust `-0.436`, Unterkiefer `-1.308 + Atmen`, Kopf-Yaw `toRadians(f3) * 0.5` mit Augenwülsten/Antennen ±0.558/0.366/0.139 (697-732). **Flug (≠0)**: Flügel schlagen `±π/2 ± cos(f2 * 1.6 * ws * spd) * π * 0.26 * amp` (870-930), äußere Segmente mit Faktor 1.3 und 1.65 und fester X-Neigung (-π/4, -3π/8, -π/2); Körper wippt `fchest.rotationPointY = -2 + sin(newangle2) * 10 * amp`, **nicht wenn geritten** (`getBeingRidden() != 0`, 791-796); Hals dreigliedrig nachgeführt (931-941); Beine angezogen (`π/2`, Füße π) oder beim Angriff pendelnd `-π/4 ± cos(f2 * 3.6 * ws) * π * 0.1` (823-869); Kiefer beim Angriff `-0.9 + cos(f2 * 2.6 * ws) * π * 0.16` (1020-1026); Angriff -> `spd = 1.7`, `amp = 1.4` (784-787). Beim Reiten wird der Kopf-Yaw über `RenderInfo.rf1` aus der Yaw-Änderung geglättet (`(prev - cur) * 8`, Grenze ±50, 974-987). Sitzend: Atmen 0 (623-625).
- **Varianten:** keine Textur-Varianten; zwei Posen-Sets wie beschrieben.
- **Klang:** living `leon_living` nur im Flug ohne Reiter (`getActivity() == 1 && riddenByEntity == null`, `Leon.java:190-198`), hurt `leon_hit` (drei Dateien), death `leon_death` (201-205); im Flug zusätzlich `orespawn:MothraWings` mit 0.5 (492, manifest `mothrawings1..3`).
- **Portierungshinweise:** `scale()` -> 1.75. Eine `LayerDefinition` mit allen 98 Teilen; in `setupAnim` das jeweils andere Set unsichtbar schalten (`visible = false`) statt zwei Modelle. `getActivity`, `getAttacking`, `getBeingRidden` synced; `RenderInfo` wird clientseitig pro Entity gehalten (im Original am Entity, `getRenderInfo/setRenderInfo`, `Leon.java:158-162`) — im Port als Client-Feld oder Map im Renderer. GeckoLib ist laut Vorgabe nicht geplant; die Kinematik ist Handarbeit (475 Zeilen).

### Lizard (`lizard`)

- **Aussehen:** grüngelbe, krokodilartige Echse mit blauem Rückenkamm: Textur `Lizard.png` 128x128, Hauptfarben Olivgrün (64/96/0, 32/64/0) mit gelben Augen, Blau (0/0/128) für die Rückenkamm-Teile `FinRidge1..7` und Flossen `Fin2..10`, weiße Zahnreihen, ein rotes 20x10-Feld bei UV 30,40 für den Hut (Textur gesichtet). 71 Teile: dreiteiliger Rumpf, vier Beine à zwei Segmente + Fuß + 2 Zehen, fünfgliedriger Schwanz, Hals, Oberkiefer `JawTop` mit fünf Nasenteilen und zwei Augen, `BottomJaw`, 16 Zähne, `Hat1/Hat2` (`ModelLizard.json`; Hüte 4x1x6 und 3x2x4, `ModelLizard.java:432-437`).
- **Größe:** Hitbox 1.5 x 1.25 (`Lizard.java:39`); Renderer `(model, 0.75, 1.0)` -> scale 1.0, Schatten 0.75; Baby scale / 2 (`RenderLizard.java:36-42`). Extent 30 x 27 x 81 (y -3..24) -> ~1.9 breit, ~1.7 hoch, ~5.1 lang.
- **Modell:** `ModelLizard`, 71 Teile, 128x128, Konstruktor-Arg 0.65 = `wingspeed`. Keine GL-Aufrufe. Ein UV-Feld (`FinRidge3`, uv 12,115, 2x13x1) ragt 1 Pixel unter den Texturrand (06-models-design.md Zeile 182).
- **Animationen (`ModelLizard.java:444-661`):** Beine spreizen auf **Y** (`newangle = cos(f2 * 1.0 * ws) * π * 0.25 * f1`), Unterschenkel auf X, Füße/Zehen folgen (455-474). Kiefer: Angriff (`getAttacking() != 0`, `Lizard.java:377`) `0.52 + cos(f2 * 0.45) * 0.35`, sonst 0.25 (475-480); die sechs Unterkieferzähne kopieren den Winkel. Schwanz: idle `cos(f2 * 0.25 * ws) * π * 0.05`, Angriff `cos(f2 * 1.25 * ws) * π * 0.35`, fünf Glieder mit Faktoren 0.25…1.25 und Nachführung (Längen 12/9/7/7; 487-503). Kopf: `Neck` `toRadians(f3) * 0.25`, Kiefergruppe `* 0.5`, Augen ±0.78 versetzt, alle Kopfteile werden auf `JawTop`-Position gesetzt (504-585). **Hut** nur wenn `get_is_activated() != 0`, `Hat2` erst ab `> 1` (647-652).
- **Varianten:** Textur nach Team-Farbe (`RenderLizard.java:48-61`): `getHatColor() == 2` -> `Lizard2.png`, `== 3` -> `Lizard3.png`, sonst `Lizard.png`; die drei PNGs unterscheiden sich **nur** im Feld (30,40)-(50,50): Rot 208/7/7, Grün 7/208/35, Blau 7/7/208 (Pixel-Diff). Hut = Battle-Mob-Team (01-mobs.md „Battle Mobs“: Rot/Blau/Grün). Baby halb so groß.
- **Klang:** living null, hurt `alo_hurt`, death `alo_death` (`Lizard.java:106-116`).
- **Portierungshinweise:** `scale()` mit Baby-Zweig; `getTextureLocation` nach `hatColor` (synced, DataWatcher 21 in `EntityCannonFodder.java:47`); Hut-Teile über `visible` nach `is_activated` (DataWatcher 20).

### Lurking Terror (`lurking_terror`)

- **Aussehen:** flache, sechsbeinige, libellenartige Kreatur mit vier Kieferklappen und Zunge: Textur `LurkingTerror.png` 256x64 in Sumpfgrün (32/96/32, 32/96/0) mit dunkelroten Kieferinnenseiten (64/0/0), weißen Zahnspitzen und violetten Augenpunkten (Textur gesichtet). 59 Teile: `body`, `thorax`, `abdomen`, `head`, sechs Beine (vier à drei, zwei à zwei Segmente), vier Kiefer `jaw1..4` je mit `part2` und sechs Zähnen, dreiteilige Zunge, vier Flügel `wing_1..4` (`ModelLurkingTerror.json`).
- **Größe:** Hitbox 1.75 x 1.25 (`LurkingTerror.java:25`); Renderer `(model, 0.45, 0.85)` -> scale 0.85, Schatten 0.3825. Extent 50 x 12.5 x 60 (y 6..18.5) -> ~2.7 breit, ~0.66 dick, ~3.2 lang; das Modell schwebt 5.5 Einheiten (~0.3 Blöcke) über der Bodenlinie — ein Flieger (`motionY`-Steuerung `LurkingTerror.java:117, 175`).
- **Modell:** `ModelLurkingTerror`, 59 Teile, 256x64, kein Konstruktor-Arg (`wingspeed = 1.0`). Keine GL-Aufrufe.
- **Animationen (`ModelLurkingTerror.java:372-652`):** Flügel dauernd `0.455 ± cos(f2 * 1.4 * ws) * π * 0.2` (587-591). Beine zucken **zufällig**: pro Zyklus von `f2 * 0.7 * ws mod 2π` wird in `RenderInfo.ri1` eine Bitmaske neu gewürfelt (Chancen 1/3, 1/3, 1/4, 1/4, 1/6, 1/6; 381-409), gesetzte Bits lassen das jeweilige Bein `sin(...) * π * 0.25/0.15/0.1` auf Z pendeln (425-481). Kiefer: Bit `ri2` mit Chance 1/20 pro Zyklus oder erzwungen bei `getAttacking() != 0` (413-422); dann öffnen alle vier Klappen `|sin(f2 * 0.9 * ws) * π * 0.35|` (jaw1/2 auf Y, jaw3/4 auf X, 483-550) und die Zunge fährt aus (`tonguepart1/3.rotationPointZ = part2 - newangle * 5 / 10`, 582-583). Thorax atmet `sin(f2 * 0.1 * ws) * π * 0.06`, Abdomen hebt sich mit `sin * 14` (584-586). Kein Laufzyklus über `f`/`f1`.
- **Varianten:** keine.
- **Klang:** living `lurkinghorror_living`, hurt `lurkinghorror_hit`, death `lurkinghorror_dead` (`LurkingTerror.java:92-100`).
- **Portierungshinweise:** `scale()` -> 0.85; `RenderInfo` (`ri1`, `ri2`, `rf1`, `rf2`) clientseitig pro Entity halten; die Zufallsbits brauchen einen Client-`RandomSource` (im Original `worldObj.rand` im Renderpfad).

### Mantis (`mantis`)

- **Aussehen:** weiß-grüne Riesengottesanbeterin: Textur `Mantistexture.png` 256x256 mit weißen Flügeln (224/224/224 — 3209 Pixel) mit grünem Rautenmuster, dunkelroten Beinen/Armsegmenten (64/0/0), grün-weißen Rumpfsegmenten mit Adern und Zähnchen (Textur gesichtet). 36 Teile: Abdomen, Thorax, zwei Halsteile, zwei Kopfteile, zwei Augen, zwei Antennen, Fangarme `larm1..3`/`rarm1..3`, vier Flügel (`lfwing`, `rfwing`, `lrwing`, `rrwing`), vier Beine à vier Segmente (`ModelMantis.json`).
- **Größe:** Hitbox 2.5 x 3.25 (`Mantis.java:33`); Renderer `(model, 0.9, 1.1)` -> scale 1.1, Schatten 0.99. Extent 99 x 64 x 86 (y -36..28) -> ~6.8 breit (Flügel/Beine unrotiert), ~4.1 hoch ((24+36)*1.1/16), ~5.9 lang. Wiki nennt „about 3 blocks tall“ (01-mobs.md #25) — die Extent-Rechnung liegt höher, weil die Beine unrotiert gezählt sind; offen ohne Posenrechnung.
- **Modell:** `ModelMantis`, 36 Teile, 256x256, Konstruktor-Arg 2.0 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelMantis.java:234-304`):** Flügel zittern dauernd: vordere `∓0.698 ∓ cos(f2 * 0.9 * ws) * π * 0.25` auf Z, hintere `∓0.349 ± cos(...) * π * 0.35` (239-244). Fangarme: ruhig `a1 = -0.2` + `cos(f2 * 0.051 * ws) * π * 0.013`, Angriff (`getAttacking() != 0`, `Mantis.java:57`) `a1 = -0.698` + `cos(f2 * 0.51 * ws) * π * 0.25`; Segment 2 `-a1 - newangle`, Segment 3 `a1 + newangle`, Positionen kinematisch mit Längen 22 und 17 (245-267). Beine, Kopf, Antennen statisch — kein Laufzyklus.
- **Varianten:** keine.
- **Klang:** living `Beebuzz` (manifest `beebuzz`), hurt `dragonfly_hurt`, death `alo_death` (`Mantis.java:74-82`).
- **Portierungshinweise:** `scale()` -> 1.1; `getAttacking()` synced. Gerades `LayerDefinition`.

### Molenoid (`molenoid`)

- **Aussehen:** Sternnasenmaulwurf im Riesenformat: Textur `Molenoidtexture.png` 256x256 mit dichtem dunkelbraunem Fell (32/0/0, 64/32/32, 96/64/32 — 20800 opake Pixel), hellbeigen Handflächen und weiß-grauen Krallen-/Zahnstreifen, dazu eine Reihe heller Zähnchen für die Nasensterne (Textur gesichtet). 37 Teile: `body`, `shoulders`, `head1..3`, sechs Nasenstern-Lappen `nosestar1..6`, Arme `larm/rarm` mit `lhand/rhand` und je vier Krallen, `butt`, `tail`, Beine mit Fuß und je vier Zehen (`ModelMolenoid.json`).
- **Größe:** Hitbox 3.9 x 2.6 (`Molenoid.java:25`); Renderer `(model, 1.0, 1.0)` -> scale 1.0, Schatten 1.0. Extent 98 x 23 x 107 (y 1..24) -> ~6.1 breit (Arme unrotiert seitlich), ~1.4 hoch, ~6.7 lang — ein flacher, breiter Brocken.
- **Modell:** `ModelMolenoid`, 37 Teile, 256x256, Konstruktor-Arg 0.5 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelMolenoid.java:240-368`):** Arme ruderndes Graben auf **Y**: Angriff `cos(f2 * 1.7 * ws) * π * 0.25`, Laufen `cos(f2 * 1.3 * ws) * π * 0.25 * f1`, sonst 0 (245-253); `larm ±0.628 + newangle`, Hand `newangle * 1.25`, Krallen `newangle * 1.5 ∓ 0.174`, Positionen mit Längen 15 und 10 nachgeführt (254-285). Beine analog mit `-newangle` (nur Laufen) und Zehen (286-323). Nasenstern: alle sechs Lappen rotieren auf Z mit `cos(f2 * 0.1 * ws) * π` und Versätzen 0/0.523/1.047/1.57/-1.047/-0.523 (324-330) — ein sich langsam drehendes Sternrad an der Schnauze.
- **Varianten:** keine.
- **Klang:** living `molenoid_living` (3 Dateien), hurt `molenoid_hit` (6), death `molenoid_death` (`Molenoid.java:83-87`, manifest).
- **Portierungshinweise:** kein `scale()`; `getAttacking()` synced. Nasenstern-Teile brauchen `rotateAngleZ` mit vollem 2π-Hub.

### Mothra (`mothra`)

- **Aussehen:** ein **zehnfach skalierter Schmetterling**: Modell `ModelButterfly` (10 Teile: `body` 1x1x8, `head` 1³, je Seite vier flache Flügelplatten 5x1x5, 6x1x7, 5x1x5, 1x1x7; `ModelButterfly.json`), Textur `eyemoth.png` 64x32 (`EntityButterfly.java:55-57, 312`): dunkelgrüne/schwarze Flügel mit zwei großen Augenflecken (blauer Ring, gelb-rot-orange Iris) je Flügelseite, rosa Punkte auf dem Körper (Textur gesichtet). **Abweichung zum Manifest:** dort steht als Renderer-Textur `minecraft:textures/entity/creeper/creeper_armor.png` — das ist nur die Overlay-Textur des zweiten Passes (`RenderButterfly.java:89`); die Haut kommt aus `EntityButterfly.getTexture` (`RenderButterfly.java:83-86`). Über allem liegt der Creeper-Energie-Schleier: `shouldRenderPass` (`RenderButterfly.java:42-80`) zeichnet für `Mothra` (und Luna-Moth Typ 0) einen zweiten Pass mit `creeper_armor.png`, Texturmatrix verschoben um `ticksExisted * 0.01` in u und v, `glColor4f(0.5, 0.5, 0.5, 1)`, `glBlendFunc(1, 1)` (additiv), Licht aus — das blau wabernde Netz des aufgeladenen Creepers.
- **Größe:** Hitbox 5.0 x 2.0 (`Mothra.java:40`); Renderer `RenderButterfly(model, 0.75, 10.0)` -> scale **10.0**, Schatten 7.5 (manifest, renderers_dump Zeile 27). Extent 15 x 1 x 19 -> ~9.4 breit, ~0.6 dick, ~11.9 lang; Flügelpivot y 17 -> Körper ~4.4 Blöcke über der Bodenlinie ((24-17)*10/16).
- **Modell:** `ModelButterfly`, 10 Teile, 64x32, Konstruktor-Arg 0.2 = `wingspeed` (Butterfly 1.0, Luna 0.75 — Mothra schlägt am langsamsten). Keine GL-Aufrufe im Modell.
- **Animationen (`ModelButterfly.java:78-99`):** alle acht Flügelteile `rotateAngleZ = ±cos(f2 * 1.3 * ws) * π * 0.25` — mit ws 0.2 ein langsames, weites Schlagen; sonst nichts.
- **Varianten:** keine Texturvarianten für Mothra (die anderen `butterfly*.png`, `lunamoth.png`, `darkmoth.png`, `firemoth.png` gehören zu `EntityButterfly`/`EntityLunaMoth`).
- **Klang:** living/hurt null, death `random.explode` (`Mothra.java:89-101`); alle 30 Ticks `orespawn:MothraWings` (`Mothra.java:130-135`, manifest `mothrawings1..3`); Schüsse mit `random.bow`/`random.fuse` (399-424).
- **Portierungshinweise:** `scale()` -> 10.0; `getTextureLocation` -> `textures/entity/eyemoth.png`. Der Energie-Pass wird eine `EnergySwirlLayer`-Kopie (wie `CreeperPowerLayer`): `RenderType.energySwirl(creeper_armor, u, v)` mit `u = v = ageInTicks * 0.01`, Farbe 0.5/0.5/0.5. Das Original macht ihn nur für `Mothra` und `EntityLunaMoth.moth_type == 0`.

### Nastysaurus (`nastysaurus`)

- **Aussehen:** armloser Raubsaurier, „bigger and even nastier than the T. Rex“ (01-mobs.md #41): Textur `Nastysaurustexture.png` 512x256 mit rosa-braun marmorierter, felsig wirkender Haut (128/96/96, 160/128/96), schwarzen Schattenkanten, rotem Augenfeld und dunklen Krallen (Textur gesichtet). 59 Teile: `body`, `body2`, drei Halsglieder, `head3`, `head7`, Kiefer `jaw1`/`jaw5`, 23 Zähne `tooth1..23`, vierteiliger Schwanz, drei Rückenspikes `Spike1..3`, Beine à drei Segmente mit je sieben Krallen `lclaw1..7`/`rclaw1..7` (`ModelNastysaurus.json`). Keine Arme.
- **Größe:** Hitbox 2.2 x 4.6 (`Nastysaurus.java:29`); Renderer `(model, 1.0, 1.5)` -> scale 1.5, Schatten 1.5. Extent 24 x 57 x 101 (y -33..24) -> ~2.25 breit, ~5.3 hoch, ~9.5 lang.
- **Modell:** `ModelNastysaurus`, 59 Teile, 512x256, Konstruktor-Arg 0.65 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelNastysaurus.java:372-725`):** Kopf-Yaw gedämpft: `f3 %= 360; f3 *= 0.35`; `neck2` `toRadians(f3) * 0.5`, Kopf/Kiefer/alle Zähne `toRadians(f3)` (387-451). Kiefer: Angriff `0.5 + cos(f2 * 0.85 * ws) * π * 0.16`; sonst zufälliges Schnappen (pro `f2 * 0.7 * ws`-Zyklus mit 1/20 `RenderInfo.ri1 = 1`, dann `0.5 + sin(...)`), Grundstellung `π/16` (452-503). Beine: `pscale = 2`, Krallenblock hebt sich `clawY - sin(f2 * ws / 2) * 4 * f1` und schwingt `clawZ + 10 * cos(...) * f1`, rechts um π versetzt (503-622); Segmente `-0.523/-0.576/0.977 + cos * π * 0.15/0.06 * f1`, Nachführung mit 17. Schwanz: Angriff `tailspeed 0.76`, `tailamp 0.25`, sonst `0.26/0.08`; zwei Glieder auf Y mit Nachführung 11 (653-664).
- **Varianten:** keine.
- **Klang:** living `alo_living`, hurt `alo_hurt`, death `alo_death` (`Nastysaurus.java:113-117`, manifest).
- **Portierungshinweise:** `scale()` -> 1.5; `RenderInfo.ri1/rf1` clientseitig; `getAttacking()` synced. Die Krallen sind 14 Einzelteile, die dieselbe Position kopieren — im Port als Kinder eines Fuß-Parts modellierbar, ohne die Optik zu ändern.

### Ostrich (`ostrich`)

- **Aussehen:** schwarzer Strauß mit fleischfarbenen Beinen und Hals: Textur `Ostrichtexture.png` 256x128 mit schwarz-dunkelrotem Gefieder (32/0/0, 32/32/32), hellrosa Hals-/Beinhaut (224/192/192), braunem Schnabel, blauem Auge, rotem Hut-Feld bei UV 40,0 (Textur gesichtet). 38 Teile: vier Rumpfteile, Hals `Neck1/neck2`, Kopf `Head1/head/mouth1`, Beine à zwei Segmente + je vier Fußteile und vier Krallen, Schwanz `Tail1..3`, Flügel `Lwing/Rwing` 1x7x11, `Hat1/Hat2` (5x1x5, 4x2x4; `ModelOstrich.java:234-239`).
- **Größe:** Hitbox 0.85 x 2.1 (`Ostrich.java:40`); Renderer `(model, 0.55, 1.0)` -> scale 1.0, Schatten 0.55; Baby scale / 2 (`RenderOstrich.java:36-42`). Extent 10 x 47 x 37 (y -23..24) -> ~0.6 breit, ~2.9 hoch, ~2.3 lang.
- **Modell:** `ModelOstrich`, 38 Teile, 256x128, Konstruktor-Arg 0.65 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelOstrich.java:246-389`):** Beine laufen **nicht** über `f1`, sondern über die echte Positionsdifferenz: `lspeed = sqrt(Δx² + Δz²)`, `newangle = cos(f2 * 1.25 * ws) * π * lspeed * 0.4`, geklemmt auf ±0.75 (255-263); Beinsegmente `-0.297/0.483/-0.437 ± newangle`, Füße/Krallen mitgedreht (264-285). Schwanz wedelt langsam (`cos(f2 * 0.05) * π * 0.06`, seitlich 0.061/0.072; 286-290). Kopf-Yaw: geritten -> geglättet über `RenderInfo.rf1` aus `(prevYaw - yaw) * 20`, ±50 (291-304); sonst `f3 / 2`; dann `toRadians(f3) * 0.65`. **Sitzen** (`isSitting() && get_is_activated() == 0`): Kopf, Hals, Schnabel und Hüte auf `rotateAngleX = 3.1415` — der Kopf klappt nach hinten auf den Rücken (308-316). Flügel flattern **zufällig**: bei jedem Nulldurchgang von `cos(f2 * 1.0 * ws)` wird `ri1` mit 1/3 neu gesetzt, dann `|cos| * π * 0.15` auf Z und halb auf Y (330-345). Hüte nur bei `get_is_activated() != 0`, `Hat2` ab `> 1` (383-388).
- **Varianten:** Textur nach Hutfarbe (`RenderOstrich.java:48-61`): 2 -> `Ostrichtexture2.png` (Grün), 3 -> `Ostrichtexture3.png` (Blau), sonst Rot; Unterschied nur im Feld (40,0)-(60,10) (Pixel-Diff). Baby halb so groß.
- **Klang:** living null in beiden Zweigen (`Ostrich.java:251-256` — der `isSitting()`-Test ist wirkungslos, beide Äste geben null); hurt `cryo_hurt`, death `cryo_death` (258-264); Pitch bei Kindern 1.5 (289).
- **Portierungshinweise:** `scale()` mit Baby-Zweig; `getTextureLocation` nach `hatColor`; `RenderInfo.rf1/ri1` clientseitig; die Laufanimation braucht `xo/zo` gegen `getX()/getZ()` statt `walkAnimation`.

### Peacock (`peacock`)

- **Aussehen:** kleiner Pfau: Textur `peacocktexture.png` 128x128, nur 581 opake Pixel — grüner Körper (0/96/0), blau (0/32/192) für Hals und Schwanzfedern mit grün-blauem Augenmuster, gelbe Kopffedern, orangener Schnabel, weiße und rote Akzente auf den Federaugen (Textur gesichtet). 16 Teile: `lleg/rleg`, `body`, `neck`, `head1/2`, drei Kopffedern `hf1..3`, sieben Schwanzfedern `tailf1..7` (`ModelPeacock.json`).
- **Größe:** Hitbox 0.65 x 1.2 (`Peacock.java:31`); Renderer `(model, 0.25, 1.0)` -> scale 1.0, Schatten 0.25; Baby scale / 2 (`RenderPeacock.java:34-40`). Extent 8 x 21 x 49 (y 3..24) -> ~0.5 breit, ~1.3 hoch, ~3.1 lang (Schleppe flach nach hinten).
- **Modell:** `ModelPeacock`, 16 Teile, 128x128, Konstruktor-Arg 0.75 = `wingspeed`. Keine GL-Aufrufe.
- **Animationen (`ModelPeacock.java:114-179`):** Beine `±cos(f2 * 1.3 * ws) * π * 0.15 * f1` (119-126). **Radschlagen** über `getBlink()` (`Peacock.java:63`; `blinker` wechselt zwischen 0 und 1 mit Zufallsdauern 50+rand(300) bzw. 25+rand(100) Ticks, Zeile 67-79): `blinker > 0` -> Kopffedern 0.401/-0.174/-0.698, alle Schwanzfedern `rotateAngleX = 1.047` und fächern auf Z mit -0.4/-0.8/-1.2/0.4/0.8/1.2 (127-144); sonst Kopffedern -1.06 und Schwanz flach 0 (145-162).
- **Varianten:** keine Textur-Varianten (`peacock_1/2.png` sind die Rüstungs-Layer aus `ItemOreSpawnArmor`). Baby halb so groß.
- **Klang:** living `peacocklive` mit 1/8 Chance je Aufruf (`Peacock.java:112-117`), hurt `peacockhit`, death `peacockdead`, Lautstärke 0.4 (119-127).
- **Portierungshinweise:** `scale()` mit Baby-Zweig; `blinker` muss synced sein (im Original ein Server-Feld, das der Client-Renderer liest — offen: im 1.7.10-Original läuft `onUpdate` auch clientseitig, deshalb sichtbar; im Port explizit `SynchedEntityData`).

---

## Offen

- Leon: Spannweite in der Flugpose und Mantis-Höhe nur durch Posenrechnung (rotierte Boxen) bestimmbar; hier stehen die unrotierten Extents.
- Irukandji hurt-Sound `little_splt` (Tippfehler): im Port stumm lassen oder korrigieren — Entscheidung nötig.
- Island/IslandToo: ob das Dreifarb-Gebilde im Spiel sichtbar ist oder in der Insel steckt, ist ohne Client nicht belegbar.
