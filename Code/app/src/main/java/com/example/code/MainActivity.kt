package com.example.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.code.ui.theme.AppColors
import com.example.code.ui.theme.CodeTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

val col = AppColors

lateinit var users : UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val db = FirebaseFirestore.getInstance()
        users = UserRepository(db)

        enableEdgeToEdge()
        setContent {
            CodeTheme {
                ArcaneApp()
            }
        }
    }
}