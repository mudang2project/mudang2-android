package com.gachon.mudang2.remote.camera

data class GetCameraHeadcountResponse (
    var isSuccess: Boolean,
    var code: Int,
    var message: String,
    var result: CameraResult
    )

data class CameraResult (
    var headCount: Int,
    var interval: Long
    )