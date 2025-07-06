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

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

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

        binding.btnRegisterSubmit.setOnClickListener {
            val name = binding.etRegisterName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            viewModel.onRegisterClicked(name, email, password)
        }

        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    // Show spinner and disable the register button
                    binding.pbRegisterLoading.isVisible = true
                    binding.btnRegisterSubmit.isEnabled = false
                }
                is RegisterState.Success -> {
                    // Hide spinner, enable button, show toast, and reset the state.
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    viewModel.onStateHandled()

                    findNavController().navigate(R.id.action_global_feedFragment)
                    Log.d("LoginFragment", "Navigate to FeedFragment.")
                }
                is RegisterState.Error -> {
                    // Hide spinner, enable button, show error, and reset the state.
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is RegisterState.Idle -> {
                    // The default state: spinner is hidden, button is enabled.
                    binding.pbRegisterLoading.isVisible = false
                    binding.btnRegisterSubmit.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
