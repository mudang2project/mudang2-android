package com.example.mudang2.layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mudang2.ApiKey.Companion.TAG
import com.example.mudang2.R
import com.example.mudang2.databinding.ActivityMainBinding
import com.example.mudang2.remote.NetworkModule
import com.example.mudang2.remote.WeatherApi
import com.example.mudang2.remote.WeatherResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import retrofit2.http.Query

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        weatherStatus("JSON",10,1,
            20220918,1900,"62","124")
    }

    // 구글맵 API
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 학교 초기 위치 설정
        val latLng = LatLng(37.4525, 127.1312)
        // 무당이 위치 마커 표시
//      mMap.addMarker(MarkerOptions().position(latLng).title("가천대학교"))
        // 카메라 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f))
    }

    // 날씨 연동 API
    private fun weatherStatus(
        dataType: String,
        numOfRows: Int,
        pageNo: Int,
        baseData: Int,
        baseTime: Int,
        nx: String,
        ny: String
    ) {

        val getWeatherService = NetworkModule().getRetrofit()?.create(WeatherApi::class.java)

        getWeatherService?.getWeather(dataType, numOfRows, pageNo, baseData, baseTime, nx, ny)
            ?.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    Log.d(TAG, response.toString())
                    Log.d(TAG, response.body().toString())
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.d("GET / FAILURE", t.message.toString())
                }
            })
    }
}