package com.shahar.stoxie.models

/**
 * Represents a single post made by a user in the social feed.
 *
 * @property id The unique document ID for this post in the Firestore 'posts' collection.
 * @property text The main content of the post.
 * @property authorId The 'uid' of the user who created this post. This is essential for linking the post back to its author.
 * @property timestamp The time the post was created, stored as milliseconds since the epoch. This allows us to sort posts chronologically.
 * @property authorName The name of the post's author. We store this directly in the post object to avoid having to do a separate database lookup for the user's name every time we display a post. This is a common optimization technique called "denormalization".
 * @property authorProfilePictureUrl The profile picture URL of the author. Also denormalized for the same reason as authorName.
 */

data class Post(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val timestamp: Long = 0L,
    // Denormalized data for easy display in the RecyclerView
    val authorName: String = "",
    val authorProfilePictureUrl: String? = null,
    val likedBy: List<String> = emptyList(),
    val commentCount: Long = 0L

)
