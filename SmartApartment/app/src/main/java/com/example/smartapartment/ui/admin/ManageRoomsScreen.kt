package com.example.smartapartment.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoomsScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val roomsState by viewModel.roomsState.collectAsState()

    // Add Room dialog state
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var newRoomNumber by remember { mutableStateOf("") }
    var newFloor by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchRooms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Rooms") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showAddRoomDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Room", tint = Color.White)
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
            when (val state = roomsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No rooms found. Tap + to add one.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.data) { room ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("room_detail/${room.room_id}")
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "Room ${room.room_number}", fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Floor ${room.floor}  |  ${room.price} THB/month",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        val statusColor = when (room.status) {
                                            "available" -> Color(0xFF4CAF50)
                                            "occupied" -> Color(0xFF1565C0)
                                            else -> Color(0xFFFF9800)
                                        }
                                        Text(
                                            text = room.status.uppercase(),
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Room Dialog
    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Add New Room") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newRoomNumber,
                        onValueChange = { newRoomNumber = it },
                        label = { Text("Room Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFloor,
                        onValueChange = { newFloor = it },
                        label = { Text("Floor") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Monthly Rent (THB)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val floor = newFloor.toIntOrNull() ?: 1
                    val price = newPrice.toDoubleOrNull() ?: 0.0
                    if (newRoomNumber.isNotEmpty() && price > 0) {
                        viewModel.addRoom(newRoomNumber, floor, price, newDesc)
                        showAddRoomDialog = false
                        newRoomNumber = ""; newFloor = ""; newPrice = ""; newDesc = ""
                    }
                }) { Text("Add Room") }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) { Text("Cancel") }
            }
        )
    }
}
