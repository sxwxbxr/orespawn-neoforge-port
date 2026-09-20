# Build rules for this port - every code agent must follow these

`docs/DECISIONS.md` is binding and wins over everything here and in the catalogue.

## Platform (hard)

- Minecraft **1.21.1**, NeoForge **21.1.248**, Java **21**, ModDevGradle 2.0.144, Parchment 1.21.1 / 2024.11.17.
- **No mixins. No library dependencies.**
- **Dedicated server must work.** Anything touching `net.minecraft.client.*` lives under
  `com.swbr.orespawn.client` and is only reached from `@EventBusSubscriber(value = Dist.CLIENT)`
  or behind `FMLEnvironment.dist`.

## Sources of truth

| Need | Where |
|---|---|
| ids, names, stats, textures, sounds, config defaults | `docs/catalog/manifest.json` (1.1 MB - extract with python, never Read whole) |
| what a class does | `docs/catalog/verhalten/*.md`, then the original source |
| original source | `reference/src-20.2/src/main/java/danger/orespawn/<Class>.java` |
| SRG names (`func_70097_a`, `field_70170_p`) | `reference/jar/mcp/methods.csv`, `fields.csv` - grep, never guess |
| look, size, animation | `docs/catalog/design/*.md`, `reference/jar/models/`, `reference/jar/anim/` |
| recipes | `docs/catalog/recipes.json` |
| 1.21.1 Minecraft + NeoForge API | `F:/Repositories/Modpacks/mods/architrave/.cache/sources/` (unzipped, Parchment names) |

## Never guess an API

Grep the unzipped 1.21.1 sources before writing any call you are not certain of. Traps already paid for
in this repo:

- `DeferredRegister.Entities` does **not** exist - use `DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID)`.
- `Entity.addAdditionalSaveData` takes **only** a `CompoundTag`.
- `AttributeModifier` is `(ResourceLocation, double, Operation)` - no UUID, no name.
- `LivingHurtEvent` is gone: `LivingIncomingDamageEvent`, `LivingDamageEvent.Pre` / `.Post`.
- `Attributes.MAX_HEALTH` is clamped to 1024 and `ARMOR` to 30 (DECISIONS R4, R5).

## Porting from 1.7.10

- Port **behaviour line by line**, keep the original numbers and the original order of checks.
  Where 1.21.1 has no equivalent, write the closest equivalent and a `// PORT:` comment saying what differs.
- Keep the original class name as the Java class name (`TheKing`, `Godzilla`, `ItemRayGun`) so every
  file maps to exactly one original. Package by domain (below), not by original package.
- Rename SRG identifiers to their MCP names via the csv files while reading; do not copy `func_`/`field_`
  names into new code.
- `DataWatcher` index N -> one `EntityDataAccessor` constant named after its meaning.
- 1.7.10 `worldObj.isRemote` -> `level().isClientSide`; `rand` -> `getRandom()`; ticks and distances unchanged.

## Code layout

`com.swbr.orespawn`:

| package | contents |
|---|---|
| `registry` | one holder class per registry (`ModItems`, `ModBlocks`, `ModEntities`, `ModSounds`, ...), each with `static void init() {}` called from the mod constructor |
| `config` | `ModConfigSpec` classes (COMMON, SERVER, CLIENT) |
| `entity`, `entity.ai`, `entity.projectile`, `entity.boss` | entities and goals |
| `item`, `block`, `block.entity`, `menu` | items, blocks, block entities, menus |
| `world`, `world.feature`, `world.structure`, `world.dimension` | worldgen and dimensions |
| `network` | payloads |
| `combat` | virtual health, legacy armor formula |
| `client`, `client.model`, `client.model.geom` (generated), `client.renderer`, `client.screen` | client only |
| `data` | datagen providers |
| `gametest` | GameTests |

## Generated code and assets - never edit by hand

- `client/model/geom/*Geometry.java` - `tools/gen_models.py`
- `src/main/resources/assets/orespawn/{textures,sounds}/`, `sounds.json`, `lang/en_us.json` - `tools/assets.py`
- `docs/catalog/manifest.json`, the numbered catalogue tables - `tools/catalog.py`

Fix the generator, then re-run it.

## Working in parallel

Workers in a wave write **only the files assigned to them**. Workers do not run Gradle - two Gradle builds
in one project directory block each other and see each other's half-written files. The wave's integrator
runs the build and fixes compile errors. Shared files (`registry/*`, the mod class, config) are owned by
exactly one worker per wave, named in the wave plan.

## Code style

Mirror `F:/Repositories/Modpacks/mods/armature/src/main/java/com/swbr/armature/`: `final` holder classes,
`public static final DeferredHolder`/`DeferredItem` fields, Javadoc that says why, English code and
comments. UTF-8 sources.

## Server compatibility checklist

- [ ] No `net.minecraft.client` import outside `client/`.
- [ ] No `Minecraft.getInstance()` outside `client/`.
- [ ] Payloads registered with an explicit direction; handlers use `context.enqueueWork`.
- [ ] Saved state on the server (`SavedData`, attachments), never derived from client state.
- [ ] Gameplay config is `SERVER` or `COMMON`, never `CLIENT`.
- [ ] Nothing per tick iterates all loaded chunks or all entities of the level.
