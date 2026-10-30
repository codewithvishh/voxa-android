package com.voxa.music.data.repository

import com.voxa.music.data.local.TokenManager
import com.voxa.music.data.model.AuthResponse
import com.voxa.music.data.model.SendOtpRequest
import com.voxa.music.data.model.VerifyOtpRequest
import com.voxa.music.data.remote.VoxaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: VoxaApiService,
    private val tokenManager: TokenManager
) {
    suspend fun sendOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendOtp(SendOtpRequest(email))
            if (response.isSuccessful) {
                Result.success("OTP sent to $email")
            } else {
                Result.failure(Exception("Failed to send OTP (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, otp))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                auth.effectiveToken?.let { token ->
                    tokenManager.saveToken(token)
                    tokenManager.saveUserEmail(email)
                }
                Result.success(auth)
            } else {
                Result.failure(Exception("Invalid verification code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clear()
    }
}
