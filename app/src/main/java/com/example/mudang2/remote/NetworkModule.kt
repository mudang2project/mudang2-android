package com.example.mudang2.remote

import com.example.mudang2.ApiKey.Companion.BASE_URL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkModule {
    private var retrofit: Retrofit? = null
    private val gson : Gson = GsonBuilder().setLenient().create()

    fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            synchronized(this) {
                retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
        }
        return retrofit
    }
}