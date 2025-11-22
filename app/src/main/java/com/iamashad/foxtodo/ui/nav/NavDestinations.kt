package com.iamashad.foxtodo.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavItem("home", "Home", Icons.Default.Home)
    object Calendar : NavItem("calendar", "Calendar", Icons.Default.DateRange)
    object Profile  : NavItem("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    NavItem.Home,
    NavItem.Calendar,
    NavItem.Profile
)
