package com.dyna.studenthub.data

interface StudentHubRepository {
    fun appName(): String
}

class InMemoryStudentHubRepository : StudentHubRepository {
    override fun appName(): String = "StudentHub"
}
