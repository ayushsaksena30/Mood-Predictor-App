package com.example.moodpredictorapp

import MyAdapter
import android.os.Bundle
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
            Item("Item 1", R.drawable.ic_launcher_background),
            Item("Item 2", R.drawable.ic_launcher_background),
            Item("Item 3", R.drawable.ic_launcher_background)
        )

        val adapter = MyAdapter(itemList)
        recyclerView.adapter = adapter

    }
}