package com.timesheet.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.timesheet.app.theme.wearColorPalette

@Composable
fun EvenlySpacedRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
fun LabeledEntry(
    title: String,
    titleWeight: Float = 1f,
    contentWeight: Float = 1f,
    content: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        Box(
            modifier = Modifier.weight(titleWeight),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(title)
        }
        Box(
            modifier = Modifier.weight(contentWeight),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

@Composable
fun Circle(size: Dp, color: Color) {
    Canvas(
        modifier = Modifier.size(size)
    ) {
        drawCircle(color)
    }
}

@Composable
fun TimeSheetPopup(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true
        )
    ) {
        Card(
            backgroundColor = wearColorPalette.secondaryVariant,
            elevation = 4.dp,
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
}


@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        content()
    }
}

data class Section(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@Composable
fun Sections(sections: Map<Any, Section>) {
    var section by remember {
        mutableStateOf(
            if (sections.isEmpty()) null else sections.keys.first()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            sections.map {
                IconButton(
                    onClick = { section = it.key },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (section?.equals(it.key) == true) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent
                        )
                ) {
                    Icon(
                        it.value.icon,
                        "Button"
                    )
                }
            }
        }
        section?.let {
            val currentSection = sections[section]
            currentSection?.let { it.content() }
        }
    }
}
