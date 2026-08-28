package com.ontheroad.core.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * High-performance motion tokens and physics spring specifications
 * tailored for 120Hz/90Hz fluid rendering and in-vehicle glanceability.
 */
object CockpitMotion {

    /**
     * Crisp, responsive spring for immediate tactile actions (buttons, toggles).
     */
    fun <T> springQuick(visibilityThreshold: T? = null) = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = visibilityThreshold
    )

    /**
     * Smooth, fluid spring for screen transitions and large dashboard elements.
     */
    fun <T> springSmooth(visibilityThreshold: T? = null) = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = visibilityThreshold
    )

    /**
     * Subtle bounce for celebratory milestones (reaching targets, profitable runs).
     */
    fun <T> springGentleBounce(visibilityThreshold: T? = null) = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = visibilityThreshold
    )

    /**
     * Screen slide-in transition from right to left with fade.
     */
    val ScreenSlideInRight: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() },
        animationSpec = springSmooth(IntOffset.VisibilityThreshold)
    ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))

    /**
     * Screen slide-out transition from right to left with fade.
     */
    val ScreenSlideOutLeft: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
        animationSpec = springSmooth(IntOffset.VisibilityThreshold)
    ) + fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))

    /**
     * Screen slide-in transition from left to right (back navigation).
     */
    val ScreenSlideInLeft: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
        animationSpec = springSmooth(IntOffset.VisibilityThreshold)
    ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))

    /**
     * Screen slide-out transition from left to right (back navigation).
     */
    val ScreenSlideOutRight: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() },
        animationSpec = springSmooth(IntOffset.VisibilityThreshold)
    ) + fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))

    /**
     * Vertical roll transition for updating numeric metric cards (odometer, earnings, speed).
     */
    val NumericCounterRoll: ContentTransform = (
        slideInVertically(
            initialOffsetY = { height -> height / 2 },
            animationSpec = springQuick(IntOffset.VisibilityThreshold)
        ) + fadeIn(animationSpec = tween(150))
    ) togetherWith (
        slideOutVertically(
            targetOffsetY = { height -> -height / 2 },
            animationSpec = springQuick(IntOffset.VisibilityThreshold)
        ) + fadeOut(animationSpec = tween(150))
    )
}
