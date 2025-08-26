package com.nova.pose.selfie.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.nova.pose.selfie.R

class BackgroundAdapter(
    private val context: Context,
    private val assetImagePaths: List<String>,
    private val onClick: (Bitmap) -> Unit
) : RecyclerView.Adapter<BackgroundAdapter.BgVH>() {

    inner class BgVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BgVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_background, parent, false)
        return BgVH(view)
    }

    override fun getItemCount(): Int = assetImagePaths.size

    override fun onBindViewHolder(holder: BgVH, position: Int) {
        val path = assetImagePaths[position]
        val bm = context.assets.open(path).use { BitmapFactory.decodeStream(it) }
        holder.img.setImageBitmap(bm)
        holder.itemView.setOnClickListener { onClick(bm) }
    }
}


