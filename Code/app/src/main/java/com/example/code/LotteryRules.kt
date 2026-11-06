package com.example.code

/* US 02.05.01 As a campsite owner, I want the system to automatically draw winners after the
registration period ends

Checks for the draw. No Firestore calls so they can be unit tested
*/

/**
 * Checks whether a lottery is ready to be drawn. Called whenever a lottery is shown so the draw
 * happens on its own once the sign up period has closed
 *
 * @param now the current time
 * @param closeDate the time the sign up period closes
 * @param isDrawn true if the lottery has already been drawn
 * @return true if the draw should run
 */
fun isDrawDue(now: Long, closeDate: Long, isDrawn: Boolean): Boolean {
    return !isDrawn && now >= closeDate
}

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
