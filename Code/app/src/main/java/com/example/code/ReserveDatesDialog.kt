package com.example.code

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.coroutines.cancellation.CancellationException

/* US 01.06.01 As an entrant, I want to view a list of my active reservations

The date picker a lottery winner uses to choose their nights. Loads the nights already booked at
the campsite first so they are greyed out
*/

/**
 * Lets a winner pick the nights to reserve at a campsite. Booked nights can't be picked, and stays
 * longer than MAX_STAY_NIGHTS or spanning a booked night can't be confirmed
 *
 * @param campsiteId id of the campsite being booked
 * @param onConfirm called with the first and last night (UTC midnight millis) when OK is tapped
 * @param onDismiss called when the dialog is closed without picking
 */
@Composable
fun ReserveDatesDialog(
    campsiteId: String,
    onConfirm: (startDate: Long, endDate: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val reservations = remember { ReservationRepository() }
    var booked by remember { mutableStateOf<Set<Long>?>(null) }

    LaunchedEffect(campsiteId) {
        booked = try {
            reservations.bookedNights(campsiteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptySet()
        }
    }

    val loaded = booked
    if (loaded == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            text = { Text("Loading available nights...") }
        )
    } else {
        CampsiteDateRangeDialog(
            startDate = null,
            endDate = null,
            unavailableDates = loaded,
            checkStay = true,
            title = "Pick your nights (up to $MAX_STAY_NIGHTS)",
            onConfirm = { start, end -> if (start != null) onConfirm(start, end ?: start) },
            onDismiss = onDismiss
        )
    }
}