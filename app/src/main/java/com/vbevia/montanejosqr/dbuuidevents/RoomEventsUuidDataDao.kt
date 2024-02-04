package com.vbevia.montanejosqr.dbuuidevents

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RoomEventsUuidDataDao {
    @Insert
    suspend fun insert(roomData: EventsUuidDBModel)

    //@Query("SELECT * FROM MainDBModel")
    @Query("SELECT * FROM EventsUuidDBModel ORDER BY id ASC")
    fun getAllRoomDataAscending(): LiveData<List<EventsUuidDBModel>>

    @Query("SELECT * FROM EventsUuidDBModel ORDER BY id DESC")
    fun getAllRoomDataDescending(): LiveData<List<EventsUuidDBModel>>

    @Query("SELECT COUNT(*) FROM EventsUuidDBModel WHERE uuid = :valueToCheck")
    suspend fun doesEntryExist(valueToCheck: String): Int

    @Query("DELETE FROM EventsUuidDBModel")
    suspend fun deleteAllData()

    @Delete
    suspend fun delete(item: EventsUuidDBModel)

    @Query("DELETE FROM EventsUuidDBModel WHERE uuid = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("SELECT COUNT(*) FROM EventsUuidDBModel")
    suspend fun getRowCount(): Int
}