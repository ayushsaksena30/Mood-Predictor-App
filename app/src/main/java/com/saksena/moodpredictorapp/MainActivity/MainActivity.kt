package com.saksena.moodpredictorapp.MainActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.saksena.moodpredictorapp.ConfigureMenu.ConfigureMenu
import com.saksena.moodpredictorapp.R
import com.saksena.moodpredictorapp.SelectWayActivity.SelectWay

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var permissionGrantedToastShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        checkAndRequestCameraPermission()

        val btn_configure = findViewById<Button>(R.id.btn_configure)
        btn_configure.setOnClickListener {
            val intent = Intent(this, ConfigureMenu::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        val btn_next = findViewById<Button>(R.id.btn_next)
        btn_next.setOnClickListener {
            val intent = Intent(this, SelectWay::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!permissionGrantedToastShown) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                    permissionGrantedToastShown = true;
                }
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                permissionGrantedToastShown = false;
            }
        }
    }
}