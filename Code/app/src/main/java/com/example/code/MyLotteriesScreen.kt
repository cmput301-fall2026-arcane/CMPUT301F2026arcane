package com.example.code

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/* US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery

"Your Active Lotteries and Waitlists" page. Lists every lottery the entrant has entered. Winners
can reserve straight from here, entrants can leave before the draw, and tapping a name opens the
campsite
*/

// TODO: move into Color.kt
private val WinnerGreen = Color(0xFF63E07F)

/**
 * Shows the lotteries the signed in entrant has entered
 *
 * @param nav the app's main nav controller
 */
@Composable
fun MyLotteriesScreen(nav: NavController) {
    val entrantLotteries = remember { EntrantLotteries() }
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<MyLotteryRow>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }    // bump to reload the list
    var leaving by remember { mutableStateOf<MyLotteryRow?>(null) }
    var reserving by remember { mutableStateOf<MyLotteryRow?>(null) }

    LaunchedEffect(refreshKey) {
        try {
            rows = entrantLotteries.myLotteries(currentEntrant())
            error = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Could not load your lotteries"
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // let the background image show through
        bottomBar = { BottomNavBar(selected = "home", navCont = nav) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = "Your Active Lotteries\nand Waitlists",
                color = Color.White,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            message?.let {
                Text(it, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
            }

            val loaded = rows
            val currentError = error
            when {
                currentError != null -> Text(currentError, color = Color(0xFFFF8A80))
                loaded == null -> CircularProgressIndicator(color = col.CampsiteCardOpen)
                loaded.isEmpty() -> {
                    Text("You haven't entered any lotteries yet", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { nav.navigate("campsites") },
                        colors = ButtonDefaults.buttonColors(containerColor = col.SubmitButton)
                    ) {
                        Text("Browse Campsites")
                    }
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(loaded) { row ->
                        MyLotteryCard(
                            row = row,
                            onOpen = { row.campsite?.let { nav.navigate("campsite/${it.id}") } },
                            onAction = {
                                message = null
                                when (row.status) {
                                    EntrantLotteryStatus.JOINED -> leaving = row
                                    EntrantLotteryStatus.WON -> reserving = row
                                    EntrantLotteryStatus.RESERVED ->
                                        row.reservation?.let { nav.navigate("reservation/${it.id}") }
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    leaving?.let { row ->
        AlertDialog(
            onDismissRequest = { leaving = null },
            title = { Text("Leave this lottery?") },
            text = { Text("You'll lose your spot in ${row.campsite?.name ?: "this lottery"}.") },
            confirmButton = {
                TextButton(onClick = {
                    leaving = null
                    scope.launch {
                        message = leaveMessage(entrantLotteries.leave(row.lottery, currentEntrant()))
                        refreshKey++
                    }
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { leaving = null }) { Text("Stay") }
            }
        )
    }

    reserving?.let { row ->
        CampsiteDateRangeDialog(
            startDate = null,
            endDate = null,
            onConfirm = { start, end ->
                reserving = null
                val site = row.campsite
                if (site != null && start != null) {
                    scope.launch {
                        try {
                            val reservation = entrantLotteries.reserve(
                                currentEntrant(), row.lottery, site, start, end ?: start
                            )
                            nav.navigate("reservation/${reservation.id}")
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            message = e.message ?: "Could not make the reservation"
                            refreshKey++
                        }
                    }
                } else if (site == null) {
                    message = "Could not load this campsite"
                }
            },
            onDismiss = { reserving = null }
        )
    }
}

/**
 * One lottery row: campsite name on the left, action button on the right. Winners are bright
 * green, lotteries waiting on the draw are teal, the rest are the usual card green
 *
 * @param row the lottery to show
 * @param onOpen called when the name is tapped
 * @param onAction called when the button is tapped
 */
@Composable
private fun MyLotteryCard(row: MyLotteryRow, onOpen: () -> Unit, onAction: () -> Unit) {
    val cardColor = when (row.status) {
        EntrantLotteryStatus.WON -> WinnerGreen
        EntrantLotteryStatus.JOINED -> col.CampsiteCardWaitlist
        else -> col.CampsiteCardOpen
    }
    val enabled = canPressLotteryButton(row.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardColor)
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.campsite?.name ?: "Unknown campsite",
            color = col.CardText,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAction,
            enabled = enabled,
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.12f),
                disabledContainerColor = Color.Black.copy(alpha = 0.06f)
            ),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = lotteryButtonLabel(row.status),
                color = col.CardText.copy(alpha = if (enabled) 1f else 0.6f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}