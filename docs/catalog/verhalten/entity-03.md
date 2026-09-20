# Verhalten: entity-03

Dieser Stapel umfasst die beiden großen „Weltbosse" von OreSpawn und ein Hilfsobjekt. **Mobzilla**
(`Godzilla`) ist ein 25 Blöcke hoher Bodenboss. Er zerdrückt fortlaufend Blöcke um sich und hinter
sich, schießt Feuerballsalven, ruft Blitze und springt auf Ziele. Durch eine Schadensobergrenze,
eine eigene Rüstungslogik und einen Trefferzeitgeber ist er weit zäher, als seine 4000 HP vermuten
lassen. **MobzillaHead** (`GodzillaHead`) ist ein unsichtbarer, kollidierbarer Stellvertreter vor
Mobzillas Kopf: Er leitet Treffer an den Körper weiter, damit man den 25 Blöcke hohen Boss überhaupt
treffen kann. **The Kraken** (`Kraken`) ist ein fliegender Boss. Er erzeugt Gewitter, packt Spieler
und Tiere, trägt sie auf Y 190–200 und lässt sie fallen; bei wenig Leben ruft er einmalig zehn
weitere Kraken. Für den Port wiegen zwei Punkte am schwersten. Erstens rechnet 1.7.10 Rüstung als
`damage * (25 - armor) / 25`: Mobzilla lässt damit nur 16 % durch und wird bei Rüstung 25 gegen
blockbaren Schaden immun. Zweitens liegt Mobzillas HP über der 1024-Grenze von 1.21.1.

Grundlage für Vanilla-1.7.10-Aussagen: Bytecode aus `reference/jar/mcp/client-1.7.10.jar`, gemappt
über `joined.srg`.
- `EntityLivingBase.applyArmorCalculations` (`sv.b(Lro;F)F`): wenn `!source.isUnblockable()`, dann
  `amount * (25 - getTotalArmorValue()) / 25`.
- `EntityMob.onUpdate` (`yg.h()V`): serverseitig `setDead()` auf Peaceful.

Aussagen mit dem Zusatz „(1.7.10-Vanilla, abgeleitet)" sind *nicht* am Bytecode geprüft.

---

### Godzilla - Mobzilla (`mobzilla`)

- **Rolle:** feindlicher Bodenboss, Basis `EntityMob` (Godzilla.java:20). Kein Zähmen, kein Reiten,
  kein Multipart-Objekt im Vanilla-Sinn. Den Kopf-Hitbox stellt ein eigenes Entity `GodzillaHead`
  bereit, das Godzilla selbst spawnt (Godzilla.java:347-351). Ist `PlayNicely != 0`, ist Mobzilla
  vollständig friedlich und zerstört nichts (siehe KI).

- **Werte** (was das Manifest nicht hat):

  | Wert | Zahl | Herkunft |
  |---|---|---|
  | XP (`experienceValue`) | 10000 | (Godzilla.java:54) |
  | `fireResistance` / `isImmuneToFire` | 10000 / true | (Godzilla.java:63-64) |
  | `renderDistanceWeight` | 12.0 | (Godzilla.java:65) |
  | Bewegungstempo | 0.75, wird **jeden Tick** neu gesetzt | (Godzilla.java:37, 133) |
  | Regeneration | je Tick Chance 1/35: +5 HP, solange HP < Max | (Godzilla.java:402-404) |
  | Schadensobergrenze je Treffer | 750, **vor** Rüstung | (Godzilla.java:697-698) |
  | Trefferzeitgeber | nach jedem Nicht-Kaktus-Treffer 20 Ticks taub; Treffer in der Zeit geben `false` zurück | (Godzilla.java:694-696, 712) |
  | Schaden von „großen Unbekannten" | Angreifer mit `height*width > 30`, nicht Royalty/Godzilla/Head/PitchBlack/Kraken: Schaden /10, `hurt_timer = 50`, das in derselben Methode sofort auf 20 überschrieben wird. Die 50 wirken nur, wenn der Treffer Kaktusschaden war | (Godzilla.java:704-708, 710-712) |
  | Kaktusschaden | wird komplett ignoriert | (Godzilla.java:710) |
  | Rüstung | `Mobzilla_defense` (21, manifest); **25**, sobald `large_unknown_detected != 0` | (Godzilla.java:119-124) |
  | Wirksamer Schaden | 1.7.10-Formel: 21 → 4/25 = 16 % kommen durch. 750 × 0.16 = **120 je Treffer**. Rüstung 25 → **0** blockbarer Schaden | (sv.b-Bytecode, s. oben) |
  | Fallschaden / Blitz | keiner (`fall`, `updateFallState`, `onStruckByLightning` leer) | (Godzilla.java:144-148, 723-724) |
  | `stream_count` (Feuerballsalven) | 8 je Fenster; zurückgesetzt, wenn `ticker % 100 == 0` und wenn kein Ziel da ist | (Godzilla.java:43, 248-250, 399) |
  | `ticker` | läuft bis 30000, dann 0 | (Godzilla.java:244-247) |
  | `jump_timer` nach Ansprung | 30 Ticks | (Godzilla.java:361) |
  | Zielverlust | Chance je Tick 1/200: `setAttackTarget(null)` | (Godzilla.java:258-260) |
  | Hitbox bei PlayNicely | 2.475 × 6.25 (normal 9.9 × 25, manifest) | (Godzilla.java:47-52) |

  `large_unknown_detected` (Godzilla.java:32) ist ein Merker ohne NBT. Er wird 1, wenn Mobzilla ein
  großes Fremdwesen trifft oder von einem getroffen wird (Godzilla.java:660, 707). Danach gilt bis
  zum Entladen: Rüstung 25, KI-Takt 1/4 statt 1/5, Sprungchance 1/15 statt 1/20,
  Nahkampfwahrscheinlichkeit 2/3 statt 1/2.

- **KI und Angriffe:**

  | Prio | Task | Herkunft |
  |---|---|---|
  | 0 | `EntityAISwimming` | (Godzilla.java:55) |
  | 1 | `EntityAIMoveThroughVillage(1.0, false)` | (Godzilla.java:56) |
  | 2 | `MyEntityAIWanderALot(xz 15, speed 1.0)`: Chance 1/30 je Prüfung, Ziel über `RandomPositionGenerator.findRandomTarget(15, 7)`, gesperrt solange `busy != 0` | (Godzilla.java:57-58; MyEntityAIWanderALot.java:27-49) |
  | 3 | `EntityAIWatchClosest(EntityLiving, 50.0)` | (Godzilla.java:59) |
  | 4 | `EntityAILookIdle` | (Godzilla.java:60) |
  | Ziel 1 | `EntityAIHurtByTarget(false)` | (Godzilla.java:61) |

  Pfadsuche meidet Wasser (Godzilla.java:53). In der Luft wird der Pfad jeden Tick gelöscht
  (Godzilla.java:135-137). Eigenes `jump()`: `motionY += 0.45`, `posY += 0.5`, Vorwärtsschub
  0.2–0.65 in Kopfrichtung, Pfad gelöscht (Godzilla.java:177-197).

  `updateAITasks` läuft nur serverseitig, jeden Tick nach `super.updateAITasks()`
  (Godzilla.java:233-405):

  1. **Landestoß**, nur bei `PlayNicely == 0` (Godzilla.java:261-278). Bei `motionY < -0.95` wird
     `jumped = 1` gesetzt, bei `< -1.5` `jumped = 2`. Beim Aufsetzen (`motionY > -0.1`) laufen drei
     `doJumpDamage`-Ringe, jeweils mit Faktor `df` = 1.0 bzw. 1.5:

     | Radius (xz, y ±10) | Schaden | Bei Standardwerten |
     |---|---|---|
     | 10 | `attack * df` | 175 |
     | 15 | `attack / 2 * df`, Ganzzahldivision | 87 |
     | 25 | `attack / 4 * df` | 43 |

     `doJumpDamage` (Godzilla.java:471-517) teilt den Schaden je Wesen: die Hälfte als
     Explosionsquelle, die Hälfte als `DamageSource.fall`. Dazu Sound `random.explode` (0.85). Kein
     Rückstoß beim Landen. Ausgenommen sind: selbst, Godzilla, GodzillaHead, Ghost, GhostSkelly.
     Die Ringe überlappen. Ein Wesen im Zentrum wird dreimal getroffen, das Vanilla-Trefferfenster
     schluckt aber gleich hohe oder kleinere Folgetreffer im selben Tick (1.7.10-Vanilla,
     abgeleitet).
  2. **Körperzone zerdrücken**, nur bei `PlayNicely == 0` (Godzilla.java:279-304). Radius xz ±12,
     ±16 solange `getAttacking() != 0`. Je Tick eine Schicht `y = posY + (-3 + ticker % 30)`, das
     durchläuft die Offsets −3 bis +26. Zerdrückbare Blöcke werden zu Luft; mit Chance 1/15 fällt
     das Block-Item an einer zufälligen Stelle ±9 xz, y+4 bis +13 heraus (Godzilla.java:599-607).
     `grass` und `farmland` werden bei `mobGriefing` zu `dirt`.
  3. **Schwanzzone**, nur bei `PlayNicely == 0` (Godzilla.java:305-331). Mittelpunkt
     `posX + 16*sin(headYaw)`, `posZ - 16*cos(headYaw)`. Das liegt **hinter** Mobzilla: Kopf
     (GodzillaHead.java:146-147) und Kanone (Godzilla.java:627-628) nutzen das umgekehrte
     Vorzeichen. Schicht `-3 + ticker % 12`, also −3 bis +8, Drops auf y+4 bis +9
     (Godzilla.java:609-617). Bei Schicht 0, also alle 12 Ticks: `doJumpDamage` mit Radius 15,
     Schaden `attack / 2` = 87, **mit** Rückstoß 3.5 horizontal und 0.75 nach oben, vom
     Mobzilla-Zentrum weg (Godzilla.java:329-331, 508-514).
  4. **`isCrushable`** (Godzilla.java:620): verlangt `mobGriefing` und schließt diese Blöcke aus:
     `grass, dirt, stone, farmland, water, flowing_water, lava, flowing_lava, bedrock, obsidian,
     sand, gravel, iron_block, diamond_block, emerald_block, gold_block, netherrack, end_stone,
     MyBlockAmethystBlock, MyBlockRubyBlock, MyBlockUraniumBlock, MyBlockTitaniumBlock,
     CrystalStone, CrystalGrass`. Alles andere wird zerstört, auch Stufen unter den Füßen (Offsets
     −3 bis −1).
  5. **Ziel- und Angriffszyklus**, je Tick mit Chance `1/(5 - large_unknown_detected)`
     (Godzilla.java:332-401):
     - Das Ziel ist `getAttackTarget()`. Es wird verworfen bei `PlayNicely != 0`, wenn es tot ist
       oder wenn es Godzilla bzw. GodzillaHead ist.
     - Ohne Ziel läuft `findSomethingToAttack()`. Liegt dabei kein GodzillaHead im Suchbereich
       (`head_found == 0`), wird `"MobzillaHead"` bei `posY + 20` gespawnt (Godzilla.java:347-351).
     - `findSomethingToAttack` (Godzilla.java:519-552) sucht in `boundingBox.expand(64, 40, 64)`,
       sortiert mit `GenericTargetSorter` (Abstand² geteilt durch `height*width`, falls > 1;
       Creeper zusätzlich /2; GenericTargetSorter.java:15-33). Ein **sichtbarer `EntityVillager`
       hat Vorrang** vor allem anderen (Godzilla.java:543-546). Sonst gewinnt das erste passende
       Ziel.
     - `isSuitableTarget` (Godzilla.java:417-465) lehnt ab: `MyUtils.isIgnoreable` (RockBase,
       EntityAnt, EntityButterfly, EntityMosquito, Dragonfly, Firefly, Cricket, Cockateil, Termite,
       Ghost, GhostSkelly, Elevator; MyUtils.java:17-18), Unsichtbares, Godzilla, GodzillaHead,
       Creeper, Zombie, Spider, Skeleton, Ghost, GhostSkelly und Spieler im Kreativmodus. Andere
       OreSpawn-Mobs und Tiere sind gültige Ziele.
     - **Mit Ziel:** `wander.setBusy(1)`, `faceEntity(e, 10, 10)`, danach wird **eine** der
       folgenden Aktionen gewählt:

     | Reihenfolge | Bedingung | Aktion | Herkunft |
     |---|---|---|---|
     | a | Chance 1/65 **und** `MygetDistanceSq > 300` | **Blitzangriff:** 100 Mob-Schaden, Ziel brennt 5 s, Explosion Stärke 3.0 am Ziel (zerstört nach `mobGriefing`), Blitz am Ziel (y+1) **und** an Mobzilla (y+15), 20 × Rauch/Funken, `random.explode` 0.5 | (Godzilla.java:356-358, 726-744) |
     | b | Chance `1/(20 - 5*large)` und `jump_timer == 0` | **Ansprung:** `motionY += 1.25`, `posY += 1.55`, Blick zum Ziel, horizontal `Abstand * 0.05`; `jump_timer = 30`. Der Landestoß aus Punkt 1 folgt | (Godzilla.java:359-362, 199-212) |
     | c | `MygetDistanceSq < 300 + (e.width/2)²` | `setAttacking(1)`, Pfad zum Ziel mit 1.0; Nahkampf, wenn `rand(4-large)==0 \|\| rand(3-large)==1` (→ 1/2 bzw. 2/3) | (Godzilla.java:363-369) |
     | d | sonst | Pfad zum Ziel; bei horizontalem Abstand² > 625, `stream_count > 0` und Kopfrichtung innerhalb 0.5 rad zum Ziel: `firecanon`, sonst `setAttacking(0)` | (Godzilla.java:370-394) |

     - `MygetDistanceSq` (Godzilla.java:220-231): Liegt das Ziel 0 bis 20 Blöcke höher, zählt die
       Höhe als 0; über 20 Blöcke zählt `dy - 10`. Tiefer liegende Ziele zählen voll.
     - **Ohne Ziel:** `setAttacking(0)`, `wander.setBusy(0)`, `stream_count = 8`
       (Godzilla.java:396-400).
  6. **`firecanon`** (Godzilla.java:623-651): Startpunkt 22 Blöcke vor dem Körper (`rotationYaw`),
     y+19. Er schießt einen großen `BetterFireball` (`setBig`, Explosionsstärke 2; BetterFireball.java:77-79)
     mit Sound `random.fuse`. Dazu kommen 5 Streufeuerbälle mit Versatz ±5 / ±3 / ±5, jeder mit
     Chance 1/2 `setSmall`, je Sound `random.bow`. Danach `stream_count--`, also höchstens 8 Salven
     je 100-Tick-Fenster.

     `BetterFireball` beim Aufprall:
     - 10 Schaden (klein: 5), Ziel brennt 5 s (BetterFireball.java:236-243).
     - Wenn nicht klein: Explosion mit der eingestellten Stärke (Standard 1, groß 2), `flaming =
       true`, `mobGriefing` (BetterFireball.java:280).
     - Trifft er einen Block, setzt er Feuer (BetterFireball.java:275-277).
     - Er **halbiert die HP** jedes `EntityLiving` mit `width*height > 30` außer Royalty, Godzilla,
       Head, PitchBlack und Kraken (BetterFireball.java:232-234).
     - Er fliegt durch GodzillaHead, Royalty und den Schützen (BetterFireball.java:139-154).
     - Lebensdauer 600 Ticks (BetterFireball.java:103-106).
  7. **`attackEntityAsMob`** (Godzilla.java:653-688):
     - Ziel `height*width > 30` und keine Ausnahme: `setHealth(health / 2)`, danach
       `causeMobDamage` mit `attack * 10` = **1750**; `large_unknown_detected = 1`.
     - `EntityDragon`: Explosionsschaden `attack / 2` = 87.5, mit Chance 1/6 auf `dragonPartHead`,
       sonst auf `dragonPartBody`.
     - Danach `super.attackEntityAsMob` mit Attribut 175 (manifest). Bei Erfolg Rückstoß 3.2
       horizontal und 0.3 hoch, doppelt so hoch bei Spielern oder toten Zielen.
  8. **Vergeltung** (Godzilla.java:713-718): Jeder lebende Angreifer außer Godzilla und Head wird
     `setAttackTarget` + `setTarget` und angesteuert (Tempo 1.2).

  **PlayNicely != 0:** kein Landestoß, kein Zerdrücken, keine Schwanzzone; Ziel immer `null`, und
  `findSomethingToAttack` setzt `head_found = 1`, damit spawnt **kein Kopf**
  (Godzilla.java:261, 284, 308, 329, 334-336, 520-523). Die Regeneration bleibt.

- **Interaktion:** kein `interact`-Override. Nicht zähmbar, nicht reitbar, keine Zucht. Bossleiste
  „Mobzilla" über `GirlfriendOverlayGui` mit `getHealth()/getMaxHealth()`
  (GirlfriendOverlayGui.java:285-289). PitchBlack greift Godzilla und Head mit mindestens 10 Blöcken
  Reichweite an (PitchBlack.java:351-356).

- **Drops** (`dropFewItems`, Godzilla.java:746-1813; `getDropItem` → `null`, Godzilla.java:173-175).
  Jeder Drop ist ein eigenes `EntityItem` an einer zufälligen Stelle ±9 xz, y+4 bis +13
  (Godzilla.java:599-607, Zufall `OreSpawnMain.OreSpawnRand` für xz):

  | Item | Anzahl | Herkunft |
  |---|---|---|
  | `item_frame` | 1 | (Godzilla.java:748) |
  | `MyGodzillaScale` (`godzillascale`) | 50–79 | (Godzilla.java:749-751) |
  | `beef` | 100–259 | (Godzilla.java:752-754) |
  | `bone` | 50–109 | (Godzilla.java:755-757) |
  | Wurftabelle | 25–39 Würfe auf `nextInt(80)` | (Godzilla.java:758-759) |

  Wurftabelle. Die Fälle 72 und 76–79 sind leer, also bleibt ein Wurf mit Chance 5/80 ohne Drop.
  Verzauberungsschemata stehen unter der Tabelle.

  | Fall | Item | Fall | Item |
  |---|---|---|---|
  | 0 | `MyUltimateSword` | 38 | `golden_boots` F |
  | 1 | `diamond` | 39 | `golden_apple` |
  | 2 | `diamond_block` | 40 | `gold_block` |
  | 3 | `diamond_sword` S | 41 | `golden_apple` Meta 1 (verzaubert), Position ±2 xz, y+1 (Godzilla.java:1267-1275) |
  | 4 | `diamond_shovel` T | 42 | `MyExperienceSword` S |
  | 5 | `diamond_pickaxe` P | 43 | `ExperienceHelmet` H |
  | 6 | `diamond_axe` T | 44 | `ExperienceBody` B |
  | 7 | `diamond_hoe` T | 45 | `ExperienceLegs` B |
  | 8 | `diamond_helmet` H* | 46 | `ExperienceBoots` F |
  | 9 | `diamond_chestplate` B | 47 | `MyAmethystSword` S |
  | 10 | `diamond_leggings` B | 48 | `MyAmethystShovel` T |
  | 11 | `diamond_boots` F | 49 | `MyAmethystPickaxe` P |
  | 12 | `MyUltimateBow` | 50 | `MyAmethystAxe` T |
  | 13 | `MyUltimateAxe` | 51 | `MyAmethystHoe` T |
  | 14 | `iron_ingot` | 52 | `MyBlockAmethystBlock` |
  | 15 | `MyUltimatePickaxe` | 53 | `AmethystHelmet` H |
  | 16 | `iron_sword` S | 54 | `AmethystBody` B |
  | 17 | `iron_shovel` T | 55 | `AmethystLegs` B |
  | 18 | `iron_pickaxe` P | 56 | `AmethystBoots` F |
  | 19 | `iron_axe` T | 57 | `RubyHelmet` H |
  | 20 | `iron_hoe` T | 58 | `RubyBody` B |
  | 21 | `iron_helmet` H | 59 | `RubyLegs` B |
  | 22 | `iron_chestplate` B | 60 | `RubyBoots` F |
  | 23 | `iron_leggings` B | 61 | `MyRubySword` S |
  | 24 | `iron_boots` F | 62 | `MyRubyShovel` T |
  | 25 | `MyUltimateShovel` (unverzaubert) | 63 | `MyRubyPickaxe` P |
  | 26 | `iron_block` | 64 | `MyRubyAxe` T |
  | 27 | `gold_nugget` | 65 | `MyRubyHoe` T |
  | 28 | `gold_ingot` | 66 | `MyBlockRubyBlock` |
  | 29 | `golden_carrot` | 67 | `UltimateHelmet` H |
  | 30 | `golden_sword` S | 68 | `UltimateBody` B |
  | 31 | `golden_shovel` T | 69 | `UltimateLegs` B |
  | 32 | `golden_pickaxe` P | 70 | `UltimateBoots` F |
  | 33 | `golden_axe` T | 71 | `MyUltimateShovel` T |
  | 34 | `golden_hoe` T | 72 | — |
  | 35 | `golden_helmet` H | 73 | `MyUltimatePickaxe` P |
  | 36 | `golden_chestplate` B | 74 | `MyUltimateAxe` T |
  | 37 | `golden_leggings` B | 75 | `MyUltimateHoe` T |

  Verzauberungsschemata. Jede Zeile ist ein eigener Wurf; „1/6: 1–5" heißt Chance 1/6, Stufe 1 + `nextInt(5)`:
  - **S** (Schwert, Godzilla.java:774-797): `sharpness` 1/6: 1–5, `baneOfArthropods` 1/6: 1–5,
    `knockback` 1/6: 1–5, `looting` 1/6: 1–5, `unbreaking` 1/2: 2–5, `fireAspect` 1/6: 1–5,
    **zweiter** `sharpness`-Wurf 1/6: 1–5.
  - **T** (Werkzeug, Godzilla.java:800-808): `unbreaking` 1/2: 2–5, `efficiency` 1/6: 1–5.
  - **P** (Spitzhacke, Godzilla.java:811-822): wie T, plus `fortune` 1/6: 1–5.
  - **H** (Helm, Godzilla.java:1013-1036): `protection`, `blastProtection`, `fireProtection`,
    `projectileProtection` je 1/6: 1–5; `unbreaking` 1/2: 2–5; `respiration` 1/6: 1–5;
    `aquaAffinity` 1/6: 1–5.
  - **H\*** (nur `diamond_helmet`, Godzilla.java:847-870): wie H, aber `respiration` 1–2.
  - **B** (Brust/Beine, Godzilla.java:873-890): die vier Schutzarten je 1/6: 1–5, `unbreaking` 1/2: 2–5.
  - **F** (Stiefel, Godzilla.java:913-921): `featherFalling` 1/6: **5–9**, `unbreaking` 1/2: 2–5.

  XP 10000 (Godzilla.java:54). Nach Vanilla-Regel gibt es XP nur nach einem Spielertreffer, Drops
  unabhängig vom Töter (1.7.10-Vanilla, abgeleitet). Insgesamt mindestens 201 einzelne
  Item-Entities (1 + 50 + 100 + 50, abgeleitet aus den Zeilen oben).

- **Spawnen:**
  - Biom-Spawnliste: `BiomeGenUtopianPlains.setVillageCreatures()` mit Gewicht 2, Gruppe 1–1,
    Kategorie Monster, nur bei `GodzillaEnable != 0` (BiomeGenUtopianPlains.java:214-216). Diese
    Methode ruft nur `WorldProviderOreSpawn3` auf (WorldProviderOreSpawn3.java:27), also die
    Dimension `Dimension-VillageMania` (`DimensionID3`, manifest `dimensions`). **Das Manifest
    führt für `mobzilla` `spawns: []`**; der Eintrag fehlt dort.
  - `getCanSpawnHere` (Godzilla.java:554-589) ruft **kein** `super` auf. Bedingungen:
    `isValidLightLevel()` (Vanilla-Dunkelheitstest), nicht Tag, `posY >= 50`,
    `godzilla_has_spawned == 0`, Chance 1/40. Eine Säule 17 × 17 von y+5 bis y+14 muss komplett
    Luft sein, und in `expand(64, 16, 64)` darf kein anderer Godzilla stehen. Erfolg setzt
    `godzilla_has_spawned = 1` (serverseitig).
  - **`OreSpawnMain.godzilla_has_spawned`** ist statisch. Jeder lebende Mobzilla setzt ihn in
    jedem KI-Tick auf 1 (Godzilla.java:257). Zurückgesetzt wird er nur im `static {}`-Block von
    OreSpawnMain (OreSpawnMain.java:6221), also einmal pro JVM-Start. Sobald in einer Spielsitzung
    ein Mobzilla existiert hat, spawnt keiner mehr natürlich, auch nicht nach einem Weltwechsel.
    Eier sind nicht betroffen.
  - Weitere Quellen:
    - Ei `GodzillaEgg` aus „Ancient Dried Mobzilla Spawn Egg" + Wassereimer. Das Dried Egg entsteht
      aus 9 Egg Parts (OreSpawnMain.java:2543-2547).
    - Erzblöcke Part und Full (OreSpawnWorld.java:665, 669; ChunkOreGenerator.java:338, 342).
    - `EasterBunny` kann `GodzillaEgg` droppen (EasterBunny.java:284).
  - `canDespawn`: nur bei `PlayNicely != 0` und ohne Persistenz; normal nie (Godzilla.java:111-113).
  - Peaceful: `EntityMob.onUpdate` entfernt ihn (yg.h-Bytecode). `getCanSpawnHere` prüft die
    Schwierigkeit nicht selbst.

- **Zustand:**

  | DataWatcher | Typ | Bedeutung | Herkunft |
  |---|---|---|---|
  | 20 | int | `attacking`: 1 im Nahkampfbereich oder während der Feuerstrahl-Phase. Vergrößert die Körper-Zerdrückzone auf ±16; Kiefer- und Armanimation (ModelGodzilla.java:622-666) | (Godzilla.java:77, 591-597) |
  | 21 | int | `PlayNicely`-Kopie für den Client-Renderer (Skalierung), jeden KI-Tick neu gesetzt | (Godzilla.java:78, 242, 92-94) |

  NBT: **keine eigenen Schlüssel.** `large_unknown_detected`, `hurt_timer`, `jump_timer`,
  `stream_count` und `ticker` gehen beim Neuladen verloren. `RenderInfo renderdata`
  (`rf1`–`rf4`, `ri1`–`ri4`) ist ein nicht synchronisierter Animationsspeicher, den `ModelGodzilla`
  liest und zurückschreibt (Godzilla.java:79-109; ModelGodzilla.java:459, 808).

- **Sounds:**

  | Anlass | Sound | Herkunft |
  |---|---|---|
  | Living | `orespawn:godzilla_living`, nur bei Chance 1/5 je Aufruf, sonst still | (Godzilla.java:150-155) |
  | Hurt | `orespawn:alo_hurt` | (Godzilla.java:157-159) |
  | Death | `orespawn:godzilla_death` | (Godzilla.java:161-163) |
  | Lautstärke / Tonhöhe | 1.65 / **fest** 1.1, ohne Zufallsvariation | (Godzilla.java:165-171) |
  | Landestoß- und Schwanztreffer | `random.explode` 0.85 am Opfer | (Godzilla.java:507) |
  | Blitzangriff | `random.explode` 0.5 am Ziel, dazu Explosion und Blitzdonner | (Godzilla.java:738-743) |
  | Kanone | `random.fuse` 1.0 (großer Ball), `random.bow` 1.0 (je Streuball) | (Godzilla.java:634, 646) |

- **Config:**
  - `OreSpawnMain.PlayNicely` (`PlayNicely`, Standard 0)
  - `OreSpawnMain.Godzilla_stats`: `Mobzilla_health` 4000, `Mobzilla_attack` 175,
    `Mobzilla_defense` 21 (OreSpawnMain.java:6186)
  - `OreSpawnMain.GodzillaEnable` (`GodzillaEnable`, Standard 1): schaltet nur den
    Biom-Spawneintrag
  - außerdem die Laufzeitstatik `godzilla_has_spawned`, `OreSpawnMain.OreSpawnRand` und die
    Gamerule `mobGriefing`

- **Portierung 1.21.1:**
  - **Virtuelles Leben:** Original 4000 HP (manifest) > 1024. Vorschlag: `MAX_HEALTH` 1024, ein
    eigenes synchronisiertes Lebensfeld mit 4000, und Regeneration (+5), Obergrenze (750) sowie
    Bossleiste auf dieses Feld beziehen. Alternativ eingehenden Schaden mit 1024/4000 skalieren.
    Halbierende Angriffe anderer Mobs (TheKing.java:252, BetterFireball.java:232-234) nehmen
    Godzilla aus; das bleibt so.
  - **Rüstungsformel:** 1.21.1 rechnet über `CombatRules` mit Toughness und einer Deckelung. Für
    ein 1:1-Ergebnis den Rüstungsschritt überschreiben und `amount * (25 - armor) / 25` für nicht
    rüstungsdurchdringenden Schaden rechnen. Die Methode ist vermutlich
    `LivingEntity.getDamageAfterArmorAbsorb`; Name und Signatur vor dem Schreiben in
    `neoforge-*-sources.jar` prüfen. Ohne diese Formel ist Mobzilla bei 21 Rüstung um ein Vielfaches
    leichter zu töten, und der „Immun bei 25"-Zustand fehlt.
  - Die Schadensobergrenze 750 wirkt **vor** der Rüstung, der eigene `hurt_timer` (20) zusätzlich
    zum Vanilla-`invulnerableTime`. Kaktus über `source.is(DamageTypes.CACTUS)` erkennen.
  - Schadensquellen:

    | 1.7.10 | 1.21.1 |
    |---|---|
    | `DamageSource.setExplosionSource(null).setExplosion()` | `damageSources().explosion(null, null)` |
    | `DamageSource.fall` | `damageSources().fall()` |
    | `causeMobDamage` | `damageSources().mobAttack(this)` |
    | Feuerball | `damageSources().fireball(ball, owner)` |

  - Explosionen `level.explode(this, x, y, z, 3.0f, Level.ExplosionInteraction.MOB)`; der
    `mobGriefing`-Bezug steckt in `MOB`. Blitze über `EntityType.LIGHTNING_BOLT` erzeugen.
    `thunderHit` leer überschreiben, `fireImmune()` im EntityType-Builder setzen.
  - **Blockzerstörung:** bis zu 2 × 33 × 33 = 2178 Blockzugriffe je Tick. Mit
    `BlockPos.MutableBlockPos` arbeiten, Luft überspringen, ungeladene Chunks mit
    `level.isLoaded(pos)` auslassen. `mobGriefing` über NeoForges `EventHooks.canEntityGrief`
    abfragen (Name prüfen). **Entscheidung nötig:** Die Ausschlussliste kennt nur 1.7.10-Blöcke.
    `deepslate`, `tuff`, `andesite`, `granite`, `diorite`, `mud`, Terracotta usw. wären 1:1
    zerdrückbar, dann gräbt Mobzilla sich durch die heutige Welt. Vorschlag: Liste auf Tags
    abbilden (`#minecraft:base_stone_overworld`, `#minecraft:dirt`, `#minecraft:sand` …) und
    zusätzlich unzerstörbare Blöcke (`getDestroySpeed < 0`) auslassen. Das Original zerstört z. B.
    `end_portal_frame`.
  - **Schwanzzone:** Das Vorzeichen (+sin / −cos) bewusst übernehmen; es ist kein Tippfehler,
    sondern die Position hinter Mobzilla.
  - KI-Ziele:

    | 1.7.10 | 1.21.1 |
    |---|---|
    | `EntityAISwimming` | `FloatGoal` |
    | `EntityAIMoveThroughVillage` | `MoveThroughVillageGoal` (verlangt `GroundPathNavigation` und POI-Dörfer; in der VillageMania-Dimension vermutlich wirkungslos) |
    | `EntityAIWatchClosest(EntityLiving, 50)` | `LookAtPlayerGoal(this, Mob.class, 50f)` |
    | `EntityAILookIdle` | `RandomLookAroundGoal` |
    | `EntityAIHurtByTarget` | `HurtByTargetGoal` |
    | `MyEntityAIWanderALot` | eigenes Goal mit `DefaultRandomPos.getPos(mob, 15, 7)` und busy-Flag |
    | `setAvoidsWater(true)` | `setPathfindingMalus(PathType.WATER, -1)` (prüfen) |
    | `setTarget(e)` (alte `entityToAttack`) | entfällt |

  - Pfadsuche für eine 9.9 breite Hitbox ist in Vanilla unzuverlässig und teuer.
  - `jump()` → `jumpFromGround()`; `fall` → `causeFallDamage` gibt `false` zurück.
  - `EntityDragon`-Sonderschaden → `EnderDragon.hurt(EnderDragonPart, DamageSource, float)` mit
    `head` / `body` (Feldnamen prüfen).
  - **Hitbox:** Abmessungen hängen von `PlayNicely` ab. Über `getDefaultDimensions(Pose)` aus dem
    synchronisierten Wert lesen und bei Änderung `refreshDimensions()` aufrufen, sonst weichen
    Client- und Server-Hitbox voneinander ab.
  - `SynchedEntityData`: zwei `EntityDataAccessor<Integer>` (attacking, playNicely); die Indizes
    20/21 entfallen.
  - **`godzilla_has_spawned`:** Als statisches Serverfeld behalten. Das Original setzt es nur beim
    JVM-Start zurück; ein Reset bei `ServerStartingEvent` wäre eine bewusste Abweichung, die man
    dokumentieren muss.
  - Spawn per Biome Modifier (`neoforge:add_spawns`, Gewicht 2, 1–1) für das Villages-Biom plus
    `SpawnPlacements` bzw. ein `checkSpawnRules`-Override mit den obigen Bedingungen.
    `canDespawn` → `removeWhenFarAway`.
  - **Drops:** `dropCustomDeathLoot` im Code, weil Wurfzahlen und Verzauberungen per `nextInt`
    gewürfelt werden. `xpReward = 10000`. Verzauberungen sind in 1.21.1 `Holder<Enchantment>`
    aus der Registry. Stufen über Vanilla (`featherFalling` 5–9) sind über
    `ItemStack.enchant` erlaubt. **Fallstrick:** Der zweite `sharpness`-Wurf erzeugt in 1.7.10 einen
    zweiten NBT-Eintrag, und gelesen wird der erste (1.7.10-Vanilla, abgeleitet). In 1.21.1 nimmt
    `ItemEnchantments.Mutable.upgrade` das Maximum. Für 1:1 den zweiten Wurf nur übernehmen, wenn
    noch keine Sharpness gesetzt ist. Über 200 `ItemEntity` auf einmal: 1:1 behalten, Vanilla
    fasst sie ohnehin zusammen.
  - **Renderer** (client): Skalierung 2.0 (manifest renderer_args), ×1/4 bei PlayNicely, also 0.5;
    Schattenradius 2.0. `ModelGodzilla` mit 71 Teilen, Textur 1024² (models_summary.txt:47);
    Animation in `render()` und `doTail()` (anim_summary.txt:48). `renderDistanceWeight 12` →
    `shouldRenderAtSqrDistance` überschreiben. `RenderInfo` gehört nicht als Client-Klasse ins
    Entity: einfache Felder behalten oder in einen Client-Zustand verlegen.

---

### GodzillaHead - MobzillaHead (`mobzilla_head`)

- **Rolle:** Hitbox-Stellvertreter für Mobzillas Kopf. Basis `EntityLiving`, **nicht** `EntityMob`
  (GodzillaHead.java:9). Unsichtbar (`RenderGodzillaHead.doRender` ist leer, Textur `null`;
  RenderGodzillaHead.java:17-31). Keine KI, keine eigenen Angriffe. Manifest nennt keinen
  lang-Namen (`name: "MobzillaHead"` ist der Registry-Name, OreSpawnMain.java:3779-3781).

- **Werte:**

  | Wert | Zahl | Herkunft |
  |---|---|---|
  | Hitbox | 9.9 × 10, `noClip = true` | (GodzillaHead.java:23-24) |
  | `fireResistance` / `isImmuneToFire` | 10000 / true | (GodzillaHead.java:25-26) |
  | MaxHealth | `Godzilla_stats.health` = 4000 (manifest); dient nur zur Anzeige, HP wird von Godzilla kopiert | (GodzillaHead.java:31, 153) |
  | Tempo | 1.33; `attackDamage` wird registriert und auf 0 gesetzt | (GodzillaHead.java:32-34) |
  | Position relativ zu Godzilla | y+16, 17 Blöcke vor `rotationYawHead` | (GodzillaHead.java:145-147) |
  | Kopplungsradius | erster Godzilla in `boundingBox.expand(32, 32, 32)` | (GodzillaHead.java:138) |
  | Spawnhöhe beim Erzeugen | Godzilla `posY + 20`; ab dem nächsten Tick y+16 | (Godzilla.java:350) |

- **KI und Angriffe:** keine. `onUpdate` ruft **kein** `super.onUpdate()` auf
  (GodzillaHead.java:116-159). Damit laufen weder Entity- noch Living-Tick: keine Bewegung über
  `moveEntity`, keine Regeneration, kein Todesablauf, kein Despawn-Test.
  - **Server:** Sucht den ersten `Godzilla` in 32 Blöcken und setzt `posX/posY/posZ` **direkt**
    (ohne `setPosition`). Kopiert `rotationYaw`, `rotationYawHead`, `motionX/Y/Z` und
    `setHealth(godzilla.getHealth())`. Ohne Godzilla folgt `setDead()`.
  - Jeden Tick `isAirBorne = true` und `setFire(0)` (GodzillaHead.java:120-121).
  - **Client:** Boot-artige Interpolation. `setPositionAndRotation2` setzt 6 Schritte (geritten:
    `par9 + 8`), `setVelocity` merkt sich die Geschwindigkeit (GodzillaHead.java:88-114, 122-136).
  - **Schadensweiterleitung** `attackEntityFrom` (GodzillaHead.java:59-82): `inWall` wird
    ignoriert, ebenso Quellen, deren Verursacher oder direkte Quelle Godzilla bzw. GodzillaHead ist.
    Sonst geht der Schaden **unverändert** an `Godzilla.attackEntityFrom` des ersten Godzilla in 32
    Blöcken, samt dessen Obergrenze, `hurt_timer` und Vergeltung. Der Kopf selbst nimmt nie Schaden.
  - `canBePushed = true`, `canBeCollidedWith = true`, `canTriggerWalking = false`, kein Fall
    (GodzillaHead.java:41-57, 84-86).
  - `BetterFireball` fliegt durch den Kopf (BetterFireball.java:147-150).

- **Interaktion:** keine (kein `interact`). Spieler treffen den Kopf und verletzen damit Mobzilla.

- **Drops:** keine. Der Kopf durchläuft nie `onDeath`, weil der Living-Tick fehlt; bei HP 0 bleibt er
  bis zum `setDead` stehen. Kein `dropFewItems`, keine XP.

- **Spawnen:** nur durch Godzilla: im Ziel-Zyklus ohne aktuelles Ziel, wenn in
  `expand(64, 40, 64)` kein Kopf liegt (Godzilla.java:347-351, 536-542). Nie bei `PlayNicely != 0`
  (Godzilla.java:520-523). Keine Biom-Spawns. `canDespawn` → `false` (GodzillaHead.java:37-39),
  entfernt wird er nur über `setDead`, wenn kein Godzilla in 32 Blöcken steht.

- **Zustand:** keine DataWatcher-Einträge (`entityInit` ruft nur `super`; GodzillaHead.java:51-53),
  keine NBT-Schlüssel. Tracking 128 / Update 10 / Velocity `true` (manifest; OreSpawnMain.java:3781).

- **Sounds:** keine eigenen. Living, Hurt und Death sind nicht überschrieben, werden aber nie
  ausgelöst, weil der Living-Tick und `super.attackEntityFrom` fehlen.

- **Config:** `OreSpawnMain.Godzilla_stats.health` (`Mobzilla_health`, 4000). Indirekt
  `PlayNicely` über Godzilla.

- **Portierung 1.21.1:**
  - **Empfehlung:** kein eigenständiges Entity, sondern NeoForges Multipart-Mechanik
    (`Entity#isMultipartEntity()` / `getParts()` mit `PartEntity<Godzilla>`), analog zu
    `EnderDragonPart`. Das beseitigt Spawnlogik, Kopplungsradius, Tracking und das Problem
    „Kopf verliert seinen Körper". Existenz und Signaturen vor dem Schreiben in
    `neoforge-*-sources.jar` prüfen.
  - Soll das Entity für 1:1 erhalten bleiben: Position **mit** `setPos()` setzen. Im Original
    bleibt die Server-Bounding-Box beim direkten Schreiben von `posX` stehen (1.7.10-Vanilla,
    abgeleitet), und die 32-Block-Suche geht dann vom Spawnort aus.
  - HP-Spiegel: 4000 > 1024. Entweder dieselbe virtuelle Lebensskala wie Godzilla, oder den Kopf
    nicht als `LivingEntity` modellieren. Er braucht keine eigenen HP.
  - Eine Weiterleitung darf nicht doppelt gegen `invulnerableTime` laufen: Schaden direkt an
    Godzillas eigene Schadenslogik geben.
  - Renderer: `NoopRenderer` (client) oder gar keiner bei PartEntity.

---

### Kraken - The Kraken (`the_kraken`)

- **Rolle:** feindlicher Flugboss, Basis `EntityMob` (Kraken.java:20). Fliegt ohne Flug-Navigator
  über direkt gesetzte `motion`-Werte. Nicht zähmbar; `interact` gibt `false` zurück
  (Kraken.java:928-930). Die meisten Wirkungen stehen unter `PlayNicely == 0`.

- **Werte:**

  | Wert | Zahl | Herkunft |
  |---|---|---|
  | XP | 500 | (Kraken.java:56) |
  | `fireResistance` / `isImmuneToFire` | 120 / true | (Kraken.java:57-58) |
  | Hitbox bei PlayNicely | 1.3333334 × 5 (normal 4 × 15, manifest) | (Kraken.java:49-54) |
  | Rüstung | `Kraken_defense` 10 (manifest) → 1.7.10-Formel: 15/25 = 60 % kommen durch | (Kraken.java:116-118; sv.b-Bytecode) |
  | Trefferzeitgeber | 30 Ticks | (Kraken.java:1204-1207) |
  | `long_enough` (Verweildauer) | 3600 Ticks, zählt je KI-Tick herunter | (Kraken.java:44, 964-966) |
  | Gewitter-Takt | erstes Mal nach 10 Ticks, dann alle 100 Ticks | (Kraken.java:43, 153-168) |
  | Regen- und Donnerdauer | je 300 Ticks | (Kraken.java:158-165) |
  | Eigener Blitz | Chance 1/400 je Tick bei `posY - 16` | (Kraken.java:968-970) |
  | Greifreichweite | Abstand² < 30, Höhe relativ zu Ziel+15 gerechnet | (Kraken.java:1106-1110) |
  | Trageschaden | Chance 1/50 je Tick `attackEntityAsMob`, Attribut 40 (manifest) | (Kraken.java:1053-1055) |
  | Fallschaden / Blitz | keiner | (Kraken.java:1194-1195, 1223-1227) |
  | Fluggeschwindigkeit | horizontal Zielwert ±0.45, Angleichung 0.15; vertikal ±0.70999, Angleichung 0.202 | (Kraken.java:1071-1073) |
  | Vertikale Dämpfung nach dem Tick | `motionY *= 0.72` unter dem Ziel, `*= 0.5` darüber | (Kraken.java:147-152) |
  | Drehrate | ein Fünftel der Winkeldifferenz je Tick; bei horizontalem Tempo < 0.15 keine Drehung | (Kraken.java:1074-1080) |
  | `moveForward` | 0.4 | (Kraken.java:1076) |
  | Höhenlimit | `posY > 256` und nicht persistent → `setDead` | (Kraken.java:1097-1099) |

- **KI und Angriffe:**

  | Prio | Task | Herkunft |
  |---|---|---|
  | 1 | `EntityAILookIdle` | (Kraken.java:61) |
  | Ziel 1 | `EntityAIHurtByTarget(false)`; das gesetzte `attackTarget` benutzt die eigene Logik nirgends | (Kraken.java:62) |

  `setAvoidsWater(false)` (Kraken.java:55). `updateAITasks` (Kraken.java:953-1100), jeden Tick nach
  `super.updateAITasks()`:

  1. **Timer:** `hurt_timer--`, `long_enough--`, DataWatcher 21 = `PlayNicely`; Blitz mit Chance
     1/400 bei `PlayNicely == 0`.
  2. **Neues Flugziel**, wenn `newtarget != 0`, Chance 1/250 oder Abstand² zum Ziel < 9.1
     (Kraken.java:974-1012):
     - Bodensuche senkrecht bis 30 Blöcke nach unten. Ein Treffer setzt `straight_down = 0`
       **dauerhaft**; es startet bei 1 und wird nie wieder 1 (Kraken.java:47, 977-983).
     - `ground_dist = 20 - Bodenabstand`, ohne Boden also −11 (Kraken.java:984).
     - Bis zu 50 Versuche: Versatz xz je ±12 bis ±17, bei `straight_down` 0; y =
       `posY + ground_dist + rand(9) - 6`. Das Ziel wird akzeptiert, wenn der Block Luft und die
       Sichtlinie von `posY + 0.75` frei ist (Kraken.java:985-1002, 949-951). Ergebnis: der Kraken
       hält 14 bis 22 Blöcke über dem Boden; ohne Boden sinkt er.
     - **Flucht:** Ist `long_enough <= 0` oder gilt `posY < 200` und HP < Max/4, geht das Ziel
       30 Blöcke höher (Kraken.java:1003-1004).
     - **Verstärkung, einmalig:** Wurde er von einem Spieler getroffen (`hit_by_player`), hat er
       noch nicht gerufen, liegt HP < Max/8 und `posY > 130`, spawnen **10 × „The Kraken"** bei
       y 170, ±9 xz (Kraken.java:1005-1010). `spawnCreature` spielt dabei den Living-Sound
       (Kraken.java:128-137). Das prüft **nicht** `PlayNicely`.
  3. **Jagd**, sonst, wenn `caught == null`, Chance 1/8 und `PlayNicely == 0`
     (Kraken.java:1013-1035):
     - Nächster `EntityPlayer` in `expand(25, 40, 25)`. Kein Kreativmodus und sichtbar →
       Flugziel 15 über dem Spieler, dann `attackWithSomething`. Im Kreativmodus wird der Spieler
       verworfen.
     - Nur wenn **kein** Spieler gefunden wurde (ein unsichtbarer Spieler blockiert den Fallback):
       Chance 1/2 → `findSomethingToAttack` in `expand(20, 40, 20)`, sortiert mit
       `GenericTargetSorter`, erstes `isSuitableTarget` → Flugziel +15, `attackWithSomething`
       (Kraken.java:1175-1192).
     - `attackWithSomething` (Kraken.java:1102-1114): Liegt der Abstand² mit dem Kraken 15 Blöcke
       über dem Ziel unter 30, gilt `caught = Ziel`, `release = 0`, `setAttacking(1)`.
  4. **Tragen** (Kraken.java:1036-1067):
     - Flugziel `(x, 200, z)`; über `posY > 190` wird `release = 1`.
     - Beute bekommt die Kraken-Geschwindigkeit, `posX/posZ` des Kraken, `posY = kraken.posY - 15`
       und den Yaw. Ist der Abstand > 16, zusätzlich `motionY += 0.25`.
     - Je Tick mit Chance 1/50 ein Biss.
     - Loslassen bei `release` oder Chance 1/250: `caught = null`, `newtarget = 1`,
       `setAttacking(0)`. Die Beute stürzt aus bis zu etwa 190 Blöcken; der Fallschaden ist die
       eigentliche Tötungsart.
     - Tote Beute wird sofort verworfen.
  5. **Hindernisausweichen** (Kraken.java:1081-1096): 19 Schichten (k = −20 bis 16, Schritt 2) × 5
     Punkte (1 bis 9 Blöcke voraus, Schritt 2). Je Nicht-Luft-Block +0.1; danach `motionY` **und**
     `posY` je `+= Summe * 0.08`.

  **`isSuitableTarget`** (Kraken.java:1116-1173):
  - Grundbedingungen: nicht null, nicht selbst, lebendig, nicht `isIgnoreable`, sichtbar.
  - **Spieler** nur, wenn weder Kreativmodus noch Flugmodus. Die direkte Spielersuche in Punkt 3
    prüft den Flugmodus **nicht**.
  - Sonst muss das Ziel `onGround` sein oder im Wasser stehen.
  - Nie: `EntitySquid`, `AttackSquid`, `Kraken`, `Spyro`, `EntityChicken`, `Chipmunk`, `StinkBug`,
    `Mothra`.
  - Nur wenn ungeritten: `Dragon`, `Cephadrome`, `Leon`, `ThePrinceTeen`, `ThePrinceAdult`.

  **`attackEntityFrom`** (Kraken.java:1197-1213):
  - Ein Spieler als Angreifer bei HP > Max/4 → `hit_by_player = true`, Flugziel 15 über dem
    Angreifer. Das passiert **vor** der Zeitgeberprüfung, auch bei abgewiesenen Treffern.
  - Danach: bei `hurt_timer > 0` → `false`. Sonst `hurt_timer = 30`, `super`, und mit Chance 1/2
    `release = 1`: der Treffer lässt die Beute fallen.

  **Sonderschaden anderer Mobs gegen Kraken:**

  | Mob | Rechnung | Herkunft |
  |---|---|---|
  | `Cephadrome` | 1.5 × 70 in dem gelesenen Zweig | (Cephadrome.java:422-425) |
  | `Dragon` | 2 × 35 | (Dragon.java:348-351) |
  | `Leon` | 4 × 55 | (Leon.java:270-273) |
  | `ThePrinceTeen` | 2 × 45 | (ThePrinceTeen.java:300-303) |
  | `ThePrinceAdult` | Sonderfall ab ThePrinceAdult.java:295 | offen: nicht gelesen |

  Die Recherche nennt 140 Schaden des Cephadrome gegen den Kraken (01-mobs.md:83); der gelesene
  Zweig ergibt 105.

- **Interaktion:**
  - Kein Rechtsklick (`interact` → `false`).
  - Einfangen: `EntityCage` scheitert mit Chance 95/100, sonst `CagedKraken`
    (EntityCage.java:441-450).
  - **`KrakenRepellent`**: prüft alle 10 Ticks eine Box ±20 xz, −10 bis +40 y. Jeden Kraken schiebt
    es horizontal weg mit Kraft `(20 - Abstand) * 0.4`, gekappt auf 0 bis 8; der Abstand wird zu
    `posY - 15` des Kraken gerechnet (KrakenRepellent.java:57-59, 78-101).
  - Bossleiste „Kraken" über `getKrakenHealth()/getMaxHealth()` (Kraken.java:97-99;
    GirlfriendOverlayGui.java:222-226).

- **Drops** (`dropFewItems`, Kraken.java:219-926). `getDropItem` → `Items.quartz`
  (Kraken.java:205-207) wird **nie** benutzt, weil `dropFewItems` ohne `super` überschrieben ist.
  Position ±7 xz, y+1 (Kraken.java:212):

  | Item | Anzahl | Herkunft |
  |---|---|---|
  | `MyKrakenTooth` (`krakentooth`) | 1 | (Kraken.java:221) |
  | `item_frame` | 1 | (Kraken.java:222) |
  | `dye` Meta 0 (= Tintenbeutel) | 120–279 | (Kraken.java:223-225) |
  | Wurftabelle | 5–14 Würfe auf `nextInt(53)` | (Kraken.java:226-227) |

  Die Wurftabelle entspricht **Mobzillas Fällen 0–52** mit denselben Items und
  Verzauberungsschemata (S/T/P/H/H\*/B/F; Kraken.java:228-924). Fall 41 ist wieder der verzauberte
  Goldapfel bei ±2 xz (Kraken.java:735-743). Alle 53 Fälle sind belegt, jeder Wurf droppt. XP 500
  (Kraken.java:56).

- **Spawnen:**
  - **Kein** Biom-Spawneintrag im Quelltext; die Suche nach `SpawnListEntry((Class)Kraken` findet
    nichts, und das Manifest meldet `spawns: []`.
  - `getCanSpawnHere` (Kraken.java:1229-1244) ist praktisch nur für Spawner relevant: `posY >= 50`
    und darüber ein Raum 2 (x) × 5 (y) × 3 (z) aus Luft oder `tallgrass`. Kein Licht- und kein
    Schwierigkeitstest.
  - Quellen:
    - Stirbt ein `AttackSquid` durch einen Spieler: Chance 1/15, nicht in `DimensionID5`,
      `KrakenEnable != 0`, `wasshot == 0` → 1–3 Kraken bei y 170, ±3 xz
      (AttackSquid.java:385-388).
    - Die Verstärkung des Kraken selbst (siehe KI).
    - `KrakenEgg` aus „Ancient Dried Kraken Spawn Egg" + Wassereimer (OreSpawnMain.java:2483-2485).
    - Erzblock (OreSpawnWorld.java:593; ChunkOreGenerator.java:266).
    - `EasterBunny` kann `KrakenEgg` droppen (EasterBunny.java:224).
  - **`canDespawn`** (Kraken.java:932-947): bei Persistenz `false`; bei `long_enough <= 0` `true`,
    also nach 3600 Ticks despawnbar; bei `posY > 150` und HP < Max/2 `true`. Der Zweig
    `posY > 180 && long_enough <= 0 → setDead` ist toter Code, weil der erste Zweig schon greift.
  - Peaceful: `EntityMob.onUpdate` entfernt ihn (yg.h-Bytecode).

- **Zustand:**

  | DataWatcher | Typ | Bedeutung | Herkunft |
  |---|---|---|---|
  | 20 | int | `attacking`: 1 solange Beute getragen wird; steuert `dangle_tentacle` (ModelKraken.java:1139-1156) | (Kraken.java:74, 1215-1221) |
  | 21 | int | `PlayNicely`-Kopie für die Renderer-Skalierung | (Kraken.java:75, 967, 89-91) |

  NBT: `LongEnough` (int) (Kraken.java:172-180). Fehlt der Schlüssel, liest `getInteger` 0 und der
  Kraken ist sofort im Flucht- und Despawn-Zustand. Nicht gespeichert werden `hit_by_player`,
  `call_reinforcements`, `caught`, `currentFlightTarget`, `straight_down`, `weather_set` und
  `hurt_timer`. `RenderInfo` wie bei Godzilla (ModelKraken.java:1151, 1243).

- **Sounds:**

  | Anlass | Sound | Herkunft |
  |---|---|---|
  | Living | `orespawn:kraken_living`, nur bei Chance 1/5 je Aufruf | (Kraken.java:182-187) |
  | Hurt | keiner (`null`) | (Kraken.java:189-191) |
  | Death | `orespawn:alo_death` | (Kraken.java:193-195) |
  | Lautstärke / Tonhöhe | 2.0 / fest 1.0 | (Kraken.java:197-203) |
  | Verstärkung | Living-Sound jedes neu gespawnten Kraken | (Kraken.java:134) |
  | Blitz | Donner des Vanilla-Blitzes | (Kraken.java:969) |

- **Config:**
  - `OreSpawnMain.PlayNicely`
  - `OreSpawnMain.Kraken_stats`: `Kraken_health` 1000, `Kraken_attack` 40, `Kraken_defense` 10
    (OreSpawnMain.java:6187)
  - `OreSpawnMain.KrakenEnable` (`KrakenEnable`, Standard 1): wird nur in `AttackSquid` gelesen,
    nicht im Kraken selbst
  - außerdem `OreSpawnMain.OreSpawnRand` für die Drop-Position

- **Portierung 1.21.1:**
  - HP 1000 ≤ 1024 (manifest): kein virtuelles Leben nötig. Rüstung 10 ≤ 30, aber **Formel wie bei
    Godzilla** ersetzen: 60 % Durchlass statt der 1.21-Rechnung.
  - **Flug:** `Monster` mit direkten `setDeltaMovement`-Impulsen behalten; Schwerkraft bleibt an,
    wie im Original (`moveEntityWithHeading`). Die Reihenfolge ist entscheidend: KI-Impulse in
    `customServerAiStep`, die vertikale Dämpfung (×0.72 / ×0.5) **nach** `super.tick()`.
    `moveForward 0.4` → `setZza(0.4f)`. Das Hindernisausweichen verschiebt `posY` direkt, dafür
    `setPos` verwenden.
  - **Wetter:** Regnet es nicht, `ServerLevel.setWeatherParameters(0, 300, true, true)`. Sonst nur
    die Dauer verlängern, über `ServerLevelData.setRainTime(300)` / `setThunderTime(300)` (Cast von
    `level.getLevelData()`, prüfen). In Nicht-Overworld-Dimensionen ist `DerivedLevelData` ein No-op;
    das war im Original genauso.
  - **Tragen von Spielern:** Serverseitig gesetzte Positionen eines `ServerPlayer` sind nicht
    maßgeblich. Je Tick `teleportTo` / `connection.teleport` oder `setDeltaMovement` + `hurtMarked`
    verwenden, sonst zappelt der Spieler clientseitig weg. Für Mobs genügt `setPos`. Reiten
    (`startRiding`) wäre eine Verhaltensänderung, weil Schleichen den Spieler befreien würde.
  - Blitz → `EntityType.LIGHTNING_BOLT`; `thunderHit` leer überschreiben; `fireImmune()`.
  - `canDespawn` → `removeWhenFarAway(double)` mit derselben Logik; `isNoDespawnRequired` →
    `isPersistenceRequired()`. Das Höhenlimit 256 bleibt eigene Logik; in 1.21.1 liegt das
    Weltlimit bei 320, der Kraken verschwindet also schon früher.
  - Verstärkung: `EntityType` statt des Namens „The Kraken"; `finalizeSpawn` und der Living-Sound
    wie im Original.
  - Hitbox abhängig von `PlayNicely`: wie bei Godzilla über `getDefaultDimensions` aus
    `SynchedEntityData` plus `refreshDimensions()`.
  - NBT `LongEnough` in `addAdditionalSaveData` / `readAdditionalSaveData`; den 0-Fall bei fehlendem
    Schlüssel beibehalten oder bewusst mit 3600 belegen (dann dokumentieren).
  - Drops: `dropCustomDeathLoot`; der Tintenbeutel ist `Items.INK_SAC` (1.7.10 `dye` Meta 0).
    Verzauberungs-Fallstrick wie bei Godzilla (doppelte Sharpness). `xpReward = 500`.
  - **Renderer** (client): Skalierung 1.0, ×1/3 bei PlayNicely; Schattenradius 1.0.
    `ModelKraken` mit 111 Teilen, Textur 512² (models_summary.txt:55). `dangle_tentacle` nutzt
    `glPushMatrix/glTranslatef/glRotatef`, im Port `PoseStack` (anim_summary.txt:56).

---

**Abweichungen Recherche ↔ Quelltext** (Quelltext gilt):
- 01-mobs.md:251 „Attack 100–175". Das Attribut ist 175. Weitere Werte: Blitzangriff 100
  (Godzilla.java:730), Stöße 87/43 und 1750 gegen große Wesen (Godzilla.java:659).
- 01-mobs.md:167/1150 „Schadensdeckel 120" (NW, unbelegt). Er stimmt **rechnerisch**: Obergrenze 750
  (Godzilla.java:697) × 4/25 bei Rüstung 21 = 120.
- 01-mobs.md:83/542 „Cephadrome 140 gegen Kraken". Der gelesene Zweig ergibt 1.5 × 70 = 105
  (Cephadrome.java:422-425); ein anderer Zweig ist nicht geprüft.
- Manifest `mobzilla.spawns: []` gegen den Biom-Eintrag in BiomeGenUtopianPlains.java:214-216
  (VillageMania).
