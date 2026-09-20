# Design: design-entities-02

Stand 10.09.2026. Quellen: `docs/catalog/manifest.json` (kurz: manifest), dekompilierter Quelltext `reference/src-20.2/src/main/java/danger/orespawn/<Klasse>.java` (Zeilenangaben `Klasse.java:N`), Geometrie `reference/jar/models/<Model>.json`, Animationszusammenfassung `reference/jar/anim/<Model>.json`, Texturen unter `reference/jar/extracted/assets/orespawn/` (fuer die kleinen 64x32/64x64-PNGs mit Nearest-Neighbour hochskaliert angesehen), Web-Recherche `docs/research/01-mobs.md` und `06-models-design.md` (niedrigere Vertrauensstufe).

**Bildsprache dieser Charge.** Dreiundzwanzig Eintraege, die sich in drei Gruppen teilen. Erstens die *Kleintiere mit 64x32-Textur* (Chipmunk, Cassowary, Bird, Cricket, Cliff Racer, Cloud Shark, Dragonfly, Easter Bunny, Cave Fisher): flache Farbflaechen, ein bis zwei Akzentfarben, acht bis sechzehn Wuerfel, ein Laufzyklus aus `MathHelper.cos(f2 * k) * pi * amp * f1` und sonst nichts. Zweitens die *grossen Fotostruktur-Modelle* (Camarasaurus, CaterKiller, Cephadrome, Dragon, Crab, DungeonBeast): 128 bis 512 px breite Texturen aus verwaschenen Foto- oder Farbverlaufskacheln (Stein, Schokoladen-Swirl, zerkratztes Fell, gelb-orange Gradienten), 20 bis 64 Wuerfel, Ketten aus Hals- und Schwanzsegmenten, deren `rotationPoint` jeden Frame per `cos`/`sin` aus dem Vorgaenger errechnet wird (OreSpawns Ersatz fuer eine Teilehierarchie). Drittens die *Sonderfaelle ohne eigenes Modell*: Boyfriend (Vanilla-`ModelBiped` mit 48 Skins), Crystal Apple Cow (Vanilla-`ModelCow`), Coin (eine einzige 256x256x1-Scheibe mit Webseiten-Logo), Hoverboard (Boot-artiger `Render`, kein `RenderLiving`) und DeadIrukandji (Billboard-Sprite aus `spinners.png`). Renderer-seitig gilt fuer alle `RenderLiving`-Klassen dasselbe Schema: `super(model, par2 * par3)` (Schattengroesse = Produkt beider Konstruktorwerte), `this.scale = par3`, `preRenderScale` mit `glScalef(scale)` und ggf. `scale / 2` fuer Kinder.

Allgemeine Groessenrechnung: Modell-Pixel / 16 * Renderer-`scale` = Bloecke; Bodenlinie ist y = 24 im Modellraum (06-models-design.md, Tabelle „Y flip and ground offset"). Die Pixelmasse unten sind **unrotierte** Box-Ausdehnungen aus `models/<Model>.json` (Pivot + Origin + Size), Rotationen und die in `render()` verschobenen Pivots sind nicht eingerechnet; wo das stark abweicht, steht es dabei.

---

### Boyfriend (`boyfriend`)

- **Aussehen:** Ein Spieler-Skin auf dem Vanilla-Steve-Modell. `boyfriend0.png` (64x32): brauner Kurzhaarschnitt, helle Haut, schwarze Augen, gruen-schwarzer Creeper-Hoodie mit gruenem Creepergesicht auf der Brust, schwarze Aermel, gruene Hose. `swimshorts0.png`: derselbe Kopf, nackter Oberkoerper, gruene Badeshorts. `FrogPrince.png`: dunkelblau-rote Uniformjacke mit gelben Knopfleisten, brauner Pony. Die Recherche nennt „28 outfits + 18 swimsuits" (01-mobs.md, #74) — deckt sich mit `boyfriend0..27` und `swimshorts0..17` (Boyfriend.java:1010-1055). Haelt in der Hand einen Game Controller bzw. das gehaltene Item (RenderBiped-Standard; Itemlogik nicht Teil dieses Katalogs).
- **Groesse:** Hitbox 0.5 x 1.6 (Boyfriend.java:101, manifest). Kein `scale`-Override; `RenderBoyfriend` reicht nur Schatten 0.55 durch (manifest renderer_args, RenderBoyfriend.java:13). Rendergroesse = Vanilla-Biped, also 32 px = 2.0 Bloecke bis Kopfoberkante (Standard-`ModelBiped`, nicht OreSpawn-spezifisch).
- **Modell:** `ModelBiped` (Vanilla), Textur 64x32. Keine eigenen Teile, keine GL-Transformationen.
- **Animationen:** Vanilla-Biped: Arm-/Beinschwung nach `limbSwing`, Kopf folgt Blick, Armschwung bei Angriff (`updateArmSwingProgress`, Boyfriend.java:481). Nichts OreSpawn-Eigenes im Modell.
- **Varianten:** `Boyfriend.getTexture()` (Boyfriend.java:278-400): ist `wet_count <= 0`, dann `is_prince == 1` → `FrogPrince.png`, `is_prince == 2` → `FrogPrince2.png`, sonst `boyfriend<getTameSkin()>.png` mit Index 0..27 (DataWatcher 20, Boyfriend.java:432-443); ist `wet_count > 0`, dann `swimshorts<getWetTameSkin()>.png` mit Index 0..17 (DataWatcher 22, Boyfriend.java:445-447). `wet_count = 500` sobald der Boyfriend im Wasser oder in Lava steht (Boyfriend.java:483-484). Der Besitzer wechselt den Skin per Rechtsklick mit Leder oder Peacock Feather: nass → `which_wet_guy` zyklisch 0..17, trocken → `which_guy` zyklisch 0..27 (Boyfriend.java:605-623). `is_prince` setzt `Frog.java:105` beim Frosch-Kuss auf `1 + rand(2)`. Insgesamt 48 PNGs, 06-models-design.md zaehlt dieselben 48.
- **Klang (manifest, Boyfriend.java:242-833):** `b_fight` (7 Dateien), `b_taunt` (6), `b_woohoo` (4), `b_water` (2), `b_thunder`, `b_rain` (2), `b_dark`, `b_hurt` (10), `bb_happy` (6, BroMode) / `b_happy` (8), `b_ow` (9), `b_death_boyfriend` (gezaehmt) / `b_death_single` (2). Dazu Vanilla `damage.fallbig/fallsmall`, `random.bow`.
- **Portierungshinweise Modell/Renderer:** `HumanoidModel` + `HumanoidMobRenderer`; `getTextureLocation` liest drei synced-Werte (Skin, Wet-Skin, Prince) plus den serverseitigen `wet_count` — der muss ebenfalls synchronisiert werden, sonst weiss der Client nicht, wann Badeshorts gelten. Texturen: `textures/entity/boyfriend0..27.png`, `swimshorts0..17.png`, `frogprince.png`, `frogprince2.png` (manifest texture_map). Held-Item-Layer wie Vanilla (`ItemInHandLayer`).

---

### Brutalfly (`brutalfly`)

- **Aussehen:** Ein riesiger flacher Nachtfalter, nur 1 px dick. `Brutalflytexture.png` (64x32): fast schwarz; die beiden Vorderfluegel (`leftwing2`/`rightwing2`, 6x1x7) tragen je einen weissen Augenfleck mit rotem Kern, die Mittelfluegel (`leftwing3`/`rightwing3`) sind dunkelrot, die Hinterfluegel (`wing4`, 2x1x7) leuchtend rot, die Fluegelspitzen (`wing6`) tuerkis, der Koerper (`body`, 1x1x8, uv 21,19) braun. Darueber legt der Renderer in einem zweiten Pass `Brutalfly_overlay2.png`: orange-gelbe Lavaflecken auf dunklem Grund, additiv und langsam scrollend — der Falter glueht wie ein Stueck Magma. Recherche: „Fireballs galore", Metamorphose des Caterkillers (01-mobs.md #18); deckt sich mit dem Feuerthema der Textur.
- **Groesse:** Hitbox 5.0 x 2.0 (Brutalfly.java:40, manifest). Renderer `scale = 9.0` (manifest renderer_args [0.75, 9.0]; RenderBrutalfly.java:17,36), Schatten 0.75 * 9 = 6.75. Modell unrotiert: 19 px breit, 20 px lang, 1 px hoch, Oberkante bei y = 17 (models/ModelBrutalfly.json) → Spannweite 19 * 9 / 16 = **10.7 Bloecke**, Laenge 11.25 Bloecke, Schwebehoehe der Scheibe 7 px * 9 / 16 = 3.9 Bloecke ueber dem Fusspunkt. Die Fluegel schlagen um bis zu ±0.25 pi um Z, die effektive Spannweite schwankt also.
- **Modell:** `ModelBrutalfly`, 14 Teile (`body`, `head`, je 6 `leftwing*`/`rightwing*`), Textur 64x32 (manifest models_summary), Konstruktorargument 0.2 (`wingspeed`, ClientProxyOreSpawn). Keine GL-Aufrufe im Modell, keine transluzenten Teile.
- **Animationen (ModelBrutalfly.java:107-118):** `rightwing.rotateAngleZ = cos(f2 * 1.3 * wingspeed) * pi * 0.25`, alle sechs rechten Teile identisch, alle sechs linken negiert. Nur `f2` (ageInTicks) — dauerhafter Fluegelschlag, unabhaengig von Bewegung oder Angriff.
- **Varianten:** keine; eine Textur plus Overlay.
- **Klang:** `mothrawings` (3 Dateien; Brutalfly.java:119, manifest), Vanilla `random.bow`, `random.fuse` beim Feuerballwurf (Brutalfly.java:370-395).
- **Portierungshinweise Modell/Renderer:** `shouldRenderPass` Pass 1 (RenderBrutalfly.java:43-61): `glDepthMask(true)`, Overlay binden, Texturmatrix um `(ticks + partial) * 0.01` in u und v verschieben, `glColor4f(0.5, 0.5, 0.5, 1)`, Beleuchtung aus, `glBlendFunc(GL_ONE, GL_ONE)`; Pass 2 setzt zurueck. In 1.21.1 ein `RenderLayer` mit einem eigenen `RenderType` nach dem Muster von `EnergySwirlLayer` (Creeper-Ladung: `RenderType.energySwirl(texture, u, v)` liefert genau das scrollende Additiv-Overlay), Farbe 0.5-Grau. `scale()`-Override mit 9.0. Texturen: `textures/entity/brutalflytexture.png`, `textures/entity/brutalfly_overlay2.png`.

---

### Camarasaurus (`camarasaurus`)

- **Aussehen:** Ein langhalsiger Sauropode mit hochgereckter Halskette (Neck1..3 um -23°/-34°/-47° gekippt) und Kopf am Ende, vier Saeulenbeinen, viergliedrigem Schwanz. `camarasaurus.png` (256x256): braun-graue Stein-/Schuppenkachel mit cremefarbenen Sternflecken, Beine olivgruen-gelb gescheckt, orangefarbene Augen im Kopf. Recherche: „Camarasaurus" aus dem Red Ant Mining Dimension, zahm per roter Apfel, „Tail speed shows health/sitting" (01-mobs.md #80) — genau das tut die Schwanzanimation unten.
- **Groesse:** Hitbox 0.5 x 1.2 (Camarasaurus.java:29, manifest). Renderer `scale = 0.65`, Kinder `scale / 2` (RenderCamarasaurus.java:35-39). Modell unrotiert 40 px hoch (y -16..24), 72 px lang, 16 px breit → Kopfhoehe 40 * 0.65 / 16 = **1.6 Bloecke**, Laenge 2.9 Bloecke, Kind 0.8 Bloecke. Der Hals ist in den Konstruktordaten nach oben gekippt, die reale Kopfhoehe liegt also eher hoeher als die unrotierte Rechnung (offen: nicht nachgerechnet).
- **Modell:** `ModelCamarasaurus`, 21 Teile (Body1-4, Tail0-3, Neck1-3, Head1-2, 8 Beinteile), Textur 256x256, ctor 0.65. Keine GL-Aufrufe.
- **Animationen (ModelCamarasaurus.java:150-191):** Beine: `newangle = cos(f2 * 1.3 * wingspeed) * pi * 0.25 * f1` wenn `f1 > 0.1`, vorne links/rechts gegenphasig, hinten mit Grundwinkel -0.15. Schwanz: `hf = getCamarasaurusHealth() / getMaxHealth()`, `newangle = cos(f2 * 1.5 * wingspeed * hf) * pi * 0.25 * hf`, beim Sitzen 0 — Schwanzfrequenz **und** Amplitude sinken mit dem Leben; Tail0..3 wedeln um Y mit Faktor 0.25/0.5/0.75/1.0, die Pivots von Tail1..3 werden per `cos/sin(rotateAngleY) * 5/8/7` aneinandergehaengt. Hals: `Neck1..3`/`Head1..2` folgen `toRadians(f3)` (Kopf-Gier) mit Faktoren 0.125/0.25/0.38/1.0/1.0, Pivots per `cos/sin * 6/7/5` verkettet. Keine Kieferanimation.
- **Varianten:** keine.
- **Klang:** `cryo_hurt`, `cryo_death` (Camarasaurus.java:297-301, manifest); Vanilla `random.burp` beim Fressen (Camarasaurus.java:201), `damage.fallbig/fallsmall`.
- **Portierungshinweise Modell/Renderer:** `scale()` mit 0.65 bzw. 0.325 fuer `isBaby()`. `setupAnim` braucht `getHealth()/getMaxHealth()` und `isSitting` vom Entity. Alle Teile Kinder des Roots, Pivot-Verkettung als `ModelPart.x/z`-Schreibzugriff nachbauen. Textur: `textures/entity/camarasaurus.png`.

---

### Cassowary (`cassowary`)

- **Aussehen:** Ein gedrungener Laufvogel: dunkelnavyblauer Rumpf und Schwanz (uv 0,13 / 38,16), hellblauer Hals und Kopf (`neck1`, `neck`, `head`, uv 48,0 / 38,0 / 24,0), brauner Helmkamm (`crest`, uv 10,0, 1x4x5), roter Kehllappen (`gobbler`, uv 38,10, 1x5x1), rosa-braune Beine und Fuesse (uv 0,0 / 47,10). Der Hals ist als S-Bogen gebaut: `neck1` -68°, `neck` -161° (also fast nach hinten geklappt und dann mit `-2.827 rad` in `render()` ueberschrieben), Kopf und Schnabel am Ende. Recherche: nur Stats und Spawn (01-mobs.md #88), keine Aussehensbeschreibung.
- **Groesse:** Hitbox 0.5 x 1.2 (Cassowary.java:19, manifest). Renderer `scale = 1.0`, Kinder 0.5 (RenderCassowary.java:35-39). Modell unrotiert 26 px hoch (y -2..24), 17 px lang → **1.6 Bloecke**, Kind 0.8.
- **Modell:** `ModelCassowary`, 12 Teile, Textur 64x32, ctor 0.55. Keine GL-Aufrufe.
- **Animationen (ModelCassowary.java:96-128):** Laufzyklus `newangle = cos(f2 * 1.3 * wingspeed) * pi * 0.15 * f1` fuer `leg1`+`foot2` und gegenphasig `leg2`+`foot1`; `newangle2 = cos(f2 * 2.6 * wingspeed) * pi * 0.1 * f1` nickt den Hals (`neck.rotateAngleX = -2.827 + newangle2`) und schwingt den Kehllappen; `head`, `crest`, `beak` werden per `sin/cos(neck.rotateAngleX) * 7` an das Halsende gehaengt. Im Stand alles 0 — kein Idle.
- **Varianten:** keine.
- **Klang:** `duck_hurt` fuer Hurt und Death (Cassowary.java:68-72, manifest). Kein Living-Sound.
- **Portierungshinweise Modell/Renderer:** Reines `LayerDefinition`, `scale()` fuer Babys. Textur: `textures/entity/cassowary.png`.

---

### CaterKiller (`cater_killer`)

- **Aussehen:** Eine monstroese Raupe mit drei Segmentklassen: ein Kopf (`Head` 16x16x8) mit dunkel-schwarzbraunem Kieselmuster, darueber ein „falscher Kopf" (`falsehead` 20x20x10) aus glaenzendem Schokoladen-Swirl mit einem roten Augenring, zwei lange orange-rot gebaenderte Stosszaehne (`ltusk1`/`rtusk1` 33x3x3, um ±32° nach aussen) mit duennen Fortsaetzen, zwei schwarze Kieferplatten (`ljaw`/`rjaw`). Dahinter `seg1` (28x32x14, dreimal gezeichnet), `seg2` (40x34x18, sechsmal gezeichnet) und `seg3` (30x28x14) — alle im gleichen braun-orangen Swirl —, jedes Segment mit Seiten- und Rueckenstacheln, Beinen bzw. rotbraunen Klumpfuessen; das Endsegment traegt zwei 20 px lange Rueckenstacheln um -56°. `CaterKillertexture.png` ist 256x512. Recherche: „decimates plant life … Don't take too long, or he will mutate" (→ Brutalfly), `PlayNicely` skaliert ihn herunter (01-mobs.md #17).
- **Groesse:** Hitbox 2.9 x 4.6, bei `PlayNicely != 0` 1.45 x 2.3 (CaterKiller.java:39-42, manifest). Renderer `scale = 1.25`, halbiert bei `getPlayNicely() != 0` (RenderCaterKiller.java:35-39). Modell unrotiert 71 px hoch (y -47..24) → **5.5 Bloecke** hoch, 112 px breit (Stosszaehne) → 8.75 Bloecke. Die Laenge steht nicht in der Geometrie, sondern in `render()`: `seg2.rotationPointZ = 39 + (16 + zdist) * i` fuer i = 0..5, `seg3` bei `seg2rspike.rotationPointZ + 16`, Rueckenstacheln noch +6 (ModelCaterKiller.java:282-309) → Koerper von z ≈ -36 (Zaehne) bis ≈ 141 px, also ≈ 177 px * 1.25 / 16 ≈ **14 Bloecke** Laenge (Naeherung, Segmentboxen ragen noch ueber ihre Pivots hinaus).
- **Modell:** `ModelCaterKiller`, 31 Teilfelder, aber 3 + 6 Segment-Wiederholungen: `seg1`-Gruppe wird in einer `for (i < 3)`-Schleife an drei Z-Positionen gezeichnet, `seg2`-Gruppe sechsmal (ModelCaterKiller.java:246-300). Textur 256x512, ctor 0.22. Keine GL-Aufrufe.
- **Animationen (ModelCaterKiller.java:212-321):** Kiefer `ljaw/rjaw.rotateAngleZ = ±(0.139 + cos(f2 * 1.7|1.3 * ws) * pi * 0.07|0.025)` (Angriff | Ruhe). Kopfheben `headoff = cos(f2 * 1.7 * ws) * 8` (Angriff) bzw. `* 0.3 * ws) * 2` (Ruhe) auf alle Kopf-Pivots (`Head`, `falsehead`, tusks, jaws: Y = -8 / -25 / -1 + headoff). Zahnspitzen `ltusk2/rtusk2.rotateAngleY = ±0.802 + cos(f2 * 2.11|2.3 * ws) * pi * 0.08`. seg1-Schleife: Pivot-Y `-8 + headoff / (i+1) + 8 i`, Seitenstacheln `cos(f2 * 0.91 * ws + i * pi/8) * pi * 0.08`, Beine `rotateAngleX` mit 0.15 (Angriff) / 0.04 Amplitude. seg2-Schleife: Pivot-Z `39 + (16 + zdist) * i` mit `zdist = cos(f2 * 1.7 * ws + zpi) * 1.5 * f1`, `zpi += pi/4` je Segment — die Segmente ziehen sich beim Laufen wellenfoermig zusammen. seg3: Rueckenstacheln wippen um X (-0.977 ± 0.04 pi) und Y (±0.28 ± 0.04 pi).
- **Varianten:** keine Texturvarianten; Groessenvariante via `PlayNicely` (Config).
- **Klang:** `caterkiller_living` (4), `caterkiller_hit` (4), `caterkiller_death` (CaterKiller.java:110-120, manifest); Vanilla `random.explode`, `random.burp` (CaterKiller.java:461, 553).
- **Portierungshinweise Modell/Renderer:** Die Schleifen-Wiederholung geht mit `ModelPart` nicht direkt: entweder in `renderToBuffer` dieselben Parts mehrfach mit veraendertem `x/y/z` rendern (1:1 zum Original) oder 3 + 6 Kopien der Segmentgruppen als eigene Parts anlegen und in `setupAnim` befuellen (sauberer, 31 → 31 + 2*7 + 5*7 Parts). `scale()` 1.25 bzw. 0.625 nach synced `PlayNicely`. Textur: `textures/entity/caterkillertexture.png`.

---

### CaveFisher (`cave_fisher`)

- **Aussehen:** Eine bleiche, hummerartige Hoehlenkreatur: weiss-hellgrauer Schuppenpanzer (fast alle Koerperteile teilen uv 0,0 / 34,0), sechs cyanfarbene, mehrgliedrige Beine (je Bein sechs Teile `xxLeg1..6`, uv 0,13 / 0,0), zwei Arme aus fuenf Segmenten mit Scheren (`ClawTop`/`ClawLow`, uv 15,15 / 25,25, weiss mit cyanem Kern), cyanfarbener Schwanzbueschel (`TailTuft`, uv 0,23), schwarze Augen (`EyeLeft/Right`, uv 0,28), fuenf Rueckenstacheln (`Spine1..5`), acht schraeg gestellte Rueckenplatten (`BodyTopLeft/Right1..4`, um 6-12° gekippt). Recherche: „deep in caves", Drops Gold/Uran/Titan-Nuggets (01-mobs.md #66), keine Aussehensangabe.
- **Groesse:** Hitbox 1.35 x 0.75 (CaveFisher.java:26, manifest). Renderer `scale = 0.75` (RenderCaveFisher.java:35). Modell unrotiert 11 px hoch, 26 px breit, 33 px lang → **0.5 Bloecke** hoch, 1.2 breit, 1.55 lang.
- **Modell:** `ModelCaveFisher`, 75 Teile, Textur 64x32, ctor 0.62. Keine GL-Aufrufe. Hilfsmethoden `doLeftClaw(float)` / `doRightClaw(float)` (anim: je 8 `rotateAngleX`-Ziele, `Math.abs` 8x).
- **Animationen (ModelCaveFisher.java:477-538):** Drei Beinpaare um Y mit `cos(f2 * 2.0 * ws - k * pi/2) * pi * 0.12 * f1`, k = 0/1/2 fuer vorne/mitte/hinten, links/rechts negiert — sechs Segmente je Bein bekommen denselben Winkel (Bein schwenkt als Ganzes). Scheren: `RenderInfo r` (ri1, ri2) wird an jedem Nulldurchgang von `cos(f2 * 3 * ws)` neu gewuerfelt — `ri1 = rand(20)` in Ruhe, `rand(4)` bei Angriff; nur bei `ri1 == 1 || 3` laufen `doLeftClaw/doRightClaw(newangle)` mit `newangle = cos(f2 * 3 * ws) * pi * 0.15`, sonst mit 0 → gelegentliches, bei Angriff haeufiges Zuschnappen.
- **Varianten:** keine.
- **Klang:** `cryo_hurt`, `cryo_death` (CaveFisher.java:108-112, manifest).
- **Portierungshinweise Modell/Renderer:** `RenderInfo` ist ein pro-Entity-Zufallszustand, den das Modell ueber `getRenderInfo()/setRenderInfo()` (CaveFisher.java:76-80) mutiert — client-only halten (z. B. Feld am Entity, nur vom Renderer beschrieben). `scale()` 0.75. Textur: `textures/entity/cavefisher.png`.

---

### Cephadrome (`cephadrome`)

- **Aussehen:** Ein fliegender Hammerhai-Drache im Stil von Monster Hunter (01-mobs.md #45 nennt die Referenz). `Cephadrome.png` (512x256): grau-blaue, rissige Steinhaut auf allen Koerperteilen, tiefblaue (fast schwarze) glatte Flaechen fuer Membranen und Flossen, ein roter Augenpunkt. Kopf: `head` plus zwei Hammerkopfplatten (`hammerhead` 36x6x14, `hammerhead2` 50x4x7, beide um 26° gekippt) und `mouth`. Vier Rueckenflossen (`topfin1..4`, -106° bis -151°) mit Membranen dazwischen, dreigliedriger Hals, Brust/Bauch, dreigliedriger Schwanz mit vier Schwanzflossen und drei 0-px-dicken Membranflaechen, zwei Fluegel aus je vier Strahlen (`wingfin1..4`, 70/64/48/37 px lang, faecherfoermig um -10°/-25°/-45°/-68°) und einer 64x0x34-Membran, zwei dreigliedrige Beine mit Fuessen.
- **Groesse:** Hitbox 2.5 x 2.25 (Cephadrome.java:58, manifest). Renderer `scale = 1.0` (RenderCephadrome.java:35), Schatten 1.25. Modell unrotiert 66 px hoch (y -40..26) → **4.1 Bloecke**, Spannweite 158 px → 9.9 Bloecke, Laenge 145 px → 9 Bloecke.
- **Modell:** `ModelCephadrome`, 50 Teile, Textur 512x256, ctor 0.55. Keine GL-Aufrufe, keine Transluzenz — die Membranen sind normale, undurchsichtige 0-px-Boxen.
- **Animationen (ModelCephadrome.java:329-466):** Beine: am Boden `newangle = cos(f2 * 0.75 * ws) * pi * lspeed * 0.4` mit `lspeed` = tatsaechliche XZ-Geschwindigkeit aus `prevPos`/`pos`, geklemmt auf ±0.75; beim Fliegen (`getActivity() != 0`) starr angezogen (`newangle = 1.0`). Fluegel `rotateAngleZ`: fliegend `cos(f2 * 0.55 * ws) * pi * 0.28`, am Boden ruhend `-0.85 + cos(f2 * 0.2 * ws) * pi * 0.028`, am Boden angreifend `-0.65 + cos(f2 * 0.9 * ws) * pi * 0.068`; rechts negiert. Rueckenflossen atmen mit `|cos(f2 * 0.15 * ws) * pi * 0.05|`, gestaffelt /1, /2, /4, /8. Schwanz: `tailspeed 0.76 / tailamp 0.1`, in Ruhe am Boden 0.22 / 0.03, drei Segmente um Y phasenverschoben um pi/4, Pivots per `cos/sin * 13/13/10` verkettet, alle Schwanzflossen und Membranen folgen `tailfin1`. Hals/Kopf: beim Fliegen wird die Gierrate `(prevRotationYaw - rotationYaw) * 10` in `r.rf1` geglaettet (/50, Klemme ±50) und als Kurvenlage in den Hals gegeben, sonst `f3 / 2`; Faktoren 0.125/0.25/0.5/0.75 auf neck3/neck2/neck1/head, Pivots per `cos/sin * 14/14/8` verkettet, Hammerkopf und Maul folgen. Maul `rotateAngleX = -0.61 + cos(f2 * 0.5 * ws) * pi * 0.14` bei Angriff, sonst -0.87.
- **Varianten:** keine.
- **Klang:** `mothrawings` (Living wenn nicht fliegend mit 1/6-Chance, Cephadrome.java:170-171; im Flug alle 20 Ticks, :656-660), `alo_hurt`, `alo_death` (:177-181).
- **Portierungshinweise Modell/Renderer:** `RenderInfo.rf1` (Kurvenlage) und `getActivity()` (0 Boden / 1 Flug, DataWatcher 21) muessen dem Client vorliegen. `setupAnim` braucht Positions-Delta des Entity (`xo/zo`). Sonst reines `LayerDefinition`. Textur: `textures/entity/cephadrome.png`.

---

### Chipmunk (`chipmunk`)

- **Aussehen:** Ein winziges Streifenhoernchen: orange-brauner Ruecken, cremefarbener Bauch und Kehle, rosa Wangen (`Cheek1/2`), zwei Ohren, Nase, buschiger zweigliedriger Schwanz (`Tail1` 18°, `Tail2` 44° aufgestellt), vier Einpixel-Beine. `chipmunktexture.png` (64x32) tan/orange, oben rechts ein einfarbiges Quadrat (uv 40,0) fuer den Hut: **rot** in texture, **gruen** in texture2, **blau** in texture3 — sonst sind die drei PNGs identisch. Der Hut (`Hat1` 5x1x5 Krempe, `Hat2` 4x2x4 Kopfteil) wird nur bei aktivierten Kampf-Chipmunks gezeichnet. Recherche: „Battle Mob", zahm per roter Apfel (01-mobs.md #83).
- **Groesse:** Hitbox 0.35 x 0.35 (Chipmunk.java:22, manifest). Renderer `scale = 0.9`, Kinder 0.45 (RenderChipmunk.java:37-41). Modell 10 px hoch, 14.7 px lang → **0.56 Bloecke** hoch, 0.83 lang.
- **Modell:** `ModelChipmunk`, 18 Teile, Textur 64x32, ctor 1.0. Keine GL-Aufrufe.
- **Animationen (ModelChipmunk.java:132-157):** Beine `cos(f2 * 2.3 * ws) * pi * 0.25 * f1`, Leg1/3 vs Leg2/4 gegenphasig. Kopfgruppe (Head, Nose, Ear1/2, MouthUnder, Cheek1/2, Hat1/2) `rotateAngleY = toRadians(f3) * 0.45`. Schwanz, sofern nicht sitzend: `Tail1.rotateAngleX = 0.306 + cos(f2 * 0.25) * pi * 0.06 + cos(f2 * 1.3 * ws) * pi * 0.25 * f1`, `Tail2 = 0.306 + Tail1` — sitzend friert der Schwanz ein.
- **Varianten:** Textur per `RenderChipmunk.getEntityTexture` (RenderChipmunk.java:48-59): `get_is_activated() != 0` und `getHatColor() == 2` → texture2 (gruen), `== 3` → texture3 (blau), sonst texture (rot). `hat_color` setzt `EntityCannonFodder` beim Rechtsklick: Karotte → 1 (:98), Quinoa (`MyQuinoa`) → 2 (:142), Kartoffel → 3 (:120). `is_activated` 1 nach erstem Anfuettern, 2 nach Zweitbesitzer (:80-103). Hutzeichnung: `Hat1` wenn `is_activated != 0`, `Hat2` zusaetzlich wenn `> 1` (ModelChipmunk.java:174-178).
- **Klang:** `scorpion_hit`, `cryo_death` (Chipmunk.java:205-209, manifest); Vanilla `damage.fallbig/fallsmall`; `random.explode` aus EntityCannonFodder (:184).
- **Portierungshinweise Modell/Renderer:** `is_activated` und `hat_color` sind DataWatcher 20/21 (EntityCannonFodder.java:55-56) → `SynchedEntityData`. Hutteile per `visible` steuern. `scale()` 0.9 / 0.45. Texturen: `textures/entity/chipmunktexture.png`, `chipmunktexture2.png`, `chipmunktexture3.png`.

---

### Cliff Racer (`cliff_racer`)

- **Aussehen:** Ein flacher Flugsaurier-Vogel aus acht Wuerfeln: dunkelgrauer Rumpf (`Body` 3x1x10), zwei braune Fluegelplatten (`LWing`/`RWing` 7x1x6, uv 0,31 / 39,0), langer roter Schwanz (`Tail` 1x1x9, uv 0,16) mit orangefarbenem Schwanzende (`TailEnd` 2x2x2, uv 0,10), senkrechte dunkle Rueckenflosse (`Fins` 1x6x3, uv 0,40), weisser Kopf mit blauen Augen (`Head` 2x2x2, uv 28,21), dunkelroter Schnabel (`Beak` 1x1x2, uv 0,0). Recherche: „harmless prehistoric bird", Danger Dimension (01-mobs.md #112).
- **Groesse:** Hitbox 0.75 x 0.5 (CliffRacer.java:18, manifest). Renderer `scale = 1.0` (RenderCliffRacer.java:35). Modell 6 px hoch, Oberkante y = 11 (13 px ueber Boden → 0.8 Bloecke Schwebehoehe), Spannweite 17 px → **1.06 Bloecke**, Laenge 25 px → 1.56 Bloecke.
- **Modell:** `ModelCliffRacer`, 8 Teile, Textur 64x64, ctor 1.0. Keine GL-Aufrufe. Hinweis aus 06-models-design.md: `RWing` (uv 39,0, 7x1x6) braucht 65 px Breite, ragt also 1 px ueber die 64x64-Textur hinaus — in 1.21.1 mit UV-Wrap unauffaellig, aber der Rand ist ein Pixel des linken Texturrandes.
- **Animationen (ModelCliffRacer.java:70-72):** `LWing.rotateAngleZ = cos(f2 * 1.3 * ws) * pi * 0.25`, `RWing` negiert. Dauerschlag, nichts sonst.
- **Varianten:** keine.
- **Klang:** `cliffracer` (CliffRacer.java:46, manifest).
- **Portierungshinweise Modell/Renderer:** Trivial. Textur: `textures/entity/cliffracertexture.png`.

---

### Cloud Shark (`cloud_shark`)

- **Aussehen:** Ein kleiner Hai aus hellgrauem, wolkig verrauschtem Material (`CloudShark.png` 64x64, fast vollflaechig grau-weisses Rauschen), mit schwarzem Maul und weissen Zahnreihen (`jaw` uv 42,0 und `head` uv 0,51, schwarz-weiss gemustert), einer blauen Zickzack-Linie als Flossenkante, senkrechter Rueckenflosse (`topfin` 54°), Schwanzflosse (`fins`, 0 px dick, 53°) und zwei seitlichen Brustflossen (`leftfin`/`rightfin`, 0 px dick, um ±65° Y und ∓35° X/Z gestellt). Recherche: spawnt am Boden und steigt in die Wolken, Cloud Shark Dungeons im Himmel (01-mobs.md #64).
- **Groesse:** Hitbox 1.0 x 0.75 (CloudShark.java:22, manifest). Renderer `scale = 1.0`. Modell 15 px hoch, 34 px lang → **0.94 Bloecke** hoch, 2.1 lang.
- **Modell:** `ModelCloudShark`, 8 Teile, Textur 64x64, ctor 1.0. Keine GL-Aufrufe.
- **Animationen (ModelCloudShark.java:70-77):** `leftfin.rotateAngleY = 1.15 + cos(f2 * 0.7 * ws) * pi * 0.15`, `rightfin = -0.9 + cos(f2 * 1.5 * ws) * pi * 0.15`, Schwanzflosse `fins.rotateAngleY = cos(f2 * 1.5 * ws) * pi * 0.25`, Kiefer `jaw.rotateAngleX = 0.5 + cos(f2 * 0.5 * ws) * pi * 0.1`. Alles nur zeitgesteuert, kein Bezug zu Bewegung oder Angriff.
- **Varianten:** keine.
- **Klang:** `little_splat` (Hurt), `big_splat` (Death) (CloudShark.java:59-63, manifest).
- **Portierungshinweise Modell/Renderer:** Trivial. Textur: `textures/entity/cloudshark.png`.

---

### Bird (`bird`, Klasse `Cockateil`)

- **Aussehen:** Ein kleiner Papagei/Nymphensittich: Rumpf 5x3x6, Kopf 3x3x4, oranger Schnabel (`Beak`, uv 0,21) mit Unterschnabel, drei rote Kammfedern (`feather1..3`, uv 11-19,9, um -13°/-37°/-73° gefaechert), zwei Fluegel aus je zwei Teilen (`lwing1/rwing1` senkrecht ±90° angelegt, `lwing2/rwing2` flach), drei Schwanzfedern mit weissen Spitzen, zwei braune Beinchen (uv 0/4,12). Textur 64x32 in sechs Farbversionen: `Bird1` gelb mit orangefarbenem Wangenfleck (Nymphensittich), `Bird2` rot, `Bird3` magenta, `Bird4` blau, `Bird5` gruen, `Bird6` dunkelrot schattiert — Schnabel, Kamm (rot) und Beine sind in allen sechs gleich. Recherche: „Birds … Health 2, rare ruby drop; red birds drop the ruby" (01-mobs.md #107).
- **Groesse:** Hitbox 0.5 x 0.5 (Cockateil.java:37, manifest). Renderer `scale = 0.75` (RenderCockateil.java:34). Modell 11 px hoch, 19 px breit (Fluegel flach), 23 px lang → **0.52 Bloecke** hoch, 0.9 breit, 1.1 lang.
- **Modell:** `ModelCockateil`, 16 Teile, Textur 64x32, ctor 1.0; laut 06-models-design.md auch von `RubyBird` benutzt. Keine GL-Aufrufe.
- **Animationen (ModelCockateil.java:118-132):** Fluegel `newangle = cos(f2 * 1.5 * ws) * pi * 0.35`; `lwing1.rotateAngleZ = -1.5 + newangle`, `lwing2 = newangle`, rechts gespiegelt (`1.5 - newangle`, `-newangle`). Schwanzfedern `rotateAngleX = cos(f2 * 0.3 * ws) * pi * 0.1`, Kammfedern `rotateAngleZ = cos(f2 * 1.1|1.2|1.3 * ws) * pi * 0.08`. Nur zeitgesteuert.
- **Varianten:** `Cockateil.getTexture()` (Cockateil.java:52-70) waehlt `Bird1..6` nach `getBirdType()` (DataWatcher 22), der im Konstruktor mit `rand.nextInt(6)` gesetzt und per NBT `BirdType` gespeichert wird (Cockateil.java:80-81, 259).
- **Klang:** `birds` (23 Dateien, Living), `duck_hurt` (Hurt und Death) (Cockateil.java:106-116, manifest).
- **Portierungshinweise Modell/Renderer:** `getTextureLocation` nach synced `birdType`. Texturen: `textures/entity/bird1.png` … `bird6.png`. Modell fuer `RubyBird` wiederverwenden.

---

### Coin (`coin`)

- **Aussehen:** Werbeobjekt: eine 2 Bloecke grosse, einen Pixel duenne, um die Hochachse rotierende Muenzscheibe. `Cointexture.png` (512x512): links ein blauer, glaenzender Kreis mit dem schwarzen OreSpawn-Emblem (Kraken-Tentakel um einen eckigen Kopf), rechts derselbe blaue Kreis mit dem Text „www.OreSpawn.com / Guides, Sneak Peaks, & New Downloads"; untere Haelfte leer. Recherche: „in-game advertising for the website; one hit kills" (01-mobs.md #114).
- **Groesse:** Hitbox 1.5 x 1.5 (Coin.java:19, manifest). Renderer `scale = 0.125` (manifest renderer_args [0.75, 0.125]; RenderCoin.java:35). Box 256x256x1 bei Pivot y = -109 (ModelCoin.java:17-18) → Scheibe von y = -237 bis 19, also 256 * 0.125 / 16 = **2.0 Bloecke** Durchmesser, Unterkante 5 px * 0.125 / 16 = 0.04 Bloecke ueber dem Boden, Dicke 1 px * 0.125 = 0.008 Bloecke.
- **Modell:** `ModelCoin`, 1 Teil (`Shape1`), Textur 512x512, ctor 0.22. Die Nordseite der Box liegt auf uv (1..257, 1..257) = linker Kreis, die Suedseite auf (258..514) = rechter Kreis; die Rueckseite ragt 2 px ueber die Textur (06-models-design.md, „Boxes whose UV rectangle leaves the declared texture").
- **Animationen (ModelCoin.java:28-29):** `Shape1.rotateAngleY = cos(f2 * 0.05 * ws) * pi` — die Muenze pendelt um ±180° um Y, also ein langsames Hin-und-Her-Drehen, keine Endlosrotation.
- **Varianten:** keine.
- **Klang:** keine — `getLivingSound/getHurtSound/getDeathSound` liefern `null` (Coin.java:66-76).
- **Portierungshinweise Modell/Renderer:** Ein `ModelPart` mit `addBox(-128, -128, 0, 256, 256, 1)`; der 2-px-Ueberhang der Rueckseite braucht in 1.21.1 dieselbe Toleranz (UV wird normalisiert, Wrap ist Standard). `scale()` 0.125. Textur: `textures/entity/cointexture.png`.

---

### Crab (`crab`)

- **Aussehen:** Trotz Texturname `RobotCrabtexture.png` eine organisch gefaerbte Riesenkrabbe: Panzer (`body1` 76x10x48, `body2`, `body5/6`, `body3/4` Seitenleisten) in gelb-orangem Farbverlauf mit rot-orangen Kanten, acht Beine (drei Segmente, 12/16/16 px) in blauem Verlauf (uv 128,0-43), zwei Scheren: Basis (`claw1/2`, uv 0,80 / 0,105) gelb-gruen, Scherenzangen (`claw3..5`, uv 0,131-197) schwarz mit silbernen Chevron-Streifen, zwei 12 px hohe Augenstiele mit magenta-weissen Augenwuerfeln (uv 26-62,0), zwei weisse Mundplatten. Textur 256x512. Recherche: drei Groessen „large / medium / small" mit Attack 24/12/6 (01-mobs.md #46) — die Attribute skalieren tatsaechlich mit `getCrabScale()` (Crab.java:55-56).
- **Groesse:** Hitbox beim Bau 1.25 x 2.5 (Crab.java:38, manifest), in `entityInit` auf `3.75 * scale x 3.5 * scale` (Crab.java:83) und spaeter `2.5 * scale x 3.5 * scale` (Crab.java:124) gesetzt. `getCrabScale()` (DataWatcher 21 / 100, Crab.java:86-97): Standard 0.25, mit `rand(4) == 1` 0.5, mit `rand(8) == 2` 1.0 (Crab.java:63-70), aus einem Spawner 0.35 (Crab.java:489). Renderer skaliert mit genau diesem Wert (RenderCrab.java:35-36). Modell unrotiert 63 px hoch (y -23..40), 108 px breit, 113 px lang; mit den in `render()` gesetzten Beinpivots (x = ±36, z = 0/10/20/30, ModelCrab.java:172-329) → bei scale 1.0 **3.9 Bloecke** hoch, 6.75 breit, 7 lang; bei 0.25 ≈ 1 x 1.7 x 1.8; bei 0.5 ≈ 2 x 3.4 x 3.5.
- **Modell:** `ModelCrab`, 25 Teilfelder, aber nur **drei** Beinteile (`leg1/2/3`), die achtmal an verschiedenen Pivots gezeichnet werden (vier je Seite, ModelCrab.java:172-329 setzt vor jedem Zeichnen `rotationPointX/Y/Z` und `rotateAngleY` neu). Textur 256x512, ctor 1.0. Keine GL-Aufrufe.
- **Animationen (ModelCrab.java:169-396):** Beine `rotateAngleY = -pi/2 ± cos(f2 * 1.7) * pi * 0.15 * f1`, benachbarte Beine gegenphasig, rechte Seite negiert. Augenstiele: Ruhe `rotateAngleX = cos(f2 * 0.35|0.3) * pi * 0.05`, `rotateAngleZ = ±0.54 + cos(f2 * 0.25|0.45) * pi * 0.05`; Angriff doppelte Amplitude 0.1. Mundplatten `∓0.72 ± cos(f2 * 0.25) * pi * 0.05` (Angriff 0.45-Frequenz, 0.15 Amplitude). Scheren: `claw3/4/5.rotateAngleY = ∓0.453/∓0.349/±0.384 ± cos(f2 * 0.15|0.13) * pi * 0.03|0.02` in Ruhe, bei Angriff Frequenz 0.35/0.43 und Amplitude 0.13/0.12 — die Zangen klappen deutlich auf und zu.
- **Varianten:** keine Textur; drei Groessen ueber `getCrabScale()`.
- **Klang:** `leaves_hit` (Hurt, Crab.java:152), `scorpion_attack`, `scorpion_living` (Crab.java:378-381), Vanilla `splash` (:395). Tonhoehe `2.0 - 0.3 / scale` (Crab.java:164) — kleine Krabben piepsen.
- **Portierungshinweise Modell/Renderer:** `scale()` liest den synced Skalierungswert; `EntityDimensions` per `refreshDimensions()` mitskalieren. Beinwiederholung wie beim CaterKiller: acht Beinkopien als eigene Parts anlegen (3 * 8 = 24 statt 3) und in `setupAnim` befuellen. Textur: `textures/entity/robotcrabtexture.png`.

---

### Creeping Horror (`creeping_horror`)

- **Aussehen:** Ein flacher, schwarzer Skorpion-Spinnen-Hybrid: Wuerfelkoerper (`body` 8x8x8, uv 0,30, schwarz mit weissen Augenpixeln), vier lange Beine aus je zwei Teilen (`leg1..4` 16x2x2 um ±33° Y / ±11° Z, `legXpart2` senkrecht 2x5x2), dreigliedriger Schwanz (`tailseg1` -32°, `tailseg2` 2x1x11 braun-orange, `tailseg3`), zwei Zangen aus je zwei Teilen (`pincer1/2` ±13°), fuenf Stacheln (`spike1..5`, 40° X, -15°..15° Y aufgefaechert), fuenf 0-px-dicke „insides"-Flaechen im Koerper (uv -1..0, 29..30 — dunkelrot). `CreepingHorror.png` (128x128) ist fast vollstaendig schwarz mit braunen Beinspitzen und dunkelroten Innereien. Recherche: „Danger Dimension, in packs" (01-mobs.md #63).
- **Groesse:** Hitbox 0.75 x 0.5 (CreepingHorror.java:21, manifest). Renderer `scale = 0.75` (RenderCreepingHorror.java:35). Modell 8 px hoch, 38 px breit, 29 px lang → **0.4 Bloecke** hoch, 1.8 breit, 1.4 lang.
- **Modell:** `ModelCreepingHorror`, 26 Teile, Textur 128x128, ctor ohne Argument (manifest model_args leer). Keine GL-Aufrufe.
- **Animationen (ModelCreepingHorror.java:174-222):** Beine `rotateAngleY = ±0.576 ± cos(f2 * 1.25) * pi * 0.35 * f1`, Beinpaare 1/4 vs 2/3 gegenphasig, `part2` folgt. Zangen `±cos(f2 * 0.48) * pi * 0.15`. Schwanz hebt sich mit `|cos(f2 * 0.11) * pi * 0.25|`: `tailseg1 = -0.55 + a`, `tailseg2 = a`, `tailseg3 = -0.22 + a`. Fuenf Stacheln wackeln unabhaengig in X (`0.7 + cos(f2 * 0.81..0.107) * pi * 0.08`), Y (`cos(f2 * 1.11..1.31)`) und Z (`cos(f2 * 1.41..1.61)`), je 0.08 pi Amplitude. Keine Angriffsabfrage.
- **Varianten:** keine.
- **Klang:** `creepinghorror_living`, `creepinghorror_hit`, `creepinghorror_dead` (CreepingHorror.java:75-83, manifest).
- **Portierungshinweise Modell/Renderer:** Die `insides`-Boxen mit Groesse 0 in einer Achse und uv -1 sind in 1.21.1 zulaessig (negative `texOffs` ergeben nur eine UV < 0, die wrappt). `scale()` 0.75. Textur: `textures/entity/creepinghorror.png`.

---

### Cricket (`cricket`)

- **Aussehen:** Eine blaue Grille im Zehntelblock-Format: blauer Koerper (`body` 3x3x6), blauer Kopf mit roten Mandibeln/Augenstreifen und weissen Pixeln (uv 0,17), kleines Abdomen, vier duenne Vorderbeine (`lfleg`/`rfleg`/`lrleg`/`rrleg`, 5-6x1x1, schraeg gestellt), zwei grosse Sprungbeine aus je zwei Teilen (`lleg1/rleg1` 1x2x8, `lleg2/rleg2` 1x1x8). `Crickettexture.png` (64x64) blau mit weissen und roten Akzenten. Recherche: „jumping, night chirping" (01-mobs.md #105).
- **Groesse:** Hitbox 0.1 x 0.1 (Cricket.java:20, manifest). Renderer `scale = 0.5` (RenderCricket.java:35). Modell 7.5 px hoch, 13 px breit, 18.5 px lang → **0.23 Bloecke** hoch, 0.4 breit, 0.58 lang.
- **Modell:** `ModelCricket`, 11 Teile, Textur 64x64, ctor 2.5 (`wingspeed`). Keine GL-Aufrufe.
- **Animationen (ModelCricket.java:89-116):** Vorderbeine `rotateAngleY = Grundwinkel (0.47 / -0.54 / -0.296 / 0.384) ± cos(f2 * ws) * pi * 0.25 * f1` (nur bei `f1 > 0.1`). Sprungbeine: bei `getSinging() != 0` (DataWatcher 20, 40 Ticks lang nach jedem Zirpen, Cricket.java:44-49, 95) reiben sie: `rotateAngleY` eng an den Koerper (∓0.035 / ∓0.105) und `newangle = cos(f2 * 3 * ws) * pi * 0.25` auf `rotateAngleX` (`lleg1 = a + 0.558`, `lleg2 = a - 0.366`, rechts negiert); sonst abgespreizt (±0.436 / ±0.349) und still.
- **Varianten:** keine.
- **Klang:** `cricket` (Living, mit 50 % Chance unterdrueckt; Cricket.java:91-97, manifest).
- **Portierungshinweise Modell/Renderer:** `singing` als synced int; `scale()` 0.5. Textur: `textures/entity/crickettexture.png`.

---

### Cryolophosaurus (`cryolophosaurus`)

- **Aussehen:** Ein kleiner Theropode: hellgrauer Koerper mit cyanfarbenen Punkt-/Streifenmustern (`Shape1` 8x9x18 uv 0,0), Hals und Kopf (`Shape2/3/5`), weisser Unterkiefer (`jaw` 4x9x1, uv 0,30, um -72° herabhaengend), cyanfarbene Augen, kleiner Kamm (`Shape8/9/16/17`), langer Schwanz (`Shape6` 4x4x14), zwei Beine aus je vier Teilen (`leg` -16°, `leg2` 22°, `leg3` -39°, `leg4` Fuss). `cryolophosaurus.png` (128x128), grosse Teile leer. Recherche: „Red Ant Mining Dimension", Drops Huhn und Nuggets (01-mobs.md #71).
- **Groesse:** Hitbox 0.75 x 0.75 (Cryolophosaurus.java:21, manifest). Renderer `scale = 0.5` (RenderCryolophosaurus.java:35). Modell 28 px hoch (y -4..24), 57 px lang → **0.88 Bloecke** hoch, 1.8 lang.
- **Modell:** `ModelCryolophosaurus`, 20 Teile, Textur 128x128, ctor 0.75. Keine GL-Aufrufe.
- **Animationen (ModelCryolophosaurus.java:142-156):** Beine `newangle = cos(f2 * 1.3 * ws) * pi * 0.25 * f1` (nur bei `f1 > 0.1`), rechts `-0.279 + a / 0.384 + a / -0.68 + a / a`, links mit `-a`. Kiefer `jaw.rotateAngleX = -1.15 + cos(f2 * 0.28) * pi * 0.1` — dauerndes langsames Kauen, keine Angriffsabfrage.
- **Varianten:** keine.
- **Klang:** `cryo_living`, `cryo_hurt`, `cryo_death` (Cryolophosaurus.java:73-83, manifest).
- **Portierungshinweise Modell/Renderer:** Trivial; `scale()` 0.5. Textur: `textures/entity/cryolophosaurus.png`.

---

### Crystal Apple Cow (`crystal_apple_cow`, Klasse `CrystalCow`)

- **Aussehen:** Vanilla-Kuhmodell mit `crystal_cow.png` (64x32): eine sehr dunkle, fast schwarze Kuh, deren Fell in rot gluehenden Rissen aufgebrochen ist (Kristall-Effekt), auf beiden Flanken je ein grosser roter Apfel mit gruenem Blatt, zwei blassrosa Quadrate (Euter/Hufe-Bereich). Kopf mit weisser Schnauze und Hoernern wie bei der Vanilla-Kuh. Recherche: „Crystal Cow in the Crystal Dimension … drops crystal apples" (01-mobs.md #92-95).
- **Groesse:** manifest `size: null` — die Klasse setzt keine eigene Groesse, es gilt die von `EntityCow`: **0.9 x 1.3** (Vanilla 1.7.10, `client-1.7.10.jar` Klasse `wh` = `EntityCow` laut `joined.srg`; der Konstruktor ruft `func_70105_a(0.9f, 1.3f)` = `setSize`, per `javap -c` gelesen). Renderer `RenderEnchantedCow` ohne `scale`, Schatten 0.7 (manifest renderer_args) → Rendergroesse = unskaliertes Vanilla-`ModelCow`.
- **Modell:** `ModelCow` (Vanilla), Textur 64x32.
- **Animationen:** Vanilla-Quadruped (Beinschwung, Kopfblick). Nichts Eigenes.
- **Varianten:** `RenderEnchantedCow.getEntityTexture` (RenderEnchantedCow.java:42-50): `EnchantedCow` und `GoldCow` → `gold_cow.png`, `CrystalCow` → `crystal_cow.png`, sonst (`RedCow`) → `red_cow.png`. Fuer `EnchantedCow` gibt es zusaetzlich Pass 3 mit Rueckgabe 31 (Enchantment-Glint-Pass; RenderEnchantedCow.java:33-40) — betrifft die Crystal Cow **nicht**.
- **Klang:** keine OreSpawn-Sounds; Vanilla-Kuh (kein Eintrag in manifest sounds fuer CrystalCow/RedCow).
- **Portierungshinweise Modell/Renderer:** `CowModel` + `CowRenderer`-Ableitung mit `getTextureLocation` nach Klasse. Textur: `textures/entity/crystal_cow.png` (manifest texture_map).

---

### DeadIrukandji (`dead_irukandji`)

- **Aussehen:** Kein Modell. Ein Wurfgeschoss (`LaserBall`-Ableitung, `EntityThrowable`), gezeichnet als Billboard-Sprite aus `spinners.png` (256x256, 16x16-Raster): Index 86 (DeadIrukandji.java:10-40) = Spalte 6, Zeile 5 → ein blasser, weisser Vierarm-Stern mit rosa Zentrum, die tote Qualle. Zum Vergleich `LaserBall` Index 81 = rot gluehende Kugel.
- **Groesse:** manifest `size: null` (Vanilla-Throwable). `RenderSpinner` zeichnet ein 1x1-Quad bei `glScalef(0.5)` (RenderSpinner.java:25) → **0.5 Bloecke** Kantenlaenge.
- **Modell:** keines. `RenderItemUrchin` setzt nur `spinItemIconIndex` (RenderItemUrchin.java:37-38), `RenderSpinner.doRender` (RenderSpinner.java:20-50) bindet `spinners.png`, richtet das Quad zur Kamera aus (`glRotatef(180 - playerViewY, Y)`, `glRotatef(-playerViewX, X)`) und dreht es um Z mit `entity.rotationPitch` — daher „Spinner": das Geschoss rotiert in der Bildebene, sofern der Pitch laeuft.
- **Animationen:** nur diese Z-Rotation.
- **Varianten:** keine.
- **Klang:** Vanilla `random.explode` beim Aufschlag (LaserBall.java:171).
- **Portierungshinweise Modell/Renderer:** Ein eigener `EntityRenderer`, der ein kamerazugewandtes Quad mit UV `(6*16..7*16, 5*16..6*16) / 256` zeichnet — Vanilla-`ThrownItemRenderer` passt nicht, weil die Textur ein Atlas-Blatt und kein Item ist. Textur: `textures/entity/spinners.png`. Alternativ das Blatt in 256 Einzel-Item-Texturen zerlegen und `ItemDisplayContext`-basiert rendern; fuer einen 1:1-Port ist das Quad einfacher.

---

### Dragon (`dragon`)

- **Aussehen:** Ein vierbeiniger, zweifluegliger Drache mit langem Hals (neck1..3), Kopf mit zwei Hoernern (`horn1/2`, ±15° Y, 23° X), Ober- und Unterkiefer (`mouth1/2`), zehn Rueckenstacheln (`spike1..10`, auf Hals und Schwanz verteilt), sechsgliedrigem Schwanz (tail1..6), vier Beinen aus je drei Segmenten plus Fuessen, und zwei Fluegeln aus je einem Oberarmknochen (`wing1/4` 11x2x2), zwei Fingerknochen (`wing3/5` 24 px, `wing2/6` 36 px) und drei Membranplatten (`wing7-9` bzw. `wing10-12`, 1 px dick, bis 36x26 px). `Dragon.png` (256x128): schwarz-violett zerkratztes Fell, violette Augen, magentafarbene Zungen-/Maulpixel. `WhiteDragon.png`: dieselbe Aufteilung in weiss mit dunkelroten Maul-/Zungenflecken und schwarzen Augen. Recherche (01-mobs.md #76): Snowball → weiss, Coal → schwarz; deckt sich mit Dragon.java:1317-1337.
- **Groesse:** Hitbox 1.5 x 1.25 (Dragon.java:77, manifest). Renderer `scale = 1.0` (RenderDragon.java:36), Schatten 1.25. Modell unrotiert 40 px hoch (y -5..35 — Beine haengen in den Konstruktordaten unter die Bodenlinie, werden aber um ±32-51° angewinkelt), 64 px breit (Fluegel um ±60-72° Y gedreht, unrotierte Breite ist also nicht die Spannweite), 92 px lang → grob **2.5 Bloecke** hoch, 5.75 lang; Spannweite bei ausgebreiteten Fluegeln ≈ 2 * (11 + 24 + 36) px ≈ 142 px ≈ 8.9 Bloecke (Kettenlaenge der Knochen, Naeherung).
- **Modell:** `ModelDragon`, 55 Teile, Textur 256x128, ctor 0.65. Keine GL-Aufrufe, **keine Transluzenz** (anders als bei King/Queen/Prince sind die Membranen hier opak).
- **Animationen (ModelDragon.java:358-539):** Beine wie beim Cephadrome: am Boden `cos(f2 * 1.25 * ws) * pi * lspeed * 0.6` aus der XZ-Geschwindigkeit, im Flug (`getActivity() != 0`) starr angezogen mit `newangle = 1.0`. Fluegel-`rotateAngleZ`: fliegend `cos(f2 * 0.75 * ws) * pi * 0.28`; am Boden ohne Angriff `-0.85 + cos(f2 * 0.2 * ws) * pi * 0.028` (gefaltet), am Boden mit Angriff `-0.45 + cos(f2 * 0.85 * ws) * pi * 0.2`; Fingerknochen mit Faktor 4/3 und 3/2, Pivots per `sin/cos * 7/6` verkettet, Membranen folgen ihrem Knochen. Schwanz: `tailspeed/tailamp` 0.76/0.45, Angriff 0.96/0.75, Boden-Ruhe 0.22/0.22, sitzend 0/0; sechs Segmente um Y mit Faktoren 0.125..0.75, Pivots per `cos/sin * 6` verkettet, Stacheln `spike10/7/8/3/9` folgen. Hals: Kurvenlage im Flug aus `(prevRotationYaw - rotationYaw) * 8`, geglaettet /60 in `r.rf1` (Klemme ±50), sonst `f3 / 2`; Faktoren 0.25/0.5/0.75 auf neck2/neck3/head, Hoerner ±0.26 versetzt. Unterkiefer `mouth2.rotateAngleX = 0.4 + cos(f2 * 1.5 * ws) * pi * 0.14` bei Angriff, sonst 0.07.
- **Varianten:** `RenderDragon.getEntityTexture` (RenderDragon.java:43-49): `getDragonType() != 0` → `WhiteDragon.png`, sonst `Dragon.png`. `DragonType` DataWatcher 22 (Dragon.java:1442-1448), Start 0 (:72, :148), Snowball setzt 1, Coal setzt 0 (:1317-1337), NBT `DragonType`.
- **Klang:** `roar` (Living bei Angriff ohne Reiter, Dragon.java:293-294), `alo_hurt`, `alo_death` (:300-304), `mothrawings` alle 20 Ticks im Flug (:596-600); Vanilla `splash`, `random.bow`, `random.fuse`, `fireworks.launch` fuer Feuerbaelle.
- **Portierungshinweise Modell/Renderer:** `RenderInfo.rf1` client-only, `getActivity()` (DataWatcher 21), `getAttacking()` (20), `isSitting`, Positions-Delta. `getTextureLocation` nach synced `dragonType`. Texturen: `textures/entity/dragon.png`, `textures/entity/whitedragon.png`.

---

### Dragonfly (`dragonfly`)

- **Aussehen:** Eine grau-weisse Libelle: Kopf (`Shape1` 5x4x7) mit gruen-cyanen Augenpixeln, Thorax (`Shape3`), zwei Kieferzangen (`ljaw/rjaw`, ±10° Y, 25° X), zweigliedriges Abdomen (`tail1` 3x3x7, `tail2` 1x2x9), vier lange weisse Fluegelplatten (`lfwing/rfwing/lrwing/rrwing` 10x1x3, um ±28°/±22° Y gefaechert), sechs Beine aus je zwei Teilen (`Shape10-15`, um ±11-20° Z gespreizt) und weitere Fuehler/Beinstummel (`Shape16-23`) mit cyan-gruenen Spitzen. `dragonfly.png` (64x64), grau-weiss mit gruen-cyanen Akzenten. Recherche: „eats ants, mosquitoes, butterflies and birds; attacks horses" (01-mobs.md #103).
- **Groesse:** Hitbox 1.5 x 0.5 (Dragonfly.java:21, manifest). Renderer `scale = 1.5` (RenderDragonfly.java:35). Modell 8 px hoch, 25 px breit, 29 px lang → **0.75 Bloecke** hoch, 2.3 breit, 2.7 lang.
- **Modell:** `ModelDragonfly`, 26 Teile, Textur 64x64, ctor 2.0. Keine GL-Aufrufe.
- **Animationen (ModelDragonfly.java:178-185):** `newangle = cos(f2 * 1.3 * ws) * pi * 0.25`; `lfwing.rotateAngleZ = a`, `rfwing = -a`, `lrwing = a + 3.14`, `rrwing = -a + 3.14` (Hinterfluegel um 180° gedreht gezeichnet). Kiefer `ljaw/rjaw.rotateAngleX = ±cos(f2 * 0.3 * ws) * pi * 0.1`. Nur zeitgesteuert.
- **Varianten:** keine.
- **Klang:** `dragonfly_living`, `dragonfly_hurt`, `dragonfly_death` (Dragonfly.java:54-62, manifest).
- **Portierungshinweise Modell/Renderer:** Trivial; `scale()` 1.5. Textur: `textures/entity/dragonfly.png`.

---

### Dungeon Beast (`dungeon_beast`)

- **Aussehen:** Ein gehoernter, vierbeiniger Daemon mit langem Stachelschwanz, in schwarz-violettem, zerkratztem Fell (`Botwtexture.png` 128x64 — fast alle 64 Teile teilen uv 0,0, die grosse dunkle Flaeche), magentafarbenen Augen (`leye/reye`, uv 14,15 / 5,15), zwei geschwungenen Hoernern aus je vier Segmenten (`lh1..4/rh1..4`, -30° bis -100° Z) plus `horn1/2`, dreiteiligen Kiefern je Seite (`ljaw1..3/rjaw1..3`, ±20-30° Y), Schultern, zwei Beinen aus je zwei Segmenten mit Ferse, Fuss und drei gespreizten Zehen, drei Rueckenstacheln (`bodys1..3`) und sieben Schwanzsegmenten (`tail1..7`) mit zwoelf Stacheln (`t1s1..t6s1`). Die Konstruktordaten sind um Z gedreht (Schwanz 145-180°, Beine -90°), das ganze Modell wird in `render()` zusaetzlich um 90° um Y gedreht. Recherche: Crystal Dimension, „guards the endless dungeon maze" (01-mobs.md #55).
- **Groesse:** Hitbox 1.15 x 1.1 (DungeonBeast.java:26, manifest). Renderer `scale = 1.0` (RenderDungeonBeast.java:35), Schatten 0.25. Modell unrotiert 18.3 px hoch (y 8..26.3), 39.5 px in X, 23.2 px in Z; nach der 90°-Y-Drehung wird X zur Laenge → **1.15 Bloecke** hoch, ≈ 2.5 Bloecke lang, 1.45 breit.
- **Modell:** `ModelDungeonBeast`, 64 Teile, Textur 128x64, ctor 0.62. **GL im Modell:** `GL11.glRotatef(90, 0, 1, 0)` vor allen `render`-Aufrufen (ModelDungeonBeast.java:560; 06-models-design.md GL-Tabelle).
- **Animationen (ModelDungeonBeast.java:413-559):** Beine um **Z** (wegen der gedrehten Aufstellung): `newangle = cos(f2 * 1.4 * ws) * pi * 0.22 * f1` auf rleg1/rleg2/rfoot/rfoot2/rheel, links negiert, Mittelzehe `-0.785 ± a`. Rueckenstacheln und Schwanzstacheln wellen um X mit `cos(f2 * 0.5 * ws + k * pi/8) * pi * 0.07`, k = 0..13, ab t2s1 negiert. Schwanz: `tailamp = f1` (Ruhe) bzw. 1.25 (Angriff), `newangle = cos(f2 * 0.75 * ws) * pi * 0.25 * tailamp`, sieben Segmente um Y mit Faktoren 0.25..1.75, Pivots per `cos/sin * 6/5/4.5/4/3/3` verkettet, Stacheln folgen ihrem Segment. Kiefer: `RenderInfo` — an jedem Nulldurchgang von `cos(f2 * 2 * ws)` werden `ri1/ri2 = rand(15)` gewuerfelt (bei Angriff 0); nur bei `ri1 == 0` schnappen die sechs Kieferteile mit `±newangle` (0.15 pi), sonst Grundstellung ∓0.349/±0.349/±0.523.
- **Varianten:** keine.
- **Klang:** `dbhit` (3), `dbdead` (DungeonBeast.java:108-112, manifest).
- **Portierungshinweise Modell/Renderer:** Die 90°-Y-Drehung in `renderToBuffer` als `poseStack.mulPose(Axis.YP.rotationDegrees(90))` vor dem Root-Render, oder in einen Root-Part mit `PartPose.rotation(0, pi/2, 0)` backen (06-models-design.md, „Whole-model transforms"). `RenderInfo` client-only. Textur: `textures/entity/botwtexture.png`.

---

### EasterBunny (`easter_bunny`)

- **Aussehen:** Ein weisser Hase in Sitzhaltung: Rumpf (`body` 6x6x7), aufgerichteter Oberkoerper (`upperbody`), Kopf (`head` 5x4x5) mit schwarzen Augen und rosa Nase (`nose`, uv 44,9), zwei 10 px lange Ohren mit magentafarbener Innenseite (`lear`/`rear`, uv 54,0 / 32,0, -13° / -24° geneigt), weisser Puschelschwanz (`tail` 4x4x4), zwei Vorderpfoten, zwei Hinterbeine mit 7 px langen Fuessen. `EasterBunnytexture.png` (64x128), obere Haelfte weiss-graues Fellrauschen, untere Haelfte leer. Recherche: spawnt nur an Ostern, „hops along dropping random spawn eggs" (01-mobs.md #96).
- **Groesse:** Hitbox 0.5 x 0.75 (EasterBunny.java:20, manifest). Renderer `scale = 1.0`, Kinder 0.5 (RenderEasterBunny.java:35-39). Modell 21 px hoch (y 3..24, Ohrenspitzen) → **1.3 Bloecke**, 10 px breit, 13 px lang.
- **Modell:** `ModelEasterBunny`, 13 Teile, Textur 64x128, ctor 0.55. Keine GL-Aufrufe.
- **Animationen (ModelEasterBunny.java:102-121):** Beine/Fuesse `newangle = cos(f2 * 2.6 * ws) * pi * 0.15 * f1` (nur `f1 > 0.1`), links/rechts gegenphasig. Ohren `newangle2 = cos(f2 * 1.3 * ws) * pi * 0.1 * f1` beim Laufen, sonst `* 0.01` (leichtes Idle-Zucken): `lear = -0.226 + a2`, `rear = -0.418 - a2`.
- **Varianten:** keine.
- **Klang:** `duck_hurt` fuer Hurt und Death (EasterBunny.java:81-85, manifest).
- **Portierungshinweise Modell/Renderer:** Trivial; `scale()` fuer Babys. Textur: `textures/entity/easterbunnytexture.png`.

---

### Hoverboard (`hoverboard`, Klasse `Elevator`)

- **Aussehen:** Ein flaches Brett: Hauptplatte `Shape1` 8x1x16 px (uv 0,0), vorne zwei kleine Stossfaenger (`Shape2` 6x1x1 bei z = -9, `Shape3` 2x1x1 bei z = -10, uv 0,18 / 0,21 — rot/orange), hinten zwei ebensolche (`Shape4`/`Shape5`, uv 17,18 / 17,21 — blau) (ModelElevator.java:19-40). Zehn Farbtexturen `Elevator1..10.png` (64x64): `Elevator1` ist ein Regenbogen-Batikmuster, `Elevator5` blau mit hellen Wolkenflecken; alle teilen die roten Front- und blauen Heckstreifen. Recherche 06-models-design.md zaehlt die 10 PNGs.
- **Groesse:** Hitbox 1.25 x 1.0 (Elevator.java:49, manifest). `RenderElevator` (kein `RenderLiving`): `glScalef(0.75)` sofort gefolgt von `glScalef(1/0.75)` — netto 1.0 (RenderElevator.java:33-34) —, dann `glScalef(-1, -1, 1)` und `model.render(..., 0.0625)` (:36-37) → Brett **0.5 x 0.06 x 1.0 Bloecke**, Schatten 0.25 (:16).
- **Modell:** `ModelElevator`, 5 Teile, Textur 64x64, im Renderer selbst instanziert (RenderElevator.java:17; manifest ctor_sites). Keine Animation in `render()` (ModelElevator.java:46-54).
- **Animationen (RenderElevator.java:22-31):** `glRotatef(180 - yaw, Y)`; bei Treffern Boot-Wobble: `f2 = getTimeSinceHit() - partial`, `f3 = max(0, getDamageTaken() - partial)`, `glRotatef(sin(f2) * f2 * f3 / 10 * getForwardDirection(), X)` — exakt das Vanilla-Boot-Rezept.
- **Varianten:** `Elevator.getTexture()` (Elevator.java:64-95) nach `getColor()` (DataWatcher 21, Elevator.java:615-617) 1..10 → `Elevator<n>.png`; Start 1 (:47), NBT `HoverColor` auf 1..10 geklemmt (:540-547), Rechtsklick mit `MyUltimateSword` zaehlt zyklisch weiter (:560-566).
- **Klang:** `hover` (6 Dateien, Elevator.java:300), Vanilla `random.explode` (:318).
- **Portierungshinweise Modell/Renderer:** Nach dem Muster von `BoatRenderer`: `EntityRenderer<Elevator>` mit eigenem `LayerDefinition`, Y-Flip per `poseStack.scale(-1, -1, 1)` (wie `LivingEntityRenderer` es tut) und `translate(0, -0.1 * ?, 0)`-Aequivalent fuer das `f2 = -0.1` — offen: `-0.1` wird als drittes Float (`ageInTicks`-Slot) an `render()` gereicht und dort ignoriert, hat also keine sichtbare Wirkung. Hit-Wobble ueber synced `timeSinceHit`/`damageTaken`/`forwardDirection` wie beim Vanilla-Boot. Texturen: `textures/entity/elevator1.png` … `elevator10.png`.

---

## Offene Punkte dieser Charge

- Crystal Apple Cow: Hitbox 0.9 x 1.3 stammt aus dem Vanilla-`EntityCow`-Bytecode (siehe Eintrag), nicht aus OreSpawn-Code; die Blockhoehe des Vanilla-`ModelCow` ist hier nicht nachgerechnet.
- Dragon/Cephadrome/CaterKiller: die Blockmasse sind aus unrotierten Boxen bzw. Pivot-Ketten genaehert; eine Messung in einer laufenden Instanz steht aus (kein Client in dieser Umgebung).
- Hoverboard: Wirkung des `-0.1` im dritten `render()`-Parameter nicht belegt (das Modell liest ihn nicht).
- RubyBird nutzt laut 06-models-design.md dasselbe `ModelCockateil`; die Textur dieses Vogels liegt in einer anderen Charge.
