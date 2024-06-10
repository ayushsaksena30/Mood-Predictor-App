package com.example.moodpredictorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btn_configure = findViewById<Button>(R.id.btn_configure)
        btn_configure.setOnClickListener {
            val intent= Intent(this, ConfigureMenu::class.java)
            startActivity(intent)
        }
    }
}