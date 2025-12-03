package com.example.weatherproject.ui

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CctvViewModel(application: Application) : AndroidViewModel(application) {

    private val _cctvInfo = MutableStateFlow<CctvInfo?>(null)
    val cctvInfo: StateFlow<CctvInfo?> = _cctvInfo

    private val _cctvError = MutableStateFlow<String?>(null)
    val cctvError: StateFlow<String?> = _cctvError

    fun fetchCctvByLocation(lat: Double, lon: Double, currentLocation: Location?) {
        viewModelScope.launch {
            _cctvInfo.value = null // 이전 CCTV 정보 초기화
            _cctvError.value = null // 이전 에러 초기화

            try {
                Log.d("CctvViewModel", "CCTV 검색: Lat=$lat, Lng=$lon")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.weatherApi.getNearbyCctv(lat = lat, lng = lon)
                }

                if (response.status == "success") {
                    val distance = currentLocation?.let { currentLoc ->
                        calculateDistance(
                            currentLoc.latitude,
                            currentLoc.longitude,
                            response.cctvLat.toDoubleOrNull() ?: 0.0,
                            response.cctvLng.toDoubleOrNull() ?: 0.0
                        )
                    }

                    val cctvInfo = CctvInfo(
                        cctvName = response.cctvName,
                        cctvUrl = response.cctvUrl,
                        type = response.cctvType,
                        roadName = response.cctvName.split(" ").firstOrNull() ?: "",
                        distance = distance?.let { String.format("%.1fkm", it) } ?: "",
                        latitude = response.cctvLat,
                        longitude = response.cctvLng
                    )
                    _cctvInfo.value = cctvInfo
                    Log.d("CctvViewModel", "CCTV 정보 업데이트 완료: ${cctvInfo.cctvName}")
                } else {
                    _cctvError.value = "주변에 CCTV 정보가 없습니다."
                }
            } catch (e: Exception) {
                Log.e("CctvViewModel", "CCTV API 호출 실패: ${e.message}", e)
                _cctvError.value = "CCTV 정보를 가져오는 데 실패했습니다."
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // 지구 반지름 (km)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
