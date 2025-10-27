package com.example.financemanagement.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentRegisterNewBinding
import com.example.financemanagement.vm.AuthUiState
import com.example.financemanagement.vm.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterNewBinding? = null
    private val binding get() = _binding!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // clear errors when user focuses fields
        binding.inputName.setOnFocusChangeListener { _, _ -> binding.inputName.error = null }
        binding.inputEmail.setOnFocusChangeListener { _, _ -> binding.inputEmail.error = null }
        binding.inputPassword.setOnFocusChangeListener { _, _ -> binding.inputPassword.error = null }

        binding.btnRegister.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString()

            // Validation
            if (name.isEmpty()) {
                binding.inputName.error = "Vui lòng nhập tên"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.inputEmail.error = "Vui lòng nhập email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.inputPassword.error = "Vui lòng nhập mật khẩu"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.inputPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
                return@setOnClickListener
            }

            // Call ViewModel to register
            vm.register(name, email, password)
        }

        // Navigate back to Login
        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        binding.progress.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                    }
                    is AuthUiState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is AuthUiState.RegisterSuccess -> {
                        binding.progress.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        // Navigate to Dashboard
                        findNavController().navigate(R.id.action_registerFragment_to_dashBoardFragment)
                    }
                    is AuthUiState.LoginSuccess -> {
                        // Ignore login success in register screen
                    }
                    is AuthUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
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