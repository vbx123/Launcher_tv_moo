package com.nasim.launcher.common

object Protocol {
    const val DEFAULT_PORT = 8080
    
    // Commands from Remote Phone
    const val CMD_LAUNCH_APP = "CMD_LAUNCH_APP"
    const val CMD_CHANGE_BG = "CMD_CHANGE_BG"
    const val CMD_REMOTE_KEY = "CMD_REMOTE_KEY"
    const val CMD_UPDATE_APP_ORDER = "CMD_UPDATE_APP_ORDER"
    const val CMD_SET_SAVED_MEDIA = "CMD_SET_SAVED_MEDIA"
    
    // Remote Keys
    const val KEY_UP = "UP"
    const val KEY_DOWN = "DOWN"
    const val KEY_LEFT = "LEFT"
    const val KEY_RIGHT = "RIGHT"
    const val KEY_ENTER = "ENTER"
    const val KEY_BACK = "BACK"
    const val KEY_HOME = "HOME"
}
