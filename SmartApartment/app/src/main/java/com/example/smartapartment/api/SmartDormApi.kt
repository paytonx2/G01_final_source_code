package com.example.smartapartment.api

import com.example.smartapartment.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SmartDormApi {

    // Auth
    @POST("register")
    suspend fun register(@Body request: HashMap<String, String>): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: HashMap<String, String>): Response<LoginResponse>

    // Rooms (Admin)
    @GET("rooms")
    suspend fun getRooms(): Response<List<Room>>

    @GET("admin/summary")
    suspend fun getAdminSummary(): Response<AdminSummary>

    @POST("rooms")
    suspend fun addRoom(@Body request: HashMap<String, Any>): Response<RoomResponse>

    @GET("users")
    suspend fun getUsers(@Query("role") role: String? = null): Response<List<User>>

    @POST("rooms/assign-tenant")
    suspend fun assignTenant(@Body request: HashMap<String, Any>): Response<GenericResponse>

    @PUT("rooms/{id}")
    suspend fun updateRoomStatus(
        @Path("id") id: Int, 
        @Body request: HashMap<String, String>
    ): Response<GenericResponse>

    // Tenant My Info
    @GET("myroom")
    suspend fun getMyRoom(@Query("user_id") userId: Int): Response<List<MyRoom>>

    @GET("myinvoice")
    suspend fun getMyInvoices(@Query("user_id") userId: Int): Response<List<Invoice>>

    // Payments
    @Multipart
    @POST("upload-slip")
    suspend fun uploadSlip(
        @Part("invoice_id") invoiceId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part slip_image: MultipartBody.Part
    ): Response<GenericResponse>

    // Maintenance
    @Multipart
    @POST("maintenance")
    suspend fun createMaintenanceRequest(
        @Part("room_id") roomId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<GenericResponse>

    @GET("maintenance")
    suspend fun getAllMaintenanceRequests(): Response<List<MaintenanceRequest>>

    @PUT("maintenance/{id}")
    suspend fun updateMaintenanceStatus(
        @Path("id") id: Int,
        @Body request: HashMap<String, String>
    ): Response<GenericResponse>

    // Announcements
    @GET("announcements")
    suspend fun getAnnouncements(): Response<List<Announcement>>

    @Multipart
    @POST("announcements")
    suspend fun createAnnouncement(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("created_by") createdBy: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<GenericResponse>

    // สำหรับแก้ไขข้อมูลห้อง
    @PUT("rooms/{id}/update")
    suspend fun updateRoom(
        @Path("id") roomId: Int,
        @Body roomData: @JvmSuppressWildcards Map<String, Any> // เพิ่ม @JvmSuppressWildcards เข้าไป
    ): Response<ResponseBody>

    // สำหรับลบห้อง
    @DELETE("rooms/{id}")
    suspend fun deleteRoom(
        @Path("id") roomId: Int
    ): Response<ResponseBody>

    // Invoices
    @POST("create-invoice")
    suspend fun createInvoice(@Body request: HashMap<String, Any>): Response<GenericResponse>

    @GET("invoice/{room_id}")
    suspend fun getRoomInvoices(@Path("room_id") roomId: Int): Response<List<Invoice>>

    // Room Tenant Management
    @DELETE("rooms/{id}/tenant")
    suspend fun removeTenant(@Path("id") roomId: Int): Response<GenericResponse>

    // สำหรับดึงรายการที่ลูกค้าแจ้งโอนเข้ามา
    @GET("admin/payments")
    suspend fun getAllPayments(): Response<List<Payment>>

    // สำหรับกด Approve หรือ Reject สลิป
    @PUT("admin/payments/{id}/status") // 👈 เช็คตรงนี้ว่ามี /status ไหม?
    suspend fun updatePaymentStatus(
        @Path("id") paymentId: Int,
        @Body request: HashMap<String, String>
    ): Response<GenericResponse>
}
