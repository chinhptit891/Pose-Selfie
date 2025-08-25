package com.nova.pose.selfie.component

import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nova.pose.selfie.R
import java.io.File

class ResultActivityActivity : AppCompatActivity() {
    
    private lateinit var imageView: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: ImageButton
    private var imagePath: String? = null
    
    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result_activity)
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        initViews()
        
        // Get image path from intent
        imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        
        // Load and display image
        loadImage()
        
        // Setup button listeners
        setupListeners()
    }
    
    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
    }
    
    private fun loadImage() {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .into(imageView)
            } else {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } ?: run {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }
        
        // Delete button
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                deleteImage()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteImage() {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                try {
                    if (file.delete()) {
                        Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                        finish() // Tự động back về màn trước sau khi xóa
                    } else {
                        Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error deleting image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}