# Verhalten: itemblock-01

Dieser Stapel umfasst 59 Item-Klassen aus OreSpawn 1.7.10 (Build 20.2/20.3), die zusammen 296 Registry-IDs tragen: die Werkzeug- und Waffensätze aus Amethyst, Emerald, Crystal Wood, Kyanite, Pink Tourmaline und Tiger's Eye, die Großschwerter der Bertha-Familie, sämtliche 114 Critter Cages, alle 56 Rüstungsteile (`ItemOreSpawnArmor`), die Nahrungsmittel- und Saatgutklassen, die Wurfgeschosse (Acid, Ice Ball, Irukandji, Laser Ball, Rocks, Shoes) sowie die „Instant“-Bauitems, die sofort Strukturen in die Welt schreiben (Instant Garden, Instant Shelter, Miner's Dream, Magic Apple, Apple-/Cherry-/Peach-Seeds). Es gibt keine TileEntity und keine GUI; die Hälfte der Klassen ist reiner Datenträger ohne eigene Logik. Die aufwendigen Stellen sind die Baum- und Tunnelgeneratoren (von `OreSpawnWorld` für die Weltgenerierung mitbenutzt), die Kapsel-Tabelle der Critter Cage, das Rüstungssystem mit dauerhaft nachgetragenen Verzauberungen und Gleitflug sowie die Loot-Tabellen des Sifter und der Magic-Apple-Truhen. Alle Zahlen tragen ihre Herkunft; Vanilla-Konstanten sind per `javap` aus `reference/jar/mcp/client-1.7.10.jar` belegt.

---

## 0. Gemeinsame Grundlagen

### 0.1 Vanilla-1.7.10-Konstanten (belegt per `javap` gegen `client-1.7.10.jar`, Klassennamen via `joined.srg`)

| Vanilla-Klasse (obf) | Belegter Wert | Bedeutung |
|---|---|---|
| `ItemSword` (`aeh`) | `ldc 4.0f` im Konstruktor, dann `+ ToolMaterial.c()` | Schwertschaden = 4 + Materialschaden |
| `ItemSword` (`aeh`) | `ldc int 72000` | Standard-`getMaxItemUseDuration` (Blocken) |
| `ItemAxe` (`abf`) | `ldc 3.0f` an `ItemTool.<init>` | Axtschaden = 3 + Material |
| `ItemPickaxe` (`adn`) | `fconst_2` | Spitzhackenschaden = 2 + Material |
| `ItemSpade` (`ady`) | `fconst_1` | Schaufelschaden = 1 + Material |
| `ItemTool` (`acg`) | `fload_1 … invokevirtual adc.c()F` | `damageVsEntity = f + material.getDamageVsEntity()` |
| `ItemHoe` (`ada`) | kein Weapon-Modifier im Konstruktor | Hacke hat keinen Angriffsbonus |
| `ItemArmor` (`abb`) | `static int[] m = {11,16,15,13}` | Haltbarkeit je Teil = Faktor × Material-`durability` (Helm, Brust, Beine, Stiefel) |
| `FoodStats.func_75122_a` (`zr.a(IF)V`) | `food = min(food + heal, 20)`; `sat = min(sat + heal·mod·2, food)` | Sättigungsgewinn = Nahrung × Modifier × 2, gedeckelt auf Nahrungsstand |
| `Item$ToolMaterial.func_78000_c` (`adc.c()F`) | Signatur `()F` | `getDamageVsEntity` gehört zu **ToolMaterial ohne Parameter** – nicht zu `Item` |

Folgerung aus der letzten Zeile: Jede Methode `getDamageVsEntity(Entity)` bzw. `getDamageVsEntity()` in einer Item-Klasse dieses Stapels überschreibt nichts und ist **toter Code**. Gleiches gilt für `hitEntity(ItemStack, EntityLiving, EntityLiving)`: die echte Signatur von `func_77644_a` verwendet `EntityLivingBase` (so überschrieben in `BigHammer.java:27`, `FairySword.java:28`, `ExperienceSword.java:112`). Die `weaponDamage`-Felder sind deshalb reine Dekoration; maßgeblich ist immer die Formel oben.

Vanilla-`EntityList`-IDs (aus `sg.<clinit>`, per `javap`): 50 Creeper, 51 Skeleton, 52 Spider, 54 Zombie, 55 Slime, 56 Ghast, 57 PigZombie, 58 Enderman, 59 CaveSpider, 60 Silverfish, 61 Blaze, 62 LavaSlime, 63 EnderDragon, 64 WitherBoss, 65 Bat, 66 Witch, 90 Pig, 91 Sheep, 92 Cow, 93 Chicken, 94 Squid, 95 Wolf, 96 MushroomCow, 97 SnowMan, 98 Ozelot, 99 VillagerGolem, 100 EntityHorse, 120 Villager.

### 0.2 Werkzeugmaterialien dieses Stapels

Alle über `EnumHelper.addToolMaterial` aus `WeaponStats` gebaut. Config-Kategorie `OreSpawnWEAPONS`, Schlüssel `<Prefix>_harvestlevel|_maxuses|_efficiency|_damage|_enchantability` (manifest config).

| Material | Prefix | Harvest | maxUses | Effizienz | Schaden | Enchant. | Herkunft |
|---|---|---|---|---|---|---|---|
| `toolAMETHYST` („AMETHYST“) | Amethyst | 4 | 2000 | 11 | 11 | 70 | (OreSpawnMain.java:1182, 1296) |
| `toolEMERALD` („REALEMERALD“) | Emerald | 3 | 1300 | 10 | 6 | 75 | (OreSpawnMain.java:1183, 1294) |
| `toolBERTHA` | Bertha | 3 | 9000 | 15 | 496 | 100 | (OreSpawnMain.java:1176, 1297) |
| `toolROYAL` | Royal | 3 | 10000 | 15 | 746 | 150 | (OreSpawnMain.java:1184, 1302) |
| `toolHAMMY` | Attitude | 5 | 2000 | 15 | 82 | 100 | (OreSpawnMain.java:1185, 1303) |
| `toolCRYSTALWOOD` | CrystalWood | 2 | 300 | 3 | 2 | 15 | (OreSpawnMain.java:1177, 1298) |
| `toolCRYSTALSTONE` | CrystalStone | 3 | 800 | 6 | 5 | 45 | (OreSpawnMain.java:1178, 1299) |
| `toolCRYSTALPINK` | Pink | 4 | 1100 | 10 | 7 | 65 | (OreSpawnMain.java:1179, 1300) |
| `toolTIGERSEYE` | TigersEye | 4 | 1600 | 12 | 8 | 75 | (OreSpawnMain.java:1180, 1301) |

Config-Klemmung in `get_weaponstats` (OreSpawnMain.java:5701-5725): `harvestlevel < default-1` → default; `maxuses`, `efficiency`, `damage`, `enchantability` jeweils auf `[default/2, default·2]` geklemmt.

**Falle:** Mehrere Klassen rufen `setMaxDamage(<Konstante>)` im Konstruktor. Das überschreibt die Config-Haltbarkeit des Materials. Abweichungen vom Material: `BigHammer` 9000 statt 2000, `Bertha` für Royal 9000 statt 10000 und für Hammy 9000 statt 2000, `ExperienceSword` 1400 statt 1300. Eine `maxuses`-Änderung in der Config wirkt auf diese Items nicht.

### 0.3 Sounds und Partikel (1.7.10-Namen → 1.21.1)

| 1.7.10-String | Vanilla-Entsprechung 1.21.1 (Name im NeoForge-sources-Jar gegenprüfen) |
|---|---|
| `random.bow` | `SoundEvents.ARROW_SHOOT` |
| `random.explode` | `SoundEvents.GENERIC_EXPLODE` |
| `fireworks.launch` | `SoundEvents.FIREWORK_ROCKET_LAUNCH` |
| Partikel `smoke`, `explode`, `largesmoke`, `largeexplode`, `reddust`, `portal`, `happyVillager` | `SMOKE`, `POOF`, `LARGE_SMOKE`, `EXPLOSION`, `DUST` (rot), `PORTAL`, `HAPPY_VILLAGER` |

Keine Klasse des Stapels benutzt einen eigenen OreSpawn-Sound. Partikel werden im Original mit `world.spawnParticle` erzeugt, teils vor der `isRemote`-Prüfung, und sind damit nur clientseitig sichtbar. Im Port gehören sie serverseitig als `ServerLevel.sendParticles` hin, damit der dedizierte Server sie verschickt.

### 0.4 Portierungs-Leitplanken, die für viele Klassen gelten

- **Tiers:** Jedes `ToolMaterial` wird ein 1.21.1-`Tier` mit `uses`, `speed`, `attackDamageBonus`, `enchantmentValue` und einem `incorrect_for_*`-Tag. Harvest-Level-Abbildung: 2 → Eisen, 3 → Diamant, 4 → Netherite. Level 5 (Hammy) hat kein Vanilla-Pendant, bleibt aber für ein Schwert folgenlos.
- **Angriffsschaden 1:1:** 1.7.10 rechnet Spieler-Grundwert 1 + Klassenkonstante (4/3/2/1) + Materialschaden. In 1.21.1 die Klassenkonstante als `attackDamage` an den Attribut-Builder geben und den Materialschaden als Tier-Bonus. 1.7.10 kennt keinen Angriffs-Cooldown, der `attackSpeed` ist also eine bewusste Designentscheidung und keine Übernahme.
- **Verzauberungen:** `addEnchantment` wird `ItemStack.enchant(Holder<Enchantment>, lvl)`. Der Holder kommt aus `level.registryAccess()`; in `onCraftedBy`/`inventoryTick` ist ein `Level` vorhanden. Stufen über dem Vanilla-Maximum (Knockback 5, Protection 10) sind im Component zulässig.
- **Hooks aus Forge 1.7.10** (`onLeftClickEntity`, `onEntitySwing`, `onArmorTick`, `getArmorTexture`, `onUsingTick`) haben NeoForge-Pendants in `IItemExtension`/`IClientItemExtensions`. Die exakten 1.21.1-Signaturen stehen **nicht** in diesem Katalog; sie werden gemäß Repo-Regel im `neoforge-*-sources.jar` nachgesehen, nicht aus dem Gedächtnis geschrieben.
- **Item-Texturen:** `registerIcons` lädt `OreSpawn:<unlocalizedName>`; das Ziel steht in manifest `textures` (z. B. `assets/orespawn/textures/item/amethystaxe.png`).

---

## 1. Amethyst-Werkzeuge

### AmethystAxe
- IDs: `amethystaxe` „Amethyst Axe“ (manifest; OreSpawnMain.java:1340)
- **Rolle:** Axt, Basis `ItemAxe`, Material `toolAMETHYST` (manifest ctor_args).
- **Werte:**
  - Haltbarkeit 2000 (AmethystAxe.java:17), identisch mit Material-maxUses (OreSpawnMain.java:1182)
  - Effizienz 11 (OreSpawnMain.java:1182)
  - Angriff 3 + 11 = 14 (ItemAxe `ldc 3.0f`; Material OreSpawnMain.java:1182)
  - Harvest-Level `axe` = `amethyst_stats.harvestlevel` = 4 (OreSpawnMain.java:1340)
  - Stack 1 (AmethystAxe.java:16), Tab Tools (AmethystAxe.java:18)
- **Verhalten:** reines Vanilla. `weaponDamage = 12` (AmethystAxe.java:15) mit `getDamageVsEntity(Entity)` ist tot (siehe 0.1). `getMaterialName()` liefert „Amethyst“ (AmethystAxe.java:25-27); eine Vanilla-/Forge-Methode dieses Namens ist nicht belegt, das ist also vermutlich ebenfalls tot.
- **Rezeptbezug:** keiner in der Klasse.
- **Sounds/Config:** keine eigenen; Material über `Amethyst_*` in `OreSpawnWEAPONS`.
- **Port 1.21.1:** `AxeItem` mit Tier AMETHYST (Netherite-Tag). Die Haltbarkeit 2000 stammt aus dem Tier, der Konstruktor-Wert entfällt.

### AmethystHoe
- IDs: `amethysthoe` „Amethyst Hoe“ (manifest; OreSpawnMain.java:1339)
- **Rolle:** Hacke, Basis `ItemHoe`, Material `toolAMETHYST`.
- **Werte:** Haltbarkeit 2000 (AmethystHoe.java:14); Stack 1 (L13); Tab Tools (L15). Kein Angriffsbonus (ItemHoe, siehe 0.1). `getDamageVsEntity` = 5 ist tot (L18-20). Kein `setHarvestLevel`-Aufruf (OreSpawnMain.java:1339).
- **Verhalten:** Vanilla-Pflügen (Grass/Dirt → Farmland).
- **Port 1.21.1:** `HoeItem` mit Tier AMETHYST.

### AmethystPickaxe
- IDs: `amethystpickaxe` „Amethyst Pickaxe“ (manifest; OreSpawnMain.java:1337)
- **Rolle:** Spitzhacke, Basis `ItemPickaxe`.
- **Werte:** Haltbarkeit 2000 (AmethystPickaxe.java:17); Angriff 2 + 11 = 13 (ItemPickaxe `fconst_2`); Effizienz 11; Harvest `pickaxe` 4 (OreSpawnMain.java:1337); Stack 1; Tab Tools. `weaponDamage = 12` samt beiden `getDamageVsEntity`-Varianten ist tot (L15, L21-27).
- **Verhalten:** Vanilla.
- **Port 1.21.1:** `PickaxeItem`; Level 4 → `incorrect_for_netherite_tool`.

### AmethystShovel
- IDs: `amethystshovel` „Amethyst Shovel“ (manifest; OreSpawnMain.java:1338)
- **Rolle:** Schaufel, Basis `ItemSpade`.
- **Werte:** Haltbarkeit 2000 (AmethystShovel.java:14); Angriff 1 + 11 = 12 (ItemSpade `fconst_1`); Harvest `shovel` 4 (OreSpawnMain.java:1338); Stack 1; Tab Tools. `getDamageVsEntity` = 5 ist tot.
- **Verhalten:** Vanilla (Grasspfad erst ab 1.8+, in 1.7.10 nicht vorhanden).
- **Port 1.21.1:** `ShovelItem`. Das Pfad-Anlegen erbt man in 1.21.1 automatisch – eine Abweichung vom Original, die man hinnehmen kann.

### AmethystSword
- IDs: `amethystsword` „Amethyst Sword“ (manifest; OreSpawnMain.java:1336)
- **Rolle:** Schwert, Basis `ItemSword`.
- **Werte:** Angriff 4 + 11 = 15 (ItemSword `ldc 4.0f`; OreSpawnMain.java:1182); Haltbarkeit 2000 (AmethystSword.java:19); Blockdauer 3500 Ticks (L36-38; Vanilla 72000); Stack 1; Tab Combat (L20). `weaponDamage = 18` (L17) ist tot.
- **Verhalten:** `hitEntity(…, EntityLiving, EntityLiving)` (L31-34) überschreibt nichts; es greift das Vanilla-`ItemSword.hitEntity`.
- **Port 1.21.1:** `SwordItem`. Blocken existiert in 1.21.1 für Schwerter nicht mehr, `getMaxItemUseDuration` entfällt.

---

## 2. Bertha-Familie und Spezialschwerter

### Bertha
- IDs:

| ID | Lang-Name | Feld | Material | Herkunft |
|---|---|---|---|---|
| `berthasmall` | Big Bertha | MyBertha | `toolBERTHA` | (OreSpawnMain.java:1313) |
| `slicesmall` | Slice | MySlice | `toolBERTHA` | (OreSpawnMain.java:1314) |
| `royalsmall` | Royal Guardian Sword | MyRoyal | `toolROYAL` | (OreSpawnMain.java:1315) |
| `hammysmall` | Attitude Adjuster | MyHammy | `toolHAMMY` | (OreSpawnMain.java:1316) |

- **Rolle:** Großschwert, Basis `ItemSword`. Jeder Schlag erzeugt zusätzlich ein unsichtbares Wurfgeschoss `BerthaHit`, das Flächenschaden verursacht.
- **Werte:**
  - Angriff: Bertha/Slice 4 + 496 = 500, Royal 4 + 746 = 750, Hammy 4 + 82 = 86 (0.1 + Material)
  - Haltbarkeit 9000 für alle vier (Bertha.java:18), überschreibt Royal 10000 und Hammy 2000
  - Blockdauer 9000 (L103-105); Stack 1 (L17); Tab Combat (L19)
  - `getMaterialName` „Uranium/Titanium“ (L94-96)
- **Verzauberungen:**
  - `onCreated` (Bertha.java:22-31): Royal → Unbreaking 5; Hammy → nichts; Bertha/Slice → Knockback 5, Bane of Arthropods 1, Fire Aspect 1
  - `onUpdate` ruft jeden Tick `onUsingTick` (L50-52). Ist Knockback 0 **und** Unbreaking ≤ 0, wird dasselbe Set nachgetragen (L33-48). Die Verzauberungen sind also nicht entfernbar.
- **Verhalten:**
  1. `onLeftClickEntity` (L54-67): Bei `BigBerthaPvp == 0` (Config, Default 0) gibt der Hook `true` zurück, der Forge-Angriff wird **abgebrochen** – gegen `EntityPlayer`, `Girlfriend`, `Boyfriend` und gezähmte `EntityTameable`.
  2. `onEntitySwing` (L69-92), nur serverseitig und nur für Spieler: `BerthaHit` wird bei `posX − 2.0·sin(yawHead)`, `posY + 1.55`, `posZ + 2.0·cos(yawHead)` erzeugt (L72-75), also 2 Blöcke vor dem Spieler. Die Bewegung wird ×2 multipliziert (L76-81). `setHitType(2)` für Royal, `setHitType(3)` für Hammy (L82-87); Bertha und Slice setzen keinen Typ (Default in `BerthaHit`: offen). Danach `stack.damageItem(1)` (L89). Rückgabe `false`: der normale Schwung läuft weiter.
  3. `hitEntity(…EntityLiving…)` (L98-101) ist tot; Vanilla-`ItemSword.hitEntity` greift. Pro Treffer mit Schwung also 1 (Schwung) + Vanilla-Verschleiß.
- **Client:** eigene `IItemRenderer` `RenderBertha`, `RenderSlice`, `RenderRoyal`, `RenderHammy` (calls_ClientProxyOreSpawn.txt:392-400) mit den Modellen `ModelBertha`, `ModelSlice` (auch für Royal), `ModelHammy` und den Texturen `textures/entity/berthatexture.png` 64×128, `slicetexture.png` 64×128, `royaltexture.png` 64×128, `attitudeadjustertexture.png` 128×256 (manifest item_renderer). Skalierung `renderSword: L4, L4, L4` (renderers_dump.txt:135-139).
- **Config:** `OreSpawnTWEAKS.BigBerthaPvp` = 0 (manifest); Materialschlüssel `Bertha_*`, `Royal_*`, `Attitude_*`.
- **Rezeptbezug:** keiner in der Klasse (Crafting in OreSpawnMain, laut Recherche Handle + Guard + Blade).
- **Port 1.21.1:**
  - `SwordItem` mit drei Tiers; Angriff 500/750 als Attribut (die Obergrenze von `ATTACK_DAMAGE` im sources-Jar prüfen)
  - Den Schwung-Hook serverseitig verankern: `onEntitySwing` in NeoForge prüfen, sonst Auslösung über den Swing-Packet-Handler bzw. `AttackEntityEvent`
  - PvP-Sperre über `onLeftClickEntity` bzw. `AttackEntityEvent` mit Abbruch
  - Renderer als `BlockEntityWithoutLevelRenderer` unter `com.swbr.orespawn.client`; Modelle aus `reference/jar/models/ModelBertha.json` u. a.
  - Die Nachtrag-Schleife gehört in `inventoryTick` mit Registry-Lookup

### BigHammer
- IDs: `bighammer` „Big Hammer“ (manifest; OreSpawnMain.java:1330), Material `toolAMETHYST`.
- **Rolle:** Schwert-Item (`ItemSword`) mit Hochschleuder-Effekt.
- **Werte:** Angriff 4 + 11 = 15; Haltbarkeit 9000 (BigHammer.java:19) statt Material 2000; Blockdauer 3000 (L36-38); Stack 1; Tab Combat. `weaponDamage = 15` (L17) wird nie gelesen. `getMaterialName` „AMETHYST“.
- **Verhalten:** `hitEntity` (echte Überschreibung, L27-34): serverseitig `target.addVelocity(0, |rand.nextFloat()·2/3|, 0)`, also 0 bis 0,667 nach oben (L30); dann `damageItem(1)` (L32). Die ungenutzte lokale Variable `var2 = 5` (L28) hat keine Wirkung.
- **Port 1.21.1:** `SwordItem.hurtEnemy` → `target.push(0, r, 0)` plus `hurtMarked = true`, damit der Client die Geschwindigkeit bekommt.

### EmeraldSword
- IDs: `emeraldsword` „Emerald Sword“ (OreSpawnMain.java:1320), `rosesword` „Rose Sword“ (OreSpawnMain.java:1361). Beide `toolEMERALD`, sonst identisch.
- **Werte:** Angriff 4 + 6 = 10; Haltbarkeit 1300 (EmeraldSword.java:21); Blockdauer 3000 (L47-49); Stack 1; Tab Combat. `weaponDamage = 15` ist tot.
- **Verhalten:** `onCreated`, `onUsingTick` und `onUpdate` sind leer überschrieben (L25-32) – das deaktiviert nichts, weil `ItemSword` dort nichts tut. `hitEntity(…EntityLiving…)` ist tot.
- **Port:** zwei Registrierungen desselben `SwordItem`.

### ExperienceSword
- IDs: `experiencesword` „Experience Sword“ (OreSpawnMain.java:1325), Material `toolEMERALD`.
- **Werte:** Angriff 4 + 6 = 10; Haltbarkeit 1400 (ExperienceSword.java:27); Blockdauer 3000 (L140-142); Stack 1; Tab Combat.
- **Verzauberungen:** `onCreated` Sharpness 2 + Unbreaking 3 (L31-34); `onUsingTick` trägt beides nach, sobald Sharpness ≤ 0 (L36-42), und wird aus `onUpdate` jeden Tick aufgerufen (L48).
- **Verhalten `onUpdate`** (L44-102), solange das Schwert irgendwo im Inventar liegt:
  1. Merkt sich die Server- und die Client-Welt in Instanzfeldern `worldObj`/`worldObjr` (L49-54). Das Item ist ein Singleton, das Feld also global.
  2. Mit Chance 1/60 pro Tick (L55) über die Ausrüstungsslots 1..4 (L60): Für jedes `ItemOreSpawnArmor` mit `get_armor_material() == 4` (Experience) je nach `armor_type` +1 XP mit Chance Helm 1/10 (L69-71), Brust 1/20 (L76-78), Beine 1/30 (L83-85), Stiefel 1/40 (L90-92). Portal-Partikel bei y + 1.5 / 1.25 / 0.75 / 0.25.
  3. `p` wird nur für Spieler gesetzt, `p.getEquipmentInSlot` läuft aber ohne Null-Prüfung (L61). Das ist ungefährlich, weil nur Spieler `onUpdate` auslösen.
- **Verhalten `hitEntity`** (L112-138):
  1. Ist das Ziel `EntityLiving` (Mobs; Spieler zählen nicht), bekommt der Spieler +10 XP (L119-124).
  2. Danach `i = experienceLevel / 2` (Ganzzahldivision) und, falls > 0, `target.attackEntityFrom(causePlayerDamage(p), i)` (L125-130) – auch gegen Spieler. Ob dieser zweite Treffer im selben Tick an den Unverwundbarkeits-Ticks scheitert: offen, Vanilla-Verhalten nicht im Repo geprüft.
  3. Portal-Partikel `i/2 + 1` Mal über die gespeicherte Client-Welt (L131-135); danach `damageItem(1)`.
- **Port 1.21.1:**
  - Die statischen Weltfelder entfallen, Partikel über `ServerLevel.sendParticles`
  - XP-Rieseln in `inventoryTick` mit Slot-Iteration über `EquipmentSlot`
  - Bonusschaden in `hurtEnemy` als eigener `DamageSource`
  - `get_armor_material()` wird zu einer Material-Identitätsprüfung gegen den registrierten Experience-`ArmorMaterial`-Holder

### FairySword
- IDs: `fairysword` „Fairy Sword“ (OreSpawnMain.java:1328), Material `toolEMERALD`.
- **Werte:** Angriff 4 + 6 = 10; Haltbarkeit 1300 (FairySword.java:20); Blockdauer 3000 (L59-61); Stack 1; Tab Combat. `weaponDamage = 15` wird nie gelesen.
- **Verhalten `hitEntity`** (L28-41): serverseitig `num = 1 + rand.nextInt(3)`, also 1–3 Feen (L31). Jede entsteht per `EntityList.createEntityByName("Fairy")` bei `target ± (r1−r2)·0.5` auf x/z und `y + r + 0.01` (L33), mit Zufalls-Yaw und `playLivingSound` (L43-57). Anschließend `setOwner(attacker)` (L35) und `damageItem(1)` (L39).
- **Port:** `EntityType` `orespawn:fairy` (manifest `fairy`/`Fairy`); `Fairy.setOwner` muss einen `LivingEntity` akzeptieren.

---

## 3. Crystal-Werkzeuge (vier Materialien je Klasse)

Gemeinsame Tabelle der Instanzen (manifest ctor_args; OreSpawnMain.java:1341-1360):

| Klasse | Crystal Wood (`toolCRYSTALWOOD`) | Kyanite (`toolCRYSTALSTONE`) | Pink Tourmaline (`toolCRYSTALPINK`) | Tiger's Eye (`toolTIGERSEYE`) |
|---|---|---|---|---|
| CrystalSword | `crystalwoodsword` | `crystalstonesword` | `crystalpinksword` | `tigerseye_sword` |
| CrystalPickaxe | `crystalwoodpickaxe` | `crystalstonepickaxe` | `crystalpinkpickaxe` | `tigerseye_pickaxe` |
| CrystalShovel | `crystalwoodshovel` | `crystalstoneshovel` | `crystalpinkshovel` | `tigerseye_shovel` |
| CrystalHoe | `crystalwoodhoe` | `crystalstonehoe` | `crystalpinkhoe` | `tigerseye_hoe` |
| CrystalAxe | `crystalwoodaxe` | `crystalstoneaxe` | `crystalpinkaxe` | `tigerseye_axe` |
| Haltbarkeit (Material) | 300 | 800 | 1100 | 1600 |
| Effizienz | 3 | 6 | 10 | 12 |
| Harvest-Level Material | 2 | 3 | 4 | 4 |

Keine dieser Klassen ruft `setMaxDamage` oder `setHarvestLevel`; es gelten die Materialwerte (OreSpawnMain.java:1177-1180). Lang-Namen laut manifest: „Crystal Wood …“, „Kyanite …“, „Pink Tourmaline …“, „Tiger's Eye …“.

### CrystalAxe
- IDs: siehe Tabelle (4).
- **Rolle:** `ItemAxe`, Stack 1 (CrystalAxe.java:12), Tab Tools.
- **Werte:** Angriff 3 + Material: 5 / 8 / 10 / 11.
- **Verhalten:** Vanilla. Das Harvest-Level entsteht nur über Forges Werkzeugklassen-Zuordnung von `ItemAxe` (Forge-1.7.10-Patch, im Repo nicht belegt).
- **Port:** `AxeItem` × 4 Tiers.

### CrystalHoe
- IDs: siehe Tabelle (4). `ItemHoe`, Stack 1 (CrystalHoe.java:12), Tab Tools. Kein Angriffsbonus. Verhalten Vanilla.
- **Port:** `HoeItem` × 4.

### CrystalPickaxe
- IDs: siehe Tabelle (4). `ItemPickaxe`, Stack 1 (CrystalPickaxe.java:12), Tab Tools. Angriff 2 + Material: 4 / 7 / 9 / 10. Verhalten Vanilla.
- **Port:** `PickaxeItem` × 4. Level 2 → Eisen-, 3 → Diamant-, 4 → Netherite-Tag.

### CrystalShovel
- IDs: siehe Tabelle (4).
- **Rolle:** **direkt `ItemTool`**, nicht `ItemSpade` (CrystalShovel.java:12; manifest superclass `ItemTool`).
- **Werte:** Grundschaden-Parameter `1.0f` (L17), also Angriff 1 + Material: 3 / 6 / 8 / 9. Stack 1 (L18); Tab Tools.
- **Wirksam gegen** (volle Material-Effizienz, L32): `grass`, `dirt`, `sand`, `gravel`, `snow`, `snow_layer`, `clay`, `farmland`, `mycelium`, `OreSpawnMain.CrystalGrass` (`crystalgrass`).
- **`canHarvestBlock`** (L22-24) liefert nur für `Blocks.snow` (Schneeblock) `true`. Vanilla-`ItemSpade` nennt zusätzlich `snow_layer`.
- **Offen:** Weil die Klasse kein `ItemSpade` ist, bekommt sie vermutlich keine Forge-Werkzeugklasse „shovel“. Der Forge-Patch liegt nicht im Repo.
- **Port 1.21.1:** Als `ShovelItem` portieren; der Tag `minecraft:mineable/shovel` deckt die Liste ab. `crystalgrass` in `mineable/shovel` aufnehmen. Wer die Schneeschicht-Abweichung 1:1 will, braucht eine eigene `Tool`-Regel.

### CrystalSword
- IDs: siehe Tabelle (4). `ItemSword`, Stack 1 (CrystalSword.java:15), Tab Combat.
- **Werte:** Angriff 4 + Material: 6 / 9 / 11 / 12. Blockdauer 300 Ticks (L19-21).
- **Verhalten:** Vanilla.
- **Port:** `SwordItem` × 4.

---

## 4. Emerald-Werkzeuge

### EmeraldAxe
- IDs: `emeraldaxe` „Emerald Axe“ (OreSpawnMain.java:1324).
- **Werte:** `ItemAxe`; Haltbarkeit 1300 (EmeraldAxe.java:17); Angriff 3 + 6 = 9; Effizienz 10; Stack 1; Tab Tools. Kein `setHarvestLevel`. `weaponDamage = 10` ist tot.
- **Port:** `AxeItem`, Tier EMERALD (Diamant-Tag).

### EmeraldHoe
- IDs: `emeraldhoe` „Emerald Hoe“ (OreSpawnMain.java:1323). `ItemHoe`; Haltbarkeit 1300 (EmeraldHoe.java:14); Stack 1; Tab Tools. `getDamageVsEntity` = 5 ist tot.
- **Port:** `HoeItem`.

### EmeraldPickaxe
- IDs: `emeraldpickaxe` „Emerald Pickaxe“ (OreSpawnMain.java:1321).
- **Werte:** `ItemPickaxe`; Haltbarkeit 1300 (EmeraldPickaxe.java:20); Angriff 2 + 6 = 8; Effizienz 10; Stack 1; Tab Tools.
- **Verhalten:** `onUpdate` → `onUsingTick` (L31-33): Fehlt Silk Touch (Stufe ≤ 0), wird **Silk Touch 1** nachgetragen (L24-29). Die Hacke hat damit dauerhaft Behutsamkeit, auch wenn sie nie gecraftet wurde (z. B. aus Loot).
- **Port 1.21.1:** Silk Touch in `inventoryTick` per Registry-Lookup setzen und beim Erzeugen gleich mitgeben, damit das Rezeptergebnis den Glint zeigt. Alternative ohne Tick-Kosten: `DataComponents.ENCHANTMENTS` als Default-Component im `Item.Properties`.

### EmeraldShovel
- IDs: `emeraldshovel` „Emerald Shovel“ (OreSpawnMain.java:1322). `ItemSpade`; Haltbarkeit 1300 (EmeraldShovel.java:14); Angriff 1 + 6 = 7; Stack 1; Tab Tools.
- **Port:** `ShovelItem`.

---

## 5. Materialien ohne Logik

### IngotTitanium
- IDs: `ingottitanium` „Titanium Ingot“ (OreSpawnMain.java:1277).
- **Rolle:** `Item`, Tab Materials (IngotTitanium.java:11), Stack 64 (Vanilla-Default). Keine Logik.
- **Port:** einfaches `Item`.

### IngotUranium
- IDs: `ingoturanium` „Uranium Ingot“ (OreSpawnMain.java:1276), `crystalpink_ingot` „Pink Tourmaline Ingot“ (OreSpawnMain.java:1285), `tigerseye_ingot` „Tiger's Eye Ingot“ (OreSpawnMain.java:1287).
- **Rolle:** `Item`, Tab Materials (IngotUranium.java:11). Keine Logik; die Klasse wird nur als Instanzträger wiederverwendet.
- **Port:** drei einfache Items.

### ItemCrystalSticks
- IDs: `crystalsticks` „Crystal Shards“ (OreSpawnMain.java:1390). `Item`, Tab Materials (ItemCrystalSticks.java:11). Keine Logik.

### ItemIrukandjiArrow
- IDs: `irukandjiarrow` „Irukandji Arrow“ (OreSpawnMain.java:1413). `Item`, Tab Combat (ItemIrukandjiArrow.java:11). Keine Logik; das Verschießen liegt in einer anderen Klasse (manifest-Entity `irukandji_arrow`/`IrukandjiArrow`).

### ItemSalt
- IDs (27, alle `Item`, Tab Materials, ItemSalt.java:11; OreSpawnMain.java:1374-1613): `mothscale` Moth Scale, `queenscale` The Queen Scale, `nightmarescale` Nightmare Scale, `emperorscorpionscale` Emperor Scorpion Scale, `basiliskscale` Basilisk Scale, `waterdragonscale` Water Dragon Scale, `peacockfeather` Peacock Feather, `jumpybugscale` Jumpy Bug Scale, `krakentooth` Kraken Tooth, `godzillascale` Mobzilla Scale, `greengoo` Green Goo, `bbhandle` Big Bertha Handle, `bbguard` Big Bertha Guard, `bbblade` Big Bertha Blade, `molenoidnose` Molenoid Nose, `seamonsterscale` Sea Monster Scale, `wormtooth` Worm Tooth, `trextooth` TRex Tooth, `caterkillerjaw` CaterKiller Jaws, `seavipertongue` Sea Viper Tongue, `vortexeye` Vortex Eye, `salt` Salt, `ruby` Ruby, `amethyst` Amethyst, `uranium_nugget` Uranium Nugget, `titanium_nugget` Titanium Nugget, `deadstinkbug` Dead Stink Bug (manifest).
- **Verhalten:** keines.
- **Port:** 27 einfache Items. Drei davon als `c:`-Tags anbieten: `c:gems/ruby`, `c:gems/amethyst`, `c:nuggets/uranium`. Nach Repo-Regel erst prüfen, ob andere Mods diese Tags schon füllen.

---

## 6. Nahrung

Vanilla-Mechanik: `ItemFood(heal, satMod, wolfFood)`; Sättigung = heal × satMod × 2, gedeckelt auf den Nahrungsstand (FoodStats-Bytecode, 0.1). Alle Einträge haben `wolfFood = false` (manifest ctor_args `0`). Die Spalte „Sättigung“ ist daraus **abgeleitet**.

### ItemPopcorn
- IDs und Werte (manifest ctor_args; OreSpawnMain.java:1504-1536):

| ID | Lang-Name | Nahrung | Mod | Sättigung (abgel.) |
|---|---|---|---|---|
| `popcorn` | Popcorn | 1 | 0.5 | 1.0 |
| `popcorn_buttered` | Buttered Popcorn | 2 | 0.6 | 2.4 |
| `popcorn_buttered_salted` | Buttered and Salted Popcorn | 3 | 0.75 | 4.5 |
| `popcorn_bag` | Bag of Popcorn | 10 | 1.25 | 25 → Deckel |
| `butter` | Butter | 1 | 0.5 | 1.0 |
| `corndog_cooked` | Corn Dog | 16 | 2.5 | 80 → Deckel |
| `corndog_raw` | Raw Corn Dog | 4 | 0.6 | 4.8 |
| `bacon` | Raw Bacon | 8 | 1.0 | 16 |
| `crabmeat` | Raw Crab Meat | 4 | 0.25 | 2.0 |
| `cheese` | Cheese | 4 | 0.5 | 4.0 |
| `salad` | Garden Salad | 10 | 0.95 | 19 |
| `blt_sandwich` | BLT Sandwich! | 12 | 0.95 | 22.8 → Deckel |
| `crabbypatty` | A Crabby Patty! | 16 | 2.35 | 75.2 → Deckel |
| `cookedpeacock` | Cooked Peacock | 12 | 1.4 | 33.6 → Deckel |
| `rawpeacock` | Raw Peacock | 6 | 0.7 | 8.4 |

- **Rolle:** `ItemFood` ohne Zusatz (ItemPopcorn.java:9-11). Tab: nicht gesetzt (Vanilla-`ItemFood` → Food-Tab).
- **Port:** `FoodProperties.Builder().nutrition(n).saturationModifier(m)`. Ob 1.21.1 den Modifier ebenso ×2 rechnet und auf den Nahrungsstand deckelt, im sources-Jar (`FoodConstants`/`FoodData`) gegenprüfen. „Salad“ und „Crabby Patty“ geben im Original keine Schüssel zurück.

### ItemGenericFish
- IDs (OreSpawnMain.java:1414-1419): `greenfish` Green Fish 3/0.5 (Sätt. 3.0), `bluefish` Blue Fish 4/0.4 (3.2), `pinkfish` Pink Fish 4/0.6 (4.8), `rockfish` Rock Fish 3/0.7 (4.2), `woodfish` Wood Fish 5/0.7 (7.0), `greyfish` Grey Fish 5/0.5 (5.0).
- **Verhalten:** `onFoodEaten` (ItemGenericFish.java:16-21): serverseitig mit `rand.nextInt(4) == 1`, also 1/4, **Hunger** (Potion `hunger`) für 20 Ticks, Amplifier 0.
- **Port:** `FoodProperties.effect(() -> new MobEffectInstance(HUNGER, 20, 0), 0.25f)`.

### ItemFireFish
- IDs: `firefish` „Fire Fish“ (OreSpawnMain.java:1371), Nahrung 4, Mod 0.6 (Sätt. 4.8).
- **Verhalten:** `setAlwaysEdible` (ItemFireFish.java:14). `onFoodEaten` serverseitig: **Fire Resistance** 1200 Ticks, Amplifier 0 (L20), immer.
- **Port:** `.alwaysEdible().effect(FIRE_RESISTANCE 1200/0, 1.0f)`.

### ItemLavaEel
- IDs: `lavaeel` „Lava Eel“ (OreSpawnMain.java:1373), Nahrung 2, Mod 0.6 (Sätt. 2.4).
- **Verhalten:** immer essbar (ItemLavaEel.java:14); Fire Resistance 600 Ticks, Amp 0 (L20).

### ItemSparkFish
- IDs: `sparkfish` „Spark Fish“ (OreSpawnMain.java:1392), Nahrung 1, Mod 0.2 (Sätt. 0.4).
- **Verhalten:** immer essbar (ItemSparkFish.java:14); Fire Resistance 100 Ticks, Amp 0 (L20).

### ItemCornCob
- IDs: `corn_seed` „Corn“ 6/0.75 (Sätt. 9.0), setzt `MyCornPlant1` (`corn_0`, BlockCorn) auf `farmland` (OreSpawnMain.java:1571). `quinoa` „Quinoa“ 7/0.85 (Sätt. 11.9), setzt `MyQuinoaPlant1` (`quinoa_0`, BlockQuinoa) auf `CrystalGrass` (OreSpawnMain.java:1576).
- **Rolle:** `ItemSeedFood` (essbar und pflanzbar), ohne eigene Logik (ItemCornCob.java:10-12).
- **Verhalten:** Vanilla-`ItemSeedFood.onItemUse` pflanzt auf der Oberseite, wenn der Boden die Pflanze trägt. `CrystalGrass.canSustainPlant` liefert **immer `true`** (CrystalGrass.java:58-60); `BlockQuinoa` akzeptiert als Boden Grass, Dirt, Farmland und CrystalGrass (BlockQuinoa.java:24).
- **Port:** `ItemNameBlockItem(cropBlock, props.food(...))`. Bodenregel über `CropBlock.mayPlaceOn` im Pflanzenblock, nicht im Item.

### ItemLettuce
- IDs: `lettuce_seed` „Lettuce“ 3/0.45 (Sätt. 2.7), Pflanze `MyLettucePlant1` (`lettuce_0`) auf `farmland` (OreSpawnMain.java:1586). `ItemSeedFood` ohne eigene Logik.
- **Port:** wie ItemCornCob.

### ItemRadish
- IDs: `radish` „Radish“ 2/0.45 (Sätt. 1.8), Pflanze `MyRadishPlant` (`radish_plant`) auf `farmland` (OreSpawnMain.java:1559). `rice` „Rice“ 5/0.65 (Sätt. 6.5), Pflanze `MyRicePlant` (`rice_plant`, BlockRice) auf `CrystalGrass` (OreSpawnMain.java:1565).
- **Port:** wie ItemCornCob.

---

## 7. Saatgut für Pflanzenblöcke

Alle vier sind `ItemSeeds(crop, Blocks.farmland)` ohne eigene Logik, Tab Decorations. `Blocks.field_150458_ak` = `farmland` (mcp fields.csv:1459). Das Verhalten der Critter-Pflanzen steckt in den Blockklassen.

### ItemButterflySeed
- IDs: `butterfly_seed` „Butterfly Plant“ → `butterfly_plant` (BlockButterflyPlant) (OreSpawnMain.java:1551; ItemButterflySeed.java:11-14).
- **Port:** `ItemNameBlockItem`.

### ItemFireflySeed
- IDs: `firefly_seed` „Firefly Plant“ → `firefly_plant` (BlockFireflyPlant) (OreSpawnMain.java:1557).

### ItemMosquitoSeed
- IDs: `mosquito_seed` „Mosquito Plant“ → `mosquito_plant` (BlockMosquitoPlant) (OreSpawnMain.java:1555).

### ItemMothSeed
- IDs: `moth_seed` „Moth Plant“ → `moth_plant` (BlockMothPlant) (OreSpawnMain.java:1553).

---

## 8. Block-Platzierer

### ItemPizza
- IDs: `pizza_item` „Pizza!“ (OreSpawnMain.java:1289: `setMaxStackSize(1)`, Tab Food, unlocalized `pizza`). Platziert `MyPizzaBlock` (`pizza`, BlockPizza).
- **Verhalten `onItemUse`** (ItemPizza.java:20-63), nachgebaut nach Vanilla-`ItemReed`:
  1. Ist der geklickte Block `Blocks.snow` mit `meta & 7 < 1`, gilt die Seite als oben und es gibt keinen Versatz (L22-24). **Abweichung:** geprüft wird der Schneeblock, nicht `snow_layer` wie bei ItemDuctTape.
  2. Bei Vine, Tallgrass oder Deadbush ersetzt der Block an Ort und Stelle; sonst Versatz um die Klickseite (L25-44).
  3. Abbruch bei fehlendem `canPlayerEdit` oder leerem Stack (L45-50).
  4. Bei `canPlaceEntityOnSide`: `onBlockPlaced`-Meta, `setBlock(…, 3)`, `onBlockPlacedBy`, `onPostBlockPlaced`, Schrittsound des Blocks mit Lautstärke (v+1)/2 und Tonhöhe ×0.8 (L51-58), Stack −1 (L59).
- **Port:** `BlockItem(pizzaBlock)`. Die Sonderregel für Schnee entfällt, weil `BlockPlaceContext` Replaceables regelt.

### ItemDuctTape
- IDs: `ducttape_item` „Duct Tape!“ (OreSpawnMain.java:1291: Stack 1, Tab Tools, unlocalized `ducttape`). Platziert `MyDuctTapeBlock` (`ducttape`, BlockDuctTape).
- **Verhalten:** identisch zu ItemPizza (ItemDuctTape.java:20-63). Unterschiede: die Schneeprüfung gilt `Blocks.snow_layer` (L22), und als Sound dient `stepSound.func_150496_b` = `getPlaceSound` (mcp methods.csv:1609) statt des Schrittsounds.
- **Port:** `BlockItem(ductTapeBlock)`.

### ItemExperienceTreeSeed
- IDs: `experiencetree_seed` „Experience Tree Seed“ (OreSpawnMain.java:1611). Stack 1 (ItemExperienceTreeSeed.java:15), Tab Decorations.
- **Verhalten `onItemUse`** (L19-36):
  - Server: Nur auf Grass, Dirt oder Farmland, sonst `false`. Dann `setBlock(x, y+1, z, MyExperiencePlant = experiencesapling, 0, 2)` **ohne Prüfung, ob oben Luft ist** – überschreibt also, was dort steht.
  - Client: 10 × `happyVillager`-Partikel.
  - Beide Seiten: Stack −1, außer im Creative-Modus.
- **Port:** Item mit `useOn`; Luftprüfung bewusst weglassen, wenn 1:1 gefordert.

### ItemRandomDungeon
- IDs: `randomdungeon` „Random Dungeon“ (OreSpawnMain.java:1603). Stack 1 (ItemRandomDungeon.java:21), Tab Redstone.
- **Werte:** Fortune 2 per `onCreated` (L25-27) und jeden Tick nachgetragen (L29-38), nur für den Glint.
- **Verhalten `onItemUse`** (L40-55): Der geklickte Block muss Stone, Cobblestone, Grass oder Dirt sein (L42), **und `clickedY ≥ 40`** (L45). Dann serverseitig `setBlock(x, y+1, z, MyDungeonSpawnerBlock = dungeonspawner, 0, 2)` (L49); Stack −1 außer Creative.
- **Port:** Item mit `useOn`. Den Glint über `DataComponents.ENCHANTMENT_GLINT_OVERRIDE = true` statt einer echten Fortune-Verzauberung setzen (wirkt identisch, spart den Tick-Lookup).

---

## 9. Wurf- und Schussitems

### ItemAcid
- IDs: `acid` „Acid“ (OreSpawnMain.java:1411). Stack 64 (ItemAcid.java:14), Tab Combat.
- **Verhalten `onItemRightClick`** (L18-27): Stack −1 außer Creative; Sound `random.bow`, Lautstärke 3.0, Tonhöhe 1.0; serverseitig `new Acid(world, player)`.
- **Port:** `EntityType` `orespawn:acid` (`Acid`, manifest), Item-`use` → `ThrowableItemProjectile.shootFromRotation`. Die Wurfgeschwindigkeit steckt im `Acid`-Konstruktor (nicht in diesem Stapel).

### ItemIceBall
- IDs: `iceball` „Ice Ball“ (OreSpawnMain.java:1395). Stack 64, Tab Combat (ItemIceBall.java:14-15).
- **Verhalten:** wie ItemAcid (Sound `random.bow` 3.0/1.0), erzeugt `IceBall` (L18-27). Ein Dispenser-Verhalten existiert in `MyDispenserBehaviorIceball` (nicht in diesem Stapel).

### ItemIrukandji
- IDs: `deadirukandji` „Dead Irukandji“ (OreSpawnMain.java:1412). Stack 64, Tab Combat.
- **Verhalten:** wie ItemAcid, erzeugt `DeadIrukandji` (ItemIrukandji.java:18-27).

### ItemLaserBall
- IDs: `laserball` „Robot Laser Charge“ (OreSpawnMain.java:1394). Stack 64, Tab Combat.
- **Verhalten:** Stack −1 außer Creative; Sound `fireworks.launch` 3.0/1.0; serverseitig `new LaserBall(world, player)` **ohne** `setSpecial` (ItemLaserBall.java:18-27).

### ItemRayGun
- IDs: `raygun` „A Freakin' Ray Gun!“ (OreSpawnMain.java:1408; unlocalized `RayGun`, Textur `raygun.png`). Stack 1, Haltbarkeit 50 (ItemRayGun.java:14-15), Tab Combat.
- **Verhalten `onItemRightClick`** (L19-42):
  1. Gilt `maxDamage − damage ≤ 1`, passiert nichts (L20-22). Nutzbar sind damit 49 Schüsse, der letzte Punkt bleibt stehen.
  2. Sound `fireworks.launch`, Lautstärke 3.5, Tonhöhe 0.5 (L23).
  3. Server: `LaserBall` mit `setSpecial()` (L28) an `posX − 1.0·sin(yawHead+45°)`, `posY + 1.55`, `posZ + 1.0·cos(yawHead+45°)` (L29), Bewegung ×3 (L30-35).
  4. Beide Seiten: `swingItem`, dann Rückstoß `addVelocity(cos(yawHead−90°)·1.5, 0.3, sin(yawHead−90°)·1.5)` (L39), also nach hinten.
  5. `damageItem(1)` (L40), keine Creative-Ausnahme.
- `getMaterialName` „Unknown“.
- **Port 1.21.1:**
  - Haltbarkeit über `durability(50)`
  - Der Rückstoß muss auf dem Client ankommen (Spielerbewegung ist clientseitig): entweder beidseitig anwenden wie im Original oder serverseitig mit `hurtMarked = true`
  - Die Nachlade-Logik (Rezept) liegt außerhalb der Klasse

### ItemRock
- IDs und Typnummern (OreSpawnMain.java:1396-1407; Typen ItemRock.java:24-59 und 75-110):

| ID | Lang-Name | Typ |
|---|---|---|
| `rocksmall` | Small Rock | 1 |
| `rock` | Big Rock | 2 |
| `rockred` | Flame Rock | 3 |
| `rockgreen` | Poison Rock | 4 |
| `rockblue` | Slowness Rock | 5 |
| `rockpurple` | Weakness Rock | 6 |
| `rockspikey` | Painful Rock | 7 |
| `rocktnt` | Explosive Rock | 8 |
| `rockcrystalred` | Flame Crystal | 9 |
| `rockcrystalgreen` | Poison Crystal | 10 |
| `rockcrystalblue` | Slowness Crystal | 11 |
| `rockcrystaltnt` | Explosive Crystal | 12 |

- **Rolle:** `Item`, Stack 64 (L14), Tab Combat.
- **Rechtsklick in die Luft** (L18-62): Stack −1 außer Creative; Sound `random.bow` 0.5 / `0.4/(rand·0.4+0.8)`; serverseitig `new EntityThrownRock(world, player, typ)`.
- **Rechtsklick auf Block** (L64-117), serverseitig:
  1. `x < 0` → `x+1`, `z < 0` → `z+1` (L66-71).
  2. `spawnCreature("Rock")` erzeugt `RockBase` (manifest `rock`). Liegt die Koordinate > 0, kommt +0.5 dazu, bei < 0 −0.5, bei genau 0 nichts (L123-134). Die Y-Position ist `y + 1.01 + 0.01` (L72, L135).
  3. Danach `placeRock(typ)` (L75-110) und `playLivingSound`.
  4. Stack −1 außer Creative, auch wenn das Entity nicht erzeugt wurde.
  - Randfehler: Bei x = 0 oder z = 0 steht der Stein auf der Blockkante.
- **Offen:** Die Wirkung der Typen 1–12 liegt in `EntityThrownRock` bzw. `RockBase`.
- **Port:** ein Item-Klasse mit Typparameter. Die Platzierung auf Blockmitte `pos.getX() + 0.5` rechnen; die Kantenverschiebung bei 0 nur übernehmen, wenn Bit-Treue verlangt ist.

### ItemShoes
- IDs (OreSpawnMain.java:1362-1366): `redheels` Red Heels (my_id 2), `blackheels` Black Heels (3), `slippers` Slippers (4), `boots` Boots (5), `gamecontroller` Game Controller (6).
- **Rolle:** `Item`, Stack 64 (ItemShoes.java:18), Tab Decorations.
- **Verhalten:** Stack −1 außer Creative; Sound `random.bow` 0.5 / `0.4/(rand·0.4+0.8)`; serverseitig `new Shoes(world, player, my_id)` (L22-31). Das Aussehen und die Wirkung pro ID liegen in `Shoes` (offen).
- **Port:** Wurf-Entity `orespawn:shoes` mit synchronisiertem Typfeld.

### ItemCreeperLauncher
- IDs: `creeperlauncher` „Creeper Launcher“ (OreSpawnMain.java:1388). Tab Redstone; `setMaxDamage(1)` (ItemCreeperLauncher.java:15), Stack 64 (Vanilla-Default).
- **Verhalten `onLeftClickEntity`** (L18-43), nur gegen `EntityCreeper`:
  1. 6 × je ein `smoke`-, `explode`- und `reddust`-Partikel, zufällig bei x/z ±1 und y +0.25..6.25 (L20-33).
  2. Sound `fireworks.launch`, Lautstärke 2.0, Tonhöhe 1.2 (L34).
  3. `creeper.addVelocity(0, 4.5, 0)` (L36).
  4. Stack −1 außer Creative; Rückgabe `true` bricht den Nahkampftreffer ab.
- **Port-Falle:** In 1.21.1 dürfen Items nicht gleichzeitig Haltbarkeit haben und stapelbar sein. `max_damage` weglassen (das Item wird nie beschädigt) und Stack 64 behalten. Hochschleudern serverseitig mit `hurtMarked = true`. Hook: `onLeftClickEntity` oder `AttackEntityEvent` (im sources-Jar prüfen).

### CritterCage
- IDs: 114 Instanzen (OreSpawnMain.java:5069-5182), alle `Item`, Stack 16 (CritterCage.java:19), Tab Misc (L20). Die Unterscheidung steckt im Konstruktorparameter `cage_id` (manifest ctor_args[1]).

| Registry-ID | Lang-Name | cage_id (manifest) | Ziel beim Freilassen | Klasse / Entity-ID | Herkunft |
|---|---|---|---|---|---|
| `cageempty` | Empty Critter Cage | 160 | leerer Kaefig, wirft `EntityCage` | – | (CritterCage.java:25) |
| `cagespider` | Caged Spider | 161 | Vanilla-ID 52 = `Spider` | vanilla | (CritterCage.java:55) |
| `cagebat` | Caged Bat | 162 | Vanilla-ID 65 = `Bat` | vanilla | (CritterCage.java:59) |
| `cagecow` | Caged Cow | 163 | Vanilla-ID 92 = `Cow` | vanilla | (CritterCage.java:63) |
| `cagepig` | Caged Pig | 164 | Vanilla-ID 90 = `Pig` | vanilla | (CritterCage.java:67) |
| `cagesquid` | Caged Squid | 165 | Vanilla-ID 94 = `Squid` | vanilla | (CritterCage.java:71) |
| `cagechicken` | Caged Chicken | 166 | Vanilla-ID 93 = `Chicken` | vanilla | (CritterCage.java:75) |
| `cagecreeper` | Caged Creeper | 167 | Vanilla-ID 50 = `Creeper` | vanilla | (CritterCage.java:79) |
| `cageskeleton` | Caged Skeleton | 168 | Vanilla-ID 51 = `Skeleton` | vanilla | (CritterCage.java:86) |
| `cagezombie` | Caged Zombie | 169 | Vanilla-ID 54 = `Zombie` | vanilla | (CritterCage.java:90) |
| `cageslime` | Caged Slime | 170 | Vanilla-ID 55 = `Slime` | vanilla | (CritterCage.java:94) |
| `cageghast` | Caged Ghast | 171 | Vanilla-ID 56 = `Ghast` | vanilla | (CritterCage.java:98) |
| `cagezombiepigman` | Caged ZombiePigman | 172 | Vanilla-ID 57 = `PigZombie` | vanilla | (CritterCage.java:102) |
| `cageenderman` | Caged Enderman | 173 | Vanilla-ID 58 = `Enderman` | vanilla | (CritterCage.java:106) |
| `cagecavespider` | Caged Cave Spider | 174 | Vanilla-ID 59 = `CaveSpider` | vanilla | (CritterCage.java:110) |
| `cagesilverfish` | Caged Silverfish | 175 | Vanilla-ID 60 = `Silverfish` | vanilla | (CritterCage.java:114) |
| `cagemagmacube` | Caged Magma Cube | 176 | Vanilla-ID 62 = `LavaSlime` | vanilla | (CritterCage.java:118) |
| `cagewitch` | Caged Witch | 177 | Vanilla-ID 66 = `Witch` | vanilla | (CritterCage.java:122) |
| `cagesheep` | Caged Sheep | 178 | Vanilla-ID 91 = `Sheep` | vanilla | (CritterCage.java:126) |
| `cagewolf` | Caged Wolf | 179 | Vanilla-ID 95 = `Wolf` | vanilla | (CritterCage.java:130) |
| `cagemooshroom` | Caged Mooshroom | 180 | Vanilla-ID 96 = `MushroomCow` | vanilla | (CritterCage.java:134) |
| `cageocelot` | Caged Ocelot | 181 | Vanilla-ID 98 = `Ozelot` | vanilla | (CritterCage.java:138) |
| `cageblaze` | Caged Blaze | 182 | Vanilla-ID 61 = `Blaze` | vanilla | (CritterCage.java:142) |
| `cagegirlfriend` | Caged Girlfriend | 183 | Name `"Girlfriend"` | `Girlfriend` / `girlfriend` | (CritterCage.java:170) |
| `cageboyfriend` | Caged Boyfriend | 215 | Name `"Boyfriend"` | `Boyfriend` / `boyfriend` | (CritterCage.java:174) |
| `cagewitherskeleton` | Caged Wither Skeleton | 188 | Vanilla-ID 51 = `Skeleton` (skelly_type=1, faellt in case 168 durch) | vanilla | (CritterCage.java:83) |
| `cageenderdragon` | Caged Ender Dragon | 184 | Vanilla-ID 63 = `EnderDragon` | vanilla | (CritterCage.java:146) |
| `cagesnowgolem` | Caged Snow Golem | 185 | Vanilla-ID 97 = `SnowMan` | vanilla | (CritterCage.java:150) |
| `cageirongolem` | Caged Iron Golem | 186 | Vanilla-ID 99 = `VillagerGolem` | vanilla | (CritterCage.java:154) |
| `cagewitherboss` | Caged Wither Boss | 187 | Vanilla-ID 64 = `WitherBoss` | vanilla | (CritterCage.java:158) |
| `cageredcow` | Caged Apple Cow | 189 | Name `"Apple Cow"` | `RedCow` / `apple_cow` | (CritterCage.java:178) |
| `cagegoldcow` | Caged Golden Apple Cow | 190 | Name `"Golden Apple Cow"` | `GoldCow` / `golden_apple_cow` | (CritterCage.java:182) |
| `cageenchantedcow` | Caged Enchanted Golden Apple Cow | 191 | Name `"Enchanted Golden Apple Cow"` | `EnchantedCow` / `enchanted_golden_apple_cow` | (CritterCage.java:186) |
| `cagemothra` | Caged MOTHRA | 208 | Name `"Mothra"` | `Mothra` / `mothra` | (CritterCage.java:190) |
| `cagealosaurus` | Caged Alosaurus | 209 | Name `"Alosaurus"` | `Alosaurus` / `alosaurus` | (CritterCage.java:194) |
| `cagecryolophosaurus` | Caged Cryosaurus | 210 | Name `"Cryolophosaurus"` | `Cryolophosaurus` / `cryolophosaurus` | (CritterCage.java:198) |
| `cagecamarasaurus` | Caged Camarasaurus | 211 | Name `"Camarasaurus"` | `Camarasaurus` / `camarasaurus` | (CritterCage.java:202) |
| `cagevelocityraptor` | Caged Velocity Raptor | 212 | Name `"Velocity Raptor"` | `VelocityRaptor` / `velocity_raptor` | (CritterCage.java:206) |
| `cagehydrolisc` | Caged Hydrolisc | 213 | Name `"Hydrolisc"` | `Hydrolisc` / `hydrolisc` | (CritterCage.java:210) |
| `cagebasilisc` | Caged Basilisk | 214 | Name `"Basilisk"` | `Basilisk` / `basilisk` | (CritterCage.java:214) |
| `cagedragonfly` | Caged Dragonfly | 220 | Name `"Dragonfly"` | `Dragonfly` / `dragonfly` | (CritterCage.java:218) |
| `cageemperorscorpion` | Caged Emperor Scorpion | 222 | Name `"Emperor Scorpion"` | `EmperorScorpion` / `emperor_scorpion` | (CritterCage.java:222) |
| `cagescorpion` | Caged Scorpion | 224 | Name `"Scorpion"` | `Scorpion` / `scorpion` | (CritterCage.java:226) |
| `cagecavefisher` | Caged Cave Fisher | 226 | Name `"CaveFisher"` | `CaveFisher` / `cave_fisher` | (CritterCage.java:230) |
| `cagespyro` | Caged Baby Dragon | 228 | Name `"Baby Dragon"` | `Spyro` / `baby_dragon` | (CritterCage.java:234) |
| `cagebaryonyx` | Caged Baryonyx | 230 | Name `"Baryonyx"` | `Baryonyx` / `baryonyx` | (CritterCage.java:238) |
| `cagegammametroid` | Caged WTF? | 232 | Name `"WTF?"` | `GammaMetroid` / `wtf` | (CritterCage.java:242) |
| `cagecockateil` | Caged Bird | 234 | Name `"Bird"` | `Cockateil` / `bird` | (CritterCage.java:246) |
| `cagekyuubi` | Caged Kyuubi | 236 | Name `"Kyuubi"` | `Kyuubi` / `kyuubi` | (CritterCage.java:250) |
| `cagealien` | Caged Alien | 238 | Name `"Alien"` | `Alien` / `alien` | (CritterCage.java:254) |
| `cageattacksquid` | Caged Attack Squid | 240 | Name `"Attack Squid"` | `AttackSquid` / `attack_squid` | (CritterCage.java:258) |
| `cagewaterdragon` | Caged Water Dragon | 242 | Name `"Water Dragon"` | `WaterDragon` / `water_dragon` | (CritterCage.java:262) |
| `cagecephadrome` | Caged Cephadrome | 248 | Name `"Cephadrome"` | `Cephadrome` / `cephadrome` | (CritterCage.java:274) |
| `cagekraken` | Caged Kraken | 244 | Name `"The Kraken"` | `Kraken` / `the_kraken` | (CritterCage.java:266) |
| `cagelizard` | Caged Lizard | 246 | Name `"Lizard"` | `Lizard` / `lizard` | (CritterCage.java:270) |
| `cagedragon` | Caged Dragon | 250 | Name `"Dragon"` | `Dragon` / `dragon` | (CritterCage.java:278) |
| `cagebee` | Caged Bee | 252 | Name `"Bee"` | `Bee` / `bee` | (CritterCage.java:282) |
| `cagehorse` | Caged Horse | 253 | Vanilla-ID 100 = `EntityHorse` | vanilla | (CritterCage.java:162) |
| `cagefirefly` | Caged Firefly | 255 | Name `"Firefly"` | `Firefly` / `firefly` | (CritterCage.java:286) |
| `cagechipmunk` | Caged Chipmunk | 256 | Name `"Chipmunk"` | `Chipmunk` / `chipmunk` | (CritterCage.java:290) |
| `cagegazelle` | Caged Gazelle | 257 | Name `"Gazelle"` | `Gazelle` / `gazelle` | (CritterCage.java:294) |
| `cageostrich` | Caged Ostrich | 258 | Name `"Ostrich"` | `Ostrich` / `ostrich` | (CritterCage.java:298) |
| `cagetrooper` | Caged Jumpy Bug | 259 | Name `"Jumpy Bug"` | `TrooperBug` / `jumpy_bug` | (CritterCage.java:302) |
| `cagespit` | Caged Spit Bug | 260 | Name `"Spit Bug"` | `SpitBug` / `spit_bug` | (CritterCage.java:306) |
| `cagestink` | Caged Stink Bug | 261 | Name `"Stink Bug"` | `StinkBug` / `stink_bug` | (CritterCage.java:310) |
| `cagecreepinghorror` | Caged Creeping Horror | 268 | Name `"Creeping Horror"` | `CreepingHorror` / `creeping_horror` | (CritterCage.java:314) |
| `cageterribleterror` | Caged Terrible Terror | 269 | Name `"Terrible Terror"` | `TerribleTerror` / `terrible_terror` | (CritterCage.java:318) |
| `cagecliffracer` | Caged Cliff Racer | 270 | Name `"Cliff Racer"` | `CliffRacer` / `cliff_racer` | (CritterCage.java:322) |
| `cagetriffid` | Caged Triffid | 271 | Name `"Triffid"` | `Triffid` / `triffid` | (CritterCage.java:326) |
| `cagenightmare` | Caged Nightmare | 272 | Name `"Nightmare"` | `PitchBlack` / `nightmare` | (CritterCage.java:330) |
| `cagelurkingterror` | Caged Lurking Terror | 273 | Name `"Lurking Terror"` | `LurkingTerror` / `lurking_terror` | (CritterCage.java:334) |
| `cagesmallworm` | Caged Small Worm | 281 | Name `"Small Worm"` | `WormSmall` / `small_worm` | (CritterCage.java:338) |
| `cagemediumworm` | Caged Medium Worm | 282 | Name `"Medium Worm"` | `WormMedium` / `medium_worm` | (CritterCage.java:346) |
| `cagelargeworm` | Caged Large Worm | 283 | Name `"Large Worm"` | `WormLarge` / `large_worm` | (CritterCage.java:342) |
| `cagecassowary` | Caged Cassowary | 284 | Name `"Cassowary"` | `Cassowary` / `cassowary` | (CritterCage.java:350) |
| `cagecloudshark` | Caged Cloud Shark | 285 | Name `"Cloud Shark"` | `CloudShark` / `cloud_shark` | (CritterCage.java:354) |
| `cagegoldfish` | Caged Gold Fish | 286 | Name `"Gold Fish"` | `GoldFish` / `gold_fish` | (CritterCage.java:358) |
| `cageleafmonster` | Caged Leaf Monster | 287 | Name `"Leaf Monster"` | `LeafMonster` / `leaf_monster` | (CritterCage.java:362) |
| `cageenderknight` | Caged Ender Knight | 296 | Name `"Ender Knight"` | `EnderKnight` / `ender_knight` | (CritterCage.java:366) |
| `cageenderreaper` | Caged Ender Reaper | 297 | Name `"Ender Reaper"` | `EnderReaper` / `ender_reaper` | (CritterCage.java:370) |
| `cagebeaver` | Caged Beaver | 300 | Name `"Beaver"` | `Beaver` / `beaver` | (CritterCage.java:374) |
| `cageurchin` | Caged Crystal Urchin | 323 | Name `"Crystal Urchin"` | `Urchin` / `crystal_urchin` | (CritterCage.java:378) |
| `cageflounder` | Caged Flounder | 319 | Name `"Flounder"` | `Flounder` / `flounder` | (CritterCage.java:382) |
| `cageskate` | Caged Skate | 322 | Name `"Skate"` | `Skate` / `skate` | (CritterCage.java:386) |
| `cagerotator` | Caged Rotator | 313 | Name `"Rotator"` | `Rotator` / `rotator` | (CritterCage.java:390) |
| `cagepeacock` | Caged Peacock | 315 | Name `"Peacock"` | `Peacock` / `peacock` | (CritterCage.java:394) |
| `cagefairy` | Caged Fairy | 316 | Name `"Fairy"` | `Fairy` / `fairy` | (CritterCage.java:398) |
| `cagedungeonbeast` | Caged Dungeon Beast | 317 | Name `"Dungeon Beast"` | `DungeonBeast` / `dungeon_beast` | (CritterCage.java:402) |
| `cagevortex` | Caged Vortex | 314 | Name `"Vortex"` | `Vortex` / `vortex` | (CritterCage.java:406) |
| `cagerat` | Caged Rat | 318 | Name `"Rat"` | `Rat` / `rat` | (CritterCage.java:410) |
| `cagewhale` | Caged Whale | 320 | Name `"Whale"` | `Whale` / `whale` | (CritterCage.java:414) |
| `cageirukandji` | Caged Irukandji | 321 | Name `"Irukandji"` | `Irukandji` / `irukandji` | (CritterCage.java:418) |
| `cagetrex` | Caged T. Rex | 345 | Name `"T. Rex"` | `TRex` / `t_rex` | (CritterCage.java:422) |
| `cagehercules` | Caged Hercules Beetle | 346 | Name `"Hercules Beetle"` | `HerculesBeetle` / `hercules_beetle` | (CritterCage.java:426) |
| `cagemantis` | Caged Mantis | 347 | Name `"Mantis"` | `Mantis` / `mantis` | (CritterCage.java:430) |
| `cagestinky` | Caged Stinky | 348 | Name `"Stinky"` | `Stinky` / `stinky` | (CritterCage.java:434) |
| `cageeasterbunny` | Caged Easter Bunny | 150 | Name `"Easter Bunny"` | `EasterBunny` / `offen (fehlt im Manifest)` | (CritterCage.java:438) |
| `cagecaterkiller` | Caged CaterKiller | 151 | Name `"CaterKiller"` | `CaterKiller` / `cater_killer` | (CritterCage.java:442) |
| `cagemolenoid` | Caged Molenoid | 152 | Name `"Molenoid"` | `Molenoid` / `molenoid` | (CritterCage.java:446) |
| `cageseamonster` | Caged Sea Monster | 153 | Name `"Sea Monster"` | `SeaMonster` / `sea_monster` | (CritterCage.java:450) |
| `cageseaviper` | Caged Sea Viper | 154 | Name `"Sea Viper"` | `SeaViper` / `sea_viper` | (CritterCage.java:454) |
| `cageleon` | Caged Leonopteryx | 357 | Name `"Leonopteryx"` | `Leon` / `leonopteryx` | (CritterCage.java:458) |
| `cagehammerhead` | Caged Hammerhead | 359 | Name `"Hammerhead"` | `Hammerhead` / `hammerhead` | (CritterCage.java:462) |
| `cagerubberducky` | Caged Rubber Ducky | 361 | Name `"Rubber Ducky"` | `RubberDucky` / `rubber_ducky` | (CritterCage.java:466) |
| `cagecrystalcow` | Caged Crystal Cow | 216 | Name `"Crystal Apple Cow"` | `CrystalCow` / `crystal_apple_cow` | (CritterCage.java:470) |
| `cagevillager` | Caged Villager | 217 | Vanilla-ID 120 = `Villager` | vanilla | (CritterCage.java:166) |
| `cagecriminal` | Caged Criminal | 218 | Name `"Criminal"` | `BandP` / `criminal` | (CritterCage.java:474) |
| `cagebrutalfly` | Caged Brutalfly | 373 | Name `"Brutalfly"` | `Brutalfly` / `brutalfly` | (CritterCage.java:478) |
| `cagenastysaurus` | Caged Nastysaurus | 374 | Name `"Nastysaurus"` | `Nastysaurus` / `nastysaurus` | (CritterCage.java:482) |
| `cagepointysaurus` | Caged Pointysaurus | 375 | Name `"Pointysaurus"` | `Pointysaurus` / `pointysaurus` | (CritterCage.java:486) |
| `cagecricket` | Caged Cricket | 376 | Name `"Cricket"` | `Cricket` / `cricket` | (CritterCage.java:490) |
| `cagefrog` | Caged Frog | 377 | Name `"Frog"` | `Frog` / `frog` | (CritterCage.java:494) |
| `cagespiderdriver` | Caged Spider Driver | 382 | Name `"Spider Driver"` | `SpiderDriver` / `spider_driver` | (CritterCage.java:498) |
| `cagecrab` | Caged Crab | 384 | Name `"Crab"` | `Crab` / `crab` | (CritterCage.java:502) |

- **Verhalten `onItemRightClick`** (L23-35), nur beim leeren Käfig (`cage_id == CageEmpty.cage_id`, also 160):
  1. Stack −1 außer Creative.
  2. Sound `random.bow` 0.5 / `0.4/(rand·0.4+0.8)`.
  3. Serverseitig `new EntityCage(world, player, 160)`.
  - Die Fangregeln stehen in `EntityCage` (nicht in diesem Stapel, offen).
- **Verhalten `onItemUse`** (L37-526), nur bei gefüllten Käfigen; der leere Käfig liefert `false`, danach greift der Rechtsklick:
  1. 6 × je ein `smoke`-, `explode`- und `reddust`-Partikel bei Block + (0.5, 1.25, 0.5) (L42-46); Sound `random.explode` 1.0/1.5 (L47). Beides läuft vor der Server-Prüfung.
  2. Server: `switch (cage_id)` setzt entweder eine Vanilla-`entityID` oder einen `name` (Tabelle oben). **`case 188` (Wither Skeleton) setzt `skelly_type = 1` und fällt in `case 168` durch** (L83-89).
  3. `spawnCreature(world, id, name, x+0.5, y+1.1, z+0.5)` (L509):
     - Ist `name == null`, erzeugt `EntityList.createEntityByID`, sonst `createEntityByName` (L530-535)
     - Zufalls-Yaw 0..360 (L537)
     - Bei ID 100 (Pferd) oder 120 (Villager) zusätzlich `onSpawnWithEgg(null)`, also Zufallsvariante bzw. -beruf (L538-541)
     - Spawnen, dann `playLivingSound`
  4. Existiert das Entity, droppt es selbst **einen leeren Käfig** (`ent.dropItem(CageEmpty, 1)`, L511). Beim Skelett mit `skelly_type != 0` folgt `setSkeletonType(1)` (L512-515). Trägt der Käfig einen Anzeigenamen, wird er zum `customNameTag` (L516-518).
  5. Stack −1 außer Creative (L520-522), **auch wenn das Entity `null` war** – dann gehen Käfig und Inhalt verloren.
- **Port 1.21.1:**
  - Der leere Käfig ist ein Wurf-Item; die gefüllten Käfige als **eine Item-Klasse mit `EntityType`-Parameter** (Supplier) statt `cage_id`-Switch
  - Vanilla-Abbildung: 51 + skelly 1 → `EntityType.WITHER_SKELETON`; 98 `Ozelot` → `OCELOT`; 100 → `HORSE` (die Varianten Esel/Maultier waren in 1.7.10 Pferdetypen, die Entscheidung ist offen); 120 → `VILLAGER` mit `finalizeSpawn(…SPAWN_EGG…)`
  - `EnderDragon` außerhalb des Endes und `WitherBoss` ohne Aufladephase wie im Original direkt spawnen
  - Den Namen über `DataComponents.CUSTOM_NAME` übernehmen
  - Die Registry-ID `cagebasilisc` (Tippfehler im Original) 1:1 beibehalten

### ItemElevator
- IDs: `elevator` „Hoverboard“ (OreSpawnMain.java:1566). Stack 1 (ItemElevator.java:14), Tab Transport.
- **Verhalten `onItemUse`** (L18-29): serverseitig `EntityList.createEntityByName("Hoverboard")` → `Elevator` (manifest `hoverboard`), Position `(x+0.5, y+1.2, z+0.5)` unabhängig von der Klickseite, Zufalls-Yaw; Stack −1 außer Creative. Kein Null-Check (L22-23).
- **Port:** Item-`useOn` → `EntityType` `orespawn:hoverboard`.

---

## 10. Nutzwerkzeuge

### ExperienceCatcher
- IDs: `experiencecatcher` „Experience Orb Catcher“ (OreSpawnMain.java:1610). Stack 16 (ExperienceCatcher.java:18), Tab Tools.
- **Verhalten `onItemUse`** (L22-70):
  1. `swingItem`; ein Debug-`System.out.printf` mit allen Parametern (L24) gehört nicht in den Port.
  2. Server: Suchbox `[x − 0.5 + hitX, y, z − 0.5 + hitZ] … [x + 0.5 + hitX, y + 2, z + 0.5 + hitZ]` (L26).
  3. Pro XP-Orb in der Box (L28-60): Orbs mit Wert < 3 werden übersprungen; bei `rand.nextInt(5) == 1` (1/5) ebenfalls. Sonst verschwindet der Orb (sein XP geht verloren) und bei `(x + hitX, y + 1, z + hitZ)` droppen 1 × `experience_bottle`, 1 × `string`, 1 × `stick`. Stack −1 außer Creative, Ende.
  4. Wird kein Orb umgewandelt: ein neuer ExperienceCatcher droppt und der Stack sinkt um 1, **auch im Creative-Modus** (L61-67). Netto bleibt der Bestand gleich, das Item liegt danach aber am Boden.
- `onItemRightClick` schwingt nur den Arm (L72-75).
- **Recherche-Abgleich:** 03-items.md nennt „80 % Chance“; das deckt sich mit 4/5 im Code.
- **Port:** Item-`useOn`, `level.getEntitiesOfClass(ExperienceOrb.class, AABB)`.

### ItemSifter
- IDs: `sifter` „Sifter“ (OreSpawnMain.java:1420). Stack 1, Haltbarkeit 600 (ItemSifter.java:17-19), Tab Decorations.
- **Verhalten `onItemUse`** (L27-465), nur serverseitig (der Client liefert sofort `true`):
  1. Ist der Block **über** dem geklickten `water` oder `flowing_water`, gilt der geklickte Block als Wasser (L31-38). Direkt angeklicktes `flowing_water` zählt nicht.
  2. Pro Klick **ein** Wurf aus der Tabelle des Blocktyps. Ein Treffer droppt ein Item (Menge 1, Meta 0) bei `x + rand(2) − rand(2) + 0.5`, `y + 1.1`, `z + rand(2) − rand(2) + 0.5` (L22-25).
  3. **Immer** `damageItem(1)` (L463), auch wenn kein Item fällt und auf Stein.
- **Wasser**, `rand.nextInt(160)` (L40-222):

| Wurf | Drop |
|---|---|
| 0, 34 | `fish` (roher Fisch) |
| 1–6 | `greenfish`, `bluefish`, `pinkfish`, `rockfish`, `woodfish`, `greyfish` |
| 7, 14, 35 | `glass_bottle` |
| 8, 26 | `iron_ingot` |
| 9, 27 | `gold_nugget` |
| 10–13, 30–33 | `redheels`, `blackheels`, `slippers`, `boots` |
| 15, 36 | `bone` |
| 16, 37 | `stone` |
| 17, 39 | `bucket` |
| 18, 40 | `water_bucket` |
| 19 | 1/3 `emerald`, sonst `gravel` |
| 20 | 1/3 `ruby`, sonst `gravel` |
| 21 | 1/3 `amethyst`, sonst `gravel` |
| 22 | `mothscale` |
| 23 | `uranium_nugget` |
| 24 | `titanium_nugget` |
| 25 | 1/2 `diamond`, sonst `gravel` |
| 28 | `redstone` |
| 29 | `coal` |
| 38 | `stone_button` |
| 41–159 | nichts |

- **Sand**, `nextInt(60)` (L224-284): 0 `iron_horse_armor`, 1 `shears`, 2 `carrot_on_a_stick`, 3 `poisonous_potato`, 4 `item_frame`, 5 `bone`, 6 `compass`, 7 `glass_bottle`, 8 `saddle`, 9 `iron_helmet`, 10 `iron_chestplate`, 11 `iron_leggings`, 12 `iron_boots`, 13 `sand`; 14–59 nichts.
- **Gravel**, `nextInt(60)` (L285-337): 0 `flint`, 1 `salt`, 2 `flint_and_steel`, 3 `spider_eye`, 4 `item_frame`, 5 `feather`, 6 `string`, 7 `glass_bottle`, 8 `lead`, 9 `name_tag`, 10 `sand`, 11 `gravel`; 12–59 nichts.
- **Dirt**, `nextInt(60)` (L338-398): 0 `string`, 1 `salt`, 2 `shears`, 3 `stick`, 4 `bowl`, 5 `flower_pot`, 6 `sign`, 7 `brick`, 8 `paper`, 9 `bone`, 10 `glass_bottle`, 11 `sand`, 12 `gravel`, 13 `dirt`; 14–59 nichts.
- **Grass**, `nextInt(60)` (L399-462):
  - 0 `yellow_flower`, 1 `red_flower`, 2 `flower_pink`, 3 `flower_blue`, 4 `flower_black`
  - **5 `flower_scary` und durch fehlendes `break` zusätzlich `wheat`** (L422-428)
  - 6 `wheat`, 7 `pumpkin_seeds`, 8 `melon_seeds`, 9 `carrot`, 10 `potato`, 11 `deadbush`, 12 `gravel`, 13 `dirt`, 14 `grass`; 15–59 nichts
- `getMaterialName` „Unknown“ (L467-469).
- **Port 1.21.1:** Die fünf Tabellen als Loot-Tables `orespawn:gameplay/sifter/{water,sand,gravel,dirt,grass}` mit Gewicht 1 je Eintrag und einem leeren Rest. Den Durchfall-Doppeldrop bei Wurf 5 bewusst als Eintrag mit zwei Items abbilden. Blumen: `red_flower` Meta 0 → `poppy`, `yellow_flower` → `dandelion`.

### ItemNetherLost
- IDs: `netherlost` „Nether Tracker“ (OreSpawnMain.java:1389). Stack 1, Haltbarkeit 3000 (ItemNetherLost.java:17-18), Tab Decorations. Die Haltbarkeit wird im Code nie verbraucht.
- **Werte:** Sharpness 2 per `onCreated` (L22-24), jeden Tick nachgetragen (L26-31). In 1.7.10 wirkt Sharpness auf jedes gehaltene Item; der Tracker ist also nebenbei eine Waffe.
- **Verhalten `onUpdate`** (L33-56), jeden Tick im Inventar, auf **beiden** Seiten: Ist der Halter ein Spieler, hält er in der Hand irgendein `ItemNetherLost`, und liegt der Spieler in Dimension −1, dann wird der Block bei `((int)posX, (int)posY − 1, (int)posZ)` von `netherrack` zu `quartz_block` (L47-51).
  - Randfehler: Der `(int)`-Cast rundet bei negativen Koordinaten zur Null hin, der Block liegt dann um 1 versetzt.
- Blockdauer 3000 (L58-60) ohne Wirkung (kein Use-Action).
- **Port:** `inventoryTick` nur serverseitig mit `BlockPos` aus `player.blockPosition().below()` (korrigiert den Cast-Fehler; wer Bit-Treue will, notiert die Abweichung). Die Haltbarkeit weglassen oder behalten; sie ist folgenlos.

### ItemMinersDream
- IDs: `minersdream` „Miner's Dream“ (OreSpawnMain.java:1588). Stack 16 (ItemMinersDream.java:16), Tab Redstone.
- **Richtungsbestimmung** (identisch in InstantGarden, InstantShelter):
  - `pposx = (int)(posX + 0.99·dirx)` mit `dirx = −1` bei negativem Klick-x, analog z (L30-38)
  - Der Klick muss in derselben Zeile oder Spalte wie der Spieler liegen, sonst `false` (L39-41)
  - Die Richtung ergibt sich aus dem Vorzeichen von `klick − spieler` auf genau einer Achse, sonst `false` (L45-62)
- **Werte:** Höhe 5, halbe Breite 5 (Tunnel 11 breit), Länge 64, Fackelabstand 5 (L25-28).
- **Verhalten** (serverseitig nach Sound `random.explode` 1.0/1.5, L63):
  1. Für i = 0..4, k = 0..63, j = −5..5 an `(x + k·dx + j·dz, y + i, z + k·dz + j·dx)`, wobei y die Füße des Spielers sind: nur `stone`, `dirt`, `gravel`, Wasser, Lava (fließend/still), `netherrack`, `end_stone` und `crystalstone` werden zu Luft (L72-74). **Erze und alle anderen Blöcke bleiben stehen** – das ist der Sinn des Items.
  2. Decke bei i = 4 (y + 5): nicht-Luft zählen (L76-79). Ist dort Luft, Gravel, Sand oder Flüssigkeit, wird `cobblestone` gesetzt, in `DimensionID5` = Dimension-Crystal (BaseDimensionID 80 + 4, OreSpawnMain.java:1270; manifest) stattdessen `crystalstone` (L80-86). War die ganze Deckenreihe vorher Luft (`solid_count == 0`), wird sie wieder zu Luft (L90-94); unter freiem Himmel entsteht so kein Dach.
  3. Fackeln bei k = 0, 5, …, 60 (13 Positionen) auf der Mittellinie: Ist der Boden (y−1) `stone`, `dirt`, `gravel`, `netherrack`, `end_stone` oder `bedrock` und die Stelle Luft → `extremetorch`; auf `crystalstone` → `crystaltorch` (L97-105).
  4. Stack −1 außer Creative. Alle `setBlock` mit Flag 2 (keine Nachbar-Updates).
- **Port 1.21.1:**
  - Serverseitige Schleife, 64 × 11 × 6 Blöcke über mehrere Chunks
  - Flüssigkeiten: `level.setBlock(pos, AIR, 2)` hinterlässt keine Flussupdates, wie im Original
  - Die Dimensionsprüfung wird `level.dimension() == OreSpawnDimensions.CRYSTAL`
  - Die Blockliste `stone` in 1.21.1 um `deepslate` erweitern? Nein – 1:1 heißt nur `stone`. Die Entscheidung als Port-Notiz festhalten.

### InstantGarden
- IDs: `instantgarden` „Instant Survival Garden“ (OreSpawnMain.java:1599). Stack 16 (InstantGarden.java:16), Tab Redstone.
- **Richtung:** wie ItemMinersDream (L29-61). Startpunkt ist der geklickte Block (x, z) auf Fußhöhe des Spielers.
- **Werte:** Höhe 10, halbe Breite 7 (15 breit), Länge 18 (L26-28).
- **Verhalten** (Sound `random.explode` 1.0/1.5, dann serverseitig):
  1. Räumen: i = 0..9, k = 0..17, j = −7..7 → Luft; bei i = 0 wird der Block darunter (y−1) zu `grass` (L66-75).
  2. Beete für k = 1..16; die Spalte ergibt sich aus dem Zähler `i` = 0..14 über j = −7..7 (L76-134):

| i | y−2 | y−1 | y |
|---|---|---|---|
| 0 | – | grass | – |
| 1 | – | farmland | `radish_plant` |
| 2 | – | farmland | `lettuce_0` |
| 3 | – | farmland | `carrots` |
| 4 | cobblestone | water | – |
| 5 | – | farmland | `potatoes` |
| 6 | – | farmland | `wheat` |
| 7 | – | farmland | `tomato_0` |
| 8 | cobblestone | water | – |
| 9 | – | farmland | `corn_0` |
| 10 | – | farmland | `strawberry_plant` |
| 11 | cobblestone | sand | `reeds` |
| 12 | cobblestone | water | – |
| 13 | – | farmland | `melon_stem` |
| 14 | – | grass | – |

  Alle Pflanzen Meta 0, alle `setBlock` mit Flag 2. Die Ränder k = 0 und k = 17 bleiben Gras.
  3. Stack −1 außer Creative.
- **Port:** Als Strukturschreiber mit festen `BlockState`s (Alter 0); `reeds` → `sugar_cane`. Wasser mit Flag 2 setzen, damit es nicht fließt.

### InstantShelter
- IDs: `instantshelter` „Instant Survival Shelter“ (OreSpawnMain.java:1598). Stack 16 (InstantShelter.java:17), Tab Redstone.
- **Richtung:** wie oben; zusätzlich `stuffdir` = 3 (−x), 2 (+x), 5 (−z), 4 (+z) als Blickrichtungs-Meta (L46-61). **Mittelpunkt ist der Spieler** (x = pposx, z = pposz), der Boden liegt bei `y = pposy − 1` (L44, L68-69).
- **Werte:** `width = length = height = 3` (L28-30), also 7 × 7 Grundfläche und 5 Ebenen.
- **Verhalten** (Sound `random.explode` 1.0/1.5; serverseitig, `setBlock` Flag 3):
  1. i, j = −3..3, k = 0..4: k = 4 Dach `planks`; k = 0 Boden `cobblestone`; Rand bei k = 3 `glass`; Rand bei k = 1..2 `planks`, außer der Tür an `i == dx·3 && j == dz·3` (Mitte der Wand in Blickrichtung) = Luft; innen Luft (L74-99).
  2. Einrichtung an `(x + i·dx + j·dz, y+1, z + i·dz + j·dx)` mit j = 2: i = 2 `furnace` (Meta `stuffdir`), i = 1 `crafting_table`, i = 0 `chest` (Meta `stuffdir`) (L100-109).
  3. Truheninhalt (L112-125): Slot 0 `compass`, 1 `map` (Typ `ItemEmptyMap`, `joined.srg` `acl`), 2 `porkchop` ×8, 3 `torch` ×32, 4 `coal` ×16, 5 `bed`, 6 `bed`, 7 `wooden_door`, 8 `iron_pickaxe`, 9 `iron_sword`, 10 `iron_axe`, 11 `bucket`, 12 `oresalt` ×4, 13 `chest`.
  4. Stack −1 außer Creative.
- **Port:** `map` → `minecraft:map` (leer), `bed` → `red_bed`, `wooden_door` → `oak_door`, `planks` Meta 0 → `oak_planks`. Die Meta von Ofen und Truhe als `HORIZONTAL_FACING` (2 = Nord, 3 = Süd, 4 = West, 5 = Ost laut 1.7.10-Konvention; die Abbildung beim Port gegen die tatsächliche Blickrichtung testen).

---

## 11. Baumgeneratoren

### ItemAppleSeed
- IDs: `appletree_seed` „Apple Tree Seed“ (OreSpawnMain.java:1606), `cherrytree_seed` „Cherry Pit“ (1621), `peachtree_seed` „Peach Pit“ (1622). Stack 16 (ItemAppleSeed.java:16), Tab Decorations.
- **Verhalten `onItemUse`** (L20-40): serverseitig nur auf Grass, Dirt oder Farmland, sonst `false`. Dann `makeTree` mit `leaves_apple` (BlockAppleLeaves), `leaves_cherry` bzw. `leaves_peach` (beide BlockScaryLeaves). Stack −1 außer Creative, clientseitig ungeprüft.
- **`makeTree(world, x, y, z, leaves, chunk)`** (L42-119), öffentlich und von `OreSpawnWorld.java:1886-1892` zur Weltgenerierung mitbenutzt:

| Parameter | Apfel | Pfirsich | Kirsche | Herkunft |
|---|---|---|---|---|
| h1 Stammhöhe (exkl.) | 12 | 10 | 8 | (L47, L55, L64) |
| h2 Ast-Ebene 1 | 6 | 5 | 3 | (L48, L56, L65) |
| h3 Ast-Ebene 2 | 9 | 7 | 5 | (L49, L57, L66) |
| h4 erste Laubebene | 6 | 5 | 3 | (L50, L58, L67) |
| h5 Laubende (exkl.) | 14 | 12 | 10 | (L51, L59, L68) |
| w1 Astlänge 1 (exkl.) | 5 | 4 | 3 | (L52, L60, L69) |
| w2 Astlänge 2 (exkl.) | 3 | 2 | 1 | (L53, L61, L70) |

  1. Stamm `log` Meta 0 von y+1 bis y+h1−1 per `setBlock` Flag 2 (L72-74).
  2. Astkreuz aus Log in vier Richtungen auf y+h2 (Länge w1−1) und y+h3 (Länge w2−1) per `setBlockSuperFast` (L75-98); die Kirsche hat bei w2 = 1 keine zweite Ebene.
  3. Laub i = h4..h5−1: Halbbreite 6, ab i > 8 → 5, ab i > 10 → 4, bei Nicht-Apfel jeweils −1 (L99-109). Gesetzt wird nur, wo Luft ist (L110-117).
- **`OreSpawnMain.setBlockSuperFast`** (OreSpawnMain.java:5526-5551): schreibt direkt in den Chunk. Außerhalb von ±30 000 000 bzw. y < 0 oder y ≥ 256 passiert nichts. Mit Flag 2 folgt `markBlockForUpdate`, mit Flag 1 `notifyBlockChange`. Ist der Chunk gleich `refChunk`, entfallen die Updates (Worldgen-Pfad).
- **Port 1.21.1:**
  - Den Generator in eine gemeinsame Klasse (z. B. `feature/FruitTreeShape`) ziehen, die sowohl vom Item (`ServerLevel`) als auch von einem `Feature` (`WorldGenLevel`) benutzt wird
  - `setBlockSuperFast` durch `level.setBlock(pos, state, 2)` ersetzen (1.21.1 hat keinen direkten Chunk-Pfad ohne Mixin)
  - Höhenlimit: 1.21.1 prüft `level.isOutsideBuildHeight`

### ItemMagicApple
- IDs: `magicapple` „OMG! No! Don't do it!!!“ (OreSpawnMain.java:1587). Stack 1 (ItemMagicApple.java:31), Tab Decorations.
- **Werte:** `tree_radius = 6` (L27). Fortune 2 per `onCreated` (L35-37) und jeden Tick nachgetragen (L50-59), nur für den Glint.
- **Verhalten `onItemUse`** (L794-848):
  1. Nur auf Grass, Farmland oder Dirt, sonst `false` (L795-798).
  2. `tree_type = rand.nextInt(4)` ist die Log-/Laub-Meta: 0 oak, 1 spruce, 2 birch, 3 jungle (L799). `no_critters` ist mit 50 % `true` (L801-804; ein Instanzfeld des Singletons).
  3. Server: der geklickte Block wird `gold_block` (L806).
  4. Partikel 6 × `largesmoke`, `largeexplode`, `reddust`; Sound `random.explode` Lautstärke 2.8, Tonhöhe 1.5 (L808-813).
  5. Server, `rand_treetype = nextInt(100)` (L815-842):

| Wurf | Chance | Baum |
|---|---|---|
| 40–99 | 60 % | `MakeBigSquareTree`, log/leaves/`mossy_cobblestone`. Laub mit 1/10 `leaves_apple`, sofern `tree_type != 3` (L818-821) |
| 20–39 | 20 % | `MakeBigRoundTree`, log/leaves/`mossy_cobblestone` (L824) |
| 1 | 1 % | bei `GinormousEmeraldTreeEnable != 0`: 50 % Stamm `gold_block` + Laub `emerald_block` + Stufen `diamond_block`, 50 % `obsidian` + `blockruby` + `blockamethyst`, jeweils `tree_type = −1`, keine Critter (L828-834); sonst Square Tree mit Stufen aus `iron_ore` (L837) |
| 0, 2–19 | 19 % | `MakeBigCircularTree`, log/leaves/`mossy_cobblestone` (L841) |

  6. Stack −1 außer Creative.
- **Recherche-Abgleich:** 03-items.md:245 nennt „50 % chance of the ginormous tree“. Der Code sagt 1 % × 50 % je Variante; der Code gilt.
- **Hilfsprädikate:**
  - `isBoringBlock` (L61-94): Tallgrass, Cactus, beide Blumen, Leaves, Snow, `strawberry_plant`, `leaves_apple`, Luft oder `null` gelten als überschreibbar für Laub/Stufen.
  - `isBoringBaseBlock` (L96-108): Luft ja, `stone` nein, `bedrock` nein, **alles andere ja**. Stamm und Fundament überschreiben also Erde, Erze, Wasser usw.
- **`MakeBigSquareTree`** (L251-496):
  1. Höhe `this_height = t_radius + rand(t_radius)` (L252), Grundhöhe `base_height = t_radius·3` = 18 (L254).
  2. Fundament: entlang des Quadratrands (Radius 6) bis 20 Blöcke tief nach unten füllen, solange „boring base“ (L262-323).
  3. Hohler quadratischer Stamm, der pro Stufe um 1 schmaler wird (`this_width` 6 → 0). Jede Stufe hat `this_height + base_height` Ebenen, wobei `base_height` nur bei voller Breite gilt (L327-366); `this_height += rand(t_radius)` pro Stufe (L469).
  4. Wendeltreppe aus `stepID` außen um den Stamm; ab Breite ≥ 3 zwei Blöcke tief (L378-408).
  5. Zwischenböden, sobald die Spirale bei 0 ankommt. In der Mitte mit 50 % eine Truhe, sofern Critter erlaubt; Füllung `t_radius − this_width + rand(10)` Ziehungen (L409-428).
  6. Äste bei `this_width != t_radius`: Richtung zufällig aus `rand(4 + this_width)`, ohne die letzten zwei Richtungen zu wiederholen; nur Werte < 4 erzeugen einen Ast (L431-456).
  7. Spitze: 2 × `emerald_block`. Bei Stufen aus `diamond_block` spawnt **„The King“** (`TheKing`) bei y+4 mit `setGuardMode(1)`; bei `blockamethyst` **„The Queen“** mit `setGuardMode(1)` und `setBadMood(1)` (L471-494).
- **`make_branch`** (L125-249): rekursive horizontale Äste, Länge `this_width·3 + rand(this_width + 3)` je Breitenstufe (L137).
  - Auf der Mittellinie ab Breite ≥ 3: Truhe mit 1/75 (normale Bäume) bzw. 1/50 (`tree_type < 0`, also Ginormous), sofern Critter erlaubt und oben Luft ist, gefüllt mit `1 + rand(8)` Ziehungen. Sonst mit 1/50 ein **Iron Golem** (Vanilla-ID 99), wenn 3 Blöcke Luft darüber sind (L149-163).
  - Laubbüschel an dünnen Enden (L165-226). Bei Dschungelbäumen (`tree_type == 3`) mit 1/5 Ranken bis zu `rand(10)` lang (L182-197).
  - Bei Ginormous-Bäumen wird Laub mit 1/20 ersetzt: 2/3 `redstone_block`, 1/3 zufällig `blockuranium`, `blocktitanium`, `blockruby`, `blockamethyst` (L200-221).
  - Unteräste mit Wahrscheinlichkeit `1/(current_width+1)` (L227-242).
- **`MakeBigCircularTree`** (L569-674), `rad = 6.0`:
  1. Fundamentring bis 20 tief (L577-597).
  2. Pro Ebene: Log-Ring (Hohlzylinder).
  3. 3 × 3-Stufenplatten aus `stepID` bei Radius `rad + 1.9` um den Winkel `stepindex ± 1`, solange `rad > 1` (L613-625); `stepindex += 15 + (t_radius − rad)·3` (L664).
  4. Ab `cury > (int)rad` pro Ebene ein Ast: Winkel `+= 80 + rand(80)`, Länge `rad·5 + rand(rad + 2)`, Breite `rad + 1`, Drall `rand(2)·±1` (L627-638), gebaut per `MakeCirclularBranch` (L498-567, Laub außen, Log innen, Laub auf Log).
  5. Alle 6 Ebenen bei `rad > 3`: voller Boden; mit 50 % eine Truhe (ohne Critter-Sperre), `t_radius − (int)rad + rand(10)` Ziehungen (L639-663).
  6. `rad −= 0.01·rand(15)` pro Ebene (L669), im Mittel 0.07; daraus abgeleitet rund 86 Ebenen Höhe. Oben ein `diamond_block` (L670-672).
- **`MakeBigRoundTree`** (L676-758): wie der Circular Tree mit `world.rand` statt `OreSpawnRand`, aber **ohne Stufen und ohne Truhen**. Die Äste per `MakeRoundBranch` sind horizontale Scheiben mit Radius `branchlen/2` und Laub in den äußeren 2 Blöcken (L760-788). `stepID` bleibt unbenutzt.
- **Truhen-Loot** `chestContentsList` (L30), Format (Item, Meta, min, max, Gewicht):

| Item | min–max | Gewicht |
|---|---|---|
| ender_pearl | 1–2 | 3 |
| diamond | 1–5 | 15 |
| blaze_rod | 1–3 | 10 |
| `cageempty` | 1–10 | 7 |
| `cagegirlfriend` | 1–2 | 6 |
| iron_ingot | 1–10 | 16 |
| gold_ingot | 1–6 | 16 |
| `uranium_nugget` | 1–6 | 6 |
| `titanium_nugget` | 1–4 | 6 |
| bread, apple | 1–8 | 20 je |
| cookie | 1–16 | 20 |
| cooked_beef, cooked_chicken, cooked_fished, cooked_porkchop | 1–8 | 20 je |
| pumpkin_pie | 1–4 | 20 |
| carrot, potato | 1–16 | 20 je |
| `sunfish` | 1–4 | 6 |
| `firefish` | 1–8 | 6 |
| `popcorn_bag` | 1–4 | 16 |
| iron_pickaxe, iron_sword, bow | 1 | 20 je |
| diamond_pickaxe, diamond_sword | 1 | 5 je |
| arrow | 1–64 | 20 |
| `ultimatepickaxe` | 1 | 2 |
| `ultimatesword` | 1 | 1 |
| `ultimatefishingrod` | 1 | 5 |
| iron_helmet/chestplate/leggings/boots | 1 | 20 je |
| diamond_helmet/chestplate/leggings/boots | 1 | 5 je |
| golden_apple | 1 | 5 |

- **Config:** `OreSpawnTWEAKS.GinormousEmeraldTreeEnable` = 1 (manifest; OreSpawnMain.java:1148).
- **Wiederverwendung:** `OreSpawnWorld.java:1931-1949` ruft `MakeBigSquareTree`, `MakeBigCircularTree` und `MakeBigRoundTree` mit Chunk-Referenz für die Weltgenerierung.
- **Port 1.21.1:**
  - Die drei Generatoren in eine Klasse mit Schnittstelle auf `LevelAccessor` bzw. `WorldGenLevel` ziehen, Aufruf aus Item und `Feature`
  - Die Bäume sprengen ein 3 × 3-Chunk-Feature-Fenster; im Worldgen-Pfad als `Structure` mit `StructurePiece`s schreiben (Leitplanke), im Item-Pfad frei per `ServerLevel`
  - Loot als `orespawn:chests/magic_apple_tree`; die Zahl der Ziehungen je Truhe wird zur Rolls-Zahl
  - Meta-Holzsorten → `oak_log`, `spruce_log`, `birch_log`, `jungle_log` samt Laub mit `persistent = true`
  - King und Queen per `EntityType` mit Guard-Mode-Setter
  - `cooked_fished` → `cooked_cod`

---

## 12. Rüstung

### ItemOreSpawnArmor
- IDs: 56 Stück (OreSpawnMain.java:1446-1501; manifest). Schema `<prefix>_helmet|_chest|_leggings|_boots` mit Typ 0/1/2/3 (ctor_args[3]); Lang-Namen laut manifest (z. B. „The Ultimate Helmet“, „Pink Tourmailine Helmet“ – Tippfehler im Original).

| Index `armor_material` | Material-Feld | Prefix | Config-Präfix | Herkunft Index |
|---|---|---|---|---|
| 0 | `armorULTIMATE` | ultimate | Ultimate | Default (ItemOreSpawnArmor.java:24) |
| 1 | `armorLAVAEEL` | lavaeel | LavaEel | (L25-27) |
| 2 | `armorMOTHSCALE` | mothscale | MothScale | (L28-30) |
| 3 | `armorEMERALD` | emerald | Emerald | (L31-33) |
| 4 | `armorEXPERIENCE` | experience | Experience | (L34-36) |
| 5 | `armorRUBY` | ruby | Ruby | (L37-39) |
| 6 | `armorAMETHYST` | amethyst | Amethyst | (L40-42) |
| 7 | `armorPINK` | pink | Pink | (L43-45) |
| 8 | `armorTIGERSEYE` | tigerseye | TigersEye | (L46-48) |
| 9 | `armorPEACOCK` | peacock | Peacock | (L49-51) |
| 10 | `armorMOBZILLA` | mobzilla | Mobzilla | (L52-54) |
| 11 | `armorROYAL` | royal | Royal | (L55-57) |
| 12 | `armorLAPIS` | lapis | Lapis | (L58-60) |
| 13 | `armorQUEEN` | queen | Queen | (L61-63) |

- **Rolle:** `ItemArmor`, Tab Combat (L23). Die Materialien entstehen per `EnumHelper.addArmorMaterial(name, durability, {head, chest, leg, boot}, enchantability)` (OreSpawnMain.java:1432-1445). Ctor-Arg 3 ist der Render-Index aus `proxy.setArmorPrefix("<prefix>")`; dessen Wert ist hier nicht untersucht (offen).
- **Werte** (`get_armorstats`, Parameterreihenfolge laut OreSpawnMain.java:5633: dura, head, chest, leg, boots, enchant, resp, aqua, prot, fire, blast, proj, unbreak, feather; Werte OreSpawnMain.java:1160-1173). Haltbarkeit je Teil = Faktor 11/16/15/13 × durability (0.1), Spalte **abgeleitet**:

| Material | dura | Rüstung H/B/L/S | Summe | Haltbarkeit H/B/L/S (abgel.) | Ench. | Auto-Verzauberungen | Zeile |
|---|---|---|---|---|---|---|---|
| Ultimate | 200 | 6/12/10/6 | 34 | 2200/3200/3000/2600 | 100 | Resp 2, Aqua 3, Prot 5, Fire 5, Blast 5, Proj 5, Feather 3 | 1165 |
| LavaEel | 40 | 2/7/5/2 | 16 | 440/640/600/520 | 35 | Resp 1, Aqua 2, Prot 3, Fire 2, Blast 10, Feather 2 | 1164 |
| MothScale | 50 | 2/7/5/2 | 16 | 550/800/750/650 | 50 | Prot 3, Fire 3, Blast 3, Feather 5 | 1163 |
| Emerald | 60 | 3/8/6/3 | 20 | 660/960/900/780 | 40 | – | 1161 |
| Experience | 70 | 5/9/7/4 | 25 | 770/1120/1050/910 | 50 | Prot 2, Blast 1, Feather 1 | 1162 |
| Ruby | 90 | 4/9/8/4 | 25 | 990/1440/1350/1170 | 40 | – | 1170 |
| Amethyst | 100 | 4/8/7/3 | 22 | 1100/1600/1500/1300 | 40 | – | 1160 |
| Pink | 50 | 3/7/5/2 | 17 | 550/800/750/650 | 40 | – | 1166 |
| TigersEye | 80 | 4/8/7/4 | 23 | 880/1280/1200/1040 | 55 | – | 1167 |
| Peacock | 40 | 2/5/4/2 | 13 | 440/640/600/520 | 30 | Feather 10 | 1168 |
| Mobzilla | 1000 | 7/13/11/7 | 38 | 11000/16000/15000/13000 | 150 | Prot 10, Fire 10, Blast 10, Proj 10, Unbreak 5, Feather 10 | 1169 |
| Royal | 2000 | 8/14/12/8 | 42 | 22000/32000/30000/26000 | 200 | Resp 1, Aqua 2, Prot 10, Fire 10, Blast 10, Proj 10, Unbreak 5, Feather 10 | 1171 |
| Lapis | 60 | 2/7/5/2 | 16 | 660/960/900/780 | 60 | Resp 1, Aqua 1, Prot 1, Proj 1 | 1172 |
| Queen | 1500 | 9/16/14/9 | 48 | 16500/24000/22500/19500 | 150 | – | 1173 |

- **Config-Klemmung** (OreSpawnMain.java:5633ff): `durability` und `enchantability` auf `[default/2, default·2]`; Schutzwerte mindestens `default − 2` (ohne Obergrenze); Verzauberungsstufen mindestens `default/2`. Resp/Aqua/Prot/Fire/Blast/Proj sind geprüft, Unbreaking und Feather analog vermutet (nicht einzeln gelesen).
- **Verhalten `onCreated`** (L76-148): Ein Material mit Stats-Objekt fügt hinzu:
  - alle Teile: Protection, Fire Protection, Blast Protection, Projectile Protection, Unbreaking (je ≠ 0)
  - nur Stiefel (Typ 3): Feather Falling
  - nur Helm (Typ 0): Respiration, Aqua Affinity
- **Verhalten `onUpdate`** (L150-252), jeden Tick als Inventar-Item: Ist die Summe aller acht `e_*` > 0 und trägt der Stack **keine** der acht Verzauberungen (Stufe 0), wird dasselbe Set wie bei `onCreated` nachgetragen (L222-249).
  - Folgen: Die Verzauberungen sind nicht entfernbar.
  - Teile ohne passende Einträge prüfen jeden Tick ergebnislos weiter, z. B. Peacock-Helm/-Brust/-Beine: Summe 10, aber Feather nur für Stiefel.
- **`getArmorTexture`** (L254-339): Typ 0/1/3 → `orespawn:<prefix>_1.png`, Typ 2 (Beine) → `_2.png`; Emerald (Index 3) ist der Fallback-Zweig. Ziel laut texture_map: `assets/orespawn/<prefix>_1.png` → `assets/orespawn/textures/entity/<prefix>_1.png` (manifest texture_map, belegt für ultimate, emerald, royal, queen).
- **`onArmorTick`** (L341-363), für jedes getragene Teil:
  - Material 11 (Royal) oder 9 (Peacock) und die Stiefel (`getEquipmentInSlot(1)`) sind `royal_boots` bei `RoyalGlideEnable != 0` **oder** `peacock_boots` (ohne Config-Sperre): `motionY` wird auf mindestens −0.1 geklemmt, `fallDistance = 0` (L345-353).
  - Material 13 (Queen) und die Stiefel sind `queen_boots` bei `RoyalGlideEnable != 0`: `motionY ≥ −0.25`, `fallDistance = 0` (L354-362).
  - Die Stiefel selbst erfüllen die Materialbedingung; die Stiefel allein genügen.
- **Querbezug:** Experience-Rüstung (Index 4) erzeugt XP nur, wenn ein `ExperienceSword` im Inventar liegt (ExperienceSword.java:55-101).
- **Config:** Kategorie `OreSpawnARMOR`, Schlüssel `<Präfix>_durability`, `_head_damage_reduce`, `_chest_damage_reduce`, `_leggings_damage_reduce`, `_boots_damage_reduce`, `_enchantability`, `_enchant_respiration|aquaaffinity|protection|fireprotection|blastprotection|projectileprotection|unbreaking|featherfalling` (manifest); `OreSpawnTWEAKS.RoyalGlideEnable` = 1 (manifest; OreSpawnMain.java:1154).
- **Port 1.21.1:**
  - 14 Einträge im `ArmorMaterial`-Registry (Defense-Map je `ArmorItem.Type`, `enchantmentValue`, Equip-Sound, Reparaturzutat, Layer)
  - Haltbarkeit über `ArmorItem.Type.getDurability(dura)`. Ob die Faktoren in 1.21.1 ebenfalls 11/16/15/13 sind, im sources-Jar prüfen.
  - **`Attributes.ARMOR` ist auf 30 geklemmt.** Ultimate (34), Mobzilla (38), Royal (42) und Queen (48) überschreiten das. Die echten Summen sind oben notiert; für 1:1 braucht es eine zusätzliche Schadensreduktion (z. B. `LivingIncomingDamageEvent`) über die Differenz. Wie 1.7.10/Forge Rüstungssummen über 25 verrechnet hat, ist hier nicht belegt (offen).
  - Texturen: Die Layer erwartet Vanilla unter `textures/models/armor/<name>_layer_1.png`. Entweder beim Asset-Rename dorthin kopieren oder den NeoForge-Hook für Rüstungstexturen nutzen (sources-Jar).
  - Auto-Verzauberung: Stufen über dem Maximum (Protection 10, Aqua Affinity 3) sind als Component möglich, im Amboss aber nicht mehr kombinierbar – dokumentieren.
  - Gleitflug: Bewegung ist clientseitig, der Clamp muss auf **beiden** Seiten laufen (Armor-Tick-Hook bzw. `inventoryTick` mit Slotprüfung).
  - Fallschaden zusätzlich serverseitig per `fallDistance = 0` oder `LivingFallEvent`

---

## 13. CritterCage-Kapselziele (Detail)

Die Tabelle steht in Abschnitt 9 bei `CritterCage`. Hinweis zu `cageeasterbunny`: Der Name `"Easter Bunny"` ist in OreSpawnMain.java:4013 als `EasterBunny` registriert, fehlt aber in manifest `entities`.
