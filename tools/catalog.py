"""Build the OreSpawn port catalogue from the original jar's extracted inventory.

Inputs (all under reference/, which is git-ignored reference material):
  jar/inventory.json      every registration, stat, material, renderer, sound, config key
  jar/models/*.json       1.7.10 model geometry reconstructed from bytecode
  jar/anim_summary.txt    animated parts and animation bytecode size per model
  src-20.2/.../*.java     decompiled source, used only for the superclass chain

Outputs:
  docs/catalog/manifest.json   single source of truth for every later generator and agent
  docs/catalog/*.md            the same data as readable tables (German prose, English data)

Every number here comes from the jar. Nothing is typed in by hand; if a value is
missing in the inventory it is written as "?" and never guessed.
"""
from __future__ import annotations

import collections
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REF = ROOT / "reference"
JAR = REF / "jar"
SRC = REF / "src-20.2" / "src" / "main" / "java" / "danger" / "orespawn"
OUT = ROOT / "docs" / "catalog"

RL_OK = re.compile(r"^[a-z0-9_./-]+$")


def snake(name: str) -> str:
    """Registry-safe snake_case: 'The King' -> the_king, EntityThrownRock -> entity_thrown_rock."""
    s = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name.strip())
    s = re.sub(r"[^A-Za-z0-9]+", "_", s).strip("_").lower()
    return re.sub(r"_+", "_", s)


def rl_id(unloc: str) -> str:
    return re.sub(r"_+", "_", re.sub(r"[^a-z0-9_]", "_", unloc.lower())).strip("_")


def superclass_map() -> dict[str, str]:
    sup = {}
    for f in SRC.glob("*.java"):
        t = f.read_text(encoding="utf-8", errors="replace")
        m = re.search(r"\bclass\s+(\w+)\s*(?:<[^>]*>)?\s*(?:extends\s+([\w.]+))?", t)
        if m:
            sup[m.group(1)] = (m.group(2) or "Object").split(".")[-1]
    return sup


def chain(cls: str, sup: dict[str, str]) -> list[str]:
    out, seen = [], set()
    while cls and cls not in seen:
        seen.add(cls)
        out.append(cls)
        cls = sup.get(cls)
    return out


def item_kind(cls: str, ch: list[str]) -> str:
    if cls == "CritterCage" or cls == "ZooCage":
        return "cage"
    if cls == "ItemSpawnEgg":
        return "spawn_egg"
    if "ItemArmor" in ch or cls == "ItemOreSpawnArmor":
        return "armor"
    if "ItemSword" in ch or "ItemBow" in ch:
        return "weapon"
    if any(c in ch for c in ("ItemPickaxe", "ItemAxe", "ItemSpade", "ItemHoe", "ItemFishingRod")):
        return "tool"
    if "ItemFood" in ch:
        return "food"
    if any(c in ch for c in ("ItemSeeds", "ItemReed")):
        return "seed"
    return "item"


def block_kind(cls: str, ch: list[str]) -> str:
    if cls == "OreGenericEgg":
        return "dried_egg_ore"
    if any(c in ch for c in ("BlockCrops", "BlockReed", "BlockFlower", "BlockBush", "BlockSapling")):
        return "plant"
    if "BlockLeaves" in ch:
        return "leaves"
    if "BlockLog" in ch:
        return "log"
    if "BlockTorch" in ch:
        return "torch"
    if cls.lower().startswith("ore") or "BlockOre" in ch:
        return "ore"
    if "BlockContainer" in ch:
        return "container"
    return "block"


def texture_dest(src: str) -> str:
    """1.7.10 path -> 1.21.1 path. Resource locations must be lowercase in 1.21.1."""
    p = src.replace("\\", "/")
    rest = p.split("assets/orespawn/", 1)[1]
    name = rest.split("/")[-1].lower()
    if rest.startswith("textures/items/"):
        return f"assets/orespawn/textures/item/{name}"
    if rest.startswith("textures/blocks/"):
        return f"assets/orespawn/textures/block/{name}"
    if rest.startswith("textures/"):
        sub = "/".join(rest.split("/")[1:-1]).lower()
        return f"assets/orespawn/textures/{sub}/{name}" if sub else f"assets/orespawn/textures/{name}"
    return f"assets/orespawn/textures/entity/{name}"


def lang_name(obj: dict, lang_objects: dict) -> str | None:
    for entry in obj.get("lang") or []:
        if entry[0] == "en_US":
            return entry[1]
    for entry in lang_objects.get(obj.get("field"), []):
        if entry[0] == "en_US":
            return entry[1]
    return None


def fmt(v) -> str:
    if v is None:
        return "?"
    if isinstance(v, float):
        return f"{v:g}"
    if isinstance(v, (list, tuple)):
        return " / ".join(fmt(x) for x in v)
    return str(v).replace("|", "/").replace("\n", " ")


def load_anim() -> dict[str, dict]:
    rows = {}
    lines = (JAR / "anim_summary.txt").read_text(encoding="utf-8").splitlines()
    head = lines[0].split("\t")
    for line in lines[1:]:
        cols = line.split("\t")
        if len(cols) == len(head):
            rows[cols[0]] = dict(zip(head, cols))
    return rows


def main() -> int:
    inv = json.loads((JAR / "inventory.json").read_text(encoding="utf-8"))
    sup = superclass_map()
    anim = load_anim()
    lang_objects = inv["lang_objects"]
    problems: list[str] = []

    # ---- models ---------------------------------------------------------------
    models = {}
    for f in sorted((JAR / "models").glob("*.json")):
        m = json.loads(f.read_text(encoding="utf-8"))
        a = anim.get(m["model"], {})
        models[m["model"]] = {
            "model": m["model"],
            "status": m.get("status"),
            "texture_size": [m.get("textureWidth"), m.get("textureHeight")],
            "parts": len(m.get("parts", [])),
            "boxes": sum(len(p.get("boxes", [])) for p in m.get("parts", [])),
            "animated_parts": int(a["animated_parts"]) if a.get("animated_parts", "").isdigit() else None,
            "anim_bytecode_len": int(a["anim_bytecode_len"]) if a.get("anim_bytecode_len", "").isdigit() else None,
            "anim_methods": a.get("methods"),
            "gl": a.get("gl"),
            "geometry_file": f"reference/jar/models/{f.name}",
            "anim_file": f"reference/jar/anim/{f.name}",
            "source_file": f"reference/src-20.2/src/main/java/danger/orespawn/{m['model']}.java",
            "used_by": [],
        }

    # ---- renderers --------------------------------------------------------------
    renderer_by_entity = {}
    item_renderer_rows = []
    for r in inv["renderers"]:
        if "entity" in r:
            renderer_by_entity.setdefault(r["entity"].split("/")[-1], []).append(r)
        else:
            item_renderer_rows.append(r)

    texture_map: dict[str, str] = {}

    def map_tex(src: str) -> str:
        if "assets/orespawn/" not in src.replace("\\", "/"):
            # a vanilla texture referenced by path (e.g. the creeper armor overlay); nothing to copy
            return "minecraft:" + src.replace("\\", "/").split("assets/minecraft/", 1)[-1]
        dst = texture_dest(src)
        prev = next((k for k, v in texture_map.items() if v == dst and k != src), None)
        if prev:
            problems.append(f"texture collision after lowercasing: {prev} and {src} -> {dst}")
        texture_map[src] = dst
        return dst

    # ---- entities ---------------------------------------------------------------
    entities = []
    used_ids = collections.Counter()
    for e in inv["entities"]:
        cls = e["cls"].split("/")[-1]
        name = e.get("name") or e.get("lang")
        eid = snake(name) if name else snake(cls)
        used_ids[eid] += 1
        attrs = e.get("attributes") or {}
        rends = renderer_by_entity.get(cls, [])
        rinfo = []
        for r in rends:
            det = r.get("details") or {}
            texs = [[map_tex(t[0]), t[1], t[0]] for t in det.get("textures", [])]
            rinfo.append({
                "renderer": r.get("renderer"),
                "renderer_args": r.get("renderer_args"),
                "model": r.get("model"),
                "model_args": r.get("model_args"),
                "textures": texs,
                "gl_scale": det.get("gl_scale"),
                "super": det.get("super"),
                "shadow_expr": det.get("super_ctor_args"),
            })
            if r.get("model") in models:
                models[r["model"]]["used_by"].append(eid)
        entities.append({
            "id": eid,
            "name": name,
            "class": cls,
            "superclass_chain": chain(cls, sup)[1:],
            "vanilla_base": (e.get("kind_root") or "").split("/")[-1] or None,
            "max_health": (attrs.get("maxHealth") or {}).get("values"),
            "attack_damage": (attrs.get("attackDamage") or {}).get("values"),
            "movement_speed": (attrs.get("movementSpeed") or {}).get("values"),
            "follow_range": (attrs.get("followRange") or {}).get("values"),
            "knockback_resistance": (attrs.get("knockbackResistance") or {}).get("values"),
            "attribute_exprs": {k: v.get("expr") for k, v in attrs.items()},
            "armor_value": e.get("armor_value"),
            "size": e.get("size"),
            "tracking": [e.get("trackingRange"), e.get("updateFrequency"), e.get("sendVelocity")],
            "spawns": e.get("spawns") or [],
            "renderers": rinfo,
            "source_file": f"reference/src-20.2/src/main/java/danger/orespawn/{cls}.java",
        })
    for eid, n in used_ids.items():
        if n > 1:
            problems.append(f"entity id used {n} times: {eid}")

    # ---- items and blocks --------------------------------------------------------
    items, blocks = [], []
    for o in inv["objects"]:
        if o["kind"] not in ("item", "block"):
            continue
        cls = o.get("cls") or "?"
        ch = chain(cls, sup)
        unloc = o.get("unloc")
        rid = rl_id(unloc) if unloc else snake(o["field"].removeprefix("My"))
        texs = [[map_tex(t[0]), t[1], t[0]] for t in o.get("textures") or []]
        rec = {
            "id": rid,
            "name": lang_name(o, lang_objects),
            "field": o["field"],
            "class": cls,
            "superclass_chain": ch[1:],
            "unlocalized": unloc,
            "ctor_args": o.get("ctor_args"),
            "calls": o.get("calls"),
            "textures": texs,
            "legacy_registry": [r[1] for r in o.get("registry") or []],
            "source_file": f"reference/src-20.2/src/main/java/danger/orespawn/{cls}.java",
        }
        if o["kind"] == "item":
            rec["kind"] = item_kind(cls, ch)
            items.append(rec)
        else:
            rec["kind"] = block_kind(cls, ch)
            blocks.append(rec)

    block_ids = {b["id"] for b in blocks}
    seen = collections.Counter(b["id"] for b in blocks)
    for bid, n in seen.items():
        if n > 1:
            problems.append(f"block id used {n} times: {bid}")
    item_seen = collections.Counter()
    for it in items:
        if it["id"] in block_ids:
            problems.append(f"item id equals a block id, renamed to {it['id']}_item: {it['field']}")
            it["id"] = it["id"] + "_item"
        item_seen[it["id"]] += 1
    for iid, n in item_seen.items():
        if n > 1:
            problems.append(f"item id used {n} times: {iid}")

    # ---- item renderers (3D held weapons) -------------------------------------------
    # IItemRenderer registrations from ClientProxyOreSpawn.registerRenderThings
    for r in item_renderer_rows:
        det = r.get("details") or {}
        created = [c.split("(")[0] for c in det.get("models_created", [])]
        match = next((i for i in items if i["field"] == r.get("item")), None)
        if not match or not created:
            problems.append(f"item renderer not resolvable: {r.get('item')} / {r.get('renderer')}")
            continue
        match["item_renderer"] = {
            "renderer": r.get("renderer"),
            "model": created[0],
            "textures": [[map_tex(t[0]), t[1], t[0]] for t in det.get("textures", [])],
            "gl": det.get("gl"),
            "gl_scale": det.get("gl_scale"),
        }
        match["item_renderer_model"] = created[0]
        if created[0] in models:
            models[created[0]]["used_by"].append("item:" + match["id"])

    for m in models.values():
        if not m["used_by"]:
            problems.append(f"model not used by any renderer: {m['model']}")

    # ---- textures not yet mapped (armor layers, overlays, spinners, gui) ------------
    for t in inv["textures"]:
        if t[0] not in texture_map:
            map_tex(t[0])
    bad = [v for v in texture_map.values() if not RL_OK.match(v)]
    for b in bad:
        problems.append(f"texture path not a valid resource location: {b}")

    sounds = []
    for s in inv["sounds"]:
        sid = rl_id(s["event"])
        if sid != s["event"]:
            problems.append(f"sound event renamed: {s['event']} -> {sid}")
        sounds.append({"id": sid, "legacy_event": s["event"], "files": [f.lower() for f in s["files"]],
                       "legacy_files": s["files"], "referenced_by": s.get("referenced_by")})

    manifest = {
        "_about": "Generated by tools/catalog.py from reference/jar/inventory.json. Do not edit by hand.",
        "source_jar": {"file": "orespawn-1.7.10-20.3.jar", "sha1": "d43dbe9a400dc8df06418da3e04d36422b2176d7"},
        "counts": {"entities": len(entities), "items": len(items), "blocks": len(blocks), "models": len(models),
                   "textures": len(texture_map), "sounds": len(sounds), "config_keys": len(inv["config"])},
        "entities": entities,
        "items": items,
        "blocks": blocks,
        "models": list(models.values()),
        "tool_materials": inv["tool_materials"],
        "armor_materials": inv["armor_materials"],
        "mob_stats": inv["mobstats"],
        "dimensions": inv["dimensions"],
        "sounds": sounds,
        "config": inv["config"],
        "texture_map": texture_map,
        "problems": problems,
    }
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "manifest.json").write_text(json.dumps(manifest, indent=1, ensure_ascii=False), encoding="utf-8")
    write_markdown(manifest)

    print(json.dumps(manifest["counts"]))
    print("item kinds:", dict(collections.Counter(i["kind"] for i in items)))
    print("block kinds:", dict(collections.Counter(b["kind"] for b in blocks)))
    print(f"problems: {len(problems)}")
    for p in problems:
        print("  -", p)
    return 0


def table(headers: list[str], rows: list[list]) -> str:
    out = ["| " + " | ".join(headers) + " |", "|" + "---|" * len(headers)]
    out += ["| " + " | ".join(fmt(c) for c in r) + " |" for r in rows]
    return "\n".join(out)


def write_markdown(m: dict) -> None:
    ents = m["entities"]
    head = ("# OreSpawn-Katalog\n\n"
            "Erzeugt von `tools/catalog.py` aus dem Original-Jar "
            f"(`{m['source_jar']['file']}`, SHA-1 `{m['source_jar']['sha1']}`). "
            "Alle Zahlen stammen aus dem Jar; `?` heißt: statisch nicht auflösbar, nicht geraten.\n\n"
            "Mehrere Werte in einer Zelle bedeuten Verzweigungen im Originalcode "
            "(zahm/wild, PlayNicely, Größenstufen).\n")

    rows = []
    def hp_key(e):
        nums = [v for v in e["max_health"] or [] if isinstance(v, (int, float))]
        return -(max(nums) if nums else -1)

    for e in sorted(ents, key=hp_key):
        r = e["renderers"][0] if e["renderers"] else {}
        rows.append([f"`{e['id']}`", e["name"], e["class"], e["vanilla_base"], e["max_health"], e["attack_damage"],
                     e["movement_speed"], e["armor_value"], e["size"], r.get("model"),
                     len(e["spawns"]) or "-"])
    (m_path := OUT / "10-entities.md").write_text(
        head + "\n## Entities\n\n" + table(
            ["id", "Name", "Klasse", "Basis", "HP", "Angriff", "Tempo", "Rüstung", "Hitbox b×h", "Modell", "Spawnregeln"],
            rows) + "\n", encoding="utf-8")

    by_kind = collections.defaultdict(list)
    for i in m["items"]:
        by_kind[i["kind"]].append(i)
    parts = [head]
    for kind in sorted(by_kind):
        parts.append(f"\n## {kind} ({len(by_kind[kind])})\n")
        parts.append(table(["id", "Name", "Klasse", "Konstruktor", "Textur", "3D-Modell"],
                           [[f"`{i['id']}`", i["name"], i["class"], i["ctor_args"],
                             i["textures"][0][0].split("/")[-1] if i["textures"] else "-",
                             i.get("item_renderer_model", "-")] for i in by_kind[kind]]))
    (OUT / "20-items.md").write_text("\n".join(parts) + "\n", encoding="utf-8")

    by_kind = collections.defaultdict(list)
    for b in m["blocks"]:
        by_kind[b["kind"]].append(b)
    parts = [head]
    for kind in sorted(by_kind):
        parts.append(f"\n## {kind} ({len(by_kind[kind])})\n")
        parts.append(table(["id", "Name", "Klasse", "Konstruktor", "Aufrufe", "Textur"],
                           [[f"`{b['id']}`", b["name"], b["class"], b["ctor_args"], b["calls"],
                             b["textures"][0][0].split("/")[-1] if b["textures"] else "-"] for b in by_kind[kind]]))
    (OUT / "30-blocks.md").write_text("\n".join(parts) + "\n", encoding="utf-8")

    rows = [[m_["model"], m_["used_by"], m_["parts"], m_["boxes"], m_["texture_size"], m_["animated_parts"],
             m_["anim_bytecode_len"], m_["gl"]] for m_ in sorted(m["models"], key=lambda x: -x["parts"])]
    (OUT / "40-models.md").write_text(
        head + "\n## Modelle (Geometrie aus dem Bytecode)\n\n"
        "Jedes Teil ist genau eine Box. Pivot, Box und UV gehen unverändert in `PartPose`/`CubeListBuilder`; "
        "die Konventionen sind in `docs/research/06-models-design.md` belegt.\n\n"
        + table(["Modell", "benutzt von", "Teile", "Boxen", "Textur", "animierte Teile", "Anim-Bytes", "GL"], rows)
        + "\n", encoding="utf-8")

    tm = [[t["name"], t["harvestLevel"], t["maxUses"], t["efficiency"], t["damage"], t["enchantability"]]
          for t in m["tool_materials"]]
    am = [[a["name"], a["durability"], a["reductions"], a["enchantability"],
           {k: v for k, v in (a.get("all_stats") or {}).items() if k.startswith("e_") and v}]
          for a in m["armor_materials"]]
    (OUT / "50-materials.md").write_text(
        head + "\n## Werkzeugmaterialien\n\n" + table(["Material", "Stufe", "Haltbarkeit", "Effizienz", "Schaden", "Verzauberbarkeit"], tm)
        + "\n\n## Rüstungsmaterialien\n\nSchutz je Teil: Kopf, Brust, Beine, Füße. Die `e_*`-Werte sind automatische "
          "Verzauberungsstufen des Originals.\n\n"
        + table(["Material", "Haltbarkeitsfaktor", "Schutz", "Verzauberbarkeit", "Auto-Verzauberungen"], am)
        + "\n\n## Mob-Werte aus der Config\n\n"
        + table(["Config-Name", "HP", "Angriff", "Verteidigung"],
                [[v.get("name"), v.get("health"), v.get("attack"), v.get("defense")] for v in m["mob_stats"].values()])
        + "\n", encoding="utf-8")

    (OUT / "60-world-config-sounds.md").write_text(
        head + "\n## Dimensionen\n\n"
        + table(["Provider", "Name", "Chunk-Generator", "Biome-Manager"],
                [[d["provider"], d.get("dimensionName"), d.get("chunkProvider"), d.get("worldChunkManager")] for d in m["dimensions"]])
        + "\n\n## Sounds\n\n" + table(["id", "Dateien", "benutzt von"], [[s["id"], s["files"], s["referenced_by"]] for s in m["sounds"]])
        + "\n\n## Config-Schlüssel\n\n" + table(["Kategorie", "Schlüssel", "Default", "Typ", "Kommentar"],
                                              [[c["category"], c["key"], c["default"], c["type"], c["comment"]] for c in m["config"]])
        + "\n\n## Offene Punkte des Generators\n\n" + ("\n".join(f"- {p}" for p in m["problems"]) or "keine") + "\n",
        encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())
