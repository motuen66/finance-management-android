package com.example.financemanagement.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financemanagement.databinding.FragmentReportsBinding
import com.example.financemanagement.viewmodel.ReportsViewModel
import com.example.financemanagement.viewmodel.SpendingCategory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by viewModels()
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    private lateinit var categoryAdapter: SpendingCategoryAdapter
    
    // Category colors
    private val categoryColors = listOf(
        Color.parseColor("#FFA26B"), // Orange
        Color.parseColor("#6BCF7F"), // Green
        Color.parseColor("#9D7BEA"), // Purple
        Color.parseColor("#FF6B9D"), // Pink
        Color.parseColor("#5B8DEE"), // Blue
        Color.parseColor("#4ECDC4"), // Teal
        Color.parseColor("#FFD93D"), // Yellow
        Color.parseColor("#FF6B6B")  // Red
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupDonutChart()
        setupComparisonChart()
        setupMonthSelector()
        observeData()
        
        // Fetch data
        viewModel.fetchTransactions()
    }

    private fun setupRecyclerView() {
        categoryAdapter = SpendingCategoryAdapter { category ->
            // Highlight corresponding chart segment
            highlightChartSegment(category)
        }
        
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = categoryAdapter
        }
    }

    private fun setupDonutChart() {
        binding.donutChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            
            // Center text
            setDrawCenterText(true)
            setCenterTextSize(18f)
            setCenterTextColor(Color.parseColor("#2B2240"))
            
            // Donut style
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 70f
            transparentCircleRadius = 75f
            
            // Legend
            legend.isEnabled = false
            
            // Rotation
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            // Animation
            animateY(1400, Easing.EaseInOutQuad)
        }
    }

    private fun setupTabs() {
        // Removed - using month selector instead
    }

    private fun updateTabSelection(tabIndex: Int) {
        // Removed - using month selector instead
    }

    private fun setupMonthSelector() {
        // Update month display
        updateMonthDisplay()
        
        binding.btnPrevMonth.setOnClickListener {
            viewModel.previousMonth()
        }
        
        binding.btnNextMonth.setOnClickListener {
            viewModel.nextMonth()
        }
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, viewModel.currentYear)
        calendar.set(Calendar.MONTH, viewModel.currentMonth - 1) // Calendar months are 0-based
        
        binding.tvCurrentMonth.text = dateFormatter.format(calendar.time)
    }

    private fun setupComparisonChart() {
        binding.chartIncomeExpense.apply {
            description.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            
            // Center text
            setDrawCenterText(false)
            
            // Donut style
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 60f
            transparentCircleRadius = 65f
            
            // Legend
            legend.isEnabled = false
            
            // Rotation
            rotationAngle = 0f
            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            
            // Animation
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Update month display when state changes
                updateMonthDisplay()
                
                // Update stats cards with real data from APIs
                updateStatsCards(state.totalIncome, state.totalExpense, state.totalSavings, state.totalBudget)
                
                if (state.error != null) {
                    showEmptyState()
                    return@collectLatest
                }
                
                if (state.categories.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    updateDonutChart(state.totalAmount, state.period, state.categories)
                    categoryAdapter.submitList(state.categories)
                }
                
                // Update comparison chart
                updateComparisonChart(state.totalIncome, state.totalExpense)
            }
        }
    }

    private fun updateStatsCards(totalIncome: Long, totalExpense: Long, totalSavings: Long, totalBudget: Long) {
        binding.tvStatExpense.text = currencyFormatter.format(totalExpense)
        binding.tvStatIncome.text = currencyFormatter.format(totalIncome)
        
        // Display total savings from saving goals API
        binding.tvStatSavings.text = currencyFormatter.format(totalSavings)
        
        // Display total budget from budgets API
        binding.tvStatBudget.text = currencyFormatter.format(totalBudget)
    }

    private fun updateComparisonChart(totalIncome: Long, totalExpense: Long) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        if (totalExpense > 0) {
            entries.add(PieEntry(totalExpense.toFloat(), "Expense"))
            colors.add(Color.parseColor("#FF6B6B")) // Red
        }
        
        if (totalIncome > 0) {
            entries.add(PieEntry(totalIncome.toFloat(), "Income"))
            colors.add(Color.parseColor("#6BCF7F")) // Green
        }
        
        if (entries.isEmpty()) {
            binding.chartIncomeExpense.clear()
            binding.tvTotalExpense.text = currencyFormatter.format(0)
            binding.tvTotalIncome.text = currencyFormatter.format(0)
            binding.tvSavings.text = currencyFormatter.format(0)
            return
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 0f // Hide values on chart
            sliceSpace = 2f
            selectionShift = 0f
        }
        
        val data = PieData(dataSet).apply {
            setDrawValues(false)
        }
        
        binding.chartIncomeExpense.data = data
        binding.chartIncomeExpense.invalidate()
        
        // Update text values
        binding.tvTotalExpense.text = currencyFormatter.format(totalExpense)
        binding.tvTotalIncome.text = currencyFormatter.format(totalIncome)
        
        val savings = totalIncome - totalExpense
        binding.tvSavings.text = currencyFormatter.format(savings)
    }

    private fun updateDonutChart(
        totalAmount: Long,
        period: String,
        categories: List<SpendingCategory>
    ) {
        val entries = categories.map { category ->
            PieEntry(category.percent, category.name)
        }
        
        if (entries.isEmpty()) {
            binding.donutChart.clear()
            binding.donutChart.centerText = "No Data"
            return
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = categories.map { it.color }
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 8f
        }
        
        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.donutChart))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        
        binding.donutChart.data = data
        
        // Center text
        val formattedAmount = currencyFormatter.format(totalAmount)
        val periodText = when (period) {
            "Week" -> "this Week"
            "Month" -> "this ${getCurrentMonth()}"
            "Year" -> "this Year"
            else -> "this Month"
        }
        binding.donutChart.centerText = "Spent $periodText\n$formattedAmount"
        
        binding.donutChart.invalidate()
    }

    private fun highlightChartSegment(category: SpendingCategory) {
        binding.donutChart.highlightValue(null)
        val categories = categoryAdapter.currentList
        val index = categories.indexOf(category)
        if (index >= 0) {
            binding.donutChart.highlightValue(index.toFloat(), 0)
        }
    }

    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.rvCategories.visibility = View.GONE
        binding.donutChart.clear()
    }

    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.rvCategories.visibility = View.VISIBLE
    }

    private fun getCurrentMonth(): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
