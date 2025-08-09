package com.shahar.stoxie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shahar.stoxie.databinding.FragmentCreatePostBinding

/**
 * Fragment for creating new social posts.
 * Provides post composition and publishing functionality.
 */
class CreatePostFragment : Fragment() {

    /**
     * View binding for safe view access.
     */
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    /**
     * ViewModel for post creation business logic and state management.
     */
    private val viewModel: CreatePostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    /**
     * Sets up click listeners for user interactions.
     * Handles post publishing from input field.
     */
    private fun setupClickListeners() {
        binding.btnCreatePostPublish.setOnClickListener {
            val postText = binding.etCreatePostContent.text.toString().trim()
            viewModel.onPublishClicked(postText)
        }
    }

    /**
     * Observes ViewModel LiveData for UI updates.
     * Handles post creation state changes and navigation.
     */
    private fun observeViewModel() {
        viewModel.createPostState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreatePostState.Loading -> {
                    binding.pbCreatePostLoading.isVisible = true
                    binding.btnCreatePostPublish.isEnabled = false
                }
                is CreatePostState.Success -> {
                    Toast.makeText(context, "Post published!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is CreatePostState.Error -> {
                    binding.pbCreatePostLoading.isVisible = false
                    binding.btnCreatePostPublish.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is CreatePostState.Idle -> {
                    binding.pbCreatePostLoading.isVisible = false
                    binding.btnCreatePostPublish.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
