package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MovementDomain {
    @SerialName("strength") STRENGTH,
    @SerialName("mobility") MOBILITY,
    @SerialName("balance") BALANCE,
    @SerialName("endurance") ENDURANCE,
    @SerialName("recovery") RECOVERY,
    @SerialName("assessment") ASSESSMENT,
}

@Serializable
data class WeeklyFrequency(
    val min: Int,
    val target: Int,
    val max: Int,
)

@Serializable
data class MovementPattern(
    val id: MovementPatternId,
    val domain: MovementDomain,
    val category: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val primaryCapacities: List<CapacityId>,
    val typicalMuscles: List<MuscleId>,
    val typicalJoints: List<JointId>,
    val functionalPurposeKey: LocalizationKey? = null,
    val recommendedWeeklyFrequency: WeeklyFrequency,
    val minimumEffectiveDoseSeconds: Int? = null,
    val recoveryCost: Int,
)

@Serializable
data class MovementPatternCollection(
    val schemaVersion: String,
    val patterns: List<MovementPattern>,
)
