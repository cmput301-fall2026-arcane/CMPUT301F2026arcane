package com.example.code.ui.start

import org.junit.Assert
import org.junit.Test

// US 01.09.01 - As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
class DeviceIdentityTest {

    @Test
    fun signUpWithoutDeviceIdIsRejected() {
        var code: Number? = null
        handleSignUp("Priya Sharma", "priya.sharma@ualberta.ca", "780-555-0142", "", "e") {
            code = it
        }
        Assert.assertEquals(1, code)
    }
}