package com.example.moodpredictorapp.SelectWayResult

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moodpredictorapp.R

class PredictedImageMoodResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predicted_mood_result)

        val imageView: ImageView = findViewById(R.id.iv_imageshow)

        val photoUriString = intent.getStringExtra("photo_uri")

        if (!photoUriString.isNullOrEmpty()) {
            try {
                // Decode the image URI and display it in the ImageView
                val photoUri = Uri.parse(photoUriString)
                val inputStream = contentResolver.openInputStream(photoUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to display the image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image received from the previous activity", Toast.LENGTH_SHORT).show()
        }
    }
}
