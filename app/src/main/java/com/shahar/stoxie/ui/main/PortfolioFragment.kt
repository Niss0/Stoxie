package com.shahar.stoxie.ui.main

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentPortfolioBinding
import com.shahar.stoxie.models.Stock
import com.shahar.stoxie.ui.adapters.ChartPagerAdapter
import com.shahar.stoxie.ui.adapters.PortfolioAdapter
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioViewModel by activityViewModels()
    private lateinit var portfolioAdapter: PortfolioAdapter
    private lateinit var chartPagerAdapter: ChartPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewPager()

        binding.swipeRefreshLayout.isEnabled = false

        binding.fabAddStock.setOnClickListener {
            showAddStockDialog()
        }

        // This observer now handles all list updates from the single source of truth.
        viewModel.portfolioState?.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = false
            binding.pbPortfolioLoading.isVisible = false

            when (state) {
                is PortfolioState.Success -> {
                    binding.tvPortfolioError.isVisible = false
                    val hasStocks = state.stocks.isNotEmpty()
                    portfolioAdapter.submitList(state.stocks)
                    binding.cardPerformance.isVisible = hasStocks
                    binding.swipeRefreshLayout.isVisible = hasStocks
                    bindPerformanceSummary(state)
                }
                is PortfolioState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.pbPortfolioLoading.isVisible = false
                    binding.swipeRefreshLayout.isVisible = false
                    binding.tvPortfolioError.isVisible = true
                    binding.tvPortfolioError.text = state.message
                }
            }
        }
    }

    private fun setupRecyclerView() {
        portfolioAdapter = PortfolioAdapter()
        binding.rvPortfolioStocks.adapter = portfolioAdapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // --- onSwiped now only reports the action to the ViewModel ---
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val stockToDelete = portfolioAdapter.currentList[position]
                    // Tell the ViewModel a swipe has happened.
                    viewModel.onStockSwiped(stockToDelete)

                    // Show the Snackbar. The ViewModel state change will handle the visual removal.
                    Snackbar.make(binding.root, "Removed ${stockToDelete.symbol}", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            // Tell the ViewModel to cancel the deletion.
                            viewModel.cancelDeletion()
                        }
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    // If not undone, tell the ViewModel to confirm the deletion.
                                    viewModel.confirmDeletion()
                                }
                            }
                        })
                        .show()
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvPortfolioStocks)
    }

    private fun showAddStockDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_stock, null, false)
        val symbolEditText = dialogView.findViewById<EditText>(R.id.et_stock_symbol)
        val sharesEditText = dialogView.findViewById<EditText>(R.id.et_stock_shares)
        val priceEditText = dialogView.findViewById<EditText>(R.id.et_buy_price)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Stock to Portfolio")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { dialog, _ ->
                val symbol = symbolEditText.text.toString().trim().uppercase()
                val shares = sharesEditText.text.toString().toDoubleOrNull() ?: 0.0
                val price = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
                if (symbol.isNotEmpty() && shares > 0 && price > 0) {
                    binding.swipeRefreshLayout.isRefreshing = true
                    viewModel.addStock(symbol, shares, price)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun setupViewPager() {
        chartPagerAdapter = ChartPagerAdapter(requireActivity())
        binding.chartViewPager.adapter = chartPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.chartViewPager) { _, _ -> }.attach()
    }

    private fun bindPerformanceSummary(state: PortfolioState.Success) {
        binding.tvTotalValue.text = String.format(Locale.US, "$%,.2f", state.totalValue)
        val gainLoss = state.totalGainLoss
        val gainLossPercent = state.totalGainLossPercent
        val gainLossText = String.format(Locale.US, "%.2f (%.2f%%)", Math.abs(gainLoss), Math.abs(gainLossPercent))

        if (gainLoss >= 0) {
            binding.tvTotalGainLoss.text = "+ $gainLossText"
            binding.tvTotalGainLoss.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_profit))
        } else {
            binding.tvTotalGainLoss.text = "- $gainLossText"
            binding.tvTotalGainLoss.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_like))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}