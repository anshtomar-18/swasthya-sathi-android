package com.swasthyasathi.app.data.model

data class ChatMessage(
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val sources: List<Map<String, Any>> = emptyList()
)

enum class MessageSender {
    USER,
    AI
}

data class AskRequest(
    val question: String
)

data class AskResponse(
    val answer: String,
    val sources: List<Map<String, Any>> = emptyList()
)
