package com.example.moodpredictorapp.SelectWayActivity.TakeATestMoodDetector

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moodpredictorapp.ConfigureMenuResult.SelectedMoodResult
import com.example.moodpredictorapp.R

class TakeATestMoodDetector : AppCompatActivity() {

    private lateinit var tvSelectMethod: TextView
    private lateinit var ivOne: ImageView
    private lateinit var ivTwo: ImageView
    private lateinit var ivThree: ImageView
    private lateinit var ivFour: ImageView
    private lateinit var progressBar: ProgressBar // Added ProgressBar

    private val answers = mutableListOf<Int>()
    private var questionIndex = 0

    private val questions = listOf(
        "Right now, which color reflects your overall energy level?",
        "Thinking about your recent interactions, which color captures your social mood?",
        "When you consider your thoughts and feelings today, which color resonates most with your inner state?",
        "If you were to choose a color to express your current level of stress or calmness, which would it be?",
        "Looking ahead to the rest of your day, which color best represents your outlook?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_take_atest_mood_detector)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSelectMethod = findViewById(R.id.tv_selectmethod)
        ivOne = findViewById(R.id.iv_one)
        ivTwo = findViewById(R.id.iv_two)
        ivThree = findViewById(R.id.iv_three)
        ivFour = findViewById(R.id.iv_four)
        progressBar = findViewById(R.id.progressBar)

        setupColorSelection()
    }

    private fun setupColorSelection() {
        tvSelectMethod.text = questions[questionIndex]
        progressBar.progress = questionIndex

        ivOne.setOnClickListener {
            answers.add(R.color.yellow)
            nextQuestion()
        }

        ivTwo.setOnClickListener {
            answers.add(R.color.blue)
            nextQuestion()
        }

        ivThree.setOnClickListener {
            answers.add(R.color.red)
            nextQuestion()
        }

        ivFour.setOnClickListener {
            answers.add(R.color.grey)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        questionIndex++
        if (questionIndex < questions.size) {
            tvSelectMethod.text = questions[questionIndex]
            progressBar.progress = questionIndex // Update progress
        } else {
            analyzeColorResponses()
        }
    }

    private fun previousQuestion() {
        questionIndex--
        if (questionIndex >= 0) {
            tvSelectMethod.text = questions[questionIndex]
            progressBar.progress = questionIndex // Update progress
        } else {
            finish()
        }
    }

    private fun analyzeColorResponses() {
        val colorCounts = mutableMapOf(
            R.color.yellow to 0,
            R.color.blue to 0,
            R.color.red to 0,
            R.color.grey to 0
        )

        for (color in answers) {
            colorCounts[color] = colorCounts[color]!! + 1
        }

        var dominantMood = ""
        val maxCount = colorCounts.values.maxOrNull()

        if (maxCount != null) {
            val dominantColors = colorCounts.filterValues { it == maxCount }.keys

            dominantMood = when {
                dominantColors.contains(R.color.yellow) && dominantColors.contains(R.color.blue) -> "Surprised"
                dominantColors.contains(R.color.yellow) && !dominantColors.contains(R.color.blue) && !dominantColors.contains(R.color.red) && !dominantColors.contains(R.color.grey) -> "Happy"
                dominantColors.contains(R.color.blue) && !dominantColors.contains(R.color.yellow) && !dominantColors.contains(R.color.red) && !dominantColors.contains(R.color.grey) -> "Neutral"
                dominantColors.contains(R.color.grey) && !dominantColors.contains(R.color.yellow) && !dominantColors.contains(R.color.blue) && !dominantColors.contains(R.color.red) -> "Sad"
                dominantColors.contains(R.color.red) && !dominantColors.contains(R.color.yellow) && !dominantColors.contains(R.color.blue) && !dominantColors.contains(R.color.grey) -> "Angry"
                else -> "Neutral"
            }
        } else {
            dominantMood = "Neutral"
        }

        val intent = Intent(this, SelectedMoodResult::class.java)
        intent.putExtra("selected_item_name", dominantMood)
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {
        if (questionIndex > 0) {
            previousQuestion()
        } else {
            super.onBackPressed()
        }
    }
}