# Verhalten: entity-13

Dieser Batch umfasst dreizehn Klassen aus drei Gruppen. Neun sind Lebewesen: das zähmbare Flug-Haustier `Stinky`, der Pflanzen fressende Battle-Mob `VelocityRaptor`, die Portal-Termite `Termite`, die Werbe-Attrappe `Tshirt` sowie fünf Feinde mit eigener Ziel- und Angriffslogik (`TRex`, `TerribleTerror`, `Triffid`, `TrooperBug` alias Jumpy Bug, `Urchin` alias Crystal Urchin). Vier sind Geschosse: `SunspotUrchin` (Wurfwaffe), `ThunderBolt` (Thunder Staff und Royal-Bosse), `UltimateArrow` (Ultimate Bow) und `UltimateFishHook` (Ultimate Fishing Rod, angelt auch in Lava). Fast alle Feinde ignorieren die Vanilla-Zielaufgaben und suchen Ziele selbst per `findSomethingToAttack()` mit `GenericTargetSorter`. Die Wahrscheinlichkeitswürfe hängen dabei direkt am KI-Tick. Mehrere Klassen haben Eigenheiten, die beim Port bewusst entschieden werden müssen: fest verdrahtete Schadenswerte statt Config, nicht gespeicherte Zustände, die Spieler-Geschwindigkeit, die nur der Client setzt, und `isDaytime()` in Dimensionen ohne Tageszyklus. Die Spawn-Listen der OreSpawn-Dimensionen stehen nicht im Manifest, weil sie nicht über `EntityRegistry.addSpawn` laufen. Sie sind hier aus `BiomeGenUtopianPlains` und `ChunkProviderOreSpawn2` nachgetragen.

Dimensionszuordnung für alle Abschnitte: `DimensionID2` = `BaseDimensionID`+1 „Dimension-Extreme" (Mining), `DimensionID3` = +2 „VillageMania", `DimensionID4` = +3 „Islands", `DimensionID5` = +4 „Crystal", `DimensionID6` = +5 „Chaos" (OreSpawnMain.java:1266-1271; WorldProviderOreSpawn2..6.java:13/19). Die Biome-Listen: `setIslandCreatures` (BiomeGenUtopianPlains.java:62-120, WorldProviderOreSpawn4.java:27), `setCrystalCreatures` (:121-191, WorldProviderOreSpawn5.java:27), `setVillageCreatures` (:192-253, WorldProviderOreSpawn3.java:27), `setChaosCreatures` (:254ff, WorldProviderOreSpawn6.java:27). `spawnableCaveCreatureList` ist in 1.7.10 die Liste für `EnumCreatureType.ambient`.

SRG-Namen, die in `reference/jar/mcp/*.csv` fehlen und deren Bedeutung nur aus der Nutzung erschlossen ist: `func_152115_b` (Besitzer-UUID setzen), `func_152114_e` (ist Besitzer), `func_152113_b` (Besitzer-String lesen), `func_145881_a` (Spawner-Logik des TileEntityMobSpawner), `func_147487_a` (WorldServer: Partikel an Clients senden), `EnchantmentHelper.func_151386_g` / `func_151387_h` (Luck-of-the-Sea- / Lure-Stufe), `StatList.field_151183_A` / `field_151184_B` (Junk- / Treasure-Statistik). Aus `methods.csv`/`fields.csv` belegt sind: `func_110163_bv` = `enablePersistence`, `func_145775_I` = `doBlockCollisions`, `func_146035_c` = `handleHookCasting`, `func_146034_e` = `handleHookRetraction`, `func_150709_a` = `setMaxDamagePercent`, `func_150707_a` = `setEnchantable`, `func_150708_a` = `getItemStack`, `field_146042_b` = `angler`, `field_146043_c` = `caughtEntity`, `field_146044_a` = `shake`, `field_146037_g` = `xTile`, `field_146051_au` = `inGround`, `field_146049_av` = `ticksInGround`, `field_146047_aw` = `ticksInAir`, `field_146055_aB` = `fishPosRotationIncrements`.

`GenericTargetSorter` (von fast allen Feinden hier benutzt) sortiert nach Distanz², halbiert sie für `EntityCreeper` und teilt sie durch `height*width`, wenn das Produkt > 1 ist. Große Ziele werden also bevorzugt (GenericTargetSorter.java:15-33). `MyUtils.isIgnoreable` schließt RockBase, EntityAnt, EntityButterfly, EntityMosquito, Dragonfly, Firefly, Cricket, Cockateil, Termite, Ghost, GhostSkelly und Elevator aus (MyUtils.java:17-18).

---

### Stinky - Stinky (`stinky`)

- **Rolle:** zähmbares Mini-Drachen-Haustier, Basis `EntityTameable`. Es hat zwei Bewegungsmodi: `activity` 1 = Boden mit Vanilla-KI, `activity` 2 = Flug mit eigener Motion, die KI-Tasks laufen dann nicht (Stinky.java:505-507). Stinky greift nur Monster an, frisst Kohleerz und lässt je nach Hautfarbe Items fallen. Gegenüber Spielern ist es nie feindlich.
- **Werte:**
  - XP 35 (Stinky.java:59). `fireResistance` 1000, feuerimmun (:45-46), atmet unter Wasser (:129-131), kein Fallschaden (`fall`/`updateFallState` leer, :258-262), immun gegen Kaktus (:288).
  - Nahkampfschaden fest 10.0 über `getAttackStrength` (:276-284). Das Attribut ist nicht beteiligt.
  - Nahkampfreichweite: Distanz² < (3 + Ziel.width/2)² (:583).
  - Regeneration: +1 HP mit Chance 1/100 je `updateAITasks`, solange HP < 100 (:508-510). +1 HP je gefressenem Kohleerz (:612).
  - Im Wasser motionY +0.07 je Tick (:383-385). Im Flugmodus motionY ×0.6 je Tick (:410-412).
  - Zähmchance 1/2 (`nextInt(2)==1`, :146).
  - Die Flucht setzt ein, wenn Stinky gezähmt ist und HP/Max < 0.25 (:574).
- **KI und Angriffe:**
  - Tasks (:49-57), keine targetTasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 1 | EntityAISwimming | – |
    | 2 | EntityAIAvoidEntity | EntityMob, 8.0, 0.3, 0.4 |
    | 3 | MyEntityAIFollowOwner | 1.15, 12.0, 2.0 |
    | 4 | EntityAITempt | 1.25, `Items.beef`, false |
    | 5 | EntityAIPanic | 1.5 |
    | 6 | EntityAIWatchClosest | EntityPlayer, 6.0 |
    | 7 | MyEntityAIWander | 0.75 |
    | 8 | EntityAILookIdle | – |
    | 9 | EntityAIMoveIndoors | – |

  - `updateAITasks` (:498-539): Rache-Ziel wird mit Chance 1/200 gelöscht. Wenn Stinky nicht sitzt: `activity` 0 wird zu 1. Mit Chance 1/100 wird neu gewürfelt, 1/20 davon ergibt Flug, sonst Boden. Fliegt der Besitzer (`capabilities.isFlying`), gilt `owner_flying`=1 und Stinky fliegt. Ist Stinky im Bodenmodus gezähmt und der Besitzer weiter als Distanz² 256 entfernt, fliegt es ebenfalls. Danach folgt `do_movement()`.
  - Angriff (:571-588): Chance 1/7 je KI-Tick, nicht auf Peaceful. Gesucht wird ein `EntityLivingBase` in `boundingBox.expand(12,6,12)`, sortiert. Das Ziel muss `EntityMob` oder `Mothra` sein, per EntitySenses sichtbar und über einen Raytrace ab posY+0.75 frei (:680, :687-699). Bei `PlayNicely` != 0 gibt es kein Ziel (:684). Wilde Stinkys greifen ebenfalls an. Ist ein Ziel gefunden, fliegt Stinky dorthin (Ziel-y+1) und schlägt in Reichweite zu. Gezähmt und unter 25 % HP spiegelt es den Zielpunkt und flieht.
  - Kohleerz fressen, nur `activity` 1 (:589-617): Chance 1/50 und `PlayNicely`==0. Es sucht in Würfelschalen i=1..8 mit dy=min(i,2), ab i≥4 wird jede zweite Schale übersprungen (:596-607), ausgehend von posY+1. Das nächste `Blocks.coal_ore` wird mit Tempo 1.25 angesteuert. Bei Distanz² < 12 wird der Block zu Luft (Flag 2, **keine mobGriefing-Prüfung**), Stinky heilt +1 und spielt `random.burp` (Vol 0.5, Pitch 1.5-1.7) (:608-615).
  - Flug, nur `activity` 2 (:619-676): ein neuer Zielpunkt wird gewählt bei Distanz² < 2.1, mit Chance 1/300, bei Besitzer-Distanz² > 100 oder bei fliegendem Besitzer und Distanz² > 36. Bis zu 50 Versuche, Ursprung ist der Besitzer (falls gezähmt), sonst Stinky selbst. x/z-Versatz ±(6..9) bei Besitzer am Boden, ±(0..7) bei fliegendem Besitzer, ±(6..10) ohne Besitzer. y-Versatz `nextInt(6+2*owner_flying)-2`. Der Zielblock muss Luft und per Raytrace sichtbar sein.
  - Flugphysik: `motionX/Z += (sign*0.5 - m)*0.15*sf`, `motionY += (sign*0.7 - m)*0.21*sf`. sf = 1.0, bei fliegendem Besitzer 1.75, bei zusätzlich Besitzer-Distanz² > 49 3.5. `moveForward = 0.75*sf`, Yaw dreht um 1/3 der Differenz (:657-676).
  - Treffer beenden Sitzen und setzen `activity` 2 (:290-291).
- **Interaktion** (:137-205):
  - `Items.beef` bei Distanz² < 16. Wild: auf dem Server Zähmchance 1/2, dabei `setTamed`, Besitzer-UUID (`func_152115_b`), Herz-Effekt, volle Heilung; sonst Rauch-Effekt. Gezähmt und Besitzer (`func_152114_e`): volle Heilung. Das Item wird verbraucht, außer im Kreativmodus.
  - `Blocks.deadbush` als Item, gezähmt, Besitzer: entzähmt, HP auf 100, Besitzer "" (:177-193).
  - Besitzer mit anderem Item oder leerer Hand: Sitzen umschalten, beim Hinsetzen `activity` 1 (:194-203).
  - Sonst `super.interact` (EntityAnimal). `isBreedingItem` ist nicht überschrieben (`isWheat` ist ein toter 1.6-Name), Vanilla-Weizen kann Stinky also verliebt machen, aber `createChild` liefert null, es gibt keinen Nachwuchs (:207-209, :272-274). Reiten gibt es nicht.
- **Drops:**
  - `dropFewItems` nur gezähmt: 1-4× `beef` (:239-248). Wild nichts, `getDropItem`=beef wird dadurch nie genutzt.
  - Laufende Drops auf dem Server: mit Chance 1/1750 je Tick 1× `coal` vor dem Kopf plus `random.burp` (:314-317, :300-304). Mit Chance 1/2000 je Tick `orespawn:fart` (Vol 1.0, Pitch 1.5) und 1 Item hinter dem Körper nach Hautfarbe (:318-377, :306-310):

    | skin | Item | skin | Item |
    |---|---|---|---|
    | 0 | blaze_powder | 10 | titanium_nugget |
    | 1 | rotten_flesh | 11 | appletree_seed |
    | 2 | melon_seeds | 12 | diamond |
    | 3 | uranium_nugget | 13 | sand |
    | 4 | wheat | 14 | cobblestone |
    | 5 | brick | 15 | bone |
    | 6 | torch | 16 | string |
    | 7 | emerald | 17 | cherrytree_seed |
    | 8 | gold_ingot | 18 | peachtree_seed |
    | 9 | leaves (oak) | | |
- **Spawnen:**
  - `getCanSpawnHere`: `worldObj.isDaytime()` und höchstens 2 Stinky in `expand(20,10,20)` (:268-270, :702-705).
  - Listen: Nether monster 2/1/1, mesa, mesaPlateau und mesaPlateau_F ambient je 1/1/1 (manifest; OreSpawnMain.java:4465-4468). Islands-Dimension ambient 2/1/2 (BiomeGenUtopianPlains.java:87). Spawner „Stinky" in `GenericDungeon.makeStinkyHouse` (GenericDungeon.java:5407). Der Spawner prüft ebenfalls `getCanSpawnHere`, Spawner-Stinkys brauchen also Tag.
  - `canDespawn`: nicht persistent und nicht gezähmt (:211-213).
  - offen: ob `isDaytime()` im Nether jemals true liefert; das hängt am Vanilla-`skylightSubtracted` des Hell-Providers und ist nicht im Batch belegt.
- **Zustand:** DataWatcher 20 = `SpyroFire` (int, nur in NBT genutzt), 21 = `activity`, 22 = `skin` 0..18 (:73-75). NBT: `SpyroActivity`, `SpyroFire`, `StinkySkin` (:80-94), dazu die Vanilla-EntityTameable-Schlüssel. Der Server schreibt `activity` und `skin` alle 20 Ticks neu, der Client liest sie (:396-409). Mit Chance 1/2000 je Living-Tick würfelt die Haut neu 0..18 (:386-389), bei skin < 0 initial zufällig (:393-395). Renderer: skin i ergibt `Stinkytexture(i+1).png`, sonst `Stinkytexture1.png` (RenderStinky.java:60-117).
- **Sounds:** living keiner (:215-217). Hurt `orespawn:duck_hurt`, death `orespawn:cryo_death`, Vol 0.6 (:219-229). Pitch 1.0±0.1, als Kind 1.5±0.1 (:250-252). Dazu `random.burp` und `orespawn:fart`.
- **Config:** `StinkyEnable` (Spawn-Listen), `PlayNicely` (Angriffe, Kohlefraß).
- **Portierung 1.21.1:**
  - `TamableAnimal`, Besitz über `setOwnerUUID`/`isOwnedBy`, Sitzen über `setOrderedToSit` plus `SitWhenOrderedToGoal`.
  - Flugmodus: `goalSelector` im Modus 2 nicht ticken lassen (eigenes Flag in `serverAiStep`/`customServerAiStep`), die Motion aus `do_movement` 1:1 in `customServerAiStep` übernehmen. Keine `FlyingMoveControl`, die Physik ist handgeschrieben.
  - `isFood()` false zurückgeben, sonst entsteht mit Weizen eine sinnlose Verliebt-Animation.
  - Kohleerz: 1:1 nur `Blocks.COAL_ORE`. Deepslate-Erz wäre eine bewusste Erweiterung. Das Original prüft kein mobGriefing; ob `EventHooks.canEntityGrief` ergänzt wird, ist eine Entscheidung.
  - Spawnregel: In 1.21.1 liefert `Level.isDay()` für Dimensionen mit `fixed_time` (Nether) immer false. Die Nether-Spawns wären damit tot. Das Prädikat muss für den Nether explizit entschieden werden.
  - 19 Texturen je Skin im Renderer, `SynchedEntityData` mit drei `EntityDataSerializers.INT`.
  - Kaktus: `DamageTypes.CACTUS` in `hurt` ablehnen. Fall: `causeFallDamage` gibt false zurück.

### SunspotUrchin - SunspotUrchin (`sunspot_urchin`)

- **Rolle:** Wurfgeschoss (`EntityThrowable`) des Items `sunspoturchin` (ItemSunspotUrchin.java:24; Wurf-Sound `random.bow` Vol 0.5, :22) und des Dispensers (MyDispenserBehaviorSunspotUrchin.java:10).
- **Werte:**
  - Entity-Treffer: Schaden 3.0, gegen `EntityCreeper` 6.0 (SunspotUrchin.java:53-56). Nicht feuerimmune Ziele brennen 5 s (:59-61). **Spieler werden weder verletzt noch angezündet** (:57).
  - Block-Treffer: Feuer auf dem Nachbarblock der getroffenen Seite, wenn dort Luft ist (:65-96). Keine mobGriefing-Prüfung.
  - Das Geschoss brennt selbst (`setFire(1)` je Tick, :109) und dreht den Pitch um +30° je Tick (:110-116).
  - Geschwindigkeit und Schwerkraft: Vanilla-`EntityThrowable`-Default, nicht überschrieben.
  - Partikel: `smoke` je Tick, beim Aufprall 5× `smoke` plus `reddust` (:98-101, :117). Nur der Server entfernt das Geschoss (:102-104).
- **KI und Angriffe:** keine.
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur durch Item und Dispenser. Zusätzlich angelbar als Lava-Fang der `UltimateFishHook` (Gewicht 25, UltimateFishHook.java:480). Registrierung `registerModEntity` 64/1/true (OreSpawnMain.java:3066).
- **Zustand:** `my_index` = 50, der Symbol-Index in `spinners.png` (:26, :47-49). Konstant, nicht synchronisiert, kein NBT.
- **Sounds:** keine eigenen.
- **Config:** keine.
- **Portierung 1.21.1:**
  - `ThrowableItemProjectile` mit `getDefaultItem()` = `orespawn:sunspoturchin`. Treffer in `onHitEntity`/`onHitBlock`, Schaden über `damageSources().thrown(this, getOwner())`. Ausnahme `instanceof Player`, Brand über `igniteForSeconds(5)`. Feuer über `BaseFireBlock.getState` auf `pos.relative(face)`.
  - Renderer (client): Das Original zeichnet Zelle 50 eines 16×16-Rasters in `spinners.png`, also Spalte 2, Zeile 3, mit Skalierung 0.5 (RenderSpinner.java:33-36; manifest `gl_scale`). Er wird mit WaterBall, InkSack, LaserBall, IceBall, Acid und DeadIrukandji geteilt (RenderItemUrchin.java). Ein eigener `EntityRenderer` mit UV-Ausschnitt ist nötig, `ThrownItemRenderer` zeigt das falsche Bild.
  - Der Dispenser-Wurf hat keinen Werfer, `getOwner()` null ist unkritisch.

### TRex - T. Rex (`t_rex`)

- **Rolle:** feindlicher Dinosaurier, `EntityMob`, greift fast jedes Lebewesen an.
- **Werte:**
  - XP 150 (TRex.java:29), `fireResistance` 100 (:30), immun gegen Kaktus (:146).
  - Schaden = Attribut `TRex_stats.attack` über `super.attackEntityAsMob` (:129). Danach Rückstoß horizontal 1.2, vertikal 0.1, verdoppelt auf 0.2 bei Spielern oder totem Ziel (:131-137).
  - Nahkampfreichweite: Distanz² < (4 + Ziel.width/2)² (:181). Verfolgung mit Tempo 1.25 (:188).
  - Angriffstakt: Die Zielprüfung läuft mit Chance 1/5 je KI-Tick (:161), der Schlag dann mit `nextInt(4)==0 || nextInt(5)==1` (:183).
  - Keine Regeneration.
- **KI und Angriffe:**
  - Tasks (:32-37): 0 Swimming. 1 MoveThroughVillage(1.0, false). 2 MyEntityAIWanderALot(16, 1.0). 3 WatchClosest(EntityPlayer, 8.0). 4 LookIdle. targetTasks: 1 HurtByTarget(false). Es gibt keinen Vanilla-Angriffstask.
  - `updateAITasks` (:156-195): Zuerst gilt `rt`, der letzte lebende Angreifer aus `attackEntityFrom` (:148-151). `PlayNicely` → kein Ziel. `rt` wird bei Tod oder mit Chance 1/200 vergessen, bei fehlender Sicht nur für diese Runde übersprungen. Sonst sucht `findSomethingToAttack` in `expand(20,6,20)` (:236). T. Rex dreht sich zum Ziel (10°/10°), schlägt in Reichweite mit `setAttacking(1)` zu, sonst navigiert er. Ohne Ziel `setAttacking(0)`.
  - Zielfilter (:197-230): nicht `isIgnoreable`, sichtbar, nicht `TRex`, `Cryolophosaurus` oder `VelocityRaptor`, kein Kreativspieler. Passive Tiere und Dorfbewohner sind damit gültige Ziele.
- **Interaktion:** `interact` gibt false zurück (:124-126).
- **Drops** (:109-119), streuen ±3 Blöcke x/z bei y+1 und werden direkt gespawnt (:104-107):
  - 1× `trextooth`, 1× `minecraft:item_frame`, 7× `beef`.
  - 2-5 Durchläufe mit je 1× `uranium_nugget` und 1× `titanium_nugget`.
  - `getDropItem`=beef (:100-102) wird nicht genutzt, weil `dropFewItems` überschrieben ist.
- **Spawnen:**
  - `getCanSpawnHere` (:259-297): Steht ein Spawner mit „T. Rex" in x/z -3..2, y 0..4, ist das Ergebnis sofort true. Sonst müssen alle Bedingungen gelten: `isValidLightLevel`, posY ≥ 50, nicht Tag, eine 3×3-Säule y+1..y+5 komplett Luft, kein weiterer TRex in `expand(24,12,24)`.
  - Listen: Mining-Dimension (`DimensionID2`) monster 6/1/2 (ChunkProviderOreSpawn2.java:361). Chaos monster 1/1/1 (BiomeGenUtopianPlains.java:416). Spawner in `GenericDungeon.addLevelDecorations` (decor 5, difficulty 6 und weitere Varianten; GenericDungeon.java:591, 620, 653, 690, 730) und `addLevelDecorationsQ` (:6725-6925). Der Manifest-Eintrag `spawns` ist leer, weil kein `addSpawn` erfolgt.
  - `canDespawn`: nicht persistent (:52-54).
- **Zustand:** DataWatcher 20 = attacking (int), vom Modell genutzt (:47-50, :251-257). Kein eigenes NBT, `rt` wird nicht gespeichert.
- **Sounds:** living `orespawn:trex_living` nur mit Chance 1/4 je Aufruf (:77-82). Hurt `orespawn:alo_hurt`, death `orespawn:trex_death`. Vol 1.5, Pitch 1.0 (:84-98).
- **Config:** `TRexEnable`. `TRex_health` 160, `TRex_attack` 22, `TRex_defense` 14 (OreSpawnMain.java:6151; manifest). `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster`. Die Attribute werden in `EntityAttributeCreationEvent` festgelegt, bevor die ModConfigSpec geladen ist. Health, Attack und Armor aus der Config deshalb im Konstruktor oder in `finalizeSpawn` per `getAttribute(...).setBaseValue` setzen. ARMOR 14 liegt unter der Grenze 30.
  - Zielwahl als eigenes `Goal` mit 1/5-Takt, nicht über `NearestAttackableTargetGoal`, weil Rache-Ziel und Größen-Gewichtung gebraucht werden.
  - Rückstoß: `target.push(cos*1.2, 0.1|0.2, sin*1.2)` plus `hurtMarked` für Spieler.
  - Spawner-Ausnahme: in `checkSpawnRules` statt Blockscan auf `MobSpawnType.SPAWNER` prüfen.
  - Drops mit Streuung in `dropCustomDeathLoot` per `spawnAtLocation` samt Versatz nachbauen. Eine Loot-Tabelle kann den Versatz nicht.
  - Mining- und Chaos-Spawns gehören in die Biome-Spawn-Settings der eigenen Dimensions-Biome.

### Termite - Termite (`termite`)

- **Rolle:** winzige feindliche Termite, erbt `EntityAnt` → `EntityAnimal` (formal Tier, verhält sich feindlich). Portal-Mob zur Crystal-Dimension, frisst Holz und vermehrt sich.
- **Werte:**
  - XP 1 (Termite.java:31), überschreibt die 0 von `EntityAnt` (EntityAnt.java:26).
  - Biss: fester Schaden 1.0 (:61), nur mit Chance 1/15 je Aufruf (:55), nie auf Peaceful (:58). Das Attribut attackDamage 2.0 wird dafür nicht benutzt.
  - Automatischer Biss alle 20 Ticks gegen den nächsten verwundbaren Spieler im Radius 1.5 (:109-122).
  - Regeneration: +1 HP je Holzfraß (:258).
- **KI und Angriffe:**
  - Tasks aus `EntityAnt`: 0 Panic(1.4), 1 MyEntityAIWanderALot(9, 1.0) (EntityAnt.java:28-29). Zusätzlich aus `Termite`: 0 Panic(1.4), 1 AttackOnCollide(EntityPlayer, 1.0, false), 2 MyEntityAIWanderALot(8, 1.0) (:33-35). Panic und Wander stehen damit doppelt in derselben Liste.
  - targetTasks: 1 NearestAttackableTarget(EntityPlayer, 6, true), nur wenn `PlayNicely`==0 **zum Konstruktionszeitpunkt** (:36-38).
  - `AttackOnCollide` ruft das `attackEntityAsMob` mit 1/15-Chance auf.
  - `updateAITick` (:213-263): Chance 1/200 löscht das Rache-Ziel. Mit Chance 1/200 und `PlayNicely`==0 läuft die Holzsuche: Schalen i=1..7, dy=min(i,4), ab i≥5 jede zweite übersprungen (:226-237), ab posY+1, Navigation mit Tempo 1.0. Bei Distanz² < 6:
    - Chance 2/3: Holz → `dirt` (nur mit mobGriefing), neue Termite an der eigenen Position, falls weniger als 10 Termiten in `expand(3,3,3)` sind.
    - Chance 1/3: Holz → Luft (nur mit mobGriefing), neue Termite am Blockort, gleiche Obergrenze.
    - In beiden Fällen +1 HP. **Die Vermehrung passiert auch ohne mobGriefing.**
  - Holzliste `isWood` (:125-127): fence, fence_gate, planks, wooden_slab, double_wooden_slab, bed, crafting_table, standing_sign, bookshelf, wooden_door, wooden_pressure_plate, birch_stairs, oak_stairs, jungle_stairs, spruce_stairs, `crystalplanks`. Keine Stämme, keine Akazien- oder Schwarzeichentreppen.
- **Interaktion (Dimensionsreise, :66-101):**
  - Wirkt nur für `EntityPlayerMP`. Die Hand muss leer sein, sonst Chat „Empty your hand!".
  - Außerhalb `DimensionID5`: Hauptinventar leer, sonst „Empty your inventory!". Keine Rüstung, sonst „Take off your armor!". Dann `transferPlayerToDimension(DimensionID5)` mit `OreSpawnTeleporter`.
  - In `DimensionID5`: zurück nach Dimension 0, ohne Inventarprüfung.
  - Research sagt „Crystal Dimension" und bestätigt das.
- **Drops:** keine (`EntityAnt.dropFewItems` leer, EntityAnt.java:116-117). XP 1.
- **Spawnen:**
  - Geerbtes `getCanSpawnHere`: posY ≥ 50 und höchstens 4 `EntityAnt` (alle Ameisen-Unterklassen) in `expand(20,10,20)` (EntityAnt.java:127-134). Keine Biome-Liste.
  - Quellen:
    - `TermiteBlock` (`AntBlock`, randomTick): kein Regen, Luft darüber → 2-7 Termiten, geprüft gegen `TermiteEnable` (AntBlock.java:46-73; OreSpawnMain.java:5945).
    - `CrystalTermiteBlock` genauso (CrystalAntBlock.java:50-80; OreSpawnMain.java:5946).
    - `TermiteTroll` (`OreBasicStone`) setzt beim Abbau 15-20 Termiten frei (OreBasicStone.java:38-41; OreSpawnMain.java:1540).
    - Selbstvermehrung wie oben.
  - Alle diese Direkt-Spawns umgehen `getCanSpawnHere`.
  - `canDespawn`: nicht persistent (EntityAnt.java:56-58). Auch vermehrte Termiten verschwinden also.
- **Zustand:** kein eigener DataWatcher, kein NBT. `attack_delay` ist flüchtig.
- **Sounds:** keine (`EntityAnt`: alle null, Vol 0.0, `playStepSound` leer; EntityAnt.java:97-114).
- **Config:** `TermiteEnable` (nur in den Nestblöcken), `PlayNicely`, `BaseDimensionID` (→ `DimensionID5`).
- **Textur:** `termite.png` über `EntityAnt.getTexture` (EntityAnt.java:50-51, :148), neu `textures/entity/termite.png` (manifest texture_map). Die Texturliste des Renderers im Manifest ist leer, weil die Auswahl in `EntityAnt` passiert.
- **Portierung 1.21.1:**
  - Basis: eine gemeinsame `EntityAnt`-Portklasse (Animal oder PathfinderMob) mit `isFood` false. `Peacock` zielt auf `Termite` (Peacock.java:47), der Typ muss also unterscheidbar bleiben.
  - Teleport: `player.changeDimension(new DimensionTransition(...))` mit eigener Platzierung statt `OreSpawnTeleporter`, Ziel `ResourceKey<Level>` der Crystal-Dimension. Die Inventarprüfung muss in 1.21.1 zusätzlich die Offhand prüfen (1.7.10 hatte keine). Chat über `displayClientMessage(Component.literal(...))`.
  - Holzliste: 1:1 auf Eiche, Birke, Tropen, Fichte abbilden (Planken/Zäune/Stufen gab es 1.7.10 nur als Meta-Varianten). Eine Tag-Lösung (`minecraft:planks`, `wooden_stairs` usw.) erweitert den Umfang; das ist eine bewusste Entscheidung.
  - mobGriefing über `EventHooks.canEntityGrief`.
  - `MeleeAttackGoal` plus `doHurtTarget`, das in 14 von 15 Fällen false zurückgibt.
  - Peaceful-Sperre selbst prüfen, Animal verschwindet auf Peaceful nicht.

### TerribleTerror - Terrible Terror (`terrible_terror`)

- **Rolle:** kleiner fliegender Feind, `EntityMob` **ohne jede KI-Task**. Das Fliegen ist handgeschriebene Motion.
- **Werte:**
  - XP 10 (TerribleTerror.java:25), `fireResistance` 5 (:27), nicht feuerimmun (:26).
  - Schaden fest 5.0 (:80). Das Attribut aus `TerribleTerror_stats.attack` wird gesetzt (:35), `attackEntityAsMob` ignoriert es aber. **Eine Config-Änderung am Angriffswert wirkt nicht.**
  - movementSpeed fest 0.1 (:34). Nahkampfreichweite Distanz² < 6.0 (:121).
  - Schwerkraftdämpfung: motionY ×0.6 nach jedem `onUpdate` (:74-77). Kein Fallschaden (:142-146).
- **KI und Angriffe:**
  - Keine `tasks`/`targetTasks`. `updateAITasks` (:88-136):
    - Neues Flugziel mit Chance 1/100 oder bei Distanz² < 2.1: bis 50 Versuche, x/z ±(5..9), y -2..+2, Ziel muss Luft und per Raytrace ab posY+0.75 frei sein (:99-115).
    - Sonst mit Chance 1/9 `findSomethingToAttack` in `expand(12,8,12)` (:263). Das Flugziel wird Ziel-y+1, Angriff bei Distanz² < 6 (:116-125). Kein Cooldown außer der Wahrscheinlichkeit.
    - Motion: `X/Z += (sign*0.4 - m)*0.3`, `Y += (sign*0.7 - m)*0.2`, `moveForward` 0.75, Yaw 1/4 der Differenz (:126-135).
  - Getroffen: das Flugziel springt auf die Angreiferposition (:152-159).
  - Ausschlüsse (:180-257): RockBase, TerribleTerror, EnderReaper, Mothra, LurkingTerror, CloudShark, Rotator, Bee, Mantis, LeafMonster, CreepingHorror, Triffid (zweimal geprüft), PitchBlack, Dragon, Island, IslandToo, EntityButterfly, Firefly, Kreativspieler. `MyUtils.isIgnoreable` wird **nicht** benutzt, Ameisen und Termiten sind also Ziele.
  - `PlayNicely` → kein Ziel (:260).
- **Interaktion:** keine eigene.
- **Drops:** `getDropItem` würfelt 1/3 `rotten_flesh`, 1/3 `emerald`, 1/3 `feather` (:278-287). Die Menge folgt dem Vanilla-`EntityLiving.dropFewItems` (nicht überschrieben, Zahl nicht im OreSpawn-Quelltext).
- **Spawnen:**
  - `getCanSpawnHere` (:161-178): ein Spawner „Terrible Terror" in x/z -2..1, y 0..4 ergibt true. Sonst `isValidLightLevel`, nicht Tag und (`dimensionId == DimensionID6` oder posY ≤ 40).
  - Listen: Islands monster 25/3/6 (BiomeGenUtopianPlains.java:102), Chaos monster 4/2/6 (:332). Spawner in `GenericDungeon.makeDungeon` (GenericDungeon.java:199, 289-304) und `makeMiniDungeon` (:2351-2394).
  - Research nennt „Danger Dim.". Laut Code spawnt es in der Islands-Dimension nur unterhalb von y 40, in Chaos überall.
  - `canDespawn`: nicht persistent und Tag (:38-40). Das Tier verschwindet also tagsüber.
- **Zustand:** kein DataWatcher, kein NBT.
- **Sounds:** living `orespawn:terribleterror_living`, hurt `orespawn:terribleterror_hit`, death `orespawn:terribleterror_dead`. Vol 0.45, Pitch 1.0 (:42-60).
- **Config:** `TerribleTerrorEnable`. `TerribleTerror_health` 10 und `_defense` 3 wirken, `_attack` 5 wirkt nur als Attribut (OreSpawnMain.java:6192; manifest). `PlayNicely`, `BaseDimensionID`.
- **Portierung 1.21.1:**
  - `Monster` ohne Goals. Motion in `customServerAiStep`, die ×0.6-Dämpfung **nach** `super.tick()`. In 1.7.10 lief sie nach `onLivingUpdate` samt Bewegung; 1.21.1 hat dieselbe Schwerkraft von 0.08 in `travel`.
  - Keine FlyingPathNavigation nötig.
  - Chaos-Prüfung über `level.dimension()`. Tag über `level.isDay()`.
  - Der feste Schaden 5 und der ungenutzte Config-Wert sind original, als bekannte Eigenheit dokumentieren.

### ThunderBolt - (kein Lang-Name, kein Registry-Eintrag) (`–`)

- **Rolle:** Blitz-Geschoss (`EntityThrowable`). Quellen: Thunder Staff (ItemThunderStaff.java:28), TheKing (TheKing.java:745), TheQueen (TheQueen.java:761), ThePrince (ThePrince.java:793), ThePrincess (ThePrincess.java:897), ThePrinceTeen (ThePrinceTeen.java:1088, 1453), ThePrinceAdult (ThePrinceAdult.java:1065, 1422). **Nirgends registriert:** `OreSpawnMain` enthält weder `registerGlobalEntityID` noch `registerModEntity` dafür, `ClientProxyOreSpawn` keinen Renderer, das Manifest keinen Eintrag.
- **Werte:**
  - Grundwert 40.0 (ThunderBolt.java:29). Treffer: 20.0 als `causeThrownDamage(this, thrower)` und direkt danach 20.0 als `causeMobDamage(thrower)`, außerdem `setFire(1)` (:34-36).
  - Der zweite Treffer im selben Tick fällt in das Vanilla-Unverwundbarkeitsfenster und zählt nur, wenn er den ersten übersteigt. Bei gleichem Wert wirken also effektiv 20 plus Explosion plus Blitz. Das ist aus der Vanilla-Logik abgeleitet, nicht im Batch-Quelltext. Research nennt 40.
  - Royalty-Ziel (`MyUtils.isRoyalty`: ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower; MyUtils.java:9-11): sofort `setDead`, **kein Schaden, keine Explosion, kein Blitz** (:30-33).
  - Sonst bei jedem Aufprall, auch auf Blöcke: Explosion Stärke 3.0, Blockschaden nur mit mobGriefing, nur auf dem Server (:44-46). Ein echter `EntityLightningBolt` bei y+1 per `addWeatherEffect` (:47, auf beiden Seiten). Sound `random.explode` Vol 0.5, Pitch 0.5-1.5 (:43).
  - Partikel: beim Aufprall 20× `smoke`, `largesmoke` und `fireworksSpark` (:38-42), im Flug 4× `fireworksSpark` je Tick (:51-56).
  - Flugbahn: Vanilla-Throwable-Default. Der Thunder Staff startet 1 Block seitlich versetzt bei y+1.55, multipliziert die Anfangsgeschwindigkeit ×3 und gibt dem Spieler Rückstoß 0.5 horizontal / 0.15 vertikal (ItemThunderStaff.java:26-38). Der Staff hat 50 Ladungen und lädt nur bei Gewitter 1 je 50 Ticks (ItemThunderStaff.java:16-18, :44-50).
  - Wird der Blitz mit dem Koordinaten-Konstruktor der Bosse erzeugt, ist der Werfer null, und `causeMobDamage(null)` entsteht.
- **KI und Angriffe:** keine.
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur durch Staff und Bosse.
- **Zustand:** keiner.
- **Sounds:** `random.explode`.
- **Config:** keine (nur Gamerule mobGriefing).
- **Portierung 1.21.1:**
  - Braucht einen eigenen `EntityType`. Das Original hat keine Id, ein Name wie `thunder_bolt` ist eine Port-Entscheidung, nicht aus dem Manifest.
  - Explosion: `level.explode(this, x, y, z, 3.0f, mobGriefing ? ExplosionInteraction.MOB : ExplosionInteraction.NONE)`.
  - Blitz: nur auf dem Server `EntityType.LIGHTNING_BOLT.create(level)`, `moveTo`, `addFreshEntity`. Der Blitz verursacht selbst Schaden und Feuer, das ist Teil des Originalverhaltens.
  - Renderer: das Original war unregistriert und damit clientseitig vermutlich unsichtbar (offen: nicht belegt). Für den Port einen Partikel-Renderer ohne Modell vorsehen.

### Triffid - Triffid (`triffid`)

- **Rolle:** stationäre Pflanze, `EntityMob`. Nur verwundbar, solange sie geöffnet ist.
- **Werte:**
  - XP 50 (Triffid.java:30), `fireResistance` 75 (:31), lässt sich nicht schieben (:195-197).
  - `hurt_timer` 300 Ticks nach **jedem** Treffer, auch nach abgewiesenen (:206-215). Solange er läuft: motionX/Z = 0 (:139-143), Feuer gelöscht und geschlossen (:223-227).
  - Regeneration: +1 HP mit Chance 1/250 je KI-Tick (:228-230), unabhängig vom Zustand.
  - Nahkampfreichweite Distanz² < 25 (:243), Suche `expand(10,8,10)` (:312). Schaden = Attribut `Triffid_stats.attack` über `super` (:199-202).
- **KI und Angriffe:**
  - Tasks (:35-38): 0 Swimming, 3 WatchClosest(EntityPlayer, 10.0), 4 LookIdle. targetTasks: 1 HurtByTarget(false).
  - Zentrierung in `onUpdate`, Chance 1/100 (:72-98): zählt Nicht-Luft-Blöcke bei y-1 entlang z und x im Bereich ±5 und verschiebt das Navigationsziel je Block um ±1 zur Seite mit mehr Boden. Die Triffid wandert so zur Inselmitte. Tempo 1.0.
  - In `onUpdate` bei `hurt_timer` ≤ 0: Yaw zum nächsten Ziel (:99-107).
  - `updateAITasks` (:218-259):
    - `hurt_timer` herunterzählen.
    - Chance 1/80 ohne laufenden Timer: 1/8 öffnen, sonst schließen.
    - Chance 1/10 ohne laufenden Timer: Ziel suchen → öffnen. In Reichweite ausrichten, `attacking` 1, schlagen; sonst `attacking` 0.
  - Schadensannahme (:204-216): läuft `hurt_timer` oder ist sie geschlossen (DW21 == 0), wird der Schaden **abgewiesen** und der Timer auf 300 zurückgesetzt. Schläge auf die geschlossene Pflanze verlängern die Schutzphase. Offen: Schaden wird angenommen, dann schließt sie sich.
  - Research: [CC] „heilt geschlossen" ist so im Code nicht vorhanden, die Heilung ist zustandsunabhängig. [OS] „nicht treffbar, solange geschlossen" stimmt.
  - Ausschlüsse (:261-306): `isIgnoreable`, EntityCreeper, EnderReaper, Triffid, TerribleTerror, LurkingTerror, PitchBlack, Dragon, Kreativspieler.
- **Interaktion:** keine eigene.
- **Drops** (:188-193), ±2 Blöcke gestreut (:178-186): 4-9× `greengoo`, 1× `minecraft:item_frame`. `getDropItem` (gold_nugget 1/3, :170-176) ist ungenutzt.
- **Spawnen:**
  - `getCanSpawnHere` ist immer true (:343-345). Es gibt keine Biome-Liste.
  - Die `Island`-Entity setzt eine Triffid bei Insel-y+2.01, wenn in ±10/±5/±10 keine steht, mit Chance 1/(2 + 2000/timer) je Tick (Island.java:328-333).
  - Spawner in `GenericDungeon.makeGreenhouseDungeon` (GenericDungeon.java:5180, 5186).
  - `canDespawn`: nicht persistent (:65-67). Insel-Triffids verschwinden und werden nachgesetzt.
  - offen: wo `TriffidEnable` ausgewertet wird. Im Batch-Grep taucht keine Spawn-Liste auf; die Island-Klasse gehört zu einem anderen Batch.
- **Zustand:** DataWatcher 20 = attacking, 21 = offen(1)/geschlossen(0) (:41-44, :327-341). `RenderInfo renderdata` ist ein Animationsspeicher und wird nicht synchronisiert. Kein NBT, `hurt_timer` geht beim Neuladen verloren.
- **Sounds:** living `orespawn:triffid_living`, hurt `orespawn:triffid_hit`, death `orespawn:triffid_dead`. Vol 0.75, Pitch 1.0 (:150-168).
- **Config:** `Triffid_health` 100, `_attack` 20, `_defense` 12 (OreSpawnMain.java:6174; manifest). `PlayNicely`. `TriffidEnable` siehe offen.
- **Portierung 1.21.1:**
  - `hurt()` gibt im geschlossenen Zustand false zurück und setzt trotzdem den Timer. Vorsicht, dass `invulnerableTime` und Hurt-Sounds nicht doppelt auslösen.
  - `isPushable` false.
  - `ModelTriffid` hat 178 Teile, Textur 532×715, eine `glRotatef(-90°, Y)`-Transformation auf das ganze Modell und Helfer `leafpartA..D` (reference/jar/models/ModelTriffid.json, anim bytecode 4264 Bytes). Die Öffnungsanimation hängt an DW21.
  - Mit 100 HP gibt es keine virtuelle Gesundheit.

### TrooperBug - Jumpy Bug (`jumpy_bug`)

- **Rolle:** bossartiger Riesenkäfer, `EntityMob`. Springt Ziele an und ruft Spit Bugs herbei.
- **Werte:**
  - XP 150 (TrooperBug.java:35), `fireResistance` 100 (:36).
  - Eigene Unverwundbarkeit: `hurt_timer` 20 Ticks nach Schaden (:369-374, :392-394). Immun gegen Kaktus und Fallschaden (:372).
  - `jump()`: motionY +1.15, posY +1.5, Vorwärtsschub 0.2-0.65 (:115-122).
  - `jumpAtEntity`: motionY +1.25, posY +1.25, Schub 0.3-0.55 in Zielrichtung (:124-132).
  - Rückstoß nach Treffer: horizontal 1.8, vertikal 0.2, verdoppelt auf 0.4 bei Spieler oder totem Ziel (:350-365).
  - Nahkampfreichweite Distanz² < (5 + Ziel.width/2)² (:409). Verfolgung Tempo 1.2 (:424).
  - Regeneration +1 HP mit Chance 1/150 (:434-436). Suche `expand(12,7,12)` (:501). Schaden = Attribut `TrooperBug_stats.attack` (:354).
- **KI und Angriffe:**
  - Tasks (:40-45): 0 Swimming. 1 MoveThroughVillage(0.9, false). 2 MyEntityAIWanderALot(14, 1.0). 3 WatchClosest(EntityPlayer, 10.0). 4 LookIdle. targetTasks: 1 HurtByTarget(false).
  - In der Luft wird der Pfad gelöscht (:79-81).
  - `updateAITasks` mit Chance 1/5 (:395-433): Ziel ist `getAttackTarget` (tote werden entfernt), sonst Suche. Ausrichten 10°/10°. Dann:
    - Chance 1/10 und am Boden: `jumpAtEntity`.
    - Sonst in Reichweite: `attacking` 1, Schlag mit `nextInt(6)==0 || nextInt(7)==1`, Sound am Ziel: mit Chance 1/3 `orespawn:scorpion_attack` (Vol 1.4), sonst `orespawn:clatter` (Vol 1.0).
    - Sonst am Boden: Navigation.
    - Zusätzlich mit Chance 1/30: ein „Spit Bug" auf der Mitte zwischen Käfer und Ziel, x/z ±4, y Mitte+1.01, mit `playLivingSound` (:426-428, :439-448). **Keine Obergrenze, `SpitBugEnable` wird nicht geprüft.**
    - Ohne Ziel `attacking` 0.
  - Getroffen von einem `EntityLiving`, also einem Mob (Spieler erben in 1.7.10 nicht von `EntityLiving`): sofortiges Angriffsziel plus Navigation 1.2 (:375-381). Spieler kommen über HurtByTarget ins Ziel.
  - Ausschlüsse (:450-495): `isIgnoreable`, Hydrolisc, EnderReaper, EnderKnight, EntityEnderman, EntityCreeper, TrooperBug, SpitBug, Kreativspieler.
- **Interaktion:** `interact` gibt false zurück (:346-348).
- **Drops** (:175-341), ±4 Blöcke gestreut (:165-173):
  - 1× `jumpybugscale`, 1× `minecraft:item_frame`, 2-6× `amethyst`.
  - Dazu 1-5 Würfe mit `nextInt(14)`:

    | Wurf | Item | mögliche Verzauberungen (Chance, Stufe) |
    |---|---|---|
    | 0, 2 | `blockamethyst` (case 0 fällt in case 2 durch) | – |
    | 1, 12, 13 | nichts | – |
    | 3 | `amethystsword` | Sharpness 1/6 (1-5), Bane of Arthropods 1/6 (1-5), Knockback 1/6 (1-5), Looting 1/6 (1-5), Unbreaking 1/2 (2-5), Fire Aspect 1/6 (1-5), Sharpness **ein zweites Mal** 1/6 (1-5) |
    | 4 | `amethystshovel` | Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5) |
    | 5 | `amethystpickaxe` | Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5), Fortune 1/6 (1-5) |
    | 6 | `amethystaxe` | Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5) |
    | 7 | `amethysthoe` | Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5) |
    | 8 | `amethyst_helmet` | Protection, Blast Prot., Fire Prot., Projectile Prot. je 1/6 (1-5), Unbreaking 1/2 (2-5), Respiration 1/6 (1-2), Aqua Affinity 1/6 (1-5) |
    | 9 | `amethyst_chest` | Protection, Blast, Fire, Projectile je 1/6 (1-5), Unbreaking 1/2 (2-5) |
    | 10 | `amethyst_leggings` | wie 9 |
    | 11 | `amethyst_boots` | Feather Falling 1/6 (**5-9**), Unbreaking 1/2 (2-5) |

    Belege: TrooperBug.java:182-338. Mehrere Stufen liegen über dem Vanilla-Maximum.
- **Spawnen:**
  - `getCanSpawnHere` (:524-557): ein Spawner „Jumpy Bug" in x/z -3..2, y 0..4 ergibt true. Sonst `isValidLightLevel`, tagsüber besteht der Wurf nur bei `nextInt(20)` ≤ 1 (2 von 20), und die Fläche x/z -2..1, y+1..y+4 muss komplett Luft sein.
  - Listen: swampland ambient 3/1/2, mesa ambient 1/1/1 (manifest; OreSpawnMain.java:4547-4548). Chaos monster 1/1/1 (BiomeGenUtopianPlains.java:389). Spawner als `critter` „Jumpy Bug" in `GenericDungeon.addLevelDecorations` (GenericDungeon.java:702, 739) und `addLevelDecorationsQ` (:6900, 6937).
  - `canDespawn`: nicht persistent (:72-74).
- **Zustand:** DataWatcher 20 = attacking. `RenderInfo` ist lokal. Kein NBT.
- **Sounds:** living `orespawn:clatter` mit Chance 1/4 (:138-143), hurt `orespawn:crunch`, death `orespawn:emperorscorpion_death`, Vol 1.5, Pitch 1.0 (:145-159). Angriff: `orespawn:scorpion_attack` / `orespawn:clatter` (:414-419).
- **Config:** `TrooperBugEnable`. `TrooperBug_health` 200, `_attack` 20, `_defense` 15 (OreSpawnMain.java:6161; manifest). `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster`. `jump()` → `jumpFromGround` überschreiben. `isAirBorne` → `!onGround()`/`hasImpulse`. Der posY-Sprung per `setPos`.
  - Verzauberungen sind datengetrieben: `registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS)`, dann `stack.enchant(holder, level)`. Stufen über dem Maximum sind erlaubt. Der doppelte Sharpness-Wurf überschreibt in `ItemEnchantments` den ersten Eintrag; in 1.7.10 standen zwei NBT-Einträge nebeneinander (offen: welcher gewertet wurde).
  - Das Spawn-Kategorie-Mapping „ambient" für einen Feind ist eine Entscheidung (MONSTER oder AMBIENT); das Original teilt sich damit den Ambient-Cap.
  - `ModelTrooperBug`: 134 Teile, 512×256, vier Bein-Helfer (anim bytecode 4218 Bytes).
  - Spit-Bug-Beschwörung ohne Cap: 1:1 übernehmen oder deckeln (Entscheidung).

### Tshirt - T-Shirt (`t_shirt`)

- **Rolle:** unbewegliche Werbe-Attrappe, `EntityAnimal`, 4×4 Blöcke, 1 HP, keine KI-Tasks, kein Angriff.
- **Werte:** XP 40 (Tshirt.java:18), `fireResistance` 100 (:19), movementSpeed 0 (:16, :25).
- **KI und Angriffe:** keine Tasks (Konstruktor :14-20), `isAIEnabled` true.
- **Interaktion:** `interact` gibt false zurück (:85-87), damit gibt es auch kein Züchten. `createChild` null (:101-103).
- **Drops:** `getDropItem` = `emerald` (:78-80), Menge nach Vanilla-`dropFewItems` (nicht überschrieben).
- **Spawnen:**
  - `getCanSpawnHere` (:89-99): Tag, posY ≥ 50, kein weiteres Tshirt in `expand(20,8,20)`. Die Vanilla-Grasprüfung von EntityAnimal entfällt.
  - Liste: VillageMania-Dimension ambient 2/1/1 (BiomeGenUtopianPlains.java:244).
  - `canDespawn`: nicht persistent (:34-36).
- **Zustand:** keiner.
- **Sounds:** keine (:58-68).
- **Config:** `TshirtEnable`.
- **Portierung 1.21.1:**
  - `Animal` oder `PathfinderMob` ohne Goals, `isFood` false, `mobInteract` gibt `InteractionResult.PASS` zurück.
  - Anzeigename: Manifest „T-Shirt", `LanguageRegistry` setzt „T-Shirt!" (OreSpawnMain.java:3629). Einen Wert wählen.
  - `ModelTshirt`: 2 Teile, das Modell deklariert 512×256, die PNG ist laut Manifest 320×160. Die UV-Größe im `LayerDefinition` gegen die echte Textur prüfen.

### UltimateArrow - (kein Lang-Name) (`ultimate_arrow`)

- **Rolle:** Pfeil (`EntityArrow`) der Ultimate Bow (UltimateBow.java:39, Tempo 3.0) und der Begleiter Girlfriend/Boyfriend (Girlfriend.java:990, Boyfriend.java:890; Tempo 2.0, Streuung 10.0).
- **Werte:**
  - Schaden = `ceil(|v| × UltimateBowDamage)` (UltimateArrow.java:187-188). Kritisch: +`nextInt(dmg/2 + 2)` (:207-209).
  - Rückstoß über das eigene Feld `knockbackStrength` × 0.6 / Horizontaltempo, dazu +0.1 y (:226-231). `setDamage` wird ignoriert (:317-318), `getDamage` liefert `UltimateBowDamage` (:320-322).
  - Ein brennender Pfeil zündet das Ziel 5 s an (:217-219).
  - **PvP-Schutz**, wenn `ultimate_sword_pvp` == 0: Treffer auf `EntityPlayer`, `Girlfriend`, `Boyfriend` oder ein gezähmtes `EntityTameable` heilen 1.0, spielen `random.bowhit` und entfernen den Pfeil ohne Schaden (:189-206).
  - Im Boden nach 500 Ticks weg (:123-125). Dämpfung 0.99, im Wasser 0.8, Schwerkraft 0.05 je Tick (:295-307).
  - Kollisionsbox der Ziele +0.3 (:170-171). Der eigene Schütze ist erst ab 5 Ticks Flugzeit treffbar (:151).
  - Ignoriert `Elevator` sowie `Cephadrome`, `Dragon` und `EntityHorse`, solange sie geritten werden (:151-169).
  - Abgewiesener Schaden: Tempo ×-0.1, Yaw +180 (:239-246). Getroffene `EntityLiving` bekommen Pfeilzähler +1, ein Spieler-Schütze erhält das Treffer-Paket `S2BPacketChangeGameState(6)` (:220-235).
  - Ultimate Bow: kritisch mit Chance 1/4 (UltimateBow.java:40-42), Punch → Rückstoß (:43-46), Flame → `setFire(100)` (:47-49), nicht aufhebbar (`canBePickedUp = 2`, :52).
- **KI und Angriffe:** keine.
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur durch Bogen und Begleiter. Registrierung nur `registerGlobalEntityID "UltimateArrow"` (OreSpawnMain.java:5016). Das Tracking übernimmt der Vanilla-EntityTracker für `EntityArrow`, die Werte stehen nicht im Batch (Manifest `tracking` null). Renderer: Vanilla `RenderArrow`.
- **Zustand:** `entityInit` ersetzt den Vanilla-Aufbau durch `addObject(16, 0)` (:71-73), das Krit-Flag. offen: das Dekompilat zeigt `(Object)0`, der Typ (Byte wie Vanilla oder Integer) ist nicht ablesbar. Die Felder `xTile..ticksInAir` **verschatten** die privaten Felder von `EntityArrow`, das geerbte NBT schreibt aber die Elternfelder. Der Flugzustand stimmt nach dem Neuladen daher nicht (aus Code abgeleitet).
- **Sounds:** `random.bowhit` (:192, :200, :236, :261).
- **Config:** `UltimateBowDamage` (Default 10), `UltimateSwordPvp` → `ultimate_sword_pvp` (Default 0) (manifest).
- **Portierung 1.21.1:**
  - `AbstractArrow`, `pickup = DISALLOWED`, `getDefaultPickupItem` leer.
  - Die 1.21.1-Schadensformel (`ceil(speed × baseDamage)` plus Krit-Zufall) entspricht der 1.7.10-Formel. `baseDamage` auf den Config-Wert setzen und `onHitEntity` nur für PvP-Heilung und die Geritten-Ausnahmen überschreiben.
  - Die Despawn-Zeit 500 statt Vanilla über `tickDespawn` festlegen.
  - Punch ist in 1.21.1 ein Verzauberungseffekt, Rückstoß daher selbst aus der Punch-Stufe des Bogens berechnen. „Geritten" → `isVehicle()`.
  - Renderer `ArrowRenderer` mit Vanilla-Pfeiltextur.

### UltimateFishHook - (kein Lang-Name) (`ultimate_fish_hook`)

- **Rolle:** Angelhaken (`EntityFishHook`) der Ultimate Fishing Rod (UltimateFishingRod.java:48). Feuerfest, angelt in Wasser **und Lava**.
- **Werte:**
  - Größe 0.25, `fireResistance` 3000, feuerimmun (UltimateFishHook.java:58-61).
  - Wurf: Grundtempo 0.4, dann `handleHookCasting` mit Faktor 1.5 und Streuung 0.0075 (:90-94, :104-117).
  - Abbruch: Angler tot, hält nicht `ultimatefishingrod` oder Distanz² > 1024 (:173-177). Im Boden 1200 Ticks (:194-196).
  - Dämpfung Luft 0.92, am Boden oder bei Kollision 0.5. In Flüssigkeit zusätzlich ×0.9 und motionY ×0.8. Auftrieb `0.04 × (2·Anteil − 1)` (:267-368). Der Anteil zählt Wasser **oder** Lava in 5 Scheiben (:271-283).
  - Beißzyklus auf dem Server (:284-358):
    - Grundrate k = 1, +1 mit Chance 25 % bei `canLightningStrikeAt`, −1 mit Chance 50 % ohne Himmelssicht.
    - `fish_wait_time` = zufällig 50..300 minus Lure×100 (:353-354).
    - Danach `ticks_catchable` = 100..200 mit anschwimmenden Blasen (:348-349, :310-325).
    - Dann `fish_on_hook` = 10..30 Ticks mit `random.splash` Vol 0.25 und Absinken 0.2 (:302-308).
  - Einholen (`handleHookRetraction`, :397-437):
    - Entity am Haken: Zug `0.1·Δ`, y zusätzlich `sqrt(dist)·0.08`, Rutenschaden 3.
    - Fisch: `EntityItem` bei y+1.25 mit `fireResistance` 3000 (überlebt Lava), fliegt zum Spieler, dazu ein XP-Orb 1-6, Rutenschaden 1.
    - Haken im Boden: Rutenschaden 2.
    - Die Rute wendet den Rückgabewert als `damageItem` an (UltimateFishingRod.java:41-42) und hält sich selbst auf Unbreaking 2 (UltimateFishingRod.java:25, :28-33).
  - Entity-Treffer: `attackEntityFrom(thrown, 0.0)`; ist das true, hängt der Haken am Ziel (:240-242).
- **Beute** (`func_146033_f`, :439-467):
  - Junk-Schwelle = clamp(0.1 − LotS·0.025 − Lure·0.01). Treasure-Schwelle = clamp(0.05 + LotS·0.01 − Lure·0.01) (:443-446).
  - **Lava** (`handleLavaMovement()` oder Block `lava`/`flowing_lava` am Haken): immer die Lava-Tabelle, Statistik fishCaught (:447-451).
  - Sonst f < Junk-Schwelle → Junk. Danach f − Junk < Treasure-Schwelle → Treasure. Sonst 50 % Vanilla-Fisch, 50 % OreSpawn-Fisch (:452-466).

  | Tabelle | Einträge (Gewicht) | Beleg |
  |---|---|---|
  | Lava | `sunspoturchin` 25, `lavaeel` 10, `sunfish` 15, `sparkfish` 10, `firefish` 15 | :480 |
  | Junk | leather_boots 10 (Schaden bis 90 %), leather 10, bone 10, potion (Wasserflasche) 10, string 5, fishing_rod 2 (bis 90 %), bowl 10, stick 5, dye (10× Tintenbeutel) 1, tripwire_hook 10, rotten_flesh 10 | :477 |
  | Treasure | waterlily 1, name_tag 1, saddle 1, bow 1 (25 %, verzaubert), fishing_rod 1 (25 %, verzaubert), book 1 (verzaubert) | :478 |
  | Vanilla-Fisch | cod 60, salmon 25, clownfish 2, pufferfish 13 | :479 |
  | OreSpawn-Fisch | `bluefish` 25, `pinkfish` 10, `rockfish` 15, `woodfish` 10, `greyfish` 15 | :481 |

  Research (03-items.md:131) nennt die Gewichte „unknown"; sie sind hiermit belegt.
- **KI und Angriffe:** keine.
- **Interaktion:** über die Rute.
- **Drops:** siehe Beute.
- **Spawnen:** nur durch die Rute. Registrierung nur `registerGlobalEntityID "UltimateFishHook"` (OreSpawnMain.java:3060), Renderer Vanilla `RenderFish`.
- **Zustand:** NBT `xTile`, `yTile`, `zTile` (short), `inTile`, `shake`, `inGround` (byte) (:374-390). Kein DataWatcher (`entityInit` setzt nur Feuerschutz, :99-102).
- **Sounds:** `random.splash`. Die Rute spielt beim Auswerfen `random.bow` Vol 0.5 (UltimateFishingRod.java:46).
- **Config:** keine.
- **Portierung 1.21.1:**
  - `Player.fishing` ist als `FishingHook` typisiert, der Haken muss also `FishingHook` erweitern. Dessen Logik ist privat und fest an `Items.FISHING_ROD` und Wasser gebunden (`shouldStopFishing`, `catchingFish`). Deshalb `tick()` und `retrieve(ItemStack)` komplett überschreiben und die 1.7.10-Physik von oben nachbauen.
  - Lava über `FluidTags.LAVA`, `fireImmune()` im EntityType-Builder. Das Beute-Item braucht ebenfalls Feuerfestigkeit (`ItemEntity` + `setInvulnerable` oder das Item selbst `fireResistant`).
  - Beute als eigene Loot-Tabellen (`orespawn:gameplay/ultimate_fishing/{lava,junk,treasure,fish,orespawn_fish}`) mit den Gewichten oben. Luck of the Sea und Lure sind datengetrieben (`EnchantmentHelper.getFishingLuckBonus` / `getFishingTimeReduction`). Die Lure-Umrechnung ×100 Ticks je Stufe ist original und muss manuell passieren.
  - Rute als `FishingRodItem`-Unterklasse. NeoForge setzt die Leinenposition im `FishingHookRenderer` über `ItemAbilities.FISHING_ROD_CAST`, das muss die Rute melden.

### Urchin - Crystal Urchin (`crystal_urchin`)

- **Rolle:** feuriger Feind der Crystal-Dimension, `EntityMob`.
- **Werte:**
  - XP 20 (Urchin.java:30), `fireResistance` 1000, feuerimmun (:31-32), immun gegen Kaktus (:190-196).
  - Angriff: Ziel brennt 5 s, dann Schaden = Attribut `Urchin_stats.attack` (:161-164).
  - Nahkampfreichweite Distanz² < 8.0 (:174). Prüfung mit Chance 1/8 (:171), Schlag bei `nextInt(7)==0 || nextInt(8)==1` (:176). Suche `expand(16,3,16)` (:255), Verfolgung Tempo 1.2 (:181).
  - **Wasser schadet:** in 1/3 der Living-Ticks und davon 1/5 greift der Urchin **sich selbst** an (`attackEntityAsMob(this)`), Schaden = Attribut, dazu Rauchpartikel (:114-122).
  - Tages-Selbstentfernung: Chance 1/400 je Tick bei Weltzeit % 24000 < 12000, wenn nicht persistent und nicht aus einem Spawner (:69-83).
- **KI und Angriffe:**
  - Tasks (:35-39): 0 Swimming. 1 MyEntityAIWanderALot(14, 1.0). 2 WatchClosest(EntityPlayer, 8.0). 3 LookIdle. targetTasks: 1 HurtByTarget(false).
  - Flammenpartikel mit Chance 1/3 je Living-Tick (:114-115).
  - Ausschlüsse (:198-249): `isIgnoreable`, Vortex, Rotator, Peacock, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin, Kreativspieler.
  - `PlayNicely` → kein Ziel (:252).
- **Interaktion:** `interact` gibt false zurück (:157-159).
- **Drops:** `getDropItem` würfelt `nextInt(3)`: 0 nichts, 1 `crystalpink_ingot`, 2 `crystalapple` (:146-155). Menge nach Vanilla-`dropFewItems`.
- **Spawnen:**
  - `getCanSpawnHere` (:278-313): ein Spawner „Crystal Urchin" in x/z ±2, y+1..y+3 setzt `was_spawnered`=1 und ergibt true. Sonst mindestens 6 von 9 Blöcken bei y+1 Luft, `isValidLightLevel` und Weltzeit % 24000 ≥ 13000.
  - Listen: Crystal monster 15/2/4 (BiomeGenUtopianPlains.java:149), Chaos monster 2/1/5 (:368). Spawner in `GenericDungeon.makeUrchinSpawner` (GenericDungeon.java:2637-2647) und `makeCrystalBattleTower` (:4945-4950).
  - `canDespawn`: nicht persistent und nicht aus Spawner (:65-67).
- **Zustand:** DataWatcher 20 = attacking. `was_spawnered` wird **nicht** gespeichert, nach dem Neuladen gilt ein Spawner-Urchin wieder als natürlich und verschwindet tagsüber. `RenderInfo` ist lokal.
- **Sounds:** living `orespawn:kyuubi_living`, hurt `orespawn:glasshit`, death `orespawn:glassdead`. Vol 1.1, Pitch 1.25 (:126-144).
- **Config:** `UrchinEnable`. `Urchin_health` 25, `_attack` 10, `_defense` 4 (OreSpawnMain.java:6156; manifest). `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit `fireImmune()`.
  - Selbstschaden im Wasser über `hurt(damageSources().mobAttack(this), attackValue)`, nicht über `doHurtTarget(this)`, das Statistiken und Rückstoß mitzieht.
  - Tageszeit über `level.getDayTime() % 24000`.
  - Spawner-Herkunft über `MobSpawnType.SPAWNER` in `finalizeSpawn`. Sie im NBT zu sichern wäre eine bewusste Korrektur des Originalfehlers.

### VelocityRaptor - Velocity Raptor (`velocity_raptor`)

- **Rolle:** zähmbarer kleiner Raptor. Basis `EntityCannonFodder` → `EntityTameable`. Battle Mob mit Team-Hüten, gibt dem Besitzer (clientseitig) mehr Tempo, frisst Pflanzen und ist züchtbar.
- **Werte:**
  - XP 5 (VelocityRaptor.java:33), `fireResistance` 10 (:30).
  - `mygetMaxHealth` gezähmt 20, wild 10 (:217-219). Das Attribut maxHealth wird nur in `applyEntityAttributes` beim Konstruieren gesetzt (:49), und dort ist `isTamed()` noch false. **Das Attribut bleibt 10**, die 20 wirkt nur in Vergleichen und `heal`-Beträgen (aus Code abgeleitet).
  - Schaden je Treffer auf 10.0 gedeckelt (:358-366).
  - Fallschaden: `ceil(Fall − 3)`, höchstens 2.0. Sound `damage.fallbig` bei > 3, sonst `damage.fallsmall` (:71-85).
  - Kampfmodus (nur `is_activated`==2, aus EntityCannonFodder.java):
    - Angriffsprüfung mit Chance 1/4 (`pfreq` 4), Schlag bei `nextInt(7)==0 || nextInt(6)==1` (`sfreq` 6), Schaden 4.0, Reichweite Distanz² < 9 (:357-379).
    - Suche `expand(10,4,10)` (:320). Rüstung 3, sonst 0 (:335-340).
  - Regeneration: +2 HP je Pflanzenfraß (VelocityRaptor.java:202). Aus CannonFodder +1 HP mit Chance 1/250 (EntityCannonFodder.java:384-386).
  - **Spieler-Tempo** (nur auf dem Client, `worldObj.isRemote`): Füttern mit Apfel als Besitzer setzt die movementSpeed-Basis des Spielers auf 0.6 (:259). Dead Bush und Sitz-Umschalten setzen 0.3 (:283, :313). Die Konstante 0.10000000149011612 kommt in `EntityPlayer.class` (client-1.7.10.jar, Klasse `yz`) vor. Das entspricht 6× beziehungsweise 3× Vanilla; die Zuordnung zum Attribut ist aus dem Kontext geschlossen. Research sagt „verdoppelt, Sitzen = aus". Laut Code wird nie auf Vanilla zurückgesetzt, der Rest-Wert 0.3 bleibt 3×.
- **KI und Angriffe:**
  - Tasks (:34-43):

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | EntityAISwimming | – |
    | 1 | EntityAIMate | 1.0 |
    | 2 | MyEntityAIFollowOwner | 1.5, 10.0, 2.0 |
    | 3 | MyEntityAIAvoidEntity | EntityMob, 8.0, 1.0, 1.4 |
    | 4 | EntityAITempt | 1.25, `Items.apple`, false |
    | 5 | EntityAIPanic | 1.6 |
    | 6 | EntityAIWatchClosest | EntityPlayer, 6.0 |
    | 7 | MyEntityAIWander | 0.9 |
    | 8 | EntityAILookIdle | – |
    | 9 | EntityAIMoveIndoors | – |

  - Pflanzenfraß in `updateAITick` (:170-207): Chance 1/200 löscht das Rache-Ziel. Wenn nicht sitzend, ((1/20 und HP < Max) oder 1/250) und `PlayNicely`==0: Schalen i=1..9, dy=min(i,2), ab i≥5 jede zweite übersprungen. Gesucht werden tallgrass, yellow_flower, red_flower, deadbush, double_plant, Navigation Tempo 1.0. Bei Distanz² < 12 wird der Block zu Luft (nur mit mobGriefing), +2 HP, `random.burp` (Vol 0.5, Pitch 1.5-1.7).
  - Battle-KI (EntityCannonFodder.java:346-387, nur `is_activated`==2):
    - Ziele: jedes `EntityMob`, andere `EntityCannonFodder` mit Hut ≠ 0 und ≠ eigener Hut, Spieler außer Kreativ und außer `name_one`/`name_two`.
    - Sitzend nur Ziele innerhalb Distanz² 144 um den Patrouillenpunkt, ohne Ziel Rückkehr dorthin mit Tempo 0.65 (:296-303, :380-382).
- **Interaktion**, Reihenfolge: zuerst `EntityCannonFodder.interact` (EntityCannonFodder.java:66-209), das selbst mit `EntityAnimal.interact` beginnt.
  1. Züchten mit `isBreedingItem` = `crystalapple` (VelocityRaptor.java:389-391).
  2. Gezähmt mit gesetztem `name_one`: Registrierung als zweiter Besitzer bzw. Tausch der Namen, `is_activated` 2 (voller Hut). Ein fremder Dritter wird blockiert (EntityCannonFodder.java:75-96).
  3. `carrot` → Hut 1 (rot), `quinoa` → Hut 2 (grün), `potato` → Hut 3 (blau). Jeweils zähmen, `name_one` setzen, `is_activated` ≥ 1, heilen, Persistenz (:97-162).
  4. `corn_seed` (Corn) bei `is_activated`==2: klont „Velocity Raptor" mit Hut und Besitzern, Sound `random.explode` Vol 0.75, Pitch 2.0 (:163-193).
  5. `is_activated`==2 mit anderem Item: Sitzen umschalten, Patrouillenpunkt setzen (:194-207).
  6. Dann `VelocityRaptor.interact`: `apple` bei Distanz² < 16. Wild: Zähmchance 1/2 (`nextInt(2)==0`, :242) plus volle Heilung. Gezähmt als Besitzer: heilen, Client-Tempo 0.6 (:239-273).
  7. `deadbush` als Besitzer: entzähmen, HP auf Max, Besitzer "", Client-Tempo 0.3 (:274-293).
  8. `name_tag` als Besitzer: `setCustomNameTag` (:294-304).
  9. Gezähmt als Besitzer, sonst: Sitzen umschalten, Client-Tempo 0.3 (:305-316).
- **Drops:** gezähmt 2-6× `red_flower` (Mohn) (:343-352). Wild nichts, `getDropItem` null (:339-341).
- **Zucht:** `createChild` liefert einen neuen VelocityRaptor (:376-383). Kinder bekommen in `canDespawn` `enablePersistence` und verschwinden nie (:368-374). Kinder werden in halber Größe gerendert (RenderVelocityRaptor.java:37-41). Anlocken mit Apfel, züchten mit Crystal Apple (`isWheat`=apple ist ein toter Name, :385-387).
- **Spawnen:**
  - `getCanSpawnHere`: posY ≥ 50 und Tag (:61-63).
  - Liste: Mining-Dimension ambient 1/2/4 (ChunkProviderOreSpawn2.java:394).
  - `canDespawn`: nicht gezähmt und nicht persistent. Kinder nie.
- **Zustand:**
  - DataWatcher 20 = `is_activated` (0 ohne, 1 halber Hut, 2 voller Hut), 21 = `hat_color` (0 ohne, 1 rot, 2 grün, 3 blau), beide aus EntityCannonFodder.java:44-48. Abgleich alle 5 Ticks (:50-64).
  - NBT `NameOne`, `NameTwo`, `IsActivated`, `HatColor`, `PatrolX`, `PatrolY`, `PatrolZ` (:238-278), dazu die Vanilla-Tameable- und Ageable-Schlüssel.
  - Textur: aktiviert mit Hut 2 → `velocityraptor2.png`, Hut 3 → `velocityraptor3.png`, sonst `velocityraptor.png`, auch bei rotem Hut (RenderVelocityRaptor.java:48-60).
- **Sounds:** living keiner (:320-325). Hurt `orespawn:cryo_hurt`, death `orespawn:cryo_death`, Vol 0.4 (:327-337). Pitch 1.0±0.1, als Kind 1.5±0.1 (:354-356). Dazu `random.burp`, `damage.fallbig`/`damage.fallsmall` und `random.explode` beim Klonen.
- **Config:** `VelocityRaptorEnable`, `PlayNicely`.
- **Portierung 1.21.1:**
  - `TamableAnimal`. `EntityCannonFodder` wird als gemeinsame Basis mit Ostrich, Lizard und Chipmunk (andere Batches) portiert.
  - Spieler-Tempo serverseitig als `AttributeModifier` (Id `orespawn:velocity_raptor_speed`) auf `Attributes.MOVEMENT_SPEED`. 1:1 zum Original wären `ADD_MULTIPLIED_BASE` +5.0 (0.6) bzw. +2.0 (0.3). Ob der dauerhafte 3×-Rest übernommen wird, ist eine Entscheidung. Das Original war ein reiner Client-Hack ohne Serverwissen.
  - Max-Health: 1:1 wirkt effektiv 10. Eine echte 20 für gezähmte Tiere wäre eine Korrektur.
  - `isFood` = `crystalapple`, `TemptGoal` mit `Items.APPLE`.
  - Namensschild: Vanilla `NameTagItem.interactLivingEntity` greift vor `mobInteract`, die Besitzer-Einschränkung des Originals entfällt sonst.
  - Pflanzenliste 1.7.10 → 1.21.1: `tallgrass` (Metas: tote Pflanze, Gras, Farn) → `SHORT_GRASS`, `FERN`. `yellow_flower` → `DANDELION`. `red_flower` (9 Metas) → Mohn, Orchidee, Allium, Porzellansternchen, vier Tulpen, Margerite. `double_plant` → Sonnenblume, Flieder, hohes Gras, großer Farn, Rosenstrauch, Pfingstrose. `deadbush` → `DEAD_BUSH`.
  - mobGriefing über `EventHooks.canEntityGrief`.
