"""Generate the natural spawn table of the port (DECISIONS R11, R18; catalogue 4.13, 6.6).

Writes
  src/main/resources/data/orespawn/spawn_table/spawns.json
  src/main/resources/data/orespawn/neoforge/biome_modifier/config_spawns.json
read at server start by com.swbr.orespawn.world.spawn.ConfigSpawnsBiomeModifier, and
  src/main/resources/data/orespawn/tags/worldgen/biome/legacy_name/<name>.json
read through com.swbr.orespawn.world.structure.LegacyBiomeNames (the biomeName comparisons of OreSpawnWorld).

Sources, all read here, none typed by hand:
  * docs/catalog/manifest.json entities[].spawns - the 348 EntityRegistry.addSpawn calls of
    OreSpawnMain.make_some_more_things. Cross-checked call by call against OreSpawnMain.java
    (:4176-4645) for line numbers and for the guard the manifest cannot express: the 44 Halloween
    lines carry `guards: []`, the real condition is the date (verhalten/core-01a.md, sections 5, 6).
  * BiomeGenUtopianPlains.java - the lists of the five OreSpawn dimension biomes. Parsed from the
    source because the manifest misses several of them (catalogue 6.6: Mobzilla in VillageMania,
    GoldFish, Irukandji, LeafMonster, HerculesBeetle).
  * ChunkProviderOreSpawn2.java getPossibleCreatures - the dino list of Dimension-Extreme.
  * docs/catalog/biome_map.json - 1.7.10 biome field -> 1.21.1 biome ids or biome tags ("#ns:path"), addSpawn
    type -> MobCategory, and legacy_names: legacy_name tag -> the 1.7.10 biome fields whose biomeName it stands for.
    DECISIONS R26: the split-off 1.21.1 biomes belong to their 1.7.10 biome, in the table and in the tags alike.
  * src/main/java/com/swbr/orespawn/config/OreSpawnConfig.java - OreSpawnMain field -> config key
    (CockateilEnable <- BirdEnable, PitchBlackEnable <- NightmareEnable).
  * docs/catalog/README.md sections 4.10-4.12 - which classes arrive only with W09, W10 or W11.

Which list feeds which biome:
  * every addSpawn biome through biome_map.json;
  * `extremeHills` additionally feeds orespawn:mining: WorldProviderOreSpawn2 used the shared
    BiomeGenBase.extremeHills object (WorldProviderOreSpawn2.java:21), so every addSpawn on that biome
    stood in the Extreme dimension too, and getPossibleCreatures put the dino list in front of the
    monster and ambient lists (ChunkProviderOreSpawn2.java:349-413). Both are one 1.7.10 list;
  * utopia = constructor; villages = constructor + setVillageCreatures (appends, duplicates stay);
    islands / crystal / chaos = their setter only (they clear the constructor list first).

Merge rule (DECISIONS R18): when two 1.7.10 lists land on one 1.21.1 biome (forest + forestHills ->
minecraft:forest), entries are not added: per entity, category, guard and target biome the larger entry
wins, compared by (weight, max, min). Entries of the same 1.7.10 list are never merged - VillageMania
really had RedCow twice. A tag target ("#minecraft:is_nether") is one more key of the table; which biomes it
holds is only known at run time, so SpawnTable applies the same rule there when one biome is reached through
several keys (a biome id and a tag, or two tags).

Generated - never edit the outputs by hand.
"""
from __future__ import annotations

import json
import re
import sys
from collections import Counter, OrderedDict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "reference" / "src-20.2" / "src" / "main" / "java" / "danger" / "orespawn"
CATALOG = ROOT / "docs" / "catalog"
CONFIG_JAVA = ROOT / "src" / "main" / "java" / "com" / "swbr" / "orespawn" / "config" / "OreSpawnConfig.java"
OUT_TABLE = ROOT / "src" / "main" / "resources" / "data" / "orespawn" / "spawn_table" / "spawns.json"
OUT_TAGS = ROOT / "src" / "main" / "resources" / "data" / "orespawn" / "tags" / "worldgen" / "biome" / "legacy_name"
OUT_MODIFIER = ROOT / "src" / "main" / "resources" / "data" / "orespawn" / "neoforge" / "biome_modifier" / "config_spawns.json"

# BiomeGenBase list fields (MCP fields.csv field_76762_K / _76761_J / _76755_L / _82914_M) -> addSpawn type.
LIST_TYPE = {
    "spawnableCreatureList": "creature",
    "spawnableMonsterList": "monster",
    "spawnableWaterCreatureList": "waterCreature",
    "spawnableCaveCreatureList": "ambient",
    "MyMobList": "monster",
    "MyAmbientList": "ambient",
}

# MobCategory.getSerializedName() of the 1.21.1 enum constants named in biome_map.json.
CATEGORY_NAME = {
    "CREATURE": "creature",
    "AMBIENT": "ambient",
    "MONSTER": "monster",
    "WATER_CREATURE": "water_creature",
}

# BiomeGenUtopianPlains method -> the OreSpawn biome(s) whose list it fills.
DIMENSION_LISTS = [
    # (target biome, source label, methods that build its list, in order)
    ("orespawn:utopia", "utopia", ["BiomeGenUtopianPlains"]),
    ("orespawn:villages", "villages", ["BiomeGenUtopianPlains", "setVillageCreatures"]),
    ("orespawn:islands", "islands", ["setIslandCreatures"]),
    ("orespawn:crystal", "crystal", ["setCrystalCreatures"]),
    ("orespawn:chaos", "chaos", ["setChaosCreatures"]),
]
MINING_BIOME = "orespawn:mining"
MINING_SOURCE_BIOME = "extremeHills"

RE_ADDSPAWN = re.compile(
    r"EntityRegistry\.addSpawn\(\(Class\)(\w+)\.class, (\d+), (\d+), (\d+), EnumCreatureType\.(\w+), "
    r"new BiomeGenBase\[\] \{ BiomeGenBase\.(\w+) \}\);")
RE_LISTADD = re.compile(
    r"this\.(\w+)\.add\(new BiomeGenBase\.SpawnListEntry\(\(Class\)(\w+)\.class, (\d+), (\d+), (\d+)\)\);")
RE_FLAG_IF = re.compile(r"^\s*if \(OreSpawnMain\.(\w+) != 0\) \{\s*$")
RE_HALLOWEEN_IF = re.compile(r"^\s*if \(nowmonth == 9 && nowday == 31\) \{\s*$")
RE_EASTER_IF = re.compile(r"^\s*if \(OreSpawnMain\.EasterBunnyEnable != 0 && OreSpawnMain\.easter_day != 0\) \{\s*$")
RE_ANY_IF = re.compile(r"^\s*if \(")
RE_TARGET = re.compile(r"^#?[a-z0-9_.-]+:[a-z0-9_./-]+$")
RE_METHOD = re.compile(r"^\s*(?:public|protected|private)\s+(?:static\s+)?(?:[\w<>\[\]]+\s+)?(\w+)\s*\(")


def fail(msg: str) -> None:
    print(f"gen_spawns: {msg}", file=sys.stderr)
    sys.exit(1)


def config_keys() -> dict[str, str]:
    """OreSpawnMain field -> config key, from the Javadoc the config generator writes above each field."""
    text = CONFIG_JAVA.read_text(encoding="utf-8")
    pairs = re.findall(r"/\*\* Target OreSpawnMain\.(\w+), read in preInit -> getMobs\. \*/\s*"
                       r"public final ModConfigSpec\.IntValue (\w+);", text)
    if not pairs:
        fail("no OreSpawnMOBS fields found in OreSpawnConfig.java")
    return dict(pairs)


UNPORTED_WAVES: tuple[str, ...] = ()


def wave_of_class() -> dict[str, str]:
    """Entity classes that arrive with a wave not yet ported (catalogue sections 4.10-4.12).

    W09, W10 (2026-09-14) and W11 (2026-09-14) are ported: their classes are registered and carry no todo
    marker any more. Add a wave id to UNPORTED_WAVES to tag the entities of a wave that is not ported yet.
    """
    readme = (CATALOG / "README.md").read_text(encoding="utf-8")
    result = {}
    for wave in UNPORTED_WAVES:
        m = re.search(rf"^### 4\.\d+ {wave} .*?^- \*\*Entities:\*\* (.*?)$", readme, re.S | re.M)
        if not m:
            fail(f"README has no entity list for {wave}")
        for cls in re.findall(r"`(\w+)`", m.group(1)):
            result[cls] = wave
    return result


def guard_of(line: str):
    """(flags, date) for an `if` line, None for a line that is not a guard."""
    if RE_HALLOWEEN_IF.match(line):
        return ((), "halloween")
    if RE_EASTER_IF.match(line):
        return (("EasterBunnyEnable",), "easter")
    m = RE_FLAG_IF.match(line)
    if m:
        return ((m.group(1),), None)
    if RE_ANY_IF.match(line):
        return "unknown"
    return None


def parse_addspawns() -> list[dict]:
    lines = (SRC / "OreSpawnMain.java").read_text(encoding="utf-8").splitlines()
    out = []
    guard = None
    for no, line in enumerate(lines, start=1):
        g = guard_of(line)
        if g is not None:
            guard = g
        m = RE_ADDSPAWN.search(line)
        if not m:
            continue
        if guard in (None, "unknown"):
            fail(f"OreSpawnMain.java:{no} addSpawn without a recognised guard")
        cls, w, lo, hi, typ, biome = m.groups()
        out.append({"class": cls, "weight": int(w), "min": int(lo), "max": int(hi), "type": typ,
                    "biome": biome, "flags": guard[0], "date": guard[1], "line": no})
    return out


def parse_lists(filename: str) -> list[dict]:
    lines = (SRC / filename).read_text(encoding="utf-8").splitlines()
    out = []
    method = None
    guard = None
    for no, line in enumerate(lines, start=1):
        mm = RE_METHOD.match(line)
        if mm:
            method = mm.group(1)
            guard = None
        g = guard_of(line)
        if g is not None:
            guard = g
        m = RE_LISTADD.search(line)
        if not m:
            continue
        field, cls, w, lo, hi = m.groups()
        if field not in LIST_TYPE:
            fail(f"{filename}:{no} unknown list field {field}")
        if guard in (None, "unknown"):
            fail(f"{filename}:{no} list entry without a recognised guard")
        out.append({"class": cls, "weight": int(w), "min": int(lo), "max": int(hi), "type": LIST_TYPE[field],
                    "flags": guard[0], "date": guard[1], "line": no, "method": method, "file": filename})
    return out


def main() -> int:
    manifest = json.loads((CATALOG / "manifest.json").read_text(encoding="utf-8"))
    biome_map = json.loads((CATALOG / "biome_map.json").read_text(encoding="utf-8"))
    keys = config_keys()
    waves = wave_of_class()
    class_to_id = {e["class"]: e["id"] for e in manifest["entities"]}
    type_to_category = biome_map["spawn_type_to_mob_category"]
    for field, targets in biome_map["map"].items():
        if not targets or len(set(targets)) != len(targets):
            fail(f"biome_map.json: {field} has an empty or duplicated target list")
        for target in targets:
            if not RE_TARGET.match(target):
                fail(f"biome_map.json: {field} target {target!r} is neither a biome id nor a #tag")

    # --- 1. the 348 addSpawn calls: manifest, cross-checked against the source -----------------------
    manifest_calls = []
    for e in manifest["entities"]:
        for s in e["spawns"]:
            if len(s["biomes"]) != 1:
                fail(f"{e['id']}: addSpawn with {len(s['biomes'])} biomes, the original had one per call")
            guards = list(s["guards"])
            date = None
            if "easter_day" in guards:
                guards.remove("easter_day")
                date = "easter"
            elif not guards:
                date = "halloween"  # the manifest drops the date guard; checked against the source below
            manifest_calls.append((e["class"], s["weight"], s["min"], s["max"], s["type"], s["biomes"][0],
                                   tuple(guards), date))
    source_calls = parse_addspawns()
    if len(manifest_calls) != 348:
        fail(f"manifest lists {len(manifest_calls)} addSpawn calls, expected 348")
    src_keys = Counter((c["class"], c["weight"], c["min"], c["max"], c["type"], c["biome"], c["flags"], c["date"])
                       for c in source_calls)
    if src_keys != Counter(manifest_calls):
        diff = (Counter(manifest_calls) - src_keys) + (src_keys - Counter(manifest_calls))
        fail(f"manifest and OreSpawnMain.java disagree on {sum(diff.values())} addSpawn calls: {list(diff)[:5]}")

    # --- 2. collect (target biome, source list, entry) ----------------------------------------------
    raw = []  # (target, source label, order, entry)
    order = 0

    def entry(c: dict, origin: str) -> dict:
        cls = c["class"]
        if cls not in class_to_id:
            fail(f"{origin}: class {cls} has no manifest id")
        cat = type_to_category.get(c["type"])
        if cat is None:
            fail(f"{origin}: unknown spawn type {c['type']}")
        flags = []
        for f in c["flags"]:
            if f not in keys:
                fail(f"{origin}: guard {f} is not an OreSpawnMOBS config field")
            flags.append(keys[f])
        e = OrderedDict()
        e["entity"] = f"orespawn:{class_to_id[cls]}"
        e["category"] = CATEGORY_NAME[cat]
        e["weight"] = c["weight"]
        e["min"] = c["min"]
        e["max"] = c["max"]
        e["flags"] = flags
        if c["date"]:
            e["date"] = c["date"]
        e["source"] = origin
        if cls in waves:
            e["todo"] = waves[cls]
        return e

    for c in sorted(source_calls, key=lambda c: c["line"]):
        biome = c["biome"]
        if biome not in biome_map["map"]:
            fail(f"OreSpawnMain.java:{c['line']} biome {biome} missing in biome_map.json")
        origin = f"OreSpawnMain.java:{c['line']} addSpawn {biome}"
        for target in biome_map["map"][biome]:
            order += 1
            raw.append((target, biome, order, entry(c, origin)))
        if biome == MINING_SOURCE_BIOME:
            order += 1
            raw.append((MINING_BIOME, "mining", order, entry(c, origin + " (Dimension-Extreme shares the biome)")))

    for c in parse_lists("ChunkProviderOreSpawn2.java"):
        if c["method"] != "getPossibleCreatures":
            fail(f"ChunkProviderOreSpawn2.java:{c['line']} list entry outside getPossibleCreatures")
        order += 1
        raw.append((MINING_BIOME, "mining", order,
                    entry(c, f"ChunkProviderOreSpawn2.java:{c['line']} getPossibleCreatures")))

    plains = parse_lists("BiomeGenUtopianPlains.java")
    by_method: dict[str, list[dict]] = {}
    for c in plains:
        by_method.setdefault(c["method"], []).append(c)
    for target, label, methods in DIMENSION_LISTS:
        for method in methods:
            if method not in by_method:
                fail(f"BiomeGenUtopianPlains.java has no list method {method}")
            for c in by_method[method]:
                order += 1
                raw.append((target, label, order,
                            entry(c, f"BiomeGenUtopianPlains.java:{c['line']} {method}")))

    # --- 3. merge per target biome (R18) --------------------------------------------------------------
    table: "OrderedDict[str, list]" = OrderedDict()
    dropped = []
    targets = sorted({t for t, _, _, _ in raw})
    for target in targets:
        groups: "OrderedDict[tuple, OrderedDict[str, list]]" = OrderedDict()
        for t, label, o, e in sorted((r for r in raw if r[0] == target), key=lambda r: r[2]):
            key = (e["entity"], e["category"], tuple(e["flags"]), e.get("date"))
            groups.setdefault(key, OrderedDict()).setdefault(label, []).append(e)
        kept = []
        for key, sources in groups.items():
            if len(sources) == 1:
                kept.extend(next(iter(sources.values())))
                continue
            if any(len(v) > 1 for v in sources.values()):
                fail(f"{target} {key}: a list with several entries meets another list - merge rule undefined")
            best_label = max(sources, key=lambda lb: (sources[lb][0]["weight"], sources[lb][0]["max"],
                                                     sources[lb][0]["min"]))
            kept.append(sources[best_label][0])
            for lb, v in sources.items():
                if lb != best_label:
                    dropped.append({"biome": target, "entity": key[0], "category": key[1],
                                    "dropped": f"{v[0]['weight']} {v[0]['min']}-{v[0]['max']} ({v[0]['source']})",
                                    "kept": f"{sources[best_label][0]['weight']} {sources[best_label][0]['min']}-"
                                            f"{sources[best_label][0]['max']} ({sources[best_label][0]['source']})"})
        table[target] = kept

    # --- 4. legacy_name biome tags (R26: same map as the table) ------------------------------------------
    tags: "OrderedDict[str, list[str]]" = OrderedDict()
    for name, fields in biome_map["legacy_names"].items():
        if name.startswith("_"):
            continue
        values: list[str] = []
        for field in fields:
            if field not in biome_map["map"]:
                fail(f"biome_map.json legacy_names.{name}: {field} missing in map")
            for target in biome_map["map"][field]:
                if target not in values:
                    values.append(target)
        tags[name] = values

    total = sum(len(v) for v in table.values())
    doc = OrderedDict()
    doc["_about"] = ("GENERATED by tools/gen_spawns.py - do not edit. Natural spawn entries per 1.21.1 biome or biome tag "
                     "(key \"#ns:path\"), read by "
                     "the biome modifier orespawn:config_spawns. flags: OreSpawnMOBS config keys that must all be != 0 "
                     "(AllMobsDisable applies through MobSwitches); date: halloween (Oct 31) or easter (Apr 20) at "
                     "server start; todo: the entity type arrives with that wave and is skipped while unregistered.")
    doc["_counts"] = {"addspawn_calls": len(source_calls),
                      "dimension_list_entries": len(plains),
                      "mining_dino_entries": sum(1 for r in raw if r[3]["source"].startswith("ChunkProviderOreSpawn2")),
                      "biomes": sum(1 for t in table if not t.startswith("#")),
                      "tags": sum(1 for t in table if t.startswith("#")),
                      "entries": total, "merged_away": len(dropped)}
    doc["_merged"] = dropped
    doc["biomes"] = table

    OUT_TABLE.parent.mkdir(parents=True, exist_ok=True)
    OUT_TABLE.write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    OUT_MODIFIER.parent.mkdir(parents=True, exist_ok=True)
    OUT_MODIFIER.write_text(json.dumps(OrderedDict([("type", "orespawn:config_spawns"), ("table", "orespawn:spawns")]),
                                       indent=2) + "\n", encoding="utf-8", newline="\n")
    OUT_TAGS.mkdir(parents=True, exist_ok=True)
    for stale in sorted(OUT_TAGS.glob("*.json")):
        if stale.stem not in tags:
            stale.unlink()
    for name, values in tags.items():
        (OUT_TAGS / f"{name}.json").write_text(
            json.dumps(OrderedDict([("replace", False), ("values", values)]), indent=2) + "\n",
            encoding="utf-8", newline="\n")
    print(f"gen_spawns: {len(tags)} legacy_name biome tags")
    print(f"gen_spawns: {len(source_calls)} addSpawn calls, {len(plains)} dimension list entries, "
          f"{doc['_counts']['mining_dino_entries']} dino entries -> {total} entries in {len(table)} biomes and tags "
          f"({len(dropped)} merged away)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
