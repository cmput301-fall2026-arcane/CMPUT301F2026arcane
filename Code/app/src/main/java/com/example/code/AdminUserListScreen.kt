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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

val sampleUsers = listOf(
    UserListItem(User(name = "Banff-ish Park", email = "dfjdkjf")),
    UserListItem(User(name = "Jasper'd Park", email = "adsadsad")),
    UserListItem(User(name = "Pine Hollow", email = "dfsfsdfs")),
    UserListItem(User(name = "Redhorse Creek", email = "sdfdsfs")),
    UserListItem(User(name = "Fairprint Lake", email = "sdfsdfsdf")),
    UserListItem(User(name = "Banff-ish Park", email = "dfjdkjf")),
    UserListItem(User(name = "Jasper'd Park", email = "adsadsad")),
    UserListItem(User(name = "Pine Hollow", email = "dfsfsdfs")),
    UserListItem(User(name = "Redhorse Creek", email = "sdfdsfs")),
    UserListItem(User(name = "Fairprint Lake", email = "sdfsdfsdf")),
    UserListItem(User(name = "Banff-ish Park", email = "dfjdkjf")),
    UserListItem(User(name = "Jasper'd Park", email = "adsadsad")),
    UserListItem(User(name = "Pine Hollow", email = "dfsfsdfs")),
    UserListItem(User(name = "Redhorse Creek", email = "sdfdsfs")),
    UserListItem(User(name = "Fairprint Lake", email = "sdfsdfsdf"))
)

data class UserListItem(
    val user: User
)

@Composable
fun AdminUserListScreen(mainNav: NavController, shown: List<UserListItem>) {

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "All Users",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text (
            text = "Admin View",
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shown) { item ->
                UserCard (
                    item = item,
                    onClick = {
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(150.dp))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomNavBar("users", mainNav)
    }
}


@Composable
fun UserCard(item: UserListItem, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(col.UserCard)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.user.name,
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
                    .background(Brush.horizontalGradient(listOf(col.UserCard, Color.Transparent)))
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333, showSystemUi = true)
@Composable
fun AdminUserListScreenPreview() {
    AdminUserListScreen(rememberNavController(), sampleUsers)
}