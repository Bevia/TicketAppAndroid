package com.vbevia.montanejosqr.registerevent.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class RegisterEventModel(
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("eventName")
    val eventName: String?,
) : Serializable


data class TerminalResponse(
    val items: List<RegisterEventModel>
)
