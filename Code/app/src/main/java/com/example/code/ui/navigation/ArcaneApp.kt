package com.example.code.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.ui.admin.AdminCampsiteListScreen
import com.example.code.ui.admin.AdminUserListScreen
import com.example.code.ui.owner.CampsiteOwnerListPage
import com.example.code.ui.entrant.CampsiteScreen
import com.example.code.ui.entrant.EntrantHomeScreen
import com.example.code.ui.admin.HomeScreen
import com.example.code.ui.entrant.MyLotteriesScreen
import com.example.code.ui.owner.OwnerHomeScreen
import com.example.code.ui.owner.OwnerToolsScreen
import com.example.code.ui.owner.OwnerWaitlistScreen
import com.example.code.ui.profile.ProfileScreen
import com.example.code.R
import com.example.code.ui.entrant.ReservationDetailScreen
import com.example.code.ui.entrant.ReservationsScreen
import com.example.code.ui.start.StartScreens
import com.example.code.ui.entrant.UserCampsiteListScreen
import com.example.code.sites
import com.example.code.users
import com.example.code.ui.admin.AdminReviewsScreen

@Composable
fun ArcaneApp() {
    val backgroundImg = painterResource(R.drawable.backgroundimg)
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Image(
            painter = backgroundImg,
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5F
        )
    }

    val mainNavCon = rememberNavController()

    NavHost(navController = mainNavCon, startDestination = "start") {
        composable("start") { StartScreens(mainNavCon) }
        composable("entrantHome") { EntrantHomeScreen(mainNavCon) }
        composable("campOwnerHome") { OwnerHomeScreen(mainNavCon) }
        composable("adminHome") { HomeScreen(mainNavCon) }
        composable("campsites") { UserCampsiteListScreen(mainNavCon) }
        composable("profile") { ProfileScreen(mainNavCon) }
        composable("campsite/{campsiteId}") { entry ->
            CampsiteScreen(mainNavCon, entry.arguments?.getString("campsiteId") ?: "")
        }
        composable("users") { AdminUserListScreen(mainNavCon, users) }
        composable("adminCampsite") { AdminCampsiteListScreen(mainNavCon, sites) }
        composable("myLotteries") { MyLotteriesScreen(mainNavCon) }
        composable("reservations") { ReservationsScreen(mainNavCon) }
        composable("reservation/{reservationId}") { entry ->
            ReservationDetailScreen(mainNavCon, entry.arguments?.getString("reservationId") ?: "")
        }
        composable("ownerCampsites") { CampsiteOwnerListPage(mainNavCon) }
        composable("ownerTools/{campsiteId}") { entry ->
            OwnerToolsScreen(mainNavCon, entry.arguments?.getString("campsiteId") ?: "")
        }
        composable("ownerWaitlist/{campsiteId}") { entry ->
            OwnerWaitlistScreen(mainNavCon, entry.arguments?.getString("campsiteId") ?: "")
        }
        composable("adminReviews/{campsiteId}") { entry ->
            AdminReviewsScreen(mainNavCon, entry.arguments?.getString("campsiteId") ?: "")
        }
    }
}

// hello