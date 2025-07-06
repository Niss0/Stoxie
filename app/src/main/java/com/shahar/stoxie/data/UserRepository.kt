package com.shahar.stoxie.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahar.stoxie.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(uid).get().await().toObject(User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserProfile(
        name: String,
        bio: String,
        profilePictureUrl: String?
    ) {
        withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val userDocRef = usersCollection.document(uid)

            val updates = mapOf(
                "name" to name,
                "bio" to bio,
                "profilePictureUrl" to profilePictureUrl
            )

            // We use update instead of set to avoid overwriting other fields.
            // We filter out null values so we don't accidentally write a null URL
            // if the user doesn't change their picture.
            userDocRef.update(updates.filterValues { it != null }).await()
        }
    }
}