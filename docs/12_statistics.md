# 12 – Statistics

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Statistics help users understand trends without encouraging obsession.

Valens analytics focus on long-term capacity and joint health.

---

## 2. Dashboard principles

Prefer meaningful trends:

- stable knee pain
- improved balance
- maintained core endurance
- missed upper pulling
- increased lower-body load from basketball

Avoid vanity metrics as primary:

- calories
- rankings
- social comparison

---

## 3. Core views

### Weekly movement coverage

Shows which movement patterns were trained.

### Capacity trends

Shows capacity states:

```text
improving
stable
needs attention
limited by pain
```

### Pain trends

Shows pain by joint over time.

### Consistency

Shows training frequency without guilt.

### Exercise progression

Shows current level and history per exercise.

---

## 4. Muscle maps

Muscle maps visualize recent training load.

Use them as educational feedback, not as the planner's main logic.

---

## 5. Joint stress maps

Valens should also show joint stress.

For sensitive joints, show:

- recent load
- pain trend
- planned load

---

## 6. External activity integration

Stats should show external activities separately from Valens workouts.

Example:

```text
Basketball: 4 sessions, 140 min
Valens: 5 sessions, 96 min
```

---

## 7. Avoiding harmful interpretation

Do not display:

```text
You failed this week.
```

Prefer:

```text
Lower-body strength was maintained. Shoulder mobility received less attention this week.
```

---

## 8. Computed, not stored

Most statistics should be computed from raw data.

Cache only if needed.

---

## 9. Acceptance criteria

MVP stats:

- workout history
- exercise progression
- pain by joint
- weekly movement coverage
- external activity summary

---

## 10. Summary

Statistics are a mirror, not a scoreboard.

They should help the user make better decisions and feel more capable.
