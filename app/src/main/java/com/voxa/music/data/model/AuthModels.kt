package com.voxa.music.data.model

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("email") val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("message") val message: String? = null
) {
    val effectiveToken: String?
        get() = accessToken ?: token
}

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("role") val role: String? = "USER"
)
