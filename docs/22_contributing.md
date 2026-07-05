# 22 – Contributing

**Project:** Valens  
**Version:** 0.1  
**Status:** Draft

## Purpose

This document explains how humans and AI assistants should contribute to Valens.

## Contribution philosophy

Valens is intended to become a long-lived open-source project.

Contributions should improve:

- healthy aging support
- maintainability
- accessibility
- test coverage
- documentation
- exercise library quality

Avoid contributions that add complexity without clear user value.

## Before contributing

Read:

- `README.md`
- `CLAUDE.md`
- `IMPLEMENTATION_PROMPT_FOR_CLAUDE.md`
- relevant files under `docs/`

## Branch naming

Suggested:

```text
feat/workout-engine
feat/exercise-json-loader
fix/pain-feedback-save
docs/movement-model
test/planner-knee-load
```

## Commit format

Suggested conventional style:

```text
feat(planner): add movement pattern scoring
fix(workout): preserve timer state after pause
test(pain): add sharp pain stop rule
docs(stack): clarify dependency policy
```

## Pull request expectations

A PR should include:

- clear summary
- spec references
- tests where applicable
- screenshots for UI changes
- explanation of trade-offs
- migration notes if database changes

## Issue types

Recommended labels:

- `feature`
- `bug`
- `docs`
- `planner`
- `workout-engine`
- `database`
- `exercise-library`
- `ui`
- `accessibility`
- `good-first-issue`

## Exercise pack contributions

Exercise definitions must include:

- movement pattern
- muscles
- joint stress
- fatigue cost
- cues
- regressions
- alternatives
- assets or placeholders
- licensing information

Do not submit copyrighted images or copied video frames.

## AI-assisted contributions

AI-generated code is acceptable if:

- it is reviewed
- it follows the spec
- it includes tests
- it does not introduce unexplained dependencies
- it does not hardcode exercise-specific planner logic

AI assistants should update documentation when architecture changes.

## Code review checklist

Reviewers should ask:

1. Does this follow the product philosophy?
2. Does this preserve planner independence?
3. Is domain logic tested?
4. Are exercise names avoided in planner logic?
5. Is accessibility preserved?
6. Are dependencies justified?
7. Does documentation need updating?

## Security and privacy

Do not add analytics, telemetry, cloud sync or account systems without explicit design discussion and documentation.

## Summary

Valens welcomes contributions that make the app more useful, safer, clearer and easier to maintain.
