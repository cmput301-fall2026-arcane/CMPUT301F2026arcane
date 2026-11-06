package com.example.code

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// US 01.01.04 - As an entrant, I want to see campsite summary information including name, location, maximum occupancy, and rating.
@RunWith(AndroidJUnit4::class)
class CampsiteSummaryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val campsite = Campsite(
        id = "c1",
        name = "Two Jack Lakeside",
        location = "Banff",
        maxOccupancy = 6,
        pricePerNight = 38.50
    )

    private fun showCard(site: Campsite) {
        composeRule.setContent {
            CampsiteCard(item = CampsiteListItem(site), onClick = {})
        }
    }

    @Test
    fun cardShowsCampsiteName() {
        showCard(campsite)
        composeRule.onNodeWithText("Two Jack Lakeside").assertIsDisplayed()
    }

    @Test
    fun shortNameIsNotTruncated() {
        showCard(campsite.copy(name = "Boulton Creek"))
        composeRule.onNodeWithText("Boulton Creek").assertIsDisplayed()
    }

    @Test
    fun longNameStaysOnCard() {
        showCard(campsite.copy(name = "Lake Louise Hard-Sided Trailer Campground"))
        composeRule.onNodeWithText("Lake Louise Hard-Sided Trailer Campground").assertIsDisplayed()
    }

    @Test
    fun eachCampsiteInListShowsItsName() {
        val sites = listOf(
            Campsite(id = "c1", name = "Two Jack Lakeside", location = "Banff"),
            Campsite(id = "c2", name = "Whistlers Campground", location = "Jasper"),
            Campsite(id = "c4", name = "Boulton Creek", location = "Kananaskis")
        )
        composeRule.setContent {
            Column {
                sites.forEach { CampsiteCard(item = CampsiteListItem(it), onClick = {}) }
            }
        }
        sites.forEach { composeRule.onNodeWithText(it.name).assertIsDisplayed() }
    }
}
