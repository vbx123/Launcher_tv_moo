package com.nasim.launcher.tv

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout

class InputsDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_inputs)

        val container = findViewById<LinearLayout>(R.id.inputsContainer)

        val inputsList = listOf(
            "HDMI 1", "HDMI 2", "HDMI 3", "AV / TV",
            "VGA", "Component", "Media Player", "Settings Inputs"
        )

        for (inputName in inputsList) {
            val btn = Button(context).apply {
                text = inputName
                setBackgroundResource(R.drawable.gold_border_tile)
                setOnClickListener {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("com.android.tv.settings")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dismiss()
                }
            }
            container.addView(btn)
        }
    }
}
