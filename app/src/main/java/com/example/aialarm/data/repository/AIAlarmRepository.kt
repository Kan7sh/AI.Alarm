package com.example.aialarm.data.repository

import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.db.entities.AIAlarmEntity

class AIAlarmRepository(
    private val db: AppDatabase
) {

    fun getAIAlarm() = db.aiAlarmDao().getAllAIAlarms()

    suspend fun saveAIAlarm(aiAlarm: AIAlarmEntity) {
        db.aiAlarmDao().insertAIAlarm(aiAlarm)
    }

    suspend fun deleteAIAlarm(aiAlarm: AIAlarmEntity) {
        db.aiAlarmDao().deleteAIAlarm(aiAlarm)
    }

    suspend fun updateAIAlarm(aiAlarm: AIAlarmEntity) {
        db.aiAlarmDao().updateAIAlarm(aiAlarm)
    }

    suspend fun updateTurnoffAIAlarm(turnedOn: Boolean, hour: Int, minute: Int) {
        db.aiAlarmDao().updateAIAlarmTurnedOnStatus(turnedOn,hour,minute)
    }

    suspend fun getAIAlarmDetails(hour: Int, minute: Int):List<AIAlarmEntity> {
        return  db.aiAlarmDao().getAIAlarmDetails(hour,minute)
    }

}