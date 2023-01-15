package com.example.mudang2.remote.gps

data class GetGpsLocationResponse (
    var isSuccess: Boolean,
    var code: Int,
    var message: String,
    var result: GpsResult
    )

data class GpsResult (
    var busIdx: Int,
    var lat: String,
    var lon: String
    )