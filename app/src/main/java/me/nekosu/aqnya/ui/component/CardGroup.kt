package me.nekosu.aqnya.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 粘连卡片分组容器
 * 与 AppList 的 getAdapterShape 逻辑一致
 */
fun groupShape(index: Int, total: Int, radius: Dp = 20.dp): Shape = when {
    total <= 1  -> RoundedCornerShape(radius)
    index == 0  -> RoundedCornerShape(topStart = radius, topEnd = radius)
    index == total - 1 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
    else        -> RoundedCornerShape(0.dp)
}

@Composable
fun CardGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
}

@Composable
fun CardItem(
    index: Int,
    total: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = groupShape(index, total),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

/**
 * Row 布局的 ListItem 替代组件，leading icon 始终垂直居中，
 * supporting 自动应用 bodyMedium 样式和 onSurfaceVariant 颜色。
 */
@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    headline: @Composable () -> Unit,
    supporting: @Composable (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            headline()
            supporting?.let { sup ->
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    sup()
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}
