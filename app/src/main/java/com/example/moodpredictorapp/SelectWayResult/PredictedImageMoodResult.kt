package com.example.moodpredictorapp.SelectWayResult

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager2.widget.ViewPager2
import com.example.moodpredictorapp.ConfigureMenuResult.SwipeAdapter
import com.example.moodpredictorapp.PlayMusic.PlayMusic
import com.example.moodpredictorapp.R

class PredictedImageMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predicted_mood_result)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val imageView: ImageView = findViewById(R.id.iv_imageshow)
        val moodTextView: TextView = findViewById(R.id.tv_mood)
        val btnNext: AppCompatButton = findViewById(R.id.btn_next)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

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

        var mood: String? = null

        if (!geminiResponse.isNullOrEmpty()) {
            mood = geminiResponse
            moodTextView.text = "$geminiResponse"
        } else {
            moodTextView.text = "Unable to determine mood."
        }

        val categories = listOf("Bollywood", "Pop", "Jazz", "Rock", "Classical", "EDM")
        val images = listOf(R.drawable.bollywood, R.drawable.pop, R.drawable.jazz, R.drawable.rock, R.drawable.classical, R.drawable.edm)

        val swipeAdapter = SwipeAdapter(categories, images)
        viewPager.adapter = swipeAdapter

        btnNext.setOnClickListener {
            val selectedPosition = viewPager.currentItem
            val selectedCategory = categories[selectedPosition]

            val intent = Intent(this, PlayMusic::class.java)
            intent.putExtra("mood", mood)
            intent.putExtra("selected_category", selectedCategory)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }
}