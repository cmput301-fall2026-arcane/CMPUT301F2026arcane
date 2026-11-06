package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Test

// US 01.01.05 - As an entrant, I want to see details of a campsite.
class CampsitePriceTest {

    @Test
    fun wholePriceShowsNoDecimals() {
        assertEquals("$75", formatPrice(75.0))
    }

    @Test
    fun partialPriceShowsTwoDecimals() {
        assertEquals("$38.50", formatPrice(38.5))
    }

    @Test
    fun centsAreKept() {
        assertEquals("$42.99", formatPrice(42.99))
    }
}
