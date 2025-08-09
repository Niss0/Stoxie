package com.shahar.stoxie.ui.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shahar.stoxie.data.AuthRepository
import com.shahar.stoxie.data.StorageRepository
import com.shahar.stoxie.data.UserRepository
import com.shahar.stoxie.models.User
import kotlinx.coroutines.launch

/**
 * States for profile editing operations.
 * Manages UI feedback during profile updates and password reset.
 */
sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object Success : EditProfileState()
    data class Error(val message: String) : EditProfileState()
    data class PasswordResetSent(val message: String) : EditProfileState()
}

/**
 * ViewModel for profile editing screen.
 * Handles user profile updates, image uploads, and password reset.
 */
class EditProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val storageRepository = StorageRepository()
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Current user data for profile editing.
     */
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    /**
     * State management for profile editing operations.
     * Provides UI feedback during updates and password reset.
     */
    private val _editState = MutableLiveData<EditProfileState>(EditProfileState.Idle)
    val editState: LiveData<EditProfileState> = _editState

    /**
     * Temporary storage for new profile image selection.
     */
    private var newProfileImageUri: Uri? = null

    /**
     * Track if profile has been successfully saved.
     */
    private val _isProfileSaved = MutableLiveData<Boolean>(false)
    val isProfileSaved: LiveData<Boolean> = _isProfileSaved

    init {
        loadCurrentUser()
    }

    /**
     * Loads current user data for profile editing.
     * Fetches user information from repository on initialization.
     */
    private fun loadCurrentUser() {
        _editState.value = EditProfileState.Loading
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                _user.value = userRepository.getUser(uid)
            }
            _editState.value = EditProfileState.Idle
        }
    }

    /**
     * Handles new profile image selection.
     * @param uri The URI of the selected image
     */
    fun onNewProfileImageSelected(uri: Uri) {
        newProfileImageUri = uri
    }

    /**
     * Handles profile save operation.
     * Uploads new image if selected and updates user profile.
     * @param name The new user name
     * @param bio The new user bio
     */
    fun onSaveChangesClicked(name: String, bio: String) {
        viewModelScope.launch {
            _editState.value = EditProfileState.Loading
            try {
                val currentImageUrl = user.value?.profilePictureUrl
                val newImageUrl = newProfileImageUri?.let {
                    storageRepository.uploadProfileImage(it)
                } ?: currentImageUrl

                userRepository.updateUserProfile(name, bio, newImageUrl)

                // Update the local user data to reflect changes immediately
                val updatedUser = user.value?.copy(
                    name = name,
                    bio = bio,
                    profilePictureUrl = newImageUrl
                )
                _user.value = updatedUser

                _editState.value = EditProfileState.Success
                _isProfileSaved.value = true
            } catch (e: Exception) {
                _editState.value = EditProfileState.Error(e.message ?: "An unknown error occurred.")
                _isProfileSaved.value = false
            }
        }
    }

    /**
     * Handles password reset request.
     * Sends reset email to current user's email address.
     */
    fun onChangePasswordClicked() {
        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail.isNullOrBlank()) {
            _editState.value = EditProfileState.Error("Could not find user email for password reset.")
            return
        }

        viewModelScope.launch {
            _editState.value = EditProfileState.Loading
            try {
                authRepository.sendPasswordResetEmail(currentUserEmail)
                _editState.value = EditProfileState.PasswordResetSent("Password reset email sent to $currentUserEmail")
            } catch (e: Exception) {
                _editState.value = EditProfileState.Error(e.message ?: "Failed to send reset email.")
                Log.e("EditProfileViewModel", "Send password reset failed", e)
            }
        }
    }

    /**
     * Resets edit state after UI has processed the result.
     * Called by Fragment to clear success/error states.
     */
    fun onStateHandled() {
        _editState.value = EditProfileState.Idle
        _isProfileSaved.value = false
    }
}