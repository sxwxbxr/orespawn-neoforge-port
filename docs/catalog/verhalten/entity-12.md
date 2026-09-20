# Verhalten: entity-12

Dieser Batch umfasst neun Klassen aus drei Gruppen. Die erste sind zwei **Wasser-Minibosse** mit fast identischem Code: `SeaMonster` (nachts) und `SeaViper` (tags, Gift). Die zweite ist das Gespann **Spider Driver + Robot Spider**: ein KI-Spinnenfahrer und ein reitbarer 1500-HP-Roboter mit Tritt- und Kieferangriff, selbstgebauter Fahrphysik und clientseitiger Bein-IK. Die dritte sind Kleinkram und Haustiere: der Wasser-Mob `Skate`, der säurespuckende `SpitBug`, der zähmbare `Spyro` (Baby Dragon, wächst zum `Dragon`), der Nausea-Furz-Käfer `StinkBug` und das Wurfgeschoss `Shoes`. Vier Muster ziehen sich durch den ganzen Batch und müssen beim Port einmal zentral gelöst werden:
- eine eigene Zielsuche (`findSomethingToAttack` plus `GenericTargetSorter`) neben dem Vanilla-Goal-System,
- ein `hurt_timer`, der Schaden komplett verwirft,
- ein `scan_it`-Würfelscan nach Wasser bzw. Lava,
- überschriebene `dropFewItems`, wodurch `getDropItem` in mehreren Klassen toter Code ist.

Dazu kommt ein Querschnittsdetail: Die Vergeltung in `attackEntityFrom` prüft `instanceof EntityLiving`. `EntityPlayer` ist aber nur `EntityLivingBase`, also greift dieser Zweig bei Spielern nie; das übernimmt nur `EntityAIHurtByTarget`.

Vanilla-Fakten aus MC 1.7.10, die nicht im OreSpawn-Quelltext stehen, sind als "(Vanilla 1.7.10)" markiert und nicht mit Zeilennummer belegt.

---

### SeaMonster - Sea Monster (`sea_monster`)

- **Rolle:** Feindlicher Wasser-Miniboss, Basis `EntityMob`. Er atmet unter Wasser (SeaMonster.java:613-615), stirbt aber langsam an Land.
- **Werte:**
  - XP 150 (SeaMonster.java:41), `fireResistance` 30 (SeaMonster.java:42).
  - Tempo im Wasser 0.55, an Land 0.25 (SeaMonster.java:115-120). `onUpdate` setzt das Attribut jeden Tick neu (SeaMonster.java:81-84).
  - Rüstung = `SeaMonster_stats.defense` (SeaMonster.java:105-107), Default 8 (manifest).
  - Schadenssperre: Nach einem angenommenen Treffer wird 8 Ticks lang **jeder** weitere Schaden verworfen. Das Retargeting läuft trotzdem (SeaMonster.java:363-366).
  - Kaktusschaden immun (SeaMonster.java:359-361).
  - Regeneration: im Wasser mit 1/120 pro AI-Tick +1 HP, dazu Sound `splash` 1.5 (SeaMonster.java:518-521).
  - Austrocknen: außerhalb von Wasser mit 1/25 pro Tick Scan nach Wasser (Radius i=1..11, dy max 10, ab i>=5 in 2er-Schritten), dann Pfad dorthin mit Tempo 1.33. Ohne Fund mit 1/40 -1 HP; bei <=0 `setDead()` **ohne Drops** (SeaMonster.java:469-498).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`, 1 `MyEntityAIWanderALot`(xzRange 16, Tempo 1.0), 2 `EntityAIWatchClosest` Player 10.0, 3 `EntityAIWatchClosest` EntityLiving 8.0, 4 `EntityAILookIdle`. Target: 1 `EntityAIHurtByTarget`(false) (SeaMonster.java:46-51). Pfadfindung meidet Wasser nicht (SeaMonster.java:40).
  - Eigene Zielsuche mit 1/5 pro AI-Tick. Box `expand(16,4,16)`, nach Distanz sortiert; ein lebendes `getAttackTarget()` hat Vorrang (SeaMonster.java:551-573). `PlayNicely != 0` schaltet sie ab (SeaMonster.java:552-554).
  - Zielfilter (SeaMonster.java:524-549): sichtbar, lebend; Spieler nur ohne Creative; kein anderes `SeaMonster`; jedes `EntityMob` (also auch `SeaViper`); sonst `MyUtils.isAttackableNonMob` (u. a. Mothra, Leon, Dragon, Spyro, Royalty, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, EntityVillager, Stinky; MyUtils.java:13-14).
  - Nahkampf: bei `distSq < (4 + w/2)^2` wird `setAttacking(1)` gesetzt und mit `rand(4)==0 || rand(5)==1` zugeschlagen, abgeleitet 40 % pro Prüfung (SeaMonster.java:504-508). Ist das Ziel weiter weg, folgt Pfad mit Tempo 1.0 (SeaMonster.java:511); ohne Ziel `setAttacking(0)`.
  - Schaden = Attribut `SeaMonster_stats.attack` über `super.attackEntityAsMob`, Default 14 (manifest). Knockback 0.6 horizontal, 0.1 vertikal, gegen Spieler oder tote Ziele 0.2 (SeaMonster.java:341-355).
  - Wird er getroffen: ist der Angreifer ein `EntityLiving` (nicht `SeaMonster`), wird er Ziel und Pfad mit Tempo 1.2 (SeaMonster.java:367-374).
- **Interaktion:** keine, `interact` liefert false (SeaMonster.java:337-339). Critter Cage: 30 % Fehlschlag, sonst `CagedSeaMonster` (EntityCage.java:747-756).
- **Drops** (`dropFewItems`, verstreut ±1 Block, y+1; SeaMonster.java:154-166):
  - fest: 1 `seamonsterscale`, 1 `item_frame`, 9-14 `fish` (SeaMonster.java:166-170)
  - dazu `rand(20)` (SeaMonster.java:171-331):

    | Wurf | Drop | Verzauberungen (je Chance) |
    |---|---|---|
    | 1 | `iron_ingot` | - |
    | 3 | `iron_sword` | 1/6 je sharpness, bane, knockback, looting, fireAspect, sharpness nochmals (1-5); 1/2 unbreaking 2-5 |
    | 4 | `iron_shovel` | 1/2 unbreaking 2-5, 1/6 efficiency 1-5 |
    | 5 | `iron_pickaxe` | wie Schaufel + 1/6 fortune 1-5 |
    | 6 | `iron_axe` | wie Schaufel |
    | 7 | `iron_hoe` | wie Schaufel |
    | 8 | `iron_helmet` | 1/6 je protection, blast, fire, projectile (1-5); 1/2 unbreaking 2-5; 1/6 respiration 1-2; 1/6 aquaAffinity 1-5 |
    | 9, 10 | `iron_chestplate`, `iron_leggings` | 1/6 je protection, blast, fire, projectile (1-5); 1/2 unbreaking 2-5 |
    | 11 | `iron_boots` | 1/6 featherFalling **5-9**; 1/2 unbreaking 2-5 |
    | 13 | `iron_block` | - |
    | 0, 2, 12, 14-19 | nichts | - |

  - `getDropItem` = `fish` (SeaMonster.java:150-152) ist toter Code, weil `dropFewItems` überschrieben ist.
  - XP 150.
- **Spawnen:**
  - Overworld `waterCreature`: ocean Gewicht 4, swampland Gewicht 2, je 1-1 (manifest; OreSpawnMain.java:4510-4511).
  - `getCanSpawnHere` (SeaMonster.java:583-611): sofort true, wenn in x/z -3..+2, y 0..+4 ein Spawner "Sea Monster" steht. Sonst gilt: `posY >= 50`, **Nacht**, `isValidLightLevel()`, kein anderes `SeaMonster` in `expand(16,5,16)`.
  - Struktur: `makeMonsterIsland` setzt mit 50 % einen "Sea Monster"-Spawner, sonst "Sea Viper" (GenericDungeon.java:5196-5203). Overworld-Dungeon-Wurf i==4 (OreSpawnWorld.java:269-285).
  - Despawn: `!isNoDespawnRequired()` (SeaMonster.java:77-79).
- **Zustand:** DataWatcher 20 = `attacking` (int 0/1, für das Model; SeaMonster.java:63, 575-581). `RenderInfo` (rf1-4, ri1-4) ist rein clientseitiger Animationsspeicher und wird nicht synchronisiert. Keine eigenen NBT-Schlüssel.
- **Sounds:** `orespawn:seamonster_living` (nur mit 1/3, sonst stumm), `orespawn:seamonster_hit`, `orespawn:seamonster_death`, Lautstärke 1.0, Pitch 1.0 (SeaMonster.java:127-148). `splash` beim Heilen.
- **Config:** `SeaMonsterEnable` (1), `SeaMonster_health` (110), `SeaMonster_attack` (14), `SeaMonster_defense` (8) (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster` mit `WaterBoundPathNavigation` oder amphibischer Navigation (`setPathfindingMalus(WATER, 0)`).
  - Tempo per `AttributeModifier` statt Base-Überschreiben pro Tick.
  - `hurt_timer` als Feld; `hurt` gibt false zurück, ohne Invulnerabilitäts-Ticks zu setzen.
  - `isAttackableNonMob` als Tag oder Predicate zentral bauen.
  - Verzauberungen über `Holder<Enchantment>` aus `registryAccess().lookupOrThrow(Registries.ENCHANTMENT)`. `featherFalling` 5-9 übersteigt das Vanilla-Maximum 4 und muss per `ItemEnchantments.Mutable.set` direkt gesetzt werden.
  - Nacht-Spawn: `SpawnPlacementRegisterEvent` mit eigenem Predicate; Spawner-Sonderfall über `MobSpawnType.SPAWNER`.
  - Verdursten ohne Drops: `discard()` statt `die()`.

### SeaViper - Sea Viper (`sea_viper`)

- **Rolle:** Feindlicher Wasser-Miniboss, Basis `EntityMob`, Code-Zwilling von `SeaMonster` mit Gift und Tag-Spawn. Atmet unter Wasser (SeaViper.java:627-629).
- **Werte:**
  - XP 120 (SeaViper.java:44), `fireResistance` 30 (SeaViper.java:45).
  - Starttempo 0.35 (SeaViper.java:41; manifest listet 0.25 und 0.35, weil der Feld-Initializer vorher 0.25 setzt). Laufend: Wasser 0.75, Land 0.25 (SeaViper.java:118-123).
  - Schadenssperre 5 Ticks (SeaViper.java:380-383); Kaktus immun (SeaViper.java:376-378).
  - Regeneration im Wasser 1/100 pro AI-Tick +1 HP mit `splash` (SeaViper.java:535-538).
  - Austrocknen: Scan wie SeaMonster (SeaViper.java:486-505), ohne Wasserfund 1/150 -1 HP (SeaViper.java:508-514).
  - `stream_count` ist deklariert, aber unbenutzt (SeaViper.java:22).
- **KI und Angriffe:**
  - Tasks identisch zu SeaMonster (SeaViper.java:49-54).
  - Zielsuche 1/5, Box `expand(18,4,18)` (SeaViper.java:568-590). Filter wie SeaMonster, schließt aber `SeaViper` statt `SeaMonster` aus (SeaViper.java:541-566).
  - Nahkampf bei `distSq < (4.5 + w/2)^2` mit `rand(2)==0 || rand(4)==1`, abgeleitet 62,5 % (SeaViper.java:521-525). Verfolgungstempo 1.5 (SeaViper.java:528).
  - Schaden = Attribut `SeaViper_stats.attack`, Default 22 (manifest). Knockback 0.8 / 0.14, gegen Spieler oder tote Ziele 0.28 (SeaViper.java:349-355).
  - Gift: mit 50 % `Potion.poison` Stufe 0, Dauer `var2*20` Ticks (SeaViper.java:365-367).
  - **Bug:** Die Schwierigkeitsstaffel ist falsch verschachtelt. Nur EASY setzt 8 s; NORMAL (10) und HARD (12) liegen im EASY-Zweig und sind unerreichbar. Ergebnis: EASY 8 s, NORMAL/HARD 6 s (SeaViper.java:346, 356-364).
  - Vergeltung wie SeaMonster (SeaViper.java:384-391).
- **Interaktion:** keine (SeaViper.java:341-343). Critter Cage: 40 % Fehlschlag (EntityCage.java:758-767).
- **Drops** (SeaViper.java:167-336):
  - fest: 1 `seavipertongue`, 1 `item_frame`
  - 9-14 Durchläufe mit je 1 `fish` **und** 1 `chicken` (SeaViper.java:171-174)
  - `rand(20)`-Tabelle identisch zu SeaMonster (SeaViper.java:175-335)
  - `getDropItem` = `chicken` ist toter Code (SeaViper.java:153-155). XP 120.
- **Spawnen:**
  - Overworld `waterCreature`: ocean Gewicht 3, stoneBeach Gewicht 2, je 1-1 (manifest; OreSpawnMain.java:4514-4515).
  - `getCanSpawnHere`: Spawner "Sea Viper" in der Nähe → true; sonst `posY >= 50`, **Tag**, kein anderes `SeaViper` in `expand(16,5,16)`. **Kein** Lichtcheck (SeaViper.java:600-625).
  - `makeMonsterIsland` (GenericDungeon.java:5200).
  - Despawn: Standard (SeaViper.java:80-82).
- **Zustand:** DataWatcher 20 = `attacking` (SeaViper.java:66, 592-598); keine NBT.
- **Sounds:** `orespawn:seaviper_living` (1/2 Chance), `orespawn:seaviper_hit`, `orespawn:seaviper_death`, Lautstärke 1.0, Pitch 1.0 (SeaViper.java:130-151).
- **Config:** `SeaViperEnable` (1), `SeaViper_health` (160), `SeaViper_attack` (22), `SeaViper_defense` (12) (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Gemeinsame abstrakte Basis `AbstractSeaBoss` mit SeaMonster; Parameter: Tempo, Sperr-Ticks, Radien, Heil- und Dürrechancen.
  - Gift: `MobEffects.POISON`. Den Schwierigkeits-Bug bewusst 1:1 übernehmen (6 s / 8 s) und kommentieren.
  - Research (01-mobs.md) nennt "hides in water, hissing"; der Code enthält kein Verstecken, nur Wasser-Tempo.

### Shoes - Shoes (`shoes`)

- **Rolle:** Wurfgeschoss, Basis `EntityThrowable`; es fliegt sich drehend.
- **Werte:**
  - `ShoeId` zufällig 2-5, sofern der Konstruktor keinen Wert bekommt (Shoes.java:19, 35, 51).
  - Id-Zuordnung über `ItemShoes.my_id`: 2 `redheels`, 3 `blackheels`, 4 `slippers`, 5 `boots`, 6 `gamecontroller` (OreSpawnMain.java:1362-1366).
  - Schaden beim Entity-Treffer (Shoes.java:59-81), Reihenfolge der Überschreibung:

    | Bedingung | Schaden |
    |---|---|
    | Basis | 2.0 |
    | `ShoeId` 6 | 6.0 |
    | Ziel `EntityCreeper` | +4.0 |
    | Ziel `Girlfriend` oder `Boyfriend` | 1.0 |
    | Ziel `EntityPlayer` | 0.0 |
    | `OreSpawnMain.valentines_day != 0` | 10.0 (überschreibt alles, **auch gegen Spieler**) |

  - Schadensquelle `causeThrownDamage(this, thrower)`.
  - Drehung +20° pro Tick in `rotationPitch` (Shoes.java:91-100).
  - Schwerkraft und Tempo sind Vanilla-`EntityThrowable` (nicht überschrieben).
- **KI und Angriffe:** keine KI.
  - Werfer 1: Spieler per Rechtsklick mit `ItemShoes`. Verbraucht 1 außer im Creative, Sound `random.bow` 0.5 (ItemShoes.java:22-30).
  - Werfer 2: `Girlfriend` mit Id 2+rand(4), Tempo 1.8, Streuung 4.0 (Girlfriend.java:1007-1013).
  - Werfer 3: `Boyfriend` mit Id 6 (Boyfriend.java:907).
  - Beim Aufprall 4x Partikel `snowballpoof` + `reddust`, danach `setDead` serverseitig (Shoes.java:82-88). Kein Item-Drop, der Schuh ist weg.
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur durch Werfer. Registrierung "Shoes", Tracking 64/1/true (OreSpawnMain.java:4646-4648).
- **Zustand:** DataWatcher 20 = `ShoeId` (int; Shoes.java:20, 55-57). Er wird im **Konstruktor** registriert, nicht in `entityInit`. Keine NBT, die Id geht beim Neuladen verloren und wird neu gewürfelt.
- **Sounds:** keine eigenen; der Werfer spielt `random.bow`.
- **Config:** keine Config-Datei. `valentines_day` wird zur Laufzeit gesetzt, wenn das Datum 14.02. ist (Monatsindex 1, Tag 14; OreSpawnMain.java:4227-4229).
- **Portierung 1.21.1:**
  - `ThrowableItemProjectile`; das Item bestimmt die Id, dann entfällt der DataWatcher. `getDefaultItem()` liefert je nach Id das Schuh-Item, und `ThrownItemRenderer` rendert das Item-Icon.
  - Das 256er-Atlas `spinners.png` samt Icon-Index (RenderShoe.java setzt `spinItemIconIndex = ShoeId`) wird damit überflüssig. Wer 1:1 bleiben will, braucht einen eigenen Renderer mit UV-Index und `EntityDataAccessor<Integer>`.
  - Id per `addAdditionalSaveData` speichern (Original verliert sie).
  - Valentinstag über `LocalDate` im Server-Setup statt statischem Feld.

### Skate - Skate (`skate`)

- **Rolle:** Kleiner feindlicher Wasser-Mob, Basis `EntityMob`. Atmet unter Wasser (Skate.java:71-73) und greift nur Spieler an.
- **Werte:**
  - XP 10 (Skate.java:35), `fireResistance` 3 (Skate.java:36), Tempo konstant 0.25 (Skate.java:28).
  - Rüstung `Skate_stats.defense` (Skate.java:75-77), Default 4 (manifest).
  - `getAttackStrength` = 4 ist toter Code; der Schaden kommt aus dem Attribut `Skate_stats.attack`, Default 8 (Skate.java:50, 87-90; manifest).
  - Keine Regeneration, keine Schadenssperre.
  - Austrocknen: außerhalb von Wasser mit 1/10 Scan (i 1..11, dy max **5**), Pfad Tempo 1.33, ohne Fund 1/25 -1 HP, bei 0 `setDead` ohne Drops (Skate.java:229-259).
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`, 1 `MyEntityAIWander`(1.0), 2 `EntityAIWatchClosest` Player 8.0, 3 `EntityAILookIdle`. Target: 1 `EntityAIHurtByTarget`(false) (Skate.java:39-43).
  - Zielsuche 1/8 pro AI-Tick, Box `expand(10,4,10)`, **nur Nicht-Creative-Spieler** (Skate.java:260-321).
  - Nahkampf bei `distSq < 4.0` mit `rand(4)==0 || rand(5)==1`; sonst Pfad Tempo 1.2. Kein `faceEntity` (Skate.java:263-271).
  - Schaden von anderen `Skate` wird verworfen (Skate.java:125-127). Vergeltung gegen `EntityLiving` mit Pfad 1.2 (Skate.java:128-136).
- **Interaktion:** keine eigene. Critter Cage fängt immer (EntityCage.java:634-636).
- **Drops:** `getDropItem` = `string` über Vanilla-`dropFewItems`: 0-2 Stück + rand(Looting+1) (Vanilla 1.7.10) (Skate.java:112-114). XP 10.
- **Spawnen:**
  - Kein Overworld-Spawn (manifest spawns leer).
  - `BiomeGenUtopianPlains` `spawnableWaterCreatureList` Gewicht 2, Gruppe 3-6 (BiomeGenUtopianPlains.java:178-180). Die Liste greift in allen Dimensionen mit dieser Biom-Klasse: Utopia (DimensionID, ChunkProviderOreSpawn.java:301-345 lässt waterCreature durch), VillageMania (ID3), Islands (ID4), Crystal (ID5) (WorldProviderOreSpawn*.java:15).
  - Research nennt nur "lakes in Crystal Dim."; laut Code ist der Spawn breiter.
  - `getCanSpawnHere`: `posY >= 50`, **Tag**, `rand(30)==1`, höchstens 6 `Skate` in `expand(16,8,16)` inklusive sich selbst (Skate.java:331-338).
  - Despawn Standard (Skate.java:58-60).
- **Zustand:** DataWatcher 20 = `attacking` (Skate.java:55, 323-329); keine NBT.
- **Sounds:** kein Living- und kein Hurt-Sound; Tod `orespawn:ratdead`, Lautstärke 0.33, Pitch 1.75 (Skate.java:92-110).
- **Config:** `SkateEnable` (1), `Skate_health` (8), `Skate_attack` (8), `Skate_defense` (4) (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Hitbox 0.75 x 0.25; Navigation im Wasser, Pfad-Malus für Wasser 0.
  - Spawn in eigenen Dimensionen über Biome-JSON (`spawners.water_creature`) der Utopia-, Village-, Islands- und Crystal-Biome. Utopia filtert nur Monster.
  - Der 1/30-Wurf plus Tageszeit gehört ins `SpawnPlacements`-Predicate.

### SpiderDriver - Spider Driver (`spider_driver`)

- **Rolle:** Feindliche Spinne, Basis `EntitySpider`. Sie steigt in den nächsten freien `SpiderRobot` und lenkt ihn auf Ziele zu.
- **Werte:**
  - Leben, Tempo, Schaden und Hitbox sind **nicht überschrieben**, also Vanilla-`EntitySpider` (Vanilla 1.7.10; manifest hat hier null). Research [NW]: HP 16, Angriff 3; unbelegt aus dem Code.
  - Rüstung 20 zu Fuß, 8 als Fahrer (SpiderDriver.java:83-88). Das deckt sich mit Research "Defense 20/8".
  - XP: Vanilla-`EntityMob`-Default (nicht gesetzt).
- **KI und Angriffe:**
  - Tasks (zusätzlich zu den leeren Vanilla-Spider-Tasks, weil `isAIEnabled()` true liefert; SpiderDriver.java:30-32): 1 `EntityAISwimming`, 2 `EntityAIPanic`(1.5), 3 `MyEntityAIWander`(0.65), 4 `EntityAILookIdle`. Target: 1 `EntityAIHurtByTarget`(false) (SpiderDriver.java:19-23).
  - **Es gibt keine Nahkampf-Task.** Das `attackEntity`-Override (16 Ticks Cooldown, 50 % Gift 60 Ticks; SpiderDriver.java:73-81) und `findPlayerToAttack` mit Radius 16 (SpiderDriver.java:34-37) hängen am alten `updateEntityActionState`-Pfad. Mit `isAIEnabled()==true` ruft 1.7.10 den nicht auf (Vanilla 1.7.10), der Code ist also vermutlich tot. offen: im Spiel verifizieren. Research nennt "a few + poison".
  - Zu Fuß (nicht PEACEFUL, 1/5 pro AI-Tick; SpiderDriver.java:44-55): sucht den nächsten `SpiderRobot` ohne Reiter in `expand(25,15,25)` (SpiderDriver.java:90-107). Innerhalb `(4 + w/2)^2` steigt er per `mountEntity` auf, sonst Pfad Tempo 0.55.
  - Als Reiter (1/4 pro AI-Tick; SpiderDriver.java:56-70): Zielsuche `expand(35,15,35)`, sortiert (SpiderDriver.java:148-165).
    - Filter: nicht PEACEFUL, lebend, nicht `isIgnoreable`, keine Spinnen (SpiderRobot, SpiderDriver, EntitySpider, EntityCaveSpider), sichtbar, Spieler ohne Creative.
    - Nicht-Spieler nur ab `distSq >= 36` (SpiderDriver.java:109-146).
    - Ab `distSq >= (11 + w/2)^2` ruft er `SpiderRobot.goThisWay(0.35*cos, 0.35*sin)` Richtung Ziel. Näher lenkt er nicht; den Angriff übernimmt der Roboter.
- **Interaktion:** keine eigene. Critter Cage fängt immer (EntityCage.java:75-77).
- **Drops:** Vanilla-`EntitySpider` (string, spider_eye; Vanilla 1.7.10), nicht überschrieben.
- **Spawnen:**
  - `BiomeGenUtopianPlains` `spawnableMonsterList` Gewicht 20, Gruppe 3-5 (BiomeGenUtopianPlains.java:211-213). Wirksam in VillageMania, Islands und Crystal; Utopia verwirft Monster (ChunkProviderOreSpawn.java:315-317).
  - Struktur `makeSpiderHangout` (GenericDungeon.java:7032-7084), nur in VillageMania (DimensionID3), Chance 1/350 pro Chunk (OreSpawnWorld.java:101-113, 1351-1370):
    - räumt einen Würfel 20 x 21 x 20 frei, Boden Stein plus Kies
    - 12 Spawner "Spider Driver": 4 Ecken x Höhe 1-3
    - ein "Robot Spider" wird in der Mitte (+10/+1/+10) gespawnt
  - `getCanSpawnHere`: true, wenn ein `SpiderRobot` in `expand(24,12,24)` ist, sonst Vanilla-Spider-Check (Licht) (SpiderDriver.java:167-171).
  - Despawn nur, wenn nicht persistent **und** nicht reitend (SpiderDriver.java:26-28).
- **Zustand:** Vanilla-`EntitySpider`-DataWatcher 16 (Kletter-Flag; Vanilla 1.7.10); keine eigenen Indizes, keine NBT.
- **Sounds:** Vanilla-Spinne.
- **Config:** `SpiderDriverEnable` (1) (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - `extends Spider`, Vanilla-Goals entfernen (`goalSelector.removeAllGoals`) und die OreSpawn-Liste setzen.
  - Entscheiden, ob der tote Nahkampf 1:1 fehlt oder als `MeleeAttackGoal` mit Gift ergänzt wird, und als bewusste Abweichung dokumentieren.
  - Aufsteigen: `startRiding(robot)`. Lenkung über eine öffentliche Methode am Roboter, die Motion setzt.
  - Rüstung: `getArmorValue()` überschreiben (20/8, unter der 30er-Grenze).
  - `mountEntity` in 1.21.1 respektiert `canAddPassenger`; der Roboter muss einen Passagier erlauben.

### SpiderRobot - Robot Spider (`robot_spider`)

- **Rolle:** Riesiger reitbarer Roboter, Basis `EntityLiving` (nicht Mob-Kategorie). Ohne Reiter ist er passiv (nur schauen); mit Reiter, egal ob Spieler oder `SpiderDriver`, greift er automatisch an.
- **Werte:**
  - Max-HP `SpiderRobot_stats.health`, Default **1500** (SpiderRobot.java:65; manifest). Das liegt über dem 1.21.1-Deckel von 1024 und braucht virtuelles Leben.
  - Angriff 100 (manifest), Rüstung 16 (SpiderRobot.java:75-77).
  - XP = health/2 = 750 bei Default (SpiderRobot.java:49).
  - Feuerimmun (SpiderRobot.java:48), löscht sich jeden Tick (SpiderRobot.java:618).
  - Ignoriert Schadensarten `inWall`, `cactus`, `inFire`, `onFire`, `magic`, `starve` (SpiderRobot.java:580-582). Kein Fallschaden (SpiderRobot.java:584-588).
  - Nicht schiebbar (SpiderRobot.java:561-563).
  - Despawnt nie (SpiderRobot.java:71-73); `func_110163_bv` = `enablePersistence` in `entityInit` (SpiderRobot.java:552; MCP methods.csv).
- **KI und Angriffe:**
  - Tasks nur ohne Reiter: 0 `EntityAIWatchClosest` Player 12.0, 1 `EntityAILookIdle` (SpiderRobot.java:46-47). Mit Reiter sind `updateAITasks`, `updateAITick` und `isAIEnabled` aus (SpiderRobot.java:79-94, 883-885).
  - Tritt-Angriff (serverseitig, Reiter vorhanden, nicht PEACEFUL, 1/40 pro Tick; SpiderRobot.java:619-621):
    - trifft **alle** passenden Ziele in `expand(20,8,20)` mit echter 3D-Distanz 12-18 Blöcke (SpiderRobot.java:932-989)
    - ausgenommen: Spinnen-Klassen und der eigene Reiter; Spieler nur ohne Creative
    - Schaden `attack/10`, Default 10; Knockback 0.6 / 0.1, gegen Spieler 0.2 (SpiderRobot.java:991-1006)
  - Kiefer-Angriff (Reiter, 1/15 pro Tick; SpiderRobot.java:622-634):
    - erstes passendes Ziel in `expand(20,12,20)`, **unsortiert** (SpiderRobot.java:1008-1024)
    - Filter: lebend, keine Spinnen, nicht der Reiter, nicht `isIgnoreable`, sichtbar
    - innerhalb `distSq < 36` gilt jedes Ziel, **auch Creative-Spieler**; sonst nur in einem Kegel von 0.75 rad vor dem Kopf, Spieler ohne Creative (SpiderRobot.java:1026-1077)
    - Treffer, wenn `distSq < (12 + w/2)^2`: Schaden `attack` (100), Knockback 1.2 / 0.15, gegen Spieler 0.3 (SpiderRobot.java:1079-1094)
  - Laut Research tritt er auch auf Pets; das deckt sich mit dem Code.
  - Bewegung serverseitig (SpiderRobot.java:649-876):
    - Clamps: `motionY` ±0.85, `motionX/Z` ±1.25.
    - Schwebehöhe mit Reiter: ist der Block bei posY-4.25 fest, `motionY += 0.06` und `posY += 0.03`, sonst `motionY -= 0.02`.
    - Ohne Reiter: Block bei posY-0.55 bzw. posY-1.55 fest ergibt +0.15, sonst -0.002.
    - Spieler als Reiter:
      - Hinderniswert je nicht-luftigem Block in einem Fächer (Tiefe k 1..dist, Radius 1..3*dist, ±90° in 30°-Schritten, dist = 3 + velocity*6) +0.03, dann `motionY` und `posY` += Wert*0.05.
      - Gier folgt dem Reiter mit Trägheitsfaktor `clamp(|1.85 - v|, 0.01, 0.9)`.
      - Vorwärts: +0.05 pro Tick bis max 0.45. Rückwärts: -0.05 bis max 0.25 (SpiderRobot.java:830-853).
      - Achtung: `moveEntity` läuft dann **zweimal pro Tick** (SpiderRobot.java:863, 868).
    - Dämpfung nach der Bewegung 0.8 / 0.98 / 0.8.
  - `SpiderDriver` steuert über `goThisWay(mx, mz)` (SpiderRobot.java:878-881).
- **Interaktion** (SpiderRobot.java:897-930):
  - `iron_ingot` in Reichweite `distSq < 25` heilt die fehlenden HP, höchstens 100 pro Barren; verbraucht 1 außer im Creative.
  - Reitet schon ein anderer Spieler: nichts.
  - Sonst ohne Reiter und `distSq < 16`: aufsteigen plus Sound.
  - Reiterposition: y-Offset 2.0 für `SpiderDriver`, sonst 2.625 + cos(rideTicker*0.19)*0.02 (SpiderRobot.java:565-570). Horizontal etwa 3 Blöcke hinter der Mitte, entgegen der Blickrichtung (±0.05 Wippen; SpiderRobot.java:572-578). `shouldRiderSit` false (SpiderRobot.java:530-532).
  - Absteigen: Vanilla-Sneak.
  - Clientsteuerung: Der Client schickt pro Tick `C05PacketPlayerLook` + `C0CPacketInput` über einen Cast auf `EntityClientPlayerMP` (SpiderRobot.java:709-713).
  - `ItemWrench` (Linksklick auf einen Roboter ohne Reiter) macht daraus ein `spiderrobotkit`: Item-Damage = fehlende HP, Partikel, `random.explode` (ItemWrench.java:19-24).
  - `ItemSpiderRobotKit` spawnt "Robot Spider" mit `health = maxDamage - damage` und übernimmt den Namen (ItemSpiderRobotKit.java:16-37).
  - Critter Cage: nicht vorgesehen.
- **Drops:** 14-27 Würfe `rand(15)` (SpiderRobot.java:1118-1165):
  - 0 `redstone`, 1 `repeater`, 2 `comparator`, 3 und 8 `redstone_block`, 4 `dispenser`, 5 `sticky_piston`, 6 `piston`, 7 `lever`, 9 `light_weighted_pressure_plate`
  - 10-14 nichts
  - `getDropItem` null. XP 750 (Default).
  - Kits kommen zusätzlich aus `GiantRobot` (GiantRobot.java:149) und aus Truhen (GenericDungeon.java:62, 70, 94 mit Gewicht 10).
- **Spawnen:** kein Biom-Spawn (manifest leer). Nur durch `makeSpiderHangout` (GenericDungeon.java:7079-7083), Kit oder Spawn-Ei `SpiderRobotEgg`.
- **Zustand:**
  - DataWatcher 20 = `attacking` (SpiderRobot.java:554, 1096-1102).
  - `writeEntityToNBT`/`readEntityFromNBT` sind **leer und rufen kein super auf** (SpiderRobot.java:887-891). Leben, Attribute und CustomName werden deshalb nicht gespeichert; nach dem Laden hat der Roboter volle HP.
  - `RenderSpiderRobotInfo` hält die Bein-IK für 8 Beine: `legoff`, `ymid`, `yrange`, `pairedwith`, `yoff` je Bein (SpiderRobot.java:96-184). Sie wird nur clientseitig in `updateLegs` berechnet (SpiderRobot.java:230-419, `findNewFooting` 421-528).
  - Nebenwirkung der IK: Landet ein Fuß, der Roboter hat einen Reiter und `mobGriefing` ist an, wird `tallgrass` am Fuß zu Luft und `grass` darunter zu `dirt` (SpiderRobot.java:407-417). Das passiert **nur in der Client-Welt**, also ein Geisterblock.
- **Sounds:**
  - `orespawn:robotspider`, Lautstärke 0.45, mit Reiter per 1/80-Wurf, danach 125 Ticks Pause (SpiderRobot.java:687-693).
  - `orespawn:robotspidermount`, Lautstärke 0.65 beim Aufsteigen (SpiderRobot.java:927).
- **Partikel:** `flame` 1/8, `smoke` 1/2, `fireworksSpark` 1/10, jeweils 8 Blöcke hinter der Mitte auf y+2 (SpiderRobot.java:635-646).
- **Config:** `SpiderRobot_health` (1500), `SpiderRobot_attack` (100), `SpiderRobot_defense` (16) (manifest), `PlayNicely`. Kein Enable-Schalter; der Hangout hängt an `SpiderDriverEnable`.
- **Portierung 1.21.1:**
  - `Mob` (oder `PathfinderMob`) mit `getControllingPassenger()`, `tickRidden()` und `getRiddenInput()`. Die Eingaben kommen in 1.21.1 serverseitig als `player.zza`/`xxa`, die Client-Pakete fallen weg. Der `EntityClientPlayerMP`-Cast wäre auf einem dedizierten Server ein Klassenladefehler.
  - HP 1500 als virtuelles Leben: `MAX_HEALTH` auf 1024 klemmen und eingehenden Schaden mit 1024/1500 skalieren.
  - Rüstung 16 passt unter 30.
  - Schadensimmunitäten über `DamageTypeTags` bzw. `damageSources().inWall()` etc.; `fireImmune()` im EntityType-Builder.
  - Ride-Physik und Doppel-`move` 1:1 nachbauen, dann Fahrgefühl im Spiel prüfen.
  - Die IK ist rein kosmetisch und gehört nach `com.swbr.orespawn.client`, z. B. in einen Render-State im Renderer oder eine clientseitige Map. Den Gras-zu-Erde-Effekt serverseitig vereinfacht nachbauen oder verwerfen (Original wirkt nur beim Client).
  - NBT: bewusst entscheiden, ob Leben gespeichert wird; 1:1 heißt nein, das ist aber ein Bug.
  - Tracking-Range 128 im EntityType (`clientTrackingRange(8)` = 128 Blöcke). `getUpdateFrequency` 10 (SpiderRobot.java:538-540) wird von FML nicht genutzt; registriert ist 1 (OreSpawnMain.java:4145).

### SpitBug - Spit Bug (`spit_bug`)

- **Rolle:** Feindlicher Käfer, Basis `EntityMob`, springt und spuckt Säure. Handlanger von `TrooperBug` (Jumpy Bug).
- **Werte:**
  - XP 50 (SpitBug.java:36), `fireResistance` 75 (SpitBug.java:37), Tempo 0.33 (SpitBug.java:32).
  - Rüstung `SpitBug_stats.defense`, Default 12 (manifest).
  - Schadenssperre **15 Ticks**: währenddessen liefert `attackEntityFrom` false, inklusive Vergeltung (SpitBug.java:218-223). Immun gegen `cactus` und `fall` (SpitBug.java:221).
  - Regeneration 1/150 pro AI-Tick +1 HP (SpitBug.java:278-280).
  - Sprung (`jump`): `motionY` und `posY` je +0.75, Vorwärtsimpuls 0.2-0.65 entlang `rotationYawHead` (SpitBug.java:116-123).
  - `jumpAtEntity`: +0.75, Impuls 0.2-0.45 zum Ziel (SpitBug.java:125-133). In der Luft wird der Pfad gelöscht (SpitBug.java:80-82).
  - `force_sync` ist unbenutzt.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`, 1 `EntityAIMoveThroughVillage`(0.9, false), 2 `MyEntityAIWanderALot`(14, 1.0), 3 `EntityAIWatchClosest` Player 10.0, 4 `EntityAILookIdle`. Target: 1 `EntityAIHurtByTarget` (SpitBug.java:41-46). Meidet Wasser (SpitBug.java:35).
  - Zielsuche 1/5 pro AI-Tick: ein lebendes Attack-Target, sonst sortiert in `expand(12,7,12)` (SpitBug.java:244-252, 354-371).
  - Zielfilter (SpitBug.java:307-352): alles Lebende und Sichtbare außer `isIgnoreable`, `EnderReaper`, `EnderKnight`, `EntityEnderman`, `Hydrolisc`, `EntityCreeper`, `SpitBug`, `TrooperBug` und Creative-Spielern. Also auch Tiere und Dorfbewohner.
  - Pro Ziel-Tick in dieser Reihenfolge (SpitBug.java:255-272):
    1. 1/15 und am Boden: `jumpAtEntity`.
    2. Sonst `distSq < 9`: `attacking=1`, Nahkampf mit `rand(6)==0 || rand(7)==1`. Schaden = Attribut 10 (manifest), Knockback 0.5 / 0.1, gegen Spieler 0.2 (SpitBug.java:199-214). Dazu 2/3 `orespawn:clatter` am Ziel.
    3. Sonst am Boden: Pfad Tempo 0.5 plus `watercanon`.
  - `watercanon` (SpitBug.java:283-305): Ist `stream_count > 0`, spawnt ein `Acid` auf y+1.5, 1.5 Blöcke vor dem Kopf (x nutzt `rotationYawHead`, z nutzt `rotationYaw`, Mischfehler im Original). Wurf-Heading Tempo 1.1, Streuung 6.0, Sound `random.bow` 0.75, `stream_count--`. Ist er leer: mit 1/7 Nachladen auf **8** Schuss.
  - `Acid` extends `LaserBall` mit `is_acid=1` (Acid.java):
    - Treffer 16 Schaden, Quelle `causeThrownDamage` (LaserBall.java:97, 153), plus `setFire(1)` = 1 s Brand (LaserBall.java:154-156)
    - trifft `TrooperBug` und `SpitBug` nicht (LaserBall.java:103-111)
    - Lebensdauer 200 Ticks (LaserBall.java:184-187); keine Explosion
  - Research "acid sets you on fire" ist damit bestätigt (1 s).
- **Interaktion:** keine (SpitBug.java:195-197). Critter Cage: 30 % Fehlschlag, sonst `CagedSpit` (EntityCage.java:512-521).
- **Drops:** 1-3 `amethyst`, verstreut ±2 (SpitBug.java:176-190). `getDropItem` (10 % je `gold_nugget`, `uranium_nugget`, `titanium_nugget`; SpitBug.java:162-174) ist toter Code, weil `dropFewItems` überschrieben ist. XP 50.
- **Spawnen:**
  - Overworld **`ambient`** swampland Gewicht 6, 1-2 (manifest; OreSpawnMain.java:4551).
  - `BiomeGenUtopianPlains` Monster Gewicht 2, 1-3 (BiomeGenUtopianPlains.java:409-411), wirksam außer in Utopia.
  - `makeSpitBugLair` mit 3 Spawnern "Spit Bug" (GenericDungeon.java:2660-2692). Overworld nur im Biom "Swampland", 1/190 pro Chunk-Versuch (OreSpawnWorld.java:1266-1278, 297).
  - `TrooperBug` ruft im Kampf mit 1/30 einen Spit Bug zwischen sich und das Ziel (TrooperBug.java:426-427).
  - `getCanSpawnHere` (SpitBug.java:381-414): Spawner "Spit Bug" nahe → true. Am Tag nur, wenn `rand(20) <= 1` (10 %). Sonst `isValidLightLevel` plus Luft im Bereich x/z -2..+1, y +1..+3.
  - Despawn Standard.
- **Zustand:** DataWatcher 20 = `attacking` (SpitBug.java:51, 373-379). `RenderInfo` nur clientseitig; keine NBT.
- **Sounds:** Living `orespawn:clatter` (1/4), Hurt `orespawn:crunch`, Tod `orespawn:emperorscorpion_death`, Lautstärke 0.75, Pitch 1.5 (SpitBug.java:139-160). Beim Treffer `clatter` am Ziel, `random.bow` beim Spucken.
- **Config:** `SpitBugEnable` (1), `SpitBug_health` (100), `SpitBug_attack` (10), `SpitBug_defense` (12) (manifest), `PlayNicely`.
- **Portierung 1.21.1:**
  - Hitbox 2x2. Säure als eigenes `ThrowableProjectile` bzw. als `LaserBall`-Variante mit Flag.
  - `jumpFromGround()` überschreiben (Vanilla-Sprunghöhe ersetzen).
  - `MoveThroughVillageGoal` braucht in 1.21.1 `GroundPathNavigation` mit Tür-Öffnen-Flag.
  - Pitfall Spawn-Kategorie: Der Original-Spawn ist `ambient`. Als `MobCategory.AMBIENT` würde er in PEACEFUL spawnen und sofort despawnen. Empfehlung `MONSTER` mit Gewicht 6 in `#minecraft:is_swamp` per BiomeModifier, als Abweichung notieren.
  - `hurt`-Sperre wie bei SeaMonster.

### Spyro - Baby Dragon (`baby_dragon`)

- **Rolle:** Zähmbares Haustier, Basis `EntityTameable`. Es jagt feindliche Mobs, fliegt und schießt Feuerbälle und wächst nach Zufall oder per Diamant zum `Dragon`.
- **Werte:**
  - Leben fest 200, nicht per Config (Spyro.java:199-201).
  - Attribut Angriff 5.0 (Spyro.java:65), der echte Nahkampfschaden kommt aber aus `getAttackStrength` = **4.0** (Spyro.java:415-423).
  - Rüstung 5 (Spyro.java:348-350), XP 35 (Spyro.java:57).
  - Feuerimmun, `fireResistance` 1000 (Spyro.java:43-44). Atmet unter Wasser (Spyro.java:195-197), bekommt im Wasser Auftrieb `motionY += 0.07` (Spyro.java:440-442).
  - Kein Fallschaden (Spyro.java:397-401). Nur Kaktusschaden wird ignoriert (Spyro.java:425-431).
  - Regeneration 1/100 pro AI-Tick +1 HP (Spyro.java:558-560). In Lava zusätzlich +1 beim Lava-Scan (Spyro.java:526-529).
  - Wachstum: serverseitig pro Tick mit 1/100000, wenn nicht persistent → `Dragon`; zahm bleibt zahm, der Besitzer wird übernommen (Spyro.java:449-461). Abgeleitet: Mittelwert etwa 83 Minuten Spielzeit.
- **KI und Angriffe:**
  - Tasks (Spyro.java:47-55):

    | Prio | Task | Parameter |
    |---|---|---|
    | 1 | `EntityAISwimming` | - |
    | 2 | `EntityAIAvoidEntity` | EntityMob, 8.0, 0.3, 0.4 |
    | 3 | `MyEntityAIFollowOwner` | Tempo 1.15, maxDist 12, minDist 2 |
    | 4 | `EntityAITempt` | beef, 1.25 |
    | 5 | `EntityAIPanic` | 1.5 |
    | 6 | `EntityAIWatchClosest` | Player 6.0 |
    | 7 | `MyEntityAIWander` | 0.75 |
    | 8 | `EntityAILookIdle` | - |
    | 9 | `EntityAIMoveIndoors` | - |

    Keine Target-Tasks. Meidet Wasser.
  - Aktivitäts-Zustand `activity` (DW 21): 1 = am Boden mit Vanilla-KI, 2 = Flug über `do_movement`, 0 wird zu 1. 3 wird nur abgefragt (Spyro.java:629), nie gesetzt.
  - `updateAITick` (Spyro.java:487-551), serverseitig, nicht sitzend:
    - `super.updateAITick` nur bei activity 1; mit 1/200 Revenge-Target zurücksetzen
    - mit 1/20 Lava-Scan (i 1..10, dy max 4, ab i>=6 in 2er-Schritten), bei Fund Pfad dorthin (Tempo 1.0) und activity 1
    - mit 1/100 ohne Ziel in Sicht: activity 1, davon 1/8 → 2
    - fliegt der Besitzer (`capabilities.isFlying`): `owner_flying=1`, activity 2
  - `onUpdate` (Spyro.java:437-481):
    - activity 2 dämpft `motionY`: unter Ziel+2 mal 0.7, sonst mal 0.5. Der Zweig mal 0.61 ist unerreichbar.
    - zahm, activity 1 und Besitzer weiter als 16 Blöcke (`distSq > 256`): activity 2
  - `do_movement` (Spyro.java:563-687), nur wenn nicht sitzend und activity != 1:
    - Neues Flugziel mit 1/300 bei activity 2, oder wenn der Besitzer weiter als 10 Blöcke ist (fliegt er: weiter als 6).
    - Jagd mit 1/6 (nicht PEACEFUL): Ziel in `expand(12,6,12)`, sortiert; nur `EntityMob` oder `Mothra`, nicht `Spyro`, sichtbar per `canSee` **und** freie Block-Sichtlinie ab y+0.75 (Spyro.java:689-710).
      - Zahm und HP < 25 %: Flucht, Flugziel gespiegelt vom Ziel weg (Spyro.java:602-607).
      - Sonst Flugziel auf dem Ziel, Pfad 1.25. Nahkampf 4.0 bei `distSq < (3 + w/2)^2`.
      - Sonst bei `distSq < 64` und nicht im Wasser: `EntitySmallFireball` ab y+1.25 mit `random.bow`, wenn `(SpyroFire==1 && rand(10)==0) || rand(15)==1`. **Auch mit gelöschtem Feuer schießt er also mit 1/15** (Spyro.java:617-622).
    - Flugzielwahl, bis zu 50 Versuche (Spyro.java:632-666):
      - mit Besitzer: um den Besitzer ±6..9; fliegt der Besitzer: ±0..5
      - ohne Besitzer: ±6..10 um sich selbst
      - y-Offset `rand(9 + 2*owner_flying) - 4`; der Zielblock muss Luft und sichtbar sein
    - Steuerung: `motionXZ += (sign*0.5 - m)*0.15*sf`, `motionY += (sign*0.7 - m)*0.21*sf`. Faktor sf 0.5, bei fliegendem Besitzer 1.75, bei mehr als 7 Blöcken Abstand zum Besitzer 3.5. `moveForward = 0.75*sf`, Gier += Differenz/3 (Spyro.java:667-686).
- **Interaktion** (Spyro.java:207-338), jeweils `distSq < 16`; Items werden außer im Creative verbraucht:

  | Item | Bedingung | Wirkung |
  |---|---|---|
  | `beef` | wild | 50 % zähmen: Besitzer-UUID, volle HP, Herzen (State 7); sonst Rauch (State 6) |
  | `beef` | zahm, Besitzer | volle HP |
  | `beef` | zahm, nicht Besitzer | keine Wirkung, Beef wird trotzdem verbraucht |
  | `deadbush` | Besitzer | Zähmung aufheben, HP voll, Besitzer "" |
  | `ice` | Besitzer | `SpyroFire=0`, Chat "Baby Dragon fireballs extinguished." |
  | `flint_and_steel` | Besitzer | `SpyroFire=1`, Chat "Baby Dragon fireballs lit!"; das ganze Feuerzeug wird verbraucht (stackSize--) |
  | `diamond` | Besitzer, server | wird zu `Dragon` (zahm, Besitzer übernommen), `setDead`; Diamant verbraucht |
  | `name_tag` | Besitzer | Name setzen |
  | sonst | Besitzer | Sitzen umschalten |
  | sonst | nicht Besitzer | `super.interact` |

  - Rückweg: Der `Dragon` wird per Diamant wieder zum "Baby Dragon" (Dragon.java:1346-1356).
  - Keine Zucht: `createChild` liefert null (Spyro.java:411-413), `isWheat` ist eine Legacy-Methode ohne Override-Wirkung.
  - Critter Cage fängt immer (EntityCage.java:399-401).
- **Drops:** nur zahm 1-4 `beef` (Spyro.java:378-387); wild nichts. `getDropItem` `beef` ist toter Code. XP 35.
- **Spawnen:**
  - `ChunkProviderOreSpawn2` (Dimension-Extreme, DimensionID2) Monster-Liste Gewicht 5, 1-2 (ChunkProviderOreSpawn2.java:381-383). Research: "Red Ant Mining Dimension"; die Zuordnung Extreme = Mining ist offen und muss gegen den Dimensions-Batch geprüft werden.
  - Ancient-Dried-Spawnblock `MySpyroSpawnBlock` in der Erzgenerierung (ChunkOreGenerator.java:234, OreSpawnWorld.java:561). Spawn-Ei in Truhen (GenericDungeon.java:94-95).
  - `getCanSpawnHere`: Tag und `posY >= 50` (Spyro.java:407-409).
  - Despawn: nicht persistent und nicht zahm (Spyro.java:344-346).
- **Zustand:**
  - DataWatcher 21 = `activity`, 20 = `SpyroFire` (Default 1) (Spyro.java:71-72). Vanilla-`EntityTameable` 16 (Sitz- und Zahm-Flags), 17 (Besitzer; Vanilla 1.7.10).
  - NBT `SpyroActivity`, `SpyroFire` (Spyro.java:77-88).
  - SRG-Namen, deren Bedeutung aus dem Gebrauch abgeleitet ist (nicht in MCP stable-12): `func_152115_b` = Besitzer setzen, `func_152114_e` = ist Besitzer, `func_152113_b` = Besitzer-String.
- **Sounds:** Living `orespawn:roar` nur nicht sitzend und bei activity 2; Hurt `orespawn:duck_hurt`; Tod `orespawn:cryo_death`; Lautstärke 0.4; Pitch 1.0 ±0.1, als Kind 1.5 ±0.1 (Spyro.java:352-391). `splash` in Lava, `random.bow` beim Feuerball.
- **Config:** `SpyroEnable` (1) (manifest), `PlayNicely`. Keine Stats-Config.
- **Portierung 1.21.1:**
  - `TamableAnimal`, Sitzen über `setOrderedToSit`.
  - Flugmodus mit `FlyingMoveControl`-Umschaltung oder, 1:1, eigene Motion-Steuerung im `tick()` bei activity 2. Dazu `setNoGravity` bzw. manuelles Dämpfen.
  - Besitzer-Flug: `player.getAbilities().flying`.
  - Feuerball: `SmallFireball(level, this, Vec3)`.
  - Wachstum und Diamant: `EntityType.create` für `orespawn:dragon`, Besitzer per `setOwnerUUID`.
  - Chattexte als Übersetzungsschlüssel (Englisch beibehalten).
  - Lava-Scan ist teuer (bis 21³ Blöcke). 1:1 übernehmen, aber nur serverseitig und auf `BlockPos.MutableBlockPos`.
  - Leben 200 und Rüstung 5 liegen unter den Grenzen.

### StinkBug - Stink Bug (`stink_bug`)

- **Rolle:** Passives Kleintier, Basis `EntityAnimal`. Züchtbar; beim Tod verpasst es der Umgebung Nausea.
- **Werte:**
  - Leben fest 5 (Spyro-artig hart codiert; StinkBug.java:93-95), Angriff 0 (StinkBug.java:42), Tempo 0.15 (StinkBug.java:22).
  - XP 2 (StinkBug.java:26), `fireResistance` 10 (StinkBug.java:24). Ertrinkt normal (StinkBug.java:89-91).
  - Nausea beim Tod (StinkBug.java:68-87): Sobald nach einem Treffer HP <= 0, bekommt **jede** `EntityLivingBase` in der Box x ±8, y -5..+10, z ±8 `Potion.confusion` für 300 Ticks, Stufe 0. Das schließt Spieler, andere Mobs und sich selbst ein.
- **KI und Angriffe:**
  - Tasks: 0 `EntityAISwimming`, 1 `EntityAIMate`(1.0), 4 `EntityAIPanic`(1.5), 5 `EntityAIAvoidEntity` Player (4.0, 1.0, 1.4), 6 `EntityAIWatchClosest` Player 6.0, 8 `MyEntityAIWanderALot`(10, 1.0), 9 `EntityAILookIdle`, 10 `EntityAIMoveIndoors` (StinkBug.java:27-34). Meidet Wasser.
  - Kein Angriff. Mit 1/200 Revenge-Target zurücksetzen (StinkBug.java:54-62).
- **Interaktion:**
  - Zucht mit `crystalapple` (`isBreedingItem`; StinkBug.java:156-158). `isWheat` = `fish` ist Legacy ohne Wirkung (StinkBug.java:152-154).
  - Kind = neuer `StinkBug` (StinkBug.java:144-150); Kinder werden persistent (StinkBug.java:136-142).
  - Critter Cage fängt immer (EntityCage.java:523-526).
- **Drops:** `deadstinkbug` über Vanilla-`dropFewItems`: 0-2 + Looting (Vanilla 1.7.10) (StinkBug.java:113-115). XP 2. Das Item geht in Kraken Repellent und Poison Sword (Research 01-mobs.md).
- **Spawnen:**
  - Overworld `ambient`: forest 10 (2-4), jungle 8 (2-4), forestHills 6 (2-4), jungleHills 4 (2-4), savanna 8 (2-5) (manifest; OreSpawnMain.java:4554-4558).
  - `BiomeGenUtopianPlains` `spawnableCaveCreatureList` (= ambient) Gewicht 3, 2-4 (BiomeGenUtopianPlains.java:308-310), in allen vier Utopian-Dimensionen.
  - Stinky House mit Spawner "Stink Bug" (GenericDungeon.java:5350, 5402).
  - `getCanSpawnHere`: Spawner "Stink Bug" nahe → true, sonst nur `posY >= 50`. Kein Licht- und kein Grascheck (StinkBug.java:117-134).
  - Despawn: erwachsen und nicht persistent (StinkBug.java:136-142).
- **Zustand:** keine eigenen DataWatcher, Vanilla-`EntityAgeable` 12 (Alter; Vanilla 1.7.10). Keine eigenen NBT.
- **Sounds:** nur Tod `orespawn:fart` (9 Varianten, sounds_dump), Lautstärke 1.0 (StinkBug.java:97-111).
- **Config:** `StinkBugEnable` (1) (manifest).
- **Portierung 1.21.1:**
  - `Animal` mit `isFood` = `crystalapple`.
  - Nausea in `die(DamageSource)` statt in `hurt`. Das Original kann bei mehreren Treffern im Todesframe mehrfach auslösen; in `die` ist es genau einmal, als Vereinfachung notieren.
  - `MobEffects.CONFUSION`, 300 Ticks.
  - Spawn als `MobCategory.AMBIENT` möglich. Achtung: Vanilla-`AMBIENT` spawnt in 1.21.1 nur Fledermaus-artig mit Cap 15; alternativ `CREATURE`.
  - Kind-Skalierung im Renderer (manifest `gl_scale` scale/2).
