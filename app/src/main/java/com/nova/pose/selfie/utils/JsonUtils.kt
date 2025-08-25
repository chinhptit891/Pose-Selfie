package com.nova.pose.selfie.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nova.pose.selfie.model.HomeResponse

object JsonUtils {
    
    fun loadCategoriesFromAssets(context: Context): List<com.nova.pose.selfie.model.DataItem> {
        return try {
            val inputStream = context.assets.open("categories.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            
            val jsonString = String(buffer, Charsets.UTF_8)
            val homeResponse = Gson().fromJson(jsonString, HomeResponse::class.java)
            
            homeResponse.data
        } catch (e: Exception) {
            Log.e("JsonUtils", "Error loading categories: ${e.message}")
            emptyList()
        }
    }
}
