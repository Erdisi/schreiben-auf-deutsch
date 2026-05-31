package com.example.schreibenaufdeutsch.ui.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.example.schreibenaufdeutsch.R
import com.example.schreibenaufdeutsch.ui.practice.PracticeSentence
import com.example.schreibenaufdeutsch.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceCard(
    sentence: PracticeSentence,
    currentInput: TextFieldValue = TextFieldValue(""),
    onUserInputChange: (TextFieldValue) -> Unit = {},
    onSend: () -> Unit = {},
    onSpeak: () -> Unit,
    isScoreMode: Boolean = false,
    onGetFeedback: () -> Unit = {},
    onRefreshSentence: (() -> Unit)? = null,
    onEditSentence: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    allowTranslation: Boolean = false,
    allowSpeak: Boolean = false,
    focusRequester: FocusRequester? = null
) {
    // Entrance animation
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = sentence.index * 100,
                easing = FastOutSlowInEasing
            )
        )
    }

    var showTranslation by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var isReported by remember { mutableStateOf(false) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(sentence.germanText) }

    // Shake animation state
    val shakeOffset = remember { Animatable(0f) }
    
    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by if (sentence.isCurrent) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
    } else {
        remember { mutableStateOf(0.3f) }
    }

    val isInputPerfect = currentInput.text == sentence.germanText

    LaunchedEffect(sentence.hadError) {
        if (sentence.hadError && sentence.isCurrent) {
            repeat(3) {
                shakeOffset.animateTo(
                    targetValue = 10f,
                    animationSpec = tween(50, easing = LinearEasing)
                )
                shakeOffset.animateTo(
                    targetValue = -10f,
                    animationSpec = tween(50, easing = LinearEasing)
                )
            }
            shakeOffset.animateTo(0f)
        }
    }
    
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (sentence.isCurrent) {
        if (isDark) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    } else {
        if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val borderColor = when {
        sentence.isCurrent -> MaterialTheme.colorScheme.primary
        sentence.hadError -> MaterialTheme.colorScheme.error
        sentence.isCompleted && !sentence.hadError -> Color(0xFF4CAF50) // Success Green
        isScoreMode && !sentence.hadError -> MaterialTheme.colorScheme.primary
        else -> if (isDark) Color.White.copy(alpha = 0.1f) else Color.Transparent
    }

    val alpha = if (sentence.isCurrent || sentence.isCompleted || isScoreMode) 1f else 0.6f

    val correctColor = if (isDark) Color(0xFF00E676) else MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val annotatedText = remember(currentInput.text, sentence.germanText, sentence.isCurrent, sentence.isCompleted, isScoreMode) {
        buildAnnotatedString {
            val target = sentence.germanText
            if (sentence.isCurrent && currentInput.text.isNotEmpty()) {
                var mismatchFound = false
                for (i in target.indices) {
                    val targetChar = target[i]
                    val inputChar = currentInput.text.getOrNull(i)

                    if (!mismatchFound) {
                        if (inputChar == null) {
                            withStyle(style = SpanStyle(fontFamily = FontFamily.Serif)) {
                                append(targetChar)
                            }
                            mismatchFound = true
                        } else if (inputChar == targetChar) {
                            withStyle(style = SpanStyle(
                                color = correctColor, 
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )) {
                                append(targetChar)
                            }
                        } else {
                            withStyle(style = SpanStyle(
                                color = errorColor, 
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )) {
                                append(targetChar)
                            }
                            mismatchFound = true
                        }
                    } else {
                        withStyle(style = SpanStyle(fontFamily = FontFamily.Serif)) {
                            append(targetChar)
                        }
                    }
                }
            } else if (sentence.isCompleted || isScoreMode) {
                withStyle(style = SpanStyle(
                    color = if (isScoreMode && sentence.hadError) errorColor else correctColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )) {
                    append(target)
                }
            } else {
                withStyle(style = SpanStyle(fontFamily = FontFamily.Serif)) {
                    append(target)
                }
            }
        }
    }

    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = animatedProgress.value * alpha
                translationX = (1f - animatedProgress.value) * 30.dp.toPx()
            }
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (sentence.isCurrent) 4.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    java.util.Locale.ROOT.let { locale -> String.format(locale, "%02d", sentence.index + 1) },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (sentence.isCurrent || isScoreMode || allowSpeak) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (onRefreshSentence != null) {
                        if (onEditSentence != null) {
                            IconButton(
                                onClick = { 
                                    editedText = sentence.germanText
                                    isEditDialogOpen = true 
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit Sentence",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshSentence,
                            modifier = Modifier.size(24.dp),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = "Refresh Variation",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (sentence.isCurrent && !isScoreMode && onClick != null) {
                val correctColorInternal = if (isDark) Color(0xFF00E676) else MaterialTheme.colorScheme.primary
                val errorColorInternal = MaterialTheme.colorScheme.error
                
                // Real-time Character Validation Annotated String
                val inputAnnotatedText = buildAnnotatedString {
                    val target = sentence.germanText
                    for (i in currentInput.text.indices) {
                        val inputChar = currentInput.text[i]
                        val targetChar = target.getOrNull(i)
                        
                        if (targetChar != null && inputChar == targetChar) {
                            withStyle(style = SpanStyle(color = correctColorInternal)) {
                                append(inputChar)
                            }
                        } else {
                            withStyle(style = SpanStyle(color = errorColorInternal)) {
                                append(inputChar)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .graphicsLayer { translationX = shakeOffset.value }
                ) {
                    val activeBorderColor = if (isInputPerfect) {
                        Color(0xFF4CAF50)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                    }
                    
                    BasicTextField(
                        value = currentInput,
                        onValueChange = onUserInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .run { if (focusRequester != null) focusRequester(focusRequester) else this }
                            .border(
                                width = if (isInputPerfect) 2.5.dp else 2.dp,
                                color = activeBorderColor,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { onSend() }
                        ),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (currentInput.text.isEmpty()) {
                                        Text(
                                            stringResource(R.string.practice_write_here),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    
                                    // Layered approach: Render the annotated validation text
                                    Text(
                                        text = inputAnnotatedText,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 18.sp
                                        )
                                    )
                                    
                                    // The actual invisible field for interaction
                                    Box(modifier = Modifier.alpha(0f)) {
                                        innerTextField()
                                    }
                                }
                                
                                IconButton(
                                    onClick = onSend,
                                    enabled = currentInput.text.isNotBlank(),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    AnimatedContent(
                                        targetState = isInputPerfect,
                                        transitionSpec = {
                                            scaleIn() togetherWith scaleOut()
                                        },
                                        label = "sendIcon"
                                    ) { perfect ->
                                        if (perfect) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Richtig",
                                                tint = Color(0xFF4CAF50)
                                            )
                                        } else {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Senden",
                                                tint = if (currentInput.text.isBlank())
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) 
                                                else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (sentence.isCurrent || isScoreMode || allowTranslation) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showTranslation = !showTranslation },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (showTranslation) "Übersetzung ausblenden" else "Übersetzung anzeigen")
                    }

                    if (sentence.hadError) {
                        TextButton(
                            onClick = { 
                                showFeedback = !showFeedback
                                if (showFeedback && sentence.aiFeedback == null) {
                                    onGetFeedback()
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (showFeedback) "Feedback ausblenden" else "KI Feedback")
                        }
                    }
                }
                
                AnimatedVisibility(visible = showTranslation) {
                    Text(
                        sentence.translation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                AnimatedVisibility(visible = showFeedback) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(Modifier.height(8.dp))
                        if (sentence.isAiLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Analysiere...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Column {
                                Text(
                                    sentence.aiFeedback ?: "Kein Feedback verfügbar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isReported) {
                                        Text(
                                            "Gemeldet. Danke!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    } else {
                                        TextButton(
                                            onClick = { isReported = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Flag,
                                                contentDescription = "Report AI content",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Ungenaue KI melden", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEditDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEditDialogOpen = false },
            title = { Text("Satz bearbeiten") },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Deutscher Satz") },
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditSentence?.invoke(editedText)
                    isEditDialogOpen = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditDialogOpen = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
