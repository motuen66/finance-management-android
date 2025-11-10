package com.example.financemanagement.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financemanagement.utils.JwtHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "finance_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")

    private val USER_ID_KEY = stringPreferencesKey("user_id")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // cached token to be used from non-suspending places (eg: okHttp interceptor)
    @Volatile
    private var cachedToken: String? = null

    val tokenFlow: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[TOKEN_KEY] }

    init {
        // keep cache updated
        scope.launch {
            tokenFlow.collect { cachedToken = it }
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = JwtHelper.getUserIdFromToken(token) as String
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun getToken(): String? = try {
        context.dataStore.data.map { it[TOKEN_KEY] }.first()
    } catch (e: IOException) {
        null
    }

    suspend fun getUserId(): String? = try {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    } catch (e: IOException) {
        null
    }

    // synchronous getter for interceptor
    fun getCachedToken(): String? = cachedToken
}
