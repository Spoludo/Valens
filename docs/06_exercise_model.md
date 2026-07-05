# 06 – Exercise Model

**Project:** Valens  
**Version:** 0.4  
**Status:** Implemented specification  
**Last updated:** 2026-07-06

---

## 1. Purpose

This document defines how exercises are represented in Valens.

Exercises are data, not code.

The planner must be able to add, remove, progress, regress or substitute exercises based on metadata.

The canonical JSON Schema files live in:

```text
schemas/
```

The first bundled exercise pack lives in:

```text
exercise-packs/bundled/isometric-foundations/
```

---

## 2. Core design rule

The planner reasons about movement patterns and metadata.

It must not special-case exercise names.

Correct:

```text
movementPatternId = squat
jointStress.knee.bilateral = 0.6
```

Incorrect:

```text
if exercise.id == wall_sit then protect knee
```

---

## 3. Exercise definition

An exercise definition describes a trainable movement.

Required fields:

```text
id
schemaVersion
nameKey
descriptionKey
type
movementPatternId
exerciseFamilyId
difficulty
equipment
homeFriendly
sideModel
defaultPrescription
muscles
jointStress
fatigueCost
progression
regressions
alternatives
contraindications
cues
assets
```

---

## 4. Exercise types

Supported exercise types:

```text
isometric
dynamic
mobility_dynamic
mobility_isometric
balance
assessment
breathing
```

Examples:

- `wall_sit`: isometric
- `horse_stance`: isometric
- `split_squat`: dynamic
- `weighted_butterfly`: mobility_isometric
- `figure_four_lift`: mobility_dynamic
- `single_leg_balance_hold`: balance

---

## 5. Movement pattern

Each exercise belongs to a movement pattern.

Example:

```json
{
  "id": "wall_sit",
  "movementPatternId": "squat"
}
```

The movement pattern is the planner's primary abstraction.

The exercise is one possible implementation of that pattern.

Example:

```text
Movement pattern: squat

Possible exercises:
- wall_sit
- horse_stance
- supported_split_squat_hold
- chair_squat_hold
```

---

## 6. Exercise family

The exercise family groups closely related variations.

Example:

```json
{
  "id": "wall_sit",
  "exerciseFamilyId": "wall_sit"
}
```

Family-level grouping is useful for:

- progression history
- substitutions
- statistics
- avoiding too much repeated variation
- future exercise browsing

---

## 7. Difficulty

Difficulty is a rough user-facing and planner-facing estimate.

```json
{
  "difficulty": 2
}
```

Initial scale:

```text
1 = very easy / entry-level
2 = easy
3 = moderate
4 = difficult
5 = advanced
```

Progression ladder steps may use a finer 1–10 difficulty scale.

---

## 8. Equipment

Equipment is declared as an array.

Examples:

```json
{
  "equipment": ["wall"]
}
```

```json
{
  "equipment": ["floor", "mat"]
}
```

```json
{
  "equipment": ["none"]
}
```

The planner must filter exercises by available equipment.

The MVP should avoid exercises requiring a pull-up bar by default.

---

## 9. Side model

The `sideModel` field tells the workout engine how to schedule the exercise.

Supported values:

```text
bilateral
left_right
single_side
midline
```

Examples:

```json
{
  "id": "wall_sit",
  "sideModel": "bilateral"
}
```

```json
{
  "id": "single_leg_glute_bridge_hold",
  "sideModel": "left_right"
}
```

For `left_right` exercises, the workout engine may schedule both sides, alternate sides, or use one side during assessment depending on the workout plan.

---

## 10. Side-aware joint modelling

Joint stress is structured by anatomical joint and side role.

Exercise definitions should usually avoid absolute left/right joint ids.

Pain reports use absolute side information.

The workout planner maps exercise-side roles to absolute sides when building or executing a session.

Examples:

```text
Pain report:
jointId = knee
side = left

Exercise definition:
jointStress.knee.bilateral = 0.6

Planner expansion:
knee.bilateral -> left knee + right knee
```

For unilateral exercises:

```text
Pain report:
jointId = hip
side = right

Exercise definition:
jointStress.hip.workingSide = 0.4

If scheduled on right side:
workingSide -> right hip

If scheduled on left side:
workingSide -> left hip
```

Supported joint-load roles:

```text
bilateral
midline
workingSide
supportSide
oppositeSide
left
right
```

In exercise definitions, prefer:

```text
bilateral
midline
workingSide
supportSide
oppositeSide
```

Use absolute `left` or `right` only if an exercise is genuinely asymmetric by design.

---

## 11. Prescription model

### Isometric prescription

```json
{
  "sets": 3,
  "holdSeconds": 30,
  "restSeconds": 45,
  "intensityTarget": 7
}
```

### Dynamic prescription

```json
{
  "sets": 2,
  "targetReps": 10,
  "restSeconds": 60,
  "tempo": {
    "eccentricSeconds": 4,
    "bottomPauseSeconds": 0,
    "concentricCue": "smooth",
    "topPauseSeconds": 0
  },
  "intensityTarget": 8
}
```

### Mobility prescription

```json
{
  "sets": 2,
  "durationSeconds": 45,
  "restSeconds": 30,
  "intensityTarget": 7
}
```

Prescription defines the default starting point.

The planner and progression engine may adjust it.

---

## 12. Muscle metadata

Muscles are not the primary planning unit, but they are important for:

- visualization
- fatigue estimation
- exercise browsing
- balancing a session
- explaining the plan

Each exercise defines:

```text
primary
secondary
stabilizers
```

Each muscle entry includes a relative load from 0 to 1.

Example:

```json
{
  "muscles": {
    "primary": [
      { "id": "quadriceps", "load": 0.8 }
    ],
    "secondary": [
      { "id": "glutes", "load": 0.5 },
      { "id": "hamstrings", "load": 0.3 }
    ],
    "stabilizers": [
      { "id": "core", "load": 0.3 }
    ]
  }
}
```

Muscle load is approximate.

It is not a biomechanical measurement.

---

## 13. Joint stress metadata

Joint stress estimates relative joint demand from 0 to 1.

```text
0 = negligible
1 = high relative stress
```

Joint stress is not inherently bad.

It is dose information.

Example, bilateral lower body:

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

Example, bilateral upper body:

```json
{
  "jointStress": {
    "shoulder": {
      "bilateral": 0.7
    },
    "wrist": {
      "bilateral": 0.6
    },
    "neck": {
      "midline": 0.3
    }
  }
}
```

Example, unilateral lower body:

```json
{
  "sideModel": "left_right",
  "jointStress": {
    "hip": {
      "workingSide": 0.4
    },
    "knee": {
      "workingSide": 0.2
    },
    "lumbar_spine": {
      "midline": 0.2
    }
  }
}
```

---

## 14. Fatigue cost

Each exercise includes estimated fatigue cost.

Example:

```json
{
  "fatigueCost": {
    "global": 6,
    "local": {
      "quadriceps": 8,
      "glutes": 5,
      "core": 3
    },
    "joint": {
      "knee": {
        "bilateral": 6
      },
      "lumbar_spine": {
        "midline": 2
      }
    }
  }
}
```

For unilateral exercises:

```json
{
  "fatigueCost": {
    "global": 5,
    "local": {
      "glutes": 8,
      "hamstrings": 4
    },
    "joint": {
      "hip": {
        "workingSide": 4
      },
      "knee": {
        "workingSide": 2
      }
    }
  }
}
```

The planner uses this to avoid excessive same-day or week-level load.

Fatigue cost is approximate and relative.

It is not a medical or physiological measurement.

---

## 15. Progression ladder

A progression ladder defines possible steps.

Example:

```json
[
  {
    "id": "high_stance_20s",
    "labelKey": "progression.horse_stance.high_20s",
    "holdSeconds": 20,
    "difficulty": 1
  },
  {
    "id": "high_stance_30s",
    "labelKey": "progression.horse_stance.high_30s",
    "holdSeconds": 30,
    "difficulty": 2
  },
  {
    "id": "standard_30s",
    "labelKey": "progression.horse_stance.standard_30s",
    "holdSeconds": 30,
    "difficulty": 3
  }
]
```

Progression can change:

- duration
- sets
- range of motion
- leverage
- load
- base of support
- unilateral demand
- tempo
- rest duration
- external weight

Progression is not automatic.

It must consider:

- recent pain
- stability
- perceived difficulty
- confidence
- recovery
- consistency
- prior success at current level

---

## 16. Regressions

Regressions are easier alternatives within the same exercise or family.

Examples:

- wall sit at 120° instead of 90°
- hollow hold with knees bent
- bear hold with knees higher
- pike hold with less shoulder angle
- single-leg balance with hand support

Regressions preserve training while reducing risk.

Example:

```json
{
  "regressions": [
    "wall_sit_120deg",
    "chair_sit_hold"
  ]
}
```

Regression ids may refer to exercise ids or progression ids depending on implementation.

The Exercise Library Engine should validate references where practical.

---

## 17. Alternatives

Alternatives are different exercises satisfying similar movement patterns.

Example:

```json
{
  "alternatives": [
    "horse_stance",
    "supported_split_squat_hold",
    "chair_squat_hold"
  ]
}
```

Alternatives are used when:

- equipment is unavailable
- pain is triggered
- user preference excludes an exercise
- fatigue overlap is excessive
- variety is needed
- the planner cannot fit the target session duration

Alternatives are not necessarily easier.

They are substitutes.

---

## 18. Contraindication flags

Exercises may include conservative warning metadata.

Example:

```json
{
  "contraindications": [
    "acute_knee_pain",
    "acute_shoulder_pain",
    "requires_floor_access"
  ]
}
```

Contraindications are planner hints, not diagnoses.

Valens must avoid medical certainty.

If an exercise is contraindicated by recent user feedback, the planner should regress, substitute or skip it.

---

## 19. Cues

Cues are localization keys.

Example:

```json
{
  "cues": {
    "setup": [
      "exercise.wall_sit.cue.setup.1",
      "exercise.wall_sit.cue.setup.2"
    ],
    "during": [
      "exercise.wall_sit.cue.during.1"
    ],
    "stopIf": [
      "exercise.wall_sit.cue.stop_if.1"
    ]
  }
}
```

Cue categories:

```text
setup
during
stopIf
```

The UI and audio engine may use different subsets of cues.

Stop cues should be short and clear.

---

## 20. Assets

Each exercise may reference:

```text
illustration
illustrationStart
illustrationHold
illustrationMistake
muscleMapFront
muscleMapBack
jointMapFront
jointMapBack
```

The MVP may ship placeholders.

Missing non-critical assets must not crash the app.

---

## 21. Example exercise: wall sit

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
  "muscles": {
    "primary": [
      { "id": "quadriceps", "load": 0.8 }
    ],
    "secondary": [
      { "id": "glutes", "load": 0.5 },
      { "id": "hamstrings", "load": 0.3 }
    ],
    "stabilizers": [
      { "id": "core", "load": 0.3 }
    ]
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

## 22. Validation expectations

Exercise definitions must validate:

- required fields
- valid exercise type
- valid movement pattern id
- valid muscle ids
- valid anatomical joint ids
- valid side-role keys
- valid equipment values
- valid cue structure
- valid progression structure

The bundled pack should fail tests if invalid.

Imported packs should fail gracefully without crashing the app.

---

## 23. Summary

The exercise model must be rich enough that the planner can make intelligent decisions without special-case code.

Every exercise is a data-defined implementation of a movement pattern.

The planner should reason from:

- movement pattern
- joint stress
- fatigue cost
- pain history
- progression state
- equipment availability
- user preferences

It should not reason from exercise names.
