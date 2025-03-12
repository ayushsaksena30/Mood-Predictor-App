package com.example.moodpredictorapp.SelectWayActivity.ChatMoodDetector

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class GeminiAPI {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val apiKey = "YOUR_KEY" // Replace with your actual API key
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private var conversationHistory = ""

    fun generateReply(userInput: String, isAnalyzingMood: Boolean, callback: ResponseCallback) {
        addToConversationHistory("User", userInput)

        val prompt = if (isAnalyzingMood) {
            buildPrompt(conversationHistory,userInput) + "\nPerform mood analysis based on the conversation above."
        } else {
            buildPrompt(conversationHistory,userInput)
        }

        val url = "$baseUrl?key=$apiKey"
        val requestBodyJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObject = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObject = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObject)
                    }
                    put("parts", partsArray)
                }
                put(contentObject)
            }
            put("contents", contentsArray)
        }

        val body = RequestBody.create("application/json".toMediaType(), requestBodyJson.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(IOException("Error: Unable to connect to API. Details: ${e.message}"))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Error: Failed to get response from API (HTTP ${response.code})"))
                        return
                    }

                    val responseBodyString = response.body?.string()
                    response.close()

                    if (!responseBodyString.isNullOrEmpty()) {
                        val reply = parseResponse(responseBodyString)
                        callback.onSuccess(reply)
                    } else {
                        callback.onFailure(IOException("Error: Empty response from API"))
                    }

                } catch (e: Exception) {
                    callback.onFailure(IOException("Exception in onResponse: ${e.message}"))
                }
            }
        })
    }

    private fun buildPrompt(conversationHistory: String, latestInput: String): String {
        val systemPrompt = """
    You are a helpful and empathetic AI assistant. You are integrated in a mood predicting based music suggesting app.Prioritize responding to the user's current input while using the conversation history as context for understanding. Provide a relevant and engaging response to the latest input.
    
    Previous Conversation (for context):
    $conversationHistory

    User's Current Input:
    $latestInput
    
    Note: 
     1.Keep the replies very concise, be friendly and always ask questions to the user that relate to knowing about user's mood. don't repeat greetings.
     2.Mood Analysis Trigger: Only perform mood analysis when the user explicitly signals the conversation is over or uses phrases like "analyze mood", "end conversation", "finish".
     3.End Conversation Reminder: When the user indicates they want to end the conversation, gently remind them to press the "Finish" button to finalize the mood analysis. Phrase this as a suggestion, not a command. For example, "To get your mood analysis, please press the 'Finish' button."
     4.Mood Analysis Format: When performing mood analysis, provide the result in the following format: "Mood: [Mood Category]". The mood categories are: Happy, Sad, Neutral, Angry, Surprise. That's it no other explanation is required.
     5.To do mood analysis if the user has explicitly mentioned that he wants a particular type of music then change the category accordingly irrespective of the mood you analysed.
     6.Consistent Tone: Maintain a friendly, supportive, and empathetic tone throughout the conversation.
     7.Don't repeat your questions. If you see the user is not answering certain questions, avoid asking similar questions.
    """.trimIndent()

        return systemPrompt
    }


    private fun parseResponse(response: String?): String {
        return try {
            if (!response.isNullOrEmpty()) {
                val jsonObject = JSONObject(response)

                if (jsonObject.has("candidates")) {
                    val candidatesArray = jsonObject.getJSONArray("candidates")
                    if (candidatesArray.length() > 0) {
                        val candidate = candidatesArray.getJSONObject(0)
                        if (candidate.has("content")) {
                            val content = candidate.getJSONObject("content")
                            if (content.has("parts")) {
                                val partsArray = content.getJSONArray("parts")
                                if (partsArray.length() > 0) {
                                    val part = partsArray.getJSONObject(0)
                                    if (part.has("text")) {
                                        return part.getString("text").trim()
                                    }
                                }
                            }
                        }
                    }
                } else if (jsonObject.has("parts")) {
                    val partsArray = jsonObject.getJSONArray("parts")
                    if (partsArray.length() > 0) {
                        val part = partsArray.getJSONObject(0)
                        if (part.has("text")) {
                            return part.getString("text").trim()
                        }
                    }
                } else if (jsonObject.has("content")) {
                    return jsonObject.getString("content").trim()
                }

                return "Error: Invalid response format"
            }
            return "Error: Empty response"
        } catch (e: Exception) {
            return "Error: Parsing error"
        }
    }

    fun extractMood(text: String): String? {
        val pattern = Pattern.compile("Mood:\\s*(Happy|Sad|Neutral|Angry|Surprise)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.capitalize()
        } else {
            "Unknown"
        }
    }

    private fun addToConversationHistory(role: String, message: String) {
        val newLine = "$role: $message\n"
        conversationHistory += newLine

        val historyLines = conversationHistory.lines()
        if (historyLines.size > 20) {
            conversationHistory = historyLines.takeLast(20).joinToString("\n")
        }
    }

    fun clearConversationHistory() {
        conversationHistory = ""
    }

    interface ResponseCallback {
        fun onSuccess(response: String)
        fun onFailure(exception: IOException)
    }
}