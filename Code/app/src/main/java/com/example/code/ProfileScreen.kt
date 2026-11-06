package com.example.code

import android.R.attr.onClick
import android.R.attr.top
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(mainNav: NavController) {
    Card(
        modifier = Modifier.height(800.dp)
            .padding(top=70.dp)
            .padding(horizontal=10.dp)
            .padding(bottom=25.dp),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(25.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Spacer(modifier = Modifier.height(110.dp)) // replace with profile image
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = logged_in_name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = logged_in_email,
                    fontSize = 30.sp
                )
                Text(
                    text = logged_in_phone,
                    fontSize = 30.sp
                )
                Row {

                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomNavBar("adminHome", mainNav)
    }
}