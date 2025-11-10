class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        setupObservers()
        return binding.root
    }

    private fun setupObservers() {
        dashboardViewModel.transactions.observe(viewLifecycleOwner, { transactions ->
            // Update UI with transactions data
            binding.recyclerView.adapter = TransactionsAdapter(transactions)
        })

        dashboardViewModel.error.observe(viewLifecycleOwner, { error ->
            // Handle error state
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.fetchTransactions()
    }
}