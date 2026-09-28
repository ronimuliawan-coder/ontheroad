package com.ontheroad.core.model

/**
 * Pure Kotlin representation of an address search result / geocoded location.
 *
 * Adheres to ARCH-001: Zero Android framework dependencies in the model layer.
 */
data class AddressSuggestion(
    val title: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double
)
