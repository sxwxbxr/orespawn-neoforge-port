# OreSpawn – Items (weapons, tools, armor, food, utility, materials, recipes)

Scope: final Minecraft 1.7.10 line of OreSpawn by TheyCallMeDanger. Mobs, dimensions/worldgen/blocks,
history/licensing and jar internals are covered by other reports and appear here only by name.

## How to read this file

**Sources and their weight** (full URLs in the "Sources" section):

| Key | What | Weight |
|---|---|---|
| `[SRC:…]` | Decompiled OreSpawn **1.7.10-20.2** source (`mcmod.info` says `1.7.10-20.2`), GitHub `history-craft/OreSpawn`, package `danger.orespawn`. Values are the **config defaults** hard-coded in `OreSpawnMain.java` plus the item classes. | Highest for numbers. It is a decompile, not an official release. The last public jar is 20.3 (archive.org), and differences between 20.2 and 20.3 are unknown. |
| `[OS:…]` | Official website orespawn.com (Weebly era, "© 2012-2021", "Website created by MeganLorraine"), read from a full static mirror on `shrekleaker.github.io/orespawn.com/`. The live www.orespawn.com is now a WordPress DangerZone site. | Author's own text; numbers are the in-game tooltip values. |
| `[FW:…]` | Fan wiki orespawnmod.fandom.com (read via MediaWiki API) | Low. Often stub pages, and some contain fan content. |
| `[BLOG:…]` | Author's dev blog posts on orespawn.com, via web.archive.org | Dates and when features were added |

The fan wiki at `orespawn.fandom.com` does not exist (HTTP 404). `betterorespawn.fandom.com` documents the **fan mod "Better OreSpawn"** and was deliberately not used as a source.

**Units (1.7.10 mechanics, needed to understand the numbers):**
- Attack values are the in-game tooltip "Attack Damage" in half-hearts. In 1.7.10 a sword shows `4 + material damage`, a pickaxe `2 + dmg`, an axe `3 + dmg` and a shovel `1 + dmg`; a hoe has no bonus. Every website attack value checked against config `damage` fits this formula; exceptions are listed under Conflicts.
- Armor piece durability in 1.7.10 is `material durability factor × {11,16,15,13}` (helmet/chest/legs/boots). Piece values in the tables are **computed** from `[SRC]` factors and marked "calc".
- Effect durations in code are in ticks (20 ticks = 1 s).
- Many classes call `setMaxDamage(...)` after the material constructor, which **overrides** the config `maxuses`. The column "effective durability" shows the class value.
- Built-in enchantments are added in `onCreated`/`onUpdate`. If they are ever removed, the item re-applies them on its own.

Config section `OreSpawnWEAPONS` (`<Material>_damage/_maxuses/_efficiency/_harvestlevel/_enchantability`, each clamped to ½×–2× of the default) and section `OreSpawnARMOR` both make all stats tunable in-game ([OS:orespawncfg-file], [SRC:OreSpawnMain.java:5633-5730]).

---

## 1. Weapons

### 1.1 Tool materials (config defaults) — [SRC:OreSpawnMain.java:1174-1188, 1292-1306]

| Config name | Enum name | harvestlevel | maxuses | efficiency | damage | enchantability | Used by |
|---|---|---|---|---|---|---|---|
| Ultimate | ULTIMATE | 10 | 3000 | 15 | 36 | 100 | Ultimate Sword/Pickaxe/Shovel/Hoe/Axe |
| Nightmare | NIGHTMARE | 3 | 1800 | 12 | 26 | 60 | Nightmare Sword |
| Bertha | BERTHA | 3 | 9000 | 15 | 496 | 100 | Big Bertha, Slice |
| Royal | ROYAL | 3 | 10000 | 15 | 746 | 150 | Royal Guardian Sword |
| Attitude | HAMMY | 5 | 2000 | 15 | 82 | 100 | Attitude Adjuster (internal name "Hammy") |
| BattleAxe | BATTLE | 3 | 1500 | 15 | 46 | 75 | Battle Axe |
| Chainsaw | CHAINSAW | 3 | 1500 | 10 | 56 | 75 | Chainsaw |
| QueenBattleAxe | QUEENBATTLE | 3 | 2200 | 15 | 662 | 100 | Queen Scale Battle Axe |
| Emerald | REALEMERALD | 3 | 1300 | 10 | 6 | 75 | Emerald tools and sword, Experience/Poison/Rat/Fairy/Rose Sword, Mantis Claw |
| Ruby | RUBY | 5 | 1500 | 11 | 16 | 85 | Ruby tools and sword |
| Amethyst | AMETHYST | 4 | 2000 | 11 | 11 | 70 | Amethyst tools and sword, Big Hammer |
| CrystalWood | CRYSTALWOOD | 2 | 300 | 3 | 2 | 15 | Crystal Wood tools |
| CrystalStone | CRYSTALSTONE | 3 | 800 | 6 | 5 | 45 | Kyanite tools |
| Pink | CRYSTALPINK | 4 | 1100 | 10 | 7 | 65 | Pink Tourmaline tools |
| TigersEye | TIGERSEYE | 4 | 1600 | 12 | 8 | 75 | Tiger's Eye tools |

Other weapon config: `UltimateSwordEnchantmentLevel` = 5 (range 1-10), `UltimateBowDamage` = 10 (range 2-20). `UltimateSwordPvp` = 0 and `BigBerthaPvp` = 0 mean these weapons do not hurt players, Girlfriends/Boyfriends or tamed pets ([SRC:OreSpawnMain.java:1150-1200], [OS:orespawncfg-file]).

### 1.2 Melee weapons

Names are the exact in-game English names from `LanguageRegistry.addNameForObject` [SRC:OreSpawnMain.java]. Website names differ in places (e.g. "SLYR Chainsaw").

| In-game name | Attack (tooltip) | Effective durability | Enchantability | Built-in enchantments | Special effect | Obtain | Sources |
|---|---|---|---|---|---|---|---|
| **The Ultimate Sword** | 40 | 3000 | 100 | Sharpness 5, Smite 5, Bane of Arthropods 5, Knockback 3, Looting 3, Unbreaking 3, Fire Aspect 2 (all derived from EnchantmentLevel 5) | No damage to players, GF/BF or tamed pets unless PvP config is set. Ingredient for Big Bertha Blade, Attitude Adjuster and Battle Axe | Craft; chest loot; random boss drop tables (Kraken, Mobzilla, Emperor Scorpion, CaterKiller) | [OS:ultimate-tools-and-weapons], [SRC:UltimateSword.java], [SRC:Kraken.java:230], [SRC:Godzilla.java:762] |
| **Big Bertha** | 500 | 9000 (class) | 100 | Knockback 5, Bane of Arthropods 1, Fire Aspect 1 | Every swing spawns an invisible `BerthaHit` projectile: 496 damage, 10 s fire and knockback to anything within 9 blocks (dist² < 81). Hence "extended reach"; per the fan wiki the ranged hit does not scale with Strength | Craft from Handle + Guard + Blade (shapeless) | [OS:big-bertha-sword], [SRC:Bertha.java], [SRC:BerthaHit.java:60-80], [FW:Big_Bertha_Sword] |
| **Slice** | 500 | 9000 | 100 | same as Big Bertha | Identical to Big Bertha, different model | Big Bertha + 1 Iron Ingot (shapeless) | [OS:slice-sword], [SRC:OreSpawnMain.java:2948] |
| **Royal Guardian Sword** | 750 | 9000 (class; config 10000 overridden) | 150 | Unbreaking 5 | Swing projectile: 746 damage + knockback within ~10 blocks (dist² < 101) | **Not craftable.** Always dropped by The King and by The Queen; chest in the Level-6 Challenge Dungeon | [OS:royal-guardian-sword], [SRC:TheKing.java:170], [SRC:TheQueen.java:179], [SRC:BerthaHit.java:84] |
| **Attitude Adjuster** | 86 | 9000 | 100 | none | Swing projectile within 8 blocks (dist² < 64): 82 damage + upward knockback + **fiery explosion** power 1.5 on hit, 2.1 otherwise (respects mobGriefing) | Craft 2× Ultimate Sword + Big Hammer + Green Goo (shapeless); rare Hammerhead drop (1/3) | [OS:attitude-adjuster], [SRC:BerthaHit.java:94-108], [SRC:Hammerhead.java:128], [FW:Attitude_Adjuster] |
| **Battle Axe** | 50 | 3000 (class `UltimateSword`) | 75 | Looting 3, Unbreaking 3 | none beyond damage | Craft Ultimate Sword + Ultimate Axe + Green Goo (shapeless); rare Leonopteryx drop (1/5) | [OS:battle-axe], [SRC:Leon.java:248], [FW:Battle_Axe] |
| **Chainsaw** (website: "SLYR Chainsaw") | 60 | 3000 | 75 | none | Left-clicking an entity deals 56 to **every visible living entity within 5 blocks**. Efficiency 10 on wood/plants; breaking a log or leaf removes and drops all matching tree blocks in an 11×16×11 box. Chainsaw sound and smoke/flame particles | Craft Ultimate Axe surrounded by 8 Redstone Blocks | [OS:chainsaw], [SRC:UltimateSword.java:117-273] |
| **Queen Scale Battle Axe** | 666 | 3000 | 100 | Same set as Ultimate Sword (Sharpness 5 …) | No swing projectile in code | Craft 4 Queen Scale + 3 Iron Ingot | [OS:queen-scale-battle-axe], [SRC:OreSpawnMain.java:2943] |
| **Nightmare Sword** | 30 per code (site says 20) | 1200 (class; config 1800) | 60 | Sharpness 1, Knockback 3, Fire Aspect 1 | Three-bladed model | Craft (see Recipes) | [OS:nightmare-sword], [SRC:NightmareSword.java] |
| **Emerald Sword** | 10 | 1300 | 75 | none | Ingredient for Experience and Poison Sword | Craft | [OS:emerald-tools-and-weapons] |
| **Rose Sword** | 10 | 1300 | 75 | none | Hitting a hostile Valentine's-Day Girlfriend: 25 % chance to calm her and drop 10-19 "Love", otherwise 1 Love | Craft 2 Red Flower (rose) + Stick. Not on the website | [SRC:OreSpawnMain.java:2837], [SRC:Girlfriend.java:1088-1108] |
| **Experience Sword** | 10+ | 1400 | 75 | Sharpness 2, Unbreaking 3 | +10 XP per hit on a mob; bonus damage = player level ÷ 2; while in inventory it makes worn Experience Armor give XP (see 3.3) | Craft Emerald Sword surrounded by 8 Bottle o' Enchanting; loot (Mobzilla, Kraken) | [OS:experience-sword], [SRC:ExperienceSword.java] |
| **Poison Sword** | 10 | 1300 | 75 | Sharpness 1 | Each hit applies Poison I, Wither I and Weakness I for 10-19 s each | Craft Emerald Sword surrounded by 8 Dead Stink Bug | [OS:poison-sword], [SRC:PoisonSword.java] |
| **Rat Sword** | 10 | 1300 | 75 | none | Each hit spawns 1-6 Rats owned by the attacker (config `RatPlayerFriendly`/`RatPetFriendly`) | Craft 2 Crystalized Rats + 1 Crystal Shards | [OS:rat-sword], [SRC:RatSword.java] |
| **Fairy Sword** | 10 | 1300 | 75 | none | Each hit spawns 1-3 Fairies that follow and protect the owner | Craft 2 Crystalized Fairies + 1 Crystal Shards | [OS:fairy-sword], [SRC:FairySword.java] |
| **Mantis Claw** | 10 | 1000 | 75 | none | Life steal: target −1 HP, attacker +1 HP (half a heart) per hit | Mantis drops 2 | [OS:mantis-claw], [SRC:MantisClaw.java], [SRC:Mantis.java:106] |
| **Big Hammer** | 15 | 9000 | 70 | none | Launches the target upward (up to 0.67 velocity), so fall damage adds up | Hercules Beetle drops 1 | [OS:big-hammer], [SRC:BigHammer.java], [SRC:HerculesBeetle.java:124] |
| **Ruby Sword** | 20 | 1500 | 85 | none | – | Craft | [OS:ruby-tools-and-weapons], [SRC:RubySword.java] |
| **Amethyst Sword** | 15 | 2000 | 70 | none | – | Craft | [OS:amethyst-tools-and-weapons], [SRC:AmethystSword.java] |
| **Crystal Wood Sword** | 6 | 300 (material) | 15 | none | – | Craft (Crystal Dimension) | [OS:wood-crystal-tools-and-weapons] |
| **Kyanite Sword** | 9 | 800 | 45 | none | – | Craft | [OS:kyanite-tools-and-weapons] |
| **Pink Tourmaline Sword** | 11 | 1100 | 65 | none | Matches the Girlfriend armor | Craft | [OS:pink-tourmaline-tools-and-weapons] |
| **Tiger's Eye Sword** | 12 | 1600 | 75 | none | – | Craft | [OS:tigers-eye-tools-and-weapons] |

### 1.3 Ranged and magic weapons

| In-game name | Damage | Durability / ammo | Enchant. | Built-in ench. | Behavior | Obtain | Sources |
|---|---|---|---|---|---|---|---|
| **The Ultimate Bow** | Arrow damage = ceil(speed × `UltimateBowDamage` 10); 25 % crit | 1000 | 50 | Power 5, Flame 3, Punch 2, Infinity 1 | **Fires on release with no draw time** (fixed velocity 3.0); burning arrows; arrows cannot be picked up. When PvP is off, arrows **heal** players/tamed mobs they hit ("Hit me Bro") | Craft; chests; boss loot tables | [OS:ultimate-tools-and-weapons], [SRC:UltimateBow.java], [SRC:UltimateArrow.java:188-220] |
| **Skate String Bow** (site: "Skate Bow") | Fires Irukandji Arrows, 100 per hit | 300 | 50 | none | Must be drawn like a vanilla bow (max power 1.75); consumes Irukandji Arrows; 5 % crit; missed arrows can be picked up | Craft | [OS:skate-bow--irukandji-arrows], [SRC:SkateBow.java] |
| **Irukandji Arrow** | 100 base (+ random bonus up to 52 in one branch; condition not verified) | stackable ammo | – | – | Ammo for the Skate String Bow only | Craft Peacock Feather + Dead Irukandji + Crystal Shards (shapeless) | [OS:skate-bow--irukandji-arrows], [SRC:IrukandjiArrow.java:188-218] |
| **A Freakin' Ray Gun!** | Laser charge, 16 damage + fire; explosive shots per website; **robots are immune** | 50 shots (stops at 1 left) | – | – | Strong recoil pushes the player | **Not craftable.** Robo-Warrior (always 1 in code; the website says "rare drop"), Giant Robot Spider random table, chests. **Reload:** empty Ray Gun + Redstone Block (shapeless) | [OS:ray-gun], [SRC:ItemRayGun.java], [SRC:Robot4.java:183], [SRC:LaserBall.java:95-173] |
| **Thunder Staff** | Thunderbolt 40 (20 as projectile + 20 as mob damage), fire, explosion power 3.0 and a real lightning strike; royalty (King/Queen/Prince) immune | 50 charges | – | – | Recharges 1 charge every 50 ticks **during a thunderstorm** | Craft 4 Ruby + 1 Diamond; chests (Basilisk Maze, Ruby Bird Dungeon, Nightmare dungeon, Cephadrome) | [OS:thunder-staff], [SRC:ItemThunderStaff.java], [SRC:ThunderBolt.java] |
| **SquidZooka!** | 0 itself; shoots an **Attack Squid** that fights nearby mobs and despawns after ~10 s; can blind the shooter (website) | 100 rounds | – | – | Small recoil | Craft 6 Iron Ingot + 3 Ink Sac (shapeless); chests. **Reload:** empty SquidZooka + Ink Sac | [OS:squidzooka], [SRC:ItemSquidZooka.java], [SRC:OreSpawnMain.java:2951,3187] |

### 1.4 Throwables (right-click to throw)

| In-game name | Website name | Damage | Effect | Obtain | Sources |
|---|---|---|---|---|---|
| Small Rock | Small Rock | 2 | – | World (desert, Extreme Hills); missed rocks can be picked up again, placed, skipped on water, and they break glass | [OS:rocks-and-crystals], [SRC:EntityThrownRock.java:83-240] |
| Big Rock | Rock | 5 | – | same | same |
| Flame Rock | Red Rock | 5 | Sets target on fire for 20 s | same | same |
| Poison Rock | Green Rock | 5 | Poison 5 s | same | same |
| Slowness Rock | Blue Rock | 10 | Slowness 5 s | same | same |
| Weakness Rock | Purple Rock | 20 | Weakness 5 s | same | same |
| Painful Rock | Spiky Rock | 40 | – | same | same |
| Explosive Rock | TNT Rock | 40 | Explosion power 2.1 | same | same |
| Flame Crystal | Red Crystal | 150 | Fire + Weakness | Crystal Dimension only | same |
| Poison Crystal | Green Crystal | 150 | Poison 10 s + Weakness 5 s | Crystal Dimension | same |
| Slowness Crystal | Blue Crystal | 150 | Slowness 10 s + Weakness 5 s | Crystal Dimension | same |
| Explosive Crystal | TNT Crystal | 250 | Explosion power 5.1 + Weakness | Crystal Dimension | same |
| Acid | Acid | 16 | – | Rare drop around Spit Bugs (website) | [OS:throwing-weapons], [SRC:Acid.java] (extends LaserBall) |
| Robot Laser Charge | Laser Charge | 16 + fire | Robots immune | Robots drop them in stacks of 4 (Robo-Gunner, Robo-Warrior, Robo-Sniper, Giant Robot) | [OS:throwing-weapons], [SRC:Robot3/4/5.java], [SRC:LaserBall.java] |
| WaterDragon Charge | Water Ball | 2/5 per website (not verified in code) | Extinguishes a burning friend; 1/10 chance to drop itself on hit | Around Water Dragons | [OS:throwing-weapons], [SRC:WaterBall.java:50-64] |
| Ice Ball | – | unknown | Can place ice blocks around the hit point (`icemaker`) | unknown (not on the website) | [SRC:IceBall.java] |
| Sunspot Urchin | Sunspot Urchin | 3 (6 vs creepers) | Sets target on fire for 5 s; never hurts players | Lava fishing with the Ultimate Fishing Rod | [OS:throwing-weapons], [SRC:SunspotUrchin.java:48-60] |
| Dead Irukandji | Dead Irukandji | 100 | – | Irukandji drop (touching a live one bare-handed is instant death) | [OS:throwing-weapons], [SRC:LaserBall.java:99] |
| Red Heels / Black Heels / Slippers / Boots | "Shoes" | 2 (+4 vs creepers) | 0 vs players; 1 vs GF/BF; 10 on Valentine's Day | Girlfriend drops; sifting water | [OS:girlfriendboyfriend-weapons], [SRC:Shoes.java:59-80] |
| Game Controller | – | 6 (+4 vs creepers) | same rules | Boyfriend drops | same |

Weapon counts: 23 melee + 6 ranged/magic + 23 throwables = **52**.

---

## 2. Tools

Harvest level, efficiency and enchantability come from the material table 1.1. Attack is the website tooltip value, consistent with the formula.

| Set | Pickaxe atk | Axe atk | Shovel atk | Hoe atk | Durability (effective) | Harvest lvl | Efficiency | Enchant. | Built-in / special | Sources |
|---|---|---|---|---|---|---|---|---|---|---|
| **Ultimate** | 38 | 39 | 37 | 1 | 3000 each | 10 (mines anything; the pickaxe returns `canHarvestBlock = true`) | 15 | 100 | Pickaxe: Efficiency 5 + Fortune 5; **iron ore 50 % chance of +1-2 Iron Ingots, gold ore 50 % chance of +1-2 Gold Ingots ("auto-smelt"), stone 1 % chance of a bonus gem** (Diamond, Emerald, Amethyst, Ruby, Uranium Nugget or Titanium Nugget, 1/10 each). Axe: Efficiency 5. Shovel: Efficiency 5. Hoe: Efficiency 2, **tills a 3×3 area (±1 height) straight to moist farmland**. No damage to players/GF/BF/tamed pets | [OS:ultimate-tools-and-weapons], [SRC:UltimatePickaxe.java], [SRC:UltimateAxe.java], [SRC:UltimateShovel.java], [SRC:UltimateHoe.java] |
| **The Ultimate Fishing Rod** | – | – | – | – | 3000 | – | – | – | Unbreaking 2; **fishes in lava** (and water). Loot pool includes Lava Eel, Fire Fish, Sun Fish, Spark Fish, Sunspot Urchin, the six OreSpawn fish and vanilla junk; weights unknown | [OS:ultimate-tools-and-weapons], [SRC:UltimateFishingRod.java], [SRC:UltimateFishHook.java] |
| **Emerald** | 8 | 9 | 7 | 1 | 1300 | 3 | 10 | 75 | **Pickaxe has Silk Touch** automatically (e.g. to mine ice for Dragons) | [OS:emerald-tools-and-weapons], [SRC:EmeraldPickaxe.java] |
| **Ruby** | 18 | 19 | 17 | 1 | 1500 | 5 | 11 | 85 | – | [OS:ruby-tools-and-weapons], [SRC:Ruby*.java] |
| **Amethyst** | 13 | 14 | 12 | 1 | 2000 | 4 | 11 | 70 | "Second only to the Ultimate stuff" (website) | [OS:amethyst-tools-and-weapons], [SRC:Amethyst*.java] |
| **Tiger's Eye** | 10 | 11 | 9 | 1 | 1600 (material) | 4 | 12 | 75 | – (Crystal Dimension) | [OS:tigers-eye-tools-and-weapons] |
| **Pink Tourmaline** | 9 | 10 | 8 | 1 | 1100 | 4 | 10 | 65 | – (Crystal Dimension) | [OS:pink-tourmaline-tools-and-weapons] |
| **Kyanite** (website: "Stone Crystal") | 7 | 8 | 6 | 1 | 800 | 3 | 6 | 45 | – | [OS:kyanite-tools-and-weapons] |
| **Crystal Wood** | 4 | 5 | 3 | 1 | 300 | 2 | 3 | 15 | – | [OS:wood-crystal-tools-and-weapons] |

Tool count: 8 sets × 4 + Ultimate Fishing Rod = **33**. Sifter, Wrench, Nether Tracker and other tool-like items are under Utility.

---

## 3. Armor

### 3.1 Armor sets — [SRC:OreSpawnMain.java:1160-1173, 1432-1445, 1446-1501], [SRC:ItemOreSpawnArmor.java]

`get_armorstats(dura, head, chest, leg, boots, enchant, resp, aqua, prot, fire, blast, proj, unbreak, feather)`. Respiration and Aqua Affinity go on the helmet only, Feather Falling on the boots only; all other enchantments go on every piece. The website "Defense" values match the code exactly for all 14 sets.

| Set (in-game names) | Defense H / C / L / B | Total | Durability factor → pieces (calc: ×11/16/15/13) | Enchant. | Built-in enchantments | Pieces take (standard armor shape) | Obtain | Sources |
|---|---|---|---|---|---|---|---|---|
| **Queen Scale** Helmet/Chestplate/Leggings/Boots | 9 / 16 / 14 / 9 | 48 | 1500 → 16500 / 24000 / 22500 / 19500 | 150 | **none** ("enchantment-free", website) | 5/8/7/4 The Queen Scale | Craft; The Queen drops 56 scales | [OS:queen-scale-armor], [SRC:TheQueen.java:182] |
| **Royal Guardian** Helmet/Chestplate/Leggings/Boots | 8 / 14 / 12 / 8 | 42 | 2000 → 22000 / 32000 / 30000 / 26000 | 200 | Protection 10, Fire Prot. 10, Blast Prot. 10, Projectile Prot. 10, Unbreaking 5; helmet Respiration 1, Aqua Affinity 2; boots Feather Falling 10 | **not craftable** | The King (full set + sword); Level-6 Challenge Dungeon chest | [OS:royal-guardian-armor], [SRC:TheKing.java:166-170], [FW:Royal_Guardian_Gear] |
| **Mobzilla Scale** | 7 / 13 / 11 / 7 | 38 | 1000 → 11000 / 16000 / 15000 / 13000 | 150 | Protection 10, Fire 10, Blast 10, Projectile 10, Unbreaking 5; boots Feather Falling 10 ("Protection X everything") | 5/8/7/4 Mobzilla Scale | Craft; Mobzilla drops 50-79 scales | [OS:mobzilla-armor], [SRC:Godzilla.java:749] |
| **The Ultimate** Helmet/Chestplate/Leggings/Boots | 6 / 12 / 10 / 6 | 34 | 200 → 2200 / 3200 / 3000 / 2600 | 100 | Protection 5, Fire 5, Blast 5, Projectile 5; helmet Respiration 2, **Aqua Affinity 3**; boots Feather Falling 3 | Titanium/Uranium/Iron Ingots (see Recipes) | Craft; chests; boss loot | [OS:ultimate-armor], [FW:Ultimate_Armor] |
| **Experience** | 5 / 9 / 7 / 4 | 25 | 70 → 770 / 1120 / 1050 / 910 | 50 | Protection 2, Blast Prot. 1; boots Feather Falling 1 | 1 **unused** Emerald piece + 8 Bottle o' Enchanting each | Craft | [OS:experience-armor], [FW:Experience_Armor] |
| **Ruby** | 4 / 9 / 8 / 4 | 25 | 90 → 990 / 1440 / 1350 / 1170 | 40 | none | 5/8/7/4 Ruby | Craft | [OS:ruby-armor] |
| **Amethyst** | 4 / 8 / 7 / 3 | 22 | 100 → 1100 / 1600 / 1500 / 1300 | 40 | none | 5/8/7/4 Amethyst | Craft | [OS:amethyst-armor] |
| **Tiger's Eye** | 4 / 8 / 7 / 4 | 23 | 80 → 880 / 1280 / 1200 / 1040 | 55 | none (see-through look) | 5/8/7/4 Tiger's Eye Ingot | Craft | [OS:tigers-eye-armor] |
| **Emerald** | 3 / 8 / 6 / 3 | 20 | 60 → 660 / 960 / 900 / 780 | 40 | none | 5/8/7/4 Emerald | Craft | [OS:emerald-armor], [FW:Emerald_Armor] |
| **Pink Tourmailine** (sic, in-game spelling) | 3 / 7 / 5 / 2 | 17 | 50 → 550 / 800 / 750 / 650 | 40 | none | 5/8/7/4 Pink Tourmaline Ingot | Craft | [OS:pink-tourmaline-armor] |
| **Lava Eel** | 2 / 7 / 5 / 2 | 16 | 40 → 440 / 640 / 600 / 520 | 35 | Protection 3, Fire Prot. 2, **Blast Prot. 10**; helmet Respiration 1, Aqua Affinity 2; boots Feather Falling 2 | 5/8/7/4 Lava Eel | Craft (Lava Eels come from lava fishing) | [OS:lava-eel-armor] |
| **Moth Scale** | 2 / 7 / 5 / 2 | 16 | 50 → 550 / 800 / 750 / 650 | 50 | Protection 3, Fire Prot. 3, Blast Prot. 3; boots Feather Falling 5 | 5/8/7/4 Moth Scale | Craft; Mothra drops 25 scales | [OS:moth-scale-armor], [SRC:Mothra.java:358] |
| **Lapis Lazuli** | 2 / 7 / 5 / 2 | 16 | 60 → 660 / 960 / 900 / 780 | 60 | Protection 1, Projectile Prot. 1; helmet Respiration 1, Aqua Affinity 1 | 5/8/7/4 **Lapis Lazuli Block** | Craft | [OS:lapis-armor] |
| **Peacock Feather** | 2 / 5 / 4 / 2 | 13 | 40 → 440 / 640 / 600 / 520 | 30 | boots Feather Falling 10 | 5/8/7/4 Peacock Feather | Craft | [OS:peacock-feather-armor] |

Armor pieces: 14 sets × 4 = **56**.

### 3.2 Set bonuses and special effects

OreSpawn has **no real full-set bonus**: every effect is tied to a single piece. Verified in code:

| Effect | Trigger | Exact behavior | Sources |
|---|---|---|---|
| **Glide** | Wearing **Royal Guardian Boots** (only if config `RoyalGlideEnable` = 1, the default) or **Peacock Feather Boots** | Fall speed capped at 0.1 blocks/tick and fall distance reset, so no fall damage | [SRC:ItemOreSpawnArmor.java:341-353], [OS:royal-guardian-armor], [OS:peacock-feather-armor] |
| **Slow fall (weaker)** | **Queen Scale Boots** (also gated by `RoyalGlideEnable`) | Fall speed capped at 0.25, no fall damage | [SRC:ItemOreSpawnArmor.java:354-361] |
| **XP trickle** | Experience armor pieces worn **and** Experience Sword anywhere in the inventory/hotbar | Every tick: 1/60 chance, then per worn piece +1 XP with chance helmet 1/10, chest 1/20, legs 1/30, boots 1/40, plus portal particles | [SRC:ExperienceSword.java onUpdate], [OS:experience-armor] |
| **Fire safety** | Lava Eel and Moth Scale | Built-in Fire/Blast Protection only; no potion effect | [OS:lava-eel-armor], [OS:moth-scale-armor] |
| **Companion armor** | Girlfriends wear only **Pink Tourmaline** or **Tiger's Eye**; Boyfriends only **Emerald**, **Amethyst** or **Ultimate** | Right-click the companion with a piece (empty hand removes it) | [OS:girlfriends--boyfriends], [SRC:Boyfriend.java:674] |

---

## 4. Food

`ItemFood(hunger, saturation)` from [SRC:OreSpawnMain.java:1371-1586]. Website values from [OS:food]. "Always edible" = `setAlwaysEdible()` in `ItemSunFish`.

| In-game name | Hunger (half-drumsticks) | Saturation mod. | Effects (code) | Recipe / source | Sources |
|---|---|---|---|---|---|
| Popcorn | 1 | 0.5 | – | Smelt Corn | [OS:food] |
| Buttered Popcorn | 2 | 0.6 | – | Popcorn + Butter | [OS:food] |
| Buttered and Salted Popcorn | 3 | 0.75 | – | Buttered Popcorn + Salt, or Popcorn + Salt + Butter | [OS:food] |
| Bag of Popcorn | 10 | 1.25 | – | 6 Buttered and Salted Popcorn + 3 Paper | [OS:food], [SRC:OreSpawnMain.java:2983] |
| Butter | 1 | 0.5 | – | 2 Milk Bucket → 4 Butter | [OS:food] |
| Butter Candy! | 4 | 0.5 | Speed I + Jump Boost I, 100 s; always edible | Butter + Sugar → 4 | [OS:food], [SRC:ItemSunFish.java] |
| Cheese | 4 | 0.5 | – | 4 Milk Bucket → 2 Cheese | [OS:food] |
| Raw Corn Dog | 4 | 0.6 | – | Corn + Raw Chicken + Raw Porkchop + Stick → 4 | [OS:food] |
| Corn Dog | **16** (site 14) | **2.5** (site 1.5) | – | Smelt Raw Corn Dog | [OS:food], [SRC:OreSpawnMain.java:1509] |
| Raw Bacon | 8 | 1.0 | – | Salt + Raw Porkchop → 2 | [OS:food] |
| Bacon! | 14 | 1.5 | Regeneration I + Strength I, 100 s; always edible | Smelt Raw Bacon | [OS:food], [SRC:ItemSunFish.java] |
| Raw Crab Meat | 4 | 0.25 | – | Crab drop | [OS:food], [SRC:Crab.java] |
| Crab Meat! | 6 | 0.75 (site 0.74) | always edible, no effect | Smelt Raw Crab Meat | [OS:food] |
| A Crabby Patty! | 16 | 2.35 | – (website: "makes you smarter", no effect in code) | Crab Meat + Lettuce + Tomato + Bread | [OS:food] |
| Garden Salad | 10 | 0.95 | – | Lettuce + Tomato + Radish + Carrot + Bowl | [OS:food] |
| BLT Sandwich! | 12 | 0.95 | – | Bacon + Lettuce + Tomato + Butter + Bread | [OS:food] |
| Pizza! | 4 per bite | 0.2 per bite | Placeable block, eaten like cake; stack size 1 | Tomato + Cheese + Bacon + Bread | [OS:food], [SRC:BlockPizza.java:90] |
| Raw Peacock | 6 | 0.7 | – | Peacock drop (1, +1 with 1/3 chance) | [OS:food], [SRC:Peacock.java:136] |
| Cooked Peacock | 12 | 1.4 | – (also tames Boyfriends) | Smelt Raw Peacock | [OS:food] |
| Strawberry | 2 | 0.65 | – | Strawberry plant | [OS:food] |
| Radish | 2 | 0.45 | – (plantable) | Plant | [OS:food] |
| Cherries | 3 | 0.45 | – | Cherry tree; → Cherry Pit | [OS:food] |
| Peach | 4 | 0.55 | – | Peach tree; → Peach Pit | [OS:food] |
| Crystal Apple | 5 | 0.85 | Regeneration I + Strength I, 150 s; always edible; breeding item | Crystal tree leaves; many passive mob drops | [OS:food], [OS:items] |
| Love | 8 | 0.95 | **Regeneration IV 300 s, Strength III 300 s, Fire Resistance III 300 s, Resistance II 300 s, Speed I 250 s, Jump Boost I 250 s**; always edible | Only from calmed Valentine's-Day Girlfriends (Rose Sword) | [SRC:ItemSunFish.java:34], [SRC:Girlfriend.java:1095-1106] |
| Rice | 5 | 0.65 | – (plant on Crystal Grass) | Crystal Dimension | [OS:food] |
| Corn | 6 | 0.75 | – (plantable; "Corn" is also the Battle Mobs multiplier) | Plains | [OS:food] |
| Quinoa | 7 | 0.85 | – (plant on Crystal Grass; green-team item) | Crystal Dimension | [OS:food] |
| Tomato | 4 | 0.55 | – | Plant | [OS:food] |
| Lettuce | 3 | 0.45 | – | Plant | [OS:food] |
| Fire Fish | 4 | 0.6 | Fire Resistance 60 s | Lava fishing | [OS:food], [SRC:ItemFireFish.java] |
| Sun Fish | **6** (site 5) | **0.6** (site 0.85) | Fire Resistance 300 s; always edible | Lava fishing | [OS:food], [SRC:OreSpawnMain.java:1372] |
| Spark Fish | 1 | 0.2 | Fire Resistance 5 s | Lava fishing | [OS:food] |
| Lava Eel | 2 | 0.6 | Fire Resistance 30 s | Lava fishing; also the armor material | [SRC:ItemLavaEel.java], [OS:items] |
| Green / Blue / Pink / Rock / Wood / Grey Fish | 3/0.5, 4/0.4, 4/0.6, 3/0.7, 5/0.7, 5/0.5 | – | – | Fishing with the Ultimate Fishing Rod or sifting water; smelt to vanilla Cooked Fish | [OS:food], [SRC:OreSpawnMain.java:1414-1419, 2771-2776] |

Food count: **40**. Salt is an ingredient, not food: smelt Salt Ore → 8 Salt [SRC:2755].

---

## 5. Utility and special items

**Dimension "keys": none exist.** Travel works by right-clicking a specific mob with an empty hand, and the same action returns you: Brown Ant → Utopia, Rainbow Ant → Village, Termite → Crystal, Red Ant → Mining, Unstable Ant → Danger, Butterfly → Chaos. Pets follow unless told to sit. The item side is only the ant nests (Ant Nest, Red/Rainbow/Unstable/Termite/Crystal Termite Nest) plus Salt blocks to fence ants in as a "telepad" ([OS:dimensions], [OS:items]).

**Taming/interaction items are vanilla items:** Red Rose tames Girlfriends (Dead Bush untames, Yellow Rose changes outfit, Ruby mutes, Amethyst unmutes, Diamond = stay). Cooked Beef or Cooked Peacock tames Boyfriends (Leather changes clothes). Raw beef tames Stinky (fan wiki). Snowball/Coal switches a pet Dragon's form. Feed a Cephadrome raw meat. Battle Mobs teams use Carrot (red), Potato (blue), Quinoa (green) and Corn to clone ([OS:girlfriends--boyfriends], [OS:things-to-do], [OS:dungeons], [OS:battle-mobs-gameplay], [FW:Stinky]).

| In-game name | Type | Behavior (code unless noted) | Recipe / obtain | Sources |
|---|---|---|---|---|
| **Empty Critter Cage** | Throwable capture | Thrown cage catches a mob (website: ~80 % success, rarely big mobs, never robots; **un-tames tamed pets**). A filled cage (one item per catchable mob, about 90 vanilla + OreSpawn species) is right-clicked on the ground to release the mob and returns the empty cage. Stack 16 | 4 Iron Ingot + 4 Stick → **2**; or 4 Pink Tourmaline Ingot + 4 Crystal Shards → 2 | [OS:items], [SRC:CritterCage.java], [SRC:OreSpawnMain.java:5011-5012] |
| **Extra Small / Small / Medium / Large / Extra Large Zoo Cage** (website: largest = "Humongous") | Instant structure | Builds a quartz floor/ceiling and glass-walled box centered on the player; outer width 5 / 7 / 11 / 15 / 19 | Iron Block + Glass + Quartz Block; each larger size = previous cage + the same three | [OS:items], [SRC:ZooCage.java], [SRC:OreSpawnMain.java:3002-3006] |
| **ZooKeeper Shard** | Single use | Left-click a living entity: it **never despawns**. Durability 1, consumed | **Only dropped by Nightmares** (2 + size + random count); chests | [OS:items], [SRC:ItemZooKeeper.java], [SRC:PitchBlack.java:572] |
| **Instant Survival Shelter** | Instant structure | 7×7 room: cobble floor, plank walls, glass band, plank roof, door gap. Places Furnace, Crafting Table and a Chest with Compass, Map, 8 Raw Porkchop, 32 Torch, 16 Coal, 2 Bed, Wooden Door, Iron Pickaxe/Sword/Axe, Bucket, 4 Salt Ore, 1 Chest. Stack 16 | Redstone Block + Stick + Cobblestone (shapeless) | [OS:items], [SRC:InstantShelter.java] |
| **Instant Survival Garden** | Instant structure | Clears 18 × 15 × 10 and lays farmland rows: Radish, Lettuce, Carrot, water, Potato, Wheat, Tomato, water, Corn, Strawberry, Sugar Cane (on sand), water, Melon stem. Stack 16 | 1.7.x: Redstone Block + Wheat + Gunpowder (1.6.4: Seeds instead of Wheat) | [OS:items], [SRC:InstantGarden.java] |
| **Miner's Dream** | Instant tunnel | Use on the block at your feet facing a straight direction: digs 64 long × 11 wide × 5 high, removing stone/dirt/gravel/water/lava/netherrack/end stone/Kyanite but **leaving ores**. Seals the ceiling with cobblestone (Kyanite in the Crystal Dimension); places an Extreme Torch every 5 blocks. Stack 16 | Cactus row / Redstone Block row / Gunpowder row (config `MinersDreamExpensive`=1 swaps Gunpowder for TNT) | [OS:items], [SRC:ItemMinersDream.java], [SRC:OreSpawnMain.java:3018-3024], [FW:Miners_dream] |
| **Stairs going Up / Stairs going Down / Insta-Bridge** (website: Stairs Up/Down/Across) | Instant build | Builds up to 32 cobblestone steps in the facing direction (8 directions), stopping at non-air; Extreme Torch every 8 steps. Stack 16 | 3 Cobblestone + 1 Gunpowder in three different shapes → 8 | [OS:items], [SRC:StepUp.java], [SRC:OreSpawnMain.java:3027-3031] |
| **Random Dungeon** | Dungeon spawner | Use on stone/cobble/grass/dirt at Y ≥ 40: places a Random Dungeon Spawner that builds any OreSpawn dungeon. Fortune 2 glint | Coal surrounded by 8 Redstone Blocks | [OS:dungeons], [SRC:ItemRandomDungeon.java], [SRC:OreSpawnMain.java:3017] |
| **OMG! No! Don't do it!!!** (website: "OMG Magic Apple") | Tree planter | Right-click grass: grows a giant multi-type tree with clouds, interior and loot. 50 % chance of the "ginormous" Gold/Emerald/Diamond-block tree if `GinormousEmeraldTreeEnable`=1. Fortune 2 glint | Apple surrounded by 8 Redstone Blocks; also Apple Tree leaves and chests | [OS:items], [OS:plants-and-trees], [SRC:ItemMagicApple.java], [SRC:OreSpawnMain.java:3015] |
| **Experience Orb Catcher** | Converter | Use on the block under an XP orb of value ≥ 3: 80 % chance to turn it into a **Bottle o' Enchanting** (returns string + stick). Stack 16. Needed for the Experience gear route | Glass Bottle + Stick + String | [OS:items], [SRC:ExperienceCatcher.java] |
| **Experience Tree Seed** | Sapling | Grows the Experience Tree (dance floor, XP) | Apple Tree Seed surrounded by 8 Bottle o' Enchanting | [OS:plants-and-trees], [SRC:OreSpawnMain.java:3056] |
| Apple Tree Seed / Cherry Pit / Peach Pit | Saplings | Plant on grass | Apple → 6 seeds; Cherries → 1 pit; Peach → 1 pit | [SRC:OreSpawnMain.java:3048-3052] |
| **Hoverboard** (internal item `elevator`) | Vehicle | Spawns the Hoverboard entity: W forward, S slow/reverse, Space boost, auto-hover, crosses lava | Row of 3 Planks over Diamond / Redstone / Diamond | [OS:items], [SRC:ItemElevator.java], [SRC:OreSpawnMain.java:5034] |
| **Nether Tracker** | Navigation | Durability 3000, Sharpness 2 glint. **While held in the Nether it turns the netherrack under your feet into Quartz Blocks**, leaving a trail home | Nether Star + Netherrack | [OS:items], [SRC:ItemNetherLost.java] |
| **Sifter** | Loot tool | Use on water (or below water), sand, gravel, dirt or grass; 600 uses. Water table (1/160 per entry): fish, OreSpawn fish, iron, gold nugget, the four shoes, Emerald/Ruby/Amethyst (1/3 each), Moth Scale, Uranium Nugget, Titanium Nugget, Diamond (1/2), redstone, coal, … Land tables (1/60 per entry): horse armor, saddle, iron armor (sand); flint, Salt, name tag, lead (gravel); etc. | 8 Stick around 1 String | [OS:items], [SRC:ItemSifter.java], [FW:Sifter] |
| **Wrench** | Robot taming | Left-click an unridden Spider Robot → **Spider Robot Kit**; Red Ant Robot below 50 % HP → tamed → **Red Ant Robot Kit** (damage stored in the kit). Durability 100, 2 per use | 4 Iron Ingot: `D D / _D_ / _D_` | [OS:items], [SRC:ItemWrench.java], [SRC:OreSpawnMain.java:2975] |
| **Spider Robot Kit / Red Ant Robot Kit** | Mount spawner | Spawns an owned rideable robot with the stored health; heal it with Iron Ingots (website) | Wrench on a robot; random Giant Robot drop; chests | [OS:spider-robot], [OS:red-ant-robot], [SRC:ItemSpiderRobotKit.java] |
| **Duct Tape!** | Repair block | Place it, then right-click with a damaged item: each click uses tape and restores up to 1/6 of the damage (website) | 3 Slimeball over 3 String | [OS:items], [SRC:OreSpawnMain.java:2990] |
| **Creeper Repellent** (block) | Repellent | Keeps Creepers away within ~10 blocks; also ants and termites (website); blocks the Queen's purple power (fan wiki) | 4 Green Goo + 2 String + 1 **Extreme Torch** (`D D / STS / D D`) | [OS:items], [SRC:OreSpawnMain.java:3042], [FW:Creeper_Repellent] |
| **Kraken Repellent** (block) | Repellent | Protects ~10×10 from the Kraken while fighting Attack Squid; also ants and termites | 4 Dead Stink Bug + 2 String + 1 Extreme Torch | [OS:items], [SRC:OreSpawnMain.java:3040] |
| **Extreme Torch** / Crystal Torch (blocks) | Light | Light level 1.0 (about 10 % brighter than a torch) / 0.99 | Redstone + Stick + Coal → 4, or Redstone + Torch → 1 / Crystal Energy + Crystal Shards → 6 | [OS:items], [SRC:OreSpawnMain.java:3033-3036] |
| **Creeper Launcher** | Consumable | Left-click a Creeper: +4.5 upward velocity, consumed | Paper + Redstone + Stick → **4**; Hammerhead drops 16 | [OS:items], [SRC:ItemCreeperLauncher.java], [SRC:Hammerhead.java:116] |
| **Eye-of-Ender Block** / Ender-Pearl Block | Summoning / storage | Eye-of-Ender Block + an Extreme Torch on top **summons a Cephadrome** | 9 Eye of Ender / 9 Ender Pearl (reversible) | [OS:materials], [SRC:OreSpawnMain.java:2970-2973] |
| **Ancient Dried … Spawn Egg** (≈120 blocks, plus 3 "… Spawn Egg Part") | Collectible | Mined at shallow depth (very common in the Mining Dimension). Craft + Water Bucket → the real spawn egg. 9 × Mobzilla/The King/The Queen Egg Part → the full dried egg | Worldgen | [OS:ancient-dried-eggs], [SRC:OreSpawnMain.java:2326-2680] |
| Island Block, Random Teleport Block, Red Ant Troll Block, Termite Troll Block | Blocks with effects | Plantable floating island; teleports players up to ~16 blocks; hidden red ants / termites. Details are in the blocks report | Worldgen | [OS:items], [OS:materials] |

Utility count: **34** item entries (Dried Eggs counted as one group).

---

## 6. Materials and drops

| In-game name | Use | Source (drop counts from code) | Sources |
|---|---|---|---|
| Uranium Nugget / Titanium Nugget | 9 → Uranium/Titanium Ingot | Smelt Uranium/Titanium Ore (1 nugget, 0.3 XP); Sifter; Ultimate Pickaxe bonus; drops from many mobs (Cave Fisher, Cephadrome, Cliff Racer, Coin, Cryolophosaurus, Dragonfly, Gold Fish, Kyuubi, Mantis, Scorpion, Spit Bug, Stinky, T. Rex, Vortex, Large Worm …); chests | [OS:materials], [SRC:OreSpawnMain.java:2751-2753] |
| Uranium Ingot / Titanium Ingot | All Ultimate gear | 9 nuggets; ↔ Uranium/Titanium Block | [OS:materials] |
| Ruby / Amethyst | Ruby and Amethyst gear, Thunder Staff; ↔ blocks | Ore drops (Ruby Ore sits under lava); Sifter; companion drops | [OS:materials] |
| Pink Tourmaline Ingot / Tiger's Eye Ingot | Crystal gear; ↔ blocks; Pink Tourmaline Ingot also makes a Bucket | Smelt Pink Tourmaline / Tiger's Eye ore (Crystal Dimension) | [SRC:OreSpawnMain.java:2763-2765, 2883] |
| Kyanite, Crystal Planks, Crystal Shards, Crystal Energy, Crystalized Rats, Crystalized Fairies | Crystal Dimension base materials (Crystal Shards = sticks) | Crystal Tree Wood → 4 Crystal Planks; 2 Crystal Planks → 6 Crystal Shards | [OS:materials] |
| Salt | Bacon, popcorn | Smelt Salt Ore → 8 | [OS:food] |
| **Green Goo** | Big Bertha Handle, Attitude Adjuster, Battle Axe, Creeper Repellent | Triffid drops 4-9 | [SRC:Triffid.java:189] |
| Dead Stink Bug | Poison Sword, Kraken Repellent | Stink Bug | [SRC:StinkBug.java] |
| Peacock Feather | Peacock armor, Irukandji Arrow, Crystal-Dimension bed | Peacock (50 %) | [SRC:Peacock.java:140] |
| Moth Scale | Moth Scale armor, Big Bertha Guard | Mothra drops 25; Sifter | [SRC:Mothra.java:358] |
| The Queen Scale | Queen armor, Queen Scale Battle Axe | The Queen drops 56 | [SRC:TheQueen.java:182] |
| Mobzilla Scale | Mobzilla armor; ↔ Mobzilla Scale Block (added V18) | Mobzilla drops 50-79 | [SRC:Godzilla.java:749], [BLOG:v18-sneak-peek] |
| Nightmare Scale | Nightmare Sword (4), Big Bertha Guard | Nightmare | [SRC:PitchBlack.java:570] |
| Kraken Tooth | Big Bertha Blade | The Kraken (1) | [SRC:Kraken.java:221] |
| Emperor Scorpion Scale, Basilisk Scale, Jumpy Bug Scale, Sea Monster Scale, Molenoid Nose | Big Bertha Guard | Emperor Scorpion, Basilisk, Jumpy Bug, Sea Monster, Molenoid | [OS:items] |
| Water Dragon Scale | Big Bertha Handle | Water Dragon | [OS:items] |
| Worm Tooth, TRex Tooth, CaterKiller Jaws, Sea Viper Tongue, Vortex Eye | Big Bertha Blade | Large Worm, T. Rex, CaterKiller, Sea Viper, Vortex | [OS:items] |
| **Big Bertha Handle** | Big Bertha | A Freakin' Ray Gun! + Big Hammer + Mantis Claw + Water Dragon Scale + Green Goo (shapeless) | [OS:big-bertha-sword], [SRC:OreSpawnMain.java:2945] |
| **Big Bertha Guard** | Big Bertha | Molenoid Nose + Sea Monster Scale + Moth Scale + Basilisk Scale + Nightmare Scale + Emperor Scorpion Scale + Jumpy Bug Scale (shapeless) | [SRC:OreSpawnMain.java:2946] |
| **Big Bertha Blade** | Big Bertha | Kraken Tooth + Worm Tooth + TRex Tooth + The Ultimate Sword + CaterKiller Jaws + Sea Viper Tongue + Vortex Eye (shapeless) | [SRC:OreSpawnMain.java:2947] |
| Lava Eel | Lava Eel armor, food | Lava fishing | [OS:items] |

All boss trophy parts can also be hung in item frames ("badge of honor"); bosses additionally drop an Item Frame ([OS:items], e.g. [SRC:Kraken.java:222]).

Material count: **32** item entries (storage blocks not counted).

---

## 7. Recipes

Notation: shaped rows in `/`-separated rows (`_` = empty); "S" = shapeless. Mirrored and column variants the code registers are listed once. Source: [SRC:OreSpawnMain.java:2739-5034]. Across the text-readable pages, every website recipe description agrees with the code except where listed under Conflicts.

**Ultimate** (T = Titanium Ingot, U = Uranium Ingot, I = Iron Ingot, S = String)

| Output | Pattern |
|---|---|
| The Ultimate Sword | `T / U / I` (column) |
| The Ultimate Pickaxe | `TUT / _U_ / _I_` |
| The Ultimate Shovel | `U / T / I` (column) |
| The Ultimate Axe | `TU_ / TI_ / _I_` |
| The Ultimate Hoe | `TU_ / _I_ / _I_` |
| The Ultimate Bow | `_TS / I_S / _US` |
| The Ultimate Fishing Rod | `__T / _US / I_S` |
| The Ultimate Helmet | `TIT / U_U` |
| The Ultimate Chestplate | `I_I / TTT / UUU` |
| The Ultimate Leggings | `III / T_T / U_U` |
| The Ultimate Boots | `T_T / U_U` |
| Uranium/Titanium Ingot | 9 nuggets (3×3); reverse S → 9 |
| Uranium/Titanium Block | 9 ingots; reverse S → 9 |

**Other weapons**

| Output | Recipe |
|---|---|
| Nightmare Sword | `ODO / RTR / OIO` (O = Nightmare Scale, D = Diamond, R = Redstone, T = Titanium Ingot, I = Iron Ingot) |
| Big Bertha | S: Big Bertha Handle + Guard + Blade (parts in section 6) |
| Slice | S: Big Bertha + Iron Ingot |
| Attitude Adjuster | S: 2 The Ultimate Sword + Big Hammer + Green Goo |
| Battle Axe | S: The Ultimate Sword + The Ultimate Axe + Green Goo |
| Chainsaw | `EEE / EIE / EEE` (I = The Ultimate Axe, E = Redstone Block) |
| Queen Scale Battle Axe | `EIE / EIE / _I_` (E = The Queen Scale, I = Iron Ingot) |
| Experience Sword | Emerald Sword surrounded by 8 Bottle o' Enchanting |
| Poison Sword | Emerald Sword surrounded by 8 Dead Stink Bug |
| Rat Sword / Fairy Sword | `E / E / I` (E = Crystalized Rats or Crystalized Fairies, I = Crystal Shards) |
| Rose Sword | `E / E / I` (E = Red Flower, I = Stick) |
| Skate String Bow | `_TS / T_S / _TS` (T = Crystal Shards, S = String) |
| Irukandji Arrow | S: Peacock Feather + Dead Irukandji + Crystal Shards |
| Thunder Staff | `DR_ / RR_ / __R` (D = Diamond, R = Ruby) |
| SquidZooka! | S: 6 Iron Ingot + 3 Ink Sac; reload S: empty SquidZooka + Ink Sac |
| A Freakin' Ray Gun! (reload only) | S: empty Ray Gun + Redstone Block |

**Generic tool/sword/armor sets.** Emerald, Ruby and Amethyst use a vanilla Stick handle; Crystal Wood, Kyanite, Pink Tourmaline and Tiger's Eye use Crystal Shards. Heads are Emerald / Ruby / Amethyst / Crystal Planks / Kyanite / Pink Tourmaline Ingot / Tiger's Eye Ingot.

| Output | Pattern (E = head material, I = handle) |
|---|---|
| Sword | `E / E / I` |
| Pickaxe | `EEE / _I_ / _I_` |
| Shovel | `E / I / I` |
| Axe | `EE_ / EI_ / _I_` |
| Hoe | `EE_ / _I_ / _I_` |

Armor for Emerald, Ruby, Amethyst, Pink Tourmaline Ingot, Tiger's Eye Ingot, Lava Eel, Moth Scale, Mobzilla Scale, Lapis Lazuli **Block**, The Queen Scale and Peacock Feather uses vanilla shapes: helmet `***/*_*`, chest `*_*/***/***`, legs `***/*_*/*_*`, boots `*_*/*_*`. Experience pieces: the matching Emerald piece surrounded by 8 Bottle o' Enchanting.

**Food:** see section 4. Smelting: Corn → Popcorn; Raw Corn Dog → Corn Dog (0.4 XP); Raw Bacon → Bacon!; Raw Peacock → Cooked Peacock; Raw Crab Meat → Crab Meat!; six OreSpawn fish → vanilla Cooked Fish; Salt Ore → 8 Salt; Uranium/Titanium Ore → nugget; Pink Tourmaline / Tiger's Eye ore → ingot.

**Utility**

| Output | Recipe |
|---|---|
| Empty Critter Cage ×2 | `IWI / W_W / IWI` (I = Iron Ingot, W = Stick; alt. I = Pink Tourmaline Ingot, W = Crystal Shards) |
| Zoo Cages | S chain: Iron Block + Glass + Quartz Block (+ previous cage) |
| Instant Survival Shelter | S: Redstone Block + Stick + Cobblestone |
| Instant Survival Garden | S: Redstone Block + Wheat + Gunpowder |
| Miner's Dream | `CCC / RRR / GGG` (C = Cactus, R = Redstone Block, G = Gunpowder or TNT if expensive) |
| Stairs going Up ×8 | `GC_ / _C_ / _C_` |
| Stairs going Down ×8 | `_C_ / _C_ / GC_` |
| Insta-Bridge ×8 | `_C_ / GC_ / _C_` (C = Cobblestone, G = Gunpowder) |
| OMG! No! Don't do it!!! | Apple surrounded by 8 Redstone Block |
| Random Dungeon | Coal surrounded by 8 Redstone Block |
| Creeper Launcher ×4 | S: Paper + Redstone + Stick |
| Nether Tracker | S: Nether Star + Netherrack |
| Sifter | Stick ring around String |
| Extreme Torch ×4 / ×1 | S: Redstone + Stick + Coal / S: Redstone + Torch |
| Crystal Torch ×6 | S: Crystal Energy + Crystal Shards |
| Kraken Repellent | `D_D / STS / D_D` (D = Dead Stink Bug, S = String, T = Extreme Torch) |
| Creeper Repellent | same shape, D = Green Goo |
| Experience Orb Catcher | S: Glass Bottle + Stick + String |
| Experience Tree Seed | Apple Tree Seed surrounded by 8 Bottle o' Enchanting |
| Apple Tree Seed ×6 / Cherry Pit / Peach Pit | S: Apple / Cherries / Peach |
| Hoverboard | `___ / WWW / DRD` (W = Planks, R = Redstone, D = Diamond) |
| Wrench | `D_D / _D_ / _D_` (Iron Ingot) |
| Duct Tape! | `___ / AAA / RRR` (A = Slimeball, R = String) |
| Crystal Planks ×4 / Crystal Workbench / Crystal Furnace | S: Crystal Tree Wood / S: 4 Crystal Planks / Kyanite ring |
| Crystal-Dimension Chest, Door, Bed, Bucket | Vanilla shapes with Crystal Planks; S: 3 Peacock Feather + 3 Crystal Planks → Bed; Pink Tourmaline Ingot bucket |
| Cobweb | `***/*_*/***` String |
| Sky Tree Wood / Duplicator Tree Wood | S → 4 Oak Planks |
| Dried spawn egg → spawn egg | S: Ancient Dried … Spawn Egg + Water Bucket (≈119 registrations) |

Recipe registrations in the 20.2 source: **381** lines (about 119 are egg rehydration); roughly **150 distinct non-egg outputs**.

---

## 8. Removed, old and version-dependent content

| Item / topic | What changed | Sources |
|---|---|---|
| Big Bertha recipe | Before **V16** (Apr 2014) it was a single craft from the boss drops; since V16 it needs Handle, Guard and Blade. The exact old recipe was not found | [OS:big-bertha-sword], [BLOG:whats-new-in-v16] |
| Instant Survival Garden | 1.6.4 build: Seeds + Gunpowder + Redstone Block; 1.7.x: Wheat instead of Seeds | [OS:items] |
| Mantis Claw, Big Hammer | Added 23 Jan 2014 (pre-V12, 1.6.4 era) | [BLOG:couple-new-weapons-coming-too] |
| Attitude Adjuster | Teased for V17 (8 May 2014, "attitude adjustment") | [BLOG:more-new-stuff-for-v17] |
| Rocks / Crystals | V18 (21 May 2014): rocks break glass, can be placed back and skipped on water, work in dispensers; "super-powerful Crystals" added; Mobzilla Scale Block added | [BLOG:v18-sneak-peek] |
| Spider Robot Kit / Red Ant Robot Kit / Wrench | Giant Robot Spider developed for V20 (Aug-Sep 2014) | [BLOG:v20-sneak-peek-what-the-heck-is-going-on], [BLOG:giant-robot-spider-update] |
| Rose Sword / Love / hostile Girlfriends | Valentine's Day event (first in V13, 14 Feb 2014); the code gates these to the `valentines_day` date | [OS:girlfriends--boyfriends], [BLOG:version-13-lag-free], [SRC:OreSpawnMain.java:4228] |
| Ray Gun drop | Website "rare drop from Robo-Warrior"; 20.2 code drops it every time (possibly changed in a later version) | [OS:ray-gun], [SRC:Robot4.java:183] |
| Fan wiki pages "Nether Armor", "Nether Sword" ("pending deletion"), "Lucky Block" | **Not in the 20.2 source**: fan content or another mod, do not port | [FW:Nether_Armor], [FW:Nether_Sword], [FW:Lucky_Block] |
| "OreSpawn 2.0" (2018) | A port inside the separate game DangerZone, not the Minecraft mod; not covered | [BLOG:dz-17-and-orespawn-20-released] |
| Pre-1.6.4 versions (1.2.5-1.5.2) | **Not researched / not found.** No item changelog for those versions was found online | – |

Items from the task list that **do not exist** under that name in 1.7.10-20.2: "Slice of Pie", "Hammy" (internal name of the Attitude Adjuster), "Slayer" (probably the website's "SLYR Chainsaw"), "Mining Pick", Lava Eel tools, Crystal tools other than Crystal Wood/Kyanite/Pink Tourmaline/Tiger's Eye, "Mocking Bird" armor, "Instant Tower/Castle", "Nightmare/Rainbow/Monster bugs" as items, "Elevator" as an item name (it is the Hoverboard), dimension key items.

---

## 9. Conflicts between sources

| # | Topic | Source A | Source B | Assessment |
|---|---|---|---|---|
| 1 | Attitude Adjuster damage | Website 86; code 82 + 4 = 86 | minecraft-mods.fandom snippet "60" (page not fetchable, HTTP 402) | 86 is correct for 20.x; 60 is possibly an old version |
| 2 | Nightmare Sword damage | Website 20 | Code 26 + 4 = 30 | Unresolved. Config default is 30 in 20.2 |
| 3 | Nightmare Sword durability | Class 1200 | Config maxuses 1800 | Class value wins in-game |
| 4 | Royal Guardian Sword durability | Fan wiki and class 9000 | Config 10000 | 9000 effective |
| 5 | Corn Dog | Website 14 / 1.5 | Code 16 / 2.5 | Unresolved |
| 6 | Sun Fish | Website 5 / 0.85 | Code 6 / 0.6 (effect 300 s agrees) | Unresolved |
| 7 | Crab Meat saturation | Website 0.74 | Code 0.75 | Rounding |
| 8 | Kraken Repellent recipe | Fan wiki 6 Dead Stink Bug + 2 String + Torch; website 4 + 2 String + Torch | Code 4 Dead Stink Bug + 2 String + **Extreme Torch** | Code |
| 9 | Creeper Repellent torch | Website "torch" | Code Extreme Torch | Code |
| 10 | Dried egg rehydration | Fan wiki: water bucket **and a stone block** | Website and code: water bucket only | Code |
| 11 | Ultimate Armor Aqua Affinity | Fan wiki level I | Code level 3 | Code |
| 12 | Slice/Bertha range | Fan wiki "10 blocks", website "almost ten blocks" | Code dist² < 81, i.e. 9 blocks | Code |
| 13 | Royal gear from The Queen | Fan wiki: Royal gear | Code: Queen drops only the **sword** (+ Prince egg, Princess, 56 scales) | Code |
| 14 | Queen Scale Battle Axe reach | minecraft-mods.fandom snippet "long reach" | No swing projectile in code | Code |
| 15 | Largest Zoo Cage name | Website "Humongous" | In-game "Extra Large Zoo Cage" | Code name |
| 16 | Ray Gun drop rate | Website "rare" | Code: always from Robo-Warrior | Version difference? |
| 17 | Experience Armor cost | Fan wiki "32 bottles + full Emerald set" | Code 8 per piece | Consistent (32 total) |

---

## 10. Open questions and gaps

1. **20.2 vs 20.3:** all code numbers come from the decompiled 20.2 source; the final 20.3 jar was not diffed (the jar-contents report should confirm).
2. **Water Ball (WaterDragon Charge) and Ice Ball damage** were not extracted; Ice Ball's source is unknown.
3. **Lava fishing and Sifter weights** (Ultimate Fishing Hook) are only partly documented; the Sifter's water table is in code but not transcribed in full.
4. **Dungeon chest loot tables** (GenericDungeon, BasiliskMaze, NightmareDungeon, RubyBirdDungeon) were not transcribed; only which items appear in them.
5. **Armor totals above 20** (Royal 42, Queen 48): 1.7.10 armor math vs a modern port (armor cap 30, toughness) needs a design decision; the vanilla effect of those totals was not verified.
6. **Ray Gun laser explosion conditions** and exact projectile damage (`LaserBall.java` special flag) were only skimmed.
7. **Irukandji Arrow bonus damage condition** (line 206) was not verified.
8. **Recipe images** on the website (`*_orig.jpg`) were not viewed; the text descriptions and code were used instead.
9. **Pre-1.6.4 item history** (1.2.5-1.5.2): no sources found.
10. The **Pizza** bite count per block was not extracted.

---

## Sources

- Official site mirror (orespawn.com, Weebly version, TheyCallMeDanger / MeganLorraine): `https://shrekleaker.github.io/orespawn.com/<page>.html`. Pages used (`[OS:page]`): items, food, materials, crafting-list, tools-and-weapons, armor, ultimate-tools-and-weapons, ultimate-armor, royal-guardian-sword, royal-guardian-armor, queen-scale-armor, queen-scale-battle-axe, big-bertha-sword, slice-sword, attitude-adjuster, battle-axe, chainsaw, big-hammer, mantis-claw, nightmare-sword, rat-sword, fairy-sword, poison-sword, experience-sword, experience-armor, emerald-armor, emerald-tools-and-weapons, ruby-armor, ruby-tools-and-weapons, amethyst-armor, amethyst-tools-and-weapons, tigers-eye-armor, tigers-eye-tools-and-weapons, pink-tourmaline-armor, pink-tourmaline-tools-and-weapons, kyanite-tools-and-weapons, wood-crystal-tools-and-weapons, lava-eel-armor, moth-scale-armor, peacock-feather-armor, mobzilla-armor, lapis-armor, ray-gun, thunder-staff, squidzooka, skate-bow--irukandji-arrows, throwing-weapons, rocks-and-crystals, girlfriendboyfriend-weapons, spider-robot, red-ant-robot, girlfriends--boyfriends, orespawncfg-file, plants-and-trees, dungeons, dimensions, things-to-do, battle-mobs-gameplay, ancient-dried-eggs. Original URLs are the same paths on `https://www.orespawn.com/`.
- Decompiled source 1.7.10-20.2 (`[SRC:…]`): https://github.com/history-craft/OreSpawn (path `src/main/java/danger/orespawn/`); identical mirrors: https://github.com/mglucas0123/OreSpawn-1.7.10, https://github.com/riesters/OpenOrespawn
- Final jar 1.7.10-20.3 (not analysed here): https://archive.org/details/orespawn-1.7.10-20.3_202109
- Fan wiki (`[FW:…]`): https://orespawnmod.fandom.com/wiki/ — pages Attitude_Adjuster, Battle_Axe, Big_Bertha_Sword, Creeper_Repellent, Critter_Cage, Dried_Spawn_Eggs, Emerald_Armor, Experience_Armor, Experience_orb_catcher, Extreme_Torch, Kraken_Repellent, Miners_dream, Mobzilla_Scale_Gear, Queen_Scale_Armor, Queen_Scale_Axe, Royal_Guardian_Gear, Salt_ore, Sifter, Slice, Stinky, Ultimate_Armor, Ultimate_Bow, Zookeeper_shard, Nether_Armor, Nether_Sword, Lucky_Block (read via `api.php`)
- Author blog via Wayback (`[BLOG:slug]`): `https://web.archive.org/web/2021/http://www.orespawn.com/home/<slug>` — couple-new-weapons-coming-too, version-12-is-out, version-13-lag-free, whats-new-in-v16, whats-next-for-v17, stuff-thats-already-new-in-v17, what-else-will-be-in-v17, more-new-stuff-for-v17, v17-almost-done, v18-sneak-peek, new-dungeons-for-v18, v19-development-preview, v19-development-release, v19-new-dimension, v20-sneak-peek-what-the-heck-is-going-on, giant-robot-spider-update, robot-update, dz-17-and-orespawn-20-released
- Search snippet only (page returned HTTP 402): https://minecraft-mods.fandom.com/wiki/OreSpawn and https://minecraft-mods.fandom.com/wiki/OreSpawn/Queen_Battle_Axe
