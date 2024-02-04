package com.vbevia.montanejosqr.registerevent.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class RegisterDeviceUuidModel(
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

/**
 *     "uuid": "1fae8d0e-1a20-37b0-a095-0b759ad42106",
 *     "name": "Barra",
 *     "phone": null,
 *     "location": null,
 *     "eventName": "Visita Fuente Baños"
 */