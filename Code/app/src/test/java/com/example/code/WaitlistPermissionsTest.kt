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
}
