package com.example.aialarm.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aialarm.data.db.entities.AIAlarmEntity

@Dao
interface AIAlarmDao {
    @Insert
    suspend fun insertAIAlarm(alarm: AIAlarmEntity)

    @Update
    suspend fun updateAIAlarm(alarm: AIAlarmEntity)

    @Delete
    suspend fun deleteAIAlarm(alarm: AIAlarmEntity)

    @Query("SELECT * FROM aiAlarms")
    fun getAllAIAlarms(): LiveData<List<AIAlarmEntity>>

    @Query("UPDATE aiAlarms SET turnedOn = :turnedOn WHERE hour = :hour AND minute = :minute")
    suspend fun updateAIAlarmTurnedOnStatus(turnedOn: Boolean, hour: Int, minute: Int)

    @Query("SELECT * FROM aiAlarms  WHERE hour = :hour AND minute = :minute ")
    suspend fun getAIAlarmDetails(hour: Int, minute: Int):List<AIAlarmEntity>
}