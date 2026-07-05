# 18 – Testing

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Testing ensures planner safety, persistence reliability and workout execution correctness.

---

## 2. Test priorities

Highest priority:

1. Planner
2. Progression
3. Pain rules
4. Workout engine state machine
5. Database persistence
6. JSON validation

UI tests are important but secondary to domain correctness.

---

## 3. Planner tests

Test cases:

- generates 20-minute session
- excludes unavailable equipment
- avoids disabled exercises
- reduces knee load after knee pain
- accounts for basketball external activity
- alternates overlapping muscle groups
- deterministic output

---

## 4. Pain model tests

- pain 0–2 allows progression
- pain 3–4 blocks progression
- pain 5+ triggers regression/substitution
- sharp pain stops exercise
- sensitive joint lowers load budget

---

## 5. Workout engine tests

Use fake clock.

Test:

- countdown
- work phase
- rest phase
- pause/resume
- skip
- completion
- feedback collection

---

## 6. Database tests

- insert workout session
- insert exercise results
- insert pain reports
- query weekly history
- migration tests

---

## 7. JSON validation tests

- valid pack loads
- missing required field fails gracefully
- invalid movement reference fails validation
- missing asset uses placeholder

---

## 8. UI tests

Compose tests:

- home starts workout
- workout timer displays
- pause button works
- feedback form saves
- settings toggle audio

---

## 9. Acceptance criteria

MVP should include unit tests before advanced UI polish.

Minimum coverage target is less important than testing critical rules.

---

## 10. Summary

Valens testing should focus on user safety, planner predictability and long-term maintainability.
