package me.nekosu.aqnya.ui.screens

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewQuilt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.nekosu.aqnya.R
import me.nekosu.aqnya.ui.component.CardGroup
import me.nekosu.aqnya.ui.component.CardItem
import me.nekosu.aqnya.ui.component.ListRow
import me.nekosu.aqnya.util.DebugPreferences
import me.nekosu.aqnya.util.LocaleHelper
import me.nekosu.aqnya.util.LogUtils
import me.nekosu.aqnya.util.NavBarStyle
import me.nekosu.aqnya.util.PagerAnimationStyle

enum class ThemeMode(
    @param:StringRes val titleRes: Int,
    val value: Int,
) {
    SYSTEM(R.string.theme_system, 0),
    LIGHT(R.string.theme_light, 1),
    DARK(R.string.theme_dark, 2),
    ;

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
    }
}

enum class ThemeColor(
    val label: String,
    val value: Int,
) {
    MATERIAL_YOU("Material You", 0),
    CATPPUCCIN_BLUE("Catppuccin Blue", 1),
    CATPPUCCIN_LAVENDER("Catppuccin Lavender", 2),
    CATPPUCCIN_GREEN("Catppuccin Green", 3),
    ;

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: MATERIAL_YOU
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val mContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val themeValue by DebugPreferences.themeModeFlow(mContext).collectAsState(initial = 0)
    val currentThemeMode = ThemeMode.fromValue(themeValue)

    val navBarStyleValue by DebugPreferences.navBarStyleFlow(mContext).collectAsState(initial = 2)
    val currentNavBarStyle = NavBarStyle.fromValue(navBarStyleValue)

    val pagerAnimValue by DebugPreferences.pagerAnimationStyleFlow(mContext).collectAsState(initial = 0)
    val currentPagerAnimStyle = PagerAnimationStyle.fromValue(pagerAnimValue)

    val themeColorValue by DebugPreferences.themeColorFlow(mContext).collectAsState(initial = 0)
    val currentThemeColor = ThemeColor.fromValue(themeColorValue)

    val amoledEnabled by DebugPreferences.amoledFlow(mContext).collectAsState(initial = false)

    val currentLang =
        LocaleHelper
            .savedLanguageTag(mContext)
            .ifBlank { "" }
    val currentLangLabel =
        LocaleHelper.availableLanguages
            .find { it.tag == currentLang }
            ?.let { stringResource(it.labelRes) }
            ?: stringResource(R.string.language_system)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets =
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
            ),
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
                    .padding(bottom = 96.dp)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── 语言 ──
            LanguageSection(
                currentLangLabel = currentLangLabel,
                onLanguageChange = { tag ->
                    LocaleHelper.saveLanguageTag(mContext, tag)
                    (mContext as? Activity)?.let { LocaleHelper.applyLanguage(it) }
                },
            )

            // ── 外观 ──
            AppearanceSection(
                currentThemeMode = currentThemeMode,
                currentNavBarStyle = currentNavBarStyle,
                currentPagerAnimStyle = currentPagerAnimStyle,
                currentThemeColor = currentThemeColor,
                amoledEnabled = amoledEnabled,
                onThemeChange = { mode ->
                    scope.launch { DebugPreferences.setThemeMode(mContext, mode.value) }
                },
                onNavBarStyleChange = { style ->
                    scope.launch { DebugPreferences.setNavBarStyle(mContext, style.value) }
                },
                onPagerAnimationChange = { style ->
                    scope.launch { DebugPreferences.setPagerAnimationStyle(mContext, style.value) }
                },
                onThemeColorChange = { color ->
                    scope.launch { DebugPreferences.setThemeColor(mContext, color.value) }
                },
                onAmoledChange = { enabled ->
                    scope.launch { DebugPreferences.setAmoled(mContext, enabled) }
                },
            )

            // ── 工具 ──
            ToolsSection(
                onExportLog = { LogUtils.exportLogs(mContext) },
                onDebugClick = { navController.navigate("debug_settings") },
            )

            // ── 关于 ──
            AboutSection(
                onAboutClick = { navController.navigate("about") },
            )

            Spacer(Modifier)
        }
    }
}

// ────────────────────────────────────────────────────────────
// 语言
// ────────────────────────────────────────────────────────────

@Composable
private fun LanguageSection(
    currentLangLabel: String,
    onLanguageChange: (String) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    CardGroup {
        CardItem(index = 0, total = 1) {
            ListRow(
                modifier = Modifier.clickable { menuExpanded = true },
                icon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
                headline = {
                    Text(
                        stringResource(R.string.language_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supporting = { Text(currentLangLabel) },
                trailing = {
                    Box {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            LocaleHelper.availableLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(lang.labelRes),
                                            fontWeight =
                                                if (currentLangLabel ==
                                                    stringResource(lang.labelRes)
                                                ) {
                                                    FontWeight.SemiBold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onLanguageChange(lang.tag)
                                    },
                                    trailingIcon = {
                                        if (currentLangLabel == stringResource(lang.labelRes)) {
                                            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 外观 — 主题模式 + 主题色 + AMOLED + 导航栏样式
// ────────────────────────────────────────────────────────────

@Composable
private fun AppearanceSection(
    currentThemeMode: ThemeMode,
    currentNavBarStyle: NavBarStyle,
    currentPagerAnimStyle: PagerAnimationStyle,
    currentThemeColor: ThemeColor,
    amoledEnabled: Boolean,
    onThemeChange: (ThemeMode) -> Unit,
    onNavBarStyleChange: (NavBarStyle) -> Unit,
    onPagerAnimationChange: (PagerAnimationStyle) -> Unit,
    onThemeColorChange: (ThemeColor) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
) {
    var themeMenuExpanded by remember { mutableStateOf(false) }
    var themeColorMenuExpanded by remember { mutableStateOf(false) }
    var navBarStyleMenuExpanded by remember { mutableStateOf(false) }
    var pagerAnimMenuExpanded by remember { mutableStateOf(false) }

    CardGroup {
        // 主题模式
        CardItem(index = 0, total = 5) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { themeMenuExpanded = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                headlineContent = {
                    Text(
                        stringResource(R.string.theme_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = { Text(stringResource(currentThemeMode.titleRes)) },
                trailingContent = {
                    Box {
                        DropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(mode.titleRes),
                                            fontWeight = if (currentThemeMode == mode) FontWeight.SemiBold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        themeMenuExpanded = false
                                        onThemeChange(mode)
                                    },
                                    trailingIcon = {
                                        if (currentThemeMode == mode) {
                                            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }

        // 主题色
        CardItem(index = 1, total = 5) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { themeColorMenuExpanded = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                headlineContent = {
                    Text(
                        stringResource(R.string.settings_theme_color),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = { Text(currentThemeColor.label) },
                trailingContent = {
                    Box {
                        DropdownMenu(
                            expanded = themeColorMenuExpanded,
                            onDismissRequest = { themeColorMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            ThemeColor.entries.forEach { color ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            color.label,
                                            fontWeight =
                                                if (currentThemeColor ==
                                                    color
                                                ) {
                                                    FontWeight.SemiBold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                        )
                                    },
                                    onClick = {
                                        themeColorMenuExpanded = false
                                        onThemeColorChange(color)
                                    },
                                    trailingIcon = {
                                        if (currentThemeColor == color) Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }

        // AMOLED 纯黑
        CardItem(index = 2, total = 5) {
            ListRow(
                modifier = Modifier.toggleable(value = amoledEnabled, role = Role.Switch, onValueChange = onAmoledChange),
                icon = { Icon(Icons.Outlined.PhoneAndroid, contentDescription = null) },
                headline = {
                    Text(
                        stringResource(R.string.settings_amoled_black),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supporting = { Text(stringResource(R.string.settings_amoled_black_summary)) },
                trailing = { Switch(checked = amoledEnabled, onCheckedChange = onAmoledChange) },
            )
        }

        // 导航栏样式
        CardItem(index = 3, total = 5) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { navBarStyleMenuExpanded = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    Icon(Icons.AutoMirrored.Outlined.ViewQuilt, contentDescription = null)
                },
                headlineContent = {
                    Text(
                        stringResource(R.string.navbar_style_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = { Text(stringResource(currentNavBarStyle.titleRes)) },
                trailingContent = {
                    Box {
                        DropdownMenu(
                            expanded = navBarStyleMenuExpanded,
                            onDismissRequest = { navBarStyleMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            NavBarStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(style.titleRes),
                                            fontWeight = if (currentNavBarStyle == style) FontWeight.SemiBold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        navBarStyleMenuExpanded = false
                                        onNavBarStyleChange(style)
                                    },
                                    trailingIcon = {
                                        if (currentNavBarStyle == style) {
                                            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }

        // 页面切换动画
        CardItem(index = 4, total = 5) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { pagerAnimMenuExpanded = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Outlined.Science, contentDescription = null) },
                headlineContent = {
                    Text(
                        stringResource(R.string.pager_anim_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = { Text(stringResource(currentPagerAnimStyle.titleRes)) },
                trailingContent = {
                    Box {
                        DropdownMenu(
                            expanded = pagerAnimMenuExpanded,
                            onDismissRequest = { pagerAnimMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            PagerAnimationStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(style.titleRes),
                                            fontWeight = if (currentPagerAnimStyle == style) FontWeight.SemiBold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        pagerAnimMenuExpanded = false
                                        onPagerAnimationChange(style)
                                    },
                                    trailingIcon = {
                                        if (currentPagerAnimStyle == style) {
                                            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 工具 — 导出日志 + 开发者选项（粘连卡片）
// ────────────────────────────────────────────────────────────

@Composable
private fun ToolsSection(
    onExportLog: () -> Unit,
    onDebugClick: () -> Unit,
) {
    CardGroup {
        CardItem(index = 0, total = 2) {
            ListRow(
                modifier = Modifier.clickable { onExportLog() },
                icon = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                headline = {
                    Text(
                        stringResource(R.string.export_log),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supporting = { Text(stringResource(R.string.export_log_describe)) },
            )
        }

        CardItem(index = 1, total = 2) {
            ListRow(
                modifier = Modifier.clickable { onDebugClick() },
                icon = { Icon(Icons.Outlined.Science, contentDescription = null) },
                headline = {
                    Text(
                        stringResource(R.string.settings_debug),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supporting = { Text(stringResource(R.string.settings_debug_summary)) },
                trailing = {
                    Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 关于 — 单张独立卡片（全圆角）
// ────────────────────────────────────────────────────────────

@Composable
private fun AboutSection(onAboutClick: () -> Unit) {
    CardGroup {
        CardItem(index = 0, total = 1) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable { onAboutClick() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                headlineContent = {
                    Text(
                        stringResource(R.string.about),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}
