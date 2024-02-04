package com.vbevia.montanejosqr.qrevent

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class QREventModel(
    @SerializedName("in_global")
    val inGlobal: Int,

    @SerializedName("in_today")
    val inToday: Int,

    @SerializedName("out_global")
    val outGlobal: Int,

    @SerializedName("out_today")
    val outToday: Int
) : Serializable

data class EventCapacity(
    val current: Int,
    val limit: Int,
    val temperature: String
)

data class YourResponse(
    val people: QREventModel,

    @SerializedName("event_capacity")
    val eventCapacity: EventCapacity
)

/**
        {
            "people": {
                "in_global": 2,
                "in_today": 2,
                "out_global": 1,
                "out_today": 1
            },
            "event_capacity": {
                "current": 49,
                "limit": 50,
                "temperature": "green"
            }
        }
 */