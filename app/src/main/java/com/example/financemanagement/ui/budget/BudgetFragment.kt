package com.example.financemanagement.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.databinding.FragmentBudgetBinding
import com.example.financemanagement.viewmodel.BudgetUiState
import com.example.financemanagement.viewmodel.BudgetViewModel
import com.example.financemanagement.viewmodel.CategoryCreationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    
    private val vm: BudgetViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter
    
    private val numberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMonthYear()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupMonthYear() {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = monthFormat.format(calendar.time)
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(
            onEditClick = { budgetItem ->
                showEditBudgetDialog(budgetItem)
            },
            onDeleteClick = { budgetItem ->
                // Only allow delete if budget exists (has been set)
                if (budgetItem.budgetId != null) {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Xóa Budget")
                        .setMessage("Bạn có chắc muốn xóa budget cho ${budgetItem.categoryName}?")
                        .setPositiveButton("Xóa") { _, _ ->
                            vm.deleteBudget(budgetItem.budgetId)
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không có budget để xóa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        
        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun setupClickListeners() {
        // Hide Add Budget button - budgets are created from expense categories
        binding.btnAddBudget.visibility = View.GONE

        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialog = AddCategoryDialog { categoryName ->
            vm.createCategory(categoryName)
        }
        dialog.show(childFragmentManager, "AddCategoryDialog")
    }

    private fun showEditBudgetDialog(budgetItem: BudgetItem) {
        val dialog = EditBudgetDialog(
            budgetItem = budgetItem,
            onSave = { budgetId, categoryId, newLimitAmount, month, year ->
                vm.createOrUpdateBudget(budgetId, categoryId, newLimitAmount, month, year)
            }
        )
        dialog.show(childFragmentManager, "EditBudgetDialog")
    }

    private fun observeViewModel() {
        // Observe budget list state
        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                when (state) {
                    is BudgetUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is BudgetUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvBudgets.visibility = View.GONE
                    }
                    is BudgetUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        
                        // Get current month and year
                        val calendar = Calendar.getInstance()
                        val currentMonth = calendar.get(Calendar.MONTH) + 1
                        val currentYear = calendar.get(Calendar.YEAR)
                        
                        // Check if there are expense categories
                        val expenseCategories = state.categories // Already filtered in ViewModel
                        
                        if (expenseCategories.isEmpty()) {
                            // No expense categories - show empty state
                            binding.tvEmptyState.visibility = View.VISIBLE
                            binding.tvEmptyState.text = "Chưa có danh mục chi tiêu.\nVui lòng tạo danh mục để quản lý ngân sách."
                            binding.rvBudgets.visibility = View.GONE
                            binding.tvTotalBudget.text = "0"
                            return@collectLatest
                        }
                        
                        // Map each expense category to a budget item
                        val budgetItems = expenseCategories.map { category ->
                            // Find budget for this category (if exists)
                            val budget = state.budgets.find { it.categoryId == category.id }
                            BudgetItem.fromCategory(category, budget, currentMonth, currentYear)
                        }
                        
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvBudgets.visibility = View.VISIBLE
                        budgetAdapter.submitList(budgetItems)
                        
                        // Calculate total budget (sum of all limit amounts) with formatted number
                        val total = budgetItems.sumOf { it.limitAmount }
                        binding.tvTotalBudget.text = numberFormat.format(total.toLong())
                    }
                    is BudgetUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvBudgets.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Observe category creation state
        viewLifecycleOwner.lifecycleScope.launch {
            vm.categoryCreationState.collectLatest { state ->
                when (state) {
                    is CategoryCreationState.Idle -> {
                        // Do nothing
                    }
                    is CategoryCreationState.Creating -> {
                        Toast.makeText(
                            requireContext(),
                            "Đang tạo category...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is CategoryCreationState.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Đã tạo category: ${state.category.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        vm.resetCategoryCreationState()
                    }
                    is CategoryCreationState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi tạo category: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        vm.resetCategoryCreationState()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
