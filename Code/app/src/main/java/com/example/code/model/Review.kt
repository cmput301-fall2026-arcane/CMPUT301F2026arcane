/*
 * Review.kt
 *
 * This is a model class for campsite reviews where every review shares thew same data shape. The campsite
 * screen will show the rating. CampsiteReviews will build it and the ReviewRepository will save it and store it
 * from Firebase "reviews"
 */
package com.example.code.model

import com.google.firebase.firestore.DocumentId

/**
 * A rating and review an entrant left for a campsite
 *
 * @property id  Firestore doc id
 * @property campsiteId  the id of the campsite being reviewed
 * @property deviceId  the id of the entrant who wrote the review
 * @property reviewerName  name of the entrant, updated when they rename themselves
 * @property rating  star rating from 1 to 5, 0 if the entrant has not rated
 * @property text  written review, empty if there is none
 * @property createdAt  creation time
 */
data class Review(
    @DocumentId val id: String = "",
    val campsiteId: String = "",
    val deviceId: String = "",
    val reviewerName: String = "",
    val rating: Int = 0,
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)