package com.shahar.stoxie.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.ListItemStockBinding
import com.shahar.stoxie.models.Stock
import java.util.Locale

class PortfolioAdapter : ListAdapter<Stock, PortfolioAdapter.StockViewHolder>(StockDiffCallback()) {

    inner class StockViewHolder(private val binding: ListItemStockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: Stock) {
            binding.tvStockSymbol.text = stock.symbol
            binding.tvCompanyName.text = stock.companyName // We'll add this field later

            stock.quote?.let { quote ->
                val context = binding.root.context
                binding.tvStockPrice.text = String.format(Locale.US, "$%.2f", quote.currentPrice)

                val change = quote.change
                val percentChange = quote.percentChange
                val changeText = String.format(Locale.US, "%.2f (%.2f%%)", change, percentChange)

                if (change >= 0) {
                    binding.tvStockChange.text = "+$changeText"
                    binding.tvStockChange.setTextColor(ContextCompat.getColor(context, R.color.green_profit))
                } else {
                    binding.tvStockChange.text = changeText
                    binding.tvStockChange.setTextColor(ContextCompat.getColor(context, R.color.red_like))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ListItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class StockDiffCallback : DiffUtil.ItemCallback<Stock>() {
    override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
        return oldItem == newItem
    }
}