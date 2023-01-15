package com.example.mudang2.layout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mudang2.databinding.ActivityMainBinding
import com.example.mudang2.remote.NetworkModule
import com.example.mudang2.remote.camera.CameraResult
import com.example.mudang2.remote.camera.GetCameraHeadcountService
import com.example.mudang2.remote.camera.GetCameraHeadcountView
import com.example.mudang2.remote.gps.GetGpsLocationService
import com.example.mudang2.remote.gps.GetGpsLocationView
import com.example.mudang2.remote.gps.GpsResult
import com.example.mudang2.remote.weather.WeatherApi
import com.example.mudang2.remote.weather.WeatherResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GetCameraHeadcountView, GetGpsLocationView {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap

    private var nowTime: Int? = null
    private var temp: String? = null // 온도 ex)26
    private var baseTime: String? = null
    private var todayDate: String? = null
    private var tmpIdx: Int? = null // 온도
    private var skyIdx: Int? = null // 하능상태
    private var ptyIdx: Int? = null // 강수상태

    private var cnt: Int? = null // 대기인원 수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val display = this.applicationContext?.resources?.displayMetrics
        val deviceWidth = display?.widthPixels
        val deviceHeight = display?.heightPixels

        // 네트워크 에러 발생
        if(!isNetworkAvailable(this)){
            val networkErrorDialog = NetworkErrorDialog(deviceWidth!!, deviceHeight!!)
            networkErrorDialog.show(this.supportFragmentManager, "error")
        }

        // 구글맵 초기화
        val mapFragment = supportFragmentManager.findFragmentById(com.example.mudang2.R.id.home_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//       // 운행시간 외 CLOSED 페이지 띄우기
//       if (nowTime !in 8..18) {
//            binding.homeClosedPageCl.visibility = View.VISIBLE
//        }

        // 시간 가져오기
        setHourDate()

        // 기상청 api 연동 날씨 가져오기
        // Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
        weatherStatus(
            "JSON", 36, 1,
            todayDate!!, baseTime!!, 62, 124
        )

        // 정류장 대기인원 수 api 연동
        val getHeadcount = GetCameraHeadcountService()
        getHeadcount.setHeadcountView(this)
        // GPS 위도, 경도 api 연동
        val getGpsLocation = GetGpsLocationService()
        getGpsLocation.setLocationView(this)

        // 대기인원 수 호출 쓰레드
        thread(start = true) {
            while(true) {
                getHeadcount.getHeadcount()
                Thread.sleep(10000)
            }
        }
    }

    // 네트워크 확인 함수
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    // 구글맵 API
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 학교 초기 위치 설정
        val latLng = LatLng(37.4525, 127.1312)
        // 카메라 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f))

        // 비트맵 아이콘 크기 조절
        val bitmapDraw = resources.getDrawable(com.example.mudang2.R.drawable.ic_bus_1) as BitmapDrawable
        val b = bitmapDraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, 60, 100, false)

        var marker1 : Marker = mMap.addMarker(MarkerOptions().position(LatLng(37.4519, 127.1312)))!!
        marker1!!.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker))

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
        polyline.color = -0x009999
        polyline.tag = "route"
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
        val status = "weather"
        val getWeatherService = NetworkModule().getRetrofit(status)?.create(WeatherApi::class.java)

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
                        binding.homeWeatherImageIv.setImageResource(com.example.mudang2.R.drawable.ic_sun) // 1일 때 맑음
                    } else {
                        binding.homeWeatherImageIv.setImageResource(com.example.mudang2.R.drawable.ic_cloud) // 3, 4일 때 흐림
                    }
                    // 비, 눈 바인딩
                    when (response.body()?.response!!.body.items.item[ptyIdx!!].fcstValue) {
                        "0" -> null
                        "3" -> binding.homeWeatherImageIv.setImageResource(com.example.mudang2.R.drawable.ic_snow) // 3일 때 눈
                        else -> binding.homeWeatherImageIv.setImageResource(com.example.mudang2.R.drawable.ic_rain) // 1, 2, 4 일 때 눈
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

        nowTime = currentHour.toInt()

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

//    // 파이어베이스에 저장된 사람 수 가져오기
//    private fun getPeopleCnt() {
//        var cnt: String
//
//        cameraRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (data in snapshot.children) {
//                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
//                        binding.homeWaitNumberTv.text = "${data.value.toString()}명"
//
//                        cnt = data.value.toString()
//
//                        if (cnt.toInt() <= 5) {
//                            binding.homeWaitConditionTv.text = "원활"
//                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#04D900"))
//                        }
//                        else if (cnt.toInt() in 6..15) {
//                            binding.homeWaitConditionTv.text = "보통"
//                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF9110"))
//                        }
//                        else {
//                            binding.homeWaitConditionTv.text = "혼잡"
//                            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF0000"))
//                        }
//                   }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("FIREBASE", error.toString())
//            }
//        })
//    }
//
//    // 파이어베이스에 저장된 무당이 위치 가져오기
//    private var marker1 : Marker? = null
//    private fun getLocate1() {
//        gps1Ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    if (marker1 != null)
//                        marker1!!.remove()
//                    for (data in snapshot.children) {
//                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
//                        val locate = data.value.toString().split(" ")
//                        var lat1 = locate[0].toDouble()
//                        var long1 = locate[1].toDouble()
//
//                        marker1 = mMap.addMarker(MarkerOptions().position(LatLng(lat1, long1)))!!
//                        marker1!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ladybug))
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("FIREBASE", error.toString())
//            }
//        })
//    }
//
//    private var marker2 : Marker? = null
//    private fun getLocate2() {
//        gps2Ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    if (marker2 != null)
//                        marker2!!.remove()
//                    for (data in snapshot.children) {
//                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
//                        val locate = data.value.toString().split(" ")
//                        var lat2 = locate[0].toDouble()
//                        var long2 = locate[1].toDouble()
//
//                        marker2 = mMap.addMarker(MarkerOptions().position(LatLng(lat2, long2)))!!
//                        marker2!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ladybug))
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("FIREBASE", error.toString())
//            }
//        })
//    }
//
//    private var marker3 : Marker? = null
//    private fun getLocate3() {
//        gps3Ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    if (marker3 != null)
//                        marker3!!.remove()
//                    for (data in snapshot.children) {
//                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
//                        val locate = data.value.toString().split(" ")
//                        var lat3 = locate[0].toDouble()
//                        var long3 = locate[1].toDouble()
//
//                        marker3 = mMap.addMarker(MarkerOptions().position(LatLng(lat3, long3)))!!
//                        marker3!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ladybug))
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("FIREBASE", error.toString())
//            }
//        })
//    }
//
//    private var marker4 : Marker? = null
//    private fun getLocate4() {
//        gps4Ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    if (marker4 != null)
//                        marker4!!.remove()
//                    for (data in snapshot.children) {
//                        Log.d("FIREBASE", "ValueEventListener-onDataChange : ${data.value}")
//                        val locate = data.value.toString().split(" ")
//                        var lat4 = locate[0].toDouble()
//                        var long4 = locate[1].toDouble()
//
//                        marker4 = mMap.addMarker(MarkerOptions().position(LatLng(lat4, long4)))!!
//                        marker4!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ladybug))
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("FIREBASE", error.toString())
//            }
//        })
//    }

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

    // 대기인원 수 조회 성공
    override fun onGetHeadcountSuccess(result: CameraResult) {
        cnt = result.headCount

        binding.homeWaitNumberTv.text = "${cnt.toString()}명"

        if (cnt!! <= 5) {
            binding.homeWaitConditionTv.text = "원활"
            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#04D900"))
        } else if (cnt!!.toInt() in 6..15) {
            binding.homeWaitConditionTv.text = "보통"
            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF9110"))
        } else {
            binding.homeWaitConditionTv.text = "혼잡"
            binding.homeWaitConditionTv.setTextColor(Color.parseColor("#FF0000"))
        }
    }

    // 대기인원 수 조회 실패
    override fun onGetHeadcountFailure(code: Int, message: String) {
        Log.d("[CAMERA] GET / FAILURE", "$code $message")
        binding.homeWaitNumberTv.text = "점검 중"
        binding.homeWaitConditionTv.text = ""
    }

    // 위도, 경도 조회 성공
    override fun onGetLocationSuccess(result: GpsResult) {

    }

    override fun onGetLocationFailure(code: Int, message: String) {
        Log.d("[GPS] GET / FAILURE", "$code $message")
    }
}