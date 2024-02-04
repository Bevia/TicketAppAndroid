package com.vbevia.montanejosqr.dbqrentries

import androidx.room.Entity
import androidx.room.PrimaryKey

//Create an Entity class:
//
//Define a data class to represent your data, including the current time:

@Entity
data class MainDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val concept: String,
    val eventUuid: String,
    val state: String,
    val availableUsages: String,
    val time: String
)

/*
{
   "uuid":"4996e7c3-8e3e-3e13-89df-ea2879224c1e",
   "title":"Visita Fuente Ba\u00f1os",
   "event_uuid":"6b6d2c29-c54e-3186-8c0e-ad57ef275012",
   "concept":"Entrada gratis",
   "current_usages":1,
   "available_usages":0,
   "max_usages":1,
   "ignore_quota":false,
   "allow_admission":true,
   "state":"IN"
}


 */