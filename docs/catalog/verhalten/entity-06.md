# Verhalten: entity-06

Dieser Stapel enthält vierzehn Klassen, und die sind sehr verschieden. Neun sind Umgebungs- und Nutztiere mit wenig Logik (Chipmunk, Cockateil/Bird, Coin, Cricket, CliffRacer, Dragonfly, CrystalCow). Vier sind feindliche Mobs, die ihre Ziele selbst per AABB-Scan suchen und dafür nicht die Vanilla-Target-Tasks nutzen (CloudShark, CreepingHorror, Cryolophosaurus, DungeonBeast). Dazu kommen der größenskalierte Crab mit Wassersuche und das Wurfgeschoss DeadIrukandji. Der mit Abstand größte Brocken ist `Dragon`: ein zähmbares, reitbares Flugtier mit zwei Elementtypen, drei Feuerstufen, eigener Flug-KI mit und ohne Reiter und Lava-Heilung (1473 Zeilen). Fünf Klassen fliegen über eine Wegpunkt-Schleife in `updateAITasks`, die direkt `motionX/Y/Z` verändert. Diese Schleife lässt sich für CliffRacer, CloudShark, Cockateil, Dragonfly und Dragon als gemeinsamer Helfer portieren. Kein Wert dieses Stapels überschreitet die 1.21.1-Klemmen (höchste Lebenspunkte: Crab mit 250 bei Skalierung 1.0, höchste Rüstung: Crab mit 18). Virtuelle Lebenspunkte sind also nicht nötig.

Hinweis zu SRG-Namen: `func_152115_b`, `func_152114_e`, `func_152113_b` (EntityTameable) und `func_145881_a` (TileEntityMobSpawner) stehen nicht in `reference/jar/mcp/methods.csv`. Die Bedeutung ist aus der Verwendung abgeleitet: `func_152115_b(String)` setzt die Besitzer-UUID, `func_152114_e(EntityLivingBase)` prüft „ist Besitzer", `func_152113_b()` liest die Besitzer-UUID, `func_145881_a()` liefert die Spawner-Logik. `func_110163_bv` = `enablePersistence` ist in der CSV belegt.

Dimensionen (OreSpawnMain.java:1266-1271, manifest `dimensions`): `DimensionID2` = BaseDimensionID+1 „Extreme", `DimensionID3` = +2 „VillageMania", `DimensionID4` = +3 „Islands", `DimensionID5` = +4 „Crystal", `DimensionID6` = +5 „Chaos"; `BaseDimensionID` Default 80 (manifest).

---

### Chipmunk - Chipmunk (`chipmunk`)

- **Rolle:** Passives, zähmbares Kleintier, `EntityTameable` über `EntityCannonFodder`. Es ist ein „Battle Mob": mit Karotte, Kartoffel oder Quinoa wird es zum Kämpfer mit Hutfarbe (EntityCannonFodder.java:97-162). Brütbar.
- **Werte:**
  - XP 5 (Chipmunk.java:27), `fireResistance` 100 (Chipmunk.java:24), Laufgeschwindigkeit 0.38 wird jeden Tick neu gesetzt (Chipmunk.java:59).
  - Regeneration 1 HP mit Chance 1/250 pro AI-Tick in Chipmunk (Chipmunk.java:86-88). Im Kampfmodus kommt noch einmal 1 HP mit 1/250 dazu (EntityCannonFodder.java:384-386), die Heilung ist dann also doppelt.
  - Fallschaden = ceil(Fallhöhe−3), gedeckelt auf 2 (Chipmunk.java:64-75).
  - Rüstung 3 nur bei `is_activated == 2`, sonst 0 (EntityCannonFodder.java:335-340).
  - Kampfschaden 3.0 direkt, nicht über das Attribut (EntityCannonFodder.java:360-362, 342-344).
  - Jungtier wird mit halber Skalierung gerendert (RenderChipmunk.java:37-38).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMate` 1.0; 2 `MyEntityAIFollowOwner` (Speed 2.0, maxDist 10, minDist 2); 3 `MyEntityAIAvoidEntity` gegen `EntityMob` (Radius 8, weit 1.0 / nah 1.6); 4 `EntityAITempt` Apfel 1.2; 5 `EntityAIPanic` 1.5; 6 Vanilla-`EntityAIAvoidEntity` Spieler (8, 1.0/1.4); 7 WatchClosest Spieler 6; 8 WatchClosest `EntityLiving` 5; 9 `MyEntityAIWanderALot` (xz 10, 1.0); 10 LookIdle; 11 `EntityAIMoveIndoors` (Chipmunk.java:28-39).
  - FollowOwner läuft, wenn das Tier nicht sitzt und entweder (y<60 oder Nacht) und Abstand > maxDist/2 ist, oder Abstand ≥ maxDist (MyEntityAIFollowOwner.java:38). Scheitert der Pfad bei Abstand² ≥ 144, teleportiert es neben den Besitzer (MyEntityAIFollowOwner.java:74-81).
  - AvoidEntity nimmt die Nahgeschwindigkeit bei Abstand² < 49 (MyEntityAIAvoidEntity.java:80).
  - WanderALot startet mit Chance 1/30, vertikaler Suchradius 7 (MyEntityAIWanderALot.java:35, 44).
  - Racheziel wird mit 1/200 pro Tick vergessen (Chipmunk.java:83-85).
  - Grasen: serverseitig mit 1/600 wird Dirt/Farmland unter dem Tier zu Luft, nur bei `mobGriefing` (Chipmunk.java:89-94).
  - Kampfmodus (nur `is_activated == 2`, EntityCannonFodder.java:354): mit Chance 1/5 (pfreq) wird ein Ziel in 10/4/10 gesucht (EntityCannonFodder.java:357, 320). Das Tier läuft mit 1.25 hin und schlägt bei Abstand² < 9 zu, wenn `rand(7)==0 || rand(6)==1` (sfreq 6, EntityCannonFodder.java:361, 375-377). Sitzt es und hat kein Ziel, kehrt es mit 0.65 zum Patrouillenpunkt zurück (EntityCannonFodder.java:380-382).
  - Ziele im Kampfmodus: `EntityMob`; andere `EntityCannonFodder` mit Hutfarbe ≠0 und ≠ eigener Farbe; Spieler außer Kreativ, `name_one` und `name_two`. Nichts auf Peaceful. Sitzend nur Ziele im Radius² 144 um den Patrouillenpunkt (EntityCannonFodder.java:280-317).
- **Interaktion:** Zuerst läuft `EntityCannonFodder.interact`, das seinerseits die Vanilla-Paarung mit dem Zuchtitem aufruft (Chipmunk.java:121, EntityCannonFodder.java:72).
  - Karotte → Hut 1, Quinoa (`quinoa`) → Hut 2, Kartoffel → Hut 3. Jeweils: zähmen, Besitzer = `name_one`, `is_activated` 0→1, volle Heilung, persistent, Item −1. Abstand² < 16 (EntityCannonFodder.java:97-162).
  - Erneuter Rechtsklick eines Besitzers setzt `is_activated = 2` (Kampfmodus) und tauscht `name_one`/`name_two`. Das sind zwei Freundes-UUIDs. Ein dritter Spieler wird abgewiesen (EntityCannonFodder.java:75-96).
  - Corn (`corn_seed`) bei `is_activated == 2`: klont ein neues „Chipmunk" mit gleichem Besitzer, Hut und Namen, spielt `random.explode` 0.75/2.0 am Spieler (EntityCannonFodder.java:163-193).
  - Bei `is_activated == 2` schaltet jeder weitere Klick das Sitzen um und merkt sich den Patrouillenpunkt (EntityCannonFodder.java:194-207).
  - Chipmunk-eigen: Apfel bei Abstand² < 16 zähmt mit Chance 1/2 (`rand(2)==0`) und heilt voll, sonst Rauch (Chipmunk.java:124-139). Der Besitzer heilt mit Apfel voll (Chipmunk.java:140-148). Dead Bush entzähmt (Chipmunk.java:158-173). Name Tag benennt (Chipmunk.java:174-184). Rechtsklick des Besitzers ohne passendes Item schaltet Sitzen um (Chipmunk.java:185-193).
  - Zucht: `isBreedingItem` = `crystalapple` (Chipmunk.java:268-270). Das Kind ist ein neues Chipmunk (Chipmunk.java:256-262). `isWheat` (Apfel) ist eine OreSpawn-eigene Methode ohne Vanilla-Aufrufer (Chipmunk.java:264-266).
- **Drops:** Gezähmt 2-6 × Mohn (`Blocks.red_flower`, `rand(5)+2`, Chipmunk.java:222-227). Wild: `getDropItem` = Weizen mit der Vanilla-Menge aus `EntityLiving.dropFewItems` (Chipmunk.java:217, 230). XP 5.
  - Research-Widerspruch (01-mobs.md:814, 121): „drops seeds, attack 0". Im Code sind es Weizen bzw. Mohn, Attribut 1.0 und Kampfschaden 3.
- **Spawnen:**
  - Nur bei y ≥ 50 und höchstens 2 Chipmunks in 20/10/20 (Chipmunk.java:239, 243). Overworld-Biome: manifest.
  - Utopia: `ambient` Gewicht 3, 1-2 (BiomeGenUtopianPlains.java:34). VillageMania: 5, 1-2 (BiomeGenUtopianPlains.java:238). Chaos: 1, 1-2 (BiomeGenUtopianPlains.java:315).
  - `canDespawn`: Jungtiere werden persistent und despawnen nie. Sonst despawnt es, wenn nicht persistent und nicht gezähmt (Chipmunk.java:247-253).
- **Zustand:**
  - DataWatcher 20 `is_activated`, 21 `hat_color`; beide werden alle 6 Ticks vom Server geschrieben und vom Client gelesen (EntityCannonFodder.java:46-47, 52-63). Dazu die Vanilla-Tameable-Watcher.
  - NBT `NameOne`, `NameTwo`, `IsActivated`, `HatColor`, `PatrolX/Y/Z` (EntityCannonFodder.java:238-257). Beim Laden setzt ein nicht leeres `NameOne` gezähmt plus Besitzer (EntityCannonFodder.java:274-277).
- **Sounds:** Living keiner (Chipmunk.java:197-202); Hurt `orespawn:scorpion_hit` (Chipmunk.java:205); Death `orespawn:cryo_death` (Chipmunk.java:209); Lautstärke 0.4 (Chipmunk.java:213); Pitch Jungtier 1.5±0.1, erwachsen 1.0±0.1 (Chipmunk.java:235); Fall `damage.fallbig` > 3 bzw. `damage.fallsmall` (Chipmunk.java:66-71).
- **Config:** `ChipmunkEnable` (manifest); `PlayNicely` wird **nicht** geprüft, auch `EntityCannonFodder.findSomethingToAttack` prüft ihn nicht (EntityCannonFodder.java:319-333). Die Texturwahl nach Hutfarbe steht im Renderer: Hut 2 → `chipmunktexture2`, Hut 3 → `chipmunktexture3`, sonst Basis, nur bei `is_activated ≠ 0` (RenderChipmunk.java:48-60).
- **Portierung 1.21.1:**
  - `TamableAnimal` plus eine gemeinsame Basisklasse `CannonFodder`, die Lizard, Ostrich und VelocityRaptor mitbenutzen.
  - `is_activated`/`hat_color` als `EntityDataAccessor<Integer>`; der 6-Tick-Sync entfällt, SynchedEntityData synchronisiert selbst.
  - Die zwei Freundes-UUIDs als `UUID` statt String speichern, dabei die NBT-Schlüssel beibehalten.
  - Grasen über `EventHooks.canEntityGrief`.
  - Falle: das Feld `moveSpeed` ist in `applyEntityAttributes` noch 0 (Feldinitialisierer laufen nach dem Superkonstruktor), erst `onUpdate` setzt 0.38. In 1.21.1 den Wert direkt in `createAttributes()` setzen.
  - `ModelChipmunk` braucht `isSitting` (ModelChipmunk.java:151).

### CliffRacer - Cliff Racer (`cliff_racer`)

- **Rolle:** Passiver Flieger, `EntityAnimal`, nicht brütbar (`createChild` = null, CliffRacer.java:153-155). Das Attribut attackDamage 1.0 wird nie benutzt.
- **Werte:**
  - XP 5 (CliffRacer.java:20), `fireResistance` 5 (CliffRacer.java:22).
  - Kein Fallschaden (CliffRacer.java:125-129); `collideWithEntity` ist leer (CliffRacer.java:61-62).
  - `motionY *= 0.6` nach jedem `onUpdate` (CliffRacer.java:74).
- **KI und Angriffe:** Keine AI-Tasks. Flugschleife in `updateAITasks` (CliffRacer.java:81-119):
  - Neuer Wegpunkt mit Chance 1/300 oder bei Abstand² < 2.1 (CliffRacer.java:92).
  - Versatz x/z je ±(5..14), y ±5 (`rand(11)−5`) (CliffRacer.java:94-102). Der Zielblock muss Luft und per Raytrace ab Augenhöhe +0.75 sichtbar sein, bis zu 50 Versuche (CliffRacer.java:84, 104, 78).
  - Beschleunigung: x/z `(signum·0.4 − motion)·0.3`, y `(signum·0.7 − motion)·0.2` (CliffRacer.java:112-114).
  - Gier dreht um 1/6 der Differenz; `moveForward = 0.75` (CliffRacer.java:117-118).
- **Interaktion:** keine eigene.
- **Drops:** `getDropItem` mit `rand(8)`: 0 roher Hühnchen, 1 `uranium_nugget`, 2 `titanium_nugget`, sonst nichts (CliffRacer.java:139-151). Menge nach Vanilla-`dropFewItems`. XP 5.
- **Spawnen:**
  - Nur bei y ≥ 50 (CliffRacer.java:136). Kein Overworld-Spawn (manifest `spawns` leer).
  - Islands: `ambient` 20, 3-6 (BiomeGenUtopianPlains.java:90). Chaos: 30, 3-6 (BiomeGenUtopianPlains.java:282).
  - Despawnt, wenn nicht persistent (CliffRacer.java:33-35).
- **Zustand:** keiner (kein DataWatcher, kein NBT).
- **Sounds:** Living `orespawn:cliffracer`, Lautstärke 0.45, Pitch 1.0 (CliffRacer.java:38-46); Hurt/Death keine (CliffRacer.java:49-55).
- **Config:** `CliffRacerEnable` (manifest).
- **Portierung 1.21.1:**
  - `Animal` mit gemeinsamem Helfer `WaypointFlight` (Parameter: Radius, y-Spanne, Beschleunigungsfaktoren, Gierteiler, Augenhöhe).
  - Den Raytrace über `level.clip(new ClipContext(..., COLLIDER, NONE, this))` machen; `ChunkCoordinates` wird `BlockPos.MutableBlockPos`.
  - Falle: Vanilla-Schwerkraft und die `motionY`-Dämpfung müssen erhalten bleiben. Also nicht `setNoGravity(true)` setzen und nicht `FlyingMoveControl` nehmen, sonst ändert sich das Flugbild. Die Schleife gehört in `customServerAiStep`, die Dämpfung in `tick()`.

### CloudShark - Cloud Shark (`cloud_shark`)

- **Rolle:** Feindlicher Flieger, `EntityMob`. Jagt vor allem kleine Flieger und Spieler.
- **Werte:**
  - XP 5 (CloudShark.java:23), `fireResistance` 5 (CloudShark.java:25).
  - Rüstung `CloudShark_stats.defense` (CloudShark.java:86-88).
  - Nahkampf mit Attributschaden, ohne Rückstoß und ohne Verzauberungen (CloudShark.java:36-40).
  - Kann unter Wasser atmen (CloudShark.java:251-253); kein Fallschaden (CloudShark.java:155-159); `motionY *= 0.6` (CloudShark.java:83).
- **KI und Angriffe:** Keine AI-Tasks. Alles in `updateAITasks` (CloudShark.java:94-149):
  - Höhenhaltung: unter y 120 → Wegpunkt +2 höher, über y 140 → −2 (CloudShark.java:106-111).
  - Neuer Wegpunkt mit 1/300 oder bei Abstand² < 2.1; Versatz x/z ±(8..17), y `rand(5)−2+updown`; Luft und sichtbar, 50 Versuche (CloudShark.java:112-128).
  - Angriff: mit Chance 1/9 pro Tick Zielsuche in 12/10/12, sortiert nach `GenericTargetSorter` (CloudShark.java:129, 222-223). Der Wegpunkt wird auf das Ziel gesetzt; bei Abstand² < 9 folgt ein Schlag (CloudShark.java:133-136). Einen weiteren Cooldown gibt es nicht.
  - Ziele: `EntityButterfly`, `Cockateil`, `EntityMosquito`, `Firefly`, Nicht-Kreativ-Spieler, `GoldFish`, `CliffRacer`. Nie `RockBase`/`EntityAnt`, sonst nichts. Nur mit Sichtlinie (CloudShark.java:178-216). `PlayNicely ≠ 0` schaltet die Suche ab (CloudShark.java:219-221).
  - Wird er getroffen, fliegt er zur Position des Angreifers (CloudShark.java:165-172).
  - Flug: x/z `(signum·0.5−m)·0.3`, y `(signum·0.7−m)·0.2`, Gier /4, `moveForward 1.0` (CloudShark.java:142-148).
- **Interaktion:** keine.
- **Drops:** `rand(3)`: Papier, Faden oder Knochen, je 1/3 (CloudShark.java:237-249); Menge nach Vanilla. XP 5.
  - Research (01-mobs.md:681) erwähnt einen Kraken-Ruf beim Tod. Der steht nicht in CloudShark.java (offen: andere Klasse, nicht gesucht).
- **Spawnen:**
  - `getCanSpawnHere` ist immer true (CloudShark.java:174-176).
  - Islands: `ambient` 1, 1-1 (BiomeGenUtopianPlains.java:93). Chaos: 2, 1-1 (BiomeGenUtopianPlains.java:285).
  - Strukturen: `GenericDungeon.makeCloudSharkDungeon` mit 4 Spawnern (GenericDungeon.java:2096-2111). Den setzt `OreSpawnWorld.addD4CloudShark` mit 1/300 pro Chunk in Islands auf y 150-159 (OreSpawnWorld.java:167-170, 2573-2577). Außerdem `makeRainbow` mit 6 Spawnern (GenericDungeon.java:6379-6404), Enormous-Castle-Ebene 1 (GenericDungeon.java:323) und `makeDungeon` mit Chance 1/12 auf den Spawner (GenericDungeon.java:193).
  - `canDespawn`: nur nachts und wenn nicht persistent (CloudShark.java:42-44). Der Despawn ist also an die Nacht gebunden, nicht an den Tag.
- **Zustand:** keiner.
- **Sounds:** Living `"splash"` ohne Namespace (CloudShark.java:55; offen: ob Vanilla 1.7.10 dieses Event kennt, in sounds_dump.txt steht es nicht); Hurt `orespawn:little_splat` (CloudShark.java:59); Death `orespawn:big_splat` (CloudShark.java:63); Lautstärke 0.25 (CloudShark.java:47).
- **Config:** `CloudSharkEnable`, `CloudShark_health/attack/defense` (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit dem `WaypointFlight`-Helfer; Zielsuche über `level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12,10,12))` plus Sortierer.
  - Die Kategorie in den Dimensions-Spawnlisten ist `ambient`, obwohl es ein Monster ist. In 1.21.1 zählt sie dann gegen das AMBIENT-Cap; das so übernehmen.
  - Den Spawnlisten-Laut „splash" auf ein existierendes Event abbilden oder stumm lassen und das dokumentieren.

### Cockateil - Bird (`bird`)

- **Rolle:** Passiver Kleinvogel, `EntityAnimal`, sechs Farbvarianten, nicht brütbar (Cockateil.java:248-250). Unterklasse `RubyBird` ruft `setFlyUp()` (RubyBird.java:15).
- **Werte:**
  - XP 2 (Cockateil.java:39), `fireResistance` 2 (Cockateil.java:41).
  - Variante `birdtype = rand(6)` in `entityInit` (Cockateil.java:80).
  - Fallschaden keiner (Cockateil.java:160-164).
  - Vertikale Dämpfung: `motionY *= 0.7` unter der Wegpunkthöhe, sonst `*= 0.5` (Cockateil.java:141-149).
- **KI und Angriffe:** Keine Tasks, kein Angriff (`getAttackStrength` 1 ist ungenutzt, Cockateil.java:152-154). Flug in `updateAITasks` (Cockateil.java:170-223):
  - In Islands `stayup = 2` (Cockateil.java:179-181).
  - Festsitzen: bleiben x/z gleich, zählt `stuck_count` hoch (Cockateil.java:182-189).
  - Neuer Wegpunkt bei `stuck_count > 40`, Chance 1/250 oder Abstand² < 4.1 (Cockateil.java:193).
  - Versatz x/z ±(5..12 − 2·flyup), y `rand(9+stayup) − 5 + flyup`; Luft und sichtbar, 35 Versuche (Cockateil.java:173, 197-205).
  - Flug: x/z `(signum·0.3−m)·0.25`, y `(signum·0.699999−m)·0.2`, Gier /3, `moveForward 0.8` (Cockateil.java:216-222).
- **Interaktion:** keine.
- **Drops:** Feder. Bei `birdtype == 5`, Spielerschaden und `rand(3)==1` gibt es stattdessen `ruby` (Cockateil.java:237-243). `killedByPlayer` wird bei **jedem** Treffer durch einen Spieler gesetzt, nicht erst beim Todesstoß (Cockateil.java:131-137). Menge nach Vanilla. XP 2.
- **Spawnen:**
  - Nur tagsüber und (in Islands oder y ≥ 50) (Cockateil.java:234).
  - Utopia `ambient` 10, 2-4 (BiomeGenUtopianPlains.java:37); Islands 4, 1-2 (:75); Crystal 4, 1-2 (:161); VillageMania 15, 2-4 (:241); Chaos 10, 2-4 (:276). Overworld: manifest.
  - Zusätzlich beschwören ThePrincess und TheQueen „Bird" (ThePrincess.java:627, TheQueen.java:437).
  - Despawnt, wenn nicht persistent (Cockateil.java:84-86).
- **Zustand:** DataWatcher 22 = `birdtype` (int) (Cockateil.java:81, 88-94). NBT `BirdType` (Cockateil.java:252-260).
- **Sounds:** Living `orespawn:birds` nur tagsüber und ohne Regen (Cockateil.java:104-109); Hurt/Death `orespawn:duck_hurt` (Cockateil.java:111-117); Lautstärke 0.55 (Cockateil.java:97).
- **Config:** Schlüssel `BirdEnable` → Feld `CockateilEnable` (manifest).
- **Portierung 1.21.1:**
  - Die Texturen `Bird1.png`…`Bird6.png` (Cockateil.java:263-268) liegen in der `texture_map` als `textures/entity/bird1..6.png`. Im manifest-Renderereintrag fehlen sie (`textures: []`), weil der Renderer `c.getTexture()` aufruft (RenderCockateil.java:41-44). Die Texturwahl gehört in den Client-Renderer, `getTexture()` nicht in die Entity übernehmen (ResourceLocation ist dort zwar erlaubt, aber unnötig).
  - Variante per `EntityDataAccessor<Integer>`, zufällig in `defineSynchedData` oder `finalizeSpawn`.
  - `killedByPlayer` in 1.21.1 lieber über `lastHurtByPlayer` beim Tod lösen; für 1:1 das eigene Flag behalten.

### Coin - Coin (`coin`)

- **Rolle:** Unbewegliches „Werbe"-Wesen, `EntityAnimal`, nur `EntityAILookIdle` (Coin.java:22). Lang-Name im Code „Coin!" (OreSpawnMain.java:3938), im manifest „Coin".
- **Werte:** XP 10 (Coin.java:20), `fireResistance` 100 (Coin.java:21), Laufgeschwindigkeit 0 (Coin.java:18, 28), Rüstung 0 (Coin.java:49-51).
- **KI und Angriffe:** keine.
- **Interaktion:** Rechtsklick immer false (Coin.java:126-128).
- **Drops:** Genau ein Item, `rand(10)` (Coin.java:91-120). Es wird mit ±1 Block Streuung (OreSpawnRand) auf y+1 abgelegt (Coin.java:85-88).

  | rand | Item |
  |---|---|
  | 0 | Diamant |
  | 1 | `uranium_nugget` |
  | 2 | `titanium_nugget` |
  | 3 | Smaragd |
  | 4 | `emeraldaxe` |
  | 5 | `emeraldshovel` |
  | 6 | `emeraldpickaxe` |
  | 7 | `emeraldhoe` |
  | 8 | `eggcoin` (Spawn Coin) |
  | 9 | `emeraldsword` (Startwert von `j`) |

  XP 10.
- **Spawnen:**
  - Nur tagsüber, bei y ≥ 50 und ohne anderes Coin in 20/8/20 (Coin.java:130-140). Overworld: manifest.
  - Utopia `ambient` 2, 1-1 (BiomeGenUtopianPlains.java:49); VillageMania 2, 1-1 (:247).
  - Despawnt, wenn nicht persistent (Coin.java:37-39).
- **Zustand:** keiner.
- **Sounds:** keine (Coin.java:61-71).
- **Config:** `CoinEnable` (manifest).
- **Portierung 1.21.1:** `Animal` mit Speed-Attribut 0; `getBreedOffspring` gibt null zurück. Die Drops als Code in `dropCustomDeathLoot`, weil es ein einzelner gewürfelter Drop mit eigener Streuung ist. Eine Loot-Table ginge auch, verliert aber die ±1-Streuung. Das Coin-Modell dreht sich vermutlich im Renderer (offen: `ModelCoin` nicht gelesen).

### Crab - Crab (`crab`)

- **Rolle:** Feindlicher Wasser-/Strandmob, `EntityMob`, in drei zufälligen Größen plus Spawnergröße. Er stirbt langsam außerhalb des Wassers.
- **Werte:**
  - Skalierung `t` in `entityInit`: Start 0.25; `rand(4)==1` → 0.5; danach unabhängig `rand(8)==2` → 1.0 (Crab.java:63-70). Wahrscheinlichkeiten also 1/8 für 1.0, 7/32 für 0.5, 21/32 für 0.25. Aus einem Spawner mit Name „Crab" wird die Skalierung 0.35 (Crab.java:488-490).
  - Max-Leben = `PitchBlack_stats.health` × Skalierung, **nicht** `Crab_stats.health` (Crab.java:128). Default 250 → 250/125/62, Spawner 87 (Nightmare_health 250, manifest). Research-Widerspruch (01-mobs.md:554): 180/90/45.
  - Schaden `Crab_stats.attack × scale` (Crab.java:56, 201); Rüstung `Crab_stats.defense + (int)(2·scale)` → 18/17/16 (Crab.java:132).
  - Geschwindigkeit an Land 0.55·scale, im Wasser 0.95·scale, jeden Tick gesetzt (Crab.java:116-122).
  - Hitbox: `entityInit` 3.75s × 3.5s (Crab.java:83), Konstruktor überschreibt mit 1.25 × 2.5 (Crab.java:38), jedes `onUpdate` setzt 2.5s × 3.5s (Crab.java:124).
  - XP: der Konstruktor setzt 150 (Crab.java:40) **nach** `entityInit` (400·t, Crab.java:81), weil `entityInit` im Entity-Superkonstruktor läuft. Frisch gespawnt also 150, nach NBT-Laden 400·scale (Crab.java:102). Dasselbe gilt für `fireResistance`: 30 (Crab.java:41) gegenüber 10·scale nach dem Laden (Crab.java:103).
  - I-Frames: nach einem akzeptierten Treffer 8 Ticks immun (Crab.java:220-223, 323-325). Kaktus immun (Crab.java:216-218).
  - Wasserheilung: im Wasser, verletzt, Chance 1/120 → +4·scale HP plus `splash` 1.5 (Crab.java:394-397).
  - Austrocknen: außer Wasser und ohne gefundenes Wasser mit 1/100 → `heal(−1·scale)`; bei HP ≤ 0 `setDead()`, also ohne Tod, ohne Drops (Crab.java:348-354).
- **KI und Angriffe:**
  - Tasks: 0 Swimming; 1 `MyEntityAIWanderALot` (xz 16, 1.0); 2 WatchClosest Spieler 10; 3 WatchClosest Living 8; 4 LookIdle. Target 1 `EntityAIHurtByTarget` (Crab.java:44-49).
  - Wassersuche außer Wasser mit 1/25: Schalen-Scan um die Füße, Radius i = 1..11 (ab i ≥ 5 in Zweierschritten), vertikal bis 10. Er läuft zum nächsten Wasser/Flowing Water auf y−1 mit Speed 1.33 (Crab.java:326-346, 235-316).
  - Angriff mit Chance 1/5 pro Tick (Crab.java:357). Mit 1/100 wird das Ziel verworfen (Crab.java:359-361), ein totes Ziel ebenfalls (Crab.java:363-366). Sonst Suche in 16/6/16 (Crab.java:446); das aktuelle Ziel wird bevorzugt (Crab.java:451-454).
  - Er dreht sich zum Ziel. In Reichweite `(6 + w/2)² · scale` (Crab.java:372) setzt er `attacking = 1` und schlägt mit `rand(4)==0 || rand(5)==1` zu (Crab.java:373-375). Sonst läuft er mit 1.0 hin (Crab.java:387). Ohne Ziel `attacking = 0` (Crab.java:391).
  - Schlag: Schaden wie oben, Rückstoß horizontal 1.15·scale, vertikal 0.48·scale, bei totem Ziel oder Spieler doppelt (Crab.java:200-211).
  - Ziele: Nicht-Kreativ-Spieler, `EntityMob` außer Crab, `Lizard`, `RubberDucky`, `EntityVillager`, `Girlfriend`, `Boyfriend`, sonst `MyUtils.isAttackableNonMob` (Crab.java:400-440, MyUtils.java:13-15). `PlayNicely ≠ 0` → keine Suche (Crab.java:443-445).
  - Wird er getroffen: Crab-Angreifer ignoriert; sonst Angreifer als Ziel und Pfad mit 1.2 (Crab.java:224-231).
- **Interaktion:** keine (Crab.java:196-198).
- **Drops:** Rohes Krabbenfleisch (`crabmeat`) ×(4 + rand(8)) × `(int)scale` (Crab.java:183-184). Weil `(int)` von 0.25/0.5/0.35 null ergibt, greift die Mindestmenge 1 (Crab.java:185-187). Nur Skalierung 1.0 gibt 4-11 Stück. Streuung ±1 (Crab.java:171-179). `getDropItem` (Fisch) ist ungenutzt.
- **Spawnen:**
  - Spawner-Sonderfall: ein `mob_spawner` mit Name „Crab" in x/z −3..2 und y 0..4 → immer erlaubt, Skalierung 0.35 (Crab.java:480-495).
  - Sonst y ≥ 50 und tagsüber (Crab.java:496-501). In Crystal zusätzlich `rand(40)==1` und höchstens 3 Crabs in 24/8/24 (Crab.java:502-509, 475).
  - Overworld `waterCreature`: manifest. Crystal: `waterCreature` 1, 1-2 (BiomeGenUtopianPlains.java:170).
  - Despawnt, wenn nicht persistent (Crab.java:111-113).
- **Zustand:** DataWatcher 20 = attacking (0/1, von `ModelCrab` gelesen, ModelCrab.java:333), 21 = Skalierung×100 als int (Crab.java:61-62, 86-96). NBT `Fscale` (float) (Crab.java:98-109).
- **Sounds:** Hurt `orespawn:leaves_hit` (Crab.java:152); Living/Death keine; Lautstärke 0.75 (Crab.java:160); Pitch `2.0 − 0.3/scale` (Crab.java:164). Beim Schlag am Ziel mit 1/3 `orespawn:scorpion_attack`, sonst `orespawn:scorpion_living`, 0.75/1.5 (Crab.java:377-382).
- **Config:** `CrabEnable`, `Crab_attack`, `Crab_defense`, `Nightmare_health` (!), `PlayNicely` (manifest). `Crab_health` wird von dieser Klasse nicht gelesen.
- **Portierung 1.21.1:**
  - Die Skalierung als `EntityDataAccessor<Integer>`; `getDefaultDimensions(pose)` mit `EntityDimensions.scalable(2.5f·s, 3.5f·s)`. Bei Änderung `refreshDimensions()` in `onSyncedDataUpdated` aufrufen.
  - Attribute nach dem Würfeln in `finalizeSpawn` setzen. Original-Fehler: Max-Leben wird im Konstruktor fest, die Spawner-Skalierung 0.35 ändert es nicht mehr. 1:1 nachbauen oder bewusst korrigieren und dokumentieren.
  - Den Spawner-Scan ersetzt `MobSpawnType.SPAWNER` in `checkSpawnRules`. Den Spawner-Entity-Namen bekommt man ohne Access Transformer nicht (`BaseSpawner.nextSpawnData` ist privat).
  - Austrocknen mit `discard()`, um das `setDead` ohne Drops zu treffen. `heal(negativ)` funktioniert in 1.21.1 weiter, weil `LivingEntity.heal` nur prüft, ob die aktuelle Gesundheit > 0 ist.
  - Kaktus-Immunität über `DamageTypes.CACTUS`. Die 8-Tick-I-Frames eigenständig nachbauen, sie ersetzen nicht die Vanilla-`invulnerableTime`.

### CreepingHorror - Creeping Horror (`creeping_horror`)

- **Rolle:** Feindlicher Bodenmob, `EntityMob`, greift fast alles an. Nachtaktiv: tagsüber zerfällt er von selbst.
- **Werte:**
  - XP 5 (CreepingHorror.java:23), `fireResistance` 10 (CreepingHorror.java:24).
  - Rüstung `CreepingHorror_stats.defense` (CreepingHorror.java:50-52).
  - Selbstauflösung: nicht persistent und Tageszeit `worldTime % 24000 ≤ 11000` → mit 1/500 pro Tick `setDead()` ohne Drops (CreepingHorror.java:61-71).
- **KI und Angriffe:**
  - Tasks: 0 Swimming; 1 `EntityAIPanic` 1.35; 2 `EntityAIMoveThroughVillage` 1.0; 3 WanderALot (10, 1.0); 4 WatchClosest Spieler 8; 5 LookIdle. Target 1 HurtByTarget (CreepingHorror.java:25-31).
  - Racheziel mit 1/200 vergessen (CreepingHorror.java:110-112).
  - Angriff mit Chance 1/5: Ziel in 16/4/16, Pfad 1.25. Bei Abstand² < 5 und `rand(12)==0 || rand(14)==1` folgt ein Vanilla-`attackEntityAsMob` (CreepingHorror.java:113-121, 183).
  - Ziele: jedes lebende sichtbare Wesen außer `CreepingHorror`, `RockBase`, `EnderReaper`, `LeafMonster`, `Dragon`, `TerribleTerror`, `LurkingTerror`, `PitchBlack`, `Firefly`, `Island`, `IslandToo` und Kreativ-Spielern, also auch Nutztiere (CreepingHorror.java:124-177). `PlayNicely` sperrt die Suche (CreepingHorror.java:180-182).
- **Interaktion:** keine.
- **Drops:** `rand(3)`: verrottetes Fleisch, Knochen oder Faden, je 1/3 (CreepingHorror.java:94-103). Menge nach Vanilla. XP 5.
- **Spawnen:**
  - `isValidLightLevel` und nicht Tag und (Chaos **oder** y ≤ 15) (CreepingHorror.java:199).
  - Islands: `monster` 60, 4-8 (BiomeGenUtopianPlains.java:99), wegen der Bedingung dort aber nur unterhalb y 16 wirksam. Chaos: `monster` 5, 1-5 (BiomeGenUtopianPlains.java:362).
  - `canDespawn` nur tagsüber (CreepingHorror.java:202-204).
  - Research (01-mobs.md:101) sagt „Danger Dim.". Im Code sind es Islands (nur y ≤ 15) und Chaos.
- **Zustand:** keiner.
- **Sounds:** `orespawn:creepinghorror_living`, `…_hit`, `…_dead` (CreepingHorror.java:75, 79, 83); Lautstärke 0.65 (CreepingHorror.java:87).
- **Config:** `CreepingHorrorEnable`, `CreepingHorror_health/attack/defense`, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - `Monster`; `PanicGoal` wirkt bei Monstern nur, wenn `getLastHurtByMob` gesetzt ist, genau wie im Original.
  - `MoveThroughVillageGoal` braucht in 1.21.1 `GroundPathNavigation` mit `setCanOpenDoors`. Im Original ist das eine Vanilla-Task, also 1:1.
  - Selbstauflösung per `level.getDayTime() % 24000`.
  - Die eigene Zielsuche **zusätzlich** zum `HurtByTargetGoal` behalten; die Zielwahl nicht auf `NearestAttackableTargetGoal` umbauen, sonst ändert sich die Sortierung (`GenericTargetSorter` bevorzugt große Ziele und Creeper, GenericTargetSorter.java:15-35).

### Cricket - Cricket (`cricket`)

- **Rolle:** Winziges Umgebungsinsekt, `EntityAnimal`, springt und zirpt, nicht brütbar (Cricket.java:128-130).
- **Werte:**
  - XP 1 (Cricket.java:21), Hitbox 0.1 (Cricket.java:20), Geschwindigkeit 0.15 jeden Tick (Cricket.java:17, 63).
  - Sprung (serverseitig): wenn `jumpcount == 0`, mit 1/50 → `motionY += 0.55 + |rand·0.35|`, `posY += 0.25`, horizontal 0.3-0.55 in zufälliger Richtung; danach 50 Ticks Pause (Cricket.java:52-60, 72-78).
  - Singen: nach einem Zirpen 40 Ticks `singing` (Cricket.java:95, 66-71). Kein Fallschaden (Cricket.java:122-126), keine Schrittgeräusche (Cricket.java:112-113).
- **KI und Angriffe:** 0 `EntityAIPanic` 1.4; 1 WanderALot (8, 1.0) (Cricket.java:23-24). Kein Angriff.
- **Interaktion:** keine.
- **Drops:** keine (`dropFewItems` leer, Cricket.java:115-116). XP 1.
- **Spawnen:**
  - y ≥ 30 und höchstens 5 Crickets in 20/10/20 (Cricket.java:133, 137). Overworld: manifest.
  - Utopia `ambient` 5, 4-6 (BiomeGenUtopianPlains.java:52). Despawnt, wenn nicht persistent (Cricket.java:40-42).
- **Zustand:** DataWatcher 20 = singing (0/40), von `ModelCricket` gelesen (Cricket.java:37, 44-50; ModelCricket.java:99). Kein NBT.
- **Sounds:** `orespawn:cricket` mit 50 % Chance pro Living-Sound-Aufruf (Cricket.java:90-98); Lautstärke 0.7 (Cricket.java:109).
- **Config:** `CricketEnable` (manifest).
- **Portierung 1.21.1:**
  - Falle: `getLivingSound` setzt nebenbei Entity-Daten. In 1.21.1 wird `getAmbientSound` nur serverseitig über `playAmbientSound` benutzt; den Seiteneffekt trotzdem nach `playAmbientSound()` verschieben, statt ihn im Getter zu lassen.
  - Den Sprung in `aiStep` serverseitig über `setDeltaMovement` und `hasImpulse = true`.

### Cryolophosaurus - Cryolophosaurus (`cryolophosaurus`)

- **Rolle:** Kleiner feindlicher Dinosaurier, `EntityMob`.
- **Werte:** XP 10 (Cryolophosaurus.java:23), `fireResistance` 10 (Cryolophosaurus.java:24), Rüstung `Cryolophosaurus_stats.defense` (Cryolophosaurus.java:54-56), Geschwindigkeit 0.25 jeden Tick (Cryolophosaurus.java:67).
- **KI und Angriffe:**
  - Tasks wie CreepingHorror: 0 Swimming; 1 Panic 1.35; 2 MoveThroughVillage 1.0; 3 WanderALot (10, 1.0); 4 WatchClosest Spieler 8; 5 LookIdle. Target 1 HurtByTarget (Cryolophosaurus.java:25-31).
  - Racheziel mit 1/200 vergessen; Angriff mit 1/5: Ziel in **9/2/9**, Pfad 1.25. Bei Abstand² < 5 und `rand(12)==0 || rand(14)==1` Vanilla-Schlag (Cryolophosaurus.java:120-131, 193).
  - Ziele: alles Lebende außer `Alosaurus`, `TRex`, `Cryolophosaurus`, `Ghost`, `GhostSkelly`, `CaveFisher`, `GammaMetroid`, `EntityButterfly`, `Firefly`, `EntityMosquito`, `RockBase` und Kreativ-Spielern (Cryolophosaurus.java:134-187). `PlayNicely` sperrt (Cryolophosaurus.java:190-192).
- **Interaktion:** keine (Cryolophosaurus.java:111-113).
- **Drops:** `rand(10)`: 0 roher Hühnchen, 1 `uranium_nugget`, 2 `titanium_nugget`, sonst nichts (Cryolophosaurus.java:94-106); Menge nach Vanilla. XP 10.
- **Spawnen:**
  - `isValidLightLevel` und (Nacht **oder** y ≤ 50) (Cryolophosaurus.java:209).
  - Extreme: `monster` 26, 4-7 in `ChunkProviderOreSpawn2.getPossibleCreatures` (ChunkProviderOreSpawn2.java:379). Chaos: `monster` 5, 1-5 (BiomeGenUtopianPlains.java:365).
  - Spawner in `GenericDungeon.makeDungeon` mit 1/12 (GenericDungeon.java:181). Kein Overworld-Biomspawn (manifest `spawns` leer).
  - Despawnt, wenn nicht persistent.
- **Zustand:** keiner.
- **Sounds:** Living `orespawn:cryo_living` nur mit 1/6 (Cryolophosaurus.java:72-75); Hurt `orespawn:cryo_hurt`; Death `orespawn:cryo_death`; Lautstärke 0.75 (Cryolophosaurus.java:79-88).
- **Config:** `CryolophosaurusEnable`, `Cryolophosaurus_health/attack/defense`, `PlayNicely` (manifest).
- **Portierung 1.21.1:** Wie CreepingHorror, gleiche Basis `OreSpawnGroundHunter(range xz/y, exclusion predicate)`. Den Living-Sound-Zufall in `getAmbientSound` behalten; null ist dort zulässig.

### CrystalCow - Crystal Apple Cow (`crystal_apple_cow`)

- **Rolle:** Passive Kuh, `EntityCow` über `RedCow`. Dimension Crystal.
- **Werte:** Attribute, Größe und KI kommen komplett von Vanilla-`EntityCow` (manifest: alles null). `RedCow` vergisst das Racheziel mit 1/200 (RedCow.java:29-34).
- **KI und Angriffe:** Vanilla-Kuh-Tasks (Weizen locken und brüten, Panik, Melken).
- **Interaktion:** Vanilla (Melken, Weizen-Zucht). Das Kind ist eine CrystalCow (CrystalCow.java:24-31).
- **Drops:** Additiv in dieser Reihenfolge:
  - `rand(3) + rand(1+Looting)` × `crystalapple` (CrystalCow.java:16-18)
  - 1 Apfel fest (CrystalCow.java:19)
  - `RedCow`: `rand(3) + rand(1+Looting)` × Apfel (RedCow.java:15-17)
  - Vanilla-Kuh-Drops (RedCow.java:18)
- **Spawnen:**
  - Kein eigenes `getCanSpawnHere`. Es gilt die Vanilla-`EntityAnimal`-Regel, die einen Grasblock unter dem Tier verlangt (offen: ob die Crystal-Oberfläche Grass hat, nicht geprüft; sonst spawnt sie dort nie natürlich).
  - Crystal: `creature` 1, 1-4, Schalter `CowEnable` (BiomeGenUtopianPlains.java:130-131).
  - `canDespawn` immer false (RedCow.java:36-38).
- **Zustand:** keiner über Vanilla hinaus.
- **Sounds:** Vanilla-Kuh.
- **Config:** `CowEnable` (BiomeGenUtopianPlains.java:130; manifest).
- **Portierung 1.21.1:**
  - Die Klasse erbt von `Cow` (`RedCow` als Zwischenklasse). `getBreedOffspring` gibt den eigenen EntityType zurück.
  - Drops als Loot-Table mit `looting_enchant`/`enchanted_count_increase` für Apfel und Crystal Apple plus `minecraft:entities/cow` als Referenz. Das `rand(3)+rand(1+L)` ist kein Uniform-Bereich; 1:1 geht nur im Code (`dropCustomDeathLoot`).
  - Renderer: Vanilla-`CowModel` mit `crystal_cow.png` (RenderEnchantedCow.java:50-52, 56).
  - `removeWhenFarAway` → false.

### DeadIrukandji - DeadIrukandji (`dead_irukandji`)

- **Rolle:** Wurfgeschoss, `EntityThrowable` über `LaserBall` mit gesetztem Irukandji-Flag (DeadIrukandji.java:13; LaserBall.java:90-93). Der Spieler wirft es mit dem Item `deadirukandji` (ItemIrukandji.java:24); ein Dispenser schießt es ebenfalls (MyDispenserBehaviorDeadIrukandji.java:10).
- **Werte:**
  - Trefferschaden 100 als `causeThrownDamage(this, thrower)`, danach verschwindet es sofort (LaserBall.java:98-102).
  - Diese Prüfung steht **vor** allen Ausnahmen (Robots, reitende Dragons und Spieler), trifft also alles.
  - Lebensdauer 200 Ticks (LaserBall.java:180-184); Drehung +50° Pitch pro Tick (LaserBall.java:186-192).
  - Schwerkraft und Wurfgeschwindigkeit: Vanilla-`EntityThrowable`, von LaserBall nicht überschrieben.
- **KI und Angriffe:** keine.
- **Interaktion:** keine.
- **Drops:** Blocktreffer → das Item `deadirukandji` ×1, serverseitig (LaserBall.java:158-160). Weil `is_acid` gesetzt ist, gibt es keine Partikel, keine Explosion und keinen Sound (LaserBall.java:161, 193-195).
- **Spawnen:** nur durch Item oder Dispenser.
- **Zustand:** keiner; `my_index` 86 ist eine Konstante für den Renderer (DeadIrukandji.java:12, 40-42).
- **Sounds:** keine.
- **Config:** keine.
- **Portierung 1.21.1:**
  - `ThrowableItemProjectile`; `onHitEntity` macht 100 Schaden mit `damageSources().thrown(this, getOwner())` und `discard()`; `onHitBlock` droppt das Item und `discard()`.
  - Die Lebensdauer von 200 Ticks in `tick()`.
  - Renderer: `RenderItemUrchin` nimmt Index 86 aus dem Atlas `spinners.png` (RenderItemUrchin.java:36-38, RenderSpinner.java:57), Skalierung 0.5 (manifest). Einen eigenen Client-Renderer mit UV-Ausschnitt schreiben; `ThrownItemRenderer` zeigt das Item-Icon statt des Atlas-Frames.
  - `DispenseItemBehavior` über `DispenserBlock.registerBehavior` mit `ProjectileDispenseBehavior`.

### Dragon - Dragon (`dragon`)

- **Rolle:** Zähmbarer, reitbarer Flugdrache, `EntityTameable`. Zwei Elementtypen: 0 Feuer (`dragon.png`) und 1 Eis/Wasser (`whitedragon.png`) (RenderDragon.java:43-50). Er entsteht natürlich oder aus dem Baby Dragon (`Spyro`): per Diamant (Spyro.java:280-290) oder spontan mit 1/100000 pro Tick, wenn nicht persistent (Spyro.java:449-458). Ein Diamant macht ihn wieder zum Baby Dragon.
- **Werte:**
  - XP 100 (Dragon.java:79), `fireResistance` 1000, feuerimmun (Dragon.java:80-81), Geschwindigkeit 0.32 jeden Tick (Dragon.java:70, 591).
  - Rüstung fest 14 (Dragon.java:189-191); Leben 200 (Dragon.java:167).
  - Sprung +0.25 (Dragon.java:193-196). Kein Fallschaden (Dragon.java:134-138); nicht schiebbar (Dragon.java:315-317); atmet unter Wasser (Dragon.java:285-287).
  - Regeneration +2 HP mit 1/250 pro Server-Tick (Dragon.java:434-436); in Lava +1 HP bei jedem erfolgreichen Lava-Scan (Dragon.java:482-485).
  - I-Frames: nach einem Treffer 20 Ticks komplett immun (Dragon.java:364-366, 408, 593-595).
  - Tracking-Reichweite 128, Update-Frequenz 10, Velocity-Updates an (Dragon.java:122-132).
  - Reiter-Offset: Höhe 1.3 (Dragon.java:319-321), 0.65 in Blickrichtung versetzt (Dragon.java:1173-1178).
- **KI und Angriffe:**
  - **Tasks** (wirken nur am Boden, weil `onLivingUpdate` im Flug `super` nicht aufruft, Dragon.java:862-864): 0 Swimming; 1 `MyEntityAIFollowOwner` (1.1, max 12, min 2); 2 Tempt rohes Rindfleisch 1.25; 3 `MyEntityAIWander` 0.75; 4 WatchClosest Spieler 9; 5 LookIdle; 6 MoveIndoors (Dragon.java:83-89).
  - **Target-Tasks:** 1 `EntityAINearestAttackableTarget(EntityLiving, IMob.mobSelector)`, nur wenn `PlayNicely == 0` beim Konstruieren (Dragon.java:90-92); 2 HurtByTarget (Dragon.java:93). Beim Reiten wird `updateAITick` ausgesetzt (Dragon.java:525-530).
  - **Zustandswechsel Boden↔Flug** (`activity` 0/1):
    - am Boden, nicht sitzend, ohne Reiter, nicht Peaceful, mit 1/10 und Ziel in Sicht → Flug (Dragon.java:421-430)
    - Besitzer fliegt (Creative-Flug) und niemand reitet → Flug (Dragon.java:441-446)
    - Besitzerabstand² > 400 → Flug (Dragon.java:447-452)
    - gezähmt, am Boden, Besitzerabstand² > 144 → Flug (Dragon.java:611-616)
    - mit 1/50 ohne Ziel und ohne Reiter: 1/15 Flug, sonst Landung (Dragon.java:453-460)
    - jeder Treffer → Flug und Aufstehen (Dragon.java:382-383)
  - **Lava:** mit 1/25 ohne Ziel und ohne Reiter ein Schalen-Scan (horizontal 1..10, ab 6 in Zweierschritten; vertikal ≤ 4) nach Lava. Gefunden → landen und mit 1.0 hinlaufen (Dragon.java:461-481, 202-283).
  - **Flug ohne Reiter** (`fly_without_rider`, Dragon.java:619-846):
    - Festsitzen > 50 Ticks → 100 Ticks „unstick", Ziel fallen lassen (Dragon.java:656-665). Neuer Wegpunkt mit 1/300 (Dragon.java:680-682).
    - Besitzerabstand² > 144 → Ziel fallen lassen, zurück zum Besitzer (Dragon.java:683-696).
    - Zielsuche mit 1/9 pro Tick, wenn nicht zu weit weg, nicht unstick, nicht `flyaway`, nicht Peaceful (Dragon.java:700). Suchbereich 20/20/20 (Dragon.java:540).
    - Ziele: `EntityMob`, `Mothra`, `Kraken`; nie Spieler; ausgenommen `LurkingTerror`, `EnderReaper`, `TerribleTerror`, `LeafMonster`, `CreepingHorror`, `Triffid` (Dragon.java:532-534).
    - Gezähmt und HP < 25 %: Flucht in Gegenrichtung (Dragon.java:703-709).
    - Sonst Verfolgung auf Ziel +1 (Dragon.java:710-715). Nahkampf bei Abstand² < `(5 + w/2)²` → `attackEntityAsMob`, danach 5..14 Ticks `flyaway` (Dragon.java:716-720).
    - Fernkampf bei Abstand² < 256, nicht im Wasser und `DragonFire ≥ 1` (Dragon.java:721). Abschusspunkt 2.25 vor dem Kopf, +1.25 hoch (Dragon.java:633-634, 722-723). Ohne eigenen Cooldown; die Rate ergibt sich aus der 1/9-Chance pro Tick.

      | Typ | Fire 1 | Fire 2 |
      |---|---|---|
      | 0 Feuer | `EntitySmallFireball` (Vanilla), `random.bow` (Dragon.java:725-731) | `BetterFireball`: 10 Schaden + 5 s Feuer; halbiert die HP von Zielen mit Breite×Höhe > 30 außer Royalty, Godzilla, PitchBlack, Kraken; `random.fuse` (Dragon.java:732-738; BetterFireball.java:230-234 Halbierung, :236-238 Schaden) |
      | 1 Eis | `WaterBall` Speed 1.4, Streuung 5: 2 Schaden (5 gegen Creeper) und löscht Feuer; `random.bow` (Dragon.java:740-750; WaterBall.java:39-42) | `IceBall` special: 16 Schaden + Explosion 3.0 bei `mobGriefing`; `random.fuse` (Dragon.java:751-763; LaserBall.java:97, 153, 172-174) |

    - Wegpunkte (bis 50 Versuche, Luft und sichtbar): mit Besitzer um den Besitzer ±(4..13), bzw. ±(0..5) wenn er fliegt; ohne Besitzer ±(16..25). y `rand(9 + 2·owner_flying) − 4` (Dragon.java:775-809).
    - Hindernisauftrieb: Blöcke unter der Flugbahn → `motionY` und `posY` +0.05·Faktor (Dragon.java:810-824).
    - Speed-Faktor 0.5; fliegt der Besitzer: 1.75, und 3.5 bei Besitzerabstand² > 49 (Dragon.java:825-837). Beschleunigung x/z `(signum − m)·0.15·f`, y `·0.21·f`; `moveForward = 0.75·f`; Gier /4; danach explizit `moveEntity` (Dragon.java:838-845).
  - **Flug mit Reiter** (Dragon.java:893-1164), nur serverseitig:
    - `motionX/Z` auf ±2 geklemmt (Dragon.java:895-906). Block 1.25 unter dem Drachen → +0.03 `motionY` und +0.1 `posY`, sonst −0.018 (Dragon.java:908-916).
    - Hindernisauftrieb ×0.07 (Dragon.java:917-931); `motionY` ≤ 2 (Dragon.java:932-934).
    - Die Gier folgt der Reiter-Gier, gedämpft mit `|1.85 − v|` geklemmt auf 0.01..0.9 (Dragon.java:936-956). Pitch = 2·v (Dragon.java:961).
    - Vorwärts (`moveForward > 0`): `deltasmooth` wächst um 0.0025 pro Tick bis 0.025; Höchstgeschwindigkeit 0.95. Rückwärts: bis −0.02, Höchstgeschwindigkeit 0.35 (Dragon.java:855, 986-1026).
    - Steigen per Taste: `OreSpawnMain.flyup_keystate ≠ 0` → `motionY += 0.03 + v·0.036` (Dragon.java:971-974). Die Taste kommt über `RiderControlMessage` → `RiderControlMessageHandler` (RiderControlMessageHandler.java:17).
    - Feuern über Strafe-Tasten, wenn `fireballticker == 0`. Abschusspunkt 4 vor, −0.25 hoch, zielt nach Kopf-Gier und Pitch des Reiters (Dragon.java:1036-1144):

      | Typ | Strafe > 0 (A) | Strafe < 0 (D) |
      |---|---|---|
      | 0 | `BetterFireball` small (5 Schaden + 5 s Feuer), Beschleunigung 0.15, `setNotMe` (trifft keine Dragons/Spieler), Cooldown 10, `random.bow` | `BetterFireball` groß (10 Schaden), Beschleunigung 0.1, Cooldown 20, `random.fuse` |
      | 1 | `WaterBall` 1.4/5, Cooldown 5, `random.bow` | `IceBall` special, Bewegung ×2, Cooldown 15, `fireworks.launch` |

      (Cooldowns Dragon.java:1065, 1091, 1113, 1141; Schaden BetterFireball.java:236-243)
    - Danach `moveEntity` und Luftwiderstand 0.985/0.94/0.985 (Dragon.java:1145-1148).
    - Schiebt Entities in 2.25/2.0/2.25 an (Dragon.java:1149-1159).
    - `fly_with_rider`: mit 1/7 und nicht Peaceful Zielsuche (1/250 Ziel verwerfen). In Reichweite `(7 + w/2)²` automatischer Nahkampf (Dragon.java:490-523).
  - **Nahkampfschlag** (Dragon.java:343-359): 35 Schaden fest, das Attribut wird ignoriert; ×2 gegen `Kraken`. Rückstoß horizontal 1.75, vertikal 0.1, bei totem Ziel oder Spieler doppelt.
  - **Schadensannahme** (Dragon.java:361-419):
    - immun gegen `cactus`, `inFire`, `onFire`, `lava`, `inWall`
    - Typ 0 zerstört eintreffende `BetterFireball` und `EntitySmallFireball` ohne Schaden; Typ ≠ 0 dasselbe mit `IceBall` und `WaterBall`
    - Angreifer `Dragon`/`Spyro` wird ignoriert
    - Ein gezähmter Drache nimmt Spielerschaden **erst an** und gibt dann false zurück, ohne den Spieler als Ziel zu setzen (Dragon.java:407-412). Andere Angreifer werden zum Ziel, Pfad 1.2.
- **Interaktion** (Dragon.java:1193-1392):

  | Bedingung | Item | Wirkung |
  |---|---|---|
  | wild, Abstand² < 25 | rohes Rindfleisch | Zähmen mit `rand(5)==1` (1/5), volle Heilung; sonst Rauch. Item −1 (Dragon.java:1199-1222) |
  | gezähmt, nicht Besitzer | beliebig | false (Dragon.java:1225-1227) |
  | Besitzer, Abstand² < 16 | leere Hand | aufsteigen, Flug, Aufstehen (Dragon.java:1228-1235) |
  | Besitzer, < 25 | rohes Rindfleisch | volle Heilung (Dragon.java:1236-1252) |
  | Besitzer, < 25 | Dead Bush | entzähmen (Dragon.java:1253-1268) |
  | Besitzer, < 25 | Eis | `DragonFire = 0`, Chat „Dragon fireballs extinguished." (Dragon.java:1269-1284) |
  | Besitzer, < 25 | Feuerzeug | `DragonFire = 1`, „Dragon fireballs lit!" (Dragon.java:1285-1300) |
  | Besitzer, < 25, Fire > 0 | Schwarzpulver | `DragonFire = 2`, „Dragon fireballs supercharged!" (Dragon.java:1301-1316) |
  | Besitzer, < 25 | Schneeball | Typ 1 (Eis) (Dragon.java:1317-1331) |
  | Besitzer, < 25 | Kohle | Typ 0 (Feuer) (Dragon.java:1332-1346) |
  | Besitzer, < 25, Server | Diamant | wird Baby Dragon (`Spyro`), gezähmt auf den Klickenden (Dragon.java:1347-1367) |
  | Besitzer, < 25 | Name Tag | benennen (Dragon.java:1368-1378) |
  | Besitzer, < 25 | jedes andere Item | Sitzen umschalten, `activity = 0` (Dragon.java:1379-1389) |

  Items werden außer im Kreativmodus verbraucht. Zähm-Effekt: 20 Partikel `heart` bzw. `smoke` (Dragon.java:1180-1191). Keine Zucht (`createChild` null, Dragon.java:1450-1452).
- **Drops:** 1-6 × rohes Rindfleisch (`1 + rand(6)`), ±4 gestreut auf y+1 (Dragon.java:337-341, 327-335). XP 100.
- **Spawnen:**
  - Tagsüber, kein anderer Dragon in 16/6/16, und (Islands oder y ≥ 50) (Dragon.java:559-566).
  - Islands: `ambient` 1, 1-2 (BiomeGenUtopianPlains.java:84). Kein Overworld-Spawn (manifest).
  - `canDespawn`: nur ohne Reiter, ungezähmt und nicht persistent (Dragon.java:1454-1456).
  - Research (01-mobs.md:759) nennt „Unstable Ant Danger Dimension". Im Code steht nur die Islands-Biomliste; das Ant-Portal ist nicht Teil dieser Klasse.
- **Zustand:**
  - DataWatcher 20 = attacking (0/1), 21 = activity (0 Boden, 1 Flug), 22 = dragontype (0/1), 24 = DragonFire (0/1/2, Init **1**) (Dragon.java:146-149). Die Setter für 20/21/24 tun auf dem Client nichts (Dragon.java:1402-1429).
  - NBT `DragonAttacking`, `DragonActivity`, `DragonFire`, `DragonType` (Dragon.java:1458-1472), plus Vanilla-Tameable.
  - `RenderInfo renderdata` ist ein clientseitiger Animationszustand, den `ModelDragon` liest und schreibt (Dragon.java:174-187; ModelDragon.java:352-533).
- **Sounds:** Living `orespawn:roar` nur bei attacking, ohne Reiter und nicht sitzend (Dragon.java:289-297); Hurt `orespawn:alo_hurt`; Death `orespawn:alo_death`; Lautstärke 0.6; Pitch 0.75 (Dragon.java:299-313); im Flug alle 21 Ticks `orespawn:MothraWings` 0.5/1.0 (Dragon.java:596-604); Lava-Heilung `splash` (Dragon.java:484); Schüsse `random.bow`, `random.fuse`, `fireworks.launch`.
- **Config:** `DragonEnable` (Biomliste), `PlayNicely` (manifest). Stats sind fest verdrahtet, `Dragon_*`-Schlüssel gibt es nicht.
- **Portierung 1.21.1:**
  - `TamableAnimal`, `PlayerRideableJumping` nicht nötig. Vier `EntityDataAccessor<Integer>`.
  - **Größte Falle, das Reiten:** In 1.21.1 simuliert der **Client** ein `LivingEntity`, dessen `getControllingPassenger()` der lokale Spieler ist (`isControlledByLocalInstance`), und schickt die Position per `ServerboundMoveVehiclePacket`. Die Original-Steuerung läuft komplett serverseitig in `onLivingUpdate`. Zwei Wege:
    1. Flugphysik in `travel()`/`tickRidden()` auf beiden Seiten mit `getRiddenInput` (rider `xxa`/`zza`, die `ServerboundPlayerInputPacket` in 1.21.1 überträgt).
    2. `getControllingPassenger()` → null und die Serverphysik 1:1 behalten; dann ruckelt es beim Client.

    Weg 1 empfohlen; das Schießen bleibt serverseitig.
  - `flyup_keystate` ist im Original **eine globale statische Variable** für alle Spieler. Im Port als per-Spieler-Zustand über ein `CustomPacketPayload` umsetzen (Client-Keybinding unter `com.swbr.orespawn.client`).
  - Die Flugphase umgeht Vanilla-Bewegung und AI, weil `super.onLivingUpdate` fehlt. In 1.21.1 `aiStep()` überschreiben und `super.aiStep()` nur bei `activity == 0` aufrufen. Achtung: `aiStep` erledigt auch Feuer, Effekte und `pushEntities`.
  - Damage-Type-Strings → `DamageTypes.CACTUS/IN_FIRE/ON_FIRE/LAVA/IN_WALL`. Projektil-Immunität per `instanceof` auf die portierten Projektilklassen; `EntitySmallFireball` → `SmallFireball`.
  - `IMob.mobSelector` → `e instanceof Enemy`.
  - `RenderInfo` ist eine reine Datenklasse; kann als transientes Feld in der Entity bleiben, gehört aber sauberer in den Renderer.
  - Diamant → Baby Dragon braucht den portierten `Spyro`-EntityType.

### Dragonfly - Dragonfly (`dragonfly`)

- **Rolle:** Umgebungsjäger, `EntityAnimal`, fliegt und frisst kleine Flieger und Pferde, nicht brütbar.
- **Werte:**
  - XP 5 (Dragonfly.java:23), `fireResistance` 5 (Dragonfly.java:25).
  - Schaden 2.0 fest im Schlag (Dragonfly.java:85-88); `motionY *= 0.6` (Dragonfly.java:82).
  - Kein Fallschaden (Dragonfly.java:148-152); `collideWithEntity` leer (Dragonfly.java:69-70).
  - Raytrace-Augenhöhe +0.25 (Dragonfly.java:91).
- **KI und Angriffe:** Keine Tasks. `updateAITasks` (Dragonfly.java:94-142):
  - Neuer Wegpunkt mit 1/300 oder bei Abstand² < 2.1, Versatz x/z ±(5..9), y `rand(5)−2`, 50 Versuche (Dragonfly.java:105-121).
  - **Nur wenn kein neuer Wegpunkt gewählt wurde:** mit 1/12, nicht Peaceful, Ziel in 10/6/10 → Wegpunkt auf Ziel +1; bei Abstand² < 6 Schlag (Dragonfly.java:122-131, 182).
  - Ziele: `EntityAnt`, `EntityButterfly`, `Cockateil`, `EntityMosquito`, `Firefly`, `EntityHorse` (nur bei `DragonflyHorseFriendly == 0`); keine Spieler (Dragonfly.java:174-176). `PlayNicely` sperrt (Dragonfly.java:179-181).
  - Flug: x/z `(signum·0.5−m)·0.3`, y `(signum·0.7−m)·0.2`, Gier /4, `moveForward 1.0` (Dragonfly.java:135-141). Wird sie getroffen, fliegt sie zum Angreifer (Dragonfly.java:158-165).
- **Interaktion:** keine.
- **Drops:** `rand(6)`: 0 Goldnugget, 1 `uranium_nugget`, 2 `titanium_nugget`, sonst nichts (Dragonfly.java:197-209); Menge nach Vanilla. XP 5.
- **Spawnen:**
  - y ≥ 50 und tagsüber (Dragonfly.java:168). Overworld: manifest.
  - Extreme: `ambient` 2, 1-3 (ChunkProviderOreSpawn2.java:397); Chaos: 2, 2-4 (BiomeGenUtopianPlains.java:303).
  - Despawnt, wenn nicht persistent (Dragonfly.java:41-43).
- **Zustand:** keiner.
- **Sounds:** `orespawn:dragonfly_living`, `…_hurt`, `…_death` (Dragonfly.java:54, 58, 62); Lautstärke 0.25 (Dragonfly.java:46).
- **Config:** `DragonflyEnable`, `DragonflyHorseFriendly` (Default 0), `PlayNicely` (manifest).
- **Portierung 1.21.1:** `Animal` mit `WaypointFlight`-Helfer. `EntityHorse` → `AbstractHorse` (umfasst in 1.21.1 auch Esel, Maultier und Lamas; 1:1 wäre `Horse` plus `Donkey`/`Mule`, da 1.7.10-`EntityHorse` alle Typen war). Den `else-if` zwischen Wegpunktwahl und Angriff erhalten.

### DungeonBeast - Dungeon Beast (`dungeon_beast`)

- **Rolle:** Feindlicher Bodenmob der Crystal-Dimension, `EntityMob`, an Spawner und Strukturen gebunden.
- **Werte:**
  - XP 60 (DungeonBeast.java:28), `fireResistance` 10 (DungeonBeast.java:29).
  - Rüstung `DungeonBeast_stats.defense` (DungeonBeast.java:91-93), Geschwindigkeit 0.29 jeden Tick (DungeonBeast.java:68).
  - Immun gegen `inWall` und `cactus` (DungeonBeast.java:169-178).
- **KI und Angriffe:**
  - Tasks: 0 Swimming; 1 WanderALot (14, 1.0); 2 WatchClosest Spieler 8; 3 LookIdle. Target 1 HurtByTarget (DungeonBeast.java:33-37).
  - Mit 1/8 pro Tick Ziel in 16/3/16 (DungeonBeast.java:150, 234). Bei Abstand² < 8 `attacking = 1` und Schlag mit `rand(7)==0 || rand(8)==1` über Vanilla-`attackEntityAsMob` (DungeonBeast.java:153-157, 141-143); sonst Pfad 1.2 (DungeonBeast.java:160). Ohne Ziel `attacking = 0` (DungeonBeast.java:164).
  - Ziele: alles Lebende außer `MyUtils.isIgnoreable` (RockBase, EntityAnt, EntityButterfly, EntityMosquito, Dragonfly, Firefly, Cricket, Cockateil, Termite, Ghost, GhostSkelly, Elevator; MyUtils.java:17-19), `Rat`, `DungeonBeast`, `Rotator`, `Peacock`, `Irukandji`, `Skate`, `Whale`, `Flounder` und Kreativ-Spielern (DungeonBeast.java:180-228). `PlayNicely` sperrt (DungeonBeast.java:231-233).
- **Interaktion:** keine (DungeonBeast.java:137-139).
- **Drops:** `rand(4)`: 1 `crystalpink_ingot`, 2 `crystalapple`, 3 Holzstamm, 0 nichts (DungeonBeast.java:123-135); Menge nach Vanilla. XP 60.
- **Spawnen:**
  - Spawner mit Name „Dungeon Beast" in x/z −3..2 und y 0..4 → immer erlaubt (DungeonBeast.java:259-273).
  - Sonst `isValidLightLevel` (DungeonBeast.java:274). In Crystal zusätzlich y 25..28 und mindestens 6 Luftblöcke in der 3×3-Ebene über dem Kopf (DungeonBeast.java:277-292).
  - Overworld Roofed Forest: manifest. Crystal `monster` 30, 4-6 (BiomeGenUtopianPlains.java:152); Chaos 2, 1-5 (:371).
  - Strukturen: Crystal Battle Tower mit 2 Spawnern (GenericDungeon.java:4928, 4933); Round Rotator mit 4 (GenericDungeon.java:6260-6275); `makeDungeon` mit 1/12 (GenericDungeon.java:208); `OreSpawnWorld.addCrystalChest`: 2/3 Spawner, davon 1/2 Dungeon Beast (OreSpawnWorld.java:1820-1841, Spawner ab :1833, Name :1838).
  - Despawnt, wenn nicht persistent (DungeonBeast.java:63-65).
  - Research (01-mobs.md:93) sagt „deep underground". Im Code ist das Band y 25..28 in Crystal, sonst nur Lichtregel.
- **Zustand:** DataWatcher 20 = attacking (DungeonBeast.java:49, 249-255), von `ModelDungeonBeast` gelesen. `RenderInfo` als Client-Animationszustand (DungeonBeast.java:76-89; ModelDungeonBeast.java:405-559). Kein NBT.
- **Sounds:** Hurt `orespawn:dbhit` (3 Varianten, sounds_dump), Death `orespawn:dbdead`, Lautstärke 0.8 (DungeonBeast.java:107-117); Living keiner.
- **Config:** `DungeonBeastEnable`, `DungeonBeast_health/attack/defense`, `PlayNicely` (manifest).
- **Portierung 1.21.1:** `Monster`; den Spawner-Scan durch `MobSpawnType.SPAWNER` in `checkSpawnRules` ersetzen. Die Crystal-Höhenregel in einem eigenen `SpawnPlacements`-Prädikat. `MyUtils.isIgnoreable` als gemeinsame Hilfsmethode portieren; sie wird von vielen Klassen genutzt.
