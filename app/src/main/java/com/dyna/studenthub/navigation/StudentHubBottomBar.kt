package com.dyna.studenthub.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyna.studenthub.domain.StudentHubDestination
import com.dyna.studenthub.ui.theme.StudyBlue

@Composable
fun StudentHubBottomBar(
    currentRoute: String?,
    onDestinationSelected: (StudentHubDestination) -> Unit,
) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        StudentHubDestination.entries.filter { it != StudentHubDestination.COURSES }.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(destination.title) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = StudyBlue.copy(alpha = 0.14f),
                ),
            )
        }
    }
}
