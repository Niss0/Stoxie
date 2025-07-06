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

// Define the states for the add comment operation
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

    // Get a real-time stream of comments for the given postId and expose it as LiveData.
    val comments: LiveData<List<Comment>> = postRepository.getCommentsForPost(postId).asLiveData()

    // LiveData to hold the state of the "add comment" operation.
    private val _addCommentState = MutableLiveData<AddCommentState>(AddCommentState.Idle)
    val addCommentState: LiveData<AddCommentState> = _addCommentState

    /**
     * Called when the user clicks the send button.
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
     * Resets the add comment state after it has been handled by the UI.
     */
    fun onStateHandled() {
        _addCommentState.value = AddCommentState.Idle
    }
}

/**
 * A ViewModelProvider.Factory for creating a CommentsViewModel with a postId.
 * This is the standard pattern for passing arguments to a ViewModel's constructor.
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
