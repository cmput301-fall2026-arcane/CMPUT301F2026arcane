package com.example.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.code.ui.theme.AppColors
import com.example.code.ui.theme.CodeTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

val col = AppColors

lateinit var users : UserRepository
lateinit var sites : CampsiteRepository
// this is set by login/signup and then read by screens that depend on user type
var logged_user_type : Role = Role.ENTRANT // just defaulted for now
var logged_in_device : String = ""
var logged_in_name : String = ""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)


        val db = FirebaseFirestore.getInstance()
        users = UserRepository(db)
        sites = CampsiteRepository(db)

        enableEdgeToEdge()
        setContent {
            CodeTheme {
                ArcaneApp()
            }
        }
    }
}