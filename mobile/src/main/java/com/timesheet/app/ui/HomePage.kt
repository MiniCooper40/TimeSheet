package com.timesheet.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.ui.pie.Legend
import com.timesheet.app.ui.pie.PieChart
import com.timesheet.app.ui.pie.PieChartSlice
import com.timesheet.app.ui.table.Table
import com.timesheet.app.view.model.HomePageViewModel
import java.util.Random


@Composable
fun HomePage(navigateTo: (Int) -> Unit) {

    val homePageViewModel: HomePageViewModel = viewModel(factory = HomePageViewModel.Factory)

    val state by homePageViewModel.lastWeekData.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        val rnd = Random()

        val data = state.tracked.map { tracked ->
            PieChartSlice(
                Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)),
                tracked.duration.toMillis(),
                tracked.timeTracker.title,
                uid = tracked.timeTracker.uid
            ) {
                Text(tracked.timeTracker.title)
                Text(
                    toCompressedTimeStamp(tracked.duration.toMillis()),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    "${
                        String.format(
                            "%.2f",
                            tracked.percentage
                        )
                    }%. ${tracked.sessions} session${if (tracked.sessions == 1) "" else "s"}."
                )
            }
        }

        Sections(
            mapOf(
                1 to Section(
                    icon = Icons.Default.DateRange,
                    title = "Table"
                ) {
                    Table(
                        chartData = state,
                        navigateTo = { navigateTo(it) },
                        sortBy = { homePageViewModel.sortWeeklyBy(it) })
                },
                2 to Section(
                    icon = Icons.Default.AccountCircle,
                    title = "Pie Chart"
                ) {
                    PieChart(data)
                    Legend(data.map { it.color to it.title })
                }
            )
        )
    }
}