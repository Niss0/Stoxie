package com.shahar.stoxie.models

/**
 * Represents a user in the application, storing their profile information.
 * This data class is used to model user data retrieved from Firestore.
 *
 * @property uid The unique identifier for the user, sourced from Firebase Authentication. This serves as the primary key.
 * @property name The display name of the user, as provided during the registration process.
 * @property bio A short, optional biography that the user can set on their profile. It is nullable as it's not a required field.
 * @property email The user's email address, used for login and communication.
 * @property profilePictureUrl The URL pointing to the user's profile image stored in Firebase Cloud Storage. It is nullable, as a user might not have uploaded a picture.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val bio: String? = null,
    val email: String = "",
    val profilePictureUrl: String? = null
)