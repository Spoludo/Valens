package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseType {
    @SerialName("isometric") ISOMETRIC,
    @SerialName("dynamic") DYNAMIC,
    @SerialName("mobility_dynamic") MOBILITY_DYNAMIC,
    @SerialName("mobility_isometric") MOBILITY_ISOMETRIC,
    @SerialName("balance") BALANCE,
    @SerialName("assessment") ASSESSMENT,
    @SerialName("breathing") BREATHING,
}

@Serializable
enum class SideModel {
    @SerialName("bilateral") BILATERAL,
    @SerialName("left_right") LEFT_RIGHT,
    @SerialName("single_side") SINGLE_SIDE,
    @SerialName("midline") MIDLINE,
}

@Serializable
enum class EquipmentItem {
    @SerialName("none") NONE,
    @SerialName("wall") WALL,
    @SerialName("floor") FLOOR,
    @SerialName("chair") CHAIR,
    @SerialName("mat") MAT,
    @SerialName("bar") BAR,
    @SerialName("band") BAND,
    @SerialName("dumbbell") DUMBBELL,
    @SerialName("backpack") BACKPACK,
    @SerialName("timer") TIMER,
}

@Serializable
enum class ContraindicationFlag {
    @SerialName("acute_knee_pain") ACUTE_KNEE_PAIN,
    @SerialName("acute_shoulder_pain") ACUTE_SHOULDER_PAIN,
    @SerialName("acute_wrist_pain") ACUTE_WRIST_PAIN,
    @SerialName("acute_low_back_pain") ACUTE_LOW_BACK_PAIN,
    @SerialName("dizziness") DIZZINESS,
    @SerialName("uncontrolled_hypertension") UNCONTROLLED_HYPERTENSION,
    @SerialName("requires_overhead_tolerance") REQUIRES_OVERHEAD_TOLERANCE,
    @SerialName("requires_floor_access") REQUIRES_FLOOR_ACCESS,
    @SerialName("requires_bar") REQUIRES_BAR,
}

@Serializable
data class Exercise(
    val id: ExerciseId,
    val schemaVersion: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val type: ExerciseType,
    val movementPatternId: MovementPatternId,
    val exerciseFamilyId: ExerciseFamilyId,
    val difficulty: Int,
    val equipment: List<EquipmentItem>,
    val homeFriendly: Boolean,
    val sideModel: SideModel,
    val defaultPrescription: Prescription,
    val muscles: Muscles,
    val jointStress: JointStressMap,
    val fatigueCost: FatigueCost,
    val progression: List<ProgressionStep>,
    val regressions: List<ExerciseId>,
    val alternatives: List<ExerciseId>,
    val contraindications: List<ContraindicationFlag>,
    val cues: Cues,
    val assets: ExerciseAssets,
)
