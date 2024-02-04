package com.vbevia.montanejosqr.utils

import android.media.AudioManager
import android.media.ToneGenerator

object BeepObject {
    fun playBeep() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100) // Adjust volume here
       // toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT)
       // val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100) // Adjust volume here
         toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 2000) // Play beep tone for 200 ms
    }
}