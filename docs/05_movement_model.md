# 05 – Movement Model

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines the movement ontology used by Valens.

Valens is not organized primarily around muscles or exercise names. It is organized around human movement and functional capacity.

---

## 2. Hierarchy

```text
Movement Domain
    └── Movement Category
            └── Movement Pattern
                    └── Exercise Family
                            └── Exercise
                                    └── Variation
```

---

## 3. Movement domains

Initial domains:

```text
strength
mobility
balance
endurance
recovery
assessment
```

---

## 4. Movement categories

Examples:

```text
upper_body_push
upper_body_pull
lower_body_squat
lower_body_hinge
core_stability
carry
balance
hip_mobility
shoulder_mobility
spinal_mobility
ankle_mobility
```

---

## 5. Movement patterns

A movement pattern is the level the planner should reason about.

Examples:

### Upper body push

- horizontal_push
- vertical_push
- scapular_push

### Upper body pull

- horizontal_pull
- vertical_pull
- scapular_retraction
- grip_hang

### Lower body

- squat
- split_squat
- hip_hinge
- hip_extension
- calf_raise
- tibialis_raise

### Core

- anti_extension
- anti_rotation
- lateral_stability
- posterior_chain_bracing

### Mobility

- shoulder_flexion
- shoulder_extension
- shoulder_external_rotation
- shoulder_internal_rotation
- hip_external_rotation
- hip_internal_rotation
- hip_extension
- hip_abduction
- thoracic_rotation

### Balance

- bilateral_static
- single_leg_static
- single_leg_dynamic
- eyes_closed_balance

---

## 6. Why movement patterns matter

If the planner needs squat capacity, it can choose among:

- wall sit
- horse stance
- supported split squat
- chair squat
- goblet squat

depending on:

- knee pain
- equipment
- ability
- fatigue
- progression state
- weekly coverage

This is superior to fixed exercise scheduling.

---

## 7. Movement pattern metadata

Each movement pattern should define:

```text
id
name
domain
category
primaryCapacities
typicalMuscles
typicalJoints
functionalPurpose
recommendedWeeklyFrequency
minimumEffectiveDose
recoveryCost
```

---

## 8. Capacity mapping

Movement patterns contribute to capacities.

Example:

```text
wall_sit
    movementPattern: squat
    capacity: lower_body_strength, knee_tolerance

single_leg_balance
    movementPattern: single_leg_static
    capacity: balance, ankle_stability

hollow_hold
    movementPattern: anti_extension
    capacity: core_endurance, spinal_control
```

---

## 9. External activities

External activities also map to movement patterns.

Basketball shooting may contribute to:

```text
ankle_elasticity
light_squat_exposure
coordination
balance
shoulder_repetition
functional_endurance
```

But it contributes little to:

```text
upper_pull
posterior_chain_strength
loaded_mobility
grip_strength
```

This helps the planner fill gaps rather than duplicate stress.

---

## 10. MVP movement patterns

Minimum viable pattern set:

```text
anti_extension_core
squat
hip_extension
horizontal_push
vertical_push
upper_body_stability
calf_raise
single_leg_balance
shoulder_mobility
hip_mobility
```

Add later:

```text
horizontal_pull
vertical_pull
carry
rotation
tibialis_raise
thoracic_rotation
floor_rise
```

---

## 11. Anti-patterns

The planner should avoid reasoning directly from muscles alone.

Example:

Bad:

```text
Train quadriceps today.
```

Better:

```text
Train squat pattern with knee-safe dose.
```

Muscle maps are useful for visualization, not sufficient for planning.

---

## 12. Summary

Valens models training as the development of movement patterns and capacities.

Exercises are replaceable tools.

Movement capacity is the durable concept.
