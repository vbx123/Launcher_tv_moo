package com.nasim.launcher.tv

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class AzanOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "PLAY_AZAN") {
            val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"
            val isFullAzan = intent.getBooleanExtra("FULL_AZAN", true)
            showAzanWindow(prayerName, isFullAzan)
        }
        return START_STICKY
    }

    private fun showAzanWindow(prayerName: String, isFullAzan: Boolean) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_azan, null)

        val titleTv = overlayView?.findViewById<TextView>(R.id.tvAzanTitle)
        titleTv?.text = "حان الآن موعد اذان صلاة $prayerName"

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 50
        }

        try {
            windowManager?.addView(overlayView, params)
            if (isFullAzan) {
                // Play audio full azan sound
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
