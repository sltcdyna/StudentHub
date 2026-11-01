package com.dyna.studenthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dyna.studenthub.ui.StudentHubApp
import com.dyna.studenthub.ui.theme.StudentHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentHubTheme {
                StudentHubApp()
            }
        }
    }
}
