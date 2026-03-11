package com.example.smartapartment.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMaintenanceScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val maintenanceState by viewModel.maintenanceState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMaintenanceRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Requests") },
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
            when (val state = maintenanceState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("No maintenance requests found.", modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        LazyColumn {
                            items(state.data) { request ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Room ${request.room_id}", fontWeight = FontWeight.Bold)
                                        Text(text = "Issue: ${request.title}", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = request.description)

                                        if (!request.image.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            AsyncImage(
                                                model = "${Constants.BASE_URL}uploads/${request.image}",
                                                contentDescription = "Maintenance Image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val statusColor = when (request.status) {
                                                "pending" -> Color(0xFFFF9800)
                                                "in_progress" -> Color.Blue
                                                "done" -> Color(0xFF4CAF50)
                                                else -> Color.Gray
                                            }
                                            Text(text = "Status: ${request.status.uppercase()}", color = statusColor, fontWeight = FontWeight.Bold)

                                            Row {
                                                if (request.status == "pending") {
                                                    Button(onClick = { viewModel.updateMaintenanceStatus(request.request_id, "in_progress") }) {
                                                        Text("Start processing")
                                                    }
                                                } else if (request.status == "in_progress") {
                                                    Button(onClick = { viewModel.updateMaintenanceStatus(request.request_id, "done") }) {
                                                        Text("Mark Done")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
