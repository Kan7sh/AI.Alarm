package com.example.aialarm.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aialarm.data.db.entities.BasicAlarmEntity

@Dao
interface BasicAlarmDao {
    @Insert
    suspend fun insertAlarm(alarm: BasicAlarmEntity)

    @Update
    suspend fun updateAlarm(alarm: BasicAlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: BasicAlarmEntity)

    @Query("SELECT * FROM basicAlarms")
    fun getAllAlarms(): LiveData<List<BasicAlarmEntity>>

    @Query("UPDATE basicAlarms SET turnedOn = :turnedOn WHERE hour = :hour AND minute = :minute")
    suspend fun updateAlarmTurnedOnStatus(turnedOn: Boolean, hour: Int, minute: Int)

    @Query("SELECT * FROM basicAlarms  WHERE hour = :hour AND minute = :minute ")
    suspend fun getAlarmDetails(hour: Int, minute: Int):List<BasicAlarmEntity>
}