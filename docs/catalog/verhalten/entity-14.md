# Verhalten: entity-14

Dieser Stapel umfasst sieben Klassen aus drei Themen. **Vortex** ist ein fliegender Crystal-/Chaos-Dimensions-Gegner, der Ziele ansaugt und anhebt. **Water Dragon** ist ein zähmbarer Wasserdrache mit Wasserball-Salven, und **WaterBall** ist sein Geschoss (auch vom Spieler, vom Dispenser, von AttackSquid und Dragon verschossen). **Whale** ist ein friedliches, züchtbares Wassertier. Dazu kommt die dreistufige **Worm**-Familie (Small/Medium/Large), die per `noClip` im Boden steckt, auftaucht und Rüstung klaut. Keiner der Werte liegt über den 1.21.1-Klemmen (Leben ≤ 150, Rüstung ≤ 14). Die Hauptrisiken des Ports sind die No-Clip-Grabmechanik der Würmer mit ihrer starren Blockliste (grass/dirt/stone), die nur clientseitig tickenden Effekte (Whale-Fontäne, Vortex-Rauch), die Geschwindigkeits-Synchronisierung beim Vortex-Sog und mehrere Stellen, an denen `setDead()` statt Tod ohne Drops entfernt. Alle Zahlen sind mit Quelle belegt; wo die Quelle nichts hergibt, steht „offen“.

Quellpfad: `reference/src-20.2/src/main/java/danger/orespawn/`. Die SRG-Namen `func_152115_b` (setOwner), `func_152114_e` (isOwner), `func_152113_b` (getOwner-String) und `func_145881_a` (Spawner-Logik) stehen nicht in `mcp/methods.csv`, ihre Bedeutung ist aus der Verwendung erschlossen. `func_110163_bv` = `enablePersistence` (methods.csv).

---

### Vortex - Vortex (`vortex`)

- **Rolle:** feindlicher Flieger, `EntityMob` (Vortex.java:15), kein Boss-Balken. Er hat Schwerkraft, hebt sich aber per Schubbewegung selbst. Feuerimmun (Vortex.java:32), `fireResistance = 250` (Vortex.java:33). Modell: eine einzige flache Ebene 128×64 (ModelVortex.java:17-18) ohne Animation.
- **Werte:**
  - XP 200 (Vortex.java:31).
  - Regeneration: 1 HP mit Chance 1/200 pro Tick (Vortex.java:104-105).
  - Rüstung = `Vortex_stats.defense` über `getTotalArmorValue` (Vortex.java:212-214); Leben/Angriff: (manifest) 150/26, Rüstung 10.
  - Vertikale Dämpfung `motionY *= 0.6` pro Tick (Vortex.java:86).
  - `winded`: nach jedem Treffer 20 Ticks ohne Sog (Vortex.java:208, 139-141).
  - Kein Fallschaden (Vortex.java:191-195), löst keine Druckplatten aus (Vortex.java:197-199), `canBePushed` true, aber `collideWithEntity` leer (Vortex.java:68-73).
- **KI und Angriffe:** keine `tasks`, alles läuft in `updateAITasks` (jeden Tick, `isAIEnabled` true, Vortex.java:79-81).
  - Flugziel: Neuwahl mit Chance 1/300 pro Tick oder bei Abstand² < 2.1 zum alten Ziel (Vortex.java:142). Bis zu 50 Versuche: x/z-Versatz ±(10..23) (Vortex.java:144-150), y ±3 (`rand(6)-3`, Vortex.java:152). Angenommen wird ein Luftblock mit freier Sichtlinie (`canSeeTarget`, Raytrace ab `posY+0.75`, Vortex.java:123-125, 154). Scheitern alle 50 Versuche, bleibt das letzte (evtl. feste) Ziel stehen.
  - Bewegung: `motionX/Z += (signum*0.4 - motion)*0.2`, `motionY += (signum*0.7 - motionY)*0.2` (Vortex.java:178-180); Gier dreht 1/4 der Differenz pro Tick zur Flugrichtung (Vortex.java:181-184); `moveForward = 0.75` (Vortex.java:183).
  - Zielsuche `findSomethingToAttack`: `PlayNicely != 0` liefert null (Vortex.java:289). Die Box ist `expand(16,10,16)` (Vortex.java:292), sortiert mit `GenericTargetSorter` (Abstand², Creeper halbiert, geteilt durch `height*width` falls > 1; GenericTargetSorter.java:15-35). Das Ziel muss lebendig sein, nicht `MyUtils.isIgnoreable`, sichtbar per `EntitySenses.canSee` und kein Kreativspieler. Ausgeschlossen sind Vortex, Rotator, Mothra, Brutalfly, Peacock, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin (Vortex.java:262-286). Damit sind **alle** anderen Lebewesen Ziele, auch Tiere.
  - Hat er ein Ziel: Flugziel = Zielposition (Vortex.java:161).
  - **Sog:** bei Abstand² < 81 (9 Blöcke) und `winded == 0` bekommt das Ziel pro Tick einen Geschwindigkeitsimpuls **zum Vortex hin**. Horizontal `(10 - d) * 0.1`, vertikal `(10 - d) * 0.05 * pm`, `pm = 2` für Spieler, sonst 1 (Vortex.java:163-170).
  - **Nahkampf:** bei Abstand² < (4 + Zielbreite/2)² und Chance 1/8 pro Tick `attackEntityAsMob`, also Vanilla-`EntityMob`-Treffer mit Attribut attackDamage (Vortex.java:171-173).
  - Wird er getroffen, wird der Angreifer zum Flugziel (Vortex.java:205-207).
  - `findSomethingToAttack` läuft pro Tick zweimal (onUpdate und updateAITasks) und auch auf dem Client (Vortex.java:88, 159).
- **Interaktion:** keine eigene. Mit der CritterCage fangbar unter dem Namen „Vortex“ (CritterCage.java:407). Die Girlfriend-Overlay-GUI zeigt den Namen „Vortex“ (GirlfriendOverlayGui.java:290-292).
- **Drops:** unabhängig von `recentlyHit` und Looting.
  - Immer 1× `vortexeye` und 1× `minecraft:item_frame` (Vortex.java:318-319).
  - Dazu 5 + rand(7) = 5..11 Würfe mit rand(10) (Vortex.java:320-346): 0 `stick`, 1 `tigerseye_ingot`, 2 `crystalpink_ingot`, 3 `iron_ingot`, 4 `uranium_nugget`, 6 `titanium_nugget`, 7 `deadirukandji`, 8 Block `crystalcoal`; bei 5 und 9 nichts.
  - Streuung x/z ±5, y +1..+10 (Vortex.java:310).
  - `getDropItem` = `eggfairy` (Vortex.java:349-351) ist toter Code, weil `dropFewItems` überschrieben ist.
  - XP 200 nach Vanilla-Regel.
- **Spawnen:** `getCanSpawnHere` (Vortex.java:216-260):
  1. Liegt im Kasten x/z −3..+2, y 0..4 ein Spawner mit „Vortex“: `was_spawnered = 1`, erlaubt (Vortex.java:217-232).
  2. Sonst: 5×5-Säule y+1..y+3 muss komplett Luft sein (Vortex.java:233-242); `isValidLightLevel` (Vortex.java:243); `posY >= 50` (Vortex.java:246); Nacht, also `worldTime % 24000 >= 12000` (Vortex.java:249-253); 50 % Chance (Vortex.java:254); kein anderer Vortex in `expand(20,16,20)` (Vortex.java:258).
  - Natürlich nur in OreSpawn-Dimensionen, keine `addSpawn`-Einträge (manifest spawns leer): `BiomeGenUtopianPlains.setCrystalCreatures` monster Gewicht 3, 1-2 (BiomeGenUtopianPlains.java:146) und `setChaosCreatures` Gewicht 1, 1-2 (BiomeGenUtopianPlains.java:326).
  - Bauwerke: `makeCrystalBattleTower` setzt 2 Vortex-Spawner auf y+22/+23 über der Truhe (GenericDungeon.java:4975-4985); `makeEnormousCastleQ` → `buildLevelQ(..., "Vortex", ...)` (GenericDungeon.java:6540).
  - Crystal-Dried-Egg-Block Fall 7 (ChunkProviderOreSpawn5.java:652).
  - Despawn: `canDespawn` nur ohne Persistenz, ohne Kampf und ohne Spawner-Herkunft (Vortex.java:44-46). Zusätzlich tagsüber (`t%24000 < 12000`) mit Chance 1/500 pro Tick `setDead()` unter denselben Bedingungen (Vortex.java:107-120).
- **Zustand:** kein DataWatcher, kein NBT. `busy_fighting` und `was_spawnered` sind reine Felder, `was_spawnered` geht beim Neuladen verloren.
- **Sounds:** Living `orespawn:vortexlive`, Tod `orespawn:vortexlive`, Hurt keiner (Vortex.java:56-66); Lautstärke 0.75, Pitch 1.0 (Vortex.java:48-54).
- **Config:** `Vortex_health`/`_attack`/`_defense` über `OreSpawnMain.Vortex_stats` (Vortex.java:41, 76, 213); `VortexEnable` (Biom-Listen); `PlayNicely` (Vortex.java:289); `AllMobsDisable` setzt `VortexEnable = 0` (OreSpawnMain.java:1258, 5863).
- **Portierung 1.21.1:**
  - `Monster`, `EntityType.Builder.fireImmune()`, `xpReward = 200`.
  - Attribute: MAX_HEALTH 150, ATTACK_DAMAGE 26, ARMOR 10 (alle unter der Klemme).
  - Flug in `customServerAiStep` mit `setDeltaMovement` 1:1 nachbauen, Gravitation lassen. `moveForward` → `zza`.
  - Sog: `target.push(...)` plus **`hurtMarked = true`**, sonst sieht der Client-Spieler keinen Impuls. Ob 1.7.10 den Impuls ohne gleichzeitigen Treffer an Spieler synchronisierte: offen.
  - Rauchpartikel (20 pro Tick, Vortex.java:91-101) brauchen einen synchronisierten Boolean „hat Ziel“ (SynchedEntityData), statt die Zielsuche auf dem Client laufen zu lassen.
  - Spawner-Scan ersetzen durch `MobSpawnType.SPAWNER` in `checkSpawnRules`/`finalizeSpawn`. `was_spawnered` dabei setzen und ins NBT schreiben (Original speichert es nicht, dokumentieren).
  - Tageszeit: `level.getDayTime() % 24000`.
  - Die Ausschlussliste referenziert elf Klassen anderer Stapel.

### WaterBall - WaterBall (`water_ball`)

- **Rolle:** Wurfgeschoss, `EntityThrowable` (WaterBall.java:10). Konstruktoren: Welt; Welt + Werfer (Item `waterball` „WaterDragon Charge“, ItemWaterBall.java:24); Welt + x/y/z (Dispenser, MyDispenserBehaviorWDCharge.java:10). WaterDragon, AttackSquid und Dragon nutzen den x/y/z-Konstruktor und setzen danach Position und Richtung neu, **ohne Werfer**.
- **Werte:**
  - Schaden 2.0 (WaterBall.java:39), gegen `EntityCreeper` 5.0 (WaterBall.java:40-42).
  - Schadensquelle `causeThrownDamage(ball, thrower)` (WaterBall.java:61).
  - Chance 1/10, dass das getroffene Wesen 1× `waterball` fallen lässt (WaterBall.java:62-64); das Ziel wird gelöscht (`extinguish`, WaterBall.java:65).
  - Schwerkraft/Geschwindigkeit sind Vanilla-`EntityThrowable`-Standard, nicht überschrieben. Aus WaterDragon: Speed 1.4, Streuung 5.0 (WaterDragon.java:666).
  - Sprite-Index `my_index = 49` (WaterBall.java:18) für `RenderItemUrchin` (RenderItemUrchin.java:16-18), Skalierung 0.5 (manifest).
  - Tracking 64/1/true (OreSpawnMain.java:3072).
- **KI und Angriffe:** `onImpact` (WaterBall.java:37-75).
  - **Kein Schaden und kein Entfernen** (früher `return`, der Ball fliegt weiter) bei Treffer auf: `WaterDragon` (WaterBall.java:43-45), `AttackSquid` (WaterBall.java:46-48), `Dragon` mit `getDragonType() != 0` (WaterBall.java:49-54), Spieler mit `ridingEntity != null` (WaterBall.java:55-60).
  - Sonst und bei Blocktreffern: je 8× Partikel `bubble` und `splash` (WaterBall.java:67-70), Sound `random.splash` 0.5, Pitch 1 ± 0.25 (WaterBall.java:71), serverseitig `setDead` (WaterBall.java:72-74).
  - Keine Blockwirkung (kein Wasser, kein Feuer löschen).
  - `onUpdate`: Pitch +30° pro Tick (WaterBall.java:79-85), 1 `splash`-Partikel pro Tick (WaterBall.java:86).
- **Interaktion:** keine.
- **Drops:** keine eigenen; siehe 1/10-Drop am Ziel.
- **Spawnen:** nur als Geschoss.
- **Zustand:** kein DataWatcher, kein NBT; `my_rotation` nur lokal.
- **Sounds:** `random.splash` beim Aufprall. Der `random.bow` beim Abschuss kommt vom Schützen bzw. vom Item (ItemWaterBall.java:22).
- **Config:** keine.
- **Portierung 1.21.1:**
  - `ThrowableItemProjectile` mit `getDefaultItem()` = `waterball`, Schaden über `damageSources().thrown(this, getOwner())`. Creeper-Sonderfall behalten.
  - Das „Durchfliegen“ immuner Ziele sauber über `canHitEntity` nachbilden, statt in `onHitEntity` früh zurückzukehren.
  - Schützen, die das Original ohne Werfer erzeugt, sollten `setOwner` bekommen (sonst fehlt Kill-Credit, wie im Original). Abweichung dokumentieren.
  - Renderer clientseitig: Sprite aus `spinners.png`, Index 49, Maßstab 0.5 (kein ThrownItemRenderer, 1:1 eigener Renderer).
  - Partikel: `ParticleTypes.BUBBLE`/`SPLASH`, Sound `SoundEvents.GENERIC_SPLASH` (Zuordnung zu `random.splash` noch prüfen).
  - `Dragon.getDragonType()`: Bedeutung von Typ 0 offen (Dragon-Stapel).

### WaterDragon - Water Dragon (`water_dragon`)

- **Rolle:** zähmbarer, amphibischer Drache, `EntityTameable` (WaterDragon.java:19). Wild greift er Spieler an, zahm nur `EntityMob`. Züchtbar. Feuerimmun (WaterDragon.java:47), `fireResistance = 3` (WaterDragon.java:46), atmet unter Wasser (WaterDragon.java:795-797), `setAvoidsWater(false)` (WaterDragon.java:44).
- **Werte:**
  - XP 100 (WaterDragon.java:45).
  - Tempo: im Wasser 0.55, an Land 0.25 (WaterDragon.java:206-213), vor jedem Tick ins Attribut geschrieben (WaterDragon.java:175).
  - Leben/Angriff/Rüstung (manifest) 150/20/8; Rüstung über `getTotalArmorValue` (WaterDragon.java:198-200).
  - Eigene Unverwundbarkeit: Schaden nur wenn `hurt_timer <= 0`, danach `hurt_timer = 10` Ticks (WaterDragon.java:481-484, 587-589). Die Vanilla-`hurtResistantTime` kommt hinzu.
  - Regeneration im Wasser: Chance 1/100 pro Tick +1 HP, nur wenn verletzt (WaterDragon.java:643-646).
  - Außerhalb von Wasser ohne Wasser in Reichweite: Chance 1/25 × 1/50 = 1/1250 pro Tick −1 HP über `heal(-1)`. Bei ≤ 0 HP `setDead()`, also **ohne Drops** (WaterDragon.java:590-619).
  - Nahkampf: `attackEntityAsMob` macht **fest** `WaterDragon_stats.attack` als `causeMobDamage` (ignoriert Attribut und Verzauberungen). Rückstoß horizontal 1.1 in Blickrichtung zum Ziel, vertikal 0.14, verdoppelt auf 0.28, wenn das Ziel tot oder ein Spieler ist (WaterDragon.java:449-464).
  - `getAttackStrength` (WaterDragon.java:220-232) ist toter Code (kein Aufrufer, verschachteltes if nie wahr).
  - Baby: greift nicht an (WaterDragon.java:716-718), halber Render-Maßstab (RenderWaterDragon.java:35-36).
- **KI und Angriffe:**
  - `tasks` (WaterDragon.java:50-56): 0 `EntityAISwimming`; 1 `EntityAIMate(1.0)`; 2 `MyEntityAIFollowOwner(speed 2.0, maxDist 10, minDist 2)`; 3 `EntityAITempt(1.2, Items.fish, false)`; 4 `MyEntityAIWanderALot(16, 1.0)`; 5 `EntityAIWatchClosest(EntityPlayer, 8)`; 6 `EntityAILookIdle`.
  - `targetTasks`: 1 `EntityAIHurtByTarget(false)` (WaterDragon.java:57).
  - FollowOwner startet, wenn nicht sitzend und ((posY < 60 oder Nacht) und Abstand > 5) oder Abstand ≥ 10 (MyEntityAIFollowOwner.java:38). Neuer Pfad alle 10 Ticks. Teleport auf festen Boden um den Besitzer, wenn der Pfad scheitert und Abstand² ≥ 144 (MyEntityAIFollowOwner.java:72-86). Stopp bei ≤ 2.
  - WanderALot: Chance 1/30, xz-Reichweite 16, y 7, nicht beim Sitzen (MyEntityAIWanderALot.java:32-49).
  - **Wassersuche** (`updateAITasks`): an Land, Chance 1/25 pro Tick, nicht sitzend. Hohlwürfel-Schalen mit i ∈ {1,2,3,4,5,7,9,11}, dy = min(i,10), nächster `water`/`flowing_water`-Block ab `posY-1`. Danach `tryMoveToXYZ(tx, ty-1, tz, 1.33)` (WaterDragon.java:590-609, Scan 499-580).
  - Chance 1/200 pro Tick: `setAttackTarget(null)` (WaterDragon.java:621-623).
  - **Kampfschleife:** nicht Peaceful, Chance 1/5 pro Tick (WaterDragon.java:624-642).
    - `findSomethingToAttack`: `PlayNicely` oder Baby → null. Ein vorhandenes lebendes `getAttackTarget` hat Vorrang. Sonst die Box `expand(14,4,14)`, sortiert mit `GenericTargetSorter` (WaterDragon.java:712-737).
    - `isSuitableTarget`: nicht Peaceful, lebendig, sichtbar, kein WaterDragon. `EntityMob` → immer ja, auch zahm. Zahm → sonst nein. Spieler → ja, wenn nicht Kreativ. Sonst `MyUtils.isAttackableNonMob`: EntityMob, Mothra, Leon, Dragon, Spyro, Royalty, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, EntityVillager, Stinky (WaterDragon.java:679-710; MyUtils.java:14).
    - Ziel da: `faceEntity(10,10)`. Bei Abstand² < (4 + Breite/2)²: `setAttacking(1)`, Biss mit `rand(4)==0 || rand(5)==1` = 40 % (WaterDragon.java:628-633). Sonst `tryMoveToEntityLiving(e, 1.0)` + `watercanon` (WaterDragon.java:634-637). Kein Ziel: `setAttacking(0)`.
  - **Wasserkanone** `watercanon` (WaterDragon.java:649-677):
    - Salve von 8 Schuss (`stream_count = 8`); Start mit Chance 1/4 pro Aufruf, wenn leer (WaterDragon.java:674-676).
    - Pro Aufruf mit `stream_count > 0`: `setAttacking(2)`, 1× WaterBall von Kopfposition (xz-Versatz 1.5, y +1.75), Ziel auf `posY+0.25`. Bogen `+ sqrt(dx²+dz²)*0.2`, Speed 1.4, Streuung 5.0, Sound `random.bow` 0.75 (WaterDragon.java:660-669).
    - Zusätzlich Chance 1/15: `EntitySmallFireball` mit Drachen als Schütze (WaterDragon.java:654-659).
    - Achtung: der Fireball nutzt `rotationYawHead` für x **und** z, der WaterBall `rotationYawHead` für x und `rotationYaw` für z (WaterDragon.java:656 vs. 661).
  - `attackEntityFrom` (WaterDragon.java:466-497):
    - Immun gegen `cactus`.
    - Quellen mit `getEntity()` WaterDragon/AttackSquid/WaterBall → false. Der WaterBall-Zweig ist tot: `getEntity()` liefert bei Wurfschaden den Werfer, nicht den Ball.
    - Ein `EntityLiving`-Angreifer (Mob, **nicht Spieler**) wird Ziel und angelaufen (Speed 1.2), auch wenn `hurt_timer` den Schaden blockt. Spieler als Ziel kommen über `EntityAIHurtByTarget`.
  - Es gibt **keinen** `EntityAISit`-Task. Sitzen blockiert nur FollowOwner, WanderALot, Wassersuche und die Flossen-Animation, nicht Kampf oder Navigation aus `updateAITasks`.
  - Tempt mit Fisch hält ihn nicht vom Angriff ab. Die Kampfschleife ist taskunabhängig; die Recherche ([NW] „approach without attacking“) widerspricht dem Code.
- **Interaktion:** `interact` (WaterDragon.java:84-164).
  - Zuerst `super.interact`, also Vanilla-`EntityAnimal`: Züchten mit `crystalapple` über `isBreedingItem` (WaterDragon.java:791-793).
  - **Fisch** (`Items.fish`, alle Metadaten) bei Abstand² < 25:
    - wild: serverseitig 1/3 Zähmung (`setTamed`, Besitzer = UUID-String, Herzen, EntityState 7, volle Heilung), sonst Rauch (State 6) (WaterDragon.java:93-107).
    - zahm und Besitzer: volle Heilung (WaterDragon.java:109-117).
    - Fisch wird außer im Kreativmodus **immer** verbraucht, auch bei fremdem Besitzer ohne Wirkung (WaterDragon.java:118-125).
  - **Toter Busch** (`deadbush`), Besitzer, Abstand² < 25: Zähmung aufheben, Besitzer "" (WaterDragon.java:127-142).
  - **Namensschild**, Besitzer, Abstand² < 16: Name setzen, Schild verbrauchen (WaterDragon.java:143-153).
  - Sonst Besitzer, Abstand² < 25: Sitzen umschalten (WaterDragon.java:154-162).
  - Nachwuchs: `new WaterDragon`. Bei zahmem Elternteil setzt der Code den **eigenen** Besitzer neu (No-op) und das Baby auf `setTamed(true)` **ohne Besitzer** (WaterDragon.java:778-785). `isWheat` (WaterDragon.java:787-789) ist ein 1.6-Rest ohne Aufrufer.
  - CritterCage-Name „Water Dragon“ (CritterCage.java:263). Girlfriend-Overlay zeigt „WaterDragon“ und `getWaterDragonHealth()` (GirlfriendOverlayGui.java:212-220).
- **Drops:** `dropFewItems` (WaterDragon.java:268-447), Streuung x/z ±1, y +1 (WaterDragon.java:261).
  - Immer: 1× `waterdragonscale`, 1× `item_frame`, 9 + rand(6) = 9..14× `fish` (Meta 0) (WaterDragon.java:270-274).
  - Zusätzlich rand(20) (WaterDragon.java:275-446):

    | Wurf | Item | Verzauberung (je Zeile Chance, Stufe) |
    |---|---|---|
    | 0 | `ultimateaxe` | - |
    | 1 | `iron_ingot` | - |
    | 2 | `ultimatepickaxe` | - |
    | 3 | `iron_sword` | sharpness 1/6 1-5; bane_of_arthropods 1/6 1-5; knockback 1/6 1-5; looting 1/6 1-5; unbreaking 1/2 2-5; fire_aspect 1/6 1-5; **zweites** sharpness 1/6 1-5 (292-311) |
    | 4 | `iron_shovel` | unbreaking 1/2 2-5; efficiency 1/6 1-5 |
    | 5 | `iron_pickaxe` | unbreaking 1/2 2-5; efficiency 1/6 1-5; fortune 1/6 1-5 |
    | 6 | `iron_axe` | unbreaking 1/2 2-5; efficiency 1/6 1-5 |
    | 7 | `iron_hoe` | unbreaking 1/2 2-5; efficiency 1/6 1-5 |
    | 8 | `iron_helmet` | protection, blast_protection, fire_protection, projectile_protection je 1/6 1-5; unbreaking 1/2 2-5; respiration 1/6 1-2; aqua_affinity 1/6 1-5 |
    | 9 | `iron_chestplate` | protection, blast, fire, projectile je 1/6 1-5; unbreaking 1/2 2-5 |
    | 10 | `iron_leggings` | wie 9 |
    | 11 | `iron_boots` | feather_falling 1/6 **5-9** (431); unbreaking 1/2 2-5 |
    | 12 | `ultimateshovel` | - |
    | 13 | Block `iron_block` | - |
    | 14-19 | nichts | - |

  - `getDropItem` = fish (WaterDragon.java:254-256) ist durch `dropFewItems` wirkungslos. Drops unabhängig von `recentlyHit`.
- **Spawnen:** `getCanSpawnHere` (WaterDragon.java:747-772): Spawner „Water Dragon“ im Kasten −3..+2/0..4 → erlaubt. Sonst `posY >= 50`, **Tag** (`isDaytime`), kein anderer WaterDragon in `expand(16,5,16)`.
  - Natürlich (manifest): waterCreature river 5, swampland 3, ocean 2, stoneBeach 2, je 1-1, Guard `WaterDragonEnable`.
  - Bauwerk: `makeWaterDragonLair` setzt 4 Spawner um den Mittelpunkt auf y+3 (GenericDungeon.java:2059-2078).
  - Despawn: Baby → `enablePersistence`, nie. Sonst nur ohne Persistenz und nicht zahm (WaterDragon.java:166-172).
- **Zustand:** DataWatcher 20 (int) `attacking`: 0 ruhig, 1 Nahkampf (Kiefer schnappt mit `cos(f2*1.2*wingspeed)*π*0.25`), 2 Wasserkanone (Kiefer offen 0.45) (WaterDragon.java:70, 739-745; ModelWaterDragon.java:214-222). Dazu Vanilla-`EntityTameable` (Owner, Sitting) und `EntityAgeable`-Alter. Kein eigenes NBT. `RenderInfo renderdata` (rf1-4, ri1-4; WaterDragon.java:71-81, 183-196) hat keinen Nutzer in Renderer/Modell (grep).
- **Sounds:** Living keiner; Hurt `orespawn:waterdragon_hurt` (3 Varianten, manifest); Tod `orespawn:waterdragon_death` (WaterDragon.java:234-244); Lautstärke/Pitch 1.0 (WaterDragon.java:246-252). `random.bow` bei jedem Schuss (WaterDragon.java:657, 667). `"splash"` 1.5, Pitch 0.9-1.1 beim Heilen im Wasser (WaterDragon.java:644); kein OreSpawn-Sound (manifest), ob Vanilla 1.7.10 das Event kennt: offen.
- **Config:** `WaterDragon_health`/`_attack`/`_defense` (WaterDragon.java:65, 180, 199, 450); `WaterDragonEnable`; `PlayNicely` (WaterDragon.java:713); `AllMobsDisable` → `WaterDragonEnable = 0` (OreSpawnMain.java:1258, 5853).
- **Portierung 1.21.1:**
  - `TamableAnimal`; Attribute MAX_HEALTH 150, ATTACK_DAMAGE 20, ARMOR 8, MOVEMENT_SPEED 0.25/0.55 pro Tick umschalten. `fireImmune()`, `canBreatheUnderwater` → `true`, `xpReward = 100`.
  - Goals: `FloatGoal`, `BreedGoal(1.0)`, eigener FollowOwner-Port, `TemptGoal(1.2, Ingredient, false)`, WanderALot-Port, `LookAtPlayerGoal(8)`, `RandomLookAroundGoal`, `HurtByTargetGoal`.
  - `Items.fish` → cod, salmon, tropical_fish, pufferfish (roh). **Nicht** `#minecraft:fishes`, das enthält Gebratenes. Fisch-Drop Meta 0 → `cod`.
  - Zähmen: `tame(player)`. Entzähmen: `setTame(false, true)` + `setOwnerUUID(null)`. Sitzen: `setOrderedToSit` ohne `SitWhenOrderedToGoal`, damit das Original-Verhalten erhalten bleibt.
  - Namensschild: in 1.21.1 läuft `mobInteract` vor `NameTagItem.interactLivingEntity`. Die Besitzerprüfung muss in `mobInteract` mit `InteractionResult.SUCCESS` liegen, sonst benennt jeder den Drachen über das Vanilla-Item.
  - Verzauberungen: data-driven Holder aus `registryAccess().lookupOrThrow(Registries.ENCHANTMENT)`. Stufen über Maximum sind erlaubt (≤ 255). Doppeltes sharpness: 1.21.1 `enchant` hebt nur an, 1.7.10 hängt zwei NBT-Einträge an; Abweichung dokumentieren.
  - `heal(-1)` → `setHealth(getHealth()-1)`; `setDead` → `discard()` (keine Drops, 1:1).
  - `hurt_timer` als eigenes Feld behalten. DataWatcher 20 → `EntityDataAccessor<Integer>`.
  - SmallFireball: `new SmallFireball(level, this, Vec3)`.
  - Spawn: BiomeModifier `neoforge:add_spawns` für river, swamp, ocean, stony_shore; `SpawnPlacementTypes.IN_WATER` plus eigene Regel (Tag, y ≥ 50, Abstand 16/5).
  - Baby-Besitzer-Bug (tame ohne Owner) bewusst entscheiden.

### Whale - Whale (`whale`)

- **Rolle:** friedliches Wassertier, `EntityAnimal` (Whale.java:14), züchtbar, atmet unter Wasser (Whale.java:98-100). Das Navigationsmodell ist Land-KI mit `EntityAISwimming`, der Wal treibt also an der Oberfläche.
- **Werte:**
  - XP 40 (Whale.java:36), `fireResistance = 100` (Whale.java:35).
  - Leben **fest 100** (`mygetMaxHealth`, Whale.java:102-104), kein Config-Schlüssel.
  - Tempo 0.35 konstant (Whale.java:26, 50, 60); attackDamage registriert mit 0 (Whale.java:51-52).
  - Regeneration: 1/200 pro Tick +1 HP (Whale.java:89-91); im Wasser zusätzlich 1/50 pro Tick +1 HP mit Sound (Whale.java:266-269).
  - An Land ohne Wasser in Reichweite: 1/20 × 1/25 = 1/500 pro Tick −4 HP über `heal(-4)`, bei ≤ 0 `setDead()` ohne Drops (Whale.java:235-265).
  - Fontäne: `spray_timer` 250 + rand(250) Ticks Pause, dann `spray` 25 + rand(25) Ticks lang je 10 `bubble` + 10 `splash` pro Tick bei y+1 (Whale.java:62-88). Startwert `spray_timer = 0`, also Fontäne sofort beim ersten Tick.
  - Baby: halber Render-Maßstab (RenderWhale.java:35-36).
- **KI und Angriffe:**
  - `tasks` (Whale.java:38-44): 0 `EntityAISwimming`; 1 `EntityAIMate(1.0)`; 2 `EntityAITempt(1.2, Items.fish, false)`; 4 `EntityAIPanic(1.5)`; 5 `EntityAIWatchClosest(EntityPlayer, 12)`; 6 `MyEntityAIWander(1.0)` (Chance 1/90, Reichweite 10/7; MyEntityAIWander.java:24-31); 7 `EntityAILookIdle`. Keine Angriffe.
  - `updateAITick`: 1/200 pro Tick `setRevengeTarget(null)` (Whale.java:232-234), beendet damit Panik.
  - Wassersuche: an Land 1/20 pro Tick, Schalen i ∈ {1,2,3,4,5,7,9}, dy = min(i,4), dann `tryMoveToXYZ(tx, ty-1, tz, 1.0)` (Whale.java:235-255; Scan 144-225).
- **Interaktion:** nur Vanilla-`EntityAnimal`: Züchten mit `crystalapple` (Whale.java:301-303), Kind = `new Whale` (Whale.java:289-295). Fisch lockt nur (Tempt). `isWheat` ist ein Rest ohne Aufrufer (Whale.java:297-299). CritterCage-Name „Whale“ (CritterCage.java:415).
- **Drops:** 20 + rand(25) = 20..44× `fish` (Meta 0), Streuung x/z ±3, y +1 (Whale.java:130-142). Unabhängig von `recentlyHit`; `getDropItem` (Whale.java:126-128) wirkungslos.
- **Spawnen:** `getCanSpawnHere`: `posY >= 50`, Tag, Chance 1/50, kein anderer Whale in `expand(32,8,32)` (Whale.java:272-279).
  - Natürlich (manifest): waterCreature deepOcean Gewicht 1, 1-2, Guard `WhaleEnable`.
  - OreSpawn-Dimensionen: Utopia-Grundbiom (Konstruktor, von `WorldProviderOreSpawn` ohne `set*Creatures` benutzt) Gewicht 1, 1-1 (BiomeGenUtopianPlains.java:43; WorldProviderOreSpawn.java:15); `setCrystalCreatures` Gewicht 1, 1-2 (BiomeGenUtopianPlains.java:167).
  - Crystal-Dried-Egg-Block Fall 9 (ChunkProviderOreSpawn5.java:660).
  - Despawn: Baby → Persistenz, nie. Erwachsene ohne Persistenz dürfen despawnen (Whale.java:281-287).
  - Die Recherche nennt nur „Lakes in Crystal Dim.“; der Code spawnt zusätzlich im Overworld-deepOcean und in Utopia.
- **Zustand:** kein DataWatcher, kein NBT. `spray` wird **nur auf dem Client** heruntergezählt (Whale.java:71-88); serverseitig bleibt es nach dem ersten Setzen > 0, der Server-Timer steht dann still (wirkungslos).
- **Sounds:** Living `"splash"` (Whale.java:107), Hurt `orespawn:little_splat`, Tod `orespawn:big_splat` (Whale.java:110-116); Lautstärke 0.9, Pitch 0.5 (Whale.java:118-124). `"splash"` 1.0, Pitch 0.9-1.1 beim Wasserheilen (Whale.java:267). Ob Vanilla 1.7.10 ein Event `splash` hat: offen.
- **Config:** `WhaleEnable`; kein Stats-Schlüssel (Leben hart 100). `AllMobsDisable` für Whale im gesichteten Ausschnitt nicht belegt: offen.
- **Portierung 1.21.1:**
  - `Animal`, `MobCategory.WATER_CREATURE`; Attribute MAX_HEALTH 100, MOVEMENT_SPEED 0.35.
  - `canBreatheUnderwater` → true. `FloatGoal` statt Swimming, Ground-Navigation mit `setCanFloat(true)`, damit das Oberflächen-Treiben bleibt.
  - Fontäne komplett clientseitig im `tick()` unter `level().isClientSide`; Timer nur dort führen.
  - `heal(-4)` → `setHealth`, `setDead` → `discard()`.
  - Spawn: `SpawnPlacementTypes.IN_WATER` + eigene Regel; deep_ocean per BiomeModifier.
  - Der Wassersuch-Scan kostet an Land bis zu mehrere tausend `getBlockState` pro Lauf; 1:1 übernehmbar, aber nicht häufiger ausführen.

### WormLarge - Large Worm (`large_worm`)

- **Rolle:** feindlicher „Boss“-Wurm, `EntityMob` (WormLarge.java:16), ohne Boss-Balken. Steckt per `noClip` im Boden, taucht auf, sobald keine Medium Worms mehr in der Nähe sind und ein Spieler da ist. Bringt beim ersten Tick eine Brut aus 20 Small und 20 Medium Worms mit.
- **Werte:**
  - XP **2050** (WormLarge.java:25).
  - `noClip = true` ab Konstruktor (WormLarge.java:26); `setAvoidsWater(true)` (WormLarge.java:24).
  - Leben/Angriff/Rüstung (manifest) 90/18/14; Rüstung über `getTotalArmorValue` (WormLarge.java:83-85).
  - Vertikale Dämpfung `motionY *= 0.85` pro Tick (WormLarge.java:164); im noClip-Zustand zusätzlich `motionY -= 0.01`, `motionX = motionZ = 0`, `moveForward = 0` (WormLarge.java:140-145).
  - Immun gegen `inWall` (WormLarge.java:316-323). Fallschaden nur wenn nicht noClip (WormLarge.java:249-259). Löst weder Schritte noch Druckplatten aus (WormLarge.java:245-247, 261-263).
  - `canBePushed` true, Kollisionen leer (WormLarge.java:69-77).
- **KI und Angriffe:**
  - `tasks` (WormLarge.java:27-31): 0 `EntityAISwimming`; 1 `EntityAIMoveThroughVillage(1.0, false)`; 2 `MyEntityAIWanderALot(16, 1.0)`; 3 `EntityAIWatchClosest(EntityPlayer, 8)`; 4 `EntityAILookIdle`. Keine targetTasks. Die Tasks laufen **nur, wenn nicht noClip** (WormLarge.java:174-176).
  - **Grab-Zustand** `onLivingUpdate` (WormLarge.java:101-145):
    - Nächster `WormMedium` in `expand(8,8,8)`; nur wenn keiner da ist, nächster Spieler in `expand(8,8,8)` (auch Kreativ) (WormLarge.java:106-109).
    - *Auftauchen* bei (kein Medium und Spieler) oder `PlayNicely != 0`: zum Spieler drehen. Ist der Block auf Fußhöhe nicht Luft (`tallgrass` zählt als Luft), `motionY += 0.25`, `posY += 0.1`; sonst `noClip = false`, normale Physik (WormLarge.java:110-125).
    - *Eingraben* sonst: `noClip = true`. Ist der Block bei `posY+3.5` nicht Luft, `motionY += 0.1`, `posY += 0.05`. Ist dieser Block nicht `grass`/`dirt`/`stone`, folgt **`setDead()`** ohne Drops (WormLarge.java:126-139). Wasser, Sand, Blumen, Schnee usw. töten ihn.
  - **Brut:** serverseitig, einmalig bei `wormsSpawned == 0`: 20× „Small Worm“ (x/z ±5) und 20× „Medium Worm“ (x/z ±4) auf gleicher Höhe, zufällige Gier (WormLarge.java:146-157, 335-343).
  - **Angriff** `updateAITasks` (WormLarge.java:167-243): `PlayNicely` → nichts; Medium Worm in `expand(8,8,8)` → nichts.
    - Ziel ist der nächste Nicht-Kreativ-Spieler in `expand(8,6,8)`: drehen, `tryMoveToXYZ(Spieler, 1.0)` (WormLarge.java:184-190).
    - Chance 1/10 pro Tick und Abstand < 3.0: Vanilla-`EntityMob.attackEntityAsMob` (Attribut 18) (WormLarge.java:191-192).
    - Danach 1/4: `getEquipmentInSlot(4)` = **Helm**, falls leer Slot 3 = **Brustpanzer**. Das Stück wird vom Spieler entfernt, um `rest/10` beschädigt (Rest ≤ 10 → 1) und als Item bei x/z ±4, y +3 abgeworfen (WormLarge.java:193-224).
    - Unabhängig davon 1/4: Slot 0 = **Haupthand**, gleiche Behandlung (WormLarge.java:225-240).
    - Slotbelegung 1.7.10: 0 Hand, 1 Stiefel, 2 Hose, 3 Brust, 4 Helm. Die Variable heißt überall `boots`, meint hier aber Helm/Brust.
  - `onUpdate`: mit `isNoDespawnRequired` (Namensschild) wird `noClip = false` vor der Bewegung gesetzt, so dass benannte Würmer nicht durch Blöcke fallen (WormLarge.java:159-162).
- **Interaktion:** keine. CritterCage-Name „Large Worm“ (CritterCage.java:343); Girlfriend-Overlay (GirlfriendOverlayGui.java:200-201).
- **Drops:** `dropFewItems`, Streuung x/z ±3, y +2.5..+5.5 (WormLarge.java:349-378): 1× `wormtooth`, 1× `item_frame`, 6× `rotten_flesh`, 6× `leather`, 8× `dirt`, 16× `gold_nugget`, 5× `diamond`, 4× `uranium_nugget`, 4× `titanium_nugget`. Unabhängig von `recentlyHit`. `getDropItem` rotten_flesh (WormLarge.java:345-347) wirkungslos.
- **Spawnen:** `getCanSpawnHere` (WormLarge.java:265-311):
  1. Spawner „Large Worm“ im Kasten −3..+2/0..4: `wormsSpawned = 1` (**keine Brut**), erlaubt.
  2. Sonst `posY >= 50`; kein anderer Large Worm in `expand(32,8,32)`; 13×13 Fläche (±6) auf y−2..y−8 komplett **nicht Luft**; 13×13 auf y+2..y+8 komplett **Luft**.
  - Natürlich (manifest): creature plains 25, savanna 15, savannaPlateau 10, je 1-1, Guard `WormEnable`.
  - Bauwerke: `makeEnormousCastle` ab level ≥ 6 bis zu 100 Spawner im Außenring (GenericDungeon.java:400-415); `addLevelDecorations` bei decor == 6 drei gestapelte Spawner y+2..+4 (GenericDungeon.java:525, 564-578); dieselben in `makeEnormousCastleQ` (GenericDungeon.java:6610) und `addLevelDecorationsQ` (GenericDungeon.java:6723, 6766-6776).
  - Despawn: nie (`canDespawn` false, WormLarge.java:45-47).
  - Ob Vanilla-1.7.10-Weltgen-Spawns `getCanSpawnHere` aufrufen: offen (Vanilla-Code nicht im Repo dekompiliert).
  - Recherche-Widerspruch: „Kill all small worms → medium appear → boss appears“ stimmt nicht mit dem Code überein. Alle drei Stufen entstehen gleichzeitig; jede Stufe bleibt nur **eingegraben**, solange die kleinere Stufe im 8er-Radius lebt.
- **Zustand:** kein DataWatcher. NBT `wormsSpawned` (int) (WormLarge.java:325-333).
- **Sounds:** Living keiner; Hurt `orespawn:big_splat`; Tod `orespawn:alo_death` (WormLarge.java:57-67); Lautstärke 0.5, Pitch 1.0 (WormLarge.java:49-55).
- **Config:** `WormLarge_health`/`_attack`/`_defense` (WormLarge.java:38, 80, 84); `WormEnable`; `PlayNicely` (WormLarge.java:110, 177).
- **Portierung 1.21.1:**
  - `Monster`; `noClip` → `noPhysics`. Attribute 90/18/14, `xpReward = 2050`.
  - Die Tasks nur ohne `noPhysics` ticken lassen: `customServerAiStep` erst nach der Prüfung an `super` delegieren, oder die Goals mit einer Bedingung versehen.
  - Blöcke: `tallgrass` → `Blocks.SHORT_GRASS` (evtl. auch `TALL_GRASS`, `FERN`), `grass` → `GRASS_BLOCK`, `dirt` → `DIRT`, `stone` → `STONE`. **Risiko:** 1.21.1-Terrain hat coarse_dirt, podzol, granite/diorite/andesite, deepslate, tuff, Schnee und Blumen; eine 1:1-Liste tötet Würmer weit häufiger als in 1.7.10. Entscheidung Liste vs. Tag offen.
  - Ausrüstungsklau: `EquipmentSlot.HEAD`/`CHEST`/`MAINHAND`, `setItemSlot(slot, ItemStack.EMPTY)`, Schaden über `stack.hurtAndBreak(n, serverLevel, null, item -> {})`. Ein dabei zerbrochenes Item nicht als leeren `ItemEntity` droppen (1.7.10 hätte Stapelgröße 0 erzeugt).
  - Brut über `EntityType.create` + `moveTo` + `addFreshEntity`. Spawner-Herkunft über `MobSpawnType.SPAWNER` in `finalizeSpawn` → `wormsSpawned = 1`.
  - Spawnregel: `SpawnPlacements.register(... ON_GROUND ...)` + Prädikat. Der 13×13×7-Doppelscan ist teuer (≈ 2 366 Blockabfragen).
  - `MobCategory.CREATURE` ist in 1.21.1 persistent und spawnt außerhalb der Weltgen nur alle 400 Ticks; als MONSTER registrieren oder bewusst CREATURE lassen: offen.

### WormMedium - Medium Worm (`medium_worm`)

- **Rolle:** feindliche Wurm-Mittelstufe, `EntityMob` (WormMedium.java:13), dauerhaft `noClip` (WormMedium.java:25), taucht in Zyklen auf und ab und stiehlt Stiefel/Hose. Bleibt eingegraben, solange ein Small Worm im 8er-Radius lebt.
- **Werte:**
  - XP **0** (WormMedium.java:24).
  - Leben/Angriff/Rüstung (manifest) 30/10/8; Rüstung über `getTotalArmorValue` (WormMedium.java:173-175).
  - Zyklus: Auftauchphase `upcount` 25 + rand(75) = 25..99 Ticks (WormMedium.java:119). Danach Abtauchphase `downcount` 100 + rand(150) = 100..249 Ticks (WormMedium.java:97). Nicht engagiert: `upcount = rand(50)`, `downcount = 0` pro Tick (WormMedium.java:135-136).
  - Pro Tick `motionY -= 0.01`, `motionX = motionZ = 0` (WormMedium.java:149-152), danach `motionY *= 0.65` (WormMedium.java:160).
  - Immun gegen `inWall` (WormMedium.java:257-264); kein Fallschaden, keine Druckplatten, keine Schritte (WormMedium.java:236-248).
- **KI und Angriffe:** keine `tasks`.
  - `onLivingUpdate` **nur serverseitig** (WormMedium.java:86-88). Nächster `WormSmall` in `expand(8,8,8)`; nur wenn keiner da ist, nächster Spieler (auch Kreativ) in `expand(8,8,8)` (WormMedium.java:89-92).
  - Bei (kein Small und Spieler) oder `PlayNicely`:
    - `upcount > 0`: herunterzählen; beim Erreichen von 0 wird `downcount` gesetzt. Zum Spieler drehen. Block bei `posY+0.25` nicht Luft (`tallgrass` = Luft) → `setDead`, falls nicht grass/dirt/stone; `motionY += 0.2`, `posY += 0.1` (WormMedium.java:94-113).
    - sonst `downcount` herunterzählen, bei 0 neuer `upcount`. Block bei `posY+3` nicht Luft → gleiche Todesregel, `motionY += 0.1`, `posY += 0.05` (WormMedium.java:114-132).
  - Sonst eingegraben mit Kopfblock `posY+3` (WormMedium.java:134-148).
  - `updateAITasks`: `PlayNicely` → nichts; Small Worm in `expand(8,8,8)` → nichts. Nicht-Kreativ-Spieler in `expand(2.25, 8, 2.25)`: drehen (WormMedium.java:185-197).
  - Nur wenn `upcount > 0` und Chance 1/15 pro Tick: Vanilla-`attackEntityAsMob` (Attribut 10) (WormMedium.java:198-199).
  - Danach 1/6: Slot 1 = **Stiefel**, falls leer Slot 2 = **Hose**; entfernen, um `rest/15` beschädigen (Rest ≤ 15 → 1), bei x/z ±4, y +3 abwerfen (WormMedium.java:200-230).
- **Interaktion:** keine. CritterCage-Name „Medium Worm“ (CritterCage.java:347).
- **Drops:** 2× `rotten_flesh`, 2× `leather`, Streuung x/z ±2, y +2.5..+4.5 (WormMedium.java:270-282). XP 0.
- **Spawnen:** `getCanSpawnHere` = nicht Tag (WormMedium.java:250-252). Keine natürlichen Spawns (manifest leer). Entsteht über die Large-Worm-Brut (WormLarge.java:155), das Spawn-Ei `eggmediumworm` und die CritterCage. Despawn nie (WormMedium.java:39-41).
  - Recherche „Spawn: plains“ gilt nur indirekt über den Large Worm.
- **Zustand:** kein DataWatcher, kein NBT. `upcount`/`downcount` sind public, werden aber nirgends außerhalb der Klasse gelesen (grep); das Modell braucht sie nicht.
- **Sounds:** Living keiner; Hurt `orespawn:little_splat`; Tod `orespawn:big_splat` (WormMedium.java:51-61); Lautstärke 0.5, Pitch 1.5 (WormMedium.java:43-49).
- **Config:** `WormMedium_health`/`_attack`/`_defense` (WormMedium.java:32, 74, 174); `PlayNicely` (WormMedium.java:93, 185). `WormEnable` nur indirekt über den Large Worm.
- **Portierung 1.21.1:**
  - `Monster` mit `noPhysics = true`; `onUpdate`-Persistenz-Ausnahme übernehmen.
  - Logik in `aiStep` unter `!level().isClientSide`. Keine Goals registrieren (`registerGoals` leer).
  - Slots: `EquipmentSlot.FEET`, dann `LEGS`.
  - Gleiche Blocklisten-Problematik wie WormLarge.
  - Spawner/Ei: Tagesprüfung über `checkSpawnRules`; das Ei ignoriert sie in 1.21.1 wie in 1.7.10.

### WormSmall - Small Worm (`small_worm`)

- **Rolle:** feindliche Wurm-Einstiegsstufe, `EntityMob` (WormSmall.java:13), dauerhaft `noClip` (WormSmall.java:25), Auf-/Abtauch-Zyklus, stiehlt Stiefel. Anders als Medium hängt sie von keiner kleineren Stufe ab.
- **Werte:**
  - XP **0** (WormSmall.java:24).
  - Startwert `upcount = 50` (WormSmall.java:20).
  - Leben/Angriff/Rüstung (manifest) 10/3/0; Rüstung über `getTotalArmorValue` (WormSmall.java:77-79). Recherche nennt Angriff 5, Config-Default ist 3 (manifest).
  - Zyklus: Auftauchen `upcount` 25 + rand(50) = 25..74 Ticks (WormSmall.java:115); Abtauchen `downcount` 100 + rand(150) Ticks (WormSmall.java:93); nicht engagiert `upcount = rand(50)` (WormSmall.java:131).
  - Pro Tick `motionY -= 0.01`, x/z = 0 (WormSmall.java:145-148), dann `motionY *= 0.75` (WormSmall.java:156).
  - Immun gegen `inWall` (WormSmall.java:228-235); kein Fallschaden, keine Druckplatten (WormSmall.java:207-219).
- **KI und Angriffe:** keine `tasks`.
  - `onLivingUpdate` läuft **auf beiden Seiten** (kein `isRemote`-Abbruch; WormSmall.java:85-149), inklusive `setDead`. Nächster Spieler (auch Kreativ) in `expand(8,8,8)` (WormSmall.java:88).
  - Bei Spieler oder `PlayNicely`:
    - `upcount > 0`: herunterzählen, drehen; Block bei `posY+0.25` → Todesregel (nicht grass/dirt/stone), `motionY += 0.15`, `posY += 0.1` (WormSmall.java:90-109).
    - sonst: Kopfblock `posY+2` → Todesregel, `motionY += 0.2`, `posY += 0.05` (WormSmall.java:110-128).
  - Sonst eingegraben mit Kopfblock `posY+2`, `motionY += 0.1`, `posY += 0.05` (WormSmall.java:130-144).
  - `updateAITasks`: `PlayNicely` → nichts. Nicht-Kreativ-Spieler in `expand(1.5, 4, 1.5)`: drehen. Bei `upcount > 0` und 1/15 pro Tick Vanilla-`attackEntityAsMob` (Attribut 3). Danach 1/6: Slot 1 = **Stiefel**, um `rest/20` beschädigen (Rest ≤ 20 → 1), bei x/z ±4, y +3 abwerfen (WormSmall.java:169-205).
- **Interaktion:** keine. CritterCage-Name „Small Worm“ (CritterCage.java:339).
- **Drops:** keine (`getDropItem` null, WormSmall.java:237-239), XP 0.
- **Spawnen:** `getCanSpawnHere` = nicht Tag (WormSmall.java:221-223). Keine natürlichen Spawns (manifest leer). Entsteht über die Large-Worm-Brut (WormLarge.java:154), das Ei `eggsmallworm` und die CritterCage. Despawn nie (WormSmall.java:39-41). Tracking-Reichweite 32 (manifest).
- **Zustand:** kein DataWatcher, kein NBT.
- **Sounds:** Living keiner; Hurt `orespawn:little_splat`; Tod keiner (WormSmall.java:51-61); Lautstärke 0.5, Pitch 1.5 (WormSmall.java:43-49).
- **Config:** `WormSmall_health`/`_attack`/`_defense` (WormSmall.java:32, 74, 78); `PlayNicely` (WormSmall.java:89, 176).
- **Portierung 1.21.1:**
  - Wie WormMedium; die Grab-/Todeslogik **nur serverseitig** ausführen. Die clientseitige `setDead`-Ausführung des Originals würde in 1.21.1 zu Geister-Entities führen.
  - Slot `EquipmentSlot.FEET`.
  - Hitbox 0.25 × 1.0 mit `noPhysics`: Treffbarkeit im Boden prüfen (Original ebenso).
  - Blocklisten-Risiko wie WormLarge.
