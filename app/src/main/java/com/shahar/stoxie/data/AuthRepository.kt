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
 * Repository for authentication operations.
 * Handles user registration, login, logout, and password reset using Firebase.
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Registers a new user with email and password.
     * Creates both authentication account and user profile in Firestore.
     */
    suspend fun registerUser(name: String, email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase user is null after authentication.")

            val newUser = User(
                uid = firebaseUser.uid,
                name = name,
                email = email
            )

            firestore.collection("users").document(firebaseUser.uid).set(newUser).await()

            authResult
        }
    }

    /**
     * Authenticates existing user with email and password.
     */
    suspend fun loginUser(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }
    }

    /**
     * Signs out the current user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Sends password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        withContext(Dispatchers.IO) {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }
}