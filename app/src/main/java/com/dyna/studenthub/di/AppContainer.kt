package com.dyna.studenthub.di

import com.dyna.studenthub.data.InMemoryStudentHubRepository
import com.dyna.studenthub.data.StudentHubRepository

object AppContainer {
    val repository: StudentHubRepository by lazy { InMemoryStudentHubRepository() }
}
