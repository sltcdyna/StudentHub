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
import com.dyna.studenthub.ui.screen.StudentHubScreen
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
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            StudentHubBottomBar(
                currentRoute = backStackEntry?.destination?.route,
                onDestinationSelected = { destination ->
                    if (destination.route == StudentHubDestination.HOME.route) {
                        navController.popBackStack(StudentHubDestination.HOME.route, false)
                    } else {
                        navController.navigate(destination.route) {
                            popUpTo(StudentHubDestination.HOME.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
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
                            onStudiesClick = { navController.navigate(StudentHubDestination.STUDIES.route) },
                            onMoneyClick = { navController.navigate(StudentHubDestination.MONEY.route) },
                            onMealsClick = { navController.navigate(StudentHubDestination.MEALS.route) },
                        )
                        StudentHubDestination.MONEY -> MoneyScreen()
                        StudentHubDestination.MEALS -> MealsScreen(onCoursesClick = { navController.navigate(StudentHubDestination.COURSES.route) })
                        StudentHubDestination.COURSES -> CoursesScreen()
                        StudentHubDestination.STUDIES -> StudiesScreen()
                        StudentHubDestination.PROFILE -> ProfileScreen()
                        else -> StudentHubScreen(title = destination.title)
                    }
                }
            }
        }
    }
}
