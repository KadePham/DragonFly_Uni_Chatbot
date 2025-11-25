package com.example.chatgptapi.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chatgptapi.databinding.ActivityMainBinding
import com.example.chatgptapi.model.ChatAdapter
import com.example.chatgptapi.model.Message
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            val userMsg = binding.inputEditText.text.toString().trim()
            if (userMsg.isNotEmpty()) {
                sendMessage(userMsg)
                callPythonBot(userMsg)
                binding.inputEditText.text.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        messages.add(Message(text, true)) // user
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun receiveMessage(text: String) {
        messages.add(Message(text, false)) // bot
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun callPythonBot(message: String) {
        val url = "https://e4bba18f54d2.ngrok-free.app/chat"


        val json = JSONObject().put("message", message)

        val request = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                val reply = response.getString("reply")
                receiveMessage(" $reply")
            },
            { error ->
                error.printStackTrace()
                receiveMessage(" Lỗi kết nối server Flask: ${error.message}")
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}
