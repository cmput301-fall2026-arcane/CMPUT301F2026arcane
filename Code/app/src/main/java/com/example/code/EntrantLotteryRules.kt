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

/** One day in millis. Dates from the date picker are UTC midnights, so nights are this far apart */
const val DAY_MILLIS = 24L * 60 * 60 * 1000

/** The longest stay a winner can reserve */
const val MAX_STAY_NIGHTS = 14

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

    /** the entrant was drawn but the booking period hasn't started */
    BOOKING_NOT_OPEN,

    /** the entrant was drawn but the booking period ended before they reserved */
    OFFER_EXPIRED,

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
        if (!result.won) return EntrantLotteryStatus.NOT_SELECTED
        if (isOfferExpired(now, lottery.bookingCloseDate)) return EntrantLotteryStatus.OFFER_EXPIRED
        if (!hasBookingStarted(now, lottery)) return EntrantLotteryStatus.BOOKING_NOT_OPEN
        return EntrantLotteryStatus.WON
    }
    if (joined) return EntrantLotteryStatus.JOINED
    if (now < lottery.openDate) return EntrantLotteryStatus.NOT_OPEN_YET
    if (now >= lottery.closeDate) return EntrantLotteryStatus.CLOSED
    return EntrantLotteryStatus.OPEN
}

/** Check whether booking period has started
 * bookingOpenDate = 0 counts as started
 */
fun hasBookingStarted(now: Long, lottery: Lottery): Boolean {
    return lottery.bookingOpenDate <= 0L || now >= lottery.bookingOpenDate
}

/** Check whether the winners can reserve
 * @param now the current time
 * @param lottery the lottery that was won
 * @return true if reserving is possible
 */
fun canReserveNow(now: Long, lottery: Lottery): Boolean {
    return hasBookingStarted(now, lottery) && !isOfferExpired(now, lottery.bookingCloseDate)
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
        EntrantLotteryStatus.BOOKING_NOT_OPEN -> "BOOKING OPENS SOON"
        EntrantLotteryStatus.OFFER_EXPIRED -> "OFFER EXPIRED"
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
    val bookingOpens = lottery?.let { formatDay(it.bookingOpenDate) } ?: ""
    val bookingCloses = lottery?.takeIf { it.bookingCloseDate > 0L }
        ?.let { formatDay(it.bookingCloseDate) }
    return when (status) {
        EntrantLotteryStatus.NO_LOTTERY -> "This campsite isn't in a lottery yet"
        EntrantLotteryStatus.NOT_OPEN_YET -> "Lottery opens $opens"
        EntrantLotteryStatus.OPEN -> "Lottery open until $closes"
        EntrantLotteryStatus.CLOSED -> "Lottery closed on $closes"
        EntrantLotteryStatus.JOINED -> "You're entered! Winners are drawn after $closes"
        EntrantLotteryStatus.WON ->
            if (bookingCloses != null) "You won! Reserve by $bookingCloses"
            else "You won! Reserve now!"

        EntrantLotteryStatus.BOOKING_NOT_OPEN -> "You won! Booking opens $bookingOpens"
        EntrantLotteryStatus.OFFER_EXPIRED -> "You won, but booking closed on ${bookingCloses ?: ""}"
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
        EntrantLotteryStatus.BOOKING_NOT_OPEN -> 1
        EntrantLotteryStatus.JOINED -> 2
        EntrantLotteryStatus.RESERVED -> 3
        else -> 4
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

/**
 * How many nights a stay covers. The end date is the last night, so Nov 10 to Nov 12 is 3 nights
 *
 * @param startDate first night, as UTC midnight millis
 * @param endDate last night, as UTC midnight millis
 * @return the number of nights, 0 if the end is before the start
 */
fun nightCount(startDate: Long, endDate: Long): Int {
    if (endDate < startDate) return 0
    return ((endDate - startDate) / DAY_MILLIS).toInt() + 1
}

/**
 * Every night a stay covers, as UTC midnight millis
 *
 * @param startDate first night
 * @param endDate last night
 * @return the nights in order, empty if the end is before the start
 */
fun nightsOf(startDate: Long, endDate: Long): List<Long> {
    return List(nightCount(startDate, endDate)) { startDate + it * DAY_MILLIS }
}

/**
 * Checks that none of a stay's nights are already booked
 *
 * @param startDate first night
 * @param endDate last night
 * @param booked nights already booked at the campsite
 * @return true if every night in the stay is free
 */
fun isRangeFree(startDate: Long, endDate: Long, booked: Set<Long>): Boolean {
    return booked.none { it in startDate..endDate }
}

/**
 * Checks a stay a winner has picked
 *
 * @param startDate first night
 * @param endDate last night
 * @param booked nights already booked at the campsite
 * @return null if the stay is fine, otherwise a message saying what's wrong
 */
fun validateStay(startDate: Long, endDate: Long, booked: Set<Long>): String? {
    val nights = nightCount(startDate, endDate)
    return when {
        nights < 1 -> "Pick at least one night"
        nights > MAX_STAY_NIGHTS -> "Stays can be at most $MAX_STAY_NIGHTS nights"
        !isRangeFree(startDate, endDate, booked) -> "Some of those nights are already booked"
        else -> null
    }
}

/**
 * Builds the document id that locks one night at one campsite. Only one reservation can hold
 * each id, which is what stops two winners booking the same night
 *
 * @param campsiteId id of the campsite
 * @param night the night, as UTC midnight millis
 * @return the document id for the night
 */
fun bookedNightId(campsiteId: String, night: Long): String {
    return "${campsiteId}_$night"
}