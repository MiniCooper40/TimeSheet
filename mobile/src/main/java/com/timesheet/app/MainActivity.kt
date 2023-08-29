package com.timesheet.app.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.data.model.TrackedTimes
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.ui.BottomBar
import com.timesheet.app.ui.DisplayTrackers
import com.timesheet.app.ui.HomePage
import com.timesheet.app.ui.TrackerDetails
import com.timesheet.app.ui.TrackerForm
import com.timesheet.app.view.TimeSheetViewModel
import com.timesheet.app.view.TimeTrackerViewModel
import java.util.Date


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val timeSheetViewModel: TimeSheetViewModel by viewModels { TimeSheetViewModel.Factory }

        setContent {
            TimeSheetTheme {
                MainApp(timeSheetViewModel)
            }
        }
    }
}
@Composable
fun MainApp(timeSheetViewModel: TimeSheetViewModel) {

    val navController = rememberNavController()

    fun navigateToTracker(uid: Int) {
       navController.navigate(
           "tracker/$uid",
           navOptions = NavOptions
            .Builder()
            .setExitAnim(0)
            .setExitAnim(0)
            .setPopEnterAnim(0)
            .setPopExitAnim(0)
            .build()
        )
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                BottomNavigationItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Home, "Home button")
                    }
                )

                FloatingActionButton(onClick = {
                    navController.navigate("create") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                  Icon(Icons.Default.Add, "Add button")
                }

                BottomNavigationItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "list" } == true,
                    onClick = {
                        navController.navigate("list") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.List, "List button")
                    }
                )
            }
        }
    ) { it ->
        NavHost(navController, startDestination = "home", Modifier.padding(it)) {
            composable("home") { HomePage(navigateTo = { uid -> navigateToTracker((uid)) }) }
            composable("list") { DisplayTrackers(timeSheetViewModel = timeSheetViewModel, navigateTo = { uid -> navigateToTracker((uid)) } ) }
            composable("create") { TrackerForm(timeSheetViewModel = timeSheetViewModel) }
            composable("tracker/{uid}", arguments = listOf(navArgument("uid") { type = NavType.IntType })) {
                it.arguments?.getInt("uid")?.let { uid -> TrackerDetails(uid = uid) }
            }
        }
    }
}