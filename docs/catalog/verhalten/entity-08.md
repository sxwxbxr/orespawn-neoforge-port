# Verhalten: entity-08

Dieser Batch ist bunt gemischt. Er enthält drei Portal-Ameisen (`EntityRainbowAnt`, `EntityRedAnt`, `EntityUnstableAnt`) auf der gemeinsamen Basis `EntityAnt`. Dazu kommen das Wurfgeschoss `EntityThrownRock` mit zwölf Steinsorten und vier schwebende Umgebungswesen ohne Pathfinding (`Fairy`, `Firefly`, `Ghost`, `GhostSkelly`). Weiter gibt es drei Tiere mit eigener Blockscan-KI (`Flounder`, `Frog`, `Gazelle`) und das zähmbare Höhlenmonster `GammaMetroid` („WTF?“). Groß sind der Laser-Riese `GiantRobot` („Jeffery“) und die umfangreichste Klasse, die Begleiterin `Girlfriend` (Ausrüstung, Fern- und Nahkampf, Valentinstag-Modus, Tanz). Den Abschluss bildet die Drop-Variante `GoldCow`. Mehrere Klassen tragen Originalfehler, die der Port bewusst entscheiden muss: der NBT-Schlüssel der Fee, die Besitzübernahme bei der Girlfriend, das gezähmte Baby ohne Besitzer beim WTF? und die Gazelle, die zu sich selbst läuft. Diese Punkte stehen jeweils unter „Portierung“ und gesammelt in `unresolved`.

Gemeinsame Hinweise für alle Klassen:

- Die Dimensions-IDs sind `BaseDimensionID` = 80 (OreSpawnMain.java:1138), `DimensionID` = Base, `DimensionID2..6` = Base+1..+5 (OreSpawnMain.java:1266-1271).
- Die Namen stammen aus den WorldProvidern: `DimensionID` „Dimension-Utopia“ (WorldProviderOreSpawn.java:19), `DimensionID2` „Dimension-Extreme“ (Mining, WorldProviderOreSpawn2.java:13), `DimensionID3` „Dimension-VillageMania“ (WorldProviderOreSpawn3.java:19), `DimensionID4` „Dimension-Islands“ (WorldProviderOreSpawn4.java:19), `DimensionID5` „Dimension-Crystal“ (WorldProviderOreSpawn5.java:19), `DimensionID6` „Dimension-Chaos“ (WorldProviderOreSpawn6.java:19).
- `BiomeGenUtopianPlains` liefert die Spawnlisten dieser Dimensionen: Konstruktor = Grundliste (Utopia), `setIslandCreatures`, `setCrystalCreatures`, `setVillageCreatures` und `setChaosCreatures` (WorldProviderOreSpawn3-6.java:27).
- `OreSpawnMain.PlayNicely` (Config `PlayNicely`, Default 0, OreSpawnMain.java:1156) schaltet bei allen Angreifern die Zielsuche ab.
- SRG-Namen, die in `mcp/methods.csv` (stable-12) **nicht** vorkommen, sind aus der Verwendung erschlossen: `func_152115_b(String)` = Besitzer-UUID setzen, `func_152114_e(EntityLivingBase)` = ist Besitzer, `func_152113_b()` = Besitzer-UUID-String lesen, `func_145881_a()` = `MobSpawnerBaseLogic` des Spawners. Per methods.csv belegt sind `func_110163_bv` = `enablePersistence` und `func_150905_g` = `getHealAmount`.

---

### EntityRainbowAnt - Rainbow Ant (`rainbow_ant`)

- **Rolle:** Passive Portal-Ameise, `EntityAnt` → `EntityAnimal`. Ein Rechtsklick teleportiert in die Village-Dimension.
- **Werte:**
  - XP 0 (EntityRainbowAnt.java:15)
  - Leben 1 aus `EntityAnt.mygetMaxHealth` (EntityAnt.java:93-95)
  - Speed 0.15 aus `EntityAnt.moveSpeed` (EntityAnt.java:24); `onUpdate` setzt den Basiswert jeden Tick neu (EntityAnt.java:60-63)
  - Kein Schaden (EntityRainbowAnt.java:26)
- **KI und Angriffe:**
  - Prio 0 `EntityAIPanic(1.4)`, Prio 1 `MyEntityAIWanderALot(range 9, speed 1.0)` (EntityRainbowAnt.java:17-18)
  - Dieselben zwei Tasks trägt schon `EntityAnt` ein (EntityAnt.java:28-29). Die Tasks liegen also doppelt auf gleicher Priorität, das ist harmlos.
  - `MyEntityAIWanderALot` startet nur mit Chance 1/30 pro Tick und nicht beim Sitzen (MyEntityAIWanderALot.java:34-39).
  - Mit Chance 1/200 pro Tick wird das Rachziel gelöscht (EntityAnt.java:136-141).
- **Interaktion:**
  - Wirkt nur serverseitig (`EntityPlayerMP`) und nur mit **leerer Hand**. Ein Stack der Größe 0 wird vorher geleert (EntityRainbowAnt.java:31-44).
  - Außerhalb von `DimensionID3` geht es per `transferPlayerToDimension` mit `OreSpawnTeleporter` nach `DimensionID3`, sonst zurück nach Dimension 0 (EntityRainbowAnt.java:45-51).
  - Keine Zucht (`createChild` null, EntityAnt.java:123-125).
- **Drops:** keine (`dropFewItems` leer, EntityAnt.java:116-117).
- **Spawnen:**
  - Kein `addSpawn` (manifest: spawns leer).
  - Quellen:
    - `AntBlock.updateTick` im Standardzweig: nicht bei Regen, Luft darüber, `nextInt(6)+2` Ameisen je Zufallstick (AntBlock.java:46-81, Anzahl :54). Gleiches Muster in `CrystalAntBlock` (CrystalAntBlock.java:84).
    - Spawn-Ei
  - `getCanSpawnHere`: `posY >= 50` und höchstens 4 `EntityAnt` im AABB ±20/±10/±20 (EntityAnt.java:127-134).
  - `canDespawn` = `!isNoDespawnRequired()` (EntityAnt.java:56-58).
- **Zustand:** kein eigener DataWatcher und kein NBT. Die Textur `rainbow_ant.png` wählt `EntityAnt.getTexture` nach Klasse (EntityAnt.java:40-54).
- **Sounds:** keine, Lautstärke 0 (EntityAnt.java:97-114).
- **Config:** `RainbowedAntEnable` → `OreSpawnMain.RainbowAntEnable` (manifest), gelesen nur im `AntBlock`. `DimensionID3` stammt aus `BaseDimensionID`.
- **Portierung 1.21.1:**
  - `Animal` mit `mobInteract` nur auf `ServerPlayer` und nur bei `getItemInHand(hand).isEmpty()`.
  - Der Teleport läuft über `player.changeDimension(new DimensionTransition(...))` mit dem `ResourceKey<Level>` der Village-Dimension. `OreSpawnTeleporter` muss als eigene Portal-/Zielplatzsuche nachgebaut werden.
  - Die Spawn-Regel gehört in `checkSpawnRules` (Instanzmethode, Zugriff auf die Position) plus `SpawnPlacements.register`.
  - Die doppelten Goals nicht übernehmen.
  - Größe 0.1×0.1: `EntityDimensions.scalable(0.1f,0.1f)`.

### EntityRedAnt - Red Ant (`red_ant`)

- **Rolle:** Feindselige Ameise, `EntityAnt` → `EntityAnimal`. Sie beißt Spieler und teleportiert per Rechtsklick in die Mining-Dimension.
- **Werte:**
  - XP 1 (EntityRedAnt.java:20)
  - Leben 2 (EntityRedAnt.java:39-41)
  - Speed 0.2 (EntityRedAnt.java:19)
  - Attributschaden 1.0 (EntityRedAnt.java:35)
  - Echter Biss: 1.0 Schaden, aber nur mit Chance 1/15 je Aufruf und nie auf Friedlich (EntityRedAnt.java:43-52)
  - Eigener Biss-Takt `attack_delay` = 20 Ticks (EntityRedAnt.java:17, :91)
  - Forschungsnotiz 01-mobs.md nennt „Attack 2“; der Code sagt 1.0.
- **KI und Angriffe:**
  - Prio 0 `EntityAIPanic(1.4)`, Prio 1 `EntityAIAttackOnCollide(EntityPlayer, 1.0, false)`, Prio 2 `MyEntityAIWanderALot(10, 1.0)` (EntityRedAnt.java:22-24)
  - Nur wenn `PlayNicely == 0`: Target-Prio 1 `EntityAINearestAttackableTarget(EntityPlayer, chance 4, sichtbar)` (EntityRedAnt.java:25-27)
  - Zusätzlich in `onUpdate`: alle 20 Ticks, nicht auf Friedlich, `PlayNicely == 0` → nächster verwundbarer Spieler im Radius 1.5 → `attackEntityAsMob` (EntityRedAnt.java:79-102). Auch dort greift die 1/15-Chance.
  - Die geerbten Panic/Wander-Tasks aus `EntityAnt` bleiben zusätzlich aktiv (EntityAnt.java:28-29).
- **Interaktion:** leere Hand, `EntityPlayerMP` → nach `DimensionID2` („Dimension-Extreme“), aus `DimensionID2` zurück nach 0 (EntityRedAnt.java:54-77).
- **Drops:** keine (EntityAnt.java:116-117). XP 1.
- **Spawnen:**
  - `AntBlock` mit `MyRedAntBlock`, `nextInt(6)+2` je Tick (AntBlock.java:61-64), dasselbe in `CrystalAntBlock` (CrystalAntBlock.java:63-66).
  - Abbau des Blocks `RedAntTroll`: `15+nextInt(6)` Rote Ameisen (OreBasicStone.java:33-37).
  - Spawnbedingung und Despawn wie `EntityAnt` (EntityAnt.java:56-58, 127-134).
- **Zustand:** nur das transiente Feld `attack_delay`, kein DataWatcher, kein NBT.
- **Sounds:** keine (EntityAnt.java:97-114).
- **Config:** `RedAntEnable` (manifest, im `AntBlock`), `PlayNicely`, `DimensionID2`.
- **Portierung 1.21.1:**
  - `MeleeAttackGoal` plus `NearestAttackableTargetGoal<>(this, Player.class, 4, true, false, null)`.
  - `doHurtTarget` mit der 1/15-Sperre überschreiben.
  - Der Radius-Biss wird zu `level().getNearestPlayer(this, 1.5)` mit `EntitySelector.NO_CREATIVE_OR_SPECTATOR`.
  - `Animal` hat in 1.21.1 kein `ATTACK_DAMAGE`; es muss im `AttributeSupplier` ergänzt werden.
  - Die Zielauswahl muss `PlayNicely` beim Goal-Aufbau abfragen. Das Original liest es nur im Konstruktor, eine Config-Änderung wirkt also erst auf neue Entities.

### EntityThrownRock - EntityThrownRock (`entity_thrown_rock`)

- **Rolle:** Projektil, `EntityThrowable`. Es entsteht aus `ItemRock` (Rechtsklick, ItemRock.java:18-61) oder aus einem Dispenser (MyDispenserBehaviorRock.java:40-66).
- **Werte:** Der Typ `rock_type` bestimmt alles. Den Schaden verbucht `DamageSource.causePlayerDamage((EntityPlayer)getThrower())`, der Rückstoß ist `addVelocity(cos(f3)*ks, inair, sin(f3)*ks)` mit dem Winkel Werfer→Ziel. `inair` verdoppelt sich, wenn das Ziel schon tot ist.

| Typ | Item-Feld (`OreSpawnMain`) | Schaden | ks | inair | Zusatzeffekt | Zeilen |
|---|---|---|---|---|---|---|
| 1 | `MySmallRock` | 2 | 0.1 | 0.025 | – | :83-92 |
| 2 | `MyRock` | 5 | 0.2 | 0.025 | – | :93-102 |
| 3 | `MyRedRock` | 5 | 0.2 | 0.025 | `setFire(20)` | :103-113 |
| 4 | `MyGreenRock` | 5 | 0.2 | 0.025 | Poison 100 Ticks, Amp 0 | :114-126 |
| 5 | `MyBlueRock` | 10 | 0.1 | 0.025 | Slowness 100 Ticks | :127-139 |
| 6 | `MyPurpleRock` | 20 | 0.2 | 0.025 | Weakness 100 Ticks | :140-152 |
| 7 | `MySpikeyRock` | 40 | 0.2 | 0.025 | – | :153-162 |
| 8 | `MyTNTRock` | 40 | 0.5 | 0.055 | `newExplosion` Stärke 2.1, Feuer an, Blockschaden = Gamerule `mobGriefing` | :163-173 |
| 9 | `MyCrystalRedRock` | 150 | 0.2 | 0.025 | `setFire(50)` + Weakness 100 | :174-187 |
| 10 | `MyCrystalGreenRock` | 150 | 0.2 | 0.025 | Poison 200 + Weakness 100 | :188-203 |
| 11 | `MyCrystalBlueRock` | 150 | 0.2 | 0.025 | Slowness 200 + Weakness 100 | :204-219 |
| 12 | `MyCrystalTNTRock` | 250 | 0.2 | 0.025 | Weakness 100 + Explosion 5.1, Feuer an, `mobGriefing` | :220-233 |

(alle Zeilen EntityThrownRock.java)

- **KI und Angriffe:**
  - `onImpact` läuft nur serverseitig (EntityThrownRock.java:74-80). Entitätstreffer wirken nur, wenn `getThrower() != null` ist und das Ziel nicht der Werfer (EntityThrownRock.java:81-83).
  - Blocktreffer bei `rock_type != 0` (EntityThrownRock.java:235-294):
    - Im 3×3×3-Würfel um den Treffer werden `glass` und `glass_pane` zu Luft. Dazu kommt einmalig der Sound `orespawn:glassdead` 1.0/1.0 (EntityThrownRock.java:240-255).
    - Das passende Stein-Item wird wieder gedroppt (EntityThrownRock.java:256-293).
  - Danach folgt immer `setDead` (EntityThrownRock.java:295).
  - `onUpdate`:
    - Pitch-Drehung +30° je Tick (EntityThrownRock.java:303-307)
    - Lebensdauer 1000 Ticks (EntityThrownRock.java:308-311)
    - Typ-Sync über DW 20 (EntityThrownRock.java:312-317)
    - **Hüpfen über Wasser**: Block an der Position `Blocks.water` (nur stehendes Wasser), `-0.55 < motionY < -0.15` und horizontale Geschwindigkeit² > 0.5 → `motionY = -motionY*3/4`, xz ×3/4 (EntityThrownRock.java:318-323)
- **Interaktion:** keine. `ItemRock.onItemUse` platziert stattdessen eine `RockBase`-Entity („Rock“), das ist eine andere Klasse (ItemRock.java:64-113). Der Wurfsound `random.bow` 0.5 kommt aus dem Item (ItemRock.java:22).
- **Drops:** Nur bei Blocktreffer: 1 Item des eigenen Typs (EntityThrownRock.java:256-293). Bei Entitätstreffer gibt es keinen Drop.
- **Spawnen:** nicht natürlich. Tracking 64/1/true (OreSpawnMain.java:3120).
- **Zustand:** DW 20 = `rock_type` (Integer) (EntityThrownRock.java:56, 59-72). Kein NBT; `rock_type` geht beim Neuladen verloren und ist dann 0.
- **Sounds:** `orespawn:glassdead` (EntityThrownRock.java:249; sounds_dump: Varianten glassdead1, glassdead2).
- **Config:** Die Klasse liest keine. `RockEnable` (manifest) betrifft sie nicht direkt.
- **Portierung 1.21.1:**
  - Basis `ThrowableItemProjectile`, `EntityDataAccessor<Integer>` für den Typ, Textur je Typ im Renderer. Das Manifest listet 12 Texturen, und `RenderThrownRock` wählt über `getRockType` (RenderThrownRock.java:60-).
  - Schaden über `damageSources().thrown(this, owner)` oder `playerAttack`. Der harte Cast `(EntityPlayer)getThrower()` wird zu einer `instanceof Player`-Prüfung.
  - Explosion: `level().explode(null, x, y+0.25, z, 2.1f/5.1f, true, Level.ExplosionInteraction.MOB)`; MOB respektiert `mobGriefing`.
  - `setFire(n)` sind Sekunden → `igniteForSeconds(n)`.
  - Tränke: `MobEffects.POISON/MOVEMENT_SLOWDOWN/WEAKNESS`.
  - Fallen zum Entscheiden:
    - Ein Dispenser-Stein hat keinen Werfer und macht beim Entitätstreffer nichts.
    - Beim Speichern geht der Typ verloren.
    - Das Wasserhüpfen prüft nur stehendes Wasser (`Blocks.WATER` mit Level 0 bzw. `FluidState.isSource`).

### EntityUnstableAnt - Unstable Ant (`unstable_ant`)

- **Rolle:** Passive Portal-Ameise, `EntityAnt` → `EntityAnimal`, Teleport nach `DimensionID4`.
- **Werte:** XP 0 (EntityUnstableAnt.java:15), Leben 1 (EntityAnt.java:93-95), Speed 0.15 (EntityAnt.java:24), Schaden 0 (EntityUnstableAnt.java:26).
- **KI und Angriffe:** Panic 1.4 auf Prio 0, `MyEntityAIWanderALot(9, 1.0)` auf Prio 1 (EntityUnstableAnt.java:17-18), doppelt wie bei der Rainbow Ant (EntityAnt.java:28-29). Rachziel-Reset mit Chance 1/200 (EntityAnt.java:136-141).
- **Interaktion:** leere Hand, `EntityPlayerMP` → `DimensionID4` („Dimension-Islands“), von dort zurück nach 0 (EntityUnstableAnt.java:30-52). 01-mobs.md nennt das Ziel „Danger Dimension“; das ist dieselbe ID, nur ein anderer Name.
- **Drops:** keine (EntityAnt.java:116-117).
- **Spawnen:** `AntBlock` mit `MyUnstableAntBlock` (AntBlock.java:66-69), `CrystalAntBlock` (CrystalAntBlock.java:68-71), Spawn-Ei. Bedingungen und Despawn wie `EntityAnt` (EntityAnt.java:56-58, 127-134).
- **Zustand:** kein DataWatcher, kein NBT. Textur `unstableant.png` (EntityAnt.java:47-49, 147).
- **Sounds:** keine.
- **Config:** `UnstableAntEnable` (manifest, im `AntBlock`), `DimensionID4`.
- **Portierung 1.21.1:** wie `EntityRainbowAnt`, Ziel ist die Islands-Dimension. Die drei Portal-Ameisen sollten sich eine Basisklasse mit abstraktem `targetDimension()` teilen.

### Fairy - Fairy (`fairy`)

- **Rolle:** Fliegendes Umgebungswesen, `EntityAmbientCreature`. Die Fee jagt `EntityMob`s. Eine beschworene Fee folgt ihrem Besitzer, der über den Spielernamen gespeichert ist.
- **Werte:**
  - Leben 40 (Fairy.java:159-161), Rüstung 4 (Fairy.java:125-127)
  - Attributschaden 3.0 (Fairy.java:63), der echte Treffer macht aber **2.0** (Fairy.java:117-123). 01-mobs.md nennt 3, der Code verbucht 2.
  - Heilung +1 mit Chance 1/250 je KI-Tick (Fairy.java:290-292)
  - `renderDistanceWeight` 3.0 (Fairy.java:52)
  - Größe 0.4×0.8 (Fairy.java:47)
  - Kein Fallschaden (Fairy.java:309-313), löst keine Druckplatten aus (Fairy.java:315-317)
  - Keine Kollision mit Entities (Fairy.java:153-157)
  - XP: `experienceValue` wird nicht gesetzt, es gilt der Basisklassen-Default.
- **KI und Angriffe:**
  - Prio 0 `EntityAIWatchClosest(EntityLiving, 8.0)`, Prio 1 `EntityAILookIdle` (Fairy.java:53-54)
  - Die Flugsteuerung steckt handgeschrieben in `updateAITasks` (Fairy.java:243-303), in dieser Rangfolge:
    1. **Neues Flugziel**, wenn `rand(200)==0` oder Abstand² zum Ziel < 2.5. Bis zu 25 Versuche: x/z-Versatz `nextInt(8)` mit zufälligem Vorzeichen, y `nextInt(5)-2`. Das Ziel muss Luft sein und per Raytrace sichtbar (`canSeeTarget`) (Fairy.java:252-267, 239-241).
    2. **Sonst Angriff** mit Chance 1/12, nicht auf Friedlich. `findSomethingToAttack` sucht `EntityLivingBase` im AABB ±8, nach Distanz sortiert (`GenericTargetSorter`), und nimmt das erste `EntityMob`, das lebt und sichtbar ist (Fairy.java:216-237). Bei `PlayNicely != 0` gibt es kein Ziel (Fairy.java:221). Flugziel = Zielposition +1 y; bei Abstand² < 6 folgt der Treffer mit 2.0 (Fairy.java:269-278).
    3. **Sonst Besitzer folgen**: Spieler per Name. Abstand² > 64 → Flugziel beim Besitzer ±2. Abstand² > 256 → **Teleport** zum Besitzer (Fairy.java:279-289).
  - Steuerung:
    - `motionX += (sign(dx)*0.2 - motionX)*0.1`, y mit 0.7, z wie x
    - Yaw dreht um 1/4 der Differenz, `moveForward` 0.2 (Fairy.java:293-302)
    - In `onUpdate` zusätzlich `motionY *= 0.6` (Fairy.java:173)
- **Interaktion:**
  - Kein Rechtsklick.
  - Das Schwert `FairySword` beschwört beim Treffen `1+nextInt(3)` Feen und ruft `setOwner(Angreifer)` auf (FairySword.java:28-41).
  - `setOwner` speichert `getDisplayName()` des Spielers (Fairy.java:99-108).
- **Drops:** `getDropItem` = Blockitem `OreSpawnMain.CrystalTorch` (Fairy.java:163-165). Die Menge folgt der Vanilla-Regel von `EntityLiving.dropFewItems`, die in dieser Klasse nicht überschrieben ist.
- **Spawnen:**
  - `getCanSpawnHere`: mindestens 6 Luftblöcke im 3×3 auf Fußhöhe und `posY >= 50` (Fairy.java:319-330)
  - `canDespawn`: `!isNoDespawnRequired() && myowner == null` (Fairy.java:335-337)
  - Quellen:
    - roofedForest 25/2-4 ambient (manifest)
    - Crystal-Dimension 10/4-8 (BiomeGenUtopianPlains.java:133-135)
    - Chaos-Dimension 5/2-4 (BiomeGenUtopianPlains.java:290-292)
    - Abbau von `CrystalFairy`: `1+nextInt(6)` Feen (OreBasicStone.java:28-32)
    - Spawner in `Trees.FairyTree` (Trees.java:502) und `Trees.addSomething` (Trees.java:628)
- **Zustand:**
  - DW 20 = `fairy_type` 0..8, zufällig im Konstruktor (Fairy.java:49, 96), alle 10 Ticks synchronisiert (Fairy.java:178-187)
  - Textur je Typ: `fairytexture.png` bzw. `fairytexture2..9.png` (Fairy.java:66-92, 339-349)
  - NBT schreibt `MyOwner` (String, `"null"` für keinen) und `FairyType` (Fairy.java:198-205)
  - **NBT liest `fairyType` mit kleinem f** (Fairy.java:213). Der Typ fällt nach dem Laden also immer auf 0 zurück, ein Originalfehler.
  - `my_blink` = `20+rand(20)`, `blinker` zählt hoch (Fairy.java:46, 174-177). `getBlink()` gibt 240 zurück, solange `blinker < my_blink/2`, sonst 0 (Fairy.java:110-115).
- **Sounds:** kein Living-Sound; Hurt `orespawn:rat_hit`, Death `orespawn:big_splat`, Lautstärke 0.25, Pitch 1.7 (Fairy.java:129-147).
- **Config:** `FairyEnable` (manifest, OreSpawnMain.java:4633-4635), `PlayNicely`.
- **Portierung 1.21.1:**
  - Basis `AmbientCreature` (wie Bat) mit eigener Flugsteuerung in `customServerAiStep`, Goals `LookAtPlayerGoal`/`RandomLookAroundGoal`.
  - Besitzer als UUID statt Anzeigename speichern. Der Name ist nicht eindeutig und `getPlayerEntityByName` gibt es nicht mehr; es wird zu `level().getPlayerByUUID`.
  - Leuchten:
    - `ModelFairy` setzt den Lightmap-Wert `(getBlink(), 240)` (ModelFairy.java:135-136).
    - Im Renderer wird daraus `getBlockLightLevel` 15 bei Blink.
    - Der Blinkzähler ist clientlokal und nicht synchronisiert, das ist in Ordnung.
  - Partikel `fireworksSpark` nur nachts (Tageszeit mod 24000 ≥ 12000), clientseitig, Chance 1/5 bei Blink (Fairy.java:188-195) → `ParticleTypes.FIREWORK`.
  - NBT-Fehler: vorschlagsweise beheben und beide Schlüssel lesen.

### Firefly - Firefly (`firefly`)

- **Rolle:** Leuchtendes, harmloses Umgebungswesen, `EntityAmbientCreature`. Tagsüber verschwindet es.
- **Werte:** Leben 1 (Firefly.java:85-87), Größe 0.4×0.8 (Firefly.java:27), `renderDistanceWeight` 3.0 (Firefly.java:29). Kein Fallschaden, keine Druckplatten, keine Kollision (Firefly.java:79-83, 147-155).
- **KI und Angriffe:**
  - Keine Tasks. Flug in `updateAITasks` (Firefly.java:117-141):
    - Neues Ziel bei `rand(40)==0` oder Abstand² < 2. Versatz x/z `nextInt(4)-nextInt(4)`, y `nextInt(4)-2`. Bis zu 25 Versuche, bis das Ziel Luft ist (Firefly.java:126-130).
    - Steuerung wie `Fairy`: 0.2/0.7/0.2, Faktor 0.1, Yaw /4, `moveForward` 0.2 (Firefly.java:131-140).
  - `onUpdate`:
    - `motionY *= 0.6` (Firefly.java:99), Blinkzähler (Firefly.java:100-103)
    - Ohne Persistenz und bei Tageszeit mod 24000 ≤ 11000 verschwindet es mit Chance 1/500 je Tick per `setDead`, ganz ohne Drop (Firefly.java:104-114).
- **Interaktion:** keine.
- **Drops:** `getDropItem` = Blockitem `OreSpawnMain.ExtremeTorch` (Firefly.java:89-91); Menge nach Vanilla-Regel.
- **Spawnen:**
  - `getCanSpawnHere`: Block an der Position ist Luft, `!isDaytime()`, höchstens 10 Fireflies im AABB ±20/±8/±20, und entweder `dimensionId == DimensionID4` oder `posY >= 50` (Firefly.java:157-165)
  - `canDespawn`: `isDaytime() && !isNoDespawnRequired()` (Firefly.java:170-172)
  - Quellen:
    - Overworld-Biome laut manifest (OreSpawnMain.java:4350-4364)
    - Utopia 15/3-6 (BiomeGenUtopianPlains.java:13-15), Islands 10/4-8 (:80-82), Village 10/3-6 (:217-219), Chaos 15/3-6 (:278-280)
    - `BlockFireflyPlant`: Zufallstick, nicht bei Regen, Rate `6 - (meta&7)`, Luft darüber, nachts → `2+rand(5)` Fireflies (BlockFireflyPlant.java:27-42)
- **Zustand:** keiner. `getBlink()` 240 bzw. 0 wie bei der Fee (Firefly.java:48-53) → `ModelFirefly` Lightmap (ModelFirefly.java:110-111).
- **Sounds:** keine, Lautstärke 0 (Firefly.java:55-73).
- **Config:** `FireflyEnable` (manifest; auch in `BlockFireflyPlant`).
- **Portierung 1.21.1:**
  - `AmbientCreature`, Leuchten per `getBlockLightLevel`.
  - Tagsüber `discard()` statt Tod, also ohne Loot.
  - `isDaytime` (1.7.10: `skylightSubtracted < 4`) → `level().isDay()`.
  - Die Spawnprüfung muss den Dimensionsschlüssel der Islands-Dimension kennen.
  - `MyUtils.isIgnoreable` und `Frog` (als Beute) referenzieren diese Klasse.

### Flounder - Flounder (`flounder`)

- **Rolle:** Passiver Plattfisch, `EntityAnimal`. Er lebt amphibisch: an Land sucht er Wasser und verliert dabei Leben.
- **Werte:** XP 5 (Flounder.java:31), Leben 5 (Flounder.java:67-69), Speed 0.25 (Flounder.java:23, 29; wird in `onUpdate` neu gesetzt, :54-57), `fireResistance` 15 (Flounder.java:30), atmet unter Wasser (Flounder.java:63-65), Größe 0.55×0.25 (Flounder.java:28).
- **KI und Angriffe:**
  - Prio 0 Swimming, 1 `EntityAIMate(1.0)`, 3 `EntityAIAvoidEntity(EntityPlayer, 8.0, 1.0, 1.4)`, 4 Panic 1.5, 5 `WatchClosest(Player, 12)`, 6 `MyEntityAIWander(1.0)`, 7 LookIdle (Flounder.java:33-39)
  - `updateAITick` (Flounder.java:183-226):
    - Rachziel-Reset mit Chance 1/200 (:188-190)
    - **An Land**, Chance 1/20: Schalen-Scan nach `water`/`flowing_water` mit `scan_it` (Flounder.java:100-181). Radius i = 1..10, Höhe j = min(i,4), ab i ≥ 5 in Zweierschritten. Ursprung ist y-1. Danach Pfad zu (tx, ty-1, tz) mit Speed 1.0 (:191-211).
    - Kein Wasser gefunden: Chance 1/25 auf `heal(-1)`, also 1 Leben ohne Schadensereignis. Bei ≤ 0 Leben `setDead` **ohne Drops** (:212-220).
    - **Im Wasser**, Chance 1/50: Sound `splash` 1.0, Pitch 0.9-1.1, `heal(1)` (:222-225)
- **Interaktion:**
  - Zucht mit `OreSpawnMain.MyCrystalApple` (`isBreedingItem`, Flounder.java:257-259)
  - `isWheat(Items.fish)` (Flounder.java:253-255) ist eine eigene Methode, die Vanilla nie aufruft; ohne Wirkung
  - `createChild` → neuer `Flounder` (Flounder.java:245-251)
- **Drops:** 1-2 `Items.fish` (`nextInt(2)+1`, Flounder.java:91-98). XP 5.
- **Spawnen:**
  - `getCanSpawnHere`: `posY >= 50`, `isDaytime()`, Chance 1/20, höchstens 10 Flounder im AABB ±16/±8/±16 (Flounder.java:233-235)
  - `canDespawn`: Kind → `enablePersistence` und false, sonst `!isNoDespawnRequired()` (Flounder.java:237-243)
  - Quellen: kein Overworld-`addSpawn` (manifest leer). Utopia-Wasserliste 2/2-4 (BiomeGenUtopianPlains.java:45-47), Crystal-Wasserliste 5/6-8 (:172-174). 01-mobs.md „Crystal Dim.“ passt dazu.
- **Zustand:** kein DataWatcher, kein eigenes NBT; die Scan-Felder sind transient.
- **Sounds:**
  - Living `splash` (Vanilla-Name, :72)
  - Hurt **`little_splat` ohne `orespawn:`-Präfix** (:76). sounds_dump führt Flounder bei `little_splat` nicht, im Original ist der Sound vermutlich stumm.
  - Death `orespawn:ratdead` (:80), Lautstärke 0.4 (:84)
- **Config:** `FlounderEnable` (manifest, nur in `BiomeGenUtopianPlains`).
- **Portierung 1.21.1:**
  - `Animal` mit `WaterBoundPathNavigation`/`AmphibiousPathNavigation`, damit das Tier an Land und im Wasser läuft. `canBreatheUnderwater` → `canBreatheUnderwater()` bzw. `MobType`-frei über `isAlwaysBreathing` / Luftlogik.
  - `heal(-1)` wird zu `setHealth(getHealth()-1)`; der Tod über `discard()` bleibt ohne Loot.
  - Spawn in `MobCategory.WATER_CREATURE` bei gleichzeitiger Tageslicht- und Höhenregel bewusst übernehmen.
  - Zuchtitem `isFood(stack)` = Crystal Apple.
  - Hurt-Sound: Präfix ergänzen oder stumm lassen, das ist offen.

### Frog - Frog (`frog`)

- **Rolle:** Springendes Tier, `EntityAnimal`. Es frisst Kleingetier. Wer den Frosch küsst (schleichen, leere Hand), verwandelt ihn in Prinz oder Prinzessin.
- **Werte:**
  - XP 5 (Frog.java:28), Leben 8 (Frog.java:131-133), Speed 0.1 (Frog.java:24)
  - Bissschaden 3.0; tötet der Biss, heilt der Frosch 1 (Frog.java:177-183). 01-mobs.md „3“ passt.
  - Größe 0.75×0.75 (Frog.java:27), atmet unter Wasser (Frog.java:49-51), kein Fallschaden, kein Schrittsound (Frog.java:157-164)
  - Sprung-Cooldown 50 Ticks nach einem Zufallssprung (Frog.java:88-91), 25 Ticks nach Treffer (Frog.java:188-191)
- **KI und Angriffe:**
  - Prio 0 Swimming, 1 Panic 1.4, 2 `MyEntityAIWander(1.0)` (Frog.java:31-33)
  - `jumpAround`: `motionY += 0.75 + |rand*0.55|`, `posY += 0.35`, horizontal `f = 0.7 + |rand*0.75|` in Blickrichtung (Frog.java:65-73). Serverseitig in `onUpdate` mit Chance 1/70, sobald `jumpcount == 0` (Frog.java:78-92), und bei jedem erlittenen Schaden (Frog.java:185-193).
  - Jagd in `updateAITasks`: Chance 1/12, nicht auf Friedlich → `findSomethingToAttack` im AABB ±8/±3/±8, nach Distanz sortiert (Frog.java:240-257)
    - Beute ist das erste sichtbare `EntityAnt` (auch Termite, die davon erbt), `EntityButterfly`, `Cricket`, `EntityMosquito`, `Firefly` oder `WormSmall` (Frog.java:236-238).
    - Pfad Speed 1.25; bei Abstand² < 6 Biss (Frog.java:224-233).
    - `PlayNicely != 0` → keine Jagd (Frog.java:241-243).
- **Interaktion:** Schleichen + leere Hand (Frog.java:95-125):
  - Beidseitig: `setDead()` und Sound `random.explode` 1.0 am Spieler.
  - **Server**: 50 % `Boyfriend` mit `setPrince(1+rand(2))`, sonst `Girlfriend` mit `setPrincess(1+rand(2))`, gespawnt an der Froschposition +0.01 y (Frog.java:100-115). `spawnCreature` spielt dabei den Living-Sound (Frog.java:259-268).
  - **Client**: 16× `smoke`, `explode` und `reddust`.
  - Rückgabe false. Keine Zucht (`createChild` null, Frog.java:203-205).
- **Drops:** 4× `slime_ball`, verteilt auf ±1 Block (Frog.java:166-175). XP 5.
- **Spawnen:**
  - `getCanSpawnHere`: `posY >= 50`, `isDaytime()`, in `DimensionID5` (Crystal) nur mit Chance 1/20, höchstens 5 Frösche im AABB ±20/±8/±20 (Frog.java:212-214)
  - `canDespawn` = `!isNoDespawnRequired()` (Frog.java:53-55)
  - Quellen:
    - Overworld laut manifest (OreSpawnMain.java:4622-4628)
    - Utopia-Wasser 5/4-6 (BiomeGenUtopianPlains.java:54-56), Crystal-Wasser 1/3-5 (:181-183)
    - Spawner in `GenericDungeon.makeFrogPond` (GenericDungeon.java:6061)
- **Zustand:** DW 20 = `singing` (Integer), gesetzt auf 35 Ticks, wenn der Living-Sound spielt (Frog.java:46, 57-63, 140). Serverseitiger Countdown (Frog.java:79-84). `ModelFrog` liest den Wert (ModelFrog.java:93). Kein NBT.
- **Sounds:** Living `orespawn:frog`, serverseitig nur mit Chance 1/2 (Frog.java:135-143); Hurt `orespawn:scorpion_hit`, Death `orespawn:big_splat`, Lautstärke 0.7 (Frog.java:145-155).
- **Config:** `FrogEnable` (manifest), `PlayNicely`, `DimensionID5`.
- **Portierung 1.21.1:**
  - Java-Namenskonflikt mit `net.minecraft.world.entity.animal.frog.Frog`: vollqualifiziert importieren oder die Klasse `OreFrog` nennen. Die Registry-ID `orespawn:frog` kollidiert nicht.
  - Der Kuss braucht die registrierten Typen `boyfriend`/`girlfriend` und ein `setPrince`/`setPrincess`, das auch synchronisiert.
  - Die Jagd als eigenes Goal oder in `customServerAiStep`.
  - Sprünge über `setDeltaMovement`; das `posY += 0.35` wird zu `setPos`.
  - `playLivingSound` beim Kuss → `playAmbientSound()`.

### GammaMetroid - WTF? (`wtf`)

- **Rolle:** Zähmbares Höhlenmonster, `EntityTameable`. Wild greift es Spieler und Tiere an (keine `EntityMob`s) und frisst Stein.
- **Werte:**
  - XP 20 (GammaMetroid.java:37), `fireResistance` 1000 (GammaMetroid.java:38), Speed 0.15 (GammaMetroid.java:29, 34), Größe 1.5×1.5 (GammaMetroid.java:35)
  - Schaden = `GammaMetroid_stats.attack` (GammaMetroid.java:66-69), Leben = `.health` (:158-160), Rüstung = `.defense` (:162-164); Defaults 100/10/12 (manifest)
  - Nahkampf-Trefferchance je Versuch: `nextInt(4)==0 || nextInt(5)==1`, also etwa 40 % (GammaMetroid.java:225)
  - Heilung +1 je Stein-Mahlzeit (GammaMetroid.java:463)
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task |
    |---|---|
    | 0 | Swimming |
    | 1 | `EntityAIMate(1.0)` |
    | 2 | `MyEntityAIFollowOwner(speed 2.0, maxDist 10, minDist 2)` |
    | 3 | `EntityAITempt(1.2, iron_ingot)` |
    | 4 | `MyEntityAIWanderALot(16, 1.0)` |
    | 5 | `WatchClosest(Player, 8)` |
    | 6 | LookIdle |

    Target-Prio 1 `EntityAIHurtByTarget` (GammaMetroid.java:40-47).
  - `MyEntityAIFollowOwner` folgt nur, wenn (`posY < 60` oder Nacht) und Abstand > maxDist/2 gilt, oder wenn Abstand ≥ maxDist (MyEntityAIFollowOwner.java:38).
  - Angriff in `updateAITasks`: nicht auf Friedlich, Chance 1/5 (GammaMetroid.java:215-234)
    - `findSomethingToAttack` im AABB ±10/±3/±10, sortiert. Nie für Kinder und nie bei `PlayNicely` (:274-294).
    - Ausgeschlossen sind: `MyUtils.isIgnoreable` (Ameisen, Schmetterlinge, Mücken, Libellen, Fireflies, Grillen, Cockateils, Termiten, Geister, Elevator, RockBase; MyUtils.java:17-19), Unsichtbares, andere GammaMetroids, `EntityMob`, **jedes Ziel, wenn selbst gezähmt**, Spieler im Kreativmodus (:236-272).
    - `faceEntity`; bei Abstand² ≤ 9 Trefferversuch, sonst Pfad Speed 1.25.
  - Steinfressen in `updateAITick` (GammaMetroid.java:434-468):
    - Auslöser: `((rand(20)==0 && Leben<max) || rand(100)==0)`, `PlayNicely == 0`, nicht sitzend
    - Schalen-Scan nach `Blocks.stone` ab y+1, Radius 1..5, Höhe min(i,2), ab i ≥ 4 in Zweierschritten (:445-456, `scan_it` :351-432)
    - Pfad Speed 1.0. Bei Distanz² < 12 vom Scan-Ursprung: Block zu Luft (nur mit `mobGriefing`), `heal(1)`, Sound `random.burp` 0.5, Pitch 1.5-1.7 (:457-466)
  - `isValidLightLevel` im Stil von `EntityMob`: Himmelslicht > `rand(32)` → nein; Blocklicht (bei Gewitter mit `skylightSubtracted=10`) ≤ `rand(8)` (:296-311)
- **Interaktion:** zuerst `super.interact`, also Vanilla-Zucht mit `MyCrystalApple` (GammaMetroid.java:77-79, 487-489). Dann:
  - `iron_ingot`, Abstand² < 25: wild → serverseitig 1/3 zähmen (Besitzer-UUID, Herzen, Status 7, volle Heilung), sonst Rauch (Status 6). Gezähmt beim Besitzer → volle Heilung. Das Item wird verbraucht (:80-113).
  - Gezähmt + Besitzer + `deadbush`, Abstand² < 25 → auswildern (:114-129).
  - Gezähmt + Besitzer + `name_tag`, Abstand² < 16 → Name setzen (:130-140).
  - Gezähmt + Besitzer, Abstand² < 25 → Sitzen umschalten (:141-149).
  - `isWheat(iron_ingot)` (:483-485) ist ungenutzt.
  - Zucht: Ist das Elternteil gezähmt, ruft `spawnBabyAnimal` `this.func_152115_b(this.func_152113_b())` auf, setzt also den **eigenen** Besitzer neu, und `w.setTamed(true)`. **Das Baby ist gezähmt, hat aber keinen Besitzer** (:474-481). Originalfehler.
- **Drops:** `dropFewItems` ist überschrieben: `5+rand(10)` Goldnuggets und `6+rand(10)` Eisenbarren, verteilt auf ±3 (GammaMetroid.java:201-213). `getDropItem` (Eisen, :197-199) wird dadurch nicht benutzt. XP 20.
- **Spawnen:**
  - `getCanSpawnHere` (GammaMetroid.java:313-349):
    1. Ein Spawner mit Entity-Name `"WTF?"` im Bereich x/z -3..2, y 0..4 → immer erlaubt.
    2. Sonst muss `isValidLightLevel` passen.
    3. In `DimensionID4` → erlaubt.
    4. `posY > 50` → verboten.
    5. Die 2×2-Säule y+1..+3 muss Luft sein.
  - `canDespawn`: Kind → Persistenz und false; sonst `!isTamed() && !isNoDespawnRequired()` (:58-64).
  - Quellen:
    - Mining-Dimension (`ChunkProviderOreSpawn2`) 35/4-7 (ChunkProviderOreSpawn2.java:369-371)
    - Chaos 1/1-1 Monster (BiomeGenUtopianPlains.java:433-435)
    - Spawner in `GenericDungeon.makeDungeon` (GenericDungeon.java:184) und `makePart` (:1786, :1797)
    - Kein Overworld-`addSpawn` (manifest leer)
- **Zustand:** nur Vanilla-Tameable-Daten (gezähmt, sitzend, Besitzer). Kein eigenes NBT.
- **Sounds:** Living `orespawn:wtf_living` mit Chance 1/5 (:174-179), Hurt `orespawn:duck_hurt`, Death `orespawn:alo_death`, Lautstärke 1.5, Pitch 1.0 (:181-195).
- **Config:** `GammaMetroid_health/_attack/_defense` (100/10/12, manifest), `GammaMetroidEnable`, `PlayNicely`, `DimensionID4`.
- **Portierung 1.21.1:**
  - Basis `TamableAnimal`, Sitzen über `setOrderedToSit` + `SitWhenOrderedToGoal`. Das Original hat keinen Sit-Task; das Sitzen wirkt dort nur, wo eigene Tasks es abfragen.
  - Die Spawner-Prüfung liest `SpawnerBlockEntity.getSpawner().nextSpawnData` und vergleicht den Entity-Typ `orespawn:wtf` statt eines Strings.
  - Die Lichtregel wird zu `Monster.isDarkEnoughToSpawn`-Logik, obwohl die Basisklasse ein Tier ist.
  - Steinfressen: `Blocks.STONE` (nur Stein, nicht Deepslate/Andesit usw.) und `EventHooks.canEntityGrief`.
  - Baby-Besitzer-Fehler: vorschlagsweise den Besitzer übernehmen.
  - Registry-ID `wtf` statt `"WTF?"`.

### Gazelle - Gazelle (`gazelle`)

- **Rolle:** Scheues, zähmbares Tier, `EntityTameable`. Es frisst Pflanzen und Feldfrüchte.
- **Werte:**
  - XP 5 (Gazelle.java:37), Leben 15 (Gazelle.java:241-243), Speed 0.3 (Gazelle.java:33 überschreibt 0.2 aus :26), `fireResistance` 100 (:34), Größe 0.6×1.8 (:32)
  - Fallschaden `ceil(Fallhöhe-3)`, **gedeckelt auf 2** (Gazelle.java:153-167)
  - Gezähmt wird eingehender Schaden auf 10 gedeckelt (Gazelle.java:372-380)
  - Heilung +1 mit Chance 1/250 je Tick (Gazelle.java:213-215) und je Mahlzeit (:201)
- **KI und Angriffe:** kein Angriff.
  - Tasks:

    | Prio | Task |
    |---|---|
    | 0 | Swimming |
    | 1 | Mate 1.0 |
    | 2 | `MyEntityAIFollowOwner(2.0, 10, 2)` |
    | 3 | `AvoidEntity(EntityMob, 8, 1.0, 1.7)` |
    | 4 | `Tempt(1.2, apple)` |
    | 5 | Panic 1.5 |
    | 6 | `AvoidEntity(EntityPlayer, 12, 1.0, 2.0)` |
    | 7 | `WatchClosest(Player, 6)` |
    | 8 | `MyEntityAIWander(1.0)` |
    | 9 | LookIdle |
    | 10 | `EntityAIMoveIndoors` |

    (Gazelle.java:39-49)
  - `updateAITick` (Gazelle.java:169-217):
    - Rachziel-Reset mit Chance 1/200
    - Nicht sitzend, Auslöser `((rand(30)==0 && Leben<max) || rand(750)==1)`, `PlayNicely == 0`: Scan nach `MyStrawberryPlant`, `potatoes`, `carrots`, `tallgrass`, `double_plant` ab y+1, Radius 1..10, Höhe min(i,2), ab i ≥ 6 in Zweierschritten (:177-194, `scan_it` :70-151)
    - Pfad; bei Distanz² < 12 wird die Pflanze (nur mit `mobGriefing`) zu Luft, `heal(1)`, `random.burp` 1.0 (:195-204)
    - Mit Chance 1/250 läuft die Gazelle zur „nächsten“ Gazelle, Speed 0.5 (:206-211). **`findBuddy` liefert das erste Element der nach Distanz sortierten Liste, und das ist die Gazelle selbst**, weil die AABB-Suche den Aufrufer nicht ausschließt (:219-231). Die Gazelle läuft also auf die eigene Position.
- **Interaktion:** zuerst `super.interact` (Zucht mit `MyCrystalApple`, :409-411). Dann:
  - `apple`, Abstand² < 16: wild → **1/2** zähmen, sonst Rauch; gezähmt beim Besitzer → volle Heilung; Item verbraucht (:258-291)
  - Gezähmt + Besitzer + `deadbush` → auswildern (:292-307); `name_tag` → Name (:308-318); leere Interaktion → Sitzen umschalten (:319-327)
  - `isWheat(apple)` ist ungenutzt (:405-407)
- **Drops:** gezähmt `2+rand(5)` `red_flower` (Mohn) (Gazelle.java:356-362); wild Vanilla-`dropFewItems` mit `getDropItem` = `Items.beef` (:350-352, 363-365). XP 5.
- **Spawnen:**
  - `getCanSpawnHere`: `50 ≤ posY ≤ 100`, Block darunter `dirt`, `grass` oder `tallgrass` (:382-391)
  - `canDespawn` = false (:393-395)
  - Quellen: nur die Utopia-Grundliste 10/2-4 creature (BiomeGenUtopianPlains.java:10-12), dazu Spawn-Ei und Critter-Käfig. 01-mobs.md nennt „Plains“; im Code gibt es kein Overworld-`addSpawn`.
- **Zustand:** Vanilla-Tameable (gezähmt, sitzend, Besitzer); kein eigenes NBT.
- **Sounds:** kein Living-Sound (:331-336); Hurt `orespawn:scorpion_hit`, Death `orespawn:cryo_death`, Lautstärke 0.4, Pitch Kind 1.5±0.1 / erwachsen 1.0±0.1 (:338-370).
- **Config:** `GazelleEnable` (manifest, nur im Biom), `PlayNicely`.
- **Portierung 1.21.1:**
  - `TamableAnimal`.
  - `EntityAIMoveIndoors` hat in 1.21.1 kein Goal-Gegenstück (Dorfbewohner nutzen Brain-Behaviours). Es braucht ein eigenes Goal oder entfällt, das ist zu entscheiden.
  - `AvoidEntityGoal<Player>` braucht bei Gezähmten ein Prädikat.
  - Pflanzenfressen mit `EventHooks.canEntityGrief`. Die Blocknamen werden zu `SHORT_GRASS`, `TALL_GRASS`/`LARGE_FERN` usw.
  - Schadensdeckel in `hurt()`.
  - `findBuddy`-Fehler: vorschlagsweise sich selbst ausschließen.
  - Kinder-Skalierung ½ macht der Renderer (RenderGazelle.java:35-39).

### Ghost - Ghost (`ghost`)

- **Rolle:** Harmloses, durch Wände schwebendes Umgebungswesen, `EntityAmbientCreature`, nachtaktiv.
- **Werte:** Leben 2 (Ghost.java:74-76), XP 5 (Ghost.java:21), `noClip = true` (Ghost.java:22), Größe 0.5×1.5 (Ghost.java:19). Nicht schiebbar, keine Kollisionen (:64-72), kein Fallschaden, keine Druckplatten (:138-146). Ersticken in Blöcken (`inWall`) wird ignoriert (:170-177).
- **KI und Angriffe:** keine Tasks. Flug in `updateAITasks` (Ghost.java:90-132):
  - Neues Ziel bei `rand(40)==1` oder Abstand² < 2.
  - Mit einem Spieler im Umkreis ±16: Ziel = Spielerposition ±2 in x/z, y+1 (:102-105).
  - Sonst: `i` = erste Luft über dem Geist (0..2), `j` = erster fester Block darunter (-1..-3). Ziel x±9, `y+i+j+rand(4)+1`, z±9 (:106-120).
  - Steuerung: x/z `sign*0.1` mit Faktor 0.05, y `sign*0.7` mit Faktor 0.1, Yaw /6, `moveForward` 0.05 (:122-131). `onUpdate` dämpft `motionY *= 0.65` (:87).
  - Ist die Entity persistent (`isNoDespawnRequired`, z. B. mit Namensschild), wird `noClip` abgeschaltet (:83-85).
- **Interaktion:** keine.
- **Drops:** keine (kein `getDropItem`). XP 5.
- **Spawnen:**
  - `getCanSpawnHere`: Spawner `"Ghost"` im Bereich x/z -2..1, y 0..4 → erlaubt; sonst `!isDaytime()` (:148-165)
  - `canDespawn` = `!isNoDespawnRequired()` (:37-39)
  - Quellen:
    - `GhostEnable`: coldTaiga 15/5-10, taigaHills 10/5-10, frozenRiver 6/4-6, jungle 2/1-4, roofedForest 15/2-5 (OreSpawnMain.java:4443-4449)
    - **Nur am 31. Oktober** (`GregorianCalendar` Monat 9, Tag 31, einmal beim Mod-Start ausgewertet) zusätzlich 22 Overworld-Biome je 15/3-6 ohne Enable-Schalter (OreSpawnMain.java:4178-4181, 4204-4226). Das Manifest führt diese Einträge ohne Datumsbedingung.
    - Spawner in `GenericDungeon.makeHauntedHouse` (GenericDungeon.java:1046), `makeIgloo` (:2773), `makeCrystalHauntedHouse` (:3108) und `makeincagrave` (:4045, :4074)
- **Zustand:** keiner.
- **Sounds:** Living `orespawn:ghost_sound` mit Chance 1/2, Lautstärke 0.3, Pitch 1.5; kein Hurt-/Death-Sound (:41-62).
- **Config:** `GhostEnable` (manifest).
- **Portierung 1.21.1:**
  - `AmbientCreature` mit `noPhysics = true` und `isNoGravity`-artiger eigener Steuerung.
  - `inWall` → `damageSources().inWall()` bzw. `DamageTypeTags`-Prüfung auf `minecraft:in_wall`.
  - Die Halloween-Spawns werden zu einem Biome-Modifier, der zur Laufzeit das Datum prüft (Biome-Modifier sind statisch), oder zu einer Bedingung in `checkSpawnRules` + `SpawnPlacementRegisterEvent`, zu entscheiden.
  - Spawner-Prüfung wie beim WTF?.
  - Transparenz im Modell: `ModelGhost` nutzt Blend/`glColor4f` (anim_summary.txt:45) → `RenderType.entityTranslucent`.

### GhostSkelly - Ghost Pumpkin Skelly (`ghost_pumpkin_skelly`)

- **Rolle:** Die größere Geister-Variante mit Kettenrasseln, `EntityAmbientCreature`, harmlos.
- **Werte:** Leben 5 (GhostSkelly.java:102-104), XP 10 (:23), `noClip = true` (:24), Größe 1.5×2.0 (:21); `inWall` wird ignoriert (:199-206).
- **KI und Angriffe:** identisch mit `Ghost` (GhostSkelly.java:118-161: Zielwahl 1/40 bzw. Abstand² < 2, Spieler ±16, Versatz ±9, Steuerung 0.1/0.05 und 0.7/0.1, Yaw /6). `motionY *= 0.65` und `noClip` aus bei Persistenz (:110-116).
- **Interaktion:** keine.
- **Drops:** keine. XP 10.
- **Spawnen:**
  - `getCanSpawnHere`: Spawner `"Ghost Pumpkin Skelly"` im Bereich x/z -2..1, y 0..4 oder Nacht (:177-194)
  - `canDespawn` = `!isNoDespawnRequired()` (:65-67)
  - Quellen:
    - `GhostSkellyEnable`: dieselben fünf Biome und Gewichte wie `Ghost` (OreSpawnMain.java:4450-4456)
    - Am 31. Oktober 22 Biome je 15/3-6 (OreSpawnMain.java:4181-4203)
    - Spawner in `GenericDungeon.makeHauntedHouse` (:1051), `makeIgloo` (:2778), `makeCrystalHauntedHouse` (:3113), `makePumpkin` (:6209, :6215)
- **Zustand:** Nur ein clientseitiger Animations-Notizzettel `RenderInfo renderdata` (rf1-4, ri1-4) liegt in der Entity (GhostSkelly.java:15, 35-63). Kein DataWatcher, kein NBT.
- **Sounds:** Living `orespawn:chain_rattles` mit Chance 1/2, Lautstärke 0.5, Pitch 1.5 (:69-90).
- **Config:** `GhostSkellyEnable` (manifest).
- **Portierung 1.21.1:**
  - Mit `Ghost` eine gemeinsame Basisklasse bilden.
  - `RenderInfo` darf als reines Datenobjekt ohne Client-Importe in der Entity bleiben, sonst in eine clientseitige `WeakHashMap<Entity, RenderInfo>` im Paket `com.swbr.orespawn.client` verschieben.
  - Transparenz wie `Ghost` (anim_summary.txt:46).

### GiantRobot - Jeffery (`jeffery`)

- **Rolle:** Riesiger Roboter-Boss, `EntityMob`. Er schießt Laserkugeln und schleudert im Nahkampf.
- **Werte:**
  - XP = `Jeffery_stats.health / 2`, Default 275 (GiantRobot.java:30; health 550 manifest)
  - Leben, Schaden und Rüstung aus `Jeffery_stats` (:45-47, 78-88), Defaults 550/40/18 (manifest)
  - `fireResistance` 40, `isImmuneToFire` (:31-32), Speed 0.55 (:27), Größe 3.0×9.75 (:28), Tracking 128 (OreSpawnMain.java:4161)
  - Extra-Sprungkraft `motionY += 0.25` (:98-101)
  - Nahkampf-Rückstoß horizontal 2.2, vertikal 0.25, verdoppelt bei totem Ziel oder Spieler (:196-210)
  - Laser-Nachladezeit 10 Ticks (nah) bzw. 25 Ticks (fern, Spezialschuss) (:257, :261)
  - Laserkugel: 16 Schaden + `setFire(1)` (LaserBall.java:97, 153-156), Lebensdauer 200 Ticks (LaserBall.java:181), trifft keine Roboter (Robot2-5, GiantRobot) (LaserBall.java:113-133). Der Spezialschuss explodiert beim Aufprall mit Stärke 3.0 und `mobGriefing` (LaserBall.java:172-174).
  - Kakteen-Schaden wird ignoriert (:288-290).
  - 550 Leben liegen unter der Klemme von 1024 und 18 Rüstung unter 30; keine virtuelle Gesundheit nötig.
- **KI und Angriffe:**
  - Prio 0 Swimming, 1 `MyEntityAIWanderALot(14, 1.0)`, 2 `EntityAIMoveThroughVillage(0.9, false)`, 3 `WatchClosest(Player, 8)`, 4 LookIdle; Target-Prio 1 `HurtByTarget` (:35-40)
  - `updateAITasks` (:212-284):
    - `reload_ticker` sinkt jeden Tick.
    - Mit Chance 1/5 pro Tick:
      - Mit Chance 1/100 wird das Ziel gelöscht, ein totes Ziel ebenfalls.
      - Ohne Ziel `findSomethingToAttack`: AABB ±16/±12/±16, sortiert. Ausgeschlossen sind `isIgnoreable`, Unsichtbares, `EntityMob` und Kreativspieler; bei `PlayNicely` gibt es kein Ziel (:299-341).
      - `faceEntity`.
      - Liegt das Ziel innerhalb Abstand² < 256 **und** weicht die Blickrichtung (Kopf-Yaw) weniger als 0.5 rad zur Zielrichtung ab:
        - `reload_ticker == 0` → `LaserBall` ab Kopfhöhe +10, seitlich 3.75 versetzt. Kurs zum Ziel mit Bogen `0.2 × horizontale Distanz`, Tempo 2.0, Streuung 4.0 (:245-254).
        - Abstand² > 100 → `setSpecial()`, Nachladen 25, Sound `fireworks.launch` 3.5/0.5.
        - Sonst Nachladen 10, Sound `fireworks.launch` 2.5/1.0 (:255-264).
        - Abstand² < (8 + Zielbreite/2)² → `setAttacking(1)` + Nahkampf, sonst `setAttacking(0)` (:266-272).
        - Pfad zum Ziel, Speed 0.5 (:274).
      - Außerhalb von 16 Blöcken oder ohne Ziel: `setAttacking(0)`.
  - Einen eigenen Nahkampf-Cooldown gibt es nicht; es wirkt nur die 1/5-Chance je Tick und die Unverwundbarkeitszeit des Ziels.
  - `attackEntityFrom`: Ein Angreifer vom Typ `EntityLiving` wird sofort Ziel (:291-295). Spieler sind in 1.7.10 kein `EntityLiving`; für sie greift nur `HurtByTarget`.
- **Interaktion:** keine (`interact` gibt false zurück, :192-194).
- **Drops** (:140-190):
  - `15+rand(15)`-mal ein Stapel von 4 `MyLaserBall`
  - dann `10+rand(10)` Würfe mit `rand(12)`:

    | Wurf | Drop |
    |---|---|
    | 0 | `SpiderRobotKit` |
    | 1 | `AntRobotKit` |
    | 2 | `MyRayGun` |
    | 3 | `redstone_block` |
    | 4 | `dispenser` |
    | 5 | `sticky_piston` |
    | 6 | `piston` |
    | 7 | `lever` |
    | 8 | `iron_block` |
    | 9 | `detector_rail` |
    | 10, 11 | nichts |

  - Alles liegt verteilt auf ±1. XP siehe oben.
- **Spawnen:**
  - `getCanSpawnHere`: `posY >= 50`, Nacht, Säule x -1..1 / z -1..0 / y +1..+5 nur aus Luft oder `tallgrass`, `isValidLightLevel` (:351-369)
  - `canDespawn` = `!isNoDespawnRequired()`, der Boss despawnt also (:69-71)
  - Quelle: Village-Dimension 8/1-2 Monster (BiomeGenUtopianPlains.java:208-210). 01-mobs.md „Village Dim., at night“ passt.
- **Zustand:**
  - DW 20 = `attacking` (0/1), liest `ModelGiantRobot` für die Angriffspose (GiantRobot.java:52, 343-349; ModelGiantRobot.java:288)
  - `RenderGiantRobotInfo renderdata` mit Hüft-, Oberschenkel- und Schienbeinwinkeln und `gpcounter` als clientseitiger Animationsspeicher in der Entity (:56-67; ModelGiantRobot.java:133)
  - Kein NBT
- **Sounds:** Living `orespawn:robot_living` mit Chance 1/4, Hurt `orespawn:robot_hurt`, Death `orespawn:robot_death`, Lautstärke und Pitch 1.0 (:103-124); `fireworks.launch` beim Schuss.
- **Config:** `Jeffery_health/_attack/_defense` (550/40/18), `JefferyEnable` (nur im Biom), `PlayNicely`.
- **Portierung 1.21.1:**
  - Basis `Monster`, `MoveThroughVillageGoal(this, 0.9, false, 4, () -> false)`.
  - Projektil `LaserBall` als `ThrowableProjectile` mit Explosion `Level.ExplosionInteraction.MOB`.
  - Kopf-Yaw → `getYHeadRot()`.
  - Größe 9.75 als `EntityDimensions`, `clientTrackingRange(8)`.
  - Die Animationsdaten nicht als synchronisierten Zustand behandeln: POJO ohne Client-Importe oder clientseitige Map.
  - Das Modell ist groß: 19 Teile, `render()` 2189 Bytes (anim_summary.txt:47); Beinanimation von Hand portieren.
  - Drops als Loot-Table mit einem Pool `rolls 15-29`, 4× Laser Ball und einem Pool `rolls 10-19` mit 12 gleich gewichteten Einträgen, davon 2 leer.

### Girlfriend - Girlfriend (`girlfriend`)

- **Rolle:** Zähmbare Begleiterin, `EntityTameable` mit `IRangedAttackMob`.
  - Sie trägt Waffe und Rüstung und kämpft nah oder mit geworfenen Schuhen bzw. dem Ultimate Bow.
  - Sie tanzt nachts auf Edelmetallblöcken.
  - Sie spricht (abschaltbar) und hat einen Valentinstag-Modus als 8-Block-Riesin.
- **Werte:**
  - Leben 80, am Valentinstag vor der Versöhnung 800 (Girlfriend.java:554-559). 800 liegt unter der Klemme von 1024.
  - Attributschaden 8.0 (:182), Speed 0.3 (:113), XP 0 (:154), `isImmuneToFire`, `fireResistance` 100 (:124-125)
  - Größe 0.5×1.6, am Valentinstag 2.5×8.0 (:120-123); der Renderer skaliert dann ×5 (RenderGirlfriend.java:23-26)
  - Rüstung = Summe `damageReduceAmount` der getragenen `ItemArmor`, geklemmt auf 8..23 (:185-200). Manifest: armor 23 = Obergrenze.
  - **Jeder** eingehende Schaden wird auf 10 gedeckelt (:1081-1083); Kaktus wird ignoriert, am Valentinstag auch `inWall` (:1084-1087)
  - Fallschaden `ceil(Fallhöhe-3)`, gedeckelt auf 3 (:540-552)
  - Auto-Heilung +1 alle 100 Ticks, erstmals nach 200 (:105, 570-576); Essen heilt `getHealAmount × 5` (:723-728)
  - `wet_count` 500 Ticks nach Wasser oder Lava (:564-569)
  - Nahkampftakt `attackTime` 25 Ticks (:272), Fernkampf über `EntityAIArrowAttack` alle 20 Ticks, Reichweite 10 (:132)
  - Schuh-Projektil `Shoes(ShoeId 2..5)`: Schaden 2, gegen Creeper +4, gegen Girlfriend/Boyfriend 1, gegen Spieler 0, am Valentinstag 10 (Shoes.java:59-80)
  - Ultimate Bow: `UltimateArrow`, Tempo 2.0, Streuung 10.0, kritisch mit Chance 1/4, Punch als Rückstoß, Flame → 100 Brand, Item-Schaden 1, nicht aufhebbar (:989-1005). Der Pfeil **heilt** Spieler, Girlfriend, Boyfriend und gezähmte Tiere um 1 statt Schaden (UltimateArrow.java:190-205).
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task |
    |---|---|
    | 1 | `MyEntityAIFollowOwner(1.4, max 12, min 1.5)` (am Valentinstag für Girlfriend aus, MyEntityAIFollowOwner.java:38) |
    | 2 | `Tempt(1.25, red_flower)` |
    | 3 | `MyEntityAIDance` |
    | 4 | `EntityAIArrowAttack(1.25, 20, 10)` |
    | 5 | Swimming |
    | 6 | Panic 1.5 |
    | 7 | `WatchClosest(Player, 6)` |
    | 8 | `MyEntityAIWander(0.75)` |
    | 9 | LookIdle |
    | 10 | `OpenDoor` |
    | 11 | `MoveIndoors` |

    (Girlfriend.java:128-139)
  - Targets:

    | Prio | Target | Bedingung |
    |---|---|---|
    | 1 | `MyValentineTarget(EntityPlayer, 16)` | nur am Valentinstag und solange `feelingBetter == 0` (MyValentineTarget.java:37-46) |
    | 2 | `MyValentineTarget(Boyfriend, 16)` | wie oben |
    | 2 | `MyEntityAINearestAttackableTarget(EntityCreeper, 20, IMob.mobSelector)` | nur bei `PlayNicely == 0` |
    | 3 | `…(EntityLiving, 15, IMob.mobSelector)` | nur bei `PlayNicely == 0` |
    | 4 | `MyEntityAIJealousy(Girlfriend, 6.0, chance 5)` | nur bei `PlayNicely == 0` |
    | 5 | `MyEntityAIJealousy(Girlfriend, 3.0, chance 15)` | nur bei `PlayNicely == 0` |

    (Girlfriend.java:140-153)
    - `chance` bedeutet: Der Task startet nur, wenn `nextInt(100) ≤ chance` (MyEntityAINearestAttackableTarget.java:44).
    - Eifersucht wirkt nur, wenn sie gezähmt ist, nicht sitzt, einen Besitzer hat und das Ziel eine **ungezähmte** Girlfriend ist (MyEntityAIJealousy.java:16-52).
  - Nahkampf in `updateAITick` (:253-308):
    - Ziel = `getAttackTarget`, bei `PlayNicely` null. Rachziel-Reset mit Chance 1/100, Angriffsziel-Reset mit Chance 1/200.
    - Nur mit Item in der Hand und nicht sitzend.
    - Distanz < 4 (mit `MyBertha` < 10) → `attackTime--`, bei ≤ 0: Takt 25, Schwung, `attackTargetEntityWithCurrentItem`. Jeder dritte Treffer spielt `orespawn:o_fight` 0.5 (`fight_sound_ticker` 3).
    - Distanz < 7 ohne Ultimate Bow → alle 300 Ticks `orespawn:o_taunt` 0.5, Pfad Speed 1.25.
    - Ziel weg nach einem Kampf → `orespawn:o_woohoo` 0.4.
  - `attackTargetEntityWithCurrentItem` bildet die Spielerformel nach (:1023-1064):
    - Attribut-Schaden plus Verzauberungs-Modifikator
    - Stärke `+3 << Amp`, Schwäche `-2 << Amp`
    - Rückstoß-Verzauberung +1 beim Sprinten
    - Kritbonus `rand(var2/2+2)` im Fall
    - Feueraspekt `Level × 4` Sekunden
  - Fernkampf `attackEntityWithRangedAttack` (:983-1017): nicht während eines Schwungs. Mit Ultimate Bow `UltimateArrow`, sonst `Shoes` mit Tempo 1.8, Streuung 4.0, Bogen `0.2 × horizontale Distanz`, Sound `random.bow` 0.75.
  - `MyEntityAIDance` (MyEntityAIDance.java:27-150):
    - Nur Tageszeit 14000-22000 und nicht sitzend.
    - Tanzblöcke `gold_block`, `diamond_block`, `emerald_block`, `MyBlockRubyBlock`, `MyBlockAmethystBlock`, `MyBlockTitaniumBlock`, `MyBlockUraniumBlock` im 7×7 unter den Füßen.
    - Sie läuft zum Schwerpunkt, 10 Tanzfiguren, Zyklus 20, Figurwechsel nach 160 Ticks.
    - Sie synchronisiert sich mit Tänzerinnen niedrigerer Entity-ID im Umkreis ±4/±3/±4.
  - Elevator: Gezähmt, nicht sitzend, und reitet der Besitzer ein `Elevator`, dann klebt sie bei Versatz -0.45 am Aufzug (:206-223).
- **Interaktion** (`interact`, :603-849), in dieser Reihenfolge:
  1. `red_flower` oder `CrystalFlowerRedBlock`, Abstand² < 16: wild → serverseitig 1/3 zähmen + volle Heilung, sonst Rauch; gezähmt beim Besitzer → volle Heilung; verbraucht (:609-642).
  2. Gezähmt + Besitzer + `deadbush` → auswildern (:643-658).
  3. Gezähmt + Besitzer + `MyRuby` → Stimme aus (`voice_enable 0`) (:659-674); `MyAmethyst` → Stimme an (:675-690).
  4. Gezähmt + Besitzer + `yellow_flower`/`CrystalFlowerYellowBlock` → nass: nächster Bikini-Skin 0..17, trocken: nächster Kleid-Skin 0..40 (:691-721).
  5. Gezähmt + Besitzer + **jedes andere Item** (:722-784):
     - `ItemFood` → Heilung `getHealAmount×5`, verbraucht.
     - Sonst wird das Item in die Hand gelegt, das alte kommt zurück. `diamond` → Sitzen, jedes andere → Aufstehen.
     - War die Hand leer und ist es ein `CrystalPink*`/`TigersEye*`-Rüstungsteil, wandert es in den Slot 4/3/2/1 (Kopf/Brust/Beine/Füße), und die alte Rüstung landet in der Hand.
  6. Gezähmt + `diamond_block` + Abstand² < 16, **ohne Besitzerprüfung** → Aufstehen, Besitzer = dieser Spieler (:785-799). Da Schritt 5 jedes Item des Besitzers abfängt, erreicht diesen Zweig nur ein **fremder** Spieler. Er übernimmt damit die Begleiterin. Originalfehler.
  7. `name_tag` beim Besitzer (:800-810) ist **unerreichbar**: Schritt 5 legt das Namensschild in die Hand.
  8. Leere Hand beim Besitzer: erstes belegtes Ausrüstungsslot 0..4 an den Spieler zurückgeben und aufstehen. Ist alles leer, kommt die Chatnachricht „I have %d health. Thank you for asking! xoxo“ (:811-847).
  9. Sonst `super.interact`. Das ist Vanilla-Zucht, aber ohne Mate-Task und mit `createChild` null folgenlos (:848, 1070-1072).
- **Valentinstag** (`OreSpawnMain.valentines_day`, gesetzt, wenn beim Mod-Start 14. Februar ist, OreSpawnMain.java:4227-4229):
  - Solange `feelingBetter == 0`: 800 Leben, Größe 2.5×8.0, Textur `girlfriendv.png`, Angriffsziel Spieler und Boyfriend.
  - Trifft ein Spieler sie mit `MyRoseSword`, gibt es mit Chance 1/4 die Versöhnung: `feelingBetter = 1`, Ziel weg, normale Größe, Max-Leben 80, `10+rand(10)` `MyLove`. Sonst droppt 1 `MyLove` (:1088-1110).
  - Der Client übernimmt die Größe beim Sync (:590-594), beim Laden ebenso (:248-250).
- **Drops** (:926-981):
  - gezähmt `2+rand(5)` Mohn
  - immer je `4+rand(16)` von `MyItemShoes`, `MyItemShoes_1`, `MyItemShoes_2`, `MyItemShoes_3`
  - gezähmt zusätzlich Handitem und alle vier Rüstungsslots als ganze Stacks
  - `getDropItem` (Mohn, :917-919) wird durch das Überschreiben nicht benutzt
  - XP 0
- **Spawnen:**
  - `getCanSpawnHere`: Spawner `"Girlfriend"` im Bereich x/z -3..2, y 0..4 → erlaubt, sonst Vanilla-`EntityAnimal`-Regel (:1117-1134)
  - `canDespawn` = false (:855-857)
  - Quellen:
    - Overworld laut manifest (OreSpawnMain.java:4233-4246)
    - Utopia 5/2-3 (BiomeGenUtopianPlains.java:16-18), Village 1/2-3 (:220-222)
    - `GenericDungeon.makeDamselInDistress` direkt (GenericDungeon.java:3730), Spawner in `makeGirlfriendIsland` (:5030)
    - Froschkuss (Frog.java:109-113)
- **Zustand:**
  - DW 20 = Kleid-Skin 0..40, 21 = Stimme 0..9, 22 = Bikini-Skin 0..17, 23 = `voice_enable`, 24 = `is_princess` (0/1/2), 25 = `feelingBetter` (:157-168)
  - 21, 23, 24 und 25 werden serverseitig alle 20 Ticks nachgeschoben (:577-596)
  - NBT: `GirlType`, `WetGirlType`, `GirlVoice`, `GirlVoiceEnable`, `IsPrincess`, `feelingBetter` (:226-251)
  - Texturwahl (:314-508):
    - Valentinstag ohne Versöhnung → `girlfriendv.png`
    - trocken → `FrogPrincess.png`/`FrogPrincess2.png` bei Prinzessin 1/2, sonst `girlfriend<N>.png`
    - nass → `bikini<N>.png`
  - `setPrincess` setzt nur das Feld, der DW folgt beim nächsten Sync (:310-312).
- **Sounds:**
  - `getLivingSound` (:859-900):
    - still, wenn sie sitzt, die Stimme aus ist, sie tanzt, ein Ziel hat, oder mit Chance 10/11
    - im Wasser/Lava `o_water`
    - mit 3/4-Chance Wetter/Nacht: unter y 60 still, Gewitter `o_thunder`, Regen `o_rain`, Nacht unter freiem Himmel mit Chance 1/3 `o_dark`
    - danach ungezähmt still; gezähmt und verletzt (oder Valentinstag-traurig) `o_hurt`, sonst `o_happy`
  - Hurt `o_ow` (nur bei Stimme an), Death `o_death_girlfriend` (gezähmt) bzw. `o_death_single`
  - Lautstärke 0.3, Pitch `(voice-5)×0.02+1`, also 0.90..1.08 (:902-915, 1066-1068)
  - Varianten laut sounds_dump: o_fight 7, o_happy 7, o_hurt 9, o_ow 8, o_taunt 4, o_water 2, o_woohoo 4
- **Config:** `GirlfriendEnable` (manifest), `PlayNicely`. `valentines_day` ist datumsgesteuert, keine Config.
- **Portierung 1.21.1:**
  - Basis `TamableAnimal implements RangedAttackMob`, `RangedAttackGoal(this, 1.25, 20, 10f)`, `OpenDoorGoal` mit `GroundPathNavigation.setCanOpenDoors(true)`. `MoveIndoors` hat kein Gegenstück, siehe Gazelle.
  - Slots: 0 → `MAINHAND`, 1 → `FEET`, 2 → `LEGS`, 3 → `CHEST`, 4 → `HEAD`. Rüstungswert aus `getArmorValue()` klemmen.
  - Die Rüstungsobergrenze 23 liegt unter 30. Schadensdeckel 10 in `hurt()`.
  - Die Nahkampfformel über `EnchantmentHelper.modifyDamage(serverLevel, weapon, target, source, dmg)`: Verzauberungen sind in 1.21.1 datengetrieben. Feueraspekt und Rückstoß laufen über `doPostAttackEffects`.
  - Größenwechsel über überschriebenes `getDefaultDimensions` + `refreshDimensions()` beim DW-Update von `feelingBetter`.
  - Das Valentins-Datum zur Laufzeit prüfen oder einmal beim Serverstart, zu entscheiden.
  - Modell: vanilla `HumanoidModel` mit `HumanoidArmorLayer` und `ItemInHandLayer`, Textur je Instanz.
  - Die Chatnachricht wird zu `Component.literal`.
  - Die Fehler in den Schritten 6 und 7 bewusst entscheiden (vorschlagsweise Besitzerprüfung für den Diamantblock und Namensschild vor Schritt 5).
  - `GirlfriendOverlayGui` ist Client-GUI und gehört nach `com.swbr.orespawn.client`.

### GoldCow - Golden Apple Cow (`golden_apple_cow`)

- **Rolle:** Passive Kuh-Variante, `RedCow` → `EntityCow`. Sie droppt Äpfel und einen goldenen Apfel.
- **Werte:** keine eigenen; Leben, Speed und XP sind Vanilla-`EntityCow`. Nicht despawnend über `RedCow.canDespawn` = false (RedCow.java:37-39).
- **KI und Angriffe:** Vanilla-Kuh-Tasks. Aus `RedCow`: Rachziel-Reset mit Chance 1/200 je Tick (RedCow.java:31-35).
- **Interaktion:** Vanilla-Kuh (Melken, Zucht mit Weizen). Der Nachwuchs ist wieder eine `GoldCow` (GoldCow.java:23-31).
- **Drops** (GoldCow.java:15-21):
  - `rand(3) + rand(1+Looting)` Äpfel
  - **1 `golden_apple`** (garantiert)
  - dann `super` = `RedCow.dropFewItems`: noch einmal `rand(3) + rand(1+Looting)` Äpfel (RedCow.java:14-19)
  - dann die Vanilla-Kuh-Drops (Leder, Rindfleisch)
  - 01-mobs.md „beef, leather, golden apples“ passt
- **Spawnen:**
  - `CowEnable`: plains 5/2-6, forest 5/2-6, megaTaiga 5/2-5, taiga 5/2-5 (OreSpawnMain.java:4276-4279)
  - Utopia 8/2-6 (BiomeGenUtopianPlains.java:24), Village 6/2-6 (:228), Chaos 2/2-4 (:322)
  - Spawnregel Vanilla-`EntityAnimal`
- **Zustand:** keiner.
- **Sounds:** Vanilla-Kuh.
- **Config:** `CowEnable` (manifest).
- **Portierung 1.21.1:**
  - Basis `Cow` über die Zwischenklasse `RedCow` (die gehört zu einem anderen Batch, ist aber Voraussetzung).
  - `getBreedOffspring` gibt den eigenen `EntityType` zurück.
  - Drops als Loot-Table: Pool Apfel `uniform(0,2)` + `looting_enchant` zweimal (einmal GoldCow, einmal RedCow), Pool goldener Apfel 1, dazu die Pools aus `entities/cow`.
  - Renderer `RenderEnchantedCow` mit `CowModel` und Schatten 0.7 (manifest).
