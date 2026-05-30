package com.example.schreibenaufdeutsch.ui.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.schreibenaufdeutsch.R
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.ui.common.SentenceCard
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    taskId: Long,
    variationIndex: Int = 0,
    onBack: () -> Unit,
    onFinish: (String) -> Unit,
    viewModel: PracticeViewModel = viewModel(
        factory = viewModelFactory { 
            PracticeViewModel(SchreibenApp.instance.taskRepository, SchreibenApp.instance.aiRepository) 
        }
    )
) {
    val task by viewModel.task.collectAsStateWithLifecycle()
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()
    
    val allAttempted = remember(sentences) { sentences.isNotEmpty() && sentences.all { it.isCompleted } }
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.submissionResult.collect { isCorrect ->
            if (isCorrect) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    LaunchedEffect(taskId, variationIndex) {
        viewModel.loadTask(taskId, variationIndex)
    }

    LaunchedEffect(currentIndex) {
        if (sentences.isNotEmpty()) {
            listState.animateScrollToItem(currentIndex)
            userInput = sentences.getOrNull(currentIndex)?.lastInput ?: ""
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onFinish(viewModel.getSentencesJson())
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    title = {
                        Column {
                            Text(
                                task?.title ?: stringResource(R.string.loading), 
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(task?.taskType ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        task?.let {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    it.germanLevel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
                if (task == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.practice_progress), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.practice_sentence_count, currentIndex, sentences.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sentences.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(
                                    color = if (index <= currentIndex) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    items(sentences, key = { it.index }) { sentence ->
                        SentenceCard(
                            sentence = sentence,
                            currentInput = if (sentence.isCurrent) userInput else "",
                            onUserInputChange = { 
                                userInput = it
                                viewModel.clearFeedback()
                            },
                            onSend = {
                                if (userInput.isNotBlank()) {
                                    viewModel.submitSentence(userInput)
                                }
                            },
                            onSpeak = { viewModel.speak(sentence.germanText) },
                            onGetFeedback = { viewModel.getAiFeedback(sentence.index) },
                            onClick = { viewModel.selectSentence(sentence.index) },
                            allowSpeak = true,
                            focusRequester = if (sentence.isCurrent) focusRequester else null
                        )
                    }
                }
            }

            // Floating elements at the bottom with a faded gradient background
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // German Umlaut Bar
                    UmlautBar(
                        onUmlautClick = { umlaut ->
                            userInput += umlaut
                            viewModel.clearFeedback()
                        }
                    )
                    
                    if (allAttempted) {
                        Button(
                            onClick = { viewModel.completeTask() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sentences.any { it.hadError }) 
                                    MaterialTheme.colorScheme.secondary 
                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            val text = if (sentences.any { it.hadError }) 
                                stringResource(R.string.practice_finish_with_errors) 
                            else stringResource(R.string.practice_finish)
                            Text(text, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UmlautBar(onUmlautClick: (String) -> Unit) {
    val umlauts = listOf("ä", "ö", "ü", "ß", "Ä", "Ö", "Ü")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Translate,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            umlauts.forEach { umlaut ->
                Surface(
                    onClick = { onUmlautClick(umlaut) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(width = 40.dp, height = 36.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            umlaut,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeScreenPreview() {
    SchreibenAufDeutschTheme {
        PracticeScreen(0L, 0, {}, {})
    }
}
