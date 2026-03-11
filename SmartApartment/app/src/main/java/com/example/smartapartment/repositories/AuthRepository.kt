package com.example.smartapartment.repositories

import com.example.smartapartment.api.RetrofitClient
import com.example.smartapartment.models.LoginResponse
import com.example.smartapartment.models.RegisterResponse
import retrofit2.Response

class AuthRepository {
    private val api = RetrofitClient.api

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val req = HashMap<String, String>()
        req["email"] = email
        req["password"] = password
        return api.login(req)
    }

    suspend fun register(name: String, email: String, password: String, phone: String?): Response<RegisterResponse> {
        val req = HashMap<String, String>()
        req["name"] = name
        req["email"] = email
        req["password"] = password
        if (phone != null) req["phone"] = phone
        req["role"] = "tenant"
        return api.register(req)
    }
}
