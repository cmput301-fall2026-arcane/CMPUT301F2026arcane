package com.example.code.rules

import com.example.code.model.Review
import com.example.code.model.Role
import java.util.Locale
import kotlin.math.roundToInt

/* US 01.13.01 As an entrant, I want to give a rating for a campsite
US 01.13.02 As an entrant, I want to post a review for a campsite
US 03.09.01 As an admin, I want to remove campsite ratings that violate app policy
US 03.10.01 As an admin, I want to remove campsite reviews that violate app policy
US 01.01.04 As an entrant, I want to see campsite summary information including name, location,
maximum occupancy, and rating

Checks a rating or review before it is saved, says who may remove one, and works out the stars shown
for a campsite. No Firestore calls so they can be unit tested. Any entrant may rate or review any
campsite because the stories do not ask for a past stay
*/

/** The fewest stars a rating can have */
const val MIN_RATING = 1

/** The most stars a rating can have */
const val MAX_RATING = 5

/** The most characters a review can have */
const val MAX_REVIEW_LENGTH = 500

/**
 * The Firestore doc id for a review. One per entrant per campsite so a second review replaces the
 * first
 *
 * @param campsiteId id of the campsite being reviewed
 * @param deviceId id of the entrant writing the review
 * @return the doc id
 */
fun reviewId(campsiteId: String, deviceId: String): String = "${campsiteId}_$deviceId"

/**
 * Checks the stars an entrant picked
 *
 * @param rating the stars picked, must be from 1 to 5
 * @return the message to show the entrant, null if the rating can be saved
 */
fun validateRating(rating: Int): String? =
    if (rating !in MIN_RATING..MAX_RATING) "Pick a rating from 1 to 5 stars" else null

/**
 * Checks the review text an entrant typed
 *
 * @param text the review as typed, must not be blank or longer than the limit
 * @return the message to show the entrant, null if the review can be saved
 */
fun validateReviewText(text: String): String? {
    if (text.isBlank()) return "Write something for your review"
    if (text.trim().length > MAX_REVIEW_LENGTH) return "Review must be $MAX_REVIEW_LENGTH characters or fewer"
    return null
}

/**
 * Checks if the entrant gave a star rating
 *
 * @param review the review to check
 * @return true if it has a rating
 */
fun hasRating(review: Review): Boolean = review.rating >= MIN_RATING

/**
 * Checks if the entrant wrote review text
 *
 * @param review the review to check
 * @return true if it has text
 */
fun hasReviewText(review: Review): Boolean = review.text.isNotBlank()

/**
 * Checks if nothing is left in a review, after an admin has removed the rating or the text, so the
 * doc can be deleted
 *
 * @param review the review to check
 * @return true if it has neither a rating nor text
 */
fun isEmptyReview(review: Review): Boolean = !hasRating(review) && !hasReviewText(review)

/**
 * Checks who may remove ratings and reviews
 *
 * @param role the role of the logged in user
 * @return true only for an admin
 */
fun canRemoveReviews(role: Role): Boolean = role == Role.ADMIN

/**
 * Averages the star ratings given. Reviews with no rating, text only, are left out
 *
 * @param reviews the reviews of a campsite
 * @return the average, 0.0 if nobody has rated
 */
fun averageRating(reviews: List<Review>): Double {
    val rated = reviews.filter { hasRating(it) }
    return if (rated.isEmpty()) 0.0 else rated.sumOf { it.rating }.toDouble() / rated.size
}

/**
 * The whole stars to draw for a campsite
 *
 * @param reviews the reviews of a campsite
 * @return the average rounded to the nearest star, from 0 to 5
 */
fun starsFor(reviews: List<Review>): Int = averageRating(reviews).roundToInt().coerceIn(0, MAX_RATING)

/**
 * The text shown next to the stars
 *
 * @param reviews the reviews of a campsite
 * @return the average and how many ratings, such as 4.5 (12 ratings), or No ratings yet
 */
fun ratingLabel(reviews: List<Review>): String {
    val count = reviews.count { hasRating(it) }
    if (count == 0) return "No ratings yet"
    val average = String.format(Locale.US, "%.1f", averageRating(reviews))
    return "$average ($count ${if (count == 1) "rating" else "ratings"})"
}