# Valens

**Valens** is an open-source Android app for adaptive strength, mobility, balance, joint resilience and healthy aging.

It is designed as a privacy-first, offline-first movement coach.

> Train for life, not records.

## Status

Early development.

Current focus:

* project specification
* Android architecture
* repository structure
* exercise metadata model
* JSON schema validation
* workout engine foundation

No production Android app exists yet.

## Project goals

Valens aims to help users preserve long-term physical capacity through:

* short sustainable sessions
* adaptive planning
* joint-aware progression
* mobility and balance work
* pain and recovery feedback
* data-driven exercise packs
* local-first private storage

It is not intended to be:

* a calorie tracker
* a bodybuilding app
* a social fitness app
* a generic interval timer
* a cloud-first coaching platform

## Repository structure

```text
Valens/
├── app/                         # Android application module
├── docs/                        # Specifications and architecture docs
│   └── ADR/                     # Architecture Decision Records
├── schemas/                     # JSON Schema files for exercise-pack validation
├── exercise-packs/              # JSON/YAML exercise definitions
│   └── bundled/
│       └── isometric-foundations/
├── assets/                      # Source assets
│   ├── muscles/
│   ├── joints/
│   ├── illustrations/
│   └── sounds/
├── external/                    # External tools / git submodules
├── .github/
│   ├── workflows/
│   └── ISSUE_TEMPLATE/
├── CLAUDE.md                    # Persistent Claude Code instructions
├── IMPLEMENTATION_PROMPT_FOR_CLAUDE.md
└── README.md
```

## Documentation

Start here:

```text
docs/00_vision.md
docs/01_product_philosophy.md
docs/02_personas.md
docs/03_architecture.md
docs/04_database.md
docs/05_movement_model.md
docs/06_exercise_model.md
docs/07_planner_algorithm.md
docs/09_pain_and_recovery.md
docs/16_json_schema.md
docs/20_technology_stack.md
docs/21_coding_guidelines.md
docs/22_contributing.md
```

## Implementation principle

The documentation is the source of truth.

Implementation should follow the specs rather than inventing architecture during coding.

## Core architecture idea

Valens is built around movement patterns, not hardcoded exercises.

Example:

```text
Need: squat pattern
Possible exercises:
- wall sit
- horse stance
- supported split squat
- chair squat
```

The planner selects exercises using metadata such as:

* movement pattern
* joint stress
* fatigue cost
* equipment
* progression level
* recent pain
* user preference

## JSON schemas and exercise packs

The schema files live in:

```text
schemas/
```

The first bundled exercise pack lives in:

```text
exercise-packs/bundled/isometric-foundations/
```

The bundled pack currently contains:

* 8 movement patterns
* 11 exercises
* muscle reference data
* joint reference data
* English translations

The pack can be checked with:

```bash
python3 scripts/validate-exercise-pack.py
```

Expected result:

```text
OK: 11 exercises, 8 movement patterns
```

## Android stack

Planned stack:

* Kotlin
* Jetpack Compose
* Material 3
* Room
* DataStore
* Kotlin Serialization
* Coroutines / Flow
* Navigation Compose
* Gradle Kotlin DSL

Exact dependency versions should be managed through:

```text
gradle/libs.versions.toml
```

## External tools

Possible submodule:

```bash
git submodule add https://github.com/MertenD/musclegroup-image-generator external/musclegroup-image-generator
```

This can help generate muscle activation graphics.

## Roadmap

### Phase 1 — Foundation

* Android project skeleton
* Gradle setup
* CI
* domain models
* JSON exercise loader
* Room entities

### Phase 2 — Workout loop

* workout engine
* timer
* audio cues
* feedback collection
* basic Compose workout UI

### Phase 3 — Adaptive planner MVP

* movement-pattern planning
* pain-aware selection
* progression rules
* external activity input

### Phase 4 — Insight layer

* statistics
* capacity trends
* pain trends
* functional assessments

## License

MIT
