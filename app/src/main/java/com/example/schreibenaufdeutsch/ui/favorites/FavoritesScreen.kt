package com.example.schreibenaufdeutsch.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.ui.common.TaskCard
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToPractice: (Long) -> Unit,
    selectedTaskId: Long? = null,
    viewModel: FavoritesViewModel = viewModel(
        factory = viewModelFactory { FavoritesViewModel(SchreibenApp.instance.taskRepository) }
    )
) {
    val tasks by viewModel.favoriteTasks.collectAsStateWithLifecycle()
    val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
    val levels = listOf("Alle", "A1", "A2", "B1", "B2", "C1")
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { 
                    Text(
                        "Lesezeichen",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levels) { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { viewModel.setLevelFilter(level) },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Keine Lesezeichen gefunden", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = tasks,
                        key = { _, task -> task.id }
                    ) { index, task ->
                        TaskCard(
                            task = task,
                            index = index,
                            isSelected = task.id == selectedTaskId,
                            onClick = { onNavigateToPractice(task.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(task) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    SchreibenAufDeutschTheme {
        FavoritesScreen({})
    }
}
