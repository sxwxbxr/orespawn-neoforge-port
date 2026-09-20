"""Generate data/orespawn/recipe/*.json from docs/catalog/recipes.json (DECISIONS R14).

The 381 registrations of OreSpawnMain.make_some_more_things (OreSpawnMain.java:2326-5034) become
1.21.1 recipe files. Generated - never edit the JSON by hand, fix this script and re-run it:

    python tools/gen_recipes.py

What the script decides, and why (semantics in docs/catalog/verhalten/core-01b.md):

* Shaped patterns are shrunk to their used rows and columns. 1.21.1 shrinks every pattern itself
  (ShapedRecipePattern.unpack -> shrink) and matches it at any grid offset and mirrored, so the
  column/row variants the author registered by hand (sword x3, helmet x2, door x2) collapse into
  one file. PORT: a pattern with an empty row or column in 1.7.10 (bucket, boots, hoverboard, duct
  tape) matched only at that offset; in 1.21.1 it matches anywhere in the grid (R18 case 3 - a JSON
  shaped recipe cannot pin an offset).
* Wildcards (meta 32767) and plain items become plain ingredients; a wildcard on a 1.7.10 block with
  variants is already an id list in recipes.json (red_flower, planks) and becomes the whole 1.21.1
  category (R22, WILDCARD_CATEGORIES): #minecraft:planks, and #minecraft:small_flowers minus the
  dandelion as a neoforge:difference ingredient.
* A bare Item with meta 0 means "undamaged" in 1.7.10. Where that item is damageable
  (ItemSword/ItemAxe/ItemArmor/ItemTool/ItemBow/ItemFishingRod or setMaxDamage in its class) the
  ingredient is a partial `neoforge:components` ingredient with `minecraft:damage` = 0.
  DataComponentPredicate.test reads stack.getComponents(), which includes the prototype, so a
  fresh stack matches and a damaged one does not - the 1.7.10 behaviour.
* The Miner's Dream pair (OreSpawnMain.java:3018-3025) is conditioned on
  `orespawn:miners_dream_expensive` (loot.MinersDreamExpensiveCondition), the cheap one wrapped in
  `neoforge:not`.
* Every orespawn id a recipe names that is not registered in the Java tree yet (waves W09-W11, and
  W12 items owned by other porters) gets a `neoforge:item_exists` condition. The recipe then loads
  silently skipped instead of logging a parse error, and switches itself on once the later wave
  registers the item - no re-run needed. "Registered" is a scan for the id as a string literal in
  registry/ModItems.java, registry/ModBlocks.java and block/ore/OreGenericEgg.java.
* Recipe ids live in the orespawn namespace, so no vanilla recipe is replaced (chest, oak_door,
  cooked_cod keep their vanilla recipes next to these).
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "docs" / "catalog"
OUT = ROOT / "src" / "main" / "resources" / "data" / "orespawn" / "recipe"
JAVA = ROOT / "src" / "main" / "java" / "com" / "swbr" / "orespawn"
REGISTRY_SOURCES = [
    JAVA / "registry" / "ModItems.java",
    JAVA / "registry" / "ModBlocks.java",
    JAVA / "block" / "ore" / "OreGenericEgg.java",
]

WILDCARD = 32767
DAMAGEABLE_BASES = {"ItemSword", "ItemAxe", "ItemArmor", "ItemTool", "ItemPickaxe", "ItemSpade",
                    "ItemHoe", "ItemBow", "ItemFishingRod", "ItemShears"}
CONDITION_EXPENSIVE = {"type": "orespawn:miners_dream_expensive"}

# PORT: R22 - a wildcard on a 1.7.10 block with variants meant the whole category, not the
# variants that existed in 2014. recipes.json spells those wildcards as the 1.7.10 id list; each
# list maps to the 1.21.1 category here (BUGHUNT2 2.3). An unmapped list aborts the run.
WILDCARD_CATEGORIES = {
    # Blocks.red_flower @ 32767 (OreSpawnMain.java:2837-2839, Rose Sword): every small flower
    # except the dandelion (Blocks.yellow_flower, a block of its own in 1.7.10). Same rule as
    # CompanionSupport.isRedFlower: #minecraft:small_flowers minus dandelion, so cornflower,
    # lily of the valley, wither rose and torchflower count.
    ("minecraft:poppy", "minecraft:blue_orchid", "minecraft:allium", "minecraft:azure_bluet",
     "minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip",
     "minecraft:oxeye_daisy"):
        {"type": "neoforge:difference", "base": {"tag": "minecraft:small_flowers"},
         "subtracted": {"item": "minecraft:dandelion"}},
    # Blocks.planks @ 32767 (OreSpawnMain.java:5034, Elevator): every plank. Crystal planks are not
    # in #minecraft:planks (R18), exactly as they were not Blocks.planks.
    ("minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:birch_planks",
     "minecraft:jungle_planks", "minecraft:acacia_planks", "minecraft:dark_oak_planks"):
        {"tag": "minecraft:planks"},
}


def ids_of(obj: dict) -> list[str]:
    v = obj["id"]
    return list(v) if isinstance(v, list) else [v]


def path_of(rid: str) -> str:
    return rid.split(":", 1)[1]


def damageable_items(manifest: dict) -> set[str]:
    out = set()
    for it in manifest["items"]:
        chain = set(it.get("superclass_chain") or [])
        dmg = bool(chain & DAMAGEABLE_BASES)
        src = it.get("source_file")
        if not dmg and src and (ROOT / src).is_file():
            text = (ROOT / src).read_text(encoding="utf-8", errors="replace")
            m = re.search(r"setMaxDamage\((\d+)\)", text)
            dmg = bool(m and int(m.group(1)) > 0)
        if dmg:
            out.add("orespawn:" + it["id"])
    return out


def registered_ids() -> set[str]:
    lits = set()
    for p in REGISTRY_SOURCES:
        lits |= set(re.findall(r'"([a-z0-9_]+)"', p.read_text(encoding="utf-8")))
    return {"orespawn:" + s for s in lits}


def ingredient(obj: dict, damageable: set[str]):
    ids = ids_of(obj)
    if len(ids) > 1:
        category = WILDCARD_CATEGORIES.get(tuple(ids))
        if category is not None:
            return category
        raise SystemExit(f"multi-item ingredient without a category mapping: {ids}")
    rid = ids[0]
    if obj["meta"] == 0 and rid in damageable:
        return {"type": "neoforge:components", "items": rid,
                "components": {"minecraft:damage": 0}, "strict": False}
    if obj["meta"] not in (0, WILDCARD):
        raise SystemExit(f"unexpected ingredient meta {obj['meta']} for {rid}")
    return {"item": rid}


def result(obj: dict) -> dict:
    out = {"id": ids_of(obj)[0]}
    if obj.get("count", 1) != 1:
        out["count"] = obj["count"]
    return out


def shrink(pattern: list[str]) -> list[str]:
    rows = [r for r in pattern]
    width = max(len(r) for r in rows)
    rows = [r.ljust(width) for r in rows]
    used_rows = [i for i, r in enumerate(rows) if r.strip()]
    used_cols = [c for c in range(width) if any(r[c] != " " for r in rows)]
    r0, r1 = used_rows[0], used_rows[-1]
    c0, c1 = used_cols[0], used_cols[-1]
    return [r[c0:c1 + 1] for r in rows[r0:r1 + 1]]


def canon(v) -> str:
    return json.dumps(v, sort_keys=True)


def convert(rec: dict, damageable: set[str]) -> tuple[dict, str, list[str]]:
    """Returns (json, dedupe key, ingredient ids in order of first appearance)."""
    t = rec["type"]
    ing_ids: list[str] = []

    def note(obj):
        for i in ids_of(obj):
            if i not in ing_ids:
                ing_ids.append(i)

    if t == "shaped":
        pat = shrink(rec["pattern"])
        used = {ch for row in pat for ch in row if ch != " "}
        key = {}
        for ch in sorted(used):
            key[ch] = ingredient(rec["key"][ch], damageable)
        for row in pat:
            for ch in row:
                if ch != " ":
                    note(rec["key"][ch])
        out = {"type": "minecraft:crafting_shaped", "pattern": pat, "key": key, "result": result(rec["result"])}

        def form(rows):
            return tuple("".join(" " if ch == " " else canon(key[ch]) + "|" for ch in row) for row in rows)
        mirrored = [row[::-1] for row in pat]
        dkey = canon(["shaped", sorted([form(pat), form(mirrored)])[0], out["result"]])
    elif t == "shapeless":
        ings = [ingredient(i, damageable) for i in rec["ingredients"]]
        for i in rec["ingredients"]:
            note(i)
        out = {"type": "minecraft:crafting_shapeless", "ingredients": ings, "result": result(rec["result"])}
        dkey = canon(["shapeless", sorted(canon(i) for i in ings), out["result"]])
    elif t == "smelting":
        (src,) = rec["ingredients"]
        note(src)
        # cookingtime 200: the 1.7.10 furnace time (TileEntityFurnace.updateEntity @207 sipush 200),
        # equal to the 1.21.1 default; the Crystal Furnace's 150 ticks live in its block entity.
        out = {"type": "minecraft:smelting", "ingredient": ingredient(src, damageable),
               "result": result(rec["result"]), "experience": rec["xp"], "cookingtime": 200}
        dkey = canon(["smelting", out["ingredient"], out["result"], out["experience"]])
    else:
        raise SystemExit(f"unknown recipe type {t}")

    cond = rec.get("condition")
    if cond == "OreSpawnMain.MinersDreamExpensive == 0":
        out["neoforge:conditions"] = [{"type": "neoforge:not", "value": CONDITION_EXPENSIVE}]
    elif cond == "NOT(OreSpawnMain.MinersDreamExpensive == 0)":
        out["neoforge:conditions"] = [CONDITION_EXPENSIVE]
    elif cond is not None:
        raise SystemExit(f"unknown condition {cond!r} at OreSpawnMain.java:{rec['source_line']}")
    if "neoforge:conditions" in out:
        dkey += canon(out["neoforge:conditions"])
    return out, dkey, ing_ids


def main() -> int:
    recipes = json.loads((CATALOG / "recipes.json").read_text(encoding="utf-8"))
    manifest = json.loads((CATALOG / "manifest.json").read_text(encoding="utf-8"))
    damageable = damageable_items(manifest)
    registered = registered_ids()

    unique: dict[str, dict] = {}
    for rec in recipes:
        out, dkey, ing_ids = convert(rec, damageable)
        if dkey in unique:
            unique[dkey]["lines"].append(rec["source_line"])
            continue
        unique[dkey] = {"json": out, "lines": [rec["source_line"]], "ings": ing_ids,
                        "type": rec["type"], "result": ids_of(rec["result"])[0]}

    # Names: result path; smelting gets _from_smelting_<input>; a reload (result among the
    # ingredients) gets _reload; remaining collisions get _from_<ingredients unique to the recipe>.
    groups: dict[str, list[dict]] = {}
    for u in unique.values():
        base = path_of(u["result"])
        if u["type"] == "smelting":
            base += "_from_smelting_" + path_of(u["ings"][0])
        elif u["result"] in u["ings"]:
            base += "_reload"
        u["base"] = base
        groups.setdefault(base, []).append(u)
    names: dict[str, dict] = {}
    for base, members in groups.items():
        if len(members) == 1:
            names[base] = members[0]
            continue
        common = set.intersection(*(set(m["ings"]) for m in members))
        for m in members:
            own = [path_of(i) for i in m["ings"] if i not in common]
            name = base + "_from_" + "_and_".join(own[:2]) if own else base
            n, candidate = 2, name
            while candidate in names:
                candidate = f"{name}_{n}"
                n += 1
            names[candidate] = m

    guarded: dict[str, list[str]] = {}
    for name, u in names.items():
        all_ids = [u["result"]] + u["ings"]
        missing = sorted({i for i in all_ids if i.startswith("orespawn:") and i not in registered})
        if missing:
            conds = [{"type": "neoforge:item_exists", "item": i} for i in missing]
            u["json"]["neoforge:conditions"] = conds + u["json"].get("neoforge:conditions", [])
            for i in missing:
                guarded.setdefault(i, []).append(name)

    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.json"):
        old.unlink()
    for name, u in sorted(names.items()):
        (OUT / f"{name}.json").write_text(json.dumps(u["json"], indent=2) + "\n", encoding="utf-8", newline="\n")

    by_type = {}
    for u in names.values():
        by_type[u["type"]] = by_type.get(u["type"], 0) + 1
    print(f"registrations {len(recipes)} -> files {len(names)} {by_type}")
    print(f"merged duplicates: {len(recipes) - len(names)}")
    undamaged = sorted({path_of(i) for u in names.values()
                        for i in re.findall(r'"items": "([a-z0-9_:]+)"', json.dumps(u["json"]))})
    print(f"damage-0 ingredients on: {undamaged}")
    if guarded:
        print(f"guarded with neoforge:item_exists ({len(guarded)} ids not registered yet):")
        for i, ns in sorted(guarded.items()):
            print(f"  {i}: {', '.join(sorted(ns))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
