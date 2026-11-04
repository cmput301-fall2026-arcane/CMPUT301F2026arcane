package com.example.code

import com.google.firebase.firestore.FirebaseFirestore

/* The user repository handles all of the reading and writing to Firestore in the "users"
collection. Each user document will use the device id as the document id. The app will call this
repository when it wants to access Firebase. MainActivity calls it once at launch to create or load
a user

Updating and deleting have not been built yet
* */
/**
 * Loads and creates users in Firestore keyed by device id
 *
 * @param db the Firestore instance to use
 * */
class UserRepository (db: FirebaseFirestore = FirebaseFirestore.getInstance()){
    private val usersRef = db.collection("users")

    /**
     * Loads the user for the specific device id  or creates and saves a new one on launch
     *
     * @param deviceId the Android device id of the current id
     * @param onDone called with the user
     */
    fun getOrCreateUser(deviceId: String, onDone: (User) -> Unit) {
        usersRef.document(deviceId).get().addOnSuccessListener { doc ->
            val existing = doc.toObject(User:: class.java)
            if (existing != null ) {
               onDone(existing)
            } else {
                val user = User(deviceId = deviceId, createdAt = System.currentTimeMillis())
                usersRef.document(deviceId).set(user).addOnSuccessListener { onDone(user) }
            }
        }
    }
}