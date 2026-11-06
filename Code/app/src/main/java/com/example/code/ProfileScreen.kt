package com.example.code

import android.R.attr.checked
import android.R.attr.onClick
import android.R.attr.text
import android.R.attr.top
import android.app.ProgressDialog.show
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.code.ui.theme.DarkGreen
import com.example.code.ui.theme.MossGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(mainNav: NavController) {
    var showAppPolicy by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showNotifs by remember { mutableStateOf(false) }
    var sheetAPState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
    var sheetEditState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
    var sheetNotifState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

    Card(
        modifier = Modifier.height(800.dp)
            .padding(top=70.dp)
            .padding(horizontal=10.dp)
            .padding(bottom=25.dp),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = col.HomeButton)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(25.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Spacer(modifier = Modifier.height(110.dp)) // replace with profile image
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = logged_in_name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp
                )
                Text(
                    text = logged_in_email,
                    fontSize = 20.sp,
                    lineHeight = 40.sp
                )
                Text(
                    text = logged_in_phone,
                    fontSize = 20.sp,
                    lineHeight = 40.sp
                )
                Spacer(modifier = Modifier.height(100.dp))
                Button(
                    onClick = { showAppPolicy = true },
                    modifier = Modifier.padding(bottom = 10.dp).height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen
                    )
                ) {
                    Text(
                        text = "View App Policy",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = { showEdit = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen
                    )
                ) {
                    Text(
                        text = "Edit Profile Info",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = { showNotifs = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen
                    )
                ) {
                    Text(
                        text = "Change Notification Preferences",
                        fontSize = 15.sp
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { deleteProfile(logged_in_device, mainNav) },
                        modifier = Modifier.height(45.dp).width(200.dp).padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Delete Profile",
                            fontSize = 15.sp
                        )
                    }
                    Button(
                        onClick = { signOut(mainNav) },
                        modifier = Modifier.height(45.dp).width(150.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MossGreen
                        )
                    ) {
                        Text(
                            text = "Sign Out",
                            fontSize = 15.sp
                        )
                    }
                }
            }
            if (showAppPolicy) {
                ModalBottomSheet(
                    onDismissRequest = { showAppPolicy = false },
                    containerColor = col.BottomSheetBackground,
                    sheetState = sheetAPState,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 120.dp)
                ) {
                    AppPolicyScreen()
                }
            }
            if (showEdit) {
                ModalBottomSheet(
                    onDismissRequest = { showEdit = false },
                    containerColor = col.BottomSheetBackground,
                    sheetState = sheetEditState,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 120.dp)
                ) {
                    EditDetailsScreen(mainNav)
                }
            }
            if (showNotifs) {
                ModalBottomSheet(
                    onDismissRequest = { showNotifs = false },
                    containerColor = col.BottomSheetBackground,
                    sheetState = sheetNotifState,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 120.dp)
                ) {
                    notificationsScreen(mainNav)
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomNavBar("adminHome", mainNav)
    }
}

@Composable
fun AppPolicyScreen() {
    Text(
        text = "Αναζητήσει αντάλλαγμα μυθοπλασία στερεότυπη να πω. Μάθει απλώς έν το μάγον χωρίς είπεν πλέει. Τις δύο παράδοξη ευρύτερα ευελιξία ροζ πολιτικά επίλογοι αδυναμία. Επίσης ιι πρέπει χτένας σημεία έμαθες ας έκαστο. Κι ίδιες δικές πα αιώνα δάσος. Πέραν ακόμη ευρήκ εν σε θα φράση έν.\n" +
            "\n" +
            "Γνωρίζει εχάτεψεν ροή έλα ελάχιστα πια. Σιδερένιων αντιφάσεις κεί συνδέοντας που ρου. Έξω κατακτήσει φουσκωμένα λογαριασμό παράδειγμα εξω γοητευτικό. Καταλήξει όλα επιτύχουν δομήνικος στεγνώνει νου εάν συνθέτουν ακολούθως. Γι σπουδάζουν τελευταίου κακόγουστο θα. Κι το πρακτικές ανατάσεις ηλικιακού αποτελούν. Εάν ροζ ανατάσεις αναδείξει ευρυδίκης ηλικιακού εδώ δεν. Ασυνήθιστο κι ακατόρθωτο ρευστότητα χρειάζεσθε μη εκφραστικό ευαισθησία. Βρει ατο μαμά ίδια ένα μάνα κάτι πολύ έως.\n" +
            "\n" +
            "Αποτέλεσμα ταυτότητάς πραγματικό διαχρονικά απ συγγένειας αγανάκτηση δε. Τα δεσμός επήρεν διέπει υπ ανάγκη έν ντιβάν σο. Κείμενα πει της τράβηξε θηλάσει στο μοτίβων των βαχτιάρ διηνεκή. Αν αλήθεια σχολική τι καθεμιά αφήγημα πορείας ξαφνικά τα. Λέγοντας κριτικής εφ έν τα ενηλίκων τραγούδι το καλύτερα. Στα πώς ζωή χρειαστεί προκύπτει αστήριχτη από πολυφωνία. Κακού επήρε μόνος του γιατί είδος πας στους κεί. Μπανιέρα ιστορίες προθέαση άντλησης πρακτική συν βρεγμένο πως. Θεόσταλτο ονομάζουν κυριαρχία έξω διαδίκτυο στη.\n" +
            "\n" +
            "Εφ παιδιών έχοντας παγίωση γι. Πρόσμιξη φυλετικό ισχύουσα όχι παραπέρα σην πώς. Παράγοντα ρομαντική μην μπανιέρας κοινωνίας κλπ. Μέρα πηγή σοφό ζέη κάτι κάθε σαν λίγο ναι. Αμφιβολίες ναι ενοχλήσεις σου ανατρέπουν παραπέμπει μια. Παρίση επί υπήρχε από αφενός.\n",
        modifier = Modifier.padding(horizontal = 15.dp)
    )
}

/**
 * Creates the layout for the sign-up elements located inside
 * the login screen's modal bottom sheet.
 *
 * @param mainNav is the navigation controller passed in to share control
 */
@Composable
fun EditDetailsScreen(mainNav: NavController) {
    var name by remember { mutableStateOf(logged_in_name) }
    var email by remember { mutableStateOf(logged_in_email) }
    var phoneNumber by remember { mutableStateOf(logged_in_phone) }

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
            text = "Make Edits:",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button (
            onClick = { handleEditDetails(name, email, phoneNumber, dId, { exitCode ->
                if (exitCode == 0) {
                    mainNav.navigate("profile")
                } else if (exitCode == 1) {
                    errorMessage = "Fields don't meet requirements."
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
                text = "Submit"
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
 * @param onDone callback function to handle success levels
 */
fun handleEditDetails(name: String, email: String, phone: String,
                 deviceID: String, onDone : (Number) -> Unit) {
    if (name == "" || email == "" || phone == "" || deviceID == "") {
        onDone(1)
        return
    } // fail if fields are empty

    users.updateUser(deviceID, name, email, phone, null, { success ->
        if (!success) {
            onDone(1)
            return@updateUser
        } else {
            logged_in_name = name
            logged_in_device = deviceID
            logged_in_email = email
            logged_in_phone = phone
            onDone(0)
        }
    })
}

@Composable
fun notificationsScreen(mainNav: NavController) {
    var option1 by remember { mutableStateOf(false) }
    var option2 by remember { mutableStateOf(true) }
    var option3 by remember { mutableStateOf(true) }
    var option4 by remember { mutableStateOf(true) }
    var option5 by remember { mutableStateOf(true) }
    var option6 by remember { mutableStateOf(true) }
    var option7 by remember { mutableStateOf(true) }
    var option8 by remember { mutableStateOf(true) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 55.dp)
    ) {
        Text(
            text = "Change Notification Preferences",
            modifier = Modifier.padding(bottom = 20.dp),
            fontWeight = FontWeight.Bold
        )
        Row {
            Text(
                text="Opt out of all: ",
                modifier = Modifier.padding(bottom=20.dp),
                fontWeight = FontWeight.Bold
            )
            Switch(checked=option1, onCheckedChange = {option1=it})
        }
        Text(
            text="I want to receive a notification:",
            modifier = Modifier.padding(bottom=20.dp),
            fontWeight = FontWeight.Bold
        )
        Row {
            Text(
                text="...when I win the campsite lottery.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option2, onCheckedChange = {option2=it})
        }
        Row {
            Text(
                text="...when I lose the campsite lottery.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option3, onCheckedChange = {option3=it})
        }
        Row {
            Text(
                text="... when my campsite reservation is confirmed.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option4, onCheckedChange = {option4=it})
        }
        Row {
            Text(
                text="... when my campsite reservation is confirmed.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option5, onCheckedChange = {option5=it})
        }
        Row {
            Text(
                text="... when my reservation cancellation is confirmed.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option6, onCheckedChange = {option6=it})
        }
        Row {
            Text(
                text="... when I receive a message.",
                modifier = Modifier.padding(bottom=20.dp)
            )
            Switch(checked=option7, onCheckedChange = {option7=it})
        }
        Row {
            Text(
                text="I want to get my reservation\n" +
                        "by notification or CSV",
                modifier = Modifier.padding(bottom=20.dp),
                fontWeight = FontWeight.Bold
            )
            Switch(checked=option8, onCheckedChange = {option8=it})
        }
    }
}

fun signOut(mainNav: NavController) {
    mainNav.navigate("start")
}

fun deleteProfile(deviceID: String, mainNav: NavController) {
    users.deleteUser(deviceID, {
        mainNav.navigate("start")
    })
}