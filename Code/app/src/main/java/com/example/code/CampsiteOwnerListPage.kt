package com.example.code


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CampsiteOwnerListPage(nav: NavController) {
    val repo = remember { CampsiteRepository() }

    DisposableEffect(Unit) {
        repo.listenToOwner(logged_in_device)
        onDispose { repo.stopListening() }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text("Your Campsites", color = Color.White, fontSize = 26.sp)

            if (repo.myCampsites.isEmpty()) {
                Text(
                    "You have no campsites yet",
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(repo.myCampsites) { campsite ->
                    Card(
                        onClick = { nav.navigate("ownerTools/${campsite.id}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
                    ) {
                        Text(
                            campsite.name,
                            color = col.HomeButtonText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }
        BottomNavBar("campOwnerHome", nav)
    }
}