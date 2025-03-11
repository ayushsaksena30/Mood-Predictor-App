package com.example.moodpredictorapp.SelectWayResult

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moodpredictorapp.R

class PredictedImageMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predicted_mood_result)

        val imageView: ImageView = findViewById(R.id.iv_imageshow)
        val moodTextView: TextView = findViewById(R.id.tv_mood)

        val imageBase64 = intent.getStringExtra("image_base64")
        val geminiResponse = intent.getStringExtra("gemini_response")

        if (!imageBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to display the image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image received from the previous activity", Toast.LENGTH_SHORT).show()
        }

        if (!geminiResponse.isNullOrEmpty()) {
            moodTextView.text = "Mood: $geminiResponse"
        } else {
            moodTextView.text = "Mood: Unable to determine mood."
        }
    }
}