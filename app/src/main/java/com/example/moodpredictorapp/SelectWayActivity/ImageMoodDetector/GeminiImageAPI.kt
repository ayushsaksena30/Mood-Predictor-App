package com.example.moodpredictorapp.SelectWayActivity.ImageMoodDetector

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.util.Log

class GeminiImageAPI {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val apiKey = "YOUR_API_KEY" // Replace with your actual API key
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    fun analyzeImage(imageData: String, callback: ResponseCallback) {
        val prompt = "Classify the emotion of the person in the image as one of the following: Happy, Sad, Neutral, Angry, or Surprised. Respond with only the emotion category."

        val url = "$baseUrl?key=$apiKey"
        val requestBodyJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObject = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObjectText = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObjectText)
                        val partObjectImage = JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", imageData)
                            })
                        }
                        put(partObjectImage)
                    }
                    put("parts", partsArray)
                }
                put(contentObject)
            }
            put("contents", contentsArray)
        }

        val body = RequestBody.create("application/json".toMediaType(), requestBodyJson.toString())
        Log.d("GeminiAPI", "Request Body: ${requestBodyJson.toString()}")
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiAPI", "API request failed: ${e.message}")
                callback.onFailure(IOException("Error: Unable to connect to Gemini API. Details: ${e.message}"))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "No error body"
                        Log.e("GeminiAPI", "HTTP ${response.code} - Error body: $errorBody")
                        callback.onFailure(IOException("Error: Failed to get response from Gemini API (HTTP ${response.code}). Error body: $errorBody"))
                        return
                    }

                    val responseBodyString = response.body?.string()
                    response.close()

                    if (!responseBodyString.isNullOrEmpty()) {
                        val reply = parseResponse(responseBodyString)
                        callback.onSuccess(reply)
                    } else {
                        Log.e("GeminiAPI", "Empty response body")
                        callback.onFailure(IOException("Error: Empty response from Gemini API"))
                    }

                } catch (e: Exception) {
                    Log.e("GeminiAPI", "Exception in onResponse: ${e.message}")
                    callback.onFailure(IOException("Exception in onResponse: ${e.message}"))
                }
            }
        })
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
                }
                return "Error: Invalid response format"
            }
            return "Error: Empty response"
        } catch (e: Exception) {
            return "Error: Parsing error"
        }
    }

    interface ResponseCallback {
        fun onSuccess(response: String)
        fun onFailure(exception: IOException)
    }
}