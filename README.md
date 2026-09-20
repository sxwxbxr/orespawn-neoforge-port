# OreSpawn → NeoForge 1.21.1

A line-faithful port of **OreSpawn** (TheyCallMeDanger, Minecraft 1.7.10, build
20.3) to **NeoForge 21.1.248 / Minecraft 1.21.1**. 134 entities, 473 items, 211
blocks, 109 models, six custom dimensions, 628 config keys — reconstructed from
the original jar rather than reinvented.

This repository holds the **source, the tooling and the full porting
documentation**. The original textures and sounds are **deliberately not
included**; you supply them from your own copy of the original jar in step 3
below. See [Legal](#legal) for why.

---

## Legal — read this first

OreSpawn is **not cleared** for redistribution or for porting. Sources with
citations are in
[`docs/research/04-history-license-ports.md`](docs/research/04-history-license-ports.md):

| | |
|---|---|
| License since 2020-10-01 | "No permission is given to redistribute OreSpawn by itself, **in whole or in part**." |
| The author, posted 2021-11 and unchanged since | "OreSpawn on MC is no longer available. … And no, you CANNOT have any rights to it whatsoever. **I DO NOT WANT IT PORTED.**" |
| Third-party rights | Per the author, roughly a quarter of the material — mostly creature models — belongs to 19 credited modelers who licensed it **to him alone**. |
| Enforcement | Two ports were withdrawn over this (InsanityCraft, jtrent238/OreSpawnDzPort). |

What follows for this repository:

- **No assets here.** Not one image, sound or model of the original is
  redistributed. Building requires **your own** copy of the original jar.
- **No license grant.** The source is *All Rights Reserved* and is a derivative
  of someone else's protected work. It is readable here, not free to use. There
  is deliberately no `LICENSE` file — there is nothing to license that was mine
  to give.
- **No releases.** No built jars are published here, and none will be.
- **Not a substitute for a clean successor.** If you want the OreSpawn feel
  *with* original assets: [Chaos Awakens](https://chaosawakens.github.io/) and
  **Antarchy** (NeoForge 1.21.1) are built for exactly that.

If a rights holder — Richard H. Clark or any of the modelers — wants this
repository gone, open an issue. It will be deleted, not debated.

---

## Build it yourself

Roughly 20 minutes, most of it Gradle downloading NeoForge on the first run.

### 1. Prerequisites

| | |
|---|---|
| **JDK 21** | Required by Minecraft 1.21.1. `java -version` must report 21. [Temurin 21](https://adoptium.net/temurin/releases/?version=21) works. Do **not** set `JAVA_HOME` if you have several JDKs — the Gradle toolchain resolves 21 on its own. |
| **Python 3.9+** | For the asset script. `python --version`. |
| **Git** | To clone. |
| **Your own `orespawn-1.7.10-20.3.jar`** | See step 3. |

Gradle itself is not needed — the repository ships a wrapper (`./gradlew`), which
fetches Gradle 8.14.5 on first use.

### 2. Clone

```bash
git clone https://github.com/sxwxbxr/orespawn-neoforge-port.git
cd orespawn-neoforge-port
```

### 3. Supply the original jar

You need **OreSpawn 1.7.10 v20.3** — the last 1.7.10 build. It is not
distributed here and there is no download link in this repository; use the copy
you already have.

Verify you have the right file:

```bash
sha1sum orespawn-1.7.10-20.3.jar
# d43dbe9a400dc8df06418da3e04d36422b2176d7
```

<details>
<summary>Windows PowerShell</summary>

```powershell
(Get-FileHash orespawn-1.7.10-20.3.jar -Algorithm SHA1).Hash.ToLower()
```
</details>

A different hash is not fatal — v20.2 and the 1.6.4 builds have most of the same
files — but the script will then report every texture it could not find, and
those entries end up as missing textures in game.

Unpack it into `reference/jar/extracted/`. That directory is git-ignored; nothing
from it is ever committed.

```bash
mkdir -p reference/jar/extracted
cd reference/jar/extracted
unzip ../../../orespawn-1.7.10-20.3.jar     # adjust the path to your jar
cd ../../..
```

<details>
<summary>Windows PowerShell</summary>

```powershell
New-Item -ItemType Directory -Force reference\jar\extracted | Out-Null
Expand-Archive -Path C:\path\to\orespawn-1.7.10-20.3.jar -DestinationPath reference\jar\extracted -Force
```

`Expand-Archive` refuses files without a `.zip` extension in some Windows
versions; copy the jar to `orespawn.zip` first if it complains.
</details>

Afterwards `reference/jar/extracted/assets/orespawn/` must exist and contain
`sounds.json` plus a pile of `.png` files. That is what the next step reads.

### 4. Generate the assets

```bash
python tools/assets.py
```

Expected output:

```
textures copied: 1058, armor layers: 28, ogg copied: 307, sound events: 126, lang keys: 825 (10 without an original name, title-cased from the id)
problems: 3
  - ogg not referenced by sounds.json, not copied: frog1.ogg
  - ogg not referenced by sounds.json, not copied: frog2.ogg
  - ogg not referenced by sounds.json, not copied: o_rain.ogg
```

Those three are correct: the original never referenced them either, so they stay
out. The script exits non-zero only on a **missing or ambiguous** texture.

It writes the four things this repository does not ship, all of them
git-ignored:

```
src/main/resources/assets/orespawn/textures/     1086 png, from the jar byte for byte
src/main/resources/assets/orespawn/sounds/        307 ogg, likewise
src/main/resources/assets/orespawn/sounds.json    126 sound events
src/main/resources/assets/orespawn/lang/en_us.json  825 display names
```

Only the paths change: 1.21.1 requires lowercase resource locations, 1.7.10 did
not care. Re-running is safe — the two owned directories are cleared first, so a
renamed texture never leaves a stale copy behind.

If you skipped step 3, the script says so and stops with exit code 2.

### 5. Build

```bash
./gradlew build
```

The jar lands in `build/libs/orespawn-0.2.0.jar`. On Windows use `gradlew.bat build`.

The first run downloads NeoForge 21.1.248 and decompiles Minecraft; expect
several minutes and ~3 GB in `~/.gradle`. Later runs take seconds.

### 6. Verify (optional, recommended)

```bash
./gradlew test           # 17 unit tests
./gradlew runGameTests   # 385 GameTests on a real dedicated server
```

`runGameTests` boots a headless dedicated server, spawns every entity, ticks it,
places every block and generates every dimension. It takes a few minutes and is
the only behavioural proof available without a client.

### 7. Install

Copy `build/libs/orespawn-0.2.0.jar` into the `mods/` folder of a **Minecraft
1.21.1 / NeoForge 21.1.248 or newer** instance. There are no library
dependencies — no GeckoLib, no mixins.

On first launch the mod writes `config/orespawn-common.toml` with all 628 keys,
section names as in the original (`OreSpawnTWEAKS`, `OreSpawnWEAPONS`,
`OreSpawnMOBS`, `OreSpawnARMOR`, `OreSpawnORES`).

### Running it from the source tree

```bash
./gradlew runClient   # dev client, world data in run/client
./gradlew runServer   # dedicated server, run/server
./gradlew runData     # regenerate datagen output
```

### Troubleshooting

| Symptom | Cause |
|---|---|
| `The extracted original jar is missing.` | Step 3 was skipped or unpacked to the wrong place. `reference/jar/extracted/assets/orespawn/sounds.json` must exist. |
| Purple-black checkerboards everywhere in game | The build ran without step 4. Run `python tools/assets.py`, then `./gradlew build` again. |
| `texture missing in jar extract: …` | Wrong jar version. Check the SHA-1 in step 3. |
| `Unsupported class file major version` / toolchain errors | Not JDK 21. Check `java -version`; unset `JAVA_HOME` if it points at another JDK. |
| Items and blocks show as `item.orespawn.…` | `lang/en_us.json` is missing — step 4 again. |

---

## What "done" means here

The machine this port was built on has **no Minecraft client**. What is
provable is exactly this:

- `./gradlew build` green,
- **363/363 GameTests** green (as of 0.2.0): every entity spawns and ticks 100
  ticks without an exception, every block places, every dimension produces FULL
  chunks, every portal trip works in both directions,
- `runServer` reaching `Done` with no `Failed to create mod instance` and zero
  ERROR lines.

**Nothing here has been rendered or played.** A green server start says nothing
about the client — a dedicated server never loads `net.minecraft.client`, so
every client-side mixin and renderer is invisible to it. That belongs in any
status report, so it is stated here too.

---

## How the port was made

Not by hand. Three stages, each built on evidence rather than recall — porting
1.7.10 code from memory produces mods that load and then behave wrong.

### 1. A catalogue instead of assumptions

`tools/catalog.py` reads the original jar and the decompiled source and produces
`docs/catalog/manifest.json` (1.1 MB): every registration, every mob stat, every
tool material, every texture mapping, all 628 config keys, all 348 spawn rules.
Alongside it, a behaviour chapter per class in `docs/catalog/verhalten/` — **with
line references into the source**. 351 classes, complete.

The rule behind it: *values from the jar beat the wiki*. The wiki is wrong in
dozens of places, and where code and config default disagree (The King: config
350, the class sets 250), the code wins — it is what actually happened in game.

### 2. Decisions before code

[`docs/DECISIONS.md`](docs/DECISIONS.md) is binding: 26 numbered rules (R0–R26),
each with its reasoning. Where a porting note says otherwise, this file wins; it
is changed *there*, not worked around in code.

The instructive ones:

| | |
|---|---|
| **R4 — health above 1024** | `Attributes.MAX_HEALTH` is clamped to 1024 in 1.21.1. The King has 7000. The attribute gets `min(original, 1024)`, the class knows the real value, and incoming damage is scaled by `1024/original`. Boss bar and fight length stay exactly as in the original. |
| **R5 — the armour formula** | 1.7.10 computed `damage × (25 − armour) / 25` **without a cap**; at 25 points, blockable damage was zero. The bosses and the endgame sets (42 and 48 points) live off precisely that. 1.21.1 caps at 80 %. The original formula is rebuilt through NeoForge's `DamageContainer` — no mixin. |
| **R8 — 109 models, 1:1** | `tools/gen_models.py` emits the geometry onto vanilla `ModelPart` (3584 parts). The animation is hand-ported from `render()`/`setRotationAngles()`, with `resetPose()` at the top of every `setupAnim` — the original left fields standing across frames. No GeckoLib dependency. |
| **R18 — 1:1, bugs included** | The ground rule. A port that quietly fixes things is no longer OreSpawn, and every fix is a claim about what the author meant. Deviation happens in **four** named cases only (would crash in 1.21.1, global state leaks across players in multiplayer, not expressible in 1.21.1, client and server would disagree) — each marked with a `// PORT:` comment. Around 30 original bugs are preserved on purpose. |
| **R22 — block lists age** | Miner's Dream left andesite, diorite and granite standing in a play test. The original's block list was complete in 1.7.10 — those stones only exist from 1.8. Where the original uses a fixed list for a *category*, the port covers the whole 1.21.1 category via tags. The most valuable finding of the project, and it came from playing, not from a checker. |
| **R24 — ordering** | In 1.7.10 OreSpawn's world generation ran as an FML `IWorldGenerator` **after** populate. Dungeons therefore overwrite decoration, not the other way round. The port runs the legacy populate before `applyBiomeDecoration` and puts the structure sets on `top_layer_modification`. |

### 3. Waves instead of files

The code was written in 14 waves (W00–W13) driven by `tools/workflows/wave.js`.
A wave is: several porters in parallel, each writing **only** its own files → one
integrator builds → GameTests → a fidelity review against the original source →
rework.

Cut by dependency, not convenience: W01 foundation, W02 ores/blocks/plants, W03
equipment, W04 projectiles, W05 dimensions, W06–W08 mobs, W09–W11
taming/bosses/GUI, W12 structure infrastructure, W13 `GenericDungeon` alone
(7111 lines, split across three porters by line range). Between them two
read-only bug hunts (`docs/port/BUGHUNT.md`, `BUGHUNT2.md`) with two skeptics per
finding, and two fix waves.

Each wave's handover is in `docs/port/W01.md` … `W13.md`, its arguments in
`docs/port/waves/*.json`.

**Two rules that made the method usable:**

1. *The workflow's report is a claim; the run is the evidence.* After every wave,
   `./gradlew build runGameTests` was run independently and the JUnit XML counted.
   W13 reported 280/280 — the recount said **279/280**, and that one failure was
   the ordering question that became R24.
2. *A wave with a missing porter result stops before integration.* On the first
   W02 attempt the integrator started on zero results.

### Cost

367 agents, 8836 requests, 2.36 B tokens (2.22 B of them cache reads) — roughly
2230 USD at API list prices.

---

## Layout

```
src/main/java/com/swbr/orespawn/
  block/ entity/ item/ combat/ world/      game content
  client/                                  everything touching net.minecraft.client
  client/model/geom/                       109 *Geometry.java, generated
  config/                                  628 keys, COMMON + EarlyConfig
  registry/ network/ menu/ loot/ dispenser/
  gametest/                                20 classes, 385 @GameTest

docs/research/     6 reports: mobs, dimensions, items, license, jar, models
docs/catalog/      generated from the jar: manifest.json, tables, per-class behaviour
docs/DECISIONS.md  R0–R26, binding
docs/STYLE.md      coding rules
docs/port/         handovers W01–W13, bug hunts, fix reports
tools/             catalogue and generator scripts, the wave workflow
RESUME.md          working journal, chronological
```

Documentation is in German; code, identifiers and tool output are in English.

### Tools

| Script | Produces |
|---|---|
| `catalog.py` | `manifest.json`, `classes.json`, the catalogue tables |
| `assets.py` | textures, sounds, `sounds.json`, `lang/en_us.json` (needs the jar) |
| `gen_models.py` | 109 `*Geometry.java`, 3584 parts |
| `gen_config.py` | `OreSpawnConfig.java`, 628 keys |
| `gen_sounds.py` | `ModSounds.java`, 126 events |
| `gen_recipes.py` | 381 recipes as JSON |
| `gen_spawns.py` | 348 spawn rules, biome mapping 1.7.10 → 1.21.1 |
| `workflows/wave.js` | the wave workflow |

Generated files are **never edited by hand** — the script is the source.

---

## Platform

Minecraft 1.21.1 · NeoForge 21.1.248 · Java 21 · ModDevGradle 2.0.144 ·
Parchment 2024.11.17. **No mixins, no library dependencies.** The dedicated
server runs; everything from `net.minecraft.client` lives under
`com.swbr.orespawn.client`.
