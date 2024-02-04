package com.vbevia.montanejosqr.dbuuidevents

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventsUuidDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val event: String
)