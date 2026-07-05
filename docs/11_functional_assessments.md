# 11 – Functional Assessments

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Functional assessments provide periodic reference points for long-term capacity.

They should be short, safe and optional.

---

## 2. Assessment philosophy

Assessments are not competitions.

They are check-ins.

The goal is to detect trends:

- improving
- stable
- declining
- pain-limited

---

## 3. Initial assessments

### Single-leg balance

Measures balance and ankle/hip stability.

Record:

```text
side
seconds
support used?
eyes open/closed
confidence
```

### Sit-to-stand 30s

Measures lower-body function.

Record:

```text
reps
chair height
hands used?
pain
```

### Wall sit

Measures lower-body isometric endurance and knee tolerance.

Record:

```text
angle
seconds
pain
stability
```

### Floor rise quality

Measures practical independence.

Record:

```text
hands used
knees used
time optional
confidence
```

### Grip strength

Optional if user has dynamometer.

Record:

```text
side
kg
```

---

## 4. Assessment frequency

Suggested:

```text
monthly
```

Do not overuse assessments. Daily training should not become testing.

---

## 5. Safety

Assessments must have stop criteria.

Stop if:

- sharp pain
- dizziness
- instability
- unusual symptoms

Use conservative language and disclaimers.

---

## 6. Planner integration

Assessment results influence capacity estimates, not daily exercise directly.

Example:

```text
single-leg balance declining
    planner increases balance exposure gradually
```

---

## 7. UI requirements

Assessment screen should show:

- purpose
- instructions
- timer/counter
- safety cues
- previous result
- trend

---

## 8. Acceptance criteria

MVP assessments:

- single-leg balance
- sit-to-stand
- wall sit

Each must save results and feed statistics.

---

## 9. Summary

Assessments help Valens validate whether training supports real-world capacity.
