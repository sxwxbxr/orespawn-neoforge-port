export const meta = {
  name: 'orespawn-bughunt',
  description: 'Read-only bug hunt over the finished OreSpawn waves: find real in-game defects, verify each adversarially, write one report',
  whenToUse: 'While a wave is building; hunters never edit source and never run Gradle',
  phases: [
    { title: 'Suche', detail: 'one hunter per feature area' },
    { title: 'Prüfung', detail: 'two independent skeptics per finding' },
    { title: 'Bericht', detail: 'deduplicated report for the fix wave' },
  ],
}

// args: { areas: [{ key, scope }] }
// Hunters and verifiers are strictly read-only: another wave may be writing the tree and running
// Gradle at the same time. The only file written is docs/port/BUGHUNT2.md by the report agent.

const MOD = 'F:/Repositories/Modpacks/mods/orespawn'
const CTX = `
PROJECT: private 1:1 port of OreSpawn 1.7.10 (build 20.3) to NeoForge 1.21.1. Mod root: ${MOD}.
Binding rulings: ${MOD}/docs/DECISIONS.md (R0-R25). Hand-off notes of finished waves: ${MOD}/docs/port/W01.md ... W13.md, FIX1.md, BUGHUNT.md (bugs already found and fixed - do not re-report them).
Original source: ${MOD}/reference/src-20.2/src/main/java/danger/orespawn/<Class>.java; SRG names via reference/jar/mcp/methods.csv, fields.csv; the jar reference/jar/orespawn-1.7.10-20.3.jar.
Behaviour chapters: ${MOD}/docs/catalog/verhalten/*.md. 1.21.1 sources (Parchment): F:/Repositories/Modpacks/mods/architrave/.cache/sources/.
STRICTLY READ-ONLY: do not edit any file, do not run Gradle (a build wave is running in parallel). Python/grep/javap are fine.

WHAT COUNTS AS A BUG (things a player would notice in game, or that crash/corrupt):
1. The R22 class, found by the user in play: a hardcoded 1.7.10 vanilla block/item/biome/entity list that covers a whole category in 1.7.10 but misses its 1.21.1 members - e.g. the Miner's Dream left andesite, diorite and granite. Look for Blocks.STONE, DIRT, SAND, LOG/LEAVES, ores, flowers, grass, wool, planks, Items.* sets, EntityType sets, biome lists, and Y ranges that assume 0..256 in the overworld (-64..320).
2. Behaviour that differs from the original source without a // PORT: comment or a DECISIONS ruling.
3. Crashes and server problems: client classes reachable from common code, NPEs on missing data, unsynchronised state the client renders, NBT keys not saved/loaded, registry ids referenced but not registered, missing assets (item model, blockstate, lang, loot table, sound).
4. Things that silently do nothing: event handlers never registered, DeferredHolders never class-loaded, tags that name no existing file, recipes/loot pointing at wrong ids, goals added with the wrong priority or never added.
NOT a bug: style, performance guesses, anything already listed as fixed in BUGHUNT.md/FIX1.md or accepted by a ruling. ALL waves W00-W13 are built now; this is the final hunt before the first play test, so crashes, missing assets and features that silently do nothing matter most. At most 8 findings per area, strongest first.
Give exact file:line in the port and Class.java:line in the original, and a concrete reproduction a player could do.
`

const FIND_SCHEMA = {
  type: 'object',
  properties: {
    bugs: { type: 'array', items: { type: 'object', properties: {
      title: { type: 'string' },
      port: { type: 'string', description: 'path:line in the port' },
      original: { type: 'string', description: 'Class.java:line, or "n/a"' },
      category: { type: 'string', enum: ['r22-list', 'divergence', 'crash', 'silent', 'asset'] },
      repro: { type: 'string', description: 'what a player does and what happens vs. what should happen' },
      fix: { type: 'string', description: 'the concrete fix in one or two sentences' },
      severity: { type: 'string', enum: ['high', 'medium', 'low'] },
    }, required: ['title', 'port', 'original', 'category', 'repro', 'fix', 'severity'] } },
  },
  required: ['bugs'],
}
const VERDICT_SCHEMA = {
  type: 'object',
  properties: {
    real: { type: 'boolean' },
    reason: { type: 'string', description: 'what you checked in port, original and 1.21.1 sources' },
  },
  required: ['real', 'reason'],
}

phase('Suche')
const found = await parallel(args.areas.map(a => () => agent(`You are a bug hunter for one feature area of the port.
${CTX}
YOUR AREA (${a.key}): ${a.scope}
Read the port code of this area completely, compare it against the original source and against what exists in a 1.21.1 world. Report every real bug you can substantiate. Prefer fewer, certain findings over many guesses.`, { label: `suche:${a.key}`, phase: 'Suche', schema: FIND_SCHEMA })))

const all = []
found.forEach((r, i) => { if (r) r.bugs.forEach(b => all.push(Object.assign({ area: args.areas[i].key }, b))) })
const missingAreas = args.areas.filter((a, i) => !found[i]).map(a => a.key)
log(`${all.length} Kandidaten aus ${args.areas.length - missingAreas.length} Bereichen` + (missingAreas.length ? `, ohne Ergebnis: ${missingAreas.join(', ')}` : ''))

phase('Prüfung')
const judged = await parallel(all.map((b, i) => () => parallel([1, 2].map(n => () => agent(`You are a skeptic. Try to REFUTE this reported bug. Default to real=false if you cannot confirm it in the code.
${CTX}
REPORTED BUG (${b.area}, ${b.severity}): ${b.title}
Port: ${b.port}
Original: ${b.original}
Repro: ${b.repro}
Proposed fix: ${b.fix}
Check the port lines, the original lines, the relevant DECISIONS ruling and the 1.21.1 sources. real=true only if a player would really hit this and no ruling or // PORT: comment already accepts it.`, { label: `pruef:${i + 1}.${n}`, phase: 'Prüfung', schema: VERDICT_SCHEMA })))
  .then(vs => ({ b, votes: vs.filter(Boolean) }))))

const confirmed = judged.filter(j => j.votes.length === 2 && j.votes.every(v => v.real)).map(j => Object.assign({ checks: j.votes.map(v => v.reason) }, j.b))
const disputed = judged.filter(j => j.votes.some(v => v.real) && !(j.votes.length === 2 && j.votes.every(v => v.real))).map(j => Object.assign({ checks: j.votes.map(v => (v.real ? 'real: ' : 'refuted: ') + v.reason) }, j.b))
log(`${confirmed.length} bestätigt, ${disputed.length} strittig, ${all.length - confirmed.length - disputed.length} verworfen`)

phase('Bericht')
const report = await agent(`Write the bug-hunt report ${MOD}/docs/port/BUGHUNT2.md in German (identifiers, paths and item names stay English). This is the only file you may write.
${CTX}
CONFIRMED (both skeptics agreed):
${JSON.stringify(confirmed, null, 1)}

DISPUTED (one skeptic agreed):
${JSON.stringify(disputed, null, 1)}

Areas without a result: ${missingAreas.join(', ') || 'none'}.

Structure: 1. Kurzfassung (counts per severity and category). 2. Bestätigte Fehler, grouped by area, a table per area: Titel | Port | Original | Wie ein Spieler es merkt | Fix | Schwere. Merge duplicates. 3. Strittige Punkte with both verdicts in one line each. 4. Fehlerklassen, die mehrfach vorkommen, and where else the same pattern may hide (grep hints). Return a plain-text summary of at most 15 lines.`, { label: 'bericht', phase: 'Bericht' })

return { candidates: all.length, confirmed: confirmed.length, disputed: disputed.length, missingAreas, report }
