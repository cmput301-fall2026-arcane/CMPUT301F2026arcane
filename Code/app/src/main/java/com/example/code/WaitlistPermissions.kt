package com.example.code

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery

Checks for the waitlist screens. No Firestore calls so they can be unit tested
*/

fun canViewWaitlist(actor: User, campsite: Campsite): Boolean {
    return campsite.ownerId.isNotEmpty() && campsite.ownerId == actor.deviceId
}

fun isWaitlistFull(entrantCount: Int, capacity: Int): Boolean {
    return capacity > 0 && entrantCount >= capacity
}

/**
 * Checks whether a lottery is inside its sign up period
 *
 * @param now the current time
 * @param opensAt the time the sign up period opens
 * @param closesAt the time the sign up period closes
 * @return true if entrants can still join
 */
fun isRegistrationOpen(now: Long, opensAt: Long, closesAt: Long): Boolean {
    return now >= opensAt && now < closesAt
}

/**
 * Builds the document id for a waitlist entry. Using the lottery and the device id together stops
 * an entrant from joining the same lottery twice
 *
 * @param lotteryId id of the lottery
 * @param deviceId device id of the entrant
 * @return the document id for the entry
 */
fun entryId(lotteryId: String, deviceId: String): String {
    return "${lotteryId}_$deviceId"
}
