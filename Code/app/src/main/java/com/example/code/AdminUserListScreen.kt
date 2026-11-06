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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

data class UserListItem(
    val user: User
)

@Composable
fun AdminUserListScreen(mainNav: NavController, repo: UserRepository) {

    val users = remember {
        mutableStateListOf<UserListItem>().also { list ->
            repo.getAllUsers { result ->
                result?.let { loaded -> list.addAll(loaded.map { UserListItem(it) }) }
            }
        }
    }
    var toDelete by remember { mutableStateOf<UserListItem?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(bottom = 100.dp),
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
            items(users) { item ->
                UserCard (
                    item = item,
                    onClick = {},
                    onDelete = { toDelete = item }
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomNavBar("users", mainNav)
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null},
            title = { Text("Delete user?") },
            text = { Text("Remove ${item.user.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    repo.deleteUser(item.user.deviceId) { success ->
                        if (success) users.remove(item)
                    }
                    toDelete = null
                }) { Text("Delete")}
            },
            dismissButton = {
                TextButton(onClick = {toDelete = null}) {Text("Cancel") }
            }
        )
    }
}


@Composable
fun UserCard(item: UserListItem, onClick: () -> Unit, onDelete:() -> Unit) {

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
            IconButton(                                   // NEW
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "🗑️",
                    fontSize = 22.sp
                )
            }
        }
    }
}
