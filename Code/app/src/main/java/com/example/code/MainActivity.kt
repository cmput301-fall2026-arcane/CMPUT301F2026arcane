package com.example.arcane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.arcane.ui.theme.ArcaneTheme
import com.example.code.ui.theme.CodeTheme

val col = AppColors()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeTheme {
                ArcaneApp()
            }
        }
    }
}