package com.example.code.ui.navigation

import com.example.code.model.Campsite
import com.example.code.rules.filterCampsites
import com.example.code.ui.entrant.CampsiteListItem
import org.junit.Assert
import org.junit.Test

// US 01.01.01 - As an entrant, I want to search for campsites.
class CampsiteSearchTest {
    private val campsites = listOf(
        CampsiteListItem(
            Campsite(
                id = "c1",
                name = "Tunnel Mountain Village",
                location = "Banff"
            )
        ),
        CampsiteListItem(
            Campsite(
                id = "c2",
                name = "Whistlers Campground",
                location = "Jasper"
            )
        ),
        CampsiteListItem(
            Campsite(
                id = "c3",
                name = "Two Jack Lakeside",
                location = "Banff"
            )
        ),
        CampsiteListItem(
            Campsite(
                id = "c4",
                name = "Boulton Creek",
                location = "Kananaskis"
            )
        ),
        CampsiteListItem(
            Campsite(
                id = "c5",
                name = "Wapiti Campground",
                location = "Jasper"
            )
        )
    )

    private fun ids(result: List<CampsiteListItem>) = result.map { it.campsite.id }

    @Test
    fun searchByNameReturnsMatchingCampsite() {
        val result = filterCampsites(campsites, "Boulton")
        Assert.assertEquals(listOf("c4"), ids(result))
    }

    @Test
    fun searchByPartialKeywordReturnsAllMatches() {
        val result = filterCampsites(campsites, "Campground")
        Assert.assertEquals(listOf("c2", "c5"), ids(result))
    }

    @Test
    fun searchIsCaseInsensitive() {
        val result = filterCampsites(campsites, "tunnel mountain")
        Assert.assertEquals(listOf("c1"), ids(result))
    }

    @Test
    fun searchByLocationReturnsCampsitesInThatArea() {
        val result = filterCampsites(campsites, "Banff")
        Assert.assertEquals(listOf("c1", "c3"), ids(result))
    }

    @Test
    fun searchIgnoresSurroundingWhitespace() {
        val result = filterCampsites(campsites, "  Wapiti  ")
        Assert.assertEquals(listOf("c5"), ids(result))
    }

    @Test
    fun emptySearchReturnsAllCampsites() {
        val result = filterCampsites(campsites, "")
        Assert.assertEquals(5, result.size)
    }

    @Test
    fun searchWithNoMatchReturnsEmptyList() {
        val result = filterCampsites(campsites, "Waterton")
        Assert.assertTrue(result.isEmpty())
    }
}