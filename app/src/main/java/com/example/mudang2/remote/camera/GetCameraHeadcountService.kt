package com.example.mudang2.remote.camera

import android.util.Log
import com.example.mudang2.remote.API
import com.example.mudang2.remote.NetworkModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetCameraHeadcountService {
    private lateinit var getHeadcountView: GetCameraHeadcountView

    fun setHeadcountView(getHeadcountView: GetCameraHeadcountView) {
        this.getHeadcountView = getHeadcountView
    }

    fun getHeadcount() {
        val status = "server"
        val getCameraHeadcountService = NetworkModule().getRetrofit(status)?.create(API::class.java)

        getCameraHeadcountService?.getHeadcount()?.enqueue(object : Callback<GetCameraHeadcountResponse> {
            override fun onResponse(
                call: Call<GetCameraHeadcountResponse>,
                response: Response<GetCameraHeadcountResponse>
            ) {
                Log.d("[Camera] GET / SUCCESS", response.toString())

                val resp: GetCameraHeadcountResponse = response.body()!!

                when(val code = resp.code) {
                    1000 -> getHeadcountView.onGetHeadcountSuccess(resp.result)
                    else -> getHeadcountView.onGetHeadcountFailure(code, resp.message)
                }
            }

            override fun onFailure(call: Call<GetCameraHeadcountResponse>, t: Throwable) {
                Log.d("[Camera] GET / FAILURE", t.message.toString())
            }
        })
    }
}