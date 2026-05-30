package com.example.schreibenaufdeutsch.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed as lazyItemsIndexed
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.ui.common.TaskCard
import com.example.schreibenaufdeutsch.ui.common.Template
import com.example.schreibenaufdeutsch.ui.common.allTemplates
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreate: (String?) -> Unit,
    onNavigateToPractice: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    selectedTaskId: Long? = null,
    viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(SchreibenApp.instance.taskRepository) }
    ),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for floating nav
        ) {
            item {
                GreetingHeader(onSettingsClick = onNavigateToSettings)
            }

            item {
                HeroCard { onNavigateToCreate(null) }
            }

            item {
                SectionHeader(
                    title = "Schnellstart",
                    onSeeAll = onNavigateToLibrary
                )
            }

            item {
                QuickStartGrid(onTemplateClick = onNavigateToCreate)
            }

            if (tasks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Deine Aufgaben",
                        showSeeAll = false
                    )
                }

                lazyItemsIndexed(
                    items = tasks,
                    key = { _, task -> task.id }
                ) { index, task ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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

@Composable
private fun GreetingHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Hallo! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Bereit, heute besser zu schreiben?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HeroCard(onAction: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    }

    val contentColor = if (isDark) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = contentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Erste Schritte",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Lerne Deutsch\ndurch Schreiben",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 32.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Erstelle deine erste Übung mit KI oder wähle ein beliebtes Thema.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.85f)
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = contentColor,
                            contentColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Eigene Aufgabe erstellen",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Placeholder for the 3D illustration
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝", fontSize = 64.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit = {},
    showSeeAll: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (showSeeAll) {
            TextButton(onClick = onSeeAll) {
                Text(
                    "Alle anzeigen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStartGrid(onTemplateClick: (String) -> Unit) {
    val templates = allTemplates.take(4)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickStartCard(templates[0], modifier = Modifier.weight(1f), onClick = { onTemplateClick(templates[0].topic) })
            QuickStartCard(templates[1], modifier = Modifier.weight(1f), onClick = { onTemplateClick(templates[1].topic) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickStartCard(templates[2], modifier = Modifier.weight(1f), onClick = { onTemplateClick(templates[2].topic) })
            QuickStartCard(templates[3], modifier = Modifier.weight(1f), onClick = { onTemplateClick(templates[3].topic) })
        }
    }
}

@Composable
private fun QuickStartCard(
    item: Template,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(item.icon, fontSize = 20.sp)
                    }
                }
                
                Column {
                    Text(
                        item.topic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        item.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SchreibenAufDeutschTheme {
        HomeScreen(
            onNavigateToCreate = {},
            onNavigateToPractice = {},
            onNavigateToSettings = {},
            onNavigateToLibrary = {}
        )
    }
}
