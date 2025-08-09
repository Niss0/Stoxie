package com.shahar.stoxie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentProfileBinding
import com.shahar.stoxie.ui.adapters.PostAdapter

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Handle Logout
        binding.btnProfileLogout.setOnClickListener {
            // Clear all ViewModel data before logout
            viewModel.logout()
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        // Handle Edit Profile Navigation
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Observe user data for the profile header
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.pbProfileLoading.isVisible = user == null
            user?.let {
                binding.tvProfileName.text = it.name
                binding.tvProfileBio.text = it.bio?.takeIf { bio -> bio.isNotBlank() } ?: "No bio yet."

                // --- THIS IS THE CODE THAT SHOWS THE IMAGE ---
                // Check if the URL from Firestore is not null or empty
                if (!it.profilePictureUrl.isNullOrEmpty()) {
                    // Use Coil's .load() extension function on the ImageView
                    binding.ivProfileAvatar.load(it.profilePictureUrl) {
                        crossfade(true) // Adds a nice fade-in animation
                        placeholder(R.drawable.ic_profile) // A default image while loading
                        error(R.drawable.ic_profile) // An image to show if the URL is invalid or loading fails
                    }
                } else {
                    // If the user has no profile picture, show the default icon
                    binding.ivProfileAvatar.setImageResource(R.drawable.ic_profile)
                }
            }
        }

        // Observe user's posts for the RecyclerView
        viewModel.postUiModels.observe(viewLifecycleOwner) { uiModels ->
            if (viewModel.user.value != null) {
                binding.pbProfileLoading.isVisible = false
            }
            postAdapter.submitList(uiModels)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning from edit profile
        viewModel.refreshUser()
    }

    private fun setupRecyclerView() {
        // Connect the adapter's listeners to the ViewModel's functions
        postAdapter = PostAdapter(
            onLikeClicked = viewModel::onLikeClicked,
            onToggleCommentsClicked = viewModel::onToggleCommentsClicked,
            onAddCommentClicked = viewModel::onAddCommentClicked
        )
        binding.rvProfilePosts.adapter = postAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}