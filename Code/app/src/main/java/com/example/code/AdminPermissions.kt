package com.example.code

/* Permission checks for the admin profile actions. No Firestore calls so they can be unit tested
*/

/**
 * Checks whether a user is allowed to remove profiles
 *
 * @param actor the user performing the action
 * @return true if the actor is an admin
 */
fun canRemoveProfiles(actor: User): Boolean {
    return actor.hasRole(Role.ADMIN)
}

/**
 * Checks whether a profile belongs to the actor, used to warn an admin before they remove
 * their own profile
 *
 * @param target the profile being viewed
 * @param actor the user performing the action
 * @return true if both have the same non empty device id
 */
fun isSelf(target: User, actor: User): Boolean {
    return target.deviceId.isNotEmpty() && target.deviceId == actor.deviceId
}
