package com.example.code

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * What the entrant is filtering the campsite list by. Empty means "any"
 *
 * @property location only show campsites whose location contains this text
 * @property minGuests only show campsites that fit at least this many guests, 0 for any
 * @property startDate first night wanted
 * @property endDate last night wanted
 */
data class CampsiteFilters(
    val location: String = "",
    val minGuests: Int = 0,
    val startDate: Long? = null,
    val endDate: Long? = null
) {
    val activeCount: Int
        get() = listOf(location.isNotBlank(), minGuests > 0, startDate != null).count { it }
}

/**
 * Filters campsites by the search box and the chosen filters
 *
 * @param items the campsites to filter
 * @param query what was typed in the search box, matched against name and location
 * @param filters the filters chosen in the filter sheet and date picker
 * @return the campsites that match everything
 */
fun filterCampsites(
    items: List<CampsiteListItem>,
    query: String,
    filters: CampsiteFilters = CampsiteFilters()
): List<CampsiteListItem> {
    val q = query.trim()
    val location = filters.location.trim()
    return items.filter { item ->
        val campsite = item.campsite
        val matchesSearch = q.isEmpty() ||
                campsite.name.contains(q, ignoreCase = true) ||
                campsite.location.contains(q, ignoreCase = true)
        val matchesLocation = location.isEmpty() ||
                campsite.location.contains(location, ignoreCase = true)
        val matchesGuests = filters.minGuests <= 0 || campsite.maxOccupancy >= filters.minGuests
        // TODO: check filters.startDate / endDate once campsites have availability
        matchesSearch && matchesLocation && matchesGuests
    }
}

/**
 * Formats a picked date range for display, e.g. "Nov 10 – Nov 12"
 *
 * @param start first date as UTC midnight millis, null if nothing is picked
 * @param end last date as UTC midnight millis, null if only one day is picked
 * @return the formatted range, or an empty string if nothing is picked
 */
fun formatDateRange(start: Long?, end: Long?): String {
    if (start == null) return ""
    // the date picker gives UTC midnight, so format in UTC or the day can shift by one
    val format = SimpleDateFormat("MMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val first = format.format(Date(start))
    return if (end == null || end == start) first else "$first – ${format.format(Date(end))}"
}

/**
 * One line describing the active filters
 *
 * @param filters the active filters
 * @return the description, or an empty string if no filters are on
 */
fun filterSummary(filters: CampsiteFilters): String {
    return listOfNotNull(
        filters.location.trim().takeIf { it.isNotEmpty() }?.let { "📍 $it" },
        filters.minGuests.takeIf { it > 0 }?.let { "👥 $it+" },
        formatDateRange(filters.startDate, filters.endDate).takeIf { it.isNotEmpty() }?.let { "📅 $it" }
    ).joinToString("  ·  ")
}

/**
 * Today's date as UTC midnight millis, the format the date picker uses. Used to stop past dates
 * from being picked
 */
fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    return utc.timeInMillis
}