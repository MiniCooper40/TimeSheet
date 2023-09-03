package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.data.repository.PreferenceKeys
import com.timesheet.app.data.repository.WeeklyStrategy
import com.timesheet.app.view.model.HomePageViewModel
import com.timesheet.app.view.model.PreferencesViewModel
import java.time.DayOfWeek
import java.util.Locale
import kotlin.math.exp

@Composable
fun PreferenceRow(title: String, content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(title)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

fun dayOfWeekToString(dayOfWeek: DayOfWeek) = when(dayOfWeek) {
    DayOfWeek.MONDAY -> "Monday"
    DayOfWeek.TUESDAY -> "Tuesday"
    DayOfWeek.WEDNESDAY -> "Wednesday"
    DayOfWeek.THURSDAY -> "Thursday"
    DayOfWeek.FRIDAY -> "Friday"
    DayOfWeek.SATURDAY -> "Saturday"
    DayOfWeek.SUNDAY -> "Sunday"
}

fun capitalizeFirst(value: String): String {
    if(value.isEmpty()) return value
    if(value.length == 1) return value.uppercase()

    val stringBuilder = StringBuilder()
    stringBuilder.append(value[0].uppercase())
    stringBuilder.append(value.substring(1, value.length).lowercase())
    return stringBuilder.toString()
}


@Composable
fun Preferences() {

    val preferencesViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory)
    
    val preferences by preferencesViewModel.preferences.collectAsState(initial = emptyPreferences())

    val weeklyStrategy = preferences[PreferenceKeys.weeklyStrategyKey]
    val weeklyCycleStartDay = preferences[PreferenceKeys.weeklyCycleStartDayKey]

    var expanded by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Section(title = "Weekly data") {
            weeklyStrategy?.let {
                PreferenceRow("Strategy") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            capitalizeFirst(
                                it
                            )
                        )
                        IconButton(onClick = { expanded = "weeklyCycleStrategy" }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                "Drop down"
                            )
                        }
                    }
                    DropdownMenu(expanded = expanded == "weeklyCycleStrategy", onDismissRequest = {
                        expanded = ""
                    }) {
                        WeeklyStrategy.values().map {weeklyCycleStrategy ->
                            DropdownMenuItem(onClick = {
                                expanded = ""
                                preferencesViewModel.setWeeklyCycleStrategy(weeklyCycleStrategy)
                            }) {
                                Text(weeklyCycleStrategy.value)
                            }
                        }
                    }
                }
            }
            println("weeklyStrategy $weeklyStrategy")
            if(weeklyStrategy == "CYCLE") {
                weeklyCycleStartDay?.let {
                    PreferenceRow("Starts on") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                capitalizeFirst(
                                    DayOfWeek.of(it).toString()
                                )
                            )
                            IconButton(onClick = { expanded = "dayOfWeek" }) {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    "Drop down"
                                )
                            }
                        }
                        DropdownMenu(expanded = expanded == "dayOfWeek", onDismissRequest = {
                            expanded = ""
                        }) {
                            DayOfWeek.values().map {dayOfWeek ->
                                DropdownMenuItem(onClick = {
                                    expanded = ""
                                    preferencesViewModel.setWeeklyCycleStartDay(dayOfWeek)
                                }) {
                                    Text(dayOfWeek.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}