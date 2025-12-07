package com.example.chatgptapi.model

data class ConversationMetadata(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val lastMessageFromUser: Boolean = true,
    val isResolved: Boolean = false
)

