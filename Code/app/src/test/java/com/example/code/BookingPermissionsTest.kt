package com.example.code

import org.junit.Test

import org.junit.Assert.*

/**
 * Unit tests for the booking period checks
 */
class BookingPermissionsTest {

    private fun lottery(): Lottery {
        return Lottery(
            id = "lottery-1",
            openDate = 100L,
            closeDate = 900L,
            bookingOpenDate = 1000L,
            bookingCloseDate = 2000L
        )
    }

    @Test
    fun bookingAfterRegistrationEndsIsValid() {
        assertNull(validateBookingPeriod(900L, 1000L, 2000L))
    }

    @Test
    fun bookingStartingWhenRegistrationEndsIsRejected() {
        assertNotNull(validateBookingPeriod(900L, 900L, 2000L))
    }

    @Test
    fun bookingStartingBeforeRegistrationEndsIsRejected() {
        assertNotNull(validateBookingPeriod(900L, 800L, 2000L))
    }

    @Test
    fun bookingEndingBeforeItStartsIsRejected() {
        assertNotNull(validateBookingPeriod(900L, 1000L, 1000L))
    }

    @Test
    fun bookingIsClosedBeforeItOpens() {
        assertFalse(isBookingOpen(999L, 1000L, 2000L))
    }

    @Test
    fun bookingIsOpenOnTheOpeningMoment() {
        assertTrue(isBookingOpen(1000L, 1000L, 2000L))
    }

    @Test
    fun bookingIsClosedWhenItEnds() {
        assertFalse(isBookingOpen(2000L, 1000L, 2000L))
    }

    @Test
    fun winnerCanBookDuringTheBookingPeriod() {
        assertTrue(canBook("device-1", listOf("device-1", "device-2"), 1500L, 1000L, 2000L))
    }

    @Test
    fun nonWinnerCannotBook() {
        assertFalse(canBook("device-3", listOf("device-1", "device-2"), 1500L, 1000L, 2000L))
    }

    @Test
    fun emptyDeviceIdCannotBook() {
        assertFalse(canBook("", listOf(""), 1500L, 1000L, 2000L))
    }

    @Test
    fun winnerCannotBookBeforeBookingOpens() {
        assertFalse(canBook("device-1", listOf("device-1"), 950L, 1000L, 2000L))
    }

    @Test
    fun winnerCannotBookAfterTheOfferExpires() {
        assertFalse(canBook("device-1", listOf("device-1"), 2000L, 1000L, 2000L))
    }

    @Test
    fun offerIsNotExpiredDuringBooking() {
        assertFalse(isOfferExpired(1999L, 2000L))
    }

    @Test
    fun offerExpiresWhenBookingEnds() {
        assertTrue(isOfferExpired(2000L, 2000L))
    }

    @Test
    fun offerWithNoBookingPeriodNeverExpires() {
        assertFalse(isOfferExpired(9999L, 0L))
    }

    @Test
    fun statusFollowsTheLotteryThroughItsLife() {
        assertEquals("Registration has not opened yet", lotteryStatus(50L, 100L, 900L, 1000L, 2000L, false))
        assertEquals("Registration is open", lotteryStatus(500L, 100L, 900L, 1000L, 2000L, false))
        assertEquals("Registration closed, winners not drawn yet", lotteryStatus(950L, 100L, 900L, 1000L, 2000L, false))
        assertEquals("Winners drawn, booking opens soon", lotteryStatus(950L, 100L, 900L, 1000L, 2000L, true))
        assertEquals("Booking is open for the winners", lotteryStatus(1500L, 100L, 900L, 1000L, 2000L, true))
        assertEquals("Expired", lotteryStatus(2000L, 100L, 900L, 1000L, 2000L, true))
    }

    @Test
    fun undrawnLotteryExpiresWhenBookingEnds() {
        assertEquals("Expired", lotteryStatus(2500L, 100L, 900L, 1000L, 2000L, false))
    }

    @Test
    fun lotteryStatusReadsTheLotteryDates() {
        assertEquals("Registration is open", lotteryStatus(lottery(), 500L))
        assertEquals("Registration closed, winners not drawn yet", lotteryStatus(lottery(), 950L))
        assertEquals("Expired", lotteryStatus(lottery(), 2000L))
    }

    @Test
    fun canBookReadsTheLotteryDates() {
        assertFalse(canBook("device-1", lottery(), 1500L))
    }
}