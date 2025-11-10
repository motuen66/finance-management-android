package com.example.financemanagement.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financemanagement.databinding.FragmentHomeCalendarBinding
import com.example.financemanagement.domain.model.CalendarDay
import com.example.financemanagement.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeCalendarFragment : Fragment() {

    private var _binding: FragmentHomeCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    
    private lateinit var calendarAdapter: CalendarAdapter
    private var currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        observeTransactions()
    }

    private fun setupCalendar() {
        // Setup RecyclerView with 7 columns (days of week)
        calendarAdapter = CalendarAdapter { day ->
            onDayClick(day)
        }
        
        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }

        // Update month/year display
        updateMonthYearDisplay()

        // Previous month button
        binding.btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateMonthYearDisplay()
            loadCalendarDays()
        }

        // Next month button
        binding.btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateMonthYearDisplay()
            loadCalendarDays()
        }

        // Load initial calendar
        loadCalendarDays()
    }

    private fun updateMonthYearDisplay() {
        binding.tvMonthYear.text = monthYearFormat.format(currentCalendar.time)
    }

    private fun loadCalendarDays() {
        val days = mutableListOf<CalendarDay>()
        
        // Get first day of current month
        val firstDayOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Get last day of current month
        val lastDayOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        
        // Get day of week for first day (0 = Sunday)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
        
        // Add days from previous month to fill the first row
        val prevMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (i in firstDayOfWeek - 1 downTo 0) {
            val day = daysInPrevMonth - i
            days.add(CalendarDay(
                dayOfMonth = day,
                month = prevMonth.get(Calendar.MONTH),
                year = prevMonth.get(Calendar.YEAR),
                isCurrentMonth = false,
                isToday = false
            ))
        }
        
        // Add days of current month
        val today = Calendar.getInstance()
        val daysInCurrentMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (day in 1..daysInCurrentMonth) {
            val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                    currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            
            days.add(CalendarDay(
                dayOfMonth = day,
                month = currentCalendar.get(Calendar.MONTH),
                year = currentCalendar.get(Calendar.YEAR),
                isCurrentMonth = true,
                isToday = isToday
            ))
        }
        
        // Add days from next month to complete the grid (6 rows x 7 days = 42 cells)
        val nextMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
            add(Calendar.MONTH, 1)
        }
        
        val remainingCells = 42 - days.size
        for (day in 1..remainingCells) {
            days.add(CalendarDay(
                dayOfMonth = day,
                month = nextMonth.get(Calendar.MONTH),
                year = nextMonth.get(Calendar.YEAR),
                isCurrentMonth = false,
                isToday = false
            ))
        }
        
        calendarAdapter.submitList(days)
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactionGroups.collectLatest { groups ->
                android.util.Log.d("HomeCalendarFragment", "Received ${groups.size} transaction groups")
                // Update calendar with transaction data
                updateCalendarWithTransactions(groups)
            }
        }
    }

    private fun updateCalendarWithTransactions(groups: List<com.example.financemanagement.domain.model.TransactionGroup>) {
        val currentList = calendarAdapter.currentList.toMutableList()
        if (currentList.isEmpty()) {
            android.util.Log.d("HomeCalendarFragment", "Calendar list is empty, skipping update")
            return
        }
        
        // Create a map of date -> (income, expense)
        val dateTransactionsMap = mutableMapOf<String, Pair<Double, Double>>()
        
        // ISO format parsers for transaction dates (try multiple formats)
        val isoFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val isoFormatWithoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        groups.forEach { group ->
            group.transactions.forEach { transaction ->
                try {
                    // Try parsing with different formats
                    val transactionDate = try {
                        isoFormatWithMillis.parse(transaction.date)
                    } catch (e: Exception) {
                        isoFormatWithoutMillis.parse(transaction.date)
                    }
                    
                    if (transactionDate != null) {
                        // Convert to dd/MM/yyyy format to match CalendarDay
                        val dateKey = dateFormat.format(transactionDate)
                        
                        // Get or create entry for this date
                        val current = dateTransactionsMap[dateKey] ?: Pair(0.0, 0.0)
                        
                        when (transaction.type.lowercase()) {
                            "income" -> {
                                dateTransactionsMap[dateKey] = Pair(current.first + transaction.amount, current.second)
                                android.util.Log.d("HomeCalendarFragment", "Added income ${transaction.amount} to $dateKey")
                            }
                            "expense" -> {
                                dateTransactionsMap[dateKey] = Pair(current.first, current.second + transaction.amount)
                                android.util.Log.d("HomeCalendarFragment", "Added expense ${transaction.amount} to $dateKey")
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeCalendarFragment", "Error parsing date: ${transaction.date}", e)
                }
            }
        }
        
        android.util.Log.d("HomeCalendarFragment", "Date transactions map size: ${dateTransactionsMap.size}")
        dateTransactionsMap.forEach { (date, amounts) ->
            android.util.Log.d("HomeCalendarFragment", "Date: $date, Income: ${amounts.first}, Expense: ${amounts.second}")
        }
        
        // Update calendar days with transaction data
        val updatedList = currentList.map { day ->
            val dateStr = day.getDateString()
            val (income, expense) = dateTransactionsMap[dateStr] ?: Pair(0.0, 0.0)
            
            day.copy(
                totalIncome = income,
                totalExpense = expense
            )
        }
        
        android.util.Log.d("HomeCalendarFragment", "Updated ${updatedList.filter { it.totalIncome > 0 || it.totalExpense > 0 }.size} days with transactions")
        calendarAdapter.submitList(updatedList)
    }

    private fun onDayClick(day: CalendarDay) {
        if (!day.isCurrentMonth) return
        
        val dateString = day.getDateString()
        binding.tvSelectedDate.text = dateString
        binding.dailySummaryCard.visibility = View.VISIBLE
        
        // Update summary
        binding.tvDailyIncome.text = formatAmount(day.totalIncome)
        binding.tvDailyExpense.text = formatAmount(day.totalExpense)
        
        val balance = day.totalIncome - day.totalExpense
        binding.tvDailyBalance.text = formatAmount(balance)
        
        binding.tvDailyBalance.setTextColor(
            if (balance >= 0) 
                android.graphics.Color.parseColor("#4CAF50") 
            else 
                android.graphics.Color.parseColor("#F44336")
        )
    }

    private fun formatAmount(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000)
            amount >= 1_000_000 -> String.format("%.0fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
            else -> String.format("%.0f", amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
