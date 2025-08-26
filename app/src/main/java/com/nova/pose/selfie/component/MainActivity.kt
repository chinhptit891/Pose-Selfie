package com.nova.pose.selfie.component

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.nova.pose.selfie.base.BaseActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nova.pose.selfie.R
import com.nova.pose.selfie.adapter.CategoryAdapter
import com.nova.pose.selfie.api.ApiClient
import com.nova.pose.selfie.model.DataItem
import com.nova.pose.selfie.model.DetalResponse
import com.nova.pose.selfie.model.RelatedImagesRequest
import com.nova.pose.selfie.utils.JsonUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class MainActivity : BaseActivity() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var btnAll: Button
    private lateinit var btnPopular: Button
    private lateinit var btnNew: Button
    private lateinit var btnCollection: LinearLayout
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var btnEditPhoto: LinearLayout
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var allCategories: List<DataItem> = emptyList()
    private var currentMode: String = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithInsets(R.layout.activity_main, R.id.main)

        initViews()
        initPickImage()
        setupRecyclerView()
        loadCategoriesFromAssets()
        setupTabButtons()
    }

    private fun initViews() {
        rvCategories = findViewById(R.id.rvCategories)
        btnAll = findViewById(R.id.btnAll)
        btnPopular = findViewById(R.id.btnPopular)
        btnNew = findViewById(R.id.btnNew)
        btnCollection = findViewById(R.id.btn_collection)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            // Handle category click
            Log.d("MainActivity", "Category clicked: ${category.name}")
            fetchRelatedImages(category)
        }

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {

                return when (position) {
                    0, 3 -> 2  // Chỉ vị trí 0 và 3 chiếm full width
                    else -> 1  // Các vị trí còn lại chiếm half width
                }
            }
        }

        rvCategories.apply {
            this.layoutManager = layoutManager
            adapter = categoryAdapter
        }
    }

    private fun initPickImage() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("IMAGE_URI", uri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategoriesFromAssets() {
        allCategories = JsonUtils.loadCategoriesFromAssets(this)
        Log.d("MainActivity", "Loaded ${allCategories.size} categories")

        if (allCategories.isNotEmpty()) {
            // Set initial mode to all
            setAllMode()
        } else {
            Log.e("MainActivity", "No categories loaded")
        }
    }

    private fun setupTabButtons() {
        btnAll.setOnClickListener {
            setAllMode()
        }

        btnPopular.setOnClickListener {
            setPopularMode()
        }

        btnNew.setOnClickListener {
            setNewMode()
        }

        btnCollection.setOnClickListener {
            val intent = Intent(this, CollectionActivity::class.java)
            startActivity(intent)
        }

        btnEditPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun setAllMode() {
        currentMode = "all"
        updateTabStates()

        // For all mode, show categories in original order
        categoryAdapter.updateData(allCategories)

        Log.d("MainActivity", "Switched to All mode")
    }

    private fun setPopularMode() {
        currentMode = "popular"
        updateTabStates()

        // For popular mode, sort by ideas count (most popular first)
        val sortedCategories = allCategories.sortedByDescending { it.ideas }
        categoryAdapter.updateData(sortedCategories)

        Log.d("MainActivity", "Switched to Popular mode")
    }

    private fun setNewMode() {
        currentMode = "new"
        updateTabStates()

        // For new mode, shuffle the list to show different order
        val shuffledCategories = allCategories.shuffled()
        categoryAdapter.updateData(shuffledCategories)

        Log.d("MainActivity", "Switched to New mode")
    }

    private fun updateTabStates() {
        // Reset all buttons to unselected state
        btnAll.isSelected = false
        btnPopular.isSelected = false
        btnNew.isSelected = false

        // Set the current button as selected
        when (currentMode) {
            "all" -> btnAll.isSelected = true
            "popular" -> btnPopular.isSelected = true
            "new" -> btnNew.isSelected = true
        }
    }

    private fun fetchRelatedImages(category: DataItem) {
        Log.d("MainActivity", "Fetching related images for category: ${category.id}")

        // Show loading indicator if needed

        // Create request body with category ID in an array as shown in the example
        val request = RelatedImagesRequest(categoryIds = listOf(category.id))

        ApiClient.apiService.getRelatedImages(request).enqueue(object : Callback<DetalResponse> {
                override fun onResponse(
                    call: Call<DetalResponse>, response: Response<DetalResponse>
                ) {
                    if (response.isSuccessful) {
                        val detailResponse = response.body()
                        if (detailResponse != null && detailResponse.success) {
                            // Navigate to detail activity with the response data
                            navigateToDetailActivity(category, detailResponse)
                        } else {
                            // Handle unsuccessful response
                            Toast.makeText(
                                this@MainActivity, "Failed to load images", Toast.LENGTH_SHORT
                            ).show()
                            Log.e("MainActivity", "API response not successful: ${response.code()}")
                        }
                    } else {
                        // Handle error response
                        Toast.makeText(
                            this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "MainActivity", "API error: ${response.code()} - ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<DetalResponse>, t: Throwable) {
                    // Handle network failure
                    Toast.makeText(
                        this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MainActivity", "API call failed", t)
                }
            })
    }

    private fun navigateToDetailActivity(category: DataItem, response: DetalResponse) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("CATEGORY_NAME", category.name)
            putExtra("CATEGORY_ID", category.id)
            putExtra("DETAIL_RESPONSE", response)
        }
        startActivity(intent)
    }
}