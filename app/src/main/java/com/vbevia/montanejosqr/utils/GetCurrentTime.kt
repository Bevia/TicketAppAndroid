package com.vbevia.montanejosqr.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GetCurrentTime {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy       HH:mm")
        return currentDateTime.format(formatter)
    }
}