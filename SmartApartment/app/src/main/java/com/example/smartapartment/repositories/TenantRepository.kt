package com.example.smartapartment.repositories

import com.example.smartapartment.api.RetrofitClient
import com.example.smartapartment.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class TenantRepository {
    private val api = RetrofitClient.api

    suspend fun getMyRoom(userId: Int): Response<List<MyRoom>> {
        return api.getMyRoom(userId)
    }

    suspend fun getMyInvoices(userId: Int): Response<List<Invoice>> {
        return api.getMyInvoices(userId)
    }

    suspend fun uploadSlip(invoiceId: Int, userId: Int, amount: Double, imagePart: MultipartBody.Part): Response<GenericResponse> {
        val iId = invoiceId.toString().toRequestBody()
        val uId = userId.toString().toRequestBody()
        val amt = amount.toString().toRequestBody()
        return api.uploadSlip(iId, uId, amt, imagePart)
    }

    suspend fun getAnnouncements(): Response<List<Announcement>> {
        return api.getAnnouncements()
    }

    suspend fun submitMaintenanceRequest(roomId: Int, userId: Int, title: String, description: String, image: MultipartBody.Part?): Response<GenericResponse> {
        val roomIdBody = roomId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        return api.createMaintenanceRequest(roomIdBody, userIdBody, titleBody, descriptionBody, image)
    }
}
