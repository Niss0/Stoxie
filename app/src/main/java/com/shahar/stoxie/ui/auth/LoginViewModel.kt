package com.shahar.stoxie.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.shahar.stoxie.data.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the possible states of the login process, ensuring the UI
 * can reactively handle every outcome.
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * The ViewModel for the [LoginFragment]. It handles the business logic for user login
 * and manages the state of the login screen.
 */
class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // Private MutableLiveData to hold the current login state.
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    // Public LiveData that the Fragment observes for UI updates.
    val loginState: LiveData<LoginState> = _loginState

    /**
     * Initiates the login process when the user clicks the login button.
     * @param email The user's email.
     * @param password The user's password.
     */
    fun onLoginClicked(email: String, password: String) {
        // Basic validation for email and password fields.
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty.")
            return
        }

        // Launch a coroutine for the login network operation.
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                Log.d("LoginViewModel", "Calling authRepository.loginUser...")
                val authResult = authRepository.loginUser(email, password)
                Log.d("LoginViewModel", "authRepository.loginUser call finished.")

                // Switch back to the main thread to update LiveData.
                withContext(Dispatchers.Main) {
                    _loginState.value = LoginState.Success(authResult.user!!)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "An exception occurred during login", e)
                withContext(Dispatchers.Main) {
                    _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred.")
                }
            }
        }
    }

    /**
     * Resets the state to Idle. This should be called from the Fragment
     * after a Success or Error state has been handled to prevent stale states.
     */
    fun onStateHandled() {
        _loginState.value = LoginState.Idle
    }
}