package ke.ac.ku.ledgerly.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * Configuration object for shared transition animations.
 * Customize these values to change the feel of your transitions.
 */
object TransitionConfig {
    const val SCREEN_DURATION = 850
    const val SHARED_DURATION = 1050
    const val DETAILS_DURATION = 900

    val ArcLinearPunchEasing = CubicBezierEasing(0.00f, 0.00f, 0.0f, 1.0f)
    val CinematicEasing = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.02f)
    val PlayfulEasingTitle = CubicBezierEasing(0.15f, 0.9f, 0.25f, 1.05f)
    val PlayfulEasingSubtitle = CubicBezierEasing(0.20f, 0.8f, 0.25f, 1.05f)
}

/**
 * Base interface for navigation states used in shared transitions.
 * Implement this in your screen-specific state classes.
 */
interface TransitionScreenState

/**
 * Creates a direction-aware shared element configuration that enables transitions
 * between list and detail screens in both directions.
 *
 * @param transition The transition managing the state changes
 * @param listState The state representing the list screen
 * @param detailStateCheck Lambda to check if current state is a detail state
 */
@Stable
@OptIn(ExperimentalSharedTransitionApi::class)
fun <T : TransitionScreenState> createDirectionAwareSharedConfig(
    transition: Transition<T>,
    listState: T,
    detailStateCheck: (T) -> Boolean
): SharedTransitionScope.SharedContentConfig {
    return object : SharedTransitionScope.SharedContentConfig {
        override val SharedTransitionScope.SharedContentState.isEnabled: Boolean
            get() {
                val current = transition.currentState
                val target = transition.targetState
                val currentIsList = current == listState
                val targetIsDetail = detailStateCheck(target)
                val currentIsDetail = detailStateCheck(current)
                val targetIsList = target == listState

                return (currentIsList && targetIsDetail) || (currentIsDetail && targetIsList)
            }

        override val shouldKeepEnabledForOngoingAnimation: Boolean
            get() = false
    }
}

/**
 * Linear bounds transform for smooth shared element movement.
 *
 * @param durationMillis Duration of the animation
 * @param easing Easing function to use
 */
fun createLinearBoundsTransform(
    durationMillis: Int = TransitionConfig.SHARED_DURATION,
    easing: CubicBezierEasing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.15f)
): BoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis, easing = easing)
}

/**
 * Arc-based bounds transform that moves elements along an arc path.
 *
 * @param durationMillis Duration of the animation
 * @param arcMode The direction of the arc path
 * @param easing Easing function to use
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun createArcBoundsTransform(
    durationMillis: Int = TransitionConfig.SHARED_DURATION,
    arcMode: ArcMode = ArcMode.ArcAbove,
    easing: CubicBezierEasing = TransitionConfig.ArcLinearPunchEasing
): BoundsTransform = BoundsTransform { initial, target ->
    keyframes {
        this.durationMillis = durationMillis
        initial at 0 using arcMode using easing
        target at durationMillis
    }
}

/**
 * Specialized arc transform for title text elements.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun createTitleBoundsTransform(
    durationMillis: Int = TransitionConfig.SHARED_DURATION
): BoundsTransform = createArcBoundsTransform(
    durationMillis = durationMillis,
    arcMode = ArcMode.ArcAbove,
    easing = TransitionConfig.PlayfulEasingTitle
)

/**
 * Specialized arc transform for subtitle text elements.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun createSubtitleBoundsTransform(
    durationMillis: Int = TransitionConfig.SHARED_DURATION
): BoundsTransform = createArcBoundsTransform(
    durationMillis = durationMillis,
    arcMode = ArcMode.ArcBelow,
    easing = TransitionConfig.PlayfulEasingSubtitle
)

/**
 * Extension function to create standard forward transition spec (list -> detail).
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.createForwardTransition(
    screenDuration: Int = TransitionConfig.SCREEN_DURATION
) = (slideIntoContainer(
    AnimatedContentTransitionScope.SlideDirection.Left,
    animationSpec = tween(screenDuration, easing = TransitionConfig.CinematicEasing)
) + fadeIn(tween(screenDuration, easing = LinearOutSlowInEasing))
        + scaleIn(
    initialScale = 0.98f,
    animationSpec = tween(screenDuration, easing = TransitionConfig.CinematicEasing)
)) with (slideOutOfContainer(
    AnimatedContentTransitionScope.SlideDirection.Left,
    animationSpec = tween(screenDuration, easing = TransitionConfig.CinematicEasing)
) + fadeOut(tween(screenDuration - 150, easing = FastOutSlowInEasing)))

/**
 * Extension function to create standard backward transition spec (detail -> list).
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.createBackwardTransition(
    screenDuration: Int = TransitionConfig.SCREEN_DURATION - 80
) = (slideIntoContainer(
    AnimatedContentTransitionScope.SlideDirection.Right,
    animationSpec = tween(screenDuration, easing = TransitionConfig.CinematicEasing)
) + fadeIn(tween(screenDuration))) with (slideOutOfContainer(
    AnimatedContentTransitionScope.SlideDirection.Right,
    animationSpec = tween(screenDuration, easing = TransitionConfig.CinematicEasing)
) + fadeOut(tween(screenDuration + 70)))

/**
 * Helper to create a shared image element with standard configuration.
 *
 * @param key Unique key for this shared element
 * @param sharedConfig The shared content configuration
 * @param animatedVisibilityScope The animated visibility scope
 * @param boundsTransform Optional custom bounds transform
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.sharedImageElement(
    key: String,
    sharedConfig: SharedTransitionScope.SharedContentConfig,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = createLinearBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = modifier.sharedElement(
    sharedContentState = rememberSharedContentState(
        key = key,
        config = sharedConfig
    ),
    animatedVisibilityScope = animatedVisibilityScope,
    boundsTransform = boundsTransform
)

/**
 * Helper to create a shared text element with standard configuration.
 *
 * @param key Unique key for this shared element
 * @param sharedConfig The shared content configuration
 * @param animatedVisibilityScope The animated visibility scope
 * @param boundsTransform Optional custom bounds transform
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.sharedTextElement(
    key: String,
    sharedConfig: SharedTransitionScope.SharedContentConfig,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = createTitleBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = modifier
    .sharedElement(
        sharedContentState = rememberSharedContentState(
            key = key,
            config = sharedConfig
        ),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = boundsTransform
    )
    .skipToLookaheadSize()

/**
 * Helper to create shared bounds (container) with standard configuration.
 *
 * @param key Unique key for this shared bounds
 * @param sharedConfig The shared content configuration
 * @param animatedVisibilityScope The animated visibility scope
 * @param resizeMode The resize mode for the shared bounds
 * @param boundsTransform Optional custom bounds transform
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.sharedBoundsElement(
    key: String,
    sharedConfig: SharedTransitionScope.SharedContentConfig,
    animatedVisibilityScope: AnimatedVisibilityScope,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
    boundsTransform: BoundsTransform = createArcBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = modifier.sharedBounds(
    sharedContentState = rememberSharedContentState(
        key = key,
        config = sharedConfig
    ),
    animatedVisibilityScope = animatedVisibilityScope,
    resizeMode = resizeMode,
    boundsTransform = boundsTransform
)

/**
 * Data class to hold shared transition dependencies for cleaner function signatures.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
data class SharedTransitionDependencies(
    val sharedScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
    val sharedConfig: SharedTransitionScope.SharedContentConfig
)

/**
 * Helper extension to simplify creating shared elements with dependencies.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionDependencies.sharedImage(
    key: String,
    boundsTransform: BoundsTransform = createLinearBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = with(sharedScope) {
    sharedImageElement(
        key = key,
        sharedConfig = sharedConfig,
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = boundsTransform,
        modifier = modifier
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionDependencies.sharedText(
    key: String,
    boundsTransform: BoundsTransform = createTitleBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = with(sharedScope) {
    sharedTextElement(
        key = key,
        sharedConfig = sharedConfig,
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = boundsTransform,
        modifier = modifier
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionDependencies.sharedBounds(
    key: String,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
    boundsTransform: BoundsTransform = createArcBoundsTransform(),
    modifier: Modifier = Modifier
): Modifier = with(sharedScope) {
    sharedBoundsElement(
        key = key,
        sharedConfig = sharedConfig,
        animatedVisibilityScope = animatedVisibilityScope,
        resizeMode = resizeMode,
        boundsTransform = boundsTransform,
        modifier = modifier
    )
}