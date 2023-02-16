package com.gachon.mudang2.remote

import com.gachon.mudang2.ApiKey.Companion.BASE_URL
import com.gachon.mudang2.ApiKey.Companion.SERVER_URL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkModule {
    private var retrofit: Retrofit? = null
    private val gson: Gson = GsonBuilder().setLenient().create()
    lateinit var URL: String

    fun getRetrofit(status: String): Retrofit? {
        URL = if (status == "weather") BASE_URL else SERVER_URL

        if (retrofit == null) {
            synchronized(this) {
                retrofit = Retrofit.Builder().baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
        }
        return retrofit
    }
}