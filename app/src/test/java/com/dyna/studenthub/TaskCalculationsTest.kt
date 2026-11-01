package com.dyna.studenthub

import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.domain.TaskCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
