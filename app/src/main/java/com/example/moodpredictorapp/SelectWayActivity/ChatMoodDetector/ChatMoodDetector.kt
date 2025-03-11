package com.example.moodpredictorapp.SelectWayActivity.ChatMoodDetector

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodpredictorapp.R
import com.example.moodpredictorapp.ConfigureMenuResult.SelectedMoodResult
import com.example.moodpredictorapp.SelectWayActivity.ChatMoodDetector.GeminiAPI.ResponseCallback
import java.io.IOException

class ChatMoodDetector : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var btnFinish: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var progressBar: ProgressBar
    private val geminiAPI = GeminiAPI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_mood_detector)

        recyclerView = findViewById(R.id.rv_chat)
        messageInput = findViewById(R.id.et_message)
        sendButton = findViewById(R.id.btn_send)
        btnFinish = findViewById(R.id.btn_finish)
        progressBar = findViewById(R.id.progressBar)

        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage()
                true
            } else {
                false
            }
        }

        btnFinish.setOnClickListener {
            handleUserMessage("Analyze mood", true)
        }
    }

    private fun sendMessage() {
        val userMessage = messageInput.text.toString().trim()
        if (userMessage.isNotEmpty()) {
            handleUserMessage(userMessage, false)
            messageInput.text.clear()
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(message, isUser))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun handleUserMessage(message: String, isAnalyzingMood: Boolean) {
        progressBar.visibility = ProgressBar.VISIBLE

        if (isAnalyzingMood) {
            addMessage(getString(R.string.analyzing_mood), isUser = false)
        } else {
            addMessage(message, isUser = true)
        }

        geminiAPI.generateReply(message, isAnalyzingMood, object : ResponseCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE

                    if (isAnalyzingMood) {
                        val moodCategories = listOf("Happy", "Sad", "Neutral", "Angry", "Surprise")
                        val mood = geminiAPI.extractMood(response)
                        if (mood != null && moodCategories.contains(mood)) {
                            addMessage("The detected mood is: $mood", isUser = false)
                            geminiAPI.clearConversationHistory()
                            val intent = Intent(this@ChatMoodDetector, SelectedMoodResult::class.java)
                            intent.putExtra("selected_item_name", mood)

                            startActivity(intent)
                        } else {
                            addMessage(response, isUser = false) // Display general message
                        }
                    } else {
                        addMessage(response, isUser = false)
                    }
                }
            }

            override fun onFailure(exception: IOException) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    addMessage("Error: ${exception.message}", isUser = false)
                }
            }
        })
    }
}