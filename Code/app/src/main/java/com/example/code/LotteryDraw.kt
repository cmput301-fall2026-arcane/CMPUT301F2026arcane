package com.example.code

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* US 02.05.01 As a campsite owner, I want the system to automatically draw winners after the
registration period ends

Runs the draw once the sign up period has closed. There is no server in this project, so this is
called whenever a lottery is shown and does nothing until the draw is due. LotterySystem picks the
winners, this writes a result for every entrant so they can all be told the outcome
*/

/**
 * The outcome of a draw
 */
sealed class DrawOutcome {
    data class Success(val winners: List<LotteryResult>, val entrants: Int) : DrawOutcome()
    object NoEntrants : DrawOutcome()
    object NotDue : DrawOutcome()
    object AlreadyDrawn : DrawOutcome()
    data class Failure(val message: String) : DrawOutcome()
}

/**
 * Draws a lottery when it is due and stores the result for every entrant
 *
 * @param db the Firestore instance to use
 * @param lotteries the lottery system that picks the winners
 */
class LotteryDraw (
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val lotteries: LotterySystem = LotterySystem(db)
) {
    private val waitlistRef = db.collection("waitlist")
    private val resultsRef = db.collection("results")
    private val lotteriesRef = db.collection("lotteries")

    /**
     * Draws the lottery if its sign up period has closed and it has not been drawn already. Safe
     * to call every time a lottery is shown
     *
     * @param lottery the lottery to draw
     * @return the outcome of the draw
     */
    suspend fun runDrawIfDue(lottery: Lottery): DrawOutcome {
        if (lottery.id.isEmpty()) return DrawOutcome.Failure("Missing lottery")

        return try {
            val document = lotteriesRef.document(lottery.id).get().await()
            if (!document.exists()) return DrawOutcome.Failure("Lottery not found")
            if (document.getLong("drawnAt") != null) return DrawOutcome.AlreadyDrawn

            val now = System.currentTimeMillis()
            if (!isDrawDue(now, lottery.closeDate, false)) return DrawOutcome.NotDue

            val entries = waitlistRef.whereEqualTo("lotteryId", lottery.id).get().await()
                .documents
                .mapNotNull { it.toObject(WaitlistEntry::class.java) }
                .distinctBy { it.deviceId }

            val winnerIds = lotteries.runLottery(lottery)

            if (entries.isEmpty()) return DrawOutcome.NoEntrants

            val results = buildResults(lottery.id, entries, winnerIds, now)
            val batch = db.batch()
            for (result in results) {
                batch.set(resultsRef.document(resultId(lottery.id, result.deviceId)), result)
            }
            batch.commit().await()

            DrawOutcome.Success(results.filter { it.won }, entries.size)
        } catch (e: IllegalStateException) {
            DrawOutcome.AlreadyDrawn
        } catch (e: Exception) {
            DrawOutcome.Failure(e.message ?: "Could not run the draw")
        }
    }

    /**
     * Loads the winners of a lottery for the owner's list, sorted by name
     *
     * @param lotteryId id of the lottery that was drawn
     * @return the winners, empty if the lottery has not been drawn
     */
    suspend fun getWinners(lotteryId: String): List<LotteryResult> {
        return resultsRef.whereEqualTo("lotteryId", lotteryId)
            .whereEqualTo("won", true)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(LotteryResult::class.java) }
            .sortedBy { it.name }
    }

    /**
     * Loads one entrant's result so their screen can tell them whether they won
     *
     * @param lotteryId id of the lottery that was drawn
     * @param deviceId device id of the entrant
     * @return the result, or null if the lottery has not been drawn
     */
    suspend fun getMyResult(lotteryId: String, deviceId: String): LotteryResult? {
        return resultsRef.document(resultId(lotteryId, deviceId))
            .get()
            .await()
            .toObject(LotteryResult::class.java)
    }
}
