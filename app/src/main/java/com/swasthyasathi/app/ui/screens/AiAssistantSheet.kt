package com.swasthyasathi.app.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.ChatMessage
import com.swasthyasathi.app.data.model.MessageSender
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "SwasthyaSathi AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "AI Clinical Triage Assistant",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppOutline
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                }
            }

            HorizontalDivider(color = SurfaceContainer)

            // Suggestion Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Headache & dry throat outdoors",
                    "Dizziness in high heat",
                    "Chest tightness during smog",
                    "Hydration dosing for laborers"
                ).forEach { prompt ->
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Message Bubbles
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == MessageSender.USER
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = "${if (isUser) "You" else "SwasthyaSathi Companion"} • ${msg.timestamp}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppOutline,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 2.dp,
                                bottomEnd = if (isUser) 2.dp else 16.dp
                            ),
                            color = if (isUser) PrimaryTeal else SurfaceContainerLow,
                            border = if (isUser) null else BorderStroke(1.dp, SurfaceContainer),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else OnSurface,
                                modifier = Modifier.padding(14.dp),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Chat Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Describe symptoms or ask a question...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    ),
                    maxLines = 3
                )

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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal)
                        .bounceClick()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
