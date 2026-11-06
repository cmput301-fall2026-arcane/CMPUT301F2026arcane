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
import androidx.navigation.NavController

/* The floating bar at the bottom of the main screens. Defined once here and called from every
screen that needs it
*/

/**
 * The floating bar at the bottom of the main screens
 *
 * @param selected route of the current screen, its icon is drawn fully opaque
 * @param navCont used to give navigation functions to the icons on the bar
 */
@Composable
fun BottomNavBar(selected: String, navCont: NavController) {
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
        NavBarIcon("📷", "scan", selected, "qrscan", navCont)
        NavBarIcon("🔔", "notifications", selected, "notifications", navCont)
        NavBarIcon("🏠", "home", selected, "home", navCont)
        NavBarIcon("👤", "profile", selected, "profile", navCont)
    }
}

// TODO: swap emoji
@Composable
private fun NavBarIcon(
    emoji: String,
    route: String,
    selected: String,
    function: String,
    navCont: NavController
) {
    IconButton(onClick = {
        if (function == "profile") navCont.navigate("profile")
        else if (function == "qrscan") navCont.navigate("qrscan")
        else if (function == "notifications") navCont.navigate("notifications")
        else if (function == "home") {
            if (logged_user_type == Role.ENTRANT) navCont.navigate("entrantHome")
            else if (logged_user_type == Role.CAMP_OWNER) navCont.navigate("campOwnerHome")
            else if (logged_user_type == Role.ADMIN) navCont.navigate("adminHome")
        }
    }) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            modifier = Modifier.alpha(if (route == selected) 1f else 0.5f)
        )
    }
}