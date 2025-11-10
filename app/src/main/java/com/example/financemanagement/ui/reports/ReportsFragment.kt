package com.example.financemanagement.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentReportsBinding
import com.example.financemanagement.viewmodel.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    // Màu sắc cho categories (vibrant colors như hình mẫu)
    private val categoryColors = listOf(
        Color.parseColor("#5B8DEE"), // Blue
        Color.parseColor("#9D7BEA"), // Purple  
        Color.parseColor("#FF6B9D"), // Pink
        Color.parseColor("#FFA26B"), // Orange
        Color.parseColor("#4ECDC4"), // Teal
        Color.parseColor("#FFD93D"), // Yellow
        Color.parseColor("#6BCF7F"), // Green
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
        
        setupCharts()
        observeData()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Month selector buttons removed for now - will add back later
        // TODO: Add prev/next month buttons
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.summary.collectLatest { summary ->
                updateMainCard(summary.income, summary.expense)
                updateStatsCards(summary.income, summary.expense, summary.balance)
                updateCategoryChart(summary.transactions)
                updateBarChart(summary.transactions)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateMainCard(income: Double, expense: Double) {
        binding.tvIncomeMain.text = formatCurrency(income)
        binding.tvExpenseMain.text = formatCurrency(expense)
        updateMainPieChart(income, expense)
    }

    private fun updateStatsCards(income: Double, expense: Double, balance: Double) {
        binding.tvBalance.text = formatCurrency(balance)
        binding.tvIncomeStat.text = formatCurrency(income)
        
        // Saving = Income - Expense (same as balance for now)
        val saving = balance
        binding.tvSaving.text = formatCurrency(saving)
        
        // TODO: Calculate percentages from previous month data
        // For now, show placeholder
    }

    private fun setupCharts() {
        setupMainPieChart()
        setupCategoryPieChart()
        setupBarChart()
    }

    private fun setupMainPieChart() {
        binding.pieChartMain.apply {
            description.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            
            // Center text với Total value
            setDrawCenterText(true)
            setCenterTextSize(18f)
            setCenterTextColor(Color.parseColor("#1F2937"))
            
            // Hole configuration (donut style)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 75f  // Lỗ lớn hơn để hiển thị text
            transparentCircleRadius = 80f
            
            // Legend ẩn đi vì đã có custom legend bên dưới
            legend.isEnabled = false
            
            // Rotation
            rotationAngle = 0f
            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            
            animateY(1200, Easing.EaseInOutQuad)
        }
    }

    private fun setupCategoryPieChart() {
        binding.pieChartCategory.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 50f
            transparentCircleRadius = 55f
            
            setDrawCenterText(false)
            
            // Legend configuration
            legend.apply {
                isEnabled = false  // Sẽ dùng RecyclerView custom
            }
            
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            animateY(1400, Easing.EaseInOutQuad)
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
                textColor = Color.parseColor("#6B7280")
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
                axisMinimum = 0f
                textSize = 10f
                textColor = Color.parseColor("#6B7280")
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
                textColor = Color.parseColor("#1F2937")
            }
            
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun updateMainPieChart(income: Double, expense: Double) {
        val total = income + expense
        val entries = mutableListOf<PieEntry>()
        
        if (income > 0) {
            entries.add(PieEntry(income.toFloat(), "Income"))
        }
        if (expense > 0) {
            entries.add(PieEntry(expense.toFloat(), "Expense"))
        }
        
        if (entries.isEmpty()) {
            binding.pieChartMain.clear()
            binding.pieChartMain.centerText = "No Data"
            return
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#5B8DEE"),  // Blue cho Income
                Color.parseColor("#FF6B9D")   // Pink cho Expense
            )
            valueTextSize = 0f  // Ẩn text trên slice
            sliceSpace = 4f
            selectionShift = 8f
        }
        
        val data = PieData(dataSet)
        binding.pieChartMain.data = data
        
        // Set center text với total amount
        binding.pieChartMain.centerText = "${formatCurrency(total)}\nTotal"
        
        binding.pieChartMain.invalidate()
    }

    private fun updateCategoryChart(transactions: List<com.example.financemanagement.domain.model.Transaction>) {
        // Filter chỉ lấy expense transactions
        val expenseTransactions = transactions.filter { 
            it.type.equals("EXPENSE", ignoreCase = true) 
        }
        
        if (expenseTransactions.isEmpty()) {
            binding.pieChartCategory.clear()
            // RecyclerView legend removed for simplicity
            return
        }
        
        // Group by category và tính tổng
        val categoryData = expenseTransactions
            .groupBy { it.category?.name ?: "Other" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }  // Sort theo amount giảm dần
        
        val entries = categoryData.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = categoryColors.take(entries.size)
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 6f
        }
        
        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChartCategory))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        
        binding.pieChartCategory.data = data
        binding.pieChartCategory.invalidate()
        
        // Legend will be shown in chart itself
    }

    private fun updateBarChart(transactions: List<com.example.financemanagement.domain.model.Transaction>) {
        val dailyData = transactions
            .groupBy { it.date?.substring(0, 10) ?: "Unknown" }
            .mapValues { (_, txns) ->
                val income = txns
                    .filter { it.type.equals("INCOME", ignoreCase = true) }
                    .sumOf { it.amount }
                val expense = txns
                    .filter { it.type.equals("EXPENSE", ignoreCase = true) }
                    .sumOf { it.amount }
                Pair(income, expense)
            }
            .toList()
            .take(7)
        
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
            color = Color.parseColor("#5B8DEE")
            valueTextSize = 10f
            valueTextColor = Color.parseColor("#1F2937")
        }
        
        val expenseDataSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.parseColor("#FF6B9D")
            valueTextSize = 10f
            valueTextColor = Color.parseColor("#1F2937")
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
