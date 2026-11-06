package com.example.code

import com.google.firebase.firestore.DocumentId

/* This is a model class for campsite listings. All campsites will share the same data shape.
Register screen will build it, the CampsiteRepository will save it / load it from the Firestore
collection which will be displayed by the list screen

Not everything has been modeled yet
*/

/**
 * A campsite listing registered by an owner
 *
 * @property id  Firestore doc id
 * @property ownerId  the id of the owner who registered the campsite
 * @property name  name of the campsite that will be displayed
 * @property location  location of the campsite
 * @property maxOccupancy  max number of guests
 * @property pricePerNight  price of the campsite per night
 * @property amenities  list of amenities available
 * @property description  details about the campsite
 * @property createdAt  creation time
 */
data class Campsite (
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val maxOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val amenities: MutableList<String> = mutableListOf<String>(),
    val description: String = "",
    val createdAt: Long = 0L
)