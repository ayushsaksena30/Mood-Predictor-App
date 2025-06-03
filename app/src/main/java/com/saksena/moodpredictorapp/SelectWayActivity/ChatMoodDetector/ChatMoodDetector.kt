package com.saksena.moodpredictorapp.SelectWayActivity.ChatMoodDetector

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saksena.moodpredictorapp.R
import com.saksena.moodpredictorapp.ConfigureMenuResult.SelectedMoodResult
import com.saksena.moodpredictorapp.SelectWayActivity.ChatMoodDetector.GeminiAPI.ResponseCallback
import java.io.IOException

class ChatMoodDetector : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnEnd: Button
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
        btnEnd = findViewById(R.id.btn_end)
        progressBar = findViewById(R.id.progressBar)
        btnEnd.isEnabled = false
        btnEnd.alpha = 0.5f
        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
        sendButton.isEnabled = false
        sendButton.alpha = 0.5f
        messageInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isNotEmpty = !s.isNullOrBlank()
                sendButton.isEnabled = isNotEmpty
                sendButton.alpha = if (isNotEmpty) 1.0f else 0.5f // 1.0 = visible, 0.5 = faded look
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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

        btnEnd.setOnClickListener {
            handleUserMessage("Analyze mood", true)
            btnEnd.isEnabled = false
            btnEnd.alpha = 0.5f

            btnBack.isEnabled = false
            btnBack.alpha = 0.5f
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
        val welcomeText: TextView = findViewById(R.id.tv_welcome)
        if (welcomeText.visibility == View.VISIBLE) {
            welcomeText.visibility = View.GONE
            btnEnd.isEnabled = true
            btnEnd.alpha = 1f
        }
        chatMessages.add(ChatMessage(message, isUser))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun handleUserMessage(message: String, isAnalyzingMood: Boolean) {
        progressBar.visibility = View.VISIBLE

        if (isAnalyzingMood) {
            addMessage(getString(R.string.analyzing_mood), isUser = false)
        } else {
            addMessage(message, isUser = true)
        }

        geminiAPI.generateReply(message, isAnalyzingMood, object : ResponseCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE

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
                            btnEnd.isEnabled = true
                            btnEnd.alpha = 1.0f
                            btnBack.isEnabled = true
                            btnBack.alpha = 1.0f
                        } else {
                            addMessage(response, isUser = false)
                            btnEnd.isEnabled = true
                            btnEnd.alpha = 1.0f
                            btnBack.isEnabled = true
                            btnBack.alpha = 1.0f
                        }
                    } else {
                        addMessage(response, isUser = false)
                    }
                }
            }

            override fun onFailure(exception: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnEnd.isEnabled = true
                    btnEnd.alpha = 1.0f

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