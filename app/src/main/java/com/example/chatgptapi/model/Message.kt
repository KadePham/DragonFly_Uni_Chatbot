package com.example.chatgptapi.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val chatId: String = "",
    val isUser: Boolean = true,
    val content: String = "",
    val timestamp: Timestamp? = null,
    val edited: Boolean = false,
    val editedAt: Timestamp? = null,
    val senderUid: String = "",
    val senderName: String = "",
    val senderRole: String = "user" // "user" hoặc "admin"
) {
    fun toMap(): Map<String, Any?> {
        val ts = timestamp ?: Timestamp.now()
        return mapOf(
            "id" to id,
            "chatId" to chatId,
            "isUser" to isUser,
            "content" to content,
            "timestamp" to ts,  // Keep as Timestamp object for Firestore
            "edited" to edited,
            "editedAt" to editedAt,  // Keep as Timestamp object
            "senderUid" to senderUid,
            "senderName" to senderName,
            "senderRole" to senderRole
        )
    }

    // For Realtime Database - convert Timestamp to milliseconds
    fun toRealtimeMap(): Map<String, Any?> {
        val ts = timestamp ?: Timestamp.now()
        return mapOf(
            "id" to id,
            "chatId" to chatId,
            "isUser" to isUser,
            "content" to content,
            "timestamp" to (ts.seconds * 1000 + ts.nanoseconds / 1000000),  // Convert to milliseconds
            "edited" to edited,
            "editedAt" to editedAt?.let { it.seconds * 1000 + it.nanoseconds / 1000000 },  // Convert to milliseconds
            "senderUid" to senderUid,
            "senderName" to senderName,
            "senderRole" to senderRole
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>?): Message? {
            if (data == null) return null
            val chatId = data["chatId"] as? String ?: ""
            val isUser = data["isUser"] as? Boolean ?: true
            val content = data["content"] as? String ?: ""

            // Parse timestamp from milliseconds long
            val timestamp = when (val ts = data["timestamp"]) {
                is Long -> {
                    val seconds = ts / 1000
                    val nanos = ((ts % 1000) * 1000000).toInt()
                    Timestamp(seconds, nanos)
                }
                is Timestamp -> ts
                else -> null
            }

            val edited = data["edited"] as? Boolean ?: false

            // Parse editedAt from milliseconds long
            val editedAt = when (val et = data["editedAt"]) {
                is Long -> {
                    val seconds = et / 1000
                    val nanos = ((et % 1000) * 1000000).toInt()
                    Timestamp(seconds, nanos)
                }
                is Timestamp -> et
                else -> null
            }

            val senderUid = data["senderUid"] as? String ?: ""
            val senderName = data["senderName"] as? String ?: ""
            val senderRole = data["senderRole"] as? String ?: "user"
            return Message(
                id = id,
                chatId = chatId,
                isUser = isUser,
                content = content,
                timestamp = timestamp,
                edited = edited,
                editedAt = editedAt,
                senderUid = senderUid,
                senderName = senderName,
                senderRole = senderRole
            )
        }
    }
}


