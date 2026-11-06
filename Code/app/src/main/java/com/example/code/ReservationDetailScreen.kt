package com.example.code

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/* US 01.06.01 As an entrant, I want to view a list of my active reservations
US 01.06.02 As an entrant, I want to view a list of my past reservations

Details of one reservation: the campsite, the booked nights on a calendar, and a Cancel
Reservation button that only shows while the reservation is still active

Not built yet: the View Messages button from the design
*/

// TODO: move into Color.kt
private val CancelSalmon = Color(0xFFD9A08B)

/**
 * Shows one reservation
 *
 * @param nav the app's main nav controller
 * @param reservationId id of the reservation to show
 */
@Composable
fun ReservationDetailScreen(nav: NavController, reservationId: String) {
    val reservationsRepo = remember { ReservationRepository() }
    val campsitesRepo = remember { CampsiteRepository() }
    val scope = rememberCoroutineScope()
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        try {
            val loaded = reservationsRepo.loadReservation(reservationId)
            reservation = loaded
            campsite = loaded?.let { campsitesRepo.loadCampsiteNow(it.campsiteId) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Could not load the reservation"
        }
        loading = false
    }

    Scaffold(
        containerColor = Color.Transparent, // let the background image show through
        bottomBar = { BottomNavBar(selected = "home", navCont = nav) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val loaded = reservation
            val currentError = error
            when {
                loading -> CircularProgressIndicator(color = col.CampsiteCardOpen)
                currentError != null -> Text(currentError, color = Color(0xFFFF8A80))
                loaded == null -> Text("Reservation not found", color = Color.White)
                else -> ReservationDetailsCard(
                    reservation = loaded,
                    campsite = campsite,
                    canCancel = isReservationActive(loaded, todayUtcMillis()) && !busy,
                    onBack = { nav.popBackStack() },
                    onCancel = { showCancelConfirm = true }
                )
            }
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Cancel this reservation?") },
            text = { Text("Your spot will be given up. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelConfirm = false
                    val current = reservation ?: return@TextButton
                    scope.launch {
                        busy = true
                        try {
                            reservationsRepo.cancel(current.id)
                            reservation = current.copy(cancelled = true)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = e.message ?: "Could not cancel the reservation"
                        }
                        busy = false
                    }
                }) { Text("Cancel Reservation") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text("Keep It") }
            }
        )
    }
}

/**
 * The rounded card for a reservation
 *
 * @param reservation the reservation to show
 * @param campsite the booked campsite, or null if it couldn't be loaded
 * @param canCancel true shows the Cancel Reservation button
 * @param onBack called when the back button is tapped
 * @param onCancel called when Cancel Reservation is tapped
 */
@Composable
private fun ReservationDetailsCard(
    reservation: Reservation,
    campsite: Campsite?,
    canCancel: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(col.UnfocusedInputContainer)
        ) {
            Text(
                text = campsite?.name ?: reservation.campsiteName,
                color = col.CardText,
                fontSize = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
            Image(
                painter = campsiteImage(campsite ?: Campsite(name = reservation.campsiteName)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your dates: ${formatDateRange(reservation.startDate, reservation.endDate)}",
                    color = col.CardText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (reservation.cancelled) {
                    Text("This reservation was cancelled", color = col.CardText, fontSize = 14.sp)
                }
                campsite?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Location: ${it.location}", color = col.CardText, fontSize = 14.sp)
                    Text(
                        text = "Capacity: Space for ${it.maxOccupancy} campers",
                        color = col.CardText,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                BookedDatesCalendar(reservation.startDate, reservation.endDate)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = col.CampsiteCardWaitlist),
                        modifier = Modifier.width(56.dp).height(36.dp)
                    ) {
                        Text("↩", color = col.CardText, fontSize = 18.sp)
                    }
                    if (canCancel) {
                        Button(
                            onClick = onCancel,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CancelSalmon),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("CANCEL RESERVATION", color = col.CardText, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * A calendar with the booked nights highlighted. It's read only: every touch is swallowed before
 * the calendar sees it, so the dates can't be changed here
 *
 * @param startDate first night, as UTC midnight millis
 * @param endDate last night, as UTC midnight millis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookedDatesCalendar(startDate: Long, endDate: Long) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate,
        initialDisplayedMonthMillis = startDate
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)     // the calendar scrolls, so it needs a fixed height in this column
            .clip(RoundedCornerShape(16.dp))
            .background(col.FocusedInputContainer)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        DateRangePicker(
            state = state,
            title = {},
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = col.FocusedInputContainer,
                selectedDayContainerColor = col.SubmitButton,
                dayInSelectionRangeContainerColor = col.CampsiteCardOpen,
                todayDateBorderColor = col.SubmitButton
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}