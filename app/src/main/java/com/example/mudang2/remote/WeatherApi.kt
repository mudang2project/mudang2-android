package com.example.mudang2.remote

import com.example.mudang2.ApiKey.Companion.API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("getVilageFcst?serviceKey=$API_KEY")
    fun getWeather(
        @Query("dataType") dataType : String,
        @Query("numOfRows") numOfRows : Int,
        @Query("pageNo") pageNo : Int,
        @Query("base_date") baseDate : String,
        @Query("base_time") baseTime : String,
        @Query("nx") nx : Int,
        @Query("ny") ny : Int
    ) : Call<WeatherResponse>
}