package com.example.schreibenaufdeutsch.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    BackHandler(onBack = onBack)

    val uriHandler = LocalUriHandler.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState()
    var showLanguageSheet by remember { mutableStateOf(false) }

    var versionTapCount by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.clickable { 
                versionTapCount++
                if (versionTapCount >= 7 && !isDeveloperMode) {
                    viewModel.toggleDeveloperMode()
                }
            }) {
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyLarge)
                Text("Entwickelt für Schreiben auf Deutsch", style = MaterialTheme.typography.bodyMedium)
            }
            
            if (isDeveloperMode) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Entwicklermodus (Kein Limit)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Switch(checked = true, onCheckedChange = { viewModel.toggleDeveloperMode() })
                    }
                }
            }
            
            HorizontalDivider()
            
            Text("Über diese App", style = MaterialTheme.typography.titleMedium)
            Text(
                "Diese App hilft Ihnen, Ihr Deutsch durch das Schreiben von Sätzen in verschiedenen Kontexten zu verbessern. " +
                "Nutzen Sie die KI, um neue Aufgaben zu generieren und Feedback zu Ihren Fehlern zu erhalten.",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            Text("Übersetzungseinstellungen", style = MaterialTheme.typography.titleMedium)

            OutlinedCard(
                onClick = { showLanguageSheet = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Language, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Zielsprache", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val currentLangName = viewModel.availableLanguages.find { it.code == selectedLanguage }?.name ?: "English"
                            Text(currentLangName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos, 
                        null, 
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            HorizontalDivider()

            Text("KI-Sicherheit & Datenschutz", style = MaterialTheme.typography.titleMedium)
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("KI-Offenlegung", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Inhalte und Feedback werden von Google Gemini & Groq (Llama) generiert. " +
                        "Ihre Texteingaben werden zur Analyse an diese Dienste gesendet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = { uriHandler.openUri("https://github.com/erdisdriza/SchreibenaufDeutsch/blob/main/PRIVACY.md") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PrivacyTip, null)
                Spacer(Modifier.width(8.dp))
                Text("Datenschutzerklärung lesen")
            }
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    "Zielsprache wählen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                LazyColumn {
                    items(viewModel.availableLanguages) { language: LanguageOption ->
                        val isSelected = selectedLanguage == language.code
                        Surface(
                            onClick = {
                                viewModel.updateTranslationLanguage(language.code)
                                showLanguageSheet = false
                            },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    language.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
