# Verhalten: itemblock-02

Dieser Stapel umfasst 34 Item-Klassen und 21 Block-Klassen. Die Items: 114 Spawn-Eier über eine Klasse, Roboter-Kits, Wrench, ZooKeeper-Scherbe, Wurfgeschosse (Sunspot Urchin, WaterDragon Charge), SquidZooka, Thunder Staff, Nahrung (Obst, Sun Fish und ihre Effekt-Varianten, Tomate, Erdbeersamen), Schwerter (Mantis Claw, Nightmare, Poison, Rat, Ruby, Ultimate plus Battle Axe, Chainsaw und Queen Battle Axe), das Ruby- und das Ultimate-Werkzeugset, zwei Bögen, die Ultimate-Angel, drei Bauhilfen (Treppen und Brücke) und die Zoo-Käfige. Die Blöcke: Ameisennester, drei Blattarten, Setzlinge, fünf Nutzpflanzen, vier Insektenpflanzen, Kristall- und Extrem-Fackel, Kristallholz, Duplikator-Holz, Kristallblöcke, Pizza und Duct Tape. Ein Befund gilt für den ganzen Stapel: **viele scheinbare Overrides sind im ausgelieferten Jar tot**, weil sie keinen SRG-Namen tragen. Betroffen sind `getDamageVsEntity`, `getMaterialName`, `hitEntity(…, EntityLiving, EntityLiving)`, `canHarvestBlock(Block)`, `getStrVsBlock`, `onNeighborBlockChange(…, int)`, `idPicked`, `itemPicked`, `getSeedItem`, `getCropItem`, `getItem(int, Random, int)` und `tickRate()`. Belegt ist das per `javap -p` auf `reference/jar/orespawn-1.7.10-20.3.jar`: lebende Overrides heißen dort `func_…`, Forge-Hooks tragen Klarnamen (`onLeftClickEntity`, `onUsingTick`, `onEntitySwing`). Der zweite Befund: Mais und Quinoa speichern ihre Zielhöhe in `meta >> 8`, obwohl Metadaten in 1.7.10 nur 4 Bit haben. Die Höhe wird deshalb bei jedem Tick neu gewürfelt. Drittens hängen mehrere Zähler als Instanzfeld am Item- bzw. Block-Singleton (`ticker`, `swingtimer`, `leaf`, `myMaxHeight`) und gelten damit für alle Stacks, Spieler und in Einzelspieler für beide Seiten gemeinsam.

## Querschnitt: verifizierte Vanilla-1.7.10-Formeln

Alle aus `reference/jar/mcp/client-1.7.10.jar` per `javap -c`, Klassennamen über `joined.srg`.

| Vanilla | Formel | Beleg |
|---|---|---|
| `ItemSword` Angriffs-Modifier | `4.0f + ToolMaterial.getDamageVsEntity()` | aeh.<init> |
| `ItemTool` Angriffs-Modifier | `f + material.damage`, `f` = 3.0 (`ItemAxe`), 2.0 (`ItemPickaxe`), 1.0 (`ItemSpade`) | acg/abf/adn/ady.<init> |
| `ItemTool` Effizienz | `material.efficiency` | acg.<init> |
| `ItemHoe` | kein Angriffs-Modifier | ada.<init> |
| `ItemSword.getStrVsBlock` | Spinnennetz 15.0; fünf Materialien (u. a. `awt.j` = leaves, laut `alt.<init>`) 1.5; sonst 1.0 | aeh.a(add,aji) |
| `ItemSword.canItemHarvestBlock` | nur Spinnennetz (`Blocks.web`) | aeh.b(aji), FD ajn/G |
| `ItemSword.hitEntity` | `damageItem(1)` | aeh.a(add,sv,sv) |
| `Block.setLightLevel(f)` | `lightValue = (int)(15 * f)` | aji.a(F) |
| `Block.setHardness(h)` | `hardness = h`; Resistenz mindestens `5h` | aji.c(F) |
| `Block.setResistance(r)` | Resistenzfeld = `3r` | aji.b(F) |
| `Block.getExplosionResistance` | Resistenzfeld / 5 | aji.a(sa) |
| `BlockLeaves.<init>` | TickRandomly, Tab Decorations, Härte 0.2, LightOpacity 1, Grass-Sound | alt.<init> |
| `BlockCrops` Wachstum | Licht über dem Block ≥ 9, `meta < 7`, `rand.nextInt((int)(25/f)+1) == 0` → `meta+1`; `f` aus Ackerboden-Umgebung (Mittelfeld 1 bzw. 3 wenn feucht, Nachbarn /4, halbiert bei gleichartigen Nachbarn) | akf.a(ahb,III,Random), akf.n |
| `BlockCrops` Drop | `meta == 7` → `getCrop()`, sonst `getSeed()`; bei `meta ≥ 7` zusätzlich `3 + fortune` Würfe `rand.nextInt(15) <= meta` → 1 Samen | akf.a(int,Random,int), akf.a(ahb,IIIIFI) |
| `BlockCrops` sonst | Boden nur Farmland (`ajn.ak`), Pick-Block = `getSeed()`, `quantityDropped` 1, TickRandomly im Konstruktor | akf |
| `BlockReed` | Drop **und Pick-Block** = `Items.reeds` (Zuckerrohr) | ane.a/ane.d, FD ade/aE |

Umrechnung der Blockwerte nach 1.21.1: `strength(hardness, resistance)` mit `resistance = setResistance-Argument × 3 / 5`. Wurde nur `setHardness` aufgerufen, gilt `resistance = hardness`. Offen: dass die 1.7.10-Explosion `getExplosionResistance` unverändert wie 1.21.1 verrechnet, ist nicht disassembliert.

---

## Items

### ItemSpawnEgg

114 Registry-IDs, alle `kind = spawn_egg`, Textur je `assets/orespawn/textures/item/<id>.png` (manifest). Das zweite Konstruktor-Argument `my_id` wählt das Ziel.

| Registry-ID | Lang-Name | my_id | Ziel (manifest entity id bzw. Vanilla) |
|---|---|---|---|
| eggwitherskeleton | Spawn Wither Skeleton | 192 | Vanilla-ID 51 + `setSkeletonType(1)` |
| eggenderdragon | Spawn Ender Dragon | 193 | Vanilla-ID 63 |
| eggsnowgolem | Spawn Snow Golem | 194 | Vanilla-ID 97 |
| eggirongolem | Spawn Iron Golem | 195 | Vanilla-ID 99 |
| eggwitherboss | Spawn Wither Boss | 196 | Vanilla-ID 64 |
| egggirlfriend | Spawn Girlfriend | 197 | girlfriend |
| eggredcow | Spawn Apple Cow | 198 | apple_cow |
| eggcrystalcow | Spawn Crystal Cow | 363 | crystal_apple_cow |
| egggoldcow | Spawn Golden Apple Cow | 199 | golden_apple_cow |
| eggenchantedcow | Spawn Enchanted Golden Apple Cow | 200 | enchanted_golden_apple_cow |
| eggmothra | Spawn MOTHRA! | 201 | mothra |
| eggalosaurus | Spawn Alosaurus | 202 | alosaurus |
| eggcryolophosaurus | Spawn Cryolophosaurus | 203 | cryolophosaurus |
| eggcamarasaurus | Spawn Camarasaurus | 204 | camarasaurus |
| eggvelocityraptor | Spawn Velocity Raptor | 205 | velocity_raptor |
| egghydrolisc | Spawn Hydrolisc | 206 | hydrolisc |
| eggbasilisc | Spawn Basilisk | 207 | basilisk |
| eggdragonfly | Spawn Dragonfly | 221 | dragonfly |
| eggemperorscorpion | Spawn Emperor Scorpion! | 223 | emperor_scorpion |
| eggscorpion | Spawn Scorpion | 225 | scorpion |
| eggcavefisher | Spawn Cave Fisher | 227 | cave_fisher |
| eggspyro | Spawn Baby Dragon | 229 | baby_dragon |
| eggbaryonyx | Spawn Baryonyx | 231 | baryonyx |
| egggammametroid | Spawn WTF? | 233 | wtf |
| eggcockateil | Spawn Bird | 235 | bird |
| eggkyuubi | Spawn Kyuubi | 237 | kyuubi |
| eggalien | Spawn Alien | 239 | alien |
| eggattacksquid | Spawn Attack Squid | 241 | attack_squid |
| eggwaterdragon | Spawn Water Dragon | 243 | water_dragon |
| eggkraken | Uh, no. Don't. | 245 | the_kraken |
| egglizard | Spawn Lizard | 247 | lizard |
| eggcephadrome | Spawn Cephadrome | 249 | cephadrome |
| eggdragon | Spawn Dragon | 251 | dragon |
| eggbee | Spawn Bee | 254 | bee |
| eggtrooper | Spawn Jumpy Bug | 262 | jumpy_bug |
| eggspit | Spawn Spit Bug | 263 | spit_bug |
| eggstink | Spawn Stink Bug | 264 | stink_bug |
| eggostrich | Spawn Ostrich | 265 | ostrich |
| egggazelle | Spawn Gazelle | 266 | gazelle |
| eggchipmunk | Spawn Chipmunk | 267 | chipmunk |
| eggcreepinghorror | Spawn Creeping Horror | 274 | creeping_horror |
| eggterribleterror | Spawn Terrible Terror | 275 | terrible_terror |
| eggcliffracer | Spawn Cliff Racer | 276 | cliff_racer |
| eggtriffid | Spawn Triffid | 277 | triffid |
| eggnightmare | Spawn Nightmare!!! | 278 | nightmare |
| egglurkingterror | Spawn Lurking Terror | 279 | lurking_terror |
| egggodzilla | Spawn Mobzilla | 280 | mobzilla |
| eggsmallworm | Spawn Small Worm | 288 | small_worm |
| eggmediumworm | Spawn Medium Worm | 289 | medium_worm |
| egglargeworm | Spawn Large Worm | 290 | large_worm |
| eggcassowary | Spawn Cassowary | 291 | cassowary |
| eggcloudshark | Spawn Cloud Shark | 292 | cloud_shark |
| egggoldfish | Spawn Gold Fish | 293 | gold_fish |
| eggleafmonster | Spawn Leaf Monster | 294 | leaf_monster |
| eggtshirt | Spawn T-Shirt! | 295 | t_shirt |
| eggenderknight | Spawn Ender Knight | 298 | ender_knight |
| eggenderreaper | Spawn Ender Reaper | 299 | ender_reaper |
| eggbeaver | Spawn Beaver | 301 | beaver |
| eggrotator | Spawn Rotator | 302 | rotator |
| eggvortex | Spawn Vortex | 303 | vortex |
| eggpeacock | Spawn Peacock | 304 | peacock |
| eggfairy | Spawn Fairy | 305 | fairy |
| eggdungeonbeast | Spawn Dungeon Beast | 306 | dungeon_beast |
| eggrat | Spawn Rat | 307 | rat |
| eggflounder | Spawn Flounder | 308 | flounder |
| eggwhale | Spawn Whale | 309 | whale |
| eggirukandji | Spawn Irukandji | 310 | irukandji |
| eggskate | Spawn Skate | 311 | skate |
| eggurchin | Spawn Crystal Urchin | 312 | crystal_urchin |
| eggrobot1 | Spawn Bomb-Omb | 324 | bomb_omb |
| eggrobot2 | Spawn Robo-Pounder | 325 | robo_pounder |
| eggrobot3 | Spawn Robo-Gunner | 326 | robo_gunner |
| eggrobot4 | Spawn Robo-Warrior | 327 | robo_warrior |
| eggghost | Spawn Ghost | 328 | ghost |
| eggghostskelly | Spawn Ghost Pumpkin Skelly | 329 | ghost_pumpkin_skelly |
| eggbrownant | Spawn Brown Ant | 330 | ant |
| eggredant | Spawn Red Ant | 331 | red_ant |
| eggrainbowant | Spawn Rainbow Ant | 332 | rainbow_ant |
| eggunstableant | Spawn Unstable Ant | 333 | unstable_ant |
| eggtermite | Spawn Termite | 334 | termite |
| eggbutterfly | Spawn Butterfly | 335 | butterfly |
| eggmoth | Spawn Moth | 336 | moth |
| eggmosquito | Spawn Mosquito | 337 | mosquito |
| eggfirefly | Spawn Firefly | 338 | firefly |
| eggtrex | Spawn T. Rex | 339 | t_rex |
| egghercules | Spawn Hercules Beetle | 340 | hercules_beetle |
| eggmantis | Spawn Mantis | 341 | mantis |
| eggstinky | Spawn Stinky | 342 | stinky |
| eggrobot5 | Spawn Robo-Sniper | 343 | robo_sniper |
| eggcoin | Spawn Coin | 344 | coin |
| eggboyfriend | Spawn Boyfriend | 349 | boyfriend |
| eggtheking | Spawn The King | 350 | the_king |
| eggtheprince | Spawn The Prince | 351 | the_prince |
| eggeasterbunny | Spawn Easter Bunny | 352 | easter_bunny (globaler Name „Easter Bunny“, OreSpawnMain.java:4013) |
| eggmolenoid | Spawn Molenoid | 353 | molenoid |
| eggseamonster | Spawn Sea Monster | 354 | sea_monster |
| eggseaviper | Spawn Sea Viper | 355 | sea_viper |
| eggcaterkiller | Spawn CaterKiller | 356 | cater_killer |
| eggleon | Spawn Leonopteryx | 358 | leonopteryx |
| egghammerhead | Spawn Hammerhead | 360 | hammerhead |
| eggrubberducky | Spawn Rubber Ducky | 362 | rubber_ducky |
| eggcriminal | Spawn Criminal | 365 | criminal |
| eggthequeen | Spawn The Queen | 366 | the_queen |
| eggbrutalfly | Spawn Brutalfly | 367 | brutalfly |
| eggnastysaurus | Spawn Nastysaurus | 368 | nastysaurus |
| eggpointysaurus | Spawn Pointysaurus | 369 | pointysaurus |
| eggcricket | Spawn Cricket | 370 | cricket |
| eggtheprincess | Spawn The Princess | 371 | the_princess |
| eggfrog | Spawn Frog | 372 | frog |
| eggrobot6 | Spawn Jeffery | 378 | jeffery |
| eggantrobot | Spawn Red Ant Robot | 379 | robot_red_ant |
| eggspiderrobot | Spawn Giant Spider Robot | 380 | robot_spider |
| eggspiderdriver | Spawn Spider Robot Driver | 381 | spider_driver |
| eggcrab | Spawn Crab | 383 | crab |

Die Zuordnung wurde per Skript geprüft: alle 114 `case`-Namen lösen auf eine Manifest-Entity auf, und kein Ei ist ohne `case`. Die Vanilla-IDs sind durch die Lang-Namen der Eier belegt, nicht durch `EntityList` disassembliert.

- **Rolle:** Spawn-Ei für OreSpawn- und Vanilla-Mobs; Basis `Item`.
- **Werte:** Stackgröße 64 (ItemSpawnEgg.java:19), Tab Misc (ItemSpawnEgg.java:20), keine Haltbarkeit.
- **Verhalten `onItemUse`** (ItemSpawnEgg.java:23-35):
  1. Client: sofort `true` (:24-26).
  2. Server: `spawn_something(my_id, world, x+0.5, y+1.01, z+0.5)`. Maßgeblich ist der angeklickte Block, nicht die angeklickte Seite (:27).
  3. Trägt der Stack einen Anzeigenamen, wird er als `CustomNameTag` gesetzt (:28-30).
  4. Außer im Creative-Modus wird 1 verbraucht (:31-33).
- **`spawn_something`** (ItemSpawnEgg.java:37-509): `switch` von `my_id` auf die Vanilla-Entity-ID oder den globalen Entity-Namen. Beim Wither Skeleton gilt `skelly_type = 1`, `entityID = 51` (:43-44), danach `setSkeletonType(1)` (:503-506).
- **`spawnCreature`** (ItemSpawnEgg.java:511-525): `createEntityByID` bzw. `createEntityByName`, Gierwinkel `rand * 360` (:520), `spawnEntityInWorld`, dann ungeprüft `((EntityLiving) e).playLivingSound()` (:522).
- **Externer Aufrufer:** `DispenserBehaviorOreSpawnEgg.java:19` ruft `spawn_something` mit `(int)`-gecasteten Koordinaten auf (ohne +0.5). Registriert wird das pro Ei in `OreSpawnMain.java:5300ff`.
- **Rezept/GUI:** keine. **Sounds:** das `playLivingSound` der Entity. **Config:** keine in dieser Klasse; ob eine abgeschaltete Mob-Art trotzdem per Ei spawnt, ist offen (die Klasse prüft keine `*Enable`-Keys).
- **Portierung 1.21.1:**
  - Eigene `Item`-Klasse mit `useOn`. Die Eier haben eigene Texturen, `DeferredSpawnEggItem` (zweifarbig) passt daher nicht.
  - Spawn per `EntityType.spawn(serverLevel, stack, player, pos, MobSpawnType.SPAWN_EGG, …)`; der Name kommt aus `DataComponents.CUSTOM_NAME`.
  - Wither Skeleton wird zum eigenen `EntityType.WITHER_SKELETON`.
  - Dispenser-Verhalten über `DispenserBlock.registerBehavior` in `FMLCommonSetupEvent`.
  - `my_id` wird zur statischen Tabelle Item → `Supplier<EntityType<?>>`.
  - Ungeprüfter `EntityLiving`-Cast: im Port auf `Mob` prüfen.

### ItemSpiderRobotKit

| Registry-ID | Lang-Name | Konstruktor |
|---|---|---|
| spiderrobotkit | Spider Robot Kit | BaseItemID+471 (OreSpawnMain.java:1385) |
| antrobotkit | Red Ant Robot Kit | BaseItemID+473 (OreSpawnMain.java:1386) |

- **Rolle:** stellt einen reitbaren Roboter mit gespeicherter Rest-HP wieder her; Basis `Item`.
- **Werte:** Stack 1 (ItemSpiderRobotKit.java:14), Tab Tools (:15). Haltbarkeit = `SpiderRobot_stats.health` für ID +471, sonst `AntRobot_stats.health` (:16-21). Defaults 1500 bzw. 300 (OreSpawnMain.java:6146-6147; manifest config `SpiderRobot_health`, `AntRobot_health`).
- **Verhalten `onItemUse`** (:24-50):
  1. Client `true`.
  2. Name „Robot Spider“, bei `AntRobotKit` „Robot Red Ant“ (:29-32); Spawn bei x+0.5, y+1.01, z+0.5 (:33).
  3. `setHealth(getMaxDamage() - itemDamage)`: der Item-Schaden ist die fehlende HP (:36).
  4. Anzeigename wird übernommen (:37-39).
  5. Sound `random.explode`, Lautstärke 1.0, Pitch `rand*0.2+0.9` (:40).
  6. Bei `AntRobot`: `setOwned()` (:41-44).
  7. Außer im Creative-Modus Stack −1 (:46-48).
- Die Gegenseite (Kit erzeugen) steckt in `ItemWrench`.
- **Portierung:**
  - Die Rest-HP wandert in `DataComponents.DAMAGE`/`MAX_DAMAGE`.
  - **SpiderRobot: 1500 HP (Original) liegt über der `MAX_HEALTH`-Grenze von 1024.** Das Kit muss auf die virtuelle HP des Ports abbilden, nicht auf `setHealth`.
  - `random.explode` → `SoundEvents.GENERIC_EXPLODE`.

### ItemSquidZooka

`squidzookasmall` – „SquidZooka!“

- **Rolle:** Feuerwaffe, verschießt eine `AttackSquid`; Basis `Item`.
- **Werte:** Stack 1, Haltbarkeit 100, Tab Combat (ItemSquidZooka.java:15-17). Item-Renderer `RenderSquidZooka` mit `SquidZookatexture.png` 128×128 (renderers_dump.txt:138).
- **Verhalten `onItemRightClick`** (:20-48):
  1. Bei Rest-Haltbarkeit ≤ 1 passiert nichts (:21-23).
  2. Sound `random.explode` 0.5/0.5 auf beiden Seiten (:24).
  3. Server: Spawn von „Attack Squid“ bei `posX − 2.5·sin(yawHead+15°)`, `posY+1.65`, `posZ + 2.5·cos(yawHead+15°)` (:26-28); `setWasShot()` (:29-32).
  4. Geschwindigkeit = Blickrichtung aus `rotationYaw`/`rotationPitch` × 3.6 (:33-36), plus Zufall ±0.05 je Achse (:38-42).
  5. Schwung (:44). Rückstoß auf den Spieler `(cos(yawHead−90°)·0.45, 0.1, sin(yawHead−90°)·0.45)` (:45).
  6. `damageItem(1)` (:46).
- `getMaterialName` ist tot (jar). Das Nachladen per Tintenbeutel steht nicht in der Klasse (laut 03-items.md:93 ein Rezept).
- **Portierung:** `use`. Null-Check nach dem Spawn ergänzen: das Original dereferenziert `e` ungeprüft (:34). Item-Renderer client-seitig als eigenes Modell oder BEWLR.

### ItemStrawberry

| Registry-ID | Lang-Name | Hunger | Sättigung | Beleg |
|---|---|---|---|---|
| strawberry | Strawberry | 2 | 0.65 | OreSpawnMain.java:1547 |
| cherries | Cherries | 3 | 0.45 | OreSpawnMain.java:1560 |
| peach | Peach | 4 | 0.55 | OreSpawnMain.java:1561 |

- **Rolle:** einfache Nahrung; Basis `ItemFood(hunger, saturation, wolf=false)` (ItemStrawberry.java:9-11). Kein Effekt, keine Sonderlogik.
- **Portierung:** `Item.Properties().food(new FoodProperties.Builder().nutrition(n).saturationModifier(s).build())`, Modifikator 1:1 übernehmen.

### ItemStrawberrySeed

`strawberry_seed` – „Strawberry Plant“

- **Rolle:** Samen; Basis `ItemSeeds(crop, soil)` (ItemStrawberrySeed.java:12). Er setzt `MyStrawberryPlant` (`strawberry_plant`) auf `Blocks.farmland` (OreSpawnMain.java:1549). Tab Decorations (:13).
- **Portierung:** `ItemNameBlockItem` bzw. `BlockItem` auf den Crop-Block; der Boden steckt in `mayPlaceOn` des Blocks (andere Klasse `BlockStrawberry`).

### ItemSunFish

| Registry-ID | Lang-Name | Hunger | Sättigung | Effekte bei `onFoodEaten` (nur Server) |
|---|---|---|---|---|
| sunfish | Sun Fish | 6 | 0.6 | Fire Resistance 6000 t, Amp 0 (ItemSunFish.java:19-21) |
| buttercandy | Butter Candy! | 4 | 0.5 | Speed 2000 t/0, Jump Boost 2000 t/0 (:22-25) |
| cookedbacon | Bacon! | 14 | 1.5 | Regeneration 2000 t/0, Strength 2000 t/0 (:26-29) |
| cookedcrabmeat | Crab Meat! | 6 | 0.75 | keine (kein Zweig) |
| crystalapple | Crystal Apple | 5 | 0.85 | Regeneration 3000 t/0, Strength 3000 t/0 (:30-33) |
| heart | Love | 8 | 0.95 | Regeneration 6000/3, Strength 6000/2, Fire Resistance 6000/2, Resistance 6000/1, Speed 5000/0, Jump Boost 5000/0 (:34-41) |

Hunger- und Sättigungswerte: OreSpawnMain.java:1372, 1511, 1512, 1514, 1562, 1563.

- **Rolle:** Nahrung mit Effekten; Basis `ItemFood`, `setAlwaysEdible()` (:14). `onFoodEaten` (`func_77849_c`, lebt laut jar) ruft zuerst `super` auf (:18); welche Effekte folgen, entscheidet der Identitätsvergleich `this == OreSpawnMain.My…`.
- **Portierung:**
  - `FoodProperties.Builder().alwaysEdible()`.
  - Effekte entweder per `.effect(() -> new MobEffectInstance(…), 1.0f)` oder weiter in `finishUsingItem`. `effect()` wirkt ebenfalls nur serverseitig.
  - Namen: `MobEffects.FIRE_RESISTANCE`, `MOVEMENT_SPEED`, `JUMP`, `REGENERATION`, `DAMAGE_BOOST`, `DAMAGE_RESISTANCE`.
  - Identitätsvergleiche im Port durch Konstruktor-Parameter ersetzen.

### ItemSunspotUrchin

`sunspoturchin` – „Sunspot Urchin“

- **Rolle:** Wurfgeschoss; Basis `Item`. Stack 64, Tab Combat (ItemSunspotUrchin.java:14-15).
- **Verhalten `onItemRightClick`** (:18-27): außer im Creative-Modus −1 (:19-21). Sound `random.bow`, 0.5, Pitch `0.4/(rand*0.4+0.8)` (:22). Server spawnt `new SunspotUrchin(world, player)` (:23-25); Entity `sunspot_urchin` (manifest).
- **Portierung:** wie `SnowballItem#use`; Flugverhalten in der Entity-Klasse (anderer Stapel). `random.bow` → `SoundEvents.ARROW_SHOOT`.

### ItemThunderStaff

`thunderstaff` – „Thunder Staff“

- **Rolle:** Magie-Stab, verschießt `ThunderBolt`; Basis `Item`.
- **Werte:** Stack 1, Haltbarkeit 50 (Ladungen), Tab Combat (ItemThunderStaff.java:17-19); Feld `ticker = 50` (:16).
- **Verhalten `onItemRightClick`** (:22-41):
  1. Rest ≤ 1 → nichts (:23-25).
  2. `new ThunderBolt(world, player)` bei `posX − 1.0·sin(yawHead+45°)`, `posY+1.55`, `posZ + 1.0·cos(yawHead+45°)` (:26-29); Bewegung ×3.0 (:30-35).
  3. `spawnEntityInWorld` **ohne `isRemote`-Prüfung** (:36): der Client erzeugt eine lokale Geister-Entity.
  4. Schwung, Rückstoß `(cos(yawHead−90°)·0.5, 0.15, sin(yawHead−90°)·0.5)` (:37-38), `damageItem(1)` (:39).
- **`onUpdate`** (:43-53): bei Regen *und* Gewitter `ticker−−`. Bei `ticker ≤ 0` und Schaden > 0 → Schaden −1, `ticker = 50`.
- **Falle:** `ticker` ist ein Feld des Item-Singletons. Mehrere Stäbe in beliebigen Inventaren zählen denselben Zähler herunter, also laden N Stäbe zusammen N-mal schneller.
- **Portierung:** Aufladen in `inventoryTick`, nur serverseitig; Zähler per Stack (eigener DataComponent) oder aus `level.getGameTime() % 50` ableiten. Projektil nur serverseitig spawnen. `getMaterialName` ist tot.

### ItemTomato

`tomato_seed` – „Tomato“

- **Rolle:** essbarer Samen; Basis `ItemSeedFood(4, 0.55f, MyTomatoPlant1, Blocks.farmland)` (ItemTomato.java:11; OreSpawnMain.java:1581). Er pflanzt `tomato_0`.
- **Portierung:** `ItemNameBlockItem` auf `tomato_0` mit `food(...)`.

### ItemWaterBall

`waterball` – „WaterDragon Charge“

- **Rolle, Werte, Verhalten:** identisch zu `ItemSunspotUrchin`, Entity `WaterBall` (`water_ball`). Stack 64, Tab Combat (ItemWaterBall.java:14-15), Verbrauch (:19-21), Sound (:22), Spawn (:23-25).

### ItemWrench

`wrench` – „Wrench“

- **Rolle:** baut Roboter zu Kits ab; Basis `Item`. Tab Tools, Haltbarkeit 100 (ItemWrench.java:15-16); Stackgröße nicht gesetzt (Item-Default).
- **Verhalten `onLeftClickEntity`** (Forge-Hook, :19-76):
  - Unberittener **SpiderRobot** (:20):
    1. `h = maxHealth − health` (:22), `setDead()` auf beiden Seiten (:23).
    2. Server: Drop `SpiderRobotKit` mit Item-Schaden `(int)h` bei posY+1.0 (:24, dropItem :78-86).
    3. 8 Runden Partikel `smoke`, `explode`, `reddust`, Offset x/z `±3`, y `0.25..2.25` (:25-38).
    4. Sound `random.explode` 0.5/1.5 (:39).
  - Unberittener **AntRobot** (:42-44): ohne Besitzer und mit HP-Anteil > 0.5 → `false` (normaler Angriff) (:46-49). Sonst `setOwned()` (:50), danach wie oben mit `AntRobotKit` (:52-69).
  - Anderes Ziel → `false` (:42-44).
  - Erfolg: `damageItem(2)` (:71), bei Stack ≤ 0 Slot leeren (:72-74), `true` bricht den Angriff ab (:75).
- **Portierung:**
  - `IItemExtension#onLeftClickEntity` gibt es in NeoForge weiter.
  - `discard()` nur serverseitig; Partikel über `ServerLevel#sendParticles`.
  - Kit-Schaden über `DataComponents.DAMAGE`; Virtual-Health-Hinweis siehe `ItemSpiderRobotKit`.

### ItemZooKeeper

`zookeeper` – „ZooKeeper Shard“

- **Rolle:** macht einen Mob dauerhaft (kein Despawn); Basis `Item`. Tab Decorations, Haltbarkeit 1 (ItemZooKeeper.java:13-14).
- **Verhalten `onLeftClickEntity`** (:17-43):
  1. Partikel wie beim Wrench, für jedes Ziel (:18-31); Sound `random.explode` 0.5/1.5 (:32).
  2. Bei `EntityLiving`: `func_110163_bv` = `enablePersistence` (MCP methods.csv) (:35), `damageItem(2)` → das Item bricht sofort (:36-39). Rückgabe `true`, also kein Schaden am Ziel.
  3. Sonst `false`.
- **Portierung:** `Mob#setPersistenceRequired()`. Das Original prüft `entity != null` erst nach der Partikelschleife (:22 vs :33); im Port vorziehen.

### MantisClaw

`mantisclaw` – „Mantis Claw“

- **Rolle:** Schwert mit Lebensraub; Basis `ItemSword(toolEMERALD)` (OreSpawnMain.java:1329).
- **Werte:**
  - Material `REALEMERALD`: harvest 3, maxUses 1300, efficiency 10, damage 6, enchantability 75 (OreSpawnMain.java:1183, 1294; manifest tool_materials).
  - Haltbarkeit **1000** statt 1300 (MantisClaw.java:19), Stack 1, Tab Combat (:18-20).
  - Angriffs-Modifier 4.0 + 6 = **10** (Querschnitt aeh.<init>). Das Feld `weaponDamage = 10` (:17) ist ungenutzt.
  - Blockdauer 3000 Ticks (`func_77626_a`, :37-39).
- **Verhalten `hitEntity`** (`func_77644_a`, lebt; :27-35): Server: Ziel `heal(-1.0f)`, Angreifer `heal(1.0f)` (:29-32). Danach `damageItem(1)` (:33).
- `getMaterialName` ist tot.
- **Portierung:**
  - Eigener `SimpleTier` „realemerald“ mit `uses = 1000`. Beachten: 1.21.1-`TieredItem` bezieht die Haltbarkeit aus dem Tier; ob `Properties.durability` danach greift, im NeoForge-sources-jar prüfen (offen).
  - `SwordItem.createAttributes(tier, 4, speed)` statt der Vanilla-3, damit der Modifier 4 + Bonus wie in 1.7.10 ergibt. Angriffsgeschwindigkeit gibt es in 1.7.10 nicht: offen.
  - Lebensraub in `hurtEnemy`: `target.setHealth(target.getHealth()-1)`, `attacker.heal(1)`.

### NightmareSword

`nightmaresword` – „Nightmare Sword“

- **Rolle:** Schwert mit Pflicht-Verzauberungen; Basis `ItemSword(toolNIGHTMARE)` (OreSpawnMain.java:1312).
- **Werte:**
  - Material NIGHTMARE: 3/1800/12/26/60 (OreSpawnMain.java:1175).
  - Haltbarkeit **1200** (NightmareSword.java:17), Stack 1.
  - Angriffs-Modifier 4 + 26 = **30**. Blockdauer 5000 (:49-51).
- **Verhalten:**
  - `onCreated` (`func_77622_d`): Sharpness I, Knockback III, Fire Aspect I (:21-25).
  - `onUsingTick` fügt alle drei neu hinzu, sobald Knockback fehlt (:27-34). `onUpdate` ruft das **jeden Tick** im Inventar auf (:36-38): wer die Verzauberung entfernt, bekommt sie sofort zurück. `addEnchantment` hängt dabei an, ohne Dubletten zu prüfen.
  - `hitEntity(ItemStack, EntityLiving, EntityLiving)` ist tot (jar) → Vanilla `damageItem(1)`. `getMaterialName` tot.
- **Abgleich:** 03-items.md:68 nennt 30 (Code) und 1200 (Klasse). Stimmt.
- **Portierung:**
  - `onCraftedBy` plus serverseitiges `inventoryTick`, das `stack.enchant(holder, lvl)` über `level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)` ausführt.
  - Ein statischer Default-`ENCHANTMENTS`-Component ist nicht möglich: die Enchantment-Registry ist datengetrieben und zur Registrierungszeit nicht verfügbar.

### PoisonSword

`poisonsword` – „Poison Sword“

- **Rolle:** Giftschwert; Basis `ItemSword(toolEMERALD)` (OreSpawnMain.java:1326).
- **Werte:** Haltbarkeit **1300** (PoisonSword.java:23), Stack 1, Angriffs-Modifier 4 + 6 = 10, Blockdauer 3000 (:56-58).
- **Verhalten:**
  - `onCreated` → Sharpness I (:27-29). `onUsingTick` fügt Sharpness I neu hinzu, wenn es fehlt (:31-36). Es gibt **kein** `onUpdate`, also greift das nur beim Blocken.
  - `hitEntity` (lebt, :42-54), für das Ziel:
    - Poison `(10+rand(10))·20` t, Amp 0 (:45-46)
    - Wither ebenso, neuer Wurf (:47-48)
    - Weakness ebenso, neuer Wurf (:49-50)
    - danach `damageItem(1)` (:52).
    - Dauer je Effekt 200..380 Ticks.
- **Abgleich:** 03-items.md:72 nennt 10-19 s. Stimmt.
- **Portierung:** Effekte in `hurtEnemy`. Schwertblocken gibt es in 1.21.1 nicht, der `onUsingTick`-Pfad wäre also unerreichbar: Re-Enchant nach `inventoryTick` verlegen (Entscheidung offen).

### RatSword

`ratsword` – „Rat Sword“

- **Rolle:** beschwört Ratten; Basis `ItemSword(toolEMERALD)` (OreSpawnMain.java:1327).
- **Werte:** Haltbarkeit 1300 (RatSword.java:20), Stack 1, Angriffs-Modifier 10, Blockdauer 3000 (:59-61). `weaponDamage = 15` ungenutzt (:18).
- **Verhalten `hitEntity`** (lebt, :28-41): Server, `num = 1 + rand(6)` → 1..6 (:31).
  - Je Ratte „Rat“ bei Ziel ±0.5 x/z (Differenz zweier `nextFloat`), y + `nextFloat` + 0.01 (:33).
  - `setOwner(attacker)` (:34-36); danach `damageItem(1)` (:39).
  - `spawnCreature` spielt den `playLivingSound` (:43-57).
- **Portierung:** `hurtEnemy`, serverseitig; Besitzer-Logik steckt in `Rat` (anderer Stapel).

### RubyAxe

`rubyaxe` – „Ruby Axe“

- **Rolle:** Axt; Basis `ItemAxe(toolRUBY)`. `setHarvestLevel("axe", ruby_stats.harvestlevel = 5)` (OreSpawnMain.java:1335).
- **Werte:** Material RUBY 5/1500/11/16/85 (OreSpawnMain.java:1181); Haltbarkeit 1500 (RubyAxe.java:17), Stack 1, Tab Tools; Angriffs-Modifier 3.0 + 16 = **19**; Effizienz 11.
- `getDamageVsEntity` (→ 12) und `getMaterialName` sind **tot** (jar).
- **Portierung:**
  - `SimpleTier` RUBY (uses 1500, speed 11, bonus 16, enchant 85); `AxeItem.createAttributes(tier, 3.0f, speed)`.
  - Harvest-Level 5 liegt über Diamant und entspricht einem Incorrect-Blocks-Tag ohne Einträge (z. B. `BlockTags.INCORRECT_FOR_NETHERITE_TOOL`).
  - Reparaturzutat im Original nicht gesetzt: offen.

### RubyHoe

`rubyhoe` – „Ruby Hoe“

- Basis `ItemHoe(toolRUBY)`, kein Harvest-Level (OreSpawnMain.java:1334). Haltbarkeit 1500, Stack 1 (RubyHoe.java:13-14).
- Kein Angriffs-Modifier (Querschnitt ada.<init>); `getDamageVsEntity` (→ 5) tot. Pflügen = Vanilla-`ItemHoe`.
- **Portierung:** `HoeItem`, `createAttributes(tier, -bonus, speed)`, damit der Modifier wie in 1.7.10 bei 0 liegt.

### RubyPickaxe

`rubypickaxe` – „Ruby Pickaxe“

- Basis `ItemPickaxe(toolRUBY)`, harvest pickaxe 5 (OreSpawnMain.java:1332). Haltbarkeit 1500 (RubyPickaxe.java:17). Angriffs-Modifier 2.0 + 16 = **18**.
- Beide `getDamageVsEntity`-Varianten (:21-27) tot.
- **Portierung:** `PickaxeItem`, `createAttributes(tier, 2.0f, speed)`.

### RubyShovel

`rubyshovel` – „Ruby Shovel“

- Basis `ItemSpade(toolRUBY)`, harvest shovel 5 (OreSpawnMain.java:1333). Haltbarkeit 1500 (RubyShovel.java:14). Angriffs-Modifier 1.0 + 16 = **17**. `getDamageVsEntity` tot.
- **Portierung:** `ShovelItem`, `createAttributes(tier, 1.0f, speed)`.

### RubySword

`rubysword` – „Ruby Sword“

- Basis `ItemSword(toolRUBY)` (OreSpawnMain.java:1331). Haltbarkeit 1500 (RubySword.java:19), Stack 1, Tab Combat. Angriffs-Modifier 4 + 16 = **20**, Blockdauer 4000 (:36-38).
- `getDamageVsEntity` (→ 18) und `hitEntity(EntityLiving…)` tot.
- **Abgleich:** 03-items.md:77 nennt 20. Stimmt.
- **Portierung:** `SwordItem`, `createAttributes(tier, 4, speed)`.

### SkateBow

`skatebow` – „Skate String Bow“

- **Rolle:** Bogen für `IrukandjiArrow`; Basis `Item`.
- **Werte:** Stack 1, Haltbarkeit 300, Tab Combat (SkateBow.java:15-17); Benutzungsdauer 9000 (:64-66); Animation `bow` (:68-70); Enchantability 50 (`func_77619_b`, :77-79).
- **Verhalten:**
  1. `onItemRightClick` → `setItemInUse` (:72-75), ohne Munitionsprüfung beim Spannen.
  2. `onPlayerStoppedUsing` (`func_77615_a`, :26-58): `var6 = 9000 − Restdauer` (:27). `flag = Creative || Infinity > 0` (:28). Weiter nur mit `flag` oder `MyIrukandjiArrow` im Inventar (:29).
  3. `f = var6/20`, `f = (f² + 2f)/3` (:30-31). Bei `f < 0.1` Abbruch (:32-34), Kappung auf 1.75 (:35-37).
  4. `new IrukandjiArrow(world, player, f)` (:38); kritisch bei `rand(20) == 1`, also 5 % (:39-41).
  5. Punch → `setKnockbackStrength(lvl)` (:42-45). Flame → `setFire(100)` (:46-48).
  6. `damageItem(1)` (:49). Sound `random.bow`, 1.0, `1/(rand*0.4+1.2)+0.5` (:50).
  7. Ohne `flag` einen Pfeil verbrauchen (:51-53). Server-Spawn (:54-56).
- `onFoodEaten` ist tot. Forges `ArrowNockEvent`/`ArrowLooseEvent` werden nicht gefeuert.
- **Portierung:** eigenes `BowItem`-artiges Item mit `releaseUsing`, `getUseDuration(stack, entity) = 9000`, `UseAnim.BOW`. Power wird im Original **nicht** ausgewertet; ob der Pfeil das intern tut, klärt `IrukandjiArrow` (anderer Stapel).

### Slice

Registry-ID: **keine**. Die Klasse wird nirgends instanziert (grep `new Slice(`: kein Treffer). `slicesmall` („Slice“) wird als `new Bertha(BaseItemID+314, toolBERTHA)` erzeugt (OreSpawnMain.java:1314; manifest `class = Bertha`); registriert als `OreSpawn_Slice` (:1861), Rezept Big Bertha + Eisenbarren formlos (:2948), Item-Renderer `RenderSlice` (renderers_dump.txt:136).

Inhalt der toten Klasse, nur als Referenz:
- `ItemSword`, Haltbarkeit 2600 (Slice.java:17).
- `onCreated`: Sharpness V, Bane of Arthropods I (:21-24); Neu-Hinzufügen per `onUsingTick`/`onUpdate` (:26-36).
- `onLeftClickEntity` bricht Angriffe auf Spieler, Girlfriend und Boyfriend ab, unabhängig von der Config (:38-40).
- `onEntitySwing` spawnt `BerthaHit` 2.0 Blöcke voraus bei y+1.55, Bewegung ×2, `damageItem(1)` (:42-59).
- Blockdauer 9000 (:70-72).

- **Portierung:** nicht portieren; `slicesmall` gehört zu `Bertha`. **Abgleich:** 03-items.md:62 („identisch zu Big Bertha“) passt zur Registrierung.

### StepAccross

`step_accross` – „Insta-Bridge“

- **Rolle:** Sofort-Brücke; Basis `Item`. Stack 16, Tab Tools (StepAccross.java:16-17).
- **Verhalten `onItemUse`** (:20-102):
  1. Richtung aus `rotationYawHead`: `f = ((yaw + 22.5) % 360) / 45`, als `int` (:27-31). Sektor → (dx, dz): 0 (0,+1), 1 (−1,+1), 2 (−1,0), 3 (−1,−1), 4 (0,−1), 5 (+1,−1), 6 (+1,0), 7 (+1,+1) (:31-72).
  2. Kein Sektor → `false` (:73-75). **Falle:** Javas `%` behält das Vorzeichen, und der Spieler-Gierwinkel ist in 1.7.10 nicht normiert; bei negativem Gierwinkel wirkt das Item nicht.
  3. Sound `random.explode` 1.0/1.5 auf beiden Seiten (:76). Client: 6× `largesmoke`, `largeexplode`, `reddust`, dann `true` (:77-84).
  4. Server, `k = 1..32` (`length = 33`, :23, :85): Ziel `(x+k·dx, y−1, z+k·dz)` mit `y` = angeklickter Block + 1 (:24-26). Ist dort kein Luftblock, Abbruch (:86-89). Sonst Bruchstein, Flag 2 (:90). Bei `(k−1) % 8 == 0` und Luft auf Höhe `y` → `ExtremeTorch` (:91-96), also bei k = 1, 9, 17, 25.
  5. Außer im Creative-Modus −1, **auch wenn nichts gebaut wurde** (:98-100).
- **Portierung:** Richtung über `Mth.wrapDegrees` normieren (bewusste Abweichung, sonst 1:1 kaputt). Fackel als stehende `extremetorch`. `setBlock(pos, state, 2)`.

### StepDown

`step_down` – „Stairs going Down“

- Identisch zu `StepAccross`, außer (StepDown.java:85-97): Bruchstein bei `(x+k·dx, y−k−1, z+k·dz)`, Fackel darüber bei `y−k`. Client-Partikel wie Accross (:77-84).

### StepUp

`step_up` – „Stairs going Up“

- Identisch zu `StepAccross`, außer (StepUp.java:85-97): Bruchstein bei `(x+k·dx, y+k−1, z+k·dz)`, Fackel bei `y+k`. Client-Partikel mit y + 1.0 (:79-81).
- **Abgleich:** 03-items.md:243 („bis zu 32 Stufen, Fackel alle 8“). Stimmt.

### UltimateAxe

`ultimateaxe` – „The Ultimate Axe“

- **Rolle:** Axt; Basis `ItemAxe(toolULTIMATE)`, harvest axe 10 (OreSpawnMain.java:1311).
- **Werte:** ULTIMATE 10/3000/15/36/100 (OreSpawnMain.java:1174); Haltbarkeit 3000 (UltimateAxe.java:21), Stack 1, Tab Tools. Angriffs-Modifier 3 + 36 = **39**, Effizienz 15.
- **Verhalten:**
  - `onCreated` → Efficiency V (:25-27); `onUsingTick` und jeden Tick `onUpdate` fügen es neu hinzu, wenn es fehlt (:29-38).
  - `onLeftClickEntity` (:40-53): Bei `UltimateSwordPvp == 0` und Ziel Spieler, Girlfriend, Boyfriend oder gezähmter `EntityTameable` → `true`, Angriff abgebrochen. Sonst normal.
  - `getDamageVsEntity` (Spieler/GF → 1, sonst 15) und `getMaterialName` tot.
- **Config:** `OreSpawnTWEAKS.UltimateSwordPvp` Default 0 (manifest; OreSpawnMain.java:1150).
- **Portierung:** `onLeftClickEntity` bleibt als NeoForge-Hook; `TamableAnimal#isTame`.

### UltimateBow

`ultimatebow` – „The Ultimate Bow“

- **Rolle:** Bogen ohne Munition und ohne Spannzeit; Basis `Item`.
- **Werte:** Stack 1, Haltbarkeit 1000, Tab Combat (UltimateBow.java:16-18); Dauer 9000, `bow`, Enchantability 50 (:62-77).
- **Verhalten:**
  - `onCreated`: Power V, Flame III, Punch II, Infinity I (:21-26). Neu hinzugefügt nur in `onUsingTick`, wenn Infinity fehlt (:28-36).
  - `onPlayerStoppedUsing` (:38-56), unabhängig von der Spannzeit:
    1. `new UltimateArrow(world, player, 3.0f)` (:39); kritisch bei `rand(4) == 1`, also 25 % (:40-42).
    2. Punch → Knockback (:43-46); Flame → `setFire(100)` (:47-49).
    3. `damageItem(1)` (:50); Sound wie SkateBow (:51).
    4. `canBePickedUp = 2`, nur im Creative-Modus aufhebbar (:52). Server-Spawn (:53-55).
- **Config:** `OreSpawnWEAPONS.UltimateBowDamage` Default 10, geklemmt auf 2..20 (OreSpawnMain.java:1190-1200); gelesen in `UltimateArrow`.
- **Portierung:** `releaseUsing`; `AbstractArrow.Pickup.CREATIVE_ONLY`; Enchant-Neuanlage in `inventoryTick`.

### UltimateFishingRod

`ultimatefishingrod` – „The Ultimate Fishing Rod“

- **Rolle:** Angel (auch in Lava, Logik im Haken); Basis `Item`.
- **Werte:** Haltbarkeit 3000, Stack 1, Tab Tools (UltimateFishingRod.java:15-17); `isFull3D` und `shouldRotateAroundWhenRendering` = true (:20-22, :35-37).
- **Verhalten:**
  - `onCreated` → Unbreaking II (:24-26); `onUsingTick` fügt es neu hinzu (:28-33). Eine Angel hat keine Use-Aktion, der Pfad läuft also praktisch nie.
  - `onItemRightClick` (:39-53):
    - Haken aktiv: `damageItem(fishEntity.func_146034_e())`. Laut MCP ist das `handleHookRetraction`, es liefert den Haltbarkeitsverbrauch (:40-43).
    - Sonst: Sound `random.bow`, 0.5, `0.4/(rand*0.4+0.8)` (:46), Server spawnt `new UltimateFishHook(world, player)` (:47-49).
    - In beiden Fällen Schwung.
- **Portierung:**
  - Unterklasse von `FishingRodItem` bzw. eigenes `use`; `FishingHook#retrieve(stack)` liefert den Schaden.
  - Der Haken muss von `FishingHook` erben, weil `Player.fishing` so typisiert ist (Haken: anderer Stapel).
  - Enchant einmalig in `onCraftedBy`.

### UltimateHoe

`ultimatehoe` – „The Ultimate Hoe“

- **Rolle:** 3×3×3-Hacke; Basis `ItemHoe(toolULTIMATE)` (OreSpawnMain.java:1310), kein Harvest-Level.
- **Werte:** Haltbarkeit 3000, Stack 1 (UltimateHoe.java:18-19). Efficiency II per `onCreated`, Neuanlage per `onUsingTick`/`onUpdate` (:23-36).
- **Verhalten `onItemUse`** (`func_77648_a`, :38-65):
  1. `canPlayerEdit` (:39-41).
  2. Angeklickt Gras oder Erde, Seite ≠ unten, Luft darüber (:42-46).
  3. Break-Sound des Farmland-Soundtyps an der Blockmitte, Lautstärke `(vol+1)/2`, Pitch `pitch·0.8` (:47-48). Client `true` (:49-51).
  4. Server: jedes Gras oder jede Erde mit Luft darüber im Würfel −1..1 → Farmland meta **7** (feucht), Flag 2 (:52-62).
  5. `damageItem(1)` (:63).
- **Portierung:** `useOn`, `FarmBlock.MOISTURE = 7`; das Original feuert kein `UseHoeEvent`. Angriffs-Modifier 0 (siehe RubyHoe).

### UltimatePickaxe

`ultimatepickaxe` – „The Ultimate Pickaxe“

- **Rolle:** Spitzhacke mit Bonus-Drops; Basis `ItemPickaxe(toolULTIMATE)`, harvest pickaxe 10 (OreSpawnMain.java:1308).
- **Werte:** Haltbarkeit 3000 (UltimatePickaxe.java:24), Angriffs-Modifier 2 + 36 = **38**. `onCreated` Efficiency V + Fortune V; beide werden neu hinzugefügt, sobald Efficiency fehlt (:28-43).
- **Verhalten:**
  - `canHarvestBlock(Block)` → `true` (:45-47) ist **tot** (jar; SRG wäre `func_150897_b`). Es gelten die Vanilla-Pickaxe-Regeln plus Forge-Level 10.
  - `getDamageVsEntity` tot. `onLeftClickEntity`: PvP-Schutz wie UltimateAxe (:62-75).
  - `onBlockDestroyed` (`func_150894_a`, :87-121):
    1. Härte ≠ 0 → `damageItem(1)` (:88-90).
    2. Server: Eisenerz und `rand(2) != 0` → `1 + rand(2)` Eisenbarren (:92-94); Golderz ebenso mit Goldbarren (:95-97).
    3. Stein und `rand(100) == 2` (1 %): `i = rand(10)` → 0 Diamant, 1 Smaragd, 2 `amethyst`, 3 `ruby`, 4 `uranium_nugget`, 5 `titanium_nugget`, 6-9 nichts (:98-118).
    4. Die Bonus-Items fallen zusätzlich zum normalen Drop an der Blockecke `(x, y, z)` (:77-85).
- **Portierung:** `mineBlock`; Bonus per `Block.popResource` nur auf dem Server. Alternativ Global Loot Modifier; wegen der Identitätsprüfung auf das Werkzeug ist `mineBlock` näher am Original.

### UltimateShovel

`ultimateshovel` – „The Ultimate Shovel“

- Basis `ItemSpade(toolULTIMATE)`, harvest shovel 10 (OreSpawnMain.java:1309). Haltbarkeit 3000 (UltimateShovel.java:18). Efficiency V mit Neuanlage (:22-35). PvP-Schutz (:37-50). Angriffs-Modifier 1 + 36 = **37**. `getDamageVsEntity` tot.

### UltimateSword

| Registry-ID | Lang-Name | Material (Default-Stats harvest/uses/eff/dmg/ench) | Konstruktor | Angriffs-Modifier |
|---|---|---|---|---|
| ultimatesword | The Ultimate Sword | ULTIMATE 10/3000/15/36/100 (OreSpawnMain.java:1174) | :1307 | 40 |
| battleaxesmall | Battle Axe | BATTLE 3/1500/15/46/75 (:1186) | :1317 | 50 |
| chainsawsmall | Chainsaw | CHAINSAW 3/1500/10/56/75 (:1187) | :1318 | 60 |
| queenbattleaxesmall | Queen Scale Battle Axe | QUEENBATTLE 3/2200/15/662/100 (:1188) | :1319 | 666 |

- **Rolle:** eine Klasse für vier Nahkampfwaffen; Varianten per Identitätsvergleich mit `MyChainsaw`/`MyBattleAxe`; Basis `ItemSword`.
- **Werte:** alle Haltbarkeit **3000** (UltimateSword.java:29, überschreibt `maxUses`), Stack 1, Tab Combat; Blockdauer 9000 (:148-150). Angriffs-Modifier = 4 + Material-Schaden (Querschnitt). Item-Renderer `RenderBattleAxe`, `RenderChainsaw`, `RenderQueenBattleAxe` (renderers_dump.txt:140-142).
- **Verzauberungen** (`onCreated` :33-50; Neuanlage wenn Looting fehlt: `onUsingTick` :60-80, `onUpdate` :103-118). M = `UltimateSwordEnchantmentLevel`:
  - Chainsaw: keine.
  - Ultimate Sword und Queen Battle Axe: Sharpness M, Smite M, Bane of Arthropods M, Knockback 1+M/2, Looting 1+M/2, Unbreaking 1+M/2, Fire Aspect 1+M/3 (Ganzzahl-Division).
  - Battle Axe: Looting 1+M/2, Unbreaking 1+M/2.
  - Default M = 5 → 5/5/5/3/3/3/2.
- **Chainsaw-Verhalten:**
  - `onEntitySwing` (:52-58): bei `swingtimer == 0` → `playSound("orespawn:chainsawshort", 1.0, rand*0.2+0.9)`, `swingtimer = 50`; gibt `false` zurück.
  - `onUpdate` (:83-101): `swingtimer−−`. Client mit `swingtimer > 0`: Partikel an Position + `(cos, sin)(yaw+135°)`: `flame` bei `rand(8)==0`, `smoke` bei `rand(2)==0`, `fireworksSpark` bei `rand(10)==0`.
  - `onLeftClickEntity` (:130-146): zuerst PvP-Schutz wie UltimateAxe (`true` = Abbruch). Bei Chainsaw danach `findSomethingToHit(player)`; Rückgabe `false`, der normale Treffer folgt.
  - `findSomethingToHit` (:157-169): alle `EntityLivingBase` in der Spieler-Hitbox + 5 → `attackEntityFrom(causePlayerDamage, chainsaw_stats.damage = 56)` für jedes geeignete Ziel, **inklusive des angeklickten**.
  - `isSuitableTarget` (:171-193): nicht der Spieler, lebendig, PvP-Schutz, dazu `MyCanSee`.
  - `MyCanSee` (:195-248): Schrittweise Abtastung von Augenhöhe `posY+1.4` zur Zielmitte; jeder Nicht-Luftblock → `false`.
  - `onBlockDestroyed` (:265-286): Server, Quader i −5..5, j −5..10, k −5..5 (11×16×11) um den abgebauten Block. Mit `leaf == true` nur `isLeaves`-Blöcke, sonst `canCrush`-Blöcke. Je Block 1 Item (meta 0) streuen (`dropItemRand`: x±rand(5), y+1+rand(5), z±rand(5); :288-296) und auf Luft setzen. Danach `super.onBlockDestroyed` (Vanilla-Haltbarkeitsabzug, Betrag nicht disassembliert: offen).
  - `canCrush` für Chainsaw (:254-259): web, log, leaves, planks, sapling, tallgrass, cactus, crystalplanks, leaves_apple, skytreelog, duplicatortreelog, leaves_experience, leaves_scary, leaves_cherry, leaves_peach, crystaltreeleaves 1-3, crystaltreelog. Nur `Blocks.log`/`Blocks.leaves`, nicht `log2`/`leaves2`. Andere Varianten: nur web.
  - **Tot im Jar:** `getStrVsBlock(ItemStack, Block)` (:298-309, sollte `leaf` setzen und Effizienz 10 liefern) und `canHarvestBlock(Block)` (:250-252). Folgen: `leaf` bleibt immer `false`; die Kettensäge gräbt mit Vanilla-Schwerttempo (1.0/1.5/15.0); nur Spinnennetz gilt als erntbar. `hitEntity(EntityLiving…)` und `getMaterialName` ebenfalls tot.
- **Falle:** `swingtimer` und `leaf` sind Felder des Item-Singletons.
- **Sounds:** `chainsawshort` (manifest sounds, referenced_by UltimateSword).
- **Config:** `UltimateSwordEnchantmentLevel` 5 (1..10), `UltimateSwordPvp` 0, `Ultimate_*`, `BattleAxe_*`, `Chainsaw_*`, `QueenBattleAxe_*` (manifest).
- **Abgleich:** 03-items.md:66 schreibt der Kettensäge Effizienz 10 und einen Blätter-Modus zu; beide hängen am toten `getStrVsBlock`. Im ausgelieferten Jar greifen sie nicht.
- **Portierung:**
  - Vier `SwordItem`-Instanzen mit Varianten-Enum statt Identitätsvergleich; je ein `SimpleTier` mit `uses = 3000`.
  - `swingtimer` als Stack-DataComponent oder `player.getCooldowns()`.
  - AoE über `level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5))`, `damageSources().playerAttack(player)`, Sichtprüfung `player.hasLineOfSight`.
  - Baumfällen in `mineBlock`; Blocklisten als Tag (`orespawn:chainsaw_crushable`).
  - Design-Entscheidung, bewusst dokumentieren: 1:1 (ohne Effizienz- und Blättermodus) oder Autorenabsicht.
  - Item-Renderer client-seitig (Custom-Modell oder BEWLR). Queen Battle Axe 666 liegt unter der Attributgrenze von `ATTACK_DAMAGE`.

### ZooCage

| Registry-ID | Lang-Name | `cage_size` | Halbbreite `w = size/2+1` | Außenmaß | Höhe gesamt |
|---|---|---|---|---|---|
| zoo2 | Extra Small Zoo Cage | 3 | 2 | 5×5 | 4 |
| zoo4 | Small Zoo Cage | 5 | 3 | 7×7 | 5 |
| zoo6 | Medium Zoo Cage | 9 | 5 | 11×11 | 7 |
| zoo8 | Large Zoo Cage | 13 | 7 | 15×15 | 9 |
| zoo10 | Extra Large Zoo Cage | 17 | 9 | 19×19 | 11 |

`cage_size` aus OreSpawnMain.java:1593-1597; Formel ZooCage.java:29; Außenmaß `2w+1`; Höhe `w+2` (k = 0..w+1).

- **Rolle:** Sofort-Käfig um den Spieler; Basis `Item`. Stack 16, Tab Decorations (ZooCage.java:18-19).
- **Verhalten `onItemUse`** (:23-65):
  1. `dirx = −1` bei negativer angeklickter x-Koordinate, `dirz` analog (:30-35). Zentrum `x = (int)(posX + 0.99·dirx)`, `y = (int)posY − 1`, `z` analog (:36-38).
  2. Sound `random.explode` 1.0/1.5 (:39); Client `true` (:40-42).
  3. Server, i −w..w, j −w..w, k 0..w+1: `k == w+1` Quarzblock (Decke) (:46-47), `k == 0` Quarzblock (Boden, also der Block unter dem Spieler) (:49-50), Rand (|i| = w oder |j| = w) Glas (:52-53), sonst **Luft** (:55-56).
  4. `setBlock` mit Default-Flag, überschreibt **alles** (auch Bedrock, Truhen).
  5. Außer im Creative-Modus −1 (:61-63).
- **Abgleich:** 03-items.md:238 nennt 5/7/11/15/19. Stimmt.
- **Portierung:** `useOn`; Zentrum per `BlockPos.containing(player.position())` (erledigt die Floor-Korrektur); `setBlock(pos, state, 3)`. Bewusst entscheiden, ob unzerstörbare Blöcke und BlockEntities geschützt werden (1:1 schützt nichts).

---

## Blöcke

### AntBlock

| Registry-ID | Lang-Name | Feld | Spawnt | Config (Default) |
|---|---|---|---|---|
| antblock | Ant Nest | MyAntBlock | „Ant“ (`ant`) | `BlackAntEnable` 1 |
| redantblock | Red Ant Nest | MyRedAntBlock | „Red Ant“ (`red_ant`) | `RedAntEnable` 1 |
| termiteblock | Termite Nest | TermiteBlock | „Termite“ (`termite`) | `TermiteEnable` 1 |
| rainbowantblock | Rainbow Ant Nest | MyRainbowAntBlock | „Rainbow Ant“ (`rainbow_ant`) | `RainbowedAntEnable` 1 (Key ≠ Feldname) |
| unstableantblock | Unstable Ant Nest | MyUnstableAntBlock | „Unstable Ant“ (`unstable_ant`) | `UnstableAntEnable` 1 |

Konstruktoren OreSpawnMain.java:5943-5948, Config OreSpawnMain.java:6049-6053 (Kategorie OreSpawnMOBS, manifest).

- **Rolle:** Ameisennest, Dauer-Spawner; Basis `BlockGrass`.
- **Werte:** TickRandomly, Tab Block (AntBlock.java:20-21). Keine Härte gesetzt, weder in der Klasse noch bei der Registrierung → 0.0 (bricht sofort). Material Gras (`alh.<init>`).
- **Texturen:** `antnest_top` (Seite 1), `antnest_bottom` (Seite 0), `antnest_side` (:127-132).
- **Farbe:** Item-Farbe = Grasfarbe bei (0.5, 1.0) (:99-109). Welt-Farbe = Mittel der Biom-Grasfarben im 3×3-Umkreis (:111-125).
- **Verhalten `updateTick`** (:46-82, Server):
  1. Bei Regen nichts (:49-51). Nur mit Luft darüber (:52-53).
  2. `howmany = OreSpawnRand.nextInt(6) + 2` → 2..7 (:54). Jede Iteration spawnt je nach Block-Identität die Art aus der Tabelle, sofern der Enable-Key ≠ 0 (:55-79). Position +0.5, +1.01, +0.5 (:88-97).
  3. **Keine Obergrenze.** Jeder Zufallstick erzeugt 2..7 Tiere.
- **Drop:** sich selbst (:84-86). Geerbt von `BlockGrass`: `IGrowable`, Knochenmehl lässt also Gras und Blumen wachsen (nicht überschrieben; nicht disassembliert).
- **Portierung:**
  - `Block` mit `randomTick` (`isRandomlyTicking` = true); Spawn per `EntityType.spawn`.
  - Tint per `BlockColor` auf allen Seiten (Modell mit `tintindex` auf allen Faces).
  - Härte 0 bewusst übernehmen. Knochenmehl-Verhalten klären (offen).
  - Ohne Mob-Cap bleibt das 1:1 ein Performance-Risiko: im Pack-README vermerken.

### BlockAppleLeaves

`leaves_apple` – „Apple Tree Leaves“

- **Rolle:** Laub mit Obst-Drops; Basis `BlockLeaves`.
- **Werte:** Härte 0.2, LightOpacity 1, Sound Gras (OreSpawnMain.java:1605); dazu die `BlockLeaves`-Defaults (Querschnitt). Laut Umrechnung: Resistenz 0.2.
- **Drops** `dropBlockAsItemWithChance` (:25-40, Server, unabhängige Würfe, kein `super` → kein Setzling):
  - `rand(25) == 1` → Apfel (4 %)
  - `rand(500) == 2` → Goldener Apfel meta 0 (0,2 %)
  - `rand(1000) == 3` → Goldener Apfel meta 1, verzaubert (0,1 %)
  - `rand(10000) == 4` → `magicapple` (0,01 %)
  - `quantityDropped` 1 (:42-44).
- **Verhalten `updateTick`** (:46-79):
  1. `r = 2`, `chance = 20`; in `DimensionID4` („Dimension-Islands“, manifest; `BaseDimensionID+3`, OreSpawnMain.java:1269) `r = 1`, `chance = 100` (:50-53).
  2. Sucht einen Block mit `canSustainLeaves` in dx, dz ∈ [−r, r], dy ∈ [−r, 0] mit Manhattan-Distanz ≤ 3 (:55-61).
  3. Gefunden: Luft darunter und `rand(chance) == 3` → Obst-Wurf unterhalb (:62-65). In DimensionID4 bei `worldTime % 24000 > 12000` → Umwandlung in `leaves_scary` (:66-70).
  4. Nicht gefunden → Drop-Wurf plus Luft (:77, 81-84).
- **Grafik:** `isOpaqueCube = FastGraphicsLeaves != 0` (:86-88); bei schneller Grafik Icon `generic_solid` (:103-108). `FastGraphicsLeaves` ist **kein Config-Key**: der Client setzt ihn jeden Frame aus `gameSettings.fancyGraphics` (GirlfriendOverlayGui.java:46-51). Auf dem Dedicated Server bleibt er 0, in Einzelspieler teilt ihn der integrierte Server.
- **Portierung:**
  - `LeavesBlock` ist ungeeignet (Distanz-Decay über `#logs`): eigener Block mit `randomTick`.
  - Drops über eine Loot-Table mit `random_chance`-Einträgen, der Obst-Wurf unten per Code.
  - Die Fast/Fancy-Weiche entfällt (RenderType `cutout_mipped`).

### BlockButterflyPlant

`butterfly_plant` (kein Lang-Name im manifest)

- **Rolle:** Insektenpflanze; Basis `BlockCrops`. Gepflanzt von `butterfly_seed` (OreSpawnMain.java:1551).
- **Werte:** Vanilla-Crop-Wachstum 0..7 (Querschnitt). Icons `butterfly_0..3`, Index: meta 0-1 → 0, 2-3 → 1, 4-6 → 2, 7 → 3 (:54-62).
- **Verhalten `updateTick`** (:22-40):
  1. `super.updateTick` (Wachstum) (:23). Server, kein Regen (:27-29).
  2. `rate = 7 − (meta & 7)`; bei `rate > 1` Abbruch, falls `OreSpawnRand.nextInt(rate) != 0` (:30-35). Spawn-Chance also 1/(7−meta), ab meta 6 immer.
  3. Luft darüber, **Tag** und `ButterflyEnable != 0` → 1 „Butterfly“ (:36-39).
- **Drops:** `quantityDropped` `1 + rand(5)` (:64-66); `getSeed` → `butterfly_seed` (:68-70); `getCrop` → `null` (:72-74).
  - Unreif: 1..5 Samen.
  - Reif (meta 7): kein Hauptdrop (Item `null`), aber 3 + Fortune Würfe `rand(15) ≤ 7` auf Samen.
- **Config:** `OreSpawnMOBS.ButterflyEnable` 1 (OreSpawnMain.java:6115).
- **Portierung:** `CropBlock` mit `getBaseSeedId`; Spawn in `randomTick` nach `super.randomTick`; Loot-Table mit Samen-Bonus nach Vanilla-Muster. Vier Texturen auf 8 Alterswerte abbilden.

### BlockCorn

`corn_0`, `corn_1`, `corn_2`, `corn_3` – alle „Corn Plant“ (OreSpawnMain.java:1567-1570)

- **Rolle:** hoher Mais in vier Blockstufen; Basis `BlockReed`. Gepflanzt von `corn_seed` („Corn“, ItemCornCob 6/0.75, OreSpawnMain.java:1571).
- **Werte:** Hitbox x/z 0.125..0.875, Höhe 1 (BlockCorn.java:17-18); TickRandomly; keine Härte → 0.
- **Boden** (:22-25): unter dem Block eine Maisstufe, Gras, Erde oder Farmland. Nachbarprüfung von Vanilla `BlockReed` (bricht, wenn der Boden fehlt).
- **Verhalten `updateTick`** (:27-81), nur Stufe 1 und 2 (:33-35):
  1. `myMaxHeight = meta >> 8`, `age = meta & 0xFF` (:36-38); ist sie 0 → `4 + rand(4)` = 4..7 (:39-41).
  2. Nur mit Luft darüber (:42-43): `Height` = 1 + zusammenhängende Maisblöcke darunter (bis 9). Liegt Stufe 3 oder 4 darunter → `myMaxHeight = Height` (:44-56).
  3. `age ≥ 6 − myMaxHeight/3` (:57):
     - `Height < max` → darüber `corn_0` setzen, aktuell → `corn_1` (:58-61).
     - Sonst für i = 1..max−2 darunter: Stufe 2 → 3, 3 → 4 (:63-71); aktuellen Block zurücksetzen (:72-73).
  4. Andernfalls `age + 1` (:76-79).
- **Falle (wichtig):** Metadaten sind in 1.7.10 4 Bit. `max << 8` landet nie im Welt-Speicher, deshalb wird `myMaxHeight` bei jedem Tick neu gewürfelt und die Reife-Schwelle springt zwischen 5 und 4. `myMaxHeight` ist zudem ein Feld des Block-Singletons.
- **Drops:** Item `corn_seed` (:83-85); Menge nur bei `corn_3`: `1 + rand(2)`, sonst 0 (:91-96). `getItem(int, Random, int)` (:87-89) ist tot → **Pick-Block liefert Zuckerrohr** (Vanilla `BlockReed`).
- **Portierung:** ein Block mit `IntegerProperty STAGE` (0..3), `AGE` (0..15), `MAX_HEIGHT` (4..7) statt vier Registry-IDs, oder vier IDs plus `AGE` für Welt-Kompatibilität (offen). Entscheidung dokumentieren: `MAX_HEIGHT` persistent (Autorenabsicht) oder 1:1 neu würfeln. `getCloneItemStack` → `corn_seed`.

### BlockCrystal

| Registry-ID | Lang-Name |
|---|---|
| crystalpink_block | Pink Tourmaline Block (OreSpawnMain.java:1284) |
| tigerseye_block | Tiger's Eye Block (OreSpawnMain.java:1286) |

- **Rolle:** leuchtender Speicherblock; Basis `Block(Material.rock)`.
- **Werte:** Härte 4.0 (BlockCrystal.java:13), `setResistance(4.0)` (:14) → Resistenzfeld 12 (die Reihenfolge Härte → Resistenz überschreibt die 20 aus `setHardness`), 1.21.1-Resistenz 2.4. Licht 0.4 → Lichtwert **6** (:16). Tab Block.
- `isOpaqueCube` und `renderAsNormalBlock` = false (:19-25). Drop: sich selbst. Harvest-Level nicht gesetzt (Vanilla-Materialregel Stein).
- **Portierung:** `strength(4.0f, 2.4f)`, `lightLevel(s -> 6)`, `noOcclusion()`, `requiresCorrectToolForDrops()` plus `mineable/pickaxe` (Materialregel entspricht „Werkzeug nötig“, das Level ist offen).

### BlockCrystalLeaves

| Registry-ID | Lang-Name | Härte | Setzling-Drop |
|---|---|---|---|
| crystaltreeleaves | Crystal Tree Leaves | 0.2 (OreSpawnMain.java:1627) | crystalsapling |
| crystaltreeleaves2 | Crystal Tree Leaves | 0.25 (:1629) | crystalsapling2 |
| crystaltreeleaves3 | Crystal Tree Leaves | 0.25 (:1630) | crystalsapling3 |

- **Rolle:** Laub der Kristallbäume; Basis `BlockLeaves`. LightOpacity 1, SoundType „grass“ 1.0/1.0; TickRandomly, Tab Decorations (BlockCrystalLeaves.java:16-17).
- **Drops** (:24-41, Server): `rand(100) == 1` → `crystalapple` (1 %); `rand(50) == 1` → Setzling der eigenen Art (2 %). `quantityDropped` 1.
- **Verhalten `updateTick`** (:47-75): wie BlockAppleLeaves, ohne Umwandlung in gruseliges Laub.
- **Grafik:** Farbe fest `14540253` = 0xDDDDDD (:104-116). Bei Fancy-Grafik `getRenderType = 1` (Kreuzmodell wie eine Pflanze) (:90-95), sonst Standardwürfel; `isOpaqueCube`/`renderAsNormalBlock` an `FastGraphicsLeaves` gekoppelt (:82-88).
- **Portierung:** wie BlockAppleLeaves. Modell: Fancy-Kreuz oder Würfel ist eine Design-Entscheidung (offen); Tint 0xDDDDDD per `BlockColor` oder direkt in die Textur backen.

### BlockCrystalPlant

| Registry-ID | Lang-Name | Baum |
|---|---|---|
| crystalsapling | Red Crystal Tree Sapling | `TallCrystalTree`, Laub `crystaltreeleaves` |
| crystalsapling2 | Yellow Crystal Tree Sapling | `ScragglyCrystalTreeWithBranches`, Laub `crystaltreeleaves2` |
| crystalsapling3 | Blue Crystal Tree Sapling | `TallCrystalTreeBlue`, Laub `crystaltreeleaves3` |

- **Rolle:** Setzling; Basis `BlockReed`. Hitbox wie Mais, TickRandomly, Tab Decorations (BlockCrystalPlant.java:15-18).
- **Boden:** Gras, Erde, Farmland, `crystalgrass` (:21-24).
- **Client:** `rand(30) == 1` → 10× `happyVillager` (:26-33).
- **Verhalten `updateTick`** (:35-52): Server, `rand(5) == 1` → Setzling auf Luft, dann Baum nach Identität (:42-51). Keine Licht- und keine Platzprüfung.
- **`TallCrystalTree`** (:80-139):
  1. Stamm `i = 10 + rand(12)` → 10..21 Blöcke (:81). Abbruch **ohne Rückbau**, wenn ab k ≥ 1 ein Block weder Luft noch Kristallholz noch eigenes Laub ist (:84-87).
  2. Oberer Stamm bis `j = i + rand(18)` (:82). Bei Hindernis Stopp (:94-96). Bei `k % 4 == 0` ein 3×3-Ring mit je 50 % Laub auf Luft (:98-109).
  3. Krone: Ebene +1 mit 3×3 zu je 50 % Holz (:111-121), auf derselben Ebene 7×7 Laub (:122-129), Ebene +2 3×3 Laub (:130-138).
- **`TallCrystalTreeBlue`** (:230-289): Stamm 5..10 (:231), `j = 2 + i + rand(12)` (:232), Ring bei `k % 3 == 0` (:248), Krone wie oben.
- **`ScragglyCrystalTreeWithBranches`** (:185-228):
  1. Stamm 1..2 (:186); `j = i + rand(8)` (:187).
  2. Je Schritt x/z-Versatz `rand(2) − rand(2)`, Aufstieg bei `rand(4) > 0` (:197-199).
  3. `rand(4) == 1` → Ast (`makeScragglyCrystalBranch`, Länge `rand(1+j−k)`, Richtungs-Bias `rand(2)−rand(2)`; Ast-Aufstieg `rand(3) > 0`, Versatz auf −1..1 geklemmt; :141-183).
  4. Ringe 3×3 zu 50 %, dazu 50 % Laub über dem Block (:211-226).
- Alle Setzvorgänge laufen über `OreSpawnMain.setBlockFast(…, 2)`.
- **Drops:** `getItemDropped` (:54-62) hat einen **Bug**: beim gelben Setzling fehlt das `return` (:59), er droppt deshalb den blauen. Menge 1. `idPicked`, `getSeedItem`, `getCropItem` tot → Pick-Block Zuckerrohr.
- **Portierung:** Block mit `randomTick`; Bäume als `Feature` (Code-Baum, direkte Übernahme der Schleifen) oder als Methode im Block. Bug 1:1 übernehmen oder beheben: Entscheidung offen. Loot-Table je Setzling.

### BlockCrystalTorch

`crystaltorch` – „Crystal Torch“

- **Rolle:** Fackel, die an Kristallblöcken hält; Basis `BlockTorch`.
- **Werte:** Licht 0.99 → Lichtwert **14** (OreSpawnMain.java:1600). Tab Decorations (BlockCrystalTorch.java:14).
- **Client `randomDisplayTick`** (:18-48): `rand(4) == 1`. Wandfackel meta 1-4: Offset ±0.271 in x bzw. z, y + 0.213. Stehend: (0.5, 0.7, 0.5). Partikel `fireworksSpark` (Geschwindigkeit ±1/8) und `flame` (±1/60, y bis 1/10).
- **Platzierung:** `isCrystalBlock` = `crystalstone`, `crystalgrass`, `crystaltreelog`, `crystalplanks` gelten als tragend (:50-53). `canPlaceBlockAt` (:64-66). `onBlockPlaced`: Seite 1 → meta 5, 2 → 4, 3 → 3, 4 → 2, 5 → 1 (:68-86).
- Die Vanilla-Nachbarprüfung von `BlockTorch` ist nicht überschrieben. Ob die Fackel an nicht-soliden Kristallblöcken bei einem Nachbar-Update abfällt: offen.
- **Portierung:** eigene `TorchBlock`- und `WallTorchBlock`-Unterklassen mit überschriebenem `canSurvive`, das die vier Kristallblöcke (als Tag) zulässt; `StandingAndWallBlockItem`. Partikel in `animateTick`.

### BlockCrystalTreeLog

`crystaltreelog` – „Crystal Tree Wood“

- **Rolle:** Stamm; Basis `BlockRotatedPillar(Material.wood)` (BlockCrystalTreeLog.java:21), Tab Block. Das zweite Konstruktor-Argument (20) ist ungenutzt.
- **Werte:** Härte 0.2, SoundType „wood“ 1.0/1.0 (OreSpawnMain.java:1628).
- `canSustainLeaves` und `isWood` = true (:29-35); `isOpaqueCube`/`renderAsNormalBlock` = false (:37-43); Drop sich selbst, meta 0 (:25-27, :45-47); Seiten- und Kopftextur (:50-63).
- **Portierung:** `RotatedPillarBlock`, `strength(0.2f)`, `noOcclusion()`, Tags `minecraft:logs` und `mineable/axe`.

### BlockDuctTape

`ducttape` – „Duct Tape!“ (Item `ducttape_item`, `ItemDuctTape`, Stack 1; OreSpawnMain.java:1291)

- **Rolle:** Reparaturblock mit 6 Anwendungen; Basis `Block(Material.anvil)`. TickRandomly ohne `updateTick` (BlockDuctTape.java:23-24). Keine Härte → 0.
- **Hitbox je meta `l`:** x von `(1+2l)/16` bis 15/16, z 1/16..15/16, Höhe 0.25 (:27-33). Kollision oben 0.1875 (:41-47). Die Innentextur zeigt ab meta > 0 nach Westen (Seite 4) (:63-65).
- **Verhalten:** Rechtsklick (`func_149727_a`) **und** Linksklick (`func_149699_a`) → `eatDuctTapeSlice` (:79-86). Keine Seitenprüfung (:88-119):
  1. Gehaltener Stack mit Größe genau 1.
  2. `cd = maxDamage / 6`, mindestens 1.
  3. Aktueller Schaden > 0 → Schaden `−cd` (Untergrenze 0).
  4. `meta + 1`; bei ≥ 6 Luft.
- Repariert jedes beschädigbare Item. Unbeschädigte Items verbrauchen nichts.
- **Halt:** `canPlaceBlockAt` verlangt ein solides Material darunter (:121-133). `onNeighborBlockChange(…, int)` ist tot (1.6-Signatur) → der Block bleibt beim Entfernen des Untergrunds stehen.
- **Drops:** keine (:135-141). Pick-Block → `ducttape_item` (:144-146).
- **Abgleich:** 03-items.md:254 schreibt „bis 1/6 des Schadens“. Der Code repariert 1/6 der **maximalen** Haltbarkeit.
- **Portierung:**
  - `IntegerProperty USES` 0..5; `useItemOn` (mit Item) und `attack` (Linksklick); `stack.setDamageValue`.
  - `VoxelShape` je Zustand; `canSurvive` mit Nachbar-Update aus 1:1-Gründen weglassen (offen).
  - Kein BlockEntity nötig.

### BlockDuplicatorLog

`duplicatortreelog` – „Duplicator Tree Wood“

- **Rolle:** Stamm des Duplikator-Baums, der sich selbst weiterbaut; Basis `Block(Material.wood)`, Tab Block, TickRandomly (BlockDuplicatorLog.java:15-17).
- **Werte:** Härte 0.2, Holz-Sound (OreSpawnMain.java:1608). `tickRate()` ohne Parameter ist tot.
- **Verhalten `updateTick`** (:24-31): Server, `DuplicatorTreeEnable != 0` → `OreSpawnMain.OreSpawnTrees.DuplicatorTree(world, x, y, z)` (Klasse `Trees`, Trees.java:122; anderer Stapel).
- `canSustainLeaves`/`isWood` true; Drop 1× sich selbst (:41-51).
- **Config:** `OreSpawnTWEAKS.DuplicatorTreeEnable` 1 (OreSpawnMain.java:1153).
- **Portierung:** Block mit `randomTick` → Feature aus `Trees` (anderer Stapel); Tags `logs` und `mineable/axe`.

### BlockExperienceLeaves

`leaves_experience` – „Experience Tree Leaves“

- **Rolle:** nachts XP spendendes Laub; Basis `BlockLeaves`. Härte 0.2, LightOpacity 1, Gras-Sound (OreSpawnMain.java:1609).
- **Drops:** keine (`dropBlockAsItemWithChance` leer, `quantityDropped` 0; :27-33).
- **Verhalten `updateTick`** (:35-74, Server): Nachbarsuche wie BlockAppleLeaves mit r = 2 (keine Dimensionsvariante). Gefunden:
  1. `t = worldTime % 24000` außerhalb 14000..22000 → nichts (:46-50).
  2. `rand(65) == 1` und Luft darüber → Item `experience_bottle` bei y+2 (:51-56).
  3. `rand(75) == 1` und Luft darunter → `EntityExpBottle` bei (x, y−1, z), Richtung (±0.5 zufällig, −0.1, ±0.5), Geschwindigkeit 0.4, Streuung 5.0 (:57-64).
  4. Nicht gefunden → Luft (:72, :107-110).
- **Client `randomDisplayTick`** (:76-105): zwischen 13000 und 23000; Randzonen-Bonus `rate = (14000−t)/2` bzw. `(t−22000)/2` (:82-88). `rand(200+rate) == 1` und Luft darüber → 10 `fireworksSpark` bei y+1.25 mit Gauss-Geschwindigkeit (:89-96). `rand(40+rate) == 1` und Luft darunter → 4 Funken nach unten bei y−1.25 (:97-104).
- **Portierung:** eigener Block mit `randomTick`/`animateTick`; `ThrownExperienceBottle` mit `shoot(dx, -0.1, dz, 0.4f, 5.0f)`; Tageszeit über `level.getDayTime() % 24000`.

### BlockExperiencePlant

`experiencesapling` – „Experience Tree Sapling“

- **Rolle:** Setzling des Erfahrungsbaums; Basis `BlockReed`. Hitbox wie Mais, TickRandomly (BlockExperiencePlant.java:14-16).
- **Boden:** Gras, Erde, Farmland (:19-22).
- **Client:** `rand(20) == 1` → 20× `happyVillager` (:24-31).
- **Verhalten `updateTick`** (:33-42): Server, `rand(10) == 1` → Luft, dann `OreSpawnTrees.ExperienceTree(world, x, y−1, z)` (Trees.java:294, anderer Stapel).
- **Drops:** 1× sich selbst (:44-50). `idPicked`/`getSeedItem`/`getCropItem` tot → Pick-Block Zuckerrohr. Das Pflanz-Item ist `experiencetree_seed` (`ItemExperienceTreeSeed`, OreSpawnMain.java:1611).
- **Portierung:** Block mit `randomTick` → Feature aus `Trees`; `getCloneItemStack` → eigenes Item.

### BlockExtremeTorch

`extremetorch` – „Extreme Torch“

- **Rolle:** hellste Fackel, beschwört auf einem Eye-of-Ender-Block einen Cephadrome; Basis `BlockTorch`.
- **Werte:** Licht 1.0 → Lichtwert **15** (OreSpawnMain.java:1589). Tab Redstone (BlockExtremeTorch.java:16).
- **Client `randomDisplayTick`** (:20-53): bei jedem Aufruf `smoke`, `flame`, `reddust` mit Offset nach meta (wie CrystalTorch, ohne Geschwindigkeit). **Danach Aufruf von `onBlockPlacedBy(world, …, null, null)`** (:52).
- **`onBlockPlacedBy`** (:59-107):
  1. Nur wenn der Block darunter `blockeyeofender` ist (:64).
  2. Bis zu 100 Versuche (:65): `x = x0 ± 4 + rand(3) − rand(3)`, `z` analog (:66-77); y von y0−2 bis y0+2 mit solidem Block darunter und zwei Luftblöcken (:78-83).
  3. Treffer, Server: „Cephadrome“ bei (x+0.5, y+0.01, z+0.5) (:86-89). Client: 16× `smoke`/`explode`/`reddust` an der Fackel (:90-96).
  4. Sound `random.explode`, 1.0, Pitch `rand*0.2+0.9` (:97-102). Fackel → Luft (:103).
- **Falle:** Über den `randomDisplayTick`-Pfad setzt der Client die Fackel lokal auf Luft und spielt Sounds (Desync); der Server beschwört nur beim echten Platzieren. Per `setBlock` gesetzte Fackeln (z. B. aus StepUp) lösen nichts aus.
- **Abgleich:** 03-items.md:259. Stimmt.
- **Portierung:** `TorchBlock`/`WallTorchBlock` mit `lightLevel 15`; Beschwörung in `setPlacedBy` nur serverseitig; `animateTick` nur mit Partikeln, ohne Welt-Mutation.

### BlockFireflyPlant

`firefly_plant` (kein Lang-Name)

- Wie `BlockButterflyPlant` (BlockFireflyPlant.java:18-85). Unterschiede:
  - `rate = 6 − (meta & 7)` (:32), also ab meta 5 immer.
  - Nur **nachts** (`!isDaytime`) und mit `FireflyEnable` (:37).
  - Spawnt `2 + rand(5)` → 2..6 „Firefly“ (:38-41).
  - Samen `firefly_seed` (:71-73), Crop `null`, Menge `1 + rand(5)` (:67-69), Icons `firefly_0..3`.
- **Config:** `FireflyEnable` 1 (OreSpawnMain.java:6103).

### BlockLettuce

`lettuce_0`, `lettuce_1`, `lettuce_2`, `lettuce_3` – „Lettuce Plant“ (OreSpawnMain.java:1582-1585)

- **Rolle:** Salat in vier Stufen, einblockig; Basis `BlockReed`. Gepflanzt von `lettuce_seed` („Lettuce“, 3/0.45, OreSpawnMain.java:1586).
- **Werte:** Hitbox wie Mais, TickRandomly (BlockLettuce.java:14-16); Härte 0.
- **Boden:** Salatstufe, Gras, Erde, Farmland (:19-22).
- **Verhalten `updateTick`** (:24-47): Server, `age = meta & 0xFF` (:29-30).
  - `age ≥ 4`: Stufe 1 → 2, 2 → 3, 3 → 4, jeweils mit meta 0 (:31-42).
  - Sonst `age + 1` (:43-46).
  - Je Stufe also 5 wirksame Zufallsticks; Stufe 4 bleibt stehen.
- **Drops:** `lettuce_seed` (:49-51); Menge nur bei `lettuce_3`: `2 + rand(3)` → 2..4, sonst 0 (:53-58). Pick-Block Zuckerrohr.
- **Portierung:** ein Block mit `STAGE` 0..3 und `AGE` 0..4 oder vier IDs (offen); Loot-Table mit Bedingung auf die Stufe.

### BlockMosquitoPlant

`mosquito_plant` (kein Lang-Name)

- Wie `BlockButterflyPlant` (BlockMosquitoPlant.java:18-83). Unterschiede:
  - Prüft zuerst `MosquitoEnable` (:27-29); **kein** Regen-Check, **keine** Tageszeit.
  - `rate = 7 − meta` (:30-35); spawnt `2 + OreSpawnRand.nextInt(5)` → 2..6 „Mosquito“ (:38-40).
  - Samen `mosquito_seed`, Crop `null`, Menge `1 + rand(5)`.
  - `getIcon` ohne `@SideOnly` (:55).
- **Config:** `MosquitoEnable` 1 (OreSpawnMain.java:6036).

### BlockMothPlant

`moth_plant` (kein Lang-Name)

- Wie `BlockButterflyPlant` (BlockMothPlant.java:18-82). Unterschiede: nur **nachts** (`!isDaytime`) und mit `MothEnable` (:37); 1 „Moth“ (:38). Samen `moth_seed`.
- **Config:** `MothEnable` 1 (OreSpawnMain.java:6116).

### BlockPizza

`pizza` – „Pizza!“ (Item `pizza_item`, `ItemPizza`, Stack 1, Tab Food; OreSpawnMain.java:1289; manifest problems: umbenannt wegen ID-Kollision)

- **Rolle:** essbarer Block mit 6 Stücken (wie Kuchen); Basis `Block(Material.cake)`. TickRandomly ohne Wirkung (BlockPizza.java:23-24). Keine Härte → 0.
- **Hitbox:** wie BlockDuctTape (:27-60). Innentextur `pizza_inner` ab meta > 0 auf Seite 4 (:63-65).
- **Verhalten:** Rechts- **und** Linksklick → `eatPizzaSlice` (:79-86): wenn `canEat(false)`: `addStats(4, 0.2f)` (:90); `meta + 1`; bei ≥ 6 Luft (:91-97). Kein Sound, keine Statistik.
- **Halt:** Block darunter `isNormalCube` (:111-113), nur beim Platzieren wirksam; `onNeighborBlockChange(…, int)` ist tot.
- **Drops:** Item `pizza_item`, Menge 0 (:115-121).
- **Abgleich:** 03-items.md:205 (4 und 0.2 pro Biss). Stimmt.
- **Portierung:** analog `CakeBlock` mit `IntegerProperty BITES` 0..5; `useWithoutItem` plus `attack`; `player.getFoodData().eat(4, 0.2f)`.

### BlockQuinoa

`quinoa_0`, `quinoa_1`, `quinoa_2`, `quinoa_3` – „Quinoa Plant“ (OreSpawnMain.java:1572-1575)

- **Rolle:** hohe Kristall-Nutzpflanze; Basis `BlockReed`. Gepflanzt von `quinoa` (ItemCornCob 7/0.85 auf `crystalgrass`, OreSpawnMain.java:1576).
- **Boden:** Quinoa-Stufe, Gras, Erde, Farmland, `crystalgrass` (BlockQuinoa.java:22-25).
- **Verhalten `updateTick`** (:27-79): nur Stufe 1 und 3 ticken (:33-35).
  1. `myMaxHeight` wie Mais, Default `2 + rand(3)` → 2..4 (:39-41).
  2. Schwelle `age ≥ 5 − max/3` (:57).
     - `Height < max` → darüber `quinoa_0`, aktuell `quinoa_1` (:58-61).
     - Sonst aktueller Block: Stufe 1 → 3, 3 → 4 (:63-69), meta zurücksetzen (:70-71).
  3. Andernfalls `age + 1` (:75-76).
- Untere `quinoa_1`-Blöcke reifen nie; nur die Spitze wird `quinoa_3`. Dieselbe 4-Bit-Metadaten-Falle wie beim Mais.
- **Drops:** `quinoa`; Menge nur bei `quinoa_3`: `3 + rand(3)` → 3..5 (:81-90). `itemPicked`, `getSeedItem`, `getCropItem` tot → Pick-Block Zuckerrohr.
- **Portierung:** wie BlockCorn.

### BlockRadish

`radish_plant` (kein Lang-Name)

- **Rolle:** Radieschen; Basis `BlockCrops` (TickRandomly aus dem `BlockCrops`-Konstruktor). Gepflanzt von `radish` (ItemRadish 2/0.45, OreSpawnMain.java:1559).
- **Werte:** Icons `radish_0..3`, Indexierung wie ButterflyPlant (BlockRadish.java:17-25, ohne `@SideOnly`).
- **Drops:** `quantityDropped` `2 + rand(4)` → 2..5 (:27-29); `getSeed` **und** `getCrop` = `radish` (:31-37).
  - Folge: auch **unreif** geerntet droppt die Pflanze 2..5 Radieschen.
  - Reif zusätzlich 3 + Fortune Würfe `rand(15) ≤ 7`.
- **Portierung:** `CropBlock`, Loot-Table ohne Altersbedingung für die Hauptmenge (1:1) oder mit (Korrektur; Entscheidung offen).
