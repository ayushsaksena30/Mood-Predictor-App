package com.saksena.moodpredictorapp.SelectWayActivity.ImageMoodDetector

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
import com.saksena.moodpredictorapp.R
import com.saksena.moodpredictorapp.SelectWayResult.PredictedImageMoodResult
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
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var imageCapture: ImageCapture? = null
    private var currentBase64Image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_mood_detector)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        previewView = findViewById(R.id.cameraPreview)
        captureButton = findViewById(R.id.captureButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        btn_uploading = findViewById(R.id.btn_uploadimg)
        val btnBack: ImageButton = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }
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
            setButtonInteraction(false)
        }

        btn_uploading.setOnClickListener {
            setButtonInteraction(false)
            openGallery()
        }
    }

    private fun openGallery() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.get().unbindAll()

        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val selectedImageUri = data.data

                if (selectedImageUri != null) {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri))
                    val base64Image = encodeImageToBase64(bitmap)
                    sendImageToGemini(base64Image)
                } else {
                    setButtonInteraction(true)
                }
            } else {
                startCamera()
                setButtonInteraction(true)
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
                    setButtonInteraction(true)
                }
            })
    }

    private fun sendImageToGemini(base64Image: String?) {
        if (base64Image == null) {
            Toast.makeText(this, "Base64 Encoding Failed", Toast.LENGTH_LONG).show()
            setButtonInteraction(true)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.get().unbindAll()
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
                    setButtonInteraction(true)
                }
            }

            override fun onFailure(exception: IOException) {
                runOnUiThread {
                    if (isInternetUnavailable(applicationContext)) {
                        Toast.makeText(applicationContext, "Connect to the internet and try again", Toast.LENGTH_LONG).show()
                    } else if (exception.message?.contains("busy", ignoreCase = true) == true || exception.message?.contains("server", ignoreCase = true) == true) {
                        Toast.makeText(applicationContext, "Servers are busy right now, try again later", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "API Error: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                    setButtonInteraction(true)

                    startCamera()
                }
            }

            private fun isInternetUnavailable(context: Context): Boolean {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork ?: return true
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return true

                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> false
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> false
                    else -> true
                }
            }
        })
    }

    private fun setButtonInteraction(enable: Boolean) {
        captureButton.isEnabled = enable
        btn_uploading.isEnabled = enable
        switchCameraButton.isEnabled = enable

        captureButton.alpha = if (enable) 1f else 0.5f
        btn_uploading.alpha = if (enable) 1f else 0.5f
        switchCameraButton.alpha = if (enable) 1f else 0.5f
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String? {
        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 240, true)
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
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

    override fun onResume() {
        super.onResume()
        startCamera()
    }
}
