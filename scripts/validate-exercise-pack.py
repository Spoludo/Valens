#!/usr/bin/env python3
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT/'exercise-packs'/'bundled'/'isometric-foundations'
def load(p):
    return json.loads(p.read_text(encoding='utf-8'))
def main():
    patterns={p['id'] for p in load(PACK/'movement-patterns.json')['patterns']}
    muscles={m['id'] for m in load(PACK/'muscles.json')['muscles']}
    joints={j['id'] for j in load(PACK/'joints.json')['joints']}
    errors=[]
    for path in sorted((PACK/'exercises').glob('*.json')):
        ex=load(path)
        if ex['movementPatternId'] not in patterns: errors.append(f"{path.name}: unknown pattern {ex['movementPatternId']}")
        for section in ('primary','secondary','stabilizers'):
            for item in ex['muscles'][section]:
                if item['id'] not in muscles: errors.append(f"{path.name}: unknown muscle {item['id']}")
        for key in ex['jointStress']:
            if key not in joints: errors.append(f"{path.name}: unknown joint stress {key}")
        for key in ex['fatigueCost']['joint']:
            if key not in joints: errors.append(f"{path.name}: unknown joint fatigue {key}")
    if errors:
        print('Validation failed:'); print('\n'.join(' - '+e for e in errors)); raise SystemExit(1)
    print(f"OK: {len(list((PACK/'exercises').glob('*.json')))} exercises, {len(patterns)} movement patterns")
if __name__=='__main__': main()
