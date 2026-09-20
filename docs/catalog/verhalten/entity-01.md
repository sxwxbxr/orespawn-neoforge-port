# Verhalten: entity-01

Dieser Batch umfasst die königliche Bossfamilie ohne Nachwuchs: **The King** und **The Queen** sind fliegende, durch Blöcke gleitende Riesenbosse (`EntityMob`, 22x24 Blöcke) mit selbstgeschriebener Flug- und Zielsteuerung in `updateAITasks()`, Phasen-Schadensmultiplikatoren, einem Heilboden bei 2000 HP, der erst nach zehn Spielertreffern fällt, und eigenen Fernangriffen (Feuerbälle, ThunderBolt, IceBall). **KingHead** und **QueenHead** sind unsichtbare Zweit-Hitboxen ohne eigene Lebenspunkte, die 30 Blöcke vor dem Boss mitfliegen und jeden Treffer an ihn weiterreichen. **PurplePower** ist eine zielsuchende Energiekugel mit fünf Typen: Queen-Kugel (Typ 0), die drei Princess-Kugeln (1-3) und die explodierende Kugel des „Ultimate King" (10). Die gesamte Logik läuft ohne vanilla-Pathfinding; ein Port muss den Tick-Code nahezu wörtlich übertragen und dabei drei 1.21.1-Grenzen umgehen: MAX_HEALTH-Deckel 1024, anderes Rüstungsformel-Verhalten und ATTACK_DAMAGE-Deckel.

Vorab, gilt für alle fünf Klassen: der Quelltext ist bereits weitgehend MCP-benannt (keine `func_`/`field_`-Namen in diesen Dateien). Die 1.7.10-Rüstungsformel ist am Vanilla-Bytecode belegt (`sv.b(ro,F)F` = `EntityLivingBase.applyArmorCalculations`, `client-1.7.10.jar`): ist die Quelle nicht `isUnblockable`, gilt `schaden = schaden * (25 - getTotalArmorValue()) / 25`. Rüstung 25 heißt damit **völlige Immunität** gegen blockbaren Schaden, 21 heißt 16 % Durchlass.

---

### TheKing - The King (`the_king`)

- **Rolle:** Endgame-Boss, `EntityMob`, feindlich, nie despawnend (`canDespawn` false, TheKing.java:110-112). Fliegt mit `noClip = true` durch Blöcke (TheKing.java:73, 224, 391), feuerimmun (71), kein Fallschaden (803-807), nicht schiebbar (142-147), Blitz ohne Wirkung (877-878). Besitzt einen Endzustand „Ultimate King" (`isEnd`).

- **Werte** (was das manifest nicht oder falsch zeigt):

  | Größe | Wert | Herkunft |
  |---|---|---|
  | XP | 25000 | TheKing.java:70 |
  | fireResistance | 5000 | TheKing.java:72 |
  | renderDistanceWeight | 12.0 | TheKing.java:75 |
  | Nahkampf-Grundschaden | **250**, nicht 350: `applyEntityAttributes()` setzt `attdam = TheKing_stats.attack` (350, manifest) im Super-Konstruktor, danach überschreibt die Feldinitialisierung im Konstruktorrumpf auf 250.0 | TheKing.java:47 vs. 85 |
  | Phase HP < 2/3 (int: < 4666) | `attdam = attack*2` = 700 | TheKing.java:226-228 |
  | Phase HP < 1/2 (< 3500) | `attack*4` = 1400 | TheKing.java:229-231 |
  | Phase HP < 1/4 (< 1750) | `attack*8` = 2800 | TheKing.java:232-234 |
  | Phase HP < 1/8 (< 875) | `attack*16` = 5600 | TheKing.java:235-237 |
  | Bedingung aller Phasen | `player_hit_count < 10`; danach bleibt `attdam` auf dem letzten Wert eingefroren, es wird nie zurückgesetzt | TheKing.java:226-237 |
  | Heilboden | solange `player_hit_count < 10` und HP < 2000: sofort auf 2000 heilen. Läuft in `updateAITasks` vor der Phasenprüfung in `onUpdate`, daher sind die Phasen 1/4 und 1/8 praktisch unerreichbar (Schluss aus der Aufrufreihenfolge) | TheKing.java:605-607 |
  | Regeneration | 1/30 pro Tick: +5 HP; zusätzlich +200, wenn `large_unknown_detected` | TheKing.java:599-604 |
  | Schadensdeckel pro Treffer | 750 (vor Rüstung) | TheKing.java:819-821 |
  | Treffersperre | nach jedem angenommenen Treffer `hurt_timer = 20` Ticks, in der Zeit `attackEntityFrom` = false | TheKing.java:816-818, 840, 365-367 |
  | Rüstung | `large_unknown_detected` → 25; sonst bei `player_hit_count < 10` und HP < 2/3 → defense+1 = 22; sonst defense = 21. Die Zweige +2 (< 1/2) und +3 (< 1/4) sind **unerreichbar**, weil < 2/3 zuerst greift. Das manifest listet 23/24 trotzdem | TheKing.java:861-875 |
  | Effektiver Maximalschaden | 750 × (25−21)/25 = 120 pro Treffer bei Rüstung 21, 90 bei 22, 0 bei 25 (nur `isUnblockable` kommt durch) | Formel oben, Deckel TheKing.java:819 |
  | Heimradius | Rückflug, wenn > 120 Blöcke von `homex/homez` | TheKing.java:289-294 |
  | Zielradius | Ziel ungültig > 144 Blöcke vom Heimpunkt | TheKing.java:960-965 |
  | Flughöhen-Deckel | Y 230 | TheKing.java:429-431, 466-468, 848-850 |
  | Flügelgeräusch | alle 31 Ticks (`wing_sound > 30`) | TheKing.java:217-223 |

- **KI und Angriffe:**
  - Tasks: `tasks` 0 `EntityAISwimming`, 1 `EntityAILookIdle`; `targetTasks` 1 `EntityAIHurtByTarget(false)` (TheKing.java:76-78). Die Kampflogik liest das vanilla-Angriffsziel **nicht**; Rache läuft über das private Feld `rt`, gesetzt in `attackEntityFrom` (845-852). `EntityAILookIdle` dreht aber `rotationYawHead`, und daran hängen Kopfposition und Zielwinkel.
  - **Flug** (jeder Tick, 589-598): `motionX/Z += (signum(dx)*0.7 - motion)*0.35`, `motionY += (signum(dy)*0.69999 - motionY)*0.3`, `rotationYaw += Δ/8`, `moveForward = 1.0`; zusätzlich `motionY *= 0.6` in `onUpdate` (225). Neues Wanderziel, wenn zu weit von daheim, 1/200 pro Tick oder Ziel erreicht (dist² < 9.1): Heim ± 0..119 in X/Z, Höhe aus einem 3x3-Blockraster (±5) mit bis zu 19 Proben auf/ab, `dist/9 + 2` (395-433).
  - **Heimpunkt:** bei `guard_mode == 0` wandert der Heimpunkt jeden Tick mit (368-371). Nur Spawnerblock und Riesenbaum setzen `guard_mode = 1`.
  - **Angriffszweig:** Chance 1/`attrand` pro Tick; `attrand` = 5, = 3 bei HP < 1/2 und `player_hit_count < 10` (388-390) oder im Ultimate-Zustand (358).
    1. `rt` bevorzugt; verworfen bei Tod, 1/250 Zufall, > 128 vom Heim mit `guard_mode 1` oder wenn `MyCanSee` fehlschlägt (435-454). `PlayNicely != 0` → kein Ziel (436-438).
    2. `findSomethingToAttack()`: alle `EntityLivingBase` in `boundingBox.expand(80, 64, 80)`, sortiert mit `GenericTargetSorter` (Distanz² durch Fläche, falls > 1; Creeper halbiert, GenericTargetSorter.java:15-34), erster passender Kandidat (1025-1042).
    3. `isSuitableTarget` (943-1003): nicht tot, nicht `KingHead` (setzt `head_found`), nicht `MyUtils.isRoyalty`, ≤ 144 vom Heim, nicht `isIgnoreable` (Ameisen, Schmetterlinge, Vögel, Ghost usw., MyUtils.java:17-19), `MyCanSee` true, dann Spieler außer Creative, `EntityHorse`, `EntityMob`, `EntityDragon` oder `isAttackableNonMob` (u. a. Mothra, Leon, Dragon, Girlfriend, Boyfriend, Villager, MyUtils.java:13-15).
    4. `MyCanSee` (883-941): eigener Schrittstrahl ab 22 Blöcken vor dem Körper in Höhe `posY + height*7/8`; durchlässig sind nur `flowing_water`, `water`, `leaves` (nur 1.7.10-`Blocks.leaves`, nicht `leaves2`), `vine`, `air`.
    5. **Kopf nachspawnen:** wurde bei der Suche kein `KingHead` gefunden, `spawnCreature("KingHead")` auf `posY + 20` (456-458).
  - **Mit Ziel** (462-570): `setAttacking(1)`. Ohne `backoff_timer` fliegt er auf Zielmitte+1; mit 1/70 Zufall Rückzug für 80..159 Ticks (470-472), in dem er Punkte ± 30..49 um das Ziel anfliegt (474-512).
    - **Nahbereich** Distanz² < 900: mit 1/2 `doJumpDamage` am eigenen Standort, Radius 15 (Y ±10), Schaden `attack/4` = 87 (int-Division), ohne Rückstoß; danach `attackEntityAsMob` (513-518).
    - **Kopfstampfer**, unabhängig von der Distanz, solange ein Ziel existiert: 1/3 pro Angriffszweig `doJumpDamage` 20 Blöcke vor dem Kopf (`rotationYawHead`), Y +10, Radius 15, Schaden `attack/2` = 175, Rückstoß 2.75 horizontal, 0.65 hoch (519-523, 1120-1123).
    - `doJumpDamage` (1083-1126): trifft jedes Lebewesen außer sich, Royalty, `Ghost`, `GhostSkelly`; je `damage/2` als Explosionsschaden **und** `damage/2` als `DamageSource.fall`, plus `random.explode` (0.65). Der zweite, gleich hohe Treffer im selben Tick wird vom vanilla-Unverwundbarkeitsfenster vermutlich verschluckt (Schluss aus vanilla `hurtResistantTime`, nicht am Bytecode geprüft).
    - **Fernkampf** bei horizontaler Distanz² > 900 (524-569): Wahl `rand(3)`; feuert nur, wenn der Kopf (`rotationYawHead + 90°`) auf < 0.5 rad zum Ziel zeigt:

      | Salve | Inhalt | Munition / Nachladen | Herkunft |
      |---|---|---|---|
      | `firecanon` | 1 `BetterFireball.setReallyBig()` (Explosionsstärke 4) + 6 `setBig()` (Stärke 2), davon je 1/2 `setSmall()`; Ursprung 32 Blöcke voraus, Y +14; Treffer 10 Schaden (small 5) + 5 s Feuer; Ziel mit Fläche > 30 verliert zusätzlich die halbe HP | `stream_count` 10, alle 80 Ticks voll | TheKing.java:699-728, 376-378; BetterFireball.java:78, 82, 231-243, 279-281 |
      | `firecanonl` | 3 `ThunderBolt`, `setThrowableHeading(…, 1.4, 4.0)`, Bewegung ×3; Treffer 20 geworfen + 20 Mob-Schaden, 1 s Feuer, Explosion 3.0, echter Blitz; Royalty immun | `stream_count_l` 5, alle 90 Ticks | TheKing.java:730-762, 379-381; ThunderBolt.java:29-47 |
      | `firecanoni` | 5 `IceBall` mit `setIceMaker(1)`, gleiche Flugbahn; Treffer 16 geworfen (LaserBall-Basis), setzt 5 Eisblöcke im Umkreis ±3 | `stream_count_i` 8, alle 70 Ticks | TheKing.java:764-797, 382-384; LaserBall.java:97, 153; IceBall.java:63-80 |

      Die Zufallsversätze `r1..r3` werden in `firecanonl`/`firecanoni` berechnet, aber nie benutzt (742-744, 776-778). ThunderBolt und IceBall entstehen ohne Werfer.
    - Ohne Ziel: `setAttacking(0)`, alle drei Magazine voll (571-576).
  - **`attackEntityAsMob`** (249-283): Ziel mit Fläche `height*width > 30`, das nicht Royalty, `Godzilla`, `GodzillaHead`, `PitchBlack` oder `Kraken` ist: HP halbieren, dann `attdam*10` Schaden, `large_unknown_detected = 1`. `EntityDragon`: `attdam` als Explosionsschaden auf `dragonPartHead` (1/6) oder `dragonPartBody`. Danach normal `attdam` Mob-Schaden; bei Erfolg Rückstoß 3.3 horizontal, 0.25..0.5 hoch, ×1.5 bei Spielern oder toten Zielen.
  - **`attackEntityFrom`** (813-855): Reihenfolge: `hurt_timer` sperrt → Deckel 750 → `inWall` ignoriert → Angreifer mit Fläche > 30 (gleiche Ausnahmen wie oben): Schaden /10, `large_unknown_detected = 1` (`hurt_timer = 50` wird zwei Zeilen später von `= 20` überschrieben, 831 vs. 840) → kleiner `EntityMob` (Fläche < 3) als Angreifer wird per `setDead()` gelöscht, Treffer abgewiesen → `cactus` ignoriert → Treffer, Spielerzähler +1, Angreifer wird `rt` und Flugziel.
  - **Ultimate King (`isEnd`)**:
    - `isEnd = 1` über `setFree()` (1049-1051), aufgerufen nur von `ThePrinceAdult` (ThePrinceAdult.java:377-386, `FullPowerKingEnable`). Zwischensequenz (610-690): King steht still, `hurt_timer = 10` (unverwundbar); der nächste Spieler in 80/64/80 wird jeden Tick eingefroren, zum King gedreht und auf **1 HP** gesetzt (619-630). Chatnachrichten an alle Spieler im Radius bei `endCounter` 10, 80, 160, 240, 300, dann Countdown „9." bis „1." bei 320..480 im Abstand 20, bei 500 „The King: Prepare to die!" und `isEnd = 2`. Texte wörtlich aus TheKing.java:632-688 übernehmen.
    - `isEnd = 2` (352-364), jeden Tick: `hurt_timer = 10` → **dauerhaft unverwundbar**; `player_hit_count = 0` (Heilboden und Phasen wieder aktiv), alle Magazine voll, `attrand = 3`, `guard_mode = 0`, `large_unknown_detected = 1` (Rüstung 25, +200 Heilung). `backoff_timer` sinkt doppelt so schnell (361-363, 385-387). Zielsuche nimmt zuerst Spieler in 80/64/80 **ohne Sichtprüfung** (1010-1024); auch Girlfriend, Boyfriend und Villager brauchen keine Sicht (970-984). Solange `getAttacking() != 0`, entsteht **jeden Tick** eine `PurplePower` Typ 10, 10 Blöcke voraus auf Y +14, mit dreifacher King-Geschwindigkeit (578-588).

- **Interaktion:** kein Rechtsklick, nicht zähmbar, nicht reitbar. `setGuardMode(int)` (1045-1047) und `setFree()` sind die einzigen Außenschnittstellen.

- **Drops** (`dropFewItems`, 162-209; `getDropItem` = `yellow_flower` bleibt wegen des Overrides ungenutzt):
  - Spawnt die Entity **„The Prince"** (`the_prince`) auf Y +10 (165). Das ist kein Ei, anders als 01-mobs.md („The Prince (egg)").
  - Je 1× `royal_chest`, `royal_helmet`, `royal_leggings`, `royal_boots`, `royalsmall` (166-170).
  - 150 zufällige Einträge aus `Item.itemRegistry` und 150 aus `Block.blockRegistry` als Item, je Stapelgröße 1 (179-208). `Item.getItemFromBlock` liefert für Blöcke ohne Item null, das Original erzeugt dann einen ItemStack mit null.
  - Alle Drops streuen um ±19 in X/Z (`nextInt(20) - nextInt(20)`), Y +12 (157-160). XP 25000 (70).

- **Spawnen:** `getCanSpawnHere` true (857-859), aber keine natürlichen Spawneinträge (manifest `spawns: []`). Quellen:
  - `KingSpawnerBlock` (`kingspawner`): beim Setzen Update nach 100 Ticks, außerdem bei Zufallstick, Abbau und `canBlockStay`; spawnt bei `TheKingEnable != 0` auf Y +8 mit `guard_mode 1` und entfernt sich samt Block darüber (KingSpawnerBlock.java:43, 47-58, 69-78). Im Quelltext platziert keine Weltgenerierung diesen Block (grep `MyKingSpawnerBlock`).
  - Spawn-Ei `eggtheking` (ItemSpawnEgg.java:406-409), ohne Wachmodus, der Heimpunkt wandert also mit.
  - Riesenbaum „Tree of Goodness" (Gold/Smaragd/Diamant) in Utopia: `OreSpawnWorld.addHugeTree` nur in `DimensionID` (OreSpawnWorld.java:28-30), Zweig `rand_treetype == 0` von 100, dann 1/2 King-Baum (1933-1938), bei `LessLag` 1/2 bzw. 1/4 seltener (1906-1911). Oben auf dem Baum steht der King auf Y +4 mit `guard_mode 1` (ItemMagicApple.java:471-482). Dieselbe Methode läuft auch beim Magic Apple, bei `rand(100) == 1` und `GinormousEmeraldTreeEnable != 0` (ItemMagicApple.java:827-831).
  - `ThePrinceAdult`-Verwandlung → King mit `isEnd = 1` (siehe oben).
  - Eierteile `orethekingpart` aus der Erzgenerierung (OreSpawnWorld.java:737, ChunkOreGenerator.java:410), 9 Teile → `oretheking` (OreSpawnMain.java:2551).

- **Zustand:**

  | DataWatcher | Typ | Bedeutung | Herkunft |
  |---|---|---|---|
  | 20 | int | `attacking` (0/1): Modell-Animation (ModelTheKing.java:750, 844, 867, 977, 1037) und PurplePower-Ausstoß | TheKing.java:91, 114-120 |
  | 21 | int | `PlayNicely` des Servers, jeden AI-Tick nachgeschrieben, Client-Renderskala | TheKing.java:92, 350 |
  | 22 | int | `isEnd`, Client-Partikel `fireworksSpark` (1/3 pro Tick, 10 Stück, 7 Blöcke voraus, Y +14) | TheKing.java:93, 238-246, 349 |

  NBT (1053-1071): `KingHomeX`, `KingHomeZ`, `GuardMode`, `PlayerHits`, `IsEnd`, `EndCounter`. **Nicht** gespeichert: `large_unknown_detected`, `attdam`, `rt`, `hurt_timer`, Magazine. Nach einem Neuladen startet der Nahkampf also wieder bei 250, bis `onUpdate` die Phase neu bewertet.

- **Sounds:** `orespawn:king_living` (living), `orespawn:king_hit` (hurt), `orespawn:trex_death` (death), Lautstärke 1.35, Tonhöhe 1.0 (122-140); `orespawn:MothraWings` (manifest-Id `mothrawings`) alle 31 Ticks mit 1.75/0.75, nur serverseitig (217-223); vanilla `random.fuse` (Riesenfeuerball), `random.bow` (jede weitere Salve), `random.explode` (0.65 pro Stampfer-Opfer).

- **Config:** `TheKing_health` 7000, `TheKing_attack` 350, `TheKing_defense` 21 (manifest, `OreSpawnMOBS`); `PlayNicely` 0 (`OreSpawnTWEAKS`): Hitbox 5.5x6 statt 22x24 (TheKing.java:63-68), Renderskala /4 (RenderTheKing.java:34-40), keine Ziele, kein Kopf (1006-1009); `TheKingEnable` 1 wirkt nur im Spawnerblock; `FullPowerKingEnable` 0 wirkt nur in `ThePrinceAdult`; Gamerule `mobGriefing` über die Projektile.

- **Portierung 1.21.1:**
  - Basis `Monster`; Logik aus `updateAITasks` nach `customServerAiStep()`, aus `onUpdate` nach `tick()`/`aiStep()`. `noPhysics = true` jeden Tick, `fireImmune()` im `EntityType.Builder`, `xpReward = 25000`, `removeWhenFarAway` → false, `isPushable` → false, `doPush` leer, `causeFallDamage` → false, `thunderHit` leer.
  - **Leben 7000 > 1024:** virtuelle Lebenspunkte (eigenes synchronisiertes float-Feld plus Skalierung auf MAX_HEALTH) oder eingehenden Schaden mit 1024/7000 skalieren. Heilboden 2000, Phasenschwellen 2/3, 1/2, 1/4, 1/8 und `KingHead.setHealth` müssen alle auf dem virtuellen Wert laufen.
  - **Rüstung:** 1.21.1 deckelt die Reduktion bei 80 % (`CombatRules`), 1.7.10 erlaubt 100 %. `getDamageAfterArmorAbsorb(DamageSource, float)` überschreiben: ohne `DamageTypeTags.BYPASSES_ARMOR` `dmg * (25 - armor) / 25` mit dem Wert aus `getTotalArmorValue()`. Nicht über das ARMOR-Attribut lösen.
  - **Schaden 5600 und `attdam*10` = 56000** liegen über dem ATTACK_DAMAGE-Attributdeckel (in `neoforge-*-sources.jar` nachsehen): `attdam` als eigenes double-Feld führen, nie als Attribut. Entscheidung dokumentieren: 1:1 heißt Grundschaden **250**, nicht der Config-Wert 350.
  - Hitbox `EntityDimensions.scalable(22, 24)` bzw. `(5.5, 6)`: `getDefaultDimensions(Pose)` aus dem synchronisierten PlayNicely-Wert, bei Änderung `refreshDimensions()` in `onSyncedDataUpdated`. `EntityType` mit `clientTrackingRange(128/16)`, `updateInterval(1)` (manifest tracking 128/1/0).
  - `DataWatcher` 20-22 → `EntityDataAccessor<Integer>` in `defineSynchedData(SynchedEntityData.Builder)`.
  - `DamageSource`-Strings → `DamageTypes.IN_WALL`, `DamageTypes.CACTUS`; `setExplosionSource(null)` → `damageSources().explosion(null, null)`; `DamageSource.fall` → `damageSources().fall()`; `causeMobDamage` → `damageSources().mobAttack(this)`.
  - Ender-Drache: `EnderDragon.head`/`body` und eine teilbezogene `hurt`-Überladung; Signatur vor dem Schreiben in den Sources prüfen (CLAUDE.md: keine API aus dem Gedächtnis).
  - **Spieler-Rückstoß und Einfrieren:** Server-seitiges `setDeltaMovement` auf Spielern braucht `hurtMarked = true`, die Blickrichtung braucht ein Teleport-Paket (`ServerPlayer.connection.teleport` o. ä.). Sonst sieht der Client davon nichts.
  - Chat: `player.sendSystemMessage(Component.literal(...))`.
  - Zufallsdrops: `BuiltInRegistries.ITEM`/`BLOCK.getRandom(random)`; `Block.asItem()` liefert `Items.AIR` statt null, das ist zu überspringen. Genau 150 + 150 Treffer zählen wie im Original (Schleife zählt nur Nicht-null). Bewusste Folge des 1:1: Command-Block, Barriere und Spawn-Eier sind möglich.
  - `MyCanSee` wörtlich übernehmen, nicht durch `ClipContext` ersetzen: die Durchlassliste (Wasser, `Blocks.leaves`, Ranke, Luft) weicht von vanilla ab. `Blocks.leaves` in 1.7.10 = Eiche/Fichte/Birke/Dschungel; `BlockTags.LEAVES` wäre breiter, das Abweichen kennzeichnen.
  - Projektile ohne Werfer (ThunderBolt, IceBall): im Port den King als Owner setzen oder die fehlende Kill-Zuordnung bewusst übernehmen.
  - Rendering: `shouldRenderAtSqrDistance` → true und Renderer-`shouldRender` → true (Original `isInRangeToRenderDist`, 100-108), sonst verschwindet das 22-Block-Modell am Frustum-Rand.
  - Ultimate-Zustand erzeugt 20 PurplePower pro Sekunde, jede mit Explosionsstärke 9.1. Kein Fehler, aber im Port einen Entity-Deckel als Konfigoption erwägen und im README erwähnen.
  - Sounds: `random.fuse` → `SoundEvents.TNT_PRIMED`, `random.bow` → `SoundEvents.ARROW_SHOOT`, `random.explode` → `SoundEvents.GENERIC_EXPLODE`; Partikel `fireworksSpark` → `ParticleTypes.FIREWORK`.
  - Recherche-Abgleich: 01-mobs.md nennt 350 [OS] gegen 250 [FW/NW]. Der Code bestätigt 250 als Nahkampfbasis (Konstruktor-Reihenfolge). Die NW-Angabe „Deckel 120 pro Treffer" passt exakt zu 750 × 4/25.

---

### KingHead - KingHead (`king_head`)

- **Rolle:** unsichtbare Zweit-Hitbox für die Köpfe des Kings, `EntityLiving` (nicht AI-aktiviert), kein Kampf, keine eigene Wirkung. Renderer zeichnet nichts (`RenderKingHead.doRender` leer, RenderKingHead.java:17-21).

- **Werte:** Hitbox 19.9x10 (KingHead.java:23), `noClip` (24), `fireResistance` 10000 (25), feuerimmun (26), Leben = `TheKing_stats.health` (31), Schaden 0 (33-34). XP 0 (kein `experienceValue` gesetzt). Tracking 128/10/true (OreSpawnMain.java:3955).

- **KI und Angriffe:** `onUpdate` ruft **nicht** `super.onUpdate()` (116-159). Damit laufen weder vanilla-Tod, Treffer-Zeiten, Feuer, Luft noch Tick-Zähler.
  - Server: sucht den ersten `TheKing` in `boundingBox.expand(32, 32, 32)`. Gefunden: Position = King Y +12, 30 Blöcke vor dem King entlang `rotationYawHead` (`posX = kx - 30*sin`, `posZ = kz + 30*cos`), Yaw, Kopf-Yaw und Bewegung übernehmen, `setHealth(king.getHealth())` (138-154). Kein King: `setDead()` (155-157).
  - Client: bootartige Interpolation über `boatPosRotationIncrements` (6 bzw. `par9 + 8` mit Reiter, 89-104, 122-136).
  - `setFire(0)` und `isAirBorne = true` jeden Tick (120-121).

- **Interaktion:** keine. `canBePushed` true (55-57), `canBeCollidedWith` true (84-86): Spieler können ihn anvisieren und schlagen.
  - `attackEntityFrom` (59-82): `inWall` ignoriert; Schaden von `TheKing`/`KingHead` als `getEntity()` oder `getSourceOfDamage()` ignoriert; sonst wird der Treffer **unverändert** an den ersten `TheKing` in 48/32/48 weitergereicht. Rückgabe ist dessen Ergebnis. Der Kopf verliert selbst nie Leben.

- **Drops:** keine.

- **Spawnen:** nur durch `TheKing.updateAITasks` auf King Y +20, wenn die Zielsuche keinen Kopf fand (TheKing.java:456-458). `canDespawn` false (37-39). Keine natürlichen Spawns, kein Ei.

- **Zustand:** keine DataWatcher, kein NBT. Nach einem Neuladen findet der King den gespeicherten Kopf wieder oder spawnt einen neuen.

- **Sounds:** keine eigenen. `canTriggerWalking` false (47-49).

- **Config:** `TheKing_health` (manifest) als Max-Leben. `PlayNicely` wirkt indirekt: der King spawnt dann keinen Kopf (TheKing.java:1006-1009).

- **Portierung 1.21.1:**
  - Für 1:1 und Id-Parität als eigener registrierter `LivingEntity`-Typ `king_head` mit `NoopRenderer`. Alternative ist NeoForges `PartEntity` über `isMultipartEntity()`/`getParts()` am King: sauberer Treffer-Pfad, aber ohne eigene Registry-Id; die Entscheidung gehört ins Port-Log.
  - Kein `super.tick()` heißt im Port: `tick()` vollständig überschreiben, `baseTick()` nicht aufrufen. Position serverseitig per `setPos`, Client über `lerpTo`/`lerpSteps` statt `setPositionAndRotation2`.
  - Leben 7000 > 1024: nur spiegeln, nie selbst auswerten. Den virtuellen Wert des Kings synchronisieren oder den Kopf ganz ohne Health-Logik lassen. `setHealth(7000)` würde auf 1024 geklemmt.
  - Weiterreichen: `hurt(DamageSource, float)` → `king.hurt(source, amount)`; Quellenvergleich mit `source.getEntity()` und `source.getDirectEntity()`.
  - Dedicated-Server-Falle: das Original hält die Interpolationsfelder unter `@SideOnly(CLIENT)`, gehört also nicht in den gemeinsamen Code.

---

### TheQueen - The Queen (`the_queen`)

- **Rolle:** Endgame-Boss, `EntityMob`, **neutral bis gereizt** (`mood`), nie despawnend (TheQueen.java:118-120). Gleiche Flugtechnik wie der King (`noClip`, 75, 207, 484), feuerimmun (73), kein Fallschaden (784-788), nicht schiebbar (158-163), Blitz ohne Wirkung (862-863). Gutgelaunt terraformt sie und folgt dem King; gereizt schießt sie PurplePower-Salven und blockiert die Heilung ihres Opfers.

- **Werte:**

  | Größe | Wert | Herkunft |
  |---|---|---|
  | XP | 25000 | TheQueen.java:72 |
  | fireResistance | 5000 | TheQueen.java:74 |
  | Nahkampf-Grundschaden | **250**, nicht 225: gleiche Konstruktor-Reihenfolge wie beim King | TheQueen.java:48 vs. 87 |
  | Phase HP < 3/4 (< 4500) | `attack*20` = 4500 | TheQueen.java:209-211 |
  | Phase HP < 1/2 (< 3000) | `attack*100` = 22500 | TheQueen.java:212-214 |
  | Phase HP < 1/3 (< 2000) | `attack*500` = 112500; wegen Heilboden 2000 praktisch unerreichbar | TheQueen.java:215-217, 707-709 |
  | Phase HP < 1/4 (< 1500) | `attack*1000` = 225000; ebenso unerreichbar | TheQueen.java:218-220 |
  | Bedingung | `player_hit_count < 10`, danach eingefroren | TheQueen.java:209-220 |
  | Heilboden | HP < 2000 bei `player_hit_count < 10` → auf 2000 | TheQueen.java:707-709 |
  | Regeneration | 1/32 pro Tick: +5, zusätzlich +50 bei `player_hit_count < 10` | TheQueen.java:701-706 |
  | Schadensdeckel | 750 | TheQueen.java:800-802 |
  | Treffersperre | `hurt_timer = 20` | TheQueen.java:797-799, 828, 447-449 |
  | Rüstung | HP < 2/3 (4000) und `player_hit_count < 10` → defense+2 = 23, sonst 21. +3 (< 1/2) und +5 (< 1/3) sind **unerreichbar**; das manifest listet 24/26 trotzdem | TheQueen.java:849-860 |
  | Heimradius / Zielradius / Höhe | 120 / 144 / Y 230 | TheQueen.java:287-292, 941-946, 522-524 |
  | Flug | Horizontal `signum*0.65`, Faktor 0.35; vertikal 0.69999/0.3; `moveForward = 0.75`; Yaw /8 | TheQueen.java:691-700 |
  | Opfer-Heilsperre | Abstand² < 2000 | TheQueen.java:321-337 |

- **KI und Angriffe:**
  - Tasks identisch zum King: 0 `EntityAISwimming`, 1 `EntityAILookIdle`, target 1 `EntityAIHurtByTarget(false)` (78-80).
  - **Laune `mood`** (0 = glücklich, `isHappy()` = `getIsHappy() == 0`, 194-196):
    - → 1 bei **jedem** `attackEntityFrom`, der Treffersperre und `inWall` passiert, also auch bei Explosion, Kaktus und PurplePower (806).
    - → 0 mit 1/500 pro Tick, wenn HP > Max − 2 (454-456).
    - `always_mad != 0` (NBT `MeanMode`, `setBadMood`, 1000-1002) erzwingt jeden Tick 1 (457-459).
    - Glücklich: `findSomethingToAttack` → null und `rt` ignoriert (549-551, 972-975). Sie greift also nie an und spawnt keinen Kopf.
  - **Energie `attack_level`** (Start 1, 60):
    - Glücklich jeden Tick +10 (460-462), immer −1, solange > 1 (444-446). Nettozuwachs +9/Tick, die Schwelle 1000 fällt also etwa alle 112 Ticks (aus 1000/9 abgeleitet).
    - Pro Angriffszweig mit Ziel, solange < 1000: +15, +15 bei HP < 1/2, +15 bei Zielfläche > 50, +15 bei > 100, +25 bei > 200 (576-591).
    - **> 1000 und gereizt:** 15 `PurplePower` Typ 0 auf einmal, **45** bei `player_hit_count < 10`, je 8 Blöcke voraus, Y +14, Bewegung ×3 der Queen (338-351).
    - **> 1000 und glücklich** (352-441): nur bei `mobGriefing` 25 zufällige Spalten ±24 um sie, von Y −20 aufwärts:
      - Gras mit Luft darüber → Blume aus 8 Sorten: `red_flower`, `yellow_flower`, `flower_blue`, `flower_pink`, `crystalflower_red/green/blue/yellow`
      - Erde mit Luft darüber → Gras
      - Stein mit Luft darüber → Erde darauf
      - Sand mit Luft darüber → 1/2 Kaktus darauf, sonst Sand → Erde
      - Lava/fließende Lava mit Luft darüber → Wasser/fließendes Wasser

      Danach, **ohne** Gamerule-Prüfung, 10 Versuche: an Luftposition ±14, Y 0..19 zu 1/2 `Butterfly`, sonst `Bird` spawnen (427-440).
    - Danach `attack_level = 1` (442). DW 23 wird alle 10 Ticks gesetzt (473-477); das Modell zeigt `PowerCube2` ab > 350 und `PowerCube3` ab > 650 (ModelTheQueen.java:1329-1334), Client-Partikel ab > 800 (TheQueen.java:221-228).
  - **Wandern:** wie King (Neuziel bei zu weit, 1/200 oder erreicht, 488-525), Magazine `stream_count` 10 alle 60 Ticks, `stream_count_l` 6 alle 70 Ticks (467-472). Glücklich sucht sie beim Neuziel einen `TheKing` in 64/32/64 und fliegt auf ±15 in X/Z, ±7 in Y an ihn heran; dabei setzt sie `guard_mode = 0` **dauerhaft**, ihr Heimpunkt wandert danach mit (526-545).
  - **Angriffszweig** (1/`attrand`; 5, bzw. 3 bei HP < 1/2 und `player_hit_count < 10`, 481-483):
    - `rt` mit 1/450 Verwurf (560), sonst wie King.
    - Zielsuche in `expand(80, 60, 80)` (976).
    - `isSuitableTarget` wie King, aber Sichtprüfung über vanilla `getEntitySenses().canSee` (951) und nur `QueenHead` als Kopfmarke (934-937).
    - `MyCanSee` (nur für `rt`) startet 10 Blöcke voraus auf Y +14 und lässt **nur Luft** durch (868-922).
    - Kopf: `QueenHead` auf Y +20 **nur wenn gereizt** (569-571).
    - Rückzug 1/50 für 90..179 Ticks (599-601).
    - Nahbereich Distanz² < 900: 1/2 `doJumpDamage` Radius 15, Schaden `attack/4` = 56, ohne Rückstoß, dann `attackEntityAsMob` (642-647).
    - Stampfer 20 Blöcke voraus, hier entlang `rotationYaw` (nicht Kopf-Yaw), 1/3, `attack/2` = 112, Rückstoß 2.75/0.65 (648-652, 1069-1072).
    - Fernkampf ab horizontaler Distanz² > 900: Wahl `rand(2)`, Zielwinkel gegen `rotationYaw + 90°` < 0.5 rad (653-683). `firecanon` identisch zum King (718-747). `firecanonl` mit 3 `ThunderBolt` ohne Versatz-Variablen (749-778). **Kein IceBall.**
  - **`attackEntityAsMob`** (231-281, nur Server für den Sperrteil):
    - Merkt sich das Opfer `ev` und dessen HP `evh`; war das Opfer seitdem geheilt, wird es auf `evh` zurückgesetzt.
    - Ziel mit Fläche > 30: HP × 3/4 plus `attdam` Extraschaden. `evh` sinkt auf die neuen HP, bei ≤ 0 folgt `setDead()`.
    - In `updateAITasks` wird die Sperre **jeden Tick** erzwungen, solange das Opfer lebt und Abstand² < 2000 (321-337). Das ist das „disables regeneration" aus 01-mobs.md, im Code bestätigt.
    - Ender-Drache wie King, dann normal `attdam`, Rückstoß 2.75, 0.2..0.45 hoch, ×1.5 bei Spielern/Toten.
  - **`attackEntityFrom`** (794-843): Treffersperre → Deckel 750 → `inWall` → `mood = 1` → **Explosion heilt** um `par2/2` (ungedeckelt, bis Max-HP) und wird abgewiesen → Angreifer `PurplePower` abgewiesen → kleiner `EntityMob` (Fläche < 3) wird gelöscht → `cactus` ignoriert → Treffer, Spielerzähler, `rt`. Keine Sonderbehandlung großer Angreifer wie beim King.

- **Interaktion:** keine direkte. Außenschnittstellen `setGuardMode`, `setBadMood` (996-1002).

- **Drops** (`dropFewItems`, 178-188):
  - 1× `royalsmall`, 1× `eggtheprince` (Spawn-Ei **The Prince**), Entity **„The Princess"** (`the_princess`) auf Y +10.
  - 56× je `queenscale`, `beef`, `bone`, `rotten_flesh`, einzeln ±19/Y +12 gestreut.
  - XP 25000.
  - Abweichung zur Recherche: 01-mobs.md nennt „eggs (Princess)". Im Code ist das Ei ein Prince-Ei und die Princess kommt als Entity. 03-items.md Zeile 429 hat es richtig.

- **Spawnen:** `getCanSpawnHere` true, keine natürlichen Spawns (manifest). Quellen:
  - `QueenSpawnerBlock` (`queenspawner`): Klon des King-Blocks, `TheQueenEnable`, Y +8, `guard_mode 1`, **ohne** `setBadMood` (QueenSpawnerBlock.java:54-58, 69-78); ebenfalls ohne Weltgen-Platzierer.
  - Spawn-Ei `eggthequeen` (ItemSpawnEgg.java:410-413).
  - „Queen's Tree" (Obsidian/Rubin/Amethyst) in Utopia: selber Zweig wie der King-Baum, andere Hälfte (OreSpawnWorld.java:1939-1941). Oben mit `guard_mode 1` **und** `setBadMood(1)`, dort also dauerhaft gereizt (ItemMagicApple.java:484-493). Ebenso über den Magic Apple (ItemMagicApple.java:832-834).
  - Eierteile `orethequeenpart` (OreSpawnWorld.java:781, ChunkOreGenerator.java:454), 9 → `orethequeen` (OreSpawnMain.java:2557).

- **Zustand:**

  | DataWatcher | Bedeutung | Herkunft |
  |---|---|---|
  | 20 | `attacking` (0/1), Modell (ModelTheQueen.java:817, 911, 934, 1044, 1104) | TheQueen.java:94, 122-128 |
  | 21 | `PlayNicely` (alle 10 Ticks) | TheQueen.java:95, 474 |
  | 22 | `mood`, Textur: glücklich → `thequeentexture2.png`, sonst `thequeentexture.png` (RenderTheQueen.java:47-53) | TheQueen.java:96, 475 |
  | 23 | `attack_level` als „Power" (Kugelgröße, Partikel) | TheQueen.java:97, 130-136, 476 |

  NBT (1004-1020): `KingHomeX`, `KingHomeZ` (**King**-Schlüssel, so übernehmen), `GuardMode`, `PlayerHits`, `MeanMode`. **Nicht** gespeichert: `mood`, `attack_level`, `ev`/`evh`. Nach dem Neuladen ist sie ohne `MeanMode` wieder glücklich.

- **Sounds:** wie King: `king_living`, `king_hit`, `trex_death` bei 1.35/1.0 (138-156); `MothraWings` alle 31 Ticks mit 1.75/0.75 (200-206); `random.fuse`, `random.bow`, `random.explode`.

- **Config:** `TheQueen_health` 6000, `TheQueen_attack` 225, `TheQueen_defense` 21, `TheQueenEnable` 1 (manifest); `PlayNicely` (Größe 5.5x6 in 65-70, Renderskala /4 in RenderTheQueen.java:35-41, keine Ziele); `LessLag` und `GinormousEmeraldTreeEnable` nur für die Baumquelle; Gamerule `mobGriefing` für das Terraforming.

- **Portierung 1.21.1:**
  - Alles aus dem King-Abschnitt gilt: virtuelle Lebenspunkte (6000), eigene Rüstungsformel, `attdam` als Feld (225000 liegt weit über jedem Attributdeckel), Größenumschaltung, Render-Distanz.
  - Terraforming: `Blocks.grass` → `GRASS_BLOCK`, `red_flower` (Meta 0) → `POPPY`, `yellow_flower` → `DANDELION`, `stone` → nur `STONE` (1.7.10 hatte keine Varianten), `lava`/`flowing_lava` → Quell- bzw. fließendes Wasser über `FluidState.createLegacyBlock()`. Setzen ohne Überlebensprüfung (Kaktus!) mit `setBlock(pos, state, 3)`. Das Original prüft nur die Gamerule; `EventHooks.canEntityGrief` wäre eine bewusste Abweichung.
  - **Heilsperre:** das Setzen von `setHealth` auf ein fremdes Wesen jeden Tick kollidiert in 1.21.1 mit Regeneration-Effekten und Sättigung, wirkt aber gleich. `setDead()` bei `evh ≤ 0` wird zu `discard()`: das entfernt Spieler **nicht** sauber. Für Spieler `kill()` oder einen Schadensaufruf verwenden und die Abweichung kennzeichnen.
  - Explosions-Heilung: `source.is(DamageTypeTags.IS_EXPLOSION)`, Rückgabe false.
  - Queen braucht `EntityType`-Referenzen auf `butterfly` und `bird` (manifest: `EntityButterfly`, `Cockateil`), `the_princess` und die Items `eggtheprince`, `queenscale`, `royalsmall`.
  - `mood` und `attack_level` bewusst nicht persistieren (1:1).
  - Recherche-Abgleich: „Queen folgt dem King" (FW) ist im Code bestätigt; die FW-Erklärung „250 statt 225" ist dieselbe Konstruktor-Reihenfolge wie beim King.

---

### QueenHead - QueenHead (`queen_head`)

- **Rolle:** Zweit-Hitbox der Queen, `EntityLiving`, unsichtbar (`RenderQueenHead.doRender` leer, RenderQueenHead.java:17-21). Zeichengetreue Kopie von `KingHead` mit `TheQueen` statt `TheKing`.

- **Werte:** Hitbox 19.9x10 (QueenHead.java:23), `noClip` (24), `fireResistance` 10000 (25), feuerimmun (26), Leben = `TheQueen_stats.health` (31), Schaden 0 (33-34). Tracking 128/10/true (OreSpawnMain.java:3969).

- **KI und Angriffe:** wie KingHead. Kein `super.onUpdate()`; Position = Queen Y +12, 30 Blöcke voraus entlang ihres `rotationYawHead`; spiegelt Yaw, Bewegung und Leben; ohne Queen in 32/32/32 folgt `setDead()` (116-159).

- **Interaktion:** Treffer (außer `inWall` und Treffern von `TheQueen`/`QueenHead`) gehen unverändert an die erste `TheQueen` in 48/32/48 (59-82). Da die Queen dabei `mood = 1` setzt, reizt jeder Kopftreffer sie.

- **Drops:** keine.

- **Spawnen:** nur durch `TheQueen.updateAITasks`, und nur gereizt (TheQueen.java:569-571). `canDespawn` false (37-39).

- **Zustand:** keine DataWatcher, kein NBT.

- **Sounds:** keine. **Befund:** die Methode heißt `canTriggerWalQueen()` (47-49), ein Suchen-Ersetzen-Unfall von „King" → „Queen" in `canTriggerWalking`. Sie überschreibt nichts, der Kopf nutzt also den vanilla-Default von `canTriggerWalking`, anders als KingHead (false).

- **Config:** `TheQueen_health` (manifest); `PlayNicely` indirekt (keine Ziele, also kein Kopf).

- **Portierung 1.21.1:** eine gemeinsame Basisklasse `BossHeadEntity<T>` für King- und Queen-Kopf, mit Klasse und Health-Quelle als Parameter. Den `canTriggerWalQueen`-Unterschied entweder 1:1 übernehmen (Schrittgeräusche/`Entity.MovementEmission` der Queen-Hitbox bleiben an) oder als bewusste Korrektur protokollieren. Da `tick()` ohne `super` läuft und `noPhysics` gilt, ist der Unterschied praktisch unhörbar (Schluss, nicht belegt). Übrige Punkte wie KingHead: `NoopRenderer`, virtuelle Lebenspunkte nur spiegeln, Client-Interpolation über `lerpTo`.

---

### PurplePower - PurplePower (`purple_power`)

- **Rolle:** zielsuchende Energiekugel, `EntityLiving` (AI-aktiviert, aber ohne Tasks), fliegt mit `noClip` (PurplePower.java:32), feuerimmun (29), zählt als Royalty (MyUtils.java:10). Wirkt als Kamikaze-Projektil: berührt sie ihr Ziel, trifft sie einmal und verschwindet.

- **Werte:**

  | Größe | Wert | Herkunft |
  |---|---|---|
  | XP | 35 | PurplePower.java:28 |
  | fireResistance | 25 | PurplePower.java:30 |
  | Leben | 1000 (`mygetMaxHealth`) | PurplePower.java:94-96 |
  | Rüstung | 25, nach 1.7.10-Formel **immun** gegen blockbaren Schaden | PurplePower.java:221-223 |
  | Schadensdeckel | 10 pro Treffer | PurplePower.java:207-209 |
  | Immun gegen | Quelle mit `getEntity() instanceof EntityArrow`. In 1.7.10 ist `getEntity()` bei Pfeilen der Schütze, das trifft also nur werferlose Pfeile (vanilla-Semantik, nicht am Bytecode geprüft) | PurplePower.java:204-206 |
  | Lebensdauer | 1/2500 pro Server-Tick `setDead()`; Typ 10 explodiert dabei mit Stärke 9.1, Feuer an, Blockschaden nach `mobGriefing` | PurplePower.java:120-125 |
  | Friedlich | sofort `setDead()` | PurplePower.java:171-173 |
  | Suchradius | `expand(32, 24, 32)` | PurplePower.java:262 |
  | Trefferdistanz | Distanz² < (4 + Zielbreite/2)² | PurplePower.java:165 |
  | Flug | horizontal `signum*0.4`, Faktor 0.2; vertikal 0.7/0.2; Yaw /4; `moveForward = 0.75`; `motionY *= 0.6` | PurplePower.java:174-183, 105 |

- **Typen** (DataWatcher 20, gesetzt über `setPurpleType`, nur Server, 48-57):

  | Typ | Quelle | Treffer | Spieler als Ziel | gezähmte Tiere als Ziel | Render |
  |---|---|---|---|---|---|
  | 0 | `TheQueen` (setzt keinen Typ, TheQueen.java:345-349) | `setHealth(hp/4 - 1)`, dann `maxHealth/8` Mob-Schaden | ja (außer Creative) | ja | Skala 2.75, `purplepowertexture.png` |
  | 1 | `ThePrincess` (`1 + rand(3)`, ThePrincess.java:538) | `hp * 15/16`, dann 5 Schaden, 10 s Feuer | nein | nein | Skala 0.55, `purplepowertexture2.png` |
  | 2 | `ThePrincess` | wie 1, statt Feuer Gift 50 Ticks, Stufe 0 | nein | nein | 0.55, `purplepowertexture3.png` |
  | 3 | `ThePrincess` | wie 1, statt Feuer Schwäche 50 Ticks, Stufe 0 | nein | nein | 0.55, `purplepowertexture4.png` |
  | 10 | `TheKing` im Ultimate-Zustand (TheKing.java:586) | wie 0, plus Explosion 9.1 mit Feuer am Ziel | ja | ja | 0.55, `purplepowertexture10.png` |

  Herkunft der Trefferwerte: PurplePower.java:277-303; Zielregeln 245-255; Render RenderPurplePower.java:23, 34-58 (die Skala 2.75 ist das Konstruktorargument aus dem manifest, das manifest-`gl_scale` zeigt nur den 0.55-Zweig).

- **KI und Angriffe** (`updateAITasks`, 132-184):
  - Neues Wanderziel bei 1/300 oder Ziel erreicht (dist² < 2.1): ±8..17 in X/Z, −10..+9 in Y, bis zu 50 Versuche, bis das Ziel Luft **und** per Strahl ab Y +0.55 sichtbar ist (144-160, 128-130).
  - Sonst mit 1/7 (nicht friedlich) Zielsuche. Sortiert mit `GenericTargetSorter` wird das erste passende Ziel angeflogen; in Trefferdistanz folgen `attackEntityAsMob` und `setDead()` (161-170).
  - `isSuitableTarget` (225-256): nicht friedlich, lebt, nicht `isIgnoreable`, vanilla-Sicht `canSee`, Spieler-/Zähmregel laut Tabelle, nicht Royalty. Alles andere Lebende ist ein Ziel, **auch Kühe und Dorfbewohner**. `PlayNicely != 0` → keine Ziele (259-261).
  - `attackEntityFrom` (200-215): Pfeilprüfung, Deckel 10, dann vanilla; der Angreifer wird neues Flugziel.
  - Creeper Repellent stößt Kugeln ≠ Typ 10 weg (`f = (20 - Abstand)` geklemmt 0..20, ×0.4, CreeperRepellent.java:124-144). **Befund:** bei Typ 10 steht dort `return` statt `continue` (CreeperRepellent.java:126-128), eine Ultimate-Kugel im Bereich beendet die Abstoßung für alle folgenden Wesen. Den Wirkradius des Repellents regelt der CreeperRepellent-Batch (offen).
  - Recherche-Abgleich: 01-mobs.md nennt „70 % [MM] / 80 % [NW] der HP, ignoriert Rüstung". Der Code nimmt 75 % + 1 HP per `setHealth` (rüstungsfrei) plus `maxHealth/8` als normalen, rüstungsbehafteten Schaden.

- **Interaktion:** keine.

- **Drops:** keine (`getDropItem` null, 305-307); XP 35.

- **Spawnen:** nur als Beiwerk: Queen (Typ 0, 15/45 Stück), Princess (Typ 1-3, 3 Stück, ThePrincess.java:531-540), Ultimate King (Typ 10, jeden Tick). `getCanSpawnHere` true, keine natürlichen Spawns, `canDespawn` false (63-65), kein Ei. Tracking 64/1/true (OreSpawnMain.java:3114).

- **Zustand:** DataWatcher 20 = Typ (45). Der Server schreibt den Typ jeden Tick nach, der Client liest ihn in sein Feld zurück (114-119). NBT `PurpleType` (309-317).

- **Sounds:** keine (`getLivingSound`/`getHurtSound`/`getDeathSound` null, 75-85); Lautstärke 0.75 (67-69) daher wirkungslos. Client-Partikel `fireworksSpark`: Typ 0 mit 1/4 auf Y +1.25 und Streuung /2, andere Typen mit 1/6 auf Y +0.65 und Streuung /5 (106-113).

- **Config:** nur `PlayNicely` (manifest, `OreSpawnTWEAKS`). Gamerule `mobGriefing` für die Typ-10-Explosion.

- **Portierung 1.21.1:**
  - Basis `Mob` (braucht Attribute, Leben, Rüstung); `noPhysics`, `setNoGravity` bleibt aus, weil das Original die Schwerkraft nur dämpft. Registrierung mit `fireImmune()`, `sized(0.75f, 0.75f)`, Tracking 64/1.
  - Rüstung 25 und Deckel 10: `getDamageAfterArmorAbsorb` wie beim King überschreiben, sonst ist die Kugel in 1.21.1 plötzlich mit 20 % Durchlass verwundbar.
  - `setHealth(hp/4 - 1)` kann negativ werden und tötet Spieler ohne Schadensquelle (kein Totem, keine Todesnachricht mit Ursache). Im Port 1:1 lassen oder den Rest als `hurt` mit `damageSources().mobAttack(this)` ausführen und als Abweichung markieren.
  - Effekte: `MobEffects.POISON`/`WEAKNESS` mit 50 Ticks, Stufe 0; Feuer `igniteForSeconds(10)`.
  - Explosion: `level.explode(null, x, y, z, 9.1f, true, mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE)`; Gamerule über `GameRules.RULE_MOBGRIEFING`.
  - `isTamed` → `TamableAnimal.isTame()`; Pfeilprüfung → `source.getEntity() instanceof AbstractArrow`.
  - Renderer (client-Paket): Skala abhängig vom Typ, Textur nach Typ; das Modell rendert transluzent mit `glColor4f(0.75, 0.75, 0.75, 0.55)` (06-models-design.md Zeile 197) → `RenderType.entityTranslucent` mit Farb-Multiplikator.
  - Entity-Spam durch Ultimate King und Queen (45 pro Salve): die Kugeln sind `canDespawn false`, sterben aber mit 1/2500 pro Tick von selbst. Keine eigene Obergrenze im Original.
