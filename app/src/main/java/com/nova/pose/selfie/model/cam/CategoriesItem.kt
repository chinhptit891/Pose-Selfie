package com.nova.pose.selfie.model.cam

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class CategoriesItem(

	@field:SerializedName("image")
	val image: String,

	@field:SerializedName("linkto")
	val linkto: String,

	@field:SerializedName("image_rectangle")
	val imageRectangle: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("paid")
	val paid: Boolean,

	@field:SerializedName("ideas")
	val ideas: Int,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("image_thumb")
	val imageThumb: String
) : Parcelable