package com.example.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.code.model.Role
import com.example.code.repositories.CampsiteRepository
import com.example.code.repositories.UserRepository
import com.example.code.ui.navigation.ArcaneApp
import com.example.code.ui.theme.AppColors
import com.example.code.ui.theme.CodeTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

val col = AppColors

lateinit var users : UserRepository
lateinit var sites : CampsiteRepository
// this is set by login/signup and then read by screens that depend on user type
var logged_user_type : Role = Role.ENTRANT // just defaulted for now
var logged_in_device : String = ""
var logged_in_name : String = ""
var logged_in_email : String = ""
var logged_in_phone : String = ""

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