package com.example.smartapartment.ui.tenant

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartapartment.models.Invoice
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: TenantViewModel = viewModel()
) {
    val userId = sessionManager.getUserId()
    val invoiceState by viewModel.invoiceState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-fetch every time this screen resumes (e.g. after returning from UploadSlipScreen)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchMyInvoices(userId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Invoices") },
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
            when (val state = invoiceState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No invoices found.")
                        }
                    } else {
                        LazyColumn {
                            items(state.data) { invoice ->
                                InvoiceCard(invoice = invoice, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Billing Month: ${invoice.month}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Rent", "$${invoice.rent}")
            InfoRow("Water", "$${invoice.water}")
            InfoRow("Electricity", "$${invoice.electricity}")
            InfoRow("Other", "$${invoice.other}")
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow("Total", "$${invoice.total}")
            Spacer(modifier = Modifier.height(8.dp))
            
            val statusColor = when (invoice.status) {
                "paid" -> Color(0xFF4CAF50)
                "pending" -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            Text(text = "Status: ${invoice.status.uppercase()}", color = statusColor, fontWeight = FontWeight.Bold)
            
            if (invoice.status == "unpaid") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        // Note: For Grade A, we pass arguments to another screen to upload slip
                        navController.navigate("upload_slip/${invoice.invoice_id}/${invoice.total}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay Now / Upload Slip")
                }
            }
        }
    }
}
