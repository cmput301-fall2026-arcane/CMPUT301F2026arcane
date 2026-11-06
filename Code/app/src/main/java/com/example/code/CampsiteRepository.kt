package com.example.code

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject

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

    private val _allCampsites = mutableStateListOf<Campsite>()
    private var allListener: ListenerRegistration? = null


    val liveCampsites: List<Campsite> get() = _allCampsites

    //Error Messages
    var allCampsitesLoaded by mutableStateOf(false)
        private set

    fun listenToAll() {
        allListener?.remove()
        allListener = campsitesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                allCampsitesLoaded = true
                return@addSnapshotListener
            }
            val campsites = snapshot.documents
                // runCatching skips a document with a badly typed field instead of crashing,
                // and copy(id = ...) fills the id in for documents added by hand in the console
                .mapNotNull { doc ->
                    runCatching { doc.toObject(Campsite::class.java) }
                        .onFailure { Log.e("CampsiteRepository", "Skipped campsite ${doc.id}", it) }
                        .getOrNull()?.copy(id = doc.id)
                }
                .sortedBy { it.name }
            _allCampsites.clear()
            _allCampsites.addAll(campsites)
            allCampsitesLoaded = true
        }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
        allListener?.remove()
        allListener = null
    }

    //Campsite Details
    fun loadCampsite(id: String, onDone: (Campsite?) -> Unit, onError: (Exception) -> Unit = {}) {
        if (id.isEmpty()) {
            onDone(null)
            return
        }
        campsitesRef.document(id).get()
            .addOnSuccessListener { doc ->
                val campsite = runCatching { doc.toObject(Campsite::class.java) }.getOrNull()
                onDone(campsite?.copy(id = doc.id))
            }
            .addOnFailureListener { e -> onError(e) }
    }

    /**
     * Starts a live listener on the campsites whose ownerId matches
     * Updates the live list of campsites when they change in the database
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

    /**
     * Update a campsite in the database. Parameters are optional and are ignored if not provided.
     *
     * @param id the id of the campsite to update
     * @param name the new name of the campsite
     * @param location the new location of the campsite
     * @param maxOccupancy the new maximum occupancy of the campsite
     * @param pricePerNight the new price per night of the campsite
     * @param amenities the new amenities of the campsite
     * @param description the new description of the campsite
     */
    fun updateCampsite(
        id: String,
        name: String? = null,
        location: String? = null,
        maxOccupancy: Int? = null,
        pricePerNight: Double? = null,
        amenities: MutableList<String>? = mutableListOf<String>(),
        description: String? = null,
    ) {
        if (id.isEmpty()) {
            // TODO: Passback status
            return
        }
        var updated = hashMapOf<String, Any>()
        if (name != null) {
            updated["name"] = name
        }
        if (location != null) {
            updated["location"] = location
        }
        if (maxOccupancy != null) {
            updated["maxOccupancy"] = maxOccupancy
        }
        if (pricePerNight != null) {
            updated["pricePerNight"] = pricePerNight
        }
        if (!amenities.isNullOrEmpty()) {
            updated["amenities"] = amenities
        }
        if (description != null) {
            updated["description"] = description
        }
        campsitesRef.document(id)
            .update(updated)
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                //TODO: Passback status
            }
    }

    /**
     * Delete a campsite from the database.
     *
     * @param id the id of the campsite to delete
     */
    fun deleteCampsite(id: String) {
        if (id.isEmpty()) {
            // TODO: Passback status
            return
        }
        campsitesRef.document(id)
            .delete()
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                //TODO: Passback status
            }
    }

    /**
     * Retrieves a campsite from the database.
     *
     * @param id the id of the campsite to retrieve
     *
     * @return the campsite
     */
    fun getCampsite(id: String): Campsite? {
        var campsite: Campsite? = null
        campsitesRef.document(id)
            .get()
            .addOnSuccessListener { document ->
                campsite = document.toObject<Campsite>()
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
        return campsite
    }

    /**
     * Retrieves all campsites from the database.
     *
     * @return a List of all campsites
     */
    fun getAllCampsites(): List<Campsite> {
        var campsites = mutableListOf<Campsite>()
        campsitesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    campsites.add(document.toObject<Campsite>())
                }
            }
        return campsites
    }


}