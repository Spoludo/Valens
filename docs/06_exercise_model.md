# 06 – Exercise Model

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines how exercises are represented in Valens.

Exercises are data, not code.

The planner must be able to add, remove or substitute exercises based on metadata.

---

## 2. Exercise definition

An exercise definition describes a trainable movement.

Required fields:

```text
id
nameKey
descriptionKey
type
movementPatternId
exerciseFamilyId
difficulty
equipment
defaultPrescription
muscles
jointStress
fatigueCost
progression
regressions
alternatives
cues
assets
```

---

## 3. Exercise types

Supported types:

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

- wall sit: isometric
- split squat: dynamic
- weighted butterfly: mobility_isometric
- figure four lift: mobility_dynamic
- single-leg stance: balance

---

## 4. Prescription model

### Isometric prescription

```json
{
  "sets": 3,
  "holdSeconds": 30,
  "restSeconds": 45
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
  }
}
```

### Mobility prescription

```json
{
  "sets": 2,
  "durationSeconds": 45,
  "intensityTarget": 7
}
```

---

## 5. Muscle metadata

Muscles are not the primary planning unit but remain important for visualization and fatigue estimation.

Each exercise defines:

```text
primaryMuscles
secondaryMuscles
stabilizers
```

Each muscle entry may include intensity:

```json
{
  "muscles": {
    "primary": [
      { "id": "quadriceps", "load": 0.8 }
    ],
    "secondary": [],
    "stabilizers": []
  }
}
```

---

## 6. Joint stress metadata

Joint stress is essential for Valens.

Example:

```json
{
  "left_knee": 0.8,
  "right_knee": 0.8,
  "left_hip": 0.5,
  "right_hip": 0.5,
  "lumbar_spine": 0.2  "lumbar_spine": 0.2,
  "shoulder": 0.0,
  "wrist": 0.0
}
```

0 means negligible. 1 means high relative stress.

Joint stress is not inherently bad. It is dose information.

---

## 7. Fatigue cost

Each exercise includes estimated fatigue cost:

```json
{
  "global": 6,
  "local": {
    "quadriceps": 8,
    "glutes": 5,
    "core": 3
  },
  "joint": {
    "knee": 7
  }
}
```

The planner uses this to avoid excessive same-day load.

---

## 8. Progression ladder

A progression ladder defines steps.

Example:

```json
[
  { "id": "high_stance_20s", "holdSeconds": 20, "difficulty": 1 },
  { "id": "high_stance_30s", "holdSeconds": 30, "difficulty": 2 },
  { "id": "standard_30s", "holdSeconds": 30, "difficulty": 3 },
  { "id": "standard_45s", "holdSeconds": 45, "difficulty": 4 }
]
```

Progression can change:

- duration
- range of motion
- leverage
- load
- base of support
- unilateral bias
- tempo
- external weight

---

## 9. Regressions

Regressions are easier alternatives within the same exercise.

Examples:

- wall sit at 120° instead of 90°
- hollow hold with knees bent
- bear hold with knees higher
- pike hold with less shoulder angle

Regressions preserve training while reducing risk.

---

## 10. Alternatives

Alternatives are different exercises satisfying similar movement patterns.

Example:

```text
horse_stance alternatives:
    wall_sit
    supported_split_squat_hold
    chair_squat_hold
```

Alternatives are used when:

- equipment unavailable
- pain triggered
- user preference
- excessive fatigue overlap
- variety needed

---

## 11. Contraindication flags

Exercises may include warning metadata:

```text
avoid_if_acute_knee_pain
avoid_if_uncontrolled_hypertension
avoid_if_wrist_pain
requires_overhead_tolerance
requires_floor_access
```

Valens must avoid medical certainty. Warnings are conservative planning hints.

---

## 12. Assets

Each exercise may reference:

```text
illustrationStart
illustrationHold
illustrationMistake
muscleMapFront
muscleMapBack
audioCueSet
videoUrlOptional
```

MVP can ship placeholders.

---

## 13. Example exercise: wall sit

```json
{
  "id": "wall_sit",
  "type": "isometric",
  "movementPatternId": "squat",
  "exerciseFamilyId": "wall_sit_family",
  "difficulty": 2,
  "equipment": ["wall"],
  "muscles": {
    "primary": ["quadriceps"],
    "secondary": ["glutes", "hamstrings"],
    "stabilizers": ["core"]
  },
  "jointStress": {
    "left_knee": 0.8,
    "right_knee": 0.8,
    "left_hip": 0.5,
    "right_hip": 0.5,
    "lumbar_spine": 0.2
  },
  "defaultPrescription": {
    "sets": 3,
    "holdSeconds": 30,
    "restSeconds": 60
  }
}
```

---

## 14. Summary

The exercise model must be rich enough that the planner can make intelligent decisions without special-case code.

Every exercise is a data-defined implementation of a movement pattern.
