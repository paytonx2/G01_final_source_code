package com.example.smartapartment.models

data class User(
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

data class LoginResponse(
    val message: String,
    val user: User
)

data class RegisterResponse(
    val message: String,
    val user_id: Int
)

data class Room(
    val room_id: Int,
    val room_number: String,
    val floor: Int,
    val price: Double,
    val status: String,
    val description: String?
)

data class RoomResponse(
    val message: String,
    val room_id: Int? = null
)

data class MyRoom(
    val room_id: Int,
    val room_number: String,
    val floor: Int,
    val price: Double,
    val status: String,
    val description: String?,
    val move_in_date: String
)

data class Invoice(
    val invoice_id: Int,
    val room_id: Int,
    val month: String,
    val rent: Double,
    val water: Double,
    val electricity: Double,
    val other: Double,
    val total: Double,
    val status: String,
    val created_at: String
)

data class Payment(
    val payment_id: Int,
    val invoice_id: Int,
    val user_id: Int,
    val amount: Double,
    val slip_image: String,
    val status: String,
    val created_at: String,
    val tenant_name: String? = null,
    val month: String? = null,
    val invoice_total: Double? = null
)

data class MaintenanceRequest(
    val request_id: Int,
    val room_id: Int,
    val user_id: Int,
    val title: String,
    val description: String,
    val image: String?,
    val status: String,
    val created_at: String
)

data class Announcement(
    val announce_id: Int,
    val title: String,
    val content: String,
    val image: String?,
    val created_by: Int,
    val created_at: String
)

data class AdminSummary(
    val total_rooms: Int,
    val available_rooms: Int,
    val pending_slips: Int,
    val pending_repairs: Int
)

data class GenericResponse(
    val message: String,
    val error: Boolean = false
)
