package com.timesheet.app.ui.table

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackerGroup
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.Circle
import com.timesheet.app.ui.LabeledEntry
import com.timesheet.app.ui.Section
import com.timesheet.app.ui.TimeSheetPopup
import com.timesheet.app.view.model.TimeSheetViewModel
import io.mhssn.colorpicker.ColorPicker
import io.mhssn.colorpicker.ColorPickerType


@Composable
fun FormTextInput(title: String, value: TextFieldValue, onChange: (TextFieldValue) -> Unit) {
    TextField(value = value, onValueChange = {
        onChange(it)
        Log.v("CHANGED", "changed to $it")
    }, label = { Text(title) }, enabled = true
    )
}

@Composable
fun Form(title: String = "Form", onSubmit: () -> Unit, content: @Composable () -> Unit) {

    Section(
        title = title
    ) {
        content()
        TextButton(onClick = onSubmit) {
            Text("Submit")
        }
    }
}

@Composable
fun FormPopup(
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
    title: String = "Form",
    content: @Composable () -> Unit
) {
    TimeSheetPopup(onDismissRequest = onDismissRequest) {
        Form(onSubmit = onSubmit, title = title) {
            content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorSelection(onSelectedColor: (Color) -> Unit) {
    ColorPicker(
        onPickedColor = onSelectedColor, type = ColorPickerType.SimpleRing()
    )
}

@Composable
fun ColorSelectionPopup(
    default: Color = Color.Gray,
    onDismissRequest: () -> Unit,
    onSelectedColor: (Color) -> Unit
) {

    var color by remember { mutableStateOf(default) }

    TimeSheetPopup(onDismissRequest = onDismissRequest) {
        Form(
            title = "Select color",
            onSubmit = {
                onDismissRequest()
                onSelectedColor(color)
            }) {
            ColorSelection {
                color = it
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected")
                SelectableColorPreview(color) { }
            }
        }
    }
}

enum class PopupType {
    ColorPicker, TrackerSelector
}

@Composable
fun SelectableColorPreview(color: Color, onClick: () -> Unit) {
    Canvas(modifier = Modifier
        .size(20.dp)
        .clickable { onClick() }) {
        drawRect(
            color
        )
    }
}


data class TrackerFormData(
    val title: String = "",
    val description: String = "",
    val color: Color = Color.Gray
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackerForm(
    default: TrackerFormData = TrackerFormData(),
    maxTitleLength: Int = 20,
    maxDescriptionLength: Int = 250,
    onSubmit: (TrackerFormData) -> Unit
) {

    var title by remember { mutableStateOf(default.title) }
    var color by remember { mutableStateOf(default.color) }
    var description by remember { mutableStateOf(default.description) }

    var popup: PopupType? by remember { mutableStateOf(null) }

    val titleWeight = 1f
    val contentWeight = 1.4f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Form(
            title = "Create tracker",
            onSubmit = {
                onSubmit(
                    TrackerFormData(
                        title = title,
                        description = description,
                        color = color
                    )
                )
            }) {
            LabeledEntry(
                "Title", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= maxTitleLength) title = it },
                    maxLines = 1,
                    singleLine = true
                )
            }

            LabeledEntry(
                "Description", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                OutlinedTextField(value = description, onValueChange = {
                    if (it.length <= maxDescriptionLength) description = it
                })
            }

            LabeledEntry(
                "Color", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                SelectableColorPreview(color) {
                    popup = PopupType.ColorPicker
                }
            }


        }
        when (popup) {
            PopupType.ColorPicker -> {
                ColorSelectionPopup(
                    onDismissRequest = { popup = null },
                    onSelectedColor = { color = it },
                    default = color
                )
            }

            else -> {

            }
        }
    }
}

@Composable
fun CreateTracker(context: Context = LocalContext.current, onComplete: () -> Unit) {

    val timeSheetViewModel: TimeSheetViewModel = viewModel(factory = TimeSheetViewModel.Factory)

    TrackerForm { form ->
        timeSheetViewModel.createTracker(
            context,
            TimeTracker(
                title = form.title,
                description = form.description,
                color = form.color.toArgb()
            )
        )
        onComplete()
    }
}

data class GroupFormData(
    val title: String = "",
    val description: String = "",
    val trackerIds: List<Int> = listOf(),
    val color: Color = Color.Gray
)

@Composable
fun MiniTrackerChip(modifier: Modifier = Modifier, tracker: TimeTracker, content: @Composable (() -> Unit)? = null) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        content?.let { it() }
        Text(tracker.title)
        Circle(size = 14.dp, color = Color(tracker.color))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackerSelector(
    trackers: List<TimeTracker>,
    selectedTrackerUIDs: Set<Int>,
    toggleSelectionOfTrackerUID: (Int) -> Unit
) {

    FlowRow {
        trackers.map { tracker ->
            MiniTrackerChip(tracker = tracker) {
                Checkbox(
                    checked = selectedTrackerUIDs.contains(tracker.uid),
                    onCheckedChange = { toggleSelectionOfTrackerUID(tracker.uid) }
                )
            }
        }
    }
}

@Composable
fun TrackerSelectionPopup(
    trackers: List<TimeTracker>,
    defaultSelectedUIDs: Set<Int>,
    onDismissRequest: () -> Unit,
    onSelectedTrackerUIDs: (Set<Int>) -> Unit
) {

    var selectedUIDs by remember { mutableStateOf(defaultSelectedUIDs) }

    FormPopup(
        onDismissRequest = onDismissRequest,
        onSubmit = {
            onDismissRequest()
            onSelectedTrackerUIDs(selectedUIDs)
        },
        title = "Select trackers"
    )
    {
        TrackerSelector(
            trackers = trackers,
            selectedTrackerUIDs = selectedUIDs
        ) { uid ->
            if (selectedUIDs.contains(uid)) selectedUIDs -= uid
            else selectedUIDs += uid
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedTrackerPreview(selectedTrackers: List<TimeTracker>, onClick: () -> Unit) {
//    Row {

    val textStyle = TextStyle.Default.copy(fontSize = 14.sp)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row {
            Text("Add", style = textStyle)
            IconButton(
                modifier = Modifier
                    .size(20.dp),
                onClick = onClick
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add"
                )
            }
        }

        FlowRow {

            selectedTrackers.map {
                Card(
                    backgroundColor = wearColorPalette.background,
                    border = BorderStroke(0.5.dp, wearColorPalette.primary),
                    modifier = Modifier
                        .padding(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(3.dp)
                    ) {
                        Text(
                            it.title,
                            style = textStyle
                        )
                        Circle(size = 12.dp, color = Color(it.color))
                    }
                }
            }
        }
    }
//    }
}

@Composable
fun GroupForm(
    default: GroupFormData = GroupFormData(),
    trackers: List<TimeTracker>,
    titleWeight: Float = 1f,
    contentWeight: Float = 1.5f,
    maxTitleLength: Int = 20,
    maxDescriptionLength: Int = 250,
    onSubmit: (GroupFormData) -> Unit
) {


    var popup: PopupType? by remember { mutableStateOf(null) }

    var selectedUIDs by remember { mutableStateOf(default.trackerIds.toSet()) }
    var title by remember { mutableStateOf(default.title) }
    var description by remember { mutableStateOf(default.description) }
    var color by remember { mutableStateOf(default.color) }

    Box(
        modifier = Modifier
            .padding(20.dp)
    ) {
        Form(
            title = "Create group",
            onSubmit = {
                onSubmit(
                    GroupFormData(
                        title = title,
                        description = description,
                        trackerIds = selectedUIDs.toList(),
                        color = color
                    )
                )
            }) {

            LabeledEntry(
                "Title", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                OutlinedTextField(
                    value = title,
                    maxLines = 1,
                    singleLine = true,
                    onValueChange = { if (it.length <= maxTitleLength) title = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = wearColorPalette.primary
                    )
                )
            }

            LabeledEntry(
                "Description", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                OutlinedTextField(value = description, onValueChange = {
                    if (it.length <= maxDescriptionLength) description = it
                })
            }

            LabeledEntry(
                "Color", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                SelectableColorPreview(color) {
                    popup = PopupType.ColorPicker
                }
            }

            LabeledEntry(
                "Trackers", titleWeight = titleWeight, contentWeight = contentWeight
            ) {
                SelectedTrackerPreview(trackers.filter { selectedUIDs.contains(it.uid) }) {
                    Log.v("CLICKED", "Clicked ADD")
                    popup = PopupType.TrackerSelector
                }
            }
        }
    }

    Log.v("POPUP IN GROUP", popup.toString())

    when (popup) {
        PopupType.TrackerSelector -> {
            TrackerSelectionPopup(
                trackers = trackers,
                defaultSelectedUIDs = selectedUIDs.toSet(),
                onDismissRequest = { popup = null },
                onSelectedTrackerUIDs = { selectedUIDs = it }
            )
        }

        PopupType.ColorPicker -> {
            ColorSelectionPopup(
                onDismissRequest = { popup = null },
                onSelectedColor = { color = it },
                default = color
            )
        }

        else -> {

        }
    }

}

@Composable
fun CreateGroup(onComplete: () -> Unit) {

    val timeSheetViewModel: TimeSheetViewModel = viewModel(factory = TimeSheetViewModel.Factory)

    val uiState by timeSheetViewModel.timeTrackers.collectAsState()
    val trackers = uiState.trackers

    GroupForm(
        trackers = trackers
    ) { form ->
        Log.v("GROUP CREATED", form.toString())
        val group = TrackerGroup(
            title = form.title,
            description = form.description,
            color = form.color.toArgb()
        )

        val trackersInGroup = trackers.filter { form.trackerIds.contains(it.uid) }

        timeSheetViewModel.addGroupWithTrackers(
            group,
            trackersInGroup
        )

        onComplete()
    }
}