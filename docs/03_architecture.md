# 03 – Architecture

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines the high-level architecture of Valens.

Valens should be built as a set of testable domain engines with a thin Android UI layer.

---

## 2. Architectural priorities

1. Offline-first
2. Privacy-first
3. Testable planner logic
4. Data-driven exercise library
5. Movement-pattern-centered domain model
6. Local storage
7. Extensible exercise packs
8. Accessibility
9. Maintainability

---

## 3. Recommended technology stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Room
- Kotlinx Serialization
- Coroutines / Flow
- Hilt or lightweight manual dependency injection
- WorkManager for reminders only
- JUnit for domain tests
- Compose UI tests

---

## 4. Layer model

```text
Presentation Layer
    Compose screens, UI state, navigation

Application Layer
    Use cases, ViewModels, session orchestration

Domain Layer
    Planner, progression, pain model, capacity, movement model

Data Layer
    Room, JSON assets, preferences, repositories
```

Dependency direction must flow inward. Domain must not depend on Android UI.

---

## 5. Engine-based architecture

Valens should be organized around domain engines rather than screens.

### Planner Engine

Generates workout plans.

Inputs:

- movement coverage
- user history
- pain reports
- external activities
- available time
- preferences
- exercise library

Outputs:

- workout plan
- rationale
- expected fatigue
- expected joint load

### Workout Engine

Executes a workout plan.

Responsibilities:

- timers
- phases
- pauses
- audio cues
- transitions
- feedback collection

The Workout Engine does not decide what should be trained.

### Exercise Library Engine

The Exercise Library Engine loads exercise packs from `exercise-packs/bundled/`, validates them against `schemas/`, and exposes typed domain models to the planner.

Responsibilities:

- exercise packs
- movement patterns
- assets
- progressions
- alternatives
- translations

### Capacity Engine

Computes capacity estimates from observations.

Examples:

- core endurance
- squat pattern capacity
- balance capacity
- shoulder mobility
- knee tolerance

### Pain and Recovery Engine

Interprets pain reports and recovery status.

Does not diagnose. Provides planner signals.

### Statistics Engine

Computes trends.

Examples:

- weekly coverage
- pain evolution
- progression
- consistency
- capacity changes

### Assessment Engine

Runs periodic functional assessments.

---

## 6. Movement-pattern-centered planning

The planner should not request a specific exercise first.

Incorrect:

```text
Today: Horse Stance
```

Correct:

```text
Need: Squat pattern
Constraints: low knee aggravation, no equipment, 20 min session
Candidate selection: Wall Sit partial angle, Horse Stance high stance, Supported Split Squat
```

Exercises are implementations of movement patterns.

---

## 7. Data flow

```text
JSON Schemas
        │
        ▼
Bundled Exercise Packs
        │
        ▼
Exercise Library Engine
        │
        ├── validates schema version
        ├── validates references
        ├── loads translations
        └── exposes typed exercise metadata
        │
        ├──────────────┐
        ▼              │
Planner Engine         │
        │              │
        ▼              │
Workout Plan           │
        │              │
        ▼              │
Workout Engine         │
        │              │
        ▼              │
Workout Results ───────┘
        │
        ▼
Room Database
        │
        ▼
Statistics / Capacity / Pain Engines
```

---

## 8. Module proposal

Initial Gradle modules may be simple.

Recommended eventual structure:

```text
:app
:core:model
:core:database
:core:exercise
:core:planner
:core:workout
:core:analytics
:core:ui
```

For MVP, a single app module with clear packages is acceptable if boundaries are respected.

---

## 9. Package proposal

```text
com.spoludo.valens
    data
        db
        repository
        json
    domain
        model
        planner
        progression
        pain
        capacity
        assessment
    workout
        engine
        timer
        audio
    ui
        home
        workout
        exercises
        stats
        settings
```

---

## 10. Repositories

Repositories should hide storage details.

Examples:

- `ExerciseRepository`
- `WorkoutRepository`
- `PainRepository`
- `ExternalActivityRepository`
- `AssessmentRepository`
- `PreferencesRepository`

The domain layer should consume repository interfaces.

---

## 11. Determinism

Planner output should be deterministic for identical inputs.

This enables:

- unit testing
- debugging
- reproducibility
- planner snapshots

If randomness is used for variety, it must use an explicit seed.

---

## 12. Error handling

The app should fail gracefully.

Examples:

- Missing illustration: show placeholder.
- Missing audio cue: continue silently.
- Invalid exercise definition: skip exercise pack and report validation error.
- Corrupted history row: exclude row from computed analytics and log locally.
- Planner cannot fill 20 minutes: produce best effort routine and explain.

---

## 13. Accessibility as architecture

Accessibility is not UI polish. It influences architecture.

The workout engine must support:

- visual cues
- audio cues
- haptics
- large timers
- screen-reader labels
- reduced animation mode

---

## 14. Future compatibility

The architecture should not prevent:

- Wear OS companion
- export/import
- Health Connect integration
- heart-rate logging
- camera form analysis
- downloadable exercise packs
- AI-assisted plan explanation

None of these belong in the MVP, but the architecture should not block them.

---

## 15. Architectural risks

### Risk: Overengineering

Mitigation: build MVP with clean package boundaries before splitting modules.

### Risk: Exercise library too complex

Mitigation: define strict schema and validation early.

### Risk: Planner becomes untestable

Mitigation: planner must be pure Kotlin domain logic with fixture-based tests.

### Risk: Android UI drives business logic

Mitigation: all planning, progression and capacity logic lives outside composables.

---

## 16. Summary

Valens is architected around one key idea:

> The planner reasons about human movement, not exercise names.

This distinction makes Valens extensible, testable and capable of evolving from a personal app into a general open-source healthy-aging platform.
