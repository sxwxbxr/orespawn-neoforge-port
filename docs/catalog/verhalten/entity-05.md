# Verhalten: entity-05

Dieser Stapel umfasst ein Projektil und sieben Kreaturen. `BetterFireball` ist das Feuerball-Projektil, das Brutalfly, Dragon, Godzilla, Mothra und die Royals verschießen. Im Original ist es nicht als Entity registriert. `Boyfriend` ist der zähmbare Begleiter mit Ausrüstung, Fern- und Nahkampf. Er ist ab Werk abgeschaltet. `Camarasaurus` ist ein zähmbarer Pflanzenfresser aus der Mining-Dimension, `Cassowary` ein scheuer Laufvogel. `CaterKiller` ist ein 450-HP-Waldboss, der Bäume frisst und Netze legt. Bleibt er zu lange verletzt, verwandelt er sich in die `Brutalfly`. Die Brutalfly ist ein fliegender Feuerball-Werfer mit eigener Flugsteuerung ohne AI-Tasks. `CaveFisher` ist ein kleiner Höhlenjäger, `Cephadrome` ein reitbarer 300-HP-Flugdrache ohne Zähmung. Drei Befunde sind für die Portierung besonders wichtig. Erstens läuft bei `BetterFireball` jeder Tick doppelt, weil geerbte Felder überdeckt werden. Zweitens setzt Cephadrome `wasfed` bei Standard-Config jeden Tick auf 1, sodass die Hunger-Mechanik nicht greift. Drittens ist die Flug-Taste der Reittiere ein globaler statischer Serverwert.

Legende: `(Datei.java:ZEILE)` verweist auf `reference/src-20.2/src/main/java/danger/orespawn/`. Bei fehlendem Dateinamen gilt die Klasse des Abschnitts. `(manifest)` steht für `docs/catalog/manifest.json`. SRG-Namen sind mit `reference/jar/mcp/joined.srg` bzw. `methods.csv` übersetzt: `func_152115_b` = `EntityTameable.setOwner(String)`, `func_152114_e` = `isOwner(EntityLivingBase)`, `func_110163_bv` = `enablePersistence`, `func_145881_a` = `TileEntityMobSpawner.getSpawnerBaseLogic`, `func_150905_g` = `ItemFood.getHealAmount`.

---

### BetterFireball - (kein Lang-Name) (`offen: nicht registriert`)

- **Rolle:** Projektil, `extends EntityFireball` (:13). Das Manifest hat keinen Eintrag. Weder `OreSpawnMain` noch `ClientProxyOreSpawn` registrieren die Klasse (grep ohne Treffer). Verwendet von Brutalfly, Dragon, Godzilla, Mothra, TheKing, ThePrince, ThePrinceAdult, ThePrincess, ThePrinceTeen und TheQueen (grep `new BetterFireball`).
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | Hitbox normal | 1.0 x 1.0 | (:41) |
  | Hitbox `setSmall()` | 0.3125 x 0.3125 | (:87) |
  | Beschleunigung | normierter Richtungsvektor x 0.1 | (:67-70) |
  | Explosionsstärke `field_92012_e` (SRG-Name, Bedeutung aus Nutzung) | 1 Standard / 2 `setBig()` / 4 `setReallyBig()` | (:38, :78, :82) |
  | Lebensdauer | `ticksAlive >= 600` oder `ticksInAir >= 600` führt zu `setDead` | (:103) |
  | Treffer-Schaden | 10, klein 5, jeweils `setFire(5)` | (:237-242) |
  | Große Ziele | bei `width*height > 30`: `setHealth(health/2)` *vor* dem Schaden; ausgenommen Royalty, Godzilla, GodzillaHead, PitchBlack, Kraken | (:232-233) |
  | Suchbox Kollision | `boundingBox.addCoord(motion).expand(1,1,1)`, Treffbox des Ziels `expand(0.3)` | (:135, :98, :160) |
  | Eigentreffer | erst ab `ticksInAir >= 25` | (:159) |
  | Wasser | Bewegungsfaktor 0.8 und 4 `bubble`-Partikel | (:198-204) |
  | Motion-Faktor sonst | `getMotionFactor()` = 0.95 (Vanilla, javap `ze.e()` in `client-1.7.10.jar`) | (:197) |

- **KI und Angriffe:**
  - `onUpdate`: Das Projektil stirbt, wenn der Schütze tot ist oder der Block nicht geladen ist (:107-109). Es setzt sich selbst jeden Tick in Brand (:112). Blockstrahl und Entity-Suche wie beim Vanilla-Feuerball (:126-173). Rauchpartikel (:211).
  - Filter in der eigenen Kollisionsschleife (:139-158): Schütze, andere `BetterFireball`, `GodzillaHead` und Royalty (`MyUtils.isRoyalty`) brechen die **gesamte** Schleife ab und löschen auch einen gefundenen Blocktreffer (`var17 = null; break`). Mit `setNotMe()` gilt das zusätzlich für `EntityPlayer`, `Dragon` und `Mothra`. Das Ergebnis hängt von der Listenreihenfolge ab.
  - `onImpact` (:216-284), nur serverseitig:
    - Trifft der Ball einen `BetterFireball` oder `Mothra`, kehrt die Methode sofort zurück, **ohne** `setDead` (:219-224).
    - `notme` und ein Treffer auf `Dragon` oder Spieler: `setDead` ohne Schaden (:225-228).
    - Blocktreffer: Feuer auf den angrenzenden Luftblock der getroffenen Seite (:246-277).
    - Nicht klein: `newExplosion(null, pos, stärke, flaming=true, smoking=mobGriefing)` (:279-281). Danach `setDead`.
  - **Doppelter Tick (Portierungsfalle):** `BetterFireball` deklariert `shootingEntity`, `ticksAlive`, `ticksInAir` und `accelerationX/Y/Z` neu (:15-25). Damit überdeckt es die Felder von `EntityFireball`. Der Konstruktor ruft nur `super(World)` (:48), die Felder der Oberklasse bleiben also 0 bzw. null. `super.onUpdate()` (:111) führt trotzdem den kompletten Vanilla-Tick aus. Das zeigt javap `ze.h()`: `putfield s/t/u` (Position), `bipush 25`, `0.3f`.
    - Folge 1: zwei Positionsschritte je Tick. Der Vanilla-Teil beschleunigt mit 0, der eigene mit 0.1, beide dämpfen mit 0.95.
    - Folge 2: eine zweite Kollisionsprüfung ohne die Filter aus :139-158. `super.shootingEntity == null`, deshalb kann auch der eigene Schütze getroffen werden. Bei Brutalfly liegt die Mündung 2.25 vor der Mitte (Brutalfly.java:362) innerhalb der 5.0 breiten Hitbox (manifest). Ob das im Spiel sichtbar war: offen, nur aus Bytecode abgeleitet. Das frühe `return` für Mothra in `onImpact` spricht dafür, dass es auffiel.
  - Client-Sicht: Die Klasse ist nicht registriert. `EntityTrackerEntry` sendet für jede `EntityFireball`-Unterklasse Spawn-Pakettyp 63 (javap `my`: `instanceof ze` gefolgt von `bipush 63`). Der Client sieht also einen **vanilla `EntityLargeFireball`**. `setSmall` wird nie synchronisiert (abgeleitet).
- **Interaktion:** keine.
- **Drops:** keine.
- **Spawnen:** nur durch die Schützen-Klassen.
- **Zustand:** keine DataWatcher-Einträge (`entityInit` leer, :44-45). NBT: `ExplosionPower` (:286-296). `small` und `notme` werden nicht gespeichert.
- **Sounds:** keine eigenen. Die Schützen spielen `random.fuse` (z. B. Brutalfly.java:386).
- **Config:** Gamerule `mobGriefing` (:280). Keine OreSpawn-Keys.
- **Portierung 1.21.1:**
  - Basis: `AbstractHurtingProjectile`. Die Filter aus :139-158 gehören in `canHitEntity`, `onHitEntity`/`onHitBlock` ersetzen `onImpact`. Die Explosion läuft über `level.explode(null, x, y, z, power, true, mobGriefing ? ExplosionInteraction.MOB : NONE)`. Die Doku von `ExplosionInteraction` gegen `smoking=false` prüfen: offen.
  - Ein `EntityType` wird zwingend gebraucht. Die Registry-ID fehlt im Manifest und muss festgelegt werden. Renderer: Vanilla-`ThrownItemRenderer` mit Fire Charge wie der Large Fireball. `small` per `SynchedEntityData` synchronisieren, falls die Größe sichtbar sein soll. Das weicht vom Original ab.
  - Doppelten Tick nachbilden: zwei Bewegungsschritte je Tick. Sonst fliegen alle Boss-Feuerbälle halb so schnell. Die zweite, ungefilterte Kollisionsprüfung bewusst entscheiden und dokumentieren.
  - „Halbiere HP bei großen Zielen": `setHealth(getHealth()/2)` umgeht Rüstung und Virtual-Health-Skalierung. Bei Zielen mit virtueller Gesundheit auf die virtuelle Größe anwenden.

---

### Boyfriend - Boyfriend (`boyfriend`)

- **Rolle:** zähmbarer Begleiter, `extends EntityTameable implements IRangedAttackMob` (:19). Trägt Waffe und Rüstung. Wilde Exemplare sind passiv, weil alle Target-Tasks Zähmung verlangen (MyEntityAINearestAttackableTarget.java: `shouldExecute` bricht bei `!isTamed()` ab). Ab Werk deaktiviert (`BoyfriendEnable` = 0, manifest).
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 0 | (:128) |
  | Rüstung | Summe `damageReduceAmount` getragener `ItemArmor`, geklemmt auf 8..23 | (:158-173) |
  | Schadensdeckel | eingehender Schaden je Treffer max. 10; `cactus` ignoriert | (:978-988) |
  | Feuer | `isImmuneToFire`, `fireResistance` 100 | (:102-103) |
  | Fallschaden | `ceil(fall-3)`, max. 3 | (:462-474) |
  | Regeneration | +1 HP alle 150 Ticks (erster Zyklus nach 200), wenn HP < 80 | (:88, :489-495) |
  | Nass-Zustand | `wet_count` = 500 Ticks nach Wasser oder Lava | (:483-487) |
  | Sync-Intervall | alle 20 Ticks (erster nach 50) | (:496-510) |
  | Nahkampf-Cooldown | `attackTime` = 25 Ticks | (:236) |
  | Nahkampf-Reichweite | < 4.0, mit `MyBertha` (`berthasmall`) < 10.0 | (:233) |
  | Verfolgen und Sticheln | < 7.0, nicht mit `MyUltimateBow`; Pfad-Speed 1.25 | (:249-258) |
  | Skins | trocken 0..27, nass 0..17, Stimme 0..9 | (:98-100) |
  | Tonhöhe | `(voice-5)*0.02 + 1.0` | (:966-968) |
  | Lautstärke | 0.3 | (:836) |
  | Atmung | `canBreatheUnderwater` = true | (:458) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 1 | `MyEntityAIFollowOwner` | Speed 1.4, maxDist 12, minDist 1.5. Start bei Abstand >= 12, oder bei (Y < 60 bzw. Nacht) und Abstand > 6. Teleport, wenn kein Pfad und Abstand² >= 144 (MyEntityAIFollowOwner.java:38, :77-79) | (:106) |
  | 2 | `EntityAITempt` | 1.25, `cooked_beef`, scaredByPlayerMovement=false | (:107) |
  | 4 | `EntityAIArrowAttack` | Speed 1.25, Intervall 20 Ticks, Reichweite 10.0 | (:108) |
  | 5 | `EntityAISwimming` | | (:109) |
  | 6 | `EntityAIPanic` | 1.5 | (:110) |
  | 7 | `EntityAIWatchClosest` | Player, 6.0 | (:111) |
  | 8 | `MyEntityAIWander` | 0.75; Chance 1/90, Radius 10/7 (MyEntityAIWander.java:23-28) | (:112) |
  | 9 | `EntityAILookIdle` | | (:113) |
  | 10 | `EntityAIOpenDoor` | true | (:114) |
  | 11 | `EntityAIMoveIndoors` | | (:115) |
  | T2 | `MyEntityAINearestAttackableTarget` | `EntityCreeper`, 20.0, Chance 0 = immer, Sicht, nearbyOnly, `IMob.mobSelector`; nur bei `PlayNicely == 0` | (:116-118) |
  | T3 | `MyEntityAINearestAttackableTarget` | `EntityLiving`, 15.0, `IMob.mobSelector`; nur bei `PlayNicely == 0` | (:119-121) |
  | T4 | `MyEntityAIJealousy` | `Boyfriend`, 6.0, targetChance 5: Abbruch bei `rand(100) > 5`, also 6 % je Prüfung (MyEntityAINearestAttackableTarget.java: `shouldExecute`); greift **ungezähmte** Boyfriends an, wenn ein Besitzer existiert (MyEntityAIJealousy.java:44-52) | (:122-124) |
  | T5 | `MyEntityAIJealousy` | `Boyfriend`, 3.0, targetChance 15, also 16 % je Prüfung | (:125-127) |

  - Zielfilter `MyEntityAITarget.isSuitableTarget` (MyEntityAITarget.java:71-110): keine gezähmten Tameables, nicht der eigene Besitzer. Spieler nur bei `valentines_day != 0`. Nie `EntityPigZombie` oder `EntityEnderman`. `Mothra` immer, auch ohne Sicht.
  - Nahkampf in `updateAITick` (:220-272): läuft nur mit Item in der Hand und wenn er nicht sitzt. Bei `PlayNicely != 0` wird das Ziel ignoriert (:224-226). Pro Tick setzt er mit Chance 1/100 das Revenge-Ziel zurück (:227-229). Kein Ziel mehr und vorher gekämpft: `b_woohoo` (:261-270).
  - `attackTargetEntityWithCurrentItem` (:923-964): Schaden = Attribut `attackDamage` (8, :155) + Verzauberungsbonus + Stärke `3<<amp` − Schwäche `2<<amp`. Der **Waffenschaden des Items fließt nicht ein**. Krit bei Fall: `+rand(var2/2+2)`, berechnet **vor** dem Addieren des Attributs, also nur aus dem Trankbonus (:943-947). Knockback = Verzauberung + Sprint, `addVelocity(±0.5*kb, 0.1, ±0.5*kb)` (:949-954). Fire Aspect: `lvl*4` Sekunden Brand (:957-960).
  - Fernkampf `attackEntityWithRangedAttack` (:883-917): keiner während eines Schwungs (:885-887).
    - Mit `MyUltimateBow`: `UltimateArrow(velocity 2.0, inaccuracy 10.0)` (:890). Krit-Chance 1/4 (:891), Punch als Knockback (:894-897), Flame setzt 100 Brand (:898-900). Der Bogen verliert 1 Haltbarkeit (:901), `canBePickedUp = 2` (:903).
    - Sonst `Shoes(id 6)` (:907), geworfen mit 1.8 und Streuung 4.0, Bogen `+horizontalDist*0.2` (:908-912). Schaden laut Shoes.java: 6 bei id 6, +4 gegen Creeper, 1 gegen Girlfriend/Boyfriend, 0 gegen Spieler, 10 am Valentinstag. Das Symbol ist Icon-Index 6 von `ItemShoes` (RenderShoe.java:11), welches Bild genau: offen. Der Fern-Task läuft auch mit Nahkampfwaffe parallel.
  - Hoverboard: Reitet der Besitzer ein `Elevator` (`hoverboard`), wird der Boyfriend jeden Tick 0.45 hinter das Board gesetzt, bekommt dessen Yaw und Pitch und keinen Fallschaden (:179-196).
- **Interaktion** (`interact`, :517-763). Alle Zweige verlangen Abstand² < 16 und verbrauchen außerhalb von Creative 1 Item:

  | Item | Bedingung | Wirkung | Beleg |
  |---|---|---|---|
  | `cooked_beef` / `cookedpeacock` | ungezähmt | Chance 1/3: zähmen, Herzen, volle Heilung; sonst Rauch | (:523-538) |
  | dito | Besitzer | volle Heilung (Herzen nur clientseitig) | (:539-547) |
  | `deadbush` | Besitzer | entzähmen, Besitzer leeren | (:557-572) |
  | `ruby` | Besitzer | Stimme aus (DW 23 = 0) | (:573-588) |
  | `amethyst` | Besitzer | Stimme an | (:589-604) |
  | `leather` / `peacockfeather` | Besitzer | nass: nächster Badeskin 0..17, sonst nächstes Outfit 0..27 | (:605-635) |
  | anderes `ItemFood` | Besitzer | Heilung `getHealAmount*5` | (:637-653) |
  | beliebiges anderes Item | Besitzer | Item in die Hand (Slot 0), altes Item zum Spieler; `diamond` = sitzen, sonst aufstehen | (:655-666) |
  | Emerald/Amethyst/Ultimate-Rüstung | Besitzer, Hand vorher leer | wandert in den passenden Rüstungsslot (1.7.10: 4 Helm, 3 Brust, 2 Beine, 1 Stiefel), altes Stück in die Hand | (:667-694) |
  | `diamond_block` | **jeder Spieler, keine Besitzerprüfung** | übernimmt den Besitz und steht auf | (:699-713) |
  | `name_tag` | Besitzer | Name setzen | (:714-724) |
  | leere Hand | Besitzer | erstes belegtes Slot 0..4 zurückgeben; sonst Chat „I have %d health. Thanks for asking!" | (:725-760) |

  Zucht gibt es nicht: `createChild` liefert null (:970-972). `isWheat` (:765-767) hat keinen Aufrufer im Mod und ist in MCP-1.7.10 kein Vanilla-Name (grep `methods.csv` = 0). Die Methode ist wirkungslos.
- **Drops** (`dropFewItems`, :844-881):
  - gezähmt: 2–6 `red_flower` (Poppy) (`rand(5)+2`, :847-851)
  - immer: 10–35 `gamecontroller` (`rand(26)+10`, :853-858)
  - gezähmt: Handitem und alle 4 Rüstungsslots mit Stackgröße (:859-880)
  - XP 0. `getDropItem` = `red_flower` (:840) ist durch den Override ungenutzt.
- **Spawnen:**
  - `getCanSpawnHere` (:990-1007): ein Spawner mit Name „Boyfriend" im Quader x/z −3..2, y 0..4 erlaubt den Spawn immer. Sonst gilt `super` (Vanilla `EntityAnimal`; genaue Regel im Repo nicht belegt: offen).
  - Biome laut manifest.
  - Frosch-Kuss: Schleichen mit leerer Hand gibt zu 50 % einen Boyfriend mit `setPrince(1..2)` (Frog.java:101-106).
  - Spawner auf Girlfriend Island (GenericDungeon.java:5035).
  - `canDespawn` = false (:769-771).
- **Zustand:**
  - DataWatcher: 20 `which_guy` (Outfit), 21 `voice`, 22 `which_wet_guy`, 23 `voice_enable`, 24 `is_prince` (0/1/2) (:134-141).
  - NBT: `GirlType`, `WetGirlType`, `GirlVoice`, `GirlVoiceEnable`, `IsPrince` (:199-218).
  - `setPrince` setzt nur das Feld (:274-276). DW 24 folgt erst beim nächsten Sync-Zyklus (:502).
  - Texturen: Prinz 1/2 = `FrogPrince.png`/`FrogPrince2.png`, nur trocken (:281-286). Nass gilt immer `swimshorts<n>.png`, auch beim Prinzen (:372-428).
- **Sounds:**
  - `b_fight` 0.5, jeder 3. Schlag (:239-245)
  - `b_taunt` 0.5, alle 300 Ticks (:250-256)
  - `b_woohoo` 0.4 (:267)
  - Living (:773-817): stumm beim Sitzen oder `voice_enable == 0`. Bei `bro_mode` 50 % stumm, danach nur 1/11. Stumm mit Ziel. Im Wasser oder in Lava `b_water`. Sonst mit 3/4: stumm bei Y < 60, bei Gewitter `b_thunder`, bei Regen `b_rain`, nachts unter freiem Himmel mit 1/3 `b_dark`. Danach nur gezähmt: bei HP < 80 `b_hurt`, sonst `bb_happy` (bro_mode) oder `b_happy`.
  - Hurt: `b_ow`, bei bro_mode 50 % stumm (:819-827)
  - Death: bro_mode stumm; gezähmt `b_death_boyfriend`, sonst `b_death_single` (:829-834)
  - Vanilla: `damage.fallbig`/`fallsmall` (:466, :470), `random.bow` (:902, :913)
- **Config:** `BoyfriendEnable` (0), `BoyfriendBroMode` → `OreSpawnMain.bro_mode` (0) (manifest), `PlayNicely`. `valentines_day` wird am 14.02. gesetzt (`nowmonth == 1`, Calendar 0-basiert; OreSpawnMain.java:4227-4229). `GuiOverlayEnable`: die Lebensleiste sieht nur der Besitzer (GirlfriendOverlayGui.java:52, :82-86).
- **Portierung 1.21.1:**
  - Basis `TamableAnimal implements RangedAttackMob`. Slot-Mapping 1.7.10 0..4 → `EquipmentSlot.MAINHAND, FEET, LEGS, CHEST, HEAD`. `setOwnerUUID` statt `func_152115_b`.
  - Rüstung: `getArmorValue()` überschreiben und auf 8..23 klemmen (unter der 30er-Grenze).
  - Schadensdeckel 10 in `hurt()`. Kaktus über `damageSource.is(DamageTypes.CACTUS)`.
  - `fireImmune()` im `EntityType`.
  - 48 Texturen (28 trocken, 18 nass, 2 Prinz) liegen nach texture_map unter `textures/entity/`. Renderer: `HumanoidMobRenderer` mit `HumanoidArmorLayer` und `ItemInHandLayer`, Textur aus DW 20/22/24.
  - Sounds mit Varianten (`b_fight1..7` usw.) in `sounds.json`. Tonhöhe per `getVoicePitch()`.
  - Die Besitzübernahme per `diamond_block` ohne Besitzerprüfung ist ein Originalverhalten; bewusst übernehmen oder als Abweichung notieren.
  - Die Spawner-Ausnahme in `getCanSpawnHere` wird zu `checkSpawnRules` mit `MobSpawnType.SPAWNER`.
  - Attributschaden ohne Waffenschaden 1:1 nachbauen; nicht `doHurtTarget` mit Waffenattributen verwenden.

---

### Brutalfly - Brutalfly (`brutalfly`)

- **Rolle:** fliegendes Monster (`extends EntityMob`, :17) mit selbstgebauter Flugsteuerung. Keine AI-Tasks, alles in `updateAITasks`. Entsteht auch aus der Verwandlung des CaterKiller (CaterKiller.java:460).
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 100 | (:42) |
  | Feuer | immun, `fireResistance` 500 | (:43-44) |
  | Rüstung | `Brutalfly_stats.defense` (6) | (:67-69) |
  | Regeneration | +1 HP alle 100 Ticks, zusätzlich +1 je Schuss | (:123-129, :398-400) |
  | Vertikale Dämpfung | `motionY *= 0.6` je Tick | (:115) |
  | Fall / Druckplatten / Kollision | kein Fallschaden, löst keine Platten aus, keine Entity-Kollision; `canBePushed` true | (:95-103, :249-261) |
  | Schussrate | pro Suchtick 1/`shoot`; `shoot` = 3, auf HARD 2 | (:140, :154-156) |
  | Mündung | 2.25 vor der Körpermitte | (:362-365) |
  | Lautstärke | 1.5 | (:75-77) |

- **KI und Angriffe** (`updateAITasks`, :136-247):
  - Hängen-Erkennung: dieselbe Blockposition zählt `stuck_count` hoch (:145-153).
  - Neues Flugziel bei `stuck_count > 30`, Chance 1/200 oder Zielabstand² < 9 (:160):
    - Bodenabstand messen: 3x3-Raster (−5/0/+5) bis 19 Blöcke nach unten, kleinster Wert = `dist` (:163-180). Bei `dist > 10`: `down = dist-10+1` (:181-183).
    - Bis 30 Versuche: x/z = ±(8..27), y = +rand(7)−1−down (:184-197). Angenommen wird ein Luftblock, der **sichtbar** ist; unsichtbare Luft löst einen neuen Versuch aus (:198-201).
  - Jeden Tick mit Chance 1/6 (:205):
    - Nächster Spieler in `expand(30,20,30)`, nicht Creative, sichtbar: Flugziel = Spielerposition +4 Y, Schuss mit 1/`shoot` (:206-216). **Dieser Zweig prüft `PlayNicely` nicht.**
    - Kein Spieler und Chance 1/3: `findSomethingToAttack` in `expand(25,20,25)`, sortiert mit `GenericTargetSorter` (Creeper zählen halb so weit, Distanz geteilt durch `width*height` wenn > 1). Mit `PlayNicely` gibt es kein Ziel (:440-442). Geeignet sind `EntityMob` und Nicht-Creative-Spieler; nicht Brutalfly, Mothra, Vortex oder `isIgnoreable` (:403-437). Flugziel = Ziel +5 Y. Abstand² > 25: Schuss mit 1/`shoot`, sonst Nahkampf `attackEntityAsMob` (Vanilla, Attribut `Brutalfly_stats.attack` = 10) (:221-234).
  - Flugphysik: `motionX/Z += (sign(d)*0.5 − motion)*0.30001`, `motionY += (sign(dy)*0.7 − motionY)*0.20001` (:237-242). Yaw folgt der Bewegung mit 1/8 je Tick (:243-246).
  - `attackWithSomething` nach Schwierigkeit (:361-401):

    | Schwierigkeit | Projektil | Sound | Beleg |
    |---|---|---|---|
    | EASY | Vanilla `EntitySmallFireball` | `random.bow` 0.75 | (:366-372) |
    | NORMAL | 50 % SmallFireball, 50 % `BetterFireball` mit `setNotMe()` | `random.bow` 0.75 / `random.fuse` 1.0 | (:373-389) |
    | HARD (und PEACEFUL, weil `else`) | immer `BetterFireball` mit `setNotMe()` | `random.fuse` 1.0 | (:390-397) |

    `setNotMe` bedeutet: der BetterFireball fliegt **durch Spieler** hindurch (BetterFireball.java:155-157, :225-227). Spieler verletzt nur die Explosion (Stärke 1) oder ein anderer Treffer. Zu Eigentreffern über die Mündung innerhalb der Hitbox siehe BetterFireball.
  - Getroffen (:263-274): Schaden von einer anderen Brutalfly wird ignoriert. Sonst Flugziel = Angreifer +2 Y.
- **Interaktion:** keine.
- **Drops** (`dropFewItems`, :332-345):
  - 20 `largeexplode`-Partikel (im Original serverseitig aufgerufen und deshalb wirkungslos)
  - 53 `gold_nugget`, einzeln verstreut um ±7 Blöcke (`OreSpawnRand.nextInt(8)-nextInt(8)`, :327-330)
  - 20 `Butterfly`-Entities (`butterfly`)
  - XP 100
- **Spawnen** (`getCanSpawnHere`, :284-322):
  - Spawner „Brutalfly" im Quader x/z −2..2, y +1..+3: erlaubt.
  - Sonst alle Bedingungen: Y >= 70, `isValidLightLevel` (dunkel), nicht Tag, Luft im Quader x −3..2 / y 1..9 / z −4..3, keine andere Brutalfly in `expand(64,32,64)`.
  - Biome laut manifest. Level „Q" der `makeEnormousCastleQ` (GenericDungeon.java:6536).
  - `canDespawn` = `!isNoDespawnRequired()` (:55-57).
- **Zustand:** keine DataWatcher-Einträge, kein eigenes NBT (:276-282). `currentFlightTarget` wird nicht gespeichert.
- **Sounds:** `MothraWings` 1.0 alle 31 Ticks (`> 30`, :116-122); Death `random.explode` (:91-93); Living und Hurt stumm (:83-89); Schüsse wie oben.
- **Config:** `Brutalfly_health` / `_attack` / `_defense` (110/10/6), `BrutalflyEnable`, `PlayNicely` (manifest).
- **Renderer (Client):** zweiter Pass mit `Brutalfly_overlay2.png`: additives Blending (`glBlendFunc(1,1)`), Farbe 0.5, Licht aus, UV-Scroll `ticks*0.01` (RenderBrutalfly.java:43-65).
- **Portierung 1.21.1:**
  - `Monster` ohne Goals; die Logik kommt nach `customServerAiStep`. Schwerkraft bleibt an, die Vertikaldämpfung 0.6 nachbauen. `causeFallDamage` → false, `isPushable` → true, `doPush`/`pushEntities` leer.
  - Overlay-Pass wie der Creeper-Energiemantel: `RenderType.energySwirl(overlay, u, v)` mit Farbe 0.5.
  - Die fehlende `PlayNicely`-Prüfung im Spielerzweig ist Originalverhalten und muss dokumentiert werden.
  - Partikel beim Tod: im Original unsichtbar; `level.sendParticles` wäre eine Abweichung.
- **Recherche-Abweichung:** 01-mobs.md nennt „Forest/Jungle" als Spawn. Laut manifest sind es megaTaigaHills, extremeHillsPlus und mesaPlateau.

---

### Camarasaurus - Camarasaurus (`camarasaurus`)

- **Rolle:** zähmbarer, friedlicher Pflanzenfresser (`extends EntityTameable`, :14). Greift nie an; das Attribut `attackDamage` 1.0 (:52) bleibt ungenutzt.
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 5 | (:34) |
  | `fireResistance` | 100 | (:31) |
  | Fallschaden | `ceil(fall-3)`, max. 2 | (:69-83) |
  | Fress-Heilung | +1 HP je Fressen | (:200) |
  | Fress-Auslöser | nicht sitzend und (1/20 bei HP < 20 oder 1/250) und `PlayNicely == 0` | (:176) |
  | Fress-Reichweite | Suchschalen i = 1,2,3,4,5,6,8,10 (ab 6 wird ein Schritt übersprungen), Höhe ±min(i,2); gefressen wird bei Abstand² < 12 | (:182-196) |
  | Lautstärke | 0.4 | (:304-306) |
  | Tonhöhe | Kind `1.5 ± 0.1`, sonst `1.0 ± 0.1` | (:323-325) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 0 | `EntityAISwimming` | | (:35) |
  | 1 | `EntityAIMate` | 1.0 | (:36) |
  | 2 | `MyEntityAIFollowOwner` | Speed 2.0, max 10, min 2 | (:37) |
  | 3 | `EntityAIAvoidEntity` | `EntityMob`, 8.0, 1.0 / 1.4 | (:38) |
  | 4 | `EntityAITempt` | 1.2, `apple`, false | (:39) |
  | 5 | `EntityAIPanic` | 1.5 | (:40) |
  | 6 | `EntityAIWatchClosest` | Player, 6.0 | (:41) |
  | 7 | `MyEntityAIWander` | 1.0 | (:42) |
  | 8 | `EntityAILookIdle` | | (:43) |
  | 9 | `EntityAIMoveIndoors` | | (:44) |

  - `updateAITick` (:168-205): Revenge-Reset mit 1/200. Fressen: nächstgelegener Block aus `Blocks.leaves` (nur Metadaten der ersten Laub-Gruppe, nicht `leaves2`), `vine`, `tallgrass`, `cactus` oder `double_plant` (:90-91). Pfad dorthin mit Speed 1.0. Nah genug: bei `mobGriefing` den Block entfernen (Flag 2), +1 HP, `random.burp`.
- **Interaktion** (:223-287):
  - Zuerst `super.interact` (Vanilla-Zucht mit `isBreedingItem` = `crystalapple`, :353-355) (:229-231).
  - `apple`, Abstand² < 16: ungezähmt mit Chance 1/2 zähmen und voll heilen, sonst Rauch (:232-247). Besitzer: volle Heilung (:248-256). Verbraucht 1 Stück.
  - `name_tag` durch den Besitzer (:266-276).
  - Sonst Besitzer mit beliebiger Hand: Sitzen umschalten (:277-285).
  - Entzähmen ist nicht möglich (deckt sich mit der Recherche).
  - Kind: `createChild` erzeugt einen neuen Camarasaurus (:341-347). `isWheat` (:349-351) ist wirkungslos (siehe Boyfriend).
- **Drops:** nur gezähmt 2–6 `red_flower` (:312-321). XP 5.
- **Spawnen:**
  - `getCanSpawnHere`: Y >= 50 und Tag (:60-62).
  - Einzige natürliche Quelle: Ambient-Liste der Mining-Dimension, `ChunkProviderOreSpawn2`, Gewicht 1, Gruppe 2–4 (ChunkProviderOreSpawn2.java:399-401). Das manifest listet keine Spawns, weil es nur `EntityRegistry.addSpawn` erfasst.
  - Ancient Dried Egg: `MyCamaSpawnBlock` (OreSpawnMain.java:2436).
  - `canDespawn` (:333-339): ein Kind wird dauerhaft (`enablePersistence`) und despawnt nie; ein Erwachsener nur, wenn ungezähmt und `!isNoDespawnRequired`.
- **Zustand:** keine eigenen DataWatcher-Einträge oder NBT-Keys; nur Tameable-Standard (Sitzen, Besitzer).
- **Sounds:** Living stumm (:289-294); Hurt `cryo_hurt`, Death `cryo_death` (:296-302); `random.burp` beim Fressen (:201).
- **Config:** `CamarasaurusEnable`, `PlayNicely`. Gamerule `mobGriefing` (:197).
- **Client:** Kind halbe Größe (RenderCamarasaurus.java:35-39). Das Modell liest `isSitting` (ModelCamarasaurus.java:166).
- **Portierung 1.21.1:**
  - `TamableAnimal`, `isFood` = `crystal_apple`, Zähmen per Apfel in `mobInteract`.
  - Block-Filter explizit abbilden: 1.7.10 `leaves` Meta 0–3 = Eiche, Fichte, Birke, Tropen, **ohne** Akazie und Schwarzeiche. `tallgrass` = `short_grass`/`fern`; `double_plant` = Sonnenblume, Flieder, Rosenbusch, Pfingstrose, hohes Gras, großer Farn. `BlockTags.LEAVES` wäre breiter.
  - Griefing über `EventHooks.canEntityGrief`.
  - Spawn per Biome-Modifier für die Mining-Dimension (Gewicht 1, 2–4, Kategorie `AMBIENT` wie im Original).
- **Recherche-Abweichung:** 01-mobs.md nennt „Attack 0". Der Code setzt 1.0, verwendet es aber nie.

---

### Cassowary - Cassowary (`cassowary`)

- **Rolle:** friedlicher, scheuer Laufvogel (`extends EntityAnimal`, :12). Züchtbar, nicht zähmbar. `attackDamage` 8.0 (:39) ist registriert, wird aber nie benutzt: es gibt keinen Angriffs-Task.
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 5 | (:22) |
  | `fireResistance` | 100 | (:21) |
  | Speed | 0.25, jeden Tick neu gesetzt | (:18, :46-49) |
  | Lautstärke | 0.4 | (:75-77) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 0 | `EntityAISwimming` | | (:24) |
  | 1 | `EntityAIMate` | 1.0 | (:25) |
  | 2 | `EntityAIAvoidEntity` | `EntityMob`, 8.0, 1.0 / 1.4 | (:26) |
  | 3 | `EntityAIAvoidEntity` | `EntityPlayer`, 8.0, 1.0 / 1.4 | (:27) |
  | 4 | `EntityAIPanic` | 1.5 | (:28) |
  | 5 | `EntityAIWatchClosest` | `EntityLiving`, 12.0 | (:29) |
  | 6 | `MyEntityAIWander` | 1.0 | (:30) |
  | 7 | `EntityAILookIdle` | | (:31) |

  Revenge-Reset mit 1/200 je Tick (:92-97).
- **Interaktion:** nur Vanilla-Zucht, `isBreedingItem` = `crystalapple` (:123-125). Das Kind ist ein neuer Cassowary (:111-117). Kein Tempt-Task, `isWheat` = `apple` (:119-121) ist wirkungslos.
- **Drops:** 2–4 `chicken` (roh) (`rand(3)+2`, :83-90). XP 5.
- **Spawnen:** `getCanSpawnHere` = nur tagsüber (:99-101), ohne weitere Block- oder Lichtprüfung. Biome laut manifest. `canDespawn` (:103-109): Kinder dauerhaft, Erwachsene `!isNoDespawnRequired`.
- **Zustand:** keine DataWatcher-Einträge, kein NBT.
- **Sounds:** Living stumm; Hurt und Death `duck_hurt` (:63-73).
- **Config:** `CassowaryEnable`.
- **Client:** Kind halbe Größe (RenderCassowary.java:35-39).
- **Portierung 1.21.1:**
  - `Animal` mit `AvoidEntityGoal<Monster>` und `AvoidEntityGoal<Player>` (8.0, 1.0, 1.4), `PanicGoal` 1.5, `LookAtPlayerGoal` auf `LivingEntity` 12.0.
  - `removeWhenFarAway` erwachsen true; Kinder mit `setPersistenceRequired`.
  - Die fehlende Licht- und Grasprüfung im Spawn ist Original: `SpawnPlacements` mit eigener Regel „nur Tag" registrieren, sonst greift die Animal-Standardregel.
  - Das ungenutzte Angriffsattribut darf fehlen oder bleiben, ohne Wirkung.
- **Recherche-Abweichung:** 01-mobs.md nennt „Attack 0". Das Attribut ist 8.0, bleibt aber wirkungslos.

---

### CaterKiller - CaterKiller (`cater_killer`)

- **Rolle:** großer Waldboss (`extends EntityMob`, :17). Frisst Holz und Laub, legt Spinnennetze und verwandelt sich nach 2400 Ticks im verletzten Zustand in eine Brutalfly. Mit `PlayNicely` halbe Größe (:38-43).
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 200 | (:45) |
  | `fireResistance` | 100 | (:46) |
  | Hitbox | 2.9 x 4.6, mit `PlayNicely` 1.45 x 2.3 | (:38-43) |
  | Rüstung | `CaterKiller_stats.defense` (19) | (:96-98) |
  | Verwandlung | Zähler `ticker` läuft je Tick, solange `HP + 1 < maxHP`; bei `> 2400` Brutalfly | (:457-468) |
  | Nahkampf-Reichweite | Abstand² < `(5.0 + ziel.width/2)²` | (:497) |
  | Trefferchance im Suchtick | `rand(3)==0 \|\| rand(4)==1` | (:499) |
  | Knockback | horizontal 1.2, vertikal 0.1 (0.2 bei Spieler oder totem Ziel) | (:352-366) |
  | Fress-Heilung | +2 HP | (:551) |
  | Fress-Auslöser | (1/8 bei HP < max oder 1/30) und `PlayNicely == 0` | (:525) |
  | Fress-Reichweite | Schalen i = 1..9, 11 (ab 9 übersprungen), Höhe ±min(i,9); gefressen wird bei Abstand² < 81 | (:531-547) |
  | Suchbox Ziel | `expand(20,8,20)` | (:591) |
  | Lautstärke | 1.5, Tonhöhe 1.0 | (:123-129) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 0 | `EntityAISwimming` | | (:48) |
  | 1 | `EntityAIMoveThroughVillage` | 1.0, false | (:49) |
  | 2 | `MyEntityAIWanderALot` | Radius 16, Speed 1.0; Chance 1/30 (MyEntityAIWanderALot.java:35) | (:50) |
  | 3 | `EntityAIWatchClosest` | Player, 8.0 | (:51) |
  | 4 | `EntityAILookIdle` | | (:52) |
  | T1 | `EntityAIHurtByTarget` | callHelp false | (:53) |

  - `attackEntityFrom`: jeder Angreifer vom Typ `EntityLiving` wird Ziel (:77-85). Spieler sind kein `EntityLiving`; sie laufen über `HurtByTarget`.
  - `updateAITasks` (:451-558):
    1. DW 21 = `PlayNicely`, jeden Tick (:456).
    2. Verwandlung: Brutalfly 4 Blöcke über ihm, `random.explode`, 10 Butterflies, `setDead` **ohne Drops** (:457-467). `ticker` wird beim Heilen nicht zurückgesetzt und nicht gespeichert.
    3. Im Netz: alle `web` im Quader x/z ±2, y −1..4 entfernen (:469-481).
    4. Mit 1/4 je Tick: tote Ziele verwerfen, Reset mit 1/200, sonst `findSomethingToAttack` (:482-493). Mit `PlayNicely` gibt es kein neues Ziel (:588-590). Ziel in Reichweite: DW 20 = 1, Schlag mit obiger Chance (:497-502). Außer Reichweite: DW 20 = 0, Pfad mit Speed 1.25. Mit 1/4 ein Netz neben das Ziel: x/z ±2 zufällig, erster Luftblock über festem Block von Ziel-Y+2 abwärts bis −1 (:503-518). Diese Netze sind **nicht** an `mobGriefing` gebunden.
    5. Fressen: Blöcke `leaves`, `vine`, `log`, `duplicatortreelog`, `log2`, `leaves2` und `leaves_apple`/`_experience`/`_scary`/`_peach`/`_cherry` (:373). Pfad nur ohne Ziel (`foundmob == 0`, :544-546). Block entfernen nur bei `mobGriefing`, `random.burp` mit 1/20 (:547-555).
  - Zielfilter (:560-585): `MyCanSee` muss wahr sein. Nicht-Creative-Spieler ja, andere CaterKiller nein, `EntityMob` ja. Sonst `MyUtils.isAttackableNonMob`: Mothra, Leon, Dragon, Spyro, Royals, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, Villager, Stinky.
  - `MyCanSee` (:652-712): eigener Sichtstrahl ab 2.5 Blöcken vor dem Kopf, Y+3, in Schritten über `nblks`. Nur `air`, `web`, `tallgrass` und `leaves` (nicht `leaves2`) sind durchsichtig. `nblks *= (int)|d|` rundet ganzzahlig ab.
- **Interaktion:** `interact` gibt immer false zurück (:348-350).
- **Drops** (:145-329). Alle Items einzeln verstreut um ±4 Blöcke (`OreSpawnRand.nextInt(5)-nextInt(5)`, :135-143):
  - immer: 1 `caterkillerjaw`, 1 `item_frame`, 10 `leather`, 6 `beef` (:147-154)
  - 1–5 Würfe `rand(20)` (:155-325):

    | Wurf | Item | Verzauberung (je unabhängig) |
    |---|---|---|
    | 0 | `ultimatesword` | – |
    | 1 | `ruby` | – |
    | 2 | `diamond_block` | – |
    | 3 | `rubysword` | je 1/6: Sharpness 1–5, Bane 1–5, Knockback 1–5, Looting 1–5, Fire Aspect 1–5, nochmals Sharpness 1–5; 1/2 Unbreaking 2–5 |
    | 4–7 | `rubyshovel` / `rubypickaxe` / `rubyaxe` / `rubyhoe` | 1/2 Unbreaking 2–5, 1/6 Efficiency 1–5; Spitzhacke zusätzlich 1/6 Fortune 1–5 |
    | 8 | `ruby_helmet` | je 1/6 Protection, Blast, Fire, Projectile 1–5; 1/2 Unbreaking 2–5; 1/6 Respiration 1–2; 1/6 Aqua Affinity 1–5 |
    | 9 / 10 | `ruby_chest` / `ruby_leggings` | je 1/6 Protection, Blast, Fire, Projectile 1–5; 1/2 Unbreaking 2–5 |
    | 11 | `ruby_boots` | 1/6 Feather Falling 5–9; 1/2 Unbreaking 2–5 |
    | 12 | `ultimatebow` | – |
    | 13–19 | nichts | |

  - danach 25 `Butterfly` (:326-328)
  - XP 200. `getDropItem` = `beef` (:131-133) ist durch den Override ungenutzt.
- **Spawnen** (`getCanSpawnHere`, :612-650):
  - Spawner „CaterKiller" im Quader x/z −3..2, y 0..4: erlaubt.
  - Sonst alle Bedingungen: Y >= 50, Chance 1/10, Tag, im Quader x/z −1..1, y 1..4 nur `air`/`leaves`/`leaves2`/`log`/`log2`, kein anderer CaterKiller in `expand(48,16,48)`.
  - Biome laut manifest. Utopia-Plains-Monsterliste Gewicht 1, Gruppe 1 (BiomeGenUtopianPlains.java:355-357). Spawner der Schwierigkeit 6 in `addLevelDecorationsQ` (GenericDungeon.java:6939-6941).
  - `canDespawn` = `!isNoDespawnRequired` (:73-75).
- **Zustand:** DataWatcher 20 = `attacking` (0/1, Modell-Animation, ModelCaterKiller.java:212/220/264); 21 = `PlayNicely` (Renderer halbiert Skalierung, RenderCaterKiller.java:35-36) (:63-67). **Kein NBT**, `ticker` geht beim Neuladen verloren.
- **Sounds:** Living `caterkiller_living` mit 1/3; Hurt `caterkiller_hit`; Death `caterkiller_death` (:108-121); `random.explode` bei der Verwandlung (:461); `random.burp` (:553).
- **Config:** `CaterKiller_health` / `_attack` / `_defense` (450/32/19), `CaterKillerEnable`, `PlayNicely`, Gamerule `mobGriefing`. Boss-Lebensleiste über `GuiOverlayEnable` (GirlfriendOverlayGui.java:335-338).
- **Portierung 1.21.1:**
  - 450 HP und Rüstung 19 liegen unter den Grenzen.
  - Größe hängt von der Config ab: `EntityType`-Maße sind fest. Entweder `getDefaultDimensions` überschreiben und `refreshDimensions()` aufrufen, oder `Attributes.SCALE` 0.5 setzen (skaliert auch den Renderer). DW 21 wird dann überflüssig, bleibt aber für die Client-Skalierung nötig, wenn nicht über SCALE gelöst.
  - Verzauberungen über `ItemEnchantments`/`EnchantmentHelper` mit `Holder<Enchantment>` aus dem Registry-Lookup.
    - Stufen über dem Maximum (Aqua Affinity 5, Feather Falling 9) sind über `ItemStack.enchant` erlaubt.
    - **Doppelte Sharpness:** In 1.7.10 entstehen zwei Listeneinträge; in 1.21 überschreibt der zweite Aufruf den ersten. Abweichung vermerken.
  - Block-Scans laufen bis zu jeden 8. Tick über mehrere tausend Blöcke: Performance messen und `BlockState`-Prüfung über ein Set statt über Einzelvergleiche.
  - Den Verwandlungszähler bei Bedarf in NBT sichern; das wäre eine Abweichung.
- **Recherche-Abweichung:** 01-mobs.md nennt Attack 30 / Defense 18. Die Config-Defaults im Code sind 32 / 19 (manifest).

---

### CaveFisher - CaveFisher (`cave_fisher`)

- **Rolle:** kleiner Höhlenjäger (`extends EntityMob`, :15). Greift **keine Monster** an, sondern Spieler, Tiere und Begleiter.
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 10 | (:28) |
  | Feuer | nicht immun, `fireResistance` 10 | (:29-30) |
  | Rüstung | `CaveFisher_stats.defense` (4) | (:91-93) |
  | Kaktus | kein Schaden | (:169-175) |
  | Suchtick | 1/8 je Tick | (:150) |
  | Suchbox | `expand(10,3,10)` | (:219) |
  | Nahkampf-Reichweite | Abstand² < 8.0 | (:153) |
  | Trefferchance | `rand(7)==0 \|\| rand(8)==1` | (:155) |
  | Verfolgung | Pfad-Speed 1.2 | (:160) |
  | Lautstärke | 1.5, Tonhöhe 1.0 | (:115-121) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 0 | `EntityAISwimming` | | (:33) |
  | 1 | `MyEntityAIWanderALot` | Radius 14, Speed 1.0 | (:34) |
  | 2 | `EntityAIWatchClosest` | Player, 8.0 | (:35) |
  | 3 | `EntityAILookIdle` | | (:36) |
  | T1 | `EntityAIHurtByTarget` | false | (:37) |

  - Zielfilter (:177-213): nicht `isIgnoreable`, sichtbar, nicht CaveFisher, EnderReaper, EnderKnight oder **`EntityMob`**. Creative-Spieler nein, alles andere Lebende ja. Mit `PlayNicely` gibt es kein Ziel (:216-218).
  - DW 20 bleibt 1, solange er einem Ziel nachläuft: der Else-Zweig setzt es nicht zurück. Erst ein Suchtick ohne Ziel setzt 0 (:159-165).
  - Nahkampf: Vanilla `attackEntityAsMob` mit `CaveFisher_stats.attack` (4) (:141-143).
- **Interaktion:** `interact` gibt false zurück (:137-139).
- **Drops:** `getDropItem` wird pro Aufruf gewürfelt, `rand(6)`: 0 `gold_nugget`, 1 `uranium_nugget`, 2 `titanium_nugget`, 3–5 nichts (:123-135). Stückzahl aus Vanilla-`dropFewItems`; Mengenregel im Repo nicht belegt: offen. XP 10.
- **Spawnen** (`getCanSpawnHere`, :242-260):
  - Spawner „CaveFisher" im Quader x/z −2..1, y 0..4: erlaubt.
  - Sonst `isValidLightLevel` und Y <= 50.
  - Quellen im Code:
    - Mining-Dimension, Monsterliste Gewicht 35, Gruppe 4–8 (ChunkProviderOreSpawn2.java:375-377)
    - Utopia Plains Gewicht 5, Gruppe 1–5 (BiomeGenUtopianPlains.java:358-360)
    - 6 Spawner in `makeEnderCastle` (GenericDungeon.java:3460, :3465, :3475, :3480, :3490, :3495)
    - Ei `CaveFisherEgg` in Dungeon-Kisten (GenericDungeon.java:94-95)
  - Kein `EntityRegistry.addSpawn` in der Oberwelt (manifest `spawns: []`).
  - `canDespawn` = `!isNoDespawnRequired` (:63-65).
- **Zustand:** DataWatcher 20 = `attacking` (Scheren-Animation, ModelCaveFisher.java:521). `RenderInfo renderdata` (rf1..4, ri1..4) ist ein reiner Client-Animationsspeicher am Entity-Objekt, den das Modell liest und schreibt (ModelCaveFisher.java:516, :538). Kein NBT.
- **Sounds:** Living stumm; Hurt `cryo_hurt`, Death `cryo_death` (:103-113).
- **Config:** `CaveFisher_health` / `_attack` / `_defense` (10/4/4), `CaveFisherEnable`, `PlayNicely`.
- **Portierung 1.21.1:**
  - `Monster`. Die Zielauswahl kommt als eigener `Goal` in `customServerAiStep`, weil sie ohne AI-Task im `updateAITasks` lief.
  - `RenderInfo` als Felder in der Entity-Klasse: nur primitive Werte, keine Client-Imports. Das Modell schreibt sie in `setupAnim`; 1.21.1 hat noch keine `EntityRenderState`.
  - Die Oberwelt-Spawnregel „dunkel und Y <= 50" gilt nur für Spawner und Dimensionen, in denen die Kreatur gelistet ist. In der Oberwelt kommt sie nicht natürlich vor.
- **Recherche-Abweichung:** 01-mobs.md nennt „deep caves" für die Oberwelt. Im Code gibt es dort keinen Biome-Spawn, nur Mining-Dimension, Utopia und Ender Castle.

---

### Cephadrome - Cephadrome (`cephadrome`)

- **Rolle:** reitbarer Flugdrache ohne Zähmung (`extends EntityCreature`, :21). Neutral gegenüber Spielern, jagt Monster und andere Bosse.
- **Werte:**

  | Größe | Wert | Beleg |
  |---|---|---|
  | XP | 200 | (:60) |
  | `fireResistance` | 100, nicht feuerimmun | (:61-62) |
  | Rüstung | 16 | (:156-158) |
  | Nahkampfschaden | 70; gegen `Kraken` x1.5 = 105 | (:404-425) |
  | Gegen `EntityDragon` | 70 als Explosionsschaden auf `dragonPartHead` (1/6) oder `dragonPartBody` | (:408-420) |
  | Knockback | horizontal 2.5, vertikal 0.35 (0.7 bei Spieler oder totem Ziel) | (:404-405, :426-430) |
  | Unverwundbarkeit | nach jedem Treffer 25 Ticks lang **jeder** Schaden ignoriert | (:437-442) |
  | Kaktus | kein Schaden | (:440) |
  | Regeneration | Chance 1/100 je Tick: +2 HP | (:484-486) |
  | Angriffsprüfung | Chance 1/7 je Tick, nicht PEACEFUL | (:490) |
  | Reichweite | am Boden `(6 + w/2)²`, geritten `(10 + w/2)²` Abstand²; Kraken horizontal | (:465, :500-511) |
  | Suchbox | `expand(16,20,16)` | (:583) |
  | Aktivitäts-Sync | alle 30 Ticks: geritten = 1, sonst 0 | (:469-483) |
  | Fall | kein Fallschaden (:108-112); Sprung +0.1 `motionY` (:160-163) | |
  | Reiter | Sitzhöhe 2.5, 0.75 nach vorn versetzt; `shouldRiderSit` | (:92-94, :196-198, :868-873) |
  | Schieben | `canBePushed` false | (:192-194) |
  | Lautstärke | 1.5 | (:184-186) |

- **KI und Angriffe:**

  | Prio | Task | Parameter | Beleg |
  |---|---|---|---|
  | 0 | `EntityAISwimming` | | (:63) |
  | 1 | `MyEntityAIWanderALot` | Radius 16, Speed 1.0 | (:64) |
  | 2 | `EntityAIWatchClosest` | Player, 9.0 | (:65) |
  | 3 | `EntityAILookIdle` | | (:66) |
  | T1 | `EntityAIHurtByTarget` | false | (:67) |

  - Die Tasks laufen nur bei Activity 0 (:487-489). Geritten ruft `onLivingUpdate` stattdessen `updateAITasks` direkt auf (:863-865).
  - Zielwahl (:519-577): nicht PEACEFUL, sichtbar, nicht Cephadrome.
    - Immer Ziel: `EntityMob`, `Mothra`, `EntityDragon`.
    - Nur ungezähmt: `Leon`, `GammaMetroid`, `WaterDragon`.
    - Spieler (nicht Creative) nur, wenn `hit_by_player`, `badmood` oder `shouldattack > 0`. `shouldattack` wird dabei auf 0 gesetzt, gilt also einmal.
    - Mit `PlayNicely` gibt es kein Ziel (:580-582).
  - Mit Ziel (:499-512): am Boden Pfad mit Speed 1.7 und Reichweite 6, geritten Reichweite 10. Ausrichten, DW 20 = 1, angreifen. Da `hit_by_player` dauerhaft ist, kann ein gerittener Cephadrome auch den **eigenen Reiter** als Ziel wählen (abgeleitet; der Filter schließt den Reiter nicht aus).
  - Getroffen (:435-455): ein lebender Angreifer wird Ziel (`setAttackTarget` und `setTarget`), Pfad mit Speed 1.2. Ein Spieler-Treffer bei HP < 90 % setzt `hit_by_player = 1`, dauerhaft und in NBT gespeichert.
  - Flug unter dem Reiter (serverseitig, Activity != 0, :708-862):
    1. Horizontale Motion auf ±2.0 klemmen (:711-722).
    2. Bodenprobe 1.55 unter dem Körper: fest → `motionY += 0.07` und `posY += 0.1`; Luft → `motionY -= 0.018` (:724-732).
    3. Hindernis-Scan vor dem Tier: Reichweite `dist = 2 + (int)(v*6)`, jeder Nicht-Luftblock +0.04. `motionY` und `posY += obs*0.09`, `motionY` max. 2.0 (:733-750).
    4. Yaw folgt dem Reiter. Bei v > 0.1 verzögert um `clamp(|1.5 − v|, 0.01, 0.9)` (:751-772). Pitch `360 − 2v` steigend, `2v` sinkend (:777-783).
    5. Vorwärts (`moveForward > 0`): +0.03 und +0.05 (weil `max_speed` 1.15 > 0.85 immer gilt), max. 1.15. Rückwärts: −0.03, max. 0.35 (:807-834).
    6. Flug-Taste (`flyup_keystate`): `motionY += 0.04 + v*0.05` (:792-795).
    7. `moveEntity`, danach Dämpfung x/z 0.985, y 0.94 (:844-847).
    8. Entities in `expand(2.25, 2.0, 2.25)` werden weggeschoben (:848-858).
  - Client-Interpolation im Stil eines Boots (`boatPosRotationIncrements`) mit Yaw vom Reiter (:636-646, :693-707).
- **Interaktion** (:888-924):
  - `beef` / `chicken` / `porkchop` (roh), Abstand² < 25: volle Heilung, `wasfed = 1`, `shouldattack = 0`, Herzen, 1 Stück verbraucht. Rückgabe false (:894-910).
  - Reitet schon ein anderer Spieler: true, nichts passiert (:911-913).
  - Leere Hand, Abstand² < 25, Server: bei `wasfed == 0` läuft er zum Spieler (Speed 1.2) und setzt `shouldattack = 1`, der Spieler wird einmal Ziel. Sonst steigt der Spieler auf und `wasfed = 0` (:914-922).
  - **Portierungsfalle:** `onUpdate` setzt `wasfed = 1` jeden Tick, wenn `PlayNicely == 0` (:665-667). Bei Standard-Config ist der Cephadrome also immer „satt" und Füttern ist unnötig. Mit `PlayNicely != 0` greift der Hunger zwar, aber `findSomethingToAttack` liefert dann null. Im Code kommt die in der Recherche beschriebene „frisst dich"-Mechanik deshalb nie zum Tragen.
  - Absteigen: Vanilla-Sneak. Das Tier despawnt danach wieder normal (`canDespawn`).
- **Drops** (:214-397). Items verstreut um ±4 Blöcke:
  - 4–9 `uranium_nugget`, 4–9 `titanium_nugget` (:215-220)
  - 1–5 Würfe `rand(20)`: 0 `rubysword` ohne Verzauberung, 1 `diamond`, 2 `thunderstaff`, 3 `rubysword` mit Verzauberungen, 4–7 Ruby-Werkzeuge, 8–11 Ruby-Rüstung (Verzauberungsschema identisch zu CaterKiller), 12–17 `ruby`, 18–19 nichts (:221-396)
  - XP 200. `getDropItem` = `beef` (:200-202) ist ungenutzt.
- **Spawnen:**
  - `getCanSpawnHere` (:598-634): Spawner „Cephadrome" im Quader x/z −3..2, y 0..4 setzt `badmood = 1` (feindlich gegenüber Spielern, gespeichert) und erlaubt den Spawn.
  - Sonst alle Bedingungen: Tag, Y >= 50, Luft im Quader x/z −2..1, y 1..4, kein anderer Cephadrome in `expand(16,6,16)`.
  - Biome laut manifest (icePlains, coldTaiga, Kategorie ambient).
  - Beschwörung: `ExtremeTorch` (`extremetorch`) auf `blockeyeofender` spawnt einen Cephadrome auf einem freien Platz ±(4±2) Blöcke entfernt, bis zu 100 Versuche (BlockExtremeTorch.java:64-89).
  - „Cephadrome Altars" aus der Recherche: im Code nicht als eigener Name gefunden (grep „Cephadrome" in GenericDungeon ohne Treffer). Offen, vermutlich der Torch-Mechanismus.
  - `canDespawn` = `!isNoDespawnRequired && riddenByEntity == null` (:948-950).
- **Zustand:**
  - DataWatcher: 20 = `attacking`, 21 = `activity` (0 Boden-AI, 1 geritten). Beide nur serverseitig setzbar (:120-123, :926-946).
  - NBT: `CephaWasFed`, `CephaAttacking`, `CephaActivity`, `CephaHitByPlayer`, `CephaBadMood` (:952-968). `shouldattack` und `hurt_timer` werden nicht gespeichert.
  - `RenderInfo` als Client-Animationsspeicher (ModelCephadrome.java:328, :466). Das Modell liest Activity und Attacking (ModelCephadrome.java:343-460).
- **Sounds:** Living `MothraWings` mit 1/6, wenn nicht geritten (:169-174); geritten `MothraWings` 0.5 alle 23 Ticks (:656-664); Hurt `alo_hurt`, Death `alo_death` (:176-182).
- **Config:** `CephadromeEnable`, `PlayNicely`. `OreSpawnMain.flyup_keystate`: **eine globale statische Variable**, gesetzt aus `RiderControlMessage` (RiderControl.java:23-28 → RiderControlMessageHandler.java:17). Sie gilt für alle Reiter und alle Reittiere des Servers gleichzeitig. Die Lebensleiste zeigt `GuiOverlayEnable` nur ohne Reiter (GirlfriendOverlayGui.java:227-232).
- **Portierung 1.21.1:**
  - `PathfinderMob`. Reiten über `getControllingPassenger`, `tickRidden`, `getRiddenInput` und `travel` statt `onLivingUpdate`-Überschreibung. Die Flugphysik in `tickRidden` bzw. `travel` 1:1 übernehmen.
  - Flug-Taste als Client→Server-Payload (`CustomPacketPayload`), gespeichert **pro Spieler** (Attachment oder Map auf UUID). Die globale Variable nicht nachbauen; Abweichung vermerken. Die Taste gehört als `KeyMapping` nach `com.swbr.orespawn.client`.
  - Unverwundbarkeit: eigener Zähler in `hurt()`, weil `invulnerableTime` stärkeren Schaden durchlässt.
  - Drachenschaden: `EnderDragon.hurt(EnderDragonPart, DamageSource, float)` mit `dragon.head` bzw. `dragon.body` (Feldnamen in den sources-Jars prüfen) und `damageSources().explosion(null, null)`.
  - `wasfed`-Überschreibung und die Möglichkeit, den eigenen Reiter anzugreifen, bewusst 1:1 übernehmen oder als Fix kennzeichnen.
  - `getTrackingRange`/`getUpdateFrequency`/`sendsVelocityUpdates` (:96-106) überschreiben in 1.7.10 nichts Bekanntes. Maßgeblich ist die Registrierung `128, 1, true` (OreSpawnMain.java:3567) → `clientTrackingRange(8)` Chunks, `updateInterval(1)`.
- **Recherche-Abweichungen:** 01-mobs.md nennt „140 gegen Kraken"; der Code rechnet `1.5f * 70` = 105 (:423-425). „Wenn hungrig, frisst er dich": siehe `wasfed`-Falle oben, im Standard nie aktiv.
