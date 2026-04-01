package com.example.todolist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.example.todolist.R

val Cormorant = FontFamily(
    Font(R.font.cormorant_variable, FontWeight.Normal),
    Font(R.font.cormorant_variable, FontWeight.SemiBold)
)

val AppTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = InkDark
    ),
    bodyMedium = TextStyle(
        fontFamily = Cormorant,
        fontSize = 15.sp,
        color = InkSoft
    )
)