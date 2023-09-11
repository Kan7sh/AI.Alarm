package com.example.aialarm.data.repository

import android.util.Log
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.db.entities.BasicAlarmEntity

class BasicAlarmRepository(
    private val db:AppDatabase
) {

    fun getBasicAlarm() = db.alarmDao().getAllAlarms()

    suspend fun saveBasicAlarm(basicAlarm:BasicAlarmEntity) {
        db.alarmDao().insertAlarm(basicAlarm)
    }

    suspend fun deleteBasicAlarm(basicAlarm:BasicAlarmEntity) {
        db.alarmDao().deleteAlarm(basicAlarm)
    }

    suspend fun updateBasicAlarm(basicAlarm:BasicAlarmEntity) {
        db.alarmDao().updateAlarm(basicAlarm)
    }

    suspend fun updateTurnoffBasicAlarm(turnedOn: Boolean, hour: Int, minute: Int) {
        db.alarmDao().updateAlarmTurnedOnStatus(turnedOn,hour,minute)
    }

    suspend fun getBasicAlarmDetails(hour: Int, minute: Int):List<BasicAlarmEntity> {
       return  db.alarmDao().getAlarmDetails(hour,minute)
    }



}