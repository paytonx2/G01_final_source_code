package com.example.smartapartment.api

import com.example.smartapartment.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: SmartDormApi by lazy {
        retrofit.create(SmartDormApi::class.java)
    }
}
