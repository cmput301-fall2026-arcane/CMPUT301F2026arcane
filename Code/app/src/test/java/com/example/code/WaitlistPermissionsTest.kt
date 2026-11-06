package com.example.code

import org.junit.Test

import org.junit.Assert.*

/**
 * Unit tests for the waitlist checks
 */
class WaitlistPermissionsTest {

    private fun owner(deviceId: String): User {
        return User(deviceId = deviceId, roles = mutableListOf(Role.CAMP_OWNER))
    }

    private fun campsite(ownerId: String): Campsite {
        return Campsite(id = "campsite-1", ownerId = ownerId)
    }

    @Test
    fun ownerCanViewWaitlist() {
        assertTrue(canViewWaitlist(owner("device-1"), campsite("device-1")))
    }

    @Test
    fun otherOwnerCannotViewWaitlist() {
        assertFalse(canViewWaitlist(owner("device-2"), campsite("device-1")))
    }

    @Test
    fun ownerCanRemoveCampsite() {
        assertTrue(canRemoveCampsite(owner("device-1"), campsite("device-1")))
    }

    @Test
    fun otherOwnerCannotRemoveCampsite() {
        assertFalse(canRemoveCampsite(owner("device-2"), campsite("device-1")))
    }

    @Test
    fun adminCanRemoveCampsite() {
        assertTrue(canRemoveCampsite(owner("device-1"), campsite("device-1")))
    }

    @Test
    fun campsiteWithNoOwnerCannotBeViewed() {
        assertFalse(canViewWaitlist(owner(""), campsite("")))
    }

    @Test
    fun waitlistWithNoCapacityIsNeverFull() {
        assertFalse(isWaitlistFull(50, 0))
    }

    @Test
    fun waitlistBelowCapacityIsNotFull() {
        assertFalse(isWaitlistFull(4, 5))
    }

    @Test
    fun waitlistAtCapacityIsFull() {
        assertTrue(isWaitlistFull(5, 5))
    }

    @Test
    fun waitlistOverCapacityIsFull() {
        assertTrue(isWaitlistFull(6, 5))
    }

    @Test
    fun emptyWaitlistIsNotFull() {
        assertFalse(isWaitlistFull(0, 5))
    }

    @Test
    fun registrationIsOpenInsideThePeriod() {
        assertTrue(isRegistrationOpen(500L, 100L, 900L))
    }

    @Test
    fun registrationIsClosedBeforeItOpens() {
        assertFalse(isRegistrationOpen(50L, 100L, 900L))
    }

    @Test
    fun registrationIsClosedAfterItCloses() {
        assertFalse(isRegistrationOpen(900L, 100L, 900L))
    }

    @Test
    fun registrationIsOpenOnTheOpeningMoment() {
        assertTrue(isRegistrationOpen(100L, 100L, 900L))
    }

    @Test
    fun registrationWithNoPeriodIsClosed() {
        assertFalse(isRegistrationOpen(500L, 0L, 0L))
    }

    @Test
    fun entryIdCombinesLotteryAndDevice() {
        assertEquals("lottery-1_device-1", entryId("lottery-1", "device-1"))
    }

    @Test
    fun entryIdIsDifferentPerEntrant() {
        assertNotEquals(entryId("lottery-1", "device-1"), entryId("lottery-1", "device-2"))
    }

    @Test
    fun canLeaveWaitlistWhenJoinedBeforeTheDraw() {
        assertTrue(canLeaveWaitlist(true, false))
    }

    @Test
    fun cannotLeaveWaitlistWhenNotJoined() {
        assertFalse(canLeaveWaitlist(false, false))
    }

    @Test
    fun cannotLeaveWaitlistAfterTheDraw() {
        assertFalse(canLeaveWaitlist(true, true))
    }
}
