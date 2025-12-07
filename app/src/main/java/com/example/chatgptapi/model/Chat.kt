package com.example.chatgptapi.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    var title: String = "",
    val ownerId: String = "",
    val lastUpdated: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "ownerId" to ownerId,
            "lastUpdated" to (lastUpdated ?: Timestamp.now())
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>?): Chat? {
            if (data == null) return null
            val title = data["title"] as? String ?: ""
            val ownerId = data["ownerId"] as? String ?: ""
            val lastUpdated = data["lastUpdated"] as? Timestamp
            return Chat(id = id, title = title, ownerId = ownerId, lastUpdated = lastUpdated)
        }
    }
}
