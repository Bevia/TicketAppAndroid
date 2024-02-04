package com.vbevia.montanejosqr.dbqrentries

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Create a Room Database:
//
//Create an abstract class that extends RoomDatabase and includes the definition of your DAO (Data Access Object):
@Database(entities = [MainDBModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDataDao(): RoomDataDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}