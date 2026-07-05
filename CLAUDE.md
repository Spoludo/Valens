# CLAUDE.md

# Valens — Repository Instructions

## Project identity

Valens is an open-source Android app for healthy aging, adaptive movement training, joint resilience, mobility, balance and long-term physical capacity.

It is not a bodybuilding app, calorie tracker, social fitness app, or generic interval timer.

The product philosophy is:

> Train for life, not records.

## Source of truth

Before implementing features, read the relevant files in `docs/`.

Highest priority documents:

1. `docs/00_vision.md`
2. `docs/01_product_philosophy.md`
3. `docs/02_personas.md`
4. `docs/03_architecture.md`
5. `docs/04_database.md`
6. `docs/05_movement_model.md`
7. `docs/06_exercise_model.md`
8. `docs/07_planner_algorithm.md`
9. `docs/09_pain_and_recovery.md`
10. `docs/20_technology_stack.md`
11. `docs/21_coding_guidelines.md`
12. `docs/22_contributing.md`

If implementation and documentation conflict, ask whether to update the code or the specification.

## Repository structure

Expected repository layout:

```text
Valens/
├── app/                         # Android app module
├── docs/                        # Product and technical specifications
│   └── ADR/                     # Architecture decision records
├── schemas/                     # JSON Schema files for exercise-pack validation
├── exercise-packs/              # Data-driven exercise definitions
│   └── bundled/
│       └── isometric-foundations/
├── assets/                      # Shared source assets
│   ├── muscles/
│   ├── joints/
│   ├── illustrations/
│   └── sounds/
├── external/                    # Git submodules / external generators
├── .github/
│   ├── workflows/
│   └── ISSUE_TEMPLATE/
├── CLAUDE.md
├── IMPLEMENTATION_PROMPT_FOR_CLAUDE.md
└── README.md
```

Do not create extra top-level folders unless justified.

## Android package structure

Inside the Android app, prefer this package structure:

```text
com.spoludo.valens
├── data
│   ├── db
│   ├── json
│   └── repository
├── domain
│   ├── model
│   ├── planner
│   ├── progression
│   ├── pain
│   ├── capacity
│   └── assessment
├── workout
│   ├── engine
│   ├── timer
│   └── audio
└── ui
    ├── home
    ├── workout
    ├── exercises
    ├── stats
    └── settings
```

Domain code must not depend on Android UI.

## Core domain rule

The planner reasons about **movement patterns**, not exercise names.

Correct:

```kotlin
movementPattern.id == "squat"
```

Incorrect:

```kotlin
exercise.name == "Horse Stance"
```

Exercise definitions come from JSON metadata.

## Planner rules

The planner must consider:

* target duration
* movement coverage
* recent workouts
* external activities
* pain reports
* sensitive joints
* equipment availability
* fatigue overlap
* progression readiness

Planner output must include a rationale.

The planner must be deterministic for the same inputs.

## Workout engine rules

The Workout Engine executes a plan. It does not decide what to train.

It handles:

* countdown
* work phase
* rest phase
* transition
* pause/resume
* skip
* audio events
* feedback collection

Use a fake clock in tests.

## Pain model rules

Separate muscle effort from joint pain.

Pain 0–2: allow normal training
Pain 3–4: caution, no progression
Pain 5+: regress or substitute
Sharp pain: stop the exercise

These are planning heuristics, not medical advice.

## Testing expectations

Add tests for:

* planner selection
* pain rules
* progression decisions
* workout engine state transitions
* JSON validation
* Room persistence

Domain tests are more important than UI tests.

## Coding style

Prefer:

* immutable data classes
* pure functions
* small files
* clear names
* explicit constants
* simple algorithms
* testable interfaces

Avoid:

* hardcoded exercise names
* business logic in composables
* global mutable state
* untested planner changes
* premature AI/cloud features

## Git workflow

Work in small coherent commits.

Suggested format:

```text
feat(planner): add movement pattern scoring
test(workout): cover pause and resume phases
refactor(domain): split pain model from progression
docs(spec): clarify exercise metadata
```

## Definition of done

A change is done only when:

* it compiles
* tests pass
* domain logic is tested
* documentation remains accurate
* accessibility is not worsened
* no exercise-specific planner hacks were added
