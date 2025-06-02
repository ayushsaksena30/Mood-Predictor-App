package com.saksena.moodpredictorapp.ConfigureMenu

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saksena.moodpredictorapp.R
import com.saksena.moodpredictorapp.ConfigureMenuResult.SelectedMoodResult

class ConfigureMenu : AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.slide_down)
    }

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

        val adapter = MyAdapter(itemList) { itemName ->
            val intent = Intent(this@ConfigureMenu, SelectedMoodResult::class.java).apply {
                putExtra("selected_item_name", itemName)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
        recyclerView.adapter = adapter
    }
}