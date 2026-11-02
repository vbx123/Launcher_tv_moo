package com.nasim.launcher.tv

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.nasim.launcher.common.QuranData

class QuranDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_quran)

        val listView = findViewById<ListView>(R.id.lvQuranOptions)
        val items = QuranData.RECITERS.map { "القارئ: ${it.name}" } + QuranData.RADIOS.map { "إذاعة: ${it.first}" }

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
    }
}
