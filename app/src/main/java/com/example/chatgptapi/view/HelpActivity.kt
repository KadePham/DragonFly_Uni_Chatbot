package com.example.chatgptapi.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatgptapi.R
import com.example.chatgptapi.databinding.ActivityHelpBinding
import com.example.chatgptapi.model.ChatAdapter
import com.example.chatgptapi.model.Message
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * HelpActivity - Messenger-style chat with admin support
 * Replaces AdminChatActivity with cleaner UI and better role handling
 */
class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private val repo = ChatRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private var messagesListenerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        val userUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        adapter = ChatAdapter(messages, userUID)  // Pass user's UID
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Set title
        binding.titleTextView.text = getString(R.string.admin_chat_title)

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Load messages realtime
        loadMessagesRealtime()

        // Send button click
        binding.sendButton.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                sendHelpMessage(text)
                binding.inputEditText.text.clear()
            }
        }
    }

    private fun loadMessagesRealtime() {
        messagesListenerJob?.cancel()
        messagesListenerJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load from Firestore for user to see admin replies properly
                // Firestore has complete message data including senderUid
                val userUID = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                repo.getAdminMessagesFromFirestore(userUID).collect { list ->
                    withContext(Dispatchers.Main) {
                        messages.clear()
                        messages.addAll(list)
                        adapter.notifyDataSetChanged()
                        if (messages.isNotEmpty()) {
                            binding.recyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Job was cancelled, normal flow
                Log.d("HelpActivity", "Messages listener cancelled")
                throw e // re-throw to properly handle cancellation
            } catch (e: Exception) {
                Log.e("HelpActivity", "Error loading messages: ${e.message}", e)
            }
        }
    }

    private fun sendHelpMessage(text: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val senderName = currentUser.displayName ?: "User"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get role from Firestore - single source of truth
                val userRole = repo.getUserRoleString(currentUser.uid)

                Log.d("HelpActivity", "User role from Firestore: $userRole")

                // Create message with correct role
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = ChatRepository.ADMIN_CHAT_ID,
                    isUser = true,
                    content = text,
                    timestamp = Timestamp.now(),
                    senderUid = currentUser.uid,
                    senderName = senderName,
                    senderRole = userRole  // Use role from Firestore
                )

                // Send message
                repo.sendAdminMessage(message)
                Log.d("HelpActivity", "Message sent to admin: $text with role: $userRole")

                // Also save to Firestore for admin to see in dashboard
                try {
                    val firestoreRef = firestore.collection("users").document(currentUser.uid)
                        .collection("conversations")
                        .document(ChatRepository.ADMIN_CHAT_ID)
                        .collection("messages")
                        .document(message.id)
                    firestoreRef.set(message.toMap()).await()
                    Log.d("HelpActivity", "Message also saved to Firestore")
                } catch (e: Exception) {
                    Log.e("HelpActivity", "Error saving to Firestore: ${e.message}", e)
                }

                // Update conversation metadata
                repo.updateConversationMetadata(
                    userUID = currentUser.uid,
                    userName = senderName,
                    userEmail = currentUser.email ?: "",
                    lastMessage = text
                )

                // Update Realtime DB with correct role (for chat UI)
                repo.saveUserInfoToRealtimeDB(
                    currentUser.uid,
                    senderName,
                    currentUser.email ?: "",
                    userRole
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HelpActivity,
                        "Tin nhắn đã gửi",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HelpActivity", "Error sending message: ${e.message}", e)
                    Toast.makeText(
                        this@HelpActivity,
                        "Gửi tin nhắn thất bại: ${e.message}",
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

