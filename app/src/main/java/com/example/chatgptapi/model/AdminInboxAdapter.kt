package com.example.chatgptapi.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import java.text.SimpleDateFormat
import java.util.*

class AdminInboxAdapter(
    private val conversations: List<ConversationMetadata>,
    private val onItemClick: (ConversationMetadata) -> Unit
) : RecyclerView.Adapter<AdminInboxAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_inbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.bind(conversation, onItemClick)
    }

    override fun getItemCount() = conversations.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtUserName = itemView.findViewById<TextView>(R.id.txtUserName)
        private val txtLastMessage = itemView.findViewById<TextView>(R.id.txtLastMessage)
        private val txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        private val txtUnread = itemView.findViewById<TextView>(R.id.txtUnread)

        fun bind(conversation: ConversationMetadata, onItemClick: (ConversationMetadata) -> Unit) {
            txtUserName.text = conversation.userName
            txtLastMessage.text = conversation.lastMessage
            txtTime.text = formatTime(conversation.lastMessageTime)

            // Hiển thị badge unread count
            if (conversation.unreadCount > 0) {
                txtUnread.visibility = View.VISIBLE
                txtUnread.text = conversation.unreadCount.toString()
                txtUnread.setBackgroundResource(R.drawable.bg_unread_badge)
            } else {
                txtUnread.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(conversation)
            }
        }

        private fun formatTime(timeMillis: Long): String {
            return try {
                val date = Date(timeMillis)
                val now = System.currentTimeMillis()
                val diff = now - timeMillis

                when {
                    diff < 60000 -> "Vừa xong" // Less than 1 minute
                    diff < 3600000 -> "${diff / 60000}p" // Less than 1 hour
                    diff < 86400000 -> "${diff / 3600000}h" // Less than 1 day
                    else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                ""
            }
        }
    }
}

