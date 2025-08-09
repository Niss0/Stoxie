package com.shahar.stoxie.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahar.stoxie.data.PostRepository
import kotlinx.coroutines.launch

/**
 * States for post creation process.
 * Manages UI feedback during post publishing operations.
 */
sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

/**
 * ViewModel for post creation screen.
 * Handles post validation, publishing, and state management.
 */
class CreatePostViewModel : ViewModel() {

    private val postRepository = PostRepository()

    /**
     * State management for post creation operations.
     * Provides UI feedback during publishing process.
     */
    private val _createPostState = MutableLiveData<CreatePostState>(CreatePostState.Idle)
    val createPostState: LiveData<CreatePostState> = _createPostState

    /**
     * Handles post publishing from UI.
     * Validates content and manages state transitions during publishing.
     * @param text The post content to publish
     */
    fun onPublishClicked(text: String) {
        if (text.isBlank()) {
            _createPostState.value = CreatePostState.Error("Post cannot be empty.")
            return
        }

        viewModelScope.launch {
            _createPostState.value = CreatePostState.Loading
            try {
                postRepository.createPost(text)
                _createPostState.value = CreatePostState.Success
            } catch (e: Exception) {
                Log.e("CreatePostViewModel", "Error creating post", e)
                _createPostState.value = CreatePostState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    /**
     * Resets post creation state after UI has processed the result.
     * Called by Fragment to clear success/error states.
     */
    fun onStateHandled() {
        _createPostState.value = CreatePostState.Idle
    }
}
