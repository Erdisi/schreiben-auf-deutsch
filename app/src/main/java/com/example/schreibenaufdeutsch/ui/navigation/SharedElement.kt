package com.example.schreibenaufdeutsch.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * A safe way to get the NavAnimatedContentScope that doesn't throw in previews
 * Note: We can't use try-catch around composables easily, so we rely on providing null
 * in a custom Local if we want to avoid the library's throwing one.
 */
val LocalSharedElementHelper = compositionLocalOf { false }
