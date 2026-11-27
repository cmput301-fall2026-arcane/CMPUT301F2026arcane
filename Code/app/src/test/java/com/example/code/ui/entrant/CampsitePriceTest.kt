package com.example.code.ui.entrant

import org.junit.Assert
import org.junit.Test

// US 01.01.05 - As an entrant, I want to see details of a campsite.
class CampsitePriceTest {

    @Test
    fun wholePriceShowsNoDecimals() {
        Assert.assertEquals("$75", formatPrice(75.0))
    }

    @Test
    fun partialPriceShowsTwoDecimals() {
        Assert.assertEquals("$38.50", formatPrice(38.5))
    }

    @Test
    fun centsAreKept() {
        Assert.assertEquals("$42.99", formatPrice(42.99))
    }
}