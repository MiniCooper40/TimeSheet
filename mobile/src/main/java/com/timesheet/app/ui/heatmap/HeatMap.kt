package com.timesheet.app.ui.heatmap

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.RichTooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColor
import com.patrykandpatrick.vico.core.extension.copyColor
import com.timesheet.app.presentation.theme.Black
import com.timesheet.app.presentation.theme.Grey
import com.timesheet.app.presentation.theme.White
import com.timesheet.app.ui.toCompressedTimeStamp
import kotlinx.coroutines.launch
import java.time.Duration

data class CalenderDay(
    val dayOfMonth: Int,
    val duration: Duration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMap(heatMapDetails: HeatMapDetails, modifier: Modifier = Modifier) {

    val elements = heatMapDetails.elements

    Log.v("HEATMAP", elements.toString())

    val scope = rememberCoroutineScope()

    var maxValue = elements.flatten().maxOfOrNull { it.duration.toMillis() } ?: 1
    if(maxValue == 0L) maxValue = 1

    Log.v("max", maxValue.toString())

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        elements.mapIndexed {index, week ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                week.map {day ->
                        if(day.dayOfMonth != 0) {
                            val toolTipState = remember{ PlainTooltipState() }
                            PlainTooltipBox(
                                tooltip = {
                                    Text(toCompressedTimeStamp(day.duration.toMillis()), color = White)
                                },
                                tooltipState = toolTipState,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clickable { scope.launch { toolTipState.show() } }
                                        .padding(all = 3.dp),
                                    elevation = 0.dp,
                                    contentColor = Black,
                                    border = BorderStroke(1.dp, SolidColor(Black)),//BorderStroke(width = 1.dp, color = Black),
                                    backgroundColor = Color(ColorUtils.blendARGB(Color.White.toArgb(), Color.DarkGray.toArgb(),day.duration.toMillis() / maxValue.toFloat()).copyColor(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            day.dayOfMonth.toString(), color = Black,
                                            modifier = Modifier
                                                .padding(all = 0.dp),

                                            )
                                    }
                                }
                            }
                        }
                        else {
                            Card(
                                modifier = Modifier
                                    .size(45.dp)
                                    .padding(all = 3.dp),
                                elevation = 0.dp,
                                contentColor = Black,
                                border = BorderStroke(0.dp, Color.Transparent),//BorderStroke(width = 1.dp, color = Black),
                                backgroundColor = Color.Transparent
                            ) {

                            }
                        }
                    }
                }
            }
        }
}