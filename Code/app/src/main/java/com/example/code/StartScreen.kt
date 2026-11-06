package com.example.code

import android.R.attr.name
import android.R.attr.phoneNumber
import android.provider.Settings
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.R
import com.example.code.col
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Simply a composable with the navigation to house all the screens
 * that are a part of the startup and login/signup processes.
 *
 * @param mainNav is the navigation controller of the entire app passed in.
 * (enables navigation out of startScreens)
 */
@Composable
fun StartScreens(mainNav: NavController) {
    val localNavCon = rememberNavController()

    NavHost(navController = localNavCon, startDestination = "title") {
        composable("title") { TitleScreen(localNavCon) }
        composable("login") { LoginScreen(mainNav, localNavCon) }
    }
}

/**
 * This is the title screen that simply displays a logo and a flashing
 * prompt to 'click anywhere'. This is the first screen in the app.
 *
 * @param localNav is the startScreens specific controller to provide
 * navigation ability, especially towards LoginScreen.
 */
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = titleLogo,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
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

/**
 * This creates the login screen, which holds a welcome message, a login
 * button, a sign-up button, and all other handling including opening
 * the sign-up page and displaying error messages.
 *
 * @param mainNav is the primary screen navigation controller shared.
 * @param localNav is the local screen navigation within startScreens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(mainNav: NavController, localNav: NavController) {
    var showSignUp by remember { mutableStateOf(false) }

    var raiseError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

    val context = LocalContext.current
    val dId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(55.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Arcane Camping!",
            fontSize = 50.sp,
            lineHeight = 52.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            text = "Login to your Account",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 15.dp)
        )
        if (raiseError) {
            Text(
                text = "Your login was not found.\n" +
                        "  Please sign in instead.",
                fontSize = 15.sp,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        Button (
            onClick = { handleLogin ( dId, { success ->
                if (success) {
                    if (logged_user_type == Role.ADMIN) mainNav.navigate("adminHome")
                    else if (logged_user_type == Role.ENTRANT) mainNav.navigate("entrantHome")
                    else if (logged_user_type == Role.CAMP_OWNER) mainNav.navigate("campOwnerHome")
                } else {
                    raiseError = true
                }
            }) },
            colors = ButtonDefaults.buttonColors(
                containerColor = col.SubmitButton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
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
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 120.dp)
        ) {
            SignUpScreen(mainNav)
        }
    }
}

/**
 * Creates the layout for the sign-up elements located inside
 * the login screen's modal bottom sheet.
 *
 * @param mainNav is the navigation controller passed in to share control
 */
@Composable
fun SignUpScreen(mainNav: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var raiseError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create your account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(25.dp))
        Spacer(modifier = Modifier.height(100.dp)) // |aN: placeholder space for an upload profile photo circle
        Spacer(modifier = Modifier.height(25.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp)
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = col.UnfocusedInputContainer,
                focusedContainerColor = col.FocusedInputContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp)
        )
        if (raiseError) {
            Text(
                text = errorMessage,
                fontSize = 15.sp,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        Text(
            text = "Sign up as:",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button (
            onClick = { handleSignUp(name, email, phoneNumber, dId, "e", { exitCode ->
                        if (exitCode == 0) {
                            mainNav.navigate("EntrantHome")
                        } else if (exitCode == 1) {
                            errorMessage = "Please fill all required fields."
                            raiseError = true
                        } else if (exitCode == 2) {
                            errorMessage = "Existing account detected."
                            raiseError = true
                        }
                    }) },
            colors = ButtonDefaults.buttonColors(
                containerColor = col.SubmitButton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .padding(bottom = 5.dp)
        ) {
            Text(
                text = "Entrant"
            )
        }
        Text("OR")
        Spacer(modifier = Modifier.height(5.dp))
        Button (
            onClick = { handleSignUp(name, email, phoneNumber, dId, "c", { exitCode ->
                        if (exitCode == 0) {
                            mainNav.navigate("campOwnerhome")
                        } else if (exitCode == 1) {
                            errorMessage = "Please fill all required fields."
                            raiseError = true
                        } else if (exitCode == 2) {
                            errorMessage = "Existing account detected."
                            raiseError = true
                        }
            }) },
            colors = ButtonDefaults.buttonColors(
                containerColor = col.SubmitButton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .padding(bottom = 5.dp)
        ) {
            Text(
                text = "Campsite Owner"
            )
        }
    }
}

/**
 * Processes user sign-up request
 *
 * This function creates a user, updates the values of the user to match the
 * signup inputs, and then adds the role selected on the page to the user. This
 * is done through passed functions as Firebase calls are asynchronous.
 *
 * @param name input field for mutation
 * @param email input field for mutation
 * @param phone input field for mutation
 * @param deviceID input field for creation and query
 * @param type input field for mutation
 * @param onDone callback function to handle success levels
 */
fun handleSignUp(name: String, email: String, phone: String,
                 deviceID: String, type: String, onDone : (Number) -> Unit) {
    if (name == "" || email == "" || phone == "" || deviceID == "") {
        onDone(1)
        return
    } // fail if fields are empty

    // Ensure that the device doesn't already have an account
    users.getUser(deviceID) { user ->
        if (user != null) {
            onDone(2)
        } else {
            var role: Role;
            if (type == "e") role = Role.ENTRANT
            else if (type == "c") role = Role.CAMP_OWNER
            else role = Role.ENTRANT

            // creates user, updates with values, and adds role
            users.getOrCreateUser(deviceID, {
                users.updateUser(deviceID, name, email, phone, null, { success ->
                    if (!success) {
                        onDone(1)
                        return@updateUser
                    }
                    users.addRole(deviceID, role, {
                        logged_user_type = role
                        logged_in_name = name
                        logged_in_device = deviceID
                        logged_in_email = email
                        logged_in_phone = phone
                        onDone(0)
                    })
                })
            })
        }
    }
}

/**
 * Processes user login request
 *
 * This function queries for the user device ID. If it exists, we can simply
 * return true so the parent navigates to the home page. Else, we return
 * false
 *
 * @param deviceID field used for user query
 * @param onDone callback function to handle success levels
 */
fun handleLogin( deviceID: String, onDone : (Boolean) -> Unit) {
    users.getUser(deviceID) { user ->
        if (user != null) {
            logged_user_type = user.roles[0]
            logged_in_name = user.name
            logged_in_device = user.deviceId
            logged_in_email = user.email
            logged_in_phone = user.phone
            onDone(true)
        } else {
            onDone(false)
        }
    }
}