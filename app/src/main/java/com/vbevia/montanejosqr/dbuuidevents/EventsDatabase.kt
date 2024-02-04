package com.vbevia.montanejosqr.dbuuidevents

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EventsUuidDBModel::class], version = 1)
abstract class EventsDatabase : RoomDatabase() {
    abstract fun roomUuidDataDao(): RoomEventsUuidDataDao

    companion object {
        private var INSTANCE: EventsDatabase? = null

        fun getInstance(context: Context): EventsDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    EventsDatabase::class.java,
                    "event_database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}