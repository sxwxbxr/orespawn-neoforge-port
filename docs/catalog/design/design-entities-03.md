# Design: design-entities-03

Stand 10.09.2026. Quellen in Prioritaet: `docs/catalog/manifest.json` (manifest), dekompilierte Quelle `reference/src-20.2/src/main/java/danger/orespawn/<Class>.java` (Class.java:LINE), `reference/jar/models/*.json` und `reference/jar/anim/*.json`, Texturen unter `reference/jar/extracted/assets/orespawn/`, zuletzt `docs/research/01-mobs.md` und `06-models-design.md`.

Pruefstand 10.09.2026: alle Zeilenangaben gegen die Quelle nachgeschlagen, alle Alpha-Prozentwerte per PIL ueber die uv-Rechtecke der Modell-JSON nachgerechnet, Sound- und Texturpfade gegen `manifest.sounds[].referenced_by` und `manifest.texture_map` geprueft. Bei den Groessen steht "unrotiert" fuer die Boxausdehnung aus pivot + origin + size ohne Ruhe-Rotation (reproduzierbar); die "grob"-Werte daneben schaetzen die gedrehte Pose und sind nicht exakt.

Konventionen: Modellmasse in Pixeln (1 px = 1/16 Block, Modellboden bei y = 24). Die Render-Argumente `(model, a, b)` aus der Registrierung bedeuten bei allen `RenderLiving`-Ablegern dieses Batches: Schattenradius = a * b, `this.scale = b`, und `preRenderScale` ruft `glScalef(scale, scale, scale)` (z. B. RenderEmperorScorpion.java:15-20, :35). In `render(entity, f, f1, f2, f3, f4, f5)` ist `f` = limbSwing, `f1` = limbSwingAmount, `f2` = ageInTicks, `f3` = netHeadYaw, `f4` = headPitch, `f5` = Skalierung (Vanilla-`ModelBase`-Signatur 1.7.10). "Hoehe ~ X Blocks" ist aus den Box-Ausdehnungen des Modell-JSON (mit Ruhe-Rotation, ohne Animation) mal Renderer-Skalierung gerechnet; das ist eine Naeherung, kein exakter Wert.

**Bildsprache dieses Batches.** Drei Familien: (1) Winzlinge und Ambient-Tiere mit 64x32-Texturen und wenigen Boxen, die fast nur aus Fluegel- oder Beinschlag bestehen (Ameisen, Schmetterling, Motte, Muecke, Gluehwuermchen, Flunder, Frosch); (2) zwei Geister, die als 25 % transparente Graufiguren durch Waende schweben, und zwei Ender-Silhouetten in reinem Schwarz mit violetten Akzenten (Knight, Reaper); (3) drei Grossmodelle - der schwarze Emperor Scorpion (78 Boxen), der zehn Blocks hohe Roboter Jeffery und der blaue, halbdurchsichtige "WTF?"-Metroid. Dazu die Menschfiguren Girlfriend (Vanilla-Biped mit 62 Skins) und die verzauberte Kuh (Vanilla-Kuhmodell mit Glint). Alle Modelle sind flache Listen von Wurzel-`ModelRenderer`n ohne Hierarchie (`max_depth 0` im Modell-JSON); Bewegung entsteht durch Winkel und, bei Emperor Scorpion, Frosch, Metroid und Jeffery, durch nachgerechnete `rotationPoint`-Ketten.

---

### Emperor Scorpion (`emperor_scorpion`)

- **Klasse/Renderer/Modell:** `EmperorScorpion` (EntityMob) -> `RenderEmperorScorpion(model, 0.95, 1.5)` -> `ModelEmperorScorpion(0.22)` (manifest). Textur `emperorscorpion.png` 256x128 -> `assets/orespawn/textures/entity/emperorscorpion.png` (manifest texture_map; RenderEmperorScorpion.java:47).
- **Aussehen:** Ein fast komplett schwarzer Skorpion mit dunkelgrauem Kettenpanzer-Muster; einzige Farbpunkte sind zwei winzige violette Augen (Texturbereich `Lefteye`/`Righteye` uv 0,113, 3x2x3 px) und olivgruene Stinger-Segmente (uv 79-83,0-5). Silhouette: breiter, flacher Koerper aus Kopf (18x8x16) plus acht Rumpfsegmenten (`Seg1..Seg8`, schmaler werdend 18 -> 11 px breit), acht fuenfgliedrige Beine, die weit seitlich abgewinkelt aufstehen (Leg*Seg2 um -53 deg Z, Seg3 um +36 deg), zwei lange Scherenarme mit je vier Segmenten und Pincer (LeftArmSeg3 24 px lang) und ein achtgliedriger Schwanz, der in einem hohen Bogen ueber den Ruecken nach vorne kippt (`Tailseg1` 34 deg bis `Tailseg6` -149 deg, Stinger3 -49 deg). Der Wiki-Text "site mascot" (01-mobs.md #19) passt: das Tier ist als grosse dunkle Silhouette gedacht.
- **Groesse:** Hitbox 3.5 x 3.0 (EmperorScorpion.java:31). Boxen unrotiert: y -19..20.5, x -34..34, z -57..43 -> 40 px hoch, 68 breit, 100 lang (Modell-JSON, pivot + origin + size ueber alle 78 Boxen); der aufgerollte Schwanz (Tailseg1 34 deg bis Tailseg6 -149 deg) und die abgewinkelten Beine machen das Tier in Ruhe kuerzer und hoeher, grob 44 px hoch / 90 px lang. Mal 1.5 -> ca. 4 Blocks hoch, 6.4 breit, 8.5-9.4 lang. Schatten 0.95 * 1.5 = 1.43.
- **Modell:** 78 Parts, 78 Boxen, alle Wurzeln, Textur 256x128, keine versteckten Parts, groesste Box 24 (Modell-JSON). Keine GL-Aufrufe im Modell.
- **Animationen (ModelEmperorScorpion.java:486-746):**
  - Beine: vier Phasengruppen je ein linkes und ein rechtes Bein (Leg1/5, Leg2/6, Leg3/7, Leg4/8) mit `cos(f2 * 2 * wingspeed - k * pi/2) * pi * 0.12 * f1`, `wingspeed` = 0.22 aus dem Konstruktor (ModelEmperorScorpion.java:91). `doLeftLeg`/`doRightLeg` (:658-692) drehen Seg2..Seg5 um Y und rechnen die Gelenkpunkte nach (`sin(angle) * 6 / 9 / 1`); ein `upangle` = `0.47 * f1 - |angle|` hebt das Bein nur in der Vorschwung-Haelfte (`nextangle > newangle`, :498-500). Nur bei Bewegung (Amplitude mal f1).
  - Mandibeln `LeftManPart2`/`RightManPart2`: Z = `cos(f2 * 0.5 * ws) * pi * 0.05` in Ruhe, `cos(f2 * 2.5 * ws) * pi * 0.15` bei `getAttacking() != 0` (:527-534).
  - Scheren und Schwanz: bei jedem Nulldurchgang von `cos(f2 * 3 * ws)` werden in `RenderInfo` (`e.getRenderInfo()`, :535) zwei Zufallszahlen gezogen: Ruhe `ri1 = rand(20)`, `ri2 = rand(25)`; Angriff `rand(4)` / `rand(3)` (:538-548). `ri1` 1 oder 3 -> linke Schere schnappt, 2 oder 3 -> rechte (`doLeftClaw/doRightClaw`, :694-712: ArmSeg1 um Y, Pincer gegenlaeufig). `ri2 == 1` -> Schwanzstoss `doTail` (:714-746): acht Schwanzsegmente plus drei Stinger-Segmente als Kette, jedes Segment setzt seinen `rotationPoint` aus `sin/cos` des Vorgaengers (Segmentlaengen 9, 10, 10, 10, 10, 10, 10, 3, 3 px).
  - `f2 + 0.1` wird als "naechster Frame" benutzt, um die Bewegungsrichtung zu erkennen - ein Muster, das auch Metroid und die Beinhelfer benutzen.
- **Varianten:** keine.
- **Klang:** `scorpion_living`, `alo_hurt`, `emperorscorpion_death`, `scorpion_attack` (manifest sounds referenced_by).
- **Stats fuer die Portierung:** Leben 350, Angriff 35, Ruestung 20 (manifest, `EmperorScorpion_stats`); alles unter den 1.21.1-Klemmen (1024 / 30).
- **Portierungshinweise:** `LayerDefinition` direkt aus dem JSON. Das Zufalls-Rendering (`RenderInfo` am Entity, per `setRenderInfo` zurueckgeschrieben, ModelEmperorScorpion.java:567) ist clientseitiger Zustand, der im Original im Entity-Objekt lebt; in 1.21.1 in ein client-only Feld am Entity oder eine `Map<Integer, RenderInfo>` im Renderer unter `com.swbr.orespawn.client`. `getAttacking()` muss ueber `SynchedEntityData` synchronisiert sein. `scale()` = 1.5.

### Enchanted Golden Apple Cow (`enchanted_golden_apple_cow`)

- **Klasse/Renderer/Modell:** `EnchantedCow extends RedCow extends EntityCow` (EnchantedCow.java:10, RedCow.java:8) -> `RenderEnchantedCow(ModelCow, 0.7)` (manifest). Vanilla `ModelCow`, Textur 64x32.
- **Aussehen:** Vanilla-Kuh mit der `gold_cow.png`-Haut: goldgelbe bis orange "Aepfel"-Flecken mit gruenen Blattraendern auf schwarzem Grund, grau-weisses Gesicht, rosa Euter - dieselbe Textur wie die Golden Apple Cow (RenderEnchantedCow.java:43-48 liefert fuer `EnchantedCow` und `GoldCow` beide `texture2` = `gold_cow.png`, :58). Unterschied zur Golden Apple Cow ist nur der Render-Durchgang: `shouldRenderPass` gibt fuer `EnchantedCow` bei Pass 3 den Wert 31 zurueck und setzt das Kuhmodell als Pass-Modell (RenderEnchantedCow.java:33-40). 31 = 15 | 16 ist derselbe Rueckgabewert, mit dem Vanilla-`RenderBiped` verzauberte Ruestung markiert - der Verzauberungs-Glint (scrollende Glint-Textur, additiv) ueber dem ganzen Koerper. Deutung aus der Vanilla-Konvention, nicht aus OreSpawn-Code.
- **Groesse:** `size` im Manifest null; weder RedCow noch EnchantedCow rufen `setSize` (grep in RedCow.java/EnchantedCow.java) -> Vanilla-`EntityCow`-Hitbox. Kein Scale-Override. Schatten 0.7.
- **Modell:** Vanilla `ModelCow` (Quadruped + Hoerner + Euter), 64x32 -> in 1.21.1 `CowModel` mit `ModelLayers.COW`.
- **Animationen:** Vanilla-Vierbeiner (Beinschwung, Kopf folgt Blick).
- **Varianten:** Texturwahl nach Klasse (RenderEnchantedCow.java:42-53): `EnchantedCow`/`GoldCow` -> `gold_cow.png`, `CrystalCow` -> `crystal_cow.png` (schwarz mit roten Kristall-Aepfeln), sonst `red_cow.png` (rote Apfelflecken). Neue Pfade `assets/orespawn/textures/entity/{gold_cow,crystal_cow,red_cow}.png` (manifest texture_map).
- **Klang:** keine eigenen (manifest); Vanilla-Kuh.
- **Spawn:** forest 3, plains 3, megaTaiga 5, mushroomIsland 15, Gruppen 2-6, Typ creature, Schalter `CowEnable` (manifest).
- **Portierungshinweise:** `CowRenderer`-Ableger mit eigener `getTextureLocation`; der Glint als `RenderLayer`, der das Kuhmodell noch einmal mit `RenderType.entityGlint()`/`armorEntityGlint()` zeichnet (so wie `ItemRenderer` den Glint auf Ruestung legt). Kein `scale()`.

### Ender Knight (`ender_knight`)

- **Klasse/Renderer/Modell:** `EnderKnight` (EntityMob) -> `RenderEnderKnight(model, 0.3, 1.0)` -> `ModelEnderKnight(0.21)` (manifest). Textur `EnderKnighttexture.png` 512x512 -> `assets/orespawn/textures/entity/enderknighttexture.png` (manifest; RenderEnderKnight.java:47).
- **Aussehen:** Ein Skelett-Enderman in Ruestung, rein schwarz: duerre Beine aus je drei 1-px-Staeben (`rleg1..3`, 14-15 px lang), ein Becken (3x3x3), Wirbelsaeule aus drei Segmenten, vier Rippen, ein 7x6x6-Kopf mit violetten Augen (Textur uv 34,106: violette Pixelreihe), Schulterplatten 5x4x4, ein Umhang `cape2` als flache 9x24-Platte hinter dem Ruecken (Textur mit 22 % Transparenz - ausgefranster Saum) und ein Schwert: `blade` 1x32x6 px mit violetten Runen-Kanten (Textur uv 36,304) am `handle`. Fuesse mit vier Boxen inklusive nach vorne gekippter Zehenplatte (`rfoot2`, 34 deg). Grundhaltung der Arme: 57 deg nach hinten/aussen (Modell-JSON `rarm1` rot[-57.3, -57.3, 0]).
- **Groesse:** Hitbox 0.6 x 2.9 (EnderKnight.java:26). Modell: Kopfoberkante bei y = -26 (head piv -18, box -8) -> 50 px = ca. 3.1 Blocks; Boxen unrotiert bis y = -36 (Schwert) -> 60 px = 3.75 Blocks (Modell-JSON). Scale 1.0, Schatten 0.3.
- **Modell:** 40 Parts, 40 Boxen, Textur 512x512 (nur die linke Spalte bis x = 100, y = 350 belegt), groesste Box 32 (Modell-JSON). Keine GL-Aufrufe.
- **Animationen (ModelEnderKnight.java:258-378):**
  - Beine/Fuesse: `cos(f2 * 1.3 * ws) * pi * 0.25 * f1` nur bei `f1 > 0.1`, sonst 0; links und rechts gegenlaeufig; `lfoot2`/`rfoot2` mit +0.6 Offset, `lleg2/3` mit -0.1 (:263-282). `ws` = 0.21.
  - Umhang: `cape2.rotateAngleZ = leg / 4`, `cape2.rotateAngleX = cos(f2 * 0.7 * ws) * pi * 0.02` (Flattern, :283-285).
  - Kopf: Y = `toRadians(f3) * 0.45`, geklemmt auf +-0.45 rad (:286-292).
  - `isScreaming()` (EnderKnight.java:270-272, DataWatcher-Slot 18): Arme hochgerissen (larm/rarm2/3 = -1.2 + swing, arm1 = -1.8 + swing mit `swing = cos(f2 * 2.7 * ws) * pi * 0.3`), Schwert `blade/handle.rotateAngleX = 0.5 + 1.5 * swing` (:293-306). Ruhe: Arme -0.5 / arm1 (-1.0, +-1.0 Y), Schwert 0.35 (:307-323).
  - Schwertposition folgt dem Ende von `rarm1` (7 px), `arm1` dem Ende von `arm2` (10 px) - nachgerechnete Gelenkkette (:324-337).
- **Varianten:** keine.
- **Klang:** Vanilla `mob.endermen.scream` / `mob.endermen.idle` je nach `isScreaming()` (EnderKnight.java:204); keine eigenen OreSpawn-Events (manifest).
- **Stats:** Leben 60, Angriff 12, Ruestung 6 (manifest `EnderKnight_stats`); Spawn breit ueber Overworld-Biome, roofedForest Gewicht 20 (manifest).
- **Portierungshinweise:** `LayerDefinition`; `cape2` ist eine Box mit Tiefe 0 (Modell-JSON `s[9,24,0]`) und braucht wegen der Textur-Transparenz `RenderType.entityCutoutNoCull`. `isScreaming` als `EntityDataAccessor<Boolean>`. Sound-Events auf `SoundEvents.ENDERMAN_SCREAM/AMBIENT`.

### Ender Reaper (`ender_reaper`)

- **Klasse/Renderer/Modell:** `EnderReaper` (EntityMob) -> `RenderEnderReaper(model, 0.2, 1.0)` -> `ModelEnderReaper(0.23)` (manifest). Textur `EnderReapertexture.png` 512x512 -> `assets/orespawn/textures/entity/enderreapertexture.png` (manifest; RenderEnderReaper.java:47).
- **Aussehen:** Ein schwarzes Skelett ohne Beine, das ueber dem Boden haengt: Rumpf und Rippen aus 46 kleinen 1-px-Boxen (`Shape7..Shape48`, je 4x1x1 oder 1x1x5/6), ein Kopf 6x6x5 mit violetten Augen (Textur uv 58,145), zwei Arme mit Ellbogenboxen, in der linken Hand eine Sense: `scythe1` 39 px langer Stiel, `scythe2` 16x6-Klinge und `scythe3` 7x5 Klingenspitze, beide Klingen als Boxen mit Tiefe 0 - die Textur zeigt eine hellgraue Klinge und einen violetten Schimmer (Textur uv 57-61, 70-133; violette Flaeche im 512er-Bild bei y ~ 350). Zwei grosse schwarze Fluegel `lwing1`/`rwing1` (0x50x17 px, flach, Textur mit 44 % Transparenz -> zerfetzte Federkanten, sichtbar im 512er-Bild unten) plus je zwei Fluegelstreben `wing2/3` (1x19x3), um 100 deg X und +-45 deg Y nach hinten-oben gestellt.
- **Groesse:** Hitbox 0.7 x 2.9 (EnderReaper.java:26). Modell: Kopfoberkante y = -22 (head piv -16, box -6) -> 46 px = ca. 2.9 Blocks bis Kopf; Rumpf endet bei ca. y = 7, das Modell schwebt also ca. 1 Block ueber dem Hitbox-Boden; die 50-px-Fluegel liegen unrotiert y bis 43 / z bis 25.5, um 100 deg X gedreht reichen sie grob 50 px nach hinten-oben (Modell-JSON). Scale 1.0, Schatten 0.2.
- **Modell:** 66 Parts, 66 Boxen, Textur 512x512 (belegt links bis x ~ 100), groesste Box 50 (Modell-JSON). Keine GL-Aufrufe.
- **Animationen (ModelEnderReaper.java:414-537):**
  - Ein Beinschwung `cos(f2 * 1.3 * ws) * pi * 0.25 * f1` wird berechnet (:419-424), obwohl es keine Beine gibt; er steuert nur die Sense: `scythe*.rotateAngleZ = 1.0 - |swing|` (:425-431) - beim Gehen senkt sich die Sense. `ws` = 0.23.
  - `isScreaming()` (EnderReaper.java:278): Sense schwingt `1.0 + cos(f2 * 1.9 * ws) * pi * 0.25`, linker Arm auf (-0.436 X, -0.488 Y), Fluegel schlagen mit `cos(f2 * 2.7 * ws) * pi * 0.3` (:432-444). Ruhe: linker Arm -2.436 X / 1.0 Y, Fluegel atmen mit `cos(f2 * 0.7 * ws) * pi * 0.06` (:445-449).
  - Fluegel Y = +-(0.785 + swing) (:450-463). Kopf Y = 0.45 * netHeadYaw, geklemmt +-0.45 (:464-470).
- **Varianten:** keine.
- **Klang:** Vanilla `mob.endermen.scream` / `mob.endermen.idle` (EnderReaper.java:204); keine eigenen Events (manifest).
- **Stats:** Leben 90, Angriff 18, Ruestung 8 (manifest `EnderReaper_stats`); roofedForest Gewicht 38 (manifest).
- **Portierungshinweise:** Fluegel und Klingen sind Nulltiefen-Boxen mit transparenter Textur -> `entityCutoutNoCull`. Schwebender Koerper: die Hitbox 2.9 hoch bleibt, das Modell haengt ohne Verschiebung im Modellraum - nichts am Renderer verschieben. `isScreaming` synchronisieren.

### Ant (`ant`)

- **Klasse/Renderer/Modell:** `EntityAnt` (EntityAnimal) -> `RenderAnt(model, 0.1, 0.25)` -> `ModelAnt()` (manifest). Textur ueber `EntityAnt.getTexture()` (RenderAnt.java:41-44): `ant.png` 64x32 -> `assets/orespawn/textures/entity/ant.png` (EntityAnt.java:144; manifest texture_map).
- **Aussehen:** Die braune Ameise: Kopf, Thorax und Abdomen in drei Brauntoenen (dunkel, mittel, rotbraun), Beine und Kiefer schwarz (Texturblatt `ant.png`). Drei Koerperboxen (`head` 3x3x3, `thorax` 3x3x3, `abdomen` 3x3x5) plus drei 1-px-Verbindungsknoten (`thorax1/3`, `abdomen1`), zwei Kieferstaebe `jawsl/r` 1x1x3 vor dem Kopf und sechs zweigliedrige Beine (`llegtop/llegbot`, 3x1x1), seitlich um 22 deg / 61 deg abgeknickt.
- **Groesse:** Hitbox 0.1 x 0.1 (EntityAnt.java:25). Modell 7.4 px hoch, 10.4 breit, 16 lang (Modell-JSON) mal 0.25 -> ca. 0.12 Blocks hoch, 0.25 lang. Schatten 0.1 * 0.25 = 0.025.
- **Modell:** 20 Parts, 20 Boxen, Textur 64x32, groesste Box 5 (Modell-JSON). Geteilt mit Red/Rainbow/Unstable Ant und Termite.
- **Animationen (ModelAnt.java:135-172):** Beine X = `cos(f2 * 2.7) * pi * 0.45 * f1` (Amplitude nur bei Bewegung), Tripod-Gang: llegtop1/llegbot1, rleg2, rleg3 in Phase, rleg1, lleg2, lleg3 gegenphasig (:138-149). Kiefer `jawsl.rotateAngleY = cos(f2 * 0.4) * pi * 0.05`, `jawsr` gespiegelt - dauerndes leichtes Kauen auch im Stand (:150-151).
- **Varianten:** Texturwahl nach Klasse in `EntityAnt.getTexture` (EntityAnt.java:40-54): RedAnt -> `red_ant.png`, RainbowAnt -> `rainbow_ant.png`, UnstableAnt -> `unstableant.png`, Termite -> `termite.png`, sonst `ant.png` (:144-148).
- **Klang:** keine (manifest).
- **Spawn/Config:** keine Biom-Spawns im Manifest (kommen aus Ameisennestern, 01-mobs.md #97); Schalter `BlackAntEnable` (manifest config).
- **Portierungshinweise:** Ein `AntModel` fuer alle vier Ameisen-IDs, `AntRenderer` mit `scale()` = 0.25 und `getTextureLocation` je Entity-Typ. `Attributes.MOVEMENT_SPEED` 0.15, Leben 1 (manifest).

### Butterfly (`butterfly`)

- **Klasse/Renderer/Modell:** `EntityButterfly` (EntityAmbientCreature) -> `RenderButterfly(model, 0.3, 1.0)` -> `ModelButterfly(1.0)` (manifest). Texturen ueber `EntityButterfly.getTexture()` (RenderButterfly.java:83-86).
- **Aussehen:** Vier Fluegelpaare aus flachen 1-px-Platten (`leftwing` 5x1x5 vorn innen, `leftwing2` 6x1x7 vorn aussen, `leftwing3` 5x1x5 hinten, `leftwing4` 1x1x7 als Schwanzfaden), dazu ein 1x1x8-`body` und ein 1x1x1-`head`. Typ 0 `butterfly.png`: Vorderfluegel hellblau/blau, Hinterfluegel gruen, Koerper rot-gelb, Rosa/Rot-Akzent (Texturblatt). Typ 1 `butterfly2.png`: gelb mit gruen-roten Punkten, gruen-weisser Koerper. Typ 2 `butterfly3.png`: rot-gruen-blau gestreift ("Regenbogen"). Typ 3 `butterfly4.png`: schwarz-weiss kariert, grauer Koerper. `vbutterfly1.png` (Vampir): graubraune Fluegel mit orangen Punkten und Zickzack-Kante, roter Kopf.
- **Groesse:** Hitbox 0.4 x 0.4 (EntityButterfly.java:41). Modell 19 px lang, 15 breit, 1 px dick (Modell-JSON) bei Scale 1.0 -> ca. 1.2 x 0.9 Blocks flach; Fluegel richten sich durch die Z-Rotation bis +-45 deg auf. Schatten 0.3.
- **Modell:** 10 Parts, 10 Boxen, Textur 64x32, groesste Box 8 (Modell-JSON). Geteilt mit `EntityLunaMoth` (0.75) und `Mothra` (0.2).
- **Animationen (ModelButterfly.java:78-99):** alle acht Fluegelplatten Z = +-`cos(f2 * 1.3 * wingspeed) * pi * 0.25`, rechts positiv, links negativ; dauernd, unabhaengig von Bewegung; `wingspeed` = 1.0 (Konstruktorargument, manifest model_args). Kopf und Koerper statisch.
- **Varianten:** `butterfly_type = OreSpawnRand.nextInt(4)` im Konstruktor (EntityButterfly.java:40), DataWatcher-Slot 20 (:89), alle 25 Ticks per `force_sync` nachgezogen (:223-230), NBT `ButterflyType` (:299/304). Texturwahl (:70-83): Typ 1 -> `butterfly2.png`, in Dimension `DimensionID4` (Danger Dimension) stattdessen `vbutterfly1.png` (:71-73); Typ 2 -> `butterfly3.png`, Typ 3 -> `butterfly4.png`, sonst `butterfly.png`. Neue Pfade `assets/orespawn/textures/entity/butterfly{,2,3,4}.png`, `vbutterfly1.png` (manifest texture_map). 01-mobs.md #72 nennt den Vampir-Schmetterling als eigenen Mob - im Code ist es Typ 1 in der Danger Dimension.
- **Render-Pass:** `shouldRenderPass` (RenderButterfly.java:42-81) zeichnet nur fuer `Mothra` und `EntityLunaMoth` mit `moth_type == 0`; fuer den Schmetterling selbst passiert nichts.
- **Klang:** keine (manifest).
- **Spawn:** 16 Overworld-Biome, forest Gewicht 30, beach 8 mit Gruppen 5-15, Schalter `ButterflyEnable` (manifest).
- **Portierungshinweise:** Fluegel als Nulldicke-Platten (1 px dick, beidseitig sichtbar) -> `entityCutoutNoCull`. `scale()` = 1.0. Variantentextur nach synchronisiertem `butterfly_type` plus Dimensionsabfrage im Renderer.

### EntityCage (`entity_cage`)

- **Klasse/Renderer:** `EntityCage` (EntityThrowable) -> `RenderCage extends RenderSpinner` (manifest). Kein Modell.
- **Gezeichnet wird:** ein Billboard-Sprite 16x16 aus `spinners.png` 256x256 -> `assets/orespawn/textures/entity/spinners.png` (manifest texture_map; RenderSpinner.java:57). Slot-Index `spinItemIconIndex` = `EntityCage.getCageIndex()` (RenderCage.java:9-13), UV = Spalte `idx % 16`, Zeile `idx / 16` (RenderSpinner.java:33-36). Default 160 (EntityCage.java:22, `getCageIndex()` :52-53) = der leere Kaefig: ein braunes Gitter-Icon mit hellen Streben (Slot 160, Zeile 10 Spalte 0 des Blattes). Die Critter-Cage-Items reichen ihre `cage_id` durch (`CritterCage.java:31`; 160 leer, 161 Spinne, 162 Fledermaus, 163 Kuh, 164 Schwein, 165 Tintenfisch, 166 Huhn, weitere folgen; OreSpawnMain.java:5069 ff.), also erscheint das jeweilige Kaefig-Icon mit Tier in der Luft.
- **Groesse:** `glScalef(0.5)` auf ein 1x1-Quad, das um -0.5/-0.25 versetzt ist (RenderSpinner.java:25, :37-48) -> ca. 0.5 Blocks; Sprite zur Kamera gedreht (`180 - playerViewY`, `-playerViewX`) und um `rotationPitch` in der Bildebene gedreht (:40-42). Hitbox: Vanilla-`EntityThrowable` (manifest size null).
- **Animationen/Varianten/Klang:** keine; keine Sounds (manifest). Beim Treffer auf eine Entity (nur `entityHit != null` und `rand(10) >= 2`) je vier `smoke`-, `explode`- und `reddust`-Partikel am Ziel (EntityCage.java:57-62).
- **Portierungshinweise:** Kein Modell - ein `EntityRenderer`, der ein kameraorientiertes Quad mit `RenderType.entityCutout(spinners.png)` und UV-Offset aus dem synchronisierten Index zeichnet (Pendant zu `ThrownItemRenderer`, aber mit eigener Sprite-Tabelle statt Item-Modell). Alternativ: das Kaefig-Item als Item-Modell rendern (`ThrownItemRenderer` mit `ItemSupplier`), was die 16x16-Icons ohnehin sind - dann entfaellt `spinners.png` fuer diese Entity.

### Moth (`moth`)

- **Klasse/Renderer/Modell:** `EntityLunaMoth` (EntityAmbientCreature) -> `RenderButterfly(model, 0.4, 1.5)` -> `ModelButterfly(0.75)` (manifest). Gleiches Modell wie der Schmetterling.
- **Aussehen:** Typ 0 `lunamoth.png`: hellgruene Fluegel mit gelben Augenflecken, gruener Koerper - dazu der Energie-Overlay-Pass (unten). Typ 1 `eyemoth.png`: dunkelblau-graue Fluegel mit je einem grossen gelb-roten "Auge", schwarzer Koerper mit rosa Punkten. Typ 2 `darkmoth.png`: fast schwarze Fluegel mit dunkelblauen Feldern, blauer Koerper. Typ 3 `firemoth.png`: rot-orange Fluegel mit gelb-weissen Flammenflecken, gelber Koerper (Texturblatt).
- **Groesse:** Hitbox 0.5 x 0.5 (EntityLunaMoth.java:27). Modell 19 x 15 px mal 1.5 -> ca. 1.8 x 1.4 Blocks. Schatten 0.4 * 1.5 = 0.6.
- **Modell:** `ModelButterfly`, 10 Parts, 64x32; `wingspeed` = 0.75, also langsamerer Fluegelschlag als der Schmetterling (`cos(f2 * 1.3 * 0.75) * pi * 0.25`, ModelButterfly.java:83).
- **Animationen:** wie Butterfly.
- **Varianten:** `moth_type = OreSpawnRand.nextInt(4)` (EntityLunaMoth.java:26); Texturwahl in `EntityButterfly.getTexture` (EntityButterfly.java:58-69): 1 -> `eyemoth.png`, 2 -> `darkmoth.png`, 3 -> `firemoth.png`, sonst `lunamoth.png`. Neue Pfade `assets/orespawn/textures/entity/{lunamoth,eyemoth,darkmoth,firemoth}.png` (manifest texture_map). **`moth_type` wird nie synchronisiert und nie gespeichert:** das Feld kommt nur in Zeilen 12/21/26 vor, `entityInit` (:40-42) legt keinen DataWatcher-Slot an, und die Klasse ueberschreibt weder `writeEntityToNBT` noch `readEntityFromNBT` (grep). Der Client wuerfelt beim Erzeugen seiner Kopie einen eigenen Wert - die sichtbare Motte ist also clientseitig zufaellig und wechselt bei jedem Neuladen des Chunks. Fuer die Portierung: `moth_type` synchronisieren und in NBT schreiben, das ist die einzige Abweichung vom Original, die hier bewusst empfohlen wird.
- **Overlay-Pass (RenderButterfly.java:42-81):** nur bei `moth_type == 0` (und Mothra): Pass 1 zeichnet das Modell erneut mit der Vanilla-Textur `textures/entity/creeper/creeper_armor.png`, Texturmatrix um `ticks * 0.01` in U und V verschoben, Farbe (0.5, 0.5, 0.5, 1), Beleuchtung aus, `glBlendFunc(1, 1)` = additiv - der Energiewirbel des geladenen Creepers. Pass 2 setzt Matrix und Zustand zurueck.
- **Klang:** keine (manifest).
- **Spawn:** 15 Biome, forest/jungle 20, Schalter `MothEnable` (manifest).
- **Portierungshinweise:** `scale()` = 1.5; Overlay als `EnergySwirlLayer`-Ableger (wie `CreeperPowerLayer`) mit `RenderType.energySwirl(creeper_armor.png, u, v)` und Rot/Gruen/Blau 0.5 - nur wenn `moth_type == 0`.

### Mosquito (`mosquito`)

- **Klasse/Renderer/Modell:** `EntityMosquito` (EntityAmbientCreature) -> `RenderMosquito(model, 0.3, 0.5)` -> `ModelMosquito()` (manifest). Textur `mosquito.png` 32x32 -> `assets/orespawn/textures/entity/mosquito.png` (manifest; RenderMosquito.java:47).
- **Aussehen:** Fuenf Boxen: `body` 1x1x8 (dunkelbraun-rot), je Seite `wing1` 3x1x3 und `wing2` 5x1x1 (hellgraue, halb durchscheinend wirkende Streifen - Textur zeigt graue Balken). Ein duenner Strich mit zwei grauen Fluegelstummeln.
- **Groesse:** Hitbox 0.2 x 0.2 (manifest). Modell 8 px lang, 11 breit, 1 dick mal 0.5 -> ca. 0.25 x 0.34 Blocks. Schatten 0.15.
- **Modell:** 5 Parts, Textur 32x32 (Modell-JSON).
- **Animationen (ModelMosquito.java:45-57):** Fluegel Z = +-`cos(f2 * 3.0) * pi * 0.25`, schnell und dauernd (keine `wingspeed`-Variable).
- **Varianten:** keine.
- **Klang:** `mosquito` (manifest).
- **Spawn:** swampland 30, jungle/jungleHills 20, roofedForest 15; Gruppen 5-10; `MosquitoEnable` (manifest).
- **Portierungshinweise:** trivial; `scale()` = 0.5; Fluegel `entityCutoutNoCull`.

### Rainbow Ant (`rainbow_ant`)

- **Klasse/Renderer/Modell:** `EntityRainbowAnt extends EntityAnt` -> `RenderAnt(model, 0.1, 0.25)`, `ModelAnt` (manifest). Textur `rainbow_ant.png` 64x32 -> `assets/orespawn/textures/entity/rainbow_ant.png` (EntityAnt.java:146; manifest).
- **Aussehen:** Wie die braune Ameise, aber Kopf rot, Thorax orange, Abdomen gelb, Kiefer magenta, Beine gruen (links) und blau (rechts) - jede Koerperzone eine Regenbogenfarbe (Texturblatt).
- **Groesse:** Hitbox 0.1 x 0.1 (EntityRainbowAnt.java:14); Modell und Skalierung wie Ant -> ca. 0.12 Blocks hoch, 0.25 lang.
- **Modell/Animationen:** `ModelAnt`, siehe Ant.
- **Varianten:** keine (Klassenwahl in `EntityAnt.getTexture`, EntityAnt.java:45-47).
- **Klang:** keine (manifest). Config `RainbowedAntEnable` (Tippfehler im Original-Schluessel, manifest).
- **Portierungshinweise:** wie Ant; eigener `EntityType`, gleicher Renderer.

### Red Ant (`red_ant`)

- **Klasse/Renderer/Modell:** `EntityRedAnt extends EntityAnt` -> `RenderAnt(model, 0.15, 0.35)`, `ModelAnt` (manifest). Textur `red_ant.png` 64x32 -> `assets/orespawn/textures/entity/red_ant.png` (EntityAnt.java:145; manifest).
- **Aussehen:** Koerper in drei Rottoenen (dunkelrot Kopf, rot Thorax, hellrot Abdomen), Kiefer und Beine sehr dunkles Rotbraun (Texturblatt) - die aggressive Variante.
- **Groesse:** Hitbox 0.2 x 0.2 (EntityRedAnt.java:18). Scale 0.35 -> ca. 0.16 Blocks hoch, 0.35 lang; Schatten 0.15 * 0.35 = 0.05.
- **Modell/Animationen:** `ModelAnt`, siehe Ant.
- **Varianten:** keine.
- **Klang:** keine (manifest). Leben 2, Angriff 1.0, Speed 0.2 (manifest); `RedAntEnable`.
- **Portierungshinweise:** wie Ant, `scale()` = 0.35.

### EntityThrownRock (`entity_thrown_rock`)

- **Klasse/Renderer:** `EntityThrownRock` (EntityThrowable) -> `RenderThrownRock extends Render` (manifest). Kein Modell.
- **Gezeichnet wird:** ein kameraorientiertes 16x16-Item-Sprite; `func_77026_a` teilt durch 16.0 statt 256.0 (RenderThrownRock.java:39-42), also fuellt die ganze 16x16-Textur das Quad. `glScalef(0.5)` (:31) -> ca. 0.5 Blocks; um `rotationPitch` in der Bildebene gedreht (:48). Zwoelf Texturen (RenderThrownRock.java:100-111), gewaehlt ueber `getRockType()` = DataWatcher-Slot 20 (EntityThrownRock.java:54-72), gesetzt vom werfenden Item (`ItemRock.java:25-58`, Typen 1-12):

| Typ | legacy | neu (`assets/orespawn/textures/item/`) | Aussehen |
|---|---|---|---|
| 1 (Default) | `rocksmall.png` | `rocksmall.png` | kleiner grauer Kiesel |
| 2 | `rock.png` | `rock.png` | grauer Stein |
| 3 | `rockred.png` | `rockred.png` | grauer Stein mit roter Ader |
| 4 | `rockgreen.png` | `rockgreen.png` | mit gruener Ader |
| 5 | `rockblue.png` | `rockblue.png` | mit blauer Ader |
| 6 | `rockpurple.png` | `rockpurple.png` | mit violetter Ader |
| 7 | `rockspikey.png` | `rockspikey.png` | dunkler, stacheliger Brocken |
| 8 | `rocktnt.png` | `rocktnt.png` | roter Brocken mit weissem TNT-Aufdruck |
| 9 | `rockcrystalred.png` | `rockcrystalred.png` | rote Kristallgruppe |
| 10 | `rockcrystalgreen.png` | `rockcrystalgreen.png` | gruene Kristallgruppe |
| 11 | `rockcrystalblue.png` | `rockcrystalblue.png` | blaue Kristallgruppe |
| 12 | `rockcrystaltnt.png` | `rockcrystaltnt.png` | rote Kristallgruppe mit weissen Punkten |

  (Farbbeschreibung aus dem Texturblatt; Pfade manifest texture_map.)
- **Groesse:** Hitbox Vanilla-`EntityThrowable` (manifest size null).
- **Klang:** `orespawn:glassdead` bei Lautstaerke 1.0 / Pitch 1.0 im Aufprallcode (EntityThrownRock.java:249; zwei Dateien `glassdead1/2`, manifest).
- **Portierungshinweise:** `ThrownItemRenderer` mit `ItemSupplier`, der den Rock-Item-Stack zum Typ liefert - dann kommen die 16x16-Item-Texturen automatisch als flaches Item. Typ ueber `SynchedEntityData` (Slot 20 im Original).

### Unstable Ant (`unstable_ant`)

- **Klasse/Renderer/Modell:** `EntityUnstableAnt extends EntityAnt` -> `RenderAnt(model, 0.1, 0.25)`, `ModelAnt` (manifest). Textur `unstableant.png` 64x32 -> `assets/orespawn/textures/entity/unstableant.png` (EntityAnt.java:147; manifest).
- **Aussehen:** Kopf und Abdomen dunkelbraun, Thorax knallrot, Kiefer schwarz, Beine schwarz-weiss gestreift (links weiss, rechts schwarz) - "instabil" durch den roten Kern (Texturblatt).
- **Groesse:** Hitbox 0.1 x 0.1 (EntityUnstableAnt.java:14); wie Ant ca. 0.12 Blocks hoch.
- **Modell/Animationen:** `ModelAnt`, siehe Ant.
- **Varianten:** keine. **Klang:** keine (manifest). `UnstableAntEnable`.
- **Portierungshinweise:** wie Ant.

### Fairy (`fairy`)

- **Klasse/Renderer/Modell:** `Fairy` (EntityAmbientCreature) -> `RenderFairy(model, 0.1, 0.35)` -> `ModelFairy(1.5)` (manifest). Texturen ueber `Fairy.getTexture()` (RenderFairy.java:41-44), 64x64, neun Varianten `fairytexture.png` .. `fairytexture9.png` -> `assets/orespawn/textures/entity/fairytexture{,2..9}.png` (Fairy.java:340-348; manifest texture_map).
- **Aussehen:** Eine kleine Menschfigur (Kopf 5x5x5, Brust 7x4x3, Taille, Hueften, ein gerades Bein 2x13x2 und ein angewinkeltes Bein aus zwei Segmenten, zwei 1-px-Arme, zwei 2x2x1-Boxen `b1/b2` an der Brust) mit vier grossen flachen Fluegeln je Seite ein 24x16x0- und ein 26x16x0-Blatt (`lwing1` 24, `lwing2` 26, `rwing1` 26, `rwing2` 24), um -47/-34 deg (links) bzw. -146/-135 deg (rechts) Y nach hinten gestellt. Die Fluegel sind zu 83-86 % transparent - nur ein duenner Umriss mit Aderzeichnung ist gemalt (Alpha-Messung `fairytexture.png` uv 0,30 und 0,47). Textur 1: blonde Haare, blaues Top, dunkelblaue Fluegel-Umrisse; 2: braun/blau; 3: rot/blau; 4: braun, gruenes Kleid, weiss-orange Fluegel; 5: rot, blaues Top, rosa-weisse Fluegel; 6: blau/lila, orange Fluegel; 7: blau/gelb, weisse Fluegel; 8: gruen-gelb, rosa Fluegel; 9: gelb-weiss, hellblaue Fluegel (Texturblatt). 01-mobs.md #113 nennt nur "blonde, brunette, redhead" - der Code hat neun Skins.
- **Groesse:** Hitbox 0.4 x 0.8 (Fairy.java:47). Boxen unrotiert y -9..24 -> 33 px (Fluegeloberkante bis Fuesse; Koerper allein ca. 29 px), x -4..28 -> 32 px unrotiert, mit den um -34..-146 deg gedrehten Fluegeln grob 47 px Spannweite (Modell-JSON) mal 0.35 -> ca. 0.65-0.7 Blocks hoch, ca. 1.0 Block Fluegelbreite. Schatten 0.035. `renderDistanceWeight = 3.0` (Fairy.java:52).
- **Modell:** 15 Parts, 15 Boxen, Textur 64x64, groesste Box 26 (Modell-JSON). `glColor4f(1,1,1,1)` im Modell (06-models-design.md).
- **Animationen (ModelFairy.java:110-149):**
  - Fluegel Y: `lwing1 = -0.6 + cos(f2 * ws) * pi * 0.35`, `rwing1 = -2.55 - ...` (gespiegelt), `wing2` mit Frequenz 0.85 und Amplitude 0.25 - Vorder- und Hinterfluegel leicht versetzt (:115-118); `ws` = 1.5, dauernd.
  - Kopf: Y = 0.45 * netHeadYaw geklemmt +-0.45, X = headPitch (:119-126).
  - Arme: langsames Schwingen, je Arm eine eigene Frequenz: X `-0.2 + cos(f2 * ws * 0.15) * pi * 0.05` (links) bzw. `* 0.12` (rechts), Z `-0.15 + cos(f2 * ws * 0.1) * pi * 0.03` (links) bzw. `+0.15 + cos(f2 * ws * 0.11) ...` (rechts) (:127-130).
  - Leuchten: Fluegel werden zuerst normal beleuchtet gezeichnet (:131-134); dann `OpenGlHelper.setLightmapTextureCoords(unit, fly.getBlink(), 240)` (:135-136) - `getBlink()` liefert 240 in der ersten Haelfte eines Blinkzyklus, sonst 0 (Fairy.java:110-115; Zyklus `my_blink = 20 + rand(20)` Ticks, :46). Koerper, Kopf, Beine, Arme werden mit dieser Lichtmap gezeichnet (:138-148): bei 240 vollhell (Block-Licht 15), bei 0 nur Himmelslicht 15. Die Fee pulsiert also zwischen "leuchtend" und "normal".
  - Nachts (`worldTime % 24000 >= 12000`) spawnt der Client mit 1/5 Chance pro Tick `fireworksSpark`-Partikel unter der Fee, solange sie leuchtet (Fairy.java:188-195).
- **Varianten:** `fairy_type = world.rand.nextInt(9)` (Fairy.java:49), DataWatcher-Slot 20 (:96), alle 10 Ticks per `force_sync` nachgezogen (:178-187), NBT `FairyType` geschrieben / `fairyType` gelesen (:204/213 - Gross-/Kleinschreibung weicht ab, beim Laden wird also 0 gelesen; Original-Bug). Texturwahl :66-92.
- **Klang:** `big_splat` (manifest). Angriff 3.0, Leben 40, Ruestung 4 (manifest).
- **Spawn:** roofedForest 25, Gruppen 2-4, `FairyEnable` (manifest).
- **Portierungshinweise:** Zwei Render-Gruppen im selben Modell: Fluegel mit normalem `packedLight`, Koerperteile mit `LightTexture.pack(15, 15)` wenn `getBlink()` an ist - also im Renderer zweimal `model.renderToBuffer` mit unterschiedlichem Licht oder eine `RenderLayer` fuer den Koerper mit `RenderType.entityTranslucentEmissive`. Fluegel `entityCutoutNoCull`. `scale()` = 0.35. Blink-Zaehler ist Entity-Zustand (Fairy.java:175-177), laeuft auf beiden Seiten.

### Firefly (`firefly`)

- **Klasse/Renderer/Modell:** `Firefly` (EntityAmbientCreature) -> `RenderFirefly(model, 0.2, 0.75)` -> `ModelFirefly(2.5)` (manifest). Textur `Fireflytexture.png` 64x128 -> `assets/orespawn/textures/entity/fireflytexture.png` (Firefly.java:175; manifest).
- **Aussehen:** Ein Kaefer: schwarzer `body` 5x5x5 mit rot-weissem Kopf (`head` 3x3x3, `mouth` 1x1x3), zwei kleine weisse Augen mit schwarzer Pupille (`eye_left/right` 1x2x2), vier 1x5x1-Beine, zwei durchsichtig wirkende weiss-graue Fluegel als Nulldicke-Platten (`wing_left/right` 0x6x2, um +-40 deg Z aufgestellt) und der `TailLight` 3x3x4 in leuchtendem Gelb (Textur uv 10,27: gelbe Flaeche mit hellen Sprenkeln).
- **Groesse:** Hitbox 0.4 x 0.8 (Firefly.java:27). Boxen unrotiert y 0..15 -> 15 px hoch (Fluegel eingerechnet, die stehen um 40 deg auf), z -8..5 -> 13 px lang (Modell-JSON) mal 0.75 -> ca. 0.6-0.7 Blocks. Schatten 0.15. `renderDistanceWeight = 3.0` (:29).
- **Modell:** 12 Parts, Textur 64x128, groesste Box 6 (Modell-JSON).
- **Animationen (ModelFirefly.java:92-114):** Fluegel Z = +-(1.11 + cos(f2 * 2.5) * pi * 0.35), dauernd (:97-98). Alles ausser `TailLight` normal gezeichnet (:99-109), dann Lichtmap = `getBlink()` x 240 und `TailLight` (:110-113). `getBlink()` (Firefly.java:48-53): 240 in der ersten Haelfte von `my_blink = 20 + rand(20)` Ticks (:26), sonst 0 -> das Hinterteil blinkt im Sekundentakt.
- **Varianten:** keine.
- **Klang:** keine im Manifest; `getSoundVolume()` = 0 (Firefly.java:55-57) - absichtlich stumm.
- **Verhalten mit Render-Folge:** verschwindet tagsueber (`worldTime % 24000 <= 11000` -> 1/500 pro Tick `setDead`, Firefly.java:107-114), ausser mit `isNoDespawnRequired`.
- **Spawn:** 13 Biome, Gruppen bis 10, `FireflyEnable` (manifest).
- **Portierungshinweise:** `TailLight` als eigene Render-Gruppe mit `LightTexture.pack(15,15)` wenn Blink an (z. B. `RenderLayer` mit `entityTranslucentEmissive` oder zweiter `renderToBuffer`-Aufruf nur fuer diesen Part). Fluegel `entityCutoutNoCull`. `scale()` = 0.75.

### Flounder (`flounder`)

- **Klasse/Renderer/Modell:** `Flounder` (EntityAnimal) -> `RenderFlounder(model, 0.1, 1.0)` -> `ModelFlounder()` (manifest). Textur `Floundertexture.png` 64x32 -> `assets/orespawn/textures/entity/floundertexture.png` (RenderFlounder.java:51; manifest).
- **Aussehen:** Ein flacher Plattfisch aus sechs 1-px-dicken Platten: `body` 8x1x12, `head` 4x1x2 vorn, `tail1` 4x1x2 und `tail2` 6x1x3 hinten, `lfin/rfin` 3x1x2 seitlich; Textur sand- bis ockerbraun mit zwei dunklen Augenpunkten ("blends in with the mud", 01-mobs.md #91).
- **Groesse:** Hitbox 0.55 x 0.25 (manifest). Modell 19 px lang, 14 breit, 1 px hoch (Modell-JSON) -> ca. 1.2 x 0.9 Blocks, 1/16 Block dick; liegt bei y = 22, also 2 px ueber dem Boden. Kind: `isChild()` -> `scale / 2` (RenderFlounder.java:35-38). Schatten 0.1.
- **Modell:** 6 Parts, Textur 64x32 (Modell-JSON).
- **Animationen (ModelFlounder.java:51-83):** bei `f1 > 0.1` Flossen Z = `cos(f2 * 1.3) * pi * 0.25 * f1` (links) / `cos(f2 * 1.7) * ...` (rechts), Schwanz X = `cos(f2 * 1.2) * pi * 0.25 * f1`; im Stand Schwanz X = `cos(f2 * 0.7) * pi * 0.05` (leichtes Wedeln), Flossen 0.
- **Varianten:** keine. **Klang:** `ratdead` (manifest). Leben 5 (manifest).
- **Spawn:** keine Biom-Eintraege (Termite Crystal Dimension laut 01-mobs.md #91); `FlounderEnable` (manifest).
- **Portierungshinweise:** `scale()` = 1.0 bzw. 0.5 fuer Babys (`isBaby()`). Platten `entityCutoutNoCull`.

### Frog (`frog`)

- **Klasse/Renderer/Modell:** `Frog` (EntityAnimal) -> `RenderFrog(model, 0.35, 1.0)` -> `ModelFrog(1.0)` (manifest). Textur `Frogtexture.png` 64x64 -> `assets/orespawn/textures/entity/frogtexture.png` (RenderFrog.java:47; manifest).
- **Aussehen:** Ein gruen gesprenkelter Frosch: `body` 8x11x2 um 42 deg nach hinten gekippt (sitzende Haltung), darunter der `jaw` 8x8x1 um 70 deg - das rote Maulinnere (Textur uv 42,15: rote Flaeche) wird sichtbar, wenn das Maul aufgeht; zwei kleine Augen 1x2x1 (gelb-gruen) oben vorn, zwei Vorderbeine 1x5x1, zwei Hinterbeine aus `leg1` 1x9x2 und `leg2` 1x10x1, seitlich abgewinkelt (13 / -22 deg Z).
- **Groesse:** Hitbox 0.75 x 0.75 (Frog.java:27). Boxen unrotiert y 14..25 -> 11 px hoch, x -6..6 -> 12 px breit, z -2..4 -> 6 px lang (Modell-JSON); der um 42 deg gekippte `body` (11 px) und die seitlich gestellten Hinterbeine (`lleg1` 13 deg, `lleg2` -22 deg, im Sprung 2.44 rad) machen das Tier in Ruhe grob 10 px hoch und bis 19 px breit -> ca. 0.6-0.7 Blocks hoch, bis 1.2 breit. Schatten 0.35, Scale 1.0.
- **Modell:** 10 Parts, Textur 64x64, groesste Box 11 (Modell-JSON).
- **Animationen (ModelFrog.java:78-122):**
  - Vorderbeine Y = +-`cos(f2 * 1.4) * pi * 0.55 * f1`, Hinterbein-Unterschenkel `leg2` Y = -+halb davon, nur bei `f1 > 0.1` (:83-92).
  - Maul: `jaw.rotateAngleX = 1.22 + (getSinging() != 0 ? cos(f2 * 0.85) * pi * 0.15 : 0)` (:93-99); `getSinging` = DataWatcher-Slot 20 (Frog.java:57-63).
  - Sprung: `|motionY| > 0.1` -> `lleg1.rotateAngleZ = 2.44` (Beine gestreckt), sonst 0.227 (:100-107); `leg2` folgt dem Ende von `leg1` (9 px, :108-111).
- **Varianten:** keine (die Frosch-Prinzen/Prinzessinnen sind eigene Girlfriend/Boyfriend-Skins).
- **Klang:** `big_splat`, `scorpion_hit` (manifest). Leben 8 (manifest).
- **Spawn:** river/swampland als waterCreature 20 und ambient 2-3, jungle ambient 3; `FrogEnable` (manifest).
- **Portierungshinweise:** `singing` als `EntityDataAccessor<Integer>`; Sprungpose aus `getDeltaMovement().y` im `setupAnim`. `scale()` = 1.0.

### WTF? (`wtf`)

- **Klasse/Renderer/Modell:** `GammaMetroid` (EntityTameable) -> `RenderGammaMetroid(model, 0.75, 0.9)` -> `ModelGammaMetroid(0.45)` (manifest). Textur `GammaMetroid.png` 256x64 -> `assets/orespawn/textures/entity/gammametroid.png` (RenderGammaMetroid.java:51; manifest).
- **Aussehen:** Der Gamma-Metroid aus Metroid II: ein grosser blauer Kaefer-Quallen-Hybrid. Panzer aus vier grossen, schraeg gestellten Schalen (`Shell1` 19x19x12 um 45 deg Z, `Shell2` 16x16x8, `Shell3` 12x12x7, `Shell4` 6x6x8) in dunklem Blau mit tuerkisen Kanten, davor ein breiter `Head` 16x8x6 mit Schnabel (`BeakUpper`/`BeakLower`, 45 deg Y verdreht -> auf der Spitze stehend) und drei 12 px langen Hauern (`Left/Middle/RightTusk`, tuerkis-weiss), zwei rot leuchtende Augen (Texturbereich Head uv 48,48 mit roten Flecken), vier zweigliedrige Beine, und im Bauch: `Bellyoutside` 16x14x16, dessen Textur (uv 0,0) zu 40 % Alpha 128 hat - halbdurchsichtige blaue Huelle, durch die der rot-orange `Core` 6x6x6 (uv 82,33; gelb-oranger Kern) und die `Bellyinside`-Platte 16x1x16 (rote Flaeche uv 150,3) sichtbar sind. `Shell1/2` haben 11-16 % voll transparente Pixel (Alpha-Messung), also ausgezackte Panzerkanten.
- **Groesse:** Hitbox 1.5 x 1.5 (GammaMetroid.java:35). Boxen unrotiert y -6..25, x -15..15, z -22..21 -> 31 px hoch, 30 breit, 43 lang (Modell-JSON); die um 45 deg gekippte `Shell1` (19 px Kante) ragt weiter, grob 35 px hoch / 36 breit. Mal 0.9 -> ca. 1.8-2.0 Blocks hoch, 1.7-2.0 breit, 2.4 lang. Kind `scale / 2` (RenderGammaMetroid.java:35-38). Schatten 0.75 * 0.9 = 0.675.
- **Modell:** 21 Parts, 21 Boxen, Textur 256x64, groesste Box 19 (Modell-JSON). GL: `glEnable(GL_NORMALIZE)`, `glEnable(GL_BLEND)`, `glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)` um alle Parts (ModelGammaMetroid.java:189-213); Reihenfolge Core zuerst, `Bellyoutside` zuletzt (:192-212) - die Zeichenreihenfolge ist Teil des Looks.
- **Animationen (ModelGammaMetroid.java:145-264):**
  - Hauer: drei unabhaengige Wackelfrequenzen X (0.81/0.87/0.99 * ws) und Y (1.11/1.17/1.25 * ws), Amplitude `pi * 0.08` (:150-161); `ws` = 0.45.
  - Beine: `cos(f2 * 2 * ws) * pi * 0.12 * f1` mit Heben (`upangle`, :164-173), Helfer `doLeftFLeg` usw. (:226-264) drehen Ober- und Unterschenkel um X und Z und rechnen den Kniepunkt nach (7 px / 5 px).
  - Panzer atmet: `cos(f2 * 0.4 * ws) * pi * 0.05`, zu 0 wenn `e.isSitting()` (:174-177); Shell1 /4 (X und -Y), Shell2 -0.49/+0.33, Shell3 -0.96/+0.63, Shell4 -0.28 Offsets (:178-184).
  - Unterkiefer: `|cos(f2 * 0.75 * ws) * pi * 0.1| + 0.14` in X und Z (:185-188) - Kauen.
- **Varianten:** keine.
- **Klang:** `wtf_living`, `duck_hurt`, `alo_death` (manifest). Leben 100, Angriff 10, Ruestung 12 (manifest `GammaMetroid_stats`). Config `GammaMetroidEnable`.
- **Portierungshinweise:** Ganzes Modell mit `RenderType.entityTranslucent(texture)` zeichnen, Part-Reihenfolge wie im Original beibehalten (Core vor Huelle), damit der Kern durch die 50 %-Huelle scheint. `scale()` = 0.9 / 0.45 fuer Babys. `isSitting` (Tameable) fuer die Panzer-Atmung. Der Wiki-Konflikt "Attack 23, Defense 35" (01-mobs.md #39) ist durch den Code widerlegt: 10 / 12.

### Gazelle (`gazelle`)

- **Klasse/Renderer/Modell:** `Gazelle` (EntityTameable) -> `RenderGazelle(model, 0.45, 1.0)` -> `ModelGazelle(0.65)` (manifest). Textur `Gazelletexture.png` 64x64 -> `assets/orespawn/textures/entity/gazelletexture.png` (RenderGazelle.java:51; manifest).
- **Aussehen:** Eine schlanke Antilope in hellem Braun mit dunkelbraunen Flecken und beigem Bauch: `Body` 6x6x13 leicht geneigt, `neck` 5x5x13 fast senkrecht (87 deg), `throatfluff` 4x3x5, Kopf 6x6x6 mit `nose` 5x3x5 und `mouth`, zwei Ohren 3x2x1 um 90 deg Y, zwei dreigliedrige Hoerner (`la1..3`/`ra1..3`, 1x4/5x1, nach hinten gebogen -21/-60/-19 deg), `tail` 4x4x4, vier Beine aus je 4-5 Segmenten (Oberschenkel 2x6x3, Rohr 2x12x2, Fessel 2x6x2 um -23 deg, Huf 3x3x3). Rote Pixel an einer Fessel (Textur unten) sind ein Detail der Hufe.
- **Groesse:** Hitbox 0.6 x 1.8 (Gazelle.java:32). Boxen unrotiert y -21.5..24 -> 45.5 px = ca. 2.8 Blocks bis zu den Hornspitzen, z -13..14.5 -> 27.5 px lang (Modell-JSON; Naeherung, weil `neck` um 87 deg und `Chest` um 134 deg gedreht sind und die Boxen dadurch anders liegen als unrotiert gerechnet). Kind `scale / 2` (RenderGazelle.java:35-38). Schatten 0.45.
- **Modell:** 34 Parts, 34 Boxen, Textur 64x64, groesste Box 13 (Modell-JSON).
- **Animationen (ModelGazelle.java:223-302):** Beine `cos(f2 * 1.1 * ws) * pi * 0.12 * f1` nur bei `f1 > 0.1`, mit festen Offsets je Segment (0.297 / -0.074 / -0.409, hinten 0.185), links-vorn und rechts-hinten in Phase (:229-252); `ws` = 0.65. Kopf, Nase, Maul, Hoerner Y = 0.45 * netHeadYaw; Ohren `1.57 + Kopf + cos(f2 * 0.5) * pi * 0.02` (Ohrenzucken) (:253-264). Schwanz `1.0 + cos(f2 * 0.1) * pi * 0.06` ausser beim Sitzen (:265-267).
- **Varianten:** keine.
- **Klang:** `scorpion_hit`, `cryo_death` (manifest). Leben 15 (manifest). Speed: `moveSpeed = 0.2` (Gazelle.java:26) wird im selben Konstruktor mit `0.3` ueberschrieben (:33) - die zwei Manifest-Werte sind kein Gehen/Laufen, effektiv gilt 0.3. `GazelleEnable`.
- **Portierungshinweise:** `scale()` 1.0 / 0.5 Baby. Kopfgruppe (Kopf, Nase, Maul, Hoerner, Ohren) im Modell-JSON getrennt - in 1.21.1 am besten als Kinder eines Kopf-Parts anlegen, damit ein einziger Yaw reicht.

### Ghost (`ghost`)

- **Klasse/Renderer/Modell:** `Ghost` (EntityAmbientCreature, `noClip = true`, Ghost.java:22) -> `RenderGhost(model, 0.0, 0.65)` -> `ModelGhost()` (manifest). Textur `Ghosttexture.png` 64x64 -> `assets/orespawn/textures/entity/ghosttexture.png` (RenderGhost.java:47; manifest).
- **Aussehen:** Das klassische Bettlaken-Gespenst: `HeadAndBody` 6x21x6 (hellgrau, unten ausgefranst - 33 % der Textur transparent), zwei Arme 2x11x2 um +-19 deg Z abgespreizt (23 % transparent), zwei dunkle Augen und ein Mund im Kopfbereich (Texturblatt). Das ganze Modell wird mit `glColor4f(0.75, 0.75, 0.75, 0.25)` und `SRC_ALPHA`-Blend gezeichnet (ModelGhost.java:41-50): 25 % Deckkraft, abgedunkelt.
- **Groesse:** Hitbox 0.5 x 1.5 (Ghost.java:19). Boxen unrotiert y 0..21 -> 21 px hoch, x -4..4 -> 8 px breit ohne die um +-19 deg abgespreizten Arme, grob 14 px mit (Modell-JSON; Modellboden 24 -> haengt 3 px ueber dem Boden) mal 0.65 -> ca. 0.85 Blocks hoch. Schatten 0.0 * 0.65 = 0 (kein Schatten).
- **Modell:** 3 Parts, Textur 64x64, groesste Box 21 (Modell-JSON).
- **Animationen (ModelGhost.java:34-50):** Arme schweben: `LArm.Z = -0.33 + cos(f2 * 0.30) * pi * 0.05`, `RArm.Z = 0.33 + cos(f2 * 0.32) ...`, `LArm.X = -0.33 + cos(f2 * 0.34) ...`, `RArm.X = 0.33 + cos(f2 * 0.36) ...` - vier leicht verschiedene Frequenzen, dauernd.
- **Varianten:** keine.
- **Klang:** `ghost_sound` mit 50 % Chance als Living-Sound, Lautstaerke 0.3, Tonhoehe 1.5, kein Hurt-/Death-Sound (Ghost.java:41-58; manifest).
- **Spawn:** 22 Biome ohne Guard mit Gewicht 15 plus fuenf Eintraege mit `GhostEnable` (manifest) - die Haelfte der Spawns ignoriert also den Schalter (Original-Verhalten).
- **Portierungshinweise:** Renderer mit `RenderType.entityTranslucent(texture)` und Farbe ARGB (0.25, 0.75, 0.75, 0.75); Schattenradius 0. `noClip` -> `noPhysics = true` plus eigene Flugbewegung. `scale()` = 0.65.

### Ghost Pumpkin Skelly (`ghost_pumpkin_skelly`)

- **Klasse/Renderer/Modell:** `GhostSkelly` (EntityAmbientCreature, `noClip = true`, GhostSkelly.java:24) -> `RenderGhostSkelly(model, 0.0, 1.05)` -> `ModelGhostSkelly()` (manifest). Textur `GhostSkellytexture.png` 128x64 -> `assets/orespawn/textures/entity/ghostskellytexture.png` (RenderGhostSkelly.java:47; manifest).
- **Aussehen:** Eine Vogelscheuche aus Geist und Kuerbis: `body` als 1x21x1-Stab (schwarz), darauf ein grauer `shirt` 5x12x5 mit zerfetztem Saum (41 % transparent), ein Kuerbiskopf `head` 7x5x7 in Orange mit rotem Gesicht (Textur uv 40,29: orange Flaeche mit roten Augen/Mund) und gruenem `stem` 1x2x1 (10 deg schief), zwei 15 px lange horizontale Armstangen `larm/rarm` 15x1x1 (schwarz-weiss gemustert) mit grauen `sleeve`-Boxen 9x8x3 und herabhaengenden Ketten `lchains` 3x16x1 / `rchains` 3x10x1 (51 % transparent - gemalte Kettenglieder). Alles mit `glColor4f(0.75, 0.75, 0.75, 0.25)` und Blend gezeichnet (ModelGhostSkelly.java:128-144): 25 % Deckkraft.
- **Groesse:** Hitbox 1.5 x 2.0 (GhostSkelly.java:21). Boxen unrotiert y -8..20 -> 28 px hoch (Stiel bis y = -8, Koerper bis 20), x -14..15 -> 29 px Armspannweite (Modell-JSON) mal 1.05 -> ca. 1.85 Blocks hoch, 1.9 breit. Schatten 0 (0.0 * 1.05).
- **Modell:** 10 Parts, Textur 128x64, groesste Box 21 (Modell-JSON).
- **Animationen (ModelGhostSkelly.java:76-145):**
  - Arme, Aermel, Ketten schwingen als Gruppe: links Z `cos(f2 * 0.2) * pi * 0.05`, rechts `cos(f2 * 0.22)`; Y links `cos(f2 * 0.24)`, rechts `cos(f2 * 0.26)` (:84-111).
  - Kopfdrehung: `newangle = cos(f2 * 0.05) * pi * 2` (+-360 deg); ein `RenderInfo`-Zustand (`rf2`, `ri2`, GhostSkelly.java:15-63) erkennt den Zyklusanfang (`f2 * 0.05 mod 2pi` faellt unter den letzten Wert) und wuerfelt dann mit 1/3, ob der Kopf in diesem Zyklus dreht (`ri2 |= 1`, :112-125); sonst `head.rotateAngleY = 0`. Das ist das "spins its head 360 deg" aus 01-mobs.md #110.
- **Varianten:** keine.
- **Klang:** `chain_rattles` mit 50 % Chance, Lautstaerke 0.5, Tonhoehe 1.5, kein Hurt/Death (GhostSkelly.java:69-90; manifest).
- **Spawn:** wie Ghost (22 Biome ohne Guard + fuenf mit `GhostSkellyEnable`, manifest).
- **Portierungshinweise:** wie Ghost (`entityTranslucent`, Alpha 0.25, Schatten 0, `scale()` 1.05). Der Kopfdreh-Zufall braucht clientseitigen Zustand pro Entity (Original: `RenderInfo` am Entity, ueber `getRenderInfo/setRenderInfo`); in 1.21.1 ein client-only Feld oder eine Map im Renderer. Beim Kopf ist die 360-deg-Rotation ein glatter Cosinus, kein Sprung.

### Jeffery (`jeffery`)

- **Klasse/Renderer/Modell:** `GiantRobot` (EntityMob) -> `RenderGiantRobot(model, 0.99, 1.0)` -> `ModelGiantRobot(0.25)` (manifest). Textur `GiantRobottexture.png` 256x512 -> `assets/orespawn/textures/entity/giantrobottexture.png` (RenderGiantRobot.java:47; manifest). Tracking-Reichweite 128 (manifest).
- **Aussehen:** Ein schwarzer, kantiger Riesenroboter mit hellgrauen Fugenlinien und magentafarbenen Leuchtstreifen: `Head` 14x12x14 mit einem violetten Punkt (Textur uv 127,0: magenta Auge), `Neck` 8x2x8, `Shoulders` 44x8x8 (breit), drei gestufte Ruecken-/Brustbloecke `Back1..3` (8x24x8, 26x24x16, 34x26x20; Back2/3 tragen die magenta Linie), `Hip` 8x8x30 quer, und je ein Bein- und Armsatz, der zweimal gezeichnet wird: `Thigh` 6x43x6 mit `Thigh2` 14x24x14 und `Thigh3` 10x17x10 als Verkleidung, `Shin` 6x43x6, dreiteiliger Fuss (`Foot1` 14x4x17 Sohle, `Foot2` 12x19x13, `Foot3` 10x14x9), `Arm1` 12x21x12, `Arm2` 8x24x8, `Arm3` 6x33x6 und `Knuckles` 14x12x10 (Faust mit magenta Punkten, Textur uv 56,400).
- **Groesse:** Hitbox 3.0 x 9.75 (GiantRobot.java:28). Boxen unrotiert y -142..24 -> 166 px hoch, x -22..35 -> 57 px (nur ein Arm-/Beinsatz im JSON, gespiegelt ca. 70 px ueber die Schultern), z -15..15 -> 30 px lang (Modell-JSON) bei Scale 1.0 -> ca. 10.4 Blocks hoch, 3.6-4.4 breit. Deckt sich mit "about 10 or 11 blocks tall" (01-mobs.md #13). Schatten 0.99.
- **Modell:** 18 Parts, 18 Boxen, Textur 256x512, groesste Box 44 (Modell-JSON). Keine GL-Aufrufe; die Beine und Arme werden mit denselben Parts zweimal gerendert (Spiegelung ueber `rotationPoint`).
- **Animationen (ModelGiantRobot.java:128-411):** Zustand in `RenderGiantRobotInfo` (`e.getRenderGiantRobotInfo()`, :133) am Entity.
  - `movescale = min(f1 * 0.65, 1)` (:134-137); `ws` = 0.25.
  - Huefte: X = `cos(-f2 * ws) * pi * 0.1 * movescale`, Y = `sin(...) + pi/2` (die Huefte steht quer, :138-147), vertikales Wippen `hipy + cos(-f2 * ws * 2) * movescale * 4` px (:144-145).
  - Oberschenkel: `cos(-f2 * ws + pi/2) * pi * 0.15 * movescale - 0.196 * movescale`, zweites Bein um pi versetzt; Schienbeine `cos(-f2 * ws + pi) * pi * 0.2 * movescale + 0.628 * movescale` (:140-143). Bein 1: Thigh-Pivot = Hip + 13 px in Hueftrichtung (:159-176), Shin 40 px unter dem Thigh-Ende (:181-183), Fuss am Shin (:195-212); Bein 2 identisch mit -13 px (:226-282).
  - Arme: Schultern Y = `-hipydisplayangle` (Gegendrehung zur Huefte), Arm-X = Oberschenkelwinkel des Gegenbeins (:283-287); bei `getAttacking() != 0` (GiantRobot.java:343) Schulterdrehung `-sin(f2 * ws * 2) * pi * 0.2` und Arme, die abwechselnd hoch- und niederschlagen (`a1angle = sin(...) * pi / 5 - pi/4 + 0.628`, Unterarm `-a1 + pi + 0.628`) (:288-298). Arm1/2 an Shoulders +-26 px, Arm3/Knuckles 41 px darunter (:301-388).
  - Kopf: Y = netHeadYaw, X = headPitch / 3 (:403-404). Oberkoerper folgt dem Hueft-Wippen (:389-402).
- **Varianten:** keine.
- **Klang:** `robot_living` (4 Dateien), `robot_hurt` (8), `robot_death` (5) (manifest).
- **Stats:** Leben 550, Angriff 40, Ruestung 18, Speed 0.55 (manifest `Jeffery_stats`); unter den 1.21.1-Klemmen. Keine Biom-Spawns (Rainbow Ant Dimension laut 01-mobs.md #13); `JefferyEnable`.
- **Portierungshinweise:** Das Modell hat nur einen Bein- und einen Armsatz; in 1.21.1 entweder die Parts im `LayerDefinition` verdoppeln (linke/rechte Kopien, saubere Loesung) oder im Renderer `renderToBuffer` zweimal mit umgesetzten Pivots aufrufen. Die prozedurale Kette (Hip -> Thigh -> Shin -> Foot mit `sin/cos`-Offsets) ist als Kinderhierarchie (`Thigh` Kind von `Hip`, `Shin` Kind von `Thigh`) deutlich einfacher zu portieren als die nachgerechneten Weltpositionen - die Segmentlaengen sind 13 / 40 / 41 px. `attacking` synchronisieren. `scale()` = 1.0.

### Girlfriend (`girlfriend`)

- **Klasse/Renderer/Modell:** `Girlfriend` (EntityTameable) -> `RenderGirlfriend(ModelBiped, 0.5) extends RenderBiped` (manifest). Vanilla `ModelBiped`, alle Skins 64x32 (Spielerskin-Format 1.7).
- **Aussehen:** Eine Spielerfigur; 41 "trockene" Outfits `girlfriend0.png` .. `girlfriend40.png` (Girlfriend.java:1137-1177; z. B. 0: blond mit rotem Top und Jeans, 7: braune Haare, weisses Top, 20: Regenbogen-Haare, schwarzes Outfit mit Creeper-Aufdruck), 18 Bikini-Skins `bikini0..17.png` (:1179-1196; 0: braune Haare, gruener Bikini), der Valentinstag-Skin `girlfriendv.png` (:1178; rote Haare, rotes Kleid mit Herz, gruen-schwarze Beine) und zwei Frosch-Prinzessinnen `FrogPrincess.png` / `FrogPrincess2.png` (:1197-1198; braune Haare, weiss-goldenes Kleid mit Krone). Insgesamt 62 PNG-Literale (06-models-design.md). Neue Pfade `assets/orespawn/textures/entity/girlfriend{0..40}.png`, `bikini{0..17}.png`, `girlfriendv.png`, `frogprincess{,2}.png` (manifest texture_map). Ausserdem liegt `girlfriendgui.png` im Jar (GUI-Overlay, nicht Teil des Entity-Renderings).
- **Groesse:** Hitbox 0.5 x 1.6 (Girlfriend.java:120); am Valentinstag (`OreSpawnMain.valentines_day != 0`) 2.5 x 8.0 (:121-123) und `preRenderCallback` skaliert mit `glScalef(5, 5, 5)`, solange `feelingBetter == 0` (RenderGirlfriend.java:22-29) -> ca. 10 Blocks gross; Leben dann 800 statt 80 (Girlfriend.java:554-559). Sobald sie "feeling better" ist (Treffer mit `MyRoseSword`, 1/4 Chance, :1088-1100) faellt sie auf 0.5 x 1.6 zurueck. Normal: Vanilla-Biped ca. 1.8-2 Blocks, Schatten 0.5.
- **Modell:** Vanilla `ModelBiped` -> `HumanoidModel` (`ModelLayers.PLAYER`-Geometrie mit 64x32-Textur: in 1.21.1 muss die `LayerDefinition` mit `CubeDeformation.NONE` und Texturgroesse 64x32 gebaut werden, `PlayerModel`-Overlays fallen weg).
- **Animationen:** Vanilla-Biped (Armschwung, Kopf, `updateArmSwingProgress` in `onLivingUpdate`, Girlfriend.java:562); sitzen ueber `setSitting` (:585). `RenderBiped` zeichnet gehaltene Items und Ruestung (Wiki: Schwerter, Pink Tourmaline / Tigers Eye Armour, 01-mobs.md #73).
- **Varianten (Girlfriend.java:314-345 ff.):** Valentine-Skin bei `valentines_day != 0 && feelingBetter == 0`; sonst `wet_count <= 0` -> Prinzessin (`is_princess` 1/2) oder `DryTexture<getTameSkin()>`; `wet_count > 0` (500 Ticks nach Wasser/Lava-Kontakt, :564-569) -> Bikini nach `which_wet_girl`. Startwerte `which_girl = rand(41)`, `which_wet_girl = rand(18)` (:117-118). Eine gelbe Blume (`Blocks.yellow_flower` oder `CrystalFlowerYellowBlock`) vom Besitzer schaltet zyklisch weiter: nass -> naechster Bikini (0..17), trocken -> naechstes Outfit (0..40) (:691-710).
- **Klang:** `o_happy`, `o_fight`, `o_hurt`, `o_ow`, `o_taunt`, `o_woohoo`, `o_water`, `o_thunder`, `o_dark`, `o_death_girlfriend`, `o_death_single` (manifest). Angriff 8.0, Ruestung 23 (manifest).
- **Spawn:** beach 30 mit Gruppen 8-15, forest/river/stoneBeach 10, weitere 2-8; Typ creature; `GirlfriendEnable` (manifest).
- **Sync im Original (Girlfriend.java:159-168):** DataWatcher 20 = `which_girl` (trockener Skin), 22 = `which_wet_girl` (Bikini), 21 = `voice`, 23 = `voice_enable`, 24 = `is_princess`, 25 = `feelingBetter`; alle 20 Ticks per `force_sync` nachgezogen (:579-585). NBT `GirlType` / `WetGirlType` (:228, :238-239).
- **Portierungshinweise:** `HumanoidMobRenderer` mit `HumanoidModel` (64x32-Layer), `ItemInHandLayer` und `HumanoidArmorLayer`; `getTextureLocation` aus den synchronisierten Werten Skin-Index, Wet-Index, `is_princess`, `feelingBetter` plus `wet_count` (letzteres ist im Original nicht synchronisiert - der Client rechnet es aus `isInWater()` selbst nach, :564-569). `scale()` = 5.0 nur im Valentinsfall. Leben 800 liegt unter 1024, braucht keine virtuelle Skalierung. Der Datumsschalter `valentines_day` ist globaler Zustand in `OreSpawnMain`; als Config-Flag im `ModConfigSpec` fuehren.
