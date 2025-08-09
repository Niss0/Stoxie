package com.shahar.stoxie.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.models.Post
import com.shahar.stoxie.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for social post operations.
 * Handles post creation, retrieval, likes, and comments using Firestore.
 */
class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Retrieves all posts in real-time with descending timestamp order.
     * Uses Firestore snapshot listener for live updates.
     * @return Flow of all posts with document IDs
     */
    fun getAllPosts(): Flow<List<Post>> = callbackFlow {
        val postsCollection = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PostRepository", "Error listening for post updates", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val posts = snapshot.toObjects(Post::class.java)
                val postsWithIds = posts.mapIndexed { index, post ->
                    post.copy(id = snapshot.documents[index].id)
                }
                trySend(postsWithIds)
            }
        }

        awaitClose {
            Log.d("PostRepository", "Closing posts listener.")
            listener.remove()
        }
    }

    /**
     * Retrieves posts for a specific user in real-time.
     * Filters posts by author ID and orders by timestamp.
     * @param userId The user ID to filter posts by
     * @return Flow of user's posts with document IDs
     */
    fun getPostsForUser(userId: String): Flow<List<Post>> = callbackFlow {
        val postsCollection = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PostRepository", "Error listening for user posts", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val posts = snapshot.toObjects(Post::class.java)
                val postsWithIds = posts.mapIndexed { index, post ->
                    post.copy(id = snapshot.documents[index].id)
                }
                trySend(postsWithIds)
            }
        }

        awaitClose {
            Log.d("PostRepository", "Closing user posts listener.")
            listener.remove()
        }
    }

    /**
     * Creates a new post for the current authenticated user.
     * Fetches user profile and creates post with current timestamp.
     * @param postText The content of the post to create
     * @throws IllegalStateException if user is not logged in or profile not found
     */
    suspend fun createPost(postText: String) {
        withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
                ?: throw IllegalStateException("User must be logged in to create a post.")

            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val author = userDoc.toObject(User::class.java)
                ?: throw IllegalStateException("User profile not found in Firestore.")

            val newPost = Post(
                text = postText,
                authorId = author.uid,
                timestamp = System.currentTimeMillis(),
                likedBy = emptyList()
            )

            firestore.collection("posts").add(newPost).await()
        }
    }

    /**
     * Updates like status for a post by adding/removing user ID from likedBy array.
     * @param postId The ID of the post to update
     * @param userId The ID of the user liking/unliking
     * @param isCurrentlyLiked Whether the user currently likes the post
     */
    suspend fun updateLikeStatus(postId: String, userId: String, isCurrentlyLiked: Boolean) {
        withContext(Dispatchers.IO) {
            val postRef = firestore.collection("posts").document(postId)
            if (isCurrentlyLiked) {
                postRef.update("likedBy", FieldValue.arrayRemove(userId)).await()
            } else {
                postRef.update("likedBy", FieldValue.arrayUnion(userId)).await()
            }
        }
    }

    /**
     * Retrieves comments for a specific post in real-time.
     * Orders comments by timestamp in ascending order.
     * @param postId The ID of the post to get comments for
     * @return Flow of comments with document IDs
     */
    fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val commentsCollection = firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = commentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val comments = snapshot.toObjects(Comment::class.java).mapIndexed { index, comment ->
                    comment.copy(id = snapshot.documents[index].id)
                }
                trySend(comments)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Adds a comment to a specific post using Firestore transaction.
     * Ensures both comment creation and post comment count update succeed together.
     * @param postId The ID of the post to comment on
     * @param commentText The text content of the comment
     * @throws IllegalStateException if user is not logged in or profile not found
     */
    suspend fun addCommentToPost(postId: String, commentText: String) {
        withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
                ?: throw IllegalStateException("User must be logged in to comment.")

            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val author = userDoc.toObject(User::class.java)
                ?: throw IllegalStateException("User profile not found for commenting.")

            val newComment = Comment(
                authorId = author.uid,
                authorName = author.name,
                postId = postId,
                text = commentText,
                timestamp = System.currentTimeMillis()
            )

            val postRef = firestore.collection("posts").document(postId)

            firestore.runTransaction { transaction ->
                val newCommentRef = postRef.collection("comments").document()
                transaction.set(newCommentRef, newComment)
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
            }.await()
        }
    }
}
