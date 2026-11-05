package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery

Reads the "waitlist" collection and keeps a live list of the entries for one lottery. The list is
Compose state so the UI redraws as entrants join or leave
*/

/**
 * The live waitlist for a single campsite lottery
 *
 * @param capacity the capacity set by the owner, 0 when none is set
 * @param db the Firestore instance to use
 * */
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

    /**
     * @return the entrants currently on the waitlist, in the order they joined
     */
    fun getEntrants(): List<WaitlistEntry> {
        return entries
    }

    /**
     * @return true if the waitlist has reached its capacity
     */
    fun isFull(): Boolean {
        return isWaitlistFull(_entries.size, capacity)
    }
}
