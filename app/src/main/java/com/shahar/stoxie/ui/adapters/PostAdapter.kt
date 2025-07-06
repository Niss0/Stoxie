package com.shahar.stoxie.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.ListItemPostBinding
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.models.Post
import com.shahar.stoxie.util.TimeAgoFormatter

data class PostUiModel(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val areCommentsVisible: Boolean = false
)

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onToggleCommentsClicked: (Post) -> Unit,
    private val onAddCommentClicked: (postId: String, commentText: String) -> Unit
) : ListAdapter<PostUiModel, PostAdapter.PostViewHolder>(PostUiModelDiffCallback()) {

    inner class PostViewHolder(private val binding: ListItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        private val commentAdapter = CommentAdapter()

        init {
            binding.rvPostComments.adapter = commentAdapter
            binding.rvPostComments.layoutManager = LinearLayoutManager(context)
        }

        fun bind(postUiModel: PostUiModel) {
            val post = postUiModel.post
            val context = binding.root.context

            // Bind Header and Content
            binding.tvPostAuthorName.text = post.authorName
            binding.tvPostTimestamp.text = TimeAgoFormatter.getTimeAgo(post.timestamp)
            binding.tvPostContent.text = post.text
            // TODO: Load author avatar with Coil

            // --- UPDATED: Social Proof with Alpha Animation for a natural look ---
            val likeCount = post.likedBy.size
            if (likeCount > 0) {
                binding.tvLikeCount.text = context.resources.getQuantityString(R.plurals.post_like_count, likeCount, likeCount)
                // Animate to fully visible
                binding.tvLikeCount.animate().alpha(1.0f).setDuration(300).start()
            } else {
                // Animate to fully transparent (but still taking up space)
                binding.tvLikeCount.animate().alpha(0.0f).setDuration(300).start()
            }

            val commentCount = post.commentCount.toInt()
            if (commentCount > 0) {
                binding.tvCommentCount.text = context.resources.getQuantityString(R.plurals.post_comment_count, commentCount, commentCount)
                // Animate to fully visible
                binding.tvCommentCount.animate().alpha(1.0f).setDuration(300).start()
            } else {
                // Animate to fully transparent
                binding.tvCommentCount.animate().alpha(0.0f).setDuration(300).start()
            }


            // Handle Like Button State (Icon, Text, and Color)
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = post.likedBy.contains(currentUserId)
            binding.btnPostLike.setIconResource(if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_empty)
            binding.btnPostLike.text = if (isLiked) "Liked" else "Like"
            val likeIconColor = if (isLiked) ContextCompat.getColor(context, R.color.red_like) else ContextCompat.getColor(context, R.color.neutral_text_secondary)
            binding.btnPostLike.iconTint = ColorStateList.valueOf(likeIconColor)
            binding.btnPostLike.setTextColor(likeIconColor)

            // Set Click Listeners
            binding.btnPostLike.setOnClickListener { onLikeClicked(post) }
            binding.btnPostComment.setOnClickListener { onToggleCommentsClicked(post) }
            binding.tvCommentCount.setOnClickListener { onToggleCommentsClicked(post) } // Also opens comments
            binding.btnAddComment.setOnClickListener {
                val commentText = binding.etAddComment.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    onAddCommentClicked(post.id, commentText)
                    binding.etAddComment.text?.clear()
                }
            }

            // Handle Comments Section Visibility
            binding.groupCommentsSection.isVisible = postUiModel.areCommentsVisible
            if (postUiModel.areCommentsVisible) {
                commentAdapter.submitList(postUiModel.comments)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ListItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostUiModelDiffCallback : DiffUtil.ItemCallback<PostUiModel>() {
    override fun areItemsTheSame(oldItem: PostUiModel, newItem: PostUiModel): Boolean {
        return oldItem.post.id == newItem.post.id
    }

    override fun areContentsTheSame(oldItem: PostUiModel, newItem: PostUiModel): Boolean {
        return oldItem == newItem
    }
}