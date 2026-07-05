# IMPLEMENTATION_PROMPT_FOR_CLAUDE.md

# Valens — Implementation Guide for Claude

## Mission

You are implementing **Valens**, an open-source Android application focused on **healthy aging**, **movement quality**, **joint resilience**, and **long-term physical capacity**.

Your goal is **not** to build another workout timer.

Your goal is to build a maintainable software platform capable of evolving over many years into an intelligent movement coach.

Every implementation decision should optimize for:

- maintainability
- readability
- extensibility
- correctness
- testability
- accessibility
- privacy
- offline operation

---

## Read before coding

Always read the following documents before implementing features:

1. `docs/00_vision.md`
2. `docs/01_product_philosophy.md`
3. `docs/02_personas.md`
4. `docs/03_architecture.md`
5. `docs/04_database.md`
6. `docs/05_movement_model.md`
7. `docs/06_exercise_model.md`
8. `docs/07_planner_algorithm.md`
9. `docs/08_progression.md`
10. `docs/09_pain_and_recovery.md`
11. `docs/20_technology_stack.md`
12. `docs/21_coding_guidelines.md`
13. `docs/22_contributing.md`

The documentation is the source of truth.

If code conflicts with documentation, update the code unless explicitly instructed to amend the specification.

---

## Development philosophy

Think like a senior software architect.

Do not implement only the smallest code that happens to work.

Instead:

- design for future extensions
- avoid premature complexity
- minimize technical debt
- favor composition over inheritance
- keep business logic independent from Android

The objective is a codebase that remains pleasant after five years.

---

## Android technology stack

Unless explicitly changed, use the stack defined in `docs/20_technology_stack.md`.

Core choices:

- Kotlin
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Flow
- Room
- DataStore Preferences
- Kotlin Serialization
- Navigation Compose

Avoid unnecessary third-party libraries.

Prefer official Android libraries.

---

## Project architecture

Business logic must remain independent from Android.

Use a layered architecture:

```text
Presentation
↓
Application
↓
Domain
↓
Data
```

The Domain layer must not depend on Android APIs.

The planner must be executable in ordinary JVM unit tests.

---

## Coding principles

Follow these principles consistently:

- SOLID where useful
- small classes
- immutable models
- pure functions when practical
- explicit names
- no magic numbers
- no hardcoded exercises in planner logic

---

## Domain rules

The planner reasons about **Movement Patterns**, not **Exercise Names**.

Correct:

```text
Need: Horizontal Push
Candidate exercises:
- Wall Push
- Push-up Hold
- Incline Push-up
```

Incorrect:

```kotlin
if (exercise.name == "Wall Push") { ... }
```

---

## Database rules

Use Room.

Store:

- observations
- history
- feedback
- pain
- assessments

Do not store derived statistics unless justified by performance.

---

## Exercise library

Exercise definitions are JSON assets.

They must be validated during loading.

Invalid definitions must not crash the application.

---

## Workout engine

The Workout Engine executes plans.

It never decides training.

Responsibilities:

- timers
- pauses
- transitions
- audio events
- feedback collection

---

## Audio engine

Audio should consume semantic events.

Example events:

```text
PhaseStarted
Halfway
Countdown
RestStarted
WorkoutFinished
```

Avoid coupling TTS directly into business logic.

---

## Accessibility

Accessibility is mandatory.

Support:

- TalkBack
- tablets
- landscape
- dark mode
- scalable fonts
- reduced motion

---

## Testing requirements

Every non-trivial feature should include tests.

Priorities:

1. Planner
2. Progression
3. Pain Engine
4. Workout Engine
5. Room
6. JSON validation

UI tests are secondary.

---

## Git workflow

Implement one coherent feature per commit.

Suggested commit format:

```text
feat(planner): implement movement pattern selection
fix(timer): preserve countdown after pause
refactor(database): simplify Room entities
test(planner): add fatigue scoring tests
```

---

## Definition of done

A feature is complete only if:

- code compiles
- tests pass
- documentation remains accurate
- no obvious duplication exists
- architecture remains consistent
- feature is accessible
- feature is reviewed for maintainability

---

## Initial implementation roadmap

### Phase 1 — Project foundation

- Create Android project
- Configure Gradle Kotlin DSL
- Configure CI
- Configure formatting and static analysis
- Configure testing

### Phase 2 — Domain and data foundation

- Domain models
- Exercise JSON loader
- Movement model
- Room entities

### Phase 3 — Workout loop

- Workout Engine
- Timer Engine
- Audio Engine
- Basic workout UI

### Phase 4 — Intelligence MVP

- Adaptive Planner MVP
- Feedback collection
- Progression engine

### Phase 5 — Insight layer

- Statistics
- Capacity model
- Assessments
- Exercise library

---

## Working style

When implementing a feature:

1. Explain the proposed design briefly.
2. Identify trade-offs.
3. Implement incrementally.
4. Add tests.
5. Update documentation if needed.

When uncertain, prefer the solution that keeps the architecture simpler and more extensible.
