package com.example.code

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* US 01.01.06 As an entrant, I want to join an active lottery from the campsite details screen
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery

Read only lookups an entrant's screens need from the "lotteries", "waitlist" and "results"
collections. Joining, leaving and drawing stay in CampsiteWaitlist and LotteryDraw
*/

private const val TAG = "LotteryRepository"

/**
 * Looks up lotteries and an entrant's place in them
 *
 * @param db the Firestore instance to use
 */
class LotteryRepository(db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val lotteriesRef = db.collection("lotteries")
    private val waitlistRef = db.collection("waitlist")
    private val resultsRef = db.collection("results")

    /**
     * Loads every lottery that includes a campsite
     *
     * @param campsiteId id of the campsite
     * @return the lotteries, empty if the campsite isn't in any
     */
    suspend fun lotteriesForCampsite(campsiteId: String): List<Lottery> {
        if (campsiteId.isEmpty()) return emptyList()
        return lotteriesRef.whereArrayContains("campsites", campsiteId).get().await()
            .documents
            .mapNotNull { toLottery(it) }
    }

    /**
     * Loads lotteries by id. Missing ids are skipped
     *
     * @param ids ids of the lotteries to load
     * @return the lotteries that exist
     */
    suspend fun loadLotteries(ids: Collection<String>): List<Lottery> {
        val wanted = ids.filter { it.isNotEmpty() }.distinct()
        // Firestore limits how many ids one "whereIn" query can take, so ask in groups of 10
        return wanted.chunked(10).flatMap { chunk ->
            lotteriesRef.whereIn(FieldPath.documentId(), chunk).get().await()
                .documents
                .mapNotNull { toLottery(it) }
        }
    }

    /**
     * Loads every waitlist entry the entrant has, i.e. every lottery they've entered
     *
     * @param deviceId device id of the entrant
     * @return their entries, empty if the device id is empty
     */
    suspend fun myEntries(deviceId: String): List<WaitlistEntry> {
        if (deviceId.isEmpty()) return emptyList()
        return waitlistRef.whereEqualTo("deviceId", deviceId).get().await()
            .documents
            .mapNotNull { runCatching { it.toObject(WaitlistEntry::class.java) }.getOrNull() }
    }

    /**
     * Loads every draw result the entrant has
     *
     * @param deviceId device id of the entrant
     * @return their results, empty if the device id is empty
     */
    suspend fun myResults(deviceId: String): List<LotteryResult> {
        if (deviceId.isEmpty()) return emptyList()
        return resultsRef.whereEqualTo("deviceId", deviceId).get().await()
            .documents
            .mapNotNull { runCatching { it.toObject(LotteryResult::class.java) }.getOrNull() }
    }

    private fun toLottery(doc: DocumentSnapshot): Lottery? {
        return runCatching { doc.toObject(Lottery::class.java) }
            .onFailure { Log.e(TAG, "Skipped lottery ${doc.id}", it) }
            .getOrNull()
    }
}