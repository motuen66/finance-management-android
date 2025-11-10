plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.financemanagement"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.financemanagement"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true   // nếu dùng XML
        // compose = true     // nếu dùng Jetpack Compose
    }
    kotlin
    buildFeatures {
        dataBinding = true // Enable data binding
        viewBinding = true   // nếu dùng XML
        // compose = true     // nếu dùng Jetpack Compose
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Hilt (nếu dùng Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation(libs.androidx.fragment.ktx)
    kapt("com.google.dagger:hilt-compiler:2.51.1")
}
