# Finance Management Android App — Full Development & Design Instructions

> **Purpose:** This document defines all unified standards for UI/UX, architecture, code style, API integration, and Copilot guidance for the **Finance Management Android App** project.  
> It ensures the entire development team and AI assistants (like GitHub Copilot) follow a single, consistent set of principles.

---

## 0) TL;DR (For Copilot & Developers)

- Kotlin | MVVM + Clean Architecture | Repository Pattern  
- Hilt DI | Retrofit + OkHttp | Room | DataStore | Coroutines + Flow | Navigation Component | ViewBinding | Material 3  
- Theme: **Purple → Pink Gradient**, no blue primary.  
- Backend: **.NET 8 Web API** hosted on **Azure**, connected to **MongoDB Atlas**.  
- Each screen supports **Loading / Success / Empty / Error** states.  
- Strings → `strings.xml`, dimens → `dimens.xml`, colors → `colors.xml`.  
- Common UI components live in `ui/common`.  
- JWT saved in DataStore and auto-attached in OkHttp interceptor.  
- App supports light/dark mode and basic accessibility.

---

## 1) Project Overview

A **modern personal finance management app** built on Android.  
It enables users to:
- Register & login securely (JWT)
- View dashboard with income, expense, and balance overview
- Add / edit / delete transactions
- Manage monthly budgets per category
- View visual financial reports (charts & summaries)
- Manage multiple accounts (cash, bank, card)
- Create & track saving goals with completion progress
- Synchronize data with a .NET 8 backend via REST API

---

## 2) Technology Stack

| Layer | Technology |
|-------|-------------|
| Language | Kotlin (minSdk 24, targetSdk 34) |
| Architecture | MVVM + Repository + Clean Architecture |
| DI | Hilt (Dagger 2.51.1) |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 + Gson |
| Local Storage | Room (SQLite) 2.5.2 + DataStore 1.1.1 |
| Async | Kotlin Coroutines + Flow + StateFlow |
| UI | ViewBinding, Material Design 3, Navigation Component |
| Backend | .NET 8 Web API, MongoDB Atlas, Azure App Service |
| Logging | HttpLoggingInterceptor (debug only) |

---

## 3) Folder Structure

```
com.example.financemanagement
│
├── data
│   ├── local/                    # Room, DAO, Entities, TokenManager
│   │   ├── dao/                  # All DAOs (UserDao, TransactionDao, etc.)
│   │   ├── db/                   # AppDatabase configuration
│   │   ├── entities/             # Room entities (UserEntity, TransactionEntity, etc.)
│   │   └── TokenManager.kt       # JWT token management with DataStore
│   │
│   ├── remote/                   # Retrofit API, DTOs, ApiService
│   │   ├── api/                  # ApiService interface
│   │   └── models/               # API request/response DTOs
│   │
│   └── repository/               # Repository interfaces & implementations
│       ├── AuthRepository.kt
│       ├── AuthRepositoryImpl.kt
│       ├── TransactionRepository.kt
│       ├── TransactionRepositoryImpl.kt
│       ├── SavingGoalRepository.kt
│       └── SavingGoalRepositoryImpl.kt
│
├── di/                           # Hilt dependency injection modules
│   ├── NetworkModule.kt          # Retrofit, OkHttp, ApiService
│   ├── DatabaseModule.kt         # Room database, DAOs
│   └── RepositoryModule.kt       # Repository bindings
│
├── domain
│   └── model/                    # Domain models (business entities)
│       ├── Transaction.kt
│       ├── Budget.kt
│       ├── Category.kt
│       ├── SavingGoal.kt
│       ├── Report.kt
│       └── User.kt
│
├── ui
│   ├── auth/                     # Authentication screens
│   │   ├── LoginFragment.kt
│   │   └── RegisterFragment.kt
│   │
│   ├── dashboard/                # Dashboard/Home screen
│   │   └── DashboardFragment.kt
│   │
│   ├── main/                     # Main activity
│   │   └── MainActivity.kt
│   │
│   └── common/                   # Shared UI components (optional)
│       ├── EmptyStateView.kt
│       └── LoadingView.kt
│
├── viewmodel/                    # ViewModels (separate from UI)
│   ├── AuthViewModel.kt
│   └── DashboardViewModel.kt
│
└── utils/                        # Helpers, extensions, constants
    └── Constants.kt              # API base URL, preferences keys
```

### Current Implementation Status

**Implemented:**
- ✅ Data layer: Room entities, DAOs, API service, repositories
- ✅ DI: Hilt modules for network, database, repositories
- ✅ Domain: Models for Transaction, Budget, Category, SavingGoal, Report, User
- ✅ UI: Login, Register, Dashboard fragments
- ✅ ViewModels: Auth, Dashboard with StateFlow
- ✅ Navigation Component with nav_graph.xml

**Not Yet Implemented (Future):**
- ⏳ UI screens: Reports, Accounts, Budget management
- ⏳ Use cases layer (currently using repositories directly)
- ⏳ Mappers for DTO ↔ Domain conversion (currently using domain models directly)
- ⏳ Common UI components library


---

## 4) Theme — Purple & Pink Gradient

### Color Tokens

| Token | Hex | Usage |
|--------|------|-------|
| Primary Gradient Start | `#A66BFF` | Start of gradient |
| Primary Gradient End | `#FF99CC` | End of gradient |
| Accent | `#FFC7E6` | Light accent color |
| Background | `#FAF7FF` | App background |
| Surface | `#FFFFFF` | Card background |
| Text Primary | `#2B2240` | Titles |
| Text Secondary | `#7A6B9B` | Descriptions |
| Positive | `#00C875` | Income |
| Negative | `#FF5A78` | Expense |
| Warning | `#FFD66B` | Alerts |
| Divider | `#E8E1F5` | Thin separators |

### Typography

| Role | Font Size | Weight |
|------|------------|---------|
| Title | 20sp | Bold |
| Subtitle | 16sp | Medium |
| Body | 14sp | Regular |
| Caption | 12sp | Regular |

### Component Design Rules

- **TopAppBar:** gradient background (#A66BFF → #FF99CC), white title.  
- **Buttons:** gradient fill, rounded corners (12dp), white bold text.  
- **Cards:** white background, 12dp radius, purple shadow (alpha 0.1).  
- **Bottom Navigation:** 5 tabs (Home, Reports, Accounts, Budget, Inbox) with purple active indicator.  
- **Charts:** use pastel colors (purple, pink, mint, orange).  
- **Animations:** `fade` or `slide` transitions (250ms).  
- **Icons:** outlined, consistent stroke width (Material Symbols).  

---

## 5) UX Guidelines

- Every list or summary must have an **empty state illustration + CTA**.  
- Always provide visible **feedback** for button clicks and loading states.  
- Avoid hard edges — use rounded corners and soft gradients.  
- Keep text minimal; prefer visuals and icons.  
- Ensure all touch targets ≥ 48dp.  
- All screens support **dark mode** with inverted gradients.  

---

## 6) Data & Model Design

### Domain Models (Kotlin)

```kotlin
data class Transaction(
  val id: String,
  val userId: String,
  val type: String, // INCOME | EXPENSE | TRANSFER
  val categoryId: String,
  val accountId: String,
  val amount: Long,
  val note: String?,
  val occurredAt: Instant
)

data class Budget(
  val id: String,
  val userId: String,
  val categoryId: String,
  val periodMonth: YearMonth,
  val amount: Long
)

data class Account(
  val id: String,
  val userId: String,
  val name: String,
  val type: String, // CASH | BANK | CARD
  val balance: Long
)

data class SavingGoal(
  val id: String,
  val userId: String,
  val title: String,
  val description: String?,
  val currentAmount: Long,
  val goalAmount: Long,
  val goalDate: LocalDate,
  val isCompleted: Boolean
)
## 7) Networking

**Base URL:** Configured in `Constants.kt`:
```kotlin
const val BASE_URL = "https://financetracker-be-hrg9czdme5hbawfq.southeastasia-01.azurewebsites.net/"
```

**Token Management:**
- JWT token saved in DataStore via `TokenManager`
- Auto-attached via `AuthInterceptor` in `NetworkModule`
- Header format: `Authorization: Bearer <token>`

**Timeout Configuration:**
- Connect: 30 seconds
- Read: 30 seconds
- Write: 30 seconds

**Logging:**
- `HttpLoggingInterceptor` with `Level.BODY` (enabled in all builds)
- Can be changed to debug-only if needed

**Error Handling:**
- HTTP 401 → Session expired / Invalid token
- HTTP 500 → Server error
- Network errors → Wrapped in `Result<T>` with proper error messages

**API Endpoints (Implemented):**
```kotlin
// Authentication
POST /api/Account/login
POST /api/Account/register

// Transactions
GET /api/Transactions
GET /api/Transactions/{id}
POST /api/Transactions
PUT /api/Transactions/{id}
DELETE /api/Transactions/{id}

// Categories
GET /api/Categories
POST /api/Categories
PUT /api/Categories/{id}
DELETE /api/Categories/{id}

// Budgets
GET /api/Budgets
POST /api/Budgets
PUT /api/Budgets/{id}
DELETE /api/Budgets/{id}

// Saving Goals
GET /api/SavingGoals
POST /api/SavingGoals
PUT /api/SavingGoals/{id}
DELETE /api/SavingGoals/{id}

// Reports
GET /api/Reports
POST /api/Reports/generate
GET /api/Reports/{id}

// Users
GET /api/Users/me
PUT /api/Users/me
```

## 8) Repository Pattern (Current Implementation)

**Repository Rules:**
- Repositories combine Room + Remote API sources
- Return `Result<T>` for operations that can fail
- Return `Flow<T>` for observable data streams
- Handle errors and convert to domain models

**Current Implementation:**
```kotlin
interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun logout()
    fun observeToken(): Flow<String?>
}

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                Result.success(body)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Use Cases (Future Enhancement):**
- Currently ViewModels call repositories directly
- Can add use case layer later for complex business logic
- Example structure:
```kotlin
class GetMonthlySummaryUseCase @Inject constructor(
    private val repo: TransactionRepository
) {
    suspend operator fun invoke(month: YearMonth): Result<Summary> {
        // Add validation, business logic here
        return repo.getSummary(month)
    }
}
```
9) UI Components & Shared Elements
Component	Purpose
PrimaryButton	Full-width gradient button
OutlinedButton	White button with purple border
GradientAppBar	Top bar with gradient background
TransactionCard	Shows icon + title + amount
CategoryChip	Selectable chip with emoji & name
EmptyStateView	Illustration + text + CTA
ProgressGoalCard	Shows saving goal with progress bar
10) Copilot Prompt Library

Prompt 1 — Gradient App Bar

Build a TopAppBar with a purple to pink gradient (#A66BFF → #FF99CC), white title, and right-aligned action icons using Material 3 and ViewBinding.

Prompt 2 — Home Screen

Create a HomeFragment showing current month header (← Nov 2025 →), three cards for Income, Expense, and Balance, followed by a list of daily transactions grouped by date, and a large "+ Add" button at the bottom.

Prompt 3 — Add Transaction Sheet

Design a bottom sheet with title "Add" and 4 cards: Expense, Income, Transfer, and Budget. Each card has an icon and description.

Prompt 4 — Add Income Screen

Build a form with date picker, category chip selector, numeric keypad for amount, note field, account dropdown, and a gradient "Save" button.

Prompt 5 — Reports Screen

Create a ReportsFragment with segmented control (Expense/Income), chart, and summary cards. Use gradient accents for KPI cards.

Prompt 6 — Accounts Screen

Display account cards (Cash, Account, Card) with income, expense, and balance. Add "+ Add Account" button at the bottom.

Prompt 7 — Budget Screen

Show list of budget categories with emoji and edit button. Tapping Edit opens a screen with custom numeric keypad and gradient Save button.

Prompt 8 — Saving Goals

Create a screen listing goals with progress bars. Add “+ Add Goal” button leading to a form for title, goal amount, current amount, and goal date.

Prompt 9 — Authentication

Implement login/register screens with gradient background, email/password inputs, “Sign in” button, and token saved via DataStore.
## 11) State Management

**Current Implementation:**

ViewModels use sealed classes for state:
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val response: LoginResponse) : AuthState()
    data class RegisterSuccess(val response: RegisterResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

Or use separate StateFlows:
```kotlin
class DashboardViewModel @Inject constructor(
    private val savingGoalRepository: SavingGoalRepository
) : ViewModel() {
    
    private val _savingGoals = MutableStateFlow<List<SavingGoal>>(emptyList())
    val savingGoals: StateFlow<List<SavingGoal>> = _savingGoals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun fetchSavingGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = savingGoalRepository.getSavingGoals()
            
            if (result.isSuccess) {
                _savingGoals.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            
            _isLoading.value = false
        }
    }
}
```

**UI Observation:**
```kotlin
viewLifecycleOwner.lifecycleScope.launchWhenStarted {
    viewModel.savingGoals.collectLatest { list ->
        if (list.isEmpty()) {
            showEmptyState()
        } else {
            renderList(list)
        }
    }
}

viewLifecycleOwner.lifecycleScope.launchWhenStarted {
    viewModel.isLoading.collectLatest { isLoading ->
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

viewLifecycleOwner.lifecycleScope.launchWhenStarted {
    viewModel.error.collectLatest { error ->
        error?.let { 
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() 
        }
    }
}
```

**Alternative Pattern (Recommended for Future):**
```kotlin
data class UiState<T>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: String? = null,
    val empty: Boolean = false
)

// In ViewModel
private val _uiState = MutableStateFlow(UiState<List<SavingGoal>>())
val uiState: StateFlow<UiState<List<SavingGoal>>> = _uiState

// In Fragment
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when {
            state.loading -> showLoading()
            state.error != null -> showError(state.error)
            state.empty -> showEmptyView()
            else -> render(state.data)
        }
    }
}
```
