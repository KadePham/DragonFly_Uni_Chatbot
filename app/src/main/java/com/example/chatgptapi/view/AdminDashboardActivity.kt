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

/**
 * AdminDashboardActivity - Chỉ dành cho Admin
 * Admin có thể xem và reply messages từ users
 * Sử dụng Realtime Database để lưu trữ messages
 */
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var backButton: ImageButton

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private val repo = ChatRepository()
    private var messagesListenerJob: Job? = null

    // Currently selected user UID
    private var currentUserUID: String? = null
    private var currentUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Find views
        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        titleTextView = findViewById(R.id.titleTextView)
        backButton = findViewById(R.id.backButton)

        // Get user UID from intent
        currentUserUID = intent.getStringExtra("userUID")
        currentUserName = intent.getStringExtra("userName")

        if (currentUserUID.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid user UID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // QUAN TRỌNG: Kiểm tra current user có phải admin không
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val isReallyAdmin = repo.isAdmin()
                withContext(Dispatchers.Main) {
                    if (!isReallyAdmin) {
                        Log.e("AdminDashboard", "User is not admin! Closing AdminDashboardActivity")
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Bạn không phải admin",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return@withContext
                    }

                    // Setup RecyclerView
                    val adminUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    adapter = ChatAdapter(messages, adminUID)  // Pass admin's UID
                    recyclerView.layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
                    recyclerView.adapter = adapter

                    // Set title with user name
                    val title = "Chat với ${currentUserName ?: "User"}"
                    titleTextView.text = title

                    // Back button
                    backButton.setOnClickListener {
                        finish()
                    }

                    // Load messages for this user realtime
                    loadAdminChatMessages(currentUserUID!!)

                    // Send button click
                    sendButton.setOnClickListener {
                        val text = inputEditText.text.toString().trim()
                        if (text.isNotEmpty()) {
                            sendAdminReply(text)
                            inputEditText.text.clear()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminDashboard", "Error checking admin status: ${e.message}", e)
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Lỗi kiểm tra quyền",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun loadAdminChatMessages(userUID: String) {
        messagesListenerJob?.cancel()
        messagesListenerJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load from Realtime Database (simpler and faster)
                repo.getAdminMessagesForUserRealtime(userUID).collect { list ->
                    withContext(Dispatchers.Main) {
                        messages.clear()
                        messages.addAll(list)
                        // Use notifyDataSetChanged() instead of notifyItemRangeChanged()
                        adapter.notifyDataSetChanged()
                        if (messages.isNotEmpty()) {
                            recyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Error loading messages: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Lỗi tải tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendAdminReply(text: String) {
        if (currentUserUID.isNullOrEmpty()) return

        val currentAdmin = FirebaseAuth.getInstance().currentUser ?: return
        val adminName = currentAdmin.displayName ?: "Admin"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // QUAN TRỌNG: Kiểm tra user thực sự là admin trước khi gửi
                val isReallyAdmin = repo.isAdmin()
                if (!isReallyAdmin) {
                    withContext(Dispatchers.Main) {
                        Log.e("AdminDashboard", "User ${currentAdmin.uid} không phải admin! Từ chối gửi message")
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Bạn không phải admin",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Lấy role thực tế từ Firestore
                val actualRole = repo.getUserRoleString(currentAdmin.uid)

                val message = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = ChatRepository.ADMIN_CHAT_ID,
                    isUser = true,  // Admin sending message = true (right side)
                    content = text,
                    timestamp = Timestamp.now(),
                    senderUid = currentAdmin.uid,
                    senderName = adminName,
                    senderRole = actualRole // Dùng role thực tế từ Firestore
                )

                // Send reply using repository
                repo.sendAdminReply(currentUserUID!!, message)

                // Đánh dấu conversation là đã reply
                repo.markConversationAsReplied(currentUserUID!!)

                Log.d("AdminDashboard", "Reply sent: $text with role: $actualRole")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminDashboard", "Error sending reply: ${e.message}", e)
                    Toast.makeText(
                        this@AdminDashboardActivity,
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



