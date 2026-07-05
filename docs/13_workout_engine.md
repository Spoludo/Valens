# 13 – Workout Engine

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

The Workout Engine executes a workout plan.

It does not decide what the user should train.

---

## 2. Responsibilities

- phase sequencing
- timers
- rest periods
- transitions
- pause/resume
- skip exercise
- audio cues
- feedback collection
- result recording

---

## 3. Workout phases

Possible phases:

```text
prepare
countdown
work
rest
transition
feedback
paused
completed
aborted
```

---

## 4. Exercise modes

### Isometric

Timer-based hold.

### Dynamic

Rep and tempo-based.

### Mobility

Duration or slow-rep based.

### Balance

Timer with side switching.

### Assessment

Special protocol.

---

## 5. Upcoming exercise preview

During rest, the UI must show:

- next exercise
- next mode
- equipment needed
- key cue

This supports readiness.

---

## 6. Audio integration

Workout Engine emits events:

```text
phase_started
countdown_tick
halfway
last_five_seconds
rest_started
next_exercise
feedback_needed
```

Audio Engine consumes events.

---

## 7. Pause behavior

Pause freezes:

- timer
- audio cues
- phase state

Resume continues from same phase.

---

## 8. Interruptions

If app backgrounded, workout should continue when feasible or pause safely depending on Android constraints.

MVP may keep screen awake during workout.

---

## 9. Feedback collection

After each exercise or at end, collect:

- difficulty
- fatigue
- pain
- stability
- confidence

MVP can allow “ask at end” mode.

---

## 10. Acceptance criteria

Workout Engine must:

- execute isometric sessions
- support rest periods
- show next exercise
- support pause/resume
- collect feedback
- save results
- be testable with fake clock

---

## 11. Summary

The Workout Engine is a reliable guided session runner. It should feel calm, predictable and safe.
