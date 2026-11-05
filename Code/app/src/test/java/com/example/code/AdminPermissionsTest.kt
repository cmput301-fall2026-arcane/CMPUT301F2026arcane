package com.example.code

import org.junit.Test

import org.junit.Assert.*

/**
 * Unit tests for the admin profile permission checks
 */
class AdminPermissionsTest {

    private fun user(deviceId: String, vararg roles: Role): User {
        return User(deviceId = deviceId, roles = roles.toMutableList())
    }

    @Test
    fun adminCanRemoveProfiles() {
        assertTrue(canRemoveProfiles(user("admin-1", Role.ADMIN)))
    }

    @Test
    fun entrantCannotRemoveProfiles() {
        assertFalse(canRemoveProfiles(user("entrant-1", Role.ENTRANT)))
    }

    @Test
    fun campOwnerCannotRemoveProfiles() {
        assertFalse(canRemoveProfiles(user("owner-1", Role.CAMP_OWNER)))
    }

    @Test
    fun adminWithOtherRolesCanRemoveProfiles() {
        assertTrue(canRemoveProfiles(user("admin-2", Role.ENTRANT, Role.ADMIN)))
    }

    @Test
    fun userWithNoRolesCannotRemoveProfiles() {
        assertFalse(canRemoveProfiles(user("nobody")))
    }

    @Test
    fun sameDeviceIdIsSelf() {
        val actor = user("device-1", Role.ADMIN)
        assertTrue(isSelf(actor, actor))
    }

    @Test
    fun differentDeviceIdIsNotSelf() {
        val actor = user("device-1", Role.ADMIN)
        val target = user("device-2", Role.ENTRANT)
        assertFalse(isSelf(target, actor))
    }

    @Test
    fun emptyDeviceIdIsNotSelf() {
        val actor = user("", Role.ADMIN)
        val target = user("", Role.ENTRANT)
        assertFalse(isSelf(target, actor))
    }
}
