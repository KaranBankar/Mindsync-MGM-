package com.koshpal.studentapp

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiApiService {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    companion object {
        private const val TAG = "GeminiApiService"
        private const val API_KEY = "AIzaSyBNPtvhBsk2FJ4e95wgvI4q0u5q2eG1uzk"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
        
        // System prompt for the chatbot
        private const val SYSTEM_CONTEXT = """
            You are an AI assistant for StudentApp, a platform designed to help students with their academic and personal queries. 
            You should be helpful, friendly, and professional. Focus on providing accurate information about:
            - Academic guidance and study tips
            - Career counseling and advice
            - Mental health and wellness support
            - General student life questions
            - Course and subject-related queries
            
            Keep your responses concise but informative. If you're unsure about something, acknowledge it and suggest consulting with human counselors through the app's booking system.
            Always maintain a supportive and encouraging tone.
        """
    }
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending message to Gemini: $userMessage")
            
            val requestBody = createRequestBody(userMessage)
            val request = Request.Builder()
                .url("$BASE_URL?key=$API_KEY")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            Log.d(TAG, "Making request to: $BASE_URL?key=***")
            
            val response = client.newCall(request).execute()
            
            Log.d(TAG, "Response code: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Gemini response: $responseBody")
                
                if (responseBody != null) {
                    val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                    val botMessage = extractMessageFromResponse(geminiResponse)
                    
                    if (botMessage.isNotEmpty()) {
                        Log.d(TAG, "Extracted bot message: $botMessage")
                        Result.success(botMessage)
                    } else {
                        Log.e(TAG, "Empty response from Gemini")
                        Result.failure(Exception("Empty response from AI"))
                    }
                } else {
                    Log.e(TAG, "Null response body")
                    Result.failure(Exception("No response from AI"))
                }
            } else {
                val errorBody = response.body?.string()
                Log.e(TAG, "API call failed: ${response.code} - ${response.message}")
                Log.e(TAG, "Error body: $errorBody")
                Result.failure(Exception("API call failed: ${response.code} - $errorBody"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    private fun createRequestBody(userMessage: String): RequestBody {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "$SYSTEM_CONTEXT\n\nUser: $userMessage")
                    )
                )
            )
        )
        
        val json = gson.toJson(request)
        Log.d(TAG, "Request JSON: $json")
        
        return json.toRequestBody("application/json".toMediaType())
    }
    
    private fun extractMessageFromResponse(response: GeminiResponse): String {
        return try {
            response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim() ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting message from response", e)
            ""
        }
    }
}
