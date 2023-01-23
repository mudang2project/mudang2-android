package com.example.mudang2.remote.gps

import android.util.Log
import com.example.mudang2.remote.API
import com.example.mudang2.remote.NetworkModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetGpsLocationService {
    private lateinit var getLocationView: GetGpsLocationView

    fun setLocationView(getLocationView: GetGpsLocationView) {
        this.getLocationView = getLocationView
    }

    fun getLocation() {
        val status = "server"
        val getGpsLocationService = NetworkModule().getRetrofit(status)?.create(API::class.java)

        getGpsLocationService?.getLocation()?.enqueue(object : Callback<GetGpsLocationResponse> {
            override fun onResponse(
                call: Call<GetGpsLocationResponse>,
                response: Response<GetGpsLocationResponse>
            ) {
                Log.d("[GPS] GET / SUCCESS", response.toString())

                val resp: GetGpsLocationResponse = response.body()!!

                when(val code = resp.code) {
                    1000 -> getLocationView.onGetLocationSuccess(resp.result)
                    else -> getLocationView.onGetLocationFailure(code, resp.message)
                }
            }

            override fun onFailure(call: Call<GetGpsLocationResponse>, t: Throwable) {
                Log.d("[GPS] GET / FAILURE", t.message.toString())
            }
        })
    }
}