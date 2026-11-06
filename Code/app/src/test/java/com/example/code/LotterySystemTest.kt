package com.example.code

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LotterySystemTest {
    private lateinit var system: LotterySystem

    private fun entry(deviceId: String, lotteryId: String = "L1") =
        WaitlistEntry(deviceId = deviceId, lotteryId = lotteryId, name = deviceId)

    @Test
    fun drawWinners_picksNumWinnersWhenMoreEntrants() {
        val lottery = Lottery(id = "L1", numWinners = 2)
        val entries = (1..5).map { entry("d$it") }
        val winners = system.drawWinners(lottery, entries, Random(1))

        assertEquals(2, winners.size)
        assertTrue(entries.containsAll(winners))
    }

    @Test
    fun drawWinners_everyoneWinsWhenFewerEntrantsThanWinners() {
        val lottery = Lottery(id = "L1", numWinners = 10)
        val entries = (1..3).map { entry("d$it") }
        val winners = system.drawWinners(lottery, entries, Random(1))

        assertEquals(entries.toSet(), winners.toSet())
    }

    @Test
    fun drawWinners_emptyWaitlistReturnsNoWinners() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val winners = system.drawWinners(lottery, emptyList(), Random(1))

        assertTrue(winners.isEmpty())
    }

    @Test
    fun drawWinners_sameDeviceCannotWinTwice() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val entries = listOf(entry("d1"), entry("d1"), entry("d1"), entry("d2"))
        val winners = system.drawWinners(lottery, entries, Random(1))

        assertEquals(2, winners.size)
        assertEquals(winners.size, winners.map { it.deviceId }.distinct().size)
    }

    @Test
    fun drawWinners_ignoresEntriesFromOtherLotteries() {
        val lottery = Lottery(id = "L1", numWinners = 5)
        val entries = listOf(entry("d1"), entry("d2", lotteryId = "L2"))
        val winners = system.drawWinners(lottery, entries, Random(1))

        assertEquals(listOf("d1"), winners.map { it.deviceId })
    }

    @Test
    fun drawWinners_sameSeedGivesSameResult() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val entries = (1..10).map { entry("d$it") }

        val first = system.drawWinners(lottery, entries, Random(42))
        val second = system.drawWinners(lottery, entries, Random(42))

        assertEquals(first, second)
    }
}