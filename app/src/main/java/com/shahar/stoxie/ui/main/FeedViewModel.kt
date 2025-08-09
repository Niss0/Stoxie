package com.shahar.stoxie.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shahar.stoxie.data.PostRepository
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.models.Post
import com.shahar.stoxie.ui.adapters.PostUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for social feed management.
 * Handles posts, comments, likes, and comment expansion with lazy loading.
 */
class FeedViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val postsFlow = postRepository.getAllPosts()
    private val commentsByPostId = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    private val expandedPostIds = MutableStateFlow<Set<String>>(emptySet())
    private val commentFetchJobs = mutableMapOf<String, Job>()

    /**
     * LiveData combining posts, comments, and expansion state.
     */
    val postUiModels: LiveData<List<PostUiModel>> = combine(
        postsFlow,
        commentsByPostId,
        expandedPostIds
    ) { posts, comments, expandedIds ->
        // Trigger comment fetching for newly expanded posts
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
     * Fetches comments for a specific post.
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
                Log.e("FeedViewModel", "Error updating like status", e)
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
                Log.e("FeedViewModel", "Error adding comment", e)
            }
        }
    }

    /**
     * Toggles comment visibility for a post.
     * Implements lazy loading - comments only fetched when first expanded.
     */
    fun onToggleCommentsClicked(post: Post) {
        val postId = post.id
        val currentExpandedIds = expandedPostIds.value.toMutableSet()

        if (postId in currentExpandedIds) {
            // Collapse comments
            currentExpandedIds.remove(postId)
            commentFetchJobs[postId]?.cancel()
            commentFetchJobs.remove(postId)
        } else {
            // Expand comments
            currentExpandedIds.add(postId)
            if (!commentsByPostId.value.containsKey(postId)) {
                fetchCommentsForPost(postId)
            }
        }
        expandedPostIds.value = currentExpandedIds
    }

    /**
     * Clears all feed data when user logs out.
     */
    fun clearFeedData() {
        commentsByPostId.value = emptyMap()
        expandedPostIds.value = emptySet()
        commentFetchJobs.values.forEach { it.cancel() }
        commentFetchJobs.clear()
    }

    /**
     * Logs out and clears all cached feed data.
     */
    fun logout() {
        clearFeedData()
        // Note: AuthRepository logout is handled by ProfileViewModel
    }
}