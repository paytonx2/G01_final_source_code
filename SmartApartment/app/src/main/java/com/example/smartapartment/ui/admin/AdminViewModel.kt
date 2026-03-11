package com.example.smartapartment.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartapartment.models.AdminSummary
import com.example.smartapartment.models.Announcement
import com.example.smartapartment.models.Invoice
import com.example.smartapartment.models.Payment
import com.example.smartapartment.models.Room
import com.example.smartapartment.repositories.AdminRepository
import com.example.smartapartment.ui.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.example.smartapartment.models.MaintenanceRequest
import com.example.smartapartment.models.User

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _roomsState = MutableStateFlow<UiState<List<Room>>>(UiState.Loading(true))
    val roomsState: StateFlow<UiState<List<Room>>> = _roomsState

    private val _paymentsState = MutableStateFlow<UiState<List<Payment>>>(UiState.Loading(true))
    val paymentsState: StateFlow<UiState<List<Payment>>> = _paymentsState

    private val _summaryState = MutableStateFlow<UiState<AdminSummary>>(UiState.Loading(true))
    val summaryState: StateFlow<UiState<AdminSummary>> = _summaryState

    private val _tenantsState = MutableStateFlow<UiState<List<User>>>(UiState.Loading(true))
    val tenantsState: StateFlow<UiState<List<User>>> = _tenantsState

    private val _assignState = MutableStateFlow<UiState<String>?>(null)
    val assignState: StateFlow<UiState<String>?> = _assignState

    private val _billingState = MutableStateFlow<UiState<String>?>(null)
    val billingState: StateFlow<UiState<String>?> = _billingState

    private val _announcementsState = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading(true))
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    private val _postAnnouncementState = MutableStateFlow<UiState<String>?>(null)
    val postAnnouncementState: StateFlow<UiState<String>?> = _postAnnouncementState

    private val _roomInvoicesState = MutableStateFlow<UiState<List<Invoice>>>(UiState.Loading(true))
    val roomInvoicesState: StateFlow<UiState<List<Invoice>>> = _roomInvoicesState

    private val _removeTenantState = MutableStateFlow<UiState<String>?>(null)
    val removeTenantState: StateFlow<UiState<String>?> = _removeTenantState

    init {
        fetchSummary()
        fetchTenants()
        fetchRooms() // เพิ่มเพื่อให้โหลดห้องตอนเริ่ม
    }

    // ── [เพิ่มใหม่] ฟังก์ชันแก้ไขข้อมูลห้อง ──
    fun updateRoom(roomId: Int, roomNumber: String, floor: Int?, price: Double?) {
        viewModelScope.launch {
            try {
                val response = repository.updateRoom(roomId, roomNumber, floor, price)
                if (response.isSuccessful) {
                    fetchRooms() // แก้เสร็จให้โหลดลิสต์ห้องใหม่ทันที
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // ── [เพิ่มใหม่] ฟังก์ชันลบห้อง ──
    fun deleteRoom(roomId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteRoom(roomId)
                if (response.isSuccessful) {
                    fetchRooms() // ลบเสร็จให้โหลดลิสต์ห้องใหม่
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    // ── ฟังก์ชันเดิมที่มีอยู่แล้ว ──

    fun fetchTenants() {
        viewModelScope.launch {
            _tenantsState.value = UiState.Loading(true)
            try {
                val response = repository.getUsers("tenant")
                if (response.isSuccessful && response.body() != null) {
                    _tenantsState.value = UiState.Success(response.body()!!)
                } else {
                    _tenantsState.value = UiState.Error("Failed to fetch tenants")
                }
            } catch (e: Exception) {
                _tenantsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun createInvoice(roomId: Int, month: String, rent: Double, water: Double, electricity: Double, other: Double) {
        viewModelScope.launch {
            _billingState.value = UiState.Loading(true)
            try {
                val response = repository.createInvoice(roomId, month, rent, water, electricity, other)
                if (response.isSuccessful) {
                    _billingState.value = UiState.Success("Invoice issued successfully")
                } else {
                    _billingState.value = UiState.Error("Failed to issue invoice")
                }
            } catch (e: Exception) {
                _billingState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetBillingState() {
        _billingState.value = null
    }

    fun assignTenant(roomId: Int, userId: Int, moveInDate: String, moveOutDate: String) {
        viewModelScope.launch {
            _assignState.value = UiState.Loading(true)
            try {
                val response = repository.assignTenant(roomId, userId, moveInDate, moveOutDate)
                if (response.isSuccessful) {
                    _assignState.value = UiState.Success("Tenant assigned successfully")
                    fetchRooms() // Refresh rooms list
                } else {
                    _assignState.value = UiState.Error("Failed to assign tenant")
                }
            } catch (e: Exception) {
                _assignState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetAssignState() {
        _assignState.value = null
    }

    fun fetchSummary() {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading(true)
            try {
                val response = repository.getAdminSummary()
                if (response.isSuccessful && response.body() != null) {
                    _summaryState.value = UiState.Success(response.body()!!)
                } else {
                    _summaryState.value = UiState.Error("Failed to fetch summary")
                }
            } catch (e: Exception) {
                _summaryState.value = UiState.Error(e.message ?: "Error")
            }
        }
    }

    fun fetchRooms() {
        viewModelScope.launch {
            _roomsState.value = UiState.Loading(true)
            try {
                val response = repository.getRooms()
                if (response.isSuccessful && response.body() != null) {
                    _roomsState.value = UiState.Success(response.body()!!)
                } else {
                    _roomsState.value = UiState.Error("Failed to fetch rooms")
                }
            } catch (e: Exception) {
                _roomsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun addRoom(roomNumber: String, floor: Int, price: Double, description: String) {
        viewModelScope.launch {
            try {
                val response = repository.addRoom(roomNumber, floor, price, description)
                if (response.isSuccessful) {
                    fetchRooms()
                }
            } catch (e: Exception) {
                // silently ignore
            }
        }
    }

    fun fetchPayments() {
        viewModelScope.launch {
            _paymentsState.value = UiState.Loading(true)
            try {
                val response = repository.getAllPayments()
                if (response.isSuccessful && response.body() != null) {
                    _paymentsState.value = UiState.Success(response.body()!!)
                } else {
                    _paymentsState.value = UiState.Error("Failed to fetch payments")
                }
            } catch (e: Exception) {
                _paymentsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun approvePayment(paymentId: Int, status: String) {
        viewModelScope.launch {
            try {
                Log.d("AdminPayment", "กำลังส่งไปที่ Server: ID=$paymentId, Status=$status")
                val response = repository.updatePaymentStatus(paymentId, status)

                if (response.isSuccessful) {
                    Log.d("AdminPayment", "อัปเดตสำเร็จ! กำลังโหลดข้อมูลใหม่...")
                    fetchPayments() // Refresh the list
                } else {
                    // ถ้าฝั่ง Server ตอบกลับมาเป็น Error (เช่น 404, 500)
                    Log.e("AdminPayment", "Server Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // ถ้าแอปพังก่อนไปถึง Server (เช่น เน็ตหลุด, แปลงข้อมูลผิด)
                Log.e("AdminPayment", "App Exception: ${e.message}")
            }
        }
    }

    private val _maintenanceState = MutableStateFlow<UiState<List<MaintenanceRequest>>>(UiState.Loading(true))
    val maintenanceState: StateFlow<UiState<List<MaintenanceRequest>>> = _maintenanceState

    fun fetchMaintenanceRequests() {
        viewModelScope.launch {
            _maintenanceState.value = UiState.Loading(true)
            try {
                val response = repository.getAllMaintenanceRequests()
                if (response.isSuccessful && response.body() != null) {
                    _maintenanceState.value = UiState.Success(response.body()!!)
                } else {
                    _maintenanceState.value = UiState.Error("Failed to fetch maintenance requests")
                }
            } catch (e: Exception) {
                _maintenanceState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun updateMaintenanceStatus(requestId: Int, status: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateMaintenanceStatus(requestId, status)
                if (response.isSuccessful) {
                    fetchMaintenanceRequests() // Refresh the list
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchAnnouncements() {
        viewModelScope.launch {
            _announcementsState.value = UiState.Loading(true)
            try {
                val response = repository.getAnnouncements()
                if (response.isSuccessful && response.body() != null) {
                    _announcementsState.value = UiState.Success(response.body()!!)
                } else {
                    _announcementsState.value = UiState.Error("Failed to fetch announcements")
                }
            } catch (e: Exception) {
                _announcementsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun postAnnouncement(title: String, content: String, adminId: Int) {
        viewModelScope.launch {
            _postAnnouncementState.value = UiState.Loading(true)
            try {
                val response = repository.postAnnouncement(title, content, adminId)
                if (response.isSuccessful) {
                    _postAnnouncementState.value = UiState.Success("Announcement posted")
                } else {
                    _postAnnouncementState.value = UiState.Error("Failed to post announcement")
                }
            } catch (e: Exception) {
                _postAnnouncementState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetPostAnnouncementState() {
        _postAnnouncementState.value = null
    }

    fun fetchRoomInvoices(roomId: Int) {
        viewModelScope.launch {
            _roomInvoicesState.value = UiState.Loading(true)
            try {
                val response = repository.getRoomInvoices(roomId)
                if (response.isSuccessful && response.body() != null) {
                    _roomInvoicesState.value = UiState.Success(response.body()!!)
                } else {
                    _roomInvoicesState.value = UiState.Error("Failed to load invoice history")
                }
            } catch (e: Exception) {
                _roomInvoicesState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun removeTenant(roomId: Int) {
        viewModelScope.launch {
            _removeTenantState.value = UiState.Loading(true)
            try {
                val response = repository.removeTenant(roomId)
                if (response.isSuccessful) {
                    _removeTenantState.value = UiState.Success("Tenant removed successfully")
                    fetchRooms() // Refresh room list
                } else {
                    _removeTenantState.value = UiState.Error("Failed to remove tenant")
                }
            } catch (e: Exception) {
                _removeTenantState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetRemoveTenantState() {
        _removeTenantState.value = null
    }
}