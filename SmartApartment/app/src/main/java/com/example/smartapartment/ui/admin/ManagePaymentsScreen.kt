package com.example.smartapartment.ui.admin

import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.smartapartment.models.Payment
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePaymentsScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val paymentsState by viewModel.paymentsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPayments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approve Payments") },
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
            when (val state = paymentsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No payment records found.")
                        }
                    } else {
                        LazyColumn {
                            items(state.data) { payment ->
                                PaymentApprovalCard(payment = payment, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentApprovalCard(payment: Payment, viewModel: AdminViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Tenant: ${payment.tenant_name}", fontWeight = FontWeight.Bold)
                Text(text = "$${payment.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "For Month: ${payment.month} | Total Bill: $${payment.invoice_total}", style = MaterialTheme.typography.bodyMedium)
            
            val statusColor = when (payment.status) {
                "approved" -> Color(0xFF4CAF50)
                "rejected" -> Color(0xFFF44336)
                else -> Color(0xFFFF9800)
            }
            Text(text = "Status: ${payment.status.uppercase()}", color = statusColor, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show slip image using Coil
            Image(
                painter = rememberAsyncImagePainter("${Constants.BASE_URL}uploads/${payment.slip_image}"),
                contentDescription = "Payment Slip",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )
            
            if (payment.status == "pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.approvePayment(payment.payment_id, "approved") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Approve")
                    }
                    Button(
                        onClick = { viewModel.approvePayment(payment.payment_id, "rejected") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}
