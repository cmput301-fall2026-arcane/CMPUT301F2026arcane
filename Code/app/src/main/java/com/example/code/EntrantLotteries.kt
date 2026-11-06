package com.example.code

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/* US 01.01.06 As an entrant, I want to join an active lottery from the campsite details screen
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery

Puts the lottery pieces together for the entrant screens: finds the right lottery, runs the draw
if it's due, works out the entrant's status, and enters, leaves or reserves. The Firestore work is
done by LotteryRepository, ReservationRepository, CampsiteWaitlist and LotteryDraw
*/

/**
 * The signed in entrant, built from the login details stored in MainActivity
 */
fun currentEntrant(): User = User(deviceId = logged_in_device, name = logged_in_name)

/**
 * Where the entrant stands with the lottery shown on a campsite's details screen
 *
 * @property lottery the lottery shown, or null if the campsite isn't in one
 * @property status the entrant's status in it
 * @property reservation the entrant's reservation from it, or null
 */
data class CampsiteLotteryState(
    val lottery: Lottery?,
    val status: EntrantLotteryStatus,
    val reservation: Reservation?
)

/**
 * One row on the "Your Active Lotteries and Waitlists" screen
 *
 * @property lottery the lottery the entrant entered
 * @property campsite the campsite to show for it, or null if it couldn't be loaded
 * @property status the entrant's status in it
 * @property reservation the entrant's reservation from it, or null
 */
data class MyLotteryRow(
    val lottery: Lottery,
    val campsite: Campsite?,
    val status: EntrantLotteryStatus,
    val reservation: Reservation?
)

/**
 * The lottery actions available to an entrant
 */
class EntrantLotteries(
    private val lotteries: LotteryRepository = LotteryRepository(),
    private val reservations: ReservationRepository = ReservationRepository(),
    private val campsites: CampsiteRepository = CampsiteRepository(),
    private val waitlist: CampsiteWaitlist = CampsiteWaitlist(),
    private val draw: LotteryDraw = LotteryDraw()
) {
    /**
     * Works out which lottery to show on a campsite's details screen and where the entrant stands
     *
     * @param campsiteId id of the campsite being viewed
     * @param entrant the signed in entrant
     * @return the lottery and the entrant's status in it
     */
    suspend fun stateForCampsite(campsiteId: String, entrant: User): CampsiteLotteryState {
        val now = System.currentTimeMillis()
        val joinedIds = lotteries.myEntries(entrant.deviceId).map { it.lotteryId }.toSet()
        val resultIds = lotteries.myResults(entrant.deviceId).map { it.lotteryId }.toSet()
        val lottery = pickLotteryForCampsite(
            lotteries.lotteriesForCampsite(campsiteId), now, joinedIds + resultIds
        ) ?: return CampsiteLotteryState(null, EntrantLotteryStatus.NO_LOTTERY, null)

        drawIfDue(lottery, now)
        val result = myResult(lottery.id, entrant.deviceId)
        val reservation = myReservation(lottery.id, entrant.deviceId)
        val status = entrantLotteryStatus(now, lottery, lottery.id in joinedIds, result, reservation)
        return CampsiteLotteryState(lottery, status, reservation)
    }

    /**
     * Loads every lottery the entrant has entered for the lotteries screen, wins first
     *
     * @param entrant the signed in entrant
     * @return one row per lottery
     */
    suspend fun myLotteries(entrant: User): List<MyLotteryRow> {
        if (entrant.deviceId.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        val entered = lotteries.loadLotteries(lotteries.myEntries(entrant.deviceId).map { it.lotteryId })
        entered.forEach { drawIfDue(it, now) }

        // load these after drawing so new results are included
        val results = lotteries.myResults(entrant.deviceId).associateBy { it.lotteryId }
        val myReservations = reservations.myReservations(entrant.deviceId).associateBy { it.lotteryId }

        return entered.map { lottery ->
            val reservation = myReservations[lottery.id]
            // show the booked campsite if there is one, otherwise the lottery's first campsite
            val campsiteId = reservation?.campsiteId ?: lottery.campsites.firstOrNull()
            MyLotteryRow(
                lottery = lottery,
                campsite = campsiteId?.let { campsites.loadCampsiteNow(it) },
                status = entrantLotteryStatus(now, lottery, true, results[lottery.id], reservation),
                reservation = reservation
            )
        }.sortedBy { myLotteryOrder(it.status) }
    }

    /**
     * Enters the entrant into a lottery by adding them to its waitlist
     */
    suspend fun enter(lottery: Lottery, entrant: User): JoinResult =
        suspendCancellableCoroutine { cont ->
            waitlist.joinWaitlist(entrant, lottery.id, lottery.openDate, lottery.closeDate) {
                if (cont.isActive) cont.resume(it)
            }
        }

    /**
     * Takes the entrant out of a lottery's waitlist. Not allowed once the draw has happened
     */
    suspend fun leave(lottery: Lottery, entrant: User): LeaveResult =
        suspendCancellableCoroutine { cont ->
            waitlist.leaveWaitlist(entrant, lottery.id, lottery.isDrawn) {
                if (cont.isActive) cont.resume(it)
            }
        }

    /**
     * Books a campsite for a lottery winner
     *
     * @return the saved reservation
     */
    suspend fun reserve(
        entrant: User,
        lottery: Lottery,
        campsite: Campsite,
        startDate: Long,
        endDate: Long
    ): Reservation = reservations.reserve(entrant, lottery.id, campsite, startDate, endDate)

    private suspend fun drawIfDue(lottery: Lottery, now: Long) {
        if (isDrawDue(now, lottery.closeDate, lottery.isDrawn)) {
            // LotteryDraw checks again in Firestore, so two phones drawing at once is safe
            runCatching { draw.runDrawIfDue(lottery) }
        }
    }

    private suspend fun myResult(lotteryId: String, deviceId: String): LotteryResult? {
        if (deviceId.isEmpty()) return null
        return runCatching { draw.getMyResult(lotteryId, deviceId) }.getOrNull()
    }

    private suspend fun myReservation(lotteryId: String, deviceId: String): Reservation? {
        if (deviceId.isEmpty()) return null
        return reservations.loadReservation(reservationId(lotteryId, deviceId))
    }
}

/**
 * Loads one campsite and waits for it, for code that runs in a coroutine
 *
 * @param id the campsite's id
 * @return the campsite, or null if it doesn't exist or couldn't be loaded
 */
suspend fun CampsiteRepository.loadCampsiteNow(id: String): Campsite? =
    suspendCancellableCoroutine { cont ->
        loadCampsite(
            id = id,
            onDone = { if (cont.isActive) cont.resume(it) },
            onError = { if (cont.isActive) cont.resume(null) }
        )
    }

/**
 * A message to show after trying to enter a lottery
 */
fun joinMessage(result: JoinResult): String {
    return when (result) {
        JoinResult.Success -> "You're entered in the lottery!"
        JoinResult.AlreadyJoined -> "You're already entered in this lottery"
        JoinResult.RegistrationClosed -> "Registration for this lottery is closed"
        JoinResult.WaitlistFull -> "This lottery is full"
        is JoinResult.Failure ->
            if (logged_in_device.isEmpty()) "Sign in to enter lotteries" else result.message
    }
}

/**
 * A message to show after trying to leave a lottery
 */
fun leaveMessage(result: LeaveResult): String {
    return when (result) {
        LeaveResult.Success -> "You've left the lottery"
        LeaveResult.NotOnWaitlist -> "You weren't entered in this lottery"
        LeaveResult.DrawCompleted -> "The draw has already happened, so you can't leave now"
        is LeaveResult.Failure -> result.message
    }
}