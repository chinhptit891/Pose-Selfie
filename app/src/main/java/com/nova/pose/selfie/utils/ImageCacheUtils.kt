package com.nova.pose.selfie.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.nova.pose.selfie.component.CamActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageCacheUtils {

    private const val CACHE_DIR_NAME = "pose_selfie_cache"
    private const val TAG = "ImageCacheUtils"

    /**
     * Downloads and caches an image from URL
     * @param context Application context
     * @param imageUrl URL of the image to download
     * @param fileName Custom filename for the cached image
     * @return File path of the cached image, or null if failed
     */
    suspend fun downloadAndCacheImage(
        context: Context, imageUrl: String, fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = getCacheDirectory(context)
            val imageFile = File(cacheDir, fileName)

            // Check if image already exists in cache
            if (imageFile.exists()) {
                Log.d(TAG, "Image already cached: ${imageFile.absolutePath}")
                return@withContext imageFile.absolutePath
            }

            // Download image from URL
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                // Save bitmap to cache file
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                Log.d(TAG, "Image cached successfully: ${imageFile.absolutePath}")
                return@withContext imageFile.absolutePath
            } else {
                Log.e(TAG, "Failed to decode image from URL: $imageUrl")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading/caching image: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Gets the cache directory for the app
     * @param context Application context
     * @return Cache directory File
     */
    private fun getCacheDirectory(context: Context): File {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Clears all cached images
     * @param context Application context
     */
    fun clearCache(context: Context) {
        try {
            val cacheDir = getCacheDirectory(context)
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                    }
                }
                Log.d(TAG, "Cache cleared successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}", e)
        }
    }

    /**
     * Gets the size of the cache directory
     * @param context Application context
     * @return Cache size in bytes
     */
    fun getCacheSize(context: Context): Long {
        return try {
            val cacheDir = getCacheDirectory(context)
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache size: ${e.message}", e)
            0L
        }
    }

    fun getOutputMediaFile(activity: Activity): File {

        val timeStamp = System.currentTimeMillis()
        return File(activity.filesDir, "IMG_" + timeStamp + ".jpg")
    }
}
