package com.shahar.stoxie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.shahar.stoxie.databinding.FragmentCommentsBinding
import com.shahar.stoxie.ui.adapters.CommentAdapter

/**
 * Fragment for displaying and managing comments for a specific post.
 * Provides real-time comment viewing and addition functionality.
 */
class CommentsFragment : Fragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    /**
     * Navigation arguments containing the post ID for comment retrieval.
     */
    private val args: CommentsFragmentArgs by navArgs()

    /**
     * ViewModel with post-specific comment management.
     * Uses factory to inject postId parameter.
     */
    private val viewModel: CommentsViewModel by viewModels {
        CommentsViewModelFactory(args.postId)
    }

    /**
     * Adapter for displaying comments in RecyclerView.
     */
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Initializes RecyclerView with adapter and layout manager.
     * Sets up comment display with linear layout.
     */
    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Sets up click listeners for user interactions.
     * Handles comment submission from input field.
     */
    private fun setupClickListeners() {
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etCommentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                viewModel.onSendCommentClicked(commentText)
            }
        }
    }

    /**
     * Observes ViewModel LiveData for UI updates.
     * Handles comment list updates and state management.
     */
    private fun observeViewModel() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            binding.pbCommentsLoading.isVisible = comments.isNullOrEmpty()
            commentAdapter.submitList(comments)
            // Scroll to show newest comment
            if (comments.isNotEmpty()) {
                binding.rvComments.scrollToPosition(comments.size - 1)
            }
        }

        viewModel.addCommentState.observe(viewLifecycleOwner) { state ->
            binding.btnSendComment.isEnabled = state !is AddCommentState.Loading
            when (state) {
                is AddCommentState.Success -> {
                    binding.etCommentInput.text?.clear()
                    viewModel.onStateHandled()
                }
                is AddCommentState.Error -> {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    viewModel.onStateHandled()
                }
                else -> { /* No action needed for Idle or Loading states. */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}