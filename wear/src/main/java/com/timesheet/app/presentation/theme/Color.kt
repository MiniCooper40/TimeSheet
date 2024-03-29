package com.timesheet.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
val Black = Color(0xFF1C1C1C)
val Grey = Color(0xFFDADDD8)
val Cream = Color(0xFFECEBE4)
val OffWhite = Color(0xFFEEF0F2)
val White = Color(0xFFFAFAFF)

internal val wearColorPalette: Colors = Colors(
    primary = Black,
    primaryVariant = Color.Black,
    secondary = OffWhite,
    secondaryVariant = White,
    background = Cream,
    error = Color.Red,
    onPrimary = White,
    onSecondary = Black,
    onError = Color.Red
)