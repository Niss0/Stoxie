package com.shahar.stoxie.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shahar.stoxie.databinding.ListItemCommentBinding
import com.shahar.stoxie.models.Comment
import com.shahar.stoxie.util.TimeAgoFormatter

/**
 * The Adapter for the comments RecyclerView.
 * It uses ListAdapter for efficient list updates.
 */
class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    /**
     * The ViewHolder holds references to the views for a single comment item.
     */
    inner class CommentViewHolder(private val binding: ListItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.tvCommentAuthorName.text = comment.authorName
            binding.tvCommentText.text = comment.text
            // Use our time utility to format the timestamp
            binding.tvCommentTimestamp.text = TimeAgoFormatter.getTimeAgo(comment.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ListItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil callback to help ListAdapter determine what has changed in the list.
 */
class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}
