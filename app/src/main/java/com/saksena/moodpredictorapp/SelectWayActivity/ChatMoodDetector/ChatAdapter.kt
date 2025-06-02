package com.saksena.moodpredictorapp.SelectWayActivity.ChatMoodDetector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saksena.moodpredictorapp.R

data class ChatMessage(
    val message: String,
    val isUser: Any
)

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tv_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 0) {
            R.layout.item_bot_chat_message
        } else {
            R.layout.item_user_chat_message
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser as Boolean) 1 else 0
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.message
        val alignment = if (message.isUser as Boolean) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
        holder.messageText.textAlignment = alignment
    }

    override fun getItemCount(): Int = messages.size
}
