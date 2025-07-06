package com.shahar.stoxie.ui.auth // Use your app's package name

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.shahar.stoxie.data.AuthRepository
import kotlinx.coroutines.Dispatchers // Make sure Dispatchers is imported
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Make sure withContext is imported

// A sealed class to represent the different states of the registration process.
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun onRegisterClicked(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("All fields are required.")
            return
        }
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val authResult = authRepository.registerUser(name, email, password)
                // Check if the user object is null, which can sometimes happen
                if (authResult.user == null) {
                    Log.e("RegisterViewModel", "Firebase auth successful but user object is null.")
                    throw IllegalStateException("Firebase user is null after registration.")
                }

                withContext(Dispatchers.Main) {
                    _registerState.value = RegisterState.Success(authResult.user!!)
                }

            } catch (e: Exception) {
                // Log the full exception to see the exact error
                Log.e("RegisterViewModel", "An exception occurred", e)
                withContext(Dispatchers.Main) {
                    _registerState.value = RegisterState.Error(e.message ?: "An unknown error occurred.")
                }
            }
        }
    }

    fun onStateHandled() {
        _registerState.value = RegisterState.Idle
    }
}
