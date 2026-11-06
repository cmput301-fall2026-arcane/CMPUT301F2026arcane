package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// US 01.01.06 - As an entrant, I want to join an active lottery from the campsite details screen.
// US 01.02.01 - As an entrant, I want to join the waiting list for a campsite lottery.
@RunWith(AndroidJUnit4::class)
class CampsiteWaitlistJoinTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = System.currentTimeMillis()

    private val entrant = User(
        deviceId = "a91f3c2e7b4d",
        name = "Priya Sharma",
        email = "priya.sharma@ualberta.ca",
        phone = "780-555-0142",
        roles = mutableListOf(Role.ENTRANT)
    )

    private fun join(user: User, lotteryId: String, opensAt: Long, closesAt: Long): JoinResult? {
        var result: JoinResult? = null
        CampsiteWaitlist(capacity = 20).joinWaitlist(user, lotteryId, opensAt, closesAt) { result = it }
        return result
    }

    @Test
    fun cannotJoinAfterLotteryCloses() {
        val result = join(entrant, "lottery-two-jack-aug", now - 7 * day, now - day)
        assertEquals(JoinResult.RegistrationClosed, result)
    }

    @Test
    fun cannotJoinBeforeLotteryOpens() {
        val result = join(entrant, "lottery-two-jack-aug", now + day, now + 7 * day)
        assertEquals(JoinResult.RegistrationClosed, result)
    }

    @Test
    fun cannotJoinWithoutDeviceId() {
        val result = join(entrant.copy(deviceId = ""), "lottery-two-jack-aug", now - day, now + day)
        assertTrue(result is JoinResult.Failure)
    }

    @Test
    fun cannotJoinWithoutLottery() {
        val result = join(entrant, "", now - day, now + day)
        assertTrue(result is JoinResult.Failure)
    }
}
