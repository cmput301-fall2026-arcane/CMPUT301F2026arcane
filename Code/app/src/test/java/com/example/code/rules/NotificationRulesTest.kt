package com.example.code.rules

import com.example.code.model.Notification
import com.example.code.model.NotificationType
import org.junit.Assert
import org.junit.Test

// US 01.03.01 - As an entrant, I want to receive a notification when I win the campsite lottery.
// US 01.03.02 - As an entrant, I want to receive a notification when I lose the campsite lottery.
// US 01.05.02 - As an entrant, I want to receive reservation by notification or downloadable CSV.
// US 02.10.01 - As a campsite owner, I want to send notifications to entrants who have joined the waitlist for my campsite lottery.
// US 02.10.02 - As a campsite owner, I want to view a list of all notifications I have sent.
class NotificationRulesTest {

    private fun sent(batch: String, recipient: String, time: Long, suppressed: Boolean = false) =
        Notification(
            id = "$batch-$recipient",
            senderId = "owner-device-42",
            recipientId = recipient,
            type = NotificationType.MESSAGE,
            content = "Road to Two Jack closes Friday",
            timestamp = time,
            batchId = batch,
            lotteryId = "lottery-two-jack-aug",
            suppressed = suppressed
        )

    // US 01.03.01
    @Test
    fun winTitle() {
        Assert.assertEquals("You won the lottery!", notificationTitle(NotificationType.LOTTERY_WON))
    }

    // US 01.03.02
    @Test
    fun lossTitle() {
        Assert.assertEquals("Lottery result", notificationTitle(NotificationType.LOTTERY_LOST))
    }

    // US 01.05.02
    @Test
    fun reservationConfirmedTitle() {
        Assert.assertEquals("Reservation confirmed", notificationTitle(NotificationType.RESERVATION_CONFIRMED))
    }

    @Test
    fun reservationCancelledTitle() {
        Assert.assertEquals("Reservation cancelled", notificationTitle(NotificationType.RESERVATION_CANCELLED))
    }

    // US 02.10.01
    @Test
    fun ownerMessageTitle() {
        Assert.assertEquals("Message from the campsite owner", notificationTitle(NotificationType.MESSAGE))
    }

    // US 02.10.02
    @Test
    fun oneSendBecomesOneSummary() {
        val list = listOf(sent("b1", "d1", 100L), sent("b1", "d2", 100L), sent("b1", "d3", 100L))
        Assert.assertEquals(1, summarizeSent(list).size)
    }

    // US 02.10.02
    @Test
    fun summaryCountsRecipients() {
        val list = listOf(sent("b1", "d1", 100L), sent("b1", "d2", 100L))
        Assert.assertEquals(2, summarizeSent(list)[0].recipients)
    }

    // US 02.10.02
    @Test
    fun summaryCountsOptedOut() {
        val list = listOf(sent("b1", "d1", 100L), sent("b1", "d2", 100L, suppressed = true))
        Assert.assertEquals(1, summarizeSent(list)[0].optedOut)
    }

    // US 02.10.02
    @Test
    fun separateSendsStaySeparate() {
        val list = listOf(sent("b1", "d1", 100L), sent("b2", "d1", 200L))
        Assert.assertEquals(2, summarizeSent(list).size)
    }

    // US 02.10.02
    @Test
    fun newestSendComesFirst() {
        val list = listOf(sent("b1", "d1", 100L), sent("b2", "d1", 200L))
        Assert.assertEquals("b2", summarizeSent(list)[0].batchId)
    }

    // US 02.10.02
    @Test
    fun summaryKeepsTheMessage() {
        Assert.assertEquals(
            "Road to Two Jack closes Friday",
            summarizeSent(listOf(sent("b1", "d1", 100L)))[0].content
        )
    }

    // US 02.10.02
    @Test
    fun nothingSentGivesNoSummaries() {
        Assert.assertTrue(summarizeSent(emptyList()).isEmpty())
    }
}
