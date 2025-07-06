package com.shahar.stoxie.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class StorageRepository {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun uploadProfileImage(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            // Create a unique file name for the image to prevent overwrites
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("profile_pictures/$uid/$fileName")

            // Upload the file
            imageRef.putFile(imageUri).await()

            // Get the public download URL for the uploaded image
            imageRef.downloadUrl.await().toString()
        }
    }
}