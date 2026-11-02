package com.nasim.launcher.tv

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class QrDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_qr)
        val tvIp = findViewById<TextView>(R.id.tvIpAddress)
        tvIp.text = "IP الشاشة للربط: 192.168.1.150"
    }
}
