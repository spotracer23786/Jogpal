package com.jogpal.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
    var localError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess((uiState as AuthUiState.Success).uid)
        }
    }

    Scaffold(
        containerColor = JogpalBackgroundLight,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, JogpalCardBorderLight, CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JogpalOnBackgroundLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JogpalBackgroundLight)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JogpalBackgroundLight)
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                JogpalLogo()

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = JogpalOnBackgroundLight
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Log in to join your running partner network",
                    fontSize = 14.sp,
                    color = JogpalMutedTextLight
                )
                
                Spacer(modifier = Modifier.height(36.dp))

                // Elevated Glass Login Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color(0x1F000000))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.dp, JogpalCardBorderLight, RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                localError = null
                                viewModel.resetState()
                            },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = JogpalPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            singleLine = true,
                            enabled = uiState !is AuthUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JogpalPrimary,
                                unfocusedBorderColor = JogpalCardBorderLight
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                localError = null
                                viewModel.resetState()
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = JogpalPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            singleLine = true,
                            enabled = uiState !is AuthUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JogpalPrimary,
                                unfocusedBorderColor = JogpalCardBorderLight
                            )
                        )
                        
                        val currentError = localError ?: (uiState as? AuthUiState.Error)?.message
                        if (currentError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Futuristic Emerald CTA Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = JogpalPrimary)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(JogpalPrimary, JogpalSecondary)
                                    )
                                )
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
                                text = "Log In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = onNavigateToSignUp,
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text(
                        text = "Don't have an account? Create one",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JogpalPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
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

