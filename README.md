# OreSpawn → NeoForge 1.21.1

A port of **OreSpawn** (TheyCallMeDanger, Minecraft 1.7.10, build 20.3) to
**NeoForge 21.1.248 / Minecraft 1.21.1**. 134 entities, 473 items, 211 blocks,
109 models, six dimensions, 628 config keys.

Source, tooling and porting documentation only. The original textures and sounds
are **not included** — you supply them from your own copy of the original jar in
step 3.

---

## Legal — read this first

OreSpawn is **not cleared** for redistribution or for porting. Sources with
citations: [`docs/research/04-history-license-ports.md`](docs/research/04-history-license-ports.md).

| | |
|---|---|
| License since 2020-10-01 | "No permission is given to redistribute OreSpawn by itself, **in whole or in part**." |
| The author, posted 2021-11, unchanged since | "OreSpawn on MC is no longer available. … And no, you CANNOT have any rights to it whatsoever. **I DO NOT WANT IT PORTED.**" |
| Third-party rights | Per the author, roughly a quarter of the material — mostly creature models — belongs to 19 credited modelers who licensed it **to him alone**. |
| Enforcement | Two ports were withdrawn over this (InsanityCraft, jtrent238/OreSpawnDzPort). |

Therefore:

- **No assets here.** Not one image, sound or model of the original is
  redistributed. Building requires **your own** copy of the original jar.
- **No license grant.** The source is *All Rights Reserved* and is a derivative
  of someone else's protected work. Readable, not free to use. There is
  deliberately no `LICENSE` file — nothing here was mine to license.
- **No releases.** No built jars are published here, and none will be.
- **Not a substitute for a clean successor.** For the OreSpawn feel with
  original assets: [Chaos Awakens](https://chaosawakens.github.io/) and
  **Antarchy** (NeoForge 1.21.1).

If a rights holder — Richard H. Clark or any of the modelers — wants this
repository gone, open an issue. It will be deleted, not debated.

---

## Build it yourself

### 1. Prerequisites

| | |
|---|---|
| **JDK 21** | `java -version` must report 21. [Temurin 21](https://adoptium.net/temurin/releases/?version=21) works. With several JDKs installed, do **not** set `JAVA_HOME` — the Gradle toolchain resolves 21 itself. |
| **Python 3.9+** | For the asset script. |
| **Git** | To clone. |
| **Your own `orespawn-1.7.10-20.3.jar`** | See step 3. |

Gradle is not needed; `./gradlew` fetches 8.14.5 on first use.

### 2. Clone

```bash
git clone https://github.com/sxwxbxr/orespawn-neoforge-port.git
cd orespawn-neoforge-port
```

### 3. Supply the original jar

You need **OreSpawn 1.7.10 v20.3**. It is not distributed here and there is no
download link in this repository; use the copy you already have.

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

A different hash is not fatal — v20.2 and the 1.6.4 builds share most files —
but the script will then report every texture it cannot find, and those end up
missing in game.

Unpack into `reference/jar/extracted/` (git-ignored, nothing from it is ever
committed):

```bash
mkdir -p reference/jar/extracted
cd reference/jar/extracted
unzip ../../../orespawn-1.7.10-20.3.jar     # adjust to where your jar is
cd ../../..
```

<details>
<summary>Windows PowerShell</summary>

```powershell
New-Item -ItemType Directory -Force reference\jar\extracted | Out-Null
Expand-Archive -Path C:\path\to\orespawn-1.7.10-20.3.jar -DestinationPath reference\jar\extracted -Force
```

Some Windows versions refuse files without a `.zip` extension; copy the jar to
`orespawn.zip` first if it complains.
</details>

`reference/jar/extracted/assets/orespawn/` must now exist and contain
`sounds.json` plus a pile of `.png`.

### 4. Generate the assets

```bash
python tools/assets.py
```

```
textures copied: 1058, armor layers: 28, ogg copied: 307, sound events: 126, lang keys: 825 (10 without an original name, title-cased from the id)
problems: 3
  - ogg not referenced by sounds.json, not copied: frog1.ogg
  - ogg not referenced by sounds.json, not copied: frog2.ogg
  - ogg not referenced by sounds.json, not copied: o_rain.ogg
```

Those three are correct — the original never referenced them either. The script
exits non-zero only on a missing or ambiguous texture, and with code 2 if step 3
was skipped.

It writes the four things this repository does not ship, all git-ignored:

```
src/main/resources/assets/orespawn/textures/        1086 png, byte for byte from the jar
src/main/resources/assets/orespawn/sounds/           307 ogg, likewise
src/main/resources/assets/orespawn/sounds.json       126 sound events
src/main/resources/assets/orespawn/lang/en_us.json   825 display names
```

Only the paths change: 1.21.1 requires lowercase resource locations. Re-running
is safe — the owned directories are cleared first, so a renamed texture leaves
no stale copy.

### 5. Build

```bash
./gradlew build      # gradlew.bat build on Windows
```

Jar: `build/libs/orespawn-0.2.0.jar`. The first run downloads NeoForge and
decompiles Minecraft — several minutes and ~3 GB in `~/.gradle`. Later runs take
seconds.

### 6. Verify (optional)

```bash
./gradlew test           # 17 unit tests
./gradlew runGameTests   # 385 GameTests on a headless dedicated server
```

### 7. Install

Copy `build/libs/orespawn-0.2.0.jar` into the `mods/` folder of a **Minecraft
1.21.1 / NeoForge 21.1.248+** instance. No library dependencies — no GeckoLib,
no mixins. First launch writes `config/orespawn-common.toml` with all 628 keys.

**Caveat:** this port was built on a machine without a Minecraft client. The
dedicated server and 363 GameTests pass, but nothing here has ever been
rendered. Expect client-side bugs.

### From the source tree

```bash
./gradlew runClient   # dev client, data in run/client
./gradlew runServer   # dedicated server, run/server
./gradlew runData     # regenerate datagen output
```

### Troubleshooting

| Symptom | Cause |
|---|---|
| `The extracted original jar is missing.` | Step 3 skipped or unpacked to the wrong place. `reference/jar/extracted/assets/orespawn/sounds.json` must exist. |
| Purple-black checkerboards in game | Built without step 4. Run `python tools/assets.py`, then build again. |
| `texture missing in jar extract: …` | Wrong jar version — check the SHA-1 in step 3. |
| `Unsupported class file major version` | Not JDK 21. Check `java -version`; unset `JAVA_HOME` if it points elsewhere. |
| Items show as `item.orespawn.…` | `lang/en_us.json` missing — step 4 again. |

---

## What it cost

Written by Claude agents in 14 dependency-ordered waves (W00–W13), each one
several porters in parallel → one integrator → GameTests → a fidelity review
against the original source → rework.

| | |
|---|---|
| Agents | 367 |
| Requests | 8836 |
| Tokens | 2.36 B, of which 2.22 B cache reads |
| At API list prices | ≈ 2230 USD |

---

Porting decisions are in [`docs/DECISIONS.md`](docs/DECISIONS.md) (binding,
R0–R26); the generated catalogue, research and per-wave handovers are under
`docs/`. Those are in German; code and tool output are English. Generated files
are never edited by hand — the script in `tools/` is the source.
