package com.example.financemanagement.utils

import android.util.Base64
import org.json.JSONObject

object JwtHelper {
    fun getUserIdFromToken(token: String): String? {
        return try {
            // JWT format: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            // Decode payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            
            // Parse JSON and extract userId
            val json = JSONObject(decodedString)
            
            // Try different possible field names for userId
            when {
                json.has("sub") -> json.getString("sub") // Standard JWT subject claim
                json.has("userId") -> json.getString("userId")
                json.has("id") -> json.getString("id")
                json.has("nameid") -> json.getString("nameid") // ASP.NET Identity uses this
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.e("JwtHelper", "Error decoding JWT token", e)
            null
        }
    }
    
    fun getEmailFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            
            val json = JSONObject(decodedString)
            
            when {
                json.has("email") -> json.getString("email")
                json.has("unique_name") -> json.getString("unique_name")
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.e("JwtHelper", "Error decoding JWT token", e)
            null
        }
    }
}
