package com.example.code

import org.junit.Assert.*
import org.junit.Test

// US 01.02.01 - As an entrant, I want to join the waiting list for a campsite lottery.
class WaitlistJoinRulesTest {

    private val minute = 60 * 1000L
    private val hour = 60 * minute
    private val day = 24 * hour

    // Sign up opens Aug 1, 2026 at 8:00 AM MDT and runs for one week
    private val opensAt = 1785592800000L
    private val closesAt = opensAt + 7 * day

    @Test
    fun canJoinOnFirstDayOfSignUp() {
        assertTrue(isRegistrationOpen(opensAt + hour, opensAt, closesAt))
    }

    @Test
    fun canJoinMidWeek() {
        assertTrue(isRegistrationOpen(opensAt + 3 * day, opensAt, closesAt))
    }

    @Test
    fun canJoinMinuteBeforeSignUpCloses() {
        assertTrue(isRegistrationOpen(closesAt - minute, opensAt, closesAt))
    }

    @Test
    fun cannotJoinBeforeSignUpOpens() {
        assertFalse(isRegistrationOpen(opensAt - day, opensAt, closesAt))
    }

    @Test
    fun cannotJoinOnceWeekEnds() {
        assertFalse(isRegistrationOpen(closesAt, opensAt, closesAt))
    }

    @Test
    fun cannotJoinDayAfterWeekEnds() {
        assertFalse(isRegistrationOpen(closesAt + day, opensAt, closesAt))
    }

    @Test
    fun joiningSameLotteryTwiceUsesSameEntry() {
        assertEquals(
            entryId("lottery-two-jack-aug", "a91f3c2e7b4d"),
            entryId("lottery-two-jack-aug", "a91f3c2e7b4d")
        )
    }

    @Test
    fun entrantCanJoinDifferentLotteries() {
        assertNotEquals(
            entryId("lottery-two-jack-aug", "a91f3c2e7b4d"),
            entryId("lottery-boulton-creek-aug", "a91f3c2e7b4d")
        )
    }
}
