package com.example.code.rules

import com.example.code.model.Lottery
import com.example.code.model.LotteryResult
import com.example.code.model.WaitlistEntry
import kotlin.random.Random

/* US 02.05.01 As a campsite owner, I want the system to automatically draw winners after the
registration period ends

Checks for the draw. No Firestore calls so they can be unit tested
*/

/**
 * Builds a result for every entrant, marking the ones that were drawn as winners
 *
 * @param lotteryId id of the lottery that was drawn
 * @param entries everyone on the waitlist
 * @param winnerIds device ids of the entrants that won
 * @param drawnAt the time the draw ran
 * @return a result for every entrant
 */
fun buildResults(
    lotteryId: String,
    entries: List<WaitlistEntry>,
    winnerIds: List<String>,
    drawnAt: Long
): List<LotteryResult> {
    val won = winnerIds.toSet()
    return entries.map {
        LotteryResult(
            lotteryId = lotteryId,
            deviceId = it.deviceId,
            name = it.name,
            won = won.contains(it.deviceId),
            drawnAt = drawnAt
        )
    }
}

/**
 * Builds the document id for a result. Using the lottery and the device id together stops an
 * entrant from holding two results in the same lottery
 *
 * @param lotteryId id of the lottery
 * @param deviceId device id of the entrant
 * @return the document id for the result
 */
fun resultId(lotteryId: String, deviceId: String): String {
    return "${lotteryId}_$deviceId"
}

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