package com.example.code.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.col
import com.example.code.model.Notification
import com.example.code.model.NotificationType
import com.example.code.rules.notificationTitle
import com.example.code.services.AdminService
import com.example.code.services.NotificationService
import com.example.code.services.SYSTEM_SENDER
import com.example.code.ui.components.NotificationCard
import com.example.code.ui.navigation.BottomNavBar
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminNotificationsLogScreen(navController: NavController) {
    val service = remember { NotificationService() }
    val format = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
    var log by remember { mutableStateOf<List<Notification>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf<NotificationType?>(null) }

    LaunchedEffect(Unit) {
        try {
            log = service.getAllNotifications()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavBar(selected = "adminHome", navCont = navController) }
    ) { innerPadding ->
        val shown = log?.filter{ filter == null || it.type == filter }
        val currentError = error
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(56.dp))
                Text("Notifications Log", color = Color.White, fontSize = 32.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()).padding(vertical = 12.dp)
                ) {
                    FilterChip(
                        selected = filter == null,
                        onClick = { filter = null },
                        label = { Text("All") }
                    )
                    NotificationType.entries.forEach { type ->
                        FilterChip(
                            selected = filter == type,
                            onClick = { filter = type },
                            label = { Text(type.name.lowercase().replace('_', ' ')) }
                        )
                    }
                }
            }

            when {
                currentError != null -> item { Text(currentError, color = Color(0xFFFF8A80)) }
                shown == null -> item { CircularProgressIndicator(color = col.CampsiteCardOpen) }
                shown.isEmpty() -> item { Text("No notifications", color = Color.White.copy(alpha = 0.7f)) }
                else -> items(shown, key = { it.id }) { n ->
                    val sender = if (n.senderId == SYSTEM_SENDER) "System" else n.senderId
                    NotificationCard(
                        title = notificationTitle(n.type) + if (n.suppressed) " (opted out)" else "",
                        body = "From: $sender\nTo: ${n.recipientId}\n${n.content}",
                        time = format.format(Date(n.timestamp))
                    )
                }
            }
        }
    }
}