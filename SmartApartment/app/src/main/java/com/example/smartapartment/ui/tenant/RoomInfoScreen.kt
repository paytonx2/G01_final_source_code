package com.example.smartapartment.ui.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInfoScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: TenantViewModel = viewModel()
) {
    val userId = sessionManager.getUserId()
    val roomState by viewModel.roomState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMyRoom(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Room Info") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = roomState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "You are not assigned to any room.")
                        }
                    } else {
                        val room = state.data.first()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Room ${room.room_number}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                InfoRow("Floor", "${room.floor}")
                                InfoRow("Status", room.status.uppercase())
                                InfoRow("Price", "$${room.price}/Month")
                                InfoRow("Move-in Date", room.move_in_date.substringBefore("T"))
                                if (room.description != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Description:", fontWeight = FontWeight.Bold)
                                    Text(text = room.description)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
