package com.shahar.stoxie.models

/**
 * Represents a user in the application.
 * @property uid The unique identifier for the user, which comes directly from Firebase Authentication. This is the primary key.
 * @property name The display name of the user, provided during registration.
 * @property bio A short, optional biography that the user can set on their profile. It's nullable because it's not required.
 * @property profilePictureUrl The URL pointing to the user's profile image stored in Firebase Cloud Storage. Nullable as a user might not upload a picture.
 */

data class User(
    val uid: String = "",
    val name: String = "",
    val bio: String? = null,
    val email: String = "",
    val profilePictureUrl: String? = null
)

