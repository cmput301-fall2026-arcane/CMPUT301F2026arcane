package com.example.code
/* US 02.04.04 As a campsite owner, I want to set a booking period

Checks for the booking period of a lottery which is how long the winners have to book before the
offer goes away
*/

/**
 * Checks the booking period chosen when a lottery is created
 *
 * @param closesAt the time registration closes
 * @param bookingOpensAt the time the winners can start reserving
 * @param bookingClosesAt the time the offer expires
 * @return null if the period is valid and a message describing the problem if there is one
 */
fun validateBookingPeriod(closesAt: Long, bookingOpensAt: Long, bookingClosesAt: Long): String? {
    return when {
        bookingOpensAt <= closesAt -> "Booking must begin after registration ends"
        bookingClosesAt <= bookingOpensAt -> "Booking must end after it starts"
        else -> null
    }
}

/**
 * Checks whether a lottery is inside its booking period
 *
 * @param now the current time
 * @param bookingOpensAt the time the winners can start reserving
 * @param bookingClosesAt the time the offer expires
 * @return true if winners can reserve now
 */
fun isBookingOpen(now: Long, bookingOpensAt: Long, bookingClosesAt: Long): Boolean {
    return now >= bookingOpensAt && now < bookingClosesAt
}

/**
 * Checks whether a user can reserve. They must have won the draw and the booking period must
 * be open
 *
 * @param deviceId device id of the user trying to reserve
 * @param winners device ids of the drawn winners
 * @param now the current time
 * @param bookingOpensAt the time the winners can start reserving
 * @param bookingClosesAt the time the offer expires
 * @return true if the user is a winner and booking is open
 */
fun canBook(
    deviceId: String,
    winners: List<String>,
    now: Long,
    bookingOpensAt: Long,
    bookingClosesAt: Long
): Boolean {
    return deviceId.isNotEmpty() && deviceId in winners &&
            isBookingOpen(now, bookingOpensAt, bookingClosesAt)
}

/**
 * Checks whether the offer has expired. Once the booking period ends the dates are released.
 * A lottery with no booking period never expires
 *
 * @param now the current time
 * @param bookingClosesAt the time the offer expires, 0 if there is no booking period
 * @return true if the booking period is over
 */
fun isOfferExpired(now: Long, bookingClosesAt: Long): Boolean {
    return bookingClosesAt > 0L && now >= bookingClosesAt
}

/**
 * The status of a lottery shown to owners and entrants
 *
 * @param now the current time
 * @param opensAt the time registration opens
 * @param closesAt the time registration closes
 * @param bookingOpensAt the time the winners can start reserving
 * @param bookingClosesAt the time the offer expires
 * @param drawCompleted true if the winners have already been drawn
 * @return text describing where the lottery is, including "Expired"
 */
fun lotteryStatus(
    now: Long,
    opensAt: Long,
    closesAt: Long,
    bookingOpensAt: Long,
    bookingClosesAt: Long,
    drawCompleted: Boolean
): String {
    return when {
        now < opensAt -> "Registration has not opened yet"
        now < closesAt -> "Registration is open"
        isOfferExpired(now, bookingClosesAt) -> "Expired"
        !drawCompleted -> "Registration closed, winners not drawn yet"
        now < bookingOpensAt -> "Winners drawn, booking opens soon"
        else -> "Booking is open for the winners"
    }
}

/**
 * The status of a lottery shown to owners and entrants
 *
 * @param lottery the lottery
 * @param now the current time
 * @return text describing where the lottery is, including "Expired"
 */
fun lotteryStatus(lottery: Lottery, now: Long): String {
    return lotteryStatus(
        now, lottery.openDate, lottery.closeDate,
        lottery.bookingOpenDate, lottery.bookingCloseDate, lottery.isDrawn
    )
}

/**
 * Checks whether a user can reserve this lottery now
 *
 * @param deviceId device id of the user trying to reserve
 * @param lottery the lottery
 * @param now the current time
 * @return true if the user is a winner and the booking period is open
 */
fun canBook(deviceId: String, lottery: Lottery, now: Long): Boolean {
    return canBook(deviceId, lottery.winners, now, lottery.bookingOpenDate, lottery.bookingCloseDate)
}