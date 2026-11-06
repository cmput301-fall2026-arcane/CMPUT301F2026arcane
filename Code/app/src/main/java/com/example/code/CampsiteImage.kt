package com.example.code

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

/* US 02.01.02 As a campsite owner, I want to provide campsite details

The optional campsite photo. The form picks an image from the gallery and hands the uri here,
and the download url that comes back is what gets saved on the campsite
*/

/**
 * The outcome of an upload
 */
sealed class ImageResult {
    data class Success(val url: String, val path: String) : ImageResult()
    data class Failure(val message: String) : ImageResult()
}

/**
 * Uploads and deletes the campsite photos held in Firebase Storage
 *
 * @param storage the Storage instance to use
 */
class CampsiteImage (storage: FirebaseStorage = FirebaseStorage.getInstance()) {
    private val root = storage.reference

    /**
     * Uploads the picked image as the photo for a campsite, replacing one already there
     *
     * @param campsiteId id of the campsite the photo belongs to
     * @param uri the image the owner picked from the gallery
     * @param onResult called with the download url and the storage path, or the failure
     */
    fun uploadImage(campsiteId: String, uri: Uri, onResult: (ImageResult) -> Unit) {
        if (campsiteId.isEmpty()) {
            onResult(ImageResult.Failure("Save the campsite before adding a photo"))
            return
        }

        val path = "campsites/$campsiteId/cover"
        val ref = root.child(path)

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url -> onResult(ImageResult.Success(url.toString(), path)) }
                    .addOnFailureListener { e ->
                        onResult(ImageResult.Failure(e.message ?: "Could not read the photo back"))
                    }
            }
            .addOnFailureListener { e ->
                onResult(ImageResult.Failure(e.message ?: "Could not upload the photo"))
            }
    }

    /**
     * Deletes a photo, used when the owner removes it and when a campsite or profile is removed
     *
     * @param path the storage path saved on the campsite
     * @param onResult called with true if the photo is gone
     */
    fun deleteImage(path: String, onResult: (Boolean) -> Unit) {
        if (path.isEmpty()) {
            onResult(true)
            return
        }
        root.child(path)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
