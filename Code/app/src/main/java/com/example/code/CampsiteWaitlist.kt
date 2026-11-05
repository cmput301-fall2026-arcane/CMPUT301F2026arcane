package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery

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


class CampsiteWaitlist (
    val capacity: Int = 0,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
}
