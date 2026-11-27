package com.example.code.ui.entrant

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
import com.example.code.model.Notification
import com.example.code.model.NotificationType
import com.example.code.rules.notificationTitle
import com.example.code.services.NotificationService
import com.example.code.ui.components.NotificationCard
import com.example.code.ui.navigation.BottomNavBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(navController: NavController) {
    val service = remember { NotificationService() }
    val scope = rememberCoroutineScope()
    val format = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    var items by remember { mutableStateOf<List<Notification>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(logged_in_device) {
        val registration = service.listenToNotifications(
            deviceId = logged_in_device,
            onChange = { items = it; error = null },
            onError = { error = it },
        )
        onDispose { registration.remove() }
    }

    fun open(n: Notification) {
        if (!n.read) scope.launch { runCatching { service.markRead(n.id) } }
        when (n.type) {
            NotificationType.LOTTERY_WON ->
                if (n.campsiteId.isNotEmpty()) navController.navigate("campsite/${n.campsiteId}")
            NotificationType.RESERVATION_CONFIRMED, NotificationType.RESERVATION_CANCELLED ->
                if (n.reservationId.isNotEmpty()) navController.navigate("reservation/${n.reservationId}")
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavBar(selected = "notifications", navCont = navController) }
    ) { innerPadding ->
        val loaded = items
        val currentError = error
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(56.dp))
                Text("Notifications", color = Color.White, fontSize = 32.sp)
                Spacer(Modifier.height(24.dp))
            }
            when {
                currentError != null -> item { Text(currentError, color = Color(0xFFFF8A80)) }
                loaded == null -> item { CircularProgressIndicator(color = col.CampsiteCardOpen) }
                loaded.isEmpty() -> item { Text("No notifications yet", color = Color.White.copy(alpha = 0.7f)) }
                else -> items(loaded, key = { it.id }) { n ->
                    NotificationCard(
                        title = notificationTitle(n.type),
                        body = n.content,
                        time = format.format(Date(n.timestamp)),
                        unread = !n.read,
                        onClick = { open(n) }
                    )
                }
            }
        }
    }
}