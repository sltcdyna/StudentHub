package com.dyna.studenthub.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dyna.studenthub.domain.StudentHubDestination
import com.dyna.studenthub.ui.screen.MoneyScreen
import com.dyna.studenthub.ui.screen.MealsScreen
import com.dyna.studenthub.ui.screen.CoursesScreen
import com.dyna.studenthub.ui.screen.StudiesScreen
import com.dyna.studenthub.ui.screen.HomeScreen
import com.dyna.studenthub.ui.screen.ProfileScreen

@Composable
fun StudentHubNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    fun navigateTo(destination: StudentHubDestination) {
        if (navController.currentDestination?.route == destination.route) return
        navController.navigate(destination.route) {
            // Keep Home as the single root destination. This prevents a restored
            // tab stack from trapping the user in Studies (or another section).
            popUpTo(StudentHubDestination.HOME.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            StudentHubBottomBar(
                currentRoute = backStackEntry?.destination?.route.let { route ->
                    if (route == StudentHubDestination.COURSES.route) StudentHubDestination.MEALS.route else route
                },
                onDestinationSelected = ::navigateTo,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = StudentHubDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            StudentHubDestination.entries.forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        StudentHubDestination.HOME -> HomeScreen(
                            onStudiesClick = { navigateTo(StudentHubDestination.STUDIES) },
                            onMoneyClick = { navigateTo(StudentHubDestination.MONEY) },
                            onMealsClick = { navigateTo(StudentHubDestination.MEALS) },
                            onProfileClick = { navigateTo(StudentHubDestination.PROFILE) },
                        )
                        StudentHubDestination.MONEY -> MoneyScreen()
                        StudentHubDestination.MEALS -> MealsScreen(onCoursesClick = { navigateTo(StudentHubDestination.COURSES) })
                        StudentHubDestination.COURSES -> CoursesScreen()
                        StudentHubDestination.STUDIES -> StudiesScreen()
                        StudentHubDestination.PROFILE -> ProfileScreen()
                    }
                }
            }
        }
    }
}
