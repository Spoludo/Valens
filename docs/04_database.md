# 04 – Database Design

**Project:** Valens

**Version:** 0.1

**Status:** Draft

**Last updated:** 2026-07-05

---

# Purpose

This document specifies the persistent data model used by Valens.

The database is designed around one principle:

> Store observations, not conclusions.

Whenever possible, raw user data is stored.

Statistics, scores and recommendations are computed dynamically.

This allows planner improvements without invalidating existing user history.

---

# Design Principles

## Offline First

The complete database resides locally.

No internet connection is required.

---

## Immutable History

Workout history should never be modified.

Corrections are stored as new observations.

Historical sessions remain reproducible.

---

## Separate Static and Dynamic Data

Three categories exist.

### Static Reference Data

Examples

- exercise definitions
- muscles
- joints
- movement patterns

Normally shipped as JSON assets.

Not stored in Room.

---

### User Data

Stored in Room.

Examples

- workouts
- progression
- pain
- assessments
- preferences

---

### Computed Data

Never persisted unless performance requires caching.

Examples

- weekly statistics
- movement balance
- capacity scores
- planner recommendations

---

# Entity Overview

```
Exercise (JSON)

        │

        ▼

WorkoutPlan

        │

        ▼

WorkoutSession

        │

        ▼

ExerciseResult

        │

        ▼

Statistics
```

---

# Database Entities

## UserProfile

One row.

Stores relatively stable information.

Fields

```
id

birthYear

sex

heightCm

weightKg

dominantSide

activityLevel

experienceLevel

createdAt

updatedAt
```

Future additions

- body fat %
- resting heart rate
- wearable identifiers

---

## UserPreferences

Stores application settings.

Fields

```
preferredWorkoutMinutes

voiceEnabled

voiceVolume

tickEnabled

countdownEnabled

theme

measurementSystem

plannerAggressiveness

autoProgressionEnabled

preferredTrainingDays
```

---

## WorkoutSession

Represents one completed workout.

Fields

```
sessionId

date

plannedDuration

actualDuration

completed

plannerVersion

exercisePackVersion

notes

overallRPE

overallEnjoyment

overallEnergy

overallRecovery

createdAt
```

A workout remains valid even if the exercise library changes later.

---

## WorkoutExercise

Each exercise performed during a session.

Fields

```
id

sessionId

exerciseId

order

plannedHold

actualHold

plannedSets

completedSets

plannedRest

actualRest

difficultyLevel

progressionStep
```

---

## ExerciseFeedback

Collected immediately after each exercise.

This is one of the core datasets.

Fields

```
id

workoutExerciseId

perceivedDifficulty

muscleFatigue

jointPain

stability

confidence

techniqueQuality

enjoyment

comment
```

All scales default to 1–5.

---

# Why Separate Feedback?

The workout stores facts.

Feedback stores perception.

This distinction allows future planner improvements.

---

## PainReport

Pain is independent from workouts.

A user may report pain any day.

Fields

```
id

date

joint

side

intensity

type

duration

trigger

notes
```

Types

```
None

Stiffness

Discomfort

Sharp

Inflammation

Unknown
```

---

## ExternalActivity

Represents activities outside Valens.

Examples

Basketball

Walking

Cycling

Swimming

Hiking

Gardening

Manual work

Fields

```
id

date

activityType

duration

estimatedIntensity

notes
```

The planner considers these activities.

---

## Assessment

Functional tests.

Fields

```
id

date

assessmentType

score

unit

notes
```

Examples

```
Wall Sit

Single Leg Balance

Sit To Stand

Grip Strength

Floor Rise

Shoulder Mobility
```

---

## ProgressionState

Current capability estimate for each exercise.

Fields

```
exerciseId

currentLevel

recommendedHold

recommendedSets

lastProgression

confidence

```

This is not history.

It is the planner's current understanding.

---

## PlannerSnapshot

Optional.

Stores why a workout was generated.

Useful for debugging.

Fields

```
plannerVersion

capacitySnapshot

constraints

generatedWorkout

```

Can be disabled.

---

# JSON Reference Data

Exercises are not stored in Room.

Instead they are loaded from JSON.

Example

```
exerciseId

name

movementPattern

difficulty

equipment

holdType

muscles

jointStress

fatigueCost

progressions

alternatives

voiceCues

illustrations
```

This allows exercise packs.

---

# Relationships

```
WorkoutSession

    │

    ├───────────────┐

    │               │

WorkoutExercise     ExternalActivity

    │

    ▼

ExerciseFeedback
```

---

# Why Exercise IDs?

Only IDs are stored.

Names remain inside the exercise library.

Benefits

- localization

- exercise packs

- renaming

- easier updates

---

# Time Series Philosophy

Valens is fundamentally a time-series application.

Everything important changes over time.

Examples

```
pain

strength

mobility

confidence

recovery

consistency
```

The database should preserve this evolution.

---

# Data Retention

Nothing is automatically deleted.

Users own their data.

Future export formats

```
CSV

JSON

SQLite backup

Health Connect
```

---

# Planner Queries

Typical planner questions

"What muscles have received the least work this week?"

"What exercises consistently produce knee pain?"

"What is today's recovery estimate?"

"Which progression has stalled?"

The schema should make these efficient.

---

# Indexes

Indexes should exist for

```
WorkoutSession.date

WorkoutExercise.exerciseId

PainReport.date

PainReport.joint

ExternalActivity.date

Assessment.date
```

These are expected to be the most common queries.

---

# Capacity Is Not Stored

Valens deliberately does **not** store

```
Overall Fitness = 73
```

Instead it stores observations.

Capacity is recalculated whenever needed.

Advantages

- planner improvements apply to old data

- no migration issues

- transparent calculations

---

# Future Entities

Potential additions

```
HeartRateObservation

SleepObservation

NutritionObservation

BloodPressureObservation

WearableSync

ExercisePack

CoachNotes

Goals

Achievements
```

These should remain optional modules.

---

# Versioning

Every workout records

```
plannerVersion

exercisePackVersion

databaseVersion
```

This allows historical workouts to remain reproducible after future planner improvements.

---

# Database Philosophy

The database should answer one question:

> **What has actually happened?**

It should not attempt to predict the future.

Prediction belongs to the Adaptive Planner.

Analytics belong to the Statistics Engine.

The database remains an objective historical record.

---

# Summary

Valens treats the database as a scientific journal rather than a scoreboard.

It stores observations of movement, recovery, pain, progression and consistency.

This provides a robust foundation for intelligent planning while remaining flexible enough to support future exercise packs, planner algorithms and wearable integrations without requiring fundamental schema redesign.