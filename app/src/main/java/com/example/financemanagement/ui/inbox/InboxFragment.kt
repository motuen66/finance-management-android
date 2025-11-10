package com.example.financemanagement.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentInboxBinding
import com.example.financemanagement.databinding.ItemCategoryBinding
import com.example.financemanagement.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = "Category"

        // Observe UI state
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            categoryViewModel.uiState.collectLatest { state ->
                when (state) {
                    is com.example.financemanagement.viewmodel.CategoryUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.scrollView.visibility = View.GONE
                    }
                    is com.example.financemanagement.viewmodel.CategoryUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView.visibility = View.VISIBLE
                        populateCategories(state.categories)
                    }
                    is com.example.financemanagement.viewmodel.CategoryUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView.visibility = View.VISIBLE
                        android.widget.Toast.makeText(requireContext(), "Error: ${state.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun populateCategories(list: List<com.example.financemanagement.domain.model.Category>) {
        val incomeContainer = binding.incomeContainer
        val expenseContainer = binding.expenseContainer

        incomeContainer.removeAllViews()
        expenseContainer.removeAllViews()

        if (list.isEmpty()) {
            // Show empty state
            val emptyText = TextView(requireContext()).apply {
                text = "No categories found. Add categories to organize your transactions."
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = 14f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(16, 32, 16, 32)
            }
            incomeContainer.addView(emptyText)
            return
        }

        val incomeCategories = list.filter { it.type.equals("Income", ignoreCase = true) }
        val expenseCategories = list.filter { !it.type.equals("Income", ignoreCase = true) }

        // Populate income categories
        if (incomeCategories.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No income categories"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = 14f
                setPadding(8, 16, 8, 16)
            }
            incomeContainer.addView(emptyText)
        } else {
            incomeCategories.forEach { cat ->
                addCategoryItem(cat, incomeContainer, true)
            }
        }

        // Populate expense categories
        if (expenseCategories.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No expense categories"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = 14f
                setPadding(8, 16, 8, 16)
            }
            expenseContainer.addView(emptyText)
        } else {
            expenseCategories.forEach { cat ->
                addCategoryItem(cat, expenseContainer, false)
            }
        }
    }

    private fun addCategoryItem(
        cat: com.example.financemanagement.domain.model.Category,
        container: LinearLayout,
        isIncome: Boolean
    ) {
        val itemBinding = ItemCategoryBinding.inflate(layoutInflater, container, false)
        
        // Set category name
        itemBinding.tvCategoryName.text = cat.name
        
        // Set icon, background, and badge based on type
        if (isIncome) {
            itemBinding.ivCategoryIcon.setImageResource(R.drawable.ic_income)
            itemBinding.iconBg.setBackgroundColor(android.graphics.Color.parseColor("#D1FAE5"))
            itemBinding.tvCategoryType.text = getString(R.string.type_income)
            itemBinding.tvCategoryType.setBackgroundResource(R.drawable.badge_income)
        } else {
            itemBinding.ivCategoryIcon.setImageResource(R.drawable.ic_expense)
            itemBinding.iconBg.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"))
            itemBinding.tvCategoryType.text = getString(R.string.type_expense)
            itemBinding.tvCategoryType.setBackgroundResource(R.drawable.badge_expense)
        }
        
        // Hide action buttons in this view
        itemBinding.btnEdit.visibility = android.view.View.GONE
        itemBinding.btnDelete.visibility = android.view.View.GONE
        
        // Add click listener (optional - for future navigation)
        itemBinding.root.setOnClickListener {
            // Handle category item click - navigate to category detail or edit
            android.widget.Toast.makeText(requireContext(), "Clicked: ${cat.name}", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // Add to container
        container.addView(itemBinding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
