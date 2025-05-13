package com.example.repository

import com.example.db.ChatMessageDAO
import com.example.db.UserDAO
import com.example.db.daoToModel
import com.example.db.suspendTransaction
import com.example.model.ChatMessage
import java.util.*

class ChatReposiratory {
    suspend fun createChatMessage(senderId: UUID, content: String): ChatMessage = suspendTransaction {
        val user = UserDAO.findById(senderId) ?: throw IllegalArgumentException("User not found")

        val dao = ChatMessageDAO.new {
            this.sender = user
            this.content = content
            this.timestamp = System.currentTimeMillis()
        }

        daoToModel(dao)
    }

}