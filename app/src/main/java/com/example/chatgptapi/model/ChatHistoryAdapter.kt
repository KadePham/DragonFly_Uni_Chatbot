package com.example.chatgptapi.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatHistoryAdapter(
    private val chats: List<Chat>,
    private val onClick: (Chat) -> Unit,
    private val onMoreClick: (anchor: View, chat: Chat) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txt: TextView = view.findViewById(R.id.iv2)
        val more: ImageButton = view.findViewById(R.id.iv3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        // Nếu chưa có lastUpdated thì dùng thời gian hiện tại
        val lastTime = chat.lastUpdated?.toDate() ?: Date()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val displayText = if (chat.title.isNullOrBlank() || chat.title == "New chat") {
            sdf.format(lastTime)
        } else {
            chat.title
        }

        holder.txt.text = displayText

        holder.itemView.setOnClickListener { onClick(chat) }
        holder.more.setOnClickListener { v ->
            onMoreClick(v, chat)
        }
    }


    override fun getItemCount() = chats.size
}
