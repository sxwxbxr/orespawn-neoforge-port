export const meta = {
  name: 'orespawn-wave',
  description: 'Build one implementation wave of the OreSpawn port: parallel porters, one integrator, GameTests, faithfulness review, fixes',
  whenToUse: 'Once per wave; pass the wave definition from docs/port/waves/wNN.json as args',
  phases: [
    { title: 'Bau', detail: 'porters write only their assigned files' },
    { title: 'Integration', detail: 'apply registry entries, build until green' },
    { title: 'Tests', detail: 'GameTests for the wave content on a real server' },
    { title: 'Treue', detail: 'reviewers compare the port against the original source' },
    { title: 'Nacharbeit', detail: 'fix confirmed findings, final build' },
  ],
}

// args: {
//   wave: 'w01', title: '...', goal: '...',
//   workers: [{ key, task, classes: [...], owns: [...], model?: 'fable' }],
//   gametests: 'what the GameTests of this wave must prove',
//   reviewClasses: ['TheKing', ...],
//   codeModel?: 'fable'      // model for porters without their own, integrator, tests, fixes
// }
//
// Models: the user's standing preference is Fable 5.1 for code agents. On 2026-09-11 Fable hit its
// usage limit during W04 and the user chose to continue on the session model instead. So the model
// is an argument: codeModel 'fable' restores the preference, no codeModel inherits the session
// model. A worker's own 'model' wins, which lets a resumed wave keep a porter that already finished
// on Fable in the cache (the cache key includes the model).
//
// Guards (added after W02 lost all agents to a session limit and the integrator then ran on
// nothing): a porter without a result gets one retry; if any porter is still missing, the wave
// stops before integration, because integrating half a wave produces a green build that hides the
// missing half. Likewise nothing runs after an integrator or test agent that returned nothing.

const MOD = 'F:/Repositories/Modpacks/mods/orespawn'
const W = args
const withModel = (opts, model) => (model ? Object.assign({ model }, opts) : opts)
const CTX = `
PROJECT: private 1:1 port of OreSpawn 1.7.10 (build 20.3) to NeoForge 1.21.1. Mod root: ${MOD} (git repo, modid orespawn, package com.swbr.orespawn).
READ FIRST, BINDING: ${MOD}/docs/DECISIONS.md (rulings R0-R26) and ${MOD}/docs/STYLE.md (build rules, sources of truth, traps).
Hand-off notes of earlier waves: ${MOD}/docs/port/W*.md - read the ones for waves before this one.
Catalogue: ${MOD}/docs/catalog/README.md (wave plan), manifest.json (ids, stats, textures - extract with python, never Read whole),
verhalten/*.md (behaviour per class), design/*.md (look, animation), recipes.json, biome_map.json.
Original source: ${MOD}/reference/src-20.2/src/main/java/danger/orespawn/<Class>.java; SRG names via ${MOD}/reference/jar/mcp/methods.csv and fields.csv.
1.21.1 Minecraft + NeoForge sources, unzipped, Parchment names: F:/Repositories/Modpacks/mods/architrave/.cache/sources/ - grep before every API call you are not sure of.
Already in the tree (generated, never edit, re-run the generator instead): client/model/geom/*Geometry.java (tools/gen_models.py), registry/ModSounds.java (tools/gen_sounds.py),
config/OreSpawnConfig.java (tools/gen_config.py; read registration-time values via config/EarlyConfig.get), assets textures/sounds/sounds.json/lang (tools/assets.py).
Build: from ${MOD} run  unset JAVA_HOME && ./gradlew build --console=plain  (Git Bash). GameTests: ./gradlew runGameTests.

THIS WAVE: ${W.wave} - ${W.title}
GOAL: ${W.goal}
`

const WORKER_SCHEMA = {
  type: 'object',
  properties: {
    key: { type: 'string' },
    files_written: { type: 'array', items: { type: 'string' } },
    registry_entries: {
      type: 'array',
      description: 'lines that must go into shared files you do not own (registry holders, mod constructor, client setup, datagen), each with its target',
      items: { type: 'object', properties: { target_file: { type: 'string' }, code: { type: 'string' }, why: { type: 'string' } }, required: ['target_file', 'code', 'why'] },
    },
    port_deviations: { type: 'array', items: { type: 'string' }, description: 'every place where the port differs from the original and why (also written as // PORT: comments)' },
    not_done: { type: 'array', items: { type: 'string' } },
  },
  required: ['key', 'files_written', 'registry_entries', 'port_deviations', 'not_done'],
}

const porter = (w, suffix) => agent(`You are a porter in a parallel team. Port exactly your assignment from the original 1.7.10 source to NeoForge 1.21.1.
${CTX}
YOUR ASSIGNMENT (${w.key}): ${w.task}
Original classes to port: ${(w.classes || []).join(', ') || '(see task)'}
You own ONLY these paths (create/edit files only there): ${w.owns.join(', ')}

Rules:
- Read the behaviour chapter for each class in docs/catalog/verhalten/ AND the original source file completely before writing.
- Port behaviour line by line with the original numbers and order of checks. Every deviation gets a // PORT: comment and an entry in port_deviations.
- Anything that must be added to a file you do not own (registry holder fields, register(modBus) calls, renderer/layer registration, attribute creation, spawn placement, datagen) goes into registry_entries - do not edit those files.
- Do NOT run Gradle (other porters are writing at the same time; the integrator builds). Make the code compile by construction: grep the 1.21.1 sources for every signature you use.
- If files in your owned paths already exist from an interrupted earlier attempt, read them and finish or replace them; do not leave half-written files.
- Other porters in this wave, for reference only: ${W.workers.filter(x => x.key !== w.key).map(x => x.key + ' -> ' + x.owns.join(',')).join(' | ')}
Return the structured object.`, (w.model || W.codeModel)
  // Key order label, phase, model, schema is the order the first W04 run used; the workflow cache
  // compares options, and a porter that already finished must replay instead of calling a model
  // whose limit is exhausted.
  ? { label: `bau:${w.key}${suffix}`, phase: 'Bau', model: w.model || W.codeModel, schema: WORKER_SCHEMA }
  : { label: `bau:${w.key}${suffix}`, phase: 'Bau', schema: WORKER_SCHEMA })

phase('Bau')
log(`${W.wave}: ${W.workers.length} Portierer, Code-Modell ${W.codeModel || 'Sitzungsmodell'}`)
const work = await parallel(W.workers.map(w => () => porter(w, '')))

const firstMissing = W.workers.map((w, i) => i).filter(i => !work[i])
if (firstMissing.length) {
  log(`${firstMissing.length} Portierer ohne Ergebnis, zweiter Versuch`)
  const again = await parallel(firstMissing.map(i => () => porter(W.workers[i], ':retry')))
  firstMissing.forEach((i, k) => { work[i] = again[k] })
}
const stillMissing = W.workers.filter((w, i) => !work[i]).map(w => w.key)
if (stillMissing.length) {
  log(`Abbruch vor der Integration: ${stillMissing.join(', ')} ohne Ergebnis`)
  return { wave: W.wave, aborted: 'porters', missingPorters: stillMissing, porters: work.filter(Boolean).map(r => ({ key: r.key, files: r.files_written })) }
}

const done = work.filter(Boolean)
const entries = done.flatMap(r => r.registry_entries.map(e => Object.assign({ from: r.key }, e)))
log(`${done.length}/${W.workers.length} Portierer fertig, ${entries.length} Registry-Einträge, ${done.reduce((n, r) => n + r.files_written.length, 0)} Dateien`)

phase('Integration')
const BUILD_SCHEMA = {
  type: 'object',
  properties: {
    green: { type: 'boolean' },
    iterations: { type: 'integer' },
    fixes: { type: 'array', items: { type: 'string' } },
    remaining_errors: { type: 'array', items: { type: 'string' } },
    log_tail: { type: 'string' },
  },
  required: ['green', 'iterations', 'fixes', 'remaining_errors', 'log_tail'],
}
const integration = await agent(`You are the integrator of this wave. You are the only one allowed to run Gradle right now.
${CTX}
The porters wrote these files:
${done.map(r => `- ${r.key}: ${r.files_written.join(', ')}` + (r.not_done.length ? `\n  NOT DONE: ${r.not_done.join(' / ')}` : '')).join('\n')}
${W.workers.length - done.length} porter(s) returned nothing: ${W.workers.filter((w, i) => !work[i]).map(w => w.key).join(', ') || 'none'}.

Registry entries to apply to shared files (registry holders, OreSpawn.java, client setup, attribute/spawn events, datagen):
${entries.map(e => `### ${e.target_file} (from ${e.from}) - ${e.why}\n${e.code}`).join('\n\n') || '(none)'}

Do:
1. Apply every registry entry, merging cleanly (one holder class per registry, init() called from the mod constructor, DeferredRegister.register(modBus)).
2. Run the build. Fix every compile error - in any file of this wave - by checking the 1.21.1 sources, never by deleting behaviour. If a porter's file is fundamentally broken, fix it; do not stub it out silently (a stub needs a // PORT: TODO comment and must appear in remaining_errors).
3. Repeat until the build is green or you are certain a problem needs a decision; then stop and report it.
4. Append an integration section to docs/port/${W.wave.toUpperCase()}.md (German): what was merged, what the next waves must know (hand-off contracts, class names, holder fields).
Return the structured object with the last 30 lines of the final build output in log_tail.`, withModel({ label: 'integration', phase: 'Integration', effort: 'high', schema: BUILD_SCHEMA }, W.codeModel))

if (!integration) {
  log('Abbruch: Integrator ohne Ergebnis')
  return { wave: W.wave, aborted: 'integration', porters: done.map(r => ({ key: r.key, files: r.files_written.length, deviations: r.port_deviations, not_done: r.not_done })) }
}

phase('Tests')
const TEST_SCHEMA = {
  type: 'object',
  properties: {
    tests_written: { type: 'array', items: { type: 'string' } },
    passed: { type: 'integer' }, failed: { type: 'integer' },
    real_defects_found: { type: 'array', items: { type: 'string' } },
    server_log_problems: { type: 'array', items: { type: 'string' }, description: 'Failed to create mod instance, Cowardly refusing, registry or datapack parse errors, exceptions - with the log line' },
    green: { type: 'boolean' },
  },
  required: ['tests_written', 'passed', 'failed', 'real_defects_found', 'server_log_problems', 'green'],
}
const tests = await agent(`You write and run the GameTests of this wave on a real dedicated server.
${CTX}
Integration result: green=${integration.green}; remaining: ${integration.remaining_errors.join(' / ') || 'none'}.
WHAT THE TESTS MUST PROVE: ${W.gametests}

Rules: tests live in src/main/java/com/swbr/orespawn/gametest/ (one class per wave: ${W.wave.toUpperCase()}GameTests), use @GameTestHolder/@PrefixGameTestTemplate(false) with the template "arena" exactly like the existing W00GameTests and W01GameTests.
Run ./gradlew runGameTests. Read the server log (run/gametest/logs/latest.log) BEFORE the test summary: grep "/(ERROR|FATAL)\\]", "Failed to create mod instance", "Cowardly refusing", "Failed to parse".
A failing test that reveals a real defect: fix the defect in the port (not the test), rebuild, rerun. Report what you fixed in real_defects_found.
Return the structured object.`, withModel({ label: 'gametests', phase: 'Tests', effort: 'high', schema: TEST_SCHEMA }, W.codeModel))

if (!tests) {
  log('Abbruch: Test-Agent ohne Ergebnis')
  return { wave: W.wave, aborted: 'tests', integration }
}

phase('Treue')
const FIND_SCHEMA = {
  type: 'object',
  properties: {
    findings: { type: 'array', items: { type: 'object', properties: {
      original: { type: 'string', description: 'Class.java:LINE in the original' },
      port: { type: 'string', description: 'path:LINE in the port, or "missing"' },
      what: { type: 'string', description: 'the concrete divergence: number, condition, branch, order, missing behaviour' },
      severity: { type: 'string', enum: ['wrong', 'missing', 'minor'] },
      documented: { type: 'boolean', description: 'true if a // PORT: comment or DECISIONS ruling already explains it' },
    }, required: ['original', 'port', 'what', 'severity', 'documented'] } },
  },
  required: ['findings'],
}
// reviewGroupSize lets a cheap wave use fewer reviewers (usage budget, 2026-09-14).
const review = W.reviewClasses || []
const groupSize = W.reviewGroupSize || 6
const groups = []
for (let i = 0; i < review.length; i += groupSize) groups.push(review.slice(i, i + groupSize))
const reviews = await parallel(groups.map((g, gi) => () => agent(`You are a skeptical reviewer. Your only question: does the port do what the original did?
${CTX}
Original classes to check: ${g.join(', ')}
For each: read the original source completely, find the port (grep for the class name under ${MOD}/src), and compare branch by branch - numbers, conditions, tick timings, random chances, drops, sounds, targets, NBT/data fields, config keys read.
Also check every method the port Javadoc calls dead or "without counterpart" against the jar bytecode (reference/jar/orespawn-1.7.10-20.3.jar: a method that overrides a vanilla method is live only if the class file contains its func_/field_ SRG name) - W03 shipped four swords without blocking because a live override was declared dead.
Report only real divergences with exact line references. A divergence explained by a // PORT: comment or a DECISIONS.md ruling is documented=true. Do not report style. Do not edit any file.`, { label: `treue:${gi + 1}`, phase: 'Treue', schema: FIND_SCHEMA })))
const reviewsMissing = reviews.filter(r => !r).length
const findings = reviews.filter(Boolean).flatMap(r => r.findings).filter(f => !f.documented && f.severity !== 'minor')
log(`Treue: ${findings.length} undokumentierte Abweichungen (wrong/missing)` + (reviewsMissing ? `, ${reviewsMissing} Prüfer ohne Ergebnis` : ''))

phase('Nacharbeit')
let final = null
if (findings.length || !integration.green || !tests.green) {
  final = await agent(`You fix what the review and tests found in this wave, then prove the build and tests are green.
${CTX}
Undocumented divergences from the original (fix each in the port, or if it cannot be ported, add a // PORT: comment explaining why):
${findings.map(f => `- [${f.severity}] ${f.original} vs ${f.port}: ${f.what}`).join('\n') || '(none)'}
Build remaining errors: ${integration.remaining_errors.join(' / ') || 'none'}
Test problems: ${[...tests.server_log_problems, tests.failed ? tests.failed + ' failed tests' : ''].filter(Boolean).join(' / ') || 'none'}
Then run ./gradlew build and ./gradlew runGameTests and report honestly.`, withModel({ label: 'nacharbeit', phase: 'Nacharbeit', effort: 'high', schema: BUILD_SCHEMA }, W.codeModel))
}

return {
  wave: W.wave,
  porters: done.map(r => ({ key: r.key, files: r.files_written.length, deviations: r.port_deviations, not_done: r.not_done })),
  missingPorters: [],
  reviewsMissing,
  integration, tests, findings, final,
}
