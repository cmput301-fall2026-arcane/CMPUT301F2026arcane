package com.example.code.Users

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

/**
 * A repository for users that manages interactions with a user or between users
 *
 * @property db the Firestore database to use
 * @property usersCollection the Firestore collection of users
 */
class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val usersCollection = db.collection("users")

    /**
     * Create a new user in the database.
     *
     * @param deviceId the device id of the user
     * @param name the name of the user
     * @param email the email of the user
     * @param phone the phone number of the user
     * @param roles a MutableList of roles of the user
     * @param notificationPreference whether the user wants notifications
     */
    fun createUser(
        deviceId: String,
        name: String,
        email: String,
        phone: String,
        roles: MutableList<Role>,
        notificationPreference: Boolean
    ) {
        val user = User(
            deviceId,
            name,
            email,
            phone,
            roles,
            notificationPreference
        )
        usersCollection.document(deviceId)
            .set(user)
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                //TODO: Passback status
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

        usersCollection.document(deviceId)
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
        usersCollection.document(deviceId)
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
        usersCollection.document(deviceId)
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
        usersCollection.get()
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
        usersCollection.document(deviceId)
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
        usersCollection.document(deviceId)
            .update("roles", FieldValue.arrayRemove(role))
            .addOnSuccessListener { documentReference ->
                // TODO: Passback status
            }
            .addOnFailureListener { e ->
                // TODO: Passback status
            }
    }
}