package com.example.code.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.col
import com.example.code.logged_in_device
import com.example.code.model.Review
import com.example.code.rules.MAX_RATING
import com.example.code.rules.MAX_REVIEW_LENGTH
import com.example.code.rules.hasRating
import com.example.code.rules.hasReviewText
import com.example.code.rules.ratingLabel
import com.example.code.services.CampsiteReviews
import com.example.code.ui.entrant.StarRating
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/* US 01.13.01 As an entrant, I want to give a rating for a campsite
US 01.13.02 As an entrant, I want to post a review for a campsite

The reviews card under the campsite details. The entrant taps a star to rate, types a review and
posts it, and sees what everyone has written. Rating and review are saved separately through
CampsiteReviews

Reviews load once when the screen opens and after each save, there is no live updating
*/

/**
 * Shows the reviews of a campsite and lets the entrant rate it and write a review
 *
 * @param campsiteId Firestore doc id of the campsite being reviewed
 * @param onChanged called after a rating or review is saved so the stars at the top can reload
 */
@Composable
fun CampsiteReviewsSection(campsiteId: String, onChanged: () -> Unit) {
    val service = remember { CampsiteReviews() }
    val scope = rememberCoroutineScope()
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var draftLoaded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }   // bump to reload the reviews

    // load the reviews when the screen opens and after every save
    LaunchedEffect(campsiteId, refreshKey) {
        try {
            reviews = service.reviewsFor(campsiteId)
            if (!draftLoaded) {
                draft = reviews.firstOrNull { it.deviceId == logged_in_device }?.text ?: ""
                draftLoaded = true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = e.message ?: "Could not load the reviews"
        }
    }

    val mine = reviews.firstOrNull { it.deviceId == logged_in_device }
    val myRating = mine?.rating ?: 0

    fun save(action: suspend () -> String?, savedMessage: String) {
        scope.launch {
            busy = true
            message = try {
                action() ?: savedMessage
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.message ?: "Could not save"
            }
            busy = false
            refreshKey++
            onChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(col.UnfocusedInputContainer)
            .padding(16.dp)
    ) {
        Text("Reviews", color = col.CardText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(ratingLabel(reviews), color = col.CardText, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your rating", color = col.CardText, fontSize = 14.sp)
        Row {
            for (star in 1..MAX_RATING) {
                Text(
                    text = if (star <= myRating) "★" else "☆",
                    color = col.CardText,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .clickable(enabled = !busy) {
                            save({ service.rate(campsiteId, star) }, "Rating saved")
                        }
                        .padding(end = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it.take(MAX_REVIEW_LENGTH) },
            label = { Text("Write a review") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.FocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.CardText,
                unfocusedLabelColor = col.CardText,
                focusedTextColor = col.CardText,
                unfocusedTextColor = col.CardText,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text("${draft.trim().length}/$MAX_REVIEW_LENGTH", color = col.CardText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { save({ service.postReview(campsiteId, draft) }, "Review posted") },
            enabled = !busy,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = col.CampsiteCardWaitlist,
                disabledContainerColor = col.CampsiteCardWaitlist.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text(
                text = if (mine != null && hasReviewText(mine)) "Update review" else "Post review",
                color = col.CardText
            )
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = col.CardText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        val shown = reviews.filter { hasRating(it) || hasReviewText(it) }
        if (shown.isEmpty()) {
            Text("No reviews yet", color = col.CardText, fontSize = 14.sp)
        }
        shown.forEach { review ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(
                    text = review.reviewerName.ifEmpty { "Anonymous" },
                    color = col.CardText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (hasRating(review)) StarRating(review.rating)
                if (hasReviewText(review)) {
                    Text(review.text, color = col.CardText, fontSize = 14.sp)
                }
            }
        }
    }
}