package com.timesheet.app.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavAction
import androidx.navigation.NavArgument
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.DisplayTrackers
import com.timesheet.app.ui.GroupDetails
import com.timesheet.app.ui.HomePage
import com.timesheet.app.ui.Preferences
import com.timesheet.app.ui.TrackerDetails
import com.timesheet.app.ui.TrackerForm
import com.timesheet.app.ui.table.CreateGroup
import com.timesheet.app.ui.table.CreateTracker
import com.timesheet.app.ui.table.EditGroup
import com.timesheet.app.ui.table.EditTracker
import com.timesheet.app.ui.table.TrackerForm
import com.timesheet.app.view.model.TimeSheetViewModel
import java.util.Arrays


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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val uiState by timeSheetViewModel.timeTrackers.collectAsState()
    val trackers = uiState.trackers
    val groupsWithTrackers by timeSheetViewModel.trackerGroups.collectAsState()

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
                this.inclusive = true
            }

            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToTracker(uid: Int) {
        navController.navigate("tracker/$uid") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
                this.inclusive = true
            }

            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToGroup(uid: Int) {
        navController.navigate("group/$uid") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
                this.inclusive = true
            }

            launchSingleTop = true
            restoreState = false
        }
    }

    fun goBack() = navController.navigateUp()

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

                        IconButton(onClick = { goBack() }) {
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
                    contentColor = wearColorPalette.onPrimary,
                    modifier = Modifier
                        .background(wearColorPalette.primary)
                ) {
                    BottomNavigationItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = { navigateTo("home") },
                        icon = {
                            Icon(Icons.Default.Home, "Home button")
                        },
                        modifier = Modifier
                            .background(wearColorPalette.primary)
                    )

                    FloatingActionButton(
                        onClick = { navigateTo("create") },
                        modifier = Modifier
                            .background(wearColorPalette.primary)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Add button",
                            tint = wearColorPalette.primary
                        )
                    }

                    if (navBackStackEntry != null) {
                        BottomNavigationItem(
                            selected = currentDestination?.hierarchy?.any { it.route == "list" } == true,
                            onClick = { navigateTo("list") },
                            icon = {
                                Icon(
                                    Icons.Default.List,
                                    "List button",
                                    tint = wearColorPalette.onPrimary
                                )
                            },
                            modifier = Modifier
                                .background(wearColorPalette.primary)
                        )
                    }
                }
            }
        ) { it ->
            NavHost(navController, startDestination = "create", Modifier.padding(it)) {
                composable("home") {
                    HomePage(
                        groups = groupsWithTrackers,
                        navigateTo = { navigateToGroup(it) },
                        deleteGroups = { timeSheetViewModel.deleteGroupsByUid(it) }
                    )
                }
                composable("list") { backStack ->
                    DisplayTrackers(
                        trackers = trackers,
                        timeSheetViewModel = timeSheetViewModel,
                        navigateTo = { uid -> navigateToTracker((uid)) },
                        createTrackerWithIds = {
                            navigateTo (
                                "group/create-selected/${it.joinToString(",")}"
                            )
                        },
                        deleteTrackers = { timeSheetViewModel.deleteTrackersByUid(it) }
                    )
                }
                composable("create") {
                    TrackerForm(
                        navigateToGroupForm = { navigateTo("group/create") },
                        navigateToTrackerForm = { navigateTo("tracker/create") }
                    )
                }
                composable("tracker/create") { CreateTracker(timeSheetViewModel) { goBack() } }
                composable("group/create") { CreateGroup(timeSheetViewModel) { goBack() } }
                composable(
                    "group/create-selected/{trackerUids}",
                    arguments = listOf(navArgument("trackerUids") { type = NavType.StringType })
                ) {
//                    Log.v("Arguments?", navController.previousBackStackEntry?.arguments?.getIntArray("trackerUids")?.toString() ?: "Noo")

                    it.arguments?.getString("trackerUids")?.let { stringifiedTrackerUids ->
                        val trackerUids = stringifiedTrackerUids.split(",").map { it.toIntOrNull() }.filterNotNull().toIntArray()
                        CreateGroup(timeSheetViewModel, trackerUids) { goBack() }
                    }
                }
                composable(
                    "group/edit/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.IntType })
                ) {
                    it.arguments?.getInt("uid")?.let { uid ->
                        EditGroup(timeSheetViewModel, trackers, uid) {
                            navigateTo("home")
                        }
                    }
                }
                composable(
                    "tracker/edit/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.IntType })
                ) {
                    it.arguments?.getInt("uid")?.let { uid ->
                        EditTracker(uid) {
                            navController.popBackStack()
                            navController.popBackStack()
                            navigateTo("list")
                        }
                    }
                }
                composable(
                    "tracker/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.IntType })
                ) {
                    it.arguments?.getInt("uid")?.let { uid ->
                        TrackerDetails(
                            uid = uid,
                            editTracker = { navigateTo("tracker/edit/$uid") }
                        )
                    }
                }
                composable(
                    "group/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.IntType })
                ) {
                    it.arguments?.getInt("uid")?.let { uid ->
                        GroupDetails(
                            uid = uid,
                            navigateToTracker = { trackerUid ->
                                navigateToTracker(trackerUid)
                            },
                            editGroup = {
                                navigateTo("group/edit/$uid")
                            }
                        )
                    }
                }
                composable("preferences") {
                    Preferences()
                }
            }
        }
    }
}