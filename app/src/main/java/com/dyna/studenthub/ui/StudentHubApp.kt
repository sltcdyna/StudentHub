package com.dyna.studenthub.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.domain.StudentHubThemeChoice
import androidx.navigation.compose.rememberNavController
import com.dyna.studenthub.di.AppContainer
import com.dyna.studenthub.navigation.StudentHubNavHost
import com.dyna.studenthub.ui.screen.LocalCompanionId
import com.dyna.studenthub.ui.theme.StudentHubTheme
import com.dyna.studenthub.viewmodel.ProfileViewModel

@Composable
fun StudentHubApp() {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.uiState.collectAsState()
    val view = LocalView.current
    val selectedTheme = StudentHubThemeChoice.fromId(profileState.themeId)
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = selectedTheme != StudentHubThemeChoice.NIGHT
            isAppearanceLightNavigationBars = selectedTheme != StudentHubThemeChoice.NIGHT
        }
    }
    remember { AppContainer.repository }
    StudentHubTheme(selectedTheme) {
        CompositionLocalProvider(
            LocalContentColor provides androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
            LocalCompanionId provides profileState.companionId,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                StudentHubNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
