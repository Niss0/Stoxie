package com.shahar.stoxie.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shahar.stoxie.databinding.FragmentFeedBinding
import com.shahar.stoxie.ui.adapters.PostAdapter

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        // UPDATED: Observe the new 'postUiModels' LiveData.
        // The rest of the code works perfectly with this change, as the PostAdapter
        // now expects a list of PostUiModel objects.
        viewModel.postUiModels.observe(viewLifecycleOwner) { postUiModels ->
            binding.pbFeedLoading.isVisible = postUiModels.isNullOrEmpty()
            postAdapter.submitList(postUiModels)
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClicked = viewModel::onLikeClicked,
            onToggleCommentsClicked = viewModel::onToggleCommentsClicked,
            onAddCommentClicked = viewModel::onAddCommentClicked,
        )
        binding.rvFeedPosts.adapter = postAdapter

        binding.rvFeedPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
            // This can help performance with nested RecyclerViews.
            setItemViewCacheSize(20)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
