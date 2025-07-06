package com.shahar.stoxie.data

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahar.stoxie.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * A Repository class that handles all data operations related to authentication.
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(name: String, email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            // Step 1: Create the user in Firebase Authentication.
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            // Step 2: Save the user's profile to Firestore.
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase user is null after authentication.")

            // --- UPDATE: Save the email address to the User object ---
            val newUser = User(
                uid = firebaseUser.uid,
                name = name,
                email = email // Save the email here
            )

            firestore.collection("users").document(firebaseUser.uid).set(newUser).await()

            authResult
        }
    }

    suspend fun loginUser(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        withContext(Dispatchers.IO) {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }
}