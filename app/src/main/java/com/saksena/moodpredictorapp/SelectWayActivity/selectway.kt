package com.saksena.moodpredictorapp.SelectWayActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.saksena.moodpredictorapp.R
import com.saksena.moodpredictorapp.SelectWayActivity.ChatMoodDetector.ChatMoodDetector
import com.saksena.moodpredictorapp.SelectWayActivity.ImageMoodDetector.ImageMoodDetector
import com.saksena.moodpredictorapp.SelectWayActivity.TakeATestMoodDetector.TakeATestMoodDetector

class SelectWay : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectway)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val chatTextView: TextView = findViewById(R.id.tv_chat)
        val imageTextView: TextView = findViewById(R.id.tv_image)
        val takeATestTextView: TextView = findViewById(R.id.tv_takeatest)
        val btnBack: ImageButton = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }
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
