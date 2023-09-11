package com.timesheet.app.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
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
fun Circle(size: Dp, color: Int) {
    Circle(
        size,
        Color(color)
    )
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
fun VerticalScrollArea(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}

@Composable
fun AlertDialogButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(text, color = wearColorPalette.background)
    }
}

@Composable
fun SelectionBar(
    actions: List<Pair<ImageVector, () -> Unit>>,
    textButton: Pair<String, () -> Unit>? = null
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(wearColorPalette.primary),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            textButton?.let { (text, action) ->
                TextButton(onClick = action) {
                    Text(text, color = wearColorPalette.onPrimary)
                }
            }
            Row {
                actions.map { (icon, action) ->
                    IconButton(onClick = action) {
                        Icon(
                            icon,
                            "Icon",
                            tint = wearColorPalette.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSheetAlert(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Alert",
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Dismiss",
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            AlertDialogButton(confirmLabel) {
                onConfirm()
            }
        },
        dismissButton = {
            AlertDialogButton(dismissLabel) {
                onDismissRequest()
            }
        },
        text = content,
        title = { Text(title) }
    )
}

@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: String,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
        }

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
