package com.nova.pose.selfie.utils

import android.content.Context
import android.util.Log
import java.io.File

object CollectionUtils {
    
    private const val TAG = "CollectionUtils"
    
    /**
     * Gets all image files from the app's internal files directory
     * @param context Application context
     * @return List of image files sorted by modification time (newest first)
     */
    fun getImageFilesFromDirectory(context: Context): List<File> {
        val imageFiles = mutableListOf<File>()
        
        try {
            val filesDir = context.filesDir
            
            if (filesDir.exists() && filesDir.isDirectory) {
                filesDir.listFiles()?.forEach { file ->
                    if (file.isFile && isImageFile(file)) {
                        imageFiles.add(file)
                    }
                }
            }
            
            // Sort by modification time (newest first)
            return imageFiles.sortedByDescending { it.lastModified() }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image files: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Checks if a file is an image file based on its extension
     * @param file File to check
     * @return true if the file is an image file
     */
    fun isImageFile(file: File): Boolean {
        val fileName = file.name.lowercase()
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".webp")
    }
    
    /**
     * Gets the total size of all image files in the collection
     * @param context Application context
     * @return Total size in bytes
     */
    fun getCollectionSize(context: Context): Long {
        return try {
            val imageFiles = getImageFilesFromDirectory(context)
            imageFiles.sumOf { it.length() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting collection size: ${e.message}", e)
            0L
        }
    }
    
    /**
     * Gets the count of images in the collection
     * @param context Application context
     * @return Number of image files
     */
    fun getImageCount(context: Context): Int {
        return try {
            getImageFilesFromDirectory(context).size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image count: ${e.message}", e)
            0
        }
    }
    
    /**
     * Clears all images from the collection
     * @param context Application context
     * @return Number of files deleted
     */
    fun clearCollection(context: Context): Int {
        return try {
            val imageFiles = getImageFilesFromDirectory(context)
            var deletedCount = 0
            
            imageFiles.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }
            
            Log.d(TAG, "Cleared $deletedCount images from collection")
            deletedCount
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing collection: ${e.message}", e)
            0
        }
    }
}
