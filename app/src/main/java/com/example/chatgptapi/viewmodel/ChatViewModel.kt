package com.example.chatgptapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import com.example.chatgptapi.model.Chat
import com.example.chatgptapi.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()

    // LiveData list of conversations
    val conversations = repo.getConversationsRealtime().asLiveData()

    // Expose messages for current conv as LiveData via Flow-as-LiveData
    fun messagesLive(convId: String) = repo.getMessagesRealtime(convId).asLiveData()

    // Create conv + return id
    fun createConversation(title: String = "New chat", onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repo.createConversation(title)
                onResult(id)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun sendMessage(convId: String, message: Message, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.sendMessage(convId, message)
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }

    fun deleteConversation(convId: String, onDone: (Boolean)->Unit = {}) {
        viewModelScope.launch {
            try {
                repo.deleteConversation(convId)
                onDone(true)
            } catch(e: Exception) {
                onDone(false)
            }
        }
    }

    fun updateMessage(convId: String, messageId: String, newContent: String, onDone: (Boolean)->Unit = {}) {
        viewModelScope.launch {
            try {
                repo.updateMessage(convId, messageId, newContent)
                onDone(true)
            } catch(e: Exception) {
                onDone(false)
            }
        }
    }
}
