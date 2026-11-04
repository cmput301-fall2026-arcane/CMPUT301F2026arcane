package com.example.code

/* This is a model class for a user of the app. They are identified by their device id instead
of a username and password. UserRepository creates a user on first launch and stores the user in
Firestore using the device id as the document id

Not everything has been modeled yet
*/

enum class Role {
    ADMIN,
    CAMP_OWNER,
    ENTRANT
}

/**
 * A user is identified with their device id
 *
 * @property deviceId Android device id used as the Firestore document id
 * @property name display name of the user
 * @property email email of the user
 * @property phone phone number of the user
 * @property createdAt creation time
 * @property roles set of roles the user has
 * */
open class User (
    val deviceId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var roles: MutableList<Role> = mutableListOf<Role>(Role.ENTRANT),
    var notificationPreference: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
) {
//    TODO: Make image class
//    private var images = mutableListOf<Image>()

    fun hasRole(role: Role): Boolean {
        return roles.contains(role)
    }
}



