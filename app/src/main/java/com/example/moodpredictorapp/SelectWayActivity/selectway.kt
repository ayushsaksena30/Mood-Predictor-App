package com.example.moodpredictorapp.SelectWayActivity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moodpredictorapp.R
import com.example.moodpredictorapp.SelectWayActivity.ChatMoodDetector.ChatMoodDetector
import com.example.moodpredictorapp.SelectWayActivity.ImageMoodDetector.ImageMoodDetector
import com.example.moodpredictorapp.SelectWayActivity.TakeATestMoodDetector.TakeATestMoodDetector

class SelectWay : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectway)

        val chatTextView: TextView = findViewById(R.id.tv_chat)
        val imageTextView: TextView = findViewById(R.id.tv_image)
        val takeATestTextView: TextView = findViewById(R.id.tv_takeatest)

        chatTextView.setOnClickListener {
            val intent = Intent(this, ChatMoodDetector::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        imageTextView.setOnClickListener {
            val intent = Intent(this, ImageMoodDetector::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        takeATestTextView.setOnClickListener {
            val intent = Intent(this, TakeATestMoodDetector::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }
}
