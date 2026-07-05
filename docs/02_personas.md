# 02 – Personas

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

These personas guide product and engineering choices. They are not marketing stereotypes. They are practical design anchors.

A feature should normally benefit at least one primary persona.

---

## 2. Primary persona group: Active Agers

### Profile

Age range: 40–75.

Generally healthy, but noticing age-related changes:

- stiffness
- slower recovery
- joint sensitivity
- declining balance
- reduced strength
- posture issues
- loss of mobility

### Goals

- stay independent
- reduce pain risk
- maintain mobility
- maintain muscle
- keep recreational activities
- exercise at home
- avoid injury

### Design implications

Valens should offer:

- short sessions
- safe progressions
- clear explanations
- joint-aware planning
- low-equipment options
- long-term capacity tracking

---

## 3. Reference persona A: Thierry

This persona represents the first design target. It is specific enough to force concrete decisions.

### Profile

- 55-year-old male
- 168 cm
- 56–57 kg
- lean body type
- plant-based diet
- mostly home-based training
- recreational basketball shooting almost daily
- previous knee surgery
- occasional knee pain
- interested in healthy aging, not bodybuilding

### Current activities

- near-daily basketball shooting
- bodyweight training
- isometric holds
- mobility work
- walking

He is not sedentary. Valens must complement existing activity.

### Constraints

- knee sensitivity
- limited equipment
- no guaranteed pull-up bar
- home environment
- prefers 20-minute daily baseline

### Product implications

The planner must:

- account for basketball as external activity
- avoid overloading knee-dominant movements after basketball
- provide alternatives to dead hangs
- progress exercises independently
- distinguish muscle effort from joint pain
- preserve 20-minute routine duration as baseline
- prioritize long-term capacity

---

## 4. Persona B: Busy Professional

### Profile

Age 35–55. Desk-based job. Feels stiff and inconsistent.

### Goals

- build a sustainable habit
- reduce stiffness
- strengthen posture
- avoid time-consuming routines

### Needs

- simple onboarding
- automatic daily plan
- minimal choices
- short sessions
- low cognitive load

---

## 5. Persona C: Retired Beginner

### Profile

Age 60–80. Little formal training experience. May have arthritis or fear of falling.

### Goals

- move safely
- improve balance
- gain confidence
- remain independent

### Needs

- large UI elements
- clear instructions
- very conservative progressions
- chair/wall-supported options
- low-impact exercises
- positive language

---

## 6. Persona D: Recreational Athlete

### Profile

Age 25–60. Already does sport such as basketball, hiking, tennis, cycling or swimming.

### Goals

- remain healthy for sport
- reduce injury risk
- maintain mobility
- fill gaps not trained by sport

### Needs

- external activity logging
- sport-aware load balancing
- mobility and stability support
- recovery-aware planning

---

## 7. Persona E: Rehabilitation-aware User

Valens is not a medical app. However, users may have old or current injuries and use Valens alongside professional guidance.

### Needs

- disable exercises
- select sensitive joints
- choose conservative progression
- track pain trends
- substitute movement patterns
- export logs for discussion with professionals

---

## 8. Persona F: Open-source Contributor

### Profile

Developer interested in Android, Kotlin, exercise science or privacy-first apps.

### Needs

- clear architecture
- documented domain model
- testable planner
- JSON schemas
- contribution guide
- issue templates
- clean module boundaries

---

## 9. Anti-personas

Valens is not optimized for:

- competitive bodybuilders
- elite powerlifters
- calorie-focused weight-loss users
- social media fitness influencers
- users seeking gamified competition as the main goal

These users may still benefit from Valens, but they are not the design target.

---

## 10. Persona validation checklist

Before implementing a feature, ask:

1. Which persona benefits?
2. Does it support healthy aging?
3. Does it increase consistency?
4. Does it improve capacity or safety?
5. Does it respect privacy?
6. Does it make the app harder to use?

A feature that fails most checks should not enter the MVP.
