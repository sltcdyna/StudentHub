package com.dyna.studenthub.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dyna.studenthub.di.AppContainer
import com.dyna.studenthub.navigation.StudentHubNavHost

@Composable
fun StudentHubApp() {
    val navController = rememberNavController()
    remember { AppContainer.repository }
    Box(modifier = Modifier.fillMaxSize()) {
        StudentHubNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
