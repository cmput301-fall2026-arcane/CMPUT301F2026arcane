package com.example.code.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.col
import com.example.code.model.Review
import com.example.code.repositories.CampsiteRepository
import com.example.code.rules.hasRating
import com.example.code.rules.hasReviewText
import com.example.code.services.CampsiteReviews
import com.example.code.ui.entrant.StarRating
import com.example.code.ui.navigation.BottomNavBar
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/* US 03.09.01 As an admin, I want to remove campsite ratings that violate app policy
US 03.10.01 As an admin, I want to remove campsite reviews that violate app policy

Lists every rating and review of one campsite. The admin can remove an entrant's rating or their
review text, each after confirming. Reached by tapping a campsite on the admin campsite list

The admin cannot see why a rating or review was reported, there is no report flow yet
*/

/**
 * A rating or a review the admin has picked to remove, waiting for them to confirm
 */
private data class Removal(val review: Review, val rating: Boolean)

/**
 * Shows the ratings and reviews of one campsite with buttons to remove them
 *
 * @param nav the app's main nav controller
 * @param campsiteId Firestore doc id of the campsite whose reviews are shown
 */
@Composable
fun AdminReviewsScreen(nav: NavController, campsiteId: String) {
    val service = remember { CampsiteReviews() }
    val repo = remember { CampsiteRepository() }
    val scope = rememberCoroutineScope()
    var campsiteName by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var removal by remember { mutableStateOf<Removal?>(null) }
    var refreshKey by remember { mutableStateOf(0) }   // bump to reload the reviews

    LaunchedEffect(campsiteId) {
        repo.loadCampsite(campsiteId, { campsiteName = it?.name ?: "" })
    }

    // load the reviews when the screen opens and after every removal
    LaunchedEffect(campsiteId, refreshKey) {
        try {
            reviews = service.reviewsFor(campsiteId).filter { hasRating(it) || hasReviewText(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = e.message ?: "Could not load the reviews"
        }
        loaded = true
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text("$campsiteName Reviews", color = Color.White, fontSize = 26.sp)
            Text("Admin View", color = Color.White, fontSize = 15.sp)

            message?.let {
                Text(it, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            if (loaded && reviews.isEmpty()) {
                Text(
                    "No ratings or reviews for this campsite",
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                review.reviewerName.ifEmpty { "Anonymous" },
                                color = col.HomeButtonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasRating(review)) StarRating(review.rating)
                            if (hasReviewText(review)) {
                                Text(review.text, color = col.HomeButtonText, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasRating(review)) {
                                    RemoveButton("Remove rating") { removal = Removal(review, true) }
                                }
                                if (hasReviewText(review)) {
                                    RemoveButton("Remove review") { removal = Removal(review, false) }
                                }
                            }
                        }
                    }
                }
            }
        }
        BottomNavBar("adminHome", nav)
    }

    removal?.let { picked ->
        val what = if (picked.rating) "rating" else "review"
        AlertDialog(
            onDismissRequest = { removal = null },
            title = { Text("Remove $what?") },
            text = { Text("Remove ${picked.review.reviewerName}'s $what? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    removal = null
                    scope.launch {
                        message = try {
                            val error = if (picked.rating) {
                                service.removeRating(campsiteId, picked.review.deviceId)
                            } else {
                                service.removeReview(campsiteId, picked.review.deviceId)
                            }
                            error ?: "Removed the $what"
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            e.message ?: "Could not remove the $what"
                        }
                        refreshKey++
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { removal = null }) { Text("Cancel") }
            }
        )
    }
}

/**
 * A small button under a review that starts removing its rating or text
 *
 * @param label text on the button
 * @param onClick called when the button is tapped
 */
@Composable
private fun RemoveButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = col.CampsiteCardWaitlist)
    ) {
        Text(label, color = col.CardText, fontSize = 13.sp)
    }
}