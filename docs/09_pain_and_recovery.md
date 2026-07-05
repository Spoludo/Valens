# 09 – Pain and Recovery

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

This document defines how Valens models pain and recovery.

Valens does not diagnose or treat medical conditions. It uses pain and recovery reports to adapt training.

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

Pain should be reportable by joint and side.

Initial joints:

```text
neck
left_shoulder
right_shoulder
left_elbow
right_elbow
left_wrist
right_wrist
lumbar_spine
left_hip
right_hip
left_knee
right_knee
left_ankle
right_ankle
```

---

## 4. Pain reporting UX

After exercise:

1. Any joint discomfort?
2. Where?
3. Intensity 0–10
4. Type
5. Did it change during the hold/movement?

The default flow must remain fast. Detailed input should appear only if pain is reported.

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

A joint can be marked sensitive.

Example:

```text
left_knee: sensitive
```

This permanently lowers progression aggressiveness for movements with high knee load.

Sensitive does not mean unusable. It means dose must be managed.

---

## 7. Pain trend detection

Valens should detect patterns.

Example:

```text
Horse Stance left knee pain:
1, 2, 2, 3, 4, 4
```

Planner response:

- reduce depth
- reduce duration
- alternate with hip extension
- consider wall sit at safer angle
- show user trend

---

## 8. Recovery inputs

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

## 9. Recovery effect on planner

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

## 10. External activity effect

Basketball, hiking or long walking may increase lower-limb load.

The planner should interpret activity based on type, duration and intensity.

Example:

```text
basketball_shooting 40 min:
    kneeLoad +3
    ankleLoad +4
    shoulderRepetition +2
    cardio +3
```

---

## 11. Safety language

Valens must avoid medical claims.

Suggested language:

```text
Your knee discomfort has increased recently. Today's plan reduces knee-dominant loading and emphasizes hip/core work. Consider consulting a professional if pain persists or worsens.
```

Avoid:

```text
This will fix your knee.
```

---

## 12. Acceptance criteria

Pain model must:

- distinguish muscle effort from joint pain
- localize pain
- reduce progression after pain
- influence planner selection
- detect worsening trends
- support sensitive joints
- provide non-alarming guidance

---

## 13. Summary

Pain is a planning signal.

Valens should keep users moving when possible while avoiding repeated aggravation and encouraging appropriate caution.
