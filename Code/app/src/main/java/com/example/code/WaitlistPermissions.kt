package com.example.code

import java.util.Calendar

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery
US 02.04.03 As a campsite owner, I want to set a registration period

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

/**
 * Checks whether an entrant can leave a waitlist. They must be in the waitlist and once the draw is
 * completed they cannot leave
 *
 * @param joined true if the entrant is on the waitlist
 * @param drawCompleted true if the winners have already been drawn
 * @return true if leaving is allowed
 */
fun canLeaveWaitlist(joined: Boolean, drawCompleted: Boolean): Boolean {
    return joined && !drawCompleted
}

/**
 * Gets midnight of the day a time falls in the device's timezone
 *
 * @param millis any moment of the day
 * @return midnight of that day
 */
fun startOfDay(millis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Checks the registration period chosen when a lottery is created
 *
 * @param opensAt the time registration opens
 * @param closesAt the time registration closes
 * @param now the current time
 * @return null if the period is valid and a message describing the problem if there is one
 */
fun validateRegistrationPeriod(opensAt: Long, closesAt: Long, now: Long): String? {
    return when {
        opensAt < startOfDay(now) -> "Registration cannot start in the past"
        closesAt <= opensAt -> "Registration must end after it starts"
        else -> null
    }
}

/**
 * Checks whether the draw should run now
 *
 * @param now the current time
 * @param closesAt the time registration closes
 * @param drawCompleted true if the winners have already been drawn
 * @return true if the draw is due
 */
fun isDrawDue(now: Long, closesAt: Long, drawCompleted: Boolean): Boolean {
    return closesAt > 0L && now >= closesAt && !drawCompleted
}

/**
 * The status shown next to the join button when it is disabled
 *
 * @param now the current time
 * @param opensAt the time registration opens
 * @param closesAt the time registration closes
 * @return text saying whether registration has not opened, is open, or is closed
 */
fun registrationStatus(now: Long, opensAt: Long, closesAt: Long): String {
    return when {
        now < opensAt -> "Registration has not opened yet"
        now < closesAt -> "Registration is open"
        else -> "Registration is closed"
    }
}