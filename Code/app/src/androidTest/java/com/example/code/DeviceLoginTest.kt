package com.example.code

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// US 01.09.01 - As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
@RunWith(AndroidJUnit4::class)
class DeviceLoginTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun showLogin() {
        composeRule.setContent {
            LoginScreen(rememberNavController(), rememberNavController())
        }
    }

    @Test
    fun loginIsOneButtonWithNoPassword() {
        composeRule.onNodeWithText("✔ Login").assertIsDisplayed()
        composeRule.onAllNodesWithText("Password", substring = true, ignoreCase = true).assertCountEquals(0)
        composeRule.onAllNodesWithText("Username", substring = true, ignoreCase = true).assertCountEquals(0)
    }

    @Test
    fun signUpAsksForProfileDetailsOnly() {
        composeRule.onNodeWithText("Sign Up").performClick()
        composeRule.onNodeWithText("Name").assertIsDisplayed()
        composeRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeRule.onNodeWithText("Phone Number").assertIsDisplayed()
        composeRule.onAllNodesWithText("Password", substring = true, ignoreCase = true).assertCountEquals(0)
    }
}
