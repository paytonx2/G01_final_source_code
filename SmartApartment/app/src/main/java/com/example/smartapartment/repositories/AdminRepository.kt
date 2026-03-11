package com.example.smartapartment.repositories

import com.example.smartapartment.api.RetrofitClient
import com.example.smartapartment.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AdminRepository {
    private val api = RetrofitClient.api

    suspend fun getRooms(): Response<List<Room>> {
        return api.getRooms()
    }

    suspend fun getAdminSummary(): Response<AdminSummary> {
        return api.getAdminSummary()
    }

    suspend fun addRoom(roomNumber: String, floor: Int, price: Double, description: String): Response<RoomResponse> {
        val req = HashMap<String, Any>()
        req["room_number"] = roomNumber
        req["floor"] = floor
        req["price"] = price
        req["description"] = description
        return api.addRoom(req)
    }

    suspend fun getUsers(role: String? = null): Response<List<User>> {
        return api.getUsers(role)
    }

    suspend fun assignTenant(roomId: Int, userId: Int, moveInDate: String, moveOutDate: String): Response<GenericResponse> {
        val req = HashMap<String, Any>()
        req["room_id"] = roomId
        req["user_id"] = userId
        req["move_in_date"] = moveInDate
        req["move_out_date"] = moveOutDate
        return api.assignTenant(req)
    }

    suspend fun updateRoomStatus(roomId: Int, status: String, description: String?): Response<GenericResponse> {
        val req = HashMap<String, String>()
        req["status"] = status
        if (description != null) req["description"] = description
        return api.updateRoomStatus(roomId, req)
    }

    suspend fun getAllPayments() = api.getAllPayments()

    suspend fun updatePaymentStatus(paymentId: Int, status: String): Response<GenericResponse> {
        val req = HashMap<String, String>()
        req["status"] = status
        return api.updatePaymentStatus(paymentId, req)
    }

    suspend fun createInvoice(roomId: Int, month: String, rent: Double, water: Double, electricity: Double, other: Double): Response<GenericResponse> {
        val req = HashMap<String, Any>()
        req["room_id"] = roomId
        req["month"] = month
        req["rent"] = rent
        req["water"] = water
        req["electricity"] = electricity
        req["other"] = other
        return api.createInvoice(req)
    }

    suspend fun getAllMaintenanceRequests(): Response<List<MaintenanceRequest>> {
        return api.getAllMaintenanceRequests()
    }

    suspend fun updateMaintenanceStatus(requestId: Int, status: String): Response<GenericResponse> {
        val req = HashMap<String, String>()
        req["status"] = status
        return api.updateMaintenanceStatus(requestId, req)
    }

    suspend fun getAnnouncements(): Response<List<Announcement>> {
        return api.getAnnouncements()
    }

    suspend fun postAnnouncement(title: String, content: String, adminId: Int): Response<GenericResponse> {
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val createdByBody = adminId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return api.createAnnouncement(titleBody, contentBody, createdByBody, null)
    }

    suspend fun getRoomInvoices(roomId: Int): Response<List<Invoice>> {
        return api.getRoomInvoices(roomId)
    }

    suspend fun removeTenant(roomId: Int): Response<GenericResponse> {
        return api.removeTenant(roomId)
    }
    suspend fun updateRoom(roomId: Int, roomNumber: String, floor: Int?, price: Double?) =
        api.updateRoom(roomId, mapOf<String, Any>( // ระบุ <String, Any> ตรงนี้
            "room_number" to roomNumber,
            "floor" to (floor ?: 0),   // จัดการค่า Null ให้เรียบร้อย
            "price" to (price ?: 0.0), // จัดการค่า Null ให้เรียบร้อย
            "description" to ""
        ))
    suspend fun deleteRoom(roomId: Int) = api.deleteRoom(roomId)
}
