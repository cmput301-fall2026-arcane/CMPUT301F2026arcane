package com.example.code

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.R
import androidx.compose.material3.TextButton

//private val sampleCampsites = listOf(
//    CampsiteListItem(Campsite(id = "sample1", name = "Banff-ish Park", location = "Banff"), true),
//    CampsiteListItem(Campsite(id = "sample2", name = "Jasper'd Park", location = "Jasper"), false),
//    CampsiteListItem(Campsite(id = "sample3", name = "Pine Hollow", location = "Canmore"), true),
//    CampsiteListItem(Campsite(id = "sample4", name = "Redhorse Creek", location = "Kananaskis"), true),
//    CampsiteListItem(Campsite(id = "sample5", name = "Fairprint Lake", location = "Fairprint"), false)
//)
data class CampsiteListItem(
    val campsite: Campsite,
    val lotteryOpen: Boolean = true
)


@Composable
fun UserCampsiteListScreen(mainNav: NavController) {

    var search by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(CampsiteFilters()) }
    var showFilters by remember { mutableStateOf(false) }
    var showDates by remember { mutableStateOf(false) }

    DisposableEffect(Unit){
        sites.listenToAll()
        onDispose { sites.stopListening() }
    }

    // TODO: load from CampsiteRepository instead of the sample list
    val shown = filterCampsites(sites.liveCampsites.map {CampsiteListItem(it)}, search, filters)

    Scaffold(
        containerColor = Color.Transparent, // let the ArcaneApp background image show through
        bottomBar = { BottomNavBar(selected = "home", mainNav) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = col.UnfocusedInputContainer,
                    focusedContainerColor = col.FocusedInputContainer,
                    focusedLabelColor = col.CardText,
                    unfocusedLabelColor = col.CardText,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { showDates = true },
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = col.FilterButton),
                    modifier = Modifier.width(48.dp).height(36.dp)
                ) {
                    Text("📅", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showFilters = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = col.FilterButton),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text(
                        text = if (filters.activeCount > 0) "Set Filters (${filters.activeCount})" else "Set Filters",
                        color = col.CardText
                    )
                }
            }
            if (filters.activeCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = filterSummary(filters),
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { filters = CampsiteFilters() }) {
                        Text("Clear all", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (shown.isEmpty() && sites.allCampsitesLoaded) {
                Text("No campsites match your search", color = Color.White)
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(shown) { item ->
                    CampsiteCard(
                        item = item,
                        onClick = {
                            mainNav.navigate("campsite/${item.campsite.id}")
                        }
                    )
                }
            }
        }
    }

    if (showFilters) {
        CampsiteFilterSheet(
            current = filters,
            onApply = { filters = it; showFilters = false },
            onDismiss = { showFilters = false }
        )
    }
    if (showDates) {
        CampsiteDateRangeDialog(
            startDate = filters.startDate,
            endDate = filters.endDate,
            onConfirm = { start, end ->
                filters = filters.copy(startDate = start, endDate = end)
                showDates = false
            },
            onDismiss = { showDates = false }
        )
    }

}

/**
 * One row in the campsite list: name on the left, photo on the right fading into the card
 *
 * @param item the campsite to show
 * @param onClick called when the card is tapped
 */
@Composable
fun CampsiteCard(item: CampsiteListItem, onClick: () -> Unit) {
    val cardColor = if (item.lotteryOpen) col.CampsiteCardOpen else col.CampsiteCardWaitlist

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.campsite.name,
            color = col.CardText,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)
        )
        Box(modifier = Modifier.weight(0.9f).fillMaxHeight()) {
            Image(
                painter = painterResource(R.drawable.backgroundimg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(32.dp)
                    .background(Brush.horizontalGradient(listOf(cardColor, Color.Transparent)))
            )
        }
    }
}
