package com.example.financemanagement.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.financemanagement.databinding.FragmentDashboardNewBinding
import com.example.financemanagement.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardNewBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            dashboardViewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe saving goals
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            dashboardViewModel.savingGoals.collectLatest { list ->
                if (list.isEmpty()) {
                    binding.tvTransactions.text = "Không có saving goals nào"
                } else {
                    val savingGoalsText = list.joinToString("\n\n") { goal ->
                        val progress = if (goal.goalAmount > 0) {
                            (goal.currentAmount / goal.goalAmount * 100).toInt()
                        } else 0
                        """
                        ID: ${goal.id}
                        Tiêu đề: ${goal.title}
                        Mục tiêu: ${goal.goalAmount}
                        Hiện tại: ${goal.currentAmount}
                        Tiến độ: $progress%
                        Ngày đến hạn: ${goal.goalDate}
                        Hoàn thành: ${if (goal.isCompleted) "Có" else "Chưa"}
                        """.trimIndent()
                    }
                    binding.tvTransactions.text = "Có ${list.size} saving goals:\n\n$savingGoalsText"
                }
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            dashboardViewModel.error.collectLatest { err ->
                err?.let { Toast.makeText(requireContext(), "Lỗi: $it", Toast.LENGTH_LONG).show() }
            }
        }

        // Fetch saving goals on start
        dashboardViewModel.fetchSavingGoals()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}