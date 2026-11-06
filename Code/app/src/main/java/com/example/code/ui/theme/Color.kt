package com.example.code.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

//Main App Palette
val PaleGreen = Color(0xFF97D1A0)
val LightGreen = Color(0xFF8ABF93)
val MediumGreen = Color(0xFF86A98C)
val MossGreen = Color(0xFF6F9877)
val DarkGreen = Color(0xFF2D6837)
val DeepGreen = Color(0xFF1B2E1F)


object AppColors {
    // Input Defaults
    val UnfocusedInputContainer = MediumGreen
    val FocusedInputContainer = LightGreen
    val FocusedInputLabel = PaleGreen

    // Buttons
    val SubmitButton = DarkGreen
    val FilterButton = MediumGreen

    // Modal Bottom Sheet
    val BottomSheetBackground = MediumGreen

    // Campsite List
    val CardText = DeepGreen
    val CampsiteCardOpen = LightGreen      // lottery still has space
    val CampsiteCardWaitlist = MossGreen   // lottery full, waiting list only
    val NavBar = LightGreen

    // Home Screen Buttons/Texts
    val HomeButton = Color(0xFF8FB996)
    val HomeButtonText = Color(0xFF1F2D24)
}