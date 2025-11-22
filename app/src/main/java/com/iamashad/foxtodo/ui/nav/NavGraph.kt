package com.iamashad.foxtodo.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iamashad.foxtodo.ui.add.AddTaskScreen
import com.iamashad.foxtodo.ui.add.AddTaskViewModel
import com.iamashad.foxtodo.ui.calendar.CalendarScreen
import com.iamashad.foxtodo.ui.calendar.CalendarViewModel
import com.iamashad.foxtodo.ui.detail.DetailScreen
import com.iamashad.foxtodo.ui.detail.DetailViewModel
import com.iamashad.foxtodo.ui.home.HomeScreen
import com.iamashad.foxtodo.ui.home.HomeViewModel
import com.iamashad.foxtodo.ui.profile.ProfileScreen
import com.iamashad.foxtodo.ui.profile.ProfileViewModel
import com.iamashad.foxtodo.ui.theme.ThemeViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    themeViewModel: ThemeViewModel,
    startDestination: String = NavItem.Home.route
) {

    val navController = rememberNavController()
    val enterSpec = tween<Float>(durationMillis = 220)
    val exitSpec = tween<Float>(durationMillis = 160)

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavItem.Home.route) { backStackEntry ->
                val vm: HomeViewModel = hiltViewModel(backStackEntry)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    HomeScreen(
                        viewModel = vm,
                        onOpenTask = { id -> navController.navigate(detailRoute(id)) },
                        onAddTask = { navController.navigate("add") }
                    )
                }
            }

            composable("add") { backStackEntry ->
                val vm: AddTaskViewModel = hiltViewModel(backStackEntry)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    AddTaskScreen(
                        viewModel = vm,
                        onSaved = { _ -> navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }

            composable(NavItem.Calendar.route) { backStackEntry ->
                val vm: CalendarViewModel = hiltViewModel(backStackEntry)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    CalendarScreen(
                        viewModel = vm,
                        onOpenTask = { id -> navController.navigate(detailRoute(id)) }
                    )
                }
            }

            composable(NavItem.Profile.route) { backStackEntry ->
                val vm: ProfileViewModel = hiltViewModel(backStackEntry)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    ProfileScreen(
                        viewModel = vm,
                        themeViewModel = themeViewModel,
                        onSignOut = {}
                    )
                }
            }

            composable(
                route = "detail/{taskId}",
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val vm: DetailViewModel = hiltViewModel(backStackEntry)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    DetailScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onDeleted = { navController.popBackStack() }
                    )
                }
            }

            composable(
                "add?date={dateEpoch}",
                arguments = listOf(
                    navArgument("dateEpoch") {
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStack ->
                val dateEpoch = backStack.arguments?.getString("dateEpoch")?.toLongOrNull()
                val vm: AddTaskViewModel = hiltViewModel(backStack)
                dateEpoch?.let {
                    val d = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    vm.updateDueDate(d)
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = enterSpec),
                    exit = fadeOut(animationSpec = exitSpec)
                ) {
                    AddTaskScreen(
                        viewModel = vm,
                        onSaved = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

fun detailRoute(taskId: Long) = "detail/$taskId"
