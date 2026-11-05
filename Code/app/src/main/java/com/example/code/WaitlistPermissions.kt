package com.example.code

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery

Checks for the waitlist screen. No Firestore calls so they can be unit tested
*/

fun canViewWaitlist(actor: User, campsite: Campsite): Boolean {
    return campsite.ownerId.isNotEmpty() && campsite.ownerId == actor.deviceId
}

fun isWaitlistFull(entrantCount: Int, capacity: Int): Boolean {
    return capacity > 0 && entrantCount >= capacity
}
