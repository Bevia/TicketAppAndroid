package com.vbevia.montanejosqr.utils

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import android.content.Context
import android.os.Build

object VibrateSingleton {

    fun vibrateDevice(context: Context) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                // For devices with versions earlier than Android Oreo
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
            }
        }
    }
}
