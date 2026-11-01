package com.nasim.launcher.tv

import android.content.Intent
import android.graphics.drawable.Drawable

class InstalledApp(val label: String, val icon: Drawable, val pkg: String, val intent: Intent)

class Tile(val label: String, val icon: Drawable?, val onClick: () -> Unit)
