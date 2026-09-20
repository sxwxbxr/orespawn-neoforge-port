# Wiederaufsatzpunkt

**Stand: 2026-09-10.** Etappe 1 (Recherche und Katalog) läuft, noch keine Zeile
Spiel-Java außer generierter Modellgeometrie.

## Was der Mod ist

Privater 1:1-Port von **OreSpawn** (TheyCallMeDanger, 1.7.10 Build 20.3) auf
NeoForge 1.21.1. modid `orespawn`, Paket `com.swbr.orespawn`. Nicht zur
Veröffentlichung — siehe `docs/DECISIONS.md`, R0.

## Was fertig ist

| | |
|---|---|
| Recherche | `docs/research/01`–`06`: Mobs, Dimensionen, Items, Lizenz/Ports, Jar-Inventar, Modelle |
| Referenz | `reference/` (git-ignoriert): Original-Jar mit SHA-1, dekompilierter Quelltext, MCP-Mappings, 109 Modell-JSONs |
| Katalog, generiert | `docs/catalog/manifest.json` + Tabellen 10–60, `classes.json`, `batches.json` (`tools/catalog.py`) |
| Entscheidungen | `docs/DECISIONS.md` R0–R17, bindend |
| Bauregeln | `docs/STYLE.md` |
| Gradle-Gerüst | baut grün |
| Assets | 1058 Texturen, 307 OGG, `sounds.json`, `en_us.json` (`tools/assets.py`) |
| Modellgeometrie | 109 `*Geometry.java`, 3584 Teile (`tools/gen_models.py`); Stichprobe Kraken gegen Quelltext belegt |
| Sounds | `registry/ModSounds.java`, 126 Events (`tools/gen_sounds.py`) |
| Config | `config/OreSpawnConfig.java`, 624 Schlüssel als COMMON (`tools/gen_config.py`), `EarlyConfig` für Registrierungswerte |
| Biome | `docs/catalog/biome_map.json`, 33 Originalbiome → 1.21.1, gegen `Biomes.java` geprüft |
| Welle 0 belegt | `W00GameTests`: 3/3 grün auf dediziertem Server (Sounds, Config, EarlyConfig) |
| Wellen-Workflow | `tools/workflows/wave.js`: Portierer (Fable) → Integrator → GameTests → Treue-Review → Nacharbeit |

## Was läuft

Workflow **orespawn-catalogue**, Run `wf_8c835210-ea1`: Verhalten aller 586
Klassen aus dem Quelltext (`docs/catalog/verhalten/`), Design auf Fable 5.1
(`docs/catalog/design/`), Lückenprüfung, `docs/catalog/README.md` mit
Abhängigkeitswellen.

Fortsetzen nach Abbruch:

    Workflow({
      scriptPath: "F:/Repositories/Modpacks/mods/orespawn/tools/workflows/catalogue.js",
      resumeFromRunId: "wf_8c835210-ea1",
      args: <Inhalt von docs/catalog/batches.json>
    })

Der erste Lauf brach um 13:10 am Sitzungslimit ab (27 von 42 Agenten fertig,
10,7 Mio. Subagenten-Token). Um 15:15 wurde er fortgesetzt. Der Pfad unter
`~/.claude/…/workflows/scripts/` wird beim Fortsetzen abgelehnt, deshalb liegt
das Skript jetzt im Repo.

**Katalog abgeschlossen** (2026-09-10, 16:00): 351/351 Klassen mit
Verhaltenskapitel, alle Design-Kapitel, `recipes.json` (381 Rezepte, alle Ids
aufgelöst), `docs/catalog/README.md` mit zwölf Wellen W01–W12. Die Widersprüche
aus 6.2 und alle Fragen aus 6.7 sind in `DECISIONS.md` entschieden (R9, R15, R18).

## Der nächste Schritt

Wellen nacheinander mit `tools/workflows/wave.js`. Die Argumente jeder Welle
liegen fertig unter `docs/port/waves/w01.json` … `w13.json` (aus
`docs/catalog/README.md` Abschnitt 4 abgeleitet). Abweichung vom Katalogplan:
`GenericDungeon` (7111 Zeilen) ist aus W12 herausgelöst und bildet **W13** mit
drei Portierern nach Zeilenbereichen; W12 baut dafür die Struktur-Infrastruktur. Code-Agenten auf
Fable 5.1 (Nutzerwunsch), Arbeiter schreiben nur ihre Dateien, ein Integrator
baut. Jede Welle endet mit `gradlew build` und GameTests.

| Welle | Stand | Run | Beleg |
|---|---|---|---|
| W01 Fundament | **fertig** 2026-09-10 16:45 | `wf_eb09bda4-c2e` | selbst nachgeprüft 16:51: `build` grün, 15/15 GameTests, 17/17 Unit-Tests; 2 Treue-Befunde (Wander-KI-Takt) behoben; Übergabe in `docs/port/W01.md` |
| W02 Rohstoffe, Blöcke, Pflanzen | **fertig** 2026-09-10 22:35 (1. Anlauf 16:50 am Sitzungslimit abgebrochen) | `wf_00638c1f-bbb` | selbst nachgeprüft 22:39: `build` grün, 37/37 GameTests, 17/17 Unit-Tests, 0 ERROR im Serverlog; 194 Blöcke, 80 Items; 1 Loot-Fehler durch Tests, 9 Treue-Befunde behoben; R18-Zeile Titan/Uran-Glühen korrigiert; Übergabe `docs/port/W02.md` |
| W03 Ausrüstung und Werkstatt | **fertig** 2026-09-11 (angehalten 2026-09-10 23:27, per `resumeFromRunId` über den Sitzungswechsel fortgesetzt) | `wf_a5bc41c8-1f3` | selbst nachgeprüft 09:54: `build` grün, 53/53 GameTests, 17/17 Unit-Tests, 0 ERROR, 0 `TODO W03`; 49 Werkzeuge/Waffen, 56 Rüstungsteile, 14 ArmorMaterials, Kristallofen/-werkbank; 3 Treue-Befunde behoben (u. a. Schwert-Blocken wiederhergestellt); Übergabe `docs/port/W03.md` |
| Nachtrag R19 (Hauptsitzung) | vier einfache Schwerter blocken mit ihrer Jar-Dauer (3500/300/4000/3000) | — | mit W04 belegt: GameTest grün |
| W04 Projektile, Girlfriend/Boyfriend | 1. Anlauf 10:11 vor der Integration abgebrochen: 5 von 6 Portierern am **Fable-Limit** gescheitert (Schutzregel hat gegriffen), nur `w04-ai` fertig; `entity/rock` und `item/rock` enthalten halbe Dateien. Nutzer entschied: Code-Agenten auf Opus 5 (`codeModel` weglassen), `w04-ai` behält `model: 'fable'` und kommt aus dem Cache. 2. Anlauf 10:14–10:53: die fünf Portierer auf Opus 5 fertig, `w04-ai` verfehlte den Cache (Optionen in anderer Schlüssel-Reihenfolge), lief erneut auf Fable und scheiterte am Limit. 3. Anlauf mit wiederhergestellter Reihenfolge `label, phase, model, schema`: `w04-ai` traf den Cache wieder nicht und scheiterte erneut am Fable-Limit. 4. Anlauf: `w04-ai` ohne Modellvorgabe (Opus 5), seine Dateien aus dem ersten Lauf liegen im Baum. **Lehre:** auf einen Cache-Treffer eines Agenten mit anderem Modell nicht bauen — wenn das Modell gesperrt ist, sofort umstellen. **Fertig** 2026-09-11 12:45 | `wf_823ed223-a43` | selbst nachgeprüft 12:49: `build` grün, **85/85 GameTests**, 17/17 Unit-Tests, 0 ERROR, 0 `TODO W04`; 19 EntityTypes, 35 Items, `EntityAIMoveIndoors` nachgebaut; 8 Treue-Befunde behoben; drei Querschnittsfragen als R20 entschieden; Übergabe `docs/port/W04.md` |
| W05 Dimensionen, Terrain, Portaltiere | **fertig** 2026-09-11 14:35 (Code-Agenten auf Opus 5; `w05-crosscut-r20` zog R20 in W01–W04 nach) | `wf_2901fc7b-e23` | selbst nachgeprüft 14:41: `build` grün, **101/101 GameTests**, 17/17 Unit-Tests, 0 ERROR, 0 „far chunk", 0 `TODO W05`; sechs Dimensionen erzeugen FULL-Chunks, Labyrinth Y 24–28, alle Portal-Reisen hin und zurück; 2 Treue-Befunde behoben (Stronghold-Zahl, Chaos-Schlaf); offene Punkte als R21 entschieden; Übergabe `docs/port/W05.md` |
| W06 Friedliche Tiere, Battle Mobs | **fertig** 2026-09-11 16:40 (Opus 5; `w06-crosscut-r21` zog R21 nach) | `wf_eddd5477-299` | selbst nachgeprüft 16:44: `build` grün, **154/154 GameTests**, 17/17 Unit-Tests, 0 ERROR, 0 `TODO W06`; 29 EntityTypes, 28 Spawn-Eier; R21-Feldtest 5×5 Chunks Mining/Utopia: 0 Far-Chunk-Schreibzugriffe (die eine Log-Zeile mit „far chunk" ist die INFO-Ausgabe des Tests selbst); Doppelklassen entfernt; 3 Treue-Befunde behoben (KI-Takt der Vanilla-Goals, Blattzerfall beim Beaver, Whale-Kalb-Füttern); Übergabe `docs/port/W06.md` |
| W07 Feindliche Landmobs, Roboter, Würmer | Bau, Integration, Tests fertig 2026-09-11; 2 von 3 Treue-Prüfern am **Wochenlimit** gescheitert (Reset 13.09. 20:00). Am 13.09. 20:10 fortgesetzt, nur Prüfer 2/3 + Nacharbeit laufen neu. **Achtung:** die Argumente dieses Runs stehen nicht in `docs/port/waves/w07.json` (inline mit Crosscut-Portierer gestartet) — sie stehen im Workflow-Journal | `wf_7c2e84dc-322` | **fertig** 13.09. 20:25; selbst nachgeprüft 20:28: `build` grün, **214/214 GameTests**, 0 ERROR, 0 `TODO W07`; 4 Treue-Befunde behoben (EnderKnight-Ausweichen, Kyuubi-Feuer, Fairy-Besitzername, Molenoid-Lichttest) |
| W08 Wasser-, Kristall-, Flugmobs, Nightmare, Mothra, Inseln | läuft seit 13.09. ~20:32 (Opus 5); Crosscut-Portierer `w08-crosscut-w07` (Lichttest Alien/Hammerhead/LeafMonster, `isValidLightLevel`-Kopien zusammenführen, Hoverboard-Flake); Argumente in `docs/port/waves/w08.json` | `wf_04304b2e-3e1` | **fertig** 13.09. ~21:35; selbst nachgeprüft 21:41: `build` grün, **247/247 GameTests**, 0 ERROR, 0 `TODO W08`; kein Schreibzugriff außerhalb von `mods/orespawn` (Sicherheitsklassifikator war bei drei Portierern rate-limitiert, deshalb geprüft); 1 Treue-Befund (Crab-Skalierung) behoben. **Offen, braucht Entscheidung:** R20-Ausnahme für die `(int)`-X/Z-Kürzung mit `xoff/zoff` in Island/IslandToo |
| Bug-Suche W01–W07 (Nutzerwunsch nach Spieltest) | läuft parallel zu W08, streng lesend: 6 Suchende, je Fund 2 Skeptiker, Bericht nach `docs/port/BUGHUNT.md`. Anlass: Miner's Dream ließ Andesit/Diorit/Granit stehen → **R22** (1.7.10-Blocklisten decken die ganze 1.21.1-Kategorie ab; Miner's Dream räumt alles außer `#c:ores`, Unzerstörbarem und Block-Entities) | `wf_8feff1d5-664` | **fertig** 20:58: 30 Kandidaten, 23 bestätigt (1 high = Miner's Dream, 13 medium, 9 low; 18 davon R22-Listen), 1 strittig (Alien ignoriert Soul Torches). Nur `BUGHUNT.md` geschrieben (W08.md stammt vom parallelen W08). Hinweis: der Sicherheitsklassifikator war während der Prüfer rate-limitiert — die Befunde vor dem Fixen einzeln gegenlesen |

**AUSLIEFERUNG (Nutzer, 14.09.):** Wenn der komplette Mod für einen ersten Testlauf
fertig ist (W09–W11 gebaut und nachgeprüft), eine **Kopie** von
`build/libs/orespawn-<ver>.jar` nach
`C:\Users\swbr\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\mods` legen.
Instanz: MC 1.21.1, NeoForge 21.1.250 (passt zu `[21.1.248,)`), dort liegt bisher nur
JEI. Der Mod hat keine Bibliotheksabhängigkeit. Vorher Version erhöhen (CLAUDE.md:
lokal gebauter Mod braucht bei inhaltlicher Änderung eine neue Version) und eine
ältere OreSpawn-Kopie im Ordner ersetzen, nicht daneben legen.

**BUDGET-REGEL (Nutzer, 14.09.):** Nicht über 50 % des Wochenlimits, außer der Mod
ist fast komplett fertig. Das Limit ist hier nicht direkt sichtbar; Schätzung über
Subagenten-Token: die Vorwoche lief nach ~55 Mio. Subagenten-Token ins Limit →
konservativ 100 % ≈ 50 Mio., 50 % ≈ 25 Mio. Seit dem Reset (13.09. 20:00): W07-Rest
0,75 + W08 4,4 + Bug-Suche 8,1 = **~13 Mio. ≈ 26 %**. Plan innerhalb der Grenze:
Fix-Welle `fix1` (Run `wf_6bb34d9e-915`, ohne Treue-Prüfer, ~4 Mio.) und danach W12
(~6 Mio., `reviewGroupSize` 8) → ~46 %. Dann anhalten und den Nutzer nach dem echten
Prozentwert fragen. Restbedarf W13+W09+W10+W11 ≈ 20 Mio.

- **fix1 fertig** 14.09. ~08:05, kostete nur 1,2 Mio. (ohne Prüfer); selbst
  nachgeprüft 08:10: `build` grün, **261/261 GameTests**, 0 ERROR; Bericht
  `docs/port/FIX1.md`. Offen daraus: `BlockSkyTreeLog` Luftschreiben ohne
  `UPDATE_KNOWN_SHAPE` (Blattzerfall-Wechselwirkung klären), weitere R22-Fälle
  (Beaver-Spawnboden, Camarasaurus Pitcher Plant). Budget danach ~14,2 Mio. ≈ 28 %.
- **W12 läuft** seit 14.09. ~08:15, Run `wf_4562f230-1fb`, Argumente in
  `docs/port/waves/w12.json` (mit Crosscut `w12-crosscut-r21`: Crystal/Chaos-Decorator,
  `legacyPanic` zusammenführen).
- **W12 fertig** 14.09. ~09:55 (3,2 Mio.); selbst nachgeprüft 09:59: `build` grün,
  **271/271 GameTests**, 0 ERROR, 0 Far-Chunk-Schreibzugriffe, 0 `TODO W12`. Offene
  Frage `getCanSpawnHere` bei Chunk-Generierung als **R23** entschieden (1:1 laut
  Bytecode), dazu R23 „Strukturschreiber ohne Nähte vor W13". Budget ~17,4 Mio. ≈ 35 %.
- **W13 läuft** seit 14.09. ~10:05 (GenericDungeon + `w13-writer`), Argumente in
  `docs/port/waves/w13.json`.
- **Kalibrierung (Nutzer, 14.09. ~10:10): 12 % Wochenlimit, 24 % Sitzungslimit** bei
  ~17 Mio. Subagenten-Token seit Reset → **100 % Woche ≈ 140 Mio.** (die alte Schätzung
  aus der Fable-Woche lag um den Faktor ~3 zu hoch). Restplan W13 + W09–W11 +
  abschließende Bug-Suche ≈ 27 Mio. ≈ +19 % → Ende bei ~31 %. **Alle Wellen laufen
  weiter**, nacheinander; Sitzungslimit beachten (Wellen sind per `resumeFromRunId`
  fortsetzbar).
- **W13 fertig** 14.09. ~11:35 (2,9 Mio.; Workflow meldete 280/280). **Meine
  Nachprüfung 11:39: 279/280 — der Dimensionstest schlug fehl** (Kyuubi-Dungeon in
  Mining: eine aufgezeichnete Blockschreibung von der Dekoration überschrieben).
  Ursache ist die offene Reihenfolgefrage aus W13.md → als **R24** entschieden
  (OreSpawn-Strukturen nach der Dekoration, wie `IWorldGenerator` in 1.7.10). Ein
  einzelner Fix-Agent setzt R24 um und macht den Test streng; danach W09.
  Budget ~20,3 Mio. ≈ 14–15 % (kalibriert).
- **R24 fertig** 14.09. ~13:20 (Fix-Agent, 0,8 Mio.; zweite Runde nach meinem
  gescheiterten Kontrolllauf 12:26 im End); selbst nachgeprüft 13:25: **280/280**,
  0 ERROR. Überlappungsregel in R24 angenommen. **Offen für die Schluss-Bug-Suche:**
  King-Altar-Chunks brauchen im Test 9–30 s bis FULL (Aufzeichnung baut die ganze
  Struktur) — im Spiel prüfen/messen.
- **W09 fertig** 14.09. ~14:40 (4,4 Mio.; Workflow 300/300). Meine Nachprüfung
  14:48: **299/300** — `W08GameTests.attacksquidloseshealthonland` flaky (zufällige
  Position). Dazu: Verhaltens-GameTests für W09 (Zähmen, Reiten, Umwandlung) wurden
  nicht geschrieben. Beides als Crosscut in W10; W10-Tests laufen dreimal.
- **W10 fertig und selbst nachgeprüft** (14.09. 16:26): 334/334 GameTests, 0 ERROR, 0 `TODO W10`, 4,7 Mio. Tokens. Offene Punkte als R25 (Bossleiste = W11-Overlay, Köpfe einheitlich) und als Crosscut in W11. Budget ~30 Mio. ≈ 21–22 %.
- **W11 fertig und selbst nachgeprüft** (14.09. 17:34): 343/343 GameTests, 0 ERROR, kein einziges `TODO W0x/W1x` mehr im Baum, 1,6 Mio. Tokens. Budget ~31,6 Mio. ≈ 22–23 %.
- **Abschluss-Bugsuche fertig** (8,2 Mio.): 15 bestätigte Fehler in `docs/port/BUGHUNT2.md`, Urteile in R26. Budget ~40 Mio. ≈ 28–29 %.
- **fix2 fertig und selbst nachgeprüft** (14.09. 19:45): 363/363 GameTests, 0 ERROR. Nachgezogen von Hand: Raptor-Tempo fällt auch bei Dimensionswechsel und Speed/Slowness weg (R26), PORT-Kommentar zu RTP-Zufallsfolge.
- **0.2.0 ausgeliefert**: `orespawn-0.2.0.jar` (Hash geprüft) in der Prism-Instanz 1.21.1. **Erster Client-Start steht aus** – nie gerendert oder gespielt (R17). Nächster Schritt: Logs/Befunde des Nutzers aus dem Testlauf.
- **Gesamtverbrauch bis hier** (exakt aus den Transkripten, `usage_total.py` im Scratchpad): 367 Agenten, 8836 Anfragen, 2,36 Mrd. Tokens inkl. 2,22 Mrd. Cache-Lesen; zu API-Listenpreisen ≈ 2230 USD.

~~**PAUSE (Nutzer, 13.09. ~20:40):**~~ aufgehoben am 14.09. — Nach W08 und der Bug-Suche **keine neuen Agenten oder Workflows starten** — der Nutzer braucht sein Kontingent für die kommende Woche. Erst auf ausdrückliches Weitermachen hin fortsetzen, dann mit den Punkten unten.

**Nach W08 und der Bug-Suche:** (1) `wave.js` auf „R0-R22" stellen (während W08 bewusst nicht geändert, sonst verfehlt ein Resume den Cache); (2) Fix-Welle für `BUGHUNT.md`; (3) **Reihenfolge geändert:** W12 und W13 (Weltgenerierung, Riesenbäume, Strukturen, wilde Pflanzen) kommen vor W09–W11, weil der Nutzer im Spiel genau das vermisst. Spawner und Bäume, die Mobs aus W09–W11 brauchen (King/Queen, Reittiere, Käfige), werden als `TODO W09/W10/W11` gelassen und dort geschlossen.

### Morgen als Erstes

1. W03 fortsetzen — fertige Agenten kommen aus dem Cache, der Rest läuft neu:

       Workflow({ scriptPath: "F:/Repositories/Modpacks/mods/orespawn/tools/workflows/wave.js",
                  resumeFromRunId: "wf_a5bc41c8-1f3", args: <Inhalt von docs/port/waves/w03.json> })

   Der Baum enthält halb geschriebene W03-Dateien; die Portierer-Anweisung sagt
   ausdrücklich, vorhandene Dateien aus einem abgebrochenen Versuch fertigzustellen.
   Der letzte **geprüfte** Stand ist W02 (37/37 GameTests). Weil es keinen Commit
   gibt, ist dieser Stand nicht wiederherstellbar, falls W03 etwas zerstört — den
   Nutzer fragen, ob nach jeder geprüften Welle committet werden soll.
2. Nach W03: selbst `./gradlew build runGameTests` laufen lassen, dann W04 bis W13
   in derselben Art.

`wave.js` hat seit dem W02-Abbruch zwei Schutzregeln: ein Portierer ohne
Ergebnis bekommt einen zweiten Versuch, und fehlt dann noch einer, stoppt die
Welle vor der Integration (Rückgabe `aborted`). Beim ersten Anlauf lief der
Integrator auf null Portierer-Ergebnissen los.

Nach Abbruch einer Welle:

    Workflow({ scriptPath: "F:/Repositories/Modpacks/mods/orespawn/tools/workflows/wave.js",
               resumeFromRunId: "<Run>", args: <Inhalt von docs/port/waves/wNN.json> })

Nach jeder Welle selbst nachprüfen: `./gradlew build runGameTests` und die
JUnit-XML unter `build/test-results/test/` zählen — der Bericht des Workflows
ist eine Behauptung, der Lauf ist der Beleg.

Das Mod-Repo hat noch **keinen Commit**; es wurde nichts committet, weil kein
Auftrag dazu vorlag.

## Umgebungsnotizen

- `JAVA_HOME` nicht setzen, der Wrapper findet das lokale JDK 21.
- Die entpackten 1.21.1-Quellen liegen unter
  `F:/Repositories/Modpacks/mods/architrave/.cache/sources/`.
- Workflow-Skripte dürfen kein CR enthalten.
