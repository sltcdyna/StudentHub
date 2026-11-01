package com.dyna.studenthub.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

enum class StudentHubDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    HOME("home", "Accueil", Icons.Rounded.Home),
    STUDIES("studies", "Études", Icons.Rounded.MenuBook),
    MONEY("money", "Argent", Icons.Rounded.AccountBalanceWallet),
    MEALS("meals", "Repas", Icons.Rounded.Restaurant),
    PROFILE("profile", "Profil", Icons.Rounded.Person),
    COURSES("courses", "Courses", Icons.Rounded.ShoppingCart),
}
