package com.example.schreibenaufdeutsch.ui.score

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.ui.common.ConfettiEffect
import com.example.schreibenaufdeutsch.ui.common.SentenceCard
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.practice.PracticeSentence
import com.example.schreibenaufdeutsch.ui.practice.PracticeViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScoreScreen(
    taskId: Long,
    variationIndex: Int,
    sentencesJson: String,
    onBack: () -> Unit,
    onRedo: () -> Unit,
    viewModel: PracticeViewModel = viewModel(
        factory = viewModelFactory { 
            PracticeViewModel(SchreibenApp.instance.taskRepository, SchreibenApp.instance.aiRepository) 
        }
    )
) {
    val task by viewModel.task.collectAsState()
    val viewModelSentences by viewModel.sentences.collectAsState()
    
    val initialSentences = remember(sentencesJson) {
        Json.decodeFromString<List<PracticeSentence>>(sentencesJson)
    }

    val errorCount = initialSentences.count { it.hadError }
    var showConfetti by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        if (errorCount == 0) {
            delay(300) // Small delay for the screen to settle
            showConfetti = true
            delay(5000) // Show for 5 seconds
            showConfetti = false
        }
    }

    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(taskId, initialSentences) {
        viewModel.loadTask(taskId, variationIndex, initialSentences = initialSentences)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Ergebnis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 100.dp, // Extra padding for floating buttons
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = task?.title ?: "Aufgabe abgeschlossen",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Text(
                        text = if (errorCount == 0) "Perfekt! Du hast keine Fehler gemacht." 
                               else "Du hast $errorCount Sätze mit Fehlern geschrieben.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (errorCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            val fullText = viewModelSentences.joinToString("\n") { it.germanText }
                            clipboardManager.setText(AnnotatedString(fullText))
                            Toast.makeText(context, "Text in die Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Vollständigen Text kopieren")
                    }
                }

                items(viewModelSentences, key = { it.index }) { sentence ->
                    SentenceCard(
                        sentence = sentence,
                        isScoreMode = true,
                        onSpeak = { viewModel.speak(sentence.germanText) },
                        onGetFeedback = { viewModel.getAiFeedback(sentence.index) }
                    )
                }
            }

            // Floating action buttons at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
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
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRedo,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Wiederholen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Fertig", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showConfetti) {
                ConfettiEffect(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
