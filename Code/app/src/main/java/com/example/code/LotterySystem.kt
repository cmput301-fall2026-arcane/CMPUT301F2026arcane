package com.example.code

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.get
import kotlin.random.Random

/**
 * Lottery class to manage lottery runs.
 *
 * @param db the Firestore instance to use
 */
class LotterySystem(
    val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val waitlistRef = db.collection("waitlist")
    private val lotteriesRef = db.collection("lotteries")

    /**
     * Randomly selects up to numWinners distinct entrants from the waitlist.
     * If there are fewer entrants than winners, everyone wins
     *
     * @param lottery the lottery being drawn
     * @param entries the waitlist entries for the lottery
     * @param random source of randomness, replaceable in tests
     *
     * @return the winning entries
     */
    fun drawWinners(
        lottery: Lottery,
        entries: List<WaitlistEntry>,
        random: Random = Random.Default
    ): List<WaitlistEntry> {
        return entries
            .filter { it.lotteryId == lottery.id }
            .distinctBy { it.deviceId }
            .shuffled(random)
            .take(lottery.numWinners)
    }

    /**
     * Loads the lottery's waitlist, draws the winners and saves the result
     * Fetches waitlist using lottery id
     * If the lottery has already been drawn, throws an exception
     *
     * @param lottery the lottery to run
     *
     * @throws IllegalStateException if the lottery has already been drawn
     *
     * @return list of winner user ids
     */
    suspend fun runLottery(
        lottery: Lottery,
    ) : List<String> {
        val waitlist = waitlistRef.whereEqualTo("lotteryId", lottery.id).get().await()
        val entries = waitlist.documents.mapNotNull { it.toObject(WaitlistEntry::class.java) }
        val winners = drawWinners(lottery, entries).map { it.deviceId }

        db.runTransaction { transaction ->
            val current = transaction.get(lotteriesRef.document(lottery.id))
            if (current.getLong("drawnAt") != null) {
                throw IllegalStateException("Lottery has already been drawn")
            }
            transaction.update(
                lotteriesRef.document(lottery.id),
                mapOf(
                    "winners" to winners,
                    "drawnAt" to System.currentTimeMillis()
                )
            )
        }.await()

        return winners
    }
}