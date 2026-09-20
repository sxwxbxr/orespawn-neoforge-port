# Verhalten: entity-11

Dieser Stapel umfasst vierzehn Klassen sehr unterschiedlicher Art. **Nightmare** (`PitchBlack`) ist ein fliegender, größenskalierter Boss mit eigener Flug-KI. Dazu kommen die fünf Roboter der Village-Dimension: **Bomb-Omb**, **Robo-Pounder**, **Robo-Gunner**, **Robo-Warrior** und **Robo-Sniper**. Vier davon kämpfen mit Nahkampf, Blockzerstörung oder `LaserBall`-Geschossen; die Bomb-Omb explodiert. Es folgen vier gewöhnliche Feinde (**Pointysaurus**, **Rat**, **Scorpion**, **Rotator**) und drei friedliche oder halbfriedliche Tiere (**Apple Cow**, **Rubber Ducky**, **Ruby Bird**). Den Abschluss bildet der **Rock** (`RockBase`), ein KI-loses Deko-Entity, das Stein-Items fallen lässt. Alle Kämpfer haben dieselbe Grundstruktur: eine Vanilla-Aufgabenliste für das Umherlaufen, dazu eine handgeschriebene Zielsuche in `updateAITasks` über `findSomethingToAttack()` und `GenericTargetSorter`, blockiert durch `OreSpawnMain.PlayNicely`. Dasselbe gilt für den Spawner-Zweig in `getCanSpawnHere`: Er sucht einen `mob_spawner` in der Nähe, dessen Entity-Name passt. Beim Lesen sind mehrere Befunde aufgefallen, die ein 1:1-Port bewusst entscheiden muss:
- Der Schild des Robo-Warrior wird nur im Client-Modell gesetzt.
- Die Pfeil-Immunität des Rotator greift nie.
- Rotator spielt seinen Living-Sound ohne Namespace ab.
- Rattenbesitzer werden per Namen gesucht, obwohl eine UUID gespeichert ist.
- `RubyBird.setFlyUp()` wird durch die Java-Initialisierungsreihenfolge wirkungslos.
- Frisch gespawnte Nightmares geben 200 XP statt 100·Scale.

## Gemeinsame Fakten für diesen Stapel

**Dimensionen** (manifest `dimensions`): `DimensionID` = Dimension-Utopia, `DimensionID2` = Dimension-Extreme, `DimensionID3` = Dimension-VillageMania, `DimensionID4` = Dimension-Islands, `DimensionID5` = Dimension-Crystal, `DimensionID6` = Dimension-Chaos. Die IDs werden als `BaseDimensionID + n` berechnet, Default 80 (OreSpawnMain.java:1269-1271, manifest).

**Spawnlisten der OreSpawn-Dimensionen** stehen in `BiomeGenUtopianPlains`:
- Die Konstruktor-Liste gilt für Utopia.
- `setIslandCreatures` (BiomeGenUtopianPlains.java:62), `setCrystalCreatures` (:121) und `setChaosCreatures` (:254) setzen zuerst alle Listen auf `null` und bauen sie neu auf (:63-66, :122-125, :255-258).
- `setVillageCreatures` (:192) setzt **nicht** zurück. Die Village-Dimension hat also die Utopia-Konstruktorliste **plus** die eigene.

**Strukturgenerator** `OreSpawnWorld.generate()` (Gates nach Dimension):
- Utopia :28 → `addRubyDungeon` :37
- VillageMania :101 → `addDamselInDistress` :109
- Islands :119 → `addD4Castle` :123, `addD4RobotLab` :136, `addD4RubyDungeon` :142, `addD4NightmareRookery` :151
- Crystal :176 → `addRotatorStation`, `addCrystalHauntedHouse`, `addRoundRotator` :180 sowie `addCrystalBattleTower` :181
- Overworld (dim 0, `DisableOverworldDungeons == 0`) :269 → `addBouncyCastle` :303, `addRubberDuckyPond` :306

**`LaserBall` (die Geschosse der Robo-Gunner, -Warrior und -Sniper)**:
- Schaden am getroffenen Entity 16, dazu `setFire(1)` (LaserBall.java:97, :153-156).
- Treffer auf `Robot2`, `Robot3`, `Robot4`, `Robot5` und `GiantRobot` machen nichts; die Kugel verschwindet (LaserBall.java:109-124). Roboter treffen sich also nicht gegenseitig. `Robot1` ist davon ausgenommen.
- Mit `setSpecial()` erzeugt der Einschlag zusätzlich eine Explosion der Stärke 3.0 (mobGriefing) (LaserBall.java:172-173).

**`MyUtils.isIgnoreable`** schließt `RockBase`, `EntityAnt`, `EntityButterfly`, `EntityMosquito`, `Dragonfly`, `Firefly`, `Cricket`, `Cockateil` (also auch `RubyBird`), `Termite`, `Ghost`, `GhostSkelly` und `Elevator` von jeder Zielsuche aus (MyUtils.java:17-18).

**`GenericTargetSorter`** sortiert nach Abstand². Bei Creepern wird dieser Wert halbiert; ist Höhe·Breite > 1, wird er durch dieses Produkt geteilt (GenericTargetSorter.java:15-27). Große Ziele und Creeper werden also bevorzugt.

**`MyEntityAIWanderALot(creature, xzRange, speed)`** (MyEntityAIWanderALot.java:18-24).

**Vanilla-Bytecode geprüft** (javap auf `reference/jar/mcp/client-1.7.10.jar`, Namen über `joined.srg`):
- `EntityDamageSourceIndirect.getEntity()` (`rq.j`) liefert den **Schützen** (drittes Konstruktor-Argument). Der Pfeil kommt nur über `getSourceOfDamage()` (`rq.i`). `DamageSource.causeArrowDamage` baut `new rq("arrow", arrow, shooter)`.
- `EntityPlayer` (`yz`) erbt von `EntityLivingBase` (`sv`), **nicht** von `EntityLiving` (`sw`).

**SRG-Namen**: `func_145881_a` = `TileEntityMobSpawner.getSpawnerBaseLogic`; `func_152113_b` = `EntityTameable.getOwnerUUIDString` (liefert den Owner-String); `func_152115_b` = Owner-String setzen; `func_152114_e` = `isOwner(EntityLivingBase)`; `func_110163_bv` = `enablePersistence` (methods.csv bzw. joined.srg). Die Bedeutung der `func_1521*`-Methoden ist aus joined.srg und der Verwendung abgeleitet; methods.csv (stable-12) kennt sie nicht.

**Gemeinsame Port-Regeln**:
- Den Spawner-Zweig von `getCanSpawnHere` nicht über einen Blockscan nachbauen, sondern in `checkSpawnRules` über `MobSpawnType.SPAWNER` abbilden.
- `isValidLightLevel` (1.7.10: Himmelslicht > rand(32) → nein, sonst Licht ≤ rand(8)) entspricht nicht dem 1.21.1-Default `Monster.isDarkEnoughToSpawn`. Für einen 1:1-Port eine eigene Prüfung schreiben.
- `experienceValue` wird zu `xpReward`.
- `DataWatcher` wird zu `SynchedEntityData`.
- `mobGriefing` über `EventHooks.canEntityGrief`.
- `PlayNicely` als ModConfigSpec-Wert.

---

### PitchBlack - Nightmare (`nightmare`)

- **Rolle:** Fliegender Boss auf Basis von `EntityMob`, feindlich, in fünf Größen. Er wechselt zwischen Boden-KI (Activity 0) und eigener Flug-KI (Activity 1).
- **Werte:**
  - **Scale** ist ein Zufallswurf in `entityInit` (PitchBlack.java:73-101): Default 0.5; `rand(4)==1` → 1.0; `rand(8)==2` → 2.0; `rand(32)==3` → 3.0; `rand(64)==4` → 4.0. Spätere Treffer überschreiben frühere.
  - Config `NightmareSize` 1..5 erzwingt 0.5/1/2/3/4 (PitchBlack.java:102-116); Default 0 = zufällig (manifest).
  - Gespeichert wird Scale als `int(scale·10.0001)` in DW 22 (PitchBlack.java:152-162).
  - **Leben** `PitchBlack_stats.health · scale` = 250·Scale (PitchBlack.java:210-212, manifest), also 125/250/500/750/1000. 1000 liegt unter dem 1024-Deckel.
  - **Rüstung** `defense + (int)(2·scale)` = 11..18 (PitchBlack.java:164-166).
  - **Schaden** `attack · scale` = 15..120 (PitchBlack.java:277-285).
  - **Laufgeschwindigkeit** 0.2 + 0.1·Scale, wird jeden Tick neu gesetzt (PitchBlack.java:219-220).
  - **Hitbox** 2.5·Scale × 3.5·Scale, jeden Tick in `onUpdate` neu gesetzt (PitchBlack.java:222). Die Konstruktorgröße 2.0×3.0 (PitchBlack.java:36) ist damit überholt; das Manifest nennt 2.0×3.0.
  - **XP-Befund:** `entityInit` setzt `experienceValue = 100·Scale` und `fireResistance = 25·Scale` (PitchBlack.java:118-119). Der Konstruktorrumpf läuft aber *nach* `entityInit`, weil Entity(World) `entityInit()` im Super-Konstruktor aufruft, und überschreibt auf 200 bzw. 25 (PitchBlack.java:38-40). Ein frisch gespawnter Nightmare gibt deshalb 200 XP. Erst nach einem NBT-Laden gilt 100·Scale (PitchBlack.java:127-128).
  - **Regeneration:** Chance 1/250 je Tick, heilt 1+Scale (PitchBlack.java:231-232).
  - **Treffersperre:** Nach jedem angenommenen Treffer 20 Ticks immun (`damage_ticker`, PitchBlack.java:395-398); `updateAITasks` zählt herunter (:307-309).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `EntityAIMoveThroughVillage(1.0,false)`, 2 `MyEntityAIWanderALot(16,1.0)`, 3 `EntityAIWatchClosest(EntityPlayer,10)`, 4 `EntityAILookIdle` (PitchBlack.java:42-46). Keine targetTasks. Die Aufgaben laufen nur bei Activity 0 (PitchBlack.java:310-313).
  - Umschalten in `onUpdate`, nur im Tick mit dem Heilwurf 1/250:
    - Mit 1/5: Liegt innerhalb von 10 Blöcken unter ihm ein Nicht-Luft-Block (bei `posY ≤ 10` gilt das immer) und findet er kein Ziel → Activity 0 (landen).
    - Mit 4/5: Activity 1 (abheben), Pfad löschen (PitchBlack.java:233-257).
    - Zusätzlich bei Activity 0 jeden Tick mit 1/10 Zielsuche; bei Treffer Activity 1 (PitchBlack.java:259-265).
  - **Flug** (Activity 1):
    - Mit 1/150 oder bei Abstand² < 2.1 zum Wegpunkt ein neuer Wegpunkt: x/z je ±(`rand(20) + 5·(int)scale`), y `rand(11)-5`. Der Zielblock muss Luft und per Raytrace sichtbar sein, bis zu 50 Versuche (PitchBlack.java:323-339).
    - Sonst mit 1/8 Zielsuche. Angriffsreichweite² = (5 + Zielbreite/2 + Scale)², bei `EntityDragon`, `Godzilla` und `GodzillaHead` mindestens 100. Der Wegpunkt wird auf Ziel +2 y gesetzt; liegt das Ziel in Reichweite, folgt `attackEntityAsMob` (PitchBlack.java:340-365).
    - Bewegung: Geschwindigkeit 0.5 + Scale/10; horizontal `signum·speed` mit Glättung 0.33, vertikal `signum·0.7` mit Glättung 0.2, Drehung Gier-Differenz/5, `moveForward = 0.1 + speed` (PitchBlack.java:366-376). Jeden Tick zusätzlich `motionY *= 0.6` (PitchBlack.java:230).
  - **Angriff:**
    - Gegen `EntityDragon`: Explosions-DamageSource über `attackEntityFromPart`, mit 1/8 auf den Kopf, sonst auf den Körper, Schaden 30·Scale (PitchBlack.java:271-283).
    - Gegen alle anderen: `causeMobDamage` mit 30·Scale. Rückstoß horizontal 1.15·Scale, vertikal 0.08·Scale; bei Spielern oder toten Zielen vertikal ×2 (PitchBlack.java:285-293).
  - **Zielsuche** in einer Box `expand(16+6·Scale, 10+4·Scale, 16+6·Scale)` (PitchBlack.java:524-526). Nie Ziel sind: ignorierbare Entities, nicht sichtbare, `PitchBlack`, `EnderReaper`, `LeafMonster`, `TerribleTerror`, `LurkingTerror`, `CreepingHorror`, `Island`, `IslandToo`, `Triffid` und Kreativspieler. Alles andere Lebende ist Ziel (PitchBlack.java:464-518).
  - **Bei Schaden:** Der Wegpunkt springt auf den Angreifer (+2 y), Activity 1 (PitchBlack.java:400-405).
  - Kein Fallschaden (PitchBlack.java:383-387); löst Druckplatten aus (:389-391).
- **Interaktion:** keine eigene.
- **Drops** (`dropFewItems`, verstreut um ±4·Scale; `getDropItem` bleibt dadurch ungenutzt):
  - `rotten_flesh` ×(3 + rand(2 + (int)(5·Scale))); pro Stück zusätzlich mit je 1/10 `feather`, `string`, `flint` oder `beef` (PitchBlack.java:554-569).
  - Immer 1× `nightmarescale` und 1× `item_frame` (PitchBlack.java:570-571).
  - `zookeeper` ×(2 + (int)Scale + rand(2 + (int)(5·Scale))) (PitchBlack.java:572-574).
  - XP siehe Werte.
- **Spawnen:**
  - **Spawner-Zweig:** Liegt im Bereich x/z −3..+2, y 0..4 ein Spawner mit dem Namen „Nightmare“, wird Scale auf höchstens 1.0 gekappt und der Spawn erlaubt (PitchBlack.java:410-429). Befund: Leben und Rüstung bleiben auf dem ursprünglich gewürfelten Wert, weil die Attribute schon gesetzt sind (PitchBlack.java:51, :423).
  - **Sonst** muss das Licht passen (`isValidLightLevel`) und es darf nicht Tag sein. In Chaos (`DimensionID6`) darf kein anderer Nightmare in ±16 sein. Bei Scale < 1.1 ist der Spawn damit erlaubt. Größere brauchen eine luftgefüllte Säule über sich: ±ix mit ix = 1 (2 bei Scale > 3.1), Höhe 1..3·ix (PitchBlack.java:430-461).
  - **Spawnlisten:** Islands `setIslandCreatures` (15, 3-6) (BiomeGenUtopianPlains.java:108); Chaos (1, 1-2) (:329).
  - **Strukturen:**
    - Nightmare Rookery in Islands (OreSpawnWorld.java:151 → GenericDungeon.java:5266).
    - Spawner in `addLevelDecorations` (GenericDungeon.java:541-556), `addLevelDecorationsQ` (:6739-6754), `makeEnderDragonHospital` (:2975-2999) und `makeShadowDungeon` (:1530); die Dimensionen dieser Aufrufer sind nicht verfolgt.
    - `NightmareDungeon.java:73` setzt ebenfalls einen Spawner, eine Instanziierung fand grep nicht.
  - **Despawn** nur tagsüber und ohne Persistenz (PitchBlack.java:183-185).
  - Recherche nennt „Danger (Unstable Ant) Dim.“; der Code spawnt in Islands und Chaos.
- **Zustand:** DW 20 = attacking (int), DW 21 = activity (0 Boden / 1 Flug), DW 22 = Scale·10 (int) (PitchBlack.java:59-61). NBT `Fscale` (float) (PitchBlack.java:125, :133). `RenderInfo` ist reiner Clientspeicher.
- **Sounds:**
  - Living `orespawn:pitchblack_living` nur mit 1/5, sonst keiner (PitchBlack.java:195-200); Hurt `pitchblack_hit`, Death `pitchblack_dead` (:202-208).
  - Lautstärke 0.75 (:188).
  - Pitch 1.0 − 0.7·(4/Scale) (:192), das ergibt 0.3 / 0.067 / −0.4 / −1.8 / −4.6 für Scale 4/3/2/1/0.5. Wie der 1.7.10-Client negative Werte behandelt, ist offen: im Repo nicht belegt.
  - Flügelschlag `orespawn:MothraWings` (manifest-ID `mothrawings`), Lautstärke 1.0, Pitch 1.0, serverseitig alle 21 Ticks (PitchBlack.java:223-229).
- **Config:** `Nightmare_health` 250, `Nightmare_attack` 30, `Nightmare_defense` 10, `NightmareEnable` (→ `PitchBlackEnable`) 1, `NightmareSize` 0, `PlayNicely` 0, `DimensionID6` (manifest).
- **Portierung 1.21.1:**
  - Scale als `EntityDataAccessor<Integer>` beibehalten. Hitbox über `getDefaultDimensions(Pose)` mit `scale()` und `refreshDimensions()` in `onSyncedDataUpdated`; alternativ `Attributes.SCALE`, dann aber den Renderer-`glScalef` nicht doppelt anwenden.
  - `Mob.serverAiStep` ist final, das Überspringen der Goals bei Activity 1 geht deshalb nicht über `super`. Alle Goals mit einer `canUse`-Bedingung `activity == 0` versehen und die Flugsteuerung in `customServerAiStep` legen.
  - `motionY *= 0.6` nach `super.tick()` auf `deltaMovement.y` anwenden; Gravitation bleibt aktiv wie im Original.
  - Drachenschaden über `EnderDragon.hurt(EnderDragonPart, DamageSource, float)` mit `damageSources().explosion(null, null)`.
  - Die 20-Tick-Sperre als eigenes Feld in `hurt()` umsetzen, unabhängig von `invulnerableTime`.
  - Pitch explizit auf ≥ 0.5 klemmen, damit das Ergebnis nicht vom Sound-Engine-Verhalten abhängt.
  - XP-Reihenfolge: `defineSynchedData` läuft auch in 1.21.1 im Entity-Konstruktor; ein Feldinitialisierer überschreibt dort genauso. 1:1 = 200 XP für frische Spawns.
  - Die Kappung beim Spawner-Spawn nicht in `checkSpawnRules` (Seiteneffekt!), sondern in `finalizeSpawn` bei `MobSpawnType.SPAWNER`. Ob Leben mitskaliert werden soll, muss der Port entscheiden: Original = nein.

### Pointysaurus - Pointysaurus (`pointysaurus`)

- **Rolle:** Dinosaurier auf Basis von `EntityMob`, greift nur Spieler an und sonst nur, wer ihn verletzt hat.
- **Werte:** XP 40 (Pointysaurus.java:29), fireResistance 100 (:30), meidet Wasser (:28). Leben, Schaden und Rüstung kommen aus der Config (Pointysaurus.java:42-44, :61-67).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `EntityAIMoveThroughVillage(1.0,false)`, 2 `MyEntityAIWanderALot(16,1.0)`, 3 `EntityAIWatchClosest(EntityPlayer,8)`, 4 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (Pointysaurus.java:32-37).
  - Mit 1/6 je Tick (Pointysaurus.java:164-197):
    - Ziel ist zuerst `rt`, der letzte lebende Angreifer. Er wird verworfen, wenn er tot ist, mit 1/250 oder wenn er nicht sichtbar ist.
    - Sonst Suche in `expand(12,5,12)` (:237). Ziel sind nur Nicht-Kreativspieler; `EntityMob`, `VelocityRaptor` und alle Nicht-Spieler sind ausgeschlossen (:200-231).
    - Reichweite² (4 + Zielbreite/2)²: Chance `rand(5)==0 || rand(6)==1` → `attackEntityAsMob`, sonst `tryMoveToEntityLiving(1.25)`.
  - Angriff: Vanilla-Nahkampf mit dem Attribut (10), danach Rückstoß horizontal 0.8, vertikal 0.1, bei Spieler oder Tod ×2 (Pointysaurus.java:131-145).
  - Immun gegen `cactus` (Pointysaurus.java:149).
- **Interaktion:** `interact` gibt false zurück (Pointysaurus.java:127-129).
- **Drops:** fest 10× `leather`, 6× `beef`, 6× `rotten_flesh`, 6× `string`, auf Höhe +2 um ±3 verstreut (Pointysaurus.java:104-122). `getDropItem` (beef) bleibt ungenutzt. XP 40.
- **Spawnen:**
  - Spawner-Zweig „Pointysaurus“, Bereich x/z −3..+2, y 0..4 (Pointysaurus.java:261-275).
  - Sonst: gültiges Licht, y ≥ 50, Nacht und eine 2×2-Säule (x/z −1..0) mit Höhe 1..5 aus Luft (Pointysaurus.java:276-295).
  - Listen: Extreme (`ChunkProviderOreSpawn2.MyMobList`, 10, 4-8) (ChunkProviderOreSpawn2.java:366-367); Chaos (2, 1-4) (BiomeGenUtopianPlains.java:422).
  - Despawn ohne Persistenz (Pointysaurus.java:52-54).
  - Recherche: Rüstung laut [NW] 6, Code-Default 16.
- **Zustand:** DW 20 = attacking (Pointysaurus.java:49, :252-258). Keine NBT-Felder; `rt` wird nicht gespeichert.
- **Sounds:** Living `orespawn:alo_living` mit 1/4; Hurt `alo_hurt`, Death `alo_death`; Lautstärke 0.9, Pitch 1.5 (Pointysaurus.java:77-98).
- **Config:** `Pointysaurus_health` 80, `_attack` 10, `_defense` 16, `PointysaurusEnable` 1, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - `Monster`, Goals 1:1. Die Zielsuche als eigenes Goal oder in `customServerAiStep`.
  - `rt` über `getLastHurtByMob()` abbilden, aber mit dem eigenen 1/250-Vergessen.
  - Kaktus-Immunität über `source.is(DamageTypes.CACTUS)`.
  - Spawn in Extreme erfolgt über den `ChunkGenerator`: `getMobsAt` bzw. Biome-Modifier der Dimension.

### Rat - Rat (`rat`)

- **Rolle:** Kleiner Nager auf Basis von `EntityMob`, greift fast alles an. Ratten von der Rat Sword gehören einem Spieler.
- **Werte:** XP 5 (Rat.java:30), fireResistance 10 (:31), Hitbox 0.25×0.5 (:28). Regeneration 1 Leben mit 1/250 je Tick (Rat.java:156-158). Sprung +0.25 `motionY` **und** +0.25 `posY` (Rat.java:87-91).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `EntityAIPanic(1.35)`, 2 `EntityAIMoveThroughVillage(1.0,false)`, 3 `MyEntityAIWanderALot(10,1.0)`, 4 `EntityAIWatchClosest(EntityPlayer,8)`, 5 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (Rat.java:32-38).
  - Mit 1/200 wird das Revenge-Ziel gelöscht (Rat.java:129-131).
  - Mit 1/5: Suche in `expand(9,2,9)` (:232). Bei Treffer attacking=1 und `tryMoveToEntityLiving(1.25)`. Bei Abstand² < 4 und `rand(8)==0 || rand(7)==1` folgt `attackEntityAsMob` mit dem Attribut 3 (Rat.java:132-139).
  - **Ohne Ziel mit Besitzer:** Ist der Spieler weiter als 8 entfernt (Abstand² > 64), folgt die Ratte mit Tempo 1.75; ab Abstand² > 256 teleportiert sie zu ihm (Rat.java:143-152). **Befund:** Gesucht wird mit `getPlayerEntityByName(myowner)`, gespeichert ist aber die UUID (Rat.java:251). Die Suche liefert deshalb nie einen Spieler; Folgen und Teleport sind im Original tot.
  - **Ziele:** Ausgeschlossen sind ignorierbare, unsichtbare, `Irukandji`, `Skate`, `Whale`, `Flounder`, `Rat`, `Ghost`, `GhostSkelly`, `DungeonBeast` und Kreativspieler.
    - Nur **wenn die Ratte einen Besitzer hat**: nicht der Besitzer; bei `RatPlayerFriendly != 0` gar kein Spieler (Rat.java:202-215).
    - Ebenso nur mit Besitzer: `EntityTameable` bei `RatPetFriendly != 0` nicht, wenn gezähmt, und nie, wenn es dem Besitzer gehört (Rat.java:216-224).
    - Alles andere, auch friedliche Tiere, ist Ziel (Rat.java:225).
  - Immun gegen `inWall` (Rat.java:276-278).
- **Interaktion:** keine (Rat.java:120-122). Besitzer setzt `RatSword.hitEntity`: Treffer spawnen 1 + rand(6) Ratten mit `setOwner` (RatSword.java:31-36).
- **Drops:** `rotten_flesh` über `getDropItem` (Rat.java:113-115), Menge nach vanilla `dropFewItems`: rand(3) + rand(Looting+1). XP 5.
- **Spawnen:**
  - Spawner-Zweig „Rat“, Bereich x/z −2..+1, y 0..4 (Rat.java:285-299).
  - Sonst gültiges Licht. In Crystal (`DimensionID5`) zusätzlich y ≤ 50 und mindestens 4 der 9 Blöcke auf y+1 Luft (Rat.java:303-318).
  - Höchstens 8 Ratten in `expand(20,10,20)` (Rat.java:319-325).
  - Listen: Overworld roofedForest/taiga (manifest); Crystal (40, 4-6) (BiomeGenUtopianPlains.java:155); Chaos (10, 1-10) (:401).
  - Weitere Quellen:
    - Abbauen von `CrystalRat` spawnt 1 + rand(10) (OreBasicStone.java:24-27).
    - Spawner in `makeDungeon` (GenericDungeon.java:205), `makeHauntedHouse` (:1041), `makeIgloo` (:2768), `makeCrystalHauntedHouse` (:3103, Crystal), `makeBouncyCastle` (:3174-3204, Overworld) und `makeCrystalBattleTower` (:4911-4916, Crystal).
  - Despawn nur ohne Persistenz und ohne Besitzer (Rat.java:54-56).
- **Zustand:** DW 20 = attacking. NBT `MyOwner` (String; ohne Besitzer wird `"null"` geschrieben) (Rat.java:258-272).
- **Sounds:** Living `orespawn:ratlive` bei jedem Aufruf, Hurt `rathit`, Death `ratdead`; Lautstärke 0.45, Pitch 1.0 (Rat.java:93-111).
- **Config:** `Rat_health` 5, `Rat_attack` 3, `Rat_defense` 1, `RatEnable` 1, `RatPlayerFriendly` 1, `RatPetFriendly` 1, `PlayNicely`, `DimensionID5` (manifest). Recherche nennt Default 0 und „freundlich zu allen Spielern“; der Code wirkt nur bei Ratten mit Besitzer, das Manifest-Default ist 1.
- **Portierung 1.21.1:**
  - `Monster` in `MobCategory.AMBIENT`, wie im Original über `addSpawn … ambient`. `PanicGoal` braucht `PathfinderMob`, `Monster` erfüllt das.
  - Sprung: `jumpFromGround()` überschreiben, +0.25 auf `deltaMovement.y` und `setPos(y+0.25)`.
  - Besitzer als `UUID` speichern. Der Port muss entscheiden, ob die tote Folge-Logik 1:1 tot bleibt oder per `level.getPlayerByUUID` repariert wird.
  - Befreundete Tiere prüfen über `TamableAnimal.getOwnerUUID()`.
  - `inWall` über `DamageTypes.IN_WALL`.

### RedCow - Apple Cow (`apple_cow`)

- **Rolle:** Friedliche Kuh auf Basis von `EntityCow`, mit allen vanilla Kuh-Fähigkeiten: Panik, Paarung mit Weizen, Melken.
- **Werte:** keine eigenen Attribute; Leben, Tempo und Größe wie `EntityCow` (RedCow.java:8-12). Die Recherche nennt 10 Leben, das entspricht dem vanilla Wert.
- **KI und Angriffe:** vanilla Kuh-Aufgaben. Zusätzlich wird mit 1/200 je AI-Tick das Revenge-Ziel gelöscht (RedCow.java:29-34).
- **Interaktion:** vanilla (Milch mit Eimer, Weizen zur Paarung). Das Kalb ist wieder eine `RedCow` (RedCow.java:21-27).
- **Drops:** `apple` ×(rand(3) + rand(1+Looting)), danach vanilla Kuh-Drops über `super.dropFewItems` (RedCow.java:14-19).
- **Spawnen:**
  - Overworld plains/forest/megaTaiga/taiga/savanna/savannaPlateau (manifest, OreSpawnMain.java:4270-4275).
  - Utopia-Konstruktorliste (10, 4-8) (BiomeGenUtopianPlains.java:23), damit auch in Village. Dort zusätzlich (8, 4-8) (:227). Chaos (3, 2-4) (:321).
  - Despawnt nie (RedCow.java:36-38).
- **Zustand:** keiner über vanilla hinaus.
- **Sounds:** vanilla Kuh.
- **Config:** `CowEnable` 1 (manifest).
- **Portierung 1.21.1:**
  - `extends Cow`; `getBreedOffspring` gibt `RedCow` zurück; `removeWhenFarAway` → false.
  - Die Apfelmenge rand(1+Looting) lässt sich per Loot-Table-Funktion nicht exakt nachbilden. Für 1:1 `dropCustomDeathLoot` in Java überschreiben und dort den Looting-Level aus der Waffe lesen.
  - Renderer: `CowModel` mit Textur `textures/entity/red_cow.png` (manifest, RenderEnchantedCow.java:52, :57).
  - Revenge-Löschen in `customServerAiStep` mit `setLastHurtByMob(null)`.

### Robot1 - Bomb-Omb (`bomb_omb`)

- **Rolle:** Kleiner Selbstmord-Roboter auf Basis von `EntityMob`. Er explodiert, ohne dass ein Nahkampf-Pfad existiert.
- **Werte:** Leben fest 5 (Robot1.java:72), Rüstung fest 2 (:91), Angriff fest 4 (:43, wird nie benutzt), XP 5 (:26), fireResistance 5 (:27), feuerimmun (:28).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `MyEntityAIWanderALot(10,1.0)`, 2 `EntityAIMoveThroughVillage(0.9,false)`, 3 `EntityAIWatchClosest(EntityPlayer,8)`, 4 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (Robot1.java:31-36).
  - In `onLivingUpdate`, **auf beiden Seiten**, mit 1/8 je Tick: Zielsuche in `expand(8,3,8)` (:196). Ziel ist jedes sichtbare, nicht ignorierbare Lebende, das kein `EntityMob` und kein Kreativspieler ist (Robot1.java:163-190).
    - Nur serverseitig, bei Abstand² < 5 und `rand(18)==1`: `createExplosion` mit Stärke 2.5 (mobGriefing), danach `setDead()` (Robot1.java:103-106). Das ist kein Tod, also gibt es keine Drops und keine XP.
    - Jedes Mal zwei Partikel `smoke` und `lava` bei y+1 (wirken nur clientseitig) und `tryMoveToEntityLiving(1.2)` (Robot1.java:107-111).
  - Kein `EntityAIAttackOnCollide`, deshalb nie Nahkampf. Immun gegen `cactus` (Robot1.java:155-161).
- **Interaktion:** keine (Robot1.java:140-142).
- **Drops:** `gunpowder` über `getDropItem` (Robot1.java:136-138), vanilla Menge, nur wenn er wirklich getötet wird. XP 5.
- **Spawnen:**
  - Kein Spawner-Zweig. Bedingung: y ≥ 50, gültiges Licht, Nacht (Robot1.java:215-217).
  - Listen: Village (25, 4-8) (BiomeGenUtopianPlains.java:194); Chaos (5, 2-8) (:341).
  - Despawn ohne Persistenz (Robot1.java:62-64).
- **Zustand:** DW 20 = attacking (nie gesetzt). Kein NBT.
- **Sounds:** Living `orespawn:kyuubi_living`, Hurt `scorpion_hit`, Death `robot1_death`; Lautstärke 1.0, Pitch 1.0 (Robot1.java:116-134).
- **Config:** `Robot1Enable` 1, `PlayNicely` (manifest). Keine Stat-Keys, alle Werte sind hart codiert.
- **Portierung 1.21.1:**
  - `Monster`. Explosion über `level().explode(this, x, y, z, 2.5f, Level.ExplosionInteraction.MOB)`, danach `discard()`, weil ohne Loot.
  - Die Zielsuche nur serverseitig; die Partikel über ein synchronisiertes Flag oder `broadcastEntityEvent` statt einer Client-Zielsuche.
  - Laut Recherche funktionieren Critter Cages nicht bei Robotern; im Code gibt es tatsächlich keine Roboter-Käfige.

### Robot2 - Robo-Pounder (`robo_pounder`)

- **Rolle:** Großer Nahkampf-Roboter auf Basis von `EntityMob`, der Terrain zerstört.
- **Werte:** XP 100 (Robot2.java:31), fireResistance 200 (:32), feuerimmun (:33), Hitbox 3.0×6.2 (:29). Leben, Schaden und Rüstung aus der Config (:46-48, :76-97). Sprung +0.25 (:107-110).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `MyEntityAIWanderALot(14,1.0)`, 2 `EntityAIMoveThroughVillage(0.9,false)`, 3 `EntityAIWatchClosest(EntityPlayer,10)`, 4 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (Robot2.java:36-41).
  - Mit 1/6 und `PlayNicely == 0` (Robot2.java:274-317):
    - Mit 1/50 wird das Angriffsziel gelöscht. Ziel ist `getAttackTarget()`, sonst eine Suche in `expand(14,3,14)` (:385). Zielregel wie bei Robot1 (:352-379).
    - Hinsehen; Winkel zwischen Zielrichtung und `rotationYaw+90°` < 1.25 rad und Abstand² < (5 + Breite/2)²:
      - attacking=1.
      - Mit `rand(5)==0 || rand(6)==1` Nahkampf (Attribut 22) plus 6× `destroyBlock` unter dem Ziel.
      - Bei jedem Durchlauf in Reichweite `destroyNearbyBlocks`.
    - Außerhalb des Winkels attacking=0. Immer `tryMoveToEntityLiving(1.0)`.
  - `destroyBlock`: Block auf Ziel-y−1 mit x/z-Versatz ±1 (Robot2.java:212-241).
  - `destroyNearbyBlocks`: 50 Zufallsblöcke in x/z ±6.5, y +0.1..+8.6 (Robot2.java:243-267). Beide setzen Luft ohne Drops, außer bei `obsidian`, `bedrock`, `quartz_block`, `mob_spawner`, `redstone_block`, `iron_block` und `chest`; nur bei mobGriefing.
  - **„just_for_fun“:** Bei attacking==0 startet mit 1/450 ein Wüten mit Zähler 50 (Robot2.java:318-334). Solange er läuft, attacking=1 und mit 1/3 `destroyNearbyBlocks`. Der Zähler sinkt nur in Ticks, in denen attacking vorher 0 war, also wenn der 1/6-Zweig ohne Ziel zurücksetzt. Deshalb dauert das Wüten effektiv deutlich länger als 50 Ticks.
  - **Bei Schaden** (außer `cactus`): Nur wenn der Angreifer ein `EntityLiving` ist, werden `setAttackTarget`, `setTarget` und `tryMove(1.2)` gesetzt (Robot2.java:337-350). **Spieler sind kein `EntityLiving`** (javap), für sie greift nur `EntityAIHurtByTarget`.
  - Immun gegen `LaserBall` (LaserBall.java:109-111).
- **Interaktion:** keine (Robot2.java:204-206).
- **Drops** (`getDropItem` iron_block ungenutzt):
  - `iron_block` ×(2 + rand(8)) und `iron_ingot` ×(5 + rand(6)) (Robot2.java:151-156).
  - (5 + rand(10)) Würfe mit rand(15). Treffer 0..9 geben: `redstone`, `repeater`, `comparator`, `redstone_block` (Fall 3 und 8), `dispenser`, `sticky_piston`, `piston`, `lever`, `light_weighted_pressure_plate`. 10..14 geben nichts (Robot2.java:157-201).
  - XP 100.
- **Spawnen:**
  - Spawner-Zweig „Robo-Pounder“ (Robot2.java:405-419).
  - Sonst y ≥ 50, Nacht, Säule x −1..+1 / z −1..0 mit Höhe 1..5 aus Luft oder `tallgrass`, gültiges Licht (Robot2.java:420-436).
  - Listen: Village (16, 2-8) (BiomeGenUtopianPlains.java:197); Chaos (2, 1-4) (:344).
  - Struktur: Robot Lab in Islands → `makerobomain` → `makeroboaltar`-Spawner (OreSpawnWorld.java:136; GenericDungeon.java:4125, :4200, :4289-4294).
- **Zustand:** DW 20 = attacking. Kein NBT; `just_for_fun` wird nicht gespeichert.
- **Sounds:** Living `orespawn:robot_living` mit 1/4, Hurt `robot_hurt`, Death `robot_death`; Lautstärke 1.0, Pitch 1.0 (Robot2.java:112-133).
- **Config:** `Robot2_health` 200, `_attack` 22, `_defense` 18, `Robot2Enable`, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - Die Block-Ausnahmen als Block-Tag `orespawn:robot_immune`. Luft setzen über `level.setBlock(pos, AIR, 3)` ohne Drops; Grief-Prüfung über `EventHooks.canEntityGrief`.
  - Die Winkelformel ist direkt übertragbar (`getYRot()+90`).
  - Rückschlag nur gegen `Mob` (1:1 zu `EntityLiving`).
  - Hitbox 3.0×6.2 ist unkritisch. Rüstung 18 liegt unter 30.

### Robot3 - Robo-Gunner (`robo_gunner`)

- **Rolle:** Fernkampf-Roboter auf Basis von `EntityMob`, schießt `LaserBall`.
- **Werte:**
  - Grunddaten: XP 60 (Robot3.java:30), fireResistance 40 (:31), feuerimmun (:32), Hitbox 2.5×5.0 (:28), Tempo 0.35 (:27).
  - Nachladezeit 35 Ticks (Robot3.java:232), attacking fällt ab Rest < 25 zurück (:215-217).
  - Reichweite: Abstand² < 256, also 16 Blöcke (:235).
  - Geschoss: Schaden 16 plus Feuer 1 (LaserBall). Startpunkt y+3.0, seitlich 1.75 entlang `rotationYawHead` (:245-248). Geschwindigkeit 1.4, Streuung 5.0, Bogen +horizontaler Abstand·0.2 (:252-253).
  - Das Nahkampf-Attribut 16 (Config) wird von keinem Code-Pfad benutzt.
- **KI und Angriffe:**
  - Aufgaben wie Robot2, `WatchClosest` aber mit 8 (Robot3.java:35-40).
  - Bei `reload_ticker == 0` (Robot3.java:219-264):
    - Mit 1/50 Ziel löschen; Ziel ist `getAttackTarget()`, sonst eine Suche in `expand(16,3,16)` (:308).
    - Nachladen wird immer auf 35 gesetzt, auch ohne Ziel.
    - Bei Abstand² < 256 und Winkel zu `rotationYawHead+90°` < 0.5 rad: Schuss, Sound, attacking=1.
    - `tryMoveToEntityLiving(0.5)`.
  - Immun gegen `cactus` (Robot3.java:267-273) und `LaserBall`.
- **Interaktion:** keine.
- **Drops** (`getDropItem` iron_ingot ungenutzt): `laserball` ×4 je Stapel, (5 + rand(6)) Stapel (Robot3.java:150-152); (5 + rand(10)) Redstone-Würfe wie Robot2 (:153-197). XP 60.
- **Spawnen:**
  - **Kein** Spawner-Zweig. Bedingung: y ≥ 50, Nacht, Säule wie Robot2, gültiges Licht (Robot3.java:327-345).
  - Listen: Village (12, 2-4) (BiomeGenUtopianPlains.java:200); Chaos (2, 1-4) (:347).
- **Zustand:** DW 20 = attacking. Kein NBT.
- **Sounds:** `robot_living` mit 1/4, `robot_hurt`, `robot_death`, Lautstärke 1.0 (Robot3.java:111-132). Beim Schuss vanilla `fireworks.launch`, Lautstärke 3.0, Pitch 1.0 (:254).
- **Config:** `Robot3_health` 80, `_attack` 16, `_defense` 14, `Robot3Enable`, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - `setThrowableHeading` wird zu `Projectile.shoot(x, y, z, 1.4f, 5.0f)`. Achtung: Die Streuung ist in 1.21.1 dreiecksverteilt (`0.0172275·inaccuracy`), in 1.7.10 gaußverteilt (`0.0075·inaccuracy`). Für 1:1 im `LaserBall`-Port eine eigene `shoot`-Variante.
  - Sound `SoundEvents.FIREWORK_ROCKET_LAUNCH`.

### Robot4 - Robo-Warrior (`robo_warrior`)

- **Rolle:** Nahkampf- und Fernkampf-Roboter auf Basis von `EntityMob`, mit Treffersperre und einem (im Original wirkungslosen) Schild.
- **Werte:**
  - Grunddaten: XP 120 (Robot4.java:33), fireResistance 120 (:34), feuerimmun (:35), Hitbox 2.5×4.0 (:31), Tempo 0.34 (:30).
  - Treffersperre 65 Ticks nach jedem angenommenen Treffer (Robot4.java:325-328). Recherche [NW] nennt etwa 1.7 s; der Code hat 65 Ticks, also 3.25 s.
  - Nahkampf: Attribut 12. Rückstoß vor dem Schlag horizontal 2.0, vertikal 0.12, bei Spieler oder Tod ×2 (Robot4.java:236-247).
  - Schuss:
    - Startpunkt y+2.0, 1.75 entlang `rotationYaw+45°` (rechte Schulter); Geschwindigkeit 2.0, Streuung 4.0 (Robot4.java:289-297).
    - Ab Abstand² > 65: `setSpecial` (Explosion 3.0), Nachladen 30, Sound 3.5/0.5.
    - Sonst Nachladen 10, Sound 2.5/1.0 (:298-306).
  - `getAttackStrength` (15/20/25 je Schwierigkeit) ist toter Code: 1.7.10 ruft die Methode nicht auf, und die Verschachtelung liefert nur bei EASY 15 (Robot4.java:127-139).
- **KI und Angriffe:**
  - Aufgaben wie Robot3 (Robot4.java:38-43).
  - Bei `reload_ticker == 0` mit 1/8 (Robot4.java:260-314): Ziel wie Robot3, Suche in `expand(16,4,16)` (:374). Bei Abstand² < 256:
    - Unter (3 + Breite/2)² Nahkampf, ohne Nachladezeit.
    - Sonst bei Winkel < 0.5 rad Schuss, dann attacking=1.
    - `tryMove(0.75)`.
  - attacking fällt zurück, wenn Nachladen und Treffersperre beide ≤ 0 sind (:315-317).
  - Bei Schaden: `cactus` → false. Schild ≠ 0 oder Sperre ≠ 0 → false. Sonst Sperre 65, attacking=1 und `super`. Bei einem `EntityLiving`-Angreifer (nicht Spieler) Ziel setzen und `tryMove(1.2)` (Robot4.java:320-339).
  - **Schild-Befund:** DW 21 wird nur in `ModelRobot4.setRotationAngles` gesetzt, abhängig vom Armwinkel bei attacking (ModelRobot4.java:385-398). Das ist Client-Rendercode. Der Server liest in `attackEntityFrom` immer 0, der Schild blockiert also nie.
  - Immun gegen `LaserBall`.
- **Interaktion:** keine.
- **Drops** (`getDropItem` quartz ungenutzt): `laserball` ×4 je Stapel, (5 + rand(10)) Stapel; 1× `raygun`; 1× `item_frame`; (10 + rand(15)) Redstone-Würfe wie Robot2 (Robot4.java:178-230). XP 120.
- **Spawnen:**
  - Spawner-Zweig „Robo-Warrior“ (Robot4.java:402-416).
  - Sonst wie Robot2 (:417-433).
  - Listen: Village (8, 1-2) (BiomeGenUtopianPlains.java:203); Chaos (1, 1-2) (:350).
  - Struktur: `makerobotreasureroom` im Robot Lab, Islands (GenericDungeon.java:4203, :4377).
- **Zustand:** DW 20 = attacking, DW 21 = shielding (Robot4.java:55-56). Kein NBT.
- **Sounds:** `robot_living` mit 1/4, `robot_hurt`, `robot_death`, Lautstärke 1.0; `fireworks.launch` wie oben. Clientseitige Partikel: `smoke` hinter dem Kopf mit 1/3, `reddust` an der Schulter bei attacking (Robot4.java:115-125).
- **Config:** `Robot4_health` 170, `_attack` 12, `_defense` 18, `Robot4Enable`, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - Shielding als `EntityDataAccessor` übernehmen. Das Setzen darf nicht im Model passieren: Das Model läuft ausschließlich unter `com.swbr.orespawn.client`, und synchronisierte Daten sind nur vom Server aus änderbar.
  - 1:1 = kein wirksamer Schild. Soll er wirken, den Armwinkel `|cos(tick·wingspeed·6)|·0.785+0.75 > 0.2618` serverseitig aus `tickCount` nachrechnen. Den Wert von `wingspeed` aus `ModelRobot4` holen: offen, hier nicht gelesen.
  - Die Treffersperre in `hurt()` als eigenes Feld.

### Robot5 - Robo-Sniper (`robo_sniper`)

- **Rolle:** Kleiner Scharfschützen-Roboter auf Basis von `EntityMob`, schießt `LaserBall` auf große Distanz.
- **Werte:**
  - Grunddaten: XP 20 (Robot5.java:29), fireResistance 40 (:30), feuerimmun (:31), Hitbox 1.0×2.25 (:27).
  - Nachladezeit 20 Ticks (:204), attacking fällt ab Rest < 15 zurück (:187-189).
  - Reichweite: Abstand² < 900, also 30 Blöcke (:207).
  - Geschoss: Startpunkt y+1.6, seitlich 1.6; Geschwindigkeit 1.4, Streuung 5.0 (:217-225). Schaden 16 laut `LaserBall`. Recherche [NW] nennt „Attack 16“ und meint das Geschoss; das Nahkampf-Attribut 5 ist ungenutzt.
- **KI und Angriffe:**
  - Aufgaben wie Robot3 (Robot5.java:33-38).
  - Ablauf wie Robot3 mit Suche in `expand(30,6,30)` (:282).
  - Nähert sich nur bei Abstand² > 36 (`tryMove(0.5)`) und hält so etwa 6 Blöcke Abstand (:230-232).
  - Immun gegen `cactus` und `LaserBall`.
- **Interaktion:** keine.
- **Drops** (`getDropItem` iron_ingot ungenutzt): `laserball` ×4 je Stapel, (5 + rand(6)) Stapel; (2 + rand(5)) Redstone-Würfe wie Robot2 (Robot5.java:120-170). XP 20.
- **Spawnen:**
  - Spawner-Zweig „Robo-Sniper“ (Robot5.java:302-316).
  - Sonst y ≥ 50, Nacht, Säule mit Höhe 1..2 aus Luft oder `tallgrass`, gültiges Licht (:317-333).
  - Listen: Village (20, 4-8) (BiomeGenUtopianPlains.java:206); Chaos (2, 3-5) (:353).
  - Struktur: `makerobopillar` in `makeRobotLab` und `makerobotower`, Islands (GenericDungeon.java:4126-4131, :4231-4234, :4156-4163).
- **Zustand:** DW 20 = attacking. Kein NBT.
- **Sounds:** `robot_living` mit 1/4, `robot_hurt`, `robot_death`, Lautstärke **0.5** (Robot5.java:83-104); `fireworks.launch` 3.0/1.0 (:226).
- **Config:** `Robot5_health` 20, `_attack` 5, `_defense` 6, `Robot5Enable`, `PlayNicely` (manifest).
- **Portierung 1.21.1:** Wie Robot3. Die Suchbox 30×6×30 in einer eigenen, gedrosselten Zielsuche halten; im Original läuft sie alle 20 Ticks.

### RockBase - Rock (`rock`)

- **Rolle:** Liegender Stein auf Basis von `EntityLiving`, ohne KI. Er wird mit einem Stein-Item platziert, lässt sich schlagen und gibt das Item zurück.
- **Werte:**
  - Hitbox 0.25×0.15 (RockBase.java:19), fireResistance 100000 (:20), feuerimmun (:21), Rüstung 0 (:67-69).
  - Leben 1 + Typ/4 (Ganzzahl), gesetzt über `placeRock` oder den Typwurf (RockBase.java:61-65, :136-137). Das ergibt: Typ 1-3 → 1, 4-7 → 2, 8-11 → 3, 12 → 4. Vor dem ersten Setzen gilt das `EntityLiving`-Default (offen: Wert hier nicht gelesen).
  - Keine XP: `onDeathUpdate` ruft sofort `setDead` (RockBase.java:205-207).
- **KI und Angriffe:**
  - Keine Aufgaben. Rotation wird jeden Tick auf 0 gesetzt (RockBase.java:80-83); kein Fallschaden (:71-72).
  - **Typwurf** serverseitig, wenn `rock_type == 0`:
    - Außerhalb von Crystal zuerst 1, dann nacheinander überschreibend: 1/10 → 2, 1/20 → 3, 1/30 → 4, 1/40 → 5, 1/50 → 6, 1/100 → 7, 1/200 → 8, 1/500 → 9, 1/500 → 10, 1/500 → 11, 1/1000 → 12 (RockBase.java:87-123).
    - In Crystal (`DimensionID5`) zuerst 9, dann 1/3 → 10, 1/5 → 11, 1/10 → 12 (:124-135).
  - Der Server synchronisiert den Typ jeden Tick (:139-141).
  - Clientseitige Partikel mit 1/20 je Tick: Typ 9 `flame`, 10 `happyVillager`, 11 `smoke`, 12 `fireworksSpark` (RockBase.java:142-155).
  - Bei Schaden: `inWall` → false. Ein lebender Angreifer löst den Sound `random.pop` aus (0.75/2.25) (RockBase.java:36-45). Keine Treffer-Animation (:198-203).
  - Kann getroffen und geschoben werden (:190-196). Wird von allen Mobs über `isIgnoreable` ignoriert.
- **Interaktion:** Keine am Entity. Platziert wird er über `ItemRock.onItemUse`: Spawn „Rock“ auf Klickblock +1.01, dann `placeRock(1..12)` je nach Item (ItemRock.java:64-110).
- **Drops** in `onDeath`, ohne `super`, also keine Vanilla-Drops. Je 1 Stück, um ±1/3 verstreut (RockBase.java:209-257):

  | Typ | Item | Typ | Item |
  |---|---|---|---|
  | 1 | `rocksmall` | 7 | `rockspikey` |
  | 2 | `rock` | 8 | `rocktnt` |
  | 3 | `rockred` | 9 | `rockcrystalred` |
  | 4 | `rockgreen` | 10 | `rockcrystalgreen` |
  | 5 | `rockblue` | 11 | `rockcrystalblue` |
  | 6 | `rockpurple` | 12 | `rockcrystaltnt` |

- **Spawnen:** `getCanSpawnHere` verlangt nur y ≥ 50 (RockBase.java:186-188). Es gibt keine Spawnliste und keinen Spawner; die einzige gefundene Quelle ist `ItemRock`. Despawnt nie (:182-184).
- **Zustand:** DW 20 = Typ (RockBase.java:29). NBT-Schlüssel **`ButterflyType`** (int, kopiert) (:259-267). Das Anfangs-x/z in `dx`/`dz` wird gemerkt, aber nie verwendet (:75-78).
- **Sounds:** Living, Hurt und Death sind null (RockBase.java:158-168); nur `random.pop` beim Treffer.
- **Texturen** (RenderRockBase.java:96-107): Typ 1, 2 und 7 → `rocktexture`; 3 red; 4 green; 5 blue; 6 purple; 8 tnt; 9 `rockcrystaltexture`; 10 crystalgreen; 11 crystalblue; 12 crystaltnt. `ModelRockBase` rendert mit GL-Blend (anim_summary.txt:78).
- **Config:** `DimensionID5`. `RockEnable` steht im Manifest, wird in `RockBase` aber nicht gelesen.
- **Portierung 1.21.1:**
  - `Mob` mit `setNoAi(true)` oder eine eigene `LivingEntity`-Unterklasse; Kategorie `MISC`.
  - `tickDeath()` überschreiben und sofort `remove(KILLED)`. Den Drop in `dropCustomDeathLoot` legen, mit leerer Loot-Table und `xpReward 0`.
  - `isPickable`/`isPushable` → true.
  - Den Typ nur serverseitig würfeln (`tick`, `!level().isClientSide`).
  - `RenderType.entityTranslucent` für die Kristalle.
  - NBT-Key `ButterflyType` für 1:1 beibehalten.

### Rotator - Rotator (`rotator`)

- **Rolle:** Fliegender Kristall-Mob auf Basis von `EntityMob`. Er hat **keine** Aufgaben und nur eigene Flug-KI, kreist um sein Ziel und despawnt tagsüber.
- **Werte:** XP 35 (Rotator.java:31), feuerimmun (:32), fireResistance 25 (:33), Hitbox 1.0×2.0 (:30), Tempo fest 0.25 (:41). Leben, Schaden und Rüstung aus der Config (:40-42, :106-108, :262-264).
- **KI und Angriffe:**
  - Konstruktor ohne `tasks.addTask` (Rotator.java:23-36).
  - **`onUpdate`, beide Seiten, jeden Tick** (Rotator.java:114-127):
    - `motionY *= 0.6`.
    - Client: Partikel `fireworksSpark` mit 1/10.
    - Zielsuche in `expand(12,10,12)` (:296). Bei Treffer Strahl-Partikel Richtung Ziel (serverseitig wirkungslos) und `busy_fighting = 1`.
  - Ziele: alles Lebende außer ignorierbaren, unsichtbaren, Kreativspielern, `Termite`, `Vortex`, `Rotator`, `DungeonBeast`, `Peacock`, `CrystalCow`, `Irukandji`, `Skate`, `Whale`, `Flounder`, `Urchin`, `TerribleTerror`, `LurkingTerror`, `CloudShark`, `Mothra`, `Bee` und `Mantis` (Rotator.java:266-290).
  - **Flug** (Rotator.java:148-198):
    - Mit 1/300 oder bei Abstand² < 2.1 neuer Wegpunkt: x/z je ±(8..17), y `rand(6)-3`, Luft und sichtbar, 50 Versuche.
    - Sonst mit `rand(9)==2` Ziel suchen. Der Wegpunkt liegt 2.5 Blöcke neben dem Ziel, um 90° versetzt (Kreisen). Bei Abstand² < 9 `attackEntityAsMob` mit dem vanilla Nahkampf und dem Attribut.
    - Bewegung: horizontal `signum·0.4` mit Glättung 0.2, vertikal `signum·0.7` mit Glättung 0.2, Drehung /4, `moveForward 0.75`.
  - **Despawn am Tag** (Rotator.java:128-141): ohne Persistenz, ohne Kampf und ohne Spawner-Herkunft; bei `worldTime % 24000 < 12000` mit 1/400 je Tick `setDead`.
  - **Bei Schaden:** Der Wegpunkt springt auf die Angreifer-Position (Rotator.java:221-223).
  - **Pfeil-Befund:** `if (e instanceof EntityArrow) return false` prüft `getEntity()` (Rotator.java:216-219). Das ist laut javap der Schütze, nie der Pfeil; die Immunität greift also nie.
  - Kein Fallschaden (:204-208), löst keine Druckplatten aus (:210-212), keine Kollision mit Entities (:103-104).
- **Interaktion:** keine.
- **Drops:** `getDropItem` würfelt einmal pro Tod rand(4): `crystalpink_ingot`, `tigerseye_ingot`, Block `crystalcoal` oder `iron_ingot` (Rotator.java:311-326). Menge nach vanilla `dropFewItems`. XP 35.
- **Spawnen:**
  - Spawner-Zweig „Rotator“ im Bereich x/z ±2, y 1..3 → `was_spawnered = 1` (Rotator.java:228-243). Das Flag wird nicht gespeichert, nach einem Neuladen kann der Rotator tagsüber despawnen.
  - Sonst gültiges Licht, 3×3-Säule mit Höhe 1..2 aus Luft, `worldTime % 24000 ≥ 12000` (Rotator.java:244-259).
  - Listen: Crystal (4, 1-2) (BiomeGenUtopianPlains.java:143); Chaos (1, 1-3) (:404).
  - Strukturen:
    - Crystal: `makeRotatorStation` (GenericDungeon.java:841-846), `makeRoundRotator` (:6240-6255), `makeCrystalBattleTower` (:4962-4967).
    - Islands: `makeEnormousCastle(Q)` über `addD4Castle`, Levels mit „Rotator“ (GenericDungeon.java:330, :6521).
    - `makeDungeon` (:202).
  - `canDespawn` nur ohne Persistenz, Kampf und Spawner-Herkunft (Rotator.java:75-77).
- **Zustand:** keine DataWatcher-Einträge. Kein NBT; `busy_fighting` und `was_spawnered` sind flüchtig.
- **Sounds:**
  - Living `"vortexlive"` **ohne Namespace** (Rotator.java:88). Das löst auf `minecraft:vortexlive` auf, einen Sound, den es nicht gibt; `Vortex` benutzt `orespawn:vortexlive` (Vortex.java:57). sounds_dump führt Rotator deshalb nicht als Nutzer.
  - Hurt `orespawn:glasshit`, Death `glassdead`; Lautstärke 0.75, Pitch 1.0 (:79-97).
- **Config:** `Rotator_health` 35, `_attack` 10, `_defense` 8, `RotatorEnable`, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - `Monster` mit leerem `registerGoals`; Flug in `customServerAiStep`, `motionY`-Dämpfung in `tick`.
  - Die jede-Tick-Zielsuche nur serverseitig ausführen und `busy_fighting` bei Bedarf synchronisieren. Im Original läuft sie pro Rotator auf beiden Seiten (Performance).
  - Zeit: `level().getDayTime() % 24000`.
  - Offene Port-Entscheidungen: Pfeil-Immunität 1:1 wirkungslos oder gemeint (`source.getDirectEntity() instanceof AbstractArrow`)? Living-Sound 1:1 stumm oder `orespawn:vortexlive`?

### RubberDucky - Rubber Ducky (`rubber_ducky`)

- **Rolle:** Zähmbare Ente auf Basis von `EntityTameable`, schwimmt und sucht Wasser. Sie greift Tintenfische an. Wird sie von Spielern getötet, kommt sie „wütender“ wieder; ab 5 Tötungen ist sie böse und greift Spieler an.
- **Werte:**
  - Grunddaten: Leben fest 5 (RubberDucky.java:153), Rüstung 1 (:157), XP 15 (:46), fireResistance 3 (:47), Tempo 0.22 (:43).
  - Das Attribut attackDamage 6 wird registriert (:66-67), aber nicht benutzt. Tatsächlicher Schaden: 1.0, ab killcount ≥ 5 → 2.0 (:426-433). Recherche nennt Attack 6.
  - Regeneration 1 mit 1/300 unter vollem Leben (:398-400).
  - killcount sinkt mit 1/200 je AI-Tick um 1 (:395-397).
  - Kein Fallschaden (:160-164). Im Wasser Auftrieb +0.1, `motionY` mindestens −0.05 (:109-114).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`; 1 `EntityAIMate(1.0)`; 1 `MyEntityAIFollowOwner(speed 2.0, maxDist 10, minDist 2)` (Argumentreihenfolge MyEntityAIFollowOwner.java:22-28); 2 `EntityAIMate(1.0)` (doppelt); 3 `EntityAITempt(1.25, Items.fish)`; 4 `MyEntityAIWanderALot(16,1.0)`; 5 `EntityAIWatchClosest(EntityLiving,6)`; 5 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (RubberDucky.java:51-59).
  - Kein `EntityAISit`; Sitzen wirkt nur über die Follow-Aufgabe (MyEntityAIFollowOwner.java:38, :42, :72).
  - **Wassersuche:** Nicht im Wasser, mit 1/50, Schalen mit Radius 1,2,3,4,5,7,9,11,13 (y ≤ 5) nach `water`/`flowing_water`. Der nächste Treffer wird mit `tryMoveToXYZ(tx, ty-1, tz, 1.33)` angesteuert (RubberDucky.java:373-394, :285-366).
  - **Kampf** (nicht Peaceful, mit 1/5):
    - Ein vorhandenes, lebendes `getAttackTarget()` hat Vorrang. Sonst Suche in `expand(8,4,8)` (RubberDucky.java:467-490).
    - Ziele: `AttackSquid` und `EntitySquid` immer; `RubberDucky` mit 1/10 als `buddy` (kein Ziel); Nicht-Kreativspieler nur bei killcount ≥ 5 (:435-465).
    - Abstand² < 12 → attacking=1, Chance `rand(4)==0 || rand(5)==1` Angriff; sonst `tryMove(1.2)`. Ohne Ziel mit 1/15 zum buddy (:401-420).
    - Außerdem jeden AI-Tick mit 1/20 zum buddy (:421-423).
  - **Wiederkehr:** Nur serverseitig, wenn der Angreifer ein Spieler ist, der Treffer tödlich war und `died == 0` (RubberDucky.java:117-150).
    - `died = 1`, killcount+1.
    - Bei killcount < 10 bis zu 20 Versuche: Versatz x/z je rand(3) mit Zufallsvorzeichen, y von +3 bis −2 abwärts. Wo Luft über Nicht-Luft liegt, spawnt eine neue „Rubber Ducky“ bei (x+i+1, y+j+1, z+k) und erbt den killcount.
  - Jeder Treffer beendet das Sitzen (:122).
- **Interaktion** (RubberDucky.java:214-283):
  - Zuerst `super.interact`: vanilla Paarung, Brut-Item `crystalapple` (:558-560).
  - **Fisch** (`Items.fish`, alle Metadaten) bei Abstand² < 16:
    - Ungezähmt: Server 50 % zähmen (Owner-UUID, Herz-Event 7, volle Heilung), sonst Rauch-Event 6.
    - Gezähmt und Besitzer: volle Heilung.
    - Außerhalb von Creative 1 Fisch verbrauchen.
  - **Toter Busch** bei gezähmter Ente und Besitzer: entzähmen (Owner „“), Event 6, Item verbrauchen.
  - **Leere Hand oder anderes Item** beim Besitzer: Sitzen umschalten; hinsetzen nur bei killcount < 5.
  - Gibt immer true zurück.
  - `isWheat(fish)` ist toter Code (:554-556).
  - Kinder sind wieder `RubberDucky` (:546-552).
- **Drops:** `getDropItem` gibt mit 1/2 `feather`, sonst mit 1/2 **`eggrubberducky`** (Spawn-Ei), sonst nichts (RubberDucky.java:204-212). Menge nach vanilla `dropFewItems`. XP 15. Recherche nennt „feather, chicken“; der Code gibt Feder oder Spawn-Ei.
- **Spawnen:**
  - Spawner-Zweig „Rubber Ducky“ (RubberDucky.java:520-534).
  - Sonst y ≥ 50 und Tag, ohne Lichtprüfung (:535).
  - Listen: Overworld river (10, 10-20) und stoneBeach (4, 4-6) als `waterCreature` (manifest).
  - Struktur: `makeRubberDuckyPond` in der Overworld (OreSpawnWorld.java:306; GenericDungeon.java:5426).
  - **Despawn:** Kinder werden persistent (`enablePersistence`) und despawnen nie. Sonst nur ohne Persistenz, ungezähmt und mit `should_despawn` (in dieser Klasse immer true) (RubberDucky.java:538-544).
- **Zustand:** DW 22 = killcount, DW 23 = attacking (RubberDucky.java:72-73, :502-517). Dazu die vanilla Tameable-Flags. NBT `Killcount` (int) (:492-500). `entityInit` dreht ein negatives `growingAge` ins Positive (:75-77); beim Konstruieren ist das Alter aber 0, also wirkungslos.
- **Sounds:** Living `orespawn:duck_hurt` mit 1/10, Hurt und Death ebenfalls `duck_hurt`; Lautstärke 0.8, Pitch 1.2 (RubberDucky.java:181-202). `spawnCreature` spielt beim Wiederkehr-Spawn den Living-Sound (:176).
- **Renderer:** Textur `evilrubberduckytexture` ab killcount ≥ 5, sonst `rubberduckytexture` (RenderRubberDucky.java:47-55). Kinder mit halber Größe (:34-40). `ModelRubberDucky` wertet killcount ebenfalls aus (ModelRubberDucky.java:92).
- **Config:** `RubberDuckyEnable` 1, `PlayNicely` (manifest). Keine Stat-Keys.
- **Portierung 1.21.1:**
  - `TamableAnimal`.
  - Die Fisch-Prüfung umfasst `Items.COD`, `SALMON`, `TROPICAL_FISH` und `PUFFERFISH`, weil `Items.fish` jede Metadatenvariante einschloss. Nicht einfach `ItemTags.FISHES` nehmen, der Tag enthält auch gebratenen Fisch.
  - Zähmen über `tame(player)` bzw. `setOwnerUUID`; Events 7/6 über `level().broadcastEntityEvent`.
  - Entzähmen: `setTame(false, true)` und `setOwnerUUID(null)`.
  - Die Wiederkehr aus `die(DamageSource)` mit `source.getEntity() instanceof Player`.
  - Spawn als `MobCategory.WATER_CREATURE` mit `SpawnPlacementTypes.IN_WATER`.
  - `isFood` → `crystalapple`.
  - Die Wassersuche ist teuer (bis zu 13er-Schalen); die Drossel 1/50 beibehalten.

### RubyBird - Ruby Bird (`ruby_bird`)

- **Rolle:** Friedlicher Vogel, `Cockateil` auf Basis von `EntityAnimal`, fest auf Vogeltyp 5 („Ruby“). Er spawnt nur aus dem Ruby-Bird-Dungeon.
- **Werte:** Alles von `Cockateil` geerbt: Leben 2 (Cockateil.java:123-125), Tempo 0.33, Angriff 1 (ungenutzt) (:47-49), XP 2 (:39), fireResistance 2 (:41), Hitbox 0.5×0.5 (:37). Kein Fallschaden (:160-164).
- **KI und Angriffe:**
  - Keine Aufgaben. Flug aus `Cockateil.updateAITasks` (Cockateil.java:170-223):
    - Hängt er fest (mehr als 40 Ticks auf demselben Block), mit 1/250 oder bei Abstand² < 4.1 neuer Wegpunkt.
    - Wegpunkt: x/z je ±(rand(8) + 5 − 2·flyup), y `rand(9+stayup) − 5 + flyup`; `stayup = 2` in Islands (`DimensionID4`). Luft und sichtbar, 35 Versuche.
    - Bewegung: horizontal `signum·0.3` mit Glättung 0.25, vertikal `signum·0.7` mit Glättung 0.2, Drehung /3, `moveForward 0.8`.
  - `motionY` wird unterhalb des Wegpunkts mit 0.7, darüber mit 0.5 multipliziert (Cockateil.java:139-150).
  - **Befund `setFlyUp`:** `RubyBird.entityInit` setzt `flyup = 2` (RubyBird.java:15). Der `Cockateil`-Konstruktorrumpf läuft danach und setzt `flyup = 0` (Cockateil.java:36). Der Aufruf ist also wirkungslos.
  - `birdtype` hat keinen Feldinitialisierer und bleibt 5.
- **Interaktion:** keine; `createChild` liefert null, also keine Zucht (Cockateil.java:248-250).
- **Drops:** Wurde der Vogel von einem Spieler verletzt (Flag `killedByPlayer`, nicht gespeichert, Cockateil.java:131-137), liefert `getDropItem` mit 1/3 `ruby`, sonst `feather` (:237-243). Menge nach vanilla `dropFewItems`. XP 2.
- **Spawnen:**
  - `getCanSpawnHere` ist immer true (RubyBird.java:27-29), ohne Licht- und Tagesprüfung.
  - Einzige Quelle ist der Spawner in `RubyBirdDungeon.makeDungeon` (RubyBirdDungeon.java:69-73):
    - Utopia: `addRubyDungeon` mit 1/15 an einer Lavastelle (OreSpawnWorld.java:37, :2083-2092).
    - Islands: `addD4RubyDungeon` auf Gras, bei `LessLag` nur mit 1/2 (:142, :2292-2301).
  - Despawn ohne Persistenz (Cockateil.java:84-86).
  - Recherche: „Ruby Bird Dungeon nur in Utopia“; der Code baut ihn zusätzlich in Islands.
- **Zustand:** DW 22 = Vogeltyp (5) (Cockateil.java:81, :88-94). NBT `BirdType` (:252-260).
- **Sounds:** Living `orespawn:rubybird` nur bei Tag ohne Regen (RubyBird.java:19-24); Hurt und Death `duck_hurt` (Cockateil.java:111-117); Lautstärke 0.55 (:96-98).
- **Textur:** `Bird6.png` über `Cockateil.getTexture()` Fall 5 (Cockateil.java:69-70, :268; RenderCockateil.java:41-44), im neuen Pfad `textures/entity/bird6.png` (manifest texture_map). Der Manifest-Eintrag `ruby_bird` hat keine Textur, weil sie dynamisch gewählt wird.
- **Config:** keine eigenen Keys; `DimensionID4` über Cockateil, `LessLag` über den Dungeon.
- **Portierung 1.21.1:**
  - `extends Cockateil` (Port).
  - `defineSynchedData` darf denselben Accessor nicht zweimal definieren. Typ 5 deshalb im Konstruktor per `entityData.set` setzen, nicht im Builder.
  - 1:1 heißt `flyup = 0`. Die Initialisierungsreihenfolge gilt in Java 21 genauso: Wird `flyup` im Port per Feldinitialisierer gesetzt, bleibt der Befund bestehen.
  - `checkSpawnRules` → true, damit der Spawner ohne Lichtregeln spawnt.
  - Dimension über `ResourceKey<Level>` statt `DimensionID4`.

### Scorpion - Scorpion (`scorpion`)

- **Rolle:** Kleiner Feind auf Basis von `EntityMob`. Er greift Spieler, Tiere, Spinnen, Creeper und Velocity Raptors an und ist Diener des Emperor Scorpion.
- **Werte:** XP 10 (Scorpion.java:28), fireResistance 100 (:29), **nicht** feuerimmun (:30), Hitbox 0.85×0.55 (:26), Tempo 0.2 (:25). Leben, Schaden und Rüstung aus der Config (:43-45, :73-95).
- **KI und Angriffe:**
  - Aufgaben: 0 `EntityAISwimming`, 1 `EntityAIMoveThroughVillage(1.0,false)`, 2 `MyEntityAIWanderALot(14,1.0)`, 3 `EntityAIWatchClosest(EntityPlayer,8)`, 4 `EntityAILookIdle`; targetTasks 1 `EntityAIHurtByTarget(false)` (Scorpion.java:33-38).
  - Mit 1/6: Suche in `expand(8,3,8)` (:238). Abstand² < 9 → attacking=1, Chance `rand(5)==0 || rand(6)==1` Nahkampf (Attribut 4). Dazu serverseitig mit 1/3 der Sound `orespawn:scorpion_attack` **am Ziel** (0.75/1.5). Sonst `tryMove(1.2)` (Scorpion.java:146-171).
  - Ziele, in dieser Reihenfolge geprüft (Scorpion.java:181-232):
    1. nein: ignorierbare, unsichtbare, `Ghost`, `GhostSkelly`
    2. ja: `VelocityRaptor`, `EntitySpider`, `EntityCaveSpider`
    3. nein: `Scorpion`, `EmperorScorpion`
    4. ja: `EntityCreeper`
    5. nein: übrige `EntityMob`, Kreativspieler
    6. ja: alles andere Lebende, auch Tiere
  - Immun gegen `cactus` (Scorpion.java:173-179).
- **Interaktion:** keine (Scorpion.java:138-140).
- **Drops:** `getDropItem` würfelt rand(10): 0 `gold_nugget`, 1 `uranium_nugget`, 2 `titanium_nugget`, sonst nichts (Scorpion.java:124-136). Menge nach vanilla `dropFewItems`. XP 10.
- **Spawnen:**
  - Spawner-Zweig „Scorpion“ (Scorpion.java:262-276).
  - Sonst gültiges Licht **und** (Nacht **oder** y ≤ 50) (:277).
  - Listen: Overworld desert/roofedForest/savanna/savannaPlateau/mesa/mesaPlateau/mesaPlateau_F als `ambient` (manifest); Chaos (2, 1-3) (BiomeGenUtopianPlains.java:407).
  - `EmperorScorpion` ruft Diener über `spawnCreature("Scorpion")` (EmperorScorpion.java:427).
  - Spawner in `makeDungeon` (GenericDungeon.java:175), `makeBouncyCastle` (:3179-3209, Overworld) und `makeDamselInDistress` (:3717-3722, VillageMania).
  - Despawn ohne Persistenz (Scorpion.java:64-66).
- **Zustand:** DW 20 = attacking. Kein NBT.
- **Sounds:** kein Living-Sound (Scorpion.java:104-106); Hurt `orespawn:scorpion_hit`, Death `orespawn:cryo_death`; Lautstärke 1.5, Pitch 1.0 (:108-122); `scorpion_attack` wie oben.
- **Config:** `Scorpion_health` 15, `_attack` 4, `_defense` 10, `ScorpionEnable` 1, `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - `Monster` in `MobCategory.AMBIENT`.
  - Spawnregel „Nacht oder y ≤ 50“ in `checkSpawnRules`. In 1.21.1 reicht die Welt bis y −64, deshalb erlaubt y ≤ 50 hier einen deutlich größeren Bereich als im Original (0..50).
  - Den Angriffssound über `level().playSound(null, target.blockPosition(), …)` am Ziel abspielen.
