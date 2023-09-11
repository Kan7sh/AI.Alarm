package com.example.aialarm.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basicAlarms")
data class BasicAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var hour: Int,
    var minute: Int,
    var turnedOn: Boolean,
    var repeatDays:String,
    var isRepeating:Boolean,
    var ringtone:Int
)
