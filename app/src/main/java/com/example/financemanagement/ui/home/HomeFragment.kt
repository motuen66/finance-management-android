package com.example.financemanagement.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentHomeBinding
import com.example.financemanagement.viewmodel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var transactionGroupAdapter: TransactionGroupAdapter

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
        setupUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh transactions when returning to this fragment
        viewModel.refreshTransactions()
    }

    private fun setupUI() {
        // Setup RecyclerView for transaction groups
        transactionGroupAdapter = TransactionGroupAdapter { transaction ->
            // Handle transaction click
            android.util.Log.d("HomeFragment", "Transaction clicked: ${transaction.id}")
            // TODO: Navigate to transaction detail
        }

        binding.rvTransactionGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionGroupAdapter
        }

        // Show bottom sheet when ADD button clicked
        binding.openSelectTransactionTypeBtn.setOnClickListener {
            showBottomSheet()
        }

        // Load more button
        binding.btnLoadMore.setOnClickListener {
            viewModel.loadMoreTransactions()
        }
    }

    private fun observeViewModel() {
        // Observe transaction groups
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactionGroups.collectLatest { groups ->
                android.util.Log.d("HomeFragment", "Received ${groups.size} transaction groups")
                transactionGroupAdapter.submitList(groups)
            }
        }

        // Observe summary
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collectLatest { summary ->
                // Update summary card
                binding.summaryCard.tvIncome.text = formatAmount(summary.totalIncome)
                binding.summaryCard.tvExpense.text = formatAmount(summary.totalExpense)
                binding.summaryCard.tvBalance.text = formatAmount(summary.balance)

                // Set balance color
                val balanceColor = if (summary.balance >= 0) {
                    0xFF4CAF50.toInt() // Green
                } else {
                    0xFFF44336.toInt() // Red
                }
                binding.summaryCard.tvBalance.setTextColor(balanceColor)
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount >= 1_000_000) {
            String.format("%.0fM", amount / 1_000_000)
        } else if (amount >= 1_000) {
            String.format("%.0fK", amount / 1_000)
        } else {
            String.format("%.0f", amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.fragment_home_select_transaction_type_bottom_sheet, null)

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        // Handle button clicks inside the sheet
        sheetView.findViewById<View>(R.id.selectExpenseBtn).setOnClickListener {
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_homeAddExpenseFragment)
        }

        sheetView.findViewById<View>(R.id.selectIncomeType).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_homeAddIncomeFragment)
            bottomSheetDialog.dismiss()
        }
    }
}
