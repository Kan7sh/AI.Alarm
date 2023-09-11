package com.example.aialarm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.example.aialarm.R
import com.example.aialarm.ui.basicalarm.BasicAlarmPopupScreen
import com.example.aialarm.ui.home.MainActivity

class BasicAlarmSoundService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var audioFocusRequested = false
    private lateinit var  floatingView: View

    private val ringtones = arrayOf(R.raw.basic,R.raw.classic_alarm,R.raw.oversimplified,R.raw.evacuation,R.raw.emergency)

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            stopAlarmSound()
        }
    }

    private var alarmHour: String? = null
    private var alarmMinute: String? = null
    private var ringtoneIndex = 0


    private fun createNotification(): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel"
            val channelName = "Alarm Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }


        val notificationBuilder = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("$alarmHour:$alarmMinute Alarm")
            .setContentText("Alarm sound is playing.")
            .setSmallIcon(R.drawable.aialarm_clock)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        return notificationBuilder.build()
    }


    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {

            ringtoneIndex = intent.getIntExtra("ringtone",0)
            alarmHour = intent.getStringExtra("hour")
            alarmMinute = intent.getStringExtra("minute")


            mediaPlayer = MediaPlayer.create(this, ringtones[ringtoneIndex])
            mediaPlayer.isLooping = true
        }

        startForeground(alarmHour!!.toInt() * 60 + alarmMinute!!.toInt(), createNotification())

        requestAudioFocus()

        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }

        return START_STICKY
    }


    override fun onDestroy() {
        stopAlarmSound()

        stopForeground(true)

        super.onDestroy()
    }


    private fun requestAudioFocus() {
        if (!audioFocusRequested) {

            val result = audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN
            )

            audioFocusRequested = true
        }
    }

    private fun stopAlarmSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        if (audioFocusRequested) {
            audioManager.abandonAudioFocus(afChangeListener)
            audioFocusRequested = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
