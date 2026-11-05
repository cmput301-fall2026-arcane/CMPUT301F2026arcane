package com.example.code

import com.google.firebase.firestore.FirebaseFirestore

/* The admin actions on user profiles. It reads every profile in the "users" collection and
deletes one once the admin has confirmed

Profile image and waitlist removal are not built yet
*/

/**
 * The outcome of a profile removal
 */
sealed class RemovalResult {
    object Success : RemovalResult()
    object NotAuthorized : RemovalResult()
    data class Failure(val message: String) : RemovalResult()
}

/**
 * Loads and removes user profiles on behalf of a signed in admin
 *
 * @param actor the admin performing the actions
 * @param db the Firestore instance to use
 * */
class Admin (private val actor: User, db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val usersRef = db.collection("users")

    /**
     * Loads every profile, sorted by name so the list keeps a stable order
     *
     * @param onDone called with the loaded profiles
     * @param onError called if the read failed
     */
    fun getProfiles(onDone: (List<User>) -> Unit, onError: (Exception) -> Unit = {}) {
        usersRef.get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                    .mapNotNull { it.toObject(User::class.java) }
                    .sortedBy { it.name }
                onDone(users)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    /**
     * Deletes a profile. The screen is expected to have confirmed with the admin first
     *
     * @param user the profile to remove
     * @param onResult called with the outcome
     */
    fun removeUser(user: User, onResult: (RemovalResult) -> Unit) {
        if (!canRemoveProfiles(actor)) {
            onResult(RemovalResult.NotAuthorized)
            return
        }
        if (user.deviceId.isEmpty()) {
            onResult(RemovalResult.Failure("Profile has no device id"))
            return
        }

        // TODO: delete the profile image and drop the user from any waitlists
        usersRef.document(user.deviceId)
            .delete()
            .addOnSuccessListener { onResult(RemovalResult.Success) }
            .addOnFailureListener { e ->
                onResult(RemovalResult.Failure(e.message ?: "Could not remove the profile"))
            }
    }
}
