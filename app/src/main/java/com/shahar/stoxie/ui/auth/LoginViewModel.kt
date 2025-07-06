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
 * A sealed class to represent the different states of the login process.
 * This ensures our UI handles every possible outcome explicitly.
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // The private MutableLiveData that holds the current state.
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    // The public LiveData that the Fragment will observe.
    val loginState: LiveData<LoginState> = _loginState

    /**
     * Called by the LoginFragment when the user clicks the login button.
     */
    fun onLoginClicked(email: String, password: String) {
        // Basic validation
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty.")
            return
        }

        // Launch a coroutine in the viewModelScope. It will be automatically
        // cancelled if the ViewModel is destroyed.
        viewModelScope.launch {
            // Set the state to Loading before starting the background operation.
            _loginState.value = LoginState.Loading
            try {
                Log.d("LoginViewModel", "Calling authRepository.loginUser...")
                // The repository will switch to a background thread for this call.
                val authResult = authRepository.loginUser(email, password)
                Log.d("LoginViewModel", "authRepository.loginUser call finished.")

                // After the background work is done, switch back to the main thread
                // to safely update the LiveData.
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
