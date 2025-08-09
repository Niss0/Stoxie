package com.shahar.stoxie.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for Firebase Storage operations.
 * Handles file uploads with automatic authentication checks.
 */
class StorageRepository {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Uploads profile image to Firebase Storage.
     * @param imageUri Local URI of the image to upload
     * @return Download URL of the uploaded image
     * @throws IllegalStateException if user is not logged in
     */
    suspend fun uploadProfileImage(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("profile_pictures/$uid/$fileName")

            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}