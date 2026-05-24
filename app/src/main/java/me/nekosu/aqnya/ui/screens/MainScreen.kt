package me.nekosu.aqnya.ui.screens

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nekosu.aqnya.KeyUtils
import me.nekosu.aqnya.R
import me.nekosu.aqnya.ncore
import me.nekosu.aqnya.ui.navbar.FlutterNavBar
import me.nekosu.aqnya.util.AppPermission
import me.nekosu.aqnya.util.BottomNavItem
import me.nekosu.aqnya.util.CheckUpdate
import me.nekosu.aqnya.util.DebugPreferences
import me.nekosu.aqnya.util.MiuiPermissionUtils
import me.nekosu.aqnya.util.NavBarStyle
import me.nekosu.aqnya.util.rememberPermissionState

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

@Composable
fun NormalBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(index) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.titleRes),
                    )
                },
                label = { Text(stringResource(item.titleRes)) },
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    style: NavBarStyle,
) {
    when (style) {
        NavBarStyle.FLOATING -> {
            FloatingBottomNavigationBar(items, selectedIndex, onTabClick)
        }

        NavBarStyle.NORMAL -> {
            NormalBottomNavigationBar(items, selectedIndex, onTabClick)
        }

        NavBarStyle.FLUTTER -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val homeViewModel: HomeViewModel =
        viewModel(
            factory = HomeViewModelFactory(context.applicationContext as Application),
        )
    val showRules by DebugPreferences.showRulesFlow(context).collectAsState(initial = false)
    val navItems = remember(showRules) { BottomNavItem.items(showRules) }

    val navBarStyleValue by DebugPreferences.navBarStyleFlow(context).collectAsState(initial = 0)
    val navBarStyle = NavBarStyle.fromValue(navBarStyleValue)

    val miuiAppsPermState = rememberPermissionState(AppPermission.MIUI_GET_INSTALLED_APPS)
    val scope = rememberCoroutineScope()

    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { navItems.size },
        )
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == "main"

    val bottomNavAlpha by animateFloatAsState(
        targetValue = if (showBottomBar) 1f else 0f,
        animationSpec = tween(durationMillis = if (showBottomBar) 200 else 150),
        label = "bottomNavAlpha"
    )

    var navBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y < -8) navBarVisible = false
                    if (available.y > 8) navBarVisible = true
                    return Offset.Zero
                }
            }
        }

    LaunchedEffect(currentRoute) { navBarVisible = true }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "main") {
            delay(300)
            homeViewModel.refresh()
        }
    }

    LaunchedEffect(Unit) {
        if (MiuiPermissionUtils.isSupportedOnThisDevice(context) &&
            !MiuiPermissionUtils.isGranted(context)
        ) {
            miuiAppsPermState.launchRequest()
        }
    }

    LaunchedEffect(showRules) {
        if (!showRules) {
            val rulesIndex = navItems.indexOfFirst { it is BottomNavItem.FmacRules }
            if (pagerState.currentPage == rulesIndex) {
                pagerState.scrollToPage(0)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (navBarStyle == NavBarStyle.NORMAL) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = bottomNavAlpha
                        translationY = (1f - bottomNavAlpha) * size.height
                    }
                ) {
                    NormalBottomNavigationBar(navItems, currentPage) { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = "main",
                modifier = Modifier.fillMaxSize(),
            ) {
                composable("main") {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection),
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            val item = navItems[page]
                            when (item) {
                                BottomNavItem.Home -> {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onNavigateToApps = {
                                            val idx = navItems.indexOfFirst { it is BottomNavItem.History }
                                            if (idx >= 0) scope.launch { pagerState.animateScrollToPage(idx) }
                                        },
                                        onNavigateToRules = {
                                            val idx = navItems.indexOfFirst { it is BottomNavItem.FmacRules }
                                            if (idx >= 0) scope.launch { pagerState.animateScrollToPage(idx) }
                                        },
                                    )
                                }

                                BottomNavItem.History -> {
                                    HistoryScreen(
                                        navController = navController,
                                        extraBottomPadding = if (navBarStyle == NavBarStyle.FLOATING) 96.dp else 12.dp,
                                    )
                                }

                                BottomNavItem.FmacRules -> {
                                    RulesScreen()
                                }

                                BottomNavItem.Settings -> {
                                    SettingsScreen(navController)
                                }
                            }
                        }

                        if (navBarStyle == NavBarStyle.FLOATING) {
                            AnimatedVisibility(
                                visible = navBarVisible,
                                enter = fadeIn(tween(200)) + slideInVertically { it },
                                exit = fadeOut(tween(150)) + slideOutVertically { it },
                                modifier = Modifier.align(Alignment.BottomCenter),
                            ) {
                                FloatingBottomNavigationBar(navItems, currentPage) { index ->
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            }
                        } else if (navBarStyle == NavBarStyle.FLUTTER) {
                            AnimatedVisibility(
                                visible = navBarVisible,
                                enter = fadeIn(tween(200)) + slideInVertically { it },
                                exit = fadeOut(tween(150)) + slideOutVertically { it },
                                modifier = Modifier.align(Alignment.BottomCenter),
                            ) {
                                FlutterNavBar(
                                    modifier = Modifier.fillMaxWidth(),
                                    selectedIndex = currentPage,
                                    navBarVisible = navBarVisible,
                                    onTabSelected = { i ->
                                        if (i < navItems.size) scope.launch { pagerState.animateScrollToPage(i) }
                                    },
                                )
                            }
                        }
                    }
                }

                composable(
                    route = "about",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) },
                ) {
                    AboutScreen(navController)
                }

                composable(
                    route = "open_source",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) },
                ) {
                    OpenSourceScreen(navController)
                }

                composable(
                    route = "debug_settings",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) },
                ) {
                    DebugSettingsScreen(navController)
                }

                composable("app_detail/{packageName}") { backStackEntry ->
                    val pkg = backStackEntry.arguments?.getString("packageName")
                    if (pkg == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                        return@composable
                    }
                    val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory(context.applicationContext))

                    val app = appViewModel.allApps.find { it.packageName == pkg }

                    if (app != null) {
                        AppDetailScreen(
                            app = app,
                            config = appViewModel.appConfigs[pkg],
                            onSave = { appViewModel.setAppConfig(app, it) },
                            onBack = { navController.popBackStack() },
                            navController = navController,
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            appViewModel.loadApps()
                            if (appViewModel.allApps.none { it.packageName == pkg }) {
                                navController.popBackStack()
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingState()
                        }
                    }
                }

                composable("selinux_rules") {
                    SelinuxRulesPage(
                        onAddRule = { rule ->
                            ncore.addSelinuxRule(
                                src = rule.src.ifBlank { null },
                                tgt = rule.tgt.ifBlank { null },
                                cls = rule.cls.ifBlank { null },
                                perm = rule.perm.ifBlank { null },
                                effect = rule.effect,
                                invert = rule.invert,
                            )
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }

            CheckUpdate(owner = "aqnya", repo = "nekosu")
        }
    }
}

@Composable
fun KeyInputDialog(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var errorType by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.8f, animationSpec = tween(250)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200)),
    ) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = stringResource(R.string.dialog_key_set),
                    style = TextStyle(fontSize = 16.sp),
                )
            },
            text = {
                Column(
                    modifier =
                        Modifier
                            .verticalScroll(scrollState)
                            .fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_key_please_input))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            errorType = 0
                        },
                        label = { Text("ECC Key (PEM/Base64)", fontSize = 14.sp) },
                        placeholder = {
                            Text("-----BEGIN EC PRIVATE KEY-----...", fontSize = 14.sp)
                        },
                        singleLine = false,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 240.dp),
                        isError = errorType != 0,
                        supportingText = {
                            when (errorType) {
                                1 -> {
                                    Text(
                                        stringResource(R.string.dialog_key_input_no_empty),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }

                                2 -> {
                                    Text(
                                        stringResource(R.string.dialog_key_input_invalid),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedKey = inputText.trim()
                        errorType =
                            when {
                                trimmedKey.isBlank() -> 1
                                !KeyUtils.isValidECCKey(trimmedKey) -> 2
                                else -> 0
                            }
                        if (errorType == 0) {
                            KeyUtils.saveKey(context, trimmedKey)
                            onDismiss()
                        }
                    },
                ) {
                    Text(stringResource(R.string.dialog_key_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_key_later))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
