# Copilot Instructions for `finance-management-android`

## App Overview
- Kotlin MVVM app (minSdk 24 / targetSdk 34) for budgets, transactions, reports, and auth; entry point `App.kt` (`@HiltAndroidApp`).
- Layers: `data/` (remote/Room/TokenManager), `domain/models/` (UI-safe models), `viewmodel/` (Hilt view models using `StateFlow`), `ui/` fragments hosted by `ui/main/MainActivity` with routes in `res/navigation/nav_graph.xml`.
- Default backend URL sits in `utils/Constants.BASE_URL`; keep overrides centralized there. JWT persistence uses `TokenManager` (DataStore + cached accessor for interceptors).

## Data & Networking
- All Retrofit routes live in `data/remote/api/ApiService.kt`; use the same HTTP verbs and relative paths when adding endpoints. Expect `Response<T>` and guard `isSuccessful`, null bodies, and error bodies like existing implementations.
- Repositories (e.g. `TransactionRepositoryImpl`) must return `Result<T>` and log failures with `Log.e`. Mirror the `try/catch IOException` + generic `Exception` pattern to keep error messaging consistent.
- When saving tokens on login/register, call `TokenManager.saveToken()` and rely on the provided auth interceptor (`NetworkModule.provideAuthInterceptor`) for request headers.

## Persistence
- Room setup lives in `data/local/db/AppDatabase.kt`; DAOs in `data/local/dao/` expose both `Flow` streams and suspend fetches. Entities include mapper helpers (`toDomainModel` / `toEntity`)—extend these instead of duplicating conversion logic.
- If you add columns/entities, update `AppDatabase` version and keep the destructive migration fallback unless a proper migration is supplied.

## ViewModels & UI
- ViewModels follow `@HiltViewModel` + constructor injection (`viewModel/` namespace). The legacy `vm/AuthViewModel` is still in use—do not add new classes under `vm`; prefer `viewmodel/` and adjust package imports carefully.
- UI fragments use View Binding (`Fragment*.inflate`), collect `StateFlow` with `viewLifecycleOwner.lifecycleScope.launchWhenStarted`, and emit toast-based errors (see `ui/auth/LoginFragment`). Match this lifecycle-aware pattern when adding observers.
- Navigation uses safe-action IDs defined in `nav_graph.xml`; update the graph and call `findNavController().navigate(...)` with generated IDs from `R.id.*` when wiring new screens.

## Dependency Injection
- Hilt modules: `NetworkModule` (OkHttp, Retrofit, auth interceptor), `DatabaseModule` (Room + DAOs), `RepositoryModule` (`@Binds` interfaces). Register new singletons in these modules instead of creating ad-hoc factories.
- OkHttp already chains logging + auth interceptors; attach additional interceptors through the same builder if required to avoid bypassing token injection.

## Build & Testing Workflow
- Use the wrapper scripts: `gradlew.bat assembleDebug`, `gradlew.bat test`, `gradlew.bat lint`. `clean-build.bat` is available for local cache resets on Windows.
- Tests reside in `app/src/test` and `app/src/androidTest`; mirror existing package paths when introducing new suites.

## Style Notes
- Resource strings belong in `res/values/strings.xml` (Vietnamese copy already present); leave Kotlin source identifiers in English.
- Reuse `utils/Resource` sealed class if you need loading/success/error wrappers, otherwise keep UI state sealed interfaces similar to `AuthUiState`.
- Avoid print statements—use `Log.d/e` or structured UI feedback. Maintain constructor injection across new classes to keep the Hilt graph coherent.
