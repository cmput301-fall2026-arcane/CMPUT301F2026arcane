package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LotteryRulesTest {

    private fun entries(count: Int): List<WaitlistEntry> {
        return (1..count).map {
            WaitlistEntry(
                deviceId = "device$it",
                lotteryId = "lot1",
                name = "Entrant $it",
                joinedAt = it.toLong()
            )
        }
    }

    @Test
    fun drawIsDueOnceTheSignUpPeriodCloses() {
        assertTrue(isDrawDue(200L, 100L, false))
    }

    @Test
    fun drawIsDueOnTheClosingMoment() {
        assertTrue(isDrawDue(100L, 100L, false))
    }

    @Test
    fun drawIsNotDueWhileSignUpIsOpen() {
        assertFalse(isDrawDue(50L, 100L, false))
    }

    @Test
    fun drawIsNotDueOnceItHasRun() {
        assertFalse(isDrawDue(200L, 100L, true))
    }

    @Test
    fun aDrawnLotteryIsNeverDueEvenBeforeItCloses() {
        assertFalse(isDrawDue(50L, 100L, true))
    }

    @Test
    fun everyEntrantGetsAResult() {
        val pool = entries(6)
        assertEquals(6, buildResults("lot1", pool, listOf("device1", "device2"), 900L).size)
    }

    @Test
    fun winnersAreMarkedAsWon() {
        val results = buildResults("lot1", entries(6), listOf("device1", "device2"), 900L)
        assertEquals(2, results.count { it.won })
    }

    @Test
    fun everyoneElseIsMarkedAsNotWon() {
        val results = buildResults("lot1", entries(6), listOf("device1", "device2"), 900L)
        assertEquals(4, results.count { !it.won })
    }

    @Test
    fun theRightEntrantIsMarkedAsTheWinner() {
        val results = buildResults("lot1", entries(3), listOf("device2"), 900L)
        assertEquals("device2", results.single { it.won }.deviceId)
    }

    @Test
    fun nobodyWinsWhenNoWinnersWereDrawn() {
        val results = buildResults("lot1", entries(4), emptyList(), 900L)
        assertTrue(results.none { it.won })
    }

    @Test
    fun resultsCarryTheLotteryAndTheDrawTime() {
        val results = buildResults("lot1", entries(3), emptyList(), 900L)
        assertTrue(results.all { it.lotteryId == "lot1" && it.drawnAt == 900L })
    }

    @Test
    fun resultsKeepTheEntrantName() {
        assertEquals("Entrant 1", buildResults("lot1", entries(1), emptyList(), 900L)[0].name)
    }

    @Test
    fun resultsStartOutNotNotified() {
        assertTrue(buildResults("lot1", entries(2), emptyList(), 900L).none { it.notified })
    }

    @Test
    fun anEmptyWaitlistBuildsNoResults() {
        assertTrue(buildResults("lot1", emptyList(), emptyList(), 900L).isEmpty())
    }

    @Test
    fun aWinnerIdThatNeverJoinedIsIgnored() {
        val results = buildResults("lot1", entries(2), listOf("device9"), 900L)
        assertTrue(results.none { it.won })
    }

    @Test
    fun resultIdCombinesLotteryAndDevice() {
        assertEquals("lot1_device1", resultId("lot1", "device1"))
    }

    @Test
    fun resultIdIsDifferentPerEntrant() {
        assertFalse(resultId("lot1", "device1") == resultId("lot1", "device2"))
    }

    @Test
    fun resultIdIsDifferentPerLottery() {
        assertFalse(resultId("lot1", "device1") == resultId("lot2", "device1"))
    }
}
