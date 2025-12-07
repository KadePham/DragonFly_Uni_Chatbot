package com.example.chatgptapi.view

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chatgptapi.R
import com.example.chatgptapi.databinding.ActivityMainBinding
import com.example.chatgptapi.databinding.NavHeaderBinding
import com.example.chatgptapi.model.Chat
import com.example.chatgptapi.model.ChatAdapter
import com.example.chatgptapi.model.ChatHistoryAdapter
import com.example.chatgptapi.model.Message
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.cancellation.CancellationException
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val chatHistory = mutableListOf<Chat>()
    private lateinit var historyAdapter: ChatHistoryAdapter

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private var currentChatId: String? = null
    private var isInitialized = false  // Flag to prevent multiple initializations

    // Firestore repository (thay Room)
    private val repo = ChatRepository()

    private var messagesListenerJob: Job? = null
    private var conversationsListenerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DON'T call ensureUserExists() here - it was causing role reset
        // ensureUserExists() is already called in LoginActivity on first login

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val headerBinding = NavHeaderBinding.bind(binding.navView)

        historyAdapter = ChatHistoryAdapter(
            chatHistory,
            onClick = { chat ->
                loadChat(chat.id)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            },
            onMoreClick = { anchor, chat ->
                showChatOptions(anchor, chat)
            }
        )
        headerBinding.rvChatList.adapter = historyAdapter

        // Set user name from Firebase
        updateUserNameInProfile(headerBinding)

        setupSidebar()
        setupRecyclerView()

        // Restore initialization state from savedInstanceState
        isInitialized = savedInstanceState?.getBoolean("isInitialized", false) ?: false
        currentChatId = savedInstanceState?.getString("currentChatId")

        // Only initialize listeners once per activity lifecycle
        if (!isInitialized) {
            // Start listening to conversations realtime
            conversationsListenerJob?.cancel()
            conversationsListenerJob = lifecycleScope.launch {
                try {
                    repo.getConversationsRealtime().collect { list ->
                        withContext(Dispatchers.Main) {
                            chatHistory.clear()
                            chatHistory.addAll(list)
                            historyAdapter.notifyDataSetChanged()
                        }
                    }
                } catch (e: CancellationException) {
                    // Job was cancelled, don't log as error
                    Log.d("MainActivity", "conversations listener cancelled")
                    throw e // re-throw to properly cancel the coroutine
                } catch (e: Exception) {
                    Log.e("MainActivity", "conversations listener error: ${e.message}", e)
                }
            }

            // Create a new chat by default if none open
            if (currentChatId == null) {
                createNewChat()
            }

            isInitialized = true
        }

        // Nút mở sidebar
        binding.headerLogo.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Gửi tin nhắn
        binding.sendButton.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                // show message immediately in UI
                sendMessage(text)
                // save to Firestore
                saveMessageToDB(text, true)
                // call backend bot (python/other) - backend trả về reply và call receiveMessage()
                callPythonBot(text)
                binding.inputEditText.text.clear()
            }
        }

        headerBinding.navProfile.setOnClickListener {
            val sheet = ProfileBottomSheet()
            sheet.onLogoutClick = {
                // Tùy chọn confirm dialog trước khi logout:
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất khỏi tài khoản này?")
                        .setPositiveButton("Đăng xuất") { _, _ ->
                            logout()
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                }
            }
            // Callback để cập nhật tên trong navbar khi user sửa tên
            sheet.onNameUpdated = {
                updateUserNameInProfile(headerBinding)
            }
            sheet.show(supportFragmentManager, "profile_bottom")
        }
    }

    // Hiện dialog YES/NO confirm xóa
    private fun showDeleteConfirmDialog(chat: Chat) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Xoá chat")
        builder.setMessage("Bạn có chắc muốn xoá cuộc chat này không?")
        builder.setPositiveButton("Có") { dialog, _ ->
            dialog.dismiss()
            deleteChat(chat) // thực hiện xóa
        }
        builder.setNegativeButton("Không") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Thực hiện xóa conversation trên Firestore và cập nhật UI
    private fun deleteChat(chat: Chat) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repo.deleteConversation(chat.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Đã xoá chat", Toast.LENGTH_SHORT).show()
                    // nếu đang mở chat vừa xóa thì clear message list
                    if (currentChatId == chat.id) {
                        messages.clear()
                        adapter.notifyDataSetChanged()
                        currentChatId = null
                    }
                    // conversations realtime sẽ cập nhật sidebar
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Xoá thất bại: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Sidebar
    private fun setupSidebar() {
        val headerBinding = NavHeaderBinding.bind(binding.navView)

        val rv = headerBinding.rvChatList
        val btnNewChat = headerBinding.itemNewChat
        val btnSearch = headerBinding.itemSearch
        val btnAdminChat = binding.navView.findViewById<View>(com.google.android.material.R.id.radio)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = historyAdapter

        btnNewChat.setOnClickListener {
            createNewChat()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnSearch.setOnClickListener {
            // Open search dialog
            val searchDialog = SearchDialog(
                chatHistory,
                onMoreClick = { anchor, chat ->
                    showChatOptions(anchor, chat)
                }
            )
            searchDialog.setOnChatSelected { chat ->
                loadChat(chat.id)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            searchDialog.show(supportFragmentManager, "search_dialog")
        }

        btnAdminChat?.setOnClickListener {
            startActivity(Intent(this, AdminChatActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // loadChatHistory() không cần nữa vì realtime listener đã chạy
    }

    // Nếu vẫn cần gọi thủ công (ví dụ để migrate) có thể implement ở đây.
    private fun loadChatHistory() {
        // no-op: conversations realtime listener cập nhật sidebar tự động
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    /** ------------------ TẠO CHAT MỚI (Firestore) ------------------ */
    private fun createNewChat() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Vui lòng đăng nhập", Toast.LENGTH_SHORT)
                            .show()
                    }
                    return@launch
                }

                val convId = repo.createConversation("New chat")
                currentChatId = convId
                Log.d("MainActivity", "Tạo chat mới (firestore) id=$currentChatId")
                withContext(Dispatchers.Main) {
                    messages.clear()
                    adapter.notifyDataSetChanged()
                }
            } catch (e: CancellationException) {
                Log.d("MainActivity", "createNewChat cancelled")
                throw e // re-throw cancellation
            } catch (e: Exception) {
                Log.e("MainActivity", "createNewChat lỗi: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Tạo chat thất bại", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /** ------------------ HIỂN THỊ TIN NHẮN ------------------ */
    private fun sendMessage(text: String) {
        val convId = currentChatId
        if (convId.isNullOrEmpty()) {
            Toast.makeText(this, "Không có conversation hiện tại", Toast.LENGTH_SHORT).show()
            return
        }
        val msg = Message(
            id = UUID.randomUUID().toString(),
            chatId = convId,
            isUser = true,
            content = text,
            timestamp = Timestamp.now()
        )
        messages.add(msg)
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun receiveMessage(text: String) {
        val convId = currentChatId
        if (convId.isNullOrEmpty()) {
            Log.e("MainActivity", "receiveMessage: no convId")
            return
        }
        val msg = Message(
            id = UUID.randomUUID().toString(),
            chatId = convId,
            isUser = false,
            content = text,
            timestamp = Timestamp.now()
        )
        messages.add(msg)
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
        // lưu reply vào Firestore
        saveMessageToDB(text, false)
    }

    /** ------------------ LƯU (Firestore) ------------------ */
    private fun saveMessageToDB(text: String, isUser: Boolean) {
        val convId = currentChatId
        if (convId.isNullOrEmpty()) {
            Log.e("MainActivity", "saveMessageToDB: currentChatId null")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val uid = currentUser?.uid ?: return@launch
                val displayName = currentUser.displayName ?: "User"

                // Lấy role từ Firestore
                val userRole = repo.getUserRoleString(uid)

                val msg = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = convId,
                    isUser = isUser,
                    content = text,
                    timestamp = Timestamp.now(),
                    senderUid = uid,
                    senderName = displayName,
                    senderRole = userRole
                )

                repo.sendMessage(convId, msg)
                Log.d("MainActivity", "Saved message to Firestore conv=$convId with role=$userRole")
            } catch (e: Exception) {
                Log.e("MainActivity", "saveMessageToDB error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Lưu message thất bại", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /** ------------------ API (backend) ------------------ */
    private fun callPythonBot(message: String) {
        val url = "https://ruinable-battlesome-stasia.ngrok-free.dev/chat"

        // Server yêu cầu field tên là "question" (không phải "message")
        val json = JSONObject().apply {
            put("question", message)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, json,
            { response ->
                Log.d("API", "Server OK: $response")
                val reply = when {
                    response.has("reply") -> response.optString("reply", "")
                    response.has("answer") -> response.optString("answer", "")
                    response.has("result") -> response.optString("result", "")
                    else -> response.optString("reply", "")
                }
                if (reply.isNotEmpty()) receiveMessage(reply)
                else {
                    Log.e("API", "Unexpected response JSON: $response")
                    receiveMessage("Server trả dữ liệu không như mong đợi")
                }
            },
            { error ->
                val networkResponse = error.networkResponse
                if (networkResponse != null) {
                    val statusCode = networkResponse.statusCode
                    val data = networkResponse.data
                    val body = try {
                        String(data, Charsets.UTF_8)
                    } catch (e: Exception) {
                        "Can't decode body"
                    }
                    Log.e("API", "HTTP error. status=$statusCode, body=$body", error)
                    receiveMessage("Lỗi từ server: $statusCode")
                } else {
                    Log.e("API", "Network error (no response)", error)
                    receiveMessage("Lỗi kết nối server")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(120_000, 0, 1f)
        Volley.newRequestQueue(this).add(request)
        Log.d("API", "POST $url body=$json")
    }

    /** ------------------ LOAD LỊCH SỬ CHAT (realtime via repo) ------------------ */
    private fun loadChat(chatId: String) {
        // cancel previous messages subscription
        messagesListenerJob?.cancel()
        messagesListenerJob = lifecycleScope.launch {
            try {
                repo.getMessagesRealtime(chatId).collect { list ->
                    withContext(Dispatchers.Main) {
                        messages.clear()
                        messages.addAll(list)
                        adapter.notifyDataSetChanged()
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            } catch (e: CancellationException) {
                Log.d("MainActivity", "loadChat listener cancelled for chatId=$chatId")
                throw e // re-throw cancellation
            } catch (e: Exception) {
                Log.e("MainActivity", "loadChat error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Không thể load messages", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        currentChatId = chatId
    }

    /** ------------------  MORE DIALOG  ------------------ */
    private fun showChatOptions(anchor: View, chat: Chat) {
        // Inflate popup layout
        val popupView = layoutInflater.inflate(R.layout.popup_chat_options, null)

        // Create PopupWindow
        val popup = android.widget.PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.elevation = 8f
        }

        popup.isOutsideTouchable = true
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popup.showAsDropDown(anchor, -anchor.width - 16, 0)

        val rowShare = popupView.findViewById<View>(R.id.rowshare)
        val rowStartGroup = popupView.findViewById<View>(R.id.rowstartgroup)
        val rowRename = popupView.findViewById<View>(R.id.rowrename)
        val rowArchive = popupView.findViewById<View>(R.id.rowarchive)
        val rowDelete = popupView.findViewById<View>(R.id.rowdelete)

        rowShare.setOnClickListener {
            popup.dismiss()
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Chat from app: ${chat.id}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ chat"))
        }

        rowStartGroup.setOnClickListener {
            popup.dismiss()
            Toast.makeText(this, "Bắt đầu đoạn chat nhóm", Toast.LENGTH_SHORT).show()
        }

        rowRename.setOnClickListener {
            popup.dismiss()
            showRenameDialog(chat)
        }

        rowArchive.setOnClickListener {
            popup.dismiss()
            // archive -> current implementation: delete conversation (or you can set an "archived" flag in Firestore)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.deleteConversation(chat.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Đã lưu trữ chat", Toast.LENGTH_SHORT)
                            .show()
                        if (currentChatId == chat.id) {
                            messages.clear()
                            adapter.notifyDataSetChanged()
                            currentChatId = null
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Lưu trữ thất bại: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        rowDelete.setOnClickListener {
            popup.dismiss()
            showDeleteConfirmDialog(chat)
        }
    }

    /** ------------------  RENAME DIALOG  ------------------ */
    private fun showRenameDialog(chat: Chat) {
        val input = EditText(this)
        input.setText(chat.title ?: "")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Đổi tên chat")
            .setView(input)
            .setPositiveButton("Lưu") { d, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.updateConversationTitle(chat.id, newName)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Đổi tên thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Đổi tên thất bại: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else {
                    d.dismiss()
                }
            }
            .setNegativeButton("Huỷ") { d, _ -> d.dismiss() }
            .show()
    }

    /** ------------------  LOG OUT  ------------------ */
    private fun logout() {
        // 1) Sign out Google nếu có
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            googleClient.signOut().addOnCompleteListener {
                // optional: revokeAccess if you want full disconnect
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2) Firebase sign out
        try {
            Firebase.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3) Clear local saved data (tên file prefs có thể khác)
        try {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4) Optional: delete FCM token (best-effort)
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().deleteToken()
        } catch (e: Exception) {
            // ignore
        }

        // 5) Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        // lifecycleScope.launch(Dispatchers.IO) {
        //     AppDatabase.getInstance(this@MainActivity).clearAllTables()
        // }

        startActivity(intent)
        finishAffinity()
    }

    /** ------------------  UPDATE USER NAME IN PROFILE  ------------------ */
    private fun updateUserNameInProfile(headerBinding: NavHeaderBinding) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userName = currentUser?.displayName ?: "User"
        headerBinding.nameProfile.text = userName

        // Get user role and display in subscription field
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userRole = repo.getUserRoleString()
                withContext(Dispatchers.Main) {
                    // Display role: "Admin" or "User"
                    val displayRole = if (userRole == "admin") "Admin" else "User"
                    headerBinding.subcription.text = displayRole
                    Log.d("MainActivity", "User role updated: $displayRole")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    headerBinding.subcription.text = "User"
                    Log.e("MainActivity", "Error getting user role: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListenerJob?.cancel()
        conversationsListenerJob?.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isInitialized", isInitialized)
        outState.putString("currentChatId", currentChatId)
    }
}
