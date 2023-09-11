package com.example.aialarm.ui.basicalarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.repository.BasicAlarmRepository
import com.example.aialarm.databinding.BasicAlarmPopupBinding
import com.example.aialarm.services.BasicAlarmReceiver
import com.example.aialarm.services.BasicAlarmSoundService
import com.example.aialarm.utlis.Coroutines
import com.example.aialarm.utlis.getCurrentDayOfWeek
import java.util.Calendar

class BasicAlarmPopupScreen : AppCompatActivity() {

    private var alarmHour: String? = null
    private var alarmMinute: String? = null
    private var ringtoneIndex = 0
    private var isRepeating = false
    private lateinit var repository: BasicAlarmRepository
    private val dayAbbreviations = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d("in", "dispatchKeyEvent: uuppp")
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        showWhenLockedAndTurnScreenOn()
        turnScreenOnAndKeyguardOff()

        val binding = DataBindingUtil.inflate<BasicAlarmPopupBinding>(LayoutInflater.from(this),R.layout.basic_alarm_popup,null,false)

        repository = BasicAlarmRepository(AppDatabase.getInstance(this))

        ringtoneIndex = intent.getIntExtra("ringtone",0)
        alarmHour = intent.getStringExtra("hour")
        alarmMinute = intent.getStringExtra("minute")
        isRepeating = intent.getBooleanExtra("repeating",false)


        val params = WindowManager.LayoutParams(

            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,

        )



        windowManager.addView(binding.root, params)

        binding.popupText.text = "${String.format("%02d", alarmHour!!.toInt())}:${String.format("%02d", alarmMinute!!.toInt())}"




        binding.dayText.text=dayAbbreviations[getCurrentDayOfWeek()-1]

        binding.basicStopButton.setOnClickListener {

            updateAlarm()

            val intent = Intent(this, BasicAlarmSoundService::class.java)
            stopService(intent)

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.cancel(alarmHour!!.toInt() * 60 + alarmMinute!!.toInt())

            windowManager.removeView(binding.root)
            finish()

        }

        binding.basicSnoozeButton.setOnClickListener {

            setSnoozeAlarm()

            val intent = Intent(this, BasicAlarmSoundService::class.java)
            stopService(intent)

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.cancel(alarmHour!!.toInt() * 60 + alarmMinute!!.toInt())

            windowManager.removeView(binding.root)
            finish()

        }

    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    fun Activity.turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
            }
        }
    }


    private fun setSnoozeAlarm() {

        updateAlarm()

        val snoozeHour =if(alarmMinute!!.toInt()<55)alarmHour!!.toInt() else (alarmHour!!.toInt()+1)%24
        val snoozeMinute =(alarmMinute!!.toInt()+5)%60

        val intent = Intent(this, BasicAlarmReceiver()::class.java)


        intent.putExtra("ringtone", ringtoneIndex)
        intent.putExtra("hour", snoozeHour.toString())
        intent.putExtra("minute", snoozeMinute.toString())
        intent.putExtra("repeating",isRepeating)


        Log.d("TESTING", "setSnoozeAlarm: $snoozeHour && $snoozeMinute")


        val EXACT_ALARM_INTENT_REQUEST_CODE =  snoozeHour * 60 + snoozeMinute
        val pendingIntent = PendingIntent.getBroadcast(this, EXACT_ALARM_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = snoozeHour
        calendar[Calendar.MINUTE] = snoozeMinute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0


        if (calendar.timeInMillis <= now.timeInMillis) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmTime = calendar.timeInMillis

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager


        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )

    }



    fun updateAlarm(){
        if(!isRepeating){
            Coroutines.io {
                repository.updateTurnoffBasicAlarm(false,alarmHour!!.toInt(),alarmMinute!!.toInt())
            }
        }
    }




}