package com.vbevia.montanejosqr.utils

import java.net.HttpURLConnection
import java.net.URL

object ValidateURL {

    fun validateURL(urlString: String?): Boolean {
        return try {
            // Create a URL object with the provided URL string
            val url = URL(urlString)

            // Check if the URL is valid
            url.toURI()

            // If no exception is thrown, the URL is valid
            true
        } catch (e: Exception) {
            // If an exception is thrown, the URL is invalid
            false
        }
    }

}