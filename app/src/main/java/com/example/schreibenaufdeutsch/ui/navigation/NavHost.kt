package com.example.schreibenaufdeutsch.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.schreibenaufdeutsch.ui.create.CreateScreen
import com.example.schreibenaufdeutsch.ui.favorites.FavoritesScreen
import com.example.schreibenaufdeutsch.ui.home.HomeScreen
import com.example.schreibenaufdeutsch.ui.library.LibraryScreen
import com.example.schreibenaufdeutsch.ui.practice.PracticeScreen
import com.example.schreibenaufdeutsch.ui.preview.PreviewScreen
import com.example.schreibenaufdeutsch.ui.score.TaskScoreScreen
import com.example.schreibenaufdeutsch.ui.settings.SettingsScreen
import com.example.schreibenaufdeutsch.util.DynamicThemeManager

@Composable
private fun HomeDetailPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Wähle eine Aufgabe aus",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavHost() {
    val navigationState = rememberNavigationState(
        startRoute = Destination.Home,
        topLevelRoutes = setOf(Destination.Home, Destination.Favorites),
    )
    val navigator = remember { Navigator(navigationState) }

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    SharedTransitionLayout {
        val entryProvider = entryProvider<NavKey> {
            entry<Destination.Home>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        HomeDetailPlaceholder()
                    }
                )
            ) {
                val backStack = navigationState.backStacks[Destination.Home]
                val selectedTaskId = remember(backStack) {
                    backStack?.asSequence()?.mapNotNull { entry ->
                        when (entry) {
                            is Destination.PreviewTask -> entry.taskId
                            is Destination.PracticeTask -> entry.taskId
                            is Destination.TaskScore -> entry.taskId
                            else -> null
                        }
                    }?.lastOrNull()
                }

                HomeScreen(
                    onNavigateToCreate = { topic -> navigator.navigate(Destination.CreateTask(initialTopic = topic)) },
                    onNavigateToPractice = { id -> navigator.navigate(Destination.PreviewTask(taskId = id)) },
                    onNavigateToSettings = { navigator.navigate(Destination.Settings) },
                    onNavigateToLibrary = { navigator.navigate(Destination.Library) },
                    selectedTaskId = selectedTaskId
                )
            }
            entry<Destination.Favorites>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        HomeDetailPlaceholder()
                    }
                )
            ) {
                val backStack = navigationState.backStacks[Destination.Favorites]
                val selectedTaskId = remember(backStack) {
                    backStack?.asSequence()?.mapNotNull { entry ->
                        when (entry) {
                            is Destination.PreviewTask -> entry.taskId
                            is Destination.PracticeTask -> entry.taskId
                            is Destination.TaskScore -> entry.taskId
                            else -> null
                        }
                    }?.lastOrNull()
                }

                FavoritesScreen(
                    onNavigateToPractice = { id -> navigator.navigate(Destination.PreviewTask(taskId = id)) },
                    selectedTaskId = selectedTaskId
                )
            }
            entry<Destination.Library>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                LibraryScreen(
                    onBack = { navigator.goBack() },
                    onTemplateClick = { topic ->
                        navigator.navigate(Destination.CreateTask(initialTopic = topic))
                    }
                )
            }
            entry<Destination.CreateTask>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                CreateScreen(
                    initialTopic = key.initialTopic,
                    onBack = { navigator.goBack() },
                    onTaskGenerated = { taskId, level, tone, type ->
                        navigator.goBack() // Remove CreateScreen from backstack
                        navigator.navigate(Destination.PreviewTask(taskId = taskId, level = level, tone = tone, type = type))
                    }
                )
            }
            entry<Destination.PreviewTask>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                PreviewScreen(
                    generatedTask = key.generatedTask,
                    taskId = key.taskId,
                    level = key.level,
                    tone = key.tone,
                    type = key.type,
                    onBack = { navigator.goBack() },
                    onStartTask = { id, variationIndex ->
                        navigator.navigate(Destination.PracticeTask(id, variationIndex))
                    }
                )
            }
            entry<Destination.PracticeTask>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                PracticeScreen(
                    taskId = key.taskId,
                    variationIndex = key.variationIndex,
                    onBack = {
                        navigator.goBack()
                    },
                    onFinish = { sentencesJson ->
                        navigator.navigate(Destination.TaskScore(key.taskId, key.variationIndex, sentencesJson))
                    }
                )
            }
            entry<Destination.TaskScore>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                TaskScoreScreen(
                    taskId = key.taskId,
                    variationIndex = key.variationIndex,
                    sentencesJson = key.sentencesJson,
                    onBack = {
                        navigator.popToRoot()
                    },
                    onRedo = {
                        navigator.goBack() // Remove TaskScore
                        navigator.navigate(Destination.PracticeTask(key.taskId, key.variationIndex))
                    }
                )
            }
            entry<Destination.Settings>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                SettingsScreen(onBack = { navigator.goBack() })
            }
        }

        val currentStack = navigationState.backStacks[navigationState.topLevelRoute]
        val showNavBar = currentStack?.size == 1

        // Reset dynamic theme when returning to Home root
        LaunchedEffect(showNavBar) {
            if (showNavBar) {
                DynamicThemeManager.reset()
            }
        }

        // NavigationSuiteScaffold(
        //    layoutType = if (showNavBar) NavigationSuiteType.NavigationBar else NavigationSuiteType.None,
        //    navigationSuiteItems = { ... }
        // )
        
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                NavDisplay(
                    backStack = navigationState.backStacks[navigationState.topLevelRoute]!!,
                    onBack = { navigator.goBack() },
                    sceneStrategies = listOf(listDetailStrategy),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    entryProvider = entryProvider,
                    popTransitionSpec = {
                        slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
                    },
                    predictivePopTransitionSpec = { _ ->
                        slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
                    }
                )
            }

            if (showNavBar) {
                FloatingNavBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    currentRoute = navigationState.topLevelRoute,
                    onRouteSelected = { navigator.navigate(it) },
                )
            }
        }
    }
}

@Composable
fun FloatingNavBar(
    modifier: Modifier = Modifier,
    currentRoute: NavKey,
    onRouteSelected: (Destination) -> Unit
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(64.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingNavItem(
                selected = currentRoute == Destination.Home,
                onClick = { onRouteSelected(Destination.Home) },
                icon = Icons.Default.Home,
                label = "Home"
            )
            FloatingNavItem(
                selected = currentRoute == Destination.Favorites,
                onClick = { onRouteSelected(Destination.Favorites) },
                icon = Icons.Default.Bookmark,
                label = "Lesezeichen"
            )
        }
    }
}

@Composable
fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
