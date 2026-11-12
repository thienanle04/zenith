package com.zenith.app.core.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent

private val slideSpec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)

private val slideForward: ContentTransform =
    slideInHorizontally(slideSpec) { fullWidth -> fullWidth } togetherWith
        slideOutHorizontally(slideSpec) { fullWidth -> -fullWidth }

private val slideBack: ContentTransform =
    slideInHorizontally(slideSpec) { fullWidth -> -fullWidth } togetherWith
        slideOutHorizontally(slideSpec) { fullWidth -> fullWidth }

val immediateTransition: ContentTransform = EnterTransition.None togetherWith ExitTransition.None

/** Push slides in from the right; pop and predictive back reverse that slide. */
val slideMetadata: Map<String, Any> =
    metadata {
        put(NavDisplay.TransitionKey) { slideForward }
        put(NavDisplay.PopTransitionKey) { slideBack }
        put(NavDisplay.PredictivePopTransitionKey) { swipeEdge ->
            if (swipeEdge == NavigationEvent.EDGE_RIGHT) slideForward else slideBack
        }
    }
