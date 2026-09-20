# Verhalten: entity-07

Dieser Batch mischt vier sehr unterschiedliche Gruppen. Erstens eigenständige Kreaturen: den Oster-Hasen (`EasterBunny`, legt laufend zufällige Spawn-Eier), den Boss-artigen `EmperorScorpion` (eigene Ziel-KI, Gift, Scorpion-Beschwörung, verzauberte Diamant-Beute) und die Enderman-Kopien `EnderKnight`/`EnderReaper` (alter, nicht task-basierter KI-Pfad). Zweitens die „Portal-Kritter“ `EntityAnt` (Utopia) und `EntityButterfly` (Chaos) samt `EntityLunaMoth` (Fackel-Anflug) und `EntityMosquito` (umschwirrt Spieler, greift nie an). Drittens Spieler-Werkzeuge: das Hoverboard `Elevator` (eigene Fahrphysik, Rider-Input, Farbwechsel) und das Wurfgeschoss `EntityCage` (Critter Cage, fängt rund 100 Kreaturtypen mit Fluchtchance). Viertens reine Basis- bzw. Totcode-Klassen: `EntityCannonFodder` (TamableAnimal-Basis für Chipmunk/Lizard/Ostrich/VelocityRaptor mit Zwei-Besitzer- und Hutfarben-Teamlogik), `EnchantedCow` (Drop-Variante von `RedCow`) und `EntityLavaLovingItem` (feuerfestes `EntityItem`, im Quellbaum nie instanziiert). Alle Zahlen stammen aus dem dekompilierten Quelltext (`Class.java:LINE`), aus `manifest.json` oder, wo ausdrücklich markiert, aus `javap` gegen `reference/jar/mcp/client-1.7.10.jar` (Vanilla 1.7.10).

Abkürzungen: `OSM` = `OreSpawnMain.java`. „pro Tick“ heißt: pro Aufruf von `updateAITasks`/`updateAITick`, also serverseitig einmal je Tick.

---

### EasterBunny - EasterBunny (`easter_bunny`)

- **Rolle:** passives Event-Tier, Basis `EntityAnimal`, neue Task-KI (`isAIEnabled` true, EasterBunny.java:64-66). Globaler Name `"Easter Bunny"` (OSM:4013), Mod-Entity-Name `"EasterBunny"` (OSM:4017) – die Critter Cage benutzt den globalen Namen (CritterCage.java:439).
- **Werte:**
  - `experienceValue = 5` (EasterBunny.java:23) ist wirkungslos: `EntityAnimal.getExperiencePoints` liefert `1 + rand.nextInt(3)` (javap `wf.e(yz)`), also 1-3 XP.
  - `fireResistance = 100` (EasterBunny.java:22).
  - `attackDamage` wird auf 8.0 registriert (EasterBunny.java:39-40), es gibt aber keinen Angriffs-Task – der Wert wird nie benutzt.
  - Geschwindigkeit 0.45 wird jeden Tick neu gesetzt (EasterBunny.java:19, 48).
- **KI und Angriffe:**

  | Prio | Task | Parameter |
  |---|---|---|
  | 0 | `EntityAISwimming` | (EasterBunny.java:25) |
  | 1 | `EntityAIMate` | Speed 1.0 (:26) |
  | 2 | `EntityAIAvoidEntity` `EntityMob` | 8.0 Blöcke, 1.0 / 1.4 (:27) |
  | 3 | `EntityAIAvoidEntity` `EntityPlayer` | 8.0, 1.0 / 1.4 (:28) |
  | 4 | `EntityAIPanic` | 1.5 (:29) |
  | 5 | `EntityAIWatchClosest` `EntityLiving` | 8.0 (:30) |
  | 6 | `MyEntityAIWanderALot` | xz-Radius 16, Speed 1.0 (:31); startet mit 1/30 je Tick, y-Radius 7 (MyEntityAIWanderALot.java:35, 42) |
  | 7 | `EntityAILookIdle` | (:32) |

  - Navigator meidet Wasser (:24).
  - `updateAITick`: mit 1/200 wird das Rache-Ziel gelöscht (:106-108); mit 1/600 `LayAnEgg(1 + nextInt(3))`, also 1-3 Stück (:110-112).
  - `LayAnEgg`: `nextInt(115)` (:121). Die Fälle 5-113 wählen je ein Spawn-Ei aus `OreSpawnMain` (:123-558), das sind 109 Ei-Typen, darunter `GirlfriendEgg`, `GodzillaEgg`, `TheKingEgg`, `TheQueenEgg`, `KrakenEgg` und `EasterBunnyEgg`. 0-4 und 114 ergeben nichts, also Trefferquote 109/115. Das `EntityItem` spawnt bei x/z ±1 (`OreSpawnRand.nextInt(2) - nextInt(2)`) und y+1 (:568).
- **Interaktion:** Zuchtitem `OreSpawnMain.MyCrystalApple` (:595-597). `isWheat(apple)` (:591-593) ist ein toter 1.6-Methodenname und hat keine Wirkung. `createChild` → neuer `EasterBunny` (:583-589). Kein Zähmen, kein Reiten.
- **Drops:** beim Tod 2-4× `Items.chicken` (`2 + nextInt(3)`, :97-102). Keine Eier beim Tod.
  - Recherche (01-mobs.md:134) nennt „spawn eggs“ als Drop und Attack 0. Laut Code kommen die Eier zu Lebzeiten, beim Tod gibt es Hühnchen; das 8.0-Attribut ist toter Wert.
- **Spawnen:** `getCanSpawnHere` verlangt alles davon:
  - `posY >= 50` (:53)
  - Tag (:56)
  - kein anderer `EasterBunny` in `expand(32, 8, 32)` (:60)

  Die Spawn-Einträge werden nur registriert, wenn `easter_day != 0` (OSM:4341). `easter_day` wird einmalig beim Laden gesetzt, wenn `Calendar.MONTH == 3 && DAY_OF_MONTH == 20`, also am **20. April** (OSM:4179-4180, 4230-4232). Die Recherche sagt „only on Easter“; der Code kennt nur dieses feste Datum zum Startzeitpunkt.
  - `canDespawn`: Kinder werden persistent und despawnen nicht (:576-579), sonst `!isNoDespawnRequired()` (:580).
- **Zustand:** keine eigenen DataWatcher, kein eigenes NBT.
- **Sounds:** hurt/death `orespawn:duck_hurt` (:81, :85), Lautstärke 0.4 (:89), kein Living-Sound (:77).
- **Config:** `EasterBunnyEnable` (Default 1, manifest), implizit Systemdatum über `easter_day`.
- **Portierung 1.21.1:**
  - `Animal` mit `goalSelector`: `FloatGoal`, `BreedGoal`, `AvoidEntityGoal<Monster>` und `AvoidEntityGoal<Player>`, `PanicGoal`, `LookAtPlayerGoal(Mob.class)`, eigener Wander-Goal (1/30, Radius 16/7), `RandomLookAroundGoal`.
  - `isFood` → Crystal Apple.
  - Die Eier-Tabelle als `List<Supplier<Item>>` mit derselben Indexlogik; fehlende Eier-Items dürfen den Index nicht verschieben, sonst ändern sich die Wahrscheinlichkeiten.
  - Die Datums-Sperre als Spawn-Prädikat auswerten (`RegisterSpawnPlacementsEvent`) statt beim Registrieren: Biome-Modifier sind statisch und können nicht datumsabhängig registriert werden.
  - Spawn-Kategorie ist im Original `ambient` (manifest). Entscheiden, ob `CREATURE` fachlich richtiger ist.
  - XP über `getBaseExperienceReward` bleibt 1-3 wie Vanilla `Animal`.

### Elevator - Hoverboard (`hoverboard`)

- **Rolle:** reitbares Fahrzeug, Basis `EntityLiving` (keine KI, `onUpdate` komplett überschrieben **ohne** `super.onUpdate()`, Elevator.java:232-527). Gesetzt wird es vom Item `MyElevator` (`ItemElevator.java:22-24`, Name `"Hoverboard"`). Registriert mit Tracking 128, Frequenz 1, Velocity-Updates true (OSM:3543). Die Methoden `getTrackingRange` 128 / `getUpdateFrequency` 10 (Elevator.java:118-124) werden von der Registrierung nicht benutzt.
- **Werte:**
  - `maxHealth` 60 wird nie reduziert, weil `attackEntityFrom` den Schaden in `DamageTaken` umleitet (:158-184).
  - Zerstörung: `DamageTaken += dmg × 10` (:169). Das Board geht kaputt, wenn Creative-Spieler oder `DamageTaken > 40` (:172). `DamageTaken` sinkt um 1 je Tick (:249-251), `TimeSinceHit` wird auf 10 gesetzt und zählt herunter (:168, :246-248).
  - Mit Reiter nimmt es nur Schaden von Spielern (:159-162). `inWall` wird ignoriert (:163-165).
  - Unbenutzt: `damage_counter = 100` (:45).
  - `getMountedYOffset` 0.5 (:155). `shouldRiderSit` false, der Reiter steht (:114-116). Schatten 0.25 (:551, RenderElevator.java:16).
  - Fallschaden aus (:130-134), keine Schritte (:136-138), `canBePushed` true (:150-152).
  - **Tippfehler-Bug:** `onLivingpdate` (:225-230) überschreibt nichts; `setFire(0)` wird nie aufgerufen. Wegen des fehlenden `super.onUpdate()` läuft aber auch kein `onEntityUpdate`/`onLivingUpdate`: kein Feuer, keine Portale, keine Regeneration.
- **KI und Angriffe (Fahrphysik):**
  - Schwebehöhe (Server, :368-384):
    - Prüfblock bei `posY − gh`, mit `gh = 0.75` ohne und 1.25 mit Reiter (:240, :369).
    - Nicht-Luft → `motionY += 0.06`, `posY += 0.1`; sonst `motionY −= 0.01`.
    - Nur mit Reiter und nur bei `mobGriefing`, je 1/200: `tallgrass` → Luft, `grass` → `dirt` (:375-380).
  - Hindernis-Anstieg (:387-401): `dist = 3 + (int)(velocity × 8)`. Für jede Tiefe `k = 1..dist−1` und jede Distanz `i = 1..2·dist−1` in Blickrichtung (`yaw + 90°`) zählt jeder Nicht-Luft-Block 0.05. Die Summe × 0.11 geht auf `motionY` **und** `posY`.
  - Lenkung (:402-429):
    - `relative_g = (riderYaw − yaw) mod 180`, auf (−90, 90] gefaltet.
    - Bei `velocity > 0.01`: `yaw = riderYaw + relative_g · clamp(|1.85 − velocity|, 0.01, 0.9)`, sonst `yaw = riderYaw`.
    - `pitch = 10 · velocity`.
  - Geschwindigkeit (:430-493):
    - `max_speed = 0.85` (:239); `+1` wenn `OreSpawnMain.flyup_keystate != 0` (:444-446).
    - Liegt die Bewegungsrichtung mehr als 1.5 rad von der Reiterblickrichtung weg, gilt `velocity` als rückwärts (:447-457).
    - Vorwärts-Input (`moveForward > 0`): `+0.025`, mit Boost zusätzlich `+0.15` (:459-463).
    - Rückwärts: `max_speed = 0.35`, `−0.02` (:465-468).
    - Clamp auf ±`max_speed`, Bewegung entlang `yaw+90°` bzw. `yaw+270°`. Ohne Input bleibt der Betrag erhalten (:486-493).
    - Während der „Explosion“ `−0.05` je Tick (:431-436).
  - Ohne Reiter `motionX/Z = 0` (:495-498).
  - Reibung `×0.98` / `×0.94` / `×0.98` (:510-512).
  - Crash: `isCollidedHorizontally && velocity > 0.75` → das Board stirbt und droppt 6-15 `stick` (`6 + nextInt(10)`) und 2 `diamond` (:500-508), **nicht** das Hoverboard-Item. Der Reiter nimmt keinen Schaden.
  - Schieben: Entities in `expand(0.25, 0, 0.25)` außer Reiter, `Girlfriend` und `Boyfriend` bekommen `applyEntityCollision` (:514-522).
  - Zufalls-„Explosion“ (rein kosmetisch plus Bremsung):
    - Server, bei `velocity > 0.65` mit 1/20000 je Tick → `exploding = 45` Ticks, `playing = 50` (:303-311).
    - Mit Reiter je 1/10 `random.explode` (Lautstärke 0.55, Pitch 0.75 + rand) sowie 15× vier Partikelsorten (:316-326).
  - Client: ohne Reiter eigene Schwebe-Vorhersage `+0.06`/`+0.07` bzw. `−0.003` (:327-338). Mit Reiter schickt es jeden Tick `C05PacketPlayerLook` und `C0CPacketInput` des `EntityClientPlayerMP` (:339-343), danach Positions-Interpolation (:344-365).
  - Partikel `smoke`/`reddust` hinter dem Board ab `velocity > 0.15` mit Reiter, `splash` über Wasser bis 9 Blöcke tief (:255-295).
- **Interaktion:**
  - Mit `MyUltimateSword` und `distSq < 16`: Farbe 1→10 zyklisch (server, :560-569).
  - Reitet schon ein anderer Spieler, passiert nichts (:570-572). Sonst `mountEntity` (server, :573-575); erneutes Klicken steigt ab (Vanilla-`mountEntity`-Toggle).
  - `RiderControlMessageHandler` setzt **eine globale** statische `flyup_keystate` für alle Spieler (RiderControlMessageHandler.java:17).
  - Server castet den Reiter hart auf `EntityPlayer` (:386). Ein Nicht-Spieler als Reiter wäre eine `ClassCastException`.
- **Drops:** Schaden über 40 (nicht Creative) → 1× `MyElevator` (:176-178); Crash → Stöcke und Diamanten (siehe oben); keine XP-relevante Todeslogik.
- **Spawnen:** nie natürlich (manifest `spawns: []`); `canDespawn` false (:110-112); `func_110163_bv` (= `enablePersistence`, methods.csv:32) schon in `entityInit` (:147).
- **Zustand:**

  | DW | Typ | Bedeutung | Zeile |
  |---|---|---|---|
  | 20 | int | `exploding`-Restticks | :145, :603-609 |
  | 21 | int | `color` 1-10 (Init 0, Feld 1) | :146, :611-617 |
  | 22 | int | `TimeSinceHit` | :142, :587-593 |
  | 23 | int | `ForwardDirection` (±1, Wackelrichtung) | :143, :595-601 |
  | 24 | float | `DamageTaken` | :144, :579-585 |

  NBT: `HoverColor`, beim Lesen auf 1..10 geklemmt (:535-548). `writeEntityToNBT` ruft kein `super`.
- **Sounds:** `orespawn:hover` am Reiter, Lautstärke 0.45, Pitch 1.0, mit 1/80 und danach 55 Ticks Sperre (:296-302; 6 Dateivarianten laut manifest). `random.explode` siehe oben.
- **Config:** keine eigenen Keys. Liest `OreSpawnMain.flyup_keystate` (Netzwerk, keine Config).
- **Portierung 1.21.1:**
  - Nicht als `LivingEntity`, sondern als `VehicleEntity` (1.21.1) portieren. Deren Hurt-Time/Hurt-Dir/Damage-Daten entsprechen DW 22/23/24, und die Boot-Logik „Schaden × 10, zerstört > 40“ passt. Im neoforge-sources-Jar nachprüfen, nicht annehmen.
  - Die 60 HP entfallen fachlich. Wegen `MyUtils.isIgnoreable` (MyUtils.java:18) wurde das Board ohnehin nie als Ziel gewählt.
  - Steuerung: `getControllingPassenger()`. 1.21.1-`LocalPlayer` schickt als Passagier selbst `ServerboundPlayerInputPacket` (xxa/zza) – die manuellen C05/C0C-Pakete entfallen und damit auch die Client-Klassenreferenz im Common-Code (Guardrail).
  - Boost-Taste: eigenes `CustomPacketPayload`, Zustand **pro Spieler** (Attachment) statt global. Die globale Variante ist ein Multiplayer-Bug des Originals.
  - Tick: eigene `tick()`-Physik serverseitig; Client-Interpolation über `lerpTo`. Partikel nur clientseitig bzw. per `ServerLevel.sendParticles`.
  - Grasabbau über `level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)`.
  - Renderer: `ModelElevator` wird direkt instanziiert (RenderElevator.java:17). Der manifest-Eintrag „model not used by any renderer: ModelElevator“ ist ein Fehlalarm.
  - Wackel-Rotation `sin(f2)·f2·f3/10·ForwardDirection` um X (RenderElevator.java:24-30), Texturen `elevator1..10.png` (texture_map).

### EmperorScorpion - Emperor Scorpion (`emperor_scorpion`)

- **Rolle:** feindlicher Mini-Boss, Basis `EntityMob`, neue Task-KI plus eigene Ziellogik in `updateAITasks` (EmperorScorpion.java:386-437).
- **Werte:**

  | Wert | Quelle |
  |---|---|
  | XP 200 | EmperorScorpion.java:33 |
  | `isImmuneToFire`, `fireResistance` 100 | :34-35 |
  | Rüstung = `EmperorScorpion_stats.defense` (20) | :97-99, manifest |
  | Health 350 / Attack 35 aus `MobStats` | :64-66, :78-80, manifest config |
  | Speed 0.35, jeden Tick neu gesetzt | :30, :74 |
  | `hurt_timer`: nach jedem angenommenen Treffer 30 Ticks Unverwundbarkeit | :370-375, Abbau :392-394 |
  | `cactus`-Schaden wird ignoriert | :373 |
  | Heilung +2.0 mit 1/100 je Tick, solange HP < max | :434-436 |
  | Sprung: zusätzlich `motionY += 0.25`, `posY += 0.5` | :109-113 |

- **KI und Angriffe:**

  | Prio | Task | Parameter |
  |---|---|---|
  | 0 | `EntityAISwimming` | :38 |
  | 1 | `EntityAIMoveThroughVillage` | 0.9, false (:39) |
  | 2 | `MyEntityAIWanderALot` | 14, 1.0 (:40) |
  | 3 | `EntityAIWatchClosest` Player | 8.0 (:41) |
  | 4 | `EntityAILookIdle` | :42 |
  | target 1 | `EntityAIHurtByTarget` | false (:43) |

  - Zielschleife (je Tick mit 1/4, :395):
    - Totes Ziel wird verworfen (:397-400).
    - Mit 1/100 wird das Ziel vergessen (:401-403).
    - Ohne Ziel `findSomethingToAttack` (:404-406).
  - Suche (:494-511):
    - `PlayNicely != 0` → nie (:495).
    - Box `expand(24, 6, 24)` (:498), sortiert mit `GenericTargetSorter`: Distanz², Creeper halbiert, geteilt durch `height·width` wenn > 1 (GenericTargetSorter.java:17-27).
  - Zulässig (:450-492): lebend, sichtbar, nicht `MyUtils.isIgnoreable`, nicht Enderman/`EnderKnight`/`EnderReaper`/Creeper/`Scorpion`/`EmperorScorpion`, kein Creative-Spieler. **Alle anderen Lebewesen**, auch Tiere und Dorfbewohner, sind Ziele.
  - Mit Ziel: `faceEntity(10, 10)` (:408). Ist `distSq < (6 + target.width/2)²` → DW 20 = 1 und Nahkampf mit `nextInt(4)==0 || nextInt(6)==1` (:409-412).
    - Treffer-Sound: 1/3 `orespawn:scorpion_attack` (Lautstärke 1.4), sonst `orespawn:scorpion_living` (1.0), abgespielt am Ziel (:413-420).
  - Außerhalb der Reichweite `tryMoveToEntityLiving(e, 1.2)` (:423-425). Ohne Ziel DW 20 = 0 (:430-432).
  - Beschwörung: mit Ziel 1/20 (innerhalb des 1/4-Gates) spawnt ein `"Scorpion"` am Mittelpunkt zwischen Skorpion und Ziel, x/z ±4, y+1.01 (:426-428, `spawnCreature` :439-448).
  - `attackEntityAsMob` (:339-366): Grundschaden über Vanilla `EntityMob.attackEntityAsMob` (Attribut 35). Danach:
    - **Gift** mit 1/3, Dauer `var2 × 15` Ticks.
    - **Bug:** `var2 = 6`; nur `EASY` setzt 8. Die Prüfungen auf `NORMAL`/`HARD` stecken im `EASY`-Zweig und sind unerreichbar (:345-353). Easy ergibt 120 Ticks, alle anderen Stufen 90 Ticks.
    - **Rückstoß** `addVelocity(cos·3.0, inair, sin·3.0)`, `inair = 0.2`, ×2 wenn das Ziel ein Spieler oder tot ist (:340-341, :357-361).
  - Getroffen von `EntityLiving` (Spieler sind in 1.7.10 **kein** `EntityLiving`) → Ziel setzen und `tryMove 1.2` (:376-381). Spieler als Angreifer übernimmt `EntityAIHurtByTarget`.
- **Interaktion:** `interact` → false (:335-337).
- **Drops** (`dropItemRand`: x/z ±4 mit `OreSpawnRand`, y+1, :143-151):
  - 1× `MyEmperorScorpionScale`, 1× `item_frame` (:154-155).
  - 4-8 `obsidian` (`4 + nextInt(5)`, :156-158), 4-11 `beef` (`4 + nextInt(8)`, :159-161).
  - `1 + nextInt(5)` Würfe mit `nextInt(20)` (:162-164). Fälle 0-12 geben ein Item, 13-19 nichts:

  | Fall | Item | Verzauberungen (je Wurf unabhängig) | Zeilen |
  |---|---|---|---|
  | 0 | `MyUltimateSword` | – | :165-167 |
  | 1 | `diamond` | – | :169-171 |
  | 2 | `diamond_block` | – | :173-175 |
  | 3 | `diamond_sword` | 1/6 Sharpness 1-5, 1/6 Bane 1-5, 1/6 Knockback 1-5, 1/6 Looting 1-5, 1/2 Unbreaking 2-5, 1/6 Fire Aspect 1-5, 1/6 **zweites** Sharpness 1-5 | :177-202 |
  | 4 | `diamond_shovel` | 1/2 Unbreaking 2-5, 1/6 Efficiency 1-5 | :203-213 |
  | 5 | `diamond_pickaxe` | 1/2 Unbreaking 2-5, 1/6 Efficiency 1-5, 1/6 Fortune 1-5 | :214-227 |
  | 6 | `diamond_axe` | 1/2 Unbreaking 2-5, 1/6 Efficiency 1-5 | :228-238 |
  | 7 | `diamond_hoe` | 1/2 Unbreaking 2-5, 1/6 Efficiency 1-5 | :239-249 |
  | 8 | `diamond_helmet` | 1/6 Protection, Blast, Fire, Projectile je 1-5; 1/2 Unbreaking 2-5; 1/6 Respiration 1-2; 1/6 Aqua Affinity 1-5 | :250-275 |
  | 9 | `diamond_chestplate` | 1/6 Prot/Blast/Fire/Proj je 1-5; 1/2 Unbreaking 2-5 | :276-295 |
  | 10 | `diamond_leggings` | wie 9 | :296-315 |
  | 11 | `diamond_boots` | 1/6 Feather Falling **5-9**; 1/2 Unbreaking 2-5 | :316-326 |
  | 12 | `MyUltimateBow` | – | :327-330 |

  XP 200 (:33).
- **Spawnen** (`getCanSpawnHere`, :521-552):
  - Über 4×4 Säulen (x/z −2..+1) und die Höhen y+2..y+4: ein `mob_spawner` mit `"Emperor Scorpion"` → sofort true (:526-532). Jeder andere Nicht-Luft-Block → false (:534-536). Der Raum darüber muss also frei sein.
  - Danach: `isValidLightLevel` (:540), Nacht (:543), `posY >= 50` (:546), kein anderer `EmperorScorpion` in `expand(20, 6, 20)` (:550).
  - Spawner in `GenericDungeon.java:310, 315, 320`.
  - Biome desert w1 / savanna w2 als Typ **ambient** (OSM:4543-4544).
  - `canDespawn = !isNoDespawnRequired()` (:69-71).
- **Zustand:** DW 20 int `attacking` 0/1 (:48, :513-519), für die Animation. `RenderInfo renderdata` ist ein lokaler Render-Cache, nicht synchronisiert (:52-59). Kein NBT.
- **Sounds:** kein Living-Sound (:120); hurt `orespawn:alo_hurt` (:124); death `orespawn:emperorscorpion_death` (:128); Lautstärke 1.5 (:132), Pitch 1.0 (:136); Angriff siehe KI.
- **Config:** `EmperorScorpionEnable` (1), `EmperorScorpion_health` (350), `EmperorScorpion_attack` (35), `EmperorScorpion_defense` (20), `PlayNicely` (0) (alle manifest config).
- **Portierung 1.21.1:**
  - `Monster`. HP 350 und Rüstung 20 liegen unter den Klemmgrenzen 1024/30, also keine virtuelle Gesundheit nötig.
  - Die Zielschleife als eigenes `Goal` oder direkt in `customServerAiStep` nachbauen, samt 1/4-Gate. `GenericTargetSorter` als `Comparator<LivingEntity>`.
  - Gift: `MobEffects.POISON`. Den Schwierigkeitsbug bewusst übernehmen (90/120 Ticks) oder dokumentiert korrigieren.
  - Unverwundbarkeit über `hurt()`-Override mit eigenem Zähler, zusätzlich zu `invulnerableTime`.
  - Verzauberungen sind in 1.21.1 Registry-Holder: `stack.enchant(registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), lvl)`. Achtung:
    - Doppeltes Sharpness schreibt in 1.7.10 zwei NBT-Einträge; `ItemEnchantments` hält pro Enchantment nur einen Wert (Upgrade auf das Maximum).
    - Stufen über dem Maximum (Feather Falling 9, Aqua Affinity 5) sind in 1.21.1 erlaubt, aber optisch ungewohnt.
  - Spawner-Scan ersetzen durch `MobSpawnType.SPAWNER` → true.
  - Kategorie `ambient` für einen Monster-Typ neu entscheiden: `AMBIENT` gilt in 1.21.1 als friendly, Peaceful-Verhalten und Cap unterscheiden sich.
  - Modell: `ModelEmperorScorpion` hat eigene Unter-Animationen `doLeftLeg`/`doRightLeg`/`doLeftClaw`/`doRightClaw`/`doTail` (anim_summary.txt:36), per Hand portieren.

### EnchantedCow - Enchanted Golden Apple Cow (`enchanted_golden_apple_cow`)

- **Rolle:** passive Kuh, `EnchantedCow → RedCow → EntityCow`. Keine eigenen Attribute (manifest `max_health: null`), also Vanilla-Kuh-Attribute und Vanilla-Kuh-KI.
- **Werte:** geerbt von `RedCow`:
  - mit 1/200 je Tick wird das Rache-Ziel gelöscht (RedCow.java:30-32)
  - **`canDespawn` false**, die Kuh ist dauerhaft (RedCow.java:36-38)
- **KI und Angriffe:** Vanilla `EntityCow` (Panik, Zucht mit Weizen, Folgen, Wandern). Nichts Eigenes.
- **Interaktion:** Vanilla-Kuh (Melken mit Eimer, Zucht mit Weizen). `createChild` → `EnchantedCow` (EnchantedCow.java:31-39).
- **Drops** (Reihenfolge):
  1. `apple` × `nextInt(4) + nextInt(1 + looting)` (EnchantedCow.java:23-25)
  2. **ein Stapel mit 2 `golden_apple` (Meta 0)** (:26)
  3. **1× verzauberter Goldapfel** (`golden_apple` Meta 1) bei y+1 (:16-19, :27)
  4. über `super` von `RedCow`: `apple` × `nextInt(3) + nextInt(1 + looting)` (RedCow.java:15-17)
  5. Vanilla `EntityCow`-Drops (Leder/Rindfleisch)

  Die Recherche (01-mobs.md:132) nennt „beef, leather, enchanted golden apples“; der Code liefert zusätzlich Äpfel und zwei normale Goldäpfel.
- **Spawnen:** keine eigene `getCanSpawnHere`, also Vanilla-`EntityAnimal`-Regel. Typ creature: forest w3 2-4, plains w3 2-4, megaTaiga w5 2-5, mushroomIsland w15 3-6 (OSM:4280-4283).
- **Zustand:** keiner.
- **Sounds:** Vanilla-Kuh.
- **Config:** `CowEnable` (1, manifest).
- **Portierung 1.21.1:**
  - `Cow`-Unterklasse über eine `RedCow`-Basis, die `removeWhenFarAway` → false und das Rache-Reset trägt.
  - Drops besser in Java (`dropCustomDeathLoot`) als in einer Loot-Table, weil das Looting-Additiv und die Reihenfolge exakt sein sollen. Eine Loot-Table ginge auch (`enchanted_golden_apple` ist in 1.21.1 ein eigenes Item).
  - Renderer: Textur `gold_cow.png` (RenderEnchantedCow.java:42-45) plus Glint-Pass: `shouldRenderPass` Pass 3 liefert 31 (RenderEnchantedCow.java:33-39). Deutung als Enchant-Glint-Bitmaske aus Vanilla `RendererLivingEntity`, im Repo nicht per javap geprüft.
  - In 1.21.1 eine `RenderLayer` mit `RenderType.entityGlint()`-artigem Overlay, unter `com.swbr.orespawn.client`.

### EnderKnight - Ender Knight (`ender_knight`)

- **Rolle:** feindlicher Enderman-Klon, Basis `EntityMob`. `isAIEnabled` **nicht** überschrieben, also alter KI-Pfad (`findPlayerToAttack` plus Vanilla `EntityMob.attackEntity`). Nimmt keine Blöcke auf.
- **Werte:**

  | Wert | Quelle |
  |---|---|
  | Health 60, Attack 12, Rüstung 6 aus `EnderKnight_stats` | EnderKnight.java:32-34, :215-217, manifest |
  | Speed 0.32 | :33 |
  | `stepHeight` 1.0 | :27 |
  | XP 5 | Vanilla `EntityMob`-Konstruktor, javap `yg.<init>`: `iconst_5 → experienceValue` |
  | Nahkampf: `attackTime` 20 Ticks, Reichweite < 2.0 plus vertikale BB-Überlappung | Vanilla `EntityMob.attackEntity`, javap `yg.a(sa,float)` |
  | Nässe: jeden Tick 1.0 `drown`-Schaden | :86-88 |
  | Angriffs-Speed-Modifier +6.2, Operation 0 (additiv), UUID `020E0DFB-87AE-4653-9556-831010E291A0`, nicht gespeichert; aktiv solange `entityToAttack != null` | :89-96, :279-280 |

  - offen: die effektive Verfolgungsgeschwindigkeit. Im alten KI-Pfad liefert `getAIMoveSpeed` für Nicht-KI-Entities 0.1 (javap `sv.bl`), und `moveFlying` normiert den Eingabebetrag auf mindestens 1 (javap `sa.a(FFF)`). Ob der Attributwert 6.52 dort überhaupt skaliert, hängt an `EntityCreature.updateEntityActionState`, das nicht geprüft wurde.
- **KI und Angriffe:**
  - `findPlayerToAttack` (:50-70):
    - `PlayNicely != 0` → nie.
    - Nächster verwundbarer Spieler in **64** (:54).
    - Nur wenn er den Knight ansieht: `shouldAttackPlayer` mit Kürbis-Ausnahme (`Blocks.pumpkin` im Helmslot) und `dot(look, dir) > 1 − 0.025/dist` plus Sichtlinie (:72-83).
    - `mob.endermen.stare` am Spieler, wenn `stareTimer == 0`; der Timer läuft bis 5 und springt dann zurück (:57-62).
    - Setzt „screaming“.
  - `onLivingUpdate` (:85-134):
    - 2 Portal-Partikel je Tick (:97-99).
    - Tagsüber (server): Helligkeit > 0.5, Himmel sichtbar und `rand·30 < (f − 0.4)·2` → Ziel weg, Zufallsteleport (:100-107).
    - Nass oder brennend → Zufallsteleport (:108-111).
    - Blickt auf das Ziel (`faceEntity 100/100`, :113-115).
    - Ziel ist ein hinsehender Spieler: bei `distSq < 16` weg-teleportieren, `teleportDelay = 0` (:118-123). Sonst bei `distSq > 256` und `teleportDelay++ >= 30` → `teleportToEntity` (:124-126).
  - `teleportRandomly`: x/z ±32, y `nextInt(64) − 32` (:136-141).
  - `teleportToEntity`: 16 Blöcke entlang der Verbindungslinie, dazu ±4 x/z und ±8 y (:143-151).
  - `teleportTo` (:153-201): sinkt auf einen bewegungsblockierenden Block. Keine Kollision, keine Flüssigkeit, sonst Abbruch. Danach 128 Portal-Partikel entlang des Pfades und `mob.endermen.portal` an Start und Ziel.
  - Treffer (:235-249): setzt „screaming“. Indirekter Schaden (Projektil) → bis zu 16 Teleportversuche; gelingt einer, entfällt der Schaden.
- **Interaktion:** keine.
- **Drops:** ein Item-Typ je Tod, 50/50 `ender_eye` oder `ender_pearl` (:219-224); Anzahl `nextInt(2 + looting)`, also 0..1+Looting (:226-233). XP 5 (s. o.).
- **Spawnen** (:251-268):
  - Spawner `"Ender Knight"` im Bereich x/z −3..+2, y 0..+4 → true.
  - Sonst `isValidLightLevel && !isDaytime && posY >= 30`.
  - Biome (Typ ambient): extremeHills/Edge, forest, forestHills, jungleHills w4; plains, river, desert w2; roofedForest w20, je 2-4 (OSM:4580-4588).
  - Spawner in `GenericDungeon.java:1900, 1905, 3355, 3365`.
  - Die Recherche nennt zusätzlich Spawns in der Danger-Dimension; in dieser Klasse steht davon nichts (offen, Dimension-Batch).
- **Zustand:** DW 18 int `screaming` 0/1 (:39, :270-276). NBT nur `super` (:42-48).
- **Sounds:** living `mob.endermen.scream` wenn screaming, sonst `mob.endermen.idle` (:203-205); hurt `mob.endermen.hit` (:208); death `mob.endermen.death` (:212); `mob.endermen.stare` und `mob.endermen.portal` siehe oben.
- **Config:** `EnderKnightEnable` (1), `EnderKnight_health` (60), `EnderKnight_attack` (12), `EnderKnight_defense` (6), `PlayNicely`.
- **Portierung 1.21.1:**
  - Am besten Vanilla `EnderMan` (1.21.1) als Vorlage kopieren, **ohne** Block-Tragen. Die Look-Detection ist identisch (`1 − 0.025/d`).
  - Kürbis → `Blocks.CARVED_PUMPKIN.asItem()`. Sounds: `SoundEvents.ENDERMAN_*`.
  - Indirekter Schaden → `source.is(DamageTypeTags.IS_PROJECTILE)`. Nässe → `isSensitiveToWater()` bzw. `hurt(damageSources().drown(), 1)`.
  - Suchradius 64 per `NearestAttackableTargetGoal` mit eigenem Prädikat.
  - Den Speed-Modifier **nicht** blind als +6.2 übernehmen; Vanilla 1.21.1 nutzt einen kleinen additiven Wert. Wegen der offenen Frage oben am Spielgefühl festlegen.
  - Alter KI-Pfad → `MeleeAttackGoal` (Cooldown 20 Ticks entspricht dem Vanilla-Verhalten).
  - Spawner-Scan → `MobSpawnType.SPAWNER`.

### EnderReaper - Ender Reaper (`ender_reaper`)

- **Rolle:** stärkere Variante von `EnderKnight`, Basis `EntityMob`, Code bis auf die unten genannten Punkte identisch (gleiche UUID des Speed-Modifiers, EnderReaper.java:287-288).
- **Werte:** Unterschiede zu `EnderKnight`:

  | Wert | Reaper | Quelle |
  |---|---|---|
  | Hitbox | 0.7 × 2.9 | :26 |
  | Speed | 0.37 | :33 |
  | Health / Attack / Rüstung | 90 / 18 / 8 aus `EnderReaper_stats` | :32-34, :219-221, manifest |
  | Spieler-Suchradius | **81** | :54 |

  Alles andere wie Knight: `stepHeight` 1.0 (:27), Modifier +6.2 additiv (:89-96, :288), Nässe 1.0 je Tick (:86-88), XP 5 und Nahkampf 20 Ticks / 2.0 (Vanilla `EntityMob`, javap).
- **KI und Angriffe:** identisch zu `EnderKnight` (EnderReaper.java:50-201, :232-246), inklusive Tageslicht-Teleport, Anstarr-Erkennung und 16 Ausweich-Teleports bei Projektilen.
- **Interaktion:** keine.
- **Drops:** nur `ender_eye` × `nextInt(2 + looting)` (:215-230).
- **Spawnen** (:248-276):
  - Spawner `"Ender Reaper"` (x/z −3..+2, y 0..+4) → true.
  - Sonst Licht gültig, Nacht, `posY >= 30` **und** kein anderer `EnderReaper` in `expand(16, 8, 16)` (:273-275).
  - Biome (ambient): extremeHills/Edge, forestHills, jungleHills w2; forest, plains, river, desert w1, je 1-2; roofedForest w38 2-4 (OSM:4591-4599).
  - Spawner in `GenericDungeon.java:1527, 2552-2576, 2943-2967, 3350, 3360`.
- **Zustand:** DW 18 int `screaming` (:39, :278-284). Kein eigenes NBT.
- **Sounds:** wie `EnderKnight` (:203-213).
- **Config:** `EnderReaperEnable` (1), `EnderReaper_health` (90), `EnderReaper_attack` (18), `EnderReaper_defense` (8), `PlayNicely`.
- **Portierung 1.21.1:** gemeinsame abstrakte Basis `AbstractEnderWarrior extends Monster` mit Parametern für Suchradius, Drop-Tabelle und Artgenossen-Abstand; sonst wie `EnderKnight`.

### EntityAnt - Ant (`ant`)

- **Rolle:** harmloser Portal-Kritter („Brown Ant“), Basis `EntityAnimal`. Oberklasse von `EntityRedAnt`, `EntityRainbowAnt`, `EntityUnstableAnt` und `Termite` (Textur-Weiche EntityAnt.java:40-54).
- **Werte:**
  - HP 1 (:93-95), Speed 0.15, jeden Tick neu gesetzt (:24, :61), Hitbox 0.1.
  - `experienceValue = 0` (:26) ist wirkungslos: `EntityAnimal.getExperiencePoints` gibt 1-3 (javap `wf.e`).
  - `attackDamage` 0.0 (:36-37).
- **KI und Angriffe:** Prio 0 `EntityAIPanic` 1.4 (:28), Prio 1 `MyEntityAIWanderALot` Radius 9, Speed 1.0 (:29). Meidet Wasser (:27). Mit 1/200 je Tick wird das Rache-Ziel gelöscht (:136-141). Kein Angriff.
- **Interaktion (Dimensionsportal):** nur serverseitig (`EntityPlayerMP`) und mit **leerer Hand** (:65-79).
  - Spieler nicht in `OreSpawnMain.DimensionID` → `transferPlayerToDimension` dorthin mit `OreSpawnTeleporter` (:80-82).
  - Sonst zurück nach Dimension 0 (:83-85).
  - `DimensionID = BaseDimensionID` (OSM:1266), Provider „Dimension-Utopia“ (manifest dimensions).
  - `createChild` null (:123-125), also keine Zucht.
- **Drops:** keine (:116-117). XP 1-3 über Vanilla `EntityAnimal`.
- **Spawnen:** nicht über Biome (manifest `spawns: []`). Gespawnt von `AntBlock.java:58` und `CrystalAntBlock.java:60` (Name `"Ant"`).
  - `getCanSpawnHere`: `posY >= 50` und höchstens 4 `EntityAnt` (inklusive Unterklassen) in `expand(20, 10, 20)` (:127-134).
  - `canDespawn = !isNoDespawnRequired()` (:56-58).
- **Zustand:** keine DataWatcher, kein NBT.
- **Sounds:** keine, Lautstärke 0 (:97-114). Trotzdem `canTriggerWalking` true mit leerem `playStepSound` (:113-121).
- **Config:** `BaseDimensionID` (80, manifest) über `DimensionID`.
- **Portierung 1.21.1:**
  - `Animal` mit `PanicGoal` und eigenem Wander-Goal. `mobInteract` → auf `ServerPlayer` und leere Hand prüfen.
  - Teleport per `player.changeDimension(new DimensionTransition(level, pos, …))`, Ziel-`ResourceKey<Level>` `orespawn:utopia` (Namen im Dimension-Batch festlegen). Der Rückweg geht in `Level.OVERWORLD`.
  - `OreSpawnTeleporter` (Plattform/Portal-Suche) wird als eigener Positionsfinder gebraucht.
  - `isFood` → false, damit keine Paarungsherzen erscheinen.
  - Textur-Weiche in den Renderer (client-Paket), Texturen `ant.png`, `red_ant.png`, `rainbow_ant.png`, `unstableant.png`, `termite.png` (texture_map).
  - Kann nicht per Critter Cage gefangen werden (siehe `EntityCage`).

### EntityButterfly - Butterfly (`butterfly`)

- **Rolle:** fliegende Ambient-Kreatur und Portal zur Chaos-Dimension, Basis `EntityAmbientCreature`. Oberklasse von `EntityLunaMoth` und `Mothra`. Typ 1 in `DimensionID4` ist der „Vampire Butterfly“.
- **Werte:**
  - `butterfly_type = OreSpawnRand.nextInt(4)` im Konstruktor (0-3, EntityButterfly.java:40). `OreSpawnRand` ist ein fest geseedeter `Random(151)` (OSM:6366).
  - HP 2 (:126-128), Speed 0.1, Attack 0.0 (:48-51).
  - Kein `experienceValue` gesetzt (Feld-Default).
  - Kein Fallschaden (:239-243), löst keine Druckplatten aus (:245-247), keine Kollisionen (:120-124), `canBePushed` true (:116-118).
- **KI und Angriffe:** eigene Flugsteuerung in `updateAITasks` (:134-168).
  - Neues Flugziel mit 1/100 je Tick oder bei `distSq < 4`: x/z ±6 (`nextInt(7) − nextInt(7)`), y −2..+3, bis zu 25 Versuche, bis der Zielblock Luft ist (:135, :143-146).
  - **Vampir-Angriff** (sonst-Zweig, :148-157): mit 1/10, nur in `DimensionID4`, nur `butterfly_type == 1`, nicht Peaceful.
    - Suche in `expand(8, 5, 8)`, sortiert (:204-218).
    - Ziele: Spieler außer Creative und `EntityHorse` (:181-202).
    - Flugziel = Ziel y+1; bei `distSq < 6` `attackEntityAsMob`.
  - `attackEntityAsMob`: 50 % Chance (`OreSpawnRand.nextInt(2)`), **1.0** Mob-Schaden, nicht auf Peaceful (:170-179).
  - Bewegung: `motion += (signum(d)·0.5 − motion)·0.1` für x/z, y mit 0.7 (:158-163). Yaw folgt der Bewegung, `moveForward = 0.5` (:164-167).
  - `onUpdate`: `motionY *= 0.6` (:222).
- **Interaktion (Dimensionsportal):** `EntityPlayerMP` mit leerer Hand → `DimensionID6` („Dimension-Chaos“, manifest; `BaseDimensionID + 5`, OSM:1271), in `DimensionID6` zurück nach 0 (:249-271).
  - Recherche (01-mobs.md:138) bestätigt „portal to Chaos Dim.“.
- **Drops:** keine (keine Overrides).
- **Spawnen** (:273-292):
  - Spawner `"Butterfly"` in x/z −3..+2, y 0..+4 → **setzt `butterfly_type = 1`** und true (Vampir-Spawner, :282-285). Spawner stehen in `GenericDungeon.java:2339, 2360, 2380, 2402, 2410`.
  - Sonst Block an der Position Luft, **Tag**, und `DimensionID4` oder `posY >= 50`.
  - Biome (ambient) laut manifest/OSM:4296-4311.
  - Weitere Quellen: `BlockButterflyPlant.java:38`, `CaterKiller.java:327, 463`, `Brutalfly.java:343`.
  - `canDespawn = !isNoDespawnRequired()` (:92-94).
- **Zustand:** DW 20 int `butterfly_type` (:89). Er wird nur alle 25 Ticks abgeglichen: Server schreibt, Client liest (:223-232). NBT `ButterflyType` (:297-305).
- **Sounds:** keine, Lautstärke 0 (:96-114).
- **Config:** `ButterflyEnable` (1), `BaseDimensionID` (80).
- **Portierung 1.21.1:**
  - `AmbientCreature` (wie `Bat`) mit eigener Flug-Tick-Logik in `customServerAiStep`; `NoGravity` bzw. `motionY`-Dämpfung wie im Original.
  - Typ als `EntityDataAccessor<Integer>`; der 25-Tick-Sync entfällt.
  - Texturwahl im Renderer (EntityButterfly.java:54-85):
    - Typ 0 `butterfly.png`
    - Typ 1 `butterfly2.png`, in `DimensionID4` `vbutterfly1.png`
    - Typ 2 `butterfly3.png`, Typ 3 `butterfly4.png`
    - `Mothra` `eyemoth.png`

    Der Client muss dazu die Dimension kennen (`level().dimension()`).
  - Spawner-Sonderfall: in `finalizeSpawn` mit `MobSpawnType.SPAWNER` Typ 1 setzen.
  - `DimensionID4` heißt intern „Dimension-Islands“, erreichbar über `EntityUnstableAnt` (EntityUnstableAnt.java:45); die Recherche nennt sie „Danger Dimension“.
  - Teleport wie `EntityAnt`.

### EntityCage - EntityCage (`entity_cage`)

- **Rolle:** Wurfgeschoss der Critter Cage, Basis `EntityThrowable`. Registriert als `"EntityCage"` mit 64/1/true (OSM:4779-4781). Geworfen nur von der **leeren** Cage (`cage_id` 160): `CritterCage.onItemRightClick` spielt `random.bow` (0.5), verbraucht außer im Creative-Modus ein Item und spawnt serverseitig (CritterCage.java:23-35); Stackgröße 16 (CritterCage.java:18).
- **Werte:**
  - Wurfgeschwindigkeit 1.5, Schwerkraft 0.03 (Vanilla `EntityThrowable`, javap `zk.e`/`zk.i`).
  - `my_index` Default 160 (EntityCage.java:22); nur für die Render-Ikone, nicht synchronisiert, nicht gespeichert.
  - Drehung +20° je Tick auf den Pitch (:849-858).
- **KI und Angriffe:** `onImpact` (:56-847):
  - **Entity getroffen und `nextInt(10) >= 2`** (80 %, :57):
    - Partikel (`smoke`/`explode`/`reddust` ×4) an der getroffenen Entity in `throwerWorld`, dazu `random.explode` (1.0, Pitch 1.5) **am Werfer** (:58-67).
    - Spieler getroffen → leere Cage zurück (:68-74).
    - Dann eine Kette **unabhängiger `if`s** (kein `else`). Treffer: Entity `setDead`, Item fällt an der Cage-Position.
    - Zeilen mit Fluchtchance: bei `rand < N` fällt stattdessen die leere Cage und die Methode endet.
    - Die Kuh-/Villager-Einträge beenden die Methode sofort nach dem Fang.
  - **Kein Treffer oder die 20 %:** leere Cage zurück (server, :841-843).
  - **Falle:** Eine Entity, die im 80-%-Zweig **in keiner Zeile** vorkommt (z. B. `EntityAnt`, `EntityButterfly`, `EntityMosquito`, `Termite`, gezähmte `Girlfriend`/`Boyfriend`, Roboter, alle ungelisteten Bosse), verbraucht die Cage ersatzlos (:844-846).
  - Die Klassenhierarchie ist überschneidungsfrei geprüft: nur `SpiderDriver → EntitySpider` (per `else if` getrennt) und `CrystalCow`/`EnchantedCow`/`GoldCow → RedCow → EntityCow` (per `return` getrennt).

  Fangtabelle (Item-Feld → Registry-Id aus dem manifest; „Flucht“ = Wahrscheinlichkeit, dass die Cage leer zurückkommt):

  | Treffer-Klasse | Item (id) | Anzahl | Flucht | Zeile |
  |---|---|---|---|---|
  | `SpiderDriver` | `cagespiderdriver` | 1 | – | 75-78 |
  | `EntityCaveSpider` | `cagecavespider` | 1 | – | 80-83 |
  | `EntitySpider` (sonst) | `cagespider` | 1 | – | 84-87 |
  | `Crab` | `cagecrab` | 1 | – | 89-92 |
  | `EntityBat` | `cagebat` | 2 | – | 93-96 |
  | `EntityPig` | `cagepig` | 1 | – | 97-100 |
  | `EntitySquid` | `cagesquid` | 1 | – | 101-104 |
  | `EntityChicken` | `cagechicken` | 1 | – | 105-108 |
  | `EntityCreeper` | `cagecreeper` | 1 | – | 109-112 |
  | `EntityHorse` | `cagehorse` | 1 | – | 113-116 |
  | `EntitySkeleton` Typ ≠ 0 | `cagewitherskeleton` | 1 | – | 117-121 |
  | `EntitySkeleton` Typ 0 | `cageskeleton` | 1 | – | 122-124 |
  | `EntityPigZombie` | `cagezombiepigman` | 1 | – | 128-131 |
  | `EntityZombie` (sonst) | `cagezombie` | 1 | – | 132-135 |
  | `EntityMagmaCube` | `cagemagmacube` | 1 | – | 138-141 |
  | `EntitySlime` (sonst) | `cageslime` | 1 | – | 142-145 |
  | `EntityGhast` | `cageghast` | 1 | 2/10 | 147-157 |
  | `EntityEnderman` | `cageenderman` | 1 | 2/10 | 158-168 |
  | `EntitySilverfish` | `cagesilverfish` | 2 | – | 169-172 |
  | `EntityWitch` | `cagewitch` | 1 | – | 173-176 |
  | `EntitySheep` | `cagesheep` | 1 | – | 177-180 |
  | `EntityWolf` | `cagewolf` | 1 | – | 181-184 |
  | `EntityOcelot` | `cageocelot` | 1 | – | 185-188 |
  | `EntityBlaze` | `cageblaze` | 1 | – | 189-192 |
  | `Girlfriend` (nur ungezähmt) | `cagegirlfriend` | 1 | – | 193-199 |
  | `Boyfriend` (nur ungezähmt) | `cageboyfriend` | 1 | – | 200-206 |
  | `EntityDragon` | `cageenderdragon` | 1 | 5/10 | 207-218 |
  | `EntityDragonPart` → `entityDragonObj` | `cageenderdragon` | 1 | 5/10 | 219-231 |
  | `EntitySnowman` | `cagesnowgolem` | 1 | – | 232-235 |
  | `EntityIronGolem` | `cageirongolem` | 1 | – | 236-239 |
  | `EntityWither` | `cagewitherboss` | 1 | 2/10 | 240-250 |
  | `CrystalCow` | `cagecrystalcow` | 1 | – (return) | 251-258 |
  | `EnchantedCow` | `cageenchantedcow` | 1 | – (return) | 259-266 |
  | `GoldCow` | `cagegoldcow` | 1 | – (return) | 267-274 |
  | `RedCow` | `cageredcow` | 1 | – (return) | 275-282 |
  | `EntityMooshroom` | `cagemooshroom` | 1 | – (return) | 283-287, 292-295 |
  | `EntityCow` (sonst) | `cagecow` | 1 | – (return) | 288-295 |
  | `EntityVillager` | `cagevillager` | 1 | – (return) | 297-304 |
  | `Mothra` | `cagemothra` | 1 | 4/10 | 305-315 |
  | `Alosaurus` | `cagealosaurus` | 1 | 4/10 | 316-326 |
  | `Cryolophosaurus` | `cagecryolophosaurus` | 1 | – | 327-330 |
  | `Camarasaurus` | `cagecamarasaurus` | 1 | – | 331-334 |
  | `VelocityRaptor` | `cagevelocityraptor` | 1 | – | 335-338 |
  | `Hydrolisc` | `cagehydrolisc` | 1 | – | 339-342 |
  | `Basilisk` | `cagebasilisc` | 1 | 6/10 | 343-353 |
  | `Dragonfly` | `cagedragonfly` | 2 | – | 354-357 |
  | `EmperorScorpion` | `cageemperorscorpion` | 1 | 7/10 | 358-368 |
  | `Cephadrome` | `cagecephadrome` | 1 | 7/10 | 369-379 |
  | `Dragon` | `cagedragon` | 1 | 7/10 | 380-390 |
  | `Scorpion` | `cagescorpion` | 1 | – | 391-394 |
  | `CaveFisher` | `cagecavefisher` | 1 | – | 395-398 |
  | `Spyro` | `cagespyro` | 1 | – | 399-402 |
  | `Baryonyx` | `cagebaryonyx` | 1 | – | 403-406 |
  | `GammaMetroid` | `cagegammametroid` | 1 | – | 407-410 |
  | `Cockateil` | `cagecockateil` | 4 | – | 411-414 |
  | `AttackSquid` | `cageattacksquid` | 6 | – | 415-418 |
  | `Kyuubi` | `cagekyuubi` | 1 | 3/10 | 419-429 |
  | `WaterDragon` | `cagewaterdragon` | 1 | 6/10 | 430-440 |
  | `Kraken` | `cagekraken` | 1 | **95/100** | 441-451 |
  | `Lizard` | `cagelizard` | 1 | 2/10 | 452-462 |
  | `Alien` | `cagealien` | 1 | 5/10 | 463-473 |
  | `Bee` | `cagebee` | 1 | 3/10 | 474-484 |
  | `Firefly` | `cagefirefly` | 1 | – | 485-488 |
  | `Chipmunk` | `cagechipmunk` | 1 | – | 489-492 |
  | `Gazelle` | `cagegazelle` | 1 | – | 493-496 |
  | `Ostrich` | `cageostrich` | 1 | – | 497-500 |
  | `TrooperBug` | `cagetrooper` | 1 | 6/10 | 501-511 |
  | `SpitBug` | `cagespit` | 1 | 3/10 | 512-522 |
  | `StinkBug` | `cagestink` | 1 | – | 523-526 |
  | `CreepingHorror` | `cagecreepinghorror` | 1 | – | 527-530 |
  | `TerribleTerror` | `cageterribleterror` | 1 | – | 531-534 |
  | `CliffRacer` | `cagecliffracer` | 1 | – | 535-538 |
  | `Triffid` | `cagetriffid` | 1 | 6/10 | 539-549 |
  | `PitchBlack` | `cagenightmare` | 1 | 7/10 | 550-560 |
  | `LurkingTerror` | `cagelurkingterror` | 1 | – | 561-564 |
  | `WormSmall` | `cagesmallworm` | 1 | – | 565-568 |
  | `WormMedium` | `cagemediumworm` | 1 | – | 569-572 |
  | `Cassowary` | `cagecassowary` | 1 | – | 573-576 |
  | `CloudShark` | `cagecloudshark` | 1 | – | 577-580 |
  | `GoldFish` | `cagegoldfish` | 1 | – | 581-584 |
  | `LeafMonster` | `cageleafmonster` | 1 | – | 585-588 |
  | `WormLarge` | `cagelargeworm` | 1 | 5/10 | 589-599 |
  | `EnderKnight` | `cageenderknight` | 1 | 3/10 | 600-610 |
  | `EnderReaper` | `cageenderreaper` | 1 | 2/10 | 611-621 |
  | `Beaver` | `cagebeaver` | 1 | – | 622-625 |
  | `Urchin` | `cageurchin` | 1 | – | 626-629 |
  | `Flounder` | `cageflounder` | 1 | – | 630-633 |
  | `Skate` | `cageskate` | 1 | – | 634-637 |
  | `Rotator` | `cagerotator` | 1 | – | 638-641 |
  | `Peacock` | `cagepeacock` | 1 | – | 642-645 |
  | `Fairy` | `cagefairy` | 1 | – | 646-649 |
  | `DungeonBeast` | `cagedungeonbeast` | 1 | – | 650-653 |
  | `Vortex` | `cagevortex` | 1 | 3/10 | 654-664 |
  | `Rat` | `cagerat` | 1 | – | 665-668 |
  | `Whale` | `cagewhale` | 1 | 2/10 | 669-679 |
  | `Irukandji` | `cageirukandji` | 1 | – | 680-683 |
  | `Stinky` | `cagestinky` | 1 | – | 684-687 |
  | `Mantis` | `cagemantis` | 1 | 3/10 | 688-698 |
  | `TRex` | `cagetrex` | 1 | 4/10 | 699-709 |
  | `HerculesBeetle` | `cagehercules` | 1 | 5/10 | 710-720 |
  | `EasterBunny` | `cageeasterbunny` | 1 | – | 721-724 |
  | `CaterKiller` | `cagecaterkiller` | 1 | 7/10 | 725-735 |
  | `Molenoid` | `cagemolenoid` | 1 | 5/10 | 736-746 |
  | `SeaMonster` | `cageseamonster` | 1 | 3/10 | 747-757 |
  | `SeaViper` | `cageseaviper` | 1 | 4/10 | 758-768 |
  | `RubberDucky` | `cagerubberducky` | 1 | – | 769-772 |
  | `Leon` | `cageleon` | 1 | 7/10 | 773-783 |
  | `Hammerhead` | `cagehammerhead` | 1 | 7/10 | 784-794 |
  | `BandP` | `cagecriminal` | 1 | – | 795-798 |
  | `Cricket` | `cagecricket` | 1 | – | 799-802 |
  | `Frog` | `cagefrog` | 1 | – | 803-806 |
  | `Brutalfly` | `cagebrutalfly` | 1 | 5/10 | 807-817 |
  | `Nastysaurus` | `cagenastysaurus` | 1 | 7/10 | 818-828 |
  | `Pointysaurus` | `cagepointysaurus` | 1 | 2/10 | 829-839 |

  Die Gesamt-Fangchance ist 0.8 × (1 − Flucht). Beispiel: `Kraken` 0.8 × 0.05 = 4 %.
- **Interaktion:** keine (Projektil). Das Freilassen (`CritterCage.onItemUse`) gehört zur Item-Klasse.
- **Drops:** siehe Tabelle; die leere Cage bei Fehlwurf, Fluchtwurf oder Spielertreffer. Gefangene Entities werden per `setDead` entfernt, also **ohne** Tod, Drops und XP.
- **Spawnen:** nie natürlich.
- **Zustand:** keine DataWatcher, kein NBT. `thrower`/`throwerWorld` sind nur Laufzeitfelder (:16-17).
- **Sounds:** `random.explode` (1.0, 1.5) am Werfer; beim Werfen `random.bow` (Item-Klasse).
- **Config:** keine.
- **Portierung 1.21.1:**
  - `ThrowableItemProjectile` (Item = leere Cage), `onHitEntity`/`onHit` **nur serverseitig**. Im Original laufen `setDead` und `dropItem` beim Fang auch clientseitig und erzeugen Geister-Items; das nicht übernehmen.
  - Fangregeln als geordnete Liste `(Predicate<Entity> bzw. Class, Supplier<Item>, count, escapeChance)`. `instanceof`-Semantik und Reihenfolge beibehalten.
  - Vanilla-Klassen neu abbilden:
    - `EntitySkeleton` Typ 1 → `WitherSkeleton`
    - `EntityHorse` → `AbstractHorse`: Esel, Maultier, Skelett- und Zombiepferd fallen mit darunter; in 1.7.10 waren das Horse-Typen
    - `EntityPigZombie` → `ZombifiedPiglin` (erbt in 1.21.1 von `Zombie`, also vor `Zombie` prüfen)
    - `Husk`/`Drowned`/`ZombieVillager` fallen unter `Zombie`
    - `EntityDragonPart` → `EnderDragonPart.parentMob`
    - `MushroomCow` vor `Cow`
  - Entfernen per `discard()`.
  - Partikel über `ServerLevel.sendParticles` an der Trefferposition statt `throwerWorld.spawnParticle`, das auf einem dedizierten Server nichts anzeigt.
  - Die ersatzlos verbrauchte Cage bei ungelisteten Entities ist Originalverhalten; bewusst entscheiden.
  - Renderer: `RenderSpinner`-Ikone (`spinners.png`, Index = `getCageIndex()`, RenderCage.java:9-13) → `ThrownItemRenderer` mit Cage-Item.

### EntityCannonFodder - (keine Registry-Id)

- **Rolle:** gemeinsame Basis `EntityTameable` für `Chipmunk`, `Lizard`, `Ostrich` und `VelocityRaptor` (je `extends EntityCannonFodder`). Selbst nicht registriert, keine Attribute (:40-42).
- **Werte:**
  - Rüstung 3, wenn `is_activated == 2`, sonst 0 (:335-340).
  - Heilung +1.0 mit 1/250 je Tick (:384-386).
  - Nahkampfschaden `dm` und Frequenzen je Unterklasse (:357-371):

  | Klasse | `dm` Schaden | `sfreq` | `pfreq` |
  |---|---|---|---|
  | Default (`Ostrich`) | 4.0 | 7 | 5 |
  | `Chipmunk` | 3.0 | 6 | 5 |
  | `Lizard` | 6.0 | 8 | 5 |
  | `VelocityRaptor` | 4.0 | 6 | 4 |

- **KI und Angriffe** (`updateAITasks`, :346-387):
  - Mit 1/200 wird das Rache-Ziel gelöscht (:351-353).
  - **Nur bei `is_activated == 2`** (:354-356), nicht Peaceful, mit 1/`pfreq` je Tick: Ziel suchen in `expand(10, 4, 10)`, sortiert mit `GenericTargetSorter` (:319-333). Dann `tryMoveToEntityLiving(1.25)`, bei `distSq < 9` und (`nextInt(sfreq+1)==0 || nextInt(sfreq)==1`) Schaden `dm` als Mob-Schaden (:372-379, :342-344).
  - Ohne Ziel, aber sitzend: zurück zum Patrouillenpunkt mit 0.65 (:380-382).
  - Zielregeln (:280-317):
    - nicht Peaceful, lebend, sichtbar
    - sitzend nur innerhalb von 12 Blöcken um den Patrouillenpunkt (`distSq ≤ 144`)
    - jedes `EntityMob`
    - andere `EntityCannonFodder` mit Hutfarbe ≠ 0 und ≠ eigener Farbe (Team-Krieg nach Hutfarbe)
    - Spieler außer Creative und außer den Besitzern `name_one`/`name_two`
  - `PlayNicely` wird **nicht** abgefragt.
- **Interaktion** (`interact`, :66-209):
  1. Zuerst `super.interact` (Vanilla `EntityTameable`/`EntityAnimal`); liefert das true, ist Schluss (:72-74).
  2. Besitzer-Tausch, wenn gezähmt und `name_one` gesetzt (:75-96); läuft ohne `return` weiter:
     - Klickt `name_one` und `name_two` ist leer → `name_two = name_one`, `is_activated = 2`.
     - Klickt ein Fremder und `name_two` ist gesetzt → **Abbruch** (`return true`).
     - Klickt `name_two` → die beiden Besitzer tauschen, `is_activated = 2`.
     - Klickt ein Fremder und `name_two` ist leer → der Fremde wird `name_one`, der alte Besitzer `name_two`, `is_activated = 2`.

     Folge: Wer nach dem Zähmen nie selbst klickt, lässt jeden anderen Spieler Mitbesitzer werden.
  3. Futter im Abstand `distSq < 16` setzt die Hutfarbe: `carrot` → 1 (:97-118), `potato` → 3 (:119-140), `MyQuinoa` → 2 (:141-162). Jeweils:
     - `name_one` setzen, falls leer; `is_activated` 0 → 1
     - `setTamed(true)`, `func_152115_b` (= `EntityTameable.setOwner(String)`, joined.srg:20231)
     - Herzen, volle Heilung, `func_110163_bv` (= `enablePersistence`), 1 Item verbraucht
  4. `MyCornCob` bei `is_activated == 2` → **Klon** per Name (`"Ostrich"` / `"Lizard"` / `"Chipmunk"` / `"Velocity Raptor"`) bei x/z + rand(0..1), y+0.01. Übernimmt Besitzer (`func_152113_b` = `getOwner`, joined.srg:20229), Tame-Status, Hutfarbe, Aktivierung und beide Namen. Sound `random.explode` (0.75, 2.0), 1 Kolben verbraucht (:163-193).
  5. Sonst bei `is_activated == 2` und `distSq < 16`: Sitzen umschalten; beim Hinsetzen wird der Patrouillenpunkt `px/py/pz` gespeichert (:194-207).
- **Drops:** keine eigenen. `createChild` null (:389-391).
- **Spawnen:** keine eigene Regel (siehe Unterklassen).
- **Zustand:** DW 20 int `is_activated` (0 wild, 1 gezähmt, 2 aktiv), DW 21 int `hat_color` (0 keine, 1 Karotte, 2 Quinoa, 3 Kartoffel) (:46-47). Abgleich nur alle 6 Ticks, Server schreibt, Client liest (:50-64).
  - NBT: `NameOne`, `NameTwo` (leerer String = null), `IsActivated`, `HatColor`, `PatrolX`, `PatrolY`, `PatrolZ` (:238-257). Beim Lesen macht ein gesetztes `NameOne` das Tier gezähmt, mit `NameOne` als Besitzer (:259-278).
- **Sounds:** `random.explode` beim Klonen; sonst Unterklassen.
- **Config:** keine.
- **Portierung 1.21.1:**
  - `abstract class CannonFodderEntity extends TamableAnimal`.
  - `name_one`/`name_two` als `UUID` (Owner über `setOwnerUUID`, zweiter Besitzer als eigenes Feld). `is_activated`/`hat_color` als `EntityDataAccessor<Integer>`, der 6-Tick-Sync entfällt.
  - Sitzen: `setOrderedToSit` plus `setInSittingPose`; Patrouillenpunkt als `BlockPos` im NBT unter denselben Schlüsseln.
  - Klonen per `EntityType.create(level)` der eigenen Klasse statt Namenslookup; Unterklassen-Parameter (`dm`, `sfreq`, `pfreq`) als abstrakte Getter statt `instanceof`.
  - Die Besitzer-Tauschlogik exakt nachbauen oder dokumentiert härten.
  - `func_110163_bv` → `setPersistenceRequired()`.

### EntityLavaLovingItem - (keine Registry-Id)

- **Rolle:** `EntityItem`-Unterklasse, die Feuer und Lava überlebt. **Im Quellbaum nirgends instanziiert**: die einzige Fundstelle ist die eigene Datei (grep über `src-20.2`). Nicht per `registerModEntity` registriert.
- **Werte:** Konstruktor: `fireResistance = 300`, `isImmuneToFire = true`, `hurtResistantTime = 300` (EntityLavaLovingItem.java:12-14). `noFire()` setzt dieselben Werte (:17-21), `yesFire()` setzt 0/false/0 (:23-27). `dealFireDamage` wirkt nur ohne Immunität (:29-33).
- **KI und Angriffe:** keine.
- **Interaktion:** keine (Vanilla-Aufheben).
- **Drops:** –
- **Spawnen:** nie.
- **Zustand:** keiner.
- **Sounds:** keine.
- **Config:** keine.
- **Portierung 1.21.1:** vermutlich entbehrlich (Totcode). Falls ein Item lavafest liegen bleiben soll, genügt in 1.21.1 `Item.Properties().fireResistant()` (Datenkomponente, `ItemEntity.fireImmune()` wertet sie aus); eine eigene Entity-Klasse ist dann unnötig.

### EntityLunaMoth - Moth (`moth`)

- **Rolle:** nächtlicher Falter, `EntityLunaMoth → EntityButterfly → EntityAmbientCreature`. Fliegt nachts Fackeln an.
- **Werte:**
  - `moth_type = OreSpawnRand.nextInt(4)` (EntityLunaMoth.java:26), **weder synchronisiert noch gespeichert**. Client und Server würfeln unabhängig, nach dem Neuladen wird neu gewürfelt.
  - Hitbox 0.5 (:27); HP 2 geerbt (EntityButterfly.java:126-128), Speed 0.1, Attack 0.0 (:34-36).
  - Geerbt, aber ebenfalls gewürfelt: `butterfly_type` (DW 20, NBT `ButterflyType`).
- **KI und Angriffe** (`updateAITasks`, :143-182):
  - Ruft **zuerst** `super.updateAITasks()` auf (:148). Die komplette Butterfly-Flug- und Vampirlogik läuft also mit, einschließlich des 1.0-Schadens gegen Spieler und Pferde in `DimensionID4`, wenn der geerbte `butterfly_type == 1` ist.
  - Danach eigene Logik mit eigenem, verdecktem `currentFlightTarget` (:11):
    - Neues Ziel mit 1/100 oder bei `distSq < 4`: x/z ±9 (`nextInt(10) − nextInt(10)`), y −2..+3, 25 Versuche (:152-155).
    - Sonst **nachts** mit 1/10: Fackelsuche über `scan_it` in Würfelschalen um die Position (:157-171). Radius i = 2, 3, 4, 5, 6, 8, 10, 12, 14 (ab 6 wird ein Schritt übersprungen, :163-167). Treffer sind `Blocks.torch` und `OreSpawnMain.ExtremeTorch` (:59-140). Stopp an der ersten Schale mit Treffer; Flugziel = nächste Fackel y+1 (:168-170).
  - Bewegung wird nach der Butterfly-Bewegung **zusätzlich** angewandt: x/z 0.5, y 0.68, Faktor 0.1 (:175-177); `moveForward = 0.75` (:180).
  - `onUpdate`: `motionY *= 0.6` zusätzlich zur Butterfly-Dämpfung, effektiv ×0.36 je Tick (:54-57).
- **Interaktion:** geerbt: leere Hand → Teleport in die Chaos-Dimension `DimensionID6` bzw. zurück (EntityButterfly.java:249-271).
- **Drops:** keine.
- **Spawnen:** `getCanSpawnHere`: Block an der Position Luft, **Nacht**, und `DimensionID4` oder `posY >= 50` (:198-201). Kein Spawner-Sonderfall.
  - Biome (ambient) OSM:4314-4328.
  - `BlockMothPlant.java:38`.
  - `canDespawn` geerbt `!isNoDespawnRequired()`.
- **Zustand:** keine eigenen DW/NBT; geerbt DW 20 und `ButterflyType`.
- **Sounds:** keine (geerbt).
- **Config:** `MothEnable` (1).
- **Portierung 1.21.1:**
  - Unterklasse der portierten Butterfly-Klasse. **Entscheiden:** `moth_type` als `EntityDataAccessor` synchronisieren und speichern (Fix) statt des Client-Zufalls.
  - Die doppelte Bewegung und die geerbte Vampirlogik exakt nachbilden oder bewusst trennen.
  - Fackelsuche: `BlockPos.betweenClosed` pro Schale, Tag-Prüfung `Blocks.TORCH`/`WALL_TORCH` (1.7.10 hatte nur einen Fackel-Block) plus `ExtremeTorch`.
  - Texturen je `moth_type` (EntityButterfly.java:58-69): 0 `lunamoth.png`, 1 `eyemoth.png`, 2 `darkmoth.png`, 3 `firemoth.png`.
  - Leucht-Pass für `moth_type == 0` (und `Mothra`): `creeper_armor.png` scrollt mit `ticks·0.01`, additiv geblendet, Farbe 0.5 (RenderButterfly.java:42-80). In 1.21.1 als `RenderLayer` mit `RenderType.energySwirl(texture, u, v)`.
  - Renderer-Skalierung 0.75 (manifest).

### EntityMosquito - Mosquito (`mosquito`)

- **Rolle:** lästige Ambient-Kreatur, Basis `EntityAmbientCreature`. **Greift nie an**, umschwirrt nur Spieler.
- **Werte:**
  - HP 2 (:69-71), Speed 0.1, Attack 0.0 (:25-28), Hitbox 0.2 (:18).
  - **XP 5** (:20); `EntityAmbientCreature` wertet `experienceValue` direkt aus, anders als `EntityAnimal`.
  - `canBePushed` false (:59-61), keine Kollisionen (:63-67), kein Fallschaden (:126-130), keine Druckplatten (:132-134).
- **KI und Angriffe** (`updateAITasks`, :82-120):
  - Neues Ziel mit 1/20 je Tick oder bei `distSq < 3` (:91):
    - Mit 1/4 (`OreSpawnRand`) wird der nächste Spieler in `expand(10, 6, 10)` gesucht. Gefunden → Flugziel über seinem Kopf (y+2); nicht gefunden → Zufallsziel (:93-103).
    - Sonst Zufallsziel: x/z ±5 (`nextInt(6) − nextInt(6)`), y −2..+3, bis zu 50 Versuche bis Luft (:83, :104-108).
  - Bewegung x/z 0.5, y 0.7, Faktor 0.1 (:110-115), `moveForward = 0.3` (:118). `onUpdate`: `motionY *= 0.6` (:79).
- **Interaktion:** keine.
- **Drops:** keine. XP 5.
- **Spawnen:** `getCanSpawnHere` immer true (:136-138). Biome (ambient): swampland w30 5-10, jungle/jungleHills w20 5-10, roofedForest w15 2-5 (OSM:4438-4441). Dazu `BlockMosquitoPlant.java:39`. `canDespawn = !isNoDespawnRequired()` (:35-37).
- **Zustand:** keiner.
- **Sounds:** Living-Sound `orespawn:mosquito`, Lautstärke 0.4, Pitch 1.5 (:39-49); kein Hurt- oder Death-Sound.
- **Config:** `MosquitoEnable` (1).
- **Portierung 1.21.1:**
  - `AmbientCreature` mit Flug-Tick wie Butterfly. `getAmbientSoundInterval` bleibt Vanilla (1.7.10 `EntityLiving`-Takt).
  - `isPushable` false, `doPush`/`pushEntities` leer.
  - `getBaseExperienceReward` → 5.
  - Spawn-Prädikat `(type, level, reason, pos, rand) -> true`.
  - Kann nicht per Cage gefangen werden (siehe `EntityCage`).
