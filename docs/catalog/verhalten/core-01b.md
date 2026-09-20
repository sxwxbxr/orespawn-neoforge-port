# Verhalten: core-01b

Diese Batch umfasst nur `OreSpawnMain`, Schwerpunkt B: alle Rezepte, dazu Truhen-Loot und Dispenser-Registrierungen. Alles davon steht in `make_some_more_things()` (OreSpawnMain.java:1639), das `preInit` (OreSpawnMain.java:1127) am Ende aufruft (OreSpawnMain.java:1636). Die Rezepte liegen zwischen OreSpawnMain.java:2326 und OreSpawnMain.java:5034, der Loot bei OreSpawnMain.java:5051-5062, die Dispenser in `DoDispenserRegistrations()` (Aufruf OreSpawnMain.java:5065, Methode OreSpawnMain.java:5299-5434). Alle 381 Rezeptregistrierungen stehen maschinenlesbar in `docs/catalog/recipes.json`, jeweils mit Legacy-Feldnamen, Registry-Id und Meta. Die Ids kommen aus dem Manifest, Vanilla-Namen sind nach 1.21.1 übersetzt. Die Datei wurde von einem eigenen Parser über den Quelltext erzeugt und zweimal gegengeprüft: gegen die Bytecode-Aufrufliste `reference/jar/calls_OreSpawnMain.txt` (Typ und Ergebnisfeld, 381/381 gleich) und gegen den Vorgängerstand der Datei (ein einziger inhaltlicher Unterschied, siehe „Format“). Keine andere Klasse registriert Rezepte, Loot oder Dispenser: `addRecipe`, `addShapelessRecipe`, `addSmelting`, `ChestGenHooks` und `dispenseBehaviorRegistry` kommen im Quellbaum nur in `OreSpawnMain.java` vor (grep über `reference/src-20.2`). `OreDictionary` wird nirgends benutzt, deshalb enthält die JSON keinen `#ore:`-Eintrag. Wo in 1.7.10 eine Wildcard gilt, ist das am Bytecode von `client-1.7.10.jar` belegt und nicht angenommen. Davon hängt ab, welche Zutaten im Port eine Beschädigungs- oder Varianten-Bedingung brauchen.

## Zählung gegen das Jar-Inventar

| API | Jar-Inventar (05-jar-inventory.md:60) | Quelltext (grep) | geschrieben | Erklärung |
|---|---|---|---|---|
| `GameRegistry.addRecipe` | 189 | 189 | 189 `shaped` in recipes.json | Stimmt. **Zur Laufzeit 188**: OreSpawnMain.java:3020 und OreSpawnMain.java:3024 stehen in `if/else` auf `MinersDreamExpensive` (OreSpawnMain.java:3018-3025), es wird immer nur eines registriert |
| `GameRegistry.addShapelessRecipe` | 176 | 176 | 176 `shapeless` | Stimmt |
| `GameRegistry.addSmelting` | 16 | 16 | 16 `smelting` | Stimmt |
| `ChestGenHooks` | 9 | 3 Zeilen mit `ChestGenHooks` (die `getInfo`-Aufrufe OreSpawnMain.java:5051, 5055, 5059) + 9 `addItem` | 9 Loot-Zeilen (Abschnitt Truhen-Loot) | Das Inventar zählt die 9 `addItem`-Aufrufe. In `calls_OreSpawnMain.txt` stehen 3 `getInfo` + 9 `addItem` (Zeilen 8608-8637) |
| `dispenseBehaviorRegistry.putObject` | nicht im Inventar | 134 | 134 Zeilen, 133 verschiedene Items | `LizardEgg` wird doppelt registriert (OreSpawnMain.java:5300 und OreSpawnMain.java:5337), der zweite Aufruf ersetzt den ersten mit derselben Klasse |

Nach 1.21.1-Normalisierung (leere Ränder entfernt, gespiegelt verglichen, Ergebnis mit Menge und Meta) bleiben **328** verschiedene Rezepte: 209 außerhalb des Trockenei-Blocks und 119 darin (116 Rehydrierungen + 3 Teilblock-Rezepte). Die 53 überzähligen Registrierungen verteilen sich auf 34 Gruppen, siehe „Sonderfälle“.

## Semantik in 1.7.10 (am Bytecode belegt)

Klassennamen aus `reference/jar/mcp/joined.srg`, Bytecode per `javap -c` (JDK 17) aus `reference/jar/mcp/client-1.7.10.jar`.

| Stelle | Befund | Beleg |
|---|---|---|
| Shaped, `CraftingManager.func_92103_a` (`afe.a`) | `Item` → `new ItemStack(item)` = Meta 0; `Block` → `new ItemStack(block, 1, 32767)` = Wildcard; `ItemStack` bleibt unverändert | `afe.a` @183 `instanceof adb` → @202 `add.<init>(Ladb;)V`; @216 `instanceof aji` → @236 `sipush 32767` → @239 `add.<init>(Laji;II)V` |
| Shapeless, `CraftingManager.func_77596_b` (`afe.b`) | `ItemStack` → Kopie; `Item` → Meta 0; **`Block` → `new ItemStack(block)` = Meta 0, keine Wildcard** | `afe.b` @61 `instanceof adb` → @77 `add.<init>(Ladb;)V`; @91 `instanceof aji` → @107 `add.<init>(Laji;)V`, kein `sipush` |
| Schmelzen, `FurnaceRecipes.func_151396_a` / `func_151393_a` (`afa.a`) | Eingabe **immer** Meta 32767; die Block-Variante geht über `Item.getItemFromBlock` | Signaturen `afa/a (Ladb;Ladd;F)V` und `afa/a (Laji;Ladd;F)V` in joined.srg; `afa.a(adb, add, float)` @7 `sipush 32767`; Vergleich `private boolean a(add, add)` @15 `sipush 32767` |
| `ShapedRecipes` (`afh`) | prüft ungespiegelt und gespiegelt (`private boolean a(aae, int, int, boolean)`), Meta 32767 ist Wildcard | `afh.a(aae, ahb)` ruft den Vergleich @32 mit `iconst_1` und @46 mit `iconst_0`; Wildcard @175 `sipush 32767` |
| `ShapelessRecipes` (`afi`) | Meta 32767 ist Wildcard | `afi` @96 `sipush 32767` |
| `WeightedRandomChestContent(ItemStack, int, int, int)` (`qx`) | Parameter 2 = Minimum (`field_76295_d theMinimumChanceToGenerateItem`), 3 = Maximum (`field_76296_e`), 4 = Gewicht (`super(int)` → `field_76292_a itemWeight`) | `qx.<init>(add,int,int,int)` @1 `iload 4` → `qw.<init>(I)`; @12 `iload_2` → Feld `c`; @17 `iload_3` → Feld `d`; Namen aus `fields.csv` |
| Vanilla-Ofen `TileEntityFurnace.func_145845_h` (`apg.h`) | Garzeit 200 Ticks | `apg.h` @207 `sipush 200` |

Daraus folgt für `recipes.json`: jedes Item-Objekt trägt `meta` ausdrücklich; `32767` heißt Wildcard. Die Trockenei-Blöcke stecken shapeless als bloßer `Block` im Rezept und haben damit Meta 0, keine Wildcard. Für OreSpawn-Blöcke ohne Varianten ist das egal; wichtig wird es bei Vanilla-Blöcken mit Varianten und bei beschädigbaren Items.

Die OreSpawn-Werkbank und der OreSpawn-Ofen haben keine eigenen Rezeptlisten. `ContainerCrystalWorkbench` fragt `CraftingManager.getInstance().findMatchingRecipe` (ContainerCrystalWorkbench.java:43), `TileEntityCrystalFurnace` fragt `FurnaceRecipes.smelting().getSmeltingResult` (TileEntityCrystalFurnace.java:189, TileEntityCrystalFurnace.java:205). Beide sehen also dieselben Rezepte wie die Vanilla-Blöcke. Der Kristallofen braucht 150 Ticks je Vorgang (TileEntityCrystalFurnace.java:165), der Vanilla-Ofen 200 (siehe Tabelle).

## Format von recipes.json

JSON-Array in Quellreihenfolge, ein Objekt je Registrierung:

`{type, result, pattern, key, ingredients, xp, source_line[, condition]}`

- `type`: `shaped` | `shapeless` | `smelting`.
- Item-Objekt: `{"item": <Legacy-Name>, "id": <Registry-Id>, "meta": int}`, bei `result` zusätzlich `count`. `item` ist der Feldname in `OreSpawnMain` (z. B. `MyUltimateSword`) oder `Items.<name>` / `Blocks.<name>` im MCP-1.7.10-Namen, wie im Quelltext. `id` ist `orespawn:<manifest id>` oder `minecraft:<1.21.1-Name>`.
- `id` ist eine **Liste**, wenn eine Wildcard (Meta 32767) einen Vanilla-Block mit mehreren 1.21.1-Items trifft. Das kommt genau zweimal vor: `Blocks.red_flower` (OreSpawnMain.java:2837-2839) und `Blocks.planks` (OreSpawnMain.java:5034).
- `pattern` und `key` nur bei `shaped`, `ingredients` bei `shapeless` und `smelting` (dort genau ein Eintrag mit Meta 32767), `xp` nur bei `smelting` (Float aus dem Quelltext), sonst `null`.
- `condition` nur bei OreSpawnMain.java:3020 (`OreSpawnMain.MinersDreamExpensive == 0`) und OreSpawnMain.java:3024 (`NOT(...)`).

Unterschied zum Vorgängerstand der Datei: `Blocks.planks` mit Wildcard stand dort als `#minecraft:planks`. Jetzt steht dort die exakte Liste der sechs 1.7.10-Holzarten. Der 1.21.1-Tag hat 11 Einträge (`data/minecraft/tags/item/planks.json` im Jar `neoforge-21.1.248-client-extra-aka-minecraft-resources.jar`), darunter crimson, warped, mangrove, bamboo und cherry. Wer den Tag nimmt, macht das Rezept bewusst großzügiger. Alle anderen 380 Einträge sind inhaltlich gleich geblieben. Das Schema ist jetzt `item` statt `legacy`, und `meta` steht immer dabei.

## Vanilla-Übersetzung 1.7.10 → 1.21.1

Nur die Namen, die sich ändern oder eine Meta tragen. Alle anderen (`iron_ingot`, `string`, `redstone_block`, `cobblestone`, `stick`, `emerald` …) heißen gleich.

| 1.7.10 | Meta | 1.21.1 | Beleg |
|---|---|---|---|
| `Items.spawn_egg` | 50/51/52/54/55/56/57/58/59/60/61/62/65/66/90/91/92/93/94/95/96/98/100/120 | `creeper` / `skeleton` / `spider` / `zombie` / `slime` / `ghast` / `zombified_piglin` / `enderman` / `cave_spider` / `silverfish` / `blaze` / `magma_cube` / `bat` / `witch` / `pig` / `sheep` / `cow` / `chicken` / `squid` / `wolf` / `mooshroom` / `ocelot` / `horse` / `villager` + `_spawn_egg` | `EntityList.<clinit>` (`sg`), nur die Aufrufe mit Eifarben: Creeper 50, Skeleton 51, Spider 52, Zombie 54, Slime 55, Ghast 56, PigZombie 57, Enderman 58, CaveSpider 59, Silverfish 60, Blaze 61, LavaSlime 62, Bat 65, Witch 66, Pig 90, Sheep 91, Cow 92, Chicken 93, Squid 94, Wolf 95, MushroomCow 96, Ozelot 98, EntityHorse 100, Villager 120. Ohne Eifarbe (kein Vanilla-Ei): EnderDragon 63, WitherBoss 64, SnowMan 97, VillagerGolem 99. Dafür hat OreSpawn eigene `ItemSpawnEgg`s |
| `Items.dye` | 0 | `minecraft:ink_sac` | Meta 0 = Tintenbeutel; nur Meta 0 kommt vor |
| `Items.coal` | 0 | `minecraft:coal` | als bloßes `Item` Meta 0, also keine Holzkohle (OreSpawnMain.java:3017, OreSpawnMain.java:3033) |
| `Items.cooked_fished` | 0 | `minecraft:cooked_cod` | Meta 0 = gebratener Fisch |
| `Items.bed` | 0 | `minecraft:red_bed` | Ableitung, nicht belegt: 1.7.10 kennt keine Bettfarbe, Rot entspricht dem alten Aussehen |
| `Items.wooden_door` | 0 | `minecraft:oak_door` | |
| `Blocks.web` | – | `minecraft:cobweb` | |
| `Blocks.red_flower` | 32767 | 9 Items: `poppy`, `blue_orchid`, `allium`, `azure_bluet`, `red_tulip`, `orange_tulip`, `white_tulip`, `pink_tulip`, `oxeye_daisy` | `BlockFlower` (`alc`) Namensliste für `red_flower`. Löwenzahn ist `yellow_flower`. `#minecraft:small_flowers` passt nicht, weil es u. a. `dandelion` und `wither_rose` enthält (Tag-Datei im 1.21.1-Ressourcen-Jar) |
| `Blocks.quartz_block` | 0 (nur shapeless) | `minecraft:quartz_block` | Meta 0 = normaler Quarzblock; die Wildcard-Liste in der Tabelle des Parsers (mit `chiseled_quartz_block`, `quartz_pillar`) wird nie gebraucht |
| `Blocks.planks` | 32767 (Zutat, OreSpawnMain.java:5034) | `oak`/`spruce`/`birch`/`jungle`/`acacia`/`dark_oak` + `_planks` | 6 Holzarten in 1.7.10 |
| `Blocks.planks` | 0 (Ergebnis, OreSpawnMain.java:5023, OreSpawnMain.java:5025) | `minecraft:oak_planks` | Meta 0 = Eiche |

## Rezepte außerhalb des Trockenei-Blocks

262 Registrierungen, zu 210 Tabellenzeilen zusammengefasst (209 verschiedene Rezepte; die Kiste aus `CrystalPlanksBlock` steht in zwei Abschnitten, OreSpawnMain.java:2742 und OreSpawnMain.java:2868). Zusammengefasst werden Varianten mit verschobener Spalte oder Zeile und gleiche Muster mit anderem Zeichen. Schreibweise: `Feld (manifest-id) „Lang-Name“`, `mc:` = minecraft, `m*` = Meta 32767 (Wildcard), ohne Zusatz = Meta 0, `·` = leeres Feld, `×n` = Ausgabemenge. Lang-Namen aus dem Manifest.

| Zeile(n) | Typ | Ergebnis | Muster / Zutaten |
|---|---|---|---|
| **2739-2744** | | **Kristall-Grundstoffe** | |
| 2739 | shapeless | CrystalPlanksBlock (crystalplanks) „Crystal Planks“ ×4 | MyCrystalTreeLog (crystaltreelog) |
| 2740 | shapeless | CrystalWorkbenchBlock (crystalworkbench) „Crystal Workbench“ | 4× CrystalPlanksBlock (crystalplanks) |
| 2741 | shaped | CrystalFurnaceBlock (crystalfurnace) „Crystal Furnace“ | `FFF/F·F/FFF` F=CrystalStone (crystalstone) m* |
| 2742 | shaped | Blocks.chest (mc:chest) | `FFF/F·F/FFF` F=CrystalPlanksBlock (crystalplanks) m* |
| 2743,2744 | shaped | Items.wooden_door (mc:oak_door) | `FF·/FF·/FF·` F=CrystalPlanksBlock (crystalplanks) m* — Varianten: `·FF/·FF/·FF` |
| **2751-2776** | | **Schmelzen** | |
| 2751 | smelting | UraniumNugget (uranium_nugget) „Uranium Nugget“ | MyOreUraniumBlock (oreuranium) m*, XP 0.3 |
| 2753 | smelting | TitaniumNugget (titanium_nugget) „Titanium Nugget“ | MyOreTitaniumBlock (oretitanium) m*, XP 0.3 |
| 2755 | smelting | MySalt (salt) „Salt“ ×8 | MyOreSaltBlock (oresalt) m*, XP 0.1 |
| 2757 | smelting | MyPopcorn (popcorn) „Popcorn“ | MyCornCob (corn_seed) m*, XP 0.1 |
| 2759 | smelting | MyCornDog (corndog_cooked) „Corn Dog“ | MyRawCornDog (corndog_raw) m*, XP 0.4 |
| 2761 | smelting | MyBacon (cookedbacon) „Bacon!“ | MyRawBacon (bacon) m*, XP 0.2 |
| 2763 | smelting | MyCrystalPinkIngot (crystalpink_ingot) „Pink Tourmaline Ingot“ | CrystalCrystal (crystalcrystal) m*, XP 0.3 |
| 2765 | smelting | MyTigersEyeIngot (tigerseye_ingot) „Tiger's Eye Ingot“ | TigersEye (tigerseye) m*, XP 0.3 |
| 2767 | smelting | MyPeacock (cookedpeacock) „Cooked Peacock“ | MyRawPeacock (rawpeacock) m*, XP 0.4 |
| 2769 | smelting | MyCrabMeat (cookedcrabmeat) „Crab Meat!“ | MyRawCrabMeat (crabmeat) m*, XP 0.2 |
| 2771 | smelting | Items.cooked_fished (mc:cooked_cod) | MyGreenFish (greenfish) m*, XP 0.2 |
| 2772 | smelting | Items.cooked_fished (mc:cooked_cod) | MyBlueFish (bluefish) m*, XP 0.2 |
| 2773 | smelting | Items.cooked_fished (mc:cooked_cod) | MyPinkFish (pinkfish) m*, XP 0.2 |
| 2774 | smelting | Items.cooked_fished (mc:cooked_cod) | MyRockFish (rockfish) m*, XP 0.2 |
| 2775 | smelting | Items.cooked_fished (mc:cooked_cod) | MyWoodFish (woodfish) m*, XP 0.2 |
| 2776 | smelting | Items.cooked_fished (mc:cooked_cod) | MyGreyFish (greyfish) m*, XP 0.2 |
| **2809-2821** | | **Ultimate-Set, Nightmare** | |
| 2809,2810,2811 | shaped | MyUltimateSword (ultimatesword) „The Ultimate Sword“ | `·T·/·U·/·I·` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) — Varianten: `T··/U··/I··`, `··T/··U/··I` |
| 2812 | shaped | MyUltimatePickaxe (ultimatepickaxe) „The Ultimate Pickaxe“ | `TUT/·U·/·I·` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 2813,2814,2815 | shaped | MyUltimateShovel (ultimateshovel) „The Ultimate Shovel“ | `·U·/·T·/·I·` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) — Varianten: `U··/T··/I··`, `··U/··T/··I` |
| 2816 | shaped | MyUltimateHoe (ultimatehoe) „The Ultimate Hoe“ | `TU·/·I·/·I·` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 2817 | shaped | MyUltimateAxe (ultimateaxe) „The Ultimate Axe“ | `TU·/TI·/·I·` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 2818 | shaped | MyUltimateBow (ultimatebow) „The Ultimate Bow“ | `·TS/I·S/·US` S=Items.string (mc:string); I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 2819 | shaped | MySkateBow (skatebow) „Skate String Bow“ | `·TS/T·S/·TS` S=Items.string (mc:string); T=CrystalSticks (crystalsticks) |
| 2820 | shaped | MyUltimateFishingRod (ultimatefishingrod) „The Ultimate Fishing Rod“ | `··T/·US/I·S` S=Items.string (mc:string); I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 2821 | shaped | MyNightmareSword (nightmaresword) „Nightmare Sword“ | `ODO/RTR/OIO` I=Items.iron_ingot (mc:iron_ingot); O=MyNightmareScale (nightmarescale); D=Items.diamond (mc:diamond); R=Items.redstone (mc:redstone); T=MyIngotTitanium (ingottitanium) |
| **2834-2853** | | **Emerald- und Sonderschwerter** | |
| 2834,2835,2836 | shaped | MyEmeraldSword (emeraldsword) „Emerald Sword“ | `·E·/·E·/·I·` I=Items.stick (mc:stick); E=Items.emerald (mc:emerald) — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2837,2838,2839 | shaped | MyRoseSword (rosesword) „Rose Sword“ | `·E·/·E·/·I·` I=Items.stick (mc:stick); E=Blocks.red_flower (mc:{poppy,blue_orchid,allium,azure_bluet,red_tulip,orange_tulip,white_tulip,pink_tulip,oxeye_daisy}) m* — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2840 | shaped | MyEmeraldPickaxe (emeraldpickaxe) „Emerald Pickaxe“ | `EEE/·I·/·I·` I=Items.stick (mc:stick); E=Items.emerald (mc:emerald) |
| 2841,2842,2843 | shaped | MyEmeraldShovel (emeraldshovel) „Emerald Shovel“ | `·E·/·I·/·I·` I=Items.stick (mc:stick); E=Items.emerald (mc:emerald) — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2844 | shaped | MyEmeraldHoe (emeraldhoe) „Emerald Hoe“ | `EE·/·I·/·I·` I=Items.stick (mc:stick); E=Items.emerald (mc:emerald) |
| 2845 | shaped | MyEmeraldAxe (emeraldaxe) „Emerald Axe“ | `EE·/EI·/·I·` I=Items.stick (mc:stick); E=Items.emerald (mc:emerald) |
| 2846 | shaped | MyExperienceSword (experiencesword) „Experience Sword“ | `EEE/EIE/EEE` I=MyEmeraldSword (emeraldsword); E=Items.experience_bottle (mc:experience_bottle) |
| 2847 | shaped | MyPoisonSword (poisonsword) „Poison Sword“ | `EEE/EIE/EEE` I=MyEmeraldSword (emeraldsword); E=MyDeadStinkBug (deadstinkbug) |
| 2848,2849,2850 | shaped | MyRatSword (ratsword) „Rat Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=CrystalRat (crystalrat) m* — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2851,2852,2853 | shaped | MyFairySword (fairysword) „Fairy Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=CrystalFairy (crystalfairy) m* — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| **2859-2939** | | **Werkzeugsätze** | |
| 2859,2860,2861 | shaped | MyCrystalWoodSword (crystalwoodsword) „Crystal Wood Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=CrystalPlanksBlock (crystalplanks) m* — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2862 | shaped | MyCrystalWoodPickaxe (crystalwoodpickaxe) „Crystal Wood Pickaxe“ | `EEE/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalPlanksBlock (crystalplanks) m* |
| 2863,2864,2865 | shaped | MyCrystalWoodShovel (crystalwoodshovel) „Crystal Wood Shovel“ | `·E·/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalPlanksBlock (crystalplanks) m* — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2866 | shaped | MyCrystalWoodHoe (crystalwoodhoe) „Crystal Wood Hoe“ | `EE·/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalPlanksBlock (crystalplanks) m* |
| 2867 | shaped | MyCrystalWoodAxe (crystalwoodaxe) „Crystal Wood Axe“ | `EE·/EI·/·I·` I=CrystalSticks (crystalsticks); E=CrystalPlanksBlock (crystalplanks) m* |
| 2868 | shaped | Blocks.chest (mc:chest) | `EEE/E·E/EEE` E=CrystalPlanksBlock (crystalplanks) m* |
| 2874,2875,2876 | shaped | MyCrystalPinkSword (crystalpinksword) „Pink Tourmaline Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=MyCrystalPinkIngot (crystalpink_ingot) — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2877 | shaped | MyCrystalPinkPickaxe (crystalpinkpickaxe) „Pink Tourmaline Pickaxe“ | `EEE/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyCrystalPinkIngot (crystalpink_ingot) |
| 2878,2879,2880 | shaped | MyCrystalPinkShovel (crystalpinkshovel) „Pink Tourmaline Shovel“ | `·E·/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyCrystalPinkIngot (crystalpink_ingot) — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2881 | shaped | MyCrystalPinkHoe (crystalpinkhoe) „Pink Tourmaline Hoe“ | `EE·/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyCrystalPinkIngot (crystalpink_ingot) |
| 2882 | shaped | MyCrystalPinkAxe (crystalpinkaxe) „Pink Tourmaline Axe“ | `EE·/EI·/·I·` I=CrystalSticks (crystalsticks); E=MyCrystalPinkIngot (crystalpink_ingot) |
| 2883 | shaped | Items.bucket (mc:bucket) | `···/I·I/·I·` I=MyCrystalPinkIngot (crystalpink_ingot) |
| 2889,2890,2891 | shaped | MyTigersEyeSword (tigerseye_sword) „Tiger's Eye Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=MyTigersEyeIngot (tigerseye_ingot) — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2892 | shaped | MyTigersEyePickaxe (tigerseye_pickaxe) „Tiger's Eye Pickaxe“ | `EEE/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyTigersEyeIngot (tigerseye_ingot) |
| 2893,2894,2895 | shaped | MyTigersEyeShovel (tigerseye_shovel) „Tiger's Eye Shovel“ | `·E·/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyTigersEyeIngot (tigerseye_ingot) — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2896 | shaped | MyTigersEyeHoe (tigerseye_hoe) „Tiger's Eye Hoe“ | `EE·/·I·/·I·` I=CrystalSticks (crystalsticks); E=MyTigersEyeIngot (tigerseye_ingot) |
| 2897 | shaped | MyTigersEyeAxe (tigerseye_axe) „Tiger's Eye Axe“ | `EE·/EI·/·I·` I=CrystalSticks (crystalsticks); E=MyTigersEyeIngot (tigerseye_ingot) |
| 2903,2904,2905 | shaped | MyCrystalStoneSword (crystalstonesword) „Kyanite Sword“ | `·E·/·E·/·I·` I=CrystalSticks (crystalsticks); E=CrystalStone (crystalstone) m* — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2906 | shaped | MyCrystalStonePickaxe (crystalstonepickaxe) „Kyanite Pickaxe“ | `EEE/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalStone (crystalstone) m* |
| 2907,2908,2909 | shaped | MyCrystalStoneShovel (crystalstoneshovel) „Kyanite Shovel“ | `·E·/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalStone (crystalstone) m* — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2910 | shaped | MyCrystalStoneHoe (crystalstonehoe) „Kyanite Hoe“ | `EE·/·I·/·I·` I=CrystalSticks (crystalsticks); E=CrystalStone (crystalstone) m* |
| 2911 | shaped | MyCrystalStoneAxe (crystalstoneaxe) „Kyanite Axe“ | `EE·/EI·/·I·` I=CrystalSticks (crystalsticks); E=CrystalStone (crystalstone) m* |
| 2917,2918,2919 | shaped | MyRubySword (rubysword) „Ruby Sword“ | `·E·/·E·/·I·` I=Items.stick (mc:stick); E=MyRuby (ruby) — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2920 | shaped | MyRubyPickaxe (rubypickaxe) „Ruby Pickaxe“ | `EEE/·I·/·I·` I=Items.stick (mc:stick); E=MyRuby (ruby) |
| 2921,2922,2923 | shaped | MyRubyShovel (rubyshovel) „Ruby Shovel“ | `·E·/·I·/·I·` I=Items.stick (mc:stick); E=MyRuby (ruby) — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2924 | shaped | MyRubyHoe (rubyhoe) „Ruby Hoe“ | `EE·/·I·/·I·` I=Items.stick (mc:stick); E=MyRuby (ruby) |
| 2925 | shaped | MyRubyAxe (rubyaxe) „Ruby Axe“ | `EE·/EI·/·I·` I=Items.stick (mc:stick); E=MyRuby (ruby) |
| 2931,2932,2933 | shaped | MyAmethystSword (amethystsword) „Amethyst Sword“ | `·E·/·E·/·I·` I=Items.stick (mc:stick); E=MyAmethyst (amethyst) — Varianten: `E··/E··/I··`, `··E/··E/··I` |
| 2934 | shaped | MyAmethystPickaxe (amethystpickaxe) „Amethyst Pickaxe“ | `EEE/·I·/·I·` I=Items.stick (mc:stick); E=MyAmethyst (amethyst) |
| 2935,2936,2937 | shaped | MyAmethystShovel (amethystshovel) „Amethyst Shovel“ | `·E·/·I·/·I·` I=Items.stick (mc:stick); E=MyAmethyst (amethyst) — Varianten: `E··/I··/I··`, `··E/··I/··I` |
| 2938 | shaped | MyAmethystHoe (amethysthoe) „Amethyst Hoe“ | `EE·/·I·/·I·` I=Items.stick (mc:stick); E=MyAmethyst (amethyst) |
| 2939 | shaped | MyAmethystAxe (amethystaxe) „Amethyst Axe“ | `EE·/EI·/·I·` I=Items.stick (mc:stick); E=MyAmethyst (amethyst) |
| **2940-2951** | | **Großwaffen, Bertha** | |
| 2940 | shapeless | MyHammy (hammysmall) „Attitude Adjuster“ | 2× MyUltimateSword (ultimatesword) + MyBigHammer (bighammer) + GreenGoo (greengoo) |
| 2941 | shapeless | MyBattleAxe (battleaxesmall) „Battle Axe“ | MyUltimateSword (ultimatesword) + MyUltimateAxe (ultimateaxe) + GreenGoo (greengoo) |
| 2942 | shaped | MyChainsaw (chainsawsmall) „Chainsaw“ | `EEE/EIE/EEE` I=MyUltimateAxe (ultimateaxe); E=Blocks.redstone_block (mc:redstone_block) m* |
| 2943 | shaped | MyQueenBattleAxe (queenbattleaxesmall) „Queen Scale Battle Axe“ | `EIE/EIE/·I·` I=Items.iron_ingot (mc:iron_ingot); E=MyQueenScale (queenscale) |
| 2944 | shapeless | MyBertha (berthasmall) „Big Bertha“ | BerthaHandle (bbhandle) + BerthaGuard (bbguard) + BerthaBlade (bbblade) |
| 2945 | shapeless | BerthaHandle (bbhandle) „Big Bertha Handle“ | MyRayGun (raygun) + MyBigHammer (bighammer) + MyMantisClaw (mantisclaw) + MyWaterDragonScale (waterdragonscale) + GreenGoo (greengoo) |
| 2946 | shapeless | BerthaGuard (bbguard) „Big Bertha Guard“ | MolenoidNose (molenoidnose) + SeaMonsterScale (seamonsterscale) + MyMothScale (mothscale) + MyBasiliskScale (basiliskscale) + MyNightmareScale (nightmarescale) + MyEmperorScorpionScale (emperorscorpionscale) + MyJumpyBugScale (jumpybugscale) |
| 2947 | shapeless | BerthaBlade (bbblade) „Big Bertha Blade“ | MyKrakenTooth (krakentooth) + WormTooth (wormtooth) + TRexTooth (trextooth) + MyUltimateSword (ultimatesword) + CaterKillerJaw (caterkillerjaw) + SeaViperTongue (seavipertongue) + VortexEye (vortexeye) |
| 2948 | shapeless | MySlice (slicesmall) „Slice“ | MyBertha (berthasmall) + Items.iron_ingot (mc:iron_ingot) |
| 2949 | shapeless | MyIrukandjiArrow (irukandjiarrow) „Irukandji Arrow“ | MyPeacockFeather (peacockfeather) + MyIrukandji (deadirukandji) + CrystalSticks (crystalsticks) |
| 2950 | shapeless | Items.bed (mc:red_bed) | 3× MyPeacockFeather (peacockfeather) + 3× CrystalPlanksBlock (crystalplanks) |
| 2951 | shapeless | MySquidZooka (squidzookasmall) „SquidZooka!“ | 6× Items.iron_ingot (mc:iron_ingot) + 3× Items.dye (mc:ink_sac) |
| **2952-2975** | | **Speicherblöcke, Nuggets, Stäbe** | |
| 2952 | shaped | MyIngotUranium (ingoturanium) „Uranium Ingot“ | `UUU/UUU/UUU` U=UraniumNugget (uranium_nugget) |
| 2953 | shapeless | UraniumNugget (uranium_nugget) „Uranium Nugget“ ×9 | MyIngotUranium (ingoturanium) |
| 2954 | shaped | MyIngotTitanium (ingottitanium) „Titanium Ingot“ | `UUU/UUU/UUU` U=TitaniumNugget (titanium_nugget) |
| 2955 | shapeless | TitaniumNugget (titanium_nugget) „Titanium Nugget“ ×9 | MyIngotTitanium (ingottitanium) |
| 2956 | shaped | MyBlockUraniumBlock (blockuranium) „Uranium Block“ | `UUU/UUU/UUU` U=MyIngotUranium (ingoturanium) |
| 2957 | shapeless | MyIngotUranium (ingoturanium) „Uranium Ingot“ ×9 | MyBlockUraniumBlock (blockuranium) |
| 2958 | shaped | MyBlockTitaniumBlock (blocktitanium) „Titanium Block“ | `TTT/TTT/TTT` T=MyIngotTitanium (ingottitanium) |
| 2959 | shapeless | MyIngotTitanium (ingottitanium) „Titanium Ingot“ ×9 | MyBlockTitaniumBlock (blocktitanium) |
| 2960 | shaped | MyBlockMobzillaScaleBlock (blockmobzillascale) „Mobzilla Scale Block“ | `TTT/TTT/TTT` T=MyGodzillaScale (godzillascale) |
| 2961 | shapeless | MyGodzillaScale (godzillascale) „Mobzilla Scale“ ×9 | MyBlockMobzillaScaleBlock (blockmobzillascale) |
| 2962 | shaped | MyBlockRubyBlock (blockruby) „Ruby Block“ | `TTT/TTT/TTT` T=MyRuby (ruby) |
| 2963 | shapeless | MyRuby (ruby) „Ruby“ ×9 | MyBlockRubyBlock (blockruby) |
| 2964 | shaped | MyBlockAmethystBlock (blockamethyst) „Amethyst Block“ | `TTT/TTT/TTT` T=MyAmethyst (amethyst) |
| 2965 | shaped | MyCrystalPinkBlock (crystalpink_block) „Pink Tourmaline Block“ | `TTT/TTT/TTT` T=MyCrystalPinkIngot (crystalpink_ingot) |
| 2966 | shaped | MyTigersEyeBlock (tigerseye_block) „Tiger's Eye Block“ | `TTT/TTT/TTT` T=MyTigersEyeIngot (tigerseye_ingot) |
| 2967 | shapeless | MyAmethyst (amethyst) „Amethyst“ ×9 | MyBlockAmethystBlock (blockamethyst) |
| 2968 | shapeless | MyCrystalPinkIngot (crystalpink_ingot) „Pink Tourmaline Ingot“ ×9 | MyCrystalPinkBlock (crystalpink_block) |
| 2969 | shapeless | MyTigersEyeIngot (tigerseye_ingot) „Tiger's Eye Ingot“ ×9 | MyTigersEyeBlock (tigerseye_block) |
| 2970 | shaped | MyEnderPearlBlock (blockenderpearl) „Ender-Pearl Block“ | `TTT/TTT/TTT` T=Items.ender_pearl (mc:ender_pearl) |
| 2971 | shapeless | Items.ender_pearl (mc:ender_pearl) ×9 | MyEnderPearlBlock (blockenderpearl) |
| 2972 | shaped | MyEyeOfEnderBlock (blockeyeofender) „Eye-of-Ender Block“ | `TTT/TTT/TTT` T=Items.ender_eye (mc:ender_eye) |
| 2973 | shapeless | Items.ender_eye (mc:ender_eye) ×9 | MyEyeOfEnderBlock (blockeyeofender) |
| 2974 | shaped | MyThunderStaff (thunderstaff) „Thunder Staff“ | `DR·/RR·/··R` D=Items.diamond (mc:diamond); R=MyRuby (ruby) |
| 2975 | shaped | MyWrench (wrench) „Wrench“ | `D·D/·D·/·D·` D=Items.iron_ingot (mc:iron_ingot) |
| **2976-2991** | | **Essen** | |
| 2978 | shapeless | MyButter (butter) „Butter“ ×4 | 2× Items.milk_bucket (mc:milk_bucket) |
| 2979 | shapeless | MyCheese (cheese) „Cheese“ ×2 | 4× Items.milk_bucket (mc:milk_bucket) |
| 2980 | shapeless | MyButteredPopcorn (popcorn_buttered) „Buttered Popcorn“ | MyPopcorn (popcorn) + MyButter (butter) |
| 2981 | shapeless | MyButteredSaltedPopcorn (popcorn_buttered_salted) „Buttered and Salted Popcorn“ | MyButteredPopcorn (popcorn_buttered) + MySalt (salt) |
| 2982 | shapeless | MyButteredSaltedPopcorn (popcorn_buttered_salted) „Buttered and Salted Popcorn“ | MyPopcorn (popcorn) + MySalt (salt) + MyButter (butter) |
| 2983 | shapeless | MyPopcornBag (popcorn_bag) „Bag of Popcorn“ | 6× MyButteredSaltedPopcorn (popcorn_buttered_salted) + 3× Items.paper (mc:paper) |
| 2984 | shapeless | MyRawCornDog (corndog_raw) „Raw Corn Dog“ ×4 | MyCornCob (corn_seed) + Items.chicken (mc:chicken) + Items.porkchop (mc:porkchop) + Items.stick (mc:stick) |
| 2985 | shapeless | MyRawBacon (bacon) „Raw Bacon“ ×2 | MySalt (salt) + Items.porkchop (mc:porkchop) |
| 2986 | shapeless | MyButterCandy (buttercandy) „Butter Candy!“ ×4 | MyButter (butter) + Items.sugar (mc:sugar) |
| 2987 | shapeless | MySalad (salad) „Garden Salad“ | MyLettuce (lettuce_seed) + MyTomato (tomato_seed) + MyRadish (radish) + Items.carrot (mc:carrot) + Items.bowl (mc:bowl) |
| 2988 | shapeless | MyBLT (blt_sandwich) „BLT Sandwich!“ | MyBacon (cookedbacon) + MyLettuce (lettuce_seed) + MyTomato (tomato_seed) + MyButter (butter) + Items.bread (mc:bread) |
| 2989 | shapeless | MyPizzaItem (pizza_item) „Pizza!“ | MyTomato (tomato_seed) + MyCheese (cheese) + MyBacon (cookedbacon) + Items.bread (mc:bread) |
| 2990 | shaped | MyDuctTapeItem (ducttape_item) „Duct Tape!“ | `···/AAA/RRR` R=Items.string (mc:string); A=Items.slime_ball (mc:slime_ball) |
| 2991 | shapeless | MyCrabbyPatty (crabbypatty) „A Crabby Patty!“ | MyCrabMeat (cookedcrabmeat) + MyLettuce (lettuce_seed) + MyTomato (tomato_seed) + Items.bread (mc:bread) |
| **3002-3056** | | **Utility** | |
| 3002 | shapeless | ZooCage2 (zoo2) „Extra Small Zoo Cage“ | Blocks.iron_block (mc:iron_block) + Blocks.glass (mc:glass) + Blocks.quartz_block (mc:quartz_block) |
| 3003 | shapeless | ZooCage4 (zoo4) „Small Zoo Cage“ | ZooCage2 (zoo2) + Blocks.iron_block (mc:iron_block) + Blocks.glass (mc:glass) + Blocks.quartz_block (mc:quartz_block) |
| 3004 | shapeless | ZooCage6 (zoo6) „Medium Zoo Cage“ | ZooCage4 (zoo4) + Blocks.iron_block (mc:iron_block) + Blocks.glass (mc:glass) + Blocks.quartz_block (mc:quartz_block) |
| 3005 | shapeless | ZooCage8 (zoo8) „Large Zoo Cage“ | ZooCage6 (zoo6) + Blocks.iron_block (mc:iron_block) + Blocks.glass (mc:glass) + Blocks.quartz_block (mc:quartz_block) |
| 3006 | shapeless | ZooCage10 (zoo10) „Extra Large Zoo Cage“ | ZooCage8 (zoo8) + Blocks.iron_block (mc:iron_block) + Blocks.glass (mc:glass) + Blocks.quartz_block (mc:quartz_block) |
| 3009 | shapeless | InstantShelter (instantshelter) „Instant Survival Shelter“ | Blocks.redstone_block (mc:redstone_block) + Items.stick (mc:stick) + Blocks.cobblestone (mc:cobblestone) |
| 3010 | shapeless | InstantGarden (instantgarden) „Instant Survival Garden“ | Blocks.redstone_block (mc:redstone_block) + Items.wheat (mc:wheat) + Items.gunpowder (mc:gunpowder) |
| 3011 | shapeless | CreeperLauncher (creeperlauncher) „Creeper Launcher“ ×4 | Items.paper (mc:paper) + Items.redstone (mc:redstone) + Items.stick (mc:stick) |
| 3012 | shapeless | NetherLost (netherlost) „Nether Tracker“ | Items.nether_star (mc:nether_star) + Blocks.netherrack (mc:netherrack) |
| 3013 | shaped | Sifter (sifter) „Sifter“ | `RRR/RAR/RRR` R=Items.stick (mc:stick); A=Items.string (mc:string) |
| 3015 | shaped | MagicApple (magicapple) „OMG! No! Don't do it!!!“ | `RRR/RAR/RRR` R=Blocks.redstone_block (mc:redstone_block) m*; A=Items.apple (mc:apple) |
| 3017 | shaped | RandomDungeon (randomdungeon) „Random Dungeon“ | `RRR/RAR/RRR` R=Blocks.redstone_block (mc:redstone_block) m*; A=Items.coal (mc:coal) |
| 3020 | shaped | MinersDream (minersdream) „Miner's Dream“ | `CCC/RRR/GGG` R=Blocks.redstone_block (mc:redstone_block) m*; C=Blocks.cactus (mc:cactus) m*; G=Items.gunpowder (mc:gunpowder) — **nur wenn** `OreSpawnMain.MinersDreamExpensive == 0` |
| 3024 | shaped | MinersDream (minersdream) „Miner's Dream“ | `CCC/RRR/GGG` R=Blocks.redstone_block (mc:redstone_block) m*; C=Blocks.cactus (mc:cactus) m*; G=Blocks.tnt (mc:tnt) m* — **nur wenn** `NOT(OreSpawnMain.MinersDreamExpensive == 0)` |
| 3027 | shaped | MyStepUp (step_up) „Stairs going Up“ ×8 | `GC·/·C·/·C·` C=Blocks.cobblestone (mc:cobblestone) m*; G=Items.gunpowder (mc:gunpowder) |
| 3029 | shaped | MyStepDown (step_down) „Stairs going Down“ ×8 | `·C·/·C·/GC·` C=Blocks.cobblestone (mc:cobblestone) m*; G=Items.gunpowder (mc:gunpowder) |
| 3031 | shaped | MyStepAccross (step_accross) „Insta-Bridge“ ×8 | `·C·/GC·/·C·` C=Blocks.cobblestone (mc:cobblestone) m*; G=Items.gunpowder (mc:gunpowder) |
| 3033 | shapeless | ExtremeTorch (extremetorch) „Extreme Torch“ ×4 | Items.redstone (mc:redstone) + Items.stick (mc:stick) + Items.coal (mc:coal) |
| 3034 | shapeless | ExtremeTorch (extremetorch) „Extreme Torch“ | Items.redstone (mc:redstone) + Blocks.torch (mc:torch) |
| 3035 | shapeless | CrystalSticks (crystalsticks) „Crystal Shards“ ×6 | 2× CrystalPlanksBlock (crystalplanks) |
| 3036 | shapeless | CrystalTorch (crystaltorch) „Crystal Torch“ ×6 | CrystalCoal (crystalcoal) + CrystalSticks (crystalsticks) |
| 3040 | shaped | KrakenRepellent (krakenrepellent) „Kraken Repellent“ | `D·D/STS/D·D` D=MyDeadStinkBug (deadstinkbug); T=ExtremeTorch (extremetorch) m*; S=Items.string (mc:string) |
| 3042 | shaped | CreeperRepellent (creeperrepellent) „Creeper Repellent“ | `D·D/STS/D·D` D=GreenGoo (greengoo); T=ExtremeTorch (extremetorch) m*; S=Items.string (mc:string) |
| 3048 | shapeless | MyAppleSeed (appletree_seed) „Apple Tree Seed“ ×6 | Items.apple (mc:apple) |
| 3050 | shapeless | MyCherrySeed (cherrytree_seed) „Cherry Pit“ | MyCherry (cherries) |
| 3052 | shapeless | MyPeachSeed (peachtree_seed) „Peach Pit“ | MyPeach (peach) |
| 3054 | shapeless | MyExperienceCatcher (experiencecatcher) „Experience Orb Catcher“ | Items.glass_bottle (mc:glass_bottle) + Items.stick (mc:stick) + Items.string (mc:string) |
| 3056 | shaped | MyExperienceTreeSeed (experiencetree_seed) „Experience Tree Seed“ | `EEE/EAE/EEE` A=MyAppleSeed (appletree_seed); E=Items.experience_bottle (mc:experience_bottle) |
| **3180-3190** | | **Nachladen** | |
| 3184 | shapeless | MyRayGun (raygun) „A Freakin' Ray Gun!“ | Blocks.redstone_block (mc:redstone_block) + MyRayGun (raygun) m* |
| 3187 | shapeless | MySquidZooka (squidzookasmall) „SquidZooka!“ | Items.dye (mc:ink_sac) + MySquidZooka (squidzookasmall) m* |
| **4650-4780** | | **Rüstungen** | |
| 4658,4659 | shaped | UltimateHelmet (ultimate_helmet) „The Ultimate Helmet“ | `···/TIT/U·U` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) — Varianten: `TIT/U·U/···` |
| 4660 | shaped | UltimateBody (ultimate_chest) „The Ultimate Chestplate“ | `I·I/TTT/UUU` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 4661 | shaped | UltimateLegs (ultimate_leggings) „The Ultimate Leggings“ | `III/T·T/U·U` I=Items.iron_ingot (mc:iron_ingot); U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) |
| 4662,4663 | shaped | UltimateBoots (ultimate_boots) „The Ultimate Boots“ | `···/T·T/U·U` U=MyIngotUranium (ingoturanium); T=MyIngotTitanium (ingottitanium) — Varianten: `T·T/U·U/···` |
| 4668,4669 | shaped | LavaEelHelmet (lavaeel_helmet) „Lava Eel Helmet“ | `···/***/*·*` *=MyLavaEel (lavaeel) — Varianten: `***/*·*/···` |
| 4670 | shaped | LavaEelBody (lavaeel_chest) „Lava Eel Chestplate“ | `*·*/***/***` *=MyLavaEel (lavaeel) |
| 4671 | shaped | LavaEelLegs (lavaeel_leggings) „Lava Eel Leggings“ | `***/*·*/*·*` *=MyLavaEel (lavaeel) |
| 4672 | shaped | LavaEelBoots (lavaeel_boots) „Lava Eel Boots“ | `···/*·*/*·*` *=MyLavaEel (lavaeel) |
| 4677,4678 | shaped | MothScaleHelmet (mothscale_helmet) „Moth Scale Helmet“ | `···/***/*·*` *=MyMothScale (mothscale) — Varianten: `***/*·*/···` |
| 4679 | shaped | MothScaleBody (mothscale_chest) „Moth Scale Chestplate“ | `*·*/***/***` *=MyMothScale (mothscale) |
| 4680 | shaped | MothScaleLegs (mothscale_leggings) „Moth Scale Leggings“ | `***/*·*/*·*` *=MyMothScale (mothscale) |
| 4681 | shaped | MothScaleBoots (mothscale_boots) „Moth Scale Boots“ | `···/*·*/*·*` *=MyMothScale (mothscale) |
| 4686,4687 | shaped | EmeraldHelmet (emerald_helmet) „Emerald Helmet“ | `···/***/*·*` *=Items.emerald (mc:emerald) — Varianten: `***/*·*/···` |
| 4688 | shaped | EmeraldBody (emerald_chest) „Emerald Chestplate“ | `*·*/***/***` *=Items.emerald (mc:emerald) |
| 4689 | shaped | EmeraldLegs (emerald_leggings) „Emerald Leggings“ | `***/*·*/*·*` *=Items.emerald (mc:emerald) |
| 4690 | shaped | EmeraldBoots (emerald_boots) „Emerald Boots“ | `···/*·*/*·*` *=Items.emerald (mc:emerald) |
| 4695,4696 | shaped | RubyHelmet (ruby_helmet) „Ruby Helmet“ | `···/***/*·*` *=MyRuby (ruby) — Varianten: `***/*·*/···` |
| 4697 | shaped | RubyBody (ruby_chest) „Ruby Chestplate“ | `*·*/***/***` *=MyRuby (ruby) |
| 4698 | shaped | RubyLegs (ruby_leggings) „Ruby Leggings“ | `***/*·*/*·*` *=MyRuby (ruby) |
| 4699 | shaped | RubyBoots (ruby_boots) „Ruby Boots“ | `···/*·*/*·*` *=MyRuby (ruby) |
| 4704,4705 | shaped | AmethystHelmet (amethyst_helmet) „Amethyst Helmet“ | `···/***/*·*` *=MyAmethyst (amethyst) — Varianten: `***/*·*/···` |
| 4706 | shaped | AmethystBody (amethyst_chest) „Amethyst Chestplate“ | `*·*/***/***` *=MyAmethyst (amethyst) |
| 4707 | shaped | AmethystLegs (amethyst_leggings) „Amethyst Leggings“ | `***/*·*/*·*` *=MyAmethyst (amethyst) |
| 4708 | shaped | AmethystBoots (amethyst_boots) „Amethyst Boots“ | `···/*·*/*·*` *=MyAmethyst (amethyst) |
| 4713,4714 | shaped | CrystalPinkHelmet (pink_helmet) „Pink Tourmailine Helmet“ | `···/***/*·*` *=MyCrystalPinkIngot (crystalpink_ingot) — Varianten: `***/*·*/···` |
| 4715 | shaped | CrystalPinkBody (pink_chest) „Pink Tourmailine Chestplate“ | `*·*/***/***` *=MyCrystalPinkIngot (crystalpink_ingot) |
| 4716 | shaped | CrystalPinkLegs (pink_leggings) „Pink Tourmailine Leggings“ | `***/*·*/*·*` *=MyCrystalPinkIngot (crystalpink_ingot) |
| 4717 | shaped | CrystalPinkBoots (pink_boots) „Pink Tourmailine Boots“ | `···/*·*/*·*` *=MyCrystalPinkIngot (crystalpink_ingot) |
| 4734,4735 | shaped | MobzillaHelmet (mobzilla_helmet) „Mobzilla Scale Helmet“ | `···/***/*·*` *=MyGodzillaScale (godzillascale) — Varianten: `***/*·*/···` |
| 4736 | shaped | MobzillaBody (mobzilla_chest) „Mobzilla Scale Chestplate“ | `*·*/***/***` *=MyGodzillaScale (godzillascale) |
| 4737 | shaped | MobzillaLegs (mobzilla_leggings) „Mobzilla Scale Leggings“ | `***/*·*/*·*` *=MyGodzillaScale (godzillascale) |
| 4738 | shaped | MobzillaBoots (mobzilla_boots) „Mobzilla Scale Boots“ | `···/*·*/*·*` *=MyGodzillaScale (godzillascale) |
| 4739,4740 | shaped | LapisHelmet (lapis_helmet) „Lapis Lazuli Helmet“ | `···/***/*·*` *=Blocks.lapis_block (mc:lapis_block) m* — Varianten: `***/*·*/···` |
| 4741 | shaped | LapisBody (lapis_chest) „Lapis Lazuli Chestplate“ | `*·*/***/***` *=Blocks.lapis_block (mc:lapis_block) m* |
| 4742 | shaped | LapisLegs (lapis_leggings) „Lapis Lazuli Leggings“ | `***/*·*/*·*` *=Blocks.lapis_block (mc:lapis_block) m* |
| 4743 | shaped | LapisBoots (lapis_boots) „Lapis Lazuli Boots“ | `···/*·*/*·*` *=Blocks.lapis_block (mc:lapis_block) m* |
| 4744,4745 | shaped | QueenHelmet (queen_helmet) „Queen Scale Helmet“ | `···/***/*·*` *=MyQueenScale (queenscale) — Varianten: `***/*·*/···` |
| 4746 | shaped | QueenBody (queen_chest) „Queen Scale Chestplate“ | `*·*/***/***` *=MyQueenScale (queenscale) |
| 4747 | shaped | QueenLegs (queen_leggings) „Queen Scale Leggings“ | `***/*·*/*·*` *=MyQueenScale (queenscale) |
| 4748 | shaped | QueenBoots (queen_boots) „Queen Scale Boots“ | `···/*·*/*·*` *=MyQueenScale (queenscale) |
| 4753,4754 | shaped | PeacockFeatherHelmet (peacock_helmet) „Peacock Feather Helmet“ | `···/***/*·*` *=MyPeacockFeather (peacockfeather) — Varianten: `***/*·*/···` |
| 4755 | shaped | PeacockFeatherBody (peacock_chest) „Peacock Feather Chestplate“ | `*·*/***/***` *=MyPeacockFeather (peacockfeather) |
| 4756 | shaped | PeacockFeatherLegs (peacock_leggings) „Peacock Feather Leggings“ | `***/*·*/*·*` *=MyPeacockFeather (peacockfeather) |
| 4757 | shaped | PeacockFeatherBoots (peacock_boots) „Peacock Feather Boots“ | `···/*·*/*·*` *=MyPeacockFeather (peacockfeather) |
| 4762,4763 | shaped | TigersEyeHelmet (tigerseye_helmet) „Tiger's Eye Helmet“ | `···/***/*·*` *=MyTigersEyeIngot (tigerseye_ingot) — Varianten: `***/*·*/···` |
| 4764 | shaped | TigersEyeBody (tigerseye_chest) „Tiger's Eye Chestplate“ | `*·*/***/***` *=MyTigersEyeIngot (tigerseye_ingot) |
| 4765 | shaped | TigersEyeLegs (tigerseye_leggings) „Tiger's Eye Leggings“ | `***/*·*/*·*` *=MyTigersEyeIngot (tigerseye_ingot) |
| 4766 | shaped | TigersEyeBoots (tigerseye_boots) „Tiger's Eye Boots“ | `···/*·*/*·*` *=MyTigersEyeIngot (tigerseye_ingot) |
| 4771 | shaped | ExperienceHelmet (experience_helmet) „Experience Helmet“ | `EEE/EAE/EEE` A=EmeraldHelmet (emerald_helmet); E=Items.experience_bottle (mc:experience_bottle) |
| 4772 | shaped | ExperienceBody (experience_chest) „Experience Chestplate“ | `EEE/EAE/EEE` A=EmeraldBody (emerald_chest); E=Items.experience_bottle (mc:experience_bottle) |
| 4773 | shaped | ExperienceLegs (experience_leggings) „Experience Leggings“ | `EEE/EAE/EEE` A=EmeraldLegs (emerald_leggings); E=Items.experience_bottle (mc:experience_bottle) |
| 4774 | shaped | ExperienceBoots (experience_boots) „Experience Boots“ | `EEE/EAE/EEE` A=EmeraldBoots (emerald_boots); E=Items.experience_bottle (mc:experience_bottle) |
| 4775 | shaped | Blocks.web (mc:cobweb) | `***/*·*/***` *=Items.string (mc:string) |
| **5000-5040** | | **Käfig, Holz, Hoverboard** | |
| 5011 | shaped | CageEmpty (cageempty) „Empty Critter Cage“ ×2 | `IWI/W·W/IWI` W=Items.stick (mc:stick); I=Items.iron_ingot (mc:iron_ingot) |
| 5012 | shaped | CageEmpty (cageempty) „Empty Critter Cage“ ×2 | `IWI/W·W/IWI` W=CrystalSticks (crystalsticks); I=MyCrystalPinkIngot (crystalpink_ingot) |
| 5023 | shapeless | Blocks.planks (mc:oak_planks) ×4 | MySkyTreeLog (skytreelog) |
| 5025 | shapeless | Blocks.planks (mc:oak_planks) ×4 | MyDT (duplicatortreelog) |
| 5034 | shaped | MyElevator (elevator) „Hoverboard“ | `···/WWW/DRD` W=Blocks.planks (mc:{oak_planks,spruce_planks,birch_planks,jungle_planks,acacia_planks,dark_oak_planks}) m*; R=Items.redstone (mc:redstone); D=Items.diamond (mc:diamond) |

## Sonderfälle und Port-Hinweise

- **Konfigabhängiges Rezept.** Miner's Dream `CCC/RRR/GGG` mit `G = Items.gunpowder`, wenn `MinersDreamExpensive == 0` (OreSpawnMain.java:3018-3021), sonst `G = Blocks.tnt` (OreSpawnMain.java:3022-3025). Config-Schlüssel `OreSpawnTWEAKS/MinersDreamExpensive`, Default 0 (manifest). Im Port zwei JSON-Rezepte mit `neoforge:conditions` und einer eigenen `ICondition`, die den `ModConfigSpec`-Wert liest. Offen: ob die Config beim Laden der Rezepte schon gelesen ist, am NeoForge-21.1.248-Jar zu prüfen, bevor die Bedingung gebaut wird.
- **Duplikate.** 34 Gruppen, 53 überzählige Registrierungen. Fast alle entstehen, weil ein Muster wie `" T "` in 1.7.10 dreispaltig mit fester Spalte gilt. Deshalb registriert der Autor Schwerter und Schaufeln in drei Spalten (je 3), Helme und Ultimate-Stiefel in zwei Zeilen (je 2) und die Tür in zwei Spalten. Ein 1.21.1-JSON-Muster wird geschrumpft und passt überall, eine Datei genügt. Echt doppelt ist die Kiste aus `CrystalPlanksBlock` (OreSpawnMain.java:2742 mit `F`, OreSpawnMain.java:2868 mit `E`, gleiches Muster).
- **Beschädigbare Zutaten mit Meta 0.** Ein bloßes `Item` als Zutat heißt in 1.7.10 Meta 0, bei Werkzeug, Waffe und Rüstung also „unbeschädigt“. Eine normale 1.21.1-Zutat prüft keinen Schaden. Betroffen sind (per Parser ermittelt, beschädigbar laut `setMaxDamage` bzw. Superklasse `ItemSword`/`ItemAxe`/`ItemArmor` im Manifest):
  - `MyEmeraldSword` (emeraldsword, `EmeraldSword` → ItemSword): OreSpawnMain.java:2846, OreSpawnMain.java:2847; `setMaxDamage(1300)` EmeraldSword.java:21
  - `MyUltimateSword` (ultimatesword, `UltimateSword` → ItemSword): OreSpawnMain.java:2940 (×2), OreSpawnMain.java:2941, OreSpawnMain.java:2947; `setMaxDamage(3000)` UltimateSword.java:29
  - `MyBigHammer` (bighammer, `BigHammer` → ItemSword): OreSpawnMain.java:2940, OreSpawnMain.java:2945; `setMaxDamage(9000)` BigHammer.java:19
  - `MyUltimateAxe` (ultimateaxe, `UltimateAxe` → ItemAxe): OreSpawnMain.java:2941, OreSpawnMain.java:2942; `setMaxDamage(3000)` UltimateAxe.java:21
  - `MyRayGun` (raygun, `ItemRayGun` → Item): OreSpawnMain.java:2945; `setMaxDamage(50)` ItemRayGun.java:15
  - `MyMantisClaw` (mantisclaw, `MantisClaw` → ItemSword): OreSpawnMain.java:2945; `setMaxDamage(1000)` MantisClaw.java:19
  - `MyBertha` (berthasmall, `Bertha` → ItemSword): OreSpawnMain.java:2948; `setMaxDamage(9000)` Bertha.java:18
  - `EmeraldHelmet` (emerald_helmet, `ItemOreSpawnArmor` → ItemArmor): OreSpawnMain.java:4771; kein `setMaxDamage` in ItemOreSpawnArmor.java, beschädigbar als `ItemArmor` (Haltbarkeitswert nicht in dieser Batch)
  - `EmeraldBody` (emerald_chest, `ItemOreSpawnArmor` → ItemArmor): OreSpawnMain.java:4772; kein `setMaxDamage` in ItemOreSpawnArmor.java, beschädigbar als `ItemArmor` (Haltbarkeitswert nicht in dieser Batch)
  - `EmeraldLegs` (emerald_leggings, `ItemOreSpawnArmor` → ItemArmor): OreSpawnMain.java:4773; kein `setMaxDamage` in ItemOreSpawnArmor.java, beschädigbar als `ItemArmor` (Haltbarkeitswert nicht in dieser Batch)
  - `EmeraldBoots` (emerald_boots, `ItemOreSpawnArmor` → ItemArmor): OreSpawnMain.java:4774; kein `setMaxDamage` in ItemOreSpawnArmor.java, beschädigbar als `ItemArmor` (Haltbarkeitswert nicht in dieser Batch)
  Für echtes 1:1 braucht es eine Zutat mit Bedingung `minecraft:damage` = 0. Offen: ob `neoforge:components` einen Stack mit Prototyp-Schaden 0 als Treffer zählt, das ist am Jar zu prüfen. Die Alternative ist, die Zutaten bewusst großzügiger zu machen; das gehört dann in den Pack-README.
- **Nachladen.** `MyRayGun` + `Blocks.redstone_block` ergibt eine neue `MyRayGun` (OreSpawnMain.java:3182-3184), `MySquidZooka` + `Items.dye` (Tintenbeutel) eine neue `MySquidZooka` (OreSpawnMain.java:3185-3187). Die Waffenzutat ist per `setItemDamage(32767)` eine Wildcard; das Ergebnis ist ein frischer Stack mit Schaden 0, also voll geladen (`setMaxDamage(50)` ItemRayGun.java:15, `setMaxDamage(100)` ItemSquidZooka.java:16). In 1.21.1 genügt ein shapeless-Rezept ohne Komponentenbedingung. Das Ergebnis darf keine Komponenten vom Eingangsstack kopieren, sonst lädt es nicht nach. Zusätzlich hat `MySquidZooka` ein Herstellungsrezept (OreSpawnMain.java:2951), `MyRayGun` hat keins.
- **Vanilla-Ergebnisse (17 ohne Spawn-Eier).** Kiste ×2 Registrierungen, Eichentür ×1, Eimer aus `MyCrystalPinkIngot`, `red_bed`, `cobweb`, 2× `oak_planks`, Enderperlen und Enderaugen aus den Speicherblöcken, 6× gebratener Kabeljau. Unterschiede zu 1.21.1-Vanilla, gelesen aus `neoforge-21.1.248-client-extra-aka-minecraft-resources.jar`: `oak_door.json` gibt `count: 3`, OreSpawn gibt 1 (OreSpawnMain.java:2743-2744). Ein `cobweb`-Rezept gibt es in 1.21.1 nicht, OreSpawn fügt eins hinzu (OreSpawnMain.java:4775). `cooked_cod.json` hat `experience: 0.35`, die OreSpawn-Fische geben 0.2 (OreSpawnMain.java:2771-2776). Rezept-Ids gehören in den Namensraum `orespawn`, damit nichts Vanilla überschreibt.
- **`CrystalPlanksBlock` gehört nicht in `#minecraft:planks`.** In 1.7.10 ist es ein eigener Block, Vanilla-Rezepte mit `Blocks.planks` nehmen ihn nicht. Genau deshalb registriert OreSpawn eigene Kiste und Tür. Mit Tag-Mitgliedschaft liefe die Kiste in 1.21.1 doppelt (`chest.json` benutzt `tag: minecraft:planks`), und das Kristallholz passte in jedes Holzrezept.
- **Schmelzen.** 16× `minecraft:smelting` mit `experience` = `xp`. Die Garzeit gehört in 1.7.10 zum Ofen, nicht zum Rezept: Vanilla 200 Ticks (`apg.h` @207). Das deckt sich mit `cookingtime: 200` in `cooked_cod.json` von 1.21.1. Den Kristallofen mit 150 Ticks (TileEntityCrystalFurnace.java:165) regelt dessen Block-Entity, nicht das Rezept. Blast Furnace, Smoker und Lagerfeuer gab es nicht, also für 1:1 keine Zusatzrezepte.
- **Teilblöcke.** 9× `MyGodzillaPartSpawnBlock` → `MyGodzillaSpawnBlock` (OreSpawnMain.java:2545), dasselbe für `MyTheKingPartSpawnBlock` (OreSpawnMain.java:2551) und `MyTheQueenPartSpawnBlock` (OreSpawnMain.java:2557). Die drei Teilblöcke sind die einzigen der 119 `*SpawnBlock`-Felder im Manifest ohne Wasser-Rezept. Das Ergebnis ist wieder ein Trockenei-Block, der dann per Wasser zum Ei wird (`GodzillaEgg` OreSpawnMain.java:2548, `TheKingEgg` OreSpawnMain.java:2554, `TheQueenEgg` OreSpawnMain.java:2560).
- **`WitherBossEgg` mit Meta 64** (OreSpawnMain.java:2404): `ItemSpawnEgg` liest den Schaden nirgends (grep auf `Damage` in ItemSpawnEgg.java ohne Treffer). 64 ist die Vanilla-EntityList-Id des Withers (`sg.<clinit>`), vermutlich aus dem Vanilla-Eimuster übernommen. Im Port ohne Wirkung, weglassen.
- **Speicherblöcke als `OreGenericEgg`.** `MyEnderPearlBlock` und `MyEyeOfEnderBlock` sind im Quelltext `new OreGenericEgg(...)` (OreSpawnMain.java:1634-1635), das Manifest führt sie deshalb als `kind: dried_egg_ore`. Inhaltlich sind es 3×3-Speicherblöcke (OreSpawnMain.java:2970-2973). Wer Trockeneier über `kind` filtert, erwischt diese beiden mit.
- **Pizza und Klebeband.** Ergebnis ist das Item (`MyPizzaItem` → `pizza_item`, `MyDuctTapeItem` → `ducttape_item`), nicht der Block.

## Trockenei-Rehydrierung (119 Registrierungen, OreSpawnMain.java:2326-2680)

116× shapeless `Items.water_bucket` + Trockenei-Block (Meta 0) → Ei; davon 24 Vanilla-`spawn_egg` mit Meta (Übersetzung siehe oben) und 92 OreSpawn-`ItemSpawnEgg`. Dazu 3 Teilblock-Rezepte. **Der leere Eimer bleibt übrig**, in 1.7.10 wie in 1.21.1. In 1.7.10 entsteht der Eimer in `Item.registerItems` (`adb`) @2323 als `astore_0`; `water_bucket` (Id 326) erhält ihn @2360-2361 per `aload_0` → `c(Ladb;)Ladb;` = `func_77642_a` (setContainerItem), und `SlotCrafting` (`aax`) ruft @69 `adb.t()` = `func_77668_q` (getContainerItem). In 1.21.1 hat `WATER_BUCKET` `craftRemainder(BUCKET)` (Items.java:1215 im Jar `neoforge-21.1.248-sources.jar`). Im Port ist dafür nichts zu tun.

| Zeile | Zutaten | Ergebnis (Feld → id) | Meta |
|---|---|---|---|
| 2326 | water_bucket + MySpiderSpawnBlock (orespider) | Items.spawn_egg (mc:spider_spawn_egg) | 52 |
| 2329 | water_bucket + MyBatSpawnBlock (orebat) | Items.spawn_egg (mc:bat_spawn_egg) | 65 |
| 2332 | water_bucket + MyCowSpawnBlock (orecow) | Items.spawn_egg (mc:cow_spawn_egg) | 92 |
| 2335 | water_bucket + MyPigSpawnBlock (orepig) | Items.spawn_egg (mc:pig_spawn_egg) | 90 |
| 2338 | water_bucket + MySquidSpawnBlock (oresquid) | Items.spawn_egg (mc:squid_spawn_egg) | 94 |
| 2341 | water_bucket + MyChickenSpawnBlock (orechicken) | Items.spawn_egg (mc:chicken_spawn_egg) | 93 |
| 2344 | water_bucket + MyCreeperSpawnBlock (orecreeper) | Items.spawn_egg (mc:creeper_spawn_egg) | 50 |
| 2347 | water_bucket + MySkeletonSpawnBlock (oreskeleton) | Items.spawn_egg (mc:skeleton_spawn_egg) | 51 |
| 2350 | water_bucket + MyZombieSpawnBlock (orezombie) | Items.spawn_egg (mc:zombie_spawn_egg) | 54 |
| 2353 | water_bucket + MySlimeSpawnBlock (oreslime) | Items.spawn_egg (mc:slime_spawn_egg) | 55 |
| 2356 | water_bucket + MyGhastSpawnBlock (oreghast) | Items.spawn_egg (mc:ghast_spawn_egg) | 56 |
| 2359 | water_bucket + MyZombiePigmanSpawnBlock (orezombiepigman) | Items.spawn_egg (mc:zombified_piglin_spawn_egg) | 57 |
| 2362 | water_bucket + MyEndermanSpawnBlock (oreenderman) | Items.spawn_egg (mc:enderman_spawn_egg) | 58 |
| 2365 | water_bucket + MyCaveSpiderSpawnBlock (orecavespider) | Items.spawn_egg (mc:cave_spider_spawn_egg) | 59 |
| 2368 | water_bucket + MySilverfishSpawnBlock (oresilverfish) | Items.spawn_egg (mc:silverfish_spawn_egg) | 60 |
| 2371 | water_bucket + MyMagmaCubeSpawnBlock (oremagmacube) | Items.spawn_egg (mc:magma_cube_spawn_egg) | 62 |
| 2374 | water_bucket + MyWitchSpawnBlock (orewitch) | Items.spawn_egg (mc:witch_spawn_egg) | 66 |
| 2377 | water_bucket + MySheepSpawnBlock (oresheep) | Items.spawn_egg (mc:sheep_spawn_egg) | 91 |
| 2380 | water_bucket + MyWolfSpawnBlock (orewolf) | Items.spawn_egg (mc:wolf_spawn_egg) | 95 |
| 2383 | water_bucket + MyMooshroomSpawnBlock (oremooshroom) | Items.spawn_egg (mc:mooshroom_spawn_egg) | 96 |
| 2386 | water_bucket + MyOcelotSpawnBlock (oreocelot) | Items.spawn_egg (mc:ocelot_spawn_egg) | 98 |
| 2389 | water_bucket + MyBlazeSpawnBlock (oreblaze) | Items.spawn_egg (mc:blaze_spawn_egg) | 61 |
| 2392 | water_bucket + MyWitherSkeletonSpawnBlock (orewitherskeleton) | WitherSkeletonEgg (eggwitherskeleton) | 0 |
| 2395 | water_bucket + MyEnderDragonSpawnBlock (oreenderdragon) | EnderDragonEgg (eggenderdragon) | 0 |
| 2398 | water_bucket + MySnowGolemSpawnBlock (oresnowgolem) | SnowGolemEgg (eggsnowgolem) | 0 |
| 2401 | water_bucket + MyIronGolemSpawnBlock (oreirongolem) | IronGolemEgg (eggirongolem) | 0 |
| 2404 | water_bucket + MyWitherBossSpawnBlock (orewitherboss) | WitherBossEgg (eggwitherboss) | 64 |
| 2407 | water_bucket + MyGirlfriendSpawnBlock (oregirlfriend) | GirlfriendEgg (egggirlfriend) | 0 |
| 2410 | water_bucket + MyBoyfriendSpawnBlock (oreboyfriend) | BoyfriendEgg (eggboyfriend) | 0 |
| 2413 | water_bucket + MyRedCowSpawnBlock (oreredcow) | RedCowEgg (eggredcow) | 0 |
| 2416 | water_bucket + MyCrystalCowSpawnBlock (orecrystalcow) | CrystalCowEgg (eggcrystalcow) | 0 |
| 2419 | water_bucket + MyVillagerSpawnBlock (orevillager) | Items.spawn_egg (mc:villager_spawn_egg) | 120 |
| 2422 | water_bucket + MyGoldCowSpawnBlock (oregoldcow) | GoldCowEgg (egggoldcow) | 0 |
| 2425 | water_bucket + MyEnchantedCowSpawnBlock (oreenchantedcow) | EnchantedCowEgg (eggenchantedcow) | 0 |
| 2428 | water_bucket + MyMOTHRASpawnBlock (oremothra) | MOTHRAEgg (eggmothra) | 0 |
| 2431 | water_bucket + MyAloSpawnBlock (orealosaurus) | AloEgg (eggalosaurus) | 0 |
| 2434 | water_bucket + MyCryoSpawnBlock (orecryolophosaurus) | CryoEgg (eggcryolophosaurus) | 0 |
| 2437 | water_bucket + MyCamaSpawnBlock (orecamarasaurus) | CamaEgg (eggcamarasaurus) | 0 |
| 2440 | water_bucket + MyVeloSpawnBlock (orevelocityraptor) | VeloEgg (eggvelocityraptor) | 0 |
| 2443 | water_bucket + MyHydroSpawnBlock (orehydrolisc) | HydroEgg (egghydrolisc) | 0 |
| 2446 | water_bucket + MyBasilSpawnBlock (orebasilisc) | BasilEgg (eggbasilisc) | 0 |
| 2449 | water_bucket + MyDragonflySpawnBlock (oredragonfly) | DragonflyEgg (eggdragonfly) | 0 |
| 2452 | water_bucket + MyEmperorScorpionSpawnBlock (oreemperorscorpion) | EmperorScorpionEgg (eggemperorscorpion) | 0 |
| 2455 | water_bucket + MyScorpionSpawnBlock (orescorpion) | ScorpionEgg (eggscorpion) | 0 |
| 2458 | water_bucket + MyCaveFisherSpawnBlock (orecavefisher) | CaveFisherEgg (eggcavefisher) | 0 |
| 2461 | water_bucket + MySpyroSpawnBlock (orespyro) | SpyroEgg (eggspyro) | 0 |
| 2464 | water_bucket + MyBaryonyxSpawnBlock (orebaryonyx) | BaryonyxEgg (eggbaryonyx) | 0 |
| 2467 | water_bucket + MyGammaMetroidSpawnBlock (oregammametroid) | GammaMetroidEgg (egggammametroid) | 0 |
| 2470 | water_bucket + MyCockateilSpawnBlock (orecockateil) | CockateilEgg (eggcockateil) | 0 |
| 2473 | water_bucket + MyKyuubiSpawnBlock (orekyuubi) | KyuubiEgg (eggkyuubi) | 0 |
| 2476 | water_bucket + MyAlienSpawnBlock (orealien) | AlienEgg (eggalien) | 0 |
| 2479 | water_bucket + MyAttackSquidSpawnBlock (oreattacksquid) | AttackSquidEgg (eggattacksquid) | 0 |
| 2482 | water_bucket + MyWaterDragonSpawnBlock (orewaterdragon) | WaterDragonEgg (eggwaterdragon) | 0 |
| 2485 | water_bucket + MyKrakenSpawnBlock (orekraken) | KrakenEgg (eggkraken) | 0 |
| 2488 | water_bucket + MyLizardSpawnBlock (orelizard) | LizardEgg (egglizard) | 0 |
| 2491 | water_bucket + MyCephadromeSpawnBlock (orecephadrome) | CephadromeEgg (eggcephadrome) | 0 |
| 2494 | water_bucket + MyDragonSpawnBlock (oredragon) | DragonEgg (eggdragon) | 0 |
| 2497 | water_bucket + MyBeeSpawnBlock (orebee) | BeeEgg (eggbee) | 0 |
| 2500 | water_bucket + MyHorseSpawnBlock (orehorse) | Items.spawn_egg (mc:horse_spawn_egg) | 100 |
| 2503 | water_bucket + MyTrooperBugSpawnBlock (oretrooper) | TrooperBugEgg (eggtrooper) | 0 |
| 2506 | water_bucket + MySpitBugSpawnBlock (orespit) | SpitBugEgg (eggspit) | 0 |
| 2509 | water_bucket + MyStinkBugSpawnBlock (orestink) | StinkBugEgg (eggstink) | 0 |
| 2512 | water_bucket + MyOstrichSpawnBlock (oreostrich) | OstrichEgg (eggostrich) | 0 |
| 2515 | water_bucket + MyGazelleSpawnBlock (oregazelle) | GazelleEgg (egggazelle) | 0 |
| 2518 | water_bucket + MyChipmunkSpawnBlock (orechipmunk) | ChipmunkEgg (eggchipmunk) | 0 |
| 2521 | water_bucket + MyCreepingHorrorSpawnBlock (orecreepinghorror) | CreepingHorrorEgg (eggcreepinghorror) | 0 |
| 2524 | water_bucket + MyTerribleTerrorSpawnBlock (oreterribleterror) | TerribleTerrorEgg (eggterribleterror) | 0 |
| 2527 | water_bucket + MyCliffRacerSpawnBlock (orecliffracer) | CliffRacerEgg (eggcliffracer) | 0 |
| 2530 | water_bucket + MyTriffidSpawnBlock (oretriffid) | TriffidEgg (eggtriffid) | 0 |
| 2533 | water_bucket + MyPitchBlackSpawnBlock (orenightmare) | PitchBlackEgg (eggnightmare) | 0 |
| 2536 | water_bucket + MyLurkingTerrorSpawnBlock (orelurkingterror) | LurkingTerrorEgg (egglurkingterror) | 0 |
| 2539 | water_bucket + MyEnderKnightSpawnBlock (oreenderknight) | EnderKnightEgg (eggenderknight) | 0 |
| 2542 | water_bucket + MyEnderReaperSpawnBlock (oreenderreaper) | EnderReaperEgg (eggenderreaper) | 0 |
| 2548 | water_bucket + MyGodzillaSpawnBlock (oregodzilla) | GodzillaEgg (egggodzilla) | 0 |
| 2554 | water_bucket + MyTheKingSpawnBlock (oretheking) | TheKingEgg (eggtheking) | 0 |
| 2560 | water_bucket + MyTheQueenSpawnBlock (orethequeen) | TheQueenEgg (eggthequeen) | 0 |
| 2563 | water_bucket + MySmallWormSpawnBlock (oresmallworm) | SmallWormEgg (eggsmallworm) | 0 |
| 2566 | water_bucket + MyMediumWormSpawnBlock (oremediumworm) | MediumWormEgg (eggmediumworm) | 0 |
| 2569 | water_bucket + MyLargeWormSpawnBlock (orelargeworm) | LargeWormEgg (egglargeworm) | 0 |
| 2572 | water_bucket + MyCassowarySpawnBlock (orecassowary) | CassowaryEgg (eggcassowary) | 0 |
| 2575 | water_bucket + MyCloudSharkSpawnBlock (orecloudshark) | CloudSharkEgg (eggcloudshark) | 0 |
| 2578 | water_bucket + MyGoldFishSpawnBlock (oregoldfish) | GoldFishEgg (egggoldfish) | 0 |
| 2581 | water_bucket + MyLeafMonsterSpawnBlock (oreleafmonster) | LeafMonsterEgg (eggleafmonster) | 0 |
| 2584 | water_bucket + MyTshirtSpawnBlock (oretshirt) | TshirtEgg (eggtshirt) | 0 |
| 2587 | water_bucket + MyBeaverSpawnBlock (orebeaver) | BeaverEgg (eggbeaver) | 0 |
| 2590 | water_bucket + MyUrchinSpawnBlock (oreurchin) | UrchinEgg (eggurchin) | 0 |
| 2593 | water_bucket + MyFlounderSpawnBlock (oreflounder) | FlounderEgg (eggflounder) | 0 |
| 2596 | water_bucket + MySkateSpawnBlock (oreskate) | SkateEgg (eggskate) | 0 |
| 2599 | water_bucket + MyRotatorSpawnBlock (orerotator) | RotatorEgg (eggrotator) | 0 |
| 2602 | water_bucket + MyPeacockSpawnBlock (orepeacock) | PeacockEgg (eggpeacock) | 0 |
| 2605 | water_bucket + MyFairySpawnBlock (orefairy) | FairyEgg (eggfairy) | 0 |
| 2608 | water_bucket + MyDungeonBeastSpawnBlock (oredungeonbeast) | DungeonBeastEgg (eggdungeonbeast) | 0 |
| 2611 | water_bucket + MyVortexSpawnBlock (orevortex) | VortexEgg (eggvortex) | 0 |
| 2614 | water_bucket + MyRatSpawnBlock (orerat) | RatEgg (eggrat) | 0 |
| 2617 | water_bucket + MyWhaleSpawnBlock (orewhale) | WhaleEgg (eggwhale) | 0 |
| 2620 | water_bucket + MyIrukandjiSpawnBlock (oreirukandji) | IrukandjiEgg (eggirukandji) | 0 |
| 2623 | water_bucket + MyTRexSpawnBlock (oretrex) | TRexEgg (eggtrex) | 0 |
| 2626 | water_bucket + MyHerculesSpawnBlock (orehercules) | HerculesEgg (egghercules) | 0 |
| 2629 | water_bucket + MyMantisSpawnBlock (oremantis) | MantisEgg (eggmantis) | 0 |
| 2632 | water_bucket + MyStinkySpawnBlock (orestinky) | StinkyEgg (eggstinky) | 0 |
| 2635 | water_bucket + MyEasterBunnySpawnBlock (oreeasterbunny) | EasterBunnyEgg (eggeasterbunny) | 0 |
| 2638 | water_bucket + MyCriminalSpawnBlock (orecriminal) | CriminalEgg (eggcriminal) | 0 |
| 2641 | water_bucket + MyBrutalflySpawnBlock (orebrutalfly) | BrutalflyEgg (eggbrutalfly) | 0 |
| 2644 | water_bucket + MyNastysaurusSpawnBlock (orenastysaurus) | NastysaurusEgg (eggnastysaurus) | 0 |
| 2647 | water_bucket + MyPointysaurusSpawnBlock (orepointysaurus) | PointysaurusEgg (eggpointysaurus) | 0 |
| 2650 | water_bucket + MyCricketSpawnBlock (orecricket) | CricketEgg (eggcricket) | 0 |
| 2653 | water_bucket + MyFrogSpawnBlock (orefrog) | FrogEgg (eggfrog) | 0 |
| 2656 | water_bucket + MySpiderDriverSpawnBlock (orespiderdriver) | SpiderDriverEgg (eggspiderdriver) | 0 |
| 2659 | water_bucket + MyCrabSpawnBlock (orecrab) | CrabEgg (eggcrab) | 0 |
| 2662 | water_bucket + MyCaterKillerSpawnBlock (orecaterkiller) | CaterKillerEgg (eggcaterkiller) | 0 |
| 2665 | water_bucket + MyMolenoidSpawnBlock (oremolenoid) | MolenoidEgg (eggmolenoid) | 0 |
| 2668 | water_bucket + MySeaMonsterSpawnBlock (oreseamonster) | SeaMonsterEgg (eggseamonster) | 0 |
| 2671 | water_bucket + MySeaViperSpawnBlock (oreseaviper) | SeaViperEgg (eggseaviper) | 0 |
| 2674 | water_bucket + MyRubberDuckySpawnBlock (orerubberducky) | RubberDuckyEgg (eggrubberducky) | 0 |
| 2677 | water_bucket + MyHammerheadSpawnBlock (orehammerhead) | HammerheadEgg (egghammerhead) | 0 |
| 2680 | water_bucket + MyLeonSpawnBlock (oreleon) | LeonEgg (eggleon) | 0 |
| 2545 | 9× MyGodzillaPartSpawnBlock (oregodzillapart) | MyGodzillaSpawnBlock (oregodzilla) | 0 |
| 2551 | 9× MyTheKingPartSpawnBlock (orethekingpart) | MyTheKingSpawnBlock (oretheking) | 0 |
| 2557 | 9× MyTheQueenPartSpawnBlock (orethequeenpart) | MyTheQueenSpawnBlock (orethequeen) | 0 |

## Truhen-Loot (ChestGenHooks, OreSpawnMain.java:5051-5062)

Konstruktor `WeightedRandomChestContent(stack, min, max, weight)`, Reihenfolge am Bytecode belegt (siehe Semantik). Alle Stacks sind `new ItemStack(<Item>)`, also Meta 0.

| Zeile | ChestGenHooks | 1.21.1-Loot-Table | Item (Feld → id) | min | max | weight |
|---|---|---|---|---|---|---|
| 5052 | `dungeonChest` | `minecraft:chests/simple_dungeon` | MyRuby → orespawn:ruby | 1 | 1 | 3 |
| 5053 | `dungeonChest` | `minecraft:chests/simple_dungeon` | MyAmethyst → orespawn:amethyst | 1 | 1 | 3 |
| 5054 | `dungeonChest` | `minecraft:chests/simple_dungeon` | MyThunderStaff → orespawn:thunderstaff | 1 | 1 | 2 |
| 5056 | `pyramidJungleChest` | `minecraft:chests/jungle_temple` | MyRuby → orespawn:ruby | 1 | 1 | 3 |
| 5057 | `pyramidJungleChest` | `minecraft:chests/jungle_temple` | MyAmethyst → orespawn:amethyst | 1 | 1 | 3 |
| 5058 | `pyramidJungleChest` | `minecraft:chests/jungle_temple` | AntRobotKit → orespawn:antrobotkit | 1 | 1 | 3 |
| 5060 | `pyramidDesertyChest` | `minecraft:chests/desert_pyramid` | MyRuby → orespawn:ruby | 1 | 1 | 2 |
| 5061 | `pyramidDesertyChest` | `minecraft:chests/desert_pyramid` | MyAmethyst → orespawn:amethyst | 1 | 1 | 2 |
| 5062 | `pyramidDesertyChest` | `minecraft:chests/desert_pyramid` | SpiderRobotKit → orespawn:spiderrobotkit | 1 | 1 | 2 |

Bytecode-Gegenprobe: `calls_OreSpawnMain.txt:8608-8637` zeigt dieselben neun Tripel.

Port: In 1.7.10 wandern die Einträge in dieselbe gewichtete Liste wie die Vanilla-Beute der Kiste, die Chance hängt also von der Gewichtssumme dieser Liste ab. **Offen:** die Summen stehen in Forges `ChestGenHooks` (1.7.10), und dieses Jar liegt lokal nicht vor. Die 1.21.1-Tabellen sind anders geschnitten; Hauptpool laut Ressourcen-Jar:

| 1.21.1-Tabelle | Pool 0: rolls | Einträge | Gewichtssumme |
|---|---|---|---|
| `chests/simple_dungeon` | uniform 1-3 | 11 | 129 |
| `chests/jungle_temple` | uniform 2-6 | 12 | 88 |
| `chests/desert_pyramid` | uniform 2-4 | 15 | 232 |

Im Port ein Global Loot Modifier, der je Tabelle ein Element mit dem Originalgewicht und `set_count` 1 in den Hauptpool einfügt. Offen ist, ob `LootTableLoadEvent` in NeoForge 21.1 das Einfügen in einen bestehenden Pool erlaubt; am Jar zu prüfen. Das Gewicht bleibt eine Näherung und gehört als solche in den README. Welche Forge-Kategorie zu welcher 1.21.1-Tabelle gehört, folgt dem Namen (Dungeon, Dschungeltempel, Wüstentempel).

## Dispenser-Registrierungen (OreSpawnMain.java:5299-5434)

134 Aufrufe von `BlockDispenser.dispenseBehaviorRegistry.putObject`, 133 verschiedene Items. Verhaltensklassen: 115× `DispenserBehaviorOreSpawnEgg` (114 Eier, `LizardEgg` doppelt), 12× `MyDispenserBehaviorRock`, je 1× `MyDispenserBehaviorArrow`, `MyDispenserBehaviorWDCharge`, `MyDispenserBehaviorSunspotUrchin`, `MyDispenserBehaviorAcid`, `MyDispenserBehaviorIceball`, `MyDispenserBehaviorDeadIrukandji`, `MyDispenserBehaviorLaserball`. Alle 114 `ItemSpawnEgg`-Felder im Manifest (`kind: spawn_egg`) sind abgedeckt.

Kurzverhalten (vollständig gelesen, Details in den Batches der jeweiligen Klasse):

| Klasse | Basis | Verhalten |
|---|---|---|
| `DispenserBehaviorOreSpawnEgg` | `BehaviorDefaultDispenseItem` | Zielpunkt 2 Blöcke in Blickrichtung (`getFrontOffsetX/Z() * 2.0`, DispenserBehaviorOreSpawnEgg.java:13,15), Y = `getYInt() + 0.2f` (Zeile 14). Ruft `ItemSpawnEgg.spawn_something(ise.my_id, world, x, y, z)` mit auf `int` gekürzten Koordinaten (Zeile 19) und übernimmt einen Anzeigenamen als `CustomNameTag` (Zeile 20-22). Verbraucht 1 Item, auch wenn es kein `ItemSpawnEgg` ist (Zeile 24) |
| `MyDispenserBehaviorRock` | `BehaviorProjectileDispense` | `EntityThrownRock` an `func_149939_a`-Position, Richtung mit Y + 0.1f (MyDispenserBehaviorRock.java:17), `setRockType` nach Item: SmallRock 1, Rock 2, RedRock 3, GreenRock 4, BlueRock 5, PurpleRock 6, SpikeyRock 7, TNTRock 8, CrystalRedRock 9, CrystalGreenRock 10, CrystalBlueRock 11, CrystalTNTRock 12 (Zeile 19-54); verbraucht 1 (Zeile 56). Geschwindigkeit/Streuung aus `func_82500_b()` / `func_82498_a()` der Basisklasse, nicht überschrieben |
| `MyDispenserBehaviorArrow` | `BehaviorProjectileDispense` | `IrukandjiArrow`, `canBePickedUp = 1` (MyDispenserBehaviorArrow.java:11) |
| `MyDispenserBehaviorWDCharge` / `SunspotUrchin` / `Acid` / `Iceball` / `DeadIrukandji` / `Laserball` | `BehaviorProjectileDispense` | nur `getProjectileEntity`: `WaterBall` / `SunspotUrchin` / `Acid` / `IceBall` / `DeadIrukandji` / `LaserBall` an der Dispenser-Position (jeweils Zeile 10) |

Port: `DispenserBlock.registerBehavior(item, behavior)` in `FMLCommonSetupEvent#enqueueWork`, serverseitig, ohne Client-Code. Die Projektil-Varianten auf `ProjectileDispenseBehavior` bzw. eine eigene `DefaultDispenseItemBehavior`-Unterklasse. Die gekürzten `int`-Koordinaten des Ei-Verhaltens sind für 1:1 zu übernehmen.

| Zeile | Item (Feld → id) | Verhalten |
|---|---|---|
| 5415 | MyIrukandjiArrow → orespawn:irukandjiarrow | `MyDispenserBehaviorArrow` |
| 5416 | MyWaterBall → orespawn:waterball | `MyDispenserBehaviorWDCharge` |
| 5417 | MySunspotUrchin → orespawn:sunspoturchin | `MyDispenserBehaviorSunspotUrchin` |
| 5418 | MyAcid → orespawn:acid | `MyDispenserBehaviorAcid` |
| 5419 | MyIceBall → orespawn:iceball | `MyDispenserBehaviorIceball` |
| 5420 | MyIrukandji → orespawn:deadirukandji | `MyDispenserBehaviorDeadIrukandji` |
| 5421 | MyLaserBall → orespawn:laserball | `MyDispenserBehaviorLaserball` |
| 5422 | MySmallRock → orespawn:rocksmall | `MyDispenserBehaviorRock` |
| 5423 | MyRock → orespawn:rock | `MyDispenserBehaviorRock` |
| 5424 | MyRedRock → orespawn:rockred | `MyDispenserBehaviorRock` |
| 5425 | MyCrystalRedRock → orespawn:rockcrystalred | `MyDispenserBehaviorRock` |
| 5426 | MyCrystalGreenRock → orespawn:rockcrystalgreen | `MyDispenserBehaviorRock` |
| 5427 | MyCrystalBlueRock → orespawn:rockcrystalblue | `MyDispenserBehaviorRock` |
| 5428 | MyCrystalTNTRock → orespawn:rockcrystaltnt | `MyDispenserBehaviorRock` |
| 5429 | MyBlueRock → orespawn:rockblue | `MyDispenserBehaviorRock` |
| 5430 | MyGreenRock → orespawn:rockgreen | `MyDispenserBehaviorRock` |
| 5431 | MyPurpleRock → orespawn:rockpurple | `MyDispenserBehaviorRock` |
| 5432 | MySpikeyRock → orespawn:rockspikey | `MyDispenserBehaviorRock` |
| 5433 | MyTNTRock → orespawn:rocktnt | `MyDispenserBehaviorRock` |

Spawn-Eier mit `DispenserBehaviorOreSpawnEgg` (Feld → id, Zeile): LizardEgg (egglizard, 5300), WitherSkeletonEgg (eggwitherskeleton, 5301), EnderDragonEgg (eggenderdragon, 5302), SnowGolemEgg (eggsnowgolem, 5303), IronGolemEgg (eggirongolem, 5304), WitherBossEgg (eggwitherboss, 5305), GirlfriendEgg (egggirlfriend, 5306), BoyfriendEgg (eggboyfriend, 5307), TheKingEgg (eggtheking, 5308), TheQueenEgg (eggthequeen, 5309), ThePrinceEgg (eggtheprince, 5310), RedCowEgg (eggredcow, 5311), CrystalCowEgg (eggcrystalcow, 5312), GoldCowEgg (egggoldcow, 5313), EnchantedCowEgg (eggenchantedcow, 5314), MOTHRAEgg (eggmothra, 5315), AloEgg (eggalosaurus, 5316), CryoEgg (eggcryolophosaurus, 5317), CamaEgg (eggcamarasaurus, 5318), VeloEgg (eggvelocityraptor, 5319), HydroEgg (egghydrolisc, 5320), BasilEgg (eggbasilisc, 5321), DragonflyEgg (eggdragonfly, 5322), EmperorScorpionEgg (eggemperorscorpion, 5323), ScorpionEgg (eggscorpion, 5324), CaveFisherEgg (eggcavefisher, 5325), SpyroEgg (eggspyro, 5326), BaryonyxEgg (eggbaryonyx, 5327), GammaMetroidEgg (egggammametroid, 5328), CockateilEgg (eggcockateil, 5329), KyuubiEgg (eggkyuubi, 5330), AlienEgg (eggalien, 5331), AttackSquidEgg (eggattacksquid, 5332), WaterDragonEgg (eggwaterdragon, 5333), CephadromeEgg (eggcephadrome, 5334), DragonEgg (eggdragon, 5335), KrakenEgg (eggkraken, 5336), LizardEgg (egglizard, 5337), BeeEgg (eggbee, 5338), TrooperBugEgg (eggtrooper, 5339), SpitBugEgg (eggspit, 5340), StinkBugEgg (eggstink, 5341), OstrichEgg (eggostrich, 5342), GazelleEgg (egggazelle, 5343), ChipmunkEgg (eggchipmunk, 5344), CreepingHorrorEgg (eggcreepinghorror, 5345), TerribleTerrorEgg (eggterribleterror, 5346), CliffRacerEgg (eggcliffracer, 5347), TriffidEgg (eggtriffid, 5348), PitchBlackEgg (eggnightmare, 5349), LurkingTerrorEgg (egglurkingterror, 5350), GodzillaEgg (egggodzilla, 5351), SmallWormEgg (eggsmallworm, 5352), MediumWormEgg (eggmediumworm, 5353), LargeWormEgg (egglargeworm, 5354), CassowaryEgg (eggcassowary, 5355), CloudSharkEgg (eggcloudshark, 5356), GoldFishEgg (egggoldfish, 5357), LeafMonsterEgg (eggleafmonster, 5358), TshirtEgg (eggtshirt, 5359), EnderKnightEgg (eggenderknight, 5360), EnderReaperEgg (eggenderreaper, 5361), BeaverEgg (eggbeaver, 5362), RotatorEgg (eggrotator, 5363), VortexEgg (eggvortex, 5364), PeacockEgg (eggpeacock, 5365), FairyEgg (eggfairy, 5366), DungeonBeastEgg (eggdungeonbeast, 5367), RatEgg (eggrat, 5368), FlounderEgg (eggflounder, 5369), WhaleEgg (eggwhale, 5370), IrukandjiEgg (eggirukandji, 5371), SkateEgg (eggskate, 5372), UrchinEgg (eggurchin, 5373), Robot1Egg (eggrobot1, 5374), Robot2Egg (eggrobot2, 5375), Robot3Egg (eggrobot3, 5376), Robot4Egg (eggrobot4, 5377), GhostEgg (eggghost, 5378), GhostSkellyEgg (eggghostskelly, 5379), BrownAntEgg (eggbrownant, 5380), RedAntEgg (eggredant, 5381), RainbowAntEgg (eggrainbowant, 5382), UnstableAntEgg (eggunstableant, 5383), TermiteEgg (eggtermite, 5384), ButterflyEgg (eggbutterfly, 5385), MothEgg (eggmoth, 5386), MosquitoEgg (eggmosquito, 5387), FireflyEgg (eggfirefly, 5388), TRexEgg (eggtrex, 5389), HerculesEgg (egghercules, 5390), MantisEgg (eggmantis, 5391), StinkyEgg (eggstinky, 5392), Robot5Egg (eggrobot5, 5393), CoinEgg (eggcoin, 5394), EasterBunnyEgg (eggeasterbunny, 5395), MolenoidEgg (eggmolenoid, 5396), SeaMonsterEgg (eggseamonster, 5397), SeaViperEgg (eggseaviper, 5398), CaterKillerEgg (eggcaterkiller, 5399), LeonEgg (eggleon, 5400), HammerheadEgg (egghammerhead, 5401), RubberDuckyEgg (eggrubberducky, 5402), CriminalEgg (eggcriminal, 5403), BrutalflyEgg (eggbrutalfly, 5404), NastysaurusEgg (eggnastysaurus, 5405), PointysaurusEgg (eggpointysaurus, 5406), CricketEgg (eggcricket, 5407), ThePrincessEgg (eggtheprincess, 5408), FrogEgg (eggfrog, 5409), JefferyEgg (eggrobot6, 5410), AntRobotEgg (eggantrobot, 5411), SpiderRobotEgg (eggspiderrobot, 5412), SpiderDriverEgg (eggspiderdriver, 5413), CrabEgg (eggcrab, 5414).

## Abweichungen zur Recherche (Quelle gewinnt)

- 03-items.md:339-340 „reload S: empty SquidZooka + Ink Sac“ / „empty Ray Gun + Redstone Block“: Der Code nimmt die Waffe mit beliebigem Schaden (Meta 32767, OreSpawnMain.java:3183, OreSpawnMain.java:3186), nicht nur leer.
- 03-items.md:70 und 03-items.md:335 „Red Flower (rose)“: `Blocks.red_flower` im shaped-Rezept ist eine Wildcard, alle 9 Varianten passen (OreSpawnMain.java:2837-2839).
- 03-items.md:389 „roughly 150 distinct non-egg outputs“: der Code hat 192 verschiedene Ergebnis-Ids bei 209 verschiedenen Rezepten außerhalb des Trockenei-Blocks (Parser über recipes.json).
- 03-items.md:387 und 03-items.md:389 „≈119 / about 119“: exakt 119 (116 Wasser + 3 Teilblöcke).
- 03-items.md:342 und 03-items.md:383 „Kyanite“: das ist `CrystalStone` (Lang-Name „Kyanite“, manifest), keine eigene Zutat. Keine Abweichung, nur ein Namensunterschied.
- Sonst stimmen die Muster in 03-items.md Abschnitt 7 mit dem Code überein (Stichproben: Ultimate-Set, Nightmare Sword, Thunder Staff, Kraken Repellent, Hoverboard, Duct Tape, Critter Cage).

## Port-Aufwand dieser Batch

Die Rezepte sind datengetrieben und lassen sich generieren: aus `recipes.json` entstehen 1:1 `data/orespawn/recipe/*.json`, nach Normalisierung 328 Dateien, davon 16 Schmelzrezepte. Aufwand entsteht an vier Stellen: Config-Bedingung (Miner's Dream), Unbeschädigt-Bedingung für Waffen und Rüstung als Zutat, Loot-Modifier mit genäherten Gewichten, Dispenser-Verhalten. Kein Mixin nötig, alles läuft auf dem dedizierten Server.
