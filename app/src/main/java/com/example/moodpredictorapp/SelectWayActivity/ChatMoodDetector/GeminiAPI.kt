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

    private val apiKey = "AIzaSyC9LJeWpj9BJ6s-GM_YdWxv0Rcb3OUK_ZU" // Replace with your actual API key
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private var conversationHistory = "" // Stores conversation history

    fun generateReply(userInput: String, isAnalyzingMood: Boolean, callback: ResponseCallback) {
        addToConversationHistory("User", userInput)

        val prompt = if (isAnalyzingMood) {
            buildPrompt(conversationHistory) + "\nPerform mood analysis based on the conversation above."
        } else {
            buildPrompt(conversationHistory)
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
                callback.onFailure(IOException("Error: Unable to connect to Gemini API. Details: ${e.message}"))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Error: Failed to get response from Gemini API (HTTP ${response.code})"))
                        return
                    }

                    val responseBodyString = response.body?.string()
                    response.close()

                    if (!responseBodyString.isNullOrEmpty()) {
                        val reply = parseResponse(responseBodyString)
                        callback.onSuccess(reply)
                    } else {
                        callback.onFailure(IOException("Error: Empty response from Gemini API"))
                    }

                } catch (e: Exception) {
                    callback.onFailure(IOException("Exception in onResponse: ${e.message}"))
                }
            }
        })
    }

    private fun buildPrompt(conversationHistory: String): String {
        val systemPrompt = """
        You are a conversational AI assistant. Your goal is to understand the user's mood through natural conversation. 
        Do not ask the same question repeatedly. Vary your responses and engage in a realistic conversation.
        Only perform mood analysis when the conversation ends or the user explicitly signals the conversation is over.
        When the user wishes to end the conversation remind the user to press Finish button to end it.
        Provide the mood analysis in this format: Mood: [Mood Category]. The following are mood categories - Happy, Sad, Neutral, Angry, Surprise
    """.trimIndent()

        return "$systemPrompt\n$conversationHistory"
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
            "Unknown" // Default value
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