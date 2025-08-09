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
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.ListItemPostBinding
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.models.Post
import com.shahar.stoxie.util.TimeAgoFormatter
import com.shahar.stoxie.data.UserRepository
import com.shahar.stoxie.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * UI model combining post with comments and visibility state.
 */
data class PostUiModel(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val areCommentsVisible: Boolean = false
)

/**
 * RecyclerView adapter for social feed posts.
 * Handles likes, comments, and dynamic content loading with efficient updates.
 */
class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onToggleCommentsClicked: (Post) -> Unit,
    private val onAddCommentClicked: (postId: String, commentText: String) -> Unit
) : ListAdapter<PostUiModel, PostAdapter.PostViewHolder>(PostUiModelDiffCallback()) {

    // User info cache: authorId -> User
    private val userCache = mutableMapOf<String, User?>()
    private val userRepository = UserRepository()
    private val adapterScope = CoroutineScope(Dispatchers.Main)

    /**
     * ViewHolder for individual post items.
     */
    inner class PostViewHolder(private val binding: ListItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        private val commentAdapter = CommentAdapter()

        init {
            binding.rvPostComments.adapter = commentAdapter
            binding.rvPostComments.layoutManager = LinearLayoutManager(context)
        }

        /**
         * Binds PostUiModel to ViewHolder, updating all UI elements.
         */
        fun bind(postUiModel: PostUiModel) {
            val post = postUiModel.post
            val context = binding.root.context

            // Bind basic post information
            // Fetch latest user info for this authorId
            val authorId = post.authorId
            val cachedUser = userCache[authorId]
            if (cachedUser != null) {
                binding.tvPostAuthorName.text = cachedUser.name
                binding.ivPostAuthorAvatar.load(cachedUser.profilePictureUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_profile)
                    error(R.drawable.ic_profile)
                }
            } else {
                // Show placeholder while loading
                binding.tvPostAuthorName.text = "Loading..."
                binding.ivPostAuthorAvatar.setImageResource(R.drawable.ic_profile)
                // Fetch user info asynchronously
                adapterScope.launch {
                    val user = userRepository.getUser(authorId)
                    userCache[authorId] = user
                    // Only update if this ViewHolder is still bound to this post
                    if (adapterPosition != RecyclerView.NO_POSITION && getItem(adapterPosition).post.authorId == authorId) {
                        binding.tvPostAuthorName.text = user?.name ?: "Unknown"
                        binding.ivPostAuthorAvatar.load(user?.profilePictureUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_profile)
                            error(R.drawable.ic_profile)
                        }
                    }
                }
            }

            binding.tvPostTimestamp.text = TimeAgoFormatter.getTimeAgo(post.timestamp)
            binding.tvPostContent.text = post.text

            updateSocialProofElements(post, context)
            updateLikeButtonState(post, context)
            setupClickListeners(post)
            updateCommentsSection(postUiModel)
        }

        /**
         * Updates social proof elements with animations.
         */
        private fun updateSocialProofElements(post: Post, context: android.content.Context) {
            val likeCount = post.likedBy.size
            if (likeCount > 0) {
                binding.tvLikeCount.text = context.resources.getQuantityString(R.plurals.post_like_count, likeCount, likeCount)
                binding.tvLikeCount.animate().alpha(1.0f).setDuration(300).start()
            } else {
                binding.tvLikeCount.animate().alpha(0.0f).setDuration(300).start()
            }

            val commentCount = post.commentCount.toInt()
            if (commentCount > 0) {
                binding.tvCommentCount.text = context.resources.getQuantityString(R.plurals.post_comment_count, commentCount, commentCount)
                binding.tvCommentCount.animate().alpha(1.0f).setDuration(300).start()
            } else {
                binding.tvCommentCount.animate().alpha(0.0f).setDuration(300).start()
            }
        }

        /**
         * Updates like button appearance based on current user's like status.
         */
        private fun updateLikeButtonState(post: Post, context: android.content.Context) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = post.likedBy.contains(currentUserId)
            
            binding.btnPostLike.setIconResource(if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_empty)
            binding.btnPostLike.text = if (isLiked) "Liked" else "Like"
            
            val likeIconColor = if (isLiked) 
                ContextCompat.getColor(context, R.color.red_like) 
            else 
                ContextCompat.getColor(context, R.color.neutral_text_secondary)
            
            binding.btnPostLike.iconTint = ColorStateList.valueOf(likeIconColor)
            binding.btnPostLike.setTextColor(likeIconColor)
        }

        /**
         * Sets up click listeners for user interactions.
         */
        private fun setupClickListeners(post: Post) {
            binding.btnPostLike.setOnClickListener { onLikeClicked(post) }
            binding.btnPostComment.setOnClickListener { onToggleCommentsClicked(post) }
            binding.tvCommentCount.setOnClickListener { onToggleCommentsClicked(post) }
            
            binding.btnAddComment.setOnClickListener {
                val commentText = binding.etAddComment.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    onAddCommentClicked(post.id, commentText)
                    binding.etAddComment.text?.clear()
                }
            }
        }

        /**
         * Updates comments section visibility and content.
         */
        private fun updateCommentsSection(postUiModel: PostUiModel) {
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

/**
 * DiffUtil callback for efficient list updates.
 */
class PostUiModelDiffCallback : DiffUtil.ItemCallback<PostUiModel>() {
    override fun areItemsTheSame(oldItem: PostUiModel, newItem: PostUiModel): Boolean {
        return oldItem.post.id == newItem.post.id
    }

    override fun areContentsTheSame(oldItem: PostUiModel, newItem: PostUiModel): Boolean {
        return oldItem == newItem
    }
}