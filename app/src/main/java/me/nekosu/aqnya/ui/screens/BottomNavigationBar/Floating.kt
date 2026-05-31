package me.nekosu.aqnya.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.nekosu.aqnya.util.BottomNavItem

@Composable
fun FloatingBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    val containerColor =
                        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                    val itemWidth by animateDpAsState(
                        targetValue = if (selected) 88.dp else 48.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "navItemWidth",
                    )

                    Surface(
                        onClick = { onTabClick(index) },
                        shape = RoundedCornerShape(50),
                        color = containerColor,
                        modifier =
                            Modifier
                                .height(48.dp)
                                .width(itemWidth),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp),
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleRes),
                                    tint =
                                        if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    modifier = Modifier.size(22.dp),
                                )
                                AnimatedVisibility(
                                    visible = selected,
                                    enter = fadeIn(tween(200)),
                                    exit = fadeOut(tween(150)),
                                ) {
                                    Text(
                                        text = stringResource(item.titleRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
