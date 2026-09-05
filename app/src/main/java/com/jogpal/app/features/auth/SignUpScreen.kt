package com.jogpal.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory()),
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignUpSuccess((uiState as AuthUiState.Success).uid)
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

                // Profile Badge Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(JogpalPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, JogpalPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("J", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JogpalPrimary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Create Account",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JogpalOnBackgroundDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign up to get started with your community.",
                    fontSize = 14.sp,
                    color = JogpalMutedTextDark
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Full Name Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Full Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = JogpalOnBackgroundDark, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; localError = null; viewModel.resetState() },
                        placeholder = { Text("Enter your name", color = JogpalMutedTextDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JogpalPrimary, unfocusedBorderColor = JogpalCardBorderDark,
                            focusedContainerColor = JogpalSurfaceDark, unfocusedContainerColor = JogpalSurfaceDark,
                            focusedTextColor = JogpalOnBackgroundDark, unfocusedTextColor = JogpalOnBackgroundDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Email", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = JogpalOnBackgroundDark, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localError = null; viewModel.resetState() },
                        placeholder = { Text("Enter your email", color = JogpalMutedTextDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JogpalPrimary, unfocusedBorderColor = JogpalCardBorderDark,
                            focusedContainerColor = JogpalSurfaceDark, unfocusedContainerColor = JogpalSurfaceDark,
                            focusedTextColor = JogpalOnBackgroundDark, unfocusedTextColor = JogpalOnBackgroundDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = JogpalOnBackgroundDark, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localError = null; viewModel.resetState() },
                        placeholder = { Text("Create a password", color = JogpalMutedTextDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JogpalPrimary, unfocusedBorderColor = JogpalCardBorderDark,
                            focusedContainerColor = JogpalSurfaceDark, unfocusedContainerColor = JogpalSurfaceDark,
                            focusedTextColor = JogpalOnBackgroundDark, unfocusedTextColor = JogpalOnBackgroundDark
                        )
                    )
                }

                val currentError = localError ?: (uiState as? AuthUiState.Error)?.message
                if (currentError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = currentError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Smooth Lime Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(JogpalPrimary)
                        .clickable(enabled = uiState !is AuthUiState.Loading) {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                localError = "Please fill in all fields"
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                localError = "Please enter a valid email address"
                            } else if (password.length < 6) {
                                localError = "Password must be at least 6 characters"
                            } else {
                                viewModel.signUp(name, email, password)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // "Or" Divider
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = JogpalCardBorderDark)
                    Text("Or", fontSize = 13.sp, color = JogpalMutedTextDark, modifier = Modifier.padding(horizontal = 16.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = JogpalCardBorderDark)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Social Media Row (Google, Apple, Facebook)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(JogpalSurfaceDark).border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) { viewModel.signUp("Google User", "google@jogpal.com", "password123") },
                        contentAlignment = Alignment.Center
                    ) { Text("G", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4)) }

                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(JogpalSurfaceDark).border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) { viewModel.signUp("Apple User", "apple@jogpal.com", "password123") },
                        contentAlignment = Alignment.Center
                    ) { Text("", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JogpalOnBackgroundDark) }

                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(JogpalSurfaceDark).border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable(enabled = uiState !is AuthUiState.Loading) { viewModel.signUp("Facebook User", "fb@jogpal.com", "password123") },
                        contentAlignment = Alignment.Center
                    ) { Text("f", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1877F2)) }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                    Text("Already have an account? ", fontSize = 14.sp, color = JogpalMutedTextDark)
                    Text("Sign In", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JogpalPrimary, modifier = Modifier.clickable { onNavigateToLogin() })
                }
            }

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(48.dp), color = JogpalPrimary)
            }
        }
    }
}
