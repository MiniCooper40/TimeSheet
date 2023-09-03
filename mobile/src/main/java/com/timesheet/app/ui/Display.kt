package com.timesheet.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

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
fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
