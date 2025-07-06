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

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object Success : EditProfileState()
    data class Error(val message: String) : EditProfileState()
    data class PasswordResetSent(val message: String) : EditProfileState()
}

class EditProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val storageRepository = StorageRepository()
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _editState = MutableLiveData<EditProfileState>(EditProfileState.Idle)
    val editState: LiveData<EditProfileState> = _editState

    private var newProfileImageUri: Uri? = null

    init {
        loadCurrentUser()
    }

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

    fun onNewProfileImageSelected(uri: Uri) {
        newProfileImageUri = uri
    }

    fun onSaveChangesClicked(name: String, bio: String) {
        viewModelScope.launch {
            _editState.value = EditProfileState.Loading
            try {
                val currentImageUrl = user.value?.profilePictureUrl
                val newImageUrl = newProfileImageUri?.let {
                    storageRepository.uploadProfileImage(it)
                } ?: currentImageUrl

                userRepository.updateUserProfile(name, bio, newImageUrl)

                _editState.value = EditProfileState.Success
            } catch (e: Exception) {
                _editState.value = EditProfileState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // --- THIS IS THE CORRECTED FUNCTION ---
    fun onChangePasswordClicked() {
        // Get the email directly from the authoritative source: FirebaseAuth
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

    fun onStateHandled() {
        _editState.value = EditProfileState.Idle
    }
}