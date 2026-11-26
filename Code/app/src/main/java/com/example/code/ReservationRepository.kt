package com.example.code

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
class ReservationRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val reservationsRef = db.collection("reservations")
    private val nightsRef = db.collection("bookedNights")

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
        validateStay(startDate, endDate, emptySet())?.let { throw IllegalArgumentException(it) }

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
        val reservationRef = reservationsRef.document(id)
        val nights = nightsOf(startDate, endDate)
        val nightRefs = nights.map { nightsRef.document(bookedNightId(campsite.id, it)) }

        db.runTransaction<Unit> { transaction ->
            val taken = nightRefs.map { transaction.get(it) }.any { it.exists() }
            val alreadyReserved = transaction.get(reservationRef).exists()

            if (taken) {
                throw FirebaseFirestoreException(
                    "Some of those nights were just booked. Please pick different nights",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            if (alreadyReserved) {
                throw FirebaseFirestoreException(
                    "You've already made a reservation",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            transaction.set(reservationRef, reservation)
            nights.zip(nightRefs).forEach { (night, ref) ->
                transaction.set(
                    ref,
                    mapOf("campsiteId" to campsite.id, "night" to night, "reservationId" to id)
                )
            }
        }.await()
        //reservationsRef.document(id).set(reservation).await()
        return reservation
    }

    /**
     * Loads every night already booked at a campsite, for greying them out in the date picker
     *
     * @param campsiteId id of the campsite
     * @return the booked nights, as UTC midnight millis
     */
    suspend fun bookedNights(campsiteId: String): Set<Long> {
        if (campsiteId.isEmpty()) return emptySet()
        return nightsRef.whereEqualTo("campsiteId", campsiteId).get().await()
            .documents
            .mapNotNull { it.getLong("night") }
            .toSet()
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
        val reservationRef = reservationsRef.document(id)
        db.runTransaction<Unit> { transaction ->
            val reservation = transaction.get(reservationRef).toObject(Reservation::class.java)
                ?: return@runTransaction

            val heldNights = nightsOf(reservation.startDate, reservation.endDate)
                .map { nightsRef.document(bookedNightId(reservation.campsiteId, it))}
                .filter { transaction.get(it).getString("reservationId") == id }
            transaction.update(reservationRef, "cancelled", true)
            heldNights.forEach { transaction.delete(it)}

        }.await()
    }
}