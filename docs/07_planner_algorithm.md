# 07 – Planner Algorithm

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

The Adaptive Planner is the heart of Valens.

It generates an appropriate workout for today based on goals, history, pain, recovery and available time.

---

## 2. Planner objective

Default objective:

> Generate a useful, safe, balanced session close to the user's target duration.

For the reference user, the baseline is approximately 20 minutes.

The planner should not simply maximize work. It should optimize appropriate dose.

---

## 3. Inputs

Planner inputs:

```text
UserPreferences
UserProfile
ExerciseLibrary
RecentWorkoutHistory
RecentPainReports
RecentExternalActivities
ProgressionState
CapacityEstimates
AvailableTime
DisabledExercises
SensitiveJoints
```

---

## 4. Outputs

Planner outputs:

```text
WorkoutPlan
PlannerRationale
ExpectedMovementCoverage
ExpectedMuscleLoad
ExpectedJointLoad
ProgressionRecommendations
SubstitutionReasons
```

The rationale is important for transparency.

---

## 5. Planning stages

### Stage 1: Establish constraints

Examples:

```text
targetDuration = 20 minutes
maxKneeLoad = conservative if knee pain recent
equipment = none
exclude dead_hang if no pull-up bar
```

### Stage 2: Determine movement needs

Based on weekly coverage and capacity gaps.

Example:

```text
Need:
    anti_extension_core
    hip_extension
    horizontal_push
    hip_mobility
    single_leg_balance
```

### Stage 3: Build candidate pool

For each movement pattern, find valid exercises.

Filter by:

- equipment
- disabled status
- pain rules
- skill level
- floor/wall availability
- contraindication flags

### Stage 4: Score candidates

Candidate score may consider:

```text
movementNeed
progressionOpportunity
painPenalty
fatiguePenalty
varietyBonus
preferenceBonus
equipmentPenalty
externalActivityOverlapPenalty
```

### Stage 5: Assemble session

Choose exercises while respecting:

- time budget
- movement diversity
- joint stress budget
- fatigue distribution
- ordering constraints
- rest requirements

### Stage 6: Adjust duration

If session is too short:

- add low-risk mobility
- add balance
- add low-load core
- add rest quality

If too long:

- reduce sets
- reduce lower-priority movements
- move an exercise to tomorrow

### Stage 7: Produce rationale

Example:

```text
Reduced knee-dominant work because basketball was logged today and left knee discomfort was reported yesterday.
Selected single-leg bridge to train hip extension with low knee stress.
```

---

## 6. Scoring model MVP

A simple weighted score is sufficient for MVP.

```text
score =
  movementNeedWeight
+ progressionOpportunity
+ preferenceBonus
+ varietyBonus
- painPenalty
- jointLoadPenalty
- fatigueOverlapPenalty
- equipmentPenalty
```

All weights should be configurable constants.

---

## 7. Joint stress budget

The planner maintains joint load estimates per session and week.

Example:

```text
kneeLoadToday <= 10
shoulderLoadToday <= 10
lumbarLoadToday <= 8
```

If recent pain exists, reduce the budget.

---

## 8. External activity integration

Basketball shooting may add estimated load:

```text
knee: light/moderate
ankle: moderate
shoulder: light repetitive
cardio: moderate
coordination: high
```

After basketball, planner may reduce:

- wall sit volume
- horse stance depth
- split squat intensity

and preserve:

- upper pull
- hip extension
- mobility
- core

---

## 9. Exercise ordering

Avoid consecutive overlap.

Example bad sequence:

```text
horse_stance
wall_sit
split_squat
```

Better:

```text
wall_sit
hollow_hold
wall_push
single_leg_bridge
hip_mobility
balance
```

Ordering rules:

- alternate local fatigue where possible
- place complex exercises before fatigue
- place mobility after strength or at end
- avoid painful patterns late if fatigue worsens form

---

## 10. Progression selection

Planner should progress at most a few items per session.

Avoid increasing every exercise simultaneously.

Suggested rule:

```text
maxProgressionsPerSession = 2
```

This reduces overload and makes causality easier to understand.

---

## 11. Determinism

Given identical inputs, planner returns identical output.

If variety is desired, use deterministic seeded selection.

---

## 12. MVP pseudocode

```kotlin
fun generatePlan(input: PlannerInput): WorkoutPlan {
    val constraints = buildConstraints(input)
    val needs = determineMovementNeeds(input)
    val candidates = needs.flatMap { pattern ->
        exerciseLibrary.findByMovementPattern(pattern)
            .filter { it.satisfies(constraints) }
            .map { scoreCandidate(it, pattern, input, constraints) }
    }

    val selected = assembleSession(
        candidates = candidates,
        targetDuration = constraints.targetDuration,
        jointBudgets = constraints.jointBudgets
    )

    return buildWorkoutPlan(selected, rationale = explain(selected, input))
}
```

---

## 13. Failure cases

If planner cannot produce ideal session:

- produce best effort
- explain missing coverage
- suggest enabling more exercises/equipment
- never crash

Example:

```text
Could not include upper pull because no available no-equipment exercise exists in the current library.
```

---

## 14. Acceptance criteria

MVP planner must:

- generate a session near target duration
- use movement patterns
- avoid disabled exercises
- respect no-equipment mode
- reduce knee load after knee pain
- include rationale
- be covered by unit tests
- be deterministic

---

## 15. Summary

The planner is a conservative coach.

It should help the user train consistently, balance movement coverage, progress gradually and avoid repeatedly irritating sensitive joints.
