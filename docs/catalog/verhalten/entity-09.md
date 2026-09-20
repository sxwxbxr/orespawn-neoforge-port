# Verhalten: entity-09

Dieser Batch umfasst dreizehn Klassen in vier Gruppen. **Kreaturen mit eigener
Ziel-KI:** Hammerhead, HerculesBeetle, Kyuubi, Irukandji und LeafMonster, alle
`EntityMob`. Sie suchen ihre Ziele nicht über vanilla `EntityAINearestAttackableTarget`,
sondern über eine handgeschriebene Schleife in `updateAITasks()` mit
`GenericTargetSorter`. **Tiere:** das zähmbare Haustier Hydrolisc (`EntityTameable`)
und der fliegende GoldFish (`EntityAnimal` ohne Tasks). **Weltbauer:** Island und
IslandToo sind zwei `EntityAnimal`-Hüllen, die zur Laufzeit schwebende Inseln aus
Blöcken bauen und diese Blockweise durch die Danger Dimension schieben. **Geschosse:**
die Familie LaserBall/IceBall (`EntityThrowable`), der InkSack der Attack Squid und
IrukandjiArrow, eine vollständige Kopie der vanilla-Pfeilphysik mit festen 100 Schaden.
Für den Port heikel sind vor allem: DataWatcher-Index 20 ist im Bytecode ein **Byte**,
obwohl das Decompilat `int` zeigt; `heal(-1)` wird als Schadensweg missbraucht;
mehrere `dropFewItems` liefern verzauberte Items über dem Vanilla-Maximum; und die
Inseln verändern ganze Bereiche der Welt direkt aus einem Entity-Tick.

Allgemeine Konventionen dieses Dokuments:
- „pro Tick“ bei KI-Prüfungen heißt: einmal pro Aufruf von `updateAITasks()`, also jeden Server-Tick.
- Werte in Klammern ohne Dateiname stammen aus der jeweils besprochenen Klasse.
- `PlayNicely` (Config `OreSpawnTWEAKS/PlayNicely`, Default 0, manifest) schaltet in allen `findSomethingToAttack()` die Zielsuche ab.

---

### GoldFish - Gold Fish (`gold_fish`)

- **Rolle:** friedlicher, fliegender Fisch („Goldfish“ laut `OreSpawnMain.java:3764`). Basis `EntityAnimal`, keine AI-Tasks, keine Zucht (`createChild` → null, GoldFish.java:159-161). Kann unter Wasser atmen (GoldFish.java:163-165).
- **Werte:**
  - `experienceValue = 5` (GoldFish.java:19). **offen:** Vanilla 1.7.10 `EntityAnimal.getExperiencePoints` überschreibt das vermutlich mit 1-3 XP; im OreSpawn-Code nicht nachprüfbar.
  - `fireResistance = 5` (GoldFish.java:21), nicht feuerimmun (GoldFish.java:20).
  - `attackDamage` 1.0 wird registriert (GoldFish.java:28-29), aber nie benutzt, weil es keinen Angriff gibt.
  - `motionY *= 0.6` in jedem `onUpdate` (GoldFish.java:73). Das hebt die Schwerkraft auf, der Fisch schwebt.
  - Kein Fallschaden, `fall` und `updateFallState` sind leer (GoldFish.java:131-135).
- **KI und Angriffe:** keine Tasks. Die Flugsteuerung steckt in `updateAITasks` (GoldFish.java:80-125):
  - Höhenband: unter Y 120 bekommt jedes neue Ziel +2 (GoldFish.java:92-94), über Y 140 bekommt es -2 (GoldFish.java:95-97).
  - Ein neues Flugziel entsteht mit Chance 1/300 pro Tick oder sobald das alte erreicht ist, also Abstand² < 2.1 (GoldFish.java:98).
  - Pro Zielsuche gibt es bis zu 50 Versuche (GoldFish.java:83). X- und Z-Versatz liegen je bei ±(5..9) (GoldFish.java:100-107), der Y-Versatz bei `rand(11)-5+updown` (GoldFish.java:108). Gültig ist ein Ziel nur, wenn der Block Luft ist **und** per Raytrace ab Augenhöhe +0.75 sichtbar ist (GoldFish.java:77, 110). Scheitern alle 50 Versuche, bleibt der letzte Kandidat als Ziel stehen.
  - Bewegung auf das Ziel: `motionX/Z += (sign(d)*0.4 - m)*0.3` (GoldFish.java:118, 120) und `motionY += (sign(d)*0.7 - m)*0.2` (GoldFish.java:119). Der Yaw dreht um 1/6 der Differenz pro Tick (GoldFish.java:124), dazu `moveForward = 0.75` (GoldFish.java:123).
  - `canBePushed` ist true, `collideWithEntity` ist leer, der Fisch schiebt also nichts an (GoldFish.java:56-61).
- **Interaktion:** keine.
- **Drops:** `getDropItem` würfelt mit `rand(3)` (GoldFish.java:145-157): 0 → `gold_block`, 1 → `uranium_nugget`, 2 → `titanium_nugget`. Die Anzahl kommt aus dem vanilla `dropFewItems`, das nicht überschrieben ist. **offen:** exakte Vanilla-Anzahl (0-2 plus Looting) nur aus dem vanilla Code; hier nicht belegt.
- **Spawnen:**
  - `getCanSpawnHere` ist immer true (GoldFish.java:141-143).
  - Keine Einträge im manifest. Natürliche Spawns gibt es nur als *cave creature* (1.21.1: AMBIENT) in den OreSpawn-Dimensionsbiomen, jeweils mit `GoldFishEnable` als Schalter:
    - Utopia (Konstruktorliste): Gewicht 1, Gruppe 1-1 (BiomeGenUtopianPlains.java:40)
    - Danger Dimension: 5, 2-4 (BiomeGenUtopianPlains.java:96, `setIslandCreatures`)
    - Chaos: 10, 2-4 (BiomeGenUtopianPlains.java:288, `setChaosCreatures`)
  - Spawner mit dem Namen `"Gold Fish"`: `GenericDungeon.makeGoldFishBowl` (GenericDungeon.java:2511) und `makeGirlfriendIsland` (GenericDungeon.java:5040, 5045).
  - Die Gold Fish Bowl entsteht in der Oberwelt mit Chance 1/350 pro Chunk (OreSpawnWorld.java:1202), nur wenn `DisableOverworldDungeons == 0` (OreSpawnWorld.java:269, 278).
  - Despawn: `canDespawn` ist `!isNoDespawnRequired() && !isDaytime()` (GoldFish.java:33). Der Fisch verschwindet also **nur nachts**.
- **Zustand:** kein DataWatcher, kein eigenes NBT. Das Flugziel wird nicht gespeichert.
- **Sounds:**
  - Lebend: `splash` (vanilla), Treffer: `splash` (GoldFish.java:44-50)
  - Tod: `orespawn:little_splat` (GoldFish.java:53)
  - Lautstärke 0.45, Tonhöhe 1.0 (GoldFish.java:36-42)
- **Config:** `GoldFishEnable` (Default 1, manifest) schaltet nur die Biome-Listen.
- **Portierung 1.21.1:**
  - Basis `Animal` mit `setNoGravity(false)` behalten und die Flugsteuerung in `customServerAiStep()` nachbauen. `motionY *= 0.6` gehört als `setDeltaMovement(v.multiply(1, 0.6, 1))` in `tick()`, nicht in die KI.
  - Den Raytrace auf `level().clip(new ClipContext(..., ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))` umstellen und auf `HitResult.Type.MISS` prüfen.
  - Die Despawn-Regel wird `removeWhenFarAway(double)` mit `!level().isDay()`. Achtung: `Animal` despawnt in 1.21.1 standardmäßig nicht, der Override ist also Pflicht.
  - `causeFallDamage` überschreiben und `false` zurückgeben.
  - Spawn-Kategorie AMBIENT hat ein eigenes Mob-Cap. Die Dimensionsbiome setzen die Einträge per Datapack-Biome-Modifier.
  - Drops als Loot-Table mit drei gleichgewichteten Alternativen.

### Hammerhead - Hammerhead (`hammerhead`)

- **Rolle:** großer, feindlicher Boss-Mob (Hitbox 3.0 × 5.0, manifest). Basis `EntityMob`. Er greift Spieler **und** Monster an.
- **Werte:**
  - XP 350 (Hammerhead.java:29), `fireResistance` 100 (Hammerhead.java:30), nicht feuerimmun.
  - `moveSpeed` 0.35 wird in jedem `onUpdate` neu gesetzt (Hammerhead.java:25, 57).
  - Die Rüstung kommt aus `getTotalArmorValue()` = `Hammerhead_stats.defense` (Hammerhead.java:65-67), Default 20 (manifest).
  - Immun gegen Kaktusschaden (Hammerhead.java:158).
  - Kein Schadensdeckel, keine Regeneration.
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | (Hammerhead.java:32) |
    | 1 | `EntityAIMoveThroughVillage` | Speed 1.0, nicht nur nachts (Hammerhead.java:33) |
    | 2 | `MyEntityAIWanderALot` | Radius 16, Speed 1.0, Start mit Chance 1/30 (Hammerhead.java:34; MyEntityAIWanderALot.java:35) |
    | 3 | `EntityAIWatchClosest` | Klasse `EntityLiving`, 8.0 (Hammerhead.java:35) |
    | 4 | `EntityAILookIdle` | (Hammerhead.java:36) |
    | Ziel 1 | `EntityAIHurtByTarget` | ohne Hilferuf (Hammerhead.java:37) |

  - `setAvoidsWater(true)` (Hammerhead.java:28).
  - Rachegedächtnis: jeder Schaden von einem `EntityLivingBase` setzt `rt` (Hammerhead.java:160-163).
  - Zielschleife, mit Chance 1/3 pro Tick (Hammerhead.java:173):
    - Als Ziel dient zuerst `rt`. `PlayNicely != 0` löscht es (Hammerhead.java:176-178).
    - `rt` wird vergessen, wenn es tot ist, mit Chance 1/250 (Hammerhead.java:180-183) oder wenn es nicht sichtbar ist (Hammerhead.java:184-186). Dann folgt `findSomethingToAttack()`.
    - Suchbox `expand(18, 9, 18)`, sortiert nach `GenericTargetSorter` (Hammerhead.java:240-241). Der Sorter halbiert den Abstand² bei Creepern und teilt ihn durch `height*width`, wenn das größer als 1 ist (GenericTargetSorter.java). Große Ziele gelten also als näher.
    - Gültige Ziele (Hammerhead.java:209-234): lebend und sichtbar, kein Hammerhead, Spieler nur außerhalb des Creative-Modus, jedes `EntityMob`, dazu alles aus `MyUtils.isAttackableNonMob`: Mothra, Leon, Dragon, Spyro, Royalty, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, `EntityVillager`, Stinky (MyUtils.java:13-15).
    - Nahkampf ab Abstand² < (7 + Zielbreite/2)² (Hammerhead.java:193). Dann `setAttacking(1)` und ein Schlag mit Chance `1/3 + 2/3·1/4 = 1/2` pro Prüfung (Hammerhead.java:195-197). Effektiv ist das 1/6 pro Tick, einen festen Cooldown gibt es nicht.
    - Außerhalb der Reichweite läuft er per Pfad mit Speed 1.25 zum Ziel (Hammerhead.java:200). Ohne Ziel folgt `setAttacking(0)` (Hammerhead.java:204).
  - Schlag (Hammerhead.java:140-154): vanilla `attackEntityAsMob` mit Schaden `Hammerhead_stats.attack` (Default 75, manifest). Danach Rückstoß mit horizontal 1.1 in Blickrichtung vom Hammerhead weg und vertikal 0.85. Bei Spielern oder schon toten Zielen ist der vertikale Wert verdoppelt auf 1.7 (Hammerhead.java:143-149).
- **Interaktion:** `interact` gibt false zurück (Hammerhead.java:136-138).
- **Drops:** `dropFewItems` ist überschrieben, `getDropItem` (Rindfleisch) wird damit nie benutzt. Jedes Item ist ein eigenes `EntityItem`, verstreut um X/Z ±4 (`rand(5)-rand(5)`) und Y +2 (Hammerhead.java:105). Mengen (Hammerhead.java:109-131):

  | Item | Anzahl |
  |---|---|
  | `experience_bottle` | 8 |
  | `experiencecatcher` (Experience Orb Catcher) | 10 |
  | `creeperlauncher` | 16 |
  | `creeperrepellent` (Block) | 4 |
  | `beef` | 6 |
  | `experiencetree_seed` | 2 |
  | `hammysmall` (Attitude Adjuster) | 1, Chance 1/3 (`rand(3)==1`) |

- **Spawnen:** `getCanSpawnHere` (Hammerhead.java:263-301):
  1. Liegt im Würfel X/Z −3..+2, Y 0..+4 ein Spawner mit Name `"Hammerhead"`, ist der Spawn sofort erlaubt (Hammerhead.java:264-275).
  2. Sonst gelten alle folgenden Bedingungen: gültige Lichtstufe (Hammerhead.java:279), Y ≥ 50 (Hammerhead.java:282), nicht Tag (Hammerhead.java:285), eine 2×2-Säule von Y+1 bis Y+5 komplett Luft (Hammerhead.java:288-297), kein anderer Hammerhead in `expand(16, 8, 16)` (Hammerhead.java:299-300).
  - Keine Biome-Spawns (manifest `spawns` leer). Quelle ist der Challenge-Dungeon-Spawner bei `difficulty == 6` (GenericDungeon.java:742, `addLevelDecorations`).
  - Despawn: `!isNoDespawnRequired()` (Hammerhead.java:53).
- **Zustand:** DataWatcher 20 = „attacking“ 0/1. Im Bytecode wird es als `Byte.valueOf(0)` angelegt und mit `getWatchableObjectByte` gelesen (javap auf `Hammerhead.class`); das Decompilat zeigt fälschlich `int` (Hammerhead.java:49, 255-261). Gelesen wird der Wert von `ModelHammerhead` (ModelHammerhead.java:284). Kein eigenes NBT.
- **Sounds:**
  - Lebend: `orespawn:hammerhead_living` nur mit Chance 1/3 je Aufruf, sonst still (Hammerhead.java:77-82)
  - Treffer: `orespawn:alo_hurt` (Hammerhead.java:85)
  - Tod: `orespawn:hammerhead_death` (Hammerhead.java:89)
  - Lautstärke 1.2, Tonhöhe 0.9 (Hammerhead.java:92-98)
- **Config:** `Hammerhead_health/attack/defense` (240/75/20), `HammerheadEnable` (1), `PlayNicely` (manifest).
- **Portierung 1.21.1:**
  - Alle Werte liegen unter den Klemmen (240 ≤ 1024, Rüstung 20 ≤ 30), `Attributes.ARMOR` direkt setzen.
  - Den Zielsortierer als `Comparator<LivingEntity>` nachbauen. Die Suche gehört in `customServerAiStep()`, nicht in ein Goal, damit die 1/3-Taktung erhalten bleibt.
  - Rückstoß per `target.push(cos*1.1, 0.85|1.7, sin*1.1)` nach `doHurtTarget`. Bei Spielern braucht es zusätzlich `hurtMarked = true`, sonst sieht der Client den Impuls nicht.
  - Kaktus: `source.is(DamageTypes.CACTUS)`.
  - Die Spawner-Ausnahme wird über `checkSpawnRules(level, MobSpawnType spawnType)` abgebildet: bei `MobSpawnType.SPAWNER` true, statt nach Spawnerblöcken zu scannen.
  - `isValidLightLevel` → `Monster.isDarkEnoughToSpawn`.
  - DataWatcher → `EntityDataAccessor<Byte>` (oder `Boolean`).
  - Drops als `dropCustomDeathLoot` mit eigenem Streuversatz. Eine Loot-Table kann die ±4-Streuung nicht nachbilden.
  - Die Hitbox 3×5 braucht eine Navigation für große Mobs; vanilla `GroundPathNavigation` stolpert bei Breite > 1 an Kanten.

### HerculesBeetle - Hercules Beetle (`hercules_beetle`)

- **Rolle:** feindlicher Riesenkäfer, Basis `EntityMob`. Er greift fast **alles** Lebende an, auch Tiere.
- **Werte:**
  - XP 200 (HerculesBeetle.java:30), `fireResistance` 100 (HerculesBeetle.java:31), **feuerimmun** (HerculesBeetle.java:32).
  - `moveSpeed` 0.25 wird in jedem `onUpdate` gesetzt (HerculesBeetle.java:27, 59).
  - Rüstung `HerculesBeetle_stats.defense` (HerculesBeetle.java:67-69), Default 19 (manifest).
  - **Unverwundbarkeitsfenster:** nach jedem angenommenen Treffer 20 Ticks lang `attackEntityFrom → false` (HerculesBeetle.java:316-321); der Zähler sinkt in `updateAITasks` (HerculesBeetle.java:338-340).
  - Immun gegen Kaktusschaden (HerculesBeetle.java:319).
  - **Regeneration:** mit Chance 1/150 pro Tick +2 HP, solange er unter Maximum ist (HerculesBeetle.java:374-376).
  - Sprungverstärkung: `motionY += 0.25` und `posY += 0.5` bei jedem Sprung (HerculesBeetle.java:79-83).
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | (HerculesBeetle.java:34) |
    | 1 | `EntityAIMoveThroughVillage` | 0.9 (HerculesBeetle.java:35) |
    | 2 | `MyEntityAIWanderALot` | Radius 14, 1.0 (HerculesBeetle.java:36) |
    | 3 | `EntityAIWatchClosest` | `EntityPlayer`, 8.0 (HerculesBeetle.java:37) |
    | 4 | `EntityAILookIdle` | (HerculesBeetle.java:38) |
    | Ziel 1 | `EntityAIHurtByTarget` | (HerculesBeetle.java:39) |

  - Wird er von einem `EntityLiving` getroffen, setzt er `setAttackTarget`, `setTarget` und einen Pfad mit Speed 1.2 (HerculesBeetle.java:323-327). **Achtung:** Spieler sind kein `EntityLiving`. Bei Spielertreffern greift nur `EntityAIHurtByTarget`.
  - Zielschleife, mit Chance 1/4 pro Tick (HerculesBeetle.java:341):
    - Das aktuelle `getAttackTarget()` wird verworfen, wenn es tot ist (HerculesBeetle.java:343-346); sonst folgt `findSomethingToAttack()`.
    - Suchbox `expand(16, 6, 16)` mit `GenericTargetSorter` (HerculesBeetle.java:415-416).
    - Gültig (HerculesBeetle.java:379-409): lebend, nicht `MyUtils.isIgnoreable`, sichtbar, kein Creeper, kein HerculesBeetle, kein Creative-Spieler. `isIgnoreable` umfasst RockBase, Ameisen, Schmetterlinge, Mosquito, Dragonfly, Firefly, Cricket, Cockateil, Termite, Ghost, GhostSkelly und Elevator (MyUtils.java:17-19).
    - Nahkampf ab Abstand² < (5 + Zielbreite/2)² (HerculesBeetle.java:352), mit `setAttacking(1)` und Chance `1/3 + 2/3·1/4 = 1/2` (HerculesBeetle.java:354). Beim Schlag spielt der Server am Ziel mit Chance 1/3 `orespawn:scorpion_attack` (Lautstärke 1.4), sonst `orespawn:scorpion_living` (1.0) (HerculesBeetle.java:356-363).
    - Sonst läuft er per Pfad mit 1.2 zum Ziel (HerculesBeetle.java:367). Ohne Ziel folgt `setAttacking(0)` (HerculesBeetle.java:371).
  - Schlag: Schaden `HerculesBeetle_stats.attack` (30, manifest). Rückstoß horizontal 0.45, vertikal `1.25·rand()`, bei Spielern oder toten Zielen `2.5·rand()` (HerculesBeetle.java:299-307).
- **Interaktion:** keine (HerculesBeetle.java:294-296).
- **Drops:** eigenes `dropFewItems`, `getDropItem` bleibt ungenutzt. Die Items werden um X/Z ±4 und Y +1 verstreut (HerculesBeetle.java:116). Mengen (HerculesBeetle.java:123-292):
  - `bighammer` × 1, `item_frame` × 1, `beef` × `4 + rand(8)` = 4-11.
  - Dazu `1 + rand(5)` = 1-5 Würfe auf `rand(20)`. Die Werte 0 und 12-19 geben nichts (9/20). Die übrigen:

  | Wurf | Item | Verzauberungen (je eigener Wurf) |
  |---|---|---|
  | 1 | `diamond` | – |
  | 2 | `diamond_block` | – |
  | 3 | `diamond_sword` | 1/6 Sharpness 1-5; 1/6 Bane of Arthropods 1-5; 1/6 Knockback 1-5; 1/6 Looting 1-5; 1/2 Unbreaking 2-5; 1/6 Fire Aspect 1-5; 1/6 nochmals Sharpness 1-5 (HerculesBeetle.java:141-163) |
  | 4 | `diamond_shovel` | 1/2 Unbreaking 2-5; 1/6 Efficiency 1-5 (HerculesBeetle.java:166-175) |
  | 5 | `diamond_pickaxe` | 1/2 Unbreaking 2-5; 1/6 Efficiency 1-5; 1/6 Fortune 1-5 (HerculesBeetle.java:177-189) |
  | 6 | `diamond_axe` | 1/2 Unbreaking 2-5; 1/6 Efficiency 1-5 (HerculesBeetle.java:191-200) |
  | 7 | `diamond_hoe` | 1/2 Unbreaking 2-5; 1/6 Efficiency 1-5 (HerculesBeetle.java:202-211) |
  | 8 | `diamond_helmet` | je 1/6 Protection, Blast, Fire, Projectile 1-5; 1/2 Unbreaking 2-5; 1/6 Respiration 1-2; 1/6 Aqua Affinity 1-5 (HerculesBeetle.java:213-237) |
  | 9 | `diamond_chestplate` | je 1/6 Protection, Blast, Fire, Projectile 1-5; 1/2 Unbreaking 2-5 (HerculesBeetle.java:239-257) |
  | 10 | `diamond_leggings` | wie Brustpanzer (HerculesBeetle.java:259-277) |
  | 11 | `diamond_boots` | 1/6 Feather Falling **5-9**; 1/2 Unbreaking 2-5 (HerculesBeetle.java:279-288) |

  - Die Stufen liegen teils über dem Vanilla-Maximum (Fire Aspect 5, Aqua Affinity 5, Feather Falling 9). Doppelte Sharpness-Würfe hängen per `addEnchantment` einen zweiten Eintrag an.
- **Spawnen:** `getCanSpawnHere` (HerculesBeetle.java:438-476):
  1. Liegt im Würfel −3..+2 / 0..+4 ein Spawner `"Hercules Beetle"`, ist der Spawn sofort erlaubt (HerculesBeetle.java:439-453).
  2. Sonst gelten alle folgenden Bedingungen: gültige Lichtstufe (HerculesBeetle.java:454), nicht Tag (HerculesBeetle.java:457), Y ≥ 50 (HerculesBeetle.java:460), Luft in einem 4×4-Feld (X/Z −2..+1) von Y+2 bis Y+4 (HerculesBeetle.java:463-472), kein anderer Käfer in `expand(16, 6, 16)` (HerculesBeetle.java:474).
  - Biome in der Oberwelt: siehe manifest (Hills-Biome, `ambient`).
  - Zusätzlich, **nicht im manifest**, als Monster in der Danger Dimension (Gewicht 5, 1-2, BiomeGenUtopianPlains.java:117) und in Chaos (1, 1-1, BiomeGenUtopianPlains.java:386).
  - Spawner in Challenge-Dungeons bei `difficulty` 6, 5 bzw. 4 (GenericDungeon.java:661, 698, 736) sowie in `addLevelDecorationsQ` (GenericDungeon.java:6859, 6896, 6934).
  - Despawn: `!isNoDespawnRequired()` (HerculesBeetle.java:55).
- **Zustand:** DataWatcher 20 = attacking, im Bytecode ein Byte (javap) (HerculesBeetle.java:44, 430-436). `ModelHerculesBeetle` liest es (ModelHerculesBeetle.java:282). `hurt_timer` wird nicht gespeichert.
- **Sounds:**
  - Lebend: keiner (HerculesBeetle.java:90)
  - Treffer: `orespawn:alo_hurt` (HerculesBeetle.java:94)
  - Tod: `orespawn:hercules_death` (HerculesBeetle.java:98)
  - Lautstärke 1.5, Tonhöhe 1.0 (HerculesBeetle.java:101-107)
  - Beim Angriff `scorpion_attack` / `scorpion_living` am Ziel (siehe oben)
- **Config:** `HerculesBeetle_health/attack/defense` (250/30/19), `HerculesBeetleEnable` (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Das Unverwundbarkeitsfenster als eigenes Feld in `hurt()` nachbauen. Vanilla `invulnerableTime` reicht nicht: es verwirft nur kleinere Folgetreffer, statt alles abzuweisen.
  - Sprung: `jumpFromGround()` überschreiben (`addDeltaMovement(0, 0.25, 0)`). Das `posY += 0.5` wird zu `setPos(getX(), getY()+0.5, getZ())`; auf Kollision mit der Decke prüfen.
  - Verzauberungen sind datengetrieben: `level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS)` und dann `stack.enchant(holder, level)`. Stufen über dem Maximum sind erlaubt (≤ 255). **Erst verzaubern, dann `ItemEntity` spawnen:** das Original verändert den Stack erst nach dem Erzeugen des `EntityItem`, und in 1.21.1 wird der Stack beim Spawnen synchronisiert.
  - Doppelte Sharpness: `enchant` überschreibt die Stufe, statt einen zweiten Eintrag anzuhängen. Das ist eine unvermeidliche Abweichung; der höhere Wert sollte gewinnen (`Math.max`).
  - Bei Breite 3.25 gilt dasselbe Navigationsproblem wie beim Hammerhead.

### Hydrolisc - Hydrolisc (`hydrolisc`)

- **Rolle:** zähmbares, amphibisches Haustier. Basis `EntityTameable`, züchtbar, flieht vor Monstern und heilt seinen Besitzer.
- **Werte:**
  - Max-HP fest 100 (Hydrolisc.java:218-220), keine Config.
  - Rüstung fest 10 (Hydrolisc.java:163-165).
  - `attackDamage` 1.0 wird registriert, aber nicht benutzt (Hydrolisc.java:55-56).
  - XP 5 (Hydrolisc.java:43). **offen:** `EntityAnimal.getExperiencePoints` überschreibt das in Vanilla vermutlich.
  - `fireResistance` 100 (Hydrolisc.java:30).
  - **Schadensdeckel:** jeder Treffer höchstens 10 (Hydrolisc.java:357-359).
  - **Fallschaden:** `ceil(Fallhöhe − 3)`, gedeckelt auf 2 (Hydrolisc.java:147-161). Sound `damage.fallbig` ab mehr als 3, sonst `damage.fallsmall`.
  - Auftrieb: im Wasser `motionY += 0.04` pro Tick (Hydrolisc.java:224-226). Atmet unter Wasser (Hydrolisc.java:214-216).
  - Babys werden halb so groß gerendert (RenderHydrolisc.java:35-36).
- **KI und Angriffe:** kein Angriff.
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | (Hydrolisc.java:33) |
    | 1 | `EntityAIMate` | 1.0 (Hydrolisc.java:34) |
    | 2 | `EntityAIAvoidEntity` | `EntityMob`, 8.0, 1.0 / 1.4 (Hydrolisc.java:35) |
    | 3 | `MyEntityAIFollowOwner` | Speed 1.2, maxDist 10, minDist 2 (Hydrolisc.java:36; MyEntityAIFollowOwner.java:22-30) |
    | 4 | `EntityAITempt` | `Items.fish`, 1.25, scheut nicht (Hydrolisc.java:37) |
    | 5 | `EntityAIPanic` | 1.5 (Hydrolisc.java:38) |
    | 6 | `EntityAIWatchClosest` | `EntityPlayer`, 6.0 (Hydrolisc.java:39) |
    | 7 | `MyEntityAIWander` | 1.0, Start mit Chance 1/90, nicht beim Sitzen (Hydrolisc.java:40; MyEntityAIWander.java:23-28) |
    | 8 | `EntityAILookIdle` | (Hydrolisc.java:41) |
    | 9 | `EntityAIMoveIndoors` | (Hydrolisc.java:42) |

  - `MyEntityAIFollowOwner`:
    - Startet, wenn der Abstand² zum Besitzer ≥ 10² ist. Unter Y 60 oder nachts reicht schon > 5² (MyEntityAIFollowOwner.java:38).
    - Folgt, solange der Abstand² > 2² ist (MyEntityAIFollowOwner.java:55).
    - Versucht alle 10 Ticks einen Pfad. Scheitert der bei Abstand² ≥ 144, teleportiert sich das Tier auf einen freien Block im 5×5-Rand um den Besitzer (MyEntityAIFollowOwner.java:72-86).
  - Wassersuche und Heilung in `updateAITick` (Hydrolisc.java:167-208):
    - Mit Chance 1/200 pro Tick wird das Rachziel gelöscht (Hydrolisc.java:172-174).
    - Nicht sitzend und (1/20 bei HP < Max **oder** 1/100 immer): Schalen-Scan nach `water`/`flowing_water` um (X, Y−1, Z) (Hydrolisc.java:175-192). Die Radien sind i = 1, 2, 3, 4, 5, 7, 9, weil ab i ≥ 5 zusätzlich `++i` läuft. Die Höhe ist `min(i, 4)`. Der Scan bricht beim ersten Treffer ab.
    - Mit Fund: Pfad zu (tx, ty−1, tz) mit Speed 1.0 (Hydrolisc.java:194). Steht es schon im Wasser, heilt es 1 HP und spielt `splash` mit Tonhöhe 0.9-1.1 (Hydrolisc.java:195-198).
  - Besitzerheilung: zahm, Chance 1/10 pro Tick, Besitzer unter Max, eigene HP > 20. Dann bekommt der Besitzer +1 und das Tier ruft `heal(-1)` auf sich selbst auf (Hydrolisc.java:201-207). Es überträgt also Leben.
- **Interaktion** (`interact`, Hydrolisc.java:233-313). Zuerst läuft `super.interact` (Zucht mit `crystalapple`, Hydrolisc.java:376-378). Danach:
  1. **Rohfisch** (`Items.fish`, alle Metas) bei Abstand² < 16:
     - Wild: Chance 1/2 zum Zähmen. Erfolg setzt Besitzer, Herz-Effekt (Status 7) und volle Heilung. Misserfolg zeigt Rauch (Status 6) (Hydrolisc.java:243-256).
     - Zahm und Besitzer: volle Heilung (Hydrolisc.java:258-266).
     - Außerhalb von Creative wird der Fisch immer verbraucht, auch bei zahmen Tieren fremder Besitzer (Hydrolisc.java:267-273).
  2. **Dead Bush**, nur Besitzer, zahm, Abstand² < 16: Zähmung aufheben, Besitzer "" setzen, Rauch; der Busch wird verbraucht (Hydrolisc.java:276-291).
  3. **Name Tag**, nur Besitzer: `setCustomNameTag`, das Tag wird verbraucht (Hydrolisc.java:292-302).
  4. Sonst Besitzer mit Abstand² < 16: Sitzen umschalten (Hydrolisc.java:303-311).
  - `isWheat` (Hydrolisc.java:315) ist ein toter Name aus 1.6 und wird in 1.7.10 nicht aufgerufen.
- **Drops:** nur **gezähmt**: `2 + rand(5)` = 2-6 `fish` (Hydrolisc.java:339-348). Wilde Tiere droppen nichts, `getDropItem` bleibt ungenutzt.
- **Spawnen:** manifest (Sumpf, Dschungel, Stone Beach, `creature`). `getCanSpawnHere` ist nicht überschrieben. Despawnt nie (Hydrolisc.java:364-366).
- **Zustand:**
  - DataWatcher: nur vanilla `EntityTameable` (16 = Flags Sitzen/Zahm, 17 = Besitzer-String). `ModelHydrolisc` liest `isSitting()` (ModelHydrolisc.java:295).
  - NBT: nur vanilla (`Owner`, `Sitting`, Age). `closest`/`tx`/`ty`/`tz` sind flüchtig.
  - SRG-Namen, aufgelöst über `joined.srg`: `func_152115_b(String)` = Besitzer-UUID setzen, `func_152114_e(EntityLivingBase)` = „ist Besitzer“.
- **Sounds:**
  - Lebend: keiner
  - Treffer: `orespawn:cryo_hurt`, Tod: `orespawn:cryo_death` (Hydrolisc.java:319-329)
  - Lautstärke 0.4 (Hydrolisc.java:332). Tonhöhe beim Baby 1.5 ± 0.1, sonst 1.0 ± 0.1 (Hydrolisc.java:351)
  - `splash` bei Wasserheilung, `damage.fall*` bei Fall
- **Config:** `HydroliscEnable` (manifest), nur Biome-Spawn.
- **Portierung 1.21.1:**
  - Research nennt HP 50, Angriff 0 und Rüstung 0 (01-mobs.md:788). Der Code sagt 100 / 1.0 / 10, der Code gilt.
  - Basis `TamableAnimal`. `func_152114_e` → `isOwnedBy(LivingEntity)`, `func_152115_b` → `setOwnerUUID`. Zähmen mit `tame(player)`, das auch den Advancement-Trigger auslöst. Effekte über `level().broadcastEntityEvent(this, (byte)7/6)`.
  - Das Zähmitem `Items.fish` entspricht in 1.21.1 `COD`, `SALMON`, `TROPICAL_FISH` und `PUFFERFISH`. `ItemTags.FISHES` enthält auch gegarte Fische, also eine eigene Liste oder ein eigenes Tag verwenden.
  - **`heal(-1)` wird im Port wirkungslos:** NeoForges `LivingEntity.heal` bricht bei Beträgen ≤ 0 nach `EventHooks.onLivingHeal` ab (vermutlich, im NeoForge-sources-Jar prüfen). Deshalb `setHealth(getHealth() - 1)` benutzen.
  - `EntityAIMoveIndoors` hat in 1.21.1 kein Gegenstück, weglassen oder durch ein eigenes Goal ersetzen.
  - `EntityAIAvoidEntity` → `AvoidEntityGoal<>(this, Monster.class, 8, 1.0, 1.4)`.
  - Die Sitz-Pose über `isOrderedToSit()`/`setOrderedToSit()` mit `SitWhenOrderedToGoal`. Das Original hat keinen solchen Goal: `MyEntityAIWander`, `MyEntityAIFollowOwner` und `updateAITick` prüfen `isSitting()` selbst. Die Tempt-, Panic- und Avoid-Goals prüfen es nicht; das beim Nachbau beibehalten.
  - Schadensdeckel in `hurt()`: `Math.min(amount, 10)`. Fallschaden: `causeFallDamage` überschreiben.

### IceBall - IceBall (`ice_ball`)

- **Rolle:** Eisgeschoss, Unterklasse von `LaserBall` mit gesetztem `is_iceball` (IceBall.java:17, 24, 31, 38, 45). Das Item heißt „Ice Ball“ (`iceball`, manifest items).
- **Werte:**
  - Renderer-Iconindex 84 (IceBall.java:15).
  - Schaden, Lebensdauer und Explosion erbt es von LaserBall: 16 Schaden **ohne** Anzünden, 200 Ticks Lebensdauer. Weil `is_iceball` gesetzt ist, gibt es beim Aufprall **immer** eine Explosion mit Stärke 3.0 (LaserBall.java:172-174).
- **KI und Angriffe:** Aufprall (IceBall.java:57-84):
  1. Trifft es ein Royalty-Wesen (ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower, MyUtils.java:9-11), verschwindet es ohne Schaden (IceBall.java:58-61).
  2. Sonst läuft `LaserBall.onImpact`: Drachen mit `getDragonType() != 0` und gerittene Drachen nehmen keinen Schaden, reitende Spieler auch nicht. Robots werden von Eis getroffen.
  3. Mit `icemaker != 0` entstehen 5 Eisblöcke bei `(int)Trefferpunkt + (±0..3)` je Achse (IceBall.java:63-82). Sie **überschreiben jeden Block**.
- **Quellen:**
  - Spieler: `ItemIceBall`, verbraucht 1, Sound `random.bow` 3.0 (ItemIceBall.java:19-25)
  - Dispenser: `MyDispenserBehaviorIceball.java:10`
  - Dragon, als Spezial-Eisball (Dragon.java:752-755, 1118-1121)
  - Mit `setIceMaker(1)`: TheKing (TheKing.java:779-780), ThePrince (ThePrince.java:822-823), ThePrinceAdult (ThePrinceAdult.java:1038-1040, 1451-1452), ThePrincess (ThePrincess.java:926-927), ThePrinceTeen (ThePrinceTeen.java:1061-1063, 1482-1483)
- **Interaktion / Drops / Spawnen:** keine.
- **Zustand:** kein DataWatcher. `icemaker` und `my_index` sind nicht in NBT; nach einem Neuladen ist `icemaker` 0.
- **Sounds:** geerbt, `random.explode` 0.5 (LaserBall.java:171).
- **Config:** keine eigene. Die Explosion folgt der Gamerule `mobGriefing`.
- **Portierung 1.21.1:**
  - Die Eisblöcke **nur serverseitig** setzen. Das Original ruft `setBlock` ohne `isRemote`-Prüfung, also auch auf dem Client mit anderem Zufall (Geisterblöcke).
  - `Blocks.ICE` per `level().setBlockAndUpdate`. Das Überschreiben von Bedrock und Blöcken mit Block Entities (Truhen) entspricht dem Original, ist aber ein Grief-Risiko und sollte `mobGriefing` und `BlockState.getDestroySpeed != -1` beachten; als bewusste Abweichung markieren.
  - Rendering wie bei LaserBall.

### InkSack - InkSack (`ink_sack`)

- **Rolle:** Tintengeschoss der AttackSquid, Basis `EntityThrowable`.
- **Werte:**
  - Schaden 1, gegen Creeper 4 (InkSack.java:51-54), Schadensquelle `thrown`.
  - Mit Chance 1/2 **Blindheit** Stufe 0 für `100 + 50·rand(8)` = 100-450 Ticks (InkSack.java:62-63).
  - Renderer-Iconindex 65 (InkSack.java:18). Rotation +30° pro Tick als Pitch (InkSack.java:77-83).
  - Keine eigene Lebensdauer. Schwerkraft ist vanilla (nicht überschrieben).
- **KI und Angriffe:**
  - Treffer auf WaterDragon oder AttackSquid → `return` **ohne** `setDead` (InkSack.java:55-60). Das Geschoss fliegt weiter bzw. prüft im nächsten Tick erneut, es gibt keinen Rauch und keinen Sound.
  - Sonst Schaden, 4 Rauchpartikel, `random.splash` mit 0.5 und Tonhöhe 1.0 ± 0.5 (InkSack.java:66-69). `setDead` nur serverseitig (InkSack.java:70-72).
  - Blockaufprall: nur Rauch, Sound, Ende.
- **Quellen:** `AttackSquid.watercanon` mit Chance 1/5 × 1/3 pro Aufruf, Heading-Speed 1.4, Streuung 5.0 (AttackSquid.java:545-553).
- **Interaktion / Drops / Spawnen:** keine.
- **Zustand:** kein DataWatcher, kein NBT.
- **Sounds:** `random.splash` beim Aufprall.
- **Config:** keine.
- **Portierung 1.21.1:**
  - `ThrowableProjectile`; Schaden über `damageSources().thrown(this, getOwner())`.
  - Den Immunitätsfall als `canHitEntity(Entity)` → false für WaterDragon/AttackSquid abbilden. Das entspricht dem Durchfliegen und vermeidet Treffer-Schleifen.
  - `MobEffects.BLINDNESS`.
  - Partikel über `level().broadcastEntityEvent` oder `ServerLevel.sendParticles`, weil `onHit` im Port nur serverseitig laufen soll.
  - Renderer: ein Sprite aus `spinners.png` (256×256, Index 65) im Client-Paket; `gl_scale` 0.5 laut manifest.

### Irukandji - Irukandji (`irukandji`)

- **Rolle:** winzige, tödliche Qualle (0.25 × 0.25, manifest). Basis `EntityMob`. Sie jagt nur Spieler und stirbt außerhalb von Wasser langsam.
- **Werte:**
  - XP 50 (Irukandji.java:35), `fireResistance` 1 (Irukandji.java:36).
  - `moveSpeed` 0.15 (Irukandji.java:28). Rüstung `Irukandji_stats.defense` (Irukandji.java:75-77).
  - `getAttackStrength` = 2 (Irukandji.java:87-90) ist ein toter Legacy-Wert. Der echte Schaden ist `Irukandji_stats.attack` (20, manifest).
  - **Berührungsgift:**
    - Rechtsklick mit leerer Hand: der Spieler bekommt 200 Mob-Schaden (Irukandji.java:119-124).
    - Schlag mit leerer Hand: 200 Schaden am Spieler; die Qualle nimmt keinen Schaden und `attackEntityFrom` gibt false zurück (Irukandji.java:132-138).
    - Das ist normaler Mob-Schaden, die Rüstung dämpft ihn.
  - Andere Irukandji können sie nicht verletzen (Irukandji.java:140-142).
  - **Austrocknen:** außerhalb von Wasser, mit Chance 1/10 pro Tick, sucht sie Wasser. Findet sie keines, verliert sie mit Chance 1/25 1 HP per `heal(-1)`. Bei HP ≤ 0 folgt `setDead()`, ohne Tod und ohne Drop (Irukandji.java:240-270).
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | (Irukandji.java:39) |
    | 1 | `MyEntityAIWander` | 1.0 (Irukandji.java:40) |
    | 2 | `EntityAIWatchClosest` | `EntityPlayer`, 8.0 (Irukandji.java:41) |
    | 3 | `EntityAILookIdle` | (Irukandji.java:42) |
    | Ziel 1 | `EntityAIHurtByTarget` | (Irukandji.java:43) |

  - `setAvoidsWater(false)` (Irukandji.java:34).
  - Wassersuche: Schalen-Scan wie beim Hydrolisc, i = 1..11 (ab 5 in Zweierschritten), Höhe `min(i, 5)` (Irukandji.java:246-257). Pfad mit Speed 1.33 (Irukandji.java:259).
  - Wird sie von einem `EntityLiving` getroffen, folgen Ziel und Pfad mit 1.2 (Irukandji.java:139-147).
  - Zielschleife, mit Chance 1/8 pro Tick (Irukandji.java:271):
    - `findSomethingToAttack()` behält ein lebendes `getAttackTarget()` (Irukandji.java:319-322), sonst sucht es in `expand(6, 4, 6)` (Irukandji.java:314).
    - Gültig sind **nur** sichtbare Spieler außerhalb von Creative (Irukandji.java:290-308).
    - Angriff ab Abstand² < 3.0 mit `setAttacking(1)` und Chance `1/4 + 3/4·1/5 = 2/5` (Irukandji.java:274-278). Sonst Pfad mit 1.2 (Irukandji.java:281).
- **Interaktion:** siehe Berührungsgift. `interact` gibt immer false zurück.
- **Drops:** `deadirukandji` (Dead Irukandji) über das vanilla `dropFewItems` (Irukandji.java:112-114).
- **Spawnen:**
  - `getCanSpawnHere`: Y ≥ 50 **und Tag** und Chance 1/60 und höchstens 2 Irukandji in `expand(16, 8, 16)` (Irukandji.java:342-349).
  - Natürlich, nicht im manifest: *water creature* in der Crystal Dimension, Gewicht 4, Gruppe 2-3 (BiomeGenUtopianPlains.java:176).
  - Weltgen-Spawner `"Irukandji"` in der Crystal Dimension (`DimensionID5`) (OreSpawnWorld.java:176, 183):
    - nur wenn `IrukandjiEnable != 0` (OreSpawnWorld.java:1758)
    - Chance 1/80 pro Chunk, 3 Versuche (OreSpawnWorld.java:1761-1764)
    - Y von 100 abwärts bis 51: der erste Luftblock direkt über `water` (OreSpawnWorld.java:1767-1772)
    - Weil der Spawner `getCanSpawnHere` fragt, spawnt er nur tagsüber.
  - Despawn: `!isNoDespawnRequired()` (Irukandji.java:59).
- **Zustand:** DataWatcher 20 = attacking, Byte im Bytecode (javap) (Irukandji.java:55, 334-340). `ModelIrukandji` liest es nicht (grep). Kein NBT.
- **Sounds:**
  - Lebend: keiner
  - Treffer: `orespawn:little_splt` (Irukandji.java:97). **Tippfehler**, der Ton existiert in `sounds.json` nicht (sounds_dump.txt:130 führt ihn als unaufgelöst). Im Original ist der Treffer also stumm.
  - Tod: `orespawn:ratdead` (Irukandji.java:101)
  - Lautstärke 0.25, Tonhöhe 2.0 (Irukandji.java:104-110)
- **Config:** `Irukandji_health/attack/defense` (1/20/0), `IrukandjiEnable` (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Die Leere-Hand-Prüfung: `player.getMainHandItem().isEmpty()`, und zwar in `mobInteract` **und** in `hurt()`.
  - `heal(-1)` durch `setHealth(getHealth()-1)` ersetzen (siehe Hydrolisc). Den Tod durch Austrocknen als `discard()` abbilden, weil das Original `setDead` ohne Drop benutzt.
  - Den vergessenen Hurt-Sound bewusst stumm lassen (1:1) oder auf `little_splat` korrigieren; die Wahl als Abweichung notieren.
  - Tagesabhängige Spawns in `checkSpawnRules` mit `level.isDay()` (auch für `MobSpawnType.SPAWNER`).
  - Die Wasser-Spawn-Kategorie ist `WATER_CREATURE`. SpawnPlacement `IN_WATER` passt nicht, weil die Weltgen-Spawner auf dem Wasser stehen; `NO_RESTRICTIONS` mit eigener Prüfung verwenden.

### IrukandjiArrow - (kein Lang-Name) (`irukandji_arrow`)

- **Rolle:** Pfeil der Skate Bow mit festem Grundschaden 100. Basis `EntityArrow`, aber `onUpdate` ist eine vollständige Kopie der Vanilla-Pfeilphysik mit **eigenen**, privaten Feldern (`xTile`, `inGround`, `ticksInAir` usw., IrukandjiArrow.java:17-25).
- **Werte:**
  - Schaden 100 unabhängig vom Tempo (IrukandjiArrow.java:188). `setDamage` ist leer, `getDamage` = 100 (IrukandjiArrow.java:311-316).
  - Kritisch: `+ rand(100/2 + 2)` = +0..51 (IrukandjiArrow.java:205-207).
  - Brennt der Pfeil, zündet er das Ziel für 5 s an (IrukandjiArrow.java:215-217).
  - Punch: horizontaler Rückstoß `Stärke · 0.6`, vertikal 0.1 (IrukandjiArrow.java:224-229).
  - Physik: Luftwiderstand 0.99, im Wasser 0.8 mit 4 Blasen; Schwerkraft 0.05 (IrukandjiArrow.java:289-301).
  - Treffer-Box: der Weg um 1.0 erweitert, Ziel-AABB um 0.3 (IrukandjiArrow.java:148, 171). Der Schütze ist die ersten 5 Ticks immun (IrukandjiArrow.java:152).
  - Im Boden: nach 50 Ticks droppt der Pfeil ein `irukandjiarrow`-Item und verschwindet (IrukandjiArrow.java:121-126). Löst sich der Block, fällt er weiter (IrukandjiArrow.java:128-135). `arrowShake` = 7 beim Einschlag (IrukandjiArrow.java:260).
- **KI und Angriffe:**
  - Ausgenommen sind `Elevator` sowie gerittene `Cephadrome`, `Dragon` und `EntityHorse` (IrukandjiArrow.java:152-170).
  - PvP-Schutz bei `ultimate_sword_pvp == 0` (IrukandjiArrow.java:189-204): Spieler, Girlfriend, Boyfriend und gezähmte `EntityTameable` nehmen keinen Schaden. Der Pfeil macht nur `random.bowhit` und verschwindet.
  - Wird der Treffer abgewiesen, prallt der Pfeil mit Faktor −0.1 ab und dreht um 180° (IrukandjiArrow.java:237-244).
  - Bei Treffer: `setArrowCountInEntity + 1` (IrukandjiArrow.java:222). Paket `S2BPacketChangeGameState(6)` an den schießenden Spieler bei Spielertreffer (IrukandjiArrow.java:230-232).
- **Quellen:**
  - `SkateBow`: Spannkraft `f = (t² + 2t)/3` mit t = Ticks/20, abgebrochen unter 0.1, gedeckelt auf 1.75 (SkateBow.java:30-37). Kritisch mit Chance 1/20 (SkateBow.java:39-41). Punch und Flame werden übernommen.
  - Dispenser (MyDispenserBehaviorArrow.java:10).
- **Interaktion:** keine eigene. Ein Aufheben durch Spieler ist nicht möglich: das geerbte `onCollideWithPlayer` liest das private `inGround` der Oberklasse, das hier nie gesetzt wird. **offen:** das folgt aus vanilla `EntityArrow` und ist nicht im OreSpawn-Code belegt.
- **Drops:** `irukandjiarrow` × 1 nach 50 Ticks im Boden.
- **Spawnen:** nur als Geschoss. Registriert **nur** per `registerGlobalEntityID` (OreSpawnMain.java:5020), ohne `registerModEntity`, deshalb steht `tracking` im manifest auf null.
- **Zustand:** DataWatcher 16 = Crit-Flag als Byte (Bytecode `Byte.valueOf`, javap). `entityInit` ruft `super.entityInit()` **nicht** auf (IrukandjiArrow.java:71-73). Die eigenen Felder werden nicht gespeichert, das geerbte NBT schreibt die leeren Oberklassenfelder. Nach dem Neuladen fällt ein steckender Pfeil deshalb wieder.
- **Sounds:** `random.bowhit` bei Treffer, Abweisung und Blockeinschlag (IrukandjiArrow.java:192, 199, 234, 258).
- **Config:** `UltimateSwordPvp` → `ultimate_sword_pvp` (Default 0, manifest).
- **Portierung 1.21.1:**
  - `AbstractArrow` als Basis. **Wichtig:** vanilla `onHitEntity` rechnet `ceil(clamp(velocity · baseDamage))`. Das Original ist aber tempounabhängig, also `onHitEntity` komplett überschreiben.
  - Die Kopie der Physik ist unnötig; die vanilla-Physik von `AbstractArrow` benutzen (Schwerkraft 0.05, Drag 0.99/0.6 statt 0.8 im Wasser; die Wasserabweichung als Kleinigkeit notieren).
  - Pickup: `pickup = Pickup.DISALLOWED` plus eigener 50-Tick-Drop in `tick()`, solange `inGround`. `getDefaultPickupItem()` muss trotzdem einen Stack liefern.
  - PvP-Regel und Ausnahmen in `canHitEntity`.
  - Registrierung wie ein vanilla-Pfeil: `clientTrackingRange(4)`, `updateInterval(20)`.
  - Renderer: `ArrowRenderer`-Unterklasse. **offen:** Textur, der manifest nennt keinen Renderer.

### Island - Light Floating Island (`island`)

- **Rolle:** unsichtbarer Motor einer **runden** schwebenden Insel („Light Floating Island“, OreSpawnMain.java:3636). Basis `EntityAnimal` ohne KI und ohne Physik. Das Entity baut die Insel zur Laufzeit aus Blöcken und verschiebt sie. Gerendert wird nur `ModelIsland` (manifest).
- **Werte:**
  - Keine eigenen Attribute, also Vanilla-Default-HP. **offen:** Vanilla-Grundwert von `maxHealth`, im OreSpawn-Code nicht gesetzt.
  - Pro Tick wird die Bewegung auf 0 gesetzt (Island.java:45-48). `onLivingUpdate` läuft nur clientseitig (Island.java:93-97), KI und Fall sind leer (Island.java:99-106).
  - Erstbau beim ersten Servertick (`just_spawned`, Island.java:58-76):
    - Richtung `dir` = rand · π mit zufälligem Vorzeichen (Island.java:59-62).
    - Mit Chance 39/40 (`rand(40) != 1`): Radius 3-6, Tiefe 2-4, Tempo `rand()/50 · IslandSpeedFactor` (Island.java:63-66).
    - Sonst: Radius 6-10, Tiefe 3-6, Tempo `rand()/200 · IslandSpeedFactor` (Island.java:68-71).
  - Update alle `timer` = 73 Ticks (Island.java:34, 77-81), Startversatz `rand(50)`.
  - Richtungswechsel: Countdown `rand(10000)` nach dem Bau, danach jeweils `rand(5000)` Ticks, dann neue Zufallsrichtung (Island.java:75, 82-89).
- **KI und Angriffe (Blockbau):**
  - `create_island` (Island.java:134-193): pro Schicht i = 0..Tiefe−1 eine Scheibe mit Radius `radius/(i+1)`. Sie wird in Winkelschritten 0.1047 rad (6°) und Radialschritten 0.35 ab 0.75 abgefahren (Island.java:135-146).
    - Oberste Schicht (i = 0) auf Y+1, nur in Luft: Chance 1/5000 `lava`, sonst `mycelium` (Island.java:154-159). Darauf mit Chance 1/20 ein brauner oder roter Pilz (Island.java:160-166). Trifft die Schicht auf `bedrock`, folgt `setDead` (Island.java:170-173).
    - Tiefere Schichten auf Y−i+1: Chance 1/10 `diamond_ore`, sonst `end_stone`. Das **überschreibt jeden Block** (Island.java:175-180).
    - Der Block an der Entity-Position wird Luft (Island.java:185-192).
  - `update_island` (Island.java:195-336):
    - `myX/myZ += speed · cos/sin(dir)` (Island.java:203-204).
    - Wechselt die Blockzelle: den äußeren Ring der alten Scheiben entfernen, samt Pilzen darauf (Island.java:210-237). An die alte Mitte kommt `end_stone` (Island.java:244). Die Position springt auf die neue Zellmitte (Island.java:245-258).
    - Danach wird der äußere Ring neu gesetzt (Island.java:259-316). In tieferen Schichten löst ein `stone`-Block eine **Explosion der Stärke 5.0 mit Blockzerstörung** aus (Island.java:299-304). Die neue Mitte wird Luft (Island.java:317-326).
    - Triffid-Spawn mit Chance `1/(2 + 2000/73)` = 1/29 pro Update, wenn in ±10/±5/±10 keiner steht. Er erscheint auf Y+2.01 und spielt seinen Lebend-Sound (Island.java:328-335, 338-347).
- **Interaktion:** keine. Das Entity ist angreifbar, weil `attackEntityFrom` nicht überschrieben ist.
- **Drops:** `island` (Island Block) über das vanilla `dropFewItems` (Island.java:349-351).
- **Spawnen:**
  - Nur durch `IslandBlock.updateTick` (IslandBlock.java:35-70):
    - 1-3 Versuche (IslandBlock.java:40). Höhe `12 + rand(m)` mit m = 64, bei `IslandSizeFactor` 2 → 55, bei 1 → 45 (IslandBlock.java:41-49).
    - Die Zielhöhe braucht eine 21×21-Fläche aus Luft (IslandBlock.java:51-59).
    - Mit Chance 1/25 entsteht `Island`, sonst `IslandToo` (IslandBlock.java:61-66). Danach werden der Block und der Block darüber Luft (IslandBlock.java:69-70).
  - Island Blocks setzt `addIslands` in der Danger Dimension (OreSpawnWorld.java:119, 172).
  - Despawnt nie (Island.java:108-110).
- **Zustand:** NBT `JustSpawned` (int), `Idepth` (int), `Iradius` (int), `Ispeed` (float), `Idir` (float) (Island.java:112-128). `myX/myY/myZ` werden nicht gespeichert: `once = 1` setzt sie beim Laden auf die Position (Island.java:52-57). Kein DataWatcher.
- **Sounds:** keine eigenen. Die Explosion hat den Vanilla-Sound.
- **Config:**
  - `IslandSpeedFactor` und `IslandSizeFactor` (Default 2, manifest), geklemmt auf 1..5 (OreSpawnMain.java:1203-1214).
  - Bei `LessLag == 1` höchstens 2, bei `LessLag == 2` höchstens 1 (OreSpawnMain.java:1227-1242). `LessLag` hat Default 0 (manifest).
  - Island liest selbst nur `IslandSpeedFactor`; `IslandSizeFactor` wirkt über `IslandBlock`.
- **Portierung 1.21.1:**
  - Die Guardrail „Strukturen als Structure-Pieces“ **passt hier nicht**: die Insel bewegt sich zur Laufzeit und muss ein tickendes Entity bleiben.
  - Basis: `Mob` mit `setNoAi(true)` und `noPhysics = true`, oder ein schlankes `Entity` mit eigener Lebensenergie; Hauptsache angreifbar mit Drop.
  - `OreSpawnMain.setBlockFast` (Flags 3, schreibt direkt in die Chunk-Section, OreSpawnMain.java:5499-5523) → `level().setBlock(pos, state, Block.UPDATE_CLIENTS)`. Nachbar-Updates sparen, sonst läuft Lava sofort.
  - Die Explosion folgt im Original **nicht** `mobGriefing` (`createExplosion(..., true)`) → `Level.ExplosionInteraction.TNT`.
  - Das Entity tickt nur in geladenen Chunks. Die Randblöcke reichen bis Radius 10 und können in ungeladene Nachbarchunks fallen; `level().hasChunkAt(pos)` prüfen, sonst lädt `setBlock` Chunks synchron.
  - Triffid über `OreSpawnEntities.TRIFFID.get().spawn(...)` statt über den Namen.
  - Research beschreibt die Insel mit Bedrock (02-dimensions-worldgen.md:220). Laut Code wird Bedrock nie gesetzt, er ist nur Abbruchbedingung.

### IslandToo - Dark Floating Island (`island_too`)

- **Rolle:** Motor einer **quadratischen** Grasinsel mit Erzkern („Dark Floating Island“, OreSpawnMain.java:3644). Basis `EntityAnimal`, sonst wie Island. Sie macht 24 von 25 Inselspawns aus (IslandBlock.java:61-66).
- **Werte:**
  - Erstbau (IslandToo.java:62-85), Richtung `dir` = `rand(4)`: 0 → −Z, 1 → +Z, 2 → +X, 3 → −X (IslandToo.java:63, 288-299).
  - Mit Chance 39/40:
    - Breite = Länge = `1 + rand(5·IslandSizeFactor)`, Tiefe 1-4, Tempo `rand()/40 · IslandSpeedFactor` (IslandToo.java:65-68).
    - Das Tempo verdoppelt sich bei Volumen `Länge·Breite·Tiefe` ≤ 64 und nochmals bei ≤ 32 (IslandToo.java:69-74).
  - Sonst: Breite `5 + rand(8·IslandSizeFactor)`, Tiefe 3-8, Tempo `rand()/150 · IslandSpeedFactor` (IslandToo.java:77-80).
  - Update alle `timer` = 42 Ticks (IslandToo.java:36, 86-90). Richtungswechsel alle `rand(5000)` Ticks (IslandToo.java:44, 84, 91-95).
  - `attackEntityFrom` rastet die Position auf die Zellmitte, ruft `super` und gibt **immer false** zurück (IslandToo.java:144-170). Schaden kommt trotzdem an.
- **KI und Angriffe (Blockbau):**
  - Aufbau (IslandToo.java:172-225): Schichten k = 0..Tiefe auf Y+k. Die halbe Kantenlänge ist `Länge/(Tiefe−k+1)`, mindestens 1 (IslandToo.java:182-185). Die Insel ist also oben am breitesten (umgedrehte Pyramide).
    - Oberste Schicht (k = Tiefe), nur in Luft: Chance 1/5000 `water`, sonst `grass` (IslandToo.java:192-197). Darauf mit Chance 1/30 `flower_pink` oder `flower_blue` (IslandToo.java:198-206), sonst mit Chance 1/100 `Trees.SmallTree` (IslandToo.java:208-209). Trifft sie auf `bedrock`, folgt `setDead` (IslandToo.java:213-216).
    - Tiefere Schichten: `mySetBlock` (IslandToo.java:227-283). Die Erzart `blocktype` wird einmal pro Insel mit 1-8 gewürfelt:

      | blocktype | Erz | Chance je Block |
      |---|---|---|
      | 1 | `coal_ore` | 1/5 |
      | 2 | `iron_ore` | 1/10 |
      | 3 | `emerald_ore` | 1/20 |
      | 4 | `oretitanium` | 1/30 |
      | 5 | `oreuranium` | 1/30 |
      | 6 | `oreruby` | 1/30 |
      | 7 | `oreamethyst` | 1/30 |
      | 8 | `gold_ore` | 1/20 |

    - Ist es sonst `stone`, gibt es acht unabhängige Würfe `rand(3000) == 1..8`. Sie ergeben der Reihe nach `blockenderpearl`, `blockeyeofender`, `blockamethyst`, `blockruby`, `blockuranium`, `blocktitanium`, `gold_block`, `diamond_block`; ein späterer Treffer überschreibt einen früheren (IslandToo.java:256-281).
  - Bewegung (IslandToo.java:285-476), wenn die Zelle wechselt:
    - Die hintere Kante aller Schichten wird Luft. In der obersten Schicht verschwinden dabei auch Blumen (pink, blau, schwarz, scary), Wasser und bis zu drei `skytreelog` darüber (IslandToo.java:339-375).
    - An die alte Mitte kommt `mySetBlock` (IslandToo.java:376). Die Position springt (IslandToo.java:377-390).
    - Dann wird die vordere Kante gesetzt. Trifft eine tiefere Schicht dabei auf `end_stone` (etwa eine Light Island), gibt es eine **Explosion der Stärke 5.0 mit Blockzerstörung** (IslandToo.java:461-466).
    - Die Mitte wird Luft (IslandToo.java:422, 474).
  - Kein Triffid-Spawn.
- **Interaktion:** keine.
- **Drops:** `island` über das vanilla `dropFewItems` (IslandToo.java:478-480).
- **Spawnen:** wie Island, mit Chance 24/25 aus `IslandBlock`. Despawnt nie (IslandToo.java:114-116).
- **Zustand:** NBT `JustSpawned`, `Iwidth`, `Idepth`, `Ilength`, `Ispeed` (float), `Idir` (int), `Iblocktype` (IslandToo.java:118-138). Kein DataWatcher.
- **Sounds:** keine eigenen.
- **Config:** `IslandSpeedFactor` und `IslandSizeFactor` (siehe Island), beide wirken direkt.
- **Portierung 1.21.1:**
  - Alle Punkte von Island gelten.
  - `Trees.SmallTree` ist eine OreSpawn-Klasse (`OreSpawnMain.OreSpawnTrees`, OreSpawnMain.java:195) und muss vorher portiert sein, oder als konfigurierter Feature-Aufruf (`ConfiguredFeature.place`) nachgebaut werden.
  - Hinter jedem Erzblock steht eine Portabhängigkeit (Ruby, Amethyst, Titanium, Uranium, Ender-Pearl-Block, Eye-of-Ender-Block).
  - Die Breite wächst mit `IslandSizeFactor` 5 bis `5 + 8·5`, also auf bis zu 44 Blöcke Halbkante. Das sprengt Chunkgrenzen massiv; Performance-Budget pro Tick einplanen.
  - Das Rückgabe-false in `hurt()` 1:1 übernehmen: kein Rückstoß, keine Rot-Flash-Animation.

### Kyuubi - Kyuubi (`kyuubi`)

- **Rolle:** feindlicher Feuerfuchs im Nether, Basis `EntityMob`. Er greift **nur Nicht-Monster** an, und zwar ausschließlich mit Feuerbällen.
- **Werte:**
  - XP 30 (Kyuubi.java:25), `fireResistance` 1000 (Kyuubi.java:26), feuerimmun (Kyuubi.java:27).
  - `moveSpeed` 0.25 (Kyuubi.java:22). Rüstung `Kyuubi_stats.defense` (Kyuubi.java:62-64).
  - `getAttackStrength` = 3 (Kyuubi.java:86-88) ist ein toter Legacy-Wert.
  - Mit Chance 1/10 pro Tick (Kyuubi.java:72-83):
    - Partikel `reddust` und `lava` auf Y+2.
    - `setFire(5)` an sich selbst. Das hat wegen der Feuerimmunität keine sichtbare Wirkung.
    - **Im Wasser:** `attackEntityAsMob(this)`, also ein Schlag mit dem eigenen Angriffswert (`Kyuubi_stats.attack`, 10, manifest) gegen sich selbst, plus Rauchpartikel.
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | (Kyuubi.java:28) |
    | 1 | `EntityAIPanic` | 1.35 (Kyuubi.java:29) |
    | 2 | `EntityAIMoveThroughVillage` | 1.0 (Kyuubi.java:30) |
    | 3 | `EntityAIWander` (vanilla) | 1.0 (Kyuubi.java:31) |
    | 4 | `EntityAIWatchClosest` | `EntityPlayer`, 10.0 (Kyuubi.java:32) |
    | 5 | `EntityAILookIdle` | (Kyuubi.java:33) |
    | Ziel 1 | `EntityAIHurtByTarget` | (Kyuubi.java:34) |

  - Mit Chance 1/200 pro Tick wird das Rachziel gelöscht (Kyuubi.java:135-137).
  - Zielschleife, mit Chance 1/10 pro Tick (Kyuubi.java:139):
    - Suchbox `expand(12, 4, 12)` (Kyuubi.java:190).
    - Gültig (Kyuubi.java:154-184): lebend, nicht ignorierbar, sichtbar, **kein** `EntityMob`, kein `EntityPigZombie`, kein Creative-Spieler. Also Spieler, Tiere, Dorfbewohner und OreSpawn-Nicht-Monster.
    - Er schaut das Ziel an und läuft per Pfad mit 1.25 hin (Kyuubi.java:142-143).
    - Ab Abstand² < 64 schießt er mit Chance `1/6 + 5/6·1/8` ≈ 0.27 pro Prüfung einen vanilla `EntitySmallFireball` (Kyuubi.java:144-149). Start auf Y+1.25, Ziel Y+0.75, Sound `random.bow` 0.75 mit Tonhöhe `1/(0.8..1.2)`.
    - Der Feuerballschaden ist vanilla, nicht im OreSpawn-Code festgelegt.
  - Es gibt keinen Nahkampf-Task. Ein Schlag kommt nur über `attackEntityAsMob` im Wasser gegen sich selbst vor.
- **Interaktion:** keine (Kyuubi.java:127-129).
- **Drops:** eigenes `dropFewItems` (Kyuubi.java:214-224): `coal` × 10, `redstone_block` × 3, `quartz_block` × 4, verstreut um X/Z ±3 (`rand(4)-rand(4)`) und Y+1 (Kyuubi.java:210). `getDropItem` (Gold-, Uran- oder Titannugget, Kyuubi.java:110-122) ist damit **toter Code**.
- **Spawnen:**
  - `getCanSpawnHere` ist immer true, ohne Lichtprüfung (Kyuubi.java:205-207).
  - Biome: manifest (hell, Gewicht 10, 1-1).
  - Spawner `"Kyuubi"`: `GenericDungeon.makeDungeon` bei Zufallstyp 4 (GenericDungeon.java:187) und `addkyuubi` im Kyuubi-Dungeon (GenericDungeon.java:1245, 1294).
  - Despawn: `!isNoDespawnRequired()` (Kyuubi.java:50).
- **Zustand:** kein eigener DataWatcher (Kyuubi.java:38-40), kein NBT.
- **Sounds:**
  - Lebend: `orespawn:kyuubi_living`, Treffer: `orespawn:alo_hurt`, Tod: `orespawn:alo_death` (Kyuubi.java:90-100)
  - Lautstärke 0.75, Tonhöhe 1.0 (Kyuubi.java:102-108)
  - `random.bow` beim Schuss
- **Config:** `Kyuubi_health/attack/defense` (125/10/10), `KyuubiEnable` (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Feuerball: `new SmallFireball(level(), this, new Vec3(dx, dy, dz))` mit `setPos(getX(), getY()+1.25, getZ())`.
  - `EntityAIPanic` greift in 1.21.1 auch bei `isFreezing` und bei Schaden. Weil `fireImmune()` gilt, löst Feuer keine Panik aus, wie im Original.
  - Selbstschaden im Wasser: `doHurtTarget(this)` benutzt in 1.21.1 `damageSources().mobAttack(this)`. Möglich, aber `Mob.doHurtTarget` wendet dabei auch Rückstoß auf sich selbst an. Klarer ist `hurt(damageSources().mobAttack(this), (float)getAttributeValue(ATTACK_DAMAGE))`; als 1:1-äquivalent markieren.
  - Partikel nur clientseitig in `tick()` bei `level().isClientSide`. Im Original erzeugt der Server keine sichtbaren Partikel.
  - `setFire(5)` → `igniteForSeconds(5)`, optisch wirkungslos. Man kann es weglassen, sollte das aber notieren.

### LaserBall - LaserBall (`laser_ball`)

- **Rolle:** Energiegeschoss („Robot Laser Charge“ als Item `laserball`, manifest items). Basis `EntityThrowable`. Oberklasse von `IceBall`, `Acid` und `DeadIrukandji`, die sich über Flags unterscheiden (LaserBall.java:78-93).
- **Werte:**
  - Lebensdauer 200 Ticks, gezählt **vor** `super.onUpdate` (LaserBall.java:180-184). Rotation +50° pro Tick als Pitch (LaserBall.java:186-192).
  - Renderer-Iconindex 81 (LaserBall.java:22). Schwerkraft vanilla (nicht überschrieben).
  - Aufprall auf ein Entity, Schadensquelle `thrown` (LaserBall.java:95-157):

    | Modus | Schaden | Sonderregeln |
    |---|---|---|
    | normal | 16 (LaserBall.java:97, 153), dazu `setFire(1)` (LaserBall.java:154-156) | Robot2-5, GiantRobot, gerittene Dragons und reitende Spieler sind immun |
    | `is_special` | 16 plus Feuer | wie normal; beim Aufprall zusätzlich Explosion (siehe unten) |
    | `is_iceball` | 16, **ohne** Feuer | Robots nicht immun; Dragons mit `getDragonType() != 0` immun (LaserBall.java:141-144) |
    | `is_acid` (`Acid`) | 16 plus Feuer | TrooperBug und SpitBug immun (LaserBall.java:103-112); Robots, Dragons und reitende Spieler **nicht** immun; keine Partikel, keine Explosion |
    | `is_irukandji` (`DeadIrukandji`) | **100**, sonst nichts (LaserBall.java:98-102) | trifft er einen Block, droppt er serverseitig `deadirukandji` (LaserBall.java:158-160) |

  - Aufprallwirkung ohne Säure (LaserBall.java:161-175): 10 Partikelrunden (special: 20) aus `smoke`, `largesmoke` und `fireworksSpark`, dazu `random.explode` mit 0.5. Mit `is_special` **oder** `is_iceball` folgt serverseitig eine Explosion der Stärke 3.0, Blockschaden nur bei Gamerule `mobGriefing` (LaserBall.java:172-174).
  - Flugspur ohne Säure (LaserBall.java:196-208): 4 `fireworksSpark` pro Tick (special: 10, Eis ohne special: 2), dazu `reddust`, außer bei Eis.
- **Quellen:**
  - `ItemLaserBall`: verbraucht 1, Sound `fireworks.launch` 3.0 (ItemLaserBall.java:19-25)
  - `ItemRayGun`: special, Tempo ×3, Versatz 1.0 seitlich / 1.55 hoch, Rückstoß am Spieler 1.5 / 0.3, 1 Haltbarkeit von 50 (ItemRayGun.java:15-40)
  - Robot3 (Robot3.java:247), Robot4 (Robot4.java:291, special 299), Robot5 (Robot5.java:219)
  - GiantRobot, special bei Abstand² > 100 (GiantRobot.java:248-256)
  - Dispenser (MyDispenserBehaviorLaserball.java:10)
- **Interaktion / Drops / Spawnen:** keine; der Block-Drop gilt nur für DeadIrukandji.
- **Zustand:** kein DataWatcher. Die Flags werden nicht gespeichert, nach dem Neuladen wird aus jeder Unterart ein normaler LaserBall; nur IceBall setzt sein Flag im Konstruktor neu. `ticksalive` ist ebenfalls flüchtig.
- **Sounds:** `random.explode` beim Aufprall. Der Abschuss-Sound kommt vom Werfer.
- **Config:** keine eigene. `mobGriefing` (Gamerule).
- **Portierung 1.21.1:**
  - `ThrowableProjectile` mit `getDefaultGravity()` = vanilla; Flags als Felder, **plus** `addAdditionalSaveData`, sonst geht beim Chunk-Neuladen der Modus verloren (bewusste Verbesserung, notieren).
  - `onHit` nur serverseitig ausführen. Das Original ruft `onImpact` auf beiden Seiten; Partikel über `ServerLevel.sendParticles`.
  - Explosion: `level().explode(this, x, y, z, 3.0f, Level.ExplosionInteraction.MOB)`; `MOB` beachtet `mobGriefing` selbst.
  - Reiten: `getVehicle() != null` beim Spieler, `isVehicle()` beim Dragon.
  - Die Immunitätsprüfungen `instanceof Robot2..5/GiantRobot/Dragon/TrooperBug/SpitBug` brauchen die portierten Klassen, alternativ Entity-Type-Tags (`orespawn:laser_immune`).
  - Renderer (Client): Sprite aus `spinners.png`, Index 81, Skalierung 0.5 (manifest).

### LeafMonster - Leaf Monster (`leaf_monster`)

- **Rolle:** Hinterhalt-Monster, das sich als Laub- und Holzblock tarnt. Basis `EntityMob`. Solange es nicht angreift, rastet es auf die Blockmitte und auf 90°-Blickwinkel ein.
- **Werte:**
  - XP 5 (LeafMonster.java:26), `fireResistance` 0 (LeafMonster.java:27), `moveSpeed` 0.25 (LeafMonster.java:23).
  - Rüstung `LeafMonster_stats.defense` (LeafMonster.java:58-60).
  - **Fallschaden:** `ceil(Fallhöhe − 3)`, gedeckelt auf 2 (LeafMonster.java:66-78). Sound `damage.fallbig` ab mehr als 2.
  - **Tarnung** in `onUpdate`, wenn `getAttacking() == 0` (LeafMonster.java:83-108):
    - Die Position wird auf ganze Blöcke abgeschnitten, dann ±0.5 je nach Vorzeichen. Randfall: bei X oder Z in (−1, 1) bleibt sie auf 0.0, weil der `int`-Cast 0 ergibt und weder `> 0` noch `< 0` greift.
    - Pitch 0. Yaw und Kopf-Yaw = `(int)yawHead / 90 · 90`.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming` (LeafMonster.java:28), 1 `EntityAIPanic` 1.35 (LeafMonster.java:29), Ziel 1 `EntityAIHurtByTarget` (LeafMonster.java:30). **Kein** Umherwandern, das Monster steht still.
  - Mit Chance 1/100 pro Tick wird das Rachziel gelöscht (LeafMonster.java:147-149).
  - Zielschleife, mit Chance 1/4 pro Tick (LeafMonster.java:150):
    - Suchbox `expand(4, 6, 4)` (LeafMonster.java:201).
    - Gültig (LeafMonster.java:166-195): sichtbar und lebend, dazu **nur** `EntityAnt`, `EntityButterfly`, `EntityLunaMoth` oder Spieler außerhalb von Creative.
    - Mit Ziel: anschauen, `setAttacking(1)` (hebt die Tarnung auf), Pfad mit 1.25 (LeafMonster.java:153-155). Schlag ab Abstand² < 5 mit Chance `1/8 + 7/8·1/10` ≈ 0.21 (LeafMonster.java:156-158).
    - Ohne Ziel: `setAttacking(0)`, die Tarnung greift wieder (LeafMonster.java:161).
  - Schaden `LeafMonster_stats.attack` (2, manifest) über vanilla `attackEntityAsMob`, ohne Zusatzeffekt.
- **Interaktion:** keine.
- **Drops:** `getDropItem` mit `rand(3)` (LeafMonster.java:131-140): 0 → `log`, 1 → `leaves`, 2 → `rotten_flesh`. Die Anzahl ist vanilla.
- **Spawnen:** `getCanSpawnHere` (LeafMonster.java:216-247):
  1. Spawner `"Leaf Monster"` im Würfel −3..+2 / 0..+4 → sofort erlaubt (LeafMonster.java:217-231).
  2. Sonst gelten alle folgenden Bedingungen:
     - gültige Lichtstufe (LeafMonster.java:232), nicht Tag (LeafMonster.java:235)
     - in `DimensionID4` (Danger Dimension, `BaseDimensionID` 80 + 3 = 83, OreSpawnMain.java:1138, 1269; manifest) nur Y ≤ 20, sonst nur Y ≥ 50 (LeafMonster.java:238-245)
     - höchstens 4 LeafMonster in `expand(20, 10, 20)` (LeafMonster.java:246, 250)
  - Biome in der Oberwelt: manifest (`ambient`).
  - Zusätzlich, **nicht im manifest**, als Monster in der Danger Dimension (Gewicht 35, 2-4, BiomeGenUtopianPlains.java:111) und in Chaos (2, 1-4, BiomeGenUtopianPlains.java:419).
  - Spawner: `GenericDungeon.makeLeafMonsterDungeon` mit 4 Spawnern (GenericDungeon.java:2237-2252). Der Dungeon entsteht in der Oberwelt mit Chance 1/275 pro Chunk (OreSpawnWorld.java:1223, 294).
  - Despawn: `!isNoDespawnRequired()` (LeafMonster.java:255).
- **Zustand:** DataWatcher 20 = attacking, Byte im Bytecode (javap, das Decompilat zeigt `int`) (LeafMonster.java:43-52). `ModelLeafMonster` liest es (ModelLeafMonster.java:49). Kein NBT.
- **Sounds:**
  - Lebend: keiner
  - Treffer: `orespawn:leaves_hit`, Tod: `orespawn:leaves_death` (LeafMonster.java:111-121)
  - Lautstärke 0.65, Tonhöhe 1.0 (LeafMonster.java:123-129)
- **Config:** `LeafMonster_health/attack/defense` (6/2/1), `LeafMonsterEnable` (manifest), `PlayNicely`; `BaseDimensionID` für den Dimensionstest.
- **Portierung 1.21.1:**
  - Die Tarnung in `tick()` **nur serverseitig** einrasten und mit `setPos(...)` plus `setYRot/setYHeadRot` setzen. Clientseitige Rundung erzeugt Ruckeln gegen die Interpolation.
  - Den Randfall (−1, 1) 1:1 übernehmen oder mit `Mth.floor` korrigieren; als Abweichung notieren.
  - Dimensionstest: `level().dimension() == OreSpawnDimensions.DANGER` statt einer numerischen ID.
  - Spawn-Kategorie `ambient` (manifest) mit Lichtprüfung ist ungewöhnlich. In 1.21.1 hat AMBIENT ein Cap von 15 und `SpawnPlacements` braucht einen Predicate, der `Monster.checkMonsterSpawnRules` plus die Höhenregel prüft.
  - Zielklassen `EntityAnt`, `EntityButterfly` und `EntityLunaMoth` müssen portiert sein, alternativ ein Entity-Type-Tag.
  - Fallschaden über `causeFallDamage` mit Deckel.
