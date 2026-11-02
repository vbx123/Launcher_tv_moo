package com.nasim.launcher.tv

import android.app.Application
import android.content.Intent

class TvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, AzanOverlayService::class.java))
    }
}
