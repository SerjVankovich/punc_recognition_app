package com.example.punkrecognition.service

import com.example.punkrecognition.model.Picture
import com.example.punkrecognition.model.Prediction
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {
    @POST("/predict/")
    fun sendPictureForRecognize(@Body picture: Picture): Call<Prediction>

    @GET("/locations/sample/{name}")
    fun getMushroomSamples(@Path("name") mushroomName: String, @Query("num") num: Int): Call<List<String>>
}