package com.example.code

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import kotlin.random.Random

/**
 * A lottery listing registered by an owner. A lottery can contain multiple campsites
 *
 * @param id Firestore doc id
 * @param campsites list of campsite ids
 * @param numWinners number of winners
 * @param openDate date the lottery opens
 * @param closeDate date the lottery closes
 */
data class Lottery(
    @DocumentId val id: String = "",
    val campsites: List<String> = emptyList(),
    val numWinners: Int = 1,
    val openDate: Long = System.currentTimeMillis(),
    val closeDate: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
    val bookingOpenDate: Long = 0L,
    val bookingCloseDate: Long = 0L,
) {
    val createdAt: Long = System.currentTimeMillis()
    val winners: List<String> = emptyList()
    val drawnAt: Long? = null

    val isDrawn: Boolean get() = drawnAt != null
}