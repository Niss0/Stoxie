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
 * Fragment for user authentication login screen.
 * Provides email/password login and navigation to registration.
 */
class LoginFragment : Fragment() {

    /**
     * View binding for safe view access.
     */
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    /**
     * ViewModel for login business logic and state management.
     */
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
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Sets up click listeners for user interactions.
     * Handles login submission and navigation to registration.
     */
    private fun setupClickListeners() {
        binding.btnLoginSubmit.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()
            viewModel.onLoginClicked(email, password)
        }

        binding.tvLoginGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            Log.d("LoginFragment", "Navigate to RegisterFragment clicked.")
        }
    }

    /**
     * Observes ViewModel LiveData for UI updates.
     * Handles login state changes and navigation.
     */
    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            Log.d("LoginFragment", "State changed to: $state")

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
        _binding = null // Prevent memory leaks
    }
}