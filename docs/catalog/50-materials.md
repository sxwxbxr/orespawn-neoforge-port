# OreSpawn-Katalog

Erzeugt von `tools/catalog.py` aus dem Original-Jar (`orespawn-1.7.10-20.3.jar`, SHA-1 `d43dbe9a400dc8df06418da3e04d36422b2176d7`). Alle Zahlen stammen aus dem Jar; `?` heißt: statisch nicht auflösbar, nicht geraten.

Mehrere Werte in einer Zelle bedeuten Verzweigungen im Originalcode (zahm/wild, PlayNicely, Größenstufen).

## Werkzeugmaterialien

| Material | Stufe | Haltbarkeit | Effizienz | Schaden | Verzauberbarkeit |
|---|---|---|---|---|---|
| ULTIMATE | 10 | 3000 | 15 | 36 | 100 |
| NIGHTMARE | 3 | 1800 | 12 | 26 | 60 |
| REALEMERALD | 3 | 1300 | 10 | 6 | 75 |
| RUBY | 5 | 1500 | 11 | 16 | 85 |
| AMETHYST | 4 | 2000 | 11 | 11 | 70 |
| BERTHA | 3 | 9000 | 15 | 496 | 100 |
| CRYSTALWOOD | 2 | 300 | 3 | 2 | 15 |
| CRYSTALSTONE | 3 | 800 | 6 | 5 | 45 |
| CRYSTALPINK | 4 | 1100 | 10 | 7 | 65 |
| TIGERSEYE | 4 | 1600 | 12 | 8 | 75 |
| ROYAL | 3 | 10000 | 15 | 746 | 150 |
| HAMMY | 5 | 2000 | 15 | 82 | 100 |
| BATTLE | 3 | 1500 | 15 | 46 | 75 |
| CHAINSAW | 3 | 1500 | 10 | 56 | 75 |
| QUEENBATTLE | 3 | 2200 | 15 | 662 | 100 |

## Rüstungsmaterialien

Schutz je Teil: Kopf, Brust, Beine, Füße. Die `e_*`-Werte sind automatische Verzauberungsstufen des Originals.

| Material | Haltbarkeitsfaktor | Schutz | Verzauberbarkeit | Auto-Verzauberungen |
|---|---|---|---|---|
| ULTIMATE | 200 | 6 / 12 / 10 / 6 | 100 | {'e_respiration': 2, 'e_aquaaffinity': 3, 'e_protection': 5, 'e_fireprotection': 5, 'e_blastprotection': 5, 'e_projectileprotection': 5, 'e_featherfalling': 3} |
| MOBZILLA | 1000 | 7 / 13 / 11 / 7 | 150 | {'e_protection': 10, 'e_fireprotection': 10, 'e_blastprotection': 10, 'e_projectileprotection': 10, 'e_unbreaking': 5, 'e_featherfalling': 10} |
| LAVAEEL | 40 | 2 / 7 / 5 / 2 | 35 | {'e_respiration': 1, 'e_aquaaffinity': 2, 'e_protection': 3, 'e_fireprotection': 2, 'e_blastprotection': 10, 'e_featherfalling': 2} |
| MOTHSCALE | 50 | 2 / 7 / 5 / 2 | 50 | {'e_protection': 3, 'e_fireprotection': 3, 'e_blastprotection': 3, 'e_featherfalling': 5} |
| EMERALD | 60 | 3 / 8 / 6 / 3 | 40 | {} |
| EXPERIENCE | 70 | 5 / 9 / 7 / 4 | 50 | {'e_protection': 2, 'e_blastprotection': 1, 'e_featherfalling': 1} |
| RUBY | 90 | 4 / 9 / 8 / 4 | 40 | {} |
| AMETHYST | 100 | 4 / 8 / 7 / 3 | 40 | {} |
| PINK | 50 | 3 / 7 / 5 / 2 | 40 | {} |
| TIGERSEYE | 80 | 4 / 8 / 7 / 4 | 55 | {} |
| PEACOCK | 40 | 2 / 5 / 4 / 2 | 30 | {'e_featherfalling': 10} |
| ROYAL | 2000 | 8 / 14 / 12 / 8 | 200 | {'e_respiration': 1, 'e_aquaaffinity': 2, 'e_protection': 10, 'e_fireprotection': 10, 'e_blastprotection': 10, 'e_projectileprotection': 10, 'e_unbreaking': 5, 'e_featherfalling': 10} |
| LAPIS | 60 | 2 / 7 / 5 / 2 | 60 | {'e_respiration': 1, 'e_aquaaffinity': 1, 'e_protection': 1, 'e_projectileprotection': 1} |
| QUEEN | 1500 | 9 / 16 / 14 / 9 | 150 | {} |

## Mob-Werte aus der Config

| Config-Name | HP | Angriff | Verteidigung |
|---|---|---|---|
| Bee | 80 | 12 | 5 |
| Mantis | 120 | 16 | 10 |
| HerculesBeetle | 250 | 30 | 19 |
| Mothra | 150 | 12 | 8 |
| Brutalfly | 110 | 10 | 6 |
| Nastysaurus | 200 | 32 | 17 |
| Pointysaurus | 80 | 10 | 16 |
| Alosaurus | 110 | 18 | 8 |
| SpiderRobot | 1500 | 100 | 16 |
| AntRobot | 300 | 30 | 16 |
| Jeffery | 550 | 40 | 18 |
| Hammerhead | 240 | 75 | 20 |
| Molenoid | 200 | 18 | 12 |
| TRex | 160 | 22 | 14 |
| BandP | 100 | 1 | 18 |
| CaterKiller | 450 | 32 | 19 |
| Cryolophosaurus | 10 | 3 | 1 |
| Rat | 5 | 3 | 1 |
| Urchin | 25 | 10 | 4 |
| Kyuubi | 125 | 10 | 10 |
| GammaMetroid | 100 | 10 | 12 |
| Basilisk | 200 | 24 | 15 |
| EmperorScorpion | 350 | 35 | 20 |
| TrooperBug | 200 | 20 | 15 |
| SpitBug | 100 | 10 | 12 |
| Alien | 100 | 12 | 8 |
| WaterDragon | 150 | 20 | 8 |
| SeaMonster | 110 | 14 | 8 |
| SeaViper | 160 | 22 | 12 |
| Robot2 | 200 | 22 | 18 |
| Robot3 | 80 | 16 | 14 |
| Robot4 | 170 | 12 | 18 |
| Robot5 | 20 | 5 | 6 |
| Rotator | 35 | 10 | 8 |
| Vortex | 150 | 26 | 10 |
| DungeonBeast | 65 | 12 | 6 |
| Triffid | 100 | 20 | 12 |
| LurkingTerror | 30 | 6 | 5 |
| WormSmall | 10 | 3 | 0 |
| WormMedium | 30 | 10 | 8 |
| WormLarge | 90 | 18 | 14 |
| EnderKnight | 60 | 12 | 6 |
| EnderReaper | 90 | 18 | 8 |
| Irukandji | 1 | 20 | 0 |
| AttackSquid | 10 | 8 | 0 |
| CaveFisher | 10 | 4 | 4 |
| CloudShark | 15 | 6 | 5 |
| CreepingHorror | 10 | 3 | 2 |
| Mobzilla | 4000 | 175 | 21 |
| Kraken | 1000 | 40 | 10 |
| LeafMonster | 6 | 2 | 1 |
| Nightmare | 250 | 30 | 10 |
| Scorpion | 15 | 4 | 10 |
| Skate | 8 | 8 | 4 |
| TerribleTerror | 10 | 5 | 3 |
| TheKing | 7000 | 350 | 21 |
| TheQueen | 6000 | 225 | 21 |
| Leonopteryx | 150 | 20 | 8 |
| Crab | 180 | 24 | 16 |
