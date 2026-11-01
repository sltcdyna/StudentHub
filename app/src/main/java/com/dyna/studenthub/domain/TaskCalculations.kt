package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Task
import java.util.Calendar
import java.util.TimeZone

object TaskCalculations {
    const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    fun reminderDelay(dueDate: Long, now: Long): Long =
        (dueDate - DAY_MILLIS - now).coerceAtLeast(0L)
    fun sorted(tasks: List<Task>): List<Task> =
        tasks.sortedWith(compareBy<Task> { it.dueDate }.thenBy { it.subject }.thenBy { it.title })

    fun datePickerMillisToLocalDay(value: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = value }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}
