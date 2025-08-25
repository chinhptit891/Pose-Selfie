package com.nova.pose.selfie.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class ImagesItem(

	@field:SerializedName("url_rectangle")
	val urlRectangle: String,

	@field:SerializedName("linkto")
	val linkto: String,

	@field:SerializedName("author")
	val author: String,

	@field:SerializedName("paid")
	val paid: Boolean,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("url")
	val url: String
) : Parcelable