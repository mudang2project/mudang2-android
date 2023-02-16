package com.gachon.mudang2.remote.camera

interface GetCameraHeadcountView {
    fun onGetHeadcountSuccess(result: CameraResult)
    fun onGetHeadcountFailure(code: Int, message: String)
}