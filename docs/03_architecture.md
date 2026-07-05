# 03 – Architecture

**Project:** Valens

**Version:** 0.1

**Status:** Draft

**Last updated:** 2026-07-05

---

# Purpose

This document describes the high-level software architecture of Valens.

The goal is not to define implementation details, but to establish stable architectural principles that allow the application to evolve over many years while remaining maintainable, testable and extensible.

---

# Architectural Principles

Valens follows the following principles.

## Offline First

The application must function completely offline.

Internet connectivity should never be required to:

- create workouts
- complete workouts
- track progress
- view statistics
- browse exercises

Online features may exist in the future but must always remain optional.

---

## Local Data Ownership

User data belongs entirely to the user.

All data is stored locally.

Future cloud synchronization should be:

- optional
- encrypted
- provider-independent

No user account is required.

---

## Modular Design

Every major responsibility is isolated into its own module.

Modules should communicate through well-defined interfaces.

No module should know implementation details of another module.

---

## Data-Driven

Business logic must not depend on hardcoded exercise names.

Instead, exercises are defined by metadata.

The planner operates entirely on exercise properties.

This allows new exercises to be added without modifying planner code.

---

## Testability

Business logic must remain independent of Android UI.

The planner should be executable inside ordinary JVM unit tests.

The majority of application logic should not require Android instrumentation tests.

---

# Technology Stack

Target platform:

Android

Language:

Kotlin

UI:

Jetpack Compose

Architecture:

MVVM

Dependency Injection:

Hilt

Persistence:

Room

Serialization:

Kotlinx Serialization

Background work:

WorkManager

Navigation:

Navigation Compose

Testing:

JUnit

MockK

Compose Testing

---

# Layered Architecture

```
+-----------------------------------+
|          Presentation             |
|      Compose / ViewModels         |
+-----------------------------------+
|         Application Layer         |
|     Planner / Workout Engine      |
+-----------------------------------+
|          Domain Layer             |
| Exercises / Capacity / Pain       |
| Progression / Statistics          |
+-----------------------------------+
|           Data Layer              |
| Room / JSON Assets / Preferences  |
+-----------------------------------+
```

---

# Main Modules

The application is divided into several logical modules.

---

## Presentation

Responsibilities

- screens
- navigation
- animations
- accessibility
- voice feedback
- dialogs

Contains no business logic.

---

## Workout Engine

Responsible for:

- executing workouts
- timers
- exercise sequencing
- audio cues
- pause management
- user interaction during sessions

The Workout Engine never decides **what** to train.

It only executes a workout produced by the Planner.

---

## Planner

The most important module.

Responsibilities

- choose exercises
- determine duration
- progression
- regressions
- substitutions
- weekly balance
- respect time budget
- respect joint stress budget
- respect recovery

The planner never interacts directly with the UI.

---

## Exercise Library

Provides access to all exercise definitions.

Responsibilities

- load exercise metadata
- load illustrations
- load SVG muscle maps
- expose progression ladders
- expose coaching cues

Exercises are immutable definitions.

User progress is stored elsewhere.

---

## Statistics

Responsible for:

- workout history
- capacity evolution
- pain trends
- movement coverage
- weekly summaries

Statistics never influence planning directly.

Instead they expose data to the Planner.

---

## Capacity Engine

Responsible for estimating long-term physical capacities.

Examples

- core endurance

- balance

- mobility

- lower body strength

- shoulder stability

Capacity scores are estimated rather than directly measured.

---

## Assessment Engine

Responsible for periodic functional tests.

Examples

- single-leg balance

- sit-to-stand

- floor rise

- grip strength

Assessments provide reference points for long-term progress.

---

# Data Sources

The application uses several independent data sources.

## Exercise Library

Static.

Bundled with the application.

May later support downloadable packs.

---

## User Database

Stores:

- workouts
- progression
- assessments
- preferences
- pain history

---

## Assets

Contains

- SVG body maps
- exercise illustrations
- sounds
- speech prompts

---

## Settings

Stores

- preferred workout duration
- voice options
- accessibility
- units
- planner preferences

---

# Planner Data Flow

```
User History
       │
Pain History
       │
Recovery
       │
External Activities
       │
Exercise Library
       │
       ▼
  Adaptive Planner
       │
       ▼
Workout Plan
       │
       ▼
Workout Engine
       │
       ▼
Workout Results
       │
       ▼
Statistics
```

The planner is deterministic.

Given the same inputs, it should produce the same workout.

---

# Domain Model

Major domain objects.

```
Exercise

ExerciseVariant

WorkoutPlan

WorkoutExercise

WorkoutSession

WorkoutResult

Assessment

PainReport

Capacity

MovementPattern

Joint

MuscleGroup

UserPreferences

PlannerConstraints
```

Each object has a single responsibility.

---

# Exercise Independence

Exercises should never be identified by code logic.

Incorrect:

```
if exercise == "Horse Stance"
```

Correct:

```
if movementPattern == Squat
and
jointStress.knee < threshold
```

The planner reasons about metadata.

Not names.

---

# Extensibility

Future exercise packs should contain only data.

Example

```
Yoga Pack

Tai Chi Pack

Basketball Pack

Resistance Bands Pack

Rehabilitation Pack
```

No planner modification should be required.

---

# Separation of Concerns

| Module | Knows about |
|----------|------------|
| UI | ViewModels |
| ViewModels | Use Cases |
| Planner | Domain Objects |
| Exercise Library | Exercise Definitions |
| Statistics | Workout History |
| Database | Storage only |

No module should bypass another layer.

---

# Dependency Direction

Dependencies always point inward.

```
Compose

↓

ViewModel

↓

Planner

↓

Repositories

↓

Room
```

Lower layers never depend on higher layers.

---

# Error Handling

The application should fail gracefully.

Examples

Missing illustration

→ use placeholder

Missing sound

→ continue silently

Corrupted workout history

→ recover if possible

Missing exercise pack

→ disable affected exercises

The application should never crash because an optional asset is unavailable.

---

# Performance Goals

Application startup

< 2 seconds

Workout screen transition

< 200 ms

Planner generation

< 100 ms

Statistics loading

< 500 ms

Animations

60 fps

These are targets rather than strict requirements.

---

# Accessibility

Architecture must support:

- TalkBack

- large fonts

- landscape mode

- tablets

- dark mode

- colour-blind friendly graphics

Accessibility is considered a first-class feature.

---

# Future Architecture

The architecture should support future additions including:

- Wear OS

- Desktop companion

- Camera posture analysis

- AI coaching

- Bluetooth sensors

- Heart-rate integration

without requiring major refactoring.

---

# Architectural Decision Records

Major architectural decisions should be documented as ADRs.

Examples:

```
ADR-001

Offline First

ADR-002

Data-driven Exercise Library

ADR-003

Planner Independence

ADR-004

Room Database

ADR-005

Jetpack Compose
```

This preserves project history and simplifies onboarding for new contributors.

---

# Summary

The architecture of Valens is intentionally centered around one concept:

**The Adaptive Planner.**

Everything else exists to either:

- provide information to the planner,
- execute the planner's decisions,
- or help the user understand the results.

This keeps the application modular, extensible and capable of supporting new exercise types, rehabilitation protocols and community exercise packs without fundamental architectural changes.