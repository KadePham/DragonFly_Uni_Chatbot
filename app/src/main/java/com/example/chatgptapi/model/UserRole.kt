package com.example.chatgptapi.model

import com.google.firebase.firestore.PropertyName

data class UserRole(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "user", // "user" hoặc "admin"
    val createdAt: Long = System.currentTimeMillis(),
    @PropertyName("active")
    val isActive: Boolean = true
)

