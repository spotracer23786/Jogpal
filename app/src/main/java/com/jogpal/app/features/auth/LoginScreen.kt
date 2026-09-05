package com.jogpal.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory()),
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess((uiState as AuthUiState.Success).uid)
        }
    }

    Scaffold(
        containerColor = JogpalBackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JogpalBackgroundDark)
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Top Shield/Security Badge Icon (from Ref UI)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(JogpalPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, JogpalPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = JogpalPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JogpalOnBackgroundDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Log in to your account to continue.",
                    fontSize = 14.sp,
                    color = JogpalMutedTextDark
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Label & Input Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JogpalOnBackgroundDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            localError = null
                            viewModel.resetState()
                        },
                        placeholder = { Text("Enter your email", color = JogpalMutedTextDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JogpalPrimary,
                            unfocusedBorderColor = JogpalCardBorderDark,
                            focusedContainerColor = JogpalSurfaceDark,
                            unfocusedContainerColor = JogpalSurfaceDark,
                            focusedTextColor = JogpalOnBackgroundDark,
                            unfocusedTextColor = JogpalOnBackgroundDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Password Label & Input Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JogpalOnBackgroundDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            localError = null
                            viewModel.resetState()
                        },
                        placeholder = { Text("Enter your password", color = JogpalMutedTextDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JogpalPrimary,
                            unfocusedBorderColor = JogpalCardBorderDark,
                            focusedContainerColor = JogpalSurfaceDark,
                            unfocusedContainerColor = JogpalSurfaceDark,
                            focusedTextColor = JogpalOnBackgroundDark,
                            unfocusedTextColor = JogpalOnBackgroundDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Remember Me + Forgot Password Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = JogpalPrimary,
                                uncheckedColor = JogpalMutedTextDark,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(
                            text = "Remember me",
                            fontSize = 13.sp,
                            color = JogpalMutedTextDark
                        )
                    }

                    Text(
                        text = "Forgot Password?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = JogpalPrimary,
                        modifier = Modifier.clickable { /* Reset password flow */ }
                    )
                }

                val currentError = localError ?: (uiState as? AuthUiState.Error)?.message
                if (currentError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Smooth Lime Action Button (Sign In / Create Account)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(JogpalPrimary)
                        .clickable(enabled = uiState !is AuthUiState.Loading) {
                            if (email.isBlank() || password.isBlank()) {
                                localError = "Please fill in all fields"
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                localError = "Please enter a valid email address"
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // "Or" Divider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = JogpalCardBorderDark
                    )
                    Text(
                        text = "Or",
                        fontSize = 13.sp,
                        color = JogpalMutedTextDark,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = JogpalCardBorderDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Social Media Buttons Row (Google, Apple, Facebook)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(JogpalSurfaceDark)
                            .border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) {
                                viewModel.login("user@jogpal.com", "password123")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                    }

                    // Apple Circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(JogpalSurfaceDark)
                            .border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) {
                                viewModel.login("user@jogpal.com", "password123")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JogpalOnBackgroundDark)
                    }

                    // Facebook Circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(JogpalSurfaceDark)
                            .border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) {
                                viewModel.login("user@jogpal.com", "password123")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1877F2))
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Toggle between Sign In / Sign Up
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 14.sp,
                        color = JogpalMutedTextDark
                    )
                    Text(
                        text = "Sign Up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JogpalPrimary,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
            }

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = JogpalPrimary
                )
            }
        }
    }
}

