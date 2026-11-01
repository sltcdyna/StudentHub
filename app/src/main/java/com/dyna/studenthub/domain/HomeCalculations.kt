package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Task
import java.util.Calendar

object HomeCalculations {
    fun objectiveProgress(balanceCents: Long, objectiveCents: Long): Float =
        if (objectiveCents <= 0L) 0f else (balanceCents.toDouble() / objectiveCents).toFloat().coerceIn(0f, 1f)

    fun tasksWithinNext7Days(tasks: List<Task>, now: Long): List<Task> {
        val start = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 7L * TaskCalculations.DAY_MILLIS
        return tasks.filter { !it.done && it.dueDate >= start && it.dueDate <= end }
            .sortedWith(compareBy<Task> { it.dueDate }.thenBy { it.subject }.thenBy { it.title })
    }
}
