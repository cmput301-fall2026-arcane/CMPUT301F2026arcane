package com.example.code

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

// US 01.01.05 - As an entrant, I want to see details of a campsite.
// US 01.01.06 - As an entrant, I want to join an active lottery from the campsite details screen.
@RunWith(AndroidJUnit4::class)
class CampsiteDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val campsite = Campsite(
        id = "c3",
        ownerId = "owner-device-42",
        name = "Two Jack Lakeside",
        location = "Banff",
        maxOccupancy = 6,
        pricePerNight = 38.50,
        amenities = mutableListOf("Fire pit", "Potable water", "Flush toilets"),
        description = "Lakeside tent sites with views of Mount Rundle. Fire pits, potable water and flush toilets nearby."
    )

    private fun showDetails(
        site: Campsite = campsite,
        lotteryOpen: Boolean = true,
        onBack: () -> Unit = {},
        onEnter: () -> Unit = {}
    ) {
        composeRule.setContent {
            CampsiteDetailsCard(campsite = site, lotteryOpen = lotteryOpen, onBack = onBack, onEnter = onEnter)
        }
    }

    private fun shown(text: String) {
        composeRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clickingCampsiteInListOpensDetails() {
        var clicked = false
        composeRule.setContent {
            CampsiteCard(item = CampsiteListItem(campsite), onClick = { clicked = true })
        }
        composeRule.onNodeWithText("Two Jack Lakeside").performClick()
        assertTrue(clicked)
    }

    @Test
    fun detailsShowName() {
        showDetails()
        shown("Two Jack Lakeside")
    }

    @Test
    fun detailsShowLocation() {
        showDetails()
        shown("Location: Banff")
    }

    @Test
    fun detailsShowDescription() {
        showDetails()
        shown("Lakeside tent sites with views of Mount Rundle. Fire pits, potable water and flush toilets nearby.")
    }

    @Test
    fun detailsShowPlaceholderWhenNoDescription() {
        showDetails(site = campsite.copy(description = ""))
        shown("No description yet")
    }

    @Test
    fun detailsShowMaxOccupancy() {
        showDetails()
        shown("6")
        shown("👥")
        shown("Capacity: Space for 6 campers")
    }

    @Test
    fun detailsShowPricePerNight() {
        showDetails()
        shown("$38.50")
        shown("per/night")
    }

    @Test
    fun detailsShowRatingForUnratedCampsite() {
        showDetails()
        shown("☆☆☆☆☆")
    }

    @Test
    fun detailsShowActiveLottery() {
        showDetails(lotteryOpen = true)
        shown("ENTER LOTTERY")
    }

    @Test
    fun detailsShowWaitingListWhenNoActiveLottery() {
        showDetails(lotteryOpen = false)
        shown("JOIN WAITING LIST")
    }

    @Test
    fun backButtonReturnsToList() {
        var wentBack = false
        showDetails(onBack = { wentBack = true })
        composeRule.onNodeWithText("↩").performScrollTo().performClick()
        assertTrue(wentBack)
    }

    // US 01.01.06
    @Test
    fun clickingEnterLotteryJoinsLottery() {
        var joined = false
        showDetails(lotteryOpen = true, onEnter = { joined = true })
        composeRule.onNodeWithText("ENTER LOTTERY").performScrollTo().performClick()
        assertTrue(joined)
    }
}
