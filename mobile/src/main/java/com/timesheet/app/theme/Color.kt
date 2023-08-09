package com.timesheet.app.presentation.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
val Black = Color(0xFF1C1C1C)
val Grey = Color(0xFFDADDD8)
val Cream = Color(0xFFECEBE4)
val OffWhite = Color(0xFFEEF0F2)
val White = Color(0xFFFAFAFF)

internal val wearColorPalette: Colors = Colors(
    primary = Black,
    primaryVariant = Color.Black,
    secondary = White,
    secondaryVariant = Cream,
    background = Cream,
    error = Color.Red,
    onPrimary = White,
    onSecondary = Black,
    onBackground = Black,
    isLight = false,
    onError = Color.Red,
    surface = Black,
    onSurface = Black
)