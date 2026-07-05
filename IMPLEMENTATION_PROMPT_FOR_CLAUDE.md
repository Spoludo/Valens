# Implementation Prompt for Claude

You are implementing Valens, an open-source Android app for healthy aging.

Before writing code, read:

1. `docs/00_vision.md`
2. `docs/01_product_philosophy.md`
3. `docs/03_architecture.md`
4. `docs/04_database.md`
5. `docs/05_movement_model.md`
6. `docs/06_exercise_model.md`
7. `docs/07_planner_algorithm.md`

Rules:

- Do not hardcode exercise names in planner logic.
- Planner reasons about movement patterns.
- Keep domain logic independent of Android UI.
- Use Room for local user data.
- Exercise definitions are JSON assets.
- MVP must prioritize a reliable workout loop before advanced analytics.
- Every implementation task must include tests for planner or domain logic when applicable.

First implementation milestone:

1. Create Android Kotlin project.
2. Add Compose navigation.
3. Implement domain models.
4. Implement bundled JSON exercise loader.
5. Implement basic workout engine for isometric exercises.
6. Implement Room entities for workouts and feedback.
7. Add unit tests for planner skeleton and workout engine phase transitions.
