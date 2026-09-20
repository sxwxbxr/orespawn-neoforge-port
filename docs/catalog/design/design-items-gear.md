# Design: design-items-gear

Stand: 10.09.2026. Batch: alle Manifest-Einträge der Kinds `weapon` (23), `tool` (28) und `armor` (56) = 107 Items (manifest `items`, nach `kind` gefiltert).
Quellen in Prioritätsreihenfolge: `docs/catalog/manifest.json` (Kürzel: manifest), dekompilierte Quelle `reference/src-20.2/src/main/java/danger/orespawn/<Class>.java` (Zeilenangaben `Klasse.java:ZEILE`), Geometrie `reference/jar/models/<Model>.json`, `reference/jar/renderers_dump.txt`, Texturen `reference/jar/extracted/assets/orespawn/`, danach `docs/research/03-items.md` und `06-models-design.md`.
Vanilla-Konstanten, die für Größen und Schadenszahlen gebraucht werden, wurden **nicht** aus dem Gedächtnis übernommen, sondern per `javap -c` aus `reference/jar/mcp/client-1.7.10.jar` gelesen (Klassen über `joined.srg` aufgelöst: `aeh` = ItemSword, `bop` = RenderPlayer, `bly` = ItemRenderer, `sv` = EntityLivingBase, `abb` = ItemArmor). Die Kontaktbögen zum Sichten der Icons und Layer-Texturen wurden mit PIL gebaut (Scratchpad, nicht im Repo); Farbangaben sind PIL-Pixelzählungen bzw. Mittelwerte über die genannte UV-Region.

**Bildsprache dieses Batches.** Die Ausrüstung von OreSpawn ist Vanilla-Grammatik in Materialfarben: jede Werkzeugstufe benutzt exakt die Vanilla-Silhouetten von Schwert, Spitzhacke, Schaufel, Hacke und Axt mit dunkelgrauem Stiel (#271b0e/#1d1d1c-Töne in jedem Icon), und nur der Kopf trägt die Materialfarbe – Smaragdgrün, Rubinrot, Amethystviolett, Elfenbein (Crystal Wood), Magenta (Pink Tourmaline), Kobaltblau mit Cyan (Kyanite), Ocker-Gold (Tiger's Eye). Die Ultimate-Reihe bricht das Schema mit einem Sprenkel aus Lavendel, Weiß und Cyan auf Tiefblau; dieselbe Sprenkelung kehrt auf der Ultimate-Rüstung wieder. Die Spezialschwerter sind Einzelstücke mit eigener Klingentextur (drei Klingen beim Nightmare Sword, Fell beim Rat Sword, Pastell beim Fairy Sword, Sichel bei der Mantis Claw). Die sieben 3D-Waffen (Big Bertha, Slice, Royal Guardian Sword, Attitude Adjuster, Battle Axe, Chainsaw, Queen Scale Battle Axe) haben je ein 16×16-Icon `*small.png` für Inventar und Boden und ein Box-Modell mit eigener Entity-Textur, das **nur in der Hand** gezeichnet wird – und dort absurd groß: 3 bis 7 Blöcke. Die 14 Rüstungssets nutzen die Vanilla-Rüstungsform (Vanilla-Silhouette in den Icons, zwei 64×32-Layer je Set); zwei Sets (Pink Tourmaline, Tiger's Eye) sind reine Kantenzeichnungen, durch die der Skin scheint, und die Hälfte der Sets glintet dauerhaft durch fest verdrahtete Verzauberungen.

## Materialtabelle (manifest `tool_materials` / `armor_materials`, Registrierung OreSpawnMain.java:1292-1306 und 1432-1445)

| Tool-Material | harvestLevel | maxUses | efficiency | damage | enchantability | Items dieses Batches |
|---|---|---|---|---|---|---|
| ULTIMATE | 10 | 3000 | 15 | 36 | 100 | ultimatesword, -pickaxe, -shovel, -hoe, -axe |
| NIGHTMARE | 3 | 1800 | 12 | 26 | 60 | nightmaresword |
| BERTHA | 3 | 9000 | 15 | 496 | 100 | berthasmall, slicesmall |
| ROYAL | 3 | 10000 | 15 | 746 | 150 | royalsmall |
| HAMMY | 5 | 2000 | 15 | 82 | 100 | hammysmall |
| BATTLE | 3 | 1500 | 15 | 46 | 75 | battleaxesmall |
| CHAINSAW | 3 | 1500 | 10 | 56 | 75 | chainsawsmall |
| QUEENBATTLE | 3 | 2200 | 15 | 662 | 100 | queenbattleaxesmall |
| REALEMERALD (`toolEMERALD`) | 3 | 1300 | 10 | 6 | 75 | emerald*, experiencesword, poisonsword, ratsword, fairysword, mantisclaw, rosesword |
| RUBY | 5 | 1500 | 11 | 16 | 85 | ruby* |
| AMETHYST | 4 | 2000 | 11 | 11 | 70 | amethyst*, bighammer |
| CRYSTALWOOD | 2 | 300 | 3 | 2 | 15 | crystalwood* |
| CRYSTALSTONE | 3 | 800 | 6 | 5 | 45 | crystalstone* (Kyanite) |
| CRYSTALPINK | 4 | 1100 | 10 | 7 | 65 | crystalpink* |
| TIGERSEYE | 4 | 1600 | 12 | 8 | 75 | tigerseye_* |

Alle Werte sind Config-Schlüssel `<Material>_damage/_maxuses/_efficiency/_harvestlevel/_enchantability` in Kategorie `OreSpawnWEAPONS` (manifest `config`); die Rüstungen haben 14 Schlüssel je Set in `OreSpawnARMOR`. `damage` ist die reine Materialzahl. **Belegt am Bytecode:** der 1.7.10-`ItemSword`-Konstruktor rechnet `4.0f + material.damage` (client-1.7.10.jar, Klasse `aeh`, `ldc 4.0f; fadd; putfield`), die Tooltip-Zahl ist also Material + 4: Ultimate 40, Nightmare 30, Bertha/Slice 500, Royal 750, Attitude Adjuster 86, Battle Axe 50, Chainsaw 60, Queen 666, Emerald-Familie 10, Ruby 20, Amethyst 15, Crystal Wood 6, Kyanite 9, Pink 11, Tiger's Eye 12. Das deckt sich mit 03-items.md:60-82. Für Werkzeuge (`ItemTool`) gilt eine eigene Addition, hier nicht nachgelesen (offen, für das Design unerheblich).

Klassen überschreiben die Material-Haltbarkeit teils per `setMaxDamage`: Ultimate-Reihe 3000 (UltimateSword.java:29, UltimatePickaxe.java:24, UltimateShovel/Hoe/Axe je Konstruktor), Bertha-Familie 9000 (Bertha.java:18 – gilt auch für Royal, obwohl das Material 10000 sagt), Battle Axe/Chainsaw/Queen Battle Axe 3000 (Klasse `UltimateSword`, Z. 29), Nightmare 1200 (NightmareSword.java, Konstruktor), Experience 1400 (ExperienceSword.java:27), Emerald/Poison/Rat/Fairy 1300, Mantis Claw 1000, Big Hammer 9000, Ruby Sword 1500, Amethyst Sword 2000 (je Konstruktor); die Crystal-Klassen setzen nichts (CrystalSword.java, 27 Zeilen) und nehmen `maxUses` des Materials.

## Icon-Konvention (alle 2D-Items)

- Jedes Item registriert sein Icon als `"OreSpawn:" + unlocalizedName` (Bertha.java:109, ItemOreSpawnArmor.java:367, UltimateSword.java:154). Legacy-Pfad `assets/orespawn/textures/items/<id>.png`, neuer Pfad `assets/orespawn/textures/item/<id>.png` (manifest `textures`, Eintrag `[neu, [w,h], legacy]`).
- Alle 107 Icons sind 16×16 px RGBA (manifest; per PIL über alle 107 geprüft, `Counter({(16,16): 107})`).
- Stil Vanilla-1.7.10: 1-px-Kontur, 45°-Diagonale von unten links nach oben rechts, zwei bis fünf Farbstufen je Material; Schwerter belegen 74 deckende Pixel, Spitzhacken 60–62, Schaufeln 53–54, Hacken 56–57, Äxte 62–65 (PIL-Zählung) – die Formen sind also Vanilla-Vorlagen, nur umgefärbt.

---

### Ultimate-Reihe (5)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| ultimatesword | The Ultimate Sword | items/ultimatesword.png → item/ultimatesword.png | Vanilla-Schwert, Klinge tiefblau (#040e71, 8 px) mit Lavendel (#9e9cfe, 9), Weiß (#fafafc, 9) und Cyan (#4bd4ed, 7) gesprenkelt; dunkelgrauer Griff mit rotem Knaufpixel |
| ultimatepickaxe | The Ultimate Pickaxe | items/ultimatepickaxe.png → item/ultimatepickaxe.png | Vanilla-Spitzhacke, Kopf im selben Blau-Lavendel-Sprenkel, dunkelpetrol Schatten (#16353c) |
| ultimateshovel | The Ultimate Shovel | items/ultimateshovel.png → item/ultimateshovel.png | Vanilla-Schaufel, Blatt cyan-lavendel auf Tiefblau |
| ultimatehoe | The Ultimate Hoe | items/ultimatehoe.png → item/ultimatehoe.png | Vanilla-Hacke, Winkel lavendel-blau |
| ultimateaxe | The Ultimate Axe | items/ultimateaxe.png → item/ultimateaxe.png | Vanilla-Axt, Blatt lavendel-weiß-cyan auf Tiefblau |

**Aussehen.** Vier Töne fast gleich verteilt (7–9 px je Ton) – das Material heißt im Code „Uranium/Titanium" (`getMaterialName`, UltimateSword.java:121-123), liest sich aber als funkelndes Tiefblau. Alle fünf glinten dauerhaft: das Schwert bekommt in `onCreated` und in jedem `onUpdate` ohne Looting-Level Sharpness/Smite/Bane of Arthropods = `UltimateSwordMagic` (Default 5, Config `UltimateSwordEnchantmentLevel`, manifest), Knockback/Looting/Unbreaking = 1 + 5/2 = 3, Fire Aspect = 1 + 5/3 = 2 (UltimateSword.java:33-50, 103-118); Pickaxe Efficiency 5 + Fortune 5 (UltimatePickaxe.java:28-31), Shovel und Axe Efficiency 5, Hoe Efficiency 2 (je `onCreated`).

**Größe.** Flaches 16-px-Sprite: 1 Block Kantenlänge im Modellraum, in der Hand vom Vanilla-Handheld-Display skaliert (Vanilla-Konstante, nicht Mod-Code).

**Portierungshinweise.** Item-Modelle `minecraft:item/handheld` mit `layer0 = orespawn:item/<id>`. Die Selbstverzauberung wird `ItemEnchantments` als Data-Component (Default-Component in `Item.Properties`, `Holder<Enchantment>` aus der Registry – data-driven, Guardrail); der Tick-Nachschub aus `onUpdate` → `inventoryTick` bleibt, weil ein entzauberter Stack im Original sichtbar wieder glintet. `UltimateSwordPvp` (Default 0, manifest) → `ModConfigSpec`; `onLeftClickEntity` gibt bei 0 für Spieler/Girlfriend/Boyfriend/gezähmte Tiere `true` zurück und blockt den Schlag (UltimateSword.java:130-146, gleiches Muster in UltimatePickaxe.java:62-75, UltimateShovel, UltimateAxe). Tier: `harvestLevel 10` + `canHarvestBlock = true` (UltimatePickaxe.java:45-47) → 1.21.1-`Tier` mit leerem `incorrectBlocksForDrops`-Tag. Pickaxe-Bonus (Eisen/Gold-Barren 50 %, 1 % Edelstein aus Stein, UltimatePickaxe.java:91-119) und Hoe-3×3×3-Pflügen (UltimateHoe.java, `onItemUse`) sind Mechanik, gehören aber zum Item. Schadenszahl 1:1: `SwordItem.createAttributes` so parametrieren, dass Modifier = Material + 4 ist (siehe Materialtabelle).

### Emerald-Reihe und Emerald-basierte Spezialschwerter (11)

| id | name | icon | look |
|---|---|---|---|
| emeraldsword | Emerald Sword | item/emeraldsword.png | Vanilla-Schwert, Klinge in fünf Grüntönen (#008516 … #19d938), dunkelgrauer Griff |
| emeraldpickaxe | Emerald Pickaxe | item/emeraldpickaxe.png | Vanilla-Spitzhacke, grüner Kopf (#0fbd2c/#05931c) |
| emeraldshovel | Emerald Shovel | item/emeraldshovel.png | Vanilla-Schaufel, grünes Blatt |
| emeraldhoe | Emerald Hoe | item/emeraldhoe.png | Vanilla-Hacke, grüner Winkel |
| emeraldaxe | Emerald Axe | item/emeraldaxe.png | Vanilla-Axt, grünes Blatt (#26f348 hell) |
| experiencesword | Experience Sword | item/experiencesword.png | wie Emerald Sword, aber Klinge gelbgrün leuchtend (#e0f902, #a6f902 auf #04a21e) – Bottle-o'-Enchanting-Grün |
| poisonsword | Poison Sword | item/poisonsword.png | Klinge schwarz (#000000, 13 px) mit orange-braunen Querstreifen (#d78505/#b87306/#965d04) und grünen Spitzen – Stink-Bug-Muster |
| ratsword | Rat Sword | item/ratsword.png | Klinge grau-braun (#645555, 17 px) mit schwarzen Fellstreifen (#0b0a0a), hellgraue Spitze, brauner Griff (#583307) |
| fairysword | Fairy Sword | item/fairysword.png | Klinge apricot (#fbc087, 16 px) mit cremeweißen und hellblauen Pixeln (#fbf9f1, #71aaef), schwarzer Griff |
| mantisclaw | Mantis Claw | item/mantisclaw.png | gebogene, breite Sichel in Grün (#308e07/#267205) mit weißen Zackenkanten (#e8f6e2, 22 px); einzige Nicht-Schwert-Silhouette der Gruppe, 85 px |
| rosesword | Rose Sword | item/rosesword.png | Klinge in vier Rottönen (#d50909 … #770404, 90 px – breiter als Vanilla) mit grünem Blattpixel; Klasse `EmeraldSword` |

**Aussehen.** Fünf Werkzeuge in reinem Smaragdgrün, dazu sechs Schwerter auf `toolEMERALD` (manifest `ctor_args`), die sich nur über die Klingentextur unterscheiden. Glint: Experience Sword (Sharpness 2 + Unbreaking 3, ExperienceSword.java:31-34) und Poison Sword (Sharpness 1, PoisonSword.java `onCreated`) glinten immer; Emerald Pickaxe durch Silk Touch 1 (EmeraldPickaxe.java `onUsingTick`). Emerald Sword, Rat, Fairy, Mantis Claw, Rose Sword und die übrigen Emerald-Werkzeuge haben keinen Glint (kein `addEnchantment` in EmeraldSword.java, RatSword.java, FairySword.java, MantisClaw.java).

Sichtbare Treffer-Effekte: Experience Sword sprüht `portal`-Partikel über dem Ziel, Anzahl ≈ Spielerlevel/4 (ExperienceSword.java:112-135) und gibt +10 XP je Treffer auf ein `EntityLiving` (Z. 119-124); Rat Sword spawnt 1–6 Ratten (RatSword.java `hitEntity`, `1 + rand.nextInt(6)`), Fairy Sword 1–3 Feen (`1 + rand.nextInt(3)`) direkt am Ziel, beide `setOwner(Angreifer)`; Poison Sword gibt Poison/Wither/Weakness je 10–19 s (PoisonSword.java `hitEntity`, `10 + rand.nextInt(10)` Sekunden × 20); Mantis Claw heilt den Angreifer um 1 und zieht dem Ziel 1 ab (MantisClaw.java `hitEntity`, `heal(-1)`/`heal(1)`).

**Portierungshinweise.** Alle elf `item/handheld`. Die Spawn-Schwerter brauchen die Entity-Typen `Rat` und `Fairy` (anderer Batch); `spawnCreature` wird `EntityType.spawn` + Owner-Setzen. Experience Sword hält zwei `World`-Referenzen im Item-Singleton (ExperienceSword.java:17-18, 49-54) – im Port `Level` aus dem Aufruf nehmen, nie im Item speichern.

### Ruby- und Amethyst-Reihe, Big Hammer (11)

| id | name | icon | look |
|---|---|---|---|
| rubysword | Ruby Sword | item/rubysword.png | Vanilla-Schwert, Klinge in fünf Rottönen (#570507 … #de0309), dunkelgrauer Griff |
| rubypickaxe | Ruby Pickaxe | item/rubypickaxe.png | Vanilla-Spitzhacke, roter Kopf |
| rubyshovel | Ruby Shovel | item/rubyshovel.png | Vanilla-Schaufel, rotes Blatt |
| rubyhoe | Ruby Hoe | item/rubyhoe.png | Vanilla-Hacke, roter Winkel |
| rubyaxe | Ruby Axe | item/rubyaxe.png | Vanilla-Axt, rotes Blatt |
| amethystsword | Amethyst Sword | item/amethystsword.png | Vanilla-Schwert, Klinge in fünf Violetttönen (#330553 … #9d18f9) |
| amethystpickaxe | Amethyst Pickaxe | item/amethystpickaxe.png | Vanilla-Spitzhacke, violetter Kopf |
| amethystshovel | Amethyst Shovel | item/amethystshovel.png | Vanilla-Schaufel, violettes Blatt |
| amethysthoe | Amethyst Hoe | item/amethysthoe.png | Vanilla-Hacke, violetter Winkel |
| amethystaxe | Amethyst Axe | item/amethystaxe.png | Vanilla-Axt, violettes Blatt |
| bighammer | Big Hammer | item/bighammer.png | Vorschlaghammer: rechteckiger dunkelgrauer Kopf (#33302d/#0d0c0c) mit gelbem Band (#b7a408), brauner Holzstiel (#944d08) |

**Aussehen.** Ruby und Amethyst sind die farbreinsten Sets: keine Sprenkel, nur Helligkeitsstufen des Materials. Der Big Hammer sitzt auf `toolAMETHYST` (manifest `ctor_args`), sieht aber nach Eisen und Holz aus; er ist Zutat für Big Bertha Handle und Attitude Adjuster (03-items.md:288, 328). Kein Glint in der Gruppe. Big-Hammer-Treffer werfen das Ziel senkrecht hoch (`addVelocity(0, |rand·2/3|, 0)`, BigHammer.java `hitEntity`).

**Portierungshinweise.** `item/handheld`; Big Hammer bleibt ein `SwordItem` mit Amethyst-Tier und `hurtEnemy`-Override.

### Kristall-Dimension: Crystal Wood, Pink Tourmaline, Kyanite, Tiger's Eye (16)

| id | name | icon | look |
|---|---|---|---|
| crystalwoodsword | Crystal Wood Sword | item/crystalwoodsword.png | Vanilla-Schwert, Klinge elfenbein/beige (#f6cf84, #f6e0b4) mit weißer Kante (#f6f4f1), 51 px |
| crystalwoodpickaxe | Crystal Wood Pickaxe | item/crystalwoodpickaxe.png | Vanilla-Spitzhacke, beiger Kopf mit weißem Rand |
| crystalwoodhoe | Crystal Wood Hoe | item/crystalwoodhoe.png | Vanilla-Hacke, beiger Winkel |
| crystalwoodaxe | Crystal Wood Axe | item/crystalwoodaxe.png | Vanilla-Axt, beiges Blatt |
| crystalpinksword | Pink Tourmaline Sword | item/crystalpinksword.png | Vanilla-Schwert, Klinge magenta (#a60bb0) mit weißer Kante |
| crystalpinkpickaxe | Pink Tourmaline Pickaxe | item/crystalpinkpickaxe.png | Vanilla-Spitzhacke, magenta-rosa Kopf (#d038d9/#e870ef) |
| crystalpinkhoe | Pink Tourmaline Hoe | item/crystalpinkhoe.png | Vanilla-Hacke, magenta Winkel |
| crystalpinkaxe | Pink Tourmaline Axe | item/crystalpinkaxe.png | Vanilla-Axt, magenta Blatt |
| crystalstonesword | Kyanite Sword | item/crystalstonesword.png | Vanilla-Schwert, Klinge kobaltblau (#0a1aed) mit weißer Kante |
| crystalstonepickaxe | Kyanite Pickaxe | item/crystalstonepickaxe.png | Vanilla-Spitzhacke, Kopf blau in vier Stufen bis Cyan (#0a1aed … #0acaed) |
| crystalstonehoe | Kyanite Hoe | item/crystalstonehoe.png | Vanilla-Hacke, blau-cyan Winkel |
| crystalstoneaxe | Kyanite Axe | item/crystalstoneaxe.png | Vanilla-Axt, blaues Blatt mit Cyan-Kante |
| tigerseye_sword | Tiger's Eye Sword | item/tigerseye_sword.png | Vanilla-Schwert, Klinge dunkelbraun (#0a0701) mit weißer Kante – dunkler als die Werkzeuge |
| tigerseye_pickaxe | Tiger's Eye Pickaxe | item/tigerseye_pickaxe.png | Vanilla-Spitzhacke, Kopf ocker-gold (#d9a94d/#f8ce7d/#bc8b2b) |
| tigerseye_hoe | Tiger's Eye Hoe | item/tigerseye_hoe.png | Vanilla-Hacke, goldener Winkel |
| tigerseye_axe | Tiger's Eye Axe | item/tigerseye_axe.png | Vanilla-Axt, goldenes Blatt |

**Aussehen.** Die vier Kristallstufen teilen die Klassen `CrystalSword`/`CrystalPickaxe`/`CrystalHoe`/`CrystalAxe` (manifest `class`) und unterscheiden sich nur in Material und Icon. Gemeinsames Stilmerkmal: ein weißer Kantensaum (#f6f4f1) um den Materialkopf, den Emerald/Ruby/Amethyst nicht haben – die Kristallwerkzeuge lesen sich als „geschliffen"; die Schwerter sind mit 51–52 px schlanker als die 74-px-Vanilla-Klinge der anderen Reihen. Kein Glint (keine `addEnchantment`-Aufrufe in den Crystal*-Klassen, je 20–27 Zeilen).

**Hinweis zu den Schaufeln.** `crystalwoodshovel`, `crystalpinkshovel`, `crystalstoneshovel`, `tigerseye_shovel` existieren (Klasse `CrystalShovel`), sind im Manifest Kind `item` und damit **nicht** in diesem Batch; visuell gehören sie in diese Familie.

**Portierungshinweise.** `item/handheld`; die Tiers als `Tier`-Implementierungen mit den Zahlen der Materialtabelle; `CrystalShovel` wird im Port ein normales `ShovelItem`.

### Nightmare Sword (1)

| id | name | icon | look |
|---|---|---|---|
| nightmaresword | Nightmare Sword | item/nightmaresword.png | drei parallele schwarze Klingen (#000000, 85 px) mit dunkelroten Kernen (#7f0000, 22) und grauen Kanten (#404040, 38) an einer gemeinsamen Parierstange; mit 168 deckenden Pixeln das dichteste Icon des Batches |

**Aussehen.** Dreiklingen-Schwert in Schwarz/Rot – die Farben des Nightmare-Mobs (Nightmare Scale ist Zutat, 03-items.md:325). Glint immer an: Sharpness 1, Knockback 3, Fire Aspect 1 (NightmareSword.java `onCreated`, Nachschub in `onUsingTick`/`onUpdate`). Haltbarkeit 1200 aus der Klasse, nicht 1800 aus dem Material.

**Abweichung zur Research.** 03-items.md:68 nennt ein „three-bladed model"; im Code gibt es für das Nightmare Sword **keinen** `IItemRenderer` (ClientProxyOreSpawn.java:149-156 registriert nur die sieben unten) und kein `Model*`. Die drei Klingen sind allein das 2D-Icon.

**Portierungshinweise.** `item/handheld`, Default-Enchantments als Component wie beim Ultimate Sword.

### 3D-Waffen mit IItemRenderer (7)

| id | name | icon (Inventar/Boden) | 3D-Modell | 3D-Textur (legacy → neu, manifest `item_renderer.textures`) | look |
|---|---|---|---|---|---|
| berthasmall | Big Bertha | item/berthasmall.png | ModelBertha (12 Boxen, 64×128) | assets/orespawn/Berthatexture.png → textures/entity/berthatexture.png | schwarze Klinge mit rotem Kern und weißer Kante, brauner Parierstab, goldene Nieten, schwarzer Griff |
| slicesmall | Slice | item/slicesmall.png | ModelSlice (14 Boxen, 64×128) | Slicetexture.png → textures/entity/slicetexture.png | vier fächerförmig gespreizte hellgraue Klingen (#b8b1b1), schwarzer Griff |
| royalsmall | Royal Guardian Sword | item/royalsmall.png | ModelSlice (14 Boxen, 64×128) | Royaltexture.png → textures/entity/royaltexture.png | dieselbe Vier-Klingen-Form wie Slice, Klingen weiß (#ffffff) mit gelbgoldenem Saum (#e7d011), goldbrauner Griffsockel |
| hammysmall | Attitude Adjuster | item/hammysmall.png | ModelHammy (33 Boxen, 128×256) | AttitudeAdjustertexture.png → textures/entity/attitudeadjustertexture.png | riesiger Kriegshammer: **rotbrauner Holzkopf** in Parkett-Maserung mit schwarzen Metallbändern, weißgrauen Spikes und Rauten, drei orange-hölzerne Schaftlatten |
| battleaxesmall | Battle Axe | item/battleaxesmall.png | ModelBattleAxe (15 Boxen, 128×64) | BattleAxetexture.png → textures/entity/battleaxetexture.png | Doppelaxt: silbergraue Klingen (#c5c6cd/#a8aab3/#f4f4f4), dunkle Nabe (#3d3333) mit rotem Punkt, goldgelber Griff (#ffcc66), graubrauner Stiel (#6d5e5e) |
| chainsawsmall | Chainsaw | item/chainsawsmall.png | ModelChainsaw (8 Boxen, 64×64, animiert) | Chainsawtexture.png → textures/entity/chainsawtexture.png | Kettensäge: weißgrauer Motorblock (Mittel #c2c7cf), dunkelrote Griffe, dunkelvioletter Auspuff, dunkelgraues Schwert mit gelbem „SLYR"-Schriftzug (#e5dd05, 52 px), **rote** Zähne (#9f0707) |
| queenbattleaxesmall | Queen Scale Battle Axe | item/queenbattleaxesmall.png | ModelQueenBattleAxe (9 Boxen, 128×64) | QueenBattleAxetexture.png → textures/entity/queenbattleaxetexture.png | vier gefächerte schwarze Klingenplatten (#030000) mit rotem Saum (#82050d/#530208), schwarzer Knauf, dunkelbrauner Holzgriff (#543122), grauer Stiel |

Box-Zahlen und Texturgrößen aus `reference/jar/models/<Model>.json` (`part_count`, `textureWidth/Height`), bestätigt durch 06-models-design.md:57-136 („PNG size equals the declared model size"). Registrierung: ClientProxyOreSpawn.java:149-156 (`MinecraftForgeClient.registerItemRenderer`).

Die Icons `*small.png` sind eigene 16×16-Zeichnungen, nicht das Modell verkleinert: berthasmall = schwarze Klinge (#030200, 47 px) mit rotem Kern (#ff0000, 10), braunem Parierstab (#583401) und Gold (#ffb401); slicesmall = hellgraue Klinge (#cbc6c5/#a09998) mit schwarzem Heft; royalsmall = weiße Klinge (#edece4) mit goldenem Saum (#bcad06); hammysmall = **dunkelgrauer** Mallet-Kopf (#0d0c0c/#33302d) auf braunem Stiel – das Icon widerspricht der Holzoptik des 3D-Kopfes; battleaxesmall = silbergraue Doppelklinge (#c5c6cd, 42 px) mit rotem Punkt und Holzstiel; chainsawsmall = weißgrau-rot-schwarz mit gelbem Pixel; queenbattleaxesmall = schwarze Rundklinge (#0b0100, 42 px) mit dunkelrotem Kern (#8a0000) und braunem Stiel.

#### Handtransformationen und Größe – gemeinsames Verfahren

Alle sieben Renderer haben dieselbe Struktur (RenderBertha.java:18-69, RenderChainsaw.java:18-70): `handleRenderType` liefert `true` nur für `EQUIPPED` und `EQUIPPED_FIRST_PERSON`, `shouldUseRenderHelper` immer `true`, `renderItem` ruft `renderSwordF5(x,y,z,scale)` (dritte Person) bzw. `renderSword(x,y,z,scale)` (erste Person); beide machen `glPushMatrix → glRotatef(…) → glScalef(scale) → glTranslatef(x,y,z) → bindTexture → model.render() → glPopMatrix`. `model.render()` rendert mit `f5 = 1.0` (ModelBertha.java `render`, ebenso alle anderen): **eine Modell-Einheit ist ein Block**, nicht 1/16.

Außen herum liegt die Vanilla-Hand-Matrix. Am Bytecode von client-1.7.10.jar belegt: `RenderPlayer` (Klasse `bop`) für die dritte Person `glTranslatef(-0.0625, 0.4375, 0.0625)`, dann im 3D-Zweig `glTranslatef(0, 0.1875, -0.3125)`, `glRotatef(20, x)`, `glRotatef(45, y)`, `glScalef(±f1)` mit `f1 = 0.5 × 0.75 = 0.375` (Konstantenfolge 0.5, 0.1875, -0.3125, 0.75, 20, 45 vor `glScalef`); `ItemRenderer` (Klasse `bly`) für die erste Person `glRotatef(…20…)`, `glRotatef(…80…)`, `glScalef(0.4)`. Dass der 3D-Zweig für Forge-`IItemRenderer` mit `shouldUseRenderHelper == true` genommen wird, ist Forge-Patch-Wissen und hier nicht am Bytecode geprüft (offen, aber konsistent mit den absurden Größen im Spiel).

**Größenformel (dritte Person):** Modell-Einheiten × Renderer-`scale` × 0.375. Erste Person: × 0.4 statt 0.375 (im Sichtraum vor der Kamera, nicht direkt vergleichbar).

| Waffe | Länge in Einheiten (Geometrie-JSON) | scale | ≈ Blöcke (3. Person) |
|---|---|---|---|
| Big Bertha | y −43 … 7 = 50 (Blade 34, Grip 12, Tip 2, Bottom 1) | 0.25 | **4,7** |
| Slice | y −46 … 7 = 53 (Blade 34 + Shape1 bis −46) | 0.30 | **6,0** |
| Royal Guardian Sword | 53 (gleiches Modell) | 0.35 | **7,0** |
| Attitude Adjuster | y −29 … 24 = 53 (Spike2 −29, Handle bis 24); Kopf 40 breit | 0.15 | **3,0** lang, Kopf **2,3** breit |
| Battle Axe | Stiel 31 (Handle1, 90° um Z), Kopfspanne x −12,2 … 11,5 ≈ 24 | 0.35 | **4,1** lang, **3,1** breit |
| Queen Scale Battle Axe | Stiel 31, Klingenplatten 20 breit | 0.35 | **4,1** lang, **2,6** breit |
| Chainsaw | z −28 … 8 = 36 (blade1 −28 … −4, engine −4 … 4, handle1 3 … 8) | 0.25 | **3,4** |

#### Big Bertha – ModelBertha

Geometrie (ModelBertha.json, alle Teile ohne Rotation, Pivot 0/0/0): `Grip` 1×12×1 bei (0,−6,0); `Blade` 1×34×3 bei (0,−41,−1); `Handguard2` 1×1×9 bei (0,−7,−4) und `Handguard1` 7×1×1 bei (−3,−7,0) bilden ein Kreuz, dazu vier Nieten `hg1..hg4` 1×1×1 an den Kreuzenden (y −8); `BaseGrip` 3×1×3 bei (−1,5,−1); `Tip1` 1×1×2 und `Tip2` 1×1×1 als Spitze bei y −42/−43; `Bottom` 1×1×1 bei (0,6,0). Textur 64×128: Klinge UV 6/0 schwarz (#030200, 124 px) mit rotem Kern (#ff0000, 62) und weißer Kante (#ffffff, 68); Griff UV 0/0 schwarz; Parierstab UV 16/0 und 18/12 sowie BaseGrip UV 0/39 braun (#583401); Nieten UV 0/15–0/24 gold (#ffb401). Der Rest der 64×128-Fläche ist leer.

| Kontext | Reihenfolge (RenderBertha.java:49-69) | Werte (Z. 39, 43) |
|---|---|---|
| EQUIPPED_FIRST_PERSON (`renderSword`) | rotate X 190° → rotate Z 25° → scale → translate | scale 0.25, translate (6, 3, −5) |
| EQUIPPED (`renderSwordF5`) | rotate X 90° → rotate Z −90° → scale → translate | scale 0.25, translate (−4, 2, −3) |

#### Slice und Royal Guardian Sword – ModelSlice

Ein Modell, zwei Texturen (ModelSlice.json `ctor_sites`: RenderRoyal, RenderSlice). Griffteile wie Bertha (`Grip`, `Handguard1/2`, `BaseGrip`, `Bottom`), aber die Nieten `hg1..hg4` sind 3×3×1 bzw. 1×3×3 (Würfelchen an den Kreuzenden, y −9) und statt einer Klinge stehen **vier** Klingen 1×34×3 bei y −41, je ±20° um Y gedreht (`Blade1` +20°, Pivot (0.5,0,−2.3); `Blade2` −20°, (0.5,0,−2.3); `Blade3` −20°, (1.5,0,0.4); `Blade4` +20°, (−1.5,0,0.7)) – ein Fächer. `Shape1` 1×6×3 bei Pivot (0.5,−40,−1) setzt die Spitze auf (bis y −46). Slicetexture: alle vier Klingenstreifen (UV 6/49, 15/49, 24/49, 33/49) einheitlich hellgrau #b8b1b1 (93 von 93 Texeln), Griff schwarz, BaseGrip dunkelrotbraun; die Nieten-UVs (0/15 ff.) sind fast leer (5 von 48 Texeln gedeckt) – die Würfelchen sind bei Slice praktisch unsichtbar. Royaltexture: Klingen weiß #ffffff (62) mit gelbgoldenem Saum #e7d011 (31), BaseGrip goldbraun (#604804), Nieten fast weiß.

Transformationen identisch zu Bertha (rotate X 190°, Z 25° / rotate X 90°, Z −90°; translate (6,3,−5) bzw. (−4,2,−3)), nur die Skalierung wächst mit dem Rang: **Slice 0.3** (RenderSlice.java:39,43), **Royal 0.35** (RenderRoyal.java:39,43).

#### Attitude Adjuster – ModelHammy

33 Boxen, Textur 128×256 (ModelHammy.json). Schaft: drei Latten `Handle1..3` 1×36×2 bei (−0.5,−12,−1), um 0°, +60°, −60° um Y gedreht – ein sechsstrahliger Schaft (UV 0/0, 7/0, 14/0: orange Holz, Mittel #c66e3a). Kopf: `Head1` 40×6×14 bei (−20,−22,−7), `Head2` 40×14×6 bei (−20,−26,−3), `Head3`/`Head4` 40×6×14 um ±45° um X – zusammen ein **achteckiger Kopf 40 Einheiten breit**. **Die Kopf-UVs 0/161, 0/184, 0/207, 0/230 liegen in den Parkett-Zeilen der Textur: rotbraune Holzbretter (Mittel #773d32 über 1768 von 1840 Texeln), kein Metall.** Je Seite acht `Band`-Platten 5×7×1 (Band1..8 bei x 12, Band1b..8b bei x −17, UV 0/79–0/148: schwarz #232121) als Ringe um beide Kopfenden; `Point1`/`Point1b` 5×5×1 um 45° um Z (UV 28/130: weißgrau #e1dbdb) als Rauten auf den Stirnseiten; acht `Spike`-Stäbe 1×20×1 bzw. 1×1×20 (UV 49–67/0: weißgrau #e1dbdb), teils ±45° um X. Der Attitude Adjuster ist also ein Holzhammer mit schwarzen Eisenbändern und weißen Dornen – das Icon zeigt dagegen einen dunkelgrauen Kopf (siehe oben).

Transformationen (RenderHammy.java:39, 43, 49-69), Scale **0.15**: erste Person rotate Y 70° → rotate X 190° → rotate Z 25° → scale → translate (−10, −13, −5); dritte Person rotate X 180° → scale → translate (6, −20, −4).

#### Battle Axe – ModelBattleAxe

15 Boxen, 128×64, alle Pivots (0,−12,0) außer Blade6 (0,−13,0) (ModelBattleAxe.json). `Handle1` 31×2×1 bei (−7,−0.5,0), 90° um Z → senkrechter Stiel; `Grip` 3×11×2 bei (−1.92,13,−0.5) darunter; `Head1` 3×4×2, `Top` 3×2×2, `Pin` 1×1×3 als Nabe. Zwei Klingenpaare aus `Blade1/2` (3×10×1, +29°/−29° um Z, rechts) und `Blade6/9` (links), dazu die Kanten `Blade3..5,7,8,10` als 7–10 lange 1×1-Leisten mit ±29° – aufgefächerte Halbmonde aus Leisten. Textur: Stiel UV 0/0 graubraun (#6d5e5e, 190 px), Grip UV 0/7 goldgelb (#ffcc66/#ffae4c) mit dunklen Wickelstreifen, Klingen UV 70/0 silber (#c5c6cd/#a8aab3/#f4f4f4), Nabe UV 29/18 dunkel (#3d3333), Pin UV 38/11 mit rotem Pixel.

Transformationen (RenderBattleAxe.java:39, 43, 49-69), Scale **0.35**: erste Person rotate Y 50° → rotate X 190° → rotate Z 15° → scale → translate (−2, −4, −6); dritte Person rotate 180° um die Achse (1, 0.25, 0) → scale → translate (3, −8, −2).

#### Queen Scale Battle Axe – ModelQueenBattleAxe

9 Boxen, 128×64 (ModelQueenBattleAxe.json). Stiel wie Battle Axe (`Handle1` 31×1×1, 90° um Z; `Grip` 2×11×2; `Head1`, `Pin`; `Top` 2×2×2 bei Pivot (−1.5,−21,−0.5) als Knauf). Statt Leisten vier volle Klingenplatten `Blade1..4` 20×4×1 bei (−10,−2,0), Pivot (−0.5,−14.5,0), um −34°, −11°, +12°, +34° um Z gedreht – ein **Fächer aus vier 20 Einheiten breiten Platten**. Textur: Klingen UV 70/0 schwarz (#030000, 140 von 210 Texeln) mit rotem Saum (#82050d, #530208, #290003), Knauf UV 13/4 schwarz, Griff UV 0/7 dunkelbraunes Holz (Mittel #543122), Stiel UV 0/0 dunkelgrau (#252424). Die Queen-Farben Schwarz/Rot wie beim Rüstungsset.

Transformationen identisch zur Battle Axe, Scale **0.35** (RenderQueenBattleAxe.java:39, 43, 51-63).

#### Chainsaw – ModelChainsaw (animiert)

8 Boxen, 64×64 (ModelChainsaw.json): `engine` 4×7×8 bei (−2,−4,−4); `handle1` 1×1×5 (−11° um X), `handle2` 1×1×4, `handle3` 1×4×1 (−5° um X) als hinterer Bügelgriff bei z 3…8; `muffler` 1×3×3 bei (−3,0,1); `blade1` 1×4×24 bei (0,−2,−28) – das Sägeschwert nach vorn (−z); `blade2` 1×5×5 mit Pivot (0,0,−28) als Umlenkrolle an der Spitze; `tooth` 1×1×1 mit Pivot (0,−2,−5). Textur: engine UV 0/19 weißgrau gemasert (Mittel #c2c7cf), blade1 UV 0/35 dunkelgrau mit dem gelben Schriftzug **„SLYR"** (#e5dd05, 52 px, Zeilen 58–63 – auf beiden Längsseiten des Schwerts lesbar; vergrößerter Ausschnitt geprüft, 03-items.md:66 „SLYR Chainsaw" stimmt), Griffe UV 49/0, 50/13, 52/7 dunkelrot (Mittel #480908), muffler UV 14/0 dunkelviolett (#2a193b), **tooth UV 0/0 rot #9f0707**.

Animation im `render()` (ModelChainsaw.java, `render` und `renderTooth..renderTooth5`): `blade2.rotateAngleX += 0.10471975` (6°) pro Frame, Reset bei > 2π – die Spitzenrolle dreht dauerhaft. Die eine `tooth`-Box wird **sechsmal** gerendert, jede mit eigenem Zustand `toothposN`/`toothdirN` (Felder Z. 15-26, Startwerte Z. 29-40: 0/7/14 vorwärts, 20/13/6 rückwärts): auf der Oberkante (`rotationPointY = −2`) wandert `rotationPointZ = −5 − toothpos` in 0.5er-Schritten bis 21, dann auf der Unterkante (`rotationPointY = 3`) zurück – eine umlaufende Kette aus sechs roten Zähnen. Die Animation läuft **framebasiert, ohne Tick-Bezug und dauerhaft**, auch ohne Benutzung.

Transformationen (RenderChainsaw.java:39, 43, 49-70), Scale **0.25**: erste Person rotate Y 150° → rotate X 100° → scale → translate (−10, 1, −4); dritte Person rotate Z 180° → rotate Y −20° → rotate X −20° → scale → translate (−3, −3, −2).

Hör- und sichtbare Beigaben (UltimateSword.java:52-58, 82-101): beim Schwingen `orespawn:chainsawshort` mit Pitch 0.9–1.1, wenn `swingtimer == 0`, danach 50 Ticks lang je Tick `flame` (1/8), `smoke` (1/2) und `fireworksSpark` (1/10) am Spieler, versetzt um 1 Block bei Yaw + 135°. `swingtimer` ist ein Feld des Item-Singletons (Z. 21), also global für alle Kettensägen.

#### Verhalten, das man sieht (Swing-Projektil)

Big Bertha, Slice, Royal und Attitude Adjuster (alle Klasse `Bertha`) spawnen bei jedem Schwung ein `BerthaHit`-Projektil 2 Blöcke vor dem Spieler auf Kopfhöhe + 1.55 mit verdoppelter Geschwindigkeit (Bertha.java:69-92); `setHitType(2)` für Royal, `(3)` für Hammy (Z. 82-87). Battle Axe, Chainsaw und Queen Scale Battle Axe (Klasse `UltimateSword`) haben **kein** Swing-Projektil; die Chainsaw trifft bei Linksklick jedes `EntityLivingBase` in der um 5 Blöcke erweiterten Spieler-AABB mit `chainsaw_stats.damage` (UltimateSword.java:157-169) und fällt beim Blockabbau alle Holz-/Laubblöcke in der Box x ±5, y −5…+10, z ±5 (Z. 265-286).

Glint: Bertha/Slice Knockback 5 + Bane of Arthropods 1 + Fire Aspect 1, Royal Unbreaking 5, Hammy **keine** (Bertha.java:22-31); Battle Axe Looting 3 + Unbreaking 3, Queen Battle Axe der volle Ultimate-Satz, Chainsaw **keine** (UltimateSword.java:33-50). Attitude Adjuster und Chainsaw sind die einzigen 3D-Waffen ohne Glint.

**Portierungshinweise (alle sieben).**
- Zwei Darstellungen je Item wie im Original: flaches Sprite für GUI/Boden/Rahmen, Modell nur in der Hand. Sauberster Weg ist ein `neoforge:separate_transforms`-Item-Modell (Basis = `item/handheld` mit `*small`-Sprite; Perspektiven `firstperson_*`/`thirdperson_*` → `{"parent": "minecraft:builtin/entity"}`) plus `IClientItemExtensions#getCustomRenderer` mit einem `BlockEntityWithoutLevelRenderer` unter `com.swbr.orespawn.client`; Loader-Name und JSON-Form vor dem Schreiben im NeoForge-Jar gegenprüfen, nicht aus dem Gedächtnis. Alternativ im BEWLR nach `ItemDisplayContext` verzweigen und für GUI/GROUND/FIXED das Sprite-Modell über `ItemRenderer` zeichnen.
- `LayerDefinition`s 1:1 aus `models/<Model>.json` (Boxen, Pivots, Rotationen, UVs, Texturgrößen). Achtung Einheiten: 1.21.1-`ModelPart.render` teilt Vertices intern durch 16, das Original rendert mit `f5 = 1.0`. Vor `model.renderToBuffer` also zusätzlich `poseStack.scale(16)` – effektiv Bertha 0.25 × 16 = 4, Slice 4.8, Royal 5.6, Hammy 2.4, Battle Axe/Queen 5.6, Chainsaw 4 – sonst ist alles 16-mal zu klein.
- GL-Reihenfolge `glRotatef → glScalef → glTranslatef` in `PoseStack` **gleich** nachbilden (`mulPose(Axis.XP.rotationDegrees(190))`, `scale`, `translate`); die Translation ist im Original bereits gedreht und skaliert, nicht „intuitiv" umsortieren. Für die linke Hand (1.7.10 hatte keine) x-spiegeln.
- Die umgebende 1.7.10-Hand-Matrix (0.375-Zweig, 0.4-Zweig, s. o.) existiert in 1.21.1 nicht; `ItemInHandRenderer` und die `builtin/entity`-Display-Transforms sind andere Ausgangslagen. Die Zieltabelle „≈ Blöcke" ist die Abnahmegröße: der Port stimmt, wenn Big Bertha in der dritten Person rund 4,7 Blöcke lang ist.
- Chainsaw-Animation: Zustände als Felder des Client-Renderers halten (ein Renderer pro Client, es gibt keine Entity). Framebasiert wie im Original oder über `partialTick`/`gameTime` framerate-unabhängig – Entscheidung offen; `swingtimer` und Partikel gehören in Spieler- oder Stack-Zustand statt ins Item.
- `BerthaHit` (Entity-Batch) ist unsichtbar bis auf ein `spinners.png`-Sprite (06-models-design.md:219).

### Rüstungen (14 Sets × 4 = 56)

Alle 56 sind Instanzen von `ItemOreSpawnArmor` (manifest `class`) mit `armor_type` 0–3 = Helm/Brust/Beine/Stiefel (ItemOreSpawnArmor.java:64) und `armor_material`-Index 0–13 (Z. 24-63; 0 = Ultimate, 3 = Emerald als Default-Fallback in `getArmorTexture`). Die Icons folgen alle der Vanilla-Rüstungssilhouette (78/132/112/86 deckende Pixel für Helm/Brust/Beine/Stiefel bei jedem Vollset; Pink und Tiger's Eye 66/85/84/68, weil ihr Inneres transparent ist). Die getragene Textur kommt aus `getArmorTexture` (Z. 254-339): `<set>_1.png` für Helm, Brust und Stiefel, `<set>_2.png` für die Beine – exakt Vanillas Layer-1/Layer-2-Aufteilung. Beide Layer sind 64×32 px (alle 28 mit PIL geprüft) und liegen legacy direkt unter `assets/orespawn/<set>_N.png`, neu laut manifest `texture_map` unter `assets/orespawn/textures/entity/<set>_N.png`. Ein Vollset deckt 648–700 der 2048 Texel des Layer-1-Atlas; Pink 348 und Tiger's Eye 347 – die Hälfte, weil nur Kanten gezeichnet sind.

| Set (Prefix) | ids | names (manifest) | Schutz H/C/L/B | Faktor | Layer-Look (getragen; Mittelfarbe Layer 1) | Icon-Look |
|---|---|---|---|---|---|---|
| ultimate | ultimate_helmet, ultimate_chest, ultimate_leggings, ultimate_boots | The Ultimate Helmet/Chestplate/Leggings/Boots | 6/12/10/6 | 200 | Tiefblau (#04115a) mit Sprenkeln in Rot, Grün, Cyan, Gelb – die Sprenkelung der Ultimate-Werkzeuge | blau (#011b9f/#000c5e) mit bunten Punkten |
| lavaeel | lavaeel_* | Lava Eel … | 2/7/5/2 | 40 | Dunkelrot (#4d0700) mit orangen Lava-Rissen (#b24305) | dunkelrot mit orangen Punkten |
| mothscale | mothscale_* | Moth Scale … | 2/7/5/2 | 50 | Schwarz (#1a0800) mit orangen und gelben Glutflecken (#b24305, #9c9206) | schwarz mit orange-gelben Punkten |
| emerald | emerald_* | Emerald … | 3/8/6/3 | 60 | Smaragd (#09682c) in diagonalen Adern aus vier Grüntönen (#064210, #0d6a5a, #0fbd2c, #05931c) | grün geädert |
| experience | experience_* | Experience … | 5/9/7/4 | 70 | wie emerald (#116b29) plus gelbgrüne Glühpixel | grün mit Gelb |
| ruby | ruby_* | Ruby … | 4/9/8/4 | 90 | Rubinrot (#780f10) mit schwarzen Schrägstreifen | dunkelrot mit schwarzen Streifen (Icon-Rot verläuft in Dutzende Töne) |
| amethyst | amethyst_* | Amethyst … | 4/8/7/3 | 100 | Violett (#7f22bb) weich gefleckt in fünf Stufen (#490976 … #9d18f9) | violett |
| pink | pink_* | Pink Tourmailine … (sic, manifest) | 3/7/5/2 | 50 | **nur Kanten**: magenta Rahmenlinien (#d63cd5; #e10eda/#f94ef3) um transparente Flächen – der Skin scheint durch | magenta Umriss (#f94ef3) mit hellrosa Füllung |
| tigerseye | tigerseye_* | Tiger's Eye … | 4/8/7/4 | 80 | **nur Kanten**: ocker-goldene Linien (#bc9359; #d9a94d/#f8ce7d) mit weißen Glanzpixeln um transparente Flächen | goldener Umriss, Helm mit Visierbalken |
| peacock | peacock_helmet, peacock_chest, peacock_leggings, peacock_boots | Peacock Feather … | 2/5/4/2 | 40 | Pfauenauge-Muster: Grün (#006400) und Blau (#0b38c9) mit gelb-orange-roten Augenflecken (Mittel #677f68) | grün-blau mit bunten Augen; Stiefel mit Regenbogenpunkten |
| mobzilla | mobzilla_* | Mobzilla Scale … | 7/13/11/7 | 1000 | Schwarz mit violetten Kantenlinien (#311a39 Mittel; #a423cf) und weißen Glanzpunkten | schwarz mit Violett |
| royal | royal_* | Royal Guardian … | 8/14/12/8 | 2000 | Weiß (#d0cab4 Mittel, #ffffff dominant) mit goldenen Säumen; Helm mit goldenem Visierrand | weiß mit Gold |
| lapis | lapis_* | Lapis Lazuli … | 2/7/5/2 | 60 | Nachtblau-grau (#36444e) mit weißen Rippenlinien und cyanfarbenen Juwelpixeln an Brust und Helm | schwarz mit weißen „Zähnen" und cyan Stirnstein |
| queen | queen_* | Queen Scale … | 9/16/14/9 | 1500 | Schwarz (#2c0814) mit dunkelroten Säumen (#8a0000) und magenta Stirnjuwel (#b409cd) | schwarz mit Rot und Magenta |

Schutz und Faktor: manifest `armor_materials`. Die Stück-Haltbarkeit ist Faktor × {11, 16, 15, 13} (Helm/Brust/Beine/Stiefel) – das statische Array in `ItemArmor` 1.7.10, am Bytecode belegt (client-1.7.10.jar, Klasse `abb`, `<clinit>`: `bipush 11/16/15/13`); Royal-Brust also 32000, Queen-Brust 24000, Lava-Eel-Brust 640.

**Glint (fest verdrahtete Verzauberungen, ItemOreSpawnArmor.java:76-148, Nachschub in `onUpdate` 150-252, sobald keine der acht Verzauberungen mehr auf dem Stack ist).** Alle Stücke glinten bei: **Ultimate** (Protection/Fire/Blast/Projectile 5; Helm Respiration 2, Aqua Affinity 3; Stiefel Feather Falling 3), **Mobzilla** (alle vier Protections 10, Unbreaking 5; Stiefel Feather Falling 10), **Royal** (wie Mobzilla plus Helm Respiration 1, Aqua Affinity 2), **Lava Eel** (Protection 3, Fire 2, Blast 10; Helm Respiration 1, Aqua Affinity 2; Stiefel Feather Falling 2), **Moth Scale** (Protection 3, Fire 3, Blast 3; Stiefel Feather Falling 5), **Experience** (Protection 2, Blast 1; Stiefel Feather Falling 1), **Lapis** (Protection 1, Projectile 1; Helm Respiration 1, Aqua Affinity 1). **Peacock**: nur die Stiefel glinten (Feather Falling 10, alle anderen `e_*` = 0). **Kein Glint**: Emerald, Ruby, Amethyst, Pink, Tiger's Eye, Queen (alle `e_*` = 0, manifest `armor_materials.all_stats`). Helm-/Stiefel-Zuordnung: Z. 136-146.

**Sichtbare Set-Effekte** (ItemOreSpawnArmor.java:341-363). Royal Boots (nur bei `RoyalGlideEnable != 0`, Default 1, manifest) und Peacock Boots (ohne Config-Schalter): `motionY` auf −0.1 gedeckelt, `fallDistance = 0` – Gleiten ohne Fallschaden. Queen Boots (ebenfalls hinter `RoyalGlideEnable`): Deckel −0.25. Die Prüfung läuft im `onArmorTick` jedes getragenen Royal-/Peacock-/Queen-Stücks und fragt den Stiefelslot ab; die Stiefel selbst sind so ein Stück, **Stiefel allein genügen**. Experience-Set: nur mit einem Experience Sword im Inventar tröpfeln XP und `portal`-Partikel (ExperienceSword.java:55-101, Chancen Helm 1/10, Brust 1/20, Beine 1/30, Stiefel 1/40 nach einem 1/60-Tick).

**Rüstungsformel 1.7.10, am Bytecode belegt** (client-1.7.10.jar, `sv` = EntityLivingBase, `applyArmorCalculations` = `func_70655_b`): `damage × (25 − Gesamtrüstung) / 25`, ohne Deckel. Ab 25 Punkten ist rüstungsreduzierbarer Schaden null – das erreichen **Queen 48, Royal 42, Mobzilla 38, Ultimate 34 und exakt Ruby 25**; Tiger's Eye 23, Amethyst 22 und Emerald 20 liegen knapp darunter. Das ist die Zahl, die ein Port „virtuell" nachbilden muss.

**Portierungshinweise.**
- 1.21.1 baut den Layer-Pfad aus `ArmorMaterial.Layer(id, suffix, dyeable)` zu `textures/models/armor/<id.path>_layer_{1,2}<suffix>.png`. Die Manifest-`texture_map` legt die Layer aber unter `textures/entity/<set>_N.png` ab. Empfehlung: die 28 Dateien beim Port nach `assets/orespawn/textures/models/armor/<set>_layer_1.png` / `_layer_2.png` **umbenennen** (Abweichung von der `texture_map`, dort vermerken; kein Client-Code nötig). Die genaue Pfadauflösung vor dem Schreiben im 1.21.1-Sources-Jar nachlesen, nicht aus dem Gedächtnis.
- `armor_type` → `ArmorItem.Type`; Set-Verzauberungen als `DataComponents.ENCHANTMENTS`-Default je Item (Helm- und Stiefel-Sonderfälle je Type), `inventoryTick` als Nachschub; Level über dem Vanilla-Maximum (Protection 10, Feather Falling 10, Aqua Affinity 3) sind als Component-Werte zulässig.
- `Attributes.ARMOR` ist in 1.21.1 auf 30 geklemmt (Guardrail) und die Vanilla-Formel deckelt die Reduktion bei 80 % (aus dem Gedächtnis, im Sources-Jar prüfen): die fünf Sets mit Summe ≥ 25 verlieren ihre 100-%-Wirkung. Für 1:1 die Restreduktion als Schadensskalierung in einem `LivingDamageEvent`-Handler nachbilden und die Originalsumme im Tooltip nennen; Einzelwerte (max. 16 bei Queen-Brust) bleiben unverändert.
- `OreSpawnMain.proxy.setArmorPrefix("<set>")` (ClientProxyOreSpawn.java:178, CommonProxyOreSpawn.java:28) war nur der Forge-1.7-Renderindex – in 1.21.1 ersatzlos.
- Kanten-Layer (pink, tigerseye): Vanilla `HumanoidArmorLayer` zeichnet den Skin darunter, Cutout genügt (Alpha ist 0 oder 255, keine Halbtransparenz gemessen).
- `onArmorTick` → `Item.inventoryTick`/`ArmorItem`-Tick über `IItemExtension#onArmorTick`-Äquivalent im NeoForge-Jar nachschlagen; `motionY`-Deckel wird `setDeltaMovement`, `fallDistance = 0` bleibt.

### Glint-Übersicht (Sichtprüfung für den Port)

| immer Glint | kein Glint |
|---|---|
| ultimatesword/-pickaxe/-shovel/-hoe/-axe, nightmaresword, experiencesword, poisonsword, emeraldpickaxe, berthasmall, slicesmall, royalsmall, battleaxesmall, queenbattleaxesmall; Sets ultimate, mobzilla, royal, lavaeel, mothscale, experience, lapis; peacock_boots | emeraldsword, emeraldshovel/-hoe/-axe, ratsword, fairysword, mantisclaw, rosesword, bighammer, ruby*, amethyst*, crystalwood*, crystalpink*, crystalstone*, tigerseye_*, hammysmall, chainsawsmall; Sets emerald, ruby, amethyst, pink, tigerseye, queen; peacock_helmet/_chest/_leggings |

### Nicht in diesem Batch, aber angrenzend

- `squidzookasmall` („SquidZooka!", Kind `item`, eigener `RenderSquidZooka`/`ModelSquidZooka`, ClientProxyOreSpawn.java:152) – in der Aufgabenliste genannt, laut Manifest-Kind nicht Teil von weapon/tool/armor.
- Spawn-Egg-Farben (Kind `spawn_egg`, 114 Items) und Critter Cages (Kind `cage`, 119) – eigene Batches.
- `crystalwoodshovel`, `crystalpinkshovel`, `crystalstoneshovel`, `tigerseye_shovel`, `ultimatebow`, `skatebow` (Kind `item`).

### Offene Punkte

- Ob der 0.375-Zweig von `RenderPlayer` für Forge-`IItemRenderer` mit `shouldUseRenderHelper == true` gilt, ist Forge-Patch-Wissen und nicht am Vanilla-Bytecode belegbar; die Blockgrößen der 3D-Waffen tragen deshalb ein „≈" und sind gegen Screenshots des Originals abzunehmen.
- Chainsaw-Zahnanimation: framebasiert im Original; Tick-Bindung im Port ist eine Design-Entscheidung.
- Tooltip-Addition für `ItemTool` (Spitzhacke/Axt/Schaufel) in 1.7.10 nicht nachgelesen.
- 1.21.1-Deckel der Rüstungsformel (80 %) aus dem Gedächtnis; vor der Implementierung im Sources-Jar prüfen.
