# Workout Guidance — Visual Pose + Spoken Cues — Design

**Status:** Approved
**Scope:** Extend the practice-first MVP workout screen with two guidance channels so the app is usable without staring at the phone: (1) a simplified body-pose illustration that animates from a neutral stance to each exercise's target pose, and (2) minimal spoken cues via Android's built-in TextToSpeech. Explicitly *not* in scope: any external animation/audio/TTS library, skeletal/3D rendering, camera or pose-detection input, a settings screen, or audio beeps.

**Revision (2026-07-15):** after on-device testing, this spec was extended to (a) add hands/feet to the pose model, (b) support multiple pose view-angles per exercise (starting with `wall_push`'s side + front views), and (c) make `WorkoutScreen` responsive to portrait/landscape instead of relying on a portrait lock. See §3, §5–§8 (updated) for the current design; §9–§14 (audio) are unchanged from the original spec.

## 1. Purpose

The current workout screen (`WorkoutScreen`/`WorkoutViewModel`, shipped in the practice-first MVP) shows only text: exercise name, phase label, countdown seconds, set counter. That's enough to follow the workout, but only if you keep looking at the screen. This design adds a lightweight visual posture guide and spoken phase/exercise cues, so the phone can be glanced at occasionally or not at all during a hold.

The two channels are independent and additive to the existing screen — neither changes `WorkoutEngine`'s behavior or public contract.

## 2. Package layout

```
workout/pose/BodyPoint.kt            — the 20-point enum
workout/pose/BodyPose.kt             — BodyPose data class + PoseProp enum + PoseViewAngle + BodyPoseView
workout/pose/PoseInterpolation.kt    — interpolatePose()
workout/pose/RoutineExercisePoses.kt — neutral pose + the 7 hardcoded target poses
workout/pose/PoseProgress.kt         — poseProgressFor()

workout/audio/WorkoutAudioCuePlayer.kt       — pure interface
workout/audio/NoOpWorkoutAudioCuePlayer.kt   — pure no-op implementation
workout/audio/WorkoutAudioCueGenerator.kt    — pure state-transition → cue-text logic
workout/audio/AndroidWorkoutAudioCuePlayer.kt — the one Android-TTS-dependent file

ui/workout/BodyPoseIllustration.kt   — Composable + Canvas drawing
```

`workout/pose` and `workout/audio` sit alongside the existing `workout/engine` and `workout/timer`, matching CLAUDE.md's own suggested package tree (`workout/{engine,timer,audio}`) and the precedent set by `data/json`, which already holds both a pure interface (`ExercisePackJsonSource`) and its Android-specific implementation (`AndroidAssetExercisePackJsonSource`) side by side. `WorkoutEngine` itself is not modified in its public behavior by either addition — it stays pure, with no knowledge that a pose illustration or audio cues exist.

**Revised 2026-07-15:** no new files or packages are introduced for the 20-point/multi-view/responsive-layout work — `PoseViewAngle` and `BodyPoseView` are added to the existing `workout/pose/BodyPose.kt`; the new `PoseViewsRow`/`WorkoutContentPortrait`/`WorkoutContentLandscape` composables are added to the existing `ui/workout/WorkoutScreen.kt`, since they're layout variants of that one screen, not reusable elsewhere.

One small shared refactor: `totalWorkSeconds(exercise: Exercise): Int` is extracted as a top-level function (in `workout/engine`) from `WorkoutEngine`'s existing private `workSeconds(prescription)` formula, so both the engine and the new audio cue generator compute "how long is this hold" from one source of truth. `WorkoutEngine`'s own tested tick behavior is unchanged — this is a pure extraction, not a behavior change.

---

## Part A — Visual posture guide

## 3. Body pose data model

```kotlin
enum class BodyPoint {
    Head,
    Neck,

    LeftShoulder,
    RightShoulder,
    LeftElbow,
    RightElbow,
    LeftWrist,
    RightWrist,
    LeftHand,
    RightHand,

    LeftHip,
    RightHip,
    LeftKnee,
    RightKnee,
    LeftAnkle,
    RightAnkle,
    LeftHeel,
    RightHeel,
    LeftToe,
    RightToe,
}

enum class PoseProp { NONE, WALL, FLOOR }

data class BodyPose(
    val points: Map<BodyPoint, Offset>,
    val prop: PoseProp = PoseProp.NONE,
)

enum class PoseViewAngle { SIDE, FRONT, BACK }

data class BodyPoseView(
    val angle: PoseViewAngle,
    val label: String,
    val pose: BodyPose,
)
```

**(Revised 2026-07-15: 14 → 20 points.)** `BodyPoint` gained `LeftHand`/`RightHand` (past the wrist) and `LeftHeel`/`RightHeel`/`LeftToe`/`RightToe` (past the ankle), after on-device testing showed `wall_push` in particular needs visible hand-on-wall and foot-placement detail, not just a limb-line skeleton. Coordinates stay normalized `0f..1f`, matching Canvas convention (x: left→right, y: top→bottom). Every `BodyPose` used by the app defines all 20 points — `RoutineExercisePoses` guarantees this by construction, so `interpolatePose` never has to handle a missing key. No anatomical precision is intended for hand/foot placement — they're short extensions/offsets past the wrist and ankle, oriented per-pose to read sensibly (e.g. a foot's heel and toe as two short segments branching from the ankle), not measured or physically simulated.

Default pose style is a side-profile stick figure: bilateral pairs (shoulders, elbows, wrists, hands, hips, knees, ankles, heels, toes) share identical coordinates, since these holds (wall push, wall sit, plank, hollow body, reverse table) are naturally side-viewed and true anatomical left/right separation isn't meaningful at phone-illustration scale. The 20-point model is kept in full (not collapsed to 10 unique points) so it stays future-proof for front-view poses, asymmetrical poses, and later camera/pose-comparison work.

`single_leg_balance_hold` is the one single-view pose that breaks from the coincident-pair default: it's modeled in a slight 3/4-front view, with the standing leg centered and the lifted leg's knee/ankle/heel/toe visibly offset, because side distinction is the entire point of that exercise and a pure side profile (or a small same-side offset) wouldn't read clearly.

`prop` (`WALL`/`FLOOR`) is a static visual reference line, not part of the interpolated geometry — the renderer reads `targetPose.prop` directly rather than blending it between neutral and target.

**New in this revision: multiple view-angles per exercise.** A single side-profile pose can't show both "how far do I lean" (a side concern) and "are my hands/feet symmetric and correctly placed" (a front/back concern) at once — `wall_push` needs both. `BodyPoseView` pairs a `BodyPose` with a `PoseViewAngle` tag and a short display label (e.g. `"Side"`, `"Front"`) shown under its illustration. `PoseViewAngle` has three values for now (`SIDE`/`FRONT`/`BACK`) — enough to distinguish the views this revision actually adds, without inventing angles nothing uses yet.

## 4. Interpolation & animation progress

```kotlin
fun interpolatePose(from: BodyPose, to: BodyPose, progress: Float): BodyPose =
    BodyPose(
        points = BodyPoint.entries.associateWith { point ->
            lerp(from.points.getValue(point), to.points.getValue(point), progress.coerceIn(0f, 1f))
        },
        prop = to.prop,
    )
```

Uses `androidx.compose.ui.geometry.lerp(Offset, Offset, Float)`, already available — no hand-rolled blending.

Progress is driven by a small pure function mapping engine state to a *raw target* (not by a single fixed-duration animation per phase):

```kotlin
fun poseProgressFor(phase: WorkoutPhase, secondsRemaining: Int): Float = when (phase) {
    WorkoutPhase.COUNTDOWN -> (1f - secondsRemaining / PREP_COUNTDOWN_SECONDS.toFloat()).coerceIn(0f, 1f)
    WorkoutPhase.WORK, WorkoutPhase.COMPLETE -> 1f
    WorkoutPhase.REST -> 0f
}
```

`WorkoutScreen` wraps this raw target in `animateFloatAsState(poseProgressFor(...), tween(900))`. Because the raw target already steps through 0 → 0.33 → 0.66 → 1.0 across the real per-second countdown ticks, the ~900ms tween smooths each step into continuous motion across the whole prep phase, rather than a single abrupt snap. During REST the pose animates back to neutral, visually signaling "resting."

## 5. Poses catalog

All 7 exercise poses are hardcoded `BodyPose` values in `RoutineExercisePoses`, plus one `neutralStandingPose` (upright, arms at sides, used as the interpolation start point, the REST-phase pose, and the fallback when no target pose exists). Every pose — including `neutralStandingPose` — now defines all 20 points (hand/heel/toe coordinates added to the 8 poses that already existed). Exact normalized coordinates are tuned during implementation; the shape intent for each is:

- **wall_push** — **now two views** (see below), replacing the single side pose from the original revision.
- **hollow_body_hold** — lying near the floor; shoulders/arms and legs both lifted off it, hips as the low pivot point, forming the characteristic shallow "banana" curve; hands past the wrists continue the overhead reach, toes past the ankles continue the leg extension. `prop = FLOOR`.
- **calf_raise_hold** — **limitation from the original revision resolved.** With `Heel`/`Toe` as separate points, the heel is placed *above* the toe (both above/at the ankle), genuinely depicting a raised heel with the ball of the foot still down — the previous version could only stretch the torso slightly since the 14-point model had no foot detail to lift.
- **wall_sit** — back vertical against the wall, thigh horizontal from hip to knee, shin vertical to the floor: the "invisible chair" L-shape; feet flat and forward of the ankle, hands resting near the thigh. `prop = WALL`.
- **single_leg_balance_hold** — 3/4-front view (see §3); standing leg vertical and centered with its heel/toe at the ground, lifted leg's knee/ankle/heel/toe offset out and up, arms slightly spread for balance with hands past the wrists.
- **reverse_table_hold** — seated, hands behind on the floor (fingers/palm past the wrists), hips lifted so torso and thighs are roughly horizontal ("tabletop"), knees bent, feet flat with heel/toe at floor level. `prop = FLOOR`.
- **plank_hold** — straight horizontal line from ankles through hips to head, forearms vertical down to the floor with hands past the wrists on the floor; toes tucked near the floor with heels raised slightly behind, matching a real plank foot position. `prop = FLOOR`.

**`wall_push`'s two views (new):**
- **`SIDE`/"Side"** — the original pose, extended with hand/foot points: body leaning ~30° into a wall on the right edge, hands (past the wrists) touching the wall, feet (heel behind, toe forward) planted back on the ground. `prop = WALL`. Shows lean angle and wall contact.
- **`FRONT`/"Front"** — new: person facing the viewer, arms raised and symmetric as if pressing into a wall where the viewer is standing (the wall itself isn't drawn — `prop = NONE` — since it's implied to be behind the camera, and a wall line on a face-on view would be confusing rather than helpful). Both hands, both feet, and the hip/shoulder line are all distinct left/right coordinates, mirrored around the horizontal center. **This view is not a physically literal camera angle** — its only job is to show bilateral symmetry and placement (both hands on the wall, both feet planted, centered stance), not body lean, which the `SIDE` view already covers.

Other exercises keep a single view for now (adding a second view where it doesn't add real clarity would be scope creep); each is tagged `SIDE`, except `single_leg_balance_hold` which is tagged `FRONT` since it's already modeled as a 3/4-front pose.

`RoutineExercisePoses.targetPoseFor(exerciseId: String): BodyPose?` from the original revision is **replaced** (not kept alongside) by:

```kotlin
fun targetPoseViewsFor(exerciseId: String): List<BodyPoseView>
```

— a `when` over the 7 known ids, returning `wall_push`'s two-element `[SIDE, FRONT]` list, a one-element list for every other known id, and an empty list for anything else (replacing the old `null` return with an empty-list return; both mean "nothing on record for this id," just list-shaped instead of nullable, since the caller — `WorkoutScreen` — now always needs a list to iterate).

## 6. Canvas renderer

```kotlin
@Composable
fun BodyPoseIllustration(
    targetPose: BodyPose?,
    modifier: Modifier = Modifier,
    progressToTarget: Float = 1f,
)
```

- `targetPose == null` → draw `RoutineExercisePoses.neutralStandingPose` only, ignoring `progressToTarget` (the "simple fallback placeholder").
- otherwise → draw `interpolatePose(neutralStandingPose, targetPose, progressToTarget)`.
- drawn elements, all scaled from normalized space by the actual Canvas `size`: head circle (`Head`), torso line (`Neck` → hip midpoint), arm lines (`Shoulder→Elbow→Wrist→Hand` × 2, extended from the original `Shoulder→Elbow→Wrist`), leg lines (`Hip→Knee→Ankle` × 2, unchanged) plus a small two-segment "foot" (`Ankle→Heel` and `Ankle→Toe`, drawn separately from the leg chain since a foot branches rather than continuing a single line) × 2, small joint dots at all 20 points (the existing `for (bodyPoint in BodyPoint.entries) { if (Head) continue; drawCircle(...) }` loop needs no change — it already covers whatever points the enum has), and a wall line (vertical, near the right edge) or floor line (horizontal, near the bottom) when `prop != NONE`. Coincident left/right points simply overlay — harmless.
- `BodyPoseIllustration` itself is unchanged in signature — it still renders one `BodyPose`. Multiple views (§7) are handled by rendering the composable multiple times, once per `BodyPoseView`, not by teaching it about views.

## 7. WorkoutScreen integration — responsive portrait/landscape layout

- `WorkoutUiState.Running` gains one new field, `exerciseId: String` (from `exercise.id.value`), so the screen can resolve target pose views.
- `WorkoutContent` computes `animatedProgress` (§4) and `val views = RoutineExercisePoses.targetPoseViewsFor(state.exerciseId)`.

**Revised 2026-07-15 — no orientation lock.** The original plan for this section was a single portrait-only `Column`. On-device testing showed that a portrait lock is the wrong MVP tradeoff: the phone may reasonably be propped up sideways during a workout, so `WorkoutScreen` must adapt to whatever orientation the device is actually in, not restrict it.

**Detecting the layout to use:** `BoxWithConstraints` is preferred over `LocalConfiguration.current.orientation`, because it reacts to the actual available space (`maxWidth`/`maxHeight`) rather than the device's nominal orientation — this also does the right thing under split-screen, multi-window, or unusual aspect ratios, where "orientation" and "available space shape" can disagree:

```kotlin
BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isLandscapeLike = maxWidth > maxHeight
    if (isLandscapeLike) {
        WorkoutContentLandscape(...)
    } else {
        WorkoutContentPortrait(...)
    }
}
```

**Shared building block — `PoseViewsRow`:** both layouts render `views: List<BodyPoseView>` the same way, just at a different size, so one composable is reused rather than duplicated:

```kotlin
@Composable
fun PoseViewsRow(
    views: List<BodyPoseView>,
    progressToTarget: Float,
    illustrationHeight: Dp,
    modifier: Modifier = Modifier,
)
```

- empty `views` → renders one `BodyPoseIllustration(targetPose = null, ...)` (the existing neutral-pose fallback).
- non-empty → a `Row` with one column per view (each `Modifier.weight(1f)`), each column stacking its `BodyPoseIllustration(targetPose = view.pose, ...)` above a small `Text(view.label)` (e.g. "Side", "Front"). For `wall_push` this puts the two views side by side, each roughly half-width; for every other exercise it's a single full-width column — visually identical to the original single-view layout.
- `illustrationHeight` is the one thing that differs between portrait and landscape call sites — everything else about `PoseViewsRow` is orientation-agnostic.

**Portrait layout** (`WorkoutContentPortrait`) — the original design, adapted to call `PoseViewsRow` instead of `BodyPoseIllustration` directly: `PoseViewsRow` on top at a generous height (e.g. 200dp), then exercise name / phase / timer / set counter / next-exercise / controls below, in a `Column`, as before.

**Landscape layout** (`WorkoutContentLandscape`) — a `Row`: a left column (~40% width) holding `PoseViewsRow` at a *smaller* `illustrationHeight` (e.g. 110dp, since landscape has less vertical room), and a right column (~60% width) stacking exercise name / phase / timer / set counter / next-exercise / controls — the same information as portrait, just in a narrower, shorter column.

**Priority when space is tight (landscape's whole reason for a separate layout):**

```text
1. timer visible
2. current exercise name visible
3. phase visible
4. controls visible
5. pose illustration visible
6. next-exercise visible if space allows
```

Concretely: the timer (`state.secondsRemaining`) keeps its large, prominent text style in landscape exactly as in portrait — it is never shrunk to make room for anything else. The `Pause`/`Resume`/`Next` buttons keep their normal tap-target size. If anything has to give under a narrow/short landscape window, it's the illustration — `PoseViewsRow`'s `illustrationHeight` is the one deliberately-shrinkable parameter in this whole layout, precisely so it can absorb the size pressure instead of the timer or controls doing so. Nothing is hidden outright in landscape; the layout is sized so timer and controls always fit, with the illustration and (if truly tight) the next-exercise line being what gets visually smaller first.

No new dependency: `BoxWithConstraints` is part of `androidx.compose.foundation.layout`, already in use throughout this screen.

## 8. Testing — pose

- `PoseInterpolationTest` — progress 0 returns `from`, progress 1 returns `to`, 0.5 returns the midpoint, for a representative point. **Unchanged by the 20-point revision** — the test fixtures build `from`/`to` via `BodyPoint.entries.associateWith { ... }`, so they automatically cover however many points the enum has; no edit needed.
- `PoseProgressTest` — table test over each `WorkoutPhase` × representative `secondsRemaining`. **Unchanged** — has no dependency on `BodyPoint` at all.
- `RoutineExercisePosesTest` — **revised** for `targetPoseViewsFor`:
  - every known id's returned view list is non-empty, and every `BodyPoseView.pose` in every returned list has exactly the 20 `BodyPoint` keys (`BodyPoint.entries.toSet()` — this assertion also needed no manual point-count change, only the fixture/iteration shape to walk views instead of a single pose).
  - `targetPoseViewsFor("wall_push")` returns exactly two views, angles `[SIDE, FRONT]` in that order, labels `"Side"` and `"Front"`.
  - every other known id returns exactly one view.
  - `targetPoseViewsFor("not_a_real_exercise")` returns an empty list.
  - `neutralStandingPose` still has exactly the 20 `BodyPoint` keys.
- No Compose/Canvas rendering test — consistent with the existing "no instrumented tests for the Compose screen" scope cut from the prior MVP commits. This also covers `PoseViewsRow` and the portrait/landscape `WorkoutContent` split — verified by compiling, `./gradlew build`, and manual on-device testing in both orientations.

---

## Part B — Spoken cues (TextToSpeech)

## 9. Abstraction

```kotlin
interface WorkoutAudioCuePlayer {
    fun speak(text: String)
    fun shutdown()
}

object NoOpWorkoutAudioCuePlayer : WorkoutAudioCuePlayer {
    override fun speak(text: String) = Unit
    override fun shutdown() = Unit
}
```

`WorkoutViewModel`/`WorkoutAudioCueGenerator` depend only on the interface. `NoOpWorkoutAudioCuePlayer` is the default for anywhere audio should be inert: unit tests, and any future preview/disabled-audio path.

## 10. Cue generation — state-transition mapping

`WorkoutAudioCueGenerator(routine: List<Exercise>, displayName: (Exercise) -> String)` exposes `fun cueFor(previous: WorkoutEngineState?, current: WorkoutEngineState): String?`, called once per emitted engine state (the caller diffs consecutive states, not raw ticks).

| Transition | Cue |
|---|---|
| `hasStarted` flips `false → true` | `"Let's begin. Prepare for <name>."` |
| `COUNTDOWN → WORK` | `"Start."` |
| `WORK`, `secondsRemaining == 10`, hold longer than 10s, guarded to fire once (`previous.secondsRemaining != 10`) | `"Ten seconds left."` |
| `WORK → REST`, only on the exercise's **last** set (`current.setIndex >= exercise.defaultPrescription.sets - 1`) | one rotating calm encouragement (`exerciseIndex % 5` into the fixed phrase list) |
| `WORK → REST`, any earlier set of a multi-set exercise | `null` |
| `REST → COUNTDOWN` (advancing to the next exercise) | `"Next: <name>. Prepare."` |
| `WORK → COMPLETE` (final exercise's last set) | `"Workout complete."` |
| pause/resume toggles, mid-phase ticks, anything else | `null` — nothing spoken |

Encouragement phrases (fixed, calm, sparse — no aggressive-coach tone):
```
Good. Keep breathing.
Stay relaxed.
Move with control.
Almost there.
Nice work.
```

This keeps cues non-redundant and restrained: pausing/resuming never re-announces "Start"; the final exercise's last set transitions `WORK → COMPLETE` directly (per `WorkoutEngine`'s own logic, `REST` never precedes the last transition), so "Workout complete." is never preceded by an encouragement line; "Next: X. Prepare." replaces a separate countdown-entry announcement for exercises 2 through 7 (only exercise 1 gets the combined "Let's begin. Prepare for X." opening line); and encouragement is capped at most once **per exercise**, not once per set — for an exercise with multiple sets, only the last set's `WORK → REST` transition speaks a phrase, so mid-exercise set changes (e.g. set 1 → rest → set 2) stay silent. `WorkoutAudioCueGenerator` already has `routine` in scope, so checking `exercise.defaultPrescription.sets` against `current.setIndex` costs nothing extra.

## 11. Android player

```kotlin
class AndroidWorkoutAudioCuePlayer(context: Context) : WorkoutAudioCuePlayer {
    @Volatile private var isReady = false
    @Volatile private var isShutdown = false

    private val tts = TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS && !isShutdown
    }

    override fun speak(text: String) {
        if (!isReady || isShutdown) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    override fun shutdown() {
        isShutdown = true
        tts.stop()
        tts.shutdown()
    }
}
```

- `QUEUE_FLUSH`, not `QUEUE_ADD`: each new cue interrupts whatever's still playing, so spoken guidance never lags behind the actual workout state after a burst of transitions.
- Speaking before init completes is a silent no-op — cues issued in that window are dropped, not queued. Keeps the implementation minimal; acceptable because TTS engine binding is typically sub-second and losing an occasional opening cue is low-cost for an MVP.
- `isShutdown` is a separate guard from `isReady`: the async `OnInitListener` callback can fire *after* `shutdown()` was already called (e.g. init completing just as the screen is torn down), so it also checks `!isShutdown` before flipping `isReady` true, and `speak()` checks both flags — any late `speak()` call after `shutdown()` is a safe no-op rather than touching a disposed `TextToSpeech` instance.
- No manifest permission required — basic TTS playback needs none.
- This is the only file in the feature that imports `android.speech.tts`; everything else in `workout/audio` is plain Kotlin.

## 12. Lifecycle ownership

**Rule:** the same owner that creates the `TextToSpeech` player shuts it down. For this MVP that owner is `MainActivity`, not `ValensApplication` — a lazy Application-scoped singleton paired with Activity-scoped `shutdown()` would let the native TTS engine binding outlive or get torn down independently of its creator, which is exactly the mismatch to avoid (and behaves unpredictably across Activity recreation).

- `MainActivity` constructs `AndroidWorkoutAudioCuePlayer(this)` once (e.g. as an Activity-scoped `by lazy` or plain `val` in `onCreate`).
- It's passed into `WorkoutViewModel.Factory(exerciseRepository, audioCuePlayer)` at the `"workout"` nav route, same place `application.exerciseRepository` is already passed today.
- `MainActivity.onDestroy()` calls `audioCuePlayer.shutdown()`.
- `WorkoutViewModel`'s own default (e.g. if ever constructed without an Activity context, such as in future previews) is `NoOpWorkoutAudioCuePlayer` — never a second real `TextToSpeech` instance.

`ExerciseRepository` stays Application-scoped exactly as before — this rule applies specifically to the TTS player, whose native resource lifecycle doesn't match the repository's cacheable-data lifecycle.

## 13. Wiring

- `WorkoutViewModel` takes `audioCuePlayer: WorkoutAudioCuePlayer` as a second constructor parameter; `Factory` gains the matching parameter.
- In `startEngine()`, alongside building `routine`, it builds `WorkoutAudioCueGenerator(routine, ::displayName)`.
- In the existing `state.collect` block, the ViewModel tracks the last-seen `WorkoutEngineState`, calls `cueGenerator.cueFor(last, state)` on each emission, and calls `audioCuePlayer.speak(cue)` when non-null, before updating `lastCuedState` and `_uiState`.

## 14. Testing — audio

- `WorkoutAudioCueGeneratorTest` — pure unit test covering every row of the mapping table in §10, plus the "nothing said" cases (pause/resume, mid-countdown ticks, mid-work ticks away from the 10-second mark, and a multi-set exercise's non-final `WORK → REST` transitions).
- No test for `AndroidWorkoutAudioCuePlayer` — Android-framework-dependent, same precedent as `AndroidAssetExercisePackJsonSource`; verified manually on-device per the acceptance criteria below.

---

## 15. Commits

Original revision (shipped):
```
docs(spec): design visual and spoken workout guidance
feat(workout): add body pose model and interpolation
feat(workout): add hardcoded target poses for the beginner routine
feat(ui): render animated body pose illustration on the workout screen
feat(audio): add lightweight workout TextToSpeech cues
```

This revision (2026-07-15 — 20-point model, multi-view poses, responsive layout):
```
docs(spec): update workout guidance with landscape and multi-angle poses
feat(workout): extend body pose model with hands feet and multiple views
feat(ui): render multi-angle pose illustrations responsively
fix(ui): support landscape workout layout
```

## 16. Out of scope (explicit)

External animation frameworks (skeletal, Rive, Lottie, Spine), any XR/3D rendering, camera input or ML Kit pose detection, cloud TTS, custom speech synthesis, audio beeps/sound effects, a settings screen for voice/volume, aggressive or "coach-style" phrasing, queued (non-flushing) TTS playback, instrumented/Robolectric tests for the Android TTS wrapper or the Compose Canvas rendering, muscle maps, detailed biomechanics, an orientation lock on `MainActivity` or `WorkoutScreen` (landscape is a real MVP requirement, not deferred), a general-purpose adaptive-layout framework (the portrait/landscape split is hand-written and specific to this one screen), redesigning the TextToSpeech layer beyond what §12's rotation-lifecycle fix already required.

## 17. Acceptance

```bash
./gradlew test
./gradlew build
```

Manual, on-device:
- workout screen shows a simplified body pose for each routine exercise
- countdown animates from neutral standing to target pose
- phone speaks the first exercise ("Let's begin. Prepare for ...")
- phone says "Start" when the hold begins
- phone gives a useful timing cue ("Ten seconds left.") on holds longer than 10s
- phone announces the next exercise ("Next: ... Prepare.")
- app does not crash if TTS is unavailable or not yet initialized
- **(2026-07-15 additions)**
  - portrait workout screen still works exactly as before
  - landscape workout screen keeps the timer, exercise name, phase, and controls clearly visible and reachable
  - `wall_push` shows two labeled illustrations ("Side" and "Front") with visible hands and feet
  - other exercises show one illustration, now with visible hands and feet
  - rotating the device mid-workout does not crash the app, and spoken cues continue working after rotation (per §12's fix)
