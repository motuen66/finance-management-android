# Finance Management Android App 💰

Mobile application for personal finance management built with modern Android development practices.

## 📱 Features

- **Authentication**: User registration and login with JWT tokens
- **Dashboard**: Overview of financial data
- **Saving Goals**: Track and manage savings goals with progress tracking
- **Transactions**: Record and view income/expense transactions
- **Categories**: Organize transactions by categories
- **Budgets**: Set and monitor spending budgets
- **Reports**: Generate financial reports

## 🛠️ Tech Stack

### Architecture & Patterns
- **MVVM (Model-View-ViewModel)** architecture
- **Repository Pattern** for data layer abstraction
- **Clean Architecture** with domain, data, and UI layers

### Libraries & Frameworks
- **Language**: Kotlin
- **Dependency Injection**: Hilt/Dagger 2.51.1
- **Networking**: 
  - Retrofit 2.11.0
  - OkHttp 4.12.0
  - Gson converter
- **Local Storage**:
  - Room 2.5.2 (SQLite)
  - DataStore Preferences 1.1.1 (JWT tokens)
- **UI**: 
  - ViewBinding
  - Material Components 1.11.0
  - Navigation Component 2.7.7
- **Async**: Kotlin Coroutines + Flow

### Backend Integration
- .NET 8 API with MongoDB
- Hosted on Azure
- RESTful API endpoints

## 📋 Prerequisites

- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK (minSdk 24, targetSdk 34)
- Kotlin 1.9.0+

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/financemanagementandroid.git
cd financemanagementandroid
```

### 2. Open in Android Studio
- Open Android Studio
- File → Open → Select project folder
- Wait for Gradle sync to complete

### 3. Configure API endpoint (Optional)
The app is already configured with a demo backend API. If you want to use your own backend:
```kotlin
// app/src/main/java/com/example/financemanagement/utils/Constants.kt
const val BASE_URL = "YOUR_BACKEND_URL"
```

### 4. Build and Run
- Connect your Android device or start an emulator
- Click Run (Shift + F10) or Debug (Shift + F9)

## 🏗️ Project Structure

```
app/
├── src/main/java/com/example/financemanagement/
│   ├── data/              # Data layer
│   │   ├── local/         # Room database, DAOs, entities
│   │   ├── remote/        # API service, DTOs
│   │   └── repository/    # Repository implementations
│   ├── di/                # Dependency injection modules
│   ├── domain/            # Domain models and business logic
│   │   └── model/         # Domain models
│   ├── ui/                # UI layer
│   │   ├── auth/          # Login & Register screens
│   │   ├── dashboard/     # Dashboard screen
│   │   └── main/          # MainActivity
│   ├── viewmodel/         # ViewModels
│   └── utils/             # Utility classes
└── src/main/res/          # Resources (layouts, drawables, etc.)
```

## 🔑 Key Components

### Authentication Flow
1. User enters credentials
2. ViewModel validates input
3. Repository calls API
4. JWT token saved to DataStore
5. Token auto-injected in subsequent requests via Interceptor

### Data Flow
```
UI (Fragment) → ViewModel → Repository → API Service / Room DAO
                    ↓
              StateFlow/Flow
                    ↓
          UI observes & updates
```

## 📦 Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard/R8 optimization

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 📝 API Endpoints

- `POST /api/Account/login` - User login
- `POST /api/Account/register` - User registration
- `GET /api/Transactions` - Get all transactions
- `GET /api/SavingGoals` - Get all saving goals
- `GET /api/Categories` - Get all categories
- `GET /api/Budgets` - Get all budgets
- `GET /api/Reports` - Get financial reports

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**Your Name**
- GitHub: [@motuen66](https://github.com/motuen66)

## 🙏 Acknowledgments

- Backend API powered by .NET 8 + MongoDB
- Material Design guidelines
- Android Jetpack libraries
