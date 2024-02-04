package com.vbevia.montanejosqr.dbqrentries

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

//Create a Data Access Object (DAO):
//
//Define an interface that includes methods for performing database operations:
@Dao
interface RoomDataDao {
    @Insert
    suspend fun insert(roomData: MainDBModel)

    //@Query("SELECT * FROM MainDBModel")
    @Query("SELECT * FROM MainDBModel ORDER BY id ASC")
    fun getAllRoomDataAscending(): LiveData<List<MainDBModel>>

    @Query("SELECT * FROM MainDBModel ORDER BY id DESC")
    fun getAllRoomDataDescending(): LiveData<List<MainDBModel>>

    @Query("DELETE FROM MainDBModel")
    suspend fun deleteAllData()

    @Query("SELECT COUNT(*) FROM MainDBModel")
    suspend fun getRowCount(): Int
}