package com.saksena.moodpredictorapp.SelectWayResult

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.saksena.moodpredictorapp.ConfigureMenuResult.SwipeAdapter
import com.saksena.moodpredictorapp.PlayMusic.PlayMusic
import com.saksena.moodpredictorapp.R

class PredictedImageMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predicted_mood_result)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val imageView: ImageView = findViewById(R.id.iv_imageshow)
        val moodTextView: TextView = findViewById(R.id.tv_mood)
        val btnNext: AppCompatButton = findViewById(R.id.btn_next)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val btnBack: ImageButton = findViewById(R.id.btn_back)

        val btnReport: Button = findViewById(R.id.btnReport)
        btnReport.setOnClickListener {
            showReportDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }
        val imageBase64 = intent.getStringExtra("image_base64")
        val geminiResponse = intent.getStringExtra("gemini_response")

        if (!imageBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to display the image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image received from the previous activity", Toast.LENGTH_SHORT).show()
        }

        var mood: String? = null

        if (!geminiResponse.isNullOrEmpty()) {
            mood = geminiResponse
            moodTextView.text = "$geminiResponse"
        } else {
            moodTextView.text = "Unable to determine mood."
        }

        val categories = listOf("Bollywood", "Pop", "Jazz", "Rock", "Classical", "EDM")
        val images = listOf(R.drawable.bollywood, R.drawable.pop, R.drawable.jazz, R.drawable.rock, R.drawable.classical, R.drawable.edm)

        val swipeAdapter = SwipeAdapter(categories, images)
        viewPager.adapter = swipeAdapter

        btnNext.setOnClickListener {
            val selectedPosition = viewPager.currentItem
            val selectedCategory = categories[selectedPosition]

            val intent = Intent(this, PlayMusic::class.java)
            intent.putExtra("mood", mood)
            intent.putExtra("selected_category", selectedCategory)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        viewPager.post {
            simulateViewPagerHint(viewPager)
        }
    }

    private fun simulateViewPagerHint(viewPager: ViewPager2) {
        val delayMillis = 1000L
        val originalItem = viewPager.currentItem
        val itemCount = viewPager.adapter?.itemCount ?: return

        viewPager.postDelayed({
            viewPager.setCurrentItem((originalItem + 1) % itemCount, true)

            viewPager.postDelayed({
                viewPager.setCurrentItem(originalItem, true)
            }, delayMillis)
        }, delayMillis)
    }

    private fun Context.showReportDialog() {
        val reportOptions = arrayOf(
            "Report Issue",
            "Flag Response",
            "Report Inappropriate Content",
            "Report Bug"
        )

        var selectedOption = reportOptions[0]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select a reason")

        builder.setSingleChoiceItems(reportOptions, 0) { _, which ->
            selectedOption = reportOptions[which]
        }

        builder.setPositiveButton("Submit") { dialog, _ ->
            val subject = "Hey Dev! I want to $selectedOption in your App."
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf("saksenaaayush5@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "Please describe the issue in more detail here...")
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email..."))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}