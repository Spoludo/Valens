# 09 – Pain and Recovery

**Project:** Valens  
**Version:** 0.4  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-06

---

## 1. Purpose

This document defines how Valens models pain and recovery.

Valens does not diagnose or treat medical conditions.

It uses pain and recovery reports to adapt training.

---

## 2. Pain categories

Valens distinguishes:

```text
muscle effort
muscle fatigue
joint discomfort
sharp pain
instability
stiffness
soreness
```

This prevents confusing productive muscular work with problematic joint irritation.

---

## 3. Pain localization

Pain reports use anatomical joint id plus absolute side.

Example:

```text
jointId = knee
side = left
```

Another example:

```text
jointId = shoulder
side = right
```

Midline structures use:

```text
jointId = lumbar_spine
side = midline
```

Initial anatomical joints:

```text
neck
shoulder
elbow
wrist
lumbar_spine
hip
knee
ankle
```

Supported pain sides:

```text
left
right
midline
bilateral
unknown
```

Exercise definitions do not usually encode absolute left/right sides.

Instead, they define side roles such as:

```text
bilateral
midline
workingSide
supportSide
oppositeSide
```

The planner maps exercise-side roles to absolute pain sides when scoring exercises.

---

## 4. Pain reporting UX

After exercise:

1. Any joint discomfort?
2. Where?
3. Which side?
4. Intensity 0–10
5. Type
6. Did it change during the hold or movement?

The default flow must remain fast.

Detailed input should appear only if pain is reported.

---

## 5. Pain rules

Suggested rules:

```text
0: no pain
1–2: monitor
3–4: caution, no progression
5–6: regress/substitute
7+: stop and suggest rest/professional advice
sharp pain: stop immediately
swelling/instability: avoid joint loading and suggest professional advice
```

---

## 6. Chronic sensitivity

A joint and side can be marked sensitive.

Example:

```text
jointId = knee
side = left
status = sensitive
```

This permanently lowers progression aggressiveness for movements that load the left knee.

Sensitive does not mean unusable.

It means dose must be managed.

---

## 7. Pain matching against exercise metadata

Pain reports are absolute.

Exercise definitions are relative.

Example:

```text
Pain:
jointId = knee
side = left

Exercise:
jointStress.knee.bilateral = 0.6

Planner:
bilateral maps to left + right, so this exercise receives a left-knee pain penalty.
```

For unilateral exercises:

```text
Pain:
jointId = hip
side = right

Exercise:
sideModel = left_right
jointStress.hip.workingSide = 0.4

If scheduled on right side:
penalty applies.

If scheduled on left side:
right-hip penalty may not apply, unless supportSide or oppositeSide also loads the right hip.
```

---

## 8. Pain trend detection

Valens should detect patterns.

Example:

```text
Horse Stance
jointId = knee
side = left
pain: 1, 2, 2, 3, 4, 4
```

Planner response:

- reduce depth
- reduce duration
- alternate with hip extension
- consider wall sit at safer angle
- show user trend

---

## 9. Recovery inputs

Simple daily recovery check:

```text
Energy: 1–5
Sleep quality: 1–5
Soreness: 1–5
Stress: 1–5
Motivation: 1–5
```

MVP may only ask:

```text
How recovered do you feel today?
Poor / Okay / Good
```

---

## 10. Recovery effect on planner

Poor recovery:

- reduce progressions
- reduce intensity
- select mobility/balance
- avoid high fatigue patterns
- preserve habit with lighter session

Good recovery:

- allow normal session
- allow 1–2 progressions
- include harder variation if pain-free

---

## 11. External activity effect

Basketball, hiking or long walking may increase lower-limb load.

The planner should interpret activity based on type, duration and intensity.

Example:

```text
basketball_shooting 40 min:
knee load +3
ankle load +4
shoulder repetition +2
cardio +3
```

---

## 12. Safety language

Valens must avoid medical claims.

Suggested language:

```text
Your left knee discomfort has increased recently. Today's plan reduces knee-dominant loading and emphasizes hip/core work. Consider consulting a professional if pain persists or worsens.
```

Avoid:

```text
This will fix your knee.
```

---

## 13. Acceptance criteria

Pain model must:

- distinguish muscle effort from joint pain
- localize pain by anatomical joint and side
- reduce progression after pain
- influence planner selection
- map exercise side roles to absolute pain sides
- detect worsening trends
- support sensitive joints
- provide non-alarming guidance

---

## 14. Summary

Pain is a planning signal.

Valens should keep users moving when possible while avoiding repeated aggravation and encouraging appropriate caution.
