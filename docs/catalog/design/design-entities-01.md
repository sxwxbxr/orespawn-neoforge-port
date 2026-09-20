# Design: design-entities-01

Stand: 10.09.2026. Quelle in dieser Reihenfolge: `docs/catalog/manifest.json` (Kürzel: manifest), dekompilierte Quelle 20.2 unter `reference/src-20.2/src/main/java/danger/orespawn/` (Zeilenangaben `Klasse.java:ZEILE`), Geometrie/Animation unter `reference/jar/models|anim/<Model>.json`, Texturen unter `reference/jar/extracted/assets/orespawn/`, zuletzt `docs/research/01-mobs.md` (Kürzel: wiki) und `06-models-design.md`.

**Bildsprache dieses Batches.** Das Batch versammelt die Königsfamilie und die drei Riesen-Bosse plus eine Handvoll Alltagsmobs. Die Königsfamilie (King, Queen, Prince in drei Stufen, Princess) teilt eine Formensprache: dreiköpfige, flügellose-arme Drachen mit sechs Flügeln, Rückenkamm und langem Peitschenschwanz, gebaut aus Marmor-Texturen (weiß für den König und seine Söhne, eisblau/schwarz für Königin und Prinzessin) mit Goldkanten an Stacheln, Kamm und Flügelrändern; die Flügelmembranen werden als halbtransparente Schicht (Alpha 0,55) über den opaken Körper gezeichnet. Mobzilla ist schwarzer, rissiger Stein mit violett leuchtenden Rückenplatten; der Kraken ist ein Nebel aus Nachtblau und Magenta mit Sternen, ein Stück Weltraum mit Tentakeln. Die kleinen Mobs sind bewusst flach und plakativ gefärbt (Alosaurus rot-schwarz, Baryonyx dunkelgrün, Attack Squid petrolblau, Biene Rinde plus Warnstreifen), damit sie auf Distanz lesbar bleiben. Projektile und Kopf-Hilfsentities haben kein Modell: sie zeichnen ein Sprite aus `spinners.png` oder gar nichts.

Größenableitung, wenn nicht anders angegeben: Ausdehnung der Boxen in Modell-Pixeln aus `models/<Model>.json` in Ruhepose (Konstruktor-Rotationen angewendet, keine Animation), mal Renderer-`scale`, geteilt durch 16. Das ist eine Näherung, weil `render()` viele Pivots jedes Bild neu setzt. `f2` in den Animationsformeln ist `ageInTicks`, `f` der Laufzyklus, `f1` die Laufgeschwindigkeit (0..1), `f3` Kopf-Gier, `f4` Kopf-Neigung (06-models-design.md, Tabelle „animation inputs"). **`wingspeed` ist das Konstruktor-Argument des Modells**, nicht 1.0: jeder Konstruktor setzt erst `this.wingspeed = 1.0f` und direkt danach `this.wingspeed = f1` (ModelTheKing.java:132-133, ModelGodzilla.java:83-84, ModelBee.java:35-36, gleiches Muster in allen 18 Modellen dieses Batches). Alle `f2 * k * this.wingspeed`-Frequenzen unten sind also mit dem Wert aus `model_args` (manifest) zu multiplizieren:

| wingspeed | Modelle |
|---|---|
| 0.65 | ModelTheKing, ModelTheQueen, ModelThePrince, ModelThePrinceTeen, ModelThePrinceAdult, ModelThePrincess |
| 1.0 | ModelPurplePower, ModelKraken, ModelAntRobot, ModelAttackSquid |
| 0.2 | ModelGodzilla (Animation läuft auf einem Fünftel der notierten Frequenz) |
| 0.22 | ModelAlien, ModelAlosaurus |
| 0.25 / 0.3 / 0.4 / 0.5 | ModelBaryonyx / ModelBasilisk / ModelBandP / ModelBeaver |
| 2.0 | ModelBee (doppelte Frequenz) |

Farbangaben in den Einträgen sind Mittelwerte der nicht-transparenten Pixel im UV-Netz des jeweiligen Parts (Box-Netz `u..u+2(l+w)`, `v..v+l+h` aus `models/<Model>.json`, gesampelt aus der PNG; eigene Rechnung, PIL). Sie belegen, welcher Part welche Kachel benutzt, nicht das gerenderte Bild.

Allen Lebewesen gemeinsam: `RenderLiving`-Unterklasse, Schattengröße `par2 * par3` (z. B. RenderTheKing.java:16), Skalierung in `preRenderScale` per `glScalef` (in 1.21.1: `LivingEntityRenderer.scale()` überschreiben). Ausnahmen stehen beim Eintrag.

---

### The King (`the_king`)

**Aussehen.** Dreiköpfiger Drache ohne Arme, zwei Beine mit je drei Krallen (`LLClaw1/2`, `LCClaw1/2`, `LRClaw1/2` plus `LClawRear`), drei Hälse aus je vier Segmenten (`NeckL1-4`, `NeckC1-4`, `NeckR1-4`), jeder Kopf aus drei Kopfboxen, drei Kieferboxen, vier Zähnen, zwei Augen, Mähne und zwei Nasenstacheln. Sechs Flügelfinger je Seite in fünf Paaren (`Lwing1..10`, `Rwing1..10`), Rückenkamm `Ridge1-6`, Schwanz aus sieben Segmenten plus `TailTip`, `TailTip2`, `TailSpike` (Hammer-Schwanz, wiki [FW] bestätigt). Textur `TheKingtexture.png` 2048x2048 (manifest), Hintergrund transparent (58144 von 65536 Stichproben Alpha 0). Sampling je Part: Körper, Hälse, Köpfe, Beine, Schwanzsegmente weißer Marmor (`Body1`, `Chest`, `Tail1-7`, `NeckL4` ≈ #e9e9e9); Füße, Krallen, Kamm, Nasenstacheln, Rückenplatten `Back1/2` und `TailSpike` Gold (`LFoot` #f3b215, `LCClaw1` #f3c715, `Ridge1` #f3c715, `CLNoseSpike` #f3c715); Mähne Amber (`LHeadMane` #e18c13); Flügelknochen innen Dunkelorange (`Lwing1` #c96a0d), außen Amber (`Lwing3` #e18c13); Flügelmembranen fast weiß mit Cremestich (`Lwing2` #fcfaee, `Lwing4` #fdfdf9 — die gewellten Formen rechts im Atlas, goldener Saum); Kiefer rosa-rot (`LJaw1` #dfbcbc, `CJaw2` #d4a0a0), Zähne weiß. **Augen je Kopf verschieden, per UV belegt: linker Kopf rot (`LLEye`/`LREye` #fb1010), mittlerer schwarz (`CLEye`/`CREye` #010208), rechter blau (`RLEye`/`RREye` #0f29ef)**; wiki [FW] „eyes blue/red/black" stimmt, die Zuordnung zu den Köpfen steht nur hier.

**Größe.** Hitbox 22,0 x 24,0 bei `PlayNicely == 0`, sonst 5,5 x 6,0 (TheKing.java:64/67). Renderer-Args `(model, 1.9, 2.1)` (manifest): `scale = 2.1`, Schatten `1.9 * 2.1 = 3.99`. Bei `getPlayNicely() != 0` wird `scale / 4` gezeichnet (RenderTheKing.java:35-39). Modellausdehnung in Ruhepose: y -168..30 px, z -278..421 px, x -396..396 px (Geometrie-JSON, eigene Rechnung) -> ca. **26 Blöcke hoch, 92 Blöcke lang (Schnauze bis Schwanzspitze), 104 Blöcke Spannweite** bei scale 2.1; Kopfoberkante ca. 25 Blöcke über Boden, passt zur Hitbox-Höhe 24. Bei PlayNicely ein Viertel davon (6,5 hoch).

**Modell.** `ModelTheKing`, 119 Parts / 119 Boxen, flach (keine Kinder), Textur 2048x2048, größte Boxkante 188 px (manifest, models-JSON). Konstruktor-Arg 0.65 = `wingspeed` (manifest `model_args`, ModelTheKing.java:133). Translucent-Gruppe: `Lwing2/4/6/8/10`, `Rwing2/4/6/8/10` (die Membranen) zwischen `glEnable(GL_BLEND)`, `glBlendFunc(770,771)`, `glColor4f(0.75,0.75,0.75,0.55)` und `glDisable` (ModelTheKing.java:1189-1205). Keine Ganzmodell-Transformation.

**Animationen** (ModelTheKing.java, `render()` ab Zeile 733):
| Was | Formel / Treiber | Zeilen |
|---|---|---|
| Flügel Z-Schlag, drei Stufen (`*5/3`, `*7/3` nach außen), Pivots der äußeren Finger folgen per sin/cos (84 px, 184 px) | Ruhe `cos(f2*0.35)*pi*0.15`, Angriff `cos(f2*0.75)*pi*0.21`; zweite Gruppe 0.25, dritte `cos(f2*0.6)*pi*0.45` | 750-754, 844-848, 867-870 |
| Beine (Thigh/UpperLeg/LowerLeg/Foot X) und Krallen X mit Pivot-Nachführung | Laufzyklus (animiert laut anim-JSON, Treiber `f`/`f1`) | render, Teil „RClawRear … RLowerLeg" |
| Schwanz Y-Welle über 7 Segmente, Phasenversatz `pi4` je Segment, Pivots nachgeführt | `tailspeed 0.26 / tailamp 0.08`, Angriff `0.56 / 0.19` | 968-996 |
| Drei Köpfe: Gieren/Nicken/Kiefer per Sinus, je Kopf andere Frequenz; `moveLeftHead/moveCenterHead/moveRightHead(lr, ud, jaw)` setzen alle 19 Kopf-Parts (Hals 4 Segmente + Kopf + Kiefer + Zähne + Augen + Mähne + Nasenstacheln) inklusive Pivots | Ruhe Amplitude 0.08/0.1/0.04, Angriff 0.25/0.25/0.12; äußere Köpfe werden am mittleren geklemmt (`Lheadlr > Cheadlr`) | 1037-1074 |
Treiber: `TheKing.getAttacking()` (DataWatcher 20, TheKing.java:118) schaltet zwischen Ruhe- und Angriffsamplitude.

**Varianten.** Eine Textur. `PlayNicely` (DataWatcher 21, TheKing.java:92/96) ändert nur Skalierung und Hitbox.

**Klang** (manifest `referenced_by` + Quelle): `king_living` (TheKing.java:131), `king_hit` (135), `trex_death` (139), `mothrawings` alle paar Ticks im Flug bei Lautstärke 1.75 / Pitch 0.75 (220); Vanilla `random.fuse`, `random.bow`, `random.explode` beim Schießen (710-774, 1116). Lautstärke 1.35 (TheKing.java:124).

**Portierungshinweise.** Zwei Render-Passes: opak (`entityCutoutNoCull`) für 109 Parts, danach `entityTranslucent` mit Farbe ARGB(0.55, 0.75, 0.75, 0.75) für die zehn Membran-Parts; Reihenfolge wie im Original (Membranen zuletzt). `scale()`-Override mit `/4` bei PlayNicely aus SynchedEntityData. Die drei Kopfhelfer werden zu einer Methode, die `ModelPart.x/y/z` und `xRot/yRot` setzt; Pivots jedes Bild überschreiben, nicht akkumulieren. Health 7000 (manifest) liegt über der 1024-Klemme -> virtuelle Gesundheit; Rüstung 25/22/23/24/21 (manifest, mehrere Werte) unter 30, unkritisch. Modellgröße: `LayerDefinition` verträgt 2048x2048 und Pivots von 300+ px ohne Sonderfall. GeckoLib-Kandidat laut 06-models-design (119 Parts, 11 kB Animation), bleibt aber ein ModelPart-Port nach Vorgabe.

---

### KingHead (`king_head`)

Kein Modell, wird nicht gezeichnet: `RenderKingHead` überschreibt `doRender` leer und `getEntityTexture` liefert `null` (RenderKingHead.java, Klasse ab Zeile 8; Dump-Zeilen 8-18). Unsichtbarer Kopf-Trefferkasten des Königs: Hitbox 19,9 x 10,0 (KingHead.java:23), `noClip = true` (24), Health = `TheKing_stats.health` (manifest, 7000), kein Angriff. Bewegt sich mit 1.33 (manifest) hinter dem Kopf her. Port: `EntityRenderer` mit leerem `render()` oder `NoopRenderer`; kein Schatten, kein Name-Tag; Hitbox-Bounds wie oben, `isPickable`/`canBeCollidedWith` beibehalten, weil sie das Treffen der Köpfe ermöglicht.

---

### The Queen (`the_queen`)

**Aussehen.** Gleiches Skelett wie der König, aber ohne `Chest`; stattdessen liegen `NeckBone1-3`, `Rib1-6` und drei `PowerCube1-3` frei — die Königin zeigt Halswirbel und Rippen und hält bis zu drei rotierende Energie-Würfel vor der Brust. Zwei Texturen 2048x2048 (manifest):
- `TheQueentexture.png` (wütend, Sampling je Part): Körper, Hälse, Köpfe, Beine, Schwanz, `NeckBone1-3`, `Rib1-6` **tiefschwarz** (`Body1` #030003, `LHead1` #170005); Füße, Rückenplatten und innere Flügelknochen dunkelrot (`LFoot`/`Back1`/`Lwing1` #910505), äußere Flügelknochen und `Ridge3` blutrot (#b80303/#ae0404), Mähne und Nasenstacheln hellrot (`LHeadMane` #e00505, `CLNoseSpike` #f80404), `TailSpike` #bc0606; Kiefer sehr dunkelrot (#2c0409); **alle sechs Augen magenta** (#dd04de); Flügelmembranen dunkelgrauer Marmor (`Lwing2` #282828); Power-Würfel violett in drei Stufen (`PowerCube1` #a606b0, `PowerCube2` #be15c9, `PowerCube3` #de22ea).
- `TheQueentexture2.png` (zufrieden): Körper eisblau (`Body1` #99cafc, `LThigh` #93c6fc); Füße, Rückenplatten, Kamm, Mähne, Flügelknochen und Schwanzhammer gelbgold (`LFoot` #efcc0b, `Ridge1`/`TailSpike` #f6d72c); **Nasenstacheln blau** (`CLNoseSpike` #0101f3); Kiefer lavendelgrau (#a4a4c8); **alle Augen grün** (#07bd14); Flügelmembranen kobaltblau (`Lwing2` #031da6, `Lwing4` #030f9d) mit grünen Seitenflächen (Bild); Power-Würfel grün in drei Stufen (#027415, #17a42e, #47dc5f). wiki [FW] „light blue with a green orb (neutral) and black with a purple orb (aggressive)" stimmt.
Beide Texturen haben denselben transparenten Hintergrund (je 57792 von 65536 Stichproben Alpha 0); der weiße bzw. schwarze Grund in der Bildvorschau war nur die Darstellung.

**Größe.** Hitbox 22,0 x 24,0 / 5,5 x 6,0 nach `PlayNicely` (TheQueen.java:66/69). Renderer `(model, 1.9, 2.0)` (manifest): `scale = 2.0`, Schatten 3.8; `/4` bei `getPlayNicely() != 0` (RenderTheQueen.java:36-40). Ruhepose y -168..32, z -278..450, x ±396 px -> ca. **25 Blöcke hoch, 91 lang, 99 Spannweite** (eigene Rechnung).

**Modell.** `ModelTheQueen`, 130 Parts / 130 Boxen, Textur 2048x2048, größte Kante 188 px (manifest). Translucent-Gruppe (ModelTheQueen.java:1312-1336): Membranen `Lwing2/4/6/8/10`, `Rwing2/4/6/8/10` plus `PowerCube1`; `PowerCube2` nur bei `getPower() > 350`, `PowerCube3` bei `> 650` (1329, 1332). Die Würfel werden **vollhell** gezeichnet: vor `PowerCube1.render` setzt `OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, 240, 240)` das Lightmap-Maximum (1327), die Membranen davor nicht. Danach `glColor4f(1,1,1,1)`, Blend aus, und **erst dann die sechs Augen** (1335-1342) — die Augen werden opak nach den Membranen gezeichnet, damit sie nicht von der Flügelschicht überdeckt werden.

**Animationen** (ModelTheQueen.java ab 800): wie König, andere Frequenzen: Flügel Ruhe `cos(f2*0.35)*pi*0.15`, Angriff `cos(f2*0.85)*pi*0.26` (817-821), dritte Stufe Angriff `cos(f2*0.7)*pi*0.45` (935); Schwanzwelle 1044-1063; Kopfsinus wie König mit L/R-Frequenzen 0.33/0.22 und C 0.27/0.18 (1104-1130). Neu: `PowerCube1-3` drehen frei um alle drei Achsen und werden bei `> pi` zurückgesetzt (1149-1197). Treiber: `getAttacking()` (TheQueen.java:122), `getPower()` (130, DataWatcher 23). Partikel: bei `getPower() > 800` clientseitig zehn `fireworksSpark` pro Trigger 14 Blöcke über Position (221-227).

**Varianten.** `isHappy()` = `getIsHappy() == 0` (TheQueen.java:194-195, DataWatcher 22 = `mood`) wählt `TheQueentexture2` (RenderTheQueen.java:49); `mood` wird bei voller Gesundheit zufällig (1/500 pro Tick) auf 0 gesetzt und bei `always_mad` auf 1 (TheQueen.java:455-459).

**Klang.** `king_living` (147), `king_hit` (151), `trex_death` (155), `mothrawings` (203), Vanilla `random.fuse`/`random.bow`/`random.explode` (729-759, 1065).

**Portierungshinweise.** Drei Passes: opak (Körper), translucent (Membranen + 1..3 Würfel nach `power`, Würfel mit `LightTexture.FULL_BRIGHT` als packedLight), dann opak Augen — die Reihenfolge ist sichtbar und muss bleiben. Texturwahl in `getTextureLocation` über synchronisiertes `mood`; `power` synchronisieren, weil das Modell die Würfelzahl daraus liest. Health 6000 (manifest) -> virtuelle Gesundheit. `getPower()`-Partikel gehören in den Client-Tick (`com.swbr.orespawn.client`), nicht ins Modell.

---

### QueenHead (`queen_head`)

Wie `king_head`: nicht gezeichnet (`RenderQueenHead`, alle Methoden leer, Textur `null`), Hitbox 19,9 x 10,0 (QueenHead.java:23), `noClip` (24), Health = `TheQueen_stats.health` 6000 (manifest). Port identisch zu KingHead.

---

### PurplePower (`purple_power`)

**Aussehen.** Kein Körper, sondern ein funkelnder Stern: drei flache Balken (`Shape1/2/3`, je 14 px lang, 1 px dick; Geometrie-JSON `largest 14`) werden je sechsmal gezeichnet, `rotateAngleZ` steigt dabei pro Iteration um `1.0471976f` = 60° (ModelPurplePower.java:54, 63, 72), also ein 6-Speichen-Stern in der XY-Ebene je Balken; vor jeder Schleife wird die ganze Gruppe per `glRotatef` um einen **pro Bild zufälligen** Winkel (`rand.nextFloat()*360`) gedreht — Shape1 um X (50), Shape2 um Y (59), Shape3 um Z (68) — und derselbe `glRotatef` steht nach der Schleife nochmals (56, 65, 74), die Zufallsdrehungen akkumulieren also über die drei Gruppen. Das Ganze mit Lightmap auf 240/240, also **vollhell unabhängig vom Umgebungslicht** (48). Das Ergebnis flimmert wie ein Funkenball (ModelPurplePower.java:43-77). Texturen 64x32, flach ohne Detail, drei Helligkeitsstufen je Farbe (manifest, Bilder):
| Textur | Farbe | Typ (`getPurpleType()`) | Wirkung (PurplePower.java) |
|---|---|---|---|
| `purplepowertexture.png` | violett | 0 (Queen) | Leben /4 -1 und Schaden MaxHP/8 (281-283) |
| `purplepowertexture2.png` | rot | 1 | Leben x15/16 und 5 Schaden (288-289), dazu Feuer 10 s (291-292) |
| `purplepowertexture3.png` | grün | 2 | wie 1, dazu Gift 50 Ticks (294-295) |
| `purplepowertexture4.png` | blau | 3 | wie 1, dazu Schwäche 50 Ticks (297-298) |
| `purplepowertexture10.png` | weiß/grau | 10 (Ultimate King) | wie 0 plus Explosion 9.1 (284-286) |
Auswahl in RenderPurplePower.java:44-58 (`i == 1 -> texture2`, `2 -> texture3`, `3 -> texture4`, `10 -> texture10`, sonst texture). wiki „purple star" [MM] und Princess-Farben rot/grün/blau = Feuer/Gift/Schwäche [FW] stimmen.

**Größe.** Hitbox 0,75 x 0,75 (PurplePower.java:27). Renderer `(model, 0.3, 2.75)` (manifest): `scale = 2.75` für Typ 0, sonst fest `0.55` (RenderPurplePower.java:36-39). Stern-Durchmesser: 14 px * 2.75 / 16 = **2,4 Blöcke** (Queen), 14 * 0.55 / 16 = **0,48 Blöcke** (Princess-Orbs, Ultimate King). Zusatz-Partikel: `fireworksSpark` 1/4 pro Tick bei Typ 0, 1/6 bei den anderen (PurplePower.java:106-113).

**Modell.** `ModelPurplePower`, 3 Parts, 64x32, ganzes Modell translucent (`glColor4f(0.75,0.75,0.75,0.55)`, Blend 770/771), Ganzmodell-`glRotatef` um alle drei Achsen (06-models-design, GL-Tabelle).

**Animationen.** Nur die Zufallsrotation je Bild; `Shape1-3.rotateAngleZ` werden in `render` gesetzt (anim-JSON). Keine Abhängigkeit von f..f4.

**Klang.** Keine eigenen Sounds (manifest); Explosion Typ 10 über `world.newExplosion` (PurplePower.java:122, 285).

**Portierungshinweise.** `renderToBuffer` mit `poseStack.mulPose(Axis.XP.rotationDegrees(rand*360))`, dann 6 x Balken in 60°-Schritten um Z, dasselbe für Y und Z mit je eigenem Zufallswinkel (Reihenfolge und doppelte Anwendung wie oben, sonst weicht das Flimmermuster ab), `RenderType.entityTranslucent`, packedLight `LightTexture.FULL_BRIGHT`. Zufall im Renderer ist Absicht (Flimmern) und läuft rein clientseitig. `purple_type` in SynchedEntityData (DataWatcher 20, PurplePower.java:45). Health 1000 (manifest) und Angriff 500 liegen innerhalb der Klemmen; das Schadensmodell ist ohnehin Prozentschaden.

---

### The Prince (`the_prince`)

**Aussehen.** Handtellergroßer dreiköpfiger Drache: `body`, `neckbase`, drei Hälse (`neck1/neck`, `Lneck1/Lneck`, `Rneck1/Rneck`), drei Köpfe mit `snout`, `jaw`, `headfin`; `Backfin`, zwei Beine (`Rleg1`, `Lleg1`), Schwanz `tail1..4`, `Tail5`, `Tail6`; je Seite drei Flügelfinger (`Lwing`, `Lwing2`, `Lwing3`). Textur `ThePrincetexture.png` 128x128 (manifest): weißer Marmor wie beim König (`body` #e8e8e8), Kopffinnen, `Backfin` und Schwanzspitze `Tail6` gold (#fbbd18), Flügel als cremeweiße Halbkreise mit goldenem Saum (`Lwing` #faf3d9, die Bogenformen rechts im Atlas), Kiefer rosa-weiß (#e0d0d0). Das Modell hat keine Augen-Parts; die drei Augenkacheln rot / schwarz / blau liegen im UV der Kopfboxen (`Lhead` mit Rotstich #ece3e3, `head` neutral, `Rhead` mit Blaustich #e5e6ec) — dieselbe Zuordnung wie beim König: links rot, Mitte schwarz, rechts blau.

**Größe.** Hitbox 0,75 x 1,25 (ThePrince.java:54). Renderer `(model, 0.75, 0.75)` (manifest): scale 0.75, Schatten 0.5625. Ruhepose y 4..24, z -14..28, x ±26 px -> **0,9 Blöcke hoch, 2,0 lang, 2,5 Spannweite** (eigene Rechnung).

**Modell.** `ModelThePrince`, 35 Parts, 128x128. Translucent: `Rwing`, `Rwing2`, `Rwing3`, `Lwing`, `Lwing2`, `Lwing3` (ModelThePrince.java:448-460).

**Animationen** (ModelThePrince.java ab 229):
| Was | Treiber | Zeilen |
|---|---|---|
| Flügel Z: Flug/Angriff `cos(f2*2.3)*pi*0.4*f1`, Ruhe `cos(f2*0.3)*pi*0.04`; zweite Stufe `cos(f2*2.0)*pi*0.25*f1` | `f1 > 0.1 || getAttacking() != 0` | 236-251 |
| Beine X gegenläufig; bei `activity == 2` (Sitzen/Rasten) beide fest auf -1.0 | `getActivity()` | 254-262 |
| Schwanz Y-Welle 5 Glieder, Faktor 1.6/2.6/3.6/4.6, Pivots nachgeführt; 0 beim Sitzen, Angriff 0.12 | `isSitting()`, `getAttacking()` | 263-284 |
| Drei Köpfe: Gier = 2/3 von f3, Nick = 2/3 von f4, bei Angriff Kopfnicken per Sinus 1.9/2.1/2.3; Halsstreckung aus `getHead1Ext/2/3` | Entity-DataWatcher | 321-343 |
Treiber-Getter: ThePrince.java:111 (`getActivity`), 130 (`getAttacking`), 138-146 (`getHeadNExt`).

**Varianten.** Eine Textur.

**Klang.** `roar` (ThePrince.java:311, 6 Dateien), `duck_hurt` (315), `cryo_death` (319); Vanilla `random.bow` beim Schießen (776-818).

**Portierungshinweise.** Zwei Passes (opak/translucent Flügel). `activity`, `attacking`, `head1..3Ext` und `sitting` (TamableAnimal) synchronisieren; die Halsstreckung ist ein int in Grad, das Modell rechnet `toRadians`. Zahm-Verhalten (EntityTameable) -> `TamableAnimal`.

---

### The Young Prince (`the_young_prince`)

**Aussehen.** Zwischenstufe: dreiköpfiger Drache mit vier Flügelknochen je Seite (`wing1-4`, `rwing1-4`) und vier Membranen (`mem1-4`, `rmem1-4`), `lshoulder/rshoulder`, zwei Beine mit je fünf Krallen (`lclaw2,4,5,6,7`), fünf Schwanzsegmente plus `Tailspike1-3`, `headfin`, `backfin1/2`; je Kopf `neck3/4/5`, `head3`, `head7`, `jaw1`, `jaw5`, `headfin` mit Suffix L/R für die Seitenköpfe. Textur `PrinceTeentexture.png` 512x256 (manifest): weißer Marmor, goldene Kamm- und Flügelknochen, Augen rot / schwarz / blau (drei Kopfspalten mit roten Mundstreifen), Membranen weiß mit goldenem Wellen-Saum.

**Größe.** Hitbox 3,25 x 4,25 (ThePrinceTeen.java:77). Renderer `(model, 1.0, 1.25)` (manifest): scale 1.25. Ruhepose y -28..24, z -49..95, x ±77 px -> **4,1 hoch, 11,2 lang, 12,0 Spannweite** (eigene Rechnung).

**Modell.** `ModelThePrinceTeen`, 71 Parts, 512x256. Translucent: `mem1-4`, `rmem1-4` (ModelThePrinceTeen.java:870-884).

**Animationen** (ab 445): Flügel Z: Gehen `cos(f2*1.3)*pi*0.2*f1`, Ruhe 0.04, Fliegen (`activity == 1`) `cos(f2*1.4)*pi*0.4`, Angriff `cos(f2*1.7)*pi*0.4` (461-471); Membranen mit 90°-Phasenversatz (`+ pi/2`) (497-513); Beine/Krallen X (Laufzyklus); Schwanzwelle mit Tailspikes (571-584); **Kurvenlage im Flug**: bei `activity == 1` wird f3 durch `(prevRotationYaw - rotationYaw) * 10` ersetzt, über `RenderInfo.rf1` mit 1/50 geglättet und auf ±50 geklemmt (601-612) — der Körper legt sich in die Kurve; Köpfe wie Prince (655-688). Treiber: `getActivity()` (ThePrinceTeen.java:1320), `getAttacking()` (1309), `getRenderInfo/setRenderInfo` (clientseitiger Zustandsspeicher), `isSitting()`.

**Varianten.** Eine Textur.

**Klang.** `roar` (248), `alo_hurt` (254), `alo_death` (258), `mothrawings` 0.5/1.0 im Flug (646); Vanilla `random.fuse`, `fireworks.launch`, `random.bow` (1054-1096, 1436-1478).

**Portierungshinweise.** `RenderInfo` (rf1) ist Renderzustand pro Entity: in 1.21.1 als clientseitiges Feld am Entity oder als `Map<UUID, RenderInfo>` im Renderer, nie in SynchedEntityData. Reitbar (`updateRiderPosition`) — Sitzposition gehört in `getPassengerAttachmentPoint`. Health 1500 (manifest) -> virtuelle Gesundheit.

---

### The Young Adult Prince (`the_young_adult_prince`)

**Aussehen.** **Identische Geometrie wie der König** (119 Parts, gleiche Partnamen, gleiche Boxen laut Geometrie-JSON; Ausdehnung px identisch) und **dieselbe Textur** `TheKingtexture.png` (RenderThePrinceAdult.java, static-Block; manifest). Optisch ein halb so großer König mit Sitz- und Reitverhalten.

**Größe.** Hitbox 6,25 x 10,25 (ThePrinceAdult.java:73). Renderer `(model, 1.2, 1.0)` (manifest): scale 1.0, Schatten 1.2. -> **12,4 hoch, 44 lang, 50 Spannweite** (Königsmaße / 2.1, eigene Rechnung).

**Modell.** `ModelThePrinceAdult`, 119 Parts, 2048x2048; Translucent-Gruppe wie König (ModelThePrinceAdult.java:1228-1244).

**Animationen** (ab 733): wie König, zusätzlich Zahmzustände: Flügel bei `activity == 0` mit `f1` skaliert (Gehen statt Fliegen, 755-756), Ruhe beim Sitzen (759); dritte Flügelstufe `!isSitting() && activity == 0` -> `cos(f2*0.3)*pi*0.25*f1` (875-876); Schwanz beim Sitzen still (986); Köpfe: Nicken kommt **nicht** aus Sinus, sondern aus `toRadians(getHeadNExt() - 30)` in allen drei Zweigen (1048-1078), Kiefer in Ruhe kleine Sinus 0.03 (1099-1101). Treiber: `getAttacking()` (ThePrinceAdult.java:1280), `getActivity()` (1291), `getHead1..3Ext` (129-137), `isSitting()`.

**Varianten.** Eine Textur (die des Königs). `FullPowerKingEnable` (ThePrinceAdult.java:374) verwandelt in den echten König — anderes Entity, kein Texturwechsel.

**Klang.** `king_living` (243), `king_hit` (249), `trex_death` (253), `mothrawings` (622), Vanilla fuse/fireworks/bow (1031-1073, 1405-1447).

**Portierungshinweise.** `LayerDefinition` des Königs wiederverwenden (`ModelTheKing` und `ModelThePrinceAdult` unterscheiden sich nur in `render()`); ein gemeinsames `KingLikeModel<T extends KingLikeEntity>` mit Strategie für die Kopf-Neigung. Health 3000 (manifest) -> virtuelle Gesundheit. Reitbar wie Teen.

---

### The Princess (`the_princess`)

**Aussehen.** Geometrie des Prinzen (35 Parts) plus drei Energie-Würfel `Lpower`, `Cpower`, `Rpower` (37 Parts), die wie bei der Königin frei rotieren. Zwei Texturen 128x128 (manifest):
- `ThePrincesstexture.png` (Ruhe): eisblau-weißer Marmor (`body` #bcc9f5), Kopffinnen, `Backfin`, `Tail6` gold (#fbbd18), **grüne Augen** auf allen drei Köpfen (Bild; im UV der Kopfboxen, keine Augen-Parts), Flügel kobaltblau mit goldenem Saum (`Lwing` #4c5fc6), drei Würfel **`Lpower` rot #c8031f / `Cpower` grün #03c816 / `Rpower` blau #0331c8** (Feuer / Gift / Schwäche, vgl. PurplePower Typ 1-3, ThePrincess.java:538 `setPurpleType(1 + rand(3))`).
- `ThePrincesstexture2.png` (Angriff): schwarzgrauer Marmor (`body` #292929), Finnen blutrot (#e51e04), **magenta Augen** (Bild), Flügel rosa-grau marmoriert mit rotem Saum (`Lwing` #d0b7b7), alle drei Würfel magenta (#c705c9).
Wechsel über `getAttacking() != 0` (RenderThePrincess.java:45). wiki [FW] beschreibt nur die Orb-Farben; die Angriffs-Umfärbung ist nur in Textur und Renderer belegt.

**Größe.** Hitbox 0,75 x 1,25 (ThePrincess.java:58). Renderer `(model, 0.7, 0.7)` (manifest): scale 0.7 -> **0,9 hoch, 1,9 lang, 2,3 Spannweite** (eigene Rechnung).

**Modell.** `ModelThePrincess`, 37 Parts, 128x128. Translucent: sechs Flügelfinger **und** `Lpower`, `Cpower`, `Rpower` (ModelThePrincess.java:516-529), die Würfel wie bei der Königin mit Lightmap 240/240 vollhell (525) — anders als bei der Königin gibt es keinen opaken Augen-Pass danach.

**Animationen.** Wie Prince (ModelThePrincess.java ab 242: Flügel 249-264, Beine/Schwanz 267-297, Köpfe 334-356) plus freie XYZ-Rotation der drei Würfel mit Reset bei `> pi` (434-482). Partikel bei `getPower() > 400`: zwei `fireworksSpark` 0.4 über Position, 1/6 pro Tick (ThePrincess.java:458-464).

**Varianten.** `attacking` (DataWatcher 22, ThePrincess.java:143) wählt Textur 2.

**Klang.** `roar` (302), `duck_hurt` (306), `cryo_death` (310), `random.bow` (880-922).

**Portierungshinweise.** Wie Prince; `getTextureLocation` liest `attacking`. Würfel im translucent-Pass. `power` synchronisieren (DataWatcher 23, ThePrincess.java:89/95) für die Partikel.

---

### Mobzilla (`mobzilla`)

**Aussehen.** Aufrechte Kaiju-Silhouette: `BodyBottom/Center/Top`, `Neck`, `Head` mit `TopJaw`/`LowerJaw`, zwei Arme aus Schulter/Ober-/Unterarm/Hand mit je drei Fingern (Daumen, Zeige-, Ringfinger, je Basis + Spitze), zwei Beine (Thigh/Upper/Lower) mit **je neun Zehen** (`LToe1-9`, `RToe1-9`), Schwanz `TailBase`, `Tail2-7`, `TailTip`, Rückenplatten `Lspikes1/Rspikes1`, `Lspike2-5/Rspike2-5`, `Spike6`, `Spikes7`. Textur `Godzillatexture.png` 1024x1024 (manifest): fast überall schwarzgrauer, rissiger Stein mit hellen Adern (`BodyBottom`/`Head`/`Tail2` ≈ #2a2a2a); **Rückenplatten violett-lavendel mit blitzartigen hellen Adern** (die gezackten Formen oben rechts, uv 500,0..376: `Lspikes1` #865e9c, `Lspike3` #593871, `Spikes7` #92589c — die mittleren Platten sind dunkler); Kiefer dunkel mit Rotstich (`TopJaw` #322725, `LowerJaw` #3d2e2e), Maul innen dunkelrot mit weißen Zahnpunkten (Bild); Augen zwei kleine rote Punkte im `Head`-UV (Bild). Die weiß-graue Marmorkachel (uv 422,18 und 45,1002) sind die **Krallen**: alle sechs Fingerspitzen (`*IndexTip`, `*ThumbTip`, `*3rdFingerTip` #c2c6d1) und je Fuß die drei Zehenspitzen `LToe1/4/7`, `RToe1/4/7` (#c1c6d1); die Zehenmittelglieder und -basen bleiben dunkelgrau.

**Größe.** Hitbox 9,9 x 25,0 bei `PlayNicely == 0`, sonst 2,475 x 6,25 (Godzilla.java:48/51). Renderer `(model, 1.0, 2.0)` (manifest): scale 2.0, Schatten 2.0; `/4` bei PlayNicely (RenderGodzilla.java:35-39). Ruhepose y -194..27, z -171..224, x -149..145 px -> **27,7 hoch, 49 lang (mit Schwanz), 37 breit (Arme ausgestreckt)** (eigene Rechnung); Kopf ca. 27 über Boden, Hitbox 25.

**Modell.** `ModelGodzilla`, 71 Parts, 1024x1024, größte Kante 80 px (manifest). Konstruktor-Arg 0.2 = `wingspeed` (ModelGodzilla.java:84) — alle Frequenzen unten laufen mit Faktor 0.2, der Gang ist entsprechend träge. Keine Blend-Gruppe, keine GL-Transformation.

**Animationen** (ModelGodzilla.java ab 444; `pscale = 1.0` 449):
| Was | Treiber | Zeilen |
|---|---|---|
| Beine: Thigh/UpperLeg/LowerLeg X mit Grund-Offsets (-0.558 / -0.17 / 0.22) plus Laufsinus; Zehen heben sich (`clawYamp 18`, `clawZamp 35` px) nur in der Schwungphase (`t1 > 0`); rechtes Bein 4 x pi/4 versetzt | `f1 > 0.001`, `f2*0.75` | 462-583 |
| Schwanz Y-Welle 7 Glieder via `doTail(angle)`, Ruhe `cos(f2*0.75)*pi*0.05`, Angriff `cos(f2*1.75)*pi*0.2` | `getAttacking()` | 622-627, doTail |
| Kopf/Kiefer: Gier = `toRadians(f3)*0.55`, Nick = `toRadians(f4)`; Unterkiefer folgt mit 11 px Versatz | f3, f4 | 628-638 |
| Kieferschnappen und Armschwingen: Zufallsauslöser pro Sinusperiode (`RenderInfo.ri1/ri2`-Bits, 1/20 in Ruhe, 1/2 im Angriff), Amplituden 0.12 (Kiefer), 0.16 (Arme) | `getAttacking()`, `worldObj.rand` | 640-705 |
Arm-Kette: Unterarm 50 px, Hand 45 px hinter dem Oberarm per sin/cos nachgeführt (696-705).

**Varianten.** Eine Textur.

**Klang.** `godzilla_living` (Godzilla.java:152), `alo_hurt` (158), `godzilla_death` (162), Vanilla `random.explode` (507, 738), `random.fuse`/`random.bow` (634-646).

**Portierungshinweise.** Einfacher ModelPart-Port; `RenderInfo` (ri1, ri2, rf1, rf2) als clientseitiger Zustand je Entity. Health 4000 (manifest) -> virtuelle Gesundheit; Rüstung 25/21 unkritisch. Hitbox-Umschaltung nach `PlayNicely` über `EntityDimensions`/`refreshDimensions()`.

---

### MobzillaHead (`mobzilla_head`)

Nicht gezeichnet (`RenderGodzillaHead`, leere `doRender`, Textur `null`). Hitbox 9,9 x 10,0 (GodzillaHead.java:23), `noClip` (24), Health = `Godzilla_stats.health` 4000 (manifest). Port wie `king_head`.

---

### The Kraken (`the_kraken`)

**Aussehen.** Fliegender Riesenkalmar: `Head`, `Frontbody`, `Centerbody`, `Backbody`, `Tailbase1`, `Tail2`, `Tailtip`, zwei Flossen (`Finleft/right`), zwei Augen, zwei Saugnapf-Parts, Maul aus `Mouth1-8` mit **41 Zähnen** (`Tooth1-41`), `Jet` (Siphon), sechs Tentakel zu je acht Gliedern (`Tent11-18`, `Tent21-28`, `Tent31-38`, `Tent41-48`, `Tent51-58`, `Tent61-68`). Textur `Kraken.png` 512x512 (manifest), Zuordnung per UV-Sampling: **Kopf, alle Körpersegmente, Schwanz, beide Flossen und `Jet` sind der Weltraum-Nebel** — Nachtblau/Schwarz mit blauen Wolken, magenta Schleiern und weißen Sternen (`Head` #0c081e, `Finleft` #0a071c, `Tailtip` #080815); **alle 48 Tentakelglieder sind grauer, rissiger Stein** (dicke Glieder uv 80,161 / 0,162 #282828-#2d2d2d, dünne Spitzen uv 0,90 #242424, die 1-px-Enden uv 0,57); `Sucktioncupleft/right` graues Kreisraster (#757575, uv 80,84); die acht `Mouth`-Platten hellgrauer Marmor mit Adern (#545051-#575354); die 41 Zähne weiß (#f9f9f9, uv 0,0); **Augen rot** (`Lefteye`/`Righteye` #980202, uv 0/56,458 — die roten T-Kacheln unten links). Der Kraken ist das Logo-Tier des Mods (wiki [FW]).

**Größe.** Hitbox 4,0 x 15,0 bei `PlayNicely == 0`, sonst 1,333 x 5,0 (Kraken.java:50/53). Renderer `(model, 1.0, 1.0)` (manifest): scale 1.0; `/3` bei PlayNicely (RenderKraken.java:35-39). Das ganze Modell wird in `render()` um **90° um X** gedreht (ModelKraken.java:1248-1251), die Modell-Z-Achse steht also senkrecht: z -278..261 px -> **ca. 34 Blöcke von Kopf bis Tentakelspitze** in Ruhepose, x -69..86 px -> ca. 10 Blöcke breit (eigene Rechnung; Tentakel hängen animiert enger). Hitbox-Höhe 15 deckt nur den Rumpf.

**Modell.** `ModelKraken`, 111 Parts, 512x512, größte Kante 104 px (manifest). Ganzmodell-Transformation `glTranslatef(0,0,0); glRotatef(90,1,0,0)` um alle `part.render` (1248-1363). Kein Blend. UV-Ausreißer: `Tailtip` (uv 272,457, Box 24x24x32) ragt über den 512er-Atlas hinaus (06-models-design, „Boxes whose UV rectangle leaves the declared texture") — im Original wrapt das; im Port prüfen.

**Animationen** (ab 1129): Flossen Z `cos(f2*0.43)*pi*0.15` / `cos(f2*0.32)*pi*0.14` (1136-1137); Tentakel 5 und 6 mit `getAttacking()` als Parameter (Greif-Tentakel, 1139-1140), Tentakel 1-4 immer ruhig (1244-1247); `dangle_tentacle(f2, dir, att, p1..p8)` (1376-1484) setzt je Glied X- und Y-Sinus mit Phasenversatz `pi4` und richtungsabhängigem Offset, Pivots verkettet; Maul/Zähne öffnen und schließen per `RenderInfo.ri1` mit Zufallsauslöser pro Periode (1151-1243). Treiber: `getAttacking()` (Kraken.java, DataWatcher 20), `getRenderInfo()`.

**Varianten.** Eine Textur.

**Klang.** `kraken_living` (Kraken.java:184), `alo_death` (194); **kein Hurt-Sound**: `getHurtSound()` liefert `null` (189-190), deshalb nennt das manifest keinen.

**Portierungshinweise.** Die 90°-Drehung entweder als `poseStack.mulPose(Axis.XP.rotationDegrees(90))` in `renderToBuffer` oder in eine Root-Part-Pose backen — Letzteres ist sauberer, weil `setupAnim` dann ohne Sonderfall arbeitet. `dangle_tentacle` wird eine Methode über `ModelPart[]`. GeckoLib-Kandidat laut 06-models-design (111 Parts, 4,4 kB Animation), bleibt ModelPart. Health 1000 (manifest) unter der Klemme.

---

### Acid (`acid`)

Projektil (`LaserBall`-Unterklasse, `EntityThrowable`; manifest). Kein Modell: `RenderItemUrchin` (RenderSpinner) zeichnet ein **16x16-Sprite aus `spinners.png`** (256x256, 16 Spalten) als Billboard zur Kamera, um `rotationPitch` um Z gedreht, bei `glScalef(0.5)` -> ca. 0,5 Blöcke groß (RenderSpinner.java:25, 33-34, 42). Sprite-Index für Acid **85** (Acid.java:12, `getAcidIndex`), also Spalte 5, Zeile 5: ein Spritzer aus bräunlich-orangen Tropfen (Bild). Hitbox: offen (manifest `size: null`; EntityThrowable-Standard). Tracking 64/1/1 (manifest). Klang: keiner am Projektil. Port: `EntityRenderer` mit `RenderType.entityCutout(spinners.png)`, Quad in `Camera`-Ausrichtung, UV `(85 % 16) * 16 / 256`; oder das Sprite als eigenes 16x16-PNG herausschneiden und als `ItemRenderer`-Sprite nutzen — 1:1 ist das Atlas-Quad.

---

### Alien (`alien`)

**Aussehen.** Xenomorph-Parodie: `torso`, `stomach`, `neck`, Kopf aus `head`, `head1`, `head2`, zwei Kiefer (`jaw1`, `jaw2`) mit vier Fangzähnen, zwei Arme mit je drei Klauen (`clawl1-3`, `clawr1-3`), digitigrade Beine (`Thigh`, `Shin`, `Shin1`, `Foot`), Schwanz `tail1-5` mit `spike1-5`, und ein **Pfauen-Fächer am Kopf** aus `fan` plus `fanl1-7`/`fanr1-7`. Textur `MyAlien.png` 256x128 (manifest), per UV-Sampling: schwarzgrauer, rissiger Stein für Torso, Bauch, Hals, Beine, Arme und Schwanz (`torso` #2b2b2b, `stomach` #262626); Kopfboxen bräunlich-dunkel (`head1` #4d3f35, `jaw1` #40352d) mit **roten Augenpunkten** im `head`-UV (Bild) und vier weißen Fangzähnen (`fang1-4` #ffffff); **die 15 Fächer-Parts nutzen die Kacheln bei uv 130,10 und 149,10 — das sind die „blauen Kugeln an Stielen": jede Fächerfeder ist ein Pfauenauge**, blaue Kugel mit weißem Saum auf dunklem Stiel, transparent drumherum (`fan`/`fanl*`/`fanr*` Mittelwert #7c8199); Schwanzspitze `tail5` und alle fünf Schwanzstacheln dunkelrot (#940e0e, uv 178,66), alle sechs Klauen dunkelrot (#740909); die orange-weiß gestreiften Kacheln rechts sind die Zahnreihen der Kieferboxen (Bild). wiki [CC] „head feathers pop up like a peacock's" ist damit wörtlich belegt.

**Größe.** Hitbox 1,1 x 3,25 (Alien.java:38). Renderer `(model, 0.35, 1.1)` (manifest): scale 1.1. Ruhepose y -31..24, z -33..62, x ±29 px -> **3,8 hoch, 6,5 lang (mit Schwanz), 3,9 breit** (eigene Rechnung). wiki [FW] „huge, can't pass a 2-wide door" gegen [NW] „small to medium" — die Geometrie sagt 3,8 Blöcke, also groß.

**Modell.** `ModelAlien`, 55 Parts, 256x128, Konstruktor-Arg 0.22 = `wingspeed` (ModelAlien.java:68). Kein Blend, keine GL-Transformation.

**Animationen** (ModelAlien.java ab 348): Beine über `doLeftLeg/doRightLeg` mit `cos(f2*4.0)*pi*0.5*f1` (355-358); Fächer: alle 15 Fächer-Parts X-Sinus mit Phasenversatz `pi6` (`fanspeed`/`fanamp` je nach `getAttacking()`, 394-408); Kopf/Kiefer/Fangzähne Y folgen `neck`; `doJaw` X; `doTail` Y-Welle mit Stacheln (`RenderInfo.ri2/ri3`-Zufallsbits, 453-498); Klauen Y über `doLeftClaw/doRightClaw` mit `Math.abs`-Faltung. Treiber: `getAttacking()` (Alien.java:401 Setter), `getRenderInfo()`.

**Varianten.** Eine Textur.

**Klang.** `alien_living` (Alien.java:135), `alien_hurt` (141), `alien_death` (145; wiki [FW]: „lautester Todesschrei des Mods").

**Portierungshinweise.** Gerader ModelPart-Port; Helfer werden zu Methoden mit `ModelPart`-Parametern. RenderInfo clientseitig. Health 100, Rüstung 8 (manifest) unkritisch.

---

### Alosaurus (`alosaurus`)

**Aussehen.** Zweibeiniger Raubsaurier, stummelige Arme (`Shape11`, `Shape17`), Kopf/Hals `Shape1-6`, `jaw`, zwei Beine mit je vier Gliedern (`leftleg..leftleg4`), Rumpf/Schwanz `Shape18-21`. Textur `alosaurus.png` 128x128 (manifest): **leuchtend orange-rot** mit schwarzen Querstreifen auf Rücken und Schwanz, **pfirsichfarbener Bauch**, schwarz-rote Flammenmuster an Flanken, gelbe Augen, rotes Maul mit weißen Zähnen. Sehr plakativ, drei Farben.

**Größe.** Hitbox 1,9 x 3,6 (Alosaurus.java:24). Renderer `(model, 1.0, 1.0)` (manifest): scale 1.0. Ruhepose y -27..24, z -36..59, x -7..8 px -> **3,2 hoch, 5,9 lang, 0,9 breit** (eigene Rechnung). wiki [FW] „~2.5 blocks tall" ist zu niedrig; Quelle sagt 3,2 (Modell) bzw. 3,6 (Hitbox).

**Modell.** `ModelAlosaurus`, 21 Parts, 128x128, Konstruktor-Arg 0.22 = `wingspeed` (ModelAlosaurus.java:36). Kein Blend. Sampling: Kopfhörner `Shape18/19` reinrot (#ff0000), Beine #ff2e00, Rumpf `Shape1` braunrot mit den schwarzen Streifen (#953b17), Kopf `Shape5/6` #d22c0e/#e92c0c, `jaw` pfirsich (#ffa865).

**Animationen** (ModelAlosaurus.java ab 144): Beine X `cos(f2*1.3)*pi*0.25*f1` gegenläufig (149-152); Kiefer bei Angriff `0.52 + cos(f2*0.45)*pi*0.18`, sonst Ruhe (163-166); Ärmchen `-0.523 + cos(f2*0.1)*pi*0.05` (169-170). Treiber: `getAttacking()` (Alosaurus.java:214).

**Varianten.** Eine Textur.

**Klang.** `alo_living` (76), `alo_hurt` (82), `alo_death` (86).

**Portierungshinweise.** Trivialer Port; Partnamen `ShapeN` beibehalten oder im LayerDefinition-Generator benennen. Stats 110/18/8 (manifest) unkritisch.

---

### Robot Red Ant (`robot_red_ant`)

**Aussehen.** Sechsbeiniger Roboter-Ameisenkörper: `Body`, `Abdomen`, `Head`, zwei `Jet1/2` (Düsen am Hinterleib), `Hip1-6`, **ein einziges Bein-Set** `Leg1/2/3` + `Foot1-7`, das sechsmal mit anderen Winkeln gezeichnet wird, zwei Kiefer (`LJaw1/2`, `RJaw1/2`), zwei Antennen. Textur `AntRobottexture.png` 128x256 (manifest): **Signalrot** in zwei Tönen (hell/dunkelrot) für Panzerplatten mit weißen Nietpunkten an den Kanten, schwarze Gelenke, ein einzelnes **rotes Auge mit weißer Iris**, eine leuchtend rote senkrechte Linie (Antenne/Laser), graue Metallstreifen. Bewusst „Spielzeugroboter".

**Größe.** Hitbox 2,75 x 1,25 (AntRobot.java:41). Renderer `(model, 0.99, 1.0)` (manifest): scale 1.0. Ruhepose y -36..15, z -61..126 px -> **3,2 hoch, 11,6 lang** (eigene Rechnung, ohne dynamisch gesetzte Beine; Breite offen, weil `Leg1.rotationPointX/Z` jedes Bild aus `RenderSpiderRobotInfo.legoff/ymid` kommen, ModelAntRobot.java:210-212). Hinweis zur Bodenlage: `renderAntRobot` übersetzt nur an die Entity-Position, dreht um `180 - yaw` und spiegelt mit `glScalef(-1,-1,1)` (RenderAntRobot.java:27), **ohne** das `-1.5`-Offset von `RenderLiving` — Modell-y = 0 liegt damit auf Fußhöhe der Entity, und Modell-+y zeigt nach der Spiegelung nach **unten**. `Body` (y 0..14) und `Abdomen` (y -10..12) reichen also statisch unter die Fußebene; wo der Rumpf im Spiel tatsächlich liegt, entscheidet `RenderSpiderRobotInfo.yoff` (Leg1.rotationPointY = `yoff * -16`, ModelAntRobot.java:213) zusammen mit der Bein-Kinematik der Entity — offen, ohne Lauf nicht belegbar. Sampling: Beine leuchtrot (#f60101), Rumpf/Hinterleib/Kopf dunkleres Rot (#a50606/#9f0303), `Jet1/2` und `Hip1-6` schwarz, Kiefer grau (#5e5a5a/#a9a1a1), Antennen rot (#be2222).

**Modell.** `ModelAntRobot`, 27 Parts, 128x256. Kein Blend. Renderer ist **kein** normaler `RenderLiving`-Pfad: eigene `doRender` ohne `super.doRender` (kein Schatten, kein Hurt-Overlay, kein Name-Tag), Modell-Aufruf `model.render(entity, 0, 0, -0.1, 0, 0, 0.0625)` — Laufzyklus und Kopfwinkel sind konstant 0 (RenderAntRobot.java:28).

**Animationen** (ModelAntRobot.java ab 180): Beine komplett aus `RenderSpiderRobotInfo` (Arrays `ydisplayangle`, `uddisplayangle`, `p1xangle..p3xangle`, `ymid`, `legoff`, `yoff`, je 6 Einträge), die die Entity selbst tickt; Glieder 49 px lang verkettet (186-250). Kiefer Ruhe fest 0.89/1.378/2.216/1.745 rad, Angriff `cos(gpcounter*0.25)*pi*0.22` gegenläufig; Antennen kleine XZ-Sinus mit `gpcounter` (252-272). Treiber: `getAttacking()` (AntRobot.java:1151), `getRenderSpiderRobotInfo()` (588).

**Varianten.** Eine Textur.

**Klang.** `robotspider` 0.35/1.0 (AntRobot.java:755; 11 Dateien), `robotspidermount` 0.45 (993). Living/Hurt/Death: manifest nennt keine weiteren.

**Portierungshinweise.** Sechsfaches Rendern eines Bein-Sets: in 1.21.1 entweder sechs Bein-Sets im `LayerDefinition` anlegen (empfohlen, dann normale `setupAnim`) oder in `renderToBuffer` das Set sechsmal mit geänderter Pose zeichnen. Bein-Kinematik (`RenderSpiderRobotInfo`) ist Spiel-Logik und wird auf dem Server getickt, aber nur clientseitig gebraucht — sie kann komplett in den Client-Renderer wandern, wenn sie deterministisch aus Position/Yaw folgt (offen: liest sie Blockhöhen? AntRobot.java ab 600 prüfen). Der Verzicht auf `super.doRender` (kein Schatten, kein Rot-Blitz) ist Originalverhalten; für 1:1 `shadowRadius = 0` setzen und `getRenderType`/Overlay-Farbe neutral halten oder bewusst abweichen und dokumentieren. Reitbar (`updateRiderPosition`, AntRobot.java:601). Health 300, Rüstung 16 (manifest).

---

### Attack Squid (`attack_squid`)

**Aussehen.** Kleiner Tintenfisch: `body` plus acht Tentakel `tent1-8`. Textur `AttackSquid.png` 64x32 (manifest): petrol-/teal-blau mit dunkleren Flecken, zwei weiße Augen mit schwarzer Pupille, ein rundes **dunkelrotes Maul mit weißem Zahnkranz**.

**Größe.** Hitbox 1,0 x 1,25 (AttackSquid.java:39). Renderer `(model, 0.25, 0.9)` (manifest): scale 0.9. Ruhepose y 5..24, z -13..9, x -9..13 px -> **1,0 hoch, 1,3 lang, 1,2 breit** (eigene Rechnung).

**Modell.** `ModelAttackSquid`, 9 Parts, 64x32. Kein Blend.

**Animationen** (ModelAttackSquid.java ab 72): acht Tentakel je eigener Frequenz (1.0..1.9) mit Amplitude 0.4*f1 beim Schwimmen, 0.1 in Ruhe; Körper XYZ mit langsamen Sinus 0.25/0.39 (87-122). Nur f1/f2, kein Attacking-Zweig im Modell.

**Varianten.** Eine Textur.

**Klang.** `squid_hurt` (AttackSquid.java:103, 4 Dateien), `squid_death` (107, 2 Dateien), `random.bow` beim Tintenschuss (554-565). Spawnt als `waterCreature` in river/swampland/ocean (manifest).

**Portierungshinweise.** Trivial. `MobCategory.WATER_CREATURE`. Stats 10/8/0 (manifest).

---

### Criminal (`criminal`)

**Aussehen.** Humanoid im Anzug: `head`, `chest`, `belly`, `larm`, `rarm`, `lleg`, `rleg` (7 Boxen). Textur `BandPtexture.png` 64x128 (manifest): helles Gesicht mit dunkelbraunen Haaren und rotem Mund, **dunkelblauer Anzug** mit weißem Kragen und **roter Krawatte**, goldener Gürtelknopf, blaue Hose, schwarze Schuhe. „Politicians and Bankers" (wiki [OS]). Klasse heißt `BandP` (Bankers and Politicians).

**Größe.** Hitbox 0,75 x 1,75 (BandP.java:34). Renderer `(model, 1.0, 1.0)` (manifest). Ruhepose y -4..24, x ±13 px -> **1,8 hoch, 1,6 breit** (eigene Rechnung). Etwas gedrungener und breiter als ein Spieler.

**Modell.** `ModelBandP`, 7 Parts, 64x128, Konstruktor-Arg 0.4. Kein Blend.

**Animationen** (ModelBandP.java ab 60): Beine/Arme X `cos(f2*1.3)*pi*0.25*f1`, Bauch leichtes Wippen 0.025*f1 bzw. Ruhe 0.005/0.02, Kopf-Nick = `toRadians(f4)`, Kopf-Gier Y (67-84).

**Varianten.** Eine Textur. wiki [NW] „Banker/Police-Titel im Health-Bar" — nicht im Renderer belegt.

**Klang.** Vanilla Villager: `mob.villager.idle`, `mob.villager.hit`, `mob.villager.death`, Lautstärke 1.5 (BandP.java:88-101). Manifest führt keinen eigenen Sound.

**Portierungshinweise.** Trivialer Port; Villager-Sounds über `SoundEvents.VILLAGER_*`. Spawnt `ambient` in plains/desert/savanna (manifest). Stats 100/1/18 (manifest).

---

### Baryonyx (`baryonyx`)

**Aussehen.** Langgestreckter, vierbeinig-krokodilhafter Saurier aus 52 gleichförmig benannten Boxen (`Shape1-52`; Rücken-Stachelreihe, langer Schwanz, schmale Schnauze). Textur `Baryonyx.png` 128x128 (manifest), per UV-Sampling: Rumpf und Schwanz **dunkles Olivgrün** mit helleren Streifen (`Shape1` #38471b, `Shape7` #33451f), Beine tiefgrün (`Shape13/24` #00380e), Fußspitzen `Shape16/21` graugrün (#6e8a75), Schnauze `Shape4/5` olivbraun (#5d5121/#5f5438) mit den **roten Augen** im `Shape4`-UV (uv 54,108; Bild), **pfirsichfarbene Unterseite** (Bild); die **25 Rückenstacheln `Shape27-51`** (0 px breit, reine Flächen) und `Shape52` sind **dunkelrot** (#7f0000, uv 0,0); der **blaue Fleck** liegt im UV von `Shape3` (uv 29,110, Kehle unter dem Kopf); `Shape11/12/22/23` (Krallen) schwarz, weiße Zähne (Bild).

**Größe.** Hitbox 1,5 x 2,8 (Baryonyx.java:29). Renderer `(model, 1.0, 1.0)` (manifest); `scale / 2` bei `isChild()` (RenderBaryonyx.java:35-39). Ruhepose y -19..23, z ±46, x ±8 px -> **2,7 hoch, 5,8 lang, 1,0 breit** (eigene Rechnung).

**Modell.** `ModelBaryonyx`, 52 Parts, 128x128, Konstruktor-Arg 0.25. Kein Blend.

**Animationen** (ModelBaryonyx.java ab 330): Beine X (`Shape13/15/17/24/25/26`) `cos(f2*1.3)*pi*0.15*f1`; Kiefer/Kopf Z-Nicken (`Shape16`, `Shape21`) `cos(f2*0.7)*pi*0.25` (335-347). Nur 8 animierte Parts.

**Varianten.** Eine Textur; Jungtier halbe Größe (EntityAnimal, `createChild` Baryonyx.java:235).

**Klang.** `duck_hurt` als Hurt **und** Death (Baryonyx.java:81/85); `random.burp` beim Fressen (221). Kein Living-Sound im manifest.

**Portierungshinweise.** `Animal` mit `AgeableMob`-Skalierung (`scale()` halbiert bei `isBaby()`). Partnamen `ShapeN` beibehalten. Stats 40/8/- (manifest); wiki [OS] „Attack 0" widerspricht `attackDamage 8.0` (manifest) — Quelle gilt.

---

### Basilisk (`basilisk`)

**Aussehen.** Schlangenartiger Riesenleguan: sechs Rumpfsegmente `body1-6`, vier Schwanzsegmente `tail1-4`, `neck1/2`, `head`, `snout`, `jaw`, sechs Rückenstacheln `rog_1-6`. Textur `basilisk.png` 256x64 (manifest), vollständig opak (0 von 16384 Stichproben transparent), per UV-Sampling: Rumpf und Hals **sehr dunkles, moosgrünes Schuppenmuster** (`body1-5`, `neck1/2` teilen eine Kachel uv 0,32, #465544), der Schwanz hellt zur Spitze auf (`tail1` #505f4e -> `tail4` #7b867a), `head` noch dunkler (#243723), `snout`/`jaw` olivbraun (#3c3d2e/#2f261a) mit dunkelrotem Maul und weißer Kante (Bild), die sechs Stacheln `rog_1-6` **mattgold/senffarben** (#b6a461, #bcac71), olivgraue Bauchplatten mit Querrillen und kleine gelbe Augen (Bild). Auf Distanz fast schwarz.

**Größe.** Hitbox 1,6 x 3,5 (Basilisk.java:29). Renderer `(model, 0.5, 1.25)` (manifest): scale 1.25. Ruhepose y ±24, z -59..108, x ±11 px -> **3,7 hoch, 13 lang, 1,7 breit** (eigene Rechnung). wiki [FW] „over 10 blocks long, 3 blocks high" passt.

**Modell.** `ModelBasilisk`, 21 Parts, 256x64, Konstruktor-Arg 0.3. Kein Blend.

**Animationen** (ModelBasilisk.java ab 144): **Schlängeln**: `body1..6`, `tail1..4` Y-Sinus `cos(f2*1.3 - n*pi4)*pi*0.1*f1` mit Phasenversatz je Segment und nachgeführten Pivots (149-183); Kiefer bei Angriff `-1.0 + cos(f2*0.45)*pi*0.18` (184-188). Treiber: `getAttacking()` (Basilisk.java:434 Setter).

**Varianten.** Eine Textur.

**Klang.** `basilisk_living` (96), `alo_hurt` (102), `emperorscorpion_death` (106).

**Portierungshinweise.** Trivial; Segmentkette in `setupAnim` mit Pivot-Nachführung. Stats 200/24/15 (manifest).

---

### Beaver (`beaver`)

**Aussehen.** Kleiner Biber: `head`, `nose`, `teeth`, `body`, flacher `tail`, vier Füße (`rff`, `lff`, `rrf`, `lrf`). Textur `Beavertexture.png` 64x32 (manifest): **rotbraunes Schachbrett-Fell**, schwarze Knopfaugen, schwarze, flache Schwanzkelle, weiße Schneidezähne, dunkelbraune Nase.

**Größe.** Hitbox 0,6 x 0,8 (Beaver.java:32). Renderer `(model, 0.15, 0.75)` (manifest): scale 0.75; `/2` bei `isChild()` (RenderBeaver.java:35-39). Ruhepose y 14..24, z -8..15, x -1..7 px -> **0,5 hoch, 1,1 lang, 0,4 breit** (eigene Rechnung).

**Modell.** `ModelBeaver`, 9 Parts, 64x32, Konstruktor-Arg 0.5. Kein Blend.

**Animationen** (ModelBeaver.java ab 72): Füße X `cos(f2*3.7)*pi*0.45*f1` diagonal gegenläufig; **Zähne nagen** `cos(f2*2.7)*pi*0.25` dauerhaft; Schwanz X `cos(f2*0.5)*pi*0.05` (76-90).

**Varianten.** Eine Textur; Jungtier halbe Größe (`createChild` Beaver.java:301), Pitch 1.5 statt 1.0 (283).

**Klang.** `chainsaw` beim Baumfällen (Beaver.java:221), `scorpion_hit` als Hurt (267), `cryo_death` (271).

**Portierungshinweise.** Trivial; `Animal` mit Baby-Skalierung. Baumfällen (`breakRecursor`, mobGriefing-Check 216) ist Verhalten, nicht Design. Stats 15/1/- (manifest).

---

### Bee (`bee`)

**Aussehen.** Riesige Wespe: `Head` mit `LeftPincerMain/Extra`, `RightPincerMain/Extra` (Mandibeln), `Neck`, `MainBody`, fünf Hinterleibssegmente `Abdomnem1-5` plus `Sting`, zwei Flügel, drei Beinpaare `LA1-3`/`RA1-3` mit `LeftPom`/`RightPom` (Fühlerkolben). Textur `Beetexture.png` 256x256 (manifest), per UV-Sampling: die Segmente **wechseln ab** — `Abdomnem1`, `Abdomnem3`, `Abdomnem5`, `Neck` in **brauner Rindenstruktur** (#522f23, #512f23, #4d2b20, #523024), `Abdomnem2`, `Abdomnem4`, `MainBody` in **gelb-schwarzen Diagonal-Warnstreifen** (#6e6e00, #7f7f00, #7e7e00); `Head` dunkelbraun (#391d19) mit roten Rauten-Augen (Bild); Flügel grau marmorierte Fläche (#585a6e, im Original opak gezeichnet, kein Blend); `Sting` dunkelrot (#630202); Mandibeln fast weiß (#d9dcef); Beine dunkel rotbraun (#462121), Fühlerkolben schwarz.

**Größe.** Hitbox 1,5 x 2,5 (Bee.java:34). Renderer `(model, 0.9, 1.1)` (manifest): scale 1.1. Ruhepose y -36..24, z -43..8, x ±10 px -> **4,2 hoch (Hinterleib hängt gekrümmt nach unten), 3,4 lang, 1,4 breit** (eigene Rechnung; Abdomen ist 1.099 rad gekippt, ModelBee.java:195).

**Modell.** `ModelBee`, 23 Parts, 256x256, Konstruktor-Arg 2.0 = `wingspeed` (ModelBee.java:36) — alle Frequenzen unten laufen doppelt so schnell. Kein Blend.

**Animationen** (ModelBee.java ab 156): Flügel Z `cos(f2*1.1)*pi*0.3` dauerhaft (161); Mandibeln Y `cos(f2*0.3)*pi*0.1`; Beine XZ je Paar 0.21/0.27/0.31/0.37 mit Amplitude 0.06 (164-184); Hinterleib: Kette aus fünf Segmenten mit `-0.35` Krümmung je Glied, Wippen `cos(f2*0.021)*pi*0.023` in Ruhe, `cos(f2*0.11)*pi*0.055` im Angriff, Pivots per cos/sin nachgeführt (10/10/6/5/7 px), Stachel am Ende (189-210). Treiber: `getAttacking()` (Bee.java:62 Setter).

**Varianten.** Eine Textur.

**Klang.** `beebuzz` (Bee.java:75; manifest: Event von `Beebuzz` auf `beebuzz` umbenannt), `dragonfly_hurt` (79), `alo_death` (83).

**Portierungshinweise.** Trivial; Sound-Id in Kleinschreibung. Fliegt (`ambient`-Spawn in Wald/Dschungel/Taiga/Savanne, manifest) — `FlyingPathNavigation`. Stats 80/12/5 (manifest).

---

### BerthaHit (`bertha_hit`)

Unsichtbares Treffer-Projektil des Big-Bertha-Schwerts (`EntityThrowable`). `RenderItemUrchin.doRender` kehrt bei `instanceof BerthaHit` sofort zurück (RenderItemUrchin.java:9-10) — **es wird nichts gezeichnet**. Hitbox 0,33 x 0,33 (BerthaHit.java:27), Startgeschwindigkeit 0.4 (BerthaHit-Konstruktor, `f = 0.4f`), Tracking 64/1/1 (manifest). Port: Renderer-Registrierung mit `NoopRenderer` (leeres `render`), `shouldRender` false; alles andere ist Kampf-Logik.

---

## Offene Punkte dieses Batches

- AntRobot: Bodenlage des Rumpfs (hängt von `RenderSpiderRobotInfo.yoff` ab) und Breite mit Beinen — beides dynamisch, ohne Lauf nicht belegbar.
- Acid: Hitbox (manifest `size: null`; der Mod-Code setzt keine, es gilt der `EntityThrowable`-Standard, der hier nicht im Repo liegt).
- Alle Größenangaben sind Ruhepose-Ausdehnungen; die animierten Pivots (Flügel, Tentakel, Beine) können die Silhouette im Spiel sichtbar verändern.

Geschlossen gegenüber dem ersten Stand: Hintergrund beider Queen-Texturen ist transparent (Sampling); PurplePower-Schrittweite 60° (ModelPurplePower.java:54); Kraken hat keinen Hurt-Sound (Kraken.java:189-190); die Marmorkachel in `Godzillatexture.png` sind Finger- und Zehenspitzen; die Stielkacheln in `MyAlien.png` sind die Fächerfedern (Pfauenaugen); `wingspeed` ist das Konstruktor-Argument, nicht 1.0.
