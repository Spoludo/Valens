# 08 – Progression

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines how Valens progresses training.

Progression is adaptive, exercise-specific and pain-aware.

---

## 2. Progression philosophy

Progression does not mean every metric always increases.

Valens supports:

```text
building
maintaining
recovering
deloading
```

Healthy aging often requires maintaining strong capacities while selectively improving weak ones.

---

## 3. Signals

Progression uses multiple signals:

```text
perceivedDifficulty
muscleFatigue
jointPain
stability
confidence
techniqueQuality
recovery
recentTrend
```

Time alone is insufficient.

---

## 4. Readiness decision

Basic rule:

```text
If effort moderate, stability good, pain low:
    progress

If effort high but pain low:
    maintain

If pain moderate or instability high:
    regress or substitute
```

---

## 5. Pain thresholds

Suggested defaults:

```text
joint pain 0–2: acceptable
joint pain 3–4: caution, do not progress
joint pain 5+: regress/substitute and flag
sharp pain: stop exercise and mark unsafe today
```

These are planning heuristics, not medical advice.

---

## 6. Independent progression

Each exercise progresses independently.

Example:

```text
hollow_hold: 60s stable
wall_sit: 35s limited by knee
single_leg_bridge: 70s easy
pike_hold: 20s difficult
```

The planner must not force uniform progression.

---

## 7. Progression dimensions

Exercises may progress through:

- duration
- sets
- range of motion
- leverage
- load
- unilateral demand
- balance challenge
- tempo
- reduced support
- shorter rest

Examples:

- hollow hold: tuck → one leg extended → both legs extended
- wall sit: 120° → 100° → 90° → longer hold
- balance: eyes open → head turns → eyes closed
- wall push: higher force → longer effort → harder angle

---

## 8. Maximum progression rate

MVP rule:

```text
Do not progress more than 10–15% per week for a given exercise.
```

Also:

```text
Progress at most 2 exercises per session.
```

---

## 9. Deloading

Automatic deload triggers:

- repeated pain increase
- poor recovery
- high fatigue
- multiple missed sessions
- user reports instability
- external activity spike

Deload options:

- reduce duration
- reduce sets
- choose easier variation
- replace with mobility
- lower session target duration

---

## 10. Maintenance mode

Once a capacity reaches a target, Valens can maintain it.

Example:

```text
hollow hold target = 75s
if stable for 4 weeks:
    reduce frequency
    keep occasional exposure
    allocate time elsewhere
```

---

## 11. ProgressionState update

After each workout:

```text
Read feedback
Update pain trend
Update completion consistency
Adjust recommended level
Set status: building/maintaining/recovering
```

ProgressionState is a current estimate, not raw history.

---

## 12. User override

The user may override:

- too easy
- too hard
- prefer not to progress
- avoid this exercise
- repeat same level

Autonomy improves adherence.

---

## 13. Acceptance criteria

The progression engine must:

- never progress after moderate joint pain
- support exercise-specific ladders
- support maintenance
- support regression
- update independently per exercise
- expose rationale

---

## 14. Summary

Valens progression is not a race.

It is a long-term calibration process that gradually increases capacity while protecting consistency and joint tolerance.
