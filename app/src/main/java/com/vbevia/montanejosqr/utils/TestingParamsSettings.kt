package com.vbevia.montanejosqr.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

object TestingParamsSettings {

    fun settingsOn(context: Context) {
        SharePreferenceSingleton.setUui(context, "1fae8d0e-1a20-37b0-a095-0b759ad42106")
        SharePreferenceSingleton.setEventName(context, "Fuente de los baños")
    }

    fun resetSettings(context: Context) {
        SharePreferenceSingleton.setUui(context, "")
        SharePreferenceSingleton.setEventName(context, "")
    }
}