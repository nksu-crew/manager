package me.nekosu.aqnya.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Floating glass bar metrics, mirroring the Flutter manager's GlassTabBar. */
val GlassBottomBarHeight = 64.dp
val GlassBottomBarHorizontalPadding = 28.dp

/** Bottom scroll clearance used by the screens when the glass bar is shown. */
val GlassBottomBarContentPadding = 124.dp

/** Clearance for the stock Material 3 navigation bar (80 dp) plus a 16 dp gap. */
val StandardBottomBarContentPadding = 96.dp

/** Bottom scroll clearance selected by the current navigation bar style. */
val LocalBottomBarClearance = compositionLocalOf { GlassBottomBarContentPadding }

/**
 * Liquid-glass bottom bar backed by the `io.github.kyant0:backdrop` library.
 *
 * Layout and motion follow KernelSU's floating navigation bar: a 64 dp glass
 * pill with a draggable selection indicator, press scale and a translucent
 * indicator tint. The caller must wrap the content shown behind the bar with
 * `Modifier.layerBackdrop(backdrop)` and build the backdrop with
 * `rememberLayerBackdrop`.
 */
@Composable
fun GlassBottomNavigationBar(
    backdrop: Backdrop,
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.surfaceContainer.luminance() < 0.5f
    val surfaceColor = scheme.surfaceContainer.copy(alpha = 0.4f)
    val indicatorTint =
        if (isDark) {
            Color.White.copy(alpha = 0.1f)
        } else {
            Color.Black.copy(alpha = 0.1f)
        }

    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    val indicatorPosition = remember { Animatable(selectedIndex.toFloat()) }
    var previewIndex by remember { mutableIntStateOf(selectedIndex) }
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "glassBarPressScale",
    )

    LaunchedEffect(selectedIndex) {
        previewIndex = selectedIndex
        indicatorPosition.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        )
    }

    BoxWithConstraints(
        modifier =
            modifier
                .height(GlassBottomBarHeight)
                .fillMaxWidth()
                .pointerInput(items.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { pressed = true },
                        onDragEnd = {
                            pressed = false
                            val target =
                                indicatorPosition.value
                                    .roundToInt()
                                    .coerceIn(0, items.lastIndex)
                            previewIndex = target
                            onTabClick(target)
                            animationScope.launch {
                                indicatorPosition.animateTo(
                                    targetValue = target.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                                )
                            }
                        },
                        onDragCancel = {
                            pressed = false
                            previewIndex = selectedIndex
                            animationScope.launch {
                                indicatorPosition.animateTo(
                                    targetValue = selectedIndex.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val tabWidthPx =
                                (size.width - with(density) { 8.dp.toPx() }) / items.size
                            if (tabWidthPx > 0f) {
                                val next =
                                    (indicatorPosition.value + dragAmount / tabWidthPx)
                                        .coerceIn(0f, items.lastIndex.toFloat())
                                animationScope.launch { indicatorPosition.snapTo(next) }
                                previewIndex = next.roundToInt().coerceIn(0, items.lastIndex)
                            }
                        },
                    )
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(24f.dp.toPx(), 24f.dp.toPx())
                    },
                    highlight = { Highlight.Default.copy(alpha = 0.75f) },
                    shadow = {
                        Shadow(
                            radius = 10.dp,
                            color = Color.Black,
                            alpha = if (isDark) 0.2f else 0.1f,
                        )
                    },
                    layerBlock = {
                        scaleX = pressScale
                        scaleY = pressScale
                    },
                    onDrawSurface = { drawRect(surfaceColor) },
                )
                .clip(CircleShape)
                .padding(4.dp),
    ) {
        val tabWidth = maxWidth / items.size

        Box(
            modifier =
                Modifier
                    .offset(x = tabWidth * indicatorPosition.value)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {},
                        onDrawSurface = { drawRect(indicatorTint) },
                    ),
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                GlassBottomBarItem(
                    item = item,
                    selected = index == previewIndex,
                    selectedColor = scheme.primary,
                    unselectedColor = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { onTabClick(index) },
                )
            }
        }
    }
}

@Composable
private fun GlassBottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) selectedColor else unselectedColor
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "glassTabIconScale",
    )

    Column(
        modifier =
            modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Crossfade(
            targetState = selected,
            animationSpec = tween(180),
            label = "glassTabIcon",
        ) { isSelected ->
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = stringResource(item.titleRes),
                tint = contentColor,
                modifier =
                    Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
            )
        }

        Spacer(Modifier.height(1.dp))

        Text(
            text = stringResource(item.titleRes),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}
