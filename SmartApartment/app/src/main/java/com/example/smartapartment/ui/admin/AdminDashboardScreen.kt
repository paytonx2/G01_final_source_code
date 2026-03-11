package com.example.smartapartment.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.ui.tenant.DashboardCard
import com.example.smartapartment.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val userName = sessionManager.getUserName() ?: "Admin"
    val summaryState by viewModel.summaryState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        sessionManager.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Welcome, $userName", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Setup
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DashboardCard(title = "Manage Rooms", icon = Icons.Filled.Home) {
                        navController.navigate("manage_rooms")
                    }
                    DashboardCard(title = "Approve Slips", icon = Icons.Filled.ShoppingCart) {
                        navController.navigate("manage_payments")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DashboardCard(title = "Announcements", icon = Icons.Filled.Build) {
                        navController.navigate("admin_announcements")
                    }
                    DashboardCard(title = "Repairs", icon = Icons.Filled.Build) {
                        navController.navigate("admin_maintenance")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Grade A Feature: System Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "System Summary", style = MaterialTheme.typography.titleLarge)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    when (val state = summaryState) {
                        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        is UiState.Error -> Text(text = (state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                        is UiState.Success -> {
                            val summary = (state as UiState.Success<com.example.smartapartment.models.AdminSummary>).data
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Rooms: ${summary.total_rooms}")
                                Text("Available: ${summary.available_rooms}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pending Slips: ${summary.pending_slips}")
                                Text("Repairs: ${summary.pending_repairs}")
                            }
                        }
                    }
                }
            }
        }
    }
}
