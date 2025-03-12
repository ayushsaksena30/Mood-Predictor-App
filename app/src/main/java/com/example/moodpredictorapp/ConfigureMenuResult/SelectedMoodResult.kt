package com.example.moodpredictorapp.ConfigureMenuResult

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.moodpredictorapp.PlayMusic.PlayMusic
import com.example.moodpredictorapp.R

class SelectedMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_mood_result)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val tvMood: TextView = findViewById(R.id.tv_mood)
        val btnNext: Button = findViewById(R.id.btn_next)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val mood = intent.getStringExtra("selected_item_name")

        if (!mood.isNullOrEmpty()) {
            tvMood.text = mood
        } else {
            tvMood.text = "No Mood Detected"
        }

        val categories = listOf("Bollywood", "Pop", "Jazz", "Rock", "Classical", "EDM")
        val images = listOf(R.drawable.bollywood, R.drawable.pop, R.drawable.jazz, R.drawable.rock_jpg, R.drawable.classical, R.drawable.edm)

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