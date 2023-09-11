package com.example.aialarm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.aialarm.ui.aialarm.AIAlarmPopupScreen
import com.example.aialarm.ui.basicalarm.BasicAlarmPopupScreen

class AIAlarmReceiver():BroadcastReceiver() {

    private var alarmHour: String? = null
    private var alarmMinute: String? = null
    private var isRepeating:Boolean = false
    private var ringtoneIndex = 0
    private lateinit var savedImageByteArray: ByteArray

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TESTING", "onReceive: INN AI ALARM RECEIVER")

        if (intent != null) {

            ringtoneIndex = intent.getIntExtra("ringtone",0)
            alarmHour = intent.getStringExtra("hour")
            alarmMinute = intent.getStringExtra("minute")
            isRepeating = intent.getBooleanExtra("repeating",false)
            savedImageByteArray = intent.getByteArrayExtra("savedImage")!!
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


        val broadcastIntent = Intent(context, AIAlarmPopupScreen::class.java)

        broadcastIntent.putExtra("ringtone", ringtoneIndex)
        broadcastIntent.putExtra("hour", alarmHour)
        broadcastIntent.putExtra("minute", alarmMinute)
        broadcastIntent.putExtra("repeating",isRepeating)
        broadcastIntent.putExtra("savedImage",savedImageByteArray)

        broadcastIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(broadcastIntent)



        wakeLock.release()

    }
}