package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery

Reads and writes the "waitlist" collection and keeps a live list of the entries for one lottery.
The list is Compose state so the UI redraws as entrants join or leave. Each entry is stored under
the lottery id and the device id together so an entrant cannot join the same lottery twice
*/

/**
 * The outcome of an entrant joining a waitlist
 */
sealed class JoinResult {
    object Success : JoinResult()
    object AlreadyJoined : JoinResult()
    object RegistrationClosed : JoinResult()
    object WaitlistFull : JoinResult()
    data class Failure(val message: String) : JoinResult()
}

/**
 * The outcome of an entrant leaving a waitlist
 */
sealed class LeaveResult {
    object Success : LeaveResult()
    object NotOnWaitlist : LeaveResult()
    object DrawCompleted : LeaveResult()
    data class Failure(val message: String) : LeaveResult()
}


class CampsiteWaitlist (
    val capacity: Int = 0,
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val waitlistRef = db.collection("waitlist")
    private val _entries = mutableStateListOf<WaitlistEntry>()
    private var listener: ListenerRegistration? = null

    val entries: List<WaitlistEntry> get() = _entries

    /**
     * Starts a live listener on the entries for the lottery, oldest first
     * Any listener already running is stopped first
     *
     * @param lotteryId id of the lottery whose waitlist should be loaded
     */
    fun listenToLottery(lotteryId: String) {
        listener?.remove()
        listener = waitlistRef.whereEqualTo("lotteryId", lotteryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val loaded = snapshot.documents
                    .mapNotNull { it.toObject(WaitlistEntry::class.java) }
                    .sortedBy { it.joinedAt }
                _entries.clear()
                _entries.addAll(loaded)
            }
    }

    /**
     * Stops the live listener, called when the screen is closed
     */
    fun stopListening() {
        listener?.remove()
        listener = null
    }

    fun getEntrants(): List<WaitlistEntry> {
        return entries
    }

    fun isFull(): Boolean {
        return isWaitlistFull(_entries.size, capacity)
    }

    fun joinWaitlist(
        entrant: User,
        lotteryId: String,
        opensAt: Long,
        closesAt: Long,
        onResult: (JoinResult) -> Unit
    ) {
        if (entrant.deviceId.isEmpty() || lotteryId.isEmpty()) {
            onResult(JoinResult.Failure("Missing entrant or lottery"))
            return
        }
        if (!isRegistrationOpen(System.currentTimeMillis(), opensAt, closesAt)) {
            onResult(JoinResult.RegistrationClosed)
            return
        }
        if (isFull()) {
            onResult(JoinResult.WaitlistFull)
            return
        }

        val doc = waitlistRef.document(entryId(lotteryId, entrant.deviceId))
        doc.get()
            .addOnSuccessListener { existing ->
                if (existing.exists()) {
                    onResult(JoinResult.AlreadyJoined)
                    return@addOnSuccessListener
                }
                val entry = WaitlistEntry(
                    deviceId = entrant.deviceId,
                    lotteryId = lotteryId,
                    name = entrant.name,
                    joinedAt = System.currentTimeMillis()
                )
                doc.set(entry)
                    .addOnSuccessListener { onResult(JoinResult.Success) }
                    .addOnFailureListener { e ->
                        onResult(JoinResult.Failure(e.message ?: "Could not join the waitlist"))
                    }
            }
            .addOnFailureListener { e ->
                onResult(JoinResult.Failure(e.message ?: "Could not join the waitlist"))
            }
    }

    /**
     * Checks if an entrant is on the waitlist of a lottery. The screen uses this to show
     * either join or leave waitlist
     *
     * @param entrant the entrant
     * @param lotteryId id of the lottery
     * @param onResult called with true if the entrant is on the waitlist
     */
    fun isOnWaitlist(entrant: User, lotteryId: String, onResult: (Boolean) -> Unit) {
        waitlistRef.document(entryId(lotteryId, entrant.deviceId)).get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Removes an entrant from the waitlist of a lottery and the screen asks them to confirm first
     * only calling this if they do. Not allowed to leave once the draw is completed
     *
     * @param entrant the entrant leaving
     * @param lotteryId id of the lottery they are leaving
     * @param drawCompleted true if the winners have already been drawn
     * @param onResult called with the outcome
     */
    fun leaveWaitlist(
        entrant: User,
        lotteryId: String,
        drawCompleted: Boolean,
        onResult: (LeaveResult) -> Unit
    ) {
        if (entrant.deviceId.isEmpty() || lotteryId.isEmpty()) {
            onResult(LeaveResult.Failure("Missing entrant or lottery"))
            return
        }
        if (drawCompleted) {
            onResult(LeaveResult.DrawCompleted)
            return
        }

        val doc = waitlistRef.document(entryId(lotteryId, entrant.deviceId))
        doc.get()
            .addOnSuccessListener { existing ->
                if (!existing.exists()) {
                    onResult(LeaveResult.NotOnWaitlist)
                    return@addOnSuccessListener
                }
                doc.delete()
                    .addOnSuccessListener { onResult(LeaveResult.Success) }
                    .addOnFailureListener { e ->
                        onResult(LeaveResult.Failure(e.message ?: "Could not leave the waitlist"))
                    }
            }
            .addOnFailureListener { e ->
                onResult(LeaveResult.Failure(e.message ?: "Could not leave the waitlist"))
            }
    }

    /**
     * Loads every waitlist the entrant has joined, newest first
     *
     * @param deviceId device id of the entrant
     * @param onDone called with the entries
     * @param onError called if the read failed
     */
    fun getJoinedLotteries(
        deviceId: String,
        onDone: (List<WaitlistEntry>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        waitlistRef.whereEqualTo("deviceId", deviceId)
            .get()
            .addOnSuccessListener { snapshot ->
                val joined = snapshot.documents
                    .mapNotNull { it.toObject(WaitlistEntry::class.java) }
                    .sortedByDescending { it.joinedAt }
                onDone(joined)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    suspend fun getEntriesForLottery(lotteryId: String): List<WaitlistEntry> {
        val snapshot = waitlistRef.whereEqualTo("lotteryId", lotteryId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(WaitlistEntry::class.java) }
    }

    fun createLottery(
        campsiteIds: List<String>,
        numWinners: Int,
        openDate: Long,
        closeDate: Long,
    ) {
        val lotteriesRef = db.collection("lotteries")

        val lottery = Lottery(
            campsites = campsiteIds,
            numWinners = numWinners,
            openDate = openDate,
            closeDate = closeDate,
        )

        lotteriesRef.add(lottery)
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
    }


}
