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

class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

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

    fun getPostsForUser(userId: String): Flow<List<Post>> = callbackFlow {
        val postsCollection = firestore.collection("posts")
            .whereEqualTo("authorId", userId) // The key difference is this line
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
                authorName = author.name,
                authorProfilePictureUrl = author.profilePictureUrl,
                timestamp = System.currentTimeMillis(),
                likedBy = emptyList()
            )

            firestore.collection("posts").add(newPost).await()
        }
    }

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

            // Get a reference to the post document
            val postRef = firestore.collection("posts").document(postId)

            // Use a transaction to ensure both operations succeed or fail together
            firestore.runTransaction { transaction ->
                val newCommentRef = postRef.collection("comments").document()
                transaction.set(newCommentRef, newComment)
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
            }.await()
        }
    }
}
