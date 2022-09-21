package com.example.mudang2.layout

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private var temp: String? = null // 온도 ex)26
    private var baseTime: String? = null
    private var tmpIdx: Int? = null
    private var skyIdx: Int? = null
    private var ptyIdx: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val todayDate = getCurrentDate()
        var currentHour = getCurrentHour()

        if (currentHour[0] == '0') { // 현재 시각이 오전일 때
            currentHour = currentHour.replace("0","")
            Log.d("HOUR", currentHour)
        }
        // baseTime 세팅
        setBaseTime(currentHour.toInt())

        weatherStatus("JSON", 36, 1,
            todayDate, baseTime!!, 62, 124)
        // Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
        // API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
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
        baseData: String,
        baseTime: String,
        nx: Int,
        ny: Int
    ) {

        val getWeatherService = NetworkModule().getRetrofit()?.create(WeatherApi::class.java)

        getWeatherService?.getWeather(dataType, numOfRows, pageNo, baseData, baseTime, nx, ny)
            ?.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    // 온도 바인딩
                    temp = response.body()?.response!!.body.items.item[tmpIdx!!].fcstValue
                    binding.homeWeatherTempTv.text = temp + "ºC"

                    // 맑음, 흐림 바인딩
                    if (response.body()?.response!!.body.items.item[skyIdx!!].fcstValue == "1") {
                        binding.homeWeatherImageIv.setImageResource(R.drawable.ic_sun) // 1일 때 맑음
                    } else {
                        binding.homeWeatherImageIv.setImageResource(R.drawable.ic_cloud) // 3, 4일 때 흐림
                    }

                    // 비, 눈 바인딩
                    when (response.body()?.response!!.body.items.item[ptyIdx!!].fcstValue) {
                        "0" -> null
                        "3" -> binding.homeWeatherImageIv.setImageResource(R.drawable.ic_snow) // 3일 때 눈
                        else -> binding.homeWeatherImageIv.setImageResource(R.drawable.ic_rain) // 3일 때 눈
                    }

                    Log.d("GET/SUCCESSS", response.body()?.response!!.body.items.item[tmpIdx!!].toString()) // SKY // 5 17 29
                    Log.d("GET/SUCCESSS", response.body()?.response!!.body.items.item[skyIdx!!].toString()) // SKY // 5 17 29
                    Log.d("GET/SUCCESSS", response.body()?.response!!.body.items.item[ptyIdx!!].toString()) // PTY // 6 18 30

//                    Log.d("GET/SUCCESS", response.body()?.response!!.toString())
//                    for (i in response.body()?.response!!.body.items.item){
//                        Log.d("GET/SUCCESS", "$i")
//                    }
                }
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.d("GET/FAIL", t.message.toString())
                }
            })
    }

    // 현재 날짜 불러오기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String{
        val localDate: LocalDate = LocalDate.now()
        return localDate.toString().replace("-","") // 현재 날짜 파싱
    }

    // 현재 시각 불러오기
    private fun getCurrentHour(): String{
        val formatter = SimpleDateFormat("HH", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    private fun setBaseTime(hour: Int){
        when(hour) {
            0 -> {
                baseTime = "2300"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            1 -> {
                baseTime = "2300"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            2 -> {
                baseTime = "2300"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            3 -> {
                baseTime = "0200"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            4 -> {
                baseTime = "0200"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            5 -> {
                baseTime = "0200"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            6 -> {
                baseTime = "0500"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            7 -> {
                baseTime = "0500"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            8 -> {
                baseTime = "0500"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            9 -> {
                baseTime = "0800"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            10 -> {
                baseTime = "0800"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            11 -> {
                baseTime = "0800"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            12 -> {
                baseTime = "1100"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            13 -> {
                baseTime = "1100"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            14 -> {
                baseTime = "1100"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            15 -> {
                baseTime = "1400"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            16 -> {
                baseTime = "1400"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            17 -> {
                baseTime = "1400"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            18 -> {
                baseTime = "1700"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            19 -> {
                baseTime = "1700"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            20 -> {
                baseTime = "1700"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
            21 -> {
                baseTime = "2000"
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            22 -> {
                baseTime = "2000"
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            23 -> {
                baseTime = "2000"
                tmpIdx = 24
                skyIdx = 29
                ptyIdx = 30
            }
        }
    }
}