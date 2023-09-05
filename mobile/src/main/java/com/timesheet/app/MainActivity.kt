package com.timesheet.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.DisplayTrackers
import com.timesheet.app.ui.HomePage
import com.timesheet.app.ui.Preferences
import com.timesheet.app.ui.TrackerDetails
import com.timesheet.app.ui.TrackerForm
import com.timesheet.app.ui.table.CreateGroup
import com.timesheet.app.ui.table.CreateTracker
import com.timesheet.app.ui.table.TrackerForm
import com.timesheet.app.view.model.TimeSheetViewModel


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(timeSheetViewModel: TimeSheetViewModel) {

    val navController = rememberNavController()

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

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

    TimeSheetTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = wearColorPalette.primary,
                        navigationIconContentColor = wearColorPalette.onPrimary,
                        actionIconContentColor = wearColorPalette.onPrimary,
                        titleContentColor = wearColorPalette.onPrimary,
                        scrolledContainerColor = wearColorPalette.primary
                    ),
                    title = {
                        Text(
                            "TimeSheet",
                            color = wearColorPalette.onPrimary
                        )
                    },
                    actions = {
                        IconButton(onClick = { navigateTo("preferences") }) {
                            Icon(
                                Icons.Filled.Settings,
                                "Settings",
                                tint = wearColorPalette.onPrimary
                            )
                        }
                    },
                    navigationIcon = {

                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                "BACK",
                                tint = wearColorPalette.onPrimary
                            )
                        }

                    },
                )
            },
            bottomBar = {
                BottomAppBar(
                    backgroundColor = wearColorPalette.primary,
                    contentColor = wearColorPalette.onPrimary
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    BottomNavigationItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = { navigateTo("home") },
                        icon = {
                            Icon(Icons.Default.Home, "Home button")
                        }
                    )

                    FloatingActionButton(onClick = { navigateTo("create") }) {
                        Icon(Icons.Default.Add, "Add button")
                    }

                    BottomNavigationItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "list" } == true,
                        onClick = { navigateTo("list") },
                        icon = {
                            Icon(
                                Icons.Default.List,
                                "List button",
                                tint = wearColorPalette.onPrimary
                            )
                        }
                    )
                }
            }
        ) { it ->
            NavHost(navController, startDestination = "home", Modifier.padding(it)) {
                composable("home") { HomePage(navigateTo = { uid -> navigateToTracker((uid)) }) }
                composable("list") {
                    DisplayTrackers(
                        timeSheetViewModel = timeSheetViewModel,
                        navigateTo = { uid -> navigateToTracker((uid)) })
                }
                composable("create") {
                    TrackerForm(
                        timeSheetViewModel = timeSheetViewModel,
                        navigateToGroupForm = { navigateTo("create/group") },
                        navigateToTrackerForm = { navigateTo("create/tracker") }
                    )
                }
                composable("create/tracker") { CreateTracker() }
                composable("create/group") { CreateGroup() }
                composable(
                    "tracker/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.IntType })
                ) {
                    it.arguments?.getInt("uid")?.let { uid -> TrackerDetails(uid = uid) }
                }
                composable("preferences") {
                    Preferences()
                }
            }
        }
    }
}