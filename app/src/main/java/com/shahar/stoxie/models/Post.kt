package com.shahar.stoxie.models

/**
 * Represents a single post made by a user in the social feed.
 *
 * @property id The unique document ID for this post in the Firestore 'posts' collection.
 * @property text The main content of the post.
 * @property authorId The 'uid' of the user who created this post. This is essential for linking the post back to its author.
 * @property timestamp The time the post was created, stored as milliseconds since the epoch. This allows us to sort posts chronologically.
 * @property likedBy List of user IDs who liked this post.
 * @property commentCount Number of comments on this post.
 *
 * NOTE: We no longer denormalize authorName or authorProfilePictureUrl. Always fetch the latest user info by authorId.
 */
data class Post(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val timestamp: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val commentCount: Long = 0L
)
