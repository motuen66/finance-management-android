package com.example.financemanagement.ui.category

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.financemanagement.R
import com.example.financemanagement.databinding.DialogAddEditCategoryBinding
import com.example.financemanagement.domain.model.Category
import com.example.financemanagement.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogAddEditCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryViewModel by viewModels({ requireParentFragment() })
    
    private var existingCategory: Category? = null

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val ARG_CATEGORY_TYPE = "category_type"
        private const val ARG_CATEGORY_USER_ID = "category_user_id"

        fun newInstance(category: Category?): AddEditCategoryDialogFragment {
            val fragment = AddEditCategoryDialogFragment()
            if (category != null) {
                val args = Bundle().apply {
                    putString(ARG_CATEGORY_ID, category.id)
                    putString(ARG_CATEGORY_NAME, category.name)
                    putString(ARG_CATEGORY_TYPE, category.type)
                    putString(ARG_CATEGORY_USER_ID, category.userId)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.containsKey(ARG_CATEGORY_ID)) {
                existingCategory = Category(
                    id = args.getString(ARG_CATEGORY_ID)!!,
                    name = args.getString(ARG_CATEGORY_NAME)!!,
                    type = args.getString(ARG_CATEGORY_TYPE)!!,
                    userId = args.getString(ARG_CATEGORY_USER_ID)!!
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTypeSpinner()
        populateFields()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupTypeSpinner() {
        val typeOptions = listOf(
            getString(R.string.type_income),
            getString(R.string.type_expense)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter
    }

    private fun populateFields() {
        existingCategory?.let { category ->
            binding.tvDialogTitle.text = getString(R.string.edit_category)
            binding.etCategoryName.setText(category.name)
            
            // Set spinner selection based on type
            val position = when (category.type.lowercase()) {
                "income" -> 0
                "expense" -> 1
                else -> 0
            }
            binding.spinnerType.setSelection(position)
        } ?: run {
            binding.tvDialogTitle.text = getString(R.string.add_category)
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveCategory()
        }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        
        if (name.isEmpty()) {
            binding.etCategoryName.error = getString(R.string.error_name_required)
            return
        }

        val type = when (binding.spinnerType.selectedItemPosition) {
            0 -> "Income"
            1 -> "Expense"
            else -> "Income"
        }

        if (existingCategory != null) {
            // Update existing category
            viewModel.updateCategory(existingCategory!!.id, name, type)
        } else {
            // Create new category
            viewModel.createCategory(name, type)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
