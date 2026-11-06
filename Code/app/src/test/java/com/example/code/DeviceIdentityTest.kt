package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Test

// US 01.09.01 - As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
class DeviceIdentityTest {

    @Test
    fun signUpWithoutDeviceIdIsRejected() {
        var code: Number? = null
        handleSignUp("Priya Sharma", "priya.sharma@ualberta.ca", "780-555-0142", "", "e") { code = it }
        assertEquals(1, code)
    }
}
