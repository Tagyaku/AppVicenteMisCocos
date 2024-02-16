package com.example.appvicentemiscocos

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
        @GET("?inc=picture&results=3")
        fun getRandomUserPictures(): Call<ApiResponse>

}



