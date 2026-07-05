# 02 – Personas

**Project:** Valens

**Version:** 0.1

**Status:** Draft

**Last updated:** 2026-07-05

---

# Purpose

This document defines the primary users of Valens.

The objective is not to create exhaustive marketing personas but rather engineering personas that help developers make better design decisions.

Whenever a feature is proposed, the following question should be asked:

> **Which persona benefits from this feature?**

If the answer is "none", the feature should probably not be implemented.

---

# Primary Persona — The Active Agers

This is the primary audience for Valens.

## Profile

Age:
40–75

Physical condition:
Generally healthy but beginning to notice changes associated with aging.

Goals:

- remain independent
- maintain strength
- improve mobility
- reduce joint pain
- continue enjoying recreational activities
- avoid unnecessary injuries

Typical characteristics

- works full-time or recently retired
- has limited time
- prefers exercising at home
- owns little or no gym equipment
- values long-term health over appearance
- appreciates scientific explanations

Typical concerns

- knee pain
- lower back stiffness
- shoulder mobility
- declining balance
- flexibility
- posture

The application should be primarily optimized for this group.

---

# Reference Persona A — Thierry

This persona represents the initial design target for Valens.

Although inspired by a real user, it is generalized enough to guide product decisions.

## Profile

Age

55

Sex

Male

Height

168 cm

Weight

56–57 kg

Body type

Lean

Diet

Plant-based

Equipment

Minimal

Exercises at home

Location

Apartment

Outdoor basketball court nearby

---

## Current Activity

Exercises almost daily.

Typical activities include:

- recreational basketball shooting
- isometric strength exercises
- mobility work
- bodyweight training
- walking

The user is not sedentary.

Valens should complement existing activities rather than replace them.

---

## Constraints

Previous knee surgery.

Occasional knee discomfort.

Prefers low-impact exercises.

Limited home equipment.

No permanent pull-up bar.

Exercises should therefore:

- minimise unnecessary joint loading
- avoid requiring expensive equipment
- provide alternatives
- adapt progression independently

---

## Motivation

The objective is not athletic performance.

The objective is healthy aging.

Examples:

- continue playing basketball
- travel comfortably
- preserve mobility
- avoid chronic pain
- maintain muscle mass
- remain independent

---

## Design Implications

The planner should:

- consider external activities
- adapt to knee discomfort
- prioritise consistency
- maintain approximately 20-minute sessions
- favour functional movement over isolated muscle work

---

# Persona B — The Busy Professional

Age

35–55

Characteristics

Works mostly at a desk.

Feels stiff.

Exercises inconsistently.

Needs structure.

Main goals

- improve posture
- regain strength
- reduce back pain
- create a sustainable habit

Design implications

Simple onboarding.

Short sessions.

Minimal decisions.

Automatic planning.

---

# Persona C — The Retired Beginner

Age

60–80

Characteristics

Little previous training experience.

Concerned about mobility.

Concerned about falls.

May have arthritis or previous injuries.

Main goals

- improve balance
- preserve independence
- increase confidence
- move safely

Design implications

Large interface elements.

Clear explanations.

Slower progression.

Very low-impact alternatives.

Encouraging feedback.

---

# Persona D — The Recreational Athlete

Age

25–60

Already practices one or more sports.

Examples

- basketball
- hiking
- cycling
- tennis
- climbing
- swimming

Needs

Valens should improve performance indirectly by developing:

- stability
- mobility
- recovery
- joint resilience

The planner should account for sport-specific workload.

---

# Persona E — Rehabilitation User

This is a secondary persona.

Valens is **not** a medical application.

However, users may be recovering from:

- knee surgery
- shoulder injury
- lower back pain

Under the supervision of healthcare professionals.

Design implications

Exercises can be:

- disabled
- substituted
- regressed

Joint pain should influence planning.

Progression should remain conservative.

---

# Persona F — Open Source Contributor

Profile

Software developer.

Interested in:

- Kotlin
- Android
- Jetpack Compose
- Exercise science
- Privacy
- Open-source software

Needs

- clear architecture
- comprehensive documentation
- modular exercise library
- clean APIs
- automated tests

This persona is essential for the long-term sustainability of Valens.

---

# Anti-Personas

Valens is intentionally **not** designed primarily for the following users.

## Competitive Bodybuilder

Needs

- hypertrophy optimisation
- advanced split routines
- bodybuilding periodisation

Better served by specialised applications.

---

## Elite Strength Athlete

Needs

- powerlifting programming
- Olympic lifting
- maximal strength analytics

Outside the scope of Valens.

---

## Weight-Loss-First User

Valens is not a calorie tracker.

Weight management may be a consequence of improved activity, but it is not the primary objective.

---

## Social Fitness Influencer

Valens intentionally avoids:

- public rankings
- follower counts
- workout feeds
- competitive streaks

The focus remains personal progress.

---

# Common User Characteristics

Across all primary personas, users generally:

- value long-term health
- appreciate evidence-based advice
- prefer simple routines
- have limited time
- exercise mostly at home
- own little equipment
- are motivated by independence rather than appearance

---

# Design Principles Derived from Personas

These personas imply several product decisions.

## Short Sessions

Default duration:

20 minutes.

---

## Home First

Every workout should be achievable with little or no equipment whenever possible.

---

## Adaptive Planning

No fixed programmes.

Training adapts to:

- recovery
- pain
- available time
- previous activity
- long-term capacity

---

## Positive Coaching

The application encourages rather than judges.

Feedback should reinforce:

- consistency
- competence
- confidence

rather than guilt.

---

## Progressive Complexity

A new user should be able to complete a workout within five minutes of installing Valens.

Advanced analytics should remain optional.

---

# Persona Validation

Every proposed feature should answer the following questions:

1. Which persona benefits?

2. Does it improve long-term capacity?

3. Does it improve adherence?

4. Does it support healthy aging?

5. Does it respect the project's philosophy?

If the answer to most of these questions is **no**, the feature should be reconsidered.

---

# Summary

Valens is designed for people who want to remain physically capable throughout life.

It is not a fitness application that happens to include healthy aging.

It is a healthy aging application that uses intelligent exercise programming as its primary tool.

Every design decision should ultimately support one objective:

> **Help users continue doing the activities they love, for as many years as possible.**