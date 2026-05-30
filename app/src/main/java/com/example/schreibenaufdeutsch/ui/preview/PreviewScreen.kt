package com.example.schreibenaufdeutsch.ui.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.remote.dto.GeneratedTaskDto
import com.example.schreibenaufdeutsch.data.remote.dto.VariationDto
import com.example.schreibenaufdeutsch.ui.common.SentenceCard
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.navigation.LocalSharedTransitionScope
import com.example.schreibenaufdeutsch.ui.practice.PracticeSentence
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme
import com.example.schreibenaufdeutsch.util.DynamicThemeManager
import com.example.schreibenaufdeutsch.util.LanguageUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PreviewScreen(
    generatedTask: GeneratedTaskDto? = null,
    taskId: Long? = null,
    level: String = "",
    tone: String = "",
    type: String = "",
    onBack: () -> Unit,
    onStartTask: (Long, Int) -> Unit,
    viewModel: PreviewViewModel = viewModel(
        factory = viewModelFactory { 
            PreviewViewModel(SchreibenApp.instance.taskRepository, SchreibenApp.instance.aiRepository) 
        }
    )
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = if (sharedTransitionScope != null) LocalNavAnimatedContentScope.current else null

    val existingTask by viewModel.existingTask.collectAsStateWithLifecycle()
    var selectedVariationIndex by remember { mutableIntStateOf(0) }
    
    val translatedTitle by viewModel.translatedTitle.collectAsStateWithLifecycle()
    val translatedDescription by viewModel.translatedDescription.collectAsStateWithLifecycle()
    
    val refreshingIndexes by viewModel.refreshingIndexes.collectAsStateWithLifecycle()
    val displayTaskState by viewModel.displayTaskState.collectAsStateWithLifecycle()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val currentOffset = scrollState.value
        isBottomBarVisible = currentOffset <= previousScrollOffset || currentOffset <= 50
        previousScrollOffset = currentOffset
    }

    BackHandler(onBack = onBack)

    val displayTask = remember(generatedTask, existingTask, displayTaskState) {
        if (displayTaskState != null) {
            displayTaskState
        } else if (generatedTask != null) {
            viewModel.setDisplayTask(generatedTask)
            generatedTask
        } else if (existingTask != null) {
            val task = GeneratedTaskDto(
                title = existingTask!!.title,
                description = existingTask!!.description,
                image_query = "",
                variations = existingTask!!.variations.map { v ->
                    VariationDto(v.text, v.translation)
                }
            )
            viewModel.setDisplayTask(task)
            task
        } else {
            null
        }
    }

    val displayLevel = existingTask?.germanLevel ?: level
    val displayType = existingTask?.taskType ?: type
    val displayTone = existingTask?.tone ?: tone

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }
    
    LaunchedEffect(displayTask) {
        if (translatedTitle.isEmpty() && displayTask != null) {
            viewModel.translateTask(displayTask.title, displayTask.description)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text(
                                displayLevel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            displayTask?.title ?: "Laden...",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                "Gespeichert",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (displayTask == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                ) {
                    val imageUrl = remember(existingTask, displayTask) {
                        if (existingTask?.imageUrl != null) {
                            existingTask?.imageUrl
                        } else if (displayTask.image_query.isNotBlank()) {
                            val encodedQuery = java.net.URLEncoder.encode(displayTask.image_query, "UTF-8")
                            "https://image.pollinations.ai/prompt/$encodedQuery?nologo=true&seed=42&model=flux"
                        } else {
                            "https://images.unsplash.com/photo-1543002588-bfa74002ed7e"
                        }
                    }

                    val imageModifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null && (taskId != null || generatedTask != null)) {
                            val elementId = taskId ?: -1L
                            with(sharedTransitionScope) {
                                imageModifier.sharedElement(
                                    rememberSharedContentState(key = "task-image-$elementId"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else {
                            imageModifier
                        },
                        contentScale = ContentScale.Crop,
                        onSuccess = { state ->
                            DynamicThemeManager.updateSeedColorFromBitmap(state.result.drawable.run {
                                if (this is android.graphics.drawable.BitmapDrawable) bitmap else null
                            })
                        }
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Text(
                                    displayTask.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Serif
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    lineHeight = 28.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                if (translatedTitle.isNotEmpty()) {
                                    Text(
                                        translatedTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    displayLevel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    displayType,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            displayTask.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Serif
                            ),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )

                        if (translatedDescription.isNotEmpty() || displayTask.description.contains("(")) {
                            val englishDesc = if (translatedDescription.isNotEmpty()) {
                                translatedDescription
                            } else {
                                val start = displayTask.description.indexOf('(')
                                val end = displayTask.description.lastIndexOf(')')
                                if (start != -1 && end != -1 && end > start) {
                                    displayTask.description.substring(start + 1, end)
                                } else {
                                    ""
                                }
                            }
                            
                            if (englishDesc.isNotEmpty()) {
                                Text(
                                    englishDesc,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "Antwort auswählen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth(),
                            space = 0.dp
                        ) {
                            displayTask.variations.forEachIndexed { index, _ ->
                                SegmentedButton(
                                    selected = selectedVariationIndex == index,
                                    onClick = { selectedVariationIndex = index },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = displayTask.variations.size),
                                    icon = { SegmentedButtonDefaults.Icon(selectedVariationIndex == index) },
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        inactiveContainerColor = Color.Transparent
                                    )
                                ) {
                                    Text("Option ${index + 1}")
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(Modifier.height(24.dp))

                        val currentVariation = displayTask.variations.getOrNull(selectedVariationIndex)
                        if (currentVariation != null) {
                            val sentences = remember(currentVariation) {
                                val germanSentences = LanguageUtils.splitIntoSentences(currentVariation.text)
                                val translationSentences = LanguageUtils.splitIntoSentences(currentVariation.translation)
                                
                                germanSentences.mapIndexed { index, german ->
                                    PracticeSentence(
                                        index = index,
                                        germanText = german.trim(),
                                        translation = translationSentences.getOrNull(index)?.trim() ?: "",
                                        isCompleted = true
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                sentences.forEach { sentence ->
                                    SentenceCard(
                                        sentence = sentence,
                                        onSpeak = { viewModel.speak(sentence.germanText) },
                                        onRefreshSentence = {
                                            viewModel.refreshSentence(
                                                displayTask,
                                                selectedVariationIndex,
                                                sentence.index,
                                                displayLevel,
                                                displayTone
                                            )
                                        },
                                        onEditSentence = { newText ->
                                            viewModel.updateSentence(
                                                displayTask,
                                                selectedVariationIndex,
                                                sentence.index,
                                                newText
                                            )
                                        },
                                        isRefreshing = refreshingIndexes.contains(sentence.index),
                                        allowTranslation = true,
                                        allowSpeak = true
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }

                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = {
                                if (taskId != null) {
                                    onStartTask(taskId, selectedVariationIndex)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = taskId != null,
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            val buttonText = if (existingTask?.status == "Completed") "Wiederholen" else "Aufgabe starten"
                            Text(buttonText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Aufgabe löschen") },
            text = { Text("Möchten Sie diese Aufgabe wirklich dauerhaft aus Ihrer Bibliothek entfernen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingTask?.let {
                            viewModel.deleteTask(it)
                            showDeleteConfirmation = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreenPreview() {
    SchreibenAufDeutschTheme {
        PreviewScreen(
            generatedTask = GeneratedTaskDto(
                title = "Beschwerde: Defektes Smartphone",
                description = "Sie haben online ein Smartphone gekauft, aber es ist defekt angekommen. Schreiben Sie eine E-Mail an den Kundenservice.",
                variations = listOf(
                    VariationDto(
                        "Sehr geehrte Damen und Herren,\n\nhiermit möchte ich mich über das Smartphone beschweren, das ich am letzten Dienstag bei Ihnen bestellt habe.",
                        "Dear Sirs and Madames,\n\nI would like to complain about the smartphone that I ordered on the last Tuesday."
                    ),
                    VariationDto("Hallo Team...", "Hello Team...")
                )
            ),
            level = "B2",
            tone = "Formal",
            type = "E-Mail",
            onBack = {},
            onStartTask = { _, _ -> }
        )
    }
}
