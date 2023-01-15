package com.example.mudang2.remote.gps

interface GetGpsLocationView {
    fun onGetLocationSuccess(result: GpsResult)
    fun onGetLocationFailure(code: Int, message: String)
}