package com.example.moodpredictorapp.MainActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.moodpredictorapp.ConfigureMenu.ConfigureMenu
import com.example.moodpredictorapp.R
import com.example.moodpredictorapp.SelectWayActivity.SelectWay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btn_configure = findViewById<Button>(R.id.btn_configure)
        btn_configure.setOnClickListener {
            val intent= Intent(this, ConfigureMenu::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        val btn_next = findViewById<Button>(R.id.btn_next)
        btn_next.setOnClickListener {
            val intent= Intent(this, SelectWay::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }
}