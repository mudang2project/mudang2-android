package com.gachon.mudang2.remote.gps

interface GetGpsLocationView {
    fun onGetLocationSuccess(result: List<GpsResult>)
    fun onGetLocationFailure(code: Int, message: String)
}