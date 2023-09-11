package com.example.aialarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aialarm.data.db.dao.AIAlarmDao
import com.example.aialarm.data.db.dao.BasicAlarmDao
import com.example.aialarm.data.db.entities.AIAlarmEntity
import com.example.aialarm.data.db.entities.BasicAlarmEntity

@Database(entities = [BasicAlarmEntity::class,AIAlarmEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): BasicAlarmDao
    abstract fun aiAlarmDao(): AIAlarmDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "alarm-db"
                ).build().also { instance = it }
            }
        }
    }
}