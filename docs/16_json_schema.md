# 16 – JSON Schema

**Project:** Valens  
**Version:** 0.4  
**Status:** Implemented specification  
**Last updated:** 2026-07-06

---

## 1. Purpose

Valens exercise packs are data-driven.

This document defines the role of JSON Schema in Valens and points to the canonical schema files.

The canonical JSON Schema files live in:

```text
schemas/
```

The first bundled exercise pack lives in:

```text
exercise-packs/bundled/isometric-foundations/
```

The JSON schema layer is the contract between:

- exercise-pack authors
- the Exercise Library Engine
- the Planner Engine
- the UI
- tests

The app must not infer required fields from examples alone.

---

## 2. Canonical schema files

Current schema files:

```text
schemas/exercise-pack.schema.json
schemas/movement-pattern.schema.json
schemas/movement-patterns.schema.json
schemas/muscle.schema.json
schemas/muscles.schema.json
schemas/joint.schema.json
schemas/joints.schema.json
schemas/exercise.schema.json
```

These files define the implementation contract for loading exercise packs.

The Android app should load JSON into typed Kotlin models only after validation.

---

## 3. Bundled exercise pack structure

Current bundled pack layout:

```text
exercise-packs/
└── bundled/
    └── isometric-foundations/
        ├── README.md
        ├── pack.json
        ├── movement-patterns.json
        ├── muscles.json
        ├── joints.json
        ├── translations/
        │   └── en.json
        └── exercises/
            ├── bear_crawl_hold.json
            ├── calf_raise_hold.json
            ├── hollow_body_hold.json
            ├── horse_stance.json
            ├── pike_pushup_hold.json
            ├── plank_hold.json
            ├── reverse_table_hold.json
            ├── single_leg_balance_hold.json
            ├── single_leg_glute_bridge_hold.json
            ├── wall_push.json
            └── wall_sit.json
```

The `bundled/` layer is intentional.

It distinguishes built-in packs from possible future imported, downloaded, community, or user-created packs.

---

## 4. Pack manifest

Each exercise pack has a `pack.json` manifest.

It defines:

- pack id
- schema version
- pack version
- localization keys
- author
- license
- homepage
- minimum app version
- tags
- content paths

The manifest is validated by:

```text
schemas/exercise-pack.schema.json
```

---

## 5. Movement patterns

Movement patterns define what the planner is trying to train.

They are not exercises.

Example movement patterns:

- `anti_extension_core`
- `squat`
- `hip_extension`
- `horizontal_push`
- `vertical_push`
- `quadruped_stability`
- `calf_raise`
- `single_leg_balance`

The planner should first reason about movement needs, then select exercises that satisfy those patterns.

---

## 6. Muscles and anatomical joints

Muscles and joints are reference data.

They support:

- fatigue estimation
- joint-load estimation
- pain matching
- body maps
- progress visualization
- planner constraints

Current anatomical joint identifiers:

```text
neck
shoulder
elbow
wrist
lumbar_spine
hip
knee
ankle
```

Each joint defines supported sides.

Example:

```json
{
  "id": "knee",
  "nameKey": "joint.knee",
  "region": "knee",
  "sides": ["left", "right"]
}
```

Midline example:

```json
{
  "id": "lumbar_spine",
  "nameKey": "joint.lumbar_spine",
  "region": "spine",
  "sides": ["midline"]
}
```

---

## 7. Side-aware joint load maps

Exercise definitions use anatomical joints plus side roles.

They should not usually encode absolute left/right joint ids.

Example, bilateral:

```json
{
  "jointStress": {
    "knee": {
      "bilateral": 0.6
    },
    "hip": {
      "bilateral": 0.3
    },
    "lumbar_spine": {
      "midline": 0.1
    }
  }
}
```

Example, unilateral:

```json
{
  "sideModel": "left_right",
  "jointStress": {
    "hip": {
      "workingSide": 0.4
    },
    "knee": {
      "workingSide": 0.2
    }
  }
}
```

Supported side roles:

```text
bilateral
midline
workingSide
supportSide
oppositeSide
left
right
```

Use `left` and `right` only for exercises that are genuinely asymmetric by design.

---

## 8. Exercise definitions

Each exercise is a metadata file under:

```text
exercise-packs/bundled/isometric-foundations/exercises/
```

Each exercise must define:

- `id`
- `schemaVersion`
- `nameKey`
- `descriptionKey`
- `type`
- `movementPatternId`
- `exerciseFamilyId`
- `difficulty`
- `equipment`
- `homeFriendly`
- `sideModel`
- `defaultPrescription`
- `muscles`
- `jointStress`
- `fatigueCost`
- `progression`
- `regressions`
- `alternatives`
- `contraindications`
- `cues`
- `assets`

The planner must reason from these fields.

The planner must not special-case exercise names.

---

## 9. Exercise definition example

Example, shortened:

```json
{
  "id": "wall_sit",
  "schemaVersion": "0.2.0",
  "nameKey": "exercise.wall_sit.name",
  "descriptionKey": "exercise.wall_sit.description",
  "type": "isometric",
  "movementPatternId": "squat",
  "exerciseFamilyId": "wall_sit",
  "difficulty": 2,
  "equipment": ["wall"],
  "homeFriendly": true,
  "sideModel": "bilateral",
  "defaultPrescription": {
    "sets": 3,
    "holdSeconds": 30,
    "restSeconds": 60,
    "intensityTarget": 7
  },
  "jointStress": {
    "knee": {
      "bilateral": 0.6
    },
    "hip": {
      "bilateral": 0.3
    },
    "lumbar_spine": {
      "midline": 0.1
    }
  },
  "fatigueCost": {
    "global": 6,
    "local": {
      "quadriceps": 8,
      "glutes": 5
    },
    "joint": {
      "knee": {
        "bilateral": 6
      }
    }
  }
}
```

---

## 10. Validation

Exercise-pack loading must validate:

- JSON syntax
- schema version
- required fields
- movement pattern references
- muscle references
- anatomical joint references
- side-role keys
- progression structure
- regression and alternative references where practical
- translation keys where practical
- asset references where practical

Invalid exercise definitions must not crash the app.

Invalid bundled data should fail tests.

Invalid imported or community packs should be disabled with a clear error.

---

## 11. Local validation script

The repository includes:

```text
scripts/validate-exercise-pack.py
```

Run:

```bash
python3 scripts/validate-exercise-pack.py
```

Expected result:

```text
OK: 11 exercises, 8 movement patterns
```

This script is a lightweight referential integrity check.

It does not replace full JSON Schema validation in the Android app.

---

## 12. Versioning

The structured side-aware joint-load model starts at schema version `0.2.0`.

Breaking schema changes require one of:

- migration logic
- compatibility adapters
- explicit rejection of older pack versions with a clear error

The MVP may support only schema version `0.2.0`, but this limitation must be explicit in code.

---

## 13. Localization

Display strings should use localization keys, not hardcoded text.

Example:

```json
"nameKey": "exercise.wall_sit.name"
```

Translations live under:

```text
translations/
```

The first bundled pack includes:

```text
translations/en.json
```

---

## 14. Assets

Exercise definitions may reference:

- exercise illustrations
- muscle maps
- joint maps
- mistake illustrations
- audio cues
- future videos

Missing assets should not crash the app.

The MVP may show placeholders.

---

## 15. Acceptance criteria

For the MVP:

- bundled packs load from `exercise-packs/bundled/`
- schema files are present under `schemas/`
- exercises validate required fields
- movement pattern references validate
- muscle references validate
- anatomical joint references validate
- side-role keys validate
- invalid bundled definitions fail tests
- invalid runtime/imported definitions fail gracefully
- the planner receives typed exercise metadata, not raw JSON

---

## 16. Summary

The JSON schema layer is now part of the Valens architecture.

It protects the core design principle:

> Exercises are data-defined implementations of movement patterns.

The planner should reason from metadata, not from exercise names.
