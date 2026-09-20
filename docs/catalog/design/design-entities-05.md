# Design: design-entities-05

Dieser Batch ist visuell dreigeteilt. Die **Village-Dimension-Roboter** (`Robot1`–`Robot5`, `SpiderRobot`) teilen sich eine Sprache: fast vollständig schwarze Texturen (70–83 % der deckenden Pixel sind `#000000`, manifest/PNG-Auszählung), graue Kantenlinien und Panelrahmen, dazu sparsame violette Leuchtpunkte an Kopf, Kanone oder Antrieb — Blockroboter aus Kisten, Kugeln und Rädern, keine organischen Formen. Die **Wasser- und Wüstenmobs** (`SeaMonster`, `SeaViper`, `Scorpion`, `Skate`, `Rat`, `SpitBug`, `Pointysaurus`) sind fotografisch-marmorierte Texturen (rostbraun, leuchtblau, hellgrau) auf langen, kettenanimierten Körpern, bei denen Hals oder Schwanz als Segmentkette per `rotationPoint`-Nachführung im `render()` laufen. Die **Kleintiere und Spielzeuge** (`RubberDucky`, `Cockateil`/`RubyBird`, `Spyro`, `StinkBug`, `RockBase`, `Rotator`, `Shoes`, `RedCow`) sind flächig bunt (Gelb, Rot, Magenta, Orange), zwischen 0,06 und 0,8 Blöcken hoch, und der `Rotator` ist der einzige „generierte" Mob: drei Boxen, je achtmal um die Achse gedreht, ergeben drei ineinander kreiselnde Ringe. Ganz oben steht der `Nightmare` (`PitchBlack`): 101 Teile, Hammerhai-Kopf mit Augen an Stielen, drei­gliedrige Flügel und gegabelter Stachelschwanz, in fünf Größenstufen von 1,7 bis fast 14 Blöcken.

Alle Größenangaben unten: „Ruhepose" ist die statische Ausdehnung aller Boxen aus `reference/jar/models/<Model>.json` (Pivot + Box, Rotation aus dem Konstruktor in GL-Reihenfolge `glRotatef(Z)·glRotatef(Y)·glRotatef(X)`, also auf den Eckpunkt zuerst X, dann Y, dann Z angewandt; 16 px = 1 Block), multipliziert mit dem Renderer-Skalierungsfaktor (`renderer_args[2]` im Manifest = `this.scale` in `Render<X>.java:19`). Der Hitbox-Wert ist `size` aus dem Manifest (`setSize` im Konstruktor). Legacy-Texturpfade stehen im Manifest `renderers[].textures[][2]`, die neuen Pfade in `textures[][0]`. Farbangaben in „Aussehen" sind Pixelzählungen der deckenden Pixel der Original-PNG, die Kanäle dabei auf 32er-Stufen abgerundet (`#583307` zählt also als `#402000`); die Prozentwerte beziehen sich auf diese Stufen, nicht auf exakte Farbwerte.

---

### Nightmare (`nightmare`)

Klasse `PitchBlack extends EntityMob` (PitchBlack.java:19), Modell `ModelPitchBlack`, Renderer `RenderPitchBlack`.

**Aussehen.** Eine schwarze Wyvern mit Hammerhai-Kopf. Textur `pitchblacktexture.png` (512×256): 51 % reines Schwarz, 37 % `#202020`, 7 % `#404040` — eine matte, grob gesteinsartig marmorierte Anthrazitfläche (PNG-Auszählung); die Flügelmembranen (`mem1–3`, `rmem1–3`, uv 10,60 ff.) sind rein schwarze Rechtecke mit gezackter Hinterkante, die Zähne (`tooth1–23`, alle uv 0,0) und die Flügelklauen hellgrau, dazu winzige dunkelrote Pixel an Kopf und Kiefer (Augen/Zahnfleisch, ca. 2 % `#a08080`). Silhouette (models/ModelPitchBlack.json):

| Gruppe | Teile | Form |
|---|---|---|
| Kopf | `head1` 36×1×12, `leye`/`reye` 3×3×15 bei x ±18…21, `head3` 4×4×16 Schnauze, `head4–6` Kamm 2×8×12 / 1×9×9 / 1×8×4 (−21°/−25°/−38°) | ein 36 px breites, flaches Kopfblech mit **Augen an den beiden Enden** (Hammerhai, 2,6 Blöcke × Skala breit), darüber ein nach hinten ansteigender Rückenkamm |
| Kiefer | `jaw1–5`, 23 Zähne (13 oben, 10 unten) | langer Unterkiefer 4×4×15, um +11° bis +23° gestaffelt |
| Hals/Rumpf | `neck1–3` 8→4 px, `body` 12×12×9, `body2` 12×14×16, `lshoulder`/`rshoulder` 3×2×6 | gedrungener Rumpf, Hals nach vorn-unten |
| Flügel | je Seite `wing1` 23×3×3, `wing2` 44×2×2, `wing3` 23×2×2 als Knochen, `mem1–3` 24/43/23×1×21 Membran, drei `wingclaw` 1×1×5…8 an der Spitze | dreigliedrig, Ruhe­spannweite 2·(6+21+43+23) ≈ 186 px = **11,6 Blöcke × Skala** |
| Beine | `leftleg1` 5×10×10 (−33°), `leftleg2` 4×12×5 (+56°), `leftleg3` 3×18×4 (−30°), `llegspike` 1×12×1 Sporn, `lclaw1–7` Zehen (±38°) — rechts gespiegelt | Z-förmig angewinkelte Raptorbeine mit gespreizten Krallen |
| Schwanz | `tail1–5` 8→3 px, ab `tail6/7` **gegabelt** (±10°), `tail8/9`, `tailpoint1/2` 1×1×22 Spitzen, 6 `tailspike` | zweizinkiger Stachelschwanz bis z 107 px |

**Größe.** Hitbox im Konstruktor 2,0×3,0 (PitchBlack.java:36, manifest), wird nach der Größenwahl auf **2,5·s × 3,5·s** gesetzt (PitchBlack.java:120, :126, :222). Skala s: Standard 0,5; danach 1/4 → 1,0, 1/8 → 2,0, 1/32 → 3,0, 1/64 → 4,0, spätere Würfe überschreiben frühere (PitchBlack.java:73–100); Config `NightmareSize` 1…5 erzwingt 0,5/1/2/3/4 (PitchBlack.java:102–116, Default 0 = zufällig, manifest). Gespeichert als int s·10 in DataWatcher 22 (PitchBlack.java:152–162), NBT `Fscale` (:133). Renderer: `glScalef(pscale)` aus `getPitchBlackScale()` (RenderPitchBlack.java:34–37). Ruhepose y −31,1…24 px = 3,44 Blöcke hoch, z −28…107,5 px = 8,5 Blöcke lang, Spannweite 11,6 Blöcke (models/ModelPitchBlack.json, Rechnung) → gerendert **1,7 / 3,4 / 6,9 / 10,3 / 13,8 Blöcke hoch** und **5,8 / 11,6 / 23 / 35 / 46 Blöcke Spannweite** für s = 0,5/1/2/3/4. Werte: Leben 250·s = 125…1000 (PitchBlack.java:211, manifest `Nightmare_health` 250) — bleibt unter 1024, kein virtuelles Leben nötig; Rüstung 10 + 2·s = 11…18 (PitchBlack.java:165, `Nightmare_defense` 10); Angriff 30·s = 15…120 (PitchBlack.java:54, `Nightmare_attack` 30). Das deckt sich mit den Wiki-Tabellen [FW] in docs/research/01-mobs.md:284; die abweichende [NW]-Reihe (01-mobs.md:285) ist damit widerlegt.

**Modell.** `ModelPitchBlack`, 101 Teile / 101 Boxen, alle auf Wurzelebene (depth 0), Textur 512×256, Konstruktorargument 0,65 = `wingspeed` (ModelPitchBlack.java:114). Keine GL-Aufrufe, keine Transluzenz (manifest `gl: {}`).

**Animationen** (ModelPitchBlack.java:624–1355; `f1` = Schwungstärke, `f2` = Schwungdistanz, `f3` = Kopf-Yaw; alle Zeitbasen werden durch `pscale` geteilt, große Exemplare schlagen also langsamer):

- **Flügel**: fliegend (`getActivity() != 0`) `cos(f2·0.45·ws/s)·π·0.24` (±43°) an `wing1`; `wing2` = 5/3 davon, am Ende von `wing1` (21 px) nachgeführt, `wing3` = 2×, am Ende von `wing2` (43 px), Klauen 1,5× (:636–711). Am Boden: −π/4 + `cos(f2·0.05·ws/s)·π·0.02` — Flügel 45° herabgeklappt mit leichtem Atmen.
- **Kopf**: `f3 % 360`, fliegend ×0,2, am Boden ×0,55; `neck3` halb, alle Kopf-, Kiefer-, Zahn- und Augenteile folgen (:712–802).
- **Kiefer**: angreifend `cos(f2·0.85·ws)·π·0.16 + 0.5` (0,5 ± 0,5 rad Kauen); sonst mit 1/20 Chance pro `f2`-Zyklus (Phasenerkennung über `RenderInfo.rf1`/`ri1`, :808–825) ein Kauzyklus, sonst π/16 leicht offen.
- **Beine**: am Boden pendeln die Zehenblöcke `rotationPointZ = 7 + 12·s·cos(f2·0.75·ws/s)·f1`, Hub `rotationPointY = 21 − 6·s·sin(…)·f1` nur in der positiven Halbwelle (Fuß hebt ab); Ober-/Unterschenkel `−0.576 / 0.977 / −0.61 + cos·π·0.18·f1`, rechtes Bein um π versetzt (:864–1033). Fliegend: Krallen eingezogen (`clawY = 9`, `rotateAngleX −0.7 ± cos(f2·0.85·ws/s)·0.2` nur beim Angriff — Beine zappeln), Segmente folgen (:1034–1175).
- **Schwanz**: neun Glieder als Kosinuskette mit Phasenversatz π/4, `tailspeed` 0,76/s beim Angriff sonst 0,26/s, Amplitude 0,25 bzw. 0,08; die Gabelung `tail6/7` mit ±0,174 rad, die Spitzen laufen mit (:1176–1252).

**Varianten.** Nur die Skala; eine Textur.

**Klang.** `pitchblack_living` (1/5 pro Aufruf, PitchBlack.java:196–199), `pitchblack_hit`, `pitchblack_dead`; `mothrawings` beim Fliegen aus dem Entity-Tick (PitchBlack.java:226, Literal `orespawn:MothraWings`). Lautstärke 0,75 (:188); Tonhöhe `1 − 0.7·(4/s)` (:192) — für s < 4 negativ. offen: wie 1.7.10 negative Pitch-Werte klemmt, ist nicht aus dem Jar belegt; im Port auf 0,5…2,0 klemmen und den Wert nachrechnen.

**Portierungshinweise Modell/Renderer.** `LivingEntityRenderer.scale()` mit `pscale` überschreiben; Schatten 1,25 (manifest `renderer_args[1]`). Skala als `SynchedEntityData<Integer>` (×10) plus `EntityDimensions.scalable(2.5f, 3.5f)` und `refreshDimensions()` beim Setzen. Die Kiefer-Zufallslogik (`RenderInfo.ri1/rf1`) ist Client-Renderzustand pro Entity — im Port in einen client-seitigen Zustandshalter (Map im Renderer oder schlichte float-Felder der Entity, die der Server nie liest). Alle 101 Teile sind Wurzelteile mit manueller Positionskette; 1:1 als Wurzel-`ModelPart`s mit derselben `x/y/z`-Nachführung in `setupAnim` portieren. **Culling**: bei s = 4 reicht die Spannweite 46 Blöcke weit über die 10×14-Hitbox hinaus — `getBoundingBoxForCulling` erweitern, sonst verschwindet die Wyvern am Bildrand. Tonhöhe siehe oben.

---

### Pointysaurus (`pointysaurus`)

Klasse `Pointysaurus extends EntityMob`, Modell `ModelPointysaurus`, Renderer `RenderPointysaurus`.

**Aussehen.** Ein Triceratops in Dunkelbraun mit grünem Nackenschild. Textur `pointysaurustexture.png` (128×128): 41 % `#200000` und 22 % Schwarz als dunkle, rotbraun geäderte Schuppenhaut (im vergrößerten PNG: faserige rot-schwarze Maserung), zwei grün marmorierte Flächen (`#204020`, uv 60,34 = `guard`) für den Nackenschild, weiß-graue Hörner (uv 0,18 und 52,13), kleine weiße Augen mit schwarzer Pupille (PNG). Form (models/ModelPointysaurus.json): `body1` 22×9×30 Rumpf, `body2` 18×7×15 Schulterbuckel, `body3` 16×6×11 Hüftbuckel, `head` 12×10×12 um −11° gesenkt, `nose` 10×6×5, **`guard` 28×23×3** Nackenschild um −15° geneigt mit 16 `bump` 2×2×2 am Rand, `lhorn`/`rhorn` 2×2×23 Brauenhörner (±8° nach außen, −9° nach oben), `chorn` 3×3×5 Nasenhorn, `tail` 6×6×9 (+16°), Vorderbeine 6×8×6, Hinterbeine 8×8×8.

**Größe.** Hitbox 2,9×2,9 (manifest). Ruhepose y −12,3…24 px = **2,27 Blöcke hoch** (Schildoberkante/Hornspitzen), z −32,2…24,5 px = 3,5 Blöcke lang, 2,0 Blöcke breit (Schild) (models/ModelPointysaurus.json, Rechnung); Renderer-Skala 1,0 (manifest) → unverändert. Werte 80 / 10 / 16 (manifest `Pointysaurus_stats`). Wiki [NW] nennt Defense 6 (01-mobs.md:528) — Config-Default ist 16.

**Modell.** 30 Teile / 30 Boxen, Wurzelebene, Textur 128×128, ctor 1,0 = `wingspeed`. Keine GL-Aufrufe.

**Animationen** (ModelPointysaurus.java:198–296): Beine `cos(f2·1.3·ws)·π·0.25·f1`, diagonal gepaart (`lfleg`+`rrleg` gegen `rfleg`+`lrleg`); Kopf Yaw `f3·0.45` und Pitch `f4·0.45`, Nase, Hörner (Yaw ∓0,14, Pitch −0,16), Schild (Pitch −0,262) und alle 16 Randnoppen folgen; Schwanz seitlich `cos(f2·1.3·ws)·π·0.25` beim Angriff, sonst `cos(f2·0.3·ws)·π·0.05`, Schwanz-Pitch langsam `cos(f2·0.02·ws)·π·0.15 + 0.28`.

**Varianten.** Keine.

**Klang.** `alo_living`, `alo_hurt`, `alo_death` (geteilt mit `Alosaurus`, manifest), Tonhöhe 1,5 (Pointysaurus.java:96–98).

**Portierungshinweise.** Gerade `LayerDefinition`; `scale()` 1,0, Schatten 1,0. Kopfgruppe (Kopf, Nase, drei Hörner, Schild, 16 Noppen) kann im Port unter einen Kopf-Parent gehängt werden, da alle denselben Pivot (0,11,−7) teilen — 1:1 bleibt aber die Wurzelform.

---

### Rat (`rat`)

Klasse `Rat extends EntityMob`, Modell `ModelRat`, Renderer `RenderRat`.

**Aussehen.** Eine flache, langschwänzige Ratte. Textur `rattexture.png` (64×64): Rumpf und Kopf in Schwarz/Dunkelbraun (`#000000` 36 %, `#402000` 28 %) mit stumpf mauvebraunen Flanken (`#604040`), braune Ohr- und Pfotenquadrate, Augen als magentafarbene Pixel im Kopf, und ein blass sandfarbener Streifen (`#e0c080`, uv 0,43) für den nackten Schwanz (PNG vergrößert). Form (models/ModelRat.json): `body` 5×3×10, `body2` 1×1×6 als Rückenkamm, `head` 3×2×4, `nose` 1×1×2, Ohren 1×1×1, `tail1` 2×2×9 + `tail2` 1×1×12 (21 px Schwanz), Vorderbeine 1×2×1, Hinterbeine 2×4×2.

**Größe.** Hitbox 0,25×0,5 (manifest). Ruhepose y 18…24 px = 0,38 Blöcke, Länge z −9…28 px = 2,31 Blöcke inkl. Schwanz (Rumpf+Kopf allein 19 px); Skala 0,75 (manifest) → **0,28 Blöcke hoch, 1,73 Blöcke lang**. Werte 5 / 3 / 1 (manifest `Rat_stats`).

**Modell.** 12 Teile, 64×64, ctor 1,0. Keine GL-Aufrufe.

**Animationen** (ModelRat.java:90–127): Beine `cos(f2·1.7·ws)·π·0.25·f1` diagonal wechselnd; Schwanz seitlich: angreifend `cos(f2·1.5·ws)·π·0.25`, sonst `cos(f2·0.4·ws)·π·0.05`; `tail1` ×0,5, `tail2` ×1,25 und am Ende von `tail1` (9 px) nachgeführt.

**Varianten.** Keine.

**Klang.** `ratlive`, `rathit`, `ratdead` (Rat.java:93–103), Lautstärke 0,45.

**Portierungshinweise.** Gerade `LayerDefinition`; Schatten 0,1, `scale()` 0,75.

---

### Apple Cow (`apple_cow`)

Klasse `RedCow extends EntityCow` (RedCow.java:8), Vanilla-Modell `ModelCow`, Renderer `RenderEnchantedCow` (geteilt mit `GoldCow`, `EnchantedCow`, `CrystalCow`).

**Aussehen.** Eine Vanilla-Kuh im Apfelbaum-Kleid. Textur `red_cow.png` (64×32, Vanilla-Kuh-Layout): 46 % Dunkelrot (`#400000`) als Grundfell, darauf leuchtend rote Apfel-Kleckse (`#e00020` 13 %), dunkelgrüne Blattflecken (`#004000` 10 %), grau-weiße Fellflecken, rosa Euter und Maul (PNG vergrößert). Modellform ist die Vanilla-Kuh (Kopf mit Hörnern, 4 Beine, Euter).

**Größe.** Kein `setSize` in `RedCow` (manifest `size: None`); offen: Hitbox aus `EntityCow` geerbt, im Jar nicht belegt. Modell = Vanilla-Kuh, Renderer-Schatten 0,7 (manifest `renderer_args`), keine Skalierung.

**Modell.** Vanilla `ModelCow` → 1.21.1 `CowModel`; keine eigenen Teile.

**Animationen.** Vanilla-Vierbeiner (`ModelQuadruped`): Beinpendel und Kopf-Yaw/Pitch.

**Varianten.** Texturwahl nach Klasse (RenderEnchantedCow.java:42–53): `RedCow` → `red_cow.png`; `GoldCow` und `EnchantedCow` → `gold_cow.png`; `CrystalCow` → `crystal_cow.png`. Nur `EnchantedCow` bekommt einen zweiten Renderpass (`shouldRenderPass` bei `par2 == 3` → 31, RenderEnchantedCow.java:33–40); für die Apple Cow gilt −1, also kein Overlay.

**Klang.** Vanilla-Kuh (keine `orespawn:`-Sounds referenziert, manifest).

**Portierungshinweise.** `CowRenderer`-Unterklasse mit `getTextureLocation` nach Entity-Klasse; `CowModel` mit `ModelLayers.COW`. Neue Pfade `textures/entity/red_cow.png`, `gold_cow.png`, `crystal_cow.png`. Der Overlay-Pass für `EnchantedCow` (Alpha-Blend, Wert 31 = Bits 16|15) ist nicht Teil dieses Eintrags.

---

### Bomb-Omb (`bomb_omb`)

Klasse `Robot1 extends EntityMob`, Modell `ModelRobot1`, Renderer `RenderRobot1`.

**Aussehen.** Ein Mario-Bob-omb: schwarze Kugel mit weißen Augen, grauer Kappe, Zündschnur, gelben Füßen und Aufziehschlüssel. Textur `robot1.png` (64×32): 70 % Schwarz (uv 0,0, ein großer schwarzer Block für alle Kugelsegmente), zwei weiße 2×3-Felder (uv 32,0 = `Shape15`/`Shape15a`, die Augen), ein graues Feld (uv 45,0 = `Shape16`, die Kappe), ein gelbes Feld (uv 33,7 und 46,8 = Zündschnur, Füße, Schlüssel) (PNG vergrößert). Form (models/ModelRobot1.json): die Kugel ist aus 16 gestapelten Boxen gebaut (`Shape1`–`Shape14`, `Shape2a`, breiteste `Shape9`/`Shape11`/`Shape13` 9 px, y 13…24), Augen 2×3×1 vorn bei z −4, `Shape16` 3×1×3 Kappe auf y 12, `Shape17` 1×2×1 Zündschnur + `Shape18` um 55° geknickt, `rfoot`/`lfoot` 2×2×4 gelbe Füße, `key1–5` bilden einen Schlüssel hinten bei z 5…8.

**Größe.** Hitbox 0,5×0,5 (manifest). Ruhepose y 8,7…24 px = 0,96 Blöcke hoch, 0,56 breit, 0,81 tief (mit Schlüssel); Skala 1,0 → **≈ 1 Block hoch**. Werte 5 / 4 / 2 (manifest; Rüstung Robot1.java:90–92).

**Modell.** 27 Teile, 64×32, ctor 2,0 = `wingspeed` (ModelRobot1.java:40).

**Animationen** (ModelRobot1.java:180–226): Füße `cos(f2·1.5·ws)·π·0.75·f1` gegenläufig (mit ws = 2 ein schneller, weiter Watschelgang); Schlüssel dreht sich um Z mit `toRadians(f2·0.75·ws)` — er zieht sich beim Laufen kontinuierlich auf. Laufzeit-Effekte aus der Entity: Rauch- und Lava-Partikel bei `posY + 1`, sobald ein Ziel gefunden ist (Robot1.java:107–110), dann Explosion Stärke 2,5 mit 1/18-Chance bei Abstand² < 5 (Robot1.java:103–105).

**Varianten.** Keine.

**Klang.** `kyuubi_living`, `scorpion_hit`, `robot1_death` (Robot1.java:116–126).

**Portierungshinweise.** Gerade `LayerDefinition`; Schatten 0,3, `scale()` 1,0. Explosion serverseitig, Partikel client­seitig (im Original beides aus `onLivingUpdate`).

---

### Robo-Pounder (`robo_pounder`)

Klasse `Robot2 extends EntityMob`, Modell `ModelRobot2`, Renderer `RenderRobot2`.

**Aussehen.** Ein sechs Blöcke hoher Klotzroboter mit hängenden Windmühlenarmen. Textur `robot2.png` (256×512): 72 % Schwarz, 16 % Grau `#606060` als Kantenlinien und Leiterbahn-Muster auf den Panels, ~9 % Violett (`#8000c0`…`#a000e0`) für den Kopf (uv 50,10) und die Schulterplatte (uv 16,400) — violette Leuchtflächen auf Schwarz (PNG). Form (models/ModelRobot2.json): Füße `rleg1`/`lleg1` 16×24×16, Schienbeine `rleg2`/`lleg2` 12×24×12, Hüfte `Shape3` 26×8×12, Taille `Shape6` 8×8×8, Brust `Shape7` 26×8×12, **Schulterplatte `Shape8` 44×18×14**, Arme je drei Glieder (`arm3` 16×24×17 Schulter, `arm2`/`arm1` 12×24×12) 72 px lang bis y 24 (Bodenhöhe), `head` 15×12×10 oben auf y −78.

**Größe.** Hitbox 3,0×6,2 (manifest). Ruhepose y −78…24 px = **6,4 Blöcke hoch**, x ±38 px = 4,75 Blöcke breit, 1,06 tief; Skala 1,0 (manifest). Werte 200 / 22 / 18 (manifest `Robot2_stats`).

**Modell.** 15 Teile, 256×512, ctor 1,0.

**Animationen** (ModelRobot2.java:108–176): Beine `cos(f2·0.3·ws)·π·0.12·f1` (langsames Stampfen ±22°); Kopf Yaw `f3`; **Arme**: bei jedem Vorzeichenwechsel von `sin(rad(f2·20·ws))` wird `RenderInfo.ri1` neu gewürfelt — 0 ohne Ziel, sonst 1…3 (`rand.nextInt(4)` bis ≠ 0); rechter Arm (ri1 1|3) und/oder linker Arm (ri1 2|3) rotieren um X mit `toRadians(f2·20·ws)`, also volle Kreise, 20° pro Schwung­einheit — die Windmühle, die das Wiki beschreibt (01-mobs.md:572). Sonst hängen sie gerade.

**Varianten.** Keine.

**Klang.** `robot_living` (1/4), `robot_hurt`, `robot_death` (Robot2.java:112–125).

**Portierungshinweise.** Gerade `LayerDefinition`; Schatten 1,0. `ri1` ist Client-Renderzustand (Zufall pro Entity), im Port client­seitig halten; die Würfelung nutzt `worldObj.rand` im Render — im Port `RandomSource` des Renderers.

---

### Robo-Gunner (`robo_gunner`)

Klasse `Robot3 extends EntityMob`, Modell `ModelRobot3`, Renderer `RenderRobot3`.

**Aussehen.** Ein gebückter Würfeltorso auf gespreizten Beinen mit Laserkasten vorn. Textur `robot3.png` (512×512): 74 % Schwarz, 26 % Grau `#606060`, nur einzelne violette Pixel — schwarze Platten mit grauen Rahmen- und Rasterlinien (PNG). Form (models/ModelRobot3.json): Füße 16×29×16, Schienbeine 14×29×14 um ±16° gespreizt, `hips` 18×16×16, `waist1–3` 12×12×12/17 gestaffelt und um −6°/11° geneigt, Torso aus `body1`/`body2` 15×47×47 Seitenplatten + `body3` 47×47×25 Rücken + `body4` 18×16×22 Kern (ein 47-px-Würfel, 11° vorgeneigt), **`lazer` 17×16×22** vorn unten (23°), Arme je `arm3` 20×18×18 Schulter + `arm2` 14×29×14 + `arm1` 14×37×14, um ±57° nach vorn geknickt.

**Größe.** Hitbox 2,5×5,0 (manifest). Ruhepose y −119,5…24 px = 8,97 Blöcke, 5,44 breit, 4,5 tief; **Renderer-Skala 0,5** (manifest `renderer_args[2]`, RenderRobot3.java:19) → **≈ 4,5 Blöcke hoch, 2,7 breit, 2,3 tief**. Werte 80 / 16 / 14 (manifest `Robot3_stats`).

**Modell.** 19 Teile, 512×512, ctor 1,0.

**Animationen** (ModelRobot3.java:132–188): Beine `cos(f2·0.55·ws)·π·0.12·f1`; `lazer` Yaw `f3/2`; Arme: Grundhaltung `arm1 −1`, `arm2/arm3 +1` rad, dazu Schwung `cos(f2·1.0·ws)·π·0.15` nur wenn `ri1 == 1` (wird beim Nulldurchgang auf 1 gesetzt, wenn `getAttacking() != 0`).

**Varianten.** Keine.

**Klang.** `robot_living` (1/4), `robot_hurt`, `robot_death` (Robot3.java:111–124).

**Portierungshinweise.** `scale()` 0,5, Schatten 1,0·0,5 (manifest `shadow_expr` L2·L3). Sonst gerade `LayerDefinition`.

---

### Robo-Warrior (`robo_warrior`)

Klasse `Robot4 extends EntityMob`, Modell `ModelRobot4`, Renderer `RenderRobot4`.

**Aussehen.** Ein humanoider Kampfroboter mit Vogelbeinen, Rückenstacheln, Schildarm rechts und Armkanone links. Textur `robot4.png` (512×512): 74 % Schwarz, 21 % Grau, ~4 % Violett; die violetten Felder liegen bei uv 150…350,20 (`glowycannonbit1–5`) und 400,400 (`cannonammo`) — die Kanone glüht violett, der Rest sind schwarze Panzerplatten mit grauen Quadratrahmen (PNG). Form (models/ModelRobot4.json): Beine digitigrad (`thigh` 6×13×8 −10°, `calf` 8×8×9, `shin` 6×13×6 +10°, `kneegaurd` 7×7×1 36°, vierteilige Füße mit `foottip`), Rumpf `hips`/`stomach`/`torso`/`chest` um 8–14° vorgeneigt, `neck` 50°, `head` 6×6×8 um 30° gesenkt (geduckt), drei Paar Rückenstacheln (`spinebase` 2×8×2 + `spinetip` 1×7…8×1, −8°/−40°/−60° nach hinten), rechts `sholdergaurd` 4×12×9 + **Schild** `sheildbase` 3×12×19, `sheildtip`, `sheildend`, links `cannonbase` 8×12×8, `cannonend` 6×4×6, vier Seitenstücke 3×6×3, fünf `glowycannonbit` 2×5×2, `cannonammo` 5×5×5, alles um −40° gekippt.

**Größe.** Hitbox 2,5×4,0 (manifest). Ruhepose y −35…24 px = **3,7 Blöcke hoch**, 2,5 breit, 2,2 tief; Skala 1,0. Werte 170 / 12 / 18 (manifest `Robot4_stats`).

**Modell.** 56 Teile, 512×512, ctor 1,0.

**Animationen** (ModelRobot4.java:355–509): Beine `cos(f2·0.5·ws)·π·0.15·f1` auf alle Beinteile, `calf +0.175`, `kneegaurd +0.63`, `thigh −0.175`, rechts negiert; Kopf Yaw `f3/1.5`; **Schildarm**: beim Angriff `|cos(rad(f2 % 360)·ws·6)|·π/4 + 0.75` → Arm zwischen 0,75 und 1,53 rad gehoben, Schild `+1.047`, sonst 0; **das Modell schreibt Spielzustand**: `e.setShielding(1)` sobald der Winkel > π/12 (:393–398), und `attackEntityFrom` verweigert Schaden bei `getShielding() != 0` (Robot4.java:325–327). **Kanonenarm**: 0,85 rad beim Angriff, sonst 0; Kanonenteile `−0.7`, Leuchtstücke `+0.17/+0.08`, Position folgt dem Unterarm (`cos/sin(rotateAngleX)·14`, :427–452). Entity-Partikel: Rauch bei `posY + 3` (1/3 der Ticks), `reddust` aus der Kanone beim Angriff (Robot4.java:117–124); `fireworks.launch` beim Schuss (:301–305).

**Varianten.** Keine.

**Klang.** `robot_living` (1/4), `robot_hurt`, `robot_death` (Robot4.java:141–154).

**Portierungshinweise.** Gerade `LayerDefinition`, Schatten 1,0. **Guardrail-Konflikt**: `setShielding` darf im Port nicht aus `setupAnim` kommen (Client-Modell auf dediziertem Server nicht vorhanden) — den Schildzustand serverseitig aus derselben Formel (`|cos(rad(tickCount·…)·6)|·π/4 + 0.75 > π/12` beim Angreifen) berechnen und nur synchronisieren; das Modell liest ihn dann. Damit ist das Verhalten identisch, nur die Richtung der Datenflusses dreht sich.

---

### Robo-Sniper (`robo_sniper`)

Klasse `Robot5 extends EntityMob`, Modell `ModelRobot5`, Renderer `RenderRobot5`.

**Aussehen.** Ein Geschützturm auf zwei Sternrädern. Textur `robot5texture.png` (128×128): 70 % Schwarz, 30 % Grau (`#606060`/`#404040`) mit einem violetten Pixel; die Räder sind schwarze Quadrate mit grauem Rahmen (uv 0,23 / 0,43), Rohr und Stange schwarz (PNG vergrößert). Form (models/ModelRobot5.json): `lwheel1/2`, `rwheel1/2` je zwei 2×8×8-Boxen um 45° gegeneinander gedreht (achtzackige Sternräder) bei x ±6…8, `axle` 12×1×1, `drivebox` 4×3×3, `stand` 1×18×1 Stange bis y 0, `swivel` 2×1×2, **`barrel1` 2×2×13 + `barrel2` 1×1×9** (22 px Rohr nach z −19), `ammobox` 4×3×5 dahinter.

**Größe.** Hitbox 1,0×2,25 (manifest). Ruhepose y −2…24,7 px = **1,67 Blöcke hoch**, 1,69 lang (Rohr), 1,0 breit; Skala 1,0. Werte 20 / 5 / 6 (manifest `Robot5_stats`).

**Modell.** 11 Teile, 128×128, ctor 1,0.

**Animationen** (ModelRobot5.java:83–117): Räder `rotateAngleX = |f2·0.15 mod 2π|` (rollen mit der Wegstrecke), das zweite Sternblatt +45°; `barrel1`, `barrel2`, `ammobox` Yaw `f3/2` (Turm dreht dem Ziel halb nach).

**Varianten.** Keine.

**Klang.** `robot_living`, `robot_hurt`, `robot_death` (manifest), Lautstärke 0,5 (Robot5.java:98–100).

**Portierungshinweise.** Gerade `LayerDefinition`; Schatten 0,5.

---

### Rock (`rock`)

Klasse `RockBase extends EntityLiving` (RockBase.java:10) — ein „lebender" Stein, der am Boden liegt. Modell `ModelRockBase`, Renderer `RenderRockBase`.

**Aussehen.** Zwölf Steinsorten, alle wenige Pixel groß. Typ in DataWatcher 20 (RockBase.java:47–58); Würfelung beim ersten Tick (RockBase.java:87–135): außerhalb `DimensionID5` Typ 1, dann 1/10 → 2, 1/20 → 3, 1/30 → 4, 1/40 → 5, 1/50 → 6, 1/100 → 7, 1/200 → 8, je 1/500 → 9/10/11, 1/1000 → 12 (spätere Treffer überschreiben); in `DimensionID5` Typ 9, 1/3 → 10, 1/5 → 11, 1/10 → 12. Gezeichnete Teile (ModelRockBase.java:151–200) und Textur (RenderRockBase.java:53–108):

| Typ | Teile | Textur | Farbe (PNG) |
|---|---|---|---|
| 1 | `RockSmallShape1/2` 2×1×1 + 3×1×1 (Kiesel) | `rocktexture.png` | Grau `#404040` mit Schwarz |
| 2 | `RockShape1–3` 6×1×2 + 3×1×1 + 2×1×1 (flacher Stein) | `rocktexture.png` | Grau |
| 3 / 4 / 5 / 6 | `RockShape1–3` | `rockredtexture` / `rockgreentexture` / `rockbluetexture` / `rockpurpletexture` | Grau mit rotem / grünem / blauem / violettem Stich (`#802000`, `#008000`, `#000080`, `#600080`) |
| 7 | `RockSpikeyShape1–3` (Querstücke um 90° gedreht) | `rocktexture.png` | Grau |
| 8 | `RockTNTShape1–4` (Extra 3×1×3) | `rocktnttexture.png` | Rot `#a00000` mit Hellgrau/Weiß — TNT-Optik |
| 9 / 10 / 11 / 12 | `CrystalShape1` 2×5×2, `CrystalShape2` 1×3×1 Spitze, `3a–d` 1×5×1 um ±31°, `4a–d` 1×3×1 um ±75° (Kristallcluster) | `rockcrystaltexture` / `rockcrystalgreentexture` / `rockcrystalbluetexture` / `rockcrystaltnttexture` | Tiefrot / Dunkelgrün `#002000` / Dunkelblau `#000040` / Rot mit Schwarz |

Typ 9–12 wird **transluzent** gezeichnet: `glEnable(GL_BLEND)`, `glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)`, Lightmap 240/240 (voll hell), `glColor4f(0.75, 0.75, 0.75, 0.55)` (ModelRockBase.java:176–193). Client-Partikel je Typ mit 1/20 pro Tick: 9 `flame`, 10 `happyVillager`, 11 `smoke`, 12 `fireworksSpark` (RockBase.java:143–154).

**Größe.** Hitbox 0,25×0,15 (RockBase.java:19, manifest). Flache Steine 6×1×2 px = 0,38 Blöcke breit, 0,06 hoch; Kristall y 16…24 px = 0,5 Blöcke hoch (models/ModelRockBase.json); Skala 1,0, **Schatten 0,0** (manifest `renderer_args[1]`). Yaw und Pitch werden jeden Tick auf 0 gezwungen (RockBase.java:80–83). Leben `1 + Typ/4` (RockBase.java:136–137), Rüstung 0 (:67–69).

**Modell.** 22 Teile, 64×64, alle am Pivot (0,23,0); nur die Teile des aktuellen Typs werden gezeichnet.

**Animationen.** Keine.

**Varianten.** Siehe Tabelle; Typ 1, 2 und 7 teilen sich `rocktexture.png`.

**Klang.** `random.pop` (0,75, 2,25) bei Schlag durch ein Lebewesen (RockBase.java:41–43); kein Living/Hurt/Death-Sound (:158–168).

**Portierungshinweise.** Zwei Renderpfade in `renderToBuffer`: opak für Typ ≤ 8, für 9–12 `RenderType.entityTranslucent(texture)` mit Farbe ARGB `0x8CBFBFBF` (0,55·255 = 140) und `packedLight = 0xF000F0`. `getTextureLocation` nach Typ (synchronisierter int). Schatten 0.

---

### Rotator (`rotator`)

Klasse `Rotator extends EntityMob`, Modell `ModelRotator`, Renderer `RenderRotator`. „My first foray into a generated mob instead of a modeled one" (01-mobs.md:624) — und genau so ist es gebaut.

**Aussehen.** Drei ineinanderliegende Gyroskop-Ringe aus je acht Balken, die um drei verschiedene Achsen kreiseln, 1,5 Blöcke über dem Boden schwebend. Textur `rotatortexture.png` (64×32): drei Streifen — Schwarz (uv 0,0 = `Shape3`), Rot `#e00000` (uv 0,7 = `Shape2`), Gelb `#e0e000` (uv 0,12 = `Shape1`) (PNG). Geometrie (models/ModelRotator.json): `Shape3` 14×3×3 bei y 13,7, `Shape2` 8×2×2 bei y 7,6, `Shape1` 4×1×1 bei y 3,9, alle mit Pivot (0,0,0…−1); `render()` (ModelRotator.java:36–72) zeichnet jede Box **achtmal** mit `rotateAngleZ` in 45°-Schritten → drei Ringe in der XY-Ebene: außen schwarz (Radius 13,7…16,7 px, Außendurchmesser **≈ 2,1 Blöcke**), mitte rot (≈ 1,2 Blöcke), innen gelb (≈ 0,6 Blöcke). Vor jedem Ring `glRotatef(rf1, …)`: außen um X, mitte um Y, innen um Z; `rf1 += 2` pro Render-Aufruf, Rücksetzen bei > 359 (:66–70) — die Drehung ist **framerate-gebunden** (60 fps → 120°/s).

**Größe.** Hitbox 1,0×2,0 (manifest). Modell-Ursprung y = 0 liegt 24 px = 1,5 Blöcke über dem Boden, der Außenring reicht also von ≈ 0,46 bis 2,54 Blöcke Höhe; Skala 1,0, Schatten 0,1 (manifest). Werte 35 / 10 / 8 (manifest `Rotator_stats`). Entity-Partikel: `fireworksSpark` bei `posY + 1.4` (1/10 der Ticks) und ein gerichteter Funkenstrahl zum Ziel jeden Tick (Rotator.java:118–127) — die „blinding sparks" des Wikis; `motionY *= 0.6` lässt ihn schweben (:117).

**Modell.** 3 Teile, 64×32, ctor 0,25 = `wingspeed` (ungenutzt im `render()`), sechs `glRotatef` (manifest `gl`).

**Animationen.** Nur die drei Achsdrehungen über `RenderInfo.rf1`; keine Abhängigkeit von Bewegung oder Angriff.

**Varianten.** Keine.

**Klang.** `getLivingSound()` liefert `"vortexlive"` **ohne** `orespawn:`-Präfix (Rotator.java:87–89). Die Datei `sounds/vortexlive.ogg` und das Event `orespawn:vortexlive` existieren im Jar (reference/jar/sounds_dump.txt:122, referenziert von `Vortex`), aber der präfixlose Name löst in 1.7.10 nach `minecraft:vortexlive` auf — das Manifest führt den Sound deshalb nicht unter `Rotator`, und im Original ist der Living-Sound faktisch stumm. Belegt: `glasshit` (5 Dateien) als Hurt, `glassdead` (2) als Death (:91–97).

**Portierungshinweise.** Kein gerades `LayerDefinition`: `renderToBuffer` mit drei `poseStack.pushPose()`/`mulPose(Axis.XP/YP/ZP.rotationDegrees(rf1))`-Blöcken und je acht Zeichnungen derselben `ModelPart` bei `zRot = k·π/4`. `rf1` client­seitig halten; Vorschlag für den Port: `rf1 = (tickCount + partialTick)·2·(60/20)`… — offen: ob 2°/Frame (framerate-abhängig) 1:1 nachgebildet oder auf Tickzeit umgestellt wird, ist eine Entscheidung, nicht ableitbar. Living-Sound siehe oben — Entscheidung, keine Ableitung: 1:1 stumm lassen oder den offensichtlich gemeinten `orespawn:vortexlive` (Datei vorhanden) als dokumentierte Abweichung anbinden.

---

### Rubber Ducky (`rubber_ducky`)

Klasse `RubberDucky extends EntityTameable`, Modell `ModelRubberDucky`, Renderer `RenderRubberDucky`.

**Aussehen.** Eine Badeente aus sieben Kästen. Textur `rubberduckytexture.png` (64×64): 90 % Gelbtöne (`#e0e000`, `#e0c000`, `#e0a000`), weiße Flügelstreifen, orangefarbener Schnabel (uv 0,21), schwarze Augenpixel (PNG). Form (models/ModelRubberDucky.json): `bottom` 4×1×4, `body` 6×2×8, `back` 6×1×10, `neck` 2×1×2, `head` 4×4×4, `beak` 3×1×3 vorn bei z −5, `Lwing`/`Rwing` 2×1×5 seitlich.

**Größe.** Hitbox 0,33×0,5 (manifest, RubberDucky.java:44). Ruhepose y 15…24 px = 0,56 Blöcke, 0,81 lang; Skala 0,75 → **0,42 Blöcke hoch, 0,61 lang**; Küken halbiert (RenderRubberDucky.java:36–38). Werte 5 / 6 / 1 (manifest).

**Modell.** 8 Teile, 64×64, ctor 1,0.

**Animationen** (ModelRubberDucky.java:66–119): Kopf Yaw `f3·0.45`, Pitch `f4·0.65`, Schnabel folgt; Flügel: beim Nulldurchgang von `cos(f2·1.0·ws)` wird `ri1` mit 1/3 auf 1 gesetzt (böse Ente 1/2 und Amplitude ×4); dann `|cos(…)·π·0.15|` als `Lwing.rotateAngleZ = −a`, `rotateAngleY = a/2`, rechts gespiegelt — Flügelschlagen in Schüben; sitzend keine Bewegung.

**Varianten.** `getKillCount() >= 5` → `evilrubberduckytexture.png` (RenderRubberDucky.java:47–55): gleiches Layout in Fast-Schwarz und Dunkelrot (`#200000`, `#000000`, `#400000`) mit **rot glühenden Augen** (`#a00000`) (PNG). Killcount synchronisiert (RubberDucky.java:510–517), NBT `Killcount` (:499); die böse Ente macht doppelten Schaden (:426–430) und lässt sich nicht mehr hinsetzen (:274).

**Klang.** `duck_hurt` als Living (1/10), Hurt und Death (RubberDucky.java:181–194), Lautstärke 0,8, Tonhöhe 1,2.

**Portierungshinweise.** Gerade `LayerDefinition`; `scale()` 0,75 bzw. 0,375 für Babys (`isBaby()`), Schatten 0,15. Texturwahl über synchronisierten Killcount; `ri1` client­seitig.

---

### Ruby Bird (`ruby_bird`)

Klasse `RubyBird extends Cockateil` (RubyBird.java:5), Modell `ModelCockateil`, Renderer `RenderCockateil`.

**Aussehen.** Ein knallroter Nymphensittich mit Haube. `Cockateil` wählt die Textur über `getBirdType()` 0…5 aus `Bird1–6.png` (Cockateil.java:52–76, :263–268); `RubyBird` erzwingt Typ 5 (RubyBird.java:14) → **`Bird6.png`** (64×32; neuer Pfad `textures/entity/bird6.png` per manifest `texture_map`; das Manifest listet für `ruby_bird` selbst keine Textur, weil sie erst in der Entity aufgelöst wird). Farben: 48 % Rottöne (`#800000`, `#a00000`, `#e00000`), 13 % Weiß (Bauch, Flügelspitzen), Schwarz, Orange `#e0a000` für Beine und Schnabel, eine goldgelbe Haubenfeder-Fläche (PNG vergrößert). Form (models/ModelCockateil.json): `Body` 5×3×6, `Head` 3×3×4, `Beak` 1×1×3 + `LowerBeak`, drei Haubenfedern 1×3…4×1 um −13°/−37°/−73° nach hinten, Flügel `rwing1`/`lwing1` 1×4×4 um ±90° angelegt + `wing2` 3×1×3 Spitzen, drei Schwanzfedern (3×2×3, 4×1×4, 5×1×4) bis z 17, zwei Beine 1×3×1.

**Größe.** Hitbox 0,5×0,5 (Cockateil.java:37, manifest). Ruhepose y 13,5…23,5 px = 0,63 Blöcke, 1,44 lang, 1,19 breit; Skala 0,75 → **0,47 Blöcke hoch, 1,08 lang**. Leben 2 (Cockateil.java:123–125), Angriff 1,0 (manifest).

**Modell.** 16 Teile, 64×32, ctor 1,0.

**Animationen** (ModelCockateil.java:114–149): Flügel `cos(f2·1.5·ws)·π·0.35` auf `lwing1 = −1.5 + a`, `rwing1 = 1.5 − a`, Spitzen ±a; Schwanzfedern Pitch `cos(f2·0.3·ws)·π·0.1`; Haubenfedern Roll `cos(f2·1.1/1.2/1.3·ws)·π·0.08`. Alles hängt an `f2` (Schwungdistanz), läuft also nur, wenn der Vogel sich bewegt; ohne `f1`-Faktor.

**Varianten.** Innerhalb `Cockateil` sechs Texturen nach `birdtype`; für `RubyBird` fest Typ 5.

**Klang.** `rubybird` tagsüber ohne Regen (RubyBird.java:19–24), sonst stumm; Hurt/Death `duck_hurt` (Cockateil.java:111–117), Lautstärke 0,55.

**Portierungshinweise.** Gerade `LayerDefinition`, Schatten 0,3, `scale()` 0,75. `getTextureLocation` über synchronisierten `birdtype` (DataWatcher 22, Cockateil.java:78–94) — `RubyBird` setzt ihn im `defineSynchedData`-Äquivalent auf 5.

---

### Scorpion (`scorpion`)

Klasse `Scorpion extends EntityMob`, Modell `ModelScorpion`, Renderer `RenderScorpion`.

**Aussehen.** Ein hellgrauer Steinskorpion. Textur `scorpion.png` (**88×24**, nicht Zweierpotenz): 50 % Hellgrau `#e0e0e0`, 38 % Mittelgrau, dunkelrote Augenpunkte auf dem Kopf (uv 28,9), orangeroter Stachelpixel, dunkelrot gepunktete Zangen-/Körperflächen rechts oben und links unten (PNG vergrößert). Form (models/ModelScorpion.json): `body` 6×4×8, `head` 5×3×4, Schwanz `tail1` 4×4×5 (15°) → `tail2` 3×3×5 (59°) → `tail3` 3×3×4 (99°) → `tail4` 2×2×5 (144°) → `tail5` 2×2×4, `tail6` 1×1×3 (180°) — über den Rücken nach vorn gekrümmt; acht Beine 11×2×2 (Yaw ±28°/±14°, Roll ±21°), Scherenarme `larm2`/`rarm2` 6×2×2 + `larm1`/`rarm1` 2×2×3 + `lclaw`/`rclaw` 3×2×4 vorn bei z −10.

**Größe.** Hitbox 0,85×0,55 (manifest). Ruhepose y 7…23,9 px = 1,05 Blöcke (Schwanzspitze), x ±12,4 px = 1,55 breit, z −13,7…11,1 px = 1,55 lang; Skala 0,75 → **0,79 Blöcke hoch, 1,16 breit, 1,16 lang**. Werte 15 / 4 / 10 (manifest `Scorpion_stats`).

**Modell.** 22 Teile, 88×24, ctor 0,62 = `wingspeed`.

**Animationen** (ModelScorpion.java:150–267): Beine Yaw `cos(f2·2.0·ws − k·π/2)·π·0.12·f1` in vier Phasengruppen mit Grundwinkeln ±0,49/±0,24; beim Nulldurchgang von `cos(f2·3.0·ws)` werden `ri1`, `ri2` gewürfelt — ohne Ziel `ri1 ∈ [0,20)`, `ri2 ∈ [0,25)` (selten), beim Angriff `ri1 ∈ [0,4)`, `ri2 ∈ [0,3)` (oft): linke Schere bei ri1 1|3, rechte bei 2|3 (`doLeftClaw`: `larm2.rotateAngleY = 0.52 + a`, Unterarm-Z `−sin·4.5`, Schere `0.381 − a`, :238–250), Schwanzstich bei ri2 == 1 (`doTail`: `tail1 0.26 + a`, `tail2 +0.769 + a`, `tail3 +0.701 + a`, `tail4 −5.501 − 1.5a − 0.4`, Glieder per sin/cos·4/3 nachgeführt, :252–267).

**Varianten.** Keine.

**Klang.** `scorpion_hit`, `cryo_death` (Scorpion.java:108–114), `scorpion_attack` (0,75, 1,5) beim Angriff (:159); Lautstärke 1,5.

**Portierungshinweise.** `LayerDefinition` mit 88×24 ist erlaubt (docs/research/06-models-design.md:183). `scale()` 0,75, Schatten 0,35. `ri1/ri2` client­seitig.

---

### Sea Monster (`sea_monster`)

Klasse `SeaMonster extends EntityMob`, Modell `ModelSeaMonster`, Renderer `RenderSeaMonster`.

**Aussehen.** Ein Plesiosaurier in Rostbraun. Textur `seamonstertexture.png` (256×128): 90 % Braun- und Rotbrauntöne (`#602020`, `#604020`, `#804020`, `#402000`) als rostig marmorierte Haut mit grünen Sprenkeln, rote Augen (uv 4,16 / 46,16), ein rot-weiß gepunktetes Maulfeld (uv 10,0 = `BottomJaw`, Zähne) (PNG vergrößert). Form (models/ModelSeaMonster.json): `BodyFront`/`BodyBack` je 16×16×16, Schwanz `TailBase` 14×14×12 → 12 → 10 → 8 → 6 → 4 → `TailTip` 2×2×6 bis z 76, Hals `NeckBase` 10×10×10 (45°) → `Neck2` 6×10×8 (40°) → `Neck3` 6×10×6 (30°) → `Neck4` 4×10×4 (15°) → `Neck5` 4×6×4 (45°) → `Neck6` 4×6×4 (70°), Kopf bei y −23 z −29: `TopJaw` 8×4×10, `BottomJaw` 8×3×10, Augen 2×2×1; vier Flossen 8×1×16 (Pitch −30°, Yaw ±40°).

**Größe.** Hitbox 1,25×2,5 (manifest). Ruhepose y −29…24,9 px = **3,4 Blöcke hoch** (Kopf), z −39…76 px = **7,2 Blöcke lang**, x ±22 px = 2,75 breit (Flossen); Skala 1,0, Schatten 1,0. Werte 110 / 14 / 8 (manifest `SeaMonster_stats`).

**Modell.** 23 Teile, 256×128, ctor 0,5 = `wingspeed`.

**Animationen** (ModelSeaMonster.java:156–282): Schwanz seitliche Welle `cos(f2·1.3·ws)·π·0.2·f1` (nur bewegt oder angreifend), auf die Glieder verteilt als /7, /6, /5, /4, /3, /2, /1 mit Positionsnachführung cos/sin·10/7/5/5/5/5; Flossen `cos(f2·1.2·ws)·π·0.2·f1` (ruhend ·0,02) auf Pitch/Yaw um −0,523/±0,698; Hals: bewegt `0.455·f1 + cos(f2·0.9·ws)·π·0.25·f1`, ruhend `cos(f2·0.3·ws)·π·0.02`, verteilt /5, /4, /3, /2, −/2, −/3 (S-Kurve, Kopf bleibt waagerecht), Glieder per sin/cos·9/9/9/9/5 nachgeführt, Kopfteile am Ende von `Neck6` (·5); Kopf Yaw `f3·0.5`; Unterkiefer angreifend `0.45 + cos(f2·1.7·ws)·π·0.17`, sonst `0.17 + cos(f2·0.2·ws)·π·0.05`.

**Varianten.** Keine.

**Klang.** `seamonster_living` (1/3), `seamonster_hit`, `seamonster_death` (SeaMonster.java:127–140); `splash` 1,5 beim Auftauchen (:519).

**Portierungshinweise.** Gerade `LayerDefinition` mit manueller Kettennachführung in `setupAnim`; Schatten 1,0.

---

### Sea Viper (`sea_viper`)

Klasse `SeaViper extends EntityMob`, Modell `ModelSeaViper`, Renderer `RenderSeaViper`.

**Aussehen.** Eine leuchtend blaue Seeschlange mit rotem Zungenstiel, roten Augen und grauen Giftzähnen. Textur `seavipertexture.png` (128×128): 82 % gesättigtes Blau (`#2020e0`…`#2060e0`, wolkig gemasert), 7 % Rot (`#a00000`/`#800000`: Zunge uv 60…80,3 und Augen uv 50/96,60 auf schwarzen Stielen), hellgraue Fangzähne (uv 60/92,18) (PNG vergrößert). Form (models/ModelSeaViper.json): `Head` 12×8×6, `MouthTop` 10×6×16, `MouthBottom` 8×2×12 um 30° geöffnet, Zunge `ToungBase` 2×1×6 + `MiddleTounge` + `ForkLeft/Right` (±25°) bis z −29, `FangLeft/Right` 1×5×1, Augen 1×3×4 seitlich; `Neck` 8×8×10 (−15°), dann **22 Schwanzglieder**: `tBase`…`t5` 8×8×10 mit Pitch −30/−60/−30/−5/0° (der Hals hebt den Kopf 1,2 Blöcke), `t6`…`t13` 8×8×10 im Zickzack (Yaw ±20/40°), `t14–16` 6×7×10, `t17–19` 4×6×10, `t20–21` 2×5×10, `TailTip` 2×5×10 bei z 152.

**Größe.** Hitbox 1,5×2,5 (manifest). Ruhepose z −30…160 px = **11,9 Blöcke lang**, y −2…24 px = 1,62 hoch, 1,58 breit; Skala 1,0, Schatten 1,0. Werte 160 / 22 / 12 (manifest `SeaViper_stats`), Tempo 0,25 oder 0,35 (manifest `movement_speed`).

**Modell.** 34 Teile, 128×128, ctor 0,5 = `wingspeed`; Helfer `doseg` (ModelSeaViper.java:417–425).

**Animationen** (ModelSeaViper.java:290–415): `tBase` Yaw `cos(f2·1.3·ws)·π·0.1·f1`; danach für jedes der 21 folgenden Glieder `doseg(inn, notinn, k, f1, f2)`: Position = Ende des Vorgängers (9 px · |cos Pitch|), Yaw `cos(f2·1.3·ws − k·π/4)·π·0.2·f1 + cos(−k·π/4)·(1 − f1)` — schwimmend eine laufende Welle, stillstehend (f1 = 0) ein fester Zickzack `cos(−kπ/4)`. Maul: angreifend `0.65 + cos(f2·1.7·ws)·π·0.17`, Zunge züngelt `cos(f2·4.7·ws)·π·0.07` mit `offsetZ = cos(f2·1.5·ws)·π·0.05`; ruhend `0.45 + cos(f2·0.2·ws)·π·0.02`, Zunge `cos(f2·1.7·ws)·π·0.03`, `offsetZ = cos(f2·0.5·ws)·π·0.05`. Kopf, Maul, Augen, Fänge Yaw `f3·0.5`, `MouthBottom` folgt dem Kopf per cos/sin·2, Zungengabel ±0,436.

**Varianten.** Keine.

**Klang.** `seaviper_living` (1/2), `seaviper_hit` (3 Dateien), `seaviper_death` (SeaViper.java:130–143).

**Portierungshinweise.** `offsetZ` gibt es an `ModelPart` nicht mehr — die Zungenverschiebung als zusätzliche `z`-Verschiebung der vier Zungenteile (Pivot + Offset) schreiben. Kettennachführung wie beim Sea Monster. Schatten 1,0.

---

### Shoes (`shoes`)

Klasse `Shoes extends EntityThrowable` (Shoes.java:10) — das Wurfgeschoss der Girlfriend/Boyfriend. Kein Modell.

**Gezeichnet wird** ein Sprite: `RenderShoe extends RenderSpinner` (RenderShoe.java:5–15) setzt `spinItemIconIndex = getShoeId()` und `RenderSpinner.doRender` (RenderSpinner.java:20–50) bindet `spinners.png` (256×256, 16×16-Raster), zeichnet die Zelle `index % 16, index / 16` als kamerazugewandtes 1×1-Quad, mit `glScalef(0.5)` → **0,5 Blöcke groß**, und dreht es um Z mit `rotationPitch`; `Shoes.onUpdate` erhöht `rotationPitch` um 20°/Tick (Shoes.java:91–100) → der Schuh wirbelt einmal pro 18 Ticks. `ShoeId` = `rand.nextInt(4) + 2` (2…5, Shoes.java:19/35/51) oder Konstruktorwert (:23–29, :39–45); Id 6 macht 6 statt 2 Schaden (:61–64). Zeilen 0, Zellen 2…6 von `spinners.png` (PNG vergrößert): 2 roter Stiletto-Absatz, 3 schwarzer Absatzschuh, 4 kleiner türkiser Schuh (offen: Sandale oder Flip-Flop, 16 px nicht eindeutig), 5 brauner Stiefel, 6 schwarzer Game-Controller (der harte Wurf). Beim Aufprall 4× `snowballpoof` + `reddust` (:82–85). Hitbox: offen, aus `EntityThrowable` geerbt (kein `setSize` in Shoes.java). Tracking 64/1/1 (manifest). `glEnable(32826)` = `GL_RESCALE_NORMAL` (RenderSpinner.java:24).

**Portierungshinweise.** Eigener `EntityRenderer<Shoes>` mit `poseStack.mulPose(camera.rotation())`, Skalierung 0,5, Roll um `rotationPitch`, Quad mit UV `(id%16·16, id/16·16)/256` aus einem `RenderType.entityCutoutNoCull(spinners.png)`. `ShoeId` als `SynchedEntityData<Integer>`. Derselbe Renderer bedient laut 06-models-design.md:219 auch `SunspotUrchin`, `WaterBall`, `InkSack`, `LaserBall`, `IceBall`, `Acid`, `DeadIrukandji`, `BerthaHit`, `EntityCage`.

---

### Skate (`skate`)

Klasse `Skate extends EntityMob`, Modell `ModelSkate`, Renderer `RenderSkate`.

**Aussehen.** Ein grauer Rochen aus drei Boxen. Textur `skatetexture.png` (64×32): Grau (`#808080` 53 %, `#a0a0a0` 34 %, `#606060`) mit einem dunklen Fleck auf der Scheibe (PNG). Form (models/ModelSkate.json): `body` 6×1×6 um **45° um Y gedreht** (Raute), `tail1` 1×1×11 dünner Schwanz bis z 14, `Shape1` 1×1×4 als um 45° aufgestellter Stachel.

**Größe.** Hitbox 0,75×0,25 (manifest). Ruhepose y 19,2…23 px = 0,24 Blöcke, 1,14 lang, 0,53 breit; Skala 0,75 → **0,18 Blöcke hoch, 0,86 lang** — praktisch flach. Werte 8 / 8 / 4 (manifest `Skate_stats`).

**Modell.** 3 Teile, 64×32, ctor 1,0.

**Animationen** (ModelSkate.java:36–51): nur der Stachel: `0.785 + cos(f2·1.2)·π·0.15·f1` bewegt, sonst `0.785 + cos(f2·0.4)·π·0.05` (ohne `wingspeed`-Faktor).

**Varianten.** Keine.

**Klang.** Nur Death `ratdead` (Skate.java:100–102), Lautstärke 0,33, Tonhöhe 1,75; Living und Hurt `null`.

**Portierungshinweise.** Gerade `LayerDefinition`; Schatten 0,1, `scale()` 0,75.

---

### Spider Driver (`spider_driver`)

Klasse `SpiderDriver extends EntitySpider` (SpiderDriver.java:11), Vanilla-Modell `ModelSpider`, Renderer `RenderSpiderDriver extends RenderSpider` (RenderSpiderDriver.java:9).

**Aussehen.** Eine dunkle, olivbraun getönte Vanilla-Spinne mit roten Augen. Textur `spiderdriver.png` (64×32, Vanilla-Spinnen-Layout): `#202020` 38 %, Schwarz 25 %, `#402020`/`#404020` — dunkelgraue Panzerung mit helleren grauen Rändern und dunkelroten Flecken, ein Cluster roter Augen (PNG vergrößert). Modellform ist die Vanilla-Spinne.

**Größe.** Kein `setSize` in `SpiderDriver` (manifest `size: None`); offen: aus `EntitySpider` geerbt, im Jar nicht belegt. Rüstung 20 zu Fuß, 8 als Reiter (SpiderDriver.java:83–88, manifest `armor_value [8, 20]`). Der Renderer-Konstruktor hat einen leeren Rumpf (RenderSpiderDriver.java:13–14) → impliziter `RenderSpider()`-Standardkonstruktor, also Vanilla-Schatten und Vanilla-Augenpass mit `spider_eyes.png`.

**Modell.** Vanilla `ModelSpider` → `SpiderModel`.

**Animationen.** Vanilla-Spinne (Beinpendel). Verhalten sichtbar: sucht einen `SpiderRobot` ohne Reiter im Umkreis 25 Blöcke und steigt auf (SpiderDriver.java:44–55, :90–107); als Reiter sitzt er 3 Blöcke hinter der Körpermitte des Roboters, 2,0 Blöcke über dessen Position (SpiderRobot.java:565–577).

**Varianten.** Keine.

**Klang.** Vanilla-Spinne (keine `orespawn:`-Referenz im Manifest).

**Portierungshinweise.** `SpiderRenderer`-Unterklasse (bzw. `MobRenderer<SpiderDriver, SpiderModel>`) mit eigener `getTextureLocation`; `SpiderEyesLayer` bleibt Vanilla. Neuer Pfad `textures/entity/spiderdriver.png`.

---

### Robot Spider (`robot_spider`)

Klasse `SpiderRobot extends EntityLiving` (SpiderRobot.java:20), Modell `ModelSpiderRobot`, Renderer `RenderSpiderRobot`.

**Aussehen.** Eine gigantische Schwarze-Witwe-Maschine. Textur `spiderrobottexture.png` (256×512): 83 % Schwarz, graue Panelrahmen, dunkelrotbraune Flächen (`#402020` 6 %), hellgraue Stacheln; auf dem Abdomen (uv 0,398) und auf dem Mittelkörper (uv 0,321) je eine **rote Sanduhr** — die Schwarze-Witwe-Marke; graue Beinsegmente mit rotem Punkt am Fußende (uv 0,28 ff.), rote Augenpunkte am Kopf (PNG vergrößert). Form (models/ModelSpiderRobot.json): `Head` 30×26×36, `BodyCenter` 36×24×51, **`Abdomen` 48×40×73** (3×2,5×4,6 Blöcke), `Tail` 10×10×49 nach hinten, zwei `HeadSpike` 2×2×21 als Fühler, Mandibeln `Ljaw1–3`/`Rjaw1–3` (8×3×8 Basis, 21×2×6, 23×1×4 Klingen, um 43°/98° bzw. 132°/81° gefaltet), acht `Hip` 10×10×10 entlang der Flanken, und **ein einziges Bein-Template** (`HipJoint` 8×8×16, `Leg1p1` 4×4×100, `UpperKnee2`, `Leg1p2` 3×3×100, `UpperKnee`/`LowerKnee2`, `Leg1p3` 2×2×100, `LowerKnee`, `LegBump1/2`, `Foot` 6×6×6, vier `FootSpike` 1×1×5, vier `AnkleSpike` 20 px), das achtmal mit den Winkeln aus `RenderSpiderRobotInfo` gezeichnet wird (ModelSpiderRobot.java:258–377). Jedes Beinsegment ist 100 px = **6,25 Blöcke** lang.

**Größe.** Hitbox 3,25×2,25 (SpiderRobot.java:43, manifest). Körper: z −70 (Fühlerspitze) … 113 (Schwanz) = 183 px = **11,4 Blöcke lang**, Abdomen y −34…6 → 2,5 Blöcke hoch; Renderer zeichnet mit `glScalef(−1, −1, 1)` und ohne den `RenderLiving`-Pfad (RenderSpiderRobot.java:22–30), Modell-y-negativ ist also Welt-oben, die Körpermitte liegt auf `posY` (Reiter bei `posY + 2.625`, Abgas-Partikel bei `posY + 2`, SpiderRobot.java:565–569, :639). Beine in der Initialpose: `p1xangle = π/4` (SpiderRobot.java:112), `p3 = −p1` (:328) → Knie 70 px = 4,4 Blöcke über der Körpermitte, Fuß 240 px ≈ 15 Blöcke seitlich → Spannweite bis ≈ 30 Blöcke. offen: die tatsächliche Beinstellung kommt aus der Client-IK `updateLegs` (SpiderRobot.java:230–410, nur `isRemote`), die Fußpunkte am Boden sucht; ein fester Rendermaßstab ist daraus nicht ableitbar. Werte 1500 / 100 / 16 (manifest `SpiderRobot_stats`) — **Leben 1500 liegt über der 1024-Klemme**, virtuelles Leben mit realem Originalwert 1500 nötig.

**Modell.** 39 Teile, 256×512, ctor 1,0; Tracking 128 (manifest).

**Animationen.** Beine vollständig aus `RenderSpiderRobotInfo` (RenderSpiderRobotInfo.java:5–31, acht Einträge je Feld): `ydisplayangle` (Yaw aller Beinteile), `uddisplayangle` + `p1xangle/p2xangle/p3xangle` (Pitch je Segment), Hüftposition aus `ymid`, `legoff`, `yoff` (·16), Segmentenden per sin/cos·99 nachgeführt (ModelSpiderRobot.java:301–357); Fußhebung über `footup`/`footingticker` in der Entity. Mandibeln: ruhend `Ljaw2 0.75`, `Ljaw3 1.71`, `Rjaw2 2.3`, `Rjaw3 1.41`; angreifend `± cos(gpcounter·0.25)·π·0.22` dazu (:378–394), `gpcounter` zählt pro Client-Tick (SpiderRobot.java:239). `f2` wird vom Renderer konstant mit −0,1 übergeben (RenderSpiderRobot.java:28) — kein Schwung-Input. Partikel: `flame` (1/8), `smoke` (1/2), `fireworksSpark` (1/10) als Abgas seitlich (`yaw − 90°`) bei `posY + 2` (SpiderRobot.java:636–646). Zerstört Gras unter den Füßen, wenn geritten (:410–415).

**Varianten.** Keine.

**Klang.** `robotspider` (11 Dateien, 1/80 pro Tick nur mit Reiter, Lautstärke 0,45, SpiderRobot.java:690–692), `robotspidermount` (0,65) beim Aufsitzen (:927).

**Portierungshinweise.** Nicht gerade: der Original-Renderer umgeht `RenderLiving.doRender` — **kein Schatten** (trotz 0,99 im Manifest), **kein Hurt-Rotton, keine Todesdrehung, kein Namensschild**, `preRenderScale` wird nie aufgerufen. Im Port entweder einen `EntityRenderer` mit derselben Pose (`translate`, `rotateY(180 − yaw)`, `scale(−1, −1, 1)`) oder `LivingEntityRenderer` mit dokumentierter Abweichung (dann kommt Schatten/Hurt-Tint hinzu). Die Bein-IK muss im Port als client-seitiger Tick (unter `com.swbr.orespawn.client`) laufen und pro Entity acht Winkel-Sätze halten; das Modell zeichnet das Bein-Template in einer Schleife aus `renderToBuffer` heraus (ein `ModelPart`-Satz, achtmal mit gesetzten Winkeln gerendert). Culling-Box auf Beinspannweite erweitern. Reiter-Offset 2,625 + `cos(rideTicker·0.19)·0.02` (2,0 für `SpiderDriver`), 3 Blöcke hinter der Mitte (SpiderRobot.java:565–577) — `getPassengerAttachmentPoint`.

---

### Spit Bug (`spit_bug`)

Klasse `SpitBug extends EntityMob`, Modell `ModelSpitBug`, Renderer `RenderSpitBug`.

**Aussehen.** Eine dunkle, zeckenartige Springwanze mit hochgezogenem Hinterleib und bezahntem Doppelkiefer. Textur `blisterbug.png` (512×256; das Renderer-Literal heißt noch `BlisterBug.png`, RenderSpitBug.java:47 — der Mob wurde umbenannt): 73 % `#202000` (schwarzoliv), 16 % `#200000`, 9 % `#402000` — sehr dunkle braun-olive Chitinplatten mit etwas helleren Kanten, grünliche Augenpixel (uv 36,37) (PNG vergrößert). Form (models/ModelSpitBug.json): Nabe `legintersection` 12×6×14 mit vier dreigliedrigen Beinen (Gelenk 6×6×4 + 5×5×4 + 4×4×2, `leg` 3×3×10, Schenkel `part2` 4×3×15 + `part2d` 3×3×12, `part3` 3×3×7, **Schienbein `part3b` 4×16×4 senkrecht** + Fuß `part3c`), um ±45°/±135° gespreizt; Rumpf `bodybase` 10×20×13 steil hoch (y 1 → −20), `bodybasepart2/3` 12–13×12×21, Buckel und geneigte Platten (`part7` −72°, `part8` −23°, `part9` −81°, `part10` 15×5×22 Rückenschild, `part12` 28°, `part13/14` ±58° Seitenflügel, `part15` −43°); Kopf: `upperjawbase` 7×5×6 bei y −13 z −15 mit drei Zähnen 2×2×7 (±21°), `lowerjawbase` 10×2×16 bei y 0 z −6 mit elf Leisten und zwei Zähnen 3×2×5; zwei kleine Arme (je drei Glieder) und Augen 1×2×2 bei x ±6,5.

**Größe.** Hitbox 2,0×2,0 (manifest). Ruhepose y −21…24,2 px = 2,82 Blöcke, x −26,9…27,7 px = 3,4 breit (Beinspreizung), z −29,7…29,3 px = 3,7 lang (models/ModelSpitBug.json, Rechnung in GL-Rotationsreihenfolge); Skala 0,75 → **2,1 Blöcke hoch, 2,6 breit, 2,8 lang**. Werte 100 / 10 / 12 (manifest `SpitBug_stats`). Springt mit `motionY += 0.75` (SpitBug.java:116–133).

**Modell.** 93 Teile, 512×256, ctor 0,55 = `wingspeed`; Helfer `doRightFrontLeg` u. a. (ModelSpitBug.java:719–933).

**Animationen** (ModelSpitBug.java:576–707): Beine: Yaw `sin(f2·2.0·ws)·π·0.12·f1`, Hub `upangle = |cos(f2·2.0·ws)·π·0.12·f1|` nur in der Vorwärts-Halbwelle (`nextangle > newangle`); links vorn und links hinten in Phase, rechte Seite um π versetzt; in den Helfern: `leg.rotateAngleY = ∓1.2 + a` (hinten ±2.1), Schenkelteile `−1.152/−0.743/−0.632/−1.041 + up`, `part3 0.669 − up`, Schienbein `−0.48 − up`, Positionen per cos/sin·14·cos(Pitch) und ·8 nachgeführt. Oberkiefer und Zähne: ruhend `|cos(f2·0.3·ws)·π·0.015|`, angreifend `|cos(f2·2.6·ws)·π·0.1|`, Zähne `0.26 + a`.

**Varianten.** Keine.

**Klang.** `clatter` (2), `crunch`, `emperorscorpion_death` (manifest `referenced_by`).

**Portierungshinweise.** Gerade `LayerDefinition`, aber 93 Wurzelteile mit Kettennachführung; `scale()` 0,75, Schatten 0,55. Texturdatei heißt weiter `blisterbug.png` (manifest `texture_map`).

---

### Baby Dragon (`baby_dragon`)

Klasse `Spyro extends EntityTameable` (Spyro.java:17), Modell `ModelSpyro`, Renderer `RenderSpyro`.

**Aussehen.** Ein kleiner schwarz-magenta Drache im Spyro-Stil. Textur `spyrotexture.png` (64×64): 73 % Schwarz als Körper, 11 % Magenta `#c000a0` (Bauch, Flügelmembran), 8 % Rot, 4 % Orange `#e06000` für Pfoten und Beine, rot-magenta gestreifte Hörner und Schwanzspitzen, violette Augen mit weißem Glanz (PNG vergrößert). Form (models/ModelSpyro.json): `Torso` 5×4×10, `Neck` 3×3×3 (26°), `HeadPieceTop` 5×3×4 + `HeadPieceBottom` 5×2×6 + `JawPiece` 3×1×3, zwei Nasenpixel, vier Hörner (Basis 2×3×2, Spitze 1×3×1, um −45° nach hinten und ±45° zur Seite), Rückenschuppen 1×1×2 / 1×2×2, **Flügel `WingLeft`/`WingRight` 10×0×4** (flache Platten, 10° geneigt), `TailBack` 2×2×4 (26°), `TailFront` 1×1×4, Schwanzspitzen; vier Beine aus zwei Gliedern + Pfote 2×1×4.

**Größe.** Hitbox 0,5×0,5 (Spyro.java:41, manifest). Ruhepose y 7,5…24,2 px = 1,04 Blöcke (Hornspitzen), 1,45 breit (Flügel), 1,25 lang (models/ModelSpyro.json, Rechnung in GL-Rotationsreihenfolge); Skala 0,75 → **0,78 Blöcke hoch, 1,09 breit, 0,94 lang**. Werte 200 / 5 / 5 (manifest; Leben Spyro.java:199–201).

**Modell.** 37 Teile, 64×64, ctor 0,65 = `wingspeed`.

**Animationen** (ModelSpyro.java:240–373), gesteuert durch `getActivity()` (DataWatcher 21, Spyro.java:173–181; 1 = am Boden/laufend, 2 = fliegend, Spyro.java:463–552) und `isSitting()`: Flügel `cos(f2·2.3·ws)·π·0.4·f1` als Roll ±, bei Aktivität 3 halbiert; Beine `cos(f2·2.0·ws)·π·0.25·f1` mit Grundwinkeln (vorn −0,087/−0,17, hinten +0,139/−0,174), bei Aktivität 3 eingefroren; **fliegend (2)**: Beine angezogen (vorn −1 rad, hinten +1 rad); Schwanz Yaw `cos(f2·1.2·ws)·π·0.25` mit 3-px-Kette, sitzend oder Aktivität 3 still; Kopf Yaw/Pitch `f3`/`f4`, Hörner ±0,785. Der Zweig „Aktivität 3" im Modell ist **tot**: `activity` nimmt in Spyro.java nur 0, 1 und 2 an (Spyro.java:33, :477, :503, :533–535, :547, :603–609), `setActivity(3)` kommt im ganzen Quellbaum nicht vor und keine Klasse erbt von `Spyro` (grep über reference/src-20.2). Im Port kann er entfallen.

**Varianten.** Keine (Textur fest, RenderSpyro.java:42–48).

**Klang.** `roar` (6 Dateien) nur fliegend und nicht sitzend (Spyro.java:352–360), `duck_hurt` als Hurt, `cryo_death` als Death; Lautstärke 0,4; Küken-Tonhöhe 1,5.

**Portierungshinweise.** Gerade `LayerDefinition`; die 10×0×4-Flügel sind Null-Dicke-Boxen — in 1.21.1 als `CubeDeformation`-freie Box mit Höhe 0 zulässig (rendert zwei Flächen). `scale()` 0,75, Schatten 0,65. Aktivität als `SynchedEntityData<Integer>`.

---

### Stink Bug (`stink_bug`)

Klasse `StinkBug extends EntityAnimal`, Modell `ModelStinkBug`, Renderer `RenderStinkBug`.

**Aussehen.** Ein schwarzer Käfer mit orangefarbenem Kopf und genoppter Hinterleibsspitze. Textur `stinkbug.png` (64×32): 92 % Schwarz, 5 % Orange `#e06020` (Kopfpixel und Kieferstreifen uv 28,0/28,8), 2 % Gelb `#e0c000` und Orange bei uv 0,0 — die 30 Ein-Pixel-Noppen (`b1–b8`, `t1–t22`, alle uv 0,0) und die Rückenstacheln `b9/b10` (uv 0,2) sind damit orange-gelb (PNG vergrößert). Form (models/ModelStinkBug.json): `body` 6×5×8, `head` 5×4×4 bis z −8, `jaw` 5×1×4 (7°), Fühler `h1/h2` 1×2×1 (30°, ±20°), sechs Beine `l1–l6` 2×1×2 + Füße `f1–f6` 2×2×2, `tail` 4×4×6 um −19° angehoben mit 22 Noppen `t1–t22` 1×1×1 in Reihen, acht Noppen `b1–b8` auf dem Rücken, `b9/b10` 1×2×1 Stacheln um ±30°.

**Größe.** Hitbox 0,55×0,55 (manifest). Ruhepose y 15,5…24 px = 0,53 Blöcke, 1,14 lang, 0,69 breit; Skala 0,85 → **0,45 Blöcke hoch, 0,97 lang**; Küken `scale/2` (RenderStinkBug.java:35–38). Leben 5, Angriff 0 (manifest).

**Modell.** 50 Teile, 64×32, ctor 0,75 = `wingspeed`.

**Animationen** (ModelStinkBug.java:418–546): Füße `sin(f2·3.1·ws)·π·0.3·f1` — durch Aliasing im Quellcode bekommen `f1` und `f3` +a, `f2`, `f4`, `f6` −a und **`f5` bleibt starr** (:423–436, Decompilat: `f2 = this.f3`); Rückenstacheln Roll `sin(f2·0.4·ws)·π·0.2`; Kiefer `0.18 + sin(f2·0.2·ws)·π·0.04`; Fühler Pitch `0.52 + sin(f2·0.4/0.46·ws)·π·0.15`, Yaw `∓0.3 + sin(f2·0.43/0.49·ws)·π·0.15`; Hinterleib `−0.2 + sin(f2·0.1·ws)·π·0.1`, alle 22 Noppen folgen. Alles an `f2` — nur beim Laufen.

**Varianten.** Keine.

**Klang.** Nur Death `fart` (9 Dateien, StinkBug.java:105–107); Living und Hurt `null`. Lautstärke 1,0.

**Portierungshinweise.** Gerade `LayerDefinition`; `scale()` 0,85 bzw. 0,425 für Babys; Schatten 0,35. Den `f5`-Aliasing-Fehler 1:1 mitnehmen oder als dokumentierte Abweichung korrigieren — Entscheidung, keine Ableitung.
