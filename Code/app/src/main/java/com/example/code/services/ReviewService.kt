package com.example.code.services

import com.example.code.logged_in_device
import com.example.code.logged_in_name
import com.example.code.logged_user_type
import com.example.code.model.Review
import com.example.code.repositories.ReviewRepository
import com.example.code.rules.canRemoveReviews
import com.example.code.rules.isEmptyReview
import com.example.code.rules.starsFor
import com.example.code.rules.validateRating
import com.example.code.rules.validateReviewText

/* US 01.13.01 As an entrant, I want to give a rating for a campsite
US 01.13.02 As an entrant, I want to post a review for a campsite
US 03.09.01 As an admin, I want to remove campsite ratings that violate app policy
US 03.10.01 As an admin, I want to remove campsite reviews that violate app policy
US 01.01.04 As an entrant, I want to see campsite summary information including name, location,
maximum occupancy, and rating

What the screens call for ratings and reviews. The rules check what was typed, this saves it through
ReviewRepository and works out the stars shown on the campsite screen and list. A rating and a review
are saved separately so giving one does not wipe the other. Admins can clear either one

There is no review form or admin screen yet, these are ready for them to call
*/

/**
 * The rating shown on a campsite
 *
 * @property stars  whole stars to draw, 0 if nobody has rated
 * @property count  how many ratings are behind the stars
 */
data class RatingSummary(val stars: Int = 0, val count: Int = 0)

/**
 * Gives, posts and removes ratings and reviews, and loads the rating for a campsite
 *
 * @param repo the repository that reads and writes reviews
 */
class CampsiteReviews(private val repo: ReviewRepository = ReviewRepository()) {

    /**
     * Loads the rating for one campsite, used by the star row on the campsite screen
     *
     * @param campsiteId id of the campsite
     * @return the stars and how many ratings are behind them
     */
    suspend fun ratingFor(campsiteId: String): RatingSummary = summarize(repo.reviewsForCampsite(campsiteId))

    /**
     * Loads the rating for every campsite that has reviews, used by the campsite list
     *
     * @return the ratings keyed by campsite id, campsites with no reviews are not in it
     */
    suspend fun ratingsByCampsite(): Map<String, RatingSummary> =
        repo.allReviews().groupBy { it.campsiteId }.mapValues { summarize(it.value) }

    /**
     * Loads every review of a campsite
     *
     * @param campsiteId id of the campsite
     * @return the reviews, newest first
     */
    suspend fun reviewsFor(campsiteId: String): List<Review> = repo.reviewsForCampsite(campsiteId)

    /**
     * Loads the logged in entrant's own review of a campsite
     *
     * @param campsiteId id of the campsite
     * @return the review, or null if they have not left one
     */
    suspend fun myReview(campsiteId: String): Review? = repo.myReview(campsiteId, logged_in_device)

    /**
     * Gives or changes the logged in entrant's star rating for a campsite
     *
     * @param campsiteId id of the campsite
     * @param rating the stars picked, from 1 to 5
     * @return the message to show the entrant, null if the rating was saved
     */
    suspend fun rate(campsiteId: String, rating: Int): String? {
        validateRating(rating)?.let { return it }
        val existing = repo.myReview(campsiteId, logged_in_device) ?: newReview(campsiteId)
        repo.saveReview(existing.copy(rating = rating))
        return null
    }

    /**
     * Posts or changes the logged in entrant's written review of a campsite
     *
     * @param campsiteId id of the campsite
     * @param text the review as typed
     * @return the message to show the entrant, null if the review was saved
     */
    suspend fun postReview(campsiteId: String, text: String): String? {
        validateReviewText(text)?.let { return it }
        val existing = repo.myReview(campsiteId, logged_in_device) ?: newReview(campsiteId)
        repo.saveReview(existing.copy(text = text.trim()))
        return null
    }

    /**
     * Admin only. Clears one entrant's star rating and leaves their review text alone
     *
     * @param campsiteId id of the campsite
     * @param deviceId device id of the entrant whose rating is removed
     * @return the message to show the admin, null if it was removed
     */
    suspend fun removeRating(campsiteId: String, deviceId: String): String? =
        moderate(campsiteId, deviceId) { it.copy(rating = 0) }

    /**
     * Admin only. Clears one entrant's review text and leaves their star rating alone
     *
     * @param campsiteId id of the campsite
     * @param deviceId device id of the entrant whose review is removed
     * @return the message to show the admin, null if it was removed
     */
    suspend fun removeReview(campsiteId: String, deviceId: String): String? =
        moderate(campsiteId, deviceId) { it.copy(text = "") }

    private suspend fun moderate(campsiteId: String, deviceId: String, clear: (Review) -> Review): String? {
        if (!canRemoveReviews(logged_user_type)) return "Only admins can remove reviews"
        val existing = repo.myReview(campsiteId, deviceId) ?: return null
        val updated = clear(existing)
        if (isEmptyReview(updated)) repo.deleteReview(campsiteId, deviceId) else repo.saveReview(updated)
        return null
    }

    private fun newReview(campsiteId: String) = Review(
        campsiteId = campsiteId,
        deviceId = logged_in_device,
        reviewerName = logged_in_name,
    )

    private fun summarize(reviews: List<Review>) =
        RatingSummary(stars = starsFor(reviews), count = reviews.count { it.rating > 0 })
}