package com.example.moodpredictorapp.SelectWayActivity.ChatMoodDetector

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
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
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

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
            btnFinish.isEnabled = false
            btnFinish.alpha = 0.5f
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
                            overridePendingTransition(R.anim.slide_up, 0)
                            btnFinish.isEnabled = true
                            btnFinish.alpha = 1.0f
                        } else {
                            addMessage(response, isUser = false)
                            btnFinish.isEnabled = true
                            btnFinish.alpha = 1.0f
                        }
                    } else {
                        addMessage(response, isUser = false)
                    }
                }
            }

            override fun onFailure(exception: IOException) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    btnFinish.isEnabled = true
                    btnFinish.alpha = 1.0f

                    if (isInternetUnavailable(this@ChatMoodDetector)) {
                        addMessage("Connect to the Internet and try again", isUser = false)
                    } else if (exception.message?.contains("busy", ignoreCase = true) == true || exception.message?.contains("server", ignoreCase = true) == true) {
                        addMessage("Servers are busy right now, try again later", isUser = false)
                    } else {
                        addMessage("Error: ${exception.message}", isUser = false)
                    }
                }
            }

            private fun isInternetUnavailable(context: Context): Boolean {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork ?: return true
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return true

                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> false
                    else -> true
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val messages = ArrayList<String>()
        val isUserList = ArrayList<Boolean>()

        for (message in chatMessages) {
            messages.add(message.message)
            isUserList.add(message.isUser as Boolean)
        }
        outState.putStringArrayList("chatMessages", messages)
        outState.putSerializable("isUserList", isUserList)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val messages = savedInstanceState.getStringArrayList("chatMessages") ?: return
        val isUserList = savedInstanceState.getSerializable("isUserList") as? ArrayList<*> ?: return

        for (i in messages.indices) {
            chatMessages.add(ChatMessage(messages[i], isUserList[i]))
        }

        chatAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(chatMessages.size - 1)
    }
}