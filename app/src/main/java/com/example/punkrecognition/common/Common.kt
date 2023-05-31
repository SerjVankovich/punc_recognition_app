package com.example.punkrecognition.common

import com.example.punkrecognition.BuildConfig
import com.example.punkrecognition.service.RetrofitClient
import com.example.punkrecognition.service.RetrofitService

object Common {
    private val BASE_URL = BuildConfig.API_URL
    val retrofitService: RetrofitService
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitService::class.java)
}