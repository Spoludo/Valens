package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun exercise_decodesWallSitBilateralJointStress() {
        val source = """
            {
              "id": "wall_sit",
              "schemaVersion": "0.2.0",
              "nameKey": "exercise.wall_sit.name",
              "descriptionKey": "exercise.wall_sit.description",
              "type": "isometric",
              "movementPatternId": "squat",
              "exerciseFamilyId": "wall_sit",
              "difficulty": 2,
              "equipment": ["wall"],
              "homeFriendly": true,
              "sideModel": "bilateral",
              "defaultPrescription": {
                "sets": 3,
                "holdSeconds": 30,
                "restSeconds": 60,
                "intensityTarget": 7
              },
              "muscles": {
                "primary": [
                  { "id": "quadriceps", "load": 0.8 }
                ],
                "secondary": [
                  { "id": "glutes", "load": 0.5 },
                  { "id": "hamstrings", "load": 0.3 }
                ],
                "stabilizers": [
                  { "id": "core", "load": 0.3 }
                ]
              },
              "jointStress": {
                "knee": {
                  "bilateral": 0.6
                },
                "hip": {
                  "bilateral": 0.3
                },
                "lumbar_spine": {
                  "midline": 0.1
                }
              },
              "fatigueCost": {
                "global": 6,
                "local": {
                  "quadriceps": 8,
                  "glutes": 5
                },
                "joint": {
                  "knee": {
                    "bilateral": 6
                  }
                }
              },
              "progression": [
                {
                  "id": "wall_sit_120deg_20s",
                  "labelKey": "progression.wall_sit_120deg_20s",
                  "holdSeconds": 20,
                  "difficulty": 1,
                  "kneeAngleDegrees": 120
                },
                {
                  "id": "wall_sit_110deg_30s",
                  "labelKey": "progression.wall_sit_110deg_30s",
                  "holdSeconds": 30,
                  "difficulty": 2,
                  "kneeAngleDegrees": 110
                },
                {
                  "id": "wall_sit_100deg_30s",
                  "labelKey": "progression.wall_sit_100deg_30s",
                  "holdSeconds": 30,
                  "difficulty": 3,
                  "kneeAngleDegrees": 100
                },
                {
                  "id": "wall_sit_90deg_30s",
                  "labelKey": "progression.wall_sit_90deg_30s",
                  "holdSeconds": 30,
                  "difficulty": 4,
                  "kneeAngleDegrees": 90
                },
                {
                  "id": "wall_sit_90deg_60s",
                  "labelKey": "progression.wall_sit_90deg_60s",
                  "holdSeconds": 60,
                  "difficulty": 5,
                  "kneeAngleDegrees": 90
                }
              ],
              "regressions": [
                "wall_sit_120deg",
                "chair_sit_hold"
              ],
              "alternatives": [
                "horse_stance",
                "supported_split_squat_hold"
              ],
              "contraindications": [
                "acute_knee_pain"
              ],
              "cues": {
                "setup": [
                  "exercise.wall_sit.cue.setup.1",
                  "exercise.wall_sit.cue.setup.2"
                ],
                "during": [
                  "exercise.wall_sit.cue.during.1",
                  "exercise.wall_sit.cue.during.2"
                ],
                "stopIf": [
                  "exercise.wall_sit.cue.stop_if.1",
                  "exercise.wall_sit.cue.stop_if.2"
                ]
              },
              "assets": {
                "illustration": "assets/illustrations/wall_sit.svg",
                "muscleMapFront": "assets/muscles/wall_sit_front.svg",
                "muscleMapBack": "assets/muscles/wall_sit_back.svg",
                "jointMapFront": "assets/joints/wall_sit_front.svg",
                "jointMapBack": "assets/joints/wall_sit_back.svg"
              }
            }
        """.trimIndent()

        val exercise = json.decodeFromString<Exercise>(source)

        assertEquals(ExerciseId("wall_sit"), exercise.id)
        assertEquals(SideModel.BILATERAL, exercise.sideModel)
        assertEquals(0.6, exercise.jointStress["knee"]?.get(JointLoadRole.BILATERAL) ?: 0.0, 0.0001)
        assertEquals(0.1, exercise.jointStress["lumbar_spine"]?.get(JointLoadRole.MIDLINE) ?: 0.0, 0.0001)
        assertEquals(
            listOf(ExerciseId("wall_sit_120deg"), ExerciseId("chair_sit_hold")),
            exercise.regressions,
        )
        assertEquals(ExerciseFamilyId("wall_sit"), exercise.exerciseFamilyId)
        assertEquals(5, exercise.progression.size)
    }

    @Test
    fun exercise_decodesSingleLegGluteBridgeHoldWorkingSideJointStress() {
        val source = """
            {
              "id": "single_leg_glute_bridge_hold",
              "schemaVersion": "0.2.0",
              "nameKey": "exercise.single_leg_glute_bridge_hold.name",
              "descriptionKey": "exercise.single_leg_glute_bridge_hold.description",
              "type": "isometric",
              "movementPatternId": "hip_extension",
              "exerciseFamilyId": "single_leg_glute_bridge",
              "difficulty": 3,
              "equipment": ["floor", "mat"],
              "homeFriendly": true,
              "sideModel": "left_right",
              "defaultPrescription": {
                "sets": 3,
                "holdSeconds": 20,
                "restSeconds": 45,
                "intensityTarget": 7
              },
              "muscles": {
                "primary": [
                  { "id": "glutes", "load": 0.9 },
                  { "id": "hamstrings", "load": 0.5 }
                ],
                "secondary": [
                  { "id": "glute_medius", "load": 0.5 },
                  { "id": "core", "load": 0.3 }
                ],
                "stabilizers": [
                  { "id": "erector_spinae", "load": 0.2 }
                ]
              },
              "jointStress": {
                "hip": {
                  "workingSide": 0.4
                },
                "knee": {
                  "workingSide": 0.2
                },
                "lumbar_spine": {
                  "midline": 0.2
                }
              },
              "fatigueCost": {
                "global": 5,
                "local": {
                  "glutes": 8,
                  "hamstrings": 4,
                  "core": 3
                },
                "joint": {
                  "hip": {
                    "workingSide": 4
                  },
                  "knee": {
                    "workingSide": 2
                  }
                }
              },
              "progression": [
                {
                  "id": "two_leg_20s",
                  "labelKey": "progression.single_leg_glute_bridge_hold.two_leg_20s",
                  "holdSeconds": 20,
                  "difficulty": 1,
                  "variationKey": "variation.two_leg"
                },
                {
                  "id": "single_leg_15s",
                  "labelKey": "progression.single_leg_glute_bridge_hold.single_leg_15s",
                  "holdSeconds": 15,
                  "difficulty": 2,
                  "variationKey": "variation.single_leg"
                },
                {
                  "id": "single_leg_25s",
                  "labelKey": "progression.single_leg_glute_bridge_hold.single_leg_25s",
                  "holdSeconds": 25,
                  "difficulty": 3,
                  "variationKey": "variation.single_leg"
                },
                {
                  "id": "single_leg_45s",
                  "labelKey": "progression.single_leg_glute_bridge_hold.single_leg_45s",
                  "holdSeconds": 45,
                  "difficulty": 4,
                  "variationKey": "variation.single_leg"
                }
              ],
              "regressions": [
                "two_leg_glute_bridge_hold"
              ],
              "alternatives": [
                "reverse_table_hold"
              ],
              "contraindications": [
                "acute_low_back_pain",
                "requires_floor_access"
              ],
              "cues": {
                "setup": [
                  "exercise.single_leg_glute_bridge_hold.cue.setup.1",
                  "exercise.single_leg_glute_bridge_hold.cue.setup.2"
                ],
                "during": [
                  "exercise.single_leg_glute_bridge_hold.cue.during.1",
                  "exercise.single_leg_glute_bridge_hold.cue.during.2"
                ],
                "stopIf": [
                  "exercise.single_leg_glute_bridge_hold.cue.stop_if.1",
                  "exercise.single_leg_glute_bridge_hold.cue.stop_if.2"
                ]
              },
              "assets": {
                "illustration": "assets/illustrations/single_leg_glute_bridge_hold.svg",
                "muscleMapFront": "assets/muscles/single_leg_glute_bridge_hold_front.svg",
                "muscleMapBack": "assets/muscles/single_leg_glute_bridge_hold_back.svg",
                "jointMapFront": "assets/joints/single_leg_glute_bridge_hold_front.svg",
                "jointMapBack": "assets/joints/single_leg_glute_bridge_hold_back.svg"
              }
            }
        """.trimIndent()

        val exercise = json.decodeFromString<Exercise>(source)

        assertEquals(ExerciseId("single_leg_glute_bridge_hold"), exercise.id)
        assertEquals(SideModel.LEFT_RIGHT, exercise.sideModel)
        assertEquals(0.4, exercise.jointStress["hip"]?.get(JointLoadRole.WORKING_SIDE) ?: 0.0, 0.0001)
        assertEquals(0.2, exercise.jointStress["knee"]?.get(JointLoadRole.WORKING_SIDE) ?: 0.0, 0.0001)
        assertEquals(MovementPatternId("hip_extension"), exercise.movementPatternId)
        assertEquals(4, exercise.progression.size)
    }
}
