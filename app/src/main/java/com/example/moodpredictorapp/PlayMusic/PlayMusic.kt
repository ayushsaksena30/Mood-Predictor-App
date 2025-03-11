package com.example.moodpredictorapp.PlayMusic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.moodpredictorapp.R

class PlayMusic : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)

        val moodTextView = findViewById<TextView>(R.id.moodTextView)
        val subcatMood = findViewById<TextView>(R.id.subcatMood)
        val appleMusicButton = findViewById<AppCompatButton>(R.id.appleMusicButton)
        val spotifyButton = findViewById<AppCompatButton>(R.id.spotifyButton)
        val youtubeButton = findViewById<AppCompatButton>(R.id.youtubeButton)

        val mood = intent.getStringExtra("mood")
        val selectedCategory = intent.getStringExtra("selected_category")
        moodTextView.text = "Your Mood: $mood"
        subcatMood.text = "Subcategory: $selectedCategory"

        appleMusicButton.setOnClickListener {
            launchAppleMusic(mood+" "+selectedCategory)
        }

        spotifyButton.setOnClickListener {
            launchSpotify(mood+" "+selectedCategory)
        }

        youtubeButton.setOnClickListener {
            launchYouTubeMusic(mood+" "+selectedCategory)
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