package com.example.arcane

import android.R.attr.prompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun StartScreens(mainNav: NavController) {
    val localNavCon = rememberNavController()

    NavHost(navController = localNavCon, startDestination = "title") {
        composable("title") { TitleScreen(localNavCon) }
        composable("login") { LoginScreen(mainNav, localNavCon) }
        composable("signup") { SignUpScreen(mainNav, localNavCon) }
    }
}

@Composable
fun TitleScreen(localNav: NavController) {
    val titleLogo = painterResource(R.drawable.faketitlelogo)
    var prompt by remember { mutableStateOf("Click anywhere to continue.") }
    var flash = true

    LaunchedEffect(Unit) {
        while (flash) {
            prompt = "Click anywhere to continue"
            delay(0.5.seconds)
            prompt = ""
            delay(0.5.seconds)
        }
    }

    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = titleLogo,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            alpha = 1.0F
        )
        Spacer(modifier = Modifier.height(350.dp))
        Text(
            text = prompt,
            fontSize = 12.sp,
            color = Color.White
        )
    }

    // Button is transparent and covers the entire screen
    // a click anywhere sends user to the login screen
    Button (
        onClick = {
            flash = false
            localNav.navigate("login")
        },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RectangleShape
    ) {

    }
}

@Composable
fun LoginScreen(mainNav: NavController, localNav: NavController) {

}

@Composable
fun SignUpScreen(mainNav: NavController, localNav: NavController) {

}