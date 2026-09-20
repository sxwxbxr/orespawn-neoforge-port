# Verhalten: entity-04

Dieser Stapel umfasst elf Klassen: zwei Projektile (`Acid` als Variante der gemeinsamen Basis `LaserBall`, `BerthaHit` als unsichtbarer Reichweiten-Schlag der Bertha-Schwerter), sieben Bodenmobs mit handgeschriebener Ziel- und Angriffslogik außerhalb des Vanilla-AI-Systems (`Alien`, `Alosaurus`, `AttackSquid`, `BandP`/Criminal, `Basilisk`, zwei friedliche `EntityAnimal`: `Baryonyx`, `Beaver`), den frei fliegenden `Bee` ohne jede AI-Task und das reitbare Mech-Fahrzeug `AntRobot` (Robot Red Ant) mit eigener Schwebephysik und clientseitiger Bein-IK. Gemeinsame Muster: Ziele werden alle N Ticks per `getEntitiesWithinAABB` gesucht und mit `GenericTargetSorter` sortiert (Abstand², Creeper halbiert, geteilt durch `height*width` wenn >1; GenericTargetSorter.java:15-34), `MyUtils.isIgnoreable` schließt Ameisen, Schmetterlinge, Mücken, Dragonfly, Firefly, Cricket, Cockateil, Termite, Ghost, GhostSkelly, Elevator und RockBase aus (MyUtils.java:17-19), `OreSpawnMain.PlayNicely != 0` schaltet die Zielsuche ab, und `MyEntityAIWanderALot` startet mit 1/30 pro Tick einen Zufallsweg im Radius `xzRange`/7 (MyEntityAIWanderALot.java:35-41). Kein Wert dieses Stapels liegt über den 1.21.1-Klemmen (höchste Lebenspunkte 300, höchste Rüstung 18; manifest), virtuelle Lebenspunkte sind hier nicht nötig.

Gemeinsame Hilfsklassen, die jeder Port dieses Stapels braucht: `GenericTargetSorter`, `MyUtils`, `MyEntityAIWanderALot`, `MyEntityAIWander` (nur Baryonyx), `LaserBall` (Basis von Acid), `InkSack` und `WaterBall` (Projektile der Attack Squid).

---

### Acid - Acid (`acid`)

- **Rolle:** Projektil, `Acid extends LaserBall extends EntityThrowable`; ruft im Konstruktor `setAcid()` (Acid.java:13). Keine eigene Logik außer dem Icon-Index.
- **Werte:**
  - Icon-Index `my_index = 85` (Acid.java:12), gelesen von `RenderItemUrchin` über `getAcidIndex()` (RenderItemUrchin.java:32-34) als Kachel in `spinners.png` (manifest).
  - Schaden 16.0 als `DamageSource.causeThrownDamage(this, thrower)` (LaserBall.java:97, 153).
  - Setzt das Ziel 1 s in Brand, weil `is_iceball == 0` (LaserBall.java:154-156).
  - Lebensdauer 200 Ticks, danach `setDead` (LaserBall.java:180-183).
  - Rotation: `rotationPitch += 50` pro Tick, nur optisch (LaserBall.java:186-192).
  - Schwerkraft und Grundgeschwindigkeit sind nicht überschrieben: `func_70185_h` = `getGravityVelocity` (methods.csv:2126) ist Vanilla; `func_70182_d` hat im MCP-stable-12 kein Mapping. offen: exakte Vanilla-Werte stehen nicht im Quelltext.
- **KI und Angriffe (Einschlag):**
  - Trifft es `TrooperBug` oder `SpitBug`: Projektil verschwindet ohne Schaden (LaserBall.java:103-111).
  - Die LaserBall-Ausnahmen für Robot2..5, GiantRobot, gerittene Dragons und reitende Spieler gelten für Acid **nicht**, weil sie `is_acid == 0` voraussetzen (LaserBall.java:113, 135, 146).
  - Keine Rauch-/Funkenpartikel, kein `random.explode`-Sound, keine Explosion (LaserBall.java:161-175 nur bei `is_acid == 0`); im Flug keine Partikel (LaserBall.java:193-195).
- **Interaktion / Quellen:**
  - `ItemAcid` Rechtsklick: verbraucht 1 (außer Creative), Sound `random.bow` Lautstärke 3.0, Spawn nur serverseitig (ItemAcid.java:19-25).
  - Dispenser-Verhalten `MyDispenserBehaviorAcid` über `BehaviorProjectileDispense` (MyDispenserBehaviorAcid.java:10).
  - `SpitBug.watercanon` feuert Acid mit Geschwindigkeit 1.1 und Streuung 6.0 (SpitBug.java:288-296).
- **Drops:** keine.
- **Spawnen:** nur durch obige Quellen. Tracking 64/1/Velocity-Updates an (manifest).
- **Zustand:** keine DataWatcher-Einträge und kein eigenes NBT; `is_acid` ist ein Konstruktor-Feld, also auf beiden Seiten gesetzt.
- **Sounds:** keine eigenen (der Wurfsound kommt aus ItemAcid bzw. SpitBug).
- **Config:** keine.
- **Portierung 1.21.1:**
  - `LaserBall` einmal als Basis `ThrowableItemProjectile` mit Flags (acid/iceball/irukandji/special) portieren; `Acid` ist dann nur ein eigener `EntityType` mit gesetztem Flag.
  - Schaden über `damageSources().thrown(this, getOwner())`, Brand über `igniteForSeconds(1)`.
  - Dispenser über `ProjectileItem` am Acid-Item (1.21-Mechanik) statt eigener Behavior-Klasse.
  - Renderer: Spinner, der Kachel 85 aus `spinners.png` zeichnet (client-Paket), `gl_scale` 0.5 (manifest).
  - Das 200-Tick-Limit in `tick()` vor `super.tick()` prüfen, wie im Original.

### AntRobot - Robot Red Ant (`robot_red_ant`)

- **Rolle:** reitbarer Kampf-Mech, `extends EntityLiving` (kein `EntityCreature`, also kein Pathfinding). Wild feindselig gegen alles Lebende, im Besitzzustand (`owned=1`) ein Fahrzeug mit automatischem Angriff. Registriert unter dem Namen „Robot Red Ant" (OreSpawnMain.java:4165-4169).
- **Werte:**
  - XP = `AntRobot_stats.health / 2` = 150 bei Default (AntRobot.java:47; Default 300 aus OreSpawnMain.java:6147).
  - `attackDamage` wird manuell registriert, weil `EntityLiving` es nicht hat (AntRobot.java:65-66).
  - Feuerimmun (AntRobot.java:46); `setFire(0)` in jedem Tick (AntRobot.java:669).
  - Schadenstypen, die ignoriert werden: `inWall`, `cactus`, `inFire`, `onFire`, `magic`, `starve` (AntRobot.java:609-626).
  - Kein Fallschaden: `fall` und `updateFallState` sind leer (AntRobot.java:635-639).
  - Nicht schiebbar (AntRobot.java:592-594); Schatten 0.95 (AntRobot.java:957).
  - Tracking-Werte in der Klasse: 128 / Frequenz 10 / Velocity an (AntRobot.java:564-574); Registrierung mit 128/1/false (OreSpawnMain.java:4169, manifest).
  - Bewegungsgrenzen: |motionY| ≤ 0.85, |motionX|,|motionZ| ≤ 1.25 (AntRobot.java:729-746).
  - Reit-Höchstgeschwindigkeit 0.3 vorwärts (AntRobot.java:720), 0.25 rückwärts (AntRobot.java:894); Beschleunigung ±0.05 pro Tick (AntRobot.java:891, 895).
- **KI und Angriffe:**
  - Tasks: 1 `EntityAIWatchClosest(EntityPlayer, 12)`, 2 `EntityAILookIdle` (AntRobot.java:44-45). `isAIEnabled()` gilt nur ohne Reiter (AntRobot.java:942-944); `updateAITasks` und `updateAITick` brechen mit Reiter sofort ab (AntRobot.java:90-92, 143-145).
  - **Wild** (`owned == 0`, Schwierigkeit nicht Peaceful; AntRobot.java:94):
    - Fuß-Stampfer, 1/20 pro AI-Tick (AntRobot.java:95-97): trifft **alle** passenden Lebewesen in `expand(10,8,10)` mit echtem Abstand 6..9 Blöcke (AntRobot.java:1041-1046). Schaden `attack/10` = 3.0 bei Default (AntRobot.java:1060), Rückstoß 0.6 horizontal, 0.1 vertikal (0.2 für Spieler oder Tote) (AntRobot.java:1057-1063).
    - Ziel verwerfen mit 1/150 (AntRobot.java:98-100); totes Ziel wird gelöscht (AntRobot.java:102-105).
    - Zielsuche `findSomethingToAttack(2.0, false)` in `expand(24,12,24)`, **unsortiert**, erstes passendes Ziel gewinnt (AntRobot.java:107, 1075).
    - Annäherung: bei Abstand² > 16 wird `motionX/Z` direkt auf 0.2 in Zielrichtung gesetzt (`goThisWay`; AntRobot.java:111-116, 937-940).
    - Biss, 1/15 (AntRobot.java:121): Ziel mit Richtungsprüfung, trifft bei Abstand² < (6 + Zielbreite/2)² (AntRobot.java:127). Voller Schaden `AntRobot_stats.attack` = 30 (AntRobot.java:1140), Rückstoß 0.7/0.1 (x2 für Spieler oder Tote) (AntRobot.java:1137-1146).
  - **Geritten** (serverseitig, nicht Peaceful):
    - Stampfer mit 1/50 pro Tick (AntRobot.java:670-672).
    - Biss mit 1/9: `findSomethingToAttack(1.0, true)` in `expand(12,12,12)`, gleiche Reichweitenformel (AntRobot.java:673-685).
    - Der Reiter selbst ist ausgeschlossen (AntRobot.java:1027, 1101).
  - Zielregeln: alles Lebende außer `AntRobot`, Reiter, `isIgnoreable` und Creative-Spielern; Sichtlinie nötig (AntRobot.java:1088-1132).
  - Richtungsprüfung `dircheck`: unter Abstand² 36 immer gültig, sonst nur bis 0.75 rad Abweichung von `rotationYaw+90°` (AntRobot.java:1111-1126).
  - Wird es von einem `EntityLiving` getroffen, wird dieser zum Ziel. Spieler sind kein `EntityLiving` und lösen das nicht aus (AntRobot.java:627-631).
  - **Schwebephysik:**
    - Ungeritten auf dem Server: Block bei `posY-0.75`, sonst `posY-1.75` prüfen; ist er nicht Luft, Wasser oder Lava, dann `motionY += 0.15` und `posY += 0.15`, sonst `motionY -= 0.002` (AntRobot.java:810-822).
    - Clientseitig derselbe Test mit 0.12 (AntRobot.java:758-772).
    - Geritten: Block bei `posY-2.25` → `motionY += 0.06`, `posY += 0.03`, sonst `-0.02` (AntRobot.java:798-808).
    - Geländefolge beim Reiten: `dist = 3 + int(v*6)`, gerastert über k<dist, i<2*dist und Winkel -90..90 in 30°-Schritten. Jeder feste Block zählt +0.02, dann `motionY` und `posY += Summe*0.05` (AntRobot.java:825-841).
  - **Reitsteuerung:**
    - Yaw folgt dem Reiter mit Verzögerungsfaktor `clamp(|1.85-v|, 0.01, 0.9)` auf den Winkelrest mod 180 (AntRobot.java:843-869); `rotationPitch = 0`.
    - `moveForward > 0` beschleunigt, `< 0` bremst bzw. fährt rückwärts (AntRobot.java:877-921).
    - `moveEntity` läuft geritten **zweimal** pro Tick (AntRobot.java:922 und 927), mit Dämpfung 0.98 und danach 0.8 (x/z) bzw. 0.98 (y) (AntRobot.java:923-930).
- **Interaktion:**
  - `owned == 0`: Rechtsklick tut nichts (AntRobot.java:966-968).
  - Eisenbarren bei Abstand² < 25: heilt bis zu 100 LP und verbraucht 1 Barren (außer Creative) (AntRobot.java:969-987).
  - Aufsteigen bei Abstand² < 16, wenn niemand reitet, mit Sound `robotspidermount` 0.45 (AntRobot.java:991-994). Sitzposition: Y-Offset `0.55 + cos(rideTicker*0.19)*0.02`, 1.25 hinter der Mitte mit Wackeln `cos(rideTicker*0.33)*0.05` (AntRobot.java:597-605); `rideTicker += rand(3)` pro Tick (AntRobot.java:750).
  - Besitz entsteht über `ItemSpiderRobotKit` (Red Ant Robot Kit). Das Kit setzt die Lebenspunkte auf `maxDamage - itemDamage`, `maxDamage` = `AntRobot_stats.health`, und ruft `setOwned()` (ItemSpiderRobotKit.java:20, 30-44).
  - `ItemWrench` auf einen ungerittenen Robot: ein wilder braucht Lebenspunkte/Max ≤ 0.5, wird dann besessen, entfernt und als Kit mit Itemschaden = fehlende LP gedroppt (ItemWrench.java:42-54).
- **Drops:** 7..13 Würfe (AntRobot.java:1175), je `nextInt(12)` (AntRobot.java:1176):

  | Wurf | Item |
  |---|---|
  | 0 | redstone |
  | 1 | repeater |
  | 2 | comparator |
  | 3, 8 | redstone_block |
  | 4 | dispenser |
  | 5 | sticky_piston |
  | 6 | piston |
  | 7 | lever |
  | 9 | light_weighted_pressure_plate |
  | 10 | iron_ingot |
  | 11 | nichts |

  Quelle der Tabelle: AntRobot.java:1177-1222. Kein `getDropItem` (AntRobot.java:1159-1161). XP siehe Werte.
- **Spawnen:**
  - Kein natürlicher Spawn. `canDespawn` ist false (AntRobot.java:69-71), dazu `func_110163_bv` = `enablePersistence` im `entityInit` (AntRobot.java:582; methods.csv:32).
  - Strukturbindung: `GenericDungeon.makeRedAntHangout` baut eine 16x16-Kiesfläche und setzt einen **wilden** Robot in die Mitte (GenericDungeon.java:7086-7109).
  - Kits in Kisten: `pyramidJungleChest` Gewicht 3 (calls_OreSpawnMain.txt:8627), Dungeon-Listen Gewicht 10 (GenericDungeon.java:62, 70, 94); dazu Spawn-Ei `AntRobotEgg` (ItemSpawnEgg.java:484).
- **Zustand:**
  - DW 20 `int` attacking (0/1) (AntRobot.java:585, 1151-1157). ModelAntRobot liest ihn für Kiefer und Fühler (ModelAntRobot.java:252-258).
  - NBT `AntRobotOwned` (int) (AntRobot.java:948, 953).
  - Clientseitige Bein-IK in `RenderSpiderRobotInfo` (6 Beine, Schrittpaare 0-1, 2-3, 4-5): `initLegData`/`updateLegs`/`findNewFooting` (AntRobot.java:149-558). Nur auf dem Client aktualisiert (AntRobot.java:270-272, 795), gelesen von ModelAntRobot (ModelAntRobot.java:185-191).
- **Sounds:**
  - `orespawn:robotspider` 0.35/1.0 mit 1/80, wenn geritten und `playing == 0`; danach 125 Ticks Pause (AntRobot.java:751-757).
  - `orespawn:robotspidermount` 0.45 beim Aufsteigen (AntRobot.java:993).
  - Keine Living-, Hurt- oder Death-Sounds.
- **Config:** `AntRobot_stats` (health 300, attack 30, defense 16; OreSpawnMain.java:6147), `PlayNicely` (OreSpawnMain.java:1156). Kein Enable-Schalter.
- **Portierung 1.21.1:**
  - Basis `Mob` (nicht `PathfinderMob`). Reiten über `getControllingPassenger()` und `tickRidden(Player, Vec3)`/`getRiddenInput`; die Eigenphysik bleibt in `aiStep()`, damit der doppelte Move und die Dämpfungen 1:1 bleiben.
  - `positionRider`/`getPassengerRidingPosition` für den Sitzversatz.
  - Immunitäten über DamageType-Keys: `IN_WALL`, `CACTUS`, `IN_FIRE`, `ON_FIRE`, `MAGIC`, `STARVE`.
  - Die Bein-IK gehört ins Client-Paket, z. B. als `WeakHashMap<AntRobot, LegState>` im Renderer. `findNewFooting` liest Blöcke aus `level()` auf dem Client, das ist erlaubt.
  - Die Rauch-, Flammen- und Funkenpartikel am Heck (AntRobot.java:686-710) sind serverseitig wirkungslos → nur clientseitig in `aiStep` erzeugen.
  - Die Client-Interpolation (`setPositionAndRotation2`, +8/+6 Schritte; AntRobot.java:646-658) wird zu `lerpTo`.
  - Die Forschungsangabe „Red Ant Robots avenge killed ants" findet sich im Quelltext nicht: `AntRobot` wird nur von Kit, Wrench, Spawn-Ei und `makeRedAntHangout` erzeugt (grep).

### Alien - Alien (`alien`)

- **Rolle:** feindseliger Bodenmob, `extends EntityMob`, jagt nur Spieler und zerstört Fackeln.
- **Werte:**
  - XP 100 (Alien.java:41); `fireResistance` 30 (Alien.java:42).
  - `jumpMovementFactor` 0.6 (Alien.java:44); Sprung zusätzlich `motionY += 0.25` (Alien.java:86-89).
  - Navigator meidet Wasser und bricht Türen auf (Alien.java:39-40).
  - Bewegungswert 0.65 wird jeden Tick neu gesetzt (Alien.java:125).
  - Regeneration 1 LP mit 1/40 pro AI-Tick (Alien.java:352-354).
  - Immun gegen `cactus` (Alien.java:211).
  - `hurt_timer` wird nie über 0 gesetzt: nur gelesen (Alien.java:214) und heruntergezählt (Alien.java:315-317), also wirkungslos.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMoveThroughVillage(1.0)`; 2 `MyEntityAIWanderALot(10, 1.0)`; 3 `EntityAIWatchClosest(EntityPlayer, 8)`; 4 `EntityAILookIdle`. Target-Task 1 `EntityAIHurtByTarget(false)` (Alien.java:47-52).
  - Mit 1/8 pro AI-Tick Zielsuche (Alien.java:318):
    - Behält ein lebendes `getAttackTarget()`, sonst nicht-kreative Spieler in `expand(12,4,12)`, sortiert (Alien.java:367-394).
    - Dreht sich 10°/10° zum Ziel (Alien.java:321).
    - Bei Abstand² < 16 `setAttacking(1)` und Angriff mit (1/4 oder 1/5) (Alien.java:322-327).
    - Läuft **immer** mit 1.2 zum Ziel (Alien.java:328).
  - Nahkampf (`attackEntityAsMob` → Vanilla-Schaden `Alien_stats.attack`):
    - Rückstoß 1.1 horizontal, 0.1 vertikal, bei Spielern oder Toten 0.2 (Alien.java:196-202).
    - Gift mit 1/5 (`nextInt(5)==1`), Dauer `var2*5` Ticks, Stufe 0 (Alien.java:193-195).
    - `var2` ist 6, auf EASY 8. Die NORMAL/HARD-Zweige sind in den EASY-Zweig geschachtelt und nie erreichbar (Alien.java:183-192), also 30 bzw. 40 Ticks.
  - Fackeljagd: sonst mit 1/30 bei `PlayNicely == 0` (Alien.java:334):
    - Schalen mit Radius 2..14 um die eigene Position, ab Radius 10 wird jede zweite übersprungen (Alien.java:340-344). Gesucht werden `Blocks.torch` oder `OreSpawnMain.ExtremeTorch` (Alien.java:232).
    - Geht mit 1.0 zur nächsten (Alien.java:346).
    - Ist sie mit Abstand² < 27 und `mobGriefing` erreicht, wird sie ohne Drop durch Luft ersetzt (Alien.java:347-349).
  - Wird es von einem `EntityLiving` getroffen (Spieler zählen nicht), wird dieser zum Ziel mit Lauf 1.2; die Methode liefert dann immer true (Alien.java:217-224).
- **Interaktion:** keine.
- **Drops:** 5..10 `spider_eye` (Alien.java:166-168), 5..10 `flint` (Alien.java:169-171), je 1 `map`, `clock`, `compass` (Alien.java:172-174), verstreut ±3 Blöcke (Alien.java:161). `getDropItem` spider_eye (Alien.java:157) ist durch das überschriebene `dropFewItems` ohne Wirkung.
- **Spawnen (`getCanSpawnHere`):**
  - Ein Mob-Spawner mit Name „Alien" in x/z -3..2 und y 0..4 erlaubt immer (Alien.java:406-420).
  - Sonst ist `isValidLightLevel` nötig (Alien.java:421).
  - In `DimensionID4` („Dimension-Islands", manifest) immer erlaubt (Alien.java:424-426).
  - Sonst nur bei `posY <= 50` (Alien.java:427) und 3x3 Luft auf y+1..3 (Alien.java:430-439).
  - `canDespawn` = `!isNoDespawnRequired` (Alien.java:78-80).
  - Spawnlisten: `ChunkProviderOreSpawn2` Monster Gewicht 35, 2-3 (ChunkProviderOreSpawn2.java:372-374; Provider von `DimensionID2` „Dimension-Extreme", manifest).
  - Spawner in `GenericDungeon.makeDungeon` (GenericDungeon.java:178) und `makeAlienWTFDungeon` (GenericDungeon.java:1783, 1794).
  - Das Manifest führt keine Spawns. Für Islands ist keine Spawnliste mit Alien belegt (grep `Alien.class`).
- **Zustand:**
  - DW 20 `int` attacking (Alien.java:64, 397-403). ModelAlien fächert damit die Federn auf (ModelAlien.java:358-364, 457-463).
  - `RenderInfo` (rf1..4, ri1..4) ist reiner Client-Animationsspeicher, den ModelAlien beschreibt (ModelAlien.java:453-463).
  - Kein NBT.
- **Sounds:** `orespawn:alien_living` mit 1/4 (Alien.java:134-137), `orespawn:alien_hurt` (Alien.java:141), `orespawn:alien_death` (Alien.java:145); Lautstärke 1.0, Tonhöhe 1.0 (Alien.java:148-154). Clientpartikel `dripLava` mit 1/20 vor dem Kopf (Alien.java:116-121).
- **Config:** `AlienEnable` (OreSpawnMain.java:6085), `Alien_stats` (OreSpawnMain.java:6163), `PlayNicely` (OreSpawnMain.java:1156), `DimensionID4` (OreSpawnMain.java:1269).
- **Portierung 1.21.1:**
  - `Monster`. Die Zielsuche als eigenes `Goal` oder direkt in `customServerAiStep` mit denselben Wahrscheinlichkeiten.
  - `ExtremeTorch` braucht die eigene Block-Referenz; Vanilla `Blocks.TORCH` plus `WALL_TORCH`. Das Original kennt nur die Stehfackel, weil 1.7.10 beide in einem Block per Metadaten hat; für 1:1 beide 1.21-Blöcke nehmen.
  - Blockabbau ohne Drop: `level.setBlock(pos, AIR, 2)` nach `EventHooks.canEntityGrief`.
  - Die Tür-Navigation braucht `GroundPathNavigation.setCanOpenDoors` plus `BreakDoorGoal`, sonst geht der Effekt verloren.
  - Den Schwierigkeits-Bug (EASY-only) 1:1 übernehmen oder bewusst korrigieren und markieren.
  - Die Forschung sagt „very, very fast" und „puts out nearby torches"; beides stimmt mit dem Code überein.

### Alosaurus - Alosaurus (`alosaurus`)

- **Rolle:** feindseliger Dinosaurier, `extends EntityMob`, greift fast alles Lebende an.
- **Werte:**
  - XP 40 (Alosaurus.java:26); `fireResistance` 100 (Alosaurus.java:27); meidet Wasser (Alosaurus.java:25).
  - Bewegung 0.35 wird jeden Tick neu gesetzt (Alosaurus.java:54).
  - Keine Regeneration, keine Immunitäten.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMoveThroughVillage(1.0)`; 2 `MyEntityAIWanderALot(16, 1.0)`; 3 `EntityAIWatchClosest(EntityPlayer, 8)`; 4 `EntityAILookIdle`. Target 1 `EntityAIHurtByTarget(false)` (Alosaurus.java:29-34).
  - Mit 1/5 pro AI-Tick (Alosaurus.java:143):
    - Nächstes passendes Ziel in `expand(12,5,12)` (Alosaurus.java:200), Drehung 10°/10° (Alosaurus.java:146).
    - Bei Abstand² < (4 + Zielbreite/2)² `setAttacking(1)` und Angriff mit (1/4 oder 1/5) (Alosaurus.java:147-151), sonst Lauf mit 1.25 (Alosaurus.java:154).
    - `setAttacking(0)` nur ohne Ziel (Alosaurus.java:157-159).
  - Zielregeln: nicht `isIgnoreable`, nicht `Alosaurus`, `Cryolophosaurus`, `VelocityRaptor`; Sichtlinie; keine Creative-Spieler (Alosaurus.java:163-194).
  - Nahkampf: Vanilla-Schaden `Alosaurus_stats.attack`; Rückstoß 1.2 horizontal, 0.1 vertikal (0.2 für Spieler oder Tote) (Alosaurus.java:125-131).
- **Interaktion:** `interact` liefert false (Alosaurus.java:118-120).
- **Drops:** fest 10 `gold_nugget` (Alosaurus.java:107-109) und 6 `beef` (Alosaurus.java:110-112), verstreut ±3 (Alosaurus.java:102).
- **Spawnen:**
  - Spawner „Alosaurus" im Umkreis erlaubt immer (Alosaurus.java:223-237).
  - Sonst sind nötig: `isValidLightLevel` (Alosaurus.java:238), `posY >= 50` (Alosaurus.java:241), Nacht (Alosaurus.java:244), Luft auf y+1..5 im 2x2-Feld x/z -1..0 (Alosaurus.java:247-256) und kein weiterer Alosaurus in `expand(16,8,16)` (Alosaurus.java:257-259).
  - `canDespawn` = `!isNoDespawnRequired` (Alosaurus.java:49-51).
  - Spawnlisten: `ChunkProviderOreSpawn2` Monster 8, 1-2 (ChunkProviderOreSpawn2.java:357-359); `BiomeGenUtopianPlains.setChaosCreatures` Monster 1, 1-1 (BiomeGenUtopianPlains.java:334-336; nur Chaos-Dimension, WorldProviderOreSpawn6.java:27).
  - Etagen-Critter in `GenericDungeon.addLevelDecorations` (GenericDungeon.java:527ff). Das Manifest führt keine Spawns.
- **Zustand:** DW 20 `int` attacking (Alosaurus.java:46, 214-220), liest ModelAlosaurus für den Kiefer (ModelAlosaurus.java:163-168). Kein NBT.
- **Sounds:** `orespawn:alo_living` mit 1/4 (Alosaurus.java:75-78), `orespawn:alo_hurt` (Alosaurus.java:82), `orespawn:alo_death` (Alosaurus.java:86); Lautstärke 1.5 (Alosaurus.java:90).
- **Config:** `AlosaurusEnable` (OreSpawnMain.java:6054), `Alosaurus_stats` (OreSpawnMain.java:6145), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster`; die Spawnregel als `SpawnPlacements`-Prädikat mit Tagesprüfung über `level.isDay()`.
  - Die Nachbarzählung über `level.getNearestEntity(Alosaurus.class, …)` bzw. `getEntitiesOfClass`.
  - Das Flag `attacking` bleibt nach Ziel-Verlust aus Reichweite auf 1 stehen; das ist Originalverhalten (Kiefer klappt weiter).

### AttackSquid - Attack Squid (`attack_squid`)

- **Rolle:** feindseliger Wasser-Nahkämpfer mit Tintenschuss, `extends EntityMob` (kein `EntityWaterMob`), kann an Land laufen. Beschwört mit Glück den Kraken. Als Munition der SquidZooka ein kurzlebiges Geschoss.
- **Werte:**
  - XP 15 (AttackSquid.java:41); `fireResistance` 3 (AttackSquid.java:42).
  - Navigator meidet Wasser **nicht** (AttackSquid.java:40); Bewegung 0.25 wird jeden Tick gesetzt (AttackSquid.java:73).
  - `getAttackStrength` = 2 (AttackSquid.java:93-96) ist ein Überbleibsel; SRG-frei, in 1.7.10 von keiner Vanilla-Methode aufgerufen (Bedeutung aus Verwendung abgeleitet).
  - Aus einer SquidZooka: `wasshot = 250` (AttackSquid.java:68-70). Ablauf pro AI-Tick, bei 0 `setDead` ohne Drops (AttackSquid.java:481-487); solange > 0 kein Fallschaden (AttackSquid.java:350-355).
  - An Land ohne Wasser in Reichweite: mit 1/25 pro Prüfung `heal(-1)` = 1 LP Verlust, bei ≤ 0 `setDead` ohne Drops (AttackSquid.java:509-516).
  - Immun gegen Schaden, dessen `getEntity()` eine `AttackSquid`, `WaterBall` oder `WaterDragon` ist (AttackSquid.java:363-371).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `MyEntityAIWanderALot(16, 1.0)`; 2 `EntityAIWatchClosest(EntityPlayer, 8)`; 3 `EntityAILookIdle`. Target 1 `EntityAIHurtByTarget(false)` (AttackSquid.java:45-49).
  - Wassersuche außerhalb von Wasser mit 1/10 (AttackSquid.java:488):
    - Schalen mit Radius 1..11 um y-1, vertikal gedeckelt auf 5, ab Radius 5 jede zweite übersprungen (AttackSquid.java:494-505).
    - Geht mit 1.33 auf (tx, ty-1, tz) (AttackSquid.java:507).
  - Zielsuche mit 1/10 (AttackSquid.java:519):
    - Behält ein lebendes Ziel, sonst `expand(10,4,10)` sortiert (AttackSquid.java:628-644).
    - Bei Abstand² < 9 `setAttacking(1)` und Angriff mit (1/4 oder 1/5), Vanilla-Schaden `AttackSquid_stats.attack` (AttackSquid.java:522-527).
    - Sonst Lauf mit 1.2 und `watercanon` (AttackSquid.java:529-530).
    - Ohne Ziel folgt sie mit 1.0 einem `buddy` (AttackSquid.java:534-537).
  - `watercanon`, mit 1/5 pro Aufruf (AttackSquid.java:545):
    - Davon 1/3 `InkSack`, sonst `WaterBall` (AttackSquid.java:546, 558).
    - Start 1.2 vor dem Kopf und 1.0 hoch (AttackSquid.java:543-548); Zielhöhe +0.25 plus Bogenkorrektur `sqrt(dx²+dz²)*0.2` (AttackSquid.java:549-552).
    - Geschwindigkeit 1.4, Streuung 5.0 (AttackSquid.java:553, 564); Sound `random.bow` 0.75 (AttackSquid.java:554, 565).
    - Wirkung der Projektile: InkSack 1.0 Schaden (InkSack.java:51) und mit 1/2 Blindheit `100 + 50*rand(8)` Ticks (InkSack.java:62-63); WaterBall 2.0 Schaden (WaterBall.java:39).
  - Zielregeln, Reihenfolge wichtig (AttackSquid.java:571-622):
    - Sichtlinie nötig; Creative-Spieler ausgeschlossen.
    - `Girlfriend`, `Boyfriend`, `EntityZombie`, `EntityVillager`, `EntitySpider`, `EntityCaveSpider` und `Lizard` sind Ziele; `Ghost` und `GhostSkelly` nicht.
    - Eine andere `AttackSquid` wird mit 1/5 zum `buddy`, nie Ziel.
    - Alles andere nur, wenn `wasshot != 0`: geschossene Tintenfische greifen alles an.
  - Wird sie von einem `EntityLiving` getroffen (Spieler zählen nicht), wird dieser zum Ziel mit Lauf 1.2 (AttackSquid.java:372-383).
  - **Kraken-Ruf** (AttackSquid.java:385-389), alle Bedingungen zugleich:
    - Tod durch einen Treffer, dessen Quelle ein `EntityPlayer` ist
    - nicht in `DimensionID5` („Dimension-Crystal", manifest)
    - Server, 1/15 (`nextInt(15)==1`)
    - `KrakenEnable != 0` und `wasshot == 0`

    Dann spawnen 1..3 „The Kraken" auf **Y = 170** bei x/z ±3.
- **Interaktion:** `interact` liefert false (AttackSquid.java:342-344). SquidZooka: Spawn 2.5 Blöcke vor dem Spieler (+15° Yaw) auf Augenhöhe +1.65, Geschwindigkeit 3.6 plus Zufall ±0.05, `setWasShot()` (ItemSquidZooka.java:25-44).
- **Drops:**
  - Ein Wurf `nextInt(50)` (AttackSquid.java:145-332):

    | Wurf | Item |
    |---|---|
    | 0 | gold_nugget |
    | 1 | gold_ingot |
    | 2 | golden_carrot |
    | 3 | golden_sword mit je 1/6 sharpness, bane, knockback, looting, fireAspect (je 1..5), unbreaking 2..5 mit 1/2, zweites sharpness 1/6 |
    | 4, 6, 7 | golden_shovel, golden_axe, golden_hoe (unbreaking 1/2, efficiency 1/6) |
    | 5 | golden_pickaxe (plus fortune 1/6) |
    | 8 | golden_helmet (protection-Varianten je 1/6, unbreaking 1/2, respiration 1..2, aquaAffinity 1..5) |
    | 9, 10 | golden_chestplate, golden_leggings |
    | 11 | golden_boots (featherFalling 5..9 mit 1/6, unbreaking 1/2) |
    | 12 | golden_apple |
    | 13 | gold_block |
    | 14 | golden_apple Meta 1 (verzaubert) |
    | 15..17 | dye Meta 0 (Tintenbeutel) |
    | 18..49 | nichts |

  - Dazu immer 1..3 `fish` (AttackSquid.java:334-336).
  - Stufen über dem Vanilla-Maximum sind Absicht des Originals (`addEnchantment` ohne Kappung).
  - `getDropItem` fish (AttackSquid.java:130) ist durch das überschriebene `dropFewItems` ohne Wirkung.
- **Spawnen:**
  - `getCanSpawnHere` ruft `super` auf, ignoriert das Ergebnis und verlangt nur `posY >= 50` und Tag (AttackSquid.java:665-668); keine Licht- oder Wasserprüfung in der Klasse.
  - `canDespawn` = `!isNoDespawnRequired` (AttackSquid.java:64-66).
  - Biome laut manifest (waterCreature).
  - Spawner in `GenericDungeon.makePlayPool` (GenericDungeon.java:1978).
- **Zustand:** DW 20 `int` attacking (AttackSquid.java:61, 647-653); NBT `WasShot` (int) (AttackSquid.java:657, 662).
- **Sounds:** kein Living-Sound (AttackSquid.java:98-100); `orespawn:squid_hurt` (AttackSquid.java:103); `orespawn:squid_death` (AttackSquid.java:107); Lautstärke und Tonhöhe 1.0.
- **Config:** `AttackSquidEnable` (OreSpawnMain.java:6089), `AttackSquid_stats` (OreSpawnMain.java:6182), `KrakenEnable` (OreSpawnMain.java:6098), `DimensionID5` (OreSpawnMain.java:1270), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit `WATER_CREATURE`-Spawnkategorie. Spawnprädikat selbst schreiben (Y ≥ 50 bezogen auf Originalhöhen, Tag). offen: ob die Y-Grenze an die 1.18+-Welthöhe angepasst wird.
  - Kraken-Spawn per `EntityType.create` statt Namenssuche; Y = 170 wörtlich.
  - Verzauberungen sind in 1.21.1 datengetrieben: `ItemEnchantments.Mutable` mit `Holder<Enchantment>` aus `registryAccess().lookupOrThrow(Registries.ENCHANTMENT)`; Stufen über dem Maximum sind damit weiter setzbar.
  - `heal(-1)` durch `setHealth(getHealth()-1)` ersetzen, weil `heal` negative Werte nicht garantiert durchreicht.
  - Die Forschung nennt „temporary blindness" als Mob-Fähigkeit; im Code kommt sie nur über den InkSack-Schuss (InkSack.java:63), nicht aus dem Nahkampf.

### BandP - Criminal (`criminal`)

- **Rolle:** feindseliger Dieb („Politicians and Bankers"), `extends EntityMob`. Greift Spieler, Dorfbewohner, Girlfriend und Boyfriend an und stiehlt Spielern je Treffer ein Item.
- **Werte:**
  - XP **1000** (BandP.java:36); `fireResistance` 2 (BandP.java:37); meidet Wasser (BandP.java:35).
  - Bewegung 0.32 jeden Tick (BandP.java:64).
  - Eigenes Inventar mit 100 Plätzen (BandP.java:32).
  - Variante `whatami` = `nextInt(2)` beim ersten Server-Update (BandP.java:66-69). Sie ist nicht in NBT gesichert und wird nach dem Laden neu gewürfelt.
  - Nahkampfschaden über Vanilla `attackEntityAsMob` = `BandP_stats.attack` (BandP.java:149-151).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAIMoveThroughVillage(0.5)`; 1 `MyEntityAIWanderALot(16, 0.5)`; 2 `EntityAIWatchClosest(EntityPlayer, 10)`; 3 `EntityAILookIdle`; 4 `EntityAIOpenDoor(true)`; 5 `EntityAIMoveIndoors` (BandP.java:39-44). **Kein** `EntityAISwimming`, keine Target-Tasks.
  - Mit 1/12 pro AI-Tick (`nextInt(12)==1`; BandP.java:158):
    - Ziel in `expand(20,6,20)` sortiert (BandP.java:233); Ziele sind nicht-kreative Spieler, `EntityVillager`, `Girlfriend`, `Boyfriend`, Sichtlinie nötig (BandP.java:209-227).
    - Drehung 10°/10° (BandP.java:161).
    - Bei Abstand² < 9 Angriff ohne Zufallswurf (BandP.java:162-163), sonst Lauf mit 1.25 (BandP.java:203).
  - **Diebstahl** bei jedem solchen Angriff auf einen Spieler (BandP.java:164-200). Das Ergebnis des Angriffs zählt nicht.
    - Ist ein freier Inventarplatz da, nimmt er das Rüstungsteil mit dem höchsten Index (Index 3 = Helm zuerst).
    - Ohne Rüstung nimmt er den höchstbelegten Hauptinventar-Platz.
    - Jeder Diebstahl erhöht `got_stuff` um 1.
- **Interaktion:** `interact` liefert false (BandP.java:145-147).
- **Drops:**
  - 10..14 `emerald` (BandP.java:126-128).
  - Nur bei Variante 0 zusätzlich je 2..4 `UraniumNugget` und `TitaniumNugget` (BandP.java:129-134).
  - Alle gestohlenen Stapel: Item und Anzahl; Schadenswert nur bei Stapelgröße 1 (BandP.java:135-142). NBT, also Verzauberungen und Namen, geht verloren, weil `dropItemRand` einen neuen `ItemStack` baut (BandP.java:117).
  - `getDropItem` emerald (BandP.java:109) ohne Wirkung.
- **Spawnen:**
  - Spawner „Criminal" im Umkreis erlaubt immer (BandP.java:255-269).
  - Sonst sind nötig: Tag (BandP.java:270), **`posY >= 100`** (BandP.java:276; die Prüfung auf 50 davor ist überflüssig), kein weiterer BandP in `expand(32,12,32)` (BandP.java:279-283) und ein `EntityVillager` in `expand(36,12,36)` (BandP.java:284-286).
  - `canDespawn` nur wenn `!isNoDespawnRequired && got_stuff == 0` (BandP.java:59-61): ein Dieb mit Beute bleibt.
  - Biome laut manifest (ambient).
  - `BiomeGenUtopianPlains.setVillageCreatures` Höhlenliste 15, 1-2 (BiomeGenUtopianPlains.java:249-251; VillageMania-Dimension, WorldProviderOreSpawn3.java:27).
  - Vier Spawner in `GenericDungeon.makeWhiteHouse` (GenericDungeon.java:5692-5725).
- **Zustand:**
  - DW 20 `int` „what" = Variante 0/1 (BandP.java:56, 246-252). Weder ModelBandP noch RenderBandP lesen den Wert (grep), er steuert nur die Drops.
  - NBT `GotStuff` (int) immer; `Inventory` (Liste von Compounds mit Byte `Slot` plus ItemStack) nur bei `got_stuff != 0` (BandP.java:289-328).
- **Sounds:** Vanilla `mob.villager.idle`/`hit`/`death` (BandP.java:88-98), Lautstärke 1.5 (BandP.java:101).
- **Config:** `CriminalEnable` (OreSpawnMain.java:6060), `BandP_stats` (OreSpawnMain.java:6152), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit `SimpleContainer(100)` oder `NonNullList<ItemStack>`; NBT über `ContainerHelper`/`ItemStack.save(provider)`.
  - Die 1.21.1-Rüstungsliste hat dieselbe Reihenfolge (Index 3 = Kopf). Die Offhand existiert im Original nicht; ob sie gestohlen wird: offen, Designentscheidung.
  - Beim Drop die Komponenten verwerfen (1:1) oder erhalten (Bugfix) und das markieren.
  - `posY >= 100` trifft in der 1.18+-Überwelt nur Bergdörfer. offen: Höhenanpassung oder wörtlich übernehmen.
  - Ohne Schwimm-Goal kann er ertrinken; das ist Originalverhalten.
  - Die Variante sollte ins NBT, damit Drops nach dem Laden stabil bleiben (Bugfix, markieren).

### Baryonyx - Baryonyx (`baryonyx`)

- **Rolle:** friedlicher Dinosaurier, `extends EntityAnimal`, züchtbar, frisst Gras.
- **Werte:**
  - XP 5 (Baryonyx.java:32); `fireResistance` 100 (Baryonyx.java:31); meidet Wasser (Baryonyx.java:33); kann nicht unter Wasser atmen (Baryonyx.java:68-70).
  - Lebenspunkte fest 40, nicht konfigurierbar (Baryonyx.java:72-74).
  - `attackDamage` 8.0 wird registriert (Baryonyx.java:47-48), aber keine AI greift an. Effektiv macht er keinen Schaden; die Forschung sagt „Attack 0", das stimmt im Ergebnis.
  - Bewegung 0.25 jeden Tick (Baryonyx.java:60).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMate(1.0)`; 2 `EntityAIAvoidEntity(EntityMob, 8, 1.0, 1.4)`; 4 `EntityAIPanic(1.5)`; 5 `EntityAIWatchClosest(EntityPlayer, 12)`; 6 `MyEntityAIWander(1.0)`; 7 `EntityAILookIdle` (Baryonyx.java:34-40).
  - `updateAITick`: mit 1/200 `setRevengeTarget(null)` (Baryonyx.java:193-195).
  - Grasfressen mit 1/60 bei `PlayNicely == 0` (Baryonyx.java:196):
    - Schalen mit Radius 1..10 um y+1, vertikal gedeckelt auf 2, ab Radius 6 jede zweite übersprungen, gesucht wird `Blocks.grass` (Baryonyx.java:202-213).
    - Geht mit 1.0 hin (Baryonyx.java:215).
    - Bei Abstand² < 12 wird mit `mobGriefing` das Gras zu `dirt`; unabhängig davon heilt er 1 LP und spielt `random.burp` (Baryonyx.java:216-222).
- **Interaktion:**
  - Zucht mit `MyCrystalApple` (`isBreedingItem`; Baryonyx.java:247-249).
  - `isWheat(apple)` (Baryonyx.java:243-245) ist keine 1.7.10-Vanilla-Methode und damit toter Code (aus Verwendung abgeleitet).
  - Kind: `new Baryonyx` (Baryonyx.java:235-241).
- **Drops:** 2..6 `beef` über `dropItem` (Baryonyx.java:96-103).
- **Spawnen:**
  - `posY >= 50`, Tag und höchstens 8 Baryonyx in `expand(20,10,20)`; die Zählung schließt sich selbst ein (Baryonyx.java:55-57, 251-254).
  - `canDespawn`: Kinder werden persistent (`func_110163_bv` = `enablePersistence`) und nie entfernt, Erwachsene gemäß `!isNoDespawnRequired` (Baryonyx.java:227-233).
  - Spawnlisten: `ChunkProviderOreSpawn2` Ambient 2, 4-8 (ChunkProviderOreSpawn2.java:402-404); `BiomeGenUtopianPlains.setChaosCreatures` Höhlenliste 2, 2-4 (BiomeGenUtopianPlains.java:293-295).
  - Das Manifest führt keine Spawns.
- **Zustand:** keine DataWatcher-Einträge über EntityAnimal hinaus; Alter über `EntityAgeable`. Kein eigenes NBT.
- **Sounds:** kein Living-Sound; `orespawn:duck_hurt` für Hurt **und** Death (Baryonyx.java:80-86); Lautstärke 0.4 (Baryonyx.java:88-90); Vanilla `random.burp` beim Fressen.
- **Config:** `BaryonyxEnable` (OreSpawnMain.java:6068), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Animal` mit `isFood(ItemStack)` = Crystal Apple; `getBreedOffspring` statt `createChild`.
  - Grasumwandlung auf `Blocks.GRASS_BLOCK` → `Blocks.DIRT`.
  - Kinder im Renderer halb skaliert (manifest `gl_scale`).
  - Das Despawnen erwachsener Tiere verlangt `removeWhenFarAway` = true; bei 1.21.1-`Animal` ist das per Default false, also überschreiben.

### Basilisk - Basilisk (`basilisk`)

- **Rolle:** feindseliger Boss-artiger Mob (Schlange), `extends EntityMob`. Blick verlangsamt stark, Biss vergiftet; Unverwundbarkeitsfenster nach jedem Treffer.
- **Werte:**
  - XP 150 (Basilisk.java:30); `fireResistance` 2000 und feuerimmun (Basilisk.java:31-32).
  - Sprung zusätzlich `motionY += 0.25` (Basilisk.java:75-78); Bewegung 0.4 jeden Tick (Basilisk.java:59).
  - **Treffer-Sperre:** Jeder angenommene Schadensaufruf setzt `hurt_timer = 30`. Solange er > 0 ist, liefert `attackEntityFrom` false (Basilisk.java:339-345); heruntergezählt wird pro AI-Tick (Basilisk.java:352-354). Also ca. 30 Ticks ohne jeden weiteren Schaden.
  - Regeneration: 1 LP mit 1/200 pro Living-Tick (Basilisk.java:85-87) und 1 LP mit 1/75 pro AI-Tick unter Maximum (Basilisk.java:376-378).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMoveThroughVillage(1.0)`; 2 `MyEntityAIWanderALot(20, 1.0)`; 3 `EntityAIWatchClosest(EntityPlayer, 8)`; 4 `EntityAILookIdle`. Target 1 `EntityAIHurtByTarget(false)` (Basilisk.java:34-39).
  - Mit 1/5 pro AI-Tick (Basilisk.java:355):
    - Ziel in `expand(24,7,24)` sortiert (Basilisk.java:417); Drehung 10°/10°.
    - Bei Abstand² < (6 + Zielbreite/2)² `setAttacking(1)` und Angriff mit (1/3 oder 1/4) (Basilisk.java:359-364), sonst Lauf mit 1.25 (Basilisk.java:366).
    - **Blick:** jedes gefundene Ziel bekommt, egal wie weit entfernt, Slowness Stufe 5 (Amplifier 5) für 100 Ticks (Basilisk.java:368-370).
  - Zielregeln: nicht `isIgnoreable`; Sichtlinie; nicht `Basilisk` oder `LeafMonster`; keine Creative-Spieler. Sonst alles Lebende (Basilisk.java:381-411).
  - Nahkampf: Vanilla-Schaden `Basilisk_stats.attack`; Rückstoß 1.5 horizontal, 0.15 vertikal (0.3 für Spieler oder Tote) (Basilisk.java:326-332).
  - Gift mit 1/3, Amplifier 0, Dauer `var2*20` Ticks (Basilisk.java:323-325). `var2` = 8 auf Peaceful, EASY 10, NORMAL 12, HARD 14 (Basilisk.java:313-322); also 160/200/240/280 Ticks.
- **Interaktion:** `interact` liefert false (Basilisk.java:306-308).
- **Drops** (verstreut ±3; Basilisk.java:124):
  - je 1 `MyBasiliskScale` und `item_frame` (Basilisk.java:133-134)
  - 12..17 `emerald` (Basilisk.java:135-137)
  - 8..12 `chicken` (Basilisk.java:138-140)
  - 3..7 Würfe `nextInt(15)` (Basilisk.java:141-303):

    | Wurf | Item |
    |---|---|
    | 1 | emerald |
    | 2 | emerald_block |
    | 3 | MyEmeraldSword, Verzauberungen wie AttackSquid-Schwert |
    | 4, 6, 7 | MyEmeraldShovel, MyEmeraldAxe, MyEmeraldHoe |
    | 5 | MyEmeraldPickaxe |
    | 8..11 | EmeraldHelmet, EmeraldBody, EmeraldLegs, EmeraldBoots |
    | 0, 12..14 | nichts |

  - `getDropItem` beef (Basilisk.java:118) ohne Wirkung.
- **Spawnen:**
  - Spawner „Basilisk" im Umkreis erlaubt immer (Basilisk.java:439-453).
  - Sonst sind nötig: `isValidLightLevel` (Basilisk.java:454), Nacht (Basilisk.java:457), 3x3 Luft auf y+1..4 (Basilisk.java:460-469) und kein weiterer Basilisk in `expand(20,6,20)` (Basilisk.java:470-472).
  - `canDespawn` = `!isNoDespawnRequired` (Basilisk.java:54-56).
  - Biome laut manifest (ambient); `BiomeGenUtopianPlains.setChaosCreatures` Monster 1, 1-1 (BiomeGenUtopianPlains.java:337-339).
  - Struktur `BasiliskMaze`: drei Basilisken nebeneinander (BasiliskMaze.java:409-419).
  - Etagen-Critter in `GenericDungeon.addLevelDecorations` (GenericDungeon.java:624, 657, 694, 733) und `addLevelDecorationsQ` (GenericDungeon.java:6822-6931).
- **Zustand:** DW 20 `int` attacking (Basilisk.java:51, 430-436), liest ModelBasilisk für den Kiefer (ModelBasilisk.java:184-189). Kein NBT.
- **Sounds:** `orespawn:basilisk_living` mit 1/2 (Basilisk.java:95-98), `orespawn:alo_hurt` (Basilisk.java:102), `orespawn:emperorscorpion_death` (Basilisk.java:106); Lautstärke und Tonhöhe 1.0.
- **Config:** `BasiliskEnable` (OreSpawnMain.java:6077), `Basilisk_stats` (OreSpawnMain.java:6159), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster`. `hurt_timer` bleibt ein eigenes Feld in `hurt(DamageSource, float)`, zusätzlich zu Vanillas `invulnerableTime`.
  - Slowness über `MobEffects.MOVEMENT_SLOWDOWN` (1.21.1-Name).
  - Die Emerald-Ausrüstung sind eigene Items dieses Ports (andere Batches).
  - Der Blick wirkt ohne Nahkampfreichweite; nicht versehentlich an die Reichweite koppeln.

### Beaver - Beaver (`beaver`)

- **Rolle:** friedliches Tier, `extends EntityAnimal`. Flieht vor Mobs und Spielern, fällt Bäume und Holzbauten rekursiv, züchtbar.
- **Werte:**
  - XP 5 (Beaver.java:36); `fireResistance` 100 (Beaver.java:34); Navigator meidet Wasser nicht (Beaver.java:35); atmet unter Wasser (Beaver.java:250-252).
  - Lebenspunkte fest 15 (Beaver.java:254-256).
  - Bewegung: 0.15 im Feldinitialisierer, im Konstruktor mit 0.2 überschrieben (Beaver.java:26, 33); wirksam ist 0.2, jeden Tick gesetzt (Beaver.java:61). Das Manifest listet beide Werte.
  - `attackDamage` 1.0 wird registriert (Beaver.java:52-53), aber keine AI greift an.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`; 1 `EntityAIMate(1.0)`; 2 `EntityAIAvoidEntity(EntityMob, 8, 1.0, 1.5)`; 4 `EntityAIPanic(1.5)`; 5 `EntityAIAvoidEntity(EntityPlayer, 8, 1.0, 1.5)`; 6 `EntityAIWatchClosest(EntityPlayer, 6)`; 7 `MyEntityAIWanderALot(10, 1.0)`; 8 `EntityAILookIdle` (Beaver.java:38-45).
  - `updateAITick`: 1/200 `setRevengeTarget(null)` (Beaver.java:191-193).
  - **Holzfällen**, Auslöser: (1/30 und LP < 15) oder 1/350, jeweils bei `PlayNicely == 0` (Beaver.java:194):
    - Schalen mit Radius 1..10 um y+1, vertikal gedeckelt auf 2, ab Radius 6 jede zweite übersprungen (Beaver.java:200-211).
    - Holz im Sinne von `isWood`: `Blocks.log`, `OreSpawnMain.MyDT`, `OreSpawnMain.MySkyTreeLog`, `Blocks.fence`, `Blocks.fence_gate`, `Blocks.standing_sign` (Beaver.java:65-67).
    - Geht mit 1.0 hin (Beaver.java:214).
    - Bei Abstand² < 12 und `mobGriefing` wird der Zielblock **ohne Drop** entfernt, dann `breakRecursor` (Beaver.java:215-219).
    - `breakRecursor` entfernt rekursiv jeden Holzblock im 3x3x3-Nachbarfeld (ohne das Feld des Vorgängers) bis Rekursionstiefe 200. Jeder entfernte Block droppt als Item mit Meta 0 auf Höhe y+4..7 (Beaver.java:152-185).
    - Heilung 1 LP und Sound `orespawn:chainsaw` (Tonhöhe 0.9..1.1) passieren auch ohne `mobGriefing` (Beaver.java:220-221).
  - Kumpelsuche mit 1/200: nächster Beaver in `expand(16,6,16)` sortiert, Lauf mit 0.5 (Beaver.java:225-230). Da die Liste den Beaver selbst enthält und er bei Abstand 0 zuerst sortiert wird, läuft er meist zur eigenen Position (Beaver.java:234-244).
- **Interaktion:** Zucht mit `MyCrystalApple` (Beaver.java:313-315); `isWheat(apple)` ist toter Code (Beaver.java:309-311, aus Verwendung abgeleitet). Kind `new Beaver` (Beaver.java:301-307).
- **Drops:** `getDropItem` = `porkchop` (Beaver.java:278-280); `dropFewItems` ist nicht überschrieben, also Vanilla-Anzahl. offen: die Vanilla-Formel steht nicht im Quelltext dieses Mods. Nebenbei droppen die gefällten Holzblöcke (siehe oben).
- **Spawnen:**
  - `50 <= posY <= 100` und Block darunter `dirt`, `grass`, `tallgrass` oder `leaves` (Beaver.java:286-295); keine Licht- oder Tagesprüfung.
  - `canDespawn` false (Beaver.java:297-299).
  - Biome laut manifest (creature); `BiomeGenUtopianPlains.setChaosCreatures` Creature 1, 1-2 (BiomeGenUtopianPlains.java:317-319).
- **Zustand:** keine DataWatcher-Einträge über EntityAnimal hinaus, kein eigenes NBT.
- **Sounds:** kein Living-Sound; `orespawn:scorpion_hit` Hurt (Beaver.java:267); `orespawn:cryo_death` Death (Beaver.java:271); Lautstärke 0.4 (Beaver.java:275). Tonhöhe: Kind 1.5±0.1, erwachsen 1.0±0.1 (Beaver.java:282-284). `orespawn:chainsaw` beim Fällen.
- **Config:** `BeaverEnable` (OreSpawnMain.java:6129), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Animal`; `removeWhenFarAway` false.
  - Die Holzmenge 1:1: in 1.7.10 deckt `Blocks.log` nur Eiche, Fichte, Birke und Tropenholz ab (log2 fehlt), `fence`, `fence_gate` und `standing_sign` nur Eiche. Für einen 1:1-Port eine eigene Block-Tag-Datei `orespawn:beaver_wood` mit diesen Blöcken plus `MyDT` und `MySkyTreeLog`, nicht `#minecraft:logs`.
  - Die Rekursion (bis 200 Ebenen, 26 Nachbarn) kann ganze Holzhäuser abreißen und läuft synchron im Server-Tick. Iterativ mit Warteschlange und Deckel umsetzen, damit kein StackOverflow entsteht; die Drops bekommen den Blockzustand ohne Metadaten.
  - `EventHooks.canEntityGrief` statt Gamerule-String.
  - Die Forschung nennt „forests, jungles"; die Spawnliste enthält keinen Dschungel (manifest), der Code gewinnt.

### Bee - Bee (`bee`)

- **Rolle:** feindseliger Flieger, `extends EntityMob` **ohne jede AI-Task**. Flug, Zielwahl und Angriff laufen komplett handgeschrieben in `updateAITasks`.
- **Werte:**
  - XP 25 (Bee.java:36); `fireResistance` 5 (Bee.java:38).
  - Bewegung 0.32 einmalig (Bee.java:45).
  - Kein Fallschaden (Bee.java:220-224); löst Druckplatten aus (Bee.java:226-228).
  - Schiebbar, schiebt aber selbst nichts: `collideWithEntity` ist leer (Bee.java:86-91).
  - Senkrechte Dämpfung `motionY *= 0.6` pro Tick (Bee.java:127).
  - **Wasserschaden:** im Wasser greift sie sich mit 1/4 pro Tick selbst an, mit vollem `Bee_stats.attack` und eventuell Gift (Bee.java:128-130).
- **KI und Angriffe:**
  - Festhänge-Erkennung: gleicher Block in x/z erhöht `stuck_count` (Bee.java:153-160).
  - Neues Flugziel, wenn `stuck_count > 50`, mit 1/300 oder bei Abstand² < 2.1 zum alten Ziel (Bee.java:164):
    - Versatz x/z je ±4..12, y -3..+2 (Bee.java:168-176).
    - Das Ziel muss Luft und per Raytrace ab Augenhöhe +0.75 sichtbar sein (Bee.java:141-143, 177-180); bis zu 50 Versuche (Bee.java:148, 181).
  - Sonst mit 1/15 (Bee.java:184):
    - Ziel = letzter Angreifer `rt`, falls lebend, sonst Suche in `expand(10,6,10)` sortiert (Bee.java:185-192, 306).
    - Mit Ziel `setAttacking(1)` und Flugziel auf Zielposition +1 y (Bee.java:193-195).
    - Bei Abstand² < 16 Stich (Bee.java:196-198).
  - Zielregeln: Sichtlinie; das Ziel darf nicht im Wasser sein; nicht-kreative Spieler, `EntityVillager`, `Girlfriend`, `Boyfriend` (Bee.java:279-300).
  - Stich: direkter Schaden `Bee_stats.attack` als Mob-Schaden (Bee.java:134), mit 1/3 Gift 50 Ticks Stufe 0 (Bee.java:135-137).
  - Steuerung pro AI-Tick (Bee.java:204-213):
    - `motionX/Z += (sign(d)*0.5 - motion)*0.3`
    - `motionY += (sign(dy)*0.7 - motionY)*0.2`
    - Yaw dreht um 1/4 der Differenz; `moveForward = 1`.
  - Wird sie getroffen: `rt` = Angreifer, Flugziel = dessen Position (Bee.java:230-238). Beim Selbstangriff im Wasser ist `rt` die Biene selbst.
- **Interaktion:** keine.
- **Drops:** je 2..11 `gold_nugget`, `MyButterCandy`, `yellow_flower` (Löwenzahn), `sugar` (Bee.java:106-119), verstreut ±3. `getDropItem` yellow_flower (Bee.java:97-99) ohne Wirkung.
- **Spawnen:**
  - In `DimensionID4` („Dimension-Islands") immer erlaubt (Bee.java:241-243).
  - Spawner „Bee" in x/z -2..1, y 0..4 erlaubt (Bee.java:244-258).
  - Sonst sind nötig: 3x3 Luft auf y+1..4 (Bee.java:259-268), `posY >= 50` und Tag (Bee.java:269). Keine Lichtprüfung.
  - `canDespawn` = `!isNoDespawnRequired` (Bee.java:54-56).
  - Biome laut manifest (ambient); `BiomeGenUtopianPlains.setChaosCreatures` Höhlenliste 2, 2-4 (BiomeGenUtopianPlains.java:296-298).
  - Spawner in `GenericDungeon.makeDungeon` (GenericDungeon.java:190), `makeBeeHive` (GenericDungeon.java:900) und `makeSmallBeeHive` (GenericDungeon.java:1484); Etagen „Bee" in `makeEnormousCastle` (GenericDungeon.java:334) und `makeEnormousCastleQ` (GenericDungeon.java:6524).
  - Für Islands ist keine Spawnliste mit Bee belegt (grep `Bee.class`).
- **Zustand:** DW 20 `int` attacking (Bee.java:51, 58-64). ModelBee schlägt damit die Flügel schneller: Faktor 0.11 statt 0.021, Amplitude 0.055 statt 0.023 (ModelBee.java:189-194). Kein NBT, `currentFlightTarget` und `rt` sind flüchtig.
- **Sounds:** `orespawn:Beebuzz` als Living-Sound (Datei `beebuzz`, manifest; Bee.java:75); `orespawn:dragonfly_hurt` Hurt (Bee.java:79); `orespawn:alo_death` Death (Bee.java:83); Lautstärke 0.25 (Bee.java:67).
- **Config:** `BeeEnable` (OreSpawnMain.java:6105), `Bee_stats` (OreSpawnMain.java:6138), `DimensionID4`, `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit leerem `registerGoals`; Flug in `customServerAiStep` über `setDeltaMovement`. Schwerkraft bleibt an, das Original dämpft nur.
  - Kein `FlyingMoveControl`, sonst ändert sich das Flugbild.
  - `doPush` leer überschreiben; `isPushable` true.
  - Selbstschaden im Wasser mit `damageSources().mobAttack(this)`: in 1.21.1 prüfen, dass `hurt` die eigene Quelle nicht verwirft. Das Original setzt dabei `rt = this`.
  - Sound-Event-Id klein schreiben (`beebuzz`, manifest texture/sound map).
  - Die Forschung sagt „flies at you after being shot" und „[FW] nausea"; im Code greift sie Spieler auch unprovoziert an, und es gibt nur Gift, keine Übelkeit.

### BerthaHit - BerthaHit (`bertha_hit`)

- **Rolle:** unsichtbares Hilfsprojektil, `extends EntityThrowable`; dient als Reichweiten-Schlag der Bertha-Schwerter. Erzeugt von `Bertha.onEntitySwing` für die Items `berthasmall` (Big Bertha), `slicesmall` (Slice), `royalsmall` (Royal Guardian Sword) und `hammysmall` (Attitude Adjuster). Alle vier sind `Bertha`-Instanzen (OreSpawnMain.java:1313-1316; manifest). `Slice.java` wird von `MySlice` nicht instanziiert.
- **Werte:**
  - Größe 0.33 (BerthaHit.java:27).
  - Start im Konstruktor auf Augenhöhe, 0.16 seitlich und 0.1 tiefer versetzt (BerthaHit.java:28-32); Richtung aus Yaw/Pitch mit 0.4 (BerthaHit.java:34-37).
  - Danach `setThrowableHeading(..., func_70182_d(), 0.1)` (BerthaHit.java:38). `func_70182_d` hat im MCP-stable-12 kein Mapping; Bedeutung aus Verwendung: Wurfgeschwindigkeit. offen: Zahlenwert nicht im Mod-Quelltext.
  - `Bertha.onEntitySwing` (nur Server, Bertha.java:70) setzt die Position neu auf 2.0 Blöcke vor dem Spieler (`rotationYawHead`) und +1.55 hoch, **verdoppelt** die Geschwindigkeit (Bertha.java:72-81) und kostet 1 Haltbarkeit (Bertha.java:89).
  - Treffertyp: 0 für Bertha und Slice, 2 für `MyRoyal` (Bertha.java:82-84), 3 für `MyHammy` (Bertha.java:85-87).
- **KI und Angriffe (`onImpact`):**
  - Nur mit Entity-Treffer **und** vorhandenem Werfer (BerthaHit.java:59):
    - `Girlfriend` und `Boyfriend` werden **immer** verschont; Spieler nur bei `big_bertha_pvp == 0` (BerthaHit.java:61-64).
    - Gezähmte `EntityTameable` werden bei `big_bertha_pvp == 0` verschont (BerthaHit.java:65-71).
  - Schaden immer als `DamageSource.causePlayerDamage((EntityPlayer) thrower)`, nie gegen den Werfer selbst. Die Reichweite gilt zum Werfer:

    | Typ | Reichweite (Abstand²) | Schaden | Feuer | Rückstoß h / v | Explosion |
    |---|---|---|---|---|---|
    | 0 Bertha/Slice | < 81 (BerthaHit.java:72) | `bertha_stats.damage`, Default 496 (BerthaHit.java:73; OreSpawnMain.java:1176) | 10 s (BerthaHit.java:74) | 2.25 / 0.35 (BerthaHit.java:75-76) | nein |
    | 2 Royal | < 101 (BerthaHit.java:83) | `royal_stats.damage`, Default 746 (BerthaHit.java:84; OreSpawnMain.java:1184) | nein | 1.5 / 0.25 (BerthaHit.java:85-86) | nein |
    | 3 Attitude Adjuster | < 64 (BerthaHit.java:93) | `hammy_stats.damage`, Default 82 (BerthaHit.java:94; OreSpawnMain.java:1185) | nein | 1.25 / 0.65 (BerthaHit.java:95-96) | Stärke 1.5 am Projektil (BerthaHit.java:102-104) |

  - Vertikaler Rückstoß ×2, wenn das Ziel dabei gestorben ist (BerthaHit.java:78-80, 88-90, 98-100).
  - Ohne Entity-Treffer und bei Typ 3 innerhalb Abstand² < 64: Explosion Stärke 2.1 (BerthaHit.java:107-109).
  - Beide Explosionen: `newExplosion(null, …, flaming=true, smoking=mobGriefing)`. Sie setzen also immer Feuer und zerstören Blöcke nur mit `mobGriefing`.
  - Danach `setDead` (BerthaHit.java:110).
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur über die vier Schwerter. Tracking 64/1/Velocity an (manifest).
- **Zustand:** `hit_type` ist nur ein Serverfeld, weder synchronisiert noch in NBT (BerthaHit.java:12, 51-53). Nach einem Chunk-Reload ist der Typ 0.
- **Sounds:** keine.
- **Config:** `BigBerthaPvp` → `big_bertha_pvp`, Default 0 (OreSpawnMain.java:1151); `Bertha_damage` 496, `Royal_damage` 746, `Attitude_damage` 82 über `get_weaponstats` (OreSpawnMain.java:1176, 1184-1185; manifest config/tool_materials).
- **Portierung 1.21.1:**
  - `ThrowableProjectile`; Schaden über `damageSources().playerAttack(player)`.
  - Werfer-Typprüfung statt blindem Cast: das Original wirft `ClassCastException`, wenn der Werfer kein Spieler ist (BerthaHit.java:73), und eine NPE im `else if`, wenn eine Entity getroffen wird, der Werfer aber null ist (BerthaHit.java:107).
  - Explosion über `level.explode(null, x, y, z, r, true, mobGriefing ? ExplosionInteraction.MOB : ExplosionInteraction.NONE)`.
  - Der Treffertyp sollte ins NBT (Bugfix, markieren).
  - Renderer: unsichtbar bzw. Spinner-Icon laut `RenderItemUrchin`-Zweig für `BerthaHit` (RenderItemUrchin.java:9; manifest `spinners.png`).
  - Das Item-Schwingen läuft in 1.21.1 nicht über `onEntitySwing`. Als Ersatz dient `Item.onEntitySwing` aus der NeoForge-`IItemExtension` (server-geprüft) oder ein `PlayerInteractEvent.LeftClickEmpty`-Paket. offen: welcher Hook den Leerschwung ohne Mixin serverseitig sicher erreicht, weil ein Luftschlag clientseitig beginnt und ein eigenes C2S-Paket braucht.
