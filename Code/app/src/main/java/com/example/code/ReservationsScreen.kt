package com.example.code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.coroutines.cancellation.CancellationException

/* US 01.06.01 As an entrant, I want to view a list of my active reservations
US 01.06.02 As an entrant, I want to view a list of my past reservations

"Your Reservations" page. Active reservations on top, past and cancelled ones below. Tapping one
opens its details
*/

/**
 * Shows the signed in entrant's active and past reservations
 *
 * @param nav the app's main nav controller
 */
@Composable
fun ReservationsScreen(nav: NavController) {
    val reservationsRepo = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            reservations = reservationsRepo.myReservations(currentEntrant().deviceId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Could not load your reservations"
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // let the background image show through
        bottomBar = { BottomNavBar(selected = "home", navCont = nav) }
    ) { innerPadding ->
        val loaded = reservations
        val currentError = error
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(56.dp))
                Text("Your Reservations", color = Color.White, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }
            when {
                currentError != null -> item { Text(currentError, color = Color(0xFFFF8A80)) }
                loaded == null -> item { CircularProgressIndicator(color = col.CampsiteCardOpen) }
                else -> {
                    val (active, past) = splitReservations(loaded, todayUtcMillis())

                    item { SectionHeader("Active Reservations") }
                    if (active.isEmpty()) item { EmptyNote("No active reservations") }
                    items(active) { reservation ->
                        ReservationCard(reservation) { nav.navigate("reservation/${reservation.id}") }
                    }

                    item { SectionHeader("Past Reservations") }
                    if (past.isEmpty()) item { EmptyNote("No past reservations") }
                    items(past) { reservation ->
                        ReservationCard(reservation) { nav.navigate("reservation/${reservation.id}") }
                    }
                }
            }
        }
    }
}

/**
 * One reservation, drawn like a campsite card. Cancelled ones say so after the name
 */
@Composable
private fun ReservationCard(reservation: Reservation, onClick: () -> Unit) {
    val name = if (reservation.cancelled) {
        "${reservation.campsiteName} (cancelled)"
    } else {
        reservation.campsiteName
    }
    CampsiteCard(
        item = CampsiteListItem(Campsite(id = reservation.campsiteId, name = name)),
        onClick = onClick
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 20.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
}