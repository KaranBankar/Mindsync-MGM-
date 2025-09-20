package com.koshpal.studentapp

import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val isFromUser: Boolean = true,
    val timestamp: String = "",
    val isTyping: Boolean = false
) {
    companion object {
        fun createUserMessage(message: String): ChatMessage {
            return ChatMessage(
                id = generateId(),
                message = message.trim(),
                isFromUser = true,
                timestamp = getCurrentTimestamp(),
                isTyping = false
            )
        }
        
        fun createBotMessage(message: String): ChatMessage {
            return ChatMessage(
                id = generateId(),
                message = message.trim(),
                isFromUser = false,
                timestamp = getCurrentTimestamp(),
                isTyping = false
            )
        }
        
        fun createTypingIndicator(): ChatMessage {
            return ChatMessage(
                id = generateId(),
                message = "",
                isFromUser = false,
                timestamp = getCurrentTimestamp(),
                isTyping = true
            )
        }
        
        private fun generateId(): String {
            return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }
        
        private fun getCurrentTimestamp(): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }
}

// Data classes for Gemini API
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)
