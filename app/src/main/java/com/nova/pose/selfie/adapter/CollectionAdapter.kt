package com.nova.pose.selfie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nova.pose.selfie.R
import java.io.File

class CollectionAdapter(
    private val onImageClick: (File) -> Unit,
    private val onOptionsClick: (File, View) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    private var imageFiles: List<File> = emptyList()

    fun updateImages(newImageFiles: List<File>) {
        imageFiles = newImageFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageFiles[position])
    }

    override fun getItemCount(): Int = imageFiles.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val btnOptions: ImageView = itemView.findViewById(R.id.btnOptions)

        fun bind(imageFile: File) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(imageFile)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView)

            // Set click listeners
            imageView.setOnClickListener {
                onImageClick(imageFile)
            }

            btnOptions.setOnClickListener {
                onOptionsClick(imageFile, btnOptions)
            }
        }
    }
}
