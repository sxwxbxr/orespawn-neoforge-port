# Design: design-items-other

Batch: alle Manifest-Einträge der Kinds `item` (95), `food` (33), `seed` (5), `cage` (119) und `spawn_egg` (114) = 366 Items (manifest).
Quellen in Prioritätsreihenfolge: `docs/catalog/manifest.json`, dekompilierte Quelle `reference/src-20.2/.../danger/orespawn/<Class>.java`,
`reference/jar/models/ModelSquidZooka.json`, `reference/jar/renderers_dump.txt`, `reference/jar/calls_ClientProxyOreSpawn.txt`,
`reference/jar/mcp/{fields,methods}.csv`, danach `docs/research/03-items.md` und `06-models-design.md`.
Die Kontaktbögen zum Sichten der 366 Icons und des `spinners.png`-Atlas sowie die Farbstatistiken (motifs.json, icon_stats.json)
wurden mit PIL aus `reference/jar/extracted/assets/orespawn/` gebaut (Scratchpad, nicht im Repo).

**Bildsprache dieses Batches.** Dieser Batch ist der „Rest" von OreSpawn, und er ist fast vollständig zweidimensional:
365 der 366 Items sind reine 16×16-Icons im Vanilla-1.7.10-Stil (schwarze 1-px-Kontur, wenige Farbstufen, Diagonale von
links unten nach rechts oben bei allem, was einen Stiel hat). Nur der SquidZooka hat ein Boxmodell für die Hand. Die drei
großen Familien sind Serien aus einer Vorlage: 114 Critter Cages sind eine kupferbraune Gitterraute mit wechselndem
Mob-Motiv in der Mitte, 114 Spawn Eggs sind die Vanilla-Eiform in zwei Konturvorlagen plus rund dreißig Piktogramm-Eier
(Roboterköpfe, Ameisen, gekrönte Eier), und die 33 Speisen greifen die Vanilla-Fischsilhouette und das Muster
„rosa roh, braun gegart" auf. Dazwischen stehen Einzelstücke mit eigener Handschrift – die violette Mobzilla-Schuppe,
das rote Herz „Love", der regenbogengesprenkelte Ultimate Bow, der SquidZooka mit Tintenfischkörper – und eine Reihe
Projektile, die im Flug nichts anderes zeigen als ihr eigenes Icon: Steine, Schuhe, Ladungen und der leere Käfig werden
alle als Billboard aus einem 256×256-Sprite-Atlas (`spinners.png`) gezeichnet, dessen Kacheln Kopien der Item-Icons sind.

## Icon-Konvention (alle Items dieses Batches)

- Jedes Item registriert sein Icon als `"OreSpawn:" + unlocalizedName.substring(5)` (z. B. ItemSalt.java:16, ItemRock.java:144,
  CritterCage.java / ZooCage.java:69, ItemSpawnEgg.java). Legacy-Pfad `assets/orespawn/textures/items/<id>.png`, neuer Pfad
  `assets/orespawn/textures/item/<id>.png` (manifest `textures`); drei Dateien wechseln dabei die Schreibweise:
  `RayGun.png` → `raygun.png`, `cageMOTHRA.png` → `cagemothra.png`, `eggMOTHRA.png` → `eggmothra.png` (manifest texture_map).
- Alle 366 Icons sind 16×16 px RGBA (manifest; mit PIL über alle Dateien geprüft, keine Abweichung).
- Projektil-Sprites: `spinners.png` (256×256, neuer Pfad `textures/entity/spinners.png`, manifest texture_map) ist ein
  16×16-Kachelraster, Kachel-Index = Zeile·16 + Spalte, UV = (Index % 16 · 16, Index / 16 · 16) / 256
  (RenderSpinner.java:33-36). Belegte Zeilen: 0–5 (Werkzeuge, Schuhe, Popcorn, Fische, Ladungen), 10–11 (Käfige 160–191),
  12 ff. (Eier ab 192); der Rest ist opakes Magenta (Kontaktbogen `spinners_zoom.png`). In 1.21.1 ersetzt
  `ThrownItemRenderer` mit dem Item-Stack den ganzen Atlas.

---

### Barren, Nuggets und Grundstoffe (12)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| ingoturanium | Uranium Ingot | items/ingoturanium.png → item/ingoturanium.png | Vanilla-Barrenform in leuchtendem Gelb mit hellgelber Oberkante |
| ingottitanium | Titanium Ingot | items/ingottitanium.png → item/ingottitanium.png | Vanilla-Barrenform in Eisweiß mit hellblauem Schatten |
| crystalpink_ingot | Pink Tourmaline Ingot | items/crystalpink_ingot.png → item/crystalpink_ingot.png | Kleiner diagonaler Kristallbarren in Magenta mit weißem Glanzstreifen |
| tigerseye_ingot | Tiger's Eye Ingot | items/tigerseye_ingot.png → item/tigerseye_ingot.png | Diagonaler Barren in Ocker-Gold mit weißem Kern und dunkelbraunen Bändern |
| uranium_nugget | Uranium Nugget | items/uranium_nugget.png → item/uranium_nugget.png | Kleiner gelbgrüner Klumpen mit hellem Glanzpunkt |
| titanium_nugget | Titanium Nugget | items/titanium_nugget.png → item/titanium_nugget.png | Kleiner eisblau-weißer Klumpen, gleiche Silhouette wie der Uranium Nugget |
| ruby | Ruby | items/ruby.png → item/ruby.png | Runder facettierter Rubin in Dunkelrot mit hellem Glanzpunkt |
| amethyst | Amethyst | items/amethyst.png → item/amethyst.png | Facettierter violetter Edelstein, gleiche Silhouette wie der Rubin |
| salt | Salt | items/salt.png → item/salt.png | Lose Streuung hellgrauer Körner auf transparentem Grund |
| greengoo | Green Goo | items/greengoo.png → item/greengoo.png | Kleiner runder Klecks in sattem Grün |
| deadstinkbug | Dead Stink Bug | items/deadstinkbug.png → item/deadstinkbug.png | Schwarzer Käferkörper mit orange-gelben Segmentstreifen, senkrecht |
| crystalsticks | Crystal Shards | items/crystalsticks.png → item/crystalsticks.png | Diagonale Kette blassrosa-weißer Kristallsplitter (Stick-Ersatz der Kristalldimension) |

**Aussehen.** Zwei Barren folgen exakt der Vanilla-Barrensilhouette (Uranium gelb, Titanium eisweiß), die beiden
Kristallbarren (`crystalpink_ingot`, `tigerseye_ingot`) sind kleinere diagonale Stäbe – trotz Klasse `IngotUranium`
(manifest: class IngotUranium für alle vier). Ruby und Amethyst teilen eine Silhouette und unterscheiden sich nur in
Rot bzw. Violett; die beiden Nuggets teilen ebenfalls eine Silhouette. Salt ist als loses Körnermuster gezeichnet,
Green Goo als einfacher Klecks. `crystalsticks` ist der Stick-Ersatz der Kristalldimension und sieht auch so aus:
eine diagonale Splitterkette, so schmal, dass sie im Inventar fast verschwindet. Alle Icons 16×16 (manifest).
Kreativ-Tab: `tabMaterials` für alle `ItemSalt`-Instanzen (ItemSalt.java:11); `IngotUranium` setzt keinen Tab
(IngotUranium.java, 18 Zeilen, nur `setUnlocalizedName`) – offen: welcher Tab die vier Barren im Original zeigt,
weil `ctor_args` nur die Id enthält (manifest).

**Portierungshinweise.** Alle zwölf sind `item/generated` mit `layer0 = orespawn:item/<id>`. Nichts an ihnen ist
Code; `ItemSalt` ist eine 18-Zeilen-Klasse, die nur den Tab und das Icon setzt (ItemSalt.java:10-17). Die
Icon-Registrierung `"OreSpawn:" + unlocalizedName.substring(5)` (ItemSalt.java:16) entfällt komplett – in 1.21.1
kommt das Icon aus dem Modell-JSON.

---

### Trophäen und Big-Bertha-Teile (20)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| mothscale | Moth Scale | items/mothscale.png → item/mothscale.png | Schwarze Raute mit orange-goldenem Ring und rotem Kern |
| queenscale | The Queen Scale | items/queenscale.png → item/queenscale.png | Schwarzer Schildumriss mit rotem Rand und violetter Spitze |
| nightmarescale | Nightmare Scale | items/nightmarescale.png → item/nightmarescale.png | Schwarze dreizackige Klaue mit dunkelroten Kanten |
| emperorscorpionscale | Emperor Scorpion Scale | items/emperorscorpionscale.png → item/emperorscorpionscale.png | Schwarzer breiter Rautenpanzer mit dunkelgrauem Rand |
| basiliskscale | Basilisk Scale | items/basiliskscale.png → item/basiliskscale.png | Grüne Rautenschuppe mit dunkelgrüner Kontur |
| waterdragonscale | Water Dragon Scale | items/waterdragonscale.png → item/waterdragonscale.png | Blaue längliche Schuppe mit dunklerem Rand |
| peacockfeather | Peacock Feather | items/peacockfeather.png → item/peacockfeather.png | Grüner Federstiel mit Pfauenauge in Gold, Cyan und Weiß |
| jumpybugscale | Jumpy Bug Scale | items/jumpybugscale.png → item/jumpybugscale.png | Braun-schwarze Chevron-Schuppe in Bumerangform |
| krakentooth | Kraken Tooth | items/krakentooth.png → item/krakentooth.png | Gebogener Reißzahn in Hellgrau bis Weiß |
| godzillascale | Mobzilla Scale | items/godzillascale.png → item/godzillascale.png | Violette Kristallschuppe mit gezacktem Kamm |
| molenoidnose | Molenoid Nose | items/molenoidnose.png → item/molenoidnose.png | Rosa Sternform mit acht hellrosa Strahlen |
| seamonsterscale | Sea Monster Scale | items/seamonsterscale.png → item/seamonsterscale.png | Braune Rautenschuppe mit beigen Riffeln |
| wormtooth | Worm Tooth | items/wormtooth.png → item/wormtooth.png | Kleiner beige-weißer Zahn mit roter Wurzel |
| trextooth | TRex Tooth | items/trextooth.png → item/trextooth.png | Großer weißer Reißzahn mit grüner Wurzel |
| caterkillerjaw | CaterKiller Jaws | items/caterkillerjaw.png → item/caterkillerjaw.png | Schwarzer Kieferbogen mit weißen Zähnen und roten Punkten |
| seavipertongue | Sea Viper Tongue | items/seavipertongue.png → item/seavipertongue.png | Gegabelte rote Zunge, diagonal |
| vortexeye | Vortex Eye | items/vortexeye.png → item/vortexeye.png | Violetter Augapfel mit grünem Stiel und schwarzer Pupille |
| bbhandle | Big Bertha Handle | items/bbhandle.png → item/bbhandle.png | Schwarzer Schwertgriff mit Goldknauf, diagonal |
| bbguard | Big Bertha Guard | items/bbguard.png → item/bbguard.png | Gelb-schwarze kantige Parierstange |
| bbblade | Big Bertha Blade | items/bbblade.png → item/bbblade.png | Schwarze Klinge mit roten Kantenpunkten, diagonal |

**Aussehen.** Zwanzig Drops, alle Klasse `ItemSalt` (manifest), also reine Materialien ohne Verhalten. Die
Bildsprache ist „Körperteil in der Farbe des Mobs": Schuppen als Rauten (Basilisk grün, Water Dragon blau,
Sea Monster braun, Emperor Scorpion schwarz), Zähne als gebogene weiße Keile (Kraken, T-Rex, Worm), zwei Organe
(Vortex Eye, Sea Viper Tongue) und die drei Schwertteile für Big Bertha, die zusammengesetzt die Silhouette der
Klinge ergeben: Griff schwarz-gold, Parierstange gelb-schwarz, Klinge schwarz mit roten Kanten. Mit `queenscale`
und `godzillascale` sind die beiden Bossschuppen die auffälligsten Icons (violette Kristallschuppe, schwarzer
Schild mit rot-violettem Rand).

**Portierungshinweise.** `item/generated`. Die Drops selbst kommen aus den Entities (z. B. Kraken.java:221,
TheQueen.java:182, Godzilla.java:749 laut 03-items.md:281-284, niedrigere Vertrauensstufe) – Loot-Tables in 1.21.1.
Die Namen enthalten Apostrophe und Ausrufezeichen nur in den lang-Strings, die Registry-Ids sind sauber (manifest).

---

### Wurfsteine und Kristalle (12)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| rocksmall | Small Rock | items/rocksmall.png → item/rocksmall.png | Kleiner grauer Kiesel mit dunklerem Rand |
| rock | Big Rock | items/rock.png → item/rock.png | Größerer dunkelgrauer Brocken mit hellgrauen Facetten |
| rockred | Flame Rock | items/rockred.png → item/rockred.png | Dunkler Brocken mit dunkelroten Glutpunkten |
| rockgreen | Poison Rock | items/rockgreen.png → item/rockgreen.png | Dunkler Brocken mit grünen Flecken |
| rockblue | Slowness Rock | items/rockblue.png → item/rockblue.png | Dunkler Brocken mit blauen Flecken |
| rockpurple | Weakness Rock | items/rockpurple.png → item/rockpurple.png | Dunkler Brocken mit violetten Flecken |
| rockspikey | Painful Rock | items/rockspikey.png → item/rockspikey.png | Schwarzer Brocken mit vier abstehenden Stacheln |
| rocktnt | Explosive Rock | items/rocktnt.png → item/rocktnt.png | Roter Brocken mit weißem Zündpunkt (TNT-Rot) |
| rockcrystalred | Flame Crystal | items/rockcrystalred.png → item/rockcrystalred.png | Roter strahlenförmiger Kristallbusch |
| rockcrystalgreen | Poison Crystal | items/rockcrystalgreen.png → item/rockcrystalgreen.png | Grüner strahlenförmiger Kristallbusch |
| rockcrystalblue | Slowness Crystal | items/rockcrystalblue.png → item/rockcrystalblue.png | Blauer strahlenförmiger Kristallbusch |
| rockcrystaltnt | Explosive Crystal | items/rockcrystaltnt.png → item/rockcrystaltnt.png | Dunkelroter Kristallbusch mit schwarzem Kern |

**Aussehen.** Zwölf Varianten einer Klasse (`ItemRock`, ItemRock.java:11). Acht Steine sind derselbe dunkelgraue
Brocken mit farbigen Flecken (rot, grün, blau, violett), dazu `rocksmall` (kleiner, heller), `rockspikey`
(vier Stacheln) und `rocktnt` (TNT-rot). Die vier Kristalle wechseln die Form: strahlenförmige Büschel in Rot,
Grün, Blau und Dunkelrot-Schwarz. Der Kreativ-Tab ist `tabCombat` (ItemRock.java:15), Stack 64 (ItemRock.java:14).

**Geworfen und platziert.** Rechtsklick wirft `EntityThrownRock` mit Typ 1–12 (ItemRock.java:24-59); dessen
Renderer `RenderThrownRock` zeichnet **dasselbe Item-Icon** als Sprite (renderers_dump.txt: alle zwölf
`textures/items/rock*.png`, `doRender: 0.5, 0.5, 0.5`). Rechtsklick auf einen Block setzt stattdessen eine
`RockBase`-Entity mit `placeRock(1..12)` (ItemRock.java:72-110); die hat ein eigenes Boxmodell `ModelRockBase`
mit zehn 64×64-Texturen `Rock*texture.png` (renderers_dump.txt) – Entity-Batch, hier nur der Verweis.

**Portierungshinweise.** `item/generated`. Der Wurf-Renderer wird `ThrownItemRenderer` (das Item selbst als
Sprite, exakt wie das Original); die Typnummer 1–12 gehört als `SynchedEntityData` in die Projektil-Entity.

---

### Schuhe und Controller (Wurfgeschosse) (5)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| redheels | Red Heels | items/redheels.png → item/redheels.png | Roter Stöckelschuh mit weißem Riemen, Seitenansicht |
| blackheels | Black Heels | items/blackheels.png → item/blackheels.png | Schwarzer Stöckelschuh, Seitenansicht |
| slippers | Slippers | items/slippers.png → item/slippers.png | Flacher cyanfarbener Pantoffel mit rotem Detail |
| boots | Boots | items/boots.png → item/boots.png | Brauner Stiefel mit dunkler Sohle |
| gamecontroller | Game Controller | items/gamecontroller.png → item/gamecontroller.png | Schwarzer Spielcontroller mit bunten Tasten |

**Aussehen.** Fünf Wurfitems der Klasse `ItemShoes` mit Id 2–6 (manifest ctor_args; ItemShoes.java:15-17),
Tab `tabDecorations` (ItemShoes.java:19), Stack 64. Die Icons sind kleine Seitenansichten: zwei Stöckelschuhe
(rot, schwarz), ein flacher cyanfarbener Pantoffel, ein brauner Stiefel und – Boyfriend-Gegenstück – ein schwarzer
Spielcontroller mit bunten Tasten.

**Im Flug.** Rechtsklick spawnt `Shoes(world, player, my_id)` (ItemShoes.java:28). `RenderShoe` setzt
`spinItemIconIndex = getShoeId()` (RenderShoe.java:11) und `RenderSpinner` zeichnet daraus eine Kachel aus
`spinners.png` (256×256, 16×16-Raster, Index = Zeile·16 + Spalte, RenderSpinner.java:33-36), als Billboard zur
Kamera gedreht, um `rotationPitch` um Z rotiert und auf 0,5 skaliert (RenderSpinner.java:25, 40-42). Kacheln 2–5
des Atlas sind pixelidentisch mit den vier Schuh-Icons (PIL-Vergleich 256/256 Pixel); Kachel 6 ist eine Variante des
Controller-Icons (198/256 Pixel gleich; Kontaktbogen `spinners_zoom.png`, Zeile 0).

**Portierungshinweise.** `item/generated`; Projektil als `ThrownItemRenderer` mit dem Item-Stack – das ersetzt den
Spinner-Atlas eins zu eins, weil die Kachel ohnehin das Item-Icon ist. `spinners.png` muss dafür nicht portiert
werden; die Drehung um `rotationPitch` (RenderSpinner.java:42) ist der einzige Unterschied zu Vanilla-Snowbällen
und lässt sich im Renderer mit `poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()))` nachbauen.

---

### Ladungen und Wurfgeschosse (6)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| sunspoturchin | Sunspot Urchin | items/sunspoturchin.png → item/sunspoturchin.png | Gelber Strahlenstern (Seeigel) mit weißem Kern |
| waterball | WaterDragon Charge | items/waterball.png → item/waterball.png | Blauer Strahlenstern mit weißem Kern, gleiche Form wie der Sunspot Urchin |
| laserball | Robot Laser Charge | items/laserball.png → item/laserball.png | Dunkelrote Kugel mit hellrotem Glanz |
| iceball | Ice Ball | items/iceball.png → item/iceball.png | Hellblau-weiße, rundlich facettierte Eiskugel |
| acid | Acid | items/acid.png → item/acid.png | Lose Streuung orange-gelber Tropfen |
| deadirukandji | Dead Irukandji | items/deadirukandji.png → item/deadirukandji.png | Blassrosa-weiße sternförmige Qualle mit rotem Punkt |

**Aussehen.** Sechs Wurfitems mit je eigener 33-Zeilen-Klasse (classes.json: ItemSunspotUrchin, ItemWaterBall,
ItemLaserBall, ItemIceBall, ItemAcid, ItemIrukandji je 33 Zeilen). Zwei Strahlensterne (Urchin gelb, Water Ball
blau) teilen eine Silhouette; Laser Charge ist eine rote Kugel, Ice Ball eine hellblaue Eiskugel, Acid eine
Tropfenstreuung, Dead Irukandji eine blasse Sternqualle.

**Im Flug.** Alle sechs Projektile nutzen `RenderItemUrchin` → `RenderSpinner` mit festen Atlas-Indizes:
SunspotUrchin 50 (SunspotUrchin.java:20), WaterBall 49 (WaterBall.java:18), LaserBall 81 (LaserBall.java:22),
IceBall 84 (IceBall.java:15), Acid 85 (Acid.java:12), DeadIrukandji 86 (DeadIrukandji.java:12). Im Atlas
(`spinners_zoom.png`) sind das: 49 blauer Stern, 50 gelber Stern, 81 rote Kugel, 84 Eiskugel, 85 Tropfen,
86 Qualle. PIL-Vergleich gegen die Item-Icons: 49, 81 und 85 pixelidentisch (256/256); 50, 84 und 86 sind
Varianten (145, 124 bzw. 215 von 256 Pixeln gleich) – der Urchin und die Eiskugel fliegen also als leicht anderes
Sprite, als sie im Inventar aussehen. `RenderItemUrchin` überspringt `BerthaHit` ganz (RenderItemUrchin.java:9-11).

**Portierungshinweise.** `item/generated` plus `ThrownItemRenderer` je Projektil; für Urchin, Ice Ball und Dead
Irukandji entweder das Item-Icon akzeptieren (kleine Abweichung) oder die drei Atlas-Kacheln 50/84/86 als eigene
16×16-Texturen ausschneiden und per `ItemStack` eines unsichtbaren Hilfsitems rendern – 1:1 wäre Letzteres. Stack
64 bei Acid und WaterBall (ItemAcid.java:14, ItemWaterBall.java:14). Die Ausnahme `BerthaHit` (unsichtbares Treffer-Projektil)
gehört in den Waffen-Batch.

---

### Fernwaffen, Werkzeuge und Kits (19)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| ultimatebow | The Ultimate Bow | items/ultimatebow.png → item/ultimatebow.png | Vanilla-Bogenform in Dunkelblau mit Regenbogensprenkel (blau, gelb, rot, grün) und gespannter Sehne |
| skatebow | Skate String Bow | items/skatebow.png → item/skatebow.png | Beiger Bogen mit grün-rotem Skate-Körper in der Mitte |
| irukandjiarrow | Irukandji Arrow | items/irukandjiarrow.png → item/irukandjiarrow.png | Diagonaler Pfeil mit blauem Schaft, grünen Federn und rosa-weißer Quallenspitze |
| raygun | A Freakin' Ray Gun! | items/RayGun.png → item/raygun.png | Schwarze Strahlenpistole mit magentafarbenen Streifen, Lauf nach rechts |
| thunderstaff | Thunder Staff | items/thunderstaff.png → item/thunderstaff.png | Roter Stab mit cyan-schwarzer Spitze, diagonal |
| squidzookasmall | SquidZooka! | items/squidzookasmall.png → item/squidzookasmall.png | Grauer Rohrwerfer diagonal mit blauer Tintenfischspitze unten rechts |
| ultimatefishingrod | The Ultimate Fishing Rod | items/ultimatefishingrod.png → item/ultimatefishingrod.png | Angelrute mit dunkelblauem Griff, hellblau-weißer Rute und grün-roter Rolle |
| sifter | Sifter | items/sifter.png → item/sifter.png | Runder brauner Siebrahmen mit heller Gitterfläche |
| wrench | Wrench | items/wrench.png → item/wrench.png | Dunkelgrauer Maulschlüssel, diagonal |
| creeperlauncher | Creeper Launcher | items/creeperlauncher.png → item/creeperlauncher.png | Dünner diagonaler Regenbogenstrahl von Blau nach Rot |
| netherlost | Nether Tracker | items/netherlost.png → item/netherlost.png | Grauer Stiel mit weißem Netherstern-Kopf, diagonal |
| zookeeper | ZooKeeper Shard | items/zookeeper.png → item/zookeeper.png | Schwarz-roter Umhangumriss mit heller Mitte |
| experiencecatcher | Experience Orb Catcher | items/experiencecatcher.png → item/experiencecatcher.png | Diagonale Rute mit grünlichem Kopf und Faden |
| spiderrobotkit | Spider Robot Kit | items/spiderrobotkit.png → item/spiderrobotkit.png | Schwarze Kiste mit goldenem Bügel und rotem Sanduhr-Emblem |
| antrobotkit | Red Ant Robot Kit | items/antrobotkit.png → item/antrobotkit.png | Rote Kiste mit goldenem Bügel und weißem Punkt |
| crystalwoodshovel | Crystal Wood Shovel | items/crystalwoodshovel.png → item/crystalwoodshovel.png | Vanilla-Schaufel mit dunkelgrauem Stiel und elfenbeinfarbenem Blatt |
| crystalpinkshovel | Pink Tourmaline Shovel | items/crystalpinkshovel.png → item/crystalpinkshovel.png | Vanilla-Schaufel mit magentafarbenem Blatt |
| crystalstoneshovel | Kyanite Shovel | items/crystalstoneshovel.png → item/crystalstoneshovel.png | Vanilla-Schaufel mit kobaltblau-cyanfarbenem Blatt |
| tigerseye_shovel | Tiger's Eye Shovel | items/tigerseye_shovel.png → item/tigerseye_shovel.png | Vanilla-Schaufel mit ocker-goldenem Blatt |

**Aussehen.** Die Bögen sind Vanilla-Bogenform: The Ultimate Bow in Dunkelblau mit dem Regenbogensprenkel der
Ultimate-Reihe, der Skate String Bow beige mit dem grün-roten Skate-Körper als Sehne. Ray Gun (Textur `RayGun.png`
mit Großbuchstaben, manifest) ist eine schwarze Pistole mit magentafarbenen Streifen, der Thunder Staff ein roter
Stab mit cyanfarbener Spitze. Die beiden Robot Kits sind Kisten mit goldenem Bügel (schwarz mit rotem
Sanduhr-Emblem für die Spinne, rot mit weißem Punkt für die Ameise). Die vier Kristallschaufeln folgen der
Vanilla-Schaufel mit Materialfarbe im Blatt (Elfenbein, Magenta, Kobalt-Cyan, Ocker) – sie liegen hier, weil das
manifest sie als `kind: item` führt, obwohl sie `ItemTool` erben (manifest superclass_chain).

**Dauerhafter Glint.** Vier Items tragen fest verdrahtete Verzauberungen und glinten deshalb immer: The Ultimate
Bow Power 5 / Flame 3 / Punch 2 / Infinity 1 (UltimateBow.java:22-25, Nachschub in `onUsingTick` :28-36), The
Ultimate Fishing Rod Unbreaking 2 (UltimateFishingRod.java:25, :31), Nether Tracker Sharpness 2
(ItemNetherLost.java:23, :29). Der Skate String Bow hat keine (SkateBow.java:20-24, leere Methoden).

**Haltbarkeitsbalken.** maxDamage: Ultimate Bow 1000 (UltimateBow.java:17), Skate Bow 300 (SkateBow.java:16),
SquidZooka 100 (ItemSquidZooka.java:16), Ultimate Fishing Rod 3000 (UltimateFishingRod.java:15), Nether Tracker
3000 (ItemNetherLost.java:18), Ray Gun 50 (ItemRayGun.java:15), Thunder Staff 50 (ItemThunderStaff.java:18),
Sifter 600 (ItemSifter.java:19), Wrench 100 (ItemWrench.java:16), ZooKeeper Shard 1 (ItemZooKeeper.java:14),
Creeper Launcher 1 (ItemCreeperLauncher.java:15). Die Robot Kits setzen maxDamage auf die konfigurierte
Roboter-Gesundheit (`SpiderRobot_stats.health` bzw. `AntRobot_stats.health`, ItemSpiderRobotKit.java:17-20) –
der Balken zeigt also den Gesundheitszustand des eingepackten Roboters.

**3D in der Hand: SquidZooka.** Das einzige Item dieses Batches mit `IItemRenderer` (manifest item_renderer;
ClientProxy-Registrierung `MinecraftForgeClient.registerItemRenderer(OreSpawnMain.MySquidZooka, new RenderSquidZooka())`,
calls_ClientProxyOreSpawn.txt:398). Das Modell `ModelSquidZooka` (ModelSquidZooka.json: 12 Parts, 12 Boxen,
Texturgröße 128×128, keine Hierarchie, keine Rotationen) besteht aus:

| Part | Box (Ursprung / Größe, px) | UV | Rolle |
|---|---|---|---|
| Barrel | (−1, −1, −19) / 2×2×34 | 29,19 | das Rohr, 34 px lang entlang Z |
| tail1…tail3 | (−1.5,−1.5,15)/3×3×1, (−2,−2,16)/4×4×1, (−2.5,−2.5,17)/5×5×1 | 0,53 / 0,58 / 0,64 | wachsende Scheiben am hinteren Ende |
| tail4 | (−3,−3,18) / 6×6×6 | 0,71 | der Würfelkörper (Tintenfischkopf) |
| tail5…tail7 | (−2.5,−2.5,24)/5×5×1, (−2,−2,25)/4×4×1, (−1.5,−1.5,26)/3×3×1 | 0,84 / 0,91 / 0,97 | schrumpfende Scheiben dahinter |
| sight1, sight3 | (1,−1,−10)/1×1×2, (1,−2,−10)/1×1×2 | 18,0 / 25,0 | Kimme, zwei Stifte übereinander |
| sight2 | (0.5,−4,−12) / 2×2×6 | 32,0 | Zielfernrohr über dem Rohr |
| handle1 | (0,1,0) / 1×7×1 | 0,0 | Griff, 7 px nach unten |

(alle Werte ModelSquidZooka.json; identisch in ModelSquidZooka.java:64-83.) Die Textur `SquidZookatexture.png`
(128×128, neuer Pfad `textures/entity/squidzookatexture.png`, manifest texture_map) ist zu 926 von 16384 Pixeln
belegt (PIL-Zählung): ein dunkelgrauer Block (66,71,66) für Rohr und Griff, ein Blauverlauf (60,125,213 bis
16,66,135) für die Tintenfisch-Scheiben, kleine dunkelrote Punkte (51,2,2) auf dem Rohr als Nieten. Optisch also:
graues Rohr mit blauem, an den Enden abgestuftem Tintenfischkörper hinten und einem Zielfernrohr oben.

In-Hand-Transformationen (RenderSquidZooka.java): `render()` selbst dreht das ganze Modell um 180° um Z
(ModelSquidZooka.java:89), d. h. Griff zeigt nach unten, Zielfernrohr nach oben. Erste Person:
`glRotatef(−30, Y)`, `glScalef(0.35)`, `glTranslatef(4, 2, 2)` (RenderSquidZooka.java:43, 49-57); dritte Person:
`glRotatef(+30, Y)`, Skalierung 0.35, Translation (2, 8, 2) (RenderSquidZooka.java:39, 59-67). Nur EQUIPPED und
EQUIPPED_FIRST_PERSON werden übernommen (RenderSquidZooka.java:18-30), Inventar und Boden zeigen das 16×16-Icon
`squidzookasmall.png`. Die Translation liegt *nach* der Skalierung, wirkt also in Modell-Pixeln × 0.35: erste Person
1,4 / 0,7 / 0,7 Blöcke, dritte Person 0,7 / 2,8 / 0,7 Blöcke. Größe: Z-Ausdehnung −19 bis +27 px = 46 px, × 0.35 /16
≈ **1,0 Block** lang in der Hand; Höhe −4 (sight2) bis +8 (handle1) = 12 px ≈ 0,26 Block.

**Portierungshinweise.** Bögen: `item/bow`-Modell mit `pulling`-Overrides; das Original hat **keine**
Spannbilder (nur `ultimatebow.png` / `skatebow.png` im Texturordner, extracted-Listing), also ein einziges
Overlay für alle drei Stufen oder das eine Icon als alle vier Zustände. Der Ultimate Bow feuert ohne Spannzeit
(UltimateBow.java:38-56: feste 3.0, kein Ladefaktor) – die Bogenanimation läuft trotzdem, weil
`getItemUseAction` `bow` liefert (UltimateBow.java:66-68). Die Enchantments werden Default-Komponenten
(`DataComponents.ENCHANTMENTS`, Holder aus der Registry). Die Angelrute meldet `isFull3D` und
`shouldRotateAroundWhenRendering` (UltimateFishingRod.java:20-22, 35-37) → `item/handheld_rod`; das Original hat
kein `_cast`-Icon (Texturordner), also kein `cast`-Override. `func_146034_e` ist `handleHookRetraction`
(mcp/methods.csv). SquidZooka: `IClientItemExtensions.getCustomRenderer()` mit
`BlockEntityWithoutLevelRenderer`, `LayerDefinition` aus der Tabelle oben (128×128), die 180°-Z-Drehung und die
beiden Transformationssätze als `ItemDisplayContext`-Fallunterscheidung (FIRST_PERSON_* / THIRD_PERSON_*), Modell-JSON
`"parent": "builtin/entity"` mit `squidzookasmall` nur für GUI/GROUND/FIXED. Ray Gun, Thunder Staff, Wrench,
Sifter: `item/handheld`. Kits: `item/generated`; die dynamische maxDamage aus der Config wird in 1.21.1 ein
`MAX_DAMAGE`-Component, das beim Registrieren aus `ModConfigSpec` gelesen wird – Guardrail-konform, aber die
Config muss vor der Item-Registrierung geladen sein (offen: Reihenfolge im Mod-Konstruktor). Der Creeper Launcher
und der ZooKeeper Shard mit maxDamage 1 sind Einmalitems; in 1.21.1 zeigt ein Item mit maxDamage 1 den Balken
nicht anders, das Verhalten „bei Benutzung zerstört" bleibt gleich.

---

### Sofortbauten und Sonderitems (11)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| step_up | Stairs going Up | items/step_up.png → item/step_up.png | Aufsteigende graue Treppe mit gelben Fackelpunkten |
| step_down | Stairs going Down | items/step_down.png → item/step_down.png | Absteigende graue Treppe mit gelben Fackelpunkten |
| step_accross | Insta-Bridge | items/step_accross.png → item/step_accross.png | Waagrechter grauer Steg mit gelben Fackelpunkten |
| instantshelter | Instant Survival Shelter | items/instantshelter.png → item/instantshelter.png | Brauner Hausgrundriss mit heller Türlücke |
| instantgarden | Instant Survival Garden | items/instantgarden.png → item/instantgarden.png | Grüne Fläche mit senkrechten braunen Beetreihen und blauen Wasserstreifen |
| minersdream | Miner's Dream | items/minersdream.png → item/minersdream.png | Grünes Kissen mit bunten Erzpunkten und dunkelgrünem Rand |
| randomdungeon | Random Dungeon | items/randomdungeon.png → item/randomdungeon.png | Weißes Quadrat mit konzentrischen schwarzen Rahmen und rotem Kern |
| magicapple | OMG! No! Don't do it!!! | items/magicapple.png → item/magicapple.png | Goldener Apfel, umrahmt von roten Sprenkelpunkten |
| elevator | Hoverboard | items/elevator.png → item/elevator.png | Braunes Brett mit cyanfarbenen Lichtstreifen, diagonal (Hoverboard) |
| pizza_item | Pizza! | items/pizza.png → item/pizza.png | Runde Pizza mit rotem Rand, gelbem Käse und roten Belagpunkten |
| ducttape_item | Duct Tape! | items/ducttape.png → item/ducttape.png | Dunkelgrüne Klebebandrolle in Rautenform |

**Aussehen.** Die drei Treppenitems sind ein Bildtriptychon: dieselbe graue Steinreihe mit gelben Fackelpunkten,
einmal steigend, einmal fallend, einmal waagrecht. Shelter und Garden sind Draufsichten (Hausgrundriss braun,
Beetreihen grün-braun-blau), Miner's Dream ein grünes Kissen mit Erzpunkten, Random Dungeon ein Zielscheibenquadrat.
Der Magic Apple ist ein goldener Apfel mit rotem Sprenkelkranz, das Hoverboard (Id `elevator`) ein braunes Brett
mit cyanfarbenen Lichtstreifen. Pizza und Duct Tape sind Item-Formen ihrer Blöcke (`ctor_args` nennen
`OreSpawnMain.MyPizzaBlock` bzw. `MyDuctTapeBlock`, manifest), Tab `tabFood` bzw. `tabTools`
(field_78039_h / field_78040_i, mcp/fields.csv), Stack 1 (manifest calls).

**Glint.** Random Dungeon und Magic Apple tragen Fortune 2 (ItemRandomDungeon.java:26, :32;
ItemMagicApple.java:36, :53), beide Stack 1 (ItemRandomDungeon.java:21, ItemMagicApple.java:31). Stack 16 bei
Miner's Dream, Shelter, Garden und den drei Treppen (ItemMinersDream.java:16, InstantShelter.java:17,
InstantGarden.java:16, StepUp.java:16); Hoverboard Stack 1 (ItemElevator.java:14).

**Portierungshinweise.** `item/generated` für alle elf. Pizza und Duct Tape werden `BlockItem`s ihrer Blöcke,
das Icon bleibt das 16×16-Bild (kein Blockmodell im Inventar – das Original zeigt das Item-Icon, manifest
textures). Der Fortune-Glint wird Default-Component. Die Sofortbauten schreiben Blöcke direkt in die Welt
(z. B. ZooCage-Muster) – kein Rendering, aber `Level.setBlock` mit Flag 3 statt `world.setBlock`.

---

### Saatgut, Feldfrüchte und Baumsamen (15)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| radish | Radish | items/radish.png → item/radish.png | Rot-weißes Radieschen mit grünen Blättern |
| rice | Rice | items/rice.png → item/rice.png | Häufchen weißer Körner |
| corn_seed | Corn | items/corn_seed.png → item/corn_seed.png | Gelber Maiskolben mit hellen Körnern, diagonal |
| quinoa | Quinoa | items/quinoa.png → item/quinoa.png | Lose Streuung beiger Körner |
| tomato_seed | Tomato | items/tomato_seed.png → item/tomato_seed.png | Rote Tomate mit grünem Stielansatz |
| lettuce_seed | Lettuce | items/lettuce_seed.png → item/lettuce_seed.png | Grüner Salatkopf mit hellgrünen Blättern |
| strawberry_seed | Strawberry Plant | items/strawberry_seed.png → item/strawberry_seed.png | Kleiner grüner Strauch auf braunem Stamm |
| butterfly_seed | Butterfly Plant | items/butterfly_seed.png → item/butterfly_seed.png | Blaue Stängelpflanze mit grünen Blättern und gelber Blüte |
| moth_seed | Moth Plant | items/moth_seed.png → item/moth_seed.png | Schwarzer Stängel mit grauen Blättern |
| mosquito_seed | Mosquito Plant | items/mosquito_seed.png → item/mosquito_seed.png | Grauer Stängel mit rotem Punkt und kleinem Blatt |
| firefly_seed | Firefly Plant | items/firefly_seed.png → item/firefly_seed.png | Grüner Stängel mit gelber Leuchtblüte |
| appletree_seed | Apple Tree Seed | items/appletree_seed.png → item/appletree_seed.png | Brauner Tropfensamen mit hellem Glanz |
| cherrytree_seed | Cherry Pit | items/cherrytree_seed.png → item/cherrytree_seed.png | Kleiner brauner Kern mit rosa Schatten |
| peachtree_seed | Peach Pit | items/peachtree_seed.png → item/peachtree_seed.png | Brauner Kern mit rötlichem Rand |
| experiencetree_seed | Experience Tree Seed | items/experiencetree_seed.png → item/experiencetree_seed.png | Grüner Tropfensamen mit dunklem Stiel |

**Aussehen.** Zwei Klassenfamilien: die sechs Feldfrüchte sind `ItemSeedFood` (manifest superclass_chain;
Klassen ItemRadish, ItemCornCob, ItemTomato, ItemLettuce je 18 Zeilen) und zeigen die Frucht selbst –
Radieschen, Reiskörner, Maiskolben, Quinoa-Körner, Tomate, Salatkopf. Die fünf `ItemSeeds`
(strawberry_seed, butterfly_seed, moth_seed, mosquito_seed, firefly_seed; Klassen je 20 Zeilen) zeigen den
Setzling: Erdbeerstrauch grün, Butterfly Plant blau mit gelber Blüte, Moth Plant schwarz-grau, Mosquito Plant grau
mit rotem Punkt, Firefly Plant grün mit gelbem Leuchtpunkt. Die vier Baumsamen sind Tropfen-/Kernformen in Braun
(Apfel, Kirsche, Pfirsich) bzw. Grün (Experience Tree). Nährwerte der Feldfrüchte aus den ctor_args (manifest):
Radish 2 / 0.45, Rice 5 / 0.65, Corn 6 / 0.75, Quinoa 7 / 0.85, Tomato 4 / 0.55, Lettuce 3 / 0.45; Boden ist
`Blocks.farmland` (field_150458_ak, SRG; Bedeutung aus der Verwendung als Pflanzgrund erschlossen) bzw.
`OreSpawnMain.CrystalGrass` für Rice und Quinoa (manifest ctor_args).

**Portierungshinweise.** `item/generated`. `ItemSeedFood` gibt es in 1.21.1 nicht mehr: `ItemNameBlockItem`
plus `FoodProperties`-Component ergibt dieselbe Doppelrolle (essbar und pflanzbar). Die Pflanzblöcke (MyRadishPlant,
MyCornPlant1 …) gehören in den Block-Batch. Die Baumsamen sind keine Saplings, sondern `Item`-Klassen mit eigenem
Wachstumscode (ItemAppleSeed 125 Zeilen, ItemExperienceTreeSeed 42 Zeilen, classes.json) – Stack 16 bzw. 1
(ItemAppleSeed.java:16, ItemExperienceTreeSeed.java:15).

---

### Essen (33)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| firefish | Fire Fish | items/firefish.png → item/firefish.png | Gelber Fisch mit orangen Flossen in Vanilla-Fischsilhouette |
| sunfish | Sun Fish | items/sunfish.png → item/sunfish.png | Gelber Fisch mit hellerem Bauch, dieselbe Silhouette wie Fire Fish |
| lavaeel | Lava Eel | items/lavaeel.png → item/lavaeel.png | Rot-orange eingerollter Aal (Spirale) |
| sparkfish | Spark Fish | items/sparkfish.png → item/sparkfish.png | Winziger roter Fisch mit gelbem Funken |
| greenfish | Green Fish | items/greenfish.png → item/greenfish.png | Dunkelgrüner Fisch in Vanilla-Silhouette |
| bluefish | Blue Fish | items/bluefish.png → item/bluefish.png | Dunkelblauer Fisch in Vanilla-Silhouette |
| pinkfish | Pink Fish | items/pinkfish.png → item/pinkfish.png | Magentafarbener Fisch in Vanilla-Silhouette |
| rockfish | Rock Fish | items/rockfish.png → item/rockfish.png | Schwarzer Fisch mit dunkelrotem Auge |
| woodfish | Wood Fish | items/woodfish.png → item/woodfish.png | Brauner Fisch mit Holzmaserung |
| greyfish | Grey Fish | items/greyfish.png → item/greyfish.png | Grauer Fisch mit dunklem Auge |
| popcorn | Popcorn | items/popcorn.png → item/popcorn.png | Lose weiße Popcornflocken |
| popcorn_buttered | Buttered Popcorn | items/popcorn_buttered.png → item/popcorn_buttered.png | Weiße Popcornflocken mit gelben Butterpunkten |
| popcorn_buttered_salted | Buttered and Salted Popcorn | items/popcorn_buttered_salted.png → item/popcorn_buttered_salted.png | Popcornflocken mit gelben und grauen Punkten |
| popcorn_bag | Bag of Popcorn | items/popcorn_bag.png → item/popcorn_bag.png | Weiße Tüte mit rot-blauem Streifen und Popcorn oben |
| butter | Butter | items/butter.png → item/butter.png | Gelber Butterwürfel mit weißem Glanz |
| corndog_cooked | Corn Dog | items/corndog_cooked.png → item/corndog_cooked.png | Brauner Corn Dog am Stiel, diagonal |
| corndog_raw | Raw Corn Dog | items/corndog_raw.png → item/corndog_raw.png | Oranger Corn Dog am Stiel, diagonal |
| buttercandy | Butter Candy! | items/buttercandy.png → item/buttercandy.png | Lose gelbe Bonbonpunkte |
| cookedbacon | Bacon! | items/cookedbacon.png → item/cookedbacon.png | Zwei gebratene braun-beige Speckstreifen |
| bacon | Raw Bacon | items/bacon.png → item/bacon.png | Zwei rohe rosa-orange Speckstreifen |
| cookedcrabmeat | Crab Meat! | items/cookedcrabmeat.png → item/cookedcrabmeat.png | Beiges Fleischstück mit hellem Rand |
| crabmeat | Raw Crab Meat | items/crabmeat.png → item/crabmeat.png | Blassrosa-weißes Fleischstück |
| cheese | Cheese | items/cheese.png → item/cheese.png | Beiger Käsekeil |
| salad | Garden Salad | items/salad.png → item/salad.png | Braune Schüssel mit grünem Salat und roten Tomatenstücken |
| blt_sandwich | BLT Sandwich! | items/blt_sandwich.png → item/blt_sandwich.png | Diagonales Sandwich mit Speck, Salat und Tomate |
| crabbypatty | A Crabby Patty! | items/crabbypatty.png → item/crabbypatty.png | Burger mit Sesambrötchen, Salatblatt und Patty |
| cookedpeacock | Cooked Peacock | items/cookedpeacock.png → item/cookedpeacock.png | Gebratener beiger Vogelkörper |
| rawpeacock | Raw Peacock | items/rawpeacock.png → item/rawpeacock.png | Roher rosa Vogelkörper |
| strawberry | Strawberry | items/strawberry.png → item/strawberry.png | Rote Erdbeere mit gelben Samenpunkten und grünem Blatt |
| cherries | Cherries | items/cherries.png → item/cherries.png | Zwei rote Kirschen an grünem Stiel |
| peach | Peach | items/peach.png → item/peach.png | Oranger Pfirsich mit hellem Glanz |
| crystalapple | Crystal Apple | items/crystalapple.png → item/crystalapple.png | Roter Apfel mit hellem Kristallglanz und grünem Blatt |
| heart | Love | items/heart.png → item/heart.png | Großes rotes Herz mit hellem Glanz |

**Aussehen.** 33 `ItemFood`-Einträge (manifest kind food; 03-items.md:225 zählt 40, weil dort Pizza und die sechs
`ItemSeedFood`-Feldfrüchte mitgezählt sind – die stehen hier in den Nachbarfamilien). Die zehn Fische nutzen alle
die Vanilla-Fischsilhouette in Farbvarianten (gelb, grün, blau, magenta, schwarz, holzbraun, grau), Ausnahmen sind
der eingerollte Lava Eel und der winzige Spark Fish. Das Popcorn ist eine Vierergruppe aus derselben Flockenstreuung
mit wachsender Punktdichte (leer → gelb → gelb+grau → Tüte). Die Fleischgruppe (Bacon roh/gebraten, Crab Meat
roh/gebraten, Peacock roh/gebraten) folgt dem Vanilla-Muster „rosa roh, braun gegart". Die drei zusammengesetzten
Gerichte (Salad, BLT, Crabby Patty) sind die detailliertesten Icons des Batches; `heart` („Love") ist ein
großes rotes Herz mit Glanz.

**Nährwerte (hunger / saturation, manifest ctor_args):** Fire Fish 4/0.6, Sun Fish 6/0.6, Lava Eel 2/0.6,
Spark Fish 1/0.2, Green 3/0.5, Blue 4/0.4, Pink 4/0.6, Rock 3/0.7, Wood 5/0.7, Grey 5/0.5, Popcorn 1/0.5,
Buttered 2/0.6, Buttered+Salted 3/0.75, Bag 10/1.25, Butter 1/0.5, Corn Dog 16/2.5, Raw Corn Dog 4/0.6,
Butter Candy 4/0.5, Bacon! 14/1.5, Raw Bacon 8/1.0, Crab Meat! 6/0.75, Raw Crab Meat 4/0.25, Cheese 4/0.5,
Salad 10/0.95, BLT 12/0.95, Crabby Patty 16/2.35, Cooked Peacock 12/1.4, Raw Peacock 6/0.7, Strawberry 2/0.65,
Cherries 3/0.45, Peach 4/0.55, Crystal Apple 5/0.85, Love 8/0.95. Die drei `ItemFireFish`/`ItemLavaEel`/
`ItemSparkFish`-Klassen und `ItemSunFish` sind `setAlwaysEdible` (ItemFireFish.java:14, ItemLavaEel.java:14,
ItemSparkFish.java:14, ItemSunFish.java:14); `ItemSunFish` trägt außerdem die Effekte für Sun Fish, Butter Candy,
Bacon!, Crystal Apple und Love (ItemSunFish.java:17-42). Kein Essen hat Glint.

**Portierungshinweise.** `item/generated`; `FoodProperties` mit `alwaysEdible()` für die genannten,
`ItemSunFish.onFoodEaten` wird `Item.finishUsingItem`-Override oder `FoodProperties.effect(...)`. Die
`ItemPopcorn`-/`ItemStrawberry`-Klassen sind 17 Zeilen (classes.json) und reine Icon-Halter.

---

### Zoo-Käfige (5)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| zoo2 | Extra Small Zoo Cage | items/zoo2.png → item/zoo2.png | Weißes Quadrat mit winzigem grauem Diagonalstrich unten rechts |
| zoo4 | Small Zoo Cage | items/zoo4.png → item/zoo4.png | Weißes Quadrat mit kurzer grauer Diagonalschraffur |
| zoo6 | Medium Zoo Cage | items/zoo6.png → item/zoo6.png | Weißes Quadrat mit mittlerer grauer Diagonalschraffur |
| zoo8 | Large Zoo Cage | items/zoo8.png → item/zoo8.png | Weißes Quadrat mit grau-rosa Diagonalschraffur über die halbe Fläche |
| zoo10 | Extra Large Zoo Cage | items/zoo10.png → item/zoo10.png | Weißes Quadrat mit grau-rosa Diagonalschraffur über die ganze Fläche |

**Aussehen.** Fünf Icons als „weißer Würfel mit Diagonalschraffur": das 16×16-Bild ist eine fast leere weiße
Fläche, in der ein grauer (bei zoo8/zoo10 grau-rosa) Diagonalstrich von rechts unten wächst – ein Pixel
bei zoo2 bis zur vollen Diagonale bei zoo10 (Kontaktbogen; PIL: Farben #c0c0c0/#e0e0e0/#e0c0c0, Anteil steigt
2 → 4 → 6 → 11 → 24 Pixel). Die Icons sind auf weißem Inventarhintergrund kaum sichtbar.

**Gebautes Ergebnis.** `cage_size` = 3 / 5 / 9 / 13 / 17 (manifest ctor_args; ZooCage.java:20), Halbbreite
`width = height = length = cage_size / 2 + 1` = 2 / 3 / 5 / 7 / 9 (ZooCage.java:29). Das ergibt einen Kasten von
**5 / 7 / 11 / 15 / 19 Blöcken Außenkante** um den Spieler, Boden und Decke aus `Blocks.quartz_block`
(k = 0 und k = height+1, ZooCage.java:46-51), Wände aus `Blocks.glass` (ZooCage.java:52-54), Innenraum Luft
(ZooCage.java:56); Außenhöhe height+2 = 4 / 5 / 7 / 9 / 11 Lagen, Innenhöhe 2 / 3 / 5 / 7 / 9. Zentrum ist die
Spielerposition (ZooCage.java:36-38), Ton `random.explode` 1.0/1.5 (ZooCage.java:39). Tab `tabDecorations`,
Stack 16 (ZooCage.java:18-19).

**Portierungshinweise.** `item/generated`. Der Bau ist eine Schleife über `Level.setBlock` – kein Structure
piece nötig, weil der Kasten immer um den Spieler liegt. Das Icon könnte für die Lesbarkeit einen dunkleren Rand
vertragen; das wäre aber eine Abweichung vom 1:1-Ziel und ist hier nur notiert.

---

### Critter Cages (114)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| cageempty | Empty Critter Cage | items/cageempty.png → item/cageempty.png | Leere Käfigraute: kupferbraunes Diagonalgitter mit hellgrauen Ecksprossen, Mitte frei |
| cagespider | Caged Spider | items/cagespider.png → item/cagespider.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit (29 Pixel weichen von cageempty ab) |
| cagebat | Caged Bat | items/cagebat.png → item/cagebat.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun (29 Pixel weichen von cageempty ab) |
| cagecow | Caged Cow | items/cagecow.png → item/cagecow.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben beige/dunkelbraun (31 Pixel weichen von cageempty ab) |
| cagepig | Caged Pig | items/cagepig.png → item/cagepig.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben pink (27 Pixel weichen von cageempty ab) |
| cagesquid | Caged Squid | items/cagesquid.png → item/cagesquid.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrau (28 Pixel weichen von cageempty ab) |
| cagechicken | Caged Chicken | items/cagechicken.png → item/cagechicken.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß (27 Pixel weichen von cageempty ab) |
| cagecreeper | Caged Creeper | items/cagecreeper.png → item/cagecreeper.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün (30 Pixel weichen von cageempty ab) |
| cageskeleton | Caged Skeleton | items/cageskeleton.png → item/cageskeleton.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grau (29 Pixel weichen von cageempty ab) |
| cagezombie | Caged Zombie | items/cagezombie.png → item/cagezombie.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrün/grün (29 Pixel weichen von cageempty ab) |
| cageslime | Caged Slime | items/cageslime.png → item/cageslime.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrün (27 Pixel weichen von cageempty ab) |
| cageghast | Caged Ghast | items/cageghast.png → item/cageghast.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß (28 Pixel weichen von cageempty ab) |
| cagezombiepigman | Caged ZombiePigman | items/cagezombiepigman.png → item/cagezombiepigman.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/rosa (46 Pixel weichen von cageempty ab) |
| cageenderman | Caged Enderman | items/cageenderman.png → item/cageenderman.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz (35 Pixel weichen von cageempty ab) |
| cagecavespider | Caged Cave Spider | items/cagecavespider.png → item/cagecavespider.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun (36 Pixel weichen von cageempty ab) |
| cagesilverfish | Caged Silverfish | items/cagesilverfish.png → item/cagesilverfish.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrau (40 Pixel weichen von cageempty ab) |
| cagemagmacube | Caged Magma Cube | items/cagemagmacube.png → item/cagemagmacube.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot (39 Pixel weichen von cageempty ab) |
| cagewitch | Caged Witch | items/cagewitch.png → item/cagewitch.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/grün (40 Pixel weichen von cageempty ab) |
| cagesheep | Caged Sheep | items/cagesheep.png → item/cagesheep.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß (42 Pixel weichen von cageempty ab) |
| cagewolf | Caged Wolf | items/cagewolf.png → item/cagewolf.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/hellgrau (41 Pixel weichen von cageempty ab) |
| cagemooshroom | Caged Mooshroom | items/cagemooshroom.png → item/cagemooshroom.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/rosa (51 Pixel weichen von cageempty ab) |
| cageocelot | Caged Ocelot | items/cageocelot.png → item/cageocelot.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb (43 Pixel weichen von cageempty ab) |
| cageblaze | Caged Blaze | items/cageblaze.png → item/cageblaze.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/orange (60 Pixel weichen von cageempty ab) |
| cagegirlfriend | Caged Girlfriend | items/cagegirlfriend.png → item/cagegirlfriend.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben blau (56 Pixel weichen von cageempty ab) |
| cageboyfriend | Caged Boyfriend | items/cageboyfriend.png → item/cageboyfriend.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelblau/dunkelgrün (46 Pixel weichen von cageempty ab) |
| cagewitherskeleton | Caged Wither Skeleton | items/cagewitherskeleton.png → item/cagewitherskeleton.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/weiß/hellgrau (42 Pixel weichen von cageempty ab) |
| cageenderdragon | Caged Ender Dragon | items/cageenderdragon.png → item/cageenderdragon.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot/hellgrau/weiß (48 Pixel weichen von cageempty ab) |
| cagesnowgolem | Caged Snow Golem | items/cagesnowgolem.png → item/cagesnowgolem.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/dunkelgrau/hellgrau (46 Pixel weichen von cageempty ab) |
| cageirongolem | Caged Iron Golem | items/cageirongolem.png → item/cageirongolem.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/hellgrau/dunkelgrau (42 Pixel weichen von cageempty ab) |
| cagewitherboss | Caged Wither Boss | items/cagewitherboss.png → item/cagewitherboss.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/hellgrau/dunkelgrau (42 Pixel weichen von cageempty ab) |
| cageredcow | Caged Apple Cow | items/cageredcow.png → item/cageredcow.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/braun/grün (44 Pixel weichen von cageempty ab) |
| cagegoldcow | Caged Golden Apple Cow | items/cagegoldcow.png → item/cagegoldcow.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/gold/grün (53 Pixel weichen von cageempty ab) |
| cageenchantedcow | Caged Enchanted Golden Apple Cow | items/cageenchantedcow.png → item/cageenchantedcow.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/gold/grün (54 Pixel weichen von cageempty ab) |
| cagemothra | Caged MOTHRA | items/cageMOTHRA.png → item/cagemothra.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben blau/schwarz/braun (127 Pixel weichen von cageempty ab) |
| cagealosaurus | Caged Alosaurus | items/cagealosaurus.png → item/cagealosaurus.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben orange/dunkelrot/hellgrau (45 Pixel weichen von cageempty ab) |
| cagecryolophosaurus | Caged Cryosaurus | items/cagecryolophosaurus.png → item/cagecryolophosaurus.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben cyan/weiß/hellgrau (44 Pixel weichen von cageempty ab) |
| cagecamarasaurus | Caged Camarasaurus | items/cagecamarasaurus.png → item/cagecamarasaurus.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/weiß (44 Pixel weichen von cageempty ab) |
| cagevelocityraptor | Caged Velocity Raptor | items/cagevelocityraptor.png → item/cagevelocityraptor.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/rot/weiß (45 Pixel weichen von cageempty ab) |
| cagehydrolisc | Caged Hydrolisc | items/cagehydrolisc.png → item/cagehydrolisc.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben blau/dunkelblau/weiß (46 Pixel weichen von cageempty ab) |
| cagebasilisc | Caged Basilisk | items/cagebasilisc.png → item/cagebasilisc.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/dunkelgrün/weiß (45 Pixel weichen von cageempty ab) |
| cagedragonfly | Caged Dragonfly | items/cagedragonfly.png → item/cagedragonfly.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/anthrazit (16 Pixel weichen von cageempty ab) |
| cageemperorscorpion | Caged Emperor Scorpion | items/cageemperorscorpion.png → item/cageemperorscorpion.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz (53 Pixel weichen von cageempty ab) |
| cagescorpion | Caged Scorpion | items/cagescorpion.png → item/cagescorpion.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau (20 Pixel weichen von cageempty ab) |
| cagecavefisher | Caged Cave Fisher | items/cagecavefisher.png → item/cagecavefisher.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/anthrazit (24 Pixel weichen von cageempty ab) |
| cagespyro | Caged Baby Dragon | items/cagespyro.png → item/cagespyro.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/violett (27 Pixel weichen von cageempty ab) |
| cagebaryonyx | Caged Baryonyx | items/cagebaryonyx.png → item/cagebaryonyx.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrün/dunkelbraun (26 Pixel weichen von cageempty ab) |
| cagegammametroid | Caged WTF? | items/cagegammametroid.png → item/cagegammametroid.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelblau/dunkelgrau/anthrazit (24 Pixel weichen von cageempty ab) |
| cagecockateil | Caged Bird | items/cagecockateil.png → item/cagecockateil.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot/weiß/cyan (20 Pixel weichen von cageempty ab) |
| cagekyuubi | Caged Kyuubi | items/cagekyuubi.png → item/cagekyuubi.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot/braun (25 Pixel weichen von cageempty ab) |
| cagealien | Caged Alien | items/cagealien.png → item/cagealien.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit (38 Pixel weichen von cageempty ab) |
| cageattacksquid | Caged Attack Squid | items/cageattacksquid.png → item/cageattacksquid.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/anthrazit/dunkelrot (41 Pixel weichen von cageempty ab) |
| cagewaterdragon | Caged Water Dragon | items/cagewaterdragon.png → item/cagewaterdragon.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben blau (40 Pixel weichen von cageempty ab) |
| cagecephadrome | Caged Cephadrome | items/cagecephadrome.png → item/cagecephadrome.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/grau (40 Pixel weichen von cageempty ab) |
| cagekraken | Caged Kraken | items/cagekraken.png → item/cagekraken.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/rot (71 Pixel weichen von cageempty ab) |
| cagelizard | Caged Lizard | items/cagelizard.png → item/cagelizard.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/dunkelgrün/blau (45 Pixel weichen von cageempty ab) |
| cagedragon | Caged Dragon | items/cagedragon.png → item/cagedragon.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/dunkelgrau/violett (42 Pixel weichen von cageempty ab) |
| cagebee | Caged Bee | items/cagebee.png → item/cagebee.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/gelb/braun (59 Pixel weichen von cageempty ab) |
| cagehorse | Caged Horse | items/cagehorse.png → item/cagehorse.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/beige/braun (40 Pixel weichen von cageempty ab) |
| cagefirefly | Caged Firefly | items/cagefirefly.png → item/cagefirefly.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrau/dunkelrot/braun (22 Pixel weichen von cageempty ab) |
| cagechipmunk | Caged Chipmunk | items/cagechipmunk.png → item/cagechipmunk.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun/gold (28 Pixel weichen von cageempty ab) |
| cagegazelle | Caged Gazelle | items/cagegazelle.png → item/cagegazelle.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben olivgrau/grau/dunkelbraun (38 Pixel weichen von cageempty ab) |
| cageostrich | Caged Ostrich | items/cageostrich.png → item/cageostrich.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/rosa/anthrazit (39 Pixel weichen von cageempty ab) |
| cagetrooper | Caged Jumpy Bug | items/cagetrooper.png → item/cagetrooper.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot/dunkelbraun (49 Pixel weichen von cageempty ab) |
| cagespit | Caged Spit Bug | items/cagespit.png → item/cagespit.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun/braun (40 Pixel weichen von cageempty ab) |
| cagestink | Caged Stink Bug | items/cagestink.png → item/cagestink.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/gelb (41 Pixel weichen von cageempty ab) |
| cagecreepinghorror | Caged Creeping Horror | items/cagecreepinghorror.png → item/cagecreepinghorror.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun (24 Pixel weichen von cageempty ab) |
| cageterribleterror | Caged Terrible Terror | items/cageterribleterror.png → item/cageterribleterror.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben petrol (20 Pixel weichen von cageempty ab) |
| cagecliffracer | Caged Cliff Racer | items/cagecliffracer.png → item/cagecliffracer.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/dunkelgrau/dunkelrot (19 Pixel weichen von cageempty ab) |
| cagetriffid | Caged Triffid | items/cagetriffid.png → item/cagetriffid.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/dunkelgrün (33 Pixel weichen von cageempty ab) |
| cagenightmare | Caged Nightmare | items/cagenightmare.png → item/cagenightmare.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/grau (47 Pixel weichen von cageempty ab) |
| cagelurkingterror | Caged Lurking Terror | items/cagelurkingterror.png → item/cagelurkingterror.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün (38 Pixel weichen von cageempty ab) |
| cagesmallworm | Caged Small Worm | items/cagesmallworm.png → item/cagesmallworm.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben rosa/grau (11 Pixel weichen von cageempty ab) |
| cagemediumworm | Caged Medium Worm | items/cagemediumworm.png → item/cagemediumworm.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/grau (20 Pixel weichen von cageempty ab) |
| cagelargeworm | Caged Large Worm | items/cagelargeworm.png → item/cagelargeworm.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/weiß/grau (45 Pixel weichen von cageempty ab) |
| cagecassowary | Caged Cassowary | items/cagecassowary.png → item/cagecassowary.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelblau (39 Pixel weichen von cageempty ab) |
| cagecloudshark | Caged Cloud Shark | items/cagecloudshark.png → item/cagecloudshark.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrau/grau (36 Pixel weichen von cageempty ab) |
| cagegoldfish | Caged Gold Fish | items/cagegoldfish.png → item/cagegoldfish.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gold (31 Pixel weichen von cageempty ab) |
| cageleafmonster | Caged Leaf Monster | items/cageleafmonster.png → item/cageleafmonster.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/grün/dunkelgrün (38 Pixel weichen von cageempty ab) |
| cageenderknight | Caged Ender Knight | items/cageenderknight.png → item/cageenderknight.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz (35 Pixel weichen von cageempty ab) |
| cageenderreaper | Caged Ender Reaper | items/cageenderreaper.png → item/cageenderreaper.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/dunkelgrau (40 Pixel weichen von cageempty ab) |
| cagebeaver | Caged Beaver | items/cagebeaver.png → item/cagebeaver.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/schwarz (24 Pixel weichen von cageempty ab) |
| cageurchin | Caged Crystal Urchin | items/cageurchin.png → item/cageurchin.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelrot (11 Pixel weichen von cageempty ab) |
| cageflounder | Caged Flounder | items/cageflounder.png → item/cageflounder.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun (38 Pixel weichen von cageempty ab) |
| cageskate | Caged Skate | items/cageskate.png → item/cageskate.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grau (38 Pixel weichen von cageempty ab) |
| cagerotator | Caged Rotator | items/cagerotator.png → item/cagerotator.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/rot (16 Pixel weichen von cageempty ab) |
| cagepeacock | Caged Peacock | items/cagepeacock.png → item/cagepeacock.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/blau (38 Pixel weichen von cageempty ab) |
| cagefairy | Caged Fairy | items/cagefairy.png → item/cagefairy.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben beige/petrol/rot (28 Pixel weichen von cageempty ab) |
| cagedungeonbeast | Caged Dungeon Beast | items/cagedungeonbeast.png → item/cagedungeonbeast.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/schwarz/dunkelgrau (39 Pixel weichen von cageempty ab) |
| cagevortex | Caged Vortex | items/cagevortex.png → item/cagevortex.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/grün/violett (17 Pixel weichen von cageempty ab) |
| cagerat | Caged Rat | items/cagerat.png → item/cagerat.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/dunkelbraun/beige (32 Pixel weichen von cageempty ab) |
| cagewhale | Caged Whale | items/cagewhale.png → item/cagewhale.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben petrol/dunkelgrau (64 Pixel weichen von cageempty ab) |
| cageirukandji | Caged Irukandji | items/cageirukandji.png → item/cageirukandji.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/grau (8 Pixel weichen von cageempty ab) |
| cagetrex | Caged T. Rex | items/cagetrex.png → item/cagetrex.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelgrün/grün/hellgrün (38 Pixel weichen von cageempty ab) |
| cagehercules | Caged Hercules Beetle | items/cagehercules.png → item/cagehercules.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/braun (38 Pixel weichen von cageempty ab) |
| cagemantis | Caged Mantis | items/cagemantis.png → item/cagemantis.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben hellgrün/grün/hellgrau (38 Pixel weichen von cageempty ab) |
| cagestinky | Caged Stinky | items/cagestinky.png → item/cagestinky.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/hellgrün/gold (38 Pixel weichen von cageempty ab) |
| cageeasterbunny | Caged Easter Bunny | items/cageeasterbunny.png → item/cageeasterbunny.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben weiß/hellgrau (47 Pixel weichen von cageempty ab) |
| cagecaterkiller | Caged CaterKiller | items/cagecaterkiller.png → item/cagecaterkiller.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben orange/anthrazit (49 Pixel weichen von cageempty ab) |
| cagemolenoid | Caged Molenoid | items/cagemolenoid.png → item/cagemolenoid.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben rosa/olivgrau (54 Pixel weichen von cageempty ab) |
| cageseamonster | Caged Sea Monster | items/cageseamonster.png → item/cageseamonster.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun/braun (38 Pixel weichen von cageempty ab) |
| cageseaviper | Caged Sea Viper | items/cageseaviper.png → item/cageseaviper.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben rot (43 Pixel weichen von cageempty ab) |
| cageleon | Caged Leonopteryx | items/cageleon.png → item/cageleon.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/orange/dunkelbraun (38 Pixel weichen von cageempty ab) |
| cagehammerhead | Caged Hammerhead | items/cagehammerhead.png → item/cagehammerhead.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit/dunkelgrau (38 Pixel weichen von cageempty ab) |
| cagerubberducky | Caged Rubber Ducky | items/cagerubberducky.png → item/cagerubberducky.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gelb/beige/gold (38 Pixel weichen von cageempty ab) |
| cagecrystalcow | Caged Crystal Cow | items/cagecrystalcow.png → item/cagecrystalcow.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben orange/dunkelrot/rot (22 Pixel weichen von cageempty ab) |
| cagevillager | Caged Villager | items/cagevillager.png → item/cagevillager.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben gold/beige (38 Pixel weichen von cageempty ab) |
| cagecriminal | Caged Criminal | items/cagecriminal.png → item/cagecriminal.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/weiß (38 Pixel weichen von cageempty ab) |
| cagebrutalfly | Caged Brutalfly | items/cagebrutalfly.png → item/cagebrutalfly.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben rot/schwarz/gold (38 Pixel weichen von cageempty ab) |
| cagenastysaurus | Caged Nastysaurus | items/cagenastysaurus.png → item/cagenastysaurus.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun (38 Pixel weichen von cageempty ab) |
| cagepointysaurus | Caged Pointysaurus | items/cagepointysaurus.png → item/cagepointysaurus.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben braun/dunkelbraun/weiß (38 Pixel weichen von cageempty ab) |
| cagecricket | Caged Cricket | items/cagecricket.png → item/cagecricket.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben dunkelbraun (5 Pixel weichen von cageempty ab) |
| cagefrog | Caged Frog | items/cagefrog.png → item/cagefrog.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben grün/dunkelgrün (38 Pixel weichen von cageempty ab) |
| cagespiderdriver | Caged Spider Driver | items/cagespiderdriver.png → item/cagespiderdriver.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben anthrazit (29 Pixel weichen von cageempty ab) |
| cagecrab | Caged Crab | items/cagecrab.png → item/cagecrab.png | Käfigraute mit Mob-Motiv in der Mitte, Motivfarben schwarz/gelb/gold (54 Pixel weichen von cageempty ab) |

**Aussehen – Schema.** 114 Icons (eine leere, 113 gefüllte) aus einer einzigen Vorlage: eine kupferbraune
Gitterraute (Farben #804000 / #604000 / #a06020, 30 + 19 + 15 Pixel in `cageempty.png`, PIL) mit hellgrauen
Ecksprossen. Die gefüllten Käfige tragen in der Mitte eine 8- bis 70-Pixel große Mob-Silhouette in den Farben
des jeweiligen Mobs; die Raute darum ist bei allen identisch (Pixeldifferenz gegen `cageempty.png`, motifs.json).
Die Motivfarbe in der Tabelle ist die dominante Farbe dieser Differenzpixel (PIL, Quantisierung 32er-Stufen,
Farbnamen per nächstem Palettenton) – ein belegbares Etikett, keine Kunstkritik.

**Ausnahmen, nach Sichtung des Kontaktbogens:** `cagemothra` füllt die Raute fast ganz mit einem blau-schwarzen
Falter (127 Differenzpixel, das größte Motiv); `cagekraken` und `cageemperorscorpion` sind schwarze Klumpen, die
über die Raute hinausragen (71 / 53 Pixel); `cagewhale` zeigt einen petrolfarbenen Wal in voller Rautenbreite;
`cagecricket` (5 Pixel) und `cageirukandji` (8 Pixel) sind fast nicht von der leeren Raute zu unterscheiden.
`cageMOTHRA.png` ist der einzige Dateiname mit Großbuchstaben (→ `cagemothra.png`, manifest texture_map).

**Ids und Verhalten, soweit sichtbar.** Jeder Käfig trägt eine `cage_id` (manifest ctor_args, zweiter Wert:
160 = leer, 161–384 gefüllt; CritterCage.java:16-18), Stack 16, Tab `tabMisc` (CritterCage.java:19-20). Nur der
leere Käfig wird geworfen: `EntityCage(world, player, cage_id)` mit Ton `random.bow` (CritterCage.java:24-32);
gefüllte Käfige setzen beim Rechtsklick auf einen Block sechs Partikelsätze `smoke` / `explode` / `reddust` und
den Ton `random.explode` 1.0/1.5 (CritterCage.java:42-47) und spawnen dann den Mob über die `switch` auf
`cage_id` (CritterCage.java:54 ff.). Im Flug zeichnet `RenderCage` die Atlas-Kachel `getCageIndex()`
(RenderCage.java:9-14), im Wurffall also Kachel 160 von `spinners.png` – pixelidentisch mit `cageempty.png`
(PIL 256/256). Der Atlas ist nach `cage_id` sortiert: Kachel 160–191 = die Käfige mit `cage_id` 160–191, jede
pixelidentisch mit ihrem Item-Icon, Kachel 192–199 = die Eier mit `my_id` 192–199 (PIL-Vergleich aller 40 Kacheln,
alle 256/256). `cage_id`s über 255 (bis 384) hätten keine Kachel mehr; da nur der leere Käfig fliegt, spielt das
keine Rolle.

**Portierungshinweise.** 114 `item/generated`-Modelle mit `layer0 = orespawn:item/cage<mob>`; die Modell-JSONs
sind generierbar. Die Wurf-Entity bekommt `ThrownItemRenderer` mit dem leeren Käfig als Stack. Die `cage_id`
bleibt als Zahl in `SynchedEntityData` der Entity und als Mapping-Tabelle Item → `EntityType<?>` im Code
(CritterCage.java:54 ff.; Vanilla-Ids wie 52 = Spider sind 1.7.10-Entity-Ids und werden durch `EntityType`-Referenzen
ersetzt). Kein Farbtint, keine Layer – die Ikonen sind fertige Bilder.

---

### Spawn Eggs (114)

| id | name | icon (legacy → neu) | look |
|---|---|---|---|
| eggwitherskeleton | Spawn Wither Skeleton | items/eggwitherskeleton.png → item/eggwitherskeleton.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe dunkelgrau, Sprenkel/Muster grau und schwarz |
| eggenderdragon | Spawn Ender Dragon | items/eggenderdragon.png → item/eggenderdragon.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster rot |
| eggsnowgolem | Spawn Snow Golem | items/eggsnowgolem.png → item/eggsnowgolem.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe weiß, Sprenkel/Muster orange |
| eggirongolem | Spawn Iron Golem | items/eggirongolem.png → item/eggirongolem.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe hellgrau, Sprenkel/Muster orange |
| eggwitherboss | Spawn Wither Boss | items/eggwitherboss.png → item/eggwitherboss.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe dunkelgrau, Sprenkel/Muster anthrazit und schwarz |
| egggirlfriend | Spawn Girlfriend | items/egggirlfriend.png → item/egggirlfriend.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe blau, Sprenkel/Muster pink |
| eggredcow | Spawn Apple Cow | items/eggredcow.png → item/eggredcow.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe rot, Sprenkel/Muster grün und braun |
| eggcrystalcow | Spawn Crystal Cow | items/eggcrystalcow.png → item/eggcrystalcow.png | Weißes Ei mit karminroter Kontur und rot-grünen Punkten – eigene Vorlage, nicht die Standard-Eiform |
| egggoldcow | Spawn Golden Apple Cow | items/egggoldcow.png → item/egggoldcow.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe gelb, Sprenkel/Muster grün und braun |
| eggenchantedcow | Spawn Enchanted Golden Apple Cow | items/eggenchantedcow.png → item/eggenchantedcow.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe gelb, Sprenkel/Muster grün und pink |
| eggmothra | Spawn MOTHRA! | items/eggMOTHRA.png → item/eggmothra.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster rot und blau |
| eggalosaurus | Spawn Alosaurus | items/eggalosaurus.png → item/eggalosaurus.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe orange, Sprenkel/Muster dunkelrot |
| eggcryolophosaurus | Spawn Cryolophosaurus | items/eggcryolophosaurus.png → item/eggcryolophosaurus.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe cyan, Sprenkel/Muster weiß |
| eggcamarasaurus | Spawn Camarasaurus | items/eggcamarasaurus.png → item/eggcamarasaurus.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe braun |
| eggvelocityraptor | Spawn Velocity Raptor | items/eggvelocityraptor.png → item/eggvelocityraptor.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe grün, Sprenkel/Muster rot |
| egghydrolisc | Spawn Hydrolisc | items/egghydrolisc.png → item/egghydrolisc.png | Standard-Eiform mit dunkelgrauer Kontur, Grundfarbe blau, Sprenkel/Muster dunkelblau |
| eggbasilisc | Spawn Basilisk | items/eggbasilisc.png → item/eggbasilisc.png | Standard-Eiform mit dunkelgrüner Kontur, Grundfarbe grün, Sprenkel/Muster dunkelgrün |
| eggdragonfly | Spawn Dragonfly | items/eggdragonfly.png → item/eggdragonfly.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelgrau |
| eggemperorscorpion | Spawn Emperor Scorpion! | items/eggemperorscorpion.png → item/eggemperorscorpion.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz |
| eggscorpion | Spawn Scorpion | items/eggscorpion.png → item/eggscorpion.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau, Sprenkel/Muster dunkelgrau und hellgrau |
| eggcavefisher | Spawn Cave Fisher | items/eggcavefisher.png → item/eggcavefisher.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau |
| eggspyro | Spawn Baby Dragon | items/eggspyro.png → item/eggspyro.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe violett, Sprenkel/Muster anthrazit |
| eggbaryonyx | Spawn Baryonyx | items/eggbaryonyx.png → item/eggbaryonyx.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe olivgrau, Sprenkel/Muster dunkelgrün |
| egggammametroid | Spawn WTF? | items/egggammametroid.png → item/egggammametroid.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe petrol, Sprenkel/Muster dunkelblau |
| eggcockateil | Spawn Bird | items/eggcockateil.png → item/eggcockateil.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe weiß, Sprenkel/Muster grau |
| eggkyuubi | Spawn Kyuubi | items/eggkyuubi.png → item/eggkyuubi.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe rot, Sprenkel/Muster braun und gelb |
| eggalien | Spawn Alien | items/eggalien.png → item/eggalien.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelgrün, Sprenkel/Muster schwarz und weiß |
| eggattacksquid | Spawn Attack Squid | items/eggattacksquid.png → item/eggattacksquid.png | Graues Ei mit dunkelroten Punkten |
| eggwaterdragon | Spawn Water Dragon | items/eggwaterdragon.png → item/eggwaterdragon.png | Blaues Ei mit dunkelblauem Verlauf und weißen Glanzpunkten (Farbverlauf, deshalb keine Zweiton-Statistik) |
| eggcephadrome | Spawn Cephadrome | items/eggcephadrome.png → item/eggcephadrome.png | Blau-graues Ei mit Verlauf von Hellblau zu Dunkelblau |
| eggkraken | Uh, no. Don't. | items/eggkraken.png → item/eggkraken.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz |
| egglizard | Spawn Lizard | items/egglizard.png → item/egglizard.png | Standard-Eiform mit dunkelgrüner Kontur, Grundfarbe grün, Sprenkel/Muster blau |
| eggdragon | Spawn Dragon | items/eggdragon.png → item/eggdragon.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe anthrazit, Sprenkel/Muster schwarz |
| eggbee | Spawn Bee | items/eggbee.png → item/eggbee.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster gelb |
| eggtrooper | Spawn Jumpy Bug | items/eggtrooper.png → item/eggtrooper.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelrot, Sprenkel/Muster schwarz |
| eggspit | Spawn Spit Bug | items/eggspit.png → item/eggspit.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun |
| eggstink | Spawn Stink Bug | items/eggstink.png → item/eggstink.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe orange, Sprenkel/Muster schwarz |
| eggostrich | Spawn Ostrich | items/eggostrich.png → item/eggostrich.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau, Sprenkel/Muster dunkelgrau |
| egggazelle | Spawn Gazelle | items/egggazelle.png → item/egggazelle.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun |
| eggchipmunk | Spawn Chipmunk | items/eggchipmunk.png → item/eggchipmunk.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun und weiß |
| eggcreepinghorror | Spawn Creeping Horror | items/eggcreepinghorror.png → item/eggcreepinghorror.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun |
| eggterribleterror | Spawn Terrible Terror | items/eggterribleterror.png → item/eggterribleterror.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe petrol, Sprenkel/Muster grün und cyan |
| eggcliffracer | Spawn Cliff Racer | items/eggcliffracer.png → item/eggcliffracer.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe weiß, Sprenkel/Muster grau und pink |
| eggtriffid | Spawn Triffid | items/eggtriffid.png → item/eggtriffid.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelgrün, Sprenkel/Muster dunkelbraun |
| eggnightmare | Spawn Nightmare!!! | items/eggnightmare.png → item/eggnightmare.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster grau und dunkelrot |
| egglurkingterror | Spawn Lurking Terror | items/egglurkingterror.png → item/egglurkingterror.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe cyan, Sprenkel/Muster petrol |
| egggodzilla | Spawn Mobzilla | items/egggodzilla.png → item/egggodzilla.png | Standard-Eiform mit anthrazitfarbener Kontur, Grundfarbe violett, Sprenkel/Muster anthrazit |
| eggsmallworm | Spawn Small Worm | items/eggsmallworm.png → item/eggsmallworm.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe rosa, Sprenkel/Muster braun |
| eggmediumworm | Spawn Medium Worm | items/eggmediumworm.png → item/eggmediumworm.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun |
| egglargeworm | Spawn Large Worm | items/egglargeworm.png → item/egglargeworm.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster dunkelbraun |
| eggcassowary | Spawn Cassowary | items/eggcassowary.png → item/eggcassowary.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe anthrazit, Sprenkel/Muster schwarz |
| eggcloudshark | Spawn Cloud Shark | items/eggcloudshark.png → item/eggcloudshark.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau, Sprenkel/Muster dunkelgrau |
| egggoldfish | Spawn Gold Fish | items/egggoldfish.png → item/egggoldfish.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe gelb |
| eggleafmonster | Spawn Leaf Monster | items/eggleafmonster.png → item/eggleafmonster.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelbraun, Sprenkel/Muster dunkelgrün und grau |
| eggtshirt | Spawn T-Shirt! | items/eggtshirt.png → item/eggtshirt.png | Weiß-graues Ei mit schwarzem T-Shirt-Piktogramm |
| eggenderknight | Spawn Ender Knight | items/eggenderknight.png → item/eggenderknight.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster anthrazit und violett |
| eggenderreaper | Spawn Ender Reaper | items/eggenderreaper.png → item/eggenderreaper.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe schwarz, Sprenkel/Muster anthrazit und violett |
| eggbeaver | Spawn Beaver | items/eggbeaver.png → item/eggbeaver.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster weiß |
| eggrotator | Spawn Rotator | items/eggrotator.png → item/eggrotator.png | Weißes Ei mit schwarzer Kontur und konzentrischem rot-schwarzem Zielscheibenmuster |
| eggvortex | Spawn Vortex | items/eggvortex.png → item/eggvortex.png | Weißes Ei mit schwarzer Kontur und grünem Wirbelzeichen |
| eggpeacock | Spawn Peacock | items/eggpeacock.png → item/eggpeacock.png | Grün-blau gesprenkeltes Ei mit schwarzer Kontur |
| eggfairy | Spawn Fairy | items/eggfairy.png → item/eggfairy.png | Weißes Ei mit schwarzer Kontur und Regenbogenstreifen in Pink, Cyan und Gelb |
| eggdungeonbeast | Spawn Dungeon Beast | items/eggdungeonbeast.png → item/eggdungeonbeast.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe schwarz, Sprenkel/Muster anthrazit und dunkelgrau |
| eggrat | Spawn Rat | items/eggrat.png → item/eggrat.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe braun, Sprenkel/Muster anthrazit und dunkelbraun |
| eggflounder | Spawn Flounder | items/eggflounder.png → item/eggflounder.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe braun, Sprenkel/Muster schwarz |
| eggwhale | Spawn Whale | items/eggwhale.png → item/eggwhale.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe dunkelgrau, Sprenkel/Muster grau und schwarz |
| eggirukandji | Spawn Irukandji | items/eggirukandji.png → item/eggirukandji.png | Weißes Ei mit schwarzer Kontur und roter Raute |
| eggskate | Spawn Skate | items/eggskate.png → item/eggskate.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe grau, Sprenkel/Muster schwarz und dunkelgrau |
| eggurchin | Spawn Crystal Urchin | items/eggurchin.png → item/eggurchin.png | Weißes Ei mit schwarzer Kontur und gelb-rotem Strahlenstern |
| eggrobot1 | Spawn Bomb-Omb | items/eggrobot1.png → item/eggrobot1.png | Grauer Roboterkopf in Eiform mit dunkelrotem Visier (identische Textur für Robot 1–4) |
| eggrobot2 | Spawn Robo-Pounder | items/eggrobot2.png → item/eggrobot2.png | Grauer Roboterkopf in Eiform mit dunkelrotem Visier (identisch mit eggrobot1) |
| eggrobot3 | Spawn Robo-Gunner | items/eggrobot3.png → item/eggrobot3.png | Grauer Roboterkopf in Eiform mit dunkelrotem Visier (identisch mit eggrobot1) |
| eggrobot4 | Spawn Robo-Warrior | items/eggrobot4.png → item/eggrobot4.png | Grauer Roboterkopf in Eiform mit dunkelrotem Visier (identisch mit eggrobot1) |
| eggghost | Spawn Ghost | items/eggghost.png → item/eggghost.png | Anthrazitfarbene Geistersilhouette ohne Eiform |
| eggghostskelly | Spawn Ghost Pumpkin Skelly | items/eggghostskelly.png → item/eggghostskelly.png | Anthrazitfarbene Geistersilhouette mit braunem Kürbiskopf |
| eggbrownant | Spawn Brown Ant | items/eggbrownant.png → item/eggbrownant.png | Weißes Ei mit brauner Ameisen-Silhouette |
| eggredant | Spawn Red Ant | items/eggredant.png → item/eggredant.png | Weißes Ei mit dunkelroter Ameisen-Silhouette |
| eggrainbowant | Spawn Rainbow Ant | items/eggrainbowant.png → item/eggrainbowant.png | Weißes Ei mit Ameisen-Silhouette in Regenbogenstreifen |
| eggunstableant | Spawn Unstable Ant | items/eggunstableant.png → item/eggunstableant.png | Weißes Ei mit dunkelrot-schwarzer Ameisen-Silhouette |
| eggtermite | Spawn Termite | items/eggtermite.png → item/eggtermite.png | Weißes Ei mit grauer Termiten-Silhouette |
| eggbutterfly | Spawn Butterfly | items/eggbutterfly.png → item/eggbutterfly.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe cyan, Sprenkel/Muster hellgrün und weiß |
| eggmoth | Spawn Moth | items/eggmoth.png → item/eggmoth.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe hellgrün, Sprenkel/Muster gelb |
| eggmosquito | Spawn Mosquito | items/eggmosquito.png → item/eggmosquito.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe weiß, Sprenkel/Muster grau und rosa |
| eggfirefly | Spawn Firefly | items/eggfirefly.png → item/eggfirefly.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe gelb, Sprenkel/Muster weiß |
| eggtrex | Spawn T. Rex | items/eggtrex.png → item/eggtrex.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grün, Sprenkel/Muster dunkelgrün |
| egghercules | Spawn Hercules Beetle | items/egghercules.png → item/egghercules.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster schwarz und dunkelbraun |
| eggmantis | Spawn Mantis | items/eggmantis.png → item/eggmantis.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau, Sprenkel/Muster hellgrün und grün |
| eggstinky | Spawn Stinky | items/eggstinky.png → item/eggstinky.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe grau, Sprenkel/Muster braun und beige |
| eggrobot5 | Spawn Robo-Sniper | items/eggrobot5.png → item/eggrobot5.png | Grauer Roboterkopf in Eiform mit größerem dunkelrotem Visier (identisch mit eggrobot6) |
| eggcoin | Spawn Coin | items/eggcoin.png → item/eggcoin.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe blau |
| eggboyfriend | Spawn Boyfriend | items/eggboyfriend.png → item/eggboyfriend.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelblau, Sprenkel/Muster schwarz und hellgrau |
| eggtheking | Spawn The King | items/eggtheking.png → item/eggtheking.png | Weißes Ei mit goldener Krone |
| eggthequeen | Spawn The Queen | items/eggthequeen.png → item/eggthequeen.png | Schwarzes Ei mit goldener Krone |
| eggtheprince | Spawn The Prince | items/eggtheprince.png → item/eggtheprince.png | Kleineres weißes Ei mit goldener Krone |
| eggeasterbunny | Spawn Easter Bunny | items/eggeasterbunny.png → item/eggeasterbunny.png | Konfetti-Ei aus rosa, gelben, roten, braunen und blauen Punkten |
| eggmolenoid | Spawn Molenoid | items/eggmolenoid.png → item/eggmolenoid.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe braun, Sprenkel/Muster rosa |
| eggseamonster | Spawn Sea Monster | items/eggseamonster.png → item/eggseamonster.png | Dunkel gesprenkeltes Ei in Braunrot, Rosa und Olivgrau (Verlauf) |
| eggseaviper | Spawn Sea Viper | items/eggseaviper.png → item/eggseaviper.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelblau, Sprenkel/Muster blau |
| eggcaterkiller | Spawn CaterKiller | items/eggcaterkiller.png → item/eggcaterkiller.png | Standard-Eiform mit olivgrauer Kontur, Grundfarbe dunkelrot, Sprenkel/Muster braun |
| eggrubberducky | Spawn Rubber Ducky | items/eggrubberducky.png → item/eggrubberducky.png | Standard-Eiform mit brauner Kontur, Grundfarbe gelb, Sprenkel/Muster gold |
| egghammerhead | Spawn Hammerhead | items/egghammerhead.png → item/egghammerhead.png | Standard-Eiform mit anthrazitfarbener Kontur, Grundfarbe anthrazit |
| eggleon | Spawn Leonopteryx | items/eggleon.png → item/eggleon.png | Gold-oranges Ei mit dunkelbraunen Diagonalstreifen |
| eggcriminal | Spawn Criminal | items/eggcriminal.png → item/eggcriminal.png | Weißes Ei mit schwarzen Querstreifen (Sträflingsmuster) |
| eggbrutalfly | Spawn Brutalfly | items/eggbrutalfly.png → item/eggbrutalfly.png | Orange-gelbes Ei in Flammenform mit oranger Kontur |
| eggnastysaurus | Spawn Nastysaurus | items/eggnastysaurus.png → item/eggnastysaurus.png | Braun-rot gesprenkeltes Ei mit schwarzen Flecken (Verlauf) |
| eggpointysaurus | Spawn Pointysaurus | items/eggpointysaurus.png → item/eggpointysaurus.png | Standard-Eiform mit dunkelbrauner Kontur, Grundfarbe braun, Sprenkel/Muster hellgrau |
| eggcricket | Spawn Cricket | items/eggcricket.png → item/eggcricket.png | Standard-Eiform mit schwarzer Kontur, Grundfarbe dunkelbraun |
| eggtheprincess | Spawn The Princess | items/eggtheprincess.png → item/eggtheprincess.png | Cyanfarbenes Ei mit goldener Krone und beiger Kontur |
| eggfrog | Spawn Frog | items/eggfrog.png → item/eggfrog.png | Hellgrünes Ei mit radialem Verlauf und hellgrauer Kontur |
| eggrobot6 | Spawn Jeffery | items/eggrobot6.png → item/eggrobot6.png | Grauer Roboterkopf in Eiform mit größerem dunkelrotem Visier (identisch mit eggrobot5) |
| eggantrobot | Spawn Red Ant Robot | items/eggantrobot.png → item/eggantrobot.png | Rote Roboterameisen-Silhouette auf Weiß |
| eggspiderrobot | Spawn Giant Spider Robot | items/eggspiderrobot.png → item/eggspiderrobot.png | Schwarze Spinnen-Silhouette mit roten Augen auf Weiß |
| eggspiderdriver | Spawn Spider Robot Driver | items/eggspiderdriver.png → item/eggspiderdriver.png | Schwarze Spinnen-Silhouette mit dunkelrotem Fahrer auf Weiß |
| eggcrab | Spawn Crab | items/eggcrab.png → item/eggcrab.png | Gelb-oranges Ei mit Farbverlauf ohne dunkle Kontur |

**Aussehen – Schema.** 114 Icons, alle 16×16 (manifest). Der Großteil ist die Vanilla-Eiform mit einer
1-Pixel-Kontur und zwei Farben (Grundfarbe plus Sprenkel oder Muster). Zwei Vorlagen sind erkennbar
(Kontaktbogen und Konturstatistik in motifs.json): die 17 ältesten Eier (`eggwitherskeleton` … `egghydrolisc`,
Ids 192–206) mit einer dunkelgrauen, dickeren Kontur (#606060, 31 Pixel) und großen Sprenkeln (16 Pixel), danach
eine dünnere olivgraue Kontur (#404020, 19 Pixel) mit Zweiton-Füllung ohne Sprenkel. Die Tabelle gibt Grund- und
Zweitfarbe der Innenpixel an (PIL: Innenpixel = opake Pixel ohne transparenten Nachbarn; Farbnamen per
Palettenzuordnung) – das ist zugleich das Farbpaar, das ein Vanilla-`SpawnEggItem` bräuchte.

**Ausnahmen (Piktogramm statt Ei):** Roboter 1–6 sind graue Roboterköpfe mit rotem Visier (1–4 byteidentische
Farbstatistik, 5 und 6 ebenso); Ghost / Ghost Pumpkin Skelly sind Geistersilhouetten; die fünf Ameisen-/Termiten-
Eier und die drei Roboterinsekten-Eier zeigen Insektensilhouetten auf Weiß; King / Queen / Prince / Princess tragen
eine goldene Krone (weiß / schwarz / klein-weiß / cyan); `eggtshirt` hat ein T-Shirt, `eggcriminal`
Sträflingsstreifen, `eggrotator` eine Zielscheibe, `eggvortex` einen Wirbel, `eggeasterbunny` Konfetti,
`eggcrystalcow` eine eigene rot-konturierte Vorlage. `eggMOTHRA.png` → `eggmothra.png` (manifest texture_map).
Die Namen sind Teil des Looks: „Uh, no. Don't." (eggkraken), „Spawn WTF?" (egggammametroid), „Spawn Nightmare!!!".

**Verhalten, soweit es das Icon betrifft.** `ItemSpawnEgg(id, my_id)` mit `my_id` 192–384 (manifest ctor_args;
ItemSpawnEgg.java:16-21), Stack 64, Tab `tabMisc`. `onItemUse` spawnt über `spawn_something(my_id, …)` bei
(x+0.5, y+1.01, z+0.5) und überträgt einen Custom-Namen des Stacks (ItemSpawnEgg.java:23-35); die `switch` mappt
`my_id` entweder auf 1.7.10-Vanilla-Entity-Ids (51 Skeleton mit `skelly_type = 1` = Wither Skeleton, 63 Ender
Dragon, 97 Snow Golem, 99 Iron Golem, 64 Wither; ItemSpawnEgg.java:42-62) oder auf OreSpawn-Entity-Namen
(`"Girlfriend"`, `"Apple Cow"`, `"WTF?"` …; ItemSpawnEgg.java:63 ff.).

**Portierungshinweise.** Kein `DeferredSpawnEggItem`: das würde die Vanilla-Eiform mit zwei Tintfarben rendern
und die 30 Piktogramm-Eier sowie beide Konturvorlagen verlieren. Stattdessen ein eigenes `Item` mit
`item/generated`-Modell je Ei und derselben `useOn`-Logik (`EntityType.spawn` mit `MobSpawnType.SPAWN_EGG`,
Custom-Name aus `DataComponents.CUSTOM_NAME`). Wer trotzdem Vanilla-Eier will (z. B. für Kreativ-Tab-Sortierung
oder JEI-Konsistenz), nimmt die Hex-Paare aus der Tabelle als `backgroundColor` / `highlightColor`. Die fünf
Vanilla-Eier (Wither Skeleton, Ender Dragon, Snow Golem, Iron Golem, Wither) bleiben eigene Items, weil sie
eigene Icons haben. Offen: für 1.7.10-Entity-Id 51 mit `skelly_type = 1` ist in 1.21.1 `EntityType.WITHER_SKELETON`
direkt zu nehmen.

---

## Offene Punkte

- Kreativ-Tab der vier Barren (`IngotUranium`-Klasse setzt keinen; manifest calls nur `setUnlocalizedName`).
- Reihenfolge Config → Item-Registrierung für die dynamische maxDamage der Robot Kits (ItemSpiderRobotKit.java:17-20).
- `EntityCage` erhält seinen Index nur im Konstruktor (EntityCage.java:35, 46); ob ein gefangener Mob den Index vor dem
  Item-Drop ändert, ist Entity-Batch (EntityCage, 552-Zeilen-Gegenstück CritterCage.java:54 ff.).
- 03-items.md zählt 40 Speisen und 23 Wurfgeschosse; das manifest hat 33 `food` und (12 Steine + 5 Schuhe + 6 Ladungen)
  = 23 Wurfitems dieses Batches – die Speisezahl weicht ab, weil die Research Pizza und die sechs `ItemSeedFood` mitzählt.
