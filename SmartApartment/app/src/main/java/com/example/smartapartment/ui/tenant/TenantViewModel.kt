package com.example.smartapartment.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartapartment.models.Announcement
import com.example.smartapartment.models.Invoice
import com.example.smartapartment.models.MyRoom
import com.example.smartapartment.repositories.TenantRepository
import com.example.smartapartment.ui.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TenantViewModel : ViewModel() {
    private val repository = TenantRepository()

    private val _roomState = MutableStateFlow<UiState<List<MyRoom>>>(UiState.Loading(true))
    val roomState: StateFlow<UiState<List<MyRoom>>> = _roomState

    private val _invoiceState = MutableStateFlow<UiState<List<Invoice>>>(UiState.Loading(true))
    val invoiceState: StateFlow<UiState<List<Invoice>>> = _invoiceState

    private val _announcementsState = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading(true))
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    private val _uploadState = MutableStateFlow<UiState<String>?>(null)
    val uploadState: StateFlow<UiState<String>?> = _uploadState

    fun fetchMyRoom(userId: Int) {
        viewModelScope.launch {
            _roomState.value = UiState.Loading(true)
            try {
                val response = repository.getMyRoom(userId)
                if (response.isSuccessful && response.body() != null) {
                    _roomState.value = UiState.Success(response.body()!!)
                } else {
                    _roomState.value = UiState.Error("Failed to fetch room")
                }
            } catch (e: Exception) {
                _roomState.value = UiState.Error(e.message ?: "Error fetching room")
            } finally {
                // Ensure loading is false, handled by specific states above if not Error/Success
            }
        }
    }

    fun fetchMyInvoices(userId: Int) {
        viewModelScope.launch {
            _invoiceState.value = UiState.Loading(true)
            try {
                val response = repository.getMyInvoices(userId)
                if (response.isSuccessful && response.body() != null) {
                    _invoiceState.value = UiState.Success(response.body()!!)
                } else {
                    _invoiceState.value = UiState.Error("Failed to fetch invoices")
                }
            } catch (e: Exception) {
                _invoiceState.value = UiState.Error(e.message ?: "Error fetching invoices")
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

    fun uploadSlip(invoiceId: Int, userId: Int, amount: Double, file: File) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading(true)
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("slip_image", file.name, requestFile)
                
                val response = repository.uploadSlip(invoiceId, userId, amount, body)
                if (response.isSuccessful) {
                    _uploadState.value = UiState.Success("Upload successful")
                    fetchMyInvoices(userId) // Refresh invoices
                } else {
                    _uploadState.value = UiState.Error("Upload failed")
                }
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = null
    }

    fun submitMaintenanceRequest(roomId: Int, userId: Int, title: String, description: String, file: File?) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading(true) // Reuse uploadState for generic submissions
            try {
                var body: MultipartBody.Part? = null
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val response = repository.submitMaintenanceRequest(roomId, userId, title, description, body)
                if (response.isSuccessful) {
                    _uploadState.value = UiState.Success("Maintenance request submitted successfully")
                } else {
                    _uploadState.value = UiState.Error("Submission failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}
