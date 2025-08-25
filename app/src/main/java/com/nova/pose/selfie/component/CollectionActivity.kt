package com.nova.pose.selfie.component

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nova.pose.selfie.R
import com.nova.pose.selfie.adapter.CollectionAdapter
import com.nova.pose.selfie.utils.CollectionUtils
import java.io.File
import java.io.IOException

class CollectionActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var collectionAdapter: CollectionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_collection)
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initViews()
        setupRecyclerView()
        loadImages()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        btnBack = findViewById(R.id.btnBack)
        
        btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        collectionAdapter = CollectionAdapter(
            onImageClick = { imageFile ->
                // Handle image click - could open in detail view
                val intent = Intent(this@CollectionActivity, ResultActivityActivity::class.java)
                intent.putExtra(ResultActivityActivity.EXTRA_IMAGE_PATH, imageFile.absolutePath)
                startActivity(intent)
            },
            onOptionsClick = { imageFile, anchorView ->
                showOptionsMenu(imageFile, anchorView)
            }
        )
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@CollectionActivity, 2)
            adapter = collectionAdapter
        }
    }
    
    private fun loadImages() {
        try {
            // Get all image files from the files directory
            val imageFiles = getImageFilesFromDirectory()
            collectionAdapter.updateImages(imageFiles)
            
            if (imageFiles.isEmpty()) {
                // Show empty state or placeholder
                Toast.makeText(this, "No images found in collection", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading images: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getImageFilesFromDirectory(): List<File> {
        return CollectionUtils.getImageFilesFromDirectory(this)
    }
    
    private fun showOptionsMenu(imageFile: File, anchorView: View) {
        showImageOptionsDialog(imageFile)
    }
    
    private fun showImageOptionsDialog(imageFile: File) {
        val dialog = Dialog(this, R.style.CustomDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_options)
        
        // Make dialog background transparent and set layout
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Find views
        val ivPreview = dialog.findViewById<ImageView>(R.id.ivPreview)
        val btnView = dialog.findViewById<LinearLayout>(R.id.btnView)
        val btnShare = dialog.findViewById<LinearLayout>(R.id.btnShare)
        val btnDelete = dialog.findViewById<LinearLayout>(R.id.btnDelete)
        
        // Load image into preview with correct orientation
        try {
            val bitmap = loadBitmapWithCorrectOrientation(imageFile)
            ivPreview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            ivPreview.setImageResource(R.drawable.placeholder_image)
        }
        
        // Set click listeners
        btnView.setOnClickListener {
            dialog.dismiss()
            viewImage(imageFile)
        }
        
        btnShare.setOnClickListener {
            dialog.dismiss()
            shareImage(imageFile)
        }
        
        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(imageFile)
        }
        
        // Add click animation effect
        addClickAnimation(btnView)
        addClickAnimation(btnShare)
        addClickAnimation(btnDelete)
        
        dialog.show()
    }
    
    private fun addClickAnimation(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                }
            }
            true
        }
    }
    
    private fun showDeleteConfirmationDialog(imageFile: File) {
        val dialog = Dialog(this, R.style.CustomDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_confirmation)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val btnCancel = dialog.findViewById<LinearLayout>(R.id.btnCancel)
        val btnConfirmDelete = dialog.findViewById<LinearLayout>(R.id.btnConfirmDelete)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnConfirmDelete.setOnClickListener {
            dialog.dismiss()
            deleteImage(imageFile)
        }
        
        addClickAnimation(btnCancel)
        addClickAnimation(btnConfirmDelete)
        
        dialog.show()
    }
    
    private fun shareImage(imageFile: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this@CollectionActivity,
                    "${packageName}.fileprovider",
                    imageFile
                ))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Image"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteImage(imageFile: File) {
        try {
            if (imageFile.delete()) {
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
                loadImages() // Reload the list
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun viewImage(imageFile: File) {
        // Could open in a detail view or full-screen viewer
        val intent = Intent(this@CollectionActivity, ResultActivityActivity::class.java)
        intent.putExtra(ResultActivityActivity.EXTRA_IMAGE_PATH, imageFile.absolutePath)
        startActivity(intent)
    }
    
    private fun loadBitmapWithCorrectOrientation(imageFile: File): Bitmap? {
        return try {
            // First, decode the image bounds to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)
            
            // Calculate sample size for efficient memory usage
            val sampleSize = calculateInSampleSize(options, 800, 600)
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            
            // Decode the bitmap
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
                ?: return null
            
            // Get the rotation angle from EXIF data
            val rotationAngle = getImageRotationAngle(imageFile.absolutePath)
            
            // Rotate the bitmap if necessary
            if (rotationAngle != 0) {
                rotateBitmap(bitmap, rotationAngle.toFloat())
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getImageRotationAngle(imagePath: String): Int {
        return try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }
    
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        return try {
            val matrix = Matrix().apply {
                postRotate(degrees)
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, 
                bitmap.width, bitmap.height, 
                matrix, true
            )
            
            // Recycle the original bitmap if it's different from the rotated one
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            
            rotatedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap // Return original bitmap if rotation fails
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    override fun onResume() {
        super.onResume()
        // Reload images when returning to the screen
        loadImages()
    }
}