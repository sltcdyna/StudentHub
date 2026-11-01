package com.dyna.studenthub.viewmodel

import androidx.lifecycle.ViewModel
import com.dyna.studenthub.data.StudentHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StudentHubUiState(
    val appName: String = "StudentHub",
)

class StudentHubViewModel(repository: StudentHubRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentHubUiState(repository.appName()))
    val uiState: StateFlow<StudentHubUiState> = _uiState.asStateFlow()
}
