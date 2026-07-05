# 15 – UI / UX

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Valens UI should feel calm, legible and trustworthy.

It should support daily use by people who may be older, tired, exercising on the floor, or using a tablet.

---

## 2. Design principles

- large readable typography
- low cognitive load
- calm colors
- clear hierarchy
- minimal taps during workout
- accessible controls
- no shame language
- tablet friendly

---

## 3. Core navigation

Initial screens:

```text
Home
Today's Session
Workout
Exercise Library
Progress
Settings
```

Later:

```text
Assessments
Exercise Packs
Export
Developer Diagnostics
```

---

## 4. Home screen

Should show:

- readiness check
- today's recommendation
- recent pain warning if relevant
- weekly movement coverage
- external activity quick log

Example:

```text
Today
20 min adaptive session

Focus:
Core, hip extension, shoulder mobility

Adjusted because:
Basketball logged yesterday
```

---

## 5. Workout screen

Must prioritize:

- current exercise name
- large timer
- set count
- phase
- next exercise
- pause button

Avoid clutter.

---

## 6. Feedback screen

Default quick mode:

```text
Difficulty: Easy / Moderate / Hard
Pain: None / Slight / Moderate / Strong
Stable? Yes / No
```

Detailed mode available after pain.

---

## 7. Exercise library

Browse by:

- movement pattern
- capacity
- body region
- equipment
- difficulty

Not only alphabetically.

---

## 8. Graphics

Use SVG body maps and exercise illustrations.

Graphics should be educational but not required for workout completion.

---

## 9. Accessibility

Requirements:

- TalkBack labels
- scalable text
- high contrast
- reduced motion
- large tap targets
- landscape support
- tablet support

---

## 10. Tone

Valens should sound like:

- calm
- supportive
- intelligent
- precise
- non-judgmental

Avoid:

- aggressive fitness language
- guilt
- hype
- medical overconfidence

---

## 11. Acceptance criteria

MVP UI must:

- start a workout quickly
- show clear timer
- show upcoming exercise
- collect feedback easily
- display basic progress
- support dark mode

---

## 12. Summary

The UI should help the user feel guided, not managed.
