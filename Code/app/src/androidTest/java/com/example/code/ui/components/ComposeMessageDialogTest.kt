package com.example.code.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// US 02.10.01 - As a campsite owner, I want to send notifications to entrants who have joined the waitlist for my campsite lottery.
@RunWith(AndroidJUnit4::class)
class ComposeMessageDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showDialog(onDismiss: () -> Unit = {}, onSend: (String) -> Unit = {}) {
        composeRule.setContent {
            ComposeMessageDialog(recipients = 3, onDismiss = onDismiss, onSend = onSend)
        }
    }

    @Test
    fun dialogShowsRecipientCount() {
        showDialog()
        composeRule.onNodeWithText("Message 3 entrants").assertIsDisplayed()
    }

    @Test
    fun cannotSendABlankMessage() {
        showDialog()
        composeRule.onNodeWithText("Send").assertIsNotEnabled()
    }

    @Test
    fun sendingPassesTheTrimmedMessage() {
        var sent: String? = null
        showDialog(onSend = { sent = it })
        composeRule.onNode(hasSetTextAction()).performTextInput("  Road to Two Jack closes Friday  ")
        composeRule.onNodeWithText("Send").performClick()
        assertEquals("Road to Two Jack closes Friday", sent)
        composeRule.onNodeWithText("Sending…").assertIsDisplayed()
    }

    @Test
    fun cancelClosesTheDialog() {
        var dismissed = false
        showDialog(onDismiss = { dismissed = true })
        composeRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
