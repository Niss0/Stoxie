package com.shahar.stoxie.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shahar.stoxie.databinding.ListItemSearchResultBinding
import com.shahar.stoxie.models.SymbolLookupResult

/**
 * RecyclerView adapter for stock search results.
 * Displays search results with click handling for symbol selection.
 */
class SearchAdapter(
    private val onSymbolClicked: (SymbolLookupResult) -> Unit
) : ListAdapter<SymbolLookupResult, SearchAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    /**
     * ViewHolder for individual search result items.
     */
    inner class SearchResultViewHolder(private val binding: ListItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds search result data to views with click handling.
         */
        fun bind(symbol: SymbolLookupResult) {
            binding.tvSymbol.text = symbol.symbol
            binding.tvDescription.text = symbol.description
            binding.root.setOnClickListener { onSymbolClicked(symbol) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ListItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil callback for efficient search result updates.
 */
class SearchResultDiffCallback : DiffUtil.ItemCallback<SymbolLookupResult>() {
    override fun areItemsTheSame(oldItem: SymbolLookupResult, newItem: SymbolLookupResult): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: SymbolLookupResult, newItem: SymbolLookupResult): Boolean {
        return oldItem == newItem
    }
}