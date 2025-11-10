package com.example.financemanagement.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentCategoryListBinding
import com.example.financemanagement.domain.model.Category
import com.example.financemanagement.viewmodel.CategoryActionState
import com.example.financemanagement.viewmodel.CategoryUiState
import com.example.financemanagement.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterSpinner()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onItemClick = { category ->
                showAddEditDialog(category)
            },
            onDeleteClick = { category ->
                showDeleteConfirmation(category)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CategoryListFragment.adapter
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_income),
            getString(R.string.filter_expense)
        )

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> viewModel.setFilter(null) // All
                    1 -> viewModel.setFilter("Income")
                    2 -> viewModel.setFilter("Expense")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CategoryUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvCategories.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                    }
                    is CategoryUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.categories.isEmpty()) {
                            binding.rvCategories.visibility = View.GONE
                            binding.tvEmpty.visibility = View.VISIBLE
                        } else {
                            binding.rvCategories.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                            adapter.submitList(state.categories)
                        }
                    }
                    is CategoryUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvCategories.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is CategoryActionState.Loading -> {
                        // Could show a progress dialog if needed
                    }
                    is CategoryActionState.Success -> {
                        Toast.makeText(requireContext(), R.string.operation_success, Toast.LENGTH_SHORT).show()
                        viewModel.resetActionState()
                    }
                    is CategoryActionState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetActionState()
                    }
                    is CategoryActionState.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun showAddEditDialog(category: Category?) {
        val dialog = AddEditCategoryDialogFragment.newInstance(category)
        dialog.show(childFragmentManager, "AddEditCategoryDialog")
    }

    private fun showDeleteConfirmation(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_category)
            .setMessage(getString(R.string.delete_category_confirmation, category.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteCategory(category.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
