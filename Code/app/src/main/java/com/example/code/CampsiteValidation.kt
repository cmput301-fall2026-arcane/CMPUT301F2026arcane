package com.example.code

/* US 02.01.02 As a campsite owner, I want to provide campsite details

Checks the details typed into the campsite form. No Firestore calls so they can be unit tested.
The form is expected to stop and show the errors instead of saving
*/

/**
 * A field on the campsite form that can be wrong
 */
enum class CampsiteField {
    NAME, LOCATION, MAX_OCCUPANCY, PRICE, MAP_LOCATION
}

/**
 * Checks every required field on the form, still as the text the owner typed
 *
 * @param name name of the campsite
 * @param location written location of the campsite
 * @param maxOccupancy max number of guests, must be a whole number of at least one
 * @param price price per night, must be more than zero
 * @param latitude map location, north south
 * @param longitude map location, east west
 * @return the fields that are wrong, empty if the form can be saved
 */
fun validateCampsite(
    name: String,
    location: String,
    maxOccupancy: String,
    price: String,
    latitude: Double,
    longitude: Double
): List<CampsiteField> {
    val errors = mutableListOf<CampsiteField>()

    if (name.isBlank()) errors.add(CampsiteField.NAME)
    if (location.isBlank()) errors.add(CampsiteField.LOCATION)

    val occupancy = maxOccupancy.trim().toIntOrNull()
    if (occupancy == null || occupancy < 1) errors.add(CampsiteField.MAX_OCCUPANCY)

    val nightly = price.trim().toDoubleOrNull()
    if (nightly == null || nightly <= 0.0) errors.add(CampsiteField.PRICE)

    if (!isMapLocationValid(latitude, longitude)) errors.add(CampsiteField.MAP_LOCATION)

    return errors
}

/**
 * Checks a map location is a real point on the globe. Zero zero is treated as not picked yet
 * because it is what an untouched map gives back
 *
 * @param latitude map location, north south
 * @param longitude map location, east west
 * @return true if the owner has picked a usable point
 */
fun isMapLocationValid(latitude: Double, longitude: Double): Boolean {
    if (latitude == 0.0 && longitude == 0.0) return false
    return latitude in -90.0..90.0 && longitude in -180.0..180.0
}

/**
 * Turns what the owner typed into the tag box into clean tags, so the same tag written two ways
 * is only stored once
 *
 * @param raw the tags as one comma separated string
 * @return the tags, trimmed, lower case and without repeats
 */
fun normalizeTags(raw: String): List<String> {
    return raw.split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()
}

/**
 * The message the form shows under a field that is wrong
 *
 * @param field the field that failed
 * @return the message to show the owner
 */
fun errorFor(field: CampsiteField): String {
    return when (field) {
        CampsiteField.NAME -> "Enter a name"
        CampsiteField.LOCATION -> "Enter a location"
        CampsiteField.MAX_OCCUPANCY -> "Enter a whole number of guests"
        CampsiteField.PRICE -> "Enter a price per night"
        CampsiteField.MAP_LOCATION -> "Pick the campsite on the map"
    }
}
