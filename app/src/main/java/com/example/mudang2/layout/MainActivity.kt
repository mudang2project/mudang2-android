package com.example.mudang2.layout

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mudang2.R
import com.example.mudang2.databinding.ActivityMainBinding
import com.example.mudang2.remote.NetworkModule
import com.example.mudang2.remote.WeatherApi
import com.example.mudang2.remote.WeatherResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap

    private val database = Firebase.database // firebase 선언 초기화
    private val cameraRef = database.getReference("Camera")
    private val gpsRef = database.getReference("GPS")

    private var temp: String? = null // 온도 ex)26
    private var baseTime: String? = null
    private var tmpIdx: Int? = null
    private var skyIdx: Int? = null
    private var ptyIdx: Int? = null
    private var todayDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPeopleCnt() // 대기인원 파이어베이스 리스너
        getLocate() // gps 파이어베이스 리스너
        
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHourDate()

        weatherStatus(
            "JSON", 36, 1,
            todayDate!!, baseTime!!, 62, 124
        )
        // Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)

    }
    // 구글맵 API
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 학교 초기 위치 설정
        val latLng = LatLng(37.4525, 127.1312)
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
                        else -> binding.homeWeatherImageIv.setImageResource(R.drawable.ic_rain) // 1, 2, 4 일 때 눈
                    }

                    Log.d("GET/SUCCESSS", response.body()?.response!!.body.items.item[tmpIdx!!].toString()) // SKY // 0  5  6
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
    // 가져올 데이터의 날짜 선택
    private fun setHourDate() {
        var currentHour = getCurrentHour()

        if (currentHour[0] == '0') { // 현재 시각이 오전일 때
            currentHour = currentHour[1].toString() // 0 제거
            Log.d("HOUR", currentHour)
        }
        if (currentHour.toInt() <= 2) // 0, 1, 2
            getCurrentDate(1) // 정각, 새벽 1시, 새벽 2시는 어제 데이터 받아옴
        else
            getCurrentDate(0)
        // baseTime 세팅
        setBaseTime(currentHour.toInt())
        Log.d("TESTTEST", currentHour)
    }

    // 현재 시각 불러오기
    private fun getCurrentHour(): String{
        val formatter = SimpleDateFormat("HH", Locale.getDefault()) // 00 01 ... 23
        return formatter.format(Calendar.getInstance().time)
    }

    // 현재 날짜 불러오기
    private fun getCurrentDate(beforeDay: Int) {
        val cal = Calendar.getInstance()
        var year = cal.get(Calendar.YEAR).toString()
        var month = (cal.get(Calendar.MONTH) + 1).toString() // 0부터 시작해서 +1
        var day = (cal.get(Calendar.DATE) - beforeDay).toString()

        if (month.toInt() <= 9)
            month = "0$month"

        if (day.toInt() <= 9)
            day = "0$day"

        todayDate = year + month + day

        Log.d("TESTTEST", todayDate.toString())
    }

    // 파이어베이스에 저장된 사람 수 가져오기
    private fun getPeopleCnt() {
        var cnt: String

        cameraRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
                        binding.homeWaitNumberTv.text = "${data.value.toString()}명"

                        cnt = data.value.toString()

                        if (cnt.toInt() <= 5) {
                            binding.homeWaitConditionTv.text = "원활"
                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#04D900"))
                        }
                        else if (cnt.toInt() in 6..15) {
                            binding.homeWaitConditionTv.text = "보통"
                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF9110"))
                        }
                        else {
                            binding.homeWaitConditionTv.text = "혼잡"
                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF0000"))
                        }
                   }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("FIREBASE", error.toString())
            }
        })
    }

    // 파이어베이스에 저장된 무당이 위치 가져오기
    private fun getLocate() {
        gpsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
                        val locate = data.value.toString().split(" ")
                        val lat = locate[0].toDouble()
                        val long = locate[1].toDouble()

                        mMap.clear()

                        val polyline = mMap.addPolyline(PolylineOptions()
                            .clickable(false)
                            .add(
                                LatLng(37.4519, 127.1312),
                                LatLng(37.4526, 127.1307),
                                LatLng(37.4526, 127.1305),
                                LatLng(37.4525, 127.1300),
                                LatLng(37.4521, 127.1295),
                                LatLng(37.4517, 127.1275),
                                LatLng(37.4506, 127.1275),
                                LatLng(37.4499, 127.1299),
                                LatLng(37.4509, 127.1303),
                                LatLng(37.4512, 127.1308),
                                LatLng(37.4522, 127.1314),
                                LatLng(37.4524, 127.1319),
                                LatLng(37.4531, 127.1336),
                                LatLng(37.4535, 127.1342),
                                LatLng(37.4536, 127.1345),
                                LatLng(37.4537, 127.1347),
                                LatLng(37.4541, 127.1348),
                                LatLng(37.4555, 127.1347)
                            ))
                        polyline.color = -0x1110000
                        polyline.tag = "route"

                        mMap.addMarker(MarkerOptions().position(LatLng(lat, long)))!!.setIcon(
                            BitmapDescriptorFactory.fromResource(R.drawable.ladybug))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("FIREBASE", error.toString())
            }
        })
    }

    private fun setBaseTime(hour: Int){
        when(hour) {
            0 -> {
                baseTime = "2300" // 어제 날짜
                tmpIdx = 0
                skyIdx = 5
                ptyIdx = 6
            }
            1 -> {
                baseTime = "2300" // 어제 날짜
                tmpIdx = 12
                skyIdx = 17
                ptyIdx = 18
            }
            2 -> {
                baseTime = "2300" // 어제 날짜
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