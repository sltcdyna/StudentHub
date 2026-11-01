package com.dyna.studenthub.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskRepository
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.domain.TaskCalculations
import com.dyna.studenthub.domain.ProfilePreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dyna.studenthub.notification.TaskReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudiesUiState(
    val tasks: List<Task> = emptyList(),
    val remindersEnabled: Boolean = false,
    val remindersMessage: String? = null,
)

@HiltViewModel
class StudiesViewModel @Inject constructor(
    private val repository: TaskRepository,
    @ApplicationContext private val context: Context,
    private val preferences: DataStore<Preferences>,
) : ViewModel() {
    private val reminders = preferences.data.map { it[ProfilePreferences.remindersEnabled] ?: false }
    val uiState: StateFlow<StudiesUiState> = combine(repository.observeAll(), reminders) { tasks, enabled ->
        StudiesUiState(TaskCalculations.sorted(tasks), enabled, if (!enabled) "Rappels désactivés" else null)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudiesUiState())

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.edit { it[ProfilePreferences.remindersEnabled] = enabled } }
    }

    fun addTask(type: TaskType, subject: String, title: String, dueDate: Long) = viewModelScope.launch(Dispatchers.IO) {
        if (subject.isBlank() || title.isBlank() || dueDate <= 0L) return@launch
        val task = Task(type = type, subject = subject.trim(), title = title.trim(), dueDate = dueDate, done = false)
        val id = repository.insert(task)
        if (preferences.data.map { it[ProfilePreferences.remindersEnabled] ?: false }.first()) {
            scheduleReminder(task.copy(id = id))
        }
    }

    fun setDone(task: Task, done: Boolean) = viewModelScope.launch(Dispatchers.IO) { repository.setDone(task, done) }

    private fun scheduleReminder(task: Task) {
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(Data.Builder()
                .putLong(TaskReminderWorker.KEY_ID, task.id)
                .putString(TaskReminderWorker.KEY_TITLE, task.title)
                .putString(TaskReminderWorker.KEY_TYPE, if (task.type == TaskType.EXAM) "examen" else "devoir")
                .build())
            .setInitialDelay(TaskCalculations.reminderDelay(task.dueDate, System.currentTimeMillis()), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("task-reminder-${task.id}", ExistingWorkPolicy.REPLACE, request)
    }
}
