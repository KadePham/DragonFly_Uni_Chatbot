package com.example.chatgptapi.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import com.example.chatgptapi.model.ConversationMetadata
import com.example.chatgptapi.model.AdminInboxAdapter
import com.example.chatgptapi.viewmodel.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminInboxActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var adapter: AdminInboxAdapter

    private val conversations = mutableListOf<ConversationMetadata>()
    private val repo = ChatRepository()
    private var inboxListenerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_inbox)

        // Find views
        recyclerView = findViewById(R.id.recyclerView)
        titleTextView = findViewById(R.id.titleTextView)

        // QUAN TRỌNG: Kiểm tra current user có phải admin không
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val isReallyAdmin = repo.isAdmin()
                withContext(Dispatchers.Main) {
                    if (!isReallyAdmin) {
                        Log.e("AdminInboxActivity", "User is not admin! Closing AdminInboxActivity")
                        Toast.makeText(
                            this@AdminInboxActivity,
                            getString(R.string.admin_access_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return@withContext
                    }

                    // Setup RecyclerView
                    adapter = AdminInboxAdapter(
                        conversations,
                        onItemClick = { conversation ->
                            // Mở AdminDashboardActivity với user này
                            val intent = Intent(this@AdminInboxActivity, AdminDashboardActivity::class.java)
                            intent.putExtra("userUID", conversation.userId)
                            intent.putExtra("userName", conversation.userName)
                            startActivity(intent)
                        }
                    )
                    recyclerView.layoutManager = LinearLayoutManager(this@AdminInboxActivity)
                    recyclerView.adapter = adapter

                    // Set title
                    titleTextView.text = getString(R.string.admin_inbox_title)


                    // Load inbox realtime
                    loadAdminInboxRealtime()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminInboxActivity", "Error checking admin status: ${e.message}", e)
                    Toast.makeText(
                        this@AdminInboxActivity,
                        getString(R.string.admin_error_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun loadAdminInboxRealtime() {
        inboxListenerJob?.cancel()
        inboxListenerJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                repo.getAdminInboxRealtime().collect { list ->
                    withContext(Dispatchers.Main) {
                        conversations.clear()
                        conversations.addAll(list)
                        // Use notifyDataSetChanged() instead of notifyItemRangeChanged()
                        // to avoid IndexOutOfBoundsException when list size changes
                        adapter.notifyDataSetChanged()

                        // Hiển thị message nếu không có conversation
                        if (conversations.isEmpty()) {
                            Toast.makeText(
                                this@AdminInboxActivity,
                                getString(R.string.admin_no_unread_messages),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdminInboxActivity", "Error: ${e.message}", e)
                    Toast.makeText(
                        this@AdminInboxActivity,
                        "Lỗi tải dữ liệu: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inboxListenerJob?.cancel()
    }
}

