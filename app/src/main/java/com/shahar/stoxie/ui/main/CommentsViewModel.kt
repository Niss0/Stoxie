package com.shahar.stoxie.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.shahar.stoxie.data.PostRepository
import com.shahar.stoxie.models.Comment
import kotlinx.coroutines.launch

/**
 * States for comment addition operations.
 * Manages UI feedback during comment posting process.
 */
sealed class AddCommentState {
    object Idle : AddCommentState()
    object Loading : AddCommentState()
    object Success : AddCommentState()
    data class Error(val message: String) : AddCommentState()
}

/**
 * The ViewModel for the CommentsFragment.
 * It requires a postId to be passed during its creation.
 */
class CommentsViewModel(private val postId: String) : ViewModel() {

    private val postRepository = PostRepository()

    /**
     * Real-time stream of comments for the specified post.
     * Converts Flow to LiveData for UI observation.
     */
    val comments: LiveData<List<Comment>> = postRepository.getCommentsForPost(postId).asLiveData()

    /**
     * State management for comment addition operations.
     * Provides UI feedback during posting process.
     */
    private val _addCommentState = MutableLiveData<AddCommentState>(AddCommentState.Idle)
    val addCommentState: LiveData<AddCommentState> = _addCommentState

    /**
     * Handles comment submission from UI.
     * Validates input and manages state transitions during posting.
     * @param commentText The comment text to post
     */
    fun onSendCommentClicked(commentText: String) {
        if (commentText.isBlank()) {
            _addCommentState.value = AddCommentState.Error("Comment cannot be empty.")
            return
        }

        viewModelScope.launch {
            _addCommentState.value = AddCommentState.Loading
            try {
                postRepository.addCommentToPost(postId, commentText)
                _addCommentState.value = AddCommentState.Success
            } catch (e: Exception) {
                _addCommentState.value = AddCommentState.Error(e.message ?: "Failed to post comment.")
            }
        }
    }

    /**
     * Resets comment state after UI has processed the result.
     * Called by Fragment to clear success/error states.
     */
    fun onStateHandled() {
        _addCommentState.value = AddCommentState.Idle
    }
}

/**
 * Factory for creating CommentsViewModel with required postId parameter.
 * Enables dependency injection for ViewModel constructor arguments.
 */
class CommentsViewModelFactory(private val postId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(postId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
