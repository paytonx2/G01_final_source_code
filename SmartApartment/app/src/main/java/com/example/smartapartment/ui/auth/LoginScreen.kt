package com.example.smartapartment.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartapartment.models.LoginResponse
import com.example.smartapartment.utils.SessionManager

@Composable
fun LoginScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                val data = (loginState as UiState.Success<LoginResponse>).data
                sessionManager.saveUserSession(data.user.user_id, data.user.name, data.user.role)
                isLoading = false
                
                // Navigate based on role
                if (data.user.role == "admin") {
                    navController.navigate("admin_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.navigate("tenant_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            is UiState.Error -> {
                errorMessage = (loginState as UiState.Error).message
                isLoading = false
            }
            is UiState.Loading -> {
                val loadingState = loginState as UiState.Loading
                // Ignore initial false state
                if(loadingState.isLoading) isLoading = true
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SMARTDORM", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                         viewModel.login(email, password)
                    } else {
                        errorMessage = "Please enter email and password"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Don't have an account? Register")
            }
        }
    }
}
