package com.example.aialarm.utlis

import java.util.Calendar

fun getCurrentHours(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.HOUR_OF_DAY)
}

fun getCurrentMinutes(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.MINUTE)
}

fun getCurrentDayOfWeek(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.DAY_OF_WEEK)
}