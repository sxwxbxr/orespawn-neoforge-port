"""Copy OreSpawn's original textures and sounds into the 1.21.1 resource layout.

Input:  docs/catalog/manifest.json (texture_map, sounds, names) and reference/jar/extracted/assets/orespawn/
Output: src/main/resources/assets/orespawn/{textures,sounds}/..., sounds.json, lang/en_us.json

The files are reused byte-for-byte; only paths change, because 1.21.1 resource locations must be
lowercase. This is a private port - see docs/DECISIONS.md, R0.

Re-running is safe: owned output directories are cleared first, so a renamed texture never
leaves its old copy behind.
"""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXTRACTED = ROOT / "reference" / "jar" / "extracted"
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "orespawn"
OWNED = [ASSETS / "textures", ASSETS / "sounds"]
RL_PATH = re.compile(r"^[a-z0-9_./-]+$")


def title(rid: str) -> str:
    return " ".join(w.capitalize() for w in rid.split("_"))


def main() -> int:
    origin = EXTRACTED / "assets" / "orespawn"
    if not (origin / "sounds.json").exists():
        print(
            f"The extracted original jar is missing.\n"
            f"  expected: {origin}\n"
            f"\n"
            f"This repository ships no textures or sounds. Unpack your own copy of\n"
            f"orespawn-1.7.10-20.3.jar into reference/jar/extracted/ first:\n"
            f"\n"
            f"  mkdir -p reference/jar/extracted\n"
            f"  cd reference/jar/extracted && unzip /path/to/orespawn-1.7.10-20.3.jar\n"
            f"\n"
            f"See README.md, \"Build it yourself\", step 3.",
            file=sys.stderr,
        )
        return 2

    manifest = json.loads((ROOT / "docs" / "catalog" / "manifest.json").read_text(encoding="utf-8"))
    problems = []
    for d in OWNED:
        if d.exists():
            shutil.rmtree(d)

    copied = 0
    for src, dst in manifest["texture_map"].items():
        if not dst.startswith("assets/orespawn/"):
            continue
        s = EXTRACTED / src
        if not s.exists():
            problems.append(f"texture missing in jar extract: {src}")
            continue
        if not RL_PATH.match(dst.split("assets/orespawn/", 1)[1]):
            problems.append(f"invalid resource path: {dst}")
            continue
        t = RES / dst
        t.parent.mkdir(parents=True, exist_ok=True)
        if t.exists():
            problems.append(f"two textures map to {dst}")
        shutil.copyfile(s, t)
        copied += 1

    # Armor layers. ItemOreSpawnArmor.getArmorTexture returns "orespawn:<set>_1.png" / "_2.png"
    # (ItemOreSpawnArmor.java:254-337), i.e. files at the asset root, which texture_map files under
    # textures/entity/. 1.21.1 resolves ArmorMaterial.Layer("orespawn:<set>") to
    # textures/models/armor/<set>_layer_1.png and _layer_2.png, so the same bytes are copied there too.
    armor_sets = ["ultimate", "lavaeel", "mothscale", "experience", "ruby", "amethyst", "pink",
                  "tigerseye", "peacock", "mobzilla", "royal", "lapis", "queen", "emerald"]
    layers = 0
    for armor_set in armor_sets:
        for n in (1, 2):
            s = EXTRACTED / "assets" / "orespawn" / f"{armor_set}_{n}.png"
            if not s.exists():
                problems.append(f"texture missing in jar extract: armor layer {s.name}")
                continue
            t = ASSETS / "textures" / "models" / "armor" / f"{armor_set}_layer_{n}.png"
            t.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(s, t)
            layers += 1

    legacy_sounds = json.loads((EXTRACTED / "assets" / "orespawn" / "sounds.json").read_text(encoding="utf-8"))
    sounds_json, ogg = {}, 0
    for event, spec in legacy_sounds.items():
        eid = event.lower()
        files = []
        for name in spec["sounds"]:
            src = EXTRACTED / "assets" / "orespawn" / "sounds" / f"{name}.ogg"
            if not src.exists():
                problems.append(f"sound file missing: {name}.ogg (event {event})")
                continue
            dst = ASSETS / "sounds" / f"{name.lower()}.ogg"
            dst.parent.mkdir(parents=True, exist_ok=True)
            if not dst.exists():
                shutil.copyfile(src, dst)
                ogg += 1
            files.append(f"orespawn:{name.lower()}")
        if eid in sounds_json:
            problems.append(f"sound event collides after lowercasing: {event}")
        sounds_json[eid] = {"sounds": files}
    for f in (EXTRACTED / "assets" / "orespawn" / "sounds").glob("*.ogg"):
        if not (ASSETS / "sounds" / f.name.lower()).exists():
            problems.append(f"ogg not referenced by sounds.json, not copied: {f.name}")
    (ASSETS / "sounds.json").write_text(json.dumps(sounds_json, indent=2) + "\n", encoding="utf-8")

    lang, unnamed = {}, 0
    for kind, key in (("entities", "entity"), ("items", "item"), ("blocks", "block")):
        for e in manifest[kind]:
            name = e.get("name")
            if not name:
                unnamed += 1
                name = title(e["id"])
            lang[f"{key}.orespawn.{e['id']}"] = name
    # Key binding of the port (KeyHandler.java:12, :25). The visible label is the original's raw
    # string; the category had no lang entry in 1.7.10 and showed its key verbatim.
    lang["key.orespawn.fly_up"] = "OreSpawn UP/FAST"
    lang["key.categories.orespawn"] = "OreSpawn"
    # The wall twins of the torches (DECISIONS R18) have no manifest entry: same display name as
    # the standing block, visible only in debug output (they have no item).
    lang["block.orespawn.crystaltorch_wall"] = lang["block.orespawn.crystaltorch"]
    lang["block.orespawn.extremetorch_wall"] = lang["block.orespawn.extremetorch"]
    lang["block.orespawn.krakenrepellent_wall"] = lang["block.orespawn.krakenrepellent"]
    lang["block.orespawn.creeperrepellent_wall"] = lang["block.orespawn.creeperrepellent"]
    # BetterFireball and ThunderBolt were never registered in 1.7.10 (catalogue 6.6) and have no
    # manifest entry; the port registers them (W04) and names them after the class, like the
    # generated "LaserBall" and "WaterBall".
    lang["entity.orespawn.better_fireball"] = "BetterFireball"
    lang["entity.orespawn.thunder_bolt"] = "ThunderBolt"
    lang_dir = ASSETS / "lang"
    lang_dir.mkdir(parents=True, exist_ok=True)
    (lang_dir / "en_us.json").write_text(json.dumps(dict(sorted(lang.items())), indent=2, ensure_ascii=False) + "\n",
                                         encoding="utf-8")

    print(f"textures copied: {copied}, armor layers: {layers}, ogg copied: {ogg}, sound events: {len(sounds_json)}, "
          f"lang keys: {len(lang)} ({unnamed} without an original name, title-cased from the id)")
    print(f"problems: {len(problems)}")
    for p in problems:
        print("  -", p)
    return 1 if any(p.startswith(("texture missing", "invalid", "two textures")) for p in problems) else 0


if __name__ == "__main__":
    sys.exit(main())
