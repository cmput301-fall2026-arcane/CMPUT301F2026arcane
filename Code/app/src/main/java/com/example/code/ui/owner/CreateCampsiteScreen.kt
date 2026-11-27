package com.example.code.ui.owner

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.col
import com.example.code.logged_in_device
import com.example.code.model.Campsite
import com.example.code.repositories.CampsiteImage
import com.example.code.repositories.CampsiteRepository
import com.example.code.repositories.ImageResult
import com.example.code.rules.CampsiteField
import com.example.code.rules.errorFor
import com.example.code.rules.normalizeTags
import com.example.code.rules.validateCampsite
import com.example.code.ui.navigation.BottomNavBar

/* US 02.01.02 As a campsite owner, I want to provide campsite details including name, location,
description (amenities), maximum occupancy, map location, optional image, and tags

The Create your Campsite form. Checks every field with validateCampsite first and saves nothing
if any are wrong. If an image was picked it is uploaded first, then the campsite is saved
*/

@Composable
fun CreateCampsiteScreen(nav: NavController) {
    val context = LocalContext.current
    val repo = remember { CampsiteRepository() }
    val images = remember { CampsiteImage() }

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxOccupancy by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var errors by remember { mutableStateOf(listOf<CampsiteField>()) }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imageUri = uri
    }

    // small preview of the picked image
    val preview = remember(imageUri) {
        imageUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 4 })
                }
            }.getOrNull()
        }
    }

    fun errorText(field: CampsiteField): String? = if (field in errors) errorFor(field) else null

    fun save() {
        val lat = latitude.trim().toDoubleOrNull() ?: 0.0
        val lng = longitude.trim().toDoubleOrNull() ?: 0.0
        errors = validateCampsite(name, location, maxOccupancy, price, lat, lng)
        if (errors.isNotEmpty()) {
            message = "Fix the fields in red"
            return
        }

        saving = true
        message = ""
        val id = repo.newCampsiteId()
        val draft = Campsite(
            name = name.trim(),
            location = location.trim(),
            description = description.trim(),
            maxOccupancy = maxOccupancy.trim().toInt(),
            pricePerNight = price.trim().toDouble(),
            latitude = lat,
            longitude = lng,
            tags = normalizeTags(tags).toMutableList()
        )

        val finish = { campsite: Campsite ->
            repo.registerCampsite(logged_in_device, campsite, id) { ok ->
                saving = false
                if (ok) nav.popBackStack() else message = "Could not save the campsite"
            }
        }

        val uri = imageUri
        if (uri == null) {
            finish(draft)
        } else {
            images.uploadImage(id, uri) { result ->
                when (result) {
                    is ImageResult.Success -> finish(draft.copy(imageUrl = result.url, imagePath = result.path))
                    is ImageResult.Failure -> {
                        saving = false
                        message = result.message
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Create your Campsite", color = Color.White, fontSize = 26.sp)
            Spacer(Modifier.height(16.dp))

            FormField("Campsite name *", name, { name = it }, errorText(CampsiteField.NAME))
            FormField("Location *", location, { location = it }, errorText(CampsiteField.LOCATION))
            FormField("Description of amenities", description, { description = it })
            FormField("Max occupancy *", maxOccupancy, { maxOccupancy = it }, errorText(CampsiteField.MAX_OCCUPANCY), KeyboardType.Number)
            FormField("Price per night *", price, { price = it }, errorText(CampsiteField.PRICE), KeyboardType.Decimal)
            FormField("Latitude *", latitude, { latitude = it }, errorText(CampsiteField.MAP_LOCATION))
            FormField("Longitude *", longitude, { longitude = it }, errorText(CampsiteField.MAP_LOCATION))
            FormField("Tags (comma separated)", tags, { tags = it })

            Button(
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = col.HomeButton),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (imageUri == null) "Pick image (optional)" else "Change image", color = col.HomeButtonText)
            }

            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Campsite image preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp)
                )
            }

            if (message.isNotEmpty()) {
                Text(message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = { save() },
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = col.SubmitButton),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (saving) "Saving..." else "Create Campsite")
            }
        }
        BottomNavBar("campOwnerHome", nav)
    }
}

/**
 * One text box on the form, with the error shown under it when the field is wrong
 */
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null) { { Text(error) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = col.UnfocusedInputContainer,
            focusedContainerColor = col.FocusedInputContainer,
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    )
}
