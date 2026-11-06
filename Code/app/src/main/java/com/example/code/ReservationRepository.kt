package com.example.code

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* US 01.06.01 As an entrant, I want to view a list of my active reservations
US 01.06.02 As an entrant, I want to view a list of my past reservations

Reads and writes the "reservations" collection. A reservation is made by a lottery winner and is
cancelled rather than deleted, so it still shows under past reservations
*/

private const val TAG = "ReservationRepository"

/**
 * Creates, loads and cancels reservations in Firestore
 *
 * @param db the Firestore instance to use
 */
class ReservationRepository(db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val reservationsRef = db.collection("reservations")

    /**
     * Saves a reservation for a lottery winner
     *
     * @param entrant the winner making the reservation
     * @param lotteryId id of the lottery they won
     * @param campsite the campsite being booked
     * @param startDate first night, as UTC midnight millis from the date picker
     * @param endDate last night, as UTC midnight millis from the date picker
     * @return the saved reservation
     * @throws IllegalArgumentException if the entrant or lottery is missing
     */
    suspend fun reserve(
        entrant: User,
        lotteryId: String,
        campsite: Campsite,
        startDate: Long,
        endDate: Long
    ): Reservation {
        require(entrant.deviceId.isNotEmpty() && lotteryId.isNotEmpty()) {
            "Missing entrant or lottery"
        }
        val id = reservationId(lotteryId, entrant.deviceId)
        val reservation = Reservation(
            id = id,
            lotteryId = lotteryId,
            campsiteId = campsite.id,
            campsiteName = campsite.name,
            deviceId = entrant.deviceId,
            startDate = startDate,
            endDate = endDate,
            createdAt = System.currentTimeMillis()
        )
        reservationsRef.document(id).set(reservation).await()
        return reservation
    }

    /**
     * Loads one reservation
     *
     * @param id the reservation's id
     * @return the reservation, or null if it doesn't exist
     */
    suspend fun loadReservation(id: String): Reservation? {
        if (id.isEmpty()) return null
        val doc = reservationsRef.document(id).get().await()
        return runCatching { doc.toObject(Reservation::class.java) }
            .onFailure { Log.e(TAG, "Could not read reservation $id", it) }
            .getOrNull()
            ?.copy(id = doc.id)
    }

    /**
     * Loads every reservation an entrant has made, including cancelled ones
     *
     * @param deviceId device id of the entrant
     * @return their reservations, empty if the device id is empty
     */
    suspend fun myReservations(deviceId: String): List<Reservation> {
        if (deviceId.isEmpty()) return emptyList()
        return reservationsRef.whereEqualTo("deviceId", deviceId).get().await()
            .documents
            .mapNotNull { doc ->
                runCatching { doc.toObject(Reservation::class.java) }
                    .onFailure { Log.e(TAG, "Skipped reservation ${doc.id}", it) }
                    .getOrNull()
                    ?.copy(id = doc.id)
            }
    }

    /**
     * Cancels a reservation. It stays in Firestore marked as cancelled
     *
     * @param id the reservation's id
     */
    suspend fun cancel(id: String) {
        reservationsRef.document(id).update("cancelled", true).await()
    }
}