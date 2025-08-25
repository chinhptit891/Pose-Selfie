package com.nova.pose.selfie.model.cam

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class Data(

	@field:SerializedName("similar")
	val similar: List<SimilarItem>,

	@field:SerializedName("contour_white_url")
	val contourWhiteUrl: String,

	@field:SerializedName("author")
	val author: String,

	@field:SerializedName("bookmarked")
	val bookmarked: Boolean,



	@field:SerializedName("url")
	val url: String,

	@field:SerializedName("author_image")
	val authorImage: String,

	@field:SerializedName("contour_white_url_png")
	val contourWhiteUrlPng: String,

	@field:SerializedName("author_url")
	val authorUrl: String,

	@field:SerializedName("contour_black_url")
	val contourBlackUrl: String,

	@field:SerializedName("paid")
	val paid: Boolean,



	@field:SerializedName("is_collage")
	val isCollage: Boolean,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("categories")
	val categories: List<CategoriesItem>,

	@field:SerializedName("views")
	val views: Int,

	@field:SerializedName("contour_black_url_png")
	val contourBlackUrlPng: String
) : Parcelable