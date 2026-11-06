package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampsiteValidationTest {

    private fun valid(
        name: String = "Pine Hollow",
        location: String = "Canmore",
        maxOccupancy: String = "6",
        price: String = "45.50",
        latitude: Double = 51.09,
        longitude: Double = -115.36
    ) = validateCampsite(name, location, maxOccupancy, price, latitude, longitude)

    @Test
    fun aFullyFilledFormHasNoErrors() {
        assertTrue(valid().isEmpty())
    }

    @Test
    fun emptyNameIsRejected() {
        assertEquals(listOf(CampsiteField.NAME), valid(name = ""))
    }

    @Test
    fun blankNameIsRejected() {
        assertEquals(listOf(CampsiteField.NAME), valid(name = "   "))
    }

    @Test
    fun emptyLocationIsRejected() {
        assertEquals(listOf(CampsiteField.LOCATION), valid(location = ""))
    }

    @Test
    fun occupancyMustBeANumber() {
        assertEquals(listOf(CampsiteField.MAX_OCCUPANCY), valid(maxOccupancy = "a few"))
    }

    @Test
    fun occupancyCannotBeAFraction() {
        assertEquals(listOf(CampsiteField.MAX_OCCUPANCY), valid(maxOccupancy = "4.5"))
    }

    @Test
    fun occupancyCannotBeZero() {
        assertEquals(listOf(CampsiteField.MAX_OCCUPANCY), valid(maxOccupancy = "0"))
    }

    @Test
    fun occupancyCannotBeNegative() {
        assertEquals(listOf(CampsiteField.MAX_OCCUPANCY), valid(maxOccupancy = "-2"))
    }

    @Test
    fun occupancyIgnoresSurroundingSpaces() {
        assertTrue(valid(maxOccupancy = " 6 ").isEmpty())
    }

    @Test
    fun priceMustBeANumber() {
        assertEquals(listOf(CampsiteField.PRICE), valid(price = "free"))
    }

    @Test
    fun priceCannotBeZero() {
        assertEquals(listOf(CampsiteField.PRICE), valid(price = "0"))
    }

    @Test
    fun priceCanBeAWholeNumber() {
        assertTrue(valid(price = "45").isEmpty())
    }

    @Test
    fun anUnpickedMapIsRejected() {
        assertEquals(listOf(CampsiteField.MAP_LOCATION), valid(latitude = 0.0, longitude = 0.0))
    }

    @Test
    fun latitudeOffTheGlobeIsRejected() {
        assertEquals(listOf(CampsiteField.MAP_LOCATION), valid(latitude = 91.0))
    }

    @Test
    fun longitudeOffTheGlobeIsRejected() {
        assertEquals(listOf(CampsiteField.MAP_LOCATION), valid(longitude = -181.0))
    }

    @Test
    fun everyErrorIsReportedAtOnce() {
        val errors = validateCampsite("", "", "", "", 0.0, 0.0)
        assertEquals(5, errors.size)
    }

    @Test
    fun aPointOnTheEdgeOfTheGlobeIsValid() {
        assertTrue(isMapLocationValid(90.0, 180.0))
    }

    @Test
    fun zeroZeroIsNotAPickedLocation() {
        assertFalse(isMapLocationValid(0.0, 0.0))
    }

    @Test
    fun tagsAreTrimmedAndLowerCased() {
        assertEquals(listOf("lake", "quiet"), normalizeTags(" Lake , QUIET "))
    }

    @Test
    fun repeatedTagsAreOnlyKeptOnce() {
        assertEquals(listOf("lake"), normalizeTags("lake, Lake, LAKE"))
    }

    @Test
    fun emptyTagsAreDropped() {
        assertEquals(listOf("lake"), normalizeTags("lake,,  ,"))
    }

    @Test
    fun noTagsGivesAnEmptyList() {
        assertTrue(normalizeTags("").isEmpty())
    }

    @Test
    fun everyFieldHasAMessage() {
        for (field in CampsiteField.entries) {
            assertTrue(errorFor(field).isNotEmpty())
        }
    }
}
