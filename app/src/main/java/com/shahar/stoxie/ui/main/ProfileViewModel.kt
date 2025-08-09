package com.shahar.stoxie.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahar.stoxie.data.AuthRepository
import com.shahar.stoxie.data.PostRepository
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.models.Post
import com.shahar.stoxie.models.User
import com.shahar.stoxie.ui.adapters.PostUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for user profile management.
 * Handles user data, posts, comments, and logout functionality.
 */
class ProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val postRepository = PostRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    init {
        loadUser()
    }

    /**
     * Loads current user profile from Firestore.
     */
    private fun loadUser() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    _user.value = userDoc.toObject(User::class.java)
                } catch (e: Exception) {
                    _user.value = null
                }
            } else {
                _user.value = null
            }
        }
    }

    /**
     * Refreshes user data from Firestore.
     * Called when returning from edit profile to ensure updated data is displayed.
     */
    fun refreshUser() {
        loadUser()
    }

    private val userPostsFlow = postRepository.getPostsForUser(firebaseAuth.currentUser?.uid ?: "")
    private val commentsByPostId = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    private val expandedPostIds = MutableStateFlow<Set<String>>(emptySet())
    private val commentFetchJobs = mutableMapOf<String, Job>()

    /**
     * LiveData combining user posts with comments and expansion state.
     */
    val postUiModels: LiveData<List<PostUiModel>> = combine(
        userPostsFlow,
        commentsByPostId,
        expandedPostIds
    ) {  posts, comments, expandedIds ->
        posts.forEach { post ->
            if (post.id in expandedIds && !commentFetchJobs.containsKey(post.id)) {
                fetchCommentsForPost(post.id)
            }
        }
        posts.map { post ->
            PostUiModel(
                post = post,
                comments = comments[post.id] ?: emptyList(),
                areCommentsVisible = post.id in expandedIds
            )
        }
    }.asLiveData()

    /**
     * Fetches comments for a specific post with lazy loading.
     */
    private fun fetchCommentsForPost(postId: String) {
        commentFetchJobs[postId] = viewModelScope.launch {
            postRepository.getCommentsForPost(postId).collect { comments ->
                val currentCommentsMap = commentsByPostId.value.toMutableMap()
                currentCommentsMap[postId] = comments
                commentsByPostId.value = currentCommentsMap
            }
        }
    }

    /**
     * Handles like/unlike functionality for posts.
     */
    fun onLikeClicked(post: Post) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                postRepository.updateLikeStatus(post.id, userId, post.likedBy.contains(userId))
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating like status", e)
            }
        }
    }

    /**
     * Adds a new comment to a post.
     */
    fun onAddCommentClicked(postId: String, commentText: String) {
        viewModelScope.launch {
            try {
                postRepository.addCommentToPost(postId, commentText)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error adding comment", e)
            }
        }
    }

    /**
     * Toggles comment visibility for a post with lazy loading.
     */
    fun onToggleCommentsClicked(post: Post) {
        val postId = post.id
        val currentExpandedIds = expandedPostIds.value.toMutableSet()

        if (postId in currentExpandedIds) {
            currentExpandedIds.remove(postId)
            commentFetchJobs[postId]?.cancel()
            commentFetchJobs.remove(postId)
        } else {
            currentExpandedIds.add(postId)
            if (!commentsByPostId.value.containsKey(postId)) {
                fetchCommentsForPost(postId)
            }
        }
        expandedPostIds.value = currentExpandedIds
    }

    /**
     * Logs out the current user and clears all cached data.
     * This ensures no user data persists when switching accounts.
     */
    fun logout() {
        // Clear all cached data
        commentsByPostId.value = emptyMap()
        expandedPostIds.value = emptySet()
        commentFetchJobs.values.forEach { it.cancel() }
        commentFetchJobs.clear()
        
        // Logout from Firebase Auth
        authRepository.logout()
    }
}