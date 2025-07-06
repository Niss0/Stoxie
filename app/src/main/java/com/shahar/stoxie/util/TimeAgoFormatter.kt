package com.shahar.stoxie.util

import java.util.concurrent.TimeUnit

/**
 * A utility object to format a timestamp into a "time ago" string.
 * This is a singleton object, meaning there will only ever be one instance of it,
 * which is efficient for a stateless utility like this.
 */
object TimeAgoFormatter {

    // Define time constants in milliseconds for clarity
    private const val MINUTE_MILLIS = 60 * 1000L
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    /**
     * Converts a given timestamp into a relative "time ago" string.
     * e.g., "5m ago", "2h ago", "Yesterday".
     *
     * @param timestamp The timestamp of the post in milliseconds.
     * @return A formatted string representing the time difference.
     */
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        if (timestamp > now || timestamp <= 0) {
            return "in the future"
        }

        val diff = now - timestamp
        return when {
            diff < MINUTE_MILLIS -> "Just now"
            diff < 2 * MINUTE_MILLIS -> "1m ago"
            diff < 60 * MINUTE_MILLIS -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < 2 * HOUR_MILLIS -> "1h ago"
            diff < 24 * HOUR_MILLIS -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < 48 * HOUR_MILLIS -> "Yesterday"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        }
    }
}
