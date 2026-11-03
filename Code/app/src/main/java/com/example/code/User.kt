package com.example.code
/* This is a model class for a user of the app. They are identified by their device id instead
of a username and password. UserRepository creates a user on first launch and stores the user in
Firestore using the device id as the document id

Not everything has been modeled yet
*/

/**
 * A user is identified with their device id
 *
 * @property deviceId Android device id used as the Firestore document id
 * @property name display name of the user
 * @property email email of the user
 * @property phone phone number of the user
 * @property createdAt creation time
 * */
data class User (
    val deviceId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = 0L
)

