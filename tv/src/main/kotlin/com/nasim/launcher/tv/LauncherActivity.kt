package com.nasim.launcher.tv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nasim.launcher.common.HijriCalendar

class LauncherActivity : AppCompatActivity() {

    private lateinit var tvGregorian: TextView
    private lateinit var tvHijri: TextView
    private lateinit var btnInputsDialog: Button
    private lateinit var btnQuranSection: Button
    private lateinit var btnQrRemote: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        tvGregorian = findViewById(R.id.tvGregorian)
        tvHijri = findViewById(R.id.tvHijri)
        btnInputsDialog = findViewById(R.id.btnInputsDialog)
        btnQuranSection = findViewById(R.id.btnQuranSection)
        btnQrRemote = findViewById(R.id.btnQrRemote)

        val (gregorian, hijri) = HijriCalendar.getFormattedDates()
        tvGregorian.text = gregorian
        tvHijri.text = hijri

        btnInputsDialog.setOnClickListener {
            InputsDialog(this).show()
        }

        btnQuranSection.setOnClickListener {
            QuranDialog(this).show()
        }

        btnQrRemote.setOnClickListener {
            QrDialog(this).show()
        }
    }
}
