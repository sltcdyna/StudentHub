package com.dyna.studenthub

import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.domain.TaskCalculations
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TaskCalculationsTest {
    @Test
    fun `tri par echeance puis matiere puis titre`() {
        val tasks = listOf(
            Task(1, TaskType.HOMEWORK, "Zoologie", "A", 2_000, false),
            Task(2, TaskType.HOMEWORK, "Maths", "B", 1_000, false),
            Task(3, TaskType.HOMEWORK, "Maths", "A", 1_000, false),
        )
        assertEquals(listOf(3L, 2L, 1L), TaskCalculations.sorted(tasks).map { it.id })
    }

    @Test
    fun `rappel programme la veille`() {
        val now = 10_000L
        assertEquals(TaskCalculations.DAY_MILLIS, TaskCalculations.reminderDelay(now + 2 * TaskCalculations.DAY_MILLIS, now))
        assertEquals(0L, TaskCalculations.reminderDelay(now, now))
    }

    @Test
    fun `les dates conservees restent identiques entre le calendrier et la date locale`() {
        val previousTimeZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
            val pickerDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(2026, Calendar.MARCH, 1)
            }.timeInMillis

            val localDate = TaskCalculations.datePickerMillisToLocalDay(pickerDate)
            val localCalendar = Calendar.getInstance().apply { timeInMillis = localDate }

            assertEquals(Calendar.MARCH, localCalendar.get(Calendar.MONTH))
            assertEquals(1, localCalendar.get(Calendar.DAY_OF_MONTH))
            assertEquals(pickerDate, TaskCalculations.localDayToDatePickerMillis(localDate))
        } finally {
            TimeZone.setDefault(previousTimeZone)
        }
    }
}
