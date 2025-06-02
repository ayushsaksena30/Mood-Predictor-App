package com.saksena.moodpredictorapp.PlayMusic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.saksena.moodpredictorapp.MainActivity.MainActivity
import com.saksena.moodpredictorapp.R

class PlayMusic : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val moodTextView = findViewById<TextView>(R.id.moodTextView)
        val subcatMood = findViewById<TextView>(R.id.subcatMood)
        val appleMusicButton = findViewById<AppCompatButton>(R.id.appleMusicButton)
        val spotifyButton = findViewById<AppCompatButton>(R.id.spotifyButton)
        val youtubeButton = findViewById<AppCompatButton>(R.id.youtubeButton)
        val homebutton = findViewById<AppCompatButton>(R.id.homebutton)

        var mood = intent.getStringExtra("mood")
        var selectedCategory = intent.getStringExtra("selected_category")
        if(selectedCategory==null) selectedCategory=""
        moodTextView.text = "Your Mood: $mood"
        subcatMood.text = "Subcategory: $selectedCategory"

        if(mood=="Neutral") mood=""

        appleMusicButton.setOnClickListener {
            launchAppleMusic(mood+" "+selectedCategory)
        }

        spotifyButton.setOnClickListener {
            launchSpotify(mood+" "+selectedCategory)
        }

        youtubeButton.setOnClickListener {
            launchYouTubeMusic(mood+" "+selectedCategory)
        }

        homebutton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }

    private fun launchAppleMusic(mood: String?) {
        val searchTerm = Uri.encode("$mood music playlist")
        val appleMusicUri = Uri.parse("https://music.apple.com/search?term=$searchTerm&app=music")

        val appleMusicIntent = Intent(Intent.ACTION_VIEW, appleMusicUri)
        startActivity(appleMusicIntent)
    }


    private fun launchSpotify(mood: String?) {
        val spotifyMood = when (mood) {
            "Happy" -> "happy"
            "Sad" -> "sad"
            "Surprise" -> "energetic surprise"
            "Angry" -> "angry aggressive"
            "Neutral" -> "chill calm"
            else -> "pop"
        }
        val spotifyUri = Uri.parse("https://open.spotify.com/search/results/$mood")

        val spotifyIntent = Intent(Intent.ACTION_VIEW, spotifyUri)
        startActivity(spotifyIntent)
    }

    private fun launchYouTubeMusic(mood: String?) {
        val searchTerm = Uri.encode("$mood music playlist")
        val youtubeMusicUri = Uri.parse("https://music.youtube.com/search?q=$searchTerm")

        val youtubeMusicIntent = Intent(Intent.ACTION_VIEW, youtubeMusicUri)

        startActivity(youtubeMusicIntent)
    }
}