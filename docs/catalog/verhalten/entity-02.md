# Verhalten: entity-02

Dieser Stapel enthält die „Royals"-Haustiere der King/Queen-Linie. Alle vier Klassen erben von `EntityTameable`, spawnen nicht natürlich und sind als Begleiter gedacht. Die Prince-Linie hat drei Stufen: `ThePrince` (Kleinkind, 500 HP) wächst zu `ThePrinceTeen` (1500 HP, reitbar), der zu `ThePrinceAdult` (3000 HP, reitbar, riesig). Die letzte Stufe verwandelt sich mit `FullPowerKingEnable` in einen freien, feindlichen `TheKing`. `ThePrincess` hat nur die Kleinkindform; sie verzaubert als Zusatz die Umgebung mit Blumen und Tieren und feuert im Kampf `PurplePower`-Kugeln. Alle Stufen teilen dasselbe Muster:
- zwei Modi (Boden-KI gegen eigene Flugsteuerung),
- drei Fernangriffe: `BetterFireball`, `ThunderBolt` und `IceBall` mit Eisblöcken,
- an- und abschaltbar mit Eis bzw. Feuerzeug,
- Immunität gegen Royals-Projektile über `MyUtils.isRoyalty`.

Für die Portierung fallen vor allem vier Punkte ins Gewicht:
- Teen und Adult liegen über dem Health-Deckel von 1024 und brauchen deshalb virtuelles Leben.
- Die Flugphysik läuft an `onLivingUpdate` vorbei.
- Das Hochflug-Tastenflag `OreSpawnMain.flyup_keystate` ist global statt pro Spieler.
- Die Blockveränderungen durch `IceBall` und die Princess sind an keine oder nur teilweise an `mobGriefing` gebunden.

Gemeinsame Nachschlage-Werte, die alle vier Klassen nutzen:

| Projektil | Schaden | Nebeneffekt | Quelle |
|---|---|---|---|
| `BetterFireball` normal | 10, Brand 5 s | Explosion Stärke `field_92012_e` mit Feuer, `mobGriefing`; `setBig()` = 2, Default 1 | (BetterFireball.java:55, 77-79, 236-238, 279-281) |
| `BetterFireball` `setSmall()` | 5, Brand 5 s | keine Explosion, Hitbox 0.3125 | (BetterFireball.java:85-88, 240-242, 279) |
| `BetterFireball`, jedes Ziel mit `width*height > 30` (ausser Royals, Godzilla, GodzillaHead, PitchBlack, Kraken) | Leben wird zusätzlich halbiert | – | (BetterFireball.java:232-234) |
| `BetterFireball` `setNotMe()` | trifft keine `EntityPlayer`, `Dragon`, `Mothra` | – | (BetterFireball.java:155-158, 225-228) |
| `ThunderBolt` | 20 Wurfschaden + 20 Mob-Schaden, Brand 1 s | Explosion 3.0 (`mobGriefing`) + echter `EntityLightningBolt` | (ThunderBolt.java:29-36, 45, 47) |
| `IceBall` (`LaserBall` mit `is_iceball`) | 16, kein Brand; trifft keinen Spieler, der reitet | Explosion 3.0 (`mobGriefing`); mit `setIceMaker(1)` fünf `Blocks.ice` im Umkreis ±3 um den Treffpunkt, **ohne** `mobGriefing`-Prüfung und über jeden Block | (LaserBall.java:97, 146-156, 172-173; IceBall.java:58-80) |
| alle drei | Royals (`isRoyalty`) werden nicht getroffen | – | (BetterFireball.java:151; ThunderBolt.java:30-33; IceBall.java:58-61; MyUtils.java:9-11) |

`GenericTargetSorter` sortiert nach Abstand². Creeper zählen halb, Ziele mit `width*height > 1` werden durch diese Fläche geteilt, grosse Ziele also bevorzugt (GenericTargetSorter.java:16-33). `MyEntityAIFollowOwner` läuft, wenn das Tier nicht sitzt und entweder (unter y 60 oder nachts) und Abstand² > (max/2)² gilt oder Abstand² ≥ max² (MyEntityAIFollowOwner.java:39). Scheitert der Pfad bei Abstand² ≥ 144, teleportiert es sich neben den Besitzer; neu geplant wird alle 10 Ticks (MyEntityAIFollowOwner.java:72-83). `MyEntityAIWander` startet mit Chance 1/90 und sucht ein Ziel im Radius 10/7 (MyEntityAIWander.java:23, 29).

---

### ThePrince - The Prince (`the_prince`)

- **Rolle:** Kleinkind-Drache, drei Köpfe, `EntityTameable`. Zahm, zähmt sich selbst, fliegt im Aktivitätsmodus 2 und ist keine Bossstufe. Er greift nur Monster und einige Insekten an, nie Spieler.
- **Werte (nur was das manifest nicht hat):**
  - XP 50 (ThePrince.java:68).
  - Nahkampfschaden 10 über `getAttackStrength` (ThePrince.java:369-371).
  - Regeneration +1 HP mit Chance 1/200 pro KI-Tick (ThePrince.java:504-506).
  - `fireResistance` 1000, `isImmuneToFire` (ThePrince.java:56-57).
  - Lauftempo: das manifest listet 0.3 und 0.32. Wirksam ist 0.32 (ThePrince.java:55), gesetzt in jedem Tick in `onLivingUpdate` (ThePrince.java:470). Hintergrund: `applyEntityAttributes` läuft im Superkonstruktor, bevor das Feld initialisiert ist (Vanilla 1.7.10, nicht in dieser Quelle).
  - Kein Fallschaden (ThePrince.java:351-355).
  - Immun gegen `inWall` und `cactus` (ThePrince.java:387-390).
  - Wachstumsschwelle: `kill_count > 25 && fed_count > 10 && day_count > 10` (ThePrince.java:552).
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 1 | `EntityAISwimming` | – |
    | 2 | `MyEntityAIFollowOwner` | Tempo 1.15, max 12, min 2 |
    | 3 | `EntityAITempt` | 1.25, `Items.beef`, false |
    | 4 | `EntityAIWatchClosest` | `EntityLiving`, 6 |
    | 5 | `MyEntityAIWander` | 0.75 |
    | 6 | `EntityAILookIdle` | – |
    | 7 | `EntityAIMoveIndoors` | – |

    Quelle: (ThePrince.java:60-66). Keine `targetTasks`.
  - Aktivität (DW 21): 1 = Boden, 2 = Flug. Im Flug läuft `super.updateAITasks()` nicht, die Vanilla-Tasks ruhen also, und `noClip = true` gilt: er fliegt durch Blöcke (ThePrince.java:404-409, 501-503). Im Flug wird `motionY *= 0.6` pro Tick gerechnet, im Wasser `motionY += 0.07` (ThePrince.java:472-474, 489-491).
  - Moduswechsel (nur wenn er nicht sitzt):
    - Chance 1/100 pro Tick, dann 1/20 Flug, sonst Boden (ThePrince.java:521-528).
    - Fliegt der Besitzer (`capabilities.isFlying`), fliegt er auch (ThePrince.java:529-536).
    - Zahm am Boden und Besitzer-Abstand² > 256: Flug (ThePrince.java:537-542).
    - Sitzend und Besitzer-Abstand² > 256: aufstehen und fliegen (ThePrince.java:545-551).
    - Jeder erlittene Schaden: aufstehen und fliegen (ThePrince.java:392-393).
  - Zielsuche in `do_movement`:
    - Chance 1/7 pro Tick, nicht auf Peaceful (ThePrince.java:616).
    - Suchbox `boundingBox.expand(12, 6, 12)`, sortiert, erster passender Treffer mit freiem Raytrace ab `posY+0.75` (ThePrince.java:741-758, 398-400).
    - Passend sind `EntityMob`, `Mothra`, `EntityButterfly`, `Cockateil`, `Dragonfly` und `EntityMosquito`: lebend, sichtbar, keine Royals (ThePrince.java:737-739).
    - `OreSpawnMain.PlayNicely != 0` schaltet die Suche ab (ThePrince.java:742-744).
  - Flucht: zahm und Leben < 25 % → Flugziel gespiegelt vom Gegner weg, `setAttacking(0)` (ThePrince.java:619-623).
  - Angriff (sonst): Flug, `setAttacking(1)`, Flugziel = Gegner +1 y (ThePrince.java:626-629).
    - **Nahkampf**, wenn Abstand² < (3 + Zielbreite/2)²: 10 Schaden, `kill_count++`, falls das Ziel danach ≤ 0 HP hat (ThePrince.java:630-632, 373-383). Kein Cooldown; die Rate folgt aus der 1/7-Chance.
    - **Fernkampf**, wenn 25 < Abstand² < 144, nicht im Wasser, `SpyroFire != 0` und (`rand(3)==0 || rand(4)==1`) (ThePrince.java:633). Dann `rand(3)`: Feuerball, Blitz oder Eisball, jeweils nur, wenn das Ziel innerhalb von 0.5 rad der Blickrichtung liegt (ThePrince.java:634-670).
  - Abschussdetails:
    - Startpunkt 3.0 vor dem Körper, 1.0 hoch (ThePrince.java:761-762, 786-787, 811-812).
    - Feuerball: `setBig()`, zu 50 % zusätzlich `setSmall()`; Streuung ±5/±3/±5 auf den Richtungsvektor (ThePrince.java:766-775).
    - Blitz und Eisball: `setThrowableHeading(..., 1.4, 4.0)`, danach Bewegung ×3; der Eisball mit `setIceMaker(1)` (ThePrince.java:799-805, 823, 829-835).
    - Schaden siehe Kopftabelle.
  - Flugsteuerung:
    - Neues Wegziel, wenn das alte erreicht ist (< 2.1), mit Chance 1/300, bei Besitzer-Abstand² > 100 oder bei fliegendem Besitzer und Abstand² > 36 (ThePrince.java:600-614, 681-683).
    - Wegziel um den Besitzer ±6..9 (fliegender Besitzer: 0..7) bzw. um sich selbst ±6..10; y + `rand(6+2*owner_flying)-2`; bis zu 10 Versuche, bis Luft getroffen wird (ThePrince.java:684-713).
    - Bewegung: horizontal `(signum*0.5 - motion)*0.15*sf`, vertikal `(signum*0.7 - motionY)*0.21*sf`. `sf` = 1.0, bei fliegendem Besitzer 1.75, dazu bei Abstand² > 49 3.5. `moveForward = 0.75*sf`, Drehung Yaw/3 (ThePrince.java:715-734).
  - Beute-Vergessen: `setRevengeTarget(null)` mit Chance 1/200 (ThePrince.java:498-500).
- **Interaktion:** Spieler-Abstand² jeweils < 16; nicht Kreativ = 1 Item verbraucht.

  | Hand | Bedingung | Wirkung | Quelle |
  |---|---|---|---|
  | `Blocks.diamond_block` | keine (auch wild) | zähmt auf den Spieler, volle Heilung, `ok_to_grow=1`, kill/fed/day = 1000 → wächst im nächsten KI-Tick zum Teen | (ThePrince.java:168-188) |
  | beliebiges `ItemFood` | zahm + Besitzer | heilt `getHealAmount*10` (`func_150905_g`, MCP getHealAmount), `fed_count++` | (ThePrince.java:189-207) |
  | `Blocks.ice` | zahm + Besitzer | `SpyroFire=0`, Chat „Prince fireballs extinguished." | (ThePrince.java:208-225) |
  | `Items.flint_and_steel` | zahm + Besitzer | `SpyroFire=1`, Chat „Prince fireballs lit!"; das Feuerzeug wird per `stackSize--` **verbraucht**, nicht beschädigt | (ThePrince.java:226-243) |
  | `Items.diamond` | Besitzer, Server, `ok_to_grow != 0` | spawnt „The Young Prince", überträgt Zahmheit und Besitzer, entfernt sich | (ThePrince.java:244-264) |
  | `Items.name_tag` | zahm + Besitzer | Name setzen | (ThePrince.java:265-275) |
  | sonst/leer | zahm + Besitzer | Sitzen umschalten; Hinsetzen setzt Aktivität 1 | (ThePrince.java:276-285) |

  - **Selbstzähmung:** wild und ein Spieler innerhalb von 10 Blöcken → sofort zahm auf diesen Spieler mit voller Heilung (ThePrince.java:507-516). Ein Prince aus Ei oder King-Tod gehört also dem nächsten Spieler.
  - `ok_to_grow` bleibt ohne Diamantblock oder Teen-Rückbildung 0; der Diamant schaltet dann nur das Sitzen um.
  - Wachstum auf natürlichem Weg: `kill_count > 25`, `fed_count > 10`, `day_count > 10` → Teen mit übertragenem Besitzer (`func_152113_b`) (ThePrince.java:552-564). Kills zählen nur im Nahkampf, Tage als Nacht→Tag-Übergänge, die ein KI-Tick beobachtet (ThePrince.java:565-579).
  - `isWheat` = `Items.beef` (ThePrince.java:296-298); `createChild` = null, also keine Nachkommen (ThePrince.java:365-367). Reiten: nein.
- **Drops:** 1-4 `Items.beef` (`rand(4)+1`) (ThePrince.java:334-341); XP 50 (ThePrince.java:68).
- **Spawnen:**
  - `getCanSpawnHere` = true (ThePrince.java:361-363), aber keine natürlichen Spawns (manifest `spawns: []`). `canDespawn` = false (ThePrince.java:300-302).
  - Quellen:
    - `TheKing` spawnt beim Tod direkt die Entity bei y+10 (TheKing.java:165).
    - Ei `eggtheprince` (ItemSpawnEgg Index 351, ItemSpawnEgg.java:415-417):
      - Drop von `TheQueen` (TheQueen.java:180), `ThePrinceTeen` und `ThePrinceAdult`,
      - Truhe des Level-6-Dungeons mit Hammerhead-Spawner (GenericDungeon.java:741-745, 789-792),
      - `EasterBunny` legt mit Chance 1/600 pro KI-Tick ein Ei, Prince bei `rand(115)==91` (EasterBunny.java:110-121, 466-468).
  - Abweichung zur Recherche: 01-mobs.md nennt beim King ein „Prince egg"; der Code spawnt dort das Tier selbst.
- **Zustand:**

  | DW | Default | Bedeutung | NBT-Schlüssel |
  |---|---|---|---|
  | 20 | 1 | `SpyroFire`: Fernangriffe an/aus | `SpyroFire` |
  | 21 | 1 | Aktivität 1 Boden / 2 Flug | `SpyroActivity` |
  | 22 | 0 | `attacking` (Modellpose, Brüllen) | – (nicht gespeichert) |

  - Quelle DW: (ThePrince.java:82-84). Weitere NBT-Schlüssel: `SpyroGrow` (`ok_to_grow`), `SpyroKill`, `SpyroFed`, `SpyroDay` (ThePrince.java:90-109).
  - Sitzen und Besitzer liegen im Vanilla-`EntityTameable` (in dieser Quelle nicht belegt).
  - `head1ext..head3ext` (0..60, Schritt ±2 oder 0, Richtungswechsel 1/10 pro Tick) sind **nicht synchronisiert**; beide Seiten würfeln sie lokal (ThePrince.java:410-466). Das Modell liest sie zusammen mit `getActivity`, `getAttacking` und `isSitting` (ModelThePrince.java:233-343).
- **Sounds:**
  - Ambient `orespawn:roar`, nur wenn er nicht sitzt und `attacking != 0` (ThePrince.java:304-312).
  - Hurt `orespawn:duck_hurt`, Death `orespawn:cryo_death` (ThePrince.java:314-320).
  - Lautstärke 0.6; Tonhöhe `(r-r)*0.2+1.3` (ThePrince.java:322-324, 343-345).
  - Jeder Schuss `random.bow`, 1.0, `1/(r*0.4+0.8)` (ThePrince.java:776, 789, 818).
  - `spawnCreature` spielt beim Neuling `playLivingSound` (ThePrince.java:845).
- **Config:** `PlayNicely` (OreSpawnTWEAKS, Default 0, manifest) → keine Zielsuche. `ThePrinceEnable` wird fest auf 1 gesetzt (OreSpawnMain.java:6330); es gibt keinen Config-Key im manifest, und die Klasse liest ihn nicht.
- **Portierung 1.21.1:**
  - Basis `TamableAnimal`. Die 1.7.10-Namen sind SRG; MCP liefert sie nicht, die Bedeutung ist aus der Nutzung erschlossen:
    - `func_152115_b(String)` → `setOwnerUUID`
    - `func_152114_e(EntityLivingBase)` → `isOwnedBy`
    - `func_152113_b()` → `getOwnerUUID`
    - `setSitting` → `setOrderedToSit` + `setInSittingPose`
  - Attribute: `MAX_HEALTH` 500 (unter dem Deckel), `ARMOR` 16, `ATTACK_DAMAGE` 10, `MOVEMENT_SPEED` 0.32. `xpReward = 50`.
  - DW → drei `EntityDataAccessor<Integer>`; NBT in `addAdditionalSaveData` mit den Originalschlüsseln.
  - `interact` → `mobInteract(Player, InteractionHand)`. Das Feuerzeug-Verbrauchsverhalten 1:1 übernehmen: `shrink(1)` statt `hurtAndBreak`.
  - `noClip` → `noPhysics` im Flug. In `aiStep`/`customServerAiStep` die Goal-Selektoren bei Aktivität 2 überspringen, zum Beispiel über ein Flag in `Goal.canUse`, weil `super.updateAITasks()` im Original entfällt.
  - Schadenstypen: `DamageTypes.IN_WALL` und `DamageTypes.CACTUS` per `source.is(...)` prüfen.
  - Tag-Zähler: `Level#isDay()` (gegen die neoforge-sources.jar prüfen).
  - `EntityAIMoveIndoors` hat kein direktes Gegenstück (offen: Ersatz-Goal oder weglassen).
  - `EntityAITempt` → `TemptGoal` mit `Ingredient.of(Items.BEEF)`.
  - Die Kopf-Ausfahrwerte sind clientlokal zufällig und brauchen keine Synchronisation.
  - Chat-Texte als `Component.literal`, falls sie englisch bleiben.

---

### ThePrinceTeen - The Young Prince (`the_young_prince`)

- **Rolle:** Jugendstufe, `EntityTameable`, reitbar, fliegt auf Kommando. Wild greift er Monster an und rächt sich an Angreifern. Er ist nicht natürlich spawnbar und hat eine eigene Flugphysik mit direkter Reitersteuerung.
- **Werte:**
  - XP 300 (ThePrinceTeen.java:79).
  - Nahkampf **45** (×2 gegen `Kraken`), obwohl das Attribut 50 beträgt (ThePrinceTeen.java:115, 300-303).
  - Rückstoss horizontal 1.75, vertikal 0.1, verdoppelt bei getötetem Ziel oder Spieler (ThePrinceTeen.java:296-308).
  - Eigene Unverwundbarkeit 20 Ticks nach jedem Treffer (`hurt_timer`) (ThePrinceTeen.java:322-324, 358, 639-641).
  - Regeneration +2 HP mit Chance 1/250 pro Tick (ThePrinceTeen.java:415-417). Abweichung: 01-mobs.md nennt nach [FW] 5 HP; das ist der Adult-Wert.
  - Immun gegen `cactus`, `inFire`, `onFire`, `lava`, `inWall` (ThePrinceTeen.java:325-339).
  - Sprung +0.25 (ThePrinceTeen.java:230-233); `canBePushed` = false (ThePrinceTeen.java:269-271).
  - Reiter: `getMountedYOffset` 2.75, Sitzpunkt 0.65 vor der Mitte (ThePrinceTeen.java:273-275, 1129-1133).
  - Rüstung 18 (ThePrinceTeen.java:226-228). Abweichung: 01-mobs.md nennt 14.
  - Wachstum zum Adult bei `kill_count > 25 && day_count > 10`, **ohne** Fütterungsbedingung (ThePrinceTeen.java:383). Abweichung: 01-mobs.md vermutet „same pattern" wie beim Prince.
  - Die Methoden `getTrackingRange` 64, `getUpdateFrequency` 10 und `sendsVelocityUpdates` true (ThePrinceTeen.java:122-132) haben keine erkennbare Überschreibung; massgeblich ist wohl die Registrierung 64/1/false (manifest). Offen: ob FML 1.7.10 diese Methoden liest.
- **KI und Angriffe:**
  - Tasks:

    | Prio | Task | Parameter |
    |---|---|---|
    | 0 | `EntityAISwimming` | – |
    | 1 | `MyEntityAIFollowOwner` | 1.1, 12, 2 |
    | 2 | `EntityAITempt` | 1.25, beef |
    | 3 | `MyEntityAIWander` | 0.75 |
    | 4 | `EntityAIWatchClosest` | `EntityLiving`, 9 |
    | 5 | `EntityAILookIdle` | – |
    | 6 | `EntityAIMoveIndoors` | – |

    Quelle: (ThePrinceTeen.java:83-89). `targetTasks`: 1 `EntityAINearestAttackableTarget(EntityLiving, 0, true, false, IMob.mobSelector)` nur bei `PlayNicely == 0` zur Konstruktionszeit; 2 `EntityAIHurtByTarget(false)` (ThePrinceTeen.java:90-93).
  - Aktivität (DW 21): 0 = Boden, 1 = Flug; im Flug `noClip = true` (ThePrinceTeen.java:571-576).
  - `super.onLivingUpdate()` läuft **nur bei Aktivität 0** (ThePrinceTeen.java:850-852). Im Flug entfallen damit Vanilla-Bewegung und KI-Aufruf; die Bewegung macht `moveEntity` direkt. Folge: `updateAITasks` mit Wachstum und Tageszählung läuft nur am Boden (Aufrufkette Vanilla 1.7.10).
  - `updateAITick` wird mit Reiter übersprungen (ThePrinceTeen.java:469-474).
  - Moduswechsel (`always_do`, jeden Server-Tick):
    - Chance 1/250 `setAttackTarget(null)` (ThePrinceTeen.java:418-420).
    - Fliegender Besitzer → Flug (ThePrinceTeen.java:424-430).
    - Chance 1/50, ohne Ziel und ohne Reiter: 1/15 Flug, sonst Landung (ThePrinceTeen.java:431-438).
    - Zahm am Boden und Besitzer-Abstand² > 400 → Flug (ThePrinceTeen.java:657-662).
    - Jeder Schaden → aufstehen und fliegen (ThePrinceTeen.java:340-341).
  - Am Boden: Chance 1/10, sucht er ein Ziel, startet er (ThePrinceTeen.java:374-382).
  - Zielsuche: Box `expand(25, 20, 25)`, sortiert (ThePrinceTeen.java:519-536). Passend sind `EntityMob`, `Mothra`, `Kraken`, wilde `Leon`/`WaterDragon`/`GammaMetroid`; keine Royals (ThePrinceTeen.java:476-517). `PlayNicely != 0` → keine Suche.
  - **Flug ohne Reiter** (`fly_without_rider`):
    - Zahm und Besitzer-Abstand² > 400 → „toofar": Ziel verwerfen, neues Wegziel (ThePrinceTeen.java:690-704).
    - Vertikale Dämpfung 0.7/0.5/0.61 relativ zur Zielhöhe (ThePrinceTeen.java:708-716).
    - Angriff mit Chance 1/7 pro Tick, wenn `flyaway == 0` (ThePrinceTeen.java:723): zuerst das `getAttackTarget`, sonst eine eigene Suche.
    - Flucht wie beim Prince bei zahm und < 25 % Leben (ThePrinceTeen.java:733-739).
    - Nahkampf bei Abstand² < (8 + Breite/2)², danach `flyaway` 5..19 Ticks Abdrehen (ThePrinceTeen.java:746-750).
    - Fernkampf bei Abstand² < 400, nicht im Wasser, Feuer an, Chance 1/2 → `shoot_something` (ThePrinceTeen.java:751-753).
  - `shoot_something`: `rand(3)` wählt Feuerball (`setBig`), Blitz oder Eisball (`setIceMaker`); gefeuert wird nur innerhalb von 0.5 rad der Blickrichtung. Start 6.0 vor, 3.5 hoch, Streuung ±5/±3/±5, `random.bow` (ThePrinceTeen.java:1379-1497).
  - Wegziele wild ±16..25; mit Besitzer ±5..18 (fliegender Besitzer 0..5); y + `rand(9+2*of)-4`; Ziel muss Luft und per Raytrace sichtbar sein, bis zu 10 Versuche (ThePrinceTeen.java:764-798).
  - Hindernis-Auftrieb: vorausschauend `2+velocity*4` Blöcke; nicht-Luft-Blöcke unterhalb zählen je 0.05, dann `motionY` und `posY` += Summe·0.05 (ThePrinceTeen.java:799-813).
  - Tempo `sf` 0.5; fliegender Besitzer 1.75, bei Abstand² > 64 3.5. Horizontal `(signum - motion)*0.15*sf`, vertikal `*0.21*sf`, Drehung Yaw/4, `moveEntity` (ThePrinceTeen.java:814-834).
  - Flügelgeräusch alle > 20 Ticks im Flug (ThePrinceTeen.java:642-650).
  - Kopfwerte serverseitig 0..60 würfeln, per DW 22/23/25 synchronisieren (ThePrinceTeen.java:577-638).
  - **Mit Reiter** (Server, Aktivität 1):
    - Bewegung auf ±2 geklemmt (ThePrinceTeen.java:883-894).
    - Bodenprobe 1.25 unter sich: Block → `motionY += 0.03`, `posY += 0.1`; sonst Schwerkraft `-0.018` (ThePrinceTeen.java:896-904).
    - Hindernis-Auftrieb `3+velocity*7` Blöcke, Faktor 0.07; `motionY` höchstens 2.0 (ThePrinceTeen.java:905-922).
    - Yaw folgt dem Reiter-Yaw mit Glättung `clamp(|1.85-v|, 0.01, 0.9)`, Pitch = `2*v` (ThePrinceTeen.java:923-950).
    - Hochfliegen bei `OreSpawnMain.flyup_keystate != 0`: `+0.035 + v*0.046` (ThePrinceTeen.java:959-962).
    - Vorwärts (`moveForward > 0`): `deltav` 0.025; der +0.05-Zweig greift nicht, weil `max_speed` 0.95 ≤ 1. `deltasmooth` rampt um `deltav/10` pro Tick bis `deltav`, Tempo höchstens 0.95 (ThePrinceTeen.java:844, 974-1006).
    - Rückwärts: `max_speed` 0.35, `deltav` -0.02 (ThePrinceTeen.java:988-998).
    - Dämpfung horizontal ×0.985, vertikal ×0.94 (ThePrinceTeen.java:1101-1104).
    - Schiebt Entities in `expand(3.25, 4, 3.25)` (ThePrinceTeen.java:1105-1115).
  - **Reiter-Schüsse:**
    - Auslöser: seitliche Eingabe (`moveStrafing ≠ 0`), `fireballticker == 0`; reihum Feuerball → Eis → Blitz; Cooldown 10 Ticks, zählt nur im Flug herunter (ThePrinceTeen.java:878-880, 1024-1099).
    - Startpunkt 7.5 vor, `1.5 + headExt*0.04` hoch (ThePrinceTeen.java:1025-1026).
    - Feuerball bei Yaw -10°: nicht `setBig`, also Explosion 1; `setNotMe`; Beschleunigung 0.1 in Kopf- und Pitch-Richtung des Reiters; `random.fuse` (ThePrinceTeen.java:1031-1056).
    - Eisball bei Yaw +10°: `setThrowableHeading(...,1.4,5.0)`, Bewegung ×2, `fireworks.launch` 0.75 (ThePrinceTeen.java:1057-1083).
    - Blitz geradeaus: Werfer ist der **Reiter**, Bewegung ×3, `random.bow` 0.75 (ThePrinceTeen.java:1084-1098).
  - Automatische Angriffe mit Reiter (`fly_with_rider`): Chance 1/5 pro Tick. Nahkampf bei Abstand² < (8 + Breite/2)², Fernkampf bei 100 < Abstand² < 625 mit Feuer an (ThePrinceTeen.java:441-467).
- **Interaktion:** Abstand² < 25, wo nicht anders angegeben.
  - `Blocks.diamond_block` (auch wild): zähmen, voll heilen, kill/day = 1000 → wächst beim nächsten Boden-KI-Tick zum Adult (ThePrinceTeen.java:1155-1173).
  - Zahm, aber nicht Besitzer → `false` (ThePrinceTeen.java:1174-1177). Wild mit etwas anderem → `false` (ThePrinceTeen.java:1302).
  - Besitzer, leere Hand → aufsteigen, Aktivität 1, aufstehen (ThePrinceTeen.java:1178-1185).
  - `Items.beef` → volle Heilung. Die Heilung ist nicht auf den Server beschränkt; Partikel erscheinen nur clientseitig (ThePrinceTeen.java:1186-1202).
  - Sonstiges `ItemFood` → `getHealAmount*10` (ThePrinceTeen.java:1203-1220).
  - Eis/Feuerzeug → DW 24 aus/an, Chat „Fireballs extinguished." / „Fireballs lit!", Item verbraucht (ThePrinceTeen.java:1221-1256).
  - `Items.diamond` → Rückbildung zu „The Prince" mit Besitzer und `set_ok_to_grow()` (ThePrinceTeen.java:1257-1278).
  - `name_tag` → Name (ThePrinceTeen.java:1279-1289).
  - Jedes andere Item bei Abstand² < **16** → Sitzen umschalten, Aktivität 0 (ThePrinceTeen.java:1290-1300).
  - Schaden durch Spieler trifft auch den zahmen Teen. Nur die Vergeltung entfällt, weil `super.attackEntityFrom` vor der Prüfung läuft (ThePrinceTeen.java:357-362). Treffer von `ThePrinceTeen` und `Spyro` werden ignoriert (ThePrinceTeen.java:351-356).
- **Drops:** genau 1 `eggtheprince`, Position ±1 xz, y+1 (ThePrinceTeen.java:277-293); XP 300.
- **Spawnen:**
  - `getCanSpawnHere` = false (ThePrinceTeen.java:542-544). Er entsteht nur aus Prince-Wachstum, Adult-Rückbildung oder per Befehl; ein Ei gibt es nicht.
  - `canDespawn` = `!isNoDespawnRequired && riddenByEntity == null && !isTamed` (ThePrinceTeen.java:1357-1359): ein wilder Teen kann despawnen.
  - `Kraken` wählt einen Teen ohne Reiter als Ziel (Kraken.java:1164-1167).
- **Zustand:**

  | DW | Default | Bedeutung | NBT |
  |---|---|---|---|
  | 20 | 0 | attacking | `ThePrinceTeenAttacking` |
  | 21 | 0 | Aktivität 0 Boden / 1 Flug | `ThePrinceTeenActivity` |
  | 22 | 0 | head1ext | – |
  | 23 | 0 | head2ext | – |
  | 24 | 1 | Fernangriffe an/aus | `ThePrinceTeenFire` |
  | 25 | 0 | head3ext | – |

  - Quelle DW: (ThePrinceTeen.java:177-188). Weitere NBT: `SpyroKill`, `SpyroDay` (ThePrinceTeen.java:1361-1377).
  - Die Setter von DW 20-25 sind nur serverseitig wirksam (ThePrinceTeen.java:146-165, 1313-1340).
  - `RenderInfo renderdata` ist ein ungenutzter Render-Zwischenspeicher (ThePrinceTeen.java:190-200).
- **Sounds:**
  - Ambient `orespawn:roar`, nur nicht sitzend, im Flug und ohne Reiter (ThePrinceTeen.java:243-251).
  - Hurt `orespawn:alo_hurt`, Death `orespawn:alo_death`; Lautstärke 0.6, Tonhöhe 0.75 (ThePrinceTeen.java:253-267).
  - Flügel `orespawn:MothraWings` (manifest-ID `mothrawings`), 0.5 / 1.0 (ThePrinceTeen.java:646).
  - KI-Schüsse `random.bow`; Reiter-Schüsse `random.fuse`, `fireworks.launch`, `random.bow`.
- **Config:** `PlayNicely` (OreSpawnTWEAKS, 0, manifest) zur Konstruktionszeit und in `findSomethingToAttack` (ThePrinceTeen.java:90, 520). `flyup_keystate` ist kein Config-Wert, sondern ein statischer Server-Zustand, den das Paket `RiderControls` setzt (RiderControlMessageHandler.java:17; CommonProxyOreSpawn.java:25).
- **Portierung 1.21.1:**
  - **Virtuelles Leben:** echtes Original 1500 HP > 1024. Zum Beispiel `MAX_HEALTH` 1024 und eingehenden Schaden × 1024/1500 skalieren; Heilwerte (2 HP, Futter × 10, volle Heilung) im selben Massstab rechnen. `ARMOR` 18 passt.
  - Flugphysik:
    - `aiStep()` überschreiben und `super.aiStep()` nur bei Aktivität 0 aufrufen; sonst `move(MoverType.SELF, getDeltaMovement())` von Hand, wie im Original.
    - Achtung: in 1.21.1 hängt `serverAiStep` an `aiStep`, im Flug ruhen also Goals und Wachstum, wie im Original.
    - `noPhysics` im Flug.
  - Reiten:
    - `getControllingPassenger()` statt `riddenByEntity`; Sitzpunkt über `getPassengerAttachmentPoint` / `positionRider` (Offset 0.65 vorwärts, 2.75 hoch).
    - Eingaben `Player#zza`/`xxa` serverseitig; gegen die neoforge-sources.jar prüfen, ob `ServerboundPlayerInputPacket` sie in 1.21.1 setzt.
    - `flyup_keystate` **pro Spieler** über ein eigenes `CustomPacketPayload` (PayloadRegistrar) halten. Das Original teilt einen globalen Wert zwischen allen Reitern.
  - Eigene 20-Tick-Unverwundbarkeit zusätzlich zu `invulnerableTime` nachbauen.
  - Schadenstypen über `DamageTypes.CACTUS/IN_FIRE/ON_FIRE/LAVA/IN_WALL`.
  - Explosionen mit `Level.ExplosionInteraction.MOB` (beachtet `mobGriefing`).
  - Die Kopfwerte bleiben synchronisiert, weil die Reiter-Schusshöhe sie serverseitig liest.
  - `removeWhenFarAway` statt `canDespawn`.
  - Das Modell (71 Teile, 512×256) per `ModelPart` von Hand, ohne GeckoLib. Laut 06-models-design.md wäre GeckoLib ein Kandidat, das widerspricht der Leitplanke, ist aber nicht zwingend.

---

### ThePrinceAdult - The Young Adult Prince (`the_young_adult_prince`)

- **Rolle:** letzte Prince-Stufe, riesiger dreiköpfiger Drache, `EntityTameable`, reitbar, nicht natürlich spawnbar. Er ist die Brücke zur Verwandlung in `TheKing`.
- **Werte:**
  - XP 3000 (ThePrinceAdult.java:75).
  - Nahkampf **100** (×2 = 200 gegen `Kraken`); Rückstoss horizontal 2.0, vertikal 0.2, doppelt bei totem Ziel oder Spieler (ThePrinceAdult.java:290-306). Kills zählen hier nicht.
  - Regeneration +5 HP mit Chance 1/250 pro Tick (ThePrinceAdult.java:391-393).
  - Unverwundbarkeit 20 Ticks (ThePrinceAdult.java:311-313, 349).
  - Immun gegen `cactus`, `inFire`, `onFire`, `lava`; `inWall` wird ebenfalls ignoriert, lässt ihn aber aufstehen und losfliegen (ThePrinceAdult.java:314-330).
  - Sprung +0.35 (ThePrinceAdult.java:225-228).
  - Reiter: Offset 9.25 hoch, 4.65 vorwärts (ThePrinceAdult.java:268-270, 1106-1111).
  - Rüstung 20 (ThePrinceAdult.java:221-223); Tempo 0.36 (ThePrinceAdult.java:62).
  - King-Verwandlung nach `growcounter > 288000` Ticks (ThePrinceAdult.java:376), das sind 4 h Echtzeit bei 20 TPS.
  - Tracking: Klassenmethoden 64/10/true (ThePrinceAdult.java:117-127) gegenüber der Registrierung 128/1/false (manifest); wirksam vermutlich die Registrierung (offen, wie beim Teen).
- **KI und Angriffe:**
  - Tasks wie beim Teen, aber **ohne** `EntityAIMoveIndoors` und mit `EntityAIWatchClosest` Radius 20; `targetTasks` gleich (ThePrinceAdult.java:79-88).
  - Aktivität und Bewegung wie beim Teen (gleicher Code), mit diesen Abweichungen:

    | Grösse | Adult | Teen | Quelle |
    |---|---|---|---|
    | Zielsuche | `expand(32, 20, 32)` | 25/20/25 | (ThePrinceAdult.java:499) |
    | Angriffschance ohne Reiter | 1/6 pro Tick | 1/7 | (ThePrinceAdult.java:699) |
    | Nahkampfradius | (10 + Breite/2)² | (8 + Breite/2)² | (ThePrinceAdult.java:432, 722) |
    | Fernkampf ohne Reiter | Abstand² < 600, Chance 1/2 | < 400 | (ThePrinceAdult.java:727) |
    | Fernkampf mit Reiter | Abstand² < 625, **keine** Untergrenze | 100..625 | (ThePrinceAdult.java:435) |
    | Wegziele wild | ±20..34 | ±16..25 | (ThePrinceAdult.java:759-760) |
    | Wegziele mit Besitzer | ±8..23, fliegender Besitzer 0..11 | ±5..18 / 0..5 | (ThePrinceAdult.java:749-756) |
    | Abflug bei Besitzer-Abstand² | > 900 | > 400 | (ThePrinceAdult.java:633-638) |
    | „toofar" im Flug | > 400 | > 400 | (ThePrinceAdult.java:672) |
    | Flügelgeräusch | alle > 30 Ticks | > 20 | (ThePrinceAdult.java:618-626) |
    | Reiter `max_speed` | 1.05 | 0.95 | (ThePrinceAdult.java:820) |
    | Vorwärts-`deltav` | 0.035 + 0.07 = 0.105 (weil 1.05 > 1) | 0.025 | (ThePrinceAdult.java:952-955) |
    | Hochfliegen | `+0.045 + v*0.066` | `+0.035 + v*0.046` | (ThePrinceAdult.java:935-938) |
    | Schiebebox | `expand(6.25, 10, 6.25)` | 3.25/4/3.25 | (ThePrinceAdult.java:1083) |
    | Reiter-Schusspunkt | 14.5 vor, `9.5 - headExt*0.08` hoch | 7.5 / `1.5 + headExt*0.04` | (ThePrinceAdult.java:1001-1002, 1008, 1035, 1062) |
    | Reiter-Feuerball | `setBig` (Explosion 2) | Explosion 1 | (ThePrinceAdult.java:1012) |
    | Reiter-Cooldown | 8 Ticks | 10 | (ThePrinceAdult.java:1076) |

  - KI-Fernschuss wie beim Teen: 6.0 vor, 3.5 hoch, Feuerball `setBig`, Aimcone 0.5 rad (ThePrinceAdult.java:1348-1466).
  - **King-Verwandlung:**
    - Bedingungen je KI-Tick: Aktivität 0, kein Reiter, nicht Peaceful, zahm, `FullPowerKingEnable != 0` → `growcounter++` (ThePrinceAdult.java:374-375).
    - Über 288000 spawnt „The King" mit `setFree()` (setzt `TheKing.isEnd = 1`, TheKing.java:1049-1051). Der Besitzer wird **nicht** übertragen, der Adult entfernt sich (ThePrinceAdult.java:376-385).
- **Interaktion:** Abstand² < 36.
  - `Blocks.diamond_block`, auch wild: volle Heilung und `growcounter = 288000`, aber **kein Zähmen** (ThePrinceAdult.java:1132-1145). Bei zahmem Adult und aktivem Config-Schalter folgt die King-Verwandlung im übernächsten Boden-KI-Tick. Ein wilder Adult lässt sich nicht zähmen.
  - Zahm, nicht Besitzer → `false` (ThePrinceAdult.java:1147-1149).
  - Leere Hand → aufsteigen und fliegen (ThePrinceAdult.java:1150-1157).
  - `beef` → voll heilen (ThePrinceAdult.java:1158-1174).
  - `ItemFood` → `getHealAmount*10` (ThePrinceAdult.java:1175-1192).
  - Eis/Feuerzeug → DW 24 aus/an mit Chat (ThePrinceAdult.java:1193-1228).
  - `Items.diamond` → Rückbildung zum Teen mit Besitzer (ThePrinceAdult.java:1229-1249).
  - `name_tag` (ThePrinceAdult.java:1250-1260).
  - Jedes andere Item (Abstand² < 36) → Sitzen umschalten, Aktivität 0 (ThePrinceAdult.java:1261-1271).
  - Wild ohne Diamantblock → `false` (ThePrinceAdult.java:1273).
  - Vergeltung und zahmer Spielerschaden wie beim Teen; ignoriert Treffer von `ThePrinceAdult` und `Spyro` (ThePrinceAdult.java:342-358).
- **Drops:** genau 1 `eggtheprince` (ThePrinceAdult.java:272-288); XP 3000. Abweichung: 01-mobs.md nennt „Prince Egg" (stimmt).
- **Spawnen:**
  - `getCanSpawnHere` = false (ThePrinceAdult.java:518-520). Er entsteht nur aus Teen-Wachstum oder per Befehl.
  - `canDespawn` wie beim Teen: wild und ohne Reiter kann er despawnen (ThePrinceAdult.java:1328-1330).
  - `Kraken` wählt ihn ohne Reiter als Ziel (Kraken.java:1168-1171).
- **Zustand:**
  - DW-Belegung identisch zum Teen (20 attacking, 21 Aktivität, 22/23/25 Köpfe, 24 Feuer; ThePrinceAdult.java:174-183).
  - NBT: `ThePrinceAdultAttacking`, `ThePrinceAdultActivity`, `ThePrinceAdultFire`, `ThePrinceAdultGrow` (`growcounter`) (ThePrinceAdult.java:1332-1346).
  - Kill- und Tageszähler gibt es nicht.
- **Sounds:**
  - Ambient `orespawn:king_living`, nur nicht sitzend, im Flug, ohne Reiter (ThePrinceAdult.java:238-246).
  - Hurt `orespawn:king_hit`, Death `orespawn:trex_death`; Lautstärke 0.85, Tonhöhe 1.1 (ThePrinceAdult.java:248-262).
  - Flügel `orespawn:MothraWings` 0.5/1.0 (ThePrinceAdult.java:622).
  - Schüsse wie beim Teen.
- **Config:** `FullPowerKingEnable` (OreSpawnTWEAKS, Default 0, manifest; gelesen in OreSpawnMain.java:1159) aktiviert die King-Verwandlung. Dazu `PlayNicely` (OreSpawnTWEAKS, 0, manifest).
- **Portierung 1.21.1:**
  - **Virtuelles Leben:** Original 3000 HP, das Dreifache des Deckels 1024. Schaden und Heilung (5 HP, Futter, volle Heilung) im Verhältnis skalieren. `ARMOR` 20 und `ATTACK_DAMAGE` 100 passen.
  - Hitbox 6.25×10.25 und Tracking 128 Blöcke → `EntityType.Builder.sized(6.25f, 10.25f)`. Die Einheit von `clientTrackingRange` in 1.21.1 in den Quellen nachsehen.
  - Der `growcounter` zählt 288000 Ticks nur, während die Entity geladen ist und am Boden steht. In einem ModConfigSpec-Boolean `FullPowerKingEnable` abbilden.
  - Die King-Verwandlung hängt an `TheKing.setFree()` (anderer Stapel) und muss die Weltgrenzen einer Entity dieser Grösse vertragen.
  - Alle Flug- und Reiter-Punkte vom Teen gelten 1:1, samt dem globalen `flyup_keystate`, der pro Spieler werden muss.
  - Textur `thekingtexture.png` 2048×2048 teilt er mit `TheKing` (manifest).
  - Das Modell hat 119 Teile, `render()` mit 4785 Bytes plus `moveLeftHead`/`moveCenterHead`/`moveRightHead` mit je 1927 Bytes (anim_summary.txt:96). Von Hand in `ModelPart` gross, aber machbar. Leitplanke „kein GeckoLib" bleibt einhaltbar; 06-models-design.md empfiehlt GeckoLib.

---

### ThePrincess - The Princess (`the_princess`)

- **Rolle:** Kleinkind-Prinzessin (keine weiteren Stufen), `EntityTameable`, zähmt sich selbst, fliegt im Aktivitätsmodus 2. Sie hat denselben Kern wie `ThePrince` und zusätzlich eine Machtleiste: im Kampf `PurplePower`-Kugeln, ausser Kampf Landschaftsverschönerung und Tier-Spawns.
- **Werte:**
  - XP 50 (ThePrincess.java:72).
  - Nahkampf **9**, obwohl das Attribut 10 beträgt (ThePrincess.java:80, 360-362). Abweichung: 01-mobs.md nennt Attack 10.
  - Regeneration +1 HP mit Chance 1/200 (ThePrincess.java:510-512).
  - Rüstung 14 (ThePrincess.java:317-319).
  - Machtleiste `attack_level`:
    - +1 pro KI-Tick, im Angriff zusätzlich +4 (ThePrincess.java:523-526); 0 bei ausgeschaltetem Feuer (ThePrincess.java:527-529).
    - Auslösung über 500, danach zurück auf 1 (ThePrincess.java:530, 632). Das ergibt etwa 500 Ticks ausser Kampf und 100 Ticks im Kampf.
    - Synchronisation nach DW 23 alle 10 Ticks (ThePrincess.java:506-509).
  - Tempo effektiv 0.32 (ThePrincess.java:59, 469).
  - Wachstumsfelder (`ok_to_grow`, `kill_count`, `fed_count`, `day_count`) werden gespeichert und gezählt, aber nie ausgewertet: es gibt keine Wachstumsprüfung (ThePrincess.java:493-684). `fed_count` erhöht Füttern hier nicht (ThePrincess.java:202-219).
- **KI und Angriffe:**
  - Tasks, Aktivitätsmodell, Flug, Selbstzähmung, Flucht, Nah- und Fernkampf sowie Abschussgeometrie sind identisch zu `ThePrince` (ThePrincess.java:64-70, 634-668, 686-839, 864-941). Die Werte stehen dort.
  - Ziele: `EntityMob`, `Mothra`, `Dragonfly`, `EntityMosquito`; **nicht** `EntityButterfly` und `Cockateil`, die sie selbst spawnt (ThePrincess.java:841-843). Suchbox 12/6/12 (ThePrincess.java:849).
  - **Macht-Auslösung, im Angriff** (DW 22 ≠ 0): 3× `PurplePower` 1.5 vor, 1.0 hoch, Anfangsbewegung = eigene Bewegung ×3, `setPurpleType(1 + rand(3))` (ThePrincess.java:531-541).
    - Typ 1..3 beim Treffer: Zielleben ×15/16, dann 5 Schaden. Typ 1 dazu Brand 10 s, Typ 2 Gift 50 Ticks, Typ 3 Schwäche 50 Ticks (PurplePower.java:288-298).
    - Royals werden nicht angegriffen (PurplePower.java:255).
  - **Macht-Auslösung, ausser Kampf:**
    - Nur bei `mobGriefing`: 5 Spalten, xz-Versatz `rand(5)-rand(5)` (-4..4), von y-5 aufwärts bis zum ersten Treffer (ThePrincess.java:543-616):

      | Gefundener Block | Bedingung darüber | Wirkung |
      |---|---|---|
      | `grass` | Luft | Blume darüber, `rand(8)`: `red_flower`, `yellow_flower`, `MyFlowerBlueBlock`, `MyFlowerPinkBlock`, `CrystalFlowerRedBlock`, `CrystalFlowerGreenBlock`, `CrystalFlowerBlueBlock`, `CrystalFlowerYellowBlock` |
      | `grass` | nicht Luft | Abbruch |
      | `dirt` | Luft | wird `grass` |
      | `stone` | Luft | `dirt` **darüber** gesetzt |
      | `sand` | Luft | 50 % `cactus` darüber, sonst Sand → `dirt` |
      | `lava` | Luft | → `water` |
      | `flowing_lava` | Luft | → `flowing_water` |
      | Luft bei j > 0 | – | Abbruch |

    - **Immer**, ohne `mobGriefing`: 2 Versuche, Luftblock bei ±3 xz und y+1..4 → 50 % „Butterfly" (`EntityButterfly`), sonst „Bird" (`Cockateil`) (ThePrincess.java:617-630; Registrierungsnamen calls_OreSpawnMain.txt:5412, 5720).
  - Client: bei `getPower() > 400` mit Chance 1/6 zwei `fireworksSpark`-Partikel am Körper (ThePrincess.java:458-465). Die Leiste kündigt die Auslösung also ab 400 an.
- **Interaktion:** wie bei `ThePrince` (Diamantblock zähmt auch wild; Futter heilt × 10; Eis/Feuerzeug; Namensschild; Sitzen), mit drei Unterschieden:
  - Chat „Princess fireballs extinguished." / „Princess fireballs lit!" (ThePrincess.java:226, 244).
  - **Kein** `Items.diamond`-Zweig, also kein Wachstum (ThePrincess.java:175-278).
  - Füttern zählt `fed_count` nicht.

  Selbstzähmung durch einen Spieler im Umkreis von 10 Blöcken (ThePrincess.java:513-522). Nicht reitbar, keine Nachkommen (ThePrincess.java:356-358).
- **Drops:** 1-4 `Items.beef` (ThePrincess.java:325-332); XP 50.
- **Spawnen:**
  - `getCanSpawnHere` = true, aber keine natürlichen Spawns (manifest). `canDespawn` = false (ThePrincess.java:291-293, 352-354).
  - Quellen:
    - `TheQueen` spawnt sie beim Tod bei y+10 (TheQueen.java:181),
    - Ei `eggtheprincess` (Index 371, ItemSpawnEgg.java:471-473) in der Truhe des Queen-Dungeons (`fill_chestsQ`, reward 6; GenericDungeon.java:6987-6990),
    - `EasterBunny`-Ei bei `rand(115)==107` (EasterBunny.java:530-532).
- **Zustand:**

  | DW | Default | Bedeutung | NBT |
  |---|---|---|---|
  | 20 | 1 | Fernangriffe an/aus | `SpyroFire` |
  | 21 | 1 | Aktivität 1 Boden / 2 Flug | `SpyroActivity` |
  | 22 | 0 | attacking; Renderer wechselt auf `theprincesstexture2.png` | – |
  | 23 | 1 | Machtleiste (`attack_level`, alle 10 Ticks) | – (nicht gespeichert) |

  - Quelle DW: (ThePrincess.java:86-89). Weitere NBT: `SpyroGrow`, `SpyroKill`, `SpyroFed`, `SpyroDay` (ThePrincess.java:103-122).
  - Texturwechsel: (RenderThePrincess.java:43-49). Kopfwerte clientlokal wie beim Prince (ThePrincess.java:401-457).
- **Sounds:** wie beim Prince (`orespawn:roar` nur im Angriff, `orespawn:duck_hurt`, `orespawn:cryo_death`, Lautstärke 0.6), aber Tonhöhe `(r-r)*0.2+1.5` (ThePrincess.java:295-315, 334-336). Schüsse `random.bow`. Neue Entities spielen beim Spawn `playLivingSound` (ThePrincess.java:949).
- **Config:** `PlayNicely` (OreSpawnTWEAKS, 0, manifest) (ThePrincess.java:846). `ThePrincessEnable` ist fest 1 (OreSpawnMain.java:6331), wird hier nicht gelesen.
- **Portierung 1.21.1:**
  - `MAX_HEALTH` 400 passt, `ARMOR` 14; Nahkampf 9 in `doHurtTarget` fest verdrahten, nicht aus dem Attribut.
  - Blockumwandlung: `Blocks.GRASS_BLOCK`, `DIRT`, `STONE`, `SAND`, `CACTUS`, `POPPY` (1.7.10 `red_flower` Meta 0), `DANDELION`. Lava/Wasser über Fluid-States (Quelle vs. fliessend unterscheiden). Den `mobGriefing`-Check mit `level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)` nur auf den Blockteil legen, wie im Original.
  - Die OreSpawn-Blumenblöcke kommen aus einem anderen Stapel.
  - Tier-Spawns über `EntityType.create` + `moveTo` + `addFreshEntity`.
  - `PurplePower` ist eine eigene Entity (anderer Stapel). Die Typ-Effekte → `MobEffects.POISON` / `WEAKNESS`, `igniteForSeconds(10)`.
  - Partikel `ParticleTypes.FIREWORK` in `tick()` hinter `level().isClientSide()`, ohne Client-Klassen, damit es serverfest bleibt.
  - Den Texturwechsel im Renderer unter `com.swbr.orespawn.client` über den synchronisierten Accessor `attacking` lösen.
