package com.nova.pose.selfie.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class Data(

	@field:SerializedName("images")
	val images: List<ImagesItem>,

	@field:SerializedName("ideas")
	val ideas: Int
) : Parcelable