package com.nova.pose.selfie.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.nova.pose.selfie.R
import com.nova.pose.selfie.base.BaseActivity
import com.nova.pose.selfie.model.ImagesItem
import com.nova.pose.selfie.model.cam.DetailCamResponse
import com.nova.pose.selfie.utils.ImageCacheUtils
import com.nova.pose.selfie.utils.TimeUtils
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class CamActivity : BaseActivity() {


    private lateinit var imgContour: ImageView
    private lateinit var cameraView: CameraView
    private lateinit var btnCapture: ImageView
    private lateinit var btnSwitchCamera: ImageView
    private lateinit var btnFlash: ImageView
    private lateinit var btnBack: ImageView

    private var imagesItem: ImagesItem? = null
    private var detailCamResponse: DetailCamResponse? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cam)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupCamera()
        processIntent()
    }

    private fun initViews() {

        imgContour = findViewById(R.id.imgContour)
        cameraView = findViewById(R.id.cameraView)
        btnCapture = findViewById(R.id.btnCapture)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnFlash = findViewById(R.id.btnFlash)
        btnBack = findViewById(R.id.btnBack)
        setupButtons()
    }

    private fun setupButtons() {
        btnCapture.setOnClickListener {
            capturePhoto()
        }

        btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
        btnBack.setOnClickListener {
            finish()
        }
        btnFlash.setOnClickListener {
            when (cameraView.flash) {
                com.otaliastudios.cameraview.controls.Flash.OFF -> {
                    cameraView.flash = com.otaliastudios.cameraview.controls.Flash.ON
                    btnFlash.setImageResource(R.drawable.ic_flash_on)
                }

                com.otaliastudios.cameraview.controls.Flash.ON -> {
                    cameraView.flash = com.otaliastudios.cameraview.controls.Flash.AUTO
                    btnFlash.setImageResource(R.drawable.ic_flash_auto)
                }

                com.otaliastudios.cameraview.controls.Flash.AUTO -> {
                    cameraView.flash = com.otaliastudios.cameraview.controls.Flash.OFF
                    btnFlash.setImageResource(R.drawable.ic_flash_off)
                }

                else -> {
                    cameraView.flash = com.otaliastudios.cameraview.controls.Flash.OFF
                    btnFlash.setImageResource(R.drawable.ic_flash_off)
                }
            }
        }
    }

    private fun setupCamera() {
        // Initialize camera view
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(object : com.otaliastudios.cameraview.CameraListener() {
            override fun onPictureTaken(result: com.otaliastudios.cameraview.PictureResult) {
                super.onPictureTaken(result)
                // Handle picture taken

                result.toFile(ImageCacheUtils.getOutputMediaFile(this@CamActivity)) { file ->


                    if (file != null) {
                        Log.d("CamActivity", "Photo saved to: ${file.absolutePath}")
                        val intent = Intent(this@CamActivity, ResultActivityActivity::class.java)
                        intent.putExtra(ResultActivityActivity.EXTRA_IMAGE_PATH, file.absolutePath)
                        startActivity(intent)
                    } else {
                        Log.e("CamActivity", "Failed to save photo")
                    }
                }
            }
        })
    }

    private fun processIntent() {
        imagesItem = intent.getParcelableExtra("IMAGE_ITEM")

        if (imagesItem != null) {
            Log.d("CamActivity", "Received ImagesItem: ${imagesItem!!.id}")
            loadCameraData(imagesItem!!.id)
        } else {
            Log.e("CamActivity", "No ImagesItem received")
            showError("No image data received")
        }
    }

    private fun loadCameraData(imageId: Int) {
        showLoading()

        // Run network call in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = makeApiCall(imageId)
                withContext(Dispatchers.Main) {
                    handleApiResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CamActivity", "Error loading camera data", e)
                    showError("Failed to load camera data: ${e.message}")
                }
            }
        }
    }

    private suspend fun makeApiCall(imageId: Int): String {
        return withContext(Dispatchers.IO) {
            val url = "https://www.photoideas.mobi/api/image/$imageId/"

            val request = Request.Builder().url(url).get().build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string() ?: ""
                } else {
                    throw IOException("API call failed with code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("CamActivity", "Network error", e)
                throw e
            }
        }
    }

    private fun handleApiResponse(responseBody: String) {
        try {
            if (responseBody.isNotEmpty()) {
                detailCamResponse = gson.fromJson(responseBody, DetailCamResponse::class.java)

                if (detailCamResponse?.success == true) {
                    showSuccess(detailCamResponse!!)
                    Log.d(
                        "CamActivity",
                        "Camera data loaded successfully: ${detailCamResponse!!.data.id}"
                    )

                    // Load and display appropriate contour based on time
                    loadContourImage()
                } else {
                    showError("API returned success: false")
                }
            } else {
                showError("Empty response from server")
            }
        } catch (e: Exception) {
            Log.e("CamActivity", "Error parsing response", e)
            showError("Failed to parse server response: ${e.message}")
        }
    }

    private fun loadContourImage() {
        detailCamResponse?.let { response ->
            val isMorning = TimeUtils.isMorning()
            val contourUrl = if (isMorning) {
                response.data.contourBlackUrlPng
            } else {
                response.data.contourWhiteUrlPng
            }

            displayContourImage(contourUrl, if (isMorning) "Morning" else "Evening")
            // Download and cache the contour image

        }
    }

    private fun displayContourImage(imagePath: String, timeDescription: String) {
        try {
            // Load image from cache using Glide
            Glide.with(this).load(imagePath).into(imgContour)

            // Show the contour image
            imgContour.visibility = View.VISIBLE


            Log.d("CamActivity", "Contour image displayed successfully: $imagePath")

        } catch (e: Exception) {
            Log.e("CamActivity", "Error displaying contour image", e)
            showError("Failed to display contour image: ${e.message}")
        }
    }

    private fun showLoading() {

        imgContour.visibility = View.GONE
    }

    private fun showSuccess(detailCamResponse: DetailCamResponse) {
        Glide.with(this).load(detailCamResponse.data.contourBlackUrlPng).into(imgContour)

    }

    private fun showError(message: String) {

        imgContour.visibility = View.GONE
    }

    private fun capturePhoto() {
        try {
            cameraView.takePicture()
            Log.d("CamActivity", "Photo capture initiated")
        } catch (e: Exception) {
            Log.e("CamActivity", "Error capturing photo", e)
            showError("Failed to capture photo: ${e.message}")
        }
    }

    private fun switchCamera() {
        try {
            when (cameraView.facing) {
                Facing.FRONT -> cameraView.facing = Facing.BACK
                Facing.BACK -> cameraView.facing = Facing.FRONT

            }
            Log.d("CamActivity", "Camera switched to: ${cameraView.facing}")
        } catch (e: Exception) {
            Log.e("CamActivity", "Error switching camera", e)
            showError("Failed to switch camera: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.open()
    }

    override fun onPause() {
        super.onPause()
        cameraView.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.destroy()
    }
}