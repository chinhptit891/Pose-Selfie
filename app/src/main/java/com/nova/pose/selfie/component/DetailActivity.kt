package com.nova.pose.selfie.component

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.nova.pose.selfie.base.BaseActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nova.pose.selfie.R
import com.nova.pose.selfie.adapter.ImageAdapter
import com.nova.pose.selfie.model.DetalResponse
import com.nova.pose.selfie.model.ImagesItem

class DetailActivity : BaseActivity() {

    private lateinit var tvCategoryName: TextView
    private lateinit var rvImages: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var imgBackground: ImageView
    private lateinit var imageAdapter: ImageAdapter

    private lateinit var imagesItem: ImagesItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithInsets(R.layout.activity_detail, R.id.main)

        initViews()
        setupRecyclerView()
        setupBackButton()
        processIntent()
    }

    private fun initViews() {
        tvCategoryName = findViewById(R.id.tvCategoryName)
        rvImages = findViewById(R.id.rvImages)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        imgBackground = findViewById(R.id.imgBackground)
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter { image ->
            // Handle image click if needed
            Log.d("DetailActivity", "Image clicked: ${image.id}")
            imagesItem = image
            loadBackground(image.urlRectangle)
        }

        rvImages.apply {
            adapter = imageAdapter
        }
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun processIntent() {
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category"
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val detailResponse = intent.getParcelableExtra<DetalResponse>("DETAIL_RESPONSE")



        tvCategoryName.text = categoryName

        if (detailResponse != null) {
            displayImages(detailResponse.data.images)

            val imageItemDefault = if (detailResponse.data.images.isNotEmpty()) {
                detailResponse.data.images[0]
            } else {
                null
            }
            if (imageItemDefault != null) {
                loadBackground(imageItemDefault.urlRectangle)
                imagesItem = imageItemDefault
            } else {
                Log.d("DetailActivity", "No images available in the detail response")
            }
        } else {
            Log.e("DetailActivity", "No detail response received")
            showEmptyState()
        }
    }

    fun loadBackground(url: String) {
        Glide.with(this).load(url).into(imgBackground)
    }


    private fun displayImages(images: List<ImagesItem>) {
        if (images.isNotEmpty()) {
            imageAdapter.updateData(images)
            rvImages.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        } else {
            showEmptyState()
        }

        progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        rvImages.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
}