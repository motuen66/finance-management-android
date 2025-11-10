package com.example.financemanagement.ui.budget

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.financemanagement.databinding.DialogAddCategoryBinding

class AddCategoryDialog(
    private val onCategoryCreated: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCreate.setOnClickListener {
            val categoryName = binding.etCategoryName.text.toString().trim()
            
            if (categoryName.isEmpty()) {
                binding.tilCategoryName.error = "Please enter category name"
                return@setOnClickListener
            }
            
            if (categoryName.length < 2) {
                binding.tilCategoryName.error = "Category name must be at least 2 characters"
                return@setOnClickListener
            }

            // Clear error and pass the name back
            binding.tilCategoryName.error = null
            onCategoryCreated(categoryName)
            dismiss()
        }

        // Clear error when user starts typing
        binding.etCategoryName.setOnFocusChangeListener { _, _ ->
            binding.tilCategoryName.error = null
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
