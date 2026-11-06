package com.example.code

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.R
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/* US 01.01.05 As an entrant, I want to see details of a campsite
US 01.01.06 As an entrant, I want to join an active lottery from the campsite details screen
US 01.02.01 As an entrant, I want to join the waiting list for a campsite lottery
US 01.02.02 As an entrant, I want to leave the waiting list for a campsite lottery

Campsite details page. Loads the campsite and its lottery, and the lottery button enters, leaves,
reserves or opens the reservation depending on where the entrant stands
*/

/**
 * Shows the details of one campsite
 *
 * @param mainNav the app's main nav controller, used for the back button
 * @param campsiteId Firestore doc id of the campsite to show
 */
@Composable
fun CampsiteScreen(mainNav: NavController, campsiteId: String) {
    val repo = remember { CampsiteRepository() }
    val entrantLotteries = remember { EntrantLotteries() }
    val scope = rememberCoroutineScope()
    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var lotteryState by remember { mutableStateOf<CampsiteLotteryState?>(null) }
    var refreshKey by remember { mutableStateOf(0) }   // bump to reload the lottery state
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showReserveDates by remember { mutableStateOf(false) }

    // load the campsite once when the screen opens
    LaunchedEffect(campsiteId) {
        repo.loadCampsite(
            id = campsiteId,
            onDone = { campsite = it; loading = false },
            onError = { error = it.message ?: "Could not load the campsite"; loading = false }
        )
    }

    // load the lottery state when the screen opens and after every action
    LaunchedEffect(campsiteId, refreshKey) {
        lotteryState = try {
            entrantLotteries.stateForCampsite(campsiteId, currentEntrant())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = e.message ?: "Could not load the lottery"
            null
        }
    }

    val state = lotteryState
    val status = state?.status

    fun onLotteryButton() {
        val lottery = state?.lottery ?: return
        when (status) {
            EntrantLotteryStatus.OPEN -> scope.launch {
                busy = true
                message = joinMessage(entrantLotteries.enter(lottery, currentEntrant()))
                busy = false
                refreshKey++
            }
            EntrantLotteryStatus.JOINED -> showLeaveConfirm = true
            EntrantLotteryStatus.WON -> showReserveDates = true
            EntrantLotteryStatus.RESERVED ->
                state?.reservation?.let { mainNav.navigate("reservation/${it.id}") }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // let the background image show through
        bottomBar = { BottomNavBar(selected = "home", navCont = mainNav) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val loaded = campsite
            when {
                loading -> CircularProgressIndicator(color = col.CampsiteCardOpen)
                error != null -> Text(error ?: "", color = Color(0xFFFF8A80))
                loaded == null -> Text("Campsite not found", color = Color.White)
                else -> CampsiteDetailsCard(
                    campsite = loaded,
                    lotteryLabel = status?.let { lotteryButtonLabel(it) } ?: "LOADING...",
                    lotteryEnabled = status != null && canPressLotteryButton(status) && !busy,
                    lotteryMessage = message ?: status?.let { lotteryStatusMessage(it, state?.lottery) },
                    onBack = { mainNav.popBackStack() },
                    onLotteryButton = { message = null; onLotteryButton() }
                )
            }
        }
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Leave this lottery?") },
            text = { Text("You'll lose your spot. You can enter again while registration is open.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirm = false
                    state?.lottery?.let { lottery ->
                        scope.launch {
                            busy = true
                            message = leaveMessage(entrantLotteries.leave(lottery, currentEntrant()))
                            busy = false
                            refreshKey++
                        }
                    }
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) { Text("Stay") }
            }
        )
    }

    if (showReserveDates) {
        CampsiteDateRangeDialog(
            startDate = null,
            endDate = null,
            onConfirm = { start, end ->
                showReserveDates = false
                val lottery = state?.lottery
                val site = campsite
                if (lottery != null && site != null && start != null) {
                    scope.launch {
                        busy = true
                        try {
                            val reservation = entrantLotteries.reserve(
                                currentEntrant(), lottery, site, start, end ?: start
                            )
                            mainNav.navigate("reservation/${reservation.id}")
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            message = e.message ?: "Could not make the reservation"
                        }
                        busy = false
                        refreshKey++
                    }
                }
            },
            onDismiss = { showReserveDates = false }
        )
    }
}

/**
 * The rounded card holding everything about the campsite
 *
 * @param campsite the campsite to show
 * @param lotteryLabel text on the lottery button
 * @param lotteryEnabled false greys the lottery button out
 * @param lotteryMessage one line about the lottery shown above the buttons, or null
 * @param onBack called when the back button is tapped
 * @param onLotteryButton called when the lottery button is tapped
 */
@Composable
private fun CampsiteDetailsCard(
    campsite: Campsite,
    lotteryLabel: String,
    lotteryEnabled: Boolean,
    lotteryMessage: String?,
    onBack: () -> Unit,
    onLotteryButton: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(col.UnfocusedInputContainer)
        ) {
            Header(campsite)

            Image(
                painter = campsiteImage(campsite),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatTile(Modifier.weight(1f)) {
                        TileText(campsite.maxOccupancy.toString(), 40)
                        TileText("👥", 20)
                    }
                    StatTile(Modifier.weight(1f)) {
                        TileText(formatPrice(campsite.pricePerNight), 34)
                        TileText("per/night", 14)
                    }
                    // TODO: generate the real QR code for this campsite
                    StatTile(Modifier.weight(1f)) {
                        TileText("QR", 30)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Description:", color = col.CardText, fontSize = 14.sp)
                Text(
                    text = campsite.description.ifEmpty { "No description yet" },
                    color = col.CardText,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Location: ${campsite.location}", color = col.CardText, fontSize = 14.sp)
                Text(
                    text = "Capacity: Space for ${campsite.maxOccupancy} campers",
                    color = col.CardText,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (lotteryMessage != null) {
                    Text(
                        text = lotteryMessage,
                        color = col.CardText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = col.CampsiteCardWaitlist),
                        modifier = Modifier.width(56.dp).height(36.dp)
                    ) {
                        Text("↩", color = col.CardText, fontSize = 18.sp)
                    }
                    Button(
                        onClick = onLotteryButton,
                        enabled = lotteryEnabled,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = col.CampsiteCardWaitlist,
                            disabledContainerColor = col.CampsiteCardWaitlist.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.width(190.dp).height(36.dp)
                    ) {
                        Text(
                            text = lotteryLabel,
                            color = col.CardText.copy(alpha = if (lotteryEnabled) 1f else 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Owner avatar, campsite name and star rating along the top of the card
 */
@Composable
private fun Header(campsite: Campsite) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: the owner's profile photo once users have images
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(col.CampsiteCardOpen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = campsite.name.take(1).uppercase(),
                color = col.CardText,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = campsite.name,
                color = col.CardText,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: show the owner's @name once it can be loaded from the users collection
                Spacer(modifier = Modifier.weight(1f))
                // TODO: real rating once campsites have reviews
                StarRating(rating = 0)
            }
        }
    }
}

/**
 * Five stars, filled up to the rating
 *
 * @param rating number of filled stars, 0 to 5
 */
@Composable
private fun StarRating(rating: Int) {
    val stars = (1..5).joinToString("") { if (it <= rating) "★" else "☆" }
    Text(stars, color = col.CardText, fontSize = 16.sp)
}

/**
 * One of the dark square tiles under the photo
 */
@Composable
private fun StatTile(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(col.CardText.copy(alpha = 0.7f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun TileText(text: String, size: Int) {
    Text(text = text, color = col.FocusedInputLabel, fontSize = size.sp, lineHeight = (size + 2).sp)
}

/**
 * Shows whole prices without decimals ($75) and others with two ($75.50)
 */
fun formatPrice(price: Double): String {
    return if (price % 1.0 == 0.0) "$${price.toInt()}" else "$%.2f".format(price)
}

@Composable
fun campsiteImage(campsite: Campsite): Painter {
    return painterResource(R.drawable.backgroundimg)
}