#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "exercise-packs" / "bundled" / "isometric-foundations"
SIDE_ROLES = {"bilateral", "midline", "workingSide", "supportSide", "oppositeSide", "left", "right"}

def load(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)

def check_joint_load_map(errors, filename, field_name, value, joint_ids):
    if not isinstance(value, dict):
        errors.append(f"{filename}: {field_name} must be an object")
        return

    for joint_id, role_map in value.items():
        if joint_id not in joint_ids:
            errors.append(f"{filename}: unknown joint {joint_id} in {field_name}")
        if not isinstance(role_map, dict):
            errors.append(f"{filename}: {field_name}.{joint_id} must be an object of side roles")
            continue
        for role in role_map.keys():
            if role not in SIDE_ROLES:
                errors.append(f"{filename}: invalid side role {role} in {field_name}.{joint_id}")

def main():
    patterns = load(PACK / "movement-patterns.json")["patterns"]
    muscles = load(PACK / "muscles.json")["muscles"]
    joints = load(PACK / "joints.json")["joints"]

    pattern_ids = {p["id"] for p in patterns}
    muscle_ids = {m["id"] for m in muscles}
    joint_ids = {j["id"] for j in joints}

    errors = []
    exercise_count = 0

    for ex_path in sorted((PACK / "exercises").glob("*.json")):
        exercise_count += 1
        ex = load(ex_path)
        filename = ex_path.name

        if ex["movementPatternId"] not in pattern_ids:
            errors.append(f"{filename}: unknown movementPatternId {ex['movementPatternId']}")

        for section in ["primary", "secondary", "stabilizers"]:
            for item in ex["muscles"][section]:
                if item["id"] not in muscle_ids:
                    errors.append(f"{filename}: unknown muscle {item['id']}")

        check_joint_load_map(errors, filename, "jointStress", ex["jointStress"], joint_ids)
        check_joint_load_map(errors, filename, "fatigueCost.joint", ex["fatigueCost"]["joint"], joint_ids)

    if errors:
        print("Validation failed:")
        for e in errors:
            print(" -", e)
        raise SystemExit(1)

    print(f"OK: {exercise_count} exercises, {len(pattern_ids)} movement patterns")

if __name__ == "__main__":
    main()
