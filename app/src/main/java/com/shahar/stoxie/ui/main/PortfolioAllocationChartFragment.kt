package com.shahar.stoxie.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentChartContainerBinding
import androidx.core.graphics.toColorInt

class PortfolioAllocationChartFragment : Fragment() {

    private var _binding: FragmentChartContainerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChartContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeViewModel()
    }

    // --- NEW: A modern color palette for individual stock allocations ---
    private fun getAllocationChartColors(): List<Int> {
        return listOf(
            "#0A84FF".toColorInt(), // Bright Blue
            "#64D2FF".toColorInt(), // Sky Blue
            "#30D158".toColorInt(), // Mint Green
            "#FF9F0A".toColorInt(), // Orange
            "#FF453A".toColorInt(), // Red
            "#BF5AF2".toColorInt(), // Purple
            "#AC8E68".toColorInt(), // Brown
            "#8E8E93".toColorInt()  // Medium Gray
        )
    }

    private fun setupChart() {
        binding.pieChart.apply {
            setTouchEnabled(false)
            isRotationEnabled = false
            setHighlightPerTapEnabled(false)
            animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setDrawCenterText(true)
            centerText = "Asset\nAllocation"
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_text_secondary))
            setCenterTextSize(18f)
            setDrawEntryLabels(false)

            val l: Legend = legend
            l.isEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
            l.isWordWrapEnabled = true
            l.textColor = ContextCompat.getColor(requireContext(), R.color.neutral_text_secondary)
            l.textSize = 12f
            l.xEntrySpace = 10f
            l.yEntrySpace = 5f
        }
    }

    private fun observeViewModel() {
        viewModel.portfolioState?.observe(viewLifecycleOwner) { state ->
            if (state is PortfolioState.Success && state.allocationChartEntries.isNotEmpty()) {
                val dataSet = PieDataSet(state.allocationChartEntries, "")

                // --- UPDATE: Use our new custom colors ---
                dataSet.colors = getAllocationChartColors()
                dataSet.sliceSpace = 2f

                dataSet.valueFormatter = PercentFormatter(binding.pieChart)

                val data = PieData(dataSet)
                data.setDrawValues(false)

                binding.pieChart.data = data
                binding.pieChart.invalidate()
            } else {
                binding.pieChart.centerText = "Add stocks to\nsee the chart"
                binding.pieChart.data?.clearValues()
                binding.pieChart.invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}