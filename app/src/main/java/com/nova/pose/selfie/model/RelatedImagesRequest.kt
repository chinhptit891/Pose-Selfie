package com.nova.pose.selfie.model

import com.google.gson.annotations.SerializedName

data class RelatedImagesRequest(
    @SerializedName("category_ids")
    val categoryIds: List<Int>
)
