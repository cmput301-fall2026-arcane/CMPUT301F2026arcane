package com.example.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* The floating bar at the bottom of the main screens. Defined once here and called from every
screen that needs it
*/

/**
 * The floating bar at the bottom of the main screens
 *
 * @param selected route of the current screen, its icon is drawn fully opaque
 * @param onSelect called with the route of the tapped icon
 */
@Composable
fun BottomNavBar(selected: String, onSelect: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(col.NavBar)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarIcon("📷", "scan", selected, onSelect)
        NavBarIcon("🔔", "notifications", selected, onSelect)
        NavBarIcon("🏠", "home", selected, onSelect)
        NavBarIcon("👤", "profile", selected, onSelect)
    }
}

// TODO: swap emoji
@Composable
private fun NavBarIcon(
    emoji: String,
    route: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    IconButton(onClick = { onSelect(route) }) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            modifier = Modifier.alpha(if (route == selected) 1f else 0.5f)
        )
    }
}