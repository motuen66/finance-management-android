class LoginFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.loginButton.setOnClickListener {
            val userDto = UserDto(
                username = binding.usernameEditText.text.toString(),
                password = binding.passwordEditText.text.toString()
            )
            authViewModel.login(userDto)
        }

        authViewModel.loginResult.observe(viewLifecycleOwner, { result ->
            result.onSuccess {
                // Handle successful login
            }.onFailure {
                // Handle login failure
            }
        })

        return binding.root
    }
}