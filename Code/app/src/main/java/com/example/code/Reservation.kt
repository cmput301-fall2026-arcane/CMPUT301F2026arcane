package com.example.code

/* US 01.06.01 As an entrant, I want to view a list of my active reservations
US 01.06.02 As an entrant, I want to view a list of my past reservations

One booking made by a lottery winner. Stored in the "reservations" collection under
reservationId(lotteryId, deviceId), so a winner can only hold one reservation per lottery
*/

/**
 * A campsite booking made by an entrant who won its lottery
 *
 * @property id Firestore doc id, the same as reservationId(lotteryId, deviceId)
 * @property lotteryId id of the lottery the entrant won
 * @property campsiteId id of the booked campsite
 * @property campsiteName name of the campsite when it was booked, so lists don't need to load it
 * @property deviceId device id of the entrant who booked
 * @property startDate first night, as UTC midnight millis from the date picker
 * @property endDate last night, as UTC midnight millis from the date picker
 * @property createdAt when the reservation was made
 * @property cancelled true once the entrant has cancelled it
 */
data class Reservation(
    val id: String = "",
    val lotteryId: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val deviceId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val createdAt: Long = 0L,
    val cancelled: Boolean = false
)