package com.example.chatgptapi.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import com.example.chatgptapi.model.Chat
import com.example.chatgptapi.model.ChatHistoryAdapter

class SearchDialog(
    private val chatHistory: List<Chat>,
    private val onMoreClick: ((View, Chat) -> Unit)? = null
) : DialogFragment() {

    private var onChatSelected: ((Chat) -> Unit)? = null
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatHistoryAdapter
    private val filteredChats = mutableListOf<Chat>()

    fun setOnChatSelected(callback: (Chat) -> Unit) {
        onChatSelected = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set fullscreen style
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_search_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        recyclerView = view.findViewById(R.id.searchRecyclerView)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseSearch)

        // Setup RecyclerView
        filteredChats.addAll(chatHistory)
        adapter = ChatHistoryAdapter(
            filteredChats,
            onClick = { chat ->
                onChatSelected?.invoke(chat)
                dismiss()
            },
            onMoreClick = { anchor, chat ->
                // Forward more click to parent callback if provided
                onMoreClick?.invoke(anchor, chat)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Search input listener
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterChats(s.toString())
            }
        })

        // Close button
        btnClose.setOnClickListener {
            dismiss()
        }

        // Focus search input
        searchInput.requestFocus()
    }

    private fun filterChats(query: String) {
        filteredChats.clear()

        if (query.isEmpty()) {
            // Show all chats if query is empty
            filteredChats.addAll(chatHistory)
        } else {
            // Filter chats by title (case-insensitive)
            val lowercaseQuery = query.lowercase()
            filteredChats.addAll(
                chatHistory.filter { chat ->
                    chat.title.lowercase().contains(lowercaseQuery)
                }
            )
        }

        adapter.notifyDataSetChanged()
    }
}

