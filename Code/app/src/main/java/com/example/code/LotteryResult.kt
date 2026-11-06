package com.example.code

/* US 02.05.01 As a campsite owner, I want the system to automatically draw winners after the
registration period ends

One entrant's result in one lottery. A result is written for everyone on the waitlist so the
winners and the losers both have something to be told about. Stored in the "results" collection
*/
data class LotteryResult (
    val lotteryId: String = "",
    val deviceId: String = "",
    val name: String = "",
    val won: Boolean = false,
    val drawnAt: Long = 0L,
    val notified: Boolean = false
)
