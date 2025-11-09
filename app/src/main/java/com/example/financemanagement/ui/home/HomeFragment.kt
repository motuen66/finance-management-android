package com.example.financemanagement.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentHomeBinding
import com.example.financemanagement.viewmodel.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCharts()
        observeData()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener {
            // TODO: Navigate to previous month
        }
        
        binding.btnNextMonth.setOnClickListener {
            // TODO: Navigate to next month
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.summary.collectLatest { summary ->
                updateSummaryCards(summary.income, summary.expense, summary.balance)
                updatePieChart(summary.income, summary.expense)
                updateBarChart(summary.transactions)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateSummaryCards(income: Double, expense: Double, balance: Double) {
        binding.tvIncome.text = formatCurrency(income)
        binding.tvExpense.text = formatCurrency(expense)
        binding.tvBalance.text = formatCurrency(balance)
    }

    private fun setupCharts() {
        setupPieChart()
        setupBarChart()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            centerText = "Income vs\nExpense"
            setCenterTextSize(16f)
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
            }
            
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 40f
            transparentCircleRadius = 45f
            
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            
            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 10f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                textSize = 10f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 12f
            }
            
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun updatePieChart(income: Double, expense: Double) {
        val entries = mutableListOf<PieEntry>()
        
        if (income > 0) {
            entries.add(PieEntry(income.toFloat(), "Income"))
        }
        if (expense > 0) {
            entries.add(PieEntry(expense.toFloat(), "Expense"))
        }
        
        if (entries.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "No Data"
            return
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#00C875"), // Green for income
                Color.parseColor("#FF5A78")  // Red for expense
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 5f
        }
        
        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        
        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun updateBarChart(transactions: List<com.example.financemanagement.domain.model.Transaction>) {
        // Group transactions by day and calculate totals
        val dailyData = transactions
            .groupBy { it.date?.substring(0, 10) ?: "Unknown" } // Extract date only
            .mapValues { (_, txns) ->
                val income = txns.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
                val expense = txns.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }
                Pair(income, expense)
            }
            .toList()
            .take(7) // Show last 7 days
        
        if (dailyData.isEmpty()) {
            binding.barChart.clear()
            return
        }
        
        val incomeEntries = dailyData.mapIndexed { index, (_, data) ->
            BarEntry(index.toFloat(), data.first.toFloat())
        }
        
        val expenseEntries = dailyData.mapIndexed { index, (_, data) ->
            BarEntry(index.toFloat(), data.second.toFloat())
        }
        
        val incomeDataSet = BarDataSet(incomeEntries, "Income").apply {
            color = Color.parseColor("#00C875")
            valueTextSize = 10f
        }
        
        val expenseDataSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.parseColor("#FF5A78")
            valueTextSize = 10f
        }
        
        val barData = BarData(incomeDataSet, expenseDataSet).apply {
            barWidth = 0.35f
        }
        
        binding.barChart.data = barData
        binding.barChart.groupBars(0f, 0.3f, 0f)
        binding.barChart.invalidate()
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
