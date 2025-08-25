package com.nova.pose.selfie.api

import com.nova.pose.selfie.model.DetalResponse
import com.nova.pose.selfie.model.RelatedImagesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("images/related/")
    fun getRelatedImages(
        @Body request: RelatedImagesRequest,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Call<DetalResponse>
}
