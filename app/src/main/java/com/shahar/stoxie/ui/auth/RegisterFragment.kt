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
import com.shahar.stoxie.databinding.FragmentRegisterBinding

/**
 * Fragment for user account registration screen.
 * Provides account creation interface and navigation to login.
 */
class RegisterFragment : Fragment() {

    /**
     * View binding for safe view access.
     */
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    /**
     * ViewModel for registration business logic and state management.
     */
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Sets up click listeners for user interactions.
     * Handles registration submission from input fields.
     */
    private fun setupClickListeners() {
        binding.btnRegisterSubmit.setOnClickListener {
            val name = binding.etRegisterName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            viewModel.onRegisterClicked(name, email, password)
        }
    }

    /**
     * Observes ViewModel LiveData for UI updates.
     * Handles registration state changes and navigation.
     */
    private fun observeViewModel() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    binding.pbRegisterLoading.isVisible = true
                    binding.btnRegisterSubmit.isEnabled = false
                }
                is RegisterState.Success -> {
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    viewModel.onStateHandled()

                    findNavController().navigate(R.id.action_global_feedFragment)
                    Log.d("LoginFragment", "Navigate to FeedFragment.")
                }
                is RegisterState.Error -> {
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is RegisterState.Idle -> {
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}