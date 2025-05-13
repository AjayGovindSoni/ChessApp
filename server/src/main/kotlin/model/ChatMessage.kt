package com.example.model

import java.util.*

data class ChatMessage(
    val id: UUID,
    val senderId: UUID,
    val content: String,
    val timestamp: Long
)
