package com.example.code.repositories

import android.util.Log
import com.example.code.model.Review
import com.example.code.rules.reviewId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/* Repository that reads and writes reviews in the Firestore "reviews" collection. There is one doc
per entrant per campsite holding the rating and the text. Nothing else touches Firebase for reviews
besides this repository, the rules and CampsiteReviews decide what gets saved
 */

/**
 * Loads, saves and deletes reviews in Firestore
 *
 * @param db the Firestore instance to use
 * */
class ReviewRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val TAG = "ReviewRepository"
    private val reviews = db.collection("reviews")

    /**
     * Saves a review to Firestore. Replaces any earlier review by the same entrant for the same
     * campsite
     *
     * @param review the review to save
     */
    suspend fun saveReview(review: Review) {
        reviews.document(reviewId(review.campsiteId, review.deviceId)).set(review).await()
        Log.d(TAG, "saved review for ${review.campsiteId}")
    }

    /**
     * Retrieves every review of a campsite from the database
     *
     * @param campsiteId the id of the campsite
     *
     * @return the reviews, newest first
     */
    suspend fun reviewsForCampsite(campsiteId: String): List<Review> =
        reviews.whereEqualTo("campsiteId", campsiteId).get().await()
            .toObjects(Review::class.java)
            .sortedByDescending { it.createdAt }

    /**
     * Retrieves every review of every campsite, used for the ratings on the campsite list
     *
     * @return a List of all reviews
     */
    suspend fun allReviews(): List<Review> =
        reviews.get().await().toObjects(Review::class.java)

    /**
     * Retrieves one entrant's review of a campsite
     *
     * @param campsiteId the id of the campsite
     * @param deviceId device id of the entrant
     *
     * @return the review, null if the entrant has not left one
     */
    suspend fun myReview(campsiteId: String, deviceId: String): Review? =
        reviews.document(reviewId(campsiteId, deviceId)).get().await().toObject(Review::class.java)

    /**
     * Delete one entrant's review of a campsite from the database
     *
     * @param campsiteId the id of the campsite
     * @param deviceId device id of the entrant
     */
    suspend fun deleteReview(campsiteId: String, deviceId: String) {
        reviews.document(reviewId(campsiteId, deviceId)).delete().await()
        Log.d(TAG, "deleted review for $campsiteId")
    }

    /**
     * Sets the reviewer name on every review an entrant wrote. Called when the entrant edits their
     * profile name so their old reviews show the new one
     *
     * @param deviceId device id of the entrant who renamed themselves
     * @param name the new name
     */
    fun updateReviewerName(deviceId: String, name: String) {
        reviews.whereEqualTo("deviceId", deviceId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                val batch = db.batch()
                snapshot.documents.forEach { batch.update(it.reference, "reviewerName", name) }
                batch.commit().addOnFailureListener { Log.w(TAG, "rename failed", it) }
            }
            .addOnFailureListener { Log.w(TAG, "rename lookup failed", it) }
    }
}