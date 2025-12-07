package com.example.chatgptapi.viewmodel

// Updated: 2024-12-08 - Added getUserRoleByEmail and setUserRoleByEmail functions

import com.example.chatgptapi.model.Chat
import com.example.chatgptapi.model.Message
import com.example.chatgptapi.model.UserRole
import com.example.chatgptapi.model.ConversationMetadata
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * ChatRepository - Hybrid approach:
 * - Firestore: User accounts (users collection)
 * - Realtime Database: Messages (messages/{uid}/{convId}/{msgId})
 *
 * Lưu trữ structure:
 * Firestore:
 *   users/{uid}  -- user info (email, displayName, role, active)
 *   users/{uid}/conversations/{convId}  -- conversation metadata
 *
 * Realtime Database (Firebase Database):
 *   messages/{uid}/{convId}/{msgId}  -- message data
 *
 * Bao gồm:
 * - createConversation
 * - getConversationsRealtime
 * - getMessagesRealtime (từ Realtime DB)
 * - sendMessage (ghi vào Realtime DB)
 * - deleteConversation
 * - updateMessage (Realtime DB)
 * - updateConversationTitle  <-- mới thêm
 */
class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference  // Realtime DB for messages

    private fun getUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User chưa login")
    }

    /**
     * Lưu user info vào Realtime Database để sử dụng trong chat
     * QUAN TRỌNG: Không ghi đè role nếu user đã là admin
     */
    suspend fun saveUserInfoToRealtimeDB(uid: String, displayName: String, email: String, role: String = "user") {
        try {
            // Lấy role hiện tại từ Firestore trước
            val currentRole = try {
                val doc = firestore.collection("users").document(uid).get().await()
                doc.getString("role") ?: "user"
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Error getting current role: ${e.message}")
                "user"
            }

            // Nếu user đã là admin, KHÔNG được downgrade thành user
            val finalRole = if (currentRole == "admin" && role == "user") {
                android.util.Log.d("ChatRepository", "User is admin, preventing downgrade from admin to user. Keeping admin role.")
                "admin"
            } else {
                role
            }

            val userInfo = mapOf(
                "uid" to uid,
                "displayName" to displayName,
                "email" to email,
                "role" to finalRole
            )
            database.child("users").child(uid).setValue(userInfo).await()
            android.util.Log.d("ChatRepository", "User info saved to Realtime DB: $uid, role=$finalRole (input was $role)")
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error saving user info: ${e.message}", e)
        }
    }

    /**
     * Lấy user info từ Realtime Database
     */
    @Suppress("unused")
    suspend fun getUserInfoFromRealtimeDB(uid: String): Map<String, Any>? {
        return try {
            val snapshot = database.child("users").child(uid).get().await()
            if (snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                snapshot.value as? Map<String, Any>
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error getting user info: ${e.message}", e)
            null
        }
    }

    /**
     * Lấy role của user từ Realtime Database
     */
    @Suppress("unused")
    suspend fun getUserRoleFromRealtimeDB(uid: String): String {
        return try {
            val snapshot = database.child("users").child(uid).child("role").get().await()
            if (snapshot.exists()) {
                snapshot.value as? String ?: "user"
            } else {
                "user"
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error getting role from Realtime DB: ${e.message}", e)
            "user"
        }
    }

    suspend fun createConversation(title: String = "New chat"): String {
        val uid = getUid()
        val convRef = firestore.collection("users")
            .document(uid)
            .collection("conversations")
            .document() // auto id
        val chat = Chat(
            id = convRef.id,
            title = title,
            ownerId = uid,
            lastUpdated = Timestamp.now()
        )
        convRef.set(chat.toMap()).await()
        return convRef.id
    }

    fun getConversationsRealtime(): Flow<List<Chat>> = callbackFlow {
        val uid = try { getUid() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        val col = firestore.collection("users").document(uid).collection("conversations")
            .orderBy("lastUpdated")
        val registration: ListenerRegistration = col.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc ->
                Chat.fromMap(doc.id, doc.data)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    fun getMessagesRealtime(convId: String): Flow<List<Message>> = callbackFlow {
        val uid = try { getUid() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        val messagesRef = database.child("messages").child(uid).child(convId)

        val listener = object : ValueEventListener {
            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { doc ->
                    val id = doc.key ?: return@mapNotNull null
                    val data = doc.value as? Map<String, Any> ?: return@mapNotNull null
                    Message.fromMap(id, data)
                }
                // Sort by timestamp
                val sorted = list.sortedBy {
                    (it.timestamp?.seconds ?: 0L) * 1000 + ((it.timestamp?.nanoseconds ?: 0) / 1000000)
                }
                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepository", "getMessagesRealtime error: ${error.message}")
                // Don't close on error, just log and continue
                trySend(emptyList())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(convId: String, message: Message) {
        val uid = getUid()
        val msgId = if (message.id.isNotEmpty()) message.id else UUID.randomUUID().toString()

        // ensure timestamp exists
        val m = message.copy(
            id = msgId,
            timestamp = message.timestamp ?: Timestamp.now()
        )

        // Write to Realtime Database - use toRealtimeMap() for proper format
        val msgRef = database.child("messages").child(uid).child(convId).child(msgId)
        msgRef.setValue(m.toRealtimeMap()).await()

        // Update conversation's lastUpdated in Firestore
        val convRef = firestore.collection("users").document(uid)
            .collection("conversations")
            .document(convId)
        convRef.update("lastUpdated", Timestamp.now()).addOnFailureListener {
            // if update fails (e.g. doc missing) set with merge
            convRef.set(mapOf("lastUpdated" to Timestamp.now()), com.google.firebase.firestore.SetOptions.merge())
        }.await()
    }

    suspend fun deleteConversation(convId: String) {
        val uid = getUid()

        // Delete messages from Realtime Database
        database.child("messages").child(uid).child(convId).removeValue().await()

        // Delete conversation metadata from Firestore
        val convRef = firestore.collection("users").document(uid)
            .collection("conversations").document(convId)
        convRef.delete().await()
    }

    suspend fun updateMessage(convId: String, messageId: String, newContent: String) {
        val uid = getUid()
        val msgRef = database.child("messages").child(uid).child(convId).child(messageId)
        val currentTimeMs = System.currentTimeMillis()

        msgRef.updateChildren(mapOf(
            "content" to newContent,
            "edited" to true,
            "editedAt" to currentTimeMs  // Use milliseconds for Realtime DB
        )).await()
    }

    /**
     * Cập nhật title của conversation
     * - Cập nhật field "title"
     * - Cập nhật "lastUpdated"
     */
    suspend fun updateConversationTitle(convId: String, newTitle: String) {
        val uid = getUid()
        val convRef = firestore.collection("users")
            .document(uid)
            .collection("conversations")
            .document(convId)

        val data = mapOf(
            "title" to newTitle,
            "lastUpdated" to Timestamp.now()
        )

        convRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    /** ------ ADMIN CHAT FUNCTIONS (Realtime DB) ------ */

    companion object {
        const val ADMIN_UID = "admin"  // For future use when creating admin-specific conversations
        const val ADMIN_CHAT_ID = "admin_support"
        const val DEFAULT_ADMIN_EMAIL = "ocheo@gmail.com"  //   default admin
    }

    /**
     * Lấy hoặc tạo conversation với admin
     */
    suspend fun getOrCreateAdminChat(): String {
        val uid = getUid()
        val adminChatRef = firestore.collection("users")
            .document(uid)
            .collection("conversations")
            .document(ADMIN_CHAT_ID)

        val doc = adminChatRef.get().await()
        if (!doc.exists()) {
            // Tạo conversation mới với admin
            val chat = Chat(
                id = ADMIN_CHAT_ID,
                title = "Chat với Admin Support",
                ownerId = uid,
                lastUpdated = Timestamp.now()
            )
            adminChatRef.set(chat.toMap()).await()
        }
        return ADMIN_CHAT_ID
    }

    /**
     * Lấy messages realtime từ chat admin (dùng Realtime Database)
     * Structure: messages/{userUid}/admin_support/{msgId}
     */
    fun getAdminMessagesRealtime(): Flow<List<Message>> = callbackFlow {
        val uid = try { getUid() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        val messagesRef = database.child("messages").child(uid).child(ADMIN_CHAT_ID)

        val listener = object : ValueEventListener {
            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { doc ->
                    val id = doc.key ?: return@mapNotNull null
                    val data = doc.value as? Map<String, Any> ?: return@mapNotNull null
                    Message.fromMap(id, data)
                }
                // Sort by timestamp
                val sorted = list.sortedBy {
                    (it.timestamp?.seconds ?: 0L) * 1000 + ((it.timestamp?.nanoseconds ?: 0) / 1000000)
                }
                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepository", "getAdminMessagesRealtime error: ${error.message}")
                // Don't close on error, just log and continue
                trySend(emptyList())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    /**
     * Gửi message tới admin (lưu vào Realtime Database)
     * Structure: messages/{userUid}/admin_support/{msgId}
     */
    suspend fun sendAdminMessage(message: Message) {
        val uid = getUid()
        val msgId = if (message.id.isNotEmpty()) message.id else UUID.randomUUID().toString()

        // ensure timestamp exists
        val m = message.copy(
            id = msgId,
            chatId = ADMIN_CHAT_ID,
            timestamp = message.timestamp ?: Timestamp.now()
        )

        // Write to Realtime Database - use toRealtimeMap() for proper format
        val msgRef = database.child("messages").child(uid).child(ADMIN_CHAT_ID).child(msgId)
        msgRef.setValue(m.toRealtimeMap()).await()

        // Update conversation's lastUpdated in Firestore
        val convRef = firestore.collection("users").document(uid)
            .collection("conversations")
            .document(ADMIN_CHAT_ID)
        convRef.update("lastUpdated", Timestamp.now()).addOnFailureListener {
            convRef.set(mapOf("lastUpdated" to Timestamp.now()), com.google.firebase.firestore.SetOptions.merge())
        }.await()
    }

    /**
     * Lấy messages realtime cho admin từ 1 user (dùng Realtime Database)
     * Admin cần xem messages của user: messages/{userUid}/admin_support/{msgId}
     */
    fun getAdminMessagesForUserRealtime(userUid: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = database.child("messages").child(userUid).child(ADMIN_CHAT_ID)

        val listener = object : ValueEventListener {
            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { doc ->
                    val id = doc.key ?: return@mapNotNull null
                    val data = doc.value as? Map<String, Any> ?: return@mapNotNull null
                    Message.fromMap(id, data)
                }
                // Sort by timestamp
                val sorted = list.sortedBy {
                    (it.timestamp?.seconds ?: 0L) * 1000 + ((it.timestamp?.nanoseconds ?: 0) / 1000000)
                }
                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepository", "getAdminMessagesForUserRealtime error: ${error.message}")
                // Don't close on error, just log and continue
                trySend(emptyList())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    /**
     * Admin gửi reply tới user (lưu vào Realtime Database + Firestore)
     * Structure: messages/{userUid}/admin_support/{msgId}
     * Firestore: users/{userUid}/conversations/admin_support/messages/{msgId}
     */
    suspend fun sendAdminReply(userUid: String, message: Message) {
        val msgId = if (message.id.isNotEmpty()) message.id else UUID.randomUUID().toString()

        // ensure timestamp exists
        val m = message.copy(
            id = msgId,
            chatId = ADMIN_CHAT_ID,
            timestamp = message.timestamp ?: Timestamp.now()
        )

        // Write to Realtime Database - use toRealtimeMap() for proper format
        val msgRef = database.child("messages").child(userUid).child(ADMIN_CHAT_ID).child(msgId)
        msgRef.setValue(m.toRealtimeMap()).await()

        // ALSO save to Firestore so user can see the reply in HelpActivity
        val firestoreRef = firestore.collection("users").document(userUid)
            .collection("conversations")
            .document(ADMIN_CHAT_ID)
            .collection("messages")
            .document(msgId)
        firestoreRef.set(m.toMap()).await()

        // Update conversation's lastUpdated in Firestore (user's conversation metadata)
        val convRef = firestore.collection("users").document(userUid)
            .collection("conversations")
            .document(ADMIN_CHAT_ID)
        convRef.update("lastUpdated", Timestamp.now()).addOnFailureListener {
            convRef.set(mapOf("lastUpdated" to Timestamp.now()), com.google.firebase.firestore.SetOptions.merge())
        }.await()

        android.util.Log.d("ChatRepository", "Admin reply saved to both Realtime DB and Firestore for user $userUid")
    }

    /**
     * Get admin-user conversation from Firestore (for admin dashboard)
     * Admin can read user's conversations from Firestore
     * Structure: users/{userUid}/conversations/admin_support/messages/{msgId}
     */
    fun getAdminMessagesFromFirestore(userUid: String): Flow<List<Message>> = callbackFlow {
        val ref = firestore.collection("users").document(userUid)
            .collection("conversations")
            .document(ADMIN_CHAT_ID)
            .collection("messages")

        // Try to query with orderBy first, fallback to no ordering if fails
        val query = try {
            ref.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
        } catch (e: Exception) {
            android.util.Log.w("ChatRepository", "orderBy failed, using unordered query: ${e.message}")
            ref  // Fallback to unordered query
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("ChatRepository", "getAdminMessagesFromFirestore error: ${error.message}")
                // Instead of closing with error, send empty list and continue
                trySend(emptyList())
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Message.fromMap(doc.id, data)
                } catch (e: Exception) {
                    android.util.Log.e("ChatRepository", "Error parsing message: ${e.message}")
                    null
                }
            } ?: emptyList()

            // Sort by timestamp if available
            val sorted = messages.sortedBy { msg ->
                (msg.timestamp?.seconds ?: 0L) * 1000 + ((msg.timestamp?.nanoseconds ?: 0) / 1000000)
            }

            trySend(sorted)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Lưu user role khi đăng ký MỚI - KHÔNG ĐƯỢC gọi để update existing users
     * QUAN TRỌNG: Chỉ dùng cho signup, không dùng để update role của user hiện tại
     */
    suspend fun saveUserRole(displayName: String, role: String = "user") {
        val user = auth.currentUser ?: throw IllegalStateException("User chưa login")
        val uid = user.uid
        val email = user.email ?: ""

        // Check if user already exists
        val existingDoc = firestore.collection("users").document(uid).get().await()
        if (existingDoc.exists()) {
            android.util.Log.w("ChatRepository", "saveUserRole: User already exists! Not overwriting. uid=$uid")
            return  // Don't overwrite existing user
        }

        val userRole = UserRole(
            uid = uid,
            email = email,
            displayName = displayName,
            role = role,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        // Save to users collection with role field (Firestore will use @PropertyName to map fields)
        firestore.collection("users").document(uid).set(userRole).await()
        android.util.Log.d("ChatRepository", "New user created with saveUserRole: $uid, role=$role")
    }

    /**
     * Đảm bảo user document tồn tại, nếu không thì tạo
     * QUAN TRỌNG: KHÔNG ghi đè role hoặc bất kỳ field nào nếu document đã tồn tại
     * ✅ SPECIAL: ocheo@gmail.com sẽ luôn được set thành admin
     */
    suspend fun ensureUserExists(): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val uid = user.uid
            val email = user.email ?: ""
            val displayName = user.displayName ?: "User"

            android.util.Log.d("ChatRepository", "ensureUserExists: uid=$uid, email=$email, displayName=$displayName")

            // Kiểm tra document có tồn tại không
            val doc = firestore.collection("users").document(uid).get().await()

            if (!doc.exists()) {
                android.util.Log.d("ChatRepository", "Document not found, creating new user")

                // ✅ Determine role: ocheo@gmail.com is always admin
                val role = if (email == DEFAULT_ADMIN_EMAIL) {
                    android.util.Log.d("ChatRepository", "✅ Setting $DEFAULT_ADMIN_EMAIL as DEFAULT ADMIN")
                    "admin"
                } else {
                    "user"
                }

                val userRole = UserRole(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    role = role,
                    createdAt = System.currentTimeMillis(),
                    isActive = true
                )
                firestore.collection("users").document(uid).set(userRole).await()
                android.util.Log.d("ChatRepository", "New user created with role=$role")
            } else {
                // Document đã tồn tại - KHÔNG CẬP NHẬT GÌ HẾT
                val currentRole = doc.getString("role")
                android.util.Log.d("ChatRepository", "User already exists. uid=$uid, current role=$currentRole - NOT UPDATING")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error in ensureUserExists: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Lấy user role từ Firestore (source of truth)
     * QUAN TRỌNG: Luôn lấy từ Firestore, không dùng cache
     */
    suspend fun getUserRoleString(uid: String = getUid()): String {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val role = doc.getString("role") ?: "user"
                android.util.Log.d("ChatRepository", "getUserRoleString: uid=$uid, role=$role")
                role
            } else {
                android.util.Log.w("ChatRepository", "getUserRoleString: User document not found for uid=$uid, returning user")
                "user"
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "getUserRoleString: Error getting role: ${e.message}", e)
            "user"
        }
    }

    /**
     * Get user account creation date (createdAt timestamp from Firestore)
     */
    suspend fun getUserCreatedAtTimestamp(uid: String = getUid()): Timestamp? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val createdAt = doc.getTimestamp("createdAt")
                android.util.Log.d("ChatRepository", "getUserCreatedAtTimestamp: uid=$uid, createdAt=$createdAt")
                createdAt
            } else {
                android.util.Log.w("ChatRepository", "getUserCreatedAtTimestamp: User document not found for uid=$uid")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "getUserCreatedAtTimestamp: Error getting createdAt: ${e.message}", e)
            null
        }
    }

    /**
     * Kiểm tra user có phải admin không
     * ✅ SPECIAL: ocheo@gmail.com (DEFAULT_ADMIN_EMAIL) luôn là admin
     */
    suspend fun isAdmin(): Boolean {
        val currentUser = auth.currentUser

        // ✅ Check if user is default admin
        if (currentUser?.email == DEFAULT_ADMIN_EMAIL) {
            android.util.Log.d("ChatRepository", "✅ User ${currentUser.email} is DEFAULT_ADMIN_EMAIL")
            return true
        }

        val roleString = getUserRoleString()
        return roleString == "admin"
    }

    /**
     * Lấy UID từ email
     */
    private suspend fun getUidByEmail(email: String): String {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("email", email)
                .get().await()

            if (query.documents.isNotEmpty()) {
                query.documents[0].id
            } else {
                throw Exception("User with email $email not found")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error finding user by email: ${e.message}", e)
            throw e
        }
    }

    /**
     * Lấy role của user bằng email
     */
    suspend fun getUserRoleByEmail(email: String): String {
        return try {
            val uid = getUidByEmail(email)
            getUserRoleString(uid)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error getting role by email: ${e.message}", e)
            throw e
        }
    }

    /**
     * Set user as admin by email (Simplified version)
     * Can be called directly without admin check for initial setup
     */
    suspend fun setUserAsAdmin(email: String) {
        try {
            // Find user by email
            val query = firestore.collection("users")
                .whereEqualTo("email", email)
                .get().await()

            if (query.documents.isEmpty()) {
                throw Exception("User with email $email not found in Firestore")
            }

            val uid = query.documents[0].id
            android.util.Log.d("ChatRepository", "Found user: uid=$uid, email=$email")

            // Update role to admin in Firestore
            firestore.collection("users").document(uid)
                .update(mapOf("role" to "admin"))
                .await()

            android.util.Log.d("ChatRepository", "✅ Successfully set admin for $email")

            // Update Realtime DB
            try {
                val userDoc = firestore.collection("users").document(uid).get().await()
                if (userDoc.exists()) {
                    val displayName = userDoc.getString("displayName") ?: "User"
                    val userEmail = userDoc.getString("email") ?: email
                    saveUserInfoToRealtimeDB(uid, displayName, userEmail, "admin")
                    android.util.Log.d("ChatRepository", "Updated Realtime DB with admin role")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Warning: Could not update Realtime DB: ${e.message}")
            }

        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "❌ Error setting admin: ${e.message}", e)
            throw e
        }
    }

    /**
     * Set role của user bằng email (chỉ admin mới được gọi)
     * QUAN TRỌNG: Hàm này phải được gọi bởi admin
     */
    suspend fun setUserRoleByEmail(email: String, newRole: String) {
        try {
            // Kiểm tra current user là admin
            if (!isAdmin()) {
                throw IllegalStateException("Chỉ admin mới có thể cập nhật role")
            }

            // Kiểm tra role hợp lệ
            if (newRole !in listOf("user", "admin")) {
                throw IllegalArgumentException("Role không hợp lệ: $newRole. Chỉ 'user' hoặc 'admin'")
            }

            // Tìm user bằng email
            val uid = getUidByEmail(email)
            android.util.Log.d("ChatRepository", "Found user: email=$email, uid=$uid")

            // Update role trong Firestore
            firestore.collection("users").document(uid)
                .update(mapOf("role" to newRole))
                .await()

            android.util.Log.d("ChatRepository", "Successfully updated role for $email to $newRole")

            // Cập nhật Realtime DB
            try {
                val userDoc = firestore.collection("users").document(uid).get().await()
                if (userDoc.exists()) {
                    val displayName = userDoc.getString("displayName") ?: "User"
                    val userEmail = userDoc.getString("email") ?: email
                    saveUserInfoToRealtimeDB(uid, displayName, userEmail, newRole)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Warning: Could not update Realtime DB: ${e.message}")
                // Continue - Firestore was already updated
            }

        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error setting role by email: ${e.message}", e)
            throw e
        }
    }

    /**
     * Lấy danh sách tất cả users (cho admin)
     */
    @Suppress("unused")
    fun getAllUsersRole(): Flow<List<UserRole>> = callbackFlow {
        val collectionRef = firestore.collection("users")
        val registration: ListenerRegistration = collectionRef.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }

            val list = snap?.documents?.mapNotNull { doc ->
                UserRole(
                    uid = doc.getString("uid") ?: "",
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    role = doc.getString("role") ?: "user",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    isActive = doc.getBoolean("active") ?: true
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    /** ------ CONVERSATION METADATA (FOR ADMIN INBOX) ------ */

    /**
     * Cập nhật conversation metadata khi user gửi message
     */
    suspend fun updateConversationMetadata(
        userUID: String,
        userName: String,
        userEmail: String,
        lastMessage: String
    ) {
        try {
            val currentUnread = firestore.collection("admin_inbox")
                .document(userUID).get().await().getLong("unreadCount") ?: 0

            val metadata = mapOf(
                "id" to ADMIN_CHAT_ID,
                "userId" to userUID,
                "userName" to userName,
                "userEmail" to userEmail,
                "lastMessage" to lastMessage,
                "lastMessageTime" to System.currentTimeMillis(),
                "unreadCount" to currentUnread + 1,
                "lastMessageFromUser" to true,
                "isResolved" to false
            )

            firestore.collection("admin_inbox").document(userUID).set(metadata).await()
        } catch (e: Exception) {
            // Log error but don't throw
            e.printStackTrace()
        }
    }

    /**
     * Lấy danh sách conversations chưa trả lời (dành cho admin)
     */
    fun getAdminInboxRealtime(): Flow<List<ConversationMetadata>> = callbackFlow {
        val col = firestore.collection("admin_inbox")
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)

        val registration: ListenerRegistration = col.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }

            val list = snap?.documents?.mapNotNull { doc ->
                ConversationMetadata(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: "",
                    userName = doc.getString("userName") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    lastMessage = doc.getString("lastMessage") ?: "",
                    lastMessageTime = doc.getLong("lastMessageTime") ?: 0L,
                    unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0,
                    lastMessageFromUser = doc.getBoolean("lastMessageFromUser") ?: true,
                    isResolved = doc.getBoolean("isResolved") ?: false
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Đánh dấu conversation là đã trả lời (admin reply)
     */
    suspend fun markConversationAsReplied(userUID: String) {
        firestore.collection("admin_inbox").document(userUID).update(
            mapOf(
                "unreadCount" to 0,
                "lastMessageFromUser" to false,
                "isResolved" to false
            )
        ).await()
    }

    /**
     * Lấy unread count cho admin
     */
    @Suppress("unused")
    suspend fun getAdminUnreadCount(): Int {
        return try {
            val snapshot = firestore.collection("admin_inbox")
                .whereGreaterThan("unreadCount", 0)
                .get().await()
            snapshot.size()
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Xóa conversation khỏi inbox (close ticket)
     */
    @Suppress("unused")
    suspend fun closeConversation(userUID: String) {
        firestore.collection("admin_inbox").document(userUID).update(
            mapOf("isResolved" to true)
        ).await()
    }

    /** ------ CREATE DEMO ADMIN ACCOUNT ------ */

    /**
     * Tạo tài khoản admin mẫu: admin@gmail.com / 123456
     * Dùng để test chức năng admin
     */
    @Suppress("unused")
    suspend fun createDemoAdminAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val adminEmail = "admin@gmail.com"
            val adminPassword = "123456"

            // Kiểm tra xem tài khoản đã tồn tại chưa
            val checkSnapshot = firestore.collection("users")
                .whereEqualTo("email", adminEmail)
                .get().await()

            if (!checkSnapshot.isEmpty) {
                onError("Tài khoản admin đã tồn tại")
                return
            }

            // Tạo user mới trong Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(adminEmail, adminPassword).await()
            val newUser = authResult.user

            if (newUser != null) {
                // Tạo document user trong Firestore với role "admin"
                val adminData = mapOf(
                    "uid" to newUser.uid,
                    "email" to adminEmail,
                    "displayName" to "Admin Support",
                    "role" to "admin",
                    "createdAt" to System.currentTimeMillis(),
                    "active" to true
                )

                firestore.collection("users").document(newUser.uid)
                    .set(adminData).await()

                android.util.Log.d("ChatRepository", "Demo admin account created successfully")
                onSuccess()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error creating admin account", e)
            onError("Lỗi: ${e.message}")
        }
    }
}
