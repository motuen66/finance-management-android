package com.example.financemanagement.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentBudgetDetailsBinding
import com.example.financemanagement.utils.CurrencyFormatter
import com.example.financemanagement.viewmodel.BudgetDetailsUiState
import com.example.financemanagement.viewmodel.BudgetDetailsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class BudgetDetailsFragment : Fragment() {

    private var _binding: FragmentBudgetDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetDetailsViewModel by viewModels()
    private val args: BudgetDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        
        // Load budget details
        viewModel.loadBudgetDetails(args.budgetId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is BudgetDetailsUiState.Loading -> {
                        showLoading(true)
                    }
                    is BudgetDetailsUiState.Success -> {
                        showLoading(false)
                        displayBudgetDetails(state)
                    }
                    is BudgetDetailsUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun displayBudgetDetails(state: BudgetDetailsUiState.Success) {
        val budget = state.budget
        val spentAmount = state.spentAmount
        val percentage = state.percentage
        val remainingAmount = budget.limitAmount - spentAmount

        // Category name
        binding.tvCategoryName.text = budget.categoryName

        // Budget limit - sử dụng format ngắn
        binding.tvBudgetLimit.text = CurrencyFormatter.formatShortWithCurrency(budget.limitAmount)

        // Spent amount - sử dụng format ngắn
        binding.tvSpentAmount.text = CurrencyFormatter.formatShortWithCurrency(spentAmount)
        
        // Set color based on percentage
        val spentColor = when {
            percentage >= 100 -> ContextCompat.getColor(requireContext(), R.color.negative)
            percentage >= 75 -> ContextCompat.getColor(requireContext(), R.color.warning)
            else -> ContextCompat.getColor(requireContext(), R.color.text_primary)
        }
        binding.tvSpentAmount.setTextColor(spentColor)

        // Remaining amount - sử dụng format ngắn
        binding.tvRemainingAmount.text = CurrencyFormatter.formatShortWithCurrency(remainingAmount)
        val remainingColor = if (remainingAmount < 0) {
            ContextCompat.getColor(requireContext(), R.color.negative)
        } else {
            ContextCompat.getColor(requireContext(), R.color.positive)
        }
        binding.tvRemainingAmount.setTextColor(remainingColor)

        // Progress bar
        binding.progressBar.progress = percentage
        val progressColor = when {
            percentage >= 100 -> R.color.negative
            percentage >= 75 -> R.color.warning
            else -> R.color.primary_gradient_start
        }
        binding.progressBar.setIndicatorColor(
            ContextCompat.getColor(requireContext(), progressColor)
        )

        // Percentage text
        binding.tvPercentage.text = "$percentage%"

        // Month info
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        binding.tvMonthInfo.text = "Tháng $month/$year"

        // Setup chart
        setupPieChart(budget.limitAmount, spentAmount, remainingAmount.coerceAtLeast(0.0))
    }

    private fun setupPieChart(limit: Double, spent: Double, remaining: Double) {
        android.util.Log.d("BudgetDetailsChart", "Chart data: limit=$limit, spent=$spent, remaining=$remaining")
        
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        if (spent > limit) {
            // Case: Budget exceeded
            // Budget limit portion (purple)
            entries.add(PieEntry(limit.toFloat(), "Budget"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.primary_gradient_start))
            
            // Exceeded portion (red)
            val exceeded = (spent - limit).toFloat()
            entries.add(PieEntry(exceeded, "Exceeded"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.negative))
            
            android.util.Log.d("BudgetDetailsChart", "Exceeded mode: budget=$limit, exceeded=$exceeded")
        } else {
            // Case: Within budget
            if (spent > 0) {
                entries.add(PieEntry(spent.toFloat(), "Spent"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.primary_gradient_start))
            }
            
            if (remaining > 0) {
                entries.add(PieEntry(remaining.toFloat(), "Remaining"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.positive))
            }
            
            android.util.Log.d("BudgetDetailsChart", "Normal mode: spent=$spent, remaining=$remaining")
        }
        
        android.util.Log.d("BudgetDetailsChart", "Chart entries: ${entries.size} items")
        entries.forEachIndexed { index, entry ->
            android.util.Log.d("BudgetDetailsChart", "Entry $index: ${entry.label} = ${entry.value}")
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChart))

        // Chart configuration
        binding.pieChart.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            // Center text sử dụng format ngắn
            centerText = CurrencyFormatter.formatShort(spent)
            setCenterTextSize(18f)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            legend.textSize = 12f
            
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            
            animateY(1000, Easing.EaseInOutQuad)
            
            invalidate()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
