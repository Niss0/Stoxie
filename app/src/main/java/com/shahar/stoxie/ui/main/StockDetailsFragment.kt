package com.shahar.stoxie.ui.main

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.shahar.stoxie.R
import com.shahar.stoxie.databinding.FragmentStockDetailsBinding
import com.shahar.stoxie.util.DateAxisValueFormatter
import java.util.*

/**
 * Formatter for 1Y period showing months.
 */
class MonthAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong())
        val calendar = Calendar.getInstance().apply { time = date }
        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
    }
}

/**
 * Formatter for 5Y period showing months and years.
 */
class MonthYearAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong())
        val calendar = Calendar.getInstance().apply { time = date }
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
        val year = calendar.get(Calendar.YEAR)
        return "$month $year"
    }
}

class StockDetailsFragment : Fragment() {

    private var _binding: FragmentStockDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: StockDetailsFragmentArgs by navArgs()
    private val viewModel: StockDetailsViewModel by viewModels()
    private var currentPeriod: String = "1D"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDescription.movementMethod = LinkMovementMethod.getInstance()
        setupChipGroupListener()
        observeViewModel()

        if (savedInstanceState == null) {
            viewModel.loadStockDetails(args.symbol, "1D")
        }
    }

    private fun setupChipGroupListener() {
        binding.chipGroupPeriod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == View.NO_ID) return@setOnCheckedChangeListener

            currentPeriod = when (checkedId) {
                R.id.chip_1d -> "1D"
                R.id.chip_5d -> "5D"
                R.id.chip_1m -> "1M"
                R.id.chip_1y -> "1Y"
                R.id.chip_5y -> "5Y"
                else -> "1D"
            }
            viewModel.loadStockDetails(args.symbol, currentPeriod)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.pbDetailsLoading.isVisible = state is StockDetailsState.Loading
            binding.tvDetailsError.isVisible = state is StockDetailsState.Error

            when (state) {
                is StockDetailsState.Success -> {
                    bindHeader(state.data)
                    setupChart(state.data.chartEntries)
                }
                is StockDetailsState.Error -> {
                    binding.tvDetailsError.text = state.message
                    setupChart(null)
                }
                else -> { /* Loading or Idle */ }
            }
        }
    }

    private fun bindHeader(data: StockDetailsData) {
        val profile = data.profile
        binding.tvStockDetailsName.text = profile.name
        binding.tvStockDetailsSymbol.text = profile.ticker

        binding.ivLogo.load(profile.logo) {
            crossfade(true)
            placeholder(R.drawable.ic_portfolio)
            error(R.drawable.ic_portfolio)
        }

        data.currentPrice?.let {
            binding.tvCurrentPrice.text = String.format(Locale.US, "$%.2f", it)
        }
        
        val change = data.priceChange
        val percent = data.priceChangePercent
        if (change != null && percent != null) {
            val sign = if (change >= 0) "+" else ""
            binding.tvPriceChange.text = String.format(Locale.US, "%s%.2f (%.2f%%)", sign, change, percent)
            val color = if (change >= 0) R.color.green_profit else R.color.red_like
            binding.tvPriceChange.setTextColor(ContextCompat.getColor(requireContext(), color))
        }

        val descriptionHtml = """
            <b>Industry:</b> ${profile.sector}<br>
            <b>Market Cap:</b> $${"%,.0f".format(profile.marketCap)}M<br>
            <b>Website:</b> <a href='${profile.weburl}'>${profile.weburl}</a>
        """.trimIndent()
        binding.tvDescription.text = HtmlCompat.fromHtml(descriptionHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private fun setupChart(entries: List<com.github.mikephil.charting.data.Entry>?) {
        try {
            if (entries.isNullOrEmpty()) {
                binding.lineChart.data = null
                binding.lineChart.setNoDataText("Historical data not available.")
                binding.lineChart.invalidate()
                return
            }

            val validEntries = entries.filter { entry ->
                !entry.x.isNaN() && !entry.y.isNaN() && 
                entry.x.isFinite() && entry.y.isFinite() &&
                entry.y > 0 && entry.x > 0
            }

            if (validEntries.isEmpty()) {
                binding.lineChart.data = null
                binding.lineChart.setNoDataText("No valid data points available.")
                binding.lineChart.invalidate()
                return
            }

            val sortedEntries = validEntries.sortedBy { it.x }

            val dataSet = LineDataSet(sortedEntries, "Stock Price").apply {
                color = ContextCompat.getColor(requireContext(), R.color.stoxie_blue_500)
                setDrawValues(false)
                setDrawCircles(false)
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(requireContext(), R.color.stoxie_blue_500)
                fillAlpha = 30
                mode = com.github.mikephil.charting.data.LineDataSet.Mode.LINEAR
            }

            binding.lineChart.apply {
                clear()
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = Color.GRAY
                    setDrawGridLines(false)
                    valueFormatter = when (currentPeriod) {
                        "1Y" -> MonthAxisValueFormatter()
                        "5Y" -> MonthYearAxisValueFormatter()
                        else -> DateAxisValueFormatter(currentPeriod)
                    }
                }

                axisLeft.textColor = Color.GRAY
                axisRight.isEnabled = false
                setTouchEnabled(false)
                setPinchZoom(false)
                setScaleEnabled(false)
                invalidate()
            }
        } catch (e: Exception) {
            android.util.Log.e("StockDetailsFragment", "Error setting up chart", e)
            try {
                binding.lineChart.data = null
                binding.lineChart.setNoDataText("Error loading chart data.")
                binding.lineChart.invalidate()
            } catch (e2: Exception) {
                android.util.Log.e("StockDetailsFragment", "Error clearing chart", e2)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}