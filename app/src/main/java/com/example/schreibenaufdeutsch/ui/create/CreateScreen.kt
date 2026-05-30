package com.example.schreibenaufdeutsch.ui.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.ui.common.viewmodel.viewModelFactory
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    initialTopic: String? = null,
    onBack: () -> Unit,
    onTaskGenerated: (Long, String, String, String) -> Unit,
    viewModel: CreateViewModel = viewModel(
        factory = viewModelFactory { 
            CreateViewModel(SchreibenApp.instance.taskRepository, SchreibenApp.instance.aiRepository) 
        }
    ),
) {
    var description by remember { mutableStateOf(initialTopic ?: "") }
    var selectedTrack by remember { mutableStateOf("General") }
    var selectedLevel by remember { mutableStateOf("B2") }
    var selectedTone by remember { mutableStateOf("Informal") }
    var selectedType by remember { mutableStateOf("Forum Post") }
    var extraContext by remember { mutableStateOf("") }
    var mustUseWords by remember { mutableStateOf("") }
    var selectedGrammar by remember { mutableStateOf(setOf<String>()) }
    var complexity by remember { mutableFloatStateOf(0.5f) }
    var isBookmarked by remember { mutableStateOf(false) }

    val grammarOptions = listOf("Passiv", "Konjunktiv II", "Relativsätze", "Genitiv", "Präteritum", "Nebensätze")

    val uiState by viewModel.uiState.collectAsState()
    val exampleTopic by viewModel.exampleTopic.collectAsState()
    val isGeneratingExample by viewModel.isGeneratingExample.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exampleTopic) {
        exampleTopic?.let {
            description = it
            viewModel.clearExampleTopic()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateUiState.Success -> {
                onTaskGenerated(
                    state.taskId,
                    selectedLevel,
                    selectedTone,
                    selectedType
                )
                viewModel.resetState()
            }
            is CreateUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    title = { Text("Aufgabe erstellen", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                // Sticky Selection Summary
                SelectionSummaryBar(selectedLevel, selectedType, selectedTone)
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.generateTask(
                                description,
                                selectedLevel,
                                selectedTone,
                                selectedType,
                                extraContext,
                                complexity,
                                selectedTrack,
                                selectedGrammar.toList(),
                                mustUseWords,
                                isBookmarked
                            )
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = description.isNotBlank() && (uiState !is CreateUiState.Loading),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState is CreateUiState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, size = 20.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Aufgabe generieren", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    OutlinedIconButton(
                        onClick = { isBookmarked = !isBookmarked },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = if (isBookmarked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                StepHeader(1, "Lernpfad (Exam Track)")
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrackCard("General", Icons.Default.School, selectedTrack == "General") { selectedTrack = "General" }
                    TrackCard("DTB (Beruf)", Icons.Default.BusinessCenter, selectedTrack == "DTB (Beruf)") { selectedTrack = "DTB (Beruf)" }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrackCard("Telc", Icons.Default.AccountBalance, selectedTrack == "Telc") { selectedTrack = "Telc" }
                    TrackCard("Goethe", Icons.Default.AccountBalance, selectedTrack == "Goethe") { selectedTrack = "Goethe" }
                }
            }

            item {
                StepHeader(2, "Thema")
                MainTopicSection(
                    description = description,
                    onDescriptionChange = { description = it },
                    onGenerateExample = {
                        viewModel.generateExampleTopic(selectedLevel, selectedTone, selectedType, selectedTrack)
                    },
                    isGeneratingExample = isGeneratingExample
                )
            }

            item {
                StepHeader(3, "Sprachniveau")
                LevelTimelineSelector(selectedLevel) { selectedLevel = it }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        StepHeader(4, "Aufgabentyp")
                        TypeGrid(selectedType) { selectedType = it }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        StepHeader(5, "Stil")
                        StyleSelector(selectedTone) { selectedTone = it }
                    }
                }
            }

            item {
                StepHeader(6, "Smart Customization", optional = true)
                SmartCustomizationCard(
                    grammarOptions = grammarOptions,
                    selectedGrammar = selectedGrammar,
                    onGrammarToggle = { 
                        selectedGrammar = if (selectedGrammar.contains(it)) selectedGrammar - it else selectedGrammar + it 
                    },
                    mustUseWords = mustUseWords,
                    onMustUseWordsChange = { mustUseWords = it },
                    extraContext = extraContext,
                    onExtraContextChange = { if (it.length <= 140) extraContext = it }
                )
            }

            item {
                StepHeader(7, "Komplexität")
                ComplexitySlider(complexity) { complexity = it }
            }
        }
    }
}

@Composable
private fun SelectionSummaryBar(level: String, type: String, tone: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryChip(Icons.Default.Bookmark, level)
        SummaryChip(Icons.Default.ChatBubbleOutline, type)
        SummaryChip(if (tone == "Formal") Icons.Default.WorkOutline else Icons.Default.SentimentSatisfied, tone)
    }
}

@Composable
private fun SummaryChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepHeader(number: Int, title: String, optional: Boolean = false) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (optional) {
            Spacer(Modifier.width(4.dp))
            Text(
                "(Optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RowScope.TrackCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LevelTimelineSelector(selectedLevel: String, onLevelSelected: (String) -> Unit) {
    val levels = listOf("A1", "A2", "B1", "B2", "C1")
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Timeline Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            levels.forEach { level ->
                val isSelected = level == selectedLevel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = { onLevelSelected(level) },
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 4.dp else 2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        if (isSelected) {
                            Box(modifier = Modifier.fillMaxSize().padding(4.dp).background(Color.White, CircleShape))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        level,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeGrid(selectedType: String, onTypeSelected: (String) -> Unit) {
    val types = listOf(
        "Email" to Icons.Default.Email,
        "Letter" to Icons.Default.Description,
        "Forum Post" to Icons.Default.Forum,
        "Essay" to Icons.AutoMirrored.Filled.Article
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeCard(types[0], selectedType == types[0].first, Modifier.weight(1f)) { onTypeSelected(types[0].first) }
            TypeCard(types[1], selectedType == types[1].first, Modifier.weight(1f)) { onTypeSelected(types[1].first) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeCard(types[2], selectedType == types[2].first, Modifier.weight(1f)) { onTypeSelected(types[2].first) }
            TypeCard(types[3], selectedType == types[3].first, Modifier.weight(1f)) { onTypeSelected(types[3].first) }
        }
    }
}

@Composable
private fun TypeCard(item: Pair<String, ImageVector>, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(item.second, null, modifier = Modifier.size(20.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.first, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun StyleSelector(selectedTone: String, onToneSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StyleCard("Formal", Icons.Default.Work, selectedTone == "Formal") { onToneSelected("Formal") }
        StyleCard("Informal", Icons.Default.SentimentSatisfied, selectedTone == "Informal") { onToneSelected("Informal") }
    }
}

@Composable
private fun StyleCard(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(70.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun MainTopicSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onGenerateExample: () -> Unit,
    isGeneratingExample: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ich möchte schreiben über...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                FilledTonalButton(
                    onClick = onGenerateExample,
                    enabled = !isGeneratingExample,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isGeneratingExample) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            size = 14.dp
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("Beispiel", style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "z.B. Homeoffice, Urlaub, Klimaschutz...",
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                minLines = 1,
                maxLines = 3
            )
            
            Text(
                "Dieses Thema bestimmt den Inhalt deiner KI-generierten Aufgabe.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SmartCustomizationCard(
    grammarOptions: List<String>,
    selectedGrammar: Set<String>,
    onGrammarToggle: (String) -> Unit,
    mustUseWords: String,
    onMustUseWordsChange: (String) -> Unit,
    extraContext: String,
    onExtraContextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Grammar Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Grammatik-Fokus", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(grammarOptions) { grammar ->
                        FilterChip(
                            selected = selectedGrammar.contains(grammar),
                            onClick = { onGrammarToggle(grammar) },
                            label = { Text(grammar) },
                            shape = CircleShape
                        )
                    }
                }
            }

            // Must use words
            Column {
                Text("Vokabeln einbauen", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = mustUseWords,
                    onValueChange = onMustUseWordsChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. trotzdem, zur Verfügung stellen") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 1,
                    maxLines = 3
                )
            }

            // Extra Context
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Zusätzlicher Kontext", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("${extraContext.length}/140", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedTextField(
                    value = extraContext,
                    onValueChange = onExtraContextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. Eine formelle E-Mail an...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }
    }
}

@Composable
private fun ComplexitySlider(value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Einfach", style = MaterialTheme.typography.labelSmall)
            Text(if (value > 0.6f) "Komplex" else if (value < 0.4f) "Einfach" else "Normal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Komplex", style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateScreenPreview() {
    SchreibenAufDeutschTheme {
        CreateScreen(
            initialTopic = null,
            onBack = {},
            onTaskGenerated = { _, _, _, _ -> }
        )
    }
}

@Composable
fun CircularProgressIndicator(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}
