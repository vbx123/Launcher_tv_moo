package com.nasim.launcher.common

import org.json.JSONObject

/** كل إعدادات اللانشر اللي بتتنقل من الموبايل للشاشة. */
data class ThemeConfig(
    val themeId: String = THEME_JERUSALEM,
    val cityName: String = "القدس",
    val lat: Double = 31.7683,
    val lon: Double = 35.2137,
    val use24h: Boolean = false,
    val showWeather: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject()
        .put("themeId", themeId)
        .put("cityName", cityName)
        .put("lat", lat)
        .put("lon", lon)
        .put("use24h", use24h)
        .put("showWeather", showWeather)

    companion object {
        const val THEME_JERUSALEM = "jerusalem"
        const val THEME_CYBER = "cyber"

        /** يقرأ JSON بشكل آمن: أي قيمة غلط تتحول لقيمة افتراضية. */
        fun fromJson(o: JSONObject): ThemeConfig {
            val d = ThemeConfig()
            val id = o.optString("themeId", d.themeId)
            return ThemeConfig(
                themeId = if (id == THEME_CYBER) THEME_CYBER else THEME_JERUSALEM,
                cityName = o.optString("cityName", d.cityName).take(40),
                lat = o.optDouble("lat", d.lat).takeIf { it in -90.0..90.0 } ?: d.lat,
                lon = o.optDouble("lon", d.lon).takeIf { it in -180.0..180.0 } ?: d.lon,
                use24h = o.optBoolean("use24h", d.use24h),
                showWeather = o.optBoolean("showWeather", d.showWeather)
            )
        }
    }
}

data class City(val name: String, val lat: Double, val lon: Double)

object CityPresets {
    val list = listOf(
        City("القدس", 31.7683, 35.2137),
        City("غزة", 31.5017, 34.4668),
        City("رام الله", 31.9038, 35.2034),
        City("الخليل", 31.5326, 35.0998),
        City("نابلس", 32.2211, 35.2544),
        City("عمّان", 31.9454, 35.9284),
        City("القاهرة", 30.0444, 31.2357),
        City("مكة المكرمة", 21.3891, 39.8579),
        City("الرياض", 24.7136, 46.6753),
        City("دبي", 25.2048, 55.2708)
    )
}
