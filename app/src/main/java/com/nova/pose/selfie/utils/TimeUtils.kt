package com.nova.pose.selfie.utils

import java.util.*

object TimeUtils {
    
    /**
     * Determines if the current time is in the morning (6 AM - 6 PM)
     * @return true if it's morning, false if it's evening
     */
    fun isMorning(): Boolean {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Morning: 6:00 AM - 5:59 PM (6:00 - 17:59)
        // Evening: 6:00 PM - 5:59 AM (18:00 - 5:59)
        return hourOfDay in 6..17
    }
    
    /**
     * Gets a human-readable time description
     * @return "morning" or "evening"
     */
    fun getTimeDescription(): String {
        return if (isMorning()) "morning" else "evening"
    }
}
