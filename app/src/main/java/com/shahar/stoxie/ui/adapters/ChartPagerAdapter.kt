package com.shahar.stoxie.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.shahar.stoxie.ui.main.PortfolioAllocationChartFragment
import com.shahar.stoxie.ui.main.PortfolioSectorChartFragment

class ChartPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PortfolioAllocationChartFragment()
            1 -> PortfolioSectorChartFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}