package com.example.code.model

import com.google.firebase.firestore.DocumentId

/**
 * The type of notification
 */
enum class NotificationType {
    LOTTERY_WON,
    LOTTERY_LOST,
    RESERVATION_CONFIRMED,
    RESERVATION_CANCELLED,
    MESSAGE
}

/**
 * A notification sent to a user
 *
 * @property id Firestore doc id
 * @property senderId user id of the sender
 * @property recipientId user id of the recipient
 * @property type type of notification
 * @property content text of the notification
 * @property timestamp time the notification was sent
 * @property campsiteId id of the campsite the notification is for
 * @property lotteryId id of the lottery the notification is for
 * @property reservationId id of the reservation the notification is for
 * @property batchId shared by every notification from the same send
 * @property suppressed true if the recipient had opted out, so it's logged but not shown to them
 * @property read true if the notification has been read
 */
data class Notification(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val batchId: String = "",
    val campsiteId: String = "",
    val lotteryId: String = "",
    val reservationId: String = "",
    val suppressed: Boolean = false,
    val read: Boolean = false,
)