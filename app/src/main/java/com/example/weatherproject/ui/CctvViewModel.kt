package com.example.weatherproject.ui

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 위치 정보를 담는 데이터 클래스
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val currentLocationForDistance: Location? // 거리 계산의 기준이 되는 현재 위치
)

@HiltViewModel
class CctvViewModel @Inject constructor(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _cctvInfo = MutableStateFlow<CctvInfo?>(null)
    val cctvInfo: StateFlow<CctvInfo?> = _cctvInfo

    private val _cctvError = MutableStateFlow<String?>(null)
    val cctvError: StateFlow<String?> = _cctvError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 사용자가 선택한 위치 정보 (검색 또는 현재 위치)
    private val _selectedLocationInfo = MutableStateFlow<LocationInfo?>(null)
    val selectedLocationInfo: StateFlow<LocationInfo?> = _selectedLocationInfo.asStateFlow()

    // 새로운 위치로 업데이트하고 CCTV 정보를 가져오는 함수
    fun updateSelectedLocation(lat: Double, lon: Double, address: String, currentLocation: Location?) {
        val newLocationInfo = LocationInfo(lat, lon, address, currentLocation)
        
        // 중복 체크 제거: 사용자가 명시적으로 검색/클릭했을 때는 항상 갱신
        _selectedLocationInfo.value = newLocationInfo
        fetchCctvByLocation()
    }
    
    fun retryFetchCctv() {
        fetchCctvByLocation()
    }

    // ViewModel 내부 상태를 기반으로 CCTV 정보를 가져오는 함수
    private fun fetchCctvByLocation() {
        val locationInfo = _selectedLocationInfo.value ?: return // 선택된 위치가 없으면 실행하지 않음

        viewModelScope.launch {
            _isLoading.value = true
            _cctvInfo.value = null
            _cctvError.value = null

            try {
                Log.d("CctvViewModel", "CCTV 검색: Lat=${locationInfo.latitude}, Lng=${locationInfo.longitude}")

                val result = repository.getNearbyCctv(lat = locationInfo.latitude, lng = locationInfo.longitude)

                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    
                    if (response.status == "success") {
                        val distance = locationInfo.currentLocationForDistance?.let { currentLoc ->
                            calculateDistance(
                                currentLoc.latitude,
                                currentLoc.longitude,
                                response.cctvLat.toDoubleOrNull() ?: 0.0,
                                response.cctvLng.toDoubleOrNull() ?: 0.0
                            )
                        }

                        val cctvData = CctvInfo(
                            cctvName = response.cctvName,
                            cctvUrl = response.cctvUrl,
                            type = response.cctvType,
                            roadName = response.cctvName.split(" ").firstOrNull() ?: "",
                            distance = distance?.let { String.format("%.1fkm", it) } ?: "",
                            latitude = response.cctvLat,
                            longitude = response.cctvLng
                        )
                        _cctvInfo.value = cctvData
                        Log.d("CctvViewModel", "CCTV 정보 업데이트 완료: ${cctvData.cctvName}")
                    } else {
                        _cctvError.value = "주변에 CCTV 정보가 없습니다."
                    }
                } else {
                     _cctvError.value = "CCTV 정보를 가져오는 데 실패했습니다."
                     Log.e("CctvViewModel", "CCTV API 호출 실패", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("CctvViewModel", "CCTV API 호출 실패: ${e.message}", e)
                _cctvError.value = "CCTV 정보를 가져오는 데 실패했습니다."
            } finally {
                _isLoading.value = false
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
