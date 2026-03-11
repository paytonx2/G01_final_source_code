package com.example.smartapartment.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartapartment.models.Room
import com.example.smartapartment.models.User
import com.example.smartapartment.ui.auth.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoomDetailScreen(
    roomId: Int,
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val roomsState by viewModel.roomsState.collectAsState()
    val tenantsState by viewModel.tenantsState.collectAsState()
    val assignState by viewModel.assignState.collectAsState()
    val billingState by viewModel.billingState.collectAsState()
    val roomInvoicesState by viewModel.roomInvoicesState.collectAsState()
    val removeTenantState by viewModel.removeTenantState.collectAsState()

    // Find this room from the already-loaded list
    val room = remember(roomsState) {
        if (roomsState is UiState.Success) {
            (roomsState as UiState.Success<List<Room>>).data.find { it.room_id == roomId }
        } else null
    }

    // --- [เพิ่มใหม่] State สำหรับการแก้ไขและลบห้อง ---
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmRoom by remember { mutableStateOf(false) }
    var editRoomNumber by remember { mutableStateOf("") }
    var editFloor by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }

    // Assign form state (ของเดิม)
    var selectedTenant by remember { mutableStateOf<User?>(null) }
    var moveInDate by remember { mutableStateOf("") }
    var moveOutDate by remember { mutableStateOf("") }
    var expandDropdown by remember { mutableStateOf(false) }

    // Date Picker States (ของเดิม)
    var showInDatePicker by remember { mutableStateOf(false) }
    val inDatePickerState = rememberDatePickerState()
    var showOutDatePicker by remember { mutableStateOf(false) }
    val outDatePickerState = rememberDatePickerState()
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Billing form state (ของเดิม)
    var billingMonth by remember { mutableStateOf("") }
    var waterUnits by remember { mutableStateOf("") }
    var electricUnits by remember { mutableStateOf("") }

    // Dialogs (ของเดิม)
    var showRemoveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        viewModel.fetchRoomInvoices(roomId)
        viewModel.fetchRooms()
        viewModel.fetchTenants()
    }

    // [เพิ่มใหม่] เมื่อดึงข้อมูลห้องมาได้ ให้เตรียมค่าใส่ฟอร์มแก้ไข
    LaunchedEffect(room) {
        room?.let {
            editRoomNumber = it.room_number
            editFloor = it.floor.toString()
            editPrice = it.price.toString()
        }
    }

    // Navigation and Success Handlers (ของเดิม)
    LaunchedEffect(removeTenantState) {
        if (removeTenantState is UiState.Success) {
            viewModel.resetRemoveTenantState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(billingState) {
        if (billingState is UiState.Success) {
            billingMonth = ""; waterUnits = ""; electricUnits = ""
            viewModel.resetBillingState()
            viewModel.fetchRoomInvoices(roomId)
        }
    }

    LaunchedEffect(assignState) {
        if (assignState is UiState.Success) {
            viewModel.resetAssignState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room ${room?.room_number ?: roomId} — Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // --- [เพิ่มใหม่] ปุ่ม Edit และ Delete บน TopBar ---
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Room", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirmRoom = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Room", tint = Color.White)
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
        if (room == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Room Info Card (ของเดิม) ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Room Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Room Number"); Text(room.room_number, fontWeight = FontWeight.SemiBold)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Floor"); Text("${room.floor}")
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Monthly Rent"); Text("${room.price} THB")
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Status")
                            val statusColor = when (room.status) {
                                "available" -> Color(0xFF4CAF50)
                                "occupied" -> Color(0xFF1565C0)
                                else -> Color(0xFFFF9800)
                            }
                            Text(room.status.uppercase(), color = statusColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── OCCUPIED Section (ของเดิม) ──
            if (room.status == "occupied") {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tenant Management", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { showRemoveConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Remove Tenant from Room", color = Color.White) }
                        }
                    }
                }

                item {
                    val rent = room.price
                    val waterCost = (waterUnits.toDoubleOrNull() ?: 0.0) * 30.0
                    val electricCost = (electricUnits.toDoubleOrNull() ?: 0.0) * 8.0
                    val totalCost = rent + waterCost + electricCost

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Issue New Bill", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = billingMonth, onValueChange = { billingMonth = it }, label = { Text("Billing Month (e.g. 03/2026)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = waterUnits, onValueChange = { waterUnits = it }, label = { Text("Water Units (30 THB/unit)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = electricUnits, onValueChange = { electricUnits = it }, label = { Text("Electric Units (8 THB/unit)") }, modifier = Modifier.fillMaxWidth())

                            if (waterUnits.isNotEmpty() || electricUnits.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Total: $totalCost THB", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.createInvoice(room.room_id, billingMonth, rent, waterCost, electricCost, 0.0) },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                enabled = billingMonth.isNotEmpty() && billingState !is UiState.Loading
                            ) { Text("Issue Bill") }
                        }
                    }
                }
            }

            // ── AVAILABLE Section (ของเดิม) ──
            if (room.status == "available") {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Assign Tenant", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = selectedTenant?.name ?: "Select Tenant", onValueChange = {}, readOnly = true, label = { Text("Tenant") }, modifier = Modifier.fillMaxWidth())
                                Box(Modifier.matchParentSize().clickable { expandDropdown = true })
                                DropdownMenu(expanded = expandDropdown, onDismissRequest = { expandDropdown = false }) {
                                    if (tenantsState is UiState.Success) {
                                        (tenantsState as UiState.Success<List<User>>).data.forEach { tenant ->
                                            DropdownMenuItem(text = { Text(tenant.name) }, onClick = { selectedTenant = tenant; expandDropdown = false })
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = moveInDate, onValueChange = {}, label = { Text("Move In Date") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                                Box(Modifier.matchParentSize().clickable { showInDatePicker = true })
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = moveOutDate, onValueChange = {}, label = { Text("Move Out Date") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                                Box(Modifier.matchParentSize().clickable { showOutDatePicker = true })
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { if (selectedTenant != null && moveInDate.isNotEmpty()) viewModel.assignTenant(room.room_id, selectedTenant!!.user_id, moveInDate, moveOutDate) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = assignState !is UiState.Loading
                            ) { Text("Assign Tenant") }
                        }
                    }
                }
            }
        }
    }

    // --- [เพิ่มใหม่] Edit Room Dialog ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Room Info") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editRoomNumber, onValueChange = { editRoomNumber = it }, label = { Text("Room Number") })
                    OutlinedTextField(value = editFloor, onValueChange = { editFloor = it }, label = { Text("Floor") })
                    OutlinedTextField(value = editPrice, onValueChange = { editPrice = it }, label = { Text("Price (Monthly)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val floorInt = editFloor.toIntOrNull() ?: room?.floor
                    val priceDouble = editPrice.toDoubleOrNull() ?: room?.price
                    viewModel.updateRoom(roomId, editRoomNumber, floorInt, priceDouble)
                    showEditDialog = false
                }) { Text("Save Changes") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    // --- [เพิ่มใหม่] Delete Room Confirmation Dialog ---
    if (showDeleteConfirmRoom) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmRoom = false },
            title = { Text("Delete Room?") },
            text = { Text("Are you sure you want to delete room ${room?.room_number}? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteRoom(roomId) { success ->
                        if (success) navController.popBackStack()
                    }
                    showDeleteConfirmRoom = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmRoom = false }) { Text("Cancel") } }
        )
    }

    // --- Date Picker Dialogs (ของเดิม) ---
    if (showInDatePicker) {
        DatePickerDialog(onDismissRequest = { showInDatePicker = false }, confirmButton = { TextButton(onClick = { inDatePickerState.selectedDateMillis?.let { moveInDate = dateFormatter.format(Date(it)) }; showInDatePicker = false }) { Text("OK") } }) { DatePicker(state = inDatePickerState) }
    }
    if (showOutDatePicker) {
        DatePickerDialog(onDismissRequest = { showOutDatePicker = false }, confirmButton = { TextButton(onClick = { outDatePickerState.selectedDateMillis?.let { moveOutDate = dateFormatter.format(Date(it)) }; showOutDatePicker = false }) { Text("OK") } }) { DatePicker(state = outDatePickerState) }
    }

    // Remove Confirmation Dialog (ของเดิม)
    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Confirm Remove") },
            text = { Text("Remove tenant from Room ${room?.room_number}?") },
            confirmButton = {
                Button(onClick = { showRemoveConfirm = false; viewModel.removeTenant(roomId) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Remove", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showRemoveConfirm = false }) { Text("Cancel") } }
        )
    }
}