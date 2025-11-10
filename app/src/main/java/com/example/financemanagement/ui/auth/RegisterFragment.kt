import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financemanagement.vm.AuthViewModel

class RegisterFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.registerButton.setOnClickListener {
            val userDto = UserDto(
                username = binding.usernameEditText.text.toString(),
                password = binding.passwordEditText.text.toString()
            )
            authViewModel.register(userDto)
        }

        authViewModel.registrationResult.observe(viewLifecycleOwner, { result ->
            result.onSuccess {
                // Handle successful registration
            }.onFailure {
                // Handle registration failure
            }
        })

        return binding.root
    }
}