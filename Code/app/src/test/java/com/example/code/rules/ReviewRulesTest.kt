package com.example.code.rules

import com.example.code.model.Review
import com.example.code.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewRulesTest {
    private fun rated(rating: Int) = Review(campsiteId = "c1", deviceId = "d$rating", rating = rating)

    @Test
    fun reviewId_joinsCampsiteAndDevice() {
        assertEquals("c1_d1", reviewId("c1", "d1"))
    }

    @Test
    fun validateRating_acceptsOneToFive() {
        for (stars in 1..5) assertNull(validateRating(stars))
    }

    @Test
    fun validateRating_rejectsOutOfRange() {
        assertNotNull(validateRating(0))
        assertNotNull(validateRating(6))
    }

    @Test
    fun validateReviewText_rejectsBlank() {
        assertNotNull(validateReviewText(""))
        assertNotNull(validateReviewText("   "))
        assertNull(validateReviewText("Lovely spot"))
    }

    @Test
    fun validateReviewText_rejectsOverLimit() {
        assertNotNull(validateReviewText("a".repeat(MAX_REVIEW_LENGTH + 1)))
        assertNull(validateReviewText("a".repeat(MAX_REVIEW_LENGTH)))
    }

    @Test
    fun isEmptyReview_trueOnlyWhenNoRatingAndNoText() {
        assertTrue(isEmptyReview(Review()))
        assertFalse(isEmptyReview(Review(rating = 3)))
        assertFalse(isEmptyReview(Review(text = "nice")))
    }

    @Test
    fun canRemoveReviews_onlyAdmin() {
        assertTrue(canRemoveReviews(Role.ADMIN))
        assertFalse(canRemoveReviews(Role.ENTRANT))
        assertFalse(canRemoveReviews(Role.CAMP_OWNER))
    }

    @Test
    fun averageRating_isZeroWithNoRatings() {
        assertEquals(0.0, averageRating(emptyList()), 0.0001)
    }

    @Test
    fun averageRating_averagesTheRatings() {
        assertEquals(4.0, averageRating(listOf(rated(5), rated(3))), 0.0001)
    }

    @Test
    fun averageRating_ignoresTextOnlyReviews() {
        val textOnly = Review(campsiteId = "c1", deviceId = "dx", text = "nice")
        assertEquals(4.0, averageRating(listOf(rated(5), rated(3), textOnly)), 0.0001)
    }

    @Test
    fun starsFor_roundsToNearestStar() {
        assertEquals(0, starsFor(emptyList()))
        assertEquals(5, starsFor(listOf(rated(5), rated(4)))) // 4.5 rounds up
        assertEquals(3, starsFor(listOf(rated(3), rated(3), rated(4)))) // 3.33 rounds down
    }

    @Test
    fun ratingLabel_showsAverageAndCount() {
        assertEquals("No ratings yet", ratingLabel(emptyList()))
        assertEquals("5.0 (1 rating)", ratingLabel(listOf(rated(5))))
        assertEquals("4.0 (2 ratings)", ratingLabel(listOf(rated(5), rated(3))))
    }
}