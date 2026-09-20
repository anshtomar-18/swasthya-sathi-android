package com.swasthyasathi.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ai.AppLanguage
import com.swasthyasathi.app.ai.VoiceState
import com.swasthyasathi.app.data.model.ChatMessage
import com.swasthyasathi.app.data.model.MessageSender
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel
import com.swasthyasathi.app.wearable.WearableConnectionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isVoiceRepliesEnabled by viewModel.isVoiceRepliesEnabled.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val riskResult by viewModel.riskResult.collectAsState()
    val wearableTelemetry by viewModel.wearableTelemetry.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isLanguageMenuOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // STT microphone permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecognition()
        }
    }

    // System Speech Recognizer Activity Launcher Fallback
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                viewModel.onSpeechActivityResult(matches[0])
            }
        }
    }

    // Auto update input text when speech recognizer returns result
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            inputText = recognizedText
            viewModel.sendChatMessage(recognizedText)
            viewModel.clearRecognizedText()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // 1. Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SwasthyaSathi AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            // Network Status Pill
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (isOfflineMode) RiskHighBg else RiskLowBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isOfflineMode) RiskHigh else RiskLow)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOfflineMode) "🔴 Offline Mode" else "🟢 RAG Live",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = if (isOfflineMode) SecondaryCoral else PrimaryTeal
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Multilingual Clinical Companion • ${selectedLanguage.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppOutline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Auto Voice Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isVoiceRepliesEnabled) PrimaryTeal.copy(alpha = 0.15f) else SurfaceContainerHigh,
                        modifier = Modifier
                            .clickable { viewModel.toggleVoiceReplies() }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVoiceRepliesEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (isVoiceRepliesEnabled) PrimaryTeal else AppOutline,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVoiceRepliesEnabled) "Auto Voice ON" else "Auto Voice OFF",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = if (isVoiceRepliesEnabled) PrimaryTeal else AppOutline
                            )
                        }
                    }

                    // SOS Quick Action
                    IconButton(onClick = { viewModel.setSosOpen(true) }) {
                        Icon(Icons.Default.Emergency, contentDescription = "Emergency SOS", tint = SecondaryCoral)
                    }

                    // Close Sheet
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                    }
                }
            }

            // 2. Personal Risk & Wearable Telemetry Summary Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, SurfaceContainerHigh),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (riskResult.level) {
                                        "Critical" -> RiskCritical
                                        "High" -> RiskHigh
                                        "Moderate" -> RiskModerate
                                        else -> RiskLow
                                    }
                                )
                        )
                        Column {
                            Text(
                                text = "Personal Risk: ${riskResult.level.uppercase()} TIER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = riskResult.primaryDriver,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppOutline,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Wearable Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (wearableTelemetry.connectionStatus == WearableConnectionStatus.CONNECTED) EmeraldContainer else SurfaceContainerHighest,
                        modifier = Modifier.clickable { viewModel.setDemoWearableOpen(true) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (wearableTelemetry.connectionStatus == WearableConnectionStatus.CONNECTED) "${wearableTelemetry.heartRate} BPM • ${wearableTelemetry.spo2}%" else "Watch Demo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 3. Quick Prompt Chips & Language Switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick Prompts Carousel
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val promptList = when (selectedLanguage) {
                        AppLanguage.HINDI -> listOf("मुझे गर्मी में चक्कर आ रहे हैं", "प्रदूषण में सांस की समस्या", "ओआरएस कब और कितना पीएं?")
                        AppLanguage.BENGALI -> listOf("আমার গরমের জন্য মাথা ঘুরাচ্ছে", "বায়ুদূষণে শ্বাসকষ্ট হচ্ছে", "কতটা জল ও ওআরএস খাওয়া উচিত?")
                        else -> listOf("I feel dizzy in extreme heat", "Chest tightness during AQI smog", "Hydration dosing for laborers")
                    }

                    promptList.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = SurfaceContainerLow,
                            border = BorderStroke(1.dp, SurfaceContainer),
                            modifier = Modifier.bounceClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.sendChatMessage(prompt)
                                scope.launch { listState.animateScrollToItem(messages.size) }
                            }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryTeal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Language Switcher Button
                Box {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SurfaceContainerHigh,
                        modifier = Modifier
                            .clickable { isLanguageMenuOpen = true }
                            .padding(start = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Language", tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                            Text(selectedLanguage.displayName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface, fontSize = 11.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppOutline, modifier = Modifier.size(14.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = isLanguageMenuOpen,
                        onDismissRequest = { isLanguageMenuOpen = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName, fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    isLanguageMenuOpen = false
                                },
                                leadingIcon = {
                                    if (lang == selectedLanguage) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryTeal)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = SurfaceContainer)

            // 4. Message Stream & RAG Sources Display
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == MessageSender.USER
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "${if (isUser) "You" else "SwasthyaSathi AI"} • ${msg.timestamp}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 16.dp
                            ),
                            color = if (isUser) PrimaryTeal else SurfaceContainerLow,
                            border = if (isUser) null else BorderStroke(1.dp, SurfaceContainer),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val cleanText = msg.text.replace("**", "").replace("__", "")
                                Text(
                                    text = cleanText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) Color.White else OnSurface,
                                    lineHeight = 20.sp,
                                    fontSize = 13.sp
                                )

                                // Listen to response button for AI replies
                                if (!isUser) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val isSpeakingThis = voiceState == VoiceState.SPEAKING
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (isSpeakingThis) SecondaryCoral.copy(alpha = 0.15f) else PrimaryTeal.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, if (isSpeakingThis) SecondaryCoral.copy(alpha = 0.4f) else PrimaryTeal.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { viewModel.speakResponse(msg.text) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSpeakingThis) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                contentDescription = "Listen to Response",
                                                tint = if (isSpeakingThis) SecondaryCoral else PrimaryTeal,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = if (isSpeakingThis) "Stop Voice" else "🔊 Listen to Response",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                color = if (isSpeakingThis) SecondaryCoral else PrimaryTeal
                                            )
                                        }
                                    }
                                }

                                // Render RAG Research Sources Card if present
                                if (!isUser && msg.sources.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                                                Text("📚 Clinical RAG Evidence", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = EmeraldPrimary)
                                            }
                                            msg.sources.forEach { src ->
                                                val title = src["title"]?.toString() ?: "Research Chunk"
                                                val sourceName = src["source"]?.toString() ?: "Medical Guidelines"
                                                Text("• $title ($sourceName)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = OnSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Voice State Notification Banner
            AnimatedVisibility(visible = voiceState != VoiceState.IDLE) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (voiceState) {
                        VoiceState.LISTENING -> PrimaryTeal.copy(alpha = 0.15f)
                        VoiceState.PROCESSING -> EmeraldContainer
                        VoiceState.SPEAKING -> SurfaceContainerHigh
                        VoiceState.ERROR -> RiskCriticalBg
                        else -> SurfaceContainerLow
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.PROCESSING) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = PrimaryTeal, strokeWidth = 2.dp)
                        } else if (voiceState == VoiceState.SPEAKING) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = when (voiceState) {
                                VoiceState.LISTENING -> "🎙 Listening... Speak clearly in ${selectedLanguage.displayName}"
                                VoiceState.PROCESSING -> "⚡ Speech-to-Text Processing..."
                                VoiceState.SPEAKING -> "🔊 Speaking AI Response..."
                                VoiceState.ERROR -> "⚠ Microphone / STT Error. Try text input."
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                }
            }

            // 5. Input Bar with Microphone & Text Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Microphone Button (STT Trigger)
                val isListening = voiceState == VoiceState.LISTENING
                val pulseScale by rememberInfiniteTransition(label = "micPulse").animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.2f else 1f,
                    animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
                    label = "micScale"
                )

                IconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopVoiceRecognition()
                        } else {
                            try {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                speechIntentLauncher.launch(viewModel.createSpeechIntent())
                            } catch (e: Exception) {
                                viewModel.startVoiceRecognition()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (isListening) SecondaryCoral else PrimaryTeal)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask SwasthyaSathi AI...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    maxLines = 3
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.sendChatMessage(inputText.trim())
                            inputText = ""
                            scope.launch { listState.animateScrollToItem(messages.size) }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal)
                        .bounceClick()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
