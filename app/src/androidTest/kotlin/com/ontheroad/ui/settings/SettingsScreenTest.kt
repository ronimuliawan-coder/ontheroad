package com.ontheroad.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ratesStayLocalUntilValidSave() {
        var rates by mutableStateOf(DirectPricingRates())
        var writes by mutableIntStateOf(0)

        composeRule.setContent {
            OnTheRoadTheme(themeMode = ThemeMode.DARK) {
                SettingsScreenContent(
                    themeMode = ThemeMode.DARK,
                    onThemeModeSelected = {},
                    directPricingRates = rates,
                    onUpdateRates = {
                        writes++
                        rates = it
                    }
                )
            }
        }

        val baseFare = composeRule.onNodeWithTag("direct_base_fare").performScrollTo()
        baseFare.performTextClearance()
        val saveButton = composeRule.onNodeWithTag("direct_rates_save_button").performScrollTo()
        saveButton.assertIsNotEnabled()
        composeRule.runOnIdle {
            assertEquals(0, writes)
            assertEquals(10_000_00L, rates.baseFareAmountCents)
        }

        baseFare.performTextInput("92233720368547759")
        saveButton.assertIsNotEnabled()
        baseFare.performTextReplacement("12000")
        val includedDistance = composeRule.onNodeWithTag("direct_included_distance").performScrollTo()
        includedDistance.performTextReplacement("NaN")
        saveButton.assertIsNotEnabled()
        includedDistance.performTextReplacement("0.0")
        val detourFactor = composeRule.onNodeWithTag("direct_road_detour_factor").performScrollTo()
        detourFactor.performTextReplacement("Infinity")
        saveButton.assertIsNotEnabled()
        detourFactor.performTextReplacement("1.4")
        saveButton.assertIsEnabled()
        composeRule.runOnIdle { assertEquals(0, writes) }

        composeRule.onNodeWithTag("direct_rates_save_button")
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle {
            assertEquals(1, writes)
            assertEquals(12_000_00L, rates.baseFareAmountCents)
            assertEquals(3_500_00L, rates.ratePerKmAmountCents)
            assertEquals(15_000_00L, rates.minimumFareAmountCents)
            assertEquals(1.4, rates.roadDetourMultiplier, 0.001)
        }
    }

    @Test
    fun profileCanBeCreatedAndSelected() {
        var profiles by mutableStateOf(DirectPricingProfileSettings())

        composeRule.setContent {
            OnTheRoadTheme(themeMode = ThemeMode.DARK) {
                SettingsScreenContent(
                    themeMode = ThemeMode.DARK,
                    onThemeModeSelected = {},
                    directPricingRates = profiles.activeProfile.rates,
                    onUpdateRates = {},
                    directPricingProfiles = profiles,
                    onSelectProfile = { id -> profiles = profiles.copy(activeProfileId = id) },
                    onCreateProfile = { name ->
                        profiles = profiles.copy(
                            profiles = profiles.profiles + DirectPricingProfile(
                                id = "motorcycle",
                                name = name,
                                rates = profiles.activeProfile.rates
                            )
                        )
                    },
                    onDeleteProfile = {}
                )
            }
        }

        composeRule.onNodeWithTag("direct_profile_add").performScrollTo().performClick()
        composeRule.onNodeWithTag("direct_profile_name").performTextInput("Motorcycle / Courier")
        composeRule.onNodeWithTag("direct_profile_create_confirm").performClick()
        composeRule.onNodeWithText("Motorcycle / Courier").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals(2, profiles.profiles.size)
            assertEquals("motorcycle", profiles.activeProfileId)
            assertEquals(profiles.profiles.first().rates, profiles.activeProfile.rates)
        }
    }
}
