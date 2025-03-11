package com.example.moodpredictorapp.ConfigureMenuResult

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moodpredictorapp.R

class SelectedMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_mood_result)

        // Get the TextView where the mood will be displayed
        val tvMood: TextView = findViewById(R.id.tv_mood)

        // Retrieve the mood passed through the Intent
        val mood = intent.getStringExtra("selected_item_name")

        // Set the TextView's text to the mood
        if (!mood.isNullOrEmpty()) {
            tvMood.text = mood
        } else {
            tvMood.text = "No Mood Detected"
        }
    }
}
