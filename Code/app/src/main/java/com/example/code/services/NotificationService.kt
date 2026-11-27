package com.example.code.services

import com.example.code.model.Lottery
import com.example.code.model.LotteryResult
import com.example.code.model.Notification
import com.example.code.model.NotificationType
import com.example.code.model.Reservation
import com.example.code.model.WaitlistEntry
import com.example.code.rules.resultId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

/*
US 01.03.01 As an entrant, I want to receive a notification when I win the campsite lottery
US 01.03.02 As an entrant, I want to receive a notification when I lose the campsite lottery
US 01.05.02 As an entrant, I want to receive reservation by notification or downloadable CSV
US 02.10.01 As a campsite owner, I want to send notifications to entrants on my waitlist
US 02.10.02 As a campsite owner, I want to view a list of all notifications I have sent
US 03.08.01 As an admin, I want to review logs of all notifications
 */

const val BATCH_WRITES = 500
const val SYSTEM_SENDER = "system"

/**
 * Sends notifications and loads them for entrants, owners and admins
 *
 * @param db the Firestore instance to use
 */
class NotificationService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val notificationsRef = db.collection("notifications")
    private val usersRef = db.collection("users")
    private val waitlistRef = db.collection("waitlist")
    private val resultsRef = db.collection("results")

    /**
     * Gets all recipients who opted out of notifications
     */
    private suspend fun getOptedOut(): Set<String> {
        return usersRef.whereEqualTo("notificationPreference", false).get().await()
            .documents.map {it.id }.toSet()
    }
    /**
     * Writes one notification per recipient, notifications for the same lottery are grouped by
     * batch id and recipients who opted out of notifications do not get notified can still view
     * Batches Firestore writes so notifications are sent if all succeeds or none at all
     *
     * @param senderId id of user sending the notification
     * @param recipientIds ids of recipients to send notifications to
     * @param type type of notification
     * @param title notification title
     * @param content notification content
     * @param campsiteId id of applicable campsite
     * @param lotteryId id of applicable lottery
     * @param reservationId id of applicable reservation
     *
     * @return how many recipients were notified
     */
    private suspend fun send(
        senderId: String,
        recipientIds: List<String>,
        type: NotificationType,
        title: String,
        content: String,
        campsiteId: String = "",
        lotteryId: String = "",
        reservationId: String = "",
    ): Int {
        val ids = recipientIds.filter{ it.isNotEmpty() }.distinct()
        if (ids.isEmpty()) return 0
        val batchId = notificationsRef.document().id
        val optedOut = getOptedOut()
        val now = System.currentTimeMillis()

        for (chunk in ids.chunked(BATCH_WRITES)) {
            val batch = db.batch()
            for (recipientId in chunk) {
                batch.set(
                    notificationsRef.document(),
                    Notification(
                        senderId = senderId,
                        recipientId = recipientId,
                        type = type,
                        title = title,
                        content = content,
                        timestamp = now,
                        batchId = batchId,
                        campsiteId = campsiteId,
                        lotteryId = lotteryId,
                        reservationId = reservationId,
                        suppressed = recipientId in optedOut,
                    )
                )
            }
            batch.commit().await()
        }
        return ids.count{ it !in optedOut }
    }

    /**
     * Notifies every entrant in a drawn lottery whether they won. Results already notified are
     * skipped, so calling this twice does not send duplicates
     *
     * @param lottery the lottery that was drawn
     * @param results the lottery results for each entrant
     *
     * @return how many entrants were notified
     */
    suspend fun sendLotteryResults(lottery: Lottery, results: List<LotteryResult>): Int {
        val pending = results.filter { !it.notified }
        if (pending.isEmpty()) return 0

        val campsiteId = lottery.campsites.firstOrNull() ?: ""
        val (winners, losers) = pending.partition { it.won }
        val sent = send(
            senderId = SYSTEM_SENDER,
            recipientIds = winners.map{ it.deviceId },
            type = NotificationType.LOTTERY_WON,
            title = "Lottery Results",
            content = "You won the campsite lottery!",
            campsiteId = campsiteId,
            lotteryId = lottery.id,
        ) + send(
            senderId = SYSTEM_SENDER,
            recipientIds = losers.map{ it.deviceId },
            type = NotificationType.LOTTERY_LOST,
            title = "Lottery Results",
            content = "Unfortunately, you were not selected for this lottery. Try again next time.",
            campsiteId = campsiteId,
            lotteryId = lottery.id,
        )

        val batch = db.batch()
        for (result in pending) {
            batch.update(
                resultsRef.document(resultId(lottery.id, result.deviceId)),
                "notified",
                true,
            )
        }
        batch.commit().await()
        return sent
    }

    /**
     * Notifies the entrant that their reservation has been confirmed
     *
     * @param reservation the reservation to confirm
     *
     * @return true if the notification was delivered
     */
    suspend fun sendReservationConfirmation(
        reservation: Reservation,
    ): Boolean {
        return send(
            senderId = SYSTEM_SENDER,
            recipientIds = listOf(reservation.deviceId),
            type = NotificationType.RESERVATION_CONFIRMED,
            title = "Reservation Confirmation",
            content = "Your reservation for ${reservation.campsiteName} has been confirmed!",
            campsiteId = reservation.campsiteId,
            lotteryId = reservation.lotteryId,
            reservationId = reservation.id,
        ) == 1
    }

    /**
     * Notifies the entrant that their reservation has been cancelled
     *
     * @param reservation the reservation to cancel
     *
     * @return true if the notification was delivered
     */
    suspend fun sendReservationCancellation(
        reservation: Reservation,
    ): Boolean {
        return send(
            senderId = SYSTEM_SENDER,
            recipientIds = listOf(reservation.deviceId),
            type = NotificationType.RESERVATION_CANCELLED,
            title = "Reservation Cancellation",
            content = "Your reservation for ${reservation.campsiteName} has been cancelled.",
            campsiteId = reservation.campsiteId,
            lotteryId = reservation.lotteryId,
            reservationId = reservation.id,
        ) == 1
    }

    /**
     * Sends a notification to all entrants in a waitlist
     *
     * @param ownerId id of the campsite owner
     * @param lotteryId id of the lottery
     * @param content notification content
     *
     * @return how many entrants were notified
     */
    suspend fun sendToWaitlist(
        ownerId: String,
        lotteryId: String,
        content: String
    ): Int {
        val recipients = waitlistRef.whereEqualTo("lotteryId", lotteryId).get().await()
            .toObjects(WaitlistEntry::class.java)
            .map { it.deviceId }
        return send(
            senderId = ownerId,
            recipientIds = recipients,
            type = NotificationType.MESSAGE,
            title = "Waitlist Notification",
            content = content,
            lotteryId = lotteryId,
        )
    }

    /**
     * Loads everything an owner has sent, newest first. One send to a waitlist is several
     * documents sharing a batchId, so the screen should group by batchId
     *
     * @param ownerId device id of the owner
     * @return the owner's sent notifications
     */
    suspend fun getSentBy(ownerId: String): List<Notification> {
        return notificationsRef
            .whereEqualTo("senderId", ownerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Notification::class.java)
    }

    /**
     * Loads everything an entrant has received, newest first
     *
     * @param recipientId device id of the recipient
     *
     * @return the recipient's received notifications
     */
    suspend fun getSentTo(recipientId: String): List<Notification> {
        return notificationsRef
            .whereEqualTo("recipientId", recipientId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Notification::class.java)
    }

    /**
     * Loads every notification for the admin log, including suppressed ones, newest first
     *
     * @return all notifications
     */
    suspend fun getAllNotifications(): List<Notification> {
        return notificationsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Notification::class.java)
    }

    /**
     * Live update of a user's inbox, newest first. Notifications suppressed by opting out are
     * left out. Call remove() on the result when the screen closes
     *
     * @param deviceId id of the user
     * @param onChange called with the inbox every time it changes
     * @param onError called if the query fails, for example when the Firestore index is missing
     */
    fun listenToNotifications(
        deviceId: String,
        onChange: (List<Notification>) -> Unit,
        onError: (String) -> Unit,
    ): ListenerRegistration {
        return notificationsRef
            .whereEqualTo("recipientId", deviceId)
            .whereEqualTo("suppressed", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Could not load notifications")
                    return@addSnapshotListener
                }
                if (snapshot != null) onChange(snapshot.toObjects(Notification::class.java))
            }
    }

    /**
     * Marks a notification as read
     *
     * @param notificationId Firestore id of the notification
     */
    suspend fun markRead(notificationId: String) {
        notificationsRef.document(notificationId).update("read", true).await()
    }
}
