package com.vbevia.montanejosqr.utils
import java.util.UUID

object UUIDGenerator {
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}