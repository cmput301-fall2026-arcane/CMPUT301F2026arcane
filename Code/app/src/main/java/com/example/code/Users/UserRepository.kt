package com.example.code.Users

import com.example.code.Role
import com.example.code.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject


class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val usersCollection = db.collection("users")


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
                // TODO: Toast success message
            }
            .addOnFailureListener { e ->
                //TODO: Toast error message
            }
    }

    fun updateUser(
        deviceId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        roles: MutableSet<Role> = mutableSetOf<Role>(),
        notificationPreference: Boolean? = null,
    ) {
        if (deviceId.isEmpty()) {
            // TODO: Toast "deviceId is empty" message
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
        if (!roles.isNotEmpty()) {
            updated["roles"] = roles
        }
        if (notificationPreference != null) {
           updated["notificationPreference"] = notificationPreference
        }

        usersCollection.document(deviceId)
            .update(
                updated
            )
            .addOnSuccessListener { documentReference ->
                // TODO: Toast success message
            }
            .addOnFailureListener { e ->
                //TODO: Toast error message
            }
    }

    fun deleteUser(deviceId: String) {
        if (deviceId.isEmpty()) {
            // TODO: Toast "deviceId is empty" message
            return
        }
        usersCollection.document(deviceId)
            .delete()
            .addOnSuccessListener { documentReference ->
                // TODO: Toast success message
            }
            .addOnFailureListener { e ->
                //TODO: Toast error message
            }
    }

    fun getUser(deviceId: String): User? {
        var user: User? = null
        usersCollection.document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                user = document.toObject<User>()
            }
            .addOnFailureListener { e ->
                // TODO: Toast user not found
            }

        return user
    }

    fun getAllUsers(): List<User> {
        var users = mutableListOf<User>()
        usersCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    users.add(document.toObject<User>())
                }
            }
            .addOnFailureListener { e ->
                // TODO: Toast error message
            }
        return users
    }

    fun addRole(deviceId: String, role: Role) {
        usersCollection.document(deviceId)
            .update("roles", FieldValue.arrayUnion(role))
            .addOnSuccessListener { documentReference ->
                // TODO: Toast success message
            }
            .addOnFailureListener { e ->
                //TODO: Toast error message
            }
    }

    fun removeRole(deviceId: String, role: Role) {
        usersCollection.document(deviceId)
            .update("roles", FieldValue.arrayRemove(role))
            .addOnSuccessListener { documentReference ->
                // TODO: Toast success message
            }
            .addOnFailureListener { e ->
                //TODO: Toast error message
            }
    }
}