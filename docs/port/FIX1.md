# fix1 – Bugfixes aus der Bug-Suche W01–W07

Grundlage: `docs/port/BUGHUNT.md` (23 bestätigte Fehler, 1 strittiger Punkt) und
`docs/DECISIONS.md` R22 samt Nachtrag vom 2026-09-14.

## Integration

Stand 2026-09-14. `./gradlew build` grün, `./gradlew runGameTests` grün
(**249 von 249** Pflicht-Tests, darunter die zwei neuen aus `Fix1GameTests`).

### Zusammengeführt

| Porter | Dateien | Inhalt |
|---|---|---|
| fix1-helpers-plants | `CannonFodderSupport`, `MonsterSupport`, `OneUse`, `WorldGenAbstractTree`, `CropBlocks`, `ReedLikePlant`, `MyBlockFlower`, `MothSupport`, `CompanionSupport`, `RiderSupport`, `WormSupport` | gemeinsame Helfer auf 1.21.1-Tags (`#minecraft:dirt`, `sand`, `leaves`, `small_flowers`, `tall_flowers`, `#c:dyes`) |
| fix1-gear | `ItemMinersDream`, `InstantGarden`, `UltimateSword`, `UltimatePickaxe`, `UltimateHoe`, `CrystalShovel`, `BigHammer`, `ItemSifter`, `W03GameTests` (Block-Map), `catalog/README.md` | Miner's Dream nach R22, Flag 2 → `UPDATE_CLIENTS \| UPDATE_KNOWN_SHAPE` im Instant Garden, Big Hammer `extends BlockingSword` (3000) |
| fix1-entities | `Termite`, `Beaver`, `Molenoid`, `Camarasaurus`, `Alien`, `OreSpawnTeleporter`, `Girlfriend`, `Boyfriend`, `EntityCannonFodder`, `Gazelle`, `Hydrolisc` | Tag-Kategorien, Soul Torches in `Alien.isTorch`, Teleporter-Rückkehr ab `WORLD_SURFACE` + 1 außerhalb der sechs Dimensionen, `die`-Override ohne Besitzernachricht |
| fix1-registry-scans | `models/item/egggirlfriend.json`, `eggboyfriend.json`, `eggbasilisc.json`, `data/neoforge/data_maps/item/furnace_fuels.json`, `W07.md` | drei Ei-Modelle, Brennwert 300 für `crystalplanks`, `crystaltreelog`, `skytreelog`, `duplicatortreelog`, `crystalworkbench` |

Vom Integrator in geteilte Dateien eingetragen:

- `registry/ModItems.java`: `EGG_GIRLFRIEND` (`egggirlfriend`, 9105) vor
  `EGG_RED_COW`, `EGG_BASILISK` (`eggbasilisc`, 9115) zwischen `EGG_HYDROLISC` und
  `EGG_DRAGONFLY`, `EGG_BOYFRIEND` (`eggboyfriend`, 9399) zwischen
  `MOBZILLA_BOOTS` und `ROYAL_HELMET`. Tab und Dispenser kommen über
  `ItemSpawnEgg.all()` / `SpawnEggSetup`, keine weitere Zeile nötig.
- `item/utility/OneUse.java`: Klasse und `isDirt` sind jetzt `public`, weil
  `item.tool.UltimateHoe` sie aufruft. Der Rest von `OneUse` bleibt paketprivat.
- `gametest/W03GameTests.java`, `minersDreamLeavesCutLiquidsAndUnsupportedBlocksStill`:
  Nach R22 gräbt der Tunnel jeden Nicht-Erz-Block, die alten Sonden bei y = 4
  wären selbst weggegraben worden. Fackel und Fallblock stehen jetzt in der
  Dachreihe (y = 7) auf dem gegrabenen Stein bei y = 6. Der Fallblock ist
  `white_concrete_powder` statt Sand, weil Sand in der Dachreihe von `needsRoof`
  durch Bruchstein ersetzt würde. Die Aussage des Tests bleibt dieselbe.
- `gametest/W07GameTests.java`: nur die zwei Javadoc-Sätze „die Basilisk hat
  kein Ei“ korrigiert.
- neu `gametest/Fix1GameTests.java` (Batch `fix1_wiring`):
  `missingSpawnEggsAreRegistered` (drei Eier, je `ItemSpawnEgg` mit dem richtigen
  Typ) und `woodBlocksBurnInTheVanillaFurnace` (fünf Items,
  `getBurnTime(RecipeType.SMELTING) == 300`). Erst dieser Lauf belegt, dass die
  Data-Map gelesen wird.

Compile-Fehler gab es keine: der erste Build nach dem Einfügen war grün.

GameTest-Nachzug: `Fix1GameTests` hat jetzt je Befund einen Test (Miner's Dream in
Granit/Diorit/Andesit/Tuff/Deepslate mit Erz, Bedrock und Truhe; Chainsaw fällt einen
Kirschbaum; Setzling und Mais auf Moos und Mud; Wurm in Mud gegen Glas als Kontrolle;
Lizard/weißer Farbstoff; Girlfriend/Kornblume gegen Löwenzahn; Termite frisst Kirschplanken;
Beaver fällt Fichtenzaun; Rückkehr-Teleporter auf eine Säule bis Y 195; keine
Besitzernachricht bei Girlfriend und Boyfriend gegen einen zahmen Wolf als Kontrolle; Big
Hammer blockt; drei Eier spawnen; Instant Garden neben Wasser und unter Betonpulver).
`./gradlew runGameTests`: **261 von 261** grün, Log ohne ERROR-Zeilen. Falle beim Schreiben:
`Girlfriend.hurt` deckelt jeden Treffer auf 10 (Original :1078-1115), `kill()` allein tötet
sie nicht.

### Was die nächsten Wellen wissen müssen

- **Tag-Helfer sind die Schnittstelle.** Neue Mobs und Items, die „Erde“,
  „Sand“, „Laub“, „Blume“ oder „Farbstoff“ prüfen, rufen die Helfer aus
  `CannonFodderSupport`, `MonsterSupport`, `OneUse`, `CropBlocks`, `ReedLikePlant`
  und `RiderSupport` auf, statt eine eigene Blockliste zu schreiben. Hat das
  Original einen eigenen Gras- oder Löwenzahnzweig, wird dieser Zweig **vor** dem
  Tag geprüft (R22-Nachtrag).
- **Zahme Tiere und `die`.** Jede `TamableAnimal`-Unterklasse eines späteren
  Wellenpakets überschreibt `die` ohne Besitzernachricht. Vorlage:
  `EntityCannonFodder.die` (Zeile für Zeile `LivingEntity.die` aus NeoForge
  21.1.248). Unterklassen von `EntityCannonFodder` erben es bereits. Ändert ein
  NeoForge-Update `LivingEntity.die`, müssen die sechs Kopien (`EntityCannonFodder`,
  `Gazelle`, `Girlfriend`, `Boyfriend`, `Camarasaurus`, `Hydrolisc`) mitziehen.
- **Brennwert.** Jeder spätere Block mit `Material.wood` im Original bekommt
  einen Eintrag in `data/neoforge/data_maps/item/furnace_fuels.json` (300) und
  eine Zeile in `Fix1GameTests.woodBlocksBurnInTheVanillaFurnace`.
- **Spawn-Eier.** W11 (EasterBunny verschenkt Eier) kann `ModItems.EGG_GIRLFRIEND`,
  `EGG_BOYFRIEND` und `EGG_BASILISK` direkt benutzen.
- **Teleporter.** `OreSpawnTeleporter` sucht außerhalb der sechs OreSpawn-Dimensionen
  ab `WORLD_SURFACE` + 1, gedeckelt auf die Bauhöhe; innerhalb bleibt die 180.

### Offen, bewusst nicht angefasst (außerhalb von BUGHUNT, „nichts anderes ändern“)

- `block/tree/BlockSkyTreeLog.java:56` und `:76` schreiben Luft nur mit
  `UPDATE_CLIENTS` (Original Flag 2). Beim Fällen eines Himmelsbaums fallen
  Sand/Kies neben dem Stamm, Wasser fließt nach. Fix wie beim Instant Garden,
  vorher prüfen, dass der Laubzerfall nicht an diesem Form-Update hängt.
- Gleiches R22-Muster, von BUGHUNT nicht gelistet: Beaver-Spawnboden listet
  Erde/Grobe Erde/Podsol/Grasblock einzeln; `Camarasaurus.isPlantAt` listet die
  sechs Doppelpflanzen ohne Pitcher Plant; `OreSpawnTeleporter.isGroundBlock`
  (toter Code) behält die schmale Liste; `ItemMinersDream.needsRoof` hält Kies
  als Einzelblock.
- `W03GameTests.java:246` spricht im Javadoc noch von „five classes“ beim
  Schwertblock; inhaltlich deckt die Map inzwischen auch den Big Hammer ab.
- Die Liste der nicht zugewiesenen Hand-off-Punkte aus W01–W07 (Prüfung 4.5)
  steht im Bericht von fix1-registry-scans: `getCanSpawnHere` bei der
  Chunk-Generierung (Regel vor W12 nötig), Vanilla-Dekoration in Crystal/Chaos
  gegen R21, fünffaches `legacyPanic` gegen R21, AI-Klassen noch in
  `entity.portal`, fehlende 1.7.10-Goals, Dauer des VelocityRaptor-Tempos,
  Katalogkorrekturen aus W07, Large Worm als `CREATURE`, Stronghold-Positionen in
  Utopia, R4/R5-GameTest auf echter VirtualHealth-Entity (W09), veraltetes
  `EventBusSubscriber.bus()`, Scheren-Verhalten von 1.7.10-`BlockLeaves`.
