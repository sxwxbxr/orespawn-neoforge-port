# OreSpawn (TheyCallMeDanger) – Mobs, Creatures & Bosses

Research slice: every creature of OreSpawn, final version for MC 1.7.10 (last public build **1.7.10 v20.3**).
Compiled 2026-09-10. Web sources only; no jar was opened (that is another agent's slice).

## How to read this file

**Source tags** (full URLs in the source list at the end):

| Tag | Source | Reliability |
|---|---|---|
| **[OS]** | Official site www.orespawn.com (TheyCallMeDanger / MeganLorraine), read from the complete GitHub mirror `ShrekLeaker/orespawn.com` (snapshot Feb 2021, the site in its 1.7.10/1.12-dev state). Page slug given, e.g. `[OS the-king]` | **Primary.** Author's own statistics box (Attack / Defense / Health / Found / Drops). Some pages are copies from older versions (V11–V17), see "Removed/old-version" |
| **[CFG]** | Official page "OreSpawn.cfg File" | Primary, for config switches and internal mob names |
| **[FW]** | Fan wiki **orespawnmod.fandom.com** (the address `orespawn.fandom.com` from the brief returns HTTP 403/404; the wiki lives under `orespawnmod`). Raw wikitext pulled via the MediaWiki API | Secondary. Stats are often copied from [OS]; also contains vandalism and fan-invented mobs (see below) |
| **[NW]** | NamuWiki "Orespawn/Added mobs" (English machine translation of the Korean article, last modified 2026-08-07) | Secondary, detailed, some deviating numbers. Machine-translation artefacts ("Robo Spinner" = Robo Sniper, "Robo Founder" = Robo Pounder) |
| **[CC]** | Crazy Craft wiki (crazy-craft-wiki.fandom.com); OreSpawn is part of the Crazy Craft modpack | Secondary; the "Normal" values are used, never the "Crazy Craft 4.0" buffed values |
| **[CC2]** | Crazy Craft 2.0 wiki (crazycraft2.fandom.com) | Low; many stubs and joke pages |
| **[MM]** | Minecraft Mods wiki (minecraft-mods.fandom.com/wiki/OreSpawn + subpages) | Secondary |

**Stat convention in OreSpawn:** "Attack" = melee damage in half-hearts, "Defense" = armour value, "Health" = HP (half-hearts).
Per [CFG], every hostile mob has `<mob>_attack`, `<mob>_defense` and `<mob>_health` in `OreSpawn.cfg`:
- attack and health can move from 1/2 to 2x the default;
- defense can move ±4, never above 22 or below 0.

A defense value above 22 in a fan source is therefore suspicious.

**XP:** no written source gives an XP value for any OreSpawn mob. Treat XP as "unbekannt / not found" throughout; it is not repeated in each section.

**"not found"** = no source gives the value. Nothing in this file is estimated.

---

## 1. Overview table

HP / Dmg are from [OS] unless marked. `W:` = only from a fan wiki. Conflicts are detailed in the per-creature sections.

| # | Name (in-game) | Category | HP | Dmg | Spawn | Tame? | Drops |
|---|---|---|---|---|---|---|---|
| 1 | The King | Boss, flying 3-headed dragon | 7000 | 350 [OS] / 250 [FW,NW] + fireballs, lightning, ice | Utopia Dim. near Golden Ore Tree; King's Altar (egg); 9 Ancient Dried King Egg Parts | no | "Everything" (items of all installed mods), Prince egg, Royal Guardian set |
| 2 | The Queen | Boss, flying 3-headed dragon, neutral until angered | 6000 | 225 [OS] / 250 [FW] + fireballs, lightning, purple power orbs | Utopia Dim., Queen's Altar; 9 egg parts | no | Queen Scales, eggs (Princess), Royal Guardian Sword |
| 3 | Purple Power (Queen's orb) | Boss sub-entity (homing orb) | W: 1000 [MM,NW] | W: % of target HP, ignores armour (70 % [MM] / 80 % [NW]) | emitted by The Queen / Princess | – | – |
| 4 | The Ultimate King ("full power King") | Secret end boss | not found | not found | Young Adult Prince grows into it if `FullPowerKingEnable=1` | no (turns hostile) | not found |
| 5 | The Prince (toddler) | Pet boss, flying | 500 | 10 + fireballs, lightning, ice | Prince egg from The King or Level-6 Challenge Dungeon | auto-tamed | raw beef |
| 6 | The Young Prince | Pet boss, rideable | 1500 | 50 | grows from The Prince | yes (ride) | Prince spawn egg |
| 7 | The Young Adult Prince | Pet boss, rideable, huge | 3000 | 100 + fireballs, lightning, ice | grows from The Young Prince | yes (ride, sit) | Prince Egg |
| 8 | The Princess | Pet boss (toddler only) | 400 | 10 + fireballs, ice, lightning, power orbs | kill The Queen or beat the Queen's Challenge Tower | auto-tamed | beef |
| 9 | Mobzilla | Kaiju boss | 4000 | 100–175 + lightning, fireballs, explosions | Village (Rainbow Ant) Dim.; 9 Ancient Dried Mobzilla Egg Parts | no | "Ridiculous amounts", Mobzilla Scales |
| 10 | Kraken | Flying sea boss | 1000 | 40 + lightning + fall | chance when a player kills an Attack Squid; dried egg | no | "Lots of stuff", Kraken Tooth |
| 11 | Nightmare | Flying boss, 5 sizes | 125–1000 | 15–120 | Danger (Unstable Ant) Dim.; Nightmare Rookeries | no | Zookeeper items, Nightmare Scale |
| 12 | Hammerhead | Boss (Avatar beast) | 240 | 75 | Bottom of tallest Challenge Tower (Danger Dim.) | no | "Awesome loot" |
| 13 | Robo Jeffery ("Jeffery") | Giant robot boss, ~10–11 blocks | 550 | 40 + explosions, laser charges | Village Dim. | no | robot parts, robot kits, Ray Gun |
| 14 | Spider Robot (Giant Spider Robot) | Robot mount, huge | 1500 | jaws 100 / feet 10 | Village Dim., driven by a Spider Driver | wrench after killing the driver | robot stuff |
| 15 | Spider Driver | Hostile elite spider | "not a lot" [OS] / 16 [NW] | "a few" + poison | Village Dim. | no | spider things |
| 16 | Red Ant Robot | Robot mount | 300 | jaws 30 / feet 10 | Village Dim. | wrench below half HP, becomes a kit | robot stuff |
| 17 | Caterkiller | Boss, giant caterpillar | 450 | 30 | Forests, Jungles | no | Ruby items, Caterkiller Jaw |
| 18 | Brutalfly | Flying boss (metamorphosis) | 110 | 10 + fireballs | Forest/Jungle, from a Caterkiller | no | gold nuggets |
| 19 | Emperor Scorpion | Boss | 350 | 35 | Deserts | no | diamond items, Emperor Scorpion Scale |
| 20 | Scorpion | Hostile minion | 15 | 4 | Desert; summoned by Emperor Scorpion | no | gold/titanium/uranium nuggets |
| 21 | Hercules (Hercules Beetle) | Boss beetle | 250 | 30 | Forest/Jungle/Taiga Hills, Extreme Hills Edge | no | Big Hammer |
| 22 | Basilisc (Basilisk) | Boss snake | 200 | 24 + poison, slowness | Mining Dim. (Basilisc Lairs), Jungles | no | emerald items, Basilisc Scale |
| 23 | Jumpy Bug | Boss arachnid | 200 | 20 | Swamps | no | amethyst blocks/tools/gems, Jumpy Bug Scale |
| 24 | Spit Bug | Hostile (Jumpy Bug henchman) | 100 | 10 + acid | Swamps; summoned by Jumpy Bug | no | amethyst |
| 25 | Mantis | Boss insect | 120 | 16 | Overworld, Crystal Dim. | no | Mantis Claw |
| 26 | Vortex | Flying boss (Crystal Dim.) | 150 | 26 | Crystal (Termite) Dim. | no | ingots, Vortex Eye |
| 27 | Water Dragon | Aquatic boss | 150 | 20 + waterballs, fireballs | Rivers, lakes, oceans | raw fish | iron items, waterballs, Water Dragon Scale |
| 28 | Sea Viper | Aquatic boss | 160 | 22 + poison | Ocean | no | iron stuff, Sea Viper Tongue |
| 29 | Sea Monster | Aquatic boss | 110 | 14 | Ocean, Swamp | no | iron stuff, Sea Monster Scale |
| 30 | Mothra | Flying boss | 150 | 12 + fireballs, explosive fireballs | Extreme Hills (+ Mining Dim.) | no | Moth Scales |
| 31 | Molenoid | Boss mole | 200 | 18 + dirt | Plains | no | gold nuggets, Molenoid Nose |
| 32 | Triffid | Stationary plant boss | 100 | 20 | Round islands in Danger Dim.; Greenhouse | no | Green Goo |
| 33 | T-Rex | Dinosaur boss | 160 | 22 | Mining Dim. surface at night | no | uranium/titanium nuggets, T-Rex Tooth |
| 34 | Small Worm | Hostile (worm stage 1) | 10 | 5 | Plains | no | none |
| 35 | Medium Worm | Hostile (stage 2) | 30 | 10 | Plains | no | rotten flesh, leather |
| 36 | Boss Worm (Large/Giant Worm) | Boss (stage 3) | 90 | 18 | Plains, after the other stages | no | metal nuggets, Worm Tooth |
| 37 | Robo Warrior | Robot boss | 170 | 12 + lasers, explosives | Village Dim. at night | no | Ray Gun, electrical stuff |
| 38 | Alien | Hostile, very fast | 100 | 12 | Deep Mining Dim. caves; Alien/WTF? dungeons | no | map, compass, "other stuff" |
| 39 | WTF? | Hostile stone-eater | 100 | 10 | Caves | iron ingots | iron ingots, gold nuggets |
| 40 | Kyuubi | Hostile fire fox | 125 | 10 + fireball | Nether; Kyuubi Dungeon (Mining Dim.) | no | coal, redstone, quartz |
| 41 | Nastysaurus | Dinosaur boss | 200 | 32 | Mining Dim. | no | iron, string, leather, rotten flesh |
| 42 | Alosaurus | Dinosaur | 110 | 18 | Mining Dim. at night | no | beef, gold nuggets |
| 43 | Pointysaurus | Dinosaur | 80 | 10 | Mining Dim. | no | leather, beef, rotten flesh, string |
| 44 | Leonopteryx ("Leon") | Flying dragon boss | 250 | 55 | Nests in Mining Dim. | raw beef, rideable | chicken, feathers, Kraken Repellents, Battle Axe (rare) |
| 45 | Cephadrome | Flying mount boss | 300 | 70 (140 vs Kraken) | Ice Plains; summoned; Cephadrome Altars | ride after feeding (not tameable) | Ruby items |
| 46 | Crab | Aquatic, 3 sizes | 180 / 90 / 45 | 24 / 12 / 6 | Ocean (Overworld) | no | crab meat |
| 47 | Criminals (Politicians & Bankers) | Hostile thief | 100 | 1 + theft | Villages; "White House" dungeon | no | emeralds + stolen items |
| 48 | Robo Pounder | Robot | 200 | 22 + terrain destruction | Village Dim. at night | no | electrical stuff |
| 49 | Robo Gunner | Robot | 80 | 16 + laser | Village Dim. at night | no | electrical stuff |
| 50 | Robo Sniper | Robot | 20 | laser balls (rapid fire) | Village Dim. at night | no | laser balls, redstone items |
| 51 | Bomb-omb | Robot / creeper-like | 5 | small explosion | Village Dim. | no | gunpowder |
| 52 | Ender Knight | Hostile Ender mob | 60 | 12 | Overworld, Danger Dim.; Ender Knight Outposts | no | Eye of Ender, Ender Pearl |
| 53 | Ender Reaper | Hostile Ender mob | 90 | 18 | Overworld, Danger Dim.; Ender Reaper Graveyards | no | Eye of Ender |
| 54 | Bee | Flying hostile | 80 | 12 + poison | Jungles, Forests; Beehives | no | gold nuggets, Butter Candy |
| 55 | Dungeon Beast | Hostile dragon | 65 | 12 | Crystal Dim., deep underground | no | "stuff" |
| 56 | Rotator | Flying crystalline mob | 35 | 10 | Crystal Dim. | no | crystal ingots, energy |
| 57 | Crystal Urchin | Hostile (Vortex army) | 25 | fire / 10 | Crystal Dim. | no | ingots, crystal apples |
| 58 | Irukandji | Tiny aquatic jellyfish | 1 | 20 or instant death | Lakes in Crystal Dim. | no | dead Irukandji |
| 59 | Skate | Aquatic hostile | 8 | 8 | Lakes in Crystal Dim. | no | silk |
| 60 | Rat | Hostile rodent | 5 | 3 | Deep underground, Crystal Dim. | no (Rat Sword rats are allies) | rotten flesh |
| 61 | Terrible Terror | Flying hostile | 10 | 5 | Danger Dim. | no | emerald, feather, rotten flesh |
| 62 | Lurking Terror | Flying hostile (daytime) | 30 | 6 | Danger Dim. | no | beef, flint, feathers |
| 63 | Creeping Horror | Hostile, spawns in packs | 10 | 3 | Danger Dim. | no | rotten flesh, bone, silk |
| 64 | Cloud Shark | Flying shark | 15 | 6 | Danger Dim.; Cloud Shark Dungeons | no | paper, silk, bone |
| 65 | Attack Squid | Aquatic hostile | 10 | 8 + blindness | Water | no | rare gold items, ink sacs |
| 66 | Cave Fisher | Hostile cave spider-thing | 10 | 4 | Deep caves | no | gold/uranium/titanium nuggets |
| 67 | Leaf Monster | Ambush hostile | 6 | 2 | Forests, jungles; Leaf Monster Dungeons | no | wood, leaves, rotten flesh |
| 68 | Rubber Ducky | Neutral, respawns, "anger issues" | 5 | 6 | Rivers; Rubber Ducky Ponds | no | feather, chicken |
| 69 | Termite | Hostile ant (Crystal portal) | 5 | 2 | Termite nests (forest, jungle, plains) | no | none |
| 70 | Red Ant | Hostile ant (Mining portal) | 2 | 2 | Ant nests "almost everywhere" | no | nothing |
| 71 | Cryolophosaurus | Small hostile dinosaur | 10 | 3 | Mining Dim. | no | chicken, uranium/titanium nuggets |
| 72 | Vampire Butterfly | Flying hostile variant | 2 | 1 | Danger Dim.; W: Caterkiller death | no | nothing |
| 73 | Girlfriend | Companion | 80 | shoes or held item | Almost everywhere; Girlfriend/Boyfriend Island | red rose | roses, shoes |
| 74 | Boyfriend | Companion (off by default) | 80 | game controller or held item | Almost everywhere if enabled | cooked beef / cooked peacock | game controllers |
| 75 | Frog Royalty: Prince, Swamp Prince, Princess, Swamp Princess | Companion | not found | not found | Kiss a Frog | cooked beef or roses | not found |
| 76 | Dragon | Flying pet boss | 200 | 35 + fireballs | Danger Dim.; grows from Baby Dragon | raw beef (a lot), rideable | beef |
| 77 | Baby Dragon | Pet | 200 | 5 | Mining Dim. | raw beef | beef |
| 78 | Stinky | Pet mini-dragon, 19 morphs | 100 | 10 | Nether, Danger Dim. | raw beef | morph-specific "goodies" |
| 79 | Hydrolisc | Pet | 50 | 0 | Swamps | raw fish | fish |
| 80 | Camarasaurus | Pet dinosaur | 20 | 0 | Mining Dim. | red apple (cannot be untamed) | red flower |
| 81 | Velocity Raptor | Pet (speed buff), Battle Mob | 10 | 0 | Mining Dim. | red apple | red flowers |
| 82 | Ostrich | Mount, Battle Mob | 25 | 0 | Desert (daytime) | red apple (optional); ride untamed | roses, feathers |
| 83 | Chipmunk | Pet, Battle Mob | 5 | 0 | Forests | red apple | seeds |
| 84 | Gazelle | Pet | 15 | 0 | Plains | red apple | beef |
| 85 | Lizard | Temporary pet, Battle Mob | 30 | 6 | In water | ink sacs (temporary) | none |
| 86 | Baryonyx | Passive dinosaur | 40 | 0 | Mining Dim. | no | beef |
| 87 | Beaver | Passive, fells trees | 15 | 0 | Forests, Jungles | no | pork |
| 88 | Cassowary | Passive bird | 10 | 0 | Extreme Hills | no | chicken |
| 89 | Peacock | Passive bird | 15 | 0 | Crystal Dim. | no | peacock feather, meat |
| 90 | Whale | Passive aquatic | 100 | 0 | Lakes in Crystal Dim. | no | fish |
| 91 | Flounder | Passive fish | 5 | 0 | Crystal Dim. | no | fish |
| 92 | Apple Cow | Passive | 10 | 0 | Most places | no (breed: wheat) | beef, leather, apples |
| 93 | Golden Apple Cow | Passive | 10 | 0 | Most places | no | beef, leather, golden apples |
| 94 | Enchanted Golden Apple Cow | Passive | 10 | 0 | Most places | no | beef, leather, enchanted golden apples |
| 95 | Crystal (Apple) Cow | Passive | 10 (shared cow stat) | 0 | Crystal Dim. | no | crystal apples |
| 96 | Easter Bunny | Passive event mob | 10 | 0 | Plains/Forest/Forest Hills, only on Easter; dried egg | no | spawn eggs |
| 97 | Brown Ant | Portal critter (Utopia) | 1 | 0 | Ant nests | no | nothing |
| 98 | Rainbow Ant | Portal critter (Village) | 1 | 0 | Ant nests | no | nothing |
| 99 | Unstable Ant | Portal critter (Danger) | 1 | 0 | Ant nests | no | nothing |
| 100 | Butterfly | Ambient, portal to Chaos Dim. | 2 | 0 | Butterfly Plants (day), almost everywhere | no | nothing |
| 101 | Moth | Ambient | 2 | 0 | Moth Plants at night, near torches; Mothra's death | no | none |
| 102 | Firefly | Ambient light | 1 | 0 | Most places at night; Firefly plants | no | Extreme Torch |
| 103 | Dragonfly | Ambient predator | 10 | 0 | Swamps, lakes | no | gold/uranium/titanium nuggets |
| 104 | Mosquito | Ambient nuisance | 2 | 0 | Swamp, mosquito plants | no | none |
| 105 | Cricket | Ambient | 3 | 0 | Forest, Jungle, Plains | no | nothing |
| 106 | Stink Bug | Ambient, nausea | 5 | 0 | Forests, jungles | no | dead stink bug |
| 107 | Birds | Ambient flying | 2 | 0 | Almost everywhere | no | rare ruby |
| 108 | Frog | Passive | 8 | 3 | Rivers, Jungles, Swamps; Frog Pond | kiss → royalty | slimeballs |
| 109 | Ghost | Ambient | 2 | 0 | Taiga; haunted houses | no | none |
| 110 | Ghost Skeleton | Ambient | 5 | 0 | Taiga | no | none |
| 111 | Gold Fish | Flying fish | 6 | 0 | Danger Dim.; Gold Fish Bowls (ocean) | no | gold |
| 112 | Cliff Racer | Ambient flying | 5 | 0 | Danger Dim. | no | chicken, uranium/titanium nuggets |
| 113 | Fairy | Friendly (Crystal Dim.) | 40 | 3 | Crystal Dim.; Fairy Sword | no (sword fairies fight for you) | Crystal Torch |
| 114 | OreSpawn Coin | "Advertising" mob | 1 | 0 | Forests, Village Dim. | no | "great green stuff" |
| 115 | OreSpawn T-shirt | "Advertising" mob | 1 | not stated | Village Dim. | no | emeralds |

**Non-creature entity (not counted):** *Island* – an entity that moves the floating islands in the Danger Dimension; Triffids spawn on the round ones [OS technical-help, items].

---

## 2. Per-creature details

### A. Royal family & top-tier bosses

#### 1. The King
- **Category:** boss; three-headed flying dragon. [FW] describes 6 wings, no arms, 2 legs, a hammer-shaped tail, eyes blue/red/black. Two hitboxes: mid-section and heads [OS]. Size "extra large" [NW].
- **Stats [OS the-king]:** Attack 350 plus fireballs, lightning & ice; Defense 21; Health 7000.
  - Conflict: [FW King] and [NW] give **Attack 250**.
  - [NW] claims damage per hit against King/Queen/Mobzilla is capped at 120 (unverified).
- **Spawn:**
  - [OS]: "Utopia Dimension, near the Golden Ore Tree (The Goodness Tree)"; he "is there to protect his tree".
  - Egg from **9 Ancient Dried The King Egg Parts** [OS]; "The King's Altar" dungeon contains his spawn egg [OS dungeons].
  - [FW]: top of "The Tree of Goodness" in Utopia.
  - [MM King and Queen] says Red Ant dimension (contradicts [OS]; probably wrong).
- **Abilities [OS]:** lightning, fireballs, exploding ice, flying through things (walls). [FW]: rapid regeneration, huge knockback; does not flee at low health.
- **Drops [OS]:** "Everything" – the King can drop anything from any installed mod, plus The Prince (egg) and the Royal Guardian set (armour + sword).
- **Relations:**
  - Killing him is the "ONLY way" to get The Prince, Royal Guardian Armor and Sword, except conquering a Level 6 Challenge Dungeon [OS].
  - [OS] fight advice: strongest Mobzilla Armor, "a few Big Bertha Swords", stacks of Enchanted Golden Apples.
  - [NW]: The Prince/Princess do not attack King/Queen.
  - [FW]: The Queen follows The King and is not aggressive towards him.
- **Config:** `PlayNicely=1` scales The King down and makes him non-hostile [CFG].

#### 2. The Queen
- **Category:** boss; three-headed flying dragon.
  - [FW]: holds an energy orb; two forms – light blue with a green orb (neutral) and black with a purple orb (aggressive).
  - [FW, NW]: neutral until provoked.
- **Stats [OS the-queen]:** Attack 225 plus fireballs, lightning, purple power orbs; Defense 21; Health 6000.
  - [FW]: "attacks for 250 … It is set to 225, however … the same attack oversight [as The King] occurs" (fan explanation, unverified).
  - [FW]: regeneration 5–50 depending on health.
- **Spawn [OS]:** Utopia Dimension "in her Altar"; 9 Ancient Dried The Queen Egg Parts.
  - [FW]: top of "The Queen Tree" (a Tree of Goodness variant).
  - [NW]: "Utopia's Ruby Tree"; [MM]: "epic tree with amethyst and rubies".
- **Abilities [OS]:**
  - lightning, fireballs, flying through things;
  - **disables regeneration on adversaries**;
  - purple power orbs "that annihilate life";
  - restores barren terrain; spawns butterflies and birds, plants flowers, grows grass while flying.
  - "She only has ONE weak spot."
- **Drops [OS]:** Scales, eggs, and Royal Guardian Sword. [FW]: Royal Guardian gear, spawn eggs, Queen Scales (for Queen Scale Armor and Queen Battle Axe).
- **Relations:** kill her (or beat the Queen's Level-6 Challenge Tower) for **The Princess** [OS the-princess].
  - [FW/NW/MM]: Creeper Repellent stops or repels the Purple Power orbs (fan claim).
  - [NW]: Princes and Princesses do not attack her.

#### 3. Purple Power (the Queen's orbs)
- **Category:** homing projectile entity, shaped like a purple star [MM].
- **Stats:** HP 1000 [MM, NW]; [NW] says invincible.
  - Damage is a percentage of the target's HP that ignores armour: 70 % [MM], 80 % [NW]; [FW] "percentage of your health, ignores armour".
  - Conflicting; not on [OS].
- **Source of orbs:** The Queen; the Princess uses red/green/blue orbs [FW: fire/poison/weakness]. [FW]: the Ultimate King releases explosive versions.
- **Note:** [NW] says a Princess's orbs deal little damage to weak mobs but ~100 vs Mobzilla ("weak to the weak, strong to the strong"). Unverified.

#### 4. The Ultimate King (full-power King)
- **Category:** secret end boss / "easter egg".
- **How:** config `FullPowerKingEnable` (default 0; "1 = the full King's wrath. Backup your world first!") [CFG]. With it enabled, the Young Adult Prince "can make his final transformation into the true King" [OS young-adult-prince].
  - [FW]/[MM]: done by feeding diamond blocks.
- **Behaviour (all fan sources):**
  - [FW]: chat monologue ("Enough of this charade. I am done. … Now, you have 10 seconds to run. … Prepare to die!"), then becomes hostile.
  - [FW]: explosive orbs, lightning, fireballs, ice breath; physical attacks do not harm it.
  - [NW]: white aura; specs "slightly stronger than The Queen's"; completely invincible.
- **Stats / drops:** not found.

#### 5. The Prince (toddler)
- **Stats [OS the-prince]:** Attack 10 plus fireballs, lightning & ice; Defense 16 ([FW, NW] say 14); Health 500.
- **Obtain:** auto-tames on spawn. Egg from killing The King or from the Level 6 Challenge Dungeon. [FW] also mentions eggs from the Easter Bunny.
- **Behaviour [OS]:**
  - "four different attacks"; backs off below 1/4 health; auto-heals slowly;
  - feed almost any food to heal; Ice Block turns firepower off, Flint and Steel on;
  - does not sit well (flies back if you walk away); chases butterflies and birds; flies through walls.
- **Growth [OS]:** at least 25 mob kills where his bite is the finishing blow, at least 10 Minecraft days, healed with food at least 10 times → The Young Prince.
  - A diamond re-grows him if he was previously un-grown. [FW, NW] also claim a diamond block grows him (unverified).
- **Drops:** raw beef [OS].
- **[FW] bugs:** attacks Skeletons; won't attack Iron Golems.

#### 6. The Young Prince
- **Stats [OS young-prince]:** Attack 50; Defense 14; Health 1500. [FW]: regenerates 5 HP periodically; "45 to Iron Golem".
- **Riding [OS]:** SPACE up, W forward, S backward, A/D shoot. Diamond un-grows / re-grows. Ice block / flint & steel toggle ranged attacks. Still does not sit well.
- **Drops:** The Prince spawn egg [OS].

#### 7. The Young Adult Prince
- **Stats [OS young-adult-prince]:** Attack 100 plus fireballs, lightning & ice; Defense 20; Health 3000. "Finally, an indestructible pet!" [OS].
- **Behaviour:** ridable ("ginormous three-headed dragon"); sits (right-click with random item; tail stops); diamond un-grows to Teen Prince. With `FullPowerKingEnable` ≠ 0 it grows into the "true King" (see #4).
- **Drops:** Prince Egg [OS].

#### 8. The Princess
- **Stats [OS the-princess]:** Attack 10, fireballs, ice, lightning, power orbs; Defense 14; Health 400.
- **Obtain:** kill The Queen or take out the Queen's Tower Dungeon [OS]. "Currently the princess only has a toddler form!" [OS].
- **Behaviour [OS]:** Mom's energy orbs + Dad's lightning and ice; spawns plants, birds, butterflies; won't sit; heals over time and with food; runs away at low health. [NW]: stops fighting below 100 HP.
- **Drops:** Beef [OS].

#### 9. Mobzilla
- **Category:** kaiju boss (Godzilla parody); config internal name **`Godzilla`** [CFG].
- **Stats [OS mobzilla]:** Attack 100–175 / lightning / fireballs / explosives; Defense 21; Health 4000.
- **Size / hitboxes:** "two hit boxes, one around his feet, the other around his head" [OS]. [NW]: extra large.
- **Spawn [OS]:** Rainbow Ant Village World; egg from **9 Ancient Dried Mobzilla Egg Parts** (Mining Dimension is best to find them).
- **Abilities [OS]:** summons lightning strikes; tail swing destroys everything; jumps cause earthquakes and destruction; exploding fireballs; rapid regeneration.
  - [FW]: regenerates ~5 HP within 1–5 s; fireballs "known to reach bedrock".
  - [NW]: moves by jumping; grass he steps on becomes dirt.
- **Drops [OS]:** "Ridiculous amounts of stuff!!! Lots of Mobzilla Scales!" (→ Mobzilla Armor). [NW]: unlike King/Queen, no items from other mods.
- **Relations:**
  - [OS nightmares]: "Nightmares are the only mob known to be able to take down Mobzilla. Even then, it takes quite a few of them."
  - [OS mothra]: Mothra is "Mobzilla's nemesis".
  - [OS]: "Spawn him next to a village! Watch him flatten it!"
  - `PlayNicely` scales him down [CFG].

#### 10. Kraken
- **Category:** flying tentacle boss; appears on the OreSpawn logo [FW]. Spawn egg name **"Uh, no. Don't."** [OS kraken, FW].
- **Stats [OS kraken]:** Attack 40 / lightning / fall; Defense 10; Health 1000.
- **Spawn:** summoned by an Attack Squid killed by a player (chance) [OS attack-squid]; Kraken dried spawn eggs [FW]. "Play Pools" dungeons in oceans "are only fun until someone accidentally summons a Kraken" [OS dungeons].
- **Abilities [OS]:**
  - flies; lifts up anything living and drops it;
  - lightning attacks; makes rain ([NW]: snow in snowy biomes);
  - retreats at low health and "call[s] in reinforcements" if it gets away;
  - wanders off when left alone.
  - [FW]: no hitbox on the tentacles; reinforcement swarms of "40–50" (fan claim).
- **Counters:**
  - Cephadrome (140 damage vs Kraken) or Dragon [OS].
  - **Kraken Repellent** (4 dead stink bugs, 2 string, 1 torch; protects ~10×10 blocks, also repels ants/termites) [OS kraken, items].
  - Lizards kill Attack Squids for you [OS lizard].
- **Drops:** "Lots of stuff!!!" [OS]; Kraken Tooth (Big Bertha Blade) [OS items]; [FW] ink sacs, enchanted armour; [NW] staffs, Kraken Repellents.
- **Config:** `PlayNicely` scales Kraken down [CFG].

#### 11. Nightmare
- **Category:** flying boss in five sizes: "medium, big, humungous, Holy Sh**!!!, and @^%$…" [OS]. [FW]: black wyvern-like; based on Pitch Black's bioraptors.
- **Stats [OS nightmares]:** Attack 15–120 depending on size; Defense 11–18; Health 125–1000.
  - Per size [FW]: HP 125/250/500/750/1000; Attack 15/30/60/90/120; Defense 11/12/14/16/18.
  - [NW]: HP 1000/750/500/250/125 with Attack 120/75/50/30/15 and Defense 18/16/14/12/10 – the middle attack/defense values conflict with [FW].
  - Config `NightmareSize` 0 = random, 1–5 fixed [CFG].
- **Spawn:** Unstable Ant Danger Dimension [OS]; Nightmare Rookeries [OS dungeons].
- **Abilities [OS]:** killing players, Dragons, Ender Dragons, "killing things in general"; fast and hard to hit. First major boss TheyCallMeDanger modelled himself [OS].
- **Drops:**
  - [OS]: **Zookeeper items**, the only source ("Only dropped by Nightmares" [OS items]).
  - Nightmare Scale: 4 for Nightmare Sword, 1 for Big Bertha Guard [OS items].
  - [FW]: also feathers, item frames, raw beef, rotten flesh.
- **Relations:** only mob able to take down Mobzilla [OS]. [FW]: can defeat Hammerhead.

#### 12. Hammerhead
- **Category:** boss; model from the "Avatar mod" beasts [FW; see conflict note].
- **Stats [OS hammerhead]:** Attack 75; Defense 20; Health 240.
- **Spawn [OS]:** does not spawn in the wild. "Official guardians of The Prince Egg and the Royal Guardian Set in the bottom level of the tallest Challenge Tower in the Danger Dimension."
  - [CC]: also at night in the Chaos Dimension; [NW]: Challenge Towers, Plains, Chaos.
- **Abilities:** smashes mobs and players, extreme knockback [OS, FW].
  - [FW]: not resistant to lightning/fire/lava, no regeneration, no damage cooldown.
  - [FW]: old glitch of hitting through the roof in the Challenge Tower, fixed.
- **Drops:** "Awesome loot!!!" [OS]; [CC] Attitude Adjuster, Bottle o' Enchanting, Creeper Launcher, Experience Orb Catcher, Experience Tree Seed, raw beef.
- **Conflict:** [CC2] calls it tameable and rideable (Attack 70) – no official support.
- **Version:** "Appeared in V17" [OS].

#### 13. Robo Jeffery ("Jeffery")
- **Stats [OS robo-jeffery]:** Attack 40, explosions, laser charges; Defense 18 ([FW] "around 16"); Health 550.
- **Size:** "HUGE! Stands about 10 or 11 blocks tall" [OS]; walks almost like a human.
- **Spawn:** Rainbow Ant Dimension [OS]; at night [FW].
- **Abilities:** punches with big knockback; shoots; "exterminating anything not robotic" [OS]. [FW]: gun that causes burning.
- **Drops [OS]:** robot parts, robot kits, Ray Gun. [FW]: redstone, iron, Red Ant Robot Kit, maybe Spider Robot Kit, "a lot of" ray guns.
- **Note [OS robo-*]:** Ray Guns and laser charges are ineffective on robots.

#### 14. Spider Robot (Giant Spider Robot)
- **Stats [OS spider-robot]:** Attack jaws 100, feet 10; Defense 16; Health 1500. "It's HUGE. And I mean HUGE!!!!!"
- **Spawn:** Rainbow Ant Dimension, driven by a Spider Driver [OS spider-driver]. Non-aggressive when not controlled [NW].
- **Taming / riding [OS]:** kill the driver, then hit the robot with a **wrench** to pack it into a kit; heal with iron ingots; ride. Steps on anything nearby, including pets.
- **Drops:** "Robot stuff" [OS]; [NW] redstone items, Giant Spider Robot Kit, laser bullets.
- **Note:** Critter Cages do not work on robots [OS items].

#### 15. Spider Driver
- **Stats [OS spider-driver]:** Attack "A few, and poison"; Defense "20/8"; Health "not a lot". [NW]: Attack 3 + poison, Defense 5, HP 16, very fast. Conflicting.
- **Spawn:** Rainbow Ant Dimension [OS].
- **Behaviour:** "Elite Guard" of the spiders; drives Giant Spider Robots [OS].
- **Drops:** "spider things" [OS].

#### 16. Red Ant Robot
- **Stats [OS red-ant-robot]:** Attack jaws 30, feet 10; Defense 16; Health 300.
- **Spawn:** Rainbow Ant Dimension [OS]. Lore: revenge of the Red Ants for all the ants you killed.
- **Taming [OS]:** beat it below half health, hit with a wrench → Ant Robot Kit; spawn it and ride; heal with iron ingots. Tamed, it only attacks while ridden.
- **Drops:** "Robot stuff" [OS].

### B. The Big Bertha bosses (their drops craft the Big Bertha sword)

#### 17. Caterkiller
- **Stats [OS caterkiller]:** Attack 30; Defense 18; Health 450. "He can give the Emperor Scorpion a good whuppin'!"
- **Spawn:** Forests and Jungles [OS]. [FW, CC]: bottom of the Queen Challenge Dungeon.
- **Abilities [OS]:** decimates plant life, eats fast; regenerates very fast around trees and leaves; spits webs; "Don't take too long, or he will mutate!" (→ Brutalfly).
  - [CC]: dies in a burst of butterflies, including Vampire Butterflies.
- **Drops:** Ruby things and Caterkiller Jaw (Big Bertha Blade) [OS].
- **Config:** `PlayNicely` scales Caterkiller down [CFG].
- **Version:** "Added in V16" [OS].

#### 18. Brutalfly
- **Stats [OS brutalfly]:** Attack 10 plus fireballs; Defense 6; Health 110.
- **Spawn:** metamorphosis of a Caterkiller that took too long to kill; Forest and Jungle [OS]. [CC]: Queen Challenge Dungeon.
- **Abilities:** "Fireballs galore!" [OS]; [CC] only small fireballs, dies into butterflies.
- **Drops:** gold nuggets [OS].

#### 19. Emperor Scorpion
- **Category:** boss; site mascot ([NW]: mascot with the Kraken).
- **Stats [OS emperor-scorpion]:** Attack 35; Defense 20; Health 350.
- **Spawn:** deserts [OS].
  - [FW]: deserts and savannas at night; [CC]: more common in the Danger Dimension (claim).
  - Bottom layer of King and Queen Challenge Dungeons [CC].
- **Abilities [OS]:** spawns regular scorpions as minions; "ridiculous amount of knockback".
  - [FW]: poison 5 s, regenerates 2 HP periodically, lightning and fire resistant.
- **Drops [OS]:** diamond items; Emperor Scorpion Scale (Big Bertha Guard; also used for "Slice").
  - [FW] guaranteed: 1 scale, 5–10 raw beef, 5–10 obsidian. Chance: diamond gear, diamond, Ultimate gear.
  - [CC]: block of diamond, diamond tools and armour, item frame.

#### 20. Scorpion
- **Stats [OS scorpion]:** Attack 4; Defense 10; Health 15.
- **Spawn:** desert [OS]; minions of the Emperor Scorpion. [NW]: Mesa, Scorpion & Rat dungeon; [CC]: Bouncy House dungeon.
- **Drops:** gold, titanium & uranium nuggets [OS].

#### 21. Hercules (Hercules Beetle)
- **Stats [OS hercules]:** Attack 30; Defense 19 ([NW] 18); Health 250.
- **Spawn:** Forest/Jungle/Taiga Hills and Extreme Hills Edge [OS].
- **Abilities:** flings victims high into the air; nasty bite [OS].
- **Drops:** Big Hammer (Big Bertha Handle) [OS]; [CC] also diamond tools and armour, diamond block, item frame, raw beef.

#### 22. Basilisc (spelled "Basilisc" on [OS]; wikis use "Basilisk")
- **Stats [OS basilisc]:** Attack 24; Defense 15; Health 200.
- **Size:** [FW] over 10 blocks long, 3 blocks high.
- **Spawn:** Mining Dimension (Red Ant) – Basilisc Lairs, a pyramid-shaped surface maze; also Jungles [OS].
  - [FW]: the lair boss room has 3 Basilisks.
  - [CC]: 3rd floor of King/Queen Challenge Dungeons.
- **Abilities:** poison and slowness [OS].
  - [FW]: slowness 6–7 "stun", poison 5 s, regenerates 1 HP.
  - [CC]: slowness when the player looks at it.
- **Drops:** emerald items & Basilisc Scale (Big Bertha Guard) [OS]; [FW] also raw chicken.
- **Relations:** [FW] territorial fights with Jumpy Bugs.

#### 23. Jumpy Bug
- **Category:** boss; config internal name **`TrooperBug`** [CFG] (Starship Troopers reference).
- **Stats [OS jumpy-bug]:** Attack 20; Defense 15; Health 200. "HUGE, and deadly."
- **Spawn:** swamps [OS]. [NW]: also Mesa (more common there), Challenge Towers, Chaos Dim.
- **Abilities:** huge jumps; spawns Spit Bugs [OS].
- **Drops:** Amethyst blocks, tools and gems [OS]; Jumpy Bug Scale (Big Bertha Guard) [OS items].

#### 24. Spit Bug
- **Stats [OS spit-bug]:** Attack 10 & spits acid; Defense 12; Health 100. "Almost nasty enough to be their own Boss Mob."
- **Spawn:** swamps [OS]; summoned by Jumpy Bug; Spit Bug Lairs [OS dungeons].
- **Abilities:** acid spit; [CC, NW] the acid sets you on fire.
- **Drops:** amethyst [OS].

#### 25. Mantis
- **Stats [OS mantis]:** Attack 16; Defense 10; Health 120 ([NW] 150).
- **Size:** about 3 blocks tall [FW].
- **Spawn:** Overworld and Crystal Dimension [OS]; Mantis Nests in jungles [OS dungeons]. [NW, CC]: plains and savannah.
- **Abilities:** "Eating players, and almost anything else that moves" [OS]; hostile to nearly all mobs [NW]; lure it into water [OS].
- **Drops:** Mantis Claw [OS] (the claw drains health; Big Bertha Handle). [FW, CC]: uranium/titanium/gold nuggets, diamonds, item frame.

#### 26. Vortex
- **Stats [OS vortex]:** Attack 26; Defense 10; Health 150.
- **Spawn:** Termite Crystal Dimension [OS]; [NW] crystal towers, Chaos Dim., Challenge Tower.
- **Abilities [OS]:** throws mobs high into the air; enslaves Fairies; leads the takeover of the Crystal Dimension (Crystal Urchins are her "Vortex Army"). "She", "those incredible eyes".
- **Drops:** various ingots and goodies; Vortex Eye (Big Bertha Blade) [OS].
- **Version:** "Added in V11" [OS].

#### 27. Water Dragon
- **Stats [OS water-dragon]:** Attack 20; Defense 8; Health 150.
- **Spawn:** rivers, lakes, oceans [OS]; Water Dragon Lairs in oceans [OS dungeons].
- **Abilities:** waterballs and fireballs [OS]. [NW]: fire resistant (swims in lava); attacks girlfriends.
- **Taming:** "Tame Him – Use raw fish!" [OS] (although "very hostile"). [NW]: holding fish makes it approach without attacking.
- **Drops:** iron items [OS]; waterballs [OS]; Water Dragon Scale (Big Bertha Handle) [OS items].
- **Relations:** loses to Sea Viper [OS sea-viper, NW].

#### 28. Sea Viper
- **Stats [OS sea-viper]:** Attack 22; Defense 12; Health 160.
- **Spawn:** Ocean [OS]; Sea Viper dungeon [NW].
- **Abilities:** hides in water, poison, hissing [OS].
- **Drops:** iron stuff and Sea Viper Tongue (Big Bertha Blade) [OS].
- **Version:** "Added in V16" [OS].

#### 29. Sea Monster
- **Stats [OS sea-monster]:** Attack 14; Defense 8; Health 110.
- **Spawn:** Ocean and Swamp [OS]; [FW] at night.
- **Drops:** iron stuff and Sea Monster Scale (Big Bertha Guard) [OS].
- **Version:** "Added in V16" [OS].

#### 30. Mothra (spelled "MOTHRA!" on [OS]/[FW])
- **Stats [OS mothra]:** Attack 12 / fireballs / explosives; Defense 8; Health 150.
- **Spawn:** Extreme Hills [OS]; also listed as hostile in the Mining Dimension [OS mining-dimension].
- **Abilities:** fireballs and explosive fireballs; spawns lots of small moths when killed [OS].
- **Drops:** Moth Scales (Moth Scale Armor, fireproof per [OS dungeons]; 1 for Big Bertha Guard) [OS]. [CC]: blaze rod, gold nuggets, nether star.
- **Relations:** "Mobzilla's nemesis"; "One of the very first Bosses created for this mod … still one of my favorites" [OS]. Kill it while riding a Dragon [OS things-to-do].

#### 31. Molenoid
- **Stats [OS molenoid]:** Attack 18; Defense 12; Health 200.
- **Spawn:** Plains [OS].
  - [FW]: at night in the Chaos Dimension, and a Danger Dimension dungeon.
  - [NW]: plains/savannah/Chaos; groups of 3 in ruined Inca castles in the Danger Dim. ([OS dungeons] "Inca Pyramids … a BIG mob to keep you out").
- **Abilities [OS]:** "throws" dirt and suffocates targets. Molenoid dirt slows you and deletes itself ("self-cleaning"). Can "see" through dirt, sand and gravel.
- **Drops:** gold nuggets and Molenoid Nose (Big Bertha Guard) [OS].
- **Version:** "Added in V16" [OS].

#### 32. Triffid
- **Stats [OS triffid]:** Attack 20; Defense "12/closed"; Health 100.
- **Spawn:** circular islands in the Danger Dimension [OS]; The Greenhouse dungeon [OS dungeons].
- **Abilities [OS]:** cannot be hit while closed; knocks players off islands.
  - [CC]: heals while closed; immune to all damage then.
  - [FW]: stationary; tongue strike.
- **Drops:** Green Goo [OS]. Used for Big Bertha Handle, Creeper Repellent (4 goo, 2 string, 1 torch), Battle Axe [OS items, FW].

#### 33. T-Rex
- **Stats [OS t-rex]:** Attack 22; Defense 14; Health 160.
- **Spawn:** Red Ant Mining Dimension, surface at night [OS].
- **Relations:** "He eats Alosaurus for lunch" [OS]. [NW]: palette swap of the Allosaurus.
- **Drops:** "Lots of Uranium and Titanium nuggets"; T-Rex Tooth (Big Bertha Blade) [OS].

#### 34–36. Worms (Small Worm, Medium Worm, Boss Worm)
- **Stats [OS worms]:**

  | Stage | Attack | Defense | Health | Drops |
  |---|---|---|---|---|
  | Small | 5 | 0 | 10 | none |
  | Medium | 10 | 8 | 30 | rotten flesh, leather |
  | Boss | 18 | 14 | 90 | precious metal nuggets and other things |

- **Spawn:** plains [OS]. [FW]: also around the 6-tier challenge dungeon; [CC]: Large Worms replace Allosaurus on floor 1 of Tier-6 dungeons.
- **Mechanic [OS]:** "Whack-A-Mole". Kill all small worms → medium worms appear; kill all medium → the Boss Worm appears.
- **Abilities:** snatch pieces of your armour [OS].
  - [NW]: small worms take shoes, medium worms shoes & pants; the large worm throws items on the ground.
  - [CC]: they burrow.
- **Drops:** Worm Tooth from the Boss Worm (Big Bertha Blade) [OS items]. [FW Giant Worm]: also titanium/uranium nuggets, dirt, diamonds.

#### 37. Robo Warrior
- **Category:** robot; config internal name **`Robot4`** [CFG].
- **Stats [OS robo-warrior]:** Attack 12 / laser / explosives; Defense "18/shield"; Health 170.
- **Spawn:** Village Dimension (Rainbow Ant World) [OS]; at night [FW].
- **Abilities:** lasers, explosive lasers, hard punch; cannot be hit while the shield is up [OS]. [NW]: invulnerability lasts ~1.7 s; lowest spawn rate of the robots.
- **Drops:** "sometimes drops a Ray Gun" + electrical stuff [OS]. [FW]: "50 % chance" (claim); redstone, item frame, iron.

### C. Other hostile and neutral mobs

#### 38. Alien
- **Stats [OS alien]:** Attack 12; Defense 8; Health 100.
- **Size:** conflicting – [FW] "Aliens are huge" (can't pass a 2-wide door); [NW] "small to medium".
- **Spawn:** "Deep in the Mining Dimension caves" [OS]; Alien/WTF? dungeons in the Mining Dim. [OS dungeons].
- **Abilities [OS]:** puts out nearby torches; very, very fast.
  - [CC]: circles the player; head feathers pop up like a peacock's.
  - [FW]: loudest death sound in the mod.
- **Lore [OS]:** "delinquent little half-brother" of "the Alien (yes, THAT one)"; "Alien's little brother" [OS things-to-do].
- **Drops:** "Map, compass, other stuff" [OS]; [FW] compasses, iron, maps; [CC] clock, compass, flint (large amounts), map, spider eye.

#### 39. WTF?
- **Category:** config internal name **`GammaMetroid`** [CFG] (Metroid reference).
- **Stats [OS wtf]:** Attack 10; Defense 12; Health 100.
  - Conflict: [FW The WTF? Beetle] "Attack 23, Defense 35", drops "80 diamonds 43 amethyst". Defense 35 exceeds the config cap of 22, so this is likely vandalism.
- **Spawn:** caves [OS]; [FW] caves in the Mining Dimension, WTF dungeons (11×11, 4 high).
- **Abilities:** eats stone; slow; hates everything; "what do you think made all those caves?" [OS].
- **Taming:** iron ingots [OS].
- **Drops:** iron ingots, gold nuggets [OS].

#### 40. Kyuubi
- **Stats [OS kyuubi]:** Attack 10 / fireball; Defense 10; Health 125. [MM]: HP 25 in an older version; fireballs set targets on fire.
- **Spawn:** Nether [OS]; Kyuubi Dungeons in the Mining Dimension [OS dungeons].
- **Drops:** coal, redstone, quartz [OS]; [MM] block of redstone, "enchanted gold", nether quartz.
- **Lore:** nine-tailed fox, "What does the fox say?" [OS]; Naruto reference [FW, MM].

#### 41. Nastysaurus
- **Stats [OS nastysaurus]:** Attack 32; Defense 17; Health 200. "Bigger and even nastier than the T. Rex."
- **Spawn:** Red Ant/Mining Dimension [OS].
- **Behaviour:** angry because he is not in the Big Bertha recipe [OS]. [FW]: no arms; ram / tail whack; beats Pointysaurus, Alosaurus and T-Rex.
- **Drops:** iron, strings, leather, rotten flesh [OS].

#### 42. Alosaurus (in-game spelling; wikis "Allosaurus")
- **Stats [OS alosaurus]:** Attack 18; Defense 8; Health 110.
- **Size:** ~2.5 blocks tall; walks up blocks instead of jumping [FW].
- **Spawn:** Mining Dimension at night [OS]. [CC]: floor 1 of Challenge Dungeons (tier ≤ 5).
- **Abilities:** bites [OS]; can throw you into the air [FW].
- **Drops:** beef and gold nuggets [OS].

#### 43. Pointysaurus
- **Stats [OS pointysaurus]:** Attack 10; Defense 16 ([NW] 6); Health 80.
- **Spawn:** Red Ant/Mining Dimension [OS].
- **Behaviour:** "Just leave him alone and he'll leave you alone" [OS]. [NW]: neutral, provoked by attack or eye contact. Horned (Triceratops-like per [CC]).
- **Drops:** leather, beef, rotten flesh, string [OS].

#### 44. Leonopteryx ("Leon")
- **Stats [OS leonopteryx]:** Attack 55; Defense 16; Health 250.
- **Spawn:** nests in the Mining Dimension [OS]; Leonopteryx Dungeon = "a nest of vicious dragons" [OS dungeons].
- **Taming / riding [OS]:** right-click with raw beef while it attacks you; once tamed it can be ridden.
- **Drops:** chicken, feathers, Kraken Repellents, Battle Axe (rare) [OS].
- **Lore:** the dragon "from a certain blue people movie" (Avatar's Toruk) [OS, FW].
- **Version:** "Appeared in V17" [OS].

#### 45. Cephadrome
- **Stats [OS cephadrome]:** Attack 70 (140 against Kraken); Defense 16 ([NW] 15); Health 300.
- **Spawn / summon [OS]:** Ice Plains. Or place an **Eye-of-Ender Block** with an **Extreme Torch** on top (each torch = one Cephadrome). Cephadrome Altars in the Danger Dimension [OS dungeons].
- **Riding [OS]:** not tameable but rideable, no saddle.
  - Feed raw beef, chicken or pork first; if it is hungry it eats you.
  - Controls: SPACE up, release down, W forward, S slow, Left Shift dismount. Wanders off and despawns after dismount.
  - Conflict: [NW] calls it tameable.
- **Behaviour:** not hostile, but "after a few hits he may get mad" [OS].
  - [FW]: attacks hostile mobs and other bosses; regenerates; hurt a bit by fire and lava.
  - Monster Hunter reference (Cephadrome) [FW, NW].
- **Drops:** Ruby items [OS]; [FW] uranium/titanium nuggets; [NW] Creeper Launcher.

#### 46. Crab
- **Stats [OS crab]** (large / medium / small): Attack 24/12/6; Defense 18/17/16; Health 180/90/45. Same in [CC].
  - Conflict: [NW] HP 250/125/62, Defense 18/17/8.
- **Spawn:** ocean, Overworld [OS]. [NW]: also the Crystal Dimension.
- **Abilities:** all sizes launch you into the air [FW, CC].
- **Drops:** crab meat [OS] (Crabby Patty food [OS things-to-do]).

#### 47. Criminals
- **Stats [OS criminals]:** Attack 1; Defense 18; Health 100.
- **Theme:** "Politicians and Bankers" – TheyCallMeDanger's political satire [OS]. [NW]: health bar shows "Banker"/"Police"-type titles (claim).
- **Spawn:** Villages [OS]; "The White House is filled with criminals" [OS dungeons]. [FW]: "quartz prison structures" in the Unstable Ant dim. (claim).
- **Abilities:** theft – "check your inventory!!!" [OS]; stolen items are dropped on death [FW].
- **Drops:** "Emeralds, and all YOUR stuff!" [OS].
- **Version:** "Appeared in V17" [OS].

#### 48. Robo Pounder
- **Category:** robot; config internal name **`Robot2`** [CFG].
- **Stats [OS robo-pounder]:** Attack 22 / destruction; Defense 18; Health 200. [FW]: 20 damage.
- **Spawn:** Village Dimension [OS]; robots appear after sunset [OS village-dimension].
- **Abilities:** spins its arms, destroying you and everything else [OS].
  - [NW]: arm swings dig up the ground; immune to fire and poison.
  - [FW]: smacks you into the air.
- **Drops:** electrical stuff [OS].

#### 49. Robo Gunner
- **Category:** config internal name **`Robot3`** [CFG].
- **Stats [OS robo-gunner]:** Attack 16 / laser; Defense 14; Health 80.
- **Spawn:** Village Dimension [OS].
- **Abilities:** shoots ray-gun charges [OS]; [NW] feigns retreat, then turns and fires.
- **Drops:** electrical stuff [OS].

#### 50. Robo Sniper
- **Category:** config internal name **`Robot5`** [CFG] ("Robo Spinner" in [NW] translation).
- **Stats [OS robo-sniper]:** Attack "Laser Ball"; Defense 6; Health 20. [NW]: Attack 16, Defense 8, HP 20.
- **Spawn:** Village Dimension at night with the other robots [OS].
- **Abilities:** rapid fire [OS].
- **Drops:** laser balls and redstone items [OS].

#### 51. Bomb-omb
- **Category:** config internal name **`Robot1`** [CFG]; Mario Bob-omb reference [CC].
- **Stats [OS bomb-omb]:** Attack small explosion; Defense 2; Health 5.
- **Spawn:** Rainbow Ant Village Dimension [OS].
- **Drops:** gunpowder [OS].

#### 52. Ender Knight
- **Stats [OS ender-knight]:** Attack 12; Defense 6; Health 60.
- **Spawn:** Overworld, Unstable Ant Danger Dimension [OS]; Ender Knight Outposts (Mining Dim. and The End), Ender Castles [OS dungeons].
- **Abilities:** teleports behind you [OS]; [CC] neutral until looked at or attacked (Enderman-like). Hide under something too low for it [OS].
- **Drops:** Eye of Ender, Ender Pearl [OS].

#### 53. Ender Reaper
- **Stats [OS ender-reaper]:** Attack 18; Defense 8 ([NW] 9); Health 90.
- **Spawn:** Overworld, Danger Dimension [OS]; Ender Reaper Graveyards in The End [OS dungeons].
- **Abilities:** teleporting, killing players [OS].
- **Drops:** Eye of Ender [OS]; [NW] also ender pearl.

#### 54. Bee
- **Stats [OS bee]:** Attack 12 and poison; Defense 5; Health 80.
- **Spawn:** Jungles and Forests [OS]; Beehives (Mining Dim.) and Small Beehives [OS dungeons]. [FW]: also savannah and plains.
- **Behaviour [OS]:** flies at you after being shot; won't follow underwater because water damages it. [FW]: chance of nausea.
- **Drops:** gold nuggets and Butter Candy [OS]; [CC] also sugar, dandelion.

#### 55. Dungeon Beast
- **Stats [OS dungeon-beast]:** Attack 12; Defense 6; Health 65.
- **Spawn:** Termite Crystal Dimension, "down deep" – guards the endless dungeon maze [OS crystal-dimension].
- **Drops:** "stuff" [OS].
- **Version:** "164.11" [OS].

#### 56. Rotator
- **Stats [OS rotator]:** Attack 10; Defense 8; Health 35.
- **Spawn:** Termite Crystal Dimension [OS]; Rotator Dungeons and Rotator Power/Charging Stations [OS dungeons].
- **Abilities [OS]:** circles targets, hard to hit, shoots blinding sparks. "My first foray into a generated mob instead of a modeled one."
- **Drops:** crystal ingots and energy [OS].
- **Version:** "164.11" [OS].

#### 57. Crystal Urchin
- **Stats [OS crystal-urchin]:** Attack "Fire/10"; Defense 4; Health 25.
- **Spawn:** Termite Crystal Dimension [OS]; Crystal Urchin Spawner dungeon [OS dungeons].
- **Behaviour:** sets innocent critters on fire; henchmen of the Vortex Army [OS].
- **Drops:** ingots and Crystal Apples [OS].

#### 58. Irukandji
- **Stats [OS irukandji]:** Attack 20 or instant death; Defense 0; Health 1.
- **Size:** tiny ("SMALL. For real!") [OS]; one of the smallest mobs [FW].
- **Spawn:** lakes in the Termite Crystal Dimension [OS].
- **Abilities [OS]:** touching it with an empty hand = instant death; kill it with anything except your hands. [FW]: used to ignore armour; can climb onto land.
- **Drops:** dead Irukandji (→ Irukandji Arrows for the Skate Bow) [OS].
- **Version:** "Added in V11" [OS].

#### 59. Skate
- **Stats [OS skate]:** Attack 8; Defense 4; Health 8.
- **Spawn:** lakes in the Termite Crystal Dimension [OS].
- **Drops:** silk (string; used for the Skate Bow) [OS].
- **Version:** "164.11" [OS].

#### 60. Rat
- **Stats [OS rat]:** Attack 3; Defense 1; Health 5.
- **Spawn:** deep underground in the Crystal Dimension [OS]; [CC] Bouncy House, Crystal Haunted House, Endless Maze.
- **Rat Sword rats:** friendly to the wielder and his pets. Config `RatPlayerFriendly` / `RatPetFriendly` (default 0) makes them friendly to all players / all pets [OS rat, CFG].
- **Drops:** rotten flesh [OS]. Crystallized rats → Rat Sword [OS crystal-dimension].
- **Version:** "164.11" [OS].

#### 61. Terrible Terror
- **Stats [OS terrible-terror]:** Attack 5; Defense 3; Health 10.
- **Spawn:** Danger Dimension [OS].
- **Abilities:** flying, quick, hard to hit [OS].
- **Drops:** emerald, feather, rotten flesh [OS].

#### 62. Lurking Terror
- **Stats [OS lurking-terror]:** Attack 6; Defense 5; Health 30.
- **Spawn:** Danger Dimension, active during the day [OS]. [FW]: dragonfly-like.
- **Abilities:** knocks players off high places [OS].
- **Drops:** beef, flint, feathers [OS].

#### 63. Creeping Horror
- **Stats [OS creeping-horror]:** Attack 3; Defense 2; Health 10.
- **Spawn:** Danger Dimension, in packs [OS].
- **Drops:** rotten flesh, bone, silk [OS].

#### 64. Cloud Shark
- **Stats [OS cloud-shark]:** Attack 6; Defense 0; Health 15.
- **Spawn:** Danger Dimension; spawns near the ground, then flies up into the clouds [OS]. Cloud Shark Dungeons in the sky [OS dungeons]. [NW]: the lowest floor of the Challenge Tower.
- **Drops:** paper, silk, bone [OS].

#### 65. Attack Squid
- **Stats [OS attack-squid]:** Attack 8; Defense 0; Health 10.
- **Spawn:** in the water [OS]; Play Pools [OS dungeons].
- **Abilities:** temporary blindness [OS]; [CC] can climb onto land.
- **Kraken link [OS]:** if killed by a player there is a chance it "call[s] home for Dad, the Kraken". Let a Girlfriend, Baby Dragon or Lizard kill them, or use a Kraken Repellent.
- **Drops:** rare gold items, ink sacks [OS]; [CC] gold tools/armour, golden and enchanted golden apple, raw fish.

#### 66. Cave Fisher
- **Stats [OS cave-fisher]:** Attack 4; Defense 4; Health 10.
- **Spawn:** deep in caves [OS]; [CC] Ender Castles.
- **Drops:** gold, uranium and titanium nuggets [OS].

#### 67. Leaf Monster
- **Stats [OS leaf-monster]:** Attack 2; Defense 1; Health 6.
- **Spawn:** forests and jungles [OS]; Leaf Monster Dungeons in the Plains [OS dungeons].
- **Abilities:** blends in, jumps out when you get close [OS].
- **Drops:** wood, leaves, rotten flesh [OS].

#### 68. Rubber Ducky
- **Stats [OS rubber-ducky]:** Attack 6; Defense 1; Health 5.
  - Conflicts: [FW] "Health 40 (5 per 1 ducky), Defense 5, Attack 8, respawns 270 times" (dubious); [CC] HP 6, Attack 5, Defense 1.
- **Spawn:** rivers [OS]; Rubber Ducky Ponds [OS dungeons].
- **Abilities:** "Re-spawning", "Anger issues" – hostile once hit [OS, CC]. [CC]: attacks squids.
- **Drops:** feather and chicken [OS].
- **Version:** "Appeared in V17" [OS].

#### 69. Termite
- **Stats [OS termite]:** Attack 2; Defense 0; Health 5.
- **Spawn:** forest, jungle, plains (from Termite Nests) [OS]; [CC] Termite Troll Block.
- **Behaviour:** hostile; destroys anything wooden and multiplies [OS, CC]. Right-click (empty hand, empty inventory) → **Crystal Dimension** [OS].
- **Counters:** Kraken and Creeper Repellents now also repel ants and termites [OS items]; Peacocks eat termites [OS peacock].
- **Drops:** none [OS].

#### 70. Red Ant
- **Stats [OS ants]:** the ants box gives Attack 0/2, Health 1/2 (harmless ants / Red Ant). [CC]: Red Ant HP 2, Attack 2.
- **Spawn:** "almost everywhere" (ant nests) [OS]; "rumored to be a Red Ant colony in The End, providing a convenient way back" [OS].
- **Behaviour:** hostile (bites). Right-click → **Mining Dimension** [OS].
- **Drops:** nothing [OS].

#### 71. Cryolophosaurus
- **Stats [OS cryolophosaurus]:** Attack 3; Defense 1; Health 10.
- **Spawn:** Red Ant Mining Dimension [OS]; [CC] Chaos Dim.
- **Drops:** chicken, uranium and titanium nuggets [OS].

#### 72. Vampire Butterfly
- **Stats:** from [OS butterflies] (shared box): Attack 0/1 → 1 for the vampire variant; Health 2.
- **Spawn:** Unstable Ant Danger Dimension [OS butterflies, danger-dimension]. [CC]: also from a dying Caterkiller. [FW]: a 0.001 % chance from Mothra's death (claim).
- **Abilities:** "Sucking blood" [OS].
- **Drops:** nothing [OS].

### D. Companions, pets and mounts

#### 73. Girlfriend
- **Stats [OS girlfriends--boyfriends]:** Attack shoes or held item; Defense 8 (or Pink Tourmaline / Tigers Eye armour); Health 80. 41 outfits + 18 bikinis.
- **Spawn:** almost everywhere [OS]; Girlfriend/Boyfriend Island, Damsel in Distress dungeons [OS dungeons]. `GirlfriendEnable` default 1 [OS].
- **Taming:** right-click with a Red Rose (possibly a dozen); Dead Bush untames. Hold a red rose → she follows closely [OS]. [FW] says "Poppy" (the 1.7 rose).
- **Mechanics [OS]:**
  - Weapons: holds and trades items; throws shoes (don't hurt players); can wield swords ("give her an Ultimate Sword").
  - Armour: only Pink Tourmaline or Tigers Eye.
  - Items: Ruby = mute, Amethyst = unmute; diamond to hold = stay.
  - Healing and outfits: heals over time, food heals, red rose heals instantly; yellow rose changes outfit (bikini when wet).
  - Combat: hates and attacks Creepers; ignores Endermen, Zombie Pigmen, players.
  - Jealous: attacks untamed girls nearby.
  - Dances at night on a ≥5×5 floor of diamond/emerald/gold/uranium/titanium blocks, best under an Experience Tree.
  - Follows through ant teleports; must be pushed through Nether portals.
- **Drops:** roses, shoes [OS].
- **Event:** Valentine's Day 2014 (V13) girlfriends turned violent [OS]; see "Removed/old-version".

#### 74. Boyfriend
- **Stats [OS]:** Attack game controller or held item; Defense 8 (or Emerald / Amethyst / Ultimate armour); Health 80. 28 outfits + 18 swimsuits.
- **Spawn:** almost everywhere, but **off by default** (`BoyfriendEnable 0` → 1) [OS, CFG]. [MM]: "found by right clicking a frog" – probably confused with frog royalty.
- **Taming:** cooked beef or cooked peacock [OS]; cooked steak heals fully.
- **Mechanics:** throws game controllers; sword possible; leather changes clothes; jealous; does not dance; `BoyfriendBroMode=1` changes dialogue [OS, CFG]. [MM]: armour "ruby, amethyst, ultimate" (conflicts with [OS] "Emerald, Amethyst, Ultimate").
- **Drops:** game controllers [OS].

#### 75. Frog Royalty – Prince, Swamp Prince, Princess, Swamp Princess
- **Category:** human companions (not the dragon Prince/Princess). "Just like the regular boyfriends/girlfriends, but you can't change their skins, just their bathing attire" [OS frog].
- **Obtain:** kiss a Frog = sneak + right-click with an empty hand. Tame with cooked beef or roses [OS frog].
- **Stats / drops:** not found.

#### 76. Dragon
- **Stats [OS dragon]:** Attack 35; Defense 14; Health 200.
- **Spawn:** Unstable Ant Danger Dimension, or raised from a Baby Dragon [OS]. Conflict: [FW] says Red Ant/Mining Dimension; [NW] mining/danger/chaos.
- **Taming & riding [OS]:** wild dragons are tameable with "a fair amount of raw beef"; no saddle.
  - Controls: empty hand = mount; SPACE up, W forward, S slow, Left Shift dismount; A = small fireballs, D = big explosive fireballs.
  - Random item = sit.
- **Item interactions [OS]:**
  - Snowball → white, coal → black.
  - Ice Block = flames off, Flint & Steel = on.
  - Gunpowder = "supercharged" explosive fireballs.
  - Diamond → turns back into a Baby Dragon (and back).
- **Role:** "Dragons should be used to fight Kraken and MOTHRA" [OS].
- **Drops:** beef [OS].

#### 77. Baby Dragon
- **Stats [OS baby-dragon]:** Attack 5; Defense 5; Health 200 ("virtually indestructible").
- **Spawn:** Red Ant Mining Dimension [OS].
- **Taming:** raw beef; Dead Bush untames. Grows into a tamed, ridable Dragon after a random time; diamond forces growth [OS].
- **Behaviour:** follows you, attacks most hostile mobs, sets things on fire, keeps up with creative flight [OS].
- **Drops:** beef [OS].

#### 78. Stinky
- **Stats [OS stinky]:** Attack 10; Defense 6; Health 100.
- **Spawn:** Nether and Unstable Ant Danger Dimension [OS]; the "Stinky House" dungeon [OS dungeons].
- **Taming:** raw beef; Dead Bush untames [OS].
- **Behaviour [OS]:** morphs randomly into 19 colours; eats coal ore and "burps it back up"; each morph "poos" a different item; "Kicks Blaze butt".
  - [FW] morph examples: Zombie → rotten flesh, Diamond Ore → diamonds, Gold Ore → gold ingots, Skeletal → bones.
  - [FW]: swarms and kills most hostile mobs.
- **Drops:** "All sorts of goodies!" [OS].

#### 79. Hydrolisc
- **Stats [OS hydrolisc]:** Attack 0; Defense 0; Health 50.
- **Spawn:** swamps [OS].
- **Taming:** raw fish; Dead Bush untames. Head ruffles show health, tail wagging shows sitting [OS].
- **Ability:** gives its health points to you when you are hurt; heals fast in or near water [OS].
- **Drops:** fish [OS].

#### 80. Camarasaurus
- **Stats [OS camarasaurus]:** Attack 0; Defense 0; Health 20.
- **Spawn:** only the Red Ant Mining Dimension [OS].
- **Taming:** red apple – **cannot be un-tamed** [OS]. Tail speed shows health/sitting. Eats grass, leaves, vines, cactus; "smooches" you.
- **Drops:** red flower [OS].

#### 81. Velocity Raptor
- **Stats [OS velocity-raptor]:** Attack 0; Defense 0; Health 10.
- **Spawn:** Red Ant Mining Dimension [OS].
- **Taming:** red apple; Dead Bush untames [OS].
- **Ability:** doubles your ground speed; sit = speed off, another red apple = on. Battle Mob [OS].
- **Drops:** red flowers [OS].

#### 82. Ostrich
- **Stats [OS ostrich]:** Attack 0; Defense 0; Health 25.
- **Spawn:** desert, daytime [OS].
- **Riding [OS]:** rideable without taming or saddle (W forward, SPACE jump). Runs up walls and trees, no fall damage. Optional taming with red apple; sits only on sand/gravel/grass/dirt. Battle Mob.
- **Drops:** roses and feathers [OS].

#### 83. Chipmunk
- **Stats [OS chipmunk]:** Attack 0; Defense 0; Health 5.
- **Spawn:** forests [OS].
- **Taming:** red apple; Dead Bush untames [OS]. Digs holes. Battle Mob.
- **Drops:** seeds [OS].

#### 84. Gazelle
- **Stats [OS gazelle]:** Attack 0; Defense 0; Health 15.
- **Spawn:** plains [OS]; Utopia Dimension [OS utopia-dimension].
- **Taming:** red apple; Dead Bush untames. Eats strawberry, potato and carrot plants [OS].
- **Drops:** beef [OS].

#### 85. Lizard
- **Stats [OS lizard]:** Attack 6; Defense 5; Health 30.
- **Spawn:** in water [OS].
- **Taming:** temporarily with ink sacs; follows while you hold an ink sac [OS].
- **Role:** kills Attack Squids (anti-Kraken) [OS]. Battle Mob.
- **Drops:** none [OS].

**Battle Mobs (applies to #81 Velocity Raptor, #82 Ostrich, #83 Chipmunk, #85 Lizard)** [OS battle-mobs-gameplay]:
- Teams: carrot = Red, potato = Blue, quinoa = Green.
- Each creature remembers two owner names (half hat after one click, full hat after two). It attacks all other players.
- Corn clones the creature, hat included.
- Empty-hand click toggles guard mode (defends ~10 blocks around its spot).

### E. Passive, ambient and utility creatures

#### 86. Baryonyx
- **Stats [OS baryonyx]:** Attack 0; Defense 0; Health 40.
- **Behaviour:** the page contradicts itself – "Harmless, kind of like a cow … He is hostile!" – while listing Attack 0; [CC] passive.
- **Spawn:** Red Ant Mining Dimension [OS].
- **Breeding:** Crystal Apple [OS].
- **Drops:** beef [OS].

#### 87. Beaver
- **Stats [OS beaver]:** Attack 0; Defense 0; Health 15.
- **Spawn:** forests, jungles [OS].
- **Ability:** takes down trees quickly (recommended against leaf lag [OS technical-help]).
- **Breeding:** Crystal Apple [OS].
- **Drops:** pork [OS].

#### 88. Cassowary
- **Stats [OS cassowary]:** Attack 0; Defense 0; Health 10.
- **Spawn:** Extreme Hills [OS]; [CC] also Mining Dim.
- **Breeding:** Crystal Apple [OS].
- **Drops:** chicken [OS].

#### 89. Peacock
- **Stats [OS peacock]:** Attack 0; Defense 0; Health 15.
- **Spawn:** Termite Crystal Dimension [OS].
- **Behaviour:** eats termites, drops eggs, runs fast [OS].
- **Breeding:** Crystal Apple [OS].
- **Drops:** peacock feather (Peacock Armor, Irukandji Arrows) and meat [OS].
- **Version:** "164.11" [OS].

#### 90. Whale
- **Stats [OS whale]:** Attack 0; Defense 0; Health 100.
- **Spawn:** lakes in the Termite Crystal Dimension [OS].
- **Breeding:** Crystal Apple [OS].
- **Drops:** fish [OS].
- **Version:** "164.11" [OS]. [FW]: an old glitch made Irukandji kill whales (fixed).

#### 91. Flounder
- **Stats [OS flounder]:** Attack 0; Defense 0; Health 5.
- **Spawn:** Termite Crystal Dimension [OS].
- **Behaviour:** starts as a normal fish, grows flat; blends in with the mud [OS].
- **Drops:** fish [OS].
- **Version:** "164.11" [OS].

#### 92–95. Cows (Apple Cow, Golden Apple Cow, Enchanted Golden Apple Cow, Crystal Apple Cow)
- **Stats [OS cows]:** Attack 0; Defense 0; Health 10 (one box for all).
- **Spawn:** most places [OS]; Crystal Cow in the Crystal Dimension [OS]; Utopia has "Cows" [OS utopia-dimension].
- **Breeding:** wheat [OS].
- **Drops:** beef, leather and the matching apple (apple / golden apple / enchanted golden apple); the Crystal Cow drops crystal apples [OS].
- **Unverified:** "Apple Cow Alpha" (Attack 6, Defense 4, Health 40; larger, defends Apple Cows) appears only on [FW Apple Cow]. No official page; see "Open questions".

#### 96. Easter Bunny
- **Stats [OS easter-bunny]:** Attack 0; Defense 0; Health 10.
- **Spawn:** Plains, Forest, Forest Hills – only on Easter; otherwise from its dried egg [OS].
- **Behaviour:** hops along dropping random spawn eggs [OS]. [FW]: despawns unless Zookeeper'd or name-tagged; drops raw chicken if killed.
- **Drops:** "ALL THE EGGS!" [OS].
- **Version:** "Added in V16", event on April 20, 2014 [OS].

#### 97–99. Brown Ant, Rainbow Ant, Unstable Ant (+ Red Ant #70, Termite #69)
- **Stats [OS ants]:** harmless ants Attack 0, Health 1 (see #70 for the Red Ant).
- **Spawn:** ant nests, almost everywhere [OS]. [OS items]: Ant Nest blocks can be dug up and kept as a "telepad", ringed with Salt Blocks.
- **Portal function (right-click, empty hand)** [OS ants, dimensions]:

  | Ant | Destination |
  |---|---|
  | Brown | Utopia Dimension |
  | Red | Mining Dimension |
  | Rainbow | Village Dimension |
  | Unstable | Danger Dimension |
  | Termite | Crystal Dimension |

  Clicking the same ant type again returns you. Pets teleport with you unless sitting.
- **Drops:** nothing [OS].

#### 100. Butterfly
- **Stats [OS butterflies]:** Attack 0; Defense 0; Health 2.
- **Spawn:** Butterfly Plants during the day, almost everywhere [OS].
- **Portal:** right-click with an empty hand → **Chaos Dimension** ("fierce warriors chasing butterflies") [OS butterflies, chaos-dimension].
- **Drops:** nothing [OS].

#### 101. Moth
- **Stats [OS moths]:** Attack 0; Defense 0; Health 2.
- **Spawn:** Moth Plants at night; swamp, forest; flock around torches; many spawn when Mothra dies [OS].
- **Drops:** none [OS].

#### 102. Firefly
- **Stats [OS firefly]:** Attack 0; Defense 0; Health 1.
- **Spawn:** most places at night; Firefly plants [OS].
- **Drops:** Extreme Torch [OS].

#### 103. Dragonfly
- **Stats [OS dragonflies]:** Attack 0; Defense 0; Health 10.
- **Spawn:** swamps and lakes [OS].
- **Behaviour:** eats ants, mosquitoes, butterflies and birds; attacks horses, not players [OS]. Config `DragonFlyHorseFriendly=1` stops the horse attacks [CFG].
- **Drops:** gold, uranium, titanium nuggets [OS].

#### 104. Mosquito
- **Stats [OS mosquito]:** Attack 0; Defense 0; Health 2.
- **Spawn:** swamp, from mosquito plants [OS].
- **Behaviour:** follows you and buzzes your ears [OS].
- **Drops:** none [OS].

#### 105. Cricket
- **Stats [OS cricket]:** Attack 0; Defense 0; Health 3.
- **Spawn:** Forest, Jungle, Plains [OS].
- **Behaviour:** jumping, night chirping [OS]. [FW]: "Sometime it glitches and … attack[s] you" (claim).
- **Drops:** nothing [OS].

#### 106. Stink Bug
- **Stats [OS stink-bug]:** Attack 0; Defense 0; Health 5.
- **Spawn:** forests and jungles [OS].
- **Ability:** farts, nausea to anything nearby [OS].
- **Drops:** dead stink bug (Kraken Repellent, Poison Sword) [OS].

#### 107. Birds
- **Stats [OS birds]:** Attack 0; Defense 0; Health 2.
- **Spawn:** almost everywhere [OS].
- **Drops:** rare ruby [OS]; [CC] red birds drop the ruby, also feathers.
- **Note:** [OS things-to-do] mentions a "Ruby Bird Dungeon – only in the Utopia Dimension". A separate "Ruby Bird" mob is not documented anywhere.

#### 108. Frog
- **Stats [OS frog]:** Attack 3; Defense 0; Health 8.
- **Spawn:** rivers, jungles, swamps [OS]; Frog Pond dungeon [OS dungeons].
- **Behaviour:** eats bugs; jumps when punched; kiss → royalty (#75) [OS].
- **Drops:** slimeballs [OS].

#### 109. Ghost
- **Stats [OS ghost]:** Attack 0; Defense 0; Health 2.
- **Spawn:** Taiga [OS]; haunted houses, Crystal Haunted House, Inca Pyramids, The Pumpkin [OS dungeons].
- **Behaviour:** floats through walls, follows and distracts [OS].
- **Drops:** none [OS].

#### 110. Ghost Skeleton
- **Stats [OS ghost-skeleton]:** Attack 0; Defense 0; Health 5.
- **Spawn:** Taiga [OS].
- **Behaviour:** spins its head 360° [OS].
- **Drops:** none [OS].

#### 111. Gold Fish
- **Stats [OS gold-fish]:** Attack 0; Defense 0; Health 6.
- **Spawn:** usually the Unstable Ant Danger Dimension [OS]; Gold Fish Bowls in the ocean [OS dungeons].
- **Behaviour:** flies up into the clouds [OS].
- **Drops:** gold ("tons") [OS]; [CC] block of gold, titanium/uranium nuggets.

#### 112. Cliff Racer
- **Stats [OS cliff-racer]:** Attack 0; Defense 0; Health 5.
- **Spawn:** Unstable Ant Danger Dimension [OS].
- **Behaviour:** harmless "prehistoric bird" [OS].
- **Drops:** chicken, uranium and titanium nuggets [OS].

#### 113. Fairy
- **Stats [OS fairy]:** Attack 3; Defense 4; Health 40.
- **Variants:** blonde, brunette, redhead [OS].
- **Spawn:** Termite Crystal Dimension [OS]; [NW] also Utopia and Chaos.
- **Fairy Sword:** every hit spawns fairies that fight for you. [NW]: sword fairies are weaker.
- **Relations:** Vortex hauls fairies off as slaves [OS vortex].
- **Drops:** Crystal Torch [OS].
- **Version:** "164.11" [OS].

#### 114. OreSpawn Coin
- **Stats [OS orespawn-coin]:** Attack 0; Defense 0; Health 1.
- **Spawn:** Forests and Village Dimension [OS].
- **Behaviour:** in-game advertising for the website; one hit kills [OS].
- **Drops:** "Great green stuff!" (emerald items per [NW]) [OS].

#### 115. OreSpawn T-shirt
- **Stats [OS orespawn-t-shirt]:** Defense 0; Health 1 (no attack stated).
- **Spawn:** randomly in the Village Dimension [OS].
- **Behaviour:** advertises the real-life shirt shop [OS]; [FW Robo pounder] floating ad showing an Emperor Scorpion.
- **Drops:** emeralds [OS].

---

## 3. Bosses & progression

### 3.1 Gateways: creatures are the portals
There is no portal block. **Creatures are the dimension keys:**

| Creature | Dimension | What lives there |
|---|---|---|
| Brown Ant | Utopia | The King, Queen's Altar |
| Red Ant | Mining | Dinosaurs, Alien/WTF?, Leonopteryx nests, Basilisc Lairs, Baby Dragons; very common Ancient Dried Spawn Eggs |
| Rainbow Ant | Village | Robots at night, Mobzilla |
| Unstable Ant | Danger | Nightmares, Triffids, Dragons, Challenge Towers |
| Termite | Crystal | Enter with an empty inventory: Vortex, Irukandji, Rotators, Fairies, Crystal Cows; source of Crystal Apples |
| Butterfly | Chaos | "Almost all the mobs" except water-based ones [OS chaos-dimension] |

Critters therefore matter for progression. Zoo Cages + Critter Cages + **Zookeeper** items (only from Nightmares) keep portal ants and pets from despawning [OS items].

The mod's name comes from **Ancient Dried OreSpawn Eggs**: ore-like blocks at shallow depth, very common in the Mining Dimension. Crafting one with a water bucket gives a spawn egg ("one for most every creature") [OS ancient-dried-eggs]. The top bosses need **9 egg parts** (King, Queen, Mobzilla) [OS].

### 3.2 Tiers (derived from official page texts; tier labels are editorial)
1. **Overworld threats (early game)**
   - Plains/forests: Mantis, Bee, Worms, Molenoid, Leaf Monster, Termites.
   - Deserts at night: Emperor Scorpion.
   - Hills: Hercules, Mothra (Extreme Hills).
   - Forests: Caterkiller → Brutalfly.
   - Swamps: Jumpy Bug + Spit Bugs.
   - Jungles: Basilisc.
   - Water: Water Dragon, Sea Viper, Sea Monster, Crab, Attack Squid → **Kraken**.
   - Nether: Kyuubi.
2. **Ultimate gear.** Nuggets of titanium and uranium are dropped by Cave Fisher, Scorpion, T-Rex, Dragonfly, Cliff Racer, Cryolophosaurus and others. They are needed for the Ultimate set per [NW]; exact recipes belong to the items slice. Several [OS] pages set "Ultimate" armour as the minimum for bosses.
3. **Big Bertha** – the key gate. Its three parts need boss drops from **18 mob types** plus an Ultimate Sword [OS items]:

   | Part | Materials |
   |---|---|
   | Blade | Kraken Tooth, Worm Tooth (Boss Worm), T-Rex Tooth, **Ultimate Sword**, Caterkiller Jaw, Sea Viper Tongue, Vortex Eye |
   | Guard | Molenoid Nose, Sea Monster Scale, Moth Scale (Mothra), Basilisc Scale, Nightmare Scale, Emperor Scorpion Scale, Jumpy Bug Scale |
   | Handle | Ray Gun (Robo Warrior; also Robo Jeffery), Big Hammer (Hercules), Mantis Claw, Water Dragon Scale, Green Goo (Triffid) |

   The Nastysaurus is "upset because he's not included in the Big Bertha recipe" [OS]. [FW Bosses] lists the same bosses as its "Big birther" category.
4. **Mobzilla** (Village Dim. or 9 egg parts). [OS] advice: strongest armour, Big Bertha, Enchanted Golden Apples, "a Cephadrome or two or three", fire- and blast-proofing, Skate Bow with Irukandji arrows, hoverboards. Reward: **Mobzilla Scales → Mobzilla Armor**.
5. **Royal tier**
   - **The King** (Utopia, guarding the Golden Ore Tree): needs Mobzilla Armor and several Big Berthas. Reward: The Prince + Royal Guardian Armor & Sword.
   - The alternative is the **Level 6 Challenge Dungeon** in the Danger Dim., guarded by Hammerheads at the bottom. It contains a Prince egg, Royal Guardian armour and sword [OS dungeons, hammerhead].
   - **The Queen** (Utopia altar) or the **Queen's Challenge Dungeon** (Danger Dim.): reward Queen Scales + The Princess (+ Royal Guardian Sword per [OS]).
   - The tall dungeons stack mid-bosses per floor. [CC] floor examples: Alosaurus / Large Worm on floor 1, Basilisk on floor 3, Emperor Scorpion or Hammerhead or Caterkiller at the bottom.
6. **Raising the Prince.** Toddler → Young Prince (25 finishing bites, 10 days, 10 feedings) → Young Adult Prince (same pattern per [FW]) → optionally, with `FullPowerKingEnable=1`, **the true/Ultimate King**. [OS] warns to back up the world first.

### 3.3 Creature relationships (as stated in sources)
- **Counter-mobs:**
  - Cephadrome does 140 vs Kraken; Dragons are made for Kraken and Mothra [OS].
  - Lizards kill Attack Squids [OS]; Girlfriends/Boyfriends hunt Creepers [OS].
  - Nightmares are the only mob that takes down Mobzilla [OS].
- **Food chains / rivalries:**
  - T-Rex eats Alosaurus; Caterkiller beats the Emperor Scorpion [OS].
  - Sea Viper beats the Water Dragon [OS]; Nastysaurus beats other dinosaurs [FW].
  - Jumpy Bug vs Basilisc [FW]; Mothra is Mobzilla's nemesis [OS].
  - Dragonflies eat ants, mosquitoes, butterflies, birds; Peacocks eat termites; Frogs eat bugs [OS].
  - The Queen follows The King [FW]; Prince/Princess never attack King/Queen [NW].
- **Spawn chains:**
  - Attack Squid → Kraken(s), and Krakens call more Krakens.
  - Caterkiller → Brutalfly (on timeout) or butterflies incl. Vampire Butterflies on death [CC].
  - Emperor Scorpion → Scorpions; Jumpy Bug → Spit Bugs.
  - Small → Medium → Boss Worm; Mothra → moths.
  - Spider Driver ↔ Spider Robot.
  - Frog → royalty; Baby Dragon ↔ Dragon; Prince stages → King.
- **Factions:**
  - Vortex + Crystal Urchins ("Vortex Army") vs Fairies [OS].
  - Robots raid villages at night; robots don't fight each other [NW].
  - Red Ant Robots avenge killed ants [OS]; Criminals steal [OS].
  - Ender Knights/Reapers avenge killed Endermen [OS dungeons].

### 3.4 Config levers relevant to a remake [CFG]
- **Per-mob switches:** `<mob>Enable`, `<mob>_attack`, `<mob>_defense`, `<mob>_health` (ranges in the intro).
- **Global:** `AllMobsDisable`; `PlayNicely` (all mobs non-hostile, non-destructive; Kraken, King, Mobzilla, CaterKiller scaled down); `GuiOverlayEnable` (Girlfriend/Boss health bar).
- **Specific mobs:** `NightmareSize`, `FullPowerKingEnable`, `BoyfriendEnable` / `GirlfriendEnable` / `BoyfriendBroMode`, `RatPlayerFriendly` / `RatPetFriendly`, `DragonFlyHorseFriendly`.
- **Weapon PvP:** `BigBerthaPvP` (Big Bertha/Slice/Royal Guardian Sword), `UltimateSwordPvP`.
- **Internal names:** GammaMetroid = WTF?, Godzilla = Mobzilla, Robot1 = Bomb-Omb, Robot2 = Robo-Pounder, Robot3 = Robo-Gunner, Robot4 = Robo-Warrior, Robot5 = Robo-Sniper, TrooperBug = Jumpy Bug.

---

## 4. Removed / old-version mobs and changes

**No creature is documented as removed between 1.6.4 and the final 1.7.10 v20.3.**
- The official mob list (100 pages, Feb 2021 snapshot) matches the mob lists on [MM] and [CC2].
- Version stamps on the official pages are historical "added in" notes:
  - V11: Irukandji, Vortex.
  - "164.11" (most likely 1.6.4 v11; interpretation unverified): Dungeon Beast, Fairy, Flounder, Peacock, Rat, Rotator, Skate, Whale (the Crystal Dimension batch).
  - V16: Caterkiller, Easter Bunny, Molenoid, Sea Monster, Sea Viper.
  - V17: Criminals, Hammerhead, Leonopteryx, Rubber Ducky.
- **Version history** [DangerZone Archive]: 1.5.2 v4–v5, 1.6.2 v5–v6, 1.6.4 v8–v20, **1.7.10 v20.0 / v20.2 / v20.3 (final)**, 1.12.2 v0.1–v0.8 (restart, dev/premium builds). [OS technical-help]: "Versions 11–15 of OreSpawn were built and tested with Forge 9.11.1.965" (MC 1.6.4).

Changed or time-limited behaviour:

| Item | Old behaviour | Source |
|---|---|---|
| Girlfriends on Valentine's Day | Feb 14, 2014 (V13): girlfriends turned violent ("forgot to get them some chocolate"). [FW] claims the system date triggers huge hostile girlfriends that a "rose sword" reverts – unverified whether still in 1.7.10 | [OS girlfriends--boyfriends], [FW Girlfriend] |
| Easter Bunny | Easter surprise April 20, 2014 (V16); only spawns on Easter | [OS easter-bunny] |
| Kyuubi | HP 25 in an older version (now 125) | [MM Kyuubi] |
| Alien model | Originally a Xenomorph model; [NW] says changed for copyright reasons, [CC] says taken from the AvP mod. Current model has peacock-like feathers | [NW], [CC Alien], [FW Alien] |
| Irukandji | Used to ignore armour; "whether this is still active is unknown" | [FW Irukandji] |
| Prince fireballs | Formerly blew up whole houses; the Ice Block / flint-and-steel toggle was added | [NW], [OS the-prince] |
| Glitches fixed | Hammerhead hitting through the Challenge Tower roof; Jumpy Bug attacking natural Spit Bugs; Irukandji killing Whales | [FW] |
| Boyfriends | Exist but disabled by default | [OS], [CFG] |
| Princess | "Currently only has a toddler form" – older growth stages were never added | [OS the-princess], [FW] |
| 1.12.2 port | "A lot of weapons, armor, tools, and mobs were deleted"; emerald and ultimate sets kept, weak bosses implemented (history/port slice) | [NW main article, footnote 53] |

**Fan-wiki pages that describe creatures NOT confirmed to exist in OreSpawn.** Treat them as fan fiction or vandalism; they are not in the table:
- **"The Nether Prince"** (300 000 HP, 4 battle phases, drops Nether Armor/Nether Sword) – no official source.
- The Frost Prince, The Magma Prince, The Sea Prince, The Predator Prince – empty or stub pages.
- Boss Pig, Lavapig, Charcoal Beast, Maurice, "Zeggy boggy doog" – empty pages.
- "Apple Cow Alpha" – stats only on [FW].
- The [CC] "Mobs" index also lists non-OreSpawn mobs from other Crazy Craft mods (Mutant Creatures, Weeping Angels, Killer Pacman, Mexican Chicken, Spider Pig, Strider Pig, Tennis Ball, Creeper Minion).

---

## 5. Open questions / gaps

1. **XP values:** none published anywhere.
2. **Exact sizes / hitboxes:** only qualitative (Jeffery "10–11 blocks", Basilisk ">10 long, 3 high", Mantis ~3, Alosaurus ~2.5). Everything else is "HUGE" or size classes from [NW]. The jar/entity-render slice should measure.
3. **Exact drop tables and chances:** [OS] often says only "stuff", "Lots of stuff", "Robot stuff", "Awesome loot". Only the Emperor Scorpion has quantities ([FW], unverified).
4. **Spawn weights, biome lists, time-of-day and light rules for 1.7.10:** only prose. The Chaos Dimension composition is only in [CC] (day / night / caves lists).
5. **Stats never published:**
   - Ultimate King;
   - Frog Royalty (Prince, Swamp Prince, Princess, Swamp Princess);
   - Purple Power (only fan numbers);
   - Vampire Butterfly and Crystal Cow (only shared boxes);
   - Spider Driver ("not a lot");
   - OreSpawn T-shirt attack;
   - Island entity.
6. **Conflicts to resolve against the jar** (the config defaults `<mob>_attack/_defense/_health` are the best tie-breaker):
   - King attack 350 [OS] vs 250 [FW, NW]; Queen 225 vs 250.
   - Nightmare mid-size attack/defense ([FW] vs [NW]); Crab HP ([OS]/[CC] vs [NW]).
   - Pointysaurus defense 16 vs 6; Mantis HP 120 vs 150; Ender Reaper defense 8 vs 9.
   - Cephadrome defense 16 vs 15, and tameable ([NW]) vs "cannot tame" ([OS]).
   - Prince defense 16 [OS] vs 14 [FW, NW]; Robo Sniper and Spider Driver stats; Robo Pounder 22 vs 20.
   - WTF? stats ([FW] likely vandalised); Rubber Ducky stats.
   - Dragon natural spawn (Danger [OS] vs Mining [FW]); Molenoid spawn (Plains [OS] vs Chaos/Danger [FW]).
   - King/Queen spawn structure (tree vs altar; [MM] even says Red Ant dimension).
   - Boyfriend armour (Emerald [OS] vs Ruby [MM]).
7. **Claimed mechanics needing verification:**
   - damage cap of 120 per hit for King/Queen/Mobzilla [NW];
   - diamond-block growth of the Prince [FW, NW];
   - Creeper Repellent blocking Purple Power [FW, NW, MM];
   - Ultimate King immunity to physical damage [FW, NW];
   - Easter Bunny dropping Prince/Princess eggs [FW];
   - whether the Valentine's hostile-girlfriend code is still in 20.3.
8. **"Ruby Bird":** [OS things-to-do] mentions a "Ruby Bird Dungeon" in Utopia; unclear whether a distinct mob exists.
9. **Version-specific content:** the official site mixes text from V11–V17 (1.6.4 era) with 1.7.10-era pages. Mobs added in v18–v20.3 without their own page would be missed. None surfaced in any source, but a definitive 1.7.10 entity list needs the jar's entity registry (other agent).
10. **Not reachable:** web.archive.org (tool-blocked), the Minecraft Forum OreSpawn threads (HTTP 403), Planet Minecraft (HTTP 403), mc-pc.net 20.3 page (HTTP 403). The GitHub mirror of orespawn.com substituted for the archived site.

---

## 6. Sources

Official (primary):
- orespawn.com mirror, index of mobs: https://shrekleaker.github.io/orespawn.com/mobs.html – every `[OS <slug>]` citation is `https://shrekleaker.github.io/orespawn.com/<slug>.html` (e.g. `the-king.html`, `kraken.html`, `girlfriends--boyfriends.html`, `dungeons.html`, `items.html`, `ants.html`, `mining-dimension.html`, `danger-dimension.html`, `village-dimension.html`, `utopia-dimension.html`, `crystal-dimension.html`, `chaos-dimension.html`, `battle-mobs-gameplay.html`, `ancient-dried-eggs.html`, `technical-help.html`, `things-to-do.html`, `download.html`)
- Mirror repository (full HTML of the official site): https://github.com/ShrekLeaker/orespawn.com
- OreSpawn.cfg page [CFG]: https://shrekleaker.github.io/orespawn.com/orespawncfg-file.html (also https://www.orespawn.com/orespawncfg-file.html)
- DangerZone Archive (official version list): https://dangerzone-archive.weebly.com/orespawn.html
- Internet Archive, final build metadata: https://archive.org/details/orespawn-1.7.10-20.3_202109

Fan sources (secondary):
- OreSpawn fan wiki [FW]: https://orespawnmod.fandom.com/wiki/ (pages used: Alien, Allosaurus, Ants, Apple Cow, Attack Squid, Baby Dragon, Basilisk, Beaver, Bee, Bosses, Brutalfly, Butterfly, Caterkiller, CaveFisher, Cephadrome, Crabs, Creeping Horror, Cricket, Criminal, Dragon, Dragonfly, Easter Bunny, Emperor scorpion, Firefly, Giant Worm, Girlfriend, Green Goo, Hammerhead, Hercules Beetle, Hydrolisc, Irukandji, Jeffery, Jumpy Bug, King, Kyuubi, Large worm, Leonopteryx, Lizard, Lurking terror, MOTHRA!, Mantis, Mobzilla, Molneoid, Nastysaurus, Nightmare, Robo Warrior, Robo pounder, Robo warrior, Rubber ducky, Sea Monster, Sea Viper, Stink bugs, Stinky, T-Rex, The Kraken, The Nether Prince, The Prince, The Princess, The Queen, The Ultimate King, The WTF? Beetle, The Young Adult Prince, The Young Prince, Triffid, Vortex, Water Dragon, Worms; plus the empty or fan pages named in section 4). Retrieved via https://orespawnmod.fandom.com/api.php
- NamuWiki, added mobs [NW]: https://en.namu.wiki/w/Orespawn/%EC%B6%94%EA%B0%80%EB%90%98%EB%8A%94%20%EB%AA%B9 ; main article: https://en.namu.wiki/w/Orespawn
- Minecraft Mods wiki [MM]: https://minecraft-mods.fandom.com/wiki/OreSpawn and subpages OreSpawn/Alien, /Ant, /King and Queen, /Mobzilla, /Ostrich, /Boyfriend, /Irukandji, /Kyuubi, /Robo Warrior
- Crazy Craft wiki [CC]: https://crazy-craft-wiki.fandom.com/wiki/ (Alien, Allosaurus, Apple Cow, Attack Squid, Baby Dragon, Baryonyx, Basilisk, Beaver, Bee, Bird, Bomb-omb, Brown Ant, Brutalfly, Butterfly, Camarasaurus, Cassowary, Caterkiller, Cave Fisher, Chaos Dimension, Crab, Dragon, Emperor Scorpion, Ender Knight, Goldfish, Hammerhead, Hercules Beetle, Jumpy Bug, Large Worm, Mantis, Mobs, Mothra, Nastysaurus, Peacock, Pointysaurus, Rainbow Ant, Rat, Red Ant, Rubber Ducky, Scorpion, Spit Bug, T-Rex, Termite, Triffid, Unstable Ant, Water Dragon)
- Crazy Craft 2.0 wiki [CC2]: https://crazycraft2.fandom.com/wiki/Orespawn (+ Bosses, Hammerhead, Kraken, Kyuubi, Mobzilla, Water Dragon)
- 9Minecraft mod page (no mob data beyond feature blurbs): https://www.9minecraft.net/orespawn-mod/
- Wminecraft mod page (generic blurb only; its claims of "griffins", "Sky Dimension" and "Garden of Eden" are not supported by any OreSpawn source and were ignored): https://wminecraft.net/orespawn-mod/

Attempted, not accessible: https://orespawn.fandom.com (403/404), https://web.archive.org (tool-blocked), https://www.minecraftforum.net OreSpawn threads (403), https://www.planetminecraft.com/mod/orespawn-mod-2090519/ (403), https://mc-pc.net/mods/393-orespawn.html (403), https://www.orespawn.com (403 via fetch; `/home/...` news posts 404).
