# 14 – Audio Engine

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Audio guidance allows users to exercise without staring at the screen.

This is essential for isometric holds, mobility and balance.

---

## 2. Audio types

- countdown voice
- beep
- soft ticking/metronome
- halfway cue
- final five-second cue
- rest cue
- next exercise cue
- safety cue

---

## 3. Implementation options

MVP:

- Android TextToSpeech
- simple generated beep/tick assets or SoundPool

Later:

- bundled voice prompts
- user-selectable voices
- multilingual cue packs

---

## 4. Cue examples

```text
Get ready.
Horse stance.
Three. Two. One. Hold.
Halfway.
Five. Four. Three. Two. One. Relax.
Next: single-leg bridge.
```

---

## 5. User controls

Settings:

```text
voiceEnabled
ticksEnabled
beepsEnabled
countdownEnabled
halfwayCueEnabled
volume
```

---

## 6. Accessibility

Audio should support:

- screen-off training
- low-vision users
- users who cannot look at screen during holds

---

## 7. Dynamic exercise tempo

For dynamic exercises:

```text
Down. Two. Three. Four. Up.
```

Tempo cues should be optional because they may become annoying.

---

## 8. Audio event API

Workout Engine emits semantic events.

Audio Engine decides how to speak them.

This avoids hardcoding audio in planner or UI.

---

## 9. Acceptance criteria

MVP audio must provide:

- countdown
- start/stop beeps
- optional ticking
- halfway cue
- final five seconds
- next exercise announcement

---

## 10. Summary

Audio should make Valens feel like a calm coach rather than a noisy alarm.
