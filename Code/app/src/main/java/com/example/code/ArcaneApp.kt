package com.example.code

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ArcaneApp() {
    val backgroundImg = painterResource(R.drawable.backgroundimg)
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Image(
            painter = backgroundImg,
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5F
        )
    }

    val mainNavCon = rememberNavController()

    NavHost(navController = mainNavCon, startDestination = "start") {
        composable("start") { StartScreens(mainNavCon) }
        composable("home") { HomeScreen(mainNavCon) }
    }
}