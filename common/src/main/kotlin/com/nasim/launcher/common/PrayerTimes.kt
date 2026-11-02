package com.nasim.launcher.common

enum class AzanMode { FULL_AZAN, NOTIFICATION_ONLY, MUTE }

data class PrayerTime(
    val name: String,
    val timeStr: String,
    var mode: AzanMode = AzanMode.FULL_AZAN
)

object PrayerManager {
    fun getPrayerTimes(): List<PrayerTime> {
        return listOf(
            PrayerTime("الفجر", "04:45"),
            PrayerTime("الشروق", "06:12"),
            PrayerTime("الظهر", "11:55"),
            PrayerTime("العصر", "15:10"),
            PrayerTime("المغرب", "17:35"),
            PrayerTime("العشاء", "18:55")
        )
    }
}
