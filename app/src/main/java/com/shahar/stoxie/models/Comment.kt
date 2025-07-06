package com.shahar.stoxie.models

/**
 * Represents a single comment on a Post.
 * @property id The unique document ID for this comment.
 * @property authorId The 'uid' of the user who wrote the comment.
 * @property authorName The name of the comment's author (denormalized).
 * @property postId The ID of the post to which this comment belongs.
 * @property text The content of the comment.
 * @property timestamp The time the comment was created.
 */

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val postId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
