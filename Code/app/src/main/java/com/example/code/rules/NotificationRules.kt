package com.example.code.rules

import com.example.code.model.Notification
import com.example.code.model.NotificationType

/***
 * Maps a notification type to a title
 *
 * @param type the notification type
 *
 * @return title string
 */
fun notificationTitle(type: NotificationType): String = when (type) {
    NotificationType.LOTTERY_WON -> "You won the lottery!"
    NotificationType.LOTTERY_LOST -> "Lottery result"
    NotificationType.RESERVATION_CONFIRMED -> "Reservation confirmed"
    NotificationType.RESERVATION_CANCELLED -> "Reservation cancelled"
    NotificationType.MESSAGE -> "Message from the campsite owner"
}

/** One owner "send", made from the per-recipient documents that share a batchId */
data class SentSummary(
    val batchId: String,
    val content: String,
    val timestamp: Long,
    val recipients: Int,
    val optedOut: Int,
)

/**
 * Summarize notifications for batch notifications
 *
 * @param sent the notifications to summarize
 *
 * @return the summarized notifications
 */
fun summarizeSent(sent: List<Notification>): List<SentSummary> =
    sent.groupBy { it.batchId }
        .map { (batchId, group) ->
            SentSummary(
                batchId = batchId,
                content = group.first().content,
                timestamp = group.first().timestamp,
                recipients = group.size,
                optedOut = group.count { it.suppressed },
            )
        }
        .sortedByDescending { it.timestamp }