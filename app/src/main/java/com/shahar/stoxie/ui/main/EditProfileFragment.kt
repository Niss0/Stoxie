package com.shahar.stoxie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    // Activity Result Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Load the selected image into the ImageView for preview
            binding.ivEditProfileAvatar.load(it)
            // Pass the URI to the ViewModel to be uploaded when "Save" is clicked
            viewModel.onNewProfileImageSelected(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle image selection
        binding.ivEditProfileAvatar.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.tvChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Handle Save Changes
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString().trim()
            val bio = binding.etEditBio.text.toString().trim()
            viewModel.onSaveChangesClicked(name, bio)
        }

        // Handle Change Password
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Observe user data to populate the fields
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etEditName.setText(it.name)
                binding.etEditBio.setText(it.bio)

                // --- THIS IS THE CODE THAT SHOWS THE CURRENT IMAGE ---
                if (!it.profilePictureUrl.isNullOrEmpty()) {
                    binding.ivEditProfileAvatar.load(it.profilePictureUrl) {
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                    }
                } else {
                    binding.ivEditProfileAvatar.setImageResource(R.drawable.ic_profile)
                }
            }
        }

        // Observe the save state
        viewModel.editState.observe(viewLifecycleOwner) { state ->
            binding.pbEditProfileLoading.isVisible = state is EditProfileState.Loading
            binding.btnSaveProfile.isEnabled = state !is EditProfileState.Loading

            when (state) {
                is EditProfileState.Success -> {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    // Don't navigate back immediately - let user see the updated photo
                    viewModel.onStateHandled()
                }
                is EditProfileState.Error -> {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is EditProfileState.PasswordResetSent -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                else -> { /* Idle */ }
            }
        }

        // Observe profile saved state to change button behavior
        viewModel.isProfileSaved.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                binding.btnSaveProfile.text = "Done"
                binding.btnSaveProfile.setOnClickListener {
                    findNavController().popBackStack()
                }
            } else {
                binding.btnSaveProfile.text = getString(R.string.edit_profile_save_changes_btn_text)
                binding.btnSaveProfile.setOnClickListener {
                    val name = binding.etEditName.text.toString().trim()
                    val bio = binding.etEditBio.text.toString().trim()
                    viewModel.onSaveChangesClicked(name, bio)
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setMessage("A password reset link will be sent to your registered email address. Proceed?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Send Email") { _, _ ->
                viewModel.onChangePasswordClicked()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}