package com.example.aialarm.ui.aialarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.aialarm.data.db.entities.AIAlarmEntity
import com.example.aialarm.data.db.entities.BasicAlarmEntity
import com.example.aialarm.data.repository.AIAlarmRepository
import com.example.aialarm.services.AIAlarmReceiver
import com.example.aialarm.services.BasicAlarmReceiver
import com.example.aialarm.utlis.Coroutines
import com.example.aialarm.utlis.RepeatConverter
import com.example.aialarm.utlis.toast
import java.util.Calendar
import kotlin.math.min

class AIAlarmViewModel(
   private val  repository: AIAlarmRepository
) : ViewModel() {

   val aiAlarms: LiveData<List<AIAlarmEntity>> = repository.getAIAlarm()

   fun setOnceAIAlarm(hour:Int, min:Int, ringtone:Int, context: Context, isRescheduled:Boolean,cameraBitmapByteArray:ByteArray){


      val aiaAlarmEntity = AIAlarmEntity(
         hour=hour,
         minute = min,
         turnedOn = true,
         repeatDays = "",
         isRepeating = false,
         ringtone = ringtone,
         imageURI = cameraBitmapByteArray
      )
      if(!isRescheduled){
         Coroutines.io {
            repository.saveAIAlarm(aiaAlarmEntity)
         }
      }


      val intent = Intent(context, AIAlarmReceiver()::class.java)


      intent.putExtra("ringtone", ringtone)
      intent.putExtra("hour", hour.toString())
      intent.putExtra("minute", min.toString())
      intent.putExtra("repeating",false)
      intent.putExtra("savedImage",cameraBitmapByteArray)

      val EXACT_ALARM_INTENT_REQUEST_CODE = (aiaAlarmEntity.hour * 60 + aiaAlarmEntity.minute)*9
      val pendingIntent = PendingIntent.getBroadcast(context, EXACT_ALARM_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


      val now = Calendar.getInstance()
      val calendar = Calendar.getInstance()
      calendar[Calendar.HOUR_OF_DAY] = hour
      calendar[Calendar.MINUTE] = min
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0

      if (calendar.timeInMillis <= now.timeInMillis) {
         calendar.add(Calendar.DAY_OF_MONTH, 1)
      }

      val alarmTime = calendar.timeInMillis

      val timeDifferenceInMillis = alarmTime - now.timeInMillis

      val days = (timeDifferenceInMillis / (24 * 60 * 60 * 1000)).toInt()
      val hours = ((timeDifferenceInMillis / (60 * 60 * 1000)) % 24).toInt()
      val minutes = ((timeDifferenceInMillis / (60 * 1000)) % 60).toInt()

      if(hours!=0){
         context.toast("Alarm will ring in $hours hours and $minutes minutes")
      }
      if(hours==0&&minutes!=0){
         context.toast("Alarm will ring in $minutes minutes")
      }
      if(days==0&&hours==0&&minutes==0){
         context.toast("Alarm will ring in less then 1 minute")
      }


      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


      alarmManager.setExact(
         AlarmManager.RTC_WAKEUP,
         alarmTime,
         pendingIntent
      )



   }


   fun setRepeatingAIAlarm(hour:Int,min:Int,ringtone:Int,repeat:List<Int>,context:Context, isRescheduled:Boolean,cameraBitmapByteArray:ByteArray) {


      val aiAlarmEntity = AIAlarmEntity(
         hour=hour,
         minute = min,
         turnedOn = true,
         repeatDays = RepeatConverter.fromList(repeat),
         isRepeating = true,
         ringtone = ringtone,
         imageURI = cameraBitmapByteArray
      )

      if(!isRescheduled){
         Coroutines.io {
            repository.saveAIAlarm(aiAlarmEntity)
         }
      }

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val intent = Intent(context, AIAlarmReceiver()::class.java)


      intent.putExtra("ringtone", ringtone)
      intent.putExtra("hour", hour.toString())
      intent.putExtra("minute", min.toString())
      intent.putExtra("repeating",true)
      intent.putExtra("savedImage",cameraBitmapByteArray)



      val now = Calendar.getInstance()

      var minTime = Long.MAX_VALUE

      for (selectedDay in repeat) {

         val calendar = Calendar.getInstance()
         calendar[Calendar.HOUR_OF_DAY] = hour
         calendar[Calendar.MINUTE] = min
         calendar[Calendar.SECOND] = 0
         calendar[Calendar.MILLISECOND] = 0
         val alarmDay = selectedDay % 7
         val dayDiff = (alarmDay - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
         calendar.set(Calendar.DAY_OF_WEEK,selectedDay)


         if (calendar.timeInMillis <= now.timeInMillis) {
            calendar.add(Calendar.DAY_OF_MONTH, 7)
         }


         val alarmTime = calendar.timeInMillis

         val EXACT_ALARM_INTENT_REQUEST_CODE =( hour * 60 + min)*selectedDay*9

         val pendingIntent = PendingIntent.getBroadcast(context, EXACT_ALARM_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

         val timeDifferenceInMillis = alarmTime - now.timeInMillis

         minTime = min(timeDifferenceInMillis,minTime)

         alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
         )
      }

      val days = (minTime / (24 * 60 * 60 * 1000)).toInt()
      val hours = ((minTime / (60 * 60 * 1000)) % 24).toInt()
      val minutes = ((minTime / (60 * 1000)) % 60).toInt()

      if(days==0&&hours!=0){
         context.toast("Alarm will ring in $hours hours and $minutes minutes")
      }
      if(days!=0){
         context.toast("Alarm will ring in $days days $hours hours and $minutes minutes")
      }
      if(days==0&&hours==0&&minutes!=0){
         context.toast("Alarm will ring in $minutes minutes")
      }
      if(days==0&&hours==0&&minutes==0){
         context.toast("Alarm will ring in less then 1 minute")
      }



   }


   fun cancelAlarm(aiAlarmEntity: AIAlarmEntity,context:Context){

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val EXACT_ALARM_INTENT_REQUEST_CODE = (aiAlarmEntity.hour * 60 + aiAlarmEntity.minute)*9
      val intent = Intent(context, AIAlarmReceiver()::class.java)
      val pendingIntent = PendingIntent.getBroadcast(context, EXACT_ALARM_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
      alarmManager.cancel(pendingIntent)

   }


   fun cancelRepeatAlarm(aiAlarmEntity: AIAlarmEntity,context:Context){

      val repeatDays = RepeatConverter.toList(aiAlarmEntity.repeatDays)

      for(days in repeatDays){
         val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         val EXACT_ALARM_INTENT_REQUEST_CODE = (aiAlarmEntity.hour * 60 + aiAlarmEntity.minute)*days*9
         val intent = Intent(context, AIAlarmReceiver()::class.java)

         val pendingIntent = PendingIntent.getBroadcast(context, EXACT_ALARM_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
         alarmManager.cancel(pendingIntent)
      }



   }


   fun deleteAlarm(aiAlarmEntity: AIAlarmEntity){
      Coroutines.io {
         repository.deleteAIAlarm(aiAlarmEntity)
      }
   }

   fun updateAlarm(aiAlarmEntity: AIAlarmEntity){
      Coroutines.io {
         repository.updateAIAlarm(aiAlarmEntity)
      }
   }


}