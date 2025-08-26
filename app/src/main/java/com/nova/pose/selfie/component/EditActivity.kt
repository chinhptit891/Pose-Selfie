package com.nova.pose.selfie.component

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.MotionEvent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.graphics.Matrix
import android.view.ScaleGestureDetector
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.nova.pose.selfie.R
import com.nova.pose.selfie.adapter.BackgroundAdapter
import com.nova.pose.selfie.utils.ImageCacheUtils
import java.io.File
import java.io.FileOutputStream

class EditActivity : AppCompatActivity() {

    private lateinit var imgBackground: ImageView
    private lateinit var imgForeground: ImageView
    private lateinit var seekOpacity: SeekBar
    private lateinit var rvBackgrounds: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var btnSave: TextView

    private var originalBitmap: Bitmap? = null
    private var segmentedBitmap: Bitmap? = null

    // Gesture/transform state for imgForeground
    private lateinit var foregroundMatrix: Matrix
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var isDragging: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupOpacity()
        setupBackgrounds()
        setupButtons()
        loadPickedImage()
    }

    private fun bindViews() {
        imgBackground = findViewById(R.id.imgBackground)
        imgForeground = findViewById(R.id.imgForeground)
        seekOpacity = findViewById(R.id.seekOpacity)
        rvBackgrounds = findViewById(R.id.rvBackgrounds)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        // Initialize matrix-based transform for foreground image
        imgForeground.scaleType = ImageView.ScaleType.MATRIX
        foregroundMatrix = Matrix()
        imgForeground.imageMatrix = foregroundMatrix
        setupGestures()
    }

    private fun setupOpacity() {
        seekOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imgForeground.alpha = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupBackgrounds() {
        rvBackgrounds.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val assetDir = "background"
        val paths = assets.list(assetDir)?.map { "$assetDir/$it" }?.sorted() ?: emptyList()
        rvBackgrounds.adapter = BackgroundAdapter(this, paths) { bitmap ->
            imgBackground.setImageBitmap(bitmap)
        }
    }

    private fun setupButtons() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveAndGoResult() }
    }

    private fun loadPickedImage() {
        val uriStr = intent.getStringExtra("IMAGE_URI") ?: return
        val uri = Uri.parse(uriStr)
        contentResolver.openInputStream(uri)?.use { stream ->
            originalBitmap = BitmapFactory.decodeStream(stream)
            imgForeground.setImageBitmap(originalBitmap)
            removeBackgroundWithMlkit(originalBitmap!!)
        }
    }

    private fun setupGestures() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                foregroundMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                imgForeground.imageMatrix = foregroundMatrix
                return true
            }
        })

        imgForeground.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    isDragging = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!scaleGestureDetector.isInProgress && isDragging) {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        foregroundMatrix.postTranslate(dx, dy)
                        imgForeground.imageMatrix = foregroundMatrix
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                }
            }
            true
        }
    }

    private fun removeBackgroundWithMlkit(bitmap: Bitmap) {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
        val segmenter = Segmentation.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        segmenter.process(image)
            .addOnSuccessListener { mask: SegmentationMask ->
                segmentedBitmap = applyMask(bitmap, mask)
                imgForeground.setImageBitmap(segmentedBitmap)
            }
            .addOnFailureListener {
                imgForeground.setImageBitmap(bitmap)
            }
    }

    private fun applyMask(source: Bitmap, mask: SegmentationMask): Bitmap {
        val width = mask.width
        val height = mask.height
        val buffer = mask.buffer
        val maskPixels = FloatArray(width * height)
        buffer.rewind()
        buffer.asFloatBuffer().get(maskPixels)

        val scaled = Bitmap.createScaledBitmap(source, width, height, true)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val alpha = (maskPixels[i] * 255).toInt().coerceIn(0, 255)
            pixels[i] = (alpha shl 24) or (pixels[i] and 0x00FFFFFF)
        }
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return Bitmap.createScaledBitmap(output, source.width, source.height, true)
    }

    private fun saveAndGoResult() {
        val bgBitmap = imgBackground.drawable?.toBitmap(imgBackground.width, imgBackground.height)
        val srcForeground = (segmentedBitmap ?: originalBitmap)
        if (srcForeground == null) return

        val result = if (bgBitmap != null) composeWithMatrix(bgBitmap, srcForeground, imgForeground.imageMatrix)
                     else composeOnTransparent(srcForeground, imgForeground.width, imgForeground.height, imgForeground.imageMatrix)
        val file = ImageCacheUtils.getOutputMediaFile(this)
        FileOutputStream(file).use { out ->
            result.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val intent = Intent(this, ResultActivityActivity::class.java)
        intent.putExtra(ResultActivityActivity.EXTRA_IMAGE_PATH, file.absolutePath)
        startActivity(intent)
    }

    private fun composeBitmaps(background: Bitmap, foreground: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(background, 0f, 0f, null)
        val left = (background.width - foreground.width) / 2f
        val top = (background.height - foreground.height) / 2f
        canvas.drawBitmap(foreground, left, top, null)
        return result
    }

    private fun composeWithMatrix(background: Bitmap, foreground: Bitmap, matrix: Matrix): Bitmap {
        val result = Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(foreground, matrix, null)
        return result
    }

    private fun composeOnTransparent(foreground: Bitmap, width: Int, height: Int, matrix: Matrix): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(foreground, matrix, null)
        return result
    }
}