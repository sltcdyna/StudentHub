package com.dyna.studenthub.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dyna.studenthub.domain.StudentHubDestination
import androidx.compose.material3.MaterialTheme

@Composable
fun StudentHubBottomBar(
    currentRoute: String?,
    onDestinationSelected: (StudentHubDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .98f),
        tonalElevation = 2.dp,
    ) {
        listOf(
            StudentHubDestination.HOME,
            StudentHubDestination.STUDIES,
            StudentHubDestination.MONEY,
            StudentHubDestination.MEALS,
            StudentHubDestination.PROFILE,
        ).forEach { destination ->
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
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
