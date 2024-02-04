package com.bevia.mfypro.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt

object ScreenUtils {
    private var screenWidthPx: Int = 0
    private var screenHeightPx: Int = 0
    private var screenLengthDp: Int = 0

    fun initialize(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay

        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)

        screenWidthPx = displayMetrics.widthPixels
        screenHeightPx = displayMetrics.heightPixels

        val density = context.resources.displayMetrics.density
        screenLengthDp = (sqrt(
            (screenWidthPx.toDouble() / density).pow(2.0) +
                    (screenHeightPx.toDouble() / density).pow(2.0)
        ) + 0.5).toInt()
    }

    fun getScreenWidthPx(): Int = screenWidthPx

    fun getScreenHeightPx(): Int = screenHeightPx

    fun getScreenLengthDp(): Int = screenLengthDp
}