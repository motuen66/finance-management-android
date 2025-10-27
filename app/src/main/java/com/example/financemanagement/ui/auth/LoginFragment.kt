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
import com.example.financemanagement.databinding.FragmentLoginNewBinding
import com.example.financemanagement.vm.AuthUiState
import com.example.financemanagement.vm.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginNewBinding? = null
    private val binding get() = _binding!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // simple input clearing of errors
        binding.inputEmail.setOnFocusChangeListener { _, _ -> binding.inputEmail.error = null }
        binding.inputPassword.setOnFocusChangeListener { _, _ -> binding.inputPassword.error = null }

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val pass = binding.inputPassword.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.login(email, pass)
        }

        // Navigate to Register screen
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        binding.progress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is AuthUiState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is AuthUiState.LoginSuccess -> {
                        binding.progress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        // Navigate to Dashboard
                        findNavController().navigate(R.id.action_loginFragment_to_dashBoardFragment)
                    }
                    is AuthUiState.RegisterSuccess -> {
                        // Ignore register success in login screen
                    }
                    is AuthUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
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