# Verhalten: core-02

Dieser Batch enthält das Grundgerüst, das keine eigenen Registry-Einträge trägt, das aber die meisten Mobs und Items trägt. Dazu gehören vier reine Datenhalter für Config-Werte (`ArmorStats`, `MobStats`, `OreStats`, `WeaponStats`), das Proxy-Paar für Client und Server mit 133 Entity-Renderern, eine Tastenbelegung und ein Netzwerkkanal für „Reittier steigen/schneller“, neun Dispenser-Verhalten (Spawn-Eier, sieben Projektile, Steine) und die eigene KI-Schicht aus Zielauswahl, Folgen, Fliehen, Wandern, Tanzen und Valentinstag-Aggression samt ihren Sortierern. Außerdem stecken hier drei Klassifikations-Helfer (`MyUtils`) und eine Tabelle mit 935 Legacy-ID-Konstanten, die der Compiler überall inline eingesetzt hat. Für den Port heißt das: Proxies entfallen. An ihre Stelle treten Mod-Bus-Events. Die Stats-Halter werden zu `ModConfigSpec`-Werten mit den originalen Klemmregeln, die Dispenser zu `DispenseItemBehavior`/`ProjectileItem` und die KI zu eigenen `Goal`-Klassen. Die Vanilla-Goals weichen in Details ab (Zufallsraten, Teleport, Sichtregeln). Die Taste läuft künftig über eine `KeyMapping` mit einem `CustomPacketPayload`. Der größte Semantikbruch betrifft die Taste: Im Original setzt sie einen **einzigen globalen** Server-Wert für alle Spieler.

**Quellen-Abkürzungen**
- `(X.java:N)`: `reference/src-20.2/src/main/java/danger/orespawn/X.java`, Zeile N
- `(JP kk.b)`: `javap -c` auf `reference/jar/mcp/client-1.7.10.jar`, Notch-Klasse und -Methode; SRG/MCP-Zuordnung über `reference/jar/mcp/joined.srg`
- `(NF X.java:N)`: `build/moddev/artifacts/neoforge-21.1.248-sources.jar`
- `(manifest)`: `docs/catalog/manifest.json`

**Gemeinsame Vanilla-Basis 1.7.10, belegt per javap**

| Größe | 1.7.10 | 1.21.1 |
|---|---|---|
| Ausgabeposition Dispenser | Blockmitte + 0.7 × Facing (JP akm.a = `BlockDispenser.func_149939_a`/getIPositionFromBlockSource) | `getDispensePosition(bs, 0.7, Vec3.ZERO)` (NF DispenserBlock.java:151) |
| `IBlockSource.getX()` | Block-x + 0.5 (JP cl.a) | `blockSource.center()` |
| Projektil-Richtung | (fx, fy + 0.1f, fz) (JP kk.b(ck,add)) | `ProjectileDispenseBehavior.execute` (NF ProjectileDispenseBehavior.java:26-39) |
| Geschwindigkeit `func_82500_b` | 1.1f (JP kk.b()) | `power = 1.1F` (NF ProjectileItem.java:35) |
| Streuung `func_82498_a` | 6.0f (JP kk.a()) | `uncertainty = 6.0F` (NF ProjectileItem.java:34) |
| Sound Projektil | levelEvent 1002 (JP kk.a(ck)) | 1002 (NF ProjectileDispenseBehavior.java:47) |
| Sound Default | levelEvent 1000 (JP cm.a(ck)) | 1000 (NF DefaultDispenseItemBehavior.java:58), Partikel 2000 (:62) |
| AI-Mutex-Bits | 1 = Bewegen (JP vc: EntityAIWander), 2 = Blick (JP un: EntityAIWatchClosest), 4 = Springen/Schwimmen (JP uf), 3 = FollowOwner (JP ug) | `Goal.Flag` MOVE, LOOK, JUMP, TARGET (NF Goal.java:54-58) |
| Attribut-Klemmen | – | MAX_HEALTH max 1024 (NF Attributes.java:103), ARMOR max 30 (:16), ATTACK_DAMAGE max 2048 (:28) |

---

### ArmorStats

**Rolle:** Datenhalter für die Werte eines Rüstungsmaterials, gefüllt aus der Forge-Config.

**Felder** (alle `public int`, ArmorStats.java:5-18):

| Feld | Config-Schlüssel `<Name>_…` (OreSpawnMain.java:5636-5694) | Klemmung | Verbraucher |
|---|---|---|---|
| `durability` | `_durability` | [d/2, d×2] (5637-5642) | `EnumHelper.addArmorMaterial` (OreSpawnMain.java:1432-1445) |
| `head_protection` | `_head_damage_reduce` | ≥ d−2, keine Obergrenze (5644-5646) | dito |
| `chest_protection` | `_chest_damage_reduce` | ≥ d−2 (5648-5650) | dito |
| `leg_protection` | `_leggings_damage_reduce` | ≥ d−2 (5652-5654) | dito |
| `boot_protection` | `_boots_damage_reduce` | ≥ d−2 (5656-5658) | dito |
| `enchantability` | `_enchantability` | [d/2, d×2] (5660-5665) | dito |
| `e_respiration` | `_enchant_respiration` | ≥ d/2 (5667-5669) | nur Helm, `armor_type == 0` (ItemOreSpawnArmor.java:139-142) |
| `e_aquaaffinity` | `_enchant_aquaaffinity` | ≥ d/2 | nur Helm (ItemOreSpawnArmor.java:143-145) |
| `e_protection` | `_enchant_protection` | ≥ d/2 | alle Teile (ItemOreSpawnArmor.java:121-123) |
| `e_fireprotection` | `_enchant_fireprotection` | ≥ d/2 | alle Teile (:124-126) |
| `e_blastprotection` | `_enchant_blastprotection` | ≥ d/2 | alle Teile (:127-129) |
| `e_projectileprotection` | `_enchant_projectileprotection` | ≥ d/2 | alle Teile (:130-132) |
| `e_unbreaking` | `_enchant_unbreaking` | ≥ d/2 | alle Teile (:133-135) |
| `e_featherfalling` | `_enchant_featherfalling` | ≥ d/2 | nur Stiefel, `armor_type == 3` (:136-138) |

- **Methoden:** keine.
- **Befüllung:** `OreSpawnMain.get_armorstats(config, name, 14 Defaults)` (OreSpawnMain.java:5633-5699), Kategorie fest `"OreSpawnARMOR"` (5635).
- **Instanzen:** 14 statische Felder (OreSpawnMain.java:62-75), Defaults z. B. `Amethyst` 100/4/8/7/3/40 (OreSpawnMain.java:1160). Das ergibt 196 Config-Schlüssel = 14 × 14 (manifest).
- Die Summe der `e_*`-Werte dient in `ItemOreSpawnArmor` als Kennzahl „schon verzaubert“ (ItemOreSpawnArmor.java:197-198).

**Materialwerte (manifest `armor_materials`):**

| Material | durability | Schutz K/B/L/S | Summe | enchant. |
|---|---|---|---|---|
| ULTIMATE | 200 | 6/12/10/6 | **34** | 100 |
| MOBZILLA | 1000 | 7/13/11/7 | **38** | 150 |
| ROYAL | 2000 | 8/14/12/8 | **42** | 200 |
| QUEEN | 1500 | 9/16/14/9 | **48** | 150 |
| EXPERIENCE | 70 | 5/9/7/4 | 25 | 50 |
| RUBY | 90 | 4/9/8/4 | 25 | 40 |
| TIGERSEYE | 80 | 4/8/7/4 | 23 | 55 |
| AMETHYST | 100 | 4/8/7/3 | 22 | 40 |
| EMERALD | 60 | 3/8/6/3 | 20 | 40 |
| PINK | 50 | 3/7/5/2 | 17 | 40 |
| LAVAEEL / MOTHSCALE / LAPIS | 40 / 50 / 60 | 2/7/5/2 | 16 | 35 / 50 / 60 |
| PEACOCK | 40 | 2/5/4/2 | 13 | 30 |

**Port 1.21.1:**
- `ArmorStats` wird ein `record` plus `ModConfigSpec`-Einträge unter `OreSpawnARMOR` mit identischen Schlüsselnamen.
- `defineInRange` setzt ungültige Werte auf den Default zurück. Das Original **klemmt** dagegen auf die Grenze. Deshalb ohne Range definieren und beim Lesen klemmen.
- `ArmorMaterial` ist in 1.21.1 ein Registry-Eintrag und entsteht, bevor Server-Configs geladen sind. Die Werte müssen also früh gelesen werden (Startup-Config; ob `ModConfig.Type.STARTUP` in 21.1.248 existiert: offen) oder zur Laufzeit per Attribut-Modifikator wirken.
- Vier Materialien überschreiten mit vollem Satz das ARMOR-Limit 30 (NF Attributes.java:16): ULTIMATE 34, MOBZILLA 38, ROYAL 42, QUEEN 48 (manifest). Der Rest muss als virtuelle Rüstung laufen, also als Schadensreduktion in `LivingIncomingDamageEvent` für die Punkte über 30.
- `e_*` sind datengetriebene Enchantments. Anwendung per `stack.enchant(registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), lvl)`, das gehört zur Item-Klasse `ItemOreSpawnArmor` (anderer Batch).

### ClientProxyOreSpawn

**Rolle:** `@SidedProxy(clientSide = …)` (OreSpawnMain.java:27). Erbt `CommonProxyOreSpawn` und registriert alles Clientseitige. Aufrufe: `registerSoundThings` (OreSpawnMain.java:1272), dann `registerRenderThings`, `registerKeyboardInput`, `registerNetworkStuff` (OreSpawnMain.java:5036-5038).

**Methoden:**
- `registerRenderThings()` (14-157):
  - `GirlfriendOverlayGui` auf `MinecraftForge.EVENT_BUS` (15).
  - 133 × `RenderingRegistry.registerEntityRenderingHandler` (16-148, grep-Zählung).
  - 8 × `MinecraftForgeClient.registerItemRenderer` für `MyBertha`, `MySlice`, `MyRoyal`, `MySquidZooka`, `MyHammy`, `MyBattleAxe`, `MyChainsaw`, `MyQueenBattleAxe` (149-156).
  - Auffällige Gruppen:
    - `Girlfriend`/`Boyfriend` mit Vanilla-`ModelBiped`, Schatten 0.5f/0.55f (16-17).
    - Vier Kühe (`RedCow`, `GoldCow`, `EnchantedCow`, `CrystalCow`) mit `RenderEnchantedCow(new ModelCow(), 0.7f)` (18-21).
    - Acht Projektile mit `RenderItemUrchin` (Item-Sprite): `SunspotUrchin`, `WaterBall`, `InkSack`, `LaserBall`, `IceBall`, `Acid`, `DeadIrukandji`, `BerthaHit` (23-30).
    - `GodzillaHead`, `KingHead`, `QueenHead` mit Modell `null` und 0.0f/0.0f, also unsichtbare Hitbox-Teile (93-95).
    - `SpiderDriver` mit Vanilla-`ModelSpider` (145).
    - `Termite` teilt `RenderAnt(new ModelAnt(), 0.15f, 0.35f)` mit `EntityRedAnt` (43, 106).
  - Die meisten Renderer nehmen (Modell, float, float). Die Bedeutung des zweiten und dritten Arguments steht in der jeweiligen `Render*`-Klasse (anderer Batch).
  - `IrukandjiArrow` ist die einzige Entity des Manifests ohne Registrierung hier (manifest gegen ClientProxy). Offen: 1.7.10 greift dann vermutlich auf den Renderer der Oberklasse `EntityArrow` zurück; im Repo nicht geprüft.
- `registerSoundThings()` (160-162): registriert `new OreSpawnSounds()` auf `EVENT_BUS`. Das bleibt wirkungslos, siehe `OreSpawnSounds`.
- `registerKeyboardInput()` (165-169): erzeugt `KeyHandler` (registriert die Taste im Konstruktor), meldet ihn am FML-Bus an und speichert ihn in `OreSpawnMain.MyKeyhandler`.
- `registerNetworkStuff()` (172-175): `super` legt den Kanal an, danach `RiderControl(getNetwork())` auf dem FML-Bus.
- `setArmorPrefix(String)` (178-180): `RenderingRegistry.addNewArmourRendererPrefix`. Der gelieferte Render-Index geht in jeden `ItemOreSpawnArmor`-Konstruktor (z. B. OreSpawnMain.java:1446).

**Port:**
- Kein Proxy.
- `com.swbr.orespawn.client.OreSpawnClient` als `@EventBusSubscriber(value = Dist.CLIENT, bus = MOD)` übernimmt:
  - `EntityRenderersEvent.RegisterRenderers`: 133 Einträge, Kopf-Teile als `NoopRenderer`.
  - `EntityRenderersEvent.RegisterLayerDefinitions` für die aus `reference/jar/models/*.json` generierten `LayerDefinition`s.
  - `RegisterKeyMappingsEvent` (NF RegisterKeyMappingsEvent.java:35).
- Die 8 `IItemRenderer` werden zu `IClientItemExtensions#getCustomRenderer` mit `BlockEntityWithoutLevelRenderer`. Registrierungs-Event in 21.1.248 (`RegisterClientExtensionsEvent`): offen.
- `GirlfriendOverlayGui` wird ein GUI-Layer (`RegisterGuiLayersEvent`, Name in 21.1.248 offen).
- Armor-Präfix entfällt, dafür Texturpfade über `ArmorMaterial.Layer`.

### CommonProxyOreSpawn

**Rolle:** Server-/Basis-Proxy (`serverSide`, OreSpawnMain.java:27).

- **Feld:** `private SimpleNetworkWrapper network` (9).
- `getNetwork()` (11-13).
- `registerRenderThings()`, `registerSoundThings()`, `registerKeyboardInput()`: leer (15-22).
- `registerNetworkStuff()` (24-26): Kanal `"RiderControls"`, Nachricht `RiderControlMessage` mit Handler `RiderControlMessageHandler`, Diskriminator `0`, Zielseite `Side.SERVER`. Der Handler läuft also auf dem Server.
- `setArmorPrefix(String)` gibt 0 zurück (28-30). Auf dem dedizierten Server ist der Render-Index damit immer 0.

**Port:** entfällt als Klasse. `RegisterPayloadHandlersEvent` (Mod-Bus, beide Seiten) → `event.registrar("1").playToServer(RiderControlPayload.TYPE, RiderControlPayload.STREAM_CODEC, RiderControlPayload::handle)` (NF PayloadRegistrar.java:52). Kanalname „RiderControls“ wird zur Payload-ID, Vorschlag `orespawn:rider_controls`.

### DispenserBehaviorOreSpawnEgg

**Rolle:** Dispenser-Verhalten für alle `ItemSpawnEgg` (paketprivat, `final`, erbt `BehaviorDefaultDispenseItem`).

`dispenseStack(IBlockSource, ItemStack)` (11-26):
1. `enumfacing = BlockDispenser.func_149937_b(meta)`, MCP `getFacingDirection` (methods.csv) (12).
2. Zielpunkt: `x = getX() + fx × 2.0`, `y = getYInt() + 0.2f`, `z = getZ() + fz × 2.0` (13-15). `getX()` = Block-x + 0.5 (JP cl.a). Die **Y-Komponente des Facings wird ignoriert**: Ein nach oben/unten zeigender Dispenser spawnt auf seiner eigenen Höhe.
3. Ist das Item ein `ItemSpawnEgg`: `ItemSpawnEgg.spawn_something(ise.my_id, world, (int)x, (int)y, (int)z)` (19). Die `(int)`-Casts schneiden zur Null hin ab, das Mob steht also auf der Blockecke. Beim Rechtsklick nutzt `ItemSpawnEgg.onItemUse` dagegen +0.5 / +1.01 / +0.5 (ItemSpawnEgg.java:27). Bei negativen Koordinaten landet der Cast wegen des Abschneidens einen Block näher an der Null.
4. Hat der Stack einen Anzeigenamen und ist das Ergebnis ein `EntityLivingBase`: Cast auf `EntityLiving`, dann `setCustomNameTag` (20-22).
5. `splitStack(1)` **immer**, auch wenn `spawn_something` null liefert (24).
6. Die geerbte finale `dispense` spielt Sound 1000 und die Partikel ab (JP cm.a).

- **Registrierung:** 115 × `putObject` für 114 Eier, `LizardEgg` doppelt (OreSpawnMain.java:5300 und 5337). Alle 114 `ItemSpawnEgg`-Items des Manifests sind abgedeckt (manifest gegen OreSpawnMain).
- Der Parameter `my_id` ist die `*EggIndex`-Konstante, z. B. `WitherSkeletonEgg = new ItemSpawnEgg(BaseItemID + 100, 192)` (OreSpawnMain.java:5183) → `case 192` (ItemSpawnEgg.java).

**Port:**
- `DispenserBlock.registerBehavior(item, new OreSpawnEggDispenseBehavior())` (NF DispenserBlock.java:61) in `FMLCommonSetupEvent.enqueueWork`.
- Klasse erbt `DefaultDispenseItemBehavior` und überschreibt `execute(BlockSource, ItemStack)`. Position: `bs.pos()` + 2 × `getStepX/Z`, Y = `bs.pos().getY()`; der Aufruf geht an die portierte `spawn_something`-Logik.
- Name: `stack.get(DataComponents.CUSTOM_NAME)` → `entity.setCustomName(...)`.
- `stack.shrink(1)` unbedingt.
- Die Vanilla-`SpawnEggItem`-Dispenserlogik nicht verwenden, denn sie spawnt vor dem Dispenser statt 2 Blöcke weiter.

### MobStats

**Rolle:** Datenhalter für Leben, Angriff und Verteidigung eines Mobs.

**Felder** (MobStats.java:5-7):

| Feld | Schlüssel `<Name>_…` | Klemmung (OreSpawnMain.java) | typischer Verbraucher |
|---|---|---|---|
| `health` | `_health` | [h/2, h×2] (5741-5746) | `mygetMaxHealth()` (Bee.java:94), Basiswert `maxHealth` (TheKing.java:83) |
| `attack` | `_attack` | [a/2, a×2] (5748-5753) | `attackDamage`-Basis und direkter Schaden (Bee.java:46, 134); TheKing ×2/×4/×8/×16 nach Lebensstand (TheKing.java:227-236) |
| `defense` | `_defense` | [d−4, d+4], danach ≤ 22 und ≥ 0 (5755-5766) | `getTotalArmorValue()` (Bee.java:272-274) |

- **Befüllung:** `get_mobstats(config, "OreSpawnMOBS", name, h, a, d)` (OreSpawnMain.java:5738-5768; Kategorie 1130).
- **Instanzen:** 59 (manifest `mob_stats`; Felder OreSpawnMain.java:1049-1107), gelesen in 62 Entity-Klassen (grep).
- Die Schlüsselnamen weichen teils vom Feldnamen ab: `Godzilla_stats` ↔ `"Mobzilla"` (6186), `PitchBlack_stats` ↔ `"Nightmare"` (6189), `Leon_stats` ↔ `"Leonopteryx"` (6195).
- **Lebenswerte über 1024** (Default, manifest): `SpiderRobot` 1500, `Godzilla` 4000, `TheQueen` 6000, `TheKing` 7000. Über die ×2-Config zusätzlich erreichbar: `Jeffery` 550 → 1100, `Kraken` 1000 → 2000 (manifest, Klemmung 5744-5746).
- `defense` ist höchstens 22 (5761) und bleibt damit unter ARMOR 30 (NF Attributes.java:16); der Default-Höchstwert ist 21 (manifest).

**Port:**
- `ModConfigSpec` (SERVER), Kategorie `OreSpawnMOBS`, Schlüssel 1:1, Klemmung manuell beim Lesen (siehe ArmorStats).
- `EntityAttributeCreationEvent` läuft vor dem Laden der Config. Deshalb Basiswerte in `finalizeSpawn`/Konstruktor aus der Config setzen.
- Leben > 1024 braucht virtuelles Leben (Schadensskalierung). Die Originalzahlen oben dokumentieren.

### MyDispenserBehaviorAcid

**Rolle:** Projektil-Dispenser für das Item `MyAcid` → Registry-ID `acid` („Acid“, manifest). Registriert: OreSpawnMain.java:5418.

- `getProjectileEntity(World, IPosition)` (9-12): `new Acid(world, x, y, z)` (Konstruktor Acid.java:34). `Acid` erbt `LaserBall` (Acid.java:6).
- Alles andere ist geerbtes `BehaviorProjectileDispense.dispenseStack`: Position +0.7, Richtung fy + 0.1f, 1.1f/6.0f, Sound 1002, 1 Item verbraucht (JP kk, siehe Tabelle oben).

**Port:** `ItemAcid implements ProjectileItem`, `asProjectile(level, pos, stack, dir)` → `new Acid(level, pos.x(), pos.y(), pos.z())` (NF ProjectileItem.java:13). Default-`DispenseConfig` hat 6.0F/1.1F (NF ProjectileItem.java:34-35), dazu `DispenserBlock.registerProjectileBehavior(item)` (NF DispenserBlock.java:65-67). Die Werte sind identisch, eine eigene Klasse ist nicht nötig.

### MyDispenserBehaviorArrow

**Rolle:** Dispenser für `MyIrukandjiArrow` → `irukandjiarrow` („Irukandji Arrow“, manifest). Registriert: OreSpawnMain.java:5415.

- `getProjectileEntity` (9-13): `new IrukandjiArrow(world, x, y, z)` (IrukandjiArrow.java:38), dazu `canBePickedUp = 1` (11). Laut MCP heißt `field_70251_a` „1 if the player can pick up the arrow“ (fields.csv): Der Pfeil darf aufgesammelt werden.
- `IrukandjiArrow` erbt `EntityArrow` (IrukandjiArrow.java:15).
- Sonst geerbtes Verhalten wie bei `MyDispenserBehaviorAcid`.

**Port:** `ItemIrukandjiArrow extends ArrowItem` bzw. `implements ProjectileItem`; in `asProjectile` `arrow.pickup = AbstractArrow.Pickup.ALLOWED` setzen. Vanilla-Default ist `DISALLOWED` (NF AbstractArrow.java:62). Registrierung per `registerProjectileBehavior`.

### MyDispenserBehaviorDeadIrukandji

**Rolle:** Dispenser für `MyIrukandji` → `deadirukandji` („Dead Irukandji“, manifest). Registriert: OreSpawnMain.java:5420.

- `getProjectileEntity` (9-12): `new DeadIrukandji(world, x, y, z)` (DeadIrukandji.java:34). Erbt `LaserBall` (DeadIrukandji.java:6).
- Sonst geerbt (siehe Acid).

**Port:** wie Acid, `ProjectileItem` + `registerProjectileBehavior`.

### MyDispenserBehaviorIceball

**Rolle:** Dispenser für `MyIceBall` → `iceball` („Ice Ball“, manifest). Registriert: OreSpawnMain.java:5419.

- `getProjectileEntity` (9-12): `new IceBall(world, x, y, z)` (IceBall.java:41). Erbt `LaserBall` (IceBall.java:8).
- Sonst geerbt.

**Port:** wie Acid.

### MyDispenserBehaviorLaserball

**Rolle:** Dispenser für `MyLaserBall` → `laserball` („Robot Laser Charge“, manifest). Registriert: OreSpawnMain.java:5421.

- `getProjectileEntity` (9-12): `new LaserBall(world, x, y, z)` (LaserBall.java:63). `LaserBall` erbt `EntityThrowable` (LaserBall.java:9) und ist Basis von `Acid`, `DeadIrukandji` und `IceBall` (classes.json).
- Sonst geerbt.

**Port:** wie Acid; `LaserBall` wird `ThrowableItemProjectile`.

### MyDispenserBehaviorRock

**Rolle:** Dispenser für alle 12 Wurfsteine. Überschreibt `dispenseStack` vollständig, weil der Steintyp aus dem Item gesetzt werden muss.

`dispenseStack` (12-58):
1. Position über `BlockDispenser.func_149939_a` (getIPositionFromBlockSource, +0.7) (14), Facing (15).
2. Projektil erzeugen (16) und `setThrowableHeading(fx, fy + 0.1f, fz, func_82500_b(), func_82498_a())` = 1.1f/6.0f (17; JP kk.b()/kk.a()). Das repliziert Vanilla 1:1.
3. Unbedingter Cast `(EntityThrownRock)` (18), danach `setRockType` je nach Item (19-54):

| Typ | Item-Feld | Registry-ID | Name (manifest) | Zeile |
|---|---|---|---|---|
| 1 | `MySmallRock` | `rocksmall` | Small Rock | 19-21 |
| 2 | `MyRock` | `rock` | Big Rock | 22-24 |
| 3 | `MyRedRock` | `rockred` | Flame Rock | 25-27 |
| 4 | `MyGreenRock` | `rockgreen` | Poison Rock | 28-30 |
| 5 | `MyBlueRock` | `rockblue` | Slowness Rock | 31-33 |
| 6 | `MyPurpleRock` | `rockpurple` | Weakness Rock | 34-36 |
| 7 | `MySpikeyRock` | `rockspikey` | Painful Rock | 37-39 |
| 8 | `MyTNTRock` | `rocktnt` | Explosive Rock | 40-42 |
| 9 | `MyCrystalRedRock` | `rockcrystalred` | Flame Crystal | 43-45 |
| 10 | `MyCrystalGreenRock` | `rockcrystalgreen` | Poison Crystal | 46-48 |
| 11 | `MyCrystalBlueRock` | `rockcrystalblue` | Slowness Crystal | 49-51 |
| 12 | `MyCrystalTNTRock` | `rockcrystaltnt` | Explosive Crystal | 52-54 |

4. `spawnEntityInWorld` (55), `splitStack(1)` (56). Der Sound kommt aus dem geerbten `kk.a(ck)` = 1002 (JP).

- `getProjectileEntity` (60-63): `new EntityThrownRock(world, x, y, z)` (EntityThrownRock.java:47).
- `setRockType` ignoriert die Client-Seite und schreibt DataWatcher-Slot 20 (EntityThrownRock.java:63-72; Default 0 in :56).
- Registriert für 12 Items (OreSpawnMain.java:5422-5433). Ein unbekanntes Item würde Typ 0 behalten, ist aber nicht registriert.

**Port:** `ItemRock implements ProjectileItem`; `asProjectile` erzeugt `EntityThrownRock` und setzt den Typ aus einer `Map<Item,Integer>` in `SynchedEntityData`. Danach `registerProjectileBehavior` für alle 12. Die eigene Dispenser-Klasse entfällt, Werte identisch.

### MyDispenserBehaviorSunspotUrchin

**Rolle:** Dispenser für `MySunspotUrchin` → `sunspoturchin` („Sunspot Urchin“, manifest). Registriert: OreSpawnMain.java:5417.

- `getProjectileEntity` (9-12): `new SunspotUrchin(world, x, y, z)` (SunspotUrchin.java:41). Erbt `EntityThrowable` (SunspotUrchin.java:12).
- Sonst geerbt.

**Port:** wie Acid.

### MyDispenserBehaviorWDCharge

**Rolle:** Dispenser für `MyWaterBall` → `waterball` („WaterDragon Charge“, manifest; „WD“ = WaterDragon). Registriert: OreSpawnMain.java:5416.

- `getProjectileEntity` (9-12): `new WaterBall(world, x, y, z)` (WaterBall.java:27). Erbt `EntityThrowable` (WaterBall.java:10).
- Sonst geerbt.

**Port:** wie Acid.

### MyUtils

**Rolle:** statische Klassifikations-Prädikate für die Zielauswahl vieler Mobs. Alle Prüfungen sind `instanceof`, **Unterklassen zählen also mit** (classes.json).

| Methode | wahr für | Nutzer (grep) |
|---|---|---|
| `isRoyalty(Entity)` (9-11) | `EntityLivingBase` **und** `ThePrince`, `ThePrinceTeen`, `ThePrinceAdult`, `ThePrincess`, `TheKing`, `KingHead`, `TheQueen`, `QueenHead`, `PurplePower` | 11: BetterFireball, Godzilla, IceBall, PurplePower, TheKing, ThePrince, ThePrinceAdult, ThePrincess, ThePrinceTeen, TheQueen, ThunderBolt |
| `isAttackableNonMob(EntityLivingBase)` (13-15) | `EntityMob`, `Mothra`, `Leon`, `Dragon`, `Spyro`, `isRoyalty`, `GammaMetroid`, `Cephadrome`, `WaterDragon`, `Girlfriend`, `Boyfriend`, `EntityVillager`, `Stinky` | 11: CaterKiller, Crab, Hammerhead, Leon, Mantis, Molenoid, SeaMonster, SeaViper, TheKing, TheQueen, WaterDragon |
| `isIgnoreable(EntityLivingBase)` (17-19) | `RockBase`, `EntityAnt` (+ `EntityRedAnt`, `EntityRainbowAnt`, `EntityUnstableAnt`, `Termite`), `EntityButterfly` (+ `EntityLunaMoth`, **`Mothra`**), `EntityMosquito`, `Dragonfly`, `Firefly`, `Cricket`, `Cockateil` (+ `RubyBird`), `Termite`, `Ghost`, `GhostSkelly`, `Elevator` | 37 Entity-Klassen (Alosaurus … Vortex) |

- Falle: `Mothra` erbt `EntityButterfly` (classes.json) und ist deshalb **gleichzeitig** „ignorierbar“ und „angreifbar“. Welche Prüfung gewinnt, hängt von der Reihenfolge im jeweiligen Mob ab (anderer Batch).
- `Termite` steht doppelt, weil es ohnehin von `EntityAnt` erbt.

**Port:** `com.swbr.orespawn.entity.ai.OreSpawnTargets` mit denselben `instanceof`-Ketten.
- `EntityMob` → `net.minecraft.world.entity.monster.Monster`, **nicht** `Enemy`; `Enemy` entspräche `IMob`.
- `EntityVillager` → `Villager`.
- Entity-Type-Tags wären erweiterbar, verlieren aber die Unterklassen-Semantik. Deshalb 1:1 als `instanceof`.

### OreSpawnConstants

**Rolle:** reine Konstantentabelle: 935 × `public static final int` (OreSpawnConstants.java:5-939; Regex-Zählung).

| Familie | Anzahl | Bedeutung | Beleg |
|---|---|---|---|
| `*BlockID` | 204 | Offset auf `BaseBlockID` (Default 2700, manifest) | `new OreUranium(BaseBlockID + 101)` (OreSpawnMain.java:1270) = `UraniumBlockID = 101` (OreSpawnConstants.java:105) |
| `*ItemID` | 475 | Offset auf `BaseItemID` (Default 9000, manifest) | `UltimatePickaxe(BaseItemID + 305)` (OreSpawnMain.java:1308) = `UltimatePickaxeItemID = 305` (:525) |
| `*Index` | 241 | `my_id` für `ItemSpawnEgg`/Käfige und weitere Indizes | `ItemSpawnEgg(BaseItemID + 100, 192)` (OreSpawnMain.java:5183) = `WitherSkeletonEggItemID = 100` (:322) und `WitherSkeletonEggIndex = 192` (:759) |
| Sonstige | 15 | Bereichsgrenzen und Pflanzen-IDs | `OREBLOCK_START_OFFSET = 0`, `NUMBER_OF_OREBLOCKS = 97` (:5-6); `CRYSTAL_OREBLOCK_START_OFFSET = 250`, `CRYSTAL_NUMBER_OF_OREBLOCKS = 12` (:199-200); `OREBLOCK_START_OFFSET_TWO = 300`, `NUMBER_OF_OREBLOCKS_TWO = 7` (:213-214); `SHOE_START_INDEX = 2`, `NUMBER_OF_SHOES = 4` (:697-698) |

- **Keine Referenz im dekompilierten Code** (grep): javac setzt `static final int` als Literal ein. Die Klasse ist also nicht tot, sondern **überall inline eingesetzt**.
- Gegenprobe der Bereichskonstanten: Spawn-Erz-Blöcke `SpiderBlockID 0` … `TheQueenPartBlockID 96` = 97 Einträge (:7-103); `UrchinBlockID 250` … `CrystalCowBlockID 261` = 12 (:201-212); `BrutalflyBlockID 300` … `CrabBlockID 306` = 7 (:215-221).
- Innerhalb einer Familie gibt es keine doppelten Werte (Regex-Auswertung). Zwischen Familien überlappen die Werte, z. B. `CRYSTAL_OREBLOCK_START_OFFSET = UrchinBlockID = 250`, die Namensräume sind aber getrennt.
- Kuriositäten:
  - `VillagerCowEggIndex = 364` (Tippfehler im Namen, :919).
  - `PeacockFeatherLegsItemID = 370` springt aus der Reihe 359/360 (:581).
  - Offen: Die Projektil-Indizes `WaterBallIndex 49`, `SunspotUrchinIndex 50`, `InkSackIndex 65`, `LaserBallIndex 81`, `IceBallIndex 84`, `AcidIndex 85`, `IrukandjiIndex 86` (:704-710) sind inline eingesetzt, ihr Verbraucher lässt sich per Namenssuche nicht finden. Kandidat: Entity-Netzwerk-IDs in `EntityRegistry.registerModEntity` (in `calls_OreSpawnMain.txt` nach den Zahlen suchen).

**Port:** Numerische Block-/Item-IDs gibt es in 1.21.1 nicht, dort gelten Registry-Namen (manifest `legacy_registry`). Nicht als Code portieren. Nur die Semantik der `*EggIndex`-Werte (Switch in `ItemSpawnEgg.spawn_something`) wandert als direkte `EntityType`-Referenz je Ei-Item mit. Die Datei bleibt Nachschlagequelle für Legacy-ID → Registry-Name.

### OreSpawnSounds

**Rolle:** Überbleibsel. `onSound(SoundLoadEvent)` ist leer und **ohne** `@SubscribeEvent`/`@ForgeSubscribe` (OreSpawnSounds.java:7-8; grep ohne Treffer), wird also nie aufgerufen, obwohl `ClientProxyOreSpawn.java:161` die Instanz registriert. Die Sounds kommen tatsächlich aus der `sounds.json` im Jar: 126 Events (manifest `counts.sounds`).

**Port:** entfällt. Ersatz ist `DeferredRegister<SoundEvent>` mit den 126 IDs aus manifest `sounds` plus die kopierte `sounds.json`. Umbenennungen laut manifest `problems`: `Beebuzz` → `beebuzz`, `MothraWings` → `mothrawings`.

### OreStats

**Rolle:** Datenhalter für eine Erzgenerierung.

| Feld (OreStats.java:5-8) | Schlüssel `<Name>_…` | Klemmung (OreSpawnMain.java) |
|---|---|---|
| `rate` | `_rate` | [r/2, r×2] (5773-5778) |
| `clumpsize` | `_clumpsize` | [c/2, c×2], danach ≥ 1 (5780-5788) |
| `mindepth` | `_mindepth` | ≥ 0 (5790-5792) |
| `maxdepth` | `_maxdepth` | ≥ 0 (5794-5796); ist `max − min < 10`, werden **beide** auf die Defaults zurückgesetzt (5797-5800) |

- **Befüllung:** `get_orestats(config, "OreSpawnORES", name, rate, clump, min, max)` (OreSpawnMain.java:5770-5802; Kategorie 1133). 13 Instanzen = 52 Schlüssel (manifest).

**Defaults (OreSpawnMain.java:1244-1256):**

| Feld | Schlüsselname | rate | clump | min | max |
|---|---|---|---|---|---|
| `Ruby_stats` | Ruby | 10 | 1 | 0 | 50 |
| `BlkRuby_stats` | BlockRuby | 1 | 2 | 0 | 15 |
| `Uranium_stats` | Uranium | 3 | 4 | 0 | 30 |
| `Titanium_stats` | Titanium | 3 | 4 | 0 | 20 |
| `Amethyst_stats` | Amethyst | 2 | 6 | 0 | 25 |
| `Salt_stats` | Salt | 5 | 12 | 50 | 128 |
| `SpawnOres_stats` | SpawnOres | 28 | 4 | 50 | 128 |
| `Diamond_stats` | Diamond | 4 | 6 | 0 | 30 |
| `BlkDiamond_stats` | BlockDiamond | 2 | 4 | 0 | 20 |
| `Emerald_stats` | Emerald | 4 | 6 | 0 | 40 |
| `BlkEmerald_stats` | BlockEmerald | 2 | 4 | 0 | 20 |
| `Gold_stats` | Gold | 4 | 8 | 0 | 40 |
| `BlkGold_stats` | BlockGold | 2 | 4 | 0 | 25 |

**Verbraucher `ChunkOreGenerator`:**
- Patches = `rate + random.nextInt(n)`, mit n = 30 bei SpawnOres (ChunkOreGenerator.java:13-14), n = 9 bei Uranium (:464), n = 12 bei Amethyst (:492).
- Y-Filter `mindepth ≤ y ≤ maxdepth` (:25, :472).
- `clumpsize` geht an `generateBlockOre` (:59, :473).
- `rate ≤ 0` schaltet das Erz ab (:13, :463).

**Port:**
- Worldgen ist in 1.21.1 datengetrieben, eine Config wirkt dort nicht von selbst. Vorschlag: eigene `PlacementModifierType`s (`orespawn:config_count`, `orespawn:config_height`), die beim Platzieren die `ModConfigSpec`-Werte lesen, plus Ore-Feature-Größe aus `clumpsize` (eigenes `Feature`, weil `OreConfiguration.size` statisch ist).
- `generateBlockOre` gehört zu `ChunkOreGenerator` (anderer Batch).
- Offen (Designentscheidung): Y-Werte absolut 1:1 übernehmen (0 liegt in 1.21.1 mitten im Gestein) oder auf −64 verschieben.

### WeaponStats

**Rolle:** Datenhalter für ein Werkzeug-/Waffenmaterial.

| Feld (WeaponStats.java:5-9) | Schlüssel `<Name>_…` | Klemmung (OreSpawnMain.java) |
|---|---|---|
| `harvestlevel` | `_harvestlevel` | Unter d−1 wird der Wert **auf d** gesetzt, nicht auf d−1; keine Obergrenze (5704-5706) |
| `maxuses` | `_maxuses` | [d/2, d×2] (5708-5713) |
| `efficiency` | `_efficiency` | [d/2, d×2] (5715-5720) |
| `damage` | `_damage` | [d/2, d×2] (5722-5727) |
| `enchantability` | `_enchantability` | [d/2, d×2] (5729-5734) |

- **Befüllung:** `get_weaponstats(config, "OreSpawnWEAPONS", name, …)` (OreSpawnMain.java:5701-5736; Kategorie 1132).
- **Instanzen:** 15 (OreSpawnMain.java:403-417). 77 Schlüssel im manifest, davon weitere Nicht-Stats-Schlüssel wie `UltimateSwordEnchantmentLevel`.

**Defaults (OreSpawnMain.java:1174-1188) und Material (1292-1306):**

| Feld | Schlüsselname | Material | harvest | uses | eff. | dmg | ench. |
|---|---|---|---|---|---|---|---|
| `ultimate_stats` | Ultimate | ULTIMATE | 10 | 3000 | 15 | 36 | 100 |
| `nightmare_stats` | Nightmare | NIGHTMARE | 3 | 1800 | 12 | 26 | 60 |
| `bertha_stats` | Bertha | BERTHA | 3 | 9000 | 15 | 496 | 100 |
| `crystalwood_stats` | CrystalWood | CRYSTALWOOD | 2 | 300 | 3 | 2 | 15 |
| `crystalstone_stats` | CrystalStone | CRYSTALSTONE | 3 | 800 | 6 | 5 | 45 |
| `crystalpink_stats` | Pink | CRYSTALPINK | 4 | 1100 | 10 | 7 | 65 |
| `tigerseye_stats` | TigersEye | TIGERSEYE | 4 | 1600 | 12 | 8 | 75 |
| `ruby_stats` | Ruby | RUBY | 5 | 1500 | 11 | 16 | 85 |
| `amethyst_stats` | Amethyst | AMETHYST | 4 | 2000 | 11 | 11 | 70 |
| `emerald_stats` | Emerald | **REALEMERALD** | 3 | 1300 | 10 | 6 | 75 |
| `royal_stats` | Royal | ROYAL | 3 | 10000 | 15 | 746 | 150 |
| `hammy_stats` | **Attitude** | HAMMY | 5 | 2000 | 15 | 82 | 100 |
| `battleaxe_stats` | BattleAxe | **BATTLE** | 3 | 1500 | 15 | 46 | 75 |
| `chainsaw_stats` | Chainsaw | CHAINSAW | 3 | 1500 | 10 | 56 | 75 |
| `queenbattleaxe_stats` | QueenBattleAxe | **QUEENBATTLE** | 3 | 2200 | 15 | 662 | 100 |

**Verbraucher:**
- `EnumHelper.addToolMaterial(name, harvestlevel, maxuses, (float)efficiency, (float)damage, enchantability)` (1292-1306).
- `setHarvestLevel("pickaxe"/"shovel"/"axe", harvestlevel)` (1308-1340).
- `BerthaHit` nutzt `bertha_stats.damage`, `royal_stats.damage` und `hammy_stats.damage` direkt als Trefferschaden (BerthaHit.java:73, 84, 94).

**Port:**
- `Tier` (NeoForge `SimpleTier`) mit `incorrectBlocksForDrops`-Tag statt Zahl. Level 4, 5 und 10 haben kein Vanilla-Gegenstück. Abbildung: alle ≥ 3 auf das Netherite-Äquivalent (`#minecraft:incorrect_for_netherite_tool`); das ist eine Designentscheidung.
- Schaden bleibt unter ATTACK_DAMAGE 2048 (NF Attributes.java:28), auch Royal 746 × 2 = 1492 mit Config-Maximum.
- Registrierungszeitpunkt vor Config: gleiches Problem wie bei ArmorStats.

### GenericTargetSorter

**Rolle:** `Comparator` (Rohtyp) für Ziellisten. Das Feld `theEntity` ist das suchende Mob (9-13).

`compareDistanceSq(a, b)` (15-34):
- `d = theEntity.getDistanceSqToEntity(x)`.
- Creeper: `d /= 2.0` (18-20, 26-28).
- `weight = height × width`; ist `weight > 1.0`, dann `d /= weight` (21-24, 29-32).
- Kleinere Werte zuerst. Große Ziele und Creeper wirken also „näher“ und werden bevorzugt.

`compare(Object, Object)` delegiert (36-39).

**Nutzer:** 75 Entity-Klassen (grep), Muster `this.TargetSorter = new GenericTargetSorter(this)` (Alien.java:45, TheKing.java:74), danach `Collections.sort(list, TargetSorter)` in der mob-eigenen Zielsuche.

**Port:** `Comparator<Entity>` als `Comparator.comparingDouble(e -> key(e))` mit `distanceToSqr`, `getBbWidth() * getBbHeight()`, `instanceof Creeper`. Die Schlüsselfunktion ist transitiv, `List.sort` damit stabil.

### MyEntityAIAvoidEntity

**Rolle:** Fluchtverhalten, Kopie von `EntityAIAvoidEntity` mit Sonderregeln.

- **Felder** (14-21): `theEntity` (EntityCreature), `farSpeed`, `nearSpeed`, `closestLivingEntity`, `distanceFromEntity` (float), `entityPathEntity`, `entityPathNavigate`, `targetEntityClass`.
- **Konstruktor** (23-31): (creature, Klasse, Distanz, farSpeed, nearSpeed); Mutex 1 = MOVE.

**Methoden:**
- `shouldExecute()` (33-65):
  1. `EntityCannonFodder` mit `get_is_activated() != 0` → nie fliehen (34-39). **Greift real**: alle drei Nutzer erben `EntityCannonFodder` (classes.json).
  2. Zielklasse `EntityPlayer`: gezähmte Tiere fliehen nie (41-43); sonst nächster Spieler in Reichweite (44).
  3. Andere Klasse: `selectEntitiesWithinAABB(klasse, bb.expand(d, 3.0, d), IMob.mobSelector)` (50). Genommen wird `list.get(0)`, **das erste**, nicht das nächste (54). Der Selektor lässt nur `IMob` durch.
  4. `RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 16, 7, bedrohungsPos)` (56). Null → false.
  5. Liegt der Punkt näher an der Bedrohung als das Mob jetzt → false (60-62).
  6. Pfad dorthin, `isDestinationSame` (63-64).
- `continueExecuting()`: `!noPath()` (67-69).
- `startExecuting()`: `setPath(path, farSpeed)` (71-73).
- `resetTask()`: Bedrohung = null (75-77).
- `updateTask()`: Distanz² < 49.0 → `nearSpeed`, sonst `farSpeed` (79-86).
- `func_98217_a(inst)` (88-90): SRG-Name, in methods.csv nicht benannt. Nach Nutzung ein statischer Accessor auf `theEntity`, im Batch unbenutzt.

**Nutzer:** `Chipmunk` (EntityMob, 8.0f, 1.0, 1.6) (Chipmunk.java:31), `Ostrich` (EntityMob, 8.0f, 1.0, 1.9) (Ostrich.java:50), `VelocityRaptor` (EntityMob, 8.0f, 1.0, 1.4) (VelocityRaptor.java:37). Weil die Zähm-Sperre nur für Spieler gilt, fliehen gezähmte Tiere weiterhin vor Monstern.

**Port:** Vanilla `AvoidEntityGoal(PathfinderMob, Class, float, walk, sprint, Predicate)` (NF AvoidEntityGoal.java:61) hat dieselben Konstanten 16/7 (:82) und 49.0 (:111). Unterschiede: CannonFodder-Sperre, Zähm-Sperre bei Spielern, `IMob`-Filter → `Enemy`, erstes statt nächstes Ziel. Deshalb eigene Klasse `MyAvoidEntityGoal extends Goal` mit `Flag.MOVE`. Die Reihenfolge von `level.getEntitiesOfClass` ist nicht mit 1.7.10 identisch, „erstes Element“ bleibt also ohnehin nicht reproduzierbar; vertretbar ist „nächstes“, als Abweichung notieren.

### MyEntityAIDance

**Rolle:** Nachttanz der `Girlfriend` auf Edelstein-/Metallblöcken, mit Takt-Synchronisation zwischen mehreren Girlfriends.

- **Felder** (13-17): `thePet` (Girlfriend), `theWorld`, `public ticker`, `public dance_move`, `public is_dancing`. `is_dancing` liest `Girlfriend.getLivingSound`: Während des Tanzes gibt es keine Idle-Laute (Girlfriend.java:863-865).
- **Konstruktor** (19-25): **kein** `setMutexBits`, der Goal läuft parallel zu allem. Priorität 3 (Girlfriend.java:130-131).

**Methoden:**
- `is_dance_block(Block)` (27-29): `gold_block`, `diamond_block`, `emerald_block`, `MyBlockRubyBlock`, `MyBlockAmethystBlock`, `MyBlockTitaniumBlock`, `MyBlockUraniumBlock`.
- `shouldExecute()` (31-54): nicht sitzend; `worldTime % 24000` in [14000, 22000] (35-39); 7 × 7-Scan (i, j = −3…3) auf Höhe `posY − 1` (43-52); mindestens ein Tanzblock.
- `continueExecuting()` (56-91): dieselben Prüfungen. Dann Mittelwert-Offset `ix/ic`, `iz/ic` (81-82):
  - `ic < 40` → `tryMoveToXYZ(pos + offset, 1.0)` (83-85);
  - sonst mit `rand.nextInt(3) == 1` Bewegung auf die eigene Position, also Stopp (86-88).
  - Setzt `is_dancing = 1`. **Nebenwirkungen in jedem Aufruf.**
- `startExecuting()` (93-118): nicht schleichen, `ticker = dance_move = 0`, `is_dancing = 1`, bei `ic < 40` zum Mittelpunkt laufen.
- `resetTask()` (120-125): alles auf 0, nicht schleichen.
- `updateTask()` (127-286):
  - Konstanten: `cycle = 20`, `halfc = 10`, `mover = 160` (128-130).
  - **Sync** (132-142): Girlfriends im Quader ±4 / ±3 / ±4; hat eine eine kleinere Entity-ID und tanzt, werden `ticker` und `dance_move` kopiert. Die niedrigste ID führt.
  - `++ticker`. Ist `dance_move == 0`: neue Figur `1 + rand.nextInt(10)`, Bewegung x/z = 0, `ticker = 0`, nicht schleichen (144-150).
  - Jede Figur endet bei `ticker > 160`; danach wird neu gewählt.

| Figur | Schleichen (`ticker % 20 < 10` aufrecht, sonst geduckt) | `swingItem` bei `ticker % 10 == 1` | `motionY = 0.25` | `move_it`-Richtungen | Zeilen |
|---|---|---|---|---|---|
| 1 | – | – | – | 0 | 152-159 |
| 2 | – | – | – | 1 | 160-167 |
| 3 | ja | – | – | – | 168-180 |
| 4 | – | ja | bei `ticker % 10 == 1` | – | 181-191 |
| 5 | – | ja | – | 0 | 192-202 |
| 6 | – | ja | – | 1 | 203-213 |
| 7 | ja | – | – | 0, 2 | 214-228 |
| 8 | ja | – | – | 1, 2 | 229-243 |
| 9 | ja | ja | – | 0, 3 | 244-261 |
| 10 | ja | ja | jeden Tick der aufrechten Hälfte | 1, 3 | 262-280 |

- `move_it(et, t, cycle, dir)` (288-339):
  - dir 0: `motionX += 0.02`; dir 1: `motionZ += 0.02`; dir 2: `rotationYaw += 10`; dir 3: `rotationYawHead += 10`.
  - Ab `t % 20 >= 10` alle Vorzeichen umgedreht (323-329). Ab `(t % 20) % 10 >= 5` nur die Yaw-Werte nochmals umgedreht (330-334).
  - Ergebnis: Die Bewegung pendelt im 10-Tick-Takt, die Drehung im 5-Tick-Takt.

**Port:**
- `Goal` mit `EnumSet.noneOf(Flag.class)`.
- Zeit über `level.getDayTime() % 24000`, Nachbarn über `level.getEntitiesOfClass(Girlfriend.class, aabb)`.
- `swingItem` → `swing(InteractionHand.MAIN_HAND)`, `motion` → `setDeltaMovement(getDeltaMovement().add(...))`, `rotationYawHead` → `yHeadRot`, Navigation `getNavigation().moveTo(x, y, z, 1.0)`.
- Schleichen: `setShiftKeyDown` (NF Entity.java:2343). Die geduckte Darstellung des Humanoid-Modells hängt in 1.21.1 an der Pose, `setPose(Pose.CROUCHING)` ist also mitzusetzen (Risiko, im Client zu prüfen).
- Risiko: `BodyRotationControl`/`LookControl` überschreiben `yRot`/`yHeadRot` jeden Tick, die Tanzdrehung kann deshalb unsichtbar bleiben.

### MyEntityAIFollowOwner

**Rolle:** Folgen des Besitzers mit Tag/Nacht-Regel und Teleport. Kopie von `EntityAIFollowOwner` mit Änderungen.

- **Felder** (12-20):
  - `thePet` (EntityTameable), `theOwner`, `theWorld`, `petPathfinder`, `maxDist`, `minDist`.
  - `field_75336_f`: Geschwindigkeit. SRG von `EntityAIFollowOwner` (joined.srg), in fields.csv ohne Namen; Bedeutung aus Nutzung (74).
  - `field_75343_h`: Neuberechnungs-Countdown (72-73).
  - `field_75344_i`: gesicherter `avoidsWater`-Wert (60, 67).
- **Konstruktor** (22-30): (pet, speed, `par3 → maxDist`, `par4 → minDist`); Mutex 3 = MOVE | LOOK, wie Vanilla (JP ug).

**Methoden:**
- `shouldExecute()` (32-39): Besitzer vorhanden; nicht sitzend; eine `Girlfriend` folgt am Valentinstag (`valentines_day != 0`) **nie**. Dann gilt:
  - (`posY < 60.0` **oder** Nacht) **und** Distanz² > (maxDist/2)², **oder**
  - Distanz² ≥ maxDist².
  - Unter Tage und nachts folgt das Tier also schon ab halber Distanz.
- `continueExecuting()` (41-56): nicht sitzend; Pfad vorhanden. Stopp, wenn `(int)x` und `(int)z` gleich denen des Besitzers sind und `(int)y` im offenen Bereich (owner−2, owner+2) liegt (51-53). Sonst Distanz² > minDist².
- `startExecuting()` (58-62): Countdown 0, `avoidsWater` sichern und auf false setzen.
- `resetTask()` (64-68): Besitzer null, Pfad löschen, `avoidsWater` wiederherstellen.
- `updateTask()` (70-89):
  - Blick auf den Besitzer (10.0f, `getVerticalFaceSpeed`).
  - Alle 10 Aufrufe `tryMoveToEntityLiving(owner, speed)`.
  - Scheitert das bei Distanz² ≥ 144.0: Teleport-Suche. Start x = `floor(owner.x) − 2`, z = `floor(owner.z) − 2`, y = `floor(owner.bb.minY)`. Geprüft wird der **5 × 5-Rand ohne das innere 3 × 3** (80): Oberseite fest bei y−1, Block y und y+1 nicht `isNormalCube`. Der erste Treffer → `setLocationAndAngles(x + 0.5, y, z + 0.5)`, Pfad löschen.

**Nutzer (speed, maxDist, minDist):**

| Werte | Klassen (Datei:Zeile) |
|---|---|
| 1.4, 12, 1.5 | Boyfriend.java:106, Girlfriend.java:128 |
| 2.0, 10, 2.0 | Camarasaurus.java:37, Chipmunk.java:30, GammaMetroid.java:42, Gazelle.java:41, Lizard.java:46, Ostrich.java:49, RubberDucky.java:53, WaterDragon.java:52 |
| 1.1, 12, 2.0 | Dragon.java:84, ThePrinceAdult.java:80, ThePrinceTeen.java:84 |
| 1.1, 16, 2.0 | Leon.java:69 |
| 1.15, 12, 2.0 | Spyro.java:49, Stinky.java:51, ThePrince.java:61, ThePrincess.java:65 |
| 1.2, 10, 2.0 | Hydrolisc.java:36 |
| 1.5, 10, 2.0 | VelocityRaptor.java:36 |

**Port:** Vanilla `FollowOwnerGoal(TamableAnimal, speed, startDistance, stopDistance)` (NF FollowOwnerGoal.java:23) hat dieselbe Flag-Kombination (:29), teleportiert aber über `TamableAnimal.tryToTeleportToOwner`/`shouldTryTeleportToOwner` (NF TamableAnimal.java:257-264) mit eigener Schwelle und eigenem Suchmuster. Für 1:1 eine eigene Klasse `MyFollowOwnerGoal extends Goal`:
- `level.isDay()`, Y-Grenze 60 absolut.
- `avoidsWater` → `getPathfindingMalus(PathType.WATER)` sichern, auf 0 setzen, wiederherstellen.
- Teleport-Ring und 144.0 wie oben.
- Alle 20 Nutzer erben `EntityTameable` (classes.json) → `TamableAnimal`.

### MyEntityAIJealousy

**Rolle:** Eifersucht: Ein gezähmter Girlfriend/Boyfriend greift **ungezähmte** Artgenossen in der Nähe an.

- **Feld:** `theTameable` (8), nach dem Setzen unbenutzt.
- **Konstruktor** (10-13): (tameable, Klasse, Distanz, Chance, Sicht) → Super-Konstruktor mit `nearbyOnly = false`, Selektor null.
- `shouldExecute()` (16-53):
  1. Gezähmt, nicht sitzend, `super.shouldExecute()` (setzt `targetEntity`), Opfer vorhanden.
  2. Eigene Klasse `Girlfriend` → Opfer-Girlfriend gezähmt → false (37-43); sonst Opfer-Boyfriend gezähmt → false (45-50).
  3. Besitzer vorhanden (51-52).
  - Schritt 2 ist redundant, weil `isSuitableTarget` gezähmt gegen gezähmt schon ausschließt (MyEntityAITarget.java:81-84).
- **Chance-Semantik** (MyEntityAINearestAttackableTarget.java:44): Abbruch bei `nextInt(100) > chance`, Erfolg also mit (chance + 1)/100 je Aufruf. Chance 5 → 6 %, 15 → 16 %, 0 → immer.

**Nutzer:** nur bei `PlayNicely == 0`:
- Girlfriend: Priorität 4 (Girlfriend.class, 6.0f, 5, Sicht), Priorität 5 (3.0f, 15, Sicht) (Girlfriend.java:148-153).
- Boyfriend: dasselbe mit Boyfriend.class (Boyfriend.java:123-126).

**Port:** Unterklasse des portierten `MyNearestAttackableTargetGoal`, `Flag.TARGET`.

### MyEntityAINearestAttackableTarget

**Rolle:** Zielsuche für **gezähmte** Begleiter (Leibwächter-Verhalten).

- **Felder** (10-14): `targetEntity` (Typ `EntityLiving`), `targetClass`, `targetChance`, `targetEntitySelector`, `theNearestAttackableTargetSorter`.
- **Konstruktoren** (16-32): 5 Argumente → `nearbyOnly = false` und Selektor null; 6 Argumente; 7 Argumente setzen Distanz, Chance, Sortierer, Selektor; Mutex 1.

**Methoden:**
- `shouldExecute()` (34-58):
  1. Ungezähmtes `EntityTameable` → false (35-37). Das Goal wirkt **nur** für gezähmte Tiere.
  2. `Girlfriend` ungezähmt oder sitzend → false (38-43). Die Sitzprüfung gilt nur für Girlfriend, ein sitzender `Boyfriend` sucht weiter Ziele.
  3. Chance wie bei Jealousy (44-46).
  4. `selectEntitiesWithinAABB(targetClass, bb.expand(d, 4.0, d), selector)` (47), Sortierung nach `MyEntityAINearestAttackableTargetSorter` (48), das erste mit `isSuitableTarget(e, false)` (49-55).
  - Die Liste wird hart auf `EntityLiving` gecastet (50). Eine Zielklasse mit `EntityPlayer` würfe `ClassCastException`; die aktuellen Nutzer verwenden nur `EntityCreeper`, `EntityLiving`, `Girlfriend`, `Boyfriend`.
- `startExecuting()` (60-64): `setAttackTarget`, danach `super`.

**Nutzer:** nur bei `PlayNicely == 0`:
- Girlfriend: Priorität 2 `(EntityCreeper, 20.0f, 0, true, true, IMob.mobSelector)`, Priorität 3 `(EntityLiving, 15.0f, 0, true, true, IMob.mobSelector)` (Girlfriend.java:142-147).
- Boyfriend identisch (Boyfriend.java:117-120).

**Port:** Vanilla `NearestAttackableTargetGoal` benutzt `randomInterval` mit `nextInt(interval) != 0` (NF NearestAttackableTargetGoal.java:49) und `TargetingConditions`, das weicht ab. Deshalb eigene Klasse. Suchquader `inflate(d, 4.0, d)` wie Vanilla (:58), Selektor `e -> e instanceof Enemy`, eigener Sortierer.

### MyEntityAINearestAttackableTargetSorter

**Rolle:** `Comparator` für `MyEntityAINearestAttackableTarget`.

- **Felder:** `theEntity`, `parent` (unbenutzt) (8-9).
- `compareDistanceSq` (17-27): Distanz², Creeper halbiert, aufsteigend. Ohne Größengewichtung (anders als `GenericTargetSorter`).
- `compare` delegiert (29-32).

**Port:** `Comparator<Entity>` mit `comparingDouble`.

### MyEntityAITarget

**Rolle:** abstrakte Basis der eigenen Ziel-Goals, abgeleitet von `EntityAITarget`.

- **Felder** (13-19): `taskOwner` (EntityLiving), `targetDistance`, `shouldCheckSight`, `nearbyOnly`, `targetSearchStatus` (0 = unbekannt, 1 = erreichbar, 2 = unerreichbar), `targetSearchDelay`, `field_75298_g` (MCP `targetUnseenTicks`, fields.csv).
- **Konstruktoren** (21-33).

**Methoden:**
- `continueExecuting()` (35-59):
  - Ziel null → false.
  - Tot → Ziel löschen, false.
  - Distanz² > `targetDistance`² → false.
  - Beide gezähmt → false.
  - Mit Sichtprüfung: sichtbar → Zähler 0, sonst `++zähler > 60` → false (50-57).
- `startExecuting()` (61-65): drei Zähler auf 0.
- `resetTask()` (67-69): `setAttackTarget(null)`.
- `isSuitableTarget(ziel, par2)` (71-122). `par2` ist unbenutzt, die Reihenfolge ist entscheidend:
  1. null / selbst / tot → false (72-80).
  2. Eigentümer gezähmt: gezähmtes Ziel → false; Ziel = eigener Besitzer → false (81-88).
  3. `EntityPlayer` → **nur am Valentinstag** gültig, ohne Sicht- und Kreativprüfung (89-91).
  4. `EntityPigZombie`, `EntityEnderman` → false (92-97).
  5. `Mothra` → true, **vor** der Sichtprüfung (98-100).
  6. Sichtprüfung (101-103).
  7. `EntityCreeper`, `EntityGhast` → true, ohne Erreichbarkeitsprüfung (104-109).
  8. `nearbyOnly`: Countdown, bei 0 `canEasilyReach` neu auswerten, Status 2 → false (110-120).
- `canEasilyReach(ziel)` (124-137): `targetSearchDelay = 10 + nextInt(5)`; Pfad zum Ziel; der Endpunkt muss horizontal Distanz² ≤ 2.25 zum Ziel-Block haben.

**Port:** abstrakte `MyTargetGoal extends Goal`. Vanilla `TargetGoal` hat dieselbe Struktur (`unseenMemoryTicks = 60`, NF TargetGoal.java:28; `reachCache`, :105-113), prüft aber über `TargetingConditions` inklusive Kreativ/Spectator. Die Sonderregeln (Valentinstag, Mothra, PigZombie → `ZombifiedPiglin`) verlangen eigenen Code.
- Takt: Vanilla zählt `reducedTickDelay(unseenMemoryTicks)` (NF TargetGoal.java:64), also halbiert, weil Goals nicht jeden Tick laufen. Für 1:1 entweder `requiresUpdateEveryTick()` oder dieselbe Umrechnung. Offen: den genauen Goal-Takt in `Mob.serverAiStep` 21.1.248 nachsehen.
- Pfad: `getNavigation().createPath(entity, 0)`, `getEndNode()`.

### MyEntityAIWander

**Rolle:** zufälliges Umherlaufen mit fester Rate.

- **Felder** (10-14): `entity`, `x/y/zPosition`, `speed` (float). Mutex 1.
- `shouldExecute()` (22-37): `entity.getRNG().nextInt(90) != 0` → false (23); sitzendes Tameable → false; `findRandomTarget(entity, 10, 7)` (29).
- `continueExecuting()` (39-48): Tameable steht auf der ganzzahligen x/z-Position des Besitzers, y im offenen Bereich ±2 → Stopp (40-45); sonst `!noPath()`.
- `startExecuting()` (50-52): `tryMoveToXYZ(ziel, speed)`.

**Aufrufstellen:** 24 (grep `new MyEntityAIWander(`), speed 0.65f × 1, 0.75f × 10, 0.9f × 1, 1.0f × 12. Beispiel: Girlfriend Priorität 8 mit 0.75f (Girlfriend.java:136).

**Port:** eigene `MyWanderGoal extends Goal`, `Flag.MOVE`, `DefaultRandomPos.getPos(mob, 10, 7)` (NF DefaultRandomPos.java:10). Vanilla `RandomStrollGoal` rechnet das Intervall mit `reducedTickDelay` und hat keine Sitz- oder Besitzer-Stopp-Regel.

### MyEntityAIWanderALot

**Rolle:** Umherlaufen mit einstellbarem Radius und Pause-Schalter.

- **Felder** (10-16): `entity`, `x/y/zPosition`, `speed` (double), `xzRange` (erst 10, dann Parameter; 19, 22), `busy` (int).
- `setBusy(int)` (27-29): nur von `Godzilla` genutzt (1 bei Godzilla.java:354, 0 bei :398).
- `shouldExecute()` (31-49): `busy != 0` → false; `entity.worldObj.rand.nextInt(30) != 0` → false (35). Das ist die **Welt-RNG**, nicht die Entity-RNG. Sitzend → false; `findRandomTarget(entity, xzRange, 7)`.
- `continueExecuting()`: `!noPath()` (51-53).
- `startExecuting()`: `tryMoveToXYZ(ziel, speed)` (55-57).

**Aufrufe:** 49 Klassen (grep). Verteilung (xzRange, speed): (8, 1.0) × 2, (9, 1.0) × 3, (10, 1.0) × 9, (14, 1.0) × 13, (15, 1.0) × 1, (16, 0.5) × 1, (16, 1.0) × 19, (20, 1.0) × 1.

**Port:** eigene `MyWanderALotGoal`, Zufall aus `level.random`, `setBusy` beibehalten.

### MyValentineTarget

**Rolle:** Aggression der `Girlfriend` gegen Spieler und Boyfriends, nur am Valentinstag.

- **Felder** (9-14): `targetEntity`, `Me`, `targetClass`, `targetChance`, `targetEntitySelector`, `theNearestAttackableTargetSorter` (MyValentineTargetSorter).
- **Konstruktoren** (16-35): Alle drei setzen `Me` (redundant); Mutex 1.

**Methoden:**
- `shouldExecute()` (37-64):
  1. `OreSpawnMain.valentines_day == 0` → false.
  2. `Me` ist Girlfriend mit `feelingBetter != 0` → false (41-46).
  3. Chance (47-49).
  4. Suche in `bb.expand(d, 4.0, d)` (50), Sortierung rein nach Distanz, erstes `EntityLivingBase` mit `isSuitableTarget` (52-61).
  - Keine Zähm-Pflicht. Den eigenen Besitzer schließt `isSuitableTarget` aus (MyEntityAITarget.java:85-87).
- `startExecuting()` (66-70).

**Nutzer:** Girlfriend Priorität 1 `(EntityPlayer, 16.0f, 0, true, true)` und Priorität 2 `(Boyfriend, 16.0f, 0, true, true)` (Girlfriend.java:140-141). Immer aktiv, unabhängig von `PlayNicely`.

**Valentinstag-Schalter:** `GregorianCalendar.get(2)` (Monat, nullbasiert) und `get(5)` (Tag) (OreSpawnMain.java:4178-4180). `nowmonth == 1 && nowday == 14` → 14. Februar (4227-4229), einmalig beim Laden ausgewertet. Weitere Folgen: Girlfriend-Hitbox 2.5 × 8.0 (Girlfriend.java:121-123), kein Folgen (MyEntityAIFollowOwner.java:38).

**Port:** eigene Goal-Klasse auf `MyTargetGoal`; Datum per `java.time.LocalDate.now()` beim Server-Start (Monat 2, Tag 14).

### MyValentineTargetSorter

**Rolle:** `Comparator` für `MyValentineTarget`.

- **Felder:** `theEntity`, `parent` (unbenutzt) (8-9).
- `compareDistanceSq` (16-20): reine Distanz², aufsteigend, ohne Creeper-Sonderfall.

**Port:** `Comparator.comparingDouble(mob::distanceToSqr)`.

### KeyHandler

**Rolle:** clientseitige Tastenbelegung für „Reittier hoch/schneller“.

**Felder:**
- `mc` (nur im Konstruktor gesetzt).
- `KEY_CATEGORY = "key.categories.orespawn"` (12).
- `KEY_FLY_UP = new KeyBinding("OreSpawn UP/FAST", 56, "key.categories.orespawn")` (25).
- Offen: 56 ist ein LWJGL-2-Tastencode. Nach LWJGL-2-Konvention wäre das `Keyboard.KEY_LMENU` (linke Alt-Taste); ein LWJGL-2-Jar liegt nicht im Repo, der Beleg fehlt.
- In `reference/jar/lang_dump.txt` gibt es für Beschreibung und Kategorie keinen Übersetzungseintrag, im Menü erscheinen also die Rohtexte.

**Methoden:**
- Konstruktor (15-18): `ClientRegistry.registerKeyBinding(KEY_FLY_UP)`.
- `onKeyInput(KeyInputEvent)` (20-22): `@SubscribeEvent`, leer.

Die Instanz liegt in `OreSpawnMain.MyKeyhandler` (OreSpawnMain.java:31; statisch null in :6204) und wird nirgends gelesen (grep). Der eigentliche Abfrager ist `RiderControl`.

**Port:** `com.swbr.orespawn.client.OreSpawnKeys` mit `new KeyMapping("key.orespawn.fly_up", InputConstants.KEY_LALT, "key.categories.orespawn")` (NF KeyMapping.java:88; `KEY_LALT = 342`, NF InputConstants.java:128, sofern sich die Alt-Zuordnung bestätigt). Registrierung in `RegisterKeyMappingsEvent`. `en_us.json` bekommt `"key.orespawn.fly_up": "OreSpawn UP/FAST"`, damit der sichtbare Text 1:1 bleibt.

### RiderControl

**Rolle:** clientseitiger Tick-Listener, der den Tastenzustand bei Änderung an den Server schickt.

- **Felder** (9-11): `rcm` (eine wiederverwendete `RiderControlMessage`), `network`, `keystate` (letzter gesendeter Zustand, Start 0).
- **Konstruktor** (13-17).
- `onTick(ClientTickEvent)` (19-31):
  - `newkeystate = KEY_FLY_UP.getIsKeyPressed() ? 1 : 0` (23-25).
  - Bei Änderung `rcm.keystate` setzen, `sendToServer(rcm)`, merken (26-30).
  - Die lokale Variable `myKeyhandler` ist unbenutzt (22).
  - Keine Phasen-Prüfung: Der Handler läuft in beiden Tick-Phasen. Das folgt aus dem FML-1.7.10-Verhalten, im Repo nicht geprüft; wegen der Änderungsprüfung ist es harmlos.
  - Keine Prüfung auf Welt oder Verbindung. Offen: Was `sendToServer` im Hauptmenü tut, ist nicht geprüft.

**Port:** Client, `@SubscribeEvent ClientTickEvent.Post` (NF ClientTickEvent.java:33) auf dem Game-Bus:
- nur senden, wenn `Minecraft.getInstance().getConnection() != null`;
- `OreSpawnKeys.FLY_UP.isDown()`;
- `PacketDistributor.sendToServer(new RiderControlPayload(state))` (NF PacketDistributor.java:38);
- beim Login den Zustand zurücksetzen (sonst sieht der Server nach einem Wiederbeitritt einen alten Wert).

### RiderControlMessage

**Rolle:** Netzwerknachricht, 1 Byte.

- **Felder:** `public int keystate` (8), `private int previous` (9).
- `fromBytes` liest `readUnsignedByte` (15-17); `toBytes` schreibt `writeByte(keystate)` (19-21).
- `fromInteger`/`toInteger` sind trivial (23-29).
- `hasChanged()` (31-36) vergleicht mit `previous` und aktualisiert es, ist aber **unbenutzt** (grep).

**Port:** `record RiderControlPayload(boolean flyUp) implements CustomPacketPayload`, `TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("orespawn", "rider_controls"))`, `STREAM_CODEC` über `ByteBufCodecs.BOOL`. Alternativ Byte für Formtreue; das Netzformat ist ohnehin nicht 1.7.10-kompatibel.

### RiderControlMessageHandler

**Rolle:** serverseitiger Empfänger (`@ChannelHandler.Sharable`).

- **Feld:** `L` (Logger, unbenutzt) (11, 22).
- `onMessage(msg, ctx)` (13-19): Client-Seite → null. Sonst `OreSpawnMain.flyup_keystate = msg.keystate` (17). Das ist **ein einziger statischer Wert für den ganzen Server**, gesetzt direkt auf dem Netty-Thread ohne Übergabe an den Server-Thread.

**Leser von `flyup_keystate`:** `Cephadrome.java:792`, `Dragon.java:971`, `Elevator.java:444`, `Leon.java:821`, `Ostrich.java:460`, `ThePrinceAdult.java:935`, `ThePrinceTeen.java:959`. Beispiele:
- Dragon: `motionY += 0.03` und `+= velocity × 0.036` je Tick bei gedrückter Taste (Dragon.java:971-974).
- Ostrich: einmalig `motionY += 1` und `+= velocity × 6.0`, danach 20 Ticks Sperre über `didjump` (Ostrich.java:460-468).

Zurückgesetzt wird der Wert nur im statischen Initialisierer (OreSpawnMain.java:6205). **Folge im Mehrspielerbetrieb:** Drückt ein Spieler die Taste, steigen die Reittiere aller Spieler.

**Port:**
- `IPayloadHandler` mit `context.enqueueWork(...)` speichert den Zustand **je Spieler**, per `AttachmentType<Boolean>` `orespawn:fly_up` am `ServerPlayer`, beim Logout gelöscht.
- Die sieben Reittiere lesen den Wert von `getControllingPassenger()`.
- Das ist eine bewusste Abweichung vom globalen Original. Im Einzelspieler ist das Verhalten identisch.
