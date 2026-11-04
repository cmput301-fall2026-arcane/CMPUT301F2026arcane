package com.example.code

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

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

    /**
     * Update a user in the database. Parameters are optional and are ignored if not provided.
     *
     * @param deviceId the device id of the user to update
     * @param name the new name of the user
     * @param email the new email of the user
     * @param phone the new phone number of the user
     * @param notificationPreference whether the user wants notifications
     *
     * @see addRole
     * @see removeRole
     */
    fun updateUser(
        deviceId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        notificationPreference: Boolean? = null,
    ) {
        if (deviceId.isEmpty()) {
            // TODO: Passback status
            return
        }
        var updated = hashMapOf<String, Any>()
        if (name != null) {
            updated["name"] = name
        }
        if (email != null) {
            updated["email"] = email
        }
        if (phone != null) {
            updated["phone"] = phone
        }
        if (notificationPreference != null) {
            updated["notificationPreference"] = notificationPreference
        }

        usersRef.document(deviceId)
            .update(
                updated
            )
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                //TODO: Passback status
            }
    }

    /**
     * Delete a user from the database.
     *
     * @param deviceId the device ID of the user to delete
     */
    fun deleteUser(deviceId: String) {
        if (deviceId.isEmpty()) {
            // TODO: Passback status
            return
        }
        usersRef.document(deviceId)
            .delete()
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                //TODO: Passback status
            }
    }

    /**
     * Retrieves a user's information from the database.
     *
     * @param deviceId the device ID of the user to retrieve
     *
     * @return the user's information
     */
    fun getUser(deviceId: String): User? {
        var user: User? = null
        usersRef.document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                user = document.toObject<User>()
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }

        return user
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a List of all users
     */
    fun getAllUsers(): List<User> {
        var users = mutableListOf<User>()
        usersRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    users.add(document.toObject<User>())
                }
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
        return users
    }

    /**
     * Adds a role to a user in the database.
     *
     * @param deviceId  the device ID of the user to add the role to
     * @param role the role to add
     *
     * @see removeRole
     * @see updateUser
     */
    fun addRole(deviceId: String, role: Role) {
        usersRef.document(deviceId)
            .update("roles", FieldValue.arrayUnion(role))
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
    }

    /**
     * Removes a role from a user in the database.
     *
     * @param deviceId  the device ID of the user to remove the role from
     * @param role the role to remove
     *
     * @see addRole
     * @see updateUser
     */
    fun removeRole(deviceId: String, role: Role) {
        usersRef.document(deviceId)
            .update("roles", FieldValue.arrayRemove(role))
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
    }
}