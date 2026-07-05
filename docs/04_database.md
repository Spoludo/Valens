# 04 – Database Design

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines the local persistence model for Valens.

The database is designed to store observations over time, not final judgments.

> Store what happened. Compute what it means.

This keeps Valens flexible as planner and analytics algorithms improve.

---

## 2. Persistence strategy

Valens uses Room for local persistence.

Exercise definitions are stored as JSON assets and are not duplicated into Room unless caching becomes necessary.

Room stores:

- user profile
- preferences
- workout history
- exercise results
- feedback
- pain reports
- external activities
- assessments
- progression state
- optional planner snapshots

---

## 3. Entity overview

```text
UserProfile
UserPreferences

WorkoutSession
    └── WorkoutExercise
            └── ExerciseFeedback

PainReport
ExternalActivity
Assessment
ProgressionState
PlannerSnapshot
```

---

## 4. UserProfile

Stores stable user information.

Fields:

```text
id: Long
birthYear: Int?
sex: String?
heightCm: Float?
weightKg: Float?
dominantSide: String?
experienceLevel: String
createdAt: Instant
updatedAt: Instant
```

Notes:

- Avoid storing unnecessary sensitive details.
- Health data should remain optional.
- Weight should be updateable over time later if needed.

---

## 5. UserPreferences

Stores app behavior.

Fields:

```text
id: Long
preferredWorkoutMinutes: Int
plannerAggressiveness: String
voiceEnabled: Boolean
tickEnabled: Boolean
countdownEnabled: Boolean
hapticsEnabled: Boolean
theme: String
measurementSystem: String
autoProgressionEnabled: Boolean
largeTextMode: Boolean
createdAt: Instant
updatedAt: Instant
```

Recommended defaults:

```text
preferredWorkoutMinutes = 20
plannerAggressiveness = conservative
voiceEnabled = true
tickEnabled = true
countdownEnabled = true
```

---

## 6. WorkoutSession

Represents one workout attempt.

Fields:

```text
sessionId: UUID
startedAt: Instant
endedAt: Instant?
plannedDurationSeconds: Int
actualDurationSeconds: Int?
completed: Boolean
completionReason: String?
plannerVersion: String
exercisePackVersion: String
overallRpe: Int?
overallEnergy: Int?
overallEnjoyment: Int?
overallRecovery: Int?
notes: String?
createdAt: Instant
```

Completion reasons:

```text
completed
user_stopped
pain
interrupted
technical_error
```

---

## 7. WorkoutExercise

Represents one planned exercise inside a workout.

Fields:

```text
id: UUID
sessionId: UUID
exerciseId: String
movementPatternId: String
orderIndex: Int
exerciseType: String
plannedSets: Int
completedSets: Int
plannedHoldSeconds: Int?
actualHoldSeconds: Int?
plannedReps: Int?
actualReps: Int?
plannedRestSeconds: Int
actualRestSeconds: Int?
plannedTempoJson: String?
difficultyLevel: Int?
progressionStepId: String?
wasSubstitution: Boolean
substitutedForExerciseId: String?
createdAt: Instant
```

Important:

- Store both `exerciseId` and `movementPatternId`.
- Exercise names are not stored because they can be localized or renamed.
- Historical identity remains stable through IDs.

---

## 8. ExerciseFeedback

Feedback collected after a workout exercise.

Fields:

```text
id: UUID
workoutExerciseId: UUID
perceivedDifficulty: Int
muscleFatigue: Int
jointPainMax: Int
stability: Int
confidence: Int
techniqueQuality: Int?
enjoyment: Int?
comment: String?
createdAt: Instant
```

Recommended scale: 1–5 for normal UI.

Pain detail is stored separately if localized pain is reported.

---

## 9. PainReport

Pain may be reported during or outside a workout.

Fields:

```text
id: UUID
dateTime: Instant
source: String
workoutExerciseId: UUID?
jointId: String
side: String?
intensity: Int
painType: String
duration: String?
trigger: String?
notes: String?
createdAt: Instant
```

Sources:

```text
exercise_feedback
daily_checkin
manual
assessment
```

Pain types:

```text
stiffness
dull_discomfort
sharp
burning
instability
swelling
unknown
```

Intensity scale: 0–10 internally.

---

## 10. ExternalActivity

Represents physical activity outside Valens.

Fields:

```text
id: UUID
startedAt: Instant
activityType: String
durationMinutes: Int
estimatedIntensity: Int
kneeLoadEstimate: Int?
shoulderLoadEstimate: Int?
notes: String?
createdAt: Instant
```

Examples:

```text
basketball_shooting
walking
hiking
cycling
swimming
gardening
manual_work
```

External activities influence planner load but are not treated as Valens workouts.

---

## 11. Assessment

Functional tests and periodic benchmarks.

Fields:

```text
id: UUID
dateTime: Instant
assessmentType: String
value: Float
unit: String
side: String?
confidence: Int?
notes: String?
createdAt: Instant
```

Examples:

```text
single_leg_balance_seconds
sit_to_stand_30s_reps
wall_sit_seconds
floor_rise_quality
grip_strength_kg
```

---

## 12. ProgressionState

Current state per exercise or movement pattern.

Fields:

```text
id: UUID
scopeType: String
scopeId: String
currentLevel: Int
recommendedHoldSeconds: Int?
recommendedSets: Int?
recommendedReps: Int?
lastProgressedAt: Instant?
confidence: Float
status: String
updatedAt: Instant
```

Scope types:

```text
exercise
movement_pattern
capacity
```

Status:

```text
building
maintaining
regressing
paused
```

This is mutable current state, not historical truth.

---

## 13. PlannerSnapshot

Optional debugging entity.

Fields:

```text
id: UUID
sessionId: UUID
plannerVersion: String
inputJson: String
constraintsJson: String
candidateScoresJson: String
outputJson: String
createdAt: Instant
```

Purpose:

- debugging
- reproducibility
- planner tests from real sessions

Can be disabled to reduce storage.

---

## 14. Static reference data

Not stored in Room initially:

```text
ExerciseDefinition
MovementPattern
MuscleGroup
Joint
ExercisePack
ProgressionLadder
AssetManifest
```

These are loaded from JSON assets.

---

## 15. Indexes

Recommended indexes:

```text
WorkoutSession.startedAt
WorkoutExercise.sessionId
WorkoutExercise.exerciseId
WorkoutExercise.movementPatternId
ExerciseFeedback.workoutExerciseId
PainReport.dateTime
PainReport.jointId
PainReport.workoutExerciseId
ExternalActivity.startedAt
Assessment.dateTime
Assessment.assessmentType
ProgressionState.scopeType + scopeId
```

---

## 16. Deletion and editing

Workout history should be mostly immutable.

If the user deletes a session:

- mark deleted if analytics reproducibility matters
- or hard-delete for privacy

Privacy should win. Provide hard-delete.

If the user edits an external activity, update it normally.

---

## 17. Export

Future export formats:

- JSON
- CSV
- SQLite backup

Export should include schema version and app version.

---

## 18. Migration philosophy

Migrations should preserve raw history.

Computed fields should be recomputable.

Avoid storing derived scores unless performance requires caching.

---

## 19. Summary

The database is a movement journal.

It records:

- what was planned
- what was done
- how it felt
- where pain occurred
- what else the user did
- how capacities changed over time

It should remain boring, stable and truthful. Intelligence belongs in engines built on top of it.
