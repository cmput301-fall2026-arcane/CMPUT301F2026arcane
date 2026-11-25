package com.example.code.services

import com.example.code.model.Lottery
import com.example.code.model.WaitlistEntry
import com.example.code.rules.drawWinners
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class LotteryServiceTest {
    private fun entry(deviceId: String, lotteryId: String = "L1") =
        WaitlistEntry(deviceId = deviceId, lotteryId = lotteryId, name = deviceId)

    @Test
    fun drawWinners_picksNumWinnersWhenMoreEntrants() {
        val lottery = Lottery(id = "L1", numWinners = 2)
        val entries = (1..5).map { entry("d$it") }
        val winners = drawWinners(lottery, entries, Random(1))

        Assert.assertEquals(2, winners.size)
        Assert.assertTrue(entries.containsAll(winners))
    }

    @Test
    fun drawWinners_everyoneWinsWhenFewerEntrantsThanWinners() {
        val lottery = Lottery(id = "L1", numWinners = 10)
        val entries = (1..3).map { entry("d$it") }
        val winners = drawWinners(lottery, entries, Random(1))

        Assert.assertEquals(entries.toSet(), winners.toSet())
    }

    @Test
    fun drawWinners_emptyWaitlistReturnsNoWinners() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val winners = drawWinners(lottery, emptyList(), Random(1))

        Assert.assertTrue(winners.isEmpty())
    }

    @Test
    fun drawWinners_sameDeviceCannotWinTwice() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val entries = listOf(entry("d1"), entry("d1"), entry("d1"), entry("d2"))
        val winners = drawWinners(lottery, entries, Random(1))

        Assert.assertEquals(2, winners.size)
        Assert.assertEquals(winners.size, winners.map { it.deviceId }.distinct().size)
    }

    @Test
    fun drawWinners_ignoresEntriesFromOtherLotteries() {
        val lottery = Lottery(id = "L1", numWinners = 5)
        val entries = listOf(entry("d1"), entry("d2", lotteryId = "L2"))
        val winners = drawWinners(lottery, entries, Random(1))

        Assert.assertEquals(listOf("d1"), winners.map { it.deviceId })
    }

    @Test
    fun drawWinners_sameSeedGivesSameResult() {
        val lottery = Lottery(id = "L1", numWinners = 3)
        val entries = (1..10).map { entry("d$it") }

        val first = drawWinners(lottery, entries, Random(42))
        val second = drawWinners(lottery, entries, Random(42))

        Assert.assertEquals(first, second)
    }
}