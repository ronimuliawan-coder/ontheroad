package com.ontheroad.core.model

data class DirectPricingProfile(
    val id: String,
    val name: String,
    val rates: DirectPricingRates
)

data class DirectPricingProfileSettings(
    val profiles: List<DirectPricingProfile> = listOf(
        DirectPricingProfile(DEFAULT_PROFILE_ID, DEFAULT_PROFILE_NAME, DirectPricingRates())
    ),
    val activeProfileId: String = DEFAULT_PROFILE_ID
) {
    val activeProfile: DirectPricingProfile
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.first()

    companion object {
        const val DEFAULT_PROFILE_ID = "default"
        const val DEFAULT_PROFILE_NAME = "Default"
        const val MAX_PROFILE_NAME_LENGTH = 40
    }
}

fun DirectPricingProfile.isPersistable(): Boolean =
    id.isNotBlank() &&
        '\n' !in id &&
        name.trim().isNotEmpty() &&
        name.trim().length <= DirectPricingProfileSettings.MAX_PROFILE_NAME_LENGTH &&
        rates.isPersistable()
