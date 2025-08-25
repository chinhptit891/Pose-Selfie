package com.nova.pose.selfie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nova.pose.selfie.R
import com.nova.pose.selfie.model.DataItem

class CategoryAdapter(
    private val onItemClick: (DataItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SQUARE = 0
        private const val VIEW_TYPE_RECTANGLE = 1
    }

    private var categories: List<DataItem> = emptyList()

    fun updateData(newCategories: List<DataItem>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        // Theo yêu cầu:
        // Position 0: rectangle (full width)
        // Position 1: square (half width)
        // Position 2: square (half width)
        // Position 3: rectangle (full width)
        // Position 4+: all square (half width)
        return if (position == 0 || position == 3) {
            VIEW_TYPE_RECTANGLE
        } else {
            VIEW_TYPE_SQUARE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SQUARE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_small, parent, false)
                SquareViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category, parent, false)
                RectangleViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SquareViewHolder -> holder.bind(categories[position])
            is RectangleViewHolder -> holder.bind(categories[position])
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class SquareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivCategory)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val ideasTextView: TextView = itemView.findViewById(R.id.tvIdeasCount)

        fun bind(category: DataItem) {
            // Load image using Glide - use square image for square items
            Glide.with(itemView.context)
                .load(category.image) // Use square image
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imageView)

            nameTextView.text = category.name
            ideasTextView.text = itemView.context.getString(R.string.ideas_count_format, category.ideas)
            
            itemView.setOnClickListener {
                onItemClick(category)
            }
        }
    }

    inner class RectangleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivCategory)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val ideasTextView: TextView = itemView.findViewById(R.id.tvIdeasCount)

        fun bind(category: DataItem) {
            // Load image using Glide - use rectangle image for rectangle items
            Glide.with(itemView.context)
                .load(category.imageRectangle) // Use rectangle image
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imageView)

            nameTextView.text = category.name
            ideasTextView.text = itemView.context.getString(R.string.ideas_count_format, category.ideas)
            
            itemView.setOnClickListener {
                onItemClick(category)
            }
        }
    }
}
