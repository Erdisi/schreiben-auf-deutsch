package com.example.schreibenaufdeutsch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    fun navigate(route: Destination) {
        if (route is Destination.Home || route is Destination.Favorites) {
            // This is a top level route, just switch to it
            state.topLevelRoute = route
        } else {
            val currentStack = state.backStacks[state.topLevelRoute]
            // If we are navigating to a destination that is already at the top of the stack,
            // we should probably replace it or just not add it again to avoid issues with StateFlows.
            if (currentStack?.lastOrNull() == route) {
                return
            }
            currentStack?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        
        // If we have more than just the start destination in the current backstack, pop it.
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (state.topLevelRoute != state.startRoute) {
            // If we are at the root of a secondary top-level route (e.g., Favorites),
            // and the user presses back, return to the start route (Home).
            state.topLevelRoute = state.startRoute
        }
    }

    fun popToRoot() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        while (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        }
    }
}

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
fun rememberNavigationState(
    startRoute: Destination,
    topLevelRoutes: Set<Destination>
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    // Create a back stack for each top level route.
    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

class NavigationState(
    val startRoute: Destination,
    topLevelRoute: MutableState<Destination>,
    val backStacks: Map<Destination, NavBackStack<NavKey>>
) {
    var topLevelRoute: Destination by topLevelRoute

    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): List<NavEntry<NavKey>> {
        val decoratedEntries = mutableMapOf<Destination, List<NavEntry<NavKey>>>()
        
        for (topLevel in backStacks.keys) {
            val stack = backStacks[topLevel]!!
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            )
            decoratedEntries[topLevel] = rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider
            )
        }

        return getTopLevelRoutesInUse()
            .flatMap { decoratedEntries[it] ?: emptyList() }
    }

    private fun getTopLevelRoutesInUse(): List<Destination> =
        if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}
