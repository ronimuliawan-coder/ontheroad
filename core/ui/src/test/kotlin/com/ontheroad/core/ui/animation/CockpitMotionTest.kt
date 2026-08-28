package com.ontheroad.core.ui.animation

import androidx.compose.animation.core.Spring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CockpitMotionTest {

    @Test
    fun springQuickHasMediumStiffnessAndNoBouncy() {
        val spec = CockpitMotion.springQuick<Float>()
        assertEquals(Spring.StiffnessMedium, spec.stiffness, 0.001f)
        assertEquals(Spring.DampingRatioNoBouncy, spec.dampingRatio, 0.001f)
    }

    @Test
    fun springSmoothHasMediumLowStiffnessAndNoBouncy() {
        val spec = CockpitMotion.springSmooth<Float>()
        assertEquals(Spring.StiffnessMediumLow, spec.stiffness, 0.001f)
        assertEquals(Spring.DampingRatioNoBouncy, spec.dampingRatio, 0.001f)
    }

    @Test
    fun springGentleBounceHasLowStiffnessAndLowBouncy() {
        val spec = CockpitMotion.springGentleBounce<Float>()
        assertEquals(Spring.StiffnessLow, spec.stiffness, 0.001f)
        assertEquals(Spring.DampingRatioLowBouncy, spec.dampingRatio, 0.001f)
    }

    @Test
    fun transitionSpecsAreConfigured() {
        assertNotNull(CockpitMotion.ScreenSlideInRight)
        assertNotNull(CockpitMotion.ScreenSlideOutLeft)
        assertNotNull(CockpitMotion.ScreenSlideInLeft)
        assertNotNull(CockpitMotion.ScreenSlideOutRight)
        assertNotNull(CockpitMotion.NumericCounterRoll)
    }
}
