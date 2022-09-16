package com.example.mudang2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mudang2.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 학교 초기 위치 설정
        val latLng = LatLng(37.4525, 127.1312)
        // 무당이 위치 마커 표시
//      mMap.addMarker(MarkerOptions().position(latLng).title("가천대학교"))
        // 카메라 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f))
    }
}