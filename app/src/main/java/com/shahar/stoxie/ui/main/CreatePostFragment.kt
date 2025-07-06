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

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    // Get a reference to our CreatePostViewModel
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

        // Set the click listener for the publish button
        binding.btnCreatePostPublish.setOnClickListener {
            val postText = binding.etCreatePostContent.text.toString().trim()
            viewModel.onPublishClicked(postText)
        }

        // Observe the state from the ViewModel to update the UI
        viewModel.createPostState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreatePostState.Loading -> {
                    binding.pbCreatePostLoading.isVisible = true
                    binding.btnCreatePostPublish.isEnabled = false
                }
                is CreatePostState.Success -> {
                    // When the post is created successfully, show a toast and navigate back.
                    Toast.makeText(context, "Post published!", Toast.LENGTH_SHORT).show()
                    // findNavController().popBackStack() is the standard way to go back to the previous screen.
                    findNavController().popBackStack()
                }
                is CreatePostState.Error -> {
                    binding.pbCreatePostLoading.isVisible = false
                    binding.btnCreatePostPublish.isEnabled = true
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    // Reset the state so the user can try again
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
        _binding = null
    }
}
