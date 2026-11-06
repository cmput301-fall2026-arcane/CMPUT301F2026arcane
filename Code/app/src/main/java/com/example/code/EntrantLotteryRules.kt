package com.example.code

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* US 01.01.06 As an entrant, I want to join an active lottery from the campsite details screen
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery
US 01.06.01 As an entrant, I want to view a list of my active reservations
US 01.06.02 As an entrant, I want to view a list of my past reservations

Works out where an entrant stands with a lottery, what the lottery button should say, and which
reservations are active. No Firestore calls so they can be unit tested
*/

/**
 * Where the entrant stands with a campsite's lottery
 */
enum class EntrantLotteryStatus {
    /** the campsite isn't in any lottery */
    NO_LOTTERY,
    /** registration hasn't opened yet */
    NOT_OPEN_YET,
    /** registration is open and the entrant hasn't entered */
    OPEN,
    /** registration has closed and the entrant didn't enter */
    CLOSED,
    /** the entrant is on the waitlist and the draw hasn't happened yet */
    JOINED,
    /** the entrant was drawn and hasn't reserved yet */
    WON,
    /** the draw happened and the entrant wasn't picked */
    NOT_SELECTED,
    /** the entrant has made (or cancelled) a reservation from this lottery */
    RESERVED
}

/**
 * Works out the entrant's status for a lottery
 *
 * @param now the current time
 * @param lottery the lottery, or null if the campsite isn't in one
 * @param joined true if the entrant is on the lottery's waitlist
 * @param result the entrant's draw result, or null if there isn't one yet
 * @param reservation the entrant's reservation from this lottery, or null
 * @return the entrant's status
 */
fun entrantLotteryStatus(
    now: Long,
    lottery: Lottery?,
    joined: Boolean,
    result: LotteryResult?,
    reservation: Reservation?
): EntrantLotteryStatus {
    if (lottery == null) return EntrantLotteryStatus.NO_LOTTERY
    if (reservation != null) return EntrantLotteryStatus.RESERVED
    if (result != null) {
        return if (result.won) EntrantLotteryStatus.WON else EntrantLotteryStatus.NOT_SELECTED
    }
    if (joined) return EntrantLotteryStatus.JOINED
    if (now < lottery.openDate) return EntrantLotteryStatus.NOT_OPEN_YET
    if (now >= lottery.closeDate) return EntrantLotteryStatus.CLOSED
    return EntrantLotteryStatus.OPEN
}

/**
 * Picks which of a campsite's lotteries to show on its details screen. In order: one the entrant
 * is already part of, then one that's open now, then the next one to open, then the latest one
 *
 * @param lotteries every lottery that includes the campsite
 * @param now the current time
 * @param involvedLotteryIds ids of lotteries the entrant has entered or has a result in
 * @return the lottery to show, or null if there are none
 */
fun pickLotteryForCampsite(
    lotteries: List<Lottery>,
    now: Long,
    involvedLotteryIds: Set<String>
): Lottery? {
    lotteries.filter { it.id in involvedLotteryIds }
        .maxByOrNull { it.openDate }
        ?.let { return it }
    lotteries.filter { isRegistrationOpen(now, it.openDate, it.closeDate) }
        .minByOrNull { it.closeDate }
        ?.let { return it }
    lotteries.filter { it.openDate > now }
        .minByOrNull { it.openDate }
        ?.let { return it }
    return lotteries.maxByOrNull { it.closeDate }
}

/**
 * The text on the lottery button for a status
 */
fun lotteryButtonLabel(status: EntrantLotteryStatus): String {
    return when (status) {
        EntrantLotteryStatus.NO_LOTTERY -> "NO LOTTERY YET"
        EntrantLotteryStatus.NOT_OPEN_YET -> "NOT OPEN YET"
        EntrantLotteryStatus.OPEN -> "ENTER LOTTERY"
        EntrantLotteryStatus.CLOSED -> "LOTTERY CLOSED"
        EntrantLotteryStatus.JOINED -> "LEAVE LOTTERY"
        EntrantLotteryStatus.WON -> "RESERVE NOW"
        EntrantLotteryStatus.NOT_SELECTED -> "NOT SELECTED"
        EntrantLotteryStatus.RESERVED -> "VIEW RESERVATION"
    }
}

/**
 * True if the lottery button does something for this status, false if it's only a label
 */
fun canPressLotteryButton(status: EntrantLotteryStatus): Boolean {
    return status == EntrantLotteryStatus.OPEN ||
            status == EntrantLotteryStatus.JOINED ||
            status == EntrantLotteryStatus.WON ||
            status == EntrantLotteryStatus.RESERVED
}

/**
 * One line explaining the status, shown above the lottery button
 *
 * @param status the entrant's status
 * @param lottery the lottery, used for its dates
 * @return the explanation
 */
fun lotteryStatusMessage(status: EntrantLotteryStatus, lottery: Lottery?): String {
    val opens = lottery?.let { formatDay(it.openDate) } ?: ""
    val closes = lottery?.let { formatDay(it.closeDate) } ?: ""
    return when (status) {
        EntrantLotteryStatus.NO_LOTTERY -> "This campsite isn't in a lottery yet"
        EntrantLotteryStatus.NOT_OPEN_YET -> "Lottery opens $opens"
        EntrantLotteryStatus.OPEN -> "Lottery open until $closes"
        EntrantLotteryStatus.CLOSED -> "Lottery closed on $closes"
        EntrantLotteryStatus.JOINED -> "You're entered! Winners are drawn after $closes"
        EntrantLotteryStatus.WON -> "You won! Pick your dates to reserve"
        EntrantLotteryStatus.NOT_SELECTED -> "You weren't selected this time"
        EntrantLotteryStatus.RESERVED -> "You have a reservation from this lottery"
    }
}

/**
 * Sort order for the lotteries list: wins first, then entered, then reserved, then the rest
 */
fun myLotteryOrder(status: EntrantLotteryStatus): Int {
    return when (status) {
        EntrantLotteryStatus.WON -> 0
        EntrantLotteryStatus.JOINED -> 1
        EntrantLotteryStatus.RESERVED -> 2
        else -> 3
    }
}

/**
 * Formats a time as a short day in the device's time zone, e.g. "Nov 12"
 */
fun formatDay(millis: Long): String {
    return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
}

/**
 * Builds the document id for a reservation. Using the lottery and the device id together stops a
 * winner from making two reservations from the same lottery
 *
 * @param lotteryId id of the lottery that was won
 * @param deviceId device id of the winner
 * @return the document id for the reservation
 */
fun reservationId(lotteryId: String, deviceId: String): String {
    return "${lotteryId}_$deviceId"
}

/**
 * Checks whether a reservation is still upcoming or in progress
 *
 * @param reservation the reservation
 * @param today today as UTC midnight millis (see todayUtcMillis)
 * @return true if it isn't cancelled and its last night is today or later
 */
fun isReservationActive(reservation: Reservation, today: Long): Boolean {
    return !reservation.cancelled && reservation.endDate >= today
}

/**
 * Splits reservations into active (soonest first) and past (most recent first). Cancelled
 * reservations count as past
 *
 * @param reservations the entrant's reservations
 * @param today today as UTC midnight millis (see todayUtcMillis)
 * @return active reservations paired with past reservations
 */
fun splitReservations(
    reservations: List<Reservation>,
    today: Long
): Pair<List<Reservation>, List<Reservation>> {
    val (active, past) = reservations.partition { isReservationActive(it, today) }
    return active.sortedBy { it.startDate } to past.sortedByDescending { it.startDate }
}