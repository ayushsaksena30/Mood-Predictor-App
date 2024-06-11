package com.example.moodpredictorapp

import MyAdapter
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConfigureMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configure_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.rv_menu)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val itemList = listOf(
            Item("Happy", R.drawable.happy_png),
            Item("Sad", R.drawable.sad_png),
            Item("Surprise", R.drawable.surprise_png),
            Item("Angry", R.drawable.angry_png),
            Item("Neutral", R.drawable.relax_png)
        )

        val adapter = MyAdapter(itemList){ view, position ->
            // Perform the click animation
            val animation = AnimationUtils.loadAnimation(this, R.anim.item_click_animation)
            view.startAnimation(animation)

            // Scroll to the center
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, recyclerView.height / 2 - view.height / 2)
        }
        recyclerView.adapter = adapter

    }
}