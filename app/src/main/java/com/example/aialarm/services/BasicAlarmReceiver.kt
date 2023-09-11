package com.example.aialarm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.aialarm.ui.basicalarm.BasicAlarmPopupScreen

class BasicAlarmReceiver():BroadcastReceiver() {

    private var alarmHour: String? = null
    private var alarmMinute: String? = null
    private var isRepeating:Boolean = false
    private var ringtoneIndex = 0


    override fun onReceive(context: Context?, intent: Intent?) {


        if (intent != null) {

            ringtoneIndex = intent.getIntExtra("ringtone",0)
            alarmHour = intent.getStringExtra("hour")
            alarmMinute = intent.getStringExtra("minute")
            isRepeating = intent.getBooleanExtra("repeating",false)
        }


        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AI ALARM :AlarmWakeLock"
        )
        wakeLock.acquire(10*60*1000L)



        val intent = Intent(context, BasicAlarmSoundService::class.java)

        intent.putExtra("ringtone", ringtoneIndex)
        intent.putExtra("hour", alarmHour)
        intent.putExtra("minute", alarmMinute)

        ContextCompat.startForegroundService(context!!, intent)


        val broadcastIntent = Intent(context, BasicAlarmPopupScreen::class.java)

        broadcastIntent.putExtra("ringtone", ringtoneIndex)
        broadcastIntent.putExtra("hour", alarmHour)
        broadcastIntent.putExtra("minute", alarmMinute)
        broadcastIntent.putExtra("repeating",isRepeating)


        broadcastIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(broadcastIntent)



        wakeLock.release()

    }
}