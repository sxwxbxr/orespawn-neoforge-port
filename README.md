# OreSpawn → NeoForge 1.21.1

Ein zeilengetreuer Port von **OreSpawn** (TheyCallMeDanger, Minecraft 1.7.10,
Build 20.3) auf **NeoForge 21.1.248 / Minecraft 1.21.1**. 134 Entities, 473
Items, 211 Blöcke, 109 Modelle, sechs eigene Dimensionen, 628 Config-Schlüssel —
aus dem Originaljar rekonstruiert, nicht nacherfunden.

Das Repo enthält den **Quelltext, die Werkzeuge und die vollständige
Portierungsdokumentation**. Die Originaltexturen, -sounds und die daraus
erzeugten Indizes sind **absichtlich nicht enthalten** (siehe
[Assets](#assets) und [Rechtliches](#rechtliches)).

---

## Rechtliches — bitte zuerst lesen

OreSpawn ist **nicht freigegeben**, weder zum Weiterverbreiten noch zum Portieren.
Die Belege stehen mit Quellenangabe in
[`docs/research/04-history-license-ports.md`](docs/research/04-history-license-ports.md):

| | |
|---|---|
| Lizenz seit 01.10.2020 | „No permission is given to redistribute OreSpawn by itself, **in whole or in part**." |
| Autor, seit 11/2021 unverändert online | „OreSpawn on MC is no longer available. … And no, you CANNOT have any rights to it whatsoever. **I DO NOT WANT IT PORTED.**" |
| Fremde Rechte | Nach Angabe des Autors gehört rund ein Viertel des Materials — überwiegend Kreaturmodelle — 19 namentlich genannten Modellierern, die die Rechte **nur ihm** eingeräumt haben. |
| Durchsetzung | Zwei Ports wurden daraufhin eingestellt (InsanityCraft, jtrent238/OreSpawnDzPort). |

Daraus folgt für dieses Repo:

- **Keine Assets im Repo.** Kein Bild, kein Ton, kein Modell des Originals wird
  hier weiterverbreitet. Wer den Mod bauen will, braucht seine **eigene** Kopie
  des Originaljars.
- **Keine Freigabe.** Der Quelltext steht unter *All Rights Reserved* und ist
  eine Ableitung eines fremden, geschützten Werks. Er ist hier einsehbar,
  nicht nutzungsfrei. Eine Datei `LICENSE` gibt es deshalb bewusst nicht — es
  gibt nichts zu lizenzieren, was mir gehörte.
- **Kein fertiger Mod.** Es gibt hier keine Release-Jars und wird keine geben.
- **Kein Ersatz für einen legalen Nachfolger.** Wer OreSpawn-Gefühl *mit*
  eigenen Assets sucht: [Chaos Awakens](https://chaosawakens.github.io/) und
  **Antarchy** (NeoForge 1.21.1) sind genau dafür gebaut.

Wenn ein Rechteinhaber — Richard H. Clark oder einer der Modellierer — die
Entfernung dieses Repos wünscht, genügt eine Nachricht über Issues; es wird dann
gelöscht, nicht diskutiert.

---

## Assets

`tools/assets.py` erzeugt die fehlenden Dateien aus einer eigenen Kopie des
Originaljars — byteweise dieselben Bilder und Töne, nur unter den kleingeschriebenen
Pfaden, die 1.21.1 verlangt.

```bash
# Eigene Kopie von orespawn-1.7.10-20.3.jar (SHA-1 d43dbe9a400dc8df06418da3e04d36422b2176d7)
mkdir -p reference/jar/extracted
cd reference/jar/extracted && unzip ../orespawn-1.7.10-20.3.jar && cd -

python tools/assets.py
# textures copied: 1058, armor layers: 28, ogg copied: 307, sound events: 126, lang keys: ...
```

Erzeugt wird alles, was `.gitignore` sperrt:
`assets/orespawn/textures/`, `assets/orespawn/sounds/`, `sounds.json`,
`lang/en_us.json`. Ohne diesen Schritt baut Gradle zwar, aber der Mod zeigt im
Spiel nur fehlende Texturen.

---

## Bauen

Gebraucht wird JDK 21. Der Wrapper findet es selbst; `JAVA_HOME` nicht setzen.

```bash
./gradlew build          # Jar nach build/libs/
./gradlew test           # 17 Unit-Tests
./gradlew runGameTests   # 385 GameTests auf einem echten dedizierten Server
./gradlew runServer      # dedizierter Server bis "Done"
```

---

## Was „fertig" hier heißt

Auf der Maschine, auf der dieser Port entstand, gibt es **keinen Minecraft-Client**.
Belegbar ist damit ausschließlich:

- `./gradlew build` grün,
- **363/363 GameTests** grün (Stand 0.2.0): jede Entity spawnt und tickt 100 Ticks
  ohne Ausnahme, jeder Block lässt sich setzen, jede Dimension erzeugt FULL-Chunks,
  jede Portalreise hin und zurück,
- `runServer` bis `Done` ohne `Failed to create mod instance`, 0 ERROR-Zeilen.

**Gerendert oder gespielt ist damit nichts.** Ein grüner Serverstart sagt über den
Client nichts — Mixins und Renderer auf `net.minecraft.client` lädt ein dedizierter
Server nie. Das gehört in jede Fertigmeldung und steht deshalb auch hier.

---

## Wie der Port entstanden ist

Nicht von Hand. Der Ablauf war dreistufig und in jeder Stufe auf Belege statt auf
Erinnerung gebaut — 1.7.10-Code aus dem Gedächtnis zu portieren erzeugt Mods, die
laden und sich falsch verhalten.

### 1. Katalog statt Annahmen

`tools/catalog.py` liest das Originaljar und den dekompilierten Quelltext und
erzeugt `docs/catalog/manifest.json` (1,1 MB): jede Registrierung, jeder
Mob-Wert, jedes Werkzeugmaterial, jede Textur-Zuordnung, alle 628
Config-Schlüssel, alle 348 Spawn-Regeln. Dazu je Klasse ein Verhaltenskapitel
unter `docs/catalog/verhalten/` — **mit Zeilenangabe in die Quelle**. 351
Klassen, vollständig.

Die Regel dahinter: *Werte aus dem Jar schlagen Wiki-Angaben.* Das Wiki ist an
Dutzenden Stellen falsch, und wo Code und Config-Default sich widersprechen
(The King: Config 350, Klasse setzt 250), gilt der Code — er ist, was im Spiel
passierte.

### 2. Entscheidungen vor Code

[`docs/DECISIONS.md`](docs/DECISIONS.md) ist bindend: 26 nummerierte Regeln
(R0–R26), jede mit Begründung. Wo eine Portierungsnotiz etwas anderes sagt, gilt
diese Datei; geändert wird sie *dort*, nicht im Code.

Die lehrreichsten:

| | |
|---|---|
| **R4 — Leben über 1024** | `Attributes.MAX_HEALTH` ist in 1.21.1 auf 1024 geklemmt. The King hat 7000. Lösung: das Attribut bekommt `min(original, 1024)`, die Klasse kennt den echten Wert, eingehender Schaden wird mit `1024/original` skaliert. Bossleiste und Kampfdauer bleiben exakt wie im Original. |
| **R5 — Rüstungsformel** | 1.7.10 rechnete `schaden × (25 − Rüstung) / 25` **ohne Deckel**; ab 25 Punkten war blockbarer Schaden null. Genau davon leben die Bosse und die Endgame-Sets (42 und 48 Punkte). 1.21.1 deckelt bei 80 %. Die Originalformel wird über NeoForges `DamageContainer` nachgebaut — ohne Mixin. |
| **R8 — 109 Modelle 1:1** | `tools/gen_models.py` erzeugt die Geometrie auf Vanilla-`ModelPart` (3584 Teile). Die Animation ist von Hand aus `render()`/`setRotationAngles()` portiert, mit `resetPose()` am Anfang jedes `setupAnim` — das Original ließ Felder frameübergreifend stehen. Keine GeckoLib-Abhängigkeit. |
| **R18 — 1:1, auch die Fehler** | Grundregel. Ein Port, der still repariert, ist nicht mehr OreSpawn, und jede Reparatur ist eine Behauptung darüber, was der Autor gemeint hat. Abgewichen wird in **vier** benannten Fällen (Absturz in 1.21.1, globaler Zustand greift im Mehrspieler über, technisch nicht nachbaubar, Client/Server sähen Verschiedenes) — jedes Mal mit `// PORT:`-Kommentar. Rund 30 Originalfehler sind bewusst erhalten. |
| **R22 — Blocklisten altern** | Miner's Dream ließ im Spieltest Andesit, Diorit und Granit stehen. Die Blockliste des Originals war 1.7.10 vollständig — diese Steine gibt es erst ab 1.8. Wo das Original eine feste Liste für eine *Kategorie* benutzt, deckt der Port die ganze 1.21.1-Kategorie über Tags ab. Der wichtigste Befund des ganzen Projekts, und er kam aus einem Spieltest, nicht aus einer Prüfung. |
| **R24 — Reihenfolge** | In 1.7.10 lief OreSpawns Weltgenerierung als FML-`IWorldGenerator` **nach** dem Populate. Dungeons überschreiben also die Dekoration, nicht umgekehrt. Im Port läuft das Legacy-Populate vor `applyBiomeDecoration`, die Structure Sets stehen auf `top_layer_modification`. |

### 3. Wellen statt Dateien

Der Code entstand in 14 Wellen (W00–W13) über `tools/workflows/wave.js`. Eine
Welle ist: mehrere Portierer parallel, jeder schreibt **nur** seine Dateien →
ein Integrator baut → GameTests → ein Treue-Review gegen den Originalquelltext →
Nacharbeit.

Schnitt nach Abhängigkeit, nicht nach Bequemlichkeit: W01 Fundament, W02
Rohstoffe/Blöcke/Pflanzen, W03 Ausrüstung, W04 Projektile, W05 Dimensionen,
W06–W08 Mobs, W09–W11 Zähmen/Bosse/GUI, W12 Strukturinfrastruktur, W13
`GenericDungeon` allein (7111 Zeilen, auf drei Portierer nach Zeilenbereichen
aufgeteilt). Dazwischen zwei lesende Bug-Suchen (`docs/port/BUGHUNT.md`,
`BUGHUNT2.md`) mit je zwei Skeptikern pro Fund und zwei Fix-Wellen.

Die Übergabe jeder Welle liegt in `docs/port/W01.md` … `W13.md`, die Argumente in
`docs/port/waves/*.json`.

**Zwei Regeln, die das Verfahren erst brauchbar gemacht haben:**

1. *Der Bericht des Workflows ist eine Behauptung, der Lauf ist der Beleg.* Nach
   jeder Welle wurde selbst `./gradlew build runGameTests` gestartet und die
   JUnit-XML gezählt. W13 meldete 280/280 — nachgezählt waren es **279/280**, und
   der eine Fehlschlag war die Reihenfolgefrage, aus der R24 wurde.
2. *Eine Welle ohne Portierer-Ergebnis stoppt vor der Integration.* Beim ersten
   W02-Anlauf lief der Integrator auf null Ergebnissen los.

### Aufwand

367 Agenten, 8836 Anfragen, 2,36 Mrd. Tokens (davon 2,22 Mrd. Cache-Lesen) —
zu API-Listenpreisen rund 2230 USD.

---

## Aufbau

```
src/main/java/com/swbr/orespawn/
  block/ entity/ item/ combat/ world/      Spielinhalt
  client/                                  alles aus net.minecraft.client
  client/model/geom/                       109 *Geometry.java, generiert
  config/                                  628 Schlüssel, COMMON + EarlyConfig
  registry/ network/ menu/ loot/ dispenser/
  gametest/                                20 Klassen, 385 @GameTest

docs/research/     6 Berichte: Mobs, Dimensionen, Items, Lizenz, Jar, Modelle
docs/catalog/      aus dem Jar erzeugt: manifest.json + Tabellen + Verhalten je Klasse
docs/DECISIONS.md  R0–R26, bindend
docs/STYLE.md      Bauregeln
docs/port/         Übergaben W01–W13, Bug-Suchen, Fix-Berichte
tools/             Katalog- und Generator-Skripte, Wellen-Workflow
RESUME.md          Arbeitsjournal, chronologisch
```

### Werkzeuge

| Skript | Erzeugt |
|---|---|
| `catalog.py` | `manifest.json`, `classes.json`, Katalogtabellen |
| `assets.py` | Texturen, Sounds, `sounds.json`, `lang/en_us.json` (braucht das Jar) |
| `gen_models.py` | 109 `*Geometry.java`, 3584 Teile |
| `gen_config.py` | `OreSpawnConfig.java`, 628 Schlüssel |
| `gen_sounds.py` | `ModSounds.java`, 126 Events |
| `gen_recipes.py` | 381 Rezepte als JSON |
| `gen_spawns.py` | 348 Spawn-Regeln, Biom-Zuordnung 1.7.10 → 1.21.1 |
| `workflows/wave.js` | der Wellen-Workflow |

Generierte Dateien werden **nie von Hand geändert** — das Skript ist die Quelle.

---

## Plattform

Minecraft 1.21.1 · NeoForge 21.1.248 · Java 21 · ModDevGradle 2.0.144 ·
Parchment 2024.11.17. **Keine Mixins, keine Bibliotheksabhängigkeit.**
Dedizierter Server läuft; alles aus `net.minecraft.client` liegt unter
`com.swbr.orespawn.client`.
