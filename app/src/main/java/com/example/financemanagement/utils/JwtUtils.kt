package com.example.financemanagement.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object JwtUtils {
    
    /**
     * Decode JWT token and extract userId from the payload
     * JWT format: header.payload.signature
     */
    fun getUserIdFromToken(token: String): String? {
        return try {
            // Split the token into parts
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("JwtUtils", "Invalid JWT token format")
                return null
            }
            
            // Decode the payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            
            // Parse JSON and extract userId (or "sub" or "id" depending on your JWT structure)
            val jsonObject = JSONObject(decodedString)
            
            // Try common claim names for user ID
            when {
                jsonObject.has("userId") -> jsonObject.getString("userId")
                jsonObject.has("sub") -> jsonObject.getString("sub")
                jsonObject.has("id") -> jsonObject.getString("id")
                jsonObject.has("nameid") -> jsonObject.getString("nameid") // Common in .NET
                else -> {
                    Log.e("JwtUtils", "No userId found in token payload: $decodedString")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("JwtUtils", "Error decoding JWT token", e)
            null
        }
    }
}
