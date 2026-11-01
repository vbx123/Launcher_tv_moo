package com.nasim.launcher.tv

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.nasim.launcher.common.CityPresets
import com.nasim.launcher.common.ThemeConfig
import com.nasim.launcher.common.ThemeStore

/** الإعدادات السريعة على الشاشة: الثيم، الموقع، نظام الوقت، الطقس + بيانات الربط مع الموبايل. */
class SettingsDialog(
    ctx: Context,
    private val store: ThemeStore,
    private val tf: Typeface?
) : Dialog(ctx, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar) {

    private lateinit var btnJer: Button
    private lateinit var btnCyber: Button
    private lateinit var btnCity: Button
    private lateinit var btnTime: Button
    private lateinit var btnWeather: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val d = context.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        fun label(t: String, size: Float, color: Int = Color.WHITE) = TextView(context).apply {
            text = t; textSize = size; setTextColor(color); typeface = tf
            setPadding(0, dp(10), 0, dp(4))
        }
        fun button(onClick: () -> Unit) = Button(context).apply {
            typeface = tf; isAllCaps = false; setOnClickListener { onClick(); refresh() }
        }
        fun mutate(f: (ThemeConfig) -> ThemeConfig) = store.save(f(store.load()))

        btnJer = button { mutate { it.copy(themeId = ThemeConfig.THEME_JERUSALEM) } }
        btnCyber = button { mutate { it.copy(themeId = ThemeConfig.THEME_CYBER) } }
        btnCity = button {
            val list = CityPresets.list
            val cur = store.load()
            val next = list[(list.indexOfFirst { it.name == cur.cityName } + 1) % list.size]
            mutate { it.copy(cityName = next.name, lat = next.lat, lon = next.lon) }
        }
        btnTime = button { mutate { it.copy(use24h = !it.use24h) } }
        btnWeather = button { mutate { it.copy(showWeather = !it.showWeather) } }
        val close = Button(context).apply { text = "إغلاق"; typeface = tf; setOnClickListener { dismiss() } }

        val info = label(
            "للتحكم من الهاتف:  IP ${NetUtil.localIp()}   |   PIN ${store.pin()}", 15f, Color.parseColor("#9FE8FF")
        )

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(btnJer, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(btnCyber, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(28), dp(20), dp(28), dp(20))
            addView(label("الإعدادات السريعة", 24f))
            addView(label("الثيم", 15f, Color.LTGRAY))
            addView(row)
            addView(label("الموقع (الوقت والطقس) — اضغط للتبديل بين المدن", 15f, Color.LTGRAY))
            addView(btnCity)
            addView(btnTime)
            addView(btnWeather)
            addView(info)
            addView(close)
        }
        setContentView(root)
        window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
        refresh()
        btnJer.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        val dm = context.resources.displayMetrics
        window?.setLayout((dm.widthPixels * 0.62).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun refresh() {
        val c = store.load()
        val jer = c.themeId == ThemeConfig.THEME_JERUSALEM
        btnJer.text = (if (jer) "✓ " else "") + "نسيم القدس"
        btnCyber.text = (if (!jer) "✓ " else "") + "السيبراني الحديث"
        btnCity.text = "الموقع: ${c.cityName}"
        btnTime.text = "نظام الوقت: " + if (c.use24h) "24 ساعة" else "12 ساعة"
        btnWeather.text = "إظهار الطقس: " + if (c.showWeather) "نعم" else "لا"
    }
}
