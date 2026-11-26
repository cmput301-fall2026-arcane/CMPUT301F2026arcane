package com.example.code

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun uploadProfileImg(uri: Uri, deviceId: String, onComplete: (String) -> Unit) {
    val location = FirebaseStorage.getInstance().reference.child("profiles/$deviceId.jpg")
    location.putFile(uri)
        .continueWithTask { task ->
            if (!task.isSuccessful) throw task.exception ?: Exception("Image upload failed.")
            location.downloadUrl
        }
        .addOnSuccessListener { du -> onComplete(du.toString()) }
        .addOnFailureListener { onComplete("") }
}

@Composable
fun ImageOptions(url : String, onSelected : (Uri) -> Unit, onDelete: () -> Unit, onCancel: () -> Unit) {

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onSelected(it) } }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "-- Image Options --",
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Button(
            onClick = { imagePicker.launch("image/*"); },
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text(text = "Set New Image")
        }
        OutlinedButton(
            onClick = { onDelete() },
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text(text = "Delete Image")
        }
        OutlinedButton(
            onClick = { onCancel() }
        ) {
            Text(text = "Cancel")
        }
    }
}