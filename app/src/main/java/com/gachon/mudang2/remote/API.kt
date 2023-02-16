package com.gachon.mudang2.remote

import com.gachon.mudang2.remote.camera.GetCameraHeadcountResponse
import com.gachon.mudang2.remote.gps.GetGpsLocationResponse
import retrofit2.Call
import retrofit2.http.GET

interface API {
    // 대기인원 수 가져오기
    @GET("/cameras/headcount")
    fun getHeadcount() : Call<GetCameraHeadcountResponse>

    // 위치정보(위도, 경도) 가져오기
    @GET("/gps/location")
    fun getLocation() : Call<GetGpsLocationResponse>
}