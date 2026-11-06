package com.example.arcane

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun StartScreens(mainNav: NavController) {
    val localNavCon = rememberNavController()

    NavHost(navController = localNavCon, startDestination = "title") {
        composable("title") { TitleScreen(localNavCon) }
        composable("login") { LoginScreen(mainNav, localNavCon) }
    }
}

@Composable
fun TitleScreen(localNav: NavController) {
    val titleLogo = painterResource(R.drawable.faketitlelogo)
    var prompt by remember { mutableStateOf("Click anywhere to continue.") }
    var flash = true

    LaunchedEffect(Unit) {
        while (flash) {
            prompt = "Click anywhere to continue"
            delay(0.5.seconds)
            prompt = ""
            delay(0.5.seconds)
        }
    }

    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = titleLogo,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            alpha = 1.0F
        )
        Spacer(modifier = Modifier.height(350.dp))
        Text(
            text = prompt,
            fontSize = 12.sp,
            color = Color.White
        )
    }

    // Button is transparent and covers the entire screen
    // a click anywhere sends user to the login screen
    Button (
        onClick = {
            flash = false
            localNav.navigate("login")
        },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RectangleShape
    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(mainNav: NavController, localNav: NavController) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var shownPassword by remember { mutableStateOf("") }
    var showSignUp by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

    Column (
        modifier = Modifier.fillMaxSize().padding(55.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Arcane Camping!",
            fontSize = 50.sp,
            lineHeight = 52.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(5.dp)
        )
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            text = "Login to your Account",
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(bottom=15.dp)
        )
        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text("Username / Email") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
                ),
            modifier = Modifier.fillMaxWidth().padding(bottom=10.dp)
        )
        OutlinedTextField(
            value = shownPassword,
            onValueChange = { password = it; shownPassword = "*".repeat(password.length) },
            label = { Text("Password") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
                ),
            modifier = Modifier.fillMaxWidth().padding(bottom=10.dp)
        )
        Button (
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = col.SubmitButton
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal=50.dp)
        ) {
            Text(
                text = "✔ Login"
            )
        }
        Spacer(modifier = Modifier.height(150.dp))
        Row {
            Text(
                text = "Don't have an account? ",
                color = Color.White
            )
            Text(
                text = "Sign Up",
                color = Color.Cyan,
                modifier = Modifier.clickable{ showSignUp = true }
            )
        }
    }
    if (showSignUp) {
        ModalBottomSheet(
            onDismissRequest = { showSignUp = false },
            containerColor = col.BottomSheetBackground,
            sheetState = sheetState,
            modifier = Modifier.padding(horizontal = 10.dp).padding(top = 80.dp)
        ) {
            SignUpScreen(mainNav)
        }
    }
}

@Composable
fun SignUpScreen(mainNav: NavController) {
    var username by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }


    Column (
        modifier = Modifier.fillMaxSize().padding(horizontal = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create your account",
            fontWeight = FontWeight(10),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(25.dp))
        Spacer(modifier = Modifier.height(100.dp)) // |aN: placeholder space for an upload profile photo circle
        Spacer(modifier = Modifier.height(25.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom=15.dp)
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom=15.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom=15.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom=15.dp)
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
                focusedLabelColor = col.FocusedInputLabel,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom=25.dp)
        )
        Button (
            onClick = { mainNav.navigate("home") },
            colors = ButtonDefaults.buttonColors(
                containerColor = col.SubmitButton
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal=50.dp)
        ) {
            Text(
                text = "Sign Up"
            )
        }
    }
}