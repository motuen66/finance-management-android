package com.example.financemanagement.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.databinding.FragmentHomeDailyBinding
import com.example.financemanagement.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeDailyFragment : Fragment() {

    private var _binding: FragmentHomeDailyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var transactionGroupAdapter: TransactionGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup RecyclerView for transaction groups
        transactionGroupAdapter = TransactionGroupAdapter { transaction ->
            // Handle transaction click
            android.util.Log.d("HomeDailyFragment", "Transaction clicked: ${transaction.id}")
        }

        binding.rvTransactionGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionGroupAdapter
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
                android.util.Log.d("HomeDailyFragment", "Received ${groups.size} transaction groups")
                transactionGroupAdapter.submitList(groups)
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnLoadMore.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
