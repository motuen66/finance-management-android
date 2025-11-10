package com.example.financemanagement.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupUI() {
        // TODO: Implement home screen UI
        binding.tvTitle.text = "Home Screen"

        // Show bottom sheet when button clicked
        binding.openSelectTransactionTypeBtn.setOnClickListener {
            showBottomSheet()
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
            // Handle Option 2
            bottomSheetDialog.dismiss()
        }

        sheetView.findViewById<View>(R.id.selectTransferType).setOnClickListener {
            // Handle Option 3
            bottomSheetDialog.dismiss()
        }

        sheetView.findViewById<View>(R.id.selectBudgetType).setOnClickListener {
            // Handle Option 4
            bottomSheetDialog.dismiss()
        }
    }
}
