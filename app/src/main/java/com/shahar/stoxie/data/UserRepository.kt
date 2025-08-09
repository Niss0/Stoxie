package com.shahar.stoxie.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahar.stoxie.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for user profile operations.
 * Manages user data retrieval and profile updates in Firestore.
 */
class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Retrieves user profile by UID.
     * @param uid The user ID to fetch
     * @return User object or null if not found
     */
    suspend fun getUser(uid: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                usersCollection.document(uid).get().await().toObject(User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Updates current user's profile information.
     * @param name Display name
     * @param bio User biography
     * @param profilePictureUrl Profile image URL (nullable)
     * @throws IllegalStateException if user is not logged in
     */
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

            // Use update to preserve other fields, filter nulls to avoid overwriting
            userDocRef.update(updates.filterValues { it != null }).await()
        }
    }
}