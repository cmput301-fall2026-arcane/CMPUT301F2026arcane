package com.example.code.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.NotificationType
import com.example.code.rules.notificationTitle
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// US 01.03.01 - As an entrant, I want to receive a notification when I win the campsite lottery.
// US 01.03.02 - As an entrant, I want to receive a notification when I lose the campsite lottery.
// US 01.05.02 - As an entrant, I want to receive reservation by notification or downloadable CSV.
@RunWith(AndroidJUnit4::class)
class NotificationCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Builds the card the same way NotificationScreen does, title from the notification type
    private fun showCard(type: NotificationType, body: String, onClick: () -> Unit = {}) {
        composeRule.setContent {
            NotificationCard(
                title = notificationTitle(type),
                body = body,
                time = "Nov 26, 9:00 AM",
                unread = true,
                onClick = onClick
            )
        }
    }

    // US 01.03.01
    @Test
    fun winNotificationIsShown() {
        showCard(NotificationType.LOTTERY_WON, "You won the campsite lottery!")
        composeRule.onNodeWithText("You won the lottery!").assertIsDisplayed()
        composeRule.onNodeWithText("You won the campsite lottery!").assertIsDisplayed()
    }

    // US 01.03.02
    @Test
    fun lossNotificationIsShown() {
        showCard(
            NotificationType.LOTTERY_LOST,
            "Unfortunately, you were not selected for this lottery. Try again next time."
        )
        composeRule.onNodeWithText("Lottery result").assertIsDisplayed()
        composeRule.onNodeWithText("Unfortunately, you were not selected for this lottery. Try again next time.")
            .assertIsDisplayed()
    }

    // US 01.05.02
    @Test
    fun reservationNotificationIsShown() {
        showCard(
            NotificationType.RESERVATION_CONFIRMED,
            "Your reservation for Two Jack Lakeside has been confirmed!"
        )
        composeRule.onNodeWithText("Reservation confirmed").assertIsDisplayed()
        composeRule.onNodeWithText("Your reservation for Two Jack Lakeside has been confirmed!")
            .assertIsDisplayed()
    }

    @Test
    fun tappingANotificationOpensIt() {
        var opened = false
        showCard(NotificationType.LOTTERY_WON, "You won the campsite lottery!", onClick = { opened = true })
        composeRule.onNodeWithText("You won the lottery!").performClick()
        assertTrue(opened)
    }
}
