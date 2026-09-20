export const meta = {
  name: 'orespawn-catalogue',
  description: 'Catalogue every OreSpawn class: behaviour from the decompiled source, visual design from models and textures',
  phases: [
    { title: 'Verhalten', detail: 'one reader per line-balanced batch of decompiled classes' },
    { title: 'Design', detail: 'look, size, animation and textures per entity, item and block', model: 'fable' },
    { title: 'Lücken', detail: 'rerun anything a batch did not cover' },
    { title: 'Übersicht', detail: 'catalogue README with complexity map and open questions' },
  ],
}

const MOD = 'F:/Repositories/Modpacks/mods/orespawn'
const CTX = `
PROJECT: a private 1:1 port of the Minecraft mod OreSpawn (TheyCallMeDanger, 1.7.10 build 20.3) to NeoForge 1.21.1 (Java 21).
Mod directory: ${MOD}  (modid "orespawn", package com.swbr.orespawn)

GROUND TRUTH, in this priority order:
1. ${MOD}/docs/catalog/manifest.json - generated from the original jar: every entity/item/block with registry id, lang name, class,
   attributes, hitbox, spawn rules, renderer, model, textures (legacy path -> new 1.21.1 path), materials, sounds, config keys.
   It is 1.1 MB: do NOT Read it whole. Extract your entries with python, e.g.
   python -c "import json;m=json.load(open(r'${MOD}/docs/catalog/manifest.json',encoding='utf-8'));print(json.dumps([e for e in m['entities'] if e['class'] in ('TheKing',)],indent=1))"
2. Decompiled source of the same build: ${MOD}/reference/src-20.2/src/main/java/danger/orespawn/<Class>.java (verified identical to the 20.3 jar).
   Names are partly SRG-obfuscated (func_70097_a, field_70170_p). MCP 1.7.10 mappings are at ${MOD}/reference/jar/mcp/ if present - grep them to translate; otherwise say "SRG name, meaning inferred from usage" instead of guessing.
3. Extracted jar data: ${MOD}/reference/jar/ (models/*.json geometry, anim/*.json animation summary, config_dump.txt, sounds_dump.txt, renderers_dump.txt, calls_OreSpawnMain.txt, extracted/ assets).
4. Web research, lower trust: ${MOD}/docs/research/01-mobs.md, 02-dimensions-worldgen.md, 03-items.md, 05-jar-inventory.md, 06-models-design.md.
   Where the source code and the research disagree, the source code wins; mention the disagreement in one line.

Class -> domain map and line counts: ${MOD}/docs/catalog/classes.json

PRELIMINARY PORT GUARDRAILS (write port notes consistent with these, flag it if one is impossible for your class):
- No mixins. Must run on a dedicated server; client-only code lives under com.swbr.orespawn.client.
- Entity models: vanilla ModelPart/LayerDefinition generated from reference/jar/models/*.json; animation ported by hand from render()/setRotationAngles(). GeckoLib is not planned.
- In 1.21.1 Attributes.MAX_HEALTH is clamped to 1024 and ARMOR to 30. Entities above that need damage scaling ("virtual health") - mention the real original number.
- Enchantments are data-driven registries in 1.21.1; DataWatcher -> SynchedEntityData; NBT via addAdditionalSaveData(CompoundTag).
- Dimensions become datapack dimension types plus Java ChunkGenerators; big hardcoded structures become Structure pieces that write within their bounding box.
- Config becomes ModConfigSpec; the original keys and defaults are in manifest.json "config".
- Sounds and textures are reused 1:1, only renamed to lowercase paths (see manifest texture_map).

WRITING RULES:
- User-facing catalogue text is GERMAN prose. Keep identifiers, class names, registry ids, method names and item names in English exactly as in the code.
- Never invent a number. Every number carries its origin as (Class.java:LINE) or (manifest). If something is not resolvable, write "offen: <why>".
- Be concrete and dense: tables and short bullet lines, no filler.
`

const BEHAVIOUR_SCHEMA = {
  type: 'object',
  properties: {
    key: { type: 'string' },
    file: { type: 'string', description: 'absolute path of the markdown file you wrote' },
    classes: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          class: { type: 'string' },
          ids: { type: 'array', items: { type: 'string' }, description: 'manifest registry ids this class backs (entities/items/blocks), empty if none' },
          summary: { type: 'string', description: 'one German sentence: what it is and does' },
          depends_on: { type: 'array', items: { type: 'string' }, description: 'other OreSpawn classes it needs at runtime' },
          port_complexity: { type: 'string', enum: ['S', 'M', 'L', 'XL'] },
          port_risks: { type: 'array', items: { type: 'string' } },
        },
        required: ['class', 'ids', 'summary', 'depends_on', 'port_complexity', 'port_risks'],
      },
    },
    unresolved: { type: 'array', items: { type: 'string' } },
  },
  required: ['key', 'file', 'classes', 'unresolved'],
}

const DESIGN_SCHEMA = {
  type: 'object',
  properties: {
    key: { type: 'string' },
    file: { type: 'string' },
    entries: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', description: 'manifest registry id' },
          look: { type: 'string', description: 'one German sentence describing the look' },
          size_blocks: { type: 'string', description: 'approximate rendered size in blocks with its derivation, or "offen"' },
          textures: { type: 'array', items: { type: 'string' }, description: 'new 1.21.1 texture paths used' },
          port_notes: { type: 'string' },
        },
        required: ['id', 'look', 'size_blocks', 'textures', 'port_notes'],
      },
    },
    unresolved: { type: 'array', items: { type: 'string' } },
  },
  required: ['key', 'file', 'entries', 'unresolved'],
}

function behaviourPrompt(b) {
  const focus = {
    entity: `For EACH class write a section "### <Class> - <lang name> (\`<id>\`)" with these sub-bullets:
- Rolle: what it is, vanilla base class, hostile/passive/tameable/projectile/boss part.
- Werte: only what the manifest does NOT already have or has as "?" - XP, regeneration, damage caps, attack cooldowns (ticks), ranges, projectile damage, tamed vs wild values, growth stages, scale formulas. Each with (Class.java:LINE).
- KI und Angriffe: every AI task with priority; every attack/ability with damage, range, cooldown, conditions; targeting rules; flight/swim/climb logic; boss phases.
- Interaktion: right-click behaviour, taming item and chance, feeding, riding and rider controls, breeding, dimension teleport if it is an ant/termite/butterfly.
- Drops: item, count, chance, conditions; XP.
- Spawnen: getCanSpawnHere conditions, light/height/block rules, despawn rules (canDespawn), spawn structures it is tied to.
- Zustand: DataWatcher indices and meaning, NBT keys.
- Sounds: sound event names it plays and when.
- Config: OreSpawnMain fields/config keys it reads.
- Portierung 1.21.1: concrete API mapping and the pitfalls specific to this class.`,
    itemblock: `For EACH class write a section "### <Class>" listing every registry id and lang name it backs (one class can back many ids - read the manifest ctor_args to see how instances differ), then:
- Rolle and vanilla base class.
- Werte: durability, damage, speed, food/saturation, effects with amplifier and duration, hardness/resistance/light, harvest level, growth stages, tick rates - each with origin.
- Verhalten: onItemRightClick / onItemUse / hitEntity / onUpdate / onBlockActivated / updateTick / dropped items / explosion logic etc., step by step with numbers.
- Rezeptbezug: if the class itself registers or checks recipes.
- GUI/TileEntity: slots, fuel rules, smelting logic, synced fields.
- Sounds, Config keys.
- Portierung 1.21.1: DataComponents, BlockState properties, BlockEntity, Menu/Screen, tool tiers (Tier/ArmorMaterial registry), pitfalls.`,
    world: `For EACH class write a section "### <Class>", then:
- Rolle: where it is called from and when (chunk populate, dimension, item use).
- For world generators: every feature/structure it places with footprint size (x*y*z), block palette, spawners and their mobs, chest loot (items, counts, chances), placement conditions (dimension, biome, height, rarity 1-in-N, surface checks), and the method name that builds it. Tables please.
- For chunk/world providers: terrain algorithm (noise, heights, fill blocks, sea level, bedrock, sky/fog colours, day cycle, respawn rules, biome manager).
- For teleporters: target position rules, portal/platform creation.
- Portierung 1.21.1: datapack JSON vs Java ChunkGenerator vs Structure/StructurePiece vs Feature, and the chunk-boundary risk for each structure (anything wider than 3x3 chunks must be a Structure).`,
    core: b.key === 'core-01a'
      ? `This batch is only OreSpawnMain, focus A: mod lifecycle, config reading (map every Configuration.get to its field), item/block/entity registration order, natural spawn registration (addSpawn with biomes and weights - as a table), dimension registration, event handlers (@SubscribeEvent / FMLCommonHandler), tick handlers, player events, chat/login messages, and anything else that is NOT a recipe, smelting or chest loot. Write it to the file as sections by concern.`
      : b.key === 'core-01b'
        ? `This batch is only OreSpawnMain, focus B: EVERY recipe. Write ${MOD}/docs/catalog/recipes.json as a JSON array of {type: "shaped"|"shapeless"|"smelting", result: {item, count, meta}, pattern: [...], key: {char: item}, ingredients: [...], xp, source_line} using the legacy item field names (e.g. MyUltimateSword) AND the manifest registry id for each item (look it up by field). Vanilla items as "minecraft:<1.21.1 name>" (translate 1.7.10 names like Items.field_151045_i via the MCP mappings; say "offen" if not translatable). OreDictionary strings stay as "#ore:<name>". Then also chest loot (ChestGenHooks: chest type, item, min, max, weight) and dispenser registrations as sections in the markdown file. Count what you wrote and report the counts in the markdown file: the jar inventory says addRecipe 189, addShapelessRecipe 176, addSmelting 16, ChestGenHooks 9 - explain any difference.`
        : `For EACH class write a section "### <Class>": role, fields/constants and their meaning, every method with behaviour, and the port mapping for 1.21.1 (AI goals, packets as CustomPacketPayload, key mappings, dispenser behaviours, sound events, stats holders -> config).`,
  }
  const domain = b.key.startsWith('entity') ? 'entity' : b.key.startsWith('itemblock') ? 'itemblock' : b.key.startsWith('world') ? 'world' : 'core'
  const file = `${MOD}/docs/catalog/verhalten/${b.key}.md`
  return `You are cataloguing the BEHAVIOUR of a batch of decompiled OreSpawn classes so a port can be built from your text plus the source.
${CTX}
YOUR BATCH: ${b.key}
Classes (read every one of these source files completely): ${b.classes.join(', ')}

${focus[domain]}

Write everything to ${file} (create the directory if needed; start the file with "# Verhalten: ${b.key}" and a one-paragraph German summary of the batch).
Do not touch any other file${b.key === 'core-01b' ? ' except recipes.json' : ''}. Do not run Gradle.
Return the structured object; every class of your batch must appear in "classes" exactly once, spelled exactly as given.`
}

function designPrompt(d) {
  let scope
  if (d.entities) {
    scope = `Entities (manifest registry ids): ${d.entities.join(', ')}
For EACH id write "### <lang name> (\`<id>\`)" with:
- Aussehen: shape, silhouette, colours, notable features - LOOK at the texture PNGs (Read the PNG files under ${MOD}/reference/jar/extracted/ using the legacy paths from the manifest renderers[].textures[2]; the Read tool shows images) and at the part names in reference/jar/models/<Model>.json; cross-check the wiki description in docs/research/01-mobs.md.
- Größe: hitbox from the manifest, renderer scale (manifest renderers[].gl_scale / renderer_args and the Render<X>.java source), resulting approximate height/length in blocks with the derivation.
- Modell: model class, part count, texture size, translucent part groups and whole-model GL transforms (docs/research/06-models-design.md "Port notes").
- Animationen: what moves and when, driven by which inputs (walk cycle, idle, wings, jaw, attack, flying, sitting) - from reference/jar/anim/<Model>.json and the model source render()/setRotationAngles().
- Varianten: texture variants and what selects them.
- Klang: sound events used (manifest sounds referenced_by).
- Portierungshinweise Modell/Renderer: render passes, scale() override, variant selection, anything that is not a straight LayerDefinition.
Entities without a model (projectiles, heads, invisible helpers): say what is drawn instead (sprite, item, nothing) and keep the entry short.`
  } else if (d.item_kinds) {
    scope = `Items of manifest kinds: ${d.item_kinds.join(', ')} (extract them from manifest.json "items" by kind).
Group items into families (e.g. one armor set, one tool tier, a food group, all critter cages, all spawn eggs). For each family write "### <family>" with a table (id | name | icon | look) and then:
- Aussehen of the icons: LOOK at a representative sample of icon PNGs (Read under ${MOD}/reference/jar/extracted/; legacy path in textures[2]) - for large uniform families (cages, spawn eggs) describe the scheme once and list only the exceptions.
- For items with an item_renderer (3D weapons: Big Bertha, Slice, Royal Guardian Sword, SquidZooka, Attitude Adjuster, Battle Axe, Chainsaw, Queen Scale Battle Axe): describe the 3D model (model json + texture) and its in-hand transforms (renderers_dump.txt).
- Armor: the armor layer textures (worn look), per set.
- Portierungshinweise: item model JSON vs BlockEntityWithoutLevelRenderer, armor layer paths in 1.21.1, spawn egg colours.`
  } else {
    scope = `All blocks in manifest.json "blocks". Group by family (ores, crystal dimension blocks, plants and crops with growth stages, leaves/logs, dried egg ores, torches, utility blocks, spawner blocks, furnace/workbench).
For each family write "### <family>" with a table (id | name | texture | look) - LOOK at representative block PNGs under ${MOD}/reference/jar/extracted/ - and then describe render type (cutout/translucent/cross plant), growth stage textures, lit variants, emissive blocks, and the port notes (blockstate + model JSON shape: cube_all, cross, crop, orientable; light level; which need a BlockEntityRenderer).`
  }
  const file = `${MOD}/docs/catalog/design/${d.key}.md`
  return `You are the art director cataloguing the VISUAL DESIGN of OreSpawn for a 1:1 port. The original models and textures are reused unchanged, so your job is an exact, vivid, verifiable description - not a redesign.
${CTX}
YOUR BATCH: ${d.key}
${scope}

Write everything to ${file} (create the directory if needed; start with "# Design: ${d.key}" and a short German paragraph on the visual language of this batch).
Do not touch any other file. Do not run Gradle.
Return the structured object; every requested id (or every item/block id of your kinds) must appear in "entries" exactly once.`
}

phase('Verhalten')
const behaviourBatches = []
for (const b of args.behaviour) {
  if (b.key === 'core-01') {
    behaviourBatches.push(Object.assign({}, b, { key: 'core-01a' }))
    behaviourBatches.push(Object.assign({}, b, { key: 'core-01b' }))
  } else {
    behaviourBatches.push(b)
  }
}
log(`${behaviourBatches.length} Verhaltens-Pakete, ${args.design.length} Design-Pakete`)

const [behaviour, design] = await Promise.all([
  parallel(behaviourBatches.map(b => () => agent(behaviourPrompt(b), { label: `verhalten:${b.key}`, phase: 'Verhalten', schema: BEHAVIOUR_SCHEMA }))),
  parallel(args.design.map(d => () => agent(designPrompt(d), { label: `design:${d.key}`, phase: 'Design', model: 'fable', schema: DESIGN_SCHEMA }))),
])

phase('Lücken')
const covered = new Set()
behaviour.filter(Boolean).forEach(r => r.classes.forEach(c => covered.add(c.class)))
const wantClasses = args.behaviour.flatMap(b => b.classes)
const missingClasses = wantClasses.filter(c => !covered.has(c))
const failedBehaviour = behaviourBatches.filter((b, i) => !behaviour[i])
log(`Verhalten: ${covered.size}/${new Set(wantClasses).size} Klassen abgedeckt, ${missingClasses.length} fehlen, ${failedBehaviour.length} Pakete ausgefallen`)

const wantIds = args.design.filter(d => d.entities).flatMap(d => d.entities)
const coveredIds = new Set()
design.filter(Boolean).forEach(r => r.entries.forEach(e => coveredIds.add(e.id)))
const missingIds = wantIds.filter(id => !coveredIds.has(id))
const failedDesign = args.design.filter((d, i) => !design[i] && !d.entities)
log(`Design: ${wantIds.length - missingIds.length}/${wantIds.length} Entities beschrieben, ${failedDesign.length} Item/Block-Pakete ausgefallen`)

const retries = []
// Keep each missing class in the domain of the batch it came from, so a world or core class gets the
// world or core instructions instead of the item/block ones.
const missingByDomain = {}
for (const c of missingClasses) {
  const origin = (args.behaviour.find(x => x.classes.includes(c)) || { key: 'itemblock' }).key
  const dom = origin.split('-')[0]
  ;(missingByDomain[dom] = missingByDomain[dom] || []).push(c)
}
for (const [dom, list] of Object.entries(missingByDomain)) {
  const size = dom === 'world' ? 1 : 12
  for (let i = 0; i < list.length; i += size) {
    const b = { key: `${dom}-nachtrag-${String(i / size + 1).padStart(2, '0')}`, classes: list.slice(i, i + size) }
    retries.push(agent(behaviourPrompt(b), { label: `verhalten:${b.key}`, phase: 'Lücken', schema: BEHAVIOUR_SCHEMA }))
  }
}
for (let i = 0; i < missingIds.length; i += 20) {
  const d = { key: `design-entities-nachtrag-${String(i / 20 + 1).padStart(2, '0')}`, entities: missingIds.slice(i, i + 20) }
  retries.push(agent(designPrompt(d), { label: `design:${d.key}`, phase: 'Lücken', model: 'fable', schema: DESIGN_SCHEMA }))
}
for (const d of failedDesign) {
  retries.push(agent(designPrompt(d), { label: `design:${d.key}:retry`, phase: 'Lücken', model: 'fable', schema: DESIGN_SCHEMA }))
}
const retryResults = (await Promise.all(retries)).filter(Boolean)
log(`${retries.length} Nachträge gelaufen, ${retryResults.length} mit Ergebnis`)

phase('Übersicht')
const allBehaviour = behaviour.filter(Boolean).concat(retryResults.filter(r => r.classes))
const allDesign = design.filter(Boolean).concat(retryResults.filter(r => r.entries))
const complexity = {}
allBehaviour.forEach(r => r.classes.forEach(c => { complexity[c.port_complexity] = (complexity[c.port_complexity] || 0) + 1 }))
const digest = allBehaviour.map(r => `## ${r.key} -> ${r.file}\n` + r.classes.map(c =>
  `- ${c.class} [${c.port_complexity}] ids=${c.ids.join(',') || '-'} :: ${c.summary}` + (c.port_risks.length ? ` RISKS: ${c.port_risks.join(' / ')}` : '') +
  (c.depends_on.length ? ` DEPS: ${c.depends_on.join(',')}` : '')).join('\n') +
  (r.unresolved.length ? `\nOFFEN: ${r.unresolved.join(' / ')}` : '')).join('\n\n')
const designDigest = allDesign.map(r => `## ${r.key} -> ${r.file}\n` + r.entries.map(e => `- ${e.id}: ${e.look} | ${e.size_blocks}`).join('\n') +
  (r.unresolved.length ? `\nOFFEN: ${r.unresolved.join(' / ')}` : '')).join('\n\n')

const readme = await agent(`You are writing the entry page of the OreSpawn port catalogue.
${CTX}
The catalogue consists of:
- generated tables: ${MOD}/docs/catalog/10-entities.md, 20-items.md, 30-blocks.md, 40-models.md, 50-materials.md, 60-world-config-sounds.md, manifest.json, classes.json, recipes.json
- behaviour chapters: ${MOD}/docs/catalog/verhalten/*.md
- design chapters: ${MOD}/docs/catalog/design/*.md

Complexity counts across all classes: ${JSON.stringify(complexity)}
Coverage: behaviour ${covered.size}/${new Set(wantClasses).size} classes before retries, design ${wantIds.length - missingIds.length}/${wantIds.length} entities before retries; ${retryResults.length} retry batches returned.

BEHAVIOUR DIGEST:
${digest}

DESIGN DIGEST:
${designDigest}

Write ${MOD}/docs/catalog/README.md in German:
1. Was dieser Katalog ist, wie er entstanden ist (Jar, Quelltext, Recherche; Rangfolge der Quellen) und wie man ihn liest - with links to every file.
2. Inhalt in Zahlen (from manifest counts and the digests).
3. Portierungsaufwand je Bereich: a table domain | classes | S/M/L/XL | biggest risks. Derive domains from the batch keys.
4. Abhängigkeitsreihenfolge: which classes/systems must exist before others (from DEPS), as an ordered list of 8-12 build waves, each wave naming its classes or class families. This will become the implementation plan, so be precise and complete: every class must land in exactly one wave; list any class you cannot place.
5. Querschnittsrisiken: the risks that repeat across many classes (e.g. HP above 1024, GL passes, SRG names, chunk-boundary structures, config-driven stats), each with the affected classes.
6. Offene Fragen: all OFFEN items, deduplicated and grouped.
Return a short plain-text summary (max 25 lines): counts, the wave list in one line per wave, and the top 5 risks.`, { label: 'readme', phase: 'Übersicht' })

return { complexity, covered: covered.size, wantClasses: new Set(wantClasses).size, missingClassesBeforeRetry: missingClasses, missingIdsBeforeRetry: missingIds, retries: retries.length, readme }
