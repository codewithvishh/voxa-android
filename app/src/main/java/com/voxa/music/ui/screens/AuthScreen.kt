package com.voxa.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxa.music.data.local.TokenManager
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.AuthRepository
import com.voxa.music.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }
    val authRepository = remember { AuthRepository(ApiClient.getService(context), tokenManager) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun sendOtp() {
        if (email.isBlank() || !email.contains("@")) {
            statusMessage = "Please enter a valid email address"
            isError = true
            return
        }
        isLoading = true
        isError = false
        statusMessage = null
        scope.launch {
            val result = authRepository.sendOtp(email.trim())
            isLoading = false
            result.onSuccess {
                isOtpSent = true
                statusMessage = "A 6-digit code has been dispatched to your email."
            }.onFailure {
                isError = true
                statusMessage = it.message ?: "Failed to send OTP code"
            }
        }
    }

    fun verifyOtp() {
        if (otpCode.trim().length < 6) {
            statusMessage = "Please enter the full 6-digit code"
            isError = true
            return
        }
        isLoading = true
        isError = false
        scope.launch {
            val result = authRepository.verifyOtp(email.trim(), otpCode.trim())
            isLoading = false
            result.onSuccess {
                onAuthSuccess()
            }.onFailure {
                isError = true
                statusMessage = it.message ?: "Verification failed"
            }
        }
    }

    Scaffold(
        containerColor = VoxaBackground,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = VoxaTextPrimary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("VOXA ACCOUNT", color = VoxaPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sign in to your library", color = VoxaTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Access personal playlists, liked songs, and synced history across devices.", color = VoxaTextSecondary, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = VoxaPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isOtpSent,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VoxaPrimary,
                    unfocusedBorderColor = VoxaDivider,
                    focusedTextColor = VoxaTextPrimary,
                    unfocusedTextColor = VoxaTextPrimary,
                    focusedLabelColor = VoxaPrimary,
                    unfocusedLabelColor = VoxaTextSecondary
                )
            )

            // OTP Field if code is sent
            if (isOtpSent) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("6-Digit OTP Code") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Code", tint = VoxaPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoxaPrimary,
                        unfocusedBorderColor = VoxaDivider,
                        focusedTextColor = VoxaTextPrimary,
                        unfocusedTextColor = VoxaTextPrimary,
                        focusedLabelColor = VoxaPrimary,
                        unfocusedLabelColor = VoxaTextSecondary
                    )
                )
            }

            // Status message
            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = statusMessage!!,
                    color = if (isError) VoxaPrimary else VoxaTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { if (isOtpSent) verifyOtp() else sendOtp() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VoxaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = VoxaTextPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isOtpSent) "Verify & Log In" else "Send Login Code",
                        color = VoxaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            if (isOtpSent) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        isOtpSent = false
                        otpCode = ""
                        statusMessage = null
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Change email address", color = VoxaTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
