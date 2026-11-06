package com.example.code

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Creates a menu button which contains the same format for all for consistency
 *
 * The function takes in label (what to call it), desired height to create a button box.
 *
 * @param label Text displayed inside the button
 * @param onClick Triggered when the user clicks on the button
 * @param height Fixed vertical size applied to the button
 * @param modifier Used to configure padding and layout
 */
@Composable
private fun MenuButton(label: String, onClick: () -> Unit, height: Dp, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = col.HomeButtonText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun OwnerToolsScreen(nav: NavController, campsiteName: String) {
    var selected by remember { mutableStateOf("home") }

    Column(                                                       // 2. NEW outer column
        modifier = Modifier.fillMaxSize().statusBarsPadding()     //    (statusBarsPadding, not systemBarsPadding)
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "$campsiteName Tools!",
                color = Color.White,
                fontSize = 26.sp,
            )
            Spacer(Modifier.height(24.dp))

            MenuButton(
                label = "Lottery Settings",
                onClick = {},
                height = 150.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuButton(
                    label = "Current Lottery List",
                    onClick = {},
                    height = 75.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            MenuButton(
                label = "Current Reservations",
                onClick = {},
                height = 100.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuButton(
                    label = "Past Reservations",
                    onClick = {},
                    height = 75.dp,
                    modifier = Modifier.weight(1f)
                )

                MenuButton(
                    label = "Canceled Reservations",
                    onClick = {},
                    height = 75.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            MenuButton(
                label = "All Notifications Sent",
                onClick = {},
                height = 100.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BottomNavBar(selected = selected, onSelect = { selected = it })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun CampsiteToolsScreenPreview() {
    OwnerToolsScreen(
        nav = rememberNavController(),
        campsiteName = "Pineapple's Backyard"
    )
}


