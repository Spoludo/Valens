# Workout Guidance — Visual Pose + Spoken Cues — Design

**Status:** Approved
**Scope:** Extend the practice-first MVP workout screen with two guidance channels so the app is usable without staring at the phone: (1) a simplified body-pose illustration that animates from a neutral stance to each exercise's target pose, and (2) minimal spoken cues via Android's built-in TextToSpeech. Explicitly *not* in scope: any external animation/audio/TTS library, skeletal/3D rendering, camera or pose-detection input, a settings screen, or audio beeps.

## 1. Purpose

The current workout screen (`WorkoutScreen`/`WorkoutViewModel`, shipped in the practice-first MVP) shows only text: exercise name, phase label, countdown seconds, set counter. That's enough to follow the workout, but only if you keep looking at the screen. This design adds a lightweight visual posture guide and spoken phase/exercise cues, so the phone can be glanced at occasionally or not at all during a hold.

The two channels are independent and additive to the existing screen — neither changes `WorkoutEngine`'s behavior or public contract.

## 2. Package layout

```
workout/pose/BodyPoint.kt            — the 14-point enum
workout/pose/BodyPose.kt             — BodyPose data class + PoseProp enum
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

One small shared refactor: `totalWorkSeconds(exercise: Exercise): Int` is extracted as a top-level function (in `workout/engine`) from `WorkoutEngine`'s existing private `workSeconds(prescription)` formula, so both the engine and the new audio cue generator compute "how long is this hold" from one source of truth. `WorkoutEngine`'s own tested tick behavior is unchanged — this is a pure extraction, not a behavior change.

---

## Part A — Visual posture guide

## 3. Body pose data model

```kotlin
enum class BodyPoint { Head, Neck, LeftShoulder, RightShoulder, LeftElbow, RightElbow,
    LeftWrist, RightWrist, LeftHip, RightHip, LeftKnee, RightKnee, LeftAnkle, RightAnkle }

enum class PoseProp { NONE, WALL, FLOOR }

data class BodyPose(
    val points: Map<BodyPoint, Offset>,
    val prop: PoseProp = PoseProp.NONE,
)
```

Coordinates are normalized `0f..1f`, matching Canvas convention (x: left→right, y: top→bottom). Every `BodyPose` used by the app defines all 14 points — `RoutineExercisePoses` guarantees this by construction, so `interpolatePose` never has to handle a missing key.

Default pose style is a side-profile stick figure: bilateral pairs (shoulders, elbows, wrists, hips, knees, ankles) share identical coordinates, since these holds (wall push, wall sit, plank, hollow body, reverse table) are naturally side-viewed and true anatomical left/right separation isn't meaningful at phone-illustration scale. The 14-point model is kept in full (not collapsed to 8 unique points) so it stays future-proof for front-view poses, asymmetrical poses, and later camera/pose-comparison work.

`single_leg_balance_hold` is the one pose in this set that breaks from the coincident-pair default: it's modeled in a slight 3/4-front view, with the standing leg centered and the lifted leg's knee/ankle visibly offset, because side distinction is the entire point of that exercise and a pure side profile (or a small same-side offset) wouldn't read clearly.

`prop` (`WALL`/`FLOOR`) is a static visual reference line, not part of the interpolated geometry — the renderer reads `targetPose.prop` directly rather than blending it between neutral and target.

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

All 7 poses are hardcoded `BodyPose` values in `RoutineExercisePoses`, plus one `neutralStandingPose` (upright, arms at sides, used as the interpolation start point, the REST-phase pose, and the fallback when no target pose exists). Exact normalized coordinates are tuned during implementation; the shape intent for each is:

- **wall_push** — body leaning ~30° into a wall on the right edge, hands at shoulder height on the wall, feet planted back. `prop = WALL`.
- **hollow_body_hold** — lying near the floor; shoulders/arms and legs both lifted off it, hips as the low pivot point, forming the characteristic shallow "banana" curve. `prop = FLOOR`.
- **calf_raise_hold** — same silhouette as neutral standing, with a slightly taller-stretched torso as the only visual hint of the raise. The 14-point model has no foot/toe joint, so heel lift can't be shown directly — documented here as a known, accepted limitation, not a bug to chase.
- **wall_sit** — back vertical against the wall, thigh horizontal from hip to knee, shin vertical to the floor: the "invisible chair" L-shape. `prop = WALL`.
- **single_leg_balance_hold** — 3/4-front view (see §3); standing leg vertical and centered, lifted leg's knee/ankle offset out and up, arms slightly spread for balance.
- **reverse_table_hold** — seated, hands behind on the floor, hips lifted so torso and thighs are roughly horizontal ("tabletop"), knees bent, feet flat. `prop = FLOOR`.
- **plank_hold** — straight horizontal line from ankles through hips to head, forearms vertical down to the floor. `prop = FLOOR`.

`RoutineExercisePoses.targetPoseFor(exerciseId: String): BodyPose?` is a `when` over these 7 ids, `null` for anything else.

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
- drawn elements, all scaled from normalized space by the actual Canvas `size`: head circle (`Head`), torso line (`Neck` → hip midpoint), arm lines (`Shoulder→Elbow→Wrist` × 2), leg lines (`Hip→Knee→Ankle` × 2), small joint dots at all 14 points, and a wall line (vertical, near the right edge) or floor line (horizontal, near the bottom) when `prop != NONE`. Coincident left/right points simply overlay — harmless.

## 7. WorkoutScreen integration

- `WorkoutUiState.Running` gains one new field, `exerciseId: String` (from `exercise.id.value`), so the screen can resolve a target pose.
- `WorkoutContent` computes `animatedProgress` (§4) and renders `BodyPoseIllustration(targetPose = RoutineExercisePoses.targetPoseFor(state.exerciseId), progressToTarget = animatedProgress)` above the existing text/buttons.

## 8. Testing — pose

- `PoseInterpolationTest` — progress 0 returns `from`, progress 1 returns `to`, 0.5 returns the midpoint, for a representative point.
- `RoutineExercisePosesTest` — all 7 known ids return a pose with exactly the 14 `BodyPoint` keys; an unknown id returns `null`.
- `PoseProgressTest` — table test over each `WorkoutPhase` × representative `secondsRemaining`.
- No Compose/Canvas rendering test — consistent with the existing "no instrumented tests for the Compose screen" scope cut from the prior MVP commits.

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
| `WORK → REST` | one rotating calm encouragement (`exerciseIndex % 5` into the fixed phrase list) |
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

This keeps cues non-redundant and restrained: pausing/resuming never re-announces "Start"; the final exercise's last set transitions `WORK → COMPLETE` directly (per `WorkoutEngine`'s own logic, `REST` never precedes the last transition), so "Workout complete." is never preceded by an encouragement line; "Next: X. Prepare." replaces a separate countdown-entry announcement for exercises 2 through 7 (only exercise 1 gets the combined "Let's begin. Prepare for X." opening line).

## 11. Android player

```kotlin
class AndroidWorkoutAudioCuePlayer(context: Context) : WorkoutAudioCuePlayer {
    @Volatile private var isReady = false
    private val tts = TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS
    }
    override fun speak(text: String) {
        if (!isReady) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }
    override fun shutdown() { tts.stop(); tts.shutdown() }
}
```

- `QUEUE_FLUSH`, not `QUEUE_ADD`: each new cue interrupts whatever's still playing, so spoken guidance never lags behind the actual workout state after a burst of transitions.
- Speaking before init completes is a silent no-op — cues issued in that window are dropped, not queued. Keeps the implementation minimal; acceptable because TTS engine binding is typically sub-second and losing an occasional opening cue is low-cost for an MVP.
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

- `WorkoutAudioCueGeneratorTest` — pure unit test covering every row of the mapping table in §10, plus the "nothing said" cases (pause/resume, mid-countdown ticks, mid-work ticks away from the 10-second mark).
- No test for `AndroidWorkoutAudioCuePlayer` — Android-framework-dependent, same precedent as `AndroidAssetExercisePackJsonSource`; verified manually on-device per the acceptance criteria below.

---

## 15. Commits

```
docs(spec): design visual and spoken workout guidance
feat(workout): add body pose model and interpolation
feat(workout): add hardcoded target poses for the beginner routine
feat(ui): render animated body pose illustration on the workout screen
feat(audio): add lightweight workout TextToSpeech cues
```

## 16. Out of scope (explicit)

External animation frameworks (skeletal, Rive, Lottie, Spine), any XR/3D rendering, camera input or ML Kit pose detection, cloud TTS, custom speech synthesis, audio beeps/sound effects, a settings screen for voice/volume, aggressive or "coach-style" phrasing, queued (non-flushing) TTS playback, instrumented/Robolectric tests for the Android TTS wrapper or the Compose Canvas rendering.

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
