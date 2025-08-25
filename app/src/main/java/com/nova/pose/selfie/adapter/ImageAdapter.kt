package com.nova.pose.selfie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nova.pose.selfie.R
import com.nova.pose.selfie.model.ImagesItem

class ImageAdapter(
    private val onItemClick: (ImagesItem) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var images: List<ImagesItem> = emptyList()

    fun updateData(newImages: List<ImagesItem>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImage)
        private val authorTextView: TextView = itemView.findViewById(R.id.tvAuthor)

        fun bind(image: ImagesItem) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(image.url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imageView)

            authorTextView.text = image.author
            
            itemView.setOnClickListener {
                onItemClick(image)
            }
        }
    }
}
