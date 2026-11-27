package com.example.code.ui.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.col
import com.example.code.logged_in_device
import com.example.code.repositories.LotteryRepository
import com.example.code.rules.SentSummary
import com.example.code.rules.summarizeSent
import com.example.code.services.NotificationService
import com.example.code.ui.components.NotificationCard
import com.example.code.ui.navigation.BottomNavBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun OwnerSentNotificationsScreen(navController: NavController, campsiteId: String) {
    val service = remember { NotificationService() }
    val lotteries = remember { LotteryRepository() }
    val format = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
    var sent by remember { mutableStateOf<List<SentSummary>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(campsiteId) {
        try {
            val ids = lotteries.lotteriesForCampsite(campsiteId).map { it.id }.toSet()
            sent = summarizeSent(service.getSentBy(logged_in_device).filter { it.lotteryId in ids })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Could not load sent notifications"
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavBar(selected = "campOwnerHome", navCont = navController) }
    ) { innerPadding ->
        val loaded = sent
        val currentError = error
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(56.dp))
                Text("Notifications Sent", color = Color.White, fontSize = 32.sp)
                Spacer(Modifier.height(24.dp))
            }
            when {
                currentError != null -> item { Text(currentError, color = Color(0xFFFF8A80)) }
                loaded == null -> item { CircularProgressIndicator(color = col.CampsiteCardOpen) }
                loaded.isEmpty() -> item { Text("You haven't sent any notifications", color = Color.White.copy(alpha = 0.7f)) }
                else -> items(loaded, key = { it.batchId }) { s ->
                    val delivered = s.recipients - s.optedOut
                    NotificationCard(
                        title = s.content,
                        body = "Sent to $delivered entrants" +
                                if (s.optedOut > 0) " (${s.optedOut} opted out)" else "",
                        time = format.format(Date(s.timestamp))
                    )
                }
            }
        }
    }
}