# 10 – Capacity Model

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines Valens' capacity model.

Capacity is the durable ability to perform meaningful movement tasks.

---

## 2. Core capacities

Initial capacities:

```text
core_endurance
lower_body_strength
hip_extension_strength
upper_push_strength
upper_pull_strength
shoulder_stability
hip_mobility
shoulder_mobility
balance
ankle_resilience
joint_tolerance
functional_endurance
```

---

## 3. Capacity vs exercise performance

Exercise performance is a signal.

Capacity is an interpretation.

Example:

```text
wall_sit 60s + no knee pain + stable technique
    contributes to lower_body_strength and knee_tolerance
```

But wall sit alone does not define lower-body capacity.

---

## 4. Capacity inputs

Capacity estimates may use:

- exercise completion
- hold duration
- reps
- RPE
- pain
- stability
- assessments
- consistency
- external activity
- recent regression

---

## 5. Capacity states

Each capacity may be:

```text
unknown
building
stable
maintaining
declining
limited_by_pain
```

---

## 6. Display philosophy

Avoid false precision.

Prefer:

```text
Improving
Stable
Needs attention
Limited by pain
```

over:

```text
73.4%
```

If scores are used, make them explanatory and secondary.

---

## 7. Reserve capacity

Valens is inspired by reserve capacity:

> The more capacity a user maintains above daily-life requirements, the more resilient they are to aging, illness and inactivity.

The product should help the user maintain a buffer.

---

## 8. Capacity targets

Targets are personal.

Example:

```text
single_leg_balance: 45s each side
wall_sit: 60s pain-free
hollow_hold: 75s
sit_to_stand: stable/improving
```

Targets should be configurable and conservative.

---

## 9. Maintenance threshold

Once target capacity is reached, planner may switch to maintenance.

Maintenance prevents endless progression pressure.

---

## 10. Capacity and planner

Planner uses capacity to:

- prioritize weak areas
- maintain strong areas
- avoid overtraining limited areas
- select assessments
- generate weekly balance

---

## 11. Acceptance criteria

Capacity engine must:

- compute from observations
- not require cloud
- explain status
- avoid overprecision
- support movement patterns
- support maintenance mode

---

## 12. Summary

Valens should help users understand what they can do, not merely what they did.

Capacity is the bridge between workout logs and healthy aging outcomes.
