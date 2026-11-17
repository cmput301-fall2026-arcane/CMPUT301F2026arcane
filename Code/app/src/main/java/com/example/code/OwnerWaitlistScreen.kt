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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerWaitlistScreen(nav: NavController, campsiteId: String) {
    val lotteries = remember { LotteryRepository() }
    val waitlist = remember { CampsiteWaitlist() }
    var hasLottery by remember { mutableStateOf(true) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // find the newest lottery for this campsite, then listen to its waitlist
    LaunchedEffect(campsiteId) {
        val lottery = runCatching { lotteries.lotteriesForCampsite(campsiteId) }
            .getOrDefault(emptyList())
            .maxByOrNull { it.createdAt }
        if (lottery == null) {
            hasLottery = false
        } else {
            waitlist.listenToLottery(lottery.id)
        }
    }

    DisposableEffect(Unit) {
        onDispose { waitlist.stopListening() }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text("Waiting List", color = Color.White, fontSize = 26.sp)

            if (!hasLottery) {
                Text("This campsite has no lottery", color = Color.White, modifier = Modifier.padding(top = 16.dp))
            } else {
                Text("${waitlist.entries.size} entrants", color = Color.White, modifier = Modifier.padding(top = 8.dp))

                if (waitlist.entries.isEmpty()) {
                    Text("No one has joined the waitlist yet", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                }

                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(waitlist.entries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = col.HomeButton)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(entry.name, color = col.HomeButtonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Joined ${dateFormat.format(Date(entry.joinedAt))}", color = col.HomeButtonText)
                            }
                        }
                    }
                }
            }
        }
        BottomNavBar("campOwnerHome", nav)
    }
}