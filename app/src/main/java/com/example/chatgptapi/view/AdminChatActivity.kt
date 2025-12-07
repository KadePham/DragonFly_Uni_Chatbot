package com.example.chatgptapi.view

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import com.example.chatgptapi.model.ChatAdapter
import com.example.chatgptapi.model.Message
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AdminChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var backButton: ImageButton

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private val repo = ChatRepository()
    private var messagesListenerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat)

        // Find views
        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        titleTextView = findViewById(R.id.titleTextView)
        backButton = findViewById(R.id.backButton)

        // Setup RecyclerView
        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set title
        titleTextView.text = getString(R.string.admin_chat_title)

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Get or create admin chat
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val adminChatId = repo.getOrCreateAdminChat()
                Log.d("AdminChatActivity", "Admin chat ID: $adminChatId")

                // DON'T set role here - let sendAdminMessage fetch it from Firestore
                // Mở messages realtime
                withContext(Dispatchers.Main) {
                    messagesListenerJob?.cancel()
                    messagesListenerJob = lifecycleScope.launch {
                        try {
                            repo.getAdminMessagesRealtime().collect { list ->
                                withContext(Dispatchers.Main) {
                                    messages.clear()
                                    messages.addAll(list)
                                    adapter.notifyDataSetChanged()
                                    if (messages.isNotEmpty()) {
                                        recyclerView.scrollToPosition(messages.size - 1)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminChatActivity", "Error loading messages: ${e.message}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminChatActivity", "Error: ${e.message}", e)
                    Toast.makeText(
                        this@AdminChatActivity,
                        "Lỗi kết nối: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Send button click
        sendButton.setOnClickListener {
            val text = inputEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                sendAdminMessage(text)
                inputEditText.text.clear()
            }
        }
    }

    private fun sendAdminMessage(text: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val senderName = currentUser.displayName ?: "User"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Lấy role từ Firestore
                val userRole = repo.getUserRoleString(currentUser.uid)

                // Cập nhật user info trong Realtime DB với role mới từ Firestore
                repo.saveUserInfoToRealtimeDB(
                    currentUser.uid,
                    senderName,
                    currentUser.email ?: "",
                    userRole
                )

                val message = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = ChatRepository.ADMIN_CHAT_ID,
                    isUser = true,
                    content = text,
                    timestamp = Timestamp.now(),
                    senderUid = currentUser.uid,
                    senderName = senderName,
                    senderRole = userRole
                )

                repo.sendAdminMessage(message)

                // Update conversation metadata để admin thấy notification
                repo.updateConversationMetadata(
                    userUID = currentUser.uid,
                    userName = senderName,
                    userEmail = currentUser.email ?: "",
                    lastMessage = text
                )

                // Log đúng giá trị role được dùng
                Log.d("AdminChatActivity", "Message sent: $text with role: $userRole")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminChatActivity", "Error sending message: ${e.message}", e)
                    Toast.makeText(
                        this@AdminChatActivity,
                        "Gửi tin nhắn thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListenerJob?.cancel()
    }
}
