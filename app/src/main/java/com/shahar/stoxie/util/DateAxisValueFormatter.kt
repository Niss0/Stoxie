package com.shahar.stoxie.util

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Chart axis formatter for dates based on time period.
 * Formats timestamps for different chart periods (intraday vs daily).
 */
class DateAxisValueFormatter(private val period: String) : ValueFormatter() {

    private val dailyFormat = SimpleDateFormat("MMM dd", Locale.US)
    private val intradayFormat = SimpleDateFormat("HH:mm", Locale.US)

    /**
     * Formats timestamp value for chart axis display.
     * @param value Unix timestamp as float
     * @return Formatted date string appropriate for the time period
     */
    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong())
        return when (period) {
            "1D" -> intradayFormat.format(date)
            else -> dailyFormat.format(date)
        }
    }
}