package com.example.code

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/* Repository that reads and writes campsites in the Firestore "campsites" collection. It saves new
registered campsites and keeps a live list of old ones for owners. Nothing else touches the Firebase
for campsites besides this repository and the list is Compose state so the UI redraws when it changes

updating and viewing have not been built yet as well as error logging
 */

/**
 * Loads and creates campsites in Firestore
 *
 * @param db the Firestore instance to use
 * */
class CampsiteRepository (db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val campsitesRef = db.collection("campsites")
    private val _myCampsites = mutableStateListOf<Campsite>()
    private var listener: ListenerRegistration? = null

    val myCampsites: List<Campsite> get() = _myCampsites

    /**
     * Starts a live listener on the campsites whose ownerId matches
     * Any listener already running is stopped first
     *
     * @param ownerId device id of the owner whose campsites need to be loaded
     */
    fun listenToOwner(ownerId: String) {
        listener?.remove()
        listener = campsitesRef.whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val campsites = snapshot.documents
                    .mapNotNull { it.toObject(Campsite::class.java) }
                    .sortedByDescending { it.createdAt }
                _myCampsites.clear()
                _myCampsites.addAll(campsites)


            }
    }

    /**
     * Saves to Firestore as a new campsite owned by the owner id.
     * The id and creation time are filled in
     *
     * @param ownerId device id of the owner registering the campsite
     * @param draft the campsite details entered on the register screen
     * @param onResult called with true if the save succeeded and false if it failed
     */
    fun registerCampsite(ownerId: String, draft: Campsite, onResult: (Boolean) -> Unit) {
        val doc = campsitesRef.document()
        val campsite = draft.copy(
            id = doc.id,
            ownerId = ownerId,
            createdAt = System.currentTimeMillis()
        )
        doc.set(campsite)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}