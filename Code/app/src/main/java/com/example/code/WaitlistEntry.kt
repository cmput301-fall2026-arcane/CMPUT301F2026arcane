package com.example.code

/* US 02.09.01 As a campsite owner, I want to view a list of entrants who have joined the waitlist
for my campsite lottery

One entrant on a campsite lottery waitlist. The entries are stored in the "waitlist" collection
*/
data class WaitlistEntry (
    val deviceId: String = "",
    val lotteryId: String = "",
    val name: String = "",
    val joinedAt: Long = 0L
)
