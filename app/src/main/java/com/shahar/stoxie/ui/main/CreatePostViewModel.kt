package com.shahar.stoxie.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahar.stoxie.data.PostRepository
import kotlinx.coroutines.launch

/**
 * A sealed class to represent the different states of the post creation process.
 */
sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class CreatePostViewModel : ViewModel() {

    private val postRepository = PostRepository()

    private val _createPostState = MutableLiveData<CreatePostState>(CreatePostState.Idle)
    val createPostState: LiveData<CreatePostState> = _createPostState

    /**
     * Called by the Fragment when the user clicks the "Publish" button.
     * @param text The content of the post from the EditText.
     */
    fun onPublishClicked(text: String) {
        // Simple validation to prevent empty posts.
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
     * Resets the state to Idle after a Success or Error event has been handled by the UI.
     */
    fun onStateHandled() {
        _createPostState.value = CreatePostState.Idle
    }
}
