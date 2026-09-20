# Design: design-entities-06

Batch 06 ist ein Querschnitt durch OreSpawns Bestiarium ohne gemeinsames Thema: zwei
Dinosaurier (T. Rex, Velocity Raptor), zwei Wassertiere (Water Dragon, Whale), drei
Wurm-Stufen, Krabbeltiere (Jumpy Bug, Termite, Crystal Urchin), Pflanze (Triffid),
Kleindrachen (Stinky, Terrible Terror), ein schwebendes Augenpaar (Vortex), ein Werbe-
T-Shirt und drei Projektile bzw. Vanilla-Hüllen. Die gemeinsame Bildsprache ist die von
TheyCallMeDanger insgesamt: flache Techne-Quader ohne Hierarchie (jedes Teil ist ein
Wurzelteil, `max_depth 0` bei allen 15 Modellen, manifest/models), 1 Texturpixel je
1/16 Block, Farbflächen mit Pixelrauschen statt Schattierung, und Animationen, die
Gelenkketten per `cos`/`sin` in `render()` selbst nachrechnen (`rotationPoint` wird
jeden Frame neu gesetzt), weil keine Eltern-Kind-Beziehung existiert. Für den Port
heisst das: LayerDefinition flach bauen und die Kettenrechnung 1:1 in `setupAnim`
übernehmen, oder die Kette in echte Kindteile umbauen und die Zahlen als Pivot nehmen —
beides ist belegbar, die Zahlen stehen unten.

Konventionen: „px" = Modell-Pixel (1/16 Block vor Renderer-Skalierung). Renderer-Skala
ist der dritte Konstruktorparameter der `Render*`-Klasse (manifest `renderer_args[2]`),
angewandt als `glScalef(scale, scale, scale)` in `preRenderScale`; Schatten =
`renderer_args[1] * renderer_args[2]` (manifest `shadow_expr`). Bounding-Boxen sind aus
`reference/jar/models/<Model>.json` ohne Rotation der Teile aufsummiert
(Pivot + Offset + Box), also Näherungen. Angezeigte Farben sind an den PNGs abgelesen
und über die dominanten Farbwerte (Quantisierung auf 32er-Stufen, nur deckende Pixel)
gegengeprüft.

---

### Stinky (`stinky`)

**Aussehen.** Ein gedrungener Mini-Drache: Würfelkörper 8×8×10 px, Kopf 5×5×5 px mit
Schnauze und zwei zweiteiligen Hörnern, vier 3×8×3-px-Stummelbeine, vierteiliger Schwanz,
zwei flache 18×0×10-px-Flügel, die um 23° nach oben angewinkelt sitzen
(`ModelStinky.json`: Lwing/Rwing pivot y 12.6, rot z ±23). Silhouette: Kugel mit Flügeln,
Kopf tief vorn. Die Standardhaut (`stinkytexture1.png`, Skin 0) ist magma-orange mit
dunkelrotem Rand und gelben, gezackten Flügeln (dominant (128,32,0) 23,7 %, (32,0,0)
15,3 %, (224,192,0) 6,6 %) — passend zum Nether-Spawn und zum Blaze-Powder-Drop.
Wiki (01-mobs.md #78): „morphs randomly into 19 colours".

**Größe.** Hitbox 0,75 × 0,75 (manifest; `Stinky.java:43`). Renderer-Skala 1,0, Schatten
0,75 (manifest `renderer_args ["model", 0.75, 1.0]`). Modell unrotiert 40 × 19,5 × 31 px
→ ≈ 2,5 × 1,2 × 1,9 Blöcke mit ausgestreckten Flügeln; Rumpf allein 8 px = 0,5 Block.
Kind: `isChild()` beeinflusst nur die Tonhöhe (`Stinky.java:251`), keine Skalierung.

**Modell.** `ModelStinky`, 20 Teile / 20 Boxen, Textur 128×64, Konstruktorarg 0,65 =
`wingspeed` (`ModelStinky.java:33`, manifest `model_args`). Keine Transparenz, keine
GL-Transformationen (06-models-design.md Zeile 138).

**Animationen** (`ModelStinky.java:145-197`):

| Teil | Formel | Auslöser |
|---|---|---|
| Rwing/Lwing (Z) | `±(cos(f2·2.3·0.65)·π·0.4·f1) ∓ 0.4` | nur bei Bewegung (`f1 > 0.1`), sonst Ruhewinkel ±0,4 rad (146-152) |
| Rleg1/Lleg1/Rleg2/Lleg2 (X) | `cos(f2·2.0·0.65)·π·0.25·f1`, diagonal gegenphasig | Gehen; bei `getActivity()==2` (Flug) starr: Hinterbeine −1,0, Vorderbeine +1,0 rad (159-172) |
| tail2/tail3/tail4 (Y) | `cos(f2·1.0·0.65)·π·0.2`, tail3 ×1,6, tail4 ×2,6; Positionen per cos/sin (4 px, 3 px) nachgeführt | immer, 0 wenn `isSitting()` (173-183) |
| head, snout, neck, 4 Hörner (Y/X) | Y = `toRadians(f3)` (Hals halb), X = `toRadians(f4)/3` | Kopfblick (184-197) |

`activity` (DataWatcher 21, `Stinky.java:74,96-104`): 1 = am Boden, 2 = fliegend. Auf 2
gesetzt bei Schaden (291), zufällig 1/100·1/20 (515-518), wenn der Besitzer fliegt
(526-528), wenn der Besitzer > 16 Blöcke weg ist (531-535) und bei jedem Angriff
(574-582). Bei 2 wird `motionY *= 0.6` (410-412) und die Boden-KI übersprungen (505-507).

**Varianten.** 19 Häute `stinkytexture1..19.png`, alle 128×64. Auswahl:
`getSkin()` (DataWatcher 22) → `RenderStinky.getEntityTexture` (`RenderStinky.java:60-117`):
Skin i → `texture(i+1)`, Skin 0 und alles andere → `texture1`. Skin wird zufällig
`rand.nextInt(19)` beim ersten Tick (`Stinky.java:393-394`) und danach mit 1/2000 je Tick
neu gewürfelt (386-388), NBT `StinkySkin` (84). Jede Haut steht für den Gegenstand, den
Stinky bei 1/2000 je Tick „hinten" ablegt (`Stinky.java:318-375`):

| Skin → PNG | dominante Farbe (Anteil) | Drop (`Stinky.java`) |
|---|---|---|
| 0 → 1 | orange-rot (128,32,0) 24 %, gelbe Flügel | blaze_powder (321) |
| 1 → 2 | graugrün (128,160,128) 41 % | rotten_flesh (324) |
| 2 → 3 | grün (64,96,64) 35 % | melon_seeds (327) |
| 3 → 4 | schwarz (0,0,0) 47 % mit hellvioletten Sprenkeln | UraniumNugget (330) |
| 4 → 5 | olivgrün (96,128,64) 40 % | wheat (333) |
| 5 → 6 | ziegelbraun (96,32,0) 47 % | brick (336) |
| 6 → 7 | dunkelblau (0,0,128) 57 % + grün | torch (339) |
| 7 → 8 | dunkelgrün (0,96,0) 52 % + lime | emerald (342) |
| 8 → 9 | grau (96,96,96) 39 % + gelb | gold_ingot (345) |
| 9 → 10 | grün (64,128,64) 32 % + beige | leaves (348) |
| 10 → 11 | anthrazit (32,32,32) 53 % | TitaniumNugget (351) |
| 11 → 12 | dunkelblau/violett (32,32,64) 21 % | MyAppleSeed (354) |
| 12 → 13 | grau (96,96,96) 40 % + cyan | diamond (357) |
| 13 → 14 | sandbeige (160,160,128) 56 % | sand (360) |
| 14 → 15 | grau (32..128) | cobblestone (363) |
| 15 → 16 | rosaweiss (224,192,192) 44 %; nur 984 deckende px statt ~2450 — grosse Teile transparent (Skelett-Look) | bone (366) |
| 16 → 17 | hellgrau/weiss (192,192,192) 31 % | string (369) |
| 17 → 18 | grau (96,96,96) 45 % + weiss | MyCherrySeed (372) |
| 18 → 19 | cyan (0,192,224) 19 %, hellblau | MyPeachSeed (375) |

**Klang.** `orespawn:duck_hurt` (hurt), `orespawn:cryo_death` (death), kein Living-Sound
(`Stinky.java:215-224`); `orespawn:fart` (9 Dateien) beim Ablegen (319), Vanilla
`random.burp` beim Kohle-Ausspucken (315, 613). Tonhöhe 1,5 als Kind (251).

**Portierungshinweise.** Reine LayerDefinition. `getTextureLocation` liest den Skin aus
`SynchedEntityData` (Int) und indexiert ein Array der 19 ResourceLocations. Die
Schwanzkette (4 px / 3 px) entweder als Kindteile oder per Nachrechnung in `setupAnim`.
Skin-15-Textur mit Alpha-Löchern braucht `RenderType.entityCutoutNoCull` (Standard bei
`MobRenderer`), kein Translucent-Pass. Flugpose hängt am synchronisierten `activity`.

---

### SunspotUrchin (`sunspot_urchin`)

Kein Modell. `RenderItemUrchin extends RenderSpinner`: ein 1×1-Quad, das immer zur
Kamera zeigt (`glRotatef(180 - playerViewY)`, `-playerViewX`), um `rotationPitch` um Z
gedreht wird und mit `glScalef(0.5)` gezeichnet ist → 0,5 Block Sprite
(`RenderSpinner.java:20-49`). Textur `spinners.png` 256×256, 16×16-px-Kacheln;
Kachelindex `getUrchinIndex()` = 50 (`SunspotUrchin.java:20`) → Spalte 2, Zeile 3: ein
gelber Stern/Sonnenblitz mit dunkelgelben Strahlen (dominant (224,224,0) 49 %). Hitbox:
manifest `size null` (EntityThrowable-Default 0,25). Tracking 64/1/1 (manifest).
Partikel: im Flug `smoke` + `reddust` (`SunspotUrchin.java:99-100`), beim Aufschlag
`smoke` (117). Kein eigener Sound. Textur neu: `assets/orespawn/textures/entity/spinners.png`.
Port: ein `EntityRenderer`, der ein Quad im `RenderType.entityCutout(spinners)` mit UV
`(2/16..3/16, 3/16..4/16)` zeichnet, Billboard per `entityRenderDispatcher.cameraOrientation()`,
Z-Rotation aus `getXRot()`; Index als konstantes Feld der Entity, nicht synchronisiert nötig.

---

### T. Rex (`t_rex`)

**Aussehen.** Klassischer Zweibeiner: Rumpf `Shape1` 10×18×31 px, Hüfte/Schwanzansatz
`Shape2` 10×11×11 px, dann Schwanzstücke bis `TailExtension` (3×3×10 px bei z 59), fünf
2×2×3-px-Rückenzacken `Spine1..5`, zwei je vierteilige Beine (Oberschenkel 3×16×10 px,
Fuss 3×3×8 px), zwei winzige Ärmchen `Shape11`/`Shape17` (2×10×2 px, −30° X, vor der
Brust), Unterkiefer `jaw` 7×1×13 px, Kopfschmuck `Shape18..21` (kleine, um 32-33°
gedrehte Quader oben am Schädel). Textur `trextexture.png` 128×128: dunkelgrün
(0,32,0) 55 % mit schwarzen Querstreifen (0,0,0) 30 %, mittelgrüne Rückenlinie
(0,128,0) 13 %, rotes Maul mit weissen Zahnpixeln, gelbe Augen mit schwarzer Pupille.
Wiki [NW]: Palette-Swap des Allosaurus (01-mobs.md #33) — passt zum geteilten
Hurt-Sound `alo_hurt`.

**Größe.** Hitbox 2,0 × 4,2 (manifest; `TRex.java:27`). Renderer-Skala 1,2, Schatten
1,0·1,2 (manifest). Modell unrotiert 15 × 50 × 105 px → ≈ 1,1 × 3,75 × 7,9 Blöcke
(50 px · 1,2 / 16 = 3,75; 105 px · 1,2 / 16 = 7,9). Länge übersteigt die Hitbox
deutlich — Schwanz und Kopf ragen heraus.

**Modell.** `ModelTRex`, 27 Teile / 27 Boxen, Textur 128×128, Konstruktorarg 0,2 =
`wingspeed` (`ModelTRex.java:42`). Keine Transparenz, kein GL.

**Animationen** (`ModelTRex.java:185-206`):

| Teil | Formel | Auslöser |
|---|---|---|
| rightleg/2/3/4, leftleg/2/3/4 (X) | `cos(f2·1.3·0.2)·π·0.25·f1` ± Ruhewinkel (−0,174 / 0,506 / −0,401 / 0), links gegenphasig | `f1 > 0.1` (langsamer Stampfschritt durch wingspeed 0,2) |
| jaw (X) | Angriff: `0.52 + cos(f2·0.45)·π·0.18` (Beissen); sonst 0,1 | `getAttacking() != 0` (DataWatcher 20, `TRex.java:182-192`) |
| Shape11/Shape17 Ärmchen (X) | `−0.523 + cos(f2·0.1)·π·0.05` | immer (Atmen) |

Kein Kopfblick, kein Schwanzschlag.

**Varianten.** Keine. **Klang.** `orespawn:trex_living` (nur mit 1/4-Chance je Aufruf,
`TRex.java:77-82`), `orespawn:alo_hurt`, `orespawn:trex_death` (84-90).

**Portierungshinweise.** Direkte LayerDefinition. `attacking` als synced Byte/Int.
`scale()`-Override 1,2. Original-Leben 160 (manifest) — unter 1024, kein Virtual-Health
nötig.

---

### Termite (`termite`)

**Aussehen.** Nutzt `ModelAnt` (gemeinsam mit den vier Ameisen): Thorax aus drei Boxen,
zweiteiliges Abdomen, Kopf, zwei Kiefer, sechs zweigliedrige Beine — 20 Teile, 64×32.
Textur `termite.png`: schwarze Segmente (0,0,0) 47 %, weisse und hellgraue Ringe
(224,224,224) 20 % / (160,160,160) 14 %, dunkelrote Mandibeln (128,0,0) 7 % — eine
schwarz-weiss geringelte Ameise. Auswahl über `EntityAnt.getTexture` per `instanceof
Termite` → `texture5 = "termite.png"` (`EntityAnt.java:50-52,148`); Manifest listet
für Termite deshalb keine Textur (leeres `textures`), die Zuordnung steht nur im
Entity-Code.

**Größe.** Hitbox 0,2 × 0,2 (manifest; `Termite.java:29`). Renderer `RenderAnt` mit
Skala 0,35, Schatten 0,15·0,35 (manifest `renderer_args`). Modell 15 × 6 × 16 px
→ ≈ 0,33 × 0,13 × 0,35 Blöcke.

**Modell.** `ModelAnt`, 20/20, 64×32, kein Konstruktorarg, kein GL, keine Transparenz.

**Animationen** (`ModelAnt.java:138-151`): sechs Beine X =
`cos(f2·2.7)·π·0.45·f1` in Tripod-Aufteilung (llegtop1, rlegtop2, rlegtop3 positiv;
rlegtop1, llegtop2, llegtop3 negativ; Unterschenkel folgen dem Oberschenkel starr);
Kiefer `jawsl/jawsr` Y = `±cos(f2·0.4)·π·0.05` (immer). Kein wingspeed.

**Varianten.** Nur die Texturwahl nach Klasse (s. o.). **Klang.** Keine — alle drei
Sound-Getter liefern `null`, `getSoundVolume()` = 0, `playStepSound` leer
(`EntityAnt.java:97-114`).

**Portierungshinweise.** Ein `AntRenderer` für alle fünf Ameisenklassen mit
`getTextureLocation` nach Entity-Typ; Skala und Schattenradius je Typ aus dem Manifest
(Termite 0,35 / 0,15). Das Modell ist trivial. Verhalten (Holzfressen, Vermehrung,
Portal-Interaktion) ist nicht Teil dieses Design-Batches.

---

### Terrible Terror (`terrible_terror`)

**Aussehen.** Kleiner geflügelter Drache in Spielzeuggrösse: Rumpf 2×3×10 px, Hals
2×5×2 px um −119° geknickt, flacher Kopf 4×1×2 px mit Schnauze und Kiefer je 4×1×4 px,
zwei 0-px-dünne Hörner, zwei ebene Flügel 0×11×15 px (rot z ±135°, x −20°), vierteiliger
Schwanz mit flacher Flosse `Tail4` 3×0×2, vier zweigliedrige Minibeine (1×2×1 px).
Textur `terribleterror.png` 119×72 (nicht Zweierpotenz): leuchtend türkisgrün
(0,224,96) 19 %, dunkleres Grün (32,128,0) 14 %, Flügel als gezackte hellgrüne
Blattform mit weissem Innenfeld, roter Fleck auf Kopf/Kehle (Detail im PNG), beige
Unterseite (224,224,160) 8 %, gelbe Augen. Wiki #61: „flying, quick, hard to hit".

**Größe.** Hitbox 1,0 × 0,75 (manifest; `TerribleTerror.java:23`). Renderer-Skala 0,75,
Schatten 0,45·0,75. Modell unrotiert 5 × 16 × 33 px → Rumpf ≈ 0,23 breit, 0,75 hoch,
1,55 lang; Flügel 11 px je Seite, bei 135° Z-Rotation Spannweite näherungsweise
2·11·sin(45°)+2 ≈ 17,6 px ≈ 0,8 Blöcke (Näherung, Rotation nicht exakt gerechnet).

**Modell.** `ModelTerribleTerror`, 21/21, 119×72, kein Konstruktorarg; `wingspeed`
fest 1,0 (`ModelTerribleTerror.java:33-34`). Kein GL, keine Transparenz.

**Animationen** (`ModelTerribleTerror.java:148-197`):

| Teil | Formel | Auslöser |
|---|---|---|
| Wing1/Wing2 (Z) | `∓2.0 ± cos(f2·1.3)·π·0.25` | immer (auch stehend flatternd) |
| Jaw (X) | `|cos(f2·0.3)·π·0.1|` | immer |
| FL11/12, FL21/22, BL11/12, BL21/22 (X) | `cos(f2·1.25)·π·0.35` ± Ruhe (0,349 / −0,296 / −0,349 / 0,174), vorne/hinten gegenphasig | immer (baumelnde Beine, nicht an f1 gekoppelt) |
| Tail1 (X, Y) | `cos(f2·0.71)·π·0.1`, `cos(f2·0.77)·π·0.1` | immer |
| Tail2 (X, Y) | 0,81 / 0,87 · π·0,15; Position 6 px hinter Tail1 nachgeführt | immer |
| Tail3+Tail4 (X, Y) | 0,91 / 0,97 · π·0,2; Position 6 px hinter Tail2 | immer |

Sechs verschiedene Frequenzen ergeben ein nie wiederholendes Schlängeln.

**Varianten.** Keine. **Klang.** `orespawn:terribleterror_living`, `_hit`, `_dead`
(`TerribleTerror.java:50-59`).

**Portierungshinweise.** LayerDefinition mit 119×72 Textursize (erlaubt, 06-models-design
Zeile 183). Wing-Boxen mit Tiefe 0 sind Flächen — im Port `CubeDeformation` 0 und
`RenderType.entityCutoutNoCull`, sonst ist die Rückseite unsichtbar (Vanilla-ModelPart
zeichnet 0-dicke Boxen beidseitig nur ohne Culling). Schwanzkette nachrechnen wie oben.

---

### Triffid (`triffid`)

**Aussehen.** Fleischfressende Riesenpflanze: `base`/`root`/`root43` und 50 Wurzelteile
`r1..r50` als Wurzelgeflecht, 16 Knollenteile `b1..b17`, 23 Kopfteile `h1..h23`,
11 Teile `c1..c11`, vier Blattketten (`l1..l15`, `l16..l30`, `l31..l43`, `l44..l57` mit
`leaf3/leaf32/leaf49` als Blattspitzen — je 13-15 Glieder, 2-6 px) und eine 15-gliedrige
Zunge `t1..t15` (`ModelTriffid.json`, Teilgruppen ausgezählt). Textur
`triffidtexture.png` 532×715, davon werden nur die oberen 186×52 px benutzt (UV-Extent
aus den Boxen); 99,6 % der Datei ist deckendes Weiss (224,224,224) — der Rest sind
kleine Felder: dunkelgrün mit schwarzen Flecken (Blätter), lila-rosa (Blüte/Zunge),
dunkelbraun mit Maserung (Stamm/Wurzeln), ein gelber und ein oranger Strich. Das
Fehlen einer sauberen Kachelung macht das Modell texturseitig anfällig: Weiss ist die
Fallback-Farbe jedes Teils, dessen UV ins Leere zeigt. Wiki #32: „cannot be hit while
closed"; die Blätter schliessen sich um den Kopf.

**Größe.** Hitbox 2,0 × 4,0 (manifest; `Triffid.java:28`). Renderer-Skala 1,0, Schatten
0,3. Modell unrotiert 68,5 × 68,5 × 41,6 px → ≈ 4,3 × 4,3 × 2,6 Blöcke; die
Blattketten sind gedreht, die reale Höhe hängt vom Öffnungszustand ab.

**Modell.** `ModelTriffid`, 178 Teile / 178 Boxen, 532×715, Konstruktorarg 1,0 =
`wingspeed` (`ModelTriffid.java:192`). GL in `render()`: `glPushMatrix; glEnable(2977
= GL_RESCALE_NORMAL); glTranslatef(0,0,0); glRotatef(-90, 0,1,0); glTranslatef(0,0,0)`
um **alle** 178 `part.render`-Aufrufe, dann `glPopMatrix` (`ModelTriffid.java:1234-1238,
1417`). Das ganze Modell ist also um −90° um Y gedreht gebaut. Keine Transparenz.
06-models-design Zeile 166: Platz 4 der schwersten Modelle.

**Animationen** (`ModelTriffid.java:1093-1233`, Helfer 1430-1452):

| Gruppe | Formel | Auslöser |
|---|---|---|
| Blattketten A (l1→l15, Z), C (l31→l43, Z), B (l16→l30, X), D (l44→l57, X) | Wurzelwinkel `∓0.95 ± a` bzw. `∓0.75 ∓ a`; jedes Glied `= Vorgänger + a`, Position per cos/sin um `j` px (2-6) vom Vorgänger (`leafpartA/B/C/D`) | `getOpenClosed()==0` (zu): `a = 0.1225` fest → Blätter einrollen um den Kopf; offen: `a = cos(f2·0.25)·π·0.039` → leichtes Wiegen (1093-1098) |
| Zunge t15→t1 (Z, Zickzack) | `t15 = −a − 0.6`, `t14 = +a − 0.6`, … alternierend; Positionen 6/3 px hinter dem Vorgänger; alle Y = 0 | `getAttacking()!=0`: `a = |cos(f2·0.25)·π·0.5|` — Zunge schnellt heraus und zieht sich zurück; sonst `a = π/2` → komplett gefaltet (1165-1219) |

`openclosed` = DataWatcher 21, `attacking` = DataWatcher 20 (`Triffid.java:43-44,
327-340`); geschlossen nach Schaden (`hurt_timer`, 206-214), Angriff nur offen (248-256).

**Varianten.** Keine. **Klang.** `orespawn:triffid_living`, `_hit`, `_dead`
(`Triffid.java:150-159`).

**Portierungshinweise.** Grösste Hürde des Batches. LayerDefinition mit 178 flachen
Teilen ist machbar (Generator aus JSON), die −90°-Y-Drehung gehört als
`poseStack.mulPose(Axis.YP.rotationDegrees(-90))` in `renderToBuffer` oder als
Rotation eines gemeinsamen Elternteils. Die vier Blattketten und die Zunge lassen sich
sauber als Kindketten bauen (Glied-Pivot = `j` px vom Vorgänger, Winkel additiv) — das
ist genau das, was der Helfer per Hand nachrechnet. Zwei Zustände (`openclosed`,
`attacking`) synchronisieren. Texturgrösse 532×715 ist erlaubt; auf den fehlenden
Kachelinhalt (Weiss) hinweisen, nicht reparieren (1:1). GeckoLib-Kandidat laut
06-models-design Zeile 225, hier nicht geplant.

---

### Jumpy Bug (`jumpy_bug`)

**Aussehen.** Klasse `TrooperBug` (Starship-Troopers-Käfer). Riesiger schwarzer Käfer:
vier je 13-teilige Beine (`legNstart/shoulder/part1/elbow/part2/part3` mit Unterteilen
b/c/d), vier Arme (`arm1/2` dreigliedrig, `arm3/4` eingliedrig), Kopf aus zehn
`headbase`-Quadern mit seitlichen Kämmen (`headleftridge/rightridge`), zweigliedrige
Antennen, zwei Augen auf Sockeln, Ober- und Unterkiefer aus je ~10 Quadern
(`jawbase1..9`, `upperjawbase1..4`, `upperjawend`, `jawleft/right/end`), Innenmund,
sechs Zähne, vier Mundkanten, zweigliedrige Zunge — 134 Teile. Textur `trooperbug.png`
512×256: 90 % Schwarz (0,0,0), Gelenke und Panzerkanten dunkelrot/braun (96,0,0)
3,9 %, (96,32,0) 1,5 %, Augen als rote Kreise mit dunklem Zentrum, Zähne/Zunge blutrot
gestreift. Silhouette: niedriger, langer Panzer mit hoch angewinkelten Spinnenbeinen.

**Größe.** Hitbox 3,0 × 3,5 (manifest; `TrooperBug.java:33`). Renderer-Skala 1,1,
Schatten 0,95·1,1. Modell unrotiert 67 × 69 × 152 px → ≈ 4,6 × 4,7 × 10,5 Blöcke;
die Beine werden zur Laufzeit stark gedreht (s. u.), die reale Spannweite ist kleiner,
die Länge (152 px inkl. gestreckter Beine) grösser als die Hitbox.

**Modell.** `ModelTrooperBug`, 134/134, 512×256, Konstruktorarg 0,22 = `wingspeed`
(`ModelTrooperBug.java:147`). Kein GL, keine Transparenz. 06-models-design Zeile 169:
Platz 7 der schwersten Modelle.

**Animationen** (`ModelTrooperBug.java:831-906`, Beinhelfer 1053-1287). Alle
Idle-Bewegungen haben eine langsame und eine schnelle Variante, geschaltet über
`getAttacking()` (DataWatcher 20, `TrooperBug.java:410-431`):

| Teil | ruhig | angreifend |
|---|---|---|
| antenna1part2/antenna2part2 (Y) | `∓0.78 ∓ cos(f2·0.4·w)·π·0.05` | `cos(f2·1.4·w)·π·0.1` |
| arm3part1*/arm4part1* (Y) | `±cos(f2·0.5·w)·π·0.05` | `cos(f2·2.5·w)·π·0.15` |
| arm1part3*/arm2part3* (X) | `1.56 ± cos(f2·0.3·w)·π·0.05` | `cos(f2·2.6·w)·π·0.2` |
| Kopf- und Oberkieferkämme (Y) | `∓0.25`, `∓0.372` ± `cos(f2·0.1·w)·π·0.02` | `cos(f2·1.0·w)·π·0.1` |
| jawbase1-9, jawleft/right/end (X; 8/9 auch Y) | `0.22 + cos(f2·0.3·w)·π·0.015` (Kauen) | `cos(f2·2.6·w)·π·0.1` |

Beine (891-906): Schwung `sin(f2·2.0·w)·π·0.12·f1`; ein Bein hebt sich
(`upangle = |cos(...)|`) nur in der Phase, in der der nächste Sample grösser ist als
der aktuelle (`nextangle > newangle`) — Schritt mit Anheben nur beim Vorschwingen.
Links vorn/rechts hinten teilen die Phase, rechts vorn/links hinten laufen um π
versetzt. `doXxxLeg(angle, upangle)`: Hüfte Y = ±1,2 + angle für die ganze Kette,
Oberschenkel X = 1,115 (+up), Unterschenkel X = 1,871 (−up), Fuss X = 1,08 (+up),
Kettenpositionen mit 26 px und 32 px Gliedlängen per cos/sin nachgeführt.

**Varianten.** Keine. **Klang.** `orespawn:clatter` (2 Dateien, Living mit 1/4-Chance,
`TrooperBug.java:138-143`, und beim Angriff 418), `orespawn:crunch` (hurt),
`orespawn:emperorscorpion_death` (death), `orespawn:scorpion_attack` (Angriff, 415).

**Portierungshinweise.** LayerDefinition flach möglich (134 Teile, Generator). Die
vier Beine als echte Ketten (Hüfte→Oberschenkel 26 px→Unterschenkel 32 px→Fuss) bauen;
die Helfer liefern die Gelenkwinkel direkt. `attacking` synced. Textur 512×256.
Original-Leben 200, Rüstung 15 (manifest) — beides unter den 1.21.1-Klemmen.

---

### T-Shirt (`t_shirt`)

**Aussehen.** Eine schwebende Werbetafel in T-Shirt-Silhouette: `Shape1` 256×64×1 px
(Ärmelbalken, UV 0,0) und darunter `Shape2` 128×128×1 px (Rumpf, UV 0,64), beide am
Pivot (0, −128, 0). Textur `tshirttexture.png` 320×160 (Modell deklariert 512×256 —
gleiches Seitenverhältnis, UVs normalisieren, 06-models-design Zeile 179; `Shape1`
überschreitet die deklarierte Textur um 2 px, Zeile 182): weisser Grund (224,224,224)
74 %, schwarze Schrift „OreSpawn — www.cafepress.com/OreSpawn", „Support the mod by
buying T-shirts or donating on our site!", „Thank you!" und das schwarze
Tintenfisch-Logo im Kreis (an der PNG abgelesen). Wiki #115: „Advertising mob".

**Größe.** Hitbox 4,0 × 4,0 (manifest; `Tshirt.java:17`). Renderer-Skala 0,33, Schatten
1,0·0,33. Modell 256 × 192 × 1 px · 0,33 → ≈ 5,3 × 4,0 Blöcke, 0,02 dick.

**Modell.** `ModelTshirt`, 2/2, 512×256 deklariert, Konstruktorarg 0,22 = `wingspeed`
(`ModelTshirt.java:15`). Kein GL.

**Animationen** (`ModelTshirt.java:34-38`): beide Flächen `rotateAngleY =
cos(f2·0.05·0.22)·π` — pendelt langsam um ±180° um die Hochachse; Periode
2π/0,011 ≈ 571 Ticks ≈ 28,6 s (abgeleitet aus der Formel).

**Varianten.** Keine. **Klang.** Keine (alle drei Getter `null`, `Tshirt.java:58-68`).

**Portierungshinweise.** Zwei 1-px-dicke Boxen; Rückseite zeigt die gespiegelte Textur.
`RenderType.entityCutoutNoCull`. Leben 1, Speed 0 (manifest) — es bewegt sich nicht.
Die Textur enthält eine reale URL (cafepress); unverändert übernehmen ist 1:1, aber im
Pack-README erwähnen.

---

### UltimateArrow (`ultimate_arrow`)

Kein eigenes Modell: Renderer `RenderArrow` (Vanilla, manifest). Gezeichnet wird der
Vanilla-Pfeil aus `textures/entity/arrow.png`; kein `lang name`, keine Hitbox-Angabe im
Manifest (`size null`, EntityArrow-Default). Partikel: `crit` im Flug (`UltimateArrow.java:272`),
`bubble` unter Wasser (300). Sound: Vanilla `random.bowhit` bei Treffern (192, 200, 236,
261). Port: `ArrowRenderer` mit `getTextureLocation` → Vanilla-Pfeiltextur; alles
andere ist Verhalten (Durchschlag/Schaden), nicht Design.

---

### UltimateFishHook (`ultimate_fish_hook`)

Kein eigenes Modell: Renderer `RenderFish` (Vanilla). Gezeichnet wird der
Vanilla-Schwimmer (Sprite aus `particles.png` in 1.7.10) plus die Angelschnur zum Spieler
über `field_146042_b.fishEntity` (SRG-Name; nach Verwendung das `EntityPlayer.fishEntity`-
Feld, `UltimateFishHook.java:83`). Hitbox 0,25 × 0,25 (manifest; `:58,83`). Sound:
Vanilla `random.splash` beim Biss (304). Port: `FishingHookRenderer` verlangt einen
`getPlayerOwner()`; Textur `textures/entity/fishing_hook.png` (Vanilla 1.21.1).

---

### Crystal Urchin (`crystal_urchin`)

**Aussehen.** Klasse `Urchin`. Ein Bündel dünner Stäbe: Mittelstab `center` 1×30×1 px,
vier innere Oberstäbe `tis1-4` 1×25×1 (±15° X/Z), vier äussere Oberstäbe `tos1-4`
1×20×1 (±30°), acht kurze „Füsse" `if1-4`/`of1-4` 1×8×1 nach unten (±15°/±30°) — alle
am Pivot y 16 (`ModelUrchin.json`). Textur `urchintexture.png` 128×128: Stäbe als
Farbverläufe — zwei türkis (128,224,192) 8,7 %, sechs rot→schwarz, sechs blau→schwarz
(0,0,192) 5,8 %; Füsse dunkelgrau (32,32,32) und braun (64,32,0). Insgesamt Schwarz
25 %. Ergebnis: ein schwarzer Igel mit rot, blau und türkis glimmenden Kristallstacheln.
Wiki #57: „sets innocent critters on fire" — dazu passen die Flammenpartikel.

**Größe.** Hitbox 1,35 × 2,1 (manifest; `Urchin.java:28`). Renderer-Skala 1,25,
Schatten 0,35·1,25. Modell unrotiert 5 × 38 × 5 px → 3,0 Blöcke hoch; Stachelradius
durch Rotation ≈ 25·sin 15° + 2 ≈ 8,5 px bzw. 20·sin 30° + 2 = 12 px → ≈ 0,9 Block
Durchmesser · 1,25 ≈ 1,2 Blöcke (Näherung).

**Modell.** `ModelUrchin`, 17/17, 128×128, Konstruktorarg 1,0 = `wingspeed`
(`ModelUrchin.java:30`). Kein GL, keine Transparenz.

**Animationen** (`ModelUrchin.java:129-193`):

| Teil | Formel | Auslöser |
|---|---|---|
| if1-4 (X), of1/2 (Z), of3/4 (X) | Ruhe ±0,261 / ±0,523; plus `cos(f2·k·w)·π·0.15·f1` mit k = 0,7 / 1,7 / 1,65 / 1,75 / 1,8 | nur `f1 > 0.1` (Füsse krabbeln unregelmässig) |
| center (Y) | Angriff: `f2·0.2 mod 2π` (schnelle Dauerrotation); ruhig `f2·0.02` | `getAttacking()` (DataWatcher 20, `Urchin.java:175-185`) |
| tis1-4, tos1-4 (X, Z) | Ruhe ±0,261 / ±0,523 / ±0,532; Wackeln `cos(f2·k·w)·π·0.06` mit k 0,35-0,8 (Angriff) bzw. `π·0.02` mit k 0,035-0,08 (ruhig) | immer |

Partikel (`Urchin.java:114-122`): mit 1/3 je Tick `flame` auf y+0,75; im Wasser mit
1/5 zusätzlich `smoke`/`largesmoke` auf y+1,75 und y+2,0 und Selbstschaden.

**Varianten.** Keine. **Klang.** `orespawn:kyuubi_living`, `orespawn:glasshit`
(5 Dateien), `orespawn:glassdead` (2) (`Urchin.java:126-135`).

**Portierungshinweise.** LayerDefinition; 1-px-Stäbe brauchen `noCull`, sonst
verschwinden sie schräg. Die Dauerrotation von `center` ist `ageInTicks`-basiert, nicht
`f2`-gepuffert — in `setupAnim` direkt aus `ageInTicks` rechnen. Partikel serverseitig
als `ServerLevel.sendParticles` oder clientseitig im `tick()` — Original spawnt sie
auf beiden Seiten ohne `isRemote`-Prüfung (114).

---

### Velocity Raptor (`velocity_raptor`)

**Aussehen.** Klasse `VelocityRaptor extends EntityCannonFodder` (Battle Mob). Schlanker
Federraptor: Rumpf `Shape1`, Hals, zweiteiliger Kopf, zweiteiliger Schwanz, zwei je
viergliedrige Beine `bl1-4`/`br1-4`, dazu 18 „Federn": vier Kopffedern `hf1-4` (um 14-54°
X gefächert), zwei Schwanzfedern-Paare `tf1-4`, Flankenfedern `lf1/2`, `rf1/2`,
`lff1-3`, `rff1-3`; plus zwei Hut-Boxen `Hat1` 4×1×5 (Krempe) und `Hat2` 3×2×3 (Kopf) auf
dem Schädel (`ModelVelocityRaptor.java:210-219`). Textur `velocityraptor.png` 128×128:
mittleres Grün (32,96,0) 33 % und (0,96,0) 32 %, hellgrüne Federkanten (64,224,0) 10 %,
schwarze Streifen, rote Augen mit weissem Pixel; oben rechts ein volles rotes Feld
(192,0,0) 14,3 % — das ist der Hut. Wiki #81: „doubles your ground speed", Battle Mob.

**Größe.** Hitbox 0,5 × 0,6 (manifest; `VelocityRaptor.java:29`). Renderer-Skala 0,75,
Schatten 0,55·0,75; Kind (`isChild()`) halbe Skala (`RenderVelocityRaptor.java:36-42`).
Modell unrotiert 6 × 20 × 39 px → ≈ 0,3 × 0,94 × 1,8 Blöcke.

**Modell.** `ModelVelocityRaptor`, 34/34, 128×128, Konstruktorarg 1,25 = `wingspeed`
(`ModelVelocityRaptor.java:47`). Kein GL, keine Transparenz.

**Animationen** (`ModelVelocityRaptor.java:228-313`):

| Teil | Formel | Auslöser |
|---|---|---|
| bl1-4 / br1-4 (X) | `cos(f2·1.3·1.25)·π·0.25·f1` (+0,488 an Glied 2, +0,628 an Glied 4), rechts gegenphasig | `f1 > 0.1` |
| hf1-4 (Y) | `±cos(f2·1.25·1.25·hf)·π·0.1·hf` mit `hf = getVHealth()/getMaxHealth()` | immer — Kopffedern wippen schneller und weiter, je gesünder (242-247) |
| lf1/2, lff1-3, rf1/2, rff1-3 (X) | `cos(f2·0.3)·π·0.05` ± Ruhe (0,279 / −0,436 / −0,279 / −0,453 / −1,047) | immer (Atmen) |
| lff1-3 / rff1-3 (Y) | `±cos(f2·1.3·1.25)·π·0.1` | immer |
| tf1-4 (Z) | `±cos(f2·1.4·1.25·hf)·π·0.25·hf` | 0 wenn `isSitting()` — Schwanzwedeln zeigt Gesundheit (266-275) |
| Hat1 / Hat2 | nur gerendert, wenn `get_is_activated() != 0`; Hat2 nur bei `> 1` | Battle-Mob-Status (308-313) |

**Varianten.** Drei Texturen, alle 128×128 mit identischen 1394 deckenden Pixeln; sie
unterscheiden sich nur im Hutfeld: `velocityraptor.png` rot (192,0,0),
`velocityraptor2.png` grün (0,192,32), `velocityraptor3.png` blau (0,0,192).
Auswahl `RenderVelocityRaptor.java:48-61`: nur wenn aktiviert und `getHatColor()==2` →
Textur 2, `==3` → Textur 3, sonst Textur 1. Hutfarbe wird beim Zähmen gesetzt:
Karotte → 1 (`EntityCannonFodder.java:97-98`), Quinoa → 2 (141-142), Kartoffel → 3
(119-120); `is_activated` 1 beim Zähmen, 2 bei zweitem Besitzer (80-94, 102-103).
NBT `HatColor`, `IsActivated` (269-270); DataWatcher 20/21 (46-47).

**Klang.** `orespawn:cryo_hurt`, `orespawn:cryo_death`, kein Living-Sound
(`VelocityRaptor.java:320-333`); Vanilla `random.burp` beim Fressen (203),
`damage.fallbig/fallsmall` (75, 78). Kind-Tonhöhe 1,5 (355).

**Portierungshinweise.** LayerDefinition; Hut als zwei Teile mit `visible`-Flag aus
synced `isActivated`. Der Health-Faktor `hf` in der Animation liest die Entity direkt —
in `setupAnim` `entity.getHealth()/entity.getMaxHealth()` verwenden (`getVHealth` ist
im Original schon die Anzeige-HP). `scale()` 0,75 bzw. 0,375 für Babys.

---

### Vortex (`vortex`)

**Aussehen.** Kein Körper: `ModelVortex` besteht aus einer einzigen Fläche `Shape1`
128×64×0 px (Pivot y 22, Box-Ursprung −64/−64/0), Textur `vortextexture.png` 256×128,
von der nur das linke obere Viertel (128×64) auf die Fläche fällt — genau dort liegt
ein Paar grüner Augen mit schwarzen Pupillen, schwarzen Wimpern und lila
Glitzer-Lidschatten (dominant Schwarz 65 %, Grün (32,128,0) 4,3 %, Violett-Töne
(128,32,160)/(96,0,128) je 3,6 %); der Rest der Fläche ist transparent. Im Spiel: ein
riesiges, körperloses Augenpaar, 8 Blöcke breit, das mit der Blickrichtung mitdreht.
Wiki #26: „She", „those incredible eyes".

**Größe.** Hitbox 2,0 × 4,0 (manifest; `Vortex.java:30`). Renderer-Skala 1,0, Schatten
0,1. Fläche 128 × 64 px → 8 × 4 Blöcke, von y −42 px bis +22 px, d. h. die Augen
schweben 2,6 Blöcke über dem Fusspunkt bis 1,4 Blöcke darunter (unrotiert).

**Modell.** `ModelVortex`, 1/1, 256×128, Konstruktorarg 0,25 = `wingspeed`, unbenutzt
(`ModelVortex.java:13,23-27`). Keine Animation, kein GL.

**Animationen.** Keine im Modell. Partikel: solange ein Ziel gefunden ist, 20
`smoke`-Partikel je Tick clientseitig in einem Kegel bis 3,5² Blöcke um die Position,
aufwärts driftend (`Vortex.java:88-102`) — der eigentliche „Wirbel" ist Rauch.

**Varianten.** Keine. **Klang.** `orespawn:vortexlive` als Living **und** Death, kein
Hurt-Sound (`Vortex.java:56-66`).

**Portierungshinweise.** Eine 0-dicke Box → `entityCutoutNoCull` und Alpha-Test; die
Fläche dreht mit `yBodyRot` (RenderLiving-Standard). Shadow 0,1 fast unsichtbar. Der
Rauchkegel gehört in `tick()` auf der Client-Seite (`level().isClientSide`), gekoppelt an
ein synced „hat Ziel"-Flag, weil `findSomethingToAttack` im Original auf beiden Seiten
läuft.

---

### WaterBall (`water_ball`)

Kein Modell. Wie `sunspot_urchin`: `RenderItemUrchin`/`RenderSpinner`, 0,5-Block-Billboard
aus `spinners.png`, Kachelindex `getWaterBallIndex()` = 49 (`WaterBall.java:18,24,30`) →
Spalte 1, Zeile 3: ein blauer Spritzer/Stern (dominant (0,0,128) 39 %, (32,64,224) 23 %,
ein gelber Pixelrest). Hitbox: manifest `size null` (Throwable-Default 0,25). Tracking
64/1/1. Partikel: im Flug je Tick `bubble` + `splash` (`WaterBall.java:68-69`), beim
Aufschlag `splash` (86); Sound Vanilla `random.splash` (71). Port wie SunspotUrchin,
UV `(1/16..2/16, 3/16..4/16)`.

---

### Water Dragon (`water_dragon`)

**Aussehen.** Langgestreckter Seedrache mit vier Paddelbeinen: Kopf `Head`, `nose`,
`jaw`, Kopfflosse `headfin`, zwei „Ohren" `leftear/rightear`, Hals `neck1-4`, Rumpf
`body1-4`, Rückenflosse `Bodyfin`, Halsflosse `neackfin`, Schwanz `tail1`,
`tailmiddle`, `tailtop`, `tailbottom` (Schwanzflosse), Beine `Leg1/2/7/8` — 24 Teile.
Textur `waterdragon.png` 128×128: blaue Schuppenflächen mit Pixelrauschen in drei
Blautönen (32,32,224)/(32,64,224)/(32,96,224) je ~15 %, alle Flossen, Ohren und
Schwanzflosse tiefrot mit schwarzen Adern (128,0,0) 11 %, (160,0,0) 7 %, ein
leuchtend roter Fleck auf dem Kopf, orange Augen, schwarze Nasenlöcher. Wiki #27:
„waterballs and fireballs".

**Größe.** Hitbox 1,25 × 1,9 (manifest; `WaterDragon.java:43`). Renderer-Skala 1,1,
Schatten 0,85·1,1; Kind halbe Skala (`RenderWaterDragon.java:35-39`). Modell unrotiert
25 × 43 × 53 px → ≈ 1,7 × 3,0 × 3,6 Blöcke (Hals ist im Modell nach oben gebogen, daher
die Höhe).

**Modell.** `ModelWaterDragon`, 24/24, 128×128, Konstruktorarg 0,5 = `wingspeed`
(`ModelWaterDragon.java:37`). Kein GL, keine Transparenz.

**Animationen** (`ModelWaterDragon.java:170-242`):

| Teil | Formel | Auslöser |
|---|---|---|
| body3, body4, tail1, tailmiddle (+tailtop/tailbottom) (Y) | `cos(f2·1.3·0.5 − n·π/4)·π·0.4·f1`, n = 0..3; Gliedpositionen 7 / 5 / 3 px per cos/sin nachgeführt | `f1` — S-förmiger Schwimmschlag, Amplitude wächst mit Tempo (176-191) |
| Leg8/Leg2/Leg7/Leg1 (Y) | `±0.58 ± cos(f2·1.3·0.5)·π·0.2·f1` | `f1 > 0.1` Paddeln (170-175, 192-195) |
| leftear/rightear (Y) | `±0.62 ± cos(f2·0.8·0.5)·π·0.1` + Kopfgier | immer |
| Bodyfin (Z), neackfin (Y), headfin (Y) | `cos(f2·0.7·0.5)·π·0.02`, `cos(f2·0.6·0.5)·π·0.1`, `cos(f2·0.5·0.5)·π·0.05` | 0 wenn `isSitting()` (199-213) |
| jaw (X) | `getAttacking()==1`: `cos(f2·1.2·0.5)·π·0.25` (Beissen); `==2`: 0,45 offen (Fernschuss); sonst −0,25 | DataWatcher 20 (`WaterDragon.java:629-672`) |
| Head, nose, jaw, headfin, Ohren (Y) | `toRadians(f3)·0.75`; nose 8 px, jaw 7 px, headfin 3 px, Ohren √13 / √20 px um den Kopfpivot rotiert | Kopfgier (224-242) |

`attacking` 2 wird beim Abschuss gesetzt (653) mit Vanilla `random.bow` (657, 667);
`splash` beim Auftauchen (644).

**Varianten.** Keine. **Klang.** `orespawn:waterdragon_hurt` (3 Dateien),
`orespawn:waterdragon_death`, kein Living-Sound (`WaterDragon.java:234-243`).

**Portierungshinweise.** LayerDefinition; Kopfgruppe (Head, nose, jaw, headfin, Ohren)
als Kinder eines Kopfteils bauen, dann entfällt die Trig-Nachführung; Rumpf-/Schwanz-
kette (7/5/3 px) als Kette. `attacking` synced (0/1/2). `scale()` 1,1 / 0,55. Leben 150,
Rüstung 8 (manifest).

---

### Whale (`whale`)

**Aussehen.** Bartenwal aus 14 Quadern: `body` 20×12×52 px, `belly`, `back`, `head`
16×8×22 px, `jaw`, `backfin`, zweigliedriger Schwanz `tail1/tail2` mit zwei flachen
Fluken `tailfin1/2` 17×2×11 px, zweigliedrige Brustflossen `lfin1/2`, `rfin1/2`
(22×2×8 px). Textur `whaletexture.png` 256×256: dunkles Graublau mit vertikalem
Pixelrauschen (32,64,96) 47 %, hellere blaugraue Flanken/Bauch (96,96,128) 34 %,
tiefblaues Rückenfeld (32,32,96) 18 %, winzige schwarze Augenpunkte. Silhouette:
lang, flach, Fluken horizontal.

**Größe.** Hitbox 1,5 × 2,5 (manifest; `Whale.java:33`). Renderer-Skala 1,0, Schatten
0,1; Kind halbe Skala (`RenderWhale.java:35-39`). Modell unrotiert 68 × 16 × 107 px →
≈ 4,25 (mit Flossen) × 1,0 × 6,7 Blöcke; Rumpf 20 px = 1,25 breit.

**Modell.** `ModelWhale`, 14/14, 256×256, kein Konstruktorarg, kein `wingspeed` im
Code. Kein GL, keine Transparenz.

**Animationen** (`ModelWhale.java:102-139`):

| Teil | Formel | Auslöser |
|---|---|---|
| lfin2/rfin2 (Z), lfin1/rfin1 halb | `±0.436 ± a`; a = `cos(f2·0.3)·π·0.2·f1` (schwimmend) bzw. `cos(f2·0.08)·π·0.05` (ruhend) | `f1 > 0.1` |
| jaw (X) | `0.087 + cos(f2·0.03)·π·0.02` | immer (kaum sichtbar) |
| tail1 (X ×0,5), tail2 (X ×1,25), tailfin1/2 (X ×2,25) | b = `cos(f2·0.4)·π·0.16·f1` bzw. `cos(f2·0.05)·π·0.03`; Positionen 14 px / 8 px nachgeführt | vertikaler Flukenschlag wie beim echten Wal |

Partikel (`Whale.java:60-88`): alle 250-500 Ticks ein „Blas" von 25-50 Ticks, je Tick
10 `bubble` + 10 `splash` in einem Kegel bis 0,75² um y+1, aufwärts bis 2 Blöcke/Tick.
`splash` beim Auftauchen (267).

**Varianten.** Keine. **Klang.** Living = Vanilla `splash`, `orespawn:little_splat`
(hurt), `orespawn:big_splat` (death) (`Whale.java:106-116`).

**Portierungshinweise.** Direkte LayerDefinition; Schwanz als Kette (14 px, 8 px).
Blas-Partikel client-seitig im `tick()` an ein synced Spray-Flag. `scale()` 1,0 / 0,5.

---

### Large Worm (`large_worm`)

**Aussehen.** Klasse `WormLarge`. Dicker Regenwurm mit aufgerichtetem Vorderteil: fünf
Halsglieder `neck1-5`, fünf Kopfteile `head1-5`, Schwanz `tail1-4` + `tailtip`, acht
Zähne `tooth1-8` ringförmig um die Mundöffnung — 23 Teile. Textur `wormlargetexture.png`
256×256: zwei Brauntöne mit Pixelrauschen (128,64,0) 59 % und (96,64,0) 37 %, ein
dunkelroter Fleck (Auge/Narbe), Zähne rosaweiss (224,192,192) mit hellgrauem Schaft.
Wiki #36 „Boss Worm".

**Größe.** Hitbox 1,55 × 2,5 (manifest; `WormLarge.java:23`). Renderer-Skala 1,0,
Schatten 0,9. Modell unrotiert 22 × 39 × 81 px → ≈ 1,4 × 2,4 × 5,1 Blöcke; der Hals
steht um −0,698 rad (≈ 40°) angehoben (`ModelWormLarge.java:158`), der Kopf sitzt 32 px
vor dem Halsansatz.

**Modell.** `ModelWormLarge`, 23/23, 256×256, kein Konstruktorarg. Kein GL.

**Animationen** (`ModelWormLarge.java:157-303`, nur `f2`-getrieben, also auch im
Stand):

| Teil | Formel |
|---|---|
| neck1-5 (X, Y) | X = `cos(f2·0.25)·π·0.08 − 0.698`, Y = `cos(f2·0.15)·π·0.07`, alle fünf identisch |
| head1-5 | Position 32 px entlang des Halswinkels; X = `cos(f2·0.35)·π·0.15`, Y = `cos(f2·0.45)·π·0.05` |
| tooth1-8 | Ring 19 px vor dem Kopf, Radius 9 px (Kreuz) bzw. 6 px (Diagonalen); öffnen/schliessen um `cos(f2·0.57)·π·0.35` — radial um Kopf-X/Y |
| tailtip (X, Y) | `cos(f2·0.63)·π·0.15 + 0.35`, Y phasenversetzt um π/2 |

**Varianten.** Keine. **Klang.** `orespawn:big_splat` (hurt), `orespawn:alo_death`
(death), kein Living-Sound (`WormLarge.java:57-67`).

**Portierungshinweise.** Kopf + acht Zähne als Kindgruppe eines Halsendteils (Pivot
32 px), Zähne mit Pivot 19 px davor — die radiale Öffnung ist dann je Zahn eine
X- oder Y-Rotation. Rüstung 14, Leben 90 (manifest).

---

### Medium Worm (`medium_worm`)

**Aussehen.** Klasse `WormMedium`. Drei 4×12×4-px-Glieder `tail`/`body`/`head` (+
`head2`), vier Zähne `tooth1-4` — 8 Teile. Textur `wormmediumtexture.png` 64×32:
gelbbraun (160,96,0) 55 % und dunkler Braun (128,64,0) 35 %, ein rosa Pixel (Auge),
Zähne hellgrau/weiss. Ein aufrecht stehender brauner Wurm mit vier Beisszangen.

**Größe.** Hitbox 0,5 × 2,0 (manifest; `WormMedium.java:22`). Renderer-Skala 1,0,
Schatten 0,25. Modell unrotiert 4 × 39 × 4 px → ≈ 0,25 × 2,4 Blöcke.

**Modell.** `ModelWormMedium`, 8/8, 64×32, kein Konstruktorarg. Kein GL.

**Animationen** (`ModelWormMedium.java:66-172`): `tail` X = `cos(f2·0.45)·π·0.1`,
Z = `cos(f2·0.25)·π·0.08`; `body` sitzt 12 px darüber (per sin/cos nachgeführt), X =
`cos(f2·0.35)·π·0.1`, Z = `cos(f2·0.15)·π·0.07`; `head`+`head2` 12 px darüber, X =
`0.62 + cos(f2·0.55)·π·0.15` (nach vorn geneigt), Z = `cos(f2·0.25)·π·0.05`; Zähne
folgen dem Kopf 12 px weiter und öffnen: tooth1/2 X = `∓(0.4 + cos(f2·0.55)·π·0.15)`,
tooth3/4 Z analog (165-172). Alles nur `f2`-getrieben.

**Varianten.** Keine. **Klang.** `orespawn:little_splat` (hurt), `orespawn:big_splat`
(death) (`WormMedium.java:55-60`).

**Portierungshinweise.** Dreigliedrige Kette (12 px), Kopf mit vier Zahn-Kindern.
Trivial nach dem Kettenumbau.

---

### Small Worm (`small_worm`)

**Aussehen.** Klasse `WormSmall`. Drei 1×5×1-px-Glieder `head`/`body`/`tail` — ein
rosa Faden. Textur `wormsmalltexture.png` 64×32 mit nur 66 deckenden Pixeln: rosa
(224,128,160) 44 %, (192,128,128) 24 %, ein dunkelroter Punkt (128,0,32) 12 % als Auge.

**Größe.** Hitbox 0,25 × 1,0 (manifest; `WormSmall.java:22`). Renderer-Skala 1,0,
Schatten 0,1. Modell 1 × 15 × 1 px → ≈ 0,06 × 0,94 Blöcke — im Spiel ein wackelnder
Strich, kaum zu treffen.

**Modell.** `ModelWormSmall`, 3/3, 64×32, kein Konstruktorarg. Kein GL.

**Animationen** (`ModelWormSmall.java:36-59`): gleiche Kette wie Medium mit 5-px-Gliedern:
`tail` X = `cos(f2·0.55)·π·0.15`, Z = `cos(f2·0.35)·π·0.1`; `body` X =
`cos(f2·0.45)·π·0.15`, Z = `cos(f2·0.25)·π·0.1`; `head` X = `0.62 + cos(f2·0.65)·π·0.15`,
Z = `cos(f2·0.3)·π·0.05`. Immer aktiv.

**Varianten.** Keine. **Klang.** Nur `orespawn:little_splat` (hurt); Living und Death
`null` (`WormSmall.java:51-60`).

**Portierungshinweise.** Dreiteilige Kette (5 px). 1-px-Boxen → `noCull`.

---

## Querverweise und offene Punkte

- **Vanilla-Sounds** in diesem Batch (`random.burp`, `random.bow`, `random.splash`,
  `splash`, `random.bowhit`, `damage.fallbig/fallsmall`) haben 1.21.1-Entsprechungen
  (`entity.player.burp`, `entity.arrow.shoot`, `entity.generic.splash`,
  `entity.arrow.hit`, `entity.player.big_fall/small_fall`); die Zuordnung ist Port-
  Entscheidung, nicht Manifest-Inhalt.
- **Keine Transparenz-Pässe** in diesem Batch (06-models-design.md Zeilen 192-209 nennt
  keines dieser Modelle). Einziger GL-Sonderfall: Triffids −90°-Y-Drehung.
- **Renderer-Skalen mit Kind-Halbierung**: VelocityRaptor, WaterDragon, Whale
  (`isChild()`-Zweig in `preRenderScale`, jeweils `Render*.java:35-42`) — die in
  06-models-design Zeile 214 als „Bedingung nicht extrahiert" geführten Fälle sind hier
  am Quelltext belegt.
- **offen:** exakte gerenderte Ausdehnung von Triffid, Jumpy Bug und Terrible Terror
  hängt von Laufzeit-Rotationen ab; die Zahlen oben sind unrotierte Box-Summen.
- **offen:** `field_146042_b` in `UltimateFishHook` — SRG-Name, Bedeutung aus der
  Verwendung (`.fishEntity = this`) als Angler-Spieler gelesen; MCP-Mapping nicht
  gegengeprüft.
