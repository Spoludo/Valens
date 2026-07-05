# 21 – Coding Guidelines

**Project:** Valens  
**Version:** 0.1  
**Status:** Draft

## Purpose

This document defines coding expectations for Valens.

The goal is a codebase that is easy for humans and AI assistants to extend safely.

## General principles

Prefer:

- clear names
- small files
- immutable data
- pure functions
- explicit dependencies
- simple algorithms
- tests for business logic

Avoid:

- hidden global state
- untested planner logic
- hardcoded exercise names
- business rules inside UI
- premature abstraction
- clever code

## Kotlin style

Use idiomatic Kotlin.

Prefer:

```kotlin
data class WorkoutPlan(...)
```

over mutable Java-style models.

Prefer sealed interfaces for closed hierarchies:

```kotlin
sealed interface WorkoutPhase
```

Use nullable types intentionally.

Avoid `!!` except in tests or impossible states with clear explanation.

## Naming

Names should describe domain meaning.

Good:

```kotlin
MovementPatternId
JointLoadBudget
PainReport
WorkoutPhase
ProgressionDecision
```

Bad:

```kotlin
DataManager
Utils
Thing
Processor
```

## Package boundaries

Domain packages must not import Android UI classes.

The planner must remain pure Kotlin.

Composables must not contain planning logic.

## Domain models

Domain models should be stable and expressive.

Prefer value classes for IDs when useful:

```kotlin
@JvmInline
value class ExerciseId(val value: String)
```

Avoid passing raw strings everywhere if it creates confusion.

## Error handling

Use explicit result types for recoverable domain errors.

Invalid exercise JSON should produce validation errors, not app crashes.

## Constants

Avoid magic numbers.

Use named configuration:

```kotlin
object PainThresholds {
    const val CAUTION = 3
    const val REGRESS = 5
}
```

## Tests

Every planner rule should have a test.

Test names should read like behavior:

```kotlin
fun planner_reducesKneeLoad_afterRecentKneePain()
```

## Compose

Composable functions should be mostly stateless.

Use state hoisting.

Preview important screens where practical.

Avoid business logic in composables.

## Room

Entities are persistence models, not domain models.

Map between Room entities and domain models where necessary.

## JSON

Exercise JSON loading must validate required fields and references.

Never assume a pack is valid.

## Documentation

Use KDoc for:

- public domain types
- planner algorithms
- state machines
- non-obvious heuristics

Avoid comments that merely repeat code.

## Summary

Valens code should feel calm, explicit and trustworthy, like the product itself.
