package com.example.moodpredictorapp.SelectWayActivity.ImageMoodDetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.moodpredictorapp.R
import com.example.moodpredictorapp.SelectWayResult.PredictedImageMoodResult
import java.io.ByteArrayOutputStream
import java.io.IOException
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
    private var currentBase64Image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_mood_detector)

        previewView = findViewById(R.id.cameraPreview)
        captureButton = findViewById(R.id.captureButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        btn_uploading = findViewById(R.id.btn_uploadimg)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        switchCameraButton.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }

        captureButton.setOnClickListener {
            takePhoto()
        }

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
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri))
                val base64Image = encodeImageToBase64(bitmap)
                sendImageToGemini(base64Image)
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

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
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
                    val base64Image = encodeImageToBase64(bitmap)
                    sendImageToGemini(base64Image)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(applicationContext, "Failed to capture photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendImageToGemini(base64Image: String?) {
        if (base64Image == null) {
            Toast.makeText(this, "Base64 Encoding Failed", Toast.LENGTH_LONG).show()
            return
        }

        currentBase64Image = base64Image

        val geminiAPI = GeminiImageAPI()
        geminiAPI.analyzeImage(base64Image, object : GeminiImageAPI.ResponseCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    val intent = Intent(this@ImageMoodDetector, PredictedImageMoodResult::class.java).apply {
                        putExtra("gemini_response", response)
                        putExtra("image_base64", currentBase64Image)
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_up, 0)
                }
            }

            override fun onFailure(exception: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Gemini API Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String? {
        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 240, true)
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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