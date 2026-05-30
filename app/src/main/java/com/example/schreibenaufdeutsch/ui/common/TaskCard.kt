package com.example.schreibenaufdeutsch.ui.common

import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil.compose.AsyncImage
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.ui.navigation.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskCard(
    task: WritingTask,
    index: Int = 0,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = if (sharedTransitionScope != null) LocalNavAnimatedContentScope.current else null
    
    // Entrance animation
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = (index % 10) * 100, // Stagger based on index
                easing = FastOutSlowInEasing
            )
        )
    }

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 50.dp.toPx()
                scaleX = 0.95f + (animatedProgress.value * 0.05f)
                scaleY = 0.95f + (animatedProgress.value * 0.05f)
            }
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val imageModifier = Modifier.fillMaxSize()
                
                AsyncImage(
                    model = task.imageUrl ?: "https://images.unsplash.com/photo-1543002588-bfa74002ed7e",
                    contentDescription = null,
                    modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            imageModifier.sharedElement(
                                rememberSharedContentState(key = "task-image-${task.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else {
                        imageModifier
                    },
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                )
                
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(task.germanLevel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text(task.taskType, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    if (task.status == "Completed") {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text("Abgeschlossen", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (task.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (task.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
