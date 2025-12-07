package com.example.chatgptapi.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserUID: String? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
    }

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]

        // If currentUserUID is provided, use it to determine who sent the message
        return if (currentUserUID != null) {
            if (msg.senderUid == currentUserUID) {
                TYPE_USER  // Current user's message → Right side
            } else {
                TYPE_BOT   // Other person's message → Left side
            }
        } else {
            // Fallback to isUser field if currentUserUID not provided
            if (msg.isUser) TYPE_USER else TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val view = inflater.inflate(R.layout.item_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is UserViewHolder) holder.bind(msg)
        if (holder is BotViewHolder) holder.bind(msg)
    }

    override fun getItemCount() = messages.size

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val date = Date(timestamp.seconds * 1000)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtSender = view.findViewById<TextView>(R.id.txtSender)
        private val txtUser = view.findViewById<TextView>(R.id.txtUser)
        private val txtTimestamp = view.findViewById<TextView?>(R.id.txtTimestamp)

        fun bind(message: Message) {
            // Hiển thị tên người gửi nếu có
            if (message.senderName.isNotEmpty()) {
                txtSender.visibility = View.VISIBLE
                txtSender.text = message.senderName
            } else {
                txtSender.visibility = View.GONE
            }
            txtUser.text = message.content

            // Hiển thị timestamp
            txtTimestamp?.let {
                val timeText = formatTimestamp(message.timestamp)
                if (timeText.isNotEmpty()) {
                    it.visibility = View.VISIBLE
                    it.text = timeText
                } else {
                    it.visibility = View.GONE
                }
            }
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            if (timestamp == null) return ""
            val date = Date(timestamp.seconds * 1000)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
    }

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtSender = view.findViewById<TextView>(R.id.txtSender)
        private val txtBot = view.findViewById<TextView>(R.id.txtBot)
        private val txtTimestamp = view.findViewById<TextView?>(R.id.txtTimestamp)

        fun bind(message: Message) {
            // Hiển thị tên admin
            if (message.senderName.isNotEmpty()) {
                txtSender.visibility = View.VISIBLE
                txtSender.text = message.senderName
            } else {
                txtSender.visibility = View.GONE
            }
            txtBot.text = message.content

            // Hiển thị timestamp
            txtTimestamp?.let {
                val timeText = formatTimestamp(message.timestamp)
                if (timeText.isNotEmpty()) {
                    it.visibility = View.VISIBLE
                    it.text = timeText
                } else {
                    it.visibility = View.GONE
                }
            }
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            if (timestamp == null) return ""
            val date = Date(timestamp.seconds * 1000)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
    }
}

