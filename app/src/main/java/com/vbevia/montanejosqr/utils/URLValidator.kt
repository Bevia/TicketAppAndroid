package com.vbevia.montanejosqr.utils

import java.net.URL


object URLValidator {
    fun isValidURL(urlString: String?): Boolean {
        return try {
            val url = URL(urlString)
            // If no exception is thrown, the URL is valid.
            true
        } catch (e: Exception) {
            // Handle the exception or return false.
            false
        }
    }
}
