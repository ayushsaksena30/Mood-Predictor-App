package com.example.moodpredictorapp.SelectWayActivity.ImageMoodDetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.moodpredictorapp.SelectWayResult.PredictedImageMoodResult
import com.example.moodpredictorapp.R
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageMoodDetector : AppCompatActivity() {

    private val GALLERY_REQUEST_CODE = 1001
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var btn_uploading: Button
    private lateinit var switchCameraButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_mood_detector)

        previewView = findViewById(R.id.cameraPreview)
        captureButton = findViewById(R.id.captureButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        btn_uploading = findViewById(R.id.btn_uploadimg)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Start the camera
        startCamera()

        // Set click listener for switching cameras
        switchCameraButton.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }

        // Set click listener for the capture button
        captureButton.setOnClickListener {
            takePhoto()
        }

        // Set click listener for the upload button
        btn_uploading.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                // Pass the URI to the next activity
                val intent = Intent(this, PredictedImageMoodResult::class.java).apply {
                    putExtra("photo_uri", selectedImageUri.toString())
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Configure ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()

                // Bind use cases to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to initialize camera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    // Save the Bitmap to a temporary file
                    val uri = saveBitmapToCache(bitmap)

                    if (uri != null) {
                        // Pass the URI to the next activity
                        val intent = Intent(this@ImageMoodDetector, PredictedImageMoodResult::class.java).apply {
                            putExtra("photo_uri", uri.toString())
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(applicationContext, "Failed to capture photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cacheDir = File(cacheDir, "images")
            cacheDir.mkdirs() // Create the directory if it doesn't exist
            val file = File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
