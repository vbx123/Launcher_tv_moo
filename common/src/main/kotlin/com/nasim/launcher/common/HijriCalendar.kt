package com.nasim.launcher.common

import java.text.SimpleDateFormat
import java.util.*

object HijriCalendar {
    fun getFormattedDates(): Pair<String, String> {
        val now = Date()
        val gregorianFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar"))
        val gregorian = gregorianFormat.format(now)
        
        val hijriCalendar = Calendar.getInstance()
        val ucal = java.time.chrono.HijriChronology.INSTANCE.dateNow()
        val hijriStr = "${ucal.get(java.time.temporal.ChronoField.DAY_OF_MONTH)} ${ucal.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)} ${ucal.get(java.time.temporal.ChronoField.YEAR)} هـ"
        
        return Pair(gregorian, hijriStr)
    }
}
