package com.shahar.stoxie.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentLoginBinding

/**
 * The Fragment responsible for displaying the user login UI.
 * This is the 'View' in our MVVM architecture for the login feature.
 */
class LoginFragment : Fragment() {

    // View Binding for safe access to the layout views.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Get a reference to the LoginViewModel using the 'by viewModels()' delegate.
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the click listener for the main login button.
        binding.btnLoginSubmit.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()
            viewModel.onLoginClicked(email, password)
        }

        // Set up the click listener for the "Go to Register" text view.
        binding.tvLoginGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            Log.d("LoginFragment", "Navigate to RegisterFragment clicked.")
        }

        // Observe the login state from the ViewModel to reactively update the UI.
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            Log.d("LoginFragment", "State changed to: $state")

            // Each 'when' branch completely defines the UI for that state.
            when (state) {
                is LoginState.Loading -> {
                    binding.pbLoginLoading.isVisible = true
                    binding.btnLoginSubmit.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.pbLoginLoading.isVisible = false
                    binding.btnLoginSubmit.isEnabled = true
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                    viewModel.onStateHandled()

                    findNavController().navigate(R.id.action_global_feedFragment)
                    Log.d("LoginFragment", "Navigate to FeedFragment.")
                }
                is LoginState.Error -> {
                    binding.pbLoginLoading.isVisible = false
                    binding.btnLoginSubmit.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is LoginState.Idle -> {
                    binding.pbLoginLoading.isVisible = false
                    binding.btnLoginSubmit.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
