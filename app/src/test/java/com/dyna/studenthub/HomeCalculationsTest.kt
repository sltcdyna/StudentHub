package com.dyna.studenthub

import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.domain.HomeCalculations
import com.dyna.studenthub.domain.TaskCalculations
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class HomeCalculationsTest {
    @Test fun objectiveProgressIsClampedAndHandlesEmptyGoal() {
        assertEquals(0f, HomeCalculations.objectiveProgress(100, 0))
        assertEquals(1f, HomeCalculations.objectiveProgress(300, 200))
        assertEquals(0.5f, HomeCalculations.objectiveProgress(100, 200))
    }

    @Test fun onlyPendingTasksInInclusiveSevenDayWindowAreCounted() {
        val now = 1_000_000L
        val tasks = listOf(
            Task(type = TaskType.HOMEWORK, subject = "A", title = "today", dueDate = now, done = false),
            Task(type = TaskType.EXAM, subject = "B", title = "day7", dueDate = now + 7 * 24 * 60 * 60 * 1000L, done = false),
            Task(type = TaskType.HOMEWORK, subject = "C", title = "done", dueDate = now + 1, done = true),
            Task(type = TaskType.HOMEWORK, subject = "D", title = "late", dueDate = now - 1, done = false),
        )
        assertEquals(2, HomeCalculations.tasksWithinNext7Days(tasks, now).size)
    }

    @Test fun taskEarlierTodayIsStillCounted() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val earlierToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 9)
        }.timeInMillis
        val task = Task(
            type = TaskType.HOMEWORK,
            subject = "A",
            title = "today",
            dueDate = earlierToday,
            done = false,
        )

        assertEquals(1, HomeCalculations.tasksWithinNext7Days(listOf(task), now).size)
    }

    @Test fun datePickerDateKeepsItsCalendarDay() {
        val pickerValue = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 14)
        }.timeInMillis
        val local = TaskCalculations.datePickerMillisToLocalDay(pickerValue)
        val calendar = Calendar.getInstance().apply { timeInMillis = local }
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(14, calendar.get(Calendar.DAY_OF_MONTH))
    }
}
