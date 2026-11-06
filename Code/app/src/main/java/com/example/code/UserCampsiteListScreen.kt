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

private val sampleCampsites = listOf(
    CampsiteListItem(Campsite(id = "sample1", name = "Banff-ish Park", location = "Banff"), true),
    CampsiteListItem(Campsite(id = "sample2", name = "Jasper'd Park", location = "Jasper"), false),
    CampsiteListItem(Campsite(id = "sample3", name = "Pine Hollow", location = "Canmore"), true),
    CampsiteListItem(Campsite(id = "sample4", name = "Redhorse Creek", location = "Kananaskis"), true),
    CampsiteListItem(Campsite(id = "sample5", name = "Fairprint Lake", location = "Fairprint"), false)
)
data class CampsiteListItem(
    val campsite: Campsite,
    val lotteryOpen: Boolean = true
)
fun filterCampsites(items: List<CampsiteListItem>, query: String): List<CampsiteListItem> {
    val q = query.trim()
    if (q.isEmpty()) return items
    return items.filter {
        it.campsite.name.contains(q, ignoreCase = true) ||
                it.campsite.location.contains(q, ignoreCase = true)
    }
}

@Composable
fun UserCampsiteListScreen(mainNav: NavController) {
    var search by remember { mutableStateOf("") }

    // TODO: load from CampsiteRepository instead of the sample list
    val shown = filterCampsites(sampleCampsites, search)

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
                    onClick = { /* TODO: date picker */ },
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = col.FilterButton),
                    modifier = Modifier.width(48.dp).height(36.dp)
                ) {
                    Text("📅", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* TODO: filters sheet */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = col.FilterButton),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Set Filters", color = col.CardText)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(shown) { item ->
                    CampsiteCard(
                        item = item,
                        onClick = {
                            //TODO: campsite card
                        }
                    )
                }
            }
        }
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
