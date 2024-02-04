package com.vbevia.montanejosqr.qrcheckout

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Keep
data class QRCheckOutModel(
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("event_uuid")
    val eventUuid: String?,
    @SerializedName("concept")
    val concept: String?,
    @SerializedName("current_usages")
    val currentUsages: Int?,
    @SerializedName("available_usages")
    val availableUsages: Int?,
    @SerializedName("max_usages")
    val maxUsages: Int?,
    @SerializedName("ignore_quota")
    val ignoreQuota: Boolean?,
    @SerializedName("allow_admission")
    val allowAdmission: Boolean?,
    @SerializedName("state")
    val state: String?,
) : Serializable
  /*

  qr : 1fae8d0e-1a20-37b0-a095-0b759ad42106

  Happy path:

  {
    "uuid": "4996e7c3-8e3e-3e13-89df-ea2879224c1e",
    "title": "Visita Fuente Baños",
    "event_uuid": "6b6d2c29-c54e-3186-8c0e-ad57ef275012",
    "concept": "Entrada gratis",
    "current_usages": 1,
    "available_usages": 0,
    "max_usages": 1,
    "ignore_quota": false,
    "allow_admission": true,
    "state": "OUT"
}

ERROR:

{
    "status": 403,
    "string_status": "error",
    "message": "Too much movements OUT for ticket [4996e7c3-8e3e-3e13-89df-ea2879224c1e], current usages [1], current movements out [1]",
    "data": []
}


   */
