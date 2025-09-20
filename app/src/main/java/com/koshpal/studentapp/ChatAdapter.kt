package com.koshpal.studentapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ChatAdapter(private val messages: MutableList<ChatMessage>) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
        private const val VIEW_TYPE_TYPING = 3
    }

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMessage: MaterialCardView = itemView.findViewById(R.id.cardUserMessage)
        val tvMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvUserTimestamp)
    }

    inner class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMessage: MaterialCardView = itemView.findViewById(R.id.cardBotMessage)
        val tvMessage: TextView = itemView.findViewById(R.id.tvBotMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvBotTimestamp)
    }

    inner class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMessage: MaterialCardView = itemView.findViewById(R.id.cardTyping)
        val tvTyping: TextView = itemView.findViewById(R.id.tvTyping)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isTyping -> VIEW_TYPE_TYPING
            message.isFromUser -> VIEW_TYPE_USER
            else -> VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_BOT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_bot, parent, false)
                BotMessageViewHolder(view)
            }
            VIEW_TYPE_TYPING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_typing, parent, false)
                TypingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is UserMessageViewHolder -> {
                holder.tvMessage.text = message.message
                holder.tvTimestamp.text = message.timestamp
            }
            is BotMessageViewHolder -> {
                holder.tvMessage.text = message.message
                holder.tvTimestamp.text = message.timestamp
            }
            is TypingViewHolder -> {
                // Typing indicator will be handled by the layout animation
                holder.tvTyping.text = "AI is typing..."
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeTypingIndicator() {
        val typingIndex = messages.indexOfFirst { it.isTyping }
        if (typingIndex != -1) {
            messages.removeAt(typingIndex)
            notifyItemRemoved(typingIndex)
        }
    }

    fun addTypingIndicator() {
        // Remove existing typing indicator first
        removeTypingIndicator()
        
        val typingMessage = ChatMessage.createTypingIndicator()
        messages.add(typingMessage)
        notifyItemInserted(messages.size - 1)
    }

    fun updateLastBotMessage(newMessage: String) {
        val lastBotIndex = messages.indexOfLast { !it.isFromUser && !it.isTyping }
        if (lastBotIndex != -1) {
            val updatedMessage = messages[lastBotIndex].copy(message = newMessage)
            messages[lastBotIndex] = updatedMessage
            notifyItemChanged(lastBotIndex)
        }
    }
}
