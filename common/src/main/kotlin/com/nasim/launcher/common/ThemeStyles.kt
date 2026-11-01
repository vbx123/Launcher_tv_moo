package com.nasim.launcher.common

import android.graphics.Color
import androidx.annotation.DrawableRes

/** كل ما يخص شكل الثيم (موارد + ألوان) في مكان واحد، يستخدمه التلفزيون وشاشة المعاينة. */
data class ThemeStyle(
    @DrawableRes val background: Int,
    @DrawableRes val cardBg: Int,
    val textColor: Int,
    val subTextColor: Int,
    val showStatusBar: Boolean,
    val useSerif: Boolean
)

object ThemeStyles {
    fun of(id: String): ThemeStyle = if (id == ThemeConfig.THEME_CYBER) {
        ThemeStyle(R.drawable.bg_cyber, R.drawable.card_cyber, Color.WHITE, Color.parseColor("#9FE8FF"), true, false)
    } else {
        ThemeStyle(R.drawable.bg_jerusalem, R.drawable.card_jerusalem, Color.WHITE, Color.parseColor("#FFE2A8"), false, true)
    }
}
